package eu.kanade.tachiyomi.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.home.HomeScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.webview.WebViewScreen
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

object HomeTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val title = stringResource(MR.strings.label_home)
            val icon = if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
            val image = rememberVectorPainter(icon)

            return TabOptions(
                index = 0u,
                title = title,
                icon = image,
            )
        }

    override suspend fun onReselect(navigator: cafe.adriel.voyager.navigator.Navigator) {
        // Handle reselect if needed
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val screenModel = rememberScreenModel { HomeScreenModel() }
        val state by screenModel.state.collectAsState()

        LaunchedEffect(screenModel) {
            screenModel.events.collectLatest { event ->
                when (event) {
                    is HomeScreenModel.Event.OpenManga -> {
                        navigator.push(MangaScreen(event.mangaId, true))
                    }
                    is HomeScreenModel.Event.ErrorLoading -> {
                        // Event error handled in UI state
                    }
                }
            }
        }

        HomeScreen(
            state = state,
            onRefresh = { screenModel.refresh() },
            onOpenSourceSettings = { screenModel.openSourceSettingsDialog() },
            onSearchClick = {
                val selectedId = state.selectedSourceId
                if (selectedId != null) {
                    navigator.push(BrowseSourceScreen(sourceId = selectedId, listingQuery = ""))
                } else {
                    navigator.push(GlobalSearchScreen(""))
                }
            },
            onDisplayModeChange = { mode -> screenModel.setDisplayMode(mode) },
            onToggleDialogSource = { sourceId -> screenModel.toggleDialogSourceSelection(sourceId) },
            onMoveDialogSource = { index, moveUp -> screenModel.moveDialogSourceItem(index, moveUp) },
            onSaveSourceSettings = { screenModel.saveSourceSettingsDialog() },
            onDismissSourceSettings = { screenModel.dismissSourceSettingsDialog() },
            onTabSelected = { tab -> screenModel.selectTab(tab) },
            onSourceFilterSelected = { sourceId -> screenModel.selectSource(sourceId) },
            onLoadMoreLatestChapters = { screenModel.loadMoreLatestChapters() },
            onLoadMoreDiscover = { screenModel.loadMoreDiscover() },
            onItemClick = { item -> screenModel.onItemClick(item) },
            onItemLongClick = { item -> screenModel.onItemLongClick(item) },
            onSetMangaCategories = { manga, categories -> screenModel.setMangaCategories(manga, categories) },
            onCloseDialog = { screenModel.closeDialog() },
            canOpenWebView = { errorSourceId -> screenModel.getWebViewSource(errorSourceId) != null },
            onOpenWebView = { errorSourceId ->
                screenModel.getWebViewSource(errorSourceId)?.let { source ->
                    navigator.push(
                        WebViewScreen(
                            url = source.baseUrl,
                            initialTitle = source.name,
                            sourceId = source.id,
                        ),
                    )
                }
            },
            onHelpClick = { uriHandler.openUri(Constants.URL_HELP) },
        )
    }
}
