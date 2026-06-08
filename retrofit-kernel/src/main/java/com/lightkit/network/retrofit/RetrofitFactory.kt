package com.lightkit.network.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.lightkit.network.callback.NetworkCallbacks
import com.lightkit.network.okhttp.AuthHeaderInterceptor
import com.lightkit.network.okhttp.HttpStatusMessageInterceptor
import com.lightkit.network.okhttp.InterceptorConfig
import com.lightkit.network.okhttp.LoggingConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class RetrofitFactoryConfig(
    val baseUrl: String,
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutSeconds: Long = 30,
    val writeTimeoutSeconds: Long = 30,
    /** @deprecated 使用 [logging.enabled] */
    val debugLogging: Boolean = false,
    val interceptors: InterceptorConfig = InterceptorConfig(),
    val logging: LoggingConfig = LoggingConfig(),
    val gson: Gson = defaultGson(),
    /**
     * 自定义 Retrofit Converter，按顺序注册。
     * 非空时仍会在末尾追加默认 Gson（除非 [useDefaultGsonConverter] = false）。
     */
    val converterFactories: List<Converter.Factory> = emptyList(),
    val useDefaultGsonConverter: Boolean = true,
    val enableBuiltInAuthInterceptor: Boolean = true,
    val enableBuiltInHttpStatusInterceptor: Boolean = true,
) {
    fun resolvedLogging(): LoggingConfig =
        if (logging.enabled || debugLogging) {
            logging.copy(enabled = true)
        } else {
            logging
        }

    companion object {
        fun defaultGson(): Gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
    }
}

object RetrofitFactory {

    fun createOkHttpClient(
        callbacks: NetworkCallbacks,
        config: RetrofitFactoryConfig,
    ): OkHttpClient {
        val loggingConfig = config.resolvedLogging()
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)

        config.interceptors.networkInterceptors.forEach { builder.addNetworkInterceptor(it) }

        config.interceptors.applicationInterceptorsBeforeBuiltIn.forEach { builder.addInterceptor(it) }

        if (config.enableBuiltInAuthInterceptor) {
            builder.addInterceptor(AuthHeaderInterceptor(callbacks))
        }
        if (config.enableBuiltInHttpStatusInterceptor) {
            builder.addInterceptor(HttpStatusMessageInterceptor(callbacks))
        }

        config.interceptors.applicationInterceptorsAfterBuiltIn.forEach { builder.addInterceptor(it) }

        if (loggingConfig.enabled) {
            val logInterceptor: Interceptor = loggingConfig.customInterceptor
                ?: HttpLoggingInterceptor().apply {
                    level = loggingConfig.defaultLevel
                }
            builder.addInterceptor(logInterceptor)
        }

        return builder.build()
    }

    fun create(
        callbacks: NetworkCallbacks,
        config: RetrofitFactoryConfig,
        client: OkHttpClient = createOkHttpClient(callbacks, config),
    ): Retrofit {
        require(config.baseUrl.endsWith("/")) { "baseUrl must end with '/'" }
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(client)

        config.converterFactories.forEach { retrofitBuilder.addConverterFactory(it) }

        if (config.useDefaultGsonConverter) {
            retrofitBuilder.addConverterFactory(GsonConverterFactory.create(config.gson))
        } else if (config.converterFactories.isEmpty()) {
            error("useDefaultGsonConverter=false 时需至少提供一个 converterFactories")
        }

        return retrofitBuilder.build()
    }
}
