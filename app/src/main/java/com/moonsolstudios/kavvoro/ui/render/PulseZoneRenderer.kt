package com.moonsolstudios.kavvoro.ui.render

import android.graphics.*
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.PulseZone
import kotlin.math.*

object PulseZoneRenderer {

    private val scratch = RectF()
    private val arrowPath = Path()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    fun drawPulseZones(
        canvas: Canvas,
        pulseZones: List<PulseZone>,
        accent: Int,
        stateElapsed: Float,
        isSimulating: Boolean,
        ballDistanceTo: (Point2) -> Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        richEffects: Boolean,
        performanceLite: Boolean,
        fullEffects: Boolean,
        levelIndex: Int,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        pulseZones.forEachIndexed { index, zone ->
            val cx = sx(zone.center.x)
            val cy = sy(zone.center.y)
            val radius = worldToScreen(zone.radius)
            val wave = 0.45f + 0.55f * sin((stateElapsed * 2.6f + zone.phase + index).toDouble()).toFloat()
            val ballInside = isSimulating && ballDistanceTo(zone.center) < zone.radius
            val heat = if (ballInside) 1f else (0.35f + wave * 0.38f)

            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, (54f + heat * 78f).roundToInt())
            if (richEffects) {
                paint.maskFilter = BlurMaskFilter(radius * 0.34f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(cx, cy, radius * (1.02f + wave * 0.1f), paint)
            paint.maskFilter = null

            paint.style = Paint.Style.FILL
            if (richEffects) {
                paint.shader = LinearGradient(
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    intArrayOf(
                        withAlpha(accent, if (ballInside) 92 else 64),
                        withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), if (ballInside) 116 else 72),
                        0x00000000
                    ),
                    floatArrayOf(0f, 0.62f, 1f),
                    Shader.TileMode.CLAMP
                )
            } else {
                paint.shader = null
                paint.color = withAlpha(accent, if (ballInside) 78 else 46)
            }
            canvas.drawCircle(cx, cy, radius, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = (if (ballInside) 3.6f else 2.2f) * dp
            paint.color = withAlpha(if (ballInside) 0xFFFFFFFF.toInt() else accent, if (ballInside) 245 else 185)
            canvas.drawCircle(cx, cy, radius, paint)

            drawPulseIndicator(
                canvas = canvas,
                zone = zone,
                cx = cx,
                cy = cy,
                radius = radius,
                wave = wave,
                richEffects = richEffects,
                performanceLite = performanceLite,
                stateElapsed = stateElapsed,
                accent = accent,
                paint = paint,
                dp = dp
            )

            if (levelIndex <= 10) {
                val iconSize = (if (ballInside) 26f else 22f) * dp
                val assetKey = if (zone.radialForce >= 0f) "hazard_boost" else "hazard_pulse"
                scratch.set(cx - iconSize * 0.5f, cy - radius - iconSize - 2f * dp, cx + iconSize * 0.5f, cy - radius - 2f * dp)
                drawWorldAsset(canvas, assetKey, scratch, if (ballInside) 255 else 190)

                textPaint.reset()
                textPaint.isAntiAlias = true
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
                textPaint.textSize = (if (ballInside) 10f else 8f) * dp
                textPaint.color = withAlpha(if (zone.radialForce >= 0f) 0xFFFFCF4A.toInt() else 0xFFC15CFF.toInt(), if (ballInside) 245 else 170)
                canvas.drawText(t(if (zone.radialForce >= 0f) "BOOST FIELD" else "VORTEX FIELD").uppercase(), cx, cy + radius + 18f * dp, textPaint)
            }
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    private fun drawPulseIndicator(
        canvas: Canvas,
        zone: PulseZone,
        cx: Float,
        cy: Float,
        radius: Float,
        wave: Float,
        richEffects: Boolean,
        performanceLite: Boolean,
        stateElapsed: Float,
        accent: Int,
        paint: Paint,
        dp: Float
    ) {
        val sign = if (zone.radialForce >= 0f) 1f else -1f
        val baseAngle = stateElapsed * 1.35f + zone.phase
        val arrowDistance = radius * (0.42f + wave * 0.13f)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 3.4f * dp
        paint.color = 0xF2FFFFFF.toInt()
        val arrowCount = when {
            performanceLite -> 2
            richEffects -> 5
            else -> 3
        }
        repeat(arrowCount) { i ->
            val angle = (baseAngle + i * Math.PI.toFloat() * 2f / arrowCount)
            val inner = arrowDistance * if (sign > 0f) 0.55f else 1.0f
            val outer = arrowDistance * if (sign > 0f) 1.12f else 0.42f
            val x1 = (cx + cos(angle.toDouble()) * inner).toFloat()
            val y1 = (cy + sin(angle.toDouble()) * inner).toFloat()
            val x2 = (cx + cos(angle.toDouble()) * outer).toFloat()
            val y2 = (cy + sin(angle.toDouble()) * outer).toFloat()
            paint.strokeWidth = 7f * dp
            paint.color = withAlpha(accent, 54)
            canvas.drawLine(x1, y1, x2, y2, paint)
            paint.strokeWidth = 3.2f * dp
            paint.color = 0xF2FFFFFF.toInt()
            canvas.drawLine(x1, y1, x2, y2, paint)
            drawArrowHead(canvas, x2, y2, angle, paint, dp)
        }

        if (richEffects && abs(zone.swirlForce) > 0.4f) {
            paint.strokeWidth = 4.2f * dp
            paint.color = withAlpha(accent, 235)
            val arcRadius = radius * 0.55f
            scratch.set(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius)
            val sweep = if (zone.swirlForce > 0f) 142f else -142f
            val start = (stateElapsed * 92f + zone.phase * 30f) % 360f
            canvas.drawArc(scratch, start, sweep, false, paint)
            val endAngle = ((start + sweep) * Math.PI.toFloat() / 180f)
            val angleOffset = if (sweep > 0f) Math.PI.toFloat() * 0.52f else -Math.PI.toFloat() * 0.52f
            drawArrowHead(
                canvas,
                (cx + cos(endAngle.toDouble()) * arcRadius).toFloat(),
                (cy + sin(endAngle.toDouble()) * arcRadius).toFloat(),
                endAngle + angleOffset,
                paint,
                dp
            )
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawArrowHead(canvas: Canvas, x: Float, y: Float, angle: Float, paint: Paint, dp: Float) {
        val size = 6f * dp
        paint.style = Paint.Style.FILL
        arrowPath.reset()
        arrowPath.moveTo((x + cos(angle.toDouble()) * size).toFloat(), (y + sin(angle.toDouble()) * size).toFloat())
        arrowPath.lineTo((x + cos((angle + 2.45f).toDouble()) * size).toFloat(), (y + sin((angle + 2.45f).toDouble()) * size).toFloat())
        arrowPath.lineTo((x + cos((angle - 2.45f).toDouble()) * size).toFloat(), (y + sin((angle - 2.45f).toDouble()) * size).toFloat())
        arrowPath.close()
        canvas.drawPath(arrowPath, paint)
        paint.style = Paint.Style.STROKE
    }
}
