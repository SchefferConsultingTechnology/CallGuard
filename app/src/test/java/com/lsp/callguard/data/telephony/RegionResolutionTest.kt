package com.lsp.callguard.data.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegionResolutionTest {

    private val supported = setOf("BR", "PT", "US", "ES")

    private fun resolve(
        sim: String? = null,
        network: String? = null,
        locale: String? = null,
        lastKnown: String? = null
    ) = resolveRegion(sim, network, locale, lastKnown, supported)

    @Test
    fun `sim country is used first and uppercased`() {
        assertEquals("BR", resolve(sim = "br", network = "us", locale = "ES"))
    }

    @Test
    fun `roaming keeps the sim region`() {
        assertEquals("BR", resolve(sim = "br", network = "pt"))
    }

    @Test
    fun `network is used when there is no sim`() {
        assertEquals("PT", resolve(network = "pt", locale = "ES"))
    }

    @Test
    fun `locale is used when there is no sim nor network`() {
        assertEquals("ES", resolve(locale = "ES", lastKnown = "BR"))
    }

    @Test
    fun `last known region is the final fallback`() {
        assertEquals("BR", resolve(lastKnown = "BR"))
    }

    @Test
    fun `null when nothing is available`() {
        assertNull(resolve())
    }

    @Test
    fun `unsupported region falls through to the next source`() {
        assertEquals("PT", resolve(sim = "xx", network = "pt"))
    }

    @Test
    fun `blank values are treated as missing`() {
        assertEquals("US", resolve(sim = "", network = "  ", locale = "us"))
    }
}
