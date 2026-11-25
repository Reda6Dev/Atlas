package tachiyomi.domain.character.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.character.model.FavoriteCharacter

interface FavoriteCharactersRepository {
    fun subscribeAll(): Flow<List<FavoriteCharacter>>
    suspend fun getAll(): List<FavoriteCharacter>
    suspend fun getById(id: Long): FavoriteCharacter?
    suspend fun insert(character: FavoriteCharacter)
    suspend fun delete(id: Long)
}
