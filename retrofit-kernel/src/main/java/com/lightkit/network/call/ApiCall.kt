package com.lightkit.network.call

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.lightkit.network.api.ApiResponseWrapper
import com.lightkit.network.api.ApiRowsPage
import com.lightkit.network.api.ApiRowsResponse
import com.lightkit.network.api.NoDataResponse
import com.lightkit.network.callback.NetworkCallbacks
import com.lightkit.network.exception.ApiException
import com.lightkit.network.json.JsonResponseParser
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/**
 * 统一 Retrofit 调用管道：HTTP 成功 → 业务 code → [Result]。
 *
 * - 强类型：[call] / [callRows]
 * - 遗留 JsonObject：[callJson] + [JsonResponseParser]
 */
class ApiCall(
    private val callbacks: NetworkCallbacks,
    private val defaultLogTag: String = "ApiCall",
) {

    suspend fun <T, R : ApiResponseWrapper<T>> call(
        options: JsonCallOptions = JsonCallOptions(),
        apiCall: suspend () -> Response<R>,
    ): Result<T> = runCatchingPipeline(options) {
        val response = apiCall()
        if (!response.isSuccessful) {
            val errorMsg = callbacks.resolveErrorMessage(response.code(), response.message())
            if (options.notifyGlobalError) callbacks.onBusinessError(errorMsg)
            return@runCatchingPipeline Result.failure(ApiException.NetworkError(errorMsg))
        }
        val body = response.body()
            ?: return@runCatchingPipeline Result.failure(
                Exception(callbacks.resolveErrorMessage(-1, "Response body is null"))
            )
        handleBusinessBody(body.code, body.msg, options) {
            val data = body.data
            if (data != null) {
                @Suppress("UNCHECKED_CAST")
                Result.success(data as T)
            } else if (body is NoDataResponse) {
                @Suppress("UNCHECKED_CAST")
                Result.success(Unit as T)
            } else if (options.allowNullData) {
                @Suppress("UNCHECKED_CAST")
                Result.success(null as T)
            } else {
                val errorMsg = callbacks.resolveErrorMessage(body.code ?: -1, body.msg)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    suspend fun <T, R : ApiRowsResponse<T>> callRows(
        options: JsonCallOptions = JsonCallOptions(),
        apiCall: suspend () -> Response<R>,
    ): Result<ApiRowsPage<T>> = runCatchingPipeline(options) {
        val response = apiCall()
        if (!response.isSuccessful) {
            val errorMsg = callbacks.resolveErrorMessage(response.code(), response.message())
            if (options.notifyGlobalError) callbacks.onBusinessError(errorMsg)
            return@runCatchingPipeline Result.failure(ApiException.NetworkError(errorMsg))
        }
        val body = response.body()
            ?: return@runCatchingPipeline Result.failure(
                Exception(callbacks.resolveErrorMessage(-1, "Response body is null"))
            )
        handleBusinessBody(body.code, body.msg, options) {
            Result.success(
                ApiRowsPage(
                    total = body.total ?: 0,
                    rows = body.rows.orEmpty(),
                )
            )
        }
    }

    /**
     * 兼容 `Response<JsonObject>` 的老接口；解析后的 [JsonElement] 为 `data` 或 rows 容器。
     */
    suspend fun callJson(
        options: JsonCallOptions = JsonCallOptions(),
        apiCall: suspend () -> Response<JsonObject>,
    ): Result<JsonElement?> = runCatchingPipeline(options) {
        val response = apiCall()
        if (!response.isSuccessful) {
            val errorMsg = callbacks.resolveErrorMessage(response.code(), response.message())
            if (options.notifyGlobalError) callbacks.onBusinessError(errorMsg)
            return@runCatchingPipeline Result.failure(ApiException.NetworkError(errorMsg))
        }
        val body = response.body()
            ?: return@runCatchingPipeline Result.failure(
                Exception(callbacks.resolveErrorMessage(-1, "Response body is null"))
            )
        val code = body.get("code")?.asInt ?: -1
        val msg = body.get("msg")?.asString
        if (code in options.successCodes) {
            Result.success(JsonResponseParser.unwrapData(body))
        } else {
            handleJsonBusinessFailure(code, msg, options.notifyGlobalError)
        }
    }

    suspend fun <T> callJsonMapped(
        options: JsonCallOptions = JsonCallOptions(),
        apiCall: suspend () -> Response<JsonObject>,
        map: (JsonElement?) -> T,
    ): Result<T> = callJson(options, apiCall).map(map)

    suspend fun <T, R : ApiResponseWrapper<T>> callSequential(
        apiCalls: List<suspend () -> Response<R>>,
    ): List<Result<T>> = apiCalls.map { call(apiCall = it) }

    private inline fun <T> handleBusinessBody(
        code: Int?,
        msg: String?,
        options: JsonCallOptions = JsonCallOptions(),
        onSuccess: () -> Result<T>,
    ): Result<T> {
        val bizCode = code ?: -1
        return when {
            bizCode in options.successCodes -> onSuccess()
            bizCode == 401 -> {
                val errorMsg = callbacks.resolveErrorMessage(bizCode, msg)
                callbacks.onUnauthorized(errorMsg)
                Result.failure(ApiException.Unauthorized(errorMsg))
            }
            bizCode == 500 -> {
                val errorMsg = callbacks.resolveErrorMessage(bizCode, msg)
                if (options.notifyGlobalError) callbacks.onBusinessError(errorMsg)
                Result.failure(ApiException.ServerError(errorMsg))
            }
            else -> {
                val errorMsg = callbacks.resolveErrorMessage(bizCode, msg)
                if (options.notifyGlobalError) callbacks.onBusinessError(errorMsg)
                Result.failure(ApiException.OtherError(bizCode, errorMsg))
            }
        }
    }

    private fun handleJsonBusinessFailure(
        code: Int,
        msg: String?,
        notifyGlobalError: Boolean,
    ): Result<JsonElement?> {
        val errorMsg = callbacks.resolveErrorMessage(code, msg)
        return when (code) {
            401 -> {
                callbacks.onUnauthorized(errorMsg)
                Result.failure(ApiException.Unauthorized(errorMsg))
            }
            500 -> {
                if (notifyGlobalError) callbacks.onBusinessError(errorMsg)
                Result.failure(ApiException.ServerError(errorMsg))
            }
            else -> {
                if (notifyGlobalError) callbacks.onBusinessError(errorMsg)
                Result.failure(ApiException.OtherError(code, errorMsg))
            }
        }
    }

    private suspend fun <T> runCatchingPipeline(
        options: JsonCallOptions = JsonCallOptions(),
        block: suspend () -> Result<T>,
    ): Result<T> {
        val tag = options.logTag ?: defaultLogTag
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is ApiException -> e.message
                else -> callbacks.resolveErrorMessage(-1, e.message ?: "Network error")
            }
            Log.e(tag, "request error: $errorMsg", e)
            if (options.notifyGlobalError && e !is ApiException.Unauthorized) {
                errorMsg?.let { callbacks.onBusinessError(it) }
            }
            Result.failure(e)
        }
    }
}
