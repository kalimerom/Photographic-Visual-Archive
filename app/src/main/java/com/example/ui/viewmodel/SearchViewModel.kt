package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.InspirationImageEntity
import com.example.data.model.MixEntity
import com.example.data.model.PhotoEntity
import com.example.data.model.SeriesEntity
import com.example.data.repository.ArchiveRepository
import com.example.data.repository.InspirationRepository
import com.example.data.repository.MixRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class SearchResults(
    val query: String = "",
    val photos: List<PhotoEntity> = emptyList(),
    val series: List<SeriesEntity> = emptyList(),
    val inspiration: List<InspirationImageEntity> = emptyList(),
    val mixes: List<MixEntity> = emptyList()
) {
    val isEmpty: Boolean get() = photos.isEmpty() && series.isEmpty() && inspiration.isEmpty() && mixes.isEmpty()
    val totalCount: Int get() = photos.size + series.size + inspiration.size + mixes.size
}

class SearchViewModel(
    private val archiveRepository: ArchiveRepository,
    private val inspirationRepository: InspirationRepository,
    private val mixRepository: MixRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<SearchResults> = _query.flatMapLatest { q ->
        val trimmed = q.trim()
        if (trimmed.length < 2) {
            flowOf(SearchResults(query = trimmed))
        } else {
            combine(
                archiveRepository.searchPhotos(trimmed),
                archiveRepository.searchSeries(trimmed),
                inspirationRepository.searchImages(trimmed),
                mixRepository.searchMixes(trimmed)
            ) { photos, series, insp, mixes ->
                SearchResults(
                    query = trimmed,
                    photos = photos,
                    series = series,
                    inspiration = insp,
                    mixes = mixes
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }

    class Factory(
        private val archiveRepository: ArchiveRepository,
        private val inspirationRepository: InspirationRepository,
        private val mixRepository: MixRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(archiveRepository, inspirationRepository, mixRepository) as T
        }
    }
}
