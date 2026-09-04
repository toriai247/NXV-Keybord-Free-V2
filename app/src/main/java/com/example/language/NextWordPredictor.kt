package com.example.language

import java.util.concurrent.ConcurrentHashMap

class NextWordPredictor {

    companion object {
        // Pre-indexed bigram/trigram models for Bangla and English
        private val BANGLA_PREDICTIONS = mapOf(
            "আমি আজ" to listOf("যাবো", "যাব", "বাড়িতে", "বাসায়", "করবো"),
            "আমি" to listOf("আজ", "ভালো", "আছি", "বাংলায়", "তোমাকে", "যাবো"),
            "তুমি কেমন" to listOf("আছো", "আছিস", "আছেন"),
            "তুমি" to listOf("কেমন", "আছো", "কোথায়", "কি", "যাবে"),
            "আপনি কেমন" to listOf("আছেন"),
            "আপনি" to listOf("কেমন", "আছেন", "কোথায়", "যাবেন"),
            "আজকে" to listOf("meeting", "ছুটি", "হবে", "বৃষ্টি", "যাবো"),
            "আমার নাম" to listOf("NXV", "বাংলাদেশ"),
            "আমার" to listOf("নাম", "ফোন", "বাড়ি", "বন্ধু", "দেশ"),
            "অনেক ধন্যবাদ" to listOf("আপনাকে", "ভাই", "তোমাকে"),
            "অনেক" to listOf("ধন্যবাদ", "ভালো", "সুন্দর", "দিন"),
            "কেমন" to listOf("আছো", "আছেন", "লাগল"),
            "ভালো" to listOf("আছি", "থেকো", "হয়েছে", "বাসি"),
            "কী" to listOf("করছো", "হয়েছে", "খবর", "ব্যাপার"),
            "কি" to listOf("করছো", "হয়েছে", "খবর", "করছেন"),
            "কোথায়" to listOf("আছো", "যাবে", "তুমি"),
            "শুভ জন্মদিন" to listOf("তোমাকে", "ভাই", "বন্ধু"),
            "শুভ" to listOf("সকাল", "রাত্রি", "জন্মদিন", "কামনা"),
            "ধন্যবাদ" to listOf("আপনাকে", "তোমাকে", "ভাই"),
            "বাংলাদেশ" to listOf("আমার", "জিন্দাবাদ", "ক্রিকেট")
        )

        private val ENGLISH_PREDICTIONS = mapOf(
            "how are" to listOf("you", "things", "we"),
            "how" to listOf("are", "is", "can", "about"),
            "thank you" to listOf("so", "very", "much"),
            "thank" to listOf("you", "god"),
            "good morning" to listOf("everyone", "all", "have"),
            "good" to listOf("morning", "afternoon", "evening", "night", "job"),
            "i am" to listOf("good", "fine", "happy", "sorry", "here"),
            "i" to listOf("am", "will", "have", "want", "hope", "think"),
            "what are" to listOf("you", "they", "the"),
            "what" to listOf("are", "is", "about", "do", "happened"),
            "see you" to listOf("soon", "tomorrow", "later"),
            "see" to listOf("you", "all"),
            "let me" to listOf("know", "see", "think"),
            "let" to listOf("me", "us", "go"),
            "nice to" to listOf("meet", "see"),
            "nice" to listOf("to", "one", "meeting")
        )

        // Mixed-language common English nouns and verbs used inside Bangla
        val COMMON_MIXED_ENGLISH_WORDS = listOf(
            "Facebook", "post", "meeting", "phone", "office", "computer", "laptop",
            "mobile", "message", "email", "online", "internet", "video", "photo",
            "account", "password", "call", "link", "status", "app", "YouTube",
            "Google", "WiFi", "SIM", "school", "college", "university", "boss",
            "friend", "group", "class", "live", "game", "page", "code", "drive",
            "doc", "pdf", "bank", "hospital", "bus", "train", "doctor", "police"
        )
    }

    // Dynamic locally learned bigram map (word -> list of next words ordered by frequency)
    private val learnedBigrams = ConcurrentHashMap<String, MutableMap<String, Int>>()

    fun recordBigram(prevWord: String?, currentWord: String) {
        if (prevWord == null) return
        val p = prevWord.trim().lowercase()
        val c = currentWord.trim()
        if (p.isEmpty() || c.isEmpty() || p.length > 30 || c.length > 30) return

        val map = learnedBigrams.getOrPut(p) { ConcurrentHashMap() }
        map[c] = (map[c] ?: 0) + 1
    }

    fun getNextWordPredictions(
        contextTextBeforeCursor: String,
        mode: String,
        maxCount: Int = 4
    ): List<String> {
        val trimmedContext = contextTextBeforeCursor.trim()
        if (trimmedContext.isEmpty()) return emptyList()

        // Extract last 1 and 2 words from context
        val words = trimmedContext.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        val lastWord = words.last()
        val twoWords = if (words.size >= 2) "${words[words.size - 2]} $lastWord" else ""

        val results = mutableListOf<String>()

        // 1. Check learned bigrams first for highest personal personalization
        val learned = learnedBigrams[lastWord.lowercase()]
        if (learned != null) {
            val sorted = learned.entries.sortedByDescending { it.value }.map { it.key }
            results.addAll(sorted.take(maxCount))
        }

        val isBanglaMode = mode.lowercase() == "bangla" || mode.lowercase() == "avro"

        if (isBanglaMode) {
            // Check 2-word match first
            if (twoWords.isNotEmpty()) {
                BANGLA_PREDICTIONS[twoWords]?.let { matches ->
                    for (m in matches) {
                        if (!results.contains(m)) results.add(m)
                    }
                }
            }
            // Check 1-word match
            BANGLA_PREDICTIONS[lastWord]?.let { matches ->
                for (m in matches) {
                    if (!results.contains(m)) results.add(m)
                }
            }
        } else {
            // English predictions
            if (twoWords.isNotEmpty()) {
                ENGLISH_PREDICTIONS[twoWords.lowercase()]?.let { matches ->
                    for (m in matches) {
                        if (!results.contains(m)) results.add(m)
                    }
                }
            }
            ENGLISH_PREDICTIONS[lastWord.lowercase()]?.let { matches ->
                for (m in matches) {
                    if (!results.contains(m)) results.add(m)
                }
            }
        }

        return results.take(maxCount)
    }

    fun getMixedEnglishCandidates(prefix: String, maxCount: Int = 2): List<String> {
        if (prefix.isBlank()) return emptyList()
        val lower = prefix.lowercase()
        return COMMON_MIXED_ENGLISH_WORDS
            .filter { it.lowercase().startsWith(lower) }
            .take(maxCount)
    }
}
