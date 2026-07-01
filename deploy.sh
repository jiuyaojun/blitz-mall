#!/usr/bin/env bash
# Blitz 智能商城 — 部署脚本
# 用法: bash deploy.sh [--skip-tests] [--no-backup]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

APP_NAME="blitz-server"
JAR_FILE="blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar"
BACKUP_DIR="backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

SKIP_TESTS=false
NO_BACKUP=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests) SKIP_TESTS=true; shift ;;
        --no-backup) NO_BACKUP=true; shift ;;
        *) echo "未知参数: $1"; exit 1 ;;
    esac
done

echo "=== Blitz 部署开始 [${TIMESTAMP}] ==="

# ---- 1. Build ----
echo "[1/4] 构建项目..."
MVN_ARGS="package -pl blitz-server -am -q"
if [ "$SKIP_TESTS" = true ]; then
    MVN_ARGS="$MVN_ARGS -DskipTests"
fi
mvn $MVN_ARGS
echo "  构建完成"

# ---- 2. Backup ----
if [ "$NO_BACKUP" = false ]; then
    echo "[2/4] 备份旧 JAR..."
    mkdir -p "$BACKUP_DIR"
    if [ -f "$JAR_FILE" ]; then
        cp "$JAR_FILE" "${BACKUP_DIR}/${APP_NAME}-${TIMESTAMP}.jar"
        echo "  备份到 ${BACKUP_DIR}/${APP_NAME}-${TIMESTAMP}.jar"
    else
        echo "  无旧 JAR 可备份"
    fi
else
    echo "[2/4] 跳过备份 (--no-backup)"
fi

# ---- 3. Stop old process ----
echo "[3/4] 停止旧进程..."
OLD_PID=$(pgrep -f "${APP_NAME}.*\.jar" || true)
if [ -n "$OLD_PID" ]; then
    kill "$OLD_PID" 2>/dev/null || true
    sleep 3
    # Force kill if still running
    if kill -0 "$OLD_PID" 2>/dev/null; then
        kill -9 "$OLD_PID" 2>/dev/null || true
    fi
    echo "  已停止 PID: $OLD_PID"
else
    echo "  无运行中的进程"
fi

# ---- 4. Start new process ----
echo "[4/4] 启动新进程..."
# Load .env if exists
if [ -f .env ]; then
    set -a; source .env; set +a
fi
nohup java -jar "$JAR_FILE" > app.log 2>&1 &
NEW_PID=$!
echo "  新进程 PID: $NEW_PID"

# Wait for startup
echo -n "  等待启动"
for i in $(seq 1 30); do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null | grep -q 200; then
        echo " OK ($(($i * 2))s)"
        break
    fi
    echo -n "."
    sleep 2
done

echo "=== Blitz 部署完成 ==="
