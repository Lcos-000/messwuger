package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.AuthApi
import com.campusassistant.android.data.model.LoginRequest

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) {
    val tokenFlow = tokenDataStore.tokenFlow

    suspend fun login(studentId: String, password: String): Result<Unit> = safeApiCall(tokenDataStore) {
        val result = authApi.login(LoginRequest(studentId = studentId, password = password))
        result.requireSuccess(tokenDataStore, "登录失败")
        if (result.data.isNullOrBlank()) error("登录失败")
        tokenDataStore.saveToken(result.data)
    }

    suspend fun refreshUserData(): Result<String> = safeApiCall(tokenDataStore) {
        val result = authApi.refreshUserData()
        result.requireSuccess(tokenDataStore, "同步数据失败")
        result.data ?: result.message ?: "同步任务已提交"
    }

    suspend fun logout() {
        tokenDataStore.clearToken()
    }
}
