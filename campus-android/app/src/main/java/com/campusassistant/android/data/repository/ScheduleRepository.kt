package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.ScheduleApi
import com.campusassistant.android.ui.text.AppMessages

class ScheduleRepository(
    private val api: ScheduleApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getSchedule(): Result<ParsedSchedule> = safeApiCall(tokenDataStore) {
        val result = api.getSchedule()
        result.requireSuccess(tokenDataStore, AppMessages.Schedule.fetchFailed)
        parseSchedule(result.data)
    }
}
