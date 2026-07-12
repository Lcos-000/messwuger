package com.campusassistant.android.data.api

import com.campusassistant.android.data.model.ApiResult
import com.campusassistant.android.data.model.ScheduleResponse
import retrofit2.http.GET

interface ScheduleApi {
    @GET("user/schedule/get")
    suspend fun getSchedule(): ApiResult<ScheduleResponse>
}
