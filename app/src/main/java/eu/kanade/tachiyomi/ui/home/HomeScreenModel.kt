package eu.kanade.tachiyomi.ui.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import eu.kanade.tachiyomi.ui.home.model.HomeSection
import eu.kanade.tachiyomi.ui.home.model.HomeSectionType
import exh.md.dto.ChapterDataDto
import exh.md.dto.MangaDataDto
import exh.md.service.MangaDexService
import exh.md.utils.MdConstants
import exh.md.utils.MdUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mihon.domain.manga.model.toDomainManga
import okhttp3.Headers
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class HomeScreenModel(
    private val networkHelper: NetworkHelper = Injekt.get(),
    private val getHistory: GetHistory = Injekt.get(),
    private val getLibraryManga: GetLibraryManga = Injekt.get(),
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
    private val sourceManager: SourceManager = Injekt.get(),
    private val sourcePreferences: SourcePreferences = Injekt.get(),
) : StateScreenModel<HomeScreenModel.State>(State()) {

    data class State(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val sections: List<HomeSection> = emptyList(),
        val error: String? = null,
    )

    sealed interface Event {
        data class OpenManga(val mangaId: Long) : Event
        data object ErrorLoading : Event
    }

    private val _events: Channel<Event> = Channel(Channel.UNLIMITED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private val mangadexService by lazy {
        val headers = Headers.Builder()
            .add("User-Agent", "Atlas v${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            .build()
        MangaDexService(networkHelper.client, headers)
    }

    private val mangaDexSourceId: Long by lazy {
        sourceManager.getOnlineSources().firstOrNull { it.id == exh.source.MANGADEX_SOURCE_ID }?.id
            ?: exh.source.MANGADEX_SOURCE_ID
    }

    init {
        loadData()
    }

    fun refresh() {
        mutableState.update { it.copy(isRefreshing = true, error = null) }
        loadData()
    }

    private fun loadData() {
        screenModelScope.launchIO {
            var networkErrorMessage: String? = null

            // 1. Load Local Recently Read Section
            val historySection = runCatching { loadRecentlyReadSection() }.getOrNull()

            // 2. Load Local Library Section
            val librarySection = runCatching { loadLibrarySection() }.getOrNull()

            // 3. Load MangaDex Latest Chapters Section
            val latestChaptersSection = runCatching { loadLatestChaptersSection() }
                .onFailure { networkErrorMessage = it.localizedMessage ?: "Failed to load latest chapters" }
                .getOrNull()

            // 4. Load MangaDex Discover Section
            val discoverSection = runCatching { loadDiscoverSection() }
                .onFailure {
                    if (networkErrorMessage == null) {
                        networkErrorMessage = it.localizedMessage ?: "Failed to load discover manga"
                    }
                }
                .getOrNull()

            val sections = listOfNotNull(
                latestChaptersSection,
                discoverSection,
                historySection,
                librarySection,
            )

            mutableState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    sections = sections,
                    error = if (sections.isEmpty()) networkErrorMessage ?: "No data available" else null,
                )
            }
        }
    }

    private suspend fun loadRecentlyReadSection(): HomeSection? {
        val historyItems = getHistory.subscribe("")
            .firstOrNull()
            ?.take(10) // Fixed to use take on Flow result directly or list
            ?.map { history ->
                val chapterText = if (history.chapterNumber >= 0) "Ch. ${history.chapterNumber.toInt()}" else null
                HomeItem.HistoryCard(
                    localMangaId = history.mangaId,
                    title = history.title,
                    thumbnailUrl = history.coverData.url ?: history.coverData.ogUrl,
                    chapterName = chapterText,
                )
            }
            .orEmpty()

        if (historyItems.isEmpty()) return null

        return HomeSection(
            id = "recently_read",
            title = "Recently Read",
            type = HomeSectionType.RecentlyRead,
            items = historyItems,
        )
    }

    private suspend fun loadLibrarySection(): HomeSection? {
        val libraryMangaList = getLibraryManga.await()
            .take(12)
            .map { libraryManga ->
                HomeItem.MangaCard(
                    domainManga = libraryManga.manga,
                    localMangaId = libraryManga.manga.id,
                    title = libraryManga.manga.title,
                    thumbnailUrl = libraryManga.manga.thumbnailUrl,
                )
            }

        if (libraryMangaList.isEmpty()) return null

        return HomeSection(
            id = "my_library",
            title = "My Library",
            type = HomeSectionType.MyLibrary,
            items = libraryMangaList,
        )
    }

    private suspend fun loadDiscoverSection(): HomeSection {
        val popularDto = mangadexService.fetchPopularManga(limit = 15, offset = 0)
        val items = popularDto.data.map { dto ->
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

        return HomeSection(
            id = "discover_manga",
            title = "Discover Manga",
            type = HomeSectionType.DiscoverManga,
            items = items,
        )
    }

    private suspend fun loadLatestChaptersSection(): HomeSection {
        val latestChaptersDto = mangadexService.fetchLatestChapters(
            limit = 15,
            offset = 0,
            languages = listOf("en", "ar"),
        )

        val chapterList = latestChaptersDto.data

        // Collect unique Manga IDs from chapter relationships
        val mangaIds = chapterList.mapNotNull { ch ->
            ch.relationships.firstOrNull { it.type == MdConstants.Types.manga }?.id
        }.distinct()

        // Batch fetch Manga info and Covers
        val mangaDetailsMap = if (mangaIds.isNotEmpty()) {
            runCatching {
                mangadexService.viewMangas(mangaIds).data.associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val items = chapterList.mapNotNull { chDto ->
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

        return HomeSection(
            id = "latest_chapters",
            title = "Latest Chapters",
            type = HomeSectionType.LatestChapters,
            items = items,
        )
    }

    fun onItemClick(item: HomeItem) {
        screenModelScope.launch {
            val localMangaId = when (item) {
                is HomeItem.HistoryCard -> item.localMangaId
                is HomeItem.MangaCard -> {
                    item.localMangaId ?: item.sManga?.let { sManga ->
                        withContext(Dispatchers.IO) {
                            networkToLocalManga(sManga.toDomainManga(mangaDexSourceId)).id
                        }
                    }
                }
                is HomeItem.ChapterCard -> {
                    withContext(Dispatchers.IO) {
                        networkToLocalManga(item.sManga.toDomainManga(mangaDexSourceId)).id
                    }
                }
            }

            if (localMangaId != null) {
                _events.send(Event.OpenManga(localMangaId))
            } else {
                _events.send(Event.ErrorLoading)
            }
        }
    }
}
