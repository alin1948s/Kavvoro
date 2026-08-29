package com.moonsolstudios.kavvoro.billing

object PremiumCatalog {
    const val VORO_PRIME = "brainball_voro_prime"
    const val KAV_ZERO = "brainball_kav_zero"
    const val CHROME_VORO = "brainball_chrome_voro"
    const val NOVA_KAV = "brainball_nova_kav"

    val productToSkinId = linkedMapOf(
        VORO_PRIME to "prism_king",
        KAV_ZERO to "void_zero",
        CHROME_VORO to "chrome_lux",
        NOVA_KAV to "plasma_crown"
    )

    val skinToProductId = productToSkinId.entries.associate { (productId, skinId) -> skinId to productId }
    val productIds: Set<String> = productToSkinId.keys
}
