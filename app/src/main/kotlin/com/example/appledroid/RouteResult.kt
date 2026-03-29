package com.example.appledroid

import android.content.Intent

/** Result of a routing decision made by [AppleLinkRouter]. */
sealed class RouteResult {
    /** A resolved intent targeting a known Android app. */
    data class LaunchIntent(val intent: Intent) : RouteResult()

    /** A fallback intent (browser or search) used when the preferred app is absent. */
    data class FallbackIntent(val intent: Intent) : RouteResult()

    /** The URI did not match any registered route. */
    object NoMatch : RouteResult()
}
