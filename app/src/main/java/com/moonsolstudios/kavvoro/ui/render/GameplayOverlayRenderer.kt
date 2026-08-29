package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Procedural HUD and in-game overlay renderer (HUD controls dock, stats strip, energy bar, metric cards, toasts, bursts).
 */
object GameplayOverlayRenderer {

    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawHudControlsDock(
        canvas: Canvas,
        buttons: List<RectF>,
        accent: Int,
        paint: Paint,
        dp: Float
    ) {
        val validButtons = buttons.filter { !it.isEmpty }
        if (validButtons.isEmpty()) return
        val left = validButtons.minOf { it.left } - 4f * dp
        val top = validButtons.minOf { it.top } - 4f * dp
        val right = validButtons.maxOf { it.right } + 4f * dp
        val bottom = validButtons.maxOf { it.bottom } + 4f * dp
        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            bottom,
            intArrayOf(0x5E16202C, withAlpha(accent, 34), 0x35101622),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 9f * dp, 9f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(accent, 94)
        canvas.drawRoundRect(scratchRect, 9f * dp, 9f * dp, paint)
    }

    fun drawCompactHudStats(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        timeFormatted: String,
        timeColor: Int,
        chainValue: String,
        hypeValue: String,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val height = 30f * dp
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(0x5A18202C, 0x2618202C),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.7f * dp
        paint.color = 0x2EFFFFFF
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        val splitOne = left + width * 0.34f
        val splitTwo = left + width * 0.62f
        paint.style = Paint.Style.FILL
        paint.color = 0x18FFFFFF
        canvas.drawRect(splitOne, top + 5f * dp, splitOne + 1f * dp, top + height - 5f * dp, paint)
        canvas.drawRect(splitTwo, top + 5f * dp, splitTwo + 1f * dp, top + height - 5f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 6.6f * dp
        textPaint.color = 0x82FFFFFF.toInt()
        val firstTextX = if (isRtl) splitOne - 7f * dp else left + 7f * dp
        val secondTextX = if (isRtl) splitTwo - 7f * dp else splitOne + 7f * dp
        val thirdTextX = if (isRtl) left + width - 7f * dp else splitTwo + 7f * dp
        canvas.drawText(t("TIME").uppercase(), firstTextX, top + 9f * dp, textPaint)
        canvas.drawText(t("CHAIN").uppercase(), secondTextX, top + 9f * dp, textPaint)
        canvas.drawText(t("HYPE").uppercase(), thirdTextX, top + 9f * dp, textPaint)
        textPaint.textSize = 10.6f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(timeFormatted, splitOne - left - 12f * dp), firstTextX, top + 23f * dp, textPaint)
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(fitText(chainValue, splitTwo - splitOne - 12f * dp), secondTextX, top + 23f * dp, textPaint)
        textPaint.color = 0xFFFFD75C.toInt()
        canvas.drawText(fitText(hypeValue, left + width - splitTwo - 12f * dp), thirdTextX, top + 23f * dp, textPaint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(timeColor, 185)
        canvas.drawCircle(if (isRtl) left + 7f * dp else left + width - 7f * dp, top + 8f * dp, 2.1f * dp, paint)
    }

    fun drawRiftEnergyBar(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        riftEnergy: Float,
        accent: Int,
        showLabel: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String
    ) {
        val danger = riftEnergy < 0.22f
        val effectiveAccent = if (danger) 0xFFFF5757.toInt() else accent
        val height = 13f * dp
        val radius = 5.5f * dp
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0x69141B27
        canvas.drawRoundRect(scratchRect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = if (danger) withAlpha(effectiveAccent, 210) else 0x55FFFFFF
        canvas.drawRoundRect(scratchRect, radius, radius, paint)

        val fillWidth = (width * riftEnergy.coerceIn(0f, 1f)).coerceAtLeast(if (riftEnergy > 0f) 5f * dp else 0f)
        if (fillWidth > 0f) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                left,
                top,
                left + width,
                top,
                intArrayOf(withAlpha(effectiveAccent, 250), withAlpha(0xFFFFCF4A.toInt(), if (danger) 130 else 210)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            scratchRect2.set(left, top, left + fillWidth, top + height)
            canvas.drawRoundRect(scratchRect2, radius, radius, paint)
            paint.shader = null
        }

        val segments = 12
        val gap = 2.2f * dp
        val segmentWidth = (width - gap * (segments - 1)) / segments
        repeat(segments) { index ->
            val x = left + index * (segmentWidth + gap)
            scratchRect2.set(x, top + 2f * dp, x + segmentWidth, top + height - 2f * dp)
            paint.style = Paint.Style.FILL
            paint.color = if (riftEnergy * segments > index) 0x1FFFFFFF else 0x22000000
            canvas.drawRoundRect(scratchRect2, 2f * dp, 2f * dp, paint)
        }

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 7.4f * dp
        if (showLabel) {
            textPaint.color = 0xBBFFFFFF.toInt()
            canvas.drawText(t("RIFT ENERGY").uppercase(), left + 7f * dp, top - 3f * dp, textPaint)
        }
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = withAlpha(effectiveAccent, 245)
        canvas.drawText("${(riftEnergy * 100).roundToInt()}%", left + width - 7f * dp, top - 3f * dp, textPaint)
    }

    fun drawPowerToast(
        canvas: Canvas,
        powerMessage: String,
        skin: BallSkin,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        powerIconKey: (BallPower) -> String,
        ballPowerName: (BallPower) -> String
    ) {
        val accent = skin.lineColor
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xF0080C13.toInt()
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.color = accent
        canvas.drawRect(left, top, left + 4f * dp, top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        val powerIconSize = 34f * dp
        scratchRect.set(left + 10f * dp, top + 7f * dp, left + 10f * dp + powerIconSize, top + 7f * dp + powerIconSize)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratchRect, 255)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 8f * dp
        textPaint.color = withAlpha(accent, 235)
        val power = skin.power
        val title = if (power != BallPower.NONE && powerMessage.startsWith(ballPowerName(power))) {
            t("SUPERPOWER ONLINE").uppercase()
        } else {
            t("SUPERPOWER TRIGGERED").uppercase()
        }
        canvas.drawText(title, left + 52f * dp, top + 17f * dp, textPaint)
        textPaint.textSize = 12f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(powerMessage, width - 68f * dp), left + 52f * dp, top + 36f * dp, textPaint)
    }

    fun drawFinishBurst(
        canvas: Canvas,
        won: Boolean,
        progress: Float,
        cx: Float,
        cy: Float,
        accent: Int,
        paint: Paint,
        dp: Float
    ) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val count = if (won) 28 else 14
        repeat(count) { i ->
            val angle = i * PI.toFloat() * 2f / count
            val base = (if (won) 42f else 26f) * dp
            val spread = (if (won) 126f else 76f) * dp * progress
            val inner = base + spread * 0.28f
            val outer = base + spread
            paint.strokeWidth = (if (won) 3.2f else 2.2f) * dp
            paint.color = withAlpha(
                if (won) if (i % 3 == 0) accent else 0xFFFFCF4A.toInt() else 0xFFFF4D8D.toInt(),
                ((1f - progress) * 170f).roundToInt()
            )
            canvas.drawLine(
                cx + cos(angle) * inner,
                cy + sin(angle) * inner,
                cx + cos(angle) * outer,
                cy + sin(angle) * outer,
                paint
            )
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawFinishConfetti(
        canvas: Canvas,
        won: Boolean,
        cx: Float,
        cy: Float,
        stateElapsed: Float,
        accent: Int,
        skinLineColor: Int,
        seed: Long,
        finishPulse: Float,
        paint: Paint,
        dp: Float
    ) {
        val alpha = ((finishPulse * if (won) 210f else 120f).roundToInt()).coerceIn(0, 220)
        if (alpha <= 0) return
        val count = if (won) 36 else 14
        val colors = intArrayOf(accent, skinLineColor, 0xFFFFCF4A.toInt(), 0xFFF7F4FF.toInt())
        paint.style = Paint.Style.FILL
        repeat(count) { i ->
            val angle = i * PI.toFloat() * 2f / count + (seed % 19L).toFloat() * 0.017f
            val drift = stateElapsed.coerceAtMost(2.2f)
            val distance = (if (won) 54f else 32f) * dp + (42f + (i % 6) * 13f) * dp * drift
            val x = cx + cos(angle) * distance + sin(stateElapsed * 2.4f + i) * 10f * dp
            val y = cy + sin(angle) * distance + drift * drift * (if (won) 36f else 18f) * dp
            val width = (if (won) 9f else 6f) * dp
            val height = (if (won) 3.8f else 3f) * dp
            canvas.save()
            canvas.rotate((angle * 180f / PI.toFloat()) + stateElapsed * 150f, x, y)
            scratchRect.set(x - width, y - height, x + width, y + height)
            paint.color = withAlpha(colors[i % colors.size], alpha)
            canvas.drawRoundRect(scratchRect, 2f * dp, 2f * dp, paint)
            canvas.restore()
        }
    }

    fun drawHudMetric(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        label: String,
        value: String,
        accent: Int,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0x5018202C
        canvas.drawRoundRect(left, top, left + width, top + 28f * dp, 5f * dp, 5f * dp, paint)
        paint.color = withAlpha(accent, 210)
        if (isRtl) {
            canvas.drawRect(left + width - 2f * dp, top, left + width, top + 28f * dp, paint)
        } else {
            canvas.drawRect(left, top, left + 2f * dp, top + 28f * dp, paint)
        }
        val iconKey = when (label) {
            "TIME" -> "boost_recharge"
            "CHAIN" -> "boost_chain"
            else -> "boost_prism"
        }
        if (isRtl) {
            scratchRect.set(left + 4f * dp, top + 3f * dp, left + 18f * dp, top + 17f * dp)
        } else {
            scratchRect.set(left + width - 18f * dp, top + 3f * dp, left + width - 4f * dp, top + 17f * dp)
        }
        drawWorldAsset(canvas, iconKey, scratchRect, 155)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 7.5f * dp
        textPaint.color = 0x77FFFFFF
        val textX = if (isRtl) left + width - 23f * dp else left + 7f * dp
        canvas.drawText(label, textX, top + 10f * dp, textPaint)
        textPaint.textSize = 11f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, width - 30f * dp), textX, top + 23f * dp, textPaint)
    }

    fun drawCurseRibbon(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        power: BallPower,
        skinLineColor: Int,
        curses: List<com.moonsolstudios.kavvoro.engine.CurseSpec>,
        isCompactHud: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        ballPowerName: (BallPower) -> String,
        powerIconKey: (BallPower) -> String,
        drawWorldAsset: (Canvas, String, RectF) -> Unit
    ) {
        var x = left
        val gap = 6f * dp
        val maxRight = left + width
        val chips = mutableListOf<Triple<String, Int, Boolean>>()
        if (power != BallPower.NONE) chips += Triple(ballPowerName(power), skinLineColor, true)
        chips += curses.map {
            Triple(t(com.moonsolstudios.kavvoro.i18n.TutorialCopy.curseRibbonKey(it.type)).uppercase(), it.accent, false)
        }
        if (chips.isEmpty()) return
        val visible = chips.take(2)
        visible.forEach { (label, accent, powered) ->
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textSize = 9f * dp
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            val shownLabel = if (powered && isCompactHud) label else if (powered) "${t("POWER").uppercase()} $label" else label
            val iconReserve = if (powered) 22f * dp else 0f
            val chipWidth = (textPaint.measureText(shownLabel) + 18f * dp + iconReserve).coerceIn(
                (if (powered) 88f else 68f) * dp,
                (if (isCompactHud) 132f else 156f) * dp
            )
            if (x + chipWidth > maxRight) return@forEach
            scratchRect.set(x, top, x + chipWidth, top + 22f * dp)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (powered) 68 else 42)
            canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = (if (powered) 1.5f else 1f) * dp
            paint.color = withAlpha(accent, 190)
            canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
            if (powered) {
                val iconSize = 17f * dp
                scratchRect2.set(
                    scratchRect.left + 4f * dp,
                    scratchRect.centerY() - iconSize * 0.5f,
                    scratchRect.left + 4f * dp + iconSize,
                    scratchRect.centerY() + iconSize * 0.5f
                )
                drawWorldAsset(canvas, powerIconKey(power), scratchRect2)
            }
            textPaint.color = 0xDDF7F4FF.toInt()
            if (powered) {
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(fitText(shownLabel, scratchRect.width() - 30f * dp), scratchRect.left + 25f * dp, scratchRect.centerY() + 3.5f * dp, textPaint)
            } else {
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(fitText(shownLabel, scratchRect.width() - 10f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.5f * dp, textPaint)
            }
            x += chipWidth + gap
        }

        val hidden = chips.size - visible.size
        if (hidden > 0 && x + 38f * dp <= maxRight) {
            scratchRect.set(x, top, x + 38f * dp, top + 22f * dp)
            paint.style = Paint.Style.FILL
            paint.color = 0x33454F65
            canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 9f * dp
            textPaint.color = 0xCCD4E0F0.toInt()
            canvas.drawText("+$hidden", scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
        }
    }

    fun drawLevelNameGlass(
        canvas: Canvas,
        stageLeft: Float,
        stageRight: Float,
        viewWidth: Float,
        safeTop48: Float,
        compactHud: Boolean,
        energyTop: Float,
        gameplayHudBottom: Float,
        accent: Int,
        titleText: String,
        paint: Paint,
        dp: Float,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit
    ) {
        val width = (stageRight - stageLeft - 36f * dp).coerceIn(188f * dp, viewWidth - 44f * dp)
        val left = ((stageLeft + stageRight) * 0.5f - width * 0.5f).coerceIn(14f * dp, viewWidth - width - 14f * dp)
        val height = (if (compactHud) 22f else 28f) * dp
        val titleBottomInset = (if (compactHud) 7f else 9f) * dp
        val hudBounds = com.moonsolstudios.kavvoro.ui.TutorialCardLayout.hudVerticalBounds(
            energyTop = energyTop,
            energyHeight = 13f * dp,
            titleHeight = height,
            titleBottomInset = titleBottomInset,
            hudBottom = gameplayHudBottom,
            minimumGap = 12f * dp
        )
        val top = hudBounds.titleTop.coerceAtLeast(safeTop48)
        scratchRect.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(0xD6070B12.toInt(), withAlpha(accent, 52), 0xB0070B12.toInt()),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(left, top, left + 3f * dp, top + height, 2f * dp, 2f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(accent, 145)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = (if (compactHud) 9.3f else 10.8f) * dp
        textPaint.color = 0xEAF7F4FF.toInt()
        drawFittedText(
            canvas,
            titleText,
            scratchRect.centerX(),
            scratchRect.centerY() + (if (compactHud) 3.3f else 3.8f) * dp,
            width - 24f * dp,
            if (compactHud) 9.3f else 10.8f,
            if (compactHud) 7.2f else 8.2f
        )
    }

    fun drawHud(
        canvas: Canvas,
        compactHud: Boolean,
        isRtl: Boolean,
        top: Float,
        left: Float,
        controlsLeft: Float,
        hasRibbon: Boolean,
        toolbarBottom: Float,
        viewWidth: Float,
        levelAccent: Int,
        levelDifficultyRating: Int,
        levelIndex: Int,
        gameModeTitle: String,
        riftEnergy: Float,
        timeRemaining: Float,
        hudHype: Int,
        chainCount: Int,
        energyTop: Float,
        energyWidth: Float,
        power: BallPower,
        skinLineColor: Int,
        curses: List<com.moonsolstudios.kavvoro.engine.CurseSpec>,
        dockButtons: List<RectF>,
        musicButton: RectF,
        sfxButton: RectF,
        homeButton: RectF,
        restartButton: RectF,
        shareButton: RectF,
        nextButton: RectF,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        formatHypeAmount: (Int) -> String,
        formatTimeRemaining: (Float) -> String,
        drawIconButton: (Canvas, RectF, Int) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        ballPowerName: (BallPower) -> String,
        powerIconKey: (BallPower) -> String
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0xEA070B12.toInt()
        canvas.drawRect(0f, 0f, viewWidth, toolbarBottom, paint)
        paint.shader = LinearGradient(
            0f,
            0f,
            viewWidth,
            0f,
            intArrayOf(withAlpha(levelAccent, 34), 0x00070B12),
            floatArrayOf(0f, 0.72f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, viewWidth, toolbarBottom, paint)
        paint.shader = null
        paint.color = withAlpha(levelAccent, 150)
        canvas.drawRect(0f, toolbarBottom - 2f * dp, viewWidth * riftEnergy, toolbarBottom, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.shader = null
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        val metaWidth = (controlsLeft - left - 10f * dp).coerceAtLeast(72f * dp)
        val metaX = if (isRtl) controlsLeft - 10f * dp else left
        if (!compactHud) {
            textPaint.textSize = 9f * dp
            textPaint.color = withAlpha(levelAccent, 235)
            val meta = "$gameModeTitle  /  D$levelDifficultyRating"
            canvas.drawText(fitText(meta, metaWidth), metaX, top + 8f * dp, textPaint)

            textPaint.color = 0xFFF7F4FF.toInt()
            textPaint.textSize = 20f * dp
            val compactTitle = "L${levelIndex.toString().padStart(2, '0')}"
            canvas.drawText(compactTitle, metaX, top + 32f * dp, textPaint)
        }

        drawHudControlsDock(canvas, dockButtons, levelAccent, paint, dp)
        if (compactHud) {
            val statsLeft = left
            val statsWidth = (controlsLeft - statsLeft - 8f * dp).coerceAtLeast(82f * dp)
            val timeFormatted = formatTimeRemaining(timeRemaining)
            val timeColor = if (timeRemaining < 3f) 0xFFFF5757.toInt() else levelAccent
            drawCompactHudStats(
                canvas = canvas,
                left = statsLeft,
                top = top + 2f * dp,
                width = statsWidth,
                timeFormatted = timeFormatted,
                timeColor = timeColor,
                chainValue = if (chainCount > 0) "x$chainCount" else "-",
                hypeValue = formatHypeAmount(hudHype),
                isRtl = isRtl,
                paint = paint,
                dp = dp,
                t = t,
                fitText = fitText
            )
        } else {
            val timeFormatted = formatTimeRemaining(timeRemaining)
            val timeColor = if (timeRemaining < 3f) 0xFFFF5757.toInt() else levelAccent
            drawHudMetric(canvas, left, top + 39f * dp, 66f * dp, t("TIME").uppercase(), timeFormatted, timeColor, isRtl, paint, dp, fitText, drawWorldAsset)
            drawHudMetric(canvas, left + 72f * dp, top + 39f * dp, 67f * dp, t("CHAIN").uppercase(), if (chainCount > 0) "x$chainCount" else "-", 0xFFFFCF4A.toInt(), isRtl, paint, dp, fitText, drawWorldAsset)
            drawHudMetric(canvas, left + 145f * dp, top + 39f * dp, 72f * dp, t("HYPE").uppercase(), formatHypeAmount(hudHype), 0xFFFFCF4A.toInt(), isRtl, paint, dp, fitText, drawWorldAsset)
        }
        if (!compactHud && hasRibbon) {
            drawCurseRibbon(canvas, left, top + 68f * dp, (controlsLeft - left - 8f * dp).coerceAtLeast(120f * dp), power, skinLineColor, curses, compactHud, paint, dp, t, fitText, ballPowerName, powerIconKey, { c, k, r -> drawWorldAsset(c, k, r, 255) })
        }
        if (compactHud && hasRibbon) {
            drawCurseRibbon(canvas, left, top + 40f * dp, (controlsLeft - left - 8f * dp).coerceAtLeast(120f * dp), power, skinLineColor, curses, compactHud, paint, dp, t, fitText, ballPowerName, powerIconKey, { c, k, r -> drawWorldAsset(c, k, r, 255) })
        }

        drawRiftEnergyBar(canvas, (viewWidth - energyWidth) * 0.5f, energyTop, energyWidth, riftEnergy, levelAccent, !compactHud || !hasRibbon, paint, dp, t)
        drawIconButton(canvas, musicButton, 1) // MUSIC
        drawIconButton(canvas, sfxButton, 2) // SFX
        drawIconButton(canvas, homeButton, 4) // HOME
        drawIconButton(canvas, restartButton, 3) // RESTART
        if (!shareButton.isEmpty) {
            drawIconButton(canvas, shareButton, 5) // SHARE
        }
        if (!nextButton.isEmpty) {
            drawIconButton(canvas, nextButton, 6) // NEXT
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}

