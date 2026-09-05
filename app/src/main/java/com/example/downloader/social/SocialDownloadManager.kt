package com.example.downloader.social

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class SocialDownloadState {
    object Idle : SocialDownloadState()

    data class LinkDetected(
        val url: String,
        val platform: SocialPlatform
    ) : SocialDownloadState()

    data class ChoosingFormat(
        val url: String,
        val platform: SocialPlatform,
        val title: String = ""
    ) : SocialDownloadState()

    data class ChoosingQuality(
        val url: String,
        val platform: SocialPlatform,
        val title: String = "",
        val isAudio: Boolean = false
    ) : SocialDownloadState()

    data class Resolving(
        val url: String,
        val platform: SocialPlatform,
        val message: String = "Fetching media details..."
    ) : SocialDownloadState()

    data class Downloading(
        val url: String,
        val platform: SocialPlatform,
        val title: String,
        val quality: String,
        val isAudio: Boolean,
        val progress: Int, // 0..100
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : SocialDownloadState()

    data class Success(
        val filePath: String,
        val fileName: String,
        val platform: SocialPlatform,
        val isAudio: Boolean,
        val quality: String,
        val title: String = "",
        val metadata: SocialMediaMetadata? = null
    ) : SocialDownloadState()

    data class Error(
        val message: String,
        val url: String? = null,
        val platform: SocialPlatform = SocialPlatform.UNKNOWN
    ) : SocialDownloadState()
}

class SocialDownloadManager(private val context: Context) {

    companion object {
        private const val TAG = "SocialDownloader"
        private const val CHANNEL_ID = "social_downloader_channel"
        private const val NOTIFICATION_ID = 5050

        @Volatile
        private var INSTANCE: SocialDownloadManager? = null

        fun getInstance(context: Context): SocialDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocialDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var downloadJob: Job? = null

    private val _state = MutableStateFlow<SocialDownloadState>(SocialDownloadState.Idle)
    val state: StateFlow<SocialDownloadState> = _state.asStateFlow()

    private var currentUrl: String = ""
    private var currentPlatform: SocialPlatform = SocialPlatform.UNKNOWN
    private var currentMetadata: SocialMediaMetadata? = null
    private var isAudioSelected: Boolean = false
    private var selectedQuality: String = "720p HD"
    private var lastDismissedUrl: String? = null

    // Callback when user requests in-keyboard playback of a downloaded file
    var onPlayInKeyboardRequested: ((filePath: String, isAudio: Boolean, title: String) -> Unit)? = null

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Social Media Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time progress of TikTok & Facebook media downloads"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if text contains a TikTok or Facebook link and activates prompt if new.
     */
    fun onClipboardUpdated(text: String?): Boolean {
        val detectedPair = SocialMediaExtractor.extractSocialUrl(text) ?: return false
        val detectedUrl = detectedPair.first
        val detectedPlatform = detectedPair.second

        if (detectedUrl == lastDismissedUrl && _state.value is SocialDownloadState.Idle) {
            return false
        }
        // Don't interrupt if currently downloading
        if (_state.value is SocialDownloadState.Downloading || _state.value is SocialDownloadState.Resolving) {
            return false
        }
        currentUrl = detectedUrl
        currentPlatform = detectedPlatform
        _state.value = SocialDownloadState.LinkDetected(detectedUrl, detectedPlatform)
        return true
    }

    fun onDownloadClicked() {
        val url = currentUrl
        if (url.isNotBlank()) {
            _state.value = SocialDownloadState.ChoosingFormat(url, currentPlatform)
        }
    }

    fun onFormatChosen(isAudio: Boolean) {
        isAudioSelected = isAudio
        val url = currentUrl
        if (isAudio) {
            startDownload(url = url, quality = "MP3 Audio", isAudio = true)
        } else {
            _state.value = SocialDownloadState.ChoosingQuality(
                url = url,
                platform = currentPlatform,
                isAudio = false
            )
        }
    }

    fun onQualityChosen(quality: String) {
        selectedQuality = quality
        startDownload(url = currentUrl, quality = quality, isAudio = false)
    }

    fun dismiss() {
        lastDismissedUrl = currentUrl
        cancelDownload()
        _state.value = SocialDownloadState.Idle
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        notificationManager?.cancel(NOTIFICATION_ID)
        if (_state.value is SocialDownloadState.Downloading || _state.value is SocialDownloadState.Resolving) {
            _state.value = SocialDownloadState.Idle
        }
    }

    fun retry() {
        if (currentUrl.isNotBlank()) {
            startDownload(currentUrl, selectedQuality, isAudioSelected)
        }
    }

    fun playInKeyboard(filePath: String, isAudio: Boolean, title: String = "") {
        onPlayInKeyboardRequested?.invoke(filePath, isAudio, title)
    }

    /**
     * Extracts rich metadata for a given URL asynchronously.
     */
    suspend fun fetchMetadata(url: String): SocialMediaMetadata? = withContext(Dispatchers.IO) {
        val platform = SocialMediaExtractor.detectPlatform(url)
        SocialMediaExtractor.resolveMedia(okHttpClient, url, platform)
    }

    private fun startDownload(url: String, quality: String, isAudio: Boolean) {
        downloadJob?.cancel()
        val platform = currentPlatform
        downloadJob = scope.launch {
            try {
                _state.value = SocialDownloadState.Resolving(
                    url = url,
                    platform = platform,
                    message = "Fetching ${platform.displayName} details..."
                )
                updateNotificationProgress("Fetching ${platform.displayName} details...", 0, true)

                // 1. Resolve direct download link
                val mediaInfo = SocialMediaExtractor.resolveMedia(okHttpClient, url, platform)
                if (mediaInfo == null) {
                    _state.value = SocialDownloadState.Error(
                        "Unable to extract ${platform.displayName} video. Check link or network.",
                        url,
                        platform
                    )
                    cancelNotification()
                    return@launch
                }
                currentMetadata = mediaInfo

                val downloadTargetUrl = when {
                    isAudio -> mediaInfo.downloadAudioUrl ?: mediaInfo.downloadSdUrl ?: mediaInfo.downloadHdUrl
                    quality.contains("1080") || quality.contains("HD") -> mediaInfo.downloadHdUrl ?: mediaInfo.downloadSdUrl
                    else -> mediaInfo.downloadSdUrl ?: mediaInfo.downloadHdUrl
                }

                if (downloadTargetUrl.isNullOrBlank()) {
                    _state.value = SocialDownloadState.Error(
                        "No direct download stream found for $quality.",
                        url,
                        platform
                    )
                    cancelNotification()
                    return@launch
                }

                // 2. Perform streaming download
                val cleanTitle = mediaInfo.title.take(30).replace(Regex("[^a-zA-Z0-9_\\-]"), "_").trim('_')
                val baseName = if (cleanTitle.isNotBlank()) cleanTitle else "${platform.displayName}_${System.currentTimeMillis()}"
                val extension = if (isAudio) "mp3" else "mp4"
                val fileName = "${baseName}_$quality.$extension"

                val destinationDir = getDownloadDirectory(platform)
                val outputFile = File(destinationDir, fileName)

                _state.value = SocialDownloadState.Downloading(
                    url = url,
                    platform = platform,
                    title = mediaInfo.title,
                    quality = quality,
                    isAudio = isAudio,
                    progress = 0,
                    downloadedBytes = 0,
                    totalBytes = 0
                )

                val downloadRequest = Request.Builder()
                    .url(downloadTargetUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                    .build()

                val response = okHttpClient.newCall(downloadRequest).execute()
                if (!response.isSuccessful) {
                    _state.value = SocialDownloadState.Error(
                        "Server error (${response.code}). Failed to download.",
                        url,
                        platform
                    )
                    cancelNotification()
                    return@launch
                }

                val body = response.body ?: run {
                    _state.value = SocialDownloadState.Error("Empty file received.", url, platform)
                    cancelNotification()
                    return@launch
                }

                val totalBytes = body.contentLength()
                var downloadedBytes: Long = 0
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(16384)
                var bytesRead: Int
                var lastProgress = 0

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val progress = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                -1
                            }

                            if (progress != lastProgress && (progress % 2 == 0 || progress == 100)) {
                                lastProgress = progress
                                _state.value = SocialDownloadState.Downloading(
                                    url = url,
                                    platform = platform,
                                    title = mediaInfo.title,
                                    quality = quality,
                                    isAudio = isAudio,
                                    progress = progress.coerceAtLeast(0),
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes
                                )
                                updateNotificationProgress(
                                    "Downloading ${platform.displayName} $quality ($progress%)",
                                    progress,
                                    false
                                )
                            }
                        }
                        output.flush()
                    }
                }

                // Notify MediaScanner
                try {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = Uri.fromFile(outputFile)
                    context.sendBroadcast(mediaScanIntent)
                } catch (_: Exception) {}

                _state.value = SocialDownloadState.Success(
                    filePath = outputFile.absolutePath,
                    fileName = fileName,
                    platform = platform,
                    isAudio = isAudio,
                    quality = quality,
                    title = mediaInfo.title,
                    metadata = mediaInfo
                )
                showDownloadCompleteNotification(outputFile, fileName, platform, isAudio)

            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                if (e !is kotlinx.coroutines.CancellationException) {
                    _state.value = SocialDownloadState.Error(
                        "Download failed: ${e.localizedMessage ?: "Unknown error"}",
                        url,
                        platform
                    )
                    cancelNotification()
                }
            }
        }
    }

    private fun getDownloadDirectory(platform: SocialPlatform): File {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (publicDownloads != null && (publicDownloads.exists() || publicDownloads.mkdirs())) {
            val folder = File(publicDownloads, platform.displayName)
            if (!folder.exists()) folder.mkdirs()
            return folder
        }
        val appDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (appDownloads != null && (appDownloads.exists() || appDownloads.mkdirs())) {
            return appDownloads
        }
        return context.filesDir
    }

    private fun updateNotificationProgress(status: String, progress: Int, indeterminate: Boolean) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Social Media Downloader")
                .setContentText(status)
                .setProgress(100, progress.coerceAtLeast(0), indeterminate)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }

    private fun showDownloadCompleteNotification(file: File, fileName: String, platform: SocialPlatform, isAudio: Boolean) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (isAudio) "audio/*" else "video/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("${platform.displayName} Download Complete")
                .setContentText(fileName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }

    private fun cancelNotification() {
        try {
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    fun openFile(filePath: String, isAudio: Boolean) {
        try {
            val file = File(filePath)
            if (!file.exists()) return
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (isAudio) "audio/*" else "video/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file", e)
        }
    }

    fun shareFile(filePath: String, isAudio: Boolean) {
        try {
            val file = File(filePath)
            if (!file.exists()) return
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isAudio) "audio/*" else "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(intent, "Share Media").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
        }
    }
}
