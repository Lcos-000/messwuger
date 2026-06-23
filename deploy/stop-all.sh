#!/bin/bash
# 一键停止校园助手后端 + 爬虫服务

pkill -f "campusswu-gateway-1.0-SNAPSHOT.jar" || true
pkill -f "user-service-1.0-SNAPSHOT.jar" || true
pkill -f "course-service-1.0-SNAPSHOT.jar" || true
pkill -f "campus-spider-service/server" || true

echo "所有服务已停止"
