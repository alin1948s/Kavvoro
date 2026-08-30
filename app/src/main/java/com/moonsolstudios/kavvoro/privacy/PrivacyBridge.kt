package com.moonsolstudios.kavvoro.privacy

interface PrivacyBridge {
    fun showPrivacyOptions()
    fun openPrivacyPolicy()
    fun openTermsOfService()
    fun openDataDeletion()
    fun openAbout()

    companion object {
        val NONE = object : PrivacyBridge {
            override fun showPrivacyOptions() = Unit
            override fun openPrivacyPolicy() = Unit
            override fun openTermsOfService() = Unit
            override fun openDataDeletion() = Unit
            override fun openAbout() = Unit
        }
    }
}
