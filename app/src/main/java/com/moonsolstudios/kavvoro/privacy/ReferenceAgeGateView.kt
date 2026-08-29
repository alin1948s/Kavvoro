package com.moonsolstudios.kavvoro.privacy

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.moonsolstudios.kavvoro.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Age gate rendered from the 941 x 1672 reference canvas.
 *
 * The reference is a design coordinate system, not an Android pixel size.
 * The responsive content column is scaled from the usable width and the
 * measured content block is positioned inside the safe area. The unused
 * vertical space of the reference artwork is never allowed to shrink the UI.
 */
class ReferenceAgeGateView(
    context: Context,
    private val onAgeGroupSelected: (AgeGroup) -> Unit
) : View(context) {
    private companion object {
        const val DESIGN_WIDTH = 941f

        const val CARD_LEFT = 96f
        const val CARD_TOP = 456f
        const val CARD_RIGHT = 845f
        const val CARD_BOTTOM = 1157f

        // One horizontal content axis. Every primary element is centered on
        // this container coordinate; no individual lateral offsets are used.
        const val CONTENT_CENTER_X = DESIGN_WIDTH * 0.5f
        const val PICKER_CENTER_X = CONTENT_CENTER_X
        const val PICKER_TEXT_CENTER_X = CONTENT_CENTER_X
        const val PICKER_CENTER_Y = 890f
        const val PICKER_ROW_HEIGHT = 160f
        const val PICKER_LINE_OFFSET = 100f

        const val BUTTON_LEFT = 116f
        const val BUTTON_TOP = 1226f
        const val BUTTON_RIGHT = 825f
        const val BUTTON_BOTTOM = 1323f

        const val MIN_AGE = 1
        const val MAX_AGE = 120

        const val REFERENCE_CARD_WIDTH = CARD_RIGHT - CARD_LEFT
        const val REFERENCE_CARD_HEIGHT = CARD_BOTTOM - CARD_TOP
        const val REFERENCE_LOGO_WIDTH = 524f
        const val REFERENCE_LOGO_HEIGHT = REFERENCE_LOGO_WIDTH * (793f / 1983f)
        const val REFERENCE_BUTTON_WIDTH = 709f
        const val REFERENCE_BUTTON_HEIGHT = 97f

        const val BASE_GAP_LOGO_TO_SETUP = 50f
        const val BASE_GAP_SETUP_TO_CARD = 30f
        const val BASE_GAP_CARD_TO_BUTTON = 54f
        const val BASE_GAP_BUTTON_TO_PRIVACY = 42f
        const val BASE_GAP_PRIVACY_TO_CATEGORY = 28f
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val logoPaint = Paint(
        Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG
    ).apply {
        isFilterBitmap = true
        isDither = true
    }
    private val logoBitmap: Bitmap? by lazy {
        BitmapFactory.decodeResource(
            resources,
            R.drawable.kavvoro_logo_lockup,
            BitmapFactory.Options().apply {
                inScaled = false
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        )
    }
    // Keep a pixel-sized copy for the current viewport. Drawing the original
    // 1983 px asset through a hardware destination rect is device-dependent
    // on small surfaces and can produce soft or aliased lettering. A cached
    // filtered copy makes the final sampling deterministic on every density.
    private var renderedLogoBitmap: Bitmap? = null
    private var renderedLogoWidth = 0
    private var renderedLogoHeight = 0
    private val grainPaint = Paint().apply {
        color = 0x03FFFFFF
        strokeWidth = 1f
    }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.6f
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.0f
        strokeCap = Paint.Cap.ROUND
    }
    private val uiTypeface = ResourcesCompat.getFont(context, R.font.manrope)
        ?: Typeface.create("sans-serif", Typeface.NORMAL)
    private val oxaniumTypeface = ResourcesCompat.getFont(context, R.font.oxanium)
        ?: Typeface.create("sans-serif", Typeface.NORMAL)
    private val oxanium600 = if (Build.VERSION.SDK_INT >= 28) {
        Typeface.create(oxaniumTypeface, 600, false)
    } else {
        Typeface.create(oxaniumTypeface, Typeface.BOLD)
    }
    private val oxanium700 = if (Build.VERSION.SDK_INT >= 28) {
        Typeface.create(oxaniumTypeface, 700, false)
    } else {
        Typeface.create(oxaniumTypeface, Typeface.BOLD)
    }
    private val manropeMediumTypeface = Typeface.create(uiTypeface, Typeface.NORMAL)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = oxanium600
        isFakeBoldText = false
        textAlign = Paint.Align.CENTER
        isSubpixelText = true
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scratch = RectF()

    // Real viewport geometry. The page is laid out on one shared center axis;
    // only the card keeps a local reference transform for its visual details.
    private var contentCenterX = 0f
    private var cardScale = 1f
    private val logoRect = RectF()
    private val cardRect = RectF()
    private val buttonRect = RectF()
    private var playerSetupCenterY = 0f
    private var privacyCenterY = 0f
    private var categoryCenterY = 0f
    private var categoryBaseline = 0f
    private var logoWidthPx = 0f
    private var playerSetupTextSizePx = 0f
    private var ageCheckTextSizePx = 0f
    private var subtitleTextSizePx = 0f
    private var selectedAgeTextSizePx = 0f
    private var neighborAgeTextSizePx = 0f
    private var privacyTextSizePx = 0f
    private var categoryTextSizePx = 0f
    private var buttonTextSizePx = 0f
    private val ageCheckTextSizeDesign = 58f
    private val subtitleTextSizeDesign = 33f
    private val selectedAgeTextSizeDesign = 158f
    private val neighborAgeTextSizeDesign = 32f
    private val playerSetupTextSizeDesign = 28f
    private val buttonTextSizeDesign = 36f
    private val privacyTextSizeDesign = 28f
    private val categoryTextSizeDesign = 27f

    private var compactMode = false
    private var tabletBreakpoint = false
    private var largeTablet = false
    private var categoryLetterSpacing = 0.05f
    private var categorySeparator = "  •  "
    private var safeLeft = 0
    private var safeTop = 0
    private var safeRight = 0
    private var safeBottom = 0

    private val density: Float
        get() = resources.displayMetrics.density.coerceAtLeast(0.1f)

    private fun dp(value: Float): Float = value * density

    private fun sp(value: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        value,
        resources.displayMetrics
    )

    private var selectedAge = 18
    private var ageWasTouched = false
    private var agePickerConfirmed = false
    private var dragStartY = 0f
    private var lastY = 0f
    private var pickerOffset = 0f
    private var moved = false
    private var selectionScale = 1f
    private var buttonPressed = false
    private var buttonScale = 1f
    private var pickerSnapAnimator: ValueAnimator? = null
    private var selectionAnimator: ValueAnimator? = null
    private var buttonAnimator: ValueAnimator? = null
    private var logoAnimator: ValueAnimator? = null
    // The logo must never be absent while the view is settling or a screenshot
    // is taken. The entrance treatment is scale-only; alpha starts at 1.0.
    private var logoAlpha = 1f
    private var logoScale = 1f
    private var categoryAnimator: ValueAnimator? = null
    private var categoryFrom = AgeGroup.ADULT.ordinal
    private var categoryTo = AgeGroup.ADULT.ordinal
    private var categoryProgress = 1f

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "Player age"
        // Keep this view on the device's accelerated canvas. The screen uses
        // several gradients and a transparent logo; forcing software
        // rendering makes tall 1080x2400 devices miss the input deadline.
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val safe = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            safeLeft = safe.left
            safeTop = safe.top
            safeRight = safe.right
            safeBottom = safe.bottom
            updateFrame()
            invalidate()
            insets
        }
        ViewCompat.requestApplyInsets(this)
        post { animateLogoIn() }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateFrame()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        if (cardRect.isEmpty) return
        drawResponsiveFrame(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(Color.rgb(3, 8, 20))
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.shader = RadialGradient(
            width * 0.28f,
            height * 0.50f,
            max(width, height) * 0.48f,
            intArrayOf(0x0D12DCE9, 0x00030814),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        backgroundPaint.shader = RadialGradient(
            width * 0.72f,
            height * 0.50f,
            max(width, height) * 0.48f,
            intArrayOf(0x0BE638A8, 0x00030814),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        backgroundPaint.shader = RadialGradient(
            width * 0.50f,
            height * 0.17f,
            max(width, height) * 0.34f,
            intArrayOf(0x080E65C5, 0x00030814),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        // A very soft vignette gives the OLED background depth without adding
        // visible texture, particles, or a tech pattern.
        backgroundPaint.shader = RadialGradient(
            width * 0.50f,
            height * 0.50f,
            max(width, height) * 0.72f,
            intArrayOf(0x00000000, 0x10000000),
            floatArrayOf(0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        backgroundPaint.shader = null
        drawGrain(canvas)
    }

    private fun drawGrain(canvas: Canvas) {
        if (width <= 0 || height <= 0) return
        var seed = 0x5EED1234
        repeat(180) {
            seed = seed * 1103515245 + 12345
            val x = ((seed ushr 1) % width).toFloat()
            seed = seed * 1103515245 + 12345
            val y = ((seed ushr 1) % height).toFloat()
            canvas.drawPoint(x, y, grainPaint)
        }
    }

    private fun drawReferenceFrame(canvas: Canvas) {
        drawResponsiveFrame(canvas)
    }

    private fun drawResponsiveFrame(canvas: Canvas) {
        drawLogo(canvas)
        drawPlayerSetup(canvas)
        drawAgeCard(canvas)
        drawContinueButton(canvas)
        textPaint.typeface = manropeMediumTypeface
        textPaint.textSize = privacyTextSizePx
        textPaint.letterSpacing = 0f
        textPaint.textScaleX = 1f
        val privacyLabel = "Only your age group is stored locally."
        val privacyMaxWidth =
            (width - safeLeft - safeRight - dp(16f)).coerceAtLeast(1f)
        val privacyScaleX = min(
            1f,
            privacyMaxWidth / textPaint.measureText(privacyLabel).coerceAtLeast(1f)
        )
        drawTextCentered(
            canvas,
            privacyLabel,
            contentCenterX,
            privacyCenterY,
            privacyTextSizePx,
            0xC9B8B9C9.toInt(),
            scaleX = privacyScaleX,
            typeface = manropeMediumTypeface,
            fakeBold = false
        )
        drawCategory(canvas)
    }

    private fun drawPlayerSetup(canvas: Canvas) {
        val centerY = playerSetupCenterY
        drawTextCentered(
            canvas,
            "PLAYER SETUP",
            contentCenterX,
            centerY,
            playerSetupTextSizePx,
            0xFF9EA8FF.toInt(),
            letterSpacing = 0.16f,
            typeface = oxanium600
        )

        textPaint.typeface = oxanium600
        textPaint.isFakeBoldText = false
        textPaint.textSize = playerSetupTextSizePx
        textPaint.letterSpacing = 0.16f
        textPaint.textScaleX = 1f
        val halfLabelWidth = textPaint.measureText("PLAYER SETUP") * 0.5f
        val lineLength = 56f * cardScale
        val lineGap = 24f * cardScale
        val leftEnd = contentCenterX - halfLabelWidth - lineGap
        val rightStart = contentCenterX + halfLabelWidth + lineGap

        accentPaint.strokeWidth = max(1f, 1.2f * cardScale)
        accentPaint.color = 0x4C1DE8E0
        canvas.drawLine(leftEnd - lineLength, centerY, leftEnd, centerY, accentPaint)
        canvas.drawCircle(leftEnd, centerY, 2.2f * cardScale, accentPaint)
        accentPaint.color = 0x4CF05CB6
        canvas.drawLine(rightStart, centerY, rightStart + lineLength, centerY, accentPaint)
        canvas.drawCircle(rightStart, centerY, 2.2f * cardScale, accentPaint)
    }

    private fun drawLogo(canvas: Canvas) {
        val bitmap = logoBitmap ?: return
        val renderWidth = logoRect.width().roundToInt().coerceAtLeast(1)
        val renderHeight = logoRect.height().roundToInt().coerceAtLeast(1)
        val renderBitmap = renderedLogoBitmap(bitmap, renderWidth, renderHeight)
        scratch.set(logoRect)

        canvas.save()
        canvas.scale(logoScale, logoScale, logoRect.centerX(), logoRect.centerY())
        logoPaint.alpha = (logoAlpha * 255f).roundToInt().coerceIn(0, 255)
        canvas.drawBitmap(renderBitmap, null, scratch, logoPaint)
        canvas.restore()
        logoPaint.alpha = 255
    }

    private fun renderedLogoBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        if (
            renderedLogoBitmap == null ||
            renderedLogoWidth != width ||
            renderedLogoHeight != height
        ) {
            renderedLogoBitmap = Bitmap.createScaledBitmap(source, width, height, true)
            renderedLogoWidth = width
            renderedLogoHeight = height
        }
        return renderedLogoBitmap!!
    }

    private fun animateLogoIn() {
        logoAnimator?.cancel()
        // Keep the artwork visible from the first frame. This avoids a race
        // between the entrance animation and Android screenshot/layout timing.
        logoAlpha = 1f
        logoScale = 0.985f
        invalidate()
        logoAnimator = ValueAnimator.ofFloat(0.985f, 1f).apply {
            duration = 400L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                logoScale = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun drawAgeCard(canvas: Canvas) {
        canvas.save()
        canvas.translate(
            cardRect.left - CARD_LEFT * cardScale,
            cardRect.top - CARD_TOP * cardScale
        )
        canvas.scale(cardScale, cardScale)
        scratch.set(CARD_LEFT, CARD_TOP, CARD_RIGHT, CARD_BOTTOM)
        cardPaint.style = Paint.Style.FILL
        cardPaint.shader = LinearGradient(
            CARD_LEFT,
            CARD_TOP,
            CARD_RIGHT,
            CARD_BOTTOM,
            intArrayOf(0xF90A1321.toInt(), 0xF7070D19.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, 28f, 28f, cardPaint)
        cardPaint.shader = null

        cardPaint.shader = RadialGradient(
            PICKER_CENTER_X - 96f,
            PICKER_CENTER_Y,
            320f,
            intArrayOf(0x1D00DDE8, 0x0A007E88, 0x00070B14),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, 28f, 28f, cardPaint)
        cardPaint.shader = RadialGradient(
            PICKER_CENTER_X + 96f,
            PICKER_CENTER_Y,
            320f,
            intArrayOf(0x190FBCD0, 0x0A8A2A93, 0x00070B14),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, 28f, 28f, cardPaint)
        cardPaint.shader = null

        borderPaint.shader = LinearGradient(
            CARD_LEFT,
            0f,
            CARD_RIGHT,
            0f,
            intArrayOf(0x8A1DE8E0.toInt(), 0x55465A78.toInt(), 0x7DF05CB6.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        // The design uses a 1dp border. Since the card is drawn in its local
        // reference coordinate system, convert that physical width back into
        // local units before applying the card transform.
        borderPaint.strokeWidth = dp(1f) / cardScale.coerceAtLeast(0.01f)
        canvas.drawRoundRect(scratch, 28f, 28f, borderPaint)
        borderPaint.shader = null

        cardPaint.shader = LinearGradient(
            CARD_LEFT + 24f,
            CARD_TOP,
            CARD_RIGHT - 24f,
            CARD_TOP,
            intArrayOf(0x0000D9D5, 0x321DE8C8, 0x0000D9D5),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            CARD_LEFT + 24f,
            CARD_TOP + 1.5f,
            CARD_RIGHT - 24f,
            CARD_TOP + 3.5f,
            1.5f,
            1.5f,
            cardPaint
        )
        cardPaint.shader = null

        drawTextCentered(
            canvas,
            "AGE CHECK",
            PICKER_CENTER_X,
            545f,
            ageCheckTextSizeDesign,
            0xFFF5F5FF.toInt(),
            letterSpacing = 0.04f,
            typeface = oxanium600
        )
        drawTextCentered(
            canvas,
            "Enter your age",
            PICKER_CENTER_X,
            613.5f,
            subtitleTextSizeDesign,
            0xD9B8B9C9.toInt(),
            typeface = manropeMediumTypeface,
            fakeBold = false
        )

        drawPicker(canvas)
        canvas.restore()
    }

    private fun drawPicker(canvas: Canvas) {
        val selectedY = PICKER_CENTER_Y + pickerOffset
        val glowRadius = 240f
        cardPaint.shader = RadialGradient(
            PICKER_CENTER_X - 92f,
            selectedY,
            glowRadius,
            intArrayOf(0x1800DDE8, 0x09007E88, 0x00070B14),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.save()
        canvas.scale(1.34f, 0.78f, PICKER_CENTER_X, selectedY)
        canvas.drawCircle(PICKER_CENTER_X, selectedY, glowRadius, cardPaint)
        canvas.restore()
        cardPaint.shader = null
        cardPaint.shader = RadialGradient(
            PICKER_CENTER_X + 92f,
            selectedY,
            glowRadius,
            intArrayOf(0x150FBCD0, 0x098A2A93, 0x00070B14),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.save()
        canvas.scale(1.34f, 0.78f, PICKER_CENTER_X, selectedY)
        canvas.drawCircle(PICKER_CENTER_X, selectedY, glowRadius, cardPaint)
        canvas.restore()
        cardPaint.shader = null

        drawPickerLine(canvas, PICKER_CENTER_Y - PICKER_LINE_OFFSET)
        drawPickerLine(canvas, PICKER_CENTER_Y + PICKER_LINE_OFFSET)

        canvas.save()
        canvas.clipRect(CARD_LEFT + 42f, 650f, CARD_RIGHT - 42f, 1125f)
        for (index in -2..2) {
            val age = selectedAge + index
            if (age !in MIN_AGE..MAX_AGE) continue
            val y = selectedY + index * PICKER_ROW_HEIGHT
            val distance = abs(y - PICKER_CENTER_Y) / PICKER_ROW_HEIGHT
            val emphasis = (1f - distance).coerceIn(0f, 1f)
            val alpha = if (distance <= 1f) {
                0.30f + 0.70f * emphasis
            } else {
                0.08f * (1f - (distance - 1f).coerceIn(0f, 1f))
            }
            val size = neighborAgeTextSizeDesign +
                (selectedAgeTextSizeDesign - neighborAgeTextSizeDesign) * emphasis
            val scale = if (index == 0) selectionScale else 0.95f
            drawTextCentered(
                canvas,
                age.toString(),
                PICKER_TEXT_CENTER_X,
                y,
                size * scale,
                (alpha * 255f).roundToInt().coerceIn(0, 255) shl 24 or 0x00F7F4FF,
                scaleX = 1.0f,
                typeface = oxanium600
            )
        }

        cardPaint.shader = LinearGradient(
            0f,
            650f,
            0f,
            1125f,
            intArrayOf(0x36070B14, 0x00070B14, 0x00070B14, 0x36070B14),
            floatArrayOf(0f, 0.22f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(CARD_LEFT + 42f, 650f, CARD_RIGHT - 42f, 1125f, cardPaint)
        cardPaint.shader = null
        canvas.restore()
    }

    private fun drawPickerLine(canvas: Canvas, y: Float) {
        cardPaint.style = Paint.Style.FILL
        cardPaint.shader = LinearGradient(
            PICKER_CENTER_X - 228f,
            y,
            PICKER_CENTER_X + 228f,
            y,
            intArrayOf(
                0x001DE8D5,
                0x701DE8D5,
                0x6D7F86FF,
                0x70F05CB6,
                0x00F05CB6
            ),
            floatArrayOf(0f, 0.22f, 0.5f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            PICKER_CENTER_X - 228f,
            y - 0.8f,
            PICKER_CENTER_X + 228f,
            y + 0.8f,
            0.8f,
            0.8f,
            cardPaint
        )
        cardPaint.shader = null
    }

    private fun drawContinueButton(canvas: Canvas) {
        canvas.save()
        val centerX = buttonRect.centerX()
        val centerY = buttonRect.centerY()
        canvas.scale(buttonScale, buttonScale, centerX, centerY)
        scratch.set(buttonRect)
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.setShadowLayer(
            if (buttonPressed) 8f * cardScale else 12f * cardScale,
            0f,
            if (buttonPressed) 3f * cardScale else 6f * cardScale,
            if (buttonPressed) 0x1200E8D4 else 0x1A00E8D4
        )
        buttonPaint.shader = LinearGradient(
            buttonRect.left,
            buttonRect.top,
            buttonRect.right,
            buttonRect.bottom,
            intArrayOf(0xFF21DED0.toInt(), 0xFF33C9E4.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        val radius = buttonRect.height() * 0.22f
        canvas.drawRoundRect(scratch, radius, radius, buttonPaint)
        buttonPaint.shader = null
        buttonPaint.clearShadowLayer()
        cardPaint.shader = LinearGradient(
            buttonRect.left + buttonRect.width() * 0.08f,
            buttonRect.top,
            buttonRect.right - buttonRect.width() * 0.08f,
            buttonRect.top,
            intArrayOf(0x00070B14, 0x6BFFFFFF, 0x00070B14),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(
            buttonRect.left + buttonRect.width() * 0.08f,
            buttonRect.top + 1f * cardScale,
            buttonRect.right - buttonRect.width() * 0.08f,
            buttonRect.top + 2.5f * cardScale,
            1.5f * cardScale,
            1.5f * cardScale,
            cardPaint
        )
        cardPaint.shader = null
        drawTextCentered(
            canvas,
            "CONTINUE",
            centerX,
            centerY,
            buttonTextSizePx,
            0xFF07090F.toInt(),
            letterSpacing = 0.07f,
            typeface = oxanium700
        )
        canvas.restore()
    }

    private fun drawCategory(canvas: Canvas) {
        ensureCategoryAnimation(ageGroup().ordinal)
        textPaint.typeface = oxanium600
        textPaint.isFakeBoldText = false
        val labels = listOf("CHILD", "TEEN", "ADULT")
        val separator = categorySeparator
        val full = labels.joinToString(separator)
        textPaint.textSize = categoryTextSizePx
        textPaint.letterSpacing = categoryLetterSpacing
        textPaint.textScaleX = 1f
        val categoryAvailableWidth = (width - safeLeft - safeRight - dp(28f))
            .coerceAtLeast(1f)
        val measuredCategoryWidth = textPaint.measureText(full)
        if (measuredCategoryWidth > categoryAvailableWidth) {
            val fit = (categoryAvailableWidth / measuredCategoryWidth).coerceIn(0.86f, 1f)
            categoryTextSizePx *= fit
            categoryLetterSpacing *= fit
            textPaint.textSize = categoryTextSizePx
            textPaint.letterSpacing = categoryLetterSpacing
        }
        val totalWidth = textPaint.measureText(full)
        var x = contentCenterX - totalWidth * 0.5f
        val categoryMetrics = textPaint.fontMetrics
        categoryBaseline = categoryCenterY -
            (categoryMetrics.ascent + categoryMetrics.descent) * 0.5f
        val inactiveColor = 0xFF8A8C9D.toInt()
        val selectedColor = 0xFF27DDD1.toInt()
        labels.forEachIndexed { index, label ->
            textPaint.color = when {
                index == categoryTo -> withAlpha(
                    blendColor(inactiveColor, selectedColor, categoryProgress),
                    255
                )
                index == categoryFrom -> withAlpha(
                    blendColor(selectedColor, inactiveColor, categoryProgress),
                    255
                )
                else -> withAlpha(inactiveColor, 214)
            }
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, x, categoryBaseline, textPaint)
            x += textPaint.measureText(label)
            if (index < labels.lastIndex) {
                textPaint.color = withAlpha(inactiveColor, 214)
                canvas.drawText(separator, x, categoryBaseline, textPaint)
                x += textPaint.measureText(separator)
            }
        }
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun ensureCategoryAnimation(target: Int) {
        if (target == categoryTo) return
        categoryAnimator?.cancel()
        categoryFrom = categoryTo
        categoryTo = target
        categoryAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 170L
            addUpdateListener {
                categoryProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun blendColor(from: Int, to: Int, progress: Float): Int {
        val t = progress.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(from) + (Color.red(to) - Color.red(from)) * t).roundToInt(),
            (Color.green(from) + (Color.green(to) - Color.green(from)) * t).roundToInt(),
            (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t).roundToInt()
        )
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private fun drawTextCentered(
        canvas: Canvas,
        value: String,
        x: Float,
        centerY: Float,
        size: Float,
        color: Int,
        letterSpacing: Float = 0f,
        scaleX: Float = 1f,
        shader: Shader? = null,
        typeface: Typeface = oxanium600,
        fakeBold: Boolean = false
    ) {
        textPaint.typeface = typeface
        textPaint.isFakeBoldText = fakeBold
        textPaint.shader = shader
        // A hardware canvas cannot apply a reliable per-glyph software blur.
        // The picker already communicates depth through opacity and scale,
        // which is both cheaper and more consistent across devices.
        textPaint.maskFilter = null
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = size
        textPaint.color = color
        textPaint.letterSpacing = letterSpacing
        textPaint.textScaleX = scaleX
        val metrics = textPaint.fontMetrics
        val baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f
        canvas.drawText(value, x, baseline, textPaint)
        textPaint.shader = null
        textPaint.maskFilter = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (insidePicker(event.x, event.y)) {
                    // A new drag must take ownership of the picker immediately.
                    // Otherwise a previous snap animator can keep writing
                    // pickerOffset for a few frames and make the wheel feel
                    // blurred or delayed under the finger.
                    pickerSnapAnimator?.cancel()
                    pickerSnapAnimator = null
                    selectionAnimator?.cancel()
                    selectionAnimator = null
                    selectionScale = 1f
                    dragStartY = event.y
                    lastY = event.y
                    pickerOffset = 0f
                    moved = false
                    ageWasTouched = true
                    agePickerConfirmed = true
                    performClick()
                    return true
                }
                ageWasTouched = false
                moved = false
                buttonPressed = insideButton(event.x, event.y)
                if (buttonPressed) {
                    animateButtonScale(0.985f)
                    invalidate()
                }
                return buttonPressed
            }

            MotionEvent.ACTION_MOVE -> {
                if (!ageWasTouched) return true
                val delta = (event.y - lastY) / cardScale.coerceAtLeast(0.01f)
                lastY = event.y
                if (abs(event.y - dragStartY) > 8f) moved = true
                pickerOffset += delta
                while (pickerOffset <= -PICKER_ROW_HEIGHT && selectedAge < MAX_AGE) {
                    selectedAge += 1
                    pickerOffset += PICKER_ROW_HEIGHT
                    tick()
                    animateSelection()
                }
                while (pickerOffset >= PICKER_ROW_HEIGHT && selectedAge > MIN_AGE) {
                    selectedAge -= 1
                    pickerOffset -= PICKER_ROW_HEIGHT
                    tick()
                    animateSelection()
                }
                pickerOffset = pickerOffset.coerceIn(-PICKER_ROW_HEIGHT, PICKER_ROW_HEIGHT)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val pressedPicker = ageWasTouched
                val pressedButton = buttonPressed && insideButton(event.x, event.y)
                if (pressedPicker) {
                    snapPicker()
                    performClick()
                }
                if (pressedButton && !moved) submit()
                buttonPressed = false
                animateButtonScale(1f)
                invalidate()
                ageWasTouched = false
                return pressedPicker || pressedButton
            }

            MotionEvent.ACTION_CANCEL -> {
                if (ageWasTouched) snapPicker()
                buttonPressed = false
                animateButtonScale(1f)
                invalidate()
                ageWasTouched = false
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun insidePicker(x: Float, y: Float): Boolean {
        return x in cardRect.left..cardRect.right &&
            y in (cardRect.top + cardRect.height() * 0.28f)..
            (cardRect.top + cardRect.height() * 0.95f)
    }

    private fun insideButton(x: Float, y: Float): Boolean {
        val extraX = 12f * cardScale
        val extraY = 12f * cardScale
        return x >= buttonRect.left - extraX &&
            x <= buttonRect.right + extraX &&
            y >= buttonRect.top - extraY &&
            y <= buttonRect.bottom + extraY
    }

    private fun snapPicker() {
        val target = when {
            pickerOffset <= -PICKER_ROW_HEIGHT * 0.5f && selectedAge < MAX_AGE -> -PICKER_ROW_HEIGHT
            pickerOffset >= PICKER_ROW_HEIGHT * 0.5f && selectedAge > MIN_AGE -> PICKER_ROW_HEIGHT
            else -> 0f
        }
        if (target == 0f && pickerOffset == 0f) return
        pickerSnapAnimator?.cancel()
        var wasCancelled = false
        val animator = ValueAnimator.ofFloat(pickerOffset, target).apply {
            duration = 105L
            interpolator = DecelerateInterpolator(1.35f)
            addUpdateListener {
                pickerOffset = it.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    wasCancelled = true
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (!wasCancelled) {
                        if (target < 0f && selectedAge < MAX_AGE) selectedAge += 1
                        if (target > 0f && selectedAge > MIN_AGE) selectedAge -= 1
                        pickerOffset = 0f
                        animateSelection()
                        invalidate()
                    }
                    if (pickerSnapAnimator === animation) pickerSnapAnimator = null
                }
            })
        }
        pickerSnapAnimator = animator
        animator.start()
    }

    private fun animateSelection() {
        selectionAnimator?.cancel()
        selectionAnimator = ValueAnimator.ofFloat(0.94f, 1f).apply {
            duration = 85L
            interpolator = DecelerateInterpolator(1.2f)
            addUpdateListener {
                selectionScale = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun animateButtonScale(target: Float) {
        buttonAnimator?.cancel()
        buttonAnimator = ValueAnimator.ofFloat(buttonScale, target).apply {
            duration = 140L
            addUpdateListener {
                buttonScale = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun tick() {
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    private fun submit() {
        if (!agePickerConfirmed) {
            // The picker opens on a valid default value (18). A user should be
            // able to continue without touching it first.
            agePickerConfirmed = true
        }
        AgeProfileStore.save(context, ageGroup())
        onAgeGroupSelected(ageGroup())
    }

    private fun ageGroup(): AgeGroup = when {
        selectedAge < 13 -> AgeGroup.CHILD
        selectedAge < 18 -> AgeGroup.TEEN
        else -> AgeGroup.ADULT
    }

    private fun updateFrame() {
        if (width <= 0 || height <= 0) return

        val contentWidth = (width - safeLeft - safeRight).coerceAtLeast(1).toFloat()
        val contentHeight = (height - safeTop - safeBottom).coerceAtLeast(1).toFloat()
        val aspectRatio = contentHeight / contentWidth
        contentCenterX = safeLeft + contentWidth * 0.5f

        val referenceCoreHeight = REFERENCE_LOGO_HEIGHT +
            BASE_GAP_LOGO_TO_SETUP + playerSetupTextSizeDesign +
            BASE_GAP_SETUP_TO_CARD + REFERENCE_CARD_HEIGHT +
            BASE_GAP_CARD_TO_BUTTON + REFERENCE_BUTTON_HEIGHT +
            BASE_GAP_BUTTON_TO_PRIVACY + privacyTextSizeDesign +
            BASE_GAP_PRIVACY_TO_CATEGORY + categoryTextSizeDesign

        val targetCardWidthRatio = when {
            aspectRatio >= 2.0f -> 0.80f
            aspectRatio >= 1.70f -> 0.78f
            aspectRatio >= 1.50f -> 0.70f
            else -> 0.60f
        }

        val targetBlockHeightRatio = when {
            aspectRatio >= 2.0f -> 0.70f
            aspectRatio >= 1.70f -> 0.80f
            else -> 0.78f
        }

        val scaleFromWidth = (contentWidth * targetCardWidthRatio) / REFERENCE_CARD_WIDTH
        val scaleFromHeight = (contentHeight * targetBlockHeightRatio) / referenceCoreHeight
        val visualScale = min(scaleFromWidth, scaleFromHeight)
        cardScale = visualScale

        val coreHeight = referenceCoreHeight * cardScale
        val availableExtraHeight = max(0f, contentHeight - coreHeight)

        val topExtra = availableExtraHeight * 0.28f
        val gapLogoSetupExtra = availableExtraHeight * 0.10f
        val gapSetupCardExtra = availableExtraHeight * 0.08f
        val gapCardButtonExtra = availableExtraHeight * 0.14f
        val gapButtonPrivacyExtra = availableExtraHeight * 0.12f
        val gapPrivacyCatExtra = availableExtraHeight * 0.08f

        playerSetupTextSizePx = playerSetupTextSizeDesign * cardScale
        ageCheckTextSizePx = ageCheckTextSizeDesign * cardScale
        subtitleTextSizePx = subtitleTextSizeDesign * cardScale
        selectedAgeTextSizePx = selectedAgeTextSizeDesign * cardScale
        neighborAgeTextSizePx = neighborAgeTextSizeDesign * cardScale
        privacyTextSizePx = privacyTextSizeDesign * cardScale
        categoryTextSizePx = categoryTextSizeDesign * cardScale
        buttonTextSizePx = buttonTextSizeDesign * cardScale

        categoryLetterSpacing = if (contentWidth / density < 380f) 0.03f else 0.05f
        categorySeparator = if (contentWidth / density < 380f) " • " else "  •  "

        textPaint.typeface = manropeMediumTypeface
        textPaint.textSize = privacyTextSizePx
        textPaint.letterSpacing = 0f
        val privacyWidth = textPaint.measureText("Only your age group is stored locally.")
        val maxTextWidth = contentWidth - dp(24f)
        if (privacyWidth > maxTextWidth && privacyWidth > 0f) {
            privacyTextSizePx *= (maxTextWidth / privacyWidth)
        }

        textPaint.typeface = oxanium600
        textPaint.textSize = categoryTextSizePx
        textPaint.letterSpacing = categoryLetterSpacing
        val catWidth = textPaint.measureText("CHILD  •  TEEN  •  ADULT")
        if (catWidth > maxTextWidth && catWidth > 0f) {
            categoryTextSizePx *= (maxTextWidth / catWidth)
        }

        val logoHeight = REFERENCE_LOGO_HEIGHT * cardScale
        val logoWidth = REFERENCE_LOGO_WIDTH * cardScale
        val logoTop = safeTop + topExtra
        logoWidthPx = logoWidth
        logoRect.set(
            contentCenterX - logoWidth * 0.5f,
            logoTop,
            contentCenterX + logoWidth * 0.5f,
            logoTop + logoHeight
        )

        val gapLogoToSetup = BASE_GAP_LOGO_TO_SETUP * cardScale + gapLogoSetupExtra
        playerSetupCenterY = logoTop + logoHeight + gapLogoToSetup + playerSetupTextSizePx * 0.5f

        val gapSetupToCard = BASE_GAP_SETUP_TO_CARD * cardScale + gapSetupCardExtra
        val cardTop = playerSetupCenterY + playerSetupTextSizePx * 0.5f + gapSetupToCard
        val cardWidth = REFERENCE_CARD_WIDTH * cardScale
        val cardHeight = REFERENCE_CARD_HEIGHT * cardScale
        cardRect.set(
            contentCenterX - cardWidth * 0.5f,
            cardTop,
            contentCenterX + cardWidth * 0.5f,
            cardTop + cardHeight
        )

        val gapCardToButton = BASE_GAP_CARD_TO_BUTTON * cardScale + gapCardButtonExtra
        val buttonTop = cardRect.bottom + gapCardToButton
        val buttonWidth = REFERENCE_BUTTON_WIDTH * cardScale
        val buttonHeight = REFERENCE_BUTTON_HEIGHT * cardScale
        buttonRect.set(
            contentCenterX - buttonWidth * 0.5f,
            buttonTop,
            contentCenterX + buttonWidth * 0.5f,
            buttonTop + buttonHeight
        )

        val gapButtonToPrivacy = BASE_GAP_BUTTON_TO_PRIVACY * cardScale + gapButtonPrivacyExtra
        privacyCenterY = buttonRect.bottom + gapButtonToPrivacy + privacyTextSizePx * 0.5f

        val gapPrivacyToCategory = BASE_GAP_PRIVACY_TO_CATEGORY * cardScale + gapPrivacyCatExtra
        categoryCenterY = privacyCenterY + privacyTextSizePx * 0.5f + gapPrivacyToCategory + categoryTextSizePx * 0.5f

        textPaint.typeface = oxanium600
        textPaint.textSize = categoryTextSizePx
        textPaint.letterSpacing = categoryLetterSpacing
        val categoryMetrics = textPaint.fontMetrics
        categoryBaseline = categoryCenterY -
            (categoryMetrics.ascent + categoryMetrics.descent) * 0.5f
    }
}
