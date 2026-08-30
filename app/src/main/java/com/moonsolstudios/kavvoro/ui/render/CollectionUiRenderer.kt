package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockType
import kotlin.math.min

/**
 * Procedural UI renderer for the Collection & Brainball Vault screens.
 */
object CollectionUiRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val scratchRect3 = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawFilters(
        canvas: Canvas,
        filterRects: List<RectF>,
        currentFilter: CollectionFilter,
        activeIndex: Int,
        activeFilterIndexFn: (Int) -> Int,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        CollectionFilter.entries.forEachIndexed { index, filter ->
            val rect = filterRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.isEmpty) return@forEachIndexed
            val selected = filter == currentFilter
            val active = activeIndex == activeFilterIndexFn(index)
            val accent = when (filter) {
                CollectionFilter.ALL -> 0xFFF7F4FF.toInt()
                CollectionFilter.SUPERPOWER -> 0xFFFFCF4A.toInt()
                CollectionFilter.HYPE -> 0xFF1DE8C8.toInt()
                CollectionFilter.PREMIUM -> 0xFFFF4D8D.toInt()
                CollectionFilter.COSMETIC -> 0xFF8AA6FF.toInt()
            }
            paint.style = Paint.Style.FILL
            paint.color = when {
                selected -> withAlpha(accent, 62)
                active -> withAlpha(accent, 42)
                else -> 0x9A080C13.toInt()
            }
            canvas.drawRoundRect(rect, 7f * dp, 7f * dp, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = (if (selected) 1.4f else 0.8f) * dp
            paint.color = withAlpha(accent, if (selected) 220 else 110)
            canvas.drawRoundRect(rect, 7f * dp, 7f * dp, paint)
            if (filter == CollectionFilter.SUPERPOWER) {
                val icon = 13f * dp
                scratchRect.set(rect.left + 5f * dp, rect.centerY() - icon * 0.5f, rect.left + 5f * dp + icon, rect.centerY() + icon * 0.5f)
                drawWorldAsset(canvas, "boost_plasma", scratchRect, if (selected) 245 else 170)
            }
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
            textPaint.textSize = 8f * dp
            textPaint.color = if (selected) 0xFFF7F4FF.toInt() else withAlpha(0xFFF7F4FF.toInt(), 170)
            val labelLeftPad = if (filter == CollectionFilter.SUPERPOWER) 12f * dp else 0f
            canvas.drawText(
                fitText(t(filter.labelKey).uppercase(), rect.width() - 10f * dp - labelLeftPad),
                rect.centerX() + labelLeftPad * 0.35f,
                rect.centerY() + 3f * dp,
                textPaint
            )
        }
    }

    fun drawItem(
        canvas: Canvas,
        rect: RectF,
        index: Int,
        skin: BallSkin,
        skinIndex: Int,
        unlocked: Boolean,
        selected: Boolean,
        hypeBalance: Int,
        activeIndex: Int,
        artBitmap: Bitmap?,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        powerIconKey: (BallPower) -> String,
        ballPowerName: (BallPower) -> String,
        unlockShortLabel: (BallSkin) -> String,
        premiumCompactPriceLabel: (BallSkin) -> String
    ) {
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val hypeReady = skin.unlock.type == UnlockType.HYPE_COST && hypeBalance >= skin.unlock.value
        val powered = skin.power != BallPower.NONE
        val active = activeIndex == index

        val rarity = brainballRarity(skin, t)
        val rarityColor = brainballRarityColor(skin)
        val locked = !unlocked && !premium
        val corner = 8f * dp
        paint.style = Paint.Style.FILL
        paint.color = when {
            selected -> 0xF20D1320.toInt()
            premium -> 0xF2181220.toInt()
            unlocked -> 0xEF0A0F18.toInt()
            else -> 0xF2070B12.toInt()
        }
        canvas.drawRoundRect(rect, corner, corner, paint)

        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                withAlpha(if (powered) 0xFFFFCF4A.toInt() else rarityColor, if (premium || powered) 78 else 42),
                withAlpha(if (powered) skin.lineColor else 0xFF0A0F18.toInt(), if (powered) 36 else 0),
                withAlpha(0xFF07090F.toInt(), 88)
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        paint.style = Paint.Style.FILL
        paint.color = if (powered) 0xFFFFCF4A.toInt() else rarityColor
        canvas.drawRect(rect.left, rect.top, rect.left + (if (selected) 5f else 3f) * dp, rect.bottom, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (selected || active) 2f else 1f) * dp
        paint.color = when {
            selected -> skin.lineColor
            premium -> withAlpha(0xFFFFCF4A.toInt(), 210)
            powered -> withAlpha(0xFFFFCF4A.toInt(), 220)
            unlocked -> withAlpha(skin.lineColor, 150)
            else -> 0x44FFFFFF
        }
        canvas.drawRoundRect(rect, corner, corner, paint)

        val isSingleColumn = rect.width() > 300f * dp
        val avatarRadius = min(rect.height() * 0.34f, (if (isSingleColumn) 32f else 27f) * dp)
        val avatarCx = rect.left + (if (isSingleColumn) 46f else 37f) * dp
        val avatarCy = rect.centerY() + 2f * dp
        drawAvatar(
            canvas = canvas,
            cx = avatarCx,
            cy = avatarCy,
            radius = avatarRadius,
            skin = skin,
            locked = locked,
            selected = selected,
            large = false,
            artBitmap = artBitmap,
            paint = paint,
            dp = dp
        )
        drawPowerBadge(canvas, avatarCx, avatarCy, avatarRadius, skin, locked, paint, dp, drawWorldAsset, powerIconKey)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        val textLeft = rect.left + (if (isSingleColumn) 92f else 74f) * dp
        val textWidth = rect.right - textLeft - 11f * dp
        if (powered) {
            val ribbonWidth = if (isSingleColumn) textWidth - 68f * dp else rect.width() * 0.46f
            drawPowerRibbon(canvas, textLeft, rect.top + 7f * dp, ribbonWidth.coerceAtLeast(72f * dp), skin, locked, paint, dp, t, fitText, drawWorldAsset, powerIconKey)
        } else {
            textPaint.textSize = 7.5f * dp
            textPaint.color = withAlpha(rarityColor, if (unlocked || premium) 240 else 150)
            val rarityWidth = if (isSingleColumn) textWidth * 0.62f else rect.width() * 0.46f
            canvas.drawText(fitText(rarity, rarityWidth), textLeft, rect.top + 18f * dp, textPaint)
        }

        scratchRect2.set(rect.right - 70f * dp, rect.top + 10f * dp, rect.right - 10f * dp, rect.top + 31f * dp)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else 0xFFFFFFFF.toInt(), if (premium || powered) 30 else 16)
        canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)
        if (powered) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.7f * dp
            paint.color = withAlpha(0xFFFFCF4A.toInt(), 155)
            canvas.drawRoundRect(scratchRect2, 6f * dp, 6f * dp, paint)
            val icon = 13f * dp
            scratchRect3.set(scratchRect2.left + 5f * dp, scratchRect2.centerY() - icon * 0.5f, scratchRect2.left + 5f * dp + icon, scratchRect2.centerY() + icon * 0.5f)
            drawWorldAsset(canvas, powerIconKey(skin.power), scratchRect3, if (locked) 145 else 230)
        }
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 7.4f * dp
        textPaint.color = if (premium || powered) 0xFFFFCF4A.toInt() else 0x88FFFFFF.toInt()
        val auraVal = if (skin.unlock.type == UnlockType.PREMIUM) 9999 - skinIndex * 111 else 404 + skinIndex * 137
        val chipLabel = if (powered) t("POWER").uppercase() else "${t("AURA").uppercase()} $auraVal"
        canvas.drawText(
            fitText(chipLabel, scratchRect2.width() - (if (powered) 24f else 8f) * dp),
            scratchRect2.centerX() + (if (powered) 8f else 0f) * dp,
            scratchRect2.top + 14f * dp,
            textPaint
        )

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = if (unlocked || premium) 0xFFF7F4FF.toInt() else 0xAAFFFFFF.toInt()
        drawFittedText(canvas, skin.name, textLeft, rect.top + 44f * dp, textWidth, if (isSingleColumn) 16f else 12.5f, 9.2f)

        textPaint.color = 0xAFFFFFFF.toInt()
        val sub = if (skin.power == BallPower.NONE) skin.subtitle.uppercase() else ballPowerName(skin.power)
        drawFittedText(canvas, sub, textLeft, rect.top + 62f * dp, textWidth, if (isSingleColumn) 9.2f else 8.4f, 7.1f)

        val label = when {
            selected -> t("EQUIPPED").uppercase()
            unlocked -> t("EQUIP").uppercase()
            premium && powered -> "${t("GET").uppercase()} ${premiumCompactPriceLabel(skin)} / ${ballPowerName(skin.power)}"
            premium -> "${t("GET").uppercase()} ${premiumCompactPriceLabel(skin)}"
            hypeReady && powered -> "${t("UNLOCK").uppercase()} ${unlockShortLabel(skin)} / ${ballPowerName(skin.power)}"
            hypeReady -> "${t("UNLOCK").uppercase()} ${unlockShortLabel(skin)}"
            powered -> "${t("NEEDS").uppercase()} ${unlockShortLabel(skin)} / ${ballPowerName(skin.power)}"
            else -> "${t("NEEDS").uppercase()} ${unlockShortLabel(skin)}"
        }
        textPaint.color = when {
            selected -> skin.lineColor
            premium || powered || hypeReady -> 0xFFFFCF4A.toInt()
            unlocked -> 0xCCFFFFFF.toInt()
            else -> withAlpha(skin.lineColor, 210)
        }
        textPaint.textSize = 8.8f * dp
        val statusWidth = (textPaint.measureText(label) + 18f * dp).coerceAtMost(if (isSingleColumn) 128f * dp else textWidth)
        scratchRect.set(rect.right - 12f * dp - statusWidth, rect.bottom - 30f * dp, rect.right - 12f * dp, rect.bottom - 9f * dp)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (premium || hypeReady || powered) 0xFFFFCF4A.toInt() else rarityColor, if (unlocked || premium || selected || hypeReady || powered) 42 else 22)
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 8.8f * dp
        canvas.drawText(fitText(label, statusWidth - 16f * dp), scratchRect.centerX(), rect.bottom - 15f * dp, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    fun drawAvatar(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        locked: Boolean,
        selected: Boolean,
        large: Boolean,
        artBitmap: Bitmap?,
        paint: Paint,
        dp: Float
    ) {
        val accent = brainballRarityColor(skin)
        val shellRadius = radius * if (large) 1.14f else 1.1f

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, if (locked) 42 else 74)
        canvas.drawCircle(cx, cy, shellRadius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (selected) 2.4f else 1.5f) * dp
        paint.color = withAlpha(if (selected) skin.lineColor else accent, if (locked) 105 else 230)
        canvas.drawCircle(cx, cy, shellRadius * 0.92f, paint)

        val imageRadius = radius * if (large) 0.91f else 0.88f
        val saveCount = canvas.save()
        tempPath.reset()
        tempPath.addCircle(cx, cy, imageRadius, Path.Direction.CW)
        canvas.clipPath(tempPath)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF05070D.toInt()
        canvas.drawCircle(cx, cy, imageRadius, paint)
        if (artBitmap != null && !artBitmap.isRecycled) {
            paint.alpha = if (locked) 145 else 255
            paint.isFilterBitmap = true
            scratchRect3.set(cx - imageRadius, cy - imageRadius, cx + imageRadius, cy + imageRadius)
            canvas.drawBitmap(artBitmap, null, scratchRect3, paint)
            paint.alpha = 255
        } else {
            paint.color = if (locked) 0xFF333947.toInt() else skin.primary
            canvas.drawCircle(cx, cy, imageRadius, paint)
        }
        if (locked) {
            paint.color = 0x6610151E
            canvas.drawCircle(cx, cy, imageRadius, paint)
        }
        canvas.restoreToCount(saveCount)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (locked) 85 else 145)
        canvas.drawCircle(cx, cy, imageRadius, paint)
        if (locked) {
            BallSkinRenderer.drawLock(canvas, cx, cy, radius * 0.88f, paint)
        }
    }

    private fun drawPowerRibbon(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        skin: BallSkin,
        locked: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        powerIconKey: (BallPower) -> String
    ) {
        val height = 19f * dp
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratchRect.left,
            scratchRect.top,
            scratchRect.right,
            scratchRect.bottom,
            intArrayOf(withAlpha(0xFFFFCF4A.toInt(), if (locked) 72 else 130), withAlpha(skin.lineColor, if (locked) 34 else 74)),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(0xFFFFCF4A.toInt(), if (locked) 130 else 230)
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
        val icon = 13f * dp
        scratchRect2.set(left + 4f * dp, top + height * 0.5f - icon * 0.5f, left + 4f * dp + icon, top + height * 0.5f + icon * 0.5f)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratchRect2, if (locked) 135 else 230)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 7.2f * dp
        textPaint.color = if (locked) 0xBBFFFFFF.toInt() else 0xFFF7F4FF.toInt()
        canvas.drawText(
            fitText(powerTierLabel(skin, t), width - 24f * dp),
            left + 21f * dp,
            top + 12.8f * dp,
            textPaint
        )
    }

    private fun drawPowerBadge(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        skin: BallSkin,
        locked: Boolean,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        powerIconKey: (BallPower) -> String
    ) {
        if (skin.power == BallPower.NONE) return
        val badgeRadius = radius * 0.42f
        val bx = cx + radius * 0.72f
        val by = cy - radius * 0.68f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(0xFFFFCF4A.toInt(), if (locked) 65 else 120)
        canvas.drawCircle(bx, by, badgeRadius * 1.55f, paint)
        paint.color = withAlpha(0xFF07090F.toInt(), if (locked) 190 else 230)
        canvas.drawCircle(bx, by, badgeRadius * 1.2f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = withAlpha(if (locked) 0xFF8AA6FF.toInt() else 0xFFFFCF4A.toInt(), if (locked) 150 else 245)
        canvas.drawCircle(bx, by, badgeRadius * 1.22f, paint)
        scratchRect3.set(bx - badgeRadius, by - badgeRadius, bx + badgeRadius, by + badgeRadius)
        drawWorldAsset(canvas, powerIconKey(skin.power), scratchRect3, if (locked) 150 else 245)
    }

    fun brainballRarity(skin: BallSkin, t: (String) -> String): String = when {
        skin.power != BallPower.NONE -> powerTierLabel(skin, t)
        skin.unlock.type == UnlockType.PREMIUM -> t("MYTHIC BRAINROT").uppercase()
        skin.unlock.type == UnlockType.DEFAULT -> t("ORIGINAL SPECIMEN").uppercase()
        skin.style == SkinStyle.CROWN -> t("MAX AURA").uppercase()
        skin.style == SkinStyle.GLITCH || skin.style == SkinStyle.STATIC -> t("GLITCHED").uppercase()
        skin.style == SkinStyle.RIFT || skin.style == SkinStyle.VOID -> t("FORBIDDEN").uppercase()
        skin.style == SkinStyle.ZAP || skin.style == SkinStyle.PLASMA -> t("OVERCLOCKED").uppercase()
        skin.style == SkinStyle.BLOP || skin.style == SkinStyle.WOBBLE -> t("GOOFY CLASS").uppercase()
        else -> t("RARE THOUGHT").uppercase()
    }

    fun powerTierLabel(skin: BallSkin, t: (String) -> String): String {
        if (skin.power == BallPower.NONE) return t("NO POWER").uppercase()
        return when {
            skin.unlock.type == UnlockType.PREMIUM -> t("MYTHIC SUPERPOWER").uppercase()
            skin.power == BallPower.MINOR_PHASE || skin.power == BallPower.MINOR_RICOCHET || skin.power == BallPower.MINOR_SURGE -> t("LITE SUPERPOWER").uppercase()
            else -> t("EARNED SUPERPOWER").uppercase()
        }
    }

    fun brainballRarityColor(skin: BallSkin): Int = when {
        skin.unlock.type == UnlockType.PREMIUM -> 0xFFFFCF4A.toInt()
        skin.power != BallPower.NONE -> 0xFF64E572.toInt()
        skin.style == SkinStyle.CROWN -> 0xFFFFCF4A.toInt()
        skin.style == SkinStyle.GLITCH || skin.style == SkinStyle.STATIC -> 0xFFC15CFF.toInt()
        skin.style == SkinStyle.RIFT || skin.style == SkinStyle.VOID -> 0xFFFF4D8D.toInt()
        else -> skin.lineColor
    }

    fun drawCollectionBackdrop(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        safeTop76: Float,
        menuPulse: Float,
        paint: Paint,
        dp: Float
    ) {
        paint.shader = LinearGradient(
            0f, 0f, viewWidth, viewHeight,
            intArrayOf(0xFF05070D.toInt(), 0xFF0A1018.toInt(), 0xFF07090F.toInt()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x0FFFFFFF
        val step = 58f * dp
        var x = -((menuPulse * 4f * dp) % step)
        while (x < viewWidth + step) {
            canvas.drawLine(x, 0f, x, viewHeight, paint)
            x += step
        }
        var y = safeTop76
        while (y < viewHeight) {
            canvas.drawLine(0f, y, viewWidth, y, paint)
            y += step
        }

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f, 0f, 0f, viewHeight,
            intArrayOf(0x0005090F, 0x441DE8C8, 0x001DE8C8),
            floatArrayOf(0f, 0.34f, 1f),
            Shader.TileMode.CLAMP
        )
        scratchRect.set(0f, 0f, 5f * dp, viewHeight)
        canvas.drawRect(scratchRect, paint)
        scratchRect.set(viewWidth - 5f * dp, 0f, viewWidth, viewHeight)
        canvas.drawRect(scratchRect, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2.2f * dp
        repeat(5) { i ->
            val yy = (150f + i * 132f) * dp + kotlin.math.sin(menuPulse * 0.8f + i) * 7f * dp
            paint.color = withAlpha(if (i % 2 == 0) 0xFF1DE8C8.toInt() else 0xFFFF4D8D.toInt(), 35)
            canvas.drawLine(16f * dp, yy, viewWidth - 16f * dp, yy + (if (i % 2 == 0) 12f else -10f) * dp, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawStatChip(
        canvas: Canvas,
        left: Float,
        top: Float,
        label: String,
        value: String,
        accent: Int,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        val width = 92f * dp
        val height = 24f * dp
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xCC0A0F18.toInt()
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(accent, 115)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 6.7f * dp
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(label, left + 8f * dp, top + 9f * dp, textPaint)
        textPaint.textSize = 10f * dp
        textPaint.color = accent
        canvas.drawText(fitText(value, width - 16f * dp), left + 8f * dp, top + 20f * dp, textPaint)
    }

    fun drawLoadoutCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        skin: BallSkin,
        equipped: Boolean,
        unlocked: Boolean,
        hypeBalance: Int,
        artBitmap: Bitmap?,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        powerIconKey: (BallPower) -> String,
        ballPowerName: (BallPower) -> String,
        ballPowerDescription: (BallPower) -> String,
        premiumPriceLabel: (BallSkin) -> String,
        brainballCardSubtitle: (BallSkin) -> String,
        unlockLongLabel: (BallSkin) -> String,
        brainballAura: (BallSkin) -> Int
    ) {
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val hypeReady = skin.unlock.type == UnlockType.HYPE_COST && hypeBalance >= skin.unlock.value
        val powered = skin.power != BallPower.NONE
        val bottom = top + 78f * dp
        val accent = brainballRarityColor(skin)

        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xF20A0F18.toInt()
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.shader = LinearGradient(
            left, top, right, bottom,
            intArrayOf(withAlpha(if (powered) 0xFFFFCF4A.toInt() else accent, if (powered) 92 else 64), withAlpha(accent, if (powered) 34 else 0), 0x000A0F18),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (powered) 1.7f else 1.1f) * dp
        paint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else accent, if (powered) 235 else 185)
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRect(left, top, left + 4f * dp, bottom, paint)

        drawAvatar(
            canvas = canvas,
            cx = left + 43f * dp,
            cy = (top + bottom) * 0.5f,
            radius = 29f * dp,
            skin = skin,
            locked = !unlocked && !premium,
            selected = equipped,
            large = true,
            artBitmap = artBitmap,
            paint = paint,
            dp = dp
        )

        val textLeft = left + 84f * dp
        val rightColumn = 70f * dp
        val textMaxWidth = (right - textLeft - rightColumn - 10f * dp).coerceAtLeast(96f * dp)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 8f * dp
        textPaint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else accent, 245)
        val kicker = when {
            powered -> "${powerTierLabel(skin, t)}  /  ${ballPowerName(skin.power)}"
            equipped -> t("EQUIPPED BRAINBALL").uppercase()
            else -> "${t("INSPECTING").uppercase()} ${brainballRarity(skin, t)}"
        }
        canvas.drawText(fitText(kicker, textMaxWidth), textLeft, top + 19f * dp, textPaint)
        textPaint.textSize = 18f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        drawFittedText(canvas, skin.name, textLeft, top + 43f * dp, textMaxWidth, 18f, 12f)
        textPaint.color = 0xAFFFFFFF.toInt()
        val detail = when {
            powered && premium -> "${ballPowerDescription(skin.power)} / ${premiumPriceLabel(skin)}"
            powered -> ballPowerDescription(skin.power)
            equipped -> "${brainballCardSubtitle(skin)} / ${t("ACTIVE").uppercase()}"
            unlocked -> "${brainballCardSubtitle(skin)} / ${t("TAP TO EQUIP").uppercase()}"
            hypeReady -> "${t("READY TO MUTATE").uppercase()} / ${t("TAP TO UNLOCK").uppercase()}"
            else -> unlockLongLabel(skin).uppercase()
        }
        drawFittedText(canvas, detail, textLeft, top + 61f * dp, textMaxWidth, 9.3f, 7.2f)

        scratchRect2.set(right - 62f * dp, top + 15f * dp, right - 12f * dp, bottom - 15f * dp)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (powered) skin.lineColor else 0xFFFFCF4A.toInt(), if (powered) 40 else 24)
        canvas.drawRoundRect(scratchRect2, 7f * dp, 7f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(if (powered) 0xFFFFCF4A.toInt() else 0xFFFFCF4A.toInt(), if (powered) 180 else 70)
        canvas.drawRoundRect(scratchRect2, 7f * dp, 7f * dp, paint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 7.4f * dp
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(if (powered) t("POWER").uppercase() else t("AURA").uppercase(), scratchRect2.centerX(), scratchRect2.top + 14f * dp, textPaint)
        if (powered) {
            scratchRect3.set(scratchRect2.centerX() - 13f * dp, scratchRect2.top + 18f * dp, scratchRect2.centerX() + 13f * dp, scratchRect2.top + 44f * dp)
            drawWorldAsset(canvas, powerIconKey(skin.power), scratchRect3, 245)
        } else {
            textPaint.textSize = 16f * dp
            textPaint.color = 0xFFFFCF4A.toInt()
            canvas.drawText(brainballAura(skin).toString(), scratchRect2.centerX(), scratchRect2.top + 35f * dp, textPaint)
        }
    }

    fun drawCollectionScreen(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        safeTop22: Float,
        safeTop54: Float,
        safeTop76: Float,
        safeTop104: Float,
        safeBottom70: Float,
        safeBottom16: Float,
        pageContentLeft: Float,
        pageContentRight: Float,
        collectionBackButton: RectF,
        collectionRestoreButton: RectF,
        collectionFilterRects: List<RectF>,
        collectionItemRects: List<RectF>,
        collectionFilter: CollectionFilter,
        activeCollectionIndex: Int,
        collectionFilterActiveIndexFn: (Int) -> Int,
        collectionViewportTop: Float,
        collectionViewportBottom: Float,
        menuPulse: Float,
        ballSkins: List<BallSkin>,
        selectedSkin: BallSkin,
        focusedSkin: BallSkin,
        unlockedCount: Int,
        bestStreak: Int,
        hypeBalance: Int,
        formatHypeAmount: (Int) -> String,
        isSkinUnlocked: (BallSkin) -> Boolean,
        brainballBitmap: (BallSkin) -> Bitmap?,
        collectionMessage: String,
        nextRewardText: String?,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawUiButtonFrame: (Canvas, RectF, Boolean, Int, Float) -> Unit,
        drawUiIconAsset: (Canvas, String, RectF, Float, Int) -> Unit,
        powerIconKey: (BallPower) -> String,
        ballPowerName: (BallPower) -> String,
        ballPowerDescription: (BallPower) -> String,
        unlockShortLabel: (BallSkin) -> String,
        unlockLongLabel: (BallSkin) -> String,
        premiumPriceLabel: (BallSkin) -> String,
        premiumCompactPriceLabel: (BallSkin) -> String
    ) {
        drawCollectionBackdrop(canvas, viewWidth, viewHeight, safeTop76, menuPulse, paint, dp)

        val left = pageContentLeft + 2f * dp
        val top = safeTop22
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.shader = null
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textSize = 8.5f * dp
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText("KAVVORO  /  ${t("VAULT").uppercase()}", left, top + 8f * dp, textPaint)
        textPaint.textSize = 29f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(t("COLLECTION").uppercase(), collectionBackButton.left - left - 10f * dp), left, top + 42f * dp, textPaint)

        val metaTop = safeTop54
        drawStatChip(canvas, left, metaTop, t("OWNED").uppercase(), "$unlockedCount/${ballSkins.size}", 0xFF1DE8C8.toInt(), paint, dp, fitText)
        drawStatChip(canvas, left + 103f * dp, metaTop, t("STREAK").uppercase(), "x$bestStreak", 0xFFC15CFF.toInt(), paint, dp, fitText)
        if (collectionRestoreButton.left - left >= 308f * dp) {
            drawStatChip(canvas, left + 206f * dp, metaTop, t("HYPE BANK").uppercase(), formatHypeAmount(hypeBalance), 0xFFFFCF4A.toInt(), paint, dp, fitText)
        }

        // Back button
        val backActive = activeCollectionIndex == -2 // COLLECTION_BACK_INDEX
        drawUiButtonFrame(canvas, collectionBackButton, backActive, 0xFF8AA6FF.toInt(), 99f)
        drawUiIconAsset(canvas, "ui_back", collectionBackButton, -1f, 245)

        // Restore button
        val restoreActive = activeCollectionIndex == -3 // COLLECTION_RESTORE_INDEX
        drawUiButtonFrame(canvas, collectionRestoreButton, restoreActive, 0xFF8AA6FF.toInt(), 7f)
        drawWorldAsset(canvas, "brand_google_play", collectionRestoreButton, if (restoreActive) 255 else 230)

        // Loadout card
        drawLoadoutCard(
            canvas = canvas,
            left = pageContentLeft,
            top = safeTop104,
            right = pageContentRight,
            skin = focusedSkin,
            equipped = selectedSkin.id == focusedSkin.id,
            unlocked = isSkinUnlocked(focusedSkin),
            hypeBalance = hypeBalance,
            artBitmap = brainballBitmap(focusedSkin),
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            drawFittedText = drawFittedText,
            drawWorldAsset = drawWorldAsset,
            powerIconKey = powerIconKey,
            ballPowerName = ballPowerName,
            ballPowerDescription = ballPowerDescription,
            premiumPriceLabel = premiumPriceLabel,
            brainballCardSubtitle = { s -> t(s.style.name).uppercase() },
            unlockLongLabel = unlockLongLabel,
            brainballAura = { s -> s.lineColor }
        )

        // Filters
        drawFilters(
            canvas = canvas,
            filterRects = collectionFilterRects,
            currentFilter = collectionFilter,
            activeIndex = activeCollectionIndex,
            activeFilterIndexFn = collectionFilterActiveIndexFn,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            drawWorldAsset = drawWorldAsset
        )

        // Items grid
        val viewportTop = collectionViewportTop
        val viewportBottom = collectionViewportBottom
        canvas.save()
        canvas.clipRect(0f, viewportTop, viewWidth, viewportBottom)
        ballSkins.forEachIndexed { index, skin ->
            val rect = collectionItemRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.bottom >= viewportTop && rect.top <= viewportBottom) {
                drawItem(
                    canvas = canvas,
                    rect = rect,
                    index = index,
                    skin = skin,
                    skinIndex = ballSkins.indexOfFirst { it.id == skin.id }.coerceAtLeast(0),
                    unlocked = isSkinUnlocked(skin),
                    selected = selectedSkin.id == skin.id,
                    hypeBalance = hypeBalance,
                    activeIndex = activeCollectionIndex,
                    artBitmap = brainballBitmap(skin),
                    paint = paint,
                    dp = dp,
                    t = t,
                    fitText = fitText,
                    drawFittedText = drawFittedText,
                    drawWorldAsset = drawWorldAsset,
                    powerIconKey = powerIconKey,
                    ballPowerName = ballPowerName,
                    unlockShortLabel = unlockShortLabel,
                    premiumCompactPriceLabel = premiumCompactPriceLabel
                )
            }
        }
        canvas.restore()

        // Status banner
        val footer = collectionMessage.ifBlank {
            nextRewardText?.let { "${t("NEXT MUTATION").uppercase()} / $it" } ?: t("VAULT COMPLETE / MAXIMUM BRAIN ACHIEVED")
        }
        LeaderboardUiRenderer.drawStatusBanner(
            canvas = canvas,
            left = pageContentLeft,
            top = safeBottom70,
            right = pageContentRight,
            bottom = safeBottom16,
            headerText = t(if (collectionMessage.isNotBlank()) "STATUS UPDATE" else "NEXT SIGNAL").uppercase(),
            message = footer,
            accent = selectedSkin.lineColor,
            transient = collectionMessage.isNotBlank(),
            paint = paint,
            dp = dp,
            fitText = fitText
        )
    }

}
