package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PhotoEntity
import com.example.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DateGroup(
    val title: String,
    val photos: List<PhotoEntity>
)

data class ImportProgress(
    val isImporting: Boolean = false,
    val current: Int = 0,
    val total: Int = 0
)

class ArchiveViewModel(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _selectedPhotoIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotoIds: StateFlow<Set<Long>> = _selectedPhotoIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _dateFilter = MutableStateFlow("ALL") // "ALL", "THIS_MONTH", "THIS_YEAR" or specific year
    val dateFilter: StateFlow<String> = _dateFilter.asStateFlow()

    private val _importProgress = MutableStateFlow(ImportProgress())
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()

    val allPhotos = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedPhotos: StateFlow<List<DateGroup>> = combine(allPhotos, _dateFilter) { photos, filter ->
        val filtered = when (filter) {
            "THIS_MONTH" -> {
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
                photos.filter { SimpleDateFormat("yyyy-MM", Locale.US).format(Date(it.captureDate)) == currentMonth }
            }
            "THIS_YEAR" -> {
                val currentYear = SimpleDateFormat("yyyy", Locale.US).format(Date())
                photos.filter { SimpleDateFormat("yyyy", Locale.US).format(Date(it.captureDate)) == currentYear }
            }
            else -> photos
        }

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        filtered.groupBy { dateFormat.format(Date(it.captureDate)) }
            .map { (dateStr, groupPhotos) -> DateGroup(title = dateStr, photos = groupPhotos) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableYears: StateFlow<List<String>> = allPhotos.combine(_dateFilter) { photos, _ ->
        val sdf = SimpleDateFormat("yyyy", Locale.US)
        photos.map { sdf.format(Date(it.captureDate)) }.distinct().sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDateFilter(filter: String) {
        _dateFilter.value = filter
    }

    fun toggleSelection(photoId: Long) {
        val current = _selectedPhotoIds.value.toMutableSet()
        if (current.contains(photoId)) {
            current.remove(photoId)
        } else {
            current.add(photoId)
        }
        _selectedPhotoIds.value = current
        if (current.isEmpty()) {
            _isSelectionMode.value = false
        } else {
            _isSelectionMode.value = true
        }
    }

    fun startSelection(photoId: Long) {
        _isSelectionMode.value = true
        _selectedPhotoIds.value = setOf(photoId)
    }

    fun selectAll() {
        val allIds = allPhotos.value.map { it.id }.toSet()
        _selectedPhotoIds.value = allIds
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedPhotoIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun importPhotos(uris: List<Pair<Uri, String?>>) {
        viewModelScope.launch {
            _importProgress.value = ImportProgress(isImporting = true, current = 0, total = uris.size)
            repository.importPhotos(uris) { current, total ->
                _importProgress.value = ImportProgress(isImporting = current < total, current = current, total = total)
            }
            _importProgress.value = ImportProgress(isImporting = false)
        }
    }

    fun createSeriesFromSelection(title: String, description: String?, onCreated: (Long) -> Unit) {
        val ids = _selectedPhotoIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val seriesId = repository.createSeries(title, description, ids)
            clearSelection()
            onCreated(seriesId)
        }
    }

    fun createContactSheetFromSelection(title: String, onCreated: (Long) -> Unit) {
        val ids = _selectedPhotoIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val sheetId = repository.createContactSheet(title, ids)
            clearSelection()
            onCreated(sheetId)
        }
    }

    fun deleteSelectedPhotos() {
        val ids = _selectedPhotoIds.value.toList()
        viewModelScope.launch {
            ids.forEach { id -> repository.deletePhoto(id) }
            clearSelection()
        }
    }

    class Factory(private val repository: ArchiveRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ArchiveViewModel(repository) as T
        }
    }
}
