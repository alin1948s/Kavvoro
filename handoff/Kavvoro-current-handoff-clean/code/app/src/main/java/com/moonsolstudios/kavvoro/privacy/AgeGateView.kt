package com.moonsolstudios.kavvoro.privacy

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n

class AgeGateView(
    context: Context,
    private val onAgeGroupSelected: (AgeGroup) -> Unit
) : LinearLayout(context) {
    private val ageInput = ageInput()
    private val errorText = TextView(context)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(24), dp(22), dp(24), dp(22))
        setBackgroundColor(Color.rgb(7, 9, 15))

        addView(brandView())
        addView(Space(context), LayoutParams(1, dp(18)))
        addView(label(t("PLAYER SETUP"), 11f, Color.rgb(138, 166, 255)))
        addView(label(t("AGE CHECK"), 26f, Color.rgb(247, 244, 255)).apply {
            setPadding(0, dp(8), 0, 0)
        })
        addView(label(t("Enter your age in years."), 13f, Color.argb(190, 255, 255, 255)).apply {
            setPadding(0, dp(8), 0, dp(18))
        })

        addView(ageInput, LayoutParams(LayoutParams.MATCH_PARENT, dp(62)))

        errorText.apply {
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(255, 87, 87))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }
        addView(errorText, LayoutParams(LayoutParams.MATCH_PARENT, dp(34)))

        val continueButton = Button(context).apply {
            text = t("CONTINUE")
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(7, 9, 15))
            background = roundedBackground(Color.rgb(29, 232, 200), Color.TRANSPARENT, 6f)
            stateListAnimator = null
            setOnClickListener { submit() }
        }
        addView(continueButton, LayoutParams(LayoutParams.MATCH_PARENT, dp(56)))

        addView(label(t("Only the age group is saved locally."), 11f, Color.argb(155, 255, 255, 255)).apply {
            setPadding(0, dp(18), 0, 0)
        })
        addView(label(t("CHILD  /  TEEN  /  ADULT"), 10f, Color.rgb(255, 207, 74)).apply {
            setPadding(0, dp(7), 0, 0)
        })
        addView(Space(context), LayoutParams(1, 0, 1f))

        ageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        post { ageInput.requestFocus() }
    }

    private fun submit() {
        val age = ageInput.text.toString().toIntOrNull()
        if (age == null) {
            showError(t("ENTER YOUR AGE"))
            return
        }
        if (age !in 0..120) {
            showError(t("CHECK THE AGE"))
            return
        }

        errorText.text = ""
        val group = when {
            age < 13 -> AgeGroup.CHILD
            age < 18 -> AgeGroup.TEEN
            else -> AgeGroup.ADULT
        }
        AgeProfileStore.save(context, group)
        onAgeGroupSelected(group)
    }

    private fun showError(message: String) {
        errorText.text = message
        val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.HapticFeedbackConstants.REJECT
        } else {
            android.view.HapticFeedbackConstants.LONG_PRESS
        }
        performHapticFeedback(feedback)
    }

    private fun brandView(): TextView {
        val brand = SpannableString("BRAINROT\nCHAOS\nKAVVORO")
        brand.setSpan(
            ForegroundColorSpan(Color.rgb(255, 77, 141)),
            "BRAINROT\n".length,
            brand.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return TextView(context).apply {
            text = brand
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.START
            setLineSpacing(0f, 0.9f)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
    }

    private fun label(value: String, size: Float, color: Int): TextView {
        return TextView(context).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
    }

    private fun ageInput(): EditText {
        return EditText(context).apply {
            hint = t("AGE")
            contentDescription = t("Player age")
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(3))
            gravity = Gravity.CENTER
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(247, 244, 255))
            setHintTextColor(Color.argb(115, 255, 255, 255))
            setPadding(dp(8), 0, dp(8), 0)
            background = roundedBackground(Color.rgb(22, 29, 41), Color.rgb(69, 79, 101), 6f)
            imeOptions = EditorInfo.IME_ACTION_DONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            }
        }
    }

    private fun roundedBackground(fill: Int, stroke: Int, radiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp.toInt()).toFloat()
            setColor(fill)
            if (stroke != Color.TRANSPARENT) setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun t(value: String): String = KavvoroI18n.t(context, value)
}
