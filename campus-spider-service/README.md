# campus-spider-service

Go 调度 + Python 爬虫的西南大学教务数据抓取微服务。

- Go 负责：HTTP 接口、任务调度、Redis Stream 多优先级队列、Worker 消费、死信队列、限流、僵尸恢复、回调 Java
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
│  ├─ model/task.go        # 数据模型（含优先级、重试字段）
│  ├─ store/
│  │  ├─ redis_store.go    # 多优先级 Redis Stream
│  │  ├─ idempotency.go    # X-Task-Id 幂等去重
│  │  ├─ ratelimiter.go    # 固定窗口全局限流
│  │  ├─ dlq.go            # 死信队列与指数退避重试
│  │  └─ zombie.go         # 僵尸消息恢复
│  ├─ worker/
│  │  ├─ worker.go         # Worker 消费池
│  │  └─ priority.go       # 加权轮询 + 防饥饿调度器
│  ├─ spider/
│  │  ├─ proxy_pool.go     # 轮询代理池
│  │  └─ runner.go         # 子进程调用 Python CLI
│  └─ client/java_client.go# 回调 Java 内部接口
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
| `JAVA_CALLBACK_URL` | `http://localhost:8000/internal/api/v1/sync/student-data` | Java 回调地址 |
| `JAVA_INTERNAL_TOKEN` | `''` | 回调 Java 时的 Bearer Token |
| `AES_SECRET_KEY` | `@aes-secret-key#` | Java 加密/Go 解密密码的 AES 密钥，生产环境请覆盖 |
| `PYTHON_PATH` | `python` | Python 可执行文件路径 |
| `SPIDER_SCRIPT` | `./scripts/spider_cli.py` | Python CLI 脚本路径 |
| `SESSION_DIR` | `./data/sessions` | Session 文件存储目录 |
| `SPIDER_TIMEOUT_MINUTES` | `20` | 单次爬虫超时（分钟） |
| `DEFAULT_ACADEMIC_YEAR` | `2025` | 默认学年 |
| `DEFAULT_SEMESTER` | `12` | 默认学期（12=第二学期，3=第一学期） |
| `PROXY_POOL` | `''` | 代理池，逗号分隔多个代理 |
| `PUNCH_CALLBACK_URL` | `http://localhost:8000/internal/api/v1/sync/punch-result` | 打卡回调地址 |
| `EMPTY_CLASSROOM_CALLBACK_URL` | `http://localhost:8000/internal/api/v1/sync/empty-classroom` | 空教室查询回调地址 |
| `GRADES_CALLBACK_URL` | `http://localhost:8000/internal/api/v1/sync/grades` | 成绩查询回调地址 |
| `YM_TOKEN` | `''` | 云打码平台 token，为空时服务无法启动 |
| `YM_TYPE` | `10110` | 云打码类型 ID |
| `PRIORITY_WEIGHTS` | `high:3,medium:2,low:1` | 三优先级队列加权轮询权重 |
| `QUEUE_STARVE_TIMEOUT_SECONDS` | `30` | 低优先级队列防饥饿超时（秒） |
| `IDEMPOTENCY_TTL_SECONDS` | `86400` | 幂等去重 key 过期时间（秒） |
| `DEAD_LETTER_STREAM` | `campus:spider:tasks:dlq` | 死信队列 Stream key |
| `DEAD_LETTER_SCAN_INTERVAL_SECONDS` | `30` | 死信队列扫描间隔（秒） |
| `MAX_RETRY_COUNT` | `5` | 任务最大重试次数 |
| `RETRY_BASE_DELAY_SECONDS` | `10` | 指数退避基数（秒） |
| `RETRY_MAX_DELAY_SECONDS` | `600` | 指数退避上限（秒） |
| `ZOMBIE_SCAN_INTERVAL_SECONDS` | `60` | 僵尸消息扫描间隔（秒） |
| `ZOMBIE_IDLE_TIMEOUT_SECONDS` | `300` | 消息 idle 多久视为僵尸（秒） |
| `RATE_LIMIT_KEY_PREFIX` | `campus:spider:rate_limit` | 限流 Redis key 前缀 |
| `RATE_LIMIT_PER_MINUTE` | `10` | 全局每分钟请求学校系统的次数上限 |

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
X-Task-Id: task-xxx
X-Priority: high
X-Student-Id: 222025321262104
X-Password: your_password
X-TYPE: FULL_CRAWL
Content-Type: application/json
```

通过 `X-TYPE` 区分模式：

| X-TYPE 值 | 说明 | 响应方式 |
|-----------|------|----------|
| `VERIFY` | 仅验证账号密码 | **同步**立即返回 |
| `FULL_CRAWL` | 爬取课表并回调 Java | **异步**入队，Worker 执行后回调 |

**Header 说明：**

| Header | 必填 | 说明 |
|--------|------|------|
| `X-Task-Id` | 否 | 任务唯一 ID；Go 会基于该 ID 做 24h 幂等去重，为空时自动生成 |
| `X-Priority` | 否 | 任务优先级：`high` / `medium` / `low`，非法值默认 `medium` |
| `X-Student-Id` | 是 | 学号 |
| `X-Password` | 是 | AES 加密后的密码 |
| `X-TYPE` | 是 | `VERIFY` 或 `FULL_CRAWL` |

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
  "message": "任务已提交",
  "data": {
    "taskId": "task-xxxx-xxxx-xxxx",
    "priority": "high"
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
  → 幂等校验 (X-Task-Id)
    → 按 X-Priority 写入对应 Redis Stream
      → Worker 按优先级加权轮询消费
        → 限流通过后才调用 Python
          → Python 登录 + 抓课表 + 输出 JSON
            → Go 回调 Java 落库接口
              → 失败则转入死信队列，按指数退避重试
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

### 3. 空教室查询任务

```http
POST /api/v1/task/empty-classroom
X-Task-Id: task-xxx
X-Priority: medium
X-Student-Id: 222025321262104
X-Password: your_password
Content-Type: application/json
```

请求体：
```json
{
  "academicYear": "2025",
  "semester": "12",
  "dayOfWeek": "1",
  "periodsMask": "16",
  "weeksMask": "262272",
  "campusId": "1",
  "building": "",
  "roomType": "",
  "callbackUrl": "http://your-java-service/empty-classroom-callback"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `academicYear` | string | 否 | 学年，如 `2025` |
| `semester` | string | 否 | 学期，`12`=第二学期，`3`=第一学期 |
| `dayOfWeek` | string | **是** | 星期几，`1`=周一，`7`=周日 |
| `periodsMask` | string | **是** | 节次掩码，如 `16` 表示第 5 节 |
| `weeksMask` | string | **是** | 周次掩码，如 `262272` 表示 1-16 周 |
| `campusId` | string | 否 | 校区号，默认 `1` |
| `building` | string | 否 | 楼号 |
| `roomType` | string | 否 | 教室类别 ID |
| `callbackUrl` | string | 否 | 覆盖默认空教室回调地址 |

**回调 Java 的数据格式：**

```json
{
  "studentId": "222025321262104",
  "academicYear": "2025",
  "semester": "12",
  "dayOfWeek": "1",
  "periodsMask": "16",
  "weeksMask": "262272",
  "campusId": "1",
  "building": "",
  "roomType": "",
  "classrooms": [
    {
      "building": "32教",
      "roomCode": "32-0505",
      "roomName": "32教505",
      "campus": "北碚校区",
      "capacity": "60",
      "realCapacity": "60",
      "roomType": "多媒体教室",
      "floor": "5",
      "remark": ""
    }
  ]
}
```

---

### 4. 成绩查询任务

```http
POST /api/v1/task/grades
X-Task-Id: task-xxx
X-Priority: medium
X-Student-Id: 222025321262104
X-Password: your_password
Content-Type: application/json
```

请求体：
```json
{
  "academicYear": "2025",
  "semester": "12",
  "callbackUrl": "http://your-java-service/grades-callback"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `academicYear` | string | 否 | 学年，如 `2025` |
| `semester` | string | 否 | 学期，`12`=第二学期，`3`=第一学期 |
| `callbackUrl` | string | 否 | 覆盖默认成绩回调地址 |

**回调 Java 的数据格式：**

```json
{
  "studentId": "222025321262104",
  "academicYear": "2025",
  "semester": "12",
  "grades": [
    {
      "courseName": "高等数学",
      "courseCode": "MATH101",
      "courseNature": "必修",
      "credit": "4.0",
      "score": "92",
      "gpa": "4.0",
      "teacher": "张老师",
      "examNature": "正常考试",
      "courseType": "公共基础课",
      "academicYear": "2025",
      "semester": "12"
    }
  ]
}
```

---

### 5. 自动打卡任务

```http
POST /api/v1/task/punch-card
X-Task-Id: task-xxx
X-Priority: low
X-Student-Id: 222025321262104
X-Password: your_password
Content-Type: application/json
```

该接口无需请求体。任务默认进入 `low` 优先级队列，由 Worker 在限流配额内择机执行打卡脚本，结果回调 `PUNCH_CALLBACK_URL`。

---

## 可靠性机制

### 1. 幂等去重

Java 下发任务时通过 `X-Task-Id` 指定唯一任务 ID。Go 使用 Redis `SET NX` 对该 ID 加锁，TTL 默认 24 小时，防止因网络超时/重试导致同一任务被重复入队。重复提交会返回 `200` 并提示“任务已提交（重复请求）”。

### 2. 多优先级队列

任务按 `X-Priority` 进入三个 Redis Stream：

| 优先级 | Stream key | 用途 |
|--------|-----------|------|
| `high` | `campus:spider:tasks:high` | 紧急任务，优先消费 |
| `medium` | `campus:spider:tasks:medium` | 默认优先级 |
| `low` | `campus:spider:tasks:low` | 可延后任务，最后消费 |

Worker 采用 **加权轮询**（默认 `high:3, medium:2, low:1`）消费；若某队列超过 `QUEUE_STARVE_TIMEOUT_SECONDS`（默认 30 秒）未被消费，则优先补偿该队列，防止低优先级任务饿死。

### 3. 全局限流

所有 Worker 共享一个固定窗口限流器，默认 **每分钟最多向学校系统发起 10 次请求**。限流在实际调用 Python 前生效，避免空队列 polling 浪费配额。窗口耗尽后 Worker 阻塞等待下一分钟窗口。

### 4. 死信队列与指数退避

任务执行失败（登录失败、爬虫异常、回调失败等）时，消息会被确认并从原队列移除，同时任务写入死信队列 `campus:spider:tasks:dlq`。

独立协程每 30 秒扫描死信队列：

- 超过 `MAX_RETRY_COUNT`（默认 5）的任务直接丢弃。
- 未达上限的任务按指数退避等待：`baseDelay * 2^(retryCount-1)`，上限 `RETRY_MAX_DELAY_SECONDS`（默认 600 秒）。
- 退避到期后按原优先级重新入队，RetryCount +1。

### 5. 僵尸消息恢复

Worker 崩溃或异常退出会导致消息长期处于 Pending 状态。独立协程每 60 秒扫描三个优先级队列的 Pending Entries List，认领 idle 超过 300 秒的消息并重新入队，避免任务永久卡住。

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

# 查询空教室（掩码整数）
python spider_cli.py \
  --mode empty-classroom \
  --student-id 222025321262104 \
  --password your_password \
  --xnm 2025 \
  --xqm 12 \
  --xqj 1 \
  --jcd 16 \
  --zcd 262272

# 查询空教室（文本自动转掩码）
python spider_cli.py \
  --mode empty-classroom \
  --student-id 222025321262104 \
  --password your_password \
  --xnm 2025 \
  --xqm 12 \
  --xqj 1 \
  --jcd-text "5-5" \
  --zcd-text "1-16"

# 查询成绩
python spider_cli.py \
  --mode grades \
  --student-id 222025321262104 \
  --password your_password \
  --xnm 2025 \
  --xqm 12
```

---

## 任务执行流程

```
queued → running → success / failed (→ dead_letter → retry) / discarded
```

- `queued`：已按优先级入队，等待 Worker 消费
- `running`：Worker 已消费，正在执行 Python 爬虫
- `success`：爬取成功且已回调 Java
- `failed`：执行失败，转入死信队列等待重试
- `dead_letter`：在死信队列中按指数退避等待重新入队
- `discarded`：超过最大重试次数，任务被丢弃

任务通过多优先级 Redis Stream 队列调度，Worker 按加权轮询消费；执行结果通过回调通知 Java 服务，失败任务由死信队列自动重试。

---

## 注意事项

1. **Session 复用**：同个学号的 Session 文件会保存在 `SESSION_DIR/session_{学号}.json`，有效期内不会重复登录。
2. **验证码**：默认使用云打码平台（`jfbym.com`），`YM_TOKEN` 为空时服务无法启动，请通过环境变量配置。
3. **代理池**：如需使用代理，设置环境变量 `PROXY_POOL=http://proxy1,http://proxy2`，Go 会轮询选取并透传给 Python。
4. **回调安全**：Go 回调 Java 时会在 Header 中携带 `Authorization: Bearer {JAVA_INTERNAL_TOKEN}`，Java 端需校验此 Token。
5. **多实例**：如需水平扩展，直接启动多个 `server.exe` 实例，共用同一个 Redis Stream，任务会自动负载均衡；全局限流器会保证所有实例合计不超过每分钟配额。
6. **幂等键**：任务入队成功后，24 小时内相同 `X-Task-Id` 会被视为重复提交；若入队失败，幂等锁会释放，允许 Java 立即重试。
