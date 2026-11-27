package eu.kanade.tachiyomi.ui.home

import android.app.Application
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.manga.interactor.UpdateManga
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.util.FormattedError
import eu.kanade.presentation.util.toFormattedError
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.ui.home.model.HomeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mihon.domain.manga.model.toDomainManga
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.i18n.sy.SYMR
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
    private val getCategories: tachiyomi.domain.category.interactor.GetCategories = Injekt.get(),
    private val setMangaCategories: tachiyomi.domain.category.interactor.SetMangaCategories = Injekt.get(),
    private val getManga: tachiyomi.domain.manga.interactor.GetManga = Injekt.get(),
    private val initializeDefaultCategories: tachiyomi.domain.category.interactor.InitializeDefaultCategories = Injekt.get(),
    private val updateManga: UpdateManga = Injekt.get(),
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
        val dialog: Dialog? = null,

        // Latest Chapters Tab State
        val latestChaptersItems: List<HomeItem> = emptyList(),
        val latestChaptersPage: Int = 0,
        val isLoadingLatest: Boolean = true,
        val isLoadingMoreLatest: Boolean = false,
        val hasMoreLatest: Boolean = true,
        val latestError: FormattedError? = null,
        val latestErrorSourceId: Long? = null,

        // Discover Manga Tab State
        val discoverItems: List<HomeItem> = emptyList(),
        val discoverPage: Int = 0,
        val isLoadingDiscover: Boolean = true,
        val isLoadingMoreDiscover: Boolean = false,
        val hasMoreDiscover: Boolean = true,
        val discoverError: FormattedError? = null,
        val discoverErrorSourceId: Long? = null,
    )

    sealed interface Dialog {
        data class ChangeMangaCategory(
            val manga: tachiyomi.domain.manga.model.Manga,
            val initialSelection: List<tachiyomi.core.common.preference.CheckboxState.State<tachiyomi.domain.category.model.Category>>,
        ) : Dialog
    }

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
        screenModelScope.launchIO {
            sourceManager.isInitialized.collectLatest { isInit: Boolean ->
                if (isInit) {
                    checkAndRegisterNewSources()
                }
            }
        }
        screenModelScope.launchIO {
            val context = Injekt.get<Application>()
            val defaultNames = listOf(
                context.stringResource(SYMR.strings.default_category_read),
                context.stringResource(SYMR.strings.default_category_plan_to_read),
                context.stringResource(SYMR.strings.default_category_reading),
                context.stringResource(SYMR.strings.default_category_on_hold),
                context.stringResource(SYMR.strings.default_category_dropped),
            )
            initializeDefaultCategories.await(defaultNames)
        }
        loadData()
    }

    private suspend fun checkAndRegisterNewSources() {
        val enabledLangs = sourcePreferences.enabledLanguages.get()
        val disabledIds = sourcePreferences.disabledSources.get()

        val allVisibleSourceIds = sourceManager.getVisibleOnlineSources()
            .filterIsInstance<CatalogueSource>()
            .filter { it.lang in enabledLangs || "all" in enabledLangs }
            .filterNot { it.id.toString() in disabledIds }
            .map { it.id.toString() }
            .toSet()

        val isKnownInit = sourcePreferences.homeKnownSourcesInitialized.get()
        val knownSources = sourcePreferences.homeKnownSources.get()

        if (!isKnownInit) {
            sourcePreferences.homeKnownSources.set(allVisibleSourceIds)
            sourcePreferences.homeKnownSourcesInitialized.set(true)
        } else {
            val newSources = allVisibleSourceIds.filterNot { it in knownSources }
            if (newSources.isNotEmpty()) {
                val currentHomeSources = sourcePreferences.homeSources.get()
                val updatedHomeSources = if (currentHomeSources.contains("NONE")) {
                    newSources.toSet()
                } else {
                    currentHomeSources + newSources
                }
                sourcePreferences.homeSources.set(updatedHomeSources)
                sourcePreferences.homeKnownSources.set(knownSources + newSources)
            }
        }
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
                latestErrorSourceId = null,
                discoverError = null,
                discoverErrorSourceId = null,
            )
        }
        loadData()
    }

    fun getWebViewSource(preferredSourceId: Long?): HttpSource? {
        val candidateId = preferredSourceId ?: state.value.selectedSourceId
        val direct = candidateId?.let { sourceManager.get(it) as? HttpSource }
        if (direct != null) return direct
        return getActiveSources().singleOrNull() as? HttpSource
    }

    fun openSourceSettingsDialog() {
        val enabledLangs = sourcePreferences.enabledLanguages.get()
        val disabledIds = sourcePreferences.disabledSources.get()
        val homeSelectedSourceIds = sourcePreferences.homeSources.get()
        val savedOrderStr = sourcePreferences.homeSourcesOrder.get()
        val savedOrderList = if (savedOrderStr.isBlank()) emptyList() else savedOrderStr.split(",")

        val allAvailableSources = sourceManager.getVisibleOnlineSources()
            .filterIsInstance<CatalogueSource>()
            .filter { it.lang in enabledLangs || "all" in enabledLangs }
            .filterNot { it.id.toString() in disabledIds }
            .ifEmpty {
                sourceManager.getVisibleOnlineSources().filterIsInstance<CatalogueSource>()
            }

        val sourceMap = allAvailableSources.associateBy { it.id.toString() }
        val orderedSources = mutableListOf<CatalogueSource>()

        savedOrderList.forEach { id ->
            val src = sourceMap[id]
            if (src != null && src !in orderedSources) {
                orderedSources.add(src)
            }
        }
        allAvailableSources.forEach { src ->
            if (src !in orderedSources) {
                orderedSources.add(src)
            }
        }

        val dialogItems = orderedSources.map { source ->
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

    fun reorderDialogSourceItems(fromIndex: Int, toIndex: Int) {
        mutableState.update { state ->
            val list = state.sourceDialogItems.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            state.copy(sourceDialogItems = list)
        }
    }

    fun moveDialogSourceItem(index: Int, moveUp: Boolean) {
        val targetIndex = if (moveUp) index - 1 else index + 1
        reorderDialogSourceItems(index, targetIndex)
    }

    fun saveSourceSettingsDialog() {
        val selectedItems = state.value.sourceDialogItems.filter { it.isSelected }
        val selectedOrderedIds = selectedItems.map { it.id.toString() }
        val allDialogOrderedIds = state.value.sourceDialogItems.map { it.id.toString() }

        sourcePreferences.homeSourcesOrder.set(allDialogOrderedIds.joinToString(","))
        sourcePreferences.homeSources.set(if (selectedOrderedIds.isEmpty()) setOf("NONE") else selectedOrderedIds.toSet())

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
                latestErrorSourceId = null,
                discoverError = null,
                discoverErrorSourceId = null,
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

    private var loadDataJob: Job? = null

    private fun loadData() {
        loadDataJob?.cancel()
        loadDataJob = screenModelScope.launchIO {
            val allSources = getActiveSources()
            val availableFilterList = listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) }
            mutableState.update { it.copy(availableSources = availableFilterList) }

            val currentSelectedId = state.value.selectedSourceId

            loadLatestChapters(0, currentSelectedId, allSources, availableFilterList)
            loadDiscoverManga(0, currentSelectedId, allSources, availableFilterList)
            mutableState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun getActiveSources(): List<CatalogueSource> {
        val enabledLangs = sourcePreferences.enabledLanguages.get()
        val disabledIds = sourcePreferences.disabledSources.get()
        val homeSelectedSourceIds = sourcePreferences.homeSources.get()
        val savedOrderStr = sourcePreferences.homeSourcesOrder.get()
        val savedOrderList = if (savedOrderStr.isBlank()) emptyList() else savedOrderStr.split(",")

        val onlineSources = sourceManager.getVisibleOnlineSources()
            .filterIsInstance<CatalogueSource>()
            .filter { it.lang in enabledLangs || "all" in enabledLangs }
            .filterNot { it.id.toString() in disabledIds }

        val baseSources = if (onlineSources.isNotEmpty()) {
            onlineSources
        } else {
            sourceManager.getVisibleOnlineSources().filterIsInstance<CatalogueSource>()
        }

        if (homeSelectedSourceIds.contains("NONE")) return emptyList()

        val baseSourceMap = baseSources.associateBy { it.id.toString() }
        val orderedActiveSources = mutableListOf<CatalogueSource>()

        savedOrderList.forEach { id ->
            val src = baseSourceMap[id]
            if (src != null && (homeSelectedSourceIds.isEmpty() || id in homeSelectedSourceIds) && src !in orderedActiveSources) {
                orderedActiveSources.add(src)
            }
        }
        baseSources.forEach { src ->
            val id = src.id.toString()
            if ((homeSelectedSourceIds.isEmpty() || id in homeSelectedSourceIds) && src !in orderedActiveSources) {
                orderedActiveSources.add(src)
            }
        }

        return orderedActiveSources
    }

    fun loadMoreLatestChapters() {
        val currentState = state.value
        if (currentState.isLoadingLatest || currentState.isLoadingMoreLatest || !currentState.hasMoreLatest) return
        screenModelScope.launchIO {
            val allSources = getActiveSources()
            val availableFilterList = listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) }
            loadLatestChapters(currentState.latestChaptersPage + 1, currentState.selectedSourceId, allSources, availableFilterList)
        }
    }

    fun loadMoreDiscover() {
        val currentState = state.value
        if (currentState.isLoadingDiscover || currentState.isLoadingMoreDiscover || !currentState.hasMoreDiscover) return
        screenModelScope.launchIO {
            val allSources = getActiveSources()
            val availableFilterList = listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) }
            loadDiscoverManga(currentState.discoverPage + 1, currentState.selectedSourceId, allSources, availableFilterList)
        }
    }

    private suspend fun loadLatestChapters(
        page: Int,
        requestSelectedSourceId: Long? = state.value.selectedSourceId,
        precomputedSources: List<CatalogueSource>? = null,
        precomputedFilterList: List<HomeSourceFilter>? = null,
    ) {
        if (page > 0) {
            mutableState.update { it.copy(isLoadingMoreLatest = true) }
        }

        val allSources = precomputedSources ?: getActiveSources()
        val availableFilterList = precomputedFilterList ?: (listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) })

        val targetSources = if (requestSelectedSourceId != null) {
            allSources.filter { it.id == requestSelectedSourceId }
        } else {
            allSources
        }

        if (targetSources.isEmpty()) {
            if (state.value.selectedSourceId == requestSelectedSourceId) {
                val context = Injekt.get<Application>()
                val errorMessage = if (allSources.isEmpty()) {
                    context.stringResource(SYMR.strings.home_no_enabled_sources)
                } else {
                    context.stringResource(SYMR.strings.home_source_unavailable)
                }
                mutableState.update {
                    it.copy(
                        availableSources = availableFilterList,
                        isLoadingLatest = false,
                        isLoadingMoreLatest = false,
                        latestError = FormattedError(errorMessage),
                        latestErrorSourceId = null,
                    )
                }
            }
            return
        }

        val combinedItems = mutableListOf<HomeItem>()
        var anySuccess = false
        var lastException: Throwable? = null
        var lastFailedSourceId: Long? = null
        var hasNextPageAny = false

        for (source in targetSources) {
            if (state.value.selectedSourceId != requestSelectedSourceId) return

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
                lastFailedSourceId = source.id
            }
        }

        if (state.value.selectedSourceId != requestSelectedSourceId) return

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
                    latestErrorSourceId = null,
                )
            }
        } else {
            val context = Injekt.get<Application>()
            val formatted = lastException?.toFormattedError(context)
                ?: FormattedError(context.stringResource(SYMR.strings.error_generic))
            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingLatest = false,
                    isLoadingMoreLatest = false,
                    latestError = formatted,
                    latestErrorSourceId = lastFailedSourceId,
                )
            }
        }
    }

    private suspend fun loadDiscoverManga(
        page: Int,
        requestSelectedSourceId: Long? = state.value.selectedSourceId,
        precomputedSources: List<CatalogueSource>? = null,
        precomputedFilterList: List<HomeSourceFilter>? = null,
    ) {
        if (page > 0) {
            mutableState.update { it.copy(isLoadingMoreDiscover = true) }
        }

        val allSources = precomputedSources ?: getActiveSources()
        val availableFilterList = precomputedFilterList ?: (listOf(HomeSourceFilter(null, "الكل")) + allSources.map { HomeSourceFilter(it.id, it.name) })

        val targetSources = if (requestSelectedSourceId != null) {
            allSources.filter { it.id == requestSelectedSourceId }
        } else {
            allSources
        }

        if (targetSources.isEmpty()) {
            if (state.value.selectedSourceId == requestSelectedSourceId) {
                val context = Injekt.get<Application>()
                val errorMessage = if (allSources.isEmpty()) {
                    context.stringResource(SYMR.strings.home_no_enabled_sources)
                } else {
                    context.stringResource(SYMR.strings.home_source_unavailable)
                }
                mutableState.update {
                    it.copy(
                        availableSources = availableFilterList,
                        isLoadingDiscover = false,
                        isLoadingMoreDiscover = false,
                        discoverError = FormattedError(errorMessage),
                        discoverErrorSourceId = null,
                    )
                }
            }
            return
        }

        val combinedItems = mutableListOf<HomeItem>()
        var anySuccess = false
        var lastException: Throwable? = null
        var lastFailedSourceId: Long? = null
        var hasNextPageAny = false

        for (source in targetSources) {
            if (state.value.selectedSourceId != requestSelectedSourceId) return

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
                lastFailedSourceId = source.id
            }
        }

        if (state.value.selectedSourceId != requestSelectedSourceId) return

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
                    discoverErrorSourceId = null,
                )
            }
        } else {
            val context = Injekt.get<Application>()
            val formatted = lastException?.toFormattedError(context)
                ?: FormattedError(context.stringResource(SYMR.strings.error_generic))
            mutableState.update {
                it.copy(
                    availableSources = availableFilterList,
                    isLoadingDiscover = false,
                    isLoadingMoreDiscover = false,
                    discoverError = formatted,
                    discoverErrorSourceId = lastFailedSourceId,
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

    fun openChangeCategoryDialog(mangaId: Long) {
        screenModelScope.launchIO {
            val manga = getManga.await(mangaId) ?: return@launchIO
            val categories = getCategories.await().filterNot { it.isSystemCategory }
            val mangaCategories = getCategories.await(mangaId)
            val initialSelection = categories.map { category ->
                if (mangaCategories.any { it.id == category.id }) {
                    tachiyomi.core.common.preference.CheckboxState.State.Checked(category)
                } else {
                    tachiyomi.core.common.preference.CheckboxState.State.None(category)
                }
            }
            mutableState.update {
                it.copy(dialog = Dialog.ChangeMangaCategory(manga, initialSelection))
            }
        }
    }

    fun setMangaCategories(manga: tachiyomi.domain.manga.model.Manga, categories: List<Long>) {
        screenModelScope.launchIO {
            setMangaCategories.await(manga.id, categories)
            if (!manga.favorite) {
                updateManga.awaitUpdateFavorite(manga.id, true)
            }
            closeDialog()
        }
    }

    fun closeDialog() {
        mutableState.update { it.copy(dialog = null) }
    }

    fun onItemLongClick(item: HomeItem) {
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
                openChangeCategoryDialog(localMangaId)
            }
        }
    }
}
