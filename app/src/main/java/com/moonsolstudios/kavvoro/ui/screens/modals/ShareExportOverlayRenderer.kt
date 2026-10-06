package com.moonsolstudios.kavvoro.ui.screens.modals

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.min
import kotlin.math.sin

/**
 * Dedicated modal overlay renderer for the 9:16 video replay export progress indicator.
 */
object ShareExportOverlayRenderer {

    private val scratch = RectF()

    fun drawExportingOverlay(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        levelAccent: Int,
        menuPulse: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xA407090F.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        val width = min(viewWidth - 34f * dp, 390f * dp)
        val height = 118f * dp
        val left = viewWidth * 0.5f - width * 0.5f
        val top = (viewHeight * 0.5f - height * 0.5f).coerceAtLeast(144f * dp)
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF07090F.toInt()
        canvas.drawRoundRect(scratch, 9f * dp, 9f * dp, paint)
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(withAlpha(levelAccent, 92), 0xFF07090F.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.color = levelAccent
        canvas.drawRect(left, top, left + 4f * dp, top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = levelAccent
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + 8f * dp, top + 1f * dp, left + width - 8f * dp, top + 1f * dp, paint)

        val iconSize = 42f * dp
        scratch.set(left + 16f * dp, top + 16f * dp, left + 16f * dp + iconSize, top + 16f * dp + iconSize)
        drawWorldAsset(canvas, "ui_share", scratch, 245)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 18f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("BUILDING SHORT").uppercase(), left + 70f * dp, top + 31f * dp, textPaint)
        textPaint.textSize = 11f * dp
        textPaint.color = 0xCCFFFFFF.toInt()
        val formatText = fitText(t("9:16 video MP4 / TikTok / Reels / Shorts").uppercase(), width - 86f * dp)
        canvas.drawText(formatText, left + 70f * dp, top + 52f * dp, textPaint)

        val barLeft = left + 70f * dp
        val barRight = left + width - 18f * dp
        val barTop = top + 74f * dp
        val pulse = 0.38f + 0.62f * ((sin(menuPulse * 5.4f) + 1f) * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = 0x36FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + 6f * dp, 4f * dp, 4f * dp, paint)
        paint.color = levelAccent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * pulse, barTop + 6f * dp, 4f * dp, 4f * dp, paint)

        textPaint.textSize = 8.6f * dp
        textPaint.color = 0xB8FFFFFF.toInt()
        canvas.drawText(t("SHARE COUNTS UNLOCK BYTE / KABOOM / 404").uppercase(), barLeft, top + 99f * dp, textPaint)
    }
}
