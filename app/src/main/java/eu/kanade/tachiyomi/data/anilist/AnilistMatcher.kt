package eu.kanade.tachiyomi.data.anilist

import eu.kanade.tachiyomi.data.track.TrackerManager
import tachiyomi.domain.character.model.MangaAnilistLink
import tachiyomi.domain.character.repository.MangaAnilistLinkRepository
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.track.interactor.GetTracks
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

sealed interface AnilistMatchResult {
    data class Exact(val anilistId: Long) : AnilistMatchResult
    data class Ambiguous(val results: List<AnilistSearchResultItem>) : AnilistMatchResult
    data object NotFound : AnilistMatchResult
}

object AnilistMatcher {

    private fun normalize(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\u0600-\\u06FF\\u3040-\\u30FF\\u4E00-\\u9FFF\\uAC00-\\uD7AF]"), "")
    }

    suspend fun resolveAnilistId(
        manga: Manga,
        linkRepository: MangaAnilistLinkRepository = Injekt.get(),
        getTracks: GetTracks = Injekt.get(),
        trackerManager: TrackerManager = Injekt.get(),
    ): AnilistMatchResult {
        // 1. Check existing link in DB
        val existingLink = linkRepository.getByMangaId(manga.id)
        if (existingLink != null && existingLink.confirmed) {
            return AnilistMatchResult.Exact(existingLink.anilistId)
        }

        // 2. Check if tracked in AniList
        val tracks = getTracks.await(manga.id)
        val aniListTrack = tracks.find { it.trackerId == trackerManager.aniList.id }
        if (aniListTrack != null && aniListTrack.remoteId > 0) {
            val link = MangaAnilistLink(manga.id, aniListTrack.remoteId, confirmed = true)
            linkRepository.insert(link)
            return AnilistMatchResult.Exact(aniListTrack.remoteId)
        }

        // 3. Search AniList by titles
        val searchTitle = manga.ogTitle.ifBlank { manga.title }
        var searchResults = AnilistCharactersApi.searchMedia(searchTitle)

        if (searchResults.isEmpty() && manga.title.isNotBlank() && manga.title != searchTitle) {
            searchResults = AnilistCharactersApi.searchMedia(manga.title)
        }

        if (searchResults.isEmpty()) {
            return AnilistMatchResult.NotFound
        }

        val normTarget1 = normalize(searchTitle)
        val normTarget2 = normalize(manga.title)

        val exactMatches = searchResults.filter { item ->
            val normRomaji = normalize(item.titleRomaji)
            val normEnglish = normalize(item.titleEnglish)
            val normNative = normalize(item.titleNative)

            (normTarget1.isNotBlank() && (normTarget1 == normRomaji || normTarget1 == normEnglish || normTarget1 == normNative)) ||
                (normTarget2.isNotBlank() && (normTarget2 == normRomaji || normTarget2 == normEnglish || normTarget2 == normNative))
        }

        if (exactMatches.size == 1) {
            val matchedId = exactMatches.first().id
            val link = MangaAnilistLink(manga.id, matchedId, confirmed = true)
            linkRepository.insert(link)
            return AnilistMatchResult.Exact(matchedId)
        }

        if (searchResults.size == 1) {
            val candidate = searchResults.first()
            val normRomaji = normalize(candidate.titleRomaji)
            val normEnglish = normalize(candidate.titleEnglish)

            if ((normTarget1.isNotBlank() && (normTarget1.contains(normRomaji) || normRomaji.contains(normTarget1))) ||
                (normTarget1.isNotBlank() && (normTarget1.contains(normEnglish) || normEnglish.contains(normTarget1)))
            ) {
                val matchedId = candidate.id
                val link = MangaAnilistLink(manga.id, matchedId, confirmed = true)
                linkRepository.insert(link)
                return AnilistMatchResult.Exact(matchedId)
            }
        }

        return AnilistMatchResult.Ambiguous(searchResults)
    }

    suspend fun manuallyLink(
        mangaId: Long,
        anilistId: Long,
        linkRepository: MangaAnilistLinkRepository = Injekt.get(),
    ) {
        val link = MangaAnilistLink(mangaId, anilistId, confirmed = true)
        linkRepository.insert(link)
    }
}
