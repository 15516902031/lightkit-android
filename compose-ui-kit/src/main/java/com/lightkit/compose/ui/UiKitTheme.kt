package com.lightkit.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 可复用 UI 组件的默认文案与颜色；业务 App 在根节点通过 [ProvideUiKitTheme] 覆盖。
 */
data class UiKitTheme(
    val loadMoreNoMoreText: String = "没有更多了",
    val loadMoreRetrySuffix: String = "，点击重试",
    val loadMoreIndicatorColor: Color = Color(0xFF36C10B),
    val loadMoreHintColor: Color = Color(0xFF666666),
    val tabPagerDividerColor: Color = Color(0xFFF7F8FA),
    val emptyStateTitleColor: Color = Color(0xFF111111),
    val emptyStateSubtitleColor: Color = Color(0xFF666666),
    val topBarBackgroundColor: Color = Color.White,
    val topBarContentColor: Color = Color.Black,
    val primaryGradientStart: Color = Color(0xFFFF97EF),
    val primaryGradientEnd: Color = Color(0xFFFF65E8),
    val secondaryButtonBackground: Color = Color(0xFFF7F8FB),
    val secondaryButtonText: Color = Color(0xFF24292C),
    val searchFieldBackground: Color = Color(0xFFF3F3F7),
    val searchFieldText: Color = Color(0xFF24292C),
    val searchFieldPlaceholder: Color = Color(0xFF999999),
    val settingsTitleColor: Color = Color(0xFF181818),
    val settingsTipColor: Color = Color(0xFF999999),
    val switchCheckedTrackColor: Color = Color(0xFF6EEF7F),
    val switchUncheckedTrackColor: Color = Color(0xFFE8E8E8),
    val dialogTitleColor: Color = Color(0xFF24292C),
    val dialogMessageColor: Color = Color(0xFF666666),
    val lockedOverlayScrim: Color = Color.Black.copy(alpha = 0.5f),
    val imagePickerStrings: ImagePickerStrings = ImagePickerStrings(),
    val imagePickerAccentColor: Color = Color(0xFF36C10B),
    val imagePickerTitleColor: Color = Color(0xFF333333),
    val imagePickerBodyColor: Color = Color(0xFF666666),
    val imagePickerMutedColor: Color = Color(0xFF999999),
    val imagePickerOptionDividerColor: Color = Color(0xFFF1F1F1),
    /** 二级页 [PageScaffold] 默认样式与文案 */
    val pageScaffold: PageScaffoldTheme = PageScaffoldTheme(),
)

val LocalUiKitTheme = staticCompositionLocalOf { UiKitTheme() }

@Composable
fun ProvideUiKitTheme(
    theme: UiKitTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalUiKitTheme provides theme, content = content)
}
