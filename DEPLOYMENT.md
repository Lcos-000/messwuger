# 校园助手项目部署手册

> 目标：以当前仓库结构为准，在一台 Ubuntu 22.04 服务器上完成中间件、Java 微服务、Go 爬虫服务、前端静态页面以及项目独立 SkyWalking 的部署。
>
> 说明：本文档以**后端服务可稳定运行**为主，前端只保留必要的构建与静态托管步骤。

---

## 目录

1. 准备工作
2. 安装基础环境
3. 上传项目代码
4. 部署中间件
5. 部署独立 SkyWalking
6. 初始化数据库
7. 配置 OSS
8. 部署 Java 服务
9. 部署 Go + Python 服务
10. 部署前端
11. 启动与验证
12. 常见问题
13. 生产环境加固

---

## 一、准备工作

### 1.1 服务器配置建议

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 2 核 |
| 内存 | 4 GB | 4 GB |
| 系统盘 | 40 GB SSD | 40 GB SSD |
| 系统 | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |

### 1.2 需要开放的端口

| 端口 | 用途 |
|------|------|
| 22 | SSH |
| 80 | 网关 / 前端入口 |
| 443 | HTTPS（可选） |
| 3306 | MySQL |
| 6379 | Redis |
| 8848 | Nacos 控制台 |
| 9848/9849 | Nacos 2.x gRPC |
| 8858 | Sentinel 控制台 |
| 8082 | Go 爬虫服务 |
| 8000 | User-Service（仅内网或联调时开放） |
| 9000 | Course-Service（仅内网或联调时开放） |
| 18080 | SkyWalking UI（仅运维或管理员访问） |
| 11810 | SkyWalking OAP gRPC |
| 12810 | SkyWalking OAP HTTP |

> 生产环境建议仅对外开放 `80`、`443`，其余端口收敛到内网或堡垒机访问。

---

## 二、安装基础环境

```bash
apt update && apt upgrade -y
apt install -y git curl wget vim unzip zip htop net-tools nginx openjdk-17-jdk maven python3 python3-pip nodejs npm docker.io docker-compose-plugin
systemctl enable docker --now
```

### 2.1 安装 Go 1.24

```bash
cd /tmp
wget https://go.dev/dl/go1.24.0.linux-amd64.tar.gz
rm -rf /usr/local/go
tar -C /usr/local -xzf go1.24.0.linux-amd64.tar.gz
echo 'export PATH=$PATH:/usr/local/go/bin' >> /etc/profile
source /etc/profile
go version
```

### 2.2 安装 Python 依赖

```bash
pip3 install requests urllib3 pycryptodome -i https://pypi.tuna.tsinghua.edu.cn/simple
```

---

## 三、上传项目代码

```bash
mkdir -p /opt/campus
cd /opt/campus
git clone <你的仓库地址> .
ls -la /opt/campus
```

应至少看到：

```text
campus-assistant/
campus-spider-service/
campus-web/
deploy/
nacos_config/
```

---

## 四、部署中间件

```bash
cd /opt/campus/deploy
mkdir -p mysql/conf redis/data nacos/logs nacos/data
```

### 4.1 当前对齐的镜像标签

- `mysql:9.7.0`
- `redis:8.8.0`
- `bladex/sentinel-dashboard:1.8.9`
- `nacos/nacos-server:v2.4.0-slim`

### 4.2 创建环境变量

```bash
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=1234
REDIS_PASSWORD=CampusRedis@1234
EOF
```

### 4.3 启动中间件

```bash
docker compose -f docker-compose.middleware.yml pull
docker compose -p campusassistant -f docker-compose.middleware.yml up -d
docker compose -p campusassistant -f docker-compose.middleware.yml ps
```

---

## 五、部署独立 SkyWalking

当前仓库已新增：`deploy/docker-compose.skywalking.yml`。

### 5.1 当前对齐的镜像标签

- `apache/skywalking-ui:10.0.1`
- `apache/skywalking-oap-server:10.0.1`
- `apache/skywalking-banyandb:0.6.0`

### 5.2 这套配置会启动

- `campus-skywalking-banyandb`
- `campus-skywalking-oap`
- `campus-skywalking-ui`

并使用以下端口：

- UI：`18080`
- OAP gRPC：`11810`
- OAP HTTP：`12810`
- BanyanDB：`17913`

### 5.3 拉取并启动

```bash
cd /opt/campus/deploy
docker compose -f docker-compose.skywalking.yml pull
docker compose -p campusassistant -f docker-compose.skywalking.yml up -d
docker compose -p campusassistant -f docker-compose.skywalking.yml ps
```

### 5.4 重置这套 SkyWalking 数据

```bash
cd /opt/campus/deploy
docker compose -p campusassistant -f docker-compose.skywalking.yml down -v
docker compose -p campusassistant -f docker-compose.skywalking.yml up -d
```

### 5.5 Java Agent 对齐

如果你的 Java 服务启用了 SkyWalking Agent，推荐把公共配置收敛到 `tools/skywalking-agent/config/agent.config`，只在服务启动入口保留 `-javaagent`。

```text
-javaagent:/opt/campus/tools/skywalking-agent/skywalking-agent.jar
```

当前仓库里的 `agent.config` 默认已包含：

```text
collector.backend_service=127.0.0.1:11810
agent.namespace=campusassistant
agent.service_name=${SW_AGENT_NAME:unknown-service}
```

因此每个服务只需单独提供自己的 `SW_AGENT_NAME`：

```text
SW_AGENT_NAME=campusassistant-gateway-service
SW_AGENT_NAME=campusassistant-user-service
SW_AGENT_NAME=campusassistant-course-service
```

否则即便新 UI 已经部署成功，你的服务仍会继续把链路上报到旧的 SkyWalking。

---

## 六、初始化数据库

中间件首次启动会自动执行 `deploy/init.sql`。当前 `init.sql` 已包含：

- `student_db`
- `personal_info`
- `course_db`
- `user_profile_style`
- `user_profile_custom_asset`

检查方式：

```bash
docker exec -it campus-mysql mysql -uroot -p1234 campus_db -e "SHOW TABLES;"
```

---

## 七、配置 OSS

当前 `user-service` 已集成阿里云 OSS，自定义头像 / 顶部背景 / 墙纸上传依赖以下配置：

```yaml
aliyun:
  oss:
    endpoint: https://oss-cn-beijing.aliyuncs.com
    access-key-id: <your-access-key-id>
    access-key-secret: <your-access-key-secret>
    bucket-name: <your-bucket-name>
    url-prefix: https://<your-bucket-name>.oss-cn-beijing.aliyuncs.com
```

推荐方式：

- 通过 Nacos 下发 `aliyun-oss.yaml`
- 或通过外部配置注入
- 不要把正式环境 AK / SK 提交到仓库

> 当前上传新图片后仅更新数据库中的最新 URL，不会自动删除旧 OSS 对象。生产环境建议后续补删除逻辑或配置生命周期规则。

---

## 八、部署 Java 服务

### 8.1 编译打包

```bash
cd /opt/campus/campus-assistant
mvn clean package -DskipTests
```

### 8.2 使用仓库内 systemd 模板

当前仓库已提供并修正以下模板：

- `deploy/systemd/campus-gateway.service`
- `deploy/systemd/campus-user.service`
- `deploy/systemd/campus-course.service`
- `deploy/systemd/campus-spider.service`

复制到系统目录：
### 8.3 如需接入 SkyWalking Agent

最稳妥的方式是在 `ExecStart` 或启动脚本里显式加入：

```text
-javaagent:/opt/campus/tools/skywalking-agent/skywalking-agent.jar
```

然后为每个服务分别设置环境变量：

```text
SW_AGENT_NAME=campusassistant-user-service
```

这部分当前仓库没有强行写死到模板中，因为每台机器的 Agent 安装路径未必一致。

---
-Dskywalking.collector.backend_service=127.0.0.1:11810
```

这部分当前仓库没有强行写死到模板中，因为每台机器的 Agent 安装路径未必一致。

---

## 九、部署 Go + Python 服务

### 9.1 编译

```bash
cd /opt/campus/campus-spider-service
go mod tidy
go build -o server ./cmd/server
```

### 9.2 启动

```bash
mkdir -p /opt/campus/campus-spider-service/data/sessions
systemctl enable --now campus-spider
systemctl status campus-spider --no-pager
```

---

## 十、部署前端

### 10.1 构建前端

```bash
cd /opt/campus/campus-web
npm install
npm run build
```

### 10.2 使用仓库内 Nginx 配置

当前仓库已修正：`deploy/nginx.conf`

```bash
cp /opt/campus/deploy/nginx.conf /etc/nginx/sites-available/campus
ln -sf /etc/nginx/sites-available/campus /etc/nginx/sites-enabled/campus
nginx -t
systemctl restart nginx
```

该配置约定：

- 静态页面目录：`/opt/campus/campus-web/dist`
- 前端 API 前缀：`/api/`
- 反向代理目标：`http://127.0.0.1:8080/gateway/`

---

## 十一、启动与验证

### 11.1 启动顺序

```bash
cd /opt/campus/deploy
docker compose -p campusassistant -f docker-compose.middleware.yml up -d
docker compose -p campusassistant -f docker-compose.skywalking.yml up -d
systemctl start campus-gateway campus-user campus-course
systemctl start campus-spider
systemctl start nginx
```

### 11.2 验证

```bash
curl http://127.0.0.1:8082/health
curl http://127.0.0.1:8848/nacos
curl http://127.0.0.1:8858
curl http://127.0.0.1:18080
systemctl status campus-gateway --no-pager
systemctl status campus-user --no-pager
systemctl status campus-course --no-pager
systemctl status campus-spider --no-pager
systemctl status nginx --no-pager
```

浏览器访问：

优先检查：

- Java 进程是否真的挂载了 SkyWalking Agent
- `tools/skywalking-agent/config/agent.config` 中的 `collector.backend_service` 是否为 `127.0.0.1:11810`
- 当前服务是否设置了正确的 `SW_AGENT_NAME`
- 是否仍残留旧的 `-Dskywalking.collector.backend_service` 或旧的 `-Dskywalking.agent.service_name`

---

## 十二、常见问题

### 12.1 新 SkyWalking UI 打开后没有当前项目数据

优先检查：

- Java 进程是否真的挂载了 SkyWalking Agent
- `collector.backend_service` 是否已改为 `127.0.0.1:11810`
- 服务名是否已切换为当前项目前缀

### 12.2 新 SkyWalking UI 里仍然看到旧项目

通常是因为：

- Java Agent 还在向旧 OAP 上报
- 你打开的仍是旧 UI 地址
- 旧容器并未停掉，资源配置仍指向旧地址

### 12.3 Nacos 配置显示 empty，服务起不来

优先检查：

- `datasource-mysql.yaml` 是否已发布到正确 namespace
- `datasource-redis.yaml` 是否已发布到正确 namespace
- group 是否为 `DATASOURCE_GROUP`
- 应用连接的 namespace 是否与 Nacos 页面中的配置一致

### 12.4 OSS 图片上传成功但页面空白

优先检查：

- `user_profile_custom_asset` 是否已落库
- 返回的 URL 是否能直接访问
- Bucket 是否允许当前访问方式读取
- OSS CORS 是否允许浏览器加载图片

### 12.5 Java 调用 Go 失败

```bash
curl http://127.0.0.1:8082/health
journalctl -u campus-spider -f
```

---

## 十三、生产环境加固

### 13.1 数据与密钥

- 修改 MySQL 密码
- 为 Redis 增加密码
- 修改内部调用 Token
- 使用 Nacos 或其他外部配置管理敏感信息

### 13.2 OSS

- Bucket 不要长期使用公共读
- 优先使用私有读写 + 签名 URL / CDN 鉴权 / 后端中转访问
- 为 `profile-custom/` 配置生命周期规则
- 若要控制存量，补充“替换成功后删除旧对象”逻辑

### 13.3 网络暴露

- `3306`、`6379`、`8848`、`8858`、`8082`、`8000`、`9000`、`11810`、`12810` 建议仅保留内网访问
- `18080` 仅供管理员或运维访问
- 对外仅暴露 `80/443`

### 13.4 备份

```bash
mkdir -p /opt/backup
docker exec campus-mysql mysqldump -uroot -p1234 campus_db > /opt/backup/campus_db_$(date +%Y%m%d).sql
```

---

按当前文档完成后，项目应能以“后端服务为主、前端静态托管为辅、SkyWalking 独立隔离”的方式部署。
