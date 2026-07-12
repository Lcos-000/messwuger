package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.PersonalizationApi
import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.CustomAssets
import com.campusassistant.android.data.model.DefaultAssetOptions
import com.campusassistant.android.data.model.PersonalizationProfile
import com.campusassistant.android.data.model.PersonalizationUpdateRequest
import com.campusassistant.android.data.model.UploadedAsset
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class PersonalizationRepository(
    private val personalizationApi: PersonalizationApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getProfile(): Result<ApiResult<PersonalizationProfile>> = safeApiCall(tokenDataStore) {
        personalizationApi.getProfile().requireSuccess(tokenDataStore, "获取个性化配置失败")
    }

    suspend fun getDefaultOptions(): Result<ApiResult<DefaultAssetOptions>> = safeApiCall(tokenDataStore) {
        personalizationApi.getDefaultOptions().requireSuccess(tokenDataStore, "获取默认资源失败")
    }

    suspend fun getCustomAssets(): Result<ApiResult<CustomAssets>> = safeApiCall(tokenDataStore) {
        personalizationApi.getCustomAssets().requireSuccess(tokenDataStore, "获取自定义资源失败")
    }

    suspend fun updateProfile(request: PersonalizationUpdateRequest): Result<ApiResult<PersonalizationProfile>> = safeApiCall(tokenDataStore) {
        personalizationApi.updateProfile(request).requireSuccess(tokenDataStore, "保存个性化配置失败")
    }

    suspend fun uploadCustomAsset(type: String, file: MultipartBody.Part): Result<ApiResult<UploadedAsset>> = safeApiCall(tokenDataStore) {
        personalizationApi.uploadCustomAsset(type.toRequestBody(MultipartBody.FORM), file)
            .requireSuccess(tokenDataStore, "上传图片失败")
    }
}
