# 校园助手项目部署手册

> 目标：以当前仓库结构为准，在一台 Ubuntu 22.04 服务器上完成中间件、Java 微服务、Go 爬虫服务、前端静态页面以及项目独立 SkyWalking 的部署。
>
> 说明：本文档以**后端服务可稳定运行**为主，前端只保留必要的构建与静态托管步骤。

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
```

应至少看到：

- `campus-assistant/`
- `campus-spider-service/`
- `campus-web/`
- `deploy/`
- `nacos_config/`
- `tools/`

---

## 四、部署中间件

```bash
cd /opt/campus/deploy
docker compose -f docker-compose.middleware.yml pull
docker compose -p campusassistant -f docker-compose.middleware.yml up -d
```

当前中间件编排文件：`/opt/campus/deploy/docker-compose.middleware.yml`

---

## 五、部署独立 SkyWalking

```bash
cd /opt/campus/deploy
docker compose -f docker-compose.skywalking.yml pull
docker compose -p campusassistant -f docker-compose.skywalking.yml up -d
```

当前对齐端口：

- UI：`18080`
- OAP gRPC：`11810`
- OAP HTTP：`12810`

Java Agent 公共目录：`/opt/campus/tools/skywalking-agent/`

推荐统一使用：

```text
-javaagent:/opt/campus/tools/skywalking-agent/skywalking-agent.jar
```

并分别设置：

```text
SW_AGENT_NAME=campusassistant-gateway-service
SW_AGENT_NAME=campusassistant-user-service
SW_AGENT_NAME=campusassistant-course-service
```

---

## 六、初始化数据库

当前仓库已提供初始化脚本：`/opt/campus/deploy/init.sql`

该脚本当前至少包含：

- `student_db`
- `personal_info`
- `course_db`
- `student_grade`
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

---

## 八、部署 Java 服务

### 8.1 编译打包

```bash
cd /opt/campus/campus-assistant
mvn clean package -DskipTests
```

### 8.2 安装 systemd 服务

当前仓库已提供模板：

- `/opt/campus/deploy/systemd/campus-gateway.service`
- `/opt/campus/deploy/systemd/campus-user.service`
- `/opt/campus/deploy/systemd/campus-course.service`
- `/opt/campus/deploy/systemd/campus-spider.service`

复制并启用：

```bash
cp /opt/campus/deploy/systemd/campus-gateway.service /etc/systemd/system/
cp /opt/campus/deploy/systemd/campus-user.service /etc/systemd/system/
cp /opt/campus/deploy/systemd/campus-course.service /etc/systemd/system/
cp /opt/campus/deploy/systemd/campus-spider.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable campus-gateway campus-user campus-course campus-spider
```

---

## 九、部署 Go + Python 服务

### 9.1 编译

```bash
cd /opt/campus/campus-spider-service
go mod tidy
go build -o server ./cmd/server
mkdir -p /opt/campus/campus-spider-service/data/sessions
```

### 9.2 关键环境变量

当前 Go 服务至少需要以下回调地址：

```text
JAVA_CALLBACK_URL=http://127.0.0.1:8000/internal/api/v1/sync/student-data
PUNCH_CALLBACK_URL=http://127.0.0.1:8000/internal/api/v1/sync/punch-result
EMPTY_CLASSROOM_CALLBACK_URL=http://127.0.0.1:8000/internal/api/v1/sync/empty-classroom
GRADES_CALLBACK_URL=http://127.0.0.1:8000/internal/api/v1/sync/grades
JAVA_INTERNAL_TOKEN=campus-internal-token
PYTHON_PATH=/usr/bin/python3
```

> 当前 `deploy/systemd/campus-spider.service` 与 `deploy/start-all.sh` 已对齐这四条回调链路。

---

## 十、部署前端

### 10.1 构建前端

```bash
cd /opt/campus/campus-web
npm install
npm run build
```

### 10.2 Nginx 配置

当前仓库已提供：`/opt/campus/deploy/nginx.conf`

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
systemctl status campus-gateway --no-pager
systemctl status campus-user --no-pager
systemctl status campus-course --no-pager
systemctl status campus-spider --no-pager
systemctl status nginx --no-pager
```

重点检查：

- 成绩回调是否命中 `GRADES_CALLBACK_URL`
- 空教室回调是否命中 `EMPTY_CLASSROOM_CALLBACK_URL`
- Java 端如查询不到成绩，检查 `student_grade` 是否已落库
- Java 端如查不到空教室，检查回调参数是否与查询指纹一致

---

## 十二、常见问题

### 12.1 成绩查询提交成功但前端一直空

优先检查：

- Go 是否已重新编译并重启
- `GRADES_CALLBACK_URL` 是否配置正确
- `student_grade` 是否已落库
- 前后端查询学年 / 学期是否一致

### 12.2 空教室提交成功但一直查不到结果

优先检查：

- Go 回调 payload 是否完整带回 `campusId`、`building`、`roomType`
- `EMPTY_CLASSROOM_CALLBACK_URL` 是否配置正确
- Java 查询指纹与回调写入指纹是否一致

### 12.3 OSS 图片上传成功但页面空白

优先检查：

- `user_profile_custom_asset` 是否已落库
- 返回 URL 是否可直接访问
- Bucket 是否允许当前访问方式读取
- OSS CORS 是否允许浏览器加载图片

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

---

按当前文档完成后，项目应能以“后端服务为主、前端静态托管为辅、SkyWalking 独立隔离”的方式部署。
