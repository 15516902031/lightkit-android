package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 渐变方向。
 */
enum class GradientDirection {
    Horizontal,
    Vertical,
}

/**
 * 通用 ButtonBox：适用于按钮、Tab、标签等「Box + Text」场景。
 *
 * - 支持纯色/渐变背景
 * - 支持横向或竖向渐变
 * - 支持任意数量颜色以及可选 colorStops
 */
@Composable
fun ButtonBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    solidColor: Color? = null,
    gradientColors: List<Color> = emptyList(),
    colorStops: List<Pair<Float, Color>>? = null,
    gradientDirection: GradientDirection = GradientDirection.Horizontal,
    disabledAlpha: Float = 0.6f,
    minWidth: Dp = 0.dp,
    minHeight: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    val brush = when {
        !colorStops.isNullOrEmpty() -> when (gradientDirection) {
            GradientDirection.Horizontal -> Brush.horizontalGradient(colorStops = colorStops.toTypedArray())
            GradientDirection.Vertical -> Brush.verticalGradient(colorStops = colorStops.toTypedArray())
        }

        gradientColors.isNotEmpty() -> when (gradientDirection) {
            GradientDirection.Horizontal -> Brush.horizontalGradient(colors = gradientColors)
            GradientDirection.Vertical -> Brush.verticalGradient(colors = gradientColors)
        }

        solidColor != null -> Brush.linearGradient(colors = listOf(solidColor, solidColor))
        else -> Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .clip(shape)
            .background(brush = brush)
            .alpha(if (enabled) 1f else disabledAlpha)
            .padding(contentPadding),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

/**
 * 文本按钮便捷封装：用于最常见的「Box + Text」形式。
 */
@Composable
fun ButtonTextBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    solidColor: Color? = null,
    gradientColors: List<Color> = emptyList(),
    colorStops: List<Pair<Float, Color>>? = null,
    gradientDirection: GradientDirection = GradientDirection.Horizontal,
    disabledAlpha: Float = 0.6f,
    minWidth: Dp = 0.dp,
    minHeight: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
    textColor: Color = Color.White,
    textStyle: TextStyle = TextStyle.Default,
    contentAlignment: Alignment = Alignment.Center,
) {
    ButtonBox(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        solidColor = solidColor,
        gradientColors = gradientColors,
        colorStops = colorStops,
        gradientDirection = gradientDirection,
        disabledAlpha = disabledAlpha,
        minWidth = minWidth,
        minHeight = minHeight,
        contentPadding = contentPadding,
        contentAlignment = contentAlignment,
    ) {
        Text(text = text, color = textColor, style = textStyle)
    }
}

/**
 * 项目常用粉紫渐变按钮（文本版）。
 */
@Composable
fun PrimaryGradientButtonTextBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
    textColor: Color = Color.White,
    textStyle: TextStyle = TextStyle.Default,
) {
    val theme = LocalUiKitTheme.current
    ButtonTextBox(
        text = text,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        gradientColors = listOf(theme.primaryGradientStart, theme.primaryGradientEnd),
        gradientDirection = GradientDirection.Horizontal,
        contentPadding = contentPadding,
        textColor = textColor,
        textStyle = textStyle,
    )
}

/**
 * 项目常用粉紫渐变按钮（容器版，自定义 content）。
 */
@Composable
fun PrimaryGradientButtonBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val theme = LocalUiKitTheme.current
    ButtonBox(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        gradientColors = listOf(theme.primaryGradientStart, theme.primaryGradientEnd),
        gradientDirection = GradientDirection.Horizontal,
        contentPadding = contentPadding,
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * 渐变胶囊按钮（全宽 Row 内常用 weight(1f)）。
 */
@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush? = null,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    textColor: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp),
) {
    val theme = LocalUiKitTheme.current
    val resolvedGradient = gradient ?: Brush.horizontalGradient(
        listOf(theme.primaryGradientStart, theme.primaryGradientEnd),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(resolvedGradient)
            .clickableWithEffect(onClick = onClick, enabled = enabled)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * 次级灰底文本按钮（常用于取消、次级操作）。
 */
@Composable
fun SecondarySolidButtonTextBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    backgroundColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
    textColor: Color? = null,
    textStyle: TextStyle = TextStyle.Default,
) {
    val theme = LocalUiKitTheme.current
    ButtonTextBox(
        text = text,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        solidColor = backgroundColor ?: theme.secondaryButtonBackground,
        contentPadding = contentPadding,
        textColor = textColor ?: theme.secondaryButtonText,
        textStyle = textStyle,
    )
}

