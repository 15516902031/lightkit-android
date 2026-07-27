package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用可清空输入框（搜索、表单、多行等场景均可）。
 *
 * - [trailingContent]：常驻尾部（如字数统计），不受清空按钮逻辑影响
 * - [clearContent]：有内容时可选的清空入口
 * - 多行时用 [singleLine]=false + [minLines]/[maxLines] + [contentAlignment]=[Alignment.TopStart]
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
    iconSize: Dp = 16.dp,
    textStyle: TextStyle? = null,
    placeholderTextStyle: TextStyle? = null,
    shape: Shape = RoundedCornerShape(21.dp),
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    minHeight: Dp = 42.dp,
    maxHeight: Dp? = null,
    fillMaxWidth: Boolean = true,
    contentAlignment: Alignment = Alignment.CenterStart,
) {
    val theme = LocalUiKitTheme.current
    val resolvedTextStyle = textStyle ?: LocalTextStyle.current.merge(
        compactTextStyle(fontSize = 16.sp, color = theme.searchFieldText),
    )
    val resolvedPlaceholderStyle = placeholderTextStyle
        ?: compactTextStyle(fontSize = 14.sp, color = theme.searchFieldPlaceholder)
    val resolvedContainerColor = containerColor ?: theme.searchFieldBackground
    val overlayTrailing = trailingContent != null && isOverlayTrailing(trailingAlignment)
    val rowVerticalAlignment = when {
        isTopAlignment(contentAlignment) -> Alignment.Top
        isBottomAlignment(contentAlignment) -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }
    val resolvedMinLines = if (singleLine) 1 else minLines.coerceAtLeast(1)
    val resolvedMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(resolvedMinLines)

    fun adaptValue(input: String): String {
        val filtered = valueFilter?.invoke(input) ?: input
        return maxLength?.let { filtered.take(it) } ?: filtered
    }

    Box(
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .then(
                when {
                    singleLine && maxHeight == null -> Modifier.height(minHeight)
                    maxHeight != null -> Modifier.heightIn(min = minHeight, max = maxHeight)
                    else -> Modifier.heightIn(min = minHeight)
                },
            )
            .clip(shape)
            .background(resolvedContainerColor)
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = rowVerticalAlignment,
        ) {
            if (leadingContent != null) {
                leadingContent()
                SpacerH(8.dp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
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
                        if (value.isBlank() && placeholder.isNotBlank()) {
                            Text(
                                text = placeholder,
                                style = resolvedPlaceholderStyle,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth(),
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
            "showClearWhenNotEmpty, iconSize, textStyle, placeholderTextStyle, shape, " +
            "containerColor, contentPadding, minHeight, maxHeight, fillMaxWidth, contentAlignment)",
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
    iconSize: Dp = 16.dp,
    textStyle: TextStyle? = null,
    placeholderTextStyle: TextStyle? = null,
    shape: Shape = RoundedCornerShape(21.dp),
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    minHeight: Dp = 42.dp,
    maxHeight: Dp? = null,
    fillMaxWidth: Boolean = true,
    contentAlignment: Alignment = Alignment.CenterStart,
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
        iconSize = iconSize,
        textStyle = textStyle,
        placeholderTextStyle = placeholderTextStyle,
        shape = shape,
        containerColor = containerColor,
        contentPadding = contentPadding,
        minHeight = minHeight,
        maxHeight = maxHeight,
        fillMaxWidth = fillMaxWidth,
        contentAlignment = contentAlignment,
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
