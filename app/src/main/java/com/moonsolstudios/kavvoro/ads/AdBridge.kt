package com.moonsolstudios.kavvoro.ads

interface AdBridge {
    fun showInterstitial(onFinished: () -> Unit)
    fun showRewardedContinue(onRewarded: () -> Unit, onUnavailable: () -> Unit)

    companion object {
        val NONE: AdBridge = object : AdBridge {
            override fun showInterstitial(onFinished: () -> Unit) {
                onFinished()
            }

            override fun showRewardedContinue(onRewarded: () -> Unit, onUnavailable: () -> Unit) {
                onRewarded()
            }
        }
    }
}
