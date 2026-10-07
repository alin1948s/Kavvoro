package com.moonsolstudios.kavvoro.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewConfiguration
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.BuildConfig
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import com.moonsolstudios.kavvoro.ui.layout.ViewportLayoutCalculator
import com.moonsolstudios.kavvoro.ads.AdBridge
import com.moonsolstudios.kavvoro.ads.AdPolicyController
import com.moonsolstudios.kavvoro.audio.KavvoroSoundEngine
import com.moonsolstudios.kavvoro.audio.MusicTrack
import com.moonsolstudios.kavvoro.audio.SoundEvent
import com.moonsolstudios.kavvoro.billing.PremiumCatalog
import com.moonsolstudios.kavvoro.billing.PurchaseBridge
import com.moonsolstudios.kavvoro.model.AdAction
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.CollectionSort
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.model.NextReward
import com.moonsolstudios.kavvoro.model.RenderProfile
import com.moonsolstudios.kavvoro.model.Screen
import com.moonsolstudios.kavvoro.model.SettingsButton
import com.moonsolstudios.kavvoro.model.SettingsTab
import com.moonsolstudios.kavvoro.playgames.LeaderboardBoard
import com.moonsolstudios.kavvoro.playgames.LeaderboardBridge
import com.moonsolstudios.kavvoro.playgames.AccountBridge
import com.moonsolstudios.kavvoro.playgames.AccountProfileDisplay
import com.moonsolstudios.kavvoro.playgames.AccountState
import com.moonsolstudios.kavvoro.privacy.PrivacyBridge
import com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuActionListener
import com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuTouchController
import com.moonsolstudios.kavvoro.ui.screens.modals.TabletOrientationPromptController
import com.moonsolstudios.kavvoro.ui.screens.home.HomeAccessibilityTouchHelper
import com.moonsolstudios.kavvoro.ui.screens.home.HomeLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.ad.AdScreenRenderer
import com.moonsolstudios.kavvoro.ui.screens.ad.AdScreenTouchController
import com.moonsolstudios.kavvoro.ui.screens.collection.BallSkinRenderer
import com.moonsolstudios.kavvoro.ui.screens.collection.CollectionLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.language.LanguageSelectorLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.leaderboards.LeaderboardLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.modals.ShareExportOverlayRenderer
import com.moonsolstudios.kavvoro.ui.screens.home.ModePickerRenderer
import com.moonsolstudios.kavvoro.ui.screens.settings.SettingsBreakpoint
import com.moonsolstudios.kavvoro.ui.screens.settings.SettingsHeaderMetrics
import com.moonsolstudios.kavvoro.ui.screens.settings.SettingsLayoutCalculator
import com.moonsolstudios.kavvoro.model.DailyRiftButton
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.AtmosphereRenderer
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.screens.modals.DailyRiftRewardRenderer
import com.moonsolstudios.kavvoro.ui.screens.missions.MissionsScreenController
import com.moonsolstudios.kavvoro.ui.screens.modals.TabletOrientationPromptRenderer
import com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuRenderer
import com.moonsolstudios.kavvoro.ui.render.BrandTitleRenderer
import com.moonsolstudios.kavvoro.ui.screens.collection.CollectionUiRenderer
import com.moonsolstudios.kavvoro.ui.screens.leaderboards.LeaderboardUiRenderer
import com.moonsolstudios.kavvoro.ui.screens.language.LanguageSelectorRenderer
import com.moonsolstudios.kavvoro.ui.screens.home.ModePickerLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.gameplay.GameplayArenaRenderer
import com.moonsolstudios.kavvoro.ui.screens.gameplay.GameplayHudRenderer
import com.moonsolstudios.kavvoro.ui.screens.gameplay.GameplayTouchController
import com.moonsolstudios.kavvoro.ui.screens.gameplay.ModeWarning
import com.moonsolstudios.kavvoro.ui.screens.gameplay.LevelArchetype
import com.moonsolstudios.kavvoro.ui.screens.outcome.OutcomeTouchController
import com.moonsolstudios.kavvoro.ui.screens.outcome.OutcomeUiRenderer
import com.moonsolstudios.kavvoro.ui.tutorial.TutorialRenderer
import com.moonsolstudios.kavvoro.ui.tutorial.TutorialTouchController
import com.moonsolstudios.kavvoro.ui.screens.settings.SettingsUiRenderer
import com.moonsolstudios.kavvoro.ui.render.UiWidgetRenderer
import com.moonsolstudios.kavvoro.ui.tutorial.TutorialInputGate
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.GameplayScoreCalculator
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsEngine
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.PhysicsOutcome
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.ReplayRecorder
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.engine.STAGE_WIDTH
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.i18n.TutorialCopy
import com.moonsolstudios.kavvoro.repository.BallSkinCatalog
import com.moonsolstudios.kavvoro.repository.AccountProgressStore
import com.moonsolstudios.kavvoro.repository.GameProgressRepository
import com.moonsolstudios.kavvoro.repository.DailyMissionsRepository
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.BEST_STREAK_KEY
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.DEFAULT_SKIN_ID
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.HYPE_BANK_KEY
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.MUSIC_MUTED_KEY
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.SELECTED_SKIN_KEY
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.SFX_MUTED_KEY
import com.moonsolstudios.kavvoro.repository.GameProgressRepository.Companion.SHARE_COUNT_KEY
import com.moonsolstudios.kavvoro.share.ReplayShareController
import com.moonsolstudios.kavvoro.share.ReplayVideoExporter
import com.moonsolstudios.kavvoro.ui.screens.language.LanguageTouchController
import com.moonsolstudios.kavvoro.ui.screens.language.LanguageSelectorLayout
import com.moonsolstudios.kavvoro.ui.screens.leaderboards.LeaderboardTouchController
import com.moonsolstudios.kavvoro.ui.controller.AdaptiveQualityController
import com.moonsolstudios.kavvoro.ui.screens.collection.CollectionTouchController
import com.moonsolstudios.kavvoro.ui.controller.GameLoopDirector
import com.moonsolstudios.kavvoro.ui.screens.settings.SettingsTouchController
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

// This SurfaceView is created programmatically because its runtime bridges cannot be inflated from XML.
@SuppressLint("ViewConstructor")
class ChaosGameView(
    context: Context,
    private val adBridge: AdBridge = AdBridge.NONE,
    private val leaderboardBridge: LeaderboardBridge = LeaderboardBridge.NONE,
    private val accountBridge: AccountBridge = AccountBridge.NONE,
    private val privacyBridge: PrivacyBridge = PrivacyBridge.NONE,
    private val purchaseBridge: PurchaseBridge = PurchaseBridge.NONE,
    private val onFirstFrameRendered: () -> Unit = {}
) : SurfaceView(context), SurfaceHolder.Callback {
    private val lock = Any()
    private val firstFrameReported = AtomicBoolean(false)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPath = Path()
    private val scratch = RectF()
    private val physics = PhysicsEngine()
    private val replay = ReplayRecorder()
    private val audio = KavvoroSoundEngine(context.applicationContext)
    private val accountProgressStore = AccountProgressStore(context)
    private var prefs: SharedPreferences = accountProgressStore.activePreferences()
    private var sfxMuted = prefs.getBoolean(SFX_MUTED_KEY, false)
    private var musicMuted = prefs.getBoolean(MUSIC_MUTED_KEY, false)
    private val ballSkins = BallSkinCatalog.ALL_SKINS
    private val languageTypeface by lazy {
        androidx.core.content.res.ResourcesCompat.getFont(context, R.font.oxanium)
    }
    @Volatile
    private var running = false
    private var loopThread: Thread? = null
    private var lastFrameNanos = 0L
    private val renderProfile = AdaptiveQualityController.detectRenderProfile()
    private var adaptiveQuality = AdaptiveQualityController.initialAdaptiveQuality(renderProfile)
    private var consecutiveSlowFrames = 0

    private var viewWidth = 1
    private var viewHeight = 1
    private var uiDensity = resources.displayMetrics.density
    private var scale = 1f
    private var stageLeft = 0f
    private var stageHeight = 17.78f
    private var levelIndex = 1
    private var level: LevelSpec = LevelDirector.create(levelIndex, stageHeight)

    private var screen = debugLaunchScreen()
    private var menuState = MenuState.MODES
    private var selectedMenuMode = GameMode.CLASSIC
    private var gameMode = GameMode.CLASSIC
    private var menuPulse = 0f
    private var state = GameState.READY
    private val playerLine = mutableListOf<Point2>()
    private val liveBallTrail = mutableListOf<Point2>()
    private var replayFrames: List<PhysicsFrame> = emptyList()
    private var ball = level.start
    private var simElapsed = 0f
    private var stateElapsed = 0f
    private var inkUsed = 0f
    private var riftEnergy = 1f
    private var riftActive = false
    private var riftAnchor: Point2? = null
    private var riftHoldSeconds = 0f
    private var riftTapReleaseTimer = 0f
    private var chainCount = 0
    private var chainCharge = 0f
    private var maxChain = 0
    private var pulseIntensity = 0f
    private var flash = 0f
    private var finishPulse = 0f
    private var trauma = 0f
    private var riftBreakTimer = 0f
    private var lastRiftBreak = false
    private var lastRiftBreakBonus = 0
    private var lastRiftBreakReason = ""
    private var lastDailyBonus = 0
    private var lastStreakMilestoneBonus = 0
    private var lastScore: RunScore? = null
    private var lastHypeScore = 0
    private var streak = prefs.getInt("streak_classic", prefs.getInt("clear_streak", 0))
    private var activeButton = ButtonId.NONE
    private val tutorialTouchController = TutorialTouchController()
    private val tutorialInputGate get() = tutorialTouchController.inputGate
    private val tutorialCardBounds get() = tutorialTouchController.cardBounds
    private val tutorialTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var tutorialCardVisible = false
    private var isDailyRiftModalVisible = false
    private var pendingAdAction = AdAction.NONE
    private var pendingResumeMode = GameMode.CLASSIC
    private var adReason = ""
    private var adLoading = false
    private var exportingShare = false
    private var selectedSkinId = prefs.getString(SELECTED_SKIN_KEY, DEFAULT_SKIN_ID) ?: DEFAULT_SKIN_ID
    private var collectionFocusSkinId = selectedSkinId
    private val premiumPricesBySkin = mutableMapOf<String, String>()
    private var progressRepository = GameProgressRepository(prefs, ballSkins, premiumPricesBySkin, ::t)
    private val missionsScreenController: MissionsScreenController = MissionsScreenController(
        repository = DailyMissionsRepository(prefs),
        onTouch = { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) },
        onBack = {
            synchronized(lock) {
                missionsScreenController.reset()
                screen = Screen.MENU
                menuState = MenuState.MODES
                triggerScreenTransition(0xFF8AA6FF.toInt())
            }
        },
        onReward = { reward ->
            synchronized(lock) {
                progressRepository.addHype(reward)
                audio.playEvent(SoundEvent.UNLOCK, selectedBallIndex())
                hapticSequence(
                    HapticFeedbackCompat.confirm to 0L,
                    HapticFeedbackConstants.LONG_PRESS to 80L
                )
                triggerScreenTransition(KavvoroPalette.gold)
            }
        },
        onRejectedClaim = { synchronized(lock) { performHapticFeedback(HapticFeedbackCompat.reject) } }
    )

    private val homeMenuController = HomeMenuTouchController(
        object : HomeMenuActionListener {
            override fun onMenuButtonSelected(button: MenuButton) {
                handleMenuButton(button)
            }
            override fun onDailyRiftClaim() {
                if (!progressRepository.dailyRiftBonusClaimed()) {
                    progressRepository.claimDailyRiftHome()
                    audio.playEvent(SoundEvent.UNLOCK, selectedBallIndex())
                    hapticSequence(
                        HapticFeedbackCompat.confirm to 0L,
                        HapticFeedbackConstants.LONG_PRESS to 80L
                    )
                    triggerScreenTransition(0xFFFFCF4A.toInt())
                } else {
                    isDailyRiftModalVisible = false
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            }
            override fun onDailyRiftOpenCollection() {
                isDailyRiftModalVisible = false
                screen = Screen.COLLECTION
                collectionMessage = ""
                collectionMessageTimer = 0f
                collectionFocusSkinId = selectedBallSkin().id
                activeMenuButton = MenuButton.NONE
                triggerScreenTransition(selectedBallSkin().lineColor)
            }
            override fun onDailyRiftDismissed() {
                isDailyRiftModalVisible = false
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            override fun performHaptic(feedbackConstant: Int) {
                performHapticFeedback(feedbackConstant)
            }
        }
    )

    private var activeMenuButton: MenuButton
        get() = homeMenuController.activeMenuButton
        set(value) { homeMenuController.activeMenuButton = value }

    private var activeDailyRiftButton: DailyRiftButton
        get() = homeMenuController.activeDailyRiftButton
        set(value) { homeMenuController.activeDailyRiftButton = value }

    private var menuBallVelocityX: Float
        get() = homeMenuController.menuBallVelocityX
        set(value) { homeMenuController.menuBallVelocityX = value }

    private var menuBallVelocityY: Float
        get() = homeMenuController.menuBallVelocityY
        set(value) { homeMenuController.menuBallVelocityY = value }

    private val dailyRiftCardBounds get() = homeMenuController.dailyRiftCardBounds
    private val dailyRiftClaimButtonRect get() = homeMenuController.dailyRiftClaimButtonRect
    private val dailyRiftActionButtonRect get() = homeMenuController.dailyRiftActionButtonRect
    private val dailyRiftCloseButtonRect get() = homeMenuController.dailyRiftCloseButtonRect
    private var rewardMessage = ""
    private var collectionMessage = ""
    private var collectionMessageTimer = 0f
    private var powerMessage = ""
    private var powerMessageTimer = 0f
    private var bounceSoundCooldown = 0f
    private var pulseFeedbackCooldown = 0f
    private var screenTransitionTimer = 0f
    private var screenTransitionAccent = 0xFF1DE8C8.toInt()
    private var collectionScroll = 0f
    private var collectionMaxScroll = 0f
    private var collectionTouchY = 0f
    private var collectionLastY = 0f
    private var collectionDragging = false
    private var collectionFilter = CollectionFilter.ALL
    private var collectionSort = CollectionSort.AURA_DESC
    private var activeCollectionIndex = -1
    private var leaderboardMessage = ""
    private var leaderboardMessageTimer = 0f
    private var activeLeaderboardIndex = -1
    @Volatile
    private var accountState = accountBridge.state
    @Volatile
    private var accountDisplayName: String? = accountBridge.displayName

    private val menuContinueButton get() = homeMenuController.menuContinueButton
    private val menuBackButton get() = homeMenuController.menuBackButton
    private val characterRect get() = homeMenuController.characterRect
    private val menuClassicCard get() = homeMenuController.menuClassicCard
    private val menuChaosCard get() = homeMenuController.menuChaosCard
    private val menuClassicContinueButton get() = homeMenuController.menuClassicContinueButton
    private val menuClassicNewButton get() = homeMenuController.menuClassicNewButton
    private val menuChaosContinueButton get() = homeMenuController.menuChaosContinueButton
    private val menuChaosNewButton get() = homeMenuController.menuChaosNewButton
    private val menuChaosStartButton get() = homeMenuController.menuChaosStartButton
    private val homeLayoutCalculator = HomeLayoutCalculator()
    private val homeAccessibilityHelper = HomeAccessibilityTouchHelper(
        host = this,
        calculator = homeLayoutCalculator,
        isHomeSurfaceVisible = { screen == Screen.MENU && menuState == MenuState.MODES },
        getStreak = { bestStreak() },
        getLevel = { level.index },
        getCoinsText = { formatHypeAmount(hypeBalance()) },
        isDailyCheckReady = { !progressRepository.dailyRiftBonusClaimed() },
        onPlayClicked = { handleMenuButton(MenuButton.PLAY) },
        onSettingsClicked = { handleMenuButton(MenuButton.SETTINGS) },
        onSkinsClicked = { handleMenuButton(MenuButton.COLLECTION) },
        onMissionsClicked = { handleMenuButton(MenuButton.MISSIONS) },
        onCoinsClicked = { handleMenuButton(MenuButton.DAILY_RIFT) },
        onLeaderboardClicked = { handleMenuButton(MenuButton.LEADERBOARDS) }
    )
    private val collectionBackButton get() = CollectionTouchController.backButtonRect
    private val collectionRestoreButton get() = CollectionTouchController.restoreButtonRect
    private val collectionFilterRects get() = CollectionTouchController.filterRects
    private val collectionItemRects get() = CollectionTouchController.itemRects
    private val leaderboardBackButton get() = LeaderboardTouchController.backButtonRect
    private val leaderboardItemRects get() = LeaderboardTouchController.itemRects
    private val languageBackButton get() = LanguageTouchController.backButtonRect
    private val languageFooterRect get() = LanguageTouchController.footerRect
    private val languageDeckRect get() = LanguageTouchController.deckRect
    private val languageItemRects get() = LanguageTouchController.itemRects
    private var latestLanguageLayout: LanguageSelectorLayout?
        get() = LanguageTouchController.latestLayout
        set(value) { LanguageTouchController.latestLayout = value }
    private var activeLanguageIndex = -1
    private var languageScroll = 0f
    private var languageMaxScroll = 0f
    private var languageTouchY = 0f
    private var languageLastY = 0f
    private var languageDragging = false
    private var languageReturnScreen = Screen.MENU
    private var settingsScroll = 0f
    private var settingsMaxScroll = 0f
    private var settingsTouchY = 0f
    private var settingsLastY = 0f
    private var settingsDragging = false
    private var activeSettingsButton = SettingsButton.NONE
    private var settingsResetConfirm = false
    private var settingsMasterVolume = prefs.getInt(GameProgressRepository.SETTINGS_MASTER_VOLUME_KEY, 100).coerceIn(0, 100)
    private var settingsMusicVolume = prefs.getInt(GameProgressRepository.SETTINGS_MUSIC_VOLUME_KEY, 100).coerceIn(0, 100)
    private var settingsSfxVolume = prefs.getInt(GameProgressRepository.SETTINGS_SFX_VOLUME_KEY, 100).coerceIn(0, 100)
    private var settingsHapticEnabled = prefs.getBoolean(GameProgressRepository.SETTINGS_HAPTIC_KEY, true)
    private var settingsScreenShake = prefs.getBoolean(GameProgressRepository.SETTINGS_SCREEN_SHAKE_KEY, true)
    private var settingsPerformanceMode = prefs.getBoolean(GameProgressRepository.SETTINGS_PERFORMANCE_KEY, false)
    private var activeSettingsTab = debugLaunchSettingsTab()
    private val settingsLayoutCalculator get() = SettingsTouchController.layoutCalculator
    private val homeButton get() = GameplayTouchController.homeButtonRect
    private val restartButton get() = GameplayTouchController.restartButtonRect
    private val sfxButton get() = GameplayTouchController.sfxButtonRect
    private val musicButton get() = GameplayTouchController.musicButtonRect
    private val shareButton get() = GameplayTouchController.shareButtonRect
    private val nextButton get() = GameplayTouchController.nextButtonRect
    private val resultNextButton get() = OutcomeTouchController.resultNextButtonRect
    private val resultRetryButton get() = OutcomeTouchController.resultRetryButtonRect
    private val resultShareButton get() = OutcomeTouchController.resultShareButtonRect
    private val adButton get() = AdScreenTouchController.adButtonRect
    private val tutorialStartButton get() = tutorialTouchController.startButtonRect
    private val tabletOrientationPromptController = TabletOrientationPromptController().apply {
        isEnabled = false
    }

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        keepScreenOn = true
        val oxanium = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.oxanium)
        } catch (_: Exception) {
            null
        }
        val spaceGrotesk = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.space_grotesk)
        } catch (_: Exception) {
            null
        }
        val manrope = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, R.font.manrope)
        } catch (_: Exception) {
            null
        }
        oxanium?.let {
            BrandTitleRenderer.initTypeface(it)
        }
        AssetResourceManager.initCustomTypefaces(oxanium, spaceGrotesk, manrope)
        textPaint.typeface = spaceGrotesk ?: oxanium ?: android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        audio.setSfxMuted(sfxMuted)
        audio.setMusicMuted(musicMuted)
        audio.setVolumes(settingsMasterVolume, settingsMusicVolume, settingsSfxVolume)
        isHapticFeedbackEnabled = settingsHapticEnabled
        audio.setLanguageCode(KavvoroI18n.audioLanguageCode(context))
        syncMusicTrack()
        configureStage(width.coerceAtLeast(1), height.coerceAtLeast(1), reset = false)
        androidx.core.view.ViewCompat.setAccessibilityDelegate(this, homeAccessibilityHelper)
        AssetResourceManager.preloadHomeAssets(resources, context)
    }

    fun resumeGame() {
        audio.setPaused(false)
        syncMusicTrack()
        tabletOrientationPromptController.onResume()
        if (running) return
        running = true
        lastFrameNanos = System.nanoTime()
        loopThread = Thread(::gameLoop, "one-line-chaos-loop").apply {
            priority = Thread.NORM_PRIORITY + 1
            start()
        }
    }

    fun pauseGame() {
        audio.setPaused(true)
        running = false
        val thread = loopThread
        if (thread != null && thread != Thread.currentThread()) {
            try {
                thread.join(700)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        loopThread = null
    }

    fun releaseGame() {
        pauseGame()
        audio.release()
        recycleScaledBackgrounds()
    }

    fun navigateBack(): Boolean = synchronized(lock) {
        if (isDailyRiftModalVisible) {
            isDailyRiftModalVisible = false
            return true
        }
        when (screen) {
            Screen.GAME -> {
                exitToMenu()
                true
            }
            Screen.COLLECTION,
            Screen.LEADERBOARDS,
            Screen.MISSIONS -> {
                screen = Screen.MENU
                menuState = MenuState.MODES
                triggerScreenTransition(0xFF8AA6FF.toInt())
                true
            }
            Screen.SETTINGS -> {
                if (settingsResetConfirm) {
                    settingsResetConfirm = false
                } else {
                    navigateToMenuFromSettings()
                }
                true
            }
            Screen.LANGUAGE -> {
                screen = languageReturnScreen
                triggerScreenTransition(0xFF45F2FF.toInt())
                true
            }
            Screen.MENU -> {
                if (menuState == MenuState.MODE_ACTION) {
                    menuState = MenuState.MODES
                    triggerScreenTransition(0xFF8AA6FF.toInt())
                    true
                } else {
                    false
                }
            }
            Screen.AD -> false
        }
    }

    fun updatePremiumPrices(pricesByProductId: Map<String, String>) {
        post {
            synchronized(lock) {
                prefs.edit {
                    pricesByProductId.forEach { (productId, price) ->
                        val skinId = PremiumCatalog.productToSkinId[productId] ?: return@forEach
                        premiumPricesBySkin[skinId] = price
                        putString(GameProgressRepository.premiumPriceKey(skinId), price)
                    }
                }
            }
        }
    }

    fun syncPremiumEntitlements(ownedProductIds: Set<String>) {
        post {
            synchronized(lock) {
                val ownedSkinIds = ownedProductIds.mapNotNull(PremiumCatalog.productToSkinId::get).toSet()
                prefs.edit {
                    PremiumCatalog.skinToProductId.keys.forEach { skinId ->
                        putBoolean(GameProgressRepository.purchasedSkinKey(skinId), skinId in ownedSkinIds)
                    }
                }
            }
        }
    }

    fun showBillingMessage(message: String) {
        post {
            synchronized(lock) {
                collectionMessage = message
                collectionMessageTimer = 3.4f
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        resumeGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(lock) {
            configureStage(width, height, reset = true)
            tabletOrientationPromptController.update(0f, context, width, height)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        post {
            tabletOrientationPromptController.update(0f, context, viewWidth, viewHeight)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pauseGame()
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (screen == Screen.MENU && menuState == MenuState.MODES) {
            if (homeAccessibilityHelper.dispatchHoverEvent(event)) {
                return true
            }
        }
        return super.dispatchHoverEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var pendingAction: (() -> Unit)? = null
        synchronized(lock) {
            if (tabletOrientationPromptController.handleTouchEvent(event)) {
                return true
            }
            when (screen) {
                Screen.MENU -> handleMenuTouch(event)
                Screen.COLLECTION -> handleCollectionTouch(event)
                Screen.LEADERBOARDS -> handleLeaderboardTouch(event)
                Screen.MISSIONS -> pendingAction = missionsScreenController.handleTouch(event, viewWidth, viewHeight, uiDensity)
                Screen.LANGUAGE -> handleLanguageTouch(event)
                Screen.SETTINGS -> pendingAction = handleSettingsTouch(event)
                Screen.AD -> pendingAction = AdScreenTouchController.handleTouch(
                    event = event,
                    viewWidth = viewWidth.toFloat(),
                    viewHeight = viewHeight.toFloat(),
                    dp = uiDensity,
                    adButton = adButton,
                    activeButton = activeButton,
                    setActiveButton = { activeButton = it },
                    onAction = ::handleButton
                )
                Screen.GAME -> pendingAction = if (missionsScreenController.handleGamePopupTouch(event)) null else GameplayTouchController.handleTouch(
                    event = event,
                    activeButton = activeButton,
                    setActiveButton = { activeButton = it },
                    buttonAt = ::buttonAt,
                    handleTutorialTouch = ::handleTutorialTouch,
                    riftTapReleaseTimer = riftTapReleaseTimer,
                    startRiftControl = ::startRiftControl,
                    moveRiftControl = ::moveRiftControl,
                    releaseRiftControl = { releaseRiftControl() },
                    onButtonAction = ::handleButton
                )
            }
        }
        pendingAction?.invoke()
        if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
        return true
    }

    fun updateAccountState(
        next: AccountState,
        profileId: String? = accountBridge.profileId,
        displayName: String? = accountBridge.displayName
    ) {
        post {
            synchronized(lock) {
                val nextPrefs = when (next) {
                    AccountState.SIGNED_IN -> accountProgressStore.onSignedIn(profileId)
                    AccountState.SIGNED_OUT,
                    AccountState.UNAVAILABLE -> accountProgressStore.onSignedOut()
                    AccountState.CONNECTING -> null
                }
                if (nextPrefs != null && nextPrefs !== prefs) {
                    prefs = nextPrefs
                    progressRepository = GameProgressRepository(prefs, ballSkins, premiumPricesBySkin, ::t)
                    missionsScreenController.replaceRepository(DailyMissionsRepository(prefs))
                    reloadProfileState()
                }
                accountState = next
                accountDisplayName = displayName
                postInvalidate()
            }
        }
    }

    private fun reloadProfileState() {
        sfxMuted = prefs.getBoolean(SFX_MUTED_KEY, false)
        musicMuted = prefs.getBoolean(MUSIC_MUTED_KEY, false)
        settingsMasterVolume = prefs.getInt(GameProgressRepository.SETTINGS_MASTER_VOLUME_KEY, 100).coerceIn(0, 100)
        settingsMusicVolume = prefs.getInt(GameProgressRepository.SETTINGS_MUSIC_VOLUME_KEY, 100).coerceIn(0, 100)
        settingsSfxVolume = prefs.getInt(GameProgressRepository.SETTINGS_SFX_VOLUME_KEY, 100).coerceIn(0, 100)
        settingsHapticEnabled = prefs.getBoolean(GameProgressRepository.SETTINGS_HAPTIC_KEY, true)
        settingsScreenShake = prefs.getBoolean(GameProgressRepository.SETTINGS_SCREEN_SHAKE_KEY, true)
        settingsPerformanceMode = prefs.getBoolean(GameProgressRepository.SETTINGS_PERFORMANCE_KEY, false)
        streak = prefs.getInt("streak_classic", prefs.getInt("clear_streak", 0))
        selectedSkinId = prefs.getString(SELECTED_SKIN_KEY, DEFAULT_SKIN_ID) ?: DEFAULT_SKIN_ID
        if (ballSkins.none { it.id == selectedSkinId && progressRepository.isSkinUnlocked(it) }) {
            selectedSkinId = DEFAULT_SKIN_ID
        }
        collectionFocusSkinId = selectedSkinId
        selectedMenuMode = GameMode.CLASSIC
        gameMode = GameMode.CLASSIC
        levelIndex = progressRepository.modeProgress(gameMode)
        menuState = MenuState.MODES
        settingsResetConfirm = false
        if (screen == Screen.GAME) {
            screen = Screen.MENU
        }
        state = GameState.READY
        audio.setSfxMuted(sfxMuted)
        audio.setMusicMuted(musicMuted)
        audio.setVolumes(settingsMasterVolume, settingsMusicVolume, settingsSfxVolume)
        isHapticFeedbackEnabled = settingsHapticEnabled
        configureStage(viewWidth, viewHeight, reset = true)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun handleTutorialTouch(event: MotionEvent): Boolean =
        tutorialTouchController.handleTouch(
            event = event,
            tutorialCardVisible = tutorialCardVisible,
            tutorialCardBounds = tutorialCardBounds,
            tutorialStartButton = tutorialStartButton,
            touchSlop = tutorialTouchSlop,
            onDismissOnly = { dismissTutorialCard() },
            onDismissAndPlay = { x, y ->
                if (dismissTutorialCard()) startRiftControl(x, y)
            }
        )

    private fun resetTutorialGesture() {
        tutorialTouchController.reset()
    }

    private fun dismissTutorialCard(): Boolean {
        prefs.edit { putBoolean(tutorialAcknowledgementKey(), true) }
        tutorialCardVisible = false
        tutorialCardBounds.setEmpty()
        tutorialStartButton.setEmpty()
        resetTutorialGesture()
        stateElapsed = 0f
        performHapticFeedback(HapticFeedbackCompat.confirm)
        audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
        return true
    }

    private fun handleMenuTouch(event: MotionEvent) {
        layoutMenuButtons()
        updateMenuPreviewGeometry()
        homeMenuController.handleTouchEvent(
            event = event,
            isDailyRiftModalVisible = isDailyRiftModalVisible,
            dailyClaimed = progressRepository.dailyRiftBonusClaimed(),
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            dp = uiDensity,
            buttonAt = { x, y -> menuButtonAt(x, y) }
        )
    }

    private fun updateMenuBallPhysics(dt: Float) {
        homeMenuController.updatePhysics(
            dt = dt,
            selectedMode = selectedMenuMode,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            dp = uiDensity
        )
    }

    private fun gameLoop() {
        while (running) {
            val now = System.nanoTime()
            val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 1f / 24f)
            lastFrameNanos = now

            try {
                synchronized(lock) {
                    update(dt)
                }

                if (holder.surface.isValid) {
                    var canvas: Canvas? = null
                    try {
                        canvas = lockRenderCanvas()
                        if (canvas != null) {
                            synchronized(lock) {
                                drawGame(canvas)
                            }
                        }
                    } catch (_: IllegalArgumentException) {
                        running = false
                    } finally {
                        if (canvas != null) {
                            holder.unlockCanvasAndPost(canvas)
                            reportFirstFrameRendered()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChaosGameView", "Error in gameLoop", e)
                running = false
            }

            val frameTime = (System.nanoTime() - now) / 1_000_000
            adjustAdaptiveQuality(frameTime.toFloat())
            val sleepMs = (targetFrameMillis() - frameTime).coerceAtLeast(2L)
            SystemClock.sleep(sleepMs)
        }
    }

    private fun reportFirstFrameRendered() {
        if (!firstFrameReported.compareAndSet(false, true)) return
        post(onFirstFrameRendered)
    }

    private fun lockRenderCanvas(): Canvas? = try {
        GameLoopDirector.lockRenderCanvas(holder)
    } catch (_: Exception) {
        null
    }

    private fun adjustAdaptiveQuality(frameTimeMs: Float) {
        val result = AdaptiveQualityController.adjustAdaptiveQuality(
            frameTimeMs = frameTimeMs,
            profile = renderProfile,
            currentAdaptiveQuality = adaptiveQuality,
            currentSlowFrames = consecutiveSlowFrames,
            targetMillis = targetFrameMillis()
        )
        adaptiveQuality = result.adaptiveQuality
        consecutiveSlowFrames = result.consecutiveSlowFrames
    }

    private fun targetFrameMillis(): Long =
        AdaptiveQualityController.targetFrameMillis(
            profile = renderProfile,
            screen = screen,
            state = state,
            screenTransitionTimer = screenTransitionTimer,
            exportingShare = exportingShare
        )

    private fun performanceLite(): Boolean =
        AdaptiveQualityController.isPerformanceLite(settingsPerformanceMode, renderProfile, adaptiveQuality)

    private fun richEffects(): Boolean =
        AdaptiveQualityController.isRichEffects(settingsPerformanceMode, renderProfile, adaptiveQuality)

    private fun fullEffects(): Boolean =
        AdaptiveQualityController.isFullEffects(settingsPerformanceMode, renderProfile, adaptiveQuality)
    private fun syncMusicTrack() {
        val track = when (screen) {
            Screen.MENU,
            Screen.COLLECTION,
            Screen.LEADERBOARDS,
            Screen.MISSIONS,
            Screen.LANGUAGE,
            Screen.SETTINGS -> MusicTrack.MENU
            Screen.AD,
            Screen.GAME -> when {
                isTutorialLevel() -> MusicTrack.TUTORIAL
                gameMode == GameMode.CHAOS -> MusicTrack.CHAOS
                else -> MusicTrack.CLASSIC
            }
        }
        audio.playMusic(track)
    }

    private fun isTutorialLevel(): Boolean = isTutorialLevel(level.index)

    private fun isTutorialLevel(levelNumber: Int): Boolean = levelNumber <= TUTORIAL_LAST_LEVEL

    private fun shouldShowLevelAd(mode: GameMode, levelNumber: Int): Boolean =
        AdPolicyController.shouldShowLevelAd(
            prefs = prefs,
            mode = mode,
            levelNumber = levelNumber,
            isTutorialLevel = ::isTutorialLevel,
            tutorialLastLevel = TUTORIAL_LAST_LEVEL,
            adLevelInterval = AD_LEVEL_INTERVAL,
            levelAdKey = ::levelAdKey
        )

    private fun markLevelAdShown(mode: GameMode, levelNumber: Int) {
        AdPolicyController.markLevelAdShown(prefs, mode, levelNumber, ::levelAdKey)
    }

    private fun configureStage(width: Int, height: Int, reset: Boolean) {
        if (viewWidth != width.coerceAtLeast(1) || viewHeight != height.coerceAtLeast(1)) {
            recycleScaledBackgrounds()
        }
        viewWidth = width.coerceAtLeast(1)
        viewHeight = height.coerceAtLeast(1)
        updateUiDensity()
        scale = min(viewWidth / STAGE_WIDTH, viewHeight / TARGET_STAGE_HEIGHT)
        stageLeft = ((viewWidth - STAGE_WIDTH * scale) * 0.5f).coerceAtLeast(0f)
        stageHeight = viewHeight / scale
        level = createLevel()
        layoutButtons()
        layoutMenuButtons()
        if (reset) resetRound()
    }

    private fun update(dt: Float) {
        tabletOrientationPromptController.update(dt, context, viewWidth, viewHeight)
        syncMusicTrack()
        stateElapsed += dt
        menuPulse += dt
        flash = max(0f, flash - dt * 1.9f)
        finishPulse = max(0f, finishPulse - dt * 1.7f)
        trauma = max(0f, trauma - dt * 2.8f)
        riftBreakTimer = max(0f, riftBreakTimer - dt)
        collectionMessageTimer = max(0f, collectionMessageTimer - dt)
        screenTransitionTimer = max(0f, screenTransitionTimer - dt)
        if (collectionMessageTimer <= 0f) {
            collectionMessage = ""
        }
        powerMessageTimer = max(0f, powerMessageTimer - dt)
        bounceSoundCooldown = max(0f, bounceSoundCooldown - dt)
        pulseFeedbackCooldown = max(0f, pulseFeedbackCooldown - dt)
        if (powerMessageTimer <= 0f) powerMessage = ""
        leaderboardMessageTimer = max(0f, leaderboardMessageTimer - dt)
        if (leaderboardMessageTimer <= 0f) {
            leaderboardMessage = ""
        }

        if (screen == Screen.MENU ||
            screen == Screen.COLLECTION ||
            screen == Screen.LEADERBOARDS ||
            screen == Screen.MISSIONS ||
            screen == Screen.LANGUAGE ||
            screen == Screen.SETTINGS
        ) {
            if (screen == Screen.MENU) updateMenuBallPhysics(dt)
            ball = Point2(
                x = 5f + sin(menuPulse * 0.9f) * 2.4f,
                y = stageHeight * 0.58f + cos(menuPulse * 1.25f) * 1.5f
            )
            pulseIntensity = 0.45f + 0.28f * sin(menuPulse * 2.1f)
            return
        }

        if (screen == Screen.AD) {
            pulseIntensity = 0.35f + 0.18f * sin(stateElapsed * 2.4f)
            return
        }

        when (state) {
            GameState.READY -> {
                ball = level.start
                pulseIntensity = 0.25f + 0.2f * sin(stateElapsed * 2.2f)
            }

            GameState.SIMULATING -> {
                simElapsed += dt
                updateRiftEnergy(dt)
                val frame = physics.step(dt, simElapsed)
                ball = frame.ball
                addLiveBallTrail(frame.ball)
                pulseIntensity = frame.pulseIntensity
                updateLiveChain(frame, dt)
                replay.add(frame)
                if (frame.impactStrength >= 0.7f && bounceSoundCooldown <= 0f) {
                    audio.playBounce(selectedBallIndex(), frame.impactStrength)
                    bounceSoundCooldown = 0.055f
                    if (frame.impactStrength >= 0.82f) {
                        addTrauma(frame.impactStrength * 0.12f)
                    }
                }
                if (frame.powerTriggered) {
                    powerMessage = t("PRISM SHIELD SAID NOT TODAY").uppercase()
                    powerMessageTimer = 2.4f
                    audio.playEvent(SoundEvent.POWER, selectedBallIndex())
                    performHapticFeedback(HapticFeedbackCompat.confirm)
                    addTrauma(0.18f)
                }
                if (frame.portalTriggered) {
                    powerMessage = t("PORTAL SLINGSHOT").uppercase()
                    powerMessageTimer = 1.6f
                    pulseFeedbackCooldown = 0.35f
                    audio.playEvent(SoundEvent.POWER, selectedBallIndex(), 1f)
                    hapticSequence(HapticFeedbackCompat.confirm to 0L, HapticFeedbackConstants.CLOCK_TICK to 95L)
                    addTrauma(0.24f)
                }
                if (frame.pulseIntensity >= 0.42f && pulseFeedbackCooldown <= 0f) {
                    powerMessage = t(if (levelHasCurse(CurseType.PULSE_STORM)) "PULSE STORM GRABBED YOU" else "BOOST FIELD ONLINE").uppercase()
                    powerMessageTimer = 1.25f
                    pulseFeedbackCooldown = 0.85f
                    audio.playEvent(SoundEvent.CHAIN, selectedBallIndex(), frame.pulseIntensity)
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                if (frame.outcome != PhysicsOutcome.RUNNING) {
                    finishSimulation(frame.outcome)
                }
            }

            GameState.WON,
            GameState.LOST -> {
                pulseIntensity *= 0.94f
            }
        }
    }

    private fun startRiftControl(x: Float, y: Float) {
        if (screen != Screen.GAME) return
        if (state == GameState.WON || state == GameState.LOST) return
        if (state == GameState.READY) beginLiveRun()
        if (state != GameState.SIMULATING || riftEnergy <= 0.025f) return

        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val point = screenToWorld(x, y).clampedToStage()
        riftActive = true
        riftAnchor = point
        riftHoldSeconds = 0f
        riftTapReleaseTimer = riftTapPulseDuration()
        addRiftTrailPoint(point, force = true)
        physics.setRiftControl(point, riftControlStrength())
        audio.playEvent(SoundEvent.RIFT_ON, selectedBallIndex(), riftEnergy)
    }

    private fun moveRiftControl(x: Float, y: Float) {
        if (!riftActive || state != GameState.SIMULATING) return
        val point = screenToWorld(x, y).clampedToStage()
        riftAnchor = point
        addRiftTrailPoint(point)
        physics.setRiftControl(point, riftControlStrength())
    }

    private fun addRiftTrailPoint(point: Point2, force: Boolean = false) {
        val minDistance = if (performanceLite()) 0.18f else 0.1f
        if (!force && playerLine.lastOrNull()?.distanceTo(point)?.let { it < minDistance } == true) return
        playerLine += point
        val maxPoints = if (performanceLite()) 96 else 220
        if (playerLine.size > maxPoints) playerLine.removeAt(0)
    }

    private fun addLiveBallTrail(point: Point2) {
        val minDistance = if (performanceLite()) 0.1f else 0.06f
        if (liveBallTrail.lastOrNull()?.distanceTo(point)?.let { it < minDistance } == true) return
        liveBallTrail += point
        val maxPoints = if (performanceLite()) 24 else 42
        if (liveBallTrail.size > maxPoints) liveBallTrail.removeAt(0)
    }

    private fun releaseRiftControl(withHaptic: Boolean = true) {
        if (!riftActive) return
        physics.setRiftControl(null, 0f)
        riftActive = false
        riftAnchor = null
        riftHoldSeconds = 0f
        riftTapReleaseTimer = 0f
        if (withHaptic) {
            audio.playEvent(SoundEvent.RIFT_OFF, selectedBallIndex(), riftEnergy)
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    private fun riftTapPulseDuration(): Float {
        return when {
            levelHasCurse(CurseType.POWER_HOLD) -> 0.34f
            levelHasCurse(CurseType.FOCUS_FIELD) -> 0.28f
            levelHasCurse(CurseType.OVERHEAT) -> 0.18f
            levelHasCurse(CurseType.RIFT_DRAIN) -> 0.2f
            gameMode == GameMode.CHAOS -> 0.22f
            else -> 0.24f
        }
    }

    private fun beginLiveRun() {
        if (state != GameState.READY) return
        playerLine.clear()
        state = GameState.SIMULATING
        stateElapsed = 0f
        simElapsed = 0f
        ball = level.start
        pulseIntensity = 0f
        replay.clear()
        replayFrames = emptyList()
        lastScore = null
        val selectedPower = selectedBallSkin().power
        if (selectedPower != BallPower.NONE) {
            ensureFairLeaderboardSnapshot()
            powerMessage = "${ballPowerName(selectedPower)}  /  ${ballPowerDescription(selectedPower)}"
            powerMessageTimer = 2.8f
            audio.playEvent(SoundEvent.POWER, selectedBallIndex())
        }
        physics.reset(level, selectedPower)
        performHapticFeedback(HapticFeedbackCompat.confirm)
    }

    private fun updateRiftEnergy(dt: Float) {
        if (riftActive) {
            riftHoldSeconds += dt
            riftTapReleaseTimer = max(0f, riftTapReleaseTimer - dt)
            val drain = when {
                levelHasCurse(CurseType.RIFT_DRAIN) -> 0.6f
                levelHasCurse(CurseType.OVERHEAT) -> 0.44f + min(1f, riftHoldSeconds) * 0.28f
                levelHasCurse(CurseType.POWER_HOLD) -> 0.48f
                levelHasCurse(CurseType.FOCUS_FIELD) -> 0.4f
                gameMode == GameMode.CHAOS -> 0.46f
                else -> 0.42f
            } * level.riftDrainMultiplier
            riftEnergy = max(0f, riftEnergy - dt * drain)
            inkUsed = max(inkUsed, (1f - riftEnergy) * level.inkLimit)
            physics.setRiftControl(riftAnchor, riftControlStrength())
            if (riftEnergy <= 0f) {
                physics.setRiftControl(null, 0f)
                riftActive = false
                riftAnchor = null
                riftTapReleaseTimer = 0f
                audio.playEvent(SoundEvent.RIFT_OFF, selectedBallIndex(), 0f)
                performHapticFeedback(HapticFeedbackCompat.reject)
            } else if (riftTapReleaseTimer <= 0f) {
                releaseRiftControl(withHaptic = false)
            }
        } else {
            val recharge = when {
                levelHasCurse(CurseType.RIFT_DRAIN) -> 0.14f
                levelHasCurse(CurseType.OVERHEAT) -> 0.18f
                gameMode == GameMode.CHAOS -> 0.22f
                else -> 0.24f
            } * when (selectedBallSkin().power) {
                BallPower.PLASMA_SURGE -> 1.35f
                BallPower.MINOR_SURGE -> 1.15f
                else -> 1f
            }
            riftEnergy = min(1f, riftEnergy + dt * recharge)
        }
    }

    private fun riftControlStrength(): Float {
        val holdPower = when {
            levelHasCurse(CurseType.POWER_HOLD) -> 0.38f + min(1f, riftHoldSeconds / 0.9f) * 0.62f
            levelHasCurse(CurseType.OVERHEAT) -> 0.52f + min(1f, riftHoldSeconds / 0.65f) * 0.48f
            levelHasCurse(CurseType.FOCUS_FIELD) -> 0.62f + min(1f, riftHoldSeconds / 0.45f) * 0.22f
            else -> 0.54f + min(1f, riftHoldSeconds / 0.52f) * 0.34f
        }
        return holdPower * (0.42f + riftEnergy * 0.58f)
    }

    private fun updateLiveChain(frame: PhysicsFrame, dt: Float) {
        val qualifying = (riftActive && frame.speed > 2.05f) ||
            (frame.speed > 3.6f && frame.pulseIntensity > 0.5f)
        if (qualifying) {
            chainCharge += dt * (0.78f + min(frame.speed, 5.2f) * 0.07f)
            if (chainCharge >= 0.62f) {
                chainCharge -= 0.62f
                chainCount = (chainCount + 1).coerceAtMost(99)
                maxChain = max(maxChain, chainCount)
                if (chainCount <= 6) {
                    audio.playEvent(SoundEvent.CHAIN, selectedBallIndex(), chainCount / 6f)
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                } else {
                    audio.playEvent(SoundEvent.CHAIN, selectedBallIndex(), 1f)
                    performHapticFeedback(HapticFeedbackCompat.confirm)
                }
            }
        } else {
            chainCharge = max(0f, chainCharge - dt * 1.15f)
            if (chainCharge <= 0f && frame.speed < 1.7f) {
                chainCount = 0
            }
        }
    }

    private fun finishSimulation(outcome: PhysicsOutcome) {
        replayFrames = replay.snapshot()
        releaseRiftControl(withHaptic = false)
        state = if (outcome == PhysicsOutcome.WON) GameState.WON else GameState.LOST
        stateElapsed = 0f
        flash = if (outcome == PhysicsOutcome.WON) 1f else 0.55f
        finishPulse = 1f
        if (outcome == PhysicsOutcome.WON) {
            val unlockedBefore = unlockedSkinIds()
            val score = replay.buildScore(level, inkUsed, simElapsed)
            lastScore = score
            streak += 1
            lastRiftBreak = GameplayScoreCalculator.shouldTriggerRiftBreak(
                riftEnergy = riftEnergy,
                maxChain = maxChain,
                gameMode = gameMode,
                seconds = score.seconds,
                timeLimitSeconds = level.timeLimitSeconds,
                rank = score.rank
            )
            lastRiftBreakBonus = if (lastRiftBreak) {
                GameplayScoreCalculator.calculateRiftBreakBonus(score.rank, riftEnergy, maxChain, gameMode)
            } else {
                0
            }
            lastRiftBreakReason = if (lastRiftBreak) riftBreakReason(score) else ""
            lastDailyBonus = claimDailyRiftBonus()
            lastStreakMilestoneBonus = GameplayScoreCalculator.calculateStreakMilestoneBonus(streak)
            lastHypeScore = GameplayScoreCalculator.calculateHypeScore(
                rank = score.rank,
                gameMode = gameMode,
                seconds = score.seconds,
                inkUsed = score.inkUsed,
                inkLimit = level.inkLimit,
                streak = streak,
                maxChain = maxChain
            ) + lastRiftBreakBonus + lastDailyBonus + lastStreakMilestoneBonus
            if (lastRiftBreak) {
                riftBreakTimer = 2.15f
                powerMessage = "${t("RIFT BREAK").uppercase()}  +$lastRiftBreakBonus"
                powerMessageTimer = 2.35f
                addTrauma(0.38f)
            }
            saveBest(score)
            missionsScreenController.recordRound(won = true, coinsEarned = lastHypeScore)
            val unlockedAfter = unlockedSkinIds()
            val newSkin = ballSkins.firstOrNull { it.id in (unlockedAfter - unlockedBefore) }
            rewardMessage = finishRewardLine(newSkin)
            clearFailContinueCount()
            audio.playEvent(if (newSkin != null) SoundEvent.UNLOCK else if (lastRiftBreak) SoundEvent.POWER else SoundEvent.GOAL, selectedBallIndex())
            if (newSkin != null) {
                hapticSequence(
                    HapticFeedbackCompat.confirm to 0L,
                    HapticFeedbackConstants.LONG_PRESS to 90L,
                    HapticFeedbackConstants.CLOCK_TICK to 180L
                )
            } else {
                hapticSequence(
                    HapticFeedbackCompat.confirm to 0L,
                    HapticFeedbackConstants.CLOCK_TICK to 110L
                )
            }
        } else {
            lastScore = null
            missionsScreenController.recordRound(won = false, coinsEarned = 0)
            audio.playEvent(SoundEvent.FAIL, selectedBallIndex())
            addTrauma(0.55f)
            hapticSequence(
                HapticFeedbackCompat.reject to 0L,
                HapticFeedbackConstants.CLOCK_TICK to 120L
            )
        }
    }

    private fun resetRound() {
        level = createLevel()
        state = GameState.READY
        stateElapsed = 0f
        simElapsed = 0f
        inkUsed = 0f
        riftEnergy = 1f
        riftActive = false
        riftAnchor = null
        riftHoldSeconds = 0f
        riftTapReleaseTimer = 0f
        chainCount = 0
        chainCharge = 0f
        maxChain = 0
        pulseIntensity = 0f
        flash = 0f
        finishPulse = 0f
        riftBreakTimer = 0f
        lastRiftBreak = false
        lastRiftBreakBonus = 0
        lastRiftBreakReason = ""
        lastDailyBonus = 0
        lastStreakMilestoneBonus = 0
        lastHypeScore = 0
        rewardMessage = ""
        powerMessage = ""
        powerMessageTimer = 0f
        bounceSoundCooldown = 0f
        pulseFeedbackCooldown = 0f
        activeButton = ButtonId.NONE
        ball = level.start
        playerLine.clear()
        liveBallTrail.clear()
        replayFrames = emptyList()
        replay.clear()
        lastScore = null
        refreshTutorialCardVisibility()
    }

    private fun tutorialAcknowledgementKey(): String =
        TutorialInputGate.acknowledgementKey(gameMode.name, level.index)

    private fun refreshTutorialCardVisibility() {
        resetTutorialGesture()
        tutorialCardBounds.setEmpty()
        tutorialStartButton.setEmpty()
        tutorialCardVisible = TutorialInputGate.shouldShow(
            gameScreen = screen == Screen.GAME,
            ready = state == GameState.READY,
            hasTutorialHint = level.tutorialHint.isNotBlank(),
            acknowledged = prefs.getBoolean(tutorialAcknowledgementKey(), false)
        )
    }

    private fun startRun(mode: GameMode, continueProgress: Boolean) {
        missionsScreenController.dismissGamePopup()
        gameMode = mode
        if (continueProgress) {
            levelIndex = modeProgress(mode)
            streak = modeStreak(mode)
        } else {
            resetModeProgress(mode)
            levelIndex = 1
            streak = 0
        }
        screen = Screen.GAME
        resetRound()
        triggerScreenTransition(level.accent)
        performHapticFeedback(HapticFeedbackCompat.confirm)
    }

    private fun createLevel(): LevelSpec {
        val spec = when (gameMode) {
            GameMode.CLASSIC -> LevelDirector.createClassic(levelIndex, stageHeight)
            GameMode.CHAOS -> LevelDirector.createChaos(levelIndex, stageHeight, LevelDirector.dailySeed() * 31L + 777L)
        }
        return spec.withHudSafeStart()
    }

    private fun LevelSpec.withHudSafeStart(): LevelSpec {
        if (viewWidth <= 1 || viewHeight <= 1 || scale <= 1.1f) return this
        val compactHud = viewWidth < dp(520f)
        val hasHeaderRibbon = selectedBallSkin().power != BallPower.NONE || curses.isNotEmpty()
        val reservedHudBottom = dp(if (compactHud) {
            if (hasHeaderRibbon) 132f else 104f
        } else {
            156f
        })
        val safeMargin = dp(if (compactHud) 56f else 64f)
        val safeStartY = ((reservedHudBottom + safeMargin) / scale)
            .coerceIn(3.35f, stageHeight - 3.0f)
        if (start.y >= safeStartY) return this
        return copy(start = start.copy(y = safeStartY))
    }

    private fun saveBest(score: RunScore) {
        val current = prefs.getString(GameProgressRepository.bestKey(gameMode, score.level), null)
        if (current == null || rankValue(score.rank) < rankValue(current)) {
            prefs.edit { putString(GameProgressRepository.bestKey(gameMode, score.level), score.rank) }
        }
        val nextLevel = max(modeProgress(gameMode), score.level + 1)
        val nextBestStreak = max(modeBestStreak(gameMode), streak)
        val newHypeBalance = (hypeBalance().toLong() + lastHypeScore.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        prefs.edit {
            putInt(GameProgressRepository.progressKey(gameMode), nextLevel)
            putInt(GameProgressRepository.streakKey(gameMode), streak)
            putInt(highestLevelKey(gameMode), max(modeHighestLevel(gameMode), nextLevel))
            putInt(bestModeStreakKey(gameMode), nextBestStreak)
            putInt(BEST_STREAK_KEY, max(bestStreak(), streak))
            putInt("clear_streak", streak)
            putInt("last_hype", lastHypeScore)
            putInt(HYPE_BANK_KEY, newHypeBalance)
        }
        val levelBoard = if (gameMode == GameMode.CLASSIC) LeaderboardBoard.CLASSIC_LEVEL else LeaderboardBoard.CHAOS_LEVEL
        val streakBoard = if (gameMode == GameMode.CLASSIC) LeaderboardBoard.CLASSIC_STREAK else LeaderboardBoard.CHAOS_STREAK
        if (selectedBallSkin().power == BallPower.NONE) {
            val fairLevel = max(prefs.getInt(fairHighestLevelKey(gameMode), modeHighestLevel(gameMode)), nextLevel)
            val fairStreak = max(prefs.getInt(fairBestStreakKey(gameMode), modeBestStreak(gameMode)), nextBestStreak)
            prefs.edit {
                putInt(fairHighestLevelKey(gameMode), fairLevel)
                putInt(fairBestStreakKey(gameMode), fairStreak)
            }
            // Keep every score submission behind the same client-side sanity guard used
            // when synchronizing the locally persisted leaderboard snapshot.
            LeaderboardTouchController.submitScore(
                board = levelBoard,
                score = fairLevel,
                leaderboardBridge = leaderboardBridge,
                modeHighestLevel = ::modeHighestLevel,
                modeBestStreak = ::modeBestStreak
            )
            LeaderboardTouchController.submitScore(
                board = streakBoard,
                score = fairStreak,
                leaderboardBridge = leaderboardBridge,
                modeHighestLevel = ::modeHighestLevel,
                modeBestStreak = ::modeBestStreak
            )
        }
    }

    private fun riftBreakReason(score: RunScore): String =
        OutcomeTouchController.riftBreakReason(
            score = score,
            riftEnergy = riftEnergy,
            maxChain = maxChain,
            timeLimitSeconds = level.timeLimitSeconds,
            gameMode = gameMode,
            t = ::t
        )

    private fun toggleSfxMuted() {
        sfxMuted = !sfxMuted
        prefs.edit { putBoolean(SFX_MUTED_KEY, sfxMuted) }
        audio.setSfxMuted(sfxMuted)
        if (!sfxMuted) {
            audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
        }
    }

    private fun toggleMusicMuted() {
        musicMuted = !musicMuted
        prefs.edit { putBoolean(MUSIC_MUTED_KEY, musicMuted) }
        audio.setMusicMuted(musicMuted)
        if (!musicMuted) {
            syncMusicTrack()
        }
    }

    private fun handleButton(button: ButtonId): (() -> Unit)? {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (button == ButtonId.SFX) {
            toggleSfxMuted()
            return null
        }
        if (button == ButtonId.MUSIC) {
            toggleMusicMuted()
            return null
        }
        if (button != ButtonId.NONE) audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
        return when (button) {
            ButtonId.HOME -> {
                exitToMenu()
                null
            }

            ButtonId.RESTART -> {
                if (state == GameState.LOST) {
                    continueAfterFail()
                } else {
                    resetRound()
                }
                null
            }

            ButtonId.SHARE -> {
                { shareRun() }
            }

            ButtonId.NEXT -> {
                if (state == GameState.WON) {
                    val nextLevel = level.index + 1
                    if (shouldShowLevelAd(gameMode, nextLevel)) {
                        markLevelAdShown(gameMode, nextLevel)
                        showAd(AdAction.NEXT_LEVEL, "${t("LEVEL").uppercase()} $nextLevel ${t("CHECKPOINT").uppercase()}")
                    } else {
                        advanceToNextLevel()
                    }
                }
                null
            }

            ButtonId.CONTINUE -> {
                continueAfterFail()
                null
            }

            ButtonId.AD_CONTINUE -> {
                requestAdThenContinue()
                null
            }

            ButtonId.SFX,
            ButtonId.MUSIC,
            ButtonId.NONE -> null
        }
    }

    private fun handleMenuButton(button: MenuButton) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (button == MenuButton.SFX) {
            toggleSfxMuted()
            return
        }
        if (button == MenuButton.MUSIC) {
            toggleMusicMuted()
            return
        }
        if (button != MenuButton.NONE) audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
        when (button) {
            MenuButton.PLAY -> playSelectedMode()
            MenuButton.CLASSIC -> selectHomeMode(GameMode.CLASSIC)
            MenuButton.CHAOS -> selectHomeMode(GameMode.CHAOS)
            MenuButton.CLASSIC_CONTINUE -> startRun(GameMode.CLASSIC, continueProgress = true)
            MenuButton.CLASSIC_START -> startRun(GameMode.CLASSIC, continueProgress = false)
            MenuButton.CHAOS_START -> startRun(GameMode.CHAOS, continueProgress = false)
            MenuButton.CHAOS_CONTINUE -> continueRunFromMenu(GameMode.CHAOS)
            MenuButton.CHAOS_NEW -> startRun(GameMode.CHAOS, continueProgress = false)
            MenuButton.VAULT,
            MenuButton.COLLECTION -> {
                screen = Screen.COLLECTION
                collectionMessage = ""
                collectionMessageTimer = 0f
                collectionFocusSkinId = selectedBallSkin().id
                activeMenuButton = MenuButton.NONE
                triggerScreenTransition(selectedBallSkin().lineColor)
            }

            MenuButton.LEADERBOARDS -> {
                screen = Screen.LEADERBOARDS
                leaderboardMessage = ""
                leaderboardMessageTimer = 0f
                activeLeaderboardIndex = -1
                triggerScreenTransition(0xFF8AA6FF.toInt())
                syncLeaderboards()
            }

            MenuButton.MISSIONS -> {
                screen = Screen.MISSIONS
                menuState = MenuState.MODES
                activeMenuButton = MenuButton.NONE
                missionsScreenController.reset()
                triggerScreenTransition(KavvoroPalette.cyan)
            }

            MenuButton.SETTINGS -> openSettings()
            MenuButton.PRIVACY -> privacyBridge.showPrivacyOptions()
            MenuButton.LANGUAGE -> {
                languageReturnScreen = Screen.MENU
                screen = Screen.LANGUAGE
                activeMenuButton = MenuButton.NONE
                activeLanguageIndex = -1
                triggerScreenTransition(0xFF45F2FF.toInt())
            }

            MenuButton.SFX,
            MenuButton.MUSIC -> Unit

            MenuButton.START -> startRun(selectedMenuMode, continueProgress = false)
            MenuButton.CONTINUE -> continueRunFromMenu(selectedMenuMode)
            MenuButton.BACK -> {
                menuState = MenuState.MODES
                activeMenuButton = MenuButton.NONE
                triggerScreenTransition(0xFF8AA6FF.toInt())
            }

            MenuButton.DAILY_RIFT -> {
                isDailyRiftModalVisible = true
                activeMenuButton = MenuButton.NONE
            }

            MenuButton.NONE -> Unit
        }
    }

    private fun openSettings() {
        screen = Screen.SETTINGS
        settingsScroll = 0f
        settingsDragging = false
        settingsResetConfirm = false
        activeSettingsButton = SettingsButton.NONE
        triggerScreenTransition(0xFF45F2FF.toInt())
    }

    private fun playSelectedMode() {
        // Play is the entry point to the mode decision. Starting or resuming a
        // run happens only after an explicit action on the picker screen.
        menuState = MenuState.MODE_ACTION
        activeMenuButton = MenuButton.NONE
        triggerScreenTransition(if (selectedMenuMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt())
    }

    private fun continueRunFromMenu(mode: GameMode) {
        val resumeLevel = modeProgress(mode)
        pendingResumeMode = mode
        gameMode = mode
        streak = modeStreak(mode)
        if (shouldShowLevelAd(mode, resumeLevel)) {
            markLevelAdShown(mode, resumeLevel)
            showAd(
                AdAction.RESUME_RUN,
                "${t("CONTINUE").uppercase()} ${mode.menuTitle(::t)} L${resumeLevel.toString().padStart(2, '0')}"
            )
        } else {
            startRun(mode, continueProgress = true)
        }
    }

    private fun selectHomeMode(mode: GameMode) {
        if (menuState == MenuState.MODE_ACTION) {
            if (selectedMenuMode != mode) {
                selectedMenuMode = mode
                val accent = if (mode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
                menuBallVelocityX += if (mode == GameMode.CHAOS) -420f else 420f
                menuBallVelocityY += if (mode == GameMode.CHAOS) 160f else -120f
                triggerScreenTransition(accent)
            }
            return
        }
        val changed = selectedMenuMode != mode
        if (selectedMenuMode == mode && modeProgress(mode) > 1) {
            menuState = MenuState.MODE_ACTION
        } else {
            selectedMenuMode = mode
            menuState = MenuState.MODES
        }
        if (changed) {
            val accent = if (mode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
            menuBallVelocityX += if (mode == GameMode.CHAOS) -420f else 420f
            menuBallVelocityY += if (mode == GameMode.CHAOS) 160f else -120f
            triggerScreenTransition(accent)
        }
    }

    private fun exitToMenu() {
        if (state == GameState.WON || state == GameState.LOST) {
            breakStreak()
        }
        screen = Screen.MENU
        menuState = MenuState.MODES
        activeButton = ButtonId.NONE
        pendingAdAction = AdAction.NONE
        pendingResumeMode = gameMode
        triggerScreenTransition(selectedBallSkin().lineColor)
    }

    private fun continueAfterFail() {
        if (state != GameState.LOST) return
        if (continueRequiresAd()) {
            if (shouldShowLevelAd(gameMode, level.index)) {
                markLevelAdShown(gameMode, level.index)
            }
            showAd(AdAction.CONTINUE_AFTER_FAIL, continueAdReason())
        } else {
            recordFreeContinue()
            resetRound()
            triggerScreenTransition(level.accent)
        }
    }

    private fun advanceToNextLevel() {
        val previousLevel = levelIndex
        levelIndex += 1
        resetRound()
        if (previousLevel == 10) {
            powerMessage = "${gameMode.menuTitle(::t)} ${t("RIFT ONLINE").uppercase()}"
            powerMessageTimer = 2.4f
            hapticSequence(HapticFeedbackCompat.confirm to 0L, HapticFeedbackConstants.CLOCK_TICK to 120L)
            triggerScreenTransition(if (gameMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt())
        } else {
            triggerScreenTransition(level.accent)
        }
    }

    private fun showAd(action: AdAction, reason: String) {
        pendingAdAction = action
        adReason = reason
        screen = Screen.AD
        activeButton = ButtonId.NONE
        stateElapsed = 0f
        adLoading = false
        triggerScreenTransition(0xFFFFCF4A.toInt())
    }

    private fun requestAdThenContinue() {
        if (pendingAdAction == AdAction.NONE || adLoading) return
        adLoading = true
        if (pendingAdAction == AdAction.CONTINUE_AFTER_FAIL) {
            adBridge.showRewardedContinue(
                onRewarded = {
                    post {
                        synchronized(lock) {
                            adLoading = false
                            completeAdGate()
                        }
                    }
                },
                onUnavailable = {
                    post {
                        synchronized(lock) {
                            adLoading = false
                            adReason = t("REWARDED AD NOT READY - TRY AGAIN").uppercase()
                        }
                    }
                }
            )
        } else {
            adBridge.showInterstitial {
                post {
                    synchronized(lock) {
                        adLoading = false
                        completeAdGate()
                    }
                }
            }
        }
    }

    private fun completeAdGate() {
        when (pendingAdAction) {
            AdAction.NEXT_LEVEL -> {
                screen = Screen.GAME
                pendingAdAction = AdAction.NONE
                advanceToNextLevel()
            }

            AdAction.CONTINUE_AFTER_FAIL -> {
                recordAdContinue()
                screen = Screen.GAME
                pendingAdAction = AdAction.NONE
                resetRound()
                triggerScreenTransition(level.accent)
            }

            AdAction.RESUME_RUN -> {
                val mode = pendingResumeMode
                pendingAdAction = AdAction.NONE
                startRun(mode, continueProgress = true)
            }

            AdAction.NONE -> {
                screen = Screen.GAME
                pendingAdAction = AdAction.NONE
                triggerScreenTransition(level.accent)
            }
        }
    }

    private fun continueRequiresAd(): Boolean =
        AdPolicyController.continueRequiresAd(prefs, gameMode, level.index, ::shouldShowLevelAd)

    private fun continueAdReason(): String =
        AdPolicyController.continueAdReason(prefs, gameMode, level.index, ::shouldShowLevelAd, ::t)

    private fun recordFreeContinue() {
        AdPolicyController.recordFreeContinue(prefs, gameMode, level.index)
    }

    private fun recordAdContinue() {
        clearFailContinueCount()
    }

    private fun clearFailContinueCount() {
        AdPolicyController.clearFailContinueCount(prefs, gameMode, level.index)
    }

    private fun breakStreak() {
        if (streak == 0) return
        streak = 0
        progressRepository.breakStreak(gameMode)
    }

    private fun triggerScreenTransition(accent: Int = selectedBallSkin().lineColor) {
        screenTransitionAccent = accent
        screenTransitionTimer = 0.34f
    }

    private fun hapticSequence(vararg pulses: Pair<Int, Long>) {
        pulses.forEach { (feedback, delayMs) ->
            if (delayMs <= 0L) {
                performHapticFeedback(feedback)
            } else {
                postDelayed({ performHapticFeedback(feedback) }, delayMs)
            }
        }
    }

    private fun addTrauma(amount: Float) {
        if (settingsScreenShake) {
            trauma = min(1f, trauma + amount)
        }
    }

    private fun drawActiveScreenContent(canvas: Canvas) {
        if (screen == Screen.AD) {
            drawBackground(canvas)
            drawAdPlaceholder(canvas)
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.MENU) {
            drawMenu(canvas)
            if (isDailyRiftModalVisible) {
                DailyRiftRewardRenderer.draw(
                    canvas = canvas,
                    viewWidth = viewWidth.toFloat(),
                    viewHeight = viewHeight.toFloat(),
                    claimed = progressRepository.dailyRiftBonusClaimed(),
                    rewardAmount = progressRepository.dailyRiftRewardForDay(progressRepository.dailyRiftStreak()),
                    streakDay = progressRepository.dailyRiftStreak(),
                    hypeBalance = hypeBalance(),
                    resetText = progressRepository.dailyRiftResetText(),
                    claimButtonRect = dailyRiftClaimButtonRect,
                    actionButtonRect = dailyRiftActionButtonRect,
                    closeButtonRect = dailyRiftCloseButtonRect,
                    isClaimPressed = activeDailyRiftButton == DailyRiftButton.CLAIM,
                    isActionPressed = activeDailyRiftButton == DailyRiftButton.ACTION,
                    pulseTime = menuPulse,
                    paint = paint,
                    textPaint = textPaint,
                    dp = uiDensity,
                    typeface = AssetResourceManager.oxaniumTypeface,
                    t = ::t,
                    formatHypeAmount = ::formatHypeAmount,
                    cardBoundsOut = dailyRiftCardBounds
                )
            }
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.COLLECTION) {
            drawCollection(canvas)
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.LEADERBOARDS) {
            drawLeaderboards(canvas)
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.MISSIONS) {
            drawBackground(canvas)
            missionsScreenController.draw(canvas, viewWidth, viewHeight, uiDensity, ::t)
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.LANGUAGE) {
            drawLanguageSelector(canvas)
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.SETTINGS) {
            drawSettings(canvas)
            drawScreenTransition(canvas)
            return
        }
        drawBackground(canvas)
        val traumaSq = trauma * trauma
        val shakeX = if (traumaSq > 0.001f) dp(8f) * traumaSq * sin(stateElapsed * 46f) else 0f
        val shakeY = if (traumaSq > 0.001f) dp(8f) * traumaSq * cos(stateElapsed * 38f) else 0f
        val hasShake = shakeX != 0f || shakeY != 0f
        if (hasShake) {
            canvas.save()
            canvas.translate(shakeX, shakeY)
        }
        val skin = selectedBallSkin()
        GameplayArenaRenderer.drawArenaScene(
            canvas = canvas,
            level = level,
            state = state,
            gameMode = gameMode,
            ballCenter = ball,
            skin = skin,
            skinIndex = selectedBallIndex(),
            totalSkinCount = ballSkins.size,
            stateElapsed = stateElapsed,
            simElapsed = simElapsed,
            menuPulse = menuPulse,
            finishPulse = finishPulse,
            pulseIntensity = pulseIntensity,
            riftEnergy = riftEnergy,
            riftActive = riftActive,
            riftAnchor = riftAnchor,
            riftHoldSeconds = riftHoldSeconds,
            chainCount = chainCount,
            adaptiveQuality = adaptiveQuality,
            stageHeight = stageHeight,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp(1f),
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            isCompactHud = isCompactHud(),
            gameplayOverlayTop = gameplayOverlayTop(),
            actionLabel = tutorialActionLabel(),
            rich = richEffects(),
            lite = performanceLite(),
            fullEffects = fullEffects(),
            replayFrames = replayFrames,
            playerLine = playerLine,
            liveBallTrail = liveBallTrail,
            paint = paint,
            textPaint = textPaint,
            t = ::t,
            fitText = ::fitText,
            drawWorldAsset = ::drawWorldAsset,
            drawBallSkin = ::drawBallSkin
        )
        if (hasShake) {
            canvas.restore()
        }
        layoutButtons()
        GameplayHudRenderer.drawHudAndOverlays(
            canvas = canvas,
            level = level,
            gameMode = gameMode,
            gameState = state,
            selectedBallSkin = skin,
            stageLeft = stageLeft,
            stageWidth = STAGE_WIDTH,
            scale = scale,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            simElapsed = simElapsed,
            stateElapsed = stateElapsed,
            riftEnergy = riftEnergy,
            lastHypeScore = lastHypeScore,
            streak = streak,
            maxChain = maxChain,
            chainCount = chainCount,
            finishPulse = finishPulse,
            lastRiftBreak = lastRiftBreak,
            riftBreakTimer = riftBreakTimer,
            lastRiftBreakBonus = lastRiftBreakBonus,
            lastRiftBreakReason = lastRiftBreakReason,
            powerMessageTimer = powerMessageTimer,
            powerMessage = powerMessage,
            ballScreenX = sx(ball.x),
            ballScreenY = sy(ball.y),
            goalScreenX = sx(level.goal.x),
            goalScreenY = sy(level.goal.y),
            performanceLite = performanceLite(),
            dp = dp(1f),
            musicButton = musicButton,
            sfxButton = sfxButton,
            homeButton = homeButton,
            restartButton = restartButton,
            shareButton = shareButton,
            nextButton = nextButton,
            paint = paint,
            textPaint = textPaint,
            t = ::t,
            fitText = ::fitText,
            localizedLevelTitle = ::localizedLevelTitle,
            formatHypeAmount = ::formatHypeAmount,
            drawWorldAsset = ::drawWorldAsset,
            drawIconButton = ::drawIconButton,
            drawFittedText = ::drawFittedText,
            onBeforePostOverlays = { drawTutorialHint(canvas) }
        )
        drawOutcome(canvas)
        if (flash > 0f) {
            AtmosphereRenderer.drawFlash(
                canvas = canvas,
                width = viewWidth.toFloat(),
                height = viewHeight.toFloat(),
                color = withAlpha(if (state == GameState.WON) level.accent else 0xFFFF4D8D.toInt(), (flash * 72).roundToInt()),
                paint = paint
            )
        }
        missionsScreenController.drawGamePopup(canvas, viewWidth, viewHeight, uiDensity, ::t)
        if (exportingShare) drawExportingOverlay(canvas)
        drawScreenTransition(canvas)
    }

    private fun drawGame(canvas: Canvas) {
        drawActiveScreenContent(canvas)
        drawTabletOrientationPrompt(canvas)
    }

    private fun drawTabletOrientationPrompt(canvas: Canvas) {
        if (!tabletOrientationPromptController.isTabletLandscape) return

        if (tabletOrientationPromptController.isModalVisible) {
            TabletOrientationPromptRenderer.drawModal(
                canvas = canvas,
                viewWidth = viewWidth.toFloat(),
                viewHeight = viewHeight.toFloat(),
                controller = tabletOrientationPromptController,
                context = context,
                paint = paint,
                textPaint = textPaint,
                dp = uiDensity
            )
        } else if (tabletOrientationPromptController.isDismissed) {
            TabletOrientationPromptRenderer.drawPersistentBadge(
                canvas = canvas,
                viewWidth = viewWidth.toFloat(),
                viewHeight = viewHeight.toFloat(),
                controller = tabletOrientationPromptController,
                context = context,
                paint = paint,
                textPaint = textPaint,
                dp = uiDensity
            )
        }
    }

    private fun drawScreenTransition(canvas: Canvas) {
        AtmosphereRenderer.drawScreenTransition(
            canvas = canvas,
            screenTransitionTimer = screenTransitionTimer,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            performanceLite = performanceLite(),
            secondaryColor = selectedBallSkin().secondary,
            screenTransitionAccent = screenTransitionAccent,
            menuPulse = menuPulse,
            dp = uiDensity,
            paint = paint,
            textPaint = textPaint,
            scratch = scratch,
            portalBitmap = worldBitmap("portal_goal"),
            t = ::t
        )
    }

    private fun drawBackground(canvas: Canvas) {
        AtmosphereRenderer.drawBackground(
            canvas = canvas,
            screen = screen,
            menuState = menuState,
            gameMode = gameMode,
            selectedMenuMode = selectedMenuMode,
            levelIndex = level.index,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            uiDensity = uiDensity,
            stageLeft = stageLeft,
            scale = scale,
            stateElapsed = stateElapsed,
            performanceLite = performanceLite(),
            richEffects = richEffects(),
            dp = dp(1f),
            paint = paint,
            scratch = scratch,
            starPath = starPath,
            worldBitmap = ::worldBitmap,
            backgroundBitmap = ::backgroundBitmap,
            worldToScreen = ::worldToScreen,
            t = ::t
        )
    }

    private fun drawMenu(canvas: Canvas) {
        drawBackground(canvas)
        layoutMenuButtons()

        HomeMenuRenderer.drawMenuScreen(
            canvas = canvas,
            menuState = menuState,
            calculator = homeLayoutCalculator,
            activeMenuButton = activeMenuButton,
            bestStreak = bestStreak(),
            currentLevel = level.index,
            hypeBalance = hypeBalance(),
            dailyReady = !dailyRiftBonusClaimed(),
            stateElapsed = stateElapsed,
            context = context,
            paint = paint,
            dp = dp(1f),
            worldBitmap = { worldBitmap(it) },
            formatHypeAmount = { formatHypeAmount(it) },
            t = { t(it) },
            drawPlayModeScreen = { c, l, w, t -> drawPlayModeScreen(c, l, w, t) }
        )
    }

    private fun layoutPlayModeScreen(left: Float, contentWidth: Float, top: Float) {
        ModePickerLayoutCalculator.layoutPlayModeScreen(
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            uiDensity = uiDensity,
            left = left,
            contentWidth = contentWidth,
            top = top,
            classicProgress = modeProgress(GameMode.CLASSIC),
            chaosProgress = modeProgress(GameMode.CHAOS),
            menuClassicCard = menuClassicCard,
            menuChaosCard = menuChaosCard,
            menuClassicContinueButton = menuClassicContinueButton,
            menuClassicNewButton = menuClassicNewButton,
            menuChaosContinueButton = menuChaosContinueButton,
            menuChaosNewButton = menuChaosNewButton,
            menuChaosStartButton = menuChaosStartButton,
            menuContinueButton = menuContinueButton,
            menuBackButton = menuBackButton
        )
    }

    private fun drawPlayModeScreen(canvas: Canvas, left: Float, contentWidth: Float, top: Float) {
        layoutPlayModeScreen(left, contentWidth, top)
        val widthDp = viewWidth / uiDensity
        val compact = widthDp <= 480f
        val short = (viewHeight / uiDensity) < 620f

        val classicSkin = BallSkinCatalog.ALL_SKINS.firstOrNull { it.id == "nodlo" } ?: selectedBallSkin()
        val chaosSkin = BallSkinCatalog.ALL_SKINS.firstOrNull { it.id == "gigi_glitch" } ?: selectedBallSkin()

        ModePickerRenderer.drawPlayModeScreen(
            canvas = canvas,
            menuClassicCard = menuClassicCard,
            menuChaosCard = menuChaosCard,
            menuClassicContinueButton = menuClassicContinueButton,
            menuClassicNewButton = menuClassicNewButton,
            menuChaosContinueButton = menuChaosContinueButton,
            menuChaosNewButton = menuChaosNewButton,
            menuChaosStartButton = menuChaosStartButton,
            menuContinueButton = menuContinueButton,
            activeMenuButton = activeMenuButton,
            classicProgress = modeProgress(GameMode.CLASSIC),
            chaosProgress = modeProgress(GameMode.CHAOS),
            classicStreak = modeStreak(GameMode.CLASSIC),
            chaosStreak = modeStreak(GameMode.CHAOS),
            compact = compact,
            short = short,
            safeCenterX = viewWidth * 0.5f,
            paint = paint,
            dp = dp(1f),
            t = { t(it) },
            fitText = { text, maxW -> fitText(text, maxW) },
            drawWorldAsset = { c, key, r, a -> drawWorldAsset(c, key, r, a) },
            menuButtonAt = { x, y -> menuButtonAt(x, y) },
            mascotBitmapClassic = brainballBitmap(classicSkin),
            mascotBitmapChaos = brainballBitmap(chaosSkin),
            cardFrameClassic = worldBitmap("card_frame_classic"),
            cardFrameChaos = worldBitmap("card_frame_chaos"),
            stateElapsed = stateElapsed
        )
    }

    private fun updateMenuPreviewGeometry() {
        val homeBg = worldBitmap("home_background")
        homeMenuController.updatePreviewGeometry(
            menuState = menuState,
            characterRect = characterRect,
            menuActionStartButtonTop = 0f,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            dp = uiDensity,
            homeBgWidth = homeBg?.width?.toFloat() ?: 0f,
            homeBgHeight = homeBg?.height?.toFloat() ?: 0f
        )
    }

    private fun drawBallSkin(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        animated: Boolean,
        locked: Boolean
    ) {
        BallSkinRenderer.drawBallSkin(
            canvas = canvas,
            cx = cx,
            cy = cy,
            radius = radius,
            skin = skin,
            animated = animated,
            locked = locked,
            menuPulse = menuPulse,
            richEffects = richEffects(),
            adaptiveQuality = adaptiveQuality,
            ballSkins = ballSkins,
            paint = paint,
            brainballBitmap = ::brainballBitmap
        )
    }

    private fun brainballBitmap(skin: BallSkin): Bitmap? =
        AssetResourceManager.brainballBitmap(skin, resources)

    private fun worldBitmap(key: String): Bitmap? =
        AssetResourceManager.worldBitmap(key, resources, context)

    private fun backgroundBitmap(key: String): Bitmap? =
        AssetResourceManager.backgroundBitmap(
            key = key,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            isLowProfile = renderProfile == RenderProfile.LOW,
            resources = resources,
            context = context
        )

    private fun recycleScaledBackgrounds() {
        AssetResourceManager.recycleScaledBackgrounds()
    }

    private fun drawWorldAsset(canvas: Canvas, key: String, bounds: RectF, alpha: Int = 255) {
        AssetResourceManager.drawWorldAsset(
            canvas = canvas,
            key = key,
            bounds = bounds,
            alpha = alpha,
            paint = paint,
            resources = resources,
            context = context
        )
    }

    private fun powerIconKey(power: BallPower): String = AssetResourceManager.powerIconKey(power)

    private fun drawCollection(canvas: Canvas) {
        drawBackground(canvas)
        layoutCollection()
        CollectionUiRenderer.drawCollectionScreen(
            canvas = canvas,
            collectionFilter = collectionFilter,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            safeTop22 = dp(22f),
            safeTop54 = dp(54f),
            safeTop76 = dp(76f),
            safeTop104 = dp(104f),
            safeBottom70 = viewHeight - dp(70f),
            safeBottom16 = viewHeight - dp(16f),
            pageContentLeft = pageContentLeft(),
            pageContentRight = pageContentRight(),
            collectionBackButton = collectionBackButton,
            collectionRestoreButton = collectionRestoreButton,
            collectionFilterRects = collectionFilterRects,
            collectionItemRects = collectionItemRects,
            activeCollectionIndex = activeCollectionIndex,
            collectionFilterActiveIndexFn = ::collectionFilterActiveIndex,
            collectionViewportTop = collectionViewportTop(),
            collectionViewportBottom = collectionViewportBottom(),
            menuPulse = menuPulse,
            ballSkins = ballSkins,
            selectedSkin = selectedBallSkin(),
            focusedSkin = focusedCollectionSkin(),
            unlockedCount = unlockedSkinCount(),
            bestStreak = bestStreak(),
            hypeBalance = hypeBalance(),
            formatHypeAmount = ::formatHypeAmount,
            isSkinUnlocked = ::isSkinUnlocked,
            brainballBitmap = ::brainballBitmap,
            collectionMessage = collectionMessage,
            nextRewardText = nextRewardText(),
            paint = paint,
            dp = uiDensity,
            t = ::t,
            fitText = ::fitText,
            drawFittedText = ::drawFittedText,
            drawWorldAsset = ::drawWorldAsset,
            drawUiButtonFrame = ::drawUiButtonFrame,
            drawUiIconAsset = ::drawUiIconAsset,
            powerIconKey = ::powerIconKey,
            ballPowerName = ::ballPowerName,
            ballPowerDescription = ::ballPowerDescription,
            unlockShortLabel = ::unlockShortLabel,
            unlockLongLabel = ::unlockLongLabel,
            premiumPriceLabel = ::premiumPriceLabel,
            premiumCompactPriceLabel = ::premiumCompactPriceLabel,
            skinHypePrice = progressRepository::skinHypePrice,
            collectionSort = collectionSort,
            collectionSortButton = CollectionTouchController.sortButtonRect
        )
    }

    private fun brainballAura(skin: BallSkin): Int =
        CollectionTouchController.calculateAura(skin, ballSkins)

    private fun ballPowerName(power: BallPower): String =
        TutorialCopy.ballPowerName(power, ::t)

    private fun ballPowerDescription(power: BallPower): String =
        TutorialCopy.ballPowerDescription(power, ::t)

    private fun handleCollectionTouch(event: MotionEvent) {
        CollectionTouchController.handleTouch(
            event = event,
            layoutCollection = ::layoutCollection,
            collectionTouchY = collectionTouchY,
            setTouchY = { collectionTouchY = it },
            collectionLastY = collectionLastY,
            setLastY = { collectionLastY = it },
            collectionDragging = collectionDragging,
            setDragging = { collectionDragging = it },
            activeCollectionIndex = activeCollectionIndex,
            setActiveIndex = { activeCollectionIndex = it },
            collectionScroll = collectionScroll,
            setScroll = { collectionScroll = it },
            collectionMaxScroll = collectionMaxScroll,
            collectionBackButton = collectionBackButton,
            collectionRestoreButton = collectionRestoreButton,
            collectionFilterRects = collectionFilterRects,
            collectionItemRects = collectionItemRects,
            viewportTop = collectionViewportTop(),
            viewportBottom = collectionViewportBottom(),
            ballSkins = ballSkins,
            onBack = {
                screen = Screen.MENU
            },
            onRestore = {
                collectionMessage = t("CHECKING GOOGLE PLAY PURCHASES")
                collectionMessageTimer = 3.4f
                audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
                purchaseBridge.restore()
            },
            onFilterSelected = { selectedFilter ->
                collectionFilter = selectedFilter
                collectionScroll = 0f
                collectionMessage = "${t("FILTER").uppercase()} / ${t(collectionFilter.labelKey).uppercase()}"
                collectionMessageTimer = 1.7f
                audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
            },
            onSkinTap = ::handleSkinTap,
            onHeroActionTap = { handleSkinTap(focusedCollectionSkin()) },
            onSortCycle = {
                collectionSort = collectionSort.next()
                collectionScroll = 0f
                collectionMessage = "${t("SORT").uppercase()} / ${CollectionUiRenderer.sortDisplayName(collectionSort, ::t)}"
                collectionMessageTimer = 1.7f
                audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
            },
            performHaptic = { performHapticFeedback(it) },
            dp = uiDensity
        )
    }
    private fun handleSkinTap(skin: BallSkin) {
        CollectionTouchController.handleSkinTap(
            skin = skin,
            ballSkins = ballSkins,
            isSkinUnlocked = ::isSkinUnlocked,
            prefs = prefs,
            hypeBalance = ::hypeBalance,
            spendHype = ::spendHype,
            formatHypeAmount = ::formatHypeAmount,
            unlockLongLabel = ::unlockLongLabel,
            unlockShortLabel = ::unlockShortLabel,
            skinHypePrice = progressRepository::skinHypePrice,
            brainballAura = ::brainballAura,
            purchaseBridge = purchaseBridge,
            performHaptic = { performHapticFeedback(it) },
            hapticSequence = { pulses -> hapticSequence(*pulses) },
            playSelection = audio::playSelection,
            playSoundEvent = { event, index -> audio.playEvent(event, index) },
            t = ::t,
            onSkinSelected = { selectedSkinId = it },
            onFocusSkin = { collectionFocusSkinId = it },
            setMessage = { message, duration ->
                collectionMessage = message
                collectionMessageTimer = duration
            }
        )
    }

    private fun layoutCollection() {
        val (scroll, maxScroll) = CollectionLayoutCalculator.layoutCollection(
            contentLeft = pageContentLeft(),
            contentRight = pageContentRight(),
            safeTop22 = dp(22f),
            safeTop68 = dp(68f),
            safeTop88 = dp(88f),
            safeTop192 = dp(192f),
            viewportTop = collectionViewportTop(),
            viewportBottom = collectionViewportBottom(),
            scroll = collectionScroll,
            ballSkins = ballSkins,
            filter = collectionFilter,
            sort = collectionSort,
            isSkinUnlocked = ::isSkinUnlocked,
            dp = uiDensity,
            backButton = collectionBackButton,
            restoreButton = collectionRestoreButton,
            filterRects = collectionFilterRects,
            itemRects = collectionItemRects
        )
        collectionScroll = scroll
        collectionMaxScroll = maxScroll
    }

    private fun collectionFilterActiveIndex(index: Int): Int = CollectionTouchController.filterActiveIndex(index)

    private fun collectionViewportTop(): Float = CollectionLayoutCalculator.viewportTop(
        sortButtonBottom = CollectionTouchController.sortButtonRect.takeUnless { it.isEmpty }?.bottom,
        heroStageBottom = CollectionTouchController.heroStageRect.takeUnless { it.isEmpty }?.bottom,
        dp = dp(1f)
    )

    private fun collectionViewportBottom(): Float =
        ViewportLayoutCalculator.bottomInset(viewHeight.toFloat(), dp(1f), 78f)

    private fun drawLeaderboards(canvas: Canvas) {
        val scores = LeaderboardBoard.entries.map { board ->
            val score = leaderboardScore(board)
            when (board) {
                LeaderboardBoard.CLASSIC_LEVEL,
                LeaderboardBoard.CHAOS_LEVEL -> "L${score.toString().padStart(2, '0')}"
                LeaderboardBoard.CLASSIC_STREAK,
                LeaderboardBoard.CHAOS_STREAK -> "x$score"
            }
        }
        drawBackground(canvas)
        layoutLeaderboards()
        LeaderboardUiRenderer.drawScreen(
            canvas = canvas,
            pageLeft = pageContentLeft(),
            pageRight = pageContentRight(),
            pageWidth = pageContentWidth(),
            viewWidth = viewWidth.toFloat(),
            top56 = dp(56f),
            top78 = dp(78f),
            bandTop = dp(98f),
            bandBottom = dp(158f),
            top118 = dp(118f),
            top146 = dp(146f),
            bottom70 = viewHeight - dp(70f),
            bottom16 = viewHeight - dp(16f),
            configured = leaderboardBridge.configured,
            accountStatusLabel = accountStatusLabel(),
            accountStatusColor = accountStatusColor(),
            highestLevelText = "L${max(modeHighestLevel(GameMode.CLASSIC), modeHighestLevel(GameMode.CHAOS)).toString().padStart(2, '0')}",
            bestStreakText = "x${max(modeBestStreak(GameMode.CLASSIC), modeBestStreak(GameMode.CHAOS))}",
            leaderboardScores = scores,
            activeLeaderboardIndex = activeLeaderboardIndex,
            leaderboardBackButton = leaderboardBackButton,
            leaderboardItemRects = leaderboardItemRects,
            leaderboardMessage = leaderboardMessage,
            paint = paint,
            dp = uiDensity,
            t = ::t,
            fitText = ::fitText,
            drawBackButton = { targetCanvas: Canvas, rect: RectF, active: Boolean ->
                drawUiButtonFrame(targetCanvas, rect, active, 0xFF1DE8C8.toInt(), 8f)
                drawUiIconAsset(targetCanvas, "ui_back", rect, padDp = -1f, alpha = 245)
            },
            selectedSkin = selectedBallSkin(),
            playerBrainballBitmap = brainballBitmap(selectedBallSkin())
        )
    }

    private fun accountStatusLabel(): String = when (accountState) {
        AccountState.CONNECTING -> t("CONNECTING TO GOOGLE PLAY")
        AccountState.SIGNED_IN -> t("GOOGLE PLAY CONNECTED")
        AccountState.SIGNED_OUT -> t("GOOGLE PLAY NOT CONNECTED")
        AccountState.UNAVAILABLE -> t("PLAY GAMES UNAVAILABLE")
    }

    private fun accountStatusColor(): Int = when (accountState) {
        AccountState.CONNECTING -> 0xFF45F2FF.toInt()
        AccountState.SIGNED_IN -> 0xFF64E572.toInt()
        AccountState.SIGNED_OUT -> 0xFFFFCF4A.toInt()
        AccountState.UNAVAILABLE -> 0xFFFF6B8A.toInt()
    }

    private fun handleLeaderboardTouch(event: MotionEvent) {
        LeaderboardTouchController.handleTouch(
            event = event,
            layoutLeaderboards = ::layoutLeaderboards,
            activeLeaderboardIndex = activeLeaderboardIndex,
            setActiveIndex = { activeLeaderboardIndex = it },
            leaderboardBackButton = leaderboardBackButton,
            leaderboardItemRects = leaderboardItemRects,
            leaderboardBridge = leaderboardBridge,
            performHaptic = { performHapticFeedback(it) },
            t = ::t,
            onBack = {
                screen = Screen.MENU
            },
            setMessage = { message, duration ->
                leaderboardMessage = message
                leaderboardMessageTimer = duration
            },
            postAction = { action ->
                post {
                    synchronized(lock) {
                        action()
                    }
                }
            }
        )
    }
    private fun settingsDensity(): Float = resources.displayMetrics.density

    private fun settingsBreakpoint() =
        SettingsLayoutCalculator.breakpointFor(viewWidth.toFloat(), settingsDensity(), viewHeight.toFloat())

    private fun settingsHeaderMetrics(): SettingsHeaderMetrics =
        SettingsLayoutCalculator.computeHeader(
            viewWidth.toFloat(),
            settingsContentWidth(),
            settingsDensity(),
            viewHeight.toFloat(),
            settingsSafeInsetTop()
        )

    private fun settingsSafeInsetTop(): Float {
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(this) ?: return 0f
        return insets.getInsets(
            androidx.core.view.WindowInsetsCompat.Type.systemBars() or
                androidx.core.view.WindowInsetsCompat.Type.displayCutout()
        ).top.toFloat()
    }

    private fun settingsSafeInsetBottom(): Float {
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(this) ?: return 0f
        return insets.getInsets(
            androidx.core.view.WindowInsetsCompat.Type.systemBars() or
                androidx.core.view.WindowInsetsCompat.Type.displayCutout()
        ).bottom.toFloat()
    }

    private fun settingsProfileLabel(): String =
        AccountProfileDisplay.headerName(accountState, accountDisplayName, t("User"))

    private fun settingsContentWidth(): Float =
        SettingsLayoutCalculator.contentWidth(viewWidth.toFloat(), settingsDensity(), viewHeight.toFloat())

    private fun settingsContentLeft(): Float =
        ViewportLayoutCalculator.centeredLeft(viewWidth.toFloat(), settingsContentWidth())

    private fun settingsContentRight(): Float =
        ViewportLayoutCalculator.contentRight(settingsContentLeft(), settingsContentWidth())

    private fun drawSettings(canvas: Canvas) {
        drawBackground(canvas)
        val compact = settingsBreakpoint() == SettingsBreakpoint.MOBILE
        val header = settingsHeaderMetrics()
        layoutSettings()
        SettingsUiRenderer.drawSettingsScreen(
            canvas = canvas,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            safeCenterX = viewWidth * 0.5f,
            pageContentLeft = settingsContentLeft(),
            pageContentRight = settingsContentRight(),
            settingsViewportTop = header.viewportTop,
            settingsViewportBottom = settingsViewportBottom(),
            compact = compact,
            activeSettingsButton = activeSettingsButton,
            settingsMasterButton = SettingsTouchController.masterButton,
            settingsMasterSlider = SettingsTouchController.masterSlider,
            settingsMasterVolume = settingsMasterVolume,
            settingsMusicButton = SettingsTouchController.musicButton,
            settingsMusicSlider = SettingsTouchController.musicSlider,
            settingsMusicVolume = settingsMusicVolume,
            settingsSfxButton = SettingsTouchController.sfxButton,
            settingsSfxSlider = SettingsTouchController.sfxSlider,
            settingsSfxVolume = settingsSfxVolume,
            settingsHapticToggle = SettingsTouchController.hapticToggle,
            settingsHapticEnabled = settingsHapticEnabled,
            settingsShakeToggle = SettingsTouchController.shakeToggle,
            settingsScreenShake = settingsScreenShake,
            settingsPerformanceToggle = SettingsTouchController.performanceToggle,
            settingsPerformanceMode = settingsPerformanceMode,
            settingsLanguageButton = SettingsTouchController.languageButton,
            selectedLanguageLabel = KavvoroI18n.label(context, KavvoroI18n.selected(context)),
            settingsAccountButton = SettingsTouchController.accountButton,
            accountStatusLabel = accountStatusLabel(),
            settingsPrivacyButton = SettingsTouchController.privacyButton,
            settingsTermsButton = SettingsTouchController.termsButton,
            settingsDataDeletionButton = SettingsTouchController.dataDeletionButton,
            settingsAboutButton = SettingsTouchController.aboutButton,
            versionName = BuildConfig.VERSION_NAME,
            settingsResetButton = SettingsTouchController.resetButton,
            settingsBackButton = SettingsTouchController.backButton,
            settingsResetConfirm = settingsResetConfirm,
            settingsResetCancelButton = SettingsTouchController.resetCancelButton,
            settingsResetConfirmButton = SettingsTouchController.resetConfirmButton,
            paint = paint,
            dp = settingsDensity(),
            t = ::t,
            fitText = ::fitText,
            isRtl = resources.configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL,
            activeSettingsTab = activeSettingsTab,
            tabAudio = SettingsTouchController.tabAudio,
            tabGameplay = SettingsTouchController.tabGameplay,
            tabSystem = SettingsTouchController.tabSystem,
            tabInfo = SettingsTouchController.tabInfo,
            contentPanel = SettingsTouchController.contentPanel,
            header = header,
            profileName = settingsProfileLabel(),
            profileOnline = accountState == AccountState.SIGNED_IN,
            breakpoint = settingsBreakpoint()
        )
    }

    private fun layoutSettings() {
        settingsLayoutCalculator.calculate(
            left = settingsContentLeft(),
            right = settingsContentRight(),
            contentWidth = settingsContentWidth(),
            viewportTop = settingsViewportTop(),
            viewportBottom = settingsViewportBottom(),
            settingsScroll = settingsScroll,
            dp = settingsDensity(),
            currentTab = activeSettingsTab,
            isLandscape = viewWidth > viewHeight,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat()
        )
        SettingsTouchController.syncLayoutRects()
        settingsScroll = settingsLayoutCalculator.scroll
        settingsMaxScroll = settingsLayoutCalculator.maxScroll
    }

    private fun handleSettingsTouch(event: MotionEvent): (() -> Unit)? {
        var deferredAction: (() -> Unit)? = null
        SettingsTouchController.handleTouch(
            event = event,
            settingsResetConfirm = settingsResetConfirm,
            settingsResetCancelButton = SettingsTouchController.resetCancelButton,
            settingsResetConfirmButton = SettingsTouchController.resetConfirmButton,
            onResetCancelled = { settingsResetConfirm = false },
            onResetConfirmed = ::resetGameDataFromSettings,
            layoutSettings = ::layoutSettings,
            settingsTouchY = settingsTouchY,
            setTouchY = { settingsTouchY = it },
            settingsLastY = settingsLastY,
            setLastY = { settingsLastY = it },
            settingsDragging = settingsDragging,
            setDragging = { settingsDragging = it },
            activeSettingsButton = activeSettingsButton,
            setActiveButton = { activeSettingsButton = it },
            settingsScroll = settingsScroll,
            setScroll = { settingsScroll = it },
            settingsMaxScroll = settingsMaxScroll,
            tutorialTouchSlop = tutorialTouchSlop,
            buttonAt = ::settingsButtonAt,
            updateSlider = ::updateSettingsSlider,
            handleAction = { deferredAction = handleSettingsAction(it) },
            requestPostInvalidate = { postInvalidate() }
        )
        return deferredAction
    }

    private fun settingsButtonAt(x: Float, y: Float): SettingsButton =
        SettingsTouchController.buttonAt(x, y, settingsViewportTop(), settingsViewportBottom())

    private fun updateSettingsSlider(button: SettingsButton, x: Float) {
        SettingsTouchController.updateSlider(
            button = button,
            x = x,
            masterSlider = SettingsTouchController.masterSlider,
            musicSlider = SettingsTouchController.musicSlider,
            sfxSlider = SettingsTouchController.sfxSlider,
            onMasterChanged = { settingsMasterVolume = it },
            onMusicChanged = { settingsMusicVolume = it },
            onSfxChanged = { settingsSfxVolume = it }
        )
        prefs.edit {
            putInt(GameProgressRepository.SETTINGS_MASTER_VOLUME_KEY, settingsMasterVolume)
            putInt(GameProgressRepository.SETTINGS_MUSIC_VOLUME_KEY, settingsMusicVolume)
            putInt(GameProgressRepository.SETTINGS_SFX_VOLUME_KEY, settingsSfxVolume)
        }
        audio.setVolumes(settingsMasterVolume, settingsMusicVolume, settingsSfxVolume)
    }

    private fun handleSettingsAction(button: SettingsButton): (() -> Unit)? {
        if (button != SettingsButton.NONE && settingsHapticEnabled) {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        when (button) {
            SettingsButton.BACK -> navigateToMenuFromSettings()
            SettingsButton.TAB_AUDIO -> {
                activeSettingsTab = SettingsTab.AUDIO
                settingsScroll = 0f
                layoutSettings()
                postInvalidate()
            }
            SettingsButton.TAB_GAMEPLAY -> {
                activeSettingsTab = SettingsTab.GAMEPLAY
                settingsScroll = 0f
                layoutSettings()
                postInvalidate()
            }
            SettingsButton.TAB_SYSTEM -> {
                activeSettingsTab = SettingsTab.SYSTEM
                settingsScroll = 0f
                layoutSettings()
                postInvalidate()
            }
            SettingsButton.TAB_INFO -> {
                activeSettingsTab = SettingsTab.INFO
                settingsScroll = 0f
                layoutSettings()
                postInvalidate()
            }
            SettingsButton.MASTER_VOLUME,
            SettingsButton.MUSIC_VOLUME,
            SettingsButton.SFX_VOLUME,
            SettingsButton.NONE -> Unit
            SettingsButton.HAPTIC -> {
                settingsHapticEnabled = !settingsHapticEnabled
                isHapticFeedbackEnabled = settingsHapticEnabled
                prefs.edit { putBoolean(GameProgressRepository.SETTINGS_HAPTIC_KEY, settingsHapticEnabled) }
            }
            SettingsButton.SCREEN_SHAKE -> {
                settingsScreenShake = !settingsScreenShake
                prefs.edit { putBoolean(GameProgressRepository.SETTINGS_SCREEN_SHAKE_KEY, settingsScreenShake) }
            }
            SettingsButton.PERFORMANCE -> {
                settingsPerformanceMode = !settingsPerformanceMode
                prefs.edit { putBoolean(GameProgressRepository.SETTINGS_PERFORMANCE_KEY, settingsPerformanceMode) }
                val toastText = if (settingsPerformanceMode) {
                    t("⚡ PERFORMANCE MODE: ON (ECO 60FPS / MINIMAL SHADERS)")
                } else {
                    t("✦ PERFORMANCE MODE: OFF (MAXIMUM AAA FIDELITY)")
                }
                android.widget.Toast.makeText(context, toastText, android.widget.Toast.LENGTH_SHORT).show()
                audio.playEvent(SoundEvent.UI_TAP)
            }
            SettingsButton.LANGUAGE -> {
                languageReturnScreen = Screen.SETTINGS
                screen = Screen.LANGUAGE
                activeLanguageIndex = -1
                triggerScreenTransition(0xFF45F2FF.toInt())
            }
            SettingsButton.ACCOUNT -> return {
                when (accountState) {
                    AccountState.CONNECTING -> Toast.makeText(
                        context,
                        t("CONNECTING TO GOOGLE PLAY"),
                        Toast.LENGTH_SHORT
                    ).show()
                    AccountState.SIGNED_IN -> Toast.makeText(
                        context,
                        t("GOOGLE PLAY CONNECTED"),
                        Toast.LENGTH_SHORT
                    ).show()
                    AccountState.SIGNED_OUT,
                    AccountState.UNAVAILABLE -> accountBridge.retry()
                }
            }
            SettingsButton.PRIVACY -> return { privacyBridge.openPrivacyPolicy() }
            SettingsButton.TERMS -> return { privacyBridge.openTermsOfService() }
            SettingsButton.DATA_DELETION -> return { privacyBridge.openDataDeletion() }
            SettingsButton.ABOUT -> return { privacyBridge.openAbout() }
            SettingsButton.RESET -> settingsResetConfirm = true
        }
        return null
    }

    private fun resetGameDataFromSettings() {
        progressRepository.resetAllProgressPreservingSettings()
        settingsResetConfirm = false
        selectedSkinId = DEFAULT_SKIN_ID
        collectionFocusSkinId = DEFAULT_SKIN_ID
        selectedMenuMode = GameMode.CLASSIC
        gameMode = GameMode.CLASSIC
        menuState = MenuState.MODES
        streak = 0
        levelIndex = 1
        configureStage(viewWidth, viewHeight, reset = true)
        navigateToMenuFromSettings()
    }

    private fun navigateToMenuFromSettings() {
        screen = Screen.MENU
        menuState = MenuState.MODES
        settingsResetConfirm = false
        activeSettingsButton = SettingsButton.NONE
        triggerScreenTransition(0xFF8AA6FF.toInt())
    }

    private fun settingsViewportTop(): Float = settingsHeaderMetrics().viewportTop

    private fun settingsViewportBottom(): Float =
        viewHeight - maxOf(24f * settingsDensity(), settingsSafeInsetBottom())

    private fun debugLaunchScreen(): Screen {
        if (!BuildConfig.DEBUG) return Screen.MENU
        val extra = (context as? Activity)?.intent?.getStringExtra("screen")?.lowercase(Locale.ROOT)
        return when (extra) {
            "language" -> Screen.LANGUAGE
            "settings" -> Screen.SETTINGS
            "missions" -> Screen.MISSIONS
            else -> Screen.MENU
        }
    }

    private fun debugLaunchSettingsTab(): SettingsTab {
        if (!BuildConfig.DEBUG) return SettingsTab.AUDIO
        val extra = (context as? Activity)?.intent?.getStringExtra("tab")?.lowercase(Locale.ROOT)
        return when (extra) {
            "gameplay" -> SettingsTab.GAMEPLAY
            "system" -> SettingsTab.SYSTEM
            "info", "about" -> SettingsTab.INFO
            else -> SettingsTab.AUDIO
        }
    }

    private fun drawLanguageSelector(canvas: Canvas) {
        drawBackground(canvas)
        val selected = KavvoroI18n.selected(context)
        layoutLanguageSelector()
        LanguageSelectorRenderer.drawScreen(
            canvas = canvas,
            side = 0f,
            contentRight = viewWidth.toFloat(),
            contentWidth = viewWidth.toFloat(),
            compact = viewWidth < dp(520f),
            centerX = viewWidth * 0.5f,
            selected = selected,
            activeLanguageIndex = activeLanguageIndex,
            backButtonRect = languageBackButton,
            itemRects = languageItemRects,
            viewportTop = languageViewportTop(),
            viewportBottom = languageViewportBottom(),
            footerRect = languageFooterRect,
            deckRect = languageDeckRect,
            typeface = languageTypeface,
            paint = paint,
            dp = uiDensity,
            t = ::t,
            fitText = ::fitText,
            context = context
        )
    }

    private fun handleLanguageTouch(event: MotionEvent) {
        LanguageTouchController.handleTouch(
            event = event,
            layoutSelector = ::layoutLanguageSelector,
            activeLanguageIndex = activeLanguageIndex,
            setActiveIndex = { activeLanguageIndex = it },
            languageDragging = languageDragging,
            setDragging = { languageDragging = it },
            languageTouchY = languageTouchY,
            setTouchY = { languageTouchY = it },
            languageLastY = languageLastY,
            setLastY = { languageLastY = it },
            languageScroll = languageScroll,
            setScroll = { languageScroll = it },
            languageMaxScroll = languageMaxScroll,
            languageBackButton = languageBackButton,
            languageItemRects = languageItemRects,
            displayLanguages = KavvoroLanguage.selectableLanguages,
            viewportTop = languageViewportTop(),
            viewportBottom = languageViewportBottom(),
            context = context,
            audio = audio,
            onBack = {
                screen = languageReturnScreen
                triggerScreenTransition(0xFF45F2FF.toInt())
            },
            performHaptic = { performHapticFeedback(it) },
            dp = uiDensity,
            layout = latestLanguageLayout
        )
    }

    private fun layoutLanguageSelector() {
        val result = LanguageSelectorLayoutCalculator.layoutLanguageSelector(
            side = 0f,
            contentWidth = viewWidth.toFloat(),
            compact = viewWidth < dp(520f),
            headerY = dp(28f),
            viewportTop = languageViewportTop(),
            viewportBottom = languageViewportBottom(),
            languageScroll = languageScroll,
            dp = uiDensity,
            languageBackButton = languageBackButton,
            languageItemRects = languageItemRects,
            languageDeckRect = languageDeckRect,
            languageFooterRect = languageFooterRect,
            viewportWidth = viewWidth.toFloat()
        )
        languageScroll = result.scroll
        languageMaxScroll = result.maxScroll
        latestLanguageLayout = result
    }

    private fun languageViewportTop(): Float = 0f

    private fun languageViewportBottom(): Float = viewHeight.toFloat()
    private fun layoutLeaderboards() {
        LeaderboardLayoutCalculator.layoutLeaderboards(
            side = pageContentLeft(),
            contentRight = pageContentRight(),
            backTop = dp(28f),
            itemsTop = dp(176f),
            viewHeight = viewHeight.toFloat(),
            dp = uiDensity,
            leaderboardBackButton = leaderboardBackButton,
            leaderboardItemRects = leaderboardItemRects
        )
    }

    private fun leaderboardScore(board: LeaderboardBoard): Int =
        LeaderboardTouchController.leaderboardScore(
            board = board,
            prefs = prefs,
            fairHighestLevelKey = ::fairHighestLevelKey,
            fairBestStreakKey = ::fairBestStreakKey,
            modeHighestLevel = ::modeHighestLevel,
            modeBestStreak = ::modeBestStreak
        )

    private fun ensureFairLeaderboardSnapshot() {
        LeaderboardTouchController.ensureFairLeaderboardSnapshot(
            prefs = prefs,
            fairHighestLevelKey = ::fairHighestLevelKey,
            fairBestStreakKey = ::fairBestStreakKey,
            modeHighestLevel = ::modeHighestLevel,
            modeBestStreak = ::modeBestStreak
        )
    }

    private fun syncLeaderboards() {
        LeaderboardTouchController.syncLeaderboards(
            prefs = prefs,
            leaderboardBridge = leaderboardBridge,
            fairHighestLevelKey = ::fairHighestLevelKey,
            fairBestStreakKey = ::fairBestStreakKey,
            modeHighestLevel = ::modeHighestLevel,
            modeBestStreak = ::modeBestStreak
        )
    }

    private fun tutorialActionLabel(): String =
        TutorialTouchController.actionLabel(::levelHasCurse, ::t)

    private fun gameplayBallScale(skin: BallSkin): Float =
        GameplayArenaRenderer.gameplayBallScale(skin, ballSkins.indexOfFirst { it.id == skin.id })

    private fun isCompactHud(): Boolean = GameplayHudRenderer.isCompactHud(viewWidth.toFloat(), dp(1f))

    private fun hudHasRibbon(): Boolean = GameplayHudRenderer.hudHasRibbon(selectedBallSkin().power, level.curses.isNotEmpty())

    private fun gameplayHudBottom(): Float = GameplayHudRenderer.gameplayHudBottom(isCompactHud(), hudHasRibbon(), dp(1f))

    private fun gameplayOverlayTop(): Float = GameplayHudRenderer.gameplayOverlayTop(isCompactHud(), hudHasRibbon(), dp(1f))

    private fun currentModeWarning(): ModeWarning? =
        GameplayHudRenderer.currentModeWarning(level, ::levelHasCurse)

    private fun drawTutorialHint(canvas: Canvas) {
        val accent = currentModeWarning()?.accent ?: level.accent
        val isArabic = KavvoroI18n.active(context) == KavvoroLanguage.AR
        val lessonLines = tutorialLessonLines() + tutorialObstacleLine()
        TutorialRenderer.drawTutorialHint(
            canvas = canvas,
            tutorialCardVisible = tutorialCardVisible,
            tutorialCardBounds = tutorialCardBounds,
            tutorialStartButton = tutorialStartButton,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            dp = dp(1f),
            accent = accent,
            levelIndex = level.index,
            tutorialLastLevel = TUTORIAL_LAST_LEVEL,
            lessonLines = lessonLines,
            tutorialIconKey = tutorialIconKey(),
            actionLabel = tutorialActionLabel(),
            actionPressed = tutorialInputGate.actionPressed,
            isArabic = isArabic,
            paint = paint,
            textPaint = textPaint,
            t = { t(it) },
            fitText = { text, maxW -> fitText(text, maxW) },
            drawWorldAsset = { c, key, r, a -> drawWorldAsset(c, key, r, a) },
            drawFittedText = { c, text, x, y, maxW, base, min -> drawFittedText(c, text, x, y, maxW, base, min) }
        )
    }

    private fun tutorialLessonLines(): List<String> =
        TutorialTouchController.lessonLines(level, ::t)

    private fun tutorialObstacleLine(): String =
        TutorialTouchController.obstacleLine(level, ::levelHasCurse, ::t)

    private fun tutorialIconKey(): String =
        TutorialRenderer.tutorialIconKey(level, ::levelHasCurse)

    private fun fitText(text: String, maxWidth: Float): String =
        UiWidgetRenderer.fitText(context, text, maxWidth, textPaint, uiDensity)

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        startSizeDp: Float,
        minSizeDp: Float
    ) {
        UiWidgetRenderer.drawFittedText(
            canvas = canvas,
            context = context,
            text = text,
            x = x,
            y = y,
            maxWidth = maxWidth,
            startSizeDp = startSizeDp,
            minSizeDp = minSizeDp,
            textPaint = textPaint,
            dp = uiDensity
        )
    }

    private fun drawIconButton(canvas: Canvas, rect: RectF, id: ButtonId) {
        UiWidgetRenderer.drawIconButton(
            canvas = canvas,
            rect = rect,
            id = id,
            active = activeButton == id,
            sfxMuted = sfxMuted,
            musicMuted = musicMuted,
            levelAccent = level.accent,
            paint = paint,
            dp = uiDensity,
            drawWorldAsset = ::drawWorldAsset
        )
    }

    private fun drawUiButtonFrame(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        accent: Int,
        cornerDp: Float
    ) {
        UiWidgetRenderer.drawUiButtonFrame(canvas, rect, active, accent, cornerDp, paint, uiDensity)
    }

    private fun drawUiIconAsset(canvas: Canvas, key: String, rect: RectF, padDp: Float, alpha: Int) {
        UiWidgetRenderer.drawUiIconAsset(
            canvas,
            key,
            rect,
            padDp,
            alpha,
            uiDensity,
            ::drawWorldAsset
        )
    }
    private fun drawOutcome(canvas: Canvas) {
        OutcomeUiRenderer.drawOutcome(
            canvas = canvas,
            won = state == GameState.WON,
            levelAccent = level.accent,
            levelTitle = level.title,
            gameMode = gameMode,
            gameplayHudBottom = gameplayHudBottom(),
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            dp = dp(1f),
            lastScore = lastScore,
            lastHypeScore = lastHypeScore,
            maxChain = maxChain,
            lastRiftBreak = lastRiftBreak,
            lastRiftBreakBonus = lastRiftBreakBonus,
            lastRiftBreakReason = lastRiftBreakReason,
            rewardMessage = rewardMessage,
            nextRewardText = nextRewardText(),
            nextRewardInfo = nextRewardInfo(),
            resultShareButton = resultShareButton,
            resultNextButton = resultNextButton,
            resultRetryButton = resultRetryButton,
            continueRequiresAd = continueRequiresAd(),
            streak = streak,
            activeButton = activeButton,
            selectedBallSkin = selectedBallSkin(),
            archetypeLabel = levelArchetype().label,
            paint = paint,
            textPaint = textPaint,
            t = { t(it) },
            fitText = { text, maxW -> fitText(text, maxW) },
            localizedLevelTitle = { localizedLevelTitle(it) },
            drawBallSkin = { c, cx, cy, r, s, a, l -> drawBallSkin(c, cx, cy, r, s, a, l) },
            drawWorldAsset = { c, key, r, a -> drawWorldAsset(c, key, r, a) }
        )
    }

    private fun drawAdPlaceholder(canvas: Canvas) {
        AdScreenRenderer.drawAdScreen(
            canvas = canvas,
            gameMode = gameMode,
            pendingAdAction = pendingAdAction,
            adReason = adReason,
            adLoading = adLoading,
            stateElapsed = stateElapsed,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            dp = uiDensity,
            adButton = adButton,
            activeButton = activeButton,
            paint = paint,
            textPaint = textPaint,
            t = ::t,
            fitText = ::fitText
        )
    }

    private fun drawExportingOverlay(canvas: Canvas) {
        ShareExportOverlayRenderer.drawExportingOverlay(
            canvas = canvas,
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            levelAccent = level.accent,
            menuPulse = menuPulse,
            dp = uiDensity,
            paint = paint,
            textPaint = textPaint,
            t = ::t,
            fitText = ::fitText,
            drawWorldAsset = ::drawWorldAsset
        )
    }

    private fun isLargeScreenLayout(): Boolean =
        resources.configuration.smallestScreenWidthDp >= 600

    private fun pageContentWidth(): Float = ViewportLayoutCalculator.centeredContentWidth(
        viewWidth = viewWidth.toFloat(),
        density = dp(1f),
        horizontalInsetDp = 36f,
        maxWidthDp = if (isLargeScreenLayout()) 920f else 540f
    )

    private fun pageContentLeft(): Float =
        ViewportLayoutCalculator.centeredLeft(viewWidth.toFloat(), pageContentWidth())

    private fun pageContentRight(): Float =
        ViewportLayoutCalculator.contentRight(pageContentLeft(), pageContentWidth())

    private fun layoutButtons() {
        GameplayHudRenderer.layoutToolbarButtons(
            viewWidth = viewWidth.toFloat(),
            dp = ::dp,
            homeButton = homeButton,
            restartButton = restartButton,
            sfxButton = sfxButton,
            musicButton = musicButton,
            shareButton = shareButton,
            nextButton = nextButton
        )
        OutcomeUiRenderer.layoutOutcomeButtons(
            viewWidth = viewWidth.toFloat(),
            viewHeight = viewHeight.toFloat(),
            won = state == GameState.WON,
            dp = ::dp,
            resultShareButton = resultShareButton,
            resultNextButton = resultNextButton,
            resultRetryButton = resultRetryButton
        )
    }

    private fun layoutMenuButtons() {
        homeLayoutCalculator.calculate(
            width = viewWidth.toFloat(),
            height = viewHeight.toFloat(),
            displayDensity = uiDensity
        )
        homeMenuController.syncHomeLayoutRects(homeLayoutCalculator)

        if (menuState != MenuState.MODES) {
            val left = homeLayoutCalculator.contentRect.left
            val contentWidth = homeLayoutCalculator.contentRect.width()
            val top = homeLayoutCalculator.brandRect.top
            layoutPlayModeScreen(left, contentWidth, top)
        }
    }

    private fun buttonAt(x: Float, y: Float): ButtonId = when {
        screen == Screen.AD -> AdScreenTouchController.buttonAt(x, y)
        else -> OutcomeTouchController.buttonAt(x, y, state)
            .takeIf { it != ButtonId.NONE }
            ?: GameplayTouchController.buttonAt(x, y, state)
    }

    private fun menuButtonAt(x: Float, y: Float): MenuButton =
        homeMenuController.menuButtonAt(x, y, menuState)

    private fun shareRun() {
        if (exportingShare) return
        val request = synchronized(lock) {
            val skin = selectedBallSkin()
            ReplayShareController.buildShareRequest(
                score = lastScore,
                skin = skin,
                gameMode = gameMode,
                modeMenuTitle = gameMode.menuTitle(::t),
                level = level,
                playerLine = playerLine,
                replayFrames = replayFrames,
                ball = ball,
                pulseIntensity = pulseIntensity,
                state = state,
                lastHypeScore = lastHypeScore,
                maxChain = maxChain,
                streak = streak,
                lastRiftBreak = lastRiftBreak,
                lastRiftBreakReason = lastRiftBreakReason,
                archetype = levelArchetype(),
                simElapsed = simElapsed,
                gameplayBallScale = gameplayBallScale(skin),
                t = ::t
            )
        }

        exportingShare = true
        hapticSequence(
            HapticFeedbackConstants.CONTEXT_CLICK to 0L,
            HapticFeedbackConstants.CLOCK_TICK to 90L
        )
        Thread(
            {
                val exporter = ReplayVideoExporter(context.applicationContext)
                try {
                    val video = exporter.export(request.payload)
                    post {
                        exportingShare = false
                        recordShareReward()
                        ReplayShareController.shareVideo(context, video, request.text, ::t)
                    }
                } catch (error: Throwable) {
                    Log.e("KavvoroReplay", "Video export failed; falling back to text share", error)
                    post {
                        exportingShare = false
                        recordShareReward()
                        ReplayShareController.shareText(context, request.text, ::t)
                    }
                }
            },
            "kavvoro-replay-export"
        ).start()
    }

    private fun recordShareReward() {
        val before = unlockedSkinIds()
        val used = prefs.getInt(SHARE_COUNT_KEY, 0)
        val total = used + 1
        prefs.edit { putInt(SHARE_COUNT_KEY, total) }
        val unlocked = unlockedSkinIds()
        val newSkin = ballSkins.firstOrNull { it.id in (unlocked - before) }
        if (newSkin != null) {
            rewardMessage = rewardLine(newSkin)
            audio.playEvent(SoundEvent.UNLOCK, selectedBallIndex())
            hapticSequence(
                HapticFeedbackCompat.confirm to 0L,
                HapticFeedbackConstants.LONG_PRESS to 90L
            )
        } else {
            rewardMessage = ReplayShareController.nextShareRewardText(
                totalShares = total,
                ballSkins = ballSkins,
                isSkinUnlocked = ::isSkinUnlocked,
                t = ::t
            ) ?: rewardLine(null)
            hapticSequence(HapticFeedbackConstants.CLOCK_TICK to 0L)
        }
    }

    private fun levelHasCurse(type: CurseType): Boolean {
        return level.hasCurse(type)
    }

    private fun hasCurse(spec: LevelSpec, type: CurseType): Boolean {
        return spec.hasCurse(type)
    }

    private fun levelArchetype(spec: LevelSpec = level): LevelArchetype =
        GameplayHudRenderer.levelArchetype(spec, gameMode)

    private fun selectedBallSkin(): BallSkin = progressRepository.selectedBallSkin(selectedSkinId)

    private fun focusedCollectionSkin(): BallSkin =
        BallSkinCatalog.byId(collectionFocusSkinId) ?: selectedBallSkin()

    private fun selectedBallIndex(): Int =
        BallSkinCatalog.indexOf(selectedBallSkin().id)

    private fun isSkinUnlocked(skin: BallSkin): Boolean = progressRepository.isSkinUnlocked(skin)

    private fun unlockedSkinIds(): Set<String> = progressRepository.unlockedSkinIds()

    private fun unlockedSkinCount(): Int = progressRepository.unlockedSkinCount()

    private fun bestStreak(): Int = progressRepository.bestStreak()

    private fun hypeBalance(): Int = progressRepository.hypeBalance()

    private fun spendHype(amount: Int) = progressRepository.spendHype(amount)

    private fun formatHypeAmount(value: Int): String = progressRepository.formatHypeAmount(value)

    private fun rewardLine(newSkin: BallSkin?): String =
        OutcomeTouchController.rewardLine(newSkin, ::nextRewardText, ::t)

    private fun finishRewardLine(newSkin: BallSkin?): String =
        OutcomeTouchController.finishRewardLine(
            newSkin = newSkin,
            lastDailyBonus = lastDailyBonus,
            lastRiftBreak = lastRiftBreak,
            lastRiftBreakBonus = lastRiftBreakBonus,
            lastStreakMilestoneBonus = lastStreakMilestoneBonus,
            streak = streak,
            nextRewardText = ::nextRewardText,
            t = ::t
        )

    private fun claimDailyRiftBonus(): Int = progressRepository.claimDailyRiftBonus(gameMode)

    private fun dailyRiftBonusClaimed(): Boolean = progressRepository.dailyRiftBonusClaimed()

    private fun nextRewardText(excludeId: String? = null): String? =
        progressRepository.nextRewardText(excludeId)

    private fun nextRewardInfo(excludeId: String? = null): NextReward? =
        progressRepository.nextRewardInfo(excludeId)

    private fun unlockShortLabel(skin: BallSkin): String = progressRepository.unlockShortLabel(skin)

    private fun unlockLongLabel(skin: BallSkin): String = progressRepository.unlockLongLabel(skin)

    private fun premiumPriceLabel(skin: BallSkin): String = progressRepository.premiumPriceLabel(skin)

    private fun premiumCompactPriceLabel(skin: BallSkin): String =
        premiumPriceLabel(skin).substringBefore(" ")

    private fun modeProgress(mode: GameMode): Int = progressRepository.modeProgress(mode)

    private fun modeStreak(mode: GameMode): Int =
        progressRepository.modeStreak(mode, if (mode == gameMode) streak else 0)

    private fun modeHighestLevel(mode: GameMode): Int = progressRepository.modeHighestLevel(mode)

    private fun modeBestStreak(mode: GameMode): Int =
        progressRepository.modeBestStreak(mode, if (mode == gameMode) streak else 0)

    private fun resetModeProgress(mode: GameMode) = progressRepository.resetModeProgress(mode)

    private fun highestLevelKey(mode: GameMode): String = progressRepository.highestLevelKey(mode)

    private fun fairHighestLevelKey(mode: GameMode): String = progressRepository.fairHighestLevelKey(mode)

    private fun bestModeStreakKey(mode: GameMode): String = progressRepository.bestModeStreakKey(mode)

    private fun fairBestStreakKey(mode: GameMode): String = progressRepository.fairBestStreakKey(mode)

    private fun levelAdKey(mode: GameMode): String = progressRepository.levelAdKey(mode)

    private fun screenToWorld(x: Float, y: Float): Point2 = Point2((x - stageLeft) / scale, y / scale)

    private fun Point2.clampedToStage(): Point2 = Point2(
        x = x.coerceIn(0.12f, STAGE_WIDTH - 0.12f),
        y = y.coerceIn(0.12f, stageHeight - 0.12f)
    )

    private fun sx(x: Float): Float = stageLeft + x * scale

    private fun sy(y: Float): Float = y * scale

    private fun worldToScreen(value: Float): Float = value * scale

    private fun updateUiDensity() {
        val deviceDensity = resources.displayMetrics.density
        if (!isLargeScreenLayout()) {
            uiDensity = deviceDensity
            return
        }
        val tabletScale = if (viewWidth <= viewHeight) {
            (viewWidth / 600f).coerceIn(1f, 1.9f)
        } else {
            (viewHeight / 744f).coerceIn(1f, 1.55f)
        }
        uiDensity = max(deviceDensity, tabletScale)
    }

    private fun dp(value: Float): Float = value * uiDensity

    private fun t(value: String): String = KavvoroI18n.t(context, value)

    private fun localizedLevelTitle(title: String): String = t(title.uppercase()).uppercase()

    private fun rankValue(rank: String): Int = when (rank) {
        "S" -> 0
        "A" -> 1
        "B" -> 2
        "C" -> 3
        else -> 9
    }

    private companion object {
        const val TARGET_STAGE_HEIGHT = 17.78f
        const val TUTORIAL_LAST_LEVEL = 10
        const val AD_LEVEL_INTERVAL = 6
    }
}
