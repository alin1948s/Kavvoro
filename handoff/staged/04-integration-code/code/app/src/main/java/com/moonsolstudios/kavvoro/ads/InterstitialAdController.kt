package com.moonsolstudios.kavvoro.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdController(
    private val activity: Activity,
    private val adUnitId: String
) {
    private var interstitialAd: InterstitialAd? = null
    private var loading = false
    private var enabled = false

    fun enable() {
        if (enabled) return
        enabled = true
        load()
    }

    fun load() {
        if (!enabled) return
        if (loading || interstitialAd != null) return
        loading = true
        InterstitialAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitialAd = null
                }
            }
        )
    }

    fun show(onFinished: () -> Unit) {
        if (!enabled) {
            onFinished()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            load()
            onFinished()
            return
        }

        interstitialAd = null
        var finished = false
        fun finishOnce() {
            if (finished) return
            finished = true
            load()
            onFinished()
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finishOnce()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                finishOnce()
            }
        }
        ad.show(activity)
    }
}
