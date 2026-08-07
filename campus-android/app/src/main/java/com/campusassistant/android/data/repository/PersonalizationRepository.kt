package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.PersonalizationApi
import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.UploadAssetResult
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class PersonalizationRepository(
    private val api: PersonalizationApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getProfile() = safeApiCall(tokenDataStore) {
        val result = api.getProfile()
        result.requireSuccess(tokenDataStore, "获取个性化配置失败")
    }

    suspend fun updateProfile(request: com.campusassistant.android.data.model.PersonalizationUpdateRequest) = safeApiCall(tokenDataStore) {
        val result = api.updateProfile(request)
        result.requireSuccess(tokenDataStore, "保存个性化配置失败")
    }

    suspend fun getDefaultOptions() = safeApiCall(tokenDataStore) {
        val result = api.getDefaultOptions()
        result.requireSuccess(tokenDataStore, "获取默认资源失败")
    }

    suspend fun getCustomAssets() = safeApiCall(tokenDataStore) {
        val result = api.getCustomAssets()
        result.requireSuccess(tokenDataStore, "获取自定义资源失败")
    }

    suspend fun uploadCustomAsset(type: String, file: MultipartBody.Part): Result<ApiResult<UploadAssetResult>> = safeApiCall(tokenDataStore) {
        val result = api.uploadCustomAsset(type = type.toRequestBody(MultipartBody.FORM), file = file)
        result.requireSuccess(tokenDataStore, "上传图片失败")
    }
}
