package eu.kanade.tachiyomi.source.online.all

import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import exh.source.MANGADEX_SOURCE_ID
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Suppress("OverridingDeprecatedMember", "DEPRECATION")
class StubMangaDexDelegate(
    override val id: Long = MANGADEX_SOURCE_ID,
    override val lang: String = "en",
    override val name: String = "MangaDex",
    override val baseUrl: String = "https://mangadex.org",
) : HttpSource() {
    override val supportsLatest: Boolean = true

    override val client: OkHttpClient by lazy { Injekt.get<NetworkHelper>().client }
    override val headers: Headers by lazy {
        Headers.Builder()
            .add("User-Agent", "Atlas v${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            .build()
    }

    override fun popularMangaRequest(page: Int): Request = throw UnsupportedOperationException("Unsupported operation")
    override fun popularMangaParse(response: Response): MangasPage = throw UnsupportedOperationException("Unsupported operation")
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = throw UnsupportedOperationException("Unsupported operation")
    override fun searchMangaParse(response: Response): MangasPage = throw UnsupportedOperationException("Unsupported operation")
    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException("Unsupported operation")
    override fun latestUpdatesParse(response: Response): MangasPage = throw UnsupportedOperationException("Unsupported operation")
    override fun mangaDetailsParse(response: Response): SManga = throw UnsupportedOperationException("Unsupported operation")
    override fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException("Unsupported operation")
    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException("Unsupported operation")
    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Unsupported operation")
}
