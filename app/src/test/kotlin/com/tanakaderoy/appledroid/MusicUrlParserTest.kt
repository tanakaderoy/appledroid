package com.tanakaderoy.appledroid

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MusicUrlParserTest {

    @Test fun `artist URL extracts slug`() {
        val uri = Uri.parse("https://music.apple.com/us/artist/radiohead/1053394")
        assertEquals("radiohead", MusicUrlParser.parse(uri))
    }

    @Test fun `album URL extracts slug`() {
        val uri = Uri.parse("https://music.apple.com/us/album/ok-computer/1097861203")
        assertEquals("ok computer", MusicUrlParser.parse(uri))
    }

    @Test fun `song URL extracts slug`() {
        val uri = Uri.parse("https://music.apple.com/us/song/karma-police/1097862350")
        assertEquals("karma police", MusicUrlParser.parse(uri))
    }

    @Test fun `playlist URL extracts slug`() {
        val uri = Uri.parse("https://music.apple.com/us/playlist/chill-vibes/pl.abc123")
        assertEquals("chill vibes", MusicUrlParser.parse(uri))
    }

    @Test fun `bare root path returns null`() {
        val uri = Uri.parse("https://music.apple.com/")
        assertNull(MusicUrlParser.parse(uri))
    }
}
