#!/usr/bin/env bash
set -e
SERVER="ubuntu@119.28.151.191"
SSH_KEY="$HOME/.ssh/blitz"
D="C:/watasinokoto/blitz"

echo "=== Blitz 最终修复 ==="

# 上传
echo "上传文件..."
scp -i "$SSH_KEY" "$D/blitz-server/src/main/resources/application-minimal.yml" "$SERVER:/tmp/"
scp -i "$SSH_KEY" "$D/blitz-server/pom.xml" "$SERVER:/tmp/blitz-server-pom.xml"

# 服务器操作
ssh -i "$SSH_KEY" "$SERVER" << 'ENDSSH'
set -e
D=/home/ubuntu/blitz

cp /tmp/application-minimal.yml $D/blitz-server/src/main/resources/application-minimal.yml
cp /tmp/blitz-server-pom.xml $D/blitz-server/pom.xml

cd $D
echo "构建..."
mvn package -DskipTests -q 2>&1 && echo "BUILD OK" || { echo "BUILD FAILED"; exit 1; }

pkill -f 'blitz-server.*jar' 2>/dev/null || true
sleep 2

echo "启动..."
set -a; source $D/.env; set +a
SPRING_PROFILES_ACTIVE=minimal nohup java -jar $D/blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > /home/ubuntu/logs/app.log 2>&1 &
echo "PID $!"

echo -n "等待启动"
for i in $(seq 1 20); do
    CODE=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ 2>/dev/null || echo "000")
    if [ "$CODE" = "200" ]; then
        echo " READY!"
        echo "--- 首页 ---"
        curl -s http://localhost:8080/ | head -c 300
        exit 0
    fi
    echo -n "."
    sleep 3
done

echo " FAILED"
head -100 /home/ubuntu/logs/app.log | grep -E "ERROR|Exception|Caused by" | head -6
exit 1
ENDSSH
