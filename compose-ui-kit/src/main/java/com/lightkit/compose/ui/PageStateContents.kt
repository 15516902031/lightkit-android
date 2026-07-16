package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 卡片式居中加载（首次进页内容区使用）。
 */
@Composable
fun PageLoading(
    modifier: Modifier = Modifier,
    message: String? = null,
    scrimColor: Color? = null,
    indicatorColor: Color? = null,
    messageColor: Color? = null,
) {
    val theme = LocalUiKitTheme.current.pageScaffold
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(scrimColor ?: theme.loadingScrimColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = indicatorColor ?: theme.loadingIndicatorColor,
                strokeWidth = 3.dp,
            )
            val text = message ?: theme.loadingMessage
            if (!text.isNullOrBlank()) {
                SpacerV(12.dp)
                Text(
                    text = text,
                    style = compactTextStyle(
                        fontSize = 14.sp,
                        color = messageColor ?: theme.loadingMessageColor,
                    ),
                )
            }
        }
    }
}

/** 内容区居中展示 [PageLoading]。 */
@Composable
fun PageLoadingContent(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PageLoading(message = message)
    }
}

/**
 * 全屏半透明遮罩 + 居中 loading（提交/支付等阻塞操作）。
 */
@Composable
fun PageBlockingLoading(
    visible: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    if (!visible) return
    val theme = LocalUiKitTheme.current.pageScaffold
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.loadingOverlayScrim),
        contentAlignment = Alignment.Center,
    ) {
        PageLoading(message = message)
    }
}

/** 在 [Box] 内叠加首次进页加载层。 */
@Composable
fun BoxScope.PageInitialLoading(
    loading: Boolean,
    hasContent: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    if (!isInitialPageLoading(loading, hasContent)) return
    val theme = LocalUiKitTheme.current.pageScaffold
    Box(
        modifier = modifier
            .matchParentSize()
            .background(theme.loadingScrimColor.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        PageLoading(message = message)
    }
}

/**
 * 默认空数据页（基于 [EmptyState]，读 [PageScaffoldTheme]）。
 */
@Composable
fun PageEmpty(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    actionButton: (@Composable () -> Unit)? = null,
) {
    val theme = LocalUiKitTheme.current.pageScaffold
    val params = PageEmptyParams(
        title = title ?: theme.emptyTitle,
        subtitle = subtitle ?: theme.emptySubtitle,
        actionButton = actionButton,
    )
    val custom = LocalPageEmptyContent.current
    if (custom != null) {
        Box(modifier = modifier.fillMaxSize()) {
            custom(params)
        }
        return
    }
    EmptyState(
        icon = icon,
        title = params.title,
        subtitle = params.subtitle,
        actionButton = params.actionButton,
        modifier = modifier,
        titleColor = theme.emptyTitleColor,
        subtitleColor = theme.emptySubtitleColor,
    )
}

/**
 * 默认错误内容页。
 */
@Composable
fun PageError(
    message: String?,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String? = null,
    retryText: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val theme = LocalUiKitTheme.current.pageScaffold
    val params = PageErrorParams(
        title = title ?: theme.errorTitle,
        message = message?.takeIf { it.isNotBlank() },
        retryText = retryText ?: theme.retryText,
        onRetry = onRetry,
    )
    val custom = LocalPageErrorContent.current
    if (custom != null) {
        Box(modifier = modifier.fillMaxSize()) {
            custom(params)
        }
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.invoke()
        if (icon != null) {
            SpacerV(24.dp)
        }
        Text(
            text = params.title,
            style = compactTextStyle(
                fontSize = 16.sp,
                color = theme.errorTitleColor,
                fontWeight = FontWeight.Medium,
            ),
            textAlign = TextAlign.Center,
        )
        if (!params.message.isNullOrBlank()) {
            SpacerV(8.dp)
            Text(
                text = params.message,
                style = compactTextStyle(fontSize = 14.sp, color = theme.errorMessageColor),
                textAlign = TextAlign.Center,
            )
        }
        if (params.onRetry != null) {
            SpacerV(24.dp)
            GradientPillButton(
                text = params.retryText,
                onClick = params.onRetry,
                modifier = Modifier.fillMaxWidth(0.55f),
                shape = CircleShape,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            )
        }
    }
}
