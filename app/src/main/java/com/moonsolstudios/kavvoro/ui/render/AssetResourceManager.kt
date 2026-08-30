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
import kotlin.math.max

object AssetResourceManager {

    val WORLD_ART_RESOURCES = mapOf(
        "bg_menu" to R.drawable.world_bg_menu,
        "bg_language" to R.drawable.bg_language,
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
        "ui_music" to R.drawable.ui_icon_music,
        "home_background" to R.drawable.home_background,
        "home_portal_back" to R.drawable.home_portal_back,
        "home_platform" to R.drawable.home_platform,
        "home_portal_platform" to R.drawable.home_portal_platform,
        "home_portal_front" to R.drawable.home_portal_front,
        "home_portal_front_fx" to R.drawable.home_portal_front_fx,
        "home_play_cta_frame" to R.drawable.home_play_cta_frame,
        "home_rift_status_frame" to R.drawable.home_rift_status_frame,
        "brand_kavvoro" to R.drawable.brand_kavvoro,
        "lang_header_frame" to R.drawable.lang_header_frame,
        "lang_diamond" to R.drawable.lang_diamond,
        "lang_back_button" to R.drawable.lang_back_button,
        "lang_card_selected" to R.drawable.lang_card_selected,
        "lang_card_left" to R.drawable.lang_card_left,
        "lang_card_right" to R.drawable.lang_card_right,
        "lang_radio_selected" to R.drawable.lang_radio_selected,
        "lang_radio_unselected" to R.drawable.lang_radio_unselected,
        "lang_footer_panel" to R.drawable.lang_footer_panel,
        "flag_badge_en" to R.drawable.flag_badge_en,
        "flag_badge_ro" to R.drawable.flag_badge_ro,
        "flag_badge_es" to R.drawable.flag_badge_es,
        "flag_badge_fr" to R.drawable.flag_badge_fr,
        "flag_badge_de" to R.drawable.flag_badge_de,
        "flag_badge_it" to R.drawable.flag_badge_it,
        "flag_badge_pt" to R.drawable.flag_badge_pt,
        "flag_badge_nl" to R.drawable.flag_badge_nl,
        "flag_badge_pl" to R.drawable.flag_badge_pl,
        "flag_badge_cs" to R.drawable.flag_badge_cs,
        "flag_badge_sv" to R.drawable.flag_badge_sv,
        "flag_badge_fi" to R.drawable.flag_badge_fi,
        "flag_badge_tr" to R.drawable.flag_badge_tr,
        "flag_badge_ru" to R.drawable.flag_badge_ru,
        "flag_badge_uk" to R.drawable.flag_badge_uk,
        "flag_badge_ar" to R.drawable.flag_badge_ar,
        "flag_badge_hi" to R.drawable.flag_badge_hi,
        "flag_badge_th" to R.drawable.flag_badge_th,
        "flag_badge_id" to R.drawable.flag_badge_id,
        "flag_badge_vi" to R.drawable.flag_badge_vi,
        "flag_badge_ja" to R.drawable.flag_badge_ja,
        "flag_badge_ko" to R.drawable.flag_badge_ko,
        "flag_badge_zh" to R.drawable.flag_badge_zh,
        "flag_badge_zh_tw" to R.drawable.flag_badge_zh_tw
    )
    private val brainballBitmaps = mutableMapOf<String, Bitmap>()
    private val worldBitmaps = mutableMapOf<String, Bitmap>()
    private val scaledBackgroundBitmaps = mutableMapOf<String, Bitmap>()

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
        var bitmap = if (key.startsWith("bg_")) {
            BitmapFactory.decodeResource(
                resources,
                resource,
                BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
            )
        } else {
            BitmapFactory.decodeResource(resources, resource)
        }

        if (bitmap == null) {
            val drawable = ContextCompat.getDrawable(context, resource)
            if (drawable != null) {
                bitmap = Bitmap.createBitmap(
                    if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 209,
                    if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 50,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        }

        return bitmap?.also { worldBitmaps[key] = it }
    }

    fun backgroundBitmap(
        key: String,
        viewWidth: Int,
        viewHeight: Int,
        isLowProfile: Boolean,
        resources: Resources,
        context: Context,
        artResources: Map<String, Int> = WORLD_ART_RESOURCES
    ): Bitmap? {
        val cacheKey = "$key:${viewWidth}x$viewHeight"
        scaledBackgroundBitmaps[cacheKey]?.takeIf { !it.isRecycled }?.let { return it }
        val source = worldBitmap(key, resources, context, artResources) ?: return null
        return try {
            Bitmap.createScaledBitmap(source, viewWidth, viewHeight, !isLowProfile)
                .also { scaledBackgroundBitmaps[cacheKey] = it }
        } catch (_: OutOfMemoryError) {
            source
        } catch (_: IllegalArgumentException) {
            source
        }
    }

    fun recycleScaledBackgrounds() {
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
        val destination = RectF(left, top, left + scaledWidth, top + scaledHeight)

        canvas.save()
        canvas.clipRect(target)
        canvas.drawBitmap(bitmap, null, destination, paint)
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
