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
import java.util.Locale
import kotlin.math.min

object MissionsUiRenderer {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arrowPath = Path()
    private val checkPath = Path()
    private val scratchBackRect = RectF()
    private val scratchTextRect = RectF()
    private val scratchCardRect = RectF()
    private val scratchChipRect = RectF()
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
        if (layout.showSummary) drawSummary(canvas, layout, missions, dp, t)
        missions.forEachIndexed { index, mission ->
            if (layout.cardRects.getOrNull(index)?.isEmpty() != false) return@forEachIndexed
            drawMissionCard(canvas, layout, mission, index, activeClaimIndex == index, dp, t)
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
        drawFitted(canvas, t("MISSIONS & REWARDS").uppercase(), layout.titleRect.toRectF(scratchTextRect), textPaint, 12f * dp)

        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.color = KavvoroPalette.mutedText
        textPaint.textSize = 10.5f * dp
        textPaint.letterSpacing = 0.02f
        drawFitted(canvas, t("COMPLETE DAILY MISSIONS & WIN").uppercase(), layout.subtitleRect.toRectF(scratchTextRect), textPaint, 8f * dp)
        textPaint.letterSpacing = 0f
    }

    private fun drawSummary(
        canvas: Canvas,
        layout: MissionsLayoutCalculator,
        missions: List<DailyMissionProgress>,
        dp: Float,
        t: (String) -> String
    ) {
        val summary = layout.summaryRect.toRectF(scratchCardRect)
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(summary.left, summary.top, summary.right, summary.bottom,
            0xEA14213E.toInt(), 0xE9091124.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(summary, 18f * dp, 18f * dp, shapePaint)
        shapePaint.shader = null
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1f * dp
        shapePaint.color = 0x7753BDE5
        canvas.drawRoundRect(summary, 18f * dp, 18f * dp, shapePaint)
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = KavvoroPalette.cyan
        canvas.drawRoundRect(summary.left + 1f * dp, summary.top + 16f * dp, summary.left + 3f * dp, summary.bottom - 16f * dp, 1f * dp, 1f * dp, shapePaint)

        val splitX = summary.left + summary.width() * 0.49f
        shapePaint.color = 0x557F93BA
        canvas.drawRect(splitX, summary.top + 19f * dp, splitX + 1f * dp, summary.bottom - 20f * dp, shapePaint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9f * dp
        textPaint.letterSpacing = 0.05f
        textPaint.color = KavvoroPalette.mutedText
        val progressRect = layout.summaryProgressTextRect.toRectF(scratchTextRect)
        canvas.drawText(t("DAILY PROGRESS").uppercase(), progressRect.left, summary.top + 29f * dp, textPaint)
        textPaint.letterSpacing = 0f
        textPaint.textSize = 20f * dp
        textPaint.color = 0xFFF5F8FF.toInt()
        val completeCount = missions.count { it.isComplete }
        canvas.drawText("$completeCount/${missions.size}", progressRect.left, progressRect.bottom - 2f * dp, textPaint)

        val rewardLabel = layout.summaryRewardLabelRect.toRectF(scratchTextRect)
        textPaint.textSize = 9f * dp
        textPaint.letterSpacing = 0.04f
        textPaint.color = KavvoroPalette.mutedText
        canvas.drawText(t("TOTAL REWARDS").uppercase(), rewardLabel.left, rewardLabel.centerY() + 3f * dp, textPaint)
        textPaint.letterSpacing = 0f
        val rewardRect = layout.summaryRewardTextRect.toRectF(scratchTextRect)
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = 13f * dp
        textPaint.color = KavvoroPalette.gold
        drawFitted(canvas, "+${missions.sumOf { it.rewardCoins }.formatGrouped()} ${t("COINS").uppercase()}", rewardRect, textPaint, 8f * dp)

        val track = layout.summaryTrackRect.toRectF(scratchTrackRect)
        shapePaint.color = 0xAA020817.toInt()
        canvas.drawRoundRect(track, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
        val fraction = (completeCount.toFloat() / missions.size.coerceAtLeast(1)).coerceIn(0f, 1f)
        if (fraction > 0f) {
            scratchFillRect.set(track.left, track.top, track.left + track.width() * fraction, track.bottom)
            shapePaint.shader = LinearGradient(scratchFillRect.left, scratchFillRect.top,
                scratchFillRect.right.coerceAtLeast(scratchFillRect.left + 1f), scratchFillRect.bottom,
                KavvoroPalette.cyan, KavvoroPalette.blue, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(scratchFillRect, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
            shapePaint.shader = null
        }
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
        val card = layout.cardRects[index].toRectF(scratchCardRect)
        val accent = when (index % 3) {
            0 -> KavvoroPalette.cyan
            1 -> KavvoroPalette.pink
            else -> KavvoroPalette.gold
        }
        val radius = 18f * dp
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(card.left, card.top, card.right, card.bottom,
            0xF3101B33.toInt(), 0xF3081023.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(card, radius, radius, shapePaint)
        shapePaint.shader = null
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = if (mission.canClaim) 1.7f * dp else 0.9f * dp
        shapePaint.color = withAlpha(if (mission.isComplete) KavvoroPalette.gold else accent, if (mission.canClaim) 220 else 100)
        canvas.drawRoundRect(card, radius, radius, shapePaint)
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(card.left + 1f * dp, card.top + 17f * dp, card.left + 3f * dp, card.top + 48f * dp, 1f * dp, 1f * dp, shapePaint)

        val iconX = if (layout.gridLayout) card.centerX() else card.left + 31f * dp
        val iconY = if (layout.gridLayout) card.top + 37f * dp else card.top + 27f * dp
        shapePaint.color = withAlpha(if (mission.isComplete) KavvoroPalette.gold else accent, if (mission.isComplete) 52 else 35)
        canvas.drawCircle(iconX, iconY, 19f * dp, shapePaint)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1.3f * dp
        shapePaint.color = withAlpha(if (mission.isComplete) KavvoroPalette.gold else accent, 220)
        canvas.drawCircle(iconX, iconY, 15f * dp, shapePaint)
        if (mission.isComplete) {
            checkPath.reset()
            checkPath.moveTo(iconX - 6f * dp, iconY)
            checkPath.lineTo(iconX - 1f * dp, iconY + 5f * dp)
            checkPath.lineTo(iconX + 7f * dp, iconY - 5f * dp)
            shapePaint.strokeCap = Paint.Cap.ROUND
            shapePaint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(checkPath, shapePaint)
        } else {
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 9f * dp
            textPaint.color = accent
            canvas.drawText((index + 1).toString().padStart(2, '0'), iconX, iconY + 3.2f * dp, textPaint)
        }

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.color = 0xFFF5F7FF.toInt()
        textPaint.textSize = if (layout.gridLayout) 14f * dp else 14.5f * dp
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.LEFT
        drawFitted(canvas, t(mission.id.titleKey).uppercase(), layout.titleTextRects[index].toRectF(scratchTextRect), textPaint, 8f * dp)

        val rewardRect = layout.rewardTextRects[index].toRectF(scratchChipRect)
        if (!rewardRect.isEmpty) {
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = withAlpha(KavvoroPalette.gold, 28)
            canvas.drawRoundRect(rewardRect, rewardRect.height() * 0.5f, rewardRect.height() * 0.5f, shapePaint)
            shapePaint.style = Paint.Style.STROKE
            shapePaint.strokeWidth = 0.8f * dp
            shapePaint.color = withAlpha(KavvoroPalette.gold, 130)
            canvas.drawRoundRect(rewardRect, rewardRect.height() * 0.5f, rewardRect.height() * 0.5f, shapePaint)
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 9f * dp
            textPaint.color = KavvoroPalette.gold
            drawFitted(canvas, "+${mission.rewardCoins} ${t("COINS").uppercase()}", rewardRect, textPaint, 6f * dp)
        }

        val progressRect = layout.progressTextRects[index].toRectF(scratchTextRect)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.textSize = 10f * dp
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.LEFT
        textPaint.color = if (mission.isComplete) KavvoroPalette.gold else KavvoroPalette.mutedText
        canvas.drawText("${mission.progress} / ${mission.target}", if (layout.gridLayout) progressRect.centerX() else progressRect.left, baselineFor(progressRect, textPaint), textPaint)

        val status = when {
            mission.claimed -> t("CLAIMED")
            mission.canClaim -> t("READY")
            else -> t("IN PROGRESS")
        }
        val statusRect = layout.statusTextRects[index].toRectF(scratchTextRect)
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = 8.5f * dp
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.RIGHT
        textPaint.color = when {
            mission.claimed -> KavvoroPalette.mutedText
            mission.canClaim -> KavvoroPalette.cyan
            else -> 0xFF8E9AB7.toInt()
        }
        if (!statusRect.isEmpty) drawFitted(canvas, status.uppercase(), statusRect, textPaint, 6.5f * dp)

        val track = layout.progressTrackRects[index].toRectF(scratchTrackRect)
        if (!track.isEmpty) {
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = 0xAA020817.toInt()
            canvas.drawRoundRect(track, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
            val fraction = (mission.progress.toFloat() / mission.target.coerceAtLeast(1)).coerceIn(0f, 1f)
            if (fraction > 0f) {
                scratchFillRect.set(track.left, track.top, track.left + track.width() * fraction, track.bottom)
                shapePaint.shader = LinearGradient(scratchFillRect.left, scratchFillRect.top,
                    scratchFillRect.right.coerceAtLeast(scratchFillRect.left + 1f), scratchFillRect.bottom,
                    accent, if (mission.isComplete) KavvoroPalette.gold else KavvoroPalette.blue, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(scratchFillRect, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
                shapePaint.shader = null
            }
        }

        drawClaimButton(canvas, layout.claimButtonRects[index].toRectF(scratchButtonRect), mission, active, dp, t)
    }

    private fun drawClaimButton(canvas: Canvas, button: RectF, mission: DailyMissionProgress, active: Boolean, dp: Float, t: (String) -> String) {
        val radius = 12f * dp
        val pressedOffset = if (active && mission.canClaim) 2f * dp else 0f
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = if (mission.canClaim) 0x99031E2B.toInt() else 0x77020A18
        scratchShadowRect.set(button.left, button.top + 3f * dp, button.right, button.bottom + 3f * dp)
        canvas.drawRoundRect(scratchShadowRect, radius, radius, shapePaint)
        scratchFaceRect.set(button.left, button.top + pressedOffset, button.right, button.bottom - 3f * dp + pressedOffset)
        if (mission.canClaim) {
            shapePaint.shader = LinearGradient(scratchFaceRect.left, scratchFaceRect.top, scratchFaceRect.right, scratchFaceRect.bottom,
                intArrayOf(0xFF27DDCA.toInt(), 0xFF147DA8.toInt()), null, Shader.TileMode.CLAMP)
        } else {
            shapePaint.color = if (mission.claimed) 0x4429D7C5 else 0x5535425E
        }
        canvas.drawRoundRect(scratchFaceRect, radius, radius, shapePaint)
        shapePaint.shader = null
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 0.9f * dp
        shapePaint.color = withAlpha(if (mission.canClaim) KavvoroPalette.cyan else KavvoroPalette.mutedText, if (mission.canClaim) 190 else 65)
        canvas.drawRoundRect(scratchFaceRect, radius, radius, shapePaint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = min(10.5f * dp, button.height() * 0.28f)
        textPaint.color = if (mission.canClaim) 0xFFFFFFFF.toInt() else KavvoroPalette.mutedText
        val label = when {
            mission.claimed -> t("CLAIMED")
            mission.canClaim -> t("CLAIM REWARD")
            else -> t("IN PROGRESS")
        }
        drawFitted(canvas, label.uppercase(), scratchFaceRect, textPaint, 7f * dp)
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

    private fun Int.formatGrouped(): String = String.format(Locale.getDefault(), "%,d", this)
}
