package com.lightkit.compose.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** 底部键盘 + 导航栏安全区（取 ime 与 navigationBars 的并集，避免双倍 padding）。 */
@Composable
fun Modifier.keyboardBottomInsetsPadding(): Modifier =
    windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
