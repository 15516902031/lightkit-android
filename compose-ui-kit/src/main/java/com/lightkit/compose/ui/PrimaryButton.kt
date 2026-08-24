package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 按钮背景；默认走 [UiKitTheme] 主色渐变。 */
sealed interface ButtonBackground {
    /** [UiKitTheme.primaryGradientStart] → [UiKitTheme.primaryGradientEnd] 水平渐变 */
    data object PrimaryGradient : ButtonBackground

    data class Solid(val color: Color) : ButtonBackground

    data class Gradient(val brush: Brush) : ButtonBackground

    data class HorizontalGradient(val colors: List<Color>) : ButtonBackground
}

/**
 * 主题化主按钮：全宽 CTA、提交 [loading]、disabled 半透明。
 *
 * 布局约定：
 * - [modifier] 控制外层（如 `navigationBarsPadding`、`widthIn`）
 * - [horizontalInset] 在 [fillMaxWidth] 内侧的水平留白
 * - [height] 固定按钮高度
 *
 * 与 [PrimaryGradientButtonTextBox]（无点击）和 [GradientPillButton]（Row 内轻量胶囊）互补。
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    background: ButtonBackground = ButtonBackground.PrimaryGradient,
    shape: Shape = CircleShape,
    height: Dp? = null,
    horizontalInset: Dp? = null,
    textStyle: TextStyle? = null,
    loadingIndicatorSize: Dp = 22.dp,
    loadingStrokeWidth: Dp = 2.dp,
) {
    val theme = LocalUiKitTheme.current
    PrimaryButtonBox(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        background = background,
        shape = shape,
        height = height,
        horizontalInset = horizontalInset,
        loadingIndicatorSize = loadingIndicatorSize,
        loadingStrokeWidth = loadingStrokeWidth,
    ) { isLoading ->
        if (!isLoading) {
            Text(
                text = text,
                style = textStyle ?: compactTextStyle(
                    color = theme.primaryButtonTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/**
 * 主按钮容器版：自定义 content（图标 + 文案等）。
 */
@Composable
fun PrimaryButtonBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    background: ButtonBackground = ButtonBackground.PrimaryGradient,
    shape: Shape = CircleShape,
    height: Dp? = null,
    horizontalInset: Dp? = null,
    loadingIndicatorSize: Dp = 22.dp,
    loadingStrokeWidth: Dp = 2.dp,
    content: @Composable BoxScope.(loading: Boolean) -> Unit,
) {
    val theme = LocalUiKitTheme.current
    val resolvedHeight = height ?: theme.primaryButtonDefaultHeight
    val resolvedInset = horizontalInset ?: theme.primaryButtonHorizontalInset
    val clickEnabled = enabled && !loading

    Box(
        modifier = modifier
            .padding(horizontal = resolvedInset)
            .fillMaxWidth()
            .height(resolvedHeight)
            .clip(shape)
            .applyButtonBackground(background, theme)
            .alpha(if (clickEnabled || loading) 1f else theme.primaryButtonDisabledAlpha)
            .then(
                if (clickEnabled) {
                    Modifier.clickableWithEffect(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(loadingIndicatorSize),
                color = theme.primaryButtonLoadingIndicatorColor,
                strokeWidth = loadingStrokeWidth,
            )
        } else {
            content(false)
        }
    }
}

private fun Modifier.applyButtonBackground(
    background: ButtonBackground,
    theme: UiKitTheme,
): Modifier = when (background) {
    ButtonBackground.PrimaryGradient -> background(
        Brush.horizontalGradient(
            colors = listOf(theme.primaryGradientStart, theme.primaryGradientEnd),
        ),
    )

    is ButtonBackground.Solid -> background(background.color)

    is ButtonBackground.Gradient -> background(background.brush)

    is ButtonBackground.HorizontalGradient -> background(
        Brush.horizontalGradient(colors = background.colors),
    )
}
