package eu.kanade.tachiyomi.ui.manga.character

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tachiyomi.domain.character.model.FavoriteCharacter
import tachiyomi.domain.character.repository.FavoriteCharactersRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class FavoriteCharactersScreenModel(
    private val favoriteCharactersRepository: FavoriteCharactersRepository = Injekt.get(),
) : ScreenModel {

    val characters: StateFlow<List<FavoriteCharacter>> = favoriteCharactersRepository
        .subscribeAll()
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun deleteFavorite(id: Long) {
        screenModelScope.launch {
            favoriteCharactersRepository.delete(id)
        }
    }
}
