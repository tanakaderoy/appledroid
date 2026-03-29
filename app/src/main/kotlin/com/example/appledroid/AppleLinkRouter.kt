package com.example.appledroid

import android.content.pm.PackageManager
import android.net.Uri

/**
 * Routes an Apple URI to a [RouteResult].
 *
 * Delegates host-specific logic to [RouteMappings].
 */
class AppleLinkRouter(private val packageManager: PackageManager) {

    fun route(uri: Uri): RouteResult = RouteMappings.resolve(uri, packageManager)
}
