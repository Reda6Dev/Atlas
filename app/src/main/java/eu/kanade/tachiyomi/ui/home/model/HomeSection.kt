package eu.kanade.tachiyomi.ui.home.model

import eu.kanade.tachiyomi.source.model.SManga
import tachiyomi.domain.manga.model.Manga

sealed interface HomeItem {
    val title: String
    val thumbnailUrl: String?

    data class MangaCard(
        val sManga: SManga? = null,
        val domainManga: Manga? = null,
        val mangaDexUuid: String? = null,
        val localMangaId: Long? = null,
        override val title: String,
        override val thumbnailUrl: String?,
        val subtitle: String? = null,
        val sourceId: Long? = null,
        val sourceName: String? = null,
    ) : HomeItem

    data class ChapterCard(
        val sManga: SManga,
        val mangaDexUuid: String = "",
        val chapterId: String = "",
        val chapterName: String = "",
        val scanlator: String? = null,
        val date: String? = null,
        val language: String? = null,
        override val title: String,
        override val thumbnailUrl: String?,
        val sourceId: Long? = null,
        val sourceName: String? = null,
    ) : HomeItem

    data class HistoryCard(
        val localMangaId: Long,
        override val title: String,
        override val thumbnailUrl: String?,
        val chapterName: String? = null,
        val readAt: String? = null,
    ) : HomeItem
}

data class HomeSection(
    val id: String,
    val title: String,
    val type: HomeSectionType,
    val items: List<HomeItem>,
)

enum class HomeSectionType {
    LatestChapters,
    DiscoverManga,
    RecentlyRead,
    MyLibrary,
}
