package com.campusassistant.android.core.network

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.data.model.ApiResult
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class CampusApiException(
    val code: Int?,
    override val message: String
) : Exception(message)

suspend fun <T> safeApiCall(
    tokenDataStore: TokenDataStore? = null,
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (throwable: Throwable) {
        val exception = throwable.toCampusApiException()
        if (exception.code == 401) {
            tokenDataStore?.clearToken()
        }
        Result.failure(exception)
    }
}

suspend fun <T> ApiResult<T>.requireSuccess(
    tokenDataStore: TokenDataStore? = null,
    fallbackMessage: String
): ApiResult<T> {
    if (isSuccess) return this
    if (code == 401) {
        tokenDataStore?.clearToken()
    }
    throw CampusApiException(code, code.toUserMessage(message, fallbackMessage))
}

fun Throwable.toUserMessage(): String = toCampusApiException().message

private fun Throwable.toCampusApiException(): CampusApiException {
    if (this is CampusApiException) return this
    return when (this) {
        is HttpException -> CampusApiException(code(), code().toUserMessage(message(), "请求失败"))
        is SocketTimeoutException -> CampusApiException(null, "网络异常，请检查连接后重试")
        is IOException -> CampusApiException(null, "网络异常，请检查连接后重试")
        else -> CampusApiException(null, message?.takeIf { it.isNotBlank() } ?: "请求失败")
    }
}

private fun Int?.toUserMessage(serverMessage: String?, fallbackMessage: String): String {
    val detail = serverMessage?.takeIf { it.isNotBlank() }
    return when (this) {
        400 -> detail ?: "参数错误，请检查输入"
        401 -> "登录已过期，请重新登录"
        403 -> detail ?: "业务错误，当前操作不可用"
        500 -> detail ?: "服务器错误，请稍后重试"
        else -> detail ?: fallbackMessage
    }
}

class UnauthorizedInterceptor(
    private val tokenDataStore: TokenDataStore
) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            runBlocking { tokenDataStore.clearToken() }
        }
        return response
    }
}
