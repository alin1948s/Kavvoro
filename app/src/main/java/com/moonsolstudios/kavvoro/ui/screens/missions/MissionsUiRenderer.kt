package com.moonsolstudios.kavvoro.ui.screens.missions

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.DailyMissionProgress
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.min

object MissionsUiRenderer {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arrowPath = Path()
    private val scratchBackRect = RectF()
    private val scratchTextRect = RectF()
    private val scratchCardRect = RectF()
    private val scratchGlowRect = RectF()
    private val scratchTrackRect = RectF()
    private val scratchFillRect = RectF()
    private val scratchButtonRect = RectF()
    private val scratchShadowRect = RectF()
    private val scratchFaceRect = RectF()

    fun drawScreen(
        canvas: Canvas,
        layout: MissionsLayoutCalculator,
        missions: List<DailyMissionProgress>,
        activeClaimIndex: Int,
        dp: Float,
        t: (String) -> String
    ) {
        drawHeader(canvas, layout, dp, t)
        missions.forEachIndexed { index, mission ->
            val card = layout.cardRects.getOrNull(index) ?: return@forEachIndexed
            drawMissionCard(
                canvas = canvas,
                layout = layout,
                mission = mission,
                index = index,
                active = activeClaimIndex == index,
                dp = dp,
                t = t
            )
        }
    }

    private fun drawHeader(canvas: Canvas, layout: MissionsLayoutCalculator, dp: Float, t: (String) -> String) {
        val back = layout.backButtonRect.toRectF(scratchBackRect)
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = 0xCC0A1530.toInt()
        canvas.drawRoundRect(back, 14f * dp, 14f * dp, shapePaint)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1.4f * dp
        shapePaint.color = withAlpha(KavvoroPalette.cyan, 185)
        canvas.drawRoundRect(back, 14f * dp, 14f * dp, shapePaint)

        arrowPath.reset()
        arrowPath.moveTo(back.centerX() + 6f * dp, back.centerY() - 9f * dp)
        arrowPath.lineTo(back.centerX() - 4f * dp, back.centerY())
        arrowPath.lineTo(back.centerX() + 6f * dp, back.centerY() + 9f * dp)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 2.8f * dp
        shapePaint.strokeCap = Paint.Cap.ROUND
        shapePaint.strokeJoin = Paint.Join.ROUND
        shapePaint.color = KavvoroPalette.cyan
        canvas.drawPath(arrowPath, shapePaint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.textSize = 19f * dp
        val title = t("MISSIONS & REWARDS").uppercase()
        drawFitted(canvas, title, layout.titleRect.toRectF(scratchTextRect), textPaint, 12f * dp)

        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.color = KavvoroPalette.mutedText
        textPaint.textSize = 10.5f * dp
        textPaint.letterSpacing = 0.02f
        drawFitted(canvas, t("COMPLETE DAILY MISSIONS & WIN").uppercase(), layout.subtitleRect.toRectF(scratchTextRect), textPaint, 8f * dp)
        textPaint.letterSpacing = 0f
    }

    private fun drawMissionCard(
        canvas: Canvas,
        layout: MissionsLayoutCalculator,
        mission: DailyMissionProgress,
        index: Int,
        active: Boolean,
        dp: Float,
        t: (String) -> String
    ) {
        val cardLayout = layout.cardRects[index]
        if (cardLayout.isEmpty()) return
        val card = cardLayout.toRectF(scratchCardRect)
        val accent = when (index % 3) {
            0 -> KavvoroPalette.cyan
            1 -> KavvoroPalette.pink
            else -> KavvoroPalette.gold
        }
        val radius = 18f * dp

        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(
            card.left, card.top, card.right, card.bottom,
            0xF30B1531.toInt(), 0xE80A1026.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(card, radius, radius, shapePaint)
        shapePaint.shader = null

        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = if (mission.canClaim) 1.8f * dp else 1.1f * dp
        shapePaint.color = withAlpha(if (mission.isComplete) KavvoroPalette.gold else accent, if (mission.canClaim) 225 else 118)
        canvas.drawRoundRect(card, radius, radius, shapePaint)

        shapePaint.style = Paint.Style.FILL
        shapePaint.color = withAlpha(accent, 30)
        scratchGlowRect.set(card.left, card.top, card.left + card.width() * 0.55f, card.bottom)
        val glow = scratchGlowRect
        canvas.drawRoundRect(glow, radius, radius, shapePaint)

        val numberRadius = 14f * dp
        val numberCenterX = if (layout.gridLayout) card.centerX() else card.left + 34f * dp
        val numberCenterY = if (layout.gridLayout) card.top + 38f * dp else card.top + 34f * dp
        shapePaint.color = withAlpha(accent, if (mission.isComplete) 245 else 155)
        canvas.drawCircle(numberCenterX, numberCenterY, numberRadius, shapePaint)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 10f * dp
        textPaint.color = 0xFF071020.toInt()
        canvas.drawText((index + 1).toString().padStart(2, '0'), numberCenterX, numberCenterY + 3.5f * dp, textPaint)

        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.LEFT
        textPaint.color = 0xFFF5F7FF.toInt()
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = if (layout.gridLayout) 15f * dp else 15.5f * dp
        val titleRect = layout.titleTextRects[index].toRectF(scratchTextRect)
        drawFitted(canvas, t(mission.id.titleKey).uppercase(), titleRect, textPaint, 8.5f * dp)

        textPaint.color = KavvoroPalette.gold
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.RIGHT
        textPaint.textSize = 11.5f * dp
        val rewardRect = layout.rewardTextRects[index].toRectF(scratchTextRect)
        drawFitted(canvas, "+${mission.rewardCoins} ${t("COINS").uppercase()}", rewardRect, textPaint, 8.5f * dp)

        val progressLabel = "${mission.progress} / ${mission.target}"
        val progressRect = layout.progressTextRects[index].toRectF(scratchTextRect)
        textPaint.color = if (mission.isComplete) KavvoroPalette.gold else KavvoroPalette.mutedText
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.textSize = 10f * dp
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.LEFT
        canvas.drawText(progressLabel, progressRect.centerX(), baselineFor(progressRect, textPaint), textPaint)

        val trackLayout = layout.progressTrackRects[index]
        if (!trackLayout.isEmpty()) {
            val track = trackLayout.toRectF(scratchTrackRect)
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = 0xAA020817.toInt()
            canvas.drawRoundRect(track, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
            val fraction = (mission.progress.toFloat() / mission.target.coerceAtLeast(1)).coerceIn(0f, 1f)
            if (fraction > 0f) {
                scratchFillRect.set(track.left, track.top, track.left + track.width() * fraction, track.bottom)
                val fill = scratchFillRect
                shapePaint.shader = LinearGradient(fill.left, fill.top, fill.right.coerceAtLeast(fill.left + 1f), fill.bottom,
                    intArrayOf(accent, if (mission.isComplete) KavvoroPalette.gold else KavvoroPalette.blue), null, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(fill, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
                shapePaint.shader = null
            }
        }

        val button = layout.claimButtonRects[index].toRectF(scratchButtonRect)
        val buttonRadius = 11f * dp
        val pressedOffset = if (active && mission.canClaim) 2f * dp else 0f
        val buttonColor = when {
            mission.claimed -> 0x5529D7C5
            mission.canClaim -> 0xFF00CBBF.toInt()
            else -> 0x6635425E
        }
        shapePaint.color = if (mission.canClaim) 0xCC03232D.toInt() else 0x66030A18
        scratchShadowRect.set(button.left, button.top + 3f * dp, button.right, button.bottom + 3f * dp)
        val shadow = scratchShadowRect
        canvas.drawRoundRect(shadow, buttonRadius, buttonRadius, shapePaint)
        scratchFaceRect.set(button.left, button.top + pressedOffset, button.right, button.bottom - 3f * dp + pressedOffset)
        val face = scratchFaceRect
        shapePaint.shader = if (mission.canClaim) {
            LinearGradient(face.left, face.top, face.right, face.bottom, intArrayOf(0xFF20E5CB.toInt(), 0xFF1185B5.toInt()), null, Shader.TileMode.CLAMP)
        } else null
        if (!mission.canClaim) shapePaint.color = buttonColor
        canvas.drawRoundRect(face, buttonRadius, buttonRadius, shapePaint)
        shapePaint.shader = null
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1f * dp
        shapePaint.color = withAlpha(if (mission.canClaim) KavvoroPalette.cyan else KavvoroPalette.mutedText, 150)
        canvas.drawRoundRect(face, buttonRadius, buttonRadius, shapePaint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = min(10.5f * dp, button.height() * 0.28f)
        textPaint.color = if (mission.canClaim) 0xFFFFFFFF.toInt() else KavvoroPalette.mutedText
        val buttonLabel = if (mission.claimed) t("CLAIMED") else t("CLAIM REWARD")
        drawFitted(canvas, buttonLabel.uppercase(), face, textPaint, 7f * dp)

    }

    private fun baselineFor(rect: RectF, paint: Paint): Float =
        rect.centerY() - (paint.ascent() + paint.descent()) * 0.5f

    private fun drawFitted(canvas: Canvas, value: String, rect: RectF, paint: Paint, minimumTextSize: Float) {
        if (rect.isEmpty) return
        val originalSize = paint.textSize
        val maxWidth = rect.width().coerceAtLeast(1f)
        while (paint.textSize > minimumTextSize && paint.measureText(value) > maxWidth) {
            paint.textSize -= 0.5f
        }
        val x = when (paint.textAlign) {
            Paint.Align.CENTER -> rect.centerX()
            Paint.Align.RIGHT -> rect.right
            else -> rect.left
        }
        canvas.drawText(value, x, baselineFor(rect, paint), paint)
        paint.textSize = originalSize
    }
}
