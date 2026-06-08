package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommonTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    titleTextStyle: TextStyle? = null,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIcon: (@Composable () -> Unit)? = null,
    height: Dp = 44.dp,
) {
    val theme = LocalUiKitTheme.current
    val bg = backgroundColor ?: theme.topBarBackgroundColor
    val fg = contentColor ?: theme.topBarContentColor

    Box(
        modifier = modifier
            .background(bg)
            .statusBarsPadding()
            .fillMaxWidth()
            .height(height),
    ) {
        Text(
            text = title,
            style = titleTextStyle ?: TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = fg,
            ),
            modifier = Modifier.align(Alignment.Center),
        )
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(height),
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                } else {
                    Icon(
                        imageVector = backIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = fg,
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}
