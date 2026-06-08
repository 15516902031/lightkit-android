package com.lightkit.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateWheelPickerBottomSheet(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String = "选择日期",
    minYear: Int = 1990,
    maxYear: Int = Calendar.getInstance().get(Calendar.YEAR) + 30,
    confirmText: String = "确定",
) {
    val theme = LocalUiKitTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val years = remember(minYear, maxYear) { (minYear..maxYear).map { "${it}年" } }
    val months = remember { (1..12).map { "${it}月" } }

    val (initialY, initialM, initialD) = remember(initialDate) {
        parseIsoDate(initialDate) ?: Triple(2000, 1, 1)
    }
    var yearIndex by remember {
        mutableIntStateOf((initialY - minYear).coerceIn(0, years.lastIndex))
    }
    var monthIndex by remember {
        mutableIntStateOf((initialM - 1).coerceIn(0, months.lastIndex))
    }
    val selectedYear = minYear + yearIndex
    val selectedMonth = monthIndex + 1
    val maxDay = remember(selectedYear, selectedMonth) { daysInMonth(selectedYear, selectedMonth) }
    var dayIndex by remember {
        mutableIntStateOf((initialD - 1).coerceIn(0, maxDay - 1))
    }
    LaunchedEffect(maxDay) {
        if (dayIndex > maxDay - 1) dayIndex = maxDay - 1
    }
    val days = remember(selectedYear, selectedMonth, maxDay) { (1..maxDay).map { "${it}日" } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = theme.dialogTitleColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "✕",
                    fontSize = 22.sp,
                    color = theme.searchFieldPlaceholder,
                    modifier = Modifier.clickableWithEffect(onClick = onDismiss),
                )
            }
            SpacerV(12.dp)
            WheelPickerRow(modifier = Modifier.fillMaxWidth()) {
                WheelPickerColumn(
                    labels = years,
                    selectedIndex = yearIndex,
                    onSelectedIndexChange = { yearIndex = it },
                    modifier = Modifier.weight(1f),
                )
                WheelPickerColumn(
                    labels = months,
                    selectedIndex = monthIndex,
                    onSelectedIndexChange = { monthIndex = it },
                    modifier = Modifier.weight(1f),
                )
                WheelPickerColumn(
                    labels = days,
                    selectedIndex = dayIndex.coerceIn(0, days.lastIndex.coerceAtLeast(0)),
                    onSelectedIndexChange = { dayIndex = it },
                    modifier = Modifier.weight(1f),
                )
            }
            SpacerV(16.dp)
            PrimaryGradientButtonTextBox(
                text = confirmText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickableWithEffect(
                        onClick = {
                            onConfirm(formatIsoDate(selectedYear, selectedMonth, dayIndex + 1))
                        },
                    ),
                textColor = Color.White,
                textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            )
            SpacerV(12.dp)
        }
    }
}

fun parseIsoDate(raw: String?): Triple<Int, Int, Int>? {
    if (raw.isNullOrBlank()) return null
    val parts = raw.split("-")
    if (parts.size != 3) return null
    return runCatching { Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()) }.getOrNull()
}

fun formatIsoDate(year: Int, month: Int, day: Int): String {
    return "%04d-%02d-%02d".format(year, month, day)
}

fun daysInMonth(year: Int, month: Int): Int {
    return Calendar.getInstance().run {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
