package com.moonsolstudios.kavvoro.ui.screens.launch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import android.view.animation.LinearInterpolator
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import kotlin.math.min
import kotlin.math.roundToInt

/** Branded startup animation shown before Age Check or the last in-game screen. */
class LaunchSplashScreenView(
    context: Context,
    private val onLinesMerge: () -> Unit,
    private val onFinished: (LaunchSplashScreenView) -> Unit
) : View(context) {
    private val density = resources.displayMetrics.density
    private val backgroundColor = Color.rgb(5, 7, 13)
    private val mark: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.moonsol_studios_mark)
    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val flarePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(244, 244, 251)
        textSize = dp(31f)
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 199, 62)
        textSize = dp(14f)
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private var animator: ValueAnimator? = null
    private var animationFraction = 0f
    private var started = false
    private var cancelled = false
    private var mergeReported = false
    private var completionReported = false

    init {
        setBackgroundColor(backgroundColor)
        contentDescription = KavvoroI18n.t(context, "LOADING")
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        isFocusable = true
        isClickable = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        dispose()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val elapsed = animationFraction * TOTAL_DURATION_MS
        val fadeStart = TOTAL_DURATION_MS - FADE_OUT_MS
        val fade = 1f - smoothStep((elapsed - fadeStart) / FADE_OUT_MS)
        val entrance = smoothStep(elapsed / LOGO_ENTRANCE_MS)
        val contentAlpha = (fade * entrance).coerceIn(0f, 1f)
        val centerX = width * 0.5f

        val markWidth = min(width * 0.28f, dp(MAX_MARK_WIDTH_DP))
        val markHeight = markWidth * mark.height / mark.width
        val titleMetrics = titlePaint.fontMetrics
        val subtitleMetrics = subtitlePaint.fontMetrics
        val titleHeight = titleMetrics.descent - titleMetrics.ascent
        val subtitleHeight = subtitleMetrics.descent - subtitleMetrics.ascent
        val lineStroke = dp(3f)
        val blockHeight = markHeight + dp(MARK_TO_TITLE_GAP_DP) + titleHeight +
            dp(TITLE_TO_SUBTITLE_GAP_DP) + subtitleHeight + dp(SUBTITLE_TO_LINES_GAP_DP) + lineStroke
        val blockTop = height * CONTENT_CENTER_Y - blockHeight * 0.5f

        val markRect = RectF(
            centerX - markWidth * 0.5f,
            blockTop,
            centerX + markWidth * 0.5f,
            blockTop + markHeight
        )
        val markEntrance = 0.96f + 0.04f * entrance
        val markScaleWidth = markRect.width() * markEntrance
        val markScaleHeight = markRect.height() * markEntrance
        markRect.set(
            centerX - markScaleWidth * 0.5f,
            blockTop + (markHeight - markScaleHeight) * 0.5f,
            centerX + markScaleWidth * 0.5f,
            blockTop + (markHeight + markScaleHeight) * 0.5f
        )
        markPaint.alpha = (255f * contentAlpha).roundToInt().coerceIn(0, 255)
        canvas.drawBitmap(mark, null, markRect, markPaint)

        val titleTop = blockTop + markHeight + dp(MARK_TO_TITLE_GAP_DP)
        val titleBaseline = titleTop - titleMetrics.ascent
        titlePaint.alpha = (255f * contentAlpha).roundToInt().coerceIn(0, 255)
        drawTrackedText(canvas, "MOONSOL", centerX, titleBaseline, titlePaint, dp(1.5f))

        val subtitleTop = titleTop + titleHeight + dp(TITLE_TO_SUBTITLE_GAP_DP)
        val subtitleBaseline = subtitleTop - subtitleMetrics.ascent
        subtitlePaint.alpha = (255f * contentAlpha).roundToInt().coerceIn(0, 255)
        drawTrackedText(canvas, "STUDIOS", centerX, subtitleBaseline, subtitlePaint, dp(3.2f))

        val lineY = subtitleTop + subtitleHeight + dp(SUBTITLE_TO_LINES_GAP_DP)
        drawMergeLines(canvas, centerX, lineY, elapsed, contentAlpha)
    }

    fun dispose() {
        val current = animator ?: return
        animator = null
        current.removeAllUpdateListeners()
        current.removeAllListeners()
        if (current.isRunning) current.cancel()
    }

    private fun startAnimation() {
        if (started) return
        started = true
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = TOTAL_DURATION_MS.toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                animationFraction = animation.animatedFraction
                val elapsed = animationFraction * TOTAL_DURATION_MS
                if (!mergeReported && elapsed >= MERGE_TIME_MS) {
                    mergeReported = true
                    contentDescription = "MoonSol Studios"
                    onLinesMerge()
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (cancelled || completionReported) return
                    completionReported = true
                    onFinished(this@LaunchSplashScreenView)
                }
            })
            start()
        }
    }

    private fun drawMergeLines(canvas: Canvas, centerX: Float, y: Float, elapsed: Float, alpha: Float) {
        val lineProgress = smoothStep(
            (elapsed - LINE_TRAVEL_START_MS) / (MERGE_TIME_MS - LINE_TRAVEL_START_MS)
        )
        val halfLength = min(width * 0.12f, dp(MAX_LINE_HALF_LENGTH_DP))
        val remainingGap = dp(INITIAL_LINE_GAP_DP) * (1f - lineProgress)
        val leftInner = centerX - remainingGap
        val rightInner = centerX + remainingGap
        val leftOuter = leftInner - halfLength
        val rightOuter = rightInner + halfLength

        drawNeonLine(canvas, leftOuter, y, leftInner, y, CYAN, alpha)
        drawNeonLine(canvas, rightInner, y, rightOuter, y, MAGENTA, alpha)

        if (elapsed >= MERGE_TIME_MS) {
            val flash = (1f - (elapsed - MERGE_TIME_MS) / MERGE_FLASH_MS).coerceIn(0f, 1f)
            flarePaint.color = Color.WHITE
            flarePaint.alpha = (220f * flash * alpha).roundToInt().coerceIn(0, 255)
            canvas.drawCircle(centerX, y, dp(4.2f) * flash, flarePaint)
        }
    }

    private fun drawNeonLine(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: Int,
        alpha: Float
    ) {
        linePaint.color = color
        linePaint.alpha = (34f * alpha).roundToInt().coerceIn(0, 255)
        linePaint.strokeWidth = dp(13f)
        canvas.drawLine(startX, startY, endX, endY, linePaint)

        linePaint.alpha = (112f * alpha).roundToInt().coerceIn(0, 255)
        linePaint.strokeWidth = dp(5f)
        canvas.drawLine(startX, startY, endX, endY, linePaint)

        linePaint.alpha = (255f * alpha).roundToInt().coerceIn(0, 255)
        linePaint.strokeWidth = dp(2.4f)
        canvas.drawLine(startX, startY, endX, endY, linePaint)
    }

    private fun drawTrackedText(
        canvas: Canvas,
        text: String,
        centerX: Float,
        baseline: Float,
        paint: Paint,
        letterSpacing: Float
    ) {
        val advances = text.map { paint.measureText(it.toString()) }
        val totalWidth = advances.sum() + letterSpacing * (text.length - 1)
        var x = centerX - totalWidth * 0.5f
        text.forEachIndexed { index, character ->
            val glyph = character.toString()
            canvas.drawText(glyph, x, baseline, paint)
            x += advances[index] + letterSpacing
        }
    }

    private fun smoothStep(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun dp(value: Float): Float = value * density

    private companion object {
        const val TOTAL_DURATION_MS = 2_250
        const val LOGO_ENTRANCE_MS = 320f
        const val LINE_TRAVEL_START_MS = 340f
        const val MERGE_TIME_MS = 1_260f
        const val MERGE_FLASH_MS = 460f
        const val FADE_OUT_MS = 170f
        const val MAX_MARK_WIDTH_DP = 205f
        const val MAX_LINE_HALF_LENGTH_DP = 68f
        const val INITIAL_LINE_GAP_DP = 24f
        const val MARK_TO_TITLE_GAP_DP = 28f
        const val TITLE_TO_SUBTITLE_GAP_DP = 5f
        const val SUBTITLE_TO_LINES_GAP_DP = 29f
        const val CONTENT_CENTER_Y = 0.54f
        val CYAN = Color.rgb(44, 233, 209)
        val MAGENTA = Color.rgb(255, 75, 154)
    }
}
