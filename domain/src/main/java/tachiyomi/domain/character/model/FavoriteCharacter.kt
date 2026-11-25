package tachiyomi.domain.character.model

data class FavoriteCharacter(
    val id: Long,
    val name: String,
    val nameNative: String?,
    val imageUrl: String?,
    val favouritesCount: Long,
    val description: String?,
    val role: String?,
    val anilistMediaId: Long?,
    val mangaTitle: String?,
    val addedAt: Long,
)
