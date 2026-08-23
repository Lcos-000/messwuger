# 校园助手系统

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white">
  <img alt="Go" src="https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white">
  <img alt="Python" src="https://img.shields.io/badge/Python-3.8+-3776AB?logo=python&logoColor=white">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white">
  <img alt="Redis" src="https://img.shields.io/badge/Redis-5.0-DC382D?logo=redis&logoColor=white">
  <br>
  <img alt="GitHub stars" src="https://img.shields.io/github/stars/Lcos-000/messwuger?style=social">
  <img alt="GitHub forks" src="https://img.shields.io/github/forks/Lcos-000/messwuger?style=social">
</p>

本项目由 4 个主要部分组成：

- `campus-assistant`：Java 微服务后端
- `campus-spider-service`：Go + Python 教务爬虫与异步任务执行服务
- `campus-web`：Vue 3 Web 前端
- `campus-android`：Kotlin + Jetpack Compose 安卓原生客户端

当前主链路已经覆盖登录、课表、成绩、空教室、个性化主页、图片资源、自定义服务器地址、公共公告与公共配置拉取等功能。

---

## 模块说明

| 模块 | 技术栈 | 职责 |
|------|--------|------|
| `campus-assistant/campusswu-gateway` | Spring Cloud Gateway | 统一入口、JWT 鉴权、公共配置与公共公告接口、路由转发 |
| `campus-assistant/user-service` | Spring Boot 3 + MyBatis Plus | 用户注册/登录、状态管理、个人信息、个性化配置、自定义资源管理、异步任务发起 |
| `campus-assistant/course-service` | Spring Boot 3 + MyBatis Plus | 课表存储与查询、成绩存储与查询 |
| `campus-spider-service` | Go 1.24 + Python 3 | 教务系统登录、课表抓取、成绩抓取、空教室抓取、打卡任务调度 |
| `campus-web` | Vue 3 + Vite + Axios | Web 管理与开发验证前端 |
| `campus-android` | Kotlin + Compose + Retrofit + DataStore | 安卓用户端，支持动态服务器设置、公告、使用手册、课表/成绩/空教室/个人主页 |
| 基础设施 | MySQL、Redis、Nacos、OSS、SkyWalking | 持久化、缓存、注册发现与配置中心、对象存储、链路追踪 |

---

## 当前已落地功能

### 后端 / 数据侧

- 用户注册、登录、退出、刷新同步、注销账号
- 课表数据抓取与查询
- 成绩任务发起、异步回调、成绩落库与查询
- 空教室任务发起、异步回调、结果缓存与查询
- 自动打卡开关持久化
- 用户个性化主页配置保存
- 用户自定义头像、顶部背景、墙纸上传与地址持久化
- 公共公告接口
- 公共使用手册接口
- 公共学期开学时间配置接口
- 阿里云 OSS 上传接入

### Web 前端

- 登录页、课表页、成绩页、空教室页、个人主页、管理员页
- 默认资源 / 自定义资源切换与上传
- 成绩查询、排序、表格视图
- 空教室条件选择与结果表格
- 管理员资源入口与日志查看

### Android 前端

- Compose 原生登录与主页面四 Tab
- Token 持久化与自动恢复登录
- 课表、成绩、空教室、我的页面主链路
- 个人主页与个性化设置
- 默认资源 / 自定义资源选择与上传
- 动态服务器设置（IP/域名 + 端口）
- 公告入口、历史公告、本地缓存
- 公共使用手册与学期开学配置拉取
- 全局统一浅色主题与公共错误处理

---

## 环境依赖

| 组件 | 版本要求 |
|------|---------|
| JDK | 17（后端） / 21（Android 构建） |
| Maven | 3.8+ |
| Go | 1.24 |
| Python | 3.8+ |
| Node.js | 18+ |
| npm | 9+ |
| Android Studio | 近期稳定版 |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Nacos | 2.x |
| Docker Desktop / Docker Engine | 当前环境可用 |
| Docker Compose | v2 |

---

## Nacos 关键配置

除了数据库、Redis、日志和 OSS 配置外，当前还需要以下公共客户端配置：

### `publicclient-config.yaml`（group: `DEFAULT_GROUP`）

```yaml
campus:
  notice:
    enabled: true
    version: 1
    title: 系统公告
    content: |
      当前服务地址如有变更，请先在登录页或“我的”页修改服务器设置。
    level: info
    updatedAt: 2026-08-07 18:00:00

  manual:
    version: 1
    title: 使用手册
    content: |
      1. 首次使用请先配置服务器地址。
      2. 登录后可进入课表、成绩、空教室和个人主页。

  schedule:
    springStartDate: 2026-03-02
    autumnStartDate: 2026-09-01
    maxWeek: 20
```

当前安卓端对应公共接口：

- `GET /api/public/notice`
- `GET /api/public/manual`
- `GET /api/public/schedule-config`

Web 端如需继续透传，也建议使用相同网关来源。

---

## 本地启动顺序

### 后端与基础设施

| 顺序 | 服务 | 命令 |
|------|------|------|
| 1 | MySQL / Redis / Nacos / Sentinel | `cd deploy/offline/package && docker compose up -d mysql redis nacos sentinel` |
| 2 | Gateway | `cd campus-assistant && mvn spring-boot:run -pl campusswu-gateway -am` |
| 3 | User-Service | `cd campus-assistant && mvn spring-boot:run -pl user-service -am` |
| 4 | Course-Service | `cd campus-assistant && mvn spring-boot:run -pl course-service -am` |
| 5 | Go 爬虫服务 | `cd campus-spider-service && $env:PYTHON_PATH="python"; .\server.exe` |
| 6 | Web 开发服务 | `cd campus-web && npm run dev` |

### Android 调试

安卓项目位于 `campus-android/`，首次调试建议：

1. 打开 Android Studio，选择 `campus-android` 目录。
2. 等待 Gradle Sync 完成。
3. 启动模拟器或连接真机。
4. 运行 `app`。
5. 在登录页通过“服务器设置”填写当前后端地址。

当前安卓端默认请求模型：

- API 基础前缀：`http://<host>:<port>/api/`
- 静态资源基础前缀：`http://<host>:<port>/`

也就是说，如果网关已统一暴露为 `/api/...`，安卓端无需再手动拼 `/gateway`。

更完整的 Android 说明见 [campus-android/README.md](campus-android/README.md)。

---

## 接口与资源说明

### 常用网关接口

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`
- `GET /api/public/notice`
- `GET /api/public/manual`
- `GET /api/public/schedule-config`
- `GET /api/user/status`
- `GET /api/user/personal`
- `GET /api/user/schedule/get`
- `GET /api/user/grades`
- `POST /api/user/grades/task`
- `POST /api/user/empty-classroom/task`
- `POST /api/user/empty-classroom/result`
- `GET /api/personalization/get-profile`
- `PUT /api/personalization/update-profile`
- `GET /api/personalization/get-default-options`
- `GET /api/personalization/get-custom-assets`
- `POST /api/personalization/upload-custom-asset`

### 静态资源

若后端返回默认资源相对路径，安卓端会按当前服务器设置拼接静态资源前缀加载。建议后端最终逐步切换为完整 OSS URL，以减少前端路径判断复杂度。

---

## 部署与测试文档

- 部署说明：[DEPLOYMENT.md](DEPLOYMENT.md)
- 测试说明：[TESTING.md](TESTING.md)
- 安卓端说明：[campus-android/README.md](campus-android/README.md)

---

## Git 与本地文件约束

仓库当前已补充忽略规则，以下内容不应继续入库：

- `campus-web/node_modules/`
- 任意模块下的 `.gradle/`
- `local.properties`
- Android `build/`、`captures/`、`.kotlin/`、`.cxx/`
- 本地 IDE 配置与临时构建产物

如果这些目录之前已经被 Git 追踪，仅修改 `.gitignore` 不会自动取消追踪，需要额外执行一次 `git rm --cached`。

---

## 说明

当前仓库同时承载后端、爬虫、Web 与 Android 客户端。后续如继续迭代公共配置、公告、开学时间等动态能力，建议统一经网关对外暴露，并由客户端各自做本地缓存与默认值回退。
