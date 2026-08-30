package com.moonsolstudios.kavvoro.privacy

import android.app.AlertDialog
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.widget.Toast
import com.google.android.gms.ads.AgeRestrictedTreatment
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.moonsolstudios.kavvoro.ads.InterstitialAdController
import com.moonsolstudios.kavvoro.ads.RewardedAdController
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.ui.PrivacyBridge
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

class PrivacyAdsController(
    private val activity: Activity,
    private val interstitialAds: InterstitialAdController,
    private val rewardedAds: RewardedAdController
) : PrivacyBridge {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
    private val mobileAdsInitialized = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val adsInitializationExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(
            {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                task.run()
            },
            "kavvoro-mobileads-init"
        )
    }

    fun start(ageGroup: AgeGroup) {
        if (closed.get()) return
        configureAgeTreatment(ageGroup)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(ageGroup != AgeGroup.ADULT)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initializeAdsIfAllowed()
                }
            },
            {
                initializeAdsIfAllowed()
            }
        )
        initializeAdsIfAllowed()
    }

    override fun showPrivacyOptions() {
        val choices = arrayOf(
            t("PRIVACY POLICY"),
            t("AD PRIVACY CHOICES"),
            t("CLOSE")
        )
        AlertDialog.Builder(activity)
            .setTitle(t("PRIVACY"))
            .setItems(choices) { dialog, which ->
                when (which) {
                    0 -> openPrivacyPolicy()
                    1 -> showAdPrivacyChoices()
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openPrivacyPolicy() {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, t("Privacy policy is temporarily unavailable."), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAdPrivacyChoices() {
        if (consentInformation.privacyOptionsRequirementStatus !=
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        ) {
            Toast.makeText(activity, t("Privacy options are not required for this profile."), Toast.LENGTH_SHORT).show()
            return
        }
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Toast.makeText(activity, t("Privacy options are temporarily unavailable."), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configureAgeTreatment(ageGroup: AgeGroup) {
        val builder = MobileAds.getRequestConfiguration().toBuilder()
        when (ageGroup) {
            AgeGroup.CHILD -> builder
                .setAgeRestrictedTreatment(AgeRestrictedTreatment.CHILD)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)

            AgeGroup.TEEN -> builder
                .setAgeRestrictedTreatment(AgeRestrictedTreatment.TEEN)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)

            AgeGroup.ADULT -> builder
                .setAgeRestrictedTreatment(AgeRestrictedTreatment.UNSPECIFIED)
                .setMaxAdContentRating(null)
        }
        MobileAds.setRequestConfiguration(builder.build())
    }

    private fun initializeAdsIfAllowed() {
        if (closed.get()) return
        if (!consentInformation.canRequestAds()) return
        if (!mobileAdsInitialized.compareAndSet(false, true)) return
        try {
            adsInitializationExecutor.execute {
                MobileAds.initialize(activity.applicationContext) {
                    if (!closed.get()) {
                        activity.runOnUiThread {
                            if (!closed.get()) {
                                interstitialAds.enable()
                                rewardedAds.enable()
                            }
                        }
                    }
                }
            }
        } catch (_: RejectedExecutionException) {
            // Activity teardown won the race with a late consent callback.
        }
    }

    fun close() {
        if (!closed.compareAndSet(false, true)) return
        adsInitializationExecutor.shutdown()
    }

    private fun t(value: String): String = KavvoroI18n.t(activity, value)

    companion object {
        const val PRIVACY_POLICY_URL = "https://brainroot-chaos-kavaroo.web.app/privacy/"
    }
}
