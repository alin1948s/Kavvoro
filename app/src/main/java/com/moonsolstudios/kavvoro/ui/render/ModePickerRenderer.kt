package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface

/**
 * Procedural renderer for the Mode Picker screen (Classic vs Chaos mode cards and actions).
 */
object ModePickerRenderer {

    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawModeCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        description: String,
        accent: Int,
        active: Boolean,
        activeRun: Boolean,
        levelText: String,
        streakText: String,
        activeRunLabel: String,
        noActiveRunLabel: String,
        startFreshLabel: String,
        continueLabel: String,
        newGameLabel: String,
        startLabel: String,
        compact: Boolean,
        short: Boolean,
        continueButton: RectF,
        newGameButton: RectF,
        startButton: RectF,
        activeButtonId: Int,
        continueButtonId: Int,
        newGameButtonId: Int,
        startButtonId: Int,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawIcon: (Canvas, RectF) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(withAlpha(accent, if (active) 60 else 32), 0xE0060C15.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, 10f * dp, 10f * dp, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.8f * dp else 1f * dp
        paint.color = withAlpha(accent, if (active) 240 else 180)
        canvas.drawRoundRect(rect, 10f * dp, 10f * dp, paint)

        val iconSize = (if (short) 31f else if (compact) 42f else 58f) * dp
        scratchRect.set(
            rect.left + (if (short) 14f else 24f) * dp,
            rect.top + (if (short) 16f else 26f) * dp,
            rect.left + (if (short) 14f else 24f) * dp + iconSize,
            rect.top + (if (short) 16f else 24f) * dp + iconSize
        )
        drawIcon(canvas, scratchRect)

        val textLeft = rect.left + (if (short) 56f else if (compact) 84f else 106f) * dp
        val titleWidth = rect.right - textLeft - 18f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (short) 15f else if (compact) 20f else 27f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(title, titleWidth), textLeft, rect.top + (if (short) 39f else if (compact) 56f else 70f) * dp, textPaint)

        textPaint.textSize = (if (short) 7f else if (compact) 9f else 11f) * dp
        textPaint.color = 0xB8D2DCE8.toInt()
        canvas.drawText(fitText(description, titleWidth), textLeft, rect.top + (if (short) 54f else if (compact) 76f else 94f) * dp, textPaint)

        if (activeRun) {
            textPaint.textSize = (if (short) 7f else if (compact) 8f else 10f) * dp
            textPaint.color = accent
            canvas.drawText(activeRunLabel, rect.left + (if (short) 14f else 28f) * dp, rect.top + (if (short) 60f else if (compact) 76f else 108f) * dp, textPaint)

            textPaint.textSize = (if (short) 14f else if (compact) 18f else 24f) * dp
            textPaint.color = 0xFFF7F4FF.toInt()
            canvas.drawText(levelText, rect.left + (if (short) 14f else 28f) * dp, rect.top + (if (short) 78f else if (compact) 96f else 132f) * dp, textPaint)

            textPaint.textSize = (if (short) 7f else if (compact) 8f else 10f) * dp
            textPaint.color = 0x99FFFFFF.toInt()
            canvas.drawText("BEST STREAK", rect.centerX() + (if (short) 4f else 20f) * dp, rect.top + (if (short) 60f else if (compact) 76f else 108f) * dp, textPaint)

            textPaint.textSize = (if (short) 14f else if (compact) 18f else 24f) * dp
            textPaint.color = 0xFFF7F4FF.toInt()
            canvas.drawText(streakText, rect.centerX() + (if (short) 4f else 20f) * dp, rect.top + (if (short) 78f else if (compact) 96f else 132f) * dp, textPaint)

            if (!continueButton.isEmpty && !newGameButton.isEmpty) {
                drawActionButton(canvas, continueButton, continueLabel, accent, filled = true, active = activeButtonId == continueButtonId, compact = compact, short = short, paint = paint, dp = dp, fitText = fitText)
                drawActionButton(canvas, newGameButton, newGameLabel, accent, filled = false, active = activeButtonId == newGameButtonId, compact = compact, short = short, paint = paint, dp = dp, fitText = fitText)
            } else if (!startButton.isEmpty) {
                drawActionButton(canvas, startButton, continueLabel, accent, filled = true, active = activeButtonId == startButtonId, compact = compact, short = short, paint = paint, dp = dp, fitText = fitText)
            }
        } else {
            textPaint.textSize = (if (short) 7f else if (compact) 8f else 10f) * dp
            textPaint.color = accent
            canvas.drawText(noActiveRunLabel, rect.left + (if (short) 14f else 28f) * dp, rect.top + (if (short) 60f else if (compact) 76f else 108f) * dp, textPaint)

            textPaint.textSize = (if (short) 9f else if (compact) 11f else 14f) * dp
            textPaint.color = 0xB8D2DCE8.toInt()
            canvas.drawText(fitText(startFreshLabel, rect.width() - (if (short) 28f else 56f) * dp), rect.left + (if (short) 14f else 28f) * dp, rect.top + (if (short) 76f else if (compact) 96f else 132f) * dp, textPaint)

            drawActionButton(canvas, startButton, startLabel, accent, filled = true, active = activeButtonId == startButtonId, compact = compact, short = short, paint = paint, dp = dp, fitText = fitText)
        }
    }

    fun drawActionButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        accent: Int,
        filled: Boolean,
        active: Boolean,
        compact: Boolean,
        short: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        if (rect.isEmpty) return
        paint.style = Paint.Style.FILL
        paint.color = if (filled) withAlpha(accent, if (active) 122 else 84) else 0x2A0F1622
        canvas.drawRoundRect(rect, 7f * dp, 7f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.7f * dp else 0.9f * dp
        paint.color = withAlpha(accent, if (active) 250 else 185)
        canvas.drawRoundRect(rect, 7f * dp, 7f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (short) 8f else if (compact) 9f else 11f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, rect.width() - 10f * dp), rect.centerX(), rect.centerY() + 4f * dp, textPaint)
    }

    fun drawBackButton(canvas: Canvas, rect: RectF, label: String, short: Boolean, paint: Paint, dp: Float) {
        paint.style = Paint.Style.FILL
        paint.color = 0xB00A111B.toInt()
        canvas.drawRoundRect(rect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = 0x668AA6FF.toInt()
        canvas.drawRoundRect(rect, 8f * dp, 8f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (short) 9f else 11f) * dp
        textPaint.color = 0xB8D2DCE8.toInt()
        canvas.drawText(label, rect.centerX(), rect.centerY() + 4f * dp, textPaint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
