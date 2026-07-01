#!/usr/bin/env bash
# 快速修复 Redis + DataSource shim 并重建
set -e

SERVER="ubuntu@119.28.151.191"
SSH_KEY="$HOME/.ssh/blitz"
BLITZ_DIR="C:/watasinokoto/blitz"

echo "=== Blitz Redis修复 ==="

# 上传文件
echo "上传..."
scp -i "$SSH_KEY" C:/tmp/RedisAutoConfiguration.java "$SERVER:/tmp/"
scp -i "$SSH_KEY" C:/tmp/DataSourceProperties.java "$SERVER:/tmp/"
scp -i "$SSH_KEY" "$BLITZ_DIR/blitz-server/pom.xml" "$SERVER:/tmp/blitz-server-pom.xml"
scp -i "$SSH_KEY" "$BLITZ_DIR/blitz-server/src/main/resources/application.yml" "$SERVER:/tmp/app.yml"

# 服务器操作
ssh -i "$SSH_KEY" "$SERVER" << 'ENDSSH'
set -e
D=/home/ubuntu/blitz

# 放置 shim
mkdir -p $D/blitz-common/src/main/java/org/springframework/boot/autoconfigure/jdbc
mkdir -p $D/blitz-common/src/main/java/org/springframework/boot/autoconfigure/data/redis
cp /tmp/DataSourceProperties.java $D/blitz-common/src/main/java/org/springframework/boot/autoconfigure/jdbc/
cp /tmp/RedisAutoConfiguration.java $D/blitz-common/src/main/java/org/springframework/boot/autoconfigure/data/redis/
cp /tmp/blitz-server-pom.xml $D/blitz-server/pom.xml
cp /tmp/app.yml $D/blitz-server/src/main/resources/application.yml

# 构建
cd $D
echo "构建中..."
mvn package -DskipTests -q 2>&1 && echo "BUILD OK" || { echo "BUILD FAILED"; exit 1; }

# 停止旧进程
pkill -f 'blitz-server.*jar' 2>/dev/null || true
sleep 2

# 启动
echo "启动..."
set -a; source $D/.env; set +a
SPRING_PROFILES_ACTIVE=minimal nohup java -jar $D/blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > /home/ubuntu/logs/app.log 2>&1 &
echo "PID $!"

# 等待
echo -n "等待启动"
for i in $(seq 1 20); do
    CODE=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ 2>/dev/null || echo "000")
    if [ "$CODE" = "200" ]; then
        echo " READY!"
        curl -s http://localhost:8080/ | head -c 200
        exit 0
    fi
    echo -n "."
    sleep 3
done

echo " FAILED"
head -100 /home/ubuntu/logs/app.log | grep -E "ERROR|Exception|Caused by" | head -10
exit 1
ENDSSH
