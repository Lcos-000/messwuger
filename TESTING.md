# 校园助手系统测试指南

本文档用于验证当前项目的核心链路是否可用，重点覆盖后端服务、爬虫服务，以及与当前版本对齐的个性化配置、自定义图片资源、管理员资源和自动打卡能力。

---

## 测试范围

当前文档覆盖以下内容：

- 基础服务启动检查
- 注册 / 登录 / 同步 / 查询课表
- 个性化主页接口
- 自定义图片上传与回显接口
- 自动打卡开关接口
- 管理员登录与资源接口
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

## 可选：清空测试数据

```powershell
mysql -u root -p1234 -e "USE campus_db; TRUNCATE TABLE user_profile_custom_asset; TRUNCATE TABLE user_profile_style; TRUNCATE TABLE course_db; TRUNCATE TABLE personal_info; TRUNCATE TABLE student_db;"
```

若要重置独立 SkyWalking 数据：

```powershell
cd deploy
docker compose -p campusassistant -f docker-compose.skywalking.yml down -v
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
.\server.exe
```

### 窗口 5：前端开发服务

```powershell
cd campus-web
npm run dev
```

> 如需链路追踪联调，确保 Java 进程实际带有 SkyWalking Agent，并且 Agent 上报地址为 `127.0.0.1:11810`。

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

补充说明：当前前端已对部分远程错误码做统一分类提示。联调时如遇失败，可额外关注：

- `531~537`：同步/打卡服务异常
- `541~547`：课表服务异常
- `429` / `503`：限流、降级或系统繁忙

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

### 3. 获取自定义图片资源

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/get-custom-assets" `
  -Method GET `
  -Headers $headers
```

### 4. 上传自定义图片

```powershell
curl.exe -X POST "http://127.0.0.1:8000/personalization/upload-custom-asset" ^
  -H "Authorization: Bearer $token" ^
  -F "type=background" ^
  -F "file=@C:\tmp\profile-bg.jpg"
```

---

## 自动打卡开关接口测试

### 1. 关闭自动打卡

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/user/auto-punch" `
  -Method PUT `
  -Headers $headers `
  -Body '{"autoPunchEnabled":0}' `
  -ContentType "application/json"
```

### 2. 打开自动打卡

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

**期望结果**：

- `code = 200`
- `data.items` 至少包含 `name` 和 `url`
- 当前示例应包含 `SkyWalking`、`Nacos`、`Sentinel`

---

## SkyWalking 独立界面测试
联调前确认 Java 服务采用统一探针方案：VM options 只保留 `-javaagent`，服务名通过 `SW_AGENT_NAME` 提供。

### 1. 打开新 UI

浏览器访问：

```text
http://127.0.0.1:18080
```

### 2. 验证隔离效果

检查点：

- 只应看到当前这套新 OAP 收到的数据
- 若仍出现旧项目服务，说明 Java Agent 仍在向旧 OAP 上报
- 若页面为空，优先检查 Java 进程是否真的挂载了 Agent，且 `collector.backend_service` 是否已改为 `127.0.0.1:11810`
- 若服务名不对，优先检查对应 Run Configuration 的 `SW_AGENT_NAME` 是否填写正确

---

## 数据库验证

```powershell
mysql -u root -p1234 -e "
USE campus_db;
SELECT student_id, sync_status, punch_status, auto_punch_enabled FROM student_db WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, card_opacity, card_blur, wallpaper_mask, global_font_enabled FROM user_profile_style WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, custom_avatar, custom_background, custom_wallpaper FROM user_profile_custom_asset WHERE student_id='YOUR_STUDENT_ID';
"
```

---

## 前端手工回归清单

浏览器打开 `http://localhost:5173`，登录后重点检查 `Profile` 页和管理员页。

### 1. Profile 页

- 资料卡透明度、模糊度、墙纸蒙版、全局字体切换正常
- 自定义头像 / 顶部背景 / 墙纸上传、裁剪、回显正常
- 自动打卡开关切换正常

### 2. Admin 页

- 输入学号后缀 `/admin` 仍使用普通登录界面
- 管理员登录成功后进入 `/admin`
- 资源列表按单列展示
- 链接显示为 `Nacos：http://...` 形式
- 悬浮时链接有明显 hover 反馈

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

前端不仅处理 HTTP 401，也处理响应体 `code = 401`。这点在联调时需要特别注意。

### 3. OSS 历史对象

当前上传成功后只会覆盖数据库记录，不会自动删除旧 OSS 对象；这不影响功能验证，但测试结束后如需控量仍需手动清理或补后台删除逻辑。
