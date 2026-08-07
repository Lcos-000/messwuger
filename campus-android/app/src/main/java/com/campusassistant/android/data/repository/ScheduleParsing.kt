package com.campusassistant.android.data.repository

import com.campusassistant.android.data.model.OtherScheduleCourse
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.data.model.ScheduleResponse
import com.campusassistant.android.data.model.TeacherSchedule
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun parseSchedule(schedule: ScheduleResponse?): ParsedSchedule {
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
        .groupBy {
            listOf(
                it.courseName,
                it.classroom.orEmpty(),
                it.campus.orEmpty(),
                it.dayOfWeek,
                it.startPeriod,
                it.endPeriod,
                it.courseType.orEmpty()
            ).joinToString("|")
        }
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
    Regex("(?<!-)(?<!\\d)(\\d+)(?!\\s*-)").findAll(weeks).forEach { match ->
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
    val diffDays = ChronoUnit.DAYS.between(termStart, today)
    return ((diffDays / 7L) + 1L).toInt().coerceIn(1, 20)
}
