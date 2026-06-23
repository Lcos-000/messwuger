# 校园助手系统

本项目由 **Java 微服务**（`campus-assistant`）与 **Go + Python 爬虫服务**（`campus-spider-service`）组成，提供西南大学教务系统的课表同步与查询功能。

---

## 技术架构

| 模块 | 技术栈 | 职责 |
|------|--------|------|
| Gateway | Spring Cloud Gateway | 统一入口、JWT 鉴权、路由转发 |
| User-Service | Spring Boot 3 + MyBatis Plus | 用户注册/登录、个人信息管理 |
| Course-Service | Spring Boot 3 + MyBatis Plus | 课表数据存储与查询 |
| Spider-Service | Go 1.24 + Python 3 | 爬虫调度、教务系统登录与数据抓取 |
| 基础设施 | Nacos、Redis、MySQL | 注册中心、缓存、持久化 |

---

## 完整依赖清单

### Java 侧（Maven 自动管理）

| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.3.2 |
| MyBatis Plus | 3.5.7 |
| MySQL Connector/J | 8.0.33 |
| java-jwt | 4.4.0 |
| MapStruct | 1.5.2.Final |
| Lombok | 1.18.30 |
| SkyWalking APM Toolkit | 9.0.0 |

### Go 侧

| 依赖 | 版本 |
|------|------|
| github.com/google/uuid | v1.6.0 |
| github.com/redis/go-redis/v9 | v9.20.1 |

### Python 侧

| 依赖 | 安装方式 |
|------|---------|
| requests | `pip install requests` |
| urllib3 | `pip install urllib3` |

### 基础设施

| 组件 | 版本要求 |
|------|---------|
| JDK | 17 |
| Maven | 3.8+ |
| Go | 1.24 |
| Python | 3.8+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Nacos | 2.x |

---

## 从零上手详细步骤

> 以下步骤以 **Windows** 环境为主，其他系统命令可类比替换。

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

#### 1.5 MySQL 8.0

```powershell
winget install Oracle.MySQL
net start MySQL80
```

> 安装时设置 root 密码，后续 Nacos 配置需要用到。

#### 1.6 Redis

```powershell
winget install Memurai.Memurai
# 或下载社区版 Redis-for-Windows 解压运行 redis-server.exe
```

#### 1.7 Nacos 2.x

1. 下载 `nacos-server-2.3.2.zip` 并解压到 `C:\nacos`
2. 启动：

```powershell
cd C:\nacos\bin
startup.cmd -m standalone
```

3. 访问 http://localhost:8848/nacos，默认账号密码 `nacos` / `nacos`
4. 进入 **命名空间**，新建一个 ID 为 `dev` 的命名空间

---

### 2. 初始化 MySQL 数据库

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
    sync_status TINYINT DEFAULT 0 COMMENT '0未同步 1同步中 2成功 3失败',
    punch_status TINYINT DEFAULT 0 COMMENT '打卡状态：0未打卡 1打卡中 2打卡成功 3打卡失败',
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
```

---

### 3. 配置 Nacos

在 `dev` 命名空间下创建以下配置：

#### 3.1 `datasource-mysql.yaml`（group: `DATASOURCE_GROUP`）

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/campus_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 1234
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 3.2 `datasource-redis.yaml`（group: `DATASOURCE_GROUP`）

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

#### 3.3 `gateway-auth.yaml`（group: `GATEWAY_GROUP`）

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

#### 3.4 `common-log.yaml`（group: `DEFAULT_GROUP`）

```yaml
logging:
  file:
    name: logs/${spring.application.name}.log
  level:
    cloudstructuretemplate: info
  pattern:
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} [%X{traceId}] %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${PID:- } --- [%t] %-40.40logger{39} : [%X{traceId}] %msg%n"
  logback:
    rolling-policy:
      max-file-size: 10MB
      max-history: 7
```

---

### 4. 编译项目

#### 4.1 安装 Python 依赖

```powershell
cd campus-spider-service/scripts
pip install requests urllib3
```

#### 4.2 编译 Go 爬虫服务

```powershell
cd campus-spider-service
go mod tidy
go build -o server.exe ./cmd/server
```

#### 4.3 编译 Java 微服务

```powershell
cd campus-assistant
mvn clean install -DskipTests
```

> 注意：父模块 `pom.xml` 中已配置 `spring-boot-maven-plugin` 的 `<skip>true</skip>`，因此 `mvn clean install` 不会尝试启动服务，仅执行编译和打包。

---

### 5. 启动服务

按以下顺序启动：

| 顺序 | 服务 | 命令 |
|------|------|------|
| 1 | MySQL | `net start MySQL80` |
| 2 | Redis | 确认 Memurai/Redis 服务已运行 |
| 3 | Nacos | `C:\nacos\bin\startup.cmd -m standalone` |
| 4 | Gateway | `cd campus-assistant && mvn spring-boot:run -pl campusswu-gateway -am` |
| 5 | User-Service | `cd campus-assistant && mvn spring-boot:run -pl user-service -am` |
| 6 | Course-Service | `cd campus-assistant && mvn spring-boot:run -pl course-service -am` |
| 7 | Go 爬虫服务 | `cd campus-spider-service && $env:PYTHON_PATH="python"; .\server.exe` |
| 8 | 前端（可选） | `cd campus-web && npm install && npm run dev` |

> **重要**：Go 爬虫服务启动前必须设置 `PYTHON_PATH` 环境变量，指向正确的 Python 可执行文件路径（例如 `C:\Users\xxx\AppData\Local\Programs\Python\Python312\python.exe`），否则调用 Python 脚本时会失败。

---

### 6. 验证服务状态

所有服务启动后，检查以下端口是否监听：

```powershell
netstat -ano | findstr ":80 "
netstat -ano | findstr ":8000 "
netstat -ano | findstr ":9000 "
netstat -ano | findstr ":8082 "
```

| 端口 | 服务 |
|------|------|
| 80 | Gateway |
| 8000 | User-Service |
| 9000 | Course-Service |
| 8082 | Go 爬虫服务 |

---

### 7. 常见问题

#### Q1: Maven 编译报错 `Unable to find a suitable main class`

父模块 `pom.xml` 中已配置 `<skip>true</skip>`，无需处理。如果子模块报错，检查子模块 `pom.xml` 中 `spring-boot-maven-plugin` 是否配置了 `<skip>false</skip>`。

#### Q2: Gateway 启动后访问 `/gateway/course/**` 返回 500

Gateway 路由配置中需要为 `course-service` 单独添加路由规则，确保 `/gateway/course/**` 转发到 `campus-course-service`。参考 `campusswu-gateway/src/main/resources/application-gateway.yml`。

#### Q3: 注册成功但数据库没有课表和个人信息

检查 `SyncDataDTO` 的 `personalInfoDTO` 字段是否添加了 `@JsonProperty("personalInfo")` 注解，确保 Jackson 能正确反序列化 Go 回调的 JSON 字段。

#### Q4: Go 爬虫服务调用 Python 脚本失败

在 Windows PowerShell 中启动 Go 服务前，务必设置环境变量：

```powershell
$env:PYTHON_PATH="C:\Users\xxx\AppData\Local\Programs\Python\Python312\python.exe"
```

#### Q5: User-Service 调用 Go 爬虫服务超时或连接失败

检查 `SpiderServiceClient.java` 中的 `url` 配置是否为本地地址 `http://127.0.0.1:8082`，而非外网 `cpolar` 地址。
