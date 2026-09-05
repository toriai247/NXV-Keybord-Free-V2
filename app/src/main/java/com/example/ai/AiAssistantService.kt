package com.example.ai

import com.example.data.preferences.KeyboardPreferences

enum class AiTone {
    PROFESSIONAL,
    FRIENDLY,
    SHORT,
    CASUAL,
    ROMANTIC,
    POETIC
}

data class AiAiResult(
    val outputText: String,
    val tokensUsed: Int = 0,
    val isSuccess: Boolean = true,
    val error: String? = null
)

interface KeyboardAiService {
    suspend fun fixGrammarAndPolish(text: String, userKey: String, model: String): AiAiResult
    suspend fun suggestReplies(message: String, userKey: String, model: String): List<String>
    suspend fun generatePoetry(topicOrText: String, userKey: String, model: String): AiAiResult
    suspend fun polishVoiceInput(voiceText: String, userKey: String, model: String): AiAiResult
    suspend fun generateCustom(prompt: String, userKey: String, model: String): AiAiResult
}

class GeminiKeyboardAiService(
    private val preferences: KeyboardPreferences? = null
) : KeyboardAiService {

    private val systemInstruction = """
        You are an intelligent, friendly AI Typing & Writing Assistant for the NXV Bangla Keyboard app.
        Your job is to assist users in writing, correcting text, suggesting smart replies, writing poems/rhymes (কবিতা/ছন্দ), polishing voice inputs, and generating creative content.
        Respond in the same language as the input (Bangla, Banglish, or English).
        Keep responses concise, natural, and directly usable as keyboard output unless instructed otherwise.
    """.trimIndent()

    override suspend fun fixGrammarAndPolish(text: String, userKey: String, model: String): AiAiResult {
        if (text.isBlank()) return AiAiResult("", 0, false, "Input text is empty")

        val prompt = "Please correct all spelling, grammar, punctuation, and style issues in the following text. Make it natural and clear. Output ONLY the polished text without quotes or explanations:\n\n$text"
        val response = GeminiApiClient.generateContent(
            prompt = prompt,
            userApiKey = userKey,
            selectedModel = model,
            systemInstruction = systemInstruction
        )

        if (response.isSuccess && preferences != null && response.totalTokens > 0) {
            preferences.addTokensUsed(response.totalTokens.toLong())
        }

        return AiAiResult(
            outputText = response.text,
            tokensUsed = response.totalTokens,
            isSuccess = response.isSuccess,
            error = response.errorMessage
        )
    }

    override suspend fun suggestReplies(message: String, userKey: String, model: String): List<String> {
        if (message.isBlank()) return listOf("ধন্যবাদ! 😊", "আচ্ছা ঠিক আছে 👍", "পরে কথা হবে 👋")

        val prompt = """
            Given this incoming message: "$message"
            Generate 3 short, natural, friendly reply options for chat (1 in Bangla, 1 in Banglish, 1 in English).
            Format as a JSON array of strings, e.g. ["হ্যাঁ ভাই, খবর ভালো!", "Thik ache, ekhoni aschi!", "Sure, thanks!"]
            Output ONLY raw JSON array.
        """.trimIndent()

        val response = GeminiApiClient.generateContent(
            prompt = prompt,
            userApiKey = userKey,
            selectedModel = model,
            systemInstruction = systemInstruction
        )

        if (response.isSuccess && preferences != null && response.totalTokens > 0) {
            preferences.addTokensUsed(response.totalTokens.toLong())
        }

        if (response.isSuccess && response.text.isNotBlank()) {
            try {
                val cleanJson = response.text
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                val jsonArray = org.json.JSONArray(cleanJson)
                val list = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonArray.getString(i))
                }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                // Fallback splitting lines
                val lines = response.text.lines().map { it.replace(Regex("^[-*\\d.]+\\s*"), "").trim() }.filter { it.isNotBlank() }
                if (lines.isNotEmpty()) return lines.take(3)
            }
        }

        return listOf("হ্যাঁ, ঠিক আছে! 😊", "একটু পর কথা বলছি 👍", "Thanks a lot! ❤️")
    }

    override suspend fun generatePoetry(topicOrText: String, userKey: String, model: String): AiAiResult {
        val topic = if (topicOrText.isBlank()) "ভালোবাসা ও জীবন" else topicOrText
        val prompt = "Write a beautiful 4 to 8 line poem or rhyming verse (সুন্দর কবিতা/ছন্দ) in Bengali/Banglish about: $topic. Make it sweet, rhythmic, and poetic."

        val response = GeminiApiClient.generateContent(
            prompt = prompt,
            userApiKey = userKey,
            selectedModel = model,
            systemInstruction = systemInstruction
        )

        if (response.isSuccess && preferences != null && response.totalTokens > 0) {
            preferences.addTokensUsed(response.totalTokens.toLong())
        }

        return AiAiResult(
            outputText = response.text,
            tokensUsed = response.totalTokens,
            isSuccess = response.isSuccess,
            error = response.errorMessage
        )
    }

    override suspend fun polishVoiceInput(voiceText: String, userKey: String, model: String): AiAiResult {
        if (voiceText.isBlank()) return AiAiResult("", 0, false, "Voice text is empty")

        val prompt = "The following is raw spoken text from voice typing. Clean up stutters, fix misrecognized Bangla/English words, add proper punctuation, and format it smoothly:\n\n$voiceText"

        val response = GeminiApiClient.generateContent(
            prompt = prompt,
            userApiKey = userKey,
            selectedModel = model,
            systemInstruction = systemInstruction
        )

        if (response.isSuccess && preferences != null && response.totalTokens > 0) {
            preferences.addTokensUsed(response.totalTokens.toLong())
        }

        return AiAiResult(
            outputText = response.text,
            tokensUsed = response.totalTokens,
            isSuccess = response.isSuccess,
            error = response.errorMessage
        )
    }

    override suspend fun generateCustom(prompt: String, userKey: String, model: String): AiAiResult {
        if (prompt.isBlank()) return AiAiResult("", 0, false, "Prompt is empty")

        val response = GeminiApiClient.generateContent(
            prompt = prompt,
            userApiKey = userKey,
            selectedModel = model,
            systemInstruction = systemInstruction
        )

        if (response.isSuccess && preferences != null && response.totalTokens > 0) {
            preferences.addTokensUsed(response.totalTokens.toLong())
        }

        return AiAiResult(
            outputText = response.text,
            tokensUsed = response.totalTokens,
            isSuccess = response.isSuccess,
            error = response.errorMessage
        )
    }
}
