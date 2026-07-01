#!/bin/bash
# 云服务器上一键编译所有服务
# 在 /opt/campus 目录下执行

set -e

ROOT_DIR="/opt/campus"

cd "$ROOT_DIR"

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
pip3 install requests urllib3 pycryptodome -i https://pypi.tuna.tsinghua.edu.cn/simple || pip3 install requests urllib3 pycryptodome

echo "===== 4/4 构建前端 ====="
cd campus-web
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
