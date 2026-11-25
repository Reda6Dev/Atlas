package eu.kanade.presentation.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.LogoHeader
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

val LocalOpenDrawer = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun AppNavigationDrawerSheet(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickHistory: () -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickFavoriteCharacters: () -> Unit = {},
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickBatchAdd: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val drawerWidth = (configuration.screenWidthDp.dp * 0.78f).coerceAtMost(320.dp)

    ModalDrawerSheet(
        modifier = modifier.width(drawerWidth),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        ScrollbarLazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
        ) {
            item {
                LogoHeader()
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.label_downloaded_only),
                    subtitle = stringResource(MR.strings.downloaded_only_summary),
                    icon = Icons.Outlined.CloudOff,
                    checked = downloadedOnly,
                    onCheckedChanged = onDownloadedOnlyChange,
                )
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.pref_incognito_mode),
                    subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                    icon = ImageVector.vectorResource(R.drawable.ic_glasses_24dp),
                    checked = incognitoMode,
                    onCheckedChanged = onIncognitoModeChange,
                )
            }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.history),
                    icon = Icons.Outlined.History,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickHistory()
                    },
                )
            }

            item {
                val downloadQueueState = downloadQueueStateProvider()
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_download_queue),
                    subtitle = when (downloadQueueState) {
                        DownloadQueueState.Stopped -> null
                        is DownloadQueueState.Paused -> {
                            val pending = downloadQueueState.pending
                            if (pending == 0) {
                                stringResource(MR.strings.paused)
                            } else {
                                "${stringResource(MR.strings.paused)} • ${
                                    pluralStringResource(
                                        MR.plurals.download_queue_summary,
                                        count = pending,
                                        pending,
                                    )
                                }"
                            }
                        }
                        is DownloadQueueState.Downloading -> {
                            val pending = downloadQueueState.pending
                            pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                        }
                    },
                    icon = Icons.Outlined.GetApp,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickDownloadQueue()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.categories),
                    icon = Icons.AutoMirrored.Outlined.Label,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickCategories()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = "شخصياتي المفضلة",
                    icon = Icons.Outlined.Person,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickFavoriteCharacters()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_stats),
                    icon = Icons.Outlined.QueryStats,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickStats()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_data_storage),
                    icon = Icons.Outlined.Storage,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickDataAndStorage()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(SYMR.strings.eh_batch_add),
                    icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickBatchAdd()
                    },
                )
            }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_settings),
                    icon = Icons.Outlined.Settings,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickSettings()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_about),
                    icon = Icons.Outlined.Info,
                    onPreferenceClick = {
                        onCloseDrawer()
                        onClickAbout()
                    },
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_help),
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    onPreferenceClick = {
                        onCloseDrawer()
                        uriHandler.openUri(Constants.URL_HELP)
                    },
                )
            }
        }
    }
}
