package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class HomeStatSpec(
    val title: String,
    val value: String,
    val accent: Int,
    val iconKey: String
)

/**
 * Procedural renderer for all Home Screen UI cards, stats, icons, and navigation elements.
 */
object HomeUiRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawTopActionCard(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        scale: Float,
        paint: Paint,
        dp: Float
    ) {
        val corner = 14f * scale * dp
        // Card Background (0xFF070C14)
        paint.style = Paint.Style.FILL
        paint.color = if (active) 0xFF141C28.toInt() else 0xFF070C14.toInt()
        canvas.drawRoundRect(rect, corner, corner, paint)

        // Card Border (0xFF35404D)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = if (active) 0xFF5A6B7F.toInt() else 0xFF35404D.toInt()
        canvas.drawRoundRect(rect, corner, corner, paint)
    }

    fun drawGearIcon(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, paint: Paint, dp: Float) {
        val outerRadius = radius
        val rootRadius = outerRadius * 0.73f
        val shoulderRadius = outerRadius * 0.86f
        val teeth = 8
        val sector = (2.0 * PI) / teeth

        tempPath.reset()
        var firstPoint = true

        for (tooth in 0 until teeth) {
            val centerAngle = -PI / 2.0 + tooth * sector
            val points = arrayOf(
                -0.50 to rootRadius,
                -0.34 to shoulderRadius,
                -0.21 to outerRadius,
                0.21 to outerRadius,
                0.34 to shoulderRadius,
                0.50 to rootRadius
            )
            for ((angleFactor, r) in points) {
                val angle = centerAngle + sector * angleFactor
                val x = cx + cos(angle).toFloat() * r
                val y = cy + sin(angle).toFloat() * r
                if (firstPoint) {
                    tempPath.moveTo(x, y)
                    firstPoint = false
                } else {
                    tempPath.lineTo(x, y)
                }
            }
        }
        tempPath.close()

        val strokeWidth = 2f * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color

        // Gear outline
        canvas.drawPath(tempPath, paint)

        // Center hub circle
        canvas.drawCircle(cx, cy, outerRadius * 0.27f, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawSpeakerIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, muted: Boolean, color: Int, paint: Paint, dp: Float) {
        val w = size * 2f
        val h = size * 2f
        val left = cx - size
        val top = cy - size
        val strokeWidth = 2f * dp

        // Speaker body path
        tempPath.reset()
        tempPath.moveTo(left + w * 0.10f, top + h * 0.39f)
        tempPath.lineTo(left + w * 0.28f, top + h * 0.39f)
        tempPath.lineTo(left + w * 0.49f, top + h * 0.21f)
        tempPath.lineTo(left + w * 0.49f, top + h * 0.79f)
        tempPath.lineTo(left + w * 0.28f, top + h * 0.61f)
        tempPath.lineTo(left + w * 0.10f, top + h * 0.61f)
        tempPath.close()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color
        canvas.drawPath(tempPath, paint)

        if (!muted) {
            // Small wave
            scratchRect.set(
                left + w * 0.45f,
                top + h * 0.32f,
                left + w * 0.45f + w * 0.27f,
                top + h * 0.32f + h * 0.36f
            )
            canvas.drawArc(scratchRect, -48f, 96f, false, paint)

            // Large wave
            scratchRect.set(
                left + w * 0.42f,
                top + h * 0.18f,
                left + w * 0.42f + w * 0.49f,
                top + h * 0.18f + h * 0.64f
            )
            canvas.drawArc(scratchRect, -48f, 96f, false, paint)
        } else {
            val muteCenterX = left + w * 0.76f
            val muteCenterY = top + h * 0.50f
            val muteRadius = w * 0.105f

            // \
            canvas.drawLine(
                muteCenterX - muteRadius,
                muteCenterY - muteRadius,
                muteCenterX + muteRadius,
                muteCenterY + muteRadius,
                paint
            )
            // /
            canvas.drawLine(
                muteCenterX + muteRadius,
                muteCenterY - muteRadius,
                muteCenterX - muteRadius,
                muteCenterY + muteRadius,
                paint
            )
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawStreakTrendIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        tempPath.reset()
        tempPath.moveTo(cx - size * 0.85f, cy + size * 0.6f)
        tempPath.lineTo(cx - size * 0.35f, cy + size * 0.1f)
        tempPath.lineTo(cx + size * 0.1f, cy + size * 0.35f)
        tempPath.lineTo(cx + size * 0.85f, cy - size * 0.6f)
        canvas.drawPath(tempPath, paint)

        tempPath.reset()
        tempPath.moveTo(cx + size * 0.45f, cy - size * 0.6f)
        tempPath.lineTo(cx + size * 0.85f, cy - size * 0.6f)
        tempPath.lineTo(cx + size * 0.85f, cy - size * 0.2f)
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawHypeLightningIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = color
        tempPath.reset()
        tempPath.moveTo(cx + size * 0.2f, cy - size * 0.9f)
        tempPath.lineTo(cx - size * 0.6f, cy + size * 0.05f)
        tempPath.lineTo(cx - size * 0.05f, cy + size * 0.05f)
        tempPath.lineTo(cx - size * 0.3f, cy + size * 0.95f)
        tempPath.lineTo(cx + size * 0.65f, cy - size * 0.05f)
        tempPath.lineTo(cx + size * 0.1f, cy - size * 0.05f)
        tempPath.close()
        canvas.drawPath(tempPath, paint)
    }

    fun drawLevelTargetIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * dp
        paint.color = color
        canvas.drawCircle(cx, cy, size * 0.68f, paint)

        canvas.drawLine(cx, cy - size * 0.95f, cx, cy - size * 0.68f, paint)
        canvas.drawLine(cx, cy + size * 0.68f, cx, cy + size * 0.95f, paint)
        canvas.drawLine(cx - size * 0.95f, cy, cx - size * 0.68f, cy, paint)
        canvas.drawLine(cx + size * 0.68f, cy, cx + size * 0.95f, cy, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.22f, paint)
    }

    fun drawRiftPortalIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * dp
        paint.color = color
        canvas.drawCircle(cx, cy, size * 0.85f, paint)
        paint.strokeWidth = 1.2f * dp
        canvas.drawCircle(cx, cy, size * 0.45f, paint)
    }

    fun drawLeaderboardPodiumIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint, dp: Float) {
        val barW = size * 0.44f
        val gap = size * 0.15f
        val base = cy + size * 0.85f

        val h1 = size * 0.95f
        val x1 = cx - barW - gap * 0.5f - barW * 0.5f
        scratchRect.set(x1 - barW * 0.5f, base - h1, x1 + barW * 0.5f, base)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawRoundRect(scratchRect, 2f * dp, 2f * dp, paint)

        val h2 = size * 1.55f
        val x2 = cx
        scratchRect.set(x2 - barW * 0.5f, base - h2, x2 + barW * 0.5f, base)
        paint.color = 0xFFFF2E93.toInt()
        canvas.drawRoundRect(scratchRect, 2f * dp, 2f * dp, paint)

        val h3 = size * 0.65f
        val x3 = cx + barW + gap * 0.5f + barW * 0.5f
        scratchRect.set(x3 - barW * 0.5f, base - h3, x3 + barW * 0.5f, base)
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawRoundRect(scratchRect, 2f * dp, 2f * dp, paint)
    }

    fun drawVaultSafeIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * dp
        paint.color = 0xFF00E5FF.toInt()
        scratchRect.set(cx - size * 0.85f, cy - size * 0.85f, cx + size * 0.85f, cy + size * 0.85f)
        canvas.drawRoundRect(scratchRect, 3.5f * dp, 3.5f * dp, paint)

        canvas.drawCircle(cx, cy, size * 0.42f, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, size * 0.16f, paint)

        val dotOffset = size * 0.62f
        canvas.drawCircle(cx - dotOffset, cy - dotOffset, 1.2f * dp, paint)
        canvas.drawCircle(cx + dotOffset, cy - dotOffset, 1.2f * dp, paint)
        canvas.drawCircle(cx - dotOffset, cy + dotOffset, 1.2f * dp, paint)
        canvas.drawCircle(cx + dotOffset, cy + dotOffset, 1.2f * dp, paint)
    }

    fun drawIsometricCubeIcon(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f * dp
        paint.color = color
        val r = size * 0.9f
        val cos30 = 0.866f
        val sin30 = 0.5f

        val topX = cx
        val topY = cy - r
        val rightTopX = cx + r * cos30
        val rightTopY = cy - r * sin30
        val rightBotX = cx + r * cos30
        val rightBotY = cy + r * sin30
        val botX = cx
        val botY = cy + r
        val leftBotX = cx - r * cos30
        val leftBotY = cy + r * sin30
        val leftTopX = cx - r * cos30
        val leftTopY = cy - r * sin30

        tempPath.reset()
        tempPath.moveTo(topX, topY)
        tempPath.lineTo(rightTopX, rightTopY)
        tempPath.lineTo(rightBotX, rightBotY)
        tempPath.lineTo(botX, botY)
        tempPath.lineTo(leftBotX, leftBotY)
        tempPath.lineTo(leftTopX, leftTopY)
        tempPath.close()
        canvas.drawPath(tempPath, paint)

        canvas.drawLine(cx, cy, topX, topY, paint)
        canvas.drawLine(cx, cy, rightBotX, rightBotY, paint)
        canvas.drawLine(cx, cy, leftBotX, leftBotY, paint)
    }

    fun drawMenuChevron(canvas: Canvas, cx: Float, cy: Float, color: Int, scale: Float, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * scale * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        val size = 5.5f * scale * dp
        tempPath.reset()
        tempPath.moveTo(cx - size * 0.4f, cy - size)
        tempPath.lineTo(cx + size * 0.6f, cy)
        tempPath.lineTo(cx - size * 0.4f, cy + size)
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
    }

    fun drawStatCard(
        canvas: Canvas,
        rect: RectF,
        spec: HomeStatSpec,
        scale: Float,
        showLevelProgress: Boolean,
        level: Int,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        val corner = 8f * scale * dp
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(withAlpha(spec.accent, 28), 0xEA070D16.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.85f * dp
        paint.color = withAlpha(spec.accent, 130)
        canvas.drawRoundRect(rect, corner, corner, paint)

        val iconCx = rect.left + 13f * scale * dp
        val iconCy = rect.centerY() - (if (showLevelProgress) 3f * scale * dp else 0f)
        val iconSize = 8f * scale * dp
        when (spec.iconKey) {
            "stat_streak" -> drawStreakTrendIcon(canvas, iconCx, iconCy, iconSize, spec.accent, paint, dp)
            "stat_hype" -> drawHypeLightningIcon(canvas, iconCx, iconCy, iconSize, spec.accent, paint)
            "stat_level" -> drawLevelTargetIcon(canvas, iconCx, iconCy, iconSize, spec.accent, paint, dp)
            "stat_daily" -> drawRiftPortalIcon(canvas, iconCx, iconCy, iconSize, spec.accent, paint, dp)
        }

        val textLeft = rect.left + 25f * scale * dp
        val textWidth = rect.width() - 29f * scale * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (6.6f * scale * dp).coerceIn(5.5f * dp, 9.5f * dp)
        textPaint.color = 0xAAFFFFFF.toInt()
        val titleY = rect.top + 13f * scale * dp
        canvas.drawText(fitText(spec.title, textWidth), textLeft, titleY, textPaint)

        textPaint.textSize = (12.5f * scale * dp).coerceIn(9.5f * dp, 17f * dp)
        textPaint.color = spec.accent
        val valY = rect.top + 28f * scale * dp
        canvas.drawText(fitText(spec.value, textWidth), textLeft, valY, textPaint)

        if (showLevelProgress) {
            val barLeft = textLeft
            val barRight = rect.right - 7f * scale * dp
            val barTop = rect.bottom - 6f * scale * dp
            paint.style = Paint.Style.FILL
            paint.color = 0x33FFFFFF
            canvas.drawRoundRect(barLeft, barTop, barRight, barTop + 2.2f * scale * dp, 2f * dp, 2f * dp, paint)
            paint.color = spec.accent
            val progress = ((level - 1).mod(5) + 1) / 5f
            canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress, barTop + 2.2f * scale * dp, 2f * dp, 2f * dp, paint)
        }
    }

    fun drawInfoRow(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        title: String,
        meta: String,
        iconType: String,
        accent: Int,
        scale: Float,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        val corner = 10f * scale * dp

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(withAlpha(accent, if (active) 42 else 20), 0xD8070E18.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (active) 1.5f else 0.85f) * dp
        paint.color = withAlpha(accent, if (active) 220 else 100)
        canvas.drawRoundRect(rect, corner, corner, paint)

        val iconCx = rect.left + 28f * scale * dp
        val iconCy = rect.centerY()
        val iconSize = 14f * scale * dp
        when (iconType) {
            "leaderboards" -> drawLeaderboardPodiumIcon(canvas, iconCx, iconCy, iconSize, paint, dp)
            "vault" -> drawVaultSafeIcon(canvas, iconCx, iconCy, iconSize, paint, dp)
        }

        val textLeft = rect.left + 58f * scale * dp
        val textWidth = rect.width() - 96f * scale * dp
        val hasMeta = meta.isNotEmpty()
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (13f * scale * dp).coerceIn(10.5f * dp, 18f * dp)
        textPaint.color = if (iconType == "leaderboards") accent else 0xFFFFFFFF.toInt()
        val titleY = if (hasMeta) rect.centerY() - 3f * scale * dp else rect.centerY() + 4.5f * scale * dp
        canvas.drawText(fitText(title, textWidth), textLeft, titleY, textPaint)

        if (hasMeta) {
            textPaint.textSize = (8.5f * scale * dp).coerceIn(7f * dp, 12f * dp)
            textPaint.color = withAlpha(accent, 230)
            canvas.drawText(fitText(meta, textWidth), textLeft, rect.centerY() + 15f * scale * dp, textPaint)
        }

        drawMenuChevron(canvas, rect.right - 24f * scale * dp, rect.centerY(), withAlpha(0xFFFFFFFF.toInt(), 180), scale, paint, dp)
    }

    fun drawCollectionRow(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        unlockedCount: Int,
        totalSkins: Int,
        nextRewardText: String,
        scale: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val cyan = 0xFF00E5FF.toInt()
        val magenta = 0xFFFF2E93.toInt()
        val corner = 10f * scale * dp

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(withAlpha(cyan, if (active) 38 else 20), 0xD8070E18.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (active) 1.5f else 0.85f) * dp
        paint.color = withAlpha(cyan, if (active) 220 else 100)
        canvas.drawRoundRect(rect, corner, corner, paint)

        val ringCx = rect.left + 28f * scale * dp
        val ringCy = rect.centerY()
        val ringR = 13f * scale * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * scale * dp
        paint.color = 0x3300E5FF
        canvas.drawCircle(ringCx, ringCy, ringR, paint)
        paint.color = cyan
        scratchRect.set(ringCx - ringR, ringCy - ringR, ringCx + ringR, ringCy + ringR)
        canvas.drawArc(scratchRect, -90f, 360f * (unlockedCount.toFloat() / totalSkins.toFloat().coerceAtLeast(1f)), false, paint)

        val dividerX = rect.left + rect.width() * 0.54f
        val textLeft = rect.left + 50f * scale * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (10.8f * scale * dp).coerceIn(8.8f * dp, 15f * dp)
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText(fitText("${t("COLLECTION").uppercase()} & ${t("UNLOCKS").uppercase()}", dividerX - textLeft - 6f * scale * dp), textLeft, rect.centerY() - 4f * scale * dp, textPaint)

        textPaint.textSize = (10.5f * scale * dp).coerceIn(8.5f * dp, 14f * dp)
        textPaint.color = cyan
        val countText = "$unlockedCount / $totalSkins"
        canvas.drawText(countText, textLeft, rect.centerY() + 14f * scale * dp, textPaint)
        val countWidth = textPaint.measureText(countText)
        textPaint.textSize = (7.2f * scale * dp).coerceIn(6f * dp, 10f * dp)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("UNLOCKED").uppercase(), textLeft + countWidth + 6f * scale * dp, rect.centerY() + 14f * scale * dp, textPaint)

        paint.style = Paint.Style.STROKE
        paint.color = 0x337C8B9F
        paint.strokeWidth = 1f * dp
        canvas.drawLine(dividerX, rect.top + 12f * scale * dp, dividerX, rect.bottom - 12f * scale * dp, paint)

        val cubeCx = dividerX + 18f * scale * dp
        val cubeCy = rect.centerY()
        val cubeSize = 11f * scale * dp
        drawIsometricCubeIcon(canvas, cubeCx, cubeCy, cubeSize, magenta, paint, dp)

        val rightTextLeft = cubeCx + 16f * scale * dp
        val chevronX = rect.right - 18f * scale * dp
        val maxNextWidth = chevronX - rightTextLeft - 6f * scale * dp
        textPaint.textSize = (7.2f * scale * dp).coerceIn(6f * dp, 10f * dp)
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("NEXT UNLOCK").uppercase(), rightTextLeft, rect.centerY() - 5f * scale * dp, textPaint)
        textPaint.textSize = (10.5f * scale * dp).coerceIn(8.5f * dp, 14f * dp)
        textPaint.color = magenta
        canvas.drawText(fitText(nextRewardText, maxNextWidth), rightTextLeft, rect.centerY() + 14f * scale * dp, textPaint)

        drawMenuChevron(canvas, chevronX, rect.centerY(), withAlpha(0xFFFFFFFF.toInt(), 180), scale, paint, dp)
    }

    fun drawHeaderActions(
        canvas: Canvas,
        settingsRect: RectF,
        activeSettings: Boolean,
        sfxRect: RectF,
        activeSound: Boolean,
        sfxMuted: Boolean,
        scale: Float,
        paint: Paint,
        dp: Float
    ) {
        val iconColor = 0xFFF2F4F7.toInt()

        // Settings button (reusable card + gear icon)
        drawTopActionCard(canvas, settingsRect, activeSettings, scale, paint, dp)
        val setRadius = settingsRect.width() * 0.26f
        drawGearIcon(canvas, settingsRect.centerX(), settingsRect.centerY(), setRadius, iconColor, paint, dp)

        // Sound button (reusable card + speaker icon)
        drawTopActionCard(canvas, sfxRect, activeSound, scale, paint, dp)
        val sndSize = sfxRect.width() * 0.27f
        drawSpeakerIcon(canvas, sfxRect.centerX(), sfxRect.centerY(), sndSize, sfxMuted, iconColor, paint, dp)
    }
    fun drawElectricLightning(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        scale: Float,
        menuPulse: Float,
        paint: Paint,
        dp: Float
    ) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND

        val boltCount = 6
        repeat(boltCount) { index ->
            val baseAngle = index * (2f * PI.toFloat() / boltCount) + (sin(menuPulse * 0.8f + index) * 0.15f)
            val isLeft = cos(baseAngle) < 0
            val color = if (isLeft) 0xFF00E5FF.toInt() else 0xFFFF2E93.toInt()

            val jitter = sin(menuPulse * 16f + index * 4.3f) * 6f * scale * dp
            val rInner = radius * 0.65f
            val rMid1 = radius * 0.85f
            val rMid2 = radius * 1.05f
            val rOuter = radius * 1.25f

            val x0 = cx + cos(baseAngle) * rInner
            val y0 = cy + sin(baseAngle) * rInner
            val x1 = cx + cos(baseAngle + 0.12f) * rMid1 + jitter
            val y1 = cy + sin(baseAngle + 0.12f) * rMid1 - jitter * 0.5f
            val x2 = cx + cos(baseAngle - 0.08f) * rMid2 - jitter * 0.8f
            val y2 = cy + sin(baseAngle - 0.08f) * rMid2 + jitter
            val x3 = cx + cos(baseAngle + 0.05f) * rOuter
            val y3 = cy + sin(baseAngle + 0.05f) * rOuter

            paint.strokeWidth = 1.8f * scale * dp
            paint.color = withAlpha(color, 235)
            tempPath.reset()
            tempPath.moveTo(x0, y0)
            tempPath.lineTo(x1, y1)
            tempPath.lineTo(x2, y2)
            tempPath.lineTo(x3, y3)
            canvas.drawPath(tempPath, paint)

            paint.strokeWidth = 1.1f * scale * dp
            paint.color = withAlpha(color, 150)
            tempPath.reset()
            tempPath.moveTo(x1, y1)
            val xFork = cx + cos(baseAngle + 0.28f) * rMid2 + jitter
            val yFork = cy + sin(baseAngle + 0.28f) * rMid2 + jitter
            tempPath.lineTo(xFork, yFork)
            canvas.drawPath(tempPath, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawRiftOnlineBadge(
        canvas: Canvas,
        cx: Float,
        y: Float,
        scale: Float,
        skinName: String,
        riftOnlineLabel: String,
        paint: Paint,
        dp: Float
    ) {
        textPaint.reset()
        textPaint.isAntiAlias = true
        val textSize = (9.2f * scale * dp).coerceIn(7.5f * dp, 13f * dp)
        textPaint.textSize = textSize
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        val part1 = "$riftOnlineLabel  /  "
        val part2 = skinName.uppercase()
        val part1W = textPaint.measureText(part1)
        val part2W = textPaint.measureText(part2)
        val dotRadius = 3.2f * scale * dp
        val dotMargin = 8f * scale * dp
        val totalContentW = dotRadius * 2f + dotMargin + part1W + part2W
        val pillW = totalContentW + 24f * scale * dp
        val pillH = 24f * scale * dp
        scratchRect.set(cx - pillW * 0.5f, y - pillH * 0.5f, cx + pillW * 0.5f, y + pillH * 0.5f)

        paint.style = Paint.Style.FILL
        paint.color = 0xEE040810.toInt()
        canvas.drawRoundRect(scratchRect, pillH * 0.5f, pillH * 0.5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0x4445F2FF.toInt()
        canvas.drawRoundRect(scratchRect, pillH * 0.5f, pillH * 0.5f, paint)

        val contentStartX = cx - totalContentW * 0.5f
        val dotCx = contentStartX + dotRadius
        paint.style = Paint.Style.FILL
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawCircle(dotCx, y, dotRadius, paint)

        val textStartX = dotCx + dotRadius + dotMargin
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText(part1, textStartX, y + 3.2f * scale * dp, textPaint)
        textPaint.color = 0xFFFF2E93.toInt()
        canvas.drawText(part2, textStartX + part1W, y + 3.2f * scale * dp, textPaint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}
