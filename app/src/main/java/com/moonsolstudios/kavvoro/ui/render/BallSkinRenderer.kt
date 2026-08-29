package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Procedural and bitmap renderer for Brainball skins, auras, trails, power badges, and lock states.
 */
object BallSkinRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawSkin(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        skinIndex: Int,
        totalSkins: Int,
        animated: Boolean,
        locked: Boolean,
        pulse: Float,
        richEffects: Boolean,
        adaptiveQuality: Float,
        artBitmap: Bitmap?,
        paint: Paint
    ) {
        val wave = if (animated) sin(pulse * 3f) else 0f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(skin.lineColor, if (locked) 55 else 105)
        if (richEffects) {
            paint.maskFilter = BlurMaskFilter(radius * 0.42f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(cx, cy, radius * (1.55f + wave * 0.06f), paint)
        paint.maskFilter = null

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
                val angle = pulse * (1.35f + particle * 0.11f) + particle * PI.toFloat() * 2f / particleCount
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

        if (artBitmap != null && !artBitmap.isRecycled) {
            val artRadius = radius * 1.1f
            val saveCount = canvas.save()
            tempPath.reset()
            tempPath.addCircle(cx, cy, artRadius * 0.985f, Path.Direction.CW)
            canvas.clipPath(tempPath)
            paint.style = Paint.Style.FILL
            paint.alpha = if (locked) 205 else 255
            paint.isFilterBitmap = true
            scratchRect.set(cx - artRadius, cy - artRadius, cx + artRadius, cy + artRadius)
            canvas.drawBitmap(artBitmap, null, scratchRect, paint)
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
            if (locked) drawLock(canvas, cx, cy, radius, paint)
            return
        }

        // ── Procedural Fallback ──
        drawProceduralFallback(canvas, cx, cy, radius, skin, wave, pulse, locked, paint)
    }

    private fun drawProceduralFallback(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        wave: Float,
        pulse: Float,
        locked: Boolean,
        paint: Paint
    ) {
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
                tempPath.reset()
                tempPath.moveTo(cx, cy - radius * 0.9f)
                tempPath.lineTo(cx - radius * 0.78f, cy + radius * 0.24f)
                tempPath.lineTo(cx, cy + radius * 0.86f)
                tempPath.lineTo(cx + radius * 0.78f, cy + radius * 0.24f)
                tempPath.close()
                paint.color = withAlpha(skin.secondary, if (locked) 70 else 210)
                canvas.drawPath(tempPath, paint)
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
                scratchRect.set(cx - radius * 0.62f, cy - radius * 0.48f, cx + radius * 0.2f, cy + radius * 0.25f)
                canvas.drawOval(scratchRect, paint)
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
                tempPath.reset()
                tempPath.moveTo(cx + radius * 0.08f, cy - radius * 0.82f)
                tempPath.lineTo(cx - radius * 0.36f, cy + radius * 0.02f)
                tempPath.lineTo(cx + radius * 0.02f, cy + radius * 0.02f)
                tempPath.lineTo(cx - radius * 0.12f, cy + radius * 0.82f)
                tempPath.lineTo(cx + radius * 0.45f, cy - radius * 0.18f)
                tempPath.lineTo(cx + radius * 0.06f, cy - radius * 0.18f)
                tempPath.close()
                canvas.drawPath(tempPath, paint)
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
                    canvas.drawLine(cx - radius * 0.68f, y, cx + radius * 0.68f, y + sin(pulse * 6f + i) * radius * 0.08f, paint)
                }
            }
            SkinStyle.RIFT -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                tempPath.reset()
                tempPath.moveTo(cx - radius * 0.18f, cy - radius * 0.86f)
                tempPath.lineTo(cx + radius * 0.18f, cy - radius * 0.12f)
                tempPath.lineTo(cx - radius * 0.08f, cy + radius * 0.12f)
                tempPath.lineTo(cx + radius * 0.24f, cy + radius * 0.86f)
                tempPath.lineTo(cx - radius * 0.2f, cy + radius * 0.16f)
                tempPath.lineTo(cx + radius * 0.04f, cy - radius * 0.12f)
                tempPath.close()
                canvas.drawPath(tempPath, paint)
            }
            SkinStyle.BYTE -> {
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(skin.secondary, if (locked) 75 else 220)
                val block = radius * 0.32f
                canvas.drawRect(cx - block * 1.5f, cy - block * 1.5f, cx - block * 0.5f, cy - block * 0.5f, paint)
                canvas.drawRect(cx + block * 0.5f, cy - block * 1.5f, cx + block * 1.5f, cy - block * 0.5f, paint)
                canvas.drawRect(cx - block * 0.5f, cy - block * 0.5f, cx + block * 0.5f, cy + block * 0.5f, paint)
                canvas.drawRect(cx - block * 1.5f, cy + block * 0.5f, cx + block * 1.5f, cy + block * 1.5f, paint)
            }
            SkinStyle.WOBBLE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.1f
                paint.color = withAlpha(skin.secondary, if (locked) 80 else 220)
                scratchRect.set(cx - radius * 0.72f, cy - radius * 0.62f, cx + radius * 0.72f, cy + radius * 0.62f)
                canvas.drawArc(scratchRect, 20f + wave * 18f, 150f, false, paint)
                scratchRect.set(cx - radius * 0.52f, cy - radius * 0.38f, cx + radius * 0.52f, cy + radius * 0.38f)
                canvas.drawArc(scratchRect, 200f - wave * 18f, 130f, false, paint)
            }
            else -> {
                // CLASSIC / CROWN
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 70 else 220)
                canvas.drawCircle(cx - radius * 0.28f, cy - radius * 0.18f, radius * 0.22f, paint)
                canvas.drawCircle(cx + radius * 0.28f, cy - radius * 0.18f, radius * 0.22f, paint)
                paint.color = 0xFF000000.toInt()
                canvas.drawCircle(cx - radius * 0.25f, cy - radius * 0.18f, radius * 0.11f, paint)
                canvas.drawCircle(cx + radius * 0.31f, cy - radius * 0.18f, radius * 0.11f, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = radius * 0.08f
                paint.color = withAlpha(skin.secondary, if (locked) 80 else 220)
                scratchRect.set(cx - radius * 0.42f, cy - radius * 0.05f, cx + radius * 0.42f, cy + radius * 0.52f)
                canvas.drawArc(scratchRect, 25f, 130f, false, paint)
            }
        }

        if (locked) drawLock(canvas, cx, cy, radius, paint)
    }

    fun drawLock(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val lockW = radius * 0.62f
        val lockH = radius * 0.52f
        val lockTop = cy - lockH * 0.15f
        paint.style = Paint.Style.FILL
        paint.color = 0xD0141923.toInt()
        scratchRect.set(cx - lockW * 0.5f, lockTop, cx + lockW * 0.5f, lockTop + lockH)
        canvas.drawRoundRect(scratchRect, radius * 0.14f, radius * 0.14f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.09f
        paint.color = 0xFFF7F4FF.toInt()
        canvas.drawRoundRect(scratchRect, radius * 0.14f, radius * 0.14f, paint)

        val shackleR = lockW * 0.32f
        scratchRect.set(cx - shackleR, lockTop - shackleR * 1.55f, cx + shackleR, lockTop + shackleR * 0.45f)
        canvas.drawArc(scratchRect, 180f, 180f, false, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, lockTop + lockH * 0.45f, radius * 0.08f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.06f
        canvas.drawLine(cx, lockTop + lockH * 0.45f, cx, lockTop + lockH * 0.72f, paint)
    }

    fun drawTrail(
        canvas: Canvas,
        trail: List<Point2>,
        skin: BallSkin,
        radius: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        paint: Paint
    ) {
        if (trail.size < 2) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        val startIndex = (trail.size - 18).coerceAtLeast(0)
        for (index in startIndex until trail.lastIndex) {
            val a = trail[index]
            val b = trail[index + 1]
            val progress = (index - startIndex + 1).toFloat() / (trail.size - startIndex).coerceAtLeast(1)
            val powerAlpha = if (skin.power == BallPower.NONE) 0.72f else 1f
            paint.strokeWidth = radius * (0.12f + progress * 0.32f)
            paint.color = withAlpha(
                if (index % 2 == 0) skin.lineColor else skin.secondary,
                (progress * 145f * powerAlpha).roundToInt()
            )
            canvas.drawLine(sx(a.x), sy(a.y), sx(b.x), sy(b.y), paint)
        }
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawAura(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        skinIndex: Int,
        pulse: Float,
        richEffects: Boolean,
        performanceLite: Boolean,
        adaptiveQuality: Float,
        paint: Paint
    ) {
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val powered = skin.power != BallPower.NONE
        val late = skinIndex >= 37
        val baseOrbitCount = when {
            premium -> 4
            powered || late -> 3
            else -> 1
        }
        val orbitCount = if (richEffects) baseOrbitCount else min(baseOrbitCount, 1)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(skin.lineColor, if (premium || powered) 150 else 92)
        if (richEffects) {
            paint.maskFilter = BlurMaskFilter(radius * 0.82f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(cx, cy, radius * if (premium) 1.82f else 1.58f, paint)
        paint.maskFilter = null

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(orbitCount) { ring ->
            val orbit = pulse * (1.25f + ring * 0.18f) + ring * PI.toFloat() * 0.42f
            val rx = radius * (1.15f + ring * 0.16f)
            val ry = radius * (0.72f + ring * 0.1f)
            paint.strokeWidth = radius * (0.045f + ring * 0.008f)
            paint.color = withAlpha(if (ring % 2 == 0) skin.secondary else skin.lineColor, 150 - ring * 20)
            scratchRect.set(cx - rx, cy - ry, cx + rx, cy + ry)
            canvas.save()
            canvas.rotate((orbit * 180f / PI.toFloat()) % 360f, cx, cy)
            canvas.drawOval(scratchRect, paint)
            canvas.restore()
        }
        paint.strokeCap = Paint.Cap.BUTT

        if ((powered || premium || late) && !performanceLite && adaptiveQuality >= 0.58f) {
            paint.style = Paint.Style.FILL
            val particles = when {
                richEffects && premium -> 8
                richEffects -> 5
                else -> 3
            }
            repeat(particles) { index ->
                val angle = pulse * (2.2f + index * 0.06f) + index * PI.toFloat() * 2f / particles
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

    fun drawPowerBadge(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        pulse: Float,
        paint: Paint
    ) {
        if (skin.power == BallPower.NONE) return
        val badge = radius * 0.58f
        val angle = pulse * 2.35f
        val bx = cx + cos(angle) * radius * 1.18f
        val by = cy + sin(angle) * radius * 1.18f
        scratchRect.set(bx - badge * 0.5f, by - badge * 0.5f, bx + badge * 0.5f, by + badge * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 195)
        canvas.drawRoundRect(scratchRect, badge * 0.35f, badge * 0.35f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = badge * 0.12f
        paint.color = 0xFFFFCF4A.toInt()
        canvas.drawRoundRect(scratchRect, badge * 0.35f, badge * 0.35f, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFCF4A.toInt()
        tempPath.reset()
        tempPath.moveTo(bx + badge * 0.06f, by - badge * 0.32f)
        tempPath.lineTo(bx - badge * 0.22f, by + badge * 0.02f)
        tempPath.lineTo(bx + badge * 0.02f, by + badge * 0.02f)
        tempPath.lineTo(bx - badge * 0.08f, by + badge * 0.32f)
        tempPath.lineTo(bx + badge * 0.24f, by - badge * 0.04f)
        tempPath.lineTo(bx + badge * 0.04f, by - badge * 0.04f)
        tempPath.close()
        canvas.drawPath(tempPath, paint)
    }

    fun gameplayBallScale(skin: BallSkin, skinIndex: Int): Float = when {
        skin.unlock.type == UnlockType.PREMIUM -> 1.34f
        skin.power != BallPower.NONE -> 1.29f
        skinIndex >= 37 -> 1.25f
        else -> 1.2f
    }

    fun drawReplayTail(
        canvas: Canvas,
        replayFrames: List<com.moonsolstudios.kavvoro.engine.PhysicsFrame>,
        isSimulating: Boolean,
        performanceLite: Boolean,
        skinLineColor: Int,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        paint: Paint,
        dp: Float
    ) {
        if (replayFrames.size < 2 || isSimulating) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 3f * dp
        tempPath.reset()
        val startIndex = (replayFrames.size - if (performanceLite) 54 else 120).coerceAtLeast(0)
        for (index in startIndex until replayFrames.size) {
            val frame = replayFrames[index]
            val p = frame.ball
            if (index == startIndex) tempPath.moveTo(sx(p.x), sy(p.y)) else tempPath.lineTo(sx(p.x), sy(p.y))
        }
        paint.color = withAlpha(skinLineColor, 92)
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawRiftTrail(
        canvas: Canvas,
        playerLine: List<Point2>,
        performanceLite: Boolean,
        skinLineColor: Int,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        paint: Paint,
        dp: Float
    ) {
        if (playerLine.isEmpty()) return
        paint.style = Paint.Style.FILL
        val startIndex = (playerLine.size - if (performanceLite) 42 else 100).coerceAtLeast(0)
        val visibleSize = playerLine.size - startIndex
        val trailStep = if (performanceLite) 4 else 2
        for (index in startIndex until playerLine.size) {
            val localIndex = index - startIndex
            if (localIndex % trailStep != 0) continue
            val point = playerLine[index]
            val progress = localIndex / visibleSize.toFloat()
            paint.color = withAlpha(skinLineColor, (18f + progress * 92f).roundToInt())
            canvas.drawCircle(sx(point.x), sy(point.y), (1.2f + progress * 2.2f) * dp, paint)
        }
    }

    fun drawGameplayPowerBadge(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        pulse: Float,
        paint: Paint,
        dp: Float,
        powerIconKey: (BallPower) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (skin.power == BallPower.NONE) return
        val badge = radius * 0.58f
        val angle = pulse * 2.35f
        val bx = cx + cos(angle) * radius * 1.18f
        val by = cy + sin(angle) * radius * 1.18f
        scratchRect.set(bx - badge * 0.5f, by - badge * 0.5f, bx + badge * 0.5f, by + badge * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 195)
        canvas.drawCircle(bx, by, badge * 0.56f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = kotlin.math.max(1f * dp, radius * 0.035f)
        paint.color = withAlpha(skin.lineColor, 230)
        canvas.drawCircle(bx, by, badge * 0.56f, paint)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratchRect, 235)
    }

    fun drawBrainballLiveTag(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        isSimulating: Boolean,
        performanceLite: Boolean,
        chainCount: Int,
        pulseIntensity: Float,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (!isSimulating) return
        if (performanceLite && chainCount < 5) return
        val label = when {
            chainCount >= 5 -> "${skin.name} ${t("CHAIN").uppercase()} x$chainCount"
            chainCount >= 3 -> t("CHAIN SPIKE").uppercase()
            pulseIntensity >= 0.62f -> t("BOOST FIELD").uppercase()
            else -> return
        }
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 8.5f * dp
        val width = (textPaint.measureText(label) + 18f * dp).coerceIn(72f * dp, 190f * dp)
        val height = 21f * dp
        val left = (cx - width * 0.5f).coerceIn(8f * dp, viewWidth - width - 8f * dp)
        val top = (cy - radius * 1.95f - height).coerceIn(138f * dp, viewHeight - 72f * dp)
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), 205)
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(skin.lineColor, 190)
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, width - 10f * dp), scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
    }

    fun drawGameplayBall(
        canvas: Canvas,
        ballX: Float,
        ballY: Float,
        ballRadiusWorld: Float,
        skin: BallSkin,
        skinIndex: Int,
        totalSkins: Int,
        liveBallTrail: List<Point2>,
        pulseIntensity: Float,
        isSimulating: Boolean,
        hasPulseStorm: Boolean,
        levelAccent: Int,
        chainCount: Int,
        pulse: Float,
        richEffects: Boolean,
        performanceLite: Boolean,
        adaptiveQuality: Float,
        viewWidth: Float,
        viewHeight: Float,
        artBitmap: Bitmap?,
        paint: Paint,
        dp: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        powerIconKey: (BallPower) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val cx = sx(ballX)
        val cy = sy(ballY)
        val r = worldToScreen(ballRadiusWorld)
        val visualRadius = r * gameplayBallScale(skin, skinIndex)

        drawTrail(canvas, liveBallTrail, skin, visualRadius, sx, sy, paint)
        drawAura(canvas, cx, cy, visualRadius, skin, skinIndex, pulse, richEffects, performanceLite, adaptiveQuality, paint)

        if (isSimulating && pulseIntensity > 0.34f) {
            val alpha = (pulseIntensity.coerceIn(0f, 1f) * 210f).roundToInt()
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 3.4f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), alpha)
            canvas.drawCircle(cx, cy, visualRadius * (2.0f + pulseIntensity * 0.75f), paint)
            paint.strokeWidth = 1.5f * dp
            paint.color = withAlpha(levelAccent, alpha)
            canvas.drawCircle(cx, cy, visualRadius * (1.38f + pulseIntensity * 0.55f), paint)
            paint.strokeCap = Paint.Cap.BUTT
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
            textPaint.textSize = 8f * dp
            textPaint.color = withAlpha(0xFFFFCF4A.toInt(), alpha)
            canvas.drawText(t(if (hasPulseStorm) "STORM" else "BOOST").uppercase(), cx, cy - visualRadius * 2.25f, textPaint)
        }

        drawSkin(canvas, cx, cy, visualRadius, skin, skinIndex, totalSkins, true, false, pulse, richEffects, adaptiveQuality, artBitmap, paint)
        drawGameplayPowerBadge(canvas, cx, cy, visualRadius, skin, pulse, paint, dp, powerIconKey, drawWorldAsset)
        drawBrainballLiveTag(canvas, cx, cy, visualRadius, skin, isSimulating, performanceLite, chainCount, pulseIntensity, viewWidth, viewHeight, paint, dp, t, fitText)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
