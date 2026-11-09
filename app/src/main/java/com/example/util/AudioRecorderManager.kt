package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed class AudioRecordState {
    object Idle : AudioRecordState()
    data class Recording(val durationMs: Long) : AudioRecordState()
    data class Recorded(val filePath: String, val durationMs: Long) : AudioRecordState()
    data class Error(val message: String) : AudioRecordState()
}

sealed class AudioPlaybackState {
    object Idle : AudioPlaybackState()
    data class Playing(val currentPositionMs: Long, val totalDurationMs: Long) : AudioPlaybackState()
    data class Paused(val currentPositionMs: Long, val totalDurationMs: Long) : AudioPlaybackState()
}

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime = 0L
    private var timerJob: Job? = null

    private var player: MediaPlayer? = null
    private var playbackJob: Job? = null

    private val _recordState = MutableStateFlow<AudioRecordState>(AudioRecordState.Idle)
    val recordState: StateFlow<AudioRecordState> = _recordState.asStateFlow()

    private val _playbackState = MutableStateFlow<AudioPlaybackState>(AudioPlaybackState.Idle)
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun startRecording(coroutineScope: CoroutineScope) {
        try {
            stopPlayback()

            val audioDir = File(context.filesDir, "audio").apply { if (!exists()) mkdirs() }
            val file = File(audioDir, "memo_${UUID.randomUUID()}.m4a")
            currentRecordingFile = file

            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            rec.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = rec
            recordingStartTime = System.currentTimeMillis()

            timerJob?.cancel()
            timerJob = coroutineScope.launch(Dispatchers.Main) {
                while (isActive) {
                    val duration = System.currentTimeMillis() - recordingStartTime
                    _recordState.value = AudioRecordState.Recording(duration)
                    delay(100)
                }
            }
        } catch (e: Exception) {
            _recordState.value = AudioRecordState.Error(e.message ?: "Failed to start recording")
        }
    }

    fun stopRecording(): String? {
        timerJob?.cancel()
        timerJob = null

        var recordedFilePath: String? = null
        try {
            recorder?.apply {
                stop()
                release()
            }
            val duration = System.currentTimeMillis() - recordingStartTime
            currentRecordingFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    recordedFilePath = file.absolutePath
                    _recordState.value = AudioRecordState.Recorded(file.absolutePath, duration)
                } else {
                    _recordState.value = AudioRecordState.Idle
                }
            }
        } catch (e: Exception) {
            _recordState.value = AudioRecordState.Error(e.message ?: "Failed to stop recording")
        } finally {
            recorder = null
        }
        return recordedFilePath
    }

    fun cancelRecording() {
        timerJob?.cancel()
        timerJob = null
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        recorder = null
        currentRecordingFile?.delete()
        currentRecordingFile = null
        _recordState.value = AudioRecordState.Idle
    }

    fun startPlayback(filePath: String, coroutineScope: CoroutineScope) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) return

        try {
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _playbackState.value = AudioPlaybackState.Idle
                    playbackJob?.cancel()
                }
                start()
            }
            player = mp

            playbackJob?.cancel()
            playbackJob = coroutineScope.launch(Dispatchers.Main) {
                while (isActive && player?.isPlaying == true) {
                    val current = player?.currentPosition?.toLong() ?: 0L
                    val total = player?.duration?.toLong() ?: 0L
                    _playbackState.value = AudioPlaybackState.Playing(current, total)
                    delay(100)
                }
            }
        } catch (e: Exception) {
            _playbackState.value = AudioPlaybackState.Idle
        }
    }

    fun pausePlayback() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                val current = it.currentPosition.toLong()
                val total = it.duration.toLong()
                _playbackState.value = AudioPlaybackState.Paused(current, total)
            }
        }
    }

    fun resumePlayback(coroutineScope: CoroutineScope) {
        player?.let {
            if (!it.isPlaying) {
                it.start()
                playbackJob?.cancel()
                playbackJob = coroutineScope.launch(Dispatchers.Main) {
                    while (isActive && player?.isPlaying == true) {
                        val current = player?.currentPosition?.toLong() ?: 0L
                        val total = player?.duration?.toLong() ?: 0L
                        _playbackState.value = AudioPlaybackState.Playing(current, total)
                        delay(100)
                    }
                }
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
        _playbackState.value = AudioPlaybackState.Idle
    }

    fun resetState() {
        _recordState.value = AudioRecordState.Idle
        stopPlayback()
    }
}
