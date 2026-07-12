package com.campusassistant.android.core.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

class NetworkLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val tokenExists = request.header("Authorization").isNullOrBlank().not()
        val bodyText = request.body?.let { body ->
            runCatching {
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readString(Charset.forName("UTF-8"))
            }.getOrNull()
        }

        val safeBodyText = bodyText?.replace(Regex("\\\"password\\\"\\s*:\\s*\\\"[^\\\"]*\\\""), "\"password\":\"***\"")
        Log.d("CampusNetwork", "request method=${request.method} url=${request.url} tokenExists=$tokenExists params=${safeBodyText.orEmpty()}")
        val response = chain.proceed(request)
        Log.d("CampusNetwork", "response code=${response.code} url=${request.url}")
        return response
    }
}
