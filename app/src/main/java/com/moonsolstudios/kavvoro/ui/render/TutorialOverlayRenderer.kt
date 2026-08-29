package com.moonsolstudios.kavvoro.ui.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.PortalPair
import com.moonsolstudios.kavvoro.engine.STAGE_WIDTH
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.ui.LocaleLayoutPolicy
import com.moonsolstudios.kavvoro.i18n.TutorialCopy
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.ui.TutorialCardLayout
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Procedural renderer for the Route Coach trajectory path, beacons, tags, and interactive Tutorial hint cards.
 */
object TutorialOverlayRenderer {

    private val path = Path()
    private val scratch = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawRouteCoach(
        canvas: Canvas,
        state: GameState,
        level: LevelSpec,
        stateElapsed: Float,
        menuPulse: Float,
        accent: Int,
        isCompactHud: Boolean,
        viewWidth: Float,
        viewHeight: Float,
        gameplayOverlayTop: Float,
        paint: Paint,
        dp: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        t: (String) -> String,
        levelHasCurse: (CurseType) -> Boolean
    ) {
        if (state != GameState.READY) return
        if ((level.index > 10 && level.portals.isEmpty()) || level.tutorialHint.isBlank() || stateElapsed > 7.2f) return

        val fade = if (stateElapsed < 5.8f) 1f else (1f - (stateElapsed - 5.8f) / 1.4f).coerceIn(0f, 1f)
        val start = level.start
        val goal = level.goal
        val orbit = menuPulse * 1.65f
        val portal = level.portals.firstOrNull()
        val pulseTarget = if (portal == null) level.pulseZones.firstOrNull()?.center else null
        val anchor = tutorialAnchorPoint(start, goal, pulseTarget, portal, orbit, level.stageHeight, levelHasCurse)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 12f * dp
        paint.color = withAlpha(accent, (fade * 38f).roundToInt())
        path.reset()
        path.moveTo(sx(start.x), sy(start.y))
        if (portal != null) {
            path.quadTo(sx(anchor.x), sy(anchor.y), sx(portal.entry.x), sy(portal.entry.y))
            path.moveTo(sx(portal.exit.x), sy(portal.exit.y))
            path.quadTo(
                sx((portal.exit.x + goal.x) * 0.5f),
                sy((portal.exit.y + goal.y) * 0.5f - 0.45f),
                sx(goal.x),
                sy(goal.y)
            )
        } else {
            path.quadTo(sx(anchor.x), sy(anchor.y), sx(goal.x), sy(goal.y))
        }
        canvas.drawPath(path, paint)
        paint.strokeWidth = 3f * dp
        paint.color = withAlpha(accent, (fade * 185f).roundToInt())
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT

        val pulse = 0.65f + 0.35f * sin(menuPulse * 4.4f)
        drawCoachHalo(canvas, sx(start.x), sy(start.y), (20f + pulse * 5f) * dp, accent, fade, paint, dp)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.color = withAlpha(accent, (fade * 180f).roundToInt())
        canvas.drawCircle(sx(anchor.x), sy(anchor.y), (15f + pulse * 8f) * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, (fade * 110f).roundToInt())
        canvas.drawCircle(sx(anchor.x), sy(anchor.y), 5f * dp, paint)
        val startTagX = sx(start.x)
        val startTagY = sy(start.y) - 34f * dp
        val actionTagX = sx(anchor.x)
        val actionTagY = sy(anchor.y) + 34f * dp
        val tagDx = actionTagX - startTagX
        val tagDy = actionTagY - startTagY
        if (!isCompactHud && tagDx * tagDx + tagDy * tagDy > (98f * dp) * (98f * dp)) {
            drawCoachTag(canvas, startTagX, startTagY, t("START").uppercase(), accent, fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
        }
        drawCoachTag(canvas, actionTagX, actionTagY, tutorialActionLabel(levelHasCurse, t), accent, fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)

        pulseTarget?.let { target ->
            drawCoachHalo(canvas, sx(target.x), sy(target.y), worldToScreen(level.pulseZones.first().radius * 0.64f), 0xFFFFCF4A.toInt(), fade, paint, dp)
            drawCoachTag(canvas, sx(target.x), sy(target.y) - 42f * dp, t("BOOST").uppercase(), 0xFFFFCF4A.toInt(), fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
        }

        portal?.let { activePortal ->
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 2.4f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), (fade * 170f).roundToInt())
            canvas.drawLine(sx(activePortal.entry.x), sy(activePortal.entry.y), sx(activePortal.exit.x), sy(activePortal.exit.y), paint)
            paint.strokeCap = Paint.Cap.BUTT
            drawCoachHalo(canvas, sx(activePortal.entry.x), sy(activePortal.entry.y), worldToScreen(activePortal.radius * 1.62f), 0xFF45F2FF.toInt(), fade, paint, dp)
            drawCoachTag(canvas, sx(activePortal.entry.x), sy(activePortal.entry.y) - 42f * dp, t("PORTAL IN").uppercase(), 0xFF45F2FF.toInt(), fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
            drawCoachHalo(canvas, sx(activePortal.exit.x), sy(activePortal.exit.y), worldToScreen(activePortal.radius * 1.62f), 0xFFFFCF4A.toInt(), fade, paint, dp)
            drawCoachTag(canvas, sx(activePortal.exit.x), sy(activePortal.exit.y) - 42f * dp, t("PORTAL OUT").uppercase(), 0xFFFFCF4A.toInt(), fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
        }

        val exitTagX = sx(goal.x)
        val exitTagY = sy(goal.y) - 42f * dp
        level.hazards.firstOrNull()?.let { hazard ->
            val position = hazard.positionAt(0f)
            val avoidTagX = sx(position.x)
            val avoidTagY = sy(position.y) + 38f * dp
            drawCoachHalo(canvas, sx(position.x), sy(position.y), worldToScreen(hazard.radius * 2.05f), 0xFFFF4D8D.toInt(), fade, paint, dp)
            val dx = avoidTagX - exitTagX
            val dy = avoidTagY - exitTagY
            if (dx * dx + dy * dy > (92f * dp) * (92f * dp)) {
                drawCoachTag(canvas, avoidTagX, avoidTagY, t("AVOID").uppercase(), 0xFFFF4D8D.toInt(), fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
            }
        }

        level.blocks.firstOrNull()?.let { block ->
            drawCoachBlockFrame(canvas, block, fade, sx, sy, worldToScreen, paint, dp)
            drawCoachTag(canvas, sx(block.center.x), sy(block.center.y) - 34f * dp, t("BOUNCE WALL").uppercase(), 0xFF8AA6FF.toInt(), fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
        }

        paint.style = Paint.Style.STROKE
        paint.color = withAlpha(0xFFFFFFFF.toInt(), (fade * 145f).roundToInt())
        canvas.drawCircle(sx(goal.x), sy(goal.y), worldToScreen(level.goalRadius * (0.75f + pulse * 0.12f)), paint)
        drawCoachTag(canvas, exitTagX, exitTagY, t("EXIT").uppercase(), 0xFF64E572.toInt(), fade, state, level, stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, paint, dp)
    }

    private fun tutorialAnchorPoint(
        start: Point2,
        goal: Point2,
        pulseTarget: Point2?,
        portal: PortalPair?,
        orbit: Float,
        stageHeight: Float,
        levelHasCurse: (CurseType) -> Boolean
    ): Point2 {
        val base = when {
            portal != null -> Point2(
                x = start.x * 0.35f + portal.entry.x * 0.65f,
                y = start.y * 0.35f + portal.entry.y * 0.65f
            )
            pulseTarget != null -> Point2(
                x = start.x * 0.45f + pulseTarget.x * 0.55f,
                y = start.y * 0.35f + pulseTarget.y * 0.65f
            )
            levelHasCurse(CurseType.FOCUS_FIELD) -> Point2(
                x = start.x * 0.55f + goal.x * 0.45f,
                y = start.y * 0.55f + goal.y * 0.45f
            )
            levelHasCurse(CurseType.RIFT_WIND) -> Point2(start.x + 1.75f, start.y + 0.8f)
            else -> Point2(start.x + 1.25f, start.y + 1.15f)
        }
        return Point2(
            x = (base.x + cos(orbit) * 0.32f).coerceIn(0.8f, STAGE_WIDTH - 0.8f),
            y = (base.y + sin(orbit) * 0.28f).coerceIn(1.2f, stageHeight - 1.2f)
        )
    }

    fun tutorialActionLabel(levelHasCurse: (CurseType) -> Boolean, t: (String) -> String): String {
        return t(
            TutorialCopy.actionLabelKey(
                hasOverheat = levelHasCurse(CurseType.OVERHEAT),
                hasPowerTap = levelHasCurse(CurseType.POWER_HOLD),
                hasFocusField = levelHasCurse(CurseType.FOCUS_FIELD),
                hasRiftDrain = levelHasCurse(CurseType.RIFT_DRAIN)
            )
        ).uppercase()
    }

    fun drawCoachHalo(canvas: Canvas, cx: Float, cy: Float, radius: Float, accent: Int, alpha: Float, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.color = withAlpha(accent, (160 * alpha).roundToInt())
        canvas.drawCircle(cx, cy, radius, paint)
        paint.strokeWidth = 7f * dp
        paint.color = withAlpha(accent, (36 * alpha).roundToInt())
        canvas.drawCircle(cx, cy, radius * 1.08f, paint)
    }

    fun drawCoachBlockFrame(
        canvas: Canvas,
        block: com.moonsolstudios.kavvoro.engine.Block,
        alpha: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        paint: Paint,
        dp: Float
    ) {
        val cx = sx(block.center.x)
        val cy = sy(block.center.y)
        val hw = worldToScreen(block.width * 0.5f)
        val hh = worldToScreen(block.height * 0.5f)
        canvas.save()
        canvas.rotate(block.angleRadians * 180f / PI.toFloat(), cx, cy)
        scratch.set(cx - hw * 1.12f, cy - hh * 2.7f, cx + hw * 1.12f, cy + hh * 2.7f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * dp
        paint.color = withAlpha(0xFF8AA6FF.toInt(), (170 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 6f * dp, 6f * dp, paint)
        paint.strokeWidth = 7f * dp
        paint.color = withAlpha(0xFF8AA6FF.toInt(), (30 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)
        canvas.restore()
    }

    fun drawCoachTag(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        label: String,
        accent: Int,
        alpha: Float,
        state: GameState,
        level: LevelSpec,
        stateElapsed: Float,
        gameplayOverlayTop: Float,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint,
        dp: Float
    ) {
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        val width = max(52f * dp, textPaint.measureText(label) + 22f * dp)
        val height = 23f * dp
        val minLeft = 8f * dp
        val maxLeft = max(minLeft, viewWidth - width - 8f * dp)
        val reservedTop = when {
            state == GameState.READY && level.tutorialHint.isNotBlank() && stateElapsed <= 3.8f -> gameplayOverlayTop + 86f * dp
            state == GameState.READY && level.tutorialHint.isNotBlank() -> gameplayOverlayTop + 38f * dp
            else -> 72f * dp
        }
        val reservedBottom = if (state == GameState.READY && level.tutorialHint.isNotBlank()) {
            viewHeight - 138f * dp - 50f * dp
        } else {
            viewHeight - 24f * dp
        }
        val maxTop = max(reservedTop, reservedBottom - height)
        scratch.set(
            (cx - width * 0.5f).coerceIn(minLeft, maxLeft),
            (cy - height * 0.5f).coerceIn(reservedTop, maxTop),
            0f,
            0f
        )
        scratch.right = scratch.left + width
        scratch.bottom = scratch.top + height
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (218 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, (190 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, 7f * dp, 7f * dp, paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (238 * alpha).roundToInt())
        canvas.drawText(label, scratch.centerX(), scratch.centerY() + 3.5f * dp, textPaint)
    }

    fun drawTutorialHint(
        canvas: Canvas,
        tutorialCardVisible: Boolean,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF,
        safeContentWidth: Float,
        safeInsetLeft: Float,
        bottom34: Float,
        accent: Int,
        level: LevelSpec,
        lessonLines: List<String>,
        obstacleLine: String,
        actionPressed: Boolean,
        context: Context,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        tutorialIconKey: () -> String,
        levelHasCurse: (CurseType) -> Boolean
    ) {
        if (!tutorialCardVisible) {
            tutorialCardBounds.setEmpty()
            tutorialStartButton.setEmpty()
            return
        }

        val width = min(safeContentWidth - 36f * dp, 430f * dp)
        val height = 184f * dp
        val left = safeInsetLeft + safeContentWidth * 0.5f - width * 0.5f
        val top = bottom34 - height
        val allLessonLines = lessonLines + obstacleLine
        tutorialCardBounds.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xF407090F.toInt()
        canvas.drawRoundRect(tutorialCardBounds, 8f * dp, 8f * dp, paint)
        paint.shader = LinearGradient(left, top, left + width, top + height, intArrayOf(withAlpha(accent, 54), 0x0007090F), null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(tutorialCardBounds, 8f * dp, 8f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(tutorialCardBounds, 8f * dp, 8f * dp, paint)

        val iconSize = 42f * dp
        scratch.set(left + 11f * dp, top + 16f * dp, left + 11f * dp + iconSize, top + 16f * dp + iconSize)
        drawWorldAsset(canvas, tutorialIconKey(), scratch, 235)

        val activeLanguage = KavvoroI18n.active(context)
        val isRtl = LocaleLayoutPolicy.isRtl(activeLanguage)
        val textX = if (isRtl) tutorialCardBounds.right - 14f * dp else left + 62f * dp
        val textMaxWidth = LocaleLayoutPolicy.safeContentWidth(width, 76f * dp, activeLanguage)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 10f * dp
        textPaint.color = accent
        val tutorialHeader = if (level.index <= 10) {
            "${t("TRAINING").uppercase()} ${level.index}/10  /  ${tutorialActionLabel(levelHasCurse, t)}"
        } else {
            "${t("RIFT MODULE").uppercase()} L${level.index.toString().padStart(2, '0')}  /  ${t("PORTAL").uppercase()}"
        }
        drawFittedText(canvas, tutorialHeader, textX, top + 21f * dp, textMaxWidth, 10f, 7.2f)

        textPaint.textSize = 11f * dp
        textPaint.color = 0xEFFFFFFF.toInt()
        val visibleLessonLines = allLessonLines.take(4)
        val widestLessonLine = visibleLessonLines.maxOfOrNull(textPaint::measureText) ?: 0f
        textPaint.textSize = dp * TutorialCardLayout.fittedTextSize(
            startSize = 11f,
            minSize = 7.2f,
            maxWidth = textMaxWidth,
            maxMeasuredWidth = widestLessonLine
        )
        visibleLessonLines.forEachIndexed { index, line ->
            canvas.drawText(fitText(line, textMaxWidth), textX, top + (40f + index * 15f) * dp, textPaint)
        }

        paint.style = Paint.Style.FILL
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 8.2f * dp
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), 225)
        val footer = if (level.index < 10) {
            "${t("NO ADS IN TRAINING").uppercase()}  /  ${t("L10 UNLOCKS VORO GRAD").uppercase()}"
        } else {
            t("TRAINING REWARD READY").uppercase()
        }
        canvas.drawText(fitText(footer, textMaxWidth), textX, top + 103f * dp, textPaint)

        val actionBounds = TutorialCardLayout.localeSafeHorizontalBounds(
            cardLeft = tutorialCardBounds.left,
            cardRight = tutorialCardBounds.right,
            padding = 14f * dp,
            language = activeLanguage
        )
        paint.style = Paint.Style.FILL
        paint.color = 0x22FFFFFF
        canvas.drawRoundRect(
            actionBounds.left,
            top + 116f * dp,
            actionBounds.right,
            top + 117.5f * dp,
            1f * dp,
            1f * dp,
            paint
        )

        tutorialStartButton.set(
            actionBounds.left,
            top + 126f * dp,
            actionBounds.right,
            top + 170f * dp
        )

        // Draw Action button
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            tutorialStartButton.left,
            tutorialStartButton.top,
            tutorialStartButton.right,
            tutorialStartButton.bottom,
            intArrayOf(
                withAlpha(accent, if (actionPressed) 255 else 232),
                withAlpha(accent, if (actionPressed) 180 else 132)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(tutorialStartButton, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (actionPressed) 1.8f else 1.1f) * dp
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (actionPressed) 235 else 175)
        canvas.drawRoundRect(tutorialStartButton, 7f * dp, 7f * dp, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.color = 0xFFF7F4FF.toInt()
        drawFittedText(
            canvas,
            t("GOT IT").uppercase(),
            tutorialStartButton.centerX(),
            tutorialStartButton.centerY() + 4f * dp,
            tutorialStartButton.width() - 20f * dp,
            12f,
            8f
        )
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
