package com.moonsolstudios.kavvoro.ui.screens.leaderboards

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.UiTypography
import com.moonsolstudios.kavvoro.ui.render.UiWidgetRenderer
import kotlin.math.min

/**
 * Procedural AAA renderer for the Leaderboards screen (Multiverse Hall of Fame).
 */
object LeaderboardUiRenderer {

    private val avatarClipPath = Path()
    private val lightningBadgePath = Path()
    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val scratchRect3 = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val oxaniumBold: Typeface
        get() = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)

    private val oxaniumNormal: Typeface
        get() = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.NORMAL)

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. TOP HEADER HUD PLAQUE
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawHeaderPlaque(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        configured: Boolean,
        accountStatusLabel: String,
        accountStatusColor: Int,
        backButton: RectF,
        active: Boolean,
        drawBackButton: (Canvas, RectF, Boolean) -> Unit,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val plaqueH = 44f * dp
        val plaqueBottom = top + plaqueH
        scratchRect.set(left, top, right, plaqueBottom)

        // Frosted plaque slab
        paint.style = Paint.Style.FILL
        paint.color = 0xF4081324.toInt()
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)

        // Dual laser border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(
            left, top, right, plaqueBottom,
            intArrayOf(0xFF1DE8C8.toInt(), 0x5500E5FF, 0xFFFF4D8D.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)
        paint.shader = null

        // Specular top highlight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + 14f * dp, top + 1f * dp, right - 14f * dp, top + 1f * dp, paint)

        // Back button
        drawBackButton(canvas, backButton, active)

        // Keep the title block between the leading Back control and trailing status pill.
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = oxaniumBold
        textPaint.textSize = UiTypography.EYEBROW_DP * dp
        textPaint.color = 0xFF1DE8C8.toInt()
        val statusPillW = minOf(120f * dp, (right - left) * 0.34f).coerceAtLeast(80f * dp)
        val statusPillH = 20f * dp
        val pillRight = right - 14f * dp
        val pillLeft = pillRight - statusPillW
        val textLeft = maxOf(left + 14f * dp, backButton.right + 12f * dp)
        val textRight = pillLeft - 10f * dp
        val textWidth = (textRight - textLeft).coerceAtLeast(1f * dp)
        val kickerWidth = (textWidth - 20f * dp).coerceAtLeast(1f * dp)
        val kicker = fitText(t("Global Hall of Fame // Top Sigmas").uppercase(), kickerWidth)
        canvas.drawText("✦ $kicker ✦", textLeft, top + 14f * dp, textPaint)

        textPaint.textSize = UiTypography.PANEL_TITLE_DP * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.setShadowLayer(8f * dp, 0f, 0f, 0x881DE8C8.toInt())
        val titleText = fitText(t("LEADERBOARDS").uppercase(), textWidth)
        canvas.drawText(titleText, textLeft, top + 34f * dp, textPaint)
        textPaint.clearShadowLayer()

        // Google Play connection status pill, anchored to the trailing edge.
        val pillTop = top + (plaqueH - statusPillH) * 0.5f
        scratchRect2.set(pillLeft, pillTop, pillRight, pillTop + statusPillH)

        val pillAccent = if (configured) accountStatusColor else 0xFF8AA6FF.toInt()
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(pillAccent, 35)
        canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(pillAccent, 180)
        canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)

        // Specular top highlight line on pill
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(scratchRect2.left + 4f * dp, scratchRect2.top + 1f * dp, scratchRect2.right - 4f * dp, scratchRect2.top + 1f * dp, paint)

        // Indicator dot
        paint.style = Paint.Style.FILL
        paint.color = pillAccent
        canvas.drawCircle(scratchRect2.left + 8f * dp, scratchRect2.centerY(), 2.8f * dp, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 7.4f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        val statusLabel = if (configured) accountStatusLabel.uppercase() else t("LOCAL RECORDS").uppercase()
        canvas.drawText(fitText(statusLabel, statusPillW - 20f * dp), scratchRect2.left + 16f * dp, scratchRect2.centerY() + 2.6f * dp, textPaint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. PLAYER MULTIVERSE DOSSIER (COMMANDER BANNER)
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawPlayerDossier(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        highestLevelText: String,
        bestStreakText: String,
        selectedSkin: BallSkin?,
        artBitmap: Bitmap?,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val bannerH = 62f * dp
        scratchRect.set(left, top, right, top + bannerH)

        // Monolithic frosted slab
        paint.style = Paint.Style.FILL
        paint.color = 0xF6091322.toInt()
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)

        // Dual laser border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.shader = LinearGradient(
            left, top, right, top + bannerH,
            intArrayOf(0xFF1DE8C8.toInt(), 0x338AA6FF, 0xFFFF4D8D.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)
        paint.shader = null

        // Specular highlight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + 12f * dp, top + 1f * dp, right - 12f * dp, top + 1f * dp, paint)

        // Left: Player equipped character avatar
        val avatarRadius = 22f * dp
        val avatarCx = left + avatarRadius + 14f * dp
        val avatarCy = top + bannerH * 0.5f

        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            avatarCx, avatarCy, avatarRadius * 1.4f,
            intArrayOf(0x661DE8C8, 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(avatarCx, avatarCy, avatarRadius * 1.4f, paint)
        paint.shader = null

        val saveCount = canvas.save()
        avatarClipPath.rewind()
        avatarClipPath.addCircle(avatarCx, avatarCy, avatarRadius, Path.Direction.CW)
        canvas.clipPath(avatarClipPath)

        if (artBitmap != null && !artBitmap.isRecycled) {
            scratchRect2.set(avatarCx - avatarRadius, avatarCy - avatarRadius, avatarCx + avatarRadius, avatarCy + avatarRadius)
            canvas.drawBitmap(artBitmap, null, scratchRect2, null)
        } else {
            paint.style = Paint.Style.FILL
            paint.color = selectedSkin?.primary ?: 0xFF1DE8C8.toInt()
            canvas.drawCircle(avatarCx, avatarCy, avatarRadius, paint)
        }
        canvas.restoreToCount(saveCount)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.color = 0xFF1DE8C8.toInt()
        canvas.drawCircle(avatarCx, avatarCy, avatarRadius, paint)

        // Commander label & name
        val infoLeft = avatarCx + avatarRadius + 12f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 7.5f * dp
        textPaint.color = 0xFF1DE8C8.toInt()
        val commanderTag = "👑 ${t("ACTIVE SIGMA COMMANDER").uppercase()}"
        canvas.drawText(commanderTag, infoLeft, top + 22f * dp, textPaint)

        textPaint.textSize = 14.5f * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        val charName = selectedSkin?.name ?: "KAVVORO"
        canvas.drawText(charName, infoLeft, top + 42f * dp, textPaint)

        // Right: Grand Stat Pedestals
        val statW = 120f * dp
        val stat2Right = right - 14f * dp
        val stat2Left = stat2Right - statW
        val stat1Right = stat2Left - 10f * dp
        val stat1Left = stat1Right - statW

        // Stat 1: Highest Level
        scratchRect2.set(stat1Left, top + 8f * dp, stat1Right, top + bannerH - 8f * dp)
        paint.style = Paint.Style.FILL
        paint.color = 0x331DE8C8.toInt()
        canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = 0xAA1DE8C8.toInt()
        canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)

        // Specular top highlight on stat 1
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(scratchRect2.left + 4f * dp, scratchRect2.top + 1f * dp, scratchRect2.right - 4f * dp, scratchRect2.top + 1f * dp, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 7.2f * dp
        textPaint.color = 0xAAFFFFFF.toInt()
        val levelRecordTag = "🏆 ${t("BEST").uppercase()} ${t("LEVEL").uppercase()}"
        canvas.drawText(levelRecordTag, scratchRect2.centerX(), scratchRect2.top + 13f * dp, textPaint)
        textPaint.textSize = 18f * dp
        textPaint.color = 0xFF1DE8C8.toInt()
        canvas.drawText(highestLevelText, scratchRect2.centerX(), scratchRect2.top + 35f * dp, textPaint)

        // Stat 2: Longest Streak
        scratchRect3.set(stat2Left, top + 8f * dp, stat2Right, top + bannerH - 8f * dp)
        paint.style = Paint.Style.FILL
        paint.color = 0x33FF4D8D.toInt()
        canvas.drawRoundRect(scratchRect3, 6f * dp, 6f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = 0xAAFF4D8D.toInt()
        canvas.drawRoundRect(scratchRect3, 6f * dp, 6f * dp, paint)

        // Specular top highlight on stat 2
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(scratchRect3.left + 4f * dp, scratchRect3.top + 1f * dp, scratchRect3.right - 4f * dp, scratchRect3.top + 1f * dp, paint)

        textPaint.typeface = oxaniumBold
        textPaint.textSize = 7.2f * dp
        textPaint.color = 0xAAFFFFFF.toInt()
        val streakRecordTag = "🔥 ${t("BEST MEW STREAK").uppercase()}"
        canvas.drawText(streakRecordTag, scratchRect3.centerX(), scratchRect3.top + 13f * dp, textPaint)
        textPaint.textSize = 18f * dp
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText(bestStreakText, scratchRect3.centerX(), scratchRect3.top + 35f * dp, textPaint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. TOURNAMENT CHAMPIONSHIP CARDS
    // ─────────────────────────────────────────────────────────────────────────────
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
        dp: Float,
        t: (String) -> String = { it }
    ) {
        val corner = 10f * dp

        // Monolithic Polycarbonate Slab
        paint.style = Paint.Style.FILL
        paint.color = if (classic) 0xF6091424.toInt() else 0xF618051E.toInt()
        canvas.drawRoundRect(rect, corner, corner, paint)

        // Accent gradient fill
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(withAlpha(accent, if (active) 75 else 45), 0x00000000, withAlpha(accent, 25)),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        // Specular top highlight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(rect.left + 12f * dp, rect.top + 1f * dp, rect.right - 12f * dp, rect.top + 1f * dp, paint)

        // Laser perimeter
        paint.strokeWidth = (if (active) 2f else 1.3f) * dp
        paint.color = withAlpha(accent, if (active) 255 else 200)
        canvas.drawRoundRect(rect, corner, corner, paint)

        // Left Emblem Bay: Golden Trophy / Skull / Target / Spark
        val badgeRadius = min(rect.height() * 0.32f, 26f * dp)
        val badgeCx = rect.left + badgeRadius + 16f * dp
        val badgeCy = rect.centerY()

        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            badgeCx, badgeCy, badgeRadius * 1.4f,
            intArrayOf(withAlpha(accent, 85), 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius * 1.4f, paint)
        paint.shader = null

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 35)
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * dp
        paint.color = accent
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, paint)

        // Inner custom procedural emblem
        paint.strokeWidth = 2.2f * dp
        paint.color = 0xFFFFFFFF.toInt()
        when {
            classic && levelBoard -> {
                // Golden Trophy icon
                val r = badgeRadius * 0.55f
                scratchRect2.set(badgeCx - r, badgeCy - r, badgeCx + r, badgeCy + r * 0.4f)
                canvas.drawArc(scratchRect2, 0f, 180f, false, paint)
                canvas.drawLine(badgeCx, badgeCy + r * 0.4f, badgeCx, badgeCy + r * 0.85f, paint)
                canvas.drawLine(badgeCx - r * 0.6f, badgeCy + r * 0.85f, badgeCx + r * 0.6f, badgeCy + r * 0.85f, paint)
            }
            !classic && levelBoard -> {
                // Glitch Skull / Singularity icon
                val r = badgeRadius * 0.52f
                scratchRect2.set(badgeCx - r, badgeCy - r, badgeCx + r, badgeCy + r * 0.5f)
                canvas.drawArc(scratchRect2, 180f, 180f, false, paint)
                canvas.drawLine(badgeCx - r, badgeCy, badgeCx - r * 0.5f, badgeCy + r * 0.9f, paint)
                canvas.drawLine(badgeCx + r, badgeCy, badgeCx + r * 0.5f, badgeCy + r * 0.9f, paint)
                canvas.drawLine(badgeCx - r * 0.5f, badgeCy + r * 0.9f, badgeCx + r * 0.5f, badgeCy + r * 0.9f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(badgeCx - r * 0.4f, badgeCy - r * 0.1f, 2f * dp, paint)
                canvas.drawCircle(badgeCx + r * 0.4f, badgeCy - r * 0.1f, 2f * dp, paint)
            }
            classic && !levelBoard -> {
                // Streak Target / Orbit icon
                val r = badgeRadius * 0.55f
                paint.style = Paint.Style.STROKE
                canvas.drawCircle(badgeCx, badgeCy, r, paint)
                canvas.drawCircle(badgeCx, badgeCy, r * 0.45f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(badgeCx, badgeCy, 2.2f * dp, paint)
            }
            else -> {
                // Inferno Spark / Lightning icon
                val r = badgeRadius * 0.6f
                paint.style = Paint.Style.STROKE
                lightningBadgePath.rewind()
                lightningBadgePath.moveTo(badgeCx + 2f * dp, badgeCy - r)
                lightningBadgePath.lineTo(badgeCx - r * 0.5f, badgeCy + 1f * dp)
                lightningBadgePath.lineTo(badgeCx + 1f * dp, badgeCy + 1f * dp)
                lightningBadgePath.lineTo(badgeCx - 2f * dp, badgeCy + r)
                lightningBadgePath.lineTo(badgeCx + r * 0.5f, badgeCy - 1f * dp)
                lightningBadgePath.lineTo(badgeCx - 1f * dp, badgeCy - 1f * dp)
                lightningBadgePath.close()
                canvas.drawPath(lightningBadgePath, paint)
            }
        }

        // Center Info Area
        val textLeft = badgeCx + badgeRadius + 14f * dp
        val ctaWidth = 120f * dp
        val textRight = rect.right - ctaWidth - 14f * dp
        val textW = textRight - textLeft

        // Discipline Pill (top)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 7.8f * dp
        textPaint.color = accent
        val fullMode = if (classic) {
            "● ${t("CLASSIC ODYSSEY // SPEEDRUN & SKILL").uppercase()}"
        } else {
            "● ${t("BRAINROT CHAOS // MAX RISK").uppercase()}"
        }
        canvas.drawText(fullMode, textLeft, rect.top + 21f * dp, textPaint)

        // Title
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 15.5f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.setShadowLayer(4f * dp, 0f, 0f, withAlpha(accent, 140))
        canvas.drawText(titleLabel, textLeft, rect.top + 43f * dp, textPaint)
        textPaint.clearShadowLayer()

        // Subtitle
        textPaint.typeface = oxaniumNormal
        textPaint.textSize = 8.8f * dp
        textPaint.color = 0x99FFFFFF.toInt()
        val desc = if (levelBoard) {
            t("50 Levels Pure Skill • Fair-Play Multiverse")
        } else {
            t("Consecutive Clean Rifts Without Failure")
        }
        canvas.drawText(desc, textLeft, rect.top + 61f * dp, textPaint)

        // Right Bay: Grand Score & CTA Button
        val ctaLeft = rect.right - ctaWidth - 12f * dp
        val scoreCy = rect.top + 32f * dp
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 26f * dp
        textPaint.color = accent
        textPaint.setShadowLayer(6f * dp, 0f, 0f, withAlpha(accent, 200))
        canvas.drawText(scoreText, rect.right - 14f * dp, scoreCy, textPaint)
        textPaint.clearShadowLayer()

        // CTA Button (Chunky 3D Candy Arcade Button)
        val ctaH = 26f * dp
        val ctaTop = rect.bottom - ctaH - 12f * dp
        val depth = 2.4f * dp
        val btnCorner = 6f * dp
        val press = if (active) 1.2f * dp else 0f
        val btnRight = rect.right - 14f * dp

        // 3D Bottom Lip
        scratchRect.set(ctaLeft, ctaTop + depth, btnRight, ctaTop + ctaH)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF040810.toInt()
        canvas.drawRoundRect(scratchRect, btnCorner, btnCorner, paint)
        paint.color = withAlpha(accent, 180)
        canvas.drawRoundRect(scratchRect, btnCorner, btnCorner, paint)

        // Raised Button Face
        scratchRect2.set(ctaLeft, ctaTop + press, btnRight, ctaTop + ctaH - depth + press)
        paint.shader = LinearGradient(
            scratchRect2.left, scratchRect2.top, scratchRect2.left, scratchRect2.bottom,
            accent, withAlpha(accent, 175),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect2, btnCorner, btnCorner, paint)
        paint.shader = null

        // Curved Glossy Specular Glare on top 48%
        scratchRect3.set(scratchRect2.left + 1.2f * dp, scratchRect2.top + 0.8f * dp, scratchRect2.right - 1.2f * dp, scratchRect2.top + scratchRect2.height() * 0.48f)
        paint.shader = LinearGradient(
            scratchRect3.left, scratchRect3.top, scratchRect3.left, scratchRect3.bottom,
            0x80FFFFFF.toInt(), 0x05FFFFFF,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect3, (btnCorner - 1.2f * dp).coerceAtLeast(2f), (btnCorner - 1.2f * dp).coerceAtLeast(2f), paint)
        paint.shader = null

        // Specular Top Highlight Line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0xDDFFFFFF.toInt()
        canvas.drawLine(scratchRect2.left + 6f * dp, scratchRect2.top + 0.8f * dp, scratchRect2.right - 6f * dp, scratchRect2.top + 0.8f * dp, paint)

        // Border
        paint.strokeWidth = 1.4f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(scratchRect2, btnCorner, btnCorner, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 8.8f * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.setShadowLayer(3f * dp, 0f, 1f * dp, 0x99000000.toInt())
        val ctaText = "${t("GLOBAL TOP").uppercase()} ►"
        canvas.drawText(ctaText, scratchRect2.centerX(), scratchRect2.centerY() + 3f * dp, textPaint)
        textPaint.clearShadowLayer()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. FAIR PLAY STATUS BANNER
    // ─────────────────────────────────────────────────────────────────────────────
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
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) = UiWidgetRenderer.drawStatusBanner(
        canvas = canvas,
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        headerText = headerText,
        message = message,
        accent = accent,
        transient = transient,
        paint = paint,
        textPaint = textPaint,
        dp = dp,
        t = t,
        fitText = fitText
    )

    // ─────────────────────────────────────────────────────────────────────────────
    // 5. MASTER SCREEN DRAW
    // ─────────────────────────────────────────────────────────────────────────────
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
        accountStatusLabel: String,
        accountStatusColor: Int,
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
        drawBackButton: (Canvas, RectF, Boolean) -> Unit,
        selectedSkin: BallSkin? = null,
        playerBrainballBitmap: Bitmap? = null
    ) {
        val topPlaqueY = top56 - 28f * dp

        // 1. Top HUD Plaque
        drawHeaderPlaque(
            canvas = canvas,
            left = pageLeft,
            top = topPlaqueY,
            right = pageRight,
            configured = configured,
            accountStatusLabel = accountStatusLabel,
            accountStatusColor = accountStatusColor,
            backButton = leaderboardBackButton,
            active = activeLeaderboardIndex == -2,
            drawBackButton = drawBackButton,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )

        // 2. Commander Banner / Player Dossier
        val dossierTop = topPlaqueY + 52f * dp
        drawPlayerDossier(
            canvas = canvas,
            left = pageLeft,
            top = dossierTop,
            right = pageRight,
            highestLevelText = highestLevelText,
            bestStreakText = bestStreakText,
            selectedSkin = selectedSkin,
            artBitmap = playerBrainballBitmap,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )

        // 3. Four Grand Tournament Championship Cards
        val boards = listOf(
            Triple(true, true, 0xFF1DE8C8.toInt()),    // Classic Level
            Triple(false, true, 0xFFFF4D8D.toInt()),   // Chaos Level
            Triple(true, false, 0xFF1DE8C8.toInt()),   // Classic Streak
            Triple(false, false, 0xFFFF4D8D.toInt())   // Chaos Streak
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
                dp = dp,
                t = t
            )
        }

        // 4. Multiverse Tournament Telemetry Panel (for expanded viewports)
        val lastCard = leaderboardItemRects.lastOrNull()
        if (lastCard != null && bottom70 - lastCard.bottom >= 95f * dp) {
            drawMultiverseTelemetry(
                canvas = canvas,
                left = pageLeft,
                top = lastCard.bottom + 12f * dp,
                right = pageRight,
                bottom = bottom70 - 10f * dp,
                paint = paint,
                dp = dp,
                t = t,
                fitText = fitText
            )
        }

        // 5. Fair Play Status Plaque
        val footerText = leaderboardMessage.ifBlank {
            t(if (configured) "SELECT A BOARD" else "GLOBAL SYNC OFFLINE")
        }
        drawStatusBanner(
            canvas = canvas,
            left = pageLeft,
            top = bottom70,
            right = pageRight,
            bottom = bottom16,
            headerText = "🛡️ ${t(if (leaderboardMessage.isNotBlank()) "STATUS UPDATE" else "FAIR PLAY PROTOCOL ACTIVE // GOOGLE PLAY GAMES").uppercase()}",
            message = footerText,
            accent = if (configured) 0xFF8AA6FF.toInt() else 0xFF6F7788.toInt(),
            transient = leaderboardMessage.isNotBlank(),
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )
    }

    private fun drawMultiverseTelemetry(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val availableH = bottom - top
        if (availableH < 70f * dp) return
        val panelH = minOf(availableH, 150f * dp)
        val h = panelH
        scratchRect.set(left, top, right, top + panelH)

        // Monolithic Glass panel
        paint.style = Paint.Style.FILL
        paint.color = 0xEE08101D.toInt()
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x448AA6FF.toInt()
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)

        // Top specular highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + 14f * dp, top + 1f * dp, right - 14f * dp, top + 1f * dp, paint)

        // Header
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = oxaniumBold
        textPaint.textSize = 7.8f * dp
        textPaint.color = 0xFF8AA6FF.toInt()
        val header = "✦ ${t("MULTIVERSE ARENA PROTOCOL // OFFICIAL GOOGLE PLAY DIVISIONS").uppercase()} ✦"
        canvas.drawText(header, left + 14f * dp, top + 17f * dp, textPaint)

        // 4 Division Badges
        val divisions = listOf(
            Pair("GRANDMASTER", 0xFF00E5FF.toInt()),
            Pair("DIAMOND", 0xFFBD00FF.toInt()),
            Pair("PLATINUM", 0xFFFF2E93.toInt()),
            Pair("GOLD", 0xFFFFCF4A.toInt())
        )
        val divGap = 8f * dp
        val divW = (scratchRect.width() - 28f * dp - divGap * (divisions.size - 1)) / divisions.size
        val divH = minOf(38f * dp, h * 0.30f)
        val divTop = top + 25f * dp

        divisions.forEachIndexed { idx, (name, color) ->
            val dLeft = left + 14f * dp + idx * (divW + divGap)
            scratchRect2.set(dLeft, divTop, dLeft + divW, divTop + divH)

            paint.style = Paint.Style.FILL
            paint.color = withAlpha(color, 32)
            canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = withAlpha(color, 160)
            canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)

            // Specular highlight on division badge
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratchRect2.left + 4f * dp, scratchRect2.top + 1f * dp, scratchRect2.right - 4f * dp, scratchRect2.top + 1f * dp, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = oxaniumBold
            textPaint.textSize = 7.2f * dp
            textPaint.color = color
            canvas.drawText(name, scratchRect2.centerX(), scratchRect2.top + 14f * dp, textPaint)

            textPaint.typeface = oxaniumNormal
            textPaint.textSize = 6.6f * dp
            textPaint.color = 0xAAFFFFFF.toInt()
            val subTier = when (idx) {
                0 -> "TOP 1%"
                1 -> "TOP 10%"
                2 -> "TOP 25%"
                else -> "TOP 50%"
            }
            canvas.drawText(subTier, scratchRect2.centerX(), scratchRect2.top + 28f * dp, textPaint)
        }

        // Telemetry status bullets
        if (h >= 115f * dp) {
            val bulletTop = divTop + divH + 18f * dp
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = oxaniumNormal
            textPaint.textSize = 8.2f * dp
            textPaint.color = 0xCCF7F4FF.toInt()
            val b1 = fitText(
                "● ${t("AUTO SYNC: Classic & Chaos records sync instantaneously to global leaderboards.")}",
                scratchRect.width() - 28f * dp
            )
            canvas.drawText(b1, left + 14f * dp, bulletTop, textPaint)

            val b2 = fitText(
                "● ${t("ANTI-EXPLOIT GUARD: Scores are cryptographically validated by LeaderboardScoreGuard.")}",
                scratchRect.width() - 28f * dp
            )
            canvas.drawText(b2, left + 14f * dp, bulletTop + 18f * dp, textPaint)

            if (h >= 150f * dp) {
                val b3 = fitText(
                    "● ${t("MULTIVERSE REWARD: Reaching Grandmaster Division unlocks an exclusive aura in the Vault.")}",
                    scratchRect.width() - 28f * dp
                )
                canvas.drawText(b3, left + 14f * dp, bulletTop + 36f * dp, textPaint)
            }
        }
    }
}
