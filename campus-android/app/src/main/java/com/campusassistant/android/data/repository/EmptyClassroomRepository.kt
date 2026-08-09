package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.EmptyClassroomApi
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.model.EmptyClassroomResponse
import com.campusassistant.android.ui.text.AppMessages

class EmptyClassroomRepository(
    private val api: EmptyClassroomApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun submitTask(request: EmptyClassroomRequest): Result<EmptyClassroomResponse> = safeApiCall(tokenDataStore) {
        val result = api.submitTask(request)
        result.requireSuccess(tokenDataStore, AppMessages.EmptyClassroom.submitTaskFailed)
        result.data ?: EmptyClassroomResponse(queryStatus = result.message)
    }

    suspend fun queryResult(request: EmptyClassroomRequest): Result<EmptyClassroomResponse> = safeApiCall(tokenDataStore) {
        val result = api.queryResult(request)
        result.requireSuccess(tokenDataStore, AppMessages.EmptyClassroom.queryResultFailed)
        result.data ?: EmptyClassroomResponse(classrooms = emptyList())
    }
}
