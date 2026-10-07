package com.moonsolstudios.kavvoro.ui.screens.modals

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import kotlin.math.min

/** Draws the opaque Daily Rift reward dialog and updates its touch targets. */
object DailyRiftRewardRenderer {

    private val cardBounds = RectF()
    private val heroBounds = RectF()
    private val vaultBounds = RectF()
    private val scratch = RectF()
    private val boltPath = Path()

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
        cardBoundsOut: RectF = cardBounds
    ) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0xF608101B.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        val cardWidth = min(viewWidth - 32f * dp, 390f * dp).coerceAtLeast(1f)
        val cardHeight = min(viewHeight - 32f * dp, 500f * dp).coerceAtLeast(1f)
        val unit = dp * (cardHeight / (500f * dp)).coerceIn(0.72f, 1f)
        val cardLeft = (viewWidth - cardWidth) * 0.5f
        val cardTop = (viewHeight - cardHeight) * 0.5f
        cardBounds.set(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)
        cardBoundsOut.set(cardBounds)

        paint.color = 0xFF0A111D.toInt()
        canvas.drawRoundRect(cardBounds, 28f * unit, 28f * unit, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF2A3B50.toInt()
        canvas.drawRoundRect(cardBounds, 28f * unit, 28f * unit, paint)

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cardLeft,
            cardTop,
            cardLeft + cardWidth,
            cardTop,
            0xFF37D9D0.toInt(),
            0xFF698BFF.toInt(),
            Shader.TileMode.CLAMP
        )
        scratch.set(cardLeft + 28f * unit, cardTop + 1f * unit, cardLeft + cardWidth - 28f * unit, cardTop + 4f * unit)
        canvas.drawRoundRect(scratch, 2f * unit, 2f * unit, paint)
        paint.shader = null

        val closeSize = 40f * unit
        val closeRight = cardBounds.right - 18f * unit
        val closeTop = cardTop + 17f * unit
        closeButtonRect.set(closeRight - closeSize, closeTop, closeRight, closeTop + closeSize)
        paint.color = 0xFF1A2638.toInt()
        canvas.drawRoundRect(closeButtonRect, 14f * unit, 14f * unit, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF35475E.toInt()
        canvas.drawRoundRect(closeButtonRect, 14f * unit, 14f * unit, paint)
        paint.color = 0xFFCFD9E7.toInt()
        paint.strokeWidth = 1.8f * unit
        val closeCx = closeButtonRect.centerX()
        val closeCy = closeButtonRect.centerY()
        val crossSize = 5f * unit
        canvas.drawLine(closeCx - crossSize, closeCy - crossSize, closeCx + crossSize, closeCy + crossSize, paint)
        canvas.drawLine(closeCx + crossSize, closeCy - crossSize, closeCx - crossSize, closeCy + crossSize, paint)

        val titleBaseline = cardTop + 42f * unit
        textPaint.shader = null
        textPaint.typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.letterSpacing = 0.08f
        textPaint.textSize = 12f * unit
        textPaint.color = 0xFFE7F1FC.toInt()
        val titleMaxWidth = cardWidth - (if (claimed) 172f else 90f) * unit
        canvas.drawText(fitText(t("DAILY RIFT BONUS").uppercase(), titleMaxWidth, textPaint), cardLeft + 24f * unit, titleBaseline, textPaint)

        if (claimed) {
            scratch.set(closeButtonRect.left - 82f * unit, closeButtonRect.centerY() - 13f * unit, closeButtonRect.left - 10f * unit, closeButtonRect.centerY() + 13f * unit)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF18342F.toInt()
            canvas.drawRoundRect(scratch, 13f * unit, 13f * unit, paint)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 8.5f * unit
            textPaint.letterSpacing = 0.03f
            textPaint.color = 0xFF75E5C4.toInt()
            canvas.drawText(fitText(t("CLAIMED").uppercase(), scratch.width() - 8f * unit, textPaint), scratch.centerX(), scratch.centerY() + 3f * unit, textPaint)
        }

        heroBounds.set(cardLeft + 18f * unit, cardTop + 61f * unit, cardBounds.right - 18f * unit, cardTop + 204f * unit)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            heroBounds.left,
            heroBounds.top,
            heroBounds.right,
            heroBounds.bottom,
            0xFF1A2B40.toInt(),
            0xFF111C2C.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(heroBounds, 22f * unit, 22f * unit, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF2B4159.toInt()
        canvas.drawRoundRect(heroBounds, 22f * unit, 22f * unit, paint)

        val pulse = (kotlin.math.sin(pulseTime * 2.2f) * 1.5f + 38f) * unit
        val iconCenterX = heroBounds.left + 54f * unit
        val iconCenterY = heroBounds.centerY()
        paint.style = Paint.Style.FILL
        paint.color = 0xFF352F24.toInt()
        canvas.drawCircle(iconCenterX, iconCenterY, pulse, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * unit
        paint.color = 0xFF665236.toInt()
        canvas.drawCircle(iconCenterX, iconCenterY, 38f * unit, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFCB62.toInt()
        drawLightningBolt(canvas, iconCenterX, iconCenterY, 23f * unit, paint)

        val amountX = heroBounds.left + 105f * unit
        val amountMaxWidth = heroBounds.right - amountX - 14f * unit
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.letterSpacing = 0f
        textPaint.textSize = 31f * unit
        textPaint.color = 0xFF69E5DD.toInt()
        canvas.drawText(fitText("+${formatHypeAmount(rewardAmount)}", amountMaxWidth, textPaint), amountX, iconCenterY + 3f * unit, textPaint)
        textPaint.textSize = 11f * unit
        textPaint.letterSpacing = 0.14f
        textPaint.color = 0xFFB6C5D8.toInt()
        canvas.drawText(t("HYPE").uppercase(), amountX + 1f * unit, iconCenterY + 26f * unit, textPaint)

        val streak = streakDay.coerceIn(1, 7)
        val streakTop = cardTop + 231f * unit
        textPaint.textSize = 10f * unit
        textPaint.letterSpacing = 0.08f
        textPaint.color = 0xFFB6C4D5.toInt()
        canvas.drawText(t("STREAK").uppercase(), cardLeft + 24f * unit, streakTop, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = 0xFFE7F1FC.toInt()
        canvas.drawText("$streak / 7", cardBounds.right - 24f * unit, streakTop, textPaint)

        val trackLeft = cardLeft + 24f * unit
        val trackRight = cardBounds.right - 24f * unit
        val trackTop = streakTop + 14f * unit
        val gap = 5f * unit
        val segmentWidth = (trackRight - trackLeft - gap * 6f) / 7f
        for (day in 0 until 7) {
            scratch.set(trackLeft + day * (segmentWidth + gap), trackTop, trackLeft + day * (segmentWidth + gap) + segmentWidth, trackTop + 7f * unit)
            paint.style = Paint.Style.FILL
            paint.color = if (day < streak) 0xFF35D6CC.toInt() else 0xFF29364A.toInt()
            canvas.drawRoundRect(scratch, 3.5f * unit, 3.5f * unit, paint)
        }

        vaultBounds.set(cardLeft + 18f * unit, cardTop + 269f * unit, cardBounds.right - 18f * unit, cardTop + 316f * unit)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF0C1522.toInt()
        canvas.drawRoundRect(vaultBounds, 15f * unit, 15f * unit, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = 0xFF27384D.toInt()
        canvas.drawRoundRect(vaultBounds, 15f * unit, 15f * unit, paint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9f * unit
        textPaint.letterSpacing = 0.1f
        textPaint.color = 0xFF8FA1B7.toInt()
        canvas.drawText(t("VAULT").uppercase(), vaultBounds.left + 16f * unit, vaultBounds.centerY() + 3.5f * unit, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 12f * unit
        textPaint.letterSpacing = 0f
        textPaint.color = 0xFFFFD26E.toInt()
        val balanceLabel = "${formatHypeAmount(hypeBalance)} ${t("HYPE").uppercase()}"
        canvas.drawText(fitText(balanceLabel, vaultBounds.width() * 0.56f, textPaint), vaultBounds.right - 16f * unit, vaultBounds.centerY() + 4f * unit, textPaint)

        val resetCountdown = resetText.dropWhile { !it.isDigit() }.trim().ifBlank { resetText }
        val resetBaseline = cardTop + 348f * unit
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9f * unit
        textPaint.letterSpacing = 0.04f
        textPaint.color = 0xFF8192A8.toInt()
        canvas.drawText(t("RESETS IN").uppercase(), cardLeft + 24f * unit, resetBaseline, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.letterSpacing = 0f
        textPaint.color = 0xFFB6C4D5.toInt()
        canvas.drawText(fitText(resetCountdown, cardWidth - 48f * unit, textPaint), cardBounds.right - 24f * unit, resetBaseline, textPaint)

        val primaryHeight = 52f * unit
        val secondaryHeight = 44f * unit
        val buttonGap = 9f * unit
        val buttonLeft = cardLeft + 20f * unit
        val buttonRight = cardBounds.right - 20f * unit
        val secondaryTop = cardBounds.bottom - 20f * unit - secondaryHeight
        val primaryTop = secondaryTop - buttonGap - primaryHeight
        if (!claimed) {
            claimButtonRect.set(buttonLeft, primaryTop, buttonRight, primaryTop + primaryHeight)
            actionButtonRect.set(buttonLeft, secondaryTop, buttonRight, secondaryTop + secondaryHeight)
            drawButton(canvas, claimButtonRect, t("CLAIM REWARD"), primary = true, pressed = isClaimPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD)
            drawButton(canvas, actionButtonRect, t("COLLECTION"), primary = false, pressed = isActionPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD)
        } else {
            actionButtonRect.set(buttonLeft, primaryTop, buttonRight, primaryTop + primaryHeight)
            claimButtonRect.set(buttonLeft, secondaryTop, buttonRight, secondaryTop + secondaryHeight)
            drawButton(canvas, actionButtonRect, t("COLLECTION"), primary = true, pressed = isActionPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD)
            drawButton(canvas, claimButtonRect, t("CLOSE"), primary = false, pressed = isClaimPressed, unit = unit, paint = paint, textPaint = textPaint, typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD)
        }

        paint.shader = null
        paint.style = Paint.Style.FILL
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.letterSpacing = 0f
        textPaint.clearShadowLayer()
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
        paint.color = if (primary) 0xFF087E83.toInt() else 0xFF0A111D.toInt()
        scratch.set(bounds.left, bounds.top + 3f * unit, bounds.right, bounds.bottom)
        canvas.drawRoundRect(scratch, 15f * unit, 15f * unit, paint)

        val offset = if (pressed) 2f * unit else 0f
        scratch.set(bounds.left, bounds.top + offset, bounds.right, bounds.bottom - 3f * unit + offset)
        if (primary) {
            paint.shader = LinearGradient(scratch.left, scratch.top, scratch.right, scratch.bottom, 0xFF42DED0.toInt(), 0xFF18AEB6.toInt(), Shader.TileMode.CLAMP)
        } else {
            paint.color = 0xFF172438.toInt()
        }
        canvas.drawRoundRect(scratch, 15f * unit, 15f * unit, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * unit
        paint.color = if (primary) 0xFF7AECE0.toInt() else 0xFF34475F.toInt()
        canvas.drawRoundRect(scratch, 15f * unit, 15f * unit, paint)

        textPaint.shader = null
        textPaint.typeface = typeface
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 12f * unit
        textPaint.letterSpacing = 0.06f
        textPaint.color = if (primary) 0xFF07151D.toInt() else 0xFFE3EBF5.toInt()
        val maxWidth = bounds.width() - 28f * unit
        canvas.drawText(fitText(label.uppercase(), maxWidth, textPaint), bounds.centerX(), scratch.centerY() + 4f * unit, textPaint)
    }

    private fun drawLightningBolt(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val halfWidth = size * 0.58f
        boltPath.rewind()
        boltPath.moveTo(cx + halfWidth * 0.22f, cy - size * 0.55f)
        boltPath.lineTo(cx - halfWidth * 0.48f, cy + size * 0.02f)
        boltPath.lineTo(cx - halfWidth * 0.03f, cy + size * 0.02f)
        boltPath.lineTo(cx - halfWidth * 0.23f, cy + size * 0.55f)
        boltPath.lineTo(cx + halfWidth * 0.48f, cy - size * 0.04f)
        boltPath.lineTo(cx + halfWidth * 0.05f, cy - size * 0.04f)
        boltPath.close()
        canvas.drawPath(boltPath, paint)
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
