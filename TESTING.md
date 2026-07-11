# 校园助手系统测试指南

本文档用于验证当前项目的核心链路是否可用，重点覆盖后端服务、爬虫服务，以及与当前版本对齐的个性化配置、自定义图片资源、成绩查询、空教室查询、管理员资源和自动打卡能力。

---

## 测试范围

当前文档覆盖以下内容：

- 基础服务启动检查
- 注册 / 登录 / 同步 / 查询课表
- 成绩任务提交、回调、落库与查询
- 空教室任务提交、回调与结果查询
- 个性化主页接口
- 自定义图片上传与回显接口
- 自动打卡开关接口
- 管理员登录与资源接口
- 管理员日志初始化、历史加载与下载能力
- SkyWalking 独立界面检查
- 前端构建验证
- 前端手工回归检查
- 数据库落库检查

> 当前仓库未配置前端单元测试或 E2E 自动化测试，因此前端主要通过 `npm run build` + 手工回归完成验证。

---

## 前置准备

确保以下依赖已启动：

```powershell
cd deploy
docker compose -p campusassistant -f docker-compose.middleware.yml up -d
```

如需验证链路追踪独立界面，再额外启动：

```powershell
cd deploy
docker compose -p campusassistant -f docker-compose.skywalking.yml up -d
```

---

## 编译检查

### Java

```powershell
cd campus-assistant
mvn clean install -DskipTests
```

### Go

```powershell
cd ..\campus-spider-service
go build -o server.exe .\cmd\server
```

### Frontend

```powershell
cd ..\campus-web
npm install
npm run build
```

**期望结果**：

- 三端均可编译成功
- `campus-web` 的 `vite build` 成功输出 `dist`

---

## 启动所有服务

建议使用 5 个独立 PowerShell 窗口。

### 窗口 1：Gateway

```powershell
cd campus-assistant
mvn spring-boot:run -pl campusswu-gateway -am
```

### 窗口 2：User-Service

```powershell
cd campus-assistant
mvn spring-boot:run -pl user-service -am
```

### 窗口 3：Course-Service

```powershell
cd campus-assistant
mvn spring-boot:run -pl course-service -am
```

### 窗口 4：Go 爬虫服务

```powershell
cd campus-spider-service
$env:PYTHON_PATH="python"
$env:YM_TOKEN="你的云打码token"
go build -o server.exe ./cmd/server
.\server.exe
```

如需手动覆盖回调地址或调整可靠性参数，可额外设置：

```powershell
$env:JAVA_CALLBACK_URL="http://127.0.0.1:8000/internal/api/v1/sync/student-data"
$env:PUNCH_CALLBACK_URL="http://127.0.0.1:8000/internal/api/v1/sync/punch-result"
$env:EMPTY_CLASSROOM_CALLBACK_URL="http://127.0.0.1:8000/internal/api/v1/sync/empty-classroom"
$env:GRADES_CALLBACK_URL="http://127.0.0.1:8000/internal/api/v1/sync/grades"
$env:RATE_LIMIT_PER_MINUTE="10"
$env:MAX_RETRY_COUNT="5"
```

### 窗口 5：前端开发服务

```powershell
cd campus-web
npm run dev
```

---

## 端口确认

```powershell
netstat -ano | findstr ":80 "
netstat -ano | findstr ":8000 "
netstat -ano | findstr ":9000 "
netstat -ano | findstr ":8082 "
netstat -ano | findstr ":8848 "
netstat -ano | findstr ":18080 "
netstat -ano | findstr ":5173 "
```

**期望结果**：上述端口均处于 `LISTENING`。

---

## 接口联调流程

### 1. 注册

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/register" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"
```

### 2. 登录

```powershell
$login = Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/login" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"

$token = $login.data
$headers = @{ Authorization = "Bearer $token" }
```

### 3. 查询用户状态

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/status" `
  -Method GET `
  -Headers $headers
```

### 4. 查询课表

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/schedule/get" `
  -Method GET `
  -Headers $headers
```

---

## 成绩链路测试

### 1. 提交成绩任务

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/user/grades/task" `
  -Method POST `
  -Headers $headers `
  -Body '{"academicYear":"2025","semester":"12"}' `
  -ContentType "application/json"
```

### 2. 查询成绩结果

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/user/grades/result" `
  -Method POST `
  -Headers $headers `
  -Body '{"academicYear":"2025","semester":"12"}' `
  -ContentType "application/json"
```

### 3. 数据库检查

```powershell
mysql -u root -p1234 -e "USE campus_db; SELECT student_id, academic_year, semester, course_name, score FROM student_grade WHERE student_id='YOUR_STUDENT_ID' ORDER BY update_time DESC LIMIT 10;"
```

---

## 空教室链路测试

### 1. 提交空教室任务

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/user/empty-classroom/task" `
  -Method POST `
  -Headers $headers `
  -Body '{"academicYear":"2025","semester":"12","dayOfWeek":"1","periodsMask":"16","weeksMask":"2","campusId":"2","building":"08","roomType":""}' `
  -ContentType "application/json"
```

### 2. 查询空教室结果

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/user/empty-classroom/result" `
  -Method POST `
  -Headers $headers `
  -Body '{"academicYear":"2025","semester":"12","dayOfWeek":"1","periodsMask":"16","weeksMask":"2","campusId":"2","building":"08","roomType":""}' `
  -ContentType "application/json"
```

**期望结果**：

- 提交接口返回 `SUBMITTED` / `QUERYING` / `RESULT_READY`
- 结果接口在回调成功后返回 `classrooms` 数组
- 若长时间为空，优先检查 Go 回调是否完整回传 `campusId`、`building`、`roomType`

---

## Go 爬虫服务新特性测试

以下测试用于验证 campus-spider-service 新增的幂等、优先级队列、死信队列、限流与僵尸恢复能力。

### 1. 前置准备

启动 Redis 后，在 PowerShell 中启动 Go 服务：

```powershell
cd campus-spider-service
$env:PYTHON_PATH="python"
$env:YM_TOKEN="你的云打码token"
go build -o server.exe ./cmd/server
.\server.exe
```

### 2. 幂等去重测试

使用相同的 `X-Task-Id` 连续提交两次：

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/submit" `
  -Method POST `
  -Headers @{
    "X-Task-Id" = "test-idempotency-001"
    "X-Priority" = "high"
    "X-Student-Id" = "222025321262104"
    "X-Password" = "AES加密后的密码"
    "X-TYPE" = "FULL_CRAWL"
  }
```

**期望结果**：
- 第一次返回 `任务已提交`。
- 24 小时内再次提交相同 `X-Task-Id`，返回 `任务已提交（重复请求）`。

### 3. 优先级队列测试

向三个优先级队列各提交一个任务，观察消费顺序：

```powershell
# low
Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/submit" -Method POST -Headers @{
  "X-Task-Id"="test-low-001"; "X-Priority"="low"; "X-Student-Id"="222025321262104"; "X-Password"="..."; "X-TYPE"="FULL_CRAWL"
}

# medium
Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/submit" -Method POST -Headers @{
  "X-Task-Id"="test-medium-001"; "X-Priority"="medium"; "X-Student-Id"="222025321262104"; "X-Password"="..."; "X-TYPE"="FULL_CRAWL"
}

# high
Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/submit" -Method POST -Headers @{
  "X-Task-Id"="test-high-001"; "X-Priority"="high"; "X-Student-Id"="222025321262104"; "X-Password"="..."; "X-TYPE"="FULL_CRAWL"
}
```

**期望结果**：在队列均有任务时，`high` 优先被消费；持续只向 `high` 入队时，`medium`/`low` 超过 30 秒未消费会触发防饥饿补偿。

### 4. 死信队列与重试测试

提交一个注定失败的任务（如错误密码）：

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/submit" -Method POST -Headers @{
  "X-Task-Id"="test-dlq-001"; "X-Priority"="high"; "X-Student-Id"="222025321262104"; "X-Password"="错误的密码"; "X-TYPE"="FULL_CRAWL"
}
```

**验证方式**：

```powershell
# 查看死信队列长度（需要 redis-cli）
redis-cli XLEN campus:spider:tasks:dlq

# 查看任务在死信队列中的字段
redis-cli XRANGE campus:spider:tasks:dlq - + COUNT 1
```

**期望结果**：
- 任务失败后进入 `campus:spider:tasks:dlq`。
- `RetryCount` 从 1 开始递增。
- 按指数退避（10s、20s、40s... 上限 600s）后重新入队原优先级。
- 超过 `MAX_RETRY_COUNT`（默认 5）后从死信队列移除并丢弃。

### 5. 限流测试

快速连续提交 20 个任务：

```powershell
for ($i = 0; $i -lt 20; $i++) {
  Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/submit" -Method POST -Headers @{
    "X-Task-Id"="test-rate-$i"; "X-Priority"="high"; "X-Student-Id"="222025321262104"; "X-Password"="..."; "X-TYPE"="FULL_CRAWL"
  }
}
```

**验证方式**：

```powershell
# 观察日志应出现类似输出
# [Worker] 触发限流，等待下一窗口 45s
```

**期望结果**：
- 所有任务成功入队。
- Worker 实际调用 Python 的频率不超过每分钟 10 次。
- 超出配额的任务在 Redis Stream 中 pending，等待下一窗口。

### 6. 僵尸消息恢复测试

模拟 Worker 崩溃：

1. 提交一个任务。
2. 在 Worker 消费该任务但尚未确认时，强制终止 `server.exe`。
3. 等待 5 分钟后重新启动服务。

**验证方式**：

```powershell
redis-cli XPENDING campus:spider:tasks:high campus-spider-workers - + 10
```

**期望结果**：
- 任务在 5 分钟后被僵尸恢复协程认领并重新入队。
- 重新启动的 Worker 会再次消费该任务。

---

## 个性化主页接口测试

### 1. 获取个性化配置

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/get-profile" `
  -Method GET `
  -Headers $headers
```

### 2. 更新个性化配置

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/update-profile" `
  -Method PUT `
  -Headers $headers `
  -Body '{"cardOpacity":0.72,"cardBlur":8,"wallpaperMask":0.65,"globalFontEnabled":1}' `
  -ContentType "application/json"
```

---

## 自动打卡开关接口测试

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/user/auto-punch" `
  -Method PUT `
  -Headers $headers `
  -Body '{"autoPunchEnabled":1}' `
  -ContentType "application/json"
```

---

## 管理员接口测试

### 1. 管理员登录

```powershell
$adminLogin = Invoke-RestMethod -Uri "http://127.0.0.1/gateway/admin/login" `
  -Method POST `
  -Body '{"studentId":"YOUR_ADMIN_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"

$adminToken = $adminLogin.data
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
```

### 2. 获取管理员资源

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/admin/resources" `
  -Method GET `
  -Headers $adminHeaders
```

---

## 数据库验证

```powershell
mysql -u root -p1234 -e "
USE campus_db;
SELECT student_id, sync_status, punch_status, auto_punch_enabled FROM student_db WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, card_opacity, card_blur, wallpaper_mask, global_font_enabled FROM user_profile_style WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, custom_avatar, custom_background, custom_wallpaper FROM user_profile_custom_asset WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, academic_year, semester, course_name, score FROM student_grade WHERE student_id='YOUR_STUDENT_ID' ORDER BY update_time DESC LIMIT 10;
"
```

---

## 前端手工回归清单

浏览器打开 `http://localhost:5173`，登录后重点检查以下页面：

### 1. Profile 页

- 资料卡透明度、模糊度、墙纸蒙版、全局字体切换正常
- 自定义头像 / 顶部背景 / 墙纸上传、裁剪、回显正常
- 自动打卡开关切换正常

### 2. Grades 页

- 学年 / 学期切换正常
- 成绩查询按钮可正常触发请求
- 表格视图正常显示成绩列表
- 排序切换正常
- 墙纸背景与个人主页一致

### 3. EmptyClassroom 页

- 学年 / 学期 / 星期 / 周次 / 节次 / 校区 / 楼栋条件可正常选择
- 楼栋默认值为当前校区第一个有效选项
- 查询结果按钮可正常触发请求
- 表格结果可正常展示
- 本地缓存能回填上次条件

### 4. Admin 页

- 管理员登录成功后进入 `/admin`
- 用户页与管理员页可在同一浏览器同时保持登录，不应互相顶掉 token
- 日志列表和资源列表正常显示

---

## 已知说明

### 1. 前端测试方式

当前前端没有现成的：

- `vitest`
- `jest`
- `cypress`
- `playwright`

因此前端目前以以下方式验收：

- `npm run build`
- 浏览器手工回归
- 接口手工联调

### 2. 401 处理

前端不仅处理 HTTP 401，也处理响应体 `code = 401`。

### 3. OSS 历史对象

当前上传成功后只会覆盖数据库记录，不会自动删除旧 OSS 对象；这不影响功能验证，但测试结束后如需控量仍需手动清理或补后台删除逻辑。

### 4. 成绩 / 空教室联调注意点

- Go 回调改动后需要重新编译 `server.exe`
- 如果提交成功但结果一直为空，优先确认当前运行的不是旧二进制
- 空教室查询如果使用了条件指纹缓存，提交参数与回调参数必须完全一致

### 5. Go 爬虫服务新特性联调注意点

- 提交任务时建议带上 `X-Task-Id`，便于 Java 侧做幂等控制和后续追踪。
- `X-Priority` 支持 `high` / `medium` / `low`，非法值会默认按 `medium` 处理。
- 若测试环境希望快速看到重试效果，可降低 `RETRY_BASE_DELAY_SECONDS` 和 `DEAD_LETTER_SCAN_INTERVAL_SECONDS`。
- 限流配额为全局每分钟 10 次，压测时如需提高并发，请调大 `RATE_LIMIT_PER_MINUTE`。
