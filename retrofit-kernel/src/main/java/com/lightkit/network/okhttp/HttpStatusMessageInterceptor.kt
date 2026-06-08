package com.lightkit.network.okhttp

import com.lightkit.network.callback.NetworkCallbacks
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 将 OkHttp 失败响应的 message 替换为宿主提供的本地化文案。
 */
class HttpStatusMessageInterceptor(
    private val callbacks: NetworkCallbacks,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response
        val replacement = callbacks.httpStatusMessageOrNull(response.code) ?: return response
        return response.newBuilder().message(replacement).build()
    }
}
