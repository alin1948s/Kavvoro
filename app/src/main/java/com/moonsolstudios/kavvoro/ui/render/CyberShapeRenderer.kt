package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

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

}
