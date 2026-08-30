package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Procedural renderer for the Leaderboards screen and list items.
 */
object LeaderboardUiRenderer {

    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawItem(
        canvas: Canvas,
        rect: RectF,
        classic: Boolean,
        levelBoard: Boolean,
        accent: Int,
        active: Boolean,
        scoreText: String,
        modeLabel: String,
        titleLabel: String,
        subLabel: String,
        paint: Paint,
        dp: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.color = if (active) withAlpha(accent, 72) else 0xD5161D29.toInt()
        canvas.drawRoundRect(rect, 8f * dp, 8f * dp, paint)
        paint.color = withAlpha(accent, 28)
        canvas.drawRoundRect(rect.left, rect.top, rect.left + 6f * dp, rect.bottom, 4f * dp, 4f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 2f * dp else 1f * dp
        paint.color = withAlpha(accent, if (active) 230 else 130)
        canvas.drawRoundRect(rect, 8f * dp, 8f * dp, paint)

        val badgeCx = rect.left + 34f * dp
        val badgeCy = rect.centerY()
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 42)
        canvas.drawCircle(badgeCx, badgeCy, 20f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.color = accent
        if (levelBoard) {
            canvas.drawLine(badgeCx - 8f * dp, badgeCy + 7f * dp, badgeCx - 2f * dp, badgeCy, paint)
            canvas.drawLine(badgeCx - 2f * dp, badgeCy, badgeCx + 3f * dp, badgeCy + 4f * dp, paint)
            canvas.drawLine(badgeCx + 3f * dp, badgeCy + 4f * dp, badgeCx + 10f * dp, badgeCy - 9f * dp, paint)
        } else {
            canvas.drawCircle(badgeCx, badgeCy, 10f * dp, paint)
            canvas.drawLine(badgeCx, badgeCy - 10f * dp, badgeCx, badgeCy + 10f * dp, paint)
            canvas.drawLine(badgeCx - 7f * dp, badgeCy - 6f * dp, badgeCx + 7f * dp, badgeCy + 6f * dp, paint)
        }

        val textLeft = rect.left + 68f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, 220)
        canvas.drawText(modeLabel, textLeft, rect.top + 21f * dp, textPaint)

        textPaint.textSize = 15f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(titleLabel, textLeft, rect.top + 45f * dp, textPaint)

        textPaint.textSize = 9f * dp
        textPaint.color = 0x99FFFFFF.toInt()
        canvas.drawText(subLabel, textLeft, rect.top + 65f * dp, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 25f * dp
        textPaint.color = accent
        canvas.drawText(scoreText, rect.right - 24f * dp, rect.centerY() + 9f * dp, textPaint)
    }

    fun drawStatusBanner(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        headerText: String,
        message: String,
        accent: Int,
        transient: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xED080C13.toInt()
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.color = accent
        canvas.drawRect(scratchRect.left, scratchRect.top, scratchRect.left + 4f * dp, scratchRect.bottom, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(accent, 135)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 235)
        canvas.drawCircle(scratchRect.left + 20f * dp, scratchRect.centerY(), (if (transient) 5f else 3f) * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 8f * dp
        textPaint.color = 0x77FFFFFF
        canvas.drawText(headerText, scratchRect.left + 36f * dp, scratchRect.top + 18f * dp, textPaint)

        textPaint.textSize = 12f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(message, scratchRect.width() - 32f * dp), scratchRect.left + 16f * dp, scratchRect.top + 39f * dp, textPaint)
    }

    fun drawStat(
        canvas: Canvas,
        left: Float,
        topLabel: Float,
        topValue: Float,
        label: String,
        value: String,
        accent: Int,
        dp: Float
    ) {
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = 0x99FFFFFF.toInt()
        canvas.drawText(label, left, topLabel, textPaint)
        textPaint.textSize = 22f * dp
        textPaint.color = accent
        canvas.drawText(value, left, topValue, textPaint)
    }

    fun drawScreen(
        canvas: Canvas,
        pageLeft: Float,
        pageRight: Float,
        pageWidth: Float,
        viewWidth: Float,
        top56: Float,
        top78: Float,
        bandTop: Float,
        bandBottom: Float,
        top118: Float,
        top146: Float,
        bottom70: Float,
        bottom16: Float,
        configured: Boolean,
        highestLevelText: String,
        bestStreakText: String,
        leaderboardScores: List<String>,
        activeLeaderboardIndex: Int,
        leaderboardBackButton: RectF,
        leaderboardItemRects: List<RectF>,
        leaderboardMessage: String,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawBackButton: (Canvas, RectF, Boolean) -> Unit
    ) {
        val left = pageLeft + 4f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 30f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("LEADERBOARDS").uppercase(), left, top56, textPaint)
        textPaint.textSize = 10f * dp
        textPaint.color = if (configured) 0xFF64E572.toInt() else 0xAAFFFFFF.toInt()
        canvas.drawText(t(if (configured) "GOOGLE PLAY / NO POWERS" else "LOCAL RECORDS").uppercase(), left, top78, textPaint)
        drawBackButton(canvas, leaderboardBackButton, activeLeaderboardIndex == -2)

        paint.style = Paint.Style.FILL
        paint.color = 0xA80B111A.toInt()
        canvas.drawRect(0f, bandTop, viewWidth, bandBottom, paint)
        paint.color = 0x331DE8C8
        canvas.drawRect(0f, bandBottom - 2f * dp, viewWidth * 0.5f, bandBottom, paint)
        paint.color = 0x33FF4D8D
        canvas.drawRect(viewWidth * 0.5f, bandBottom - 2f * dp, viewWidth, bandBottom, paint)

        drawStat(
            canvas,
            left = pageLeft + 4f * dp,
            topLabel = top118,
            topValue = top146,
            label = t("HIGHEST LEVEL").uppercase(),
            value = highestLevelText,
            accent = 0xFF1DE8C8.toInt(),
            dp = dp
        )
        drawStat(
            canvas,
            left = pageLeft + pageWidth * 0.54f,
            topLabel = top118,
            topValue = top146,
            label = t("LONGEST STREAK").uppercase(),
            value = bestStreakText,
            accent = 0xFFFF4D8D.toInt(),
            dp = dp
        )

        val boards = listOf(
            Triple(true, true, 0xFF1DE8C8.toInt()),
            Triple(false, true, 0xFFFF4D8D.toInt()),
            Triple(true, false, 0xFF1DE8C8.toInt()),
            Triple(false, false, 0xFFFF4D8D.toInt())
        )
        boards.forEachIndexed { index, (classic, levelBoard, accent) ->
            val rect = leaderboardItemRects.getOrNull(index) ?: return@forEachIndexed
            val scoreText = leaderboardScores.getOrElse(index) { "0" }
            drawItem(
                canvas = canvas,
                rect = rect,
                classic = classic,
                levelBoard = levelBoard,
                accent = accent,
                active = activeLeaderboardIndex == index,
                scoreText = scoreText,
                modeLabel = t(if (classic) "CLASSIC" else "CHAOS").uppercase(),
                titleLabel = t(if (levelBoard) "HIGHEST LEVEL" else "LONGEST STREAK").uppercase(),
                subLabel = t(if (configured) "OPEN FAIR GLOBAL RANKING" else "PERSONAL BEST").uppercase(),
                paint = paint,
                dp = dp
            )
        }

        val footerText = leaderboardMessage.ifBlank {
            t(if (configured) "SELECT A BOARD" else "GLOBAL SYNC OFFLINE")
        }
        drawStatusBanner(
            canvas = canvas,
            left = pageLeft,
            top = bottom70,
            right = pageRight,
            bottom = bottom16,
            headerText = t(if (leaderboardMessage.isNotBlank()) "STATUS UPDATE" else "NEXT SIGNAL").uppercase(),
            message = footerText,
            accent = if (configured) 0xFF8AA6FF.toInt() else 0xFF6F7788.toInt(),
            transient = leaderboardMessage.isNotBlank(),
            paint = paint,
            dp = dp,
            fitText = fitText
        )
    }

}
