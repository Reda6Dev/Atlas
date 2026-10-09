package eu.kanade.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.library.components.MangaCompactGridItem
import eu.kanade.tachiyomi.ui.home.HomeState
import eu.kanade.tachiyomi.ui.home.model.HomeSection
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.presentation.core.components.material.padding

@Composable
fun HomeScreen(
    state: HomeState,
    onMangaClick: (Manga) -> Unit,
    onRefresh: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = MaterialTheme.padding.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.large)
            ) {
                items(state.sections) { section ->
                    HomeSectionContent(
                        section = section,
                        onMangaClick = onMangaClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionContent(
    section: HomeSection,
    onMangaClick: (Manga) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.padding.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)
        ) {
            items(section.items) { manga ->
                Box(modifier = Modifier.width(120.dp)) {
                    MangaCompactGridItem(
                        coverData = MangaCover(
                            mangaId = manga.id,
                            sourceId = manga.source,
                            isMangaFavorite = manga.favorite,
                            ogUrl = manga.ogThumbnailUrl,
                            lastModified = manga.coverLastModified,
                        ),
                        onClick = { onMangaClick(manga) },
                        onLongClick = { },
                        title = manga.title,
                    )
                }
            }
        }
    }
}
