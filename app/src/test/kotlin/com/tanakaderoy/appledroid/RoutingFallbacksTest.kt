package com.tanakaderoy.appledroid

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoutingFallbacksTest {

    @Test fun `browser returns FallbackIntent with the original URI`() {
        val uri = Uri.parse("https://maps.apple.com/?q=test")
        val result = RoutingFallbacks.browser(uri)
        assertEquals(Intent.ACTION_VIEW, result.intent.action)
        assertEquals(uri, result.intent.data)
    }

    @Test fun `webSearch returns FallbackIntent with Google search URI`() {
        val result = RoutingFallbacks.webSearch("radiohead")
        assertEquals(Intent.ACTION_VIEW, result.intent.action)
        assertTrue(result.intent.data.toString().contains("google.com/search"))
        assertTrue(result.intent.data.toString().contains("radiohead"))
    }
}
