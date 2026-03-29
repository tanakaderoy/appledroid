package com.tanakaderoy.appledroid

import android.net.Uri

/** Parses an `maps.apple.com` URL into a geo URI string suitable for Google Maps. */
object MapsUrlParser {

    /**
     * Returns a `geo:` URI string, or `null` if no usable location data is found.
     *
     * Priority:
     *  1. `ll` param  → `geo:lat,lon`
     *  2. `q` param   → `geo:0,0?q=<query>`
     *  3. `address`   → `geo:0,0?q=<address>`
     *  4. `auid`      → `geo:0,0?q=<auid>` (place ID — Maps will resolve it)
     */
    fun parse(uri: Uri): String? {
        val ll = uri.getQueryParameter("ll")
        if (!ll.isNullOrBlank()) {
            val parts = ll.split(",")
            if (parts.size == 2 && parts.all { it.isNumericCoord() }) {
                return "geo:${parts[0].trim()},${parts[1].trim()}"
            }
        }

        val q = uri.getQueryParameter("q")?.trim()
        if (!q.isNullOrBlank()) return "geo:0,0?q=${Uri.encode(q)}"

        val address = uri.getQueryParameter("address")?.trim()
        if (!address.isNullOrBlank()) return "geo:0,0?q=${Uri.encode(address)}"

        val auid = uri.getQueryParameter("auid")?.trim()
        if (!auid.isNullOrBlank()) return "geo:0,0?q=${Uri.encode(auid)}"

        return null
    }

    private fun String.isNumericCoord(): Boolean =
        trim().toDoubleOrNull() != null
}
