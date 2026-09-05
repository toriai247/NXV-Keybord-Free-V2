package com.example.downloader.tiktok

import android.content.Context
import com.example.downloader.social.SocialDownloadManager
import com.example.downloader.social.SocialDownloadState
import com.example.downloader.social.SocialMediaExtractor
import com.example.downloader.social.SocialPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TikTokDownloadState {
    object Idle : TikTokDownloadState()
    data class LinkDetected(val url: String, val platformName: String = "TikTok") : TikTokDownloadState()
    data class ChoosingFormat(val url: String, val platformName: String = "TikTok") : TikTokDownloadState()
    data class ChoosingQuality(val url: String, val isAudio: Boolean, val platformName: String = "TikTok") : TikTokDownloadState()
    data class Resolving(val url: String, val message: String = "Fetching video details...") : TikTokDownloadState()
    data class Downloading(
        val url: String,
        val title: String,
        val quality: String,
        val isAudio: Boolean,
        val progress: Int, // 0..100
        val downloadedBytes: Long,
        val totalBytes: Long,
        val platformName: String = "TikTok"
    ) : TikTokDownloadState()
    data class Success(
        val filePath: String,
        val fileName: String,
        val isAudio: Boolean,
        val quality: String,
        val title: String = "",
        val platformName: String = "TikTok"
    ) : TikTokDownloadState()
    data class Error(val message: String, val url: String? = null, val platformName: String = "TikTok") : TikTokDownloadState()
}

/**
 * Enhanced TikTokDownloadManager providing unified support for TikTok and Facebook video downloads.
 */
class TikTokDownloadManager(private val context: Context) {

    private val socialManager = SocialDownloadManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow<TikTokDownloadState>(TikTokDownloadState.Idle)
    val state: StateFlow<TikTokDownloadState> = _state.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: TikTokDownloadManager? = null

        fun getInstance(context: Context): TikTokDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TikTokDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun extractTikTokUrl(text: String?): String? {
            return SocialMediaExtractor.extractSocialUrl(text)?.first
        }
    }

    init {
        scope.launch {
            socialManager.state.collect { socialState ->
                _state.value = mapToTikTokState(socialState)
            }
        }
    }

    private fun mapToTikTokState(socialState: SocialDownloadState): TikTokDownloadState {
        return when (socialState) {
            is SocialDownloadState.Idle -> TikTokDownloadState.Idle
            is SocialDownloadState.LinkDetected -> TikTokDownloadState.LinkDetected(
                url = socialState.url,
                platformName = socialState.platform.displayName
            )
            is SocialDownloadState.ChoosingFormat -> TikTokDownloadState.ChoosingFormat(
                url = socialState.url,
                platformName = socialState.platform.displayName
            )
            is SocialDownloadState.ChoosingQuality -> TikTokDownloadState.ChoosingQuality(
                url = socialState.url,
                isAudio = socialState.isAudio,
                platformName = socialState.platform.displayName
            )
            is SocialDownloadState.Resolving -> TikTokDownloadState.Resolving(
                url = socialState.url,
                message = socialState.message
            )
            is SocialDownloadState.Downloading -> TikTokDownloadState.Downloading(
                url = socialState.url,
                title = socialState.title,
                quality = socialState.quality,
                isAudio = socialState.isAudio,
                progress = socialState.progress,
                downloadedBytes = socialState.downloadedBytes,
                totalBytes = socialState.totalBytes,
                platformName = socialState.platform.displayName
            )
            is SocialDownloadState.Success -> TikTokDownloadState.Success(
                filePath = socialState.filePath,
                fileName = socialState.fileName,
                isAudio = socialState.isAudio,
                quality = socialState.quality,
                title = socialState.title,
                platformName = socialState.platform.displayName
            )
            is SocialDownloadState.Error -> TikTokDownloadState.Error(
                message = socialState.message,
                url = socialState.url,
                platformName = socialState.platform.displayName
            )
        }
    }

    fun onClipboardUpdated(text: String?): Boolean {
        return socialManager.onClipboardUpdated(text)
    }

    fun onDownloadClicked() {
        socialManager.onDownloadClicked()
    }

    fun onFormatChosen(isAudio: Boolean) {
        socialManager.onFormatChosen(isAudio)
    }

    fun onQualityChosen(quality: String) {
        socialManager.onQualityChosen(quality)
    }

    fun dismiss() {
        socialManager.dismiss()
    }

    fun cancelDownload() {
        socialManager.cancelDownload()
    }

    fun retry() {
        socialManager.retry()
    }

    fun openFile(filePath: String, isAudio: Boolean) {
        socialManager.openFile(filePath, isAudio)
    }

    fun shareFile(filePath: String, isAudio: Boolean) {
        socialManager.shareFile(filePath, isAudio)
    }

    fun playInKeyboard(filePath: String, isAudio: Boolean, title: String = "") {
        socialManager.playInKeyboard(filePath, isAudio, title)
    }
}
