#!/bin/bash
# 云服务器上一键编译所有服务
# 在 /opt/campus-assistant 目录下执行

set -e

echo "===== 1/4 编译 Java 微服务 ====="
cd campus-assistant
mvn clean install -DskipTests
cd ..

echo "===== 2/4 编译 Go 爬虫服务 ====="
cd campus-spider-service
go mod tidy
GOOS=linux GOARCH=amd64 go build -o server ./cmd/server
cd ..

echo "===== 3/4 安装 Python 依赖 ====="
pip3 install requests urllib3 -i https://pypi.tuna.tsinghua.edu.cn/simple || pip install requests urllib3

echo "===== 4/4 构建前端 ====="
cd campus-web
# 生产环境需要把 baseURL 改成相对路径 /gateway，否则前端会请求 localhost
if grep -q "http://localhost:80/gateway" src/utils/request.js; then
  echo "[WARN] 请手动把 src/utils/request.js 里的 baseURL 改为 '/gateway' 再重新执行 build.sh"
  echo "       示例：baseURL: '/gateway'"
  exit 1
fi
npm install
npm run build
cd ..

echo "===== 编译完成 ====="
echo "Java JAR 列表:"
ls -lh campus-assistant/*/target/*.jar
echo "Go 二进制:"
ls -lh campus-spider-service/server
echo "前端产物:"
ls -lh campus-web/dist
