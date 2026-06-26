# 校园助手项目部署手册

> 目标：以当前仓库结构为准，在一台 Ubuntu 22.04 服务器上完成中间件、Java 微服务、Go 爬虫服务和前端静态页面部署。
>
> 说明：本文档以**后端服务可稳定运行**为主，前端只保留必要的构建与静态托管步骤。

---

## 目录

1. 准备工作
2. 安装基础环境
3. 上传项目代码
4. 部署中间件
5. 初始化数据库
6. 配置 OSS
7. 部署 Java 服务
8. 部署 Go + Python 服务
9. 部署前端
10. 启动与验证
11. 常见问题
12. 生产环境加固

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
| 8082 | Go 爬虫服务 |
| 8000 | User-Service（仅内网或联调时开放） |
| 9000 | Course-Service（仅内网或联调时开放） |

> 若使用云服务器，请同步配置安全组。生产环境建议仅开放 `22`、`80`、`443`，其余端口收敛到内网。

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
```

### 3.1 Git 拉取

```bash
git clone <你的仓库地址> .
```

### 3.2 检查目录

```bash
ls -la /opt/campus
```

应至少看到：

```text
campus-assistant/
campus-spider-service/
campus-web/
deploy/
```

---

## 四、部署中间件

### 4.1 创建目录

```bash
cd /opt/campus/deploy
mkdir -p mysql/conf mysql/data redis/data nacos/logs nacos/data
```

### 4.2 创建 MySQL 配置

```bash
cat > mysql/conf/custom.cnf << 'EOF'
[mysqld]
max_connections=200
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+08:00'
EOF
```

### 4.3 创建 Redis 配置

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

### 4.4 创建环境变量

```bash
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=1234
EOF
```

### 4.5 启动中间件

```bash
docker compose -f docker-compose.middleware.yml up -d
docker compose -f docker-compose.middleware.yml ps
```

---

## 五、初始化数据库

中间件首次启动会自动执行 `deploy/init.sql`，但当前仓库内的 `init.sql` 只覆盖基础用户 / 个人信息 / 课表表。

### 5.1 检查基础表

```bash
docker exec -it campus-mysql mysql -uroot -p1234 campus_db -e "SHOW TABLES;"
```

### 5.2 执行当前版本扩展 SQL

```bash
docker exec -i campus-mysql mysql -uroot -p1234 campus_db <<'EOF'
ALTER TABLE student_db
  ADD COLUMN auto_punch_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启自动打卡：0关闭 1开启';

CREATE TABLE IF NOT EXISTS user_profile_style (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(32) NOT NULL COMMENT '学号',
  avatar VARCHAR(255) NOT NULL COMMENT '头像地址',
  background VARCHAR(255) NOT NULL COMMENT '顶部背景地址',
  wallpaper VARCHAR(255) NOT NULL COMMENT '墙纸地址',
  card_opacity DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '资料卡透明度',
  card_blur INT DEFAULT 14 COMMENT '资料卡模糊度',
  wallpaper_mask DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '墙纸蒙版强度(0.00-1.00)',
  global_font_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用全局字体：0关闭 1开启',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个性化配置表';

CREATE TABLE IF NOT EXISTS user_profile_custom_asset (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(32) NOT NULL COMMENT '学号',
  custom_avatar VARCHAR(255) DEFAULT NULL COMMENT '自定义头像 OSS 地址',
  custom_background VARCHAR(255) DEFAULT NULL COMMENT '自定义顶部背景 OSS 地址',
  custom_wallpaper VARCHAR(255) DEFAULT NULL COMMENT '自定义墙纸 OSS 地址',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义图片资源表';
EOF
```

> 若列已存在，请手动去掉重复 SQL 再执行。

---

## 六、配置 OSS

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

### 6.1 推荐做法

- 不要把真实 AK / SK 直接提交到仓库
- 服务器上外置 `application-aliyun.yml`
- 或通过环境变量 / Nacos 配置中心注入
- 优先使用 RAM 子账号最小权限 AK / SK

### 6.2 关于图片删除

当前实现会在上传新图片后更新数据库中的最新 URL，但**不会自动删除旧 OSS 对象**。

- 测试环境：可以手动删除，或者暂时保留
- 生产环境：建议增加旧对象删除逻辑，或为 `profile-custom/` 目录配置生命周期规则

---

## 七、部署 Java 服务

### 7.1 编译打包

```bash
cd /opt/campus/campus-assistant
mvn clean package -DskipTests
```

### 7.2 创建 systemd 服务

#### Gateway

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

#### User-Service

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

#### Course-Service

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

### 7.3 启动服务

```bash
systemctl daemon-reload
systemctl enable --now campus-gateway campus-user campus-course
systemctl status campus-gateway --no-pager
systemctl status campus-user --no-pager
systemctl status campus-course --no-pager
```

---

## 八、部署 Go + Python 服务

### 8.1 编译

```bash
cd /opt/campus/campus-spider-service
go mod tidy
go build -o server ./cmd/server
```

### 8.2 创建环境变量文件

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

### 8.3 创建 systemd 服务

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

### 8.4 启动服务

```bash
mkdir -p /opt/campus/campus-spider-service/data/sessions
systemctl daemon-reload
systemctl enable --now campus-spider
systemctl status campus-spider --no-pager
```

---

## 九、部署前端

> 前端只保留静态页面托管，核心状态与资源路径以后端接口返回为准。

### 9.1 构建前端

```bash
cd /opt/campus/campus-web
npm install
npm run build
```

### 9.2 配置 Nginx

```bash
cat > /etc/nginx/sites-available/campus << 'EOF'
server {
    listen 80;
    server_name _;

    location / {
        root /opt/campus/campus-web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

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

ln -sf /etc/nginx/sites-available/campus /etc/nginx/sites-enabled/campus
nginx -t
systemctl restart nginx
```

---

## 十、启动与验证

### 10.1 启动顺序

```bash
cd /opt/campus/deploy
docker compose -f docker-compose.middleware.yml up -d
systemctl start campus-gateway campus-user campus-course
systemctl start campus-spider
systemctl start nginx
```

### 10.2 验证

```bash
curl http://127.0.0.1:8082/health
curl http://127.0.0.1:8848/nacos
systemctl status campus-gateway --no-pager
systemctl status campus-user --no-pager
systemctl status campus-course --no-pager
systemctl status campus-spider --no-pager
systemctl status nginx --no-pager
```

浏览器访问：

```text
http://你的服务器IP
```

---

## 十一、常见问题

### 11.1 OSS 图片上传成功但页面空白

优先检查：

- `user_profile_custom_asset` 是否已落库
- 返回的 URL 是否能直接访问
- Bucket 是否允许当前访问方式读取
- OSS CORS 是否允许浏览器加载图片

### 11.2 Java 调用 Go 失败

```bash
curl http://127.0.0.1:8082/health
journalctl -u campus-spider -f
```

### 11.3 自动打卡不触发

```bash
journalctl -u campus-user -f
docker exec -it campus-redis redis-cli keys "user:pwd:*"
```

### 11.4 MySQL 连接失败

```bash
docker ps | grep campus-mysql
docker logs campus-mysql
```

---

## 十二、生产环境加固

### 12.1 数据与密钥

- 修改 MySQL 密码
- 为 Redis 增加密码
- 修改内部调用 Token
- 使用外部配置管理敏感信息

### 12.2 OSS

- Bucket 不要长期使用公共读
- 优先使用私有读写 + 签名 URL / CDN 鉴权 / 后端中转访问
- 为 `profile-custom/` 配置生命周期规则
- 若要控制存量，补充“替换成功后删除旧对象”逻辑

### 12.3 网络暴露

- 3306、6379、8848、8082、8000、9000 仅保留内网访问
- 对外仅暴露 `80/443`

### 12.4 备份

```bash
mkdir -p /opt/backup
docker exec campus-mysql mysqldump -uroot -p1234 campus_db > /opt/backup/campus_db_$(date +%Y%m%d).sql
```

---

按当前文档完成后，项目应能以“后端服务为主、前端静态托管为辅”的方式稳定部署。
