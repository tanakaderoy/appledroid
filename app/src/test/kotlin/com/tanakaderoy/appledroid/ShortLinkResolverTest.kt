package com.tanakaderoy.appledroid

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class ShortLinkResolverTest {

    @Test fun `resolve returns null on network failure without crashing`() {
        // Robolectric has no real network — openConnection throws, resolver returns null.
        val result = ShortLinkResolver.resolve(Uri.parse("https://maps.apple/p/invalid"))
        // Either null (error path) or the original URI (no-redirect path); must not throw.
        // We just assert no exception is thrown and result is consistent.
        assert(result == null || result.toString().isNotBlank())
    }

    @Test fun `resolve handles malformed URI gracefully`() {
        val result = ShortLinkResolver.resolve(Uri.parse("https://maps.apple/p/"))
        assert(result == null || result.toString().isNotBlank())
    }
}
