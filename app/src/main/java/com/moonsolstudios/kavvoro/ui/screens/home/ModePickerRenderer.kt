package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Modern Monolithic AAA Renderer for the Mode Selection screen.
 * Engineered for maximum readability, sleek high-contrast aesthetics, and authentic game character representation:
 * 1. Monolithic Dark Polycarbonate Slabs (96% opacity) with zero heavy frames or visual clutter.
 * 2. Ultra-Legible Typographic Hierarchy (crisp, large text visible against any background).
 * 3. Authentic Hero Mascots: Real game Brainballs (Nodlo & Gigi Glitch) rendered large on holographic auras.
 * 4. Satisfying 3D Tactile Action Buttons with glass specular reflections.
 */
object ModePickerRenderer {

    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val capsuleRect = RectF()
    private val buttonFaceRect = RectF()
    private val buttonLipRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mascotSrcRect = Rect()
    private val brandDiamondPath = Path()

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or ((alpha and 0xFF) shl 24)
    }

    fun drawPlayModeScreen(
        canvas: Canvas,
        menuClassicCard: RectF,
        menuChaosCard: RectF,
        menuClassicContinueButton: RectF,
        menuClassicNewButton: RectF,
        menuChaosContinueButton: RectF,
        menuChaosNewButton: RectF,
        menuChaosStartButton: RectF,
        menuContinueButton: RectF,
        activeMenuButton: MenuButton,
        classicProgress: Int,
        chaosProgress: Int,
        classicStreak: Int,
        chaosStreak: Int,
        compact: Boolean,
        short: Boolean,
        safeCenterX: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        menuButtonAt: (Float, Float) -> MenuButton,
        mascotBitmapClassic: Bitmap? = null,
        mascotBitmapChaos: Bitmap? = null,
        cardFrameClassic: Bitmap? = null,
        cardFrameChaos: Bitmap? = null,
        stateElapsed: Float = 0f
    ) {
        val headingBottom = menuClassicCard.top - (if (compact) 14f else 20f) * dp

        // 1. High-Contrast AAA HUD Backplate (ensures 100% crisp legibility over space stars)
        val hudW = min(menuClassicCard.width() * 0.78f, (if (compact) 320f else 440f) * dp)
        val hudH = (if (compact) 54f else 62f) * dp
        val hudTop = headingBottom - hudH - (if (compact) 6f else 10f) * dp
        scratchRect.set(safeCenterX - hudW * 0.5f, hudTop, safeCenterX + hudW * 0.5f, hudTop + hudH)

        // Soft dark sapphire glass plaque
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
            0xF4081324.toInt(), 0xFC030712.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 14f * dp, 14f * dp, paint)
        paint.shader = null

        // Laser gradient rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.right, scratchRect.top,
            0xAA00E5FF.toInt(), 0xAAFF2E93.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 14f * dp, 14f * dp, paint)
        paint.shader = null

        // 2. Micro Protocol Tag
        val missionTag = "✦  ${t("MULTIVERSE PORTAL").uppercase()} // ${t("PORTAL RIFT").uppercase()}  ✦"
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = (if (compact) 8.5f else 10f) * dp
        textPaint.color = 0xFF00E5FF.toInt()
        textPaint.letterSpacing = 0.08f
        val pillY = scratchRect.top + (if (compact) 18f else 21f) * dp
        canvas.drawText(fitText(missionTag, scratchRect.width() - 28f * dp), safeCenterX, pillY, textPaint)

        // 3. Dominant Title Header: "ALEGE MODUL"
        textPaint.textSize = (if (compact) 21f else 27f) * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.06f
        textPaint.setShadowLayer(10f * dp, 0f, 2f * dp, 0xFF000000.toInt())
        val titleY = scratchRect.top + (if (compact) 42f else 50f) * dp
        canvas.drawText(fitText(t("CHOOSE MODE").uppercase(), scratchRect.width() - 28f * dp), safeCenterX, titleY, textPaint)
        textPaint.clearShadowLayer()

        // 4. Cyber Split Dual Gradient Divider with Center Diamond
        val divW = (if (compact) 100f else 140f) * dp
        val divY = headingBottom - 2f * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(
            safeCenterX - divW, divY,
            safeCenterX - 6f * dp, divY,
            0x0000E5FF, 0x8800E5FF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(safeCenterX - divW, divY, safeCenterX - 6f * dp, divY, paint)

        paint.shader = LinearGradient(
            safeCenterX + 6f * dp, divY,
            safeCenterX + divW, divY,
            0x88FF2E93.toInt(), 0x00FF2E93,
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(safeCenterX + 6f * dp, divY, safeCenterX + divW, divY, paint)
        paint.shader = null

        paint.style = Paint.Style.FILL
        paint.color = 0xCCFFFFFF.toInt()
        brandDiamondPath.rewind()
        brandDiamondPath.moveTo(safeCenterX, divY - 3f * dp)
        brandDiamondPath.lineTo(safeCenterX + 3f * dp, divY)
        brandDiamondPath.lineTo(safeCenterX, divY + 3f * dp)
        brandDiamondPath.lineTo(safeCenterX - 3f * dp, divY)
        brandDiamondPath.close()
        canvas.drawPath(brandDiamondPath, paint)

        // Mode 1: CLASSIC (Quantum Odyssey / Steady Progression)
        val classicDetail = "★ ${t("STEADY PROGRESSION").uppercase()}  •  50 ${t("LEVELS").uppercase()} 🌌"
        drawModeCard(
            canvas = canvas,
            rect = menuClassicCard,
            title = t("CLASSIC").uppercase(),
            description = classicDetail,
            modeBadge = "[ ✦ ${t("STANDARD RULES").uppercase()} ✦ ]",
            accent = 0xFF00E5FF.toInt(),
            active = activeMenuButton == MenuButton.CLASSIC,
            activeRun = classicProgress > 1,
            levelText = "${t("LEVEL").uppercase()} ${classicProgress.toString().padStart(2, '0')}",
            streakText = classicStreak.toString(),
            activeRunLabel = t("ACTIVE RUN").uppercase(),
            noActiveRunLabel = t("NO ACTIVE RUN").uppercase(),
            bestStreakLabel = t("BEST STREAK").uppercase(),
            startFreshLabel = t("START FRESH WHEN READY").uppercase(),
            continueLabel = t("CONTINUE").uppercase(),
            newGameLabel = t("NEW GAME").uppercase(),
            startLabel = t("START NEW GAME").uppercase(),
            compact = compact,
            short = short,
            continueButton = menuClassicContinueButton,
            newGameButton = menuClassicNewButton,
            startButton = menuClassicNewButton,
            activeButtonId = activeMenuButton.ordinal,
            continueButtonId = if (!menuClassicContinueButton.isEmpty) menuButtonAt(menuClassicContinueButton.centerX(), menuClassicContinueButton.centerY()).ordinal else -1,
            newGameButtonId = if (!menuClassicNewButton.isEmpty) menuButtonAt(menuClassicNewButton.centerX(), menuClassicNewButton.centerY()).ordinal else -1,
            startButtonId = if (!menuClassicNewButton.isEmpty) menuButtonAt(menuClassicNewButton.centerX(), menuClassicNewButton.centerY()).ordinal else -1,
            mascotBitmap = mascotBitmapClassic,
            mascotTag = "👑 CHAD NODLO",
            isChaos = false,
            stateElapsed = stateElapsed,
            paint = paint,
            dp = dp,
            fitText = fitText,
            drawIcon = { c, r -> drawWorldAsset(c, "portal_goal", r, 235) },
            progressInt = classicProgress,
            cardFrameBitmap = cardFrameClassic,
            t = t
        )

        // Mode 2: CHAOS (Glitch Mayhem / Extreme Modifiers)
        val chaosDetail = "💀 ${t("UNLEASH THE CHAOS").uppercase()}  •  2X ${t("HYPE").uppercase()} 🔥"
        drawModeCard(
            canvas = canvas,
            rect = menuChaosCard,
            title = t("CHAOS").uppercase(),
            description = chaosDetail,
            modeBadge = "[ 💀 ${t("WILD MODIFIERS").uppercase()} 💀 ]",
            accent = 0xFFFF007F.toInt(),
            active = activeMenuButton == MenuButton.CHAOS,
            activeRun = chaosProgress > 1,
            levelText = "${t("LEVEL").uppercase()} ${chaosProgress.toString().padStart(2, '0')}",
            streakText = chaosStreak.toString(),
            activeRunLabel = t("ACTIVE RUN").uppercase(),
            noActiveRunLabel = t("NO ACTIVE RUN").uppercase(),
            bestStreakLabel = t("BEST STREAK").uppercase(),
            startFreshLabel = t("START FRESH WHEN READY").uppercase(),
            continueLabel = t("CONTINUE").uppercase(),
            newGameLabel = t("NEW GAME").uppercase(),
            startLabel = t("START NEW GAME").uppercase(),
            compact = compact,
            short = short,
            continueButton = menuChaosContinueButton,
            newGameButton = menuChaosNewButton,
            startButton = menuChaosStartButton,
            activeButtonId = activeMenuButton.ordinal,
            continueButtonId = if (!menuChaosContinueButton.isEmpty) menuButtonAt(menuChaosContinueButton.centerX(), menuChaosContinueButton.centerY()).ordinal else -1,
            newGameButtonId = if (!menuChaosNewButton.isEmpty) menuButtonAt(menuChaosNewButton.centerX(), menuChaosNewButton.centerY()).ordinal else -1,
            startButtonId = if (!menuChaosStartButton.isEmpty) menuButtonAt(menuChaosStartButton.centerX(), menuChaosStartButton.centerY()).ordinal else -1,
            mascotBitmap = mascotBitmapChaos,
            mascotTag = "⚡ GIGI GLITCH 💀",
            isChaos = true,
            stateElapsed = stateElapsed,
            paint = paint,
            dp = dp,
            fitText = fitText,
            drawIcon = { c, r -> drawWorldAsset(c, "hazard_glitch", r, 235) },
            progressInt = chaosProgress,
            cardFrameBitmap = cardFrameChaos,
            t = t
        )

        drawBackButton(
            canvas = canvas,
            rect = menuContinueButton,
            label = t("BACK TO HOME").uppercase(),
            short = short,
            compact = compact,
            active = activeMenuButton == MenuButton.BACK,
            paint = paint,
            dp = dp
        )
    }

    fun drawModeCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        description: String,
        modeBadge: String,
        accent: Int,
        active: Boolean,
        activeRun: Boolean,
        levelText: String,
        streakText: String,
        activeRunLabel: String,
        noActiveRunLabel: String,
        bestStreakLabel: String,
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
        mascotBitmap: Bitmap?,
        mascotTag: String,
        isChaos: Boolean,
        stateElapsed: Float,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawIcon: (Canvas, RectF) -> Unit,
        progressInt: Int = 1,
        cardFrameBitmap: Bitmap? = null,
        t: (String) -> String = { it }
    ) {
        if (isChaos) {
            drawChaosCard(
                canvas = canvas,
                rect = rect,
                title = title,
                description = description,
                modeBadge = modeBadge,
                accent = accent,
                active = active,
                activeRun = activeRun,
                levelText = levelText,
                streakText = streakText,
                activeRunLabel = activeRunLabel,
                noActiveRunLabel = noActiveRunLabel,
                bestStreakLabel = bestStreakLabel,
                startFreshLabel = startFreshLabel,
                continueLabel = continueLabel,
                newGameLabel = newGameLabel,
                startLabel = startLabel,
                compact = compact,
                short = short,
                continueButton = continueButton,
                newGameButton = newGameButton,
                startButton = startButton,
                activeButtonId = activeButtonId,
                continueButtonId = continueButtonId,
                newGameButtonId = newGameButtonId,
                startButtonId = startButtonId,
                mascotBitmap = mascotBitmap,
                mascotTag = mascotTag,
                stateElapsed = stateElapsed,
                paint = paint,
                dp = dp,
                fitText = fitText,
                drawIcon = drawIcon,
                progressInt = progressInt,
                t = t
            )
        } else {
            drawClassicCard(
                canvas = canvas,
                rect = rect,
                title = title,
                description = description,
                modeBadge = modeBadge,
                accent = accent,
                active = active,
                activeRun = activeRun,
                levelText = levelText,
                streakText = streakText,
                activeRunLabel = activeRunLabel,
                noActiveRunLabel = noActiveRunLabel,
                bestStreakLabel = bestStreakLabel,
                startFreshLabel = startFreshLabel,
                continueLabel = continueLabel,
                newGameLabel = newGameLabel,
                startLabel = startLabel,
                compact = compact,
                short = short,
                continueButton = continueButton,
                newGameButton = newGameButton,
                startButton = startButton,
                activeButtonId = activeButtonId,
                continueButtonId = continueButtonId,
                newGameButtonId = newGameButtonId,
                startButtonId = startButtonId,
                mascotBitmap = mascotBitmap,
                mascotTag = mascotTag,
                stateElapsed = stateElapsed,
                paint = paint,
                dp = dp,
                fitText = fitText,
                drawIcon = drawIcon,
                progressInt = progressInt,
                t = t
            )
        }
    }

    /* =========================================================================
     * 1. CLASSIC CARD: Monolithic Quantum Odyssey (Maximum Legibility & Clean AAA)
     * ========================================================================= */
    private fun drawClassicCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        description: String,
        modeBadge: String,
        accent: Int,
        active: Boolean,
        activeRun: Boolean,
        levelText: String,
        streakText: String,
        activeRunLabel: String,
        noActiveRunLabel: String,
        bestStreakLabel: String,
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
        mascotBitmap: Bitmap?,
        mascotTag: String,
        stateElapsed: Float,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawIcon: (Canvas, RectF) -> Unit,
        progressInt: Int,
        t: (String) -> String = { it }
    ) {
        val cornerRad = (if (short) 14f else 18f) * dp

        // 1. Soft Ambient Outer Bloom
        paint.style = Paint.Style.STROKE
        paint.shader = null
        paint.strokeWidth = (if (active) 14f else 8f) * dp
        paint.color = withAlpha(0xFF00E5FF.toInt(), if (active) 55 else 25)
        canvas.drawRoundRect(rect, cornerRad, cornerRad, paint)

        // 2. Monolithic Dark Tinted Polycarbonate Slab (96% solid dark sapphire)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            0xF6091424.toInt(), 0xFC030710.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cornerRad, cornerRad, paint)
        paint.shader = null

        // 3. Subtle Tech Grid Pattern (Very low opacity, adds AAA surface depth)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x0800E5FF
        val gridStep = 24f * dp
        var gx = rect.left + gridStep
        while (gx < rect.right) {
            canvas.drawLine(gx, rect.top + 8f * dp, gx, rect.bottom - 8f * dp, paint)
            gx += gridStep
        }

        // 4. Razor-Sharp Perimeter Laser Border (Electric Cyan to Royal Gold)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 2f * dp else 1.3f * dp
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.top,
            0xFF00E5FF.toInt(), 0xAAFFD700.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cornerRad, cornerRad, paint)
        paint.shader = null

        // 5. Specular Top Glass Edge Highlight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.shader = LinearGradient(
            rect.left + cornerRad, rect.top + 1f * dp,
            rect.right - cornerRad, rect.top + 1f * dp,
            intArrayOf(0x0000E5FF, 0xDD00E5FF.toInt(), 0xDDFFEAA7.toInt(), 0x00FFEAA7),
            floatArrayOf(0f, 0.35f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(
            rect.left + cornerRad + 8f * dp, rect.top + 1f * dp,
            rect.right - cornerRad - 8f * dp, rect.top + 1f * dp,
            paint
        )
        paint.shader = null

        // 6. Right Column: Real Hero Mascot (Nodlo / King Kavvoro) on Clean Holographic Aura
        val mascotAreaW = minOf(rect.width() * 0.36f, rect.height() * 0.88f)
        val mascotCx = rect.right - mascotAreaW * 0.52f
        val mascotCy = rect.centerY()
        val bob = sin((stateElapsed * 2.5f).toDouble()).toFloat() * (if (short) 2f else 3.5f) * dp
        val my = mascotCy + bob

        drawHeroMascotClassic(
            canvas = canvas,
            cx = mascotCx,
            cy = my,
            areaW = mascotAreaW,
            mascotBitmap = mascotBitmap,
            tag = if (mascotTag.isNotBlank()) mascotTag else "👑 NODLO",
            stateElapsed = stateElapsed,
            paint = paint,
            dp = dp,
            short = short,
            drawIcon = drawIcon
        )

        // 7. Left Column: Open Spacious Control Bay (High Contrast & Legibility)
        val leftMargin = rect.left + (if (short) 16f else if (compact) 22f else 28f) * dp
        val bayRight = mascotCx - mascotAreaW * 0.46f - (if (short) 8f else 14f) * dp
        val contentMaxW = maxOf(100f * dp, bayRight - leftMargin)

        // A. Mode Pill Tag
        val tagTop = rect.top + (if (short) 14f else if (compact) 18f else 22f) * dp
        val tagH = (if (short) 16f else 19f) * dp
        val tagW = (if (short) 120f else 138f) * dp
        capsuleRect.set(leftMargin, tagTop, leftMargin + tagW, tagTop + tagH)

        paint.style = Paint.Style.FILL
        paint.color = 0x2800E5FF.toInt()
        canvas.drawRoundRect(capsuleRect, tagH * 0.5f, tagH * 0.5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0xAA00E5FF.toInt()
        canvas.drawRoundRect(capsuleRect, tagH * 0.5f, tagH * 0.5f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(capsuleRect.left + 4f * dp, capsuleRect.top + 1f * dp, capsuleRect.right - 4f * dp, capsuleRect.top + 1f * dp, paint)

        // Pulsing cyan LED
        val pulse = (sin((stateElapsed * 4f).toDouble()).toFloat() * 0.5f + 0.5f)
        val ledCx = capsuleRect.left + 9f * dp
        val ledCy = capsuleRect.centerY()
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF00E5FF.toInt(), (100 + pulse * 155).toInt())
        canvas.drawCircle(ledCx, ledCy, 3f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = (if (short) 7.5f else 8.5f) * dp
        textPaint.color = 0xFF00E5FF.toInt()
        val tagText = "✦ ${t("STANDARD RULES").uppercase()} ✦"
        canvas.drawText(fitText(tagText, tagW - 18f * dp), ledCx + 6f * dp, capsuleRect.centerY() + 3f * dp, textPaint)

        // B. Large Bold Mode Title: "CLASSIC"
        val titleY = capsuleRect.bottom + (if (short) 20f else if (compact) 24f else 28f) * dp
        textPaint.textSize = (if (short) 20f else if (compact) 25f else 29f) * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.06f
        textPaint.setShadowLayer(10f * dp, 0f, 2f * dp, 0xFF000000.toInt())
        canvas.drawText(fitText(title, contentMaxW), leftMargin, titleY, textPaint)
        textPaint.clearShadowLayer()

        // C. Clean High-Contrast Subtitle / Lore
        val descY = titleY + (if (short) 14f else 17f) * dp
        textPaint.textSize = (if (short) 8.5f else if (compact) 10.5f else 11.5f) * dp
        textPaint.color = 0xFFD2EAFF.toInt()
        textPaint.letterSpacing = 0.03f
        canvas.drawText(fitText(description, contentMaxW), leftMargin, descY, textPaint)

        // D. Telemetry Chips Row
        val telemY = descY + (if (short) 8f else 11f) * dp
        val chipH = (if (short) 18f else 22f) * dp

        if (activeRun) {
            // Chip 1: RUN ACTIV
            val statusW = (if (short) 82f else 96f) * dp
            capsuleRect.set(leftMargin, telemY, leftMargin + statusW, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x3300E5FF.toInt()
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0xCC00E5FF.toInt()
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(capsuleRect.left + 3f * dp, capsuleRect.top + 0.8f * dp, capsuleRect.right - 3f * dp, capsuleRect.top + 0.8f * dp, paint)

            val dotX = capsuleRect.left + 9f * dp
            val dotY = capsuleRect.centerY()
            paint.style = Paint.Style.FILL
            paint.color = 0xFF00E5FF.toInt()
            canvas.drawCircle(dotX, dotY, 2.5f * dp, paint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = (if (short) 7.5f else 9f) * dp
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.letterSpacing = 0.04f
            canvas.drawText(fitText(activeRunLabel, (capsuleRect.right - dotX - 11f * dp).coerceAtLeast(36f * dp)), dotX + 6f * dp, capsuleRect.centerY() + 3.2f * dp, textPaint)

            // Chip 2: LEVEL with Gold Accent
            val chip1X = capsuleRect.right + 6f * dp
            val chip1W = (if (short) 74f else 88f) * dp
            scratchRect.set(chip1X, telemY, chip1X + chip1W, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x55091C30.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0xCCFFD700.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratchRect.left + 3f * dp, scratchRect.top + 0.8f * dp, scratchRect.right - 3f * dp, scratchRect.top + 0.8f * dp, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = (if (short) 7.5f else 9f) * dp
            textPaint.color = 0xFFFFE57F.toInt()
            canvas.drawText(fitText("🏆 $levelText", scratchRect.width() - 8f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)

            // Chip 3: STREAK
            val chip2X = scratchRect.right + 6f * dp
            val chip2W = (if (short) 60f else 72f) * dp
            scratchRect.set(chip2X, telemY, chip2X + chip2W, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x55091C30.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f * dp
            paint.color = 0x888BA6C4.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratchRect.left + 3f * dp, scratchRect.top + 0.8f * dp, scratchRect.right - 3f * dp, scratchRect.top + 0.8f * dp, paint)

            textPaint.color = if (streakText != "0") 0xFFFFCF4A.toInt() else 0xFFE0E8F0.toInt()
            canvas.drawText("🔥 x$streakText", scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)

            // E. Sleek Progress Bar to Level 50
            val btnTop = if (!continueButton.isEmpty) continueButton.top else if (!newGameButton.isEmpty) newGameButton.top else startButton.top
            val spaceBelow = btnTop - (telemY + chipH)
            if (spaceBelow >= 20f * dp && !short) {
                val progBarY = telemY + chipH + 6f * dp
                val progBarH = 4.5f * dp
                val progTrackW = minOf(contentMaxW, (if (compact) 230f else 280f) * dp)
                scratchRect.set(leftMargin, progBarY, leftMargin + progTrackW, progBarY + progBarH)

                paint.style = Paint.Style.FILL
                paint.color = 0xFF061424.toInt()
                canvas.drawRoundRect(scratchRect, progBarH * 0.5f, progBarH * 0.5f, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.8f * dp
                paint.color = 0x4400E5FF
                canvas.drawRoundRect(scratchRect, progBarH * 0.5f, progBarH * 0.5f, paint)

                val fillPct = (progressInt.coerceIn(1, 50) / 50f)
                val fillW = maxOf(progBarH, progTrackW * fillPct)
                scratchRect2.set(leftMargin, progBarY, leftMargin + fillW, progBarY + progBarH)
                paint.style = Paint.Style.FILL
                paint.shader = LinearGradient(
                    scratchRect2.left, scratchRect2.top,
                    scratchRect2.right, scratchRect2.top,
                    0xFF00E5FF.toInt(), 0xFFFFD700.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratchRect2, progBarH * 0.5f, progBarH * 0.5f, paint)
                paint.shader = null

                // Glowing Tip
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(leftMargin + fillW, progBarY + progBarH * 0.5f, 2f * dp, paint)

                // Progress Caption
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.textSize = 7.2f * dp
                textPaint.color = 0xCC8EC6FA.toInt()
                val completedLevelsText = t("LEVELS").uppercase()
                canvas.drawText("$progressInt / 50 $completedLevelsText", leftMargin, progBarY + progBarH + 9f * dp, textPaint)
            }
        } else {
            // No active run: Status Chip + Callout
            val statusW = (if (short) 94f else 114f) * dp
            capsuleRect.set(leftMargin, telemY, leftMargin + statusW, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x22FFFFFF
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f * dp
            paint.color = 0x55FFFFFF
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = (if (short) 7.5f else 9f) * dp
            textPaint.color = 0xEEFFFFFF.toInt()
            textPaint.letterSpacing = 0.04f
            canvas.drawText(fitText("○ $noActiveRunLabel", capsuleRect.width() - 8f * dp), capsuleRect.centerX(), capsuleRect.centerY() + 3.2f * dp, textPaint)

            val calloutX = capsuleRect.right + 8f * dp
            val calloutW = minOf(contentMaxW - statusW - 8f * dp, (if (short) 130f else 170f) * dp)
            if (calloutW > 50f * dp) {
                scratchRect.set(calloutX, telemY, calloutX + calloutW, telemY + chipH)
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(0xFF00E5FF.toInt(), 30)
                canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.9f * dp
                paint.color = withAlpha(0xFF00E5FF.toInt(), 140)
                canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

                textPaint.color = 0xFF00E5FF.toInt()
                canvas.drawText(fitText(startFreshLabel, calloutW - 10f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
            }
        }

        // 8. 3D Buttons: Emerald-Cyan Crystal with Royal Gold Specular
        drawActionButtons(
            canvas = canvas,
            continueButton = continueButton,
            newGameButton = newGameButton,
            startButton = startButton,
            continueLabel = continueLabel,
            newGameLabel = newGameLabel,
            startLabel = startLabel,
            activeRun = activeRun,
            accent = 0xFF00E5FF.toInt(),
            activeButtonId = activeButtonId,
            continueButtonId = continueButtonId,
            newGameButtonId = newGameButtonId,
            startButtonId = startButtonId,
            compact = compact,
            short = short,
            paint = paint,
            dp = dp,
            fitText = fitText
        )
    }

    /* =========================================================================
     * 2. CHAOS CARD: Monolithic Hazard Overdrive (Maximum Legibility & Clean AAA)
     * ========================================================================= */
    private fun drawChaosCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        description: String,
        modeBadge: String,
        accent: Int,
        active: Boolean,
        activeRun: Boolean,
        levelText: String,
        streakText: String,
        activeRunLabel: String,
        noActiveRunLabel: String,
        bestStreakLabel: String,
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
        mascotBitmap: Bitmap?,
        mascotTag: String,
        stateElapsed: Float,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawIcon: (Canvas, RectF) -> Unit,
        progressInt: Int,
        t: (String) -> String = { it }
    ) {
        val cornerRad = (if (short) 14f else 18f) * dp

        // 1. Soft Ambient Outer Bloom (Hot Cyber Magenta)
        paint.style = Paint.Style.STROKE
        paint.shader = null
        paint.strokeWidth = (if (active) 14f else 8f) * dp
        paint.color = withAlpha(0xFFFF007F.toInt(), if (active) 55 else 25)
        canvas.drawRoundRect(rect, cornerRad, cornerRad, paint)

        // 2. Monolithic Dark Tinted Polycarbonate Slab (96% solid toxic plum/obsidian)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            0xF618051E.toInt(), 0xFC07000B.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cornerRad, cornerRad, paint)
        paint.shader = null

        // 3. Subtle Tech Glitch Lines (Low opacity, adds AAA hazard feel)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x0AFFFFFF
        val scanStep = 18f * dp
        var sy = rect.top + scanStep
        while (sy < rect.bottom) {
            canvas.drawLine(rect.left + 8f * dp, sy, rect.right - 8f * dp, sy, paint)
            sy += scanStep
        }

        // 4. Razor-Sharp Perimeter Laser Border (Hot Magenta to Volt Yellow)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 2f * dp else 1.3f * dp
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.top,
            0xFFFF007F.toInt(), 0xFFFFE600.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cornerRad, cornerRad, paint)
        paint.shader = null

        // 5. Specular Top Glass Edge Highlight (Hot Magenta / Volt)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.shader = LinearGradient(
            rect.left + cornerRad, rect.top + 1f * dp,
            rect.right - cornerRad, rect.top + 1f * dp,
            intArrayOf(0x00FF007F, 0xDDFF007F.toInt(), 0xDDFFE600.toInt(), 0x00FFE600),
            floatArrayOf(0f, 0.35f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(
            rect.left + cornerRad + 8f * dp, rect.top + 1f * dp,
            rect.right - cornerRad - 8f * dp, rect.top + 1f * dp,
            paint
        )
        paint.shader = null

        // 6. Right Column: Real Hero Mascot (Gigi Glitch) on High-Voltage Energy Corona
        val mascotAreaW = minOf(rect.width() * 0.36f, rect.height() * 0.88f)
        val mascotCx = rect.right - mascotAreaW * 0.52f
        val mascotCy = rect.centerY()
        val jitter = (sin((stateElapsed * 28f).toDouble()).toFloat() * 1.2f) * dp
        val bob = sin((stateElapsed * 2.8f).toDouble()).toFloat() * (if (short) 2f else 3.5f) * dp
        val mx = mascotCx + jitter
        val my = mascotCy + bob

        drawHeroMascotChaos(
            canvas = canvas,
            cx = mx,
            cy = my,
            areaW = mascotAreaW,
            mascotBitmap = mascotBitmap,
            tag = if (mascotTag.isNotBlank()) mascotTag else "⚡ GIGI GLITCH",
            stateElapsed = stateElapsed,
            paint = paint,
            dp = dp,
            short = short,
            drawIcon = drawIcon
        )

        // 7. Left Column: Open Spacious Control Bay (High Contrast & Legibility)
        val leftMargin = rect.left + (if (short) 16f else if (compact) 22f else 28f) * dp
        val bayRight = mascotCx - mascotAreaW * 0.46f - (if (short) 8f else 14f) * dp
        val contentMaxW = maxOf(100f * dp, bayRight - leftMargin)

        // A. Mode Pill Tag
        val tagTop = rect.top + (if (short) 14f else if (compact) 18f else 22f) * dp
        val tagH = (if (short) 16f else 19f) * dp
        val tagW = (if (short) 124f else 142f) * dp
        capsuleRect.set(leftMargin, tagTop, leftMargin + tagW, tagTop + tagH)

        paint.style = Paint.Style.FILL
        paint.color = 0x28FF007F.toInt()
        canvas.drawRoundRect(capsuleRect, tagH * 0.5f, tagH * 0.5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0xCCFFE600.toInt()
        canvas.drawRoundRect(capsuleRect, tagH * 0.5f, tagH * 0.5f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(capsuleRect.left + 4f * dp, capsuleRect.top + 1f * dp, capsuleRect.right - 4f * dp, capsuleRect.top + 1f * dp, paint)

        // Strobe hazard LED
        val strobe = (sin((stateElapsed * 8f).toDouble()).toFloat() * 0.5f + 0.5f)
        val ledCx = capsuleRect.left + 9f * dp
        val ledCy = capsuleRect.centerY()
        paint.style = Paint.Style.FILL
        paint.color = if (strobe > 0.5f) 0xFFFFE600.toInt() else 0xFFFF007F.toInt()
        canvas.drawCircle(ledCx, ledCy, 3f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = (if (short) 7.5f else 8.5f) * dp
        textPaint.color = 0xFFFFE600.toInt()
        val tagText = "💀 ${t("WILD MODIFIERS").uppercase()} 💀"
        canvas.drawText(fitText(tagText, tagW - 18f * dp), ledCx + 6f * dp, capsuleRect.centerY() + 3.2f * dp, textPaint)

        // B. Large Bold Mode Title: "CHAOS" with Chromatic 3D Split
        val titleY = capsuleRect.bottom + (if (short) 20f else if (compact) 24f else 28f) * dp
        val titleText = fitText(title, contentMaxW)
        textPaint.textSize = (if (short) 20f else if (compact) 25f else 29f) * dp
        textPaint.letterSpacing = 0.06f

        // Chromatic split offset passes
        textPaint.color = 0x88FF007F.toInt()
        canvas.drawText(titleText, leftMargin + 2f * dp, titleY, textPaint)
        textPaint.color = 0x8800F0FF.toInt()
        canvas.drawText(titleText, leftMargin - 1.5f * dp, titleY, textPaint)

        // Crisp white foreground title
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.setShadowLayer(10f * dp, 0f, 2f * dp, 0xFF000000.toInt())
        canvas.drawText(titleText, leftMargin, titleY, textPaint)
        textPaint.clearShadowLayer()

        // C. Clean High-Contrast Subtitle / Lore
        val descY = titleY + (if (short) 14f else 17f) * dp
        textPaint.textSize = (if (short) 8.5f else if (compact) 10.5f else 11.5f) * dp
        textPaint.color = 0xFFFFD2EA.toInt()
        textPaint.letterSpacing = 0.03f
        canvas.drawText(fitText(description, contentMaxW), leftMargin, descY, textPaint)

        // D. Telemetry Chips Row
        val telemY = descY + (if (short) 8f else 11f) * dp
        val chipH = (if (short) 18f else 22f) * dp

        if (activeRun) {
            // Chip 1: HAOS ACTIV
            val statusW = (if (short) 82f else 96f) * dp
            capsuleRect.set(leftMargin, telemY, leftMargin + statusW, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x33FF007F.toInt()
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0xCCFF007F.toInt()
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(capsuleRect.left + 3f * dp, capsuleRect.top + 0.8f * dp, capsuleRect.right - 3f * dp, capsuleRect.top + 0.8f * dp, paint)

            val dotX = capsuleRect.left + 9f * dp
            val dotY = capsuleRect.centerY()
            paint.style = Paint.Style.FILL
            paint.color = if (strobe > 0.5f) 0xFFFFE600.toInt() else 0xFFFF007F.toInt()
            canvas.drawCircle(dotX, dotY, 2.5f * dp, paint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = (if (short) 7.5f else 9f) * dp
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.letterSpacing = 0.04f
            canvas.drawText(fitText(activeRunLabel, (capsuleRect.right - dotX - 11f * dp).coerceAtLeast(36f * dp)), dotX + 6f * dp, capsuleRect.centerY() + 3.2f * dp, textPaint)

            // Chip 2: 2X RECOMPENSE
            val chip1X = capsuleRect.right + 6f * dp
            val chip1W = (if (short) 84f else 102f) * dp
            scratchRect.set(chip1X, telemY, chip1X + chip1W, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x552A082E.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0xFFFFE600.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratchRect.left + 3f * dp, scratchRect.top + 0.8f * dp, scratchRect.right - 3f * dp, scratchRect.top + 0.8f * dp, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = (if (short) 7.5f else 9f) * dp
            textPaint.color = 0xFFFFE600.toInt()
            canvas.drawText(fitText("💀 2X ${t("HYPE").uppercase()}", scratchRect.width() - 8f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)

            // Chip 3: LEVEL
            val chip2X = scratchRect.right + 6f * dp
            val chip2W = (if (short) 60f else 72f) * dp
            scratchRect.set(chip2X, telemY, chip2X + chip2W, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x552A082E.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f * dp
            paint.color = 0xAAFF007F.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratchRect.left + 3f * dp, scratchRect.top + 0.8f * dp, scratchRect.right - 3f * dp, scratchRect.top + 0.8f * dp, paint)

            textPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawText(fitText("⚡ $levelText", scratchRect.width() - 8f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
        } else {
            // No active run: Status Chip + Danger Callout
            val statusW = (if (short) 94f else 114f) * dp
            capsuleRect.set(leftMargin, telemY, leftMargin + statusW, telemY + chipH)

            paint.style = Paint.Style.FILL
            paint.color = 0x22FFFFFF
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f * dp
            paint.color = 0x55FFFFFF
            canvas.drawRoundRect(capsuleRect, 5f * dp, 5f * dp, paint)
            paint.color = 0x44FFFFFF
            canvas.drawLine(capsuleRect.left + 3f * dp, capsuleRect.top + 0.8f * dp, capsuleRect.right - 3f * dp, capsuleRect.top + 0.8f * dp, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = (if (short) 7.5f else 9f) * dp
            textPaint.color = 0xEEFFFFFF.toInt()
            textPaint.letterSpacing = 0.04f
            canvas.drawText(fitText("○ $noActiveRunLabel", capsuleRect.width() - 8f * dp), capsuleRect.centerX(), capsuleRect.centerY() + 3.2f * dp, textPaint)

            val calloutX = capsuleRect.right + 8f * dp
            val calloutW = minOf(contentMaxW - statusW - 8f * dp, (if (short) 140f else 185f) * dp)
            if (calloutW > 50f * dp) {
                scratchRect.set(calloutX, telemY, calloutX + calloutW, telemY + chipH)
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(0xFFFF007F.toInt(), 35)
                canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.9f * dp
                paint.color = 0xFFFFE600.toInt()
                canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)

                textPaint.color = 0xFFFFE600.toInt()
                val calloutText = "💀 ${t("2X HYPE • HIGH RISK").uppercase()}"
                canvas.drawText(fitText(calloutText, calloutW - 10f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
            }
        }

        // 8. 3D Buttons: Hot Cyber Magenta with Volt Yellow Specular
        drawActionButtons(
            canvas = canvas,
            continueButton = continueButton,
            newGameButton = newGameButton,
            startButton = startButton,
            continueLabel = continueLabel,
            newGameLabel = newGameLabel,
            startLabel = startLabel,
            activeRun = activeRun,
            accent = 0xFFFF007F.toInt(),
            activeButtonId = activeButtonId,
            continueButtonId = continueButtonId,
            newGameButtonId = newGameButtonId,
            startButtonId = startButtonId,
            compact = compact,
            short = short,
            paint = paint,
            dp = dp,
            fitText = fitText
        )
    }

    /* =========================================================================
     * REAL HERO MASCOTS (Holographic Auras & Real Character Bitmaps)
     * ========================================================================= */
    private fun drawHeroMascotClassic(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        areaW: Float,
        mascotBitmap: Bitmap?,
        tag: String,
        stateElapsed: Float,
        paint: Paint,
        dp: Float,
        short: Boolean,
        drawIcon: (Canvas, RectF) -> Unit
    ) {
        val radius = areaW * 0.44f

        // 1. Soft Atmospheric Luminous Radial Glow
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy,
            radius * 1.35f,
            intArrayOf(0x6000E5FF.toInt(), 0x18FFD700.toInt(), 0x00000000),
            floatArrayOf(0f, 0.60f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius * 1.35f, paint)
        paint.shader = null

        // 2. Rotating Holographic Quantum Orbital Ring
        val rotationAngle = (stateElapsed * 24f) % 360f
        canvas.save()
        canvas.rotate(rotationAngle, cx, cy)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = 0x9900E5FF.toInt()
        scratchRect.set(cx - radius * 0.88f, cy - radius * 0.88f, cx + radius * 0.88f, cy + radius * 0.88f)
        canvas.drawArc(scratchRect, 15f, 60f, false, paint)
        canvas.drawArc(scratchRect, 105f, 60f, false, paint)
        canvas.drawArc(scratchRect, 195f, 60f, false, paint)
        canvas.drawArc(scratchRect, 285f, 60f, false, paint)

        // Orbital Gold Compass Ticks
        paint.color = 0xAAFFD700.toInt()
        paint.strokeWidth = 1.8f * dp
        for (i in 0 until 4) {
            val angleRad = Math.toRadians((i * 90.0))
            val x1 = cx + (cos(angleRad) * radius * 0.78f).toFloat()
            val y1 = cy + (sin(angleRad) * radius * 0.78f).toFloat()
            val x2 = cx + (cos(angleRad) * radius * 0.95f).toFloat()
            val y2 = cy + (sin(angleRad) * radius * 0.95f).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
        canvas.restore()

        // 3. Real Character Mascot (Nodlo)
        val charDiameter = areaW * 0.74f
        scratchRect.set(cx - charDiameter * 0.5f, cy - charDiameter * 0.5f, cx + charDiameter * 0.5f, cy + charDiameter * 0.5f)

        if (mascotBitmap != null) {
            mascotSrcRect.set(0, 0, mascotBitmap.width, mascotBitmap.height)
            paint.style = Paint.Style.FILL
            paint.alpha = 255
            paint.isFilterBitmap = true
            canvas.drawBitmap(mascotBitmap, mascotSrcRect, scratchRect, paint)
        } else {
            drawIcon(canvas, scratchRect)
        }

        // 4. Floating Royal Character Badge
        val badgeW = (if (short) 68f else 82f) * dp
        val badgeH = (if (short) 15f else 18f) * dp
        val badgeY = cy + radius * 0.80f
        capsuleRect.set(cx - badgeW * 0.5f, badgeY, cx + badgeW * 0.5f, badgeY + badgeH)

        paint.style = Paint.Style.FILL
        paint.color = 0xEE081626.toInt()
        canvas.drawRoundRect(capsuleRect, badgeH * 0.5f, badgeH * 0.5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0xFFFFD700.toInt()
        canvas.drawRoundRect(capsuleRect, badgeH * 0.5f, badgeH * 0.5f, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = (if (short) 7.5f else 9f) * dp
        textPaint.color = 0xFFFFE57F.toInt()
        textPaint.letterSpacing = 0.06f
        canvas.drawText(tag, capsuleRect.centerX(), capsuleRect.centerY() + 3.2f * dp, textPaint)
    }

    private fun drawHeroMascotChaos(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        areaW: Float,
        mascotBitmap: Bitmap?,
        tag: String,
        stateElapsed: Float,
        paint: Paint,
        dp: Float,
        short: Boolean,
        drawIcon: (Canvas, RectF) -> Unit
    ) {
        val radius = areaW * 0.44f

        // 1. High-Voltage Neon Corona
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy,
            radius * 1.35f,
            intArrayOf(0x60FF007F.toInt(), 0x20FFE600.toInt(), 0x00000000),
            floatArrayOf(0f, 0.60f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius * 1.35f, paint)
        paint.shader = null

        // 2. Rotating Electric Arc Ring
        val rotationAngle = (stateElapsed * 50f) % 360f
        canvas.save()
        canvas.rotate(rotationAngle, cx, cy)
        scratchRect.set(cx - radius * 0.88f, cy - radius * 0.88f, cx + radius * 0.88f, cy + radius * 0.88f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.color = 0xFFFFE600.toInt()
        canvas.drawArc(scratchRect, 0f, 50f, false, paint)
        canvas.drawArc(scratchRect, 120f, 50f, false, paint)
        canvas.drawArc(scratchRect, 240f, 50f, false, paint)

        paint.color = 0xFFFF007F.toInt()
        canvas.drawArc(scratchRect, 60f, 40f, false, paint)
        canvas.drawArc(scratchRect, 180f, 40f, false, paint)
        canvas.drawArc(scratchRect, 300f, 40f, false, paint)
        canvas.restore()

        // 3. Real Character Mascot (Gigi Glitch) with Chromatic Aberration RGB Displacement
        val charDiameter = areaW * 0.74f
        scratchRect.set(cx - charDiameter * 0.5f, cy - charDiameter * 0.5f, cx + charDiameter * 0.5f, cy + charDiameter * 0.5f)

        if (mascotBitmap != null) {
            mascotSrcRect.set(0, 0, mascotBitmap.width, mascotBitmap.height)
            paint.style = Paint.Style.FILL
            paint.isFilterBitmap = true

            // Magenta offset pass
            paint.alpha = 85
            scratchRect.offset(2.5f * dp, -1.5f * dp)
            canvas.drawBitmap(mascotBitmap, mascotSrcRect, scratchRect, paint)

            // Cyan offset pass
            scratchRect.offset(-5f * dp, 3f * dp)
            canvas.drawBitmap(mascotBitmap, mascotSrcRect, scratchRect, paint)

            // Crisp center pass
            scratchRect.offset(2.5f * dp, -1.5f * dp)
            paint.alpha = 255
            canvas.drawBitmap(mascotBitmap, mascotSrcRect, scratchRect, paint)
        } else {
            drawIcon(canvas, scratchRect)
        }

        // 4. Floating Character Badge
        val badgeW = (if (short) 68f else 82f) * dp
        val badgeH = (if (short) 15f else 18f) * dp
        val badgeY = cy + radius * 0.80f
        capsuleRect.set(cx - badgeW * 0.5f, badgeY, cx + badgeW * 0.5f, badgeY + badgeH)

        paint.style = Paint.Style.FILL
        paint.color = 0xEE220528.toInt()
        canvas.drawRoundRect(capsuleRect, badgeH * 0.5f, badgeH * 0.5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0xFFFFE600.toInt()
        canvas.drawRoundRect(capsuleRect, badgeH * 0.5f, badgeH * 0.5f, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = (if (short) 7.5f else 9f) * dp
        textPaint.color = 0xFFFFE600.toInt()
        textPaint.letterSpacing = 0.06f
        canvas.drawText(tag, capsuleRect.centerX(), capsuleRect.centerY() + 3.2f * dp, textPaint)
    }

    /* =========================================================================
     * ACTION BUTTONS (Tactile 3D Buttons inside Card)
     * ========================================================================= */
    private fun drawActionButtons(
        canvas: Canvas,
        continueButton: RectF,
        newGameButton: RectF,
        startButton: RectF,
        continueLabel: String,
        newGameLabel: String,
        startLabel: String,
        activeRun: Boolean,
        accent: Int,
        activeButtonId: Int,
        continueButtonId: Int,
        newGameButtonId: Int,
        startButtonId: Int,
        compact: Boolean,
        short: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        if (!continueButton.isEmpty && !newGameButton.isEmpty) {
            draw3DButton(
                canvas = canvas,
                rect = continueButton,
                label = "$continueLabel ►",
                accent = accent,
                isPrimary = true,
                active = activeButtonId == continueButtonId,
                compact = compact,
                short = short,
                paint = paint,
                dp = dp,
                fitText = fitText
            )
            draw3DButton(
                canvas = canvas,
                rect = newGameButton,
                label = newGameLabel,
                accent = accent,
                isPrimary = false,
                active = activeButtonId == newGameButtonId,
                compact = compact,
                short = short,
                paint = paint,
                dp = dp,
                fitText = fitText
            )
        } else if (!newGameButton.isEmpty) {
            draw3DButton(
                canvas = canvas,
                rect = newGameButton,
                label = "${if (activeRun) continueLabel else startLabel} ►",
                accent = accent,
                isPrimary = true,
                active = activeButtonId == newGameButtonId,
                compact = compact,
                short = short,
                paint = paint,
                dp = dp,
                fitText = fitText
            )
        } else if (!startButton.isEmpty) {
            draw3DButton(
                canvas = canvas,
                rect = startButton,
                label = "${if (activeRun) continueLabel else startLabel} ►",
                accent = accent,
                isPrimary = true,
                active = activeButtonId == startButtonId,
                compact = compact,
                short = short,
                paint = paint,
                dp = dp,
                fitText = fitText
            )
        }
    }

    /**
     * Renders a tactile, chunky 3D arcade button with physical depth, top gloss reflection,
     * and satisfying press translation.
     */
    fun draw3DButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        accent: Int,
        isPrimary: Boolean,
        active: Boolean,
        compact: Boolean,
        short: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        if (rect.isEmpty) return

        val cornerRad = (if (short) 10f else 14f) * dp
        val depth = (if (short) 4f else 5.5f) * dp
        val pressOffset = if (active) (depth * 0.75f) else 0f

        // 1. Bottom 3D Lip / Extrusion Base
        buttonLipRect.set(rect.left, rect.top + depth, rect.right, rect.bottom)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.alpha = 255

        val isMagenta = (accent == 0xFFFF007F.toInt()) || (accent == 0xFFFF2E93.toInt()) || (accent == 0xFFFF4D8D.toInt())
        val lipColor = if (isPrimary) {
            if (isMagenta) 0xFF6B0033.toInt() else 0xFF00584D.toInt()
        } else {
            0xFF06182E.toInt()
        }
        paint.color = lipColor
        canvas.drawRoundRect(buttonLipRect, cornerRad, cornerRad, paint)

        // 2. Main Button Face (Slightly translated down if active/pressed)
        buttonFaceRect.set(rect.left, rect.top + pressOffset, rect.right, rect.bottom - depth + pressOffset)

        if (isPrimary) {
            val topColor = if (isMagenta) 0xFFFF1493.toInt() else 0xFF00FFA3.toInt()
            val bottomColor = if (isMagenta) 0xFFBA0058.toInt() else 0xFF00A896.toInt()

            paint.alpha = 255
            paint.shader = LinearGradient(
                buttonFaceRect.left, buttonFaceRect.top,
                buttonFaceRect.left, buttonFaceRect.bottom,
                topColor, bottomColor,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(buttonFaceRect, cornerRad, cornerRad, paint)
            paint.shader = null

            // Glass Specular Curved Glare on Upper 48% (Juicy Candy Arcade Shine)
            capsuleRect.set(
                buttonFaceRect.left + 3f * dp,
                buttonFaceRect.top + 1.5f * dp,
                buttonFaceRect.right - 3f * dp,
                buttonFaceRect.top + buttonFaceRect.height() * 0.48f
            )
            paint.shader = LinearGradient(
                capsuleRect.left, capsuleRect.top,
                capsuleRect.left, capsuleRect.bottom,
                0x90FFFFFF.toInt(), 0x05FFFFFF,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
            paint.shader = null

            // Crisp Top Specular Edge Line
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f * dp
            paint.color = if (isMagenta) 0xFFFFF59D.toInt() else 0xFFE0F7FA.toInt()
            canvas.drawLine(
                buttonFaceRect.left + cornerRad,
                buttonFaceRect.top + 1.2f * dp,
                buttonFaceRect.right - cornerRad,
                buttonFaceRect.top + 1.2f * dp,
                paint
            )

            // Outer Bold 3D Edge Border
            paint.strokeWidth = if (active) 2.4f * dp else 1.6f * dp
            paint.color = if (active) 0xFFFFFFFF.toInt() else 0xEEFFFFFF.toInt()
            canvas.drawRoundRect(buttonFaceRect, cornerRad, cornerRad, paint)

            // Button Label with Deep 3D Shadow
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
            textPaint.textSize = (if (short) 10f else if (compact) 12f else 13.5f) * dp
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.letterSpacing = 0.08f
            textPaint.setShadowLayer(6f * dp, 0f, 2.5f * dp, 0xCC000000.toInt())
            canvas.drawText(
                fitText(label, buttonFaceRect.width() - 14f * dp),
                buttonFaceRect.centerX(),
                buttonFaceRect.centerY() + (textPaint.textSize * 0.36f),
                textPaint
            )
            textPaint.clearShadowLayer()
        } else {
            // Secondary Chunky Arcade Button (Vibrant Deep Cobalt Candy)
            paint.alpha = 255
            paint.shader = LinearGradient(
                buttonFaceRect.left, buttonFaceRect.top,
                buttonFaceRect.left, buttonFaceRect.bottom,
                0xFF143763.toInt(), 0xFF0B213D.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(buttonFaceRect, cornerRad, cornerRad, paint)
            paint.shader = null

            // Specular Glare on Upper 44%
            capsuleRect.set(
                buttonFaceRect.left + 3f * dp,
                buttonFaceRect.top + 1.5f * dp,
                buttonFaceRect.right - 3f * dp,
                buttonFaceRect.top + buttonFaceRect.height() * 0.44f
            )
            paint.shader = LinearGradient(
                capsuleRect.left, capsuleRect.top,
                capsuleRect.left, capsuleRect.bottom,
                0x55FFFFFF, 0x04FFFFFF,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
            paint.shader = null

            // Crisp Top Specular Edge Line
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = 0x88FFFFFF.toInt()
            canvas.drawLine(
                buttonFaceRect.left + cornerRad,
                buttonFaceRect.top + 1.2f * dp,
                buttonFaceRect.right - cornerRad,
                buttonFaceRect.top + 1.2f * dp,
                paint
            )

            // Chunky Cyan Border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = if (active) 2.2f * dp else 1.4f * dp
            paint.color = if (active) 0xFF00E5FF.toInt() else 0xCC00E5FF.toInt()
            canvas.drawRoundRect(buttonFaceRect, cornerRad, cornerRad, paint)

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
            textPaint.textSize = (if (short) 10f else if (compact) 12f else 13.5f) * dp
            textPaint.color = 0xFFE0F2FE.toInt()
            textPaint.letterSpacing = 0.08f
            textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0xAA000000.toInt())
            canvas.drawText(
                fitText(label, buttonFaceRect.width() - 14f * dp),
                buttonFaceRect.centerX(),
                buttonFaceRect.centerY() + (textPaint.textSize * 0.36f),
                textPaint
            )
            textPaint.clearShadowLayer()
        }
    }

    /**
     * Renders the bottom back button to navigate back to Home.
     */
    fun drawBackButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        short: Boolean,
        compact: Boolean,
        active: Boolean,
        paint: Paint,
        dp: Float
    ) {
        if (rect.isEmpty) return

        val cornerRad = (if (short) 10f else 14f) * dp
        val depth = (if (short) 4f else 5.5f) * dp
        val pressOffset = if (active) (depth * 0.75f) else 0f

        // Bottom 3D Lip
        buttonLipRect.set(rect.left, rect.top + depth, rect.right, rect.bottom)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.alpha = 255
        paint.color = 0xFF003844.toInt()
        canvas.drawRoundRect(buttonLipRect, cornerRad, cornerRad, paint)

        // Main Face
        buttonFaceRect.set(rect.left, rect.top + pressOffset, rect.right, rect.bottom - depth + pressOffset)
        paint.alpha = 255
        paint.shader = LinearGradient(
            buttonFaceRect.left, buttonFaceRect.top,
            buttonFaceRect.left, buttonFaceRect.bottom,
            if (active) 0xFF008399.toInt() else 0xFF006677.toInt(),
            if (active) 0xFF004D5A.toInt() else 0xFF003D47.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(buttonFaceRect, cornerRad, cornerRad, paint)
        paint.shader = null

        // Specular Glare on Upper 44%
        capsuleRect.set(
            buttonFaceRect.left + 3f * dp,
            buttonFaceRect.top + 1.2f * dp,
            buttonFaceRect.right - 3f * dp,
            buttonFaceRect.top + buttonFaceRect.height() * 0.44f
        )
        paint.shader = LinearGradient(
            capsuleRect.left, capsuleRect.top,
            capsuleRect.left, capsuleRect.bottom,
            0x66FFFFFF, 0x05FFFFFF,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
        paint.shader = null

        // Specular Top Edge Line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0x88FFFFFF.toInt()
        canvas.drawLine(
            buttonFaceRect.left + cornerRad,
            buttonFaceRect.top + 1f * dp,
            buttonFaceRect.right - cornerRad,
            buttonFaceRect.top + 1f * dp,
            paint
        )

        // Cyan Neon Border
        paint.strokeWidth = if (active) 2.4f * dp else 1.6f * dp
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawRoundRect(buttonFaceRect, cornerRad, cornerRad, paint)

        // Corner tick accents
        paint.strokeWidth = 2.4f * dp
        canvas.drawLine(buttonFaceRect.left + cornerRad, buttonFaceRect.top, buttonFaceRect.left + cornerRad + 10f * dp, buttonFaceRect.top, paint)
        canvas.drawLine(buttonFaceRect.right - cornerRad, buttonFaceRect.bottom, buttonFaceRect.right - cornerRad - 10f * dp, buttonFaceRect.bottom, paint)

        // Oxanium Label
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        var backTextSize = (if (short) 10f else if (compact) 12f else 13.5f) * dp
        textPaint.textSize = backTextSize
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.08f
        val fullBackText = "◄ $label"
        val maxBackWidth = (buttonFaceRect.width() - 24f * dp).coerceAtLeast(36f * dp)
        while (backTextSize > 8f * dp && textPaint.measureText(fullBackText) > maxBackWidth) {
            backTextSize -= 0.5f * dp
            textPaint.textSize = backTextSize
        }
        textPaint.setShadowLayer(8f * dp, 0f, 0f, 0x9900E5FF.toInt())
        canvas.drawText(
            fullBackText,
            buttonFaceRect.centerX(),
            buttonFaceRect.centerY() + (textPaint.textSize * 0.36f),
            textPaint
        )
        textPaint.clearShadowLayer()
    }
}
