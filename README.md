# 校园助手系统

本项目由 **Java 微服务后端**（`campus-assistant`）、**Go + Python 爬虫服务**（`campus-spider-service`）和 **Vue 3 前端**（`campus-web`）组成，提供西南大学教务系统的注册登录、课表同步、个人资料展示、个性化主页与自动打卡开关等能力。

---

## 模块说明

| 模块 | 技术栈 | 职责 |
|------|--------|------|
| `campus-assistant/campusswu-gateway` | Spring Cloud Gateway | 统一入口、JWT 鉴权、路由转发 |
| `campus-assistant/user-service` | Spring Boot 3 + MyBatis Plus | 用户注册/登录、状态管理、个人信息、个性化配置 |
| `campus-assistant/course-service` | Spring Boot 3 + MyBatis Plus | 课表存储与查询 |
| `campus-spider-service` | Go 1.24 + Python 3 | 教务系统登录、数据抓取、打卡任务调度 |
| `campus-web` | Vue 3 + Vite + Axios | 登录页、课表页、个人主页、个性化设置 |
| 基础设施 | MySQL、Redis、Nacos | 持久化、缓存、注册发现与配置中心 |

---

## 当前已落地功能

### 后端 / 数据侧

- 用户注册、登录、刷新同步、注销账号
- 课表数据抓取与查询
- 用户同步状态与打卡状态查询
- 用户个性化主页配置保存
- 自动打卡开关持久化

### 前端侧

- 登录页与课表页
- 个人主页资料卡展示
- 个性化设置面板：
  - 资料卡透明度
  - 资料卡模糊度
  - 墙纸蒙版强度
  - 全局字体开关
  - 默认头像选择
  - 默认顶部背景选择
  - 默认壁纸选择
- 自动打卡开关
- 前端统一处理响应体 `code = 401` 的登录失效场景

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

### 前端依赖

前端依赖由 `campus-web/package.json` 管理，核心依赖包括：

- `vue`
- `vue-router`
- `axios`
- `vite`
- `@vitejs/plugin-vue`

---

## 从零启动

> 以下示例以 Windows PowerShell 为主。

### 1. 安装基础环境

#### 1.1 JDK 17

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
java -version
```

#### 1.2 Maven 3.8+

```powershell
winget install Apache.Maven
mvn -v
```

#### 1.3 Go 1.24

```powershell
winget install GoLang.Go
go version
```

#### 1.4 Python 3.8+

```powershell
winget install Python.Python.3.12
python --version
```

#### 1.5 Node.js 18+

```powershell
winget install OpenJS.NodeJS.LTS
node -v
npm -v
```

#### 1.6 MySQL 8.0

```powershell
winget install Oracle.MySQL
net start MySQL80
```

#### 1.7 Redis

```powershell
winget install Memurai.Memurai
```

#### 1.8 Nacos 2.x

1. 下载 `nacos-server-2.3.2.zip` 并解压到 `C:\nacos`
2. 启动：

```powershell
cd C:\nacos\bin
startup.cmd -m standalone
```

3. 访问 [http://localhost:8848/nacos](http://localhost:8848/nacos)
4. 新建命名空间：`dev`

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
```

### 建表 SQL（新环境推荐直接使用）

```sql
CREATE TABLE IF NOT EXISTS student_db (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL COMMENT '教务学号',
    password VARCHAR(128) NOT NULL COMMENT 'BCrypt加密后的密码',
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
```

### 旧环境升级 SQL（已有表时执行）

```sql
ALTER TABLE student_db
  ADD COLUMN auto_punch_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启自动打卡：0关闭 1开启';

ALTER TABLE user_profile_style
  ADD COLUMN card_blur INT DEFAULT 14 COMMENT '资料卡模糊度',
  ADD COLUMN wallpaper_mask DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '墙纸蒙版强度(0.00-1.00)',
  ADD COLUMN global_font_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用全局字体：0关闭 1开启';
```

> 如果列已存在，请根据数据库实际状态手动跳过对应 `ALTER TABLE` 语句。

---

## Nacos 配置

在 `dev` 命名空间下创建以下配置。

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
    cloudstructuretemplate: info
```

---

## 编译项目

### 1. 安装 Python 依赖

```powershell
cd campus-spider-service\scripts
pip install requests urllib3
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

---

## 启动顺序

| 顺序 | 服务 | 命令 |
|------|------|------|
| 1 | MySQL | `net start MySQL80` |
| 2 | Redis | 确认 Memurai / Redis 服务已运行 |
| 3 | Nacos | `C:\nacos\bin\startup.cmd -m standalone` |
| 4 | Gateway | `cd campus-assistant && mvn spring-boot:run -pl campusswu-gateway -am` |
| 5 | User-Service | `cd campus-assistant && mvn spring-boot:run -pl user-service -am` |
| 6 | Course-Service | `cd campus-assistant && mvn spring-boot:run -pl course-service -am` |
| 7 | Go 爬虫服务 | `cd campus-spider-service && $env:PYTHON_PATH="python"; .\server.exe` |
| 8 | 前端开发服务 | `cd campus-web && npm run dev` |

> Go 爬虫服务启动前必须设置 `PYTHON_PATH`。

---

## 前端相关说明

### 本地访问

- 前端开发地址：`http://localhost:5173`
- User-Service 文档地址：`http://localhost:8000/doc.html`

### 当前前端接口约定

前端通过 `campus-web/src/config/apiPaths.js` 管理接口路径，当前新增并已接入的接口包括：

- `GET /personalization/get-profile`
- `PUT /personalization/update-profile`
- `GET /personalization/get-default-options`
- `PUT /user/auto-punch`

### 个性化配置字段

`GET /personalization/get-profile` 当前前端依赖以下字段：

```json
{
  "avatar": "/avatar/avatar_default_1.jpg",
  "background": "/background/background_default_1.jpg",
  "wallpaper": "/wallpaper/wallpaper_default_1.jpg",
  "cardOpacity": 1.00,
  "cardBlur": 14,
  "wallpaperMask": 1.00,
  "globalFontEnabled": 1
}
```

### 自动打卡字段

`GET /user/status` / `PUT /user/auto-punch` 当前前端依赖：

```json
{
  "autoPunchEnabled": 1
}
```

> 前端当前按 `0/1` 数值与后端对齐，而不是布尔值。

### 字体资源

前端当前使用的全局字体文件为：

- `campus-web/public/fonts/SourceHanSerifCN-Regular.ttf`

---

## 服务验证

所有服务启动后，可检查端口：

```powershell
netstat -ano | findstr ":80 "
netstat -ano | findstr ":8000 "
netstat -ano | findstr ":9000 "
netstat -ano | findstr ":8082 "
netstat -ano | findstr ":5173 "
```

| 端口 | 服务 |
|------|------|
| 80 | Gateway |
| 8000 | User-Service |
| 9000 | Course-Service |
| 8082 | Go 爬虫服务 |
| 5173 | 前端开发服务 |

---

## 常见问题

### Q1：前端页面接口返回 HTTP 200，但页面仍跳回登录页

这是预期行为之一。前端已统一处理“HTTP 200 但响应体 `code = 401`”的场景，会主动清除 token 并跳转登录页。

### Q2：资料卡模糊度或字体开关保存后不生效

优先检查：

- `user_profile_style.card_blur` 是否存在并有值
- `user_profile_style.wallpaper_mask` 是否存在并有值
- `user_profile_style.global_font_enabled` 是否存在并有值
- 前端 `campus-web/public/fonts/SourceHanSerifCN-Regular.ttf` 是否存在
- 后端返回字段是否为 `cardBlur`、`wallpaperMask` 与 `globalFontEnabled`

### Q3：自动打卡开关点击后无效果

优先检查：

- `student_db.auto_punch_enabled` 字段是否存在
- `PUT /user/auto-punch` 是否成功落库
- `GET /user/status` 是否返回 `autoPunchEnabled`
- 前后端是否统一使用 `0/1` 而非 `true/false`

### Q4：默认头像、背景或壁纸不显示

检查后端静态资源与默认配置，推荐至少存在：

```text
src/main/resources/static/avatar/avatar_default_1.jpg
src/main/resources/static/background/background_default_1.jpg
src/main/resources/static/wallpaper/wallpaper_default_1.jpg
```

### Q5：Go 爬虫服务调用 Python 脚本失败

启动前显式设置：

```powershell
$env:PYTHON_PATH="C:\Users\xxx\AppData\Local\Programs\Python\Python312\python.exe"
```

### Q6：Course-Service 返回 503

检查 Nacos 服务发现、Gateway 路由以及 `course-service` 注册状态是否正常。

---

## 测试说明

完整链路测试请查看根目录：

- `E:\develop\idea\collaborative project\messwuger\TESTING.md`
