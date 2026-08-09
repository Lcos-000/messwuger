package com.campusassistant.android.core.network

import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlOverrideInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val config = ServerConfigHolder.current()
        if (!config.isValid()) return chain.proceed(request)

        val newUrl = request.url.newBuilder()
            .scheme(config.scheme)
            .host(config.normalizedHost)
            .port(config.normalizedPort.toInt())
            .build()

        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}
