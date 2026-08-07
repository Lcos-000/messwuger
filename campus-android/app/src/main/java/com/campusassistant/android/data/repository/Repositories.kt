package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.AuthApi
import com.campusassistant.android.data.api.EmptyClassroomApi
import com.campusassistant.android.data.api.PersonalizationApi
import com.campusassistant.android.data.api.ScheduleApi
import com.campusassistant.android.data.api.UserApi
import com.campusassistant.android.data.model.AutoPunchRequest
import com.campusassistant.android.data.model.CustomAssets
import com.campusassistant.android.data.model.DefaultAssetOptions
import com.campusassistant.android.data.model.EmptyClassroomItem
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.model.EmptyClassroomResponse
import com.campusassistant.android.data.model.LoginRequest
import com.campusassistant.android.data.model.OtherScheduleCourse
import com.campusassistant.android.data.model.PersonalizationProfile
import com.campusassistant.android.data.model.PersonalizationUpdateRequest
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.data.model.ScheduleResponse
import com.campusassistant.android.data.model.TeacherSchedule
import com.campusassistant.android.data.model.UploadAssetResult
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.data.model.UserStatus
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun hasToken(): Boolean = !tokenDataStore.getToken().isNullOrBlank()

    suspend fun login(studentId: String, password: String): Result<Unit> = safeApiCall(tokenDataStore) {
        val result = authApi.login(LoginRequest(studentId = studentId, password = password))
        result.requireSuccess(tokenDataStore, "登录失败")
        val token = result.data?.trim().orEmpty()
        if (token.isBlank()) error("登录成功但未返回 token")
        tokenDataStore.saveToken(token)
    }

    suspend fun register(studentId: String, password: String): Result<String> = safeApiCall(tokenDataStore) {
        val result = authApi.register(LoginRequest(studentId = studentId, password = password))
        result.requireSuccess(tokenDataStore, "注册失败")
        result.data ?: result.message ?: "注册成功"
    }

    suspend fun loginOrRegister(studentId: String, password: String): Result<Unit> {
        val loginResult = login(studentId, password)
        if (loginResult.isSuccess) return loginResult
        register(studentId, password)
            .onSuccess { return login(studentId, password) }
        return loginResult
    }

    suspend fun logout(): Result<Unit> = safeApiCall(tokenDataStore) {
        runCatching { authApi.logout() }
        tokenDataStore.clearToken()
    }

    suspend fun getPublicNotice() = safeApiCall(tokenDataStore) {
        authApi.getPublicNotice()
    }

    suspend fun refreshUserData(): Result<String> = safeApiCall(tokenDataStore) {
        val result = authApi.refresh()
        result.requireSuccess(tokenDataStore, "提交同步任务失败")
        result.data ?: result.message ?: "刷新任务已提交"
    }
}

class UserRepository(
    private val userApi: UserApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getStatus(): Result<com.campusassistant.android.data.model.ApiResult<UserStatus>> = safeApiCall(tokenDataStore) {
        val result = userApi.getStatus()
        result.requireSuccess(tokenDataStore, "获取用户状态失败")
    }

    suspend fun getPersonal(): Result<com.campusassistant.android.data.model.ApiResult<UserPersonal>> = safeApiCall(tokenDataStore) {
        val result = userApi.getPersonal()
        result.requireSuccess(tokenDataStore, "获取个人信息失败")
    }

    suspend fun updateAutoPunch(enabled: Boolean): Result<com.campusassistant.android.data.model.ApiResult<String>> = safeApiCall(tokenDataStore) {
        val result = userApi.updateAutoPunch(AutoPunchRequest(autoPunchEnabled = if (enabled) 1 else 0))
        result.requireSuccess(tokenDataStore, "更新自动打卡失败")
    }

    suspend fun deleteAccount(): Result<com.campusassistant.android.data.model.ApiResult<String>> = safeApiCall(tokenDataStore) {
        val result = userApi.deleteAccount()
        result.requireSuccess(tokenDataStore, "注销账号失败")
        tokenDataStore.clearToken()
        result
    }
}

class PersonalizationRepository(
    private val api: PersonalizationApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getProfile() = safeApiCall(tokenDataStore) {
        val result = api.getProfile()
        result.requireSuccess(tokenDataStore, "获取个性化配置失败")
    }

    suspend fun updateProfile(request: PersonalizationUpdateRequest) = safeApiCall(tokenDataStore) {
        val result = api.updateProfile(request)
        result.requireSuccess(tokenDataStore, "保存个性化配置失败")
    }

    suspend fun getDefaultOptions() = safeApiCall(tokenDataStore) {
        val result = api.getDefaultOptions()
        result.requireSuccess(tokenDataStore, "获取默认资源失败")
    }

    suspend fun getCustomAssets() = safeApiCall(tokenDataStore) {
        val result = api.getCustomAssets()
        result.requireSuccess(tokenDataStore, "获取自定义资源失败")
    }

    suspend fun uploadCustomAsset(type: String, file: MultipartBody.Part): Result<com.campusassistant.android.data.model.ApiResult<UploadAssetResult>> = safeApiCall(tokenDataStore) {
        val result = api.uploadCustomAsset(type = type.toRequestBody(MultipartBody.FORM), file = file)
        result.requireSuccess(tokenDataStore, "上传图片失败")
    }
}

class ScheduleRepository(
    private val api: ScheduleApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getSchedule(): Result<ParsedSchedule> = safeApiCall(tokenDataStore) {
        val result = api.getSchedule()
        result.requireSuccess(tokenDataStore, "获取课表失败")
        parseSchedule(result.data)
    }

    private fun parseSchedule(schedule: ScheduleResponse?): ParsedSchedule {
        if (schedule == null || schedule.scheduleJson.isNullOrBlank()) {
            return ParsedSchedule(schedule = schedule, courses = emptyList(), otherCourses = emptyList())
        }
        val root = runCatching { JsonParser.parseString(schedule.scheduleJson) }.getOrNull()
            ?: return ParsedSchedule(schedule = schedule, courses = emptyList(), otherCourses = emptyList())

        val array = when {
            root.isJsonArray -> root.asJsonArray
            root.isJsonObject -> root.asJsonObject.getAsJsonArray("courses")
            else -> null
        } ?: return ParsedSchedule(schedule = schedule, courses = emptyList(), otherCourses = emptyList())

        val complete = mutableListOf<RawScheduleCourse>()
        val other = mutableListOf<OtherScheduleCourse>()

        array.forEach { element ->
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@forEach
            val raw = RawScheduleCourse(
                courseName = obj.stringValue("courseName", "name", "kcmc").orEmpty(),
                teacher = obj.stringValue("teacher", "xm", "teacherName"),
                classroom = obj.stringValue("classroom", "roomName", "jxcdmc"),
                campus = obj.stringValue("campus", "xqmc"),
                dayOfWeek = obj.intValue("dayOfWeek", "xqj"),
                periods = obj.stringValue("periods", "jcdm"),
                weeks = obj.stringValue("weeks", "zcd"),
                courseType = obj.stringValue("courseType", "type")
            )
            val periodRange = parsePeriodRange(raw.periods)
            val weekNumbers = parseWeekNumbers(raw.weeks)
            if (raw.courseName.isBlank() || raw.dayOfWeek !in 1..7 || periodRange == null || weekNumbers.isEmpty()) {
                if (raw.courseName.isNotBlank()) {
                    other += OtherScheduleCourse(
                        courseName = raw.courseName,
                        teacher = raw.teacher,
                        classroom = raw.classroom,
                        campus = raw.campus,
                        dayOfWeek = raw.dayOfWeek.takeIf { it in 1..7 },
                        periods = raw.periods,
                        weeks = raw.weeks,
                        courseType = raw.courseType
                    )
                }
                return@forEach
            }
            complete += raw.copy(startPeriod = periodRange.first, endPeriod = periodRange.last, weekNumbers = weekNumbers)
        }

        val merged = complete
            .groupBy { listOf(it.courseName, it.classroom.orEmpty(), it.campus.orEmpty(), it.dayOfWeek, it.startPeriod, it.endPeriod, it.courseType.orEmpty()).joinToString("|") }
            .values
            .map { group ->
                val first = group.first()
                val mergedWeeks = group.flatMap { it.weekNumbers }.toSortedSet()
                val teacherSchedules = group.map {
                    TeacherSchedule(
                        teacher = it.teacher,
                        weeks = formatWeekNumbers(it.weekNumbers)
                    )
                }.distinct()
                ScheduleCourse(
                    courseName = first.courseName,
                    teacher = group.firstNotNullOfOrNull { it.teacher?.takeIf(String::isNotBlank) },
                    classroom = first.classroom,
                    campus = first.campus,
                    dayOfWeek = first.dayOfWeek,
                    periods = first.periods,
                    weeks = formatWeekNumbers(mergedWeeks),
                    courseType = first.courseType,
                    startPeriod = first.startPeriod,
                    endPeriod = first.endPeriod,
                    weekNumbers = mergedWeeks,
                    teacherSchedules = teacherSchedules
                )
            }
            .sortedWith(compareBy<ScheduleCourse> { it.dayOfWeek }.thenBy { it.startPeriod }.thenBy { it.courseName })

        return ParsedSchedule(schedule = schedule, courses = merged, otherCourses = other)
    }
}

data class ParsedSchedule(
    val schedule: ScheduleResponse?,
    val courses: List<ScheduleCourse>,
    val otherCourses: List<OtherScheduleCourse>
)

private data class RawScheduleCourse(
    val courseName: String,
    val teacher: String? = null,
    val classroom: String? = null,
    val campus: String? = null,
    val dayOfWeek: Int = 0,
    val periods: String? = null,
    val weeks: String? = null,
    val courseType: String? = null,
    val startPeriod: Int = 0,
    val endPeriod: Int = 0,
    val weekNumbers: Set<Int> = emptySet()
)

class EmptyClassroomRepository(
    private val api: EmptyClassroomApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun submitTask(request: EmptyClassroomRequest): Result<EmptyClassroomResponse> = safeApiCall(tokenDataStore) {
        val result = api.submitTask(request)
        result.requireSuccess(tokenDataStore, "提交空教室任务失败")
        result.data ?: EmptyClassroomResponse(queryStatus = result.message)
    }

    suspend fun queryResult(request: EmptyClassroomRequest): Result<EmptyClassroomResponse> = safeApiCall(tokenDataStore) {
        val result = api.queryResult(request)
        result.requireSuccess(tokenDataStore, "查询空教室结果失败")
        result.data ?: EmptyClassroomResponse(classrooms = emptyList())
    }
}

private fun JsonObject.stringValue(vararg keys: String): String? {
    for (key in keys) {
        val value = get(key) ?: continue
        if (value.isJsonNull) continue
        if (value.isJsonPrimitive) return value.asString
        return value.toString()
    }
    return null
}

private fun JsonObject.intValue(vararg keys: String): Int {
    return keys.firstNotNullOfOrNull { key -> get(key)?.takeIf { !it.isJsonNull }?.asInt } ?: 0
}

private fun parsePeriodRange(periods: String?): IntRange? {
    val numbers = Regex("\\d+").findAll(periods.orEmpty()).map { it.value.toInt() }.toList()
    if (numbers.isEmpty()) return null
    val start = numbers.first()
    val end = numbers.last()
    if (start !in 1..14 || end !in 1..14 || end < start) return null
    return start..end
}

private fun parseWeekNumbers(weeks: String?): Set<Int> {
    if (weeks.isNullOrBlank()) return emptySet()
    val set = sortedSetOf<Int>()
    Regex("(\\d+)\\s*-\\s*(\\d+)").findAll(weeks).forEach { match ->
        val start = match.groupValues[1].toInt()
        val end = match.groupValues[2].toInt()
        for (week in start..end) set += week
    }
    Regex("(?<!-)(?<!\\d)(\\d+)(?!\\s*-").findAll(weeks).forEach { match ->
        set += match.groupValues[1].toInt()
    }
    return set.filter { it in 1..30 }.toSet()
}

private fun formatWeekNumbers(weeks: Set<Int>): String {
    if (weeks.isEmpty()) return ""
    val sorted = weeks.sorted()
    val parts = mutableListOf<String>()
    var start = sorted.first()
    var prev = start
    for (index in 1 until sorted.size) {
        val value = sorted[index]
        if (value == prev + 1) {
            prev = value
            continue
        }
        parts += if (start == prev) "${start}周" else "${start}-${prev}周"
        start = value
        prev = value
    }
    parts += if (start == prev) "${start}周" else "${start}-${prev}周"
    return parts.joinToString("、")
}

fun calculateCurrentWeek(semester: String?, today: LocalDate = LocalDate.now()): Int {
    val termStart = if (semester == "12") {
        LocalDate.of(today.year, 3, 1)
    } else {
        LocalDate.of(today.year, 9, 1)
    }
    val diffDays = java.time.temporal.ChronoUnit.DAYS.between(termStart, today)
    return ((diffDays / 7L) + 1L).toInt().coerceIn(1, 20)
}
