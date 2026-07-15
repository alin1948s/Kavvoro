package com.moonsolstudios.kavvoro.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumCatalogTest {
    @Test
    fun everyPremiumProductMapsToOneUniqueBrainball() {
        assertEquals(4, PremiumCatalog.productToSkinId.size)
        assertEquals(4, PremiumCatalog.productToSkinId.values.toSet().size)
        assertEquals(PremiumCatalog.productToSkinId.size, PremiumCatalog.skinToProductId.size)
        assertTrue(PremiumCatalog.productIds.all { it.matches(Regex("[a-z0-9_]+")) })
    }
}
