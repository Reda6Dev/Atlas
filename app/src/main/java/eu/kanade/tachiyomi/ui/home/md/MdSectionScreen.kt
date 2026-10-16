package eu.kanade.tachiyomi.ui.home.md

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import eu.kanade.tachiyomi.ui.home.model.HomeSectionType
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import tachiyomi.domain.manga.model.MangaCover as DomainMangaCover

class MdSectionScreen(val sectionType: HomeSectionType) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { MdSectionScreenModel(sectionType) }
        val state by screenModel.state.collectAsState()
        val gridState = rememberLazyGridState()

        val shouldLoadMore = remember {
            derivedStateOf {
                val totalItems = gridState.layoutInfo.totalItemsCount
                val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                totalItems > 0 && lastVisibleItem >= totalItems - 6
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value && state.hasMore && !state.isLoadingMore && !state.isLoading) {
                screenModel.loadMore()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = state.title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = { screenModel.refresh() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "تحديث")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    state.isLoading && state.items.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    state.error != null && state.items.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { screenModel.refresh() }) {
                                Text(text = "إعادة المحاولة")
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = state.items,
                                key = { item ->
                                    when (item) {
                                        is HomeItem.MangaCard -> item.mangaDexUuid ?: item.localMangaId.toString()
                                        is HomeItem.ChapterCard -> item.chapterId
                                        is HomeItem.HistoryCard -> item.localMangaId.toString()
                                    }
                                }
                            ) { item ->
                                CompactGridCard(
                                    item = item,
                                    onClick = {
                                        screenModel.onItemClick(item) { localMangaId ->
                                            navigator.push(MangaScreen(localMangaId, true))
                                        }
                                    }
                                )
                            }

                            if (state.isLoadingMore) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
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
    }

    @Composable
    private fun CompactGridCard(
        item: HomeItem,
        onClick: () -> Unit
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
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
                overflow = TextOverflow.Ellipsis
            )

            val badgeText = when (item) {
                is HomeItem.ChapterCard -> item.chapterName
                is HomeItem.HistoryCard -> item.chapterName
                is HomeItem.MangaCard -> item.subtitle
            }

            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = tachiyomi.presentation.core.theme.primaryTextColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
