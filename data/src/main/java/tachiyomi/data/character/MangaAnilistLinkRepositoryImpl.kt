package tachiyomi.data.character

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import tachiyomi.data.Database
import tachiyomi.domain.character.model.MangaAnilistLink
import tachiyomi.domain.character.repository.MangaAnilistLinkRepository

class MangaAnilistLinkRepositoryImpl(
    private val database: Database,
) : MangaAnilistLinkRepository {

    private fun mapLink(
        mangaId: Long,
        anilistId: Long,
        confirmed: Long,
    ): MangaAnilistLink {
        return MangaAnilistLink(
            mangaId = mangaId,
            anilistId = anilistId,
            confirmed = confirmed == 1L,
        )
    }

    override suspend fun getByMangaId(mangaId: Long): MangaAnilistLink? {
        return database.manga_anilist_linkQueries
            .selectByMangaId(mangaId, ::mapLink)
            .awaitAsOneOrNull()
    }

    override suspend fun insert(link: MangaAnilistLink) {
        database.manga_anilist_linkQueries.insert(
            manga_id = link.mangaId,
            anilist_id = link.anilistId,
            confirmed = if (link.confirmed) 1L else 0L,
        )
    }

    override suspend fun delete(mangaId: Long) {
        database.manga_anilist_linkQueries.delete(mangaId)
    }
}
