package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.STAGE_WIDTH
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.model.Screen
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

object AtmosphereRenderer {

    fun drawFlash(canvas: Canvas, width: Float, height: Float, color: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRect(0f, 0f, width, height, paint)
    }

    fun drawScreenTransition(
        canvas: Canvas,
        screenTransitionTimer: Float,
        viewWidth: Int,
        viewHeight: Int,
        performanceLite: Boolean,
        secondaryColor: Int,
        screenTransitionAccent: Int,
        menuPulse: Float,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        scratch: RectF,
        portalBitmap: Bitmap?,
        t: (String) -> String
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
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val transitionRings = if (performanceLite) 1 else 3
        repeat(transitionRings) { ring ->
            paint.strokeWidth = dp * (3.4f - ring * 0.7f)
            paint.color = withAlpha(
                if (ring == 1) secondaryColor else screenTransitionAccent,
                (205f * ease * (1f - ring * 0.22f)).roundToInt()
            )
            canvas.drawCircle(cx, cy, maxRadius * (0.22f + ring * 0.12f), paint)
        }

        repeat(if (performanceLite) 2 else 7) { i ->
            val y = cy - dp * 118f + i * dp * 36f + sin(menuPulse * 4.2f + i) * dp * 6f
            val offset = (1f - ease) * viewWidth * 0.34f
            paint.strokeWidth = dp * (if (i % 2 == 0) 2.4f else 1.2f)
            paint.color = withAlpha(if (i % 2 == 0) screenTransitionAccent else 0xFFFFCF4A.toInt(), (145f * ease).roundToInt())
            canvas.drawLine(
                dp * 18f + offset,
                y,
                viewWidth - dp * 18f - offset * 0.45f,
                y + dp * (if (i % 2 == 0) 9f else -7f),
                paint
            )
        }
        paint.strokeCap = Paint.Cap.BUTT

        if (portalBitmap != null && !performanceLite) {
            val size = dp * (86f + 30f * (1f - ease))
            scratch.set(cx - size * 0.5f, cy - size * 0.5f, cx + size * 0.5f, cy + size * 0.5f)
            paint.alpha = (230f * ease).roundToInt().coerceIn(0, 230)
            canvas.save()
            canvas.rotate((1f - ease) * 42f, cx, cy)
            canvas.drawBitmap(portalBitmap, null, scratch, paint)
            canvas.restore()
            paint.alpha = 255
        }

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = BrandTitleRenderer.customTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 13f
        textPaint.letterSpacing = 0.10f
        textPaint.color = withAlpha(0xFFFFFFFF.toInt(), (220f * ease).roundToInt())
        canvas.drawText("✦ ${t("Brainrot Chaos: Kavvoro").uppercase()} ✦", cx, cy + dp * 72f, textPaint)
        textPaint.letterSpacing = 0f
    }

    fun drawBackground(
        canvas: Canvas,
        screen: Screen,
        menuState: MenuState,
        gameMode: GameMode,
        selectedMenuMode: GameMode,
        levelIndex: Int,
        viewWidth: Int,
        viewHeight: Int,
        uiDensity: Float,
        stageLeft: Float,
        scale: Float,
        stateElapsed: Float,
        performanceLite: Boolean,
        richEffects: Boolean,
        dp: Float,
        paint: Paint,
        scratch: RectF,
        starPath: Path,
        worldBitmap: (String) -> Bitmap?,
        backgroundBitmap: (String) -> Bitmap?,
        worldToScreen: (Float) -> Float,
        t: (String) -> String
    ) {
        if (screen == Screen.COLLECTION || screen == Screen.LEADERBOARDS || screen == Screen.SETTINGS || screen == Screen.LANGUAGE || (screen == Screen.MENU && menuState == MenuState.MODE_ACTION)) {
            val isModeSelect = (screen == Screen.MENU && menuState == MenuState.MODE_ACTION)
            val nebulaBmp = if (isModeSelect) (worldBitmap("bg_mode_select") ?: worldBitmap("bg_space_nebula")) else worldBitmap("bg_space_nebula")
            if (nebulaBmp != null) {
                scratch.set(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
                paint.shader = null
                paint.style = Paint.Style.FILL
                paint.alpha = 255
                paint.isFilterBitmap = true
                AssetResourceManager.drawCenterCrop(canvas, nebulaBmp, scratch, paint)

                // 1. Celestial 3D Planets from Home screen (framing the sub-screens)
                val widthDp = viewWidth / uiDensity
                val minDim = minOf(viewWidth, viewHeight).toFloat()
                val planetCyanBmp = worldBitmap("planet_cyan")
                val planetMagentaBmp = worldBitmap("planet_magenta")
                val isTablet = widthDp >= 600f
                val planetWidthFraction = if (isTablet) 0.38f else 0.44f
                val planetSize = minDim * (if (isTablet) planetWidthFraction * 1.05f else planetWidthFraction)

                if (!isModeSelect && planetCyanBmp != null) {
                    val pCx = viewWidth * (if (isTablet) 0.10f else 0.08f)
                    val pCy = viewHeight * (if (isTablet) 0.22f else 0.18f)
                    scratch.set(pCx - planetSize * 0.5f, pCy - planetSize * 0.5f, pCx + planetSize * 0.5f, pCy + planetSize * 0.5f)
                    paint.alpha = 240
                    canvas.drawBitmap(planetCyanBmp, null, scratch, paint)
                }

                if (!isModeSelect && planetMagentaBmp != null) {
                    val pCx = viewWidth * (if (isTablet) 0.90f else 0.92f)
                    val pCy = viewHeight * (if (isTablet) 0.22f else 0.18f)
                    scratch.set(pCx - planetSize * 0.5f, pCy - planetSize * 0.5f, pCx + planetSize * 0.5f, pCy + planetSize * 0.5f)
                    paint.alpha = 240
                    canvas.drawBitmap(planetMagentaBmp, null, scratch, paint)
                }

                // 2. Focused Vibrant Cyber-Atmosphere (Keep nebula radiant & punchy)
                paint.style = Paint.Style.FILL
                paint.shader = RadialGradient(
                    viewWidth * 0.5f,
                    viewHeight * 0.44f,
                    maxOf(viewWidth, viewHeight) * 0.65f,
                    if (isModeSelect) intArrayOf(0x1502050B, 0x3502050B, 0x6602050B)
                    else intArrayOf(0x3502050B, 0x5502050B, 0x8802050B.toInt()),
                    floatArrayOf(0f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
                paint.shader = null

                // 2b. Floating Comic Sparkle Stars in background
                if (!performanceLite) {
                    repeat(10) { i ->
                        val sx = viewWidth * (((i * 419 + 73) % 1000) / 1000f)
                        val sy = viewHeight * (((i * 613 + 181) % 1000) / 1000f) + kotlin.math.sin(stateElapsed.toDouble() * 1.6 + i).toFloat() * 10f * dp
                        val sSize = (3.2f + (i % 3) * 1.8f) * dp
                        val sColor = when (i % 3) {
                            0 -> 0xDD00E5FF.toInt()
                            1 -> 0xDDFF2E93.toInt()
                            else -> 0xDDFFCF4A.toInt()
                        }
                        drawSparkleStar(canvas, sx, sy, sSize, sColor, starPath, paint)
                    }
                }

                // 3. Subtle edge color grading (Cyan pulse left, Magenta pulse right)
                if (!performanceLite) {
                    val pulse = (kotlin.math.sin(stateElapsed.toDouble() * 1.5).toFloat() * 0.5f + 0.5f)
                    paint.shader = RadialGradient(
                        viewWidth * 0.05f,
                        viewHeight * 0.35f,
                        viewWidth * 0.40f,
                        intArrayOf(withAlpha(0xFF00E5FF.toInt(), (20 + (pulse * 10).toInt())), 0x00000000),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)

                    paint.shader = RadialGradient(
                        viewWidth * 0.95f,
                        viewHeight * 0.45f,
                        viewWidth * 0.40f,
                        intArrayOf(withAlpha(0xFFFF2E93.toInt(), (20 + ((1f - pulse) * 10).toInt())), 0x00000000),
                        floatArrayOf(0f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
                    paint.shader = null
                }
                return
            }
        }

        if (screen == Screen.MENU) {
            val baseBg = worldBitmap("bg_space_base") ?: worldBitmap("home_background")
            if (baseBg != null) {
                scratch.set(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
                paint.shader = null
                paint.style = Paint.Style.FILL
                paint.alpha = 255
                paint.isFilterBitmap = true
                AssetResourceManager.drawCenterCrop(canvas, baseBg, scratch, paint)

                val nebula = worldBitmap("nebula_overlay")
                if (nebula != null) {
                    paint.alpha = 240
                    AssetResourceManager.drawCenterCrop(canvas, nebula, scratch, paint)
                    paint.alpha = 255
                }
                return
            }
        }
        val chaosTheme = ((screen == Screen.GAME || screen == Screen.AD) && gameMode == GameMode.CHAOS) ||
            (screen == Screen.MENU && selectedMenuMode == GameMode.CHAOS)
        paint.shader = null
        paint.alpha = 255
        paint.style = Paint.Style.FILL
        val backgroundKey = when {
            screen == Screen.MENU -> if (selectedMenuMode == GameMode.CHAOS) "bg_chaos" else "bg_classic"
            screen != Screen.GAME && screen != Screen.AD -> "bg_menu"
            levelIndex <= 10 -> if (gameMode == GameMode.CHAOS) "bg_tutorial_chaos" else "bg_tutorial_classic"
            levelIndex >= 150 -> "bg_endgame"
            chaosTheme -> "bg_chaos"
            else -> "bg_classic"
        }
        val background = backgroundBitmap(backgroundKey)
        if (background != null) {
            paint.isFilterBitmap = false
            canvas.drawBitmap(background, 0f, 0f, paint)
        } else {
            paint.color = if (chaosTheme) 0xFF160A17.toInt() else 0xFF07121A.toInt()
            canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
        }

        if (performanceLite) {
            paint.shader = null
            paint.color = if (screen == Screen.GAME) 0x5E070A10 else 0x8A070A10.toInt()
            canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
        } else {
            paint.shader = LinearGradient(
                0f,
                0f,
                0f,
                viewHeight.toFloat(),
                intArrayOf(
                    0xB8070A10.toInt(),
                    if (screen == Screen.GAME) 0x4A070A10 else 0x76070A10,
                    if (screen == Screen.GAME) 0x66070A10 else 0xA6070A10.toInt()
                ),
                floatArrayOf(0f, 0.42f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
            paint.shader = null
        }

        if (stageLeft > dp * 2f) {
            val stageRight = stageLeft + STAGE_WIDTH * scale
            paint.style = Paint.Style.FILL
            paint.color = 0x8A07090F.toInt()
            canvas.drawRect(0f, 0f, stageLeft, viewHeight.toFloat(), paint)
            canvas.drawRect(stageRight, 0f, viewWidth.toFloat(), viewHeight.toFloat(), paint)
            paint.color = withAlpha(if (chaosTheme) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt(), 80)
            canvas.drawRect(stageLeft - dp * 1f, 0f, stageLeft, viewHeight.toFloat(), paint)
            canvas.drawRect(stageRight, 0f, stageRight + dp * 1f, viewHeight.toFloat(), paint)
        }

        val gridStep = worldToScreen(
            when {
                performanceLite -> 2.2f
                screen == Screen.GAME && !richEffects -> 1.6f
                else -> 1f
            }
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1f, dp * 0.7f)
        paint.color = if (performanceLite) 0x08FFFFFF else if (screen == Screen.GAME) 0x0EFFFFFF else 0x08FFFFFF
        var x = stageLeft
        while (x <= viewWidth) {
            canvas.drawLine(x, 0f, x, viewHeight.toFloat(), paint)
            x += gridStep
        }
        var y = 0f
        while (y <= viewHeight) {
            canvas.drawLine(0f, y, viewWidth.toFloat(), y, paint)
            y += gridStep
        }

        if (chaosTheme && !performanceLite) {
            paint.strokeWidth = dp * 2.2f
            repeat(if (richEffects) 5 else 3) { i ->
                val offset = -viewHeight * 0.35f + i * viewHeight * 0.24f + sin(stateElapsed * 0.7f + i) * dp * 16f
                paint.color = if (i % 2 == 0) 0x20FF4D8D else 0x18FFCF4A
                canvas.drawLine(0f, offset, viewWidth.toFloat(), offset + viewWidth * 0.72f, paint)
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
            canvas.drawCircle(px, py, dp * (1f + (i % 3)), paint)
        }
    }

    fun drawSparkleStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, starPath: Path, paint: Paint) {
        starPath.reset()
        starPath.moveTo(cx, cy - radius)
        starPath.quadTo(cx, cy, cx + radius, cy)
        starPath.quadTo(cx, cy, cx, cy + radius)
        starPath.quadTo(cx, cy - radius * 0.05f, cx - radius, cy)
        starPath.quadTo(cx, cy, cx, cy - radius)
        starPath.close()
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(starPath, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy, radius * 0.22f, paint)
    }
}
