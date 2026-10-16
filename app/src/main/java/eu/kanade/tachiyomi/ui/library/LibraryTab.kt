package eu.kanade.tachiyomi.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.category.components.ChangeCategoryDialog
import eu.kanade.presentation.history.HistoryScreen
import eu.kanade.presentation.history.components.HistoryDeleteAllDialog
import eu.kanade.presentation.history.components.HistoryDeleteDialog
import eu.kanade.presentation.library.DeleteLibraryMangaDialog
import eu.kanade.presentation.library.LibrarySettingsDialog
import eu.kanade.presentation.library.components.LibraryContent
import eu.kanade.presentation.library.components.LibraryToolbar
import eu.kanade.presentation.library.components.SyncFavoritesConfirmDialog
import eu.kanade.presentation.library.components.SyncFavoritesProgressDialog
import eu.kanade.presentation.library.components.SyncFavoritesWarningDialog
import eu.kanade.presentation.manga.DuplicateMangaDialog
import eu.kanade.presentation.manga.components.LibraryBottomActionMenu
import eu.kanade.presentation.more.onboarding.GETTING_STARTED_URL
import eu.kanade.presentation.updates.UpdateScreen
import eu.kanade.presentation.updates.UpdatesDeleteConfirmationDialog
import eu.kanade.presentation.updates.UpdatesFilterDialog
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.sync.SyncDataJob
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.category.CategoryScreen
import eu.kanade.tachiyomi.ui.history.HistoryScreenModel
import eu.kanade.tachiyomi.ui.home.HomeScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.updates.UpdatesScreenModel
import eu.kanade.tachiyomi.ui.updates.UpdatesSettingsScreenModel
import eu.kanade.tachiyomi.util.system.toast
import exh.favorites.FavoritesSyncStatus
import exh.recs.RecommendsScreen
import exh.recs.batch.RecommendationSearchBottomSheetDialog
import exh.recs.batch.RecommendationSearchProgressDialog
import exh.recs.batch.SearchStatus
import exh.source.MERGED_SOURCE_ID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mihon.feature.migration.config.MigrationConfigScreen
import mihon.feature.migration.dialog.MigrateMangaDialog
import mihon.feature.upcoming.UpcomingScreen
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryGroup
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.source.local.isLocal

data object LibraryTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_library_enter)
            return TabOptions(
                index = 0u,
                title = stringResource(MR.strings.label_library),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        requestOpenSettingsSheet()
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current

        var selectedSection by rememberSaveable { mutableIntStateOf(0) }

        val libraryScreenModel = rememberScreenModel { LibraryScreenModel() }
        val librarySettingsScreenModel = rememberScreenModel { LibrarySettingsScreenModel() }
        val libraryState by libraryScreenModel.state.collectAsState()

        val updatesScreenModel = rememberScreenModel { UpdatesScreenModel() }
        val updatesSettingsScreenModel = rememberScreenModel { UpdatesSettingsScreenModel() }
        val updatesState by updatesScreenModel.state.collectAsState()

        val historyScreenModel = rememberScreenModel { HistoryScreenModel() }
        val historyState by historyScreenModel.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        val onClickRefresh: (Category?) -> Boolean = { category ->
            val started = LibraryUpdateJob.startNow(
                context = context,
                category = if (libraryState.groupType == LibraryGroup.BY_DEFAULT) category else null,
                group = libraryState.groupType,
                groupExtra = when (libraryState.groupType) {
                    LibraryGroup.BY_DEFAULT -> null
                    LibraryGroup.BY_SOURCE, LibraryGroup.BY_TRACK_STATUS -> category?.id?.toString()
                    LibraryGroup.BY_STATUS -> category?.id?.minus(1)?.toString()
                    else -> null
                },
            )
            scope.launch {
                val msgRes = when {
                    !started -> MR.strings.update_already_running
                    category != null -> MR.strings.updating_category
                    else -> MR.strings.updating_library
                }
                snackbarHostState.showSnackbar(context.stringResource(msgRes))
            }
            started
        }

        Crossfade(
            targetState = selectedSection,
            label = "LibrarySubSections",
            modifier = Modifier.fillMaxSize(),
        ) { section ->
            when (section) {
                0 -> RenderLibrarySection(
                    navigator = navigator,
                    context = context,
                    scope = scope,
                    haptic = haptic,
                    screenModel = libraryScreenModel,
                    settingsScreenModel = librarySettingsScreenModel,
                    state = libraryState,
                    snackbarHostState = snackbarHostState,
                    onClickRefresh = onClickRefresh,
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it },
                )
                1 -> RenderUpdatesSection(
                    navigator = navigator,
                    context = context,
                    screenModel = updatesScreenModel,
                    settingsScreenModel = updatesSettingsScreenModel,
                    state = updatesState,
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it },
                )
                2 -> RenderHistorySection(
                    navigator = navigator,
                    screenModel = historyScreenModel,
                    state = historyState,
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it },
                )
            }
        }

        LaunchedEffect(selectedSection, libraryState.selectionMode, updatesState.selectionMode) {
            val isSelectionMode = when (selectedSection) {
                0 -> libraryState.selectionMode
                1 -> updatesState.selectionMode
                else -> false
            }
            HomeScreen.showBottomNav(!isSelectionMode)
        }

        LaunchedEffect(Unit) {
            launch { queryEvent.receiveAsFlow().collect(libraryScreenModel::search) }
            launch { requestSettingsSheetEvent.receiveAsFlow().collectLatest { libraryScreenModel.showSettingsDialog() } }
        }
    }

    @Composable
    private fun CustomSegmentedControl(
        selectedSection: Int,
        onSectionSelected: (Int) -> Unit,
        updatesBadgeCount: Int = 0,
        modifier: Modifier = Modifier,
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
        val outlineVariant = MaterialTheme.colorScheme.outlineVariant
        val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh

        val titles = listOf(
            stringResource(MR.strings.label_library),
            stringResource(MR.strings.label_recent_updates),
            stringResource(MR.strings.label_recent_manga),
        )

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            shape = CircleShape,
            color = surfaceContainer,
            border = BorderStroke(1.dp, outlineVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                titles.forEachIndexed { index, title ->
                    val isSelected = selectedSection == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (isSelected) primaryColor else Color.Transparent,
                                CircleShape,
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSectionSelected(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = title,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) onPrimaryColor else onSurfaceVariant,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (index == 1 && updatesBadgeCount > 0) {
                                Spacer(Modifier.width(4.dp))
                                Badge(
                                    containerColor = if (isSelected) onPrimaryColor else primaryColor,
                                    contentColor = if (isSelected) primaryColor else onPrimaryColor,
                                ) {
                                    Text(
                                        text = updatesBadgeCount.toString(),
                                        style = TextStyle(fontSize = 10.sp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RenderLibrarySection(
        navigator: Navigator,
        context: android.content.Context,
        scope: kotlinx.coroutines.CoroutineScope,
        haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
        screenModel: LibraryScreenModel,
        settingsScreenModel: LibrarySettingsScreenModel,
        state: LibraryScreenModel.State,
        snackbarHostState: SnackbarHostState,
        onClickRefresh: (Category?) -> Boolean,
        selectedSection: Int,
        onSectionSelected: (Int) -> Unit,
    ) {
        Scaffold(
            topBar = { scrollBehavior ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    val title = state.getToolbarTitle(
                        defaultTitle = stringResource(MR.strings.label_library),
                        defaultCategoryTitle = stringResource(MR.strings.label_default),
                        page = state.coercedActiveCategoryIndex,
                    )
                    LibraryToolbar(
                        hasActiveFilters = state.hasActiveFilters,
                        selectedCount = state.selection.size,
                        title = title,
                        onClickUnselectAll = screenModel::clearSelection,
                        onClickSelectAll = screenModel::selectAll,
                        onClickInvertSelection = screenModel::invertSelection,
                        onClickFilter = screenModel::showSettingsDialog,
                        onClickRefresh = { onClickRefresh(state.activeCategory) },
                        onClickGlobalUpdate = { onClickRefresh(null) },
                        onClickOpenRandomManga = {
                            scope.launch {
                                val randomItem = screenModel.getRandomLibraryItemForCurrentCategory()
                                if (randomItem != null) {
                                    navigator.push(MangaScreen(randomItem.libraryManga.manga.id))
                                } else {
                                    snackbarHostState.showSnackbar(
                                        context.stringResource(MR.strings.information_no_entries_found),
                                    )
                                }
                            }
                        },
                        onClickSyncNow = {
                            if (!SyncDataJob.isRunning(context)) {
                                SyncDataJob.startNow(context, manual = true)
                            } else {
                                context.toast(SYMR.strings.sync_in_progress)
                            }
                        },
                        onClickSyncExh = screenModel::openFavoritesSyncDialog.takeIf { state.showSyncExh },
                        isSyncEnabled = state.isSyncEnabled,
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = screenModel::search,
                        scrollBehavior = scrollBehavior.takeIf { !state.showCategoryTabs },
                    )
                    Spacer(Modifier.height(16.dp))
                    CustomSegmentedControl(
                        selectedSection = selectedSection,
                        onSectionSelected = onSectionSelected,
                    )
                    Spacer(Modifier.height(14.dp))
                }
            },
            bottomBar = {
                LibraryBottomActionMenu(
                    visible = state.selectionMode,
                    onChangeCategoryClicked = screenModel::openChangeCategoryDialog,
                    onMarkAsReadClicked = { screenModel.markReadSelection(true) },
                    onMarkAsUnreadClicked = { screenModel.markReadSelection(false) },
                    onDownloadClicked = screenModel::performDownloadAction
                        .takeIf { state.selectedManga.fastAll { !it.isLocal() } },
                    onDeleteClicked = screenModel::openDeleteMangaDialog,
                    onMigrateClicked = {
                        val selection = state.selectedManga
                            .filterNot { it.source == MERGED_SOURCE_ID }
                            .map { it.id }
                        screenModel.clearSelection()
                        if (selection.isNotEmpty()) {
                            navigator.push(MigrationConfigScreen(selection))
                        } else {
                            context.toast(SYMR.strings.no_valid_entry)
                        }
                    },
                    onClickCleanTitles = screenModel::cleanTitles.takeIf { state.showCleanTitles },
                    onClickCollectRecommendations = screenModel::showRecommendationSearchDialog.takeIf { state.selection.size > 1 },
                    onClickAddToMangaDex = screenModel::syncMangaToDex.takeIf { state.showAddToMangadex },
                    onClickResetInfo = screenModel::resetInfo.takeIf { state.showResetInfo },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { contentPadding ->
            when {
                state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
                state.searchQuery.isNullOrEmpty() && !state.hasActiveFilters && state.isLibraryEmpty -> {
                    val handler = LocalUriHandler.current
                    EmptyScreen(
                        stringRes = MR.strings.information_empty_library,
                        modifier = Modifier.padding(contentPadding),
                        actions = listOf(
                            EmptyScreenAction(
                                stringRes = MR.strings.getting_started_guide,
                                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                                onClick = { handler.openUri(GETTING_STARTED_URL) },
                            ),
                        ),
                    )
                }
                else -> {
                    LibraryContent(
                        categories = state.displayedCategories,
                        searchQuery = state.searchQuery,
                        selection = state.selection,
                        contentPadding = contentPadding,
                        currentPage = state.coercedActiveCategoryIndex,
                        hasActiveFilters = state.hasActiveFilters,
                        showPageTabs = state.showCategoryTabs || !state.searchQuery.isNullOrEmpty(),
                        onChangeCurrentPage = screenModel::updateActiveCategoryIndex,
                        onClickManga = { navigator.push(MangaScreen(it)) },
                        onContinueReadingClicked = { it: LibraryManga ->
                            scope.launchIO {
                                val chapter = screenModel.getNextUnreadChapter(it.manga)
                                if (chapter != null) {
                                    context.startActivity(
                                        ReaderActivity.newIntent(context, chapter.mangaId, chapter.id),
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(context.stringResource(MR.strings.no_next_chapter))
                                }
                            }
                            Unit
                        }.takeIf { state.showMangaContinueButton },
                        onToggleSelection = screenModel::toggleSelection,
                        onToggleRangeSelection = { category, manga ->
                            screenModel.toggleRangeSelection(category, manga)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onRefresh = { onClickRefresh(state.activeCategory) },
                        onGlobalSearchClicked = {
                            navigator.push(GlobalSearchScreen(screenModel.state.value.searchQuery ?: ""))
                        },
                        getItemCountForCategory = { state.getItemCountForCategory(it) },
                        getDisplayMode = { screenModel.getDisplayMode() },
                        getColumnsForOrientation = { screenModel.getColumnsForOrientation(it) },
                        getItemsForCategory = { state.getItemsForCategory(it) },
                    )
                }
            }
        }

        val onDismissRequest = screenModel::closeDialog
        when (val dialog = state.dialog) {
            is LibraryScreenModel.Dialog.SettingsSheet -> {
                LibrarySettingsDialog(
                    onDismissRequest = onDismissRequest,
                    screenModel = settingsScreenModel,
                    category = state.activeCategory,
                    hasCategories = state.libraryData.categories.fastAny { !it.isSystemCategory },
                )
            }
            is LibraryScreenModel.Dialog.ChangeCategory -> {
                ChangeCategoryDialog(
                    initialSelection = dialog.initialSelection,
                    onDismissRequest = onDismissRequest,
                    onEditCategories = {
                        screenModel.clearSelection()
                        navigator.push(CategoryScreen())
                    },
                    onConfirm = { include, exclude ->
                        screenModel.clearSelection()
                        screenModel.setMangaCategories(dialog.manga, include, exclude)
                    },
                )
            }
            is LibraryScreenModel.Dialog.DeleteManga -> {
                DeleteLibraryMangaDialog(
                    containsLocalManga = dialog.manga.any(Manga::isLocal),
                    onDismissRequest = onDismissRequest,
                    onConfirm = { deleteManga, deleteChapter ->
                        screenModel.removeMangas(dialog.manga, deleteManga, deleteChapter)
                        screenModel.clearSelection()
                    },
                )
            }
            LibraryScreenModel.Dialog.SyncFavoritesWarning -> {
                SyncFavoritesWarningDialog(
                    onDismissRequest = onDismissRequest,
                    onAccept = {
                        onDismissRequest()
                        screenModel.onAcceptSyncWarning()
                    },
                )
            }
            LibraryScreenModel.Dialog.SyncFavoritesConfirm -> {
                SyncFavoritesConfirmDialog(
                    onDismissRequest = onDismissRequest,
                    onAccept = {
                        onDismissRequest()
                        screenModel.runSync()
                    },
                )
            }
            is LibraryScreenModel.Dialog.RecommendationSearchSheet -> {
                RecommendationSearchBottomSheetDialog(
                    onDismissRequest = onDismissRequest,
                    onSearchRequest = {
                        onDismissRequest()
                        screenModel.clearSelection()
                        screenModel.runRecommendationSearch(dialog.manga)
                    },
                )
            }
            null -> {}
        }

        SyncFavoritesProgressDialog(
            status = screenModel.favoritesSync.status.collectAsState().value,
            setStatusIdle = { screenModel.favoritesSync.status.value = FavoritesSyncStatus.Idle },
            openManga = { navigator.push(MangaScreen(it)) },
        )

        RecommendationSearchProgressDialog(
            status = screenModel.recommendationSearch.status.collectAsState().value,
            setStatusIdle = { screenModel.recommendationSearch.status.value = SearchStatus.Idle },
            setStatusCancelling = { screenModel.recommendationSearch.status.value = SearchStatus.Cancelling },
        )

        BackHandler(enabled = state.selectionMode || state.searchQuery != null) {
            when {
                state.selectionMode -> screenModel.clearSelection()
                state.searchQuery != null -> screenModel.search(null)
            }
        }

        LaunchedEffect(state.isLoading) {
            if (!state.isLoading) {
                (context as? MainActivity)?.ready = true
            }
        }

        val recSearchState by screenModel.recommendationSearch.status.collectAsState()
        LaunchedEffect(recSearchState) {
            when (val current = recSearchState) {
                is SearchStatus.Finished.WithResults -> {
                    RecommendsScreen.Args.MergedSourceMangas(current.results)
                        .let(::RecommendsScreen)
                        .let(navigator::push)
                    screenModel.recommendationSearch.status.value = SearchStatus.Idle
                }
                is SearchStatus.Finished.WithoutResults -> {
                    context.toast(SYMR.strings.rec_no_results)
                    screenModel.recommendationSearch.status.value = SearchStatus.Idle
                }
                is SearchStatus.Cancelling -> {
                    screenModel.cancelRecommendationSearch()
                    screenModel.recommendationSearch.status.value = SearchStatus.Idle
                }
                else -> {}
            }
        }
    }

    @Composable
    private fun RenderUpdatesSection(
        navigator: Navigator,
        context: android.content.Context,
        screenModel: UpdatesScreenModel,
        settingsScreenModel: UpdatesSettingsScreenModel,
        state: UpdatesScreenModel.State,
        selectedSection: Int,
        onSectionSelected: (Int) -> Unit,
    ) {
        UpdateScreen(
            state = state,
            snackbarHostState = screenModel.snackbarHostState,
            lastUpdated = screenModel.lastUpdated,
            preserveReadingPosition = screenModel.preserveReadingPosition,
            onClickCover = { item -> navigator.push(MangaScreen(item.update.mangaId)) },
            onSelectAll = screenModel::toggleAllSelection,
            onInvertSelection = screenModel::invertSelection,
            onUpdateLibrary = screenModel::updateLibrary,
            onDownloadChapter = screenModel::downloadChapters,
            onMultiBookmarkClicked = screenModel::bookmarkUpdates,
            onMultiMarkAsReadClicked = screenModel::markUpdatesRead,
            onMultiDeleteClicked = screenModel::showConfirmDeleteChapters,
            onUpdateSelected = screenModel::toggleSelection,
            onOpenChapter = {
                val intent = ReaderActivity.newIntent(context, it.update.mangaId, it.update.chapterId)
                context.startActivity(intent)
            },
            onCalendarClicked = { navigator.push(UpcomingScreen()) },
            onFilterClicked = screenModel::showFilterDialog,
            hasActiveFilters = state.hasActiveFilters,
            sectionBar = {
                Spacer(Modifier.height(16.dp))
                CustomSegmentedControl(
                    selectedSection = selectedSection,
                    onSectionSelected = onSectionSelected,
                )
                Spacer(Modifier.height(14.dp))
            },
        )

        val onDismissDialog = { screenModel.setDialog(null) }
        when (val dialog = state.dialog) {
            is UpdatesScreenModel.Dialog.DeleteConfirmation -> {
                UpdatesDeleteConfirmationDialog(
                    onDismissRequest = onDismissDialog,
                    onConfirm = { screenModel.deleteChapters(dialog.toDelete) },
                )
            }
            is UpdatesScreenModel.Dialog.FilterSheet -> {
                UpdatesFilterDialog(
                    onDismissRequest = onDismissDialog,
                    screenModel = settingsScreenModel,
                )
            }
            null -> {}
        }

        LaunchedEffect(Unit) {
            screenModel.events.collectLatest { event ->
                when (event) {
                    UpdatesScreenModel.Event.InternalError -> screenModel.snackbarHostState.showSnackbar(
                        context.stringResource(MR.strings.internal_error),
                    )
                    is UpdatesScreenModel.Event.LibraryUpdateTriggered -> {
                        val msg = if (event.started) {
                            MR.strings.updating_library
                        } else {
                            MR.strings.update_already_running
                        }
                        screenModel.snackbarHostState.showSnackbar(context.stringResource(msg))
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            screenModel.resetNewUpdatesCount()
            onDispose {
                screenModel.resetNewUpdatesCount()
            }
        }
    }

    @Composable
    private fun RenderHistorySection(
        navigator: Navigator,
        screenModel: HistoryScreenModel,
        state: HistoryScreenModel.State,
        selectedSection: Int,
        onSectionSelected: (Int) -> Unit,
    ) {
        HistoryScreen(
            state = state,
            snackbarHostState = screenModel.snackbarHostState,
            onSearchQueryChange = screenModel::updateSearchQuery,
            onClickCover = { navigator.push(MangaScreen(it)) },
            onClickResume = screenModel::getNextChapterForManga,
            onDialogChange = screenModel::setDialog,
            onClickFavorite = screenModel::addFavorite,
            sectionBar = {
                Spacer(Modifier.height(16.dp))
                CustomSegmentedControl(
                    selectedSection = selectedSection,
                    onSectionSelected = onSectionSelected,
                )
                Spacer(Modifier.height(14.dp))
            },
        )

        val onDismissRequest = { screenModel.setDialog(null) }
        when (val dialog = state.dialog) {
            is HistoryScreenModel.Dialog.Delete -> {
                HistoryDeleteDialog(
                    onDismissRequest = onDismissRequest,
                    onDelete = { all ->
                        if (all) {
                            screenModel.removeAllFromHistory(dialog.history.mangaId)
                        } else {
                            screenModel.removeFromHistory(dialog.history)
                        }
                    },
                )
            }
            is HistoryScreenModel.Dialog.DeleteAll -> {
                HistoryDeleteAllDialog(
                    onDismissRequest = onDismissRequest,
                    onDelete = screenModel::removeAllHistory,
                )
            }
            is HistoryScreenModel.Dialog.DuplicateManga -> {
                DuplicateMangaDialog(
                    duplicates = dialog.duplicates,
                    onDismissRequest = onDismissRequest,
                    onConfirm = { screenModel.addFavorite(dialog.manga) },
                    onOpenManga = { navigator.push(MangaScreen(it.id)) },
                    onMigrate = { screenModel.showMigrateDialog(dialog.manga, it) },
                )
            }
            is HistoryScreenModel.Dialog.ChangeCategory -> {
                ChangeCategoryDialog(
                    initialSelection = dialog.initialSelection,
                    onDismissRequest = onDismissRequest,
                    onEditCategories = { navigator.push(CategoryScreen()) },
                    onConfirm = { include, _ ->
                        screenModel.moveMangaToCategoriesAndAddToLibrary(dialog.manga, include)
                    },
                )
            }
            is HistoryScreenModel.Dialog.Migrate -> {
                MigrateMangaDialog(
                    current = dialog.current,
                    target = dialog.target,
                    onClickTitle = { navigator.push(MangaScreen(dialog.current.id)) },
                    onDismissRequest = onDismissRequest,
                )
            }
            null -> {}
        }
    }

    private val queryEvent = Channel<String>()
    suspend fun search(query: String) = queryEvent.send(query)

    private val requestSettingsSheetEvent = Channel<Unit>()
    private suspend fun requestOpenSettingsSheet() = requestSettingsSheetEvent.send(Unit)
}
