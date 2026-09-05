package com.example.ui.keyboard

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.downloader.social.SocialPlatform
import com.example.theme.KeyboardPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocalMediaFile(
    val id: String,
    val file: File,
    val title: String,
    val filePath: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isAudio: Boolean,
    val durationMs: Long = 0,
    val platform: SocialPlatform = SocialPlatform.UNKNOWN
) {
    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1.0) {
                String.format(Locale.US, "%.1f MB", mb)
            } else {
                val kb = sizeBytes / 1024.0
                String.format(Locale.US, "%.0f KB", kb)
            }
        }

    val formattedDuration: String
        get() {
            if (durationMs <= 0) return ""
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return String.format(Locale.US, "%d:%02d", min, sec)
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}

enum class MediaFilterTab(val label: String) {
    ALL("All"),
    TIKTOK("TikTok"),
    FACEBOOK("Facebook"),
    VIDEOS("Videos"),
    AUDIO("Audio")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalMediaBrowserLayout(
    palette: KeyboardPalette,
    onCloseBrowser: () -> Unit,
    onPlayMedia: (filePath: String, isAudio: Boolean, title: String) -> Unit,
    onPasteText: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var filesList by remember { mutableStateOf<List<LocalMediaFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasPermission by remember { mutableStateOf(StoragePermissionActivity.hasStoragePermission(context)) }
    var selectedFilter by remember { mutableStateOf(MediaFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<LocalMediaFile?>(null) }
    var actionSheetFile by remember { mutableStateOf<LocalMediaFile?>(null) }
    var copyToastMessage by remember { mutableStateOf<String?>(null) }

    fun scanLocalFiles() {
        isLoading = true
        coroutineScope.launch {
            val foundList = withContext(Dispatchers.IO) {
                val list = mutableListOf<LocalMediaFile>()
                val seenPaths = HashSet<String>()

                fun inspectAndAdd(file: File, fallbackPlatform: SocialPlatform? = null) {
                    if (!file.exists() || !file.isFile) return
                    val path = file.absolutePath
                    if (seenPaths.contains(path)) return
                    seenPaths.add(path)

                    val nameLower = file.name.lowercase(Locale.ROOT)
                    val isVideo = nameLower.endsWith(".mp4") || nameLower.endsWith(".mkv") || nameLower.endsWith(".webm") || nameLower.endsWith(".3gp")
                    val isAudio = nameLower.endsWith(".mp3") || nameLower.endsWith(".m4a") || nameLower.endsWith(".wav") || nameLower.endsWith(".aac")

                    if (!isVideo && !isAudio) return

                    val platform = fallbackPlatform ?: when {
                        nameLower.contains("tiktok") || path.contains("/TikTok/") -> SocialPlatform.TIKTOK
                        nameLower.contains("facebook") || nameLower.contains("fb_") || path.contains("/Facebook/") -> SocialPlatform.FACEBOOK
                        else -> SocialPlatform.UNKNOWN
                    }

                    var durationMs = 0L
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(path)
                        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        durationMs = durationStr?.toLongOrNull() ?: 0L
                        retriever.release()
                    } catch (_: Exception) {}

                    val cleanTitle = file.nameWithoutExtension
                        .replace(Regex("^(tiktok|facebook|fb)_", RegexOption.IGNORE_CASE), "")
                        .replace("_", " ")
                        .trim()

                    list.add(
                        LocalMediaFile(
                            id = path,
                            file = file,
                            title = if (cleanTitle.isNotBlank()) cleanTitle else file.name,
                            filePath = path,
                            sizeBytes = file.length(),
                            lastModified = file.lastModified(),
                            isAudio = isAudio,
                            durationMs = durationMs,
                            platform = platform
                        )
                    )
                }

                // 1. Scan Public Downloads/TikTok
                val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (publicDownloads != null && publicDownloads.exists()) {
                    val ttFolder = File(publicDownloads, "TikTok")
                    if (ttFolder.exists()) {
                        ttFolder.listFiles()?.forEach { inspectAndAdd(it, SocialPlatform.TIKTOK) }
                    }
                    val fbFolder = File(publicDownloads, "Facebook")
                    if (fbFolder.exists()) {
                        fbFolder.listFiles()?.forEach { inspectAndAdd(it, SocialPlatform.FACEBOOK) }
                    }
                    publicDownloads.listFiles()?.forEach {
                        if (it.isFile) inspectAndAdd(it)
                    }
                }

                // 2. Scan App-Specific Downloads (works without storage permissions)
                val appDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (appDownloads != null && appDownloads.exists()) {
                    appDownloads.listFiles()?.forEach {
                        if (it.isFile) inspectAndAdd(it)
                    }
                    val ttSub = File(appDownloads, "TikTok")
                    if (ttSub.exists()) {
                        ttSub.listFiles()?.forEach { inspectAndAdd(it, SocialPlatform.TIKTOK) }
                    }
                    val fbSub = File(appDownloads, "Facebook")
                    if (fbSub.exists()) {
                        fbSub.listFiles()?.forEach { inspectAndAdd(it, SocialPlatform.FACEBOOK) }
                    }
                }

                // 3. MediaStore query if permission is granted
                if (hasPermission) {
                    try {
                        val videoProjection = arrayOf(
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media.DISPLAY_NAME,
                            MediaStore.Video.Media.SIZE,
                            MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media.DATE_MODIFIED
                        )
                        val cursor = context.contentResolver.query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            videoProjection,
                            null,
                            null,
                            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
                        )
                        cursor?.use { c ->
                            val dataCol = c.getColumnIndex(MediaStore.Video.Media.DATA)
                            val nameCol = c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                            val sizeCol = c.getColumnIndex(MediaStore.Video.Media.SIZE)
                            val durCol = c.getColumnIndex(MediaStore.Video.Media.DURATION)
                            val dateCol = c.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)

                            while (c.moveToNext()) {
                                val path = if (dataCol >= 0) c.getString(dataCol) else null
                                if (path != null && !seenPaths.contains(path)) {
                                    val f = File(path)
                                    if (f.exists()) {
                                        seenPaths.add(path)
                                        val name = if (nameCol >= 0) c.getString(nameCol) else f.name
                                        val size = if (sizeCol >= 0) c.getLong(sizeCol) else f.length()
                                        val dur = if (durCol >= 0) c.getLong(durCol) else 0L
                                        val date = if (dateCol >= 0) c.getLong(dateCol) * 1000L else f.lastModified()

                                        val nameLower = name.lowercase(Locale.ROOT)
                                        val platform = when {
                                            nameLower.contains("tiktok") || path.contains("/TikTok/") -> SocialPlatform.TIKTOK
                                            nameLower.contains("facebook") || nameLower.contains("fb_") || path.contains("/Facebook/") -> SocialPlatform.FACEBOOK
                                            else -> SocialPlatform.UNKNOWN
                                        }

                                        list.add(
                                            LocalMediaFile(
                                                id = path,
                                                file = f,
                                                title = f.nameWithoutExtension.replace("_", " "),
                                                filePath = path,
                                                sizeBytes = size,
                                                lastModified = date,
                                                isAudio = false,
                                                durationMs = dur,
                                                platform = platform
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                // Sort newest first
                list.sortedByDescending { it.lastModified }
            }

            filesList = foundList
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        hasPermission = StoragePermissionActivity.hasStoragePermission(context)
        scanLocalFiles()
    }

    LaunchedEffect(copyToastMessage) {
        if (copyToastMessage != null) {
            kotlinx.coroutines.delay(1500)
            copyToastMessage = null
        }
    }

    fun requestStoragePermission() {
        StoragePermissionActivity.requestPermission(context) { granted ->
            hasPermission = granted
            scanLocalFiles()
        }
    }

    fun shareMedia(item: LocalMediaFile) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", item.file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (item.isAudio) "audio/*" else "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "${item.title}\nSaved with NXV Keyboard")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val chooser = Intent.createChooser(intent, "Share ${item.title}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }

    fun openExternal(item: LocalMediaFile) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", item.file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (item.isAudio) "audio/*" else "video/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun deleteMedia(item: LocalMediaFile) {
        try {
            if (item.file.exists()) {
                item.file.delete()
            }
            filesList = filesList.filter { it.id != item.id }
        } catch (_: Exception) {}
    }

    val filteredList = filesList.filter { item ->
        val matchesFilter = when (selectedFilter) {
            MediaFilterTab.ALL -> true
            MediaFilterTab.TIKTOK -> item.platform == SocialPlatform.TIKTOK
            MediaFilterTab.FACEBOOK -> item.platform == SocialPlatform.FACEBOOK
            MediaFilterTab.VIDEOS -> !item.isAudio
            MediaFilterTab.AUDIO -> item.isAudio
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery.trim(), ignoreCase = true) ||
                item.file.name.contains(searchQuery.trim(), ignoreCase = true)

        matchesFilter && matchesSearch
    }

    val totalSizeBytes = filesList.sumOf { it.sizeBytes }
    val formattedTotalSize = if (totalSizeBytes > 1024 * 1024 * 1024) {
        String.format(Locale.US, "%.1f GB", totalSizeBytes / (1024.0 * 1024.0 * 1024.0))
    } else {
        String.format(Locale.US, "%.1f MB", totalSizeBytes / (1024.0 * 1024.0))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(palette.keyboardBackground)
            .testTag("local_media_browser")
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(palette.suggestionBarBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back to typing chip + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground)
                        .clickable { onCloseBrowser() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("browser_btn_typing"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Typing",
                            tint = palette.textColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Typing",
                            color = palette.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = palette.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Downloaded Media",
                        color = palette.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (filesList.isNotEmpty()) {
                        Text(
                            text = "(${filesList.size})",
                            color = palette.secondaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Search, Refresh, Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSearchActive) palette.accentColor.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        }
                        .padding(6.dp)
                        .testTag("browser_btn_search"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Refresh scan
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { scanLocalFiles() }
                        .padding(6.dp)
                        .testTag("browser_btn_refresh"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = palette.textColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Close browser
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onCloseBrowser() }
                        .padding(6.dp)
                        .testTag("browser_btn_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = palette.textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Search bar (if active)
        AnimatedVisibility(visible = isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(palette.keyBackground)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = palette.secondaryTextColor,
                    modifier = Modifier.padding(end = 6.dp).size(14.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search downloaded files...",
                            color = palette.secondaryTextColor,
                            fontSize = 12.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = palette.textColor, fontSize = 12.sp),
                        cursorBrush = SolidColor(palette.accentColor),
                        modifier = Modifier.fillMaxWidth().testTag("browser_search_input")
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { searchQuery = "" }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = palette.secondaryTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Filter Pills & Storage Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Filter pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MediaFilterTab.values().forEach { tab ->
                    val isSelected = selectedFilter == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) palette.accentColor
                                else palette.keyBackground
                            )
                            .clickable { selectedFilter = tab }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("browser_filter_${tab.name.lowercase(Locale.ROOT)}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.White else palette.textColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (filesList.isNotEmpty()) {
                Text(
                    text = "$formattedTotalSize total",
                    color = palette.secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Permission Banner (if not granted)
        if (!hasPermission) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = palette.keyBackground),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = palette.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Grant storage access to browse all videos",
                            fontSize = 10.sp,
                            color = palette.textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { requestStoragePermission() },
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Allow", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Action Sheet / Item Options Card
        actionSheetFile?.let { target ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = palette.keyBackground),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Media File Actions",
                            fontSize = 11.sp,
                            color = palette.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = palette.secondaryTextColor,
                            modifier = Modifier.size(16.dp).clickable { actionSheetFile = null }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = target.title,
                        fontSize = 12.sp,
                        color = palette.textColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${target.formattedSize} • ${target.formattedDate}${if (target.formattedDuration.isNotEmpty()) " • ${target.formattedDuration}" else ""}",
                        fontSize = 10.sp,
                        color = palette.secondaryTextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play In Keyboard Mini-View
                        Button(
                            onClick = {
                                onPlayMedia(target.filePath, target.isAudio, target.title)
                                actionSheetFile = null
                            },
                            modifier = Modifier.height(30.dp).weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accentColor),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Text("Play Mini-View", fontSize = 10.sp, color = Color.White)
                            }
                        }

                        // Share
                        Button(
                            onClick = {
                                shareMedia(target)
                                actionSheetFile = null
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.keyActionBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Share, "Share", tint = palette.textColor, modifier = Modifier.size(13.dp))
                        }

                        // Open External Player
                        Button(
                            onClick = {
                                openExternal(target)
                                actionSheetFile = null
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.keyActionBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, "External", tint = palette.textColor, modifier = Modifier.size(13.dp))
                        }

                        // Delete
                        Button(
                            onClick = {
                                fileToDelete = target
                                actionSheetFile = null
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }

        // File List / Empty View
        if (isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = palette.accentColor, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Scanning downloaded videos...", fontSize = 11.sp, color = palette.secondaryTextColor)
                }
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Folder,
                        contentDescription = null,
                        tint = palette.secondaryTextColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No files match \"$searchQuery\""
                        else if (selectedFilter != MediaFilterTab.ALL) "No ${selectedFilter.label} files found"
                        else "No downloaded media files yet",
                        color = palette.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try searching for a different keyword"
                        else "Download TikTok or Facebook videos using the keyboard toolbar to play them here without leaving your chat.",
                        color = palette.secondaryTextColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .combinedClickable(
                                onClick = {
                                    // Direct 1-tap play in keyboard mini-view
                                    onPlayMedia(item.filePath, item.isAudio, item.title)
                                },
                                onLongClick = {
                                    actionSheetFile = item
                                }
                            )
                            .testTag("browser_item_${item.id.hashCode()}"),
                        colors = CardDefaults.cardColors(containerColor = palette.keyBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Media thumbnail/icon badge
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (item.platform) {
                                            SocialPlatform.FACEBOOK -> Color(0xFF1877F2).copy(alpha = 0.18f)
                                            SocialPlatform.TIKTOK -> Color(0xFFFE2C55).copy(alpha = 0.18f)
                                            else -> palette.accentColor.copy(alpha = 0.18f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isAudio) Icons.Default.Audiotrack else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = when (item.platform) {
                                        SocialPlatform.FACEBOOK -> Color(0xFF1877F2)
                                        SocialPlatform.TIKTOK -> Color(0xFFFE2C55)
                                        else -> palette.accentColor
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // File details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (item.platform != SocialPlatform.UNKNOWN) {
                                        val badgeColor = if (item.platform == SocialPlatform.FACEBOOK) Color(0xFF1877F2) else Color(0xFFFE2C55)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeColor.copy(alpha = 0.18f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = item.platform.displayName,
                                                color = badgeColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = item.title,
                                        color = palette.textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.formattedSize,
                                        color = palette.secondaryTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (item.formattedDuration.isNotEmpty()) {
                                        Text(
                                            text = "•",
                                            color = palette.secondaryTextColor.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = item.formattedDuration,
                                            color = palette.accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        text = "•",
                                        color = palette.secondaryTextColor.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )

                                    Text(
                                        text = item.formattedDate,
                                        color = palette.secondaryTextColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Quick Action Buttons (Play Mini-View & Menu)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // 1-Tap Play in Mini-View
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(palette.accentColor)
                                        .clickable {
                                            onPlayMedia(item.filePath, item.isAudio, item.title)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("browser_play_btn_${item.id.hashCode()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Play",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // More Options
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(palette.keyActionBackground)
                                        .clickable { actionSheetFile = item }
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Options",
                                        tint = palette.textColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    fileToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete Downloaded File?") },
            text = {
                Text("Are you sure you want to delete \"${target.title}\" (${target.formattedSize})? This will permanently remove it from your device storage.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteMedia(target)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
