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
        val expandedUrl = expandRedirects(client, url)
        return when (platform) {
            SocialPlatform.TIKTOK -> resolveTikTok(client, expandedUrl)
            SocialPlatform.FACEBOOK -> resolveFacebook(client, expandedUrl)
            SocialPlatform.UNKNOWN -> {
                // Try TikTok then Facebook
                resolveTikTok(client, expandedUrl) ?: resolveFacebook(client, expandedUrl)
            }
        }
    }

    private fun expandRedirects(client: OkHttpClient, url: String): String {
        if (!url.contains("fb.watch") && !url.contains("fb.me") && !url.contains("/share/")) return url
        try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .head()
                .build()
            val resp = client.newCall(req).execute()
            val redirectedUrl = resp.request.url.toString()
            if (redirectedUrl.isNotBlank()) return redirectedUrl
        } catch (e: Exception) {
            Log.w(TAG, "Expand redirect failed for $url: ${e.message}")
        }
        return url
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

        // Fallback to Render downloader API
        return resolveViaRenderApi(client, tiktokUrl, SocialPlatform.TIKTOK)
    }

    private fun resolveFacebook(client: OkHttpClient, fbUrl: String): SocialMediaMetadata? {
        // Strategy 1: Render Downloader API (Formula from provided HTML app)
        try {
            val renderResult = resolveViaRenderApi(client, fbUrl, SocialPlatform.FACEBOOK)
            if (renderResult != null) return renderResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook Render API resolution failed: ${e.message}")
        }

        // Strategy 2: FDown / SnapSave public service resolvers
        try {
            val fdownResult = resolveFacebookViaSnapsave(client, fbUrl)
            if (fdownResult != null) return fdownResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook Snapsave API resolution failed: ${e.message}")
        }

        // Strategy 3: Direct Facebook public page inspection with Mobile & Desktop User Agents
        try {
            val directResult = resolveFacebookDirectScrape(client, fbUrl)
            if (directResult != null) return directResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook direct scrape failed: ${e.message}")
        }

        // Strategy 4: Multi-platform Cobalt API instance resolver
        try {
            val cobaltResult = resolveViaCobalt(client, fbUrl, SocialPlatform.FACEBOOK)
            if (cobaltResult != null) return cobaltResult
        } catch (e: Exception) {
            Log.w(TAG, "Facebook Cobalt API resolution failed: ${e.message}")
        }

        return null
    }

    /**
     * Dedicated Downloader API service strategy from provided HTML formula.
     */
    private fun resolveViaRenderApi(client: OkHttpClient, url: String, platform: SocialPlatform): SocialMediaMetadata? {
        val renderEndpoints = listOf(
            "https://ff-ii.onrender.com/api/download",
            "https://dfdfdsf44-rasiyy66.hf.space/force_download"
        )

        for (endpoint in renderEndpoints) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("url", url)
                }
                val body = jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: continue
                if (!response.isSuccessful) continue

                val json = JSONObject(responseStr)
                val dataObj = if (json.has("data") && json.opt("data") is JSONObject) json.getJSONObject("data") else json

                val title = dataObj.optString("title", if (platform == SocialPlatform.FACEBOOK) "Facebook Video" else "TikTok Video")
                var hdplay = dataObj.optString("hdplay", null)
                var play = dataObj.optString("play", null)
                var music = dataObj.optString("music", null)
                val cover = dataObj.optString("cover", null)

                if (hdplay.isNullOrBlank()) hdplay = null
                if (play.isNullOrBlank()) play = null
                if (music.isNullOrBlank()) music = null

                if (hdplay != null || play != null || music != null) {
                    return SocialMediaMetadata(
                        platform = platform,
                        sourceUrl = url,
                        title = if (title.isNotBlank()) title else "${platform.displayName} Media",
                        thumbnailUrl = if (!cover.isNullOrBlank()) cover else null,
                        downloadHdUrl = hdplay ?: play,
                        downloadSdUrl = play ?: hdplay,
                        downloadAudioUrl = music
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Render API instance $endpoint failed: ${e.message}")
            }
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
        var html = response.body?.string() ?: return null

        // If JS unpacked code is returned, attempt unpacking
        if (html.contains("eval(function(p,a,c,k,e,d)")) {
            val unpacked = unpackJs(html)
            if (!unpacked.isNullOrBlank()) {
                html = unpacked
            }
        }

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

        // Generic URL search if table class is formatted differently
        if (hdUrl == null && sdUrl == null) {
            val genericMatcher = Pattern.compile("href=\"(https?://[^\"]+)\"[^>]*>Download", Pattern.CASE_INSENSITIVE).matcher(html)
            if (genericMatcher.find()) {
                sdUrl = decodeHtml(genericMatcher.group(1))
            }
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

    private fun unpackJs(packedJs: String): String? {
        try {
            val match = Pattern.compile("}\\('([^']*)',(\\d+),(\\d+),'([^']*)'\\.split\\('\\|'\\)").matcher(packedJs)
            if (match.find()) {
                val p = match.group(1) ?: return null
                val a = match.group(2)?.toIntOrNull() ?: 36
                val c = match.group(3)?.toIntOrNull() ?: 0
                val k = match.group(4)?.split("|") ?: return null

                var result = p
                for (i in (c - 1) downTo 0) {
                    val word = k.getOrNull(i)
                    if (!word.isNullOrEmpty()) {
                        val key = i.toString(a)
                        result = result.replace(Regex("\\b$key\\b"), word)
                    }
                }
                return result
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unpack JS error: ${e.message}")
        }
        return null
    }

    private fun resolveFacebookDirectScrape(client: OkHttpClient, fbUrl: String): SocialMediaMetadata? {
        val urlsToTry = listOf(
            fbUrl.replace("www.facebook.com", "m.facebook.com").replace("web.facebook.com", "m.facebook.com"),
            fbUrl.replace("m.facebook.com", "mbasic.facebook.com").replace("www.facebook.com", "mbasic.facebook.com")
        )

        for (targetUrl in urlsToTry) {
            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build()

                val response = client.newCall(request).execute()
                val html = response.body?.string() ?: continue

                var hdUrl: String? = null
                var sdUrl: String? = null
                var title = "Facebook Video"

                // Search for hd_src_no_ratelimit / playable_url_quality_hd / hd_src / browser_native_hd_url
                val hdRegex = Pattern.compile("\"(?:playable_url_quality_hd|browser_native_hd_url|hd_src|hd_src_no_ratelimit)\":\"(https?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"")
                val hdM = hdRegex.matcher(html)
                if (hdM.find()) {
                    hdUrl = unescapeJsonUrl(hdM.group(1))
                }

                // Search for sd_src_no_ratelimit / playable_url / sd_src / browser_native_sd_url
                val sdRegex = Pattern.compile("\"(?:playable_url|browser_native_sd_url|sd_src|sd_src_no_ratelimit)\":\"(https?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"")
                val sdM = sdRegex.matcher(html)
                if (sdM.find()) {
                    sdUrl = unescapeJsonUrl(sdM.group(1))
                }

                // Fallback for mbasic video redirect URLs
                if (hdUrl == null && sdUrl == null) {
                    val redirectRegex = Pattern.compile("href=\"(/video_redirect/[^\"]+)\"")
                    val redirectM = redirectRegex.matcher(html)
                    if (redirectM.find()) {
                        val rawPath = decodeHtml(redirectM.group(1))
                        val srcParam = Pattern.compile("src=([^&]+)").matcher(rawPath)
                        if (srcParam.find()) {
                            sdUrl = URLDecoder.decode(srcParam.group(1), "UTF-8")
                        }
                    }
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
            } catch (e: Exception) {
                Log.w(TAG, "Direct scrape $targetUrl error: ${e.message}")
            }
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
