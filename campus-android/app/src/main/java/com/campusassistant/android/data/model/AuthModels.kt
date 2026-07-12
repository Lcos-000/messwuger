package com.campusassistant.android.data.model

data class LoginRequest(
    val studentId: String,
    val password: String
)

data class UserStatus(
    val studentId: String?,
    val syncStatus: Int?,
    val punchStatus: Int?,
    val autoPunchEnabled: Int?
)

data class UserPersonal(
    val studentId: String?,
    val name: String?,
    val major: String?,
    val className: String?,
    val college: String?
)

data class AutoPunchRequest(
    val autoPunchEnabled: Int
)
