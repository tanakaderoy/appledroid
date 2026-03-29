package com.tanakaderoy.appledroid

import android.net.Uri

/**
 * Parses a `music.apple.com` URL and returns a human-readable search term.
 *
 * Apple Music URL shapes:
 *   /us/artist/{slug}/{id}
 *   /us/album/{slug}/{id}
 *   /us/song/{slug}/{id}
 *   /us/playlist/{slug}/{id}
 *
 * The locale segment (e.g. "us") is optional and variable.
 * We extract the slug that appears before the numeric ID.
 */
object MusicUrlParser {

    /**
     * Returns a search term derived from the URL path, or `null` if none can be extracted.
     */
    fun parse(uri: Uri): String? {
        val segments = uri.pathSegments.filter { it.isNotBlank() }
        // segments: [locale?, type, slug, id?]
        // Find the last non-numeric segment before a numeric ID, or just use the last slug.
        val slug = segments
            .dropWhile { it.toLongOrNull() != null } // skip any leading numeric (shouldn't happen)
            .lastOrNull { it.toLongOrNull() == null && !KNOWN_TYPE_SEGMENTS.contains(it) && !it.startsWith("pl.") }
            ?: return null
        return slug.replace('-', ' ').trim().takeIf { it.isNotBlank() }
    }

    private val KNOWN_TYPE_SEGMENTS = setOf("artist", "album", "song", "playlist", "show", "episode")
}
