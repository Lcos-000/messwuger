package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.CampusApiException
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.AuthApi
import com.campusassistant.android.data.model.LoginRequest

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) {
    val tokenFlow = tokenDataStore.tokenFlow

    suspend fun login(studentId: String, password: String): Result<Unit> {
        val firstLogin = loginOnly(studentId, password)
        if (firstLogin.isSuccess) return firstLogin

        val loginError = firstLogin.exceptionOrNull()
        if (!shouldAttemptRegister(loginError)) return firstLogin

        val registerResult = register(studentId, password)
        if (registerResult.isFailure) {
            val message = registerResult.exceptionOrNull()?.message ?: loginError?.message ?: "登录失败"
            return Result.failure(CampusApiException(null, "登录失败，自动注册未完成：$message"))
        }

        return loginOnly(studentId, password).recoverCatching { throwable ->
            throw CampusApiException(null, "注册成功，但自动登录失败：${throwable.message ?: "请稍后重试"}")
        }
    }

    suspend fun register(studentId: String, password: String): Result<String> = safeApiCall(tokenDataStore) {
        val result = authApi.register(LoginRequest(studentId = studentId, password = password))
        result.requireSuccess(tokenDataStore, "注册失败")
        result.data ?: result.message ?: "注册成功"
    }

    suspend fun refreshUserData(): Result<String> = safeApiCall(tokenDataStore) {
        val result = authApi.refreshUserData()
        result.requireSuccess(tokenDataStore, "同步数据失败")
        result.data ?: result.message ?: "同步任务已提交"
    }

    suspend fun logout() {
        safeApiCall(tokenDataStore) {
            authApi.logout().requireSuccess(tokenDataStore, "退出登录失败")
        }
        tokenDataStore.clearToken()
    }

    private suspend fun loginOnly(studentId: String, password: String): Result<Unit> = safeApiCall(tokenDataStore) {
        val result = authApi.login(LoginRequest(studentId = studentId, password = password))
        result.requireSuccess(tokenDataStore, "登录失败")
        if (result.data.isNullOrBlank()) error("登录失败")
        tokenDataStore.saveToken(result.data)
    }

    private fun shouldAttemptRegister(throwable: Throwable?): Boolean {
        val campusError = throwable as? CampusApiException
        val message = throwable?.message.orEmpty()
        val userMissing = listOf("未注册", "不存在", "用户不存在", "账号不存在", "not found", "not exist")
            .any { message.contains(it, ignoreCase = true) }
        return campusError?.code == 404 || userMissing
    }
}
