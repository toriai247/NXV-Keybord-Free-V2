package com.example.sticker

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class StickerManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs = context.getSharedPreferences("nxv_stickers_pref", Context.MODE_PRIVATE)

    private val _stickerPacks = MutableStateFlow<List<StickerPack>>(emptyList())
    val stickerPacks: StateFlow<List<StickerPack>> = _stickerPacks.asStateFlow()

    private val _recentStickers = MutableStateFlow<List<StickerItem>>(emptyList())
    val recentStickers: StateFlow<List<StickerItem>> = _recentStickers.asStateFlow()

    init {
        loadPacks()
        loadRecents()
    }

    private fun loadPacks() {
        scope.launch {
            try {
                val packFolders = listOf(
                    Triple("samu", "Samu", "🐱"),
                    Triple("cutie_cat", "Cutie Cat", "🐾"),
                    Triple("cures", "Cures", "💖"),
                    Triple("rino", "Rino", "🌸"),
                    Triple("ripu", "Ripu", "✨")
                )

                val assetManager = context.assets
                val loadedPacks = mutableListOf<StickerPack>()

                for ((folder, displayName, emoji) in packFolders) {
                    val files = assetManager.list("stickers/$folder") ?: emptyArray()
                    val validFiles = files.filter { it.endsWith(".webp", true) || it.endsWith(".jpg", true) || it.endsWith(".png", true) }
                    if (validFiles.isNotEmpty()) {
                        val stickerItems = validFiles.mapIndexed { index, fileName ->
                            val mime = when {
                                fileName.endsWith(".webp", true) -> "image/webp"
                                fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                                else -> "image/png"
                            }
                            StickerItem(
                                id = "${folder}_$index",
                                packId = folder,
                                assetPath = "stickers/$folder/$fileName",
                                mimeType = mime,
                                displayName = "$displayName #${index + 1}"
                            )
                        }

                        loadedPacks.add(
                            StickerPack(
                                id = folder,
                                name = displayName,
                                category = displayName,
                                previewAssetPath = stickerItems.first().assetPath,
                                previewEmoji = emoji,
                                stickers = stickerItems
                            )
                        )
                    }
                }

                _stickerPacks.value = loadedPacks
            } catch (e: Exception) {
                Log.e("StickerManager", "Error loading sticker packs", e)
            }
        }
    }

    private fun loadRecents() {
        val savedIds = prefs.getString("recent_sticker_paths", "") ?: ""
        if (savedIds.isBlank()) return

        scope.launch {
            val paths = savedIds.split(";").filter { it.isNotBlank() }
            val recents = paths.mapNotNull { path ->
                val fileName = path.substringAfterLast('/')
                val folder = path.substringBeforeLast('/').substringAfterLast('/')
                val mime = when {
                    fileName.endsWith(".webp", true) -> "image/webp"
                    fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                    else -> "image/png"
                }
                StickerItem(
                    id = "recent_${path.hashCode()}",
                    packId = folder,
                    assetPath = path,
                    mimeType = mime,
                    displayName = "Sticker"
                )
            }
            _recentStickers.value = recents
        }
    }

    private fun recordRecent(sticker: StickerItem) {
        scope.launch {
            val current = _recentStickers.value.toMutableList()
            current.removeAll { it.assetPath == sticker.assetPath }
            current.add(0, sticker)
            val trimmed = current.take(24)
            _recentStickers.value = trimmed

            val serialized = trimmed.joinToString(";") { it.assetPath }
            prefs.edit().putString("recent_sticker_paths", serialized).apply()
        }
    }

    /**
     * Sends the sticker to the active input connection using Rich Content (commitContent).
     * If commitContent is unsupported by target app, copies image to system clipboard.
     */
    fun sendSticker(
        sticker: StickerItem,
        inputConnection: InputConnection?,
        editorInfo: EditorInfo?,
        onSuccess: () -> Unit = {},
        onCopiedToClipboard: () -> Unit = {}
    ) {
        recordRecent(sticker)

        scope.launch {
            try {
                // Ensure asset is copied to cache directory
                val cacheDir = File(context.cacheDir, "stickers").apply { mkdirs() }
                val extension = sticker.assetPath.substringAfterLast('.', "webp")
                val sanitizedFileName = "${sticker.packId}_${sticker.assetPath.hashCode()}.$extension"
                val cacheFile = File(cacheDir, sanitizedFileName)

                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    context.assets.open(sticker.assetPath).use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )

                // Grant read permission to the host app
                editorInfo?.packageName?.let { hostPkg ->
                    try {
                        context.grantUriPermission(
                            hostPkg,
                            contentUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) {}
                }

                val mimeType = sticker.mimeType
                val description = ClipDescription(sticker.displayName, arrayOf(mimeType, "image/*"))
                val inputContentInfo = InputContentInfoCompat(contentUri, description, null)

                var flags = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    flags = flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
                }

                var committed = false
                if (inputConnection != null && editorInfo != null) {
                    try {
                        committed = InputConnectionCompat.commitContent(
                            inputConnection,
                            editorInfo,
                            inputContentInfo,
                            flags,
                            null
                        )
                    } catch (e: Exception) {
                        Log.e("StickerManager", "commitContent failed", e)
                        committed = false
                    }
                }

                withContext(Dispatchers.Main) {
                    if (committed) {
                        onSuccess()
                    } else {
                        // Fallback: Copy to system clipboard
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newUri(
                                context.contentResolver,
                                sticker.displayName,
                                contentUri
                            )
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                "Sticker copied to clipboard! Paste into chat.",
                                Toast.LENGTH_SHORT
                            ).show()
                            onCopiedToClipboard()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not send sticker.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StickerManager", "Error sending sticker", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load sticker: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: StickerManager? = null

        fun getInstance(context: Context): StickerManager {
            return instance ?: synchronized(this) {
                instance ?: StickerManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
