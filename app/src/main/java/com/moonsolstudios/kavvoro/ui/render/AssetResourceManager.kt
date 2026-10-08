package com.moonsolstudios.kavvoro.ui.render

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.repository.BallSkinCatalog
import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import android.os.Build
import kotlin.math.max

object AssetResourceManager {

    var oxaniumTypeface: Typeface? = null
    var spaceGroteskTypeface: Typeface? = null
    var manropeTypeface: Typeface? = null

    private var oxaniumBoldTypeface: Typeface? = null
    private var oxaniumNormalTypeface: Typeface? = null
    private var oxaniumMediumTypeface: Typeface? = null
    private var spaceGroteskBoldTypeface: Typeface? = null
    private var spaceGroteskExtraBoldTypeface: Typeface? = null
    private val blurFilterCache = HashMap<Int, BlurMaskFilter>(64)

    private fun createWeightedTypeface(base: Typeface, weight: Int): Typeface {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface.create(base, weight, false)
        } else {
            Typeface.create(base, if (weight >= 600) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    fun initCustomTypefaces(oxanium: Typeface?, spaceGrotesk: Typeface?, manrope: Typeface?) {
        oxaniumTypeface = oxanium
        spaceGroteskTypeface = spaceGrotesk
        manropeTypeface = manrope
        oxaniumBoldTypeface = oxanium?.let { Typeface.create(it, Typeface.BOLD) }
        oxaniumNormalTypeface = oxanium?.let { Typeface.create(it, Typeface.NORMAL) }
        oxaniumMediumTypeface = oxanium?.let { createWeightedTypeface(it, 500) }
        spaceGroteskBoldTypeface = spaceGrotesk?.let { Typeface.create(it, Typeface.BOLD) }
        spaceGroteskExtraBoldTypeface = spaceGrotesk?.let { createWeightedTypeface(it, 800) }
    }

    fun requireSpaceGrotesk(): Typeface {
        val face = spaceGroteskTypeface
        if (com.moonsolstudios.kavvoro.BuildConfig.DEBUG) {
            checkNotNull(face) {
                "Space Grotesk must be loaded for Home UI"
            }
        }
        return face ?: Typeface.DEFAULT
    }

    fun requireOxanium(): Typeface {
        val face = oxaniumTypeface
        if (com.moonsolstudios.kavvoro.BuildConfig.DEBUG) {
            checkNotNull(face) {
                "Oxanium must be loaded for Home UI"
            }
        }
        return face ?: Typeface.DEFAULT
    }

    fun oxaniumBold(): Typeface =
        oxaniumBoldTypeface ?: Typeface.create(requireOxanium(), Typeface.BOLD).also { oxaniumBoldTypeface = it }

    fun oxaniumNormal(): Typeface =
        oxaniumNormalTypeface ?: Typeface.create(requireOxanium(), Typeface.NORMAL).also { oxaniumNormalTypeface = it }

    fun oxaniumMedium(): Typeface =
        oxaniumMediumTypeface ?: createWeightedTypeface(requireOxanium(), 500).also { oxaniumMediumTypeface = it }

    fun spaceGroteskBold(): Typeface =
        spaceGroteskBoldTypeface ?: Typeface.create(requireSpaceGrotesk(), Typeface.BOLD).also { spaceGroteskBoldTypeface = it }

    fun spaceGroteskExtraBold(): Typeface =
        spaceGroteskExtraBoldTypeface ?: createWeightedTypeface(requireSpaceGrotesk(), 800).also { spaceGroteskExtraBoldTypeface = it }

    fun cachedNormalBlur(radius: Float): BlurMaskFilter? {
        val bucket = (radius * 4f + 0.5f).toInt()
        if (bucket <= 0) return null
        blurFilterCache[bucket]?.let { return it }
        if (blurFilterCache.size >= 128) {
            blurFilterCache.clear()
        }
        return BlurMaskFilter(bucket * 0.25f, BlurMaskFilter.Blur.NORMAL).also {
            blurFilterCache[bucket] = it
        }
    }

    val WORLD_ART_RESOURCES = mapOf(
        "bg_menu" to R.drawable.world_bg_menu,
        "bg_mode_select" to R.drawable.bg_mode_select_rift,
        "bg_language" to R.drawable.bg_language,
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
        "ui_music" to R.drawable.ui_icon_music,
        "home_background" to R.drawable.home_bg_cosmic_clean,
        "bg_space_base" to R.drawable.home_bg_cosmic_clean,
        "bg_space_nebula" to R.drawable.home_bg_cosmic_clean,
        "nebula_overlay" to R.drawable.nebula_overlay,
        "kavvoro_logo" to R.drawable.kavvoro_logo,
        "brainball_main" to R.drawable.brainball_main,
        "portal_beam" to R.drawable.portal_beam,
        "planet_cyan" to R.drawable.planet_blue_ring,
        "planet_magenta" to R.drawable.planet_pink_wink,
        "ic_stat_flame_3d" to R.drawable.ic_stat_flame_3d,
        "ic_stat_level_3d" to R.drawable.ic_stat_level_3d,
        "ic_stat_hype_3d" to R.drawable.ic_stat_hype_3d,
        "btn_settings_3d" to R.drawable.btn_settings_3d,
        "home_portal_disc" to R.drawable.home_portal_disc,
        "home_planet_blue" to R.drawable.home_planet_blue,
        "home_planet_pink" to R.drawable.home_planet_pink,
        "asteroid_cluster_left" to R.drawable.asteroid_cluster_left,
        "asteroid_cluster_right" to R.drawable.asteroid_cluster_right,
        "home_card_art_skins" to R.drawable.home_card_art_skins,
        "home_card_art_missions" to R.drawable.home_card_art_missions,
        "home_card_art_leaderboard" to R.drawable.home_card_art_leaderboard
    )
    private val brainballBitmaps = mutableMapOf<String, Bitmap>()
    private val worldBitmaps = mutableMapOf<String, Bitmap>()
    private val scaledBackgroundBitmaps = mutableMapOf<String, Bitmap>()

    fun preloadHomeAssets(resources: Resources, context: Context) {
        val keys = listOf(
            "bg_space_base",
            "nebula_overlay",
            "kavvoro_logo",
            "brainball_main",
            "portal_beam",
            "home_planet_blue",
            "home_planet_pink",
            "asteroid_cluster_left",
            "asteroid_cluster_right",
            "home_portal_disc",
            "home_card_art_skins",
            "home_card_art_missions",
            "home_card_art_leaderboard",
            "ic_stat_flame_3d",
            "ic_stat_level_3d",
            "ic_stat_hype_3d",
            "btn_settings_3d"
        )
        for (k in keys) {
            worldBitmap(k, resources, context)
        }
    }

    fun brainballBitmap(
        skin: BallSkin,
        resources: Resources,
        artResources: Map<String, Int> = BallSkinCatalog.ART_RESOURCES
    ): Bitmap? {
        brainballBitmaps[skin.id]?.let { return it }
        val resource = artResources[skin.id] ?: return null
        return BitmapFactory.decodeResource(resources, resource)?.also { brainballBitmaps[skin.id] = it }
    }

    fun worldBitmap(
        key: String,
        resources: Resources,
        context: Context,
        artResources: Map<String, Int> = WORLD_ART_RESOURCES
    ): Bitmap? {
        worldBitmaps[key]?.let { return it }
        val resource = artResources[key] ?: return null
        var bitmap = if (key == "home_background" || key == "bg_space_base" || key == "nebula_overlay" || key == "brainball_main" || key == "kavvoro_logo" || key.startsWith("planet_") || key.startsWith("portal_") || key.startsWith("asteroid_") || key.startsWith("spark_") || key.startsWith("graffiti_") || key.startsWith("asteroids_") || key.startsWith("home_")) {
            BitmapFactory.decodeResource(
                resources,
                resource,
                BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inScaled = false
                }
            )
        } else if (key.startsWith("bg_")) {
            BitmapFactory.decodeResource(
                resources,
                resource,
                BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inScaled = false
                }
            )
        } else {
            BitmapFactory.decodeResource(resources, resource)
        }

        if (bitmap == null) {
            val drawable = ContextCompat.getDrawable(context, resource)
            if (drawable != null) {
                val density = resources.displayMetrics.density.coerceAtLeast(1f)
                val baseWidth = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else (420f * density).toInt()
                val baseHeight = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else (100f * density).toInt()
                bitmap = Bitmap.createBitmap(
                    baseWidth.coerceAtLeast(420),
                    baseHeight.coerceAtLeast(100),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        }

        return bitmap?.also { worldBitmaps[key] = it }
    }

    private val centerCropScratch = RectF()
    private var lastBgKey: String? = null
    private var lastBgWidth: Int = -1
    private var lastBgHeight: Int = -1
    private var lastBgBitmap: Bitmap? = null

    fun backgroundBitmap(
        key: String,
        viewWidth: Int,
        viewHeight: Int,
        isLowProfile: Boolean,
        resources: Resources,
        context: Context,
        artResources: Map<String, Int> = WORLD_ART_RESOURCES
    ): Bitmap? {
        val cached = lastBgBitmap
        if (cached != null && !cached.isRecycled && key == lastBgKey && viewWidth == lastBgWidth && viewHeight == lastBgHeight) {
            return cached
        }
        val cacheKey = "$key:${viewWidth}x$viewHeight"
        scaledBackgroundBitmaps[cacheKey]?.takeIf { !it.isRecycled }?.let {
            lastBgKey = key
            lastBgWidth = viewWidth
            lastBgHeight = viewHeight
            lastBgBitmap = it
            return it
        }
        val source = worldBitmap(key, resources, context, artResources) ?: return null
        return try {
            Bitmap.createScaledBitmap(source, viewWidth, viewHeight, !isLowProfile)
                .also {
                    scaledBackgroundBitmaps[cacheKey] = it
                    lastBgKey = key
                    lastBgWidth = viewWidth
                    lastBgHeight = viewHeight
                    lastBgBitmap = it
                }
        } catch (_: OutOfMemoryError) {
            source
        } catch (_: IllegalArgumentException) {
            source
        }
    }

    fun recycleScaledBackgrounds() {
        lastBgBitmap = null
        lastBgKey = null
        scaledBackgroundBitmaps.values.forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        scaledBackgroundBitmaps.clear()
    }

    fun drawWorldAsset(
        canvas: Canvas,
        key: String,
        bounds: RectF,
        alpha: Int = 255,
        paint: Paint,
        resources: Resources,
        context: Context,
        artResources: Map<String, Int> = WORLD_ART_RESOURCES
    ) {
        val bitmap = worldBitmap(key, resources, context, artResources) ?: return
        paint.alpha = alpha.coerceIn(0, 255)
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, null, bounds, paint)
        paint.alpha = 255
    }

    fun drawCenterCrop(
        canvas: Canvas,
        bitmap: Bitmap,
        target: RectF,
        paint: Paint
    ) {
        val scale = max(
            target.width() / bitmap.width.toFloat(),
            target.height() / bitmap.height.toFloat()
        )
        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale
        val left = target.centerX() - scaledWidth / 2f
        val top = target.centerY() - scaledHeight / 2f
        centerCropScratch.set(left, top, left + scaledWidth, top + scaledHeight)

        canvas.save()
        canvas.clipRect(target)
        canvas.drawBitmap(bitmap, null, centerCropScratch, paint)
        canvas.restore()
    }

    fun powerIconKey(power: BallPower): String = when (power) {
        BallPower.PRISM_SHIELD -> "boost_prism"
        BallPower.VOID_PHASE, BallPower.MINOR_PHASE -> "boost_void"
        BallPower.CHROME_RICOCHET, BallPower.MINOR_RICOCHET -> "boost_rebound"
        BallPower.PLASMA_SURGE, BallPower.MINOR_SURGE -> "boost_plasma"
        BallPower.NONE -> "boost_rift_pull"
    }
}
