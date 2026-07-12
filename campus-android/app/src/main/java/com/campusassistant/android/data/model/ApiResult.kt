package com.campusassistant.android.data.model

data class ApiResult<T>(
    val code: Int,
    val message: String?,
    val data: T?,
    val timestamp: String?
) {
    val isSuccess: Boolean
        get() = code == 200 || code == 204
}
