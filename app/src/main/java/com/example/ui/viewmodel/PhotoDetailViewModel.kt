package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PhotoEntity
import com.example.data.model.TagEntity
import com.example.data.repository.ArchiveRepository
import com.example.util.AudioPlaybackState
import com.example.util.AudioRecordState
import com.example.util.AudioRecorderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhotoDetailViewModel(
    private val photoId: Long,
    private val repository: ArchiveRepository,
    private val audioRecorderManager: AudioRecorderManager
) : ViewModel() {

    val photo: StateFlow<PhotoEntity?> = repository.getPhotoById(photoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tags: StateFlow<List<TagEntity>> = repository.getTagsForPhoto(photoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioRecordState: StateFlow<AudioRecordState> = audioRecorderManager.recordState
    val audioPlaybackState: StateFlow<AudioPlaybackState> = audioRecorderManager.playbackState

    private val _isRecordingVoiceMemo = MutableStateFlow(false)
    val isRecordingVoiceMemo: StateFlow<Boolean> = _isRecordingVoiceMemo.asStateFlow()

    fun setSelectionStatus(status: String) {
        viewModelScope.launch {
            repository.setPhotoSelectionStatus(photoId, status)
        }
    }

    fun attachEditedVersion(uri: Uri) {
        viewModelScope.launch {
            repository.attachEditedVersion(photoId, uri)
        }
    }

    fun removeEditedVersion() {
        viewModelScope.launch {
            repository.removeEditedVersion(photoId)
        }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            repository.addTagToPhoto(photoId, name)
        }
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch {
            repository.removeTagFromPhoto(photoId, tagId)
        }
    }

    // Voice Memo recording
    fun startVoiceMemoRecording() {
        _isRecordingVoiceMemo.value = true
        audioRecorderManager.startRecording(viewModelScope)
    }

    fun stopVoiceMemoRecording() {
        _isRecordingVoiceMemo.value = false
        val path = audioRecorderManager.stopRecording()
        if (path != null) {
            val state = audioRecorderManager.recordState.value
            val duration = if (state is AudioRecordState.Recorded) state.durationMs else 0L
            viewModelScope.launch {
                repository.attachVoiceMemo(photoId, path, duration)
            }
        }
    }

    fun cancelVoiceMemoRecording() {
        _isRecordingVoiceMemo.value = false
        audioRecorderManager.cancelRecording()
    }

    fun playVoiceMemo() {
        val path = photo.value?.voiceMemoPath ?: return
        audioRecorderManager.startPlayback(path, viewModelScope)
    }

    fun pauseVoiceMemo() {
        audioRecorderManager.pausePlayback()
    }

    fun resumeVoiceMemo() {
        audioRecorderManager.resumePlayback(viewModelScope)
    }

    fun stopVoiceMemo() {
        audioRecorderManager.stopPlayback()
    }

    fun deleteVoiceMemo() {
        audioRecorderManager.stopPlayback()
        viewModelScope.launch {
            repository.deleteVoiceMemo(photoId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorderManager.resetState()
    }

    class Factory(
        private val photoId: Long,
        private val repository: ArchiveRepository,
        private val audioRecorderManager: AudioRecorderManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhotoDetailViewModel(photoId, repository, audioRecorderManager) as T
        }
    }
}
