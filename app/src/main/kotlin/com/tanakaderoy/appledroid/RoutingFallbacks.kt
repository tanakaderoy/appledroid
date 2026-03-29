package com.tanakaderoy.appledroid

import android.content.Intent
import android.net.Uri

/** Builds fallback intents for when the preferred app is not installed. */
object RoutingFallbacks {

    /** Opens the original URL in the system browser. */
    fun browser(uri: Uri): RouteResult.FallbackIntent =
        RouteResult.FallbackIntent(Intent(Intent.ACTION_VIEW, uri))

    /** Opens a generic web search for [query] in the browser. */
    fun webSearch(query: String): RouteResult.FallbackIntent {
        val encoded = Uri.encode(query)
        val searchUri = Uri.parse("https://www.google.com/search?q=$encoded")
        return RouteResult.FallbackIntent(Intent(Intent.ACTION_VIEW, searchUri))
    }
}
