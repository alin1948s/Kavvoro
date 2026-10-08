package com.moonsolstudios.kavvoro.ui.screens.collection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.UiTypography
import com.moonsolstudios.kavvoro.ui.render.UiWidgetRenderer
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Procedural AAA UI renderer for the Collection & Brainball Vault (Quantum Holo-Chamber 3.0).
 */
object CollectionUiRenderer {

    private val circleClipPath = Path()
    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val scratchRect3 = RectF()
    private val capsuleRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)

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

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. TOP HUD PLAQUE
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawHeaderPlaque(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        unlockedCount: Int,
        totalSkins: Int,
        bestStreak: Int,
        hypeBalance: Int,
        formatHypeAmount: (Int) -> String,
        backButton: RectF,
        restoreButton: RectF,
        activeCollectionIndex: Int,
        drawUiButtonFrame: (Canvas, RectF, Boolean, Int, Float) -> Unit,
        drawUiIconAsset: (Canvas, String, RectF, Float, Int) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val plaqueHeight = 44f * dp
        val plaqueBottom = top + plaqueHeight
        scratchRect.set(left, top, right, plaqueBottom)

        // Frosted plaque slab
        paint.style = Paint.Style.FILL
        paint.color = 0xF4081324.toInt()
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)

        // Laser perimeter
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(
            left, top, right, plaqueBottom,
            intArrayOf(0xFF1DE8C8.toInt(), 0x5500E5FF, 0xFFFF4D8D.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 10f * dp, 10f * dp, paint)
        paint.shader = null

        // Specular top edge
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + 14f * dp, top + 1f * dp, right - 14f * dp, top + 1f * dp, paint)

        // Protocol kicker
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = UiTypography.EYEBROW_DP * dp
        textPaint.color = 0xFF1DE8C8.toInt()
        val kicker = fitText(t("Brainrot Vault // 50 Meme Legends").uppercase(), (right - left - 28f * dp).coerceAtLeast(50f * dp))
        canvas.drawText("✦ $kicker ✦", left + 14f * dp, top + 14f * dp, textPaint)

        // Main title
        textPaint.textSize = UiTypography.PANEL_TITLE_DP * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.setShadowLayer(8f * dp, 0f, 0f, 0x881DE8C8.toInt())
        val titleText = fitText(t("COLLECTION").uppercase(), (restoreButton.left - left - 24f * dp).coerceAtLeast(80f * dp))
        canvas.drawText(titleText, left + 14f * dp, top + 34f * dp, textPaint)
        textPaint.clearShadowLayer()

        // Back button
        val backActive = activeCollectionIndex == CollectionTouchController.COLLECTION_BACK_INDEX
        drawUiButtonFrame(canvas, backButton, backActive, 0xFF1DE8C8.toInt(), 8f)
        drawUiIconAsset(canvas, "ui_back", backButton, -1f, 245)

        // Restore purchases button
        val restoreActive = activeCollectionIndex == CollectionTouchController.COLLECTION_RESTORE_INDEX
        drawUiButtonFrame(canvas, restoreButton, restoreActive, 0xFF8AA6FF.toInt(), 8f)
        drawUiIconAsset(canvas, "ui_restore", restoreButton, 4f, if (restoreActive) 255 else 220)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. HERO QUANTUM HOLO-STAGE & SPECIMEN PEDESTAL (INNOVATIVE CENTERPIECE)
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawHeroHoloStage(
        canvas: Canvas,
        stageRect: RectF,
        actionRect: RectF,
        focusedSkin: BallSkin,
        selectedSkin: BallSkin,
        unlocked: Boolean,
        hypeBalance: Int,
        artBitmap: Bitmap?,
        menuPulse: Float,
        activeIndex: Int,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        powerIconKey: (BallPower) -> String,
        ballPowerName: (BallPower) -> String,
        ballPowerDescription: (BallPower) -> String,
        unlockShortLabel: (BallSkin) -> String,
        unlockLongLabel: (BallSkin) -> String,
        premiumCompactPriceLabel: (BallSkin) -> String,
        skinHypePrice: (BallSkin) -> Int?,
        formatHypeAmount: (Int) -> String
    ) {
        val accent = brainballRarityColor(focusedSkin)
        val isEquipped = selectedSkin.id == focusedSkin.id
        val premium = focusedSkin.unlock.type == UnlockType.PREMIUM
        val hypePrice = skinHypePrice(focusedSkin) ?: if (focusedSkin.unlock.type == UnlockType.HYPE_COST) focusedSkin.unlock.value else null
        val canAffordHype = !unlocked && !premium && hypePrice != null && hypeBalance >= hypePrice
        val powered = focusedSkin.power != BallPower.NONE

        // Stage Chassis slab
        paint.style = Paint.Style.FILL
        paint.color = 0xF60B1220.toInt()
        canvas.drawRoundRect(stageRect, 12f * dp, 12f * dp, paint)

        // Tech background gradient
        paint.shader = LinearGradient(
            stageRect.left, stageRect.top, stageRect.right, stageRect.bottom,
            intArrayOf(withAlpha(accent, 45), 0x00000000, withAlpha(accent, 25)),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(stageRect, 12f * dp, 12f * dp, paint)
        paint.shader = null

        // Laser border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = withAlpha(accent, 200)
        canvas.drawRoundRect(stageRect, 12f * dp, 12f * dp, paint)

        // Corner brackets
        val bracketLen = 14f * dp
        paint.strokeWidth = 2.4f * dp
        paint.color = accent
        // Top-left
        canvas.drawLine(stageRect.left, stageRect.top + bracketLen, stageRect.left, stageRect.top, paint)
        canvas.drawLine(stageRect.left, stageRect.top, stageRect.left + bracketLen, stageRect.top, paint)
        // Top-right
        canvas.drawLine(stageRect.right - bracketLen, stageRect.top, stageRect.right, stageRect.top, paint)
        canvas.drawLine(stageRect.right, stageRect.top, stageRect.right, stageRect.top + bracketLen, paint)
        // Bottom-left
        canvas.drawLine(stageRect.left, stageRect.bottom - bracketLen, stageRect.left, stageRect.bottom, paint)
        canvas.drawLine(stageRect.left, stageRect.bottom, stageRect.left + bracketLen, stageRect.bottom, paint)
        // Bottom-right
        canvas.drawLine(stageRect.right - bracketLen, stageRect.bottom, stageRect.right, stageRect.bottom, paint)
        canvas.drawLine(stageRect.right, stageRect.bottom, stageRect.right, stageRect.bottom - bracketLen, paint)

        // Left bay: Holographic Pedestal and 3D floating character
        val pedestalBayW = minOf(stageRect.width() * 0.38f, stageRect.height() * 1.05f)
        val pCx = stageRect.left + pedestalBayW * 0.5f
        val pCy = stageRect.bottom - 22f * dp

        // Under-pedestal glow
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            pCx, pCy, pedestalBayW * 0.55f,
            intArrayOf(withAlpha(accent, 90), withAlpha(accent, 28), 0x00000000),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        scratchRect.set(pCx - pedestalBayW * 0.55f, pCy - 16f * dp, pCx + pedestalBayW * 0.55f, pCy + 16f * dp)
        canvas.drawOval(scratchRect, paint)
        paint.shader = null

        // 3D Elliptical holo ring (base)
        val ellipRx = pedestalBayW * 0.42f
        val ellipRy = 11f * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.color = withAlpha(accent, 220)
        scratchRect.set(pCx - ellipRx, pCy - ellipRy, pCx + ellipRx, pCy + ellipRy)
        canvas.drawOval(scratchRect, paint)

        // Inner dashed ring
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 160)
        scratchRect.set(pCx - ellipRx * 0.65f, pCy - ellipRy * 0.65f, pCx + ellipRx * 0.65f, pCy + ellipRy * 0.65f)
        canvas.drawOval(scratchRect, paint)

        // Vertical containment laser beams
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, 85)
        canvas.drawLine(pCx - ellipRx * 0.85f, pCy, pCx - ellipRx * 0.85f, stageRect.top + 18f * dp, paint)
        canvas.drawLine(pCx + ellipRx * 0.85f, pCy, pCx + ellipRx * 0.85f, stageRect.top + 18f * dp, paint)

        // Floating Hero Brainball (with sinusoidal breathing hover)
        val hoverOffset = sin(menuPulse.toDouble() * 2.8).toFloat() * 3.5f * dp
        val heroRadius = minOf(pedestalBayW * 0.35f, stageRect.height() * 0.30f)
        val heroCx = pCx
        val heroCy = pCy - ellipRy - heroRadius - 4f * dp + hoverOffset

        // Character aura halo
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            heroCx, heroCy, heroRadius * 1.55f,
            intArrayOf(withAlpha(accent, if (unlocked) 95 else 45), 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(heroCx, heroCy, heroRadius * 1.55f, paint)
        paint.shader = null

        // Render Hero 3D Brainball Art
        val saveCount = canvas.save()
        circleClipPath.rewind()
        circleClipPath.addCircle(heroCx, heroCy, heroRadius, Path.Direction.CW)
        canvas.clipPath(circleClipPath)

        if (artBitmap != null && !artBitmap.isRecycled) {
            paint.style = Paint.Style.FILL
            paint.alpha = if (unlocked || premium) 255 else 215
            paint.isFilterBitmap = true
            scratchRect2.set(heroCx - heroRadius, heroCy - heroRadius, heroCx + heroRadius, heroCy + heroRadius)
            canvas.drawBitmap(artBitmap, null, scratchRect2, paint)
            paint.alpha = 255
        } else {
            paint.style = Paint.Style.FILL
            paint.color = if (unlocked) focusedSkin.primary else 0xFF2A3342.toInt()
            canvas.drawCircle(heroCx, heroCy, heroRadius, paint)
        }
        canvas.restoreToCount(saveCount)

        // Outer precision rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.color = withAlpha(accent, if (unlocked) 240 else 130)
        canvas.drawCircle(heroCx, heroCy, heroRadius, paint)

        // Lock overlay on pedestal if locked (anchored to bottom-right rim of hero orb so face remains completely visible)
        if (!unlocked && !premium) {
            val lockRadius = 13.5f * dp
            val lockCx = heroCx + heroRadius * 0.68f
            val lockCy = heroCy + heroRadius * 0.68f
            paint.style = Paint.Style.FILL
            paint.color = 0xEE080D15.toInt()
            canvas.drawCircle(lockCx, lockCy, lockRadius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.3f * dp
            paint.color = 0xFFFF4D8D.toInt()
            canvas.drawCircle(lockCx, lockCy, lockRadius, paint)
            BallSkinRenderer.drawLock(canvas, lockCx, lockCy, lockRadius * 0.85f, paint)
        }

        // Equipped Badge on Pedestal
        if (isEquipped) {
            val eqW = 76f * dp
            val eqH = 16f * dp
            scratchRect.set(pCx - eqW * 0.5f, pCy + 4f * dp, pCx + eqW * 0.5f, pCy + 4f * dp + eqH)
            paint.style = Paint.Style.FILL
            paint.color = 0xEE09261E.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0xFF64E572.toInt()
            canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
            textPaint.textSize = 7.5f * dp
            textPaint.color = 0xFF64E572.toInt()
            canvas.drawText("✔ ${t("EQUIPPED").uppercase()}", pCx, pCy + 15.5f * dp, textPaint)
        }

        // Right bay: Specimen Dossier & Metrics
        val dossierLeft = stageRect.left + pedestalBayW + 12f * dp
        val dossierRight = stageRect.right - 14f * dp
        val dossierW = dossierRight - dossierLeft

        // 1. Rarity pill
        val rarityStr = brainballRarity(focusedSkin, t)
        val rarityPillW = minOf(dossierW * 0.52f, 126f * dp)
        val rarityPillH = 16f * dp
        val topPillY = stageRect.top + 11f * dp
        scratchRect.set(dossierLeft, topPillY, dossierLeft + rarityPillW, topPillY + rarityPillH)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 45)
        canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, 220)
        canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 7.5f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText("✦ $rarityStr", rarityPillW - 8f * dp), scratchRect.centerX(), scratchRect.centerY() + 2.5f * dp, textPaint)

        // 2. Aura score pill (top right)
        val auraW = 86f * dp
        scratchRect.set(dossierRight - auraW, topPillY, dossierRight, topPillY + rarityPillH)
        paint.style = Paint.Style.FILL
        paint.color = 0x44FFCF4A.toInt()
        canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0xFFFFCF4A.toInt()
        canvas.drawRoundRect(scratchRect, 5f * dp, 5f * dp, paint)
        val auraVal = CollectionTouchController.calculateAura(focusedSkin, emptyList())
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText("👑 +$auraVal ${t("AURA").uppercase()}", scratchRect.centerX(), scratchRect.centerY() + 2.5f * dp, textPaint)

        // 3. Specimen Name (clean, generous clearance from rarity pill above)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 19f * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.setShadowLayer(3f * dp, 0f, 1.5f * dp, 0xCC000000.toInt())
        val fitName = fitText(focusedSkin.name, dossierW)
        canvas.drawText(fitName, dossierLeft, stageRect.top + 52f * dp, textPaint)
        textPaint.clearShadowLayer()

        // 4. Lore quote
        textPaint.typeface = Typeface.create("sans-serif", Typeface.ITALIC)
        textPaint.textSize = 9.2f * dp
        textPaint.color = 0xDDF7F4FF.toInt()
        val quote = "„${focusedSkin.subtitle}”"
        val fitQuote = fitText(quote, dossierW)
        canvas.drawText(fitQuote, dossierLeft, stageRect.top + 69f * dp, textPaint)

        // 5. Superpower description ribbon OR Core Telemetry ribbon
        val ribbonH = 22f * dp
        val ribbonTop = stageRect.top + 80f * dp
        val ribbonW = if (ribbonTop + ribbonH <= actionRect.top) dossierW else minOf(dossierW, actionRect.left - dossierLeft - 8f * dp).coerceAtLeast(dossierW * 0.48f)
        scratchRect.set(dossierLeft, ribbonTop, dossierLeft + ribbonW, ribbonTop + ribbonH)

        if (powered) {
            paint.style = Paint.Style.FILL
            paint.color = 0x3364E572.toInt()
            canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0xFF64E572.toInt()
            canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)

            val pIconSize = 14f * dp
            scratchRect2.set(dossierLeft + 5f * dp, ribbonTop + 4f * dp, dossierLeft + 5f * dp + pIconSize, ribbonTop + 4f * dp + pIconSize)
            drawWorldAsset(canvas, powerIconKey(focusedSkin.power), scratchRect2, 255)

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
            textPaint.textSize = 7.8f * dp
            textPaint.color = 0xFF64E572.toInt()
            val pName = ballPowerName(focusedSkin.power)
            val pDesc = ballPowerDescription(focusedSkin.power)
            canvas.drawText(fitText("⚡ $pName: $pDesc", ribbonW - 24f * dp), dossierLeft + 22f * dp, ribbonTop + 14.5f * dp, textPaint)
        } else {
            paint.style = Paint.Style.FILL
            paint.color = 0x2200E5FF.toInt()
            canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * dp
            paint.color = 0x8800E5FF.toInt()
            canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
            textPaint.textSize = 7.4f * dp
            textPaint.color = 0xFF00E5FF.toInt()
            val syncText = "✦ ${t("ACTIVE").uppercase()} // ${t("RIFT RESONANCE MAX").uppercase()}"
            canvas.drawText(fitText(syncText, ribbonW - 12f * dp), dossierLeft + 8f * dp, ribbonTop + 14.5f * dp, textPaint)
        }

        // 6. Dynamic Hero Action CTA Button (Tactile 3D Candy Arcade)
        val btnActive = activeIndex == CollectionTouchController.COLLECTION_HERO_ACTION_INDEX
        val cornerRad = 10f * dp
        val depth = 4.5f * dp
        val pressOffset = if (btnActive) (depth * 0.75f) else 0f

        // Bottom 3D Lip
        scratchRect2.set(actionRect.left, actionRect.top + depth, actionRect.right, actionRect.bottom)
        paint.style = Paint.Style.FILL
        paint.shader = null
        val lipColor = when {
            isEquipped -> 0xFF005826.toInt()
            unlocked -> 0xFF00586B.toInt()
            premium -> 0xFF6B0033.toInt()
            canAffordHype -> 0xFF7A3E00.toInt()
            else -> 0xFF08121E.toInt()
        }
        paint.color = lipColor
        canvas.drawRoundRect(scratchRect2, cornerRad, cornerRad, paint)

        // Main Button Face
        scratchRect.set(actionRect.left, actionRect.top + pressOffset, actionRect.right, actionRect.bottom - depth + pressOffset)

        when {
            isEquipped -> {
                // Vibrant Slime Emerald 3D Candy Button
                paint.shader = LinearGradient(
                    scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
                    0xFF00E676.toInt(), 0xFF00A843.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)
                paint.shader = null

                // Specular curved glare
                capsuleRect.set(scratchRect.left + 2f * dp, scratchRect.top + 1.2f * dp, scratchRect.right - 2f * dp, scratchRect.top + scratchRect.height() * 0.46f)
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0x85FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
                paint.shader = null

                // White Specular Line
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f * dp
                paint.color = 0xAAFFFFFF.toInt()
                canvas.drawLine(scratchRect.left + cornerRad, scratchRect.top + 1f * dp, scratchRect.right - cornerRad, scratchRect.top + 1f * dp, paint)

                paint.strokeWidth = 2f * dp
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 11.5f * dp
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0xAA000000.toInt())
                canvas.drawText("✔ ${t("EQUIPPED").uppercase()}", scratchRect.centerX(), scratchRect.centerY() + 3.8f * dp, textPaint)
                textPaint.clearShadowLayer()
            }

            unlocked -> {
                // Electric Cyan Candy 3D Button
                paint.shader = LinearGradient(
                    scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
                    0xFF00F5D4.toInt(), 0xFF0099B8.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)
                paint.shader = null

                capsuleRect.set(scratchRect.left + 2f * dp, scratchRect.top + 1.2f * dp, scratchRect.right - 2f * dp, scratchRect.top + scratchRect.height() * 0.46f)
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0x85FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f * dp
                paint.color = 0xAAFFFFFF.toInt()
                canvas.drawLine(scratchRect.left + cornerRad, scratchRect.top + 1f * dp, scratchRect.right - cornerRad, scratchRect.top + 1f * dp, paint)

                paint.strokeWidth = 2f * dp
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 12f * dp
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0xAA000000.toInt())
                canvas.drawText("${t("EQUIP").uppercase()} ►", scratchRect.centerX(), scratchRect.centerY() + 4f * dp, textPaint)
                textPaint.clearShadowLayer()
            }

            premium -> {
                // Hot Magenta Candy 3D Button
                paint.shader = LinearGradient(
                    scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
                    0xFFFF1493.toInt(), 0xFFBA0058.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)
                paint.shader = null

                capsuleRect.set(scratchRect.left + 2f * dp, scratchRect.top + 1.2f * dp, scratchRect.right - 2f * dp, scratchRect.top + scratchRect.height() * 0.46f)
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0x85FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f * dp
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 11f * dp
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0xAA000000.toInt())
                canvas.drawText("💎 ${t("GET").uppercase()} ${premiumCompactPriceLabel(focusedSkin)}", scratchRect.centerX(), scratchRect.centerY() + 3.8f * dp, textPaint)
                textPaint.clearShadowLayer()
            }

            canAffordHype -> {
                // Golden Flame 3D Button
                paint.shader = LinearGradient(
                    scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
                    0xFFFFD600.toInt(), 0xFFFF6D00.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)
                paint.shader = null

                capsuleRect.set(scratchRect.left + 2f * dp, scratchRect.top + 1.2f * dp, scratchRect.right - 2f * dp, scratchRect.top + scratchRect.height() * 0.46f)
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0x85FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(capsuleRect, cornerRad - 2f * dp, cornerRad - 2f * dp, paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f * dp
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 11f * dp
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.setShadowLayer(4f * dp, 0f, 1.5f * dp, 0xAA000000.toInt())
                canvas.drawText("⚡ ${t("UNLOCK").uppercase()} (${formatHypeAmount(hypePrice!!)} HYPE)", scratchRect.centerX(), scratchRect.centerY() + 3.8f * dp, textPaint)
                textPaint.clearShadowLayer()
            }

            hypePrice != null -> {
                paint.color = 0xFF14243A.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.4f * dp
                paint.color = 0xCCFFCF4A.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 9.2f * dp
                textPaint.color = 0xFFFFCF4A.toInt()
                val missing = (hypePrice - hypeBalance).coerceAtLeast(0)
                canvas.drawText("🔒 ${formatHypeAmount(hypeBalance)} / ${formatHypeAmount(hypePrice)} ${t("HYPE").uppercase()} (+${formatHypeAmount(missing)})", scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
            }

            else -> {
                paint.color = 0xFF14243A.toInt()
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f * dp
                paint.color = 0x66FFFFFF
                canvas.drawRoundRect(scratchRect, cornerRad, cornerRad, paint)

                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 9.5f * dp
                textPaint.color = 0xAAFFFFFF.toInt()
                canvas.drawText("🔒 ${unlockLongLabel(focusedSkin)}", scratchRect.centerX(), scratchRect.centerY() + 3.2f * dp, textPaint)
            }
        }

        // Specular top highlight on action button
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(actionRect.left + 6f * dp, actionRect.top + 1f * dp, actionRect.right - 6f * dp, actionRect.top + 1f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. CATEGORY FILTERS
    // ─────────────────────────────────────────────────────────────────────────────
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
                CollectionFilter.ALL -> 0xFF00E5FF.toInt()
                CollectionFilter.OWNED -> 0xFF00FFA3.toInt()
                CollectionFilter.SUPERPOWER -> 0xFFFFCF4A.toInt()
                CollectionFilter.HYPE -> 0xFF1DE8C8.toInt()
                CollectionFilter.PREMIUM -> 0xFFFF2E93.toInt()
                CollectionFilter.COSMETIC -> 0xFF8AA6FF.toInt()
            }
            val corner = 8f * dp
            val depth = (2.2f * dp).coerceAtLeast(2f)
            val pressOffset = if (active) 1.2f * dp else 0f

            if (selected) {
                // 3D bottom extrusion lip
                scratchRect.set(rect.left, rect.top + depth, rect.right, rect.bottom)
                paint.style = Paint.Style.FILL
                paint.color = withAlpha(accent, 150)
                canvas.drawRoundRect(scratchRect, corner, corner, paint)

                // 3D raised candy tab face
                capsuleRect.set(rect.left, rect.top + pressOffset, rect.right, rect.bottom - depth + pressOffset)
                paint.shader = LinearGradient(
                    capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom,
                    accent, withAlpha(accent, 190),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(capsuleRect, corner, corner, paint)
                paint.shader = null

                // Curved glossy specular glare
                scratchRect.set(capsuleRect.left + 1.2f * dp, capsuleRect.top + 0.8f * dp, capsuleRect.right - 1.2f * dp, capsuleRect.top + capsuleRect.height() * 0.48f)
                paint.shader = LinearGradient(
                    scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
                    0x85FFFFFF.toInt(), 0x05FFFFFF,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(scratchRect, (corner - 1.2f * dp).coerceAtLeast(2f), (corner - 1.2f * dp).coerceAtLeast(2f), paint)
                paint.shader = null

                // Specular top highlight line
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f * dp
                paint.color = 0xCCFFFFFF.toInt()
                canvas.drawLine(capsuleRect.left + 4f * dp, capsuleRect.top + 0.8f * dp, capsuleRect.right - 4f * dp, capsuleRect.top + 0.8f * dp, paint)

                // White border
                paint.strokeWidth = 1.6f * dp
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawRoundRect(capsuleRect, corner, corner, paint)

                // Glowing indicator dot
                paint.style = Paint.Style.FILL
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(capsuleRect.left + 7f * dp, capsuleRect.centerY(), 2.2f * dp, paint)
            } else {
                // Unselected 3D tab slab
                scratchRect.set(rect.left, rect.top + depth, rect.right, rect.bottom)
                paint.style = Paint.Style.FILL
                paint.color = 0xFF040812.toInt()
                canvas.drawRoundRect(scratchRect, corner, corner, paint)

                capsuleRect.set(rect.left, rect.top + pressOffset, rect.right, rect.bottom - depth + pressOffset)
                paint.color = if (active) withAlpha(accent, 45) else 0xCC091220.toInt()
                canvas.drawRoundRect(capsuleRect, corner, corner, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = (if (active) 1.2f else 0.8f) * dp
                paint.color = withAlpha(accent, if (active) 170 else 75)
                canvas.drawRoundRect(capsuleRect, corner, corner, paint)
            }

            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
            textPaint.textSize = 8.2f * dp
            textPaint.color = if (selected) 0xFFFFFFFF.toInt() else withAlpha(0xFFF7F4FF.toInt(), 160)
            if (selected) {
                textPaint.setShadowLayer(3f * dp, 0f, 1f * dp, 0x88000000.toInt())
            }
            val padLeft = if (selected) 6f * dp else 0f
            val iconPrefix = when (filter) {
                CollectionFilter.ALL -> "✦ "
                CollectionFilter.OWNED -> "✔ "
                CollectionFilter.SUPERPOWER -> "⚡ "
                CollectionFilter.HYPE -> "💎 "
                CollectionFilter.PREMIUM -> "👑 "
                CollectionFilter.COSMETIC -> "✨ "
            }
            val filterText = fitText("$iconPrefix${t(filter.labelKey).uppercase()}", rect.width() - 8f * dp - padLeft)
            canvas.drawText(
                filterText,
                rect.centerX() + padLeft * 0.5f,
                (if (selected) capsuleRect.centerY() else rect.centerY()) + 3f * dp,
                textPaint
            )
            textPaint.clearShadowLayer()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3.1 TACTICAL TOOLBAR & DYNAMIC SORT BUTTON
    // ─────────────────────────────────────────────────────────────────────────────
    fun sortDisplayName(sort: com.moonsolstudios.kavvoro.model.CollectionSort, t: (String) -> String): String = when (sort) {
        com.moonsolstudios.kavvoro.model.CollectionSort.AURA_DESC -> t("AURA").uppercase()
        com.moonsolstudios.kavvoro.model.CollectionSort.RARITY -> t("RARITY").uppercase()
        com.moonsolstudios.kavvoro.model.CollectionSort.POWER_FIRST -> t("POWER").uppercase()
        com.moonsolstudios.kavvoro.model.CollectionSort.NAME_ASC -> "A-Z"
        com.moonsolstudios.kavvoro.model.CollectionSort.OWNED_FIRST -> t("OWNED").uppercase()
    }

    fun drawToolbarAndSort(
        canvas: Canvas,
        sortRect: RectF,
        contentLeft: Float,
        currentSort: com.moonsolstudios.kavvoro.model.CollectionSort,
        activeIndex: Int,
        totalItemsCount: Int,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        if (sortRect.isEmpty) return

        val sortActive = activeIndex == CollectionTouchController.COLLECTION_SORT_INDEX
        val sortLabel = "⇅ ${sortDisplayName(currentSort, t)}"

        // Left telemetry status
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 7.8f * dp
        textPaint.color = 0xBB1DE8C8.toInt()
        val telemText = "✦ $totalItemsCount ${t("COLLECTION").uppercase()} // ${t("VAULT").uppercase()}"
        canvas.drawText(fitText(telemText, (sortRect.left - contentLeft - 10f * dp).coerceAtLeast(60f * dp)), contentLeft + 4f * dp, sortRect.centerY() + 2.8f * dp, textPaint)

        // Sort button 3D slab
        val depth = 2.2f * dp
        val pressOffset = if (sortActive) 1.2f * dp else 0f
        val corner = 6f * dp

        // 3D bottom lip
        scratchRect.set(sortRect.left, sortRect.top + depth, sortRect.right, sortRect.bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF031622.toInt()
        canvas.drawRoundRect(scratchRect, corner, corner, paint)

        // Raised sort face
        capsuleRect.set(sortRect.left, sortRect.top + pressOffset, sortRect.right, sortRect.bottom - depth + pressOffset)
        paint.shader = LinearGradient(
            capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom,
            if (sortActive) 0xFF00E5FF.toInt() else 0xFF143048.toInt(),
            if (sortActive) 0xFF0088A8.toInt() else 0xFF081A28.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(capsuleRect, corner, corner, paint)
        paint.shader = null

        // Specular top glare
        scratchRect.set(capsuleRect.left + 1.2f * dp, capsuleRect.top + 0.8f * dp, capsuleRect.right - 1.2f * dp, capsuleRect.top + capsuleRect.height() * 0.48f)
        paint.shader = LinearGradient(scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom, 0x66FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, (corner - 1.2f * dp).coerceAtLeast(2f), (corner - 1.2f * dp).coerceAtLeast(2f), paint)
        paint.shader = null

        // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (sortActive) 1.6f else 1f) * dp
        paint.color = if (sortActive) 0xFFFFFFFF.toInt() else 0xFF1DE8C8.toInt()
        canvas.drawRoundRect(capsuleRect, corner, corner, paint)

        // Sort button label
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 8.2f * dp
        textPaint.color = if (sortActive) 0xFFFFFFFF.toInt() else 0xFF1DE8C8.toInt()
        canvas.drawText(fitText(sortLabel, sortRect.width() - 8f * dp), sortRect.centerX(), sortRect.centerY() + 2.8f * dp, textPaint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. VERTICAL CYBER-HOLO SPECIMEN PODS (AAA CONTAINMENT CAPSULES)
    // ─────────────────────────────────────────────────────────────────────────────
    fun drawSpecimenPod(
        canvas: Canvas,
        rect: RectF,
        index: Int,
        skin: BallSkin,
        skinIndex: Int,
        unlocked: Boolean,
        selected: Boolean,
        focused: Boolean,
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
        premiumCompactPriceLabel: (BallSkin) -> String,
        skinHypePrice: (BallSkin) -> Int? = { null },
        formatHypeAmount: (Int) -> String = { it.toString() }
    ) {
        val premium = skin.unlock.type == UnlockType.PREMIUM
        val hypePrice = skinHypePrice(skin) ?: if (skin.unlock.type == UnlockType.HYPE_COST) skin.unlock.value else null
        val hypeReady = !unlocked && !premium && hypePrice != null && hypeBalance >= hypePrice
        val powered = skin.power != BallPower.NONE
        val active = activeIndex == index
        val rarity = brainballRarity(skin, t)
        val rarityColor = brainballRarityColor(skin)
        val locked = !unlocked && !premium
        val corner = 10f * dp

        // 1. Pod Base Slab (Polycarbonate Obsidian)
        paint.style = Paint.Style.FILL
        paint.color = when {
            selected -> 0xF80A1F2C.toInt()
            focused -> 0xF81A162A.toInt()
            unlocked -> 0xF60B1422.toInt()
            else -> 0xF4070C14.toInt()
        }
        canvas.drawRoundRect(rect, corner, corner, paint)

        // 2. Diagonal Specular Sheen
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(0x30FFFFFF, 0x05FFFFFF, 0x14000000),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        // 3. Top Specular Laser Line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x50FFFFFF
        canvas.drawLine(rect.left + 8f * dp, rect.top + 1f * dp, rect.right - 8f * dp, rect.top + 1f * dp, paint)

        // 4. Laser Perimeter & Chevrons
        val borderColor = when {
            selected -> 0xFF1DE8C8.toInt()
            focused -> 0xFFFFCF4A.toInt()
            active -> withAlpha(rarityColor, 240)
            unlocked -> withAlpha(rarityColor, 150)
            else -> 0x33FFFFFF
        }
        paint.strokeWidth = (if (selected || focused || active) 1.8f else 1f) * dp
        paint.color = borderColor
        canvas.drawRoundRect(rect, corner, corner, paint)

        if (selected || focused) {
            val bLen = 8f * dp
            paint.strokeWidth = 2.2f * dp
            paint.color = if (selected) 0xFF1DE8C8.toInt() else 0xFFFFCF4A.toInt()
            // Top-left
            canvas.drawLine(rect.left, rect.top + bLen, rect.left, rect.top, paint)
            canvas.drawLine(rect.left, rect.top, rect.left + bLen, rect.top, paint)
            // Top-right
            canvas.drawLine(rect.right - bLen, rect.top, rect.right, rect.top, paint)
            canvas.drawLine(rect.right, rect.top, rect.right, rect.top + bLen, paint)
            // Bottom-left
            canvas.drawLine(rect.left, rect.bottom - bLen, rect.left, rect.bottom, paint)
            canvas.drawLine(rect.left, rect.bottom, rect.left + bLen, rect.bottom, paint)
            // Bottom-right
            canvas.drawLine(rect.right - bLen, rect.bottom, rect.right, rect.bottom, paint)
            canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - bLen, paint)
        }

        // 5. Top Telemetry Header (Rarity / Aura)
        val topY = rect.top + 13f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 7.2f * dp
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = withAlpha(rarityColor, if (unlocked) 240 else 150)
        val shortRarity = when {
            premium -> "✦ ${t("MYTHIC BRAINROT").split(" ").firstOrNull() ?: "MYTHIC"}"
            powered -> "⚡ ${t("POWERED").uppercase()}"
            selected -> "✔ ${t("EQUIPPED").uppercase()}"
            unlocked -> "● ${t("ACTIVE")}"
            else -> "🔒 ${t("UNLOCK").uppercase()}"
        }
        canvas.drawText(fitText(shortRarity, (rect.width() - 46f * dp).coerceAtLeast(28f * dp)), rect.left + 7f * dp, topY, textPaint)

        val auraVal = CollectionTouchController.calculateAura(skin, emptyList())
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), if (unlocked) 245 else 160)
        canvas.drawText("✦ $auraVal", rect.right - 7f * dp, topY, textPaint)

        // 6. Cylindrical Containment Well & Character Art (Prominent & Large!)
        val tubeCx = rect.centerX()
        val tubeRadius = minOf(rect.width() * 0.28f, (rect.height() - 58f * dp) * 0.32f).coerceIn(24f * dp, 34f * dp)
        val tubeCy = rect.top + 18f * dp + tubeRadius

        // Under-pedestal glow
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            tubeCx, tubeCy, tubeRadius * 1.5f,
            intArrayOf(withAlpha(rarityColor, if (unlocked) 80 else 25), 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(tubeCx, tubeCy, tubeRadius * 1.5f, paint)
        paint.shader = null

        // 3D Brainball Art
        val saveCount = canvas.save()
        circleClipPath.rewind()
        circleClipPath.addCircle(tubeCx, tubeCy, tubeRadius, Path.Direction.CW)
        canvas.clipPath(circleClipPath)

        if (artBitmap != null && !artBitmap.isRecycled) {
            paint.style = Paint.Style.FILL
            paint.alpha = if (unlocked || premium) 255 else 205
            paint.isFilterBitmap = true
            scratchRect3.set(tubeCx - tubeRadius, tubeCy - tubeRadius, tubeCx + tubeRadius, tubeCy + tubeRadius)
            canvas.drawBitmap(artBitmap, null, scratchRect3, paint)
            paint.alpha = 255
        } else {
            paint.style = Paint.Style.FILL
            paint.color = if (unlocked) skin.primary else 0xFF242C38.toInt()
            canvas.drawCircle(tubeCx, tubeCy, tubeRadius, paint)
        }
        canvas.restoreToCount(saveCount)

        // Outer precision ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (selected) 2f else 1.2f) * dp
        paint.color = withAlpha(if (selected) 0xFF1DE8C8.toInt() else rarityColor, if (unlocked) 235 else 120)
        canvas.drawCircle(tubeCx, tubeCy, tubeRadius, paint)

        // Lock badge on character rim if locked (non-intrusive bottom-right badge, face remains completely visible)
        if (locked) {
            val lRad = tubeRadius * 0.28f
            val lockCx = tubeCx + tubeRadius * 0.65f
            val lockCy = tubeCy + tubeRadius * 0.65f
            paint.style = Paint.Style.FILL
            paint.color = 0xEE070B12.toInt()
            canvas.drawCircle(lockCx, lockCy, lRad, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.1f * dp
            paint.color = 0xFFFF4D8D.toInt()
            canvas.drawCircle(lockCx, lockCy, lRad, paint)
            BallSkinRenderer.drawLock(canvas, lockCx, lockCy, lRad * 0.85f, paint)
        }

        // 7. Specimen Name Centered Below Character
        val nameY = tubeCy + tubeRadius + 14f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 9.8f * dp
        textPaint.color = if (unlocked || premium) 0xFFF7F4FF.toInt() else 0xCCFFFFFF.toInt()
        val fitSkinName = fitText(skin.name.uppercase(), rect.width() - 8f * dp)
        canvas.drawText(fitSkinName, tubeCx, nameY, textPaint)

        // 8. Tactical Status / Price Pill at the Base (3D Candy Arcade Pill)
        val pillH = 20f * dp
        val pillW = rect.width() - 14f * dp
        val pillLeft = rect.left + 7f * dp
        val pillTop = rect.bottom - pillH - 6f * dp
        val pDepth = 1.8f * dp
        val pCorner = 5.5f * dp

        val pillText = when {
            selected -> "✔ ${t("EQUIPPED").uppercase()}"
            unlocked -> t("EQUIP").uppercase()
            premium -> "💎 ${premiumCompactPriceLabel(skin)}"
            hypeReady -> "⚡ ${formatHypeAmount(hypePrice!!)}"
            hypePrice != null -> "🔒 ${formatHypeAmount(hypePrice)}"
            else -> "🔒 ${unlockShortLabel(skin)}"
        }

        // 3D Bottom Lip
        scratchRect.set(pillLeft, pillTop + pDepth, pillLeft + pillW, pillTop + pillH)
        paint.style = Paint.Style.FILL
        paint.color = when {
            selected -> 0xFF00582B.toInt()
            unlocked -> 0xFF004D5C.toInt()
            premium -> 0xFF6B0033.toInt()
            hypeReady -> 0xFF7A3800.toInt()
            else -> 0xFF050B14.toInt()
        }
        canvas.drawRoundRect(scratchRect, pCorner, pCorner, paint)

        // Raised Pill Face
        capsuleRect.set(pillLeft, pillTop, pillLeft + pillW, pillTop + pillH - pDepth)
        when {
            selected -> {
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0xFF00E676.toInt(), 0xFF009940.toInt(), Shader.TileMode.CLAMP)
            }
            unlocked -> {
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0xFF00F5D4.toInt(), 0xFF0090A8.toInt(), Shader.TileMode.CLAMP)
            }
            premium -> {
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0xFFFF1493.toInt(), 0xFFB00050.toInt(), Shader.TileMode.CLAMP)
            }
            hypeReady -> {
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0xFFFFD600.toInt(), 0xFFFF6D00.toInt(), Shader.TileMode.CLAMP)
            }
            else -> {
                paint.shader = LinearGradient(capsuleRect.left, capsuleRect.top, capsuleRect.left, capsuleRect.bottom, 0xFF182638.toInt(), 0xFF0C1624.toInt(), Shader.TileMode.CLAMP)
            }
        }
        canvas.drawRoundRect(capsuleRect, pCorner, pCorner, paint)
        paint.shader = null

        // Specular Top Glare
        scratchRect.set(capsuleRect.left + 1f * dp, capsuleRect.top + 0.6f * dp, capsuleRect.right - 1f * dp, capsuleRect.top + capsuleRect.height() * 0.48f)
        paint.shader = LinearGradient(scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom, 0x75FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, (pCorner - 1f * dp).coerceAtLeast(2f), (pCorner - 1f * dp).coerceAtLeast(2f), paint)
        paint.shader = null

        // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (selected || unlocked || premium || hypeReady) 1.2f * dp else 0.8f * dp
        paint.color = when {
            selected -> 0xFFFFFFFF.toInt()
            unlocked -> 0xFFE0FFFF.toInt()
            premium -> 0xFFFFD6EC.toInt()
            hypeReady -> 0xFFFFF0B3.toInt()
            else -> 0x55FFFFFF
        }
        canvas.drawRoundRect(capsuleRect, pCorner, pCorner, paint)

        // Text
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 7.8f * dp
        textPaint.color = if (selected || unlocked || premium || hypeReady) 0xFFFFFFFF.toInt() else 0xAAFFFFFF.toInt()
        if (selected || unlocked || premium || hypeReady) {
            textPaint.setShadowLayer(3f * dp, 0f, 1f * dp, 0x99000000.toInt())
        }
        canvas.drawText(fitText(pillText, pillW - 6f * dp), capsuleRect.centerX(), capsuleRect.centerY() + 2.8f * dp, textPaint)
        textPaint.clearShadowLayer()
    }

    fun drawItem(
        canvas: Canvas,
        rect: RectF,
        index: Int,
        skin: BallSkin,
        skinIndex: Int,
        unlocked: Boolean,
        selected: Boolean,
        focused: Boolean,
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
        premiumCompactPriceLabel: (BallSkin) -> String,
        skinHypePrice: (BallSkin) -> Int? = { null },
        formatHypeAmount: (Int) -> String = { it.toString() }
    ) = drawSpecimenPod(
        canvas, rect, index, skin, skinIndex, unlocked, selected, focused, hypeBalance, activeIndex,
        artBitmap, paint, dp, t, fitText, drawFittedText, drawWorldAsset, powerIconKey, ballPowerName,
        unlockShortLabel, premiumCompactPriceLabel, skinHypePrice, formatHypeAmount
    )

    // ─────────────────────────────────────────────────────────────────────────────
    // 5. MASTER COLLECTION DRAW CALL
    // ─────────────────────────────────────────────────────────────────────────────
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
        premiumCompactPriceLabel: (BallSkin) -> String,
        skinHypePrice: (BallSkin) -> Int? = { null },
        collectionSort: com.moonsolstudios.kavvoro.model.CollectionSort = com.moonsolstudios.kavvoro.model.CollectionSort.AURA_DESC,
        collectionSortButton: RectF = CollectionTouchController.sortButtonRect
    ) {
        // 1. Top HUD Plaque
        drawHeaderPlaque(
            canvas = canvas,
            left = pageContentLeft,
            top = safeTop22,
            right = pageContentRight,
            unlockedCount = unlockedCount,
            totalSkins = ballSkins.size,
            bestStreak = bestStreak,
            hypeBalance = hypeBalance,
            formatHypeAmount = formatHypeAmount,
            backButton = collectionBackButton,
            restoreButton = collectionRestoreButton,
            activeCollectionIndex = activeCollectionIndex,
            drawUiButtonFrame = drawUiButtonFrame,
            drawUiIconAsset = drawUiIconAsset,
            drawWorldAsset = drawWorldAsset,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )

        // 2. Hero Quantum Holo-Stage
        drawHeroHoloStage(
            canvas = canvas,
            stageRect = CollectionTouchController.heroStageRect,
            actionRect = CollectionTouchController.heroActionRect,
            focusedSkin = focusedSkin,
            selectedSkin = selectedSkin,
            unlocked = isSkinUnlocked(focusedSkin),
            hypeBalance = hypeBalance,
            artBitmap = brainballBitmap(focusedSkin),
            menuPulse = menuPulse,
            activeIndex = activeCollectionIndex,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            drawFittedText = drawFittedText,
            drawWorldAsset = drawWorldAsset,
            powerIconKey = powerIconKey,
            ballPowerName = ballPowerName,
            ballPowerDescription = ballPowerDescription,
            unlockShortLabel = unlockShortLabel,
            unlockLongLabel = unlockLongLabel,
            premiumCompactPriceLabel = premiumCompactPriceLabel,
            skinHypePrice = skinHypePrice,
            formatHypeAmount = formatHypeAmount
        )

        // 3. Category Filter Tabs
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

        // 3.1 Tactical Toolbar & Sort Button
        drawToolbarAndSort(
            canvas = canvas,
            sortRect = collectionSortButton,
            contentLeft = pageContentLeft,
            currentSort = collectionSort,
            activeIndex = activeCollectionIndex,
            totalItemsCount = ballSkins.size,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )

        // 4. Specimen Pods Matrix (Vertical Cyber-Holo Pods)
        val viewportTop = collectionViewportTop
        val viewportBottom = collectionViewportBottom
        canvas.save()
        canvas.clipRect(0f, viewportTop, viewWidth, viewportBottom)
        val orderedIndexes = CollectionTouchController.sortedFilteredIndexes(
            ballSkins = ballSkins,
            filter = collectionFilter,
            sort = collectionSort,
            isSkinUnlocked = isSkinUnlocked
        )
        for (skinIndex in orderedIndexes) {
            val skin = ballSkins[skinIndex]
            val rect = collectionItemRects.getOrNull(skinIndex) ?: continue
            if (rect.bottom >= viewportTop && rect.top <= viewportBottom) {
                drawSpecimenPod(
                    canvas = canvas,
                    rect = rect,
                    index = skinIndex,
                    skin = skin,
                    skinIndex = skinIndex,
                    unlocked = isSkinUnlocked(skin),
                    selected = selectedSkin.id == skin.id,
                    focused = focusedSkin.id == skin.id,
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
                    premiumCompactPriceLabel = premiumCompactPriceLabel,
                    skinHypePrice = skinHypePrice,
                    formatHypeAmount = formatHypeAmount
                )
            }
        }
        canvas.restore()

        // 5. Status / Radar Banner
        val footer = collectionMessage.ifBlank {
            nextRewardText?.let { "${t("NEXT MUTATION").uppercase()} / $it" } ?: t("VAULT COMPLETE / MAXIMUM BRAIN ACHIEVED")
        }
        UiWidgetRenderer.drawStatusBanner(
            canvas = canvas,
            left = pageContentLeft,
            top = safeBottom70,
            right = pageContentRight,
            bottom = safeBottom16,
            headerText = "✦ ${t(if (collectionMessage.isNotBlank()) "STATUS UPDATE" else "NEXT SIGNAL").uppercase()}",
            message = footer,
            accent = selectedSkin.lineColor,
            transient = collectionMessage.isNotBlank(),
            paint = paint,
            textPaint = textPaint,
            dp = dp,
            t = t,
            fitText = fitText
        )
    }
}
