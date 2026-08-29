package com.moonsolstudios.kavvoro.ui

interface PrivacyBridge {
    fun showPrivacyOptions()

    companion object {
        val NONE = object : PrivacyBridge {
            override fun showPrivacyOptions() = Unit
        }
    }
}
