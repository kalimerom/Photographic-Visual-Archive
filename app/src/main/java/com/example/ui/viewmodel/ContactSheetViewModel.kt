package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactSheetEntity
import com.example.data.model.ContactSheetPhotoEntity
import com.example.data.model.PhotoEntity
import com.example.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactSheetFrame(
    val frameEntity: ContactSheetPhotoEntity,
    val photo: PhotoEntity?
)

enum class ContactSheetMode {
    GRID,
    FILM_STRIP
}

class ContactSheetViewModel(
    private val sheetId: Long,
    private val repository: ArchiveRepository
) : ViewModel() {

    val contactSheet: StateFlow<ContactSheetEntity?> = repository.getContactSheetById(sheetId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val sheetPhotos = repository.getContactSheetPhotos(sheetId)

    val frames: StateFlow<List<ContactSheetFrame>> = combine(sheetPhotos, repository.allPhotos) { sheetItems, allPhotos ->
        val photosMap = allPhotos.associateBy { it.id }
        sheetItems.map { frame ->
            ContactSheetFrame(
                frameEntity = frame,
                photo = photosMap[frame.photoId]
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _viewMode = MutableStateFlow(ContactSheetMode.GRID)
    val viewMode: StateFlow<ContactSheetMode> = _viewMode.asStateFlow()

    private val _selectedFrameId = MutableStateFlow<Long?>(null)
    val selectedFrameId: StateFlow<Long?> = _selectedFrameId.asStateFlow()

    fun setViewMode(mode: ContactSheetMode) {
        _viewMode.value = mode
    }

    fun selectFrame(frameId: Long) {
        _selectedFrameId.value = frameId
    }

    fun cycleFrameStatus(frameId: Long) {
        val frame = frames.value.find { it.frameEntity.id == frameId } ?: return
        val currentStatus = frame.frameEntity.status
        val nextStatus = when (currentStatus) {
            "normal" -> "selected"
            "selected" -> "hero"
            "hero" -> "normal"
            else -> "normal"
        }
        viewModelScope.launch {
            repository.updateContactSheetPhotoStatus(frameId, nextStatus)
        }
    }

    fun setHero(frameId: Long) {
        viewModelScope.launch {
            repository.updateContactSheetPhotoStatus(frameId, "hero")
        }
    }

    class Factory(
        private val sheetId: Long,
        private val repository: ArchiveRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactSheetViewModel(sheetId, repository) as T
        }
    }
}
