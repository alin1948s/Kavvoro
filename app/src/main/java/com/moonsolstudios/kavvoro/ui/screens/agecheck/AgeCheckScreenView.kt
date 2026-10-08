package com.moonsolstudios.kavvoro.ui.screens.agecheck

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.VelocityTracker
import android.view.animation.DecelerateInterpolator
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.privacy.AgeGroup
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.render.UiTypography
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/** Startup age selection screen. The exact age remains in memory and is never persisted. */
class AgeCheckScreenView @JvmOverloads constructor(
    context: Context,
    initialAge: Int = AGE_OF_ADULTHOOD,
    private val onConfirm: (AgeGroup) -> Unit,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var selectedAge: Int = initialAge.coerceIn(MIN_AGE, MAX_AGE)
        private set

    private var categoryLabel: TextView? = null
    private var picker: AgePickerView? = null

    init {
        setBackgroundColor(KavvoroPalette.background)
        setWillNotDraw(false)
        isFocusableInTouchMode = true
        buildLayout()
    }

    /** Rebuilds the responsive composition when the host Activity handles rotation in place. */
    fun onHostConfigurationChanged() {
        removeAllViews()
        categoryLabel = null
        picker = null
        buildLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x4D8EBBFF }
        // Stable, restrained star points keep the onboarding screen in the same cosmic world as Home.
        for (index in 0 until 34) {
            val seed = index * 73 + 19
            val x = ((seed * 37) % 997) / 997f * width
            val y = ((seed * 61) % 991) / 991f * height
            paint.alpha = 26 + (seed % 70)
            val radius = if (index % 7 == 0) dp(1.5f) else dp(0.8f)
            canvas.drawCircle(x, y, radius.toFloat(), paint)
        }
    }

    private fun buildLayout() {
        val landscape = resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE &&
            resources.configuration.smallestScreenWidthDp >= 600
        val body = if (landscape) buildLandscapeLayout() else buildPortraitLayout()
        val maxWidth = if (landscape) dp(1160f) else dp(620f)
        addView(
            body,
            LayoutParams(minOf(maxWidth, resources.displayMetrics.widthPixels),
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER)
        )
    }

    private fun buildPortraitLayout(): View {
        val heightDp = resources.configuration.screenHeightDp.takeIf { it > 0 }
            ?: (resources.displayMetrics.heightPixels / resources.displayMetrics.density).roundToInt()
        val compact = heightDp < 840
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(26f), dp(if (compact) 14f else 22f), dp(26f), dp(if (compact) 14f else 22f))
        }
        content.addView(Space(context), LinearLayout.LayoutParams(1, 0, 1f))
        content.addView(
            logoImage(),
            LinearLayout.LayoutParams(dp(if (compact) 250f else 286f), dp(if (compact) 116f else 132f))
        )
        content.addView(setupLabel(), linearParams(height = dp(30f)).apply { topMargin = dp(8f) })
        content.addView(makeAgeCard(compact), linearParams(height = dp(if (compact) 276f else 320f)).apply {
            topMargin = dp(if (compact) 16f else 24f)
        })
        content.addView(continueButton(), linearParams(height = dp(58f)).apply {
            topMargin = dp(if (compact) 20f else 26f)
        })
        content.addView(privacyNote(), linearParams(height = dp(30f)).apply {
            topMargin = dp(if (compact) 14f else 20f)
        })
        content.addView(categoryLine(), linearParams(height = dp(28f)).apply {
            topMargin = dp(6f)
        })
        content.addView(Space(context), LinearLayout.LayoutParams(1, 0, 1f))
        return content
    }

    private fun buildLandscapeLayout(): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(36f), dp(26f), dp(36f), dp(26f))
        }
        val brandColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12f), 0, dp(24f), 0)
        }
        brandColumn.addView(
            logoImage(),
            LinearLayout.LayoutParams(dp(380f), dp(180f))
        )
        brandColumn.addView(setupLabel(), linearParams(height = dp(34f)).apply { topMargin = dp(20f) })
        root.addView(brandColumn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.9f))

        val formColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        formColumn.addView(makeAgeCard(compact = true), linearParams(height = dp(288f)))
        formColumn.addView(continueButton(), linearParams(height = dp(58f)).apply { topMargin = dp(18f) })
        formColumn.addView(privacyNote(), linearParams(height = dp(28f)).apply { topMargin = dp(14f) })
        formColumn.addView(categoryLine(), linearParams(height = dp(26f)).apply { topMargin = dp(6f) })
        root.addView(formColumn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.1f))
        return root
    }

    private fun logoImage() = ImageView(context).apply {
        setImageResource(R.drawable.kavvoro_logo)
        scaleType = ImageView.ScaleType.FIT_CENTER
        adjustViewBounds = true
        contentDescription = context.getString(R.string.app_name)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private fun setupLabel() = TextView(context).apply {
        text = t("PLAYER SETUP")
        setTextColor(KavvoroPalette.mutedText)
        setTextSize(16f)
        letterSpacing = 0.18f
        gravity = Gravity.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        maxLines = 1
        contentDescription = text
    }

    private fun makeAgeCard(compact: Boolean): View {
        val border = FrameLayout(context).apply {
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(KavvoroPalette.cyan, KavvoroPalette.blue, KavvoroPalette.magenta)
            ).apply { cornerRadius = dp(22f).toFloat() }
            elevation = dp(8f).toFloat()
        }
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18f), dp(if (compact) 14f else 20f), dp(18f), dp(12f))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(8, 15, 40))
                cornerRadius = dp(21f).toFloat()
            }
        }
        val title = TextView(context).apply {
            text = t("AGE CHECK")
            setTextColor(Color.rgb(244, 246, 255))
            setTextSize(UiTypography.screenTitleDp(compact))
            letterSpacing = 0.06f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val subtitle = TextView(context).apply {
            text = t("Enter your age in years.")
            setTextColor(KavvoroPalette.mutedText)
            setTextSize(UiTypography.screenSubtitleDp(compact))
            gravity = Gravity.CENTER
            maxLines = 2
        }
        val agePicker = AgePickerView(context, selectedAge).apply {
            onAgeChanged = { age ->
                selectedAge = age
                updateCategoryLine()
            }
        }
        picker = agePicker
        panel.addView(title, linearParams(height = dp(40f)))
        panel.addView(subtitle, linearParams(height = dp(34f)).apply { topMargin = dp(2f) })
        panel.addView(agePicker, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { topMargin = dp(4f) })
        border.addView(panel, LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { setMargins(dp(2f), dp(2f), dp(2f), dp(2f)) })
        return border
    }

    private fun continueButton(): View {
        val locale = java.util.Locale.forLanguageTag(KavvoroI18n.active(context).code.replace('_', '-'))
        val button = AgeContinueButton(context, t("CONTINUE").uppercase(locale))
        button.setOnClickListener {
            button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onConfirm(ageGroupForAge(selectedAge))
        }
        return button
    }

    private fun privacyNote() = TextView(context).apply {
        text = t("Only the age group is saved locally.")
        setTextColor(KavvoroPalette.mutedText)
        setTextSize(14f)
        gravity = Gravity.CENTER
        maxLines = 2
        contentDescription = text
    }

    private fun categoryLine() = TextView(context).apply {
        setTextSize(13f)
        letterSpacing = 0.05f
        gravity = Gravity.CENTER
        maxLines = 1
        textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        categoryLabel = this
        updateCategoryLine()
    }

    private fun updateCategoryLine() {
        val label = categoryLabel ?: return
        val source = t("CHILD  /  TEEN  /  ADULT")
        val categories = source.split('•').map(String::trim)
        val selected = when (ageGroupForAge(selectedAge)) {
            AgeGroup.CHILD -> 0
            AgeGroup.TEEN -> 1
            AgeGroup.ADULT -> 2
        }
        val styled = SpannableString(source)
        var searchFrom = 0
        categories.forEachIndexed { index, category ->
            val start = source.indexOf(category, searchFrom)
            if (start >= 0) {
                val end = start + category.length
                val color = if (index == selected) KavvoroPalette.cyan else 0xFF8995B9.toInt()
                styled.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (index == selected) {
                    styled.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                searchFrom = end
            }
        }
        label.text = styled
        label.contentDescription = "$source. ${t("AGE")}: $selectedAge"
    }

    private fun linearParams(height: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, height
    )

    private fun t(key: String): String = KavvoroI18n.t(context, key)

    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).roundToInt()

    companion object {
        const val MIN_AGE = 1
        const val MAX_AGE = 120
        const val AGE_OF_ADULTHOOD = 18
    }
}

internal fun ageGroupForAge(age: Int): AgeGroup = when {
    age < 13 -> AgeGroup.CHILD
    age < AgeCheckScreenView.AGE_OF_ADULTHOOD -> AgeGroup.TEEN
    else -> AgeGroup.ADULT
}

/** A clean beveled call to action that stays legible across translated labels. */
private class AgeContinueButton(context: Context, private val label: String) : View(context) {
    private val density = resources.displayMetrics.density
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(241, 249, 255)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    private val shellPath = Path()
    private val facePath = Path()

    init {
        isClickable = true
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = label
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val inset = dp(1f)
        val left = inset
        val top = inset
        val right = width - inset
        val bottom = height - inset
        val cut = dp(13f).coerceAtMost(height * 0.24f)
        createBeveledPath(shellPath, left, top, right, bottom, cut)

        val scale = if (isPressed) 0.985f else 1f
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.save()
        canvas.scale(scale, scale, centerX, centerY)

        facePaint.shader = null
        facePaint.color = 0xFF020A1C.toInt()
        canvas.save()
        canvas.translate(0f, dp(3f))
        canvas.drawPath(shellPath, facePaint)
        canvas.restore()

        edgePaint.strokeWidth = dp(1.5f)
        edgePaint.shader = LinearGradient(
            left, top, right, bottom,
            intArrayOf(KavvoroPalette.cyan, KavvoroPalette.blue, KavvoroPalette.magenta),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(shellPath, edgePaint)
        edgePaint.shader = null

        createBeveledPath(facePath, left + dp(2.5f), top + dp(2.5f),
            right - dp(2.5f), bottom - dp(2.5f), cut - dp(1.5f))
        facePaint.shader = LinearGradient(
            left, top, right, bottom,
            if (isPressed) intArrayOf(0xFF145A78.toInt(), 0xFF172957.toInt(), 0xFF301C57.toInt())
            else intArrayOf(0xFF123B5B.toInt(), 0xFF101D3C.toInt(), 0xFF21183F.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(facePath, facePaint)
        facePaint.shader = null

        // One restrained glint and one energy seam keep the layered plate visually connected.
        accentPaint.strokeWidth = dp(1.2f)
        accentPaint.shader = LinearGradient(left + dp(20f), top, right - dp(20f), top,
            intArrayOf(0x0031E8FF, 0xCC31E8FF.toInt(), 0xAAD93DFF.toInt(), 0x00D93DFF),
            null, Shader.TileMode.CLAMP)
        canvas.drawLine(left + cut + dp(9f), top + dp(1.5f), right - cut - dp(9f), top + dp(1.5f), accentPaint)
        accentPaint.strokeWidth = dp(2.2f)
        canvas.drawLine(left + dp(21f), bottom - dp(1.5f), right - dp(21f), bottom - dp(1.5f), accentPaint)
        accentPaint.shader = null

        val textMaxWidth = (width - dp(48f)).coerceAtLeast(dp(80f))
        var textSize = dp(19f)
        textPaint.textSize = textSize
        while (textPaint.measureText(label) > textMaxWidth && textSize > dp(13f)) {
            textSize -= dp(0.5f)
            textPaint.textSize = textSize
        }
        textPaint.alpha = if (isPressed) 255 else 244
        canvas.drawText(label, centerX, centerY - (textPaint.ascent() + textPaint.descent()) / 2f, textPaint)
        canvas.restore()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = android.widget.Button::class.java.name
        info.contentDescription = label
    }

    private fun createBeveledPath(path: Path, left: Float, top: Float, right: Float, bottom: Float, cut: Float) {
        path.reset()
        path.moveTo(left + cut, top)
        path.lineTo(right - cut, top)
        path.lineTo(right, top + cut)
        path.lineTo(right, bottom - cut)
        path.lineTo(right - cut, bottom)
        path.lineTo(left + cut, bottom)
        path.lineTo(left, bottom - cut)
        path.lineTo(left, top + cut)
        path.close()
    }

    private fun dp(value: Float): Float = value * density
}

private class AgePickerView(context: Context, initialAge: Int) : View(context) {
    private val density = resources.displayMetrics.density
    private val selectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(244, 246, 255)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        setShadowLayer(dp(4f), 0f, 0f, 0x5531E8FF.toInt())
    }
    private val adjacentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8995B9.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val railPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = dp(1f) }
    private var railShader: Shader? = null
    private var numberGlowShader: Shader? = null
    private var itemExtentPx = dp(70f)
    private var selectedNumberSizePx = dp(60f)
    private var scrollOffsetPx = 0f
    private var lastY = 0f
    private var dragDistancePx = 0f
    private var velocityTracker: VelocityTracker? = null
    private var scrollAnimator: ValueAnimator? = null
    private var age = initialAge.coerceIn(AgeCheckScreenView.MIN_AGE, AgeCheckScreenView.MAX_AGE)

    var onAgeChanged: ((Int) -> Unit)? = null

    init {
        isFocusable = true
        isClickable = true
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        contentDescription = accessibilityLabel()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        itemExtentPx = min(dp(70f), height * 0.29f).coerceAtLeast(dp(52f))
        selectedNumberSizePx = minOf(dp(60f), height * 0.34f)
        numberGlowShader = RadialGradient(
            width / 2f, height / 2f, selectedNumberSizePx * 1.3f,
            intArrayOf(0x2631E8FF, 0x1431E8FF, Color.TRANSPARENT),
            floatArrayOf(0f, 0.46f, 1f), Shader.TileMode.CLAMP
        )
        railShader = LinearGradient(
            dp(12f), height / 2f, width - dp(12f), height / 2f,
            intArrayOf(0x0031E8FF, 0xB431E8FF.toInt(), 0xB4D93DFF.toInt(), 0x00D93DFF),
            null, Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val gap = itemExtentPx
        val selectedSize = selectedNumberSizePx
        glowPaint.shader = numberGlowShader
        canvas.drawCircle(centerX, centerY, selectedSize * 1.3f, glowPaint)
        glowPaint.shader = null

        // Keep the selector rails outside the focused numeral while respecting the wheel pitch.
        val railOffset = maxOf(gap * 0.62f, selectedSize * 0.55f).coerceAtMost(gap * 0.72f)
        railPaint.shader = railShader
        canvas.drawLine(dp(14f), centerY - railOffset,
            width - dp(14f), centerY - railOffset, railPaint)
        canvas.drawLine(dp(14f), centerY + railOffset,
            width - dp(14f), centerY + railOffset, railPaint)
        railPaint.shader = null

        for (step in -2..2) {
            val candidateAge = age + step
            if (candidateAge !in AgeCheckScreenView.MIN_AGE..AgeCheckScreenView.MAX_AGE) continue
            val baselineY = centerY + step * gap - scrollOffsetPx
            val distance = abs(baselineY - centerY) / gap
            if (distance > 2.1f) continue
            val scale = 1f - 0.54f * distance.coerceAtMost(1f)
            val textSize = selectedSize * scale
            val focused = distance < 0.48f
            val paint = if (focused) selectedTextPaint else adjacentTextPaint
            paint.textSize = textSize
            paint.alpha = (255 * (1f - 0.47f * distance.coerceAtMost(1.7f))).toInt().coerceIn(35, 255)
            val tilt = ((baselineY - centerY) / gap * 7f).coerceIn(-12f, 12f)
            canvas.save()
            canvas.rotate(tilt, centerX, baselineY)
            canvas.drawText(candidateAge.toString(), centerX, baselineY + textSize * 0.34f, paint)
            canvas.restore()
        }
        selectedTextPaint.alpha = 255
        adjacentTextPaint.alpha = 255
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scrollAnimator?.cancel()
                scrollAnimator = null
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                lastY = event.y
                dragDistancePx = 0f
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                val delta = lastY - event.y
                lastY = event.y
                dragDistancePx += abs(delta)
                if (delta != 0f) applyScrollDelta(delta, withHaptics = true)
                return true
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000, dp(4200f))
                if (dragDistancePx < dp(10f)) {
                    if (event.y < height * 0.42f) animateAgeSteps(-1)
                    else if (event.y > height * 0.58f) animateAgeSteps(1)
                } else {
                    settleScroll(velocityTracker?.yVelocity ?: 0f)
                }
                recycleVelocityTracker()
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                settleScroll(0f)
                recycleVelocityTracker()
                return true
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        scrollAnimator?.cancel()
        scrollAnimator = null
        recycleVelocityTracker()
        super.onDetachedFromWindow()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = android.widget.NumberPicker::class.java.name
        info.contentDescription = accessibilityLabel()
        info.isScrollable = true
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)
    }

    override fun performAccessibilityAction(action: Int, arguments: android.os.Bundle?): Boolean {
        when (action) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> animateAgeSteps(1)
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> animateAgeSteps(-1)
            else -> return super.performAccessibilityAction(action, arguments)
        }
        return true
    }

    private fun animateAgeSteps(steps: Int) {
        val targetSteps = (scrollOffsetPx / itemExtentPx).roundToInt() + steps
        animateToSteps(targetSteps.coerceIn(-12, 12))
    }

    private fun settleScroll(fingerVelocityY: Float) {
        val projected = scrollOffsetPx - fingerVelocityY * 0.14f
        val targetSteps = (projected / itemExtentPx).roundToInt().coerceIn(-12, 12)
        animateToSteps(targetSteps)
    }

    private fun animateToSteps(targetSteps: Int) {
        scrollAnimator?.cancel()
        val targetOffset = targetSteps * itemExtentPx
        val travel = targetOffset - scrollOffsetPx
        if (abs(travel) < 1f) {
            scrollOffsetPx = 0f
            invalidate()
            return
        }
        var previousValue = 0f
        val animator = ValueAnimator.ofFloat(0f, travel).apply {
            duration = (190L + abs(targetSteps) * 24L).coerceIn(190L, 480L)
            interpolator = DecelerateInterpolator(1.45f)
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                applyScrollDelta(value - previousValue, withHaptics = true)
                previousValue = value
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    scrollOffsetPx = 0f
                    scrollAnimator = null
                    invalidate()
                }
            })
        }
        scrollAnimator = animator
        animator.start()
    }

    private fun applyScrollDelta(delta: Float, withHaptics: Boolean) {
        scrollOffsetPx += delta.coerceIn(-itemExtentPx * 12f, itemExtentPx * 12f)
        var crossedSteps = 0
        while (scrollOffsetPx >= itemExtentPx && crossedSteps < 12) {
            if (age >= AgeCheckScreenView.MAX_AGE) {
                scrollOffsetPx = itemExtentPx * 0.16f
                break
            }
            scrollOffsetPx -= itemExtentPx
            updateAge(age + 1, withHaptics)
            crossedSteps++
        }
        while (scrollOffsetPx <= -itemExtentPx && crossedSteps > -12) {
            if (age <= AgeCheckScreenView.MIN_AGE) {
                scrollOffsetPx = -itemExtentPx * 0.16f
                break
            }
            scrollOffsetPx += itemExtentPx
            updateAge(age - 1, withHaptics)
            crossedSteps--
        }
        postInvalidateOnAnimation()
    }

    private fun updateAge(value: Int, withHaptics: Boolean) {
        val updated = value.coerceIn(AgeCheckScreenView.MIN_AGE, AgeCheckScreenView.MAX_AGE)
        if (updated == age) return
        age = updated
        if (withHaptics) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        contentDescription = accessibilityLabel()
        onAgeChanged?.invoke(age)
        sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SELECTED)
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun accessibilityLabel(): String = "$age. ${KavvoroI18n.t(context, "Enter your age in years.")}"

    private fun dp(value: Float): Float = value * density
}
