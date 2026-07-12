package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.EmptyClassroomApi
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.model.EmptyClassroomResult

class EmptyClassroomRepository(
    private val emptyClassroomApi: EmptyClassroomApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun submitTask(request: EmptyClassroomRequest): Result<EmptyClassroomResult> = safeApiCall(tokenDataStore) {
        val result = emptyClassroomApi.submitTask(request)
        result.requireSuccess(tokenDataStore, "提交空教室任务失败")
        result.data ?: error(result.message ?: "提交空教室任务失败")
    }

    suspend fun queryResult(request: EmptyClassroomRequest): Result<EmptyClassroomResult> = safeApiCall(tokenDataStore) {
        val result = emptyClassroomApi.queryResult(request)
        result.requireSuccess(tokenDataStore, "查询空教室结果失败")
        result.data ?: error(result.message ?: "查询空教室结果失败")
    }
}
