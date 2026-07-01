#!/bin/bash
export OPENAI_API_KEY=sk-demo
cd /home/ubuntu
pkill -9 -f blitz-server 2>/dev/null
sleep 2
java -jar blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > app.log 2>&1 &
echo "Started PID=$!"
