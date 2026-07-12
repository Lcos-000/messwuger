# 校园助手项目部署手册

> 目标：零配置、离线化、一键启动完成全部服务部署。
>
> 核心思路：在一台有网络的构建机上把所有业务镜像和中间件镜像预先构建/拉取好，导出为 tar 包并整理部署文件；将离线包传输到服务器后，执行一条命令即可加载镜像并启动全部服务。

---

## 一、方案概述

本项目采用 **Docker 容器化 + 离线镜像包** 的方式部署，包含：

- **中间件**：MySQL 8.0、Redis 7.2、Nacos 2.4、Sentinel 1.8.9
- **Java 微服务**：Gateway、User-Service、Course-Service
- **Go + Python 爬虫服务**：Spider-Service
- **前端**：基于 Nginx 托管的 Vue 3 静态页面

所有服务通过单个 `docker-compose.yml` 编排，首次启动时自动完成：

- MySQL 建库建表（`init.sql`）
- Nacos `dev` 命名空间创建
- Nacos 配置导入（`nacos_config/`）
- 敏感占位符替换（`ALIYUN_OSS_*`、`JAVA_INTERNAL_TOKEN`、`AES_SECRET_KEY` 等）

---

## 二、部署前准备

### 2.1 服务器要求

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核 |
| 内存 | 8 GB | 16 GB |
| 磁盘 | 60 GB SSD | 100 GB SSD |
| 系统 | Linux x86_64（推荐 Ubuntu 22.04） | Linux x86_64 |
| 软件 | Docker 20.10+、Docker Compose 2.0+ | 最新稳定版 |

### 2.2 需要开放的端口

| 端口 | 用途 |
|------|------|
| 22 | SSH |
| 80 | 前端入口 |
| 8080 | 网关入口 |
| 3306 | MySQL |
| 6379 | Redis |
| 8848 | Nacos 控制台 |
| 9848/9849 | Nacos 2.x gRPC |
| 8858 | Sentinel 控制台 |
| 8082 | Go 爬虫服务 |

生产环境建议仅暴露 `80/443` 与 `8080`，其余端口限制内网访问。

### 2.3 必须获取的离线包

协作者从 GitHub 拉取代码后，仍需向项目维护者索取以下文件（**切勿提交到 GitHub**）：

- `deploy/.env.secret`：包含 `JAVA_INTERNAL_TOKEN`、`AES_SECRET_KEY`、`YM_TOKEN`、`YM_TYPE`、阿里云 OSS 密钥等敏感配置
- 构建好的离线包目录 `deploy/offline/package/`（或压缩后的等价文件）

> 说明：`deploy/.env` 默认配置（如 MySQL root 密码 `1234`）已随仓库提交，无需单独发送。

---

## 三、构建离线包（构建机操作）

在任意一台已安装 Docker 且能访问外网的机器上执行：

```bash
cd campus(1)(4)
bash deploy/offline/build-and-export.sh
```

> 说明：离线包中业务镜像统一使用 `latest` 标签，构建脚本和服务器端均无需指定版本号。

脚本会自动完成：

1. 构建 5 个业务镜像：
   - `campus-assistant/gateway`
   - `campus-assistant/user-service`
   - `campus-assistant/course-service`
   - `campus-assistant/spider-service`
   - `campus-assistant/web`
2. 拉取 5 个中间件镜像：
   - `mysql:8.0`
   - `redis:7.2`
   - `nacos/nacos-server:v2.4.0-slim`
   - `bladex/sentinel-dashboard:1.8.9`
   - `curlimages/curl:latest`
3. 导出镜像到 `deploy/offline/package/images/`
   - `business.tar`：业务镜像
   - `middleware.tar`：中间件镜像
4. 复制部署文件到 `deploy/offline/package/`

构建完成后，`deploy/offline/package/` 结构如下：

```text
deploy/offline/package/
├── .env
├── .env.secret
├── docker-compose.yml
├── load-and-start.sh
├── stop.sh
├── init.sql
├── config/
│   └── application-docker.yml
├── scripts/
│   └── import-nacos-config.sh
├── nacos_config/
│   ├── DEFAULT_GROUP/
│   ├── DATASOURCE_GROUP/
│   └── GATEWAY_GROUP/
└── images/
    ├── business.tar
    └── middleware.tar
```

---

## 四、传输到服务器

将 `deploy/offline/package/` 整体打包并传输到服务器，例如：

```bash
# 构建机端
cd deploy/offline
tar -czvf campus-assistant-offline.tar.gz package/

# 传输到服务器（示例使用 scp）
scp campus-assistant-offline.tar.gz root@<服务器IP>:/opt/

# 服务器端
ssh root@<服务器IP>
cd /opt
tar -xzvf campus-assistant-offline.tar.gz
```

---

## 五、服务器一键启动

进入离线包目录并执行启动脚本：

```bash
cd /opt/package
bash load-and-start.sh
```

脚本会：

1. 检查 `.env.secret` 和镜像包是否存在
2. 加载 `business.tar` 和 `middleware.tar`
3. 启动所有容器
4. 等待约 20 秒后展示服务状态

首次启动时，MySQL 会自动执行 `init.sql` 建表，Nacos 会自动导入配置并替换占位符。

---

## 六、验证部署

### 6.1 查看服务状态

```bash
cd /opt/package
docker compose ps
```

### 6.2 访问入口

| 地址 | 说明 |
|------|------|
| `http://<服务器IP>/` | 前端页面 |
| `http://<服务器IP>:8080/gateway/auth/login` | 网关登录接口 |
| `http://<服务器IP>:8848/nacos` | Nacos 控制台 |
| `http://<服务器IP>:8858` | Sentinel 控制台 |

### 6.3 常用检查命令

```bash
# 查看网关日志
docker logs -f campus-gateway

# 查看用户服务日志
docker logs -f campus-user

# 查看爬虫服务日志
docker logs -f campus-spider

# 进入 MySQL 检查表
docker exec -it campus-mysql mysql -uroot -p1234 campus_db -e "SHOW TABLES;"

# 检查 Redis
docker exec -it campus-redis redis-cli ping
```

---

## 七、本地验证（可选）

如果你想在本地先验证离线部署流程，可以直接在本地执行构建和启动脚本，跳过压缩传输步骤。

前提：

- Windows 用户需安装 Docker Desktop 并启用 WSL2 后端
- 保证 80、8080、3306、6379、8848、8858、8082 端口未被占用
- 内存建议 8G 以上

步骤：

```bash
bash deploy/offline/build-and-export.sh
cd deploy/offline/package
bash load-and-start.sh
```

本地访问：

- 前端：`http://localhost/`
- 网关：`http://localhost:8080/gateway/auth/login`

---

## 八、停止服务

在离线包目录执行：

```bash
bash stop.sh
```

该命令会停止并移除容器，但保留数据卷（MySQL 数据、Nacos 数据等）。

如需完全清理并重新初始化：

```bash
docker compose down -v
```

> 注意：`-v` 会删除数据卷，所有业务数据将丢失，请谨慎操作。

---

## 九、协作者零配置部署

协作者只需完成以下两步：

1. 从 GitHub 拉取代码（获取 `deploy/offline/package/` 中的编排文件和脚本）
2. 向维护者索取：
   - `deploy/.env.secret`
   - 构建好的 `deploy/offline/package/images/` 下的两个 tar 包（或完整离线包压缩文件）

将 `.env.secret` 放到 `package/` 目录，将镜像包放到 `package/images/`，然后执行：

```bash
cd deploy/offline/package
bash load-and-start.sh
```

无需修改任何配置即可启动。

---

## 十、配置说明

### 10.1 默认配置

- MySQL root 密码：`1234`（可在 `deploy/.env` 中修改）
- Redis：无密码（与中间件编排保持一致）
- Nacos：无认证，命名空间为 `dev`
- 内部调用 Token：`campus-internal-token`（生产必须修改）

### 10.2 敏感配置

所有敏感信息集中在 `deploy/.env.secret`，包括：

- `JAVA_INTERNAL_TOKEN`
- `AES_SECRET_KEY`
- `YM_TOKEN`
- `YM_TYPE`
- `ALIYUN_OSS_ENDPOINT`
- `ALIYUN_OSS_ACCESS_KEY_ID`
- `ALIYUN_OSS_ACCESS_KEY_SECRET`
- `ALIYUN_OSS_BUCKET_NAME`
- `ALIYUN_OSS_URL_PREFIX`

`import-nacos-config.sh` 会在首次启动时读取 `.env.secret`，将 `nacos_config/` 中的 `${VAR}` 占位符替换为真实值后导入 Nacos。

---

## 十一、常见问题

### 11.1 启动后 Java 服务不断重启

优先检查：

- Nacos 是否健康：`docker logs campus-nacos`
- `nacos-init` 是否成功：`docker logs campus-nacos-init`
- MySQL 是否可用：`docker exec -it campus-mysql mysql -uroot -p1234 -e "SELECT 1;"`
- 数据库连接 URL 环境变量是否注入：`docker inspect campus-user | grep SPRING_DATASOURCE_URL`

### 11.2 成绩/空教室回调失败

优先检查：

- 爬虫服务是否启动：`docker logs campus-spider`
- `JAVA_CALLBACK_URL` 等回调地址是否正确
- `JAVA_INTERNAL_TOKEN` 是否一致

### 11.3 前端页面空白或接口 404

优先检查：

- `campus-web` 容器是否启动
- Nginx 配置中 `/api/` 反向代理是否指向 `campus-gateway:8080/gateway/`

### 11.4 镜像包传输到服务器后 load 失败

可能是 Windows 传输导致 shell 脚本换行符变为 CRLF。`load-and-start.sh` 已自动处理 `import-nacos-config.sh` 的换行问题。如其他脚本也报错，可执行：

```bash
sed -i 's/\r$//' *.sh scripts/*.sh
```

---

## 十二、生产环境加固

部署到生产前，请至少完成以下修改：

1. 修改 `deploy/.env` 中的 `MYSQL_ROOT_PASSWORD` 为强密码
2. 同步修改 `deploy/init.sql` 中涉及到的默认密码引用（如有）
3. 修改 `deploy/.env.secret` 中的 `JAVA_INTERNAL_TOKEN` 和 `AES_SECRET_KEY`
4. 替换 `YM_TOKEN` / `YM_TYPE` 为当前有效的学校系统凭证
5. 替换阿里云 OSS 为真实可用的 Bucket 和 AccessKey
6. 关闭不必要的外部端口，仅保留 `80/443` 对外
7. 为 Nacos 开启认证，并调整 `NACOS_AUTH_ENABLE`
8. 为 Redis 增加密码，并同步修改配置

---

完成以上步骤后，项目即可通过离线镜像包实现零配置一键部署。
