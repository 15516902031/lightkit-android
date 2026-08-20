package com.lightkit.compose.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppIcon(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    /** 为 null 时完全由 [modifier] 控制尺寸（例如自定义 `Modifier.size` / `fillMaxSize`）。 */
    size: Dp? = 22.dp,
    tint: Color = Color.Unspecified,
) {
    Icon(
        painter = painterResource(id = resId),
        contentDescription = null,
        tint = tint,
        modifier = if (size != null) modifier.size(size) else modifier,
    )
}

@Composable
fun AppIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    /** 为 null 时完全由 [modifier] 控制尺寸（例如自定义 `Modifier.size` / `fillMaxSize`）。 */
    size: Dp? = 22.dp,
    tint: Color = Color.Unspecified,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = if (size != null) modifier.size(size) else modifier,
    )
}
