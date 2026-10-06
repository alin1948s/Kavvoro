package com.moonsolstudios.kavvoro

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.moonsolstudios.kavvoro.ads.AdBridge
import com.moonsolstudios.kavvoro.ads.InterstitialAdController
import com.moonsolstudios.kavvoro.ads.RewardedAdController
import com.moonsolstudios.kavvoro.billing.PlayBillingController
import com.moonsolstudios.kavvoro.playgames.PlayGamesLeaderboardController
import com.moonsolstudios.kavvoro.playgames.PlayGamesAccountController
import com.moonsolstudios.kavvoro.privacy.AgeGroup
import com.moonsolstudios.kavvoro.privacy.AgeProfileStore
import com.moonsolstudios.kavvoro.privacy.PrivacyAdsController
import com.moonsolstudios.kavvoro.startup.FirstFrameStartupGate
import com.moonsolstudios.kavvoro.ui.ChaosGameView
import com.moonsolstudios.kavvoro.ui.screens.agecheck.AgeCheckScreenView

class MainActivity : ComponentActivity() {
    private var gameView: ChaosGameView? = null
    private var ageCheckView: AgeCheckScreenView? = null
    private var billingController: PlayBillingController? = null
    private var privacyAdsController: PrivacyAdsController? = null
    private var accountController: PlayGamesAccountController? = null
    private var accountStarted = false
    private val firstFrameStartupGate = FirstFrameStartupGate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val sw = resources.configuration.smallestScreenWidthDp
        if (sw < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // On tablets, follow the device orientation sensor dynamically
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (gameView?.navigateBack() == true) return
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })
        hideSystemBars()

        val savedAgeGroup = AgeProfileStore.read(this)
        if (savedAgeGroup == null) {
            showAgeCheck(AgeCheckScreenView.AGE_OF_ADULTHOOD)
        } else {
            startGame(savedAgeGroup)
        }
    }

    private fun showAgeCheck(initialAge: Int) {
        val view = AgeCheckScreenView(this, initialAge = initialAge, onConfirm = { ageGroup ->
            AgeProfileStore.save(this, ageGroup)
            ageCheckView = null
            startGame(ageGroup)
        })
        ageCheckView = view
        setContentView(view)
    }

    private fun startGame(ageGroup: AgeGroup) {
        if (gameView != null) return
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val ads = InterstitialAdController(this, BuildConfig.ADMOB_INTERSTITIAL_ID)
        val rewardedAds = RewardedAdController(this, BuildConfig.ADMOB_REWARDED_CONTINUE_ID)
        val privacy = PrivacyAdsController(this, ads, rewardedAds)
        val billing = PlayBillingController(this)
        val account = PlayGamesAccountController(this)

        val view = ChaosGameView(
            context = this,
            adBridge = object : AdBridge {
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
            leaderboardBridge = PlayGamesLeaderboardController(this, account),
            accountBridge = account,
            privacyBridge = privacy,
            purchaseBridge = billing,
            onFirstFrameRendered = {
                firstFrameStartupGate.runOnce {
                    if (isFinishing || isDestroyed) {
                        return@runOnce
                    }
                    reportFullyDrawn()
                    accountStarted = true
                    account.start { state ->
                        gameView?.updateAccountState(state, account.profileId, account.displayName)
                    }
                    privacy.start(ageGroup)
                    billing.start()
                }
            }
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
        privacyAdsController = privacy
        accountController = account
        gameView = view
        setContentView(view)
        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        gameView?.resumeGame()
        if (accountStarted) {
            accountController?.refresh()
        }
        billingController?.refreshPurchases()
    }

    override fun onPause() {
        gameView?.pauseGame()
        super.onPause()
    }

    override fun onDestroy() {
        firstFrameStartupGate.cancel()
        privacyAdsController?.close()
        privacyAdsController = null
        accountStarted = false
        accountController = null
        billingController?.close()
        billingController = null
        gameView?.releaseGame()
        gameView = null
        ageCheckView = null
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ageCheckView?.onHostConfigurationChanged()
        hideSystemBars()
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

}
