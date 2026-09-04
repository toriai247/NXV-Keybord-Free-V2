package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ClipboardUtils {
    /**
     * Formats a millisecond timestamp into a user-friendly relative time string.
     */
    fun formatRelativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - timestamp
        if (diff < 0) return "Just now"
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 45 -> "Just now"
            minutes < 60 -> "${minutes.coerceAtLeast(1)}m ago"
            hours < 24 -> "${hours}h ago"
            days == 1L -> "Yesterday"
            days < 7 -> "${days}d ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    /**
     * Returns a concise character and word count summary for a text fragment.
     */
    fun getWordAndCharCount(text: String): String {
        val chars = text.length
        val words = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        return "$chars chars • $words ${if (words == 1) "word" else "words"}"
    }
}
