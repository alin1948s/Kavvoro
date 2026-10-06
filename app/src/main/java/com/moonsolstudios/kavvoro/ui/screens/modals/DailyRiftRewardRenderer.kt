package com.moonsolstudios.kavvoro.ui.screens.modals

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Procedural AAA renderer for the Daily Rift Reward modal.
 * Features chamfered cyber-plate geometry, glowing energy core, 7-day streak tracking,
 * and direct call-to-action routing to Collection for unlocking Brainballs.
 */
object DailyRiftRewardRenderer {

    private val cardPath = Path()
    private val buttonPath = Path()
    private val lipPath = Path()
    private val lightningBoltPath = Path()
    val cardBounds = RectF()
    private val scratch = RectF()
    private val scratch2 = RectF()

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
        // 1. Semi-transparent backdrop with cosmic obsidian tint
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0xD803060E.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        // 2. Card dimensions and geometry
        val cardWidth = min(viewWidth - 36f * dp, 440f * dp)
        val cardHeight = min(viewHeight - 60f * dp, 520f * dp)
        val cardLeft = (viewWidth - cardWidth) * 0.5f
        val cardTop = (viewHeight - cardHeight) * 0.48f
        val cardRight = cardLeft + cardWidth
        val cardBottom = cardTop + cardHeight
        cardBounds.set(cardLeft, cardTop, cardRight, cardBottom)
        cardBoundsOut.set(cardBounds)

        val chamfer = 20f * dp
        buildChamferedPath(cardPath, cardBounds, chamfer)

        // Outer soft glow aura
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f * dp
        paint.color = 0x2A00E5FF.toInt()
        canvas.drawPath(cardPath, paint)

        // Card body gradient fill
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cardLeft, cardTop, cardRight, cardBottom,
            intArrayOf(0xFF0D1424.toInt(), 0xFF060913.toInt(), 0xFF020409.toInt()),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(cardPath, paint)
        paint.shader = null

        // Cyber neon dual-tone border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.shader = LinearGradient(
            cardLeft, cardTop, cardRight, cardBottom,
            intArrayOf(0xFF00E5FF.toInt(), 0xFF8AA6FF.toInt(), 0xFFFF2E93.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(cardPath, paint)
        paint.shader = null

        // Specular highlight on top edge
        paint.strokeWidth = 1.2f * dp
        paint.color = 0x99E0F7FF.toInt()
        canvas.drawLine(cardLeft + chamfer, cardTop, cardRight - chamfer, cardTop, paint)

        // Close button [X] at top-right
        val closeSize = 32f * dp
        val closeX = cardRight - 16f * dp - closeSize
        val closeY = cardTop + 14f * dp
        closeButtonRect.set(closeX, closeY, closeX + closeSize, closeY + closeSize)
        paint.style = Paint.Style.FILL
        paint.color = 0x22FFFFFF.toInt()
        canvas.drawCircle(closeButtonRect.centerX(), closeButtonRect.centerY(), closeSize * 0.46f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0x6600E5FF.toInt()
        canvas.drawCircle(closeButtonRect.centerX(), closeButtonRect.centerY(), closeSize * 0.46f, paint)

        val crossR = 5f * dp
        val cx = closeButtonRect.centerX()
        val cy = closeButtonRect.centerY()
        paint.color = 0xCCFFFFFF.toInt()
        canvas.drawLine(cx - crossR, cy - crossR, cx + crossR, cy + crossR, paint)
        canvas.drawLine(cx + crossR, cy - crossR, cx - crossR, cy + crossR, paint)

        // 3. Eyebrow Tag: Status Beacon + Telemetry
        val eyebrowY = cardTop + 24f * dp
        val beaconX = cardLeft + 22f * dp
        val beaconR = 3f * dp
        paint.style = Paint.Style.FILL
        paint.color = if (!claimed) 0x4000E5FF.toInt() else 0x40FFCF4A.toInt()
        canvas.drawCircle(beaconX, eyebrowY, beaconR * 2.2f, paint)
        paint.color = if (!claimed) 0xFF00E5FF.toInt() else 0xFFFFCF4A.toInt()
        canvas.drawCircle(beaconX, eyebrowY, beaconR, paint)

        textPaint.shader = null
        textPaint.typeface = typeface ?: AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9.5f * dp
        textPaint.letterSpacing = 0.16f
        textPaint.color = if (!claimed) 0xFF00E5FF.toInt() else 0xFFFFCF4A.toInt()
        val eyebrowLine = "${t("DAILY RIFT").uppercase()} // 🎁 ${t("DAILY RIFT BONUS").uppercase()} ⚡"
        canvas.drawText(fitText(eyebrowLine, cardWidth - 42f * dp, textPaint), beaconX + 10f * dp, eyebrowY + 3.5f * dp, textPaint)

        // 4. Main Title
        val titleY = cardTop + 54f * dp
        textPaint.textSize = 21f * dp
        textPaint.letterSpacing = 0.08f
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText(fitText(t("DAILY RIFT").uppercase(), cardWidth - 40f * dp, textPaint), cardLeft + 20f * dp, titleY, textPaint)

        // 5. Central Energy Vault / Reward Visual
        val coreCenterY = cardTop + 160f * dp
        val coreCenterX = cardBounds.centerX()
        val pulse = sin(pulseTime * 3f) * 3f * dp

        // Concentric glowing rings
        val r1 = 52f * dp + pulse
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f * dp
        paint.color = if (!claimed) 0x6600E5FF.toInt() else 0x44FFCF4A.toInt()
        canvas.drawCircle(coreCenterX, coreCenterY, r1, paint)

        val r2 = 40f * dp - pulse * 0.5f
        paint.strokeWidth = 1.4f * dp
        paint.color = if (!claimed) 0x55FF2E93.toInt() else 0x3300E5FF.toInt()
        canvas.drawCircle(coreCenterX, coreCenterY, r2, paint)

        // Orbital spark nodes
        val angle1 = pulseTime * 1.5f
        val sparkX = coreCenterX + cos(angle1) * r1
        val sparkY = coreCenterY + sin(angle1) * r1
        paint.style = Paint.Style.FILL
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawCircle(sparkX, sparkY, 3.5f * dp, paint)

        val angle2 = -pulseTime * 2.0f
        val sparkX2 = coreCenterX + cos(angle2) * r2
        val sparkY2 = coreCenterY + sin(angle2) * r2
        paint.color = 0xFFFF2E93.toInt()
        canvas.drawCircle(sparkX2, sparkY2, 2.5f * dp, paint)

        // Core fill & icon
        paint.style = Paint.Style.FILL
        paint.color = if (!claimed) 0x2200E5FF.toInt() else 0x22FFCF4A.toInt()
        canvas.drawCircle(coreCenterX, coreCenterY, 30f * dp, paint)

        // Lightning / Hype Bolt inside core
        paint.style = Paint.Style.FILL
        paint.color = if (!claimed) 0xFFFFCF4A.toInt() else 0xFF00E5FF.toInt()
        drawLightningBolt(canvas, coreCenterX, coreCenterY, 18f * dp, paint)

        // 6. Reward Callout / Amount
        textPaint.textAlign = Paint.Align.CENTER
        if (!claimed) {
            textPaint.textSize = 28f * dp
            textPaint.letterSpacing = 0.06f
            textPaint.color = 0xFF00E5FF.toInt()
            canvas.drawText(fitText("+${formatHypeAmount(rewardAmount)} ${t("HYPE").uppercase()}", cardWidth - 36f * dp, textPaint), coreCenterX, coreCenterY + 74f * dp, textPaint)

            textPaint.textSize = 10f * dp
            textPaint.letterSpacing = 0.04f
            textPaint.color = 0xAAFFFFFF.toInt()
            canvas.drawText(fitText("${t("TAP TO UNLOCK").uppercase()} BRAINBALLS", cardWidth - 36f * dp, textPaint), coreCenterX, coreCenterY + 94f * dp, textPaint)
        } else {
            textPaint.textSize = 21f * dp
            textPaint.letterSpacing = 0.06f
            textPaint.color = 0xFFFFCF4A.toInt()
            canvas.drawText(fitText("✓ ${t("CLAIMED").uppercase()}", cardWidth - 36f * dp, textPaint), coreCenterX, coreCenterY + 70f * dp, textPaint)

            textPaint.textSize = 10.5f * dp
            textPaint.letterSpacing = 0.04f
            textPaint.color = 0xCC00E5FF.toInt()
            val cleanReset = resetText.replace(Regex("^(?i)[^\\d]*"), "").trim()
            canvas.drawText(fitText("${t("RESETS IN").uppercase()}: $cleanReset", cardWidth - 36f * dp, textPaint), coreCenterX, coreCenterY + 92f * dp, textPaint)
        }

        // 7. 7-Day Login Streak Track
        val streakTop = coreCenterY + 115f * dp
        textPaint.textSize = 8.5f * dp
        textPaint.letterSpacing = 0.12f
        textPaint.color = 0x889AA8BA.toInt()
        val streakWord = t("BEST STREAK")
        canvas.drawText(fitText("${streakWord.uppercase()} • $streakDay / 7", cardWidth - 36f * dp, textPaint), coreCenterX, streakTop, textPaint)

        val chitsY = streakTop + 10f * dp
        val chitW = (cardWidth - 56f * dp) / 7f
        val chitH = 34f * dp
        val chitGap = 4f * dp
        val effectiveW = chitW - chitGap
        val startChitsX = cardLeft + 28f * dp

        for (day in 1..7) {
            val left = startChitsX + (day - 1) * chitW
            val right = left + effectiveW
            scratch.set(left, chitsY, right, chitsY + chitH)

            val isPassed = day < streakDay
            val isCurrent = day == streakDay
            val isJackpot = day == 7

            // Chit background
            paint.style = Paint.Style.FILL
            paint.color = when {
                isCurrent -> 0x3300E5FF.toInt()
                isPassed -> 0x221DE8C8.toInt()
                isJackpot -> 0x1AFFCF4A.toInt()
                else -> 0x14050810.toInt()
            }
            canvas.drawRoundRect(scratch, 4f * dp, 4f * dp, paint)

            // Chit border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = if (isCurrent) 1.5f * dp else 0.8f * dp
            paint.color = when {
                isCurrent -> 0xFF00E5FF.toInt()
                isPassed -> 0x881DE8C8.toInt()
                isJackpot -> 0x99FFCF4A.toInt()
                else -> 0x2AFFFFFF.toInt()
            }
            canvas.drawRoundRect(scratch, 4f * dp, 4f * dp, paint)

            // Specular top highlight on chit
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f * dp
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratch.left + 3f * dp, scratch.top + 0.8f * dp, scratch.right - 3f * dp, scratch.top + 0.8f * dp, paint)

            // Day label
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 7.5f * dp
            textPaint.letterSpacing = 0f
            textPaint.color = when {
                isCurrent -> 0xFFFFFFFF.toInt()
                isPassed -> 0xFF1DE8C8.toInt()
                isJackpot -> 0xFFFFCF4A.toInt()
                else -> 0x66FFFFFF.toInt()
            }
            val label = if (isJackpot) "D7 ★" else "D$day"
            canvas.drawText(label, scratch.centerX(), scratch.top + 13f * dp, textPaint)

            // Reward preview per day
            textPaint.textSize = 7f * dp
            textPaint.color = if (isCurrent || isPassed) 0xFF00E5FF.toInt() else 0x559AA8BA.toInt()
            val valText = if (day == 7) "5K" else "${1 + (day - 1) * 25 / 100f}".take(3) + "K"
            canvas.drawText(valText, scratch.centerX(), scratch.bottom - 7f * dp, textPaint)
        }

        // 8. Hype Bank Balance Telemetry
        val bankY = chitsY + chitH + 24f * dp
        textPaint.textSize = 9.5f * dp
        textPaint.letterSpacing = 0.08f
        textPaint.color = 0xAA9AA8BA.toInt()
        val bankLabel = "${t("VAULT").uppercase()} // ${formatHypeAmount(hypeBalance)} HYPE"
        canvas.drawText(bankLabel, coreCenterX, bankY, textPaint)

        // 9. Action Buttons Layout (Bottom of Card)
        val btnMargin = 20f * dp
        val btnH = 48f * dp
        val btnGap = 10f * dp

        val depth = (4f * dp).coerceAtLeast(3f)
        val btnChamfer = 10f * dp

        if (!claimed) {
            // State: READY TO CLAIM
            // Primary Button: CLAIM REWARD (Chunky 3D Candy Cyan Button)
            val claimTop = cardBottom - btnMargin - btnH * 2f - btnGap
            claimButtonRect.set(cardLeft + btnMargin, claimTop, cardRight - btnMargin, claimTop + btnH)
            val claimPress = if (isClaimPressed) 2f * dp else 0f

            // 3D bottom extrusion lip
            scratch.set(claimButtonRect.left, claimButtonRect.top + depth, claimButtonRect.right, claimButtonRect.bottom)
            buildChamferedPath(lipPath, scratch, btnChamfer)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF005869.toInt()
            canvas.drawPath(lipPath, paint)

            // Raised button face
            scratch2.set(claimButtonRect.left, claimButtonRect.top + claimPress, claimButtonRect.right, claimButtonRect.bottom - depth + claimPress)
            buildChamferedPath(buttonPath, scratch2, btnChamfer)
            paint.shader = LinearGradient(
                scratch2.left, scratch2.top, scratch2.right, scratch2.bottom,
                if (isClaimPressed) intArrayOf(0xFF00C7E6.toInt(), 0xFF007A8A.toInt())
                else intArrayOf(0xFF00F5D4.toInt(), 0xFF0090B0.toInt()),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(buttonPath, paint)
            paint.shader = null

            // Curved glossy specular glare on top 48%
            scratch.set(scratch2.left + 2f * dp, scratch2.top + 1f * dp, scratch2.right - 2f * dp, scratch2.top + scratch2.height() * 0.48f)
            paint.shader = LinearGradient(scratch.left, scratch.top, scratch.left, scratch.bottom, 0x85FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(scratch, btnChamfer - 2f * dp, btnChamfer - 2f * dp, paint)
            paint.shader = null

            // Specular top highlight line
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = 0xDDFFFFFF.toInt()
            canvas.drawLine(scratch2.left + 12f * dp, scratch2.top + 1f * dp, scratch2.right - 12f * dp, scratch2.top + 1f * dp, paint)

            // Border
            paint.strokeWidth = 2f * dp
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawPath(buttonPath, paint)

            textPaint.textSize = 12.5f * dp
            textPaint.letterSpacing = 0.12f
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0x99000000.toInt())
            canvas.drawText("⚡ ${t("DAILY RIFT BONUS").uppercase()} 🎁", scratch2.centerX(), scratch2.centerY() + 4.5f * dp, textPaint)
            textPaint.clearShadowLayer()

            // Secondary Button: GO TO COLLECTION (Tactile 3D Deep Cobalt Candy)
            val actionTop = cardBottom - btnMargin - btnH
            actionButtonRect.set(cardLeft + btnMargin, actionTop, cardRight - btnMargin, actionTop + btnH)
            val actionPress = if (isActionPressed) 2f * dp else 0f

            // 3D bottom extrusion lip
            scratch.set(actionButtonRect.left, actionButtonRect.top + depth, actionButtonRect.right, actionButtonRect.bottom)
            buildChamferedPath(lipPath, scratch, btnChamfer)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF06101E.toInt()
            canvas.drawPath(lipPath, paint)

            // Raised button face
            scratch2.set(actionButtonRect.left, actionButtonRect.top + actionPress, actionButtonRect.right, actionButtonRect.bottom - depth + actionPress)
            buildChamferedPath(buttonPath, scratch2, btnChamfer)
            paint.shader = LinearGradient(
                scratch2.left, scratch2.top, scratch2.right, scratch2.bottom,
                if (isActionPressed) 0xFF1C3A5E.toInt() else 0xFF142844.toInt(),
                0xFF0B1728.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(buttonPath, paint)
            paint.shader = null

            // Specular top highlight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0x66FFFFFF
            canvas.drawLine(scratch2.left + 12f * dp, scratch2.top + 1f * dp, scratch2.right - 12f * dp, scratch2.top + 1f * dp, paint)

            // Cyan border
            paint.strokeWidth = 1.4f * dp
            paint.color = 0xFF00E5FF.toInt()
            canvas.drawPath(buttonPath, paint)

            textPaint.textSize = 10.5f * dp
            textPaint.letterSpacing = 0.10f
            textPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawText("🚀 ${t("COLLECTION").uppercase()} & ${t("VAULT").uppercase()} >>", scratch2.centerX(), scratch2.centerY() + 4f * dp, textPaint)
        } else {
            // State: ALREADY CLAIMED
            // Primary Button: OPEN COLLECTION & SEIF (Chunky 3D Hot Magenta Candy Button)
            val actionTop = cardBottom - btnMargin - btnH * 2f - btnGap
            actionButtonRect.set(cardLeft + btnMargin, actionTop, cardRight - btnMargin, actionTop + btnH)
            val actionPress = if (isActionPressed) 2f * dp else 0f

            // 3D bottom extrusion lip
            scratch.set(actionButtonRect.left, actionButtonRect.top + depth, actionButtonRect.right, actionButtonRect.bottom)
            buildChamferedPath(lipPath, scratch, btnChamfer)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF6B0033.toInt()
            canvas.drawPath(lipPath, paint)

            // Raised button face
            scratch2.set(actionButtonRect.left, actionButtonRect.top + actionPress, actionButtonRect.right, actionButtonRect.bottom - depth + actionPress)
            buildChamferedPath(buttonPath, scratch2, btnChamfer)
            paint.shader = LinearGradient(
                scratch2.left, scratch2.top, scratch2.right, scratch2.bottom,
                if (isActionPressed) intArrayOf(0xFFD9247A.toInt(), 0xFF8A134C.toInt())
                else intArrayOf(0xFFFF1493.toInt(), 0xFFB30059.toInt()),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(buttonPath, paint)
            paint.shader = null

            // Specular top glare
            scratch.set(scratch2.left + 2f * dp, scratch2.top + 1f * dp, scratch2.right - 2f * dp, scratch2.top + scratch2.height() * 0.48f)
            paint.shader = LinearGradient(scratch.left, scratch.top, scratch.left, scratch.bottom, 0x85FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(scratch, btnChamfer - 2f * dp, btnChamfer - 2f * dp, paint)
            paint.shader = null

            // Specular top highlight line
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = 0xDDFFFFFF.toInt()
            canvas.drawLine(scratch2.left + 12f * dp, scratch2.top + 1f * dp, scratch2.right - 12f * dp, scratch2.top + 1f * dp, paint)

            // Border
            paint.strokeWidth = 2f * dp
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawPath(buttonPath, paint)

            textPaint.textSize = 12f * dp
            textPaint.letterSpacing = 0.12f
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0x99000000.toInt())
            canvas.drawText("${t("UNLOCK").uppercase()} BRAINBALLS >>", scratch2.centerX(), scratch2.centerY() + 4.5f * dp, textPaint)
            textPaint.clearShadowLayer()

            // Secondary Button: CLOSE (Tactile 3D Slate Button)
            val closeBottomTop = cardBottom - btnMargin - btnH
            claimButtonRect.set(cardLeft + btnMargin, closeBottomTop, cardRight - btnMargin, closeBottomTop + btnH)
            val closePress = if (isClaimPressed) 2f * dp else 0f

            // 3D bottom lip
            scratch.set(claimButtonRect.left, claimButtonRect.top + depth, claimButtonRect.right, claimButtonRect.bottom)
            buildChamferedPath(lipPath, scratch, btnChamfer)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF050B14.toInt()
            canvas.drawPath(lipPath, paint)

            // Raised face
            scratch2.set(claimButtonRect.left, claimButtonRect.top + closePress, claimButtonRect.right, claimButtonRect.bottom - depth + closePress)
            buildChamferedPath(buttonPath, scratch2, btnChamfer)
            paint.shader = LinearGradient(
                scratch2.left, scratch2.top, scratch2.right, scratch2.bottom,
                if (isClaimPressed) 0xFF1E2838.toInt() else 0xFF141C28.toInt(),
                0xFF0A0F18.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(buttonPath, paint)
            paint.shader = null

            // Specular top highlight on close button
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0x55FFFFFF
            canvas.drawLine(scratch2.left + 12f * dp, scratch2.top + 1f * dp, scratch2.right - 12f * dp, scratch2.top + 1f * dp, paint)

            // Border
            paint.strokeWidth = 1.2f * dp
            paint.color = 0x88FFFFFF.toInt()
            canvas.drawPath(buttonPath, paint)

            textPaint.textSize = 10.5f * dp
            textPaint.letterSpacing = 0.10f
            textPaint.color = 0xDDFFFFFF.toInt()
            canvas.drawText(t("CLOSE").uppercase(), scratch2.centerX(), scratch2.centerY() + 4f * dp, textPaint)
        }

        textPaint.letterSpacing = 0f
    }

    private fun drawLightningBolt(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val h = size
        val w = size * 0.6f
        lightningBoltPath.rewind()
        lightningBoltPath.moveTo(cx + w * 0.15f, cy - h * 0.5f)
        lightningBoltPath.lineTo(cx - w * 0.45f, cy + h * 0.05f)
        lightningBoltPath.lineTo(cx - w * 0.05f, cy + h * 0.05f)
        lightningBoltPath.lineTo(cx - w * 0.25f, cy + h * 0.5f)
        lightningBoltPath.lineTo(cx + w * 0.45f, cy - h * 0.05f)
        lightningBoltPath.lineTo(cx + w * 0.05f, cy - h * 0.05f)
        lightningBoltPath.close()
        canvas.drawPath(lightningBoltPath, paint)
    }

    private fun buildChamferedPath(path: Path, rect: RectF, c: Float) {
        val chamfer = min(c, min(rect.width(), rect.height()) * 0.5f)
        path.reset()
        path.moveTo(rect.left + chamfer, rect.top)
        path.lineTo(rect.right - chamfer, rect.top)
        path.lineTo(rect.right, rect.top + chamfer)
        path.lineTo(rect.right, rect.bottom - chamfer)
        path.lineTo(rect.right - chamfer, rect.bottom)
        path.lineTo(rect.left + chamfer, rect.bottom)
        path.lineTo(rect.left, rect.bottom - chamfer)
        path.lineTo(rect.left, rect.top + chamfer)
        path.close()
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
