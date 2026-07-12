package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.GradeItem
import com.campusassistant.android.data.model.GradeTaskRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GradesApi {
    @GET("user/grades")
    suspend fun getGrades(
        @Query("academicYear") academicYear: String,
        @Query("semester") semester: String
    ): ApiResult<List<GradeItem>>

    @POST("user/grades/task")
    suspend fun submitGradeTask(@Body request: GradeTaskRequest): ApiResult<String>
}
