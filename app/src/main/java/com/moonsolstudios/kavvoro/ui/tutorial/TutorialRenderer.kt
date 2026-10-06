package com.moonsolstudios.kavvoro.ui.tutorial

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.min

/**
 * Dedicated renderer for tutorial HUD hints, cards, and interactive start buttons.
 */
object TutorialRenderer {

    private val scratch = RectF()

    fun drawTutorialHint(
        canvas: Canvas,
        tutorialCardVisible: Boolean,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        accent: Int,
        levelIndex: Int,
        tutorialLastLevel: Int,
        lessonLines: List<String>,
        tutorialIconKey: String,
        actionLabel: String,
        actionPressed: Boolean,
        isArabic: Boolean,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit
    ) {
        if (!tutorialCardVisible) {
            tutorialCardBounds.setEmpty()
            tutorialStartButton.setEmpty()
            return
        }

        val width = min(viewWidth - dp * 36f, dp * 430f)
        val height = dp * 184f
        val left = viewWidth * 0.5f - width * 0.5f
        val top = viewHeight - height - dp * 34f
        tutorialCardBounds.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xF407090F.toInt()
        canvas.drawRoundRect(tutorialCardBounds, dp * 8f, dp * 8f, paint)
        paint.shader = LinearGradient(
            left, top, left + width, top + height,
            intArrayOf(withAlpha(accent, 54), 0x0007090F),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tutorialCardBounds, dp * 8f, dp * 8f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1.2f
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(tutorialCardBounds, dp * 8f, dp * 8f, paint)

        // Specular top highlight line
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + dp * 12f, top + dp * 1f, left + width - dp * 12f, top + dp * 1f, paint)

        val iconSize = dp * 42f
        scratch.set(left + dp * 11f, top + dp * 16f, left + dp * 11f + iconSize, top + dp * 16f + iconSize)
        drawWorldAsset(canvas, tutorialIconKey, scratch, 235)

        val textX = if (isArabic) tutorialCardBounds.right - dp * 14f else left + dp * 62f
        val textMaxWidth = width - dp * 76f
        textPaint.textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 10f
        textPaint.color = accent
        val tutorialHeader = if (levelIndex <= 10) {
            "${t("TRAINING").uppercase()} $levelIndex/10  /  $actionLabel"
        } else {
            "${t("RIFT MODULE").uppercase()} L${levelIndex.toString().padStart(2, '0')}  /  ${t("PORTAL").uppercase()}"
        }
        drawFittedText(canvas, tutorialHeader, textX, top + dp * 21f, textMaxWidth, 10f, 7.2f)

        textPaint.textSize = dp * 11f
        textPaint.color = 0xEFFFFFFF.toInt()
        val visibleLessonLines = lessonLines.take(4)
        val widestLessonLine = visibleLessonLines.maxOfOrNull(textPaint::measureText) ?: 0f
        textPaint.textSize = dp * TutorialCardLayout.fittedTextSize(
            startSize = 11f,
            minSize = 7.2f,
            maxWidth = textMaxWidth,
            maxMeasuredWidth = widestLessonLine
        )
        visibleLessonLines.forEachIndexed { index, line ->
            canvas.drawText(fitText(line, textMaxWidth), textX, top + dp * (40f + index * 15f), textPaint)
        }

        paint.style = Paint.Style.FILL
        textPaint.textAlign = if (isArabic) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 8.2f
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), 225)
        val footer = if (levelIndex < tutorialLastLevel) {
            "${t("NO ADS IN TRAINING").uppercase()}  /  ${t("L10 UNLOCKS VORO GRAD").uppercase()}"
        } else {
            t("TRAINING REWARD READY").uppercase()
        }
        canvas.drawText(fitText(footer, textMaxWidth), textX, top + dp * 103f, textPaint)

        val actionBounds = TutorialCardLayout.centeredHorizontalBounds(
            cardLeft = tutorialCardBounds.left,
            cardRight = tutorialCardBounds.right,
            padding = dp * 14f
        )
        paint.style = Paint.Style.FILL
        paint.color = 0x22FFFFFF
        canvas.drawRoundRect(
            actionBounds.left,
            top + dp * 116f,
            actionBounds.right,
            top + dp * 117.5f,
            dp * 1f,
            dp * 1f,
            paint
        )

        tutorialStartButton.set(
            actionBounds.left,
            top + dp * 126f,
            actionBounds.right,
            top + dp * 170f
        )
        drawTutorialStartButton(
            canvas = canvas,
            tutorialStartButton = tutorialStartButton,
            accent = accent,
            actionPressed = actionPressed,
            dp = dp,
            paint = paint,
            textPaint = textPaint,
            t = t,
            drawFittedText = drawFittedText
        )
    }

    fun drawTutorialStartButton(
        canvas: Canvas,
        tutorialStartButton: RectF,
        accent: Int,
        actionPressed: Boolean,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            tutorialStartButton.left,
            tutorialStartButton.top,
            tutorialStartButton.right,
            tutorialStartButton.bottom,
            intArrayOf(
                withAlpha(accent, if (actionPressed) 255 else 232),
                withAlpha(accent, if (actionPressed) 180 else 132)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tutorialStartButton, dp * 7f, dp * 7f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * if (actionPressed) 1.8f else 1.1f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (actionPressed) 235 else 175)
        canvas.drawRoundRect(tutorialStartButton, dp * 7f, dp * 7f, paint)

        // Specular top highlight line
        paint.color = 0x55FFFFFF
        canvas.drawLine(
            tutorialStartButton.left + dp * 8f,
            tutorialStartButton.top + dp * 1f,
            tutorialStartButton.right - dp * 8f,
            tutorialStartButton.top + dp * 1f,
            paint
        )

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.color = 0xFFF7F4FF.toInt()
        drawFittedText(
            canvas,
            t("START LEVEL").uppercase(),
            tutorialStartButton.centerX(),
            tutorialStartButton.centerY() + dp * 4f,
            tutorialStartButton.width() - dp * 20f,
            12f,
            8f
        )
    }

    fun tutorialIconKey(
        level: com.moonsolstudios.kavvoro.engine.LevelSpec,
        hasCurse: (com.moonsolstudios.kavvoro.engine.CurseType) -> Boolean
    ): String {
        return when {
            level.portals.isNotEmpty() -> "portal_goal"
            level.hazards.isNotEmpty() -> "hazard_glitch"
            hasCurse(com.moonsolstudios.kavvoro.engine.CurseType.OVERHEAT) -> "danger_beacon"
            hasCurse(com.moonsolstudios.kavvoro.engine.CurseType.PULSE_STORM) || level.pulseZones.isNotEmpty() -> "boost_pulse"
            hasCurse(com.moonsolstudios.kavvoro.engine.CurseType.POWER_HOLD) -> "boost_plasma"
            hasCurse(com.moonsolstudios.kavvoro.engine.CurseType.FOCUS_FIELD) -> "boost_recharge"
            else -> "boost_rift_pull"
        }
    }
}
