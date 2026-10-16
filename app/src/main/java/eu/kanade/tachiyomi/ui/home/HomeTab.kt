package eu.kanade.tachiyomi.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.home.HomeScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

object HomeTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val title = stringResource(MR.strings.label_home)
            val image = rememberVectorPainter(Icons.Outlined.Home)

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
            onSaveSourceSettings = { screenModel.saveSourceSettingsDialog() },
            onDismissSourceSettings = { screenModel.dismissSourceSettingsDialog() },
            onTabSelected = { tab -> screenModel.selectTab(tab) },
            onSourceFilterSelected = { sourceId -> screenModel.selectSource(sourceId) },
            onLoadMoreLatestChapters = { screenModel.loadMoreLatestChapters() },
            onLoadMoreDiscover = { screenModel.loadMoreDiscover() },
            onItemClick = { item -> screenModel.onItemClick(item) },
        )
    }
}
