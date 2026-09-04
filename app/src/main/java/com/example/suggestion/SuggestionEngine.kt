package com.example.suggestion

import android.content.Context
import com.example.data.KeyboardDataRepository
import com.example.dictionary.AndroidUserDictionary
import com.example.language.AutocorrectEngine
import com.example.language.NextWordPredictor
import com.example.language.avro.AvroPhoneticEngine
import com.example.language.bangla.BanglaLayouts
import com.example.language.english.EnglishEngine

data class SuggestionItem(
    val displayText: String,
    val replacementText: String,
    val isPrimary: Boolean = false,
    val isShortcut: Boolean = false,
    val isAutocorrect: Boolean = false,
    val isPrediction: Boolean = false
)

class SuggestionEngine(
    private val repository: KeyboardDataRepository,
    context: Context? = null
) {
    private val avroEngine = AvroPhoneticEngine()
    private val englishEngine = EnglishEngine()
    val autocorrectEngine = AutocorrectEngine()
    val nextWordPredictor = NextWordPredictor()
    private val androidUserDictionary = context?.let { AndroidUserDictionary(it) }

    suspend fun getSuggestions(
        currentWord: String,
        mode: String, // "english", "bangla", "avro"
        contextTextBeforeCursor: String = "",
        isPasswordField: Boolean = false,
        autoCorrectEnabled: Boolean = true
    ): List<SuggestionItem> {
        if (isPasswordField) {
            return emptyList()
        }

        val trimmed = currentWord.trim()

        // When currentWord is empty, provide context-aware next-word predictions
        if (trimmed.isEmpty()) {
            if (contextTextBeforeCursor.isNotBlank()) {
                val predictions = nextWordPredictor.getNextWordPredictions(
                    contextTextBeforeCursor = contextTextBeforeCursor,
                    mode = mode,
                    maxCount = 4
                )
                if (predictions.isNotEmpty()) {
                    return predictions.mapIndexed { index, word ->
                        SuggestionItem(
                            displayText = word,
                            replacementText = word,
                            isPrimary = index == 0,
                            isPrediction = true
                        )
                    }
                }
            }
            return getDefaultSuggestions(mode)
        }

        val results = mutableListOf<SuggestionItem>()

        // 1. Check for Text Shortcuts
        val shortcutExpansion = repository.getShortcutExpansion(trimmed)
        if (shortcutExpansion != null) {
            results.add(
                SuggestionItem(
                    displayText = "⚡ $shortcutExpansion",
                    replacementText = shortcutExpansion,
                    isPrimary = true,
                    isShortcut = true
                )
            )
        }

        // 2. Check for Autocorrection match
        if (autoCorrectEnabled) {
            val correction = autocorrectEngine.getCorrection(trimmed, mode)
            if (correction != null && !correction.equals(trimmed, ignoreCase = true)) {
                results.add(
                    SuggestionItem(
                        displayText = correction,
                        replacementText = correction,
                        isPrimary = results.none { it.isShortcut },
                        isAutocorrect = true
                    )
                )
            }
        }

        when (mode.lowercase()) {
            "avro" -> {
                // 1. Primary candidate from Avro phonetic engine
                val candidates = avroEngine.getCandidates(trimmed, maxCandidates = 5)
                val primaryCandidate = candidates.firstOrNull()
                if (primaryCandidate != null && results.none { it.replacementText == primaryCandidate }) {
                    results.add(
                        SuggestionItem(
                            displayText = primaryCandidate,
                            replacementText = primaryCandidate,
                            isPrimary = !results.any { it.isShortcut || it.isAutocorrect }
                        )
                    )
                }

                // 2. User learned words from local Room database (high priority as user values learned words)
                val learned = repository.getWordSuggestions(trimmed, "bn")
                learned.take(2).forEach { word ->
                    if (results.none { it.replacementText == word }) {
                        results.add(SuggestionItem(displayText = word, replacementText = word))
                    }
                }

                // 3. Remaining Avro candidate matches
                candidates.drop(1).forEach { candidate ->
                    if (results.none { it.replacementText == candidate }) {
                        results.add(
                            SuggestionItem(
                                displayText = candidate,
                                replacementText = candidate,
                                isPrimary = false
                            )
                        )
                    }
                }

                // 4. Frequent word prefix suggestions from Avro engine
                val frequent = avroEngine.getFrequentWordSuggestions(trimmed, maxCount = 3)
                frequent.forEach { word ->
                    if (results.none { it.replacementText == word }) {
                        results.add(SuggestionItem(displayText = word, replacementText = word))
                    }
                }

                // 5. Mixed-language candidate support (e.g. "Facebook", "meeting", "post", "phone")
                val mixedCandidates = nextWordPredictor.getMixedEnglishCandidates(trimmed, maxCount = 2)
                for (mixed in mixedCandidates) {
                    if (results.none { it.replacementText.equals(mixed, ignoreCase = true) }) {
                        results.add(
                            SuggestionItem(
                                displayText = mixed,
                                replacementText = mixed,
                                isPrimary = false
                            )
                        )
                    }
                }
            }

            "bangla" -> {
                // 1. User learned words from Room database
                val learned = repository.getWordSuggestions(trimmed, "bn")
                learned.take(2).forEach { word ->
                    if (results.none { it.replacementText == word }) {
                        results.add(
                            SuggestionItem(
                                displayText = word,
                                replacementText = word,
                                isPrimary = results.none { it.isShortcut || it.isAutocorrect }
                            )
                        )
                    }
                }

                // 2. 1,200+ Bangla Dictionary prefix matching
                val matching = BanglaLayouts.COMMON_WORDS
                    .filter { it.startsWith(trimmed) && it != trimmed }
                    .take(5)
                matching.forEachIndexed { index, word ->
                    if (results.none { it.replacementText == word }) {
                        results.add(
                            SuggestionItem(
                                displayText = word,
                                replacementText = word,
                                isPrimary = index == 0 && results.none { it.isShortcut || it.isAutocorrect || it.isPrimary }
                            )
                        )
                    }
                }
            }

            else -> {
                // 1. User learned words from Room database
                val learned = repository.getWordSuggestions(trimmed, "en")
                learned.take(2).forEach { word ->
                    if (results.none { it.replacementText == word }) {
                        results.add(
                            SuggestionItem(
                                displayText = word,
                                replacementText = word,
                                isPrimary = results.none { it.isShortcut || it.isAutocorrect }
                            )
                        )
                    }
                }

                // 2. English & Banglish Dictionary matches (1,200+ English + 1,000+ Banglish words)
                val englishWords = englishEngine.getSuggestions(trimmed, maxCount = 5)
                englishWords.forEachIndexed { index, word ->
                    if (results.none { it.replacementText.equals(word, ignoreCase = true) }) {
                        results.add(
                            SuggestionItem(
                                displayText = word,
                                replacementText = word,
                                isPrimary = index == 0 && results.none { it.isShortcut || it.isAutocorrect || it.isPrimary }
                            )
                        )
                    }
                }

                // 3. Query Android system User Dictionary if available
                androidUserDictionary?.let { aud ->
                    val userWords = aud.queryWords(trimmed, maxCount = 2)
                    for (uw in userWords) {
                        if (results.none { it.replacementText.equals(uw, ignoreCase = true) }) {
                            results.add(SuggestionItem(displayText = uw, replacementText = uw))
                        }
                    }
                }
            }
        }

        // Return up to 5 best ranked, non-duplicated suggestions
        return results.distinctBy { it.replacementText }.take(5)
    }

    private fun getDefaultSuggestions(mode: String): List<SuggestionItem> {
        return when (mode.lowercase()) {
            "avro" -> listOf(
                SuggestionItem("আমি", "আমি"),
                SuggestionItem("তুমি", "তুমি"),
                SuggestionItem("কেমন", "কেমন"),
                SuggestionItem("ভালো", "ভালো")
            )
            "bangla" -> listOf(
                SuggestionItem("বাংলাদেশ", "বাংলাদেশ"),
                SuggestionItem("ধন্যবাদ", "ধন্যবাদ"),
                SuggestionItem("কেমন", "কেমন"),
                SuggestionItem("আছো", "আছো")
            )
            else -> listOf(
                SuggestionItem("Hello", "Hello"),
                SuggestionItem("Thanks", "Thanks"),
                SuggestionItem("How", "How"),
                SuggestionItem("Hope", "Hope")
            )
        }
    }
}
