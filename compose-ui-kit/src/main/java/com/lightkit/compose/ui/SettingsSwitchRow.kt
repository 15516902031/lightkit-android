package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsSwitchRow(
    title: String,
    tip: String? = null,
    tipTextStyle: TextStyle? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color? = null,
    switchCheckedTrackColor: Color? = null,
    switchUncheckedTrackColor: Color? = null,
) {
    val theme = LocalUiKitTheme.current
    val resolvedTitleColor = titleColor ?: theme.settingsTitleColor
    val resolvedTipStyle = tipTextStyle ?: compactTextStyle(fontSize = 9.sp, color = theme.settingsTipColor)

    Row(
        // 设置行默认占满行宽；标题区 weight 需要有界宽度
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = resolvedTitleColor,
                fontWeight = FontWeight.Normal,
            )
            tip?.let {
                Text(text = it, style = resolvedTipStyle)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = switchCheckedTrackColor ?: theme.switchCheckedTrackColor,
                uncheckedTrackColor = switchUncheckedTrackColor ?: theme.switchUncheckedTrackColor,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
            thumbContent = {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            },
            modifier = Modifier.scale(0.718f),
        )
    }
}
