## 暂时没有部署成功

# 校园助手项目 - 傻瓜式服务器部署手册

> **目标**：把当前项目原封不动搬到服务器上跑起来，尽量不改代码配置。
>
> **部署方式**：所有服务运行在同一台服务器上（推荐新手）。
>
> **适用系统**：Ubuntu 22.04 LTS（推荐）
>
> **假设**：你有一台可以通过 SSH 登录的 Linux 服务器，拥有 root 权限。

---

## 目录

1. [准备工作](#一准备工作)
2. [连接服务器](#二连接服务器)
3. [安装基础环境](#三安装基础环境)
4. [上传项目代码](#四上传项目代码)
5. [部署中间件](#五部署中间件)
6. [初始化数据库](#六初始化数据库)
7. [部署 Java 服务](#七部署-java-服务)
8. [部署 Go + Python 服务](#八部署-go--python-服务)
9. [部署前端](#九部署前端)
10. [启动与验证](#十启动与验证)
11. [自动打卡验证](#十一自动打卡验证)
12. [常见问题](#十二常见问题)
13. [生产环境加固（可选）](#十三生产环境加固可选)

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
| 22 | SSH 远程连接 |
| 80 | HTTP 网页访问、Java 网关 |
| 443 | HTTPS（可选） |
| 3306 | MySQL 数据库 |
| 6379 | Redis 缓存 |
| 8848 | Nacos 控制台 |
| 9848/9849 | Nacos 2.x gRPC |
| 8082 | Go 爬虫服务 |

> 如果你买的是阿里云/腾讯云/华为云，记得去安全组里放行这些端口。

---

## 二、连接服务器

### Windows 用户

用 [MobaXterm](https://mobaxterm.mobatek.net/) 或 [PuTTY](https://www.putty.org/)，输入服务器 IP、用户名 `root`、密码登录。

### Mac / Linux 用户

打开终端：

```bash
ssh root@你的服务器IP
```

输入密码。

---

## 三、安装基础环境

> 以下命令全部在服务器上执行。

### 3.1 更新系统并安装工具

```bash
apt update && apt upgrade -y
apt install -y git curl wget vim unzip zip htop net-tools
```

### 3.2 安装 Docker

```bash
apt install -y docker.io docker-compose-plugin
systemctl enable docker --now

# 验证
docker --version
docker compose version
```

### 3.3 安装 JDK 17

```bash
apt install -y openjdk-17-jdk
java -version
```

### 3.4 安装 Maven

```bash
apt install -y maven
mvn -v
```

### 3.5 安装 Go

```bash
cd /tmp
wget https://go.dev/dl/go1.24.0.linux-amd64.tar.gz
rm -rf /usr/local/go
tar -C /usr/local -xzf go1.24.0.linux-amd64.tar.gz
echo 'export PATH=$PATH:/usr/local/go/bin' >> /etc/profile
echo 'export PATH=$PATH:/usr/local/go/bin' >> ~/.bashrc
source /etc/profile
go version
```

### 3.6 安装 Python 和 Node.js

```bash
apt install -y python3 python3-pip nodejs npm
python3 --version
node --version
npm --version
```

### 3.7 安装 Python 依赖

```bash
pip3 install requests urllib3 pycryptodome -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 3.8 安装 Nginx

```bash
apt install -y nginx
nginx -v
```

### 3.9 放行防火墙

```bash
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 3306/tcp
ufw allow 6379/tcp
ufw allow 8848/tcp
ufw allow 9848/tcp
ufw allow 9849/tcp
ufw allow 8082/tcp
ufw --force enable
ufw status
```

---

## 四、上传项目代码

### 4.1 创建目录

```bash
mkdir -p /opt/campus
cd /opt/campus
```

### 4.2 上传代码

**方式一：Git 拉取**

```bash
cd /opt/campus
git clone 你的仓库地址 .
```

**方式二：本地上传**

用 MobaXterm / WinSCP / scp 把当前项目文件夹里的内容上传到 `/opt/campus/`。

### 4.3 确认目录结构

```bash
ls -la /opt/campus
```

应该看到：

```
campus-assistant/
campus-spider-service/
campus-web/
deploy/
```

---

## 五、部署中间件

> 中间件 = MySQL + Redis + Nacos。这里使用项目当前代码能直接连上的默认配置。
>
> 当前项目默认配置：
> - MySQL：`root` / `1234`
> - Redis：`localhost:6379`，无密码
> - Nacos：`localhost:8848`

### 5.1 进入部署目录

```bash
cd /opt/campus/deploy
ls -la
```

### 5.2 创建必要目录和配置文件

```bash
mkdir -p mysql/conf mysql/data redis/data nacos/logs nacos/data
```

创建 MySQL 配置文件：

```bash
cat > mysql/conf/custom.cnf << 'EOF'
[mysqld]
max_connections=200
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+08:00'
EOF
```

创建 Redis 配置文件（**无密码，与项目代码一致**）：

```bash
cat > redis/redis.conf << 'EOF'
bind 0.0.0.0
port 6379
appendonly yes
timeout 300
maxmemory 256mb
maxmemory-policy allkeys-lru
EOF
```

创建环境变量文件（**MySQL 密码与项目代码一致**）：

```bash
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=1234
EOF
```

### 5.3 启动中间件

```bash
cd /opt/campus/deploy
docker compose -f docker-compose.middleware.yml up -d
```

等待 1-2 分钟，查看状态：

```bash
docker compose -f docker-compose.middleware.yml ps
```

三个容器都应该是 `running` 或 `healthy`。

### 5.4 验证中间件

```bash
# MySQL
docker exec -it campus-mysql mysql -uroot -p1234 -e "SHOW DATABASES;"

# Redis（无密码）
docker exec -it campus-redis redis-cli ping

# Nacos
curl http://localhost:8848/nacos
```

Nacos 控制台：`http://你的服务器IP:8848/nacos`，账号密码 `nacos`。

---

## 六、初始化数据库

中间件首次启动会自动执行 `deploy/init.sql`，创建 `campus_db` 数据库和表。

### 6.1 检查表是否创建

```bash
docker exec -it campus-mysql mysql -uroot -p1234 campus_db -e "SHOW TABLES;"
```

应该看到：

```
+----------------------+
| Tables_in_campus_db  |
+----------------------+
| course_db            |
| personal_info        |
| student_db           |
+----------------------+
```

### 6.2 如果表没创建，手动导入

```bash
cd /opt/campus/deploy
docker cp init.sql campus-mysql:/tmp/init.sql
docker exec -it campus-mysql mysql -uroot -p1234 -e "source /tmp/init.sql;"
```

---

## 七、部署 Java 服务

> 当前项目代码里的 Java 配置默认就是连接 `localhost:3306`、密码 `1234`、Redis `localhost:6379` 无密码、Nacos `localhost:8848`。
>
> **所以这一步基本不需要改配置文件，直接打包运行即可。**

### 7.1 编译打包

```bash
cd /opt/campus/campus-assistant
mvn clean package -DskipTests
```

第一次会比较慢，耐心等待，看到 `BUILD SUCCESS` 即可。

### 7.2 确认 jar 包

```bash
find /opt/campus/campus-assistant -name "*.jar" | grep target
```

应该看到三个 jar 包。

### 7.3 创建 systemd 服务

#### user-service（端口 8000）

```bash
cat > /etc/systemd/system/campus-user.service << 'EOF'
[Unit]
Description=Campus User Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus/campus-assistant/user-service
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m target/user-service-1.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

#### course-service（端口 9000）

```bash
cat > /etc/systemd/system/campus-course.service << 'EOF'
[Unit]
Description=Campus Course Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus/campus-assistant/course-service
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m target/course-service-1.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

#### gateway（端口 80）

```bash
cat > /etc/systemd/system/campus-gateway.service << 'EOF'
[Unit]
Description=Campus Gateway
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus/campus-assistant/campusswu-gateway
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m target/campusswu-gateway-1.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

### 7.4 启动 Java 服务

```bash
systemctl daemon-reload
systemctl enable --now campus-user campus-course campus-gateway

# 查看状态
systemctl status campus-user --no-pager
systemctl status campus-course --no-pager
systemctl status campus-gateway --no-pager
```

如果都是 `active (running)`，表示成功。

### 7.5 查看日志

```bash
journalctl -u campus-user -f
```

按 `Ctrl+C` 退出。

---

## 八、部署 Go + Python 服务

### 8.1 编译

```bash
cd /opt/campus/campus-spider-service
go mod tidy
go build -o server ./cmd/server

ls -la server
```

### 8.2 创建环境变量文件

> 这里的大部分配置保持默认值即可，因为中间件和 Java 服务都在同一台机器上。

```bash
cat > /opt/campus/campus-spider-service/.env << 'EOF'
HTTP_ADDR=0.0.0.0:8082
REDIS_ADDR=127.0.0.1:6379
REDIS_DB=0
WORKER_CONCURRENCY=4
JAVA_CALLBACK_URL=http://127.0.0.1:8000/internal/api/v1/sync/student-data
PUNCH_CALLBACK_URL=http://127.0.0.1:8000/internal/api/v1/sync/punch-result
JAVA_INTERNAL_TOKEN=internal-token
PYTHON_PATH=python3
SPIDER_SCRIPT=./scripts/spider_cli.py
CHECKIN_SCRIPT=./scripts/checkin_cli.py
SESSION_DIR=./data/sessions
SPIDER_TIMEOUT_MINUTES=20
CHECKIN_TIMEOUT_MINUTES=5
DEFAULT_ACADEMIC_YEAR=2025
DEFAULT_SEMESTER=12
EOF
```

> **注意**：`YM_TOKEN` 在 `checkin_token.py` 里已经硬编码，不需要在 `.env` 里配置。但你要确认这个 token 是否有效。

### 8.3 创建数据目录

```bash
mkdir -p /opt/campus/campus-spider-service/data/sessions
```

### 8.4 创建 systemd 服务

```bash
cat > /etc/systemd/system/campus-spider.service << 'EOF'
[Unit]
Description=Campus Spider Service
After=network.target redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/campus/campus-spider-service
EnvironmentFile=/opt/campus/campus-spider-service/.env
ExecStart=/opt/campus/campus-spider-service/server
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

### 8.5 启动

```bash
systemctl daemon-reload
systemctl enable --now campus-spider
systemctl status campus-spider --no-pager
```

### 8.6 验证

```bash
curl http://127.0.0.1:8082/health
```

返回：

```json
{"code":200,"message":"ok","data":{"status":"ok"}}
```

---

## 九、部署前端

### 9.1 安装依赖并打包

```bash
cd /opt/campus/campus-web
npm install
npm run build
```

打包产物在 `dist/` 目录。

### 9.2 配置 Nginx

备份默认配置：

```bash
mv /etc/nginx/sites-enabled/default /etc/nginx/sites-enabled/default.bak
```

创建新配置：

```bash
cat > /etc/nginx/sites-available/campus << 'EOF'
server {
    listen 80;
    server_name _;

    # 前端静态资源
    location / {
        root /opt/campus/campus-web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 请求转发到网关
    location /api/ {
        proxy_pass http://127.0.0.1:80/gateway/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    client_max_body_size 50M;
}
EOF
```

启用配置：

```bash
ln -s /etc/nginx/sites-available/campus /etc/nginx/sites-enabled/campus
nginx -t
systemctl restart nginx
systemctl status nginx --no-pager
```

---

## 十、启动与验证

### 10.1 启动顺序

```bash
# 1. 中间件
cd /opt/campus/deploy
docker compose -f docker-compose.middleware.yml up -d

# 2. Java 服务
systemctl start campus-user campus-course campus-gateway

# 3. Go 服务
systemctl start campus-spider

# 4. Nginx
systemctl start nginx
```

### 10.2 设置开机自启

```bash
systemctl enable docker
systemctl enable campus-user campus-course campus-gateway campus-spider nginx
```

### 10.3 一键启动脚本（可选）

```bash
cat > /opt/campus/start-all.sh << 'EOF'
#!/bin/bash
set -e

echo "启动中间件..."
cd /opt/campus/deploy
docker compose -f docker-compose.middleware.yml up -d

echo "等待中间件启动..."
sleep 15

echo "启动 Java 服务..."
systemctl start campus-user campus-course campus-gateway

echo "启动 Go 服务..."
systemctl start campus-spider

echo "启动 Nginx..."
systemctl start nginx

echo "所有服务启动完成"
EOF

chmod +x /opt/campus/start-all.sh
```

以后执行：

```bash
/opt/campus/start-all.sh
```

### 10.4 验证部署

#### 浏览器访问前端

```
http://你的服务器IP
```

应该看到登录页面。

#### 测试登录

用 SWU 学号和密码登录。如果登录成功，说明 Java 服务 + 网关 + 前端链路正常。

#### 测试刷新课表

登录后点击"刷新课表"，然后看 Go 服务日志：

```bash
journalctl -u campus-spider -f
```

应该看到爬虫任务执行并回调 Java。

---

## 十一、自动打卡验证

### 11.1 打卡调度策略

Java 端在 `PunchCardScheduledTask.java` 中：

- 每天晚上 **20:00** 重置所有人状态为"未打卡"
- 每天晚上 **21:00 - 22:30**，每 **15 分钟**轮询一次
- 只处理"未打卡"和"打卡失败"的用户
- 每批 100 个用户，错峰 200ms

### 11.2 查看打卡状态

```bash
docker exec -it campus-mysql mysql -uroot -p1234 campus_db -e "SELECT student_id, punch_status FROM student_db;"
docker exec -it campus-mysql mysql -uroot -p1234 campus_db -e "SELECT student_id, avatar, background, wallpaper, card_opacity, card_blur, wallpaper_mask, global_font_enabled FROM user_profile_style;"
```

| 状态值 | 含义 |
|--------|------|
| 0 | 未打卡 |
| 1 | 打卡中 |
| 2 | 打卡成功 |
| 3 | 打卡失败 |

晚上 21:00 后，如果看到状态从 0 变成 2，说明自动打卡成功。

---

## 十二、常见问题

### 12.1 浏览器打开是空白

```bash
systemctl status nginx
nginx -t
ls -la /opt/campus/campus-web/dist
```

### 12.2 能打开前端但登录失败

```bash
journalctl -u campus-gateway -f
journalctl -u campus-user -f
```

检查 Nacos 服务列表：

```bash
curl "http://127.0.0.1:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10"
```

### 12.3 Java 调用 Go 失败

```bash
curl http://127.0.0.1:8082/health
journalctl -u campus-spider -f
```

检查 Java 中 `SpiderServiceClient` 的 URL 是否是 `http://127.0.0.1:8082`。

### 12.4 自动打卡不触发

```bash
journalctl -u campus-user -f
```

检查密码缓存：

```bash
docker exec -it campus-redis redis-cli keys "user:pwd:*"
```

如果为空，说明用户没登录或缓存过期，需要重新登录。

### 12.5 验证码识别失败

检查 `checkin_token.py` 中的 `YM_TOKEN` 是否还有效、余额是否充足。

### 12.6 MySQL 连接失败

```bash
docker ps | grep campus-mysql
docker logs campus-mysql
```

确认 Java 配置中的密码是 `1234`。

---

## 十三、生产环境加固（可选）

当前文档为了让你快速跑通，使用了项目默认的弱密码/无密码配置。**正式上线前建议做以下加固**：

### 13.1 修改 MySQL 密码

1. 修改 `deploy/.env` 中的 `MYSQL_ROOT_PASSWORD`
2. 修改 Java 三个服务中 `application-datasource.yml` 的 `password`
3. 重新打包 Java 服务

### 13.2 Redis 加密码

1. 在 `deploy/redis/redis.conf` 中添加 `requirepass 你的密码`
2. 修改 Java `application-datasource.yml` 中 redis 配置添加 `password`
3. 修改 Go `.env` 中 `REDIS_PASSWORD=你的密码`

### 13.3 修改默认 Token

- 修改 Go `.env` 中的 `JAVA_INTERNAL_TOKEN`
- 同步修改 Java 端对应配置

### 13.4 修改 AES 密钥

- 修改 `campus-assistant/campusswu-core/src/main/resources/application-aes.yml`
- 同步修改 Go 端 `internal/crypto/aes.go` 中的密钥

### 13.5 关闭公网暴露的管理端口

- 3306、6379、8848、8082 只监听内网或绑定安全组白名单

### 13.6 配置 HTTPS

申请 SSL 证书，在 Nginx 中配置 443 端口。

### 13.7 数据库备份

```bash
mkdir -p /opt/backup
docker exec campus-mysql mysqldump -uroot -p1234 campus_db > /opt/backup/campus_db_$(date +%Y%m%d).sql
```

---

**至此，按当前项目默认配置部署完成。第十三章是上线前必须做的安全加固，测试阶段可跳过。**
