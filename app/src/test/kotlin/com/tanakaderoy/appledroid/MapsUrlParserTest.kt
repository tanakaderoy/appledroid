package com.tanakaderoy.appledroid

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MapsUrlParserTest {

    @Test fun `ll param produces geo URI with coordinates`() {
        val uri = Uri.parse("https://maps.apple.com/?ll=37.332,-122.031")
        assertEquals("geo:37.332,-122.031", MapsUrlParser.parse(uri))
    }

    @Test fun `q param produces geo URI with query`() {
        val uri = Uri.parse("https://maps.apple.com/?q=Eiffel+Tower")
        assertEquals("geo:0,0?q=Eiffel%20Tower", MapsUrlParser.parse(uri))
    }

    @Test fun `address param produces geo URI with query`() {
        val uri = Uri.parse("https://maps.apple.com/?address=1+Infinite+Loop")
        assertEquals("geo:0,0?q=1%20Infinite%20Loop", MapsUrlParser.parse(uri))
    }

    @Test fun `auid param produces geo URI with place id`() {
        val uri = Uri.parse("https://maps.apple.com/?auid=1234567890")
        assertEquals("geo:0,0?q=1234567890", MapsUrlParser.parse(uri))
    }

    @Test fun `ll takes priority over q`() {
        val uri = Uri.parse("https://maps.apple.com/?ll=48.858,2.294&q=Eiffel")
        assertEquals("geo:48.858,2.294", MapsUrlParser.parse(uri))
    }

    @Test fun `malformed ll falls back to q`() {
        val uri = Uri.parse("https://maps.apple.com/?ll=notanumber&q=fallback")
        assertEquals("geo:0,0?q=fallback", MapsUrlParser.parse(uri))
    }

    @Test fun `coordinate param (place URLs) produces geo URI`() {
        val uri = Uri.parse("https://maps.apple.com/place?coordinate=39.943653,-83.078491&name=2988+Sullivant+Ave")
        assertEquals("geo:39.943653,-83.078491?q=2988%20Sullivant%20Ave", MapsUrlParser.parse(uri))
    }

    @Test fun `coordinate param without name produces bare geo URI`() {
        val uri = Uri.parse("https://maps.apple.com/place?coordinate=39.943653,-83.078491")
        assertEquals("geo:39.943653,-83.078491", MapsUrlParser.parse(uri))
    }

    @Test fun `empty URI returns null`() {
        val uri = Uri.parse("https://maps.apple.com/")
        assertNull(MapsUrlParser.parse(uri))
    }
}
