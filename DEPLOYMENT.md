# 校园助手项目部署手册

本文档描述当前项目的部署方式，并补充 Android 客户端分发与后端公共配置对齐要求。

---

## 一、部署范围

当前仓库包含以下交付物：

- Java 微服务后端：`campus-assistant`
- Go + Python 爬虫服务：`campus-spider-service`
- Web 前端：`campus-web`
- Android 原生客户端源码：`campus-android`

其中：

- 服务端与 Web 前端适合 Docker / 离线包部署
- Android 客户端当前以 APK 本地打包和分发为主，不在服务器容器内运行

---

## 二、服务端部署目标

部署完成后，建议至少提供以下稳定入口：

- Web 前端：`http://<host>/`
- 网关 API：`http://<host>/api/`
- 静态资源前缀：`http://<host>/`

这与当前安卓客户端的动态服务器设置保持一致。安卓端默认按如下规则拼接：

- API：`scheme://host:port/api/`
- 静态资源：`scheme://host:port/`

如果你未来切换为域名反向代理，也建议继续保留这两个公开入口约定，避免客户端额外适配。

---

## 三、离线部署方案概述

项目采用 Docker 容器化 + 离线镜像包方式部署，主要包含：

- MySQL 8.0
- Redis 7.2
- Nacos 2.4
- Sentinel 1.8.9
- Gateway
- User-Service
- Course-Service
- Spider-Service
- Web（Nginx 托管）

离线包目录位于：

```text
deploy/offline/package/
```

---

## 四、关键外部端口建议

| 端口 | 用途 |
|------|------|
| 80 / 443 | Web 与统一公网入口 |
| 8848 | Nacos 控制台 |
| 8858 | Sentinel 控制台 |
| 3306 | MySQL（建议仅内网） |
| 6379 | Redis（建议仅内网） |
| 8082 | Spider-Service（建议仅内网） |

如果希望 Android 客户端长期稳定访问，最实际的方式仍然是：

1. 给网关提供稳定域名
2. 由公网入口统一反代到当前网关实际监听端口
3. Android 端保留服务器设置作为兜底，而不是主路径

---

## 五、Nacos 配置要求

除数据源、日志、OSS 外，当前部署还需要公共客户端配置文件：

### `publicclient-config.yaml`

```yaml
campus:
  notice:
    enabled: true
    version: 1
    title: 系统公告
    content: |
      当前服务如有迁移，请先修改客户端服务器设置。
    level: info
    updatedAt: 2026-08-07 18:00:00

  manual:
    version: 1
    title: 使用手册
    content: |
      1. 登录前请确认服务器地址。
      2. 若成绩、课表、空教室为空，请先提交同步任务。

  schedule:
    springStartDate: 2026-03-02
    autumnStartDate: 2026-09-01
    maxWeek: 20
```

网关对外建议提供：

- `GET /api/public/notice`
- `GET /api/public/manual`
- `GET /api/public/schedule-config`

这样 Web 与 Android 都能复用同一份来源。

---

## 六、离线包构建与启动

### 构建机

```bash
bash deploy/offline/build-and-export.sh
```

### 服务器端

```bash
cd /opt/package
bash load-and-start.sh
```

首次启动后建议验证：

```bash
docker compose ps
docker logs -f campus-gateway
docker logs -f campus-user
docker logs -f campus-spider
```

---

## 七、Android 分发说明

Android 不参与 Docker 部署，当前分发方式如下：

1. 在 Android Studio 打开 `campus-android/`
2. 使用 `Build > Generate App Bundles or APKs > Generate APKs`
3. 产出 APK 后发给测试人员或安装到真机

首次安装后，建议测试人员在登录页先完成服务器设置：

- 本机开发后端：模拟器可填写 `10.0.2.2` 对应映射逻辑后的服务地址
- 局域网调试：填写电脑局域网 IP + 网关端口
- 生产环境：填写稳定域名或公网 IP + 端口

如果后端静态资源与 API 不在同一路径层级，优先在网关或 Nginx 层统一，而不是继续增加客户端特殊分支。

---

## 八、常见部署问题

### 1. Nacos 配置已存在但服务读取不到

优先检查：

- `application-nacos.yml` 是否已 import 对应配置文件
- 配置 group 是否正确
- YAML 缩进是否有效
- `@ConfigurationProperties(prefix = ...)` 前缀是否与配置层级一致
- 是否需要 `@RefreshScope`

### 2. Android 能打开登录页但始终网络异常

优先检查：

- 网关是否已暴露 `/api/` 前缀
- 手机是否能访问当前 IP/域名与端口
- 是否把 `/gateway/` 与 `/api/` 搞混
- 服务端是否真的收到请求日志

### 3. 默认图片能返回但 Android 不显示

优先检查：

- 返回的是相对路径还是完整 URL
- 当前服务器设置对应的静态资源基础前缀是否正确
- OSS bucket 是否允许公开读取或有临时访问签名

---

## 九、仓库本地文件约束

部署仓库不应包含以下本地文件：

- `node_modules/`
- `local.properties`
- `.gradle/`
- Android `build/` 产物
- IDE 私有目录

如果历史上已经提交过这些目录，除了补 `.gitignore` 外，还需要执行一次取消追踪。

---

## 十、关联文档

- 总览说明：[README.md](README.md)
- 测试说明：[TESTING.md](TESTING.md)
- Android 说明：[campus-android/README.md](campus-android/README.md)
