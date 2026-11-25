package tachiyomi.domain.character.model

data class MangaAnilistLink(
    val mangaId: Long,
    val anilistId: Long,
    val confirmed: Boolean,
)
