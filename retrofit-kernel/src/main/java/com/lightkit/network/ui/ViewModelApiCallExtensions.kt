package com.lightkit.network.ui

import com.lightkit.network.exception.ApiException
import kotlinx.coroutines.flow.MutableStateFlow

suspend fun <T, S : LoadingErrorState> callResultWithState(
    uiState: MutableStateFlow<S>,
    updateState: (S, Boolean, String?) -> S,
    showLoading: Boolean = true,
    apiRequest: suspend () -> Result<T>,
    onSuccess: suspend (T) -> Unit,
    onUnauthorized: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
) {
    if (showLoading) {
        uiState.value = updateState(uiState.value, true, null)
    }

    val result = apiRequest()
    val exception = result.exceptionOrNull()
    val errMsg = exception?.message

    result.fold(
        onSuccess = { data ->
            if (showLoading) {
                uiState.value = updateState(uiState.value, false, null)
            }
            onSuccess(data)
        },
        onFailure = {
            if (exception is ApiException.Unauthorized) {
                onUnauthorized?.invoke()
            } else if (errMsg != null) {
                onError?.invoke(errMsg)
            }
            if (showLoading) {
                uiState.value = updateState(uiState.value, false, errMsg)
            }
        },
    )
}
