package com.campusassistant.android.core.network

import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.data.model.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend inline fun <T> safeApiCall(
    tokenDataStore: TokenDataStore,
    crossinline block: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    runCatching { block() }.recoverCatching { throwable ->
        throw publicMapThrowable(tokenDataStore, throwable)
    }
}

suspend fun <T> ApiResult<T>.requireSuccess(
    tokenDataStore: TokenDataStore,
    defaultMessage: String
): ApiResult<T> {
    return when (code) {
        200, 204 -> this
        400 -> throw IllegalArgumentException(message ?: "参数错误")
        401 -> {
            tokenDataStore.clearToken()
            throw IllegalStateException(message ?: "登录已过期，请重新登录")
        }
        403 -> throw IllegalStateException(message ?: "当前操作不可用")
        404 -> throw IllegalStateException(message ?: "资源不存在")
        500 -> throw IllegalStateException(message ?: "服务器异常")
        else -> throw IllegalStateException(message ?: defaultMessage)
    }
}

suspend fun publicMapThrowable(tokenDataStore: TokenDataStore, throwable: Throwable): Throwable {
    return when (throwable) {
        is IllegalArgumentException, is IllegalStateException -> throwable
        is SocketTimeoutException, is UnknownHostException, is ConnectException -> IOException("网络异常，请检查服务器地址和网络连接")
        is HttpException -> {
            if (throwable.code() == 401) {
                tokenDataStore.clearToken()
                IllegalStateException("登录已过期，请重新登录")
            } else {
                IOException("网络请求失败：HTTP ${throwable.code()}")
            }
        }
        is IOException -> IOException(throwable.message ?: "网络异常，请稍后重试")
        else -> IllegalStateException(throwable.message ?: "请求失败")
    }
}
