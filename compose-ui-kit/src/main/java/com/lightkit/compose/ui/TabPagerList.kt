package com.lightkit.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> TabPagerList(
    tabs: List<T>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    tabContent: @Composable (List<T>, T, (T) -> Unit) -> Unit,
    pageContent: @Composable (T, Int) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    dividerColor: Color? = null,
    pagerModifier: Modifier = Modifier,
    enableCache: Boolean = true,
    onPageChanged: ((T) -> Unit)? = null,
    shouldLoadOnPageChange: ((T) -> Boolean)? = null,
) {
    val theme = LocalUiKitTheme.current
    val resolvedDividerColor = dividerColor ?: theme.tabPagerDividerColor

    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(selectedTab).coerceAtLeast(0),
        pageCount = { tabs.size },
    )
    val coroutineScope = rememberCoroutineScope()
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    val loadedTabs = remember { mutableSetOf<T>() }

    LaunchedEffect(selectedTab, tabs) {
        if (tabs.isEmpty()) return@LaunchedEffect
        val targetPage = tabs.indexOf(selectedTab).coerceAtLeast(0)
        if (pagerState.currentPage == targetPage) return@LaunchedEffect
        isProgrammaticScroll = true
        try {
            pagerState.scrollToPage(targetPage)
        } finally {
            isProgrammaticScroll = false
        }
    }

    LaunchedEffect(pagerState.currentPage, tabs) {
        if (isProgrammaticScroll || tabs.isEmpty()) return@LaunchedEffect
        val currentTab = tabs.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (currentTab != selectedTab) {
            onTabSelected(currentTab)
            onPageChanged?.invoke(currentTab)
            if (shouldLoadOnPageChange?.invoke(currentTab) == true) {
                if (!enableCache || !loadedTabs.contains(currentTab)) {
                    loadedTabs.add(currentTab)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        tabContent(tabs, selectedTab) { tab ->
            onTabSelected(tab)
            val index = tabs.indexOf(tab).coerceAtLeast(0)
            coroutineScope.launch {
                isProgrammaticScroll = true
                try {
                    pagerState.animateScrollToPage(index)
                    while (pagerState.isScrollInProgress) {
                        delay(50)
                    }
                } finally {
                    delay(100)
                    isProgrammaticScroll = false
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(color = resolvedDividerColor, thickness = 0.5.dp)
        }
        HorizontalPager(
            state = pagerState,
            modifier = pagerModifier.fillMaxSize(),
        ) { pageIndex ->
            pageContent(tabs[pageIndex], pageIndex)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T, D> TabPagerRefreshList(
    tabs: List<T>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    tabContent: @Composable (List<T>, T, (T) -> Unit) -> Unit,
    dataProvider: (T) -> List<D>,
    isEmpty: (List<D>) -> Boolean = { it.isEmpty() },
    emptyContent: (@Composable (T) -> Unit)? = null,
    listContent: LazyListScope.(T, List<D>) -> Unit,
    refreshState: RefreshLoadState,
    onRefresh: (T) -> Unit,
    onLoadMore: ((T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    dividerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    enableCache: Boolean = true,
    onPageChanged: ((T) -> Unit)? = null,
    shouldLoadOnPageChange: ((T) -> Boolean)? = null,
) {
    TabPagerList(
        tabs = tabs,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        tabContent = tabContent,
        pageContent = { tab, _ ->
            val data = dataProvider(tab)
            val isEmptyData = isEmpty(data)
            val isCurrentTab = tab == selectedTab
            RefreshLoadList(
                state = refreshState,
                onRefresh = { if (isCurrentTab) onRefresh(tab) },
                onLoadMore = onLoadMore?.let { { if (isCurrentTab) it(tab) } } ?: {},
                onRetryLoadMore = onLoadMore?.let { { if (isCurrentTab) it(tab) } },
                showNoMoreDataItem = !(isEmptyData && refreshState is RefreshLoadState.NoMoreData),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
            ) {
                if (isEmptyData && emptyContent != null) {
                    item(key = "empty_state") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 400.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            emptyContent(tab)
                        }
                    }
                } else {
                    listContent(tab, data)
                }
            }
        },
        modifier = modifier,
        showDivider = showDivider,
        dividerColor = dividerColor,
        enableCache = enableCache,
        onPageChanged = onPageChanged,
        shouldLoadOnPageChange = shouldLoadOnPageChange,
    )
}

@Composable
fun EmptyState(
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    actionButton: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
) {
    val theme = LocalUiKitTheme.current
    val resolvedTitleColor = titleColor ?: theme.emptyStateTitleColor
    val resolvedSubtitleColor = subtitleColor ?: theme.emptyStateSubtitleColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.invoke()
        if (icon != null) {
            SpacerV(24.dp)
        }
        Text(
            text = title,
            fontSize = 16.sp,
            color = resolvedTitleColor,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            SpacerV(8.dp)
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = resolvedSubtitleColor,
                textAlign = TextAlign.Center,
            )
        }
        if (actionButton != null) {
            SpacerV(24.dp)
            actionButton()
        }
    }
}

@Composable
fun <T> rememberCenteringTabRowState(
    tabs: List<T>,
    selectedTab: T,
): LazyListState {
    val listState = rememberLazyListState()
    LaunchedEffect(tabs, selectedTab) {
        if (tabs.isEmpty()) return@LaunchedEffect
        val index = tabs.indexOf(selectedTab).coerceAtLeast(0)
        suspend fun tryCenterIfVisible(): Boolean {
            val layoutInfo = listState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            listState.animateScrollBy((itemCenter - viewportCenter).toFloat())
            return true
        }
        if (!tryCenterIfVisible()) {
            listState.animateScrollToItem(index)
            delay(16)
            tryCenterIfVisible()
        }
    }
    return listState
}
