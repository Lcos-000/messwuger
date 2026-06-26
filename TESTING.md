# 校园助手系统测试指南

本文档用于验证当前项目的核心链路是否可用，重点覆盖后端服务、爬虫服务，以及与当前版本对齐的个性化配置、自定义图片资源和自动打卡能力。

---

## 测试范围

当前文档覆盖以下内容：

- 基础服务启动检查
- 注册 / 登录 / 同步 / 查询课表
- 个性化主页接口
- 自定义图片上传与回显接口
- 自动打卡开关接口
- 前端构建验证
- 前端手工回归检查
- 数据库落库检查

> 当前仓库未配置前端单元测试或 E2E 自动化测试，因此前端主要通过 `npm run build` + 手工回归完成验证。

---

## 前置准备

确保以下依赖已启动：

```powershell
net start MySQL80
```

```powershell
cd C:\nacos\bin
startup.cmd -m standalone
```

Redis 请确保服务已运行。

---

## 可选：清空测试数据

```powershell
mysql -u root -p1234 -e "USE campus_db; TRUNCATE TABLE user_profile_custom_asset; TRUNCATE TABLE user_profile_style; TRUNCATE TABLE course_db; TRUNCATE TABLE personal_info; TRUNCATE TABLE student_db;"
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

---

## 端口确认

```powershell
netstat -ano | findstr ":80 "
netstat -ano | findstr ":8000 "
netstat -ano | findstr ":9000 "
netstat -ano | findstr ":8082 "
netstat -ano | findstr ":5173 "
```

**期望结果**：上述端口均处于 `LISTENING`。

---

## 接口联调流程

> 如果 PowerShell 中文乱码，可先执行：
>
> ```powershell
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
> $OutputEncoding = [System.Text.Encoding]::UTF8
> chcp 65001
> ```

### 1. 注册

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/register" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"
```

**期望结果**：

- `code = 200`
- 后台开始同步

### 2. 登录

```powershell
$login = Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/login" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"

$token = $login.data
$headers = @{ Authorization = "Bearer $token" }
```

**期望结果**：

- `code = 200`
- `data` 返回 JWT 字符串

### 3. 查询用户状态

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/status" `
  -Method GET `
  -Headers $headers
```

**期望结果**：

- `code = 200`
- `data` 至少包含：`studentId`、`syncStatus`、`punchStatus`
- 当前版本还应包含：`autoPunchEnabled`

### 4. 查询个人信息

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/personal" `
  -Method GET `
  -Headers $headers
```

### 5. 查询课表

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/schedule/get" `
  -Method GET `
  -Headers $headers
```

### 6. 手动刷新同步

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/auth/refresh" `
  -Method POST `
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

**期望结果**：

- `code = 200`
- `data` 包含：`avatar`、`background`、`wallpaper`、`cardOpacity`、`cardBlur`、`wallpaperMask`、`globalFontEnabled`

### 2. 更新个性化配置

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/update-profile" `
  -Method PUT `
  -Headers $headers `
  -Body '{"cardOpacity":0.72,"cardBlur":8,"wallpaperMask":0.65,"globalFontEnabled":1}' `
  -ContentType "application/json"
```

若要验证“极简默认资料”回退逻辑，可额外测试：

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/update-profile" `
  -Method PUT `
  -Headers $headers `
  -Body '{"avatar":"","background":"","wallpaper":""}' `
  -ContentType "application/json"
```

**期望结果**：

- `code = 200`
- 再次调用 `get-profile` 时，返回值发生变化；若传空字符串，前端应回退到极简默认显示

### 3. 获取默认图库

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/get-default-options" `
  -Method GET `
  -Headers $headers
```

**期望结果**：

- `code = 200`
- `data.avatars`、`data.backgrounds`、`data.wallpapers` 为数组

### 4. 获取自定义图片资源

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:8000/personalization/get-custom-assets" `
  -Method GET `
  -Headers $headers
```

**期望结果**：

- `code = 200`
- `data` 包含 `customAvatar`、`customBackground`、`customWallpaper`，未上传时允许为 `null`

### 5. 上传自定义图片

```powershell
curl.exe -X POST "http://127.0.0.1:8000/personalization/upload-custom-asset" ^
  -H "Authorization: Bearer $token" ^
  -F "type=background" ^
  -F "file=@C:\tmp\profile-bg.jpg"
```

**期望结果**：

- `code = 200`
- `data.url` 返回可访问的 OSS URL
- 再次调用 `get-custom-assets` 时，对应字段已更新

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

### 3. 再次查询用户状态

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/gateway/user/status" `
  -Method GET `
  -Headers $headers
```

**期望结果**：

- `autoPunchEnabled` 与刚刚写入值一致
- 当前前后端按 `0/1` 对齐，而不是 `true/false`

---

## 数据库验证

```powershell
mysql -u root -p1234 -e "
USE campus_db;
SELECT student_id, sync_status, punch_status, auto_punch_enabled FROM student_db WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, card_opacity, card_blur, wallpaper_mask, global_font_enabled FROM user_profile_style WHERE student_id='YOUR_STUDENT_ID';
SELECT student_id, custom_avatar, custom_background, custom_wallpaper FROM user_profile_custom_asset WHERE student_id='YOUR_STUDENT_ID';
SELECT COUNT(*) AS personal_info_count FROM personal_info WHERE student_id='YOUR_STUDENT_ID';
SELECT COUNT(*) AS course_count FROM course_db WHERE student_id='YOUR_STUDENT_ID';
"
```

**期望结果**：

| 检查项 | 期望 |
|---|---|
| `student_db.sync_status` | `2` 或符合当前同步流程状态 |
| `student_db.punch_status` | `0-3` |
| `student_db.auto_punch_enabled` | `0` 或 `1` |
| `user_profile_style.card_opacity` | 能与最近一次保存值对应 |
| `user_profile_style.card_blur` | 能与最近一次保存值对应 |
| `user_profile_style.wallpaper_mask` | 能与最近一次保存值对应 |
| `user_profile_style.global_font_enabled` | `0` 或 `1` |
| `user_profile_custom_asset.custom_*` | 上传后应为对应 OSS URL，未上传可为空 |
| `personal_info_count` | `1` |
| `course_count` | `1` |

---

## 前端手工回归清单

浏览器打开 `http://localhost:5173`，登录后重点检查 `Profile` 页。

### 1. 基础展示

- 头像、顶部背景、壁纸正常显示
- 学号、学院、专业、班级正常显示
- 同步状态、打卡状态正常显示

### 2. 个性化设置

展开“个性化设置”，检查：

- 资料卡透明度滑块可拖动，刷新后保持
- 资料卡模糊度滑块可拖动，切页后返回仍保持
- 墙纸蒙版强度滑块可拖动，且课表页与个人页表现一致
- 全局字体开关可切换，刷新后保持
- 默认头像可切换，且下方说明文案与头像一一对应
- 默认顶部背景可切换
- 默认壁纸可切换
- 自定义头像 / 顶部背景 / 墙纸可上传并回显
- 点击“重置为极简默认”后，头像/背景/墙纸应回退到前端兜底样式

### 3. 显示效果检查

- 透明度变化后资料卡视觉明显变化
- 模糊度调到 `0` 时接近完全清晰
- 模糊度调高后磨砂感增强
- 墙纸蒙版调到 `0` 时更接近清晰原图
- 墙纸蒙版调高后页面更偏浅灰极简效果
- 字体开关开启后页面使用 `SourceHanSerifCN-Regular.ttf`
- 字体开关关闭后恢复系统字体

### 4. 自动打卡开关

- 开关样式为圆角轨道 + 圆形滑块
- 切换后文案同步变化
- 刷新页面后状态保持

### 5. 登录失效场景

手动构造后端返回 `code = 401` 的情况，检查：

- 前端会清理 token
- 页面跳转回登录页

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

> 当前自定义图片上传成功后，历史 OSS 对象不会自动删除；这不影响功能验证，但测试结束后如需控量需手动清理 Bucket。

### 2. 鉴权头格式

当前前端请求拦截器会自动附加：

```text
Authorization: Bearer <token>
```

如果手工调接口时不加 `Bearer` 前缀，可能导致鉴权失败。

### 3. 401 处理

前端不仅处理 HTTP 401，也处理响应体 `code = 401`。这点在联调时需要特别注意。

