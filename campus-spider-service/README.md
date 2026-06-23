# campus-spider-service

Go 调度 + Python 爬虫的西南大学课表抓取微服务。

- Go 负责：HTTP 接口、任务调度、Redis Stream 队列、Worker 消费、回调 Java
- Python 负责：纯爬虫逻辑，作为命令行工具被 Go 通过子进程调用
- 云打码：自动识别验证码，无需人工干预

---

## 依赖环境

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| Go | >= 1.22 | 编译主服务 |
| Python | >= 3.8 | 运行爬虫脚本 |
| Node.js | 任意 | 执行 `des.js` 做 DES 加密 |
| Redis | >= 6.0 | 任务队列与状态存储 |

Python 依赖：
```bash
pip install requests urllib3
```

---

## 目录结构

```
campus-spider-service/
├─ cmd/server/
│  ├─ main.go              # HTTP 服务入口 + Worker 启动
│  └─ util.go              # JSON 工具函数
├─ internal/
│  ├─ config/config.go     # 环境变量配置
│  ├─ model/task.go        # 数据模型
│  ├─ store/redis_store.go # Redis Stream + Hash 存储
│  ├─ spider/
│  │  ├─ proxy_pool.go     # 轮询代理池
│  │  └─ runner.go         # 子进程调用 Python CLI
│  ├─ client/java_client.go# 回调 Java 内部接口
│  └─ worker/worker.go     # Redis Stream 消费者池
├─ scripts/
│  ├─ des.js               # DES 加密脚本
│  ├─ swu_kb.py            # 爬虫核心逻辑
│  └─ spider_cli.py        # Go 调用的命令行入口
└─ go.mod
```

---

## 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `HTTP_ADDR` | `:8082` | Go 服务监听地址 |
| `REDIS_ADDR` | `127.0.0.1:6379` | Redis 地址 |
| `REDIS_PASSWORD` | `''` | Redis 密码 |
| `REDIS_DB` | `0` | Redis 数据库 |
| `WORKER_CONCURRENCY` | `4` | Worker 并发数 |
| `JAVA_CALLBACK_URL` | `http://localhost:8081/internal/api/v1/sync/student-data` | Java 回调地址 |
| `JAVA_INTERNAL_TOKEN` | `internal-token` | 回调 Java 时的 Bearer Token |
| `PYTHON_PATH` | `python` | Python 可执行文件路径 |
| `SPIDER_SCRIPT` | `./scripts/spider_cli.py` | Python CLI 脚本路径 |
| `SESSION_DIR` | `./data/sessions` | Session 文件存储目录 |
| `SPIDER_TIMEOUT_MINUTES` | `20` | 单次爬虫超时（分钟） |
| `DEFAULT_ACADEMIC_YEAR` | `2025` | 默认学年 |
| `DEFAULT_SEMESTER` | `12` | 默认学期（12=上学期，3=下学期） |
| `PROXY_POOL` | `''` | 代理池，逗号分隔多个代理 |
| `YM_TOKEN` | *(内置)* | 云打码平台 token，建议通过环境变量覆盖 |
| `YM_TYPE` | `10110` | 云打码类型 ID |

---

## 编译与启动

### 1. 编译

```bash
cd campus-spider-service
go mod tidy
go build -o server.exe ./cmd/server
```

### 2. 启动 Redis

```bash
redis-server
```

### 3. 启动服务

```bash
# Windows
.\server.exe

# Linux / macOS
./server
```

---

## 测试流程

完整的联调测试流程请参考：[TESTING.md](./TESTING.md)

---

## HTTP 接口

### 1. 健康检查

```http
GET /health
```

**响应：**
```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "status": "ok"
  }
}
```

---

### 2. 统一任务提交接口

```http
POST /api/v1/task/submit
X-Student-Id: 222025321262104
X-Password: your_password
X-TYPE: FULL_CRAWL
Content-Type: application/json
```

通过 `X-TYPE` 区分两种模式：

| X-TYPE 值 | 说明 | 响应方式 |
|-----------|------|----------|
| `VERIFY` | 仅验证账号密码 | **同步**立即返回 |
| `FULL_CRAWL` | 爬取课表并回调 Java | **异步**入队，Worker 执行后回调 |

请求体可空，爬取模式下可指定学年和学期：
```json
{
  "academicYear": "2025",
  "semester": "12",
  "callbackUrl": "http://your-java-service/callback"
}
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `academicYear` | string | 否 | 学年，如 `2025` |
| `semester` | string | 否 | 学期，`12`=第二学期，`3`=第一学期 |
| `callbackUrl` | string | 否 | 覆盖默认的 Java 回调地址 |

---

#### 2.1 VERIFY 模式（验证账号密码）

**请求：**
```http
POST /api/v1/task/submit
X-Student-Id: 222025321262104
X-Password: your_password
X-TYPE: VERIFY
```

**成功响应：**
```json
{
  "code": 200,
  "message": "账号密码验证成功",
  "data": {
    "valid": true
  }
}
```

**失败响应：**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": {
    "valid": false
  }
}
```

---

#### 2.2 FULL_CRAWL 模式（爬取课表）

**请求：**
```http
POST /api/v1/task/submit
X-Student-Id: 222025321262104
X-Password: your_password
X-TYPE: FULL_CRAWL
```

**成功响应：**
```json
{
  "code": 200,
  "message": "爬虫任务已提交",
  "data": {
    "taskId": "task-xxxx-xxxx-xxxx"
  }
}
```

**失败响应：**
```json
{
  "code": 400,
  "message": "缺少 X-Student-Id 或 X-Password"
}
```

**调用链：**
```
Java 调用 Go /api/v1/task/submit (X-TYPE: FULL_CRAWL)
  → Go 写入 Redis Stream
    → Worker 消费
      → Go 调用 python spider_cli.py --mode crawl
        → Python 登录 + 抓课表 + 输出 JSON
          → Go 回调 Java 落库接口
```

**回调 Java 的数据格式：**

```json
{
  "studentId": "222025321262104",
  "academicYear": "2025",
  "semester": "12",
  "personalInfo": {
    "studentId": "222025321262104",
    "name": "姓名",
    "major": "专业",
    "className": "班级",
    "college": "学院"
  },
  "scheduleData": [
    {
      "courseName": "课程名",
      "teacher": "教师",
      "campus": "校区",
      "classroom": "教室",
      "dayOfWeek": 1,
      "periods": "1-2节",
      "weeks": "1-16周",
      "courseType": "regular"
    }
  ]
}
```

**scheduleData 字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `courseName` | string | 课程名称 |
| `teacher` | string | 教师姓名 |
| `campus` | string | 校区（可能为空，需根据教室号二次判断） |
| `classroom` | string | 教室号，如 `25-0608` |
| `dayOfWeek` | int | 星期几（1=周一，7=周日） |
| `periods` | string | 节次，如 `1-2节` |
| `weeks` | string | 周次，如 `1-16周` |
| `courseType` | string | `regular`=普通课，`practice`=实践课 |

---

## Python CLI 独立使用

如需单独调试爬虫，可直接运行：

```bash
cd scripts

# 爬课表
python spider_cli.py \
  --mode crawl \
  --student-id 222025321262104 \
  --password your_password \
  --xnm 2025 \
  --xqm 12

# 仅验证登录
python spider_cli.py \
  --mode validate \
  --student-id 222025321262104 \
  --password your_password
```

---

## 任务执行流程

```
queued → running → success / failed / callback_failed
```

- `queued`：已入队，等待 Worker 消费
- `running`：Worker 正在执行 Python 爬虫
- `success`：爬取成功且已回调 Java
- `failed`：爬虫执行失败（如登录失败、课表接口异常）
- `callback_failed`：爬取成功但回调 Java 失败（已重试 3 次）

任务通过 Redis Stream 队列调度，Worker 消费后执行，执行结果通过回调通知 Java 服务。

---

## 注意事项

1. **Session 复用**：同个学号的 Session 文件会保存在 `SESSION_DIR/session_{学号}.json`，有效期内不会重复登录。
2. **验证码**：默认使用云打码平台（`jfbym.com`），token 建议通过环境变量 `YM_TOKEN` 配置，避免硬编码。
3. **代理池**：如需使用代理，设置环境变量 `PROXY_POOL=http://proxy1,http://proxy2`，Go 会轮询选取并透传给 Python。
4. **回调安全**：Go 回调 Java 时会在 Header 中携带 `Authorization: Bearer {JAVA_INTERNAL_TOKEN}`，Java 端需校验此 Token。
5. **多实例**：如需水平扩展，直接启动多个 `server.exe` 实例，共用同一个 Redis Stream，任务会自动负载均衡。
