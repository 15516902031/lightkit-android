package com.lightkit.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * [PageScaffold] 默认文案与颜色；经 [UiKitTheme.pageScaffold] / [ProvideUiKitTheme] 全局注入。
 */
data class PageScaffoldTheme(
    val containerColor: Color = Color.White,
    val emptyTitle: String = "暂无数据",
    val emptySubtitle: String? = null,
    val errorTitle: String = "加载失败",
    val retryText: String = "重试",
    val loadingMessage: String? = null,
    val loadingIndicatorColor: Color = Color(0xFF36C10B),
    val loadingScrimColor: Color = Color.White.copy(alpha = 0.72f),
    val loadingOverlayScrim: Color = Color.Black.copy(alpha = 0.25f),
    val errorMessageColor: Color = Color(0xFF999999),
    val errorTitleColor: Color = Color(0xFF222222),
    val emptyTitleColor: Color = Color(0xFF111111),
    val emptySubtitleColor: Color = Color(0xFF666666),
    val loadingMessageColor: Color = Color(0xFF666666),
)

data class PageEmptyParams(
    val title: String,
    val subtitle: String?,
    val actionButton: (@Composable () -> Unit)?,
)

data class PageErrorParams(
    val title: String,
    val message: String?,
    val retryText: String,
    val onRetry: (() -> Unit)?,
)

/**
 * 全局替换默认空态模板；未设置时使用 [PageEmpty]。
 */
val LocalPageEmptyContent =
    staticCompositionLocalOf<(@Composable (PageEmptyParams) -> Unit)?> { null }

/**
 * 全局替换默认错误态模板；未设置时使用 [PageError]。
 */
val LocalPageErrorContent =
    staticCompositionLocalOf<(@Composable (PageErrorParams) -> Unit)?> { null }
