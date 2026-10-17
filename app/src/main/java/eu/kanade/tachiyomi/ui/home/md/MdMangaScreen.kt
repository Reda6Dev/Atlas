package eu.kanade.tachiyomi.ui.home.md

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import exh.md.dto.ChapterDataDto
import exh.md.utils.MdUtil

class MdMangaScreen(private val mangaId: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { MdMangaScreenModel(mangaId) }
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val title = state.manga?.let { MdUtil.getTitleFromManga(it.attributes, "en", true) } ?: "تفاصيل المانغا"
                        Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            MangaHeader(state)
                        }

                        item {
                            Text(
                                text = "الفصول",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp),
                            )
                        }

                        items(state.chapters) { chapter ->
                            ChapterItem(chapter) {
                                navigator.push(MdReaderScreen(chapter.id, mangaId))
                            }
                        }

                        if (state.chaptersLoading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (state.hasMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { screenModel.loadMoreChapters() }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = "تحميل المزيد", color = tachiyomi.presentation.core.theme.primaryTextColor())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MangaHeader(state: MdMangaScreenModel.State) {
        val manga = state.manga ?: return
        val title = MdUtil.getTitleFromManga(manga.attributes, "en", true)
        val description = try {
            MdUtil.cleanDescription(manga.attributes.description.toString())
        } catch (e: Exception) {
            "لا يوجد وصف"
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }

    @Composable
    private fun ChapterItem(chapter: ChapterDataDto, onClick: () -> Unit) {
        val chapterNum = chapter.attributes.chapter
        val title = chapter.attributes.title

        val displayText = when {
            !chapterNum.isNullOrBlank() && !title.isNullOrBlank() -> "Ch. $chapterNum - $title"
            !chapterNum.isNullOrBlank() -> "Ch. $chapterNum"
            !title.isNullOrBlank() -> title
            else -> "Chapter (Unknown)"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column {
                Text(text = displayText, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اللغة: ${chapter.attributes.translatedLanguage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
