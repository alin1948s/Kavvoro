package com.moonsolstudios.kavvoro.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.i18n.HomeCopy
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.model.LayoutMode
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.BrandTitleRenderer
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.layout.LocaleLayoutPolicy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Procedural renderer for brand header and home menu screen.
 */
object HomeMenuRenderer {

    private val scratch = RectF()
    private val scratchFit = RectF()
    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val statsDockRect = RectF()
    private val statsDockShaderRect = RectF()
    private var statsDockShaderDensity = Float.NaN
    private var statsDockFillShader: LinearGradient? = null
    private var statsDockBorderShader: LinearGradient? = null
    private val gearPath = Path()
    private val cardClipPath = Path()
    private val mountainPath = Path()
    private val ridgePath = Path()
    private val chevronPath = Path()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val navTitleMeasurePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val laserPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val GEAR_ANGLE_FACTORS = floatArrayOf(-0.50f, -0.34f, -0.21f, 0.21f, 0.34f, 0.50f)
    private var cachedChevronDrawable: android.graphics.drawable.Drawable? = null
    private var cachedTrophyDrawable: android.graphics.drawable.Drawable? = null
    private var cachedNavTitleSize = Float.NaN
    private var cachedNavTitleWidth = Float.NaN
    private var cachedNavTitleHeight = Float.NaN
    private var cachedNavTitleDensity = Float.NaN
    private var cachedNavTitleSkins = ""
    private var cachedNavTitleMissions = ""
    private var cachedNavTitleLeaderboard: String? = null

    private fun sharedNavTitleSize(
        skinsTitle: String,
        missionsTitle: String,
        leaderboardTitle: String?,
        cardWidth: Float,
        cardHeight: Float,
        dp: Float
    ): Float {
        if (skinsTitle == cachedNavTitleSkins &&
            missionsTitle == cachedNavTitleMissions &&
            leaderboardTitle == cachedNavTitleLeaderboard &&
            cardWidth == cachedNavTitleWidth &&
            cardHeight == cachedNavTitleHeight &&
            dp == cachedNavTitleDensity
        ) return cachedNavTitleSize

        navTitleMeasurePaint.reset()
        navTitleMeasurePaint.isAntiAlias = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            navTitleMeasurePaint.fontVariationSettings = "'wght' 700"
        }
        navTitleMeasurePaint.typeface = AssetResourceManager.spaceGroteskBold()
        navTitleMeasurePaint.isFakeBoldText = true
        navTitleMeasurePaint.letterSpacing = 0.03f

        val baseSize = (cardHeight * 0.09f).coerceIn(17f * dp, 25f * dp)
        navTitleMeasurePaint.textSize = baseSize
        val widestTitle = maxOf(
            navTitleMeasurePaint.measureText(skinsTitle),
            navTitleMeasurePaint.measureText(missionsTitle),
            leaderboardTitle?.let(navTitleMeasurePaint::measureText) ?: 0f
        )
        val availableWidth = (cardWidth - 50f * dp).coerceAtLeast(1f * dp)
        val computedSize = if (widestTitle > availableWidth && widestTitle > 0f) {
            baseSize * (availableWidth / widestTitle)
        } else {
            baseSize
        }

        cachedNavTitleSkins = skinsTitle
        cachedNavTitleMissions = missionsTitle
        cachedNavTitleLeaderboard = leaderboardTitle
        cachedNavTitleWidth = cardWidth
        cachedNavTitleHeight = cardHeight
        cachedNavTitleDensity = dp
        cachedNavTitleSize = computedSize
        return computedSize
    }

    private fun chevronDrawable(context: Context): android.graphics.drawable.Drawable? =
        cachedChevronDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_chevron_right)?.mutate()?.also {
            cachedChevronDrawable = it
        }

    private fun trophyDrawable(context: Context): android.graphics.drawable.Drawable? =
        cachedTrophyDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_trophy)?.mutate()?.also {
            cachedTrophyDrawable = it
        }

    private fun drawBitmapAspectFit(
        canvas: Canvas,
        bitmap: Bitmap?,
        destRect: RectF,
        paint: Paint
    ) {
        if (bitmap == null || destRect.isEmpty) return
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        if (bw <= 0f || bh <= 0f) return
        val scale = min(destRect.width() / bw, destRect.height() / bh)
        val drawW = bw * scale
        val drawH = bh * scale
        val dx = destRect.centerX() - drawW * 0.5f
        val dy = destRect.centerY() - drawH * 0.5f
        scratchFit.set(dx, dy, dx + drawW, dy + drawH)
        canvas.drawBitmap(bitmap, null, scratchFit, paint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)

    private fun drawStatsDock(
        canvas: Canvas,
        rect: RectF,
        firstDividerX: Float,
        secondDividerX: Float,
        paint: Paint,
        dp: Float
    ) {
        if (rect.isEmpty) return
        val radius = rect.height() * 0.24f
        if (statsDockShaderRect.left != rect.left || statsDockShaderRect.top != rect.top ||
            statsDockShaderRect.right != rect.right || statsDockShaderRect.bottom != rect.bottom ||
            statsDockShaderDensity != dp
        ) {
            statsDockShaderRect.set(rect)
            statsDockShaderDensity = dp
            statsDockFillShader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(0xEF0A1530.toInt(), 0xF20B1027.toInt(), 0xF21A102C.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            statsDockBorderShader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                withAlpha(KavvoroPalette.cyan, 190),
                withAlpha(KavvoroPalette.purple, 190),
                Shader.TileMode.CLAMP
            )
        }
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.shader = statsDockFillShader
        canvas.drawRoundRect(rect, radius, radius, paint)

        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.15f * dp
        paint.shader = statsDockBorderShader
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x24FFFFFF
        canvas.drawLine(rect.left + radius, rect.top + 1f * dp, rect.right - radius, rect.top + 1f * dp, paint)

        // Dividers sit in the existing layout gaps, keeping the three metrics distinct inside one dock.
        paint.strokeWidth = 1f * dp
        paint.color = 0x477DA8D7
        val verticalInset = (rect.height() * 0.23f).coerceAtLeast(4f * dp)
        canvas.drawLine(firstDividerX, rect.top + verticalInset, firstDividerX, rect.bottom - verticalInset, paint)
        canvas.drawLine(secondDividerX, rect.top + verticalInset, secondDividerX, rect.bottom - verticalInset, paint)
    }

    private fun drawStatChip3D(
        canvas: Canvas,
        rect: RectF,
        iconBmp: Bitmap?,
        label: String,
        value: String,
        paint: Paint,
        dp: Float
    ) {
        val compact = rect.width() < 72f * dp

        // The compact header presents a metric row inside the shared dock instead of three tiny cards.
        val iconSize = if (compact) {
            (rect.height() * 0.30f).coerceIn(9f * dp, 13f * dp)
        } else {
            (rect.height() * 0.72f).coerceIn(20f * dp, 36f * dp)
        }
        val iconLeft = rect.left + if (compact) 4f * dp else 7f * dp
        val iconTop = if (compact) rect.top + 3f * dp else rect.centerY() - iconSize * 0.5f
        if (iconBmp != null) {
            paint.alpha = 255
            paint.isFilterBitmap = true
            scratchRect.set(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
            drawBitmapAspectFit(canvas, iconBmp, scratchRect, paint)
        }

        // 5. Draw text centered horizontally in the right text zone
        val textCenterX = (iconLeft + iconSize + rect.right) * 0.5f
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 500"
        }
        textPaint.typeface = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            AssetResourceManager.oxaniumMedium()
        } else {
            AssetResourceManager.oxaniumBold()
        }

        if (compact) {
            val textLeft = iconLeft + iconSize + 3f * dp
            val maxTextWidth = (rect.right - 3f * dp - textLeft).coerceAtLeast(1f * dp)
            val labelText = label.uppercase()
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.letterSpacing = 0f
            var labelSize = 6f * dp
            textPaint.textSize = labelSize
            while (labelSize > 5f * dp && textPaint.measureText(labelText) > maxTextWidth) {
                labelSize -= 0.2f * dp
                textPaint.textSize = labelSize
            }
            textPaint.color = 0xFFB9CBE7.toInt()
            canvas.drawText(labelText, textLeft, rect.top + 11f * dp, textPaint)

            var valueSize = (rect.height() * 0.34f).coerceIn(10f * dp, 14f * dp)
            textPaint.textSize = valueSize
            textPaint.textAlign = Paint.Align.CENTER
            while (valueSize > 8f * dp && textPaint.measureText(value) > rect.width() - 7f * dp) {
                valueSize -= 0.4f * dp
                textPaint.textSize = valueSize
            }
            textPaint.color = Color.WHITE
            textPaint.setShadowLayer(3f * dp, 0f, 0f, 0x6600DFFF)
            canvas.drawText(value, rect.centerX(), rect.bottom - 4f * dp, textPaint)
            textPaint.clearShadowLayer()
            textPaint.letterSpacing = 0f
            return
        }

        // Label
        textPaint.textSize = (rect.height() * 0.24f).coerceIn(8.5f * dp, 11.5f * dp)
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.08f
        val labelY = rect.centerY() - 2.5f * dp
        canvas.drawText(label.uppercase(), textCenterX, labelY, textPaint)
        textPaint.letterSpacing = 0f

        // Value
        textPaint.textSize = (rect.height() * 0.44f).coerceIn(14f * dp, 20f * dp)
        textPaint.color = 0xFFFFFFFF.toInt()
        val valueY = rect.centerY() + (textPaint.textSize * 0.78f)
        canvas.drawText(value, textCenterX, valueY, textPaint)
    }

    private fun drawDailyReadyBadge(
        canvas: Canvas,
        badgeRect: RectF,
        dp: Float,
        stateElapsed: Float,
        t: (String) -> String
    ) {
        if (badgeRect.isEmpty) return
        val readyText = t("READY").uppercase()
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = 9f * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.12f
        textPaint.textAlign = Paint.Align.LEFT
        val maxTextW = badgeRect.width() - 22f * dp
        while (textPaint.textSize > 6.5f * dp && textPaint.measureText(readyText) > maxTextW) {
            textPaint.textSize -= 0.5f * dp
        }
        val badgeH = badgeRect.height()

        val pulse = (sin(stateElapsed * 4.0f) * 0.5f + 0.5f)
        badgePaint.reset()
        badgePaint.isAntiAlias = true
        badgePaint.color = 0xEE091428.toInt()
        badgePaint.style = Paint.Style.FILL
        canvas.drawRoundRect(badgeRect, badgeH * 0.5f, badgeH * 0.5f, badgePaint)

        badgePaint.color = withAlpha(KavvoroPalette.cyan, (140 + pulse * 115).toInt())
        badgePaint.style = Paint.Style.STROKE
        badgePaint.strokeWidth = 1.5f * dp
        canvas.drawRoundRect(badgeRect, badgeH * 0.5f, badgeH * 0.5f, badgePaint)

        badgePaint.color = KavvoroPalette.cyan
        badgePaint.style = Paint.Style.FILL
        val dotCx = badgeRect.left + 10f * dp
        val dotCy = badgeRect.centerY()
        val dotRadius = 3f * dp
        canvas.drawCircle(dotCx, dotCy, dotRadius, badgePaint)

        val textY = badgeRect.centerY() + 3.2f * dp
        canvas.drawText(readyText, dotCx + 6f * dp, textY, textPaint)
        textPaint.letterSpacing = 0f
    }

    private fun drawLandscapeLeaderboardCard(
        canvas: Canvas,
        rect: RectF,
        bitmap: Bitmap?,
        active: Boolean,
        paint: Paint,
        dp: Float,
        context: Context,
        showSubtitle: Boolean = true
    ) {
        if (rect.isEmpty) return
        canvas.save()
        if (active) {
            canvas.scale(0.98f, 0.98f, rect.centerX(), rect.centerY())
        }
        val radius = 18f * dp

        // 1. Dark Cyber Card Chassis Fill: dark navy -> transparent space blue
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            0xF60A1226.toInt(), 0xEE0D1B36.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)

        // 2. Subtle cosmic/mountain backdrop inside the card (clipped to rounded frame)
        canvas.save()
        cardClipPath.rewind()
        cardClipPath.addRoundRect(rect, radius, radius, Path.Direction.CW)
        canvas.clipPath(cardClipPath)

        // Cosmic backdrop gradient ridges
        mountainPath.rewind()
        val baseY = rect.bottom
        mountainPath.moveTo(rect.left, baseY)
        mountainPath.lineTo(rect.left, baseY - rect.height() * 0.38f)
        mountainPath.lineTo(rect.left + rect.width() * 0.28f, baseY - rect.height() * 0.58f)
        mountainPath.lineTo(rect.left + rect.width() * 0.46f, baseY - rect.height() * 0.32f)
        mountainPath.lineTo(rect.left + rect.width() * 0.70f, baseY - rect.height() * 0.62f)
        mountainPath.lineTo(rect.left + rect.width() * 0.88f, baseY - rect.height() * 0.40f)
        mountainPath.lineTo(rect.right, baseY - rect.height() * 0.50f)
        mountainPath.lineTo(rect.right, baseY)
        mountainPath.close()
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.centerX(), rect.centerY(),
            rect.centerX(), rect.bottom,
            0x2600E5FF.toInt(), 0x08142850.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(mountainPath, paint)

        ridgePath.rewind()
        ridgePath.moveTo(rect.left, baseY)
        ridgePath.lineTo(rect.left, baseY - rect.height() * 0.22f)
        ridgePath.lineTo(rect.left + rect.width() * 0.35f, baseY - rect.height() * 0.34f)
        ridgePath.lineTo(rect.left + rect.width() * 0.60f, baseY - rect.height() * 0.18f)
        ridgePath.lineTo(rect.left + rect.width() * 0.82f, baseY - rect.height() * 0.36f)
        ridgePath.lineTo(rect.right, baseY - rect.height() * 0.24f)
        ridgePath.lineTo(rect.right, baseY)
        ridgePath.close()
        paint.shader = LinearGradient(
            rect.centerX(), rect.centerY(),
            rect.centerX(), rect.bottom,
            0x32FFD54F.toInt(), 0x04081020.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(ridgePath, paint)
        paint.shader = null
        canvas.restore()

        // 3. Glowing Gold Border #FFD54F, 2dp
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.0f * dp
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.top,
            0xFFFFD54F.toInt(), withAlpha(KavvoroPalette.gold, 204),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null

        // 4. 3D Trophy Artwork on the left: compact ~72% height, vertically centered (Point 8)
        val artH = rect.height() * 0.72f
        val artW = if (bitmap != null && bitmap.height > 0) {
            artH * (bitmap.width.toFloat() / bitmap.height.toFloat())
        } else {
            artH
        }
        val artPad = 12f * dp
        scratchRect.set(
            rect.left + artPad,
            rect.centerY() - artH * 0.5f,
            rect.left + artPad + artW,
            rect.centerY() + artH * 0.5f
        )

        // Radial golden glow behind trophy
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            scratchRect.centerX(), scratchRect.centerY(), scratchRect.width() * 0.68f,
            intArrayOf(withAlpha(KavvoroPalette.gold, 128), 0x30FFD54F.toInt(), withAlpha(KavvoroPalette.gold, 0)),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(scratchRect.centerX(), scratchRect.centerY(), scratchRect.width() * 0.68f, paint)
        paint.shader = null

        if (bitmap != null) {
            paint.alpha = 255
            paint.isFilterBitmap = true
            canvas.drawBitmap(bitmap, null, scratchRect, paint)
        } else {
            // Dark amber disc base fallback
            val emblemRadius = artH * 0.44f
            val emblemCx = scratchRect.centerX()
            val emblemCy = scratchRect.centerY()
            paint.shader = LinearGradient(
                emblemCx, emblemCy - emblemRadius,
                emblemCx, emblemCy + emblemRadius,
                0xFF3E2005.toInt(),
                0xFF180A01.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(emblemCx, emblemCy, emblemRadius, paint)

            // Glowing gold rim ring
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.0f * dp
            paint.shader = LinearGradient(
                emblemCx, emblemCy - emblemRadius,
                emblemCx, emblemCy + emblemRadius,
                0xFFFFE082.toInt(),
                KavvoroPalette.gold,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(emblemCx, emblemCy, emblemRadius, paint)
            paint.shader = null

            val trophy = trophyDrawable(context)
            if (trophy != null) {
                val tSize = (emblemRadius * 1.50f).toInt()
                trophy.setBounds(
                    (emblemCx - tSize * 0.5f).toInt(),
                    (emblemCy - tSize * 0.5f).toInt(),
                    (emblemCx + tSize * 0.5f).toInt(),
                    (emblemCy + tSize * 0.5f).toInt()
                )
                trophy.setTint(0xFFFFD54F.toInt())
                trophy.draw(canvas)
            }
        }

        // 5. Localized Title and Subtitle in Center: Space Grotesk Bold title, Oxanium subtitle
        val textLeft = scratchRect.right + 12f * dp
        val rightMargin = 36f * dp
        val maxTextW = (rect.right - rightMargin - textLeft).coerceAtLeast(10f)

        textPaint.reset()
        textPaint.isAntiAlias = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 700"
        }
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.isFakeBoldText = true
        var titleSize = rect.height() * 0.20f
        textPaint.textSize = titleSize
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.04f
        textPaint.textAlign = Paint.Align.LEFT

        val title = HomeCopy.leaderboardTitle(context).uppercase()
        val measuredTitleW = textPaint.measureText(title)
        if (measuredTitleW > maxTextW && measuredTitleW > 0f) {
            textPaint.textSize = titleSize * (maxTextW / measuredTitleW)
        }

        if (showSubtitle) {
            val titleCenterY = rect.centerY() - rect.height() * 0.10f
            val titleFm = textPaint.fontMetrics
            val titleBaseline = titleCenterY - (titleFm.ascent + titleFm.descent) / 2f
            canvas.drawText(title, textLeft, titleBaseline, textPaint)

            // Subtitle (Oxanium Regular)
            textPaint.reset()
            textPaint.isAntiAlias = true
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                textPaint.fontVariationSettings = "'wght' 400"
            }
            textPaint.typeface = AssetResourceManager.oxaniumNormal()
            textPaint.isFakeBoldText = false
            var subSize = (11f * dp).coerceIn(9f * dp, 12f * dp)
            textPaint.textSize = subSize
            textPaint.color = 0xFFB0CDE8.toInt()
            textPaint.letterSpacing = 0.04f
            val subtitle = HomeCopy.leaderboardSubtitle(context)
            val measuredSubW = textPaint.measureText(subtitle)
            if (measuredSubW > maxTextW && measuredSubW > 0f) {
                textPaint.textSize = subSize * (maxTextW / measuredSubW)
            }
            val subCenterY = rect.centerY() + rect.height() * 0.16f
            val subFm = textPaint.fontMetrics
            val subBaseline = subCenterY - (subFm.ascent + subFm.descent) / 2f
            canvas.drawText(subtitle, textLeft, subBaseline, textPaint)
        } else {
            val titleFm = textPaint.fontMetrics
            val titleBaseline = rect.centerY() - (titleFm.ascent + titleFm.descent) / 2f
            canvas.drawText(title, textLeft, titleBaseline, textPaint)
        }

        // 6. Chevron > on the right
        val chevronX = rect.right - 22f * dp
        val chevronY = rect.centerY()
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = 0xFFFFD54F.toInt()
        val chevHalf = 6f * dp
        chevronPath.rewind()
        chevronPath.moveTo(chevronX - chevHalf * 0.5f, chevronY - chevHalf)
        chevronPath.lineTo(chevronX + chevHalf * 0.5f, chevronY)
        chevronPath.lineTo(chevronX - chevHalf * 0.5f, chevronY + chevHalf)
        canvas.drawPath(chevronPath, paint)

        canvas.restore()
    }

    private fun drawLeftSciFiWatermark(canvas: Canvas, calculator: HomeLayoutCalculator, context: Context, dp: Float) {
        if (calculator.landscapeClass == null) return
        val left = calculator.heroStageRect.left + 8f * dp
        val right = calculator.heroStageRect.right - 8f * dp
        val top = calculator.heroStageRect.top + calculator.heroStageRect.height() * 0.22f
        val language = KavvoroI18n.active(context)
        val isRtl = LocaleLayoutPolicy.isRtl(language)
        val textX = if (isRtl) right else left
        val maxTextWidth = calculator.heroStageRect.width() * 0.42f

        // Glowing cyan accent line above watermark
        val lineWidth = min(32f * dp, maxTextWidth * 0.35f)
        badgePaint.reset()
        badgePaint.isAntiAlias = true
        badgePaint.style = Paint.Style.STROKE
        badgePaint.strokeWidth = 2.2f * dp
        badgePaint.strokeCap = Paint.Cap.ROUND
        badgePaint.color = KavvoroPalette.cyan
        if (isRtl) {
            canvas.drawLine(right - lineWidth, top, right, top, badgePaint)
        } else {
            canvas.drawLine(left, top, left + lineWidth, top, badgePaint)
        }

        // Text lines (Oxanium Regular)
        textPaint.reset()
        textPaint.isAntiAlias = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 400"
        }
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.textSize = 9.5f * dp
        textPaint.color = KavvoroPalette.mutedText
        textPaint.alpha = 204
        textPaint.letterSpacing = 0.10f
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT

        var curY = top + 15f * dp
        val lineSpacing = 14f * dp
        val lines = LocaleLayoutPolicy.wrapText(HomeCopy.landscapeWatermark(context), maxTextWidth) {
            textPaint.measureText(it)
        }
        for (line in lines) {
            if (isRtl) {
                canvas.drawTextRun(line, 0, line.length, 0, line.length, textX, curY, true, textPaint)
            } else {
                canvas.drawText(line, textX, curY, textPaint)
            }
            curY += lineSpacing
        }
        textPaint.alpha = 255
        textPaint.letterSpacing = 0f
    }

    private fun drawHomeFooterBrandAccent(canvas: Canvas, rect: RectF, dp: Float, t: (String) -> String) {
        if (rect.isEmpty) return
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 400"
        }
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.textSize = (rect.height() * 0.68f).coerceIn(8.5f * dp, 11f * dp)
        textPaint.color = 0xFF7FB3EE.toInt()
        textPaint.letterSpacing = 0.30f

        val mottoText = t("SMALL MINDS BIG WORLDS").uppercase()
        val textWidth = textPaint.measureText(mottoText)
        val cy = rect.centerY()

        // Draw centered motto
        canvas.drawText(mottoText, rect.centerX(), cy + textPaint.textSize * 0.35f, textPaint)

        // Draw glowing cyan laser accent lines on left and right
        val lineWidth = 36f * dp
        val lineGap = 16f * dp
        val leftLineEnd = rect.centerX() - textWidth * 0.5f - lineGap
        val leftLineStart = leftLineEnd - lineWidth
        val rightLineStart = rect.centerX() + textWidth * 0.5f + lineGap
        val rightLineEnd = rightLineStart + lineWidth

        laserPaint.reset()
        laserPaint.isAntiAlias = true
        laserPaint.style = Paint.Style.STROKE
        laserPaint.strokeCap = Paint.Cap.ROUND
        laserPaint.strokeWidth = 2.2f * dp

        // Left laser line gradient (fade out on outer end, bright cyan towards text)
        laserPaint.shader = LinearGradient(
            leftLineStart, cy, leftLineEnd, cy,
            0x0000E5FF.toInt(), 0xFF00F0FF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(leftLineStart, cy, leftLineEnd, cy, laserPaint)

        // Right laser line gradient (bright cyan near text, fade out on outer end)
        laserPaint.shader = LinearGradient(
            rightLineStart, cy, rightLineEnd, cy,
            0xFF00F0FF.toInt(), 0x0000E5FF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(rightLineStart, cy, rightLineEnd, cy, laserPaint)
        laserPaint.shader = null
        textPaint.letterSpacing = 0f
    }

    private fun drawHeader(
        canvas: Canvas,
        calculator: HomeLayoutCalculator,
        activeMenuButton: MenuButton,
        bestStreak: Int,
        currentLevel: Int,
        hypeBalance: Int,
        dailyReady: Boolean,
        stateElapsed: Float,
        context: Context,
        paint: Paint,
        dp: Float,
        worldBitmap: (String) -> Bitmap?,
        formatHypeAmount: (Int) -> String,
        t: (String) -> String
    ) {
        // 1. Brand Logo
        calculator.brandRect.toRectF(scratch)
        val logoBmp = worldBitmap("kavvoro_logo")
        if (logoBmp != null) {
            paint.alpha = 255
            paint.isFilterBitmap = true
            drawBitmapAspectFit(canvas, logoBmp, scratch, paint)
        } else {
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textSize = scratch.height() * 0.7f
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(t("Kavvoro").uppercase(), scratch.left, scratch.bottom - 4f * dp, textPaint)
        }

        // 2. Brand Motto ("SMALL MINDS BIG WORLDS")
        calculator.brandMottoRect.toRectF(scratch)
        textPaint.reset()
        textPaint.isAntiAlias = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 400"
        }
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.textSize = 8.5f * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.letterSpacing = 0.32f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(HomeCopy.brandMotto(context).uppercase(), scratch.centerX(), scratch.centerY() + textPaint.textSize * 0.38f, textPaint)
        textPaint.letterSpacing = 0f

        // 3. Three Stat Chips (Streak, Level, Coins)
        statsDockRect.set(
            calculator.streakChipRect.left,
            calculator.streakChipRect.top,
            calculator.coinsChipRect.right,
            calculator.streakChipRect.bottom
        )
        val firstDividerX = (calculator.streakChipRect.right + calculator.levelChipRect.left) * 0.5f
        val secondDividerX = (calculator.levelChipRect.right + calculator.coinsChipRect.left) * 0.5f
        drawStatsDock(canvas, statsDockRect, firstDividerX, secondDividerX, paint, dp)

        calculator.streakChipRect.toRectF(scratch)
        drawStatChip3D(
            canvas = canvas,
            rect = scratch,
            iconBmp = worldBitmap("ic_stat_flame_3d"),
            label = HomeCopy.streak(context),
            value = bestStreak.toString(),
            paint = paint,
            dp = dp
        )

        calculator.levelChipRect.toRectF(scratch)
        drawStatChip3D(
            canvas = canvas,
            rect = scratch,
            iconBmp = worldBitmap("ic_stat_star_3d"),
            label = HomeCopy.level(context),
            value = currentLevel.toString(),
            paint = paint,
            dp = dp
        )

        calculator.coinsChipRect.toRectF(scratch)
        drawStatChip3D(
            canvas = canvas,
            rect = scratch,
            iconBmp = worldBitmap("ic_stat_coin_3d"),
            label = HomeCopy.coins(context),
            value = formatHypeAmount(hypeBalance),
            paint = paint,
            dp = dp
        )

        if (dailyReady) {
            calculator.coinsReadyBadgeRect.toRectF(scratchRect2)
            drawDailyReadyBadge(canvas, scratchRect2, dp, stateElapsed, t)
        }

        // 4. Settings Button
        calculator.settingsButtonRect.toRectF(scratch)
        val settingsBmp = worldBitmap("btn_settings_3d")
        if (settingsBmp != null) {
            canvas.save()
            if (activeMenuButton == MenuButton.SETTINGS) {
                canvas.scale(0.94f, 0.94f, scratch.centerX(), scratch.centerY())
            }
            paint.alpha = 255
            paint.isFilterBitmap = true
            drawBitmapAspectFit(canvas, settingsBmp, scratch, paint)
            canvas.restore()
        } else {
            drawSettingsButton(
                canvas = canvas,
                rect = scratch,
                active = activeMenuButton == MenuButton.SETTINGS,
                context = context,
                paint = paint,
                dp = dp
            )
        }
    }

    fun drawGearIcon(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, paint: Paint, dp: Float) {
        val safeRadius = radius.coerceAtLeast(6f * dp)
        val outerRadius = safeRadius
        val rootRadius = outerRadius * 0.73f
        val shoulderRadius = outerRadius * 0.86f
        val teeth = 8
        val sector = (2.0 * PI) / teeth

        gearPath.rewind()
        var firstPoint = true

        for (tooth in 0 until teeth) {
            val centerAngle = -PI / 2.0 + tooth * sector
            for (ptIdx in 0 until 6) {
                val angleFactor = GEAR_ANGLE_FACTORS[ptIdx]
                val r = when (ptIdx) {
                    0, 5 -> rootRadius
                    1, 4 -> shoulderRadius
                    else -> outerRadius
                }
                val angle = centerAngle + sector * angleFactor
                val x = cx + cos(angle).toFloat() * r
                val y = cy + sin(angle).toFloat() * r
                if (firstPoint) {
                    gearPath.moveTo(x, y)
                    firstPoint = false
                } else {
                    gearPath.lineTo(x, y)
                }
            }
        }
        gearPath.close()

        val strokeWidth = (2f * dp).coerceAtLeast(1.5f)
        paint.shader = null
        paint.maskFilter = null
        paint.colorFilter = null
        paint.pathEffect = null
        paint.xfermode = null
        paint.alpha = 255
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color

        canvas.drawPath(gearPath, paint)
        canvas.drawCircle(cx, cy, (outerRadius * 0.27f).coerceAtLeast(1.5f * dp), paint)
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
    }

    fun drawSettingsButton(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        context: Context,
        paint: Paint,
        dp: Float
    ) {
        val radius = 14f * dp
        val pressScale = if (active) 0.94f else 1.0f

        canvas.save()
        canvas.scale(pressScale, pressScale, rect.centerX(), rect.centerY())

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = if (active) 0xEE080E28.toInt() else KavvoroPalette.panel
        canvas.drawRoundRect(rect, radius, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f * dp
        paint.color = if (active) KavvoroPalette.cyan else withAlpha(KavvoroPalette.cyan, 153)
        canvas.drawRoundRect(rect, radius, radius, paint)

        val iconColor = if (active) KavvoroPalette.cyan else Color.WHITE
        drawGearIcon(canvas, rect.centerX(), rect.centerY(), rect.width() * 0.26f, iconColor, paint, dp)

        canvas.restore()
    }

    fun drawNavCard(
        canvas: Canvas,
        rect: RectF,
        title: String,
        subtitle: String,
        showSubtitle: Boolean,
        iconRes: Int,
        accentColor: Int,
        active: Boolean,
        pulseGlow: Boolean,
        stateElapsed: Float,
        context: Context,
        paint: Paint,
        dp: Float,
        titleTextSize: Float,
        artBitmap: Bitmap? = null
    ) {
        val radius = 22f * dp
        val pressScale = if (active) 0.97f else 1.0f

        canvas.save()
        canvas.scale(pressScale, pressScale, rect.centerX(), rect.centerY())

        val baseGlowAlpha = if (active) 130 else if (pulseGlow) (150 + 40 * sin(stateElapsed * 4.0)).toInt() else 180
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 2.2f * dp else 1.6f * dp
        paint.color = withAlpha(accentColor, baseGlowAlpha)
        canvas.drawRoundRect(rect, radius, radius, paint)

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.centerX(), rect.top,
            rect.centerX(), rect.bottom,
            intArrayOf(
                withAlpha(accentColor, 48),
                0xF20C1634.toInt(),
                0xFC050B20.toInt()
            ),
            floatArrayOf(0f, 0.40f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x40FFFFFF
        canvas.drawLine(rect.left + radius, rect.top + 1f * dp, rect.right - radius, rect.top + 1f * dp, paint)

        val artTop = rect.top + rect.height() * 0.06f
        val artBottom = rect.top + rect.height() * 0.56f
        val artBoxH = artBottom - artTop
        val artBoxW = rect.width() - 16f * dp

        val titleCenterY = if (showSubtitle && subtitle.isNotBlank()) {
            rect.top + rect.height() * 0.70f
        } else {
            rect.top + rect.height() * 0.76f
        }
        val subtitleCenterY = rect.top + rect.height() * 0.83f

        if (artBitmap != null) {
            paint.reset()
            paint.isAntiAlias = true
            paint.alpha = 255
            paint.isFilterBitmap = true
            val bw = artBitmap.width.toFloat()
            val bh = artBitmap.height.toFloat()
            if (bw > 0f && bh > 0f) {
                val scale = min(artBoxW / bw, artBoxH / bh)
                val dw = bw * scale
                val dh = bh * scale
                val dx = rect.centerX() - dw * 0.5f
                val dy = artTop + (artBoxH - dh) * 0.5f
                scratchRect.set(dx, dy, dx + dw, dy + dh)
                canvas.drawBitmap(artBitmap, null, scratchRect, paint)

                paint.color = 0x20000000
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)
            }
        } else {
            val iconSize = (if (showSubtitle) 42f else 38f) * dp
            val iconCx = rect.centerX()
            val iconTop = artTop + (artBoxH - iconSize) * 0.5f
            val drawable = ContextCompat.getDrawable(context, iconRes)
            if (drawable != null) {
                drawable.setBounds(
                    (iconCx - iconSize * 0.5f).toInt(),
                    iconTop.toInt(),
                    (iconCx + iconSize * 0.5f).toInt(),
                    (iconTop + iconSize).toInt()
                )
                drawable.draw(canvas)
            }
        }

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 700"
        }
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.isFakeBoldText = true
        textPaint.color = Color.WHITE
        textPaint.textSize = titleTextSize
        textPaint.letterSpacing = 0.03f
        val chevronSize = 14f * dp
        val titleChevronGap = 8f * dp
        val titleWidth = textPaint.measureText(title)
        val titleGroupWidth = titleWidth + titleChevronGap + chevronSize
        val titleGroupLeft = rect.centerX() - titleGroupWidth * 0.5f
        val titleCenterX = titleGroupLeft + titleWidth * 0.5f
        val titleFm = textPaint.fontMetrics
        val titleBaseline = titleCenterY - (titleFm.ascent + titleFm.descent) / 2f
        canvas.drawText(title, titleCenterX, titleBaseline, textPaint)

        // Keep the chevron attached to the localized title, with one consistent gap.
        val chevronLeft = titleGroupLeft + titleWidth + titleChevronGap
        val chevronTop = titleCenterY - chevronSize * 0.5f
        chevronDrawable(context)?.let { chevron ->
            chevron.setBounds(
                chevronLeft.toInt(),
                chevronTop.toInt(),
                (chevronLeft + chevronSize).toInt(),
                (chevronTop + chevronSize).toInt()
            )
            chevron.setTint(withAlpha(accentColor, 220))
            chevron.draw(canvas)
        }

        if (showSubtitle && subtitle.isNotBlank()) {
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                textPaint.fontVariationSettings = "'wght' 400"
            }
            textPaint.typeface = AssetResourceManager.oxaniumNormal()
            textPaint.isFakeBoldText = false
            textPaint.color = KavvoroPalette.mutedText
            textPaint.textSize = (rect.height() * 0.043f).coerceIn(9f * dp, 12f * dp)
            textPaint.letterSpacing = 0.04f
            val maxSubW = rect.width() - 16f * dp
            val measuredSubW = textPaint.measureText(subtitle)
            if (measuredSubW > maxSubW && measuredSubW > 0f) {
                textPaint.textSize = textPaint.textSize * (maxSubW / measuredSubW)
            }
            val subFm = textPaint.fontMetrics
            val subBaseline = subtitleCenterY - (subFm.ascent + subFm.descent) / 2f
            canvas.drawText(subtitle, rect.centerX(), subBaseline, textPaint)
            textPaint.letterSpacing = 0f
        }

        canvas.restore()
    }

    private fun drawHomeCelestialDecor(
        canvas: Canvas,
        calculator: HomeLayoutCalculator,
        paint: Paint,
        worldBitmap: (String) -> Bitmap?
    ) {
        paint.isFilterBitmap = true

        worldBitmap("home_planet_blue")?.let { bitmap ->
            calculator.planetBlueRect.toRectF(scratch)
            paint.alpha = 235
            drawBitmapAspectFit(
                canvas,
                bitmap,
                scratch,
                paint
            )
        }

        worldBitmap("home_planet_pink")?.let { bitmap ->
            calculator.planetPinkRect.toRectF(scratch)
            paint.alpha = 225
            drawBitmapAspectFit(
                canvas,
                bitmap,
                scratch,
                paint
            )
        }

        if (!calculator.reduceDecor) {
            worldBitmap("asteroid_cluster_left")?.let { bitmap ->
                calculator.asteroidLeftRect.toRectF(scratch)
                paint.alpha = 160
                drawBitmapAspectFit(canvas, bitmap, scratch, paint)
            }
            worldBitmap("asteroid_cluster_right")?.let { bitmap ->
                calculator.asteroidRightRect.toRectF(scratch)
                paint.alpha = 160
                drawBitmapAspectFit(canvas, bitmap, scratch, paint)
            }
        }

        paint.alpha = 255
    }

    fun drawMenuScreen(
        canvas: Canvas,
        menuState: MenuState,
        calculator: HomeLayoutCalculator,
        activeMenuButton: MenuButton,
        bestStreak: Int,
        currentLevel: Int,
        hypeBalance: Int,
        dailyReady: Boolean,
        stateElapsed: Float,
        context: Context,
        paint: Paint,
        dp: Float,
        worldBitmap: (String) -> Bitmap?,
        formatHypeAmount: (Int) -> String,
        t: (String) -> String,
        drawPlayModeScreen: (Canvas, Float, Float, Float) -> Unit
    ) {
        if (menuState == MenuState.MODES) {
            // 1. CELESTIAL DECOR (Planets)
            drawHomeCelestialDecor(
                canvas = canvas,
                calculator = calculator,
                paint = paint,
                worldBitmap = worldBitmap
            )

            // 2. LEFT SCI-FI WATERMARK TEXT
            drawLeftSciFiWatermark(canvas, calculator, context, dp)

            // 3. PORTAL BEAM (Both Portrait & Landscape, behind Brainball)
            val beamBmp = worldBitmap("portal_beam")
            if (beamBmp != null) {
                calculator.portalBeamRect.toRectF(scratch)
                val beamAlpha = (200 + 40 * sin(stateElapsed * 2.2f)).toInt().coerceIn(150, 255)
                paint.alpha = beamAlpha
                paint.isFilterBitmap = true
                canvas.drawBitmap(beamBmp, null, scratch, paint)
                paint.alpha = 255
            }

            // Strong radiant neon glow under Brainball (Point 10)
            val heroCx = calculator.platformRect.centerX()
            val platformTop = calculator.platformRect.top
            val platformHeight = calculator.platformRect.height()
            val glowRadius = calculator.platformRect.width() * 0.55f
            paint.shader = RadialGradient(
                heroCx, platformTop + platformHeight * 0.15f, glowRadius,
                intArrayOf(0xE600E5FF.toInt(), 0x88FF2E93.toInt(), 0x00000000),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(heroCx, platformTop + platformHeight * 0.15f, glowRadius, paint)
            paint.shader = null

            // 4. HERO MASCOT (Brainball - drawn BEFORE platform disc so it appears to emerge from portal)
            val mascotBmp = worldBitmap("brainball_main")
            val floatY = sin(stateElapsed * 2.4f) * 5f * dp
            calculator.heroMascotRect.toRectF(scratch)
            scratch.offset(0f, floatY)
            if (mascotBmp != null) {
                paint.alpha = 255
                paint.isFilterBitmap = true
                drawBitmapAspectFit(canvas, mascotBmp, scratch, paint)
            }

            // 5. 3D CYBER PORTAL DISC (Platform beneath Brainball, overlapping its base)
            val platformBmp = worldBitmap("home_portal_disc")
            if (platformBmp != null) {
                calculator.platformRect.toRectF(scratch)
                paint.alpha = 255
                paint.isFilterBitmap = true
                drawBitmapAspectFit(canvas, platformBmp, scratch, paint)
            }

            // 6. PLAY NOW CTA BUTTON (procedural gradient with localized runtime text)
            calculator.playCtaRect.toRectF(scratch)
            val isPlayActive = activeMenuButton == MenuButton.PLAY
            SciFiCtaButtonRenderer.draw(
                canvas = canvas,
                rect = scratch,
                active = isPlayActive,
                density = calculator.density,
                context = context,
                playTitle = HomeCopy.ctaPlay(context),
                playTitleShort = HomeCopy.ctaPlayShort(context),
                playSubtitle = HomeCopy.ctaSubtitle(context),
                showSubtitle = calculator.showPlaySubtitle
            )

            // 7. NAVIGATION CARDS (Localized via drawNavCard)
            val skinsTitle = HomeCopy.skinsTitle(context)
            val missionsTitle = HomeCopy.missionsTitle(context)
            val leaderboardTitle = HomeCopy.leaderboardTitle(context)
            val leaderboardUsesStandardCard =
                calculator.landscapeClass == null || calculator.landscapeClass == LandscapeClass.WIDE
            val sharedTitleWidth = if (leaderboardUsesStandardCard) {
                minOf(
                    calculator.skinsCardRect.width(),
                    calculator.missionsCardRect.width(),
                    calculator.leaderboardCardRect.width()
                )
            } else {
                minOf(calculator.skinsCardRect.width(), calculator.missionsCardRect.width())
            }
            val sharedTitleHeight = if (leaderboardUsesStandardCard) {
                minOf(
                    calculator.skinsCardRect.height(),
                    calculator.missionsCardRect.height(),
                    calculator.leaderboardCardRect.height()
                )
            } else {
                minOf(calculator.skinsCardRect.height(), calculator.missionsCardRect.height())
            }
            val navCardTitleSize = sharedNavTitleSize(
                skinsTitle = skinsTitle,
                missionsTitle = missionsTitle,
                leaderboardTitle = leaderboardTitle.takeIf { leaderboardUsesStandardCard },
                cardWidth = sharedTitleWidth,
                cardHeight = sharedTitleHeight,
                dp = dp
            )

            if (calculator.landscapeClass != null) {
                // Subtle readability dark scrim behind navigation deck
                paint.reset()
                paint.isAntiAlias = true
                paint.style = Paint.Style.FILL
                calculator.navigationDeckRect.toRectF(scratch)
                scratch.inset(-16f * dp, -16f * dp)
                paint.shader = RadialGradient(
                    scratch.centerX(),
                    scratch.centerY(),
                    scratch.width() * 0.75f,
                    0x38050C1A.toInt(),
                    0x00050C1A.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratch, 28f * dp, 28f * dp, paint)
                paint.shader = null
            }

            // 7a. Skins Card (Dynamic localized text + rich Brainball art)
            calculator.skinsCardRect.toRectF(scratch)
            val skinsArtBmp = worldBitmap("home_card_art_skins")
            drawNavCard(
                canvas = canvas,
                rect = scratch,
                title = skinsTitle,
                subtitle = HomeCopy.skinsSubtitle(context),
                showSubtitle = calculator.showCardSubtitles,
                iconRes = R.drawable.ic_skins,
                accentColor = KavvoroPalette.pink,
                active = activeMenuButton == MenuButton.COLLECTION,
                pulseGlow = false,
                stateElapsed = stateElapsed,
                context = context,
                paint = paint,
                dp = dp,
                titleTextSize = navCardTitleSize,
                artBitmap = skinsArtBmp
            )

            // 7b. Missions Card (Dynamic localized text + Neon compass art + Pulsing glow/badge)
            calculator.missionsCardRect.toRectF(scratch)
            val missionsArtBmp = worldBitmap("home_card_art_missions")
            drawNavCard(
                canvas = canvas,
                rect = scratch,
                title = missionsTitle,
                subtitle = HomeCopy.missionsSubtitle(context),
                showSubtitle = calculator.showCardSubtitles,
                iconRes = R.drawable.ic_missions,
                accentColor = KavvoroPalette.cyan,
                active = activeMenuButton == MenuButton.MISSIONS,
                pulseGlow = false,
                stateElapsed = stateElapsed,
                context = context,
                paint = paint,
                dp = dp,
                titleTextSize = navCardTitleSize,
                artBitmap = missionsArtBmp
            )
            // 7c. Leaderboard Card (3D trophy artwork + cosmic backdrop)
            calculator.leaderboardCardRect.toRectF(scratch)
            val leaderboardArtBmp = worldBitmap("home_card_art_leaderboard")
            if (calculator.landscapeClass != null && calculator.landscapeClass != LandscapeClass.WIDE) {
                drawLandscapeLeaderboardCard(
                    canvas = canvas,
                    rect = scratch,
                    bitmap = leaderboardArtBmp,
                    active = activeMenuButton == MenuButton.LEADERBOARDS,
                    paint = paint,
                    dp = dp,
                    context = context,
                    showSubtitle = calculator.showCardSubtitles
                )
            } else {
                drawNavCard(
                    canvas = canvas,
                    rect = scratch,
                    title = leaderboardTitle,
                    subtitle = HomeCopy.leaderboardSubtitle(context),
                    showSubtitle = calculator.showCardSubtitles,
                    iconRes = R.drawable.ic_trophy,
                    accentColor = 0xFFFFD54F.toInt(),
                    active = activeMenuButton == MenuButton.LEADERBOARDS,
                    pulseGlow = false,
                    stateElapsed = stateElapsed,
                    context = context,
                    paint = paint,
                    dp = dp,
                    titleTextSize = navCardTitleSize,
                    artBitmap = leaderboardArtBmp
                )
            }

            // 8. FOOTER NOTE / BRAND ACCENT (only when non-empty)
            if (!calculator.footerRect.isEmpty()) {
                calculator.footerRect.toRectF(scratch)
                drawHomeFooterBrandAccent(canvas, scratch, dp, t)
            }

            // 10. BRAND & HEADER (Drawn last on top of scene)
            drawHeader(
                canvas = canvas,
                calculator = calculator,
                activeMenuButton = activeMenuButton,
                bestStreak = bestStreak,
                currentLevel = currentLevel,
                hypeBalance = hypeBalance,
                dailyReady = dailyReady,
                stateElapsed = stateElapsed,
                context = context,
                paint = paint,
                dp = dp,
                worldBitmap = worldBitmap,
                formatHypeAmount = formatHypeAmount,
                t = t
            )
        } else {
            drawPlayModeScreen(canvas, calculator.contentRect.left, calculator.contentRect.width(), calculator.brandRect.top)
        }
    }
}
