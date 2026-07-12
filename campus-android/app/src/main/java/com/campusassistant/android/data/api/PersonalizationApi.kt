package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.CustomAssets
import com.campusassistant.android.data.model.DefaultAssetOptions
import com.campusassistant.android.data.model.PersonalizationProfile
import com.campusassistant.android.data.model.PersonalizationUpdateRequest
import com.campusassistant.android.data.model.UploadedAsset
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PUT

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
    @retrofit2.http.POST("personalization/upload-custom-asset")
    suspend fun uploadCustomAsset(
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part
    ): ApiResult<UploadedAsset>
}
