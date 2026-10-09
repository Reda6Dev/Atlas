package eu.kanade.presentation.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.tachiyomi.ui.home.HomeScreenModel
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import eu.kanade.tachiyomi.ui.home.model.HomeSection
import tachiyomi.domain.manga.model.MangaCover as DomainMangaCover
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding

@Composable
fun HomeScreen(
    state: HomeScreenModel.State,
    onRefresh: () -> Unit,
    onItemClick: (HomeItem) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            HomeTopAppBar(
                scrollBehavior = scrollBehavior,
                onRefresh = onRefresh,
            )
        },
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingScreen(modifier = Modifier.padding(paddingValues))
            }
            state.sections.isEmpty() -> {
                HomeErrorScreen(
                    errorMessage = state.error ?: "Unable to load data",
                    onRetry = onRefresh,
                    modifier = Modifier.padding(paddingValues),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = MaterialTheme.padding.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
                ) {
                    if (state.error != null) {
                        item {
                            HomeErrorBanner(
                                message = state.error,
                                onRetry = onRefresh,
                            )
                        }
                    }

                    items(
                        items = state.sections,
                        key = { it.id },
                    ) { section ->
                        HomeSectionRow(
                            section = section,
                            onItemClick = onItemClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onRefresh: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Atlas Home",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
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
            modifier = Modifier.padding(MaterialTheme.padding.medium),
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.padding.medium))
            Button(onClick = onRetry) {
                Text(text = "Retry")
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
            .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.padding.medium)
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
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun HomeSectionRow(
    section: HomeSection,
    onItemClick: (HomeItem) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.padding.medium,
                vertical = MaterialTheme.padding.small,
            ),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.padding.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
        ) {
            items(
                items = section.items,
                key = { item ->
                    when (item) {
                        is HomeItem.MangaCard -> item.mangaDexUuid ?: item.localMangaId.toString()
                        is HomeItem.ChapterCard -> item.chapterId
                        is HomeItem.HistoryCard -> item.localMangaId.toString()
                    }
                },
            ) { item ->
                HomeItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

@Composable
private fun HomeItemCard(
    item: HomeItem,
    onClick: () -> Unit,
) {
    val cardWidth = 120.dp
    val coverHeight = 160.dp

    Column(
        modifier = Modifier
            .width(cardWidth)
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.padding.extraSmall),
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

        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(coverHeight)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            MangaCover.Book(
                modifier = Modifier.fillMaxSize(),
                data = domainCover,
                onClick = onClick,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        val badgeText = when (item) {
            is HomeItem.ChapterCard -> item.chapterName
            is HomeItem.HistoryCard -> item.chapterName
            is HomeItem.MangaCard -> item.subtitle
        }

        if (!badgeText.isNullOrBlank()) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
