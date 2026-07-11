#!/bin/bash
# 校园助手离线部署包构建脚本
# 用法：bash deploy/offline/build-and-export.sh
# 说明：构建业务镜像、拉取中间件镜像、导出 tar 包，并将部署所需文件整理到 deploy/offline/package/

set -e

VERSION=latest
ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
OFFLINE_DIR="$ROOT_DIR/deploy/offline"
PACKAGE_DIR="$OFFLINE_DIR/package"
IMAGE_DIR="$PACKAGE_DIR/images"

echo "===== Campus Assistant 离线部署包构建 ====="
echo "版本号: $VERSION"
echo "项目根目录: $ROOT_DIR"
echo ""

# 检查 Docker
if ! docker info >/dev/null 2>&1; then
  echo "错误：无法连接 Docker，请确保 Docker 已启动。"
  exit 1
fi

# 业务镜像列表（离线包统一使用 latest 标签，避免服务器端再设置 VERSION 环境变量）
BUSINESS_IMAGES=(
  "campus-assistant/gateway:latest"
  "campus-assistant/user-service:latest"
  "campus-assistant/course-service:latest"
  "campus-assistant/spider-service:latest"
  "campus-assistant/web:latest"
)

# 中间件镜像列表
MIDDLEWARE_IMAGES=(
  "mysql:8.0"
  "redis:7.2"
  "nacos/nacos-server:v2.4.0-slim"
  "bladex/sentinel-dashboard:1.8.9"
  "curlimages/curl:latest"
)

# 构建业务镜像
echo "=== 构建业务镜像 ==="

build_and_tag() {
  local name=$1
  local dockerfile=$2
  local context=$3
  echo "-> $name:$VERSION"
  docker build -t "$name:$VERSION" -f "$dockerfile" "$context"
  docker tag "$name:$VERSION" "$name:latest"
}

build_and_tag "campus-assistant/gateway" \
  "$ROOT_DIR/campus-assistant/campusswu-gateway/Dockerfile" \
  "$ROOT_DIR/campus-assistant"

build_and_tag "campus-assistant/user-service" \
  "$ROOT_DIR/campus-assistant/user-service/Dockerfile" \
  "$ROOT_DIR/campus-assistant"

build_and_tag "campus-assistant/course-service" \
  "$ROOT_DIR/campus-assistant/course-service/Dockerfile" \
  "$ROOT_DIR/campus-assistant"

build_and_tag "campus-assistant/spider-service" \
  "$ROOT_DIR/campus-spider-service/Dockerfile" \
  "$ROOT_DIR/campus-spider-service"

build_and_tag "campus-assistant/web" \
  "$ROOT_DIR/campus-web/Dockerfile" \
  "$ROOT_DIR/campus-web"

# 拉取中间件镜像
echo ""
echo "=== 拉取中间件镜像 ==="
for img in "${MIDDLEWARE_IMAGES[@]}"; do
  echo "-> $img"
  docker pull "$img"
done

# 准备离线包目录
echo ""
echo "=== 准备离线包目录 ==="
mkdir -p "$IMAGE_DIR" "$PACKAGE_DIR/config"
rm -rf "$PACKAGE_DIR"/nacos_config "$PACKAGE_DIR"/scripts "$PACKAGE_DIR"/config/*

# 导出业务镜像
echo ""
echo "=== 导出业务镜像 ==="
BUSINESS_TAR="$IMAGE_DIR/business.tar"
docker save -o "$BUSINESS_TAR" "${BUSINESS_IMAGES[@]}"
echo "业务镜像已保存: $BUSINESS_TAR"

# 导出中间件镜像
echo ""
echo "=== 导出中间件镜像 ==="
MIDDLEWARE_TAR="$IMAGE_DIR/middleware.tar"
docker save -o "$MIDDLEWARE_TAR" "${MIDDLEWARE_IMAGES[@]}"
echo "中间件镜像已保存: $MIDDLEWARE_TAR"

# 复制部署文件
echo ""
echo "=== 复制部署文件 ==="
cp "$ROOT_DIR/deploy/.env" "$PACKAGE_DIR/.env"
cp "$ROOT_DIR/deploy/.env.secret" "$PACKAGE_DIR/.env.secret"
cp "$ROOT_DIR/deploy/init.sql" "$PACKAGE_DIR/init.sql"
cp "$ROOT_DIR/deploy/config/application-docker.yml" "$PACKAGE_DIR/config/application-docker.yml"
cp -r "$ROOT_DIR/deploy/scripts" "$PACKAGE_DIR/scripts"
cp -r "$ROOT_DIR/nacos_config" "$PACKAGE_DIR/nacos_config"
cp "$OFFLINE_DIR/package/load-and-start.sh" "$PACKAGE_DIR/load-and-start.sh"
cp "$OFFLINE_DIR/package/stop.sh" "$PACKAGE_DIR/stop.sh"

echo ""
echo "===== 离线部署包构建完成 ====="
echo "输出目录: $PACKAGE_DIR"
echo ""
echo "文件清单:"
ls -lh "$PACKAGE_DIR"
echo ""
echo "镜像清单:"
ls -lh "$IMAGE_DIR"
echo ""
echo "下一步："
echo "1. 将 $PACKAGE_DIR 整体压缩或传输到服务器任意目录"
echo "2. 在服务器上执行 bash load-and-start.sh 即可一键启动"
