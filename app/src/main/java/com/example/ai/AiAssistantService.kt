package com.example.ai

/**
 * Modular AI Interface for future AI enhancements (Rewrite, Tone, Grammar, Translation).
 * Keeps keyboard core 100% offline and decoupled from cloud APIs.
 */
interface KeyboardAiService {
    suspend fun rewrite(text: String, tone: AiTone): Result<String>
    suspend fun fixGrammar(text: String): Result<String>
    suspend fun translate(text: String, targetLanguage: String): Result<String>
    suspend fun completeSentence(prefix: String): Result<List<String>>
}

enum class AiTone {
    PROFESSIONAL,
    FRIENDLY,
    SHORT,
    CASUAL
}

/**
 * Default offline implementation providing safe local heuristics.
 */
class LocalRuleBasedAiService : KeyboardAiService {
    override suspend fun rewrite(text: String, tone: AiTone): Result<String> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return Result.success("")

        val result = when (tone) {
            AiTone.PROFESSIONAL -> {
                // Ensure proper punctuation and tone
                var out = trimmed
                if (!out.endsWith("।") && !out.endsWith(".")) {
                    out += if (out.any { it in '\u0980'..'\u09FF' }) "।" else "."
                }
                out
            }
            AiTone.FRIENDLY -> {
                "$trimmed 😊"
            }
            AiTone.SHORT -> {
                trimmed.split(" ").take(4).joinToString(" ")
            }
            AiTone.CASUAL -> {
                trimmed.replace("।", "!").replace(".", "!")
            }
        }
        return Result.success(result)
    }

    override suspend fun fixGrammar(text: String): Result<String> {
        val clean = text.trim().replace(Regex("\\s+"), " ")
        return Result.success(clean)
    }

    override suspend fun translate(text: String, targetLanguage: String): Result<String> {
        return Result.success(text)
    }

    override suspend fun completeSentence(prefix: String): Result<List<String>> {
        return Result.success(emptyList())
    }
}
