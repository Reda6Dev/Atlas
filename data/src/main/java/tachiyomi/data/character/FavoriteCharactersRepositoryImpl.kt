package tachiyomi.data.character

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.flow.Flow
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList
import tachiyomi.domain.character.model.FavoriteCharacter
import tachiyomi.domain.character.repository.FavoriteCharactersRepository

class FavoriteCharactersRepositoryImpl(
    private val database: Database,
) : FavoriteCharactersRepository {

    private fun mapFavoriteCharacter(
        id: Long,
        name: String,
        nameNative: String?,
        imageUrl: String?,
        favouritesCount: Long,
        description: String?,
        role: String?,
        anilistMediaId: Long?,
        mangaTitle: String?,
        addedAt: Long,
    ): FavoriteCharacter {
        return FavoriteCharacter(
            id = id,
            name = name,
            nameNative = nameNative,
            imageUrl = imageUrl,
            favouritesCount = favouritesCount,
            description = description,
            role = role,
            anilistMediaId = anilistMediaId,
            mangaTitle = mangaTitle,
            addedAt = addedAt,
        )
    }

    override fun subscribeAll(): Flow<List<FavoriteCharacter>> {
        return database.favorite_charactersQueries
            .selectAll(::mapFavoriteCharacter)
            .subscribeToList()
    }

    override suspend fun getAll(): List<FavoriteCharacter> {
        return database.favorite_charactersQueries
            .selectAll(::mapFavoriteCharacter)
            .awaitAsList()
    }

    override suspend fun getById(id: Long): FavoriteCharacter? {
        return database.favorite_charactersQueries
            .selectById(id, ::mapFavoriteCharacter)
            .awaitAsOneOrNull()
    }

    override suspend fun insert(character: FavoriteCharacter) {
        database.favorite_charactersQueries.insert(
            id = character.id,
            name = character.name,
            name_native = character.nameNative,
            image_url = character.imageUrl,
            favourites_count = character.favouritesCount,
            description = character.description,
            role = character.role,
            anilist_media_id = character.anilistMediaId,
            manga_title = character.mangaTitle,
            added_at = character.addedAt,
        )
    }

    override suspend fun delete(id: Long) {
        database.favorite_charactersQueries.delete(id)
    }
}
