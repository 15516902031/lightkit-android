package com.lightkit.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用可清空输入框（搜索、表单、多行等场景均可）。
 *
 * 外层尺寸 / 背景 / 圆角 / padding 一律由 [modifier] 控制，组件内部不再强制
 * `fillMaxWidth`、固定高度或容器样式。例如：
 * - 全宽搜索条：`Modifier.fillMaxWidth().height(42.dp).clip(...).background(...).padding(...)`
 * - 宽度随内容：`Modifier.wrapContentWidth().height(42.dp).padding(...)`
 *
 * - [trailingContent]：常驻尾部（如字数统计），不受清空按钮逻辑影响
 * - [clearContent]：有内容时可选的清空入口
 * - 多行时用 [singleLine]=false + [minLines]/[maxLines] + [contentAlignment]=[Alignment.TopStart]
 * - [centerContentWhenEmpty]：空态时 leading + 占位文案作为一组水平居中（常见搜索框样式）
 */
@Composable
fun ClearableInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int? = null,
    valueFilter: ((String) -> String)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    /** 尾部对齐；[Alignment.BottomEnd] 等会叠在输入区域角落，适合字数统计。 */
    trailingAlignment: Alignment = Alignment.CenterEnd,
    clearContent: (@Composable (onClear: () -> Unit) -> Unit)? = null,
    showClearWhenNotEmpty: Boolean = true,
    textStyle: TextStyle? = null,
    placeholderTextStyle: TextStyle? = null,
    contentAlignment: Alignment = Alignment.CenterStart,
    centerContentWhenEmpty: Boolean = false,
) {
    val theme = LocalUiKitTheme.current
    val resolvedTextStyle = textStyle ?: LocalTextStyle.current.merge(
        compactTextStyle(fontSize = 16.sp, color = theme.searchFieldText),
    )
    val resolvedPlaceholderStyle = placeholderTextStyle
        ?: compactTextStyle(fontSize = 14.sp, color = theme.searchFieldPlaceholder)
    val overlayTrailing = trailingContent != null && isOverlayTrailing(trailingAlignment)
    val rowVerticalAlignment = when {
        isTopAlignment(contentAlignment) -> Alignment.Top
        isBottomAlignment(contentAlignment) -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }
    val resolvedMinLines = if (singleLine) 1 else minLines.coerceAtLeast(1)
    val resolvedMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(resolvedMinLines)
    val centerEmpty = centerContentWhenEmpty && value.isBlank()

    fun adaptValue(input: String): String {
        val filtered = valueFilter?.invoke(input) ?: input
        return maxLength?.let { filtered.take(it) } ?: filtered
    }

    BoxWithConstraints(modifier = modifier) {
        // 仅在外层给出固定宽/高时再撑满，避免写死 fillMaxWidth 导致无法 wrapContent
        val expandWidth = constraints.hasFixedWidth
        val expandHeight = constraints.hasFixedHeight

        if (centerEmpty) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    SpacerH(8.dp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(adaptValue(it)) },
                    enabled = enabled,
                    readOnly = readOnly,
                    singleLine = singleLine,
                    minLines = resolvedMinLines,
                    maxLines = resolvedMaxLines,
                    textStyle = resolvedTextStyle,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    decorationBox = { innerTextField ->
                        Box {
                            if (placeholder.isNotBlank()) {
                                Text(
                                    text = placeholder,
                                    style = resolvedPlaceholderStyle,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .then(if (expandWidth) Modifier.fillMaxWidth() else Modifier)
                    .then(if (expandHeight) Modifier.fillMaxHeight() else Modifier)
                    .align(Alignment.CenterStart),
                verticalAlignment = rowVerticalAlignment,
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    SpacerH(8.dp)
                }
                Box(
                    modifier = Modifier
                        .then(if (expandWidth) Modifier.weight(1f) else Modifier)
                        .then(if (expandHeight) Modifier.fillMaxHeight() else Modifier)
                        .padding(
                            end = if (overlayTrailing) 8.dp else 0.dp,
                            bottom = if (overlayTrailing && isBottomAlignment(trailingAlignment)) {
                                18.dp
                            } else {
                                0.dp
                            },
                        ),
                    contentAlignment = contentAlignment,
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = { onValueChange(adaptValue(it)) },
                        enabled = enabled,
                        readOnly = readOnly,
                        singleLine = singleLine,
                        minLines = resolvedMinLines,
                        maxLines = resolvedMaxLines,
                        textStyle = resolvedTextStyle,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        decorationBox = { innerTextField ->
                            // 占位与输入同起点叠放，避免 CenterStart 导致占位垂直居中、正文贴顶错位
                            Box(
                                modifier = if (expandWidth) Modifier.fillMaxWidth() else Modifier,
                            ) {
                                if (value.isBlank() && placeholder.isNotBlank()) {
                                    Text(
                                        text = placeholder,
                                        style = resolvedPlaceholderStyle,
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = if (expandWidth) Modifier.fillMaxWidth() else Modifier,
                    )
                }
                if (trailingContent != null && !overlayTrailing) {
                    SpacerH(8.dp)
                    trailingContent()
                }
                if (showClearWhenNotEmpty && value.isNotEmpty() && clearContent != null) {
                    SpacerH(8.dp)
                    clearContent {
                        if (value.isNotEmpty()) onValueChange("")
                    }
                }
            }
        }
        if (trailingContent != null && overlayTrailing) {
            Box(modifier = Modifier.align(trailingAlignment)) {
                trailingContent()
            }
        }
    }
}

/**
 * @deprecated 已更名为 [ClearableInputField]，请迁移调用方。
 */
@Deprecated(
    message = "Renamed to ClearableInputField",
    replaceWith = ReplaceWith(
        expression = "ClearableInputField(" +
            "value, onValueChange, modifier, placeholder, enabled, readOnly, singleLine, " +
            "minLines, maxLines, maxLength, valueFilter, keyboardOptions, keyboardActions, " +
            "leadingContent, trailingContent, trailingAlignment, clearContent, " +
            "showClearWhenNotEmpty, textStyle, placeholderTextStyle, " +
            "contentAlignment, centerContentWhenEmpty)",
        imports = ["com.lightkit.compose.ui.ClearableInputField"],
    ),
)
@Composable
fun SearchClearInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int? = null,
    valueFilter: ((String) -> String)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    trailingAlignment: Alignment = Alignment.CenterEnd,
    clearContent: (@Composable (onClear: () -> Unit) -> Unit)? = null,
    showClearWhenNotEmpty: Boolean = true,
    textStyle: TextStyle? = null,
    placeholderTextStyle: TextStyle? = null,
    contentAlignment: Alignment = Alignment.CenterStart,
    centerContentWhenEmpty: Boolean = false,
) {
    ClearableInputField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        maxLength = maxLength,
        valueFilter = valueFilter,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        trailingAlignment = trailingAlignment,
        clearContent = clearContent,
        showClearWhenNotEmpty = showClearWhenNotEmpty,
        textStyle = textStyle,
        placeholderTextStyle = placeholderTextStyle,
        contentAlignment = contentAlignment,
        centerContentWhenEmpty = centerContentWhenEmpty,
    )
}

private fun isTopAlignment(alignment: Alignment): Boolean =
    alignment == Alignment.TopStart ||
        alignment == Alignment.TopCenter ||
        alignment == Alignment.TopEnd

private fun isBottomAlignment(alignment: Alignment): Boolean =
    alignment == Alignment.BottomStart ||
        alignment == Alignment.BottomCenter ||
        alignment == Alignment.BottomEnd

/** 角落对齐走 Box overlay；[Alignment.CenterEnd] 等走 Row 内联。 */
private fun isOverlayTrailing(alignment: Alignment): Boolean =
    isTopAlignment(alignment) || isBottomAlignment(alignment)
