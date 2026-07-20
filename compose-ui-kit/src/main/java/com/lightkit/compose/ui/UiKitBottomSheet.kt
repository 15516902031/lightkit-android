package com.lightkit.compose.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LightKit 底部弹窗统一入口：关闭手势下滑关闭，与业务侧 BanlvBottomSheet 策略一致。
 *
 * 常用路径不暴露 [SheetState]，避免调用方 OptIn；选图等需外部 state 时用带 [sheetState] 的重载。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiKitBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    UiKitBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = containerColor,
        shape = shape,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberUiKitBottomSheetState(
    skipPartiallyExpanded: Boolean = true,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): SheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = skipPartiallyExpanded,
    confirmValueChange = confirmValueChange,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiKitBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        containerColor = containerColor,
        shape = shape,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = null,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}
