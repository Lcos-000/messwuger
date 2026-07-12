# campus-android

校园助手 Android 原生前端第一轮实现。

## 当前闭环

- Kotlin + Jetpack Compose 项目结构
- Retrofit + OkHttp 网络层
- `ApiResult<T>` 统一响应体
- DataStore 保存 token
- 登录页调用 `POST /auth/login`
- 登录成功保存 token 并进入主页面
- 主页面包含课表、成绩、空教室、我的 4 个占位 Tab
- 我的页调用 `GET /user/status` 并展示 token 是否有效及接口返回

## 后端地址

默认 Base URL 在 `app/build.gradle.kts`：

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:80/gateway/\"")
```

如果后端网关不是 `8000` 端口，或真机调试需要局域网 IP，请先修改这个值后重新构建。

## 本地构建前提

命令行构建需要配置 Android SDK 路径，例如在 `local.properties` 中设置：

```properties
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

也可以直接用 Android Studio 打开本目录，让 IDE 自动配置 SDK。
