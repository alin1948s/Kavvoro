package com.moonsolstudios.kavvoro

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.moonsolstudios.kavvoro.ads.InterstitialAdController
import com.moonsolstudios.kavvoro.ads.RewardedAdController
import com.moonsolstudios.kavvoro.billing.PlayBillingController
import com.moonsolstudios.kavvoro.playgames.PlayGamesLeaderboardController
import com.moonsolstudios.kavvoro.privacy.AgeGateView
import com.moonsolstudios.kavvoro.privacy.AgeGroup
import com.moonsolstudios.kavvoro.privacy.AgeProfileStore
import com.moonsolstudios.kavvoro.privacy.PrivacyAdsController
import com.moonsolstudios.kavvoro.telemetry.KavvoroTelemetry
import com.moonsolstudios.kavvoro.ui.ChaosGameView
import com.moonsolstudios.kavvoro.ui.KavvoroSplashView
import java.util.Locale

class MainActivity : Activity() {
    private var gameView: ChaosGameView? = null
    private var billingController: PlayBillingController? = null
    private var splashView: KavvoroSplashView? = null
    private var launchAfterSplash: Runnable? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KavvoroTelemetry.initialize(this)
        KavvoroTelemetry.logEvent(KavvoroTelemetry.Event.APP_OPEN)
        if (BuildConfig.DEBUG && intent.getBooleanExtra(EXTRA_FORCE_TEST_CRASH, false)) {
            mainHandler.postDelayed({
                throw IllegalStateException("Kavvoro debug Crashlytics verification")
            }, 900L)
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        val savedAgeGroup = AgeProfileStore.read(this)
        showLaunchSplash(savedAgeGroup)
    }

    private fun showLaunchSplash(savedAgeGroup: AgeGroup?) {
        val splash = KavvoroSplashView(this)
        splashView = splash
        setContentView(splash)
        val launch = Runnable {
            splashView = null
            showEntryContent(savedAgeGroup)
            hideSystemBars()
        }
        launchAfterSplash = launch
        mainHandler.postDelayed(launch, KavvoroSplashView.SPLASH_DURATION_MS)
    }

    private fun showEntryContent(savedAgeGroup: AgeGroup?) {
        if (isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed)) return
        if (savedAgeGroup == null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            setContentView(AgeGateView(this) { ageGroup -> startGame(ageGroup, ageGateCompleted = true) })
        } else {
            startGame(savedAgeGroup, ageGateCompleted = false)
        }
        hideSystemBars()
    }

    private fun startGame(ageGroup: AgeGroup, ageGateCompleted: Boolean) {
        if (gameView != null) return
        if (ageGateCompleted) {
            KavvoroTelemetry.logEvent(
                KavvoroTelemetry.Event.AGE_GATE_COMPLETED,
                mapOf("result" to ageGroup.name.lowercase(Locale.ROOT))
            )
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val ads = InterstitialAdController(this, BuildConfig.ADMOB_INTERSTITIAL_ID)
        val rewardedAds = RewardedAdController(this, BuildConfig.ADMOB_REWARDED_CONTINUE_ID)
        val privacy = PrivacyAdsController(this, ads, rewardedAds)
        val billing = PlayBillingController(this)

        (application as GameApplication).initializePlayGames()
        privacy.start(ageGroup)

        val view = ChaosGameView(
            context = this,
            adBridge = object : ChaosGameView.AdBridge {
                override fun showInterstitial(onFinished: () -> Unit) {
                    runOnUiThread {
                        ads.show(onFinished)
                    }
                }

                override fun showRewardedContinue(onRewarded: () -> Unit, onUnavailable: () -> Unit) {
                    runOnUiThread {
                        rewardedAds.show(onRewarded, onUnavailable)
                    }
                }
            },
            leaderboardBridge = PlayGamesLeaderboardController(this),
            privacyBridge = privacy,
            purchaseBridge = billing
        )
        billing.listener = object : PlayBillingController.Listener {
            override fun onPremiumPricesUpdated(pricesByProductId: Map<String, String>) {
                view.updatePremiumPrices(pricesByProductId)
            }

            override fun onPremiumEntitlementsSynced(ownedProductIds: Set<String>) {
                view.syncPremiumEntitlements(ownedProductIds)
            }

            override fun onBillingMessage(message: String) {
                view.showBillingMessage(message)
            }
        }
        billingController = billing
        gameView = view
        setContentView(view)
        billing.start()
        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        gameView?.resumeGame()
        billingController?.refreshPurchases()
    }

    override fun onPause() {
        gameView?.pauseGame()
        super.onPause()
    }

    override fun onDestroy() {
        launchAfterSplash?.let { mainHandler.removeCallbacks(it) }
        launchAfterSplash = null
        splashView = null
        billingController?.close()
        billingController = null
        gameView?.releaseGame()
        gameView = null
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.decorView.windowInsetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private companion object {
        const val EXTRA_FORCE_TEST_CRASH = "force_test_crash"
    }

}
