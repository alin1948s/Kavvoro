package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

/**
 * Shared procedural cyber shapes and icon helpers for Kavvoro UI.
 */
object CyberShapeRenderer {

    private val chamferRectPath = Path()
    private val dualRailLeftPath = Path()
    private val dualRailRightPath = Path()

    fun drawCyberChamferRect(
        canvas: Canvas,
        rect: RectF,
        corner: Float,
        notch: Float,
        fillPaint: Paint
    ) {
        chamferRectPath.rewind()
        val c = corner
        val n = notch * 0.85f
        chamferRectPath.moveTo(rect.left + c + n, rect.top)
        chamferRectPath.lineTo(rect.right - c - n, rect.top)
        chamferRectPath.lineTo(rect.right, rect.top + c + n)
        chamferRectPath.lineTo(rect.right, rect.bottom - c - n)
        chamferRectPath.lineTo(rect.right - c - n, rect.bottom)
        chamferRectPath.lineTo(rect.left + c + n, rect.bottom)
        chamferRectPath.lineTo(rect.left, rect.bottom - c - n)
        chamferRectPath.lineTo(rect.left, rect.top + c + n)
        chamferRectPath.close()
        canvas.drawPath(chamferRectPath, fillPaint)
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
        dualRailLeftPath.rewind()
        dualRailLeftPath.moveTo(midX, rect.top)
        dualRailLeftPath.lineTo(rect.left + c + n, rect.top)
        dualRailLeftPath.lineTo(rect.left, rect.top + c + n)
        dualRailLeftPath.lineTo(rect.left, rect.bottom - c - n)
        dualRailLeftPath.lineTo(rect.left + c + n, rect.bottom)
        dualRailLeftPath.lineTo(midX, rect.bottom)
        canvas.drawPath(dualRailLeftPath, paint)

        paint.color = pink
        dualRailRightPath.rewind()
        dualRailRightPath.moveTo(midX, rect.top)
        dualRailRightPath.lineTo(rect.right - c - n, rect.top)
        dualRailRightPath.lineTo(rect.right, rect.top + c + n)
        dualRailRightPath.lineTo(rect.right, rect.bottom - c - n)
        dualRailRightPath.lineTo(rect.right - c - n, rect.bottom)
        dualRailRightPath.lineTo(midX, rect.bottom)
        canvas.drawPath(dualRailRightPath, paint)
    }

    fun createChamferPath(
        path: Path,
        rect: RectF,
        corner: Float,
        notch: Float
    ) {
        path.reset()
        val c = corner
        val n = notch * 0.85f
        path.moveTo(rect.left + c + n, rect.top)
        path.lineTo(rect.right - c - n, rect.top)
        path.lineTo(rect.right, rect.top + c + n)
        path.lineTo(rect.right, rect.bottom - c - n)
        path.lineTo(rect.right - c - n, rect.bottom)
        path.lineTo(rect.left + c + n, rect.bottom)
        path.lineTo(rect.left, rect.bottom - c - n)
        path.lineTo(rect.left, rect.top + c + n)
        path.close()
    }
}
