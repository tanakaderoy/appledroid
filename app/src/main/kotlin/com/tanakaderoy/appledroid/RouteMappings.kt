package com.tanakaderoy.appledroid

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Host-to-handler registry.
 *
 * Each entry in [ROUTES] maps an Apple hostname to a handler function.
 * To add a new mapping, add an entry here and implement the logic.
 */
object RouteMappings {

    const val PACKAGE_GOOGLE_MAPS = "com.google.android.apps.maps"
    const val PACKAGE_SPOTIFY = "com.spotify.music"

    private fun PackageManager.isInstalled(pkg: String): Boolean =
        getLaunchIntentForPackage(pkg) != null

    private val ROUTES: Map<String, (Uri, PackageManager) -> RouteResult> = mapOf(
        "maps.apple.com"     to ::routeMaps,
        "maps.apple"         to ::routeMaps, // short links redirect to maps.apple.com/place
        "music.apple.com"    to ::routeMusic,
        "podcasts.apple.com" to ::routePodcasts,
        "tv.apple.com"       to ::routeTv,
    )

    fun resolve(uri: Uri, pm: PackageManager): RouteResult {
        val handler = ROUTES[uri.host] ?: return RouteResult.NoMatch
        return handler(uri, pm)
    }

    private fun routeMaps(uri: Uri, pm: PackageManager): RouteResult {
        val geoUri = MapsUrlParser.parse(uri) ?: return RoutingFallbacks.browser(uri)
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)).apply {
            setPackage(PACKAGE_GOOGLE_MAPS)
        }
        return if (pm.isInstalled(PACKAGE_GOOGLE_MAPS)) {
            RouteResult.LaunchIntent(mapsIntent)
        } else {
            RoutingFallbacks.browser(uri)
        }
    }

    private fun routeMusic(uri: Uri, pm: PackageManager): RouteResult {
        val query = MusicUrlParser.parse(uri) ?: return RoutingFallbacks.browser(uri)
        val spotifyUri = Uri.parse("spotify:search:${Uri.encode(query)}")
        val spotifyIntent = Intent(Intent.ACTION_VIEW, spotifyUri).apply {
            setPackage(PACKAGE_SPOTIFY)
        }
        return if (pm.isInstalled(PACKAGE_SPOTIFY)) {
            RouteResult.LaunchIntent(spotifyIntent)
        } else {
            val webUri = Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}")
            RoutingFallbacks.browser(webUri)
        }
    }

    private fun routePodcasts(uri: Uri, pm: PackageManager): RouteResult {
        // Extract show/episode name from path for a web search fallback.
        // Path shape: /us/podcast/{show-slug}/id{numeric} or /us/podcast/{show-slug}
        val segments = uri.pathSegments.filter { it.isNotBlank() }
        val slug = segments
            .filterNot { it == "podcast" || it.startsWith("id") && it.drop(2).toLongOrNull() != null }
            .lastOrNull { it.toLongOrNull() == null }
        return if (slug != null) {
            val query = slug.replace('-', ' ')
            RoutingFallbacks.webSearch("$query podcast")
        } else {
            RoutingFallbacks.browser(uri)
        }
    }

    private fun routeTv(uri: Uri, pm: PackageManager): RouteResult =
        // No direct Android equivalent; pass through to browser.
        RoutingFallbacks.browser(uri)
}
