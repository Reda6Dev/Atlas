package eu.kanade.tachiyomi.ui.manga.character

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.data.anilist.AnilistCharacter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tachiyomi.domain.character.model.FavoriteCharacter
import tachiyomi.domain.character.repository.FavoriteCharactersRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class CharacterDetailsScreenModel(
    val character: AnilistCharacter,
    val mangaTitle: String?,
    private val favoriteCharactersRepository: FavoriteCharactersRepository = Injekt.get(),
) : ScreenModel {

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        checkFavorite()
    }

    private fun checkFavorite() {
        screenModelScope.launch {
            val fav = favoriteCharactersRepository.getById(character.id)
            _isFavorite.value = (fav != null)
        }
    }

    fun toggleFavorite() {
        screenModelScope.launch {
            if (_isFavorite.value) {
                favoriteCharactersRepository.delete(character.id)
                _isFavorite.value = false
            } else {
                val fav = FavoriteCharacter(
                    id = character.id,
                    name = character.nameFull,
                    nameNative = character.nameNative,
                    imageUrl = character.imageUrl,
                    favouritesCount = character.favouritesCount,
                    description = character.description,
                    role = character.role,
                    anilistMediaId = null,
                    mangaTitle = mangaTitle,
                    addedAt = System.currentTimeMillis(),
                )
                favoriteCharactersRepository.insert(fav)
                _isFavorite.value = true
            }
        }
    }
}
