package com.example.language.english

import com.example.language.dictionary.BanglishDictionary
import com.example.language.dictionary.EnglishDictionary

class EnglishEngine {

    companion object {
        val QWERTY_ROW_1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val QWERTY_ROW_2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val QWERTY_ROW_3 = listOf("z", "x", "c", "v", "b", "n", "m")

        val NUMBER_ROW = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

        val SYMBOL_ROW_1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val SYMBOL_ROW_2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
        val SYMBOL_ROW_3 = listOf("*", "\"", "'", ":", ";", "!", "?")

        val MORE_SYMBOLS_ROW_1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
        val MORE_SYMBOLS_ROW_2 = listOf("£", "€", "¥", "¢", "^", "°", "=", "{", "}", "\\")
        val MORE_SYMBOLS_ROW_3 = listOf("%", "©", "®", "™", "✓", "[", "]")

        // English high-frequency word dictionary for fast autocomplete
        val COMMON_ENGLISH_WORDS: List<String> = EnglishDictionary.WORDS
    }

    fun getSuggestions(prefix: String, maxCount: Int = 5): List<String> {
        val query = prefix.lowercase().trim()
        if (query.isEmpty()) return listOf("I", "The", "Hello", "Thanks", "How")

        // 1. First get matches from English Dictionary (1200+ words)
        val englishMatches = EnglishDictionary.getSuggestions(prefix, maxCount)

        // 2. Also check Banglish Roman matches (e.g. k -> ki, koro, koy; ko -> koro, kothay, koy; ki -> kire, kisu)
        val banglishMatches = BanglishDictionary.getBanglishRomanSuggestions(prefix, 3)

        val combined = LinkedHashSet<String>()
        // If user typed Roman Bengali prefix like 'k', 'ko', 'ki', prioritize common banglish items as requested
        if (query == "k" || query == "ko" || query == "ki" || query == "valo" || query == "bhalo" || query == "tumi" || query == "ami") {
            combined.addAll(banglishMatches)
            combined.addAll(englishMatches)
        } else {
            combined.addAll(englishMatches)
            combined.addAll(banglishMatches)
        }

        val result = combined.take(maxCount).toList()
        return if (prefix.isNotEmpty() && prefix.first().isUpperCase()) {
            result.map { it.replaceFirstChar { c -> c.uppercase() } }
        } else {
            result
        }
    }
}

