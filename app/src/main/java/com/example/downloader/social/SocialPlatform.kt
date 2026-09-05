package com.example.downloader.social

import java.text.NumberFormat
import java.util.Locale

enum class SocialPlatform(val displayName: String, val badgeColorHex: Long) {
    TIKTOK("TikTok", 0xFFFE2C55),
    FACEBOOK("Facebook", 0xFF1877F2),
    UNKNOWN("Link", 0xFF6200EE)
}

data class SocialMediaMetadata(
    val platform: SocialPlatform,
    val sourceUrl: String,
    val title: String,
    val authorName: String? = null,
    val authorUsername: String? = null,
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    val sharesCount: Long = 0,
    val durationSeconds: Int = 0,
    val musicTitle: String? = null,
    val thumbnailUrl: String? = null,
    val downloadHdUrl: String? = null,
    val downloadSdUrl: String? = null,
    val downloadAudioUrl: String? = null
) {
    /**
     * Formats the extracted metadata into a beautiful, shareable text summary
     * ready to be copied or pasted directly into any messaging app.
     */
    fun formatShareableSummary(): String {
        val sb = StringBuilder()
        when (platform) {
            SocialPlatform.TIKTOK -> {
                sb.appendLine("🎵 [TikTok Video Card]")
            }
            SocialPlatform.FACEBOOK -> {
                sb.appendLine("📘 [Facebook Video Card]")
            }
            else -> {
                sb.appendLine("🎬 [Video Card]")
            }
        }

        if (title.isNotBlank()) {
            sb.appendLine("📌 $title")
        }

        if (!authorName.isNullOrBlank() || !authorUsername.isNullOrBlank()) {
            val authorDisplay = buildString {
                if (!authorName.isNullOrBlank()) append(authorName)
                if (!authorUsername.isNullOrBlank()) {
                    if (isNotEmpty()) append(" (@${authorUsername.removePrefix("@")})")
                    else append("@${authorUsername.removePrefix("@")}")
                }
            }
            sb.appendLine("👤 Creator: $authorDisplay")
        }

        val stats = mutableListOf<String>()
        if (likesCount > 0) stats.add("❤️ ${formatMetric(likesCount)} Likes")
        if (commentsCount > 0) stats.add("💬 ${formatMetric(commentsCount)} Comments")
        if (sharesCount > 0) stats.add("🔄 ${formatMetric(sharesCount)} Shares")
        if (durationSeconds > 0) {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            stats.add("⏱️ ${String.format(Locale.US, "%d:%02d", mins, secs)}")
        }

        if (stats.isNotEmpty()) {
            sb.appendLine("📊 " + stats.joinToString(" • "))
        }

        if (!musicTitle.isNullOrBlank()) {
            sb.appendLine("🎶 Sound: $musicTitle")
        }

        sb.append("🔗 Link: $sourceUrl")
        return sb.toString().trim()
    }

    fun formatCompactSummary(): String {
        val author = if (!authorUsername.isNullOrBlank()) "@${authorUsername.removePrefix("@")}" else authorName ?: platform.displayName
        val cleanTitle = title.take(60)
        return "🎬 $cleanTitle (by $author)\n🔗 $sourceUrl"
    }

    private fun formatMetric(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> NumberFormat.getNumberInstance(Locale.US).format(count)
        }
    }
}
