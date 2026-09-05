package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiResponse(
    val text: String,
    val promptTokens: Int = 0,
    val candidateTokens: Int = 0,
    val totalTokens: Int = 0,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

object GeminiApiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Resolves model alias to supported API model string.
     */
    fun resolveModelName(selectedModel: String): String {
        return when (selectedModel.lowercase()) {
            "gemini-1.5-pro", "pro" -> "gemini-1.5-pro"
            "gemini-1.5-flash-8b", "gemini-1.5-flash-lite", "lite", "8b" -> "gemini-1.5-flash-8b"
            else -> "gemini-1.5-flash"
        }
    }

    suspend fun generateContent(
        prompt: String,
        userApiKey: String? = null,
        selectedModel: String = "gemini-1.5-flash",
        systemInstruction: String? = null
    ): GeminiResponse = withContext(Dispatchers.IO) {
        // Resolve key: user key -> BuildConfig key (safely accessed via reflection)
        val apiKey = when {
            !userApiKey.isNullOrBlank() -> userApiKey.trim()
            else -> {
                try {
                    val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                    val value = field.get(null) as? String
                    if (!value.isNullOrBlank() && value != "null" && value != "dummy") value else null
                } catch (_: Exception) {
                    null
                }
            }
        }

        if (apiKey.isNullOrBlank()) {
            return@withContext GeminiResponse(
                text = "",
                isSuccess = false,
                errorMessage = "Gemini API Key missing! Please enter your free Gemini API Key in Keyboard Settings or AI panel."
            )
        }

        val modelName = resolveModelName(selectedModel)
        val url = "$BASE_URL$modelName:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject()

            // System Instruction if provided
            if (!systemInstruction.isNullOrBlank()) {
                val sysPart = JSONObject().put("text", systemInstruction)
                val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
                rootJson.put("systemInstruction", sysContent)
            }

            // User Prompt Parts
            val userPart = JSONObject().put("text", prompt)
            val userContent = JSONObject().put("parts", JSONArray().put(userPart))
            val contentsArray = JSONArray().put(userContent)
            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().put("temperature", 0.7)
            rootJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorJson = try { JSONObject(responseString) } catch (e: Exception) { null }
                val errMessage = errorJson?.optJSONObject("error")?.optString("message") 
                    ?: "HTTP Error ${response.code}: ${response.message}"
                return@withContext GeminiResponse(
                    text = "",
                    isSuccess = false,
                    errorMessage = errMessage
                )
            }

            val jsonObject = JSONObject(responseString)
            val candidates = jsonObject.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val partsArray = contentObj?.optJSONArray("parts")

            val sb = StringBuilder()
            if (partsArray != null) {
                for (i in 0 until partsArray.length()) {
                    val part = partsArray.optJSONObject(i)
                    val txt = part?.optString("text")
                    if (!txt.isNullOrEmpty()) {
                        sb.append(txt)
                    }
                }
            }

            val textResult = sb.toString().trim()

            // Extract Usage Metadata or estimate tokens
            val usageMeta = jsonObject.optJSONObject("usageMetadata")
            val pTokens = usageMeta?.optInt("promptTokenCount") ?: (prompt.length / 4 + 10)
            val cTokens = usageMeta?.optInt("candidatesTokenCount") ?: (textResult.length / 4 + 10)
            val tTokens = usageMeta?.optInt("totalTokenCount") ?: (pTokens + cTokens)

            return@withContext GeminiResponse(
                text = textResult,
                promptTokens = pTokens,
                candidateTokens = cTokens,
                totalTokens = tTokens,
                isSuccess = true
            )

        } catch (e: Exception) {
            return@withContext GeminiResponse(
                text = "",
                isSuccess = false,
                errorMessage = "Network error: ${e.localizedMessage ?: "Failed to connect to Gemini API"}"
            )
        }
    }
}
