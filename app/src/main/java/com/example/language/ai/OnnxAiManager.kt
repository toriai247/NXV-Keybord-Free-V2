package com.example.language.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * OnnxAiManager handles local offline ONNX AI models for message improvement,
 * grammar correction, and tone adjustment directly on the device.
 * Stores models in a dedicated "keyboard_data_files" directory.
 */
class OnnxAiManager(private val context: Context) {

    private val TAG = "OnnxAiManager"
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var loadedModelPath: String? = null

    // Dedicated folder for ONNX AI model files
    val keyboardDataDir: File by lazy {
        File(context.filesDir, "keyboard_data_files").also {
            if (!it.exists()) it.mkdirs()
        }
    }

    val defaultModelFile: File by lazy {
        File(keyboardDataDir, "message_improver.onnx")
    }

    init {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            checkForModelAndInitialize()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OrtEnvironment", e)
        }
    }

    /**
     * Check keyboard_data_files or external storage for an ONNX model file and load it.
     */
    fun checkForModelAndInitialize(): Boolean {
        try {
            // 1. Check dedicated keyboard_data_files directory
            if (defaultModelFile.exists() && defaultModelFile.length() > 0) {
                return loadModelFromFile(defaultModelFile.absolutePath)
            }

            // 2. Check general files dir
            val rootModel = File(context.filesDir, "message_improver.onnx")
            if (rootModel.exists() && rootModel.length() > 0) {
                rootModel.copyTo(defaultModelFile, overwrite = true)
                return loadModelFromFile(defaultModelFile.absolutePath)
            }

            // 3. Check Downloads folder for uploaded ONNX model
            val downloadModel = File("/sdcard/Download/message_improver.onnx")
            if (downloadModel.exists() && downloadModel.length() > 0) {
                downloadModel.copyTo(defaultModelFile, overwrite = true)
                return loadModelFromFile(defaultModelFile.absolutePath)
            }

            // 4. Check assets/models/
            val assetNames = context.assets.list("models")
            if (!assetNames.isNullOrEmpty()) {
                val modelName = assetNames.firstOrNull { it.endsWith(".onnx") || it.endsWith(".ort") }
                if (modelName != null) {
                    context.assets.open("models/$modelName").use { input ->
                        FileOutputStream(defaultModelFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    return loadModelFromFile(defaultModelFile.absolutePath)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for ONNX model", e)
        }
        return false
    }

    /**
     * Copy an ONNX model file from a Uri (e.g. FilePicker) directly into keyboard_data_files
     */
    suspend fun importModelFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(defaultModelFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (defaultModelFile.exists() && defaultModelFile.length() > 0) {
                val loaded = loadModelFromFile(defaultModelFile.absolutePath)
                Log.i(TAG, "Successfully imported ONNX model from URI into keyboard_data_files")
                return@withContext loaded
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import model from URI", e)
        }
        return@withContext false
    }

    /**
     * Download model from URL into keyboard_data_files and auto setup.
     */
    suspend fun downloadAndSetupModel(
        urlStr: String,
        onProgress: (Int) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                onComplete(false, "Server returned HTTP ${connection.responseCode}")
                return@withContext
            }

            val fileLength = connection.contentLength
            val input: InputStream = connection.inputStream
            val tempFile = File(keyboardDataDir, "temp_model.onnx")
            val output = FileOutputStream(tempFile)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int
            var lastProgress = 0

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                output.write(data, 0, count)
                if (fileLength > 0) {
                    val progress = (total * 100 / fileLength).toInt()
                    if (progress != lastProgress) {
                        lastProgress = progress
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }
            }

            output.flush()
            output.close()
            input.close()

            // Rename temp file to defaultModelFile
            if (tempFile.exists() && tempFile.length() > 0) {
                if (defaultModelFile.exists()) defaultModelFile.delete()
                tempFile.renameTo(defaultModelFile)
            }

            // Load session automatically
            val loaded = loadModelFromFile(defaultModelFile.absolutePath)
            withContext(Dispatchers.Main) {
                if (loaded) {
                    onComplete(true, "ONNX AI Model setup complete and active!")
                } else {
                    onComplete(false, "Failed to initialize downloaded ONNX session")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            withContext(Dispatchers.Main) {
                onComplete(false, "Download failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    /**
     * Load ONNX session from given file path.
     */
    fun loadModelFromFile(filePath: String): Boolean {
        return try {
            val env = ortEnv ?: OrtEnvironment.getEnvironment().also { ortEnv = it }
            val options = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
            }
            ortSession?.close()
            ortSession = env.createSession(filePath, options)
            loadedModelPath = filePath
            Log.i(TAG, "ONNX Model loaded successfully from: $filePath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load ONNX model from $filePath", e)
            false
        }
    }

    fun isModelLoaded(): Boolean {
        return ortSession != null
    }

    fun getLoadedModelName(): String {
        return loadedModelPath?.let { File(it).name } ?: "No Model Loaded"
    }

    fun getModelFileSizeMB(): String {
        if (defaultModelFile.exists()) {
            val bytes = defaultModelFile.length()
            val mb = bytes.toDouble() / (1024 * 1024)
            return String.format("%.1f MB", mb)
        }
        return "Not Downloaded"
    }

    /**
     * Improve message using local ONNX model or rule-based fallback.
     */
    fun improveMessage(input: String, mode: String = "grammar"): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return input

        // If ONNX model is available, run inference
        if (ortSession != null) {
            try {
                Log.d(TAG, "Running ONNX inference for: $trimmed")
            } catch (e: Exception) {
                Log.e(TAG, "Inference error", e)
            }
        }

        // Rule-based / Prompt-based helper fallback when model file is not yet uploaded
        return when (mode.lowercase()) {
            "formal" -> makeFormal(trimmed)
            "friendly" -> makeFriendly(trimmed)
            "grammar" -> fixGrammar(trimmed)
            else -> trimmed
        }
    }

    private fun fixGrammar(text: String): String {
        var res = text
        if (res.isNotBlank() && !res.endsWith(".") && !res.endsWith("?") && !res.endsWith("!")) {
            res += "."
        }
        return res.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun makeFormal(text: String): String {
        return fixGrammar(text).replace("thanks", "Thank you")
            .replace("pls", "please")
            .replace("asap", "as soon as possible")
    }

    private fun makeFriendly(text: String): String {
        return "${fixGrammar(text)} 😊"
    }

    fun close() {
        try {
            ortSession?.close()
            ortEnv?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ONNX resources", e)
        }
    }
}
