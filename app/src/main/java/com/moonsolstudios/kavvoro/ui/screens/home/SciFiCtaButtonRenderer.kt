package com.moonsolstudios.kavvoro.ui.screens.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.i18n.HomeCopy
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import kotlin.math.max
import kotlin.math.min

/**
 * Procedural renderer for the approved KAVVORO Play CTA button.
 *
 * Implements a dynamic neon gradient (Cyan -> Blue -> Purple -> Pink), glowing double border,
 * auto-fitting bold typography, play triangle and chevron vector icons, and smooth press response.
 * Renders its gradient, highlight, icons, and localized text at runtime.
 */
object SciFiCtaButtonRenderer {

    const val usesRasterBackground: Boolean = false

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerRect = RectF()
    private var cachedPlayDrawable: android.graphics.drawable.Drawable? = null
    private var cachedChevronDrawable: android.graphics.drawable.Drawable? = null

    fun draw(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        density: Float,
        context: Context,
        playTitle: String = HomeCopy.ctaPlay(context),
        playTitleShort: String = HomeCopy.ctaPlayShort(context),
        playSubtitle: String = HomeCopy.ctaSubtitle(context),
        showSubtitle: Boolean = true
    ) {
        val w = rect.width()
        val h = rect.height()
        if (w <= 1f || h <= 1f) return

        val dp = density.coerceAtLeast(0.1f)
        val pressScale = if (active) 0.975f else 1.0f

        // Procedural gradient button with an inset white highlight and edge glow.
        val radius = min(32f * dp, h * 0.48f)

        canvas.save()
        canvas.scale(pressScale, pressScale, rect.centerX(), rect.centerY())

        // 1. Outer Neon Glow (Cyan on left, Magenta on right, -18% on press)
        val glowAlpha = if (active) 94 else 115
        buttonPaint.reset()
        buttonPaint.isAntiAlias = true
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 6f * dp
        buttonPaint.shader = LinearGradient(
            rect.left, rect.centerY(),
            rect.right, rect.centerY(),
            intArrayOf(
                Color.argb(glowAlpha, Color.red(KavvoroPalette.cyan), Color.green(KavvoroPalette.cyan), Color.blue(KavvoroPalette.cyan)),
                Color.argb(glowAlpha, Color.red(KavvoroPalette.purple), Color.green(KavvoroPalette.purple), Color.blue(KavvoroPalette.purple)),
                Color.argb(glowAlpha, Color.red(KavvoroPalette.pink), Color.green(KavvoroPalette.pink), Color.blue(KavvoroPalette.pink))
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, buttonPaint)

        // 2. Base Gradient Fill
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.shader = LinearGradient(
            rect.left, rect.top,
            rect.right, rect.bottom,
            intArrayOf(
                KavvoroPalette.cyan,
                KavvoroPalette.blue,
                KavvoroPalette.purple,
                KavvoroPalette.pink
            ),
            floatArrayOf(0f, 0.32f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, buttonPaint)

        // 3. Subtle Inner Specular Sheen (top-down white sheen)
        buttonPaint.shader = LinearGradient(
            rect.centerX(), rect.top,
            rect.centerX(), rect.bottom,
            intArrayOf(
                0x66FFFFFF,
                0x1AFFFFFF,
                0x00FFFFFF,
                0x26000000
            ),
            floatArrayOf(0f, 0.28f, 0.70f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radius, radius, buttonPaint)

        // 4. Crisp Inner Highlight Border
        buttonPaint.shader = null
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 1.8f * dp
        buttonPaint.color = 0xAAFFFFFF.toInt()
        innerRect.set(rect.left + 1f * dp, rect.top + 1f * dp, rect.right - 1f * dp, rect.bottom - 1f * dp)
        canvas.drawRoundRect(innerRect, radius - 1f * dp, radius - 1f * dp, buttonPaint)

        // 5. Left Play Icon & Right Chevron Icon
        val iconSize = (if (showSubtitle) 30f else 26f) * dp
        val playIconLeft = rect.left + 22f * dp
        val playIconTop = rect.centerY() - iconSize * 0.5f

        val playDrawable = cachedPlayDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_play)?.mutate()?.also {
            cachedPlayDrawable = it
        }
        if (playDrawable != null) {
            playDrawable.setBounds(
                playIconLeft.toInt(),
                playIconTop.toInt(),
                (playIconLeft + iconSize).toInt(),
                (playIconTop + iconSize).toInt()
            )
            playDrawable.setTint(Color.WHITE)
            playDrawable.draw(canvas)
        }

        val chevronSize = 24f * dp
        val chevronRight = rect.right - 22f * dp
        val chevronTop = rect.centerY() - chevronSize * 0.5f
        val chevronDrawable = cachedChevronDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_chevron_right)?.mutate()?.also {
            cachedChevronDrawable = it
        }
        if (chevronDrawable != null) {
            chevronDrawable.setBounds(
                (chevronRight - chevronSize).toInt(),
                chevronTop.toInt(),
                chevronRight.toInt(),
                (chevronTop + chevronSize).toInt()
            )
            chevronDrawable.setTint(Color.WHITE)
            chevronDrawable.draw(canvas)
        }

        // 6. Typography (Auto-Fitting 34sp standard, 28sp compact, 26sp minimum)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            textPaint.fontVariationSettings = "'wght' 800"
        }
        textPaint.typeface = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            AssetResourceManager.spaceGroteskExtraBold()
        } else {
            AssetResourceManager.spaceGroteskBold()
        }
        textPaint.isFakeBoldText = true
        textPaint.color = Color.WHITE

        val textCenterX = rect.centerX()
        val textMaxW = rect.width() - (playIconLeft + iconSize - rect.left) * 2f - 16f * dp

        val minSize = 26f * dp
        val baseTitleSize = min(44f * dp, h * 0.34f)

        if (showSubtitle && playSubtitle.isNotBlank()) {
            val titleCenterY = rect.centerY() - 7f * dp
            val subCenterY = rect.centerY() + 15f * dp

            var chosenTitle = playTitle
            var titleSize = min(baseTitleSize, h * 0.34f)
            textPaint.textSize = titleSize
            textPaint.letterSpacing = 0.04f

            var measuredTitleW = textPaint.measureText(chosenTitle)
            if (measuredTitleW > textMaxW && measuredTitleW > 0f) {
                titleSize = max(minSize, titleSize * (textMaxW / measuredTitleW))
                textPaint.textSize = titleSize
                measuredTitleW = textPaint.measureText(chosenTitle)
            }
            if (measuredTitleW > textMaxW) {
                textPaint.letterSpacing = 0f
                measuredTitleW = textPaint.measureText(chosenTitle)
            }
            if (measuredTitleW > textMaxW && playTitleShort.isNotBlank()) {
                chosenTitle = playTitleShort
                titleSize = min(baseTitleSize, h * 0.34f)
                textPaint.textSize = titleSize
                textPaint.letterSpacing = 0.04f
                measuredTitleW = textPaint.measureText(chosenTitle)
                if (measuredTitleW > textMaxW && measuredTitleW > 0f) {
                    titleSize = max(minSize, titleSize * (textMaxW / measuredTitleW))
                    textPaint.textSize = titleSize
                    textPaint.letterSpacing = 0f
                }
            }

            val titleFm = textPaint.fontMetrics
            val titleBaseline = titleCenterY - (titleFm.ascent + titleFm.descent) / 2f
            textPaint.setShadowLayer(8f * dp, 0f, 0f, 0x8800E5FF.toInt())
            canvas.drawText(chosenTitle, textCenterX, titleBaseline, textPaint)
            textPaint.clearShadowLayer()

            // Subtitle
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                textPaint.fontVariationSettings = "'wght' 400"
            }
            textPaint.typeface = AssetResourceManager.oxaniumNormal()
            textPaint.color = 0xFFF0F4FF.toInt()
            textPaint.letterSpacing = 0.08f
            var subSize = (11f * dp).coerceIn(10f * dp, 12f * dp).coerceAtMost(h * 0.13f)
            textPaint.textSize = subSize
            val measuredSubW = textPaint.measureText(playSubtitle)
            if (measuredSubW > textMaxW && measuredSubW > 0f) {
                subSize *= (textMaxW / measuredSubW)
                textPaint.textSize = subSize
            }
            val subFm = textPaint.fontMetrics
            val subBaseline = subCenterY - (subFm.ascent + subFm.descent) / 2f
            textPaint.setShadowLayer(4f * dp, 0f, 0f, 0x66000000)
            canvas.drawText(playSubtitle, textCenterX, subBaseline, textPaint)
            textPaint.clearShadowLayer()
            textPaint.letterSpacing = 0f
        } else {
            // Title only (vertically centered)
            var chosenTitle = playTitle
            val standardTitleSize = min(44f * dp, h * 0.44f)
            var titleSize = standardTitleSize
            textPaint.textSize = titleSize
            textPaint.letterSpacing = 0.04f

            var measuredTitleW = textPaint.measureText(chosenTitle)
            if (measuredTitleW > textMaxW && measuredTitleW > 0f) {
                titleSize = max(minSize, standardTitleSize * (textMaxW / measuredTitleW))
                textPaint.textSize = titleSize
                measuredTitleW = textPaint.measureText(chosenTitle)
            }
            if (measuredTitleW > textMaxW) {
                textPaint.letterSpacing = 0f
                measuredTitleW = textPaint.measureText(chosenTitle)
            }
            if (measuredTitleW > textMaxW && playTitleShort.isNotBlank()) {
                chosenTitle = playTitleShort
                titleSize = standardTitleSize
                textPaint.textSize = titleSize
                textPaint.letterSpacing = 0.04f
                measuredTitleW = textPaint.measureText(chosenTitle)
                if (measuredTitleW > textMaxW && measuredTitleW > 0f) {
                    titleSize = max(minSize, standardTitleSize * (textMaxW / measuredTitleW))
                    textPaint.textSize = titleSize
                    textPaint.letterSpacing = 0f
                }
            }

            val titleFm = textPaint.fontMetrics
            val titleBaseline = rect.centerY() - (titleFm.ascent + titleFm.descent) / 2f
            textPaint.setShadowLayer(10f * dp, 0f, 0f, 0x8800E5FF.toInt())
            canvas.drawText(chosenTitle, textCenterX, titleBaseline, textPaint)
            textPaint.clearShadowLayer()
        }

        canvas.restore()
    }
}
