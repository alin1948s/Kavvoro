package com.moonsolstudios.kavvoro.ui

interface PrivacyBridge {
    fun showPrivacyOptions()
    fun openPrivacyPolicy()
    fun openTermsOfService()

    companion object {
        val NONE = object : PrivacyBridge {
            override fun showPrivacyOptions() = Unit
            override fun openPrivacyPolicy() = Unit
            override fun openTermsOfService() = Unit
        }
    }
}
