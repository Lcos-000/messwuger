package com.campusassistant.android.data.model

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

data class EmptyClassroomResult(
    val academicYear: String?,
    val semester: String?,
    val dayOfWeek: String?,
    val periodsMask: String?,
    val weeksMask: String?,
    val campusId: String?,
    val building: String?,
    val roomType: String?,
    val queryStatus: String?,
    val resultReady: Boolean?,
    val taskSubmitted: Boolean?,
    val classrooms: List<EmptyClassroomItem>?
)

data class EmptyClassroomItem(
    val building: String?,
    val roomCode: String?,
    val roomName: String?,
    val campus: String?,
    val capacity: String?,
    val realCapacity: String?,
    val roomType: String?,
    val floor: String?,
    val remark: String?
)
