package eu.kanade.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.tachiyomi.ui.home.HomeScreenModel
import eu.kanade.tachiyomi.ui.home.HomeSourceDialogItem
import eu.kanade.tachiyomi.ui.home.HomeSourceFilter
import eu.kanade.tachiyomi.ui.home.HomeTabOption
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import tachiyomi.domain.library.model.LibraryDisplayMode
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
    onSaveSourceSettings: () -> Unit,
    onDismissSourceSettings: () -> Unit,
    onTabSelected: (HomeTabOption) -> Unit,
    onSourceFilterSelected: (Long?) -> Unit,
    onLoadMoreLatestChapters: () -> Unit,
    onLoadMoreDiscover: () -> Unit,
    onItemClick: (HomeItem) -> Unit,
) {
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
                    HomeSourceFilterRow(
                        availableSources = state.availableSources,
                        selectedSourceId = state.selectedSourceId,
                        onSourceFilterSelected = onSourceFilterSelected,
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                    when (state.selectedTab) {
                        HomeTabOption.LatestChapters -> {
                            LatestChaptersTabContent(
                                state = state,
                                onRefresh = onRefresh,
                                onLoadMore = onLoadMoreLatestChapters,
                                onItemClick = onItemClick,
                            )
                        }
                        HomeTabOption.DiscoverManga -> {
                            DiscoverMangaTabContent(
                                state = state,
                                onRefresh = onRefresh,
                                onLoadMore = onLoadMoreDiscover,
                                onItemClick = onItemClick,
                            )
                        }
                    }
                }
            }

            if (state.showSourceSettingsDialog) {
                HomeSourceSettingsDialog(
                    items = state.sourceDialogItems,
                    onToggleSource = onToggleDialogSource,
                    onSave = onSaveSourceSettings,
                    onDismiss = onDismissSourceSettings,
                )
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
        titleContent = {
            Text(
                text = "الرئيسية",
                fontSize = 34.sp,
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                ) {
                    Icon(Icons.Default.Search, contentDescription = "بحث")
                }
                IconButton(
                    onClick = onOpenSourceSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "مصادر الشاشة الرئيسية")
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.MoreVert, contentDescription = "خيارات أكثر")
                        }
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
    LazyRow(
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
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(
                    items = items,
                    key = { it.id },
                ) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleSource(item.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
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

    when {
        state.isLoadingLatest && state.latestChaptersItems.isEmpty() -> {
            LoadingScreen()
        }
        state.latestError != null && state.latestChaptersItems.isEmpty() -> {
            HomeErrorScreen(
                errorMessage = state.latestError,
                onRetry = onRefresh,
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
                            message = state.latestError,
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

@Composable
private fun DiscoverMangaTabContent(
    state: HomeScreenModel.State,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (HomeItem) -> Unit,
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

    when {
        state.isLoadingDiscover && state.discoverItems.isEmpty() -> {
            LoadingScreen()
        }
        state.discoverError != null && state.discoverItems.isEmpty() -> {
            HomeErrorScreen(
                errorMessage = state.discoverError,
                onRetry = onRefresh,
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
                            message = state.discoverError,
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
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "إعادة المحاولة")
            }
        }
    }
}

@Composable
private fun HomeErrorBanner(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onRetry) {
                Text(text = "إعادة المحاولة")
            }
        }
    }
}

@Composable
private fun GridCardItem(
    item: HomeItem,
    onClick: () -> Unit,
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
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MangaCover.Book(
                modifier = Modifier.fillMaxWidth(),
                data = domainCover,
                onClick = onClick,
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

@Composable
private fun ListItemCard(
    item: HomeItem,
    onClick: () -> Unit,
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
            .clickable { onClick() },
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
