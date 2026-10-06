package com.moonsolstudios.kavvoro.ui.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.ui.layout.LocaleLayoutPolicy
import com.moonsolstudios.kavvoro.ui.layout.LocaleTextRole
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object UiWidgetRenderer {

    private val scratch = RectF()
    private val scratchRect = RectF()
    private val homePath = Path()
    private val restartArrowPath = Path()
    private val speakerPath = Path()
    private val nextChevronPath = Path()

    private fun ensureCleanPaint(paint: Paint) {
        paint.shader = null
        paint.maskFilter = null
        paint.colorFilter = null
        paint.pathEffect = null
        paint.xfermode = null
        paint.alpha = 255
    }

    private fun restorePaintDefaults(paint: Paint) {
        paint.shader = null
        paint.maskFilter = null
        paint.colorFilter = null
        paint.pathEffect = null
        paint.xfermode = null
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
        paint.style = Paint.Style.FILL
        paint.alpha = 255
    }

    fun fitText(
        context: Context,
        text: String,
        maxWidth: Float,
        textPaint: Paint,
        dp: Float
    ): String {
        textPaint.textScaleX = 1f
        val initialWidth = textPaint.measureText(text)
        if (initialWidth <= maxWidth) return text
        val minimumSize = LocaleLayoutPolicy.minimumTextSizeDp(
            KavvoroI18n.active(context),
            LocaleTextRole.LABEL
        ) * dp
        val step = 0.35f * dp
        val initialSize = textPaint.textSize
        var k = 0
        if (initialSize > minimumSize && initialWidth > 0f && maxWidth > 0f && step > 0f) {
            val maxSteps = kotlin.math.ceil((initialSize - minimumSize) / step).toInt()
            val targetSize = initialSize * (maxWidth / initialWidth) + step
            val skipSteps = ((initialSize - targetSize) / step).toInt().coerceIn(0, maxSteps)
            if (skipSteps > 1) {
                k = skipSteps
                textPaint.textSize = initialSize - k * step
            }
        }
        var measuredWidth = if (k == 0) initialWidth else textPaint.measureText(text)
        while (k > 0 && measuredWidth <= maxWidth) {
            textPaint.textSize = initialSize - (k - 1) * step
            val prevWidth = textPaint.measureText(text)
            if (prevWidth <= maxWidth) {
                k--
                measuredWidth = prevWidth
            } else {
                textPaint.textSize = initialSize - k * step
                break
            }
        }
        while (textPaint.textSize > minimumSize && measuredWidth > maxWidth) {
            k++
            textPaint.textSize = initialSize - k * step
            measuredWidth = textPaint.measureText(text)
        }
        if (measuredWidth > maxWidth && maxWidth > 0f) {
            textPaint.textScaleX = (maxWidth / measuredWidth).coerceAtMost(1f)
        }
        return text
    }

    fun drawFittedText(
        canvas: Canvas,
        context: Context,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        startSizeDp: Float,
        minSizeDp: Float,
        textPaint: Paint,
        dp: Float
    ) {
        val initialSize = startSizeDp * dp
        textPaint.textSize = initialSize
        textPaint.textScaleX = 1f
        val initialWidth = textPaint.measureText(text)
        if (initialWidth > maxWidth) {
            val minSize = maxOf(
                minSizeDp,
                LocaleLayoutPolicy.minimumTextSizeDp(
                    KavvoroI18n.active(context),
                    LocaleTextRole.TITLE
                )
            ) * dp
            val step = 0.35f * dp
            var k = 0
            if (initialSize > minSize && initialWidth > 0f && maxWidth > 0f && step > 0f) {
                val maxSteps = kotlin.math.ceil((initialSize - minSize) / step).toInt()
                val targetSize = initialSize * (maxWidth / initialWidth) + step
                val skipSteps = ((initialSize - targetSize) / step).toInt().coerceIn(0, maxSteps)
                if (skipSteps > 1) {
                    k = skipSteps
                    textPaint.textSize = initialSize - k * step
                }
            }
            var measuredWidth = if (k == 0) initialWidth else textPaint.measureText(text)
            while (k > 0 && measuredWidth <= maxWidth) {
                textPaint.textSize = initialSize - (k - 1) * step
                val prevWidth = textPaint.measureText(text)
                if (prevWidth <= maxWidth) {
                    k--
                    measuredWidth = prevWidth
                } else {
                    textPaint.textSize = initialSize - k * step
                    break
                }
            }
            while (textPaint.textSize > minSize && measuredWidth > maxWidth) {
                k++
                textPaint.textSize = initialSize - k * step
                measuredWidth = textPaint.measureText(text)
            }
        }
        canvas.drawText(fitText(context, text, maxWidth, textPaint, dp), x, y, textPaint)
    }

    fun drawUiButtonFrame(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        accent: Int,
        cornerDp: Float,
        paint: Paint,
        dp: Float
    ) {
        ensureCleanPaint(paint)
        val corner = cornerDp * dp
        val depth = (3.2f * dp).coerceAtLeast(2.5f)
        val pressOffset = if (active) 1.8f * dp else 0f

        // 1. Tactile 3D bottom extrusion lip
        scratch.set(rect.left, rect.top + depth, rect.right, rect.bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF050B16.toInt()
        canvas.drawRoundRect(scratch, corner, corner, paint)
        paint.color = withAlpha(accent, if (active) 140 else 90)
        canvas.drawRoundRect(scratch, corner, corner, paint)

        // 2. Raised button face
        scratchRect.set(rect.left, rect.top + pressOffset, rect.right, rect.bottom - depth + pressOffset)
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
            intArrayOf(
                if (active) withAlpha(accent, 175) else withAlpha(accent, 95),
                if (active) 0xFF14243A.toInt() else 0xF00B1626.toInt(),
                0xF8050B14.toInt()
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, corner, corner, paint)
        paint.shader = null

        // 3. Specular curved glossy glare on top half of face
        scratch.set(scratchRect.left + 1.5f * dp, scratchRect.top + 1f * dp, scratchRect.right - 1.5f * dp, scratchRect.top + scratchRect.height() * 0.48f)
        paint.shader = LinearGradient(
            scratch.left, scratch.top, scratch.left, scratch.bottom,
            0x75FFFFFF.toInt(), 0x05FFFFFF,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, (corner - 1.5f * dp).coerceAtLeast(2f), (corner - 1.5f * dp).coerceAtLeast(2f), paint)
        paint.shader = null

        // 4. Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.color = 0x99FFFFFF.toInt()
        canvas.drawLine(
            scratchRect.left + corner * 0.6f,
            scratchRect.top + 1f * dp,
            scratchRect.right - corner * 0.6f,
            scratchRect.top + 1f * dp,
            paint
        )

        // 5. Laser perimeter border
        paint.strokeWidth = if (active) 1.8f * dp else 1.2f * dp
        paint.color = if (active) 0xFFFFFFFF.toInt() else withAlpha(accent, 220)
        canvas.drawRoundRect(scratchRect, corner, corner, paint)
        restorePaintDefaults(paint)
    }

    fun drawUiIconAsset(
        canvas: Canvas,
        key: String,
        rect: RectF,
        padDp: Float,
        alpha: Int,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val pad = padDp * dp
        scratch.set(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)
        drawWorldAsset(canvas, key, scratch, alpha)
    }

    fun drawHomeVectorIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        ensureCleanPaint(paint)
        val safeSize = size.coerceAtLeast(8f * dp)
        val r = safeSize * 0.5f
        homePath.rewind()
        // Roof apex
        homePath.moveTo(cx, cy - r * 0.72f)
        homePath.lineTo(cx + r * 0.72f, cy - r * 0.08f)
        homePath.lineTo(cx + r * 0.48f, cy - r * 0.08f)
        homePath.lineTo(cx + r * 0.48f, cy + r * 0.68f)
        // Doorway cutout
        homePath.lineTo(cx + r * 0.18f, cy + r * 0.68f)
        homePath.lineTo(cx + r * 0.18f, cy + r * 0.16f)
        homePath.lineTo(cx - r * 0.18f, cy + r * 0.16f)
        homePath.lineTo(cx - r * 0.18f, cy + r * 0.68f)
        // Left side
        homePath.lineTo(cx - r * 0.48f, cy + r * 0.68f)
        homePath.lineTo(cx - r * 0.48f, cy - r * 0.08f)
        homePath.lineTo(cx - r * 0.72f, cy - r * 0.08f)
        homePath.close()

        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(homePath, paint)
        restorePaintDefaults(paint)
    }

    fun drawRestartVectorIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        ensureCleanPaint(paint)
        val safeSize = size.coerceAtLeast(8f * dp)
        val r = safeSize * 0.46f
        scratchRect.set(cx - r, cy - r, cx + r, cy + r)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color
        canvas.drawArc(scratchRect, 35f, 290f, false, paint)

        // Arrow head at 35 degrees pointing counter-clockwise
        val rad = Math.toRadians(35.0)
        val ax = cx + r * cos(rad).toFloat()
        val ay = cy + r * sin(rad).toFloat()
        restartArrowPath.rewind()
        restartArrowPath.moveTo(ax - 2f * dp, ay - 6f * dp)
        restartArrowPath.lineTo(ax + 4f * dp, ay + 1f * dp)
        restartArrowPath.lineTo(ax - 5f * dp, ay + 5f * dp)
        restartArrowPath.close()

        paint.style = Paint.Style.FILL
        canvas.drawPath(restartArrowPath, paint)
        restorePaintDefaults(paint)
    }

    fun drawSpeakerVectorIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, muted: Boolean, color: Int, paint: Paint, dp: Float) {
        ensureCleanPaint(paint)
        val safeSize = size.coerceAtLeast(8f * dp)
        val r = safeSize * 0.5f
        speakerPath.rewind()
        // Speaker body (cone)
        val leftX = cx - r * 0.58f
        val coneX = cx - r * 0.12f
        speakerPath.moveTo(leftX, cy - r * 0.26f)
        speakerPath.lineTo(leftX + r * 0.22f, cy - r * 0.26f)
        speakerPath.lineTo(coneX, cy - r * 0.58f)
        speakerPath.lineTo(coneX, cy + r * 0.58f)
        speakerPath.lineTo(leftX + r * 0.22f, cy + r * 0.26f)
        speakerPath.lineTo(leftX, cy + r * 0.26f)
        speakerPath.close()

        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(speakerPath, paint)

        // Sound waves
        if (!muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f * dp
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = color
            scratchRect.set(cx - r * 0.45f, cy - r * 0.45f, cx + r * 0.45f, cy + r * 0.45f)
            canvas.drawArc(scratchRect, -40f, 80f, false, paint)
            scratchRect.set(cx - r * 0.8f, cy - r * 0.8f, cx + r * 0.8f, cy + r * 0.8f)
            canvas.drawArc(scratchRect, -40f, 80f, false, paint)
        } else {
            // Mute diagonal bar in magenta
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.4f * dp
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = 0xFFFF2E93.toInt()
            canvas.drawLine(cx - r * 0.55f, cy + r * 0.55f, cx + r * 0.65f, cy - r * 0.55f, paint)
        }
        restorePaintDefaults(paint)
    }

    fun drawMusicVectorIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, muted: Boolean, color: Int, paint: Paint, dp: Float) {
        ensureCleanPaint(paint)
        val safeSize = size.coerceAtLeast(8f * dp)
        val r = safeSize * 0.5f
        paint.style = Paint.Style.FILL
        paint.color = color

        // Notehead 1 (left)
        val n1x = cx - r * 0.32f
        val n1y = cy + r * 0.42f
        canvas.save()
        canvas.rotate(-25f, n1x, n1y)
        scratchRect.set(n1x - r * 0.24f, n1y - r * 0.16f, n1x + r * 0.24f, n1y + r * 0.16f)
        canvas.drawOval(scratchRect, paint)
        canvas.restore()

        // Notehead 2 (right)
        val n2x = cx + r * 0.35f
        val n2y = cy + r * 0.25f
        canvas.save()
        canvas.rotate(-25f, n2x, n2y)
        scratchRect.set(n2x - r * 0.24f, n2y - r * 0.16f, n2x + r * 0.24f, n2y + r * 0.16f)
        canvas.drawOval(scratchRect, paint)
        canvas.restore()

        // Stems & connecting beam
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * dp
        paint.strokeCap = Paint.Cap.BUTT
        val stem1X = n1x + r * 0.16f
        val stem2X = n2x + r * 0.16f
        val beamTop1 = cy - r * 0.48f
        val beamTop2 = cy - r * 0.62f
        canvas.drawLine(stem1X, n1y, stem1X, beamTop1, paint)
        canvas.drawLine(stem2X, n2y, stem2X, beamTop2, paint)

        // Horizontal connecting beam
        paint.strokeWidth = 3.6f * dp
        canvas.drawLine(stem1X - 1f * dp, beamTop1, stem2X + 1f * dp, beamTop2, paint)

        if (muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.4f * dp
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = 0xFFFF2E93.toInt()
            canvas.drawLine(cx - r * 0.55f, cy + r * 0.55f, cx + r * 0.65f, cy - r * 0.55f, paint)
        }
        restorePaintDefaults(paint)
    }

    fun drawShareVectorIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        ensureCleanPaint(paint)
        val safeSize = size.coerceAtLeast(8f * dp)
        val r = safeSize * 0.5f
        val x1 = cx - r * 0.45f
        val y1 = cy
        val x2 = cx + r * 0.38f
        val y2 = cy - r * 0.42f
        val x3 = cx + r * 0.38f
        val y3 = cy + r * 0.42f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * dp
        paint.color = color
        canvas.drawLine(x1, y1, x2, y2, paint)
        canvas.drawLine(x1, y1, x3, y3, paint)

        paint.style = Paint.Style.FILL
        val nodeRadius = 3.6f * dp
        canvas.drawCircle(x1, y1, nodeRadius, paint)
        canvas.drawCircle(x2, y2, nodeRadius, paint)
        canvas.drawCircle(x3, y3, nodeRadius, paint)
        restorePaintDefaults(paint)
    }

    fun drawNextVectorIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        ensureCleanPaint(paint)
        val safeSize = size.coerceAtLeast(8f * dp)
        val r = safeSize * 0.5f
        nextChevronPath.rewind()
        // Chevron 1
        nextChevronPath.moveTo(cx - r * 0.45f, cy - r * 0.48f)
        nextChevronPath.lineTo(cx - r * 0.05f, cy)
        nextChevronPath.lineTo(cx - r * 0.45f, cy + r * 0.48f)
        // Chevron 2
        nextChevronPath.moveTo(cx + r * 0.05f, cy - r * 0.48f)
        nextChevronPath.lineTo(cx + r * 0.45f, cy)
        nextChevronPath.lineTo(cx + r * 0.05f, cy + r * 0.48f)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        canvas.drawPath(nextChevronPath, paint)
        restorePaintDefaults(paint)
    }

    fun drawIconButton(
        canvas: Canvas,
        rect: RectF,
        id: ButtonId,
        active: Boolean,
        sfxMuted: Boolean,
        musicMuted: Boolean,
        levelAccent: Int,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (rect.isEmpty) return
        val accent = when (id) {
            ButtonId.HOME -> 0xFF00E5FF.toInt()
            ButtonId.RESTART -> 0xFFFF2E93.toInt()
            ButtonId.SHARE -> 0xFFC15CFF.toInt()
            ButtonId.NEXT -> 0xFFFFCF4A.toInt()
            ButtonId.SFX -> 0xFF00E5FF.toInt()
            ButtonId.MUSIC -> 0xFFFFCF4A.toInt()
            else -> levelAccent
        }
        drawUiButtonFrame(canvas, rect, active, accent, cornerDp = 8f, paint = paint, dp = dp)

        val cx = rect.centerX()
        val cy = rect.centerY()
        val iconSize = min(rect.width(), rect.height()) * 0.54f
        val glyphColor = if (active) 0xFFFFFFFF.toInt() else 0xFFF2F4F7.toInt()

        when (id) {
            ButtonId.HOME -> drawHomeVectorIcon(canvas, cx, cy, iconSize, glyphColor, paint, dp)
            ButtonId.RESTART -> drawRestartVectorIcon(canvas, cx, cy, iconSize, glyphColor, paint, dp)
            ButtonId.SFX -> drawSpeakerVectorIcon(canvas, cx, cy, iconSize, sfxMuted, glyphColor, paint, dp)
            ButtonId.MUSIC -> drawMusicVectorIcon(canvas, cx, cy, iconSize, musicMuted, glyphColor, paint, dp)
            ButtonId.SHARE -> drawShareVectorIcon(canvas, cx, cy, iconSize, glyphColor, paint, dp)
            ButtonId.NEXT -> drawNextVectorIcon(canvas, cx, cy, iconSize, glyphColor, paint, dp)
            else -> {
                val iconKey = when (id) {
                    ButtonId.CONTINUE,
                    ButtonId.AD_CONTINUE,
                    ButtonId.NONE -> null
                    else -> null
                }
                iconKey?.let {
                    drawUiIconAsset(canvas, it, rect, padDp = -1f, alpha = if (active) 255 else 232, dp = dp, drawWorldAsset = drawWorldAsset)
                }
            }
        }
    }

    fun drawResultActionButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        accent: Int,
        button: ButtonId,
        activeButton: ButtonId?,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        fitText: (String, Float) -> String
    ) {
        val secondary = button == ButtonId.SHARE
        val active = activeButton == button
        val radius = dp * 8f
        paint.style = Paint.Style.FILL
        paint.shader = if (secondary) {
            LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(withAlpha(accent, if (active) 74 else 34), 0xDC070B12.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    if (active) withAlpha(accent, 245) else withAlpha(accent, 225),
                    if (button == ButtonId.CONTINUE || button == ButtonId.AD_CONTINUE) 0xFF9D214F.toInt() else 0xFF6C4E12.toInt()
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        paint.color = if (secondary) accent else withAlpha(0xFFFFFFFF.toInt(), 62)
        canvas.drawRect(rect.left, rect.top, rect.left + dp * (if (active) 5f else 3f), rect.bottom, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * (if (active) 1.8f else if (secondary) 1.3f else 1.1f)
        paint.color = if (secondary) accent else withAlpha(accent, if (active) 255 else 180)
        canvas.drawRoundRect(rect, radius, radius, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = 0x55FFFFFF
        canvas.drawLine(rect.left + dp * 8f, rect.top + dp * 1f, rect.right - dp * 8f, rect.top + dp * 1f, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumBold()
        textPaint.textSize = dp * 14f
        textPaint.color = when {
            secondary -> accent
            button == ButtonId.CONTINUE || button == ButtonId.AD_CONTINUE -> 0xFFF7F4FF.toInt()
            else -> 0xFF07090F.toInt()
        }
        canvas.drawText(fitText(label, rect.width() - dp * 20f), rect.centerX(), rect.centerY() + dp * 5f, textPaint)
    }

    fun drawStatusBanner(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        headerText: String,
        message: String,
        accent: Int,
        transient: Boolean,
        paint: Paint,
        textPaint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xF408101C.toInt()
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, if (transient) 220 else 150)
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(scratchRect.left + 8f * dp, scratchRect.top + 1f * dp, scratchRect.right - 8f * dp, scratchRect.top + 1f * dp, paint)

        // Indicator dot
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawCircle(scratchRect.left + 16f * dp, scratchRect.centerY(), 3.5f * dp, paint)

        val oxBold = AssetResourceManager.oxaniumBold()
        val oxNormal = AssetResourceManager.oxaniumNormal()

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = oxBold
        textPaint.textSize = 7.8f * dp
        textPaint.color = if (transient) withAlpha(accent, 240) else 0x88FFFFFF.toInt()
        val resolvedHeader = headerText.ifBlank {
            "🛡️ ${t("FAIR PLAY PROTOCOL ACTIVE // GOOGLE PLAY GAMES").uppercase()}"
        }
        canvas.drawText(
            fitText(resolvedHeader, scratchRect.width() - 40f * dp),
            scratchRect.left + 28f * dp,
            scratchRect.top + 16f * dp,
            textPaint
        )

        textPaint.typeface = oxNormal
        textPaint.textSize = 10.5f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        val defaultMsg = t("Tap any card to open the official world rankings.")
        val text = message.ifBlank { defaultMsg }
        canvas.drawText(
            fitText(text, scratchRect.width() - 40f * dp),
            scratchRect.left + 28f * dp,
            scratchRect.top + 35f * dp,
            textPaint
        )
    }
}
