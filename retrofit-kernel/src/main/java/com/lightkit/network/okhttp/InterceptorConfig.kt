package com.lightkit.network.okhttp

import okhttp3.Interceptor

/**
 * OkHttp 拦截器扩展点（类似 Net 对 OkHttp 的非侵入扩展）。
 *
 * - [networkInterceptors]：`addNetworkInterceptor`，贴近 Socket（证书固定、重试等）
 * - [applicationInterceptorsBeforeBuiltIn]：在鉴权 / HTTP 文案拦截器之前
 * - [applicationInterceptorsAfterBuiltIn]：在内置拦截器之后、日志之前
 */
data class InterceptorConfig(
    val networkInterceptors: List<Interceptor> = emptyList(),
    val applicationInterceptorsBeforeBuiltIn: List<Interceptor> = emptyList(),
    val applicationInterceptorsAfterBuiltIn: List<Interceptor> = emptyList(),
)

/**
 * 日志扩展：可完全替换默认 [okhttp3.logging.HttpLoggingInterceptor]。
 */
data class LoggingConfig(
    val enabled: Boolean = false,
    /** 非 null 时直接使用，忽略 [defaultLevel] */
    val customInterceptor: Interceptor? = null,
    val defaultLevel: okhttp3.logging.HttpLoggingInterceptor.Level =
        okhttp3.logging.HttpLoggingInterceptor.Level.BODY,
)
