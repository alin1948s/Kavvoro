package com.moonsolstudios.kavvoro.ui.render

import android.graphics.*
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameState
import kotlin.math.min
import kotlin.math.roundToInt

data class ModeWarning(
    val title: String,
    val subtitle: String,
    val accent: Int
)

data class LevelArchetypeInfo(
    val label: String,
    val detail: String,
    val accent: Int,
    val iconKey: String
)

object MissionBriefRenderer {

    private val scratch = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    fun currentModeWarning(
        level: LevelSpec,
        levelHasCurse: (CurseType) -> Boolean
    ): ModeWarning? {
        return when {
            level.portals.isNotEmpty() -> ModeWarning(
                title = "PORTAL RIFT!",
                subtitle = "ENTER IN / EXIT OUT WITH EXTRA SPEED",
                accent = 0xFF45F2FF.toInt()
            )

            levelHasCurse(CurseType.RIFT_WIND) && levelHasCurse(CurseType.OVERHEAT) -> ModeWarning(
                title = "WIND + OVERHEAT!",
                subtitle = "TAP AGAINST WIND / ENERGY DRAINS FAST",
                accent = 0xFF8AA6FF.toInt()
            )

            levelHasCurse(CurseType.FOCUS_FIELD) && levelHasCurse(CurseType.HEAVY_CORE) -> ModeWarning(
                title = "FOCUS HEAVY!",
                subtitle = "TAP TO SLOW / GRAVITY IS HEAVY",
                accent = 0xFFFFCF4A.toInt()
            )

            levelHasCurse(CurseType.POWER_HOLD) && levelHasCurse(CurseType.MOON_GLIDE) -> ModeWarning(
                title = "POWER MOON!",
                subtitle = "POWER TAP / GLIDE AFTER BURST",
                accent = 0xFF45F2FF.toInt()
            )

            levelHasCurse(CurseType.RIFT_WIND) -> ModeWarning(
                title = "WIND GUARD!",
                subtitle = "TAP AGAINST THE GUST",
                accent = 0xFF8AA6FF.toInt()
            )

            levelHasCurse(CurseType.OVERHEAT) -> ModeWarning(
                title = "OVERHEAT!",
                subtitle = "POWER RISES / ENERGY MELTS FAST",
                accent = 0xFFFF5757.toInt()
            )

            levelHasCurse(CurseType.RIFT_DRAIN) -> ModeWarning(
                title = "RIFT DRAIN!",
                subtitle = "USE SHORT CONTROL BURSTS",
                accent = 0xFF64E572.toInt()
            )

            levelHasCurse(CurseType.PULSE_STORM) -> ModeWarning(
                title = "PULSE GUARD!",
                subtitle = "TAP TO DAMPEN PULSE FORCE",
                accent = 0xFFC15CFF.toInt()
            )

            levelHasCurse(CurseType.FOCUS_FIELD) -> ModeWarning(
                title = "FOCUS FIELD!",
                subtitle = "TAP TO SLOW FOR PRECISION",
                accent = 0xFFFFCF4A.toInt()
            )

            levelHasCurse(CurseType.POWER_HOLD) -> ModeWarning(
                title = "POWER TAP!",
                subtitle = "RAPID TAPS BUILD FORCE",
                accent = 0xFFFF4D8D.toInt()
            )

            levelHasCurse(CurseType.HEAVY_CORE) -> ModeWarning(
                title = "HEAVY CORE!",
                subtitle = "GRAVITY PULLS HARDER",
                accent = 0xFFFF8C42.toInt()
            )

            levelHasCurse(CurseType.MOON_GLIDE) -> ModeWarning(
                title = "MOON GLIDE!",
                subtitle = "RELEASE KEEPS MOMENTUM",
                accent = 0xFF45F2FF.toInt()
            )

            levelHasCurse(CurseType.TINY_GATE) -> ModeWarning(
                title = "TINY GATE!",
                subtitle = "THE EXIT WINDOW IS SMALLER",
                accent = 0xFFF7F4FF.toInt()
            )

            else -> null
        }
    }

    fun drawMissionBrief(
        canvas: Canvas,
        state: GameState,
        stateElapsed: Float,
        level: LevelSpec,
        gameModeTitle: String,
        warning: ModeWarning?,
        selectedSkin: BallSkin,
        archetypeLabel: String,
        archetypeDetail: String,
        safeContentWidth: Float,
        safeCenterX: Float,
        overlayTop: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        localizedLevelTitle: (String) -> String,
        ballPowerName: (BallPower) -> String,
        ballPowerDescription: (BallPower) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (state != GameState.READY || stateElapsed > 3.6f) return
        if (level.tutorialHint.isNotBlank()) return
        val accent = warning?.accent ?: level.accent
        val alpha = when {
            stateElapsed < 2.8f -> 1f
            else -> (1f - (stateElapsed - 2.8f) / 0.8f).coerceIn(0f, 1f)
        }
        val width = min(safeContentWidth - 32f * dp, 430f * dp)
        val height = (if (warning == null) 66f else 76f) * dp
        val left = safeCenterX - width * 0.5f
        val top = overlayTop
        scratch.set(left, top, left + width, top + height)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF080C13.toInt(), (232 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)
        paint.color = withAlpha(accent, (220 * alpha).roundToInt())
        canvas.drawRect(left, top, left + 4f * dp, top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(accent, (125 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)

        val iconX = left + 29f * dp
        val iconY = top + height * 0.5f
        val missionIconSize = 34f * dp
        scratch.set(iconX - missionIconSize * 0.5f, iconY - missionIconSize * 0.5f, iconX + missionIconSize * 0.5f, iconY + missionIconSize * 0.5f)
        drawWorldAsset(canvas, if (warning == null) "boost_rift_pull" else "danger_beacon", scratch, (255 * alpha).roundToInt())

        val textLeft = left + 54f * dp
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, (235 * alpha).roundToInt())
        canvas.drawText("$gameModeTitle  /  ${t("LEVEL").uppercase()} ${level.index.toString().padStart(2, '0')}  /  D${level.difficultyRating}", textLeft, top + 19f * dp, textPaint)
        textPaint.textSize = 16f * dp
        textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255 * alpha).roundToInt())
        canvas.drawText(fitText(localizedLevelTitle(level.title), width - 74f * dp), textLeft, top + 41f * dp, textPaint)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (165 * alpha).roundToInt())
        val detail = warning?.let { "${t(it.title.removeSuffix("!")).uppercase()}  /  ${t(it.subtitle).uppercase()}" }
            ?: selectedSkin.power.takeIf { it != BallPower.NONE }?.let { "${ballPowerName(it)}  /  ${ballPowerDescription(it)}" }
            ?: "${t(archetypeLabel).uppercase()}  /  ${t(archetypeDetail).uppercase()}"
        canvas.drawText(fitText(detail, width - 74f * dp), textLeft, top + 59f * dp, textPaint)

        repeat(3) { index ->
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (stateElapsed < index + 1f) (210 * alpha).roundToInt() else (45 * alpha).roundToInt())
            canvas.drawCircle(left + width - (16f + index * 8f) * dp, top + 14f * dp, 2f * dp, paint)
        }
    }

    fun drawLevelStartCard(
        canvas: Canvas,
        state: GameState,
        stateElapsed: Float,
        level: LevelSpec,
        gameModeTitle: String,
        rewardLine: String,
        selectedSkin: BallSkin,
        safeContentWidth: Float,
        safeCenterX: Float,
        top: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        localizedLevelTitle: (String) -> String,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit
    ) {
        if (state != GameState.READY) return
        if (stateElapsed > 2.6f && level.tutorialHint.isNotBlank()) return
        val width = min(safeContentWidth - 42f * dp, 380f * dp)
        val height = 82f * dp
        val left = safeCenterX - width * 0.5f
        val alpha = if (stateElapsed < 2.2f) 1f else (1f - (stateElapsed - 2.2f) / 0.8f).coerceIn(0f, 1f)
        if (alpha <= 0f) return

        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (215 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = withAlpha(level.accent, (190 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)

        drawBallSkin(canvas, left + 34f * dp, top + height * 0.5f, 18f * dp, selectedSkin, true, false)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 10f * dp
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (150 * alpha).roundToInt())
        canvas.drawText("$gameModeTitle ${t("LEVEL").uppercase()} ${level.index.toString().padStart(2, '0')}", left + 62f * dp, top + 24f * dp, textPaint)
        textPaint.textSize = 17f * dp
        textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255 * alpha).roundToInt())
        canvas.drawText(fitText(localizedLevelTitle(level.title), width - 82f * dp), left + 62f * dp, top + 48f * dp, textPaint)
        textPaint.textSize = 10f * dp
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), (230 * alpha).roundToInt())
        canvas.drawText(fitText(rewardLine, width - 82f * dp), left + 62f * dp, top + 66f * dp, textPaint)
    }

    fun drawRiftBreakMoment(
        canvas: Canvas,
        lastRiftBreak: Boolean,
        state: GameState,
        riftBreakTimer: Float,
        ballScreenX: Float,
        ballScreenY: Float,
        accent: Int,
        lastRiftBreakReason: String,
        lastRiftBreakBonus: Int,
        safeInsetLeft: Float,
        safeInsetRight: Float,
        safeContentWidth: Float,
        safeCenterX: Float,
        viewWidth: Float,
        viewHeight: Float,
        safeTop138: Float,
        safeBottom260: Float,
        safeTop168: Float,
        safeBottom160: Float,
        performanceLite: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (!lastRiftBreak || state != GameState.WON || riftBreakTimer <= 0f) return
        val age = (2.15f - riftBreakTimer).coerceAtLeast(0f)
        val intro = (age / 0.32f).coerceIn(0f, 1f)
        val alpha = (1f - (age / 2.15f)).coerceIn(0f, 1f)
        val cx = ballScreenX.coerceIn(safeInsetLeft + 54f * dp, viewWidth - safeInsetRight - 54f * dp)
        val cy = ballScreenY.coerceIn(safeTop168, safeBottom160)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (72f * alpha).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(if (performanceLite) 3 else 5) { index ->
            val wave = ((age * 1.45f + index * 0.18f) % 1f + 1f) % 1f
            paint.strokeWidth = (2.6f + index * 0.45f) * dp
            paint.color = withAlpha(if (index % 2 == 0) accent else 0xFFFFCF4A.toInt(), ((1f - wave) * 230f * alpha).roundToInt())
            canvas.drawCircle(cx, cy, (34f + wave * (112f + index * 15f)) * dp, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        val panelWidth = min(safeContentWidth - 38f * dp, 360f * dp) * intro
        val panelHeight = 78f * dp
        val left = safeCenterX - panelWidth * 0.5f
        val top = (cy - 132f * dp).coerceIn(safeTop138, safeBottom260)
        scratch.set(left, top, left + panelWidth, top + panelHeight)
        if (panelWidth > 80f * dp) {
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                scratch.left,
                scratch.top,
                scratch.right,
                scratch.bottom,
                intArrayOf(withAlpha(accent, (120f * alpha).roundToInt()), 0xF2070B12.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), (235f * alpha).roundToInt())
            canvas.drawRoundRect(scratch, 8f * dp, 8f * dp, paint)

            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
            textPaint.textSize = 26f * dp
            textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (255f * alpha).roundToInt())
            canvas.drawText(t("RIFT BREAK").uppercase(), scratch.centerX(), top + 34f * dp, textPaint)
            textPaint.textSize = 12f * dp
            textPaint.color = withAlpha(0xFFFFCF4A.toInt(), (245f * alpha).roundToInt())
            val subtitle = "${lastRiftBreakReason.ifBlank { t("CLEAN RIFT SNAP").uppercase() }}  /  +$lastRiftBreakBonus ${t("HYPE").uppercase()}"
            canvas.drawText(fitText(subtitle, panelWidth - 24f * dp), scratch.centerX(), top + 58f * dp, textPaint)
        }
    }
}
