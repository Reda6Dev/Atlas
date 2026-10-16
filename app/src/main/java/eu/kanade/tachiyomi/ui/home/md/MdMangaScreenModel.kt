package eu.kanade.tachiyomi.ui.home.md

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import exh.md.dto.ChapterDataDto
import exh.md.dto.MangaDataDto
import exh.md.service.MangaDexService
import exh.md.utils.MdConstants
import exh.md.utils.MdUtil
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Headers
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MdMangaScreenModel(
    val mangaId: String,
    private val networkHelper: NetworkHelper = Injekt.get()
) : StateScreenModel<MdMangaScreenModel.State>(State()) {

    data class State(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val manga: MangaDataDto? = null,
        val chapters: List<ChapterDataDto> = emptyList(),
        val error: String? = null,
        val chaptersLoading: Boolean = true,
        val page: Int = 0,
        val hasMore: Boolean = true
    )

    private val mangadexService by lazy {
        val headers = Headers.Builder()
            .add("User-Agent", "Atlas v${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            .build()
        MangaDexService(networkHelper.client, headers)
    }

    init {
        loadMangaDetails()
        loadChapters()
    }

    fun refresh() {
        mutableState.update { it.copy(isRefreshing = true, error = null, page = 0, hasMore = true, chapters = emptyList()) }
        loadMangaDetails()
        loadChapters()
    }

    fun loadMoreChapters() {
        if (state.value.chaptersLoading || !state.value.hasMore) return
        loadChapters(state.value.page + 1)
    }

    private fun loadMangaDetails() {
        screenModelScope.launchIO {
            runCatching {
                mangadexService.viewManga(mangaId)
            }.onSuccess { response ->
                mutableState.update { it.copy(isLoading = false, isRefreshing = false, manga = response.data) }
            }.onFailure { e ->
                mutableState.update { it.copy(isLoading = false, isRefreshing = false, error = e.localizedMessage) }
            }
        }
    }

    private fun loadChapters(page: Int = 0) {
        mutableState.update { it.copy(chaptersLoading = true) }
        screenModelScope.launchIO {
            runCatching {
                mangadexService.viewChapters(
                    mangaId = mangaId,
                    limit = 20,
                    offset = page * 20,
                    languages = listOf("en", "ar")
                )
            }.onSuccess { response ->
                val newChapters = response.data
                val allChapters = if (page == 0) newChapters else state.value.chapters + newChapters
                val total = response.total ?: 0
                val hasMore = (page * 20 + newChapters.size) < total

                mutableState.update {
                    it.copy(
                        chaptersLoading = false,
                        chapters = allChapters,
                        page = page,
                        hasMore = hasMore
                    )
                }
            }.onFailure { e ->
                mutableState.update { it.copy(chaptersLoading = false, error = e.localizedMessage) }
            }
        }
    }
}
