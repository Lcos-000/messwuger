package com.campusassistant.android.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.campusassistant.android.data.model.PublicNotice
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.noticeDataStore by preferencesDataStore(name = "notice_store")

class NoticeStore(private val context: Context) {
    private val gson = Gson()
    private val latestVersionKey = intPreferencesKey("latest_notice_version")
    private val shownVersionKey = intPreferencesKey("shown_notice_version")
    private val enabledKey = stringPreferencesKey("notice_enabled")
    private val titleKey = stringPreferencesKey("notice_title")
    private val contentKey = stringPreferencesKey("notice_content")
    private val levelKey = stringPreferencesKey("notice_level")
    private val updatedAtKey = stringPreferencesKey("notice_updated_at")
    private val historyJsonKey = stringPreferencesKey("notice_history_json")

    val latestNoticeFlow: Flow<PublicNotice?> = context.noticeDataStore.data.map { preferences ->
        val version = preferences[latestVersionKey] ?: return@map null
        PublicNotice(
            enabled = preferences[enabledKey]?.toBooleanStrictOrNull() ?: false,
            version = version,
            title = preferences[titleKey].orEmpty(),
            content = preferences[contentKey].orEmpty(),
            level = preferences[levelKey].orEmpty(),
            updatedAt = preferences[updatedAtKey].orEmpty()
        )
    }

    suspend fun getLatestNotice(): PublicNotice? = latestNoticeFlow.first()

    suspend fun getHistory(): List<PublicNotice> {
        val raw = context.noticeDataStore.data.first()[historyJsonKey].orEmpty()
        if (raw.isBlank()) return emptyList()
        val type = object : TypeToken<List<PublicNotice>>() {}.type
        return runCatching { gson.fromJson<List<PublicNotice>>(raw, type) ?: emptyList() }.getOrDefault(emptyList())
    }

    suspend fun getShownVersion(): Int = context.noticeDataStore.data.first()[shownVersionKey] ?: 0

    suspend fun saveLatestNotice(notice: PublicNotice) {
        val history = mergeHistory(getHistory(), notice)
        context.noticeDataStore.edit { preferences ->
            preferences[latestVersionKey] = notice.version ?: 0
            preferences[enabledKey] = (notice.enabled ?: false).toString()
            preferences[titleKey] = notice.title.orEmpty()
            preferences[contentKey] = notice.content.orEmpty()
            preferences[levelKey] = notice.level.orEmpty()
            preferences[updatedAtKey] = notice.updatedAt.orEmpty()
            preferences[historyJsonKey] = gson.toJson(history)
        }
    }

    suspend fun markShown(version: Int) {
        context.noticeDataStore.edit { preferences ->
            preferences[shownVersionKey] = version
        }
    }

    private fun mergeHistory(current: List<PublicNotice>, notice: PublicNotice): List<PublicNotice> {
        val merged = buildList {
            add(notice)
            current.filterNot { sameNotice(it, notice) }.forEach { add(it) }
        }
        return merged.take(20)
    }

    private fun sameNotice(left: PublicNotice, right: PublicNotice): Boolean {
        return when {
            left.version != null && right.version != null -> left.version == right.version
            else -> left.title == right.title && left.content == right.content && left.updatedAt == right.updatedAt
        }
    }
}
