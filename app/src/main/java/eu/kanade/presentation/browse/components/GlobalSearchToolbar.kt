package eu.kanade.presentation.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.UpIcon
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SourceFilter
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.runOnEnterKeyPressed

@Composable
fun GlobalSearchToolbar(
    searchQuery: String?,
    progress: Int,
    total: Int,
    navigateUp: () -> Unit,
    onChangeSearchQuery: (String?) -> Unit,
    onSearch: (String) -> Unit,
    hideSourceFilter: Boolean,
    sourceFilter: SourceFilter,
    onChangeSearchFilter: (SourceFilter) -> Unit,
    onlyShowHasResults: Boolean,
    onToggleResults: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(
                    onClick = navigateUp,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                ) {
                    UpIcon()
                }

                var isFocused by remember { mutableStateOf(false) }
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusManager = LocalFocusManager.current

                val searchAndClearFocus: () -> Unit = {
                    if (!searchQuery.isNullOrBlank()) {
                        onSearch(searchQuery)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            1.dp,
                            if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape,
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = searchQuery.orEmpty(),
                        onValueChange = onChangeSearchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .runOnEnterKeyPressed(action = searchAndClearFocus),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { searchAndClearFocus() }),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isNullOrEmpty()) {
                                        Text(
                                            text = stringResource(MR.strings.action_search_hint),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 16.sp,
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    innerTextField()
                                }
                                if (!searchQuery.isNullOrEmpty()) {
                                    IconButton(
                                        onClick = { onChangeSearchQuery("") },
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = stringResource(MR.strings.action_reset),
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            }

            if (progress in 1..<total) {
                LinearProgressIndicator(
                    progress = { progress / total.toFloat() },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(),
                )
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val outlineVariant = MaterialTheme.colorScheme.outlineVariant
            val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
            val accentTextColor = tachiyomi.presentation.core.theme.primaryTextColor()

            if (!hideSourceFilter) {
                val pinnedSelected = sourceFilter == SourceFilter.PinnedOnly
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (pinnedSelected) primaryColor.copy(alpha = 0.16f) else Color.Transparent,
                            CircleShape,
                        )
                        .border(
                            1.dp,
                            if (pinnedSelected) primaryColor else outlineVariant,
                            CircleShape,
                        )
                        .clickable { onChangeSearchFilter(SourceFilter.PinnedOnly) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (pinnedSelected) accentTextColor else onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(MR.strings.pinned_sources),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (pinnedSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                            ),
                            color = if (pinnedSelected) accentTextColor else onSurfaceVariant,
                        )
                    }
                }

                val allSelected = sourceFilter == SourceFilter.All
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (allSelected) primaryColor.copy(alpha = 0.16f) else Color.Transparent,
                            CircleShape,
                        )
                        .border(
                            1.dp,
                            if (allSelected) primaryColor else outlineVariant,
                            CircleShape,
                        )
                        .clickable { onChangeSearchFilter(SourceFilter.All) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (allSelected) accentTextColor else onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(MR.strings.all),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                            ),
                            color = if (allSelected) accentTextColor else onSurfaceVariant,
                        )
                    }
                }
            }

            val resultsSelected = onlyShowHasResults
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (resultsSelected) primaryColor.copy(alpha = 0.16f) else Color.Transparent,
                        CircleShape,
                    )
                    .border(
                        1.dp,
                        if (resultsSelected) primaryColor else outlineVariant,
                        CircleShape,
                    )
                    .clickable { onToggleResults() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (resultsSelected) accentTextColor else onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(MR.strings.has_results),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (resultsSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                        ),
                        color = if (resultsSelected) accentTextColor else onSurfaceVariant,
                    )
                }
            }
        }

        HorizontalDivider()
    }
}
