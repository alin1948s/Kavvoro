package com.moonsolstudios.kavvoro.ui.screens.modals

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import kotlin.math.min
import kotlin.math.sin

/** Renders the branded Daily Rift reward dialog and updates its touch targets. */
object DailyRiftRewardRenderer {

    private const val DESIGN_HEIGHT_DP = 530f

    private val cardBounds = RectF()
    private val heroBounds = RectF()
    private val progressBounds = RectF()
    private val vaultBounds = RectF()
    private val scratch = RectF()

    fun draw(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        claimed: Boolean,
        rewardAmount: Int,
        streakDay: Int,
        hypeBalance: Int,
        resetText: String,
        claimButtonRect: RectF,
        actionButtonRect: RectF,
        closeButtonRect: RectF,
        isClaimPressed: Boolean,
        isActionPressed: Boolean,
        pulseTime: Float,
        paint: Paint,
        textPaint: Paint,
        dp: Float,
        typeface: Typeface?,
        t: (String) -> String,
        formatHypeAmount: (Int) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        cardBoundsOut: RectF = cardBounds
    ) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0xF608101B.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        val cardWidth = min(viewWidth - 32f * dp, 390f * dp).coerceAtLeast(1f)
        val cardHeight = min(viewHeight - 32f * dp, DESIGN_HEIGHT_DP * dp).coerceAtLeast(1f)
        val unit = dp * (cardHeight / (DESIGN_HEIGHT_DP * dp)).coerceIn(0.62f, 1f)
        val cardLeft = (viewWidth - cardWidth) * 0.5f
        val cardTop = (viewHeight - cardHeight) * 0.5f
        cardBounds.set(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)
        cardBoundsOut.set(cardBounds)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f * unit
        paint.color = 0x443ADBD7.toInt()
        canvas.drawRoundRect(cardBounds, 30f * unit, 30f * unit, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        paint.shader = LinearGradient(
            cardLeft,
            cardTop,
            cardLeft,
            cardBounds.bottom,
            intArrayOf(0xFF172338.toInt(), 0xFF0B1321.toInt(), 0xFF080E19.toInt()),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardBounds, 28f * unit, 28f * unit, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * unit
        paint.color = 0xFFFFFFFF.toInt()
        paint.shader = LinearGradient(
            cardBounds.left,
            cardBounds.top,
            cardBounds.right,
            cardBounds.bottom,
            intArrayOf(0xFF35DCD4.toInt(), 0xFF697CF7.toInt(), 0xFFD34DAE.toInt()),
            floatArrayOf(0f, 0.56f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardBounds, 28f * unit, 28f * unit, paint)
        paint.shader = null

        scratch.set(cardLeft + 28f * unit, cardTop + 1f * unit, cardBounds.right - 28f * unit, cardTop + 5f * unit)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(scratch.left, scratch.top, scratch.right, scratch.top, 0xFF42E7D3.toInt(), 0xFFB74FED.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratch, 2f * unit, 2f * unit, paint)
        paint.shader = null

        drawCloseButton(canvas, cardBounds, closeButtonRect, unit, paint)
        drawHeader(canvas, cardBounds, closeButtonRect, claimed, unit, typeface, t, paint, textPaint)

        heroBounds.set(cardLeft + 18f * unit, cardTop + 60f * unit, cardBounds.right - 18f * unit, cardTop + 216f * unit)
        drawRewardHero(canvas, heroBounds, rewardAmount, unit, pulseTime, paint, textPaint, typeface, t, formatHypeAmount, drawWorldAsset)

        progressBounds.set(cardLeft + 18f * unit, cardTop + 228f * unit, cardBounds.right - 18f * unit, cardTop + 309f * unit)
        drawStreakTrack(canvas, progressBounds, streakDay, claimed, unit, paint, textPaint, typeface, t)

        vaultBounds.set(cardLeft + 18f * unit, cardTop + 321f * unit, cardBounds.right - 18f * unit, cardTop + 371f * unit)
        drawVaultBalance(canvas, vaultBounds, hypeBalance, unit, paint, textPaint, typeface, t, formatHypeAmount, drawWorldAsset)

        val resetBaseline = cardTop + 392f * unit
        drawResetLine(canvas, cardBounds, resetBaseline, resetText, unit, textPaint, typeface, t)

        val primaryHeight = 52f * unit
        val secondaryHeight = 44f * unit
        val buttonGap = 9f * unit
        val buttonLeft = cardLeft + 20f * unit
        val buttonRight = cardBounds.right - 20f * unit
        val secondaryTop = cardBounds.bottom - 20f * unit - secondaryHeight
        val primaryTop = secondaryTop - buttonGap - primaryHeight
        val boldTypeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD

        if (!claimed) {
            claimButtonRect.set(buttonLeft, primaryTop, buttonRight, primaryTop + primaryHeight)
            actionButtonRect.set(buttonLeft, secondaryTop, buttonRight, secondaryTop + secondaryHeight)
            drawButton(canvas, claimButtonRect, t("CLAIM REWARD"), primary = true, pressed = isClaimPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = boldTypeface)
            drawButton(canvas, actionButtonRect, t("COLLECTION"), primary = false, pressed = isActionPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = boldTypeface)
        } else {
            actionButtonRect.set(buttonLeft, primaryTop, buttonRight, primaryTop + primaryHeight)
            claimButtonRect.set(buttonLeft, secondaryTop, buttonRight, secondaryTop + secondaryHeight)
            drawButton(canvas, actionButtonRect, t("COLLECTION"), primary = true, pressed = isActionPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = boldTypeface)
            drawButton(canvas, claimButtonRect, t("CLOSE"), primary = false, pressed = isClaimPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = boldTypeface)
        }

        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.alpha = 255
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.letterSpacing = 0f
        textPaint.clearShadowLayer()
    }

    private fun drawCloseButton(canvas: Canvas, card: RectF, target: RectF, unit: Float, paint: Paint) {
        val size = 40f * unit
        val right = card.right - 18f * unit
        val top = card.top + 17f * unit
        target.set(right - size, top, right, top + size)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF1A2638.toInt()
        canvas.drawRoundRect(target, 14f * unit, 14f * unit, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF51627A.toInt()
        canvas.drawRoundRect(target, 14f * unit, 14f * unit, paint)
        paint.strokeWidth = 1.8f * unit
        paint.color = 0xFFE1E9F5.toInt()
        val cx = target.centerX()
        val cy = target.centerY()
        val crossSize = 5f * unit
        canvas.drawLine(cx - crossSize, cy - crossSize, cx + crossSize, cy + crossSize, paint)
        canvas.drawLine(cx + crossSize, cy - crossSize, cx - crossSize, cy + crossSize, paint)
    }

    private fun drawHeader(
        canvas: Canvas,
        card: RectF,
        closeTarget: RectF,
        claimed: Boolean,
        unit: Float,
        typeface: Typeface?,
        t: (String) -> String,
        paint: Paint,
        textPaint: Paint
    ) {
        val titleX = card.left + 24f * unit
        val titleBaseline = card.top + 42f * unit
        val titleTypeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        paint.style = Paint.Style.FILL
        paint.color = if (claimed) 0xFF6DE5C8.toInt() else 0xFFFFCF65.toInt()
        canvas.drawCircle(titleX + 2f * unit, titleBaseline - 5f * unit, 4.5f * unit, paint)

        textPaint.shader = null
        textPaint.typeface = titleTypeface
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 15f * unit
        textPaint.letterSpacing = 0.05f
        textPaint.color = 0xFFF4F7FD.toInt()
        val badgeWidth = if (claimed) 82f else 0f
        val titleWidth = closeTarget.left - titleX - (if (claimed) 106f else 10f) * unit
        canvas.drawText(fitText(t("DAILY RIFT BONUS").uppercase(), titleWidth, textPaint), titleX + 13f * unit, titleBaseline, textPaint)

        if (claimed) {
            scratch.set(closeTarget.left - badgeWidth * unit - 10f * unit, closeTarget.centerY() - 13f * unit, closeTarget.left - 10f * unit, closeTarget.centerY() + 13f * unit)
            paint.color = 0xFF183D36.toInt()
            canvas.drawRoundRect(scratch, 13f * unit, 13f * unit, paint)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 8f * unit
            textPaint.letterSpacing = 0.02f
            textPaint.color = 0xFF84F2CE.toInt()
            canvas.drawText(fitText(t("CLAIMED").uppercase(), scratch.width() - 8f * unit, textPaint), scratch.centerX(), scratch.centerY() + 3f * unit, textPaint)
        }
    }

    private fun drawRewardHero(
        canvas: Canvas,
        bounds: RectF,
        rewardAmount: Int,
        unit: Float,
        pulseTime: Float,
        paint: Paint,
        textPaint: Paint,
        typeface: Typeface?,
        t: (String) -> String,
        formatHypeAmount: (Int) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        paint.shader = LinearGradient(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom,
            intArrayOf(0xFF302052.toInt(), 0xFF1A2841.toInt(), 0xFF112434.toInt()),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(bounds, 23f * unit, 23f * unit, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * unit
        paint.color = 0xFF53607E.toInt()
        canvas.drawRoundRect(bounds, 23f * unit, 23f * unit, paint)

        val centerX = bounds.left + 72f * unit
        val centerY = bounds.centerY()
        val glowRadius = 57f * unit + sin(pulseTime * 2.4f) * 1.5f * unit
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            centerX,
            centerY,
            glowRadius,
            intArrayOf(0x99FFBA48.toInt(), 0x44E75CDA.toInt(), 0x0044CDEA),
            floatArrayOf(0f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(centerX, centerY, glowRadius, paint)
        paint.shader = null
        scratch.set(centerX - 51f * unit, centerY - 51f * unit, centerX + 51f * unit, centerY + 51f * unit)
        drawWorldAsset(canvas, "ic_stat_coin_3d", scratch, 255)

        val amountX = bounds.left + 158f * unit
        val amountMaxWidth = bounds.right - amountX - 14f * unit
        textPaint.shader = null
        textPaint.typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.letterSpacing = -0.01f
        textPaint.textSize = 36f * unit
        textPaint.color = 0xFFFFD978.toInt()
        textPaint.setShadowLayer(8f * unit, 0f, 0f, 0xAAFF9F39.toInt())
        canvas.drawText(fitText("+${formatHypeAmount(rewardAmount)}", amountMaxWidth, textPaint), amountX, centerY + 1f * unit, textPaint)
        textPaint.clearShadowLayer()
        textPaint.textSize = 11f * unit
        textPaint.letterSpacing = 0.18f
        textPaint.color = 0xFFB6D8E7.toInt()
        canvas.drawText(t("HYPE").uppercase(), amountX + 2f * unit, centerY + 26f * unit, textPaint)

    }

    private fun drawStreakTrack(
        canvas: Canvas,
        bounds: RectF,
        streakDay: Int,
        claimed: Boolean,
        unit: Float,
        paint: Paint,
        textPaint: Paint,
        typeface: Typeface?,
        t: (String) -> String
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFF111D2F.toInt()
        canvas.drawRoundRect(bounds, 19f * unit, 19f * unit, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF34435F.toInt()
        canvas.drawRoundRect(bounds, 19f * unit, 19f * unit, paint)

        val streak = streakDay.coerceIn(1, 7)
        textPaint.shader = null
        textPaint.typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = 9.5f * unit
        textPaint.letterSpacing = 0.12f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFAEBBD0.toInt()
        canvas.drawText(t("STREAK").uppercase(), bounds.left + 17f * unit, bounds.top + 23f * unit, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 10f * unit
        textPaint.letterSpacing = 0.04f
        textPaint.color = if (claimed) 0xFF7BE5C9.toInt() else 0xFFFFD16C.toInt()
        canvas.drawText("$streak / 7", bounds.right - 17f * unit, bounds.top + 23f * unit, textPaint)

        val firstX = bounds.left + 24f * unit
        val lastX = bounds.right - 24f * unit
        val centerY = bounds.top + 58f * unit
        val gap = (lastX - firstX) / 6f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * unit
        paint.color = 0xFF29374C.toInt()
        canvas.drawLine(firstX, centerY, lastX, centerY, paint)
        val reachedX = firstX + gap * (streak - 1)
        if (streak > 1 || claimed) {
            paint.strokeWidth = 3f * unit
            paint.color = 0xFF32D6C9.toInt()
            canvas.drawLine(firstX, centerY, reachedX, centerY, paint)
        }

        for (day in 1..7) {
            val cx = firstX + gap * (day - 1)
            val isCurrent = day == streak && !claimed
            val isComplete = day < streak || (claimed && day == streak)
            val isJackpot = day == 7 && streak == 7
            val radius = if (isCurrent || isJackpot) 11f * unit else 9f * unit
            if (isCurrent || isJackpot) {
                paint.style = Paint.Style.FILL
                paint.color = 0x33FFD36B
                canvas.drawCircle(cx, centerY, 17f * unit, paint)
            }
            paint.style = Paint.Style.FILL
            paint.color = when {
                isCurrent || isJackpot -> 0xFFFFC95E.toInt()
                isComplete -> 0xFF35D6C9.toInt()
                else -> 0xFF27364A.toInt()
            }
            canvas.drawCircle(cx, centerY, radius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * unit
            paint.color = if (isCurrent || isJackpot) 0xFFFFE3A0.toInt() else 0xFF50617A.toInt()
            canvas.drawCircle(cx, centerY, radius, paint)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 6.8f * unit
            textPaint.letterSpacing = 0f
            textPaint.color = if (isCurrent || isJackpot || isComplete) 0xFF132033.toInt() else 0xFF9DACC0.toInt()
            canvas.drawText(day.toString(), cx, centerY + 2.5f * unit, textPaint)
        }
    }

    private fun drawVaultBalance(
        canvas: Canvas,
        bounds: RectF,
        hypeBalance: Int,
        unit: Float,
        paint: Paint,
        textPaint: Paint,
        typeface: Typeface?,
        t: (String) -> String,
        formatHypeAmount: (Int) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(bounds.left, bounds.top, bounds.right, bounds.bottom, 0xFF111E30.toInt(), 0xFF0D1625.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(bounds, 16f * unit, 16f * unit, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF34435B.toInt()
        canvas.drawRoundRect(bounds, 16f * unit, 16f * unit, paint)

        scratch.set(bounds.left + 11f * unit, bounds.centerY() - 15f * unit, bounds.left + 41f * unit, bounds.centerY() + 15f * unit)
        drawWorldAsset(canvas, "ic_stat_coin_3d", scratch, 250)
        textPaint.typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9f * unit
        textPaint.letterSpacing = 0.1f
        textPaint.color = 0xFFAAB8CC.toInt()
        canvas.drawText(t("VAULT").uppercase(), bounds.left + 49f * unit, bounds.centerY() + 3.5f * unit, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 12f * unit
        textPaint.letterSpacing = 0f
        textPaint.color = 0xFFFFD471.toInt()
        val balance = "${formatHypeAmount(hypeBalance)} ${t("HYPE").uppercase()}"
        canvas.drawText(fitText(balance, bounds.width() * 0.56f, textPaint), bounds.right - 16f * unit, bounds.centerY() + 4f * unit, textPaint)
    }

    private fun drawResetLine(
        canvas: Canvas,
        card: RectF,
        baseline: Float,
        resetText: String,
        unit: Float,
        textPaint: Paint,
        typeface: Typeface?,
        t: (String) -> String
    ) {
        val countdown = resetText.dropWhile { !it.isDigit() }.trim().ifBlank { resetText }
        textPaint.typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9f * unit
        textPaint.letterSpacing = 0.05f
        textPaint.color = 0xFF8C9CB2.toInt()
        canvas.drawText(t("RESETS IN").uppercase(), card.left + 24f * unit, baseline, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.letterSpacing = 0f
        textPaint.color = 0xFFCCD6E4.toInt()
        canvas.drawText(fitText(countdown, card.width() - 48f * unit, textPaint), card.right - 24f * unit, baseline, textPaint)
    }

    private fun drawButton(
        canvas: Canvas,
        bounds: RectF,
        label: String,
        primary: Boolean,
        pressed: Boolean,
        unit: Float,
        paint: Paint,
        textPaint: Paint,
        typeface: Typeface
    ) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = if (primary) 0xFF4C397B.toInt() else 0xFF080F1B.toInt()
        scratch.set(bounds.left, bounds.top + 4f * unit, bounds.right, bounds.bottom)
        canvas.drawRoundRect(scratch, 16f * unit, 16f * unit, paint)

        val offset = if (pressed) 2f * unit else 0f
        scratch.set(bounds.left, bounds.top + offset, bounds.right, bounds.bottom - 4f * unit + offset)
        if (primary) {
            paint.shader = LinearGradient(
                scratch.left,
                scratch.top,
                scratch.right,
                scratch.bottom,
                intArrayOf(0xFF3BE3D2.toInt(), 0xFF5289F4.toInt(), 0xFFD04CC8.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        } else {
            paint.color = 0xFF16243A.toInt()
        }
        canvas.drawRoundRect(scratch, 16f * unit, 16f * unit, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * unit
        paint.color = if (primary) 0xFFB0F8E9.toInt() else 0xFF42536D.toInt()
        canvas.drawRoundRect(scratch, 16f * unit, 16f * unit, paint)

        textPaint.shader = null
        textPaint.typeface = typeface
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 12f * unit
        textPaint.letterSpacing = 0.08f
        textPaint.color = if (primary) 0xFF081522.toInt() else 0xFFE4ECF6.toInt()
        val maxWidth = bounds.width() - 28f * unit
        canvas.drawText(fitText(label.uppercase(), maxWidth, textPaint), bounds.centerX(), scratch.centerY() + 4f * unit, textPaint)
    }

    private fun fitText(text: String, maxWidth: Float, paint: Paint): String {
        if (maxWidth <= 0f || paint.measureText(text) <= maxWidth) return text
        var trimmed = text
        while (trimmed.length > 1 && paint.measureText("$trimmed…") > maxWidth) {
            trimmed = trimmed.dropLast(1)
        }
        return "$trimmed…"
    }
}
