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
        userApi.getStatus().requireSuccess(tokenDataStore, "获取用户状态失败")
    }

    suspend fun getPersonal(): Result<ApiResult<UserPersonal>> = safeApiCall(tokenDataStore) {
        userApi.getPersonal().requireSuccess(tokenDataStore, "获取个人信息失败")
    }

    suspend fun updateAutoPunch(enabled: Boolean): Result<ApiResult<Any>> = safeApiCall(tokenDataStore) {
        userApi.updateAutoPunch(AutoPunchRequest(autoPunchEnabled = if (enabled) 1 else 0))
            .requireSuccess(tokenDataStore, "自动打卡更新失败")
    }

    suspend fun deleteAccount(): Result<ApiResult<String>> = safeApiCall(tokenDataStore) {
        val result = userApi.deleteAccount().requireSuccess(tokenDataStore, "注销账号失败")
        tokenDataStore.clearToken()
        result
    }
}
