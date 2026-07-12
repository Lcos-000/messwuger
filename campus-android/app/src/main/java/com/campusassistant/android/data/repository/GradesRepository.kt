package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.GradesApi
import com.campusassistant.android.data.model.GradeItem
import com.campusassistant.android.data.model.GradeTaskRequest

class GradesRepository(
    private val gradesApi: GradesApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getGrades(academicYear: String, semester: String): Result<List<GradeItem>> = safeApiCall(tokenDataStore) {
        val result = gradesApi.getGrades(academicYear = academicYear, semester = semester)
        result.requireSuccess(tokenDataStore, "查询成绩失败")
        result.data.orEmpty()
    }

    suspend fun submitGradeTask(academicYear: String, semester: String): Result<String> = safeApiCall(tokenDataStore) {
        val result = gradesApi.submitGradeTask(GradeTaskRequest(academicYear = academicYear, semester = semester))
        result.requireSuccess(tokenDataStore, "提交成绩同步任务失败")
        result.data ?: result.message ?: "成绩同步任务已提交"
    }
}
