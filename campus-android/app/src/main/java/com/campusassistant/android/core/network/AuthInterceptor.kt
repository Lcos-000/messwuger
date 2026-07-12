package com.campusassistant.android.core.network

import com.campusassistant.android.core.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenDataStore.getToken() }
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
