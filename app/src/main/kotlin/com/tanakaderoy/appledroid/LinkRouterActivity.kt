package com.tanakaderoy.appledroid

import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.util.Log

class LinkRouterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data ?: run {
            Log.w(TAG, "Launched with no URI")
            finish()
            return
        }

        Log.d(TAG, "Incoming: $uri")

        if (uri.host == "maps.apple") {
            // Short link — resolve the redirect off the main thread first.
            Thread {
                val resolved = ShortLinkResolver.resolve(uri) ?: uri
                Log.d(TAG, "Resolved to: $resolved")
                runOnUiThread { dispatchAndFinish(resolved) }
            }.start()
        } else {
            dispatchAndFinish(uri)
        }
    }

    private fun dispatchAndFinish(uri: Uri) {
        val result = AppleLinkRouter(packageManager).route(uri)
        Log.d(TAG, "Route result: $result")
        try {
            when (result) {
                is RouteResult.LaunchIntent   -> startActivity(result.intent)
                is RouteResult.FallbackIntent -> startActivity(result.intent)
                is RouteResult.NoMatch        -> Log.i(TAG, "No route for $uri")
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity for route: ${e.message}")
        }
        finish()
    }

    companion object {
        private const val TAG = "LinkRouter"
    }
}
