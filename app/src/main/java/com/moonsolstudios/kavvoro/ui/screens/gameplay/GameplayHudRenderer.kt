package com.moonsolstudios.kavvoro.ui.screens.gameplay

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.CurseSpec
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.GameplayScoreCalculator
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.i18n.TutorialCopy
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.LayoutRect
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class ModeWarning(
    val title: String,
    val subtitle: String,
    val accent: Int
)

data class LevelArchetype(
    val label: String,
    val detail: String,
    val accent: Int,
    val iconKey: String
)

/**
 * Dedicated renderer for in-game HUD, metric cards, notifications, and finish burst/confetti.
 */
object GameplayHudRenderer {

    private val scratch = RectF()
    private val scratch2 = RectF()

    fun isCompactHud(viewWidth: Float, dp: Float): Boolean = viewWidth < dp * 520f

    fun hudHasRibbon(power: BallPower, hasCurses: Boolean): Boolean =
        power != BallPower.NONE || hasCurses

    fun gameplayHudBottom(isCompact: Boolean, hasRibbon: Boolean, dp: Float): Float =
        dp * (if (isCompact) {
            if (hasRibbon) 132f else 104f
        } else {
            156f
        })

    fun gameplayOverlayTop(isCompact: Boolean, hasRibbon: Boolean, dp: Float): Float =
        gameplayHudBottom(isCompact, hasRibbon, dp) + dp * 8f

    private val WARNING_PORTAL_RIFT = ModeWarning("PORTAL RIFT!", "ENTER IN / EXIT OUT WITH EXTRA SPEED", 0xFF45F2FF.toInt())
    private val WARNING_WIND_OVERHEAT = ModeWarning("WIND + OVERHEAT!", "TAP AGAINST WIND / ENERGY DRAINS FAST", 0xFF8AA6FF.toInt())
    private val WARNING_FOCUS_HEAVY = ModeWarning("FOCUS HEAVY!", "TAP TO SLOW / GRAVITY IS HEAVY", 0xFFFFCF4A.toInt())
    private val WARNING_POWER_MOON = ModeWarning("POWER MOON!", "POWER TAP / GLIDE AFTER BURST", 0xFF45F2FF.toInt())
    private val WARNING_WIND_GUARD = ModeWarning("WIND GUARD!", "TAP AGAINST THE GUST", 0xFF8AA6FF.toInt())
    private val WARNING_OVERHEAT = ModeWarning("OVERHEAT!", "POWER RISES / ENERGY MELTS FAST", 0xFFFF5757.toInt())
    private val WARNING_RIFT_DRAIN = ModeWarning("RIFT DRAIN!", "USE SHORT CONTROL BURSTS", 0xFF64E572.toInt())
    private val WARNING_PULSE_GUARD = ModeWarning("PULSE GUARD!", "TAP TO DAMPEN PULSE FORCE", 0xFFC15CFF.toInt())
    private val WARNING_FOCUS_FIELD = ModeWarning("FOCUS FIELD!", "TAP TO SLOW FOR PRECISION", 0xFFFFCF4A.toInt())
    private val WARNING_POWER_TAP = ModeWarning("POWER TAP!", "RAPID TAPS BUILD FORCE", 0xFFFF4D8D.toInt())
    private val WARNING_HEAVY_CORE = ModeWarning("HEAVY CORE!", "GRAVITY PULLS HARDER", 0xFFFF8C42.toInt())
    private val WARNING_MOON_GLIDE = ModeWarning("MOON GLIDE!", "RELEASE KEEPS MOMENTUM", 0xFF45F2FF.toInt())
    private val WARNING_TINY_GATE = ModeWarning("TINY GATE!", "THE EXIT WINDOW IS SMALLER", 0xFFF7F4FF.toInt())

    private val ARCHETYPE_PORTAL_SLING = LevelArchetype("PORTAL SLING", "Teleport timing and launch control", 0xFF45F2FF.toInt(), "portal_goal")
    private val ARCHETYPE_WIND_TUNNEL = LevelArchetype("WIND TUNNEL", "Short bursts beat the gust", 0xFF8AA6FF.toInt(), "boost_recharge")
    private val ARCHETYPE_ENERGY_TAX = LevelArchetype("ENERGY TAX", "Spend Rift in tiny snaps", 0xFFFF5757.toInt(), "danger_beacon")
    private val ARCHETYPE_CONTROL_LAB = LevelArchetype("CONTROL LAB", "Tap timing changes the pull", 0xFFFFCF4A.toInt(), "boost_plasma")
    private val ARCHETYPE_PULSE_MAZE = LevelArchetype("PULSE MAZE", "Fields bend speed and direction", 0xFFC15CFF.toInt(), "boost_pulse")
    private val ARCHETYPE_MOVING_DANGER = LevelArchetype("MOVING DANGER", "Read the lanes before committing", 0xFFFF4D8D.toInt(), "hazard_glitch")
    private val ARCHETYPE_GATE_STACK = LevelArchetype("GATE STACK", "Bounce angles matter", 0xFF1DE8C8.toInt(), "platform_classic")
    private val ARCHETYPE_CHAOS_TOUCH = LevelArchetype("CHAOS TOUCH", "Fast reactions, no sleepy holds", 0xFFFF4D8D.toInt(), "boost_chain")
    private val ARCHETYPE_RIFT_PATH = LevelArchetype("RIFT PATH", "Clean control and smooth release", 0xFF1DE8C8.toInt(), "boost_rift_pull")

    fun hudControlsLeft(buttons: List<RectF>, defaultLeft: Float): Float {
        var minLeft = Float.POSITIVE_INFINITY
        for (i in 0 until buttons.size) {
            val b = buttons[i]
            if (b.left < b.right && b.top < b.bottom && b.left < minLeft) {
                minLeft = b.left
            }
        }
        return if (minLeft.isFinite()) minLeft else defaultLeft
    }

    fun hudControlsLeft(b1: RectF, b2: RectF, b3: RectF, b4: RectF, defaultLeft: Float): Float {
        var minLeft = Float.POSITIVE_INFINITY
        if (b1.left < b1.right && b1.top < b1.bottom && b1.left < minLeft) minLeft = b1.left
        if (b2.left < b2.right && b2.top < b2.bottom && b2.left < minLeft) minLeft = b2.left
        if (b3.left < b3.right && b3.top < b3.bottom && b3.left < minLeft) minLeft = b3.left
        if (b4.left < b4.right && b4.top < b4.bottom && b4.left < minLeft) minLeft = b4.left
        return if (minLeft.isFinite()) minLeft else defaultLeft
    }

    fun hudControlsLeftBounds(buttons: List<LayoutRect>, defaultLeft: Float): Float =
        buttons.filter { it.left < it.right && it.top < it.bottom }.minOfOrNull { it.left } ?: defaultLeft

    fun currentModeWarning(level: LevelSpec, hasCurse: (CurseType) -> Boolean = level::hasCurse): ModeWarning? {
        return when {
            level.portals.isNotEmpty() -> WARNING_PORTAL_RIFT
            hasCurse(CurseType.RIFT_WIND) && hasCurse(CurseType.OVERHEAT) -> WARNING_WIND_OVERHEAT
            hasCurse(CurseType.FOCUS_FIELD) && hasCurse(CurseType.HEAVY_CORE) -> WARNING_FOCUS_HEAVY
            hasCurse(CurseType.POWER_HOLD) && hasCurse(CurseType.MOON_GLIDE) -> WARNING_POWER_MOON
            hasCurse(CurseType.RIFT_WIND) -> WARNING_WIND_GUARD
            hasCurse(CurseType.OVERHEAT) -> WARNING_OVERHEAT
            hasCurse(CurseType.RIFT_DRAIN) -> WARNING_RIFT_DRAIN
            hasCurse(CurseType.PULSE_STORM) -> WARNING_PULSE_GUARD
            hasCurse(CurseType.FOCUS_FIELD) -> WARNING_FOCUS_FIELD
            hasCurse(CurseType.POWER_HOLD) -> WARNING_POWER_TAP
            hasCurse(CurseType.HEAVY_CORE) -> WARNING_HEAVY_CORE
            hasCurse(CurseType.MOON_GLIDE) -> WARNING_MOON_GLIDE
            hasCurse(CurseType.TINY_GATE) -> WARNING_TINY_GATE
            else -> null
        }
    }

    fun drawHud(
        canvas: Canvas,
        compactHud: Boolean,
        hasRibbon: Boolean,
        controlsLeft: Float,
        toolbarBottom: Float,
        levelAccent: Int,
        levelDifficultyRating: Int,
        levelIndex: Int,
        levelTimeLimitSeconds: Float,
        simElapsed: Float,
        stateElapsed: Float,
        gameMode: GameMode,
        gameState: GameState,
        riftEnergy: Float,
        lastHypeScore: Int,
        streak: Int,
        maxChain: Int,
        chainCount: Int,
        performanceLite: Boolean,
        selectedBallSkin: BallSkin,
        levelCurses: List<CurseSpec>,
        viewWidth: Float,
        dp: Float,
        musicButton: RectF,
        sfxButton: RectF,
        homeButton: RectF,
        restartButton: RectF,
        shareButton: RectF,
        nextButton: RectF,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        formatHypeAmount: (Int) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawIconButton: (Canvas, RectF, ButtonId) -> Unit
    ) {
        val top = dp * (if (compactHud) 8f else 12f)
        val left = dp * (if (compactHud) 12f else 16f)

        paint.style = Paint.Style.FILL
        paint.color = 0xEA070B12.toInt()
        canvas.drawRect(0f, 0f, viewWidth, toolbarBottom, paint)
        paint.shader = LinearGradient(
            0f, 0f, viewWidth, 0f,
            intArrayOf(withAlpha(levelAccent, 34), 0x00070B12),
            floatArrayOf(0f, 0.72f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, viewWidth, toolbarBottom, paint)
        paint.shader = null
        paint.color = withAlpha(levelAccent, 150)
        canvas.drawRect(0f, toolbarBottom - dp * 2f, viewWidth * riftEnergy, toolbarBottom, paint)

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        val metaWidth = (controlsLeft - left - dp * 10f).coerceAtLeast(dp * 72f)
        if (!compactHud) {
            textPaint.textSize = dp * 9f
            val perfBadge = if (performanceLite) "  •  ⚡ ECO" else "  •  ✦ HQ"
            val meta = "${gameMode.menuTitle(t)}  /  D$levelDifficultyRating$perfBadge"
            textPaint.color = if (performanceLite) 0xFF64E572.toInt() else withAlpha(levelAccent, 235)
            canvas.drawText(fitText(meta, metaWidth), left, top + dp * 8f, textPaint)

            textPaint.color = 0xFFF7F4FF.toInt()
            textPaint.textSize = dp * 20f
            val compactTitle = "L${levelIndex.toString().padStart(2, '0')}"
            canvas.drawText(compactTitle, left, top + dp * 32f, textPaint)
        }

        val timeRemaining = (levelTimeLimitSeconds - simElapsed).coerceAtLeast(0f)
        val timeUrgent = timeRemaining in 0.01f..3.0f
        val timeColor = if (timeUrgent) {
            if (sin(stateElapsed * 15f) > 0f) 0xFFFF3333.toInt() else 0xFFFF8888.toInt()
        } else {
            levelAccent
        }
        val hudHype = GameplayScoreCalculator.currentHudHypeScore(
            won = gameState == GameState.WON,
            lost = gameState == GameState.LOST,
            lastHypeScore = lastHypeScore,
            gameMode = gameMode,
            levelIndex = levelIndex,
            timeLimitSeconds = levelTimeLimitSeconds,
            simElapsed = simElapsed,
            riftEnergy = riftEnergy,
            streak = streak,
            maxChain = maxChain,
            chainCount = chainCount
        )
        drawHudControlsDock(canvas, listOf(musicButton, sfxButton, restartButton, homeButton), levelAccent, dp, paint)
        if (compactHud) {
            val statsLeft = left
            val statsWidth = (controlsLeft - statsLeft - dp * 8f).coerceAtLeast(dp * 82f)
            drawCompactHudStats(canvas, statsLeft, top + dp * 2f, statsWidth, timeRemaining, chainCount, hudHype, levelAccent, stateElapsed, performanceLite, dp, paint, textPaint, t, fitText, formatHypeAmount)
        } else {
            drawHudMetric(canvas, left, top + dp * 39f, dp * 66f, t("TIME").uppercase(), "${((timeRemaining * 10f).roundToInt() / 10f)}s", timeColor, dp, paint, textPaint, fitText, drawWorldAsset, iconKey = "boost_recharge")
            drawHudMetric(canvas, left + dp * 72f, top + dp * 39f, dp * 67f, t("CHAIN").uppercase(), if (chainCount > 0) "x$chainCount" else "-", 0xFFFFCF4A.toInt(), dp, paint, textPaint, fitText, drawWorldAsset, iconKey = "boost_chain")
            drawHudMetric(canvas, left + dp * 145f, top + dp * 39f, dp * 72f, t("HYPE").uppercase(), formatHypeAmount(hudHype), 0xFFFFCF4A.toInt(), dp, paint, textPaint, fitText, drawWorldAsset, iconKey = "boost_prism")
        }
        if (!compactHud && hasRibbon) {
            drawCurseRibbon(canvas, left, top + dp * 68f, (controlsLeft - left - dp * 8f).coerceAtLeast(dp * 120f), selectedBallSkin, levelCurses, compactHud, dp, paint, textPaint, t, fitText, drawWorldAsset)
        }
        if (compactHud && hasRibbon) {
            drawCurseRibbon(canvas, left, top + dp * 40f, (controlsLeft - left - dp * 8f).coerceAtLeast(dp * 120f), selectedBallSkin, levelCurses, compactHud, dp, paint, textPaint, t, fitText, drawWorldAsset)
        }
        val energyWidth = min(viewWidth - dp * (if (compactHud) 88f else 42f), dp * (if (compactHud) 230f else 320f))
        val energyTop = top + dp * (if (compactHud && hasRibbon) 78f else if (compactHud) 54f else 114f)
        drawRiftEnergyBar(canvas, (viewWidth - energyWidth) * 0.5f, energyTop, energyWidth, riftEnergy, levelAccent, dp, showLabel = !compactHud || !hasRibbon, paint = paint, textPaint = textPaint, t = t)
        drawIconButton(canvas, musicButton, ButtonId.MUSIC)
        drawIconButton(canvas, sfxButton, ButtonId.SFX)
        drawIconButton(canvas, homeButton, ButtonId.HOME)
        drawIconButton(canvas, restartButton, ButtonId.RESTART)
        if (!shareButton.isEmpty) {
            drawIconButton(canvas, shareButton, ButtonId.SHARE)
        }
        if (!nextButton.isEmpty) {
            drawIconButton(canvas, nextButton, ButtonId.NEXT)
        }
    }

    fun drawCompactHudStats(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        timeRemaining: Float,
        chainCount: Int,
        hype: Int,
        levelAccent: Int,
        stateElapsed: Float,
        performanceLite: Boolean,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        formatHypeAmount: (Int) -> String
    ) {
        val height = dp * 30f
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left, top, left + width, top + height,
            intArrayOf(0xF0081324.toInt(), 0xD00D1A2D.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + dp * 6f, top + dp * 1f, left + width - dp * 6f, top + dp * 1f, paint)
        paint.color = 0x33FFFFFF
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)

        val splitOne = left + width * 0.34f
        val splitTwo = left + width * 0.62f
        paint.style = Paint.Style.FILL
        paint.color = 0x18FFFFFF
        canvas.drawRect(splitOne, top + dp * 5f, splitOne + dp * 1f, top + height - dp * 5f, paint)
        canvas.drawRect(splitTwo, top + dp * 5f, splitTwo + dp * 1f, top + height - dp * 5f, paint)

        val timeUrgent = timeRemaining in 0.01f..3.0f
        val timeColor = if (timeUrgent) {
            if (sin(stateElapsed * 15f) > 0f) 0xFFFF3333.toInt() else 0xFFFF8888.toInt()
        } else {
            levelAccent
        }
        val timeValue = "${((timeRemaining * 10f).roundToInt() / 10f)}s"
        val chainValue = if (chainCount > 0) "x$chainCount" else "-"
        val hypeValue = formatHypeAmount(hype)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 6.6f
        textPaint.color = 0x82FFFFFF.toInt()
        canvas.drawText(fitText(t("TIME").uppercase(), splitOne - left - dp * 10f), left + dp * 7f, top + dp * 9f, textPaint)
        canvas.drawText(fitText(t("CHAIN").uppercase(), splitTwo - splitOne - dp * 10f), splitOne + dp * 7f, top + dp * 9f, textPaint)
        canvas.drawText(fitText(t("HYPE").uppercase(), left + width - splitTwo - dp * 16f), splitTwo + dp * 7f, top + dp * 9f, textPaint)
        textPaint.textSize = dp * 10.6f
        textPaint.color = if (timeUrgent) timeColor else 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(timeValue, splitOne - left - dp * 12f), left + dp * 7f, top + dp * 23f, textPaint)
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(fitText(chainValue, splitTwo - splitOne - dp * 12f), splitOne + dp * 7f, top + dp * 23f, textPaint)
        textPaint.color = 0xFFFFD75C.toInt()
        canvas.drawText(fitText(hypeValue, left + width - splitTwo - dp * 12f), splitTwo + dp * 7f, top + dp * 23f, textPaint)
        paint.style = Paint.Style.FILL
        paint.color = if (performanceLite) 0xFF64E572.toInt() else withAlpha(timeColor, 185)
        canvas.drawCircle(left + width - dp * 7f, top + dp * 8f, dp * 2.1f, paint)
    }

    fun drawHudControlsDock(
        canvas: Canvas,
        buttons: List<RectF>,
        levelAccent: Int,
        dp: Float,
        paint: Paint
    ) {
        var minLeft = Float.POSITIVE_INFINITY
        var minTop = Float.POSITIVE_INFINITY
        var maxRight = Float.NEGATIVE_INFINITY
        var maxBottom = Float.NEGATIVE_INFINITY
        for (i in 0 until buttons.size) {
            val b = buttons[i]
            if (!b.isEmpty) {
                if (b.left < minLeft) minLeft = b.left
                if (b.top < minTop) minTop = b.top
                if (b.right > maxRight) maxRight = b.right
                if (b.bottom > maxBottom) maxBottom = b.bottom
            }
        }
        if (!minLeft.isFinite()) return
        val left = minLeft - dp * 4f
        val top = minTop - dp * 4f
        val right = maxRight + dp * 4f
        val bottom = maxBottom + dp * 4f
        scratch.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left, top, right, bottom,
            intArrayOf(0x5E16202C, withAlpha(levelAccent, 34), 0x35101622),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp * 9f, dp * 9f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = withAlpha(levelAccent, 94)
        canvas.drawRoundRect(scratch, dp * 9f, dp * 9f, paint)
        // Specular top highlight line
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + dp * 8f, top + dp * 1f, right - dp * 8f, top + dp * 1f, paint)
    }

    fun drawLevelNameGlass(
        canvas: Canvas,
        stageLeft: Float,
        stageWidth: Float,
        scale: Float,
        viewWidth: Float,
        compactHud: Boolean,
        hudBottom: Float,
        accent: Int,
        gameMode: GameMode,
        levelIndex: Int,
        levelTitle: String,
        performanceLite: Boolean,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        localizedLevelTitle: (String) -> String,
        t: (String) -> String = { it }
    ) {
        val stageRight = stageLeft + stageWidth * scale
        val width = (stageRight - stageLeft - dp * 36f).coerceIn(dp * 188f, viewWidth - dp * 44f)
        val left = ((stageLeft + stageRight) * 0.5f - width * 0.5f).coerceIn(dp * 14f, viewWidth - width - dp * 14f)
        val height = dp * (if (compactHud) 22f else 28f)
        val top = (hudBottom - height - dp * (if (compactHud) 7f else 9f)).coerceAtLeast(dp * 48f)
        scratch.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left, top, left + width, top + height,
            intArrayOf(0xD6070B12.toInt(), withAlpha(accent, 52), 0xB0070B12.toInt()),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = if (performanceLite) 0xFF64E572.toInt() else withAlpha(accent, 190)
        canvas.drawRoundRect(left, top, left + dp * 3f, top + height, dp * 2f, dp * 2f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = if (performanceLite) 0x8864E572.toInt() else withAlpha(accent, 145)
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.color = 0x44FFFFFF
        canvas.drawLine(scratch.left + dp * 6f, scratch.top + dp * 1f, scratch.right - dp * 6f, scratch.top + dp * 1f, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * (if (compactHud) 9.3f else 10.8f)
        textPaint.color = 0xEAF7F4FF.toInt()
        val perfTag = if (performanceLite) " [⚡ ECO]" else ""
        val title = "${gameMode.menuTitle(t)} - L${levelIndex.toString().padStart(2, '0')} ${localizedLevelTitle(levelTitle)}$perfTag"
        drawFittedText(
            canvas,
            title,
            scratch.centerX(),
            scratch.centerY() + dp * (if (compactHud) 3.3f else 3.8f),
            width - dp * 24f,
            if (compactHud) 9.3f else 10.8f,
            if (compactHud) 7.2f else 8.2f
        )
    }

    fun drawRiftEnergyBar(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        riftEnergy: Float,
        levelAccent: Int,
        dp: Float,
        showLabel: Boolean = true,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String
    ) {
        val danger = riftEnergy < 0.22f
        val accent = if (danger) 0xFFFF5757.toInt() else levelAccent
        val height = dp * 13f
        val radius = dp * 5.5f
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0x69141B27
        canvas.drawRoundRect(scratch, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = if (danger) withAlpha(accent, 210) else 0x55FFFFFF
        canvas.drawRoundRect(scratch, radius, radius, paint)
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + dp * 4f, top + dp * 1f, left + width - dp * 4f, top + dp * 1f, paint)
        paint.style = Paint.Style.FILL

        val fillWidth = (width * riftEnergy.coerceIn(0f, 1f)).coerceAtLeast(if (riftEnergy > 0f) dp * 5f else 0f)
        if (fillWidth > 0f) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                left, top, left + width, top,
                intArrayOf(withAlpha(accent, 250), withAlpha(0xFFFFCF4A.toInt(), if (danger) 130 else 210)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            scratch2.set(left, top, left + fillWidth, top + height)
            canvas.drawRoundRect(scratch2, radius, radius, paint)
            paint.shader = null
        }

        val segments = 12
        val gap = dp * 2.2f
        val segmentWidth = (width - gap * (segments - 1)) / segments
        repeat(segments) { index ->
            val x = left + index * (segmentWidth + gap)
            scratch2.set(x, top + dp * 2f, x + segmentWidth, top + height - dp * 2f)
            paint.style = Paint.Style.FILL
            paint.color = if (riftEnergy * segments > index) 0x1FFFFFFF else 0x22000000
            canvas.drawRoundRect(scratch2, dp * 2f, dp * 2f, paint)
        }

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 7.4f
        if (showLabel) {
            textPaint.color = 0xBBFFFFFF.toInt()
            canvas.drawText(t("RIFT ENERGY").uppercase(), left + dp * 7f, top - dp * 3f, textPaint)
        }
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = withAlpha(accent, 245)
        canvas.drawText("${(riftEnergy * 100).roundToInt()}%", left + width - dp * 7f, top - dp * 3f, textPaint)
    }

    fun drawHudMetric(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        label: String,
        value: String,
        accent: Int,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        iconKey: String = when (label) {
            "TIME" -> "boost_recharge"
            "CHAIN" -> "boost_chain"
            else -> "boost_prism"
        }
    ) {
        val r = dp * 7f
        scratch.set(left, top, left + width, top + dp * 28f)
        paint.style = Paint.Style.FILL
        paint.color = 0xF0081324.toInt()
        canvas.drawRoundRect(scratch, r, r, paint)

        // Specular top line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + dp * 5f, top + dp * 1f, left + width - dp * 5f, top + dp * 1f, paint)

        // Laser border
        paint.color = 0x33FFFFFF
        canvas.drawRoundRect(scratch, r, r, paint)

        // Accent strip on left
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 220)
        canvas.drawRoundRect(left, top, left + dp * 3f, top + dp * 28f, dp * 1.5f, dp * 1.5f, paint)

        scratch.set(left + width - dp * 18f, top + dp * 3f, left + width - dp * 4f, top + dp * 17f)
        drawWorldAsset(canvas, iconKey, scratch, 155)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 7f
        textPaint.color = 0x77FFFFFF
        canvas.drawText(fitText(label, width - dp * 26f), left + dp * 7f, top + dp * 10f, textPaint)
        textPaint.textSize = dp * 11.5f
        textPaint.color = if (accent == 0xFFFF3333.toInt() || accent == 0xFFFF8888.toInt()) accent else 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, width - dp * 12f), left + dp * 7f, top + dp * 23f, textPaint)
    }

    fun drawCurseRibbon(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        selectedBallSkin: BallSkin,
        curses: List<CurseSpec>,
        compactHud: Boolean,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val power = selectedBallSkin.power
        val hasPowerChip = power != BallPower.NONE
        val totalChips = (if (hasPowerChip) 1 else 0) + curses.size
        if (totalChips == 0) return

        var x = left
        val gap = dp * 6f
        val maxRight = left + width
        val visibleCount = min(2, totalChips)
        for (slot in 0 until visibleCount) {
            val powered = hasPowerChip && slot == 0
            val label: String
            val accent: Int
            if (powered) {
                label = t(TutorialCopy.ballPowerRibbonKey(power)).uppercase()
                accent = selectedBallSkin.lineColor
            } else {
                val curse = curses[if (hasPowerChip) slot - 1 else slot]
                label = t(TutorialCopy.curseRibbonKey(curse.type)).uppercase()
                accent = curse.accent
            }
            textPaint.textSize = dp * 9f
            textPaint.typeface = AssetResourceManager.oxaniumBold()
            val shownLabel = if (powered && compactHud) label else if (powered) "${t("POWER").uppercase()} $label" else label
            val iconReserve = if (powered) dp * 22f else 0f
            val chipWidth = (textPaint.measureText(shownLabel) + dp * 18f + iconReserve).coerceIn(
                dp * (if (powered) 88f else 68f),
                dp * (if (compactHud) 132f else 156f)
            )
            if (x + chipWidth > maxRight) continue
            scratch.set(x, top, x + chipWidth, top + dp * 22f)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (powered) 68 else 42)
            canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * if (powered) 1.5f else 1f
            paint.color = withAlpha(accent, 190)
            canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
            paint.strokeWidth = dp * 0.8f
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratch.left + dp * 4f, scratch.top + dp * 1f, scratch.right - dp * 4f, scratch.top + dp * 1f, paint)
            if (powered) {
                val iconSize = dp * 17f
                scratch2.set(
                    scratch.left + dp * 4f,
                    scratch.centerY() - iconSize * 0.5f,
                    scratch.left + dp * 4f + iconSize,
                    scratch.centerY() + iconSize * 0.5f
                )
                drawWorldAsset(canvas, AssetResourceManager.powerIconKey(power), scratch2, 255)
            }
            textPaint.color = 0xDDF7F4FF.toInt()
            if (powered) {
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(fitText(shownLabel, scratch.width() - dp * 30f), scratch.left + dp * 25f, scratch.centerY() + dp * 3.5f, textPaint)
            } else {
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(fitText(shownLabel, scratch.width() - dp * 10f), scratch.centerX(), scratch.centerY() + dp * 3.5f, textPaint)
            }
            x += chipWidth + gap
        }

        val hidden = totalChips - visibleCount
        if (hidden > 0 && x + dp * 38f <= maxRight) {
            scratch.set(x, top, x + dp * 38f, top + dp * 22f)
            paint.style = Paint.Style.FILL
            paint.color = 0x33454F65
            canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * 0.8f
            paint.color = 0x44FFFFFF
            canvas.drawLine(scratch.left + dp * 3f, scratch.top + dp * 1f, scratch.right - dp * 3f, scratch.top + dp * 1f, paint)
            textPaint.typeface = AssetResourceManager.oxaniumBold()
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = dp * 9f
            textPaint.color = 0xCCFFFFFF.toInt()
            canvas.drawText("+$hidden", scratch.centerX(), scratch.centerY() + dp * 3.5f, textPaint)
        }
    }

    fun drawPowerToast(
        canvas: Canvas,
        powerMessageTimer: Float,
        powerMessage: String,
        skin: BallSkin,
        viewWidth: Float,
        overlayTop: Float,
        isReadyState: Boolean,
        hasTutorialHint: Boolean,
        stateElapsed: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (powerMessageTimer <= 0f || powerMessage.isBlank()) return
        val accent = skin.lineColor
        val width = min(viewWidth - dp * 32f, dp * 360f)
        val height = dp * 48f
        val left = viewWidth * 0.5f - width * 0.5f
        val top = if (isReadyState && stateElapsed <= 3.6f) {
            overlayTop + dp * (if (hasTutorialHint) 126f else 96f)
        } else {
            overlayTop
        }
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xF0080C13.toInt()
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.color = accent
        canvas.drawRect(left, top, left + dp * 4f, top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        // Specular top highlight line
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + dp * 8f, top + dp * 1f, left + width - dp * 8f, top + dp * 1f, paint)

        val powerIconSize = dp * 34f
        scratch.set(left + dp * 10f, top + dp * 7f, left + dp * 10f + powerIconSize, top + dp * 7f + powerIconSize)
        drawWorldAsset(canvas, AssetResourceManager.powerIconKey(skin.power), scratch, 255)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 8f
        textPaint.color = withAlpha(accent, 235)
        val power = skin.power
        val powerName = when (power) {
            BallPower.NONE -> t("NO POWER")
            else -> t(TutorialCopy.ballPowerRibbonKey(power))
        }.uppercase()
        val title = if (power != BallPower.NONE && powerMessage.startsWith(powerName)) {
            t("SUPERPOWER ONLINE").uppercase()
        } else {
            t("SUPERPOWER TRIGGERED").uppercase()
        }
        canvas.drawText(title, left + dp * 52f, top + dp * 17f, textPaint)
        textPaint.textSize = dp * 12f
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(powerMessage, width - dp * 68f), left + dp * 52f, top + dp * 36f, textPaint)
    }

    fun drawMissionBrief(
        canvas: Canvas,
        isReadyState: Boolean,
        stateElapsed: Float,
        hasTutorialHint: Boolean,
        warning: ModeWarning?,
        levelAccent: Int,
        gameModeTitle: String,
        levelIndex: Int,
        levelDifficultyRating: Int,
        levelTitle: String,
        archetype: LevelArchetype,
        skin: BallSkin,
        viewWidth: Float,
        overlayTop: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        localizedLevelTitle: (String) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (!isReadyState || stateElapsed > 3.6f) return
        if (hasTutorialHint) return
        val accent = warning?.accent ?: levelAccent
        val alpha = when {
            stateElapsed < 2.8f -> 1f
            else -> (1f - (stateElapsed - 2.8f) / 0.8f).coerceIn(0f, 1f)
        }
        val width = min(viewWidth - dp * 32f, dp * 430f)
        val height = dp * (if (warning == null) 66f else 76f)
        val left = viewWidth * 0.5f - width * 0.5f
        val top = overlayTop
        scratch.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF080C13.toInt(), (232 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.color = withAlpha(accent, (220 * alpha).roundToInt())
        canvas.drawRect(left, top, left + dp * 4f, top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = withAlpha(accent, (125 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        // Specular top highlight line
        paint.color = withAlpha(0x55FFFFFF, (alpha * 255).roundToInt())
        canvas.drawLine(left + dp * 8f, top + dp * 1f, left + width - dp * 8f, top + dp * 1f, paint)

        val iconX = left + dp * 29f
        val iconY = top + height * 0.5f
        val missionIconSize = dp * 34f
        scratch.set(iconX - missionIconSize * 0.5f, iconY - missionIconSize * 0.5f, iconX + missionIconSize * 0.5f, iconY + missionIconSize * 0.5f)
        drawWorldAsset(canvas, if (warning == null) "boost_rift_pull" else "danger_beacon", scratch, (255 * alpha).roundToInt())

        val textLeft = left + dp * 54f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 9f
        textPaint.color = withAlpha(accent, (235 * alpha).roundToInt())
        canvas.drawText("$gameModeTitle  /  ${t("LEVEL").uppercase()} ${levelIndex.toString().padStart(2, '0')}  /  D$levelDifficultyRating", textLeft, top + dp * 19f, textPaint)
        textPaint.textSize = dp * 16f
        textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255 * alpha).roundToInt())
        canvas.drawText(fitText(localizedLevelTitle(levelTitle), width - dp * 74f), textLeft, top + dp * 41f, textPaint)
        textPaint.textSize = dp * 9f
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (165 * alpha).roundToInt())
        val powerDescription = when (skin.power) {
            BallPower.NONE -> t("COSMETIC LOADOUT")
            BallPower.PRISM_SHIELD -> t("BLOCKS THE FIRST HAZARD HIT")
            BallPower.VOID_PHASE -> t("SLIPS CLOSER TO HAZARDS")
            BallPower.CHROME_RICOCHET -> t("HARDER BOUNCES AND MORE SPEED")
            BallPower.PLASMA_SURGE -> t("STRONGER PULL AND 35% FASTER RECHARGE")
            BallPower.MINOR_PHASE -> t("SMALL HAZARD HITBOX REDUCTION")
            BallPower.MINOR_RICOCHET -> t("SMALL BOUNCE BOOST")
            BallPower.MINOR_SURGE -> t("10% PULL AND 15% RECHARGE BOOST")
        }.uppercase()
        val powerName = when (skin.power) {
            BallPower.NONE -> t("NO POWER")
            else -> t(TutorialCopy.ballPowerRibbonKey(skin.power))
        }.uppercase()

        val detail = warning?.let { "${t(it.title.removeSuffix("!")).uppercase()}  /  ${t(it.subtitle).uppercase()}" }
            ?: skin.power.takeIf { it != BallPower.NONE }?.let { "$powerName  /  $powerDescription" }
            ?: "${t(archetype.label).uppercase()}  /  ${t(archetype.detail).uppercase()}"
        canvas.drawText(fitText(detail, width - dp * 74f), textLeft, top + dp * 59f, textPaint)

        repeat(3) { index ->
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (stateElapsed < index + 1f) (210 * alpha).roundToInt() else (45 * alpha).roundToInt())
            canvas.drawCircle(left + width - dp * (16f + index * 8f), top + dp * 14f, dp * 2f, paint)
        }
    }

    fun drawFinishBurst(
        canvas: Canvas,
        won: Boolean,
        lost: Boolean,
        stateElapsed: Float,
        finishPulse: Float,
        levelAccent: Int,
        skinLineColor: Int,
        seed: Long,
        performanceLite: Boolean,
        goalX: Float,
        goalY: Float,
        ballX: Float,
        ballY: Float,
        dp: Float,
        paint: Paint
    ) {
        if (!won && !lost) return
        val progress = (stateElapsed / 1.2f).coerceIn(0f, 1f)
        val cx = if (won) goalX else ballX
        val cy = if (won) goalY else ballY
        drawFinishConfetti(canvas, won, cx, cy, finishPulse, performanceLite, levelAccent, skinLineColor, seed, stateElapsed, dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val rayCount = if (won) 28 else 14
        repeat(rayCount) { i ->
            val angle = i * PI.toFloat() * 2f / rayCount.toFloat()
            val base = dp * (if (won) 42f else 26f)
            val spread = dp * (if (won) 126f else 76f) * progress
            val inner = base + spread * 0.28f
            val outer = base + spread
            paint.strokeWidth = dp * (if (won) 3.2f else 2.2f)
            paint.color = withAlpha(
                if (won) if (i % 3 == 0) levelAccent else 0xFFFFCF4A.toInt() else 0xFFFF4D8D.toInt(),
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

    fun drawRiftBreakMoment(
        canvas: Canvas,
        lastRiftBreak: Boolean,
        isWon: Boolean,
        riftBreakTimer: Float,
        ballX: Float,
        ballY: Float,
        viewWidth: Float,
        viewHeight: Float,
        accent: Int,
        performanceLite: Boolean,
        lastRiftBreakBonus: Int,
        lastRiftBreakReason: String,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (!lastRiftBreak || !isWon || riftBreakTimer <= 0f) return
        val age = (2.15f - riftBreakTimer).coerceAtLeast(0f)
        val intro = (age / 0.32f).coerceIn(0f, 1f)
        val alpha = (1f - (age / 2.15f)).coerceIn(0f, 1f)
        val cx = ballX.coerceIn(dp * 54f, viewWidth - dp * 54f)
        val cy = ballY.coerceIn(dp * 168f, viewHeight - dp * 160f)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (72f * alpha).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val ringCount = if (performanceLite) 3 else 5
        repeat(ringCount) { index ->
            val wave = ((age * 1.45f + index * 0.18f) % 1f + 1f) % 1f
            paint.strokeWidth = dp * (2.6f + index * 0.45f)
            paint.color = withAlpha(if (index % 2 == 0) accent else 0xFFFFCF4A.toInt(), ((1f - wave) * 230f * alpha).roundToInt())
            canvas.drawCircle(cx, cy, dp * (34f + wave * (112f + index * 15f)), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        val panelWidth = min(viewWidth - dp * 38f, dp * 360f) * intro
        val panelHeight = dp * 78f
        val left = viewWidth * 0.5f - panelWidth * 0.5f
        val top = (cy - dp * 132f).coerceIn(dp * 138f, viewHeight - dp * 260f)
        scratch.set(left, top, left + panelWidth, top + panelHeight)
        if (panelWidth > dp * 80f) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                scratch.left, scratch.top, scratch.right, scratch.bottom,
                intArrayOf(withAlpha(accent, (120f * alpha).roundToInt()), 0xF2070B12.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * 1.2f
            paint.color = withAlpha(0xFFFFCF4A.toInt(), (235f * alpha).roundToInt())
            canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
            paint.strokeWidth = dp * 1f
            paint.color = withAlpha(0x66FFFFFF, (220f * alpha).roundToInt())
            canvas.drawLine(scratch.left + dp * 8f, scratch.top + dp * 1f, scratch.right - dp * 8f, scratch.top + dp * 1f, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = AssetResourceManager.oxaniumBold()
            textPaint.textSize = dp * 26f
            textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255f * alpha).roundToInt())
            canvas.drawText(t("RIFT BREAK").uppercase(), scratch.centerX(), top + dp * 34f, textPaint)
            textPaint.textSize = dp * 12f
            textPaint.color = withAlpha(0xFFFFCF4A.toInt(), (245f * alpha).roundToInt())
            val subtitle = "${lastRiftBreakReason.ifBlank { t("CLEAN RIFT SNAP").uppercase() }}  /  +$lastRiftBreakBonus ${t("HYPE").uppercase()}"
            canvas.drawText(fitText(subtitle, panelWidth - dp * 24f), scratch.centerX(), top + dp * 58f, textPaint)
        }
    }

    fun drawFinishConfetti(
        canvas: Canvas,
        won: Boolean,
        cx: Float,
        cy: Float,
        finishPulse: Float,
        performanceLite: Boolean,
        levelAccent: Int,
        skinLineColor: Int,
        seed: Long,
        stateElapsed: Float,
        dp: Float,
        paint: Paint
    ) {
        val alpha = ((finishPulse * if (won) 210f else 120f).roundToInt()).coerceIn(0, 220)
        if (alpha <= 0) return
        val count = if (performanceLite) (if (won) 12 else 6) else (if (won) 36 else 14)
        val colors = intArrayOf(levelAccent, skinLineColor, 0xFFFFCF4A.toInt(), 0xFFF7F4FF.toInt())
        paint.style = Paint.Style.FILL
        repeat(count) { i ->
            val angle = i * PI.toFloat() * 2f / count + (seed % 19L).toFloat() * 0.017f
            val drift = stateElapsed.coerceAtMost(2.2f)
            val distance = dp * (if (won) 54f else 32f) + dp * (42f + (i % 6) * 13f) * drift
            val x = cx + cos(angle) * distance + sin(stateElapsed * 2.4f + i) * dp * 10f
            val y = cy + sin(angle) * distance + drift * drift * dp * (if (won) 36f else 18f)
            val width = dp * (if (won) 9f else 6f)
            val height = dp * (if (won) 3.8f else 3f)
            canvas.save()
            canvas.rotate((angle * 180f / PI.toFloat()) + stateElapsed * 150f, x, y)
            scratch.set(x - width, y - height, x + width, y + height)
            paint.color = withAlpha(colors[i % colors.size], alpha)
            canvas.drawRoundRect(scratch, dp * 2f, dp * 2f, paint)
            canvas.restore()
        }
    }

    fun layoutToolbarButtons(
        viewWidth: Float,
        dp: (Float) -> Float,
        homeButton: RectF,
        restartButton: RectF,
        sfxButton: RectF,
        musicButton: RectF,
        shareButton: RectF,
        nextButton: RectF
    ) {
        val compactControls = viewWidth < dp(430f)
        val size = dp(if (compactControls) 30f else 38f)
        val gap = dp(if (compactControls) 4f else 8f)
        val rowTop = dp(if (compactControls) 10f else 12f)
        val rowBottom = rowTop + size
        val rightEdge = viewWidth - dp(if (compactControls) 12f else 16f)
        var cursor = rightEdge
        homeButton.left = cursor - size; homeButton.top = rowTop; homeButton.right = cursor; homeButton.bottom = rowBottom
        homeButton.set(homeButton.left, rowTop, cursor, rowBottom)
        cursor -= size + gap
        restartButton.left = cursor - size; restartButton.top = rowTop; restartButton.right = cursor; restartButton.bottom = rowBottom
        restartButton.set(restartButton.left, rowTop, cursor, rowBottom)
        cursor -= size + gap
        sfxButton.left = cursor - size; sfxButton.top = rowTop; sfxButton.right = cursor; sfxButton.bottom = rowBottom
        sfxButton.set(sfxButton.left, rowTop, cursor, rowBottom)
        cursor -= size + gap
        musicButton.left = cursor - size; musicButton.top = rowTop; musicButton.right = cursor; musicButton.bottom = rowBottom
        musicButton.set(musicButton.left, rowTop, cursor, rowBottom)
        shareButton.left = 0f; shareButton.top = 0f; shareButton.right = 0f; shareButton.bottom = 0f
        shareButton.set(0f, 0f, 0f, 0f)
        nextButton.left = 0f; nextButton.top = 0f; nextButton.right = 0f; nextButton.bottom = 0f
        nextButton.set(0f, 0f, 0f, 0f)
    }

    fun levelArchetype(spec: LevelSpec, gameMode: com.moonsolstudios.kavvoro.model.GameMode): LevelArchetype {
        return when {
            spec.portals.isNotEmpty() -> ARCHETYPE_PORTAL_SLING
            spec.hasCurse(CurseType.RIFT_WIND) -> ARCHETYPE_WIND_TUNNEL
            spec.hasCurse(CurseType.OVERHEAT) || spec.hasCurse(CurseType.RIFT_DRAIN) -> ARCHETYPE_ENERGY_TAX
            spec.hasCurse(CurseType.FOCUS_FIELD) || spec.hasCurse(CurseType.POWER_HOLD) -> ARCHETYPE_CONTROL_LAB
            spec.pulseZones.size >= 2 || spec.hasCurse(CurseType.PULSE_STORM) -> ARCHETYPE_PULSE_MAZE
            spec.movingHazardCount >= 3 -> ARCHETYPE_MOVING_DANGER
            spec.blocks.size >= 5 -> ARCHETYPE_GATE_STACK
            gameMode == com.moonsolstudios.kavvoro.model.GameMode.CHAOS -> ARCHETYPE_CHAOS_TOUCH
            else -> ARCHETYPE_RIFT_PATH
        }
    }

    fun drawHudAndOverlays(
        canvas: Canvas,
        level: LevelSpec,
        gameMode: GameMode,
        gameState: GameState,
        selectedBallSkin: BallSkin,
        stageLeft: Float,
        stageWidth: Float,
        scale: Float,
        viewWidth: Float,
        viewHeight: Float,
        simElapsed: Float,
        stateElapsed: Float,
        riftEnergy: Float,
        lastHypeScore: Int,
        streak: Int,
        maxChain: Int,
        chainCount: Int,
        finishPulse: Float,
        lastRiftBreak: Boolean,
        riftBreakTimer: Float,
        lastRiftBreakBonus: Int,
        lastRiftBreakReason: String,
        powerMessageTimer: Float,
        powerMessage: String,
        ballScreenX: Float,
        ballScreenY: Float,
        goalScreenX: Float,
        goalScreenY: Float,
        performanceLite: Boolean,
        dp: Float,
        musicButton: RectF,
        sfxButton: RectF,
        homeButton: RectF,
        restartButton: RectF,
        shareButton: RectF,
        nextButton: RectF,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        localizedLevelTitle: (String) -> String,
        formatHypeAmount: (Int) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawIconButton: (Canvas, RectF, ButtonId) -> Unit,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        onBeforePostOverlays: () -> Unit = {}
    ) {
        val compact = isCompactHud(viewWidth, dp)
        val ribbon = hudHasRibbon(selectedBallSkin.power, level.curses.isNotEmpty())
        val hudBottom = gameplayHudBottom(compact, ribbon, dp)
        val overlayTop = gameplayOverlayTop(compact, ribbon, dp)
        val controlsLeft = hudControlsLeft(
            homeButton, restartButton, sfxButton, musicButton,
            viewWidth
        )
        val archetype = levelArchetype(level, gameMode)
        val warning = currentModeWarning(level)

        drawHud(
            canvas = canvas,
            compactHud = compact,
            hasRibbon = ribbon,
            controlsLeft = controlsLeft,
            toolbarBottom = hudBottom,
            levelAccent = level.accent,
            levelDifficultyRating = level.difficultyRating,
            levelIndex = level.index,
            levelTimeLimitSeconds = level.timeLimitSeconds,
            simElapsed = simElapsed,
            stateElapsed = stateElapsed,
            gameMode = gameMode,
            gameState = gameState,
            riftEnergy = riftEnergy,
            lastHypeScore = lastHypeScore,
            streak = streak,
            maxChain = maxChain,
            chainCount = chainCount,
            performanceLite = performanceLite,
            selectedBallSkin = selectedBallSkin,
            levelCurses = level.curses,
            viewWidth = viewWidth,
            dp = dp,
            musicButton = musicButton,
            sfxButton = sfxButton,
            homeButton = homeButton,
            restartButton = restartButton,
            shareButton = shareButton,
            nextButton = nextButton,
            paint = paint,
            textPaint = textPaint,
            t = t,
            fitText = fitText,
            formatHypeAmount = formatHypeAmount,
            drawWorldAsset = drawWorldAsset,
            drawIconButton = drawIconButton
        )
        drawLevelNameGlass(
            canvas = canvas,
            stageLeft = stageLeft,
            stageWidth = stageWidth,
            scale = scale,
            viewWidth = viewWidth,
            compactHud = compact,
            hudBottom = hudBottom,
            accent = archetype.accent,
            gameMode = gameMode,
            levelIndex = level.index,
            levelTitle = level.title,
            performanceLite = performanceLite,
            dp = dp,
            paint = paint,
            textPaint = textPaint,
            drawFittedText = drawFittedText,
            localizedLevelTitle = localizedLevelTitle,
            t = t
        )
        drawMissionBrief(
            canvas = canvas,
            isReadyState = gameState == GameState.READY,
            stateElapsed = stateElapsed,
            hasTutorialHint = level.tutorialHint.isNotBlank(),
            warning = warning,
            levelAccent = level.accent,
            gameModeTitle = gameMode.menuTitle(t),
            levelIndex = level.index,
            levelDifficultyRating = level.difficultyRating,
            levelTitle = level.title,
            archetype = archetype,
            skin = selectedBallSkin,
            viewWidth = viewWidth,
            overlayTop = overlayTop,
            dp = dp,
            paint = paint,
            textPaint = textPaint,
            t = t,
            fitText = fitText,
            localizedLevelTitle = localizedLevelTitle,
            drawWorldAsset = drawWorldAsset
        )
        drawPowerToast(
            canvas = canvas,
            powerMessageTimer = powerMessageTimer,
            powerMessage = powerMessage,
            skin = selectedBallSkin,
            viewWidth = viewWidth,
            overlayTop = overlayTop,
            isReadyState = gameState == GameState.READY,
            hasTutorialHint = level.tutorialHint.isNotBlank(),
            stateElapsed = stateElapsed,
            dp = dp,
            paint = paint,
            textPaint = textPaint,
            t = t,
            fitText = fitText,
            drawWorldAsset = drawWorldAsset
        )
        onBeforePostOverlays()
        drawFinishBurst(
            canvas = canvas,
            won = gameState == GameState.WON,
            lost = gameState == GameState.LOST,
            stateElapsed = stateElapsed,
            finishPulse = finishPulse,
            levelAccent = level.accent,
            skinLineColor = selectedBallSkin.lineColor,
            seed = level.seed,
            performanceLite = performanceLite,
            goalX = goalScreenX,
            goalY = goalScreenY,
            ballX = ballScreenX,
            ballY = ballScreenY,
            dp = dp,
            paint = paint
        )
        drawRiftBreakMoment(
            canvas = canvas,
            lastRiftBreak = lastRiftBreak,
            isWon = gameState == GameState.WON,
            riftBreakTimer = riftBreakTimer,
            ballX = ballScreenX,
            ballY = ballScreenY,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            accent = selectedBallSkin.lineColor,
            performanceLite = performanceLite,
            lastRiftBreakBonus = lastRiftBreakBonus,
            lastRiftBreakReason = lastRiftBreakReason,
            dp = dp,
            paint = paint,
            textPaint = textPaint,
            t = t,
            fitText = fitText
        )
    }
}
