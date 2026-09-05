package com.example.language

import com.example.language.dictionary.BanglaDictionary
import com.example.language.dictionary.BanglishDictionary
import com.example.language.dictionary.EnglishDictionary
import kotlin.math.abs

class AutocorrectEngine {

    companion object {
        private val ENGLISH_CORRECTIONS = mapOf(
            // User requested app names & Islamic phrases autocorrect
            "whatsap" to "WhatsApp",
            "watsapp" to "WhatsApp",
            "wasap" to "WhatsApp",
            "whatsapp" to "WhatsApp",
            "messengar" to "Messenger",
            "messanger" to "Messenger",
            "mesanger" to "Messenger",
            "masenger" to "Messenger",
            "teligram" to "Telegram",
            "telegram" to "Telegram",
            "facebok" to "Facebook",
            "facbook" to "Facebook",
            "fbook" to "Facebook",
            "instgram" to "Instagram",
            "ytube" to "YouTube",
            "yutube" to "YouTube",
            "tiktak" to "TikTok",
            "toktik" to "TikTok",
            "bkash" to "bKash",
            "nogod" to "Nagad",

            // Love & Relationship terms with emojis
            "lve" to "love ❤️",
            "lov" to "love ❤️",
            "luvu" to "love you ❤️",
            "lvu" to "love you ❤️",
            "lv u" to "love you ❤️",
            "loveu" to "love you ❤️",
            "loveyou" to "love you ❤️",
            "loveyousomuch" to "love you so much ❤️",
            "missu" to "miss you 😘",
            "misu" to "miss you 😘",
            "missyou" to "miss you 😘",
            "ummah" to "ummah 😘",
            "umah" to "ummah 😘",
            "umaah" to "ummah 😘",
            "ummaah" to "ummah 😘",
            "uma" to "ummah 😘",
            "muah" to "muah 😘",
            "mwah" to "mwah 😘",
            "kissu" to "kiss you 😘",
            "kisu" to "kiss you 😘",
            "kis" to "kiss 😘",
            "hugu" to "hug you 🤗",
            "loveyaa" to "love ya 🥰",
            "janu" to "জানু ❤️",
            "janumoni" to "জানুমণি ❤️",
            "sonamoni" to "সোনামণি 🥰",
            "kolijar" to "কলিজার টুকরা ❤️",

            // Islamic greetings & phrases
            "allhamdullilah" to "Alhamdulillah",
            "alhamdullilah" to "Alhamdulillah",
            "alhamdilillah" to "Alhamdulillah",
            "alhamdullila" to "Alhamdulillah",
            "alhamdulillah" to "Alhamdulillah",
            "assalamulaykum" to "Assalamu Alaikum",
            "assalamualaikum" to "Assalamu Alaikum",
            "assalamu alaykum" to "Assalamu Alaikum",
            "assalamulaikum" to "Assalamu Alaikum",
            "assalamu" to "Assalamu Alaikum",
            "walaykum assalam" to "Wa Alaikum Assalam",
            "walaikum assalam" to "Wa Alaikum Assalam",
            "walaikumassalam" to "Wa Alaikum Assalam",
            "walaykumassalam" to "Wa Alaikum Assalam",
            "walaykum" to "Wa Alaikum Assalam",
            "insallah" to "InshaAllah",
            "inshallah" to "InshaAllah",
            "inshaallah" to "InshaAllah",
            "mashaallah" to "MashaAllah",
            "mashallah" to "MashaAllah",
            "subhanallah" to "SubhanAllah",
            "subhan allah" to "SubhanAllah",
            "jazakallah" to "JazakAllah",
            "jazakallahkhair" to "JazakAllah Khair",
            "astagfirullah" to "Astaghfirullah",
            "astaghfirullah" to "Astaghfirullah",
            "bismillah" to "Bismillah",

            // User requested examples: lungage/languege -> language
            "lungage" to "language",
            "languege" to "language",
            "languge" to "language",
            "lunguage" to "language",
            "lenguage" to "language",

            // Common English misspellings & typos
            "teh" to "the",
            "adn" to "and",
            "nad" to "and",
            "youre" to "you're",
            "ur" to "your",
            "u" to "you",
            "r" to "are",
            "recieve" to "receive",
            "recive" to "receive",
            "seperate" to "separate",
            "definately" to "definitely",
            "definetly" to "definitely",
            "definitly" to "definitely",
            "wont" to "won't",
            "cant" to "can't",
            "dont" to "don't",
            "didnt" to "didn't",
            "isnt" to "isn't",
            "arent" to "aren't",
            "wasnt" to "wasn't",
            "werent" to "weren't",
            "hasnt" to "hasn't",
            "havent" to "haven't",
            "hadnt" to "hadn't",
            "couldnt" to "couldn't",
            "wouldnt" to "wouldn't",
            "shouldnt" to "shouldn't",
            "im" to "I'm",
            "ive" to "I've",
            "id" to "I'd",
            "ill" to "I'll",
            "thier" to "their",
            "freind" to "friend",
            "becuase" to "because",
            "becuse" to "because",
            "bcoz" to "because",
            "untill" to "until",
            "tommorow" to "tomorrow",
            "tomorow" to "tomorrow",
            "beleive" to "believe",
            "belive" to "believe",
            "alot" to "a lot",
            "wierd" to "weird",
            "goverment" to "government",
            "enviroment" to "environment",
            "calender" to "calendar",
            "occured" to "occurred",
            "neccessary" to "necessary",
            "necesary" to "necessary",
            "accommodate" to "accommodate",
            "acommodate" to "accommodate",
            "embarass" to "embarrass",
            "mornig" to "morning",
            "evning" to "evening",
            "awsome" to "awesome",
            "beutiful" to "beautiful",
            "peaple" to "people",
            "thnk" to "thank",
            "thnks" to "thanks",
            "welcom" to "welcome",
            "plz" to "please",
            "pls" to "please",
            "thx" to "thanks",
            "tq" to "thank you",
            "msg" to "message",
            "pic" to "picture"
        )

        private val BANGLA_CORRECTIONS = mapOf(
            "পাখী" to "পাখি",
            "বাড়ী" to "বাড়ি",
            "পোষ্ট" to "পোস্ট",
            "মাষ্টার" to "মাস্টার",
            "শ্রেণী" to "শ্রেণি",
            "ধণ্যবাদ" to "ধন্যবাদ",
            "পূজো" to "পুজো",
            "পুরষ্কার" to "পুরস্কার",
            "আবিস্কার" to "আবিষ্কার",
            "সূচী" to "সূচি",
            "ধরণ" to "ধরন",
            "শহীদ" to "শহিদ",
            "লাইব্রেরী" to "লাইব্রেরি",
            "একডেমী" to "একাডেমি",
            "ষ্টেশন" to "স্টেশন",
            "ষ্টাইল" to "স্টাইল",
            "পোষ্টার" to "পোস্টার",
            "ইন্টারন্যাশানাল" to "ইন্টারন্যাশনাল",
            "সরকারী" to "সরকারি",
            "দাবী" to "দাবি",
            "প্রতিযোগীতা" to "প্রতিযোগিতা",
            "সহযোগীতা" to "সহযোগিতা",
            "ভূল" to "ভুল",
            "খূব" to "খুব"
        )

        private val BANGLISH_CORRECTIONS = mapOf(
            "kivabe" to "কিভাবে",
            "kibhabe" to "কিভাবে",
            "valo" to "ভালো",
            "bhalo" to "ভালো",
            "kemon" to "কেমন",
            "kmne" to "কেমনে",
            "kire" to "কিরে",
            "kisu" to "কিছু",
            "kichu" to "কিছু",
            "koy" to "কই",
            "kothay" to "কোথায়",
            "kothai" to "কোথায়",
            "shobai" to "সবাই",
            "sobai" to "সবাই",
            "dhonnobad" to "ধন্যবাদ",
            "dhonnobaad" to "ধন্যবাদ",
            "thikase" to "ঠিক আছে",
            "thikache" to "ঠিক আছে",
            "plz" to "please",
            "pls" to "please",
            "thnx" to "thanks",
            "tq" to "thank you"
        )
    }

    /**
     * Compute Levenshtein distance between two strings with upper bound optimization.
     */
    private fun levenshtein(s1: String, s2: String, maxLimit: Int = 2): Int {
        if (abs(s1.length - s2.length) > maxLimit) return maxLimit + 1
        var prev = IntArray(s2.length + 1) { it }
        var curr = IntArray(s2.length + 1)

        for (i in 1..s1.length) {
            curr[0] = i
            var minInRow = curr[0]
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,
                    prev[j] + 1,
                    prev[j - 1] + cost
                )
                if (curr[j] < minInRow) minInRow = curr[j]
            }
            if (minInRow > maxLimit) return maxLimit + 1
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[s2.length]
    }

    /**
     * Check dictionary for close fuzzy match if user made a typo.
     */
    fun findFuzzyMatch(word: String, dictionary: List<String>, maxDistance: Int = 2): String? {
        val query = word.lowercase()
        if (query.length < 4) return null
        val firstChar = query.first()

        // Filter candidates starting with same first letter
        val candidates = dictionary.filter { it.isNotEmpty() && it.first().lowercaseChar() == firstChar }
        var bestMatch: String? = null
        var bestDist = maxDistance + 1

        for (cand in candidates) {
            if (cand.equals(query, ignoreCase = true)) return cand
            val dist = levenshtein(query, cand.lowercase(), maxDistance)
            if (dist < bestDist) {
                bestDist = dist
                bestMatch = cand
            }
        }
        return if (bestDist <= maxDistance) bestMatch else null
    }

    fun getCorrection(word: String, locale: String): String? {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return null

        return when (locale.lowercase()) {
            "bn", "bangla" -> {
                BANGLA_CORRECTIONS[trimmed]
                    ?: BANGLISH_CORRECTIONS[trimmed.lowercase()]
                    ?: findFuzzyMatch(trimmed, BanglaDictionary.WORDS, maxDistance = 1)
            }
            "avro" -> {
                BANGLISH_CORRECTIONS[trimmed.lowercase()]
                    ?: BanglishDictionary.BANGLISH_MAP[trimmed.lowercase()]
                    ?: BANGLA_CORRECTIONS[trimmed]
            }
            else -> {
                val lower = trimmed.lowercase()
                val direct = ENGLISH_CORRECTIONS[lower]
                val corrected = direct ?: run {
                    // Check Levenshtein fuzzy match against 1,200+ daily English words
                    val maxDist = if (lower.length >= 6) 2 else 1
                    findFuzzyMatch(lower, EnglishDictionary.WORDS, maxDistance = maxDist)
                }

                if (corrected != null && !corrected.equals(trimmed, ignoreCase = true)) {
                    if (trimmed.first().isUpperCase()) {
                        corrected.replaceFirstChar { it.uppercase() }
                    } else {
                        corrected
                    }
                } else null
            }
        }
    }
}

