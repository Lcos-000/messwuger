package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResult<String>

    @POST("auth/register")
    suspend fun register(@Body request: LoginRequest): ApiResult<String>

    @POST("auth/logout")
    suspend fun logout(): ApiResult<String>

    @POST("auth/refresh")
    suspend fun refreshUserData(): ApiResult<String>
}
