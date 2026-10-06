package com.moonsolstudios.kavvoro.ui.screens.gameplay

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.Block
import com.moonsolstudios.kavvoro.engine.CurseSpec
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.Hazard
import com.moonsolstudios.kavvoro.engine.HazardMotion
import com.moonsolstudios.kavvoro.engine.PhysicsEngine
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.PortalPair
import com.moonsolstudios.kavvoro.engine.PulseZone
import com.moonsolstudios.kavvoro.engine.STAGE_WIDTH
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dedicated procedural renderer for in-game arena elements:
 * Pulse zones, portals, platforms/blocks, goal vortex, hazards,
 * trajectory/route coach, player trails, drawing assist line, ball & aura.
 */
object GameplayArenaRenderer {

    private val scratch = RectF()
    private val path = Path()

    fun gameplayBallScale(skin: BallSkin, skinIndex: Int = 0): Float {
        return when {
            skin.unlock.type == UnlockType.PREMIUM -> 1.34f
            skin.power != BallPower.NONE -> 1.29f
            skinIndex >= 37 -> 1.25f
            else -> 1.2f
        }
    }

    fun tutorialAnchorPoint(
        start: Point2,
        goal: Point2,
        pulseTarget: Point2?,
        portal: PortalPair?,
        orbit: Float,
        hasFocusField: Boolean,
        hasRiftWind: Boolean,
        stageHeight: Float
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

            hasFocusField -> Point2(
                x = start.x * 0.55f + goal.x * 0.45f,
                y = start.y * 0.55f + goal.y * 0.45f
            )

            hasRiftWind -> Point2(start.x + 1.75f, start.y + 0.8f)
            else -> Point2(start.x + 1.25f, start.y + 1.15f)
        }
        return Point2(
            x = (base.x + cos(orbit) * 0.32f).coerceIn(0.8f, STAGE_WIDTH - 0.8f),
            y = (base.y + sin(orbit) * 0.28f).coerceIn(1.2f, stageHeight - 1.2f)
        )
    }

    fun drawPulseZones(
        canvas: Canvas,
        pulseZones: List<PulseZone>,
        levelAccent: Int,
        levelIndex: Int,
        rich: Boolean,
        lite: Boolean,
        fullEffects: Boolean,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        stateElapsed: Float,
        isSimulating: Boolean,
        ballCenter: Point2,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        pulseZones.forEachIndexed { index, zone ->
            val cx = stageLeft + zone.center.x * scale
            val cy = zone.center.y * scale
            val radius = zone.radius * scale
            val wave = 0.45f + 0.55f * sin(stateElapsed * 2.6f + zone.phase + index)
            val ballInside = isSimulating && ballCenter.distanceTo(zone.center) < zone.radius
            val heat = if (ballInside) 1f else (0.35f + wave * 0.38f)

            paint.style = Paint.Style.FILL
            paint.color = withAlpha(levelAccent, (54f + heat * 78f).roundToInt())
            if (rich && !lite) {
                paint.maskFilter = AssetResourceManager.cachedNormalBlur(radius * 0.34f)
            }
            canvas.drawCircle(cx, cy, radius * (1.02f + wave * 0.1f), paint)
            paint.maskFilter = null

            paint.style = Paint.Style.FILL
            if (rich && !lite) {
                paint.shader = LinearGradient(
                    cx - radius, cy - radius, cx + radius, cy + radius,
                    intArrayOf(
                        withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), (58f + heat * 52f).roundToInt()),
                        withAlpha(levelAccent, (24f + heat * 44f).roundToInt()),
                        0x00000000
                    ),
                    floatArrayOf(0f, 0.48f, 1f),
                    Shader.TileMode.CLAMP
                )
            } else {
                paint.color = withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), (34f + heat * 42f).roundToInt())
            }
            canvas.drawCircle(cx, cy, radius * 1.04f, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            val ringCount = when {
                lite -> 1
                rich -> 4
                else -> 2
            }
            repeat(ringCount) { ring ->
                val raw = stateElapsed * (0.62f + ring * 0.08f) + ring * 0.24f + zone.phase * 0.13f
                val progress = ((raw % 1f) + 1f) % 1f
                paint.strokeWidth = dp * (2.4f + heat * 2.6f - ring * 0.28f)
                paint.color = withAlpha(
                    if (ring % 2 == 0) levelAccent else 0xFFFFCF4A.toInt(),
                    ((1f - progress) * (95f + heat * 88f)).roundToInt()
                )
                canvas.drawCircle(cx, cy, radius * (0.34f + progress * 0.92f), paint)
            }

            val particleCount = when {
                lite -> 0
                fullEffects -> 10
                rich -> 6
                else -> 3
            }
            repeat(particleCount) { particle ->
                val angle = stateElapsed * (if (zone.swirlForce >= 0f) 2.9f else -2.9f) + particle * PI.toFloat() * 2f / particleCount + zone.phase
                val orbit = radius * (0.57f + 0.2f * sin(stateElapsed * 2.1f + particle))
                val px = cx + cos(angle) * orbit
                val py = cy + sin(angle) * orbit
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(if (particle % 2 == 0) 0xFFFFCF4A.toInt() else levelAccent, (118f + heat * 95f).roundToInt())
                canvas.drawCircle(px, py, dp * (2.2f + heat * 1.8f), paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp * (1.2f + heat)
                paint.color = withAlpha(levelAccent, (70f + heat * 90f).roundToInt())
                canvas.drawLine(px, py, px - cos(angle) * dp * (10f + heat * 8f), py - sin(angle) * dp * (10f + heat * 8f), paint)
            }

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * (1.8f + wave * 1.4f)
            paint.color = withAlpha(levelAccent, 120 + (heat * 95).roundToInt())
            canvas.drawCircle(cx, cy, radius * (0.86f + wave * 0.12f), paint)
            paint.strokeWidth = dp * 1.4f
            paint.color = 0x72FFFFFF
            canvas.drawCircle(cx, cy, radius, paint)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(levelAccent, 32 + (heat * 38f).roundToInt())
            canvas.drawCircle(cx, cy, radius * 0.88f, paint)
            val reactorRadius = radius * (0.55f + heat * 0.08f)
            scratch.set(cx - reactorRadius, cy - reactorRadius, cx + reactorRadius, cy + reactorRadius)
            drawWorldAsset(canvas, if (zone.radialForce >= 0f) "reactor_out" else "reactor_in", scratch, if (ballInside) 255 else 245)
            drawPulseIndicator(canvas, zone, cx, cy, radius, wave, rich, lite, levelAccent, stateElapsed, dp, paint)

            if (ballInside || levelIndex <= 10) {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = AssetResourceManager.oxaniumBold()
                textPaint.textSize = dp * (if (ballInside) 10f else 8f)
                textPaint.color = withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), if (ballInside) 245 else 170)
                canvas.drawText(t(if (zone.radialForce >= 0f) "BOOST FIELD" else "VORTEX FIELD").uppercase(), cx, cy + radius + dp * 18f, textPaint)
            }
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    fun drawPulseIndicator(
        canvas: Canvas,
        zone: PulseZone,
        cx: Float,
        cy: Float,
        radius: Float,
        wave: Float,
        rich: Boolean,
        lite: Boolean,
        levelAccent: Int,
        stateElapsed: Float,
        dp: Float,
        paint: Paint
    ) {
        val sign = if (zone.radialForce >= 0f) 1f else -1f
        val baseAngle = stateElapsed * 1.35f + zone.phase
        val arrowDistance = radius * (0.42f + wave * 0.13f)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp * 3.4f
        paint.color = 0xF2FFFFFF.toInt()
        val arrowCount = when {
            lite -> 2
            rich -> 5
            else -> 3
        }
        repeat(arrowCount) { i ->
            val angle = baseAngle + i * PI.toFloat() * 2f / arrowCount
            val inner = arrowDistance * if (sign > 0f) 0.55f else 1.0f
            val outer = arrowDistance * if (sign > 0f) 1.12f else 0.42f
            val x1 = cx + cos(angle) * inner
            val y1 = cy + sin(angle) * inner
            val x2 = cx + cos(angle) * outer
            val y2 = cy + sin(angle) * outer
            paint.strokeWidth = dp * 7f
            paint.color = withAlpha(levelAccent, 54)
            canvas.drawLine(x1, y1, x2, y2, paint)
            paint.strokeWidth = dp * 3.2f
            paint.color = 0xF2FFFFFF.toInt()
            canvas.drawLine(x1, y1, x2, y2, paint)
            drawArrowHead(canvas, x2, y2, angle, dp, paint)
        }

        if (rich && kotlin.math.abs(zone.swirlForce) > 0.4f) {
            paint.strokeWidth = dp * 4.2f
            paint.color = withAlpha(levelAccent, 235)
            val arcRadius = radius * 0.55f
            scratch.set(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius)
            val sweep = if (zone.swirlForce > 0f) 142f else -142f
            val start = (stateElapsed * 92f + zone.phase * 30f) % 360f
            canvas.drawArc(scratch, start, sweep, false, paint)
            val endAngle = (start + sweep) * PI.toFloat() / 180f
            drawArrowHead(canvas, cx + cos(endAngle) * arcRadius, cy + sin(endAngle) * arcRadius, endAngle + if (sweep > 0f) PI.toFloat() * 0.52f else -PI.toFloat() * 0.52f, dp, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawPortals(
        canvas: Canvas,
        portals: List<PortalPair>,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        lite: Boolean,
        rich: Boolean,
        stateElapsed: Float,
        paint: Paint,
        textPaint: Paint,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (portals.isEmpty()) return
        portals.forEach { portal ->
            val ex = stageLeft + portal.entry.x * scale
            val ey = portal.entry.y * scale
            val ox = stageLeft + portal.exit.x * scale
            val oy = portal.exit.y * scale
            val radius = portal.radius * scale

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            if (!lite) {
                paint.strokeWidth = dp * 11f
                paint.color = withAlpha(0xFF45F2FF.toInt(), 32)
                canvas.drawLine(ex, ey, ox, oy, paint)
            }
            paint.strokeWidth = dp * 2.2f
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 140)
            canvas.drawLine(ex, ey, ox, oy, paint)
            paint.strokeCap = Paint.Cap.BUTT

            drawPortalNode(canvas, portal, ex, ey, radius, "IN", 0xFF45F2FF.toInt(), true, rich, lite, stateElapsed, dp, paint, textPaint, drawWorldAsset)
            drawPortalNode(canvas, portal, ox, oy, radius, "OUT", 0xFFFFCF4A.toInt(), false, rich, lite, stateElapsed, dp, paint, textPaint, drawWorldAsset)
        }
    }

    fun drawPortalNode(
        canvas: Canvas,
        portal: PortalPair,
        cx: Float,
        cy: Float,
        radius: Float,
        label: String,
        accent: Int,
        entry: Boolean,
        rich: Boolean,
        lite: Boolean,
        stateElapsed: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val spin = stateElapsed * (if (entry) 92f else -72f) + portal.phase * 45f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 140)
        if (rich && !lite) {
            paint.maskFilter = AssetResourceManager.cachedNormalBlur(radius * 0.62f)
        }
        canvas.drawCircle(cx, cy, radius * 1.42f, paint)
        paint.maskFilter = null

        val assetRadius = radius * 1.05f
        scratch.set(cx - assetRadius, cy - assetRadius, cx + assetRadius, cy + assetRadius)
        canvas.save()
        canvas.rotate(spin, cx, cy)
        drawWorldAsset(canvas, "portal_goal", scratch, 245)
        canvas.restore()

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val rings = when {
            lite -> 0
            rich -> 3
            else -> 1
        }
        repeat(rings) { ring ->
            val progress = ((stateElapsed * (0.7f + ring * 0.09f) + ring * 0.31f + portal.phase) % 1f + 1f) % 1f
            paint.strokeWidth = dp * (2.4f - ring * 0.25f)
            paint.color = withAlpha(accent, ((1f - progress) * 190f).roundToInt())
            canvas.drawCircle(cx, cy, radius * (0.62f + progress * 0.88f), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 9f
        textPaint.color = withAlpha(accent, 245)
        canvas.drawText(label, cx, cy + radius + dp * 18f, textPaint)
    }

    fun drawArrowHead(canvas: Canvas, x: Float, y: Float, angle: Float, dp: Float, paint: Paint) {
        val size = dp * 6f
        paint.style = Paint.Style.FILL
        path.reset()
        path.moveTo(x + cos(angle) * size, y + sin(angle) * size)
        path.lineTo(x + cos(angle + 2.45f) * size, y + sin(angle + 2.45f) * size)
        path.lineTo(x + cos(angle - 2.45f) * size, y + sin(angle - 2.45f) * size)
        path.close()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
    }

    fun drawBlocks(
        canvas: Canvas,
        blocks: List<Block>,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        levelAccent: Int,
        isChaos: Boolean,
        paint: Paint,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        for (block in blocks) {
            val cx = stageLeft + block.center.x * scale
            val cy = block.center.y * scale
            val hw = block.width * 0.5f * scale
            val hh = block.height * 0.5f * scale
            canvas.save()
            canvas.rotate((block.angleRadians * 180f / PI.toFloat()), cx, cy)
            scratch.set(cx - hw, cy - hh, cx + hw, cy + hh)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(levelAccent, 46)
            canvas.drawRoundRect(cx - hw * 1.03f, cy - hh * 1.45f, cx + hw * 1.03f, cy + hh * 1.45f, dp * 5f, dp * 5f, paint)
            drawWorldAsset(canvas, if (isChaos) "platform_chaos" else "platform_classic", scratch, 255)
            paint.color = withAlpha(block.tone, 34)
            canvas.drawRoundRect(scratch, dp * 5f, dp * 5f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * 1.2f
            paint.color = 0x44FFFFFF
            canvas.drawRoundRect(scratch, dp * 5f, dp * 5f, paint)

            // Specular top bevel highlight line
            paint.strokeWidth = dp * 1f
            paint.color = 0x66FFFFFF
            canvas.drawLine(cx - hw + dp * 5f, cy - hh + dp * 1f, cx + hw - dp * 5f, cy - hh + dp * 1f, paint)
            canvas.restore()
        }
    }

    fun drawGoal(
        canvas: Canvas,
        goal: Point2,
        goalRadius: Float,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        stateElapsed: Float,
        isWon: Boolean,
        finishPulse: Float,
        paint: Paint,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val cx = stageLeft + goal.x * scale
        val cy = goal.y * scale
        val radius = goalRadius * scale
        val pulse = 0.9f + 0.1f * sin(stateElapsed * 5.2f)
        val winBurst = if (isWon) 1f + finishPulse * 0.40f else 1f

        val portalRadius = radius * (1.12f + pulse * 0.06f) * winBurst
        scratch.set(cx - portalRadius, cy - portalRadius, cx + portalRadius, cy + portalRadius)
        val savePortal = canvas.save()
        canvas.rotate(stateElapsed * 36f, cx, cy)
        drawWorldAsset(canvas, "portal_goal", scratch, 255)
        canvas.restoreToCount(savePortal)

        val saveVortex = canvas.save()
        canvas.rotate(-stateElapsed * 52f, cx, cy)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1.6f
        paint.color = withAlpha(0xFF64E572.toInt(), (140 + 60 * pulse).roundToInt())
        canvas.drawCircle(cx, cy, radius * 0.82f * winBurst, paint)
        canvas.restoreToCount(saveVortex)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 2.4f
        paint.color = 0xAA64E572.toInt()
        canvas.drawCircle(cx, cy, radius * pulse * winBurst, paint)
        paint.strokeWidth = dp * 1.6f
        paint.color = 0xCCFFFFFF.toInt()
        canvas.drawCircle(cx, cy, radius * 0.56f * winBurst, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0x2264E572
        canvas.drawCircle(cx, cy, radius * 1.4f * winBurst, paint)
    }

    fun drawHazards(
        canvas: Canvas,
        hazards: List<Hazard>,
        isReady: Boolean,
        simElapsed: Float,
        stateElapsed: Float,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        rich: Boolean,
        lite: Boolean,
        paint: Paint,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val hazardTime = if (isReady) 0f else simElapsed
        for (i in 0 until hazards.size) {
            val hazard = hazards[i]
            drawHazardTrack(canvas, hazard, stageLeft, scale, dp, lite, paint)
            val cx = stageLeft + hazard.positionXAt(hazardTime) * scale
            val cy = hazard.positionYAt(hazardTime) * scale
            val r = hazard.radius * scale
            paint.style = Paint.Style.FILL
            paint.color = 0x88FF4D8D.toInt()
            if (rich && !lite) {
                paint.maskFilter = AssetResourceManager.cachedNormalBlur(r * 0.6f)
            }
            canvas.drawCircle(cx, cy, r * 1.18f, paint)
            paint.maskFilter = null
            val hazardKey = when (hazard.motion) {
                HazardMotion.STATIC -> "hazard_static"
                HazardMotion.HORIZONTAL, HazardMotion.VERTICAL -> "hazard_glitch"
                HazardMotion.ORBIT, HazardMotion.FIGURE_EIGHT -> "hazard_void"
            }
            val assetRadius = r * 1.3f
            scratch.set(cx - assetRadius, cy - assetRadius, cx + assetRadius, cy + assetRadius)
            val saveCount = canvas.save()
            canvas.rotate(stateElapsed * if (hazard.isMoving) 42f else 24f, cx, cy)
            drawWorldAsset(canvas, hazardKey, scratch, 255)
            canvas.restoreToCount(saveCount)
        }
    }

    fun drawHazardTrack(
        canvas: Canvas,
        hazard: Hazard,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        lite: Boolean,
        paint: Paint
    ) {
        if (!hazard.isMoving) return
        val cx = stageLeft + hazard.center.x * scale
        val cy = hazard.center.y * scale
        val travel = hazard.travel * scale
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1.2f
        paint.color = 0x55FF4D8D
        when (hazard.motion) {
            HazardMotion.HORIZONTAL -> canvas.drawLine(cx - travel, cy, cx + travel, cy, paint)
            HazardMotion.VERTICAL -> canvas.drawLine(cx, cy - travel, cx, cy + travel, paint)
            HazardMotion.ORBIT -> canvas.drawCircle(cx, cy, travel, paint)
            HazardMotion.FIGURE_EIGHT -> {
                path.reset()
                val segments = if (lite) 16 else 32
                repeat(segments + 1) { i ->
                    val t = i * PI.toFloat() * 2f / segments
                    val x = cx + sin(t) * travel
                    val y = cy + sin(t * 2f) * travel * 0.5f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
            }
            HazardMotion.STATIC -> Unit
        }
        paint.style = Paint.Style.FILL
        paint.color = 0x99FF4D8D.toInt()
        canvas.drawCircle(cx, cy, dp * 2.2f, paint)
    }

    fun drawRouteCoach(
        canvas: Canvas,
        isReady: Boolean,
        levelIndex: Int,
        hasPortals: Boolean,
        tutorialHint: String,
        stateElapsed: Float,
        lineColor: Int,
        start: Point2,
        goal: Point2,
        goalRadius: Float,
        pulseZones: List<PulseZone>,
        portals: List<PortalPair>,
        hazards: List<Hazard>,
        blocks: List<Block>,
        stageHeight: Float,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        menuPulse: Float,
        isCompactHud: Boolean,
        gameplayOverlayTop: Float,
        viewWidth: Float,
        viewHeight: Float,
        actionLabel: String,
        hasFocusField: Boolean,
        hasRiftWind: Boolean,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String
    ) {
        if (!isReady) return
        if ((levelIndex > 10 && !hasPortals) || tutorialHint.isBlank() || stateElapsed > 7.2f) return

        val fade = if (stateElapsed < 5.8f) 1f else (1f - (stateElapsed - 5.8f) / 1.4f).coerceIn(0f, 1f)
        val accent = lineColor
        val orbit = menuPulse * 1.65f
        val portal = portals.firstOrNull()
        val pulseTarget = if (portal == null) pulseZones.firstOrNull()?.center else null
        val anchor = tutorialAnchorPoint(start, goal, pulseTarget, portal, orbit, hasFocusField, hasRiftWind, stageHeight)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp * 12f
        paint.color = withAlpha(accent, (fade * 38f).roundToInt())
        path.reset()
        val startX = stageLeft + start.x * scale
        val startY = start.y * scale
        val goalX = stageLeft + goal.x * scale
        val goalY = goal.y * scale
        val anchorX = stageLeft + anchor.x * scale
        val anchorY = anchor.y * scale

        path.moveTo(startX, startY)
        if (portal != null) {
            val portalEntryX = stageLeft + portal.entry.x * scale
            val portalEntryY = portal.entry.y * scale
            val portalExitX = stageLeft + portal.exit.x * scale
            val portalExitY = portal.exit.y * scale
            path.quadTo(anchorX, anchorY, portalEntryX, portalEntryY)
            path.moveTo(portalExitX, portalExitY)
            path.quadTo(
                stageLeft + (portal.exit.x + goal.x) * 0.5f * scale,
                ((portal.exit.y + goal.y) * 0.5f - 0.45f) * scale,
                goalX,
                goalY
            )
        } else {
            path.quadTo(anchorX, anchorY, goalX, goalY)
        }
        canvas.drawPath(path, paint)
        paint.strokeWidth = dp * 3f
        paint.color = withAlpha(accent, (fade * 185f).roundToInt())
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT

        val pulse = 0.65f + 0.35f * sin(menuPulse * 4.4f)
        drawCoachHalo(canvas, startX, startY, dp * (20f + pulse * 5f), accent, fade, dp, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 2f
        paint.color = withAlpha(accent, (fade * 180f).roundToInt())
        canvas.drawCircle(anchorX, anchorY, dp * (15f + pulse * 8f), paint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, (fade * 110f).roundToInt())
        canvas.drawCircle(anchorX, anchorY, dp * 5f, paint)
        val startTagX = startX
        val startTagY = startY - dp * 34f
        val actionTagX = anchorX
        val actionTagY = anchorY + dp * 34f
        val tagDx = actionTagX - startTagX
        val tagDy = actionTagY - startTagY
        if (!isCompactHud && tagDx * tagDx + tagDy * tagDy > dp * dp * 98f * 98f) {
            drawCoachTag(canvas, startTagX, startTagY, t("START").uppercase(), accent, fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
        }
        drawCoachTag(canvas, actionTagX, actionTagY, actionLabel, accent, fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)

        pulseTarget?.let { target ->
            val targetX = stageLeft + target.x * scale
            val targetY = target.y * scale
            val pRadius = pulseZones.first().radius * scale
            drawCoachHalo(canvas, targetX, targetY, pRadius * 0.64f, 0xFFFFCF4A.toInt(), fade, dp, paint)
            drawCoachTag(canvas, targetX, targetY - dp * 42f, t("BOOST").uppercase(), 0xFFFFCF4A.toInt(), fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
        }

        portal?.let { activePortal ->
            val pEntryX = stageLeft + activePortal.entry.x * scale
            val pEntryY = activePortal.entry.y * scale
            val pExitX = stageLeft + activePortal.exit.x * scale
            val pExitY = activePortal.exit.y * scale
            val pRadius = activePortal.radius * scale
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp * 2.4f
            paint.color = withAlpha(0xFFFFCF4A.toInt(), (fade * 170f).roundToInt())
            canvas.drawLine(pEntryX, pEntryY, pExitX, pExitY, paint)
            paint.strokeCap = Paint.Cap.BUTT
            drawCoachHalo(canvas, pEntryX, pEntryY, pRadius * 1.62f, 0xFF45F2FF.toInt(), fade, dp, paint)
            drawCoachTag(canvas, pEntryX, pEntryY - dp * 42f, t("PORTAL IN").uppercase(), 0xFF45F2FF.toInt(), fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
            drawCoachHalo(canvas, pExitX, pExitY, pRadius * 1.62f, 0xFFFFCF4A.toInt(), fade, dp, paint)
            drawCoachTag(canvas, pExitX, pExitY - dp * 42f, t("PORTAL OUT").uppercase(), 0xFFFFCF4A.toInt(), fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
        }

        val exitTagX = goalX
        val exitTagY = goalY - dp * 42f
        hazards.firstOrNull()?.let { hazard ->
            val hazardX = stageLeft + hazard.positionXAt(0f) * scale
            val hazardY = hazard.positionYAt(0f) * scale
            val avoidTagX = hazardX
            val avoidTagY = hazardY + dp * 38f
            drawCoachHalo(canvas, hazardX, hazardY, hazard.radius * scale * 2.05f, 0xFFFF4D8D.toInt(), fade, dp, paint)
            val dx = avoidTagX - exitTagX
            val dy = avoidTagY - exitTagY
            if (dx * dx + dy * dy > dp * dp * 92f * 92f) {
                drawCoachTag(canvas, avoidTagX, avoidTagY, t("AVOID").uppercase(), 0xFFFF4D8D.toInt(), fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
            }
        }

        blocks.firstOrNull()?.let { block ->
            drawCoachBlockFrame(canvas, block, fade, stageLeft, scale, dp, paint)
            drawCoachTag(canvas, stageLeft + block.center.x * scale, block.center.y * scale - dp * 34f, t("BOUNCE WALL").uppercase(), 0xFF8AA6FF.toInt(), fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
        }

        paint.style = Paint.Style.STROKE
        paint.color = withAlpha(0xFFFFFFFF.toInt(), (fade * 145f).roundToInt())
        canvas.drawCircle(goalX, goalY, goalRadius * scale * (0.75f + pulse * 0.12f), paint)
        drawCoachTag(canvas, exitTagX, exitTagY, t("EXIT").uppercase(), 0xFF64E572.toInt(), fade, isReady, tutorialHint.isNotBlank(), stateElapsed, gameplayOverlayTop, viewWidth, viewHeight, dp, paint, textPaint)
    }

    fun drawCoachHalo(canvas: Canvas, cx: Float, cy: Float, radius: Float, accent: Int, alpha: Float, dp: Float, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 2f
        paint.color = withAlpha(accent, (160 * alpha).roundToInt())
        canvas.drawCircle(cx, cy, radius, paint)
        paint.strokeWidth = dp * 7f
        paint.color = withAlpha(accent, (36 * alpha).roundToInt())
        canvas.drawCircle(cx, cy, radius * 1.08f, paint)
    }

    fun drawCoachBlockFrame(canvas: Canvas, block: Block, alpha: Float, stageLeft: Float, scale: Float, dp: Float, paint: Paint) {
        val cx = stageLeft + block.center.x * scale
        val cy = block.center.y * scale
        val hw = block.width * 0.5f * scale
        val hh = block.height * 0.5f * scale
        canvas.save()
        canvas.rotate(block.angleRadians * 180f / PI.toFloat(), cx, cy)
        scratch.set(cx - hw * 1.12f, cy - hh * 2.7f, cx + hw * 1.12f, cy + hh * 2.7f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 2.2f
        paint.color = withAlpha(0xFF8AA6FF.toInt(), (170 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp * 6f, dp * 6f, paint)
        paint.strokeWidth = dp * 7f
        paint.color = withAlpha(0xFF8AA6FF.toInt(), (30 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        canvas.restore()
    }

    fun drawCoachTag(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        label: String,
        accent: Int,
        alpha: Float,
        isReady: Boolean,
        hasTutorialHint: Boolean,
        stateElapsed: Float,
        gameplayOverlayTop: Float,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint
    ) {
        val width = max(dp * 52f, textPaint.apply { textSize = dp * 9f }.measureText(label) + dp * 22f)
        val height = dp * 23f
        val minLeft = dp * 8f
        val maxLeft = max(minLeft, viewWidth - width - dp * 8f)
        val reservedTop = when {
            isReady && hasTutorialHint && stateElapsed <= 3.8f -> gameplayOverlayTop + dp * 86f
            isReady && hasTutorialHint -> gameplayOverlayTop + dp * 38f
            else -> dp * 72f
        }
        val reservedBottom = if (isReady && hasTutorialHint) {
            viewHeight - dp * 138f - dp * 50f
        } else {
            viewHeight - dp * 24f
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
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = withAlpha(accent, (190 * alpha).roundToInt())
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.color = withAlpha(0x55FFFFFF, (220 * alpha).roundToInt())
        canvas.drawLine(scratch.left + dp * 4f, scratch.top + dp * 1f, scratch.right - dp * 4f, scratch.top + dp * 1f, paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 9f
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (238 * alpha).roundToInt())
        canvas.drawText(label, scratch.centerX(), scratch.centerY() + dp * 3.5f, textPaint)
    }

    fun drawReplayTail(
        canvas: Canvas,
        replayFrames: List<PhysicsFrame>,
        isSimulating: Boolean,
        lite: Boolean,
        lineColor: Int,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        paint: Paint
    ) {
        if (replayFrames.size < 2 || isSimulating) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = dp * 3f
        path.reset()
        val startIndex = (replayFrames.size - if (lite) 54 else 120).coerceAtLeast(0)
        for (index in startIndex until replayFrames.size) {
            val frame = replayFrames[index]
            val p = frame.ball
            val px = stageLeft + p.x * scale
            val py = p.y * scale
            if (index == startIndex) path.moveTo(px, py) else path.lineTo(px, py)
        }
        paint.color = withAlpha(lineColor, 92)
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawRiftTrail(
        canvas: Canvas,
        playerLine: List<Point2>,
        lite: Boolean,
        lineColor: Int,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        paint: Paint
    ) {
        if (playerLine.isEmpty()) return
        paint.style = Paint.Style.FILL
        val startIndex = (playerLine.size - if (lite) 42 else 100).coerceAtLeast(0)
        val visibleSize = playerLine.size - startIndex
        val trailStep = if (lite) 4 else 2
        for (index in startIndex until playerLine.size) {
            val localIndex = index - startIndex
            if (localIndex % trailStep != 0) continue
            val point = playerLine[index]
            val progress = localIndex / visibleSize.toFloat()
            paint.color = withAlpha(lineColor, (18f + progress * 92f).roundToInt())
            canvas.drawCircle(stageLeft + point.x * scale, point.y * scale, dp * (1.2f + progress * 2.2f), paint)
        }
    }

    fun drawDrawingAssist(
        canvas: Canvas,
        isSimulating: Boolean,
        riftActive: Boolean,
        riftAnchor: Point2?,
        ballCenter: Point2,
        skin: BallSkin,
        riftEnergy: Float,
        riftHoldSeconds: Float,
        stateElapsed: Float,
        simElapsed: Float,
        levelIndex: Int,
        hasFocusField: Boolean,
        hasPowerHold: Boolean,
        hasOverheat: Boolean,
        hasRiftWind: Boolean,
        hasPulseStorm: Boolean,
        lite: Boolean,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (!isSimulating || !riftActive) return
        val anchor = riftAnchor ?: return
        val cx = stageLeft + anchor.x * scale
        val cy = anchor.y * scale
        val ballX = stageLeft + ballCenter.x * scale
        val ballY = ballCenter.y * scale
        val danger = riftEnergy < 0.22f
        val accent = if (danger) 0xFFFF5757.toInt() else skin.lineColor
        val pulse = 0.65f + 0.35f * sin(stateElapsed * 8.5f)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = dp * 15f
        paint.color = withAlpha(accent, 42)
        canvas.drawLine(ballX, ballY, cx, cy, paint)
        paint.strokeWidth = dp * 5f
        paint.color = withAlpha(accent, 210)
        canvas.drawLine(ballX, ballY, cx, cy, paint)
        paint.strokeWidth = dp * 1.2f
        paint.color = 0xDDF7F4FF.toInt()
        canvas.drawLine(ballX, ballY, cx, cy, paint)

        if (hasFocusField) {
            paint.strokeWidth = dp * 2.4f
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 150)
            canvas.drawCircle(ballX, ballY, dp * (28f + pulse * 5f), paint)
            paint.strokeWidth = dp * 9f
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 28)
            canvas.drawCircle(ballX, ballY, dp * (28f + pulse * 5f), paint)
        }

        paint.strokeWidth = dp * 2.2f
        paint.color = withAlpha(accent, 190)
        val powerGrowth = if (hasPowerHold || hasOverheat) {
            min(1f, riftHoldSeconds / 0.9f) * 9f
        } else {
            0f
        }
        canvas.drawCircle(cx, cy, dp * (13f + pulse * 6f + powerGrowth), paint)
        paint.strokeWidth = dp * 1.1f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 130)
        canvas.drawCircle(cx, cy, dp * 4.5f, paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        repeat(if (lite) 0 else 6) { i ->
            val angle = stateElapsed * 5.2f + i * PI.toFloat() * 2f / 6f
            val distance = dp * (15f + (i % 3) * 4f) * (0.75f + pulse * 0.25f)
            paint.color = withAlpha(if (i % 2 == 0) accent else 0xFFFFCF4A.toInt(), 120)
            canvas.drawCircle(cx + cos(angle) * distance, cy + sin(angle) * distance, dp * 1.7f, paint)
        }

        val energy = (riftEnergy * 100f).roundToInt().coerceIn(0, 100)
        val holdMode = when {
            hasFocusField -> t("FOCUS").uppercase()
            hasPowerHold -> "${t("POWER").uppercase()} ${(min(1f, riftHoldSeconds / 0.9f) * 100).roundToInt()}%"
            hasOverheat -> "${t("HEAT").uppercase()} ${(min(1f, riftHoldSeconds / 1.0f) * 100).roundToInt()}%"
            hasRiftWind -> t("WIND GUARD").uppercase()
            hasPulseStorm -> t("PULSE GUARD").uppercase()
            levelIndex <= 3 && simElapsed < 4.2f -> t("TAP TO PULL").uppercase()
            else -> ""
        }
        val label = if (holdMode.isBlank()) "${t("RIFT").uppercase()} $energy%" else "${t("RIFT").uppercase()} $energy%   $holdMode"
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 10f
        val chipWidth = (textPaint.measureText(label) + dp * 24f).coerceIn(dp * 94f, dp * 194f)
        val chipHeight = dp * 30f
        val chipLeft = (cx - chipWidth * 0.5f).coerceIn(dp * 12f, viewWidth - dp * 12f - chipWidth)
        val chipTop = (cy - dp * 58f).coerceIn(dp * 112f, viewHeight - dp * 104f)
        scratch.set(chipLeft, chipTop, chipLeft + chipWidth, chipTop + chipHeight)
        paint.style = Paint.Style.FILL
        paint.color = 0xE607090F.toInt()
        canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
        paint.color = withAlpha(accent, 58)
        canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = withAlpha(accent, 190)
        canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
        paint.color = 0x55FFFFFF
        canvas.drawLine(scratch.left + dp * 6f, scratch.top + dp * 1f, scratch.right - dp * 6f, scratch.top + dp * 1f, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, scratch.width() - dp * 12f), scratch.centerX(), scratch.centerY() + dp * 3.6f, textPaint)
    }

    fun drawBall(
        canvas: Canvas,
        ballCenter: Point2,
        goalCenter: Point2,
        isWon: Boolean,
        isSimulating: Boolean,
        stateElapsed: Float,
        skin: BallSkin,
        skinIndex: Int,
        totalSkinCount: Int,
        pulseIntensity: Float,
        levelAccent: Int,
        hasPulseStorm: Boolean,
        lite: Boolean,
        rich: Boolean,
        full: Boolean,
        adaptiveQuality: Float,
        menuPulse: Float,
        liveBallTrail: List<Point2>,
        chainCount: Int,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit
    ) {
        val rawCx = stageLeft + ballCenter.x * scale
        val rawCy = ballCenter.y * scale
        val cx: Float
        val cy: Float
        val winShrink: Float
        if (isWon) {
            val suctionT = (stateElapsed / 0.45f).coerceIn(0f, 1f)
            val smoothSuction = suctionT * suctionT * (3f - 2f * suctionT)
            val goalCx = stageLeft + goalCenter.x * scale
            val goalCy = goalCenter.y * scale
            cx = rawCx + (goalCx - rawCx) * smoothSuction
            cy = rawCy + (goalCy - rawCy) * smoothSuction
            winShrink = (1f - smoothSuction * 0.65f).coerceAtLeast(0.32f)
        } else {
            cx = rawCx
            cy = rawCy
            winShrink = 1f
        }
        val r = PhysicsEngine.BALL_RADIUS * scale
        val visualRadius = r * gameplayBallScale(skin, skinIndex) * winShrink
        drawGameplayBallTrail(canvas, liveBallTrail, skin, visualRadius, lite, stageLeft, scale, paint)
        drawGameplayBallAura(canvas, cx, cy, visualRadius, skin, skinIndex, totalSkinCount, lite, adaptiveQuality, rich, full, menuPulse, dp, paint)
        if (isSimulating && pulseIntensity > 0.34f) {
            val alpha = (pulseIntensity.coerceIn(0f, 1f) * 210f).roundToInt()
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dp * 3.4f
            paint.color = withAlpha(0xFFFFCF4A.toInt(), alpha)
            canvas.drawCircle(cx, cy, visualRadius * (2.0f + pulseIntensity * 0.75f), paint)
            paint.strokeWidth = dp * 1.5f
            paint.color = withAlpha(levelAccent, alpha)
            canvas.drawCircle(cx, cy, visualRadius * (1.38f + pulseIntensity * 0.55f), paint)
            paint.strokeCap = Paint.Cap.BUTT
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = AssetResourceManager.oxaniumBold()
            textPaint.textSize = dp * 8f
            textPaint.color = withAlpha(0xFFFFCF4A.toInt(), alpha)
            canvas.drawText(t(if (hasPulseStorm) "STORM" else "BOOST").uppercase(), cx, cy - visualRadius * 2.25f, textPaint)
        }
        drawBallSkin(canvas, cx, cy, visualRadius, skin, true, false)
        drawGameplayPowerBadge(canvas, cx, cy, visualRadius, skin, menuPulse, dp, paint, drawWorldAsset)
        drawBrainballLiveTag(canvas, isSimulating, lite, chainCount, pulseIntensity, skin, stateElapsed, cx, cy, visualRadius, viewWidth, viewHeight, dp, paint, textPaint, t, fitText)
    }

    fun drawGameplayBallTrail(
        canvas: Canvas,
        liveBallTrail: List<Point2>,
        skin: BallSkin,
        radius: Float,
        lite: Boolean,
        stageLeft: Float,
        scale: Float,
        paint: Paint
    ) {
        if (liveBallTrail.size < 2) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        val trailCount = if (lite) 7 else 24
        val startIndex = (liveBallTrail.size - trailCount).coerceAtLeast(0)
        for (index in startIndex until liveBallTrail.lastIndex) {
            val a = liveBallTrail[index]
            val b = liveBallTrail[index + 1]
            val progress = (index - startIndex + 1).toFloat() / (liveBallTrail.size - startIndex).coerceAtLeast(1)
            val powerAlpha = if (skin.power == BallPower.NONE) 0.72f else 1f
            paint.strokeWidth = if (lite) radius * 0.22f else radius * (0.12f + progress * 0.38f)
            paint.color = withAlpha(
                if (index % 2 == 0) skin.lineColor else skin.secondary,
                (progress * (if (lite) 190f else 145f) * powerAlpha).roundToInt()
            )
            canvas.drawLine(stageLeft + a.x * scale, a.y * scale, stageLeft + b.x * scale, b.y * scale, paint)
        }
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawGameplayBallAura(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        skinIndex: Int,
        totalSkinCount: Int,
        lite: Boolean,
        adaptiveQuality: Float,
        rich: Boolean,
        full: Boolean,
        menuPulse: Float,
        dp: Float,
        paint: Paint
    ) {
        if (lite) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * 1.2f
            paint.color = withAlpha(skin.lineColor, 125)
            canvas.drawCircle(cx, cy, radius * 1.30f, paint)
            return
        }
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val powered = skin.power != BallPower.NONE
        val late = skinIndex >= 37
        val baseOrbitCount = when {
            premium -> 4
            powered || late -> 3
            else -> 1
        }
        val orbitCount = if (rich) baseOrbitCount else min(baseOrbitCount, 1)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(skin.lineColor, if (premium || powered) 150 else 92)
        if (rich) {
            paint.maskFilter = AssetResourceManager.cachedNormalBlur(radius * 0.82f)
        }
        canvas.drawCircle(cx, cy, radius * if (premium) 1.82f else 1.58f, paint)
        paint.maskFilter = null

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(orbitCount) { ring ->
            val orbit = menuPulse * (1.25f + ring * 0.18f) + ring * PI.toFloat() * 0.42f
            val rx = radius * (1.15f + ring * 0.16f)
            val ry = radius * (0.72f + ring * 0.1f)
            paint.strokeWidth = radius * (0.045f + ring * 0.008f)
            paint.color = withAlpha(if (ring % 2 == 0) skin.secondary else skin.lineColor, 150 - ring * 20)
            scratch.set(cx - rx, cy - ry, cx + rx, cy + ry)
            canvas.save()
            canvas.rotate((orbit * 180f / PI.toFloat()) % 360f, cx, cy)
            canvas.drawOval(scratch, paint)
            canvas.restore()
        }
        paint.strokeCap = Paint.Cap.BUTT

        if ((powered || premium || late) && !lite && adaptiveQuality >= 0.58f) {
            paint.style = Paint.Style.FILL
            val particles = when {
                full && premium -> 8
                rich -> 5
                else -> 3
            }
            repeat(particles) { index ->
                val angle = menuPulse * (2.2f + index * 0.06f) + index * PI.toFloat() * 2f / particles
                val distance = radius * (1.48f + (index % 3) * 0.18f)
                paint.color = withAlpha(if (index % 2 == 0) skin.lineColor else 0xFFFFCF4A.toInt(), 185)
                canvas.drawCircle(
                    cx + cos(angle) * distance,
                    cy + sin(angle) * distance,
                    radius * if (premium) 0.08f else 0.058f,
                    paint
                )
            }
        }
    }

    fun drawGameplayPowerBadge(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        menuPulse: Float,
        dp: Float,
        paint: Paint,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (skin.power == BallPower.NONE) return
        val badge = radius * 0.58f
        val angle = menuPulse * 2.35f
        val bx = cx + cos(angle) * radius * 1.18f
        val by = cy + sin(angle) * radius * 1.18f
        scratch.set(bx - badge * 0.5f, by - badge * 0.5f, bx + badge * 0.5f, by + badge * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 195)
        canvas.drawCircle(bx, by, badge * 0.56f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(dp * 1f, radius * 0.035f)
        paint.color = withAlpha(skin.lineColor, 230)
        canvas.drawCircle(bx, by, badge * 0.56f, paint)
        drawWorldAsset(canvas, AssetResourceManager.powerIconKey(skin.power), scratch, 235)
    }

    fun drawBrainballLiveTag(
        canvas: Canvas,
        isSimulating: Boolean,
        lite: Boolean,
        chainCount: Int,
        pulseIntensity: Float,
        skin: BallSkin,
        stateElapsed: Float,
        cx: Float,
        cy: Float,
        radius: Float,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (!isSimulating) return
        if (lite && chainCount < 5) return
        val (label, tagAccent) = when {
            chainCount >= 10 -> "⚡ OVERDRIVE x$chainCount" to 0xFFFFCF4A.toInt()
            chainCount >= 7 -> "✦ SURGE x$chainCount" to 0xFFFF2E93.toInt()
            chainCount >= 5 -> "${skin.name} ${t("CHAIN").uppercase()} x$chainCount" to 0xFF00E5FF.toInt()
            chainCount >= 3 -> "${t("CHAIN SPIKE").uppercase()} x$chainCount" to skin.lineColor
            pulseIntensity >= 0.62f -> t("BOOST FIELD").uppercase() to 0xFF64E572.toInt()
            else -> return
        }
        val tagScale = if (chainCount >= 7) 1f + 0.06f * sin(stateElapsed * 14f) else 1f
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 8.5f * tagScale
        val width = (textPaint.measureText(label) + dp * 18f).coerceIn(dp * 72f, dp * 210f) * tagScale
        val height = dp * 21f * tagScale
        val left = (cx - width * 0.5f).coerceIn(dp * 8f, viewWidth - width - dp * 8f)
        val top = (cy - radius * 1.95f - height).coerceIn(dp * 138f, viewHeight - dp * 72f)
        scratch.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 215)
        canvas.drawRoundRect(scratch, dp * 6f, dp * 6f, paint)
        paint.color = withAlpha(tagAccent, 45)
        canvas.drawRoundRect(scratch, dp * 6f, dp * 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * (if (chainCount >= 7) 1.4f else 0.9f)
        paint.color = withAlpha(tagAccent, if (chainCount >= 7) 255 else 190)
        canvas.drawRoundRect(scratch, dp * 6f, dp * 6f, paint)
        paint.color = 0x55FFFFFF
        canvas.drawLine(scratch.left + dp * 4f, scratch.top + dp * 1f, scratch.right - dp * 4f, scratch.top + dp * 1f, paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, width - dp * 10f), scratch.centerX(), scratch.centerY() + dp * 3.2f * tagScale, textPaint)
    }

    fun drawCurseAtmosphere(
        canvas: Canvas,
        curses: List<CurseSpec>,
        rich: Boolean,
        hasRiftWind: Boolean,
        hasRiftDrain: Boolean,
        hasOverheat: Boolean,
        isSimulating: Boolean,
        stateElapsed: Float,
        riftEnergy: Float,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        paint: Paint
    ) {
        if (curses.isEmpty()) return

        if (hasRiftWind) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp * 2.2f
            paint.strokeCap = Paint.Cap.ROUND
            repeat(if (rich) 8 else 4) { i ->
                val direction = if (sin(stateElapsed * 1.45f + i) >= 0f) 1f else -1f
                val y = dp * 122f + i * viewHeight * 0.095f + sin(stateElapsed * 2f + i) * dp * 7f
                val x = ((stateElapsed * 68f * direction + i * 83f) % (viewWidth + dp * 90f)) - dp * 45f
                val startX = if (direction > 0f) x else viewWidth - x
                paint.color = if (i % 2 == 0) 0x668AA6FF else 0x5545F2FF
                canvas.drawLine(startX, y, startX + direction * dp * 44f, y + sin(i.toFloat()) * dp * 5f, paint)
                canvas.drawLine(startX + direction * dp * 44f, y + sin(i.toFloat()) * dp * 5f, startX + direction * dp * 31f, y - dp * 8f, paint)
                canvas.drawLine(startX + direction * dp * 44f, y + sin(i.toFloat()) * dp * 5f, startX + direction * dp * 31f, y + dp * 8f, paint)
            }
            paint.strokeCap = Paint.Cap.BUTT
        }

        if (hasRiftDrain) {
            paint.style = Paint.Style.FILL
            repeat(if (rich) 10 else 5) { i ->
                val x = ((i * 97) % 1000) / 1000f * viewWidth
                val y = ((stateElapsed * 72f + i * 53f) % viewHeight)
                paint.color = if (i % 2 == 0) 0x4464E572 else 0x331DE8C8
                canvas.drawRoundRect(x, y, x + dp * 3f, y + dp * (12f + i % 4), dp * 2f, dp * 2f, paint)
            }
        }

        if (hasOverheat && isSimulating) {
            paint.style = Paint.Style.FILL
            paint.color = 0x44FF5757
            canvas.drawRect(0f, dp * 104f, viewWidth * riftEnergy, dp * 108f, paint)
        }
    }

    fun drawArenaScene(
        canvas: Canvas,
        level: com.moonsolstudios.kavvoro.engine.LevelSpec,
        state: com.moonsolstudios.kavvoro.model.GameState,
        gameMode: com.moonsolstudios.kavvoro.model.GameMode,
        ballCenter: Point2,
        skin: BallSkin,
        skinIndex: Int,
        totalSkinCount: Int,
        stateElapsed: Float,
        simElapsed: Float,
        menuPulse: Float,
        finishPulse: Float,
        pulseIntensity: Float,
        riftEnergy: Float,
        riftActive: Boolean,
        riftAnchor: Point2?,
        riftHoldSeconds: Float,
        chainCount: Int,
        adaptiveQuality: Float,
        stageHeight: Float,
        stageLeft: Float,
        scale: Float,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float,
        isCompactHud: Boolean,
        gameplayOverlayTop: Float,
        actionLabel: String,
        rich: Boolean,
        lite: Boolean,
        fullEffects: Boolean,
        replayFrames: List<PhysicsFrame>,
        playerLine: List<Point2>,
        liveBallTrail: List<Point2>,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit
    ) {
        fun hasCurse(type: CurseType): Boolean = level.hasCurse(type)
        val isReady = state == com.moonsolstudios.kavvoro.model.GameState.READY
        val isSimulating = state == com.moonsolstudios.kavvoro.model.GameState.SIMULATING
        val isWon = state == com.moonsolstudios.kavvoro.model.GameState.WON

        drawCurseAtmosphere(
            canvas = canvas,
            curses = level.curses,
            rich = rich,
            hasRiftWind = hasCurse(CurseType.RIFT_WIND),
            hasRiftDrain = hasCurse(CurseType.RIFT_DRAIN),
            hasOverheat = hasCurse(CurseType.OVERHEAT),
            isSimulating = isSimulating,
            stateElapsed = stateElapsed,
            riftEnergy = riftEnergy,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            dp = dp,
            paint = paint
        )
        drawPulseZones(
            canvas = canvas,
            pulseZones = level.pulseZones,
            levelAccent = level.accent,
            levelIndex = level.index,
            rich = rich,
            lite = lite,
            fullEffects = fullEffects,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            stateElapsed = stateElapsed,
            isSimulating = isSimulating,
            ballCenter = ballCenter,
            paint = paint,
            textPaint = textPaint,
            t = t,
            drawWorldAsset = drawWorldAsset
        )
        drawPortals(
            canvas = canvas,
            portals = level.portals,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            lite = lite,
            rich = rich,
            stateElapsed = stateElapsed,
            paint = paint,
            textPaint = textPaint,
            drawWorldAsset = drawWorldAsset
        )
        drawBlocks(
            canvas = canvas,
            blocks = level.blocks,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            levelAccent = level.accent,
            isChaos = gameMode == com.moonsolstudios.kavvoro.model.GameMode.CHAOS,
            paint = paint,
            drawWorldAsset = drawWorldAsset
        )
        drawGoal(
            canvas = canvas,
            goal = level.goal,
            goalRadius = level.goalRadius,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            stateElapsed = stateElapsed,
            isWon = isWon,
            finishPulse = finishPulse,
            paint = paint,
            drawWorldAsset = drawWorldAsset
        )
        drawHazards(
            canvas = canvas,
            hazards = level.hazards,
            isReady = isReady,
            simElapsed = simElapsed,
            stateElapsed = stateElapsed,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            rich = rich,
            lite = lite,
            paint = paint,
            drawWorldAsset = drawWorldAsset
        )
        drawRouteCoach(
            canvas = canvas,
            isReady = isReady,
            levelIndex = level.index,
            hasPortals = level.portals.isNotEmpty(),
            tutorialHint = level.tutorialHint,
            stateElapsed = stateElapsed,
            lineColor = skin.lineColor,
            start = level.start,
            goal = level.goal,
            goalRadius = level.goalRadius,
            pulseZones = level.pulseZones,
            portals = level.portals,
            hazards = level.hazards,
            blocks = level.blocks,
            stageHeight = stageHeight,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            menuPulse = menuPulse,
            isCompactHud = isCompactHud,
            gameplayOverlayTop = gameplayOverlayTop,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            actionLabel = actionLabel,
            hasFocusField = hasCurse(CurseType.FOCUS_FIELD),
            hasRiftWind = hasCurse(CurseType.RIFT_WIND),
            paint = paint,
            textPaint = textPaint,
            t = t
        )
        drawReplayTail(
            canvas = canvas,
            replayFrames = replayFrames,
            isSimulating = isSimulating,
            lite = lite,
            lineColor = skin.lineColor,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            paint = paint
        )
        drawRiftTrail(
            canvas = canvas,
            playerLine = playerLine,
            lite = lite,
            lineColor = skin.lineColor,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            paint = paint
        )
        drawDrawingAssist(
            canvas = canvas,
            isSimulating = isSimulating,
            riftActive = riftActive,
            riftAnchor = riftAnchor,
            ballCenter = ballCenter,
            skin = skin,
            riftEnergy = riftEnergy,
            riftHoldSeconds = riftHoldSeconds,
            stateElapsed = stateElapsed,
            simElapsed = simElapsed,
            levelIndex = level.index,
            hasFocusField = hasCurse(CurseType.FOCUS_FIELD),
            hasPowerHold = hasCurse(CurseType.POWER_HOLD),
            hasOverheat = hasCurse(CurseType.OVERHEAT),
            hasRiftWind = hasCurse(CurseType.RIFT_WIND),
            hasPulseStorm = hasCurse(CurseType.PULSE_STORM),
            lite = lite,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            paint = paint,
            textPaint = textPaint,
            t = t,
            fitText = fitText
        )
        drawBall(
            canvas = canvas,
            ballCenter = ballCenter,
            goalCenter = level.goal,
            isWon = isWon,
            isSimulating = isSimulating,
            stateElapsed = stateElapsed,
            skin = skin,
            skinIndex = skinIndex,
            totalSkinCount = totalSkinCount,
            pulseIntensity = pulseIntensity,
            levelAccent = level.accent,
            hasPulseStorm = hasCurse(CurseType.PULSE_STORM),
            lite = lite,
            rich = rich,
            full = fullEffects,
            adaptiveQuality = adaptiveQuality,
            menuPulse = menuPulse,
            liveBallTrail = liveBallTrail,
            chainCount = chainCount,
            stageLeft = stageLeft,
            scale = scale,
            dp = dp,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            paint = paint,
            textPaint = textPaint,
            t = t,
            fitText = fitText,
            drawWorldAsset = drawWorldAsset,
            drawBallSkin = drawBallSkin
        )
    }
}
