package com.lightkit.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.abs

private val DefaultSelectionBarColor = Color(0xFFF7F8FB)
private val DefaultLabelColor = Color(0xFF181818)

@Composable
fun WheelPickerRow(
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    visibleCount: Int = 5,
    selectionBarColor: Color = DefaultSelectionBarColor,
    selectionCornerDp: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val viewportHeight = itemHeight * visibleCount
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(viewportHeight),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .background(selectionBarColor, RoundedCornerShape(selectionCornerDp)),
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPickerColumn(
    labels: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    visibleCount: Int = 5,
    labelColor: Color = DefaultLabelColor,
) {
    val count = labels.size.coerceAtLeast(1)
    val safeIndex = selectedIndex.coerceIn(0, count - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val viewportHeight = itemHeight * visibleCount
    val verticalPad = (viewportHeight - itemHeight) / 2

    LaunchedEffect(labels, selectedIndex) {
        val idx = selectedIndex.coerceIn(0, count - 1)
        listState.scrollToItem(idx)
    }

    var settledIndex by remember(labels) { mutableIntStateOf(safeIndex) }

    LaunchedEffect(listState, count) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                val layout = listState.layoutInfo
                val centerY = layout.viewportStartOffset + layout.viewportSize.height / 2f
                val closest = layout.visibleItemsInfo.minByOrNull { info ->
                    val itemCenter = info.offset + info.size / 2f
                    abs(itemCenter - centerY)
                }
                val idx = (closest?.index ?: settledIndex).coerceIn(0, count - 1)
                if (idx != settledIndex) {
                    settledIndex = idx
                    onSelectedIndexChange(idx)
                }
            }
    }

    Box(
        modifier = modifier
            .height(viewportHeight)
            .fillMaxWidth(),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = verticalPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(count) { index ->
                val label = labels.getOrElse(index) { "" }
                val distance = abs(index - settledIndex)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.55f
                    else -> 0.35f
                }
                val fontSize = if (distance == 0) 18.sp else 15.sp
                val weight = if (distance == 0) FontWeight.SemiBold else FontWeight.Normal
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = fontSize,
                        fontWeight = weight,
                        color = labelColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.alpha(alpha),
                    )
                }
            }
        }
    }
}
