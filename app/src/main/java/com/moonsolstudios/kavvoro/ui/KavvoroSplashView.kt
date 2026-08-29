package com.moonsolstudios.kavvoro.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import com.moonsolstudios.kavvoro.R
import kotlin.math.roundToInt

/**
 * Short, quiet studio splash shown before the game entry screen.
 *
 * The logo asset contains the complete MoonSol lockup. The icon is cropped from
 * its upper square so the wordmark can be revealed independently and the
 * timings remain under the user's recommended 1.5 second ceiling.
 */
class KavvoroSplashView(context: Context) : View(context) {
    private val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconSource = Rect()
    private val iconDestination = RectF()
    private val startedAt = SystemClock.uptimeMillis()
    private var responsiveScale = 1f
    private val studioLogo: Bitmap? = BitmapFactory.decodeResource(
        resources,
        R.drawable.moonsol_studios_splash_logo
    )

    init {
        setBackgroundColor(BLACK)
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width > 0 && height > 0) {
            responsiveScale = ResponsiveUiMetrics.scaleFor(width, height, resources.displayMetrics)
        }

        val elapsed = ((SystemClock.uptimeMillis() - startedAt) / 1000f)
            .coerceAtLeast(0f)
        val fadeOut = fadeOut(elapsed)

        canvas.drawColor(BLACK)
        drawIcon(canvas, elapsed, fadeOut)
        drawWordmark(canvas, elapsed, fadeOut)

        if (elapsed < SPLASH_DURATION_SECONDS) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawIcon(canvas: Canvas, elapsed: Float, fadeOut: Float) {
        val progress = easeOut((elapsed - ICON_START_SECONDS) / ICON_DURATION_SECONDS)
        if (progress <= 0f || studioLogo == null) return

        val alpha = (progress * fadeOut * 255f).roundToInt().coerceIn(0, 255)
        val size = minOf(width * 0.40f, height * 0.20f)
        val centerX = width * 0.5f
        val centerY = height * 0.43f
        val scale = 0.96f + progress * 0.04f
        val renderedSize = size * scale

        // The source image is a square lockup. This crop isolates the Moon/Sun symbol.
        val sourceSize = minOf(studioLogo.width, studioLogo.height)
        // Keep the crop below the wordmark boundary; the source asset contains
        // both the icon and the full lockup in one square bitmap.
        val cropSize = (sourceSize * 0.60f).roundToInt()
        val cropLeft = ((studioLogo.width - cropSize) * 0.5f).roundToInt()
        val cropTop = (sourceSize * 0.055f).roundToInt()
        iconSource.set(cropLeft, cropTop, cropLeft + cropSize, cropTop + cropSize)
        iconDestination.set(
            centerX - renderedSize * 0.5f,
            centerY - renderedSize * 0.5f,
            centerX + renderedSize * 0.5f,
            centerY + renderedSize * 0.5f
        )

        logoPaint.alpha = alpha
        canvas.drawBitmap(studioLogo, iconSource, iconDestination, logoPaint)
        logoPaint.alpha = 255
    }

    private fun drawWordmark(canvas: Canvas, elapsed: Float, fadeOut: Float) {
        val moonsolProgress = easeOut((elapsed - MOONSOL_START_SECONDS) / MOONSOL_DURATION_SECONDS)
        val studiosProgress = easeOut((elapsed - STUDIOS_START_SECONDS) / STUDIOS_DURATION_SECONDS)
        if (moonsolProgress <= 0f && studiosProgress <= 0f) return

        val iconSize = minOf(width * 0.40f, height * 0.20f)
        val iconBottom = height * 0.43f + iconSize * 0.5f
        val centerX = width * 0.5f

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.letterSpacing = 0.08f
        textPaint.textSize = dp(31f)
        textPaint.color = withAlpha(0xFFF7F4FF.toInt(), (moonsolProgress * fadeOut * 255f).roundToInt())
        canvas.drawText("MOONSOL", centerX, iconBottom + dp(82f), textPaint)

        textPaint.letterSpacing = 0.20f
        textPaint.textSize = dp(16f)
        textPaint.color = withAlpha(
            0xFFFFCF4A.toInt(),
            (studiosProgress * fadeOut * 255f).roundToInt()
        )
        canvas.drawText("STUDIOS", centerX, iconBottom + dp(116f), textPaint)

        val lineProgress = studiosProgress * fadeOut
        val lineWidth = width * 0.105f * lineProgress
        val lineGap = width * 0.035f
        val lineY = iconBottom + dp(145f)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        stroke.color = withAlpha(0xFF1DE8C8.toInt(), (lineProgress * 255f).roundToInt())
        canvas.drawRoundRect(
            centerX - lineGap - lineWidth,
            lineY,
            centerX - lineGap,
            lineY + dp(4f),
            dp(2f),
            dp(2f),
            stroke
        )

        stroke.color = withAlpha(0xFFFF4D8D.toInt(), (lineProgress * 255f).roundToInt())
        canvas.drawRoundRect(
            centerX + lineGap,
            lineY,
            centerX + lineGap + lineWidth,
            lineY + dp(4f),
            dp(2f),
            dp(2f),
            stroke
        )
    }

    private fun fadeOut(elapsed: Float): Float {
        return 1f - ((elapsed - FADE_OUT_START_SECONDS) / FADE_OUT_DURATION_SECONDS)
            .coerceIn(0f, 1f)
    }

    private fun easeOut(value: Float): Float {
        return value.coerceIn(0f, 1f).let { 1f - (1f - it) * (1f - it) }
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density * responsiveScale

    companion object {
        const val SPLASH_DURATION_MS = 1450L
        private const val SPLASH_DURATION_SECONDS = SPLASH_DURATION_MS / 1000f
        private const val ICON_START_SECONDS = 0.10f
        private const val ICON_DURATION_SECONDS = 0.40f
        private const val MOONSOL_START_SECONDS = 0.40f
        private const val MOONSOL_DURATION_SECONDS = 0.40f
        private const val STUDIOS_START_SECONDS = 0.60f
        private const val STUDIOS_DURATION_SECONDS = 0.30f
        private const val FADE_OUT_START_SECONDS = 1.20f
        private const val FADE_OUT_DURATION_SECONDS = 0.25f
        private const val BLACK = 0xFF05070D.toInt()
    }
}
