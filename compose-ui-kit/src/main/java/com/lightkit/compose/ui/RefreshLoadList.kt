package com.lightkit.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshLoadList(
    modifier: Modifier = Modifier,
    state: RefreshLoadState = RefreshLoadState.Idle,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onRetryLoadMore: (() -> Unit)? = null,
    showNoMoreDataItem: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    noMoreText: String? = null,
    loadingIndicatorColor: Color? = null,
    hintTextColor: Color? = null,
    content: LazyListScope.() -> Unit,
) {
    val theme = LocalUiKitTheme.current
    val resolvedNoMoreText = noMoreText ?: theme.loadMoreNoMoreText
    val resolvedLoadingColor = loadingIndicatorColor ?: theme.loadMoreIndicatorColor
    val resolvedHintColor = hintTextColor ?: theme.loadMoreHintColor

    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val lastVisibleItem = visibleItems.lastOrNull()
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
        }
    }

    LaunchedEffect(isAtBottom, state) {
        if (isAtBottom && state == RefreshLoadState.Idle) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        state = pullToRefreshState,
        isRefreshing = state == RefreshLoadState.Refreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
        ) {
            content()
            refreshLoadListFooter(
                state = state,
                noMoreText = resolvedNoMoreText,
                loadingIndicatorColor = resolvedLoadingColor,
                hintTextColor = resolvedHintColor,
                retrySuffix = theme.loadMoreRetrySuffix,
                showNoMoreDataItem = showNoMoreDataItem,
                onRetryLoadMore = onRetryLoadMore,
            )
        }
    }
}
