package com.moonsolstudios.kavvoro.ui.screens.modals

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.UiTypography
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object TabletOrientationPromptRenderer {

    private val cardScratch = RectF()
    private val iconScratch = RectF()
    private val buttonScratch = RectF()
    private val badgeScratch = RectF()
    private val arrowPath = Path()

    fun drawModal(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        controller: TabletOrientationPromptController,
        context: Context,
        paint: Paint,
        textPaint: Paint,
        dp: Float
    ) {
        val language = KavvoroI18n.active(context)

        // 1. Semi-transparent backdrop
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0xDE060913.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        // 2. Card dimensions
        val cardWidth = min(viewWidth - 40f * dp, 410f * dp)
        val cardHeight = 370f * dp
        val cardLeft = (viewWidth - cardWidth) * 0.5f
        val cardTop = (viewHeight - cardHeight) * 0.46f
        val cardRight = cardLeft + cardWidth
        val cardBottom = cardTop + cardHeight
        cardScratch.set(cardLeft, cardTop, cardRight, cardBottom)
        controller.modalCardRect.set(cardScratch)

        // Outer soft glow
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f * dp
        paint.color = 0x3326E5FF.toInt()
        canvas.drawRoundRect(cardScratch, 18f * dp, 18f * dp, paint)

        // Card background gradient
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cardLeft, cardTop, cardRight, cardBottom,
            intArrayOf(0xFF10192C.toInt(), 0xFF080B14.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardScratch, 16f * dp, 16f * dp, paint)
        paint.shader = null

        // Card dual-tone cyber border (Cyan to Magenta)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.shader = LinearGradient(
            cardLeft, cardTop, cardRight, cardBottom,
            intArrayOf(0xFF26E5FF.toInt(), 0xFFFF2A85.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(cardScratch, 16f * dp, 16f * dp, paint)
        paint.shader = null

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(cardLeft + 14f * dp, cardTop + 1f * dp, cardRight - 14f * dp, cardTop + 1f * dp, paint)

        // Corner decorative cyber-brackets
        val bracketLen = 14f * dp
        paint.strokeWidth = 3f * dp
        paint.color = 0xFF26E5FF.toInt()
        // Top-left
        canvas.drawLine(cardLeft + 8f * dp, cardTop + 4f * dp, cardLeft + 8f * dp + bracketLen, cardTop + 4f * dp, paint)
        canvas.drawLine(cardLeft + 4f * dp, cardTop + 8f * dp, cardLeft + 4f * dp, cardTop + 8f * dp + bracketLen, paint)
        // Top-right
        canvas.drawLine(cardRight - 8f * dp - bracketLen, cardTop + 4f * dp, cardRight - 8f * dp, cardTop + 4f * dp, paint)
        canvas.drawLine(cardRight - 4f * dp, cardTop + 8f * dp, cardRight - 4f * dp, cardTop + 8f * dp + bracketLen, paint)
        // Bottom corners in Magenta
        paint.color = 0xFFFF2A85.toInt()
        // Bottom-left
        canvas.drawLine(cardLeft + 8f * dp, cardBottom - 4f * dp, cardLeft + 8f * dp + bracketLen, cardBottom - 4f * dp, paint)
        canvas.drawLine(cardLeft + 4f * dp, cardBottom - 8f * dp - bracketLen, cardLeft + 4f * dp, cardBottom - 8f * dp, paint)
        // Bottom-right
        canvas.drawLine(cardRight - 8f * dp - bracketLen, cardBottom - 4f * dp, cardRight - 8f * dp, cardBottom - 4f * dp, paint)
        canvas.drawLine(cardRight - 4f * dp, cardBottom - 8f * dp - bracketLen, cardRight - 4f * dp, cardBottom - 8f * dp, paint)

        // 3. Vector Rotating Device Illustration
        val iconCenterY = cardTop + 66f * dp
        drawRotatingDeviceIllustration(canvas, cardScratch.centerX(), iconCenterY, controller.pulseTimer, paint, dp)

        // 4. Eyebrow badge
        val eyebrowText = TabletPromptTranslations.getEyebrow(language)
        val eyebrowY = cardTop + 140f * dp
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 10.5f * dp
        textPaint.letterSpacing = 0.12f
        textPaint.textAlign = Paint.Align.CENTER
        val eyebrowW = textPaint.measureText(eyebrowText) + 24f * dp
        val eyebrowH = 20f * dp
        iconScratch.set(
            cardScratch.centerX() - eyebrowW * 0.5f,
            eyebrowY - eyebrowH * 0.5f,
            cardScratch.centerX() + eyebrowW * 0.5f,
            eyebrowY + eyebrowH * 0.5f
        )
        paint.style = Paint.Style.FILL
        paint.color = 0x3326E5FF.toInt()
        canvas.drawRoundRect(iconScratch, eyebrowH * 0.5f, eyebrowH * 0.5f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x8826E5FF.toInt()
        canvas.drawRoundRect(iconScratch, eyebrowH * 0.5f, eyebrowH * 0.5f, paint)

        textPaint.color = 0xFF45F2FF.toInt()
        canvas.drawText(eyebrowText, cardScratch.centerX(), eyebrowY + 3.5f * dp, textPaint)

        // 5. Headline: "ROTATE YOUR SCREEN"
        val titleText = TabletPromptTranslations.getTitle(language)
        textPaint.letterSpacing = 0.05f
        textPaint.textSize = UiTypography.PANEL_TITLE_DP * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.setShadowLayer(8f * dp, 0f, 2f * dp, 0x80000000.toInt())
        canvas.drawText(titleText, cardScratch.centerX(), cardTop + 180f * dp, textPaint)
        textPaint.clearShadowLayer()

        // 6. Description Body
        val descText = TabletPromptTranslations.getDescription(language)
        textPaint.textSize = 13f * dp
        textPaint.letterSpacing = 0.02f
        textPaint.color = 0xFFABC2DD.toInt()
        drawMultilineCentered(canvas, descText, cardScratch.centerX(), cardTop + 212f * dp, cardWidth - 52f * dp, 18f * dp, textPaint)

        // 7. Action Button: "PLAY ANYWAY"
        val btnHeight = 46f * dp
        val btnWidth = cardWidth - 56f * dp
        val btnLeft = cardScratch.centerX() - btnWidth * 0.5f
        val btnTop = cardBottom - 64f * dp
        val btnRight = btnLeft + btnWidth
        val btnBottom = btnTop + btnHeight
        buttonScratch.set(btnLeft, btnTop, btnRight, btnBottom)
        controller.dismissButtonRect.set(buttonScratch)

        // Button background
        paint.style = Paint.Style.FILL
        paint.color = 0xFF122238.toInt()
        canvas.drawRoundRect(buttonScratch, 8f * dp, 8f * dp, paint)

        // Button glowing border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * dp
        paint.color = 0xFF26E5FF.toInt()
        canvas.drawRoundRect(buttonScratch, 8f * dp, 8f * dp, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x55FFFFFF
        canvas.drawLine(buttonScratch.left + 6f * dp, buttonScratch.top + 1f * dp, buttonScratch.right - 6f * dp, buttonScratch.top + 1f * dp, paint)

        // Button text
        val buttonText = TabletPromptTranslations.getDismissButton(language)
        textPaint.textSize = 14f * dp
        textPaint.letterSpacing = 0.08f
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText(buttonText, buttonScratch.centerX(), btnTop + 28f * dp, textPaint)
    }

    fun drawPersistentBadge(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        controller: TabletOrientationPromptController,
        context: Context,
        paint: Paint,
        textPaint: Paint,
        dp: Float
    ) {
        val language = KavvoroI18n.active(context)
        val text = TabletPromptTranslations.getBadgeText(language)

        val badgeHeight = 36f * dp
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 11.5f * dp
        textPaint.letterSpacing = 0.06f
        val textWidth = textPaint.measureText(text)
        val badgeWidth = min(viewWidth - 32f * dp, textWidth + 48f * dp)

        // In landscape tablet, anchor badge gracefully to top-right margin to avoid obstructing header chips
        val badgeRight = viewWidth - 20f * dp
        val badgeLeft = badgeRight - badgeWidth
        val badgeTop = 18f * dp
        val badgeBottom = badgeTop + badgeHeight
        badgeScratch.set(badgeLeft, badgeTop, badgeRight, badgeBottom)
        controller.persistentBadgeRect.set(badgeScratch)

        // Breathing pulse animation
        val pulse = (sin(controller.pulseTimer * 3.5f) + 1f) * 0.5f
        val glowAlpha = (70 + 80 * pulse).toInt().coerceIn(0, 255)

        // Pill background
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xEA090D18.toInt()
        canvas.drawRoundRect(badgeScratch, badgeHeight * 0.5f, badgeHeight * 0.5f, paint)

        // Pulsing border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = (1.4f + 0.6f * pulse) * dp
        paint.color = (glowAlpha shl 24) or 0x0026E5FF
        canvas.drawRoundRect(badgeScratch, badgeHeight * 0.5f, badgeHeight * 0.5f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = 0x44FFFFFF
        canvas.drawLine(badgeScratch.left + 12f * dp, badgeScratch.top + 1f * dp, badgeScratch.right - 12f * dp, badgeScratch.top + 1f * dp, paint)

        // Rotate Icon (Mini vector on the left)
        val iconX = badgeLeft + 20f * dp
        val iconY = badgeTop + badgeHeight * 0.5f
        drawMiniRotateIcon(canvas, iconX, iconY, controller.pulseTimer, paint, dp)

        // Badge Text
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFF0F6FF.toInt()
        canvas.drawText(text, iconX + 14f * dp, badgeTop + 22.5f * dp, textPaint)
    }

    private fun drawRotatingDeviceIllustration(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        timer: Float,
        paint: Paint,
        dp: Float
    ) {
        // 1. Tablet outline (landscape aspect: 54dp x 36dp)
        val tabletW = 54f * dp
        val tabletH = 36f * dp
        iconScratch.set(cx - tabletW * 0.5f, cy - tabletH * 0.5f, cx + tabletW * 0.5f, cy + tabletH * 0.5f)

        // Tablet body fill
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xFF142036.toInt()
        canvas.drawRoundRect(iconScratch, 6f * dp, 6f * dp, paint)

        // Tablet body stroke
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.color = 0xFF26E5FF.toInt()
        canvas.drawRoundRect(iconScratch, 6f * dp, 6f * dp, paint)

        // Inner screen glow
        val innerW = tabletW - 8f * dp
        val innerH = tabletH - 8f * dp
        iconScratch.set(cx - innerW * 0.5f, cy - innerH * 0.5f, cx + innerW * 0.5f, cy + innerH * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = 0x2226E5FF.toInt()
        canvas.drawRoundRect(iconScratch, 3f * dp, 3f * dp, paint)

        // Center screen vertical phone placeholder indicating desired orientation
        val phoneW = 12f * dp
        val phoneH = 20f * dp
        iconScratch.set(cx - phoneW * 0.5f, cy - phoneH * 0.5f, cx + phoneW * 0.5f, cy + phoneH * 0.5f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0xFFFF2A85.toInt()
        canvas.drawRoundRect(iconScratch, 2f * dp, 2f * dp, paint)

        // 2. Orbiting curved arrows indicating rotation
        val orbitRadius = 40f * dp
        val rotAngle = (timer * 80f) % 360f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * dp
        paint.color = 0xFF26E5FF.toInt()

        iconScratch.set(cx - orbitRadius, cy - orbitRadius, cx + orbitRadius, cy + orbitRadius)
        // Arc 1
        canvas.drawArc(iconScratch, rotAngle, 90f, false, paint)
        drawArrowHead(canvas, cx, cy, orbitRadius, rotAngle + 90f, paint, dp)

        // Arc 2 (opposite side)
        paint.color = 0xFFFF2A85.toInt()
        canvas.drawArc(iconScratch, rotAngle + 180f, 90f, false, paint)
        drawArrowHead(canvas, cx, cy, orbitRadius, rotAngle + 270f, paint, dp)
    }

    private fun drawArrowHead(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        angleDeg: Float,
        paint: Paint,
        dp: Float
    ) {
        val rad = Math.toRadians(angleDeg.toDouble())
        val tipX = cx + (radius * cos(rad)).toFloat()
        val tipY = cy + (radius * sin(rad)).toFloat()

        // Tangent direction + 90 degrees
        val tangentAngle = Math.toRadians((angleDeg + 90.0))
        val arrowSize = 6f * dp

        arrowPath.reset()
        arrowPath.moveTo(tipX, tipY)
        val backAngle1 = tangentAngle - Math.PI * 0.75
        val backAngle2 = tangentAngle + Math.PI * 0.75
        arrowPath.lineTo((tipX + arrowSize * cos(backAngle1)).toFloat(), (tipY + arrowSize * sin(backAngle1)).toFloat())
        arrowPath.moveTo(tipX, tipY)
        arrowPath.lineTo((tipX + arrowSize * cos(backAngle2)).toFloat(), (tipY + arrowSize * sin(backAngle2)).toFloat())
        canvas.drawPath(arrowPath, paint)
    }

    private fun drawMiniRotateIcon(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        timer: Float,
        paint: Paint,
        dp: Float
    ) {
        val radius = 7f * dp
        iconScratch.set(cx - radius, cy - radius, cx + radius, cy + radius)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * dp
        paint.color = 0xFF26E5FF.toInt()
        val angle = (timer * 100f) % 360f
        canvas.drawArc(iconScratch, angle, 240f, false, paint)
        drawArrowHead(canvas, cx, cy, radius, angle + 240f, paint, dp)
    }

    private fun drawMultilineCentered(
        canvas: Canvas,
        text: String,
        cx: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ) {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = candidate
            } else {
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, cx, y, paint)
                    y += lineHeight
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, cx, y, paint)
        }
    }
}

object TabletPromptTranslations {
    fun getTitle(lang: KavvoroLanguage): String =
        KavvoroI18n.t(lang, "ROTATE YOUR SCREEN")

    fun getDescription(lang: KavvoroLanguage): String =
        KavvoroI18n.t(
            lang,
            "For the best full-screen arcade experience, please rotate your tablet to portrait orientation."
        )

    fun getDismissButton(lang: KavvoroLanguage): String =
        KavvoroI18n.t(lang, "PLAY ANYWAY")

    fun getBadgeText(lang: KavvoroLanguage): String =
        KavvoroI18n.t(lang, "ROTATE FOR FULLSCREEN")

    fun getEyebrow(lang: KavvoroLanguage): String =
        KavvoroI18n.t(lang, "TABLET DETECTED")
}
