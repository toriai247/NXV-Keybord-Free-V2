package com.example.downloader.social

import android.util.Log
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLDecoder
import java.util.regex.Pattern

object SocialMediaExtractor {
    private const val TAG = "SocialMediaExtractor"

    val TIKTOK_PATTERN = Pattern.compile(
        "https?://(?:(?:www|vm|vt|m|t)\\.)?tiktok\\.com/[^\\s\"'<>]+",
        Pattern.CASE_INSENSITIVE
    )

    val FACEBOOK_PATTERN = Pattern.compile(
        "https?://(?:(?:www|m|web|mbasic|fb)\\.)?(?:facebook\\.com|fb\\.watch|fb\\.me|fb\\.gg)/(?:reel/|watch/?\\?v=|videos/|story\\.php|share/(?:r|v|p)/|[^/]+/videos/|[^/]+/posts/|groups/[^/]+/permalink/|[A-Za-z0-9_.-]+)[^\\s\"'<>]*",
        Pattern.CASE_INSENSITIVE
    )

    fun detectPlatform(url: String): SocialPlatform {
        return when {
            TIKTOK_PATTERN.matcher(url).find() -> SocialPlatform.TIKTOK
            FACEBOOK_PATTERN.matcher(url).find() -> SocialPlatform.FACEBOOK
            else -> SocialPlatform.UNKNOWN
        }
    }

    fun extractSocialUrl(text: String?): Pair<String, SocialPlatform>? {
        if (text.isNullOrBlank()) return null
        val ttMatcher = TIKTOK_PATTERN.matcher(text)
        if (ttMatcher.find()) {
            return Pair(ttMatcher.group(0), SocialPlatform.TIKTOK)
        }
        val fbMatcher = FACEBOOK_PATTERN.matcher(text)
        if (fbMatcher.find()) {
            val fbUrl = fbMatcher.group(0)
            // Filter out generic non-video pages if needed
            if (fbUrl.contains("facebook.com") || fbUrl.contains("fb.watch") || fbUrl.contains("fb.me")) {
                return Pair(fbUrl, SocialPlatform.FACEBOOK)
            }
        }
        return null
    }

    /**
     * Resolves metadata and download URLs for TikTok or Facebook.
     */
    suspend fun resolveMedia(client: OkHttpClient, url: String, platform: SocialPlatform): SocialMediaMetadata? {
        return when (platform) {
            SocialPlatform.TIKTOK -> resolveTikTok(client, url)
            SocialPlatform.FACEBOOK -> resolveFacebook(client, url)
            SocialPlatform.UNKNOWN -> {
                // Try TikTok then Facebook
                resolveTikTok(client, url) ?: resolveFacebook(client, url)
            }
        }
    }

    private fun resolveTikTok(client: OkHttpClient, tiktokUrl: String): SocialMediaMetadata? {
        val endpoints = listOf(
            "https://www.tikwm.com/api/",
            "https://api.tikwm.com/api/"
        )

        for (endpoint in endpoints) {
            try {
                val formBody = FormBody.Builder()
                    .add("url", tiktokUrl)
                    .add("hd", "1")
                    .build()

                val request = Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: continue
                val json = JSONObject(responseStr)
                val code = json.optInt("code", -1)

                if (code == 0 && json.has("data")) {
                    val data = json.getJSONObject("data")
                    val title = data.optString("title", "TikTok Video")
                    var play = data.optString("play", null)
                    var hdplay = data.optString("hdplay", null)
                    var music = data.optString("music", null)
                    val cover = data.optString("cover", null)
                    val duration = data.optInt("duration", 0)

                    val diggCount = data.optLong("digg_count", 0L)
                    val commentCount = data.optLong("comment_count", 0L)
                    val shareCount = data.optLong("share_count", 0L)

                    var authorNickname: String? = null
                    var authorUniqueId: String? = null
                    if (data.has("author")) {
                        val authorObj = data.getJSONObject("author")
                        authorNickname = authorObj.optString("nickname", null)
                        authorUniqueId = authorObj.optString("unique_id", null)
                    }

                    var musicTitle: String? = null
                    if (data.has("music_info")) {
                        val musicObj = data.getJSONObject("music_info")
                        musicTitle = musicObj.optString("title", null)
                    }

                    // Normalize relative URLs
                    if (play?.startsWith("/") == true) play = "https://www.tikwm.com$play"
                    if (hdplay?.startsWith("/") == true) hdplay = "https://www.tikwm.com$hdplay"
                    if (music?.startsWith("/") == true) music = "https://www.tikwm.com$music"

                    return SocialMediaMetadata(
                        platform = SocialPlatform.TIKTOK,
                        sourceUrl = tiktokUrl,
                        title = title,
                        authorName = authorNickname,
                        authorUsername = authorUniqueId,
                        likesCount = diggCount,
                        commentsCount = commentCount,
                        sharesCount = shareCount,
                        durationSeconds = duration,
                        musicTitle = musicTitle,
                        thumbnailUrl = cover,
                        downloadHdUrl = hdplay ?: play,
                        downloadSdUrl = play ?: hdplay,
                        downloadAudioUrl = music
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed resolving TikTok with $endpoint: ${e.message}")
            }
        }
        return null
    }

    private fun resolveFacebook(client: OkHttpClient, fbUrl: String): SocialMediaMetadata? {
        // Strategy 1: FDown / SnapSave public service resolvers
        try {
            val fdownResult = resolveFacebookViaSnapsave(client, fbUrl)
            if (fdownResult != null) return fdownResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook Snapsave API resolution failed: ${e.message}")
        }

        // Strategy 2: Direct Facebook public page inspection with Mobile User Agent
        try {
            val directResult = resolveFacebookDirectScrape(client, fbUrl)
            if (directResult != null) return directResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook direct scrape failed: ${e.message}")
        }

        // Strategy 3: Multi-platform Cobalt API instance resolver
        try {
            val cobaltResult = resolveViaCobalt(client, fbUrl, SocialPlatform.FACEBOOK)
            if (cobaltResult != null) return cobaltResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook Cobalt API resolution failed: ${e.message}")
        }

        return null
    }

    private fun resolveFacebookViaSnapsave(client: OkHttpClient, fbUrl: String): SocialMediaMetadata? {
        val formBody = FormBody.Builder()
            .add("url", fbUrl)
            .build()

        val request = Request.Builder()
            .url("https://snapsave.app/action.php")
            .post(formBody)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://snapsave.app/")
            .header("Origin", "https://snapsave.app")
            .build()

        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: return null

        // Parse SnapSave rendered table for HD/SD download buttons
        var hdUrl: String? = null
        var sdUrl: String? = null
        var title = "Facebook Video"

        val hdMatcher = Pattern.compile("href=\"(https?://[^\"]+)\"[^>]*>Download (?:HD|1080p|720p)", Pattern.CASE_INSENSITIVE).matcher(html)
        if (hdMatcher.find()) {
            hdUrl = decodeHtml(hdMatcher.group(1))
        }

        val sdMatcher = Pattern.compile("href=\"(https?://[^\"]+)\"[^>]*>Download (?:SD|360p|480p|Render)", Pattern.CASE_INSENSITIVE).matcher(html)
        if (sdMatcher.find()) {
            sdUrl = decodeHtml(sdMatcher.group(1))
        }

        val titleMatcher = Pattern.compile("video-card-title\">(.*?)<", Pattern.CASE_INSENSITIVE).matcher(html)
        if (titleMatcher.find()) {
            title = titleMatcher.group(1)?.trim() ?: "Facebook Video"
        }

        if (hdUrl != null || sdUrl != null) {
            return SocialMediaMetadata(
                platform = SocialPlatform.FACEBOOK,
                sourceUrl = fbUrl,
                title = title,
                downloadHdUrl = hdUrl ?: sdUrl,
                downloadSdUrl = sdUrl ?: hdUrl,
                downloadAudioUrl = null
            )
        }

        return null
    }

    private fun resolveFacebookDirectScrape(client: OkHttpClient, fbUrl: String): SocialMediaMetadata? {
        val mobileUrl = fbUrl.replace("www.facebook.com", "m.facebook.com")
            .replace("web.facebook.com", "m.facebook.com")

        val request = Request.Builder()
            .url(mobileUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: return null

        var hdUrl: String? = null
        var sdUrl: String? = null
        var title = "Facebook Video"

        // Search for playable_url_quality_hd / hd_src / browser_native_hd_url
        val hdRegex = Pattern.compile("\"(?:playable_url_quality_hd|browser_native_hd_url|hd_src)\":\"(https?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"")
        val hdM = hdRegex.matcher(html)
        if (hdM.find()) {
            hdUrl = unescapeJsonUrl(hdM.group(1))
        }

        // Search for playable_url / sd_src / browser_native_sd_url
        val sdRegex = Pattern.compile("\"(?:playable_url|browser_native_sd_url|sd_src)\":\"(https?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"")
        val sdM = sdRegex.matcher(html)
        if (sdM.find()) {
            sdUrl = unescapeJsonUrl(sdM.group(1))
        }

        // Search for title/og:title/og:description
        val titleRegex = Pattern.compile("<meta property=\"og:title\" content=\"([^\"]+)\"")
        val titleM = titleRegex.matcher(html)
        if (titleM.find()) {
            title = decodeHtml(titleM.group(1))
        } else {
            val descRegex = Pattern.compile("<meta property=\"og:description\" content=\"([^\"]+)\"")
            val descM = descRegex.matcher(html)
            if (descM.find()) {
                title = decodeHtml(descM.group(1)).take(60)
            }
        }

        if (hdUrl != null || sdUrl != null) {
            return SocialMediaMetadata(
                platform = SocialPlatform.FACEBOOK,
                sourceUrl = fbUrl,
                title = title,
                downloadHdUrl = hdUrl ?: sdUrl,
                downloadSdUrl = sdUrl ?: hdUrl,
                downloadAudioUrl = null
            )
        }

        return null
    }

    private fun resolveViaCobalt(client: OkHttpClient, url: String, platform: SocialPlatform): SocialMediaMetadata? {
        val cobaltInstances = listOf(
            "https://co.wuk.sh/api/json",
            "https://api.cobalt.tools/api/json"
        )

        for (endpoint in cobaltInstances) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("url", url)
                    put("vQuality", "720")
                    put("filenamePattern", "basic")
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: continue
                val json = JSONObject(responseStr)

                val streamUrl = json.optString("url", null)
                if (!streamUrl.isNullOrBlank()) {
                    return SocialMediaMetadata(
                        platform = platform,
                        sourceUrl = url,
                        title = "${platform.displayName} Video",
                        downloadHdUrl = streamUrl,
                        downloadSdUrl = streamUrl
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cobalt instance $endpoint error: ${e.message}")
            }
        }
        return null
    }

    private fun unescapeJsonUrl(escaped: String): String {
        return escaped
            .replace("\\/", "/")
            .replace("\\u0025", "%")
            .replace("\\u0026", "&")
            .replace("&amp;", "&")
    }

    private fun decodeHtml(htmlStr: String): String {
        return htmlStr
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
    }
}
