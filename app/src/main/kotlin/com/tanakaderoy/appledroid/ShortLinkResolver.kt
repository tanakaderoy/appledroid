package com.tanakaderoy.appledroid

import android.net.Uri
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * Resolves a short/redirect URL to its final destination by following HTTP redirects.
 *
 * Used for `maps.apple` short links (e.g. maps.apple/p/N7PT9JJnRGtVyi) which redirect
 * to full `maps.apple.com/place?coordinate=...` URLs that our parsers can handle.
 *
 * Must be called off the main thread.
 */
object ShortLinkResolver {

    private const val TAG = "ShortLinkResolver"
    private const val TIMEOUT_MS = 5_000

    /**
     * Returns the resolved [Uri] after following all redirects, or `null` on failure.
     */
    fun resolve(uri: Uri): Uri? {
        var current = uri.toString()
        repeat(MAX_REDIRECTS) {
            val location = headLocation(current) ?: return Uri.parse(current)
            current = location
        }
        Log.w(TAG, "Exceeded max redirects for $uri")
        return null
    }

    /** Issues a HEAD request and returns the Location header, or null if none. */
    private fun headLocation(url: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                instanceFollowRedirects = false
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            conn.connect()
            val code = conn.responseCode
            if (code in 300..399) conn.getHeaderField("Location") else null
        } catch (e: Exception) {
            Log.w(TAG, "HEAD $url failed: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    private const val MAX_REDIRECTS = 5
}
