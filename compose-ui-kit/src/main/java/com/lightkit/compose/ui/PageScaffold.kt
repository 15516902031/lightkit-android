package com.lightkit.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * 二级独立页基类：Scaffold + 默认 [CommonTopBar] + 可选 bottomBar +
 * Loading / Empty / Error / Content 统一托管。
 *
 * 全局默认经 [UiKitTheme.pageScaffold] / [LocalPageEmptyContent] / [LocalPageErrorContent] 注入；
 * 单页可通过 [emptyContent] / [errorContent] / [loadingContent] / [topBar] 覆盖。
 */
@Composable
fun PageScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    showTopBar: Boolean = true,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    containerColor: Color? = null,
    topBarBackgroundColor: Color? = null,
    topBarContentColor: Color? = null,
    topBarTitleTextStyle: TextStyle? = null,
    topBarBackIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    topBarNavigationIcon: (@Composable () -> Unit)? = null,
    topBarHeight: Dp = 44.dp,
    state: PageUiState = PageUiState.Content,
    blockingLoading: Boolean = false,
    onRetry: (() -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (String?) -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val theme = LocalUiKitTheme.current
    val pageTheme = theme.pageScaffold
    val snackbarHostState = remember { SnackbarHostState() }
    val resolvedContainer = containerColor ?: pageTheme.containerColor

    Scaffold(
        modifier = modifier,
        containerColor = resolvedContainer,
        topBar = {
            when {
                !showTopBar -> Unit
                topBar != null -> topBar()
                else -> CommonTopBar(
                    title = title,
                    onBackClick = onBackClick,
                    actions = actions,
                    backgroundColor = topBarBackgroundColor ?: theme.topBarBackgroundColor,
                    contentColor = topBarContentColor ?: theme.topBarContentColor,
                    titleTextStyle = topBarTitleTextStyle,
                    backIcon = topBarBackIcon,
                    navigationIcon = topBarNavigationIcon,
                    height = topBarHeight,
                )
            }
        },
        bottomBar = {
            if (bottomBar != null) {
                Box(modifier = Modifier.navigationBarsPadding()) {
                    bottomBar()
                }
            }
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = { snackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                PageUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    ) {
                        if (loadingContent != null) {
                            loadingContent()
                        } else {
                            PageLoadingContent(message = pageTheme.loadingMessage)
                        }
                    }
                }
                is PageUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    ) {
                        if (emptyContent != null) {
                            emptyContent()
                        } else {
                            PageEmpty(
                                title = s.title,
                                subtitle = s.subtitle,
                            )
                        }
                    }
                }
                is PageUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    ) {
                        if (errorContent != null) {
                            errorContent(s.message)
                        } else {
                            PageError(
                                message = s.message,
                                onRetry = onRetry,
                            )
                        }
                    }
                }
                PageUiState.Content -> content(padding)
            }
            PageBlockingLoading(
                visible = blockingLoading,
                message = pageTheme.loadingMessage,
            )
        }
    }
}

/**
 * VM 友好重载：内部 [resolvePageUiState]。
 */
@Composable
fun PageScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    loading: Boolean,
    hasContent: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    showTopBar: Boolean = true,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    containerColor: Color? = null,
    topBarBackgroundColor: Color? = null,
    topBarContentColor: Color? = null,
    topBarTitleTextStyle: TextStyle? = null,
    topBarBackIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    topBarNavigationIcon: (@Composable () -> Unit)? = null,
    topBarHeight: Dp = 44.dp,
    blockingLoading: Boolean = false,
    onRetry: (() -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (String?) -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    PageScaffold(
        title = title,
        onBackClick = onBackClick,
        modifier = modifier,
        actions = actions,
        showTopBar = showTopBar,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        topBarBackgroundColor = topBarBackgroundColor,
        topBarContentColor = topBarContentColor,
        topBarTitleTextStyle = topBarTitleTextStyle,
        topBarBackIcon = topBarBackIcon,
        topBarNavigationIcon = topBarNavigationIcon,
        topBarHeight = topBarHeight,
        state = resolvePageUiState(
            loading = loading,
            hasContent = hasContent,
            errorMessage = errorMessage,
            isEmpty = isEmpty,
        ),
        blockingLoading = blockingLoading,
        onRetry = onRetry,
        emptyContent = emptyContent,
        errorContent = errorContent,
        loadingContent = loadingContent,
        content = content,
    )
}
