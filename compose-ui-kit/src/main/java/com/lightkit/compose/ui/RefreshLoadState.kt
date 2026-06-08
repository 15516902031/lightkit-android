package com.lightkit.compose.ui

sealed class RefreshLoadState {
    data object Idle : RefreshLoadState()
    data object Refreshing : RefreshLoadState()
    data object LoadingMore : RefreshLoadState()
    data object NoMoreData : RefreshLoadState()
    data class Error(val message: String) : RefreshLoadState()
}
