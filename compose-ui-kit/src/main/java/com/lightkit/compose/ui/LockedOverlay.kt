package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.LockedOverlay(
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    /** 大于 0 时裁剪圆角；需要其它 shape 时请直接在 [modifier] 上 `clip`。 */
    cornerRadius: Dp = 0.dp,
    scrimColor: Color? = null,
) {
    val theme = LocalUiKitTheme.current
    Box(
        modifier = modifier
            .matchParentSize()
            .then(
                if (cornerRadius > 0.dp) {
                    Modifier.clip(RoundedCornerShape(cornerRadius))
                } else {
                    Modifier
                },
            )
            .background(scrimColor ?: theme.lockedOverlayScrim),
        contentAlignment = Alignment.Center,
    ) {
        iconContent()
    }
}
