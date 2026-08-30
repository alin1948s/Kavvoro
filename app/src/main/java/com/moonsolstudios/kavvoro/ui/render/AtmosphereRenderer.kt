package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.model.Screen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
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

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
