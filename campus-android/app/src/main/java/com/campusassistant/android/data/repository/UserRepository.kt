package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.UserApi
import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.AutoPunchRequest
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.data.model.UserStatus

class UserRepository(
    private val userApi: UserApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getStatus(): Result<ApiResult<UserStatus>> = safeApiCall(tokenDataStore) {
        val result = userApi.getStatus()
        result.requireSuccess(tokenDataStore, "获取用户状态失败")
    }

    suspend fun getPersonal(): Result<ApiResult<UserPersonal>> = safeApiCall(tokenDataStore) {
        val result = userApi.getPersonal()
        result.requireSuccess(tokenDataStore, "获取个人信息失败")
    }

    suspend fun updateAutoPunch(enabled: Boolean): Result<ApiResult<String>> = safeApiCall(tokenDataStore) {
        val result = userApi.updateAutoPunch(AutoPunchRequest(autoPunchEnabled = if (enabled) 1 else 0))
        result.requireSuccess(tokenDataStore, "更新自动打卡失败")
    }

    suspend fun deleteAccount(): Result<ApiResult<String>> = safeApiCall(tokenDataStore) {
        val result = userApi.deleteAccount()
        result.requireSuccess(tokenDataStore, "注销账号失败")
        tokenDataStore.clearToken()
        result
    }
}
