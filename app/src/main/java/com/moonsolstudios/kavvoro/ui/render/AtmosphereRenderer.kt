package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.model.Screen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Procedural sci-fi atmosphere renderer: background grids, ambient radial glow,
 * language hex patterns, dynamic starfield/dust particles, and atmospheric curse overlays.
 */
object AtmosphereRenderer {

    private val hexPath = Path()
    private val path = Path()
    private val scratchRect = RectF()

    fun drawBackground(
        canvas: Canvas,
        screen: Screen,
        menuState: MenuState,
        gameMode: GameMode,
        selectedMenuMode: GameMode,
        levelIndex: Int,
        viewWidth: Float,
        viewHeight: Float,
        safeInsetLeft: Float,
        safeInsetRight: Float,
        safeInsetBottom: Float,
        safeTop14: Float,
        stageLeft: Float,
        scale: Float,
        stageWidth: Float,
        stateElapsed: Float,
        performanceLite: Boolean,
        richEffects: Boolean,
        bgMenuBitmap: Bitmap?,
        backgroundBitmap: Bitmap?,
        paint: Paint,
        dp: Float,
        worldToScreen: (Float) -> Float,
        drawCenterCrop: (Canvas, Bitmap, RectF, Paint) -> Unit
    ) {
        if (screen == Screen.LANGUAGE) {
            paint.shader = null
            paint.alpha = 255
            paint.style = Paint.Style.FILL
            paint.color = 0xFF01040E.toInt()
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

            val glowRadius = max(viewWidth, viewHeight) * 0.60f

            val leftGlow = RadialGradient(
                viewWidth * 0.12f,
                viewHeight * 0.12f,
                glowRadius,
                intArrayOf(0x3800E5FF.toInt(), 0x1400B4D8.toInt(), 0x00000000),
                floatArrayOf(0f, 0.40f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = leftGlow
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

            val rightGlow = RadialGradient(
                viewWidth * 0.88f,
                viewHeight * 0.12f,
                glowRadius,
                intArrayOf(0x38FF2E93.toInt(), 0x14FF0077.toInt(), 0x00000000),
                floatArrayOf(0f, 0.40f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = rightGlow
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

            val botRightGlow = RadialGradient(
                viewWidth * 0.85f,
                viewHeight * 0.90f,
                glowRadius * 0.85f,
                intArrayOf(0x2CFF2E93.toInt(), 0x0CFF0077.toInt(), 0x00000000),
                floatArrayOf(0f, 0.40f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = botRightGlow
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
            paint.shader = null

            drawLanguageHexPattern(canvas, viewWidth, viewHeight, paint, dp)
            drawLanguageSideCircuitry(canvas, viewWidth, viewHeight, safeInsetLeft, safeInsetRight, safeInsetBottom, safeTop14, paint, dp)
            return
        }

        if (screen == Screen.MENU && menuState == MenuState.MODES) {
            if (bgMenuBitmap != null) {
                paint.shader = null
                paint.alpha = 255
                scratchRect.set(0f, 0f, viewWidth, viewHeight)
                drawCenterCrop(canvas, bgMenuBitmap, scratchRect, paint)
            } else {
                paint.shader = null
                paint.alpha = 255
                paint.style = Paint.Style.FILL
                paint.color = 0xFF040811.toInt()
                canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
            }
            return
        }

        val chaosTheme = ((screen == Screen.GAME || screen == Screen.AD) && gameMode == GameMode.CHAOS) ||
            (screen == Screen.MENU && selectedMenuMode == GameMode.CHAOS)
        paint.shader = null
        paint.alpha = 255
        paint.style = Paint.Style.FILL

        if (backgroundBitmap != null) {
            paint.isFilterBitmap = false
            canvas.drawBitmap(backgroundBitmap, 0f, 0f, paint)
        } else {
            paint.color = if (chaosTheme) 0xFF160A17.toInt() else 0xFF07121A.toInt()
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        }

        if (performanceLite) {
            paint.shader = null
            paint.color = if (screen == Screen.GAME) 0x5E070A10 else 0x8A070A10.toInt()
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        } else {
            paint.shader = LinearGradient(
                0f, 0f, 0f, viewHeight,
                intArrayOf(
                    0xB8070A10.toInt(),
                    if (screen == Screen.GAME) 0x4A070A10 else 0x76070A10,
                    if (screen == Screen.GAME) 0x66070A10 else 0xA6070A10.toInt()
                ),
                floatArrayOf(0f, 0.42f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
            paint.shader = null
        }

        if (stageLeft > 2f * dp) {
            val stageRight = stageLeft + stageWidth * scale
            paint.style = Paint.Style.FILL
            paint.color = 0x8A07090F.toInt()
            canvas.drawRect(0f, 0f, stageLeft, viewHeight, paint)
            canvas.drawRect(stageRight, 0f, viewWidth, viewHeight, paint)
            paint.color = withAlpha(if (chaosTheme) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt(), 80)
            canvas.drawRect(stageLeft - 1f * dp, 0f, stageLeft, viewHeight, paint)
            canvas.drawRect(stageRight, 0f, stageRight + 1f * dp, viewHeight, paint)
        }

        val gridStep = worldToScreen(
            when {
                performanceLite -> 2.2f
                screen == Screen.GAME && !richEffects -> 1.6f
                else -> 1f
            }
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1f, 0.7f * dp)
        paint.color = if (performanceLite) 0x08FFFFFF else if (screen == Screen.GAME) 0x0EFFFFFF else 0x08FFFFFF
        var x = stageLeft
        while (x <= viewWidth) {
            canvas.drawLine(x, 0f, x, viewHeight, paint)
            x += gridStep
        }
        var y = 0f
        while (y <= viewHeight) {
            canvas.drawLine(0f, y, viewWidth, y, paint)
            y += gridStep
        }

        if (chaosTheme && !performanceLite) {
            paint.strokeWidth = 2.2f * dp
            repeat(if (richEffects) 5 else 3) { i ->
                val offset = -viewHeight * 0.35f + i * viewHeight * 0.24f + sin(stateElapsed * 0.7f + i) * 16f * dp
                paint.color = if (i % 2 == 0) 0x20FF4D8D else 0x18FFCF4A
                canvas.drawLine(0f, offset, viewWidth, offset + viewWidth * 0.72f, paint)
            }
        }

        paint.style = Paint.Style.FILL
        repeat(
            when {
                performanceLite -> 0
                richEffects -> 12
                else -> 6
            }
        ) { i ->
            val px = ((i * 137) % 1000) / 1000f * viewWidth
            val py = ((i * 251 + 91) % 1000) / 1000f * viewHeight
            paint.color = if (chaosTheme) {
                if (i % 2 == 0) 0x24FF4D8D else 0x20FFCF4A
            } else {
                if (i % 3 == 0) 0x18FFCF4A else 0x181DE8C8
            }
            canvas.drawCircle(px, py, (1f + (i % 3)) * dp, paint)
        }
    }

    fun drawCurseAtmosphere(
        canvas: Canvas,
        spec: LevelSpec,
        richEffects: Boolean,
        stateElapsed: Float,
        viewWidth: Float,
        viewHeight: Float,
        state: GameState,
        riftEnergy: Float,
        paint: Paint,
        dp: Float
    ) {
        if (spec.curses.isEmpty()) return

        if (spec.curses.any { it.type == CurseType.RIFT_WIND }) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f * dp
            paint.strokeCap = Paint.Cap.ROUND
            repeat(if (richEffects) 8 else 4) { i ->
                val direction = if (sin(stateElapsed * 1.45f + i) >= 0f) 1f else -1f
                val y = 122f * dp + i * viewHeight * 0.095f + sin(stateElapsed * 2f + i) * 7f * dp
                val x = ((stateElapsed * 68f * direction + i * 83f) % (viewWidth + 90f * dp)) - 45f * dp
                val startX = if (direction > 0f) x else viewWidth - x
                paint.color = if (i % 2 == 0) 0x668AA6FF else 0x5545F2FF
                canvas.drawLine(startX, y, startX + direction * 44f * dp, y + sin(i.toFloat()) * 5f * dp, paint)
                canvas.drawLine(startX + direction * 44f * dp, y + sin(i.toFloat()) * 5f * dp, startX + direction * 31f * dp, y - 8f * dp, paint)
                canvas.drawLine(startX + direction * 44f * dp, y + sin(i.toFloat()) * 5f * dp, startX + direction * 31f * dp, y + 8f * dp, paint)
            }
            paint.strokeCap = Paint.Cap.BUTT
        }

        if (spec.curses.any { it.type == CurseType.RIFT_DRAIN }) {
            paint.style = Paint.Style.FILL
            repeat(if (richEffects) 10 else 5) { i ->
                val x = ((i * 97) % 1000) / 1000f * viewWidth
                val y = ((stateElapsed * 72f + i * 53f) % viewHeight)
                paint.color = if (i % 2 == 0) 0x4464E572 else 0x331DE8C8
                canvas.drawRoundRect(x, y, x + 3f * dp, y + (12f + i % 4) * dp, 2f * dp, 2f * dp, paint)
            }
        }

        if (spec.curses.any { it.type == CurseType.OVERHEAT } && state == GameState.SIMULATING) {
            paint.style = Paint.Style.FILL
            paint.color = 0x44FF5757
            canvas.drawRect(0f, 104f * dp, viewWidth * riftEnergy, 108f * dp, paint)
        }
    }

    private fun drawLanguageHexPattern(canvas: Canvas, viewWidth: Float, viewHeight: Float, paint: Paint, dp: Float) {
        val hexR = 14f * dp
        val hexW = hexR * 1.732f
        val hexH = hexR * 1.5f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.75f * dp

        var row = 0
        var y = 0f
        while (y < viewHeight + hexH) {
            val offsetX = if (row % 2 == 1) hexW * 0.5f else 0f
            var x = offsetX
            while (x < viewWidth + hexW) {
                val frac = (x / viewWidth).coerceIn(0f, 1f)
                val alpha = if (y < viewHeight * 0.35f || y > viewHeight * 0.70f) 0x18 else 0x0E
                paint.color = if (frac < 0.5f) {
                    withAlpha(0x00E5FF, (alpha * (1f - frac * 0.4f)).toInt())
                } else {
                    withAlpha(0xFFFF2E93.toInt(), (alpha * (0.6f + frac * 0.4f)).toInt())
                }
                drawSingleHexagon(canvas, x, y, hexR, paint)
                x += hexW
            }
            y += hexH
            row++
        }
    }

    private fun drawLanguageSideCircuitry(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        safeInsetLeft: Float,
        safeInsetRight: Float,
        safeInsetBottom: Float,
        safeTop14: Float,
        paint: Paint,
        dp: Float
    ) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.SQUARE
        paint.strokeJoin = Paint.Join.MITER

        val leftX = safeInsetLeft + 6f * dp
        val rightX = viewWidth - safeInsetRight - 6f * dp
        val topY = safeTop14
        val botY = viewHeight - safeInsetBottom - 14f * dp

        paint.strokeWidth = 1.0f * dp
        paint.color = 0x5500E5FF
        path.reset()
        path.moveTo(leftX + 14f * dp, topY)
        path.lineTo(leftX, topY + 14f * dp)
        path.lineTo(leftX, botY - 14f * dp)
        path.lineTo(leftX + 14f * dp, botY)
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0xAA00F0FF.toInt()
        canvas.drawRect(leftX - 1.5f * dp, topY + 45f * dp, leftX + 2.5f * dp, topY + 49f * dp, paint)
        canvas.drawRect(leftX - 1.5f * dp, botY - 49f * dp, leftX + 2.5f * dp, botY - 45f * dp, paint)

        paint.style = Paint.Style.STROKE
        paint.color = 0x55FF2E93
        path.reset()
        path.moveTo(rightX - 14f * dp, topY)
        path.lineTo(rightX, topY + 14f * dp)
        path.lineTo(rightX, botY - 14f * dp)
        path.lineTo(rightX - 14f * dp, botY)
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0xAAFF0077.toInt()
        canvas.drawRect(rightX - 2.5f * dp, topY + 45f * dp, rightX + 1.5f * dp, topY + 49f * dp, paint)
        canvas.drawRect(rightX - 2.5f * dp, botY - 49f * dp, rightX + 1.5f * dp, botY - 45f * dp, paint)
    }

    private fun drawSingleHexagon(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        hexPath.reset()
        for (i in 0 until 6) {
            val angle = i * PI.toFloat() / 3f - PI.toFloat() / 6f
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r
            if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
        }
        hexPath.close()
        canvas.drawPath(hexPath, paint)
    }

    fun drawScreenTransition(
        canvas: Canvas,
        screenTransitionTimer: Float,
        screenTransitionAccent: Int,
        selectedSkinSecondary: Int,
        portalBitmap: Bitmap?,
        menuPulse: Float,
        performanceLite: Boolean,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint,
        textPaint: Paint,
        dp: Float
    ) {
        if (screenTransitionTimer <= 0f) return
        val progress = (screenTransitionTimer / 0.34f).coerceIn(0f, 1f)
        val ease = progress * progress * (3f - 2f * progress)
        val cx = viewWidth * 0.5f
        val cy = viewHeight * 0.48f
        val maxRadius = max(viewWidth, viewHeight) * (0.58f + (1f - ease) * 0.34f)

        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF07090F.toInt(), (150f * ease).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val transitionRings = if (performanceLite) 1 else 3
        repeat(transitionRings) { ring ->
            paint.strokeWidth = (3.4f - ring * 0.7f) * dp
            paint.color = withAlpha(
                if (ring == 1) selectedSkinSecondary else screenTransitionAccent,
                (205f * ease * (1f - ring * 0.22f)).roundToInt()
            )
            canvas.drawCircle(cx, cy, maxRadius * (0.22f + ring * 0.12f), paint)
        }

        repeat(if (performanceLite) 2 else 7) { i ->
            val y = cy - 118f * dp + i * 36f * dp + sin(menuPulse * 4.2f + i) * 6f * dp
            val offset = (1f - ease) * viewWidth * 0.34f
            paint.strokeWidth = (if (i % 2 == 0) 2.4f else 1.2f) * dp
            paint.color = withAlpha(if (i % 2 == 0) screenTransitionAccent else 0xFFFFCF4A.toInt(), (145f * ease).roundToInt())
            canvas.drawLine(18f * dp + offset, y, viewWidth - 18f * dp - offset * 0.45f, y + (if (i % 2 == 0) 9f else -7f) * dp, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        if (portalBitmap != null && !performanceLite) {
            val size = (86f + 30f * (1f - ease)) * dp
            scratchRect.set(cx - size * 0.5f, cy - size * 0.5f, cx + size * 0.5f, cy + size * 0.5f)
            paint.alpha = (230f * ease).roundToInt().coerceIn(0, 230)
            canvas.save()
            canvas.rotate((1f - ease) * 42f, cx, cy)
            canvas.drawBitmap(portalBitmap, null, scratchRect, paint)
            canvas.restore()
            paint.alpha = 255
        }

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 12f * dp
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (220f * ease).roundToInt())
        canvas.drawText("KAVVORO RIFT", cx, cy + 72f * dp, textPaint)
    }

    fun drawFlash(
        canvas: Canvas,
        flash: Float,
        won: Boolean,
        accent: Int,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (won) accent else 0xFFFF4D8D.toInt(), (flash * 72).roundToInt())
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
