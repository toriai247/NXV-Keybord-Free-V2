package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
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

class BackgroundMusicManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _activeFilePath = MutableStateFlow<String?>(null)
    val activeFilePath: StateFlow<String?> = _activeFilePath.asStateFlow()

    private val _activeTitle = MutableStateFlow("")
    val activeTitle: StateFlow<String> = _activeTitle.asStateFlow()

    private val _isAudio = MutableStateFlow(true)
    val isAudio: StateFlow<Boolean> = _isAudio.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    fun playMedia(filePath: String, isAudio: Boolean, title: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e("BackgroundMusicManager", "File not found: $filePath")
                return
            }

            // Release previous player
            stop()

            _activeFilePath.value = filePath
            _isAudio.value = isAudio
            _activeTitle.value = title.ifBlank { file.nameWithoutExtension }

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(appContext, Uri.fromFile(file))
                prepare()
                start()
            }

            mp.setOnCompletionListener {
                _isPlaying.value = false
                _currentPositionMs.value = _durationMs.value
            }

            mediaPlayer = mp
            _isPlaying.value = true
            _durationMs.value = mp.duration.coerceAtLeast(1)
            _currentPositionMs.value = 0

            startProgressTracker()
        } catch (e: Exception) {
            Log.e("BackgroundMusicManager", "Error playing media: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicManager", "Error toggling play/pause", e)
        }
    }

    fun pause() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicManager", "Error pausing", e)
        }
    }

    fun resume() {
        val mp = mediaPlayer ?: return
        try {
            if (!mp.isPlaying) {
                mp.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicManager", "Error resuming", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicManager", "Error stopping player", e)
        } finally {
            mediaPlayer = null
            _activeFilePath.value = null
            _activeTitle.value = ""
            _isPlaying.value = false
            _currentPositionMs.value = 0
            _durationMs.value = 0
        }
    }

    fun seekTo(positionMs: Int) {
        val mp = mediaPlayer ?: return
        try {
            val clamped = positionMs.coerceIn(0, _durationMs.value)
            mp.seekTo(clamped)
            _currentPositionMs.value = clamped
        } catch (e: Exception) {
            Log.e("BackgroundMusicManager", "Error seeking", e)
        }
    }

    fun replay10() {
        seekTo((_currentPositionMs.value - 10000).coerceAtLeast(0))
    }

    fun forward10() {
        seekTo((_currentPositionMs.value + 10000).coerceAtMost(_durationMs.value))
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            _currentPositionMs.value = mp.currentPosition
                            _durationMs.value = mp.duration.coerceAtLeast(1)
                        }
                    } catch (_: Exception) {}
                }
                delay(250)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: BackgroundMusicManager? = null

        fun getInstance(context: Context): BackgroundMusicManager {
            return instance ?: synchronized(this) {
                instance ?: BackgroundMusicManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
