package com.moonsolstudios.kavvoro.ui.screens.ad

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.AdAction
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.UiWidgetRenderer
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.min
import kotlin.math.sin

/**
 * Dedicated renderer for the Ad Checkpoint / Rewarded Continue screen (Screen.AD).
 */
object AdScreenRenderer {

    private val scratch = RectF()

    fun drawAdScreen(
        canvas: Canvas,
        gameMode: GameMode,
        pendingAdAction: AdAction,
        adReason: String,
        adLoading: Boolean,
        stateElapsed: Float,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        adButton: RectF,
        activeButton: ButtonId,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val accent = if (gameMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
        val panelWidth = min(viewWidth - 32f * dp, 390f * dp)
        val panelHeight = 206f * dp
        val left = viewWidth * 0.5f - panelWidth * 0.5f
        val top = viewHeight * 0.5f - panelHeight * 0.5f
        scratch.set(left, top, left + panelWidth, top + panelHeight)

        paint.style = Paint.Style.FILL
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 170)
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(left, top, left + panelWidth, top + 4f * dp, 3f * dp, 3f * dp, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + 14f * dp, top + 1f * dp, left + panelWidth - 14f * dp, top + 1f * dp, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, 235)
        val headerLine = "${t("STREAK PROTECTION").uppercase()}  /  ${gameMode.menuTitle(t)}"
        canvas.drawText(fitText(headerLine, panelWidth - 74f * dp), left + 20f * dp, top + 28f * dp, textPaint)
        textPaint.textSize = 27f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(t("AD CONTINUE").uppercase(), panelWidth - 74f * dp), left + 20f * dp, top + 59f * dp, textPaint)
        textPaint.textSize = 12f * dp
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText(fitText(adReason, panelWidth - 40f * dp), left + 20f * dp, top + 84f * dp, textPaint)
        textPaint.textSize = 9f * dp
        textPaint.color = 0x99FFFFFF.toInt()
        val adLine = if (pendingAdAction == AdAction.CONTINUE_AFTER_FAIL) {
            t("WATCH TO KEEP THE RUN ALIVE").uppercase()
        } else {
            t("THE RUN RESUMES AFTER THE INTERSTITIAL").uppercase()
        }
        canvas.drawText(fitText(adLine, panelWidth - 40f * dp), left + 20f * dp, top + 105f * dp, textPaint)

        val cx = left + panelWidth - 38f * dp
        val cy = top + 43f * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = withAlpha(accent, 210)
        scratch.set(cx - 13f * dp, cy - 13f * dp, cx + 13f * dp, cy + 13f * dp)
        canvas.drawArc(scratch, -80f, 290f + sin(stateElapsed * 4f) * 30f, false, paint)
        paint.strokeCap = Paint.Cap.BUTT

        AdScreenTouchController.layoutAdButton(viewWidth, viewHeight, dp, adButton)
        UiWidgetRenderer.drawResultActionButton(
            canvas = canvas,
            rect = adButton,
            label = t(if (adLoading) "LOADING" else "CONTINUE WITH AD").uppercase(),
            accent = accent,
            button = ButtonId.AD_CONTINUE,
            activeButton = activeButton,
            dp = dp,
            paint = paint,
            textPaint = textPaint,
            fitText = fitText
        )
    }
}
