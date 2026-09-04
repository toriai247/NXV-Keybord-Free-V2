package com.example.downloader.tiktok

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
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class TikTokDownloadState {
    object Idle : TikTokDownloadState()
    data class LinkDetected(val url: String) : TikTokDownloadState()
    data class ChoosingFormat(val url: String) : TikTokDownloadState()
    data class ChoosingQuality(val url: String, val isAudio: Boolean) : TikTokDownloadState()
    data class Resolving(val url: String, val message: String = "Fetching video details...") : TikTokDownloadState()
    data class Downloading(
        val url: String,
        val title: String,
        val quality: String,
        val isAudio: Boolean,
        val progress: Int, // 0..100
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : TikTokDownloadState()
    data class Success(
        val filePath: String,
        val fileName: String,
        val isAudio: Boolean,
        val quality: String
    ) : TikTokDownloadState()
    data class Error(val message: String, val url: String? = null) : TikTokDownloadState()
}

class TikTokDownloadManager(private val context: Context) {

    companion object {
        private const val TAG = "TikTokDownloader"
        private const val CHANNEL_ID = "tiktok_downloader_channel"
        private const val NOTIFICATION_ID = 4040

        private val TIKTOK_URL_PATTERN = Pattern.compile(
            "https?://(?:(?:www|vm|vt|m|t)\\.)?tiktok\\.com/[^\\s\"'<>]+",
            Pattern.CASE_INSENSITIVE
        )

        @Volatile
        private var INSTANCE: TikTokDownloadManager? = null

        fun getInstance(context: Context): TikTokDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TikTokDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun extractTikTokUrl(text: String?): String? {
            if (text.isNullOrBlank()) return null
            val matcher = TIKTOK_URL_PATTERN.matcher(text)
            return if (matcher.find()) matcher.group(0) else null
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var downloadJob: Job? = null

    private val _state = MutableStateFlow<TikTokDownloadState>(TikTokDownloadState.Idle)
    val state: StateFlow<TikTokDownloadState> = _state.asStateFlow()

    private var currentUrl: String = ""
    private var isAudioSelected: Boolean = false
    private var selectedQuality: String = "720p"
    private var lastDismissedUrl: String? = null

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
                "TikTok Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time progress of TikTok media downloads"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if text contains a TikTok link and activates the LinkDetected prompt if new.
     */
    fun onClipboardUpdated(text: String?): Boolean {
        val detected = extractTikTokUrl(text) ?: return false
        if (detected == lastDismissedUrl && _state.value is TikTokDownloadState.Idle) {
            return false
        }
        // Don't interrupt if currently downloading
        if (_state.value is TikTokDownloadState.Downloading || _state.value is TikTokDownloadState.Resolving) {
            return false
        }
        currentUrl = detected
        _state.value = TikTokDownloadState.LinkDetected(detected)
        return true
    }

    /**
     * User clicks "Download Video" or toolbar action
     */
    fun onDownloadClicked() {
        val url = currentUrl
        if (url.isNotBlank()) {
            _state.value = TikTokDownloadState.ChoosingFormat(url)
        }
    }

    /**
     * User chooses "Video" or "Audio"
     */
    fun onFormatChosen(isAudio: Boolean) {
        isAudioSelected = isAudio
        val url = currentUrl
        if (isAudio) {
            // Audio doesn't need resolution selection, start download directly
            startDownload(url = url, quality = "MP3 Audio", isAudio = true)
        } else {
            // Video: ask for 480p, 720p, or 1080p
            _state.value = TikTokDownloadState.ChoosingQuality(url = url, isAudio = false)
        }
    }

    /**
     * User chooses quality: "480p", "720p", "1080p"
     */
    fun onQualityChosen(quality: String) {
        selectedQuality = quality
        startDownload(url = currentUrl, quality = quality, isAudio = false)
    }

    /**
     * Dismisses the banner
     */
    fun dismiss() {
        lastDismissedUrl = currentUrl
        cancelDownload()
        _state.value = TikTokDownloadState.Idle
    }

    /**
     * Cancels active download
     */
    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        notificationManager?.cancel(NOTIFICATION_ID)
        if (_state.value is TikTokDownloadState.Downloading || _state.value is TikTokDownloadState.Resolving) {
            _state.value = TikTokDownloadState.Idle
        }
    }

    /**
     * Retries last action
     */
    fun retry() {
        if (currentUrl.isNotBlank()) {
            startDownload(currentUrl, selectedQuality, isAudioSelected)
        }
    }

    private fun startDownload(url: String, quality: String, isAudio: Boolean) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            try {
                _state.value = TikTokDownloadState.Resolving(url, "Fetching TikTok details...")
                updateNotificationProgress("Fetching video details...", 0, true)

                // 1. Resolve direct download link via TikWM API
                val mediaInfo = resolveTikTokMedia(url)
                if (mediaInfo == null) {
                    _state.value = TikTokDownloadState.Error("Unable to extract TikTok video. Check link or network.", url)
                    cancelNotification()
                    return@launch
                }

                val downloadTargetUrl = when {
                    isAudio -> mediaInfo.audioUrl ?: mediaInfo.playUrl
                    quality.contains("1080") -> mediaInfo.hdPlayUrl ?: mediaInfo.playUrl
                    quality.contains("720") -> mediaInfo.playUrl ?: mediaInfo.hdPlayUrl
                    else -> mediaInfo.playUrl ?: mediaInfo.hdPlayUrl
                }

                if (downloadTargetUrl.isNullOrBlank()) {
                    _state.value = TikTokDownloadState.Error("No valid download link found for $quality.", url)
                    cancelNotification()
                    return@launch
                }

                // 2. Perform streaming download
                val cleanTitle = mediaInfo.title.take(30).replace(Regex("[^a-zA-Z0-9_\\-]"), "_").trim('_')
                val baseName = if (cleanTitle.isNotBlank()) cleanTitle else "TikTok_${System.currentTimeMillis()}"
                val extension = if (isAudio) "mp3" else "mp4"
                val fileName = "${baseName}_$quality.$extension"

                // Destination file
                val destinationDir = getDownloadDirectory()
                val outputFile = File(destinationDir, fileName)

                _state.value = TikTokDownloadState.Downloading(
                    url = url,
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
                    _state.value = TikTokDownloadState.Error("Server error (${response.code}). Failed to download.", url)
                    cancelNotification()
                    return@launch
                }

                val body = response.body ?: run {
                    _state.value = TikTokDownloadState.Error("Empty file received.", url)
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
                                _state.value = TikTokDownloadState.Downloading(
                                    url = url,
                                    title = mediaInfo.title,
                                    quality = quality,
                                    isAudio = isAudio,
                                    progress = progress.coerceAtLeast(0),
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes
                                )
                                updateNotificationProgress("Downloading $quality ($progress%)", progress, false)
                            }
                        }
                        output.flush()
                    }
                }

                // Notify Android MediaScanner so file appears in gallery/downloads immediately
                try {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = Uri.fromFile(outputFile)
                    context.sendBroadcast(mediaScanIntent)
                } catch (_: Exception) {}

                _state.value = TikTokDownloadState.Success(
                    filePath = outputFile.absolutePath,
                    fileName = fileName,
                    isAudio = isAudio,
                    quality = quality
                )
                showDownloadCompleteNotification(outputFile, fileName, isAudio)

            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                if (e !is kotlinx.coroutines.CancellationException) {
                    _state.value = TikTokDownloadState.Error("Download failed: ${e.localizedMessage ?: "Unknown error"}", url)
                    cancelNotification()
                }
            }
        }
    }

    private data class TikTokMediaInfo(
        val title: String,
        val playUrl: String?,
        val hdPlayUrl: String?,
        val audioUrl: String?
    )

    private fun resolveTikTokMedia(tiktokUrl: String): TikTokMediaInfo? {
        val endpoints = listOf(
            "https://www.tikwm.com/api/",
            "https://api.tikwm.com/api/"
        )

        for (endpoint in endpoints) {
            try {
                val formBody = FormBody.Builder()
                    .add("url", tiktokUrl)
                    .add("hd", "1")
                    .build()

                val request = Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseStr = response.body?.string() ?: continue
                val json = JSONObject(responseStr)
                val code = json.optInt("code", -1)

                if (code == 0 && json.has("data")) {
                    val data = json.getJSONObject("data")
                    val title = data.optString("title", "TikTok Video")
                    var play = data.optString("play", null)
                    var hdplay = data.optString("hdplay", null)
                    var music = data.optString("music", null)

                    // Normalize relative URLs
                    if (play?.startsWith("/") == true) play = "https://www.tikwm.com$play"
                    if (hdplay?.startsWith("/") == true) hdplay = "https://www.tikwm.com$hdplay"
                    if (music?.startsWith("/") == true) music = "https://www.tikwm.com$music"

                    return TikTokMediaInfo(
                        title = title,
                        playUrl = play,
                        hdPlayUrl = hdplay,
                        audioUrl = music
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed resolving with $endpoint: ${e.message}")
            }
        }
        return null
    }

    private fun getDownloadDirectory(): File {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (publicDownloads != null && (publicDownloads.exists() || publicDownloads.mkdirs())) {
            val tiktokFolder = File(publicDownloads, "TikTok")
            if (!tiktokFolder.exists()) tiktokFolder.mkdirs()
            return tiktokFolder
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
                .setContentTitle("TikTok Downloader")
                .setContentText(status)
                .setProgress(100, progress.coerceAtLeast(0), indeterminate)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }

    private fun showDownloadCompleteNotification(file: File, fileName: String, isAudio: Boolean) {
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
                .setContentTitle("Download Complete")
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
            context.startActivity(Intent.createChooser(intent, "Share TikTok Media").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
        }
    }
}
