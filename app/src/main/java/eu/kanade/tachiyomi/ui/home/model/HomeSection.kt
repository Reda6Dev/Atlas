package eu.kanade.tachiyomi.ui.home.model

import tachiyomi.domain.manga.model.Manga

data class HomeSection(
    val type: HomeSectionType,
    val title: String,
    val items: List<Manga>,
)

enum class HomeSectionType {
    LatestChapters,
    RecentlyRead,
    Popular,
    Trending,
}
