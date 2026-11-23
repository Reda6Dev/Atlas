package eu.kanade.tachiyomi.ui.browse.source.globalsearch

import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.tachiyomi.source.Source
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GlobalSearchScreenModel(
    initialQuery: String = "",
    initialExtensionFilter: String? = null,
) : SearchScreenModel(State(searchQuery = initialQuery)) {

    private var queryCandidates = generateSearchCandidates(initialQuery)
    private var currentCandidateIndex = 0

    init {
        extensionFilter = initialExtensionFilter
        setSourceFilter(SourceFilter.All)
        if (initialQuery.isNotBlank() || !initialExtensionFilter.isNullOrBlank()) {
            searchWithCandidates()
        }
    }

    override fun search() {
        val query = state.value.searchQuery ?: return
        queryCandidates = generateSearchCandidates(query)
        currentCandidateIndex = 0
        searchWithCandidates()
    }

    override fun getEnabledSources(): List<Source> {
        return super.getEnabledSources()
            .filter { state.value.sourceFilter != SourceFilter.PinnedOnly || "${it.id}" in pinnedSources }
    }

    fun searchWithCandidates() {
        searchJob?.cancel()
        searchJob = screenModelScope.launch {
            val query = state.value.searchQuery
            if (query.isNullOrBlank()) return@launch

            searchInternal(query)

            val totalResults = state.value.items.values.sumOf {
                (it as? SearchItemResult.Success)?.result?.size ?: 0
            }

            if (totalResults == 0 && currentCandidateIndex + 1 < queryCandidates.size) {
                currentCandidateIndex++
                val nextQuery = queryCandidates[currentCandidateIndex]
                mutableState.update { it.copy(searchQuery = nextQuery) }
                searchWithCandidates()
            }
        }
    }

    companion object {
        fun generateSearchCandidates(query: String): List<String> {
            val candidates = mutableListOf<String>()
            val trimmed = query.trim()
            if (trimmed.isBlank()) return emptyList()

            candidates.add(trimmed)

            // 1. Remove parenthetical or bracketed content e.g. "Title (Official)" -> "Title"
            val cleanTitle = trimmed
                .replace(Regex("\\(.*\\)|\\[.*\\]"), "")
                .replace(Regex("[-:_].*"), "")
                .trim()
            if (cleanTitle.isNotBlank() && !candidates.contains(cleanTitle)) {
                candidates.add(cleanTitle)
            }

            // 2. Take first 3 words then first 2 words if title has 4+ words
            val words = (if (cleanTitle.isNotBlank()) cleanTitle else trimmed)
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
            if (words.size >= 4) {
                val firstThree = words.take(3).joinToString(" ")
                if (firstThree.isNotBlank() && !candidates.contains(firstThree)) {
                    candidates.add(firstThree)
                }
                val firstTwo = words.take(2).joinToString(" ")
                if (firstTwo.isNotBlank() && !candidates.contains(firstTwo)) {
                    candidates.add(firstTwo)
                }
            }

            return candidates
        }
    }
}
