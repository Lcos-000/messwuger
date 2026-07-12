package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.model.EmptyClassroomResult
import retrofit2.http.Body
import retrofit2.http.POST

interface EmptyClassroomApi {
    @POST("user/empty-classroom/task")
    suspend fun submitTask(@Body request: EmptyClassroomRequest): ApiResult<EmptyClassroomResult>

    @POST("user/empty-classroom/result")
    suspend fun queryResult(@Body request: EmptyClassroomRequest): ApiResult<EmptyClassroomResult>
}
