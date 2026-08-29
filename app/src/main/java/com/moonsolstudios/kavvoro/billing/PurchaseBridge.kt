package com.moonsolstudios.kavvoro.billing

interface PurchaseBridge {
    fun purchase(productId: String)
    fun restore()

    companion object {
        val NONE = object : PurchaseBridge {
            override fun purchase(productId: String) = Unit
            override fun restore() = Unit
        }
    }
}
