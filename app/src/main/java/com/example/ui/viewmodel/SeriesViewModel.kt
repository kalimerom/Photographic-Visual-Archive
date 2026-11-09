package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PhotoEntity
import com.example.data.model.SeriesEntity
import com.example.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SeriesViewModel(
    private val repository: ArchiveRepository
) : ViewModel() {

    val allSeries: StateFlow<List<SeriesEntity>> = repository.allSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContactSheets = repository.allContactSheets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhotos = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSeriesById(seriesId: Long): StateFlow<SeriesEntity?> =
        repository.getSeriesById(seriesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getPhotosForSeries(seriesId: Long): StateFlow<List<PhotoEntity>> =
        repository.getPhotosForSeries(seriesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSeries(title: String, description: String?, photoIds: List<Long>, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSeries(title, description, photoIds)
            onCreated(id)
        }
    }

    fun removePhotoFromSeries(seriesId: Long, photoId: Long) {
        viewModelScope.launch {
            repository.removePhotoFromSeries(seriesId, photoId)
        }
    }

    fun deleteSeries(seriesId: Long) {
        viewModelScope.launch {
            repository.deleteSeries(seriesId)
        }
    }

    fun deleteContactSheet(sheetId: Long) {
        viewModelScope.launch {
            repository.deleteContactSheet(sheetId)
        }
    }

    class Factory(private val repository: ArchiveRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SeriesViewModel(repository) as T
        }
    }
}
