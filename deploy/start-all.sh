#!/bin/bash
# 一键启动校园助手后端 + 爬虫服务（宿主机直接运行版）
# 前置条件：中间件 MySQL/Redis/Nacos 已用 docker-compose.middleware.yml 启动

set -e

ROOT_DIR="/opt/campus"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

cd "$ROOT_DIR"

nohup java -jar "$ROOT_DIR/campus-assistant/campusswu-gateway/target/campusswu-gateway-1.0-SNAPSHOT.jar" \
  --server.port=8080 \
  >> "$LOG_DIR/gateway.log" 2>&1 &
echo "Gateway started (PID $!)"

nohup java -jar "$ROOT_DIR/campus-assistant/user-service/target/user-service-1.0-SNAPSHOT.jar" \
  >> "$LOG_DIR/user-service.log" 2>&1 &
echo "User-Service started (PID $!)"

nohup java -jar "$ROOT_DIR/campus-assistant/course-service/target/course-service-1.0-SNAPSHOT.jar" \
  >> "$LOG_DIR/course-service.log" 2>&1 &
echo "Course-Service started (PID $!)"

cd "$ROOT_DIR/campus-spider-service"
export REDIS_ADDR="127.0.0.1:6379"
export REDIS_PASSWORD="CampusRedis@1234"
export REDIS_DB="0"
export HTTP_ADDR=":8082"
export JAVA_CALLBACK_URL="http://127.0.0.1:8000/internal/api/v1/sync/student-data"
export PUNCH_CALLBACK_URL="http://127.0.0.1:8000/internal/api/v1/sync/punch-result"
export JAVA_INTERNAL_TOKEN="campus-internal-token"
export PYTHON_PATH="/usr/bin/python3"
export SPIDER_SCRIPT="./scripts/spider_cli.py"
export CHECKIN_SCRIPT="./scripts/checkin_cli.py"
export SESSION_DIR="./data/sessions"
export WORKER_CONCURRENCY="4"

nohup "$ROOT_DIR/campus-spider-service/server" \
  >> "$LOG_DIR/spider-service.log" 2>&1 &
echo "Spider-Service started (PID $!)"

echo ""
echo "所有服务已启动，日志目录: $LOG_DIR"
echo "查看状态: ps -ef | grep -E 'campusswu|campus-spider'"
