package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.MixEntity
import com.example.data.repository.ArchiveRepository
import com.example.data.repository.InspirationRepository
import com.example.data.repository.MixRepository
import com.example.data.repository.ResolvedMix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MixViewModel(
    private val mixRepository: MixRepository,
    private val archiveRepository: ArchiveRepository,
    private val inspirationRepository: InspirationRepository
) : ViewModel() {

    val allMixes: StateFlow<List<MixEntity>> = mixRepository.allMixes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivePhotos = archiveRepository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inspirationImages = inspirationRepository.allImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentResolvedMix = MutableStateFlow<ResolvedMix?>(null)
    val currentResolvedMix: StateFlow<ResolvedMix?> = _currentResolvedMix.asStateFlow()

    fun loadMixDetail(mixId: Long) {
        viewModelScope.launch {
            mixRepository.getMixById(mixId).collect { mix ->
                if (mix != null) {
                    _currentResolvedMix.value = mixRepository.resolveMix(mix)
                } else {
                    _currentResolvedMix.value = null
                }
            }
        }
    }

    suspend fun resolveMixPreview(mix: MixEntity): ResolvedMix {
        return mixRepository.resolveMix(mix)
    }

    fun createMix(
        title: String,
        note: String?,
        imageAId: Long,
        imageAType: String,
        imageBId: Long,
        imageBType: String,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = mixRepository.createMix(
                title = title,
                note = note,
                imageAId = imageAId,
                imageAType = imageAType,
                imageBId = imageBId,
                imageBType = imageBType
            )
            onCreated(id)
        }
    }

    fun updateMixNote(mix: MixEntity, newNote: String) {
        viewModelScope.launch {
            mixRepository.updateMix(mix.copy(note = newNote))
        }
    }

    fun deleteMix(id: Long) {
        viewModelScope.launch {
            mixRepository.deleteMix(id)
        }
    }

    class Factory(
        private val mixRepository: MixRepository,
        private val archiveRepository: ArchiveRepository,
        private val inspirationRepository: InspirationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MixViewModel(mixRepository, archiveRepository, inspirationRepository) as T
        }
    }
}
