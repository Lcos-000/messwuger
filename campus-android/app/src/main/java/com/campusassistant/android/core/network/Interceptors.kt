package com.campusassistant.android.core.network

import android.util.Log
import com.campusassistant.android.core.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private const val NetworkTag = "CampusNetwork"

class AuthInterceptor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenDataStore.getTokenBlocking().orEmpty()
        val request = chain.request()
        if (token.isBlank() || request.header("Authorization") != null) {
            return chain.proceed(request)
        }
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }
}

class UnauthorizedInterceptor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            runBlocking { tokenDataStore.clearToken() }
        }
        return response
    }
}

class NetworkLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d(NetworkTag, "--> ${request.method} ${request.url}")
        Log.d(NetworkTag, "tokenExists=${!request.header("Authorization").isNullOrBlank()}")
        val response = chain.proceed(request)
        Log.d(NetworkTag, "<-- ${response.code} ${request.method} ${request.url}")
        return response
    }
}
