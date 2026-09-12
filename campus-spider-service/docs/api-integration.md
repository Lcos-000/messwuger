# Go 爬虫服务空教室 / 成绩查询对接文档

## 一、接口概览

| 功能 | Go 提交接口 | 默认回调地址 | 回调方式 |
|------|------------|--------------|---------|
| 空教室查询 | `POST /api/v1/task/empty-classroom` | `http://localhost:8000/internal/api/v1/sync/empty-classroom` | 异步 POST |
| 成绩查询 | `POST /api/v1/task/grades` | `http://localhost:8000/internal/api/v1/sync/grades` | 异步 POST |

流程：Java 调用 Go 接口提交任务 → Go 写入 Redis Stream → Worker 消费 → 调用 Python 爬虫 → Go 回调 Java。

---

## 二、公共约定

### 2.1 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| `X-Student-Id` | 是 | 学生学号 |
| `X-Password` | 是 | AES 加密后的密码 |
| `Content-Type` | 是 | `application/json` |

### 2.2 密码加密

Go 端使用 **AES/CBC/PKCS5Padding** 解密，IV 取 key 的前 16 字节：


### 2.3 提交接口响应

```json
{
  "code": 200,
  "message": "空教室任务已提交",
  "data": {
    "taskId": "task-xxxx-xxxx-xxxx"
  }
}
```

---

## 三、空教室查询接口

### 3.1 Java → Go 提交任务

```http
POST http://localhost:8082/api/v1/task/empty-classroom HTTP/1.1
X-Student-Id: 222025321262104
X-Password: {AES加密后的密码}
Content-Type: application/json
X-Spider-Token: {SPIDER_API_TOKEN}
Content-Length: 248
```json
{
  "academicYear": "2025",
  "semester": "12",
  "dayOfWeek": "3,6",
  "periodsMask": "3072",
  "weeksMask": "65535",
  "campusId": "2",
  "building": "01",
  "roomType": "",
  "callbackUrl": "http://localhost:8000/internal/api/v1/sync/empty-classroom"
}
```

> 注：`weeksMask=65535` 表示 1-16 周全选；`262272` 表示第 8 周 + 第 19 周。

### 3.2 Go → Java 异步回调

```http
POST http://localhost:8000/internal/api/v1/sync/empty-classroom HTTP/1.1
Content-Type: application/json
Authorization: Bearer {JAVA_INTERNAL_TOKEN}
Content-Length: 452

{
  "studentId": "222025321262104",
  "academicYear": "2025",
  "semester": "12",
  "dayOfWeek": "3,6",
  "periodsMask": "3072",
  "weeksMask": "65535",
  "campusId": "2",
  "building": "01",
  "roomType": "",
  "classrooms": [
    {
      "building": "01教",
      "roomCode": "01-0101",
      "roomName": "01-0101",
      "campus": "北区",
      "capacity": "60",
      "realCapacity": "60",
      "roomType": "多媒体教室",
      "floor": "1",
      "remark": ""
    }
  ]
}
```

---

## 四、成绩查询接口

### 4.1 Java → Go 提交任务

```http
POST http://localhost:8082/api/v1/task/grades HTTP/1.1
X-Student-Id: 222025321262104
X-Password: {AES加密后的密码}
Content-Type: application/json
Content-Length: 168

{
  "academicYear": "2025",
  "semester": "12",
  "callbackUrl": "http://localhost:8000/internal/api/v1/sync/grades"
}
```

### 4.2 Go → Java 异步回调

```http
POST http://localhost:8000/internal/api/v1/sync/grades HTTP/1.1
Content-Type: application/json
Authorization: Bearer {JAVA_INTERNAL_TOKEN}
Content-Length: 386

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

## 五、参数说明与构造

### 5.1 参数总表

| 参数 | 接口 | 必填 | 类型 | 默认值 | 说明 |
|------|------|------|------|--------|------|
| `academicYear` | 两者 | 否 | string | `2025` | 学年 |
| `semester` | 两者 | 否 | string | `12` | 学期，`12`=第二学期，`3`=第一学期 |
| `callbackUrl` | 两者 | 否 | string | 环境变量默认值 | 自定义回调地址 |
| `dayOfWeek` | 空教室 | **是** | string | - | 星期几，可多选，如 `"3,6"` |
| `periodsMask` | 空教室 | **是** | string | - | 节次掩码，支持多选 |
| `weeksMask` | 空教室 | **是** | string | - | 周次掩码，支持多选 |
| `campusId` | 空教室 | 否 | string | `1` | 校区编码 |
| `building` | 空教室 | 否 | string | `""` | 楼号编码，空表示不限 |
| `roomType` | 空教室 | 否 | string | `""` | 教室类别编码，空表示不限 |

### 5.2 `dayOfWeek` 星期

格式为逗号分隔的数字：

| 值 | 含义 |
|----|------|
| `1` | 周一 |
| `2` | 周二 |
| `3` | 周三 |
| `4` | 周四 |
| `5` | 周五 |
| `6` | 周六 |
| `7` | 周日 |

示例：
- `"3"`：周三
- `"3,6"`：周三和周六
- `"1,3,5"`：周一、周三、周五

### 5.3 `periodsMask` 节次掩码

使用二进制位表示节次，第 n 节对应 `2^(n-1)`：

| 节次 | 掩码值 |
|------|--------|
| 第 1 节 | 1 |
| 第 2 节 | 2 |
| 第 3 节 | 4 |
| 第 4 节 | 8 |
| 第 5 节 | 16 |
| 第 6 节 | 32 |
| 第 7 节 | 64 |
| 第 8 节 | 128 |
| 第 9 节 | 256 |
| 第 10 节 | 512 |
| 第 11 节 | 1024 |
| 第 12 节 | 2048 |
| 第 13 节 | 4096 |
| 第 14 节 | 8192 |

多选时做位或运算：
- `"5-5"` → `16`
- `"11-12"` → `3072`
- `"1-2,5-6"` → `51`

### 5.4 `weeksMask` 周次掩码

与节次掩码规则相同，第 n 周对应 `2^(n-1)`：

- `"1-1"` → `1`
- `"8-8"` → `128`
- `"1-16"` → `65535`
- `"8,19"` → `262272`（第 8 周 + 第 19 周）

> 常见误区：`262272` 不是 1-16 周全选，而是第 8 周和第 19 周的组合。

### 5.5 `campusId` 校区编码

| 编码 | 校区 |
|------|------|
| `1` | 南区 |
| `2` | 北区 |
| `3` | 荣昌校区 |
| `4` | 其他/线上 |

### 5.6 `building` 楼号编码

#### 南区（`campusId=1`）

| 编码 | 名称 |
|------|------|
| `30` | 30教 |
| `31` | 31教 |
| `32` | 32教 |
| `33` | 33教 |
| `35` | 35教 |
| `36` | 36教 |
| `37` | 37教 |
| `38` | 38教 |
| `39` | 39教 |
| `40` | 40教 |
| `45` | 45教 |
| `46` | 46教 |
| `48` | 48教 |
| `96` | 96教 |
| `97` | 97教 |
| `98` | 98教 |
| `1001` | Online Learning |
| `GKL-A` | 工科大楼A座 |
| `GKL-B` | 工科大楼B座 |
| `KJL` | 科技楼 |
| `HDZX` | 学生活动中心 |
| `wlh` | 无楼号 |

#### 北区（`campusId=2`）

| 编码 | 名称 |
|------|------|
| `01` | 01教 |
| `02` | 02教 |
| `03` | 03教 |
| `04` | 04教 |
| `05` | 05教 |
| `06` | 06教 |
| `07` | 07教 |
| `08` | 08教 |
| `09` | 09教 |
| `10` | 10教 |
| `11` | 11教 |
| `13` | 13教 |
| `14` | 14教 |
| `15` | 15教 |
| `16` | 16教 |
| `17` | 17教 |
| `19` | 19教 |
| `21` | 21教 |
| `23` | 23教 |
| `24` | 24教 |
| `25` | 25教 |
| `26` | 26教 |
| `27` | 27教 |
| `28` | 28教 |
| `29` | 29教 |
| `93` | 93教 |
| `95` | 95教 |
| `99` | 99教 |
| `1002` | Online Learning |
| `CMSYSX` | 传媒实验实训大楼 |
| `HY01` | 化学与药学大楼 |
| `SXDL` | 数学大楼 |
| `XCBL` | 新出版楼 |
| `wlh` | 无楼号 |

#### 荣昌校区（`campusId=3`）

| 编码 | 名称 |
|------|------|
| `RC00` | 第零教楼 |
| `RC01` | 第一教学楼 |
| `RC02` | 第二教学楼 |
| `RC03` | 第三教学楼 |
| `RC04` | 第四教学楼 |
| `RC05` | 第五教学楼 |
| `RC7B` | 第七教学楼 |
| `RC09` | 第九教学楼 |
| `1003` | Online Learning |
| `rwlh` | rc-无楼号 |
| `wlh` | 无楼号 |

### 5.7 `roomType` 教室类别编码

目前抓包中 `cdlb_id` 都为空，常见值需要后续补充。传空字符串表示不限。

---

## 六、Java 构造参数示例

```java
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EmptyClassroomExample {

    public static int encodeMask(String text) {
        int mask = 0;
        for (String part : text.replace("，", ",").split(",")) {
            String[] range = part.trim().split("-");
            int start = Integer.parseInt(range[0]);
            int end = range.length > 1 ? Integer.parseInt(range[1]) : start;
            for (int i = start; i <= end; i++) {
                mask |= 1 << (i - 1);
            }
        }
        return mask;
    }

    public static String aesEncrypt(String plainText, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(key.getBytes(StandardCharsets.UTF_8), 0, 16);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] data = plainText.getBytes(StandardCharsets.UTF_8);
        int padLen = 16 - (data.length % 16);
        byte[] padded = new byte[data.length + padLen];
        System.arraycopy(data, 0, padded, 0, data.length);
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) padLen;
        }
        return Base64.getEncoder().encodeToString(cipher.doFinal(padded));
    }

    public static void main(String[] args) throws Exception {
        String aesKey = System.getenv().getOrDefault("AES_SECRET_KEY", "@aes-secret-key#");
        String studentId = "222025321262104";
        String password = "your_password";

        Map<String, Object> body = new HashMap<>();
        body.put("academicYear", "2025");
        body.put("semester", "12");
        body.put("dayOfWeek", "3,6");
        body.put("periodsMask", String.valueOf(encodeMask("11-12")));
        body.put("weeksMask", String.valueOf(encodeMask("1-16")));
        body.put("campusId", "2");
        body.put("building", "01");
        body.put("roomType", "");

        // 使用 HttpClient 发送 POST 请求
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create("http://localhost:8082/api/v1/task/empty-classroom"))
        //     .header("X-Student-Id", studentId)
        //     .header("X-Password", aesEncrypt(password, aesKey))
        //     .header("Content-Type", "application/json")
        //     .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(body)))
        //     .build();
    }
}
```

---

## 七、注意事项

1. **回调是异步的**，提交接口只返回 `taskId`，真实结果通过 `callbackUrl` 推送。
2. **回调需要校验 Token**，Go 会带 `Authorization: Bearer {JAVA_INTERNAL_TOKEN}`。
3. **空教室查询必填** `dayOfWeek`、`periodsMask`、`weeksMask`。
4. **`dayOfWeek` 不是掩码**，直接传逗号分隔字符串，如 `"3,6"`。
5. **`periodsMask` 和 `weeksMask` 是掩码**，按 `2^(n-1)` 编码。
6. Java 回调接口返回 HTTP 2xx 即可，Go 不解析响应体。
