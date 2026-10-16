package eu.kanade.tachiyomi.ui.home.md

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import eu.kanade.tachiyomi.ui.home.model.HomeSectionType
import exh.md.service.MangaDexService
import exh.md.utils.MdConstants
import exh.md.utils.MdUtil
import kotlinx.coroutines.flow.update
import mihon.domain.manga.model.toDomainManga
import okhttp3.Headers
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MdSectionScreenModel(
    val sectionType: HomeSectionType,
    private val networkHelper: NetworkHelper = Injekt.get(),
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
) : StateScreenModel<MdSectionScreenModel.State>(State()) {

    data class State(
        val title: String = "",
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val items: List<HomeItem> = emptyList(),
        val page: Int = 0,
        val hasMore: Boolean = true,
        val error: String? = null
    )

    private val mangadexService by lazy {
        val headers = Headers.Builder()
            .add("User-Agent", "Atlas v${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            .build()
        MangaDexService(networkHelper.client, headers)
    }

    init {
        val title = when (sectionType) {
            HomeSectionType.LatestChapters -> "آخر الفصول"
            HomeSectionType.DiscoverManga -> "اكتشف المانغا"
            HomeSectionType.RecentlyRead -> "أكمل القراءة"
            HomeSectionType.MyLibrary -> "مكتبتي"
        }
        mutableState.update { it.copy(title = title) }
        loadPage(0)
    }

    fun refresh() {
        if (state.value.isRefreshing) return
        mutableState.update { it.copy(isRefreshing = true, error = null, page = 0, hasMore = true, items = emptyList()) }
        loadPage(0)
    }

    fun loadMore() {
        if (state.value.isLoading || state.value.isLoadingMore || !state.value.hasMore) return
        loadPage(state.value.page + 1)
    }

    fun onItemClick(item: HomeItem, onOpenManga: (Long) -> Unit) {
        screenModelScope.launchIO {
            val localMangaId = when (item) {
                is HomeItem.HistoryCard -> item.localMangaId
                is HomeItem.MangaCard -> {
                    item.localMangaId ?: item.sManga?.let { sManga ->
                        networkToLocalManga(sManga.toDomainManga(exh.source.MANGADEX_SOURCE_ID)).id
                    }
                }
                is HomeItem.ChapterCard -> {
                    networkToLocalManga(item.sManga.toDomainManga(exh.source.MANGADEX_SOURCE_ID)).id
                }
            }
            if (localMangaId != null) {
                onOpenManga(localMangaId)
            }
        }
    }

    private fun loadPage(page: Int) {
        if (page > 0) {
            mutableState.update { it.copy(isLoadingMore = true) }
        }
        screenModelScope.launchIO {
            runCatching {
                when (sectionType) {
                    HomeSectionType.LatestChapters -> loadLatestChaptersPage(page)
                    HomeSectionType.DiscoverManga -> loadDiscoverMangaPage(page)
                    else -> emptyList()
                }
            }.onSuccess { newItems ->
                val currentItems = state.value.items
                val existingKeys = currentItems.map { getItemKey(it) }.toSet()
                val uniqueNewItems = newItems.filter { getItemKey(it) !in existingKeys }
                val allItems = if (page == 0) newItems else currentItems + uniqueNewItems
                val hasMore = newItems.size >= 20

                mutableState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        items = allItems,
                        page = page,
                        hasMore = hasMore,
                        error = null
                    )
                }
            }.onFailure { e ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = e.localizedMessage ?: "حدث خطأ أثناء تحميل البيانات"
                    )
                }
            }
        }
    }

    private fun getItemKey(item: HomeItem): String {
        return when (item) {
            is HomeItem.MangaCard -> item.mangaDexUuid ?: item.localMangaId.toString()
            is HomeItem.ChapterCard -> item.chapterId
            is HomeItem.HistoryCard -> item.localMangaId.toString()
        }
    }

    private suspend fun loadDiscoverMangaPage(page: Int): List<HomeItem> {
        val popularDto = mangadexService.fetchPopularManga(limit = 20, offset = page * 20)
        return popularDto.data.map { dto ->
            val title = MdUtil.getTitleFromManga(dto.attributes, "en", true)
            val coverFileName = dto.relationships
                .firstOrNull { it.type == MdConstants.Types.coverArt }
                ?.attributes?.fileName

            val thumbnailUrl = coverFileName?.let { fileName ->
                "${MdUtil.cdnCoverUrl(dto.id, fileName)}.512.jpg"
            }

            val sManga = SManga(
                url = "/manga/${dto.id}",
                title = title,
                thumbnail_url = thumbnailUrl,
            )

            HomeItem.MangaCard(
                sManga = sManga,
                mangaDexUuid = dto.id,
                title = title,
                thumbnailUrl = thumbnailUrl,
                subtitle = dto.attributes.status?.replaceFirstChar { it.uppercase() },
            )
        }
    }

    private suspend fun loadLatestChaptersPage(page: Int): List<HomeItem> {
        val latestChaptersDto = mangadexService.fetchLatestChapters(
            limit = 20,
            offset = page * 20,
            languages = listOf("en", "ar"),
        )

        val chapterList = latestChaptersDto.data
        val mangaIds = chapterList.mapNotNull { ch ->
            ch.relationships.firstOrNull { it.type == MdConstants.Types.manga }?.id
        }.distinct()

        val mangaDetailsMap = if (mangaIds.isNotEmpty()) {
            runCatching {
                mangadexService.viewMangas(mangaIds).data.associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        return chapterList.mapNotNull { chDto ->
            val mangaId = chDto.relationships.firstOrNull { it.type == MdConstants.Types.manga }?.id
                ?: return@mapNotNull null
            val mangaDto = mangaDetailsMap[mangaId]

            val mangaTitle = mangaDto?.let { MdUtil.getTitleFromManga(it.attributes, "en", true) }
                ?: "Manga"

            val coverFileName = mangaDto?.relationships
                ?.firstOrNull { it.type == MdConstants.Types.coverArt }
                ?.attributes?.fileName

            val thumbnailUrl = coverFileName?.let { fileName ->
                "${MdUtil.cdnCoverUrl(mangaId, fileName)}.512.jpg"
            }

            val chapterNum = chDto.attributes.chapter
            val chapterTitleText = chDto.attributes.title
            val chapterName = when {
                !chapterNum.isNullOrBlank() && !chapterTitleText.isNullOrBlank() -> "Ch. $chapterNum - $chapterTitleText"
                !chapterNum.isNullOrBlank() -> "Ch. $chapterNum"
                !chapterTitleText.isNullOrBlank() -> chapterTitleText
                else -> "Chapter"
            }

            val scanlator = chDto.relationships
                .firstOrNull { it.type == MdConstants.Types.scanlator }
                ?.attributes?.name

            val sManga = SManga(
                url = "/manga/$mangaId",
                title = mangaTitle,
                thumbnail_url = thumbnailUrl,
            )

            HomeItem.ChapterCard(
                sManga = sManga,
                mangaDexUuid = mangaId,
                chapterId = chDto.id,
                chapterName = chapterName,
                scanlator = scanlator,
                language = chDto.attributes.translatedLanguage,
                title = mangaTitle,
                thumbnailUrl = thumbnailUrl,
            )
        }
    }
}
