package com.example.language.bangla

import com.example.language.dictionary.BanglaDictionary

object BanglaLayouts {

    // Normal mode keys for native Bangla typing
    val NORMAL_ROWS = listOf(
        listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ"),
        listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন"),
        listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ"),
        listOf("স", "হ", "ড়", "ঢ়", "য়", "্", "া", "ি", "ী", "ু")
    )

    // Shift mode keys (Independent vowels, remaining Kar, symbols, modifiers)
    val SHIFT_ROWS = listOf(
        listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও"),
        listOf("ঔ", "ূ", "ৃ", "ে", "ৈ", "ো", "ৌ", "ং", "ঃ", "ঁ"),
        listOf("ৎ", "্", "।", "‘", "’", "“", "”", "—", "…", "৳"),
        listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")
    )

    // Bangla Digits
    val BANGLA_DIGITS = listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯")

    // Common Bangla high-frequency suggestion words (1,200+ words)
    val COMMON_WORDS: List<String> = BanglaDictionary.WORDS
}

