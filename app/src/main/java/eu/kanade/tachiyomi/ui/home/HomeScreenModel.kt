package eu.kanade.tachiyomi.ui.home

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mihon.domain.manga.model.toDomainManga
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

enum class HomeTabOption {
    LatestChapters,
    DiscoverManga,
}

data class HomeSourceFilter(
    val id: Long?,
    val name: String,
)

data class HomeSourceDialogItem(
    val id: Long,
    val name: String,
    val isSelected: Boolean,
)

class HomeScreenModel(
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
    private val sourceManager: SourceManager = Injekt.get(),
    private val sourcePreferences: SourcePreferences = Injekt.get(),
    private val uiPreferences: UiPreferences = Injekt.get(),
) : StateScreenModel<HomeScreenModel.State>(
    State(displayMode = uiPreferences.homeDisplayMode.get()),
) {

    data class State(
        val selectedTab: HomeTabOption = HomeTabOption.LatestChapters,
        val selectedSourceId: Long? = null,
        val availableSources: List<HomeSourceFilter> = emptyList(),
        val displayMode: LibraryDisplayMode = LibraryDisplayMode.CompactGrid,
        val isRefreshing: Boolean = false,
        val showSourceSettingsDialog: Boolean = false,
        val sourceDialogItems: List<HomeSourceDialogItem> = emptyList(),

        // Latest Chapters Tab State
        val latestChaptersItems: List<HomeItem> = emptyList(),
        val latestChaptersPage: Int = 0,
        val isLoadingLatest: Boolean = true,
        val isLoadingMoreLatest: Boolean = false,
        val hasMoreLatest: Boolean = true,
        val latestError: String? = null,

        // Discover Manga Tab State
        val discoverItems: List<HomeItem> = emptyList(),
        val discoverPage: Int = 0,
        val isLoadingDiscover: Boolean = true,
        val isLoadingMoreDiscover: Boolean = false,
        val hasMoreDiscover: Boolean = true,
        val discoverError: String? = null,
    )

    sealed interface Event {
        data class OpenManga(val mangaId: Long) : Event
        data object ErrorLoading : Event
    }

    private val _events: Channel<Event> = Channel(Channel.UNLIMITED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private val mangaDexSourceId: Long by lazy {
        sourceManager.getOnlineSources().firstOrNull { it.id == exh.source.MANGADEX_SOURCE_ID }?.id
            ?: exh.source.MANGADEX_SOURCE_ID
    }

    init {
        loadData()
    }

    fun selectTab(tab: HomeTabOption) {
        mutableState.update { it.copy(selectedTab = tab) }
    }

    fun setDisplayMode(mode: LibraryDisplayMode) {
        uiPreferences.homeDisplayMode.set(mode)
        mutableState.update { it.copy(displayMode = mode) }
    }

    fun selectSource(sourceId: Long?) {
        if (state.value.selectedSourceId == sourceId && state.value.availableSources.isNotEmpty()) return
        mutableState.update {
            it.copy(
                selectedSourceId = sourceId,
                latestChaptersPage = 0,
                discoverPage = 0,
                hasMoreLatest = true,
                hasMoreDiscover = true,
                latestChaptersItems = emptyList(),
                discoverItems = emptyList(),
                isLoadingLatest = true,
                isLoadingDiscover = true,
                latestError = null,
                discoverError = null,
            )
        }
        loadData()
    }

    fun openSourceSettingsDialog() {
        val enabledLangs = sourcePreferences.enabledLanguages.get()
        val disabledIds = sourcePreferences.disabledSources.get()
        val homeSelectedSourceIds = sourcePreferences.homeSources.get()

        val allAvailableSources = sourceManager.getVisibleOnlineSources()
            .filterIsInstance<CatalogueSource>()
            .filter { it.lang in enabledLangs || "all" in enabledLangs }
            .filterNot { it.id.toString() in disabledIds }
            .ifEmpty {
                sourceManager.getOnlineSources().filterIsInstance<CatalogueSource>()
            }

        val dialogItems = allAvailableSources.map { source ->
            val isSelected = if (homeSelectedSourceIds.isEmpty()) {
                true
            } else if (homeSelectedSourceIds.contains("NONE")) {
                false
            } else {
                source.id.toString() in homeSelectedSourceIds
            }
            HomeSourceDialogItem(
                id = source.id,
                name = source.name,
                isSelected = isSelected,
            )
        }

        mutableState.update {
            it.copy(
                showSourceSettingsDialog = true,
                sourceDialogItems = dialogItems,
            )
        }
    }

    fun toggleDialogSourceSelection(sourceId: Long) {
        mutableState.update { state ->
            val updatedItems = state.sourceDialogItems.map { item ->
                if (item.id == sourceId) item.copy(isSelected = !item.isSelected) else item
            }
            state.copy(sourceDialogItems = updatedItems)
        }
    }

    fun saveSourceSettingsDialog() {
        val selectedIds = state.value.sourceDialogItems
            .filter { it.isSelected }
            .map { it.id.toString() }
            .toSet()

        val toSave = if (selectedIds.isEmpty()) setOf("NONE") else selectedIds
        sourcePreferences.homeSources.set(toSave)

        mutableState.update {
            it.copy(
                showSourceSettingsDialog = false,
                selectedSourceId = null,
                latestChaptersPage = 0,
                discoverPage = 0,
                hasMoreLatest = true,
                hasMoreDiscover = true,
                latestChaptersItems = emptyList(),
                discoverItems = emptyList(),
                isLoadingLatest = true,
                isLoadingDiscover = true,
            )
        }
        loadData()
    }

    fun dismissSourceSettingsDialog() {
        mutableState.update { it.copy(showSourceSettingsDialog = false) }
    }

    fun refresh() {
        if (state.value.isRefreshing) return
        mutableState.update {
            it.copy(
                isRefreshing = true,
                latestError = null,
                discoverError = null,
                latestChaptersPage = 0,
                discoverPage = 0,
                hasMoreLatest = true,
                hasMoreDiscover = true,
                latestChaptersItems = emptyList(),
                discoverItems = emptyList(),
                isLoadingLatest = true,
                isLoadingDiscover = true,
            )
        }
        loadData()
    }

    private fun loadData() {
        screenModelScope.launchIO {
            loadLatestChapters(0)
            loadDiscoverManga(0)
            mutableState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun getActiveSources(): List<CatalogueSource> {
        val enabledLangs = sourcePreferences.enabledLanguages.get()
        val disabledIds = sourcePreferences.disabledSources.get()
        val homeSelectedSourceIds = sourcePreferences.homeSources.get()

        val onlineSources = sourceManager.getVisibleOnlineSources()
            .filterIsInstance<CatalogueSource>()
            .filter { it.lang in enabledLangs || "all" in enabledLangs }
            .filterNot { it.id.toString() in disabledIds }

        val baseSources = if (onlineSources.isNotEmpty()) {
            onlineSources
        } else {
            sourceManager.getOnlineSources()
                .filterIsInstance<CatalogueSource>()
                .ifEmpty {
                    listOfNotNull(sourceManager.get(exh.source.MANGADEX_SOURCE_ID) as? CatalogueSource)
                }
        }

        return when {
            homeSelectedSourceIds.isEmpty() -> baseSources
            homeSelectedSourceIds.contains("NONE") -> emptyList()
            else -> baseSources.filter { it.id.toString() in homeSelectedSourceIds }
        }
    }

    fun loadMoreLatestChapters() {
        val currentState = state.value
        if (currentState.isLoadingLatest || currentState.isLoadingMoreLatest || !currentState.hasMoreLatest) return
        screenModelScope.launchIO {
            loadLatestChapters(currentState.latestChaptersPage + 1)
        }
    }

    fun loadMoreDiscover() {
        val currentState = state.value
        if (currentState.isLoadingDiscover || currentState.isLoadingMoreDiscover || !currentState.hasMoreDiscover) return
        screenModelScope.launchIO {
            loadDiscoverManga(currentState.discoverPage + 1)
        }
    }

    private suspend fun loadLatestChapters(page: Int) {
        if (page > 0) {
            mutableState.update { it.copy(isLoadingMoreLatest = true) }
        }

        val allSources = getActiveSources()
        val availableFilterList = listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) }

        val selectedId = state.value.selectedSourceId
        val targetSources = if (selectedId != null) {
            allSources.filter { it.id == selectedId }
        } else {
            allSources
        }

        if (targetSources.isEmpty()) {
            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingLatest = false,
                    isLoadingMoreLatest = false,
                    latestError = if (allSources.isEmpty()) "لا توجد مصادر مفعلة للشاشة الرئيسية." else "تعذر التوصل لمحتوى من المصدر المالي.",
                )
            }
            return
        }

        val combinedItems = mutableListOf<HomeItem>()
        var anySuccess = false
        var lastException: Throwable? = null
        var hasNextPageAny = false

        for (source in targetSources) {
            val result = runCatching {
                val mangasPage = source.getLatestUpdates(page + 1)
                val items = mangasPage.mangas.map { sManga ->
                    HomeItem.MangaCard(
                        sManga = sManga,
                        title = sManga.title,
                        thumbnailUrl = sManga.thumbnail_url,
                        subtitle = null,
                        sourceId = source.id,
                        sourceName = source.name,
                    )
                }
                Pair(items, mangasPage.hasNextPage)
            }

            if (result.isSuccess) {
                val (items, hasNext) = result.getOrThrow()
                combinedItems.addAll(items)
                if (hasNext) hasNextPageAny = true
                anySuccess = true
            } else {
                lastException = result.exceptionOrNull()
            }
        }

        if (anySuccess) {
            val currentItems = state.value.latestChaptersItems
            val existingKeys = currentItems.map { getItemKey(it) }.toSet()
            val uniqueNewItems = combinedItems.filter { getItemKey(it) !in existingKeys }
            val allItems = if (page == 0) combinedItems.distinctBy { getItemKey(it) } else currentItems + uniqueNewItems

            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingLatest = false,
                    isLoadingMoreLatest = false,
                    latestChaptersItems = allItems,
                    latestChaptersPage = page,
                    hasMoreLatest = hasNextPageAny && combinedItems.isNotEmpty(),
                    latestError = null,
                )
            }
        } else {
            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingLatest = false,
                    isLoadingMoreLatest = false,
                    latestError = lastException?.localizedMessage ?: "تعذر تحميل البيانات من المصادر المتاحة.",
                )
            }
        }
    }

    private suspend fun loadDiscoverManga(page: Int) {
        if (page > 0) {
            mutableState.update { it.copy(isLoadingMoreDiscover = true) }
        }

        val allSources = getActiveSources()
        val availableFilterList = listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) }

        val selectedId = state.value.selectedSourceId
        val targetSources = if (selectedId != null) {
            allSources.filter { it.id == selectedId }
        } else {
            allSources
        }

        if (targetSources.isEmpty()) {
            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingDiscover = false,
                    isLoadingMoreDiscover = false,
                    discoverError = if (allSources.isEmpty()) "لا توجد مصادر مفعلة للشاشة الرئيسية." else "تعذر التوصل لمحتوى من المصدر المالي.",
                )
            }
            return
        }

        val combinedItems = mutableListOf<HomeItem>()
        var anySuccess = false
        var lastException: Throwable? = null
        var hasNextPageAny = false

        for (source in targetSources) {
            val result = runCatching {
                val mangasPage = source.getPopularManga(page + 1)
                val items = mangasPage.mangas.map { sManga ->
                    HomeItem.MangaCard(
                        sManga = sManga,
                        title = sManga.title,
                        thumbnailUrl = sManga.thumbnail_url,
                        subtitle = null,
                        sourceId = source.id,
                        sourceName = source.name,
                    )
                }
                Pair(items, mangasPage.hasNextPage)
            }

            if (result.isSuccess) {
                val (items, hasNext) = result.getOrThrow()
                combinedItems.addAll(items)
                if (hasNext) hasNextPageAny = true
                anySuccess = true
            } else {
                lastException = result.exceptionOrNull()
            }
        }

        if (anySuccess) {
            val currentItems = state.value.discoverItems
            val existingKeys = currentItems.map { getItemKey(it) }.toSet()
            val uniqueNewItems = combinedItems.filter { getItemKey(it) !in existingKeys }
            val allItems = if (page == 0) combinedItems.distinctBy { getItemKey(it) } else currentItems + uniqueNewItems

            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingDiscover = false,
                    isLoadingMoreDiscover = false,
                    discoverItems = allItems,
                    discoverPage = page,
                    hasMoreDiscover = hasNextPageAny && combinedItems.isNotEmpty(),
                    discoverError = null,
                )
            }
        } else {
            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingDiscover = false,
                    isLoadingMoreDiscover = false,
                    discoverError = lastException?.localizedMessage ?: "تعذر تحميل البيانات من المصادر المتاحة.",
                )
            }
        }
    }

    private fun getItemKey(item: HomeItem): String {
        return when (item) {
            is HomeItem.MangaCard -> "${item.sourceId}_${item.sManga?.url ?: item.mangaDexUuid ?: item.localMangaId.toString()}"
            is HomeItem.ChapterCard -> "${item.sourceId}_${item.chapterId.ifBlank { item.sManga.url }}"
            is HomeItem.HistoryCard -> item.localMangaId.toString()
        }
    }

    fun onItemClick(item: HomeItem) {
        screenModelScope.launch {
            val targetSourceId = when (item) {
                is HomeItem.MangaCard -> item.sourceId
                is HomeItem.ChapterCard -> item.sourceId
                is HomeItem.HistoryCard -> null
            } ?: mangaDexSourceId

            val localMangaId = when (item) {
                is HomeItem.HistoryCard -> item.localMangaId
                is HomeItem.MangaCard -> {
                    item.localMangaId ?: item.sManga?.let { sManga ->
                        withContext(Dispatchers.IO) {
                            networkToLocalManga(sManga.toDomainManga(targetSourceId)).id
                        }
                    }
                }
                is HomeItem.ChapterCard -> {
                    withContext(Dispatchers.IO) {
                        networkToLocalManga(item.sManga.toDomainManga(targetSourceId)).id
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
