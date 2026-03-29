package com.example.appledroid

import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log

class LinkRouterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri == null) {
            Log.w(TAG, "Launched with no URI — finishing")
            finish()
            return
        }

        Log.d(TAG, "Routing: $uri")

        val router = AppleLinkRouter(packageManager)
        val result = router.route(uri)

        Log.d(TAG, "Route result: $result")

        try {
            when (result) {
                is RouteResult.LaunchIntent -> startActivity(result.intent)
                is RouteResult.FallbackIntent -> startActivity(result.intent)
                is RouteResult.NoMatch -> Log.i(TAG, "No route for $uri — doing nothing")
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found for route result: ${e.message}")
        }

        finish()
    }

    companion object {
        private const val TAG = "LinkRouter"
    }
}
