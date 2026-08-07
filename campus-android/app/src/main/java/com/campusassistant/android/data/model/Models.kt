package com.campusassistant.android.data.model

import com.google.gson.JsonElement


data class ApiResult<T>(
    val code: Int,
    val message: String? = null,
    val data: T? = null,
    val timestamp: String? = null
) {
    val isSuccess: Boolean
        get() = code == 200 || code == 204
}

data class LoginRequest(
    val studentId: String,
    val password: String
)

data class AutoPunchRequest(
    val autoPunchEnabled: Int
)

data class GradeTaskRequest(
    val academicYear: String,
    val semester: String
)

data class EmptyClassroomRequest(
    val academicYear: String,
    val semester: String,
    val dayOfWeek: String,
    val periodsMask: String,
    val weeksMask: String,
    val campusId: String,
    val building: String,
    val roomType: String = ""
)

data class PersonalizationUpdateRequest(
    val avatar: String? = null,
    val background: String? = null,
    val wallpaper: String? = null,
    val cardOpacity: Double? = null,
    val cardBlur: Double? = null,
    val globalFontEnabled: Int? = null,
    val wallpaperMask: Double? = null
)

data class UploadAssetResult(
    val type: String? = null,
    val url: String? = null
)

data class UserStatus(
    val studentId: String? = null,
    val syncStatus: Int? = null,
    val punchStatus: Int? = null,
    val autoPunchEnabled: Int? = null
)

data class UserPersonal(
    val studentId: String? = null,
    val name: String? = null,
    val major: String? = null,
    val className: String? = null,
    val college: String? = null
)

data class PersonalizationProfile(
    val studentId: String? = null,
    val avatar: String? = null,
    val background: String? = null,
    val wallpaper: String? = null,
    val cardOpacity: Double? = null,
    val cardBlur: Double? = null,
    val globalFontEnabled: Int? = null,
    val wallpaperMask: Double? = null
)

data class DefaultAssetOptions(
    val avatars: List<String>? = null,
    val backgrounds: List<String>? = null,
    val wallpapers: List<String>? = null
)

data class CustomAssets(
    val customAvatar: String? = null,
    val customBackground: String? = null,
    val customWallpaper: String? = null
)

data class ScheduleResponse(
    val studentId: String? = null,
    val academicYear: String? = null,
    val semester: String? = null,
    val scheduleJson: String? = null
)

data class TeacherSchedule(
    val teacher: String? = null,
    val weeks: String? = null
)

data class ScheduleCourse(
    val courseName: String,
    val teacher: String? = null,
    val classroom: String? = null,
    val campus: String? = null,
    val dayOfWeek: Int,
    val periods: String? = null,
    val weeks: String? = null,
    val courseType: String? = null,
    val startPeriod: Int,
    val endPeriod: Int,
    val weekNumbers: Set<Int> = emptySet(),
    val teacherSchedules: List<TeacherSchedule> = emptyList()
)

data class OtherScheduleCourse(
    val courseName: String,
    val teacher: String? = null,
    val classroom: String? = null,
    val campus: String? = null,
    val dayOfWeek: Int? = null,
    val periods: String? = null,
    val weeks: String? = null,
    val courseType: String? = null
)

data class GradeItem(
    val studentId: String? = null,
    val academicYear: String? = null,
    val semester: String? = null,
    val courseName: String? = null,
    val courseCode: String? = null,
    val courseNature: String? = null,
    val credit: String? = null,
    val score: String? = null,
    val gpa: String? = null,
    val teacher: String? = null,
    val examNature: String? = null,
    val courseType: String? = null,
    val syncTime: String? = null
)

data class EmptyClassroomResponse(
    val academicYear: String? = null,
    val semester: String? = null,
    val dayOfWeek: String? = null,
    val periodsMask: String? = null,
    val weeksMask: String? = null,
    val campusId: String? = null,
    val building: String? = null,
    val roomType: String? = null,
    val queryStatus: String? = null,
    val resultReady: Boolean? = null,
    val taskSubmitted: Boolean? = null,
    val classrooms: List<EmptyClassroomItem>? = null
)

data class EmptyClassroomItem(
    val building: String? = null,
    val roomCode: String? = null,
    val roomName: String? = null,
    val campus: String? = null,
    val capacity: String? = null,
    val realCapacity: String? = null,
    val roomType: String? = null,
    val floor: String? = null,
    val remark: String? = null
)

data class JsonWrapper(
    val raw: JsonElement? = null
)
