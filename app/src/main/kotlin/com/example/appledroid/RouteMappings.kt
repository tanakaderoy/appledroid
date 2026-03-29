package com.example.appledroid

import android.content.pm.PackageManager
import android.net.Uri

/**
 * Host-to-handler registry.
 *
 * Each entry in [ROUTES] maps an Apple hostname to a handler function.
 * To add a new mapping, add an entry here and implement the logic.
 */
object RouteMappings {

    private val ROUTES: Map<String, (Uri, PackageManager) -> RouteResult> = mapOf(
        "maps.apple.com"     to ::routeMaps,
        "music.apple.com"    to ::routeMusic,
        "podcasts.apple.com" to ::routePodcasts,
        "tv.apple.com"       to ::routeTv,
    )

    fun resolve(uri: Uri, pm: PackageManager): RouteResult {
        val handler = ROUTES[uri.host] ?: return RouteResult.NoMatch
        return handler(uri, pm)
    }

    // Implementations filled in subsequent chunks.
    private fun routeMaps(uri: Uri, pm: PackageManager): RouteResult =
        RoutingFallbacks.browser(uri)

    private fun routeMusic(uri: Uri, pm: PackageManager): RouteResult =
        RoutingFallbacks.browser(uri)

    private fun routePodcasts(uri: Uri, pm: PackageManager): RouteResult =
        RoutingFallbacks.browser(uri)

    private fun routeTv(uri: Uri, pm: PackageManager): RouteResult =
        RoutingFallbacks.browser(uri)
}
