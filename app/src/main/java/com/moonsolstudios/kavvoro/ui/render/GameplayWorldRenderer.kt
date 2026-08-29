package com.moonsolstudios.kavvoro.ui.render

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.moonsolstudios.kavvoro.engine.Block
import com.moonsolstudios.kavvoro.engine.Hazard
import com.moonsolstudios.kavvoro.engine.HazardMotion
import com.moonsolstudios.kavvoro.engine.PortalPair
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Procedural world entities renderer for Gameplay (Portals, Blocks, Goal, Hazards, and Tracks).
 */
object GameplayWorldRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawPortals(
        canvas: Canvas,
        portals: List<PortalPair>,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        stateElapsed: Float,
        performanceLite: Boolean,
        richEffects: Boolean,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (portals.isEmpty()) return
        portals.forEach { portal ->
            val ex = sx(portal.entry.x)
            val ey = sy(portal.entry.y)
            val ox = sx(portal.exit.x)
            val oy = sy(portal.exit.y)
            val radius = worldToScreen(portal.radius)

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            if (!performanceLite) {
                paint.strokeWidth = 11f * dp
                paint.color = withAlpha(0xFF45F2FF.toInt(), 32)
                canvas.drawLine(ex, ey, ox, oy, paint)
            }
            paint.strokeWidth = 2.2f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 140)
            canvas.drawLine(ex, ey, ox, oy, paint)
            paint.strokeCap = Paint.Cap.BUTT

            drawPortalNode(canvas, portal, ex, ey, radius, "IN", 0xFF45F2FF.toInt(), true, stateElapsed, performanceLite, richEffects, paint, dp, drawWorldAsset)
            drawPortalNode(canvas, portal, ox, oy, radius, "OUT", 0xFFFFCF4A.toInt(), false, stateElapsed, performanceLite, richEffects, paint, dp, drawWorldAsset)
        }
    }

    private fun drawPortalNode(
        canvas: Canvas,
        portal: PortalPair,
        cx: Float,
        cy: Float,
        radius: Float,
        label: String,
        accent: Int,
        entry: Boolean,
        stateElapsed: Float,
        performanceLite: Boolean,
        richEffects: Boolean,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val spin = stateElapsed * (if (entry) 92f else -72f) + portal.phase * 45f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 140)
        if (richEffects) {
            paint.maskFilter = BlurMaskFilter(radius * 0.62f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(cx, cy, radius * 1.42f, paint)
        paint.maskFilter = null

        val assetRadius = radius * 1.05f
        scratchRect.set(cx - assetRadius, cy - assetRadius, cx + assetRadius, cy + assetRadius)
        canvas.save()
        canvas.rotate(spin, cx, cy)
        drawWorldAsset(canvas, "portal_goal", scratchRect, 245)
        canvas.restore()

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val rings = when {
            performanceLite -> 0
            richEffects -> 3
            else -> 1
        }
        repeat(rings) { ring ->
            val progress = ((stateElapsed * (0.7f + ring * 0.09f) + ring * 0.31f + portal.phase) % 1f + 1f) % 1f
            paint.strokeWidth = (2.4f - ring * 0.25f) * dp
            paint.color = withAlpha(accent, ((1f - progress) * 190f).roundToInt())
            canvas.drawCircle(cx, cy, radius * (0.62f + progress * 0.88f), paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, 245)
        canvas.drawText(label, cx, cy + radius + 18f * dp, textPaint)
    }

    fun drawBlocks(
        canvas: Canvas,
        blocks: List<Block>,
        accent: Int,
        isChaos: Boolean,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        for (block in blocks) {
            val cx = sx(block.center.x)
            val cy = sy(block.center.y)
            val hw = worldToScreen(block.width * 0.5f)
            val hh = worldToScreen(block.height * 0.5f)
            canvas.save()
            canvas.rotate((block.angleRadians * 180f / PI.toFloat()), cx, cy)
            scratchRect.set(cx - hw, cy - hh, cx + hw, cy + hh)
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, 46)
            canvas.drawRoundRect(cx - hw * 1.03f, cy - hh * 1.45f, cx + hw * 1.03f, cy + hh * 1.45f, 5f * dp, 5f * dp, paint)
            drawWorldAsset(canvas, if (isChaos) "platform_chaos" else "platform_classic", scratchRect, 255)
            paint.color = withAlpha(block.tone, 34)
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = 0x44FFFFFF
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            canvas.restore()
        }
    }

    fun drawGoal(
        canvas: Canvas,
        goalX: Float,
        goalY: Float,
        goalRadius: Float,
        stateElapsed: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val cx = sx(goalX)
        val cy = sy(goalY)
        val radius = worldToScreen(goalRadius)
        val pulse = 0.9f + 0.1f * sin(stateElapsed * 5.2f)

        val portalRadius = radius * (1.12f + pulse * 0.06f)
        scratchRect.set(cx - portalRadius, cy - portalRadius, cx + portalRadius, cy + portalRadius)
        drawWorldAsset(canvas, "portal_goal", scratchRect, 255)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f * dp
        paint.color = 0xAA64E572.toInt()
        canvas.drawCircle(cx, cy, radius * pulse, paint)
        paint.strokeWidth = 1.6f * dp
        paint.color = 0xCCFFFFFF.toInt()
        canvas.drawCircle(cx, cy, radius * 0.56f, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0x2264E572
        canvas.drawCircle(cx, cy, radius * 1.4f, paint)
    }

    fun drawHazards(
        canvas: Canvas,
        hazards: List<Hazard>,
        hazardTime: Float,
        stateElapsed: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        richEffects: Boolean,
        performanceLite: Boolean,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        for (hazard in hazards) {
            drawHazardTrack(canvas, hazard, sx, sy, worldToScreen, performanceLite, paint, dp)
            val position = hazard.positionAt(hazardTime)
            val cx = sx(position.x)
            val cy = sy(position.y)
            val r = worldToScreen(hazard.radius)
            paint.style = Paint.Style.FILL
            paint.color = 0x88FF4D8D.toInt()
            if (richEffects) {
                paint.maskFilter = BlurMaskFilter(r * 0.6f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(cx, cy, r * 1.18f, paint)
            paint.maskFilter = null
            val hazardKey = when (hazard.motion) {
                HazardMotion.STATIC -> "hazard_static"
                HazardMotion.HORIZONTAL, HazardMotion.VERTICAL -> "hazard_glitch"
                HazardMotion.ORBIT, HazardMotion.FIGURE_EIGHT -> "hazard_void"
            }
            val assetRadius = r * 1.3f
            scratchRect.set(cx - assetRadius, cy - assetRadius, cx + assetRadius, cy + assetRadius)
            val saveCount = canvas.save()
            canvas.rotate(stateElapsed * if (hazard.isMoving) 42f else 24f, cx, cy)
            drawWorldAsset(canvas, hazardKey, scratchRect, 255)
            canvas.restoreToCount(saveCount)
        }
    }

    fun drawHazardTrack(
        canvas: Canvas,
        hazard: Hazard,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        performanceLite: Boolean,
        paint: Paint,
        dp: Float
    ) {
        if (!hazard.isMoving) return
        val cx = sx(hazard.center.x)
        val cy = sy(hazard.center.y)
        val travel = worldToScreen(hazard.travel)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0x55FF4D8D
        when (hazard.motion) {
            HazardMotion.HORIZONTAL -> canvas.drawLine(cx - travel, cy, cx + travel, cy, paint)
            HazardMotion.VERTICAL -> canvas.drawLine(cx, cy - travel, cx, cy + travel, paint)
            HazardMotion.ORBIT -> canvas.drawCircle(cx, cy, travel, paint)
            HazardMotion.FIGURE_EIGHT -> {
                tempPath.reset()
                val segments = if (performanceLite) 16 else 32
                repeat(segments + 1) { i ->
                    val t = i * PI.toFloat() * 2f / segments
                    val x = cx + sin(t) * travel
                    val y = cy + sin(t * 2f) * travel * 0.5f
                    if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                }
                canvas.drawPath(tempPath, paint)
            }
            HazardMotion.STATIC -> Unit
        }
        paint.style = Paint.Style.FILL
        paint.color = 0x99FF4D8D.toInt()
        canvas.drawCircle(cx, cy, 2.2f * dp, paint)
    }

    fun drawWorldEntities(
        canvas: Canvas,
        level: com.moonsolstudios.kavvoro.engine.LevelSpec,
        isChaos: Boolean,
        isReady: Boolean,
        stateElapsed: Float,
        simElapsed: Float,
        richEffects: Boolean,
        performanceLite: Boolean,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        drawPortals(canvas, level.portals, sx, sy, worldToScreen, stateElapsed, performanceLite, richEffects, paint, dp, drawWorldAsset)
        drawBlocks(canvas, level.blocks, level.accent, isChaos, sx, sy, worldToScreen, paint, dp, drawWorldAsset)
        drawGoal(canvas, level.goal.x, level.goal.y, level.goalRadius, stateElapsed, sx, sy, worldToScreen, paint, dp, drawWorldAsset)
        drawHazards(canvas, level.hazards, if (isReady) 0f else simElapsed, stateElapsed, sx, sy, worldToScreen, richEffects, performanceLite, paint, dp, drawWorldAsset)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
