package com.tanakaderoy.appledroid

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppleLinkRouterTest {

    private fun pmWith(vararg installedPackages: String): PackageManager = mock {
        for (pkg in installedPackages) {
            on { getLaunchIntentForPackage(pkg) } doReturn Intent()
        }
    }

    @Test fun `maps URL with Google Maps installed returns LaunchIntent`() {
        val pm = pmWith(RouteMappings.PACKAGE_GOOGLE_MAPS)
        val result = AppleLinkRouter(pm).route(Uri.parse("https://maps.apple.com/?q=Paris"))
        assertTrue(result is RouteResult.LaunchIntent)
    }

    @Test fun `maps URL without Google Maps installed returns FallbackIntent`() {
        val pm = pmWith()
        val result = AppleLinkRouter(pm).route(Uri.parse("https://maps.apple.com/?q=Paris"))
        assertTrue(result is RouteResult.FallbackIntent)
    }

    @Test fun `music URL with Spotify installed returns LaunchIntent`() {
        val pm = pmWith(RouteMappings.PACKAGE_SPOTIFY)
        val result = AppleLinkRouter(pm).route(
            Uri.parse("https://music.apple.com/us/artist/radiohead/1053394")
        )
        assertTrue(result is RouteResult.LaunchIntent)
    }

    @Test fun `music URL without Spotify returns FallbackIntent`() {
        val pm = pmWith()
        val result = AppleLinkRouter(pm).route(
            Uri.parse("https://music.apple.com/us/album/ok-computer/1097861203")
        )
        assertTrue(result is RouteResult.FallbackIntent)
    }

    @Test fun `podcasts URL returns FallbackIntent`() {
        val pm = pmWith()
        val result = AppleLinkRouter(pm).route(
            Uri.parse("https://podcasts.apple.com/us/podcast/my-show/id123456")
        )
        assertTrue(result is RouteResult.FallbackIntent)
    }

    @Test fun `tv URL returns FallbackIntent with original URI`() {
        val pm = pmWith()
        val original = Uri.parse("https://tv.apple.com/show/severance/12345")
        val result = AppleLinkRouter(pm).route(original)
        assertTrue(result is RouteResult.FallbackIntent)
        assertEquals(original, (result as RouteResult.FallbackIntent).intent.data)
    }

    @Test fun `unknown host returns NoMatch`() {
        val pm = pmWith()
        val result = AppleLinkRouter(pm).route(Uri.parse("https://unknown.apple.com/foo"))
        assertEquals(RouteResult.NoMatch, result)
    }

    @Test fun `malformed URI does not crash`() {
        val pm = pmWith()
        val result = AppleLinkRouter(pm).route(Uri.parse("https://maps.apple.com/"))
        // No useful params — should return a FallbackIntent or NoMatch, but never throw.
        assertTrue(result is RouteResult.FallbackIntent || result is RouteResult.NoMatch)
    }
}
