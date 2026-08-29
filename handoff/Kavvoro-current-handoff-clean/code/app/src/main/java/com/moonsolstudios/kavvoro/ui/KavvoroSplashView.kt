package com.moonsolstudios.kavvoro.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.view.View
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class KavvoroSplashView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val dst = RectF()
    private val src = Rect()
    private val background: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.world_bg_menu)
    private val studioLogo: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.moonsol_studios_splash_logo)
    private val startedAt = SystemClock.uptimeMillis()

    init {
        setBackgroundColor(0xFF05070D.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val elapsed = ((SystemClock.uptimeMillis() - startedAt) / 1000f).coerceAtLeast(0f)
        val intro = (elapsed / 0.55f).coerceIn(0f, 1f)
        val pulse = 0.5f + 0.5f * sin(elapsed * 3.4f)

        canvas.drawColor(0xFF05070D.toInt())
        drawBackground(canvas, width, height, elapsed)
        drawEnergyGrid(canvas, width, height, elapsed)
        drawStudioLogoCore(canvas, width, height, elapsed, intro, pulse)
        drawLoadingSignal(canvas, width, height, elapsed)

        postInvalidateOnAnimation()
    }

    private fun drawBackground(canvas: Canvas, width: Float, height: Float, elapsed: Float) {
        val bitmap = background
        if (bitmap != null) {
            drawCover(canvas, bitmap, width, height, 190)
        }

        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            height,
            intArrayOf(0xF605070D.toInt(), 0x8805070D.toInt(), 0xF005070D.toInt()),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        val sweep = ((sin(elapsed * 1.2f) + 1f) * 0.5f) * width
        paint.shader = LinearGradient(
            sweep - width * 0.34f,
            0f,
            sweep + width * 0.18f,
            height,
            intArrayOf(0x001DE8C8, 0x331DE8C8, 0x00FF4D8D),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null
    }

    private fun drawEnergyGrid(canvas: Canvas, width: Float, height: Float, elapsed: Float) {
        val gap = dp(54f)
        val offset = (elapsed * dp(15f)) % gap
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(0.8f)
        paint.color = 0x1EFFFFFF
        var x = -offset
        while (x < width + gap) {
            canvas.drawLine(x, 0f, x, height, paint)
            x += gap
        }
        var y = offset * 0.55f
        while (y < height + gap) {
            canvas.drawLine(0f, y, width, y, paint)
            y += gap
        }

        paint.strokeWidth = dp(2.2f)
        paint.color = 0xAA1DE8C8.toInt()
        canvas.drawLine(width * 0.1f, height * 0.72f, width * 0.9f, height * 0.72f, paint)
        paint.color = 0x88FF4D8D.toInt()
        canvas.drawLine(width * 0.23f, height * 0.24f, width * 0.77f, height * 0.24f, paint)
    }

    private fun drawStudioLogoCore(
        canvas: Canvas,
        width: Float,
        height: Float,
        elapsed: Float,
        intro: Float,
        pulse: Float
    ) {
        val cx = width * 0.5f
        val cy = height * 0.48f + sin(elapsed * 2.4f) * dp(6f)
        val radius = min(width, height) * (0.18f + intro * 0.014f)
        val logoSize = min(width * 0.82f, height * 0.47f) * (0.9f + intro * 0.1f)
        val rotation = elapsed * 18f

        canvas.save()
        canvas.rotate(rotation, cx, cy)
        for (ring in 0 until 3) {
            val ringRadius = radius * (1.24f + ring * 0.19f + pulse * 0.035f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(3.2f - ring * 0.45f)
            paint.color = withAlpha(if (ring % 2 == 0) 0xFF1DE8C8.toInt() else 0xFFFF4D8D.toInt(), 178 - ring * 38)
            dst.set(cx - ringRadius, cy - ringRadius, cx + ringRadius, cy + ringRadius)
            canvas.drawArc(dst, 24f + ring * 54f, 246f, false, paint)
        }
        canvas.restore()

        paint.maskFilter = BlurMaskFilter(dp(28f), BlurMaskFilter.Blur.NORMAL)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFF1DE8C8.toInt(), 58 + (pulse * 46f).roundToInt())
        canvas.drawCircle(cx, cy, radius * 1.38f, paint)
        paint.color = withAlpha(0xFFFF4D8D.toInt(), 54)
        canvas.drawCircle(cx + radius * 0.24f, cy - radius * 0.04f, radius * 1.15f, paint)
        paint.maskFilter = null

        studioLogo?.let { bitmap ->
            dst.set(
                cx - logoSize * 0.5f,
                cy - logoSize * 0.5f,
                cx + logoSize * 0.5f,
                cy + logoSize * 0.5f
            )
            paint.alpha = 255
            paint.isFilterBitmap = true
            canvas.drawBitmap(bitmap, null, dst, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1.8f)
            paint.color = withAlpha(0xFFF7F4FF.toInt(), 120)
            canvas.drawCircle(cx, cy - logoSize * 0.16f, logoSize * 0.24f, paint)
            paint.alpha = 255
        }

        drawSpark(canvas, cx - radius * 1.15f, cy - radius * 0.92f, dp(9f), elapsed, 0xFFFFCF4A.toInt())
        drawSpark(canvas, cx + radius * 1.12f, cy + radius * 0.8f, dp(7f), elapsed + 0.8f, 0xFF45F2FF.toInt())
        drawSpark(canvas, cx + radius * 0.96f, cy - radius * 1.1f, dp(5f), elapsed + 1.4f, 0xFFFF4D8D.toInt())
    }

    private fun drawBrand(canvas: Canvas, width: Float, height: Float, intro: Float) {
        val titleY = height * 0.18f - (1f - intro) * dp(22f)
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.letterSpacing = 0f

        textPaint.textSize = dp(15f)
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText("MOONSOL", width * 0.5f, titleY, textPaint)

        textPaint.textSize = min(dp(50f), width * 0.12f)
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText("STUDIOS", width * 0.5f, titleY + dp(50f), textPaint)

        paint.style = Paint.Style.FILL
        paint.color = 0xFF8AA6FF.toInt()
        canvas.drawRoundRect(width * 0.31f, titleY + dp(70f), width * 0.5f - dp(10f), titleY + dp(76f), dp(4f), dp(4f), paint)
        paint.color = 0xFFFFCF4A.toInt()
        canvas.drawRoundRect(width * 0.5f + dp(10f), titleY + dp(70f), width * 0.69f, titleY + dp(76f), dp(4f), dp(4f), paint)
    }

    private fun drawLoadingSignal(canvas: Canvas, width: Float, height: Float, elapsed: Float) {
        val left = width * 0.17f
        val right = width * 0.83f
        val top = height * 0.79f
        val progress = ((elapsed / SPLASH_DURATION_SECONDS).coerceIn(0f, 1f))

        paint.style = Paint.Style.FILL
        paint.color = 0xAA0B101C.toInt()
        canvas.drawRoundRect(left, top, right, top + dp(48f), dp(9f), dp(9f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.1f)
        paint.color = 0x881DE8C8.toInt()
        canvas.drawRoundRect(left, top, right, top + dp(48f), dp(9f), dp(9f), paint)

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = dp(12f)
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText(KavvoroI18n.t(context, "INITIALIZING KAVVORO").uppercase(), left + dp(16f), top + dp(20f), textPaint)

        val barLeft = left + dp(16f)
        val barRight = right - dp(16f)
        val barTop = top + dp(30f)
        paint.style = Paint.Style.FILL
        paint.color = 0x24FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + dp(5f), dp(3f), dp(3f), paint)
        paint.shader = LinearGradient(barLeft, barTop, barRight, barTop, 0xFF1DE8C8.toInt(), 0xFFFF4D8D.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress, barTop + dp(5f), dp(3f), dp(3f), paint)
        paint.shader = null
    }

    private fun drawSpark(canvas: Canvas, cx: Float, cy: Float, radius: Float, elapsed: Float, color: Int) {
        val rotation = elapsed * 2.8f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(color, 210)
        path.reset()
        for (i in 0 until 8) {
            val angle = rotation + i * PI.toFloat() / 4f
            val r = if (i % 2 == 0) radius else radius * 0.42f
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawCover(canvas: Canvas, bitmap: Bitmap, width: Float, height: Float, alpha: Int) {
        val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val viewRatio = width / height
        if (bitmapRatio > viewRatio) {
            val srcWidth = (bitmap.height * viewRatio).roundToInt()
            val srcLeft = ((bitmap.width - srcWidth) * 0.5f).roundToInt()
            src.set(srcLeft, 0, srcLeft + srcWidth, bitmap.height)
        } else {
            val srcHeight = (bitmap.width / viewRatio).roundToInt()
            val srcTop = ((bitmap.height - srcHeight) * 0.5f).roundToInt()
            src.set(0, srcTop, bitmap.width, srcTop + srcHeight)
        }
        dst.set(0f, 0f, width, height)
        paint.alpha = alpha.coerceIn(0, 255)
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, src, dst, paint)
        paint.alpha = 255
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    companion object {
        const val SPLASH_DURATION_MS = 1650L
        private const val SPLASH_DURATION_SECONDS = SPLASH_DURATION_MS / 1000f
    }
}
