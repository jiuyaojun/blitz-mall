#!/usr/bin/env bash
# Blitz 部署修复脚本 — 一次性完成所有修复
# 在 Git Bash 中执行: bash deploy-fix.sh
set -e

SERVER="ubuntu@119.28.151.191"
SSH_KEY="$HOME/.ssh/blitz"
BLITZ_DIR="/home/ubuntu/blitz"
LOCAL_DIR="C:/watasinokoto/blitz"

echo "============================================"
echo " Blitz 服务器部署修复"
echo "============================================"

# ---- Step 1: 创建完整的 shim 类 ----
echo "[1/6] 创建 shim 类..."

# DataSourceAutoConfiguration shim (空壳即可)
cat > /tmp/DataSourceAutoConfiguration.java << 'JAVA'
package org.springframework.boot.autoconfigure.jdbc;

/**
 * Spring Boot 4.0 compatibility shim.
 * MyBatis-Plus and Druid reference this old package path.
 * In SB 4.0, the real class moved to org.springframework.boot.jdbc.autoconfigure.
 */
public class DataSourceAutoConfiguration {
}
JAVA

# DataSourceProperties shim (需要 @ConfigurationProperties)
cat > /tmp/DataSourceProperties.java << 'JAVA'
package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot 4.0 compatibility shim.
 * Must have @ConfigurationProperties for MyBatis-Plus/Druid to bind.
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private ClassLoader classLoader;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    public String determineDriverClassName() { return driverClassName; }
    public ClassLoader getClassLoader() { return classLoader; }
    public void setClassLoader(ClassLoader classLoader) { this.classLoader = classLoader; }
}
JAVA

# ---- Step 2: 上传所有文件到服务器 ----
echo "[2/6] 上传文件到服务器..."
scp -i "$SSH_KEY" /tmp/DataSourceAutoConfiguration.java "$SERVER:/tmp/"
scp -i "$SSH_KEY" /tmp/DataSourceProperties.java "$SERVER:/tmp/"
scp -i "$SSH_KEY" "$LOCAL_DIR/blitz-server/src/main/resources/application.yml" "$SERVER:/tmp/app.yml"
scp -i "$SSH_KEY" "$LOCAL_DIR/blitz-server/src/main/resources/application-dev.yml" "$SERVER:/tmp/app-dev.yml"
scp -i "$SSH_KEY" "$LOCAL_DIR/blitz-server/pom.xml" "$SERVER:/tmp/blitz-server-pom.xml"

# ---- Step 3: 服务器端操作 ----
echo "[3/6] 服务器端部署..."
ssh -i "$SSH_KEY" "$SERVER" << 'ENDSSH'
set -e
BLITZ_DIR="/home/ubuntu/blitz"

# 放置 shim 类
mkdir -p $BLITZ_DIR/blitz-common/src/main/java/org/springframework/boot/autoconfigure/jdbc
cp /tmp/DataSourceAutoConfiguration.java $BLITZ_DIR/blitz-common/src/main/java/org/springframework/boot/autoconfigure/jdbc/
cp /tmp/DataSourceProperties.java $BLITZ_DIR/blitz-common/src/main/java/org/springframework/boot/autoconfigure/jdbc/

# 放置配置文件
cp /tmp/app.yml $BLITZ_DIR/blitz-server/src/main/resources/application.yml
cp /tmp/app-dev.yml $BLITZ_DIR/blitz-server/src/main/resources/application-dev.yml
cp /tmp/blitz-server-pom.xml $BLITZ_DIR/blitz-server/pom.xml

# 禁用 ai profile
mv $BLITZ_DIR/blitz-server/src/main/resources/application-ai.yml $BLITZ_DIR/blitz-server/src/main/resources/application-ai.yml.disabled 2>/dev/null || true

# 构建
cd $BLITZ_DIR
echo "  Building..."
mvn package -DskipTests -q 2>&1
echo "  Build OK"

# 停止旧进程
pkill -f 'blitz-server.*jar' 2>/dev/null || true
sleep 3

# 启动 (先尝试 minimal profile)
echo "  Starting with minimal profile..."
set -a; source $BLITZ_DIR/.env; set +a
SPRING_PROFILES_ACTIVE=minimal nohup java -jar $BLITZ_DIR/blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > /home/ubuntu/logs/app.log 2>&1 &
PID=$!
echo "  Started PID $PID"

# 等待启动
echo "  Waiting for startup..."
for i in $(seq 1 20); do
    CODE=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ 2>/dev/null || echo "000")
    if [ "$CODE" = "200" ]; then
        echo "  READY! (${i}x3s)"
        exit 0
    fi
    sleep 3
done

echo "  FAILED with minimal profile - trying dev profile..."
# 回退到 dev profile
pkill -f 'blitz-server.*jar' 2>/dev/null || true
sleep 2
SPRING_PROFILES_ACTIVE=dev nohup java -jar $BLITZ_DIR/blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > /home/ubuntu/logs/app.log 2>&1 &
PID=$!
echo "  Started PID $PID with dev profile"

for i in $(seq 1 20); do
    CODE=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ 2>/dev/null || echo "000")
    if [ "$CODE" = "200" ]; then
        echo "  READY! (${i}x3s)"
        exit 0
    fi
    sleep 3
done

echo "  FAILED with dev profile too"
echo "  Last errors:"
head -80 /home/ubuntu/logs/app.log | grep -E "ERROR|Exception|Caused by" | head -10
exit 1
ENDSSH

echo ""
echo "============================================"
echo " 部署完成!"
echo "============================================"
