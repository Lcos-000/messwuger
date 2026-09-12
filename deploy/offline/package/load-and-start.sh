#!/bin/bash
# 服务器上一键加载镜像并启动全部服务
# 用法：bash load-and-start.sh

set -e

cd "$(dirname "$0")"

# 兼容 Windows 传输导致的 CRLF
sed -i 's/\r$//' scripts/import-nacos-config.sh 2>/dev/null || true

echo "===== Campus Assistant 离线启动脚本 ====="

# 前置检查
if [ ! -f ".env.secret" ]; then
  echo "错误：缺少 .env.secret 文件，请从项目维护者处获取后放到当前目录。"
  exit 1
fi

# 让 Compose 插值使用同一份部署密钥，例如 SPIDER_API_TOKEN。
set -a
. ./.env.secret
set +a

if [ ! -f "images/business.tar" ]; then
  echo "错误：缺少 images/business.tar 业务镜像包。"
  exit 1
fi

if [ ! -f "images/middleware.tar" ]; then
  echo "错误：缺少 images/middleware.tar 中间件镜像包。"
  exit 1
fi

# 加载镜像
load_image() {
  local file="$1"
  local name
  name=$(basename "$file")
  echo ""
  echo "=== 加载镜像包: $name ==="
  docker load -i "$file"
}

load_image images/business.tar
load_image images/middleware.tar

# 启动所有服务
echo ""
echo "=== 启动所有服务 ==="
docker compose up -d

# 等待并展示状态
echo ""
echo "=== 等待服务初始化（约 20 秒）==="
sleep 20

echo ""
echo "=== 服务状态 ==="
docker compose ps

echo ""
echo "===== 部署完成 ====="
echo "前端访问: http://<服务器IP>/"
echo "网关登录: http://<服务器IP>:8080/gateway/auth/login"
echo "Nacos 控制台: http://<服务器IP>:8848/nacos"
echo "Sentinel 控制台: http://<服务器IP>:8858"
echo ""
echo "停止服务请执行: bash stop.sh"
