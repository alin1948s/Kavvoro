package com.moonsolstudios.kavvoro.ui.screens.agecheck

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import kotlin.math.abs
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
            gravity = Gravity.CENTER_HORIZONTAL
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
            setTextSize(if (compact) 26f else 29f)
            letterSpacing = 0.06f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            maxLines = 1
        }
        val subtitle = TextView(context).apply {
            text = t("Enter your age in years.")
            setTextColor(KavvoroPalette.mutedText)
            setTextSize(16f)
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

    private fun continueButton() = TextView(context).apply {
        text = t("CONTINUE").uppercase(KavvoroI18n.active(context).let { locale ->
            java.util.Locale.forLanguageTag(locale.code.replace('_', '-'))
        })
        setTextColor(Color.rgb(6, 22, 43))
        setTextSize(20f)
        letterSpacing = 0.08f
        gravity = Gravity.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        isClickable = true
        isFocusable = true
        contentDescription = text
        background = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(KavvoroPalette.cyan, KavvoroPalette.blue)
        ).apply { cornerRadius = dp(16f).toFloat() }
        elevation = dp(5f).toFloat()
        setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onConfirm(ageGroupForAge(selectedAge))
        }
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

private class AgePickerView(context: Context, initialAge: Int) : View(context) {
    private val density = resources.displayMetrics.density
    private val selectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(244, 246, 255)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    }
    private val adjacentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8995B9.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val railPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = dp(1f) }
    private var downY = 0f
    private var age = initialAge.coerceIn(AgeCheckScreenView.MIN_AGE, AgeCheckScreenView.MAX_AGE)

    var onAgeChanged: ((Int) -> Unit)? = null

    init {
        isFocusable = true
        isClickable = true
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        contentDescription = accessibilityLabel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radiusX = width * 0.44f
        val radiusY = dp(60f)
        glowPaint.shader = RadialGradient(
            centerX,
            centerY,
            maxOf(radiusX, radiusY),
            intArrayOf(0x4431E8FF, 0x1A7B43FF, Color.TRANSPARENT),
            floatArrayOf(0f, 0.58f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawOval(centerX - radiusX, centerY - radiusY, centerX + radiusX,
            centerY + radiusY, glowPaint)
        glowPaint.shader = null

        val gap = minOf(dp(70f), height * 0.29f)
        val selectedSize = minOf(dp(76f), height * 0.38f)
        val adjacentSize = minOf(dp(28f), height * 0.16f)
        selectedTextPaint.textSize = selectedSize
        adjacentTextPaint.textSize = adjacentSize
        val rail = LinearGradient(
            dp(12f).toFloat(), centerY, width - dp(12f).toFloat(), centerY,
            intArrayOf(0x0031E8FF, 0xB431E8FF.toInt(), 0xB4D93DFF.toInt(), 0x00D93DFF),
            null,
            Shader.TileMode.CLAMP
        )
        railPaint.shader = rail
        canvas.drawLine(dp(14f).toFloat(), centerY - gap * 0.55f,
            width - dp(14f).toFloat(), centerY - gap * 0.55f, railPaint)
        canvas.drawLine(dp(14f).toFloat(), centerY + gap * 0.55f,
            width - dp(14f).toFloat(), centerY + gap * 0.55f, railPaint)
        railPaint.shader = null

        if (age > AgeCheckScreenView.MIN_AGE) {
            canvas.drawText((age - 1).toString(), centerX,
                centerY - gap + adjacentSize * 0.34f, adjacentTextPaint)
        }
        canvas.drawText(age.toString(), centerX, centerY + selectedSize * 0.34f, selectedTextPaint)
        if (age < AgeCheckScreenView.MAX_AGE) {
            canvas.drawText((age + 1).toString(), centerX,
                centerY + gap + adjacentSize * 0.34f, adjacentTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val delta = downY - event.y
                if (abs(delta) > dp(12f)) {
                    val steps = (abs(delta) / dp(48f)).roundToInt().coerceAtLeast(1)
                    adjustAge(if (delta > 0f) steps else -steps)
                } else if (event.y < height * 0.42f) {
                    adjustAge(-1)
                } else if (event.y > height * 0.58f) {
                    adjustAge(1)
                }
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> return true
        }
        return true
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
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> adjustAge(1)
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> adjustAge(-1)
            else -> return super.performAccessibilityAction(action, arguments)
        }
        return true
    }

    private fun adjustAge(delta: Int) = setAge(age + delta)

    private fun setAge(value: Int) {
        val updated = value.coerceIn(AgeCheckScreenView.MIN_AGE, AgeCheckScreenView.MAX_AGE)
        if (updated == age) return
        age = updated
        contentDescription = accessibilityLabel()
        invalidate()
        onAgeChanged?.invoke(age)
        sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SELECTED)
    }

    private fun accessibilityLabel(): String = "$age. ${KavvoroI18n.t(context, "Enter your age in years.")}"

    private fun dp(value: Float): Float = value * density
}
