package com.moonsolstudios.kavvoro.ui.screens.language

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.CyberShapeRenderer

/**
 * AAA Console-grade Procedural Vector Renderer for the Language Selector screen.
 * Resolution-independent, zero raster bitmap artifacts, 100% anti-aliased.
 */
object LanguageSelectorRenderer {

    private val backArrowPath = Path()
    private val cardPath = Path()
    private val cardGlow8Path = Path()
    private val cardGlow5Path = Path()
    private val cardGlow2Path = Path()
    private val cardInnerPath = Path()
    private val deckPath = Path()
    private val footerPath = Path()
    private val diamondPath = Path()
    private val flagClipPath = Path()
    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var currentPass: Int = 6

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

    /**
     * Single unified geometry generator for all language cards.
     * Guarantees identical chamfer clipping across left and right columns.
     */
    fun createLanguageCardPath(path: Path, bounds: RectF, corner: Float, notch: Float) {
        CyberShapeRenderer.createChamferPath(path, bounds, corner, notch)
    }

    fun drawBackButton(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        bmp: Bitmap? = null,
        paint: Paint,
        dp: Float
    ) {
        ensureCleanPaint(paint)
        val scale = (rect.width() / 36f).coerceAtLeast(0.5f)
        val corner = 7f * scale
        val notch = 4f * scale

        // Background: #06151F alpha ~75% (or 10% cyan when active)
        paint.style = Paint.Style.FILL
        paint.color = if (active) 0x1A00DFF2.toInt() else 0xBF06151F.toInt()
        CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)

        // Glow at pressed: 5-6px / ~15%
        if (active) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5.5f * scale
            paint.color = 0x2600DFF2.toInt()
            CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)
        }

        // Border: #00DFF2, 1-1.5px, alpha ~70% (or 100% when active)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.5f * scale else 1.2f * scale
        paint.color = if (active) 0xFF00DFF2.toInt() else 0xB300DFF2.toInt()
        CyberShapeRenderer.drawCyberChamferRect(canvas, rect, corner, notch, paint)

        // Chevron Arrow (<): 16-18dp, cyan
        val cx = rect.centerX()
        val cy = rect.centerY()
        val arm = 8.5f * scale

        backArrowPath.rewind()
        backArrowPath.moveTo(cx + arm * 0.40f, cy - arm)
        backArrowPath.lineTo(cx - arm * 0.45f, cy)
        backArrowPath.lineTo(cx + arm * 0.40f, cy + arm)

        if (active) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3.5f * scale
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            paint.color = 0x3300DFF2
            canvas.drawPath(backArrowPath, paint)
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * scale
        paint.color = if (active) 0xFF00E9FF.toInt() else 0xFF00DFF2.toInt()
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(backArrowPath, paint)
        restorePaintDefaults(paint)
    }

    fun drawItem(
        canvas: Canvas,
        rect: RectF,
        language: KavvoroLanguage,
        selected: Boolean,
        active: Boolean,
        isRightCol: Boolean,
        cardBmp: Bitmap? = null,
        flagBmp: Bitmap? = null,
        radioBmp: Bitmap? = null,
        typeface: Typeface?,
        paint: Paint,
        dp: Float,
        drawFlagFallback: ((Canvas, RectF, KavvoroLanguage) -> Unit)? = null,
        context: Context? = null
    ) {
        ensureCleanPaint(paint)
        val translateY = if (active) 1f * dp else 0f
        val itemRect = if (active) scratchRect.apply { set(rect.left, rect.top + translateY, rect.right, rect.bottom + translateY) } else rect

        val vw = if (canvas.width > 0) canvas.width.toFloat() else 1024f * dp
        val vh = if (canvas.height > 0) canvas.height.toFloat() else 1536f * dp
        val visualScale = kotlin.math.min(vw / 1024f, vh / 1536f)

        // Chamfered / clipped card geometry: EXACT SAME function for both columns
        val baseCorner = 11f * visualScale
        val cardCorner = if (selected) baseCorner * LanguageSelectorMetrics.SELECTED_CHAMFER_SCALE else baseCorner * LanguageSelectorMetrics.INACTIVE_CHAMFER_SCALE
        val cardNotch = cardCorner * 0.85f
        cardPath.rewind()
        createLanguageCardPath(cardPath, itemRect, cardCorner, cardNotch)

        if (selected) {
            // Selected Română: Fill gradient #0A202C -> #091722
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                itemRect.left, itemRect.top, itemRect.right, itemRect.bottom,
                0xFF0A202C.toInt(), 0xFF091722.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(cardPath, paint)
            paint.shader = null

            // Deterministic Procedural Multi-layer Glow (Canvas Paths)
            // Layer 1: path expand +8px, cyan ~5% (reduced ~10% for subtle balance)
            val expand8 = 8f * visualScale
            val r8 = RectF(itemRect.left - expand8, itemRect.top - expand8, itemRect.right + expand8, itemRect.bottom + expand8)
            cardGlow8Path.rewind()
            createLanguageCardPath(cardGlow8Path, r8, cardCorner + expand8, cardNotch + expand8)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.3f * visualScale
            paint.color = 0x0D00E9FF
            canvas.drawPath(cardGlow8Path, paint)

            // Layer 2: path expand +5px, cyan ~8.5%
            val expand5 = 5f * visualScale
            val r5 = RectF(itemRect.left - expand5, itemRect.top - expand5, itemRect.right + expand5, itemRect.bottom + expand5)
            cardGlow5Path.rewind()
            createLanguageCardPath(cardGlow5Path, r5, cardCorner + expand5, cardNotch + expand5)
            paint.strokeWidth = 1.3f * visualScale
            paint.color = 0x1600E9FF
            canvas.drawPath(cardGlow5Path, paint)

            // Layer 3: path expand +2px, cyan ~15%
            val expand2 = 2f * visualScale
            val r2 = RectF(itemRect.left - expand2, itemRect.top - expand2, itemRect.right + expand2, itemRect.bottom + expand2)
            cardGlow2Path.rewind()
            createLanguageCardPath(cardGlow2Path, r2, cardCorner + expand2, cardNotch + expand2)
            paint.strokeWidth = 1.3f * visualScale
            paint.color = 0x2600E9FF
            canvas.drawPath(cardGlow2Path, paint)

            // Main border: 2px #00E9FF
            paint.strokeWidth = 2.0f * visualScale
            paint.color = 0xFF00E9FF.toInt()
            canvas.drawPath(cardPath, paint)

            // Inner stroke: inset 2px, cyan 18%
            val inset2 = 2f * visualScale
            val rIn2 = RectF(itemRect.left + inset2, itemRect.top + inset2, itemRect.right - inset2, itemRect.bottom - inset2)
            cardInnerPath.rewind()
            createLanguageCardPath(cardInnerPath, rIn2, (cardCorner - inset2).coerceAtLeast(2f), (cardNotch - inset2).coerceAtLeast(2f))
            paint.strokeWidth = 1f * visualScale
            paint.color = 0x2E00E9FF
            canvas.drawPath(cardInnerPath, paint)
        } else {
            // Inactive Card: Distinct atmospheres (cool teal left vs subtle violet right -10..15% magenta)
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                itemRect.left, itemRect.top, itemRect.right, itemRect.bottom,
                if (isRightCol) 0xFF0E0D17.toInt() else 0xFF061A25.toInt(),
                if (isRightCol) 0xFF110B16.toInt() else 0xFF07111A.toInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(cardPath, paint)
            paint.shader = null

            // Border ~70% opacity: #12506A left vs #532845 right
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = if (active) 1.5f * visualScale else 1.1f * visualScale
            paint.color = if (active) {
                if (isRightCol) 0xFF7A4885.toInt() else 0xFF246D91.toInt()
            } else {
                if (isRightCol) 0xB3532845.toInt() else 0xB312506A.toInt()
            }
            canvas.drawPath(cardPath, paint)
        }

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * visualScale
        paint.color = if (selected) 0x66FFFFFF else 0x33FFFFFF
        canvas.drawLine(itemRect.left + cardCorner + cardNotch, itemRect.top + 0.8f * visualScale, itemRect.right - cardCorner - cardNotch, itemRect.top + 0.8f * visualScale, paint)

        // Hero Vector Flag Box (~60x44 design px, centerCrop, clip rounded 4px, 1px dark border)
        val flagH = (itemRect.height() * 0.60f).coerceAtLeast(26f * visualScale)
        val flagW = flagH * (60f / 44f)
        val flagLeft = itemRect.left + itemRect.width() * 0.045f
        val flagTop = itemRect.centerY() - flagH * 0.5f
        val flagRect = RectF(flagLeft, flagTop, flagLeft + flagW, flagTop + flagH)

        if (drawFlagFallback != null) {
            val badgeRadius = 4f * visualScale
            flagClipPath.rewind()
            flagClipPath.addRoundRect(flagRect, badgeRadius, badgeRadius, Path.Direction.CW)
            canvas.save()
            canvas.clipPath(flagClipPath)
            drawFlagFallback(canvas, flagRect, language)
            canvas.restore()
        } else {
            FlagDrawableManager.drawFlag(canvas, flagRect, language, context, paint, visualScale)
        }

        // Radio Indicator
        val radioR = (itemRect.height() * 0.19f).coerceIn(8.5f * visualScale, 12f * visualScale)
        val radioInnerR = radioR * 0.48f
        val radioCx = itemRect.right - itemRect.width() * 0.048f - radioR
        val radioCy = itemRect.centerY()

        if (selected) {
            // Radio outer glow 5px
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f * visualScale
            paint.color = 0x3300E9FF
            canvas.drawCircle(radioCx, radioCy, radioR, paint)

            paint.strokeWidth = 1.8f * visualScale
            paint.color = 0xFF00E9FF.toInt()
            canvas.drawCircle(radioCx, radioCy, radioR, paint)

            paint.style = Paint.Style.FILL
            paint.color = 0xFF00E9FF.toInt()
            canvas.drawCircle(radioCx, radioCy, radioInnerR, paint)

            // Specular center dot (white 2px)
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(radioCx, radioCy, 1.4f * visualScale, paint)
        } else if (active) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f * visualScale
            paint.color = 0xCC00E9FF.toInt()
            canvas.drawCircle(radioCx, radioCy, radioR, paint)
        } else {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.1f * visualScale
            paint.color = if (isRightCol) 0x80532845.toInt() else 0x8012506A.toInt()
            canvas.drawCircle(radioCx, radioCy, radioR, paint)
        }

        // Language Name Typography (+20-25% larger, weight 400 for inactive, weight 500 for selected)
        val textLeft = flagRect.right + (itemRect.width() * 0.055f)
        val maxTextWidth = (radioCx - radioR - 10f * visualScale) - textLeft
        val textSize = LanguageSelectorMetrics.languageNameTextSize(itemRect.height(), dp)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = if (selected) {
            Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
            Typeface.create("sans-serif", Typeface.NORMAL)
        }
        textPaint.textSize = textSize
        textPaint.color = if (selected) 0xFFFFFFFF.toInt() else 0xFFE2EDF8.toInt()
        textPaint.letterSpacing = if (selected) 0.01f else 0f

        val fontMetrics = textPaint.fontMetrics
        val baseline = if (fontMetrics != null) itemRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f else itemRect.centerY()
        val displayName = TextUtils.ellipsize(
            language.nativeName,
            TextPaint(textPaint),
            maxTextWidth.coerceAtLeast(1f),
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(displayName, textLeft, baseline, textPaint)
        textPaint.letterSpacing = 0f
        restorePaintDefaults(paint)
    }

    fun drawCurrentBar(
        canvas: Canvas,
        rect: RectF,
        footerBmp: Bitmap? = null,
        activeLanguageName: String,
        currentPrefix: String,
        typeface: Typeface?,
        paint: Paint,
        dp: Float
    ) {
        ensureCleanPaint(paint)
        val vw = if (canvas.width > 0) canvas.width.toFloat() else 1024f * dp
        val vh = if (canvas.height > 0) canvas.height.toFloat() else 1536f * dp
        val visualScale = kotlin.math.min(vw / 1024f, vh / 1536f)

        // Enclosed Chamfered Cyber Bar: 12-14 design px
        val footerCorner = 13f * visualScale
        val footerNotch = footerCorner * 0.85f
        footerPath.rewind()
        createLanguageCardPath(footerPath, rect, footerCorner, footerNotch)

        // Translucent dark cyberpunk glass fill
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.centerY(), rect.right, rect.centerY(),
            intArrayOf(0xC006121C.toInt(), 0xC00B0E18.toInt(), 0xC0160918.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(footerPath, paint)
        paint.shader = null

        // Outer soft glow
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * visualScale
        paint.color = 0x1800E5FF
        canvas.drawPath(footerPath, paint)

        // Border: 5-stop LinearGradient (cyan -> muted cyan -> navy -> purple -> magenta)
        paint.strokeWidth = 1.3f * visualScale
        paint.shader = LinearGradient(
            rect.left, rect.centerY(), rect.right, rect.centerY(),
            intArrayOf(
                0xFF00E5FF.toInt(), // cyan puternic
                0xCC006482.toInt(), // cyan muted
                0x991F2A3B.toInt(), // navy
                0xCC6E1255.toInt(), // purple
                0xFFFF0091.toInt()  // magenta puternic
            ),
            floatArrayOf(0f, 0.25f, 0.50f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(footerPath, paint)
        paint.shader = null

        // Status beacon dot: ~30px after left edge of footer, slightly larger with clear glow
        val dotR = 4.2f * visualScale
        val dotCx = rect.left + 30f * visualScale
        val dotCy = rect.centerY()

        // Soft glow halo and specular center
        paint.style = Paint.Style.FILL
        paint.color = 0x4400ECFF.toInt()
        canvas.drawCircle(dotCx, dotCy, 8.5f * visualScale, paint)
        paint.color = 0xFF00ECFF.toInt()
        canvas.drawCircle(dotCx, dotCy, dotR, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(dotCx, dotCy, 1.4f * visualScale, paint)

        // Status text: "LIMBA CURENTĂ: ROMÂNĂ"
        val textX = dotCx + dotR + 14f * visualScale
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textPaint.textSize = (13.5f * visualScale).coerceIn(12f, 16.5f)
        textPaint.letterSpacing = 0.08f

        // Prefix: LIMBA CURENTĂ: #9AA8B8
        textPaint.color = 0xE69AA8B8.toInt()
        val fontMetrics = textPaint.fontMetrics
        val textY = if (fontMetrics != null) dotCy - (fontMetrics.ascent + fontMetrics.descent) / 2f else dotCy
        canvas.drawText(currentPrefix, textX, textY, textPaint)

        val prefixWidth = textPaint.measureText(currentPrefix + "  ")
        // Language: ROMÂNĂ: #00ECFF
        textPaint.color = 0xFF00ECFF.toInt()
        textPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        canvas.drawText(activeLanguageName, textX + prefixWidth, textY, textPaint)
        textPaint.letterSpacing = 0f
        restorePaintDefaults(paint)
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
        headerFrameBmp: Bitmap? = null,
        diamondBmp: Bitmap? = null,
        backButtonRect: RectF,
        backButtonBmp: Bitmap? = null,
        itemRects: List<RectF>,
        viewportTop: Float,
        viewportBottom: Float,
        footerRect: RectF,
        footerBmp: Bitmap? = null,
        langCardBitmap: ((Boolean, Boolean) -> Bitmap?)? = null,
        languageFlagBitmap: ((KavvoroLanguage) -> Bitmap?)? = null,
        langRadioBitmap: ((Boolean) -> Bitmap?)? = null,
        typeface: Typeface?,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        drawFlagFallback: ((Canvas, RectF, KavvoroLanguage) -> Unit)? = null,
        deckRect: RectF? = null,
        gridViewportRect: RectF? = null,
        context: Context? = null
    ) {
        val vw = if (canvas.width > 0) canvas.width.toFloat() else (if (contentWidth > 0f) contentWidth else 1024f * dp)
        val vh = if (viewportBottom > viewportTop) (viewportBottom - viewportTop) else (if (canvas.height > 0) canvas.height.toFloat() else 1536f * dp)
        val visualScale = kotlin.math.min(vw / 1024f, vh / 1536f)

        // ── 1. Single Main Cyber Frame (Exact Reference Canvas Bounds) ──
        val frameL = vw * LanguageReferenceCanvas.FRAME_LEFT
        val frameR = vw * LanguageReferenceCanvas.FRAME_RIGHT
        val frameT = vh * LanguageReferenceCanvas.FRAME_TOP
        val frameB = vh * LanguageReferenceCanvas.FRAME_BOTTOM
        val deck = deckRect?.takeUnless { it.isEmpty } ?: RectF(frameL, frameT, frameR, frameB)

        val deckCorner = 18f * visualScale
        val deckNotch = 14f * visualScale
        deckPath.rewind()
        createLanguageCardPath(deckPath, deck, deckCorner, deckNotch)

        // Soft floating underglow under frame
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 18f * visualScale
        paint.shader = LinearGradient(
            deck.left, deck.centerY(), deck.right, deck.centerY(),
            intArrayOf(0x1000E5FF, 0x00000000, 0x10FF0088.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(deckPath, paint)

        // Console fill: permeable dark glass
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xD4050812.toInt()
        canvas.drawPath(deckPath, paint)

        // Subtle cyan/magenta wash INSIDE frame to illuminate glass
        paint.shader = LinearGradient(
            deck.left, deck.centerY(), deck.right, deck.centerY(),
            intArrayOf(0x1800E5FF.toInt(), 0x00000000, 0x18FF007F.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(deckPath, paint)
        paint.shader = null

        // Outer soft glow halo
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f * visualScale
        paint.shader = LinearGradient(
            deck.left, deck.top, deck.right, deck.bottom,
            intArrayOf(0x3300E5FF.toInt(), 0x1A6A1A7A.toInt(), 0x33FF007F.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(deckPath, paint)

        // Frame border: cyan left -> navy top -> magenta right -> navy bottom
        paint.strokeWidth = 1.8f * visualScale
        paint.shader = LinearGradient(
            deck.left, deck.centerY(), deck.right, deck.centerY(),
            intArrayOf(0xFF00E5FF.toInt(), 0xFF183B54.toInt(), 0xFF4A1842.toInt(), 0xFFFF007F.toInt()),
            floatArrayOf(0f, 0.35f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(deckPath, paint)
        paint.shader = null

        // Side notch micro-ticks
        val tickLen = 8.5f * visualScale
        val tickY = deck.top + (deck.bottom - deck.top) * 0.065f
        paint.strokeWidth = 1.3f * visualScale
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawLine(deck.left - 2f * visualScale, tickY, deck.left + tickLen, tickY, paint)
        paint.color = 0xFFFF007F.toInt()
        canvas.drawLine(deck.right - tickLen, tickY, deck.right + 2f * visualScale, tickY, paint)

        val bTickY = deck.bottom - (deck.bottom - deck.top) * 0.065f
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawLine(deck.left - 2f * visualScale, bTickY, deck.left + tickLen, bTickY, paint)
        paint.color = 0xFFFF007F.toInt()
        canvas.drawLine(deck.right - tickLen, bTickY, deck.right + 2f * visualScale, bTickY, paint)

        // ── 1b. Visible Cyber Back Button (<): Aligned with title center, chamfered ──
        drawBackButton(
            canvas = canvas,
            rect = backButtonRect,
            active = activeLanguageIndex == -2,
            bmp = backButtonBmp,
            paint = paint,
            dp = dp
        )

        // ── 1c. Telemetric Protocol Kicker (unified with Collection & Leaderboards) ──
        val kickerY = vh * (LanguageReferenceCanvas.FRAME_TOP + 0.016f)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = (9.5f * visualScale).coerceIn(8.5f, 16f)
        textPaint.letterSpacing = 0.16f
        textPaint.color = 0xFF00E5FF.toInt()
        canvas.drawText("✦ ${t("SYSTEM").uppercase()} // ${t("CHOOSE LANGUAGE").uppercase()} ✦", deck.centerX(), kickerY, textPaint)

        // ── 2. Dominant Title Header: "ALEGE LIMBA" (Oxanium Regular/Light weight 300-350, not bold) ──
        val titleY = vh * LanguageReferenceCanvas.TITLE_CENTER_Y
        val titleStr = t("CHOOSE LANGUAGE").uppercase()
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && typeface != null) {
            Typeface.create(typeface, 350, false)
        } else {
            typeface ?: Typeface.create("sans-serif", Typeface.NORMAL)
        }
        textPaint.letterSpacing = 0.16f

        val targetTitleW = vw * LanguageReferenceCanvas.TITLE_TARGET_WIDTH_RATIO
        var titleSize = 42f * visualScale
        textPaint.textSize = titleSize
        val measuredTitleW = textPaint.measureText(titleStr)
        if (measuredTitleW > 0f) {
            titleSize = (titleSize * (targetTitleW / measuredTitleW)).coerceIn(28f * visualScale, 56f * visualScale)
            textPaint.textSize = titleSize
        }
        textPaint.color = 0xFFFFFFFF.toInt()
        val titleMetrics = textPaint.fontMetrics
        val titleBaseline = if (titleMetrics != null) titleY - (titleMetrics.ascent + titleMetrics.descent) / 2f else titleY
        canvas.drawText(titleStr, deck.centerX(), titleBaseline, textPaint)

        // ── 3. Separator with Central Neon Diamond: Fixed at SEPARATOR_Y = 0.103f ──
        val divY = vh * LanguageReferenceCanvas.SEPARATOR_Y
        val diamondSize = 7.5f * visualScale
        val halfD = diamondSize * 0.5f
        val cx = deck.centerX()

        val sepWidth = deck.width() * 0.80f
        val sepLeft = cx - sepWidth * 0.5f
        val sepRight = cx + sepWidth * 0.5f
        val diamondGap = 7f * visualScale

        // Left cyan line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * visualScale
        paint.shader = LinearGradient(
            sepLeft, divY, cx - halfD - diamondGap, divY,
            0x0000E5FF, 0xFF00E5FF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(sepLeft, divY, cx - halfD - diamondGap, divY, paint)

        // Right magenta line
        paint.shader = LinearGradient(
            cx + halfD + diamondGap, divY, sepRight, divY,
            0xFFFF007F.toInt(), 0x00FF007F,
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(cx + halfD + diamondGap, divY, sepRight, divY, paint)
        paint.shader = null

        // Central diamond
        diamondPath.rewind()
        diamondPath.moveTo(cx, divY - halfD)
        diamondPath.lineTo(cx + halfD, divY)
        diamondPath.lineTo(cx, divY + halfD)
        diamondPath.lineTo(cx - halfD, divY)
        diamondPath.close()

        paint.style = Paint.Style.FILL
        paint.color = 0xFF050812.toInt()
        canvas.drawPath(diamondPath, paint)

        // Diamond subtle glow & outline
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * visualScale
        paint.color = 0x3300E5FF
        canvas.drawPath(diamondPath, paint)
        paint.strokeWidth = 1.3f * visualScale
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawPath(diamondPath, paint)
        restorePaintDefaults(paint)

        // ── 4. Subtitle: Fixed at SUBTITLE_CENTER_Y = 0.123f ──
        val subY = vh * LanguageReferenceCanvas.SUBTITLE_CENTER_Y
        val subtitleText = t("Select interface language")
        textPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textPaint.letterSpacing = 0.035f
        var subSize = 20.5f * visualScale
        textPaint.textSize = subSize
        val measuredSubW = textPaint.measureText(subtitleText)
        val maxSubW = deck.width() * 0.85f
        if (measuredSubW > maxSubW && measuredSubW > 0f) {
            subSize = (subSize * (maxSubW / measuredSubW)).coerceAtLeast(17.5f * visualScale)
            textPaint.textSize = subSize
        }
        textPaint.color = 0xE09AB0C4.toInt() // #9AB0C4 at 88% alpha (crisp slate-cyan contrast)
        val subMetrics = textPaint.fontMetrics
        val subBaseline = if (subMetrics != null) subY - (subMetrics.ascent + subMetrics.descent) / 2f else subY
        canvas.drawText(subtitleText, deck.centerX(), subBaseline, textPaint)
        textPaint.letterSpacing = 0f

        // ── 5. Language Cards Grid ──
        val resolvedFooterRect = footerRect.takeUnless { it.isEmpty } ?: RectF(
            vw * LanguageReferenceCanvas.FOOTER_LEFT,
            vh * LanguageReferenceCanvas.FOOTER_TOP,
            vw * LanguageReferenceCanvas.FOOTER_RIGHT,
            vh * LanguageReferenceCanvas.FOOTER_BOTTOM
        )
        val resolvedGridViewport = gridViewportRect?.takeUnless { it.isEmpty }
        val clipTop = resolvedGridViewport?.top ?: (subY + 14f * visualScale)
        val clipBottom = resolvedGridViewport?.bottom ?: (resolvedFooterRect.top - 6f * dp)
        canvas.save()
        canvas.clipRect(deck.left + 2f * dp, clipTop, deck.right - 2f * dp, clipBottom)
        val displayLanguages = KavvoroLanguage.selectableLanguages
        displayLanguages.forEachIndexed { index, language ->
            val rect = itemRects.getOrNull(index) ?: return@forEachIndexed
            if (rect.bottom >= clipTop && rect.top <= clipBottom) {
                val isSelected = language == selected
                val isRightCol = (index % 2) == 1
                drawItem(
                    canvas = canvas,
                    rect = rect,
                    language = language,
                    selected = isSelected,
                    active = activeLanguageIndex == index,
                    isRightCol = isRightCol,
                    cardBmp = null,
                    flagBmp = null,
                    radioBmp = null,
                    typeface = typeface,
                    paint = paint,
                    dp = dp,
                    drawFlagFallback = drawFlagFallback,
                    context = context
                )
            }
        }
        canvas.restore()

        // ── 6. Enclosed Cyber Footer Bar ──
        val langName = (if (selected == KavvoroLanguage.EN || selected == KavvoroLanguage.SYSTEM) "ENGLISH" else selected.nativeName).uppercase()
        val currentPrefix = t("CURRENT").uppercase() + ":"
        drawCurrentBar(
            canvas = canvas,
            rect = resolvedFooterRect,
            footerBmp = null,
            activeLanguageName = langName,
            currentPrefix = currentPrefix,
            typeface = typeface,
            paint = paint,
            dp = dp
        )
    }
}
