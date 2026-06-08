package com.lightkit.network.okhttp

import com.lightkit.network.callback.NetworkCallbacks
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(
    private val callbacks: NetworkCallbacks,
    private val headerName: String = "Authorization",
    private val tokenPrefix: String = "Bearer ",
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = callbacks.authTokenOrNull()
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header(headerName, "$tokenPrefix$token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
