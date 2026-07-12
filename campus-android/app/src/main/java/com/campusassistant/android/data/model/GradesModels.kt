package com.campusassistant.android.data.model

data class GradeItem(
    val studentId: String?,
    val academicYear: String?,
    val semester: String?,
    val courseName: String?,
    val courseCode: String?,
    val courseNature: String?,
    val credit: String?,
    val score: String?,
    val gpa: String?,
    val teacher: String?,
    val examNature: String?,
    val courseType: String?,
    val syncTime: String?
)

data class GradeTaskRequest(
    val academicYear: String,
    val semester: String
)
