package exh.recs.sources

import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.data.track.mangabaka.MangaBakaApi
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import tachiyomi.data.source.NoResultsException
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.sy.SYMR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MangaBakaSimilarPagingSource(
    manga: Manga,
) : RecommendationPagingSource(manga) {

    override val name: String
        get() = "MangaBaka"

    override val category: StringResource
        get() = SYMR.strings.similar_titles

    private val api: MangaBakaApi by lazy {
        Injekt.get<TrackerManager>().mangaBaka.api
    }

    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        val mbId = api.resolveMangaBakaId(manga) ?: throw NoResultsException()
        val recs = api.getSimilarSeries(mbId)
        if (recs.isEmpty()) throw NoResultsException()
        return MangasPage(recs, false)
    }
}
