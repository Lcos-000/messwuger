package com.campusassistant.android.core.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class ServerConfig(
    val host: String = defaultHost(),
    val port: String = defaultPort(),
    val scheme: String = defaultScheme()
) {
    val normalizedHost: String
        get() = host.trim().removeSuffix("/")

    val normalizedPort: String
        get() = port.trim()

    fun isValid(): Boolean = normalizedHost.isNotBlank() && normalizedPort.toIntOrNull() != null

    fun apiBaseUrl(): String = "${scheme.trim().ifBlank { "http" }}://$normalizedHost:$normalizedPort/api/"

    fun staticBaseUrl(): String = "${scheme.trim().ifBlank { "http" }}://$normalizedHost:$normalizedPort/"
}

private fun defaultScheme(): String = runCatching {
    com.campusassistant.android.BuildConfig.BASE_URL.substringBefore("://")
}.getOrDefault("http")

private fun defaultHost(): String = runCatching {
    val url = com.campusassistant.android.BuildConfig.BASE_URL.toHttpUrlOrNull()
    url?.host ?: "10.0.2.2"
}.getOrDefault("10.0.2.2")

private fun defaultPort(): String = runCatching {
    val url = com.campusassistant.android.BuildConfig.BASE_URL.toHttpUrlOrNull()
    val port = url?.port ?: return@runCatching "8000"
    if (port > 0) port.toString() else if (url.isHttps) "443" else "80"
}.getOrDefault("8000")
