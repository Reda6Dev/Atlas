package eu.kanade.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.category.components.ChangeCategoryDialog
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.LocalOpenDrawer
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.FormattedError
import eu.kanade.tachiyomi.ui.home.HomeScreenModel
import eu.kanade.tachiyomi.ui.home.HomeSourceDialogItem
import eu.kanade.tachiyomi.ui.home.HomeSourceFilter
import eu.kanade.tachiyomi.ui.home.HomeTabOption
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import kotlinx.coroutines.launch
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.theme.primaryTextColor
import tachiyomi.domain.manga.model.MangaCover as DomainMangaCover

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeScreenModel.State,
    onRefresh: () -> Unit,
    onOpenSourceSettings: () -> Unit,
    onSearchClick: () -> Unit,
    onDisplayModeChange: (LibraryDisplayMode) -> Unit,
    onToggleDialogSource: (Long) -> Unit,
    onMoveDialogSource: (Int, Boolean) -> Unit,
    onSaveSourceSettings: () -> Unit,
    onDismissSourceSettings: () -> Unit,
    onTabSelected: (HomeTabOption) -> Unit,
    onSourceFilterSelected: (Long?) -> Unit,
    onLoadMoreLatestChapters: () -> Unit,
    onLoadMoreDiscover: () -> Unit,
    onItemClick: (HomeItem) -> Unit,
    onItemLongClick: (HomeItem) -> Unit = {},
    onSetMangaCategories: (tachiyomi.domain.manga.model.Manga, List<Long>) -> Unit = { _, _ -> },
    onCloseDialog: () -> Unit = {},
    canOpenWebView: (Long?) -> Boolean = { false },
    onOpenWebView: (Long?) -> Unit = {},
    onHelpClick: () -> Unit = {},
) {
    var isFilterRowVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -10f) {
                    isFilterRowVisible = false
                } else if (delta > 10f) {
                    isFilterRowVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                HomeTopAppBar(
                    isRefreshing = state.isRefreshing,
                    displayMode = state.displayMode,
                    onRefresh = onRefresh,
                    onOpenSourceSettings = onOpenSourceSettings,
                    onSearchClick = onSearchClick,
                    onDisplayModeChange = onDisplayModeChange,
                )
                Spacer(Modifier.height(16.dp))
                HomeTabSelector(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                )
                if (state.availableSources.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = isFilterRowVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        HomeSourceFilterRow(
                            availableSources = state.availableSources,
                            selectedSourceId = state.selectedSourceId,
                            onSourceFilterSelected = onSourceFilterSelected,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(nestedScrollConnection),
        ) {
            when {
                state.isLoadingLatest && state.isLoadingDiscover && state.latestChaptersItems.isEmpty() && state.discoverItems.isEmpty() -> {
                    LoadingScreen()
                }
                state.availableSources.isEmpty() -> {
                    HomeEmptySourcesScreen(
                        onOpenSourceSettings = onOpenSourceSettings,
                    )
                }
                else -> {
                    if (state.availableSources.isNotEmpty()) {
                        val selectedIndex = state.availableSources.indexOfFirst { it.id == state.selectedSourceId }.coerceAtLeast(0)
                        val sourcePagerState = rememberPagerState(initialPage = selectedIndex) { state.availableSources.size }

                        LaunchedEffect(selectedIndex) {
                            if (sourcePagerState.currentPage != selectedIndex && selectedIndex in state.availableSources.indices) {
                                sourcePagerState.animateScrollToPage(selectedIndex)
                            }
                        }

                        LaunchedEffect(sourcePagerState.currentPage) {
                            val targetSourceId = state.availableSources.getOrNull(sourcePagerState.currentPage)?.id
                            if (targetSourceId != state.selectedSourceId) {
                                onSourceFilterSelected(targetSourceId)
                            }
                        }

                        HorizontalPager(
                            state = sourcePagerState,
                            modifier = Modifier.fillMaxSize(),
                            key = { page -> state.availableSources.getOrNull(page)?.id ?: -1L },
                        ) {
                            when (state.selectedTab) {
                                HomeTabOption.LatestChapters -> {
                                    LatestChaptersTabContent(
                                        state = state,
                                        onRefresh = onRefresh,
                                        onLoadMore = onLoadMoreLatestChapters,
                                        onItemClick = onItemClick,
                                        canOpenWebView = canOpenWebView,
                                        onOpenWebView = onOpenWebView,
                                        onHelpClick = onHelpClick,
                                    )
                                }
                                HomeTabOption.DiscoverManga -> {
                                    DiscoverMangaTabContent(
                                        state = state,
                                        onRefresh = onRefresh,
                                        onLoadMore = onLoadMoreDiscover,
                                        onItemClick = onItemClick,
                                        canOpenWebView = canOpenWebView,
                                        onOpenWebView = onOpenWebView,
                                        onHelpClick = onHelpClick,
                                    )
                                }
                            }
                        }
                    } else {
                        when (state.selectedTab) {
                            HomeTabOption.LatestChapters -> {
                                LatestChaptersTabContent(
                                    state = state,
                                    onRefresh = onRefresh,
                                    onLoadMore = onLoadMoreLatestChapters,
                                    onItemClick = onItemClick,
                                    canOpenWebView = canOpenWebView,
                                    onOpenWebView = onOpenWebView,
                                    onHelpClick = onHelpClick,
                                )
                            }
                            HomeTabOption.DiscoverManga -> {
                                DiscoverMangaTabContent(
                                    state = state,
                                    onRefresh = onRefresh,
                                    onLoadMore = onLoadMoreDiscover,
                                    onItemClick = onItemClick,
                                    canOpenWebView = canOpenWebView,
                                    onOpenWebView = onOpenWebView,
                                    onHelpClick = onHelpClick,
                                )
                            }
                        }
                    }
                }
            }

            if (state.showSourceSettingsDialog) {
                HomeSourceSettingsDialog(
                    items = state.sourceDialogItems,
                    onToggleSource = onToggleDialogSource,
                    onMoveSource = onMoveDialogSource,
                    onSave = onSaveSourceSettings,
                    onDismiss = onDismissSourceSettings,
                )
            }

            when (val dialog = state.dialog) {
                is HomeScreenModel.Dialog.ChangeMangaCategory -> {
                    ChangeCategoryDialog(
                        initialSelection = dialog.initialSelection,
                        onDismissRequest = onCloseDialog,
                        onEditCategories = {},
                        onConfirm = { selected, _ ->
                            onSetMangaCategories(dialog.manga, selected)
                        },
                    )
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun HomeTopAppBar(
    isRefreshing: Boolean,
    displayMode: LibraryDisplayMode,
    onRefresh: () -> Unit,
    onOpenSourceSettings: () -> Unit,
    onSearchClick: () -> Unit,
    onDisplayModeChange: (LibraryDisplayMode) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDisplayModeSubMenu by remember { mutableStateOf(false) }

    AppBar(
        navigateUp = LocalOpenDrawer.current,
        navigationIcon = Icons.Default.Menu,
        titleContent = {
            Text(
                text = "الرئيسية",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "بحث",
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onOpenSourceSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "مصادر الشاشة الرئيسية",
                        modifier = Modifier.size(20.dp),
                    )
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "خيارات أكثر",
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = {
                            showMenu = false
                            showDisplayModeSubMenu = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "تحديث المحتوى") },
                            onClick = {
                                showMenu = false
                                onRefresh()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                )
                            },
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "طريقة العرض",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                            onClick = { showDisplayModeSubMenu = !showDisplayModeSubMenu },
                        )

                        if (showDisplayModeSubMenu) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (displayMode == LibraryDisplayMode.CompactGrid) "✓ الشبكة المدمجة" else "الشبكة المدمجة",
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDisplayModeSubMenu = false
                                    onDisplayModeChange(LibraryDisplayMode.CompactGrid)
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (displayMode == LibraryDisplayMode.ComfortableGrid) "✓ الشبكة المريحة" else "الشبكة المريحة",
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDisplayModeSubMenu = false
                                    onDisplayModeChange(LibraryDisplayMode.ComfortableGrid)
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (displayMode == LibraryDisplayMode.List) "✓ القائمة" else "القائمة",
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDisplayModeSubMenu = false
                                    onDisplayModeChange(LibraryDisplayMode.List)
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTabSelector(
    selectedTab: HomeTabOption,
    onTabSelected: (HomeTabOption) -> Unit,
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Tab(
            selected = selectedTab == HomeTabOption.LatestChapters,
            onClick = { onTabSelected(HomeTabOption.LatestChapters) },
            selectedContentColor = primaryTextColor(),
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            text = {
                Text(
                    text = "آخر الفصول",
                    fontWeight = FontWeight.Bold,
                )
            },
        )

        Tab(
            selected = selectedTab == HomeTabOption.DiscoverManga,
            onClick = { onTabSelected(HomeTabOption.DiscoverManga) },
            selectedContentColor = primaryTextColor(),
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            text = {
                Text(
                    text = "اكتشف المانغا",
                    fontWeight = FontWeight.Bold,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSourceFilterRow(
    availableSources: List<HomeSourceFilter>,
    selectedSourceId: Long?,
    onSourceFilterSelected: (Long?) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val selectedIndex = availableSources.indexOfFirst { it.id == selectedSourceId }.coerceAtLeast(0)

    LaunchedEffect(selectedIndex) {
        if (availableSources.isNotEmpty() && selectedIndex in availableSources.indices) {
            lazyListState.animateScrollToItem(selectedIndex)
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = availableSources,
            key = { filterItem: HomeSourceFilter -> filterItem.id ?: -1L },
        ) { filterItem: HomeSourceFilter ->
            FilterChip(
                selected = filterItem.id == selectedSourceId,
                onClick = { onSourceFilterSelected(filterItem.id) },
                label = {
                    Text(
                        text = filterItem.name,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}

@Composable
private fun HomeSourceSettingsDialog(
    items: List<HomeSourceDialogItem>,
    onToggleSource: (Long) -> Unit,
    onMoveSource: (Int, Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مصادر الشاشة الرئيسية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.id },
                ) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { onToggleSource(item.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                onMoveSource(index, true)
                                coroutineScope.launch {
                                    if (index > 0) {
                                        lazyListState.animateScrollToItem(index - 1)
                                    }
                                }
                            },
                            enabled = index > 0,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "رفع",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        IconButton(
                            onClick = {
                                onMoveSource(index, false)
                                coroutineScope.launch {
                                    if (index < items.lastIndex) {
                                        lazyListState.animateScrollToItem(index + 1)
                                    }
                                }
                            },
                            enabled = index < items.lastIndex,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "خفض",
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )

                        Checkbox(
                            checked = item.isSelected,
                            onCheckedChange = { onToggleSource(item.id) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text(text = "حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "إلغاء")
            }
        },
    )
}

@Composable
private fun HomeEmptySourcesScreen(
    onOpenSourceSettings: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "لا توجد مصادر مفعلة للشاشة الرئيسية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "اختر مصادر من قائمة الإعدادات لعرض محتواها في الرئيسية.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onOpenSourceSettings) {
                Text(text = "مصادر الشاشة الرئيسية")
            }
        }
    }
}

@Composable
private fun LatestChaptersTabContent(
    state: HomeScreenModel.State,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (HomeItem) -> Unit,
    canOpenWebView: (Long?) -> Boolean,
    onOpenWebView: (Long?) -> Unit,
    onHelpClick: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && state.hasMoreLatest && !state.isLoadingMoreLatest && !state.isLoadingLatest) {
            onLoadMore()
        }
    }

    val columnsCount = when (state.displayMode) {
        LibraryDisplayMode.ComfortableGrid -> 2
        LibraryDisplayMode.List -> 1
        else -> 3
    }

    PullRefresh(
        refreshing = state.isRefreshing,
        enabled = true,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.isLoadingLatest && state.latestChaptersItems.isEmpty() -> {
                LoadingScreen()
            }
            state.latestError != null && state.latestChaptersItems.isEmpty() -> {
                val errorSourceId = state.latestErrorSourceId
                HomeErrorScreen(
                    error = state.latestError,
                    onRetry = onRefresh,
                    onOpenWebView = if (canOpenWebView(errorSourceId)) {
                        { onOpenWebView(errorSourceId) }
                    } else {
                        null
                    },
                    onHelpClick = onHelpClick,
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.latestError != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeErrorBanner(
                                error = state.latestError,
                                onRetry = onRefresh,
                            )
                        }
                    }

                    items(
                        items = state.latestChaptersItems,
                        key = { item -> getItemKey(item) },
                    ) { item ->
                        if (state.displayMode == LibraryDisplayMode.List) {
                            ListItemCard(
                                item = item,
                                onClick = { onItemClick(item) },
                            )
                        } else {
                            GridCardItem(
                                item = item,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }

                    if (state.isLoadingMoreLatest) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverMangaTabContent(
    state: HomeScreenModel.State,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (HomeItem) -> Unit,
    canOpenWebView: (Long?) -> Boolean,
    onOpenWebView: (Long?) -> Unit,
    onHelpClick: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && state.hasMoreDiscover && !state.isLoadingMoreDiscover && !state.isLoadingDiscover) {
            onLoadMore()
        }
    }

    val columnsCount = when (state.displayMode) {
        LibraryDisplayMode.ComfortableGrid -> 2
        LibraryDisplayMode.List -> 1
        else -> 3
    }

    PullRefresh(
        refreshing = state.isRefreshing,
        enabled = true,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.isLoadingDiscover && state.discoverItems.isEmpty() -> {
                LoadingScreen()
            }
            state.discoverError != null && state.discoverItems.isEmpty() -> {
                val errorSourceId = state.discoverErrorSourceId
                HomeErrorScreen(
                    error = state.discoverError,
                    onRetry = onRefresh,
                    onOpenWebView = if (canOpenWebView(errorSourceId)) {
                        { onOpenWebView(errorSourceId) }
                    } else {
                        null
                    },
                    onHelpClick = onHelpClick,
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.discoverError != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeErrorBanner(
                                error = state.discoverError,
                                onRetry = onRefresh,
                            )
                        }
                    }

                    items(
                        items = state.discoverItems,
                        key = { item -> getItemKey(item) },
                    ) { item ->
                        if (state.displayMode == LibraryDisplayMode.List) {
                            ListItemCard(
                                item = item,
                                onClick = { onItemClick(item) },
                            )
                        } else {
                            GridCardItem(
                                item = item,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }

                    if (state.isLoadingMoreDiscover) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getItemKey(item: HomeItem): String {
    return when (item) {
        is HomeItem.MangaCard -> {
            val src = item.sourceId ?: 0L
            val identifier = item.localMangaId?.let { "id_$it" }
                ?: item.mangaDexUuid?.takeIf { it.isNotBlank() }?.let { "uuid_$it" }
                ?: item.sManga?.url?.takeIf { it.isNotBlank() }?.let { "url_$it" }
                ?: "title_${item.title.hashCode()}"
            "manga_${src}_$identifier"
        }
        is HomeItem.ChapterCard -> {
            val src = item.sourceId ?: 0L
            val chId = item.chapterId.takeIf { it.isNotBlank() }
                ?: item.sManga.url.takeIf { it.isNotBlank() }
                ?: "title_${item.title.hashCode()}"
            "chapter_${src}_$chId"
        }
        is HomeItem.HistoryCard -> "history_${item.localMangaId}"
    }
}

@Composable
private fun HomeErrorScreen(
    error: FormattedError,
    onRetry: () -> Unit,
    onOpenWebView: (() -> Unit)?,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyScreen(
        message = error.message,
        secondaryMessage = error.detail,
        modifier = modifier,
        actions = listOfNotNull(
            EmptyScreenAction(
                stringRes = MR.strings.action_retry,
                icon = Icons.Outlined.Refresh,
                onClick = onRetry,
            ),
            if (onOpenWebView != null) {
                EmptyScreenAction(
                    stringRes = MR.strings.action_open_in_web_view,
                    icon = Icons.Outlined.Public,
                    onClick = onOpenWebView,
                )
            } else {
                null
            },
            EmptyScreenAction(
                stringRes = MR.strings.label_help,
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                onClick = onHelpClick,
            ),
        ),
    )
}

@Composable
private fun HomeErrorBanner(
    error: FormattedError,
    onRetry: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!error.detail.isNullOrBlank()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = error.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
            TextButton(onClick = onRetry) {
                Text(text = stringResource(MR.strings.action_retry))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridCardItem(
    item: HomeItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val domainCover = DomainMangaCover(
        mangaId = when (item) {
            is HomeItem.HistoryCard -> item.localMangaId
            is HomeItem.MangaCard -> item.localMangaId ?: 0L
            is HomeItem.ChapterCard -> 0L
        },
        sourceId = 0L,
        isMangaFavorite = false,
        ogUrl = item.thumbnailUrl,
        lastModified = 0L,
    )

    val sourceName = when (item) {
        is HomeItem.MangaCard -> item.sourceName
        is HomeItem.ChapterCard -> item.sourceName
        is HomeItem.HistoryCard -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MangaCover.Book(
                modifier = Modifier.fillMaxWidth(),
                data = domainCover,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        val badgeText = when (item) {
            is HomeItem.ChapterCard -> item.chapterName
            is HomeItem.HistoryCard -> item.chapterName
            is HomeItem.MangaCard -> item.subtitle?.takeIf { it != sourceName }
        }

        if (!badgeText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = tachiyomi.presentation.core.theme.primaryTextColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!sourceName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sourceName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListItemCard(
    item: HomeItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val domainCover = DomainMangaCover(
        mangaId = when (item) {
            is HomeItem.HistoryCard -> item.localMangaId
            is HomeItem.MangaCard -> item.localMangaId ?: 0L
            is HomeItem.ChapterCard -> 0L
        },
        sourceId = 0L,
        isMangaFavorite = false,
        ogUrl = item.thumbnailUrl,
        lastModified = 0L,
    )

    val sourceName = when (item) {
        is HomeItem.MangaCard -> item.sourceName
        is HomeItem.ChapterCard -> item.sourceName
        is HomeItem.HistoryCard -> null
    }

    val badgeText = when (item) {
        is HomeItem.ChapterCard -> item.chapterName
        is HomeItem.HistoryCard -> item.chapterName
        is HomeItem.MangaCard -> item.subtitle?.takeIf { it != sourceName }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(80.dp),
            ) {
                MangaCover.Book(
                    modifier = Modifier.fillMaxSize(),
                    data = domainCover,
                    onClick = onClick,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!badgeText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = tachiyomi.presentation.core.theme.primaryTextColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (!sourceName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sourceName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
