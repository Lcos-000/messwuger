package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.ScheduleApi

class ScheduleRepository(
    private val api: ScheduleApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getSchedule(): Result<ParsedSchedule> = safeApiCall(tokenDataStore) {
        val result = api.getSchedule()
        result.requireSuccess(tokenDataStore, "获取课表失败")
        parseSchedule(result.data)
    }
}
