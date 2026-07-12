package com.campusassistant.android.data.model

data class ScheduleResponse(
    val studentId: String?,
    val academicYear: String?,
    val semester: String?,
    val scheduleJson: String?
)

data class CourseTeacherSchedule(
    val teacher: String?,
    val weeks: String?,
    val weekNumbers: Set<Int>
)

data class ScheduleCourse(
    val courseName: String,
    val teacher: String?,
    val classroom: String?,
    val campus: String?,
    val dayOfWeek: Int,
    val periods: String?,
    val weeks: String?,
    val startPeriod: Int,
    val endPeriod: Int,
    val weekNumbers: Set<Int>,
    val teacherSchedules: List<CourseTeacherSchedule> = emptyList()
) {
    fun isInWeek(week: Int): Boolean = weekNumbers.isEmpty() || weekNumbers.contains(week)
}

data class OtherScheduleCourse(
    val courseName: String,
    val teacher: String?,
    val classroom: String?,
    val campus: String?,
    val periods: String?,
    val weeks: String?,
    val courseType: String?
)

data class ParsedSchedule(
    val studentId: String?,
    val academicYear: String?,
    val semester: String?,
    val courses: List<ScheduleCourse>,
    val otherCourses: List<OtherScheduleCourse>
)
