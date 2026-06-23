# 校园助手系统 - 完整测试指南

本文档提供从启动到验证完整链路的详细测试步骤。

---

## 前置检查

确保以下服务已启动：

```powershell
# MySQL
net start MySQL80

# Redis（Memurai 已作为服务运行，或手动启动 redis-server）

# Nacos
cd C:\nacos\bin
startup.cmd -m standalone
```

---

## 清空数据库（可选）

```powershell
mysql -u root -p1234 -e "USE campus_db; TRUNCATE TABLE course_db; TRUNCATE TABLE personal_info; TRUNCATE TABLE student_db;"
```

---

## 一键启动所有服务

在 **5 个独立的 PowerShell 窗口** 中分别执行：

### 窗口 1：Gateway（端口 80）

```powershell
cd campus-assistant
mvn spring-boot:run -pl campusswu-gateway -am
```

### 窗口 2：User-Service（端口 8000）

```powershell
cd campus-assistant
mvn spring-boot:run -pl user-service -am
```

### 窗口 3：Course-Service（端口 9000）

```powershell
cd campus-assistant
mvn spring-boot:run -pl course-service -am
```

### 窗口 4：Go 爬虫服务（端口 8082）

```powershell
cd campus-spider-service
$env:PYTHON_PATH="python"
.\server.exe
```

### 窗口 5：前端（可选，端口 5173）

```powershell
cd campus-web
npm install
npm run dev
```

> 如果 `python` 不在 PATH 中，将 `PYTHON_PATH` 替换为完整路径，例如 `C:\Users\xxx\AppData\Local\Programs\Python\Python312\python.exe`。

---

## 端口确认

所有服务启动后，执行：

```powershell
netstat -ano | findstr ":80 "
netstat -ano | findstr ":8000 "
netstat -ano | findstr ":9000 "
netstat -ano | findstr ":8082 "
```

应看到 4 个 `LISTENING` 状态。

---

## 完整接口测试流程

> PowerShell 如果中文乱码，先执行：
> ```powershell
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
> $OutputEncoding = [System.Text.Encoding]::UTF8
> chcp 65001
> ```
>
> 修改 `campus-spider-service` 后需要重新编译并重启 Go 服务：
> ```powershell
> cd campus-spider-service
> go build -o server.exe .\cmd\server
> .\server.exe
> ```

### 1. 注册（触发爬虫同步）

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/register" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"
```

**期望结果**：
- `code`: `200`
- 约 10~30 秒后，Go 爬虫完成登录、抓取、回调 Java 服务

### 2. 登录

```powershell
$login = Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/login" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"
$token = $login.data
Write-Host "Token: $token"
```

**期望结果**：
- `code`: `200`
- `data`: 返回 JWT Token 字符串

### 3. 查询用户信息

```powershell
$headers = @{"Authorization"=$token}
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/search" `
  -Method GET `
  -Headers $headers
```

**期望结果**：
- `code`: `200`
- `data` 包含 `studentId`、`name`、`major`、`className`、`syncStatus`

### 4. 查询课表

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/course/get" `
  -Method GET `
  -Headers $headers
```

**期望结果**：
- `code`: `200`
- `data` 包含 `studentId`、`academicYear`、`semester`、`scheduleJson`

### 5. 刷新数据（手动触发爬虫同步）

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/refresh" `
  -Method POST `
  -Headers $headers
```

**期望结果**：
- `code`: `200`
- `data`: "刷新任务已提交，后台正在同步数据"

### 6. 注销用户

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/delete" `
  -Method DELETE `
  -Headers $headers
```

**期望结果**：
- `code`: `204`
- 用户数据被删除

---

## 数据库验证

```powershell
mysql -u root -p1234 -e "
USE campus_db;
SELECT student_id, sync_status, punch_status FROM student_db WHERE student_id='YOUR_STUDENT_ID';
SELECT COUNT(*) AS personal_info_count FROM personal_info WHERE student_id='YOUR_STUDENT_ID';
SELECT COUNT(*) AS course_count FROM course_db WHERE student_id='YOUR_STUDENT_ID';
"
```

**期望结果**：

| 表 | 期望 |
|---|---|
| `student_db.sync_status` | `2`（同步成功）|
| `student_db.punch_status` | `0-3`（打卡状态）|
| `personal_info_count` | `1` |
| `course_count` | `1` |

---

## 打卡功能测试

### 手动触发打卡
# 直接生成加密密码并调用接口（一行搞定）

$encryptedPwd = (python -c "from Crypto.Cipher import AES; from Crypto.Util.Padding import pad; import base64; key = b'YOUR_AES_SECRET_KEY'; iv = key[:16]; cipher = AES.new(key, AES.MODE_CBC, iv); pwd = b'YOUR_PUNCH_PASSWORD'; enc = base64.b64encode(cipher.encrypt(pad(pwd, 16))).decode(); print(enc, end='')")

# 调用打卡接口
Invoke-RestMethod -Uri "http://127.0.0.1:8082/api/v1/task/punch-card" `
  -Method POST `
  -Headers @{
    "X-Student-Id" = "YOUR_STUDENT_ID"
    "X-Password" = $encryptedPwd
    "X-TYPE" = "PUNCH_CARD"
  } `
  -ContentType "application/json"


3. 等待 5 秒后验证数据库：

```powershell
mysql -u root -p1234 -e "USE campus_db; SELECT student_id, punch_status FROM student_db WHERE student_id='YOUR_STUDENT_ID';"
```

**期望结果**：`punch_status = 2`（打卡成功）

---

## 全部通过 = 项目跑通

如果以上接口均返回 `200`，且数据库 3 张表均有数据，则整个链路已完全跑通。

---

## 常见问题

### Q1: Course-Service 返回 503

**原因**：Course-Service 的 Nacos 服务发现被禁用。

**解决**：修改 `course-service/src/main/resources/application-nacos.yml`，将 `discovery.enabled` 从 `false` 改为 `true`。

### Q2: 打卡功能无法触发

**原因**：项目没有提供手动打卡 API，打卡功能由定时任务自动触发（每天 21:00 首轮，22:30 补偿）。

**解决**：可以直接调用 Go 端打卡接口进行测试（见上方"打卡功能测试"章节）。

### Q3: 新用户 punch_status 为 NULL

**原因**：注册时未初始化 `punchStatus` 字段。

**解决**：在 `UserWriteSupport.addUser()` 方法中添加 `userEntity.setPunchStatus(PunchStatusEnum.NOT_PUNCHED.getCode())`。
