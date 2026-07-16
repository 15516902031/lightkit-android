package com.lightkit.compose.ui

/**
 * 二级页内容区统一状态（与列表 [RefreshLoadState] 分工：有内容时的刷新不切到 [Loading]）。
 */
sealed interface PageUiState {
    data object Content : PageUiState

    /** 首次进页：内容区替换为 loading */
    data object Loading : PageUiState

    data class Empty(
        val title: String? = null,
        val subtitle: String? = null,
    ) : PageUiState

    data class Error(
        val message: String? = null,
    ) : PageUiState
}

/**
 * 由 VM 字段推导页面状态。
 *
 * 优先级：Error（无内容）→ Loading（无内容）→ Empty（无内容）→ Content。
 * 有内容时即使 [loading]/true 也返回 [PageUiState.Content]，留给列表自身刷新指示。
 */
fun resolvePageUiState(
    loading: Boolean,
    hasContent: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
): PageUiState {
    val error = errorMessage?.trim().orEmpty()
    if (error.isNotEmpty() && !hasContent) {
        return PageUiState.Error(message = error)
    }
    if (loading && !hasContent) {
        return PageUiState.Loading
    }
    if (isEmpty && !hasContent) {
        return PageUiState.Empty()
    }
    return PageUiState.Content
}

/** 是否为页面首次进入加载（有内容后的刷新请返回 false）。 */
fun isInitialPageLoading(loading: Boolean, hasContent: Boolean): Boolean =
    loading && !hasContent
