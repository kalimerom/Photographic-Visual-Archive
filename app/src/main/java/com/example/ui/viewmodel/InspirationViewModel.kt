package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.InspirationImageEntity
import com.example.data.model.InspirationPhotographerEntity
import com.example.data.model.InspirationProjectEntity
import com.example.data.repository.InspirationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class InspirationTab {
    IMAGES,
    PHOTOGRAPHERS,
    PROJECTS
}

class InspirationViewModel(
    private val repository: InspirationRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(InspirationTab.IMAGES)
    val selectedTab: StateFlow<InspirationTab> = _selectedTab.asStateFlow()

    private val _photographerFilter = MutableStateFlow<String?>(null)
    val photographerFilter: StateFlow<String?> = _photographerFilter.asStateFlow()

    val allImages = repository.allImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredImages: StateFlow<List<InspirationImageEntity>> =
        combine(allImages, _photographerFilter) { images, filter ->
            if (filter.isNullOrBlank()) images
            else images.filter { it.photographerName?.equals(filter, ignoreCase = true) == true }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhotographers: StateFlow<List<InspirationPhotographerEntity>> = repository.allPhotographers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<InspirationProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: InspirationTab) {
        _selectedTab.value = tab
    }

    fun setPhotographerFilter(name: String?) {
        _photographerFilter.value = name
        _selectedTab.value = InspirationTab.IMAGES
    }

    fun addInspiration(
        uri: Uri?,
        photographerName: String?,
        projectName: String?,
        sourceUrl: String?,
        note: String?,
        tags: List<String>,
        onDone: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.addInspirationImage(
                uri = uri,
                photographerName = photographerName,
                projectName = projectName,
                sourceUrl = sourceUrl,
                note = note,
                tags = tags
            )
            onDone(id)
        }
    }

    fun addPhotographer(name: String, notes: String?) {
        viewModelScope.launch {
            repository.addPhotographer(name, notes)
        }
    }

    fun addProject(title: String, photographerId: Long?, notes: String?) {
        viewModelScope.launch {
            repository.addProject(title, photographerId, notes)
        }
    }

    fun deleteInspiration(id: Long) {
        viewModelScope.launch {
            repository.deleteInspirationImage(id)
        }
    }

    class Factory(private val repository: InspirationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InspirationViewModel(repository) as T
        }
    }
}
