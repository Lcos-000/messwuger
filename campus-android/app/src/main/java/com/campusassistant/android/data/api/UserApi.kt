package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.AutoPunchRequest
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.data.model.UserStatus
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("user/status")
    suspend fun getStatus(): ApiResult<UserStatus>

    @GET("user/personal")
    suspend fun getPersonal(): ApiResult<UserPersonal>

    @PUT("user/auto-punch")
    suspend fun updateAutoPunch(@Body request: AutoPunchRequest): ApiResult<Any>

    @DELETE("user/delete")
    suspend fun deleteAccount(): ApiResult<String>
}
