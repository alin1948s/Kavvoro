package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/** Shared lock treatment for Brainball cards. */
object BallSkinRenderer {
    private val scratchRect = RectF()

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
}
