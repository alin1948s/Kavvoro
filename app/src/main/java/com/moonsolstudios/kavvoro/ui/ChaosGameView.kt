package com.moonsolstudios.kavvoro.ui

import android.content.Context
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewConfiguration
import android.util.Log
import androidx.core.content.FileProvider
import com.moonsolstudios.kavvoro.BuildConfig
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.audio.KavvoroSoundEngine
import com.moonsolstudios.kavvoro.audio.MusicTrack
import com.moonsolstudios.kavvoro.audio.SoundEvent
import com.moonsolstudios.kavvoro.billing.PremiumCatalog
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.Hazard
import com.moonsolstudios.kavvoro.engine.HazardMotion
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsEngine
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.PhysicsOutcome
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.PortalPair
import com.moonsolstudios.kavvoro.engine.PulseZone
import com.moonsolstudios.kavvoro.engine.ReplayRecorder
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.engine.STAGE_WIDTH
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.i18n.TutorialCopy
import com.moonsolstudios.kavvoro.share.ReplaySharePayload
import com.moonsolstudios.kavvoro.share.ReplayVideoExporter
import java.io.File
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class ChaosGameView(
    context: Context,
    private val adBridge: AdBridge = AdBridge.NONE,
    private val leaderboardBridge: LeaderboardBridge = LeaderboardBridge.NONE,
    private val privacyBridge: PrivacyBridge = PrivacyBridge.NONE,
    private val purchaseBridge: PurchaseBridge = PurchaseBridge.NONE
) : SurfaceView(context), SurfaceHolder.Callback {
    private val lock = Any()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val scratch = RectF()
    private val scratch2 = RectF()
    private val scratch3 = RectF()
    private val physics = PhysicsEngine()
    private val replay = ReplayRecorder()
    private val audio = KavvoroSoundEngine(context.applicationContext)
    private val prefs = context.getSharedPreferences("kavvoro_progress", Context.MODE_PRIVATE)
    private var sfxMuted = prefs.getBoolean(SFX_MUTED_KEY, false)
    private var musicMuted = prefs.getBoolean(MUSIC_MUTED_KEY, false)
    private val ballSkins = listOf(
        BallSkin("prism_king", "VORO PRIME", "unlimited aura final boss", 0xFFF7F4FF.toInt(), 0xFF8AA6FF.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.PRISM, UnlockRule(UnlockType.PREMIUM, 0, "Premium 0.99 local price"), BallPower.PRISM_SHIELD),
        BallSkin("void_zero", "KAV ZERO", "taxed the void at 3am", 0xFF07090F.toInt(), 0xFFFF4D8D.toInt(), 0xFF45F2FF.toInt(), SkinStyle.VOID, UnlockRule(UnlockType.PREMIUM, 0, "Premium 0.99 local price"), BallPower.VOID_PHASE),
        BallSkin("chrome_lux", "CHROME VORO", "sigma mirror from ohio", 0xFFE6EDF7.toInt(), 0xFF1DE8C8.toInt(), 0xFFFF4D8D.toInt(), SkinStyle.CHROME, UnlockRule(UnlockType.PREMIUM, 0, "Premium 0.99 local price"), BallPower.CHROME_RICOCHET),
        BallSkin("plasma_crown", "NOVA KAV", "galaxy rizz meltdown", 0xFFFFCF4A.toInt(), 0xFFC15CFF.toInt(), 0xFFFF8C42.toInt(), SkinStyle.PLASMA, UnlockRule(UnlockType.PREMIUM, 0, "Premium 0.99 local price"), BallPower.PLASMA_SURGE),
        BallSkin("nodlo", "KAVVORO", "the original brainrot specimen", 0xFFF7F4FF.toInt(), 0xFF1DE8C8.toInt(), 0xFF1DE8C8.toInt(), SkinStyle.CLASSIC, UnlockRule(UnlockType.DEFAULT, 0, "Unlocked")),
        BallSkin("curse_grad", "VORO GRAD", "graduated brain academy", 0xFF8AA6FF.toInt(), 0xFFFFCF4A.toInt(), 0xFF8AA6FF.toInt(), SkinStyle.CROWN, UnlockRule(UnlockType.TUTORIAL_CLEAR, 10, "Clear tutorial L10")),
        BallSkin("blop_13", "BLOP VORO", "emotionally aerodynamic", 0xFF64E572.toInt(), 0xFF07090F.toInt(), 0xFF64E572.toInt(), SkinStyle.BLOP, UnlockRule(UnlockType.CLASSIC_LEVEL, 25, "Clear Classic L25")),
        BallSkin("fizz_nana", "FIZZ KAV", "battery ate the charger", 0xFFFFCF4A.toInt(), 0xFF07090F.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.ZAP, UnlockRule(UnlockType.BEST_STREAK, 15, "Reach streak 15")),
        BallSkin("lala_glitch", "LALA VORO", "wifi password was wrong", 0xFFFF4D8D.toInt(), 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), SkinStyle.GLITCH, UnlockRule(UnlockType.CHAOS_LEVEL, 25, "Clear Chaos L25")),
        BallSkin("womp_loop", "WOMP KAV", "looping the same thought", 0xFF8AA6FF.toInt(), 0xFFFFCF4A.toInt(), 0xFF8AA6FF.toInt(), SkinStyle.LOOP, UnlockRule(UnlockType.BEST_STREAK, 25, "Reach streak 25")),
        BallSkin("mimi_static", "MIMI VORO", "staring aggressively in 4k", 0xFFC15CFF.toInt(), 0xFFF7F4FF.toInt(), 0xFFC15CFF.toInt(), SkinStyle.STATIC, UnlockRule(UnlockType.CLASSIC_LEVEL, 40, "Clear Classic L40")),
        BallSkin("zaza_volt", "ZAZA KAV", "too fast for homework", 0xFFFF8C42.toInt(), 0xFF07090F.toInt(), 0xFFFF8C42.toInt(), SkinStyle.ZAP, UnlockRule(UnlockType.BEST_STREAK, 35, "Reach streak 35")),
        BallSkin("tik_rift", "TIKKAV RIFT", "the wind owes him money", 0xFF45F2FF.toInt(), 0xFF431934.toInt(), 0xFF45F2FF.toInt(), SkinStyle.RIFT, UnlockRule(UnlockType.CLASSIC_LEVEL, 55, "Clear Classic L55")),
        BallSkin("byte_baba", "BYTE VORO", "born in the share button", 0xFFF7F4FF.toInt(), 0xFFFF4D8D.toInt(), 0xFFF7F4FF.toInt(), SkinStyle.BYTE, UnlockRule(UnlockType.SHARE_COUNT, 5, "Share five replays")),
        BallSkin("globo_wobble", "GLOBO KAV", "gravity's unemployed cousin", 0xFF1DE8C8.toInt(), 0xFFFFCF4A.toInt(), 0xFF1DE8C8.toInt(), SkinStyle.WOBBLE, UnlockRule(UnlockType.CHAOS_LEVEL, 30, "Clear Chaos L30")),
        BallSkin("rift_baba", "ELDER VORO", "ancient forbidden yapping", 0xFF431934.toInt(), 0xFFFFCF4A.toInt(), 0xFFFF4D8D.toInt(), SkinStyle.RIFT, UnlockRule(UnlockType.CHAOS_LEVEL, 50, "Clear Chaos L50")),
        BallSkin("king_static", "KING KAV", "maximum aura no evidence", 0xFFFFCF4A.toInt(), 0xFFFF4D8D.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.CROWN, UnlockRule(UnlockType.BEST_STREAK, 25, "Reach streak 25"))
    ) + lateGameBallSkins()

    private fun lateGameBallSkins(): List<BallSkin> = listOf(
        BallSkin("nibbi_kav", "NIBBI KAV", "forgot why it entered", 0xFF7EE787.toInt(), 0xFF1F2937.toInt(), 0xFF7EE787.toInt(), SkinStyle.BLOP, UnlockRule(UnlockType.CLASSIC_LEVEL, 35, "Clear Classic L35")),
        BallSkin("voro_rizz", "VORO RIZZ", "unlicensed aura dealer", 0xFFFF7AB2.toInt(), 0xFF231942.toInt(), 0xFFFF7AB2.toInt(), SkinStyle.LOOP, UnlockRule(UnlockType.CHAOS_LEVEL, 40, "Clear Chaos L40")),
        BallSkin("bongo_kav", "BONGO KAV", "thinks in drum noises", 0xFFFFA94D.toInt(), 0xFF101820.toInt(), 0xFFFFA94D.toInt(), SkinStyle.WOBBLE, UnlockRule(UnlockType.HYPE_COST, 7_500, "Unlock with 7500 HYPE")),
        BallSkin("glitch_nona", "GLITCH NONA", "buffering since birth", 0xFFB765FF.toInt(), 0xFF45F2FF.toInt(), 0xFFB765FF.toInt(), SkinStyle.GLITCH, UnlockRule(UnlockType.CLASSIC_LEVEL, 50, "Clear Classic L50")),
        BallSkin("sloppi_voro", "SLOPPI VORO", "microwaved premium water", 0xFF65E6A7.toInt(), 0xFF342A21.toInt(), 0xFF65E6A7.toInt(), SkinStyle.BLOP, UnlockRule(UnlockType.HYPE_COST, 14_000, "Unlock with 14000 HYPE")),
        BallSkin("kav_kaboom", "KAV KABOOM", "volume permanently maximum", 0xFFFF5757.toInt(), 0xFFFFCF4A.toInt(), 0xFFFF5757.toInt(), SkinStyle.ZAP, UnlockRule(UnlockType.SHARE_COUNT, 10, "Share ten replays")),
        BallSkin("drippi_mim", "DRIPPI MIM", "wearing invisible sneakers", 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), 0xFF45F2FF.toInt(), SkinStyle.CHROME, UnlockRule(UnlockType.HYPE_COST, 22_000, "Unlock with 22000 HYPE")),
        BallSkin("nappa_voro", "NAPPA VORO", "sleeping at full speed", 0xFF8AA6FF.toInt(), 0xFF2B2440.toInt(), 0xFF8AA6FF.toInt(), SkinStyle.STATIC, UnlockRule(UnlockType.CLASSIC_LEVEL, 75, "Clear Classic L75")),
        BallSkin("yappa_kav", "YAPPA KAV", "podcast with no microphone", 0xFFFF8C42.toInt(), 0xFFC15CFF.toInt(), 0xFFFF8C42.toInt(), SkinStyle.LOOP, UnlockRule(UnlockType.CHAOS_LEVEL, 75, "Clear Chaos L75")),
        BallSkin("turbo_blob", "TURBO BLOB", "zero brakes many opinions", 0xFF64E572.toInt(), 0xFFFFCF4A.toInt(), 0xFF64E572.toInt(), SkinStyle.WOBBLE, UnlockRule(UnlockType.BEST_STREAK, 50, "Reach streak 50")),
        BallSkin("wifi_voro", "WIFI VORO", "connected without internet", 0xFF45F2FF.toInt(), 0xFF07090F.toInt(), 0xFF45F2FF.toInt(), SkinStyle.RIFT, UnlockRule(UnlockType.CLASSIC_LEVEL, 100, "Clear Classic L100")),
        BallSkin("cringe_kav", "CRINGE KAV", "weaponized secondhand shame", 0xFFFF4D8D.toInt(), 0xFFFFCF4A.toInt(), 0xFFFF4D8D.toInt(), SkinStyle.GLITCH, UnlockRule(UnlockType.CHAOS_LEVEL, 100, "Clear Chaos L100")),
        BallSkin("kav_404", "KAV 404", "thought process not found", 0xFFF7F4FF.toInt(), 0xFFFF5757.toInt(), 0xFFF7F4FF.toInt(), SkinStyle.BYTE, UnlockRule(UnlockType.SHARE_COUNT, 20, "Share twenty replays")),
        BallSkin("pasta_voro", "PASTA VORO", "certified sauce technician", 0xFFFFCF4A.toInt(), 0xFFFF4D8D.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.LOOP, UnlockRule(UnlockType.HYPE_COST, 34_000, "Unlock with 34000 HYPE")),
        BallSkin("laggi_kav", "LAGGI KAV", "arrived three frames late", 0xFFC15CFF.toInt(), 0xFF45F2FF.toInt(), 0xFFC15CFF.toInt(), SkinStyle.STATIC, UnlockRule(UnlockType.CLASSIC_LEVEL, 125, "Clear Classic L125")),
        BallSkin("moggo_voro", "MOGGO VORO", "jawline rendered separately", 0xFF1DE8C8.toInt(), 0xFF431934.toInt(), 0xFF1DE8C8.toInt(), SkinStyle.PRISM, UnlockRule(UnlockType.CHAOS_LEVEL, 125, "Clear Chaos L125")),
        BallSkin("brain_bean", "BRAIN BEAN", "one bean two braincells", 0xFF64E572.toInt(), 0xFFC15CFF.toInt(), 0xFF64E572.toInt(), SkinStyle.BLOP, UnlockRule(UnlockType.HYPE_COST, 55_000, "Unlock with 55000 HYPE"), BallPower.MINOR_RICOCHET),
        BallSkin("aura_thief", "AURA THIEF", "borrowed everything forever", 0xFF07090F.toInt(), 0xFFFFCF4A.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.VOID, UnlockRule(UnlockType.CLASSIC_LEVEL, 150, "Clear Classic L150")),
        BallSkin("gigi_glitch", "GIGI GLITCH", "reality needs an update", 0xFFFF4D8D.toInt(), 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), SkinStyle.GLITCH, UnlockRule(UnlockType.CHAOS_LEVEL, 150, "Clear Chaos L150")),
        BallSkin("noodle_kav", "NOODLE KAV", "structurally just vibes", 0xFFFFCF4A.toInt(), 0xFF64E572.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.WOBBLE, UnlockRule(UnlockType.SHARE_COUNT, 30, "Share thirty replays")),
        BallSkin("sleepy_voro", "SLEEPY VORO", "dreaming in leaderboard points", 0xFF8AA6FF.toInt(), 0xFF07090F.toInt(), 0xFF8AA6FF.toInt(), SkinStyle.STATIC, UnlockRule(UnlockType.HYPE_COST, 85_000, "Unlock with 85000 HYPE"), BallPower.MINOR_PHASE),
        BallSkin("panic_bean", "PANIC BEAN", "calm mode sold separately", 0xFFFF8C42.toInt(), 0xFFFF4D8D.toInt(), 0xFFFF8C42.toInt(), SkinStyle.ZAP, UnlockRule(UnlockType.CLASSIC_LEVEL, 200, "Clear Classic L200")),
        BallSkin("bossy_blop", "BOSSY BLOP", "promoted itself during lunch", 0xFF64E572.toInt(), 0xFFFFCF4A.toInt(), 0xFF64E572.toInt(), SkinStyle.CROWN, UnlockRule(UnlockType.CHAOS_LEVEL, 200, "Clear Chaos L200")),
        BallSkin("quantum_kav", "QUANTUM KAV", "winning and crashing together", 0xFF45F2FF.toInt(), 0xFFC15CFF.toInt(), 0xFF45F2FF.toInt(), SkinStyle.PRISM, UnlockRule(UnlockType.HYPE_COST, 130_000, "Unlock with 130000 HYPE"), BallPower.MINOR_SURGE),
        BallSkin("wobble_ceo", "WOBBLE CEO", "owns three fake companies", 0xFFFFCF4A.toInt(), 0xFF1DE8C8.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.WOBBLE, UnlockRule(UnlockType.CLASSIC_LEVEL, 250, "Clear Classic L250")),
        BallSkin("error_voro", "ERROR VORO", "too forbidden to compile", 0xFFFF5757.toInt(), 0xFF07090F.toInt(), 0xFFFF5757.toInt(), SkinStyle.BYTE, UnlockRule(UnlockType.CHAOS_LEVEL, 250, "Clear Chaos L250")),
        BallSkin("golden_yap", "GOLDEN YAP", "talking in legendary subtitles", 0xFFFFCF4A.toInt(), 0xFFF7F4FF.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.CROWN, UnlockRule(UnlockType.SHARE_COUNT, 50, "Share fifty replays")),
        BallSkin("void_junior", "VOID JUNIOR", "small void huge confidence", 0xFF18111F.toInt(), 0xFFFF4D8D.toInt(), 0xFFC15CFF.toInt(), SkinStyle.VOID, UnlockRule(UnlockType.BEST_STREAK, 150, "Reach streak 150")),
        BallSkin("kav_maxx", "KAV MAXX", "every slider set to illegal", 0xFFF7F4FF.toInt(), 0xFFFF5757.toInt(), 0xFFFFCF4A.toInt(), SkinStyle.PLASMA, UnlockRule(UnlockType.CLASSIC_LEVEL, 300, "Clear Classic L300")),
        BallSkin("rift_rizzler", "RIFT RIZZLER", "gravity follows his account", 0xFF431934.toInt(), 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), SkinStyle.RIFT, UnlockRule(UnlockType.CHAOS_LEVEL, 300, "Clear Chaos L300")),
        BallSkin("ultra_nona", "ULTRA NONA", "recharges from pure yapping", 0xFFC15CFF.toInt(), 0xFFFFCF4A.toInt(), 0xFFC15CFF.toInt(), SkinStyle.PLASMA, UnlockRule(UnlockType.BEST_STREAK, 200, "Reach streak 200"), BallPower.MINOR_SURGE),
        BallSkin("aura_titan", "AURA TITAN", "phases through awkward moments", 0xFFF7F4FF.toInt(), 0xFF8AA6FF.toInt(), 0xFF45F2FF.toInt(), SkinStyle.PRISM, UnlockRule(UnlockType.CLASSIC_LEVEL, 400, "Clear Classic L400"), BallPower.MINOR_PHASE),
        BallSkin("final_voro", "FINAL VORO", "bounces beyond the lore", 0xFFFFCF4A.toInt(), 0xFFFF4D8D.toInt(), 0xFFF7F4FF.toInt(), SkinStyle.CROWN, UnlockRule(UnlockType.CHAOS_LEVEL, 400, "Clear Chaos L400"), BallPower.MINOR_RICOCHET)
    )

    private val brainballArtResources = mapOf(
        "prism_king" to R.drawable.brainball_prism_king,
        "void_zero" to R.drawable.brainball_void_zero,
        "chrome_lux" to R.drawable.brainball_chrome_lux,
        "plasma_crown" to R.drawable.brainball_plasma_crown,
        "nodlo" to R.drawable.brainball_nodlo,
        "curse_grad" to R.drawable.brainball_curse_grad,
        "blop_13" to R.drawable.brainball_blop_13,
        "fizz_nana" to R.drawable.brainball_fizz_nana,
        "lala_glitch" to R.drawable.brainball_lala_glitch,
        "womp_loop" to R.drawable.brainball_womp_loop,
        "mimi_static" to R.drawable.brainball_mimi_static,
        "zaza_volt" to R.drawable.brainball_zaza_volt,
        "tik_rift" to R.drawable.brainball_tik_rift,
        "byte_baba" to R.drawable.brainball_byte_baba,
        "globo_wobble" to R.drawable.brainball_globo_wobble,
        "rift_baba" to R.drawable.brainball_rift_baba,
        "king_static" to R.drawable.brainball_king_static,
        "nibbi_kav" to R.drawable.brainball_nibbi_kav,
        "voro_rizz" to R.drawable.brainball_voro_rizz,
        "bongo_kav" to R.drawable.brainball_bongo_kav,
        "glitch_nona" to R.drawable.brainball_glitch_nona,
        "sloppi_voro" to R.drawable.brainball_sloppi_voro,
        "kav_kaboom" to R.drawable.brainball_kav_kaboom,
        "drippi_mim" to R.drawable.brainball_drippi_mim,
        "nappa_voro" to R.drawable.brainball_nappa_voro,
        "yappa_kav" to R.drawable.brainball_yappa_kav,
        "turbo_blob" to R.drawable.brainball_turbo_blob,
        "wifi_voro" to R.drawable.brainball_wifi_voro,
        "cringe_kav" to R.drawable.brainball_cringe_kav,
        "kav_404" to R.drawable.brainball_kav_404,
        "pasta_voro" to R.drawable.brainball_pasta_voro,
        "laggi_kav" to R.drawable.brainball_laggi_kav,
        "moggo_voro" to R.drawable.brainball_moggo_voro,
        "brain_bean" to R.drawable.brainball_brain_bean,
        "aura_thief" to R.drawable.brainball_aura_thief,
        "gigi_glitch" to R.drawable.brainball_gigi_glitch,
        "noodle_kav" to R.drawable.brainball_noodle_kav,
        "sleepy_voro" to R.drawable.brainball_sleepy_voro,
        "panic_bean" to R.drawable.brainball_panic_bean,
        "bossy_blop" to R.drawable.brainball_bossy_blop,
        "quantum_kav" to R.drawable.brainball_quantum_kav,
        "wobble_ceo" to R.drawable.brainball_wobble_ceo,
        "error_voro" to R.drawable.brainball_error_voro,
        "golden_yap" to R.drawable.brainball_golden_yap,
        "void_junior" to R.drawable.brainball_void_junior,
        "kav_maxx" to R.drawable.brainball_kav_maxx,
        "rift_rizzler" to R.drawable.brainball_rift_rizzler,
        "ultra_nona" to R.drawable.brainball_ultra_nona,
        "aura_titan" to R.drawable.brainball_aura_titan,
        "final_voro" to R.drawable.brainball_final_voro
    )
    private val brainballBitmaps = mutableMapOf<String, Bitmap>()
    private val worldArtResources = mapOf(
        "bg_menu" to R.drawable.world_bg_menu,
        "bg_tutorial" to R.drawable.world_bg_tutorial,
        "bg_tutorial_classic" to R.drawable.world_bg_tutorial_classic,
        "bg_tutorial_chaos" to R.drawable.world_bg_tutorial_chaos,
        "bg_classic" to R.drawable.world_bg_classic,
        "bg_chaos" to R.drawable.world_bg_chaos,
        "bg_endgame" to R.drawable.world_bg_endgame,
        "boost_rift_pull" to R.drawable.boost_rift_pull,
        "boost_pulse" to R.drawable.boost_pulse,
        "boost_prism" to R.drawable.boost_prism,
        "boost_void" to R.drawable.boost_void,
        "boost_rebound" to R.drawable.boost_rebound,
        "boost_plasma" to R.drawable.boost_plasma,
        "boost_chain" to R.drawable.boost_chain,
        "boost_recharge" to R.drawable.boost_recharge,
        "boost_goal" to R.drawable.boost_goal,
        "hazard_static" to R.drawable.world_hazard_static,
        "hazard_glitch" to R.drawable.world_hazard_glitch,
        "hazard_void" to R.drawable.world_hazard_void,
        "reactor_out" to R.drawable.world_reactor_out,
        "reactor_in" to R.drawable.world_reactor_in,
        "portal_goal" to R.drawable.world_portal_goal,
        "platform_classic" to R.drawable.world_platform_classic,
        "platform_chaos" to R.drawable.world_platform_chaos,
        "danger_beacon" to R.drawable.world_danger_beacon,
        "ui_home" to R.drawable.ui_icon_home,
        "ui_retry" to R.drawable.ui_icon_retry,
        "ui_share" to R.drawable.ui_icon_share,
        "ui_next" to R.drawable.ui_icon_next,
        "ui_back" to R.drawable.ui_icon_back,
        "ui_restore" to R.drawable.ui_icon_restore,
        "ui_sound" to R.drawable.ui_icon_sound,
        "ui_music" to R.drawable.ui_icon_music
    )
    private val worldBitmaps = mutableMapOf<String, Bitmap>()
    private val scaledBackgroundBitmaps = mutableMapOf<String, Bitmap>()

    @Volatile
    private var running = false
    private var loopThread: Thread? = null
    private var lastFrameNanos = 0L
    private val renderProfile = detectRenderProfile()
    private var adaptiveQuality = initialAdaptiveQuality()
    private var consecutiveSlowFrames = 0

    private var viewWidth = 1
    private var viewHeight = 1
    private var uiDensity = resources.displayMetrics.density
    private var scale = 1f
    private var stageLeft = 0f
    private var stageHeight = 17.78f
    private var levelIndex = 1
    private var level: LevelSpec = LevelDirector.create(levelIndex, stageHeight)

    private var screen = Screen.MENU
    private var menuState = MenuState.MODES
    private var selectedMenuMode = GameMode.CLASSIC
    private var gameMode = GameMode.CLASSIC
    private var menuPulse = 0f
    private var menuBallOffsetX = 0f
    private var menuBallOffsetY = 0f
    private var menuBallVelocityX = 0f
    private var menuBallVelocityY = 0f
    private var menuBallDragging = false
    private var menuBallTouchDx = 0f
    private var menuBallTouchDy = 0f
    private var menuBallLastDragTime = 0L
    private var menuPreviewCenterX = 0f
    private var menuPreviewCenterY = 0f
    private var menuPreviewRadius = 0f
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
    private val tutorialInputGate = TutorialInputGate()
    private val tutorialCardBounds = RectF()
    private val tutorialTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var tutorialDownX = 0f
    private var tutorialDownY = 0f
    private var tutorialMovedBeyondSlop = false
    private var tutorialCardVisible = false
    private var activeMenuButton = MenuButton.NONE
    private var backgroundShader: LinearGradient? = null
    private var pendingAdAction = AdAction.NONE
    private var pendingResumeMode = GameMode.CLASSIC
    private var adReason = ""
    private var adLoading = false
    private var exportingShare = false
    private var selectedSkinId = prefs.getString(SELECTED_SKIN_KEY, DEFAULT_SKIN_ID) ?: DEFAULT_SKIN_ID
    private var collectionFocusSkinId = selectedSkinId
    private val premiumPricesBySkin = mutableMapOf<String, String>()
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
    private var activeCollectionIndex = -1
    private var leaderboardMessage = ""
    private var leaderboardMessageTimer = 0f
    private var activeLeaderboardIndex = -1

    private val menuStartButton = RectF()
    private val menuActionStartButton = RectF()
    private val menuChaosButton = RectF()
    private val menuContinueButton = RectF()
    private val menuBackButton = RectF()
    private val menuCollectionButton = RectF()
    private val menuLeaderboardButton = RectF()
    private val menuPrivacyButton = RectF()
    private val menuLanguageButton = RectF()
    private val menuSfxButton = RectF()
    private val menuMusicButton = RectF()
    private val menuPreviewBounds = RectF()
    private val collectionBackButton = RectF()
    private val collectionRestoreButton = RectF()
    private val collectionFilterRects = MutableList(CollectionFilter.entries.size) { RectF() }
    private val collectionItemRects = mutableListOf<RectF>()
    private val leaderboardBackButton = RectF()
    private val leaderboardItemRects = mutableListOf<RectF>()
    private val languageBackButton = RectF()
    private val languageItemRects = MutableList(KavvoroLanguage.entries.size) { RectF() }
    private var activeLanguageIndex = -1
    private var languageScroll = 0f
    private var languageMaxScroll = 0f
    private var languageTouchY = 0f
    private var languageLastY = 0f
    private var languageDragging = false
    private val homeButton = RectF()
    private val restartButton = RectF()
    private val sfxButton = RectF()
    private val musicButton = RectF()
    private val shareButton = RectF()
    private val nextButton = RectF()
    private val resultNextButton = RectF()
    private val resultRetryButton = RectF()
    private val resultShareButton = RectF()
    private val adButton = RectF()
    private val tutorialStartButton = RectF()

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
        keepScreenOn = true
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        audio.setSfxMuted(sfxMuted)
        audio.setMusicMuted(musicMuted)
        audio.setLanguageCode(KavvoroI18n.audioLanguageCode(context))
        syncMusicTrack()
        configureStage(width.coerceAtLeast(1), height.coerceAtLeast(1), reset = false)
    }

    fun resumeGame() {
        audio.setPaused(false)
        syncMusicTrack()
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

    fun updatePremiumPrices(pricesByProductId: Map<String, String>) {
        post {
            synchronized(lock) {
                val editor = prefs.edit()
                pricesByProductId.forEach { (productId, price) ->
                    val skinId = PremiumCatalog.productToSkinId[productId] ?: return@forEach
                    premiumPricesBySkin[skinId] = price
                    editor.putString(premiumPriceKey(skinId), price)
                }
                editor.apply()
            }
        }
    }

    fun syncPremiumEntitlements(ownedProductIds: Set<String>) {
        post {
            synchronized(lock) {
                val ownedSkinIds = ownedProductIds.mapNotNull(PremiumCatalog.productToSkinId::get).toSet()
                val editor = prefs.edit()
                PremiumCatalog.skinToProductId.keys.forEach { skinId ->
                    editor.putBoolean(purchasedSkinKey(skinId), skinId in ownedSkinIds)
                }
                editor.apply()
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
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pauseGame()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var pendingAction: (() -> Unit)? = null
        synchronized(lock) {
            if (screen == Screen.MENU) {
                handleMenuTouch(event)
            } else if (screen == Screen.COLLECTION) {
                handleCollectionTouch(event)
            } else if (screen == Screen.LEADERBOARDS) {
                handleLeaderboardTouch(event)
            } else if (screen == Screen.LANGUAGE) {
                handleLanguageTouch(event)
            } else if (screen == Screen.AD) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        activeButton = buttonAt(event.x, event.y)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val releasedButton = activeButton
                        activeButton = ButtonId.NONE
                        if (releasedButton != ButtonId.NONE && buttonAt(event.x, event.y) == releasedButton) {
                            pendingAction = handleButton(releasedButton)
                        }
                    }
                }
            } else {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        activeButton = buttonAt(event.x, event.y)
                        if (activeButton == ButtonId.NONE &&
                            !handleTutorialTouch(event)
                        ) {
                            startRiftControl(event.x, event.y)
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (activeButton == ButtonId.NONE &&
                            !handleTutorialTouch(event) &&
                            riftTapReleaseTimer <= 0f
                        ) {
                            moveRiftControl(event.x, event.y)
                        }
                    }

                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_POINTER_UP -> {
                        handleTutorialTouch(event)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val releasedButton = activeButton
                        activeButton = ButtonId.NONE
                        if (releasedButton != ButtonId.NONE && buttonAt(event.x, event.y) == releasedButton) {
                            pendingAction = handleButton(releasedButton)
                        } else if (!handleTutorialTouch(event) &&
                            (event.actionMasked == MotionEvent.ACTION_CANCEL ||
                                riftTapReleaseTimer <= 0f)
                        ) {
                            releaseRiftControl()
                        }
                    }
                }
            }
        }
        pendingAction?.invoke()
        return true
    }

    private fun handleTutorialTouch(event: MotionEvent): Boolean {
        if (!tutorialCardVisible) return false
        val action = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tutorialDownX = event.x
                tutorialDownY = event.y
                tutorialMovedBeyondSlop = false
                TutorialPointerAction.DOWN
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - tutorialDownX
                val dy = event.y - tutorialDownY
                if (dx * dx + dy * dy > tutorialTouchSlop * tutorialTouchSlop) {
                    tutorialMovedBeyondSlop = true
                }
                TutorialPointerAction.MOVE
            }

            MotionEvent.ACTION_UP -> TutorialPointerAction.UP
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_UP -> TutorialPointerAction.MULTI_TOUCH
            MotionEvent.ACTION_CANCEL -> TutorialPointerAction.CANCEL
            else -> return true
        }
        val result = tutorialInputGate.onPointer(
            action = action,
            target = tutorialTouchTarget(event.x, event.y),
            movedBeyondTapSlop = tutorialMovedBeyondSlop
        )

        when (result.outcome) {
            TutorialGateOutcome.NONE -> Unit
            TutorialGateOutcome.DISMISS_ONLY -> dismissTutorialCard()
            TutorialGateOutcome.DISMISS_AND_PLAY -> {
                if (dismissTutorialCard()) startRiftControl(event.x, event.y)
            }
        }

        if (action == TutorialPointerAction.UP ||
            action == TutorialPointerAction.CANCEL ||
            action == TutorialPointerAction.MULTI_TOUCH
        ) {
            resetTutorialGesture()
        }
        return result.consumed
    }

    private fun tutorialTouchTarget(x: Float, y: Float): TutorialTouchTarget = when {
        tutorialCardBounds.isEmpty -> TutorialTouchTarget.CARD
        tutorialStartButton.contains(x, y) -> TutorialTouchTarget.ACTION_BUTTON
        tutorialCardBounds.contains(x, y) -> TutorialTouchTarget.CARD
        else -> TutorialTouchTarget.PLAYFIELD
    }

    private fun resetTutorialGesture() {
        tutorialInputGate.reset()
        tutorialDownX = 0f
        tutorialDownY = 0f
        tutorialMovedBeyondSlop = false
    }

    private fun dismissTutorialCard(): Boolean {
        if (!prefs.edit()
                .putBoolean(tutorialAcknowledgementKey(), true)
                .commit()
        ) {
            return false
        }
        tutorialCardVisible = false
        tutorialCardBounds.setEmpty()
        tutorialStartButton.setEmpty()
        resetTutorialGesture()
        stateElapsed = 0f
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
        return true
    }

    private fun handleMenuTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutMenuButtons()
                updateMenuPreviewGeometry()
                val pressedButton = menuButtonAt(event.x, event.y)
                if (pressedButton != MenuButton.NONE) {
                    activeMenuButton = pressedButton
                } else {
                    activeMenuButton = MenuButton.NONE
                    menuBallDragging = true
                    menuBallVelocityX = 0f
                    menuBallVelocityY = 0f
                    val ballX = menuPreviewCenterX + menuBallOffsetX
                    val ballY = menuPreviewCenterY + menuBallOffsetY
                    if (menuPreviewBallHit(event.x, event.y)) {
                        menuBallTouchDx = event.x - ballX
                        menuBallTouchDy = event.y - ballY
                    } else {
                        menuBallTouchDx = 0f
                        menuBallTouchDy = 0f
                    }
                    menuBallLastDragTime = event.eventTime
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (menuBallDragging) {
                    dragMenuBall(event)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (menuBallDragging) {
                    dragMenuBall(event)
                    menuBallDragging = false
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    return
                }
                val releasedButton = activeMenuButton
                activeMenuButton = MenuButton.NONE
                if (releasedButton != MenuButton.NONE && menuButtonAt(event.x, event.y) == releasedButton) {
                    handleMenuButton(releasedButton)
                }
            }
        }
    }

    private fun dragMenuBall(event: MotionEvent) {
        val previousX = menuBallOffsetX
        val previousY = menuBallOffsetY
        val targetOffsetX = event.x - menuBallTouchDx - menuPreviewCenterX
        val targetOffsetY = event.y - menuBallTouchDy - menuPreviewCenterY
        val clamped = clampMenuBallOffset(targetOffsetX, targetOffsetY)
        menuBallOffsetX = clamped.x
        menuBallOffsetY = clamped.y

        val elapsedMs = (event.eventTime - menuBallLastDragTime).coerceAtLeast(1L)
        val dt = (elapsedMs / 1000f).coerceIn(0.008f, 0.08f)
        menuBallVelocityX = ((menuBallOffsetX - previousX) / dt).coerceIn(-2400f, 2400f)
        menuBallVelocityY = ((menuBallOffsetY - previousY) / dt).coerceIn(-2400f, 2400f)
        menuBallLastDragTime = event.eventTime
    }

    private fun updateMenuBallPhysics(dt: Float) {
        if (menuBallDragging) return
        val spring = if (selectedMenuMode == GameMode.CHAOS) 8.8f else 6.8f
        val damping = if (selectedMenuMode == GameMode.CHAOS) 0.8f else 0.84f
        menuBallVelocityX += -menuBallOffsetX * spring * dt
        menuBallVelocityY += -menuBallOffsetY * spring * dt
        menuBallVelocityX *= (1f - dt * (1f - damping) * 18f).coerceIn(0.42f, 1f)
        menuBallVelocityY *= (1f - dt * (1f - damping) * 18f).coerceIn(0.42f, 1f)
        val next = clampMenuBallOffset(
            menuBallOffsetX + menuBallVelocityX * dt,
            menuBallOffsetY + menuBallVelocityY * dt
        )
        if (next.x != menuBallOffsetX + menuBallVelocityX * dt) menuBallVelocityX *= -0.35f
        if (next.y != menuBallOffsetY + menuBallVelocityY * dt) menuBallVelocityY *= -0.35f
        menuBallOffsetX = next.x
        menuBallOffsetY = next.y
        if (kotlin.math.abs(menuBallOffsetX) < 0.4f && kotlin.math.abs(menuBallVelocityX) < 4f) {
            menuBallOffsetX = 0f
            menuBallVelocityX = 0f
        }
        if (kotlin.math.abs(menuBallOffsetY) < 0.4f && kotlin.math.abs(menuBallVelocityY) < 4f) {
            menuBallOffsetY = 0f
            menuBallVelocityY = 0f
        }
    }

    private fun clampMenuBallOffset(offsetX: Float, offsetY: Float): Point2 {
        val safeRadius = dp(42f)
        val minX = safeRadius
        val maxX = viewWidth - safeRadius
        val minY = dp(58f)
        val maxY = viewHeight - dp(58f)
        val targetX = menuPreviewCenterX + offsetX
        val targetY = menuPreviewCenterY + offsetY
        return Point2(
            targetX.coerceIn(minX, maxX) - menuPreviewCenterX,
            targetY.coerceIn(minY, maxY) - menuPreviewCenterY
        )
    }

    private fun menuPreviewBallHit(x: Float, y: Float): Boolean {
        val ballX = menuPreviewCenterX + menuBallOffsetX
        val ballY = menuPreviewCenterY + menuBallOffsetY
        val radius = dp(60f)
        val dx = x - ballX
        val dy = y - ballY
        return dx * dx + dy * dy <= radius * radius
    }

    private fun gameLoop() {
        while (running) {
            val now = System.nanoTime()
            val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 1f / 24f)
            lastFrameNanos = now

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
                    }
                }
            }

            val frameTime = (System.nanoTime() - now) / 1_000_000
            adjustAdaptiveQuality(frameTime.toFloat())
            val sleepMs = (targetFrameMillis() - frameTime).coerceAtLeast(2L)
            SystemClock.sleep(sleepMs)
        }
    }

    private fun lockRenderCanvas(): Canvas? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                holder.lockHardwareCanvas()
            } catch (_: IllegalArgumentException) {
                holder.lockCanvas()
            } catch (_: IllegalStateException) {
                holder.lockCanvas()
            } catch (_: UnsupportedOperationException) {
                holder.lockCanvas()
            }
        } else {
            holder.lockCanvas()
        }
    }

    private fun adjustAdaptiveQuality(frameTimeMs: Float) {
        val target = targetFrameMillis().toFloat()
        consecutiveSlowFrames = if (frameTimeMs > target * 1.18f) consecutiveSlowFrames + 1 else 0
        val floor = qualityFloor()
        val ceiling = qualityCeiling()
        adaptiveQuality = when {
            frameTimeMs > target * 1.8f -> adaptiveQuality - 0.16f
            frameTimeMs > target * 1.32f -> adaptiveQuality - 0.085f
            consecutiveSlowFrames >= 3 -> adaptiveQuality - 0.05f
            frameTimeMs < target * 0.58f -> adaptiveQuality + if (renderProfile == RenderProfile.LOW) 0.004f else 0.012f
            frameTimeMs < target * 0.78f -> adaptiveQuality + if (renderProfile == RenderProfile.HIGH) 0.006f else 0.002f
            else -> adaptiveQuality
        }.coerceIn(floor, ceiling)
    }

    private fun targetFrameMillis(): Long {
        return when {
            exportingShare -> 33L
            screenTransitionTimer > 0f -> if (renderProfile == RenderProfile.LOW) 42L else 22L
            screen == Screen.GAME && state == GameState.SIMULATING -> when (renderProfile) {
                RenderProfile.LOW -> 33L
                RenderProfile.BALANCED -> 18L
                RenderProfile.HIGH -> 16L
            }
            screen == Screen.GAME -> if (renderProfile == RenderProfile.LOW) 42L else 22L
            screen == Screen.MENU -> if (renderProfile == RenderProfile.LOW) 50L else 33L
            else -> if (renderProfile == RenderProfile.LOW) 50L else 33L
        }
    }

    private fun initialAdaptiveQuality(): Float = when (renderProfile) {
        RenderProfile.LOW -> 0.38f
        RenderProfile.BALANCED -> 0.72f
        RenderProfile.HIGH -> 0.88f
    }

    private fun qualityFloor(): Float = when (renderProfile) {
        RenderProfile.LOW -> 0.3f
        RenderProfile.BALANCED -> 0.5f
        RenderProfile.HIGH -> 0.56f
    }

    private fun qualityCeiling(): Float = when (renderProfile) {
        RenderProfile.LOW -> 0.5f
        RenderProfile.BALANCED -> 0.84f
        RenderProfile.HIGH -> 1f
    }

    private fun performanceLite(): Boolean = renderProfile == RenderProfile.LOW || adaptiveQuality < 0.64f

    private fun richEffects(): Boolean = renderProfile != RenderProfile.LOW && adaptiveQuality >= 0.8f

    private fun fullEffects(): Boolean = renderProfile == RenderProfile.HIGH && adaptiveQuality >= 0.94f

    private fun syncMusicTrack() {
        val track = when (screen) {
            Screen.MENU,
            Screen.COLLECTION,
            Screen.LEADERBOARDS,
            Screen.LANGUAGE -> MusicTrack.MENU
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

    private fun shouldShowLevelAd(mode: GameMode, levelNumber: Int): Boolean {
        if (isTutorialLevel(levelNumber)) return false
        val firstAdLevel = TUTORIAL_LAST_LEVEL + AD_LEVEL_INTERVAL + 1
        if (levelNumber < firstAdLevel) return false
        if ((levelNumber - firstAdLevel) % AD_LEVEL_INTERVAL != 0) return false
        return prefs.getInt(levelAdKey(mode), 0) != levelNumber
    }

    private fun markLevelAdShown(mode: GameMode, levelNumber: Int) {
        prefs.edit().putInt(levelAdKey(mode), levelNumber).apply()
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
        backgroundShader = null
        layoutButtons()
        layoutMenuButtons()
        if (reset) resetRound()
    }

    private fun update(dt: Float) {
        syncMusicTrack()
        stateElapsed += dt
        menuPulse += dt
        flash = max(0f, flash - dt * 1.9f)
        finishPulse = max(0f, finishPulse - dt * 1.7f)
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

        if (screen == Screen.MENU || screen == Screen.COLLECTION || screen == Screen.LEADERBOARDS) {
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
                }
                if (frame.powerTriggered) {
                    powerMessage = t("PRISM SHIELD SAID NOT TODAY").uppercase()
                    powerMessageTimer = 2.4f
                    audio.playEvent(SoundEvent.POWER, selectedBallIndex())
                    performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                }
                if (frame.portalTriggered) {
                    powerMessage = t("PORTAL SLINGSHOT").uppercase()
                    powerMessageTimer = 1.6f
                    pulseFeedbackCooldown = 0.35f
                    audio.playEvent(SoundEvent.POWER, selectedBallIndex(), 1f)
                    hapticSequence(HapticFeedbackConstants.CONFIRM to 0L, HapticFeedbackConstants.CLOCK_TICK to 95L)
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
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
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
                performHapticFeedback(HapticFeedbackConstants.REJECT)
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
            lastRiftBreak = shouldTriggerRiftBreak(score)
            lastRiftBreakBonus = if (lastRiftBreak) calculateRiftBreakBonus(score) else 0
            lastRiftBreakReason = if (lastRiftBreak) riftBreakReason(score) else ""
            lastDailyBonus = claimDailyRiftBonus()
            lastStreakMilestoneBonus = calculateStreakMilestoneBonus()
            lastHypeScore = calculateHypeScore(score) + lastRiftBreakBonus + lastDailyBonus + lastStreakMilestoneBonus
            if (lastRiftBreak) {
                riftBreakTimer = 2.15f
                powerMessage = "${t("RIFT BREAK").uppercase()}  +$lastRiftBreakBonus"
                powerMessageTimer = 2.35f
            }
            saveBest(score)
            val unlockedAfter = unlockedSkinIds()
            val newSkin = ballSkins.firstOrNull { it.id in (unlockedAfter - unlockedBefore) }
            rewardMessage = finishRewardLine(newSkin)
            clearFailContinueCount()
            audio.playEvent(if (newSkin != null) SoundEvent.UNLOCK else if (lastRiftBreak) SoundEvent.POWER else SoundEvent.GOAL, selectedBallIndex())
            if (newSkin != null) {
                hapticSequence(
                    HapticFeedbackConstants.CONFIRM to 0L,
                    HapticFeedbackConstants.LONG_PRESS to 90L,
                    HapticFeedbackConstants.CLOCK_TICK to 180L
                )
            } else {
                hapticSequence(
                    HapticFeedbackConstants.CONFIRM to 0L,
                    HapticFeedbackConstants.CLOCK_TICK to 110L
                )
            }
        } else {
            lastScore = null
            audio.playEvent(SoundEvent.FAIL, selectedBallIndex())
            hapticSequence(
                HapticFeedbackConstants.REJECT to 0L,
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
        backgroundShader = null
        resetRound()
        triggerScreenTransition(level.accent)
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
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
        val current = prefs.getString(bestKey(score.level), null)
        if (current == null || rankValue(score.rank) < rankValue(current)) {
            prefs.edit().putString(bestKey(score.level), score.rank).apply()
        }
        val nextLevel = max(modeProgress(gameMode), score.level + 1)
        val nextBestStreak = max(modeBestStreak(gameMode), streak)
        val newHypeBalance = (hypeBalance().toLong() + lastHypeScore.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        prefs.edit()
            .putInt(progressKey(gameMode), nextLevel)
            .putInt(streakKey(gameMode), streak)
            .putInt(highestLevelKey(gameMode), max(modeHighestLevel(gameMode), nextLevel))
            .putInt(bestModeStreakKey(gameMode), nextBestStreak)
            .putInt(BEST_STREAK_KEY, max(bestStreak(), streak))
            .putInt("clear_streak", streak)
            .putInt("last_hype", lastHypeScore)
            .putInt(HYPE_BANK_KEY, newHypeBalance)
            .apply()
        val levelBoard = if (gameMode == GameMode.CLASSIC) LeaderboardBoard.CLASSIC_LEVEL else LeaderboardBoard.CHAOS_LEVEL
        val streakBoard = if (gameMode == GameMode.CLASSIC) LeaderboardBoard.CLASSIC_STREAK else LeaderboardBoard.CHAOS_STREAK
        if (selectedBallSkin().power == BallPower.NONE) {
            val fairLevel = max(prefs.getInt(fairHighestLevelKey(gameMode), modeHighestLevel(gameMode)), nextLevel)
            val fairStreak = max(prefs.getInt(fairBestStreakKey(gameMode), modeBestStreak(gameMode)), nextBestStreak)
            prefs.edit()
                .putInt(fairHighestLevelKey(gameMode), fairLevel)
                .putInt(fairBestStreakKey(gameMode), fairStreak)
                .apply()
            leaderboardBridge.submitScore(levelBoard, fairLevel.toLong())
            leaderboardBridge.submitScore(streakBoard, fairStreak.toLong())
        }
    }

    private fun calculateHypeScore(score: RunScore): Int {
        val rankBonus = when (score.rank) {
            "S" -> 900
            "A" -> 650
            "B" -> 420
            else -> 250
        }
        val modeBonus = when (gameMode) {
            GameMode.CHAOS -> 360
            GameMode.CLASSIC -> 120
        }
        val speedBonus = ((9f - score.seconds).coerceAtLeast(0f) * 72f).roundToInt()
        val inkBonus = ((1f - (score.inkUsed / level.inkLimit).coerceIn(0f, 1f)) * 520f).roundToInt()
        return rankBonus + modeBonus + speedBonus + inkBonus + streak * 80 + maxChain * 120
    }

    private fun currentHudHypeScore(): Int {
        if (state == GameState.WON && lastHypeScore > 0) return lastHypeScore
        if (state == GameState.LOST) return 0
        val modeBonus = if (gameMode == GameMode.CHAOS) 360 else 120
        val levelBonus = (level.index * if (gameMode == GameMode.CHAOS) 16 else 10).coerceAtMost(520)
        val paceRatio = ((level.timeLimitSeconds - simElapsed).coerceAtLeast(0f) / level.timeLimitSeconds.coerceAtLeast(1f))
            .coerceIn(0f, 1f)
        val paceBonus = (paceRatio * 360f).roundToInt()
        val energyBonus = (riftEnergy.coerceIn(0f, 1f) * 420f).roundToInt()
        val chainBonus = (maxChain * 120 + chainCount * 34).coerceAtMost(920)
        return 220 + modeBonus + levelBonus + paceBonus + energyBonus + streak * 80 + chainBonus
    }

    private fun shouldTriggerRiftBreak(score: RunScore): Boolean {
        val lowEnergyFinish = riftEnergy <= 0.24f
        val comboSpike = maxChain >= if (gameMode == GameMode.CHAOS) 3 else 4
        val clutchTimer = score.seconds >= level.timeLimitSeconds * 0.72f && riftEnergy <= 0.36f
        val cleanHighRank = (score.rank == "S" || score.rank == "A") && maxChain >= 2 && riftEnergy <= 0.42f
        return lowEnergyFinish || comboSpike || clutchTimer || cleanHighRank
    }

    private fun calculateRiftBreakBonus(score: RunScore): Int {
        val rankBonus = if (score.rank == "S") 220 else if (score.rank == "A") 140 else 80
        val energyBonus = ((1f - riftEnergy.coerceIn(0f, 1f)) * 420f).roundToInt()
        val chainBonus = (maxChain * 65).coerceAtMost(520)
        val modeBonus = if (gameMode == GameMode.CHAOS) 180 else 90
        return 360 + rankBonus + energyBonus + chainBonus + modeBonus
    }

    private fun riftBreakReason(score: RunScore): String {
        return when {
            riftEnergy <= 0.18f -> t("LOW ENERGY FINISH").uppercase()
            maxChain >= 5 -> t("CHAIN SPIKE").uppercase()
            score.seconds >= level.timeLimitSeconds * 0.8f -> t("LAST SECOND CLUTCH").uppercase()
            gameMode == GameMode.CHAOS -> t("CHAOS CONTROL").uppercase()
            else -> t("CLEAN RIFT SNAP").uppercase()
        }
    }

    private fun calculateStreakMilestoneBonus(): Int {
        if (streak <= 0 || streak % 5 != 0) return 0
        return 250 + (streak * 18).coerceAtMost(900)
    }

    private fun toggleSfxMuted() {
        sfxMuted = !sfxMuted
        prefs.edit().putBoolean(SFX_MUTED_KEY, sfxMuted).apply()
        audio.setSfxMuted(sfxMuted)
        if (!sfxMuted) {
            audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
        }
    }

    private fun toggleMusicMuted() {
        musicMuted = !musicMuted
        prefs.edit().putBoolean(MUSIC_MUTED_KEY, musicMuted).apply()
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
            MenuButton.COLLECTION -> {
                screen = Screen.COLLECTION
                collectionMessage = ""
                collectionMessageTimer = 0f
                collectionFocusSkinId = selectedBallSkin().id
                activeMenuButton = MenuButton.NONE
                backgroundShader = null
                triggerScreenTransition(selectedBallSkin().lineColor)
            }

            MenuButton.LEADERBOARDS -> {
                screen = Screen.LEADERBOARDS
                leaderboardMessage = ""
                leaderboardMessageTimer = 0f
                activeLeaderboardIndex = -1
                backgroundShader = null
                triggerScreenTransition(0xFF8AA6FF.toInt())
                syncLeaderboards()
            }

            MenuButton.PRIVACY -> privacyBridge.showPrivacyOptions()
            MenuButton.LANGUAGE -> {
                screen = Screen.LANGUAGE
                activeMenuButton = MenuButton.NONE
                activeLanguageIndex = -1
                backgroundShader = null
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

            MenuButton.NONE -> Unit
        }
    }

    private fun playSelectedMode() {
        val mode = selectedMenuMode
        if (modeProgress(mode) <= 1) {
            startRun(mode, continueProgress = false)
        } else {
            continueRunFromMenu(mode)
        }
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
                "CONTINUE ${mode.menuTitle()} L${resumeLevel.toString().padStart(2, '0')}"
            )
        } else {
            startRun(mode, continueProgress = true)
        }
    }

    private fun selectHomeMode(mode: GameMode) {
        val changed = selectedMenuMode != mode
        if (selectedMenuMode == mode && modeProgress(mode) > 1) {
            menuState = MenuState.MODE_ACTION
        } else {
            selectedMenuMode = mode
            menuState = MenuState.MODES
        }
        backgroundShader = null
        if (changed) {
            val accent = if (mode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
            menuBallVelocityX += if (mode == GameMode.CHAOS) -420f else 420f
            menuBallVelocityY += if (mode == GameMode.CHAOS) 160f else -120f
            triggerScreenTransition(accent)
        }
    }

    private fun selectMode(mode: GameMode) {
        selectedMenuMode = mode
        if (modeProgress(mode) <= 1) {
            startRun(mode, continueProgress = false)
        } else {
            menuState = MenuState.MODE_ACTION
        }
    }

    private fun exitToMenu() {
        if (state == GameState.WON || state == GameState.LOST) {
            breakStreak()
        }
        screen = Screen.MENU
        menuState = MenuState.MODES
        backgroundShader = null
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
            powerMessage = "${gameMode.menuTitle()} ARENA UPGRADED"
            powerMessageTimer = 2.4f
            hapticSequence(HapticFeedbackConstants.CONFIRM to 0L, HapticFeedbackConstants.CLOCK_TICK to 120L)
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
        backgroundShader = null
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

    private fun continueRequiresAd(): Boolean {
        return shouldShowLevelAd(gameMode, level.index) || shouldShowFailContinueAd()
    }

    private fun continueAdReason(): String {
        return if (shouldShowLevelAd(gameMode, level.index)) {
            "${t("LEVEL").uppercase()} ${level.index} ${t("CHECKPOINT").uppercase()}"
        } else {
            "${t("FAILED").uppercase()} ${FAILS_BEFORE_CONTINUE_AD}X"
        }
    }

    private fun recordFreeContinue() {
        val used = failContinueCountForCurrentLevel()
        prefs.edit().putInt(failContinueCountKey(gameMode, level.index), used + 1).apply()
    }

    private fun recordAdContinue() {
        clearFailContinueCount()
    }

    private fun shouldShowFailContinueAd(): Boolean {
        return failContinueCountForCurrentLevel() + 1 >= FAILS_BEFORE_CONTINUE_AD
    }

    private fun failContinueCountForCurrentLevel(): Int {
        return prefs.getInt(failContinueCountKey(gameMode, level.index), 0)
    }

    private fun clearFailContinueCount() {
        prefs.edit().putInt(failContinueCountKey(gameMode, level.index), 0).apply()
    }

    private fun breakStreak() {
        if (streak == 0) return
        streak = 0
        prefs.edit()
            .putInt(streakKey(gameMode), 0)
            .putInt("clear_streak", 0)
            .apply()
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

    private fun drawGame(canvas: Canvas) {
        if (screen == Screen.AD) {
            drawBackground(canvas)
            drawAdPlaceholder(canvas)
            drawScreenTransition(canvas)
            return
        }
        if (screen == Screen.MENU) {
            drawMenu(canvas)
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
        if (screen == Screen.LANGUAGE) {
            drawLanguageSelector(canvas)
            drawScreenTransition(canvas)
            return
        }
        drawBackground(canvas)
        drawCurseAtmosphere(canvas)
        drawPulseZones(canvas)
        drawPortals(canvas)
        drawBlocks(canvas)
        drawGoal(canvas)
        drawHazards(canvas)
        drawRouteCoach(canvas)
        drawReplayTail(canvas)
        drawRiftTrail(canvas)
        drawDrawingAssist(canvas)
        drawBall(canvas)
        drawHud(canvas)
        drawLevelNameGlass(canvas)
        drawMissionBrief(canvas)
        drawPowerToast(canvas)
        drawTutorialHint(canvas)
        drawFinishBurst(canvas)
        drawRiftBreakMoment(canvas)
        drawOutcome(canvas)
        if (flash > 0f) drawFlash(canvas)
        if (exportingShare) drawExportingOverlay(canvas)
        drawScreenTransition(canvas)
    }

    private fun drawScreenTransition(canvas: Canvas) {
        if (screenTransitionTimer <= 0f) return
        val progress = (screenTransitionTimer / 0.34f).coerceIn(0f, 1f)
        val ease = progress * progress * (3f - 2f * progress)
        val cx = viewWidth * 0.5f
        val cy = viewHeight * 0.48f
        val maxRadius = max(viewWidth, viewHeight) * (0.58f + (1f - ease) * 0.34f)

        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (150f * ease).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val transitionRings = if (performanceLite()) 1 else 3
        repeat(transitionRings) { ring ->
            paint.strokeWidth = dp(3.4f - ring * 0.7f)
            paint.color = withAlpha(
                if (ring == 1) selectedBallSkin().secondary else screenTransitionAccent,
                (205f * ease * (1f - ring * 0.22f)).roundToInt()
            )
            canvas.drawCircle(cx, cy, maxRadius * (0.22f + ring * 0.12f), paint)
        }

        repeat(if (performanceLite()) 2 else 7) { i ->
            val y = cy - dp(118f) + i * dp(36f) + sin(menuPulse * 4.2f + i) * dp(6f)
            val offset = (1f - ease) * viewWidth * 0.34f
            paint.strokeWidth = dp(if (i % 2 == 0) 2.4f else 1.2f)
            paint.color = withAlpha(if (i % 2 == 0) screenTransitionAccent else 0xFFFFCF4A.toInt(), (145f * ease).roundToInt())
            canvas.drawLine(dp(18f) + offset, y, viewWidth - dp(18f) - offset * 0.45f, y + dp(if (i % 2 == 0) 9f else -7f), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        val portal = worldBitmap("portal_goal")
        if (portal != null && !performanceLite()) {
            val size = dp(86f + 30f * (1f - ease))
            scratch.set(cx - size * 0.5f, cy - size * 0.5f, cx + size * 0.5f, cy + size * 0.5f)
            paint.alpha = (230f * ease).roundToInt().coerceIn(0, 230)
            canvas.save()
            canvas.rotate((1f - ease) * 42f, cx, cy)
            canvas.drawBitmap(portal, null, scratch, paint)
            canvas.restore()
            paint.alpha = 255
        }

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(12f)
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (220f * ease).roundToInt())
        canvas.drawText("KAVVORO RIFT", cx, cy + dp(72f), textPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        val chaosTheme = ((screen == Screen.GAME || screen == Screen.AD) && gameMode == GameMode.CHAOS) ||
            (screen == Screen.MENU && selectedMenuMode == GameMode.CHAOS)
        paint.shader = null
        paint.alpha = 255
        paint.style = Paint.Style.FILL
        val backgroundKey = when {
            screen == Screen.MENU -> if (selectedMenuMode == GameMode.CHAOS) "bg_chaos" else "bg_classic"
            screen != Screen.GAME && screen != Screen.AD -> "bg_menu"
            level.index <= 10 -> if (gameMode == GameMode.CHAOS) "bg_tutorial_chaos" else "bg_tutorial_classic"
            level.index >= 150 -> "bg_endgame"
            chaosTheme -> "bg_chaos"
            else -> "bg_classic"
        }
        val background = backgroundBitmap(backgroundKey)
        if (background != null) {
            paint.isFilterBitmap = false
            canvas.drawBitmap(background, 0f, 0f, paint)
        } else {
            paint.color = if (chaosTheme) 0xFF160A17.toInt() else 0xFF07121A.toInt()
            canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
        }

        if (performanceLite()) {
            paint.shader = null
            paint.color = if (screen == Screen.GAME) 0x5E070A10 else 0x8A070A10.toInt()
            canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
        } else {
            paint.shader = LinearGradient(
                0f,
                0f,
                0f,
                viewHeight.toFloat(),
                intArrayOf(
                    0xB8070A10.toInt(),
                    if (screen == Screen.GAME) 0x4A070A10 else 0x76070A10,
                    if (screen == Screen.GAME) 0x66070A10 else 0xA6070A10.toInt()
                ),
                floatArrayOf(0f, 0.42f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
            paint.shader = null
        }

        if (stageLeft > dp(2f)) {
            val stageRight = stageLeft + STAGE_WIDTH * scale
            paint.style = Paint.Style.FILL
            paint.color = 0x8A07090F.toInt()
            canvas.drawRect(0f, 0f, stageLeft, viewHeight.toFloat(), paint)
            canvas.drawRect(stageRight, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
            paint.color = withAlpha(if (chaosTheme) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt(), 80)
            canvas.drawRect(stageLeft - dp(1f), 0f, stageLeft, viewHeight.toFloat(), paint)
            canvas.drawRect(stageRight, 0f, stageRight + dp(1f), viewHeight.toFloat(), paint)
        }

        val gridStep = worldToScreen(
            when {
                performanceLite() -> 2.2f
                screen == Screen.GAME && !richEffects() -> 1.6f
                else -> 1f
            }
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1f, dp(0.7f))
        paint.color = if (performanceLite()) 0x08FFFFFF else if (screen == Screen.GAME) 0x0EFFFFFF else 0x08FFFFFF
        var x = stageLeft
        while (x <= viewWidth) {
            canvas.drawLine(x, 0f, x, viewHeight.toFloat(), paint)
            x += gridStep
        }
        var y = 0f
        while (y <= viewHeight) {
            canvas.drawLine(0f, y, viewWidth.toFloat(), y, paint)
            y += gridStep
        }

        if (chaosTheme && !performanceLite()) {
            paint.strokeWidth = dp(2.2f)
            repeat(if (richEffects()) 5 else 3) { i ->
                val offset = -viewHeight * 0.35f + i * viewHeight * 0.24f + sin(stateElapsed * 0.7f + i) * dp(16f)
                paint.color = if (i % 2 == 0) 0x20FF4D8D else 0x18FFCF4A
                canvas.drawLine(0f, offset, viewWidth.toFloat(), offset + viewWidth * 0.72f, paint)
            }
        }

        paint.style = Paint.Style.FILL
        repeat(
            when {
                performanceLite() -> 0
                richEffects() -> 12
                else -> 6
            }
        ) { i ->
            val px = ((i * 137) % 1000) / 1000f * viewWidth
            val py = ((i * 251 + 91) % 1000) / 1000f * viewHeight
            paint.color = if (chaosTheme) {
                if (i % 2 == 0) 0x24FF4D8D else 0x20FFCF4A
            } else {
                if (i % 3 == 0) 0x18FFCF4A else 0x181DE8C8
            }
            canvas.drawCircle(px, py, dp(1f + (i % 3)), paint)
        }
    }

    private fun drawCurseAtmosphere(canvas: Canvas) {
        if (level.curses.isEmpty()) return
        val rich = richEffects()

        if (levelHasCurse(CurseType.RIFT_WIND)) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2.2f)
            paint.strokeCap = Paint.Cap.ROUND
            repeat(if (rich) 8 else 4) { i ->
                val direction = if (sin(stateElapsed * 1.45f + i) >= 0f) 1f else -1f
                val y = dp(122f) + i * viewHeight * 0.095f + sin(stateElapsed * 2f + i) * dp(7f)
                val x = ((stateElapsed * 68f * direction + i * 83f) % (viewWidth + dp(90f))) - dp(45f)
                val startX = if (direction > 0f) x else viewWidth - x
                paint.color = if (i % 2 == 0) 0x668AA6FF else 0x5545F2FF
                canvas.drawLine(startX, y, startX + direction * dp(44f), y + sin(i.toFloat()) * dp(5f), paint)
                canvas.drawLine(startX + direction * dp(44f), y + sin(i.toFloat()) * dp(5f), startX + direction * dp(31f), y - dp(8f), paint)
                canvas.drawLine(startX + direction * dp(44f), y + sin(i.toFloat()) * dp(5f), startX + direction * dp(31f), y + dp(8f), paint)
            }
            paint.strokeCap = Paint.Cap.BUTT
        }

        if (levelHasCurse(CurseType.RIFT_DRAIN)) {
            paint.style = Paint.Style.FILL
            repeat(if (rich) 10 else 5) { i ->
                val x = ((i * 97) % 1000) / 1000f * viewWidth
                val y = ((stateElapsed * 72f + i * 53f) % viewHeight)
                paint.color = if (i % 2 == 0) 0x4464E572 else 0x331DE8C8
                canvas.drawRoundRect(x, y, x + dp(3f), y + dp(12f + i % 4), dp(2f), dp(2f), paint)
            }
        }

        if (levelHasCurse(CurseType.OVERHEAT) && state == GameState.SIMULATING) {
            paint.style = Paint.Style.FILL
            paint.color = 0x44FF5757
            canvas.drawRect(0f, dp(104f), viewWidth * riftEnergy, dp(108f), paint)
        }
    }

    private fun drawMenu(canvas: Canvas) {
        drawBackground(canvas)
        layoutMenuButtons()
        drawMenuPreview(canvas)

        val left = menuContentLeft()
        val top = dp(54f)
        val contentWidth = menuContentWidth()
        drawBrandTitle(canvas, left, top)
        drawMenuHeaderStats(canvas, left, top)
        drawMenuRewardStrip(canvas, left, top + dp(42f), contentWidth)

        if (menuState == MenuState.MODES) {
            drawDailyStreakHub(canvas, left, top + dp(100f), contentWidth)
            drawHomeModeChip(canvas, menuContinueButton, GameMode.CLASSIC, 0xFF1DE8C8.toInt())
            drawHomeModeChip(canvas, menuChaosButton, GameMode.CHAOS, 0xFFFF4D8D.toInt())
            drawPrimaryPlayButton(canvas, menuStartButton)
            drawMenuButton(
                canvas = canvas,
                rect = menuLeaderboardButton,
                button = MenuButton.LEADERBOARDS,
                title = t("LEADERBOARDS").uppercase(),
                meta = t("GLOBAL RANKS").uppercase(),
                accent = 0xFF8AA6FF.toInt()
            )
            drawMenuButton(
                canvas = canvas,
                rect = menuCollectionButton,
                button = MenuButton.COLLECTION,
                title = t("COLLECTION").uppercase(),
                meta = "${unlockedSkinCount()}/${ballSkins.size} ${t("UNLOCKED COUNT").uppercase()}",
                accent = selectedBallSkin().lineColor
            )
        } else {
            val mode = selectedMenuMode
            val accent = if (mode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
            drawModeOptionsHeader(canvas, mode, accent)
            drawMenuButton(
                canvas = canvas,
                rect = menuActionStartButton,
                button = MenuButton.START,
                title = t("START NEW").uppercase(),
                meta = t("RESET TO LEVEL 01").uppercase(),
                accent = 0xFFFFCF4A.toInt()
            )
            drawMenuButton(
                canvas = canvas,
                rect = menuChaosButton,
                button = MenuButton.CONTINUE,
                title = t("CONTINUE").uppercase(),
                meta = "${t("LEVEL").uppercase()} ${modeProgress(mode).toString().padStart(2, '0')}   ${t("STREAK").uppercase()} ${modeStreak(mode)}",
                accent = accent
            )
            drawMenuButton(
                canvas = canvas,
                rect = menuContinueButton,
                button = MenuButton.BACK,
                title = t("BACK").uppercase(),
                meta = t("CHOOSE MODE").uppercase(),
                accent = 0xFF8AA6FF.toInt()
            )
        }
        drawPrivacyMenuButton(canvas)
        drawLanguageMenuButton(canvas)
        drawMenuAudioButtons(canvas)
    }

    private fun drawPrivacyMenuButton(canvas: Canvas) {
        val active = activeMenuButton == MenuButton.PRIVACY
        val accent = 0xFF8AA6FF.toInt()
        drawUiButtonFrame(canvas, menuPrivacyButton, active, accent, cornerDp = 7f)
        val iconX = menuPrivacyButton.left + dp(22f)
        val iconY = menuPrivacyButton.centerY()

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, if (active) 120 else 72)
        canvas.drawCircle(iconX, iconY, dp(12f), paint)
        path.reset()
        path.moveTo(iconX, iconY - dp(9f))
        path.lineTo(iconX + dp(7f), iconY - dp(5f))
        path.lineTo(iconX + dp(6f), iconY + dp(4f))
        path.lineTo(iconX, iconY + dp(9f))
        path.lineTo(iconX - dp(6f), iconY + dp(4f))
        path.lineTo(iconX - dp(7f), iconY - dp(5f))
        path.close()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.6f)
        paint.color = 0xDDF7F4FF.toInt()
        canvas.drawPath(path, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = dp(10f)
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText(t("PRIVACY").uppercase(), menuPrivacyButton.left + dp(43f), menuPrivacyButton.centerY() + dp(3.5f), textPaint)
    }

    private fun drawMenuAudioButtons(canvas: Canvas) {
        drawAudioToggleButton(
            canvas = canvas,
            rect = menuSfxButton,
            active = activeMenuButton == MenuButton.SFX,
            muted = sfxMuted,
            music = false,
            accent = 0xFF45F2FF.toInt()
        )
        drawAudioToggleButton(
            canvas = canvas,
            rect = menuMusicButton,
            active = activeMenuButton == MenuButton.MUSIC,
            muted = musicMuted,
            music = true,
            accent = 0xFFFFCF4A.toInt()
        )
    }

    private fun drawLanguageMenuButton(canvas: Canvas) {
        val active = activeMenuButton == MenuButton.LANGUAGE
        val accent = 0xFF45F2FF.toInt()
        drawUiButtonFrame(canvas, menuLanguageButton, active, accent, cornerDp = 7f)
        val lang = KavvoroI18n.selected(context)
        val code = if (lang == KavvoroLanguage.SYSTEM) KavvoroI18n.active(context).shortCode else lang.shortCode
        val languageName = if (lang == KavvoroLanguage.SYSTEM) "AUTO" else lang.nativeName
        val iconCx = menuLanguageButton.left + dp(22f)
        val iconCy = menuLanguageButton.centerY()

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, if (active) 120 else 72)
        canvas.drawCircle(iconCx, iconCy, dp(12f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = 0xDDF7F4FF.toInt()
        canvas.drawCircle(iconCx, iconCy, dp(8.5f), paint)
        canvas.drawLine(iconCx - dp(8f), iconCy, iconCx + dp(8f), iconCy, paint)
        canvas.drawLine(iconCx, iconCy - dp(8f), iconCx, iconCy + dp(8f), paint)
        scratch.set(iconCx - dp(5.8f), iconCy - dp(8.5f), iconCx + dp(5.8f), iconCy + dp(8.5f))
        canvas.drawOval(scratch, paint)

        val badgeRight = menuLanguageButton.right - dp(8f)
        val badgeWidth = dp(28f)
        scratch.set(
            badgeRight - badgeWidth,
            menuLanguageButton.centerY() - dp(11f),
            badgeRight,
            menuLanguageButton.centerY() + dp(11f)
        )
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, if (active) 120 else 72)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = withAlpha(accent, 220)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(7.4f)
        textPaint.color = 0x99FFFFFF.toInt()
        val textLeft = menuLanguageButton.left + dp(43f)
        val textRight = scratch.left - dp(7f)
        canvas.drawText("LANG", textLeft, menuLanguageButton.top + dp(15f), textPaint)
        textPaint.textSize = dp(9.4f)
        textPaint.color = 0xEAF7F4FF.toInt()
        canvas.drawText(fitText(languageName, textRight - textLeft), textLeft, menuLanguageButton.top + dp(34f), textPaint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = dp(9.5f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(code, scratch.centerX(), scratch.centerY() + dp(3.4f), textPaint)
    }


    private fun drawBrandTitle(canvas: Canvas, left: Float, top: Float) {
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(11f)
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText("BRAINROT CHAOS", left, top - dp(25f), textPaint)

        textPaint.textSize = dp(39f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText("KAVVORO", left, top + dp(9f), textPaint)

        paint.style = Paint.Style.FILL
        paint.color = selectedBallSkin().lineColor
        canvas.drawRoundRect(left, top + dp(18f), left + dp(78f), top + dp(22f), dp(2f), dp(2f), paint)
        paint.color = 0xFFFFCF4A.toInt()
        canvas.drawRoundRect(left + dp(84f), top + dp(18f), left + dp(122f), top + dp(22f), dp(2f), dp(2f), paint)
    }

    private fun drawMenuHeaderStats(canvas: Canvas, left: Float, top: Float) {
        val right = menuContentRight()
        val hype = hypeBalance()
        val best = bestStreak()
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("HYPE").uppercase(), right, top - dp(22f), textPaint)
        canvas.drawText(t("BEST STREAK").uppercase(), right - dp(68f), top - dp(22f), textPaint)
        textPaint.textSize = dp(16f)
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(formatHypeAmount(hype), right, top + dp(1f), textPaint)
        textPaint.color = selectedBallSkin().lineColor
        canvas.drawText("x$best", right - dp(68f), top + dp(1f), textPaint)
    }

    private fun drawMenuRewardStrip(canvas: Canvas, left: Float, top: Float, width: Float) {
        val height = dp(52f)
        val gap = dp(10f)
        val cardWidth = (width - gap) * 0.5f
        val rewardLeft = left + cardWidth + gap

        val rawReward = nextRewardText()
        val vaultMaxed = rawReward == null
        val rewardAccent = if (vaultMaxed) 0xFF45F2FF.toInt() else 0xFFFFCF4A.toInt()
        val skin = selectedBallSkin()
        val reward = rawReward
            ?.replace(" ${t("AT").uppercase()} ", " / ")
            ?: t("VAULT MAXED").uppercase()

        scratch2.set(left, top, left + cardWidth, top + height)
        drawHubCard(
            canvas = canvas,
            rect = scratch2,
            iconKey = null,
            title = t("EQUIPPED").uppercase(),
            value = skin.name,
            meta = equippedSkinMeta(skin).uppercase(),
            accent = skin.lineColor,
            progress = 1f,
            skin = skin,
            compact = true
        )

        scratch3.set(rewardLeft, top, left + width, top + height)
        drawHubCard(
            canvas = canvas,
            rect = scratch3,
            iconKey = if (vaultMaxed) "boost_chain" else "boost_goal",
            title = t("NEXT UNLOCK").uppercase(),
            value = reward,
            meta = if (vaultMaxed) t("ALL FREE REWARDS UNLOCKED").uppercase() else t("REWARD SIGNAL").uppercase(),
            accent = rewardAccent,
            progress = nextRewardInfo()?.progress ?: 1f,
            compact = true
        )
    }

    private fun equippedSkinMeta(skin: BallSkin): String {
        return when {
            skin.power != BallPower.NONE -> "${t("POWER").uppercase()} / ${ballPowerName(skin.power).uppercase()}"
            skin.unlock.type == UnlockType.PREMIUM -> t("MYTHIC BRAINBALL").uppercase()
            else -> t("SELECTED BRAINBALL").uppercase()
        }
    }

    private fun drawDailyStreakHub(canvas: Canvas, left: Float, top: Float, width: Float) {
        val gap = dp(10f)
        val cardHeight = dp(66f)
        val cardWidth = (width - gap) * 0.5f
        scratch2.set(left, top, left + cardWidth, top + cardHeight)
        scratch3.set(left + cardWidth + gap, top, left + width, top + cardHeight)
        val dailyClaimed = dailyRiftBonusClaimed()
        val dailyAmount = if (dailyClaimed) {
            dailyRiftClaimedAmount().takeIf { it > 0 } ?: dailyRiftBonusForMode(selectedMenuMode)
        } else {
            dailyRiftBonusForMode(selectedMenuMode)
        }
        val dailyMeta = if (dailyClaimed) {
            dailyRiftResetText()
        } else {
            "${t("NEXT CLEAR").uppercase()} / ${dailyRiftResetText()}"
        }
        drawHubCard(
            canvas = canvas,
            rect = scratch2,
            iconKey = "boost_recharge",
            title = t("DAILY RIFT").uppercase(),
            value = "+$dailyAmount ${t("HYPE").uppercase()}",
            meta = dailyMeta,
            accent = 0xFF45F2FF.toInt(),
            progress = if (dailyClaimed) 1f else dailyRiftDayProgress(),
            status = if (dailyClaimed) t("CLAIMED").uppercase() else t("READY").uppercase()
        )

        val nextStreak = nextStreakRewardInfo()
        val currentBest = bestStreak()
        val target = nextStreak?.target ?: max(currentBest, 1)
        drawHubCard(
            canvas = canvas,
            rect = scratch3,
            iconKey = "boost_chain",
            title = t("STREAK VAULT").uppercase(),
            value = "x${max(modeStreak(GameMode.CLASSIC), modeStreak(GameMode.CHAOS))}",
            meta = nextStreak?.let { "x${it.target}  ${it.name}" } ?: t("VAULT MAXED").uppercase(),
            accent = selectedBallSkin().lineColor,
            progress = (currentBest.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        )
    }

    private fun drawHubCard(
        canvas: Canvas,
        rect: RectF,
        iconKey: String?,
        title: String,
        value: String,
        meta: String,
        accent: Int,
        progress: Float,
        status: String? = null,
        skin: BallSkin? = null,
        compact: Boolean = false
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(withAlpha(accent, 58), 0xD70A1018.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.9f)
        paint.color = withAlpha(accent, 135)
        canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)

        val iconSize = dp(if (compact) 26f else 30f)
        val iconLeft = rect.left + dp(if (compact) 10f else 9f)
        val iconTop = rect.centerY() - iconSize * 0.5f
        if (skin != null) {
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, 80)
            canvas.drawCircle(iconLeft + iconSize * 0.5f, rect.centerY(), iconSize * 0.72f, paint)
            drawBallSkin(canvas, iconLeft + iconSize * 0.5f, rect.centerY(), iconSize * 0.44f, skin, animated = true, locked = false)
        } else if (iconKey != null) {
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (compact) 82 else 0)
            if (compact) canvas.drawCircle(iconLeft + iconSize * 0.5f, rect.centerY(), iconSize * 0.64f, paint)
            scratch.set(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
            drawWorldAsset(canvas, iconKey, scratch, 220)
        }

        val textLeft = rect.left + dp(if (compact) 44f else 46f)
        val statusRight = rect.right - dp(8f)
        val statusLeft = status?.let {
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            textPaint.textSize = dp(7f)
            val pillWidth = (textPaint.measureText(it) + dp(14f)).coerceIn(dp(42f), dp(66f))
            val pillLeft = statusRight - pillWidth
            scratch.set(pillLeft, rect.top + dp(7f), statusRight, rect.top + dp(22f))
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, 46)
            canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(0.7f)
            paint.color = withAlpha(accent, 135)
            canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = withAlpha(0xFFF7F4FF.toInt(), 230)
            canvas.drawText(fitText(it, pillWidth - dp(8f)), scratch.centerX(), scratch.centerY() + dp(2.5f), textPaint)
            pillLeft
        }
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(if (compact) 7.2f else 8.2f)
        textPaint.color = withAlpha(accent, 225)
        val titleMaxWidth = (statusLeft ?: rect.right) - textLeft - dp(8f)
        canvas.drawText(fitText(title, titleMaxWidth), textLeft, rect.top + dp(if (compact) 14f else 17f), textPaint)
        textPaint.textSize = dp(if (compact) 11.8f else 15f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, rect.right - textLeft - dp(8f)), textLeft, rect.top + dp(if (compact) 31f else 40f), textPaint)
        textPaint.textSize = dp(if (compact) 6.5f else 7.6f)
        textPaint.color = 0xAFFFFFFF.toInt()
        canvas.drawText(fitText(meta, rect.right - textLeft - dp(8f)), textLeft, rect.top + dp(if (compact) 44f else 56f), textPaint)

        val barLeft = rect.left + dp(9f)
        val barRight = rect.right - dp(9f)
        val barTop = rect.bottom - dp(4f)
        paint.style = Paint.Style.FILL
        paint.color = 0x22FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + dp(2f), dp(2f), dp(2f), paint)
        paint.color = accent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress, barTop + dp(2f), dp(2f), dp(2f), paint)
    }

    private fun drawMenuPreview(canvas: Canvas) {
        updateMenuPreviewGeometry()
        val cx = menuPreviewCenterX
        val cy = menuPreviewCenterY
        val radius = menuPreviewRadius
        val skin = selectedBallSkin()
        val floatX = sin(menuPulse * 1.3f) * dp(6f)
        val floatY = sin(menuPulse * 1.8f) * dp(5f)
        val ballX = cx + floatX + menuBallOffsetX
        val ballY = cy + floatY + menuBallOffsetY
        val modeAccent = if (selectedMenuMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
        val portalRadius = radius * (0.74f + sin(menuPulse * 2.2f) * 0.025f)
        scratch.set(cx - portalRadius, cy - portalRadius, cx + portalRadius, cy + portalRadius)
        drawWorldAsset(canvas, "portal_goal", scratch, 235)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(2.2f)
        paint.color = withAlpha(modeAccent, if (menuBallDragging) 210 else 120)
        canvas.drawLine(cx, cy, ballX, ballY, paint)
        paint.strokeWidth = dp(10f)
        paint.color = withAlpha(modeAccent, if (menuBallDragging) 34 else 18)
        canvas.drawLine(cx, cy, ballX, ballY, paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (selectedMenuMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else skin.lineColor, 125)
        if (richEffects()) {
            paint.maskFilter = BlurMaskFilter(dp(24f), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(ballX, ballY, dp(if (menuBallDragging) 47f else 42f), paint)
        paint.maskFilter = null
        drawBallSkin(canvas, ballX, ballY, dp(if (menuBallDragging) 37f else 34f), skin, animated = true, locked = false)

        val caption = "${t("RIFT ONLINE").uppercase()}  /  ${skin.name}"
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        val maxCaptionWidth = viewWidth - dp(48f)
        val captionWidth = min(textPaint.measureText(caption) + dp(24f), maxCaptionWidth)
        val captionX = ballX.coerceIn(dp(24f) + captionWidth * 0.5f, viewWidth - dp(24f) - captionWidth * 0.5f)
        val captionSafeBottom = if (menuState == MenuState.MODES) {
            min(menuContinueButton.top, menuChaosButton.top) - dp(8f)
        } else {
            menuActionStartButton.top - dp(8f)
        }
        val preferredCaptionY = if (ballY + dp(58f) <= captionSafeBottom) {
            ballY + dp(58f)
        } else {
            ballY - dp(56f)
        }
        val captionY = preferredCaptionY.coerceIn(menuPreviewBounds.top + dp(20f), captionSafeBottom)
        scratch.set(
            captionX - captionWidth * 0.5f,
            captionY - dp(16f),
            captionX + captionWidth * 0.5f,
            captionY + dp(8f)
        )
        paint.style = Paint.Style.FILL
        paint.color = 0xAA050911.toInt()
        canvas.drawRoundRect(scratch, dp(10f), dp(10f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = withAlpha(modeAccent, 120)
        canvas.drawRoundRect(scratch, dp(10f), dp(10f), paint)
        textPaint.color = 0xDEFFFFFF.toInt()
        canvas.drawText(fitText(caption, captionWidth - dp(18f)), captionX, captionY, textPaint)
    }

    private fun updateMenuPreviewGeometry() {
        val controlsTop = if (menuState == MenuState.MODES) menuContinueButton.top else menuActionStartButton.top
        val previewTop = if (menuState == MenuState.MODES) dp(262f) else dp(188f)
        val previewBottom = controlsTop - dp(22f)
        val radius = min(viewWidth * 0.2f, (previewBottom - previewTop) * 0.36f).coerceIn(dp(52f), dp(78f))
        menuPreviewCenterX = viewWidth * 0.5f
        menuPreviewCenterY = (previewTop + previewBottom) * 0.5f
        menuPreviewRadius = radius
        menuPreviewBounds.set(dp(10f), dp(76f), viewWidth - dp(10f), viewHeight - dp(78f))
    }

    private fun drawModeOptionsHeader(canvas: Canvas, mode: GameMode, accent: Int) {
        val left = dp(24f)
        val baseline = menuActionStartButton.top - dp(22f)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(10f)
        textPaint.color = withAlpha(accent, 220)
        canvas.drawText("${mode.menuTitle()} ${t("LOADOUT").uppercase()}", left, baseline - dp(20f), textPaint)
        textPaint.textSize = dp(22f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText("${t("LEVEL").uppercase()} ${modeProgress(mode).toString().padStart(2, '0')}", left, baseline + dp(3f), textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = dp(11f)
        textPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawText("${t("STREAK").uppercase()} ${modeStreak(mode)}", viewWidth - dp(24f), baseline + dp(2f), textPaint)
    }

    private fun drawMascotFace(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        accent: Int,
        name: String,
        glitch: Boolean
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFFF7F4FF.toInt()
        scratch.set(cx - radius, cy - radius * 0.86f, cx + radius, cy + radius * 0.9f)
        canvas.drawRoundRect(scratch, radius * 0.34f, radius * 0.34f, paint)

        paint.color = accent
        canvas.drawRoundRect(cx - radius * 0.48f, cy - radius * 1.18f, cx + radius * 0.48f, cy - radius * 0.84f, radius * 0.14f, radius * 0.14f, paint)
        paint.strokeWidth = radius * 0.1f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(cx - radius * 0.48f, cy - radius * 1.1f, cx - radius * 0.72f, cy - radius * 1.32f, paint)
        canvas.drawLine(cx + radius * 0.48f, cy - radius * 1.1f, cx + radius * 0.72f, cy - radius * 1.32f, paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        paint.color = 0xFF07090F.toInt()
        val eyeShift = if (glitch) sin(menuPulse * 3.2f) * radius * 0.08f else 0f
        canvas.drawCircle(cx - radius * 0.34f + eyeShift, cy - radius * 0.16f, radius * 0.15f, paint)
        scratch.set(cx + radius * 0.18f - eyeShift, cy - radius * 0.3f, cx + radius * 0.52f - eyeShift, cy + radius * 0.04f)
        canvas.drawRoundRect(scratch, radius * 0.07f, radius * 0.07f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.08f
        scratch.set(cx - radius * 0.38f, cy + radius * 0.1f, cx + radius * 0.38f, cy + radius * 0.46f)
        canvas.drawArc(scratch, 12f, 156f, false, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = (radius * 0.32f).coerceAtLeast(dp(7f))
        textPaint.color = accent
        canvas.drawText(name.take(10), cx, cy + radius * 1.32f, textPaint)
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
        val wave = if (animated) sin(menuPulse * 3f) else 0f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(skin.lineColor, if (locked) 55 else 105)
        val rich = richEffects()
        if (rich) {
            paint.maskFilter = BlurMaskFilter(radius * 0.42f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(cx, cy, radius * (1.55f + wave * 0.06f), paint)
        paint.maskFilter = null

        val skinIndex = ballSkins.indexOfFirst { it.id == skin.id }
        val effectTier = when {
            skin.unlock.type == UnlockType.PREMIUM || skinIndex >= ballSkins.size - 4 -> 3
            skinIndex >= 37 -> 2
            skinIndex >= 28 -> 1
            else -> 0
        }
        val renderTier = if (rich) effectTier else min(effectTier, 1)
        if (animated && !locked && renderTier > 0 && adaptiveQuality >= 0.62f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = radius * 0.045f
            repeat(renderTier) { ring ->
                paint.color = withAlpha(if (ring % 2 == 0) skin.lineColor else skin.secondary, 95 - ring * 18)
                val orbitRadius = radius * (1.08f + ring * 0.13f + wave * 0.018f)
                canvas.drawCircle(cx, cy, orbitRadius, paint)
            }
            paint.style = Paint.Style.FILL
            val particleCount = renderTier + 1
            repeat(particleCount) { particle ->
                val angle = menuPulse * (1.35f + particle * 0.11f) + particle * PI.toFloat() * 2f / particleCount
                val orbitRadius = radius * (1.14f + (particle % renderTier) * 0.13f)
                paint.color = withAlpha(if (particle % 2 == 0) skin.lineColor else skin.secondary, 220)
                canvas.drawCircle(
                    cx + cos(angle) * orbitRadius,
                    cy + sin(angle) * orbitRadius,
                    radius * (0.055f + effectTier * 0.012f),
                    paint
                )
            }
        }

        val art = brainballBitmap(skin)
        if (art != null) {
            val artRadius = radius * 1.1f
            val saveCount = canvas.save()
            path.reset()
            path.addCircle(cx, cy, artRadius * 0.985f, Path.Direction.CW)
            canvas.clipPath(path)
            paint.style = Paint.Style.FILL
            paint.alpha = if (locked) 205 else 255
            paint.isFilterBitmap = true
            scratch.set(cx - artRadius, cy - artRadius, cx + artRadius, cy + artRadius)
            canvas.drawBitmap(art, null, scratch, paint)
            paint.alpha = 255
            if (locked) {
                paint.color = 0x44232936
                canvas.drawCircle(cx, cy, radius, paint)
            }
            canvas.restoreToCount(saveCount)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = radius * 0.075f
            paint.color = if (locked) 0x9999A1B5.toInt() else withAlpha(skin.lineColor, 240)
            canvas.drawCircle(cx, cy, radius * 0.96f, paint)
            if (locked) drawBrainballLock(canvas, cx, cy, radius)
            return
        }

        paint.style = Paint.Style.FILL
        paint.color = if (locked) 0xFF333947.toInt() else skin.primary
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.08f
        paint.color = if (locked) 0x7799A1B5 else withAlpha(skin.secondary, 230)
        canvas.drawCircle(cx, cy, radius * 0.94f, paint)

        when (skin.style) {
            SkinStyle.PRISM -> {
                paint.style = Paint.Style.FILL
                path.reset()
                path.moveTo(cx, cy - radius * 0.9f)
                path.lineTo(cx - radius * 0.78f, cy + radius * 0.24f)
                path.lineTo(cx, cy + radius * 0.86f)
                path.lineTo(cx + radius * 0.78f, cy + radius * 0.24f)
                path.close()
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 210)
                canvas.drawPath(path, paint)
                paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 45 else 150)
                canvas.drawCircle(cx - radius * 0.24f, cy - radius * 0.22f, radius * 0.18f, paint)
            }

            SkinStyle.VOID -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.14f
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 230)
                canvas.drawCircle(cx, cy, radius * 0.58f, paint)
                paint.strokeWidth = radius * 0.07f
                paint.color = withAlpha(skin.lineColor, if (locked) 70 else 200)
                scratch.set(cx - radius * 0.9f, cy - radius * 0.34f, cx + radius * 0.9f, cy + radius * 0.34f)
                canvas.drawArc(scratch, 12f + wave * 10f, 220f, false, paint)
            }

            SkinStyle.CHROME -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 55 else 185)
                scratch.set(cx - radius * 0.55f, cy - radius * 0.72f, cx + radius * 0.2f, cy - radius * 0.18f)
                canvas.drawOval(scratch, paint)
                paint.color = withAlpha(skin.secondary, if (locked) 55 else 165)
                canvas.drawRect(cx - radius * 0.86f, cy + radius * 0.02f, cx + radius * 0.86f, cy + radius * 0.2f, paint)
            }

            SkinStyle.PLASMA -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.13f
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 230)
                repeat(5) { i ->
                    val angle = i * PI.toFloat() * 2f / 5f + wave * 0.2f
                    canvas.drawLine(cx, cy, cx + cos(angle) * radius * 0.82f, cy + sin(angle) * radius * 0.82f, paint)
                }
                paint.strokeCap = Paint.Cap.BUTT
            }

            SkinStyle.BLOP -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 90 else 235)
                canvas.drawOval(cx - radius * 0.62f, cy - radius * 0.48f, cx + radius * 0.2f, cy + radius * 0.25f, paint)
            }

            SkinStyle.GLITCH -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 210)
                canvas.drawRect(cx - radius * 0.9f, cy - radius * 0.34f, cx + radius * 0.2f, cy - radius * 0.12f, paint)
                paint.color = withAlpha(skin.lineColor, if (locked) 60 else 190)
                canvas.drawRect(cx - radius * 0.2f, cy + radius * 0.28f, cx + radius * 0.9f, cy + radius * 0.48f, paint)
            }

            SkinStyle.ZAP -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 80 else 235)
                path.reset()
                path.moveTo(cx + radius * 0.08f, cy - radius * 0.82f)
                path.lineTo(cx - radius * 0.36f, cy + radius * 0.02f)
                path.lineTo(cx + radius * 0.02f, cy + radius * 0.02f)
                path.lineTo(cx - radius * 0.12f, cy + radius * 0.82f)
                path.lineTo(cx + radius * 0.45f, cy - radius * 0.18f)
                path.lineTo(cx + radius * 0.06f, cy - radius * 0.18f)
                path.close()
                canvas.drawPath(path, paint)
            }

            SkinStyle.LOOP -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.12f
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 220)
                scratch.set(cx - radius * 0.7f, cy - radius * 0.35f, cx, cy + radius * 0.35f)
                canvas.drawOval(scratch, paint)
                scratch.set(cx, cy - radius * 0.35f, cx + radius * 0.7f, cy + radius * 0.35f)
                canvas.drawOval(scratch, paint)
            }

            SkinStyle.STATIC -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.09f
                paint.color = withAlpha(skin.secondary, if (locked) 80 else 220)
                repeat(4) { i ->
                    val y = cy - radius * 0.48f + i * radius * 0.3f
                    canvas.drawLine(cx - radius * 0.68f, y, cx + radius * 0.68f, y + sin(menuPulse * 6f + i) * radius * 0.08f, paint)
                }
            }

            SkinStyle.RIFT -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                path.reset()
                path.moveTo(cx - radius * 0.18f, cy - radius * 0.86f)
                path.lineTo(cx + radius * 0.18f, cy - radius * 0.12f)
                path.lineTo(cx - radius * 0.08f, cy + radius * 0.12f)
                path.lineTo(cx + radius * 0.24f, cy + radius * 0.86f)
                path.lineTo(cx - radius * 0.42f, cy + radius * 0.16f)
                path.lineTo(cx - radius * 0.12f, cy - radius * 0.1f)
                path.close()
                canvas.drawPath(path, paint)
            }

            SkinStyle.BYTE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.1f
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                scratch.set(cx - radius * 0.58f, cy - radius * 0.58f, cx + radius * 0.58f, cy + radius * 0.58f)
                canvas.drawRoundRect(scratch, radius * 0.16f, radius * 0.16f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawRect(cx - radius * 0.28f, cy - radius * 0.1f, cx + radius * 0.28f, cy + radius * 0.1f, paint)
            }

            SkinStyle.WOBBLE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.1f
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                path.reset()
                path.moveTo(cx - radius * 0.72f, cy)
                repeat(6) { i ->
                    val x = cx - radius * 0.72f + i * radius * 0.29f
                    val y = cy + sin(menuPulse * 2.8f + i) * radius * 0.28f
                    path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
            }

            SkinStyle.CROWN -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 230)
                path.reset()
                path.moveTo(cx - radius * 0.62f, cy - radius * 0.18f)
                path.lineTo(cx - radius * 0.36f, cy - radius * 0.78f)
                path.lineTo(cx, cy - radius * 0.24f)
                path.lineTo(cx + radius * 0.36f, cy - radius * 0.78f)
                path.lineTo(cx + radius * 0.62f, cy - radius * 0.18f)
                path.lineTo(cx + radius * 0.52f, cy + radius * 0.14f)
                path.lineTo(cx - radius * 0.52f, cy + radius * 0.14f)
                path.close()
                canvas.drawPath(path, paint)
            }

            SkinStyle.CLASSIC -> Unit
        }

        paint.style = Paint.Style.FILL
        paint.color = if (locked) 0x99232936.toInt() else 0xFF07090F.toInt()
        when (skin.style) {
            SkinStyle.VOID -> {
                canvas.drawCircle(cx - radius * 0.28f, cy - radius * 0.16f, radius * 0.1f, paint)
                canvas.drawCircle(cx + radius * 0.28f, cy - radius * 0.16f, radius * 0.1f, paint)
            }

            SkinStyle.GLITCH, SkinStyle.STATIC -> {
                canvas.drawRect(cx - radius * 0.45f, cy - radius * 0.25f, cx - radius * 0.12f, cy - radius * 0.06f, paint)
                canvas.drawRect(cx + radius * 0.14f, cy - radius * 0.22f, cx + radius * 0.48f, cy - radius * 0.02f, paint)
            }

            else -> {
                canvas.drawCircle(cx - radius * 0.32f, cy - radius * 0.18f, radius * 0.13f, paint)
                canvas.drawCircle(cx + radius * 0.32f, cy - radius * 0.18f, radius * 0.13f, paint)
            }
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.08f
        scratch.set(cx - radius * 0.42f, cy + radius * 0.02f, cx + radius * 0.42f, cy + radius * 0.42f)
        canvas.drawArc(scratch, 18f, 144f, false, paint)

        if (locked) {
            drawBrainballLock(canvas, cx, cy, radius)
        }
    }

    private fun brainballBitmap(skin: BallSkin): Bitmap? {
        brainballBitmaps[skin.id]?.let { return it }
        val resource = brainballArtResources[skin.id] ?: return null
        return BitmapFactory.decodeResource(resources, resource)?.also { brainballBitmaps[skin.id] = it }
    }

    private fun worldBitmap(key: String): Bitmap? {
        worldBitmaps[key]?.let { return it }
        val resource = worldArtResources[key] ?: return null
        val bitmap = if (key.startsWith("bg_")) {
            BitmapFactory.decodeResource(
                resources,
                resource,
                BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
            )
        } else {
            BitmapFactory.decodeResource(resources, resource)
        }
        return bitmap?.also { worldBitmaps[key] = it }
    }

    private fun backgroundBitmap(key: String): Bitmap? {
        val cacheKey = "$key:${viewWidth}x$viewHeight"
        scaledBackgroundBitmaps[cacheKey]?.takeIf { !it.isRecycled }?.let { return it }
        val source = worldBitmap(key) ?: return null
        return try {
            Bitmap.createScaledBitmap(source, viewWidth, viewHeight, renderProfile != RenderProfile.LOW)
                .also { scaledBackgroundBitmaps[cacheKey] = it }
        } catch (_: OutOfMemoryError) {
            source
        } catch (_: IllegalArgumentException) {
            source
        }
    }

    private fun recycleScaledBackgrounds() {
        scaledBackgroundBitmaps.values.forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        scaledBackgroundBitmaps.clear()
    }

    private fun drawWorldAsset(canvas: Canvas, key: String, bounds: RectF, alpha: Int = 255) {
        val bitmap = worldBitmap(key) ?: return
        paint.alpha = alpha.coerceIn(0, 255)
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, null, bounds, paint)
        paint.alpha = 255
    }

    private fun powerIconKey(power: BallPower): String = when (power) {
        BallPower.PRISM_SHIELD -> "boost_prism"
        BallPower.VOID_PHASE, BallPower.MINOR_PHASE -> "boost_void"
        BallPower.CHROME_RICOCHET, BallPower.MINOR_RICOCHET -> "boost_rebound"
        BallPower.PLASMA_SURGE, BallPower.MINOR_SURGE -> "boost_plasma"
        BallPower.NONE -> "boost_rift_pull"
    }

    private fun drawBrainballLock(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val lockCx = cx + radius * 0.5f
        val lockCy = cy + radius * 0.46f
        paint.style = Paint.Style.FILL
        paint.color = 0xE6070B12.toInt()
        canvas.drawCircle(lockCx, lockCy, radius * 0.34f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.075f
        paint.color = 0xDDF7F4FF.toInt()
        canvas.drawCircle(lockCx, lockCy, radius * 0.32f, paint)
        scratch.set(lockCx - radius * 0.16f, lockCy - radius * 0.01f, lockCx + radius * 0.16f, lockCy + radius * 0.2f)
        canvas.drawRoundRect(scratch, radius * 0.06f, radius * 0.06f, paint)
        canvas.drawArc(
            lockCx - radius * 0.12f,
            lockCy - radius * 0.2f,
            lockCx + radius * 0.12f,
            lockCy + radius * 0.08f,
            205f,
            130f,
            false,
            paint
        )
    }

    private fun drawMenuButton(
        canvas: Canvas,
        rect: RectF,
        button: MenuButton,
        title: String,
        meta: String,
        accent: Int
    ) {
        val active = activeMenuButton == button
        val compact = button == MenuButton.COLLECTION || button == MenuButton.LEADERBOARDS || button == MenuButton.BACK
        val modeButton = button == MenuButton.CLASSIC || button == MenuButton.CHAOS || button == MenuButton.CONTINUE
        val radius = dp(6f)
        paint.style = Paint.Style.FILL
        paint.color = if (active) withAlpha(accent, 82) else 0xE30D131D.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(withAlpha(accent, if (active) 74 else 32), 0x00161D29),
            floatArrayOf(0f, 0.78f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        paint.color = accent
        canvas.drawRect(rect.left, rect.top, rect.left + dp(if (active) 5f else 3f), rect.bottom, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (active) 1.8f else 0.8f)
        paint.color = if (active) withAlpha(accent, 230) else 0x32FFFFFF
        canvas.drawRoundRect(rect, radius, radius, paint)

        val iconCx = rect.left + dp(if (compact) 22f else 29f)
        val iconCy = rect.centerY()
        val iconRadius = dp(if (compact) 11f else 15f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, if (active) 150 else 90)
        if (richEffects()) {
            paint.maskFilter = BlurMaskFilter(iconRadius * 0.7f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(iconCx, iconCy, iconRadius * 1.15f, paint)
        paint.maskFilter = null
        val assetKey = when (button) {
            MenuButton.CLASSIC -> "portal_goal"
            MenuButton.CHAOS -> "hazard_glitch"
            MenuButton.START, MenuButton.CONTINUE -> "boost_goal"
            MenuButton.LEADERBOARDS -> "boost_chain"
            else -> null
        }
        when {
            button == MenuButton.COLLECTION -> {
                drawBallSkin(canvas, iconCx, iconCy, iconRadius, selectedBallSkin(), animated = true, locked = false)
            }

            assetKey != null -> {
                scratch.set(iconCx - iconRadius, iconCy - iconRadius, iconCx + iconRadius, iconCy + iconRadius)
                drawWorldAsset(canvas, assetKey, scratch)
            }

            button == MenuButton.BACK -> {
                paint.style = Paint.Style.STROKE
                paint.color = accent
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(2.4f)
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawLine(iconCx + dp(7f), iconCy - dp(7f), iconCx - dp(4f), iconCy, paint)
                canvas.drawLine(iconCx - dp(4f), iconCy, iconCx + dp(7f), iconCy + dp(7f), paint)
                paint.strokeCap = Paint.Cap.BUTT
            }

            else -> Unit
        }

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(if (compact) 12f else 16f)
        textPaint.color = 0xFFF7F4FF.toInt()
        val textLeft = rect.left + dp(if (compact) 42f else 56f)
        val titleY = if (compact) rect.top + dp(24f) else rect.top + dp(28f)
        canvas.drawText(fitText(title, rect.right - textLeft - dp(16f)), textLeft, titleY, textPaint)
        textPaint.textSize = dp(if (compact) 8.5f else 9.5f)
        textPaint.color = 0x99FFFFFF.toInt()
        canvas.drawText(fitText(meta, rect.right - textLeft - dp(16f)), textLeft, if (compact) rect.top + dp(41f) else rect.top + dp(48f), textPaint)

        if (modeButton) {
            val mode = when (button) {
                MenuButton.CHAOS -> GameMode.CHAOS
                MenuButton.CLASSIC -> GameMode.CLASSIC
                else -> selectedMenuMode
            }
            val cleared = (modeProgress(mode) - 1).coerceAtLeast(0)
            val progress = (cleared.mod(5)) / 5f
            val barLeft = rect.left + dp(56f)
            val barTop = rect.bottom - dp(11f)
            val barRight = rect.right - dp(42f)
            paint.style = Paint.Style.FILL
            paint.color = 0x22FFFFFF
            canvas.drawRoundRect(barLeft, barTop, barRight, barTop + dp(3f), dp(2f), dp(2f), paint)
            paint.color = accent
            canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress.coerceIn(0f, 1f), barTop + dp(3f), dp(2f), dp(2f), paint)

            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = dp(11f)
            textPaint.color = withAlpha(accent, 230)
            canvas.drawText("L${modeProgress(mode).toString().padStart(2, '0')}", rect.right - dp(16f), rect.top + dp(28f), textPaint)
            drawMenuChevron(canvas, rect.right - dp(20f), rect.bottom - dp(17f), accent)
        }

        if (button == MenuButton.CONTINUE) {
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = dp(9f)
            textPaint.color = 0xFFFFCF4A.toInt()
            scratch.set(rect.right - dp(42f), rect.top + dp(35f), rect.right - dp(14f), rect.top + dp(51f))
            paint.style = Paint.Style.FILL
            paint.color = 0x33FFCF4A
            canvas.drawRoundRect(scratch, dp(4f), dp(4f), paint)
            canvas.drawText(t("AD").uppercase(), scratch.centerX(), scratch.centerY() + dp(3f), textPaint)
        }
    }

    private fun drawHomeModeChip(canvas: Canvas, rect: RectF, mode: GameMode, accent: Int) {
        val selected = selectedMenuMode == mode
        val active = activeMenuButton == if (mode == GameMode.CLASSIC) MenuButton.CLASSIC else MenuButton.CHAOS
        val radius = dp(8f)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                if (selected) withAlpha(accent, 92) else 0xC80B1019.toInt(),
                if (active) withAlpha(accent, 62) else 0xE30D131D.toInt()
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (selected || active) 1.6f else 0.8f)
        paint.color = if (selected) withAlpha(accent, 235) else 0x42FFFFFF
        canvas.drawRoundRect(rect, radius, radius, paint)

        val iconSize = dp(28f)
        scratch.set(rect.left + dp(10f), rect.centerY() - iconSize * 0.5f, rect.left + dp(10f) + iconSize, rect.centerY() + iconSize * 0.5f)
        drawWorldAsset(canvas, if (mode == GameMode.CLASSIC) "portal_goal" else "hazard_glitch", scratch, if (selected) 255 else 210)

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(12.5f)
        textPaint.color = 0xFFF7F4FF.toInt()
        val textLeft = rect.left + dp(46f)
        canvas.drawText(mode.menuTitle(), textLeft, rect.top + dp(22f), textPaint)
        textPaint.textSize = dp(8.3f)
        textPaint.color = if (selected) withAlpha(accent, 230) else 0x99FFFFFF.toInt()
        val meta = homeModeChipMeta(mode, selected)
        canvas.drawText(fitText(meta, rect.right - textLeft - dp(10f)), textLeft, rect.top + dp(39f), textPaint)

        if (modeProgress(mode) > 1) {
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = dp(10f)
            textPaint.color = withAlpha(accent, 235)
            canvas.drawText("L${modeProgress(mode).toString().padStart(2, '0')}", rect.right - dp(10f), rect.top + dp(22f), textPaint)
        }
    }

    private fun homeModeChipMeta(mode: GameMode, selected: Boolean): String {
        val progress = modeProgress(mode)
        if (progress <= 1) return t("START LEVEL 01").uppercase()
        val compact = "L${progress.toString().padStart(2, '0')} / x${modeStreak(mode)}"
        return if (selected) "${t("SELECTED").uppercase()} / $compact" else compact
    }

    private fun drawPrimaryPlayButton(canvas: Canvas, rect: RectF) {
        val mode = selectedMenuMode
        val accent = if (mode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
        val active = activeMenuButton == MenuButton.PLAY
        val hasProgress = modeProgress(mode) > 1
        val radius = dp(9f)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                if (active) withAlpha(accent, 238) else withAlpha(accent, 212),
                if (mode == GameMode.CHAOS) 0xFF5C1432.toInt() else 0xFF073A35.toInt()
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (active) 2f else 1f)
        paint.color = 0xBBFFFFFF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)

        val iconSize = dp(45f)
        scratch.set(rect.left + dp(13f), rect.centerY() - iconSize * 0.5f, rect.left + dp(13f) + iconSize, rect.centerY() + iconSize * 0.5f)
        drawWorldAsset(canvas, if (mode == GameMode.CHAOS) "boost_plasma" else "boost_goal", scratch, 245)

        val title = if (hasProgress) "${t("CONTINUE").uppercase()} ${mode.menuTitle()}" else "${t("PLAY").uppercase()} ${mode.menuTitle()}"
        val meta = if (hasProgress) {
            "${t("LEVEL").uppercase()} ${modeProgress(mode).toString().padStart(2, '0')}  /  ${t("STREAK").uppercase()} ${modeStreak(mode)}  /  ${t("AD CHECK").uppercase()}"
        } else {
            "${t("START LEVEL 01").uppercase()}  /  ${t("FIRST RUN").uppercase()}"
        }
        val textLeft = rect.left + dp(72f)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(18f)
        textPaint.color = 0xFF07090F.toInt()
        canvas.drawText(fitText(title, rect.right - textLeft - dp(66f)), textLeft, rect.top + dp(28f), textPaint)
        textPaint.textSize = dp(9.2f)
        textPaint.color = 0xCC07090F.toInt()
        canvas.drawText(fitText(meta, rect.right - textLeft - dp(22f)), textLeft, rect.top + dp(49f), textPaint)

        paint.style = Paint.Style.FILL
        paint.color = 0x3307090F
        canvas.drawCircle(rect.right - dp(28f), rect.centerY(), dp(17f), paint)
        drawMenuChevron(canvas, rect.right - dp(20f), rect.centerY(), 0xFF07090F.toInt())
    }

    private fun drawMenuChevron(canvas: Canvas, x: Float, y: Float, accent: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.8f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = withAlpha(accent, 210)
        canvas.drawLine(x - dp(4f), y - dp(4f), x, y, paint)
        canvas.drawLine(x, y, x - dp(4f), y + dp(4f), paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawCollection(canvas: Canvas) {
        drawCollectionBackdrop(canvas)
        layoutCollection()

        val left = pageContentLeft() + dp(2f)
        val top = dp(22f)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8.5f)
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText("KAVVORO  /  ${t("VAULT").uppercase()}", left, top + dp(8f), textPaint)
        textPaint.textSize = dp(29f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(t("COLLECTION").uppercase(), collectionBackButton.left - left - dp(10f)), left, top + dp(42f), textPaint)

        val owned = unlockedSkinCount()
        val metaTop = top + dp(54f)
        drawCollectionStatChip(canvas, left, metaTop, t("OWNED").uppercase(), "$owned/${ballSkins.size}", 0xFF1DE8C8.toInt())
        drawCollectionStatChip(canvas, left + dp(103f), metaTop, t("STREAK").uppercase(), "x${bestStreak()}", 0xFFC15CFF.toInt())
        if (collectionRestoreButton.left - left >= dp(308f)) {
            drawCollectionStatChip(canvas, left + dp(206f), metaTop, t("HYPE BANK").uppercase(), formatHypeAmount(hypeBalance()), 0xFFFFCF4A.toInt())
        }

        drawCollectionBackButton(canvas)
        drawCollectionRestoreButton(canvas)
        drawCollectionLoadout(canvas)
        drawCollectionFilters(canvas)

        val viewportTop = collectionViewportTop()
        val viewportBottom = collectionViewportBottom()
        canvas.save()
        canvas.clipRect(0f, viewportTop, viewWidth.toFloat(), viewportBottom)
        ballSkins.forEachIndexed { index, skin ->
            val rect = collectionItemRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.bottom >= viewportTop && rect.top <= viewportBottom) {
                drawCollectionItem(canvas, rect, index, skin)
            }
        }
        canvas.restore()

        val footer = collectionMessage.ifBlank {
            nextRewardText()?.let { "${t("NEXT MUTATION").uppercase()} / $it" } ?: t("VAULT COMPLETE / MAXIMUM BRAIN ACHIEVED")
        }
        drawStatusMessage(canvas, footer, selectedBallSkin().lineColor, collectionMessage.isNotBlank())
    }

    private fun drawCollectionBackdrop(canvas: Canvas) {
        paint.shader = LinearGradient(
            0f,
            0f,
            viewWidth.toFloat(),
            viewHeight.toFloat(),
            intArrayOf(0xFF05070D.toInt(), 0xFF0A1018.toInt(), 0xFF07090F.toInt()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = 0x0FFFFFFF
        val step = dp(58f)
        var x = -((menuPulse * dp(4f)) % step)
        while (x < viewWidth + step) {
            canvas.drawLine(x, 0f, x, viewHeight.toFloat(), paint)
            x += step
        }
        var y = dp(76f)
        while (y < viewHeight) {
            canvas.drawLine(0f, y, viewWidth.toFloat(), y, paint)
            y += step
        }

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            viewHeight.toFloat(),
            intArrayOf(0x0005090F, 0x441DE8C8, 0x001DE8C8),
            floatArrayOf(0f, 0.34f, 1f),
            Shader.TileMode.CLAMP
        )
        scratch.set(0f, 0f, dp(5f), viewHeight.toFloat())
        canvas.drawRect(scratch, paint)
        scratch.set(viewWidth - dp(5f), 0f, viewWidth.toFloat(), viewHeight.toFloat())
        canvas.drawRect(scratch, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(2.2f)
        repeat(5) { i ->
            val yy = dp(150f + i * 132f) + sin(menuPulse * 0.8f + i) * dp(7f)
            paint.color = withAlpha(if (i % 2 == 0) 0xFF1DE8C8.toInt() else 0xFFFF4D8D.toInt(), 35)
            canvas.drawLine(dp(16f), yy, viewWidth - dp(16f), yy + dp(if (i % 2 == 0) 12f else -10f), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawCollectionStatChip(canvas: Canvas, left: Float, top: Float, label: String, value: String, accent: Int) {
        val width = dp(92f)
        val height = dp(24f)
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xCC0A0F18.toInt()
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(accent, 115)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(6.7f)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(label, left + dp(8f), top + dp(9f), textPaint)
        textPaint.textSize = dp(10f)
        textPaint.color = accent
        canvas.drawText(fitText(value, width - dp(16f)), left + dp(8f), top + dp(20f), textPaint)
    }

    private fun drawCollectionLoadout(canvas: Canvas) {
        val skin = focusedCollectionSkin()
        val equipped = selectedBallSkin().id == skin.id
        val unlocked = isSkinUnlocked(skin)
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val hypeReady = skin.unlock.type == UnlockType.HYPE_COST && hypeBalance() >= skin.unlock.value
        val powered = skin.power != BallPower.NONE
        val left = pageContentLeft()
        val top = dp(104f)
        val right = pageContentRight()
        val bottom = top + dp(78f)
        val accent = brainballRarityColor(skin)

        scratch.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xF20A0F18.toInt()
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.shader = LinearGradient(
            left,
            top,
            right,
            bottom,
            intArrayOf(withAlpha(if (powered) 0xFFFFCF4A.toInt() else accent, if (powered) 92 else 64), withAlpha(accent, if (powered) 34 else 0), 0x000A0F18),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (powered) 1.7f else 1.1f)
        paint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else accent, if (powered) 235 else 185)
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRect(left, top, left + dp(4f), bottom, paint)

        drawCollectionAvatar(
            canvas = canvas,
            cx = left + dp(43f),
            cy = (top + bottom) * 0.5f,
            radius = dp(29f),
            skin = skin,
            locked = !unlocked && !premium,
            selected = equipped,
            large = true
        )

        val textLeft = left + dp(84f)
        val rightColumn = dp(70f)
        val textMaxWidth = (right - textLeft - rightColumn - dp(10f)).coerceAtLeast(dp(96f))
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8f)
        textPaint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else accent, 245)
        val kicker = when {
            powered -> "${powerTierLabel(skin)}  /  ${ballPowerName(skin.power)}"
            equipped -> t("EQUIPPED BRAINBALL").uppercase()
            else -> "${t("INSPECTING").uppercase()} ${brainballRarity(skin)}"
        }
        canvas.drawText(
            fitText(kicker, textMaxWidth),
            textLeft,
            top + dp(19f),
            textPaint
        )
        textPaint.textSize = dp(18f)
        textPaint.color = 0xFFF7F4FF.toInt()
        drawFittedText(canvas, skin.name, textLeft, top + dp(43f), textMaxWidth, 18f, 12f)
        textPaint.color = 0xAFFFFFFF.toInt()
        val detail = when {
            powered && premium -> "${ballPowerDescription(skin.power)} / ${premiumPriceLabel(skin)}"
            powered -> ballPowerDescription(skin.power)
            equipped -> "${brainballCardSubtitle(skin)} / ${t("ACTIVE").uppercase()}"
            unlocked -> "${brainballCardSubtitle(skin)} / ${t("TAP TO EQUIP").uppercase()}"
            hypeReady -> "${t("READY TO MUTATE").uppercase()} / ${t("TAP TO UNLOCK").uppercase()}"
            else -> unlockLongLabel(skin).uppercase()
        }
        drawFittedText(canvas, detail, textLeft, top + dp(61f), textMaxWidth, 9.3f, 7.2f)

        scratch2.set(right - dp(62f), top + dp(15f), right - dp(12f), bottom - dp(15f))
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (powered) skin.lineColor else 0xFFFFCF4A.toInt(), if (powered) 40 else 24)
        canvas.drawRoundRect(scratch2, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.9f)
        paint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else 0xFFFFCF4A.toInt(), if (powered) 180 else 70)
        canvas.drawRoundRect(scratch2, dp(7f), dp(7f), paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = dp(7.4f)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(if (powered) t("POWER").uppercase() else t("AURA").uppercase(), scratch2.centerX(), scratch2.top + dp(14f), textPaint)
        if (powered) {
            scratch3.set(scratch2.centerX() - dp(13f), scratch2.top + dp(18f), scratch2.centerX() + dp(13f), scratch2.top + dp(44f))
            drawWorldAsset(canvas, powerIconKey(skin.power), scratch3, 245)
        } else {
            textPaint.textSize = dp(16f)
            textPaint.color = 0xFFFFCF4A.toInt()
            canvas.drawText(brainballAura(skin).toString(), scratch2.centerX(), scratch2.top + dp(35f), textPaint)
        }
    }

    private fun drawCollectionFilters(canvas: Canvas) {
        CollectionFilter.entries.forEachIndexed { index, filter ->
            val rect = collectionFilterRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.isEmpty) return@forEachIndexed
            val selected = filter == collectionFilter
            val active = activeCollectionIndex == collectionFilterActiveIndex(index)
            val accent = when (filter) {
                CollectionFilter.ALL -> 0xFFF7F4FF.toInt()
                CollectionFilter.SUPERPOWER -> 0xFFFFCF4A.toInt()
                CollectionFilter.HYPE -> 0xFF1DE8C8.toInt()
                CollectionFilter.PREMIUM -> 0xFFFF4D8D.toInt()
                CollectionFilter.COSMETIC -> 0xFF8AA6FF.toInt()
            }
            paint.style = Paint.Style.FILL
            paint.color = when {
                selected -> withAlpha(accent, 62)
                active -> withAlpha(accent, 42)
                else -> 0x9A080C13.toInt()
            }
            canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(if (selected) 1.4f else 0.8f)
            paint.color = withAlpha(accent, if (selected) 220 else 110)
            canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
            if (filter == CollectionFilter.SUPERPOWER) {
                val icon = dp(13f)
                scratch.set(rect.left + dp(5f), rect.centerY() - icon * 0.5f, rect.left + dp(5f) + icon, rect.centerY() + icon * 0.5f)
                drawWorldAsset(canvas, "boost_plasma", scratch, if (selected) 245 else 170)
            }
            textPaint.shader = null
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            textPaint.textSize = dp(8f)
            textPaint.color = if (selected) 0xFFF7F4FF.toInt() else withAlpha(0xFFF7F4FF.toInt(), 170)
            val labelLeftPad = if (filter == CollectionFilter.SUPERPOWER) dp(12f) else 0f
            canvas.drawText(
                fitText(t(filter.labelKey).uppercase(), rect.width() - dp(10f) - labelLeftPad),
                rect.centerX() + labelLeftPad * 0.35f,
                rect.centerY() + dp(3f),
                textPaint
            )
        }
    }

    private fun drawCollectionBackButton(canvas: Canvas) {
        drawUiButtonFrame(
            canvas = canvas,
            rect = collectionBackButton,
            active = activeCollectionIndex == COLLECTION_BACK_INDEX,
            accent = 0xFF45F2FF.toInt(),
            cornerDp = 7f
        )
        drawUiIconAsset(canvas, "ui_back", collectionBackButton, padDp = -1f, alpha = 245)
    }

    private fun drawCollectionRestoreButton(canvas: Canvas) {
        val active = activeCollectionIndex == COLLECTION_RESTORE_INDEX
        drawUiButtonFrame(canvas, collectionRestoreButton, active, 0xFF1DE8C8.toInt(), cornerDp = 5f)
        val iconSize = dp(19f)
        scratch.set(
            collectionRestoreButton.left + dp(2f),
            collectionRestoreButton.centerY() - iconSize * 0.5f,
            collectionRestoreButton.left + dp(2f) + iconSize,
            collectionRestoreButton.centerY() + iconSize * 0.5f
        )
        drawWorldAsset(canvas, "ui_restore", scratch, if (active) 255 else 225)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(7.5f)
        textPaint.color = 0xFFBDFBF2.toInt()
        canvas.drawText(t("RESTORE").uppercase(), collectionRestoreButton.centerX() + dp(8f), collectionRestoreButton.centerY() + dp(2.6f), textPaint)
    }

    private fun drawCollectionItem(canvas: Canvas, rect: RectF, index: Int, skin: BallSkin) {
        val unlocked = isSkinUnlocked(skin)
        val selected = selectedBallSkin().id == skin.id
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val hypeReady = skin.unlock.type == UnlockType.HYPE_COST && hypeBalance() >= skin.unlock.value
        val powered = skin.power != BallPower.NONE
        val active = activeCollectionIndex == index

        val rarity = brainballRarity(skin)
        val rarityColor = brainballRarityColor(skin)
        val locked = !unlocked && !premium
        val corner = dp(8f)
        paint.style = Paint.Style.FILL
        paint.color = when {
            selected -> 0xF20D1320.toInt()
            premium -> 0xF2181220.toInt()
            unlocked -> 0xEF0A0F18.toInt()
            else -> 0xF2070B12.toInt()
        }
        canvas.drawRoundRect(rect, corner, corner, paint)

        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                withAlpha(if (powered) 0xFFFFCF4A.toInt() else rarityColor, if (premium || powered) 78 else 42),
                withAlpha(if (powered) skin.lineColor else 0xFF0A0F18.toInt(), if (powered) 36 else 0),
                withAlpha(0xFF07090F.toInt(), 88)
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        paint.style = Paint.Style.FILL
        paint.color = if (powered) 0xFFFFCF4A.toInt() else rarityColor
        canvas.drawRect(rect.left, rect.top, rect.left + dp(if (selected) 5f else 3f), rect.bottom, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (selected || active) 2f else 1f)
        paint.color = when {
            selected -> skin.lineColor
            premium -> withAlpha(0xFFFFCF4A.toInt(), 210)
            powered -> withAlpha(0xFFFFCF4A.toInt(), 220)
            unlocked -> withAlpha(skin.lineColor, 150)
            else -> 0x44FFFFFF
        }
        canvas.drawRoundRect(rect, corner, corner, paint)

        val isSingleColumn = rect.width() > dp(300f)
        val avatarRadius = min(rect.height() * 0.34f, dp(if (isSingleColumn) 32f else 27f))
        val avatarCx = rect.left + dp(if (isSingleColumn) 46f else 37f)
        val avatarCy = rect.centerY() + dp(2f)
        drawCollectionAvatar(
            canvas,
            avatarCx,
            avatarCy,
            avatarRadius,
            skin = skin,
            locked = locked,
            selected = selected,
            large = false
        )
        drawCollectionPowerBadge(canvas, avatarCx, avatarCy, avatarRadius, skin, locked)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        val textLeft = rect.left + dp(if (isSingleColumn) 92f else 74f)
        val textWidth = rect.right - textLeft - dp(11f)
        if (powered) {
            val ribbonWidth = if (isSingleColumn) textWidth - dp(68f) else rect.width() * 0.46f
            drawCollectionPowerRibbon(canvas, textLeft, rect.top + dp(7f), ribbonWidth.coerceAtLeast(dp(72f)), skin, locked)
        } else {
            textPaint.textSize = dp(7.5f)
            textPaint.color = withAlpha(rarityColor, if (unlocked || premium) 240 else 150)
            val rarityWidth = if (isSingleColumn) textWidth * 0.62f else rect.width() * 0.46f
            canvas.drawText(fitText(rarity, rarityWidth), textLeft, rect.top + dp(18f), textPaint)
        }

        scratch2.set(rect.right - dp(70f), rect.top + dp(10f), rect.right - dp(10f), rect.top + dp(31f))
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else 0xFFFFFFFF.toInt(), if (premium || powered) 30 else 16)
        canvas.drawRoundRect(scratch2, dp(6f), dp(6f), paint)
        if (powered) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(0.7f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 155)
            canvas.drawRoundRect(scratch2, dp(6f), dp(6f), paint)
            val icon = dp(13f)
            scratch3.set(scratch2.left + dp(5f), scratch2.centerY() - icon * 0.5f, scratch2.left + dp(5f) + icon, scratch2.centerY() + icon * 0.5f)
            drawWorldAsset(canvas, powerIconKey(skin.power), scratch3, if (locked) 145 else 230)
        }
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = dp(7.4f)
        textPaint.color = if (premium || powered) 0xFFFFCF4A.toInt() else 0x88FFFFFF.toInt()
        val chipLabel = if (powered) t("POWER").uppercase() else "${t("AURA").uppercase()} ${brainballAura(skin)}"
        canvas.drawText(
            fitText(chipLabel, scratch2.width() - dp(if (powered) 24f else 8f)),
            scratch2.centerX() + if (powered) dp(8f) else 0f,
            scratch2.top + dp(14f),
            textPaint
        )

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = if (unlocked || premium) 0xFFF7F4FF.toInt() else 0xAAFFFFFF.toInt()
        drawFittedText(canvas, skin.name, textLeft, rect.top + dp(44f), textWidth, if (isSingleColumn) 16f else 12.5f, 9.2f)

        textPaint.color = 0xAFFFFFFF.toInt()
        drawFittedText(canvas, brainballCardSubtitle(skin), textLeft, rect.top + dp(62f), textWidth, if (isSingleColumn) 9.2f else 8.4f, 7.1f)

        val label = when {
            selected -> t("EQUIPPED").uppercase()
            unlocked -> t("EQUIP").uppercase()
            premium && powered -> "${t("GET").uppercase()} ${premiumCompactPriceLabel(skin)} / ${ballPowerName(skin.power)}"
            premium -> "${t("GET").uppercase()} ${premiumCompactPriceLabel(skin)}"
            hypeReady && powered -> "${t("UNLOCK").uppercase()} ${unlockShortLabel(skin)} / ${ballPowerName(skin.power)}"
            hypeReady -> "${t("UNLOCK").uppercase()} ${unlockShortLabel(skin)}"
            powered -> "${t("NEEDS").uppercase()} ${unlockShortLabel(skin)} / ${ballPowerName(skin.power)}"
            else -> "${t("NEEDS").uppercase()} ${unlockShortLabel(skin)}"
        }
        textPaint.color = when {
            selected -> skin.lineColor
            premium || powered -> 0xFFFFCF4A.toInt()
            hypeReady -> 0xFFFFCF4A.toInt()
            unlocked -> 0xCCFFFFFF.toInt()
            else -> withAlpha(skin.lineColor, 210)
        }
        textPaint.textSize = dp(8.8f)
        val statusWidth = (textPaint.measureText(label) + dp(18f)).coerceAtMost(if (isSingleColumn) dp(128f) else textWidth)
        scratch.set(rect.right - dp(12f) - statusWidth, rect.bottom - dp(30f), rect.right - dp(12f), rect.bottom - dp(9f))
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (premium || hypeReady || powered) 0xFFFFCF4A.toInt() else rarityColor, if (unlocked || premium || selected || hypeReady || powered) 42 else 22)
        canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = dp(8.8f)
        canvas.drawText(fitText(label, statusWidth - dp(16f)), scratch.centerX(), rect.bottom - dp(15f), textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawCollectionPowerRibbon(canvas: Canvas, left: Float, top: Float, width: Float, skin: BallSkin, locked: Boolean) {
        val height = dp(19f)
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratch.left,
            scratch.top,
            scratch.right,
            scratch.bottom,
            intArrayOf(withAlpha(0xFFFFCF4A.toInt(), if (locked) 72 else 130), withAlpha(skin.lineColor, if (locked) 34 else 74)),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(0xFFFFCF4A.toInt(), if (locked) 130 else 230)
        canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
        val icon = dp(13f)
        scratch2.set(left + dp(4f), top + height * 0.5f - icon * 0.5f, left + dp(4f) + icon, top + height * 0.5f + icon * 0.5f)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratch2, if (locked) 135 else 230)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(7.2f)
        textPaint.color = if (locked) 0xBBFFFFFF.toInt() else 0xFFF7F4FF.toInt()
        canvas.drawText(
            fitText(powerTierLabel(skin), width - dp(24f)),
            left + dp(21f),
            top + dp(12.8f),
            textPaint
        )
    }

    private fun drawCollectionPowerBadge(canvas: Canvas, cx: Float, cy: Float, radius: Float, skin: BallSkin, locked: Boolean) {
        if (skin.power == BallPower.NONE) return
        val badgeRadius = radius * 0.42f
        val bx = cx + radius * 0.72f
        val by = cy - radius * 0.68f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFFFFCF4A.toInt(), if (locked) 65 else 120)
        canvas.drawCircle(bx, by, badgeRadius * 1.55f, paint)
        paint.color = withAlpha(0xFF07090F.toInt(), if (locked) 190 else 230)
        canvas.drawCircle(bx, by, badgeRadius * 1.2f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = withAlpha(if (locked) 0xFF8AA6FF.toInt() else 0xFFFFCF4A.toInt(), if (locked) 150 else 245)
        canvas.drawCircle(bx, by, badgeRadius * 1.22f, paint)
        scratch3.set(bx - badgeRadius, by - badgeRadius, bx + badgeRadius, by + badgeRadius)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratch3, if (locked) 150 else 245)
    }

    private fun drawCollectionAvatar(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        locked: Boolean,
        selected: Boolean,
        large: Boolean
    ) {
        val accent = brainballRarityColor(skin)
        val shellRadius = radius * if (large) 1.14f else 1.1f

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, if (locked) 42 else 74)
        canvas.drawCircle(cx, cy, shellRadius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (selected) 2.4f else 1.5f)
        paint.color = withAlpha(if (selected) skin.lineColor else accent, if (locked) 105 else 230)
        canvas.drawCircle(cx, cy, shellRadius * 0.92f, paint)

        val art = brainballBitmap(skin)
        val imageRadius = radius * if (large) 0.91f else 0.88f
        val saveCount = canvas.save()
        path.reset()
        path.addCircle(cx, cy, imageRadius, Path.Direction.CW)
        canvas.clipPath(path)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF05070D.toInt()
        canvas.drawCircle(cx, cy, imageRadius, paint)
        if (art != null) {
            paint.alpha = if (locked) 145 else 255
            paint.isFilterBitmap = true
            scratch3.set(cx - imageRadius, cy - imageRadius, cx + imageRadius, cy + imageRadius)
            canvas.drawBitmap(art, null, scratch3, paint)
            paint.alpha = 255
        } else {
            paint.color = if (locked) 0xFF333947.toInt() else skin.primary
            canvas.drawCircle(cx, cy, imageRadius, paint)
        }
        if (locked) {
            paint.color = 0x6610151E
            canvas.drawCircle(cx, cy, imageRadius, paint)
        }
        canvas.restoreToCount(saveCount)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.1f)
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 85 else 145)
        canvas.drawCircle(cx, cy, imageRadius, paint)
        if (locked) {
            drawBrainballLock(canvas, cx, cy, radius * 0.88f)
        }
    }

    private fun brainballRarity(skin: BallSkin): String = when {
        skin.power != BallPower.NONE -> powerTierLabel(skin)
        skin.unlock.type == UnlockType.PREMIUM -> t("MYTHIC BRAINROT").uppercase()
        skin.unlock.type == UnlockType.DEFAULT -> t("ORIGINAL SPECIMEN").uppercase()
        skin.style == SkinStyle.CROWN -> t("MAX AURA").uppercase()
        skin.style == SkinStyle.GLITCH || skin.style == SkinStyle.STATIC -> t("GLITCHED").uppercase()
        skin.style == SkinStyle.RIFT || skin.style == SkinStyle.VOID -> t("FORBIDDEN").uppercase()
        skin.style == SkinStyle.ZAP || skin.style == SkinStyle.PLASMA -> t("OVERCLOCKED").uppercase()
        skin.style == SkinStyle.BLOP || skin.style == SkinStyle.WOBBLE -> t("GOOFY CLASS").uppercase()
        else -> t("RARE THOUGHT").uppercase()
    }

    private fun powerTierLabel(skin: BallSkin): String {
        if (skin.power == BallPower.NONE) return t("NO POWER").uppercase()
        return when {
            skin.unlock.type == UnlockType.PREMIUM -> t("MYTHIC SUPERPOWER").uppercase()
            skin.power == BallPower.MINOR_PHASE || skin.power == BallPower.MINOR_RICOCHET || skin.power == BallPower.MINOR_SURGE -> t("LITE SUPERPOWER").uppercase()
            else -> t("EARNED SUPERPOWER").uppercase()
        }
    }

    private fun brainballRarityColor(skin: BallSkin): Int = when {
        skin.unlock.type == UnlockType.PREMIUM -> 0xFFFFCF4A.toInt()
        skin.power != BallPower.NONE -> 0xFF64E572.toInt()
        skin.style == SkinStyle.CROWN -> 0xFFFFCF4A.toInt()
        skin.style == SkinStyle.GLITCH || skin.style == SkinStyle.STATIC -> 0xFFC15CFF.toInt()
        skin.style == SkinStyle.RIFT || skin.style == SkinStyle.VOID -> 0xFFFF4D8D.toInt()
        else -> skin.lineColor
    }

    private fun brainballAura(skin: BallSkin): Int {
        val index = ballSkins.indexOfFirst { it.id == skin.id }.coerceAtLeast(0)
        return if (skin.unlock.type == UnlockType.PREMIUM) 9999 - index * 111 else 404 + index * 137
    }

    private fun brainballCardSubtitle(skin: BallSkin): String {
        return if (skin.power == BallPower.NONE) skin.subtitle.uppercase() else ballPowerName(skin.power)
    }

    private fun ballPowerName(power: BallPower): String = when (power) {
        BallPower.NONE -> t("NO POWER")
        BallPower.PRISM_SHIELD -> t("PRISM DENIAL")
        BallPower.VOID_PHASE -> t("VOID PHASE")
        BallPower.CHROME_RICOCHET -> t("CHROME REBOUND")
        BallPower.PLASMA_SURGE -> t("PLASMA SURGE")
        BallPower.MINOR_PHASE -> t("PHASE LITE")
        BallPower.MINOR_RICOCHET -> t("REBOUND LITE")
        BallPower.MINOR_SURGE -> t("SURGE LITE")
    }.uppercase()

    private fun ballPowerDescription(power: BallPower): String = when (power) {
        BallPower.NONE -> t("COSMETIC LOADOUT")
        BallPower.PRISM_SHIELD -> t("BLOCKS THE FIRST HAZARD HIT")
        BallPower.VOID_PHASE -> t("SLIPS CLOSER TO HAZARDS")
        BallPower.CHROME_RICOCHET -> t("HARDER BOUNCES AND MORE SPEED")
        BallPower.PLASMA_SURGE -> t("STRONGER PULL AND 35% FASTER RECHARGE")
        BallPower.MINOR_PHASE -> t("SMALL HAZARD HITBOX REDUCTION")
        BallPower.MINOR_RICOCHET -> t("SMALL BOUNCE BOOST")
        BallPower.MINOR_SURGE -> t("10% PULL AND 15% RECHARGE BOOST")
    }.uppercase()

    private fun handleCollectionTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutCollection()
                collectionTouchY = event.y
                collectionLastY = event.y
                collectionDragging = false
                activeCollectionIndex = when {
                    collectionBackButton.contains(event.x, event.y) -> COLLECTION_BACK_INDEX
                    collectionRestoreButton.contains(event.x, event.y) -> COLLECTION_RESTORE_INDEX
                    collectionFilterAt(event.x, event.y) >= 0 -> collectionFilterActiveIndex(collectionFilterAt(event.x, event.y))
                    else -> collectionItemAt(event.x, event.y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - collectionLastY
                if (kotlin.math.abs(event.y - collectionTouchY) > dp(5f)) {
                    collectionDragging = true
                    activeCollectionIndex = -1
                }
                collectionScroll = (collectionScroll - dy).coerceIn(0f, collectionMaxScroll)
                collectionLastY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val released = activeCollectionIndex
                activeCollectionIndex = -1
                if (!collectionDragging && released == COLLECTION_BACK_INDEX && collectionBackButton.contains(event.x, event.y)) {
                    screen = Screen.MENU
                    backgroundShader = null
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                if (!collectionDragging && released == COLLECTION_RESTORE_INDEX && collectionRestoreButton.contains(event.x, event.y)) {
                    collectionMessage = t("CHECKING GOOGLE PLAY PURCHASES")
                    collectionMessageTimer = 3.4f
                    audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
                    purchaseBridge.restore()
                    return
                }
                val releasedFilter = collectionFilterFromActiveIndex(released)
                if (!collectionDragging && releasedFilter >= 0 && collectionFilterAt(event.x, event.y) == releasedFilter) {
                    collectionFilter = CollectionFilter.entries[releasedFilter]
                    collectionScroll = 0f
                    collectionMessage = "${t("FILTER").uppercase()} / ${t(collectionFilter.labelKey).uppercase()}"
                    collectionMessageTimer = 1.7f
                    audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                if (!collectionDragging && released >= 0 && collectionItemAt(event.x, event.y) == released) {
                    handleSkinTap(ballSkins[released])
                }
            }
        }
    }

    private fun handleSkinTap(skin: BallSkin) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        collectionFocusSkinId = skin.id
        val skinIndex = ballSkins.indexOf(skin).coerceAtLeast(0)
        audio.playSelection(skinIndex)
        if (isSkinUnlocked(skin)) {
            selectedSkinId = skin.id
            collectionFocusSkinId = skin.id
            prefs.edit().putString(SELECTED_SKIN_KEY, skin.id).apply()
            collectionMessage = "${skin.name} ${t("IS NOW IN YOUR HEAD")} / ${t("AURA").uppercase()} ${brainballAura(skin)}"
            collectionMessageTimer = 2.4f
            return
        }

        if (skin.unlock.type == UnlockType.PREMIUM) {
            val productId = PremiumCatalog.skinToProductId[skin.id]
            if (productId != null) {
                collectionMessage = "${t("OPENING GOOGLE PLAY")} / ${skin.name}"
                collectionMessageTimer = 3.4f
                purchaseBridge.purchase(productId)
            }
            return
        }

        if (skin.unlock.type == UnlockType.HYPE_COST) {
            val price = skin.unlock.value.coerceAtLeast(0)
            val balance = hypeBalance()
            if (balance >= price) {
                spendHype(price)
                selectedSkinId = skin.id
                collectionFocusSkinId = skin.id
                prefs.edit()
                    .putBoolean(earnedSkinKey(skin.id), true)
                    .putString(SELECTED_SKIN_KEY, skin.id)
                    .apply()
                collectionMessage = "${t("UNLOCKED").uppercase()} ${skin.name} / -${formatHypeAmount(price)} ${t("HYPE").uppercase()}"
                collectionMessageTimer = 3.2f
                audio.playEvent(SoundEvent.UNLOCK, skinIndex)
                hapticSequence(
                    HapticFeedbackConstants.CONFIRM to 0L,
                    HapticFeedbackConstants.LONG_PRESS to 90L
                )
            } else {
                val missing = (price - balance).coerceAtLeast(0)
                collectionMessage = "${skin.name} ${t("WANTS MORE HYPE")} / ${formatHypeAmount(balance)} / ${formatHypeAmount(price)}  +${formatHypeAmount(missing)}"
                collectionMessageTimer = 3.4f
            }
            return
        }

        collectionMessage = "${skin.name} ${t("REFUSES YOU")} / ${unlockLongLabel(skin)}"
        collectionMessageTimer = 3.4f
    }

    private fun layoutCollection() {
        val size = dp(40f)
        val contentLeft = pageContentLeft()
        val contentRight = pageContentRight()
        collectionBackButton.set(contentRight - size, dp(22f), contentRight, dp(22f) + size)
        collectionRestoreButton.set(contentRight - dp(96f), dp(68f), contentRight, dp(88f))
        layoutCollectionFilters()

        val side = contentLeft
        val gap = dp(10f)
        val contentWidth = contentRight - contentLeft
        val columns = when {
            contentWidth >= dp(840f) -> 3
            contentWidth >= dp(560f) -> 2
            else -> 1
        }
        val itemWidth = (contentWidth - gap * (columns - 1)) / columns
        val itemHeight = dp(
            when (columns) {
                1 -> 98f
                2 -> 110f
                else -> 94f
            }
        )
        val top = collectionViewportTop() + dp(4f) - collectionScroll

        while (collectionItemRects.size < ballSkins.size) {
            collectionItemRects += RectF()
        }
        collectionItemRects.forEach { it.setEmpty() }
        val visibleIndexes = filteredCollectionIndexes()
        visibleIndexes.forEachIndexed { visibleIndex, skinIndex ->
            val col = visibleIndex % columns
            val row = visibleIndex / columns
            val left = side + col * (itemWidth + gap)
            val itemTop = top + row * (itemHeight + gap)
            val rect = collectionItemRects[skinIndex]
            rect.set(left, itemTop, left + itemWidth, itemTop + itemHeight)
        }
        while (collectionItemRects.size > ballSkins.size) {
            collectionItemRects.removeAt(collectionItemRects.lastIndex)
        }

        val rows = ((visibleIndexes.size + columns - 1) / columns).coerceAtLeast(1)
        val contentHeight = rows * itemHeight + (rows - 1) * gap + dp(8f)
        val viewportHeight = collectionViewportBottom() - collectionViewportTop()
        collectionMaxScroll = (contentHeight - viewportHeight).coerceAtLeast(0f)
        collectionScroll = collectionScroll.coerceIn(0f, collectionMaxScroll)
    }

    private fun layoutCollectionFilters() {
        val side = pageContentLeft()
        val contentWidth = pageContentWidth()
        val gap = dp(5f)
        val top = dp(192f)
        val height = dp(28f)
        val width = (contentWidth - gap * (CollectionFilter.entries.size - 1)) / CollectionFilter.entries.size
        CollectionFilter.entries.forEachIndexed { index, _ ->
            val left = side + index * (width + gap)
            collectionFilterRects[index].set(left, top, left + width, top + height)
        }
    }

    private fun collectionItemAt(x: Float, y: Float): Int {
        if (y < collectionViewportTop() || y > collectionViewportBottom()) return -1
        return collectionItemRects.indexOfFirst { it.contains(x, y) }
    }

    private fun collectionFilterAt(x: Float, y: Float): Int = collectionFilterRects.indexOfFirst { it.contains(x, y) }

    private fun collectionFilterActiveIndex(index: Int): Int = COLLECTION_FILTER_BASE_INDEX - index

    private fun collectionFilterFromActiveIndex(activeIndex: Int): Int {
        if (activeIndex > COLLECTION_FILTER_BASE_INDEX) return -1
        val index = COLLECTION_FILTER_BASE_INDEX - activeIndex
        return if (index in CollectionFilter.entries.indices) index else -1
    }

    private fun filteredCollectionIndexes(): List<Int> {
        return ballSkins.indices.filter { index -> collectionFilterMatches(collectionFilter, ballSkins[index]) }
    }

    private fun collectionFilterMatches(filter: CollectionFilter, skin: BallSkin): Boolean {
        return when (filter) {
            CollectionFilter.ALL -> true
            CollectionFilter.SUPERPOWER -> skin.power != BallPower.NONE
            CollectionFilter.HYPE -> skin.unlock.type == UnlockType.HYPE_COST
            CollectionFilter.PREMIUM -> skin.unlock.type == UnlockType.PREMIUM
            CollectionFilter.COSMETIC -> skin.power == BallPower.NONE && skin.unlock.type != UnlockType.PREMIUM
        }
    }

    private fun collectionViewportTop(): Float = dp(228f)

    private fun collectionViewportBottom(): Float = viewHeight - dp(86f)

    private fun drawLeaderboards(canvas: Canvas) {
        drawBackground(canvas)
        layoutLeaderboards()

        val left = pageContentLeft() + dp(4f)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(30f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("LEADERBOARDS").uppercase(), left, dp(56f), textPaint)
        textPaint.textSize = dp(10f)
        textPaint.color = if (leaderboardBridge.configured) 0xFF64E572.toInt() else 0xAAFFFFFF.toInt()
        canvas.drawText(t(if (leaderboardBridge.configured) "GOOGLE PLAY / NO POWERS" else "LOCAL RECORDS").uppercase(), left, dp(78f), textPaint)
        drawLeaderboardBackButton(canvas)

        val bandTop = dp(98f)
        val bandBottom = dp(158f)
        paint.style = Paint.Style.FILL
        paint.color = 0xA80B111A.toInt()
        canvas.drawRect(0f, bandTop, viewWidth.toFloat(), bandBottom, paint)
        paint.color = 0x331DE8C8
        canvas.drawRect(0f, bandBottom - dp(2f), viewWidth * 0.5f, bandBottom, paint)
        paint.color = 0x33FF4D8D
        canvas.drawRect(viewWidth * 0.5f, bandBottom - dp(2f), viewWidth.toFloat(), bandBottom, paint)

        drawLeaderboardStat(
            canvas,
            left = pageContentLeft() + dp(4f),
            label = t("HIGHEST LEVEL").uppercase(),
            value = "L${max(modeHighestLevel(GameMode.CLASSIC), modeHighestLevel(GameMode.CHAOS)).toString().padStart(2, '0')}",
            accent = 0xFF1DE8C8.toInt()
        )
        drawLeaderboardStat(
            canvas,
            left = pageContentLeft() + pageContentWidth() * 0.54f,
            label = t("LONGEST STREAK").uppercase(),
            value = "x${max(modeBestStreak(GameMode.CLASSIC), modeBestStreak(GameMode.CHAOS))}",
            accent = 0xFFFF4D8D.toInt()
        )

        LeaderboardBoard.entries.forEachIndexed { index, board ->
            drawLeaderboardItem(canvas, leaderboardItemRects[index], board, index)
        }

        val footerText = leaderboardMessage.ifBlank {
            t(if (leaderboardBridge.configured) "SELECT A BOARD" else "GLOBAL SYNC OFFLINE")
        }
        drawStatusMessage(canvas, footerText, if (leaderboardBridge.configured) 0xFF8AA6FF.toInt() else 0xFF6F7788.toInt(), leaderboardMessage.isNotBlank())
    }

    private fun drawStatusMessage(canvas: Canvas, message: String, accent: Int, transient: Boolean) {
        scratch.set(pageContentLeft(), viewHeight - dp(70f), pageContentRight(), viewHeight - dp(16f))
        paint.style = Paint.Style.FILL
        paint.color = 0xED080C13.toInt()
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.color = accent
        canvas.drawRect(scratch.left, scratch.top, scratch.left + dp(4f), scratch.bottom, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(accent, 135)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 235)
        canvas.drawCircle(scratch.left + dp(20f), scratch.centerY(), dp(if (transient) 5f else 3f), paint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8f)
        textPaint.color = 0x77FFFFFF
        canvas.drawText(t(if (transient) "STATUS UPDATE" else "NEXT SIGNAL").uppercase(), scratch.left + dp(36f), scratch.top + dp(18f), textPaint)
        textPaint.textSize = dp(10.5f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(message, scratch.width() - dp(54f)), scratch.left + dp(36f), scratch.top + dp(38f), textPaint)
    }

    private fun drawLeaderboardStat(canvas: Canvas, left: Float, label: String, value: String, accent: Int) {
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = dp(9f)
        textPaint.color = 0x99FFFFFF.toInt()
        canvas.drawText(t(label).uppercase(), left, dp(118f), textPaint)
        textPaint.textSize = dp(22f)
        textPaint.color = accent
        canvas.drawText(value, left, dp(146f), textPaint)
    }

    private fun drawLeaderboardBackButton(canvas: Canvas) {
        drawUiButtonFrame(
            canvas = canvas,
            rect = leaderboardBackButton,
            active = activeLeaderboardIndex == LEADERBOARD_BACK_INDEX,
            accent = 0xFF8AA6FF.toInt(),
            cornerDp = 99f
        )
        drawUiIconAsset(canvas, "ui_back", leaderboardBackButton, padDp = -1f, alpha = 245)
    }

    private fun drawLeaderboardItem(canvas: Canvas, rect: RectF, board: LeaderboardBoard, index: Int) {
        val classic = board == LeaderboardBoard.CLASSIC_LEVEL || board == LeaderboardBoard.CLASSIC_STREAK
        val levelBoard = board == LeaderboardBoard.CLASSIC_LEVEL || board == LeaderboardBoard.CHAOS_LEVEL
        val accent = if (classic) 0xFF1DE8C8.toInt() else 0xFFFF4D8D.toInt()
        val active = activeLeaderboardIndex == index
        val score = leaderboardScore(board)

        paint.style = Paint.Style.FILL
        paint.color = if (active) withAlpha(accent, 72) else 0xD5161D29.toInt()
        canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
        paint.color = withAlpha(accent, 28)
        canvas.drawRoundRect(rect.left, rect.top, rect.left + dp(6f), rect.bottom, dp(4f), dp(4f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (active) 2f else 1f)
        paint.color = withAlpha(accent, if (active) 230 else 130)
        canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)

        val badgeCx = rect.left + dp(34f)
        val badgeCy = rect.centerY()
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 42)
        canvas.drawCircle(badgeCx, badgeCy, dp(20f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = accent
        if (levelBoard) {
            canvas.drawLine(badgeCx - dp(8f), badgeCy + dp(7f), badgeCx - dp(2f), badgeCy, paint)
            canvas.drawLine(badgeCx - dp(2f), badgeCy, badgeCx + dp(3f), badgeCy + dp(4f), paint)
            canvas.drawLine(badgeCx + dp(3f), badgeCy + dp(4f), badgeCx + dp(10f), badgeCy - dp(9f), paint)
        } else {
            canvas.drawCircle(badgeCx, badgeCy, dp(10f), paint)
            canvas.drawLine(badgeCx, badgeCy - dp(10f), badgeCx, badgeCy + dp(10f), paint)
            canvas.drawLine(badgeCx - dp(7f), badgeCy - dp(6f), badgeCx + dp(7f), badgeCy + dp(6f), paint)
        }

        val textLeft = rect.left + dp(68f)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(accent, 220)
        canvas.drawText(t(if (classic) "CLASSIC" else "CHAOS").uppercase(), textLeft, rect.top + dp(21f), textPaint)
        textPaint.textSize = dp(15f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t(if (levelBoard) "HIGHEST LEVEL" else "LONGEST STREAK").uppercase(), textLeft, rect.top + dp(45f), textPaint)
        textPaint.textSize = dp(9f)
        textPaint.color = 0x99FFFFFF.toInt()
        canvas.drawText(t(if (leaderboardBridge.configured) "OPEN FAIR GLOBAL RANKING" else "PERSONAL BEST").uppercase(), textLeft, rect.top + dp(65f), textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = dp(25f)
        textPaint.color = accent
        val value = if (levelBoard) "L${score.toString().padStart(2, '0')}" else "x$score"
        canvas.drawText(value, rect.right - dp(24f), rect.centerY() + dp(9f), textPaint)
    }

    private fun handleLeaderboardTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutLeaderboards()
                activeLeaderboardIndex = when {
                    leaderboardBackButton.contains(event.x, event.y) -> LEADERBOARD_BACK_INDEX
                    else -> leaderboardItemRects.indexOfFirst { it.contains(event.x, event.y) }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val released = activeLeaderboardIndex
                activeLeaderboardIndex = -1
                if (released == LEADERBOARD_BACK_INDEX && leaderboardBackButton.contains(event.x, event.y)) {
                    screen = Screen.MENU
                    backgroundShader = null
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                val board = LeaderboardBoard.entries.getOrNull(released) ?: return
                if (!leaderboardItemRects[released].contains(event.x, event.y)) return
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                if (!leaderboardBridge.configured) {
                    leaderboardMessage = t("GLOBAL SYNC OFFLINE")
                    leaderboardMessageTimer = 2.8f
                    return
                }
                leaderboardMessage = t("OPENING GOOGLE PLAY")
                leaderboardMessageTimer = 3f
                leaderboardBridge.open(board) {
                    post {
                        synchronized(lock) {
                            leaderboardMessage = t("PLAY GAMES UNAVAILABLE")
                            leaderboardMessageTimer = 3.2f
                        }
                    }
                }
            }
        }
    }

    private fun drawLanguageSelector(canvas: Canvas) {
        drawBackground(canvas)
        layoutLanguageSelector()

        val left = pageContentLeft() + dp(4f)
        val selected = KavvoroI18n.selected(context)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(30f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("CHOOSE LANGUAGE").uppercase(), left, dp(56f), textPaint)
        textPaint.textSize = dp(10f)
        textPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawText(t("GAME TEXT + COLLECTION VOICE").uppercase(), left, dp(78f), textPaint)
        drawLanguageBackButton(canvas)

        val viewportTop = languageViewportTop()
        val viewportBottom = languageViewportBottom()
        canvas.save()
        canvas.clipRect(0f, viewportTop, viewWidth.toFloat(), viewportBottom)
        KavvoroLanguage.entries.forEachIndexed { index, language ->
            val rect = languageItemRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.bottom >= viewportTop && rect.top <= viewportBottom) {
                drawLanguageItem(canvas, rect, language, language == selected, activeLanguageIndex == index)
            }
        }
        canvas.restore()

        drawStatusMessage(
            canvas = canvas,
            message = "${t("CURRENT")}: ${KavvoroI18n.label(context, selected)}",
            accent = 0xFF45F2FF.toInt(),
            transient = false
        )
    }

    private fun drawLanguageBackButton(canvas: Canvas) {
        drawUiButtonFrame(
            canvas = canvas,
            rect = languageBackButton,
            active = activeLanguageIndex == LANGUAGE_BACK_INDEX,
            accent = 0xFF8AA6FF.toInt(),
            cornerDp = 99f
        )
        drawUiIconAsset(canvas, "ui_back", languageBackButton, padDp = -1f, alpha = 245)
    }

    private fun drawLanguageItem(canvas: Canvas, rect: RectF, language: KavvoroLanguage, selected: Boolean, active: Boolean) {
        val accent = if (language == KavvoroLanguage.SYSTEM) 0xFF8AA6FF.toInt() else 0xFF45F2FF.toInt()
        paint.style = Paint.Style.FILL
        paint.color = when {
            selected -> withAlpha(accent, 92)
            active -> withAlpha(accent, 52)
            else -> 0xD5161D29.toInt()
        }
        canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (selected || active) 1.7f else 0.8f)
        paint.color = if (selected) accent else 0x44FFFFFF
        canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(14f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(KavvoroI18n.label(context, language), rect.width() - dp(58f)), rect.left + dp(14f), rect.top + dp(27f), textPaint)
        textPaint.textSize = dp(9f)
        textPaint.color = if (selected) accent else 0x99FFFFFF.toInt()
        val meta = if (language == KavvoroLanguage.SYSTEM) {
            "${t("SYSTEM")} / ${KavvoroI18n.active(context).nativeName}"
        } else {
            language.shortCode
        }
        canvas.drawText(fitText(meta, rect.width() - dp(58f)), rect.left + dp(14f), rect.top + dp(44f), textPaint)

        if (selected) {
            paint.style = Paint.Style.FILL
            paint.color = accent
            canvas.drawCircle(rect.right - dp(22f), rect.centerY(), dp(6f), paint)
        }
    }

    private fun handleLanguageTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutLanguageSelector()
                languageTouchY = event.y
                languageLastY = event.y
                languageDragging = false
                activeLanguageIndex = when {
                    languageBackButton.contains(event.x, event.y) -> LANGUAGE_BACK_INDEX
                    else -> languageItemAt(event.x, event.y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - languageLastY
                if (kotlin.math.abs(event.y - languageTouchY) > dp(5f)) {
                    languageDragging = true
                    activeLanguageIndex = -1
                }
                languageScroll = (languageScroll - dy).coerceIn(0f, languageMaxScroll)
                languageLastY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val released = activeLanguageIndex
                activeLanguageIndex = -1
                if (!languageDragging && released == LANGUAGE_BACK_INDEX && languageBackButton.contains(event.x, event.y)) {
                    screen = Screen.MENU
                    backgroundShader = null
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                val language = KavvoroLanguage.entries.getOrNull(released) ?: return
                val rect = languageItemRects.getOrNull(released) ?: return
                if (languageDragging || !rect.contains(event.x, event.y)) return
                KavvoroI18n.setSelected(context, language)
                audio.setLanguageCode(KavvoroI18n.audioLanguageCode(context))
                performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
        }
    }

    private fun layoutLanguageSelector() {
        val backSize = dp(44f)
        val side = pageContentLeft()
        val contentRight = pageContentRight()
        languageBackButton.set(contentRight - backSize, dp(28f), contentRight, dp(28f) + backSize)
        val gap = dp(10f)
        val top = languageViewportTop() + dp(2f)
        val contentWidth = pageContentWidth()
        val columns = if (contentWidth < dp(430f)) 1 else 2
        val itemHeight = dp(58f)
        val itemWidth = (contentWidth - gap * (columns - 1)) / columns
        KavvoroLanguage.entries.forEachIndexed { index, _ ->
            val row = index / columns
            val column = index % columns
            val left = side + column * (itemWidth + gap)
            val itemTop = top + row * (itemHeight + gap) - languageScroll
            languageItemRects[index].set(left, itemTop, left + itemWidth, itemTop + itemHeight)
        }
        val rows = ((KavvoroLanguage.entries.size + columns - 1) / columns).coerceAtLeast(1)
        val contentHeight = rows * itemHeight + (rows - 1) * gap + dp(8f)
        val viewportHeight = languageViewportBottom() - languageViewportTop()
        languageMaxScroll = (contentHeight - viewportHeight).coerceAtLeast(0f)
        languageScroll = languageScroll.coerceIn(0f, languageMaxScroll)
    }

    private fun languageItemAt(x: Float, y: Float): Int {
        if (y < languageViewportTop() || y > languageViewportBottom()) return -1
        return languageItemRects.indexOfFirst { it.contains(x, y) }
    }

    private fun languageViewportTop(): Float = dp(102f)

    private fun languageViewportBottom(): Float = viewHeight - dp(86f)

    private fun layoutLeaderboards() {
        val backSize = dp(44f)
        val side = pageContentLeft()
        val contentRight = pageContentRight()
        leaderboardBackButton.set(contentRight - backSize, dp(28f), contentRight, dp(28f) + backSize)
        val top = dp(176f)
        val gap = dp(10f)
        val height = min(dp(88f), (viewHeight - top - dp(94f) - gap * 3f) / 4f)
        repeat(4) { index ->
            val itemTop = top + index * (height + gap)
            val rect = leaderboardItemRects.getOrNull(index) ?: RectF().also { leaderboardItemRects += it }
            rect.set(side, itemTop, contentRight, itemTop + height)
        }
        while (leaderboardItemRects.size > 4) {
            leaderboardItemRects.removeAt(leaderboardItemRects.lastIndex)
        }
    }

    private fun leaderboardScore(board: LeaderboardBoard): Int {
        return when (board) {
            LeaderboardBoard.CLASSIC_LEVEL -> prefs.getInt(fairHighestLevelKey(GameMode.CLASSIC), modeHighestLevel(GameMode.CLASSIC))
            LeaderboardBoard.CHAOS_LEVEL -> prefs.getInt(fairHighestLevelKey(GameMode.CHAOS), modeHighestLevel(GameMode.CHAOS))
            LeaderboardBoard.CLASSIC_STREAK -> prefs.getInt(fairBestStreakKey(GameMode.CLASSIC), modeBestStreak(GameMode.CLASSIC))
            LeaderboardBoard.CHAOS_STREAK -> prefs.getInt(fairBestStreakKey(GameMode.CHAOS), modeBestStreak(GameMode.CHAOS))
        }
    }

    private fun ensureFairLeaderboardSnapshot() {
        val editor = prefs.edit()
        GameMode.entries.forEach { mode ->
            if (!prefs.contains(fairHighestLevelKey(mode))) editor.putInt(fairHighestLevelKey(mode), modeHighestLevel(mode))
            if (!prefs.contains(fairBestStreakKey(mode))) editor.putInt(fairBestStreakKey(mode), modeBestStreak(mode))
        }
        editor.apply()
    }

    private fun syncLeaderboards() {
        if (!leaderboardBridge.configured) return
        LeaderboardBoard.entries.forEach { board ->
            leaderboardBridge.submitScore(board, leaderboardScore(board).toLong())
        }
    }

    private fun drawPulseZones(canvas: Canvas) {
        level.pulseZones.forEachIndexed { index, zone ->
            val rich = richEffects()
            val lite = performanceLite()
            val cx = sx(zone.center.x)
            val cy = sy(zone.center.y)
            val radius = worldToScreen(zone.radius)
            val wave = 0.45f + 0.55f * sin(stateElapsed * 2.6f + zone.phase + index)
            val ballInside = state == GameState.SIMULATING && ball.distanceTo(zone.center) < zone.radius
            val heat = if (ballInside) 1f else (0.35f + wave * 0.38f)

            paint.style = Paint.Style.FILL
            paint.color = withAlpha(level.accent, (54f + heat * 78f).roundToInt())
            if (rich) {
                paint.maskFilter = BlurMaskFilter(radius * 0.34f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(cx, cy, radius * (1.02f + wave * 0.1f), paint)
            paint.maskFilter = null

            paint.style = Paint.Style.FILL
            if (rich) {
                paint.shader = LinearGradient(
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    intArrayOf(
                        withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), (58f + heat * 52f).roundToInt()),
                        withAlpha(level.accent, (24f + heat * 44f).roundToInt()),
                        0x00000000
                    ),
                    floatArrayOf(0f, 0.48f, 1f),
                    Shader.TileMode.CLAMP
                )
            } else {
                paint.color = withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), (34f + heat * 42f).roundToInt())
            }
            canvas.drawCircle(cx, cy, radius * 1.04f, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            val ringCount = when {
                lite -> 1
                rich -> 4
                else -> 2
            }
            repeat(ringCount) { ring ->
                val raw = stateElapsed * (0.62f + ring * 0.08f) + ring * 0.24f + zone.phase * 0.13f
                val progress = ((raw % 1f) + 1f) % 1f
                paint.strokeWidth = dp(2.4f + heat * 2.6f - ring * 0.28f)
                paint.color = withAlpha(
                    if (ring % 2 == 0) level.accent else 0xFFFFCF4A.toInt(),
                    ((1f - progress) * (95f + heat * 88f)).roundToInt()
                )
                canvas.drawCircle(cx, cy, radius * (0.34f + progress * 0.92f), paint)
            }

            val particleCount = when {
                lite -> 0
                fullEffects() -> 10
                rich -> 6
                else -> 3
            }
            repeat(particleCount) { particle ->
                val angle = stateElapsed * (if (zone.swirlForce >= 0f) 2.9f else -2.9f) + particle * PI.toFloat() * 2f / particleCount + zone.phase
                val orbit = radius * (0.57f + 0.2f * sin(stateElapsed * 2.1f + particle))
                val px = cx + cos(angle) * orbit
                val py = cy + sin(angle) * orbit
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(if (particle % 2 == 0) 0xFFFFCF4A.toInt() else level.accent, (118f + heat * 95f).roundToInt())
                canvas.drawCircle(px, py, dp(2.2f + heat * 1.8f), paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(1.2f + heat)
                paint.color = withAlpha(level.accent, (70f + heat * 90f).roundToInt())
                canvas.drawLine(px, py, px - cos(angle) * dp(10f + heat * 8f), py - sin(angle) * dp(10f + heat * 8f), paint)
            }

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1.8f + wave * 1.4f)
            paint.color = withAlpha(level.accent, 120 + (heat * 95).roundToInt())
            canvas.drawCircle(cx, cy, radius * (0.86f + wave * 0.12f), paint)
            paint.strokeWidth = dp(1.4f)
            paint.color = 0x72FFFFFF
            canvas.drawCircle(cx, cy, radius, paint)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(level.accent, 32 + (heat * 38f).roundToInt())
            canvas.drawCircle(cx, cy, radius * 0.88f, paint)
            val reactorRadius = radius * (0.55f + heat * 0.08f)
            scratch.set(cx - reactorRadius, cy - reactorRadius, cx + reactorRadius, cy + reactorRadius)
            drawWorldAsset(canvas, if (zone.radialForce >= 0f) "reactor_out" else "reactor_in", scratch, if (ballInside) 255 else 245)
            drawPulseIndicator(canvas, zone, cx, cy, radius, wave, rich)

            if (ballInside || level.index <= 10) {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
                textPaint.textSize = dp(if (ballInside) 10f else 8f)
                textPaint.color = withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), if (ballInside) 245 else 170)
                canvas.drawText(t(if (zone.radialForce >= 0f) "BOOST FIELD" else "VORTEX FIELD").uppercase(), cx, cy + radius + dp(18f), textPaint)
            }
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    private fun drawPulseIndicator(canvas: Canvas, zone: PulseZone, cx: Float, cy: Float, radius: Float, wave: Float, rich: Boolean) {
        val sign = if (zone.radialForce >= 0f) 1f else -1f
        val baseAngle = stateElapsed * 1.35f + zone.phase
        val arrowDistance = radius * (0.42f + wave * 0.13f)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(3.4f)
        paint.color = 0xF2FFFFFF.toInt()
        val arrowCount = when {
            performanceLite() -> 2
            rich -> 5
            else -> 3
        }
        repeat(arrowCount) { i ->
            val angle = baseAngle + i * PI.toFloat() * 2f / arrowCount
            val inner = arrowDistance * if (sign > 0f) 0.55f else 1.0f
            val outer = arrowDistance * if (sign > 0f) 1.12f else 0.42f
            val x1 = cx + cos(angle) * inner
            val y1 = cy + sin(angle) * inner
            val x2 = cx + cos(angle) * outer
            val y2 = cy + sin(angle) * outer
            paint.strokeWidth = dp(7f)
            paint.color = withAlpha(level.accent, 54)
            canvas.drawLine(x1, y1, x2, y2, paint)
            paint.strokeWidth = dp(3.2f)
            paint.color = 0xF2FFFFFF.toInt()
            canvas.drawLine(x1, y1, x2, y2, paint)
            drawArrowHead(canvas, x2, y2, angle)
        }

        if (rich && kotlin.math.abs(zone.swirlForce) > 0.4f) {
            paint.strokeWidth = dp(4.2f)
            paint.color = withAlpha(level.accent, 235)
            val arcRadius = radius * 0.55f
            scratch.set(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius)
            val sweep = if (zone.swirlForce > 0f) 142f else -142f
            val start = (stateElapsed * 92f + zone.phase * 30f) % 360f
            canvas.drawArc(scratch, start, sweep, false, paint)
            val endAngle = (start + sweep) * PI.toFloat() / 180f
            drawArrowHead(canvas, cx + cos(endAngle) * arcRadius, cy + sin(endAngle) * arcRadius, endAngle + if (sweep > 0f) PI.toFloat() * 0.52f else -PI.toFloat() * 0.52f)
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawPortals(canvas: Canvas) {
        if (level.portals.isEmpty()) return
        level.portals.forEach { portal ->
            val ex = sx(portal.entry.x)
            val ey = sy(portal.entry.y)
            val ox = sx(portal.exit.x)
            val oy = sy(portal.exit.y)
            val radius = worldToScreen(portal.radius)

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            if (!performanceLite()) {
                paint.strokeWidth = dp(11f)
                paint.color = withAlpha(0xFF45F2FF.toInt(), 32)
                canvas.drawLine(ex, ey, ox, oy, paint)
            }
            paint.strokeWidth = dp(2.2f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 140)
            canvas.drawLine(ex, ey, ox, oy, paint)
            paint.strokeCap = Paint.Cap.BUTT

            drawPortalNode(canvas, portal, ex, ey, radius, "IN", 0xFF45F2FF.toInt(), true)
            drawPortalNode(canvas, portal, ox, oy, radius, "OUT", 0xFFFFCF4A.toInt(), false)
        }
    }

    private fun drawPortalNode(
        canvas: Canvas,
        portal: PortalPair,
        cx: Float,
        cy: Float,
        radius: Float,
        label: String,
        accent: Int,
        entry: Boolean
    ) {
        val rich = richEffects()
        val spin = stateElapsed * (if (entry) 92f else -72f) + portal.phase * 45f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 140)
        if (rich) {
            paint.maskFilter = BlurMaskFilter(radius * 0.62f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(cx, cy, radius * 1.42f, paint)
        paint.maskFilter = null

        val assetRadius = radius * 1.05f
        scratch.set(cx - assetRadius, cy - assetRadius, cx + assetRadius, cy + assetRadius)
        canvas.save()
        canvas.rotate(spin, cx, cy)
        drawWorldAsset(canvas, "portal_goal", scratch, 245)
        canvas.restore()

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(
            when {
                performanceLite() -> 0
                rich -> 3
                else -> 1
            }
        ) { ring ->
            val progress = ((stateElapsed * (0.7f + ring * 0.09f) + ring * 0.31f + portal.phase) % 1f + 1f) % 1f
            paint.strokeWidth = dp(2.4f - ring * 0.25f)
            paint.color = withAlpha(accent, ((1f - progress) * 190f).roundToInt())
            canvas.drawCircle(cx, cy, radius * (0.62f + progress * 0.88f), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(accent, 245)
        canvas.drawText(label, cx, cy + radius + dp(18f), textPaint)
    }

    private fun drawArrowHead(canvas: Canvas, x: Float, y: Float, angle: Float) {
        val size = dp(6f)
        paint.style = Paint.Style.FILL
        path.reset()
        path.moveTo(x + cos(angle) * size, y + sin(angle) * size)
        path.lineTo(x + cos(angle + 2.45f) * size, y + sin(angle + 2.45f) * size)
        path.lineTo(x + cos(angle - 2.45f) * size, y + sin(angle - 2.45f) * size)
        path.close()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawBlocks(canvas: Canvas) {
        for (block in level.blocks) {
            val cx = sx(block.center.x)
            val cy = sy(block.center.y)
            val hw = worldToScreen(block.width * 0.5f)
            val hh = worldToScreen(block.height * 0.5f)
            canvas.save()
            canvas.rotate((block.angleRadians * 180f / PI.toFloat()), cx, cy)
            scratch.set(cx - hw, cy - hh, cx + hw, cy + hh)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(level.accent, 46)
            canvas.drawRoundRect(cx - hw * 1.03f, cy - hh * 1.45f, cx + hw * 1.03f, cy + hh * 1.45f, dp(5f), dp(5f), paint)
            drawWorldAsset(canvas, if (gameMode == GameMode.CHAOS) "platform_chaos" else "platform_classic", scratch)
            paint.color = withAlpha(block.tone, 34)
            canvas.drawRoundRect(scratch, dp(5f), dp(5f), paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1.2f)
            paint.color = 0x44FFFFFF
            canvas.drawRoundRect(scratch, dp(5f), dp(5f), paint)
            canvas.restore()
        }
    }

    private fun drawGoal(canvas: Canvas) {
        val cx = sx(level.goal.x)
        val cy = sy(level.goal.y)
        val radius = worldToScreen(level.goalRadius)
        val pulse = 0.9f + 0.1f * sin(stateElapsed * 5.2f)

        val portalRadius = radius * (1.12f + pulse * 0.06f)
        scratch.set(cx - portalRadius, cy - portalRadius, cx + portalRadius, cy + portalRadius)
        drawWorldAsset(canvas, "portal_goal", scratch)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.4f)
        paint.color = 0xAA64E572.toInt()
        canvas.drawCircle(cx, cy, radius * pulse, paint)
        paint.strokeWidth = dp(1.6f)
        paint.color = 0xCCFFFFFF.toInt()
        canvas.drawCircle(cx, cy, radius * 0.56f, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0x2264E572
        canvas.drawCircle(cx, cy, radius * 1.4f, paint)
    }

    private fun drawHazards(canvas: Canvas) {
        val hazardTime = if (state == GameState.READY) 0f else simElapsed
        for (hazard in level.hazards) {
            drawHazardTrack(canvas, hazard)
            val position = hazard.positionAt(hazardTime)
            val cx = sx(position.x)
            val cy = sy(position.y)
            val r = worldToScreen(hazard.radius)
            paint.style = Paint.Style.FILL
            paint.color = 0x88FF4D8D.toInt()
            if (richEffects()) {
                paint.maskFilter = BlurMaskFilter(r * 0.6f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(cx, cy, r * 1.18f, paint)
            paint.maskFilter = null
            val hazardKey = when (hazard.motion) {
                HazardMotion.STATIC -> "hazard_static"
                HazardMotion.HORIZONTAL, HazardMotion.VERTICAL -> "hazard_glitch"
                HazardMotion.ORBIT, HazardMotion.FIGURE_EIGHT -> "hazard_void"
            }
            val assetRadius = r * 1.3f
            scratch.set(cx - assetRadius, cy - assetRadius, cx + assetRadius, cy + assetRadius)
            val saveCount = canvas.save()
            canvas.rotate(stateElapsed * if (hazard.isMoving) 42f else 24f, cx, cy)
            drawWorldAsset(canvas, hazardKey, scratch)
            canvas.restoreToCount(saveCount)
        }
    }

    private fun drawHazardTrack(canvas: Canvas, hazard: Hazard) {
        if (!hazard.isMoving) return
        val cx = sx(hazard.center.x)
        val cy = sy(hazard.center.y)
        val travel = worldToScreen(hazard.travel)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = 0x55FF4D8D
        when (hazard.motion) {
            HazardMotion.HORIZONTAL -> canvas.drawLine(cx - travel, cy, cx + travel, cy, paint)
            HazardMotion.VERTICAL -> canvas.drawLine(cx, cy - travel, cx, cy + travel, paint)
            HazardMotion.ORBIT -> canvas.drawCircle(cx, cy, travel, paint)
            HazardMotion.FIGURE_EIGHT -> {
                path.reset()
                val segments = if (performanceLite()) 16 else 32
                repeat(segments + 1) { i ->
                    val t = i * PI.toFloat() * 2f / segments
                    val x = cx + sin(t) * travel
                    val y = cy + sin(t * 2f) * travel * 0.5f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
            }
            HazardMotion.STATIC -> Unit
        }
        paint.style = Paint.Style.FILL
        paint.color = 0x99FF4D8D.toInt()
        canvas.drawCircle(cx, cy, dp(2.2f), paint)
    }

    private fun drawHazardSpikes(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.5f)
        paint.color = 0xCC07090F.toInt()
        repeat(8) { i ->
            val angle = i * PI.toFloat() / 4f + stateElapsed * 0.7f
            val inner = r * 0.35f
            val outer = r * 0.72f
            canvas.drawLine(
                cx + cos(angle) * inner,
                cy + sin(angle) * inner,
                cx + cos(angle) * outer,
                cy + sin(angle) * outer,
                paint
            )
        }
    }

    private fun drawRouteCoach(canvas: Canvas) {
        if (state != GameState.READY) return
        if ((level.index > 10 && level.portals.isEmpty()) || level.tutorialHint.isBlank() || stateElapsed > 7.2f) return

        val fade = if (stateElapsed < 5.8f) 1f else (1f - (stateElapsed - 5.8f) / 1.4f).coerceIn(0f, 1f)
        val accent = selectedBallSkin().lineColor
        val start = level.start
        val goal = level.goal
        val orbit = menuPulse * 1.65f
        val portal = level.portals.firstOrNull()
        val pulseTarget = if (portal == null) level.pulseZones.firstOrNull()?.center else null
        val anchor = tutorialAnchorPoint(start, goal, pulseTarget, portal, orbit)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(12f)
        paint.color = withAlpha(accent, (fade * 38f).roundToInt())
        path.reset()
        path.moveTo(sx(start.x), sy(start.y))
        if (portal != null) {
            path.quadTo(sx(anchor.x), sy(anchor.y), sx(portal.entry.x), sy(portal.entry.y))
            path.moveTo(sx(portal.exit.x), sy(portal.exit.y))
            path.quadTo(
                sx((portal.exit.x + goal.x) * 0.5f),
                sy((portal.exit.y + goal.y) * 0.5f - 0.45f),
                sx(goal.x),
                sy(goal.y)
            )
        } else {
            path.quadTo(sx(anchor.x), sy(anchor.y), sx(goal.x), sy(goal.y))
        }
        canvas.drawPath(path, paint)
        paint.strokeWidth = dp(3f)
        paint.color = withAlpha(accent, (fade * 185f).roundToInt())
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT

        val pulse = 0.65f + 0.35f * sin(menuPulse * 4.4f)
        drawCoachHalo(canvas, sx(start.x), sy(start.y), dp(20f + pulse * 5f), accent, fade)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = withAlpha(accent, (fade * 180f).roundToInt())
        canvas.drawCircle(sx(anchor.x), sy(anchor.y), dp(15f + pulse * 8f), paint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, (fade * 110f).roundToInt())
        canvas.drawCircle(sx(anchor.x), sy(anchor.y), dp(5f), paint)
        val startTagX = sx(start.x)
        val startTagY = sy(start.y) - dp(34f)
        val actionTagX = sx(anchor.x)
        val actionTagY = sy(anchor.y) + dp(34f)
        val tagDx = actionTagX - startTagX
        val tagDy = actionTagY - startTagY
        if (!isCompactHud() && tagDx * tagDx + tagDy * tagDy > dp(98f) * dp(98f)) {
            drawCoachTag(canvas, startTagX, startTagY, t("START").uppercase(), accent, fade)
        }
        drawCoachTag(canvas, actionTagX, actionTagY, tutorialActionLabel(), accent, fade)

        pulseTarget?.let { target ->
            drawCoachHalo(canvas, sx(target.x), sy(target.y), worldToScreen(level.pulseZones.first().radius * 0.64f), 0xFFFFCF4A.toInt(), fade)
            drawCoachTag(canvas, sx(target.x), sy(target.y) - dp(42f), t("BOOST").uppercase(), 0xFFFFCF4A.toInt(), fade)
        }

        portal?.let { activePortal ->
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp(2.4f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), (fade * 170f).roundToInt())
            canvas.drawLine(sx(activePortal.entry.x), sy(activePortal.entry.y), sx(activePortal.exit.x), sy(activePortal.exit.y), paint)
            paint.strokeCap = Paint.Cap.BUTT
            drawCoachHalo(canvas, sx(activePortal.entry.x), sy(activePortal.entry.y), worldToScreen(activePortal.radius * 1.62f), 0xFF45F2FF.toInt(), fade)
            drawCoachTag(canvas, sx(activePortal.entry.x), sy(activePortal.entry.y) - dp(42f), t("PORTAL IN").uppercase(), 0xFF45F2FF.toInt(), fade)
            drawCoachHalo(canvas, sx(activePortal.exit.x), sy(activePortal.exit.y), worldToScreen(activePortal.radius * 1.62f), 0xFFFFCF4A.toInt(), fade)
            drawCoachTag(canvas, sx(activePortal.exit.x), sy(activePortal.exit.y) - dp(42f), t("PORTAL OUT").uppercase(), 0xFFFFCF4A.toInt(), fade)
        }

        val exitTagX = sx(goal.x)
        val exitTagY = sy(goal.y) - dp(42f)
        level.hazards.firstOrNull()?.let { hazard ->
            val position = hazard.positionAt(0f)
            val avoidTagX = sx(position.x)
            val avoidTagY = sy(position.y) + dp(38f)
            drawCoachHalo(canvas, sx(position.x), sy(position.y), worldToScreen(hazard.radius * 2.05f), 0xFFFF4D8D.toInt(), fade)
            val dx = avoidTagX - exitTagX
            val dy = avoidTagY - exitTagY
            if (dx * dx + dy * dy > dp(92f) * dp(92f)) {
                drawCoachTag(canvas, avoidTagX, avoidTagY, t("AVOID").uppercase(), 0xFFFF4D8D.toInt(), fade)
            }
        }

        level.blocks.firstOrNull()?.let { block ->
            drawCoachBlockFrame(canvas, block, fade)
            drawCoachTag(canvas, sx(block.center.x), sy(block.center.y) - dp(34f), t("BOUNCE WALL").uppercase(), 0xFF8AA6FF.toInt(), fade)
        }

        paint.style = Paint.Style.STROKE
        paint.color = withAlpha(0xFFFFFFFF.toInt(), (fade * 145f).roundToInt())
        canvas.drawCircle(sx(goal.x), sy(goal.y), worldToScreen(level.goalRadius * (0.75f + pulse * 0.12f)), paint)
        drawCoachTag(canvas, exitTagX, exitTagY, t("EXIT").uppercase(), 0xFF64E572.toInt(), fade)
    }

    private fun tutorialAnchorPoint(start: Point2, goal: Point2, pulseTarget: Point2?, portal: PortalPair?, orbit: Float): Point2 {
        val base = when {
            portal != null -> Point2(
                x = start.x * 0.35f + portal.entry.x * 0.65f,
                y = start.y * 0.35f + portal.entry.y * 0.65f
            )

            pulseTarget != null -> Point2(
                x = start.x * 0.45f + pulseTarget.x * 0.55f,
                y = start.y * 0.35f + pulseTarget.y * 0.65f
            )

            levelHasCurse(CurseType.FOCUS_FIELD) -> Point2(
                x = start.x * 0.55f + goal.x * 0.45f,
                y = start.y * 0.55f + goal.y * 0.45f
            )

            levelHasCurse(CurseType.RIFT_WIND) -> Point2(start.x + 1.75f, start.y + 0.8f)
            else -> Point2(start.x + 1.25f, start.y + 1.15f)
        }
        return Point2(
            x = (base.x + cos(orbit) * 0.32f).coerceIn(0.8f, STAGE_WIDTH - 0.8f),
            y = (base.y + sin(orbit) * 0.28f).coerceIn(1.2f, level.stageHeight - 1.2f)
        )
    }

    private fun tutorialActionLabel(): String {
        return t(
            TutorialCopy.actionLabelKey(
                hasOverheat = levelHasCurse(CurseType.OVERHEAT),
                hasPowerTap = levelHasCurse(CurseType.POWER_HOLD),
                hasFocusField = levelHasCurse(CurseType.FOCUS_FIELD),
                hasRiftDrain = levelHasCurse(CurseType.RIFT_DRAIN)
            )
        ).uppercase()
    }

    private fun drawCoachHalo(canvas: Canvas, cx: Float, cy: Float, radius: Float, accent: Int, alpha: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = withAlpha(accent, (160 * alpha).roundToInt())
        canvas.drawCircle(cx, cy, radius, paint)
        paint.strokeWidth = dp(7f)
        paint.color = withAlpha(accent, (36 * alpha).roundToInt())
        canvas.drawCircle(cx, cy, radius * 1.08f, paint)
    }

    private fun drawCoachBlockFrame(canvas: Canvas, block: com.moonsolstudios.kavvoro.engine.Block, alpha: Float) {
        val cx = sx(block.center.x)
        val cy = sy(block.center.y)
        val hw = worldToScreen(block.width * 0.5f)
        val hh = worldToScreen(block.height * 0.5f)
        canvas.save()
        canvas.rotate(block.angleRadians * 180f / PI.toFloat(), cx, cy)
        scratch.set(cx - hw * 1.12f, cy - hh * 2.7f, cx + hw * 1.12f, cy + hh * 2.7f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.2f)
        paint.color = withAlpha(0xFF8AA6FF.toInt(), (170 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
        paint.strokeWidth = dp(7f)
        paint.color = withAlpha(0xFF8AA6FF.toInt(), (30 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        canvas.restore()
    }

    private fun drawCoachTag(canvas: Canvas, cx: Float, cy: Float, label: String, accent: Int, alpha: Float) {
        val width = max(dp(52f), textPaint.apply { textSize = dp(9f) }.measureText(label) + dp(22f))
        val height = dp(23f)
        val minLeft = dp(8f)
        val maxLeft = max(minLeft, viewWidth - width - dp(8f))
        val reservedTop = when {
            state == GameState.READY && level.tutorialHint.isNotBlank() && stateElapsed <= 3.8f -> gameplayOverlayTop() + dp(86f)
            state == GameState.READY && level.tutorialHint.isNotBlank() -> gameplayOverlayTop() + dp(38f)
            else -> dp(72f)
        }
        val reservedBottom = if (state == GameState.READY && level.tutorialHint.isNotBlank()) {
            viewHeight - dp(138f) - dp(50f)
        } else {
            viewHeight - dp(24f)
        }
        val maxTop = max(reservedTop, reservedBottom - height)
        scratch.set(
            (cx - width * 0.5f).coerceIn(minLeft, maxLeft),
            (cy - height * 0.5f).coerceIn(reservedTop, maxTop),
            0f,
            0f
        )
        scratch.right = scratch.left + width
        scratch.bottom = scratch.top + height
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (218 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = withAlpha(accent, (190 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (238 * alpha).roundToInt())
        canvas.drawText(label, scratch.centerX(), scratch.centerY() + dp(3.5f), textPaint)
    }

    private fun drawReplayTail(canvas: Canvas) {
        if (replayFrames.size < 2 || state == GameState.SIMULATING) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = dp(3f)
        path.reset()
        val startIndex = (replayFrames.size - if (performanceLite()) 54 else 120).coerceAtLeast(0)
        for (index in startIndex until replayFrames.size) {
            val frame = replayFrames[index]
            val p = frame.ball
            if (index == startIndex) path.moveTo(sx(p.x), sy(p.y)) else path.lineTo(sx(p.x), sy(p.y))
        }
        paint.color = withAlpha(selectedBallSkin().lineColor, 92)
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawRiftTrail(canvas: Canvas) {
        if (playerLine.isEmpty()) return
        paint.style = Paint.Style.FILL
        val startIndex = (playerLine.size - if (performanceLite()) 42 else 100).coerceAtLeast(0)
        val visibleSize = playerLine.size - startIndex
        val trailStep = if (performanceLite()) 4 else 2
        for (index in startIndex until playerLine.size) {
            val localIndex = index - startIndex
            if (localIndex % trailStep != 0) continue
            val point = playerLine[index]
            val progress = localIndex / visibleSize.toFloat()
            paint.color = withAlpha(selectedBallSkin().lineColor, (18f + progress * 92f).roundToInt())
            canvas.drawCircle(sx(point.x), sy(point.y), dp(1.2f + progress * 2.2f), paint)
        }
    }

    private fun drawDrawingAssist(canvas: Canvas) {
        if (state != GameState.SIMULATING || !riftActive) return
        val anchor = riftAnchor ?: return
        val cx = sx(anchor.x)
        val cy = sy(anchor.y)
        val ballX = sx(ball.x)
        val ballY = sy(ball.y)
        val skin = selectedBallSkin()
        val danger = riftEnergy < 0.22f
        val accent = if (danger) 0xFFFF5757.toInt() else skin.lineColor
        val pulse = 0.65f + 0.35f * sin(stateElapsed * 8.5f)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp(15f)
        paint.color = withAlpha(accent, 42)
        canvas.drawLine(ballX, ballY, cx, cy, paint)
        paint.strokeWidth = dp(5f)
        paint.color = withAlpha(accent, 210)
        canvas.drawLine(ballX, ballY, cx, cy, paint)
        paint.strokeWidth = dp(1.2f)
        paint.color = 0xDDF7F4FF.toInt()
        canvas.drawLine(ballX, ballY, cx, cy, paint)

        if (levelHasCurse(CurseType.FOCUS_FIELD)) {
            paint.strokeWidth = dp(2.4f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 150)
            canvas.drawCircle(ballX, ballY, dp(28f + pulse * 5f), paint)
            paint.strokeWidth = dp(9f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 28)
            canvas.drawCircle(ballX, ballY, dp(28f + pulse * 5f), paint)
        }

        paint.strokeWidth = dp(2.2f)
        paint.color = withAlpha(accent, 190)
        val powerGrowth = if (levelHasCurse(CurseType.POWER_HOLD) || levelHasCurse(CurseType.OVERHEAT)) {
            min(1f, riftHoldSeconds / 0.9f) * 9f
        } else {
            0f
        }
        canvas.drawCircle(cx, cy, dp(13f + pulse * 6f + powerGrowth), paint)
        paint.strokeWidth = dp(1.1f)
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 130)
        canvas.drawCircle(cx, cy, dp(4.5f), paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        repeat(if (performanceLite()) 0 else 6) { i ->
            val angle = stateElapsed * 5.2f + i * PI.toFloat() * 2f / 6f
            val distance = dp(15f + (i % 3) * 4f) * (0.75f + pulse * 0.25f)
            paint.color = withAlpha(if (i % 2 == 0) accent else 0xFFFFCF4A.toInt(), 120)
            canvas.drawCircle(cx + cos(angle) * distance, cy + sin(angle) * distance, dp(1.7f), paint)
        }

        val energy = (riftEnergy * 100f).roundToInt().coerceIn(0, 100)
        val holdMode = when {
            levelHasCurse(CurseType.FOCUS_FIELD) -> t("FOCUS").uppercase()
            levelHasCurse(CurseType.POWER_HOLD) -> "POWER ${(min(1f, riftHoldSeconds / 0.9f) * 100).roundToInt()}%"
            levelHasCurse(CurseType.OVERHEAT) -> "HEAT ${(min(1f, riftHoldSeconds / 1.0f) * 100).roundToInt()}%"
            levelHasCurse(CurseType.RIFT_WIND) -> t("WIND GUARD").uppercase()
            levelHasCurse(CurseType.PULSE_STORM) -> t("PULSE GUARD").uppercase()
            level.index <= 3 && simElapsed < 4.2f -> t("TAP TO PULL").uppercase()
            else -> ""
        }
        val label = if (holdMode.isBlank()) "${t("RIFT").uppercase()} $energy%" else "${t("RIFT").uppercase()} $energy%   $holdMode"
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(10f)
        val chipWidth = (textPaint.measureText(label) + dp(24f)).coerceIn(dp(94f), dp(194f))
        val chipHeight = dp(30f)
        val chipLeft = (cx - chipWidth * 0.5f).coerceIn(dp(12f), viewWidth - dp(12f) - chipWidth)
        val chipTop = (cy - dp(58f)).coerceIn(dp(112f), viewHeight - dp(104f))
        scratch.set(chipLeft, chipTop, chipLeft + chipWidth, chipTop + chipHeight)
        paint.style = Paint.Style.FILL
        paint.color = 0xE607090F.toInt()
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.color = withAlpha(accent, 58)
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, scratch.width() - dp(12f)), scratch.centerX(), scratch.centerY() + dp(3.6f), textPaint)
    }

    private fun drawBall(canvas: Canvas) {
        val cx = sx(ball.x)
        val cy = sy(ball.y)
        val skin = selectedBallSkin()
        val r = worldToScreen(PhysicsEngine.BALL_RADIUS)
        val visualRadius = r * gameplayBallScale(skin)
        drawGameplayBallTrail(canvas, skin, visualRadius)
        drawGameplayBallAura(canvas, cx, cy, visualRadius, skin)
        if (state == GameState.SIMULATING && pulseIntensity > 0.34f) {
            val alpha = (pulseIntensity.coerceIn(0f, 1f) * 210f).roundToInt()
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp(3.4f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), alpha)
            canvas.drawCircle(cx, cy, visualRadius * (2.0f + pulseIntensity * 0.75f), paint)
            paint.strokeWidth = dp(1.5f)
            paint.color = withAlpha(level.accent, alpha)
            canvas.drawCircle(cx, cy, visualRadius * (1.38f + pulseIntensity * 0.55f), paint)
            paint.strokeCap = Paint.Cap.BUTT
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            textPaint.textSize = dp(8f)
            textPaint.color = withAlpha(0xFFFFCF4A.toInt(), alpha)
            canvas.drawText(t(if (levelHasCurse(CurseType.PULSE_STORM)) "STORM" else "BOOST").uppercase(), cx, cy - visualRadius * 2.25f, textPaint)
        }
        drawBallSkin(canvas, cx, cy, visualRadius, skin, animated = true, locked = false)
        drawGameplayPowerBadge(canvas, cx, cy, visualRadius, skin)
        drawBrainballLiveTag(canvas, cx, cy, visualRadius, skin)
    }

    private fun gameplayBallScale(skin: BallSkin): Float {
        return when {
            skin.unlock.type == UnlockType.PREMIUM -> 1.34f
            skin.power != BallPower.NONE -> 1.29f
            ballSkins.indexOfFirst { it.id == skin.id } >= 37 -> 1.25f
            else -> 1.2f
        }
    }

    private fun drawGameplayBallTrail(canvas: Canvas, skin: BallSkin, radius: Float) {
        if (liveBallTrail.size < 2) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        val startIndex = (liveBallTrail.size - 18).coerceAtLeast(0)
        for (index in startIndex until liveBallTrail.lastIndex) {
            val a = liveBallTrail[index]
            val b = liveBallTrail[index + 1]
            val progress = (index - startIndex + 1).toFloat() / (liveBallTrail.size - startIndex).coerceAtLeast(1)
            val powerAlpha = if (skin.power == BallPower.NONE) 0.72f else 1f
            paint.strokeWidth = radius * (0.12f + progress * 0.32f)
            paint.color = withAlpha(
                if (index % 2 == 0) skin.lineColor else skin.secondary,
                (progress * 145f * powerAlpha).roundToInt()
            )
            canvas.drawLine(sx(a.x), sy(a.y), sx(b.x), sy(b.y), paint)
        }
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawGameplayBallAura(canvas: Canvas, cx: Float, cy: Float, radius: Float, skin: BallSkin) {
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val powered = skin.power != BallPower.NONE
        val late = ballSkins.indexOfFirst { it.id == skin.id } >= 37
        val rich = richEffects()
        val baseOrbitCount = when {
            premium -> 4
            powered || late -> 3
            else -> 1
        }
        val orbitCount = if (rich) baseOrbitCount else min(baseOrbitCount, 1)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(skin.lineColor, if (premium || powered) 150 else 92)
        if (rich) {
            paint.maskFilter = BlurMaskFilter(radius * 0.82f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(cx, cy, radius * if (premium) 1.82f else 1.58f, paint)
        paint.maskFilter = null

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(orbitCount) { ring ->
            val orbit = menuPulse * (1.25f + ring * 0.18f) + ring * PI.toFloat() * 0.42f
            val rx = radius * (1.15f + ring * 0.16f)
            val ry = radius * (0.72f + ring * 0.1f)
            paint.strokeWidth = radius * (0.045f + ring * 0.008f)
            paint.color = withAlpha(if (ring % 2 == 0) skin.secondary else skin.lineColor, 150 - ring * 20)
            scratch.set(cx - rx, cy - ry, cx + rx, cy + ry)
            canvas.save()
            canvas.rotate((orbit * 180f / PI.toFloat()) % 360f, cx, cy)
            canvas.drawOval(scratch, paint)
            canvas.restore()
        }
        paint.strokeCap = Paint.Cap.BUTT

        if ((powered || premium || late) && !performanceLite() && adaptiveQuality >= 0.58f) {
            paint.style = Paint.Style.FILL
            val particles = when {
                fullEffects() && premium -> 8
                rich -> 5
                else -> 3
            }
            repeat(particles) { index ->
                val angle = menuPulse * (2.2f + index * 0.06f) + index * PI.toFloat() * 2f / particles
                val distance = radius * (1.48f + (index % 3) * 0.18f)
                paint.color = withAlpha(if (index % 2 == 0) skin.lineColor else 0xFFFFCF4A.toInt(), 185)
                canvas.drawCircle(
                    cx + cos(angle) * distance,
                    cy + sin(angle) * distance,
                    radius * if (premium) 0.08f else 0.058f,
                    paint
                )
            }
        }
    }

    private fun drawGameplayPowerBadge(canvas: Canvas, cx: Float, cy: Float, radius: Float, skin: BallSkin) {
        if (skin.power == BallPower.NONE) return
        val badge = radius * 0.58f
        val angle = menuPulse * 2.35f
        val bx = cx + cos(angle) * radius * 1.18f
        val by = cy + sin(angle) * radius * 1.18f
        scratch.set(bx - badge * 0.5f, by - badge * 0.5f, bx + badge * 0.5f, by + badge * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 195)
        canvas.drawCircle(bx, by, badge * 0.56f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(dp(1f), radius * 0.035f)
        paint.color = withAlpha(skin.lineColor, 230)
        canvas.drawCircle(bx, by, badge * 0.56f, paint)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratch, 235)
    }

    private fun drawBrainballLiveTag(canvas: Canvas, cx: Float, cy: Float, radius: Float, skin: BallSkin) {
        if (state != GameState.SIMULATING) return
        if (performanceLite() && chainCount < 5) return
        val label = when {
            chainCount >= 5 -> "${skin.name} ${t("CHAIN").uppercase()} x$chainCount"
            chainCount >= 3 -> t("CHAIN SPIKE").uppercase()
            pulseIntensity >= 0.62f -> t("BOOST FIELD").uppercase()
            else -> return
        }
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8.5f)
        val width = (textPaint.measureText(label) + dp(18f)).coerceIn(dp(72f), dp(190f))
        val height = dp(21f)
        val left = (cx - width * 0.5f).coerceIn(dp(8f), viewWidth - width - dp(8f))
        val top = (cy - radius * 1.95f - height).coerceIn(dp(138f), viewHeight - dp(72f))
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 205)
        canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.9f)
        paint.color = withAlpha(skin.lineColor, 190)
        canvas.drawRoundRect(scratch, dp(6f), dp(6f), paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, width - dp(10f)), scratch.centerX(), scratch.centerY() + dp(3.2f), textPaint)
    }

    private fun drawHud(canvas: Canvas) {
        layoutButtons()
        val compactHud = isCompactHud()
        val top = dp(if (compactHud) 8f else 12f)
        val left = dp(if (compactHud) 12f else 16f)
        val controlsLeft = hudControlsLeft()
        val hasRibbon = hudHasRibbon()
        val toolbarBottom = gameplayHudBottom()
        paint.style = Paint.Style.FILL
        paint.color = 0xEA070B12.toInt()
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), toolbarBottom, paint)
        paint.shader = LinearGradient(
            0f,
            0f,
            viewWidth.toFloat(),
            0f,
            intArrayOf(withAlpha(level.accent, 34), 0x00070B12),
            floatArrayOf(0f, 0.72f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), toolbarBottom, paint)
        paint.shader = null
        paint.color = withAlpha(level.accent, 150)
        canvas.drawRect(0f, toolbarBottom - dp(2f), viewWidth * riftEnergy, toolbarBottom, paint)

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        val metaWidth = (controlsLeft - left - dp(10f)).coerceAtLeast(dp(72f))
        if (!compactHud) {
            textPaint.textSize = dp(9f)
            textPaint.color = withAlpha(level.accent, 235)
            val meta = "${gameMode.menuTitle()}  /  D${level.difficultyRating}"
            canvas.drawText(fitText(meta, metaWidth), left, top + dp(8f), textPaint)

            textPaint.color = 0xFFF7F4FF.toInt()
            textPaint.textSize = dp(20f)
            val compactTitle = "L${level.index.toString().padStart(2, '0')}"
            canvas.drawText(compactTitle, left, top + dp(32f), textPaint)
        }

        val timeRemaining = (level.timeLimitSeconds - simElapsed).coerceAtLeast(0f)
        val hudHype = currentHudHypeScore()
        drawHudControlsDock(canvas)
        if (compactHud) {
            val statsLeft = left
            val statsWidth = (controlsLeft - statsLeft - dp(8f)).coerceAtLeast(dp(82f))
            drawCompactHudStats(canvas, statsLeft, top + dp(2f), statsWidth, timeRemaining, hudHype)
        } else {
            drawHudMetric(canvas, left, top + dp(39f), dp(66f), t("TIME").uppercase(), "${((timeRemaining * 10f).roundToInt() / 10f)}s", if (timeRemaining < 3f) 0xFFFF5757.toInt() else level.accent)
            drawHudMetric(canvas, left + dp(72f), top + dp(39f), dp(67f), t("CHAIN").uppercase(), if (chainCount > 0) "x$chainCount" else "-", 0xFFFFCF4A.toInt())
            drawHudMetric(canvas, left + dp(145f), top + dp(39f), dp(72f), t("HYPE").uppercase(), formatHypeAmount(hudHype), 0xFFFFCF4A.toInt())
        }
        if (!compactHud && hasRibbon) {
            drawCurseRibbon(canvas, left, top + dp(68f), (controlsLeft - left - dp(8f)).coerceAtLeast(dp(120f)))
        }
        if (compactHud && hasRibbon) {
            drawCurseRibbon(canvas, left, top + dp(40f), (controlsLeft - left - dp(8f)).coerceAtLeast(dp(120f)))
        }
        val energyWidth = min(viewWidth - dp(if (compactHud) 88f else 42f), dp(if (compactHud) 230f else 320f))
        val energyTop = top + dp(if (compactHud && hasRibbon) 78f else if (compactHud) 54f else 114f)
        drawRiftEnergyBar(canvas, (viewWidth - energyWidth) * 0.5f, energyTop, energyWidth, showLabel = !compactHud || !hasRibbon)
        drawIconButton(canvas, musicButton, ButtonId.MUSIC)
        drawIconButton(canvas, sfxButton, ButtonId.SFX)
        drawIconButton(canvas, homeButton, ButtonId.HOME)
        drawIconButton(canvas, restartButton, ButtonId.RESTART)
        if (!shareButton.isEmpty) {
            drawIconButton(canvas, shareButton, ButtonId.SHARE)
        }
        if (!nextButton.isEmpty) {
            drawIconButton(canvas, nextButton, ButtonId.NEXT)
        }
    }

    private fun drawCompactHudStats(canvas: Canvas, left: Float, top: Float, width: Float, timeRemaining: Float, hype: Int) {
        val height = dp(30f)
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(0x5A18202C, 0x2618202C),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.7f)
        paint.color = 0x2EFFFFFF
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        val splitOne = left + width * 0.34f
        val splitTwo = left + width * 0.62f
        paint.style = Paint.Style.FILL
        paint.color = 0x18FFFFFF
        canvas.drawRect(splitOne, top + dp(5f), splitOne + dp(1f), top + height - dp(5f), paint)
        canvas.drawRect(splitTwo, top + dp(5f), splitTwo + dp(1f), top + height - dp(5f), paint)

        val timeColor = if (timeRemaining < 3f) 0xFFFF5757.toInt() else level.accent
        val timeValue = "${((timeRemaining * 10f).roundToInt() / 10f)}s"
        val chainValue = if (chainCount > 0) "x$chainCount" else "-"
        val hypeValue = formatHypeAmount(hype)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(6.6f)
        textPaint.color = 0x82FFFFFF.toInt()
        canvas.drawText(t("TIME").uppercase(), left + dp(7f), top + dp(9f), textPaint)
        canvas.drawText(t("CHAIN").uppercase(), splitOne + dp(7f), top + dp(9f), textPaint)
        canvas.drawText(t("HYPE").uppercase(), splitTwo + dp(7f), top + dp(9f), textPaint)
        textPaint.textSize = dp(10.6f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(timeValue, splitOne - left - dp(12f)), left + dp(7f), top + dp(23f), textPaint)
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(fitText(chainValue, splitTwo - splitOne - dp(12f)), splitOne + dp(7f), top + dp(23f), textPaint)
        textPaint.color = 0xFFFFD75C.toInt()
        canvas.drawText(fitText(hypeValue, left + width - splitTwo - dp(12f)), splitTwo + dp(7f), top + dp(23f), textPaint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(timeColor, 185)
        canvas.drawCircle(left + width - dp(7f), top + dp(8f), dp(2.1f), paint)
    }

    private fun drawHudControlsDock(canvas: Canvas) {
        val buttons = listOf(musicButton, sfxButton, restartButton, homeButton).filter { !it.isEmpty }
        if (buttons.isEmpty()) return
        val left = buttons.minOf { it.left } - dp(4f)
        val top = buttons.minOf { it.top } - dp(4f)
        val right = buttons.maxOf { it.right } + dp(4f)
        val bottom = buttons.maxOf { it.bottom } + dp(4f)
        scratch.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            bottom,
            intArrayOf(0x5E16202C, withAlpha(level.accent, 34), 0x35101622),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(9f), dp(9f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(level.accent, 94)
        canvas.drawRoundRect(scratch, dp(9f), dp(9f), paint)
    }

    private fun isCompactHud(): Boolean = viewWidth < dp(520f)

    private fun hudHasRibbon(): Boolean = selectedBallSkin().power != BallPower.NONE || level.curses.isNotEmpty()

    private fun gameplayHudBottom(): Float {
        return dp(if (isCompactHud()) {
            if (hudHasRibbon()) 132f else 104f
        } else {
            156f
        })
    }

    private fun gameplayOverlayTop(): Float = gameplayHudBottom() + dp(8f)

    private fun hudControlsLeft(): Float {
        val buttons = mutableListOf(homeButton, restartButton, sfxButton, musicButton)
        return buttons.filter { !it.isEmpty }.minOfOrNull { it.left } ?: viewWidth.toFloat()
    }

    private fun drawLevelNameGlass(canvas: Canvas) {
        if (screen != Screen.GAME) return
        val stageRight = stageLeft + STAGE_WIDTH * scale
        val compactHud = isCompactHud()
        val width = (stageRight - stageLeft - dp(36f)).coerceIn(dp(188f), viewWidth - dp(44f))
        val left = ((stageLeft + stageRight) * 0.5f - width * 0.5f).coerceIn(dp(14f), viewWidth - width - dp(14f))
        val height = dp(if (compactHud) 22f else 28f)
        val top = (gameplayHudBottom() - height - dp(if (compactHud) 7f else 9f)).coerceAtLeast(dp(48f))
        val accent = levelArchetype().accent
        scratch.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(0xD6070B12.toInt(), withAlpha(accent, 52), 0xB0070B12.toInt()),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(left, top, left + dp(3f), top + height, dp(2f), dp(2f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(accent, 145)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(if (compactHud) 9.3f else 10.8f)
        textPaint.color = 0xEAF7F4FF.toInt()
        val title = "${gameMode.menuTitle()} - L${level.index.toString().padStart(2, '0')} ${localizedLevelTitle(level.title)}"
        drawFittedText(
            canvas,
            title,
            scratch.centerX(),
            scratch.centerY() + dp(if (compactHud) 3.3f else 3.8f),
            width - dp(24f),
            if (compactHud) 9.3f else 10.8f,
            if (compactHud) 7.2f else 8.2f
        )
    }

    private fun drawInkBar(canvas: Canvas, left: Float, top: Float, width: Float) {
        val accent = if (riftEnergy < 0.22f) 0xFFFF5757.toInt() else level.accent
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8f)
        textPaint.color = 0x88FFFFFF.toInt()
        scratch.set(left, top - dp(3f), left + dp(13f), top + dp(10f))
        drawWorldAsset(canvas, "boost_rift_pull", scratch, 220)
        canvas.drawText(t("RIFT").uppercase(), left + dp(15f), top + dp(7f), textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = withAlpha(accent, 235)
        canvas.drawText("${(riftEnergy * 100).roundToInt()}%", left + width, top + dp(7f), textPaint)
        val barTop = top + dp(13f)
        val gap = dp(2f)
        val segmentWidth = (width - gap * 9f) / 10f
        repeat(10) { index ->
            val segmentLeft = left + index * (segmentWidth + gap)
            scratch.set(segmentLeft, barTop, segmentLeft + segmentWidth, barTop + dp(5f))
            paint.style = Paint.Style.FILL
            paint.color = if (riftEnergy * 10f > index) withAlpha(accent, 230) else 0x22FFFFFF
            canvas.drawRoundRect(scratch, dp(1.5f), dp(1.5f), paint)
        }
    }

    private fun drawRiftEnergyBar(canvas: Canvas, left: Float, top: Float, width: Float, showLabel: Boolean = true) {
        val danger = riftEnergy < 0.22f
        val accent = if (danger) 0xFFFF5757.toInt() else level.accent
        val height = dp(13f)
        val radius = dp(5.5f)
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0x69141B27
        canvas.drawRoundRect(scratch, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = if (danger) withAlpha(accent, 210) else 0x55FFFFFF
        canvas.drawRoundRect(scratch, radius, radius, paint)

        val fillWidth = (width * riftEnergy.coerceIn(0f, 1f)).coerceAtLeast(if (riftEnergy > 0f) dp(5f) else 0f)
        if (fillWidth > 0f) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                left,
                top,
                left + width,
                top,
                intArrayOf(withAlpha(accent, 250), withAlpha(0xFFFFCF4A.toInt(), if (danger) 130 else 210)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            scratch2.set(left, top, left + fillWidth, top + height)
            canvas.drawRoundRect(scratch2, radius, radius, paint)
            paint.shader = null
        }

        val segments = 12
        val gap = dp(2.2f)
        val segmentWidth = (width - gap * (segments - 1)) / segments
        repeat(segments) { index ->
            val x = left + index * (segmentWidth + gap)
            scratch2.set(x, top + dp(2f), x + segmentWidth, top + height - dp(2f))
            paint.style = Paint.Style.FILL
            paint.color = if (riftEnergy * segments > index) 0x1FFFFFFF else 0x22000000
            canvas.drawRoundRect(scratch2, dp(2f), dp(2f), paint)
        }

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(7.4f)
        if (showLabel) {
            textPaint.color = 0xBBFFFFFF.toInt()
            canvas.drawText(t("RIFT ENERGY").uppercase(), left + dp(7f), top - dp(3f), textPaint)
        }
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = withAlpha(accent, 245)
        canvas.drawText("${(riftEnergy * 100).roundToInt()}%", left + width - dp(7f), top - dp(3f), textPaint)
    }

    private fun drawHudMetric(canvas: Canvas, left: Float, top: Float, width: Float, label: String, value: String, accent: Int) {
        paint.style = Paint.Style.FILL
        paint.color = 0x5018202C
        canvas.drawRoundRect(left, top, left + width, top + dp(28f), dp(5f), dp(5f), paint)
        paint.color = withAlpha(accent, 210)
        canvas.drawRect(left, top, left + dp(2f), top + dp(28f), paint)
        val iconKey = when (label) {
            "TIME" -> "boost_recharge"
            "CHAIN" -> "boost_chain"
            else -> "boost_prism"
        }
        scratch.set(left + width - dp(18f), top + dp(3f), left + width - dp(4f), top + dp(17f))
        drawWorldAsset(canvas, iconKey, scratch, 155)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(7.5f)
        textPaint.color = 0x77FFFFFF
        canvas.drawText(label, left + dp(7f), top + dp(10f), textPaint)
        textPaint.textSize = dp(11f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, width - dp(12f)), left + dp(7f), top + dp(23f), textPaint)
    }

    private fun drawCurseRibbon(canvas: Canvas, left: Float, top: Float, width: Float) {
        val power = selectedBallSkin().power
        var x = left
        val gap = dp(6f)
        val maxRight = left + width
        val chips = mutableListOf<Triple<String, Int, Boolean>>()
        if (power != BallPower.NONE) chips += Triple(ballPowerName(power), selectedBallSkin().lineColor, true)
        chips += level.curses.map { Triple(it.name.uppercase(), it.accent, false) }
        if (chips.isEmpty()) return
        val visible = chips.take(2)
        visible.forEach { (label, accent, powered) ->
            textPaint.textSize = dp(9f)
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            val shownLabel = if (powered && isCompactHud()) label else if (powered) "${t("POWER").uppercase()} $label" else label
            val iconReserve = if (powered) dp(22f) else 0f
            val chipWidth = (textPaint.measureText(shownLabel) + dp(18f) + iconReserve).coerceIn(
                dp(if (powered) 88f else 68f),
                dp(if (isCompactHud()) 132f else 156f)
            )
            if (x + chipWidth > maxRight) return@forEach
            scratch.set(x, top, x + chipWidth, top + dp(22f))
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (powered) 68 else 42)
            canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(if (powered) 1.5f else 1f)
            paint.color = withAlpha(accent, 190)
            canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
            if (powered) {
                val iconSize = dp(17f)
                scratch2.set(
                    scratch.left + dp(4f),
                    scratch.centerY() - iconSize * 0.5f,
                    scratch.left + dp(4f) + iconSize,
                    scratch.centerY() + iconSize * 0.5f
                )
                drawWorldAsset(canvas, powerIconKey(power), scratch2)
            }
            textPaint.color = 0xDDF7F4FF.toInt()
            if (powered) {
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(fitText(shownLabel, scratch.width() - dp(30f)), scratch.left + dp(25f), scratch.centerY() + dp(3.5f), textPaint)
            } else {
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(fitText(shownLabel, scratch.width() - dp(10f)), scratch.centerX(), scratch.centerY() + dp(3.5f), textPaint)
            }
            x += chipWidth + gap
        }

        val hidden = chips.size - visible.size
        if (hidden > 0 && x + dp(38f) <= maxRight) {
            scratch.set(x, top, x + dp(38f), top + dp(22f))
            paint.style = Paint.Style.FILL
            paint.color = 0x33454F65
            canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = dp(9f)
            textPaint.color = 0xCCFFFFFF.toInt()
            canvas.drawText("+$hidden", scratch.centerX(), scratch.centerY() + dp(3.5f), textPaint)
        }
    }

    private fun drawPowerToast(canvas: Canvas) {
        if (powerMessageTimer <= 0f || powerMessage.isBlank()) return
        val accent = selectedBallSkin().lineColor
        val width = min(viewWidth - dp(32f), dp(360f))
        val height = dp(48f)
        val left = viewWidth * 0.5f - width * 0.5f
        val top = if (state == GameState.READY && stateElapsed <= 3.6f) {
            gameplayOverlayTop() + dp(if (level.tutorialHint.isNotBlank()) 126f else 96f)
        } else {
            gameplayOverlayTop()
        }
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xF0080C13.toInt()
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.color = accent
        canvas.drawRect(left, top, left + dp(4f), top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        val powerIconSize = dp(34f)
        scratch.set(left + dp(10f), top + dp(7f), left + dp(10f) + powerIconSize, top + dp(7f) + powerIconSize)
        drawWorldAsset(canvas, powerIconKey(selectedBallSkin().power), scratch)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8f)
        textPaint.color = withAlpha(accent, 235)
        val power = selectedBallSkin().power
        val title = if (power != BallPower.NONE && powerMessage.startsWith(ballPowerName(power))) {
            t("SUPERPOWER ONLINE").uppercase()
        } else {
            t("SUPERPOWER TRIGGERED").uppercase()
        }
        canvas.drawText(title, left + dp(52f), top + dp(17f), textPaint)
        textPaint.textSize = dp(12f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(powerMessage, width - dp(68f)), left + dp(52f), top + dp(36f), textPaint)
    }

    private fun drawMissionBrief(canvas: Canvas) {
        if (state != GameState.READY || stateElapsed > 3.6f) return
        if (level.tutorialHint.isNotBlank()) return
        val warning = currentModeWarning()
        val accent = warning?.accent ?: level.accent
        val alpha = when {
            stateElapsed < 2.8f -> 1f
            else -> (1f - (stateElapsed - 2.8f) / 0.8f).coerceIn(0f, 1f)
        }
        val width = min(viewWidth - dp(32f), dp(430f))
        val height = dp(if (warning == null) 66f else 76f)
        val left = viewWidth * 0.5f - width * 0.5f
        val top = gameplayOverlayTop()
        scratch.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF080C13.toInt(), (232 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.color = withAlpha(accent, (220 * alpha).roundToInt())
        canvas.drawRect(left, top, left + dp(4f), top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(accent, (125 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        val iconX = left + dp(29f)
        val iconY = top + height * 0.5f
        val missionIconSize = dp(34f)
        scratch.set(iconX - missionIconSize * 0.5f, iconY - missionIconSize * 0.5f, iconX + missionIconSize * 0.5f, iconY + missionIconSize * 0.5f)
        drawWorldAsset(canvas, if (warning == null) "boost_rift_pull" else "danger_beacon", scratch, (255 * alpha).roundToInt())

        val textLeft = left + dp(54f)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(accent, (235 * alpha).roundToInt())
        canvas.drawText("${gameMode.menuTitle()}  /  ${t("LEVEL").uppercase()} ${level.index.toString().padStart(2, '0')}  /  D${level.difficultyRating}", textLeft, top + dp(19f), textPaint)
        textPaint.textSize = dp(16f)
        textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255 * alpha).roundToInt())
        canvas.drawText(fitText(localizedLevelTitle(level.title), width - dp(74f)), textLeft, top + dp(41f), textPaint)
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (165 * alpha).roundToInt())
        val archetype = levelArchetype()
        val detail = warning?.let { "${t(it.title.removeSuffix("!")).uppercase()}  /  ${t(it.subtitle).uppercase()}" }
            ?: selectedBallSkin().power.takeIf { it != BallPower.NONE }?.let { "${ballPowerName(it)}  /  ${ballPowerDescription(it)}" }
            ?: "${t(archetype.label).uppercase()}  /  ${t(archetype.detail).uppercase()}"
        canvas.drawText(fitText(detail, width - dp(74f)), textLeft, top + dp(59f), textPaint)

        repeat(3) { index ->
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (stateElapsed < index + 1f) (210 * alpha).roundToInt() else (45 * alpha).roundToInt())
            canvas.drawCircle(left + width - dp(16f + index * 8f), top + dp(14f), dp(2f), paint)
        }
    }

    private fun currentModeWarning(): ModeWarning? {
        return when {
            level.portals.isNotEmpty() -> ModeWarning(
                title = "PORTAL RIFT!",
                subtitle = "ENTER IN / EXIT OUT WITH EXTRA SPEED",
                accent = 0xFF45F2FF.toInt()
            )

            levelHasCurse(CurseType.RIFT_WIND) && levelHasCurse(CurseType.OVERHEAT) -> ModeWarning(
                title = "WIND + OVERHEAT!",
                subtitle = "TAP AGAINST WIND / ENERGY DRAINS FAST",
                accent = 0xFF8AA6FF.toInt()
            )

            levelHasCurse(CurseType.FOCUS_FIELD) && levelHasCurse(CurseType.HEAVY_CORE) -> ModeWarning(
                title = "FOCUS HEAVY!",
                subtitle = "TAP TO SLOW / GRAVITY IS HEAVY",
                accent = 0xFFFFCF4A.toInt()
            )

            levelHasCurse(CurseType.POWER_HOLD) && levelHasCurse(CurseType.MOON_GLIDE) -> ModeWarning(
                title = "POWER MOON!",
                subtitle = "POWER TAP / GLIDE AFTER BURST",
                accent = 0xFF45F2FF.toInt()
            )

            levelHasCurse(CurseType.RIFT_WIND) -> ModeWarning(
                title = "WIND GUARD!",
                subtitle = "TAP AGAINST THE GUST",
                accent = 0xFF8AA6FF.toInt()
            )

            levelHasCurse(CurseType.OVERHEAT) -> ModeWarning(
                title = "OVERHEAT!",
                subtitle = "POWER RISES / ENERGY MELTS FAST",
                accent = 0xFFFF5757.toInt()
            )

            levelHasCurse(CurseType.RIFT_DRAIN) -> ModeWarning(
                title = "RIFT DRAIN!",
                subtitle = "USE SHORT CONTROL BURSTS",
                accent = 0xFF64E572.toInt()
            )

            levelHasCurse(CurseType.PULSE_STORM) -> ModeWarning(
                title = "PULSE GUARD!",
                subtitle = "TAP TO DAMPEN PULSE FORCE",
                accent = 0xFFC15CFF.toInt()
            )

            levelHasCurse(CurseType.FOCUS_FIELD) -> ModeWarning(
                title = "FOCUS FIELD!",
                subtitle = "TAP TO SLOW FOR PRECISION",
                accent = 0xFFFFCF4A.toInt()
            )

            levelHasCurse(CurseType.POWER_HOLD) -> ModeWarning(
                title = "POWER TAP!",
                subtitle = "RAPID TAPS BUILD FORCE",
                accent = 0xFFFF4D8D.toInt()
            )

            levelHasCurse(CurseType.HEAVY_CORE) -> ModeWarning(
                title = "HEAVY CORE!",
                subtitle = "GRAVITY PULLS HARDER",
                accent = 0xFFFF8C42.toInt()
            )

            levelHasCurse(CurseType.MOON_GLIDE) -> ModeWarning(
                title = "MOON GLIDE!",
                subtitle = "RELEASE KEEPS MOMENTUM",
                accent = 0xFF45F2FF.toInt()
            )

            levelHasCurse(CurseType.TINY_GATE) -> ModeWarning(
                title = "TINY GATE!",
                subtitle = "THE EXIT WINDOW IS SMALLER",
                accent = 0xFFF7F4FF.toInt()
            )

            else -> null
        }
    }

    private fun drawLevelStartCard(canvas: Canvas) {
        if (state != GameState.READY) return
        if (stateElapsed > 2.6f && level.tutorialHint.isNotBlank()) return
        val width = min(viewWidth - dp(42f), dp(380f))
        val height = dp(82f)
        val left = viewWidth * 0.5f - width * 0.5f
        val top = dp(if (level.curses.isEmpty()) 118f else 184f)
        val alpha = if (stateElapsed < 2.2f) 1f else (1f - (stateElapsed - 2.2f) / 0.8f).coerceIn(0f, 1f)
        if (alpha <= 0f) return

        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (215 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = withAlpha(level.accent, (190 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)

        drawBallSkin(canvas, left + dp(34f), top + height * 0.5f, dp(18f), selectedBallSkin(), animated = true, locked = false)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(10f)
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (150 * alpha).roundToInt())
        canvas.drawText("${gameMode.menuTitle()} ${t("LEVEL").uppercase()} ${level.index.toString().padStart(2, '0')}", left + dp(62f), top + dp(24f), textPaint)
        textPaint.textSize = dp(17f)
        textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255 * alpha).roundToInt())
        canvas.drawText(fitText(localizedLevelTitle(level.title), width - dp(82f)), left + dp(62f), top + dp(48f), textPaint)
        textPaint.textSize = dp(10f)
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), (230 * alpha).roundToInt())
        val rewardLine = when {
            isTutorialLevel() && level.index < TUTORIAL_LAST_LEVEL -> t("NO ADS IN TRAINING").uppercase()
            isTutorialLevel() -> t("L10 UNLOCKS VORO GRAD").uppercase()
            else -> nextRewardText() ?: t("CHASE CLEAN RUNS").uppercase()
        }
        canvas.drawText(fitText(rewardLine, width - dp(82f)), left + dp(62f), top + dp(66f), textPaint)
    }

    private fun drawFinishBurst(canvas: Canvas) {
        if (state != GameState.WON && state != GameState.LOST) return
        val won = state == GameState.WON
        val progress = (stateElapsed / 1.2f).coerceIn(0f, 1f)
        val cx = viewWidth * 0.5f
        val cy = viewHeight * 0.39f
        drawFinishConfetti(canvas, won, cx, cy)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(if (won) 28 else 14) { i ->
            val angle = i * PI.toFloat() * 2f / if (won) 28f else 14f
            val base = dp(if (won) 42f else 26f)
            val spread = dp(if (won) 126f else 76f) * progress
            val inner = base + spread * 0.28f
            val outer = base + spread
            paint.strokeWidth = dp(if (won) 3.2f else 2.2f)
            paint.color = withAlpha(
                if (won) if (i % 3 == 0) level.accent else 0xFFFFCF4A.toInt() else 0xFFFF4D8D.toInt(),
                ((1f - progress) * 170f).roundToInt()
            )
            canvas.drawLine(
                cx + cos(angle) * inner,
                cy + sin(angle) * inner,
                cx + cos(angle) * outer,
                cy + sin(angle) * outer,
                paint
            )
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawRiftBreakMoment(canvas: Canvas) {
        if (!lastRiftBreak || state != GameState.WON || riftBreakTimer <= 0f) return
        val age = (2.15f - riftBreakTimer).coerceAtLeast(0f)
        val intro = (age / 0.32f).coerceIn(0f, 1f)
        val alpha = (1f - (age / 2.15f)).coerceIn(0f, 1f)
        val cx = sx(ball.x).coerceIn(dp(54f), viewWidth - dp(54f))
        val cy = sy(ball.y).coerceIn(dp(168f), viewHeight - dp(160f))
        val accent = selectedBallSkin().lineColor

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (72f * alpha).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(if (performanceLite()) 3 else 5) { index ->
            val wave = ((age * 1.45f + index * 0.18f) % 1f + 1f) % 1f
            paint.strokeWidth = dp(2.6f + index * 0.45f)
            paint.color = withAlpha(if (index % 2 == 0) accent else 0xFFFFCF4A.toInt(), ((1f - wave) * 230f * alpha).roundToInt())
            canvas.drawCircle(cx, cy, dp(34f + wave * (112f + index * 15f)), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        val panelWidth = min(viewWidth - dp(38f), dp(360f)) * intro
        val panelHeight = dp(78f)
        val left = viewWidth * 0.5f - panelWidth * 0.5f
        val top = (cy - dp(132f)).coerceIn(dp(138f), viewHeight - dp(260f))
        scratch.set(left, top, left + panelWidth, top + panelHeight)
        if (panelWidth > dp(80f)) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                scratch.left,
                scratch.top,
                scratch.right,
                scratch.bottom,
                intArrayOf(withAlpha(accent, (120f * alpha).roundToInt()), 0xF2070B12.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1.2f)
            paint.color = withAlpha(0xFFFFCF4A.toInt(), (235f * alpha).roundToInt())
            canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            textPaint.textSize = dp(26f)
            textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255f * alpha).roundToInt())
            canvas.drawText(t("RIFT BREAK").uppercase(), scratch.centerX(), top + dp(34f), textPaint)
            textPaint.textSize = dp(12f)
            textPaint.color = withAlpha(0xFFFFCF4A.toInt(), (245f * alpha).roundToInt())
            val subtitle = "${lastRiftBreakReason.ifBlank { t("CLEAN RIFT SNAP").uppercase() }}  /  +$lastRiftBreakBonus ${t("HYPE").uppercase()}"
            canvas.drawText(fitText(subtitle, panelWidth - dp(24f)), scratch.centerX(), top + dp(58f), textPaint)
        }
    }

    private fun drawFinishConfetti(canvas: Canvas, won: Boolean, cx: Float, cy: Float) {
        val alpha = ((finishPulse * if (won) 210f else 120f).roundToInt()).coerceIn(0, 220)
        if (alpha <= 0) return
        val count = if (won) 36 else 14
        val colors = intArrayOf(level.accent, selectedBallSkin().lineColor, 0xFFFFCF4A.toInt(), 0xFFF7F4FF.toInt())
        paint.style = Paint.Style.FILL
        repeat(count) { i ->
            val angle = i * PI.toFloat() * 2f / count + (level.seed % 19L).toFloat() * 0.017f
            val drift = stateElapsed.coerceAtMost(2.2f)
            val distance = dp(if (won) 54f else 32f) + dp(42f + (i % 6) * 13f) * drift
            val x = cx + cos(angle) * distance + sin(stateElapsed * 2.4f + i) * dp(10f)
            val y = cy + sin(angle) * distance + drift * drift * dp(if (won) 36f else 18f)
            val width = dp(if (won) 9f else 6f)
            val height = dp(if (won) 3.8f else 3f)
            canvas.save()
            canvas.rotate((angle * 180f / PI.toFloat()) + stateElapsed * 150f, x, y)
            scratch.set(x - width, y - height, x + width, y + height)
            paint.color = withAlpha(colors[i % colors.size], alpha)
            canvas.drawRoundRect(scratch, dp(2f), dp(2f), paint)
            canvas.restore()
        }
    }

    private fun drawTutorialHint(canvas: Canvas) {
        if (!tutorialCardVisible) {
            tutorialCardBounds.setEmpty()
            tutorialStartButton.setEmpty()
            return
        }

        val width = min(viewWidth - dp(36f), dp(430f))
        val height = dp(184f)
        val left = viewWidth * 0.5f - width * 0.5f
        val top = viewHeight - height - dp(34f)
        val accent = currentModeWarning()?.accent ?: level.accent
        val lessonLines = tutorialLessonLines() + tutorialObstacleLine()
        tutorialCardBounds.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xF407090F.toInt()
        canvas.drawRoundRect(tutorialCardBounds, dp(8f), dp(8f), paint)
        paint.shader = LinearGradient(left, top, left + width, top + height, intArrayOf(withAlpha(accent, 54), 0x0007090F), null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(tutorialCardBounds, dp(8f), dp(8f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(tutorialCardBounds, dp(8f), dp(8f), paint)

        val iconSize = dp(42f)
        scratch.set(left + dp(11f), top + dp(16f), left + dp(11f) + iconSize, top + dp(16f) + iconSize)
        drawWorldAsset(canvas, tutorialIconKey(), scratch, 235)

        val isArabic = KavvoroI18n.active(context) == KavvoroLanguage.AR
        val textX = if (isArabic) tutorialCardBounds.right - dp(14f) else left + dp(62f)
        val textMaxWidth = width - dp(76f)
        textPaint.textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(10f)
        textPaint.color = accent
        val tutorialHeader = if (level.index <= 10) {
            "${t("TRAINING").uppercase()} ${level.index}/10  /  ${tutorialActionLabel()}"
        } else {
            "${t("RIFT MODULE").uppercase()} L${level.index.toString().padStart(2, '0')}  /  ${t("PORTAL").uppercase()}"
        }
        drawFittedText(canvas, tutorialHeader, textX, top + dp(21f), textMaxWidth, 10f, 7.2f)

        textPaint.textSize = dp(11f)
        textPaint.color = 0xEFFFFFFF.toInt()
        lessonLines.take(4).forEachIndexed { index, line ->
            canvas.drawText(fitText(line, textMaxWidth), textX, top + dp(40f + index * 15f), textPaint)
        }

        paint.style = Paint.Style.FILL
        textPaint.textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8.2f)
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), 225)
        val footer = if (level.index < TUTORIAL_LAST_LEVEL) {
            "${t("NO ADS IN TRAINING").uppercase()}  /  ${t("L10 UNLOCKS VORO GRAD").uppercase()}"
        } else {
            t("TRAINING REWARD READY").uppercase()
        }
        canvas.drawText(fitText(footer, textMaxWidth), textX, top + dp(103f), textPaint)

        val actionBounds = TutorialCardLayout.centeredHorizontalBounds(
            cardLeft = tutorialCardBounds.left,
            cardRight = tutorialCardBounds.right,
            padding = dp(14f)
        )
        paint.style = Paint.Style.FILL
        paint.color = 0x22FFFFFF
        canvas.drawRoundRect(
            actionBounds.left,
            top + dp(116f),
            actionBounds.right,
            top + dp(117.5f),
            dp(1f),
            dp(1f),
            paint
        )

        tutorialStartButton.set(
            actionBounds.left,
            top + dp(126f),
            actionBounds.right,
            top + dp(170f)
        )
        drawTutorialStartButton(canvas, accent)
    }

    private fun drawTutorialStartButton(canvas: Canvas, accent: Int) {
        val active = tutorialInputGate.actionPressed
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            tutorialStartButton.left,
            tutorialStartButton.top,
            tutorialStartButton.right,
            tutorialStartButton.bottom,
            intArrayOf(
                withAlpha(accent, if (active) 255 else 232),
                withAlpha(accent, if (active) 180 else 132)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tutorialStartButton, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (active) 1.8f else 1.1f)
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (active) 235 else 175)
        canvas.drawRoundRect(tutorialStartButton, dp(7f), dp(7f), paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create(
            "sans",
            android.graphics.Typeface.BOLD
        )
        textPaint.color = 0xFFF7F4FF.toInt()
        drawFittedText(
            canvas,
            t("START LEVEL").uppercase(),
            tutorialStartButton.centerX(),
            tutorialStartButton.centerY() + dp(4f),
            tutorialStartButton.width() - dp(20f),
            12f,
            8f
        )
    }

    private fun tutorialLessonLines(): List<String> {
        return TutorialCopy.lessonKeys(
            levelIndex = level.index,
            hasPortals = level.portals.isNotEmpty()
        ).map(::t)
    }

    private fun tutorialObstacleLine(): String {
        return t(
            TutorialCopy.obstacleKey(
                hasPortals = level.portals.isNotEmpty(),
                hasHazards = level.hazards.isNotEmpty(),
                hasTinyGate = levelHasCurse(CurseType.TINY_GATE),
                hasPulseZones = level.pulseZones.isNotEmpty(),
                hasBlocks = level.blocks.isNotEmpty()
            )
        )
    }

    private fun tutorialIconKey(): String {
        return when {
            level.portals.isNotEmpty() -> "portal_goal"
            level.hazards.isNotEmpty() -> "hazard_glitch"
            levelHasCurse(CurseType.OVERHEAT) -> "danger_beacon"
            levelHasCurse(CurseType.PULSE_STORM) || level.pulseZones.isNotEmpty() -> "boost_pulse"
            levelHasCurse(CurseType.POWER_HOLD) -> "boost_plasma"
            levelHasCurse(CurseType.FOCUS_FIELD) -> "boost_recharge"
            else -> "boost_rift_pull"
        }
    }

    private fun drawTwoLineText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float) {
        if (textPaint.measureText(text) <= maxWidth) {
            canvas.drawText(text, x, y, textPaint)
            return
        }

        val words = text.split(" ")
        var first = ""
        var second = ""
        for (word in words) {
            val candidate = if (first.isEmpty()) word else "$first $word"
            if (second.isEmpty() && textPaint.measureText(candidate) <= maxWidth) {
                first = candidate
            } else {
                second = if (second.isEmpty()) word else "$second $word"
            }
        }
        canvas.drawText(first, x, y, textPaint)
        if (second.isNotEmpty()) {
            var line = second
            while (line.isNotEmpty() && textPaint.measureText(line) > maxWidth) {
                line = line.dropLast(1)
            }
            canvas.drawText(line, x, y + dp(16f), textPaint)
        }
    }

    private fun fitText(text: String, maxWidth: Float): String {
        if (textPaint.measureText(text) <= maxWidth) return text
        var clipped = text
        while (clipped.length > 3 && textPaint.measureText("$clipped...") > maxWidth) {
            clipped = clipped.dropLast(1)
        }
        return if (clipped.length <= 3) clipped else "$clipped..."
    }

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        startSizeDp: Float,
        minSizeDp: Float
    ) {
        textPaint.textSize = dp(startSizeDp)
        val minSize = dp(minSizeDp)
        while (textPaint.textSize > minSize && textPaint.measureText(text) > maxWidth) {
            textPaint.textSize -= dp(0.35f)
        }
        canvas.drawText(fitText(text, maxWidth), x, y, textPaint)
    }

    private fun drawIconButton(canvas: Canvas, rect: RectF, id: ButtonId) {
        if (rect.isEmpty) return
        val active = activeButton == id
        val accent = when (id) {
            ButtonId.HOME -> 0xFF45F2FF.toInt()
            ButtonId.RESTART -> 0xFFFF4D8D.toInt()
            ButtonId.SHARE -> 0xFFC15CFF.toInt()
            ButtonId.NEXT -> 0xFFFFCF4A.toInt()
            ButtonId.SFX -> 0xFF45F2FF.toInt()
            ButtonId.MUSIC -> 0xFFFFCF4A.toInt()
            else -> level.accent
        }
        drawUiButtonFrame(canvas, rect, active, accent, cornerDp = 7f)
        val iconKey = when (id) {
            ButtonId.HOME -> "ui_home"
            ButtonId.RESTART -> "ui_retry"
            ButtonId.SHARE -> "ui_share"
            ButtonId.NEXT -> "ui_next"
            ButtonId.SFX -> "ui_sound"
            ButtonId.MUSIC -> "ui_music"
            ButtonId.CONTINUE,
            ButtonId.AD_CONTINUE,
            ButtonId.NONE -> null
        }
        when (id) {
            ButtonId.SFX -> drawAudioIconAsset(canvas, rect, "ui_sound", muted = sfxMuted, active = active)
            ButtonId.MUSIC -> drawAudioIconAsset(canvas, rect, "ui_music", muted = musicMuted, active = active)
            else -> iconKey?.let {
                drawUiIconAsset(canvas, it, rect, padDp = -1f, alpha = if (active) 255 else 232)
            }
        }
    }

    private fun drawAudioToggleButton(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        muted: Boolean,
        music: Boolean,
        accent: Int
    ) {
        drawUiButtonFrame(canvas, rect, active, accent, cornerDp = 7f)
        drawAudioIconAsset(canvas, rect, if (music) "ui_music" else "ui_sound", muted, active)
    }

    private fun drawUiButtonFrame(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        accent: Int,
        cornerDp: Float
    ) {
        val corner = dp(cornerDp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                if (active) withAlpha(accent, 92) else 0xA00B101C.toInt(),
                0x7A141B27,
                if (active) withAlpha(accent, 42) else 0x52101822
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (active) 1.35f else 0.85f)
        paint.color = if (active) withAlpha(accent, 220) else 0x52FFFFFF
        canvas.drawRoundRect(rect, corner, corner, paint)

        paint.style = Paint.Style.FILL
        paint.color = if (active) withAlpha(0xFFF7F4FF.toInt(), 34) else 0x18FFFFFF
        canvas.drawRoundRect(
            rect.left + dp(3f),
            rect.top + dp(3f),
            rect.right - dp(3f),
            rect.top + rect.height() * 0.42f,
            max(dp(2f), corner - dp(3f)),
            max(dp(2f), corner - dp(3f)),
            paint
        )
    }

    private fun drawUiIconAsset(canvas: Canvas, key: String, rect: RectF, padDp: Float, alpha: Int) {
        val pad = dp(padDp)
        scratch.set(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)
        drawWorldAsset(canvas, key, scratch, alpha)
    }

    private fun drawAudioIconAsset(canvas: Canvas, rect: RectF, key: String, muted: Boolean, active: Boolean) {
        drawUiIconAsset(canvas, key, rect, padDp = -1f, alpha = if (muted) 155 else if (active) 255 else 232)
        if (muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp(2.4f)
            paint.color = 0xFFFF4D8D.toInt()
            canvas.drawLine(
                rect.left + rect.width() * 0.26f,
                rect.bottom - rect.height() * 0.25f,
                rect.right - rect.width() * 0.24f,
                rect.top + rect.height() * 0.24f,
                paint
            )
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    private fun drawAudioGlyph(canvas: Canvas, rect: RectF, music: Boolean, muted: Boolean, active: Boolean) {
        val alpha = if (muted) 145 else if (active) 255 else 232
        paint.color = withAlpha(0xFFF7F4FF.toInt(), alpha)
        if (music) {
            drawMusicNoteIcon(canvas, rect)
        } else {
            drawSpeakerIcon(canvas, rect, muted)
        }
        if (muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp(2.4f)
            paint.color = 0xFFFF4D8D.toInt()
            canvas.drawLine(
                rect.left + rect.width() * 0.28f,
                rect.bottom - rect.height() * 0.27f,
                rect.right - rect.width() * 0.24f,
                rect.top + rect.height() * 0.25f,
                paint
            )
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    private fun drawSpeakerIcon(canvas: Canvas, rect: RectF, muted: Boolean) {
        paint.style = Paint.Style.FILL
        path.reset()
        path.moveTo(rect.left + rect.width() * 0.25f, rect.top + rect.height() * 0.43f)
        path.lineTo(rect.left + rect.width() * 0.39f, rect.top + rect.height() * 0.43f)
        path.lineTo(rect.left + rect.width() * 0.56f, rect.top + rect.height() * 0.30f)
        path.lineTo(rect.left + rect.width() * 0.56f, rect.top + rect.height() * 0.70f)
        path.lineTo(rect.left + rect.width() * 0.39f, rect.top + rect.height() * 0.57f)
        path.lineTo(rect.left + rect.width() * 0.25f, rect.top + rect.height() * 0.57f)
        path.close()
        canvas.drawPath(path, paint)
        if (!muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp(1.9f)
            scratch.set(
                rect.left + rect.width() * 0.48f,
                rect.top + rect.height() * 0.34f,
                rect.left + rect.width() * 0.73f,
                rect.top + rect.height() * 0.66f
            )
            canvas.drawArc(scratch, -42f, 84f, false, paint)
            paint.strokeWidth = dp(1.45f)
            scratch.set(
                rect.left + rect.width() * 0.50f,
                rect.top + rect.height() * 0.24f,
                rect.left + rect.width() * 0.86f,
                rect.top + rect.height() * 0.76f
            )
            canvas.drawArc(scratch, -42f, 84f, false, paint)
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    private fun drawMusicNoteIcon(canvas: Canvas, rect: RectF) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = dp(2.4f)
        val stemX = rect.left + rect.width() * 0.61f
        val top = rect.top + rect.height() * 0.26f
        val bottom = rect.top + rect.height() * 0.62f
        canvas.drawLine(stemX, top, stemX, bottom, paint)
        canvas.drawLine(stemX, top, rect.left + rect.width() * 0.39f, rect.top + rect.height() * 0.32f, paint)
        paint.style = Paint.Style.FILL
        canvas.drawOval(
            rect.left + rect.width() * 0.31f,
            rect.top + rect.height() * 0.56f,
            rect.left + rect.width() * 0.53f,
            rect.top + rect.height() * 0.74f,
            paint
        )
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
    }

    private fun drawLegacyIconButton(canvas: Canvas, rect: RectF, id: ButtonId) {
        paint.style = Paint.Style.FILL
        paint.color = if (activeButton == id) withAlpha(level.accent, 88) else 0x7A141B27
        canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (activeButton == id) 1.6f else 0.8f)
        paint.color = if (activeButton == id) withAlpha(level.accent, 220) else 0x42FFFFFF
        canvas.drawRoundRect(rect, dp(7f), dp(7f), paint)
        when (id) {
            ButtonId.HOME -> drawHomeIcon(canvas, rect)
            ButtonId.RESTART -> drawRestartIcon(canvas, rect)
            ButtonId.SHARE -> drawShareIcon(canvas, rect)
            ButtonId.NEXT -> drawNextIcon(canvas, rect)
            ButtonId.SFX -> drawAudioGlyph(canvas, rect, music = false, muted = sfxMuted, active = activeButton == id)
            ButtonId.MUSIC -> drawAudioGlyph(canvas, rect, music = true, muted = musicMuted, active = activeButton == id)
            ButtonId.CONTINUE,
            ButtonId.AD_CONTINUE,
            ButtonId.NONE -> Unit
        }
    }

    private fun drawHomeIcon(canvas: Canvas, rect: RectF) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.4f)
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xFFF7F4FF.toInt()
        path.reset()
        path.moveTo(rect.left + rect.width() * 0.28f, rect.centerY())
        path.lineTo(rect.centerX(), rect.top + rect.height() * 0.28f)
        path.lineTo(rect.left + rect.width() * 0.72f, rect.centerY())
        path.lineTo(rect.left + rect.width() * 0.72f, rect.top + rect.height() * 0.72f)
        path.lineTo(rect.left + rect.width() * 0.34f, rect.top + rect.height() * 0.72f)
        path.lineTo(rect.left + rect.width() * 0.34f, rect.centerY())
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
    }

    private fun drawRestartIcon(canvas: Canvas, rect: RectF) {
        val pad = rect.width() * 0.27f
        scratch.set(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.4f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xFFF7F4FF.toInt()
        canvas.drawArc(scratch, 35f, 285f, false, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.style = Paint.Style.FILL
        val x = rect.left + rect.width() * 0.68f
        val y = rect.top + rect.height() * 0.22f
        path.reset()
        path.moveTo(x, y)
        path.lineTo(x + dp(1f), y + dp(9f))
        path.lineTo(x - dp(8f), y + dp(5f))
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawShareIcon(canvas: Canvas, rect: RectF) {
        val a = Point2(rect.left + rect.width() * 0.34f, rect.centerY())
        val b = Point2(rect.left + rect.width() * 0.66f, rect.top + rect.height() * 0.32f)
        val c = Point2(rect.left + rect.width() * 0.66f, rect.top + rect.height() * 0.68f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.1f)
        paint.color = 0xFFF7F4FF.toInt()
        canvas.drawLine(a.x, a.y, b.x, b.y, paint)
        canvas.drawLine(a.x, a.y, c.x, c.y, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(a.x, a.y, dp(3.4f), paint)
        canvas.drawCircle(b.x, b.y, dp(3.4f), paint)
        canvas.drawCircle(c.x, c.y, dp(3.4f), paint)
    }

    private fun drawNextIcon(canvas: Canvas, rect: RectF) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFFF7F4FF.toInt()
        path.reset()
        path.moveTo(rect.left + rect.width() * 0.38f, rect.top + rect.height() * 0.28f)
        path.lineTo(rect.left + rect.width() * 0.38f, rect.top + rect.height() * 0.72f)
        path.lineTo(rect.left + rect.width() * 0.72f, rect.centerY())
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawOutcome(canvas: Canvas) {
        if (state != GameState.WON && state != GameState.LOST) return
        val won = state == GameState.WON
        val accent = if (won) level.accent else 0xFFFF4D8D.toInt()
        val panelHeight = dp(if (won) 378f else 238f)
        val panelWidth = min(viewWidth - dp(32f), dp(540f))
        val left = (viewWidth - panelWidth) * 0.5f
        val right = left + panelWidth
        val bottom = viewHeight - dp(16f)
        val top = bottom - panelHeight

        paint.style = Paint.Style.FILL
        paint.color = 0x52000000
        canvas.drawRect(0f, gameplayHudBottom(), viewWidth.toFloat(), viewHeight.toFloat(), paint)
        scratch.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.9f)
        paint.color = withAlpha(accent, 150)
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(left, top, right, top + dp(4f), dp(3f), dp(3f), paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(accent, 235)
        canvas.drawText("${t(if (won) "RUN COMPLETE" else "RUN INTERRUPTED").uppercase()}  /  ${gameMode.menuTitle()}", left + dp(18f), top + dp(25f), textPaint)
        textPaint.textSize = dp(23f)
        textPaint.color = 0xFFF7F4FF.toInt()
        val title = if (won) localizedLevelTitle(level.title) else t("RIFT COLLAPSED").uppercase()
        canvas.drawText(fitText(title, right - left - dp(36f)), left + dp(18f), top + dp(51f), textPaint)
        drawBrainballResultLine(canvas, left + dp(18f), top + dp(66f), right - dp(18f), accent, won)

        if (won) {
            val score = lastScore
            drawResultSummaryRow(canvas, left + dp(18f), top + dp(102f), right - dp(18f), score, accent)

            if (lastRiftBreak) {
                drawRiftBreakResultBadge(canvas, left + dp(18f), top + dp(184f), right - dp(18f), accent)
            }
            val rewardTop = top + if (lastRiftBreak) dp(222f) else dp(196f)
            val reward = rewardMessage.ifBlank { nextRewardText() ?: t("ALL FREE REWARDS UNLOCKED").uppercase() }.replace(" | ", "  /  ")
            drawRewardSignalCard(canvas, left + dp(18f), rewardTop, right - dp(18f), reward, accent)
            drawResultActionButton(canvas, resultShareButton, t("SHARE SHORT").uppercase(), 0xFFFFCF4A.toInt(), ButtonId.SHARE)
            drawResultActionButton(canvas, resultNextButton, t("NEXT LEVEL").uppercase(), level.accent, ButtonId.NEXT)
        } else {
            val needsAd = continueRequiresAd()
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = dp(12f)
            textPaint.color = 0xBFFFFFFF.toInt()
            canvas.drawText(
                fitText(
                    if (needsAd) t("Keep streak %s with one ad.").replace("%s", "x$streak")
                    else t("Free recovery available. Streak %s stays active.").replace("%s", "x$streak"),
                    right - left - dp(36f)
                ),
                left + dp(18f),
                top + dp(106f),
                textPaint
            )
            paint.style = Paint.Style.FILL
            paint.color = 0x22FFFFFF
            canvas.drawRect(left + dp(18f), top + dp(126f), right - dp(18f), top + dp(127f), paint)
            textPaint.textSize = dp(9f)
            textPaint.color = 0x77FFFFFF
            canvas.drawText(t("RIFT ENERGY RESETS / LEVEL RESTARTS").uppercase(), left + dp(18f), top + dp(148f), textPaint)
            val label = if (continueRequiresAd()) t("WATCH AD").uppercase() else t("CONTINUE FREE").uppercase()
            drawResultActionButton(canvas, resultRetryButton, label, 0xFFFF4D8D.toInt(), ButtonId.CONTINUE)
        }
    }

    private fun drawResultSummaryRow(canvas: Canvas, left: Float, top: Float, right: Float, score: RunScore?, accent: Int) {
        val height = dp(68f)
        scratch.set(left, top, right, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            top + height,
            intArrayOf(withAlpha(accent, 28), 0xB50B1019.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = withAlpha(accent, 95)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        val rankWidth = dp(72f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 32)
        canvas.drawRoundRect(left, top, left + rankWidth, top + height, dp(7f), dp(7f), paint)
        paint.color = withAlpha(accent, 190)
        canvas.drawRect(left + rankWidth - dp(2f), top + dp(10f), left + rankWidth, top + height - dp(10f), paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8.5f)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("RANK").uppercase(), left + rankWidth * 0.5f, top + dp(16f), textPaint)
        textPaint.textSize = dp(48f)
        textPaint.color = accent
        canvas.drawText(score?.rank ?: "-", left + rankWidth * 0.5f, top + dp(58f), textPaint)

        val metricLeft = left + rankWidth + dp(10f)
        val metricWidth = (right - metricLeft) / 3f
        drawResultMetricCell(canvas, metricLeft, top, metricWidth, t("TIME").uppercase(), score?.let { "${"%.1f".format(it.seconds)}s" } ?: "-", true)
        drawResultMetricCell(canvas, metricLeft + metricWidth, top, metricWidth, t("HYPE").uppercase(), lastHypeScore.toString(), true)
        drawResultMetricCell(canvas, metricLeft + metricWidth * 2f, top, metricWidth, t("CHAIN").uppercase(), "x$maxChain", false)
    }

    private fun drawResultMetricCell(canvas: Canvas, left: Float, top: Float, width: Float, label: String, value: String, divider: Boolean) {
        if (divider) {
            paint.style = Paint.Style.FILL
            paint.color = 0x20FFFFFF
            canvas.drawRect(left + width - dp(1f), top + dp(12f), left + width, top + dp(56f), paint)
        }
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8f)
        textPaint.color = 0x78FFFFFF
        canvas.drawText(label, left + dp(9f), top + dp(22f), textPaint)
        textPaint.textSize = dp(15f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, width - dp(18f)), left + dp(9f), top + dp(46f), textPaint)
    }

    private fun drawResultMetric(canvas: Canvas, left: Float, top: Float, width: Float, label: String, value: String) {
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8f)
        textPaint.color = 0x66FFFFFF
        canvas.drawText(t(label).uppercase(), left + dp(7f), top + dp(12f), textPaint)
        textPaint.textSize = dp(16f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, width - dp(14f)), left + dp(7f), top + dp(34f), textPaint)
        paint.style = Paint.Style.FILL
        paint.color = 0x24FFFFFF
        canvas.drawRect(left + width - dp(1f), top, left + width, top + dp(42f), paint)
    }

    private fun drawBrainballResultLine(canvas: Canvas, left: Float, top: Float, right: Float, accent: Int, won: Boolean) {
        val skin = selectedBallSkin()
        val archetype = levelArchetype()
        val iconRadius = dp(12f)
        drawBallSkin(canvas, left + iconRadius, top + dp(11f), iconRadius, skin, animated = true, locked = false)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8.6f)
        textPaint.color = withAlpha(accent, 230)
        val header = "${skin.name}  /  ${t(archetype.label).uppercase()}"
        canvas.drawText(fitText(header, right - left - dp(34f)), left + dp(32f), top + dp(8f), textPaint)

        textPaint.textSize = dp(9f)
        textPaint.color = 0xAFFFFFFF.toInt()
        canvas.drawText(fitText(t(brainballReactionText(won)), right - left - dp(34f)), left + dp(32f), top + dp(23f), textPaint)
    }

    private fun drawRiftBreakResultBadge(canvas: Canvas, left: Float, top: Float, right: Float, accent: Int) {
        scratch.set(left, top, right, top + dp(28f))
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            top,
            intArrayOf(withAlpha(0xFFFFCF4A.toInt(), 92), withAlpha(accent, 56), 0x00070B12),
            floatArrayOf(0f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = withAlpha(0xFFFFCF4A.toInt(), 205)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(10f)
        textPaint.color = 0xFFF7F4FF.toInt()
        val label = "${t("RIFT BREAK").uppercase()}  +$lastRiftBreakBonus ${t("HYPE").uppercase()}"
        canvas.drawText(fitText(label, (right - left) * 0.58f), left + dp(12f), top + dp(18f), textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = dp(8.8f)
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), 240)
        canvas.drawText(fitText(lastRiftBreakReason, (right - left) * 0.38f), right - dp(10f), top + dp(18f), textPaint)
    }

    private fun brainballReactionText(won: Boolean): String {
        if (!won) return "Brainball rebooting. Try cleaner taps."
        if (lastRiftBreak) return "Rift snapped. Braincell promoted."
        return when (selectedBallSkin().style) {
            SkinStyle.PRISM -> "Prism brain approved this nonsense."
            SkinStyle.VOID -> "Void walked through the bad idea."
            SkinStyle.CHROME -> "Chrome bounce paid rent today."
            SkinStyle.PLASMA -> "Plasma cooked the route."
            SkinStyle.BLOP -> "Blop survived on pure vibes."
            SkinStyle.GLITCH -> "Glitch found the illegal angle."
            SkinStyle.ZAP -> "Zap arrived before the plan."
            SkinStyle.LOOP -> "Loop did it twice for no reason."
            SkinStyle.STATIC -> "Static stared the level down."
            SkinStyle.RIFT -> "Rift brain knew the shortcut."
            SkinStyle.BYTE -> "Byte uploaded the win."
            SkinStyle.WOBBLE -> "Wobble made physics look confused."
            SkinStyle.CROWN -> "Crown behavior, no debate."
            SkinStyle.CLASSIC -> "Original brainball still has aura."
        }
    }

    private fun drawRewardSignalCard(canvas: Canvas, left: Float, top: Float, right: Float, reward: String, accent: Int) {
        val height = dp(66f)
        val info = nextRewardInfo()
        scratch.set(left, top, right, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            top + height,
            intArrayOf(withAlpha(accent, 44), 0xCC0B1019.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.9f)
        paint.color = withAlpha(accent, 135)
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        val iconSize = dp(34f)
        scratch.set(left + dp(10f), top + dp(14f), left + dp(10f) + iconSize, top + dp(14f) + iconSize)
        drawWorldAsset(canvas, "boost_chain", scratch, 220)

        val textLeft = left + dp(54f)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(8.8f)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("REWARD SIGNAL").uppercase(), textLeft, top + dp(18f), textPaint)
        textPaint.textSize = dp(11f)
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(fitText(reward, right - textLeft - dp(14f)), textLeft, top + dp(37f), textPaint)

        val barLeft = textLeft
        val barRight = right - dp(14f)
        val barTop = top + dp(49f)
        val progress = info?.progress ?: 1f
        paint.style = Paint.Style.FILL
        paint.color = 0x24FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + dp(5f), dp(3f), dp(3f), paint)
        paint.color = info?.accent ?: accent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress.coerceIn(0f, 1f), barTop + dp(5f), dp(3f), dp(3f), paint)

        if (info != null) {
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = dp(8.2f)
            textPaint.color = 0xAAFFFFFF.toInt()
            canvas.drawText("${(progress * 100f).roundToInt()}%", barRight, top + dp(18f), textPaint)
        }
    }

    private fun drawAdPlaceholder(canvas: Canvas) {
        val accent = if (gameMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
        val panelWidth = min(viewWidth - dp(32f), dp(390f))
        val panelHeight = dp(206f)
        val left = viewWidth * 0.5f - panelWidth * 0.5f
        val top = viewHeight * 0.5f - panelHeight * 0.5f
        scratch.set(left, top, left + panelWidth, top + panelHeight)

        paint.style = Paint.Style.FILL
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.9f)
        paint.color = withAlpha(accent, 170)
        canvas.drawRoundRect(scratch, dp(8f), dp(8f), paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(left, top, left + panelWidth, top + dp(4f), dp(3f), dp(3f), paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(9f)
        textPaint.color = withAlpha(accent, 235)
        canvas.drawText("${t("STREAK PROTECTION").uppercase()}  /  ${gameMode.menuTitle()}", left + dp(20f), top + dp(28f), textPaint)
        textPaint.textSize = dp(27f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("AD CONTINUE").uppercase(), left + dp(20f), top + dp(59f), textPaint)
        textPaint.textSize = dp(12f)
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText(fitText(adReason, panelWidth - dp(40f)), left + dp(20f), top + dp(84f), textPaint)
        textPaint.textSize = dp(9f)
        textPaint.color = 0x99FFFFFF.toInt()
        val adLine = if (pendingAdAction == AdAction.CONTINUE_AFTER_FAIL) {
            t("WATCH TO KEEP THE RUN ALIVE").uppercase()
        } else {
            t("THE RUN RESUMES AFTER THE INTERSTITIAL").uppercase()
        }
        canvas.drawText(adLine, left + dp(20f), top + dp(105f), textPaint)

        val cx = left + panelWidth - dp(38f)
        val cy = top + dp(43f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(3f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = withAlpha(accent, 210)
        scratch.set(cx - dp(13f), cy - dp(13f), cx + dp(13f), cy + dp(13f))
        canvas.drawArc(scratch, -80f, 290f + sin(stateElapsed * 4f) * 30f, false, paint)
        paint.strokeCap = Paint.Cap.BUTT

        adButton.set(left + dp(20f), top + panelHeight - dp(68f), left + panelWidth - dp(20f), top + panelHeight - dp(20f))
        drawResultActionButton(canvas, adButton, t(if (adLoading) "LOADING" else "CONTINUE WITH AD").uppercase(), accent, ButtonId.AD_CONTINUE)
    }

    private fun drawExportingOverlay(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xA407090F.toInt()
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        val width = min(viewWidth - dp(34f), dp(390f))
        val height = dp(118f)
        val left = viewWidth * 0.5f - width * 0.5f
        val top = (viewHeight * 0.5f - height * 0.5f).coerceAtLeast(dp(144f))
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF07090F.toInt()
        canvas.drawRoundRect(scratch, dp(9f), dp(9f), paint)
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(withAlpha(level.accent, 92), 0xFF07090F.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)
        paint.shader = null
        paint.color = level.accent
        canvas.drawRect(left, top, left + dp(4f), top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.2f)
        paint.color = level.accent
        canvas.drawRoundRect(scratch, dp(7f), dp(7f), paint)

        val iconSize = dp(42f)
        scratch.set(left + dp(16f), top + dp(16f), left + dp(16f) + iconSize, top + dp(16f) + iconSize)
        drawWorldAsset(canvas, "ui_share", scratch, 245)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(18f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("BUILDING SHORT").uppercase(), left + dp(70f), top + dp(31f), textPaint)
        textPaint.textSize = dp(11f)
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText("9:16 MP4  /  TIKTOK  /  REELS  /  SHORTS", left + dp(70f), top + dp(52f), textPaint)

        val barLeft = left + dp(70f)
        val barRight = left + width - dp(18f)
        val barTop = top + dp(74f)
        val pulse = 0.38f + 0.62f * ((sin(menuPulse * 5.4f) + 1f) * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = 0x36FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + dp(6f), dp(4f), dp(4f), paint)
        paint.color = level.accent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * pulse, barTop + dp(6f), dp(4f), dp(4f), paint)

        textPaint.textSize = dp(8.6f)
        textPaint.color = 0xB8FFFFFF.toInt()
        canvas.drawText(t("SHARE COUNTS UNLOCK BYTE / KABOOM / 404").uppercase(), barLeft, top + dp(99f), textPaint)
    }

    private fun drawResultActionButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        accent: Int,
        button: ButtonId
    ) {
        val secondary = button == ButtonId.SHARE
        val active = activeButton == button
        val radius = dp(8f)
        paint.style = Paint.Style.FILL
        paint.shader = if (secondary) {
            LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                intArrayOf(withAlpha(accent, if (active) 74 else 34), 0xDC070B12.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                intArrayOf(
                    if (active) withAlpha(accent, 245) else withAlpha(accent, 225),
                    if (button == ButtonId.CONTINUE || button == ButtonId.AD_CONTINUE) 0xFF9D214F.toInt() else 0xFF6C4E12.toInt()
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        paint.color = if (secondary) accent else withAlpha(0xFFFFFFFF.toInt(), 62)
        canvas.drawRect(rect.left, rect.top, rect.left + dp(if (active) 5f else 3f), rect.bottom, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(if (active) 1.8f else if (secondary) 1.3f else 0.9f)
        paint.color = if (secondary) accent else 0x9AFFFFFF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = dp(14f)
        textPaint.color = when {
            secondary -> accent
            button == ButtonId.CONTINUE || button == ButtonId.AD_CONTINUE -> 0xFFF7F4FF.toInt()
            else -> 0xFF07090F.toInt()
        }
        canvas.drawText(fitText(label, rect.width() - dp(20f)), rect.centerX(), rect.centerY() + dp(5f), textPaint)
    }

    private fun drawFlash(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (state == GameState.WON) level.accent else 0xFFFF4D8D.toInt(), (flash * 72).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
    }

    private fun isLargeScreenLayout(): Boolean =
        resources.configuration.smallestScreenWidthDp >= 600

    private fun menuContentWidth(): Float =
        min(viewWidth - dp(48f), dp(if (isLargeScreenLayout()) 760f else 540f))

    private fun menuContentLeft(): Float = (viewWidth - menuContentWidth()) * 0.5f

    private fun menuContentRight(): Float = menuContentLeft() + menuContentWidth()

    private fun pageContentWidth(): Float =
        min(viewWidth - dp(36f), dp(if (isLargeScreenLayout()) 920f else 540f))

    private fun pageContentLeft(): Float = (viewWidth - pageContentWidth()) * 0.5f

    private fun pageContentRight(): Float = pageContentLeft() + pageContentWidth()

    private fun layoutButtons() {
        val compactControls = viewWidth < dp(430f)
        val size = dp(if (compactControls) 30f else 38f)
        val gap = dp(if (compactControls) 4f else 8f)
        val top = dp(if (compactControls) 10f else 12f)
        val right = viewWidth - dp(if (compactControls) 12f else 16f)
        var cursor = right
        homeButton.set(cursor - size, top, cursor, top + size)
        cursor -= size + gap
        restartButton.set(cursor - size, top, cursor, top + size)
        cursor -= size + gap
        sfxButton.set(cursor - size, top, cursor, top + size)
        cursor -= size + gap
        musicButton.set(cursor - size, top, cursor, top + size)
        shareButton.setEmpty()
        nextButton.setEmpty()

        val panelWidth = min(viewWidth - dp(68f), dp(500f))
        val panelLeft = (viewWidth - panelWidth) * 0.5f
        val panelRight = panelLeft + panelWidth
        val resultHeight = dp(48f)
        val resultBottom = viewHeight - dp(34f)
        if (state == GameState.WON) {
            val gapWidth = dp(10f)
            val half = (panelRight - panelLeft - gapWidth) * 0.5f
            resultShareButton.set(panelLeft, resultBottom - resultHeight, panelLeft + half, resultBottom)
            resultNextButton.set(panelLeft + half + gapWidth, resultBottom - resultHeight, panelRight, resultBottom)
        } else {
            resultShareButton.setEmpty()
            resultNextButton.set(panelLeft, resultBottom - resultHeight, panelRight, resultBottom)
        }
        resultRetryButton.set(resultNextButton)
    }

    private fun layoutMenuButtons() {
        val side = menuContentLeft()
        val right = menuContentRight()
        val gap = dp(8f)
        val bottom = viewHeight - dp(86f)
        val audioTop = viewHeight - dp(68f)
        val audioBottom = viewHeight - dp(22f)
        val audioSize = dp(46f)
        menuPrivacyButton.set(side, audioTop, side + dp(94f), audioBottom)
        menuMusicButton.set(right - audioSize, audioTop, right, audioBottom)
        menuSfxButton.set(right - audioSize * 2f - gap, audioTop, right - audioSize - gap, audioBottom)
        menuLanguageButton.set(menuPrivacyButton.right + gap, audioTop, menuSfxButton.left - gap, audioBottom)
        if (menuState == MenuState.MODES) {
            val compactHeight = dp(54f)
            val playHeight = dp(66f)
            val chipHeight = dp(54f)
            val half = side + (right - side) * 0.5f
            menuLeaderboardButton.set(side, bottom - compactHeight, half - gap * 0.5f, bottom)
            menuCollectionButton.set(half + gap * 0.5f, bottom - compactHeight, right, bottom)
            menuStartButton.set(side, menuLeaderboardButton.top - gap - playHeight, right, menuLeaderboardButton.top - gap)
            menuContinueButton.set(side, menuStartButton.top - gap - chipHeight, half - gap * 0.5f, menuStartButton.top - gap)
            menuChaosButton.set(half + gap * 0.5f, menuStartButton.top - gap - chipHeight, right, menuStartButton.top - gap)
            menuActionStartButton.setEmpty()
            menuBackButton.setEmpty()
        } else {
            menuCollectionButton.setEmpty()
            menuLeaderboardButton.setEmpty()
            menuContinueButton.set(side, bottom - dp(54f), right, bottom)
            menuChaosButton.set(side, menuContinueButton.top - gap - dp(76f), right, menuContinueButton.top - gap)
            menuActionStartButton.set(side, menuChaosButton.top - gap - dp(62f), right, menuChaosButton.top - gap)
            menuStartButton.setEmpty()
            menuBackButton.set(menuContinueButton)
        }
    }

    private fun buttonAt(x: Float, y: Float): ButtonId {
        if (screen == Screen.AD && adButton.contains(x, y)) return ButtonId.AD_CONTINUE
        if (state == GameState.WON && resultNextButton.contains(x, y)) return ButtonId.NEXT
        if (state == GameState.WON && resultShareButton.contains(x, y)) return ButtonId.SHARE
        if (state == GameState.LOST && resultRetryButton.contains(x, y)) return ButtonId.CONTINUE
        if (homeButton.contains(x, y)) return ButtonId.HOME
        if (restartButton.contains(x, y)) return ButtonId.RESTART
        if (sfxButton.contains(x, y)) return ButtonId.SFX
        if (musicButton.contains(x, y)) return ButtonId.MUSIC
        if ((state == GameState.WON || state == GameState.LOST) && shareButton.contains(x, y)) return ButtonId.SHARE
        if (state == GameState.WON && nextButton.contains(x, y)) return ButtonId.NEXT
        return ButtonId.NONE
    }

    private fun menuButtonAt(x: Float, y: Float): MenuButton {
        if (menuPrivacyButton.contains(x, y)) return MenuButton.PRIVACY
        if (menuLanguageButton.contains(x, y)) return MenuButton.LANGUAGE
        if (menuSfxButton.contains(x, y)) return MenuButton.SFX
        if (menuMusicButton.contains(x, y)) return MenuButton.MUSIC
        if (menuState == MenuState.MODES) {
            if (menuStartButton.contains(x, y)) return MenuButton.PLAY
            if (menuContinueButton.contains(x, y)) return MenuButton.CLASSIC
            if (menuChaosButton.contains(x, y)) return MenuButton.CHAOS
            if (menuLeaderboardButton.contains(x, y)) return MenuButton.LEADERBOARDS
            if (menuCollectionButton.contains(x, y)) return MenuButton.COLLECTION
        } else {
            if (menuActionStartButton.contains(x, y)) return MenuButton.START
            if (menuChaosButton.contains(x, y)) return MenuButton.CONTINUE
            if (menuContinueButton.contains(x, y)) return MenuButton.BACK
        }
        return MenuButton.NONE
    }

    private fun shareRun() {
        if (exportingShare) return
        val request = synchronized(lock) {
            val score = lastScore
            val skin = selectedBallSkin()
            val body = if (score != null) {
                t("Can you beat my Kavvoro rift?").replace("%mode", gameMode.menuTitle()).replace("%level", "L${score.level}")
                    .replace("%ball", skin.name)
                    .replace("%rank", score.rank)
                    .replace("%hype", lastHypeScore.toString())
                    .replace("%chain", maxChain.toString())
                    .replace("%streak", streak.toString())
                    .replace("%code", challengeCode())
            } else {
                t("Trying Brainrot Chaos: Kavvoro")
                    .replace("%mode", gameMode.menuTitle())
                    .replace("%level", "L${level.index}")
                    .replace("%ball", skin.name)
                    .replace("%code", challengeCode())
            }
            val resultLabel = score?.let {
                "${t("RANK").uppercase()} ${it.rank}  ${"%.1f".format(it.seconds)}s  ${t("HYPE").uppercase()} $lastHypeScore"
            } ?: t("CRASH REPLAY").uppercase()
            val frames = replayFrames.ifEmpty {
                listOf(PhysicsFrame(ball, 0f, pulseIntensity, if (state == GameState.WON) PhysicsOutcome.WON else PhysicsOutcome.LOST))
            }
            val archetype = levelArchetype()
            ShareRequest(
                payload = ReplaySharePayload(
                    level = level,
                    line = simplifyLine(playerLine),
                    replayFrames = frames,
                    modeLabel = gameMode.label,
                    hypeScore = lastHypeScore,
                    streak = streak,
                    challengeCode = challengeCode(),
                    curseLabel = curseStackLabel(),
                    resultLabel = resultLabel,
                    ballName = skin.name,
                    ballPrimary = skin.primary,
                    ballSecondary = skin.secondary,
                    lineColor = skin.lineColor,
                    ballArtResource = brainballArtResources[skin.id] ?: R.drawable.brainball_nodlo,
                    ballVisualScale = gameplayBallScale(skin),
                    riftBreak = lastRiftBreak,
                    riftBreakLabel = lastRiftBreakReason,
                    archetypeLabel = t(archetype.label).uppercase(),
                    archetypeDetail = t(archetype.detail),
                    runSeconds = score?.seconds ?: simElapsed
                ),
                text = body
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
                        shareVideo(video, request.text)
                    }
                } catch (error: Throwable) {
                    Log.e("KavvoroReplay", "Video export failed; falling back to text share", error)
                    post {
                        exportingShare = false
                        shareText(request.text)
                    }
                }
            },
            "kavvoro-replay-export"
        ).start()
    }

    private fun shareVideo(file: File, body: String) {
        recordShareReward()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_TITLE, t("Kavvoro 9:16 replay"))
            putExtra(Intent.EXTRA_SUBJECT, t("Beat my Kavvoro rift"))
            clipData = ClipData.newUri(context.contentResolver, t("Kavvoro replay"), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, t("Share Kavvoro short")))
    }

    private fun shareText(body: String) {
        recordShareReward()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_TITLE, t("Kavvoro challenge"))
            putExtra(Intent.EXTRA_SUBJECT, t("Beat my Kavvoro rift"))
        }
        context.startActivity(Intent.createChooser(intent, t("Share Kavvoro short")))
    }

    private fun recordShareReward() {
        val before = unlockedSkinIds()
        val used = prefs.getInt(SHARE_COUNT_KEY, 0)
        val total = used + 1
        prefs.edit().putInt(SHARE_COUNT_KEY, total).apply()
        val unlocked = unlockedSkinIds()
        val newSkin = ballSkins.firstOrNull { it.id in (unlocked - before) }
        if (newSkin != null) {
            rewardMessage = rewardLine(newSkin)
            audio.playEvent(SoundEvent.UNLOCK, selectedBallIndex())
            hapticSequence(
                HapticFeedbackConstants.CONFIRM to 0L,
                HapticFeedbackConstants.LONG_PRESS to 90L
            )
        } else {
            rewardMessage = nextShareRewardText(total) ?: rewardLine(null)
            hapticSequence(HapticFeedbackConstants.CLOCK_TICK to 0L)
        }
    }

    private fun nextShareRewardText(totalShares: Int): String? {
        val skin = ballSkins
            .filter { it.unlock.type == UnlockType.SHARE_COUNT && !isSkinUnlocked(it) }
            .minByOrNull { it.unlock.value }
            ?: return null
        val remaining = (skin.unlock.value - totalShares).coerceAtLeast(0)
        return "${t("SHARE").uppercase()} $totalShares / ${skin.unlock.value}  /  ${skin.name} ${t("IN").uppercase()} $remaining"
    }

    private fun simplifyLine(points: List<Point2>): List<Point2> {
        if (points.size <= 2) return points.toList()
        val simplified = mutableListOf(points.first())
        var last = points.first()
        for (i in 1 until points.lastIndex) {
            val p = points[i]
            val next = points[i + 1]
            val angleA = atan2(p.y - last.y, p.x - last.x)
            val angleB = atan2(next.y - p.y, next.x - p.x)
            val angleDelta = kotlin.math.abs(angleA - angleB)
            if (last.distanceTo(p) > 0.18f || angleDelta > 0.18f) {
                simplified += p
                last = p
            }
        }
        simplified += points.last()
        return simplified
    }

    private fun levelHasCurse(type: CurseType): Boolean {
        return hasCurse(level, type)
    }

    private fun hasCurse(spec: LevelSpec, type: CurseType): Boolean {
        return spec.curses.any { it.type == type }
    }

    private fun levelArchetype(spec: LevelSpec = level): LevelArchetype {
        val movingHazards = spec.hazards.count { it.isMoving }
        return when {
            spec.portals.isNotEmpty() -> LevelArchetype("PORTAL SLING", "Teleport timing and launch control", 0xFF45F2FF.toInt(), "portal_goal")
            hasCurse(spec, CurseType.RIFT_WIND) -> LevelArchetype("WIND TUNNEL", "Short bursts beat the gust", 0xFF8AA6FF.toInt(), "boost_recharge")
            hasCurse(spec, CurseType.OVERHEAT) || hasCurse(spec, CurseType.RIFT_DRAIN) -> LevelArchetype("ENERGY TAX", "Spend Rift in tiny snaps", 0xFFFF5757.toInt(), "danger_beacon")
            hasCurse(spec, CurseType.FOCUS_FIELD) || hasCurse(spec, CurseType.POWER_HOLD) -> LevelArchetype("CONTROL LAB", "Tap timing changes the pull", 0xFFFFCF4A.toInt(), "boost_plasma")
            spec.pulseZones.size >= 2 || hasCurse(spec, CurseType.PULSE_STORM) -> LevelArchetype("PULSE MAZE", "Fields bend speed and direction", 0xFFC15CFF.toInt(), "boost_pulse")
            movingHazards >= 3 -> LevelArchetype("MOVING DANGER", "Read the lanes before committing", 0xFFFF4D8D.toInt(), "hazard_glitch")
            spec.blocks.size >= 5 -> LevelArchetype("GATE STACK", "Bounce angles matter", 0xFF1DE8C8.toInt(), "platform_classic")
            gameMode == GameMode.CHAOS -> LevelArchetype("CHAOS TOUCH", "Fast reactions, no sleepy holds", 0xFFFF4D8D.toInt(), "boost_chain")
            else -> LevelArchetype("RIFT PATH", "Clean control and smooth release", 0xFF1DE8C8.toInt(), "boost_rift_pull")
        }
    }

    private fun selectedBallSkin(): BallSkin {
        return ballSkins.firstOrNull { it.id == selectedSkinId && isSkinUnlocked(it) }
            ?: ballSkins.first { it.id == DEFAULT_SKIN_ID }
    }

    private fun focusedCollectionSkin(): BallSkin {
        return ballSkins.firstOrNull { it.id == collectionFocusSkinId } ?: selectedBallSkin()
    }

    private fun selectedBallIndex(): Int = ballSkins.indexOfFirst { it.id == selectedBallSkin().id }.coerceAtLeast(0)

    private fun isSkinUnlocked(skin: BallSkin): Boolean {
        if (BuildConfig.FORCE_UNLOCK_ALL_BRAINBALLS) return true
        if (skin.unlock.type == UnlockType.DEFAULT) return true
        if (skin.unlock.type == UnlockType.PREMIUM) return prefs.getBoolean(purchasedSkinKey(skin.id), false)
        if (skin.unlock.type == UnlockType.HYPE_COST) return prefs.getBoolean(earnedSkinKey(skin.id), false)
        if (prefs.getBoolean(earnedSkinKey(skin.id), false)) return true
        if (!unlockConditionMet(skin.unlock)) return false
        prefs.edit().putBoolean(earnedSkinKey(skin.id), true).apply()
        return true
    }

    private fun unlockConditionMet(rule: UnlockRule): Boolean {
        return when (rule.type) {
            UnlockType.DEFAULT -> true
            UnlockType.PREMIUM -> false
            UnlockType.CLASSIC_LEVEL -> clearedLevel(GameMode.CLASSIC) >= rule.value
            UnlockType.CHAOS_LEVEL -> clearedLevel(GameMode.CHAOS) >= rule.value
            UnlockType.TUTORIAL_CLEAR -> max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS)) >= rule.value
            UnlockType.BEST_STREAK -> bestStreak() >= rule.value
            UnlockType.SHARE_COUNT -> prefs.getInt(SHARE_COUNT_KEY, 0) >= rule.value
            UnlockType.HYPE_COST -> false
        }
    }

    private fun unlockedSkinIds(): Set<String> {
        return ballSkins.filter(::isSkinUnlocked).map { it.id }.toSet()
    }

    private fun unlockedSkinCount(): Int = ballSkins.count(::isSkinUnlocked)

    private fun bestStreak(): Int = prefs.getInt(BEST_STREAK_KEY, prefs.getInt("clear_streak", 0)).coerceAtLeast(0)

    private fun hypeBalance(): Int = prefs.getInt(HYPE_BANK_KEY, prefs.getInt("last_hype", 0)).coerceAtLeast(0)

    private fun spendHype(amount: Int) {
        val next = (hypeBalance() - amount.coerceAtLeast(0)).coerceAtLeast(0)
        prefs.edit().putInt(HYPE_BANK_KEY, next).apply()
    }

    private fun formatHypeAmount(value: Int): String {
        val safe = value.coerceAtLeast(0)
        return when {
            safe >= 1_000_000 -> "${safe / 100_000 / 10f}M"
            safe >= 10_000 -> "${safe / 1_000}K"
            safe >= 1_000 -> {
                val tenths = safe / 100
                "${tenths / 10}.${tenths % 10}K"
            }
            else -> safe.toString()
        }
    }

    private fun clearedLevel(mode: GameMode): Int = (modeHighestLevel(mode) - 1).coerceAtLeast(0)

    private fun rewardLine(newSkin: BallSkin?): String {
        val next = nextRewardText(excludeId = newSkin?.id)
        return if (newSkin != null) {
            "${t("UNLOCKED").uppercase()} ${newSkin.name} | ${next ?: t("ALL FREE REWARDS UNLOCKED").uppercase()}"
        } else {
            next ?: t("ALL FREE REWARDS UNLOCKED").uppercase()
        }
    }

    private fun finishRewardLine(newSkin: BallSkin?): String {
        val signals = mutableListOf<String>()
        if (lastDailyBonus > 0) {
            signals += "${t("DAILY RIFT BONUS").uppercase()} +$lastDailyBonus"
        }
        if (lastRiftBreak && lastRiftBreakBonus > 0) {
            signals += "${t("RIFT BREAK").uppercase()} +$lastRiftBreakBonus"
        }
        if (lastStreakMilestoneBonus > 0) {
            signals += "${t("STREAK SURGE").uppercase()} x$streak +$lastStreakMilestoneBonus"
        }
        signals += rewardLine(newSkin)
        return signals.joinToString(" | ")
    }

    private fun claimDailyRiftBonus(): Int {
        val key = dailyRiftBonusKey()
        if (prefs.getBoolean(key, false)) return 0
        val bonus = dailyRiftBonusForMode(gameMode)
        prefs.edit()
            .putBoolean(key, true)
            .putInt(dailyRiftBonusAmountKey(), bonus)
            .putString(dailyRiftBonusModeKey(), gameMode.name)
            .putLong(dailyRiftBonusClaimedAtKey(), System.currentTimeMillis())
            .apply()
        return bonus
    }

    private fun dailyRiftBonusClaimed(): Boolean = prefs.getBoolean(dailyRiftBonusKey(), false)

    private fun dailyRiftBonusKey(): String = "daily_rift_bonus_${LevelDirector.dailySeed()}"

    private fun dailyRiftBonusAmountKey(): String = "${dailyRiftBonusKey()}_amount"

    private fun dailyRiftBonusModeKey(): String = "${dailyRiftBonusKey()}_mode"

    private fun dailyRiftBonusClaimedAtKey(): String = "${dailyRiftBonusKey()}_claimed_at"

    private fun dailyRiftBonusForMode(mode: GameMode): Int = if (mode == GameMode.CHAOS) 420 else 320

    private fun dailyRiftClaimedAmount(): Int = prefs.getInt(dailyRiftBonusAmountKey(), 0)

    private fun dailyRiftClaimedMode(): GameMode? {
        return prefs.getString(dailyRiftBonusModeKey(), null)?.let { saved ->
            runCatching { GameMode.valueOf(saved) }.getOrNull()
        }
    }

    private fun dailyRiftResetText(): String {
        val remaining = dailyRiftRemainingMillis()
        val hours = remaining / HOUR_MILLIS
        val minutes = ((remaining % HOUR_MILLIS) / MINUTE_MILLIS).coerceAtLeast(1L)
        return "${t("RESET").uppercase()} ${hours}H ${minutes}M"
    }

    private fun dailyRiftDayProgress(): Float {
        val remaining = dailyRiftRemainingMillis().coerceIn(0L, DAY_MILLIS)
        return (1f - remaining.toFloat() / DAY_MILLIS.toFloat()).coerceIn(0.05f, 0.95f)
    }

    private fun dailyRiftRemainingMillis(): Long {
        val now = System.currentTimeMillis()
        val nextReset = (LevelDirector.dailySeed() + 1L) * DAY_MILLIS
        return (nextReset - now).coerceIn(0L, DAY_MILLIS)
    }

    private fun nextRewardText(excludeId: String? = null): String? {
        val next = nextRewardInfo(excludeId)
        return next?.let { "${it.name} ${t("AT").uppercase()} ${it.label.uppercase()}" }
    }

    private fun nextRewardInfo(excludeId: String? = null): NextReward? {
        return ballSkins
            .filter { it.id != excludeId && it.unlock.type != UnlockType.PREMIUM && !isSkinUnlocked(it) }
            .mapNotNull { skin ->
                rewardDistance(skin.unlock)?.let { distance ->
                    val current = rewardProgressValue(skin.unlock)
                    val target = skin.unlock.value.coerceAtLeast(1)
                    NextReward(
                        name = skin.name,
                        label = unlockShortLabel(skin),
                        target = target,
                        distance = distance,
                        progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f),
                        accent = skin.lineColor
                    )
                }
            }
            .minWithOrNull(compareBy<NextReward> { it.distance }.thenBy { it.label })
    }

    private fun nextStreakRewardInfo(): NextReward? {
        return ballSkins
            .filter { it.unlock.type == UnlockType.BEST_STREAK && !isSkinUnlocked(it) }
            .minByOrNull { it.unlock.value }
            ?.let { skin ->
                val target = skin.unlock.value.coerceAtLeast(1)
                NextReward(
                    name = skin.name,
                    label = unlockShortLabel(skin),
                    target = target,
                    distance = (target - bestStreak()).coerceAtLeast(0),
                    progress = (bestStreak().toFloat() / target.toFloat()).coerceIn(0f, 1f),
                    accent = skin.lineColor
                )
            }
    }

    private fun rewardDistance(rule: UnlockRule): Int? {
        return when (rule.type) {
            UnlockType.CLASSIC_LEVEL -> (rule.value - clearedLevel(GameMode.CLASSIC)).coerceAtLeast(0)
            UnlockType.CHAOS_LEVEL -> (rule.value - clearedLevel(GameMode.CHAOS)).coerceAtLeast(0)
            UnlockType.TUTORIAL_CLEAR -> (rule.value - max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS))).coerceAtLeast(0)
            UnlockType.BEST_STREAK -> (rule.value - bestStreak()).coerceAtLeast(0)
            UnlockType.SHARE_COUNT -> (rule.value - prefs.getInt(SHARE_COUNT_KEY, 0)).coerceAtLeast(0)
            UnlockType.HYPE_COST -> (rule.value - hypeBalance()).coerceAtLeast(0)
            UnlockType.DEFAULT,
            UnlockType.PREMIUM -> null
        }
    }

    private fun rewardProgressValue(rule: UnlockRule): Int {
        return when (rule.type) {
            UnlockType.CLASSIC_LEVEL -> clearedLevel(GameMode.CLASSIC)
            UnlockType.CHAOS_LEVEL -> clearedLevel(GameMode.CHAOS)
            UnlockType.TUTORIAL_CLEAR -> max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS))
            UnlockType.BEST_STREAK -> bestStreak()
            UnlockType.SHARE_COUNT -> prefs.getInt(SHARE_COUNT_KEY, 0)
            UnlockType.HYPE_COST -> hypeBalance()
            UnlockType.DEFAULT,
            UnlockType.PREMIUM -> 0
        }
    }

    private fun unlockShortLabel(skin: BallSkin): String {
        return when (skin.unlock.type) {
            UnlockType.DEFAULT -> t("UNLOCKED").uppercase()
            UnlockType.PREMIUM -> premiumPriceLabel(skin)
            UnlockType.CLASSIC_LEVEL -> "${t("CLASSIC")} L${skin.unlock.value.toString().padStart(2, '0')}"
            UnlockType.CHAOS_LEVEL -> "${t("CHAOS")} L${skin.unlock.value.toString().padStart(2, '0')}"
            UnlockType.TUTORIAL_CLEAR -> "${t("TUTORIAL")} L${skin.unlock.value.toString().padStart(2, '0')}"
            UnlockType.BEST_STREAK -> "${t("STREAK")} ${skin.unlock.value}"
            UnlockType.SHARE_COUNT -> "${t("SHARE")} ${skin.unlock.value}"
            UnlockType.HYPE_COST -> "${formatHypeAmount(skin.unlock.value)} ${t("HYPE").uppercase()}"
        }
    }

    private fun unlockLongLabel(skin: BallSkin): String {
        return when (skin.unlock.type) {
            UnlockType.PREMIUM -> "${premiumPriceLabel(skin)} - ${t("local price from Play Billing")}"
            UnlockType.HYPE_COST -> "${t("UNLOCK WITH").uppercase()} ${formatHypeAmount(skin.unlock.value)} ${t("HYPE").uppercase()} / ${t("HYPE BANK").uppercase()} ${formatHypeAmount(hypeBalance())}"
            else -> t(skin.unlock.label)
        }
    }

    private fun premiumPriceLabel(skin: BallSkin): String {
        return premiumPricesBySkin[skin.id]
            ?: prefs.getString(premiumPriceKey(skin.id), null)
            ?: prefs.getString(PREMIUM_PRICE_KEY, "0.99 LOCAL")
            ?: "0.99 LOCAL"
    }

    private fun premiumCompactPriceLabel(skin: BallSkin): String = premiumPriceLabel(skin).substringBefore(" ")

    private fun premiumPriceKey(id: String): String = "premium_price_$id"

    private fun purchasedSkinKey(id: String): String = "skin_purchased_$id"

    private fun earnedSkinKey(id: String): String = "skin_unlocked_$id"

    private fun curseStackLabel(): String {
        if (level.curses.isEmpty()) return t("NO CURSE").uppercase()
        return level.curses.joinToString(" + ") { t(it.name.uppercase()).uppercase() }
    }

    private fun challengeCode(): String {
        val hype = lastHypeScore.toLong().coerceAtLeast(17L)
        val mixed = level.seed xor (level.index.toLong() * 0x9E3779B9L) xor (hype * 131L)
        val raw = (mixed ushr 1).toString(36).uppercase()
        return "KAV-" + raw.takeLast(6).padStart(6, '0')
    }

    private fun modeMeta(mode: GameMode): String {
        val progress = modeProgress(mode)
        val modeStreak = modeStreak(mode)
        return if (progress <= 1) {
            t("START LEVEL 01").uppercase()
        } else {
            "${t("LEVEL").uppercase()} ${progress.toString().padStart(2, '0')}   ${t("STREAK").uppercase()} $modeStreak"
        }
    }

    private fun modeProgress(mode: GameMode): Int {
        return when (mode) {
            GameMode.CLASSIC -> prefs.getInt(progressKey(mode), prefs.getInt("unlocked_level", 1)).coerceAtLeast(1)
            GameMode.CHAOS -> prefs.getInt(progressKey(mode), prefs.getInt("chaos_level", 1)).coerceAtLeast(1)
        }
    }

    private fun modeStreak(mode: GameMode): Int {
        return prefs.getInt(streakKey(mode), if (mode == gameMode) streak else 0).coerceAtLeast(0)
    }

    private fun modeHighestLevel(mode: GameMode): Int {
        return prefs.getInt(highestLevelKey(mode), modeProgress(mode)).coerceAtLeast(1)
    }

    private fun modeBestStreak(mode: GameMode): Int {
        return prefs.getInt(bestModeStreakKey(mode), modeStreak(mode)).coerceAtLeast(0)
    }

    private fun resetModeProgress(mode: GameMode) {
        val existingHighestLevel = modeHighestLevel(mode)
        val existingBestStreak = modeBestStreak(mode)
        prefs.edit()
            .putInt(highestLevelKey(mode), existingHighestLevel)
            .putInt(bestModeStreakKey(mode), existingBestStreak)
            .putInt(progressKey(mode), 1)
            .putInt(streakKey(mode), 0)
            .putInt(freeFailContinueKey(mode), 0)
            .putInt(continueAdStreakKey(mode), 0)
            .putInt(levelAdKey(mode), 0)
            .apply()
    }

    private fun progressKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "classic_level"
        GameMode.CHAOS -> "chaos_level"
    }

    private fun highestLevelKey(mode: GameMode): String = "highest_level_${mode.name.lowercase()}"

    private fun fairHighestLevelKey(mode: GameMode): String = "fair_highest_level_${mode.name.lowercase()}"

    private fun bestModeStreakKey(mode: GameMode): String = "best_streak_${mode.name.lowercase()}"

    private fun fairBestStreakKey(mode: GameMode): String = "fair_best_streak_${mode.name.lowercase()}"

    private fun streakKey(mode: GameMode): String = "streak_${mode.name.lowercase()}"

    private fun freeFailContinueKey(mode: GameMode): String = "free_fail_continue_${mode.name.lowercase()}"

    private fun continueAdStreakKey(mode: GameMode): String = "continue_ad_streak_${mode.name.lowercase()}"

    private fun levelAdKey(mode: GameMode): String = "level_ad_checkpoint_${mode.name.lowercase()}"

    private fun failContinueCountKey(mode: GameMode, levelNumber: Int): String =
        "fail_continue_${mode.name.lowercase()}_$levelNumber"

    private fun GameMode.menuTitle(): String = when (this) {
        GameMode.CLASSIC -> t("CLASSIC").uppercase()
        GameMode.CHAOS -> t("CHAOS").uppercase()
    }

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

    private fun bestKey(level: Int): String = "best_rank_${gameMode.name.lowercase()}_$level"

    private fun rankValue(rank: String): Int = when (rank) {
        "S" -> 0
        "A" -> 1
        "B" -> 2
        "C" -> 3
        else -> 9
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
    }

    private fun detectRenderProfile(): RenderProfile {
        val deviceText = listOf(
            Build.MANUFACTURER,
            Build.MODEL,
            Build.DEVICE,
            Build.PRODUCT,
            Build.HARDWARE,
            Build.BOARD
        ).joinToString(" ").lowercase()
        val cores = Runtime.getRuntime().availableProcessors()
        val lowEndSignature = listOf(
            "moto g06",
            "xt2535",
            "mt6768",
            "mt6769",
            "mt6765",
            "helio g80",
            "helio g81",
            "helio g85",
            "mali-g52"
        ).any { it in deviceText }
        return when {
            lowEndSignature || cores <= 4 -> RenderProfile.LOW
            cores <= 6 -> RenderProfile.BALANCED
            else -> RenderProfile.HIGH
        }
    }

    private data class ShareRequest(
        val payload: ReplaySharePayload,
        val text: String
    )

    private data class ModeWarning(
        val title: String,
        val subtitle: String,
        val accent: Int
    )

    private data class LevelArchetype(
        val label: String,
        val detail: String,
        val accent: Int,
        val iconKey: String
    )

    private data class BallSkin(
        val id: String,
        val name: String,
        val subtitle: String,
        val primary: Int,
        val secondary: Int,
        val lineColor: Int,
        val style: SkinStyle,
        val unlock: UnlockRule,
        val power: BallPower = BallPower.NONE
    )

    private data class UnlockRule(
        val type: UnlockType,
        val value: Int,
        val label: String
    )

    private data class NextReward(
        val name: String,
        val label: String,
        val target: Int,
        val distance: Int,
        val progress: Float,
        val accent: Int
    )

    interface AdBridge {
        fun showInterstitial(onFinished: () -> Unit)
        fun showRewardedContinue(onRewarded: () -> Unit, onUnavailable: () -> Unit)

        companion object {
            val NONE = object : AdBridge {
                override fun showInterstitial(onFinished: () -> Unit) {
                    onFinished()
                }

                override fun showRewardedContinue(onRewarded: () -> Unit, onUnavailable: () -> Unit) {
                    onRewarded()
                }
            }
        }
    }

    interface PurchaseBridge {
        fun purchase(productId: String)
        fun restore()

        companion object {
            val NONE = object : PurchaseBridge {
                override fun purchase(productId: String) = Unit
                override fun restore() = Unit
            }
        }
    }

    private enum class GameState {
        READY,
        SIMULATING,
        WON,
        LOST
    }

    private enum class Screen {
        MENU,
        GAME,
        COLLECTION,
        LEADERBOARDS,
        LANGUAGE,
        AD
    }

    private enum class MenuState {
        MODES,
        MODE_ACTION
    }

    private enum class GameMode(val label: String) {
        CLASSIC("CLASSIC"),
        CHAOS("CHAOS")
    }

    private enum class RenderProfile {
        LOW,
        BALANCED,
        HIGH
    }

    private enum class MenuButton {
        NONE,
        PLAY,
        CLASSIC,
        START,
        CHAOS,
        LEADERBOARDS,
        COLLECTION,
        PRIVACY,
        LANGUAGE,
        SFX,
        MUSIC,
        CONTINUE,
        BACK
    }

    private enum class CollectionFilter(val labelKey: String) {
        ALL("ALL"),
        SUPERPOWER("SUPERPOWER"),
        HYPE("HYPE"),
        PREMIUM("PREMIUM"),
        COSMETIC("COSMETIC")
    }

    private enum class SkinStyle {
        CLASSIC,
        PRISM,
        VOID,
        CHROME,
        PLASMA,
        BLOP,
        GLITCH,
        ZAP,
        LOOP,
        STATIC,
        RIFT,
        BYTE,
        WOBBLE,
        CROWN
    }

    private enum class UnlockType {
        DEFAULT,
        PREMIUM,
        CLASSIC_LEVEL,
        CHAOS_LEVEL,
        TUTORIAL_CLEAR,
        BEST_STREAK,
        SHARE_COUNT,
        HYPE_COST
    }

    private enum class ButtonId {
        NONE,
        HOME,
        RESTART,
        SHARE,
        NEXT,
        SFX,
        MUSIC,
        CONTINUE,
        AD_CONTINUE
    }

    private enum class AdAction {
        NONE,
        NEXT_LEVEL,
        CONTINUE_AFTER_FAIL,
        RESUME_RUN
    }

    private companion object {
        const val TARGET_STAGE_HEIGHT = 17.78f
        const val TUTORIAL_LAST_LEVEL = 10
        const val AD_LEVEL_INTERVAL = 6
        const val FAILS_BEFORE_CONTINUE_AD = 4
        const val MINUTE_MILLIS = 60_000L
        const val HOUR_MILLIS = 60L * MINUTE_MILLIS
        const val DAY_MILLIS = 24L * HOUR_MILLIS
        const val DEFAULT_SKIN_ID = "nodlo"
        const val SELECTED_SKIN_KEY = "selected_ball_skin"
        const val SFX_MUTED_KEY = "sfx_muted"
        const val MUSIC_MUTED_KEY = "music_muted"
        const val BEST_STREAK_KEY = "best_streak"
        const val SHARE_COUNT_KEY = "share_count"
        const val HYPE_BANK_KEY = "hype_bank"
        const val PREMIUM_PRICE_KEY = "premium_price_label"
        const val COLLECTION_BACK_INDEX = -2
        const val COLLECTION_RESTORE_INDEX = -3
        const val COLLECTION_FILTER_BASE_INDEX = -100
        const val LEADERBOARD_BACK_INDEX = -2
        const val LANGUAGE_BACK_INDEX = -2
    }
}
