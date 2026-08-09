package com.campusassistant.android.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenDataStore by preferencesDataStore(name = "token_store")

class TokenDataStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("auth_token")

    val tokenFlow: Flow<String?> = context.tokenDataStore.data.map { preferences ->
        preferences[tokenKey]
    }

    suspend fun getToken(): String? = tokenFlow.first()

    fun getTokenBlocking(): String? = runBlocking { getToken() }

    suspend fun saveToken(token: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun clearToken() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}
