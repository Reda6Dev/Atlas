package eu.kanade.tachiyomi.ui.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.ui.home.model.HomeSection
import eu.kanade.tachiyomi.ui.home.model.HomeSectionType
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import tachiyomi.domain.manga.model.Manga

data class HomeState(
    val sections: List<HomeSection> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeScreenModel : StateScreenModel<HomeState>(HomeState()) {

    init {
        loadHomeSections()
    }

    private fun loadHomeSections() {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }

            // Create demo sections with mock data
            val sections = listOf(
                HomeSection(
                    type = HomeSectionType.LatestChapters,
                    title = "Latest Chapters",
                    items = createMockManga(5)
                ),
                HomeSection(
                    type = HomeSectionType.RecentlyRead,
                    title = "Recently Read",
                    items = createMockManga(4)
                ),
                HomeSection(
                    type = HomeSectionType.Popular,
                    title = "Popular Today",
                    items = createMockManga(6)
                ),
            )

            mutableState.update {
                it.copy(
                    sections = sections,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        loadHomeSections()
    }

    private fun createMockManga(count: Int): List<Manga> {
        val currentTime = System.currentTimeMillis()

        return List(count) { index ->
            Manga(
                id = index.toLong(),
                source = 0L,
                favorite = index % 2 == 0,
                lastUpdate = currentTime,
                nextUpdate = 0L,
                fetchInterval = 0,
                dateAdded = currentTime,
                viewerFlags = 0L,
                chapterFlags = 0L,
                coverLastModified = 0L,
                url = "",
                ogTitle = "Demo Manga ${index + 1}",
                ogArtist = "Artist ${index + 1}",
                ogAuthor = "Author ${index + 1}",
                ogThumbnailUrl = null,
                ogDescription = "This is demo content for testing the Home screen. Real data will come from your library and sources.",
                ogGenre = listOf("Action", "Adventure", "Fantasy"),
                ogStatus = 0L,
                updateStrategy = UpdateStrategy.ALWAYS_UPDATE,
                initialized = true,
                lastModifiedAt = 0L,
                favoriteModifiedAt = null,
                version = 0L,
                notes = "",
                memo = JsonObject(emptyMap()),
            )
        }
    }
}
