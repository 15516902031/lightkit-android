package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun SearchClearInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLength: Int? = null,
    valueFilter: ((String) -> String)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingContent: (@Composable () -> Unit)? = null,
    clearContent: (@Composable (onClear: () -> Unit) -> Unit)? = null,
    showClearWhenNotEmpty: Boolean = true,
    iconSize: Dp = 16.dp,
    textStyle: TextStyle? = null,
    placeholderTextStyle: TextStyle? = null,
    shape: Shape = RoundedCornerShape(21.dp),
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    minHeight: Dp = 42.dp,
) {
    val theme = LocalUiKitTheme.current
    val resolvedTextStyle = textStyle ?: LocalTextStyle.current.merge(
        compactTextStyle(fontSize = 16.sp, color = theme.searchFieldText),
    )
    val resolvedPlaceholderStyle = placeholderTextStyle
        ?: compactTextStyle(fontSize = 14.sp, color = theme.searchFieldPlaceholder)
    val resolvedContainerColor = containerColor ?: theme.searchFieldBackground

    fun adaptValue(input: String): String {
        val filtered = valueFilter?.invoke(input) ?: input
        return maxLength?.let { filtered.take(it) } ?: filtered
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight)
            .clip(shape)
            .background(resolvedContainerColor)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            leadingContent()
            SpacerH(8.dp)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(adaptValue(it)) },
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
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
        if (showClearWhenNotEmpty && value.isNotEmpty() && clearContent != null) {
            SpacerH(8.dp)
            clearContent {
                if (value.isNotEmpty()) onValueChange("")
            }
        }
    }
}
