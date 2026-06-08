package com.lightkit.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal fun LazyListScope.refreshLoadListFooter(
    state: RefreshLoadState,
    noMoreText: String,
    loadingIndicatorColor: Color,
    hintTextColor: Color,
    retrySuffix: String,
    showNoMoreDataItem: Boolean,
    onRetryLoadMore: (() -> Unit)?,
) {
    when (state) {
        is RefreshLoadState.LoadingMore -> {
            item(key = "loading_more") {
                RefreshLoadLoadingRow(loadingIndicatorColor)
            }
        }
        is RefreshLoadState.NoMoreData -> {
            if (showNoMoreDataItem) {
                item(key = "no_more_data") {
                    RefreshLoadHintRow(noMoreText, hintTextColor)
                }
            }
        }
        is RefreshLoadState.Error -> {
            item(key = "error") {
                RefreshLoadErrorRow(
                    message = state.message,
                    hintTextColor = hintTextColor,
                    retrySuffix = retrySuffix,
                    onRetryLoadMore = onRetryLoadMore,
                )
            }
        }
        else -> Unit
    }
}

internal fun LazyGridScope.refreshLoadGridFooter(
    state: RefreshLoadState,
    noMoreText: String,
    loadingIndicatorColor: Color,
    hintTextColor: Color,
    retrySuffix: String,
    showNoMoreDataItem: Boolean,
) {
    when (state) {
        is RefreshLoadState.LoadingMore -> {
            item(key = "loading_more", span = { GridItemSpan(maxLineSpan) }) {
                RefreshLoadLoadingRow(loadingIndicatorColor)
            }
        }
        is RefreshLoadState.NoMoreData -> {
            if (showNoMoreDataItem) {
                item(key = "no_more_data", span = { GridItemSpan(maxLineSpan) }) {
                    RefreshLoadHintRow(noMoreText, hintTextColor)
                }
            }
        }
        is RefreshLoadState.Error -> {
            item(key = "error", span = { GridItemSpan(maxLineSpan) }) {
                RefreshLoadErrorRow(
                    message = state.message,
                    hintTextColor = hintTextColor,
                    retrySuffix = retrySuffix,
                    onRetryLoadMore = null,
                )
            }
        }
        else -> Unit
    }
}

@Composable
private fun RefreshLoadLoadingRow(loadingIndicatorColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = loadingIndicatorColor,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun RefreshLoadHintRow(text: String, hintTextColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = hintTextColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RefreshLoadErrorRow(
    message: String,
    hintTextColor: Color,
    retrySuffix: String,
    onRetryLoadMore: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onRetryLoadMore != null) { onRetryLoadMore?.invoke() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (onRetryLoadMore != null) message + retrySuffix else message,
            fontSize = 12.sp,
            color = hintTextColor,
            textAlign = TextAlign.Center,
        )
    }
}
