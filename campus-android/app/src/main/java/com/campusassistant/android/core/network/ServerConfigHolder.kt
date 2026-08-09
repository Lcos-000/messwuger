package com.campusassistant.android.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ServerConfigHolder {
    private val mutableConfig = MutableStateFlow(ServerConfig())
    val config: StateFlow<ServerConfig> = mutableConfig.asStateFlow()

    fun current(): ServerConfig = mutableConfig.value

    fun update(config: ServerConfig) {
        mutableConfig.value = config
    }
}
