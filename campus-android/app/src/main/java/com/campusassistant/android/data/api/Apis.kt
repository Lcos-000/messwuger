package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.AutoPunchRequest
import com.campusassistant.android.data.model.CustomAssets
import com.campusassistant.android.data.model.DefaultAssetOptions
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.model.EmptyClassroomResponse
import com.campusassistant.android.data.model.LoginRequest
import com.campusassistant.android.data.model.ManualConfig
import com.campusassistant.android.data.model.PersonalizationProfile
import com.campusassistant.android.data.model.PublicNotice
import com.campusassistant.android.data.model.PersonalizationUpdateRequest
import com.campusassistant.android.data.model.ScheduleConfig
import com.campusassistant.android.data.model.ScheduleResponse
import com.campusassistant.android.data.model.UploadAssetResult
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.data.model.UserStatus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApi {
    @GET("public/notice")
    suspend fun getPublicNotice(): ApiResult<PublicNotice>

    @GET("public/manual")
    suspend fun getManual(): ApiResult<ManualConfig>

    @GET("public/schedule-config")
    suspend fun getScheduleConfig(): ApiResult<ScheduleConfig>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResult<String>

    @POST("auth/register")
    suspend fun register(@Body request: LoginRequest): ApiResult<String>

    @POST("auth/logout")
    suspend fun logout(): ApiResult<String>

    @POST("auth/refresh")
    suspend fun refresh(): ApiResult<String>
}

interface UserApi {
    @GET("user/status")
    suspend fun getStatus(): ApiResult<UserStatus>

    @GET("user/personal")
    suspend fun getPersonal(): ApiResult<UserPersonal>

    @PUT("user/auto-punch")
    suspend fun updateAutoPunch(@Body request: AutoPunchRequest): ApiResult<String>

    @DELETE("user/delete")
    suspend fun deleteAccount(): ApiResult<String>
}

interface ScheduleApi {
    @GET("user/schedule/get")
    suspend fun getSchedule(): ApiResult<ScheduleResponse>
}

interface EmptyClassroomApi {
    @POST("user/empty-classroom/task")
    suspend fun submitTask(@Body request: EmptyClassroomRequest): ApiResult<EmptyClassroomResponse>

    @POST("user/empty-classroom/result")
    suspend fun queryResult(@Body request: EmptyClassroomRequest): ApiResult<EmptyClassroomResponse>
}

interface PersonalizationApi {
    @GET("personalization/get-profile")
    suspend fun getProfile(): ApiResult<PersonalizationProfile>

    @PUT("personalization/update-profile")
    suspend fun updateProfile(@Body request: PersonalizationUpdateRequest): ApiResult<PersonalizationProfile>

    @GET("personalization/get-default-options")
    suspend fun getDefaultOptions(): ApiResult<DefaultAssetOptions>

    @GET("personalization/get-custom-assets")
    suspend fun getCustomAssets(): ApiResult<CustomAssets>

    @Multipart
    @POST("personalization/upload-custom-asset")
    suspend fun uploadCustomAsset(
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part
    ): ApiResult<UploadAssetResult>
}
