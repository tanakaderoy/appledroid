package com.tanakaderoy.appledroid

import android.net.Uri

/** Parses an `maps.apple.com` URL into a geo URI string suitable for Google Maps. */
object MapsUrlParser {

    /**
     * Returns a `geo:` URI string, or `null` if no usable location data is found.
     *
     * Priority:
     *  1. `ll` or `coordinate` param → `geo:lat,lon` (optionally labelled with `name`)
     *  2. `q` param                  → `geo:0,0?q=<query>`
     *  3. `address`                  → `geo:0,0?q=<address>`
     *  4. `auid`                     → `geo:0,0?q=<auid>` (place ID — Maps will resolve it)
     *
     * `maps.apple.com/place` uses `coordinate` + `name` instead of `ll`.
     * Example: ?coordinate=39.943653,-83.078491&name=2988+Sullivant+Ave
     */
    fun parse(uri: Uri): String? {
        // `ll` (classic) and `coordinate` (/place URLs) are both lat,lon pairs.
        val coords = uri.getQueryParameter("ll") ?: uri.getQueryParameter("coordinate")
        if (!coords.isNullOrBlank()) {
            val parts = coords.split(",")
            if (parts.size == 2 && parts.all { it.isNumericCoord() }) {
                val lat = parts[0].trim()
                val lon = parts[1].trim()
                val name = uri.getQueryParameter("name")?.trim()
                return if (!name.isNullOrBlank()) {
                    "geo:$lat,$lon?q=${Uri.encode(name)}"
                } else {
                    "geo:$lat,$lon"
                }
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
