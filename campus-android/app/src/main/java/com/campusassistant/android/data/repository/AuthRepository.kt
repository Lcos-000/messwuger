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
