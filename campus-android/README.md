# campus-android

安卓原生客户端，使用 Kotlin + Jetpack Compose 实现。

当前项目目标不是简单复刻 Web 页面，而是按移动端使用场景重做一套可维护的原生前端。

---

## 技术栈

- Kotlin
- Jetpack Compose
- ViewModel + StateFlow
- Retrofit + OkHttp
- Gson
- DataStore
- Coil

---

## 当前能力

- 登录 / 自动注册 / 退出登录 / 注销账号
- Token 持久化与自动恢复
- 课表、成绩、空教室、我的四个主页面
- 个性化主页与资源选择、上传
- 动态服务器设置
- 公共公告、本地历史公告缓存
- 公共使用手册拉取
- 学期开学时间动态配置拉取

---

## 目录结构

```text
app/src/main/java/com/campusassistant/android
├─ core
│  ├─ datastore
│  └─ network
├─ data
│  ├─ api
│  ├─ model
│  └─ repository
└─ ui
   ├─ common
   ├─ login
   ├─ schedule
   ├─ grades
   ├─ emptyclassroom
   ├─ profile
   ├─ notice
   ├─ navigation
   ├─ splash
   ├─ text
   └─ theme
```

---

## 运行要求

- Android Studio 近期稳定版
- JDK 21
- Android SDK 已安装
- 模拟器或真机 Android 8.0+

---

## 首次运行

1. 用 Android Studio 打开 `campus-android`
2. 等待 Gradle Sync
3. 运行 `app`
4. 在登录页展开“服务器设置”
5. 填写当前网关所在主机与端口

当前客户端内部规则：

- API 基础地址：`http://<host>:<port>/api/`
- 静态资源基础地址：`http://<host>:<port>/`

因此服务端最好统一暴露：

- `/api/...` 接口
- `/...` 静态资源

---

## 本机构建命令

```powershell
cd campus-android
$env:GRADLE_USER_HOME='E:\develop\AndroidDev\.gradle'
$env:JAVA_HOME='E:\develop\Android Studio\jbr'
$env:LOCALAPPDATA=(Resolve-Path '.\.codex-localappdata').Path
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat --no-daemon --console=plain assembleDebug
```

---

## 打包 APK

在 Android Studio 中使用：

- `Build > Generate App Bundles or APKs > Generate APKs`

或自行调用 Gradle 打包。

---

## 本地文件约束

以下内容不应提交到仓库：

- `local.properties`
- `.gradle/`
- `build/`
- `.kotlin/`
- `captures/`
- `.cxx/`
- `*.apk`
- `*.aab`

如果历史上已经被 Git 追踪，需要额外执行 `git rm --cached` 取消追踪。
