package com.moonsolstudios.kavvoro.ui.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n

/**
 * Procedural renderer for the wide sci-fi Play CTA button matching play_cta_reference.webp.
 */
object SciFiCtaButtonRenderer {

    private val tempPath = Path()
    private val finPath = Path()
    private val railPath = Path()
    private val chevronPath = Path()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        paint: Paint,
        density: Float,
        context: Context,
        playTitle: String = KavvoroI18n.t(context, "PLAY").uppercase(),
        playSubtitle: String = KavvoroI18n.t(context, "CHOOSE YOUR MODE").uppercase()
    ) {
        val w = rect.width()
        val h = rect.height()
        if (w <= 1f || h <= 1f) return

        val cyan = 0xFF00EFFF.toInt()
        val cyanHot = 0xFFB8FDFF.toInt()
        val blue = 0xFF438DFF.toInt()
        val violet = 0xFF735DFF.toInt()
        val pink = 0xFFFF00B8.toInt()
        val pinkHot = 0xFFFFB2EF.toInt()

        fun framePath(left: Float, top: Float, right: Float, bottom: Float, cut: Float): Path {
            tempPath.reset()
            tempPath.moveTo(left + cut, top)
            tempPath.lineTo(right - cut, top)
            tempPath.lineTo(right, top + cut)
            tempPath.lineTo(right, bottom - cut)
            tempPath.lineTo(right - cut, bottom)
            tempPath.lineTo(left + cut, bottom)
            tempPath.lineTo(left, bottom - cut)
            tempPath.lineTo(left, top + cut)
            tempPath.close()
            return tempPath
        }

        fun hexCellPath(left: Float, top: Float, right: Float, bottom: Float): Path {
            val midY = (top + bottom) * 0.5f
            val cutX = (right - left) * 0.22f
            tempPath.reset()
            tempPath.moveTo(left + cutX, top)
            tempPath.lineTo(right - cutX, top)
            tempPath.lineTo(right, midY)
            tempPath.lineTo(right - cutX, bottom)
            tempPath.lineTo(left + cutX, bottom)
            tempPath.lineTo(left, midY)
            tempPath.close()
            return tempPath
        }

        // 1. Outer Chassis
        val outerCut = h * 0.185f
        val outerInset = h * 0.022f
        val outerPath = framePath(
            rect.left + outerInset,
            rect.top + outerInset,
            rect.right - outerInset,
            rect.bottom - outerInset,
            outerCut
        )

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.left, rect.bottom,
            intArrayOf(0xFF1D3B57.toInt(), 0xFF091523.toInt(), 0xFF050D17.toInt(), 0xFF18334C.toInt()),
            floatArrayOf(0f, 0.35f, 0.70f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(outerPath, paint)
        paint.shader = null

        // Outer Neon Glow Multi-Pass
        val neonShader = LinearGradient(
            rect.left, rect.centerY(), rect.right, rect.centerY(),
            intArrayOf(cyan, cyan, blue, violet, pink, pink),
            floatArrayOf(0f, 0.20f, 0.40f, 0.60f, 0.80f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.BEVEL
        paint.shader = neonShader

        paint.strokeWidth = h * 0.105f
        paint.alpha = (255 * 0.20f).toInt()
        canvas.drawPath(outerPath, paint)

        paint.strokeWidth = h * 0.046f
        paint.alpha = (255 * 0.40f).toInt()
        canvas.drawPath(outerPath, paint)

        paint.strokeWidth = h * 0.012f
        paint.alpha = if (active) 255 else (255 * 0.95f).toInt()
        canvas.drawPath(outerPath, paint)
        paint.shader = null
        paint.alpha = 255

        // 2. Side Armor Fins
        fun drawSideFin(isLeft: Boolean, accent: Int) {
            val x = if (isLeft) rect.left else rect.right
            val innerX = if (isLeft) rect.left + w * 0.070f else rect.right - w * 0.070f
            val dir = if (isLeft) 1f else -1f
            finPath.reset()
            finPath.moveTo(x + dir * w * 0.010f, rect.top + h * 0.385f)
            finPath.lineTo(x + dir * w * 0.042f, rect.top + h * 0.300f)
            finPath.lineTo(innerX, rect.top + h * 0.350f)
            finPath.lineTo(innerX, rect.top + h * 0.650f)
            finPath.lineTo(x + dir * w * 0.042f, rect.top + h * 0.700f)
            finPath.lineTo(x + dir * w * 0.010f, rect.top + h * 0.615f)
            finPath.close()

            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                if (isLeft) rect.left else rect.right, rect.centerY(),
                if (isLeft) rect.right else rect.left, rect.centerY(),
                if (isLeft) intArrayOf(0xFF0C2034.toInt(), 0xFF244A67.toInt())
                else intArrayOf(0xFF4A133E.toInt(), 0xFF180D25.toInt()),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawPath(finPath, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.008f
            paint.color = withAlpha(accent, (255 * 0.65f).toInt())
            canvas.drawPath(finPath, paint)
        }
        drawSideFin(isLeft = true, accent = cyan)
        drawSideFin(isLeft = false, accent = pink)

        // 3. Segmented Top and Bottom Rails
        fun drawRail(startX: Float, endX: Float, isTop: Boolean, accent: Int) {
            val railY = if (isTop) rect.top + h * 0.026f else rect.top + h * 0.974f
            val depth = h * 0.052f
            val slant = h * 0.050f
            railPath.reset()
            if (isTop) {
                railPath.moveTo(startX + slant, railY)
                railPath.lineTo(endX - slant, railY)
                railPath.lineTo(endX, railY + depth)
                railPath.lineTo(startX, railY + depth)
            } else {
                railPath.moveTo(startX, railY - depth)
                railPath.lineTo(endX, railY - depth)
                railPath.lineTo(endX - slant, railY)
                railPath.lineTo(startX + slant, railY)
            }
            railPath.close()

            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                startX, rect.top, startX, rect.bottom,
                intArrayOf(0xFF24435E.toInt(), 0xFF0A1726.toInt(), 0xFF1A334B.toInt()),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawPath(railPath, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = h * 0.006f
            paint.color = withAlpha(accent, (255 * 0.52f).toInt())
            canvas.drawPath(railPath, paint)

            paint.strokeWidth = h * 0.003f
            paint.color = withAlpha(accent, (255 * 0.25f).toInt())
            val lineY = if (isTop) railY + h * 0.010f else railY - h * 0.010f
            canvas.drawLine(startX + slant, lineY, endX - slant, lineY, paint)
        }

        val railSegments = listOf(
            Triple(0.085f, 0.245f, cyan),
            Triple(0.255f, 0.410f, blue),
            Triple(0.420f, 0.595f, violet),
            Triple(0.605f, 0.765f, pink),
            Triple(0.775f, 0.915f, pink)
        )
        for (seg in railSegments) {
            drawRail(rect.left + w * seg.first, rect.left + w * seg.second, isTop = true, accent = seg.third)
            drawRail(rect.left + w * seg.first, rect.left + w * seg.second, isTop = false, accent = seg.third)
        }

        // 4. Inner Luminous Panel
        val innerInset = h * 0.135f
        val innerCut = h * 0.135f
        val innerPath = framePath(
            rect.left + innerInset,
            rect.top + innerInset,
            rect.right - innerInset,
            rect.bottom - innerInset,
            innerCut
        )

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.centerY(), rect.right, rect.centerY(),
            intArrayOf(0xFF041526.toInt(), 0xFF020A16.toInt(), 0xFF080318.toInt(), 0xFF180318.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(innerPath, paint)
        paint.shader = null

        // Inner Multi-Pass Glow
        paint.style = Paint.Style.STROKE
        paint.shader = neonShader
        paint.strokeWidth = h * 0.100f
        paint.alpha = (255 * 0.16f).toInt()
        canvas.drawPath(innerPath, paint)

        paint.strokeWidth = h * 0.045f
        paint.alpha = (255 * 0.34f).toInt()
        canvas.drawPath(innerPath, paint)

        paint.strokeWidth = h * 0.020f
        paint.alpha = (255 * 0.82f).toInt()
        canvas.drawPath(innerPath, paint)
        paint.shader = null

        // Hot Neon Core
        val hotNeonShader = LinearGradient(
            rect.left, rect.centerY(), rect.right, rect.centerY(),
            intArrayOf(cyanHot, 0xFFFFFFFF.toInt(), 0xFFDCE8FF.toInt(), 0xFFFFFFFF.toInt(), pinkHot),
            floatArrayOf(0f, 0.40f, 0.50f, 0.60f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = hotNeonShader
        paint.strokeWidth = h * 0.0065f
        paint.alpha = (255 * 0.98f).toInt()
        canvas.drawPath(innerPath, paint)
        paint.shader = null
        paint.alpha = 255

        // Faint tech lines in panel
        paint.color = withAlpha(cyan, (255 * 0.12f).toInt())
        paint.strokeWidth = h * 0.0025f
        canvas.drawLine(rect.left + w * 0.105f, rect.top + h * 0.255f, rect.left + w * 0.310f, rect.top + h * 0.255f, paint)
        paint.color = withAlpha(pink, (255 * 0.11f).toInt())
        canvas.drawLine(rect.left + w * 0.690f, rect.top + h * 0.745f, rect.left + w * 0.905f, rect.top + h * 0.745f, paint)

        // 5. Left Honeycomb Module
        val moduleCx = rect.left + w * 0.190f
        val moduleCy = rect.top + h * 0.500f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(cyan, (255 * 0.045f).toInt())
        canvas.drawCircle(moduleCx, moduleCy, h * 0.355f, paint)

        val cellW = h * 0.068f
        val cellH = h * 0.070f
        val startX = rect.left + w * 0.112f
        val startY = rect.top + h * 0.260f
        val gapX = h * 0.016f
        val gapY = h * 0.014f

        for (col in 0 until 6) {
            for (row in 0 until 5) {
                val l = startX + col * (cellW + gapX)
                val t = startY + row * (cellH + gapY)
                val r = l + cellW
                val b = t + cellH
                if (r < rect.left + w * 0.315f && b < rect.top + h * 0.745f) {
                    val cell = hexCellPath(l, t, r, b)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = h * 0.0035f
                    paint.color = withAlpha(cyan, (255 * 0.18f).toInt())
                    canvas.drawPath(cell, paint)

                    paint.strokeWidth = h * 0.002f
                    paint.color = withAlpha(cyan, (255 * 0.08f).toInt())
                    val midY = (t + b) * 0.5f
                    canvas.drawLine(l + h * 0.011f, midY, r - h * 0.011f, midY, paint)
                }
            }
        }

        // 6. Right Target Pod
        val podCx = rect.left + w * 0.812f
        val podCy = rect.top + h * 0.500f
        val podR = h * 0.175f

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(pink, (255 * 0.035f).toInt())
        canvas.drawCircle(podCx, podCy, podR * 2.10f, paint)
        paint.color = withAlpha(pink, (255 * 0.065f).toInt())
        canvas.drawCircle(podCx, podCy, podR * 1.62f, paint)

        // Crosshairs
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.0025f
        paint.color = withAlpha(pink, (255 * 0.13f).toInt())
        canvas.drawLine(podCx - podR * 2.05f, podCy, podCx + podR * 2.05f, podCy, paint)
        paint.color = withAlpha(pink, (255 * 0.10f).toInt())
        canvas.drawLine(podCx, podCy - podR * 1.80f, podCy, podCy + podR * 1.80f, paint)

        // Dashed HUD Ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.005f
        paint.pathEffect = DashPathEffect(floatArrayOf(h * 0.018f, h * 0.022f), 0f)
        paint.color = withAlpha(pink, (255 * 0.35f).toInt())
        canvas.drawCircle(podCx, podCy, podR * 1.52f, paint)
        paint.pathEffect = null

        paint.strokeWidth = h * 0.005f
        paint.color = withAlpha(pink, (255 * 0.20f).toInt())
        canvas.drawCircle(podCx, podCy, podR * 1.30f, paint)

        paint.strokeWidth = h * 0.085f
        paint.color = withAlpha(pink, (255 * 0.17f).toInt())
        canvas.drawCircle(podCx, podCy, podR, paint)

        paint.strokeWidth = h * 0.024f
        paint.color = withAlpha(pink, (255 * 0.50f).toInt())
        canvas.drawCircle(podCx, podCy, podR, paint)

        // Dark Radial Cavity
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            podCx, podCy, podR,
            intArrayOf(0xFF3D0A39.toInt(), 0xFF17041D.toInt(), 0xFF05020A.toInt()),
            floatArrayOf(0f, 0.60f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(podCx, podCy, podR * 0.86f, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.009f
        paint.color = withAlpha(pinkHot, (255 * 0.95f).toInt())
        canvas.drawCircle(podCx, podCy, podR * 0.73f, paint)

        paint.strokeWidth = h * 0.0035f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), (255 * 0.10f).toInt())
        canvas.drawCircle(podCx, podCy, podR * 0.56f, paint)

        // White Chevron with Soft Neon Under-stroke
        val arrowSize = h * 0.105f
        chevronPath.reset()
        chevronPath.moveTo(podCx - arrowSize * 0.48f, podCy - arrowSize)
        chevronPath.lineTo(podCx + arrowSize * 0.42f, podCy)
        chevronPath.lineTo(podCx - arrowSize * 0.48f, podCy + arrowSize)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND

        paint.strokeWidth = h * 0.075f
        paint.color = withAlpha(pink, (255 * 0.36f).toInt())
        canvas.drawPath(chevronPath, paint)

        paint.strokeWidth = h * 0.043f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), (255 * 0.20f).toInt())
        canvas.drawPath(chevronPath, paint)

        paint.strokeWidth = h * 0.022f
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(chevronPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER

        // 7. Centered Typography: PLAY & CHOOSE YOUR MODE
        val playCenterX = (rect.left + podCx - podR) * 0.5f
        val playCenterY = rect.centerY()

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = (h * 0.38f).coerceIn(26f * density, 48f * density)

        // Drop shadow
        textPaint.color = 0xAA000000.toInt()
        canvas.drawText(playTitle, playCenterX, playCenterY - 1f * density, textPaint)

        // Text Specular Gradient Face
        textPaint.shader = LinearGradient(
            playCenterX, playCenterY - h * 0.20f,
            playCenterX, playCenterY + h * 0.10f,
            intArrayOf(0xFFFFFFFF.toInt(), 0xFFE2F3FF.toInt(), 0xFFC0DAF0.toInt()),
            floatArrayOf(0f, 0.60f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(playTitle, playCenterX, playCenterY - 2f * density, textPaint)
        textPaint.shader = null

        // Subtitle Text
        textPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textPaint.textSize = (h * 0.11f).coerceIn(8f * density, 13f * density)
        textPaint.color = 0xFFF3F7FF.toInt()
        textPaint.letterSpacing = 0.14f
        canvas.drawText(playSubtitle, playCenterX, playCenterY + h * 0.26f, textPaint)
        textPaint.letterSpacing = 0f
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
