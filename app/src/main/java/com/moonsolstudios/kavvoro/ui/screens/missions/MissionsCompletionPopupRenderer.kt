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
import kotlin.math.max
import kotlin.math.min

/** A short, dismissible reward notice shown after a round completes new missions. */
object MissionsCompletionPopupRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val checkPath = Path()
    private val panelRect = RectF()
    private val rowRect = RectF()

    fun draw(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        missions: List<DailyMissionProgress>,
        dp: Float,
        alpha: Float,
        t: (String) -> String
    ) {
        val visibleMissions = missions.take(3)
        if (visibleMissions.isEmpty() || alpha <= 0f) return
        val opacity = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        val width = min((viewWidth - 28f * dp).coerceAtLeast(1f), 448f * dp)
        val rowHeight = 27f * dp
        val height = 91f * dp + rowHeight * visibleMissions.size
        val left = (viewWidth - width) * 0.5f
        val preferredTop = viewHeight * 0.46f - height * 0.5f
        val top = preferredTop.coerceIn(24f * dp, max(24f * dp, viewHeight - height - 24f * dp))
        panelRect.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF020612.toInt(), (105f * alpha).toInt())
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        paint.shader = LinearGradient(
            panelRect.left, panelRect.top, panelRect.right, panelRect.bottom,
            withAlpha(0xFF152443.toInt(), opacity),
            withAlpha(0xFF080F20.toInt(), opacity),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(panelRect, 20f * dp, 20f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = withAlpha(KavvoroPalette.gold, (210f * alpha).toInt())
        canvas.drawRoundRect(panelRect, 20f * dp, 20f * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(KavvoroPalette.gold, (220f * alpha).toInt())
        canvas.drawRoundRect(left + 16f * dp, top + 2f * dp, left + width - 16f * dp, top + 4f * dp, 2f * dp, 2f * dp, paint)

        val badgeX = left + 31f * dp
        val badgeY = top + 28f * dp
        paint.color = withAlpha(KavvoroPalette.gold, (45f * alpha).toInt())
        canvas.drawCircle(badgeX, badgeY, 16f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * dp
        paint.color = withAlpha(KavvoroPalette.gold, (225f * alpha).toInt())
        canvas.drawCircle(badgeX, badgeY, 12f * dp, paint)
        checkPath.reset()
        checkPath.moveTo(badgeX - 5f * dp, badgeY)
        checkPath.lineTo(badgeX - 1f * dp, badgeY + 4f * dp)
        checkPath.lineTo(badgeX + 6f * dp, badgeY - 5f * dp)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(checkPath, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 15f * dp
        textPaint.letterSpacing = 0.02f
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), opacity)
        drawFitted(canvas, t("MISSIONS COMPLETE").uppercase(), left + 55f * dp, top + 33f * dp, width - 74f * dp, textPaint, 10f * dp)
        textPaint.letterSpacing = 0f

        val rowsTop = top + 51f * dp
        visibleMissions.forEachIndexed { index, mission ->
            val rowTop = rowsTop + index * rowHeight
            rowRect.set(left + 16f * dp, rowTop, left + width - 16f * dp, rowTop + rowHeight)
            if (index > 0) {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(0xFF8292B8.toInt(), (36f * alpha).toInt())
                canvas.drawRect(rowRect.left, rowRect.top, rowRect.right, rowRect.top + 1f * dp, paint)
            }
            textPaint.typeface = AssetResourceManager.oxaniumNormal()
            textPaint.textSize = 10.5f * dp
            textPaint.color = withAlpha(0xFFE6ECFF.toInt(), opacity)
            textPaint.textAlign = Paint.Align.LEFT
            drawFitted(canvas, t(mission.id.titleKey).uppercase(), rowRect.left + 2f * dp, rowRect.centerY() + 3.8f * dp, width * 0.62f, textPaint, 7f * dp)

            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = 10.5f * dp
            textPaint.color = withAlpha(KavvoroPalette.gold, opacity)
            canvas.drawText("+${mission.rewardCoins}", rowRect.right - 14f * dp, rowRect.centerY() + 3.8f * dp, textPaint)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(KavvoroPalette.gold, opacity)
            canvas.drawCircle(rowRect.right - 5f * dp, rowRect.centerY(), 2.2f * dp, paint)
        }

        val footerTop = rowsTop + visibleMissions.size * rowHeight + 1f * dp
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(KavvoroPalette.cyan, opacity)
        canvas.drawText(t("REWARDS READY TO CLAIM").uppercase(), panelRect.centerX(), footerTop + 12f * dp, textPaint)
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.textSize = 8f * dp
        textPaint.color = withAlpha(KavvoroPalette.mutedText, (190f * alpha).toInt())
        canvas.drawText(t("TAP TO CONTINUE").uppercase(), panelRect.centerX(), footerTop + 27f * dp, textPaint)
    }

    private fun drawFitted(
        canvas: Canvas,
        value: String,
        x: Float,
        baseline: Float,
        maxWidth: Float,
        paint: Paint,
        minTextSize: Float
    ) {
        val originalSize = paint.textSize
        while (paint.textSize > minTextSize && paint.measureText(value) > maxWidth) {
            paint.textSize -= 0.5f
        }
        canvas.drawText(value, x, baseline, paint)
        paint.textSize = originalSize
    }
}
