package eu.kanade.tachiyomi.ui.home.md

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import exh.md.service.MangaDexService
import exh.md.utils.MdApi
import kotlinx.coroutines.flow.update
import okhttp3.Headers
import tachiyomi.core.common.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MdReaderScreenModel(
    val chapterId: String,
    private val networkHelper: NetworkHelper = Injekt.get()
) : StateScreenModel<MdReaderScreenModel.State>(State()) {

    data class State(
        val isLoading: Boolean = true,
        val chapterTitle: String = "جارٍ التحميل...",
        val pageUrls: List<String> = emptyList(),
        val error: String? = null
    )

    private val mangadexService by lazy {
        val headers = Headers.Builder()
            .add("User-Agent", "Atlas v${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            .build()
        MangaDexService(networkHelper.client, headers)
    }

    init {
        loadChapterPages()
    }

    fun loadChapterPages() {
        mutableState.update { it.copy(isLoading = true, error = null) }
        screenModelScope.launchIO {
            runCatching {
                // 1. Get Chapter Details
                val chapterDto = mangadexService.viewChapter(chapterId)
                val chNum = chapterDto.data.attributes.chapter
                val chTitle = chapterDto.data.attributes.title
                val displayTitle = when {
                    !chNum.isNullOrBlank() && !chTitle.isNullOrBlank() -> "الفصل $chNum - $chTitle"
                    !chNum.isNullOrBlank() -> "الفصل $chNum"
                    !chTitle.isNullOrBlank() -> chTitle
                    else -> "الفصل"
                }

                // 2. Get At-Home Image Server
                val atHomeDto = mangadexService.getAtHomeServer("${MdApi.atHomeServer}/$chapterId")
                val baseUrl = atHomeDto.baseUrl
                val hash = atHomeDto.chapter.hash
                val fileNames = atHomeDto.chapter.data.ifEmpty { atHomeDto.chapter.dataSaver }

                val pages = fileNames.map { fileName ->
                    "$baseUrl/data/$hash/$fileName"
                }

                displayTitle to pages
            }.onSuccess { (title, pages) ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        chapterTitle = title,
                        pageUrls = pages
                    )
                }
            }.onFailure { e ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "حدث خطأ أثناء تحميل الصفحات"
                    )
                }
            }
        }
    }
}
