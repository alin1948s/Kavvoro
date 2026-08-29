package com.moonsolstudios.kavvoro.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdController(
    private val activity: Activity,
    private val adUnitId: String
) {
    private var rewardedAd: RewardedAd? = null
    private var loading = false
    private var enabled = false

    fun enable() {
        if (enabled) return
        enabled = true
        load()
    }

    fun load() {
        if (!enabled) return
        if (loading || rewardedAd != null) return
        loading = true
        RewardedAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loading = false
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    rewardedAd = null
                }
            }
        )
    }

    fun show(onRewarded: () -> Unit, onUnavailable: () -> Unit) {
        if (!enabled) {
            onUnavailable()
            return
        }
        val ad = rewardedAd
        if (ad == null) {
            load()
            onUnavailable()
            return
        }

        rewardedAd = null
        var rewardEarned = false
        var finished = false
        fun finishOnce() {
            if (finished) return
            finished = true
            load()
            if (rewardEarned) {
                onRewarded()
            } else {
                onUnavailable()
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finishOnce()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                finishOnce()
            }
        }
        ad.show(activity) {
            rewardEarned = true
        }
    }
}
