package com.lightkit.compose.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 常用间距常量，新项目可在 [UiKitTheme] 中覆盖默认值。 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 18.dp
    val xxl = 20.dp
    val xxxl = 24.dp
    val huge = 32.dp
    val giant = 40.dp
}

@Composable
fun SpacerH(width: Dp = Spacing.xl) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun SpacerV(height: Dp = Spacing.xxl) {
    Spacer(modifier = Modifier.height(height))
}
