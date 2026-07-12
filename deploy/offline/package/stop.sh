#!/bin/bash
# 停止全部服务（保留数据卷）
# 用法：bash stop.sh

set -e

cd "$(dirname "$0")"
docker compose down

echo "服务已停止，数据卷保留。"
