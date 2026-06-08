package com.lightkit.compose.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

/** 紧凑文本样式（关闭 includeFontPadding，lineHeight = fontSize）。 */
fun compactTextStyle(
    fontSize: TextUnit,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    brush: Brush? = null,
): TextStyle {
    val platformStyle = PlatformTextStyle(includeFontPadding = false)
    return if (brush != null) {
        TextStyle(
            brush = brush,
            fontSize = fontSize,
            lineHeight = fontSize,
            fontWeight = fontWeight,
            platformStyle = platformStyle,
        )
    } else {
        TextStyle(
            color = color,
            fontSize = fontSize,
            lineHeight = fontSize,
            fontWeight = fontWeight,
            platformStyle = platformStyle,
        )
    }
}
