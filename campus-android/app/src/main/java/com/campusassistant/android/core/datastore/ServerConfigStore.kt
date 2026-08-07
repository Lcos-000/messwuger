package com.campusassistant.android.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.campusassistant.android.core.network.ServerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.serverConfigDataStore by preferencesDataStore(name = "server_config_store")

class ServerConfigStore(private val context: Context) {
    private val hostKey = stringPreferencesKey("server_host")
    private val portKey = stringPreferencesKey("server_port")
    private val schemeKey = stringPreferencesKey("server_scheme")

    val configFlow: Flow<ServerConfig> = context.serverConfigDataStore.data.map { preferences ->
        ServerConfig(
            host = preferences[hostKey] ?: ServerConfig().host,
            port = preferences[portKey] ?: ServerConfig().port,
            scheme = preferences[schemeKey] ?: ServerConfig().scheme
        )
    }

    suspend fun getConfig(): ServerConfig = configFlow.first()

    suspend fun saveConfig(config: ServerConfig) {
        context.serverConfigDataStore.edit { preferences ->
            preferences[hostKey] = config.host
            preferences[portKey] = config.port
            preferences[schemeKey] = config.scheme
        }
    }
}
