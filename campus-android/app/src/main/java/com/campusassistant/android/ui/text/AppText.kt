package com.campusassistant.android.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

@Immutable
data class AppText(
    val common: CommonText = CommonText(),
    val splash: SplashText = SplashText(),
    val login: LoginText = LoginText(),
    val nav: NavText = NavText(),
    val schedule: ScheduleText = ScheduleText(),
    val grades: GradesText = GradesText(),
    val emptyClassroom: EmptyClassroomText = EmptyClassroomText(),
    val profile: ProfileText = ProfileText()
)

@Immutable
data class CommonText(
    val loading: String = "加载中",
    val close: String = "关闭",
    val refresh: String = "刷新",
    val refreshing: String = "刷新中",
    val saving: String = "保存中",
    val emptyValue: String = "-"
)

@Immutable
data class SplashText(
    val appName: String = "cheese record",
    val logoDescription: String = "校园助手 Logo"
)

@Immutable
data class LoginText(
    val kicker: String = "Campus",
    val title: String = "校园课表查询",
    val subtitle: String = "统一登录后进入课表、成绩、空教室查询。未注册账号会使用当前学号自动创建。",
    val loginTab: String = "登录",
    val autoRegisterTab: String = "自动注册",
    val studentIdLabel: String = "学号",
    val studentIdPlaceholder: String = "请输入学号",
    val passwordLabel: String = "密码",
    val passwordPlaceholder: String = "请输入校园网密码",
    val submit: String = "登 录",
    val submitting: String = "处理中...",
    val checkingAccount: String = "正在校验账号",
    val connectionPrefix: String = "当前连接"
)

@Immutable
data class NavText(
    val schedule: String = "课表",
    val grades: String = "成绩",
    val emptyClassroom: String = "空教室",
    val profile: String = "我的"
)

@Immutable
data class ScheduleText(
    val title: String = "课表",
    val currentWeekPrefix: String = "当前第",
    val currentWeekSuffix: String = "周",
    val syncing: String = "同步中",
    val syncData: String = "同步数据",
    val currentWeek: String = "本周",
    val allWeeks: String = "全部周次",
    val otherCourses: String = "其他课程",
    val loading: String = "正在加载课表",
    val empty: String = "暂无课表/正在同步",
    val retry: String = "重新请求",
    val academicYear: String = "学年",
    val semester: String = "学期",
    val courseCountSuffix: String = "门课",
    val periodHeader: String = "节",
    val teacher: String = "教师",
    val classroom: String = "教室",
    val campus: String = "校区",
    val weekday: String = "星期",
    val periods: String = "节次",
    val weeks: String = "周次",
    val teachingPlan: String = "授课安排",
    val otherCoursesTitle: String = "其他课程",
    val noOtherCourses: String = "暂无其他课程",
    val incompleteInfo: String = "信息不完整"
)

@Immutable
data class GradesText(
    val title: String = "成绩",
    val subtitle: String = "查询已同步成绩，或提交任务获取最新成绩",
    val academicYear: String = "学年",
    val semesterFilter: String = "学期",
    val sortFilter: String = "排序",
    val viewFilter: String = "视图",
    val defaultSort: String = "默认排序",
    val scoreDesc: String = "成绩从高到低",
    val defaultSortShort: String = "默认",
    val scoreDescShort: String = "高分优先",
    val cardView: String = "卡片视图",
    val tableView: String = "表格视图",
    val course: String = "课程",
    val score: String = "成绩",
    val nature: String = "性质",
    val type: String = "类别",
    val code: String = "代码",
    val query: String = "查询成绩",
    val querying: String = "查询中",
    val latest: String = "获取最新成绩",
    val submitting: String = "提交中",
    val loading: String = "正在查询成绩",
    val empty: String = "暂无成绩数据",
    val unnamedCourse: String = "未命名课程",
    val courseInfoMissing: String = "课程信息未完善",
    val credit: String = "学分",
    val gpa: String = "绩点",
    val teacher: String = "教师",
    val exam: String = "考试",
    val semester: String = "学期",
    val sync: String = "同步"
)

@Immutable
data class EmptyClassroomText(
    val title: String = "空教室",
    val subtitle: String = "提交查询任务后，再查询空教室结果",
    val academicYear: String = "学年",
    val semester: String = "学期",
    val weekday: String = "星期",
    val weeks: String = "周次",
    val periods: String = "节次",
    val campus: String = "校区",
    val building: String = "楼栋",
    val submitTask: String = "提交任务",
    val submitting: String = "提交中",
    val queryResult: String = "查询结果",
    val querying: String = "查询中",
    val reset: String = "重置选择",
    val loading: String = "正在查询空教室",
    val empty: String = "暂无空教室结果",
    val unnamedRoom: String = "未命名教室",
    val roomInfoMissing: String = "教室信息未完善",
    val code: String = "代码",
    val capacity: String = "容量",
    val realCapacity: String = "实际容量"
)

@Immutable
data class ProfileText(
    val unknownUser: String = "未同步用户",
    val unknownStudentId: String = "学号未知",
    val refreshLoading: String = "刷新中",
    val refresh: String = "刷新",
    val studentId: String = "学号",
    val college: String = "学院",
    val major: String = "专业",
    val className: String = "班级",
    val loggedIn: String = "已登录",
    val loggedOut: String = "未登录",
    val syncStatus: String = "同步状态",
    val punchStatus: String = "打卡状态",
    val autoPunch: String = "自动打卡",
    val updating: String = "正在更新",
    val enabled: String = "已开启",
    val disabled: String = "已关闭",
    val refreshingProfile: String = "正在刷新个人主页",
    val deletingAccount: String = "正在注销账号",
    val personalization: String = "个性化设置",
    val personalizationHint: String = "点击自定义资源或上传卡片可选择相册图片；裁剪下一轮接入",
    val avatar: String = "头像",
    val background: String = "顶部背景",
    val wallpaper: String = "墙纸",
    val localInitial: String = "本地首字",
    val lightBackground: String = "浅色背景",
    val lightWallpaper: String = "浅灰墙纸",
    val cardOpacity: String = "资料卡不透明度",
    val cardBlur: String = "资料卡阴影",
    val wallpaperMask: String = "墙纸蒙版强度",
    val globalFont: String = "全局字体",
    val serverSettings: String = "服务器设置",
    val serverSummaryPrefix: String = "当前目标：",
    val serverHostLabel: String = "IP 或域名",
    val serverHostPlaceholder: String = "223.109.239.11 或 api.example.com",
    val serverPortLabel: String = "端口",
    val serverPortPlaceholder: String = "57714",
    val serverSettingsHint: String = "保存后新的接口请求和默认静态资源地址会切换到这里。适合服务器 IP、域名、端口经常变化的情况。",
    val saveServerSettings: String = "保存服务器设置",
    val expand: String = "展开",
    val collapse: String = "收起",
    val savePersonalization: String = "保存个性化配置",
    val logout: String = "退出登录",
    val deleteAccount: String = "注销账号",
    val deleteAccountHint: String = "删除当前账号数据",
    val processing: String = "处理中",
    val deleteConfirmTitle: String = "确认注销账号",
    val deleteConfirmMessage: String = "注销会调用后端删除当前用户账号，成功后将自动退出登录。此操作不可在 App 内撤销。",
    val deleteConfirm: String = "确认注销",
    val cancel: String = "取消",
    val customAsset: String = "自定义资源",
    val uploadAsset: String = "上传图片"
)

val LocalAppText = compositionLocalOf { AppText() }

@Composable
fun AppTextProvider(
    text: AppText = AppText(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppText provides text, content = content)
}
