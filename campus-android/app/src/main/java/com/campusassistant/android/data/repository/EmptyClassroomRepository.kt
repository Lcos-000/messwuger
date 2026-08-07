package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.EmptyClassroomApi
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.model.EmptyClassroomResponse

class EmptyClassroomRepository(
    private val api: EmptyClassroomApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun submitTask(request: EmptyClassroomRequest): Result<EmptyClassroomResponse> = safeApiCall(tokenDataStore) {
        val result = api.submitTask(request)
        result.requireSuccess(tokenDataStore, "提交空教室任务失败")
        result.data ?: EmptyClassroomResponse(queryStatus = result.message)
    }

    suspend fun queryResult(request: EmptyClassroomRequest): Result<EmptyClassroomResponse> = safeApiCall(tokenDataStore) {
        val result = api.queryResult(request)
        result.requireSuccess(tokenDataStore, "查询空教室结果失败")
        result.data ?: EmptyClassroomResponse(classrooms = emptyList())
    }
}
