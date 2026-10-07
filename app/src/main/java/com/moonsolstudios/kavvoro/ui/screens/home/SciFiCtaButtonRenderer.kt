package com.moonsolstudios.kavvoro.ui.screens.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
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
    private val chassisPath = Path()
    private val insetPath = Path()
    private val arrowPath = Path()
    private var shaderLeft = Float.NaN
    private var shaderTop = Float.NaN
    private var shaderRight = Float.NaN
    private var shaderBottom = Float.NaN
    private var shaderDensity = Float.NaN
    private var cachedOuterGlowIdle: LinearGradient? = null
    private var cachedOuterGlowActive: LinearGradient? = null
    private var cachedChassisFill: LinearGradient? = null
    private var cachedFaceFill: LinearGradient? = null
    private var cachedFaceSheen: LinearGradient? = null
    private var cachedPortalGlow: RadialGradient? = null
    private var cachedPortalRim: LinearGradient? = null
    private var cachedPlayDrawable: android.graphics.drawable.Drawable? = null

    private fun ensureShaders(rect: RectF, dp: Float, portalCx: Float, portalCy: Float, portalRadius: Float) {
        if (shaderLeft == rect.left && shaderTop == rect.top && shaderRight == rect.right &&
            shaderBottom == rect.bottom && shaderDensity == dp
        ) return

        shaderLeft = rect.left
        shaderTop = rect.top
        shaderRight = rect.right
        shaderBottom = rect.bottom
        shaderDensity = dp

        fun glow(alpha: Int) = LinearGradient(
            rect.left, rect.centerY(), rect.right, rect.centerY(),
            intArrayOf(
                Color.argb(alpha, Color.red(KavvoroPalette.cyan), Color.green(KavvoroPalette.cyan), Color.blue(KavvoroPalette.cyan)),
                Color.argb(alpha, Color.red(KavvoroPalette.purple), Color.green(KavvoroPalette.purple), Color.blue(KavvoroPalette.purple)),
                Color.argb(alpha, Color.red(KavvoroPalette.pink), Color.green(KavvoroPalette.pink), Color.blue(KavvoroPalette.pink))
            ),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        cachedOuterGlowIdle = glow(118)
        cachedOuterGlowActive = glow(94)
        cachedChassisFill = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            intArrayOf(KavvoroPalette.cyan, KavvoroPalette.blue, KavvoroPalette.purple, KavvoroPalette.pink),
            floatArrayOf(0f, 0.32f, 0.68f, 1f), Shader.TileMode.CLAMP
        )
        cachedFaceFill = LinearGradient(
            rect.left + 2.5f * dp, rect.top + 2.5f * dp,
            rect.right - 2.5f * dp, rect.bottom - 2.5f * dp,
            intArrayOf(0xF0162948.toInt(), 0xF41A1740.toInt(), 0xF427153F.toInt()),
            floatArrayOf(0f, 0.54f, 1f), Shader.TileMode.CLAMP
        )
        cachedFaceSheen = LinearGradient(
            rect.centerX(), rect.top + 2.5f * dp,
            rect.centerX(), rect.bottom - 2.5f * dp,
            intArrayOf(0x22FFFFFF, 0x0A8FEAFF, 0x00000000, 0x30000000),
            floatArrayOf(0f, 0.24f, 0.68f, 1f), Shader.TileMode.CLAMP
        )
        cachedPortalGlow = RadialGradient(
            portalCx, portalCy, portalRadius * 1.5f,
            intArrayOf(0x553DF7FF, 0x243B6FFF, 0x003B6FFF), null, Shader.TileMode.CLAMP
        )
        cachedPortalRim = LinearGradient(
            portalCx - portalRadius, portalCy - portalRadius,
            portalCx + portalRadius, portalCy + portalRadius,
            KavvoroPalette.cyan, KavvoroPalette.pink, Shader.TileMode.CLAMP
        )
    }

    private fun setChamferedPath(path: Path, rect: RectF, cut: Float) {
        val bevel = cut.coerceIn(0f, min(rect.width(), rect.height()) * 0.35f)
        path.reset()
        path.moveTo(rect.left + bevel, rect.top)
        path.lineTo(rect.right - bevel, rect.top)
        path.lineTo(rect.right, rect.top + bevel)
        path.lineTo(rect.right, rect.bottom - bevel)
        path.lineTo(rect.right - bevel, rect.bottom)
        path.lineTo(rect.left + bevel, rect.bottom)
        path.lineTo(rect.left, rect.bottom - bevel)
        path.lineTo(rect.left, rect.top + bevel)
        path.close()
    }

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

        // The Play action uses a beveled portal-gate chassis, distinct from the rounded menu cards.
        val bevel = min(h * 0.28f, 24f * dp)
        val inset = 2.5f * dp
        val medallionCx = rect.left + h * 0.61f
        val medallionCy = rect.centerY()
        val medallionRadius = (h * 0.285f).coerceAtLeast(13f * dp)
        ensureShaders(rect, dp, medallionCx, medallionCy, medallionRadius)

        canvas.save()
        canvas.scale(pressScale, pressScale, rect.centerX(), rect.centerY())

        // A low offset gives the control weight without obscuring the portal composition.
        innerRect.set(rect.left, rect.top + 3f * dp, rect.right, rect.bottom + 3f * dp)
        setChamferedPath(chassisPath, innerRect, bevel)
        buttonPaint.reset()
        buttonPaint.isAntiAlias = true
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.color = 0xE5080D25.toInt()
        canvas.drawPath(chassisPath, buttonPaint)

        // Luminous outer edge: restrained cyan through violet into pink.
        setChamferedPath(chassisPath, rect, bevel)
        buttonPaint.reset()
        buttonPaint.isAntiAlias = true
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 5f * dp
        buttonPaint.strokeJoin = Paint.Join.ROUND
        buttonPaint.shader = if (active) cachedOuterGlowActive else cachedOuterGlowIdle
        canvas.drawPath(chassisPath, buttonPaint)

        // Bright chassis rim under the dark glass face.
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.shader = cachedChassisFill
        canvas.drawPath(chassisPath, buttonPaint)

        // Deep glass face keeps the CTA legible against the colorful Home artwork.
        innerRect.set(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        setChamferedPath(insetPath, innerRect, (bevel - inset).coerceAtLeast(2f * dp))
        buttonPaint.shader = cachedFaceFill
        canvas.drawPath(insetPath, buttonPaint)

        // A soft top sheen is clipped to the custom silhouette.
        val clipSave = canvas.save()
        canvas.clipPath(insetPath)
        buttonPaint.shader = cachedFaceSheen
        canvas.drawRect(innerRect, buttonPaint)
        canvas.restoreToCount(clipSave)

        // Crisp glass edge, with a quiet cyan upper rail.
        buttonPaint.shader = null
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 1f * dp
        buttonPaint.color = 0x667BDFFF
        canvas.drawPath(insetPath, buttonPaint)
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 1.15f * dp
        buttonPaint.color = 0x997DF7FF.toInt()
        canvas.drawLine(rect.left + bevel + 8f * dp, rect.top + 1.5f * dp, rect.right - bevel - 8f * dp, rect.top + 1.5f * dp, buttonPaint)

        // Portal medallion: a compact, luminous focus for the play glyph.
        buttonPaint.reset()
        buttonPaint.isAntiAlias = true
        buttonPaint.style = Paint.Style.FILL
        buttonPaint.shader = cachedPortalGlow
        canvas.drawCircle(medallionCx, medallionCy, medallionRadius * 1.5f, buttonPaint)
        buttonPaint.shader = null
        buttonPaint.color = 0xB5112845.toInt()
        canvas.drawCircle(medallionCx, medallionCy, medallionRadius * 0.86f, buttonPaint)
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 1.5f * dp
        buttonPaint.shader = cachedPortalRim
        canvas.drawCircle(medallionCx, medallionCy, medallionRadius * 0.88f, buttonPaint)
        buttonPaint.shader = null
        buttonPaint.strokeWidth = 1f * dp
        buttonPaint.color = 0x55FFFFFF
        canvas.drawCircle(medallionCx, medallionCy, medallionRadius * 0.68f, buttonPaint)

        val iconSize = (if (showSubtitle) 27f else 24f) * dp
        val playIconLeft = medallionCx - iconSize * 0.5f
        val playIconTop = medallionCy - iconSize * 0.5f

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

        // Custom forward marker mirrors the chassis bevel and avoids a generic toolbar glyph.
        val arrowCx = rect.right - h * 0.48f
        val arrowCy = rect.centerY()
        val arrowHalf = min(h * 0.13f, 13f * dp)
        arrowPath.reset()
        arrowPath.moveTo(arrowCx - arrowHalf * 0.42f, arrowCy - arrowHalf)
        arrowPath.lineTo(arrowCx + arrowHalf * 0.62f, arrowCy)
        arrowPath.lineTo(arrowCx - arrowHalf * 0.42f, arrowCy + arrowHalf)
        buttonPaint.reset()
        buttonPaint.isAntiAlias = true
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 2.2f * dp
        buttonPaint.strokeCap = Paint.Cap.ROUND
        buttonPaint.strokeJoin = Paint.Join.ROUND
        buttonPaint.color = 0xDDF4FFFF.toInt()
        canvas.drawPath(arrowPath, buttonPaint)
        buttonPaint.color = 0x6689F5FF
        buttonPaint.strokeWidth = 5f * dp
        canvas.drawPath(arrowPath, buttonPaint)

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

        val textLeft = medallionCx + medallionRadius + 8f * dp
        val textRight = arrowCx - arrowHalf - 9f * dp
        val textCenterX = (textLeft + textRight) * 0.5f
        val textMaxW = (textRight - textLeft).coerceAtLeast(1f * dp)

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
