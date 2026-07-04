# Debug Session: go-callback-test

## Status
[FIXED]

## Objective
验证 Java 调用 Go 新增的两个接口（空教室查询、成绩查询）能否正常提交任务，并验证 Go 能否正确回调 Java 端。

## Environment
- OS: Windows
- Go service root: `c:\Users\罗宇轩\Desktop\campus(1)(4)\campus-spider-service`
- Go service addr: `localhost:8082`
- Mock Java callback addr: `localhost:18000`

## Hypotheses
1. Go 服务能正常启动并监听 8082 端口，接口路由注册正确。 ✅
2. 提交空教室/成绩任务时，密码 AES 解密成功，任务能写入 Redis Stream。 ✅
3. Worker 能消费任务并调用 Python 子进程执行爬虫逻辑。 ✅
4. Python 爬虫能复用登录逻辑并返回空教室/成绩数据。 ✅
5. Go 回调 Java 时 HTTP 请求格式、URL、Token 正确，Java 端能收到回调。 ✅

## Reproduction Steps
1. 启动模拟 Java 回调服务（监听 18000）。
2. 启动 Go 服务，设置回调 URL 指向 18000。
3. 调用 `POST /api/v1/task/empty-classroom`。
4. 调用 `POST /api/v1/task/grades`。
5. 检查模拟 Java 服务是否收到回调请求。

## Issues Found & Fixed

### Issue 1: Python `urlparse` 未定义
- **现象**：Worker 调用 Python 时报 `name 'urlparse' is not defined`
- **原因**：`swu_kb.py` 使用了 `urlparse` 但没有导入
- **修复**：添加 `from urllib.parse import urlparse`

### Issue 2: Go 端结果类型断言失败
- **现象**：Python 返回成功但 Worker 报"空教室结果类型错误"、"成绩结果类型错误"
- **原因**：`SpiderOutput.Data` 是 `any` 类型，JSON 反序列化后是 `map[string]interface{}`，无法直接断言为 `model.EmptyClassroomPayload`
- **修复**：在 `internal/spider/runner.go` 的 `RunEmptyClassroom` 和 `RunGrades` 中，使用具体类型解析 `Data` 字段

### Issue 3: 文档密码加密算法错误
- **现象**：最初测试脚本使用 AES/ECB，Go 端使用 AES/CBC
- **修复**：更新 `docs/api-integration.md`，明确 Go 使用 AES/CBC/PKCS5Padding，IV 取 key 前 16 字节

## Verification Result

两次任务提交均返回 200，模拟 Java 服务正确收到回调：

- 空教室回调：`POST /internal/api/v1/sync/empty-classroom`
- 成绩回调：`POST /internal/api/v1/sync/grades`

回调 Header 包含 `Authorization: Bearer test-token`，Body 与文档一致。
