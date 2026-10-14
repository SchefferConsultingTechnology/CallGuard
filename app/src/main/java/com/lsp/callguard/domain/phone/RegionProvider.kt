package com.lsp.callguard.domain.phone

/**
 * Supplies the default region used to interpret numbers dialed without a country code.
 */
fun interface RegionProvider {
    /** ISO 3166-1 alpha-2 uppercase supported by libphonenumber, or null; never throws. */
    suspend fun defaultRegion(): String?
}
