# 校园助手系统

本项目由 **Java 微服务后端**（`campus-assistant`）、**Go + Python 爬虫服务**（`campus-spider-service`）和 **Vue 3 前端**（`campus-web`）组成，当前已经打通注册登录、课表同步、成绩查询、空教室查询、个性化主页、自定义图片资源、管理员资源入口与自动打卡开关等核心链路。

---

## 模块说明

| 模块 | 技术栈 | 职责 |
|------|--------|------|
| `campus-assistant/campusswu-gateway` | Spring Cloud Gateway | 统一入口、JWT 鉴权、路由转发 |
| `campus-assistant/user-service` | Spring Boot 3 + MyBatis Plus | 用户注册/登录、状态管理、个人信息、个性化配置、自定义资源管理、管理员资源查询、异步任务发起 |
| `campus-assistant/course-service` | Spring Boot 3 + MyBatis Plus | 课表存储与查询、成绩存储与查询 |
| `campus-spider-service` | Go 1.24 + Python 3 | 教务系统登录、课表抓取、成绩抓取、空教室抓取、打卡任务调度 |
| `campus-web` | Vue 3 + Vite + Axios | 登录页、课表页、成绩页、空教室页、个人主页、管理员页 |
| 基础设施 | MySQL、Redis、Nacos、OSS、SkyWalking | 持久化、缓存、注册发现与配置中心、图片对象存储、链路追踪 |

---

## 当前已落地功能

### 后端 / 数据侧

- 用户注册、登录、刷新同步、注销账号
- 课表数据抓取与查询
- 成绩任务发起、异步回调、成绩落库与查询
- 空教室任务发起、异步回调、结果缓存与查询
- 自动打卡开关持久化
- 用户个性化主页配置保存
- 用户自定义头像、顶部背景、墙纸上传与地址持久化
- 阿里云 OSS 上传接入
- 管理员登录与资源入口查询
- Go 爬虫服务：多优先级 Redis Stream 队列、幂等去重、死信队列重试、僵尸消息恢复、全局限流

### 前端侧

- 登录页、课表页、成绩页、空教室页、个人主页、管理员页
- 个性化设置：资料卡透明度 / 模糊度 / 墙纸蒙版 / 全局字体 / 自动打卡
- 默认图片切换、自定义图片上传裁剪与回显
- 成绩查询参数缓存、表格视图、排序切换
- 空教室图形化条件选择、结果表格展示、查询条件缓存
- 用户端与管理员端采用双 token 存储，支持同一浏览器同时保持两套登录态
- 管理员日志页支持日志文件分组、初始化窗口、向前加载历史、重置控制台、单次刷新、手动开启轮询与压缩日志下载

---

## 环境依赖

### 基础环境

| 组件 | 版本要求 |
|------|---------|
| JDK | 17 |
| Maven | 3.8+ |
| Go | 1.24 |
| Python | 3.8+ |
| Node.js | 18+ |
| npm | 9+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Nacos | 2.x |
| Docker Desktop | 当前本机已验证可使用 |
| Docker Compose | v2 |

### Java 主要依赖

| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.3.2 |
| MyBatis Plus | 3.5.7 |
| MySQL Connector/J | 8.0.33 |
| java-jwt | 4.4.0 |
| Knife4j (OpenAPI 3) | 4.4.0 |
| MapStruct | 1.5.2.Final |
| Lombok | 1.18.30 |
| SkyWalking APM Toolkit | 9.0.0 |
| Aliyun OSS SDK | 3.17.4 |

### Go 主要依赖

| 依赖 | 版本 |
|------|------|
| `github.com/google/uuid` | `v1.6.0` |
| `github.com/redis/go-redis/v9` | `v9.20.1` |

### Python 依赖

| 依赖 | 安装方式 |
|------|---------|
| `requests` | `pip install requests` |
| `urllib3` | `pip install urllib3` |
| `pycryptodome` | `pip install pycryptodome` |

---

## 数据库初始化

### 新建数据库

```powershell
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS campus_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_db;

CREATE TABLE IF NOT EXISTS student_db (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          student_id VARCHAR(32) NOT NULL COMMENT '教务学号',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt加密后的密码',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
    sync_status TINYINT DEFAULT 0 COMMENT '0未同步 1同步中 2成功 3失败',
    punch_status TINYINT DEFAULT 0 COMMENT '打卡状态：0未打卡 1打卡中 2打卡成功 3打卡失败',
    auto_punch_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启自动打卡：0关闭 1开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_id (student_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS personal_info (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             student_id VARCHAR(32) NOT NULL COMMENT '学号',
    name VARCHAR(64) DEFAULT NULL COMMENT '姓名',
    major VARCHAR(128) DEFAULT NULL COMMENT '专业',
    class_name VARCHAR(128) DEFAULT NULL COMMENT '班级',
    college VARCHAR(128) DEFAULT NULL COMMENT '学院',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_id (student_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人信息表';

CREATE TABLE IF NOT EXISTS course_db (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         student_id VARCHAR(32) NOT NULL COMMENT '学号',
    academic_year VARCHAR(16) DEFAULT NULL COMMENT '学年',
    semester VARCHAR(8) DEFAULT NULL COMMENT '学期',
    schedule_json LONGTEXT COMMENT '课表JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表数据表';

CREATE TABLE IF NOT EXISTS student_grade (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             student_id VARCHAR(32) NOT NULL COMMENT '学号',
    academic_year VARCHAR(16) DEFAULT NULL COMMENT '学年',
    semester VARCHAR(8) DEFAULT NULL COMMENT '学期',
    course_name VARCHAR(128) DEFAULT NULL COMMENT '课程名称',
    course_code VARCHAR(64) DEFAULT NULL COMMENT '课程代码',
    course_nature VARCHAR(64) DEFAULT NULL COMMENT '课程性质',
    credit VARCHAR(32) DEFAULT NULL COMMENT '学分',
    score VARCHAR(32) DEFAULT NULL COMMENT '成绩',
    gpa VARCHAR(32) DEFAULT NULL COMMENT '绩点',
    teacher VARCHAR(64) DEFAULT NULL COMMENT '任课教师',
    exam_nature VARCHAR(64) DEFAULT NULL COMMENT '考试性质',
    course_type VARCHAR(64) DEFAULT NULL COMMENT '课程类别',
    sync_time DATETIME DEFAULT NULL COMMENT '本次同步时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_term (student_id, academic_year, semester)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩数据表';

CREATE TABLE IF NOT EXISTS user_profile_style (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  student_id VARCHAR(32) NOT NULL COMMENT '学号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址，可为空，空时前端使用姓名首字母兜底',
    background VARCHAR(255) DEFAULT NULL COMMENT '顶部背景地址，可为空，空时前端使用纯白极简背景',
    wallpaper VARCHAR(255) DEFAULT NULL COMMENT '墙纸地址，可为空，空时前端使用浅灰极简背景',
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

```

### 核心表

当前初始化脚本位置：`deploy/init.sql`

当前至少包含以下表：

- `student_db`
- `personal_info`
- `course_db`
- `student_grade`
- `user_profile_style`
- `user_profile_custom_asset`

如需手动初始化，可直接执行 `deploy/init.sql`。

---

## Nacos 配置

在 `dev` 命名空间下创建以下配置。当前仓库也提供了本地示例目录：`nacos_config/`。

### `datasource-mysql.yaml`（group: `DATASOURCE_GROUP`）

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/campus_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 1234
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### `datasource-redis.yaml`（group: `DATASOURCE_GROUP`）

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000
  data:
    redis:
      host: localhost
      port: 6379
```

### `gateway-auth.yaml`（group: `GATEWAY_GROUP`）

```yaml
gatewaylist:
  whitelist:
    - /gateway/auth/login
    - /gateway/auth/register
    - /gateway/admin/login
  adminlist:
    - /gateway/admin/**

security:
  gatewaySecret: YOUR_GATEWAY_SECRET
```

### `common-log.yaml`（group: `DEFAULT_GROUP`）

```yaml
logging:
  file:
    name: logs/${spring.application.name}.log
  level:
    campusassistant: info
```

### `aliyun-oss.yaml`（group: `DEFAULT_GROUP`）

```yaml
aliyun:
  oss:
    endpoint: https://oss-cn-beijing.aliyuncs.com
    access-key-id: <your-access-key-id>
    access-key-secret: <your-access-key-secret>
    bucket-name: <your-bucket-name>
    url-prefix: https://<your-bucket-name>.oss-cn-beijing.aliyuncs.com
```

### `admin-resources.yaml`（group: `DEFAULT_GROUP`）

```yaml
admin:
  resources:
    items:
      - code: skywalking
        name: SkyWalking
        url: http://127.0.0.1:18080
      - code: nacos
        name: Nacos
        url: http://127.0.0.1:8848/nacos
      - code: sentinel
        name: Sentinel
        url: http://127.0.0.1:8858
```

---

## 编译项目

### 1. 安装 Python 依赖

```powershell
cd campus-spider-service\scripts
pip install requests urllib3 pycryptodome
```

### 2. 编译 Go 爬虫服务

```powershell
cd ..
go mod tidy
go build -o server.exe ./cmd/server
```

### 3. 编译 Java 微服务

```powershell
cd ..\campus-assistant
mvn clean install -DskipTests
```

### 4. 安装并编译前端

```powershell
cd ..\campus-web
npm install
npm run build
```

### 5. 服务器部署

生产或服务器环境请直接参考 [DEPLOYMENT.md](DEPLOYMENT.md)，使用离线镜像包一键部署。

---

## 启动顺序（本地开发）

| 顺序 | 服务 | 命令 |
|------|------|------|
| 1 | MySQL / Redis / Nacos / Sentinel | `cd deploy/offline/package && docker compose up -d mysql redis nacos sentinel` |
| 2 | SkyWalking（可选） | `cd deploy && docker compose -f docker-compose.skywalking.yml up -d` |
| 3 | Gateway | `cd campus-assistant && mvn spring-boot:run -pl campusswu-gateway -am` |
| 4 | User-Service | `cd campus-assistant && mvn spring-boot:run -pl user-service -am` |
| 5 | Course-Service | `cd campus-assistant && mvn spring-boot:run -pl course-service -am` |
| 6 | Go 爬虫服务 | `cd campus-spider-service && $env:PYTHON_PATH="python"; .\server.exe` |
| 7 | 前端开发服务 | `cd campus-web && npm run dev` |

> Go 爬虫服务启动前必须设置 `PYTHON_PATH`，并确保 `JAVA_CALLBACK_URL`、`PUNCH_CALLBACK_URL`、`GRADES_CALLBACK_URL`、`EMPTY_CLASSROOM_CALLBACK_URL` 指向本地 User-Service。

---

## 接口与资源说明

### 本地访问

- 前端开发地址：`http://localhost:5173`
- User-Service 文档地址：`http://localhost:8000/doc.html`
- SkyWalking UI：`http://127.0.0.1:18080`
- Nacos：`http://127.0.0.1:8848/nacos`
- Sentinel：`http://127.0.0.1:8858`

### 用户核心接口

- `GET /personalization/get-profile`
- `PUT /personalization/update-profile`
- `GET /personalization/get-default-options`
- `GET /personalization/get-custom-assets`
- `POST /personalization/upload-custom-asset`
- `PUT /user/auto-punch`
- `POST /user/grades/task`
- `POST /user/grades/result`
- `POST /user/empty-classroom/task`
- `POST /user/empty-classroom/result`

### Go 内部回调接口

- `POST /internal/api/v1/sync/student-data`
- `POST /internal/api/v1/sync/punch-result`
- `POST /internal/api/v1/sync/grades`
- `POST /internal/api/v1/sync/empty-classroom`

### 管理员接口

- `POST /admin/login`
- `POST /admin/logout`
- `GET /admin/resources`
- `GET /admin/logs/files`
- `GET /admin/logs/tail/init`
- `GET /admin/logs/tail/poll`
- `GET /admin/logs/tail/history`
- `GET /admin/logs/download`

---

## 默认资源与兜底说明

当前版本允许 `avatar`、`background`、`wallpaper` 为空；前端会自动兜底为极简默认样式：

- `avatar` 为空：显示姓名首字母头像
- `background` 为空：显示纯白顶部背景
- `wallpaper` 为空：显示浅灰墙纸背景

若希望后端初始化时就显式写入“空资源”，建议在 `application-static.yml` 中使用空字符串 `""`，不要只保留空冒号。

> 当前实现只负责上传新对象并更新数据库记录，不会自动删除历史 OSS 图片。测试环境可手动清理，生产环境建议增加旧对象删除逻辑或配置生命周期规则。

---

## 常见问题

### Q1：SkyWalking UI 里为什么还会看到历史项目

因为历史项目和当前项目如果共用同一个 OAP 和存储，SkyWalking UI 会一起展示。要彻底隔离，必须单独起一套新的 OAP / UI / 存储，并把当前项目 Agent 改为上报到新的 OAP。

### Q2：前端页面接口返回 HTTP 200，但页面仍跳回登录页

这是预期行为之一。前端已统一处理“HTTP 200 但响应体 `code = 401`”的场景，会主动清除 token 并跳转登录页。

### Q3：成绩查询返回空数组，但 Go 似乎没报错

优先检查：

- Go 爬虫是否已重编译，而不是仍在运行旧 `server.exe`
- `GRADES_CALLBACK_URL` 是否已正确配置
- `student_grade` 是否已成功落库
- Java 查询接口是否命中了正确学年 / 学期

### Q4：空教室提交成功但查询不到结果

优先检查：

- Go 回调 payload 中是否完整回传 `campusId`、`building`、`roomType`
- `EMPTY_CLASSROOM_CALLBACK_URL` 是否已正确配置
- Java 端查询时使用的指纹是否与回调写入指纹一致
- Redis 中查询状态是否仍卡在 `QUERYING`

### Q5：Go 爬虫服务调用 Python 脚本失败

启动前显式设置：

```powershell
$env:PYTHON_PATH="C:\Users\xxx\AppData\Local\Programs\Python\Python312\python.exe"
```

---

## 测试说明

完整链路测试请查看根目录：

- [TESTING.md](TESTING.md)
