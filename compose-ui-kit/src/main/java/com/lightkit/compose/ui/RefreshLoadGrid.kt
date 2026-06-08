package com.lightkit.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
fun RefreshLoadGrid(
    modifier: Modifier = Modifier,
    columns: GridCells,
    state: RefreshLoadState = RefreshLoadState.Idle,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    showNoMoreDataItem: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    noMoreText: String? = null,
    loadingIndicatorColor: Color? = null,
    hintTextColor: Color? = null,
    content: LazyGridScope.() -> Unit,
) {
    val theme = LocalUiKitTheme.current
    val resolvedNoMoreText = noMoreText ?: theme.loadMoreNoMoreText
    val resolvedLoadingColor = loadingIndicatorColor ?: theme.loadMoreIndicatorColor
    val resolvedHintColor = hintTextColor ?: theme.loadMoreHintColor

    val gridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
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
        LazyVerticalGrid(
            state = gridState,
            columns = columns,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalArrangement = verticalArrangement,
        ) {
            content()
            refreshLoadGridFooter(
                state = state,
                noMoreText = resolvedNoMoreText,
                loadingIndicatorColor = resolvedLoadingColor,
                hintTextColor = resolvedHintColor,
                retrySuffix = theme.loadMoreRetrySuffix,
                showNoMoreDataItem = showNoMoreDataItem,
            )
        }
    }
}
