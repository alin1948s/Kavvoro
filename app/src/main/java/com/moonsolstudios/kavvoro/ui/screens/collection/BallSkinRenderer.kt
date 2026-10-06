package com.moonsolstudios.kavvoro.ui.screens.collection

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Procedural renderer for Brainball skins, aura rings, style motifs, and lock treatments.
 */
object BallSkinRenderer {
    private val scratchRect = RectF()
    private val clipPath = Path()

    fun drawLock(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val lockWidth = radius * 0.62f
        val lockHeight = radius * 0.52f
        val lockTop = cy - lockHeight * 0.15f
        paint.style = Paint.Style.FILL
        paint.color = 0xD0141923.toInt()
        scratchRect.set(
            cx - lockWidth * 0.5f,
            lockTop,
            cx + lockWidth * 0.5f,
            lockTop + lockHeight
        )
        canvas.drawRoundRect(scratchRect, radius * 0.14f, radius * 0.14f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.09f
        paint.color = 0xFFF7F4FF.toInt()
        canvas.drawRoundRect(scratchRect, radius * 0.14f, radius * 0.14f, paint)

        val shackleRadius = lockWidth * 0.32f
        scratchRect.set(
            cx - shackleRadius,
            lockTop - shackleRadius * 1.55f,
            cx + shackleRadius,
            lockTop + shackleRadius * 0.45f
        )
        canvas.drawArc(scratchRect, 180f, 180f, false, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, lockTop + lockHeight * 0.45f, radius * 0.08f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.06f
        canvas.drawLine(
            cx,
            lockTop + lockHeight * 0.45f,
            cx,
            lockTop + lockHeight * 0.72f,
            paint
        )
    }

    fun drawBrainballLock(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val lockCx = cx + radius * 0.65f
        val lockCy = cy + radius * 0.65f
        val lockRad = radius * 0.28f
        paint.style = Paint.Style.FILL
        paint.color = 0xEE070B12.toInt()
        canvas.drawCircle(lockCx, lockCy, lockRad, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.075f
        paint.color = 0xFFFF4D8D.toInt()
        canvas.drawCircle(lockCx, lockCy, lockRad, paint)
        scratchRect.set(lockCx - lockRad * 0.5f, lockCy - lockRad * 0.05f, lockCx + lockRad * 0.5f, lockCy + lockRad * 0.6f)
        canvas.drawRoundRect(scratchRect, lockRad * 0.2f, lockRad * 0.2f, paint)
        canvas.drawArc(
            lockCx - lockRad * 0.35f,
            lockCy - lockRad * 0.6f,
            lockCx + lockRad * 0.35f,
            lockCy + lockRad * 0.25f,
            205f,
            130f,
            false,
            paint
        )
    }

    fun drawBallSkin(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        animated: Boolean,
        locked: Boolean,
        menuPulse: Float,
        richEffects: Boolean,
        adaptiveQuality: Float,
        ballSkins: List<BallSkin>,
        brainballBitmap: (BallSkin) -> Bitmap?,
        paint: Paint
    ) {
        val wave = if (animated) sin(menuPulse * 3f) else 0f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(skin.lineColor, if (locked) 55 else 105)
        if (richEffects) {
            paint.maskFilter = com.moonsolstudios.kavvoro.ui.render.AssetResourceManager.cachedNormalBlur(radius * 0.42f)
        }
        canvas.drawCircle(cx, cy, radius * (1.55f + wave * 0.06f), paint)
        paint.maskFilter = null

        val skinIndex = com.moonsolstudios.kavvoro.repository.BallSkinCatalog.indexOf(skin.id)
        val totalSkins = if (ballSkins.isNotEmpty()) ballSkins.size else com.moonsolstudios.kavvoro.repository.BallSkinCatalog.ALL_SKINS.size
        val effectTier = when {
            skin.unlock.type == UnlockType.PREMIUM || skinIndex >= totalSkins - 4 -> 3
            skinIndex >= 37 -> 2
            skinIndex >= 28 -> 1
            else -> 0
        }
        val renderTier = if (richEffects) effectTier else min(effectTier, 1)
        if (animated && !locked && renderTier > 0 && adaptiveQuality >= 0.62f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = radius * 0.045f
            repeat(renderTier) { ring ->
                paint.color = withAlpha(if (ring % 2 == 0) skin.lineColor else skin.secondary, 95 - ring * 18)
                val orbitRadius = radius * (1.08f + ring * 0.13f + wave * 0.018f)
                canvas.drawCircle(cx, cy, orbitRadius, paint)
            }
            paint.style = Paint.Style.FILL
            val particleCount = renderTier + 1
            repeat(particleCount) { particle ->
                val angle = menuPulse * (1.35f + particle * 0.11f) + particle * PI.toFloat() * 2f / particleCount
                val orbitRadius = radius * (1.14f + (particle % renderTier) * 0.13f)
                paint.color = withAlpha(if (particle % 2 == 0) skin.lineColor else skin.secondary, 220)
                canvas.drawCircle(
                    cx + cos(angle) * orbitRadius,
                    cy + sin(angle) * orbitRadius,
                    radius * (0.055f + effectTier * 0.012f),
                    paint
                )
            }
        }

        val art = brainballBitmap(skin)
        if (art != null) {
            val artRadius = radius * 1.1f
            val saveCount = canvas.save()
            clipPath.reset()
            clipPath.addCircle(cx, cy, artRadius * 0.985f, Path.Direction.CW)
            canvas.clipPath(clipPath)
            paint.style = Paint.Style.FILL
            paint.alpha = if (locked) 205 else 255
            paint.isFilterBitmap = true
            scratchRect.set(cx - artRadius, cy - artRadius, cx + artRadius, cy + artRadius)
            canvas.drawBitmap(art, null, scratchRect, paint)
            paint.alpha = 255
            if (locked) {
                paint.color = 0x44232936
                canvas.drawCircle(cx, cy, radius, paint)
            }
            canvas.restoreToCount(saveCount)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = radius * 0.075f
            paint.color = if (locked) 0x9999A1B5.toInt() else withAlpha(skin.lineColor, 240)
            canvas.drawCircle(cx, cy, radius * 0.96f, paint)
            if (locked) drawBrainballLock(canvas, cx, cy, radius, paint)
            return
        }

        paint.style = Paint.Style.FILL
        paint.color = if (locked) 0xFF333947.toInt() else skin.primary
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.08f
        paint.color = if (locked) 0x7799A1B5 else withAlpha(skin.secondary, 230)
        canvas.drawCircle(cx, cy, radius * 0.94f, paint)

        when (skin.style) {
            SkinStyle.PRISM -> {
                paint.style = Paint.Style.FILL
                clipPath.reset()
                clipPath.moveTo(cx, cy - radius * 0.9f)
                clipPath.lineTo(cx - radius * 0.78f, cy + radius * 0.24f)
                clipPath.lineTo(cx, cy + radius * 0.86f)
                clipPath.lineTo(cx + radius * 0.78f, cy + radius * 0.24f)
                clipPath.close()
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 210)
                canvas.drawPath(clipPath, paint)
                paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 45 else 150)
                canvas.drawCircle(cx - radius * 0.24f, cy - radius * 0.22f, radius * 0.18f, paint)
            }

            SkinStyle.VOID -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.14f
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 230)
                canvas.drawCircle(cx, cy, radius * 0.58f, paint)
                paint.strokeWidth = radius * 0.07f
                paint.color = withAlpha(skin.lineColor, if (locked) 70 else 200)
                scratchRect.set(cx - radius * 0.9f, cy - radius * 0.34f, cx + radius * 0.9f, cy + radius * 0.34f)
                canvas.drawArc(scratchRect, 12f + wave * 10f, 220f, false, paint)
            }

            SkinStyle.CHROME -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 55 else 185)
                scratchRect.set(cx - radius * 0.55f, cy - radius * 0.72f, cx + radius * 0.2f, cy - radius * 0.18f)
                canvas.drawOval(scratchRect, paint)
                paint.color = withAlpha(skin.secondary, if (locked) 55 else 165)
                canvas.drawRect(cx - radius * 0.86f, cy + radius * 0.02f, cx + radius * 0.86f, cy + radius * 0.2f, paint)
            }

            SkinStyle.PLASMA -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.13f
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 230)
                repeat(5) { i ->
                    val angle = i * PI.toFloat() * 2f / 5f + wave * 0.2f
                    canvas.drawLine(cx, cy, cx + cos(angle) * radius * 0.82f, cy + sin(angle) * radius * 0.82f, paint)
                }
                paint.strokeCap = Paint.Cap.BUTT
            }

            SkinStyle.BLOP -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 90 else 235)
                canvas.drawOval(cx - radius * 0.62f, cy - radius * 0.48f, cx + radius * 0.2f, cy + radius * 0.25f, paint)
            }

            SkinStyle.GLITCH -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 210)
                canvas.drawRect(cx - radius * 0.9f, cy - radius * 0.34f, cx + radius * 0.2f, cy - radius * 0.12f, paint)
                paint.color = withAlpha(skin.lineColor, if (locked) 60 else 190)
                canvas.drawRect(cx - radius * 0.2f, cy + radius * 0.28f, cx + radius * 0.9f, cy + radius * 0.48f, paint)
            }

            SkinStyle.ZAP -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 80 else 235)
                clipPath.reset()
                clipPath.moveTo(cx + radius * 0.08f, cy - radius * 0.82f)
                clipPath.lineTo(cx - radius * 0.36f, cy + radius * 0.02f)
                clipPath.lineTo(cx + radius * 0.02f, cy + radius * 0.02f)
                clipPath.lineTo(cx - radius * 0.12f, cy + radius * 0.82f)
                clipPath.lineTo(cx + radius * 0.45f, cy - radius * 0.18f)
                clipPath.lineTo(cx + radius * 0.06f, cy - radius * 0.18f)
                clipPath.close()
                canvas.drawPath(clipPath, paint)
            }

            SkinStyle.LOOP -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.12f
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 220)
                scratchRect.set(cx - radius * 0.7f, cy - radius * 0.35f, cx, cy + radius * 0.35f)
                canvas.drawOval(scratchRect, paint)
                scratchRect.set(cx, cy - radius * 0.35f, cx + radius * 0.7f, cy + radius * 0.35f)
                canvas.drawOval(scratchRect, paint)
            }

            SkinStyle.STATIC -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.09f
                paint.color = withAlpha(skin.secondary, if (locked) 80 else 220)
                repeat(4) { i ->
                    val y = cy - radius * 0.48f + i * radius * 0.3f
                    canvas.drawLine(cx - radius * 0.68f, y, cx + radius * 0.68f, y + sin(menuPulse * 6f + i) * radius * 0.08f, paint)
                }
            }

            SkinStyle.RIFT -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                clipPath.reset()
                clipPath.moveTo(cx - radius * 0.18f, cy - radius * 0.86f)
                clipPath.lineTo(cx + radius * 0.18f, cy - radius * 0.12f)
                clipPath.lineTo(cx - radius * 0.08f, cy + radius * 0.12f)
                clipPath.lineTo(cx + radius * 0.24f, cy + radius * 0.86f)
                clipPath.lineTo(cx - radius * 0.42f, cy + radius * 0.16f)
                clipPath.lineTo(cx - radius * 0.12f, cy - radius * 0.1f)
                clipPath.close()
                canvas.drawPath(clipPath, paint)
            }

            SkinStyle.BYTE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.1f
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                scratchRect.set(cx - radius * 0.58f, cy - radius * 0.58f, cx + radius * 0.58f, cy + radius * 0.58f)
                canvas.drawRoundRect(scratchRect, radius * 0.16f, radius * 0.16f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawRect(cx - radius * 0.28f, cy - radius * 0.1f, cx + radius * 0.28f, cy + radius * 0.1f, paint)
            }

            SkinStyle.WOBBLE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.1f
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                clipPath.reset()
                clipPath.moveTo(cx - radius * 0.72f, cy)
                repeat(6) { i ->
                    val x = cx - radius * 0.72f + i * radius * 0.29f
                    val y = cy + sin(menuPulse * 2.8f + i) * radius * 0.28f
                    clipPath.lineTo(x, y)
                }
                canvas.drawPath(clipPath, paint)
            }

            SkinStyle.CROWN -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 230)
                clipPath.reset()
                clipPath.moveTo(cx - radius * 0.62f, cy - radius * 0.18f)
                clipPath.lineTo(cx - radius * 0.36f, cy - radius * 0.78f)
                clipPath.lineTo(cx, cy - radius * 0.24f)
                clipPath.lineTo(cx + radius * 0.36f, cy - radius * 0.78f)
                clipPath.lineTo(cx + radius * 0.62f, cy - radius * 0.18f)
                clipPath.lineTo(cx + radius * 0.52f, cy + radius * 0.14f)
                clipPath.lineTo(cx - radius * 0.52f, cy + radius * 0.14f)
                clipPath.close()
                canvas.drawPath(clipPath, paint)
            }

            SkinStyle.CLASSIC -> Unit
        }

        paint.style = Paint.Style.FILL
        paint.color = if (locked) 0x99232936.toInt() else 0xFF07090F.toInt()
        when (skin.style) {
            SkinStyle.VOID -> {
                canvas.drawCircle(cx - radius * 0.28f, cy - radius * 0.16f, radius * 0.1f, paint)
                canvas.drawCircle(cx + radius * 0.28f, cy - radius * 0.16f, radius * 0.1f, paint)
            }

            SkinStyle.GLITCH, SkinStyle.STATIC -> {
                canvas.drawRect(cx - radius * 0.45f, cy - radius * 0.25f, cx - radius * 0.12f, cy - radius * 0.06f, paint)
                canvas.drawRect(cx + radius * 0.14f, cy - radius * 0.22f, cx + radius * 0.48f, cy - radius * 0.02f, paint)
            }

            else -> {
                canvas.drawCircle(cx - radius * 0.32f, cy - radius * 0.18f, radius * 0.13f, paint)
                canvas.drawCircle(cx + radius * 0.32f, cy - radius * 0.18f, radius * 0.13f, paint)
            }
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.08f
        scratchRect.set(cx - radius * 0.42f, cy + radius * 0.02f, cx + radius * 0.42f, cy + radius * 0.42f)
        canvas.drawArc(scratchRect, 18f, 144f, false, paint)

        if (locked) {
            drawBrainballLock(canvas, cx, cy, radius, paint)
        }
    }
}
