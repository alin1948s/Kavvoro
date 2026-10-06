package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.LayoutMode
import kotlin.math.min

/**
 * Procedural brand title renderer providing authentic, crisp native Android typography
 * for Home and Settings screens, matching Option 4 exact proportions (4:1 font-size ratio).
 */
object BrandTitleRenderer {
    const val BRAND_ASPECT = 440f / 110f
    private const val MIN_TOUCH_TARGET_DP = 48f
    private val boldTypeface: Typeface? = Typeface.create("sans-serif", Typeface.BOLD)
    var customTypeface: Typeface? = null
        private set

    fun initTypeface(typeface: Typeface?) {
        customTypeface = typeface
    }

    /**
     * Computes the canonical brand wordmark placement rectangle shared between Home and Settings.
     */
    fun placement(
        width: Float,
        height: Float,
        density: Float,
        brandAspect: Float = BRAND_ASPECT
    ): LayoutRect {
        val screenWidth = width.coerceAtLeast(1f)
        val screenHeight = height.coerceAtLeast(1f)
        val safeDensity = density.coerceAtLeast(0.1f)
        val widthDp = screenWidth / safeDensity
        val heightDp = screenHeight / safeDensity

        if (screenWidth > screenHeight) {
            val scaleFactor = (heightDp / 800f).coerceIn(0.75f, 1.25f)
            val safeLeft = 24f * scaleFactor * safeDensity
            val safeRight = screenWidth - 24f * scaleFactor * safeDensity
            val headerTop = 16f * scaleFactor * safeDensity
            val actionButtonSize = (MIN_TOUCH_TARGET_DP * scaleFactor * safeDensity)
                .coerceIn(MIN_TOUCH_TARGET_DP * safeDensity, (MIN_TOUCH_TARGET_DP + 6f) * safeDensity)
            val actionGap = 10f * scaleFactor * safeDensity
            var logoWidth = (210f * scaleFactor * safeDensity).coerceIn(170f * safeDensity, 240f * safeDensity)
            val logoHeight = logoWidth / brandAspect.coerceAtLeast(0.1f)
            val settingsLeft = safeRight - actionButtonSize
            val availableHeaderSpace = (settingsLeft - actionGap) - (safeLeft + logoWidth + actionGap)
            val requiredChipsSpace = (100f + 84f + 92f + 8f * 2f) * scaleFactor * safeDensity
            if (availableHeaderSpace < requiredChipsSpace) {
                logoWidth = (175f * scaleFactor * safeDensity).coerceIn(160f * safeDensity, 190f * safeDensity)
            }
            return LayoutRect(safeLeft, headerTop, safeLeft + logoWidth, headerTop + logoHeight)
        }

        val layoutMode = when {
            widthDp <= 480f -> LayoutMode.COMPACT
            widthDp > 840f || (widthDp >= 750f && heightDp >= 1200f && safeDensity > 1.2f) -> LayoutMode.TABLET
            else -> LayoutMode.MEDIUM
        }
        val fraction = when {
            widthDp <= 480f -> 0.94f
            widthDp <= 840f -> 0.90f
            else -> 0.82f
        }
        val maxWidthPx = safeDensity * when {
            widthDp <= 480f -> 460f
            widthDp <= 840f -> 720f
            else -> 680f
        }
        val contentWidth = min(screenWidth * fraction, maxWidthPx)
        val contentLeft = (screenWidth - contentWidth) / 2f
        val scaleFactor = (heightDp / 800f).coerceIn(0.72f, 1.25f)
        val headerTop = 16f * scaleFactor * safeDensity
        val actionGap = 8f * scaleFactor * safeDensity
        val maxLogoWidth = when (layoutMode) {
            LayoutMode.COMPACT -> contentWidth * 0.44f
            LayoutMode.MEDIUM -> contentWidth * 0.40f
            LayoutMode.TABLET -> min(260f * safeDensity, contentWidth * 0.35f)
        }
        val safeLogoWidth = min(maxLogoWidth, (contentWidth * 0.50f - actionGap * 2f).coerceAtLeast(60f * safeDensity))
        val logoHeight = safeLogoWidth / brandAspect.coerceAtLeast(0.1f)
        return LayoutRect(contentLeft, headerTop, contentLeft + safeLogoWidth, headerTop + logoHeight)
    }

    fun draw(
        canvas: Canvas,
        x: Float,
        topY: Float,
        maxWidth: Float,
        targetHeight: Float,
        isRtl: Boolean,
        paint: Paint,
        textPaint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        textPaint.shader = null
        textPaint.typeface = customTypeface ?: boldTypeface
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.isAntiAlias = true
        textPaint.isSubpixelText = true

        // Scale normalized to 80dp base height (Option 4 exact 320x80 viewBox)
        val scale = targetHeight / 80f
        val titleX = x

        // 1. Eyebrow Row: Status Beacon + QUANTUM + Diamond + CHAOS (Option 4: font-size 11px)
        val dotR = 2.5f * scale
        val dotX = if (isRtl) titleX - 6f * scale else titleX + 6f * scale
        val dotY = topY + 14f * scale

        // Glowing Beacon Aura & Core
        paint.style = Paint.Style.FILL
        paint.color = 0x4000E5FF.toInt()
        canvas.drawCircle(dotX, dotY, dotR * 2.0f, paint)

        paint.color = 0xFF00E5FF.toInt()
        canvas.drawCircle(dotX, dotY, dotR, paint)

        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(dotX, dotY, dotR * 0.48f, paint)

        // Eyebrow Text (11 * scale)
        val eyebrowTextSize = 11f * scale
        textPaint.textSize = eyebrowTextSize
        textPaint.letterSpacing = 0.20f

        val startX = if (isRtl) dotX - dotR - 4f * scale else dotX + dotR + 4f * scale
        val eyebrowY = topY + 18f * scale

        // "QUANTUM" (Electric Cyan)
        textPaint.color = 0xFF00E5FF.toInt()
        val first = "QUANTUM"
        canvas.drawText(first, startX, eyebrowY, textPaint)
        val firstW = textPaint.measureText(first)

        // Diamond Divider (Gold)
        val diamondX = if (isRtl) startX - firstW - 6f * scale else startX + firstW + 6f * scale
        paint.color = 0xFFFFD700.toInt()
        canvas.drawCircle(diamondX, dotY, 2.0f * scale, paint)

        // "CHAOS" (Hot Magenta)
        textPaint.color = 0xFFFF2E93.toInt()
        val second = "CHAOS"
        val secondX = if (isRtl) diamondX - 6f * scale else diamondX + 6f * scale
        canvas.drawText(second, secondX, eyebrowY, textPaint)

        // 2. Main Title: "KAVVORO" (Option 4: font-size 44px, exactly 4.0x eyebrow!)
        val mainTextSize = 44f * scale
        textPaint.textSize = mainTextSize
        textPaint.letterSpacing = 0.10f
        val mainY = topY + 58f * scale
        val kavvoroText = fitText("KAVVORO", maxWidth)

        // Contrast / Drop Shadow
        textPaint.color = 0x9901040A.toInt()
        canvas.drawText(kavvoroText, titleX + (if (isRtl) -4f else 4f) * scale, mainY + 2.5f * scale, textPaint)

        // Crisp White Typography
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText(kavvoroText, titleX + (if (isRtl) -4f else 4f) * scale, mainY, textPaint)

        // 3. Cyber Dual-Tone Accent Rail at Base (Exact Option 4 SVG: x=4, w=138, gap=4, w=138, total=280)
        val railY = topY + 67f * scale
        val railH = (2.4f * scale).coerceAtLeast(1.5f * dp)
        val maxAvailableW = maxWidth - 8f * scale
        val targetRailW = 280f * scale
        val effectiveRailScale = if (maxAvailableW < targetRailW) (maxAvailableW / targetRailW) * scale else scale
        val segmentW = 138f * effectiveRailScale
        val railGap = 4f * effectiveRailScale

        if (segmentW > 4f * dp) {
            paint.style = Paint.Style.FILL

            if (!isRtl) {
                val railStartX = titleX + 4f * scale
                val cyanLeft = railStartX
                val cyanRight = cyanLeft + segmentW
                val magentaLeft = cyanRight + railGap
                val magentaRight = magentaLeft + segmentW

                // Cyan segment under KAV
                paint.color = 0xFF00E5FF.toInt()
                canvas.drawRoundRect(cyanLeft, railY, cyanRight, railY + railH, railH * 0.5f, railH * 0.5f, paint)

                // Magenta segment under VORO
                paint.color = 0xFFFF2E93.toInt()
                canvas.drawRoundRect(magentaLeft, railY, magentaRight, railY + railH, railH * 0.5f, railH * 0.5f, paint)
            } else {
                val railStartX = titleX - 4f * scale
                val cyanRight = railStartX
                val cyanLeft = cyanRight - segmentW
                val magentaRight = cyanLeft - railGap
                val magentaLeft = magentaRight - segmentW

                // Cyan segment
                paint.color = 0xFF00E5FF.toInt()
                canvas.drawRoundRect(cyanLeft, railY, cyanRight, railY + railH, railH * 0.5f, railH * 0.5f, paint)

                // Magenta segment
                paint.color = 0xFFFF2E93.toInt()
                canvas.drawRoundRect(magentaLeft, railY, magentaRight, railY + railH, railH * 0.5f, railH * 0.5f, paint)
            }
        }

        textPaint.letterSpacing = 0f
    }
}
