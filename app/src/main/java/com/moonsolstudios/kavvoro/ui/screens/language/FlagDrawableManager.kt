package com.moonsolstudios.kavvoro.ui.screens.language

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import kotlin.math.max

/**
 * Official VectorDrawable manager for language flags.
 * Loads authentic SVG/VectorDrawable assets with centerCrop aspect-fit,
 * 4px rounded clip, and 1px dark contrast border.
 */
object FlagDrawableManager {

    private val drawableCache = mutableMapOf<KavvoroLanguage, Drawable>()
    private val clipPath = Path()

    fun getDrawable(context: Context, language: KavvoroLanguage): Drawable? {
        drawableCache[language]?.let { return it }
        val resId = when (language) {
            KavvoroLanguage.EN -> R.drawable.flag_en
            KavvoroLanguage.RO -> R.drawable.flag_ro
            KavvoroLanguage.ES -> R.drawable.flag_es
            KavvoroLanguage.FR -> R.drawable.flag_fr
            KavvoroLanguage.DE -> R.drawable.flag_de
            KavvoroLanguage.IT -> R.drawable.flag_it
            KavvoroLanguage.PT -> R.drawable.flag_pt
            KavvoroLanguage.NL -> R.drawable.flag_nl
            KavvoroLanguage.PL -> R.drawable.flag_pl
            KavvoroLanguage.CS -> R.drawable.flag_cs
            KavvoroLanguage.SV -> R.drawable.flag_sv
            KavvoroLanguage.FI -> R.drawable.flag_fi
            KavvoroLanguage.TR -> R.drawable.flag_tr
            KavvoroLanguage.RU -> R.drawable.flag_ru
            KavvoroLanguage.UK -> R.drawable.flag_uk
            KavvoroLanguage.AR -> R.drawable.flag_ar
            KavvoroLanguage.HI -> R.drawable.flag_hi
            KavvoroLanguage.TH -> R.drawable.flag_th
            KavvoroLanguage.ID -> R.drawable.flag_id
            KavvoroLanguage.VI -> R.drawable.flag_vi
            KavvoroLanguage.JA -> R.drawable.flag_ja
            KavvoroLanguage.KO -> R.drawable.flag_ko
            KavvoroLanguage.ZH -> R.drawable.flag_zh
            KavvoroLanguage.ZH_TW -> R.drawable.flag_zh_tw
            else -> R.drawable.flag_en
        }
        val drawable = try {
            ContextCompat.getDrawable(context, resId)
        } catch (_: Throwable) {
            null
        }
        if (drawable != null) {
            drawableCache[language] = drawable
        }
        return drawable
    }

    /**
     * Renders the authentic vector flag into [targetRect] using centerCrop,
     * clipped to a 4px rounded rectangle, with a 1px dark border.
     */
    fun drawFlag(
        canvas: Canvas,
        targetRect: RectF,
        language: KavvoroLanguage,
        context: Context?,
        paint: Paint,
        visualScale: Float
    ) {
        val cornerRadius = 4f * visualScale
        clipPath.reset()
        clipPath.addRoundRect(targetRect, cornerRadius, cornerRadius, Path.Direction.CW)

        // Dark underlay
        paint.style = Paint.Style.FILL
        paint.color = 0xFF040710.toInt()
        canvas.drawPath(clipPath, paint)

        val drawable = if (context != null) getDrawable(context, language) else null
        if (drawable != null) {
            canvas.save()
            canvas.clipPath(clipPath)

            val dw = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.toFloat() else 60f
            val dh = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.toFloat() else 40f
            val scale = max(targetRect.width() / dw, targetRect.height() / dh)
            val renderW = dw * scale
            val renderH = dh * scale
            val renderLeft = targetRect.centerX() - renderW * 0.5f
            val renderTop = targetRect.centerY() - renderH * 0.5f

            drawable.setBounds(
                renderLeft.toInt(),
                renderTop.toInt(),
                (renderLeft + renderW).toInt(),
                (renderTop + renderH).toInt()
            )
            drawable.draw(canvas)
            canvas.restore()
        }

        // 1px dark crisp border (50% alpha)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * visualScale
        paint.color = 0x80000000.toInt()
        canvas.drawPath(clipPath, paint)
    }
}
