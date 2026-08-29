package com.moonsolstudios.kavvoro.ui.render

import android.graphics.*
import kotlin.math.*

object RiftAssistRenderer {

    private val scratch = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    fun drawDrawingAssist(
        canvas: Canvas,
        isSimulating: Boolean,
        riftActive: Boolean,
        anchorScreenX: Float?,
        anchorScreenY: Float?,
        ballScreenX: Float,
        ballScreenY: Float,
        skinLineColor: Int,
        riftEnergy: Float,
        stateElapsed: Float,
        riftHoldSeconds: Float,
        hasFocusField: Boolean,
        hasPowerHold: Boolean,
        hasOverheat: Boolean,
        hasRiftWind: Boolean,
        hasPulseStorm: Boolean,
        levelIndex: Int,
        simElapsed: Float,
        viewWidth: Float,
        viewHeight: Float,
        performanceLite: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (!isSimulating || !riftActive) return
        val cx = anchorScreenX ?: return
        val cy = anchorScreenY ?: return
        val ballX = ballScreenX
        val ballY = ballScreenY
        val danger = riftEnergy < 0.22f
        val accent = if (danger) 0xFFFF5757.toInt() else skinLineColor
        val pulse = 0.65f + 0.35f * sin(stateElapsed * 8.5f)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 15f * dp
        paint.color = withAlpha(accent, 42)
        canvas.drawLine(ballX, ballY, cx, cy, paint)
        paint.strokeWidth = 5f * dp
        paint.color = withAlpha(accent, 210)
        canvas.drawLine(ballX, ballY, cx, cy, paint)
        paint.strokeWidth = 1.2f * dp
        paint.color = 0xDDF7F4FF.toInt()
        canvas.drawLine(ballX, ballY, cx, cy, paint)

        if (hasFocusField) {
            paint.strokeWidth = 2.4f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 150)
            canvas.drawCircle(ballX, ballY, (28f + pulse * 5f) * dp, paint)
            paint.strokeWidth = 9f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 28)
            canvas.drawCircle(ballX, ballY, (28f + pulse * 5f) * dp, paint)
        }

        paint.strokeWidth = 2.2f * dp
        paint.color = withAlpha(accent, 190)
        val powerGrowth = if (hasPowerHold || hasOverheat) {
            min(1f, riftHoldSeconds / 0.9f) * 9f
        } else {
            0f
        }
        canvas.drawCircle(cx, cy, (13f + pulse * 6f + powerGrowth) * dp, paint)
        paint.strokeWidth = 1.1f * dp
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 130)
        canvas.drawCircle(cx, cy, 4.5f * dp, paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        repeat(if (performanceLite) 0 else 6) { i ->
            val angle = stateElapsed * 5.2f + i * PI.toFloat() * 2f / 6f
            val distance = (15f + (i % 3) * 4f) * dp * (0.75f + pulse * 0.25f)
            paint.color = withAlpha(if (i % 2 == 0) accent else 0xFFFFCF4A.toInt(), 120)
            canvas.drawCircle(cx + cos(angle) * distance, cy + sin(angle) * distance, 1.7f * dp, paint)
        }

        val energy = (riftEnergy * 100f).roundToInt().coerceIn(0, 100)
        val holdMode = when {
            hasFocusField -> t("FOCUS").uppercase()
            hasPowerHold -> "${t("POWER").uppercase()} ${(min(1f, riftHoldSeconds / 0.9f) * 100).roundToInt()}%"
            hasOverheat -> "${t("HEAT").uppercase()} ${(min(1f, riftHoldSeconds / 1.0f) * 100).roundToInt()}%"
            hasRiftWind -> t("WIND GUARD").uppercase()
            hasPulseStorm -> t("PULSE GUARD").uppercase()
            levelIndex <= 3 && simElapsed < 4.2f -> t("TAP TO PULL").uppercase()
            else -> ""
        }
        val label = if (holdMode.isBlank()) "${t("RIFT").uppercase()} $energy%" else "${t("RIFT").uppercase()} $energy%   $holdMode"
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 10f * dp
        val chipWidth = (textPaint.measureText(label) + 24f * dp).coerceIn(94f * dp, 194f * dp)
        val chipHeight = 30f * dp
        val chipLeft = (cx - chipWidth * 0.5f).coerceIn(12f * dp, viewWidth - 12f * dp - chipWidth)
        val chipTop = (cy - 58f * dp).coerceIn(112f * dp, viewHeight - 104f * dp)
        scratch.set(chipLeft, chipTop, chipLeft + chipWidth, chipTop + chipHeight)
        paint.style = Paint.Style.FILL
        paint.color = 0xE607090F.toInt()
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)
        paint.color = withAlpha(accent, 58)
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, scratch.width() - 12f * dp), scratch.centerX(), scratch.centerY() + 3.6f * dp, textPaint)
    }
}
