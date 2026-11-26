package eu.kanade.presentation.manga.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eu.kanade.tachiyomi.data.anilist.AnilistCharacter
import eu.kanade.tachiyomi.data.anilist.AnilistCharactersApi
import eu.kanade.tachiyomi.data.anilist.AnilistMatchResult
import eu.kanade.tachiyomi.data.anilist.AnilistMatcher
import eu.kanade.tachiyomi.data.anilist.AnilistMediaDetails
import eu.kanade.tachiyomi.data.anilist.AnilistSearchResultItem
import eu.kanade.tachiyomi.data.anilist.AnilistStaff
import kotlinx.coroutines.launch
import tachiyomi.domain.manga.model.Manga

private sealed interface CharactersTabState {
    data object Loading : CharactersTabState
    data class Success(val details: AnilistMediaDetails) : CharactersTabState
    data class Ambiguous(val results: List<AnilistSearchResultItem>) : CharactersTabState
    data object NotFound : CharactersTabState
    data class Error(val message: String) : CharactersTabState
}

fun getStaffRoleTranslation(role: String): String {
    return when (role) {
        "Story" -> "القصة"
        "Art" -> "الرسم"
        "Original Story" -> "القصة الأصلية"
        "Background Art" -> "رسم الخلفيات"
        "Story & Art" -> "القصة والرسم"
        "Coloring" -> "التلوين"
        "Character Design" -> "تصميم الشخصيات"
        else -> role
    }
}

@Composable
fun MangaCharactersTab(
    manga: Manga,
    onCharacterClick: (AnilistCharacter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<CharactersTabState>(CharactersTabState.Loading) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var showSelectionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(manga.id, refreshTrigger) {
        state = CharactersTabState.Loading
        try {
            when (val matchResult = AnilistMatcher.resolveAnilistId(manga)) {
                is AnilistMatchResult.Exact -> {
                    val details = AnilistCharactersApi.getMediaDetails(matchResult.anilistId)
                    state = CharactersTabState.Success(details)
                }
                is AnilistMatchResult.Ambiguous -> {
                    state = CharactersTabState.Ambiguous(matchResult.results)
                }
                AnilistMatchResult.NotFound -> {
                    val searchResults = AnilistCharactersApi.searchMedia(manga.ogTitle.ifBlank { manga.title })
                    if (searchResults.isNotEmpty()) {
                        state = CharactersTabState.Ambiguous(searchResults)
                    } else {
                        state = CharactersTabState.NotFound
                    }
                }
            }
        } catch (e: Exception) {
            state = CharactersTabState.Error(e.message ?: "حدث خطأ أثناء تحميل البيانات")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val currentState = state) {
            CharactersTabState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is CharactersTabState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { refreshTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة المحاولة")
                    }
                }
            }

            CharactersTabState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "لا توجد شخصيات مسجّلة لهذا العمل",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            is CharactersTabState.Ambiguous -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "لم نتمكن من المطابقة التلقائية بثقة",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يرجى اختيار العمل الصحيح من القائمة لإظهار شخصياته وطاقم العمل",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { showSelectionDialog = true }) {
                        Text("اختر العمل الصحيح")
                    }
                }

                if (showSelectionDialog) {
                    AnilistSelectionDialog(
                        items = currentState.results,
                        onDismiss = { showSelectionDialog = false },
                        onSelect = { selectedItem ->
                            showSelectionDialog = false
                            scope.launch {
                                AnilistMatcher.manuallyLink(manga.id, selectedItem.id)
                                refreshTrigger++
                            }
                        },
                    )
                }
            }

            is CharactersTabState.Success -> {
                val details = currentState.details
                val hasMain = details.mainCharacters.isNotEmpty()
                val hasSupp = details.supportingCharacters.isNotEmpty()
                val hasStaff = details.staff.isNotEmpty()

                if (!hasMain && !hasSupp && !hasStaff) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "لا توجد شخصيات مسجّلة لهذا العمل",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = navBarBottom + 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        if (hasMain) {
                            item {
                                CharacterSection(
                                    title = "الشخصيات الرئيسية",
                                    characters = details.mainCharacters,
                                    onCharacterClick = onCharacterClick,
                                )
                            }
                        }

                        if (hasSupp) {
                            item {
                                CharacterSection(
                                    title = "الشخصيات المساعدة",
                                    characters = details.supportingCharacters,
                                    onCharacterClick = onCharacterClick,
                                )
                            }
                        }

                        if (hasStaff) {
                            item {
                                StaffSection(
                                    title = "طاقم العمل",
                                    staffList = details.staff,
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
private fun CharacterSection(
    title: String,
    characters: List<AnilistCharacter>,
    onCharacterClick: (AnilistCharacter) -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(characters, key = { it.id }) { character ->
                CharacterCard(
                    character = character,
                    onClick = { onCharacterClick(character) },
                )
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: AnilistCharacter,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                if (!character.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = character.imageUrl,
                        contentDescription = character.nameFull,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = character.nameFull,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StaffSection(
    title: String,
    staffList: List<AnilistStaff>,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(staffList, key = { "${it.id}-${it.role}" }) { staff ->
                StaffCard(staff = staff)
            }
        }
    }
}

@Composable
private fun StaffCard(staff: AnilistStaff) {
    val isDefaultImg = staff.imageUrl.isNullOrBlank() || staff.imageUrl.endsWith("default.jpg")

    Card(
        modifier = Modifier.width(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                if (!isDefaultImg) {
                    AsyncImage(
                        model = staff.imageUrl,
                        contentDescription = staff.nameFull,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = staff.nameFull,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = getStaffRoleTranslation(staff.role),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AnilistSelectionDialog(
    items: List<AnilistSearchResultItem>,
    onDismiss: () -> Unit,
    onSelect: (AnilistSearchResultItem) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("اختر العمل الصحيح من AniList") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!item.coverImage.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.coverImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column {
                                Text(
                                    text = item.titleEnglish ?: item.titleRomaji ?: item.titleNative ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val formatYear = listOfNotNull(item.format, item.year?.toString()).joinToString(" • ")
                                if (formatYear.isNotBlank()) {
                                    Text(
                                        text = formatYear,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
    )
}
