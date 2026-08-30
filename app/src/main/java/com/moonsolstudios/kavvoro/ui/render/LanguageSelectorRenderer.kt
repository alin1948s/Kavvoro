package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage

/**
 * Procedural and bitmap renderer for the Language Selector screen.
 */
object LanguageSelectorRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawBackButton(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        bmp: Bitmap?,
        paint: Paint,
        dp: Float
    ) {
        if (bmp != null && !bmp.isRecycled) {
            if (active) {
                paint.style = Paint.Style.FILL
                paint.color = 0x4400F0FF
                CyberShapeRenderer.drawCyberChamferRect(canvas, rect, 9f * dp, 6f * dp, paint)
            }
            canvas.drawBitmap(bmp, null, rect, null)
            if (active) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.8f * dp
                paint.color = 0xFF00F0FF.toInt()
                CyberShapeRenderer.drawCyberChamferRect(canvas, rect, 9f * dp, 6f * dp, paint)
            }
            return
        }

        val corner = 9f * dp
        val notch = 6f * dp

        paint.style = Paint.Style.FILL
        paint.color = if (active) 0x5500F0FF.toInt() else 0xE0060910.toInt()
        CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.6f * dp else 1.2f * dp
        paint.color = if (active) 0xFF00F0FF.toInt() else 0x88FF2E93.toInt()
        CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)

        val cx = rect.centerX()
        val cy = rect.centerY()
        val arm = 7f * dp

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4.5f * dp
        paint.color = 0x4400F0FF
        tempPath.reset()
        tempPath.moveTo(cx + arm * 0.35f, cy - arm)
        tempPath.lineTo(cx - arm * 0.45f, cy)
        tempPath.lineTo(cx + arm * 0.35f, cy + arm)
        canvas.drawPath(tempPath, paint)

        paint.strokeWidth = 2.6f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = 0xFF00F0FF.toInt()
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawItem(
        canvas: Canvas,
        rect: RectF,
        language: KavvoroLanguage,
        selected: Boolean,
        active: Boolean,
        isRightCol: Boolean,
        cardBmp: Bitmap?,
        flagBmp: Bitmap?,
        radioBmp: Bitmap?,
        typeface: Typeface?,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawFlagFallback: (Canvas, RectF, KavvoroLanguage) -> Unit
    ) {
        val cyanAccent = 0xFF00F0FF.toInt()

        // ── Card background frame (PSD Asset / Procedural fallback) ──
        if (cardBmp != null && !cardBmp.isRecycled) {
            canvas.drawBitmap(cardBmp, null, rect, null)
        } else {
            val corner = 7f * dp
            val notch = 4.5f * dp
            paint.style = Paint.Style.FILL
            paint.color = when {
                selected -> 0xF00D1C2A.toInt()
                active -> 0xF0122234.toInt()
                else -> 0xF0090E17.toInt()
            }
            CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)

            paint.style = Paint.Style.STROKE
            if (selected) {
                paint.strokeWidth = 3.8f * dp
                paint.color = 0x4400F0FF
                scratchRect.set(rect.left - 1f * dp, rect.top - 1f * dp, rect.right + 1f * dp, rect.bottom + 1f * dp)
                CyberShapeRenderer.drawCyberChamferRect(canvas, scratchRect, corner + 1f * dp, notch + 1f * dp, paint)

                paint.strokeWidth = 1.8f * dp
                paint.color = cyanAccent
                CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)
            } else {
                paint.strokeWidth = 1.0f * dp
                paint.color = if (isRightCol) 0x66FF0077.toInt() else 0x6600B4D8.toInt()
                CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)
            }
        }

        // ── Flag badge (left side) ──
        val flagH = rect.height() * 0.72f
        val flagW = flagH * (78f / 88f)
        val flagLeft = rect.left + 8f * dp
        val flagTop = rect.centerY() - flagH * 0.5f
        scratchRect.set(flagLeft, flagTop, flagLeft + flagW, flagTop + flagH)

        if (flagBmp != null && !flagBmp.isRecycled) {
            canvas.drawBitmap(flagBmp, null, scratchRect, null)
        } else {
            val badgeCorner = 3.5f * dp
            val badgeNotch = 3f * dp
            canvas.save()
            tempPath.reset()
            val bn = badgeNotch * 0.85f
            val bc = badgeCorner
            tempPath.moveTo(scratchRect.left + bc + bn, scratchRect.top)
            tempPath.lineTo(scratchRect.right - bc - bn, scratchRect.top)
            tempPath.lineTo(scratchRect.right, scratchRect.top + bc + bn)
            tempPath.lineTo(scratchRect.right, scratchRect.bottom - bc - bn)
            tempPath.lineTo(scratchRect.right - bc - bn, scratchRect.bottom)
            tempPath.lineTo(scratchRect.left + bc + bn, scratchRect.bottom)
            tempPath.lineTo(scratchRect.left, scratchRect.bottom - bc - bn)
            tempPath.lineTo(scratchRect.left, scratchRect.top + bc + bn)
            tempPath.close()
            canvas.clipPath(tempPath)
            drawFlagFallback(canvas, scratchRect, language)
            canvas.restore()

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = 0xFFC29B38.toInt()
            canvas.drawPath(tempPath, paint)
        }

        // ── Language name ──
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = typeface
        textPaint.textSize = (rect.height() * 0.26f).coerceIn(11.5f * dp, 14.5f * dp)
        textPaint.color = 0xFFF5F8FF.toInt()
        val textLeft = flagLeft + flagW + 8f * dp
        val maxTextWidth = rect.right - 34f * dp - textLeft
        val textY = rect.centerY() + textPaint.textSize * 0.46f
        canvas.drawText(fitText(language.nativeName, maxTextWidth), textLeft, textY, textPaint)

        // ── Radio button (right side) ──
        if (radioBmp != null && !radioBmp.isRecycled) {
            val radioH = rect.height() * 0.54f
            val radioW = radioH * (59f / 74f)
            val radioRight = rect.right - 8f * dp
            val radioTop = rect.centerY() - radioH * 0.5f
            scratchRect.set(radioRight - radioW, radioTop, radioRight, radioTop + radioH)
            canvas.drawBitmap(radioBmp, null, scratchRect, null)
        } else {
            val radioCx = rect.right - 16f * dp
            val radioCy = rect.centerY()
            val radioR = 8.5f * dp

            if (selected) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.8f * dp
                paint.color = cyanAccent
                canvas.drawCircle(radioCx, radioCy, radioR, paint)

                paint.strokeWidth = 2.0f * dp
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND
                tempPath.reset()
                tempPath.moveTo(radioCx - 3.8f * dp, radioCy + 0.2f * dp)
                tempPath.lineTo(radioCx - 1.0f * dp, radioCy + 3.0f * dp)
                tempPath.lineTo(radioCx + 3.8f * dp, radioCy - 2.8f * dp)
                canvas.drawPath(tempPath, paint)
                paint.strokeCap = Paint.Cap.BUTT
            } else {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.1f * dp
                paint.color = if (isRightCol) 0x66FF0077.toInt() else 0x6600B4D8.toInt()
                canvas.drawCircle(radioCx, radioCy, radioR, paint)
            }
        }
    }

    fun drawCurrentBar(
        canvas: Canvas,
        rect: RectF,
        footerBmp: Bitmap?,
        activeLanguageName: String,
        currentPrefix: String,
        typeface: Typeface?,
        paint: Paint,
        dp: Float
    ) {
        if (footerBmp != null && !footerBmp.isRecycled) {
            canvas.drawBitmap(footerBmp, null, rect, null)
        } else {
            paint.style = Paint.Style.FILL
            paint.color = 0xF5060A12.toInt()
            CyberShapeRenderer.drawCyberChamferRect(canvas, rect, 7f * dp, 9f * dp, paint)
            CyberShapeRenderer.drawDualRailCyberBorder(canvas, rect, 7f * dp, 9f * dp, 0xFF00F0FF.toInt(), 0xFFFF0077.toInt(), 1.4f * dp, paint)

            val dotCx = rect.left + rect.width() * (69.5f / 800f)
            val dotCy = rect.top + rect.height() * (85.7f / 165f)
            paint.style = Paint.Style.FILL
            paint.color = 0x4000F0FF
            canvas.drawCircle(dotCx, dotCy, 7.5f * dp, paint)
            paint.color = 0xFF00F0FF.toInt()
            canvas.drawCircle(dotCx, dotCy, 3.8f * dp, paint)
        }

        val textX = rect.left + rect.width() * (105f / 800f)
        val textCenterY = rect.top + rect.height() * (85.7f / 165f)
        val textSize = (rect.height() * 0.28f).coerceIn(12f * dp, 16f * dp)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = typeface
        textPaint.textSize = textSize
        textPaint.letterSpacing = 0.05f

        textPaint.color = 0xFFD3C1AF.toInt()
        val textY = textCenterY + textSize * 0.35f
        canvas.drawText(currentPrefix, textX, textY, textPaint)

        val prefixWidth = textPaint.measureText(currentPrefix + "  ")
        textPaint.color = 0xFF00F0FF.toInt()
        canvas.drawText(activeLanguageName, textX + prefixWidth, textY, textPaint)
        textPaint.letterSpacing = 0f
    }

    fun drawScreen(
        canvas: Canvas,
        side: Float,
        contentRight: Float,
        contentWidth: Float,
        compact: Boolean,
        centerX: Float,
        selected: KavvoroLanguage,
        activeLanguageIndex: Int,
        headerFrameBmp: Bitmap?,
        diamondBmp: Bitmap?,
        backButtonRect: RectF,
        backButtonBmp: Bitmap?,
        itemRects: List<RectF>,
        viewportTop: Float,
        viewportBottom: Float,
        footerRect: RectF,
        footerBmp: Bitmap?,
        langCardBitmap: (Boolean, Boolean) -> Bitmap?,
        languageFlagBitmap: (KavvoroLanguage) -> Bitmap?,
        langRadioBitmap: (Boolean) -> Bitmap?,
        typeface: Typeface?,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFlagFallback: (Canvas, RectF, KavvoroLanguage) -> Unit
    ) {
        // ── Top Framing Decals ──
        if (headerFrameBmp != null && !headerFrameBmp.isRecycled) {
            val hfH = (contentWidth * (240f / 810f)).coerceAtMost(100f * dp)
            scratchRect.set(side, backButtonRect.top - 6f * dp, contentRight, backButtonRect.top - 6f * dp + hfH)
            canvas.drawBitmap(headerFrameBmp, null, scratchRect, null)
        }

        // ── Back button (top-left) ──
        drawBackButton(canvas, backButtonRect, activeLanguageIndex == -2, backButtonBmp, paint, dp)

        // ── Title: "CHOOSE LANGUAGE" (Centered, Oxanium typeface, bold) ──
        val titleSize = if (compact) 16.5f * dp else kotlin.math.min(21f * dp, contentWidth / 15f)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = typeface
        textPaint.textSize = titleSize
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = if (compact) 0.04f else 0.07f
        canvas.drawText(t("CHOOSE LANGUAGE").uppercase(), centerX, backButtonRect.centerY() + titleSize * 0.35f, textPaint)
        textPaint.letterSpacing = 0f

        // ── Separator line with 3D Center Jewel Diamond ──
        val divY = backButtonRect.bottom + (if (compact) 9f else 12f) * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.shader = android.graphics.LinearGradient(
            side, divY, contentRight, divY,
            intArrayOf(0x0000F0FF, 0xEE00F0FF.toInt(), 0xEEFF0077.toInt(), 0x00FF0077),
            floatArrayOf(0f, 0.46f, 0.54f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawLine(side, divY, contentRight, divY, paint)
        paint.shader = null

        // Center glowing 3D Jewel Diamond
        if (diamondBmp != null && !diamondBmp.isRecycled) {
            val dSize = (if (compact) 13f else 15f) * dp
            scratchRect.set(centerX - dSize * 0.5f, divY - dSize * 0.5f, centerX + dSize * 0.5f, divY + dSize * 0.5f)
            paint.style = Paint.Style.FILL
            paint.color = 0x3300F0FF
            canvas.drawCircle(centerX, divY, dSize * 0.8f, paint)
            canvas.drawBitmap(diamondBmp, null, scratchRect, null)
        } else {
            val diamondR = 5.5f * dp
            paint.style = Paint.Style.FILL
            paint.color = 0x4400F0FF
            canvas.drawCircle(centerX, divY, diamondR * 1.8f, paint)

            tempPath.reset()
            tempPath.moveTo(centerX - diamondR, divY)
            tempPath.lineTo(centerX, divY - diamondR)
            tempPath.lineTo(centerX + diamondR, divY)
            tempPath.close()
            paint.color = 0xFF00F0FF.toInt()
            canvas.drawPath(tempPath, paint)

            tempPath.reset()
            tempPath.moveTo(centerX - diamondR, divY)
            tempPath.lineTo(centerX, divY + diamondR)
            tempPath.lineTo(centerX + diamondR, divY)
            tempPath.close()
            paint.color = 0xFFBD00FF.toInt()
            canvas.drawPath(tempPath, paint)

            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(centerX, divY, 1.2f * dp, paint)
        }

        // ── Language Grid (2 columns) ──
        canvas.save()
        canvas.clipRect(0f, viewportTop, canvas.width.toFloat(), viewportBottom)
        val displayLanguages = KavvoroLanguage.selectableLanguages
        displayLanguages.forEachIndexed { index, language ->
            val rect = itemRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.bottom >= viewportTop && rect.top <= viewportBottom) {
                val isSelected = language == selected
                val isRightCol = (index % 2) == 1
                drawItem(
                    canvas = canvas,
                    rect = rect,
                    language = language,
                    selected = isSelected,
                    active = activeLanguageIndex == index,
                    isRightCol = isRightCol,
                    cardBmp = langCardBitmap(isSelected, isRightCol),
                    flagBmp = languageFlagBitmap(language),
                    radioBmp = langRadioBitmap(isSelected),
                    typeface = typeface,
                    paint = paint,
                    dp = dp,
                    fitText = fitText,
                    drawFlagFallback = drawFlagFallback
                )
            }
        }
        canvas.restore()

        // ── Bottom status bar: CURRENT: LANGUAGE ──
        val langName = (if (selected == KavvoroLanguage.EN || selected == KavvoroLanguage.SYSTEM) "ENGLISH" else selected.nativeName).uppercase()
        val currentPrefix = t("CURRENT").uppercase() + ":"
        drawCurrentBar(
            canvas = canvas,
            rect = footerRect,
            footerBmp = footerBmp,
            activeLanguageName = langName,
            currentPrefix = currentPrefix,
            typeface = typeface,
            paint = paint,
            dp = dp
        )
    }

    fun drawVectorFlag(canvas: Canvas, r: RectF, language: KavvoroLanguage, paint: Paint, dp: Float) {
        val w = r.width()
        val h = r.height()
        val cx = r.centerX()
        val cy = r.centerY()
        val left = r.left
        val top = r.top
        val right = r.right
        val bottom = r.bottom

        canvas.save()
        canvas.clipRect(r)

        paint.style = Paint.Style.FILL
        when (language) {
            KavvoroLanguage.EN -> {
                paint.color = 0xFF012169.toInt()
                canvas.drawRect(r, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = h * 0.28f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawLine(left, top, right, bottom, paint)
                canvas.drawLine(left, bottom, right, top, paint)
                paint.strokeWidth = h * 0.14f
                paint.color = 0xFFC8102E.toInt()
                canvas.drawLine(left, top, right, bottom, paint)
                canvas.drawLine(left, bottom, right, top, paint)
                paint.strokeWidth = h * 0.38f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawLine(cx, top, cx, bottom, paint)
                canvas.drawLine(left, cy, right, cy, paint)
                paint.strokeWidth = h * 0.22f
                paint.color = 0xFFC8102E.toInt()
                canvas.drawLine(cx, top, cx, bottom, paint)
                canvas.drawLine(left, cy, right, cy, paint)
            }

            KavvoroLanguage.RO -> {
                val third = w / 3f
                paint.color = 0xFF002B7F.toInt()
                canvas.drawRect(left, top, left + third, bottom, paint)
                paint.color = 0xFFFCD116.toInt()
                canvas.drawRect(left + third, top, left + third * 2f, bottom, paint)
                paint.color = 0xFFCE1126.toInt()
                canvas.drawRect(left + third * 2f, top, right, bottom, paint)
            }

            KavvoroLanguage.ES -> {
                val quarter = h / 4f
                paint.color = 0xFFAA151B.toInt()
                canvas.drawRect(left, top, right, left + quarter, paint)
                paint.color = 0xFFF1BF00.toInt()
                canvas.drawRect(left, top + quarter, right, bottom - quarter, paint)
                paint.color = 0xFFAA151B.toInt()
                canvas.drawRect(left, bottom - quarter, right, bottom, paint)
                paint.color = 0xFFAA151B.toInt()
                canvas.drawCircle(left + w * 0.30f, cy, h * 0.16f, paint)
            }

            KavvoroLanguage.FR -> {
                val third = w / 3f
                paint.color = 0xFF002654.toInt()
                canvas.drawRect(left, top, left + third, bottom, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left + third, top, left + third * 2f, bottom, paint)
                paint.color = 0xFFED2939.toInt()
                canvas.drawRect(left + third * 2f, top, right, bottom, paint)
            }

            KavvoroLanguage.DE -> {
                val third = h / 3f
                paint.color = 0xFF000000.toInt()
                canvas.drawRect(left, top, right, top + third, paint)
                paint.color = 0xFFDD0000.toInt()
                canvas.drawRect(left, top + third, right, top + third * 2f, paint)
                paint.color = 0xFFFFCE00.toInt()
                canvas.drawRect(left, top + third * 2f, right, bottom, paint)
            }

            KavvoroLanguage.IT -> {
                val third = w / 3f
                paint.color = 0xFF009246.toInt()
                canvas.drawRect(left, top, left + third, bottom, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left + third, top, left + third * 2f, bottom, paint)
                paint.color = 0xFFCE2B37.toInt()
                canvas.drawRect(left + third * 2f, top, right, bottom, paint)
            }

            KavvoroLanguage.PT -> {
                val split = left + w * 0.38f
                paint.color = 0xFF006600.toInt()
                canvas.drawRect(left, top, split, bottom, paint)
                paint.color = 0xFFFF0000.toInt()
                canvas.drawRect(split, top, right, bottom, paint)
                paint.color = 0xFFFFCC00.toInt()
                canvas.drawCircle(split, cy, h * 0.22f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(split, cy, h * 0.12f, paint)
            }

            KavvoroLanguage.NL -> {
                val third = h / 3f
                paint.color = 0xFFAE1C28.toInt()
                canvas.drawRect(left, top, right, top + third, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left, top + third, right, top + third * 2f, paint)
                paint.color = 0xFF21468B.toInt()
                canvas.drawRect(left, top + third * 2f, right, bottom, paint)
            }

            KavvoroLanguage.PL -> {
                val half = top + h * 0.5f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left, top, right, half, paint)
                paint.color = 0xFFDC143C.toInt()
                canvas.drawRect(left, half, right, bottom, paint)
            }

            KavvoroLanguage.TR -> {
                paint.color = 0xFFE30A17.toInt()
                canvas.drawRect(r, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(left + w * 0.40f, cy, h * 0.30f, paint)
                paint.color = 0xFFE30A17.toInt()
                canvas.drawCircle(left + w * 0.46f, cy, h * 0.24f, paint)
                drawSimpleStar(canvas, left + w * 0.65f, cy, h * 0.16f, 0xFFFFFFFF.toInt(), paint)
            }

            KavvoroLanguage.RU -> {
                val third = h / 3f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left, top, right, top + third, paint)
                paint.color = 0xFF0039A6.toInt()
                canvas.drawRect(left, top + third, right, top + third * 2f, paint)
                paint.color = 0xFFD52B1E.toInt()
                canvas.drawRect(left, top + third * 2f, right, bottom, paint)
            }

            KavvoroLanguage.UK -> {
                val half = top + h * 0.5f
                paint.color = 0xFF0057B7.toInt()
                canvas.drawRect(left, top, right, half, paint)
                paint.color = 0xFFFFD700.toInt()
                canvas.drawRect(left, half, right, bottom, paint)
            }

            KavvoroLanguage.AR -> {
                paint.color = 0xFF006C35.toInt()
                canvas.drawRect(r, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = h * 0.08f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawLine(left + w * 0.20f, cy + h * 0.18f, right - w * 0.20f, cy + h * 0.18f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy - h * 0.08f, h * 0.16f, paint)
            }

            KavvoroLanguage.HI -> {
                val third = h / 3f
                paint.color = 0xFFFF9933.toInt()
                canvas.drawRect(left, top, right, top + third, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left, top + third, right, top + third * 2f, paint)
                paint.color = 0xFF128807.toInt()
                canvas.drawRect(left, top + third * 2f, right, bottom, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp
                paint.color = 0xFF000088.toInt()
                canvas.drawCircle(cx, cy, h * 0.14f, paint)
            }

            KavvoroLanguage.ID -> {
                val half = top + h * 0.5f
                paint.color = 0xFFFF0000.toInt()
                canvas.drawRect(left, top, right, half, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left, half, right, bottom, paint)
            }

            KavvoroLanguage.VI -> {
                paint.color = 0xFFDA251D.toInt()
                canvas.drawRect(r, paint)
                drawSimpleStar(canvas, cx, cy, h * 0.36f, 0xFFFFFF00.toInt(), paint)
            }

            KavvoroLanguage.JA -> {
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(r, paint)
                paint.color = 0xFFBC002D.toInt()
                canvas.drawCircle(cx, cy, h * 0.30f, paint)
            }

            KavvoroLanguage.KO -> {
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(r, paint)
                val tr = h * 0.28f
                paint.color = 0xFFC60C30.toInt()
                canvas.drawArc(RectF(cx - tr, cy - tr, cx + tr, cy + tr), 180f, 180f, true, paint)
                paint.color = 0xFF003478.toInt()
                canvas.drawArc(RectF(cx - tr, cy - tr, cx + tr, cy + tr), 0f, 180f, true, paint)
                paint.color = 0xFFC60C30.toInt()
                canvas.drawCircle(cx - tr * 0.5f, cy, tr * 0.5f, paint)
                paint.color = 0xFF003478.toInt()
                canvas.drawCircle(cx + tr * 0.5f, cy, tr * 0.5f, paint)
            }

            KavvoroLanguage.CS -> {
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(left, top, right, cy, paint)
                paint.color = 0xFFD7141A.toInt()
                canvas.drawRect(left, cy, right, bottom, paint)
                tempPath.reset()
                tempPath.moveTo(left, top)
                tempPath.lineTo(cx, cy)
                tempPath.lineTo(left, bottom)
                tempPath.close()
                paint.color = 0xFF11457E.toInt()
                canvas.drawPath(tempPath, paint)
            }

            KavvoroLanguage.SV -> {
                paint.color = 0xFF006AA7.toInt()
                canvas.drawRect(r, paint)
                val barW = w * 0.18f
                val crossX = left + w * 0.38f
                paint.color = 0xFFFECC00.toInt()
                canvas.drawRect(crossX - barW * 0.5f, top, crossX + barW * 0.5f, bottom, paint)
                canvas.drawRect(left, cy - barW * 0.5f, right, cy + barW * 0.5f, paint)
            }

            KavvoroLanguage.FI -> {
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRect(r, paint)
                val barW = w * 0.20f
                val crossX = left + w * 0.38f
                paint.color = 0xFF003580.toInt()
                canvas.drawRect(crossX - barW * 0.5f, top, crossX + barW * 0.5f, bottom, paint)
                canvas.drawRect(left, cy - barW * 0.5f, right, cy + barW * 0.5f, paint)
            }

            KavvoroLanguage.TH -> {
                val h6 = h / 6f
                paint.color = 0xFFA51931.toInt()
                canvas.drawRect(left, top, right, top + h6, paint)
                paint.color = 0xFFF4F5F8.toInt()
                canvas.drawRect(left, top + h6, right, top + h6 * 2, paint)
                paint.color = 0xFF2D2A4A.toInt()
                canvas.drawRect(left, top + h6 * 2, right, top + h6 * 4, paint)
                paint.color = 0xFFF4F5F8.toInt()
                canvas.drawRect(left, top + h6 * 4, right, top + h6 * 5, paint)
                paint.color = 0xFFA51931.toInt()
                canvas.drawRect(left, top + h6 * 5, right, bottom, paint)
            }

            KavvoroLanguage.ZH -> {
                paint.color = 0xFFDE2910.toInt()
                canvas.drawRect(r, paint)
                drawSimpleStar(canvas, left + w * 0.28f, top + h * 0.36f, h * 0.22f, 0xFFFFDE00.toInt(), paint)
                drawSimpleStar(canvas, left + w * 0.52f, top + h * 0.20f, h * 0.08f, 0xFFFFDE00.toInt(), paint)
                drawSimpleStar(canvas, left + w * 0.60f, top + h * 0.34f, h * 0.08f, 0xFFFFDE00.toInt(), paint)
                drawSimpleStar(canvas, left + w * 0.60f, top + h * 0.52f, h * 0.08f, 0xFFFFDE00.toInt(), paint)
                drawSimpleStar(canvas, left + w * 0.52f, top + h * 0.66f, h * 0.08f, 0xFFFFDE00.toInt(), paint)
            }

            KavvoroLanguage.ZH_TW -> {
                paint.color = 0xFFDE2910.toInt()
                canvas.drawRect(r, paint)
                paint.color = 0xFF000095.toInt()
                canvas.drawRect(left, top, cx, cy, paint)
                paint.color = 0xFFFFFFFF.toInt()
                val sunR = h * 0.16f
                val sunCx = left + (cx - left) * 0.5f
                val sunCy = top + (cy - top) * 0.5f
                canvas.drawCircle(sunCx, sunCy, sunR, paint)
                paint.color = 0xFF000095.toInt()
                canvas.drawCircle(sunCx, sunCy, sunR * 0.75f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(sunCx, sunCy, sunR * 0.45f, paint)
            }

            KavvoroLanguage.SYSTEM -> {
                paint.color = 0xFF0A2238.toInt()
                canvas.drawRect(r, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f * dp
                paint.color = 0xFF00E5FF.toInt()
                canvas.drawCircle(cx, cy, h * 0.30f, paint)
                canvas.drawLine(left, cy, right, cy, paint)
                canvas.drawLine(cx, top, cx, bottom, paint)
            }
        }

        canvas.restore()
    }

    private fun drawSimpleStar(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int, paint: Paint) {
        val innerR = r * 0.40f
        val starPath = Path()
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) r else innerR
            val angle = i * Math.PI.toFloat() / 5f - Math.PI.toFloat() / 2f
            val x = cx + kotlin.math.cos(angle) * radius
            val y = cy + kotlin.math.sin(angle) * radius
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(starPath, paint)
    }
}
