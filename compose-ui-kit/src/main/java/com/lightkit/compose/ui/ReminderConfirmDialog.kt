package com.lightkit.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ReminderConfirmDialog(
    title: String = "提醒",
    message: String,
    cancelText: String = "我再想想",
    confirmText: String = "确定",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit = onCancel,
) {
    val theme = LocalUiKitTheme.current
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp)
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SpacerV(22.dp)
            Text(
                text = title,
                style = compactTextStyle(fontSize = 18.sp, color = theme.dialogTitleColor),
            )
            SpacerV(20.dp)
            Text(
                text = message,
                style = compactTextStyle(fontSize = 15.sp, color = theme.dialogMessageColor),
                textAlign = TextAlign.Center,
            )
            SpacerV(37.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SecondarySolidButtonTextBox(
                    text = cancelText,
                    textStyle = TextStyle(fontSize = 16.sp),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickableWithEffect(onClick = onCancel),
                )
                PrimaryGradientButtonTextBox(
                    text = confirmText,
                    textStyle = TextStyle(fontSize = 16.sp),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickableWithEffect(onClick = onConfirm),
                )
            }
            SpacerV(16.dp)
        }
    }
}
