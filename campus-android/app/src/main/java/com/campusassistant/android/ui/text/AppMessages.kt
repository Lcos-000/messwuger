package com.campusassistant.android.ui.text

object AppMessages {

    object Auth {
        const val loginFailed = "登录失败"
        const val loginNoToken = "登录成功但未返回 token"
        const val registerFailed = "注册失败"
        const val registerSuccess = "注册成功"
        const val submitSyncTaskFailed = "提交同步任务失败"
        const val refreshTaskSubmitted = "刷新任务已提交"
        const val enterStudentIdAndPassword = "请输入学号和密码"
    }

    object Schedule {
        const val loadFailed = "加载课表失败"
        const val submitSyncFailed = "提交同步任务失败"
        const val fetchFailed = "获取课表失败"
        const val weekSuffix = "周"
        const val weekRangeSeparator = "、"
    }

    object Grades {
        const val queryFailed = "查询成绩失败"
        const val submitSyncFailed = "提交成绩同步任务失败"
        const val syncTaskSubmitted = "成绩同步任务已提交"
        const val enterYear = "请输入 4 位学年"
        const val messageSeparator = "；"
        fun queriedCount(count: Int) = "已查询到 $count 条成绩"
        fun syncPending(message: String) = "$message，请稍后点击查询成绩"
    }

    object EmptyClassroom {
        const val submitTaskFailed = "提交空教室任务失败"
        const val queryResultFailed = "查询空教室结果失败"
        const val submitFailed = "提交任务失败"
        const val queryFailed = "查询结果失败"
        const val enterYear = "请输入 4 位学年"
        const val selectWeek = "请至少选择一个周次"
        const val selectPeriod = "请至少选择一个节次"
        fun taskStatus(status: String) = "任务状态：$status"
        fun queryStatus(status: String) = "查询状态：$status"
    }

    object Profile {
        const val autoPunchUpdated = "自动打卡状态已更新"
        const val autoPunchUpdateFailed = "自动打卡更新失败"
        const val serverConfigInvalid = "服务器地址格式无效，请检查 IP/域名 和端口"
        const val personalizationSaved = "个性化配置已保存"
        const val personalizationSaveFailed = "保存个性化配置失败"
        const val uploadImageFailed = "上传图片失败"
        const val uploadSuccessSaveFailed = "上传成功，但保存选择失败"
        const val accountDeleted = "账号已注销"
        const val deleteAccountFailed = "注销账号失败"
        const val messageSeparator = "；"
        fun serverConfigSaved(host: String, port: String) = "服务器设置已保存，后续请求将使用 $host:$port"
        fun assetUploaded(typeLabel: String) = "${typeLabel}已上传并保存"

        object AssetType {
            const val avatar = "头像"
            const val background = "顶部背景"
            const val wallpaper = "墙纸"
            const val image = "图片"
        }
    }

    object User {
        const val getStatusFailed = "获取用户状态失败"
        const val getInfoFailed = "获取个人信息失败"
        const val updateAutoPunchFailed = "更新自动打卡失败"
        const val deleteAccountFailed = "注销账号失败"
    }

    object Personalization {
        const val getConfigFailed = "获取个性化配置失败"
        const val saveConfigFailed = "保存个性化配置失败"
        const val getDefaultAssetsFailed = "获取默认资源失败"
        const val getCustomAssetsFailed = "获取自定义资源失败"
        const val uploadImageFailed = "上传图片失败"
    }

    object EmptyClassroomOptions {
        val semesters = listOf(
            Triple("3", "秋季/上学期", null),
            Triple("6", "小学期", null),
            Triple("12", "春季/下学期", null)
        )
        val weekdays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val campuses = listOf(
            Triple("1", "南区", null),
            Triple("2", "北区", null),
            Triple("3", "荣昌校区", null)
        )
    }
}
