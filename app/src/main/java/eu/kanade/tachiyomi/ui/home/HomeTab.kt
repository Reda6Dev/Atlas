package eu.kanade.tachiyomi.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.home.HomeScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import tachiyomi.domain.manga.model.Manga
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

        HomeScreen(
            state = state,
            onMangaClick = { manga ->
                navigator.push(MangaScreen(manga.id, true))
            },
            onRefresh = {
                screenModel.refresh()
            }
        )
    }
}
