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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.LockedOverlay(
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
    scrimColor: androidx.compose.ui.graphics.Color? = null,
) {
    val theme = LocalUiKitTheme.current
    Box(
        modifier = modifier
            .matchParentSize()
            .clip(RoundedCornerShape(cornerRadius))
            .background(scrimColor ?: theme.lockedOverlayScrim),
        contentAlignment = Alignment.Center,
    ) {
        iconContent()
    }
}
