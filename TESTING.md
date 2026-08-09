# 校园助手系统测试指南

本文档用于验证当前仓库四部分内容是否处于可用状态：

- 后端微服务
- Go + Python 爬虫服务
- Web 前端
- Android 原生客户端

---

## 一、测试范围

当前文档覆盖以下内容：

- 基础服务启动检查
- 注册 / 登录 / 用户状态 / 课表 / 成绩 / 空教室 / 个性化接口验证
- 公共公告、公共使用手册、公共学期开学配置接口验证
- Web 前端构建与手工回归
- Android 客户端安装、服务器设置、登录与主链路回归
- 数据库落库检查

---

## 二、前置准备

启动基础组件：

```powershell
cd deploy/offline/package
docker compose up -d mysql redis nacos sentinel
```

如需链路追踪：

```powershell
cd deploy
docker compose -p campusassistant -f docker-compose.skywalking.yml up -d
```

---

## 三、编译检查

### 后端

```powershell
cd campus-assistant
mvn clean install -DskipTests
```

### 爬虫服务

```powershell
cd ..\campus-spider-service
go build -o server.exe .\cmd\server
```

### Web 前端

```powershell
cd ..\campus-web
npm install
npm run build
```

### Android 客户端

```powershell
cd ..\campus-android
$env:GRADLE_USER_HOME='E:\develop\AndroidDev\.gradle'
$env:JAVA_HOME='E:\develop\Android Studio\jbr'
$env:LOCALAPPDATA=(Resolve-Path '.\.codex-localappdata').Path
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat --no-daemon --console=plain assembleDebug
```

期望结果：四部分均可构建成功。

---

## 四、服务启动

### Gateway

```powershell
cd campus-assistant
mvn spring-boot:run -pl campusswu-gateway -am
```

### User-Service

```powershell
cd campus-assistant
mvn spring-boot:run -pl user-service -am
```

### Course-Service

```powershell
cd campus-assistant
mvn spring-boot:run -pl course-service -am
```

### Go 爬虫服务

```powershell
cd campus-spider-service
$env:PYTHON_PATH="python"
go build -o server.exe ./cmd/server
.\server.exe
```

### Web 前端

```powershell
cd campus-web
npm run dev
```

---

## 五、接口联调

### 注册

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/auth/register" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"
```

### 登录

```powershell
$login = Invoke-RestMethod -Uri "http://127.0.0.1/api/auth/login" `
  -Method POST `
  -Body '{"studentId":"YOUR_STUDENT_ID","password":"yourpassword"}' `
  -ContentType "application/json"

$token = $login.data
$headers = @{ Authorization = "Bearer $token" }
```

### 用户状态

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/user/status" -Method GET -Headers $headers
```

### 公共公告

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/public/notice" -Method GET
```

### 公共手册

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/public/manual" -Method GET
```

### 学期开学配置

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/public/schedule-config" -Method GET
```

### 课表

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/user/schedule/get" -Method GET -Headers $headers
```

### 成绩查询

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/user/grades?academicYear=2025&semester=12" -Method GET -Headers $headers
```

### 成绩同步任务

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/user/grades/task" `
  -Method POST `
  -Headers $headers `
  -Body '{"academicYear":"2025","semester":"12"}' `
  -ContentType "application/json"
```

### 空教室提交与查询

```powershell
$body = '{"academicYear":"2025","semester":"12","dayOfWeek":"1","periodsMask":"16","weeksMask":"2","campusId":"2","building":"08","roomType":""}'
Invoke-RestMethod -Uri "http://127.0.0.1/api/user/empty-classroom/task" -Method POST -Headers $headers -Body $body -ContentType "application/json"
Invoke-RestMethod -Uri "http://127.0.0.1/api/user/empty-classroom/result" -Method POST -Headers $headers -Body $body -ContentType "application/json"
```

### 个性化配置

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1/api/personalization/get-profile" -Method GET -Headers $headers
```

---

## 六、Android 回归清单

在 Android Studio 或已安装 APK 的设备上验证：

### 1. 登录页

- 可展开服务器设置
- 修改 IP/域名和端口后保存生效
- 服务器设置区域在小屏手机上可完整滚动
- 公告入口可见
- 密码支持明文/隐藏切换

### 2. 登录与状态恢复

- 登录成功后进入主页
- 杀掉应用重启后仍保持登录
- Token 失效时能回到登录页

### 3. 课表

- 顶部可切换本周 / 全部周次
- 课程卡片可弹详情
- 同一课程不同老师已合并展示
- 无法定位时间的课程进入“其他课程”而不是主课表
- 同步数据按钮会触发后端刷新而不是仅前端重绘

### 4. 成绩

- 查询成绩可显示列表与表格视图
- 排序切换有效
- 条件区与结果区滚动行为正常
- 空状态、加载状态、错误状态正常

### 5. 空教室

- 条件卡片可完整显示，不应遮挡输入
- 周次、节次多选有效
- 校区切换后楼栋自动重置为第一个有效项
- 状态提示清晰

### 6. 我的页面

- 个人信息、状态信息正常显示
- 自动打卡开关可更新并失败回滚
- 个性化设置卡片可展开/收起
- 服务器设置已独立于个性化设置卡片
- 公告入口、使用手册入口可用

### 7. 个性化资源

- 默认资源与自定义资源可切换
- 再次选择已上传的自定义资源时不会错误弹到上传流程
- 头像按圆形展示
- 墙纸、蒙版、卡片样式全局生效

---

## 七、数据库验证

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

## 八、已知注意点

1. 前端当前主要依靠构建通过 + 手工回归验证，不依赖完整自动化 UI 测试。
2. Android 客户端当前固定浅色主题，未专门适配深色模式。
3. 若后端返回相对静态资源路径，Android 会按当前服务器设置自动拼接资源前缀；如果返回完整 OSS URL，则优先直接加载完整 URL。
4. 如果 Nacos 中公共配置更新后客户端仍无变化，优先检查 YAML 缩进、配置 import、`@RefreshScope` 和接口实际返回值。
5. 若 Git 历史中已包含 `node_modules/` 或 Android 本地文件，本轮需要额外执行一次取消追踪。
