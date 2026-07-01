#!/bin/bash
cd /home/ubuntu
pkill -9 -f blitz-server 2>/dev/null
sleep 2
nohup java -jar blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > app.log 2>&1 &
echo "PID=$!"
sleep 3
ps aux | grep blitz-server | grep -v grep
