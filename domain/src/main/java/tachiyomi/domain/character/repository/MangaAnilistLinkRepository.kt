package tachiyomi.domain.character.repository

import tachiyomi.domain.character.model.MangaAnilistLink

interface MangaAnilistLinkRepository {
    suspend fun getByMangaId(mangaId: Long): MangaAnilistLink?
    suspend fun insert(link: MangaAnilistLink)
    suspend fun delete(mangaId: Long)
}
