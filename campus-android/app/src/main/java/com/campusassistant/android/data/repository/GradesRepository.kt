package com.campusassistant.android.data.repository

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.requireSuccess
import com.campusassistant.android.core.network.safeApiCall
import com.campusassistant.android.data.api.GradesApi
import com.campusassistant.android.data.model.GradeItem
import com.campusassistant.android.data.model.GradeTaskRequest
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class GradesRepository(
    private val gradesApi: GradesApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun getGrades(academicYear: String, semester: String): Result<List<GradeItem>> = safeApiCall(tokenDataStore) {
        val result = gradesApi.getGrades(academicYear = academicYear, semester = semester)
        result.requireSuccess(tokenDataStore, "查询成绩失败")
        parseGrades(result.data)
    }

    suspend fun submitGradeTask(academicYear: String, semester: String): Result<String> = safeApiCall(tokenDataStore) {
        val result = gradesApi.submitGradeTask(GradeTaskRequest(academicYear = academicYear, semester = semester))
        result.requireSuccess(tokenDataStore, "提交成绩同步任务失败")
        result.data.toReadableMessage() ?: result.message ?: "成绩同步任务已提交"
    }

    private fun parseGrades(data: JsonElement?): List<GradeItem> {
        if (data == null || data.isJsonNull) return emptyList()

        return when {
            data.isJsonArray -> data.asJsonArray.mapNotNull { it.toGradeItemOrNull() }
            data.isJsonObject -> parseGradesObject(data.asJsonObject)
            else -> emptyList()
        }
    }

    private fun parseGradesObject(obj: JsonObject): List<GradeItem> {
        val candidateKeys = listOf("grades", "records", "list", "items", "rows", "data", "result")
        for (key in candidateKeys) {
            val value = obj.get(key) ?: continue
            when {
                value.isJsonArray -> return value.asJsonArray.mapNotNull { it.toGradeItemOrNull() }
                value.isJsonObject -> parseGradesObject(value.asJsonObject).takeIf { it.isNotEmpty() }?.let { return it }
            }
        }
        return obj.toGradeItemOrNull()?.let { listOf(it) }.orEmpty()
    }

    private fun JsonElement.toGradeItemOrNull(): GradeItem? {
        if (!isJsonObject) return null
        val obj = asJsonObject
        val courseName = obj.stringValue("courseName", "name", "kcmc")
        val score = obj.stringValue("score", "grade", "cj")
        if (courseName.isNullOrBlank() && score.isNullOrBlank()) return null

        return GradeItem(
            studentId = obj.stringValue("studentId", "xh"),
            academicYear = obj.stringValue("academicYear", "year", "xn"),
            semester = obj.stringValue("semester", "term", "xq"),
            courseName = courseName,
            courseCode = obj.stringValue("courseCode", "code", "kcdm", "kch"),
            courseNature = obj.stringValue("courseNature", "nature", "kcxz"),
            credit = obj.stringValue("credit", "credits", "xf"),
            score = score,
            gpa = obj.stringValue("gpa", "jd"),
            teacher = obj.stringValue("teacher", "teacherName", "xm"),
            examNature = obj.stringValue("examNature", "examType", "ksxz"),
            courseType = obj.stringValue("courseType", "type", "kclb"),
            syncTime = obj.stringValue("syncTime", "updatedAt", "createTime")
        )
    }

    private fun JsonObject.stringValue(vararg keys: String): String? {
        for (key in keys) {
            val value = get(key) ?: continue
            val parsed = value.asDisplayString()
            if (!parsed.isNullOrBlank()) return parsed
        }
        return null
    }

    private fun JsonElement.asDisplayString(): String? {
        if (isJsonNull) return null
        if (isJsonPrimitive) return asJsonPrimitive.asString
        if (isJsonObject) {
            val obj = asJsonObject
            val nestedKeys = listOf("value", "name", "label", "text", "score", "displayName")
            for (key in nestedKeys) {
                val nested = obj.get(key)?.asDisplayString()
                if (!nested.isNullOrBlank()) return nested
            }
        }
        return toString()
    }

    private fun JsonElement?.toReadableMessage(): String? {
        if (this == null || isJsonNull) return null
        if (isJsonPrimitive) return asJsonPrimitive.asString.takeIf { it.isNotBlank() }
        if (isJsonObject) {
            val obj = asJsonObject
            val messageKeys = listOf("message", "msg", "status", "queryStatus", "taskStatus", "result")
            val parts = messageKeys.mapNotNull { key -> obj.get(key)?.asDisplayString()?.takeIf { it.isNotBlank() } }
            if (parts.isNotEmpty()) return parts.distinct().joinToString("；")
        }
        return null
    }
}
