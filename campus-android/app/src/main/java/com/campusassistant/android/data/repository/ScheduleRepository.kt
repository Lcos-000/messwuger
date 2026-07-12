package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.ScheduleApi
import com.campusassistant.android.data.model.CourseTeacherSchedule
import com.campusassistant.android.data.model.OtherScheduleCourse
import com.campusassistant.android.data.model.ParsedSchedule
import com.campusassistant.android.data.model.ScheduleCourse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class ScheduleRepository(
    private val scheduleApi: ScheduleApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getSchedule(): Result<ParsedSchedule> = safeApiCall(tokenDataStore) {
        val result = scheduleApi.getSchedule()
        result.requireSuccess(tokenDataStore, "获取课表失败")

        val data = result.data
        val rawCourses = parseRawCourses(data?.scheduleJson)
        val normalCourses = rawCourses.filterIsInstance<ParsedCourse.Normal>().map { it.course }
        val otherCourses = rawCourses.filterIsInstance<ParsedCourse.Other>().map { it.course }

        ParsedSchedule(
            studentId = data?.studentId,
            academicYear = data?.academicYear,
            semester = data?.semester,
            courses = mergeCourses(normalCourses),
            otherCourses = otherCourses
        )
    }

    private fun parseRawCourses(scheduleJson: String?): List<ParsedCourse> {
        if (scheduleJson.isNullOrBlank() || scheduleJson == "null") return emptyList()

        return runCatching {
            val root = JsonParser.parseString(scheduleJson)
            val array = when {
                root.isJsonArray -> root.asJsonArray
                root.isJsonObject && root.asJsonObject.has("courses") && root.asJsonObject.get("courses").isJsonArray -> {
                    root.asJsonObject.getAsJsonArray("courses")
                }
                else -> return emptyList()
            }

            array.mapNotNull { element -> parseCourse(element) }
        }.getOrElse { emptyList() }
    }

    private fun parseCourse(element: JsonElement): ParsedCourse? {
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        val name = obj.stringValue("courseName", "name", "kcmc")?.takeIf { it.isNotBlank() } ?: return null
        val teacher = obj.stringValue("teacher", "teacherName", "xm")
        val classroom = obj.stringValue("classroom", "room", "cdmc")
        val campus = obj.stringValue("campus", "campusName")
        val periods = obj.stringValue("periods", "period", "jc")
        val weeks = obj.stringValue("weeks", "week", "zcd")
        val courseType = obj.stringValue("courseType", "type")
        val dayOfWeek = obj.intValue("dayOfWeek", "weekday", "xqj") ?: 0
        val periodRange = parseRange(periods, 1, 14)
        val weekNumbers = parseNumberSet(weeks, 1, 20)
        val hasValidTime = dayOfWeek in 1..7 && !periods.isNullOrBlank() && periodRange.first in 1..14 && periodRange.second in 1..14
        val hasValidWeeks = !weeks.isNullOrBlank() && weekNumbers.isNotEmpty()

        if (!hasValidTime || !hasValidWeeks) {
            return ParsedCourse.Other(
                OtherScheduleCourse(
                    courseName = name,
                    teacher = teacher,
                    classroom = classroom,
                    campus = campus,
                    periods = periods,
                    weeks = weeks,
                    courseType = courseType
                )
            )
        }

        return ParsedCourse.Normal(
            ScheduleCourse(
                courseName = name,
                teacher = teacher,
                classroom = classroom,
                campus = campus,
                dayOfWeek = dayOfWeek,
                periods = periods,
                weeks = weeks,
                startPeriod = periodRange.first,
                endPeriod = periodRange.second,
                weekNumbers = weekNumbers,
                teacherSchedules = listOf(CourseTeacherSchedule(teacher, weeks, weekNumbers))
            )
        )
    }

    private fun mergeCourses(courses: List<ScheduleCourse>): List<ScheduleCourse> {
        return courses
            .groupBy { course ->
                CourseMergeKey(
                    courseName = course.courseName,
                    classroom = course.classroom.orEmpty(),
                    campus = course.campus.orEmpty(),
                    dayOfWeek = course.dayOfWeek,
                    startPeriod = course.startPeriod,
                    endPeriod = course.endPeriod
                )
            }
            .values
            .map { group ->
                val first = group.first()
                val teacherSchedules = group
                    .flatMap { it.teacherSchedules.ifEmpty { listOf(CourseTeacherSchedule(it.teacher, it.weeks, it.weekNumbers)) } }
                    .distinctBy { "${it.teacher.orEmpty()}|${it.weeks.orEmpty()}" }
                val allWeeks = teacherSchedules.flatMap { it.weekNumbers }.toSet()
                val weekText = teacherSchedules.mapNotNull { it.weeks?.takeIf { value -> value.isNotBlank() } }.distinct().joinToString(" / ")

                first.copy(
                    teacher = teacherSchedules.firstOrNull()?.teacher,
                    weeks = weekText.ifBlank { first.weeks },
                    weekNumbers = allWeeks.ifEmpty { first.weekNumbers },
                    teacherSchedules = teacherSchedules
                )
            }
            .sortedWith(compareBy<ScheduleCourse> { it.dayOfWeek }.thenBy { it.startPeriod }.thenBy { it.courseName })
    }

    private fun JsonObject.stringValue(vararg keys: String): String? {
        for (key in keys) {
            val value = get(key) ?: continue
            if (!value.isJsonNull) return value.asString
        }
        return null
    }

    private fun JsonObject.intValue(vararg keys: String): Int? {
        for (key in keys) {
            val value = get(key) ?: continue
            if (value.isJsonNull) continue
            val asString = value.asString
            asString.toIntOrNull()?.let { return it }
        }
        return null
    }

    private fun parseRange(value: String?, min: Int, max: Int): Pair<Int, Int> {
        val numbers = Regex("\\d+").findAll(value.orEmpty()).mapNotNull { it.value.toIntOrNull() }.toList()
        val start = (numbers.firstOrNull() ?: min).coerceIn(min, max)
        val end = (numbers.getOrNull(1) ?: start).coerceIn(start, max)
        return start to end
    }

    private fun parseNumberSet(value: String?, min: Int, max: Int): Set<Int> {
        if (value.isNullOrBlank()) return emptySet()
        return value
            .replace("周", "")
            .replace(Regex("[（(](单|双)[）)]"), "")
            .split(',', '，', '、', ';', '；')
            .flatMap { part ->
                val numbers = Regex("\\d+").findAll(part).mapNotNull { it.value.toIntOrNull() }.toList()
                when {
                    numbers.size >= 2 && (part.contains('-') || part.contains('~') || part.contains('至')) -> {
                        val start = numbers[0].coerceIn(min, max)
                        val end = numbers[1].coerceIn(min, max)
                        if (start <= end) (start..end).toList() else emptyList()
                    }
                    numbers.isNotEmpty() -> listOf(numbers[0].coerceIn(min, max))
                    else -> emptyList()
                }
            }
            .toSet()
    }
}

private sealed interface ParsedCourse {
    data class Normal(val course: ScheduleCourse) : ParsedCourse
    data class Other(val course: OtherScheduleCourse) : ParsedCourse
}

private data class CourseMergeKey(
    val courseName: String,
    val classroom: String,
    val campus: String,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val endPeriod: Int
)
