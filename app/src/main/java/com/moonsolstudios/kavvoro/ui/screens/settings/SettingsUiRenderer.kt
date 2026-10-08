package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.moonsolstudios.kavvoro.model.SettingsButton
import com.moonsolstudios.kavvoro.model.SettingsTab
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.BrandTitleRenderer
import com.moonsolstudios.kavvoro.ui.render.CyberShapeRenderer
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural renderer for Settings screen components (sliders, toggles, backdrop, section headers).
 */
object SettingsUiRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val iconScratchRect = RectF()
    private val controlScratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val layoutPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val subLayoutPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private data class StaticLayoutKey(
        val text: String,
        val width: Int,
        val textSizeBits: Int,
        val color: Int,
        val isSubtitle: Boolean,
        val hasCustomFont: Boolean
    )

    private val staticLayoutCache = object : LinkedHashMap<StaticLayoutKey, StaticLayout>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<StaticLayoutKey, StaticLayout>?): Boolean =
            size > 64
    }

    private fun rowIconSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
        SettingsLayoutCalculator.rowIconSize(viewWidth, dp, viewHeight)

    private fun rowShowsSubtitle(row: RectF, subtitle: String, dp: Float): Boolean =
        subtitle.isNotBlank() && row.height() >= 64f * dp

    fun drawBackdrop(
        canvas: Canvas,
        width: Float,
        height: Float,
        centerX: Float,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
        paint: Paint,
        dp: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = KavvoroPalette.background
        canvas.drawRect(0f, 0f, width, height, paint)

        paint.shader = RadialGradient(
            centerX - 115f * dp, height * 0.46f, 360f * dp,
            intArrayOf(
                withAlpha(KavvoroPalette.cyan, 22),
                withAlpha(KavvoroPalette.blue, 6),
                withAlpha(KavvoroPalette.background, 0)
            ), null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)

        paint.shader = RadialGradient(
            centerX + 155f * dp, height * 0.52f, 330f * dp,
            intArrayOf(
                withAlpha(KavvoroPalette.magenta, 16),
                withAlpha(KavvoroPalette.purple, 5),
                withAlpha(KavvoroPalette.background, 0)
            ), null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f * dp
        paint.color = 0x080C7A94
        val grid = 54f * dp
        var x = left - grid
        while (x < right + grid) {
            canvas.drawLine(x, top, x, bottom, paint)
            x += grid
        }
        var y = top
        while (y < bottom) {
            canvas.drawLine(left, y, right, y, paint)
            y += grid
        }
    }

    fun drawDivider(canvas: Canvas, left: Float, right: Float, y: Float, centerX: Float, paint: Paint, dp: Float) {
        val notch = 12f * dp

        // 1. Left line with fade from left
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(left, y, centerX - notch, y, withAlpha(KavvoroPalette.cyan, 0), withAlpha(KavvoroPalette.cyan, 221), Shader.TileMode.CLAMP)
        canvas.drawLine(left, y, centerX - notch, y, paint)

        // 2. Right line with fade to right
        paint.shader = LinearGradient(centerX + notch, y, right, y, withAlpha(KavvoroPalette.pink, 221), withAlpha(KavvoroPalette.pink, 0), Shader.TileMode.CLAMP)
        canvas.drawLine(centerX + notch, y, right, y, paint)
        paint.shader = null

        // 3. Center Diamond Node ◆
        tempPath.reset()
        tempPath.moveTo(centerX, y - 4f * dp)
        tempPath.lineTo(centerX + 5f * dp, y)
        tempPath.lineTo(centerX, y + 4f * dp)
        tempPath.lineTo(centerX - 5f * dp, y)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.color = KavvoroPalette.cyan
        canvas.drawPath(tempPath, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = KavvoroPalette.pink
        canvas.drawPath(tempPath, paint)

        // Subtle side bracket ticks
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(KavvoroPalette.cyan, 170)
        canvas.drawLine(centerX - notch, y - 2.5f * dp, centerX - notch, y + 2.5f * dp, paint)
        paint.color = withAlpha(KavvoroPalette.pink, 170)
        canvas.drawLine(centerX + notch, y - 2.5f * dp, centerX + notch, y + 2.5f * dp, paint)
    }

    fun drawSlider(canvas: Canvas, rect: RectF, value: Int, accent: Int, active: Boolean, paint: Paint, dp: Float) {
        val cy = rect.centerY()
        val fillWidth = rect.width() * (value / 100f)

        // 1. Recessed cyber groove channel
        val grooveHeight = 5.5f * dp
        scratchRect.set(rect.left, cy - grooveHeight * 0.5f, rect.right, cy + grooveHeight * 0.5f)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0x60060E18.toInt()
        canvas.drawRoundRect(scratchRect, grooveHeight * 0.5f, grooveHeight * 0.5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(KavvoroPalette.cyan, 64)
        canvas.drawRoundRect(scratchRect, grooveHeight * 0.5f, grooveHeight * 0.5f, paint)

        // 2. Segmented Guide Pips along the groove (every 25%)
        paint.style = Paint.Style.FILL
        paint.color = 0x30FFFFFF.toInt()
        for (step in 1..3) {
            val stepX = rect.left + rect.width() * (step * 0.25f)
            canvas.drawCircle(stepX, cy, 1.2f * dp, paint)
        }

        // 3. Active filled luminous track with energy beam
        if (fillWidth > 0f) {
            controlScratchRect.set(rect.left, cy - grooveHeight * 0.5f, rect.left + fillWidth, cy + grooveHeight * 0.5f)
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                rect.left, cy, rect.left + fillWidth, cy,
                intArrayOf(KavvoroPalette.cyan, accent),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(controlScratchRect, grooveHeight * 0.5f, grooveHeight * 0.5f, paint)
            paint.shader = null

            // Subtle energy glow line across active core
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.color = 0xAAFFFFFF.toInt()
            canvas.drawLine(rect.left + 2f * dp, cy, rect.left + fillWidth - 2f * dp, cy, paint)
        }

        // 4. Knurled Cyber Thumb Knob
        val knobX = rect.left + fillWidth
        val outerRadius = (if (active) 11.5f else 9.5f) * dp
        val coreRadius = (if (active) 7.5f else 6.2f) * dp

        // Soft glow halo
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            knobX, cy, outerRadius * 1.5f,
            intArrayOf(withAlpha(accent, if (active) 130 else 70), 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(knobX, cy, outerRadius * 1.5f, paint)
        paint.shader = null

        // Outer Metallic Rim
        paint.style = Paint.Style.FILL
        paint.color = if (active) 0xFFFFFFFF.toInt() else 0xFFE2EFFC.toInt()
        canvas.drawCircle(knobX, cy, coreRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.color = accent
        canvas.drawCircle(knobX, cy, coreRadius, paint)

        // Center glowing diode dot
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawCircle(knobX, cy, 2.5f * dp, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(knobX, cy, 1f * dp, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawToggle(canvas: Canvas, rect: RectF, enabled: Boolean, accent: Int, active: Boolean, paint: Paint, dp: Float) {
        val corner = rect.height() * 0.45f

        // 1. Tactical Switch Housing with Chamfered Style
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            if (enabled) withAlpha(KavvoroPalette.cyan, 85) else 0x550A1420.toInt(),
            if (enabled) 0x30102A44.toInt() else 0x7003060C.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        // 2. Precision Laser Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = if (enabled) withAlpha(accent, if (active) 255 else 200) else withAlpha(KavvoroPalette.cyan, 64)
        canvas.drawRoundRect(rect, corner, corner, paint)

        // 3. Status LED Diode on inactive side
        val ledRadius = 1.8f * dp
        val ledY = rect.centerY()
        if (enabled) {
            // ON side indicator (left)
            val ledX = rect.left + rect.width() * 0.28f
            paint.style = Paint.Style.FILL
            paint.color = KavvoroPalette.cyan
            canvas.drawCircle(ledX, ledY, ledRadius, paint)
            // Tiny glow
            paint.color = withAlpha(KavvoroPalette.cyan, 85)
            canvas.drawCircle(ledX, ledY, ledRadius * 2.2f, paint)
        } else {
            // OFF side indicator (right)
            val ledX = rect.right - rect.width() * 0.28f
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(KavvoroPalette.pink, 80)
            canvas.drawCircle(ledX, ledY, ledRadius, paint)
        }

        // 4. Tactile Mechanical Slider Knob
        val knobRadius = rect.height() * 0.36f
        val knobX = if (enabled) rect.right - rect.height() * 0.48f else rect.left + rect.height() * 0.48f
        val knobY = rect.centerY()

        // Knob glow
        if (enabled) {
            paint.style = Paint.Style.FILL
            paint.color = withAlpha(accent, if (active) 90 else 50)
            canvas.drawCircle(knobX, knobY, knobRadius * 1.5f, paint)
        }

        // Knob body
        paint.style = Paint.Style.FILL
        paint.color = if (enabled) 0xFFFFFFFF.toInt() else 0xFF8A9CAA.toInt()
        canvas.drawCircle(knobX, knobY, knobRadius, paint)

        // Knob border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = if (enabled) accent else 0xFF4B5A68.toInt()
        canvas.drawCircle(knobX, knobY, knobRadius, paint)

        // Knurled grip grooves on knob
        paint.strokeWidth = 0.9f * dp
        paint.color = if (enabled) 0x66000000 else 0x44FFFFFF
        canvas.drawLine(knobX - 1.2f * dp, knobY - knobRadius * 0.5f, knobX - 1.2f * dp, knobY + knobRadius * 0.5f, paint)
        canvas.drawLine(knobX + 1.2f * dp, knobY - knobRadius * 0.5f, knobX + 1.2f * dp, knobY + knobRadius * 0.5f, paint)
    }

    fun drawChevron(canvas: Canvas, cx: Float, cy: Float, color: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        val size = 6.5f * dp
        tempPath.reset()
        tempPath.moveTo(cx - size * 0.4f, cy - size)
        tempPath.lineTo(cx + size * 0.6f, cy)
        tempPath.lineTo(cx - size * 0.4f, cy + size)
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
    }

    fun drawBackHomeButton(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        label: String,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        viewWidth: Float = rect.width(),
        viewHeight: Float = rect.height()
    ) {
        val inset = 0.8f * dp
        val corner = 14f * dp
        val pressOffset = if (active) 0.8f * dp else 0f
        val faceTop = rect.top + inset + pressOffset
        val faceBottom = rect.bottom - inset + pressOffset
        val faceLeft = rect.left + inset
        val faceRight = rect.right - inset
        scratchRect.set(faceLeft, faceTop, faceRight, faceBottom)

        // A quiet dark surface keeps this secondary action within the Settings visual system.
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
            if (active) 0xFF12364A.toInt() else 0xFF102235.toInt(),
            if (active) 0xFF0B2437.toInt() else 0xFF0A1727.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, corner, corner, paint)
        paint.shader = null

        // Subtle interior light, with enough contrast to read as a control rather than a panel.
        scratchRect2.set(
            scratchRect.left + 2f * dp,
            scratchRect.top + 1f * dp,
            scratchRect.right - 2f * dp,
            scratchRect.centerY()
        )
        paint.shader = LinearGradient(
            scratchRect2.left, scratchRect2.top, scratchRect2.left, scratchRect2.bottom,
            if (active) 0x263DEBFF else 0x143DEBFF,
            0x00000000,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect2, corner - 2f * dp, corner - 2f * dp, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.8f * dp else 1.2f * dp
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.right, scratchRect.bottom,
            if (active) 0xFFFF69D8.toInt() else withAlpha(KavvoroPalette.cyan, 220),
            if (active) 0xFF35E8FF.toInt() else withAlpha(KavvoroPalette.pink, 190),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, corner, corner, paint)
        paint.shader = null

        // Compact round back icon; its arrow remains clear at small phone sizes.
        val badgeRadius = minOf(18f * dp, scratchRect.height() * 0.34f)
        val arrowCenterX = scratchRect.centerX()
        val arrowCenterY = scratchRect.centerY()
        val textBaseSize = SettingsLayoutCalculator.backTextSize(viewWidth, dp, viewHeight)
        val gap = 12f * dp
        val horizontalInset = 18f * dp
        val maxTextWidth = (scratchRect.width() - horizontalInset * 2f - badgeRadius * 2f - gap).coerceAtLeast(0f)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.letterSpacing = 0.035f
        textPaint.textSize = textBaseSize
        val measuredLabelWidth = textPaint.measureText(label)
        if (measuredLabelWidth > maxTextWidth && measuredLabelWidth > 0f) {
            textPaint.textSize = (textBaseSize * maxTextWidth / measuredLabelWidth).coerceAtLeast(11f * dp)
        }
        val displayLabel = ellipsizeForPaint(label, textPaint, maxTextWidth)
        val labelWidth = textPaint.measureText(displayLabel).coerceAtMost(maxTextWidth)
        val groupWidth = badgeRadius * 2f + gap + labelWidth
        val groupLeft = scratchRect.centerX() - groupWidth * 0.5f
        val badgeCenterX = groupLeft + badgeRadius

        paint.style = Paint.Style.FILL
        paint.color = if (active) 0xFF0C3A4A.toInt() else 0xFF0A1B2B.toInt()
        canvas.drawCircle(badgeCenterX, arrowCenterY, badgeRadius + 1.5f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.color = if (active) 0xCC65F3FF.toInt() else withAlpha(KavvoroPalette.cyan, 180)
        canvas.drawCircle(badgeCenterX, arrowCenterY, badgeRadius, paint)
        paint.style = Paint.Style.FILL
        paint.color = KavvoroPalette.pink
        canvas.drawCircle(badgeCenterX + badgeRadius * 0.72f, arrowCenterY - badgeRadius * 0.68f, 1.7f * dp, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = if (active) 0xFFFFFFFF.toInt() else 0xFF6AF1FF.toInt()
        val arrowSize = 6f * dp
        tempPath.reset()
        tempPath.moveTo(badgeCenterX + arrowSize * 0.85f, arrowCenterY)
        tempPath.lineTo(badgeCenterX - arrowSize, arrowCenterY)
        tempPath.moveTo(badgeCenterX - arrowSize * 0.25f, arrowCenterY - arrowSize * 0.72f)
        tempPath.lineTo(badgeCenterX - arrowSize, arrowCenterY)
        tempPath.lineTo(badgeCenterX - arrowSize * 0.25f, arrowCenterY + arrowSize * 0.72f)
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.setShadowLayer(3f * dp, 0f, 1f * dp, 0x99000000.toInt())
        val baseline = arrowCenterY - (textPaint.ascent() + textPaint.descent()) * 0.5f
        canvas.drawText(displayLabel, badgeCenterX + badgeRadius + gap, baseline, textPaint)
        textPaint.clearShadowLayer()
        textPaint.letterSpacing = 0f
        paint.shader = null
        paint.style = Paint.Style.FILL
    }

    fun drawTabPill(
        canvas: Canvas,
        rect: RectF,
        label: String,
        iconId: SettingsIconRenderer.SettingsIconId,
        isSelected: Boolean,
        isPressed: Boolean,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        val corner = 6f * dp
        val notch = 4f * dp
        val depth = 2f * dp
        val press = if (isPressed) 1f * dp else 0f
        val accent = if (isSelected) KavvoroPalette.cyan else withAlpha(KavvoroPalette.cyan, 153)

        if (isSelected) {
            // 3D Bottom Lip
            scratchRect.set(rect.left, rect.top + depth, rect.right, rect.bottom)
            CyberShapeRenderer.createChamferPath(tempPath, scratchRect, corner, notch)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF004F60.toInt()
            canvas.drawPath(tempPath, paint)

            // Raised Tab Face
            scratchRect2.set(rect.left, rect.top + press, rect.right, rect.bottom - depth + press)
            CyberShapeRenderer.createChamferPath(tempPath, scratchRect2, corner, notch)
            paint.shader = LinearGradient(
                scratchRect2.left,
                scratchRect2.top,
                scratchRect2.right,
                scratchRect2.bottom,
                intArrayOf(KavvoroPalette.cyan, KavvoroPalette.blue, KavvoroPalette.pink),
                floatArrayOf(0f, 0.54f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawPath(tempPath, paint)
            paint.shader = null

            // Glossy Specular Glare
            scratchRect.set(scratchRect2.left + 1f * dp, scratchRect2.top + 0.6f * dp, scratchRect2.right - 1f * dp, scratchRect2.top + scratchRect2.height() * 0.48f)
            paint.shader = LinearGradient(scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom, 0x80FFFFFF.toInt(), 0x05FFFFFF, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(scratchRect, corner - 1f * dp, corner - 1f * dp, paint)
            paint.shader = null

            // White Border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.6f * dp
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawPath(tempPath, paint)
        } else {
            // Unselected Tab Body
            scratchRect.set(rect.left, rect.top + depth, rect.right, rect.bottom)
            CyberShapeRenderer.createChamferPath(tempPath, scratchRect, corner, notch)
            paint.style = Paint.Style.FILL
            paint.color = 0xFF040A14.toInt()
            canvas.drawPath(tempPath, paint)

            scratchRect2.set(rect.left, rect.top + press, rect.right, rect.bottom - depth + press)
            CyberShapeRenderer.createChamferPath(tempPath, scratchRect2, corner, notch)
            paint.color = if (isPressed) withAlpha(KavvoroPalette.cyan, 40) else 0xCC0C1828.toInt()
            canvas.drawPath(tempPath, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f * dp
            paint.color = withAlpha(KavvoroPalette.cyan, 85)
            canvas.drawPath(tempPath, paint)
        }

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = if (isSelected) 0xDDFFFFFF.toInt() else 0x33FFFFFF
        canvas.drawLine(scratchRect2.left + corner + notch, scratchRect2.top + 0.8f * dp, scratchRect2.right - corner - notch, scratchRect2.top + 0.8f * dp, paint)

        // 3. Selected Tab Glowing Under-Bracket with Diamond Node
        if (isSelected) {
            val cx = rect.centerX()
            val notchW = rect.width() * 0.48f
            paint.strokeWidth = 2.4f * dp
            paint.color = KavvoroPalette.cyan
            canvas.drawLine(cx - notchW * 0.5f, rect.bottom - 1.2f * dp, cx + notchW * 0.5f, rect.bottom - 1.2f * dp, paint)

            // Diamond Center Pip
            paint.style = Paint.Style.FILL
            paint.color = 0xFFFFFFFF.toInt()
            val dSize = 2f * dp
            tempPath.reset()
            tempPath.moveTo(cx, rect.bottom - 1.2f * dp - dSize)
            tempPath.lineTo(cx + dSize, rect.bottom - 1.2f * dp)
            tempPath.lineTo(cx, rect.bottom - 1.2f * dp + dSize)
            tempPath.lineTo(cx - dSize, rect.bottom - 1.2f * dp)
            tempPath.close()
            canvas.drawPath(tempPath, paint)
        }

        // 4. Centered icon and label share the same vertical rhythm in every tab.
        val faceTop = rect.top + press
        val faceBottom = rect.bottom - depth + press
        val contentCenterY = (faceTop + faceBottom) * 0.5f

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        val tabText = SettingsLayoutCalculator.tabTextSize(viewWidth, dp, viewHeight)
        textPaint.textSize = tabText
        val maxLabel = (rect.width() - 16f * dp).coerceAtLeast(8f * dp)
        val labelWidth = textPaint.measureText(label)
        if (labelWidth > maxLabel && labelWidth > 0f) {
            textPaint.textSize = (textPaint.textSize * (maxLabel / labelWidth)).coerceAtLeast(9.5f * dp)
        }
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = if (isSelected) 0xFFFFFFFF.toInt() else 0xB8C8D8E8.toInt()

        val labelHeight = textPaint.descent() - textPaint.ascent()
        val labelGap = 2.5f * dp
        val iconSpace = (faceBottom - faceTop - labelGap - labelHeight).coerceAtLeast(0f)
        val badgeSize = minOf(SettingsLayoutCalculator.tabIconSize(viewWidth, dp, viewHeight), iconSpace)
        val contentHeight = badgeSize + labelGap + labelHeight
        val iconTop = contentCenterY - contentHeight * 0.5f
        iconScratchRect.set(
            rect.centerX() - badgeSize * 0.5f,
            iconTop,
            rect.centerX() + badgeSize * 0.5f,
            iconTop + badgeSize
        )
        SettingsIconRenderer.drawSettingsIcon(canvas, iconScratchRect, iconId, accent, isSelected, paint, dp)

        val labelTop = iconTop + badgeSize + labelGap
        canvas.drawText(label, rect.centerX(), labelTop - textPaint.ascent(), textPaint)
    }

    fun drawSectionLabel(
        canvas: Canvas,
        label: String,
        left: Float,
        right: Float,
        baseline: Float,
        accent: Int,
        paint: Paint,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        val titleSize = SettingsLayoutCalculator.sectionTitleSize(viewWidth, dp, viewHeight)
        val cy = baseline - titleSize * 0.35f
        val pillHeight = titleSize * 2f
        val pillTop = cy - pillHeight * 0.5f
        val pillBottom = cy + pillHeight * 0.5f

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = titleSize
        textPaint.letterSpacing = 0.05f
        val leadingSpace = 24f * dp
        val trailingSpace = 16f * dp
        val maxTextWidth = (right - left - leadingSpace - trailingSpace).coerceAtLeast(1f * dp)
        val originalTextWidth = textPaint.measureText(label)
        val labelSize = if (originalTextWidth > maxTextWidth && originalTextWidth > 0f) {
            (titleSize * maxTextWidth / originalTextWidth).coerceAtLeast(9f * dp)
        } else {
            titleSize
        }
        textPaint.textSize = labelSize
        val textWidth = textPaint.measureText(label)
        val pillWidth = (textWidth + leadingSpace + trailingSpace).coerceAtMost(right - left)
        val pillLeft = left
        val pillRight = pillLeft + pillWidth

        // 1. Sleek Tactical HUD Pill Backdrop
        scratchRect.set(pillLeft, pillTop, pillRight, pillBottom)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            pillLeft, pillTop, pillRight, pillBottom,
            0xF40A1828.toInt(), 0xF803070E.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 4f * dp, 4f * dp, paint)
        paint.shader = null

        // 2. Precision Laser Rim on Pill
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(accent, 175)
        canvas.drawRoundRect(scratchRect, 4f * dp, 4f * dp, paint)

        // Specular top highlight line
        paint.strokeWidth = 0.8f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(pillLeft + 4f * dp, pillTop + 0.8f * dp, pillRight - 4f * dp, pillTop + 0.8f * dp, paint)

        // 3. Left Power Notch on Pill
        paint.style = Paint.Style.FILL
        paint.color = accent
        scratchRect2.set(pillLeft + 1f * dp, pillTop + 2.5f * dp, pillLeft + 3.5f * dp, pillBottom - 2.5f * dp)
        canvas.drawRoundRect(scratchRect2, 1f * dp, 1f * dp, paint)

        // 4. Pill Content: one lead marker and a comfortably inset title.
        val diamondX = pillLeft + 12f * dp
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 30)
        canvas.drawCircle(diamondX, cy, 5f * dp, paint)
        paint.color = accent
        drawDiamondNode(canvas, diamondX, cy, 2.3f * dp, paint)

        val labelX = diamondX + 12f * dp
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        textPaint.textSize = labelSize
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = withAlpha(accent, 80)
        canvas.drawText(label, labelX, baseline + 0.8f * dp, textPaint)
        textPaint.color = 0xFFF2FBFF.toInt()
        canvas.drawText(label, labelX, baseline, textPaint)
        textPaint.letterSpacing = 0f

        // 5. Continue the divider directly from the pill rim, then let it fade out.
        val lineStart = pillRight - 1f * dp
        if (lineStart < right - 8f * dp) {
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 1f * dp
            paint.shader = LinearGradient(
                lineStart, cy, right - 8f * dp, cy,
                withAlpha(accent, 180), withAlpha(accent, 0), Shader.TileMode.CLAMP
            )
            canvas.drawLine(lineStart, cy, right - 8f * dp, cy, paint)
            paint.shader = null
        }
    }

    private fun drawDiamondNode(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        tempPath.reset()
        tempPath.moveTo(x, y - radius)
        tempPath.lineTo(x + radius, y)
        tempPath.lineTo(x, y + radius)
        tempPath.lineTo(x - radius, y)
        tempPath.close()
        canvas.drawPath(tempPath, paint)
    }

    fun drawResetDialog(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        pageLeft: Float,
        pageWidth: Float,
        centerX: Float,
        cancelButton: RectF,
        confirmButton: RectF,
        paint: Paint,
        dp: Float,
        t: (String) -> String
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0xCC02040A.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)
        val width = kotlin.math.min(pageWidth - 28f * dp, 360f * dp)
        val left = centerX - width * 0.5f
        val top = viewHeight * 0.5f - 100f * dp
        scratchRect.set(left, top, left + width, top + 200f * dp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(scratchRect.left, scratchRect.top, scratchRect.right, scratchRect.bottom, 0xEE081322.toInt(), 0xEE030811.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, 14f * dp, 14f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(scratchRect.left, scratchRect.top, scratchRect.right, scratchRect.bottom, KavvoroPalette.cyan, KavvoroPalette.pink, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, 14f * dp, 14f * dp, paint)
        paint.shader = null
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = 18f * dp
        textPaint.color = KavvoroPalette.pink
        canvas.drawText(t("RESET PROGRESS?"), scratchRect.centerX(), scratchRect.top + 40f * dp, textPaint)
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 11f * dp
        textPaint.color = 0xCCDDE4EF.toInt()
        canvas.drawText(t("This cannot be undone."), scratchRect.centerX(), scratchRect.top + 68f * dp, textPaint)
        val buttonTop = scratchRect.bottom - 58f * dp
        cancelButton.set(scratchRect.left + 16f * dp, buttonTop, scratchRect.centerX() - 5f * dp, scratchRect.bottom - 16f * dp)
        confirmButton.set(scratchRect.centerX() + 5f * dp, buttonTop, scratchRect.right - 16f * dp, scratchRect.bottom - 16f * dp)
        CyberShapeRenderer.drawCyberChamferRect(canvas, cancelButton, 7f * dp, 5f * dp, paint)
        CyberShapeRenderer.drawCyberChamferRect(canvas, confirmButton, 7f * dp, 5f * dp, paint)
        textPaint.textSize = 10f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("CANCEL"), cancelButton.centerX(), cancelButton.centerY() + 3f * dp, textPaint)
        textPaint.color = KavvoroPalette.pink
        canvas.drawText(t("RESET"), confirmButton.centerX(), confirmButton.centerY() + 3f * dp, textPaint)
    }

    fun drawModulePanel(
        canvas: Canvas,
        rect: RectF,
        paint: Paint,
        dp: Float,
        isDanger: Boolean = false,
        cornerRadius: Float = 16f * dp
    ) {
        // Soft atmospheric vignette under the deck bed - zero wireframe lines, zero brackets!
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            rect.centerX(), rect.centerY(), rect.width() * 0.7f,
            intArrayOf(0x30020814, 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect.left - 12f * dp, rect.top - 8f * dp, rect.right + 12f * dp, rect.bottom + 8f * dp, paint)
        paint.shader = null
    }

    fun drawPanelDivider(
        canvas: Canvas,
        left: Float,
        right: Float,
        y: Float,
        paint: Paint,
        dp: Float,
        indentLeft: Float = 10f * dp,
        indentRight: Float = 10f * dp,
        color: Int = 0x1400F0FF.toInt()
    ) {
        // Individual cards have distinct borders and 6dp spacing; divider omitted for clean standalone deck.
    }

    private fun drawSubCardPlate(
        canvas: Canvas,
        rect: RectF,
        accent: Int,
        active: Boolean,
        paint: Paint,
        dp: Float,
        isDanger: Boolean = false
    ) {
        val corner = 10f * dp
        val cardRect = scratchRect
        // Inset by 3dp vertically to create a tangible 6dp physical gap between adjacent setting cards!
        cardRect.set(rect.left, rect.top + 3f * dp, rect.right, rect.bottom - 3f * dp)

        // 1. Ambient Drop Shadow / Elevation
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0x55000000.toInt()
        canvas.drawRoundRect(
            cardRect.left, cardRect.top + 2.5f * dp, cardRect.right, cardRect.bottom + 4.5f * dp,
            corner, corner, paint
        )

        // 2. Heavy Polycarbonate Dark Obsidian Glass Fill
        val topFill = if (isDanger) 0xF61F050E.toInt() else 0xF6091426.toInt()
        val bottomFill = if (isDanger) 0xF80B0205.toInt() else 0xF8030810.toInt()
        paint.shader = LinearGradient(
            cardRect.left, cardRect.top, cardRect.left, cardRect.bottom,
            topFill, bottomFill, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardRect, corner, corner, paint)
        paint.shader = null

        // 3. Diagonal Surface Light Sheen Gradient
        val sheenAlpha = if (active) 65 else 32
        paint.shader = LinearGradient(
            cardRect.left, cardRect.top, cardRect.right, cardRect.bottom,
            intArrayOf(withAlpha(accent, sheenAlpha), 0x00000000, withAlpha(accent, 15)),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardRect, corner, corner, paint)
        paint.shader = null

        // 4. Razor-Sharp Specular Top Highlight Bevel Line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        val highlightColor = if (isDanger) withAlpha(KavvoroPalette.pink, 96) else 0x40FFFFFF
        paint.shader = LinearGradient(
            cardRect.left + 16f * dp, cardRect.top + 1f * dp,
            cardRect.right - 16f * dp, cardRect.top + 1f * dp,
            intArrayOf(0x00FFFFFF, highlightColor, 0x00FFFFFF),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(cardRect.left + 16f * dp, cardRect.top + 1f * dp, cardRect.right - 16f * dp, cardRect.top + 1f * dp, paint)
        paint.shader = null

        // 5. Precision Laser Perimeter Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (if (active) 1.6f else 1.1f) * dp
        val borderTop = withAlpha(accent, if (active) 235 else 165)
        val borderBottom = withAlpha(accent, if (active) 130 else 75)
        paint.shader = LinearGradient(
            cardRect.left, cardRect.top, cardRect.left, cardRect.bottom,
            borderTop, borderBottom, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardRect, corner, corner, paint)
        paint.shader = null

        // 6. Left Tactical Power Conduit Notch
        val notchW = 3.2f * dp
        val notchH = cardRect.height() * 0.46f
        val notchY = cardRect.centerY()
        paint.style = Paint.Style.FILL
        // Power conduit glow bloom
        paint.color = withAlpha(accent, if (active) 100 else 50)
        canvas.drawRoundRect(
            cardRect.left + 1f * dp, notchY - notchH * 0.5f - 1f * dp,
            cardRect.left + 1f * dp + notchW + 2f * dp, notchY + notchH * 0.5f + 1f * dp,
            1.5f * dp, 1.5f * dp, paint
        )
        // Power conduit solid illuminated core
        paint.color = if (active) 0xFFFFFFFF.toInt() else accent
        canvas.drawRoundRect(
            cardRect.left + 1.5f * dp, notchY - notchH * 0.5f,
            cardRect.left + 1.5f * dp + notchW, notchY + notchH * 0.5f,
            1.2f * dp, 1.2f * dp, paint
        )
    }

    fun drawSliderRow(
        canvas: Canvas,
        rect: RectF,
        sliderRect: RectF,
        title: String,
        subtitle: String,
        value: Int,
        accent: Int,
        iconId: SettingsIconRenderer.SettingsIconId,
        active: Boolean,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        breakpoint: SettingsBreakpoint = SettingsBreakpoint.MOBILE,
        viewWidth: Float = 0f,
        viewHeight: Float = 0f
    ) {
        // 1. Modular Tactical Sub-Card Plate
        drawSubCardPlate(canvas, rect, accent, active, paint, dp)

        val vw = if (viewWidth > 0f) viewWidth else rect.width()
        val vh = if (viewHeight > 0f) viewHeight else vw
        val badgeSize = rowIconSize(vw, dp, vh)
        val gap = SettingsLayoutCalculator.rowGap(vw, dp, vh)
        val iconLeft = rect.left + 8f * dp
        iconScratchRect.set(
            iconLeft,
            rect.centerY() - badgeSize * 0.5f,
            iconLeft + badgeSize,
            rect.centerY() + badgeSize * 0.5f
        )
        SettingsIconRenderer.drawSettingsIcon(canvas, iconScratchRect, iconId, accent, active, paint, dp)

        val textLeft = iconLeft + badgeSize + gap
        val maxTextWidth = (sliderRect.left - textLeft - 8f * dp).coerceAtLeast(32f * dp)
        drawRowCopy(canvas, rect, title, subtitle, textLeft, maxTextWidth, 0xFFFFFFFF.toInt(), compact, dp, breakpoint, vw, vh)

        drawSlider(canvas, sliderRect, value, accent, active, paint, dp)

        // 2. Monospace Digital Telemetry Percentage Capsule
        val capW = (if (compact) 34f else 38f) * dp
        val capH = (if (compact) 18f else 20f) * dp
        val capRight = rect.right - 6f * dp
        val capLeft = capRight - capW
        val capTop = rect.centerY() - capH * 0.5f
        val capBottom = rect.centerY() + capH * 0.5f
        controlScratchRect.set(capLeft, capTop, capRight, capBottom)

        // Capsule glass body
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xD0050D18.toInt()
        CyberShapeRenderer.drawCyberChamferRect(canvas, controlScratchRect, 4f * dp, 3f * dp, paint)

        // Capsule laser rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 160)
        CyberShapeRenderer.drawCyberChamferRect(canvas, controlScratchRect, 4f * dp, 3f * dp, paint)

        // Specular top highlight on capsule
        paint.strokeWidth = 0.8f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(controlScratchRect.left + 3f * dp, controlScratchRect.top + 0.8f * dp, controlScratchRect.right - 3f * dp, controlScratchRect.top + 0.8f * dp, paint)

        // Monospace/Arcade text
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.MONOSPACE
        textPaint.textSize = (if (compact) 9f else 10f) * dp
        textPaint.color = accent
        canvas.drawText("$value%", controlScratchRect.centerX(), controlScratchRect.centerY() + textPaint.textSize * 0.35f, textPaint)
    }

    fun drawToggleRow(
        canvas: Canvas,
        rect: RectF,
        title: String,
        subtitle: String,
        enabled: Boolean,
        accent: Int,
        active: Boolean,
        iconId: SettingsIconRenderer.SettingsIconId,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        breakpoint: SettingsBreakpoint = SettingsBreakpoint.MOBILE,
        viewWidth: Float = 0f,
        viewHeight: Float = 0f
    ) {
        // 1. Modular Tactical Sub-Card Plate
        drawSubCardPlate(canvas, rect, accent, active, paint, dp)

        val vw = if (viewWidth > 0f) viewWidth else rect.width()
        val vh = if (viewHeight > 0f) viewHeight else vw
        val badgeSize = rowIconSize(vw, dp, vh)
        val gap = SettingsLayoutCalculator.rowGap(vw, dp, vh)
        val iconLeft = rect.left + 8f * dp
        iconScratchRect.set(
            iconLeft,
            rect.centerY() - badgeSize * 0.5f,
            iconLeft + badgeSize,
            rect.centerY() + badgeSize * 0.5f
        )
        SettingsIconRenderer.drawSettingsIcon(canvas, iconScratchRect, iconId, accent, active, paint, dp)

        val toggleWidth = (if (compact) 40f else 44f) * dp
        val toggleHeight = (if (compact) 22f else 24f) * dp
        val toggleRight = rect.right - 8f * dp
        controlScratchRect.set(
            toggleRight - toggleWidth,
            rect.centerY() - toggleHeight * 0.5f,
            toggleRight,
            rect.centerY() + toggleHeight * 0.5f
        )

        val textLeft = iconLeft + badgeSize + gap
        val maxTextWidth = (controlScratchRect.left - textLeft - 8f * dp).coerceAtLeast(32f * dp)
        drawRowCopy(canvas, rect, title, subtitle, textLeft, maxTextWidth, 0xFFFFFFFF.toInt(), compact, dp, breakpoint, vw, vh)

        drawToggle(canvas, controlScratchRect, enabled, accent, active, paint, dp)
    }

    fun drawNavRow(
        canvas: Canvas,
        rect: RectF,
        title: String,
        subtitle: String,
        accent: Int,
        iconId: SettingsIconRenderer.SettingsIconId,
        showFrame: Boolean = true,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        breakpoint: SettingsBreakpoint = SettingsBreakpoint.MOBILE,
        viewWidth: Float = 0f,
        viewHeight: Float = 0f
    ) {
        val isPink = (iconId == SettingsIconRenderer.SettingsIconId.RESET)
        val isYellow = (iconId == SettingsIconRenderer.SettingsIconId.DATA_DELETION)
        val effectiveAccent = if (isYellow) KavvoroPalette.gold else accent

        // 1. Modular Tactical Sub-Card Plate
        drawSubCardPlate(canvas, rect, effectiveAccent, false, paint, dp, isDanger = isPink)

        val vw = if (viewWidth > 0f) viewWidth else rect.width()
        val vh = if (viewHeight > 0f) viewHeight else vw
        val badgeSize = rowIconSize(vw, dp, vh)
        val gap = SettingsLayoutCalculator.rowGap(vw, dp, vh)
        val iconLeft = rect.left + 8f * dp
        iconScratchRect.set(
            iconLeft,
            rect.centerY() - badgeSize * 0.5f,
            iconLeft + badgeSize,
            rect.centerY() + badgeSize * 0.5f
        )
        SettingsIconRenderer.drawSettingsIcon(canvas, iconScratchRect, iconId, effectiveAccent, false, paint, dp)

        val chevronSlotW = 32f * dp
        val chevronCx = rect.right - 18f * dp
        val chevronCy = rect.centerY()

        // Recessed circular backing for chevron
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0x240A1828.toInt()
        canvas.drawCircle(chevronCx, chevronCy, 12f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(effectiveAccent, 60)
        canvas.drawCircle(chevronCx, chevronCy, 12f * dp, paint)

        drawChevron(canvas, chevronCx, chevronCy, withAlpha(effectiveAccent, 240), paint, dp)

        val textLeft = iconLeft + badgeSize + gap
        val maxTextWidth = (rect.right - chevronSlotW - textLeft).coerceAtLeast(24f * dp)
        val titleColor = if (isPink) KavvoroPalette.pink else if (isYellow) KavvoroPalette.gold else 0xFFFFFFFF.toInt()
        drawRowCopy(canvas, rect, title, subtitle, textLeft, maxTextWidth, titleColor, compact, dp, breakpoint, vw, vh)
    }

    fun drawResetRow(
        canvas: Canvas,
        rect: RectF,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        t: (String) -> String,
        breakpoint: SettingsBreakpoint = SettingsBreakpoint.MOBILE,
        viewWidth: Float = 0f,
        viewHeight: Float = 0f
    ) {
        val accent = KavvoroPalette.pink

        // 1. Hazard Warning Sub-Card Plate
        drawSubCardPlate(canvas, rect, accent, false, paint, dp, isDanger = true)

        val vw = if (viewWidth > 0f) viewWidth else rect.width()
        val vh = if (viewHeight > 0f) viewHeight else vw
        val badgeSize = rowIconSize(vw, dp, vh)
        val gap = SettingsLayoutCalculator.rowGap(vw, dp, vh)
        val iconLeft = rect.left + 8f * dp
        iconScratchRect.set(
            iconLeft,
            rect.centerY() - badgeSize * 0.5f,
            iconLeft + badgeSize,
            rect.centerY() + badgeSize * 0.5f
        )
        SettingsIconRenderer.drawSettingsIcon(canvas, iconScratchRect, SettingsIconRenderer.SettingsIconId.RESET, accent, false, paint, dp)

        val chevronSlotW = 32f * dp
        val chevronCx = rect.right - 18f * dp
        val chevronCy = rect.centerY()

        // Hazard chevron backing
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0x2A330B18.toInt()
        canvas.drawCircle(chevronCx, chevronCy, 12f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(KavvoroPalette.pink, 102)
        canvas.drawCircle(chevronCx, chevronCy, 12f * dp, paint)

        drawChevron(canvas, chevronCx, chevronCy, accent, paint, dp)

        val textLeft = iconLeft + badgeSize + gap
        val maxTextWidth = (rect.right - chevronSlotW - textLeft).coerceAtLeast(24f * dp)
        drawRowCopy(
            canvas, rect, t("RESET PROGRESS"), t("Clears gameplay only"),
            textLeft, maxTextWidth, accent, compact, dp, breakpoint, vw, vh
        )
    }

    private fun drawRowCopy(
        canvas: Canvas,
        rect: RectF,
        title: String,
        subtitle: String,
        textLeft: Float,
        maxTextWidth: Float,
        titleColor: Int,
        compact: Boolean,
        dp: Float,
        breakpoint: SettingsBreakpoint,
        viewWidth: Float = 0f,
        viewHeight: Float = 0f
    ) {
        val vw = if (viewWidth > 0f) viewWidth else rect.width()
        val vh = if (viewHeight > 0f) viewHeight else vw
        val preferredTitleSize = SettingsLayoutCalculator.rowTitleSize(vw, dp, vh)
        val subtitleSize = SettingsLayoutCalculator.rowSubtitleSize(vw, dp, vh)
        val showSubtitle = rowShowsSubtitle(rect, subtitle, dp)
        val width = maxTextWidth.toInt().coerceAtLeast(1)

        val hasCustomFont = AssetResourceManager.oxaniumTypeface != null
        layoutPaint.reset()
        layoutPaint.isAntiAlias = true
        layoutPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
        layoutPaint.textSize = preferredTitleSize
        val measuredTitleWidth = layoutPaint.measureText(title)
        val minTitleSize = (if (SettingsLayoutCalculator.widthDp(vw, dp) <= 480f) 12.5f else 14f) * dp
        val fittedTitleSize = if (measuredTitleWidth > maxTextWidth && measuredTitleWidth > 0f) {
            (preferredTitleSize * maxTextWidth / measuredTitleWidth).coerceAtLeast(minTitleSize)
        } else {
            preferredTitleSize
        }
        val titleKey = StaticLayoutKey(title, width, fittedTitleSize.toRawBits(), titleColor, false, hasCustomFont)
        val titleLayout = staticLayoutCache.getOrPut(titleKey) {
            layoutPaint.reset()
            layoutPaint.isAntiAlias = true
            layoutPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.DEFAULT_BOLD
            layoutPaint.textSize = fittedTitleSize
            layoutPaint.color = titleColor
            layoutPaint.textAlign = Paint.Align.LEFT
            StaticLayout.Builder
                .obtain(title, 0, title.length, TextPaint(layoutPaint), width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setIncludePad(false)
                .build()
        }

        var subtitleLayout: StaticLayout? = null
        if (showSubtitle) {
            val subKey = StaticLayoutKey(subtitle, width, subtitleSize.toRawBits(), 0xCCA4C4E0.toInt(), true, true)
            subtitleLayout = staticLayoutCache.getOrPut(subKey) {
                subLayoutPaint.reset()
                subLayoutPaint.isAntiAlias = true
                subLayoutPaint.typeface = Typeface.DEFAULT
                subLayoutPaint.textSize = subtitleSize
                subLayoutPaint.color = 0xCCA4C4E0.toInt()
                subLayoutPaint.textAlign = Paint.Align.LEFT
                StaticLayout.Builder
                    .obtain(subtitle, 0, subtitle.length, TextPaint(subLayoutPaint), width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setMaxLines(2)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setIncludePad(false)
                    .build()
            }
        }

        val blockHeight = titleLayout.height + (subtitleLayout?.let { it.height + 2f * dp } ?: 0f)
        val blockTop = rect.centerY() - blockHeight * 0.5f
        canvas.save()
        canvas.translate(textLeft, blockTop)
        titleLayout.draw(canvas)
        subtitleLayout?.let {
            canvas.translate(0f, titleLayout.height + 2f * dp)
            it.draw(canvas)
        }
        canvas.restore()
    }

    fun drawSettingsScreen(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        safeCenterX: Float,
        pageContentLeft: Float,
        pageContentRight: Float,
        settingsViewportTop: Float,
        settingsViewportBottom: Float,
        compact: Boolean,
        activeSettingsButton: SettingsButton,
        settingsMasterButton: RectF,
        settingsMasterSlider: RectF,
        settingsMasterVolume: Int,
        settingsMusicButton: RectF,
        settingsMusicSlider: RectF,
        settingsMusicVolume: Int,
        settingsSfxButton: RectF,
        settingsSfxSlider: RectF,
        settingsSfxVolume: Int,
        settingsHapticToggle: RectF,
        settingsHapticEnabled: Boolean,
        settingsShakeToggle: RectF,
        settingsScreenShake: Boolean,
        settingsPerformanceToggle: RectF,
        settingsPerformanceMode: Boolean,
        settingsLanguageButton: RectF,
        selectedLanguageLabel: String,
        settingsAccountButton: RectF,
        accountStatusLabel: String,
        settingsPrivacyButton: RectF,
        settingsTermsButton: RectF,
        settingsDataDeletionButton: RectF,
        settingsAboutButton: RectF,
        versionName: String,
        settingsResetButton: RectF,
        settingsBackButton: RectF,
        settingsResetConfirm: Boolean,
        settingsResetCancelButton: RectF,
        settingsResetConfirmButton: RectF,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        isRtl: Boolean = false,
        activeSettingsTab: SettingsTab = SettingsTab.AUDIO,
        tabAudio: RectF? = null,
        tabGameplay: RectF? = null,
        tabSystem: RectF? = null,
        tabInfo: RectF? = null,
        contentPanel: RectF? = null,
        header: SettingsHeaderMetrics,
        brandLogo: Bitmap? = null,
        brandMotto: String = "",
        profileName: String = "",
        profileOnline: Boolean = false,
        breakpoint: SettingsBreakpoint = if (compact) SettingsBreakpoint.MOBILE else SettingsBreakpoint.DESKTOP
    ) {
        paint.reset()
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeMiter = 4f
        paint.pathEffect = null
        paint.shader = null

        drawBackdrop(canvas, viewWidth, viewHeight, safeCenterX, pageContentLeft, pageContentRight, settingsViewportTop, settingsViewportBottom, paint, dp)
        val left = pageContentLeft
        val right = pageContentRight
        val titleSize = header.titleSize
        val subtitleSize = header.subtitleSize
        val titleBaseline = header.titleBaseline
        val subtitleBaseline = header.subtitleBaseline
        val dividerY = header.dividerY

        drawBrandLogo(canvas, brandLogo, header, paint, textPaint, dp, fitText, isRtl)
        drawBrandMotto(canvas, brandMotto, header, dp)
        drawProfileChip(
            canvas = canvas,
            header = header,
            name = profileName,
            online = profileOnline,
            onlineLabel = t("ONLINE"),
            paint = paint,
            dp = dp,
            viewWidth = viewWidth,
            viewHeight = viewHeight
        )

        drawSettingsHero(
            canvas = canvas,
            left = left,
            right = right,
            centerX = safeCenterX,
            title = t("SETTINGS").uppercase(),
            subtitle = t("PERSONALIZE YOUR EXPERIENCE").uppercase(),
            titleBaseline = titleBaseline,
            titleSize = titleSize,
            subtitleBaseline = subtitleBaseline,
            subtitleSize = subtitleSize,
            dividerY = dividerY,
            paint = paint,
            dp = dp,
            widthDp = SettingsLayoutCalculator.widthDp(viewWidth, dp)
        )

        canvas.save()
        val clipBottom = if (settingsBackButton.isEmpty) {
            settingsViewportBottom
        } else {
            minOf(settingsViewportBottom, settingsBackButton.top - 8f * dp)
        }
        canvas.clipRect(0f, settingsViewportTop, viewWidth, clipBottom)

        tabAudio?.let { drawTabPill(canvas, it, t("AUDIO"), SettingsIconRenderer.SettingsIconId.MASTER_VOLUME, activeSettingsTab == SettingsTab.AUDIO, activeSettingsButton == SettingsButton.TAB_AUDIO, compact, paint, dp, viewWidth, viewHeight) }
        tabGameplay?.let { drawTabPill(canvas, it, t("GAMEPLAY"), SettingsIconRenderer.SettingsIconId.SCREEN_SHAKE, activeSettingsTab == SettingsTab.GAMEPLAY, activeSettingsButton == SettingsButton.TAB_GAMEPLAY, compact, paint, dp, viewWidth, viewHeight) }
        tabSystem?.let { drawTabPill(canvas, it, t("SYSTEM"), SettingsIconRenderer.SettingsIconId.LANGUAGE, activeSettingsTab == SettingsTab.SYSTEM, activeSettingsButton == SettingsButton.TAB_SYSTEM, compact, paint, dp, viewWidth, viewHeight) }
        tabInfo?.let { drawTabPill(canvas, it, t("ABOUT"), SettingsIconRenderer.SettingsIconId.ABOUT, activeSettingsTab == SettingsTab.INFO, activeSettingsButton == SettingsButton.TAB_INFO, compact, paint, dp, viewWidth, viewHeight) }

        contentPanel?.let {
            drawModulePanel(
                canvas, it, paint, dp, isDanger = false,
                cornerRadius = SettingsLayoutCalculator.cardRadius(viewWidth, dp, viewHeight)
            )
        }

        val headerTitle = when (activeSettingsTab) {
            SettingsTab.AUDIO -> t("AUDIO & SOUND FX")
            SettingsTab.GAMEPLAY -> t("GAMEPLAY & VISUALS")
            SettingsTab.SYSTEM -> t("SYSTEM & ACCOUNT")
            SettingsTab.INFO -> t("ABOUT & LEGAL")
        }
        val pLeft = contentPanel?.left ?: left
        val pRight = contentPanel?.right ?: right
        val pTop = (contentPanel?.top ?: settingsViewportTop) +
            SettingsLayoutCalculator.cardPaddingY(viewWidth, viewHeight, dp) +
            SettingsLayoutCalculator.sectionTitleSize(viewWidth, dp, viewHeight)
        val sectionInset = SettingsLayoutCalculator.cardPaddingX(viewWidth, viewHeight, dp)

        drawSectionLabel(
            canvas, headerTitle.uppercase(), pLeft + sectionInset, pRight - sectionInset, pTop,
            KavvoroPalette.cyan, paint, dp, viewWidth, viewHeight
        )

        val rowDividerColor = withAlpha(KavvoroPalette.cyan, 18)
        when (activeSettingsTab) {
            SettingsTab.AUDIO -> {
                if (!settingsMasterButton.isEmpty) {
                    drawSliderRow(canvas, settingsMasterButton, settingsMasterSlider, t("MASTER VOLUME"), t("Main game volume"), settingsMasterVolume, KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.MASTER_VOLUME, activeSettingsButton == SettingsButton.MASTER_VOLUME, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsMasterButton.left, settingsMasterButton.right, settingsMasterButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsMusicButton.isEmpty) {
                    drawSliderRow(canvas, settingsMusicButton, settingsMusicSlider, t("MUSIC VOLUME"), t("Synthwave soundtrack"), settingsMusicVolume, KavvoroPalette.pink, SettingsIconRenderer.SettingsIconId.MUSIC_VOLUME, activeSettingsButton == SettingsButton.MUSIC_VOLUME, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsMusicButton.left, settingsMusicButton.right, settingsMusicButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsSfxButton.isEmpty) {
                    drawSliderRow(canvas, settingsSfxButton, settingsSfxSlider, t("SOUND EFFECTS"), t("Arcade sound effects"), settingsSfxVolume, KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.SFX_VOLUME, activeSettingsButton == SettingsButton.SFX_VOLUME, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsSfxButton.left, settingsSfxButton.right, settingsSfxButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsHapticToggle.isEmpty) {
                    drawToggleRow(canvas, settingsHapticToggle, t("HAPTIC FEEDBACK"), t("Vibration on actions"), settingsHapticEnabled, KavvoroPalette.cyan, activeSettingsButton == SettingsButton.HAPTIC, SettingsIconRenderer.SettingsIconId.HAPTIC, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                }
            }
            SettingsTab.GAMEPLAY -> {
                if (!settingsShakeToggle.isEmpty) {
                    drawToggleRow(canvas, settingsShakeToggle, t("SCREEN SHAKE"), t("Shake the screen on impact"), settingsScreenShake, KavvoroPalette.cyan, activeSettingsButton == SettingsButton.SCREEN_SHAKE, SettingsIconRenderer.SettingsIconId.SCREEN_SHAKE, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsShakeToggle.left, settingsShakeToggle.right, settingsShakeToggle.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsPerformanceToggle.isEmpty) {
                    val perfSubtitle = if (settingsPerformanceMode) {
                        t("ON • Eco 60 FPS / No Blurs / Extended Battery")
                    } else {
                        t("OFF • Maximum AAA Visuals & Shaders")
                    }
                    val perfColor = if (settingsPerformanceMode) 0xFF64E572.toInt() else KavvoroPalette.cyan
                    drawToggleRow(
                        canvas,
                        settingsPerformanceToggle,
                        t("PERFORMANCE MODE"),
                        perfSubtitle,
                        settingsPerformanceMode,
                        perfColor,
                        activeSettingsButton == SettingsButton.PERFORMANCE,
                        SettingsIconRenderer.SettingsIconId.PERFORMANCE,
                        compact,
                        paint,
                        dp,
                        fitText,
                        breakpoint,
                        viewWidth,
                        viewHeight
                    )
                }
            }
            SettingsTab.SYSTEM -> {
                if (!settingsLanguageButton.isEmpty) {
                    drawNavRow(canvas, settingsLanguageButton, t("LANGUAGE"), selectedLanguageLabel, KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.LANGUAGE, true, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsLanguageButton.left, settingsLanguageButton.right, settingsLanguageButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsAccountButton.isEmpty) {
                    drawNavRow(canvas, settingsAccountButton, t("ACCOUNT"), accountStatusLabel, KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.ACCOUNT, true, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsAccountButton.left, settingsAccountButton.right, settingsAccountButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsResetButton.isEmpty) {
                    drawResetRow(canvas, settingsResetButton, compact, paint, dp, fitText, t, breakpoint, viewWidth, viewHeight)
                }
            }
            SettingsTab.INFO -> {
                if (!settingsAboutButton.isEmpty) {
                    drawNavRow(canvas, settingsAboutButton, t("ABOUT MOONSOL STUDIOS"), "Kavvoro v$versionName", KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.ABOUT, true, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsAboutButton.left, settingsAboutButton.right, settingsAboutButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsPrivacyButton.isEmpty) {
                    drawNavRow(canvas, settingsPrivacyButton, t("PRIVACY POLICY"), t("Privacy details & terms"), KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.PRIVACY, true, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsPrivacyButton.left, settingsPrivacyButton.right, settingsPrivacyButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsTermsButton.isEmpty) {
                    drawNavRow(canvas, settingsTermsButton, t("TERMS OF SERVICE"), t("Terms and conditions"), KavvoroPalette.cyan, SettingsIconRenderer.SettingsIconId.TERMS, true, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                    drawPanelDivider(canvas, settingsTermsButton.left, settingsTermsButton.right, settingsTermsButton.bottom, paint, dp, 0f, 0f, rowDividerColor)
                }
                if (!settingsDataDeletionButton.isEmpty) {
                    drawNavRow(canvas, settingsDataDeletionButton, t("DATA DELETION"), t("Erase all local app data"), KavvoroPalette.gold, SettingsIconRenderer.SettingsIconId.DATA_DELETION, true, compact, paint, dp, fitText, breakpoint, viewWidth, viewHeight)
                }
            }
        }

        canvas.restore()

        drawBackHomeButton(
            canvas, settingsBackButton, activeSettingsButton == SettingsButton.BACK,
            t("BACK TO MAIN MENU").uppercase(), compact, paint, dp, viewWidth, viewHeight
        )

        if (settingsResetConfirm) {
            drawResetDialog(canvas, viewWidth, viewHeight, pageContentLeft, pageContentRight - pageContentLeft, safeCenterX, settingsResetCancelButton, settingsResetConfirmButton, paint, dp, t)
        }
    }

    private fun drawSettingsHero(
        canvas: Canvas,
        left: Float,
        right: Float,
        centerX: Float,
        title: String,
        subtitle: String,
        titleBaseline: Float,
        titleSize: Float,
        subtitleBaseline: Float,
        subtitleSize: Float,
        dividerY: Float,
        paint: Paint,
        dp: Float,
        widthDp: Float
    ) {
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        val safeTitleWidth = (right - left - 24f * dp).coerceAtLeast(1f * dp)
        textPaint.textSize = titleSize
        textPaint.letterSpacing = 0.05f
        val measuredTitleWidth = textPaint.measureText(title)
        val titleScale = if (measuredTitleWidth > safeTitleWidth) safeTitleWidth / measuredTitleWidth else 1f
        val fittedTitleSize = (titleSize * titleScale).coerceAtLeast(22f * dp)
        textPaint.textSize = fittedTitleSize
        val displayTitle = ellipsizeForPaint(title, textPaint, safeTitleWidth)
        val titleWidth = textPaint.measureText(displayTitle)
        val lineGap = (16f * dp).coerceAtMost((right - left) * 0.06f)
        val lineY = titleBaseline - titleSize * 0.32f
        val lineLeftEnd = centerX - titleWidth * 0.5f - lineGap
        val lineRightStart = centerX + titleWidth * 0.5f + lineGap

        // Glowing Flanking Laser Lines with Diamond Ticks
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        if (lineLeftEnd > left + 8f * dp) {
            paint.shader = LinearGradient(
                left, lineY, lineLeftEnd, lineY,
                withAlpha(KavvoroPalette.cyan, 0), withAlpha(KavvoroPalette.cyan, 170), Shader.TileMode.CLAMP
            )
            canvas.drawLine(left, lineY, lineLeftEnd, lineY, paint)

            // Diamond tick at lineLeftEnd
            paint.shader = null
            paint.style = Paint.Style.FILL
            paint.color = KavvoroPalette.cyan
            val dSize = 2.2f * dp
            tempPath.reset()
            tempPath.moveTo(lineLeftEnd, lineY - dSize)
            tempPath.lineTo(lineLeftEnd + dSize, lineY)
            tempPath.lineTo(lineLeftEnd, lineY + dSize)
            tempPath.lineTo(lineLeftEnd - dSize, lineY)
            tempPath.close()
            canvas.drawPath(tempPath, paint)
        }
        if (lineRightStart < right - 8f * dp) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f * dp
            paint.shader = LinearGradient(
                lineRightStart, lineY, right, lineY,
                withAlpha(KavvoroPalette.cyan, 170), withAlpha(KavvoroPalette.cyan, 0), Shader.TileMode.CLAMP
            )
            canvas.drawLine(lineRightStart, lineY, right, lineY, paint)

            // Diamond tick at lineRightStart
            paint.shader = null
            paint.style = Paint.Style.FILL
            paint.color = KavvoroPalette.cyan
            val dSize = 2.2f * dp
            tempPath.reset()
            tempPath.moveTo(lineRightStart, lineY - dSize)
            tempPath.lineTo(lineRightStart + dSize, lineY)
            tempPath.lineTo(lineRightStart, lineY + dSize)
            tempPath.lineTo(lineRightStart - dSize, lineY)
            tempPath.close()
            canvas.drawPath(tempPath, paint)
        }
        paint.shader = null

        // Title Under-Glow Pass
        textPaint.color = 0x5045F2FF.toInt()
        canvas.drawText(displayTitle, centerX, titleBaseline + 1.2f * dp, textPaint)
        // Title Crisp Foreground
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText(displayTitle, centerX, titleBaseline, textPaint)

        // Telemetric Subtitle
        textPaint.typeface = Typeface.DEFAULT
        val safeSubtitleWidth = (right - left - 20f * dp).coerceAtLeast(1f * dp)
        textPaint.textSize = subtitleSize
        textPaint.letterSpacing = if (widthDp <= 480f) 0.12f else 0.16f
        textPaint.color = 0xCCB5D8F0.toInt()
        val measuredSubtitleWidth = textPaint.measureText(subtitle)
        val subtitleScale = if (measuredSubtitleWidth > safeSubtitleWidth) {
            safeSubtitleWidth / measuredSubtitleWidth
        } else {
            1f
        }
        textPaint.textSize = (subtitleSize * subtitleScale).coerceAtLeast(9.5f * dp)
        val displaySubtitle = ellipsizeForPaint(subtitle, textPaint, safeSubtitleWidth)
        canvas.drawText(displaySubtitle, centerX, subtitleBaseline, textPaint)
        textPaint.letterSpacing = 0f

        drawDivider(canvas, left, right, dividerY, centerX, paint, dp)
    }

    private fun drawBrandLogo(
        canvas: Canvas,
        bitmap: Bitmap?,
        header: SettingsHeaderMetrics,
        paint: Paint,
        fallbackTextPaint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        isRtl: Boolean
    ) {
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
            BrandTitleRenderer.draw(
                canvas = canvas,
                x = header.brandLeft,
                topY = header.brandTop,
                maxWidth = header.brandMaxWidth,
                targetHeight = header.brandHeight,
                isRtl = isRtl,
                paint = paint,
                textPaint = fallbackTextPaint,
                dp = dp,
                fitText = fitText
            )
            return
        }

        val sourceAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetWidth = minOf(header.brandMaxWidth, header.brandHeight * sourceAspect)
        val targetHeight = targetWidth / sourceAspect
        scratchRect.set(
            header.brandLeft,
            header.brandTop,
            header.brandLeft + targetWidth,
            header.brandTop + targetHeight
        )
        paint.reset()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.alpha = 255
        paint.style = Paint.Style.FILL
        canvas.drawBitmap(bitmap, null, scratchRect, paint)
    }

    private fun drawBrandMotto(canvas: Canvas, motto: String, header: SettingsHeaderMetrics, dp: Float) {
        if (!header.brandMottoVisible || motto.isBlank() || header.brandMaxWidth <= 0f) return
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.color = 0xFFEAF5FF.toInt()
        textPaint.letterSpacing = 0.32f
        textPaint.textAlign = Paint.Align.CENTER
        val maxWidth = header.brandMaxWidth
        val fullMotto = motto.uppercase()
        textPaint.textSize = header.brandMottoSize
        val measuredWidth = textPaint.measureText(fullMotto)
        if (measuredWidth > maxWidth && measuredWidth > 0f) {
            textPaint.textSize = (header.brandMottoSize * maxWidth / measuredWidth).coerceAtLeast(7f * dp)
        }
        val displayMotto = ellipsizeForPaint(fullMotto, textPaint, maxWidth)
        canvas.drawText(displayMotto, header.brandLeft + maxWidth * 0.5f, header.brandMottoBaseline, textPaint)
        textPaint.letterSpacing = 0f
    }

    private fun ellipsizeForPaint(text: CharSequence, paint: Paint, maxWidth: Float): String {
        layoutPaint.set(paint)
        return TextUtils.ellipsize(text, layoutPaint, maxWidth, TextUtils.TruncateAt.END).toString()
    }

    private fun drawProfileChip(
        canvas: Canvas,
        header: SettingsHeaderMetrics,
        name: String,
        online: Boolean,
        onlineLabel: String,
        paint: Paint,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float = viewWidth
    ) {
        val displayName = name.ifBlank { return }
        val iconSize = SettingsLayoutCalculator.iconByWidth(
            SettingsLayoutCalculator.widthDp(viewWidth, dp), 26f, 30f, 34f, 38f
        ) * dp
        val iconTop = header.profileTop
        val iconRight = header.profileRight
        val iconCy = iconTop + iconSize * 0.5f

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.color = 0xFFE8F4FF.toInt()
        val maxName = SettingsLayoutCalculator.profileNameMaxWidth(viewWidth, dp)

        textPaint.textAlign = Paint.Align.LEFT
        val profileSize = SettingsLayoutCalculator.profileTextSize(viewWidth, dp, viewHeight)
        textPaint.textSize = profileSize
        val label = ellipsize(displayName, maxName, textPaint)
        val nameW = textPaint.measureText(label)
        var statusW = 0f
        if (online) {
            textPaint.textSize = (profileSize * 0.7f).coerceAtLeast(8.5f * dp)
            statusW = 10f * dp + textPaint.measureText(onlineLabel.uppercase())
            textPaint.textSize = profileSize
        }
        val textW = maxOf(nameW, statusW)
        val blockLeft = iconRight - iconSize - 8f * dp - textW
        val alignedIconLeft = blockLeft
        val alignedIconCx = alignedIconLeft + iconSize * 0.5f
        drawProfileHex(canvas, alignedIconCx, iconCy, iconSize * 0.52f, paint, dp)
        drawProfilePerson(canvas, alignedIconCx, iconCy, iconSize * 0.22f, paint)
        val textX = alignedIconLeft + iconSize + 8f * dp
        val nameY = if (online) iconCy - 1f * dp else iconCy + 4f * dp
        textPaint.textSize = profileSize
        textPaint.color = 0xFFE8F4FF.toInt()
        canvas.drawText(label, textX, nameY, textPaint)
        if (online) {
            paint.style = Paint.Style.FILL
            paint.color = 0xFF64E572.toInt()
            canvas.drawCircle(textX + 3f * dp, iconCy + 8.5f * dp, 2.2f * dp, paint)
            textPaint.textSize = 8.5f * dp
            textPaint.color = 0xFF64E572.toInt()
            canvas.drawText(onlineLabel.uppercase(), textX + 10f * dp, iconCy + 11.5f * dp, textPaint)
        }
    }

    private fun drawProfilePerson(canvas: Canvas, cx: Float, cy: Float, unit: Float, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeMiter = 4f
        paint.pathEffect = null
        paint.color = KavvoroPalette.cyan
        canvas.drawCircle(cx, cy - unit * 0.55f, unit * 0.72f, paint)
        tempPath.reset()
        tempPath.moveTo(cx - unit * 1.45f, cy + unit * 1.55f)
        tempPath.quadTo(cx - unit * 0.9f, cy + unit * 0.15f, cx, cy + unit * 0.15f)
        tempPath.quadTo(cx + unit * 0.9f, cy + unit * 0.15f, cx + unit * 1.45f, cy + unit * 1.55f)
        tempPath.close()
        canvas.drawPath(tempPath, paint)
    }

    private fun drawProfileHex(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint, dp: Float) {
        tempPath.reset()
        for (i in 0..5) {
            val angle = Math.toRadians(30.0 + i * 60.0)
            val x = cx + radius * cos(angle).toFloat()
            val y = cy + radius * sin(angle).toFloat()
            if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
        }
        tempPath.close()
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xE20B1A2C.toInt()
        canvas.drawPath(tempPath, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeMiter = 4f
        paint.pathEffect = null
        paint.shader = null
        paint.strokeWidth = 1.2f * dp
        paint.color = withAlpha(KavvoroPalette.cyan, 204)
        canvas.drawPath(tempPath, paint)
    }

    private fun ellipsize(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return if (end <= 0) ellipsis else text.substring(0, end) + ellipsis
    }
}
