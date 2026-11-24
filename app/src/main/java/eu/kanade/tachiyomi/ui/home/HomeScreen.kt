import android.view.ViewConfiguration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.core.preference.asState
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppNavigationDrawerSheet
import eu.kanade.presentation.components.LocalOpenDrawer
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.isTabletUi
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.category.CategoryScreen
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreScreenModel
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.ui.setting.SettingsScreen
import eu.kanade.tachiyomi.ui.stats.StatsScreen
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import exh.ui.batchadd.BatchAddScreen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import logcat.LogPriority
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.NavigationRail
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.abs

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()
    private val showBottomNavEvent = Channel<Boolean>()

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 200

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val scope = rememberCoroutineScope()
        val alwaysShowLabel by remember {
            Injekt.get<UiPreferences>().bottomBarLabels.asState(scope)
        }

        val snackbarHostState = remember { SnackbarHostState() }

        val onResumeClick: () -> Unit = {
            scope.launch {
                try {
                    val historyRepository = Injekt.get<HistoryRepository>()
                    val lastHistory = withIOContext { historyRepository.getLastHistory() }
                    if (lastHistory != null) {
                        val intent = ReaderActivity.newIntent(context, lastHistory.mangaId, lastHistory.chapterId)
                        context.startActivity(intent)
                    } else {
                        snackbarHostState.showSnackbar(context.stringResource(MR.strings.no_next_chapter))
                    }
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, throwable = e) { "Failed to resume reading" }
                    snackbarHostState.showSnackbar(context.stringResource(MR.strings.no_next_chapter))
                }
            }
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val openDrawer: () -> Unit = remember(drawerState, scope) {
            { scope.launch { drawerState.open() } }
        }
        val moreScreenModel = rememberScreenModel { MoreScreenModel() }
        val downloadQueueState by moreScreenModel.downloadQueueState.collectAsState()

        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        val touchSlop = ViewConfiguration.get(LocalContext.current).scaledTouchSlop.toFloat()
        val edgeWidthPx = with(density) { 24.dp.toPx() }

        val edgeSwipeModifier = Modifier.pointerInput(layoutDirection, drawerState.isOpen) {
            if (drawerState.isOpen) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                    val down = downEvent.changes.firstOrNull() ?: continue
                    val startX = down.position.x
                    val startY = down.position.y

                    val isFromEdge = when (layoutDirection) {
                        LayoutDirection.Rtl -> startX >= size.width - edgeWidthPx
                        LayoutDirection.Ltr -> startX <= edgeWidthPx
                    }

                    if (!isFromEdge) continue

                    var isEdgeDragInwards = false
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!pointer.pressed) break

                        val dx = pointer.position.x - startX
                        val dy = pointer.position.y - startY

                        val dragInwardsPx = when (layoutDirection) {
                            LayoutDirection.Rtl -> -dx
                            LayoutDirection.Ltr -> dx
                        }

                        if (dragInwardsPx > touchSlop && dragInwardsPx > abs(dy)) {
                            isEdgeDragInwards = true
                            pointer.consume()
                        }

                        if (isEdgeDragInwards) {
                            pointer.consume()
                        }
                    }

                    if (isEdgeDragInwards) {
                        scope.launch { drawerState.open() }
                    }
                }
            }
        }

        TabNavigator(
            tab = HomeTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->
            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalOpenDrawer provides openDrawer,
            ) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerState.isOpen,
                    drawerContent = {
                        AppNavigationDrawerSheet(
                            downloadQueueStateProvider = { downloadQueueState },
                            downloadedOnly = moreScreenModel.downloadedOnly,
                            onDownloadedOnlyChange = { moreScreenModel.downloadedOnly = it },
                            incognitoMode = moreScreenModel.incognitoMode,
                            onIncognitoModeChange = { moreScreenModel.incognitoMode = it },
                            onClickHistory = { navigator.push(HistoryTab) },
                            onClickDownloadQueue = { navigator.push(DownloadQueueScreen) },
                            onClickCategories = { navigator.push(CategoryScreen()) },
                            onClickStats = { navigator.push(StatsScreen()) },
                            onClickDataAndStorage = { navigator.push(SettingsScreen(SettingsScreen.Destination.DataAndStorage)) },
                            onClickSettings = { navigator.push(SettingsScreen()) },
                            onClickAbout = { navigator.push(SettingsScreen(SettingsScreen.Destination.About)) },
                            onClickBatchAdd = { navigator.push(BatchAddScreen()) },
                            onCloseDrawer = { scope.launch { drawerState.close() } },
                        )
                    },
                ) {
                    Scaffold(
                        modifier = edgeSwipeModifier,
                        startBar = {
                            if (isTabletUi()) {
                                NavigationRail {
                                    if (HomeTab.isEnabled()) NavigationRailItem(HomeTab, alwaysShowLabel)
                                    if (LibraryTab.isEnabled()) NavigationRailItem(LibraryTab, alwaysShowLabel)
                                    if (BrowseTab.isEnabled()) NavigationRailItem(BrowseTab, alwaysShowLabel)
                                }
                            }
                        },
                        bottomBar = {
                            if (!isTabletUi()) {
                                val bottomNavVisible by produceState(initialValue = true) {
                                    showBottomNavEvent.receiveAsFlow().collectLatest { value = it }
                                }
                                AnimatedVisibility(
                                    visible = bottomNavVisible,
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    CustomFloatingNavigationBar(
                                        currentTab = tabNavigator.current,
                                        onTabSelected = { tab ->
                                            if (tabNavigator.current::class != tab::class) {
                                                tabNavigator.current = tab
                                            } else {
                                                scope.launch { tab.onReselect(navigator) }
                                            }
                                        },
                                    )
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets(0),
                    ) { contentPadding ->
                        Box(
                            modifier = Modifier
                                .padding(contentPadding)
                                .consumeWindowInsets(contentPadding),
                        ) {
                            AnimatedContent(
                                targetState = tabNavigator.current,
                                transitionSpec = {
                                    materialFadeThroughIn(initialScale = 1f, durationMillis = TabFadeDuration) togetherWith
                                        materialFadeThroughOut(durationMillis = TabFadeDuration)
                                },
                                label = "tabContent",
                            ) {
                                tabNavigator.saveableState(key = "currentTab", it) {
                                    it.Content()
                                }
                            }
                        }
                    }
                }
            }

            val goToLibraryTab = { tabNavigator.current = HomeTab }

            BackHandler(enabled = tabNavigator.current != HomeTab, onBack = goToLibraryTab)

            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        goToLibraryTab()
                        LibraryTab.search(it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = when (it) {
                            is Tab.Library -> LibraryTab
                            Tab.Updates -> UpdatesTab
                            Tab.History -> HistoryTab
                            is Tab.Browse -> {
                                if (it.toExtensions) {
                                    BrowseTab.showExtension()
                                }
                                BrowseTab
                            }

                            is Tab.More -> MoreTab
                        }

                        if (it is Tab.Library && it.mangaIdToOpen != null) {
                            navigator.push(MangaScreen(it.mangaIdToOpen))
                        }
                        if (it is Tab.More && it.toDownloads) {
                            navigator.push(DownloadQueueScreen)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CustomFloatingNavigationBar(
        currentTab: cafe.adriel.voyager.navigator.tab.Tab,
        onTabSelected: (eu.kanade.presentation.util.Tab) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val navBarsInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(bottom = 16.dp + navBarsInset),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(34.dp),
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.25f),
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(34.dp),
                    )
                    .clip(RoundedCornerShape(34.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (HomeTab.isEnabled()) {
                        FloatingNavItem(
                            tab = HomeTab,
                            isSelected = currentTab::class == HomeTab::class,
                            onClick = { onTabSelected(HomeTab) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (LibraryTab.isEnabled()) {
                        FloatingNavItem(
                            tab = LibraryTab,
                            isSelected = currentTab::class == LibraryTab::class,
                            onClick = { onTabSelected(LibraryTab) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (BrowseTab.isEnabled()) {
                        FloatingNavItem(
                            tab = BrowseTab,
                            isSelected = currentTab::class == BrowseTab::class,
                            onClick = { onTabSelected(BrowseTab) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun FloatingNavItem(
        tab: eu.kanade.presentation.util.Tab,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

        Box(
            modifier = modifier
                .fillMaxHeight()
                .clickable(
                    onClick = onClick,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.16f),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp),
                ) {}
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BadgedBox(
                    badge = {
                        when {
                            tab is UpdatesTab -> {
                                val count by produceState(initialValue = 0) {
                                    val pref = Injekt.get<LibraryPreferences>()
                                    combine(
                                        pref.newShowUpdatesCount.changes(),
                                        pref.newUpdatesCount.changes(),
                                    ) { show, count -> if (show) count else 0 }
                                        .collectLatest { value = it }
                                }
                                if (count > 0) {
                                    Badge {
                                        Text(text = count.toString())
                                    }
                                }
                            }
                            BrowseTab::class.isInstance(tab) -> {
                                val count by produceState(initialValue = 0) {
                                    Injekt.get<SourcePreferences>().extensionUpdatesCount.changes()
                                        .collectLatest { value = it }
                                }
                                if (count > 0) {
                                    Badge {
                                        Text(text = count.toString())
                                    }
                                }
                            }
                        }
                    },
                ) {
                    Icon(
                        painter = tab.options.icon!!,
                        contentDescription = tab.options.title,
                        tint = if (isSelected) tachiyomi.presentation.core.theme.primaryTextColor() else inactiveColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = tab.options.title,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) tachiyomi.presentation.core.theme.primaryTextColor() else inactiveColor,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    @Composable
    fun NavigationRailItem(tab: eu.kanade.presentation.util.Tab, alwaysShowLabel: Boolean) {
        val tabNavigator = LocalTabNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val selected = tabNavigator.current::class == tab::class
        NavigationRailItem(
            selected = selected,
            onClick = {
                if (!selected) {
                    tabNavigator.current = tab
                } else {
                    scope.launch { tab.onReselect(navigator) }
                }
            },
            icon = {
                Icon(
                    painter = tab.options.icon!!,
                    contentDescription = tab.options.title,
                )
            },
            label = {
                Text(
                    text = tab.options.title,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            alwaysShowLabel = alwaysShowLabel,
        )
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

    sealed interface Tab {
        data class Library(val mangaIdToOpen: Long? = null) : Tab
        data object Updates : Tab
        data object History : Tab
        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean) : Tab
    }
}
