package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared procedural cyber shapes and icon helpers for Kavvoro UI.
 */
object CyberShapeRenderer {

    private val tempPath = Path()

    fun drawCyberChamferRect(
        canvas: Canvas,
        rect: RectF,
        corner: Float,
        notch: Float,
        fillPaint: Paint
    ) {
        tempPath.reset()
        val c = corner
        val n = notch * 0.85f
        tempPath.moveTo(rect.left + c + n, rect.top)
        tempPath.lineTo(rect.right - c - n, rect.top)
        tempPath.lineTo(rect.right, rect.top + c + n)
        tempPath.lineTo(rect.right, rect.bottom - c - n)
        tempPath.lineTo(rect.right - c - n, rect.bottom)
        tempPath.lineTo(rect.left + c + n, rect.bottom)
        tempPath.lineTo(rect.left, rect.bottom - c - n)
        tempPath.lineTo(rect.left, rect.top + c + n)
        tempPath.close()
        canvas.drawPath(tempPath, fillPaint)
    }

    fun drawDualRailCyberBorder(
        canvas: Canvas,
        rect: RectF,
        corner: Float,
        notch: Float,
        cyan: Int,
        pink: Int,
        strokeW: Float,
        paint: Paint
    ) {
        val midX = rect.centerX()
        val n = notch * 0.85f
        val c = corner
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeW

        paint.color = cyan
        tempPath.reset()
        tempPath.moveTo(midX, rect.top)
        tempPath.lineTo(rect.left + c + n, rect.top)
        tempPath.lineTo(rect.left, rect.top + c + n)
        tempPath.lineTo(rect.left, rect.bottom - c - n)
        tempPath.lineTo(rect.left + c + n, rect.bottom)
        tempPath.lineTo(midX, rect.bottom)
        canvas.drawPath(tempPath, paint)

        paint.color = pink
        tempPath.reset()
        tempPath.moveTo(midX, rect.top)
        tempPath.lineTo(rect.right - c - n, rect.top)
        tempPath.lineTo(rect.right, rect.top + c + n)
        tempPath.lineTo(rect.right, rect.bottom - c - n)
        tempPath.lineTo(rect.right - c - n, rect.bottom)
        tempPath.lineTo(midX, rect.bottom)
        canvas.drawPath(tempPath, paint)
    }

    fun drawSingleHexagon(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        paint: Paint
    ) {
        tempPath.reset()
        for (i in 0..5) {
            val angle = Math.toRadians(60.0 * i - 30.0)
            val x = (cx + radius * cos(angle)).toFloat()
            val y = (cy + radius * sin(angle)).toFloat()
            if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
        }
        tempPath.close()
        canvas.drawPath(tempPath, paint)
    }

    fun drawHexPattern(canvas: Canvas, rect: RectF, scale: Float, paint: Paint, dp: Float) {
        val hexR = 11f * scale * dp
        val hexW = hexR * 1.732f
        val hexH = hexR * 1.5f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.7f * dp
        paint.color = 0x1245F2FF

        canvas.save()
        canvas.clipRect(rect.left + 8f * dp, rect.top + 6f * dp, rect.right - 8f * dp, rect.bottom - 6f * dp)
        var row = 0
        var y = rect.top + 6f * dp
        while (y < rect.bottom + hexH) {
            val offsetX = if (row % 2 == 1) hexW * 0.5f else 0f
            var x = rect.left + offsetX
            while (x < rect.right + hexW) {
                drawSingleHexagon(canvas, x, y, hexR, paint)
                x += hexW
            }
            y += hexH
            row++
        }
        canvas.restore()
    }
}
