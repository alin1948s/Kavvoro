package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.SettingsButton

/**
 * Procedural renderer for Settings screen components (sliders, toggles, backdrop, section headers).
 */
object SettingsUiRenderer {

    private val tempPath = Path()
    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        paint.color = 0xFF020710.toInt()
        canvas.drawRect(0f, 0f, width, height, paint)

        paint.shader = RadialGradient(
            centerX - 115f * dp, height * 0.46f, 360f * dp,
            intArrayOf(0x1622DFFF, 0x060B4C7A, 0x00020710), null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)

        paint.shader = RadialGradient(
            centerX + 155f * dp, height * 0.52f, 330f * dp,
            intArrayOf(0x102A0B44, 0x050D0924, 0x00020710), null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f * dp
        paint.color = 0x100F8CA8
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
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(left, y, right, y, 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), Shader.TileMode.CLAMP)
        val notch = 8f * dp
        canvas.drawLine(left, y, centerX - notch, y, paint)
        canvas.drawLine(centerX + notch, y, right, y, paint)
        tempPath.reset()
        tempPath.moveTo(centerX - notch, y)
        tempPath.lineTo(centerX, y + 6f * dp)
        tempPath.lineTo(centerX + notch, y)
        canvas.drawPath(tempPath, paint)
        paint.shader = null
    }

    fun drawCardFrame(canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float, paint: Paint, dp: Float) {
        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(left, top, right, bottom, 0x160C1A2C, 0x08030C18, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, 12f * dp, 12f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.shader = LinearGradient(left, top, right, bottom, 0xAA45F2FF.toInt(), 0x885B173E.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, 12f * dp, 12f * dp, paint)
        paint.shader = null
        paint.color = 0x182E4154
        canvas.drawLine(left + 14f * dp, top + 0.8f * dp, right - 14f * dp, top + 0.8f * dp, paint)
    }

    fun drawSlider(canvas: Canvas, rect: RectF, value: Int, accent: Int, active: Boolean, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 1.4f * dp
        paint.color = 0x664C335F
        canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), paint)
        paint.shader = LinearGradient(rect.left, rect.centerY(), rect.right, rect.centerY(), 0xFF1DE8C8.toInt(), 0xFFFF4D8D.toInt(), Shader.TileMode.CLAMP)
        canvas.drawLine(rect.left, rect.centerY(), rect.left + rect.width() * value / 100f, rect.centerY(), paint)
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = if (active) 0xFFF7F4FF.toInt() else accent
        canvas.drawCircle(rect.left + rect.width() * value / 100f, rect.centerY(), (if (active) 7f else 6f) * dp, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawToggle(canvas: Canvas, rect: RectF, enabled: Boolean, accent: Int, active: Boolean, paint: Paint, dp: Float) {
        paint.style = Paint.Style.FILL
        paint.color = if (enabled) withAlpha(accent, if (active) 210 else 150) else 0x3023303E
        canvas.drawRoundRect(rect, rect.height() * 0.5f, rect.height() * 0.5f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = if (enabled) accent else 0x889AA8BA.toInt()
        canvas.drawRoundRect(rect, rect.height() * 0.5f, rect.height() * 0.5f, paint)
        paint.style = Paint.Style.FILL
        paint.color = if (enabled) 0xFFF7F4FF.toInt() else 0xFFB8BDC8.toInt()
        canvas.drawCircle(if (enabled) rect.right - rect.height() * 0.5f else rect.left + rect.height() * 0.5f, rect.centerY(), rect.height() * 0.34f, paint)
    }

    fun drawChevron(canvas: Canvas, cx: Float, cy: Float, color: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        val size = 5.5f * dp
        tempPath.reset()
        tempPath.moveTo(cx - size * 0.4f, cy - size)
        tempPath.lineTo(cx + size * 0.6f, cy)
        tempPath.lineTo(cx - size * 0.4f, cy + size)
        canvas.drawPath(tempPath, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
    }

    fun drawBackHomeButton(canvas: Canvas, rect: RectF, active: Boolean, label: String, compact: Boolean, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 2f * dp else 1.3f * dp
        paint.shader = LinearGradient(rect.left, rect.top, rect.right, rect.top, 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, 10f * dp, 10f * dp, paint)
        paint.shader = null

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (compact) 11f else 15f) * dp
        textPaint.color = 0xFF45F2FF.toInt()
        canvas.drawText(label, rect.centerX(), rect.centerY() + 5f * dp, textPaint)
    }

    fun drawGlyph(canvas: Canvas, rect: RectF, accent: Int, active: Boolean, glyph: Int, paint: Paint, dp: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 2f * dp else 1.4f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = withAlpha(accent, if (active) 255 else 220)
        val cx = rect.centerX()
        val cy = rect.centerY()
        when (glyph) {
            0 -> {
                canvas.drawCircle(cx, cy, 11f * dp, paint)
                canvas.drawCircle(cx, cy, 4f * dp, paint)
                canvas.drawLine(cx, cy - 14f * dp, cx, cy - 8f * dp, paint)
            }
            1 -> {
                canvas.drawRect(cx - 10f * dp, cy - 11f * dp, cx + 10f * dp, cy + 11f * dp, paint)
                canvas.drawCircle(cx, cy, 4f * dp, paint)
                canvas.drawLine(cx, cy - 16f * dp, cx, cy - 11f * dp, paint)
            }
            2 -> {
                tempPath.reset()
                tempPath.moveTo(cx, cy - 14f * dp)
                tempPath.lineTo(cx + 11f * dp, cy - 9f * dp)
                tempPath.lineTo(cx + 8f * dp, cy + 8f * dp)
                tempPath.lineTo(cx, cy + 14f * dp)
                tempPath.lineTo(cx - 8f * dp, cy + 8f * dp)
                tempPath.lineTo(cx - 11f * dp, cy - 9f * dp)
                tempPath.close()
                canvas.drawPath(tempPath, paint)
            }
            3 -> {
                canvas.drawRect(cx - 9f * dp, cy - 12f * dp, cx + 9f * dp, cy + 12f * dp, paint)
                canvas.drawLine(cx - 5f * dp, cy - 5f * dp, cx + 5f * dp, cy - 5f * dp, paint)
                canvas.drawLine(cx - 5f * dp, cy, cx + 5f * dp, cy, paint)
            }
            4 -> {
                canvas.drawCircle(cx, cy, 12f * dp, paint)
                textPaint.reset()
                textPaint.isAntiAlias = true
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 12f * dp
                textPaint.color = accent
                canvas.drawText("i", cx, cy + 4f * dp, textPaint)
            }
            5 -> {
                tempPath.reset()
                tempPath.arcTo(RectF(cx - 10f * dp, cy - 10f * dp, cx + 10f * dp, cy + 10f * dp), -55f, 290f)
                canvas.drawPath(tempPath, paint)
                canvas.drawLine(cx - 10f * dp, cy - 4f * dp, cx - 3f * dp, cy - 4f * dp, paint)
                canvas.drawLine(cx - 10f * dp, cy - 4f * dp, cx - 10f * dp, cy + 3f * dp, paint)
            }
            else -> {
                canvas.drawCircle(cx, cy, 11f * dp, paint)
                canvas.drawLine(cx - 8f * dp, cy, cx + 8f * dp, cy, paint)
                canvas.drawLine(cx, cy - 8f * dp, cx, cy + 8f * dp, paint)
            }
        }
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawSectionLabel(canvas: Canvas, label: String, left: Float, baseline: Float, compact: Boolean, accent: Int, dp: Float) {
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (compact) 14f else 17f) * dp
        textPaint.color = accent
        canvas.drawText(label, left + 14f * dp, baseline, textPaint)
    }

    fun drawGearButton(canvas: Canvas, rect: RectF, active: Boolean, paint: Paint, dp: Float) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = rect.width() * 0.26f
        HomeUiRenderer.drawGearIcon(canvas, cx, cy, radius, 0xFFF2F4F7.toInt(), paint, dp)
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
        val box = RectF(left, top, left + width, top + 200f * dp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(box.left, box.top, box.right, box.bottom, 0xEE081322.toInt(), 0xEE030811.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(box, 14f * dp, 14f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.shader = LinearGradient(box.left, box.top, box.right, box.bottom, 0xFF45F2FF.toInt(), 0xFFFF4D8D.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(box, 14f * dp, 14f * dp, paint)
        paint.shader = null
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 18f * dp
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText(t("RESET PROGRESS?"), box.centerX(), box.top + 40f * dp, textPaint)
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = 11f * dp
        textPaint.color = 0xCCDDE4EF.toInt()
        canvas.drawText(t("This cannot be undone."), box.centerX(), box.top + 68f * dp, textPaint)
        val buttonTop = box.bottom - 58f * dp
        cancelButton.set(box.left + 16f * dp, buttonTop, box.centerX() - 5f * dp, box.bottom - 16f * dp)
        confirmButton.set(box.centerX() + 5f * dp, buttonTop, box.right - 16f * dp, box.bottom - 16f * dp)
        CyberShapeRenderer.drawCyberChamferRect(canvas, cancelButton, 7f * dp, 5f * dp, paint)
        CyberShapeRenderer.drawCyberChamferRect(canvas, confirmButton, 7f * dp, 5f * dp, paint)
        textPaint.textSize = 10f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("CANCEL"), cancelButton.centerX(), cancelButton.centerY() + 3f * dp, textPaint)
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText(t("RESET"), confirmButton.centerX(), confirmButton.centerY() + 3f * dp, textPaint)
    }

    fun drawSliderRow(
        canvas: Canvas,
        rect: RectF,
        sliderRect: RectF,
        title: String,
        value: Int,
        accent: Int,
        iconKey: String,
        active: Boolean,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        drawAudioIconAsset: (Canvas, RectF, String, Boolean, Boolean) -> Unit
    ) {
        val iconRect = RectF(rect.left + 14f * dp, rect.centerY() - 15f * dp, rect.left + 44f * dp, rect.centerY() + 15f * dp)
        drawAudioIconAsset(canvas, iconRect, iconKey, false, active)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = (if (compact) 10f else 13f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(title, rect.width() * 0.36f), rect.left + 62f * dp, rect.centerY() + 5f * dp, textPaint)
        drawSlider(canvas, sliderRect, value, accent, active, paint, dp)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = (if (compact) 11f else 14f) * dp
        textPaint.color = accent
        canvas.drawText("$value%", rect.right - 14f * dp, rect.centerY() + 5f * dp, textPaint)
    }

    fun drawToggleRow(
        canvas: Canvas,
        rect: RectF,
        title: String,
        subtitle: String,
        enabled: Boolean,
        accent: Int,
        active: Boolean,
        glyph: Int,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        val iconRect = RectF(rect.left + 14f * dp, rect.centerY() - 15f * dp, rect.left + 44f * dp, rect.centerY() + 15f * dp)
        drawGlyph(canvas, iconRect, accent, active, glyph, paint, dp)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = (if (compact) 10f else 13f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(title, rect.width() * 0.58f), rect.left + 62f * dp, rect.top + 22f * dp, textPaint)
        textPaint.textSize = (if (compact) 8f else 10f) * dp
        textPaint.color = 0xAAB5C0D0.toInt()
        canvas.drawText(fitText(subtitle, rect.width() * 0.58f), rect.left + 62f * dp, rect.top + 39f * dp, textPaint)
        val toggleRect = RectF(rect.right - 48f * dp, rect.centerY() - 11f * dp, rect.right - 14f * dp, rect.centerY() + 11f * dp)
        drawToggle(canvas, toggleRect, enabled, accent, active, paint, dp)
    }

    fun drawNavRow(
        canvas: Canvas,
        rect: RectF,
        title: String,
        subtitle: String,
        accent: Int,
        glyph: Int,
        showFrame: Boolean,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        if (showFrame) drawCardFrame(canvas, rect.left, rect.right, rect.top, rect.bottom, paint, dp)
        drawGlyph(canvas, RectF(rect.left + 14f * dp, rect.centerY() - 15f * dp, rect.left + 44f * dp, rect.centerY() + 15f * dp), accent, false, glyph, paint, dp)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = (if (compact) 10f else 13f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(title, rect.width() * 0.56f), rect.left + 62f * dp, rect.top + 22f * dp, textPaint)
        if (subtitle.isNotBlank()) {
            textPaint.textSize = (if (compact) 8f else 10f) * dp
            textPaint.color = 0xAAB5C0D0.toInt()
            canvas.drawText(fitText(subtitle, rect.width() * 0.56f), rect.left + 62f * dp, rect.top + 40f * dp, textPaint)
        }
        drawChevron(canvas, rect.right - 24f * dp, rect.centerY(), withAlpha(accent, 220), paint, dp)
    }

    fun drawResetRow(
        canvas: Canvas,
        rect: RectF,
        compact: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        t: (String) -> String
    ) {
        drawCardFrame(canvas, rect.left, rect.right, rect.top, rect.bottom, paint, dp)
        drawGlyph(canvas, RectF(rect.left + 14f * dp, rect.centerY() - 15f * dp, rect.left + 44f * dp, rect.centerY() + 15f * dp), 0xFFFF4D8D.toInt(), false, 5, paint, dp)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = (if (compact) 10f else 13f) * dp
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText(t("RESET PROGRESS"), rect.left + 62f * dp, rect.top + 23f * dp, textPaint)
        textPaint.textSize = (if (compact) 8f else 10f) * dp
        textPaint.color = 0xAAB5C0D0.toInt()
        canvas.drawText(t("Clears gameplay only"), rect.left + 62f * dp, rect.top + 41f * dp, textPaint)
        drawChevron(canvas, rect.right - 24f * dp, rect.centerY(), 0xFFFF4D8D.toInt(), paint, dp)
    }

    fun drawSettingsScreen(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        safeTop24: Float,
        safeTop112: Float,
        safeTop132: Float,
        safeCenterX: Float,
        pageContentLeft: Float,
        pageContentRight: Float,
        settingsViewportTop: Float,
        settingsViewportBottom: Float,
        compact: Boolean,
        settingsHeaderGearButton: RectF,
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
        drawBrandTitle: (Canvas, Float, Float) -> Unit,
        drawUiButtonFrame: (Canvas, RectF, Boolean, Int, Float) -> Unit,
        drawAudioIconAsset: (Canvas, RectF, String, Boolean, Boolean) -> Unit
    ) {
        drawBackdrop(canvas, viewWidth, viewHeight, safeCenterX, pageContentLeft, pageContentRight, settingsViewportTop, settingsViewportBottom, paint, dp)
        val left = pageContentLeft
        val right = pageContentRight
        drawBrandTitle(canvas, left, safeTop24)
        HomeUiRenderer.drawTopActionCard(canvas, settingsHeaderGearButton, activeSettingsButton == SettingsButton.HEADER_GEAR, 1f, paint, dp)
        drawGearButton(canvas, settingsHeaderGearButton, activeSettingsButton == SettingsButton.HEADER_GEAR, paint, dp)

        textPaint.shader = null
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (compact) 24f else 30f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("SETTINGS").uppercase(), safeCenterX, safeTop112, textPaint)
        drawDivider(canvas, left, right, safeTop132, safeCenterX, paint, dp)

        canvas.save()
        canvas.clipRect(0f, settingsViewportTop, viewWidth, settingsViewportBottom)
        drawSectionLabel(canvas, t("AUDIO").uppercase(), left, settingsMasterButton.top - 24f * dp, compact, 0xFF45F2FF.toInt(), dp)
        drawCardFrame(canvas, left, right, settingsMasterButton.top - 7f * dp, settingsHapticToggle.bottom + 7f * dp, paint, dp)
        drawSliderRow(canvas, settingsMasterButton, settingsMasterSlider, t("MASTER VOLUME"), settingsMasterVolume, 0xFF45F2FF.toInt(), "ui_sound", activeSettingsButton == SettingsButton.MASTER_VOLUME, compact, paint, dp, fitText, drawAudioIconAsset)
        drawSliderRow(canvas, settingsMusicButton, settingsMusicSlider, t("MUSIC VOLUME"), settingsMusicVolume, 0xFF45F2FF.toInt(), "ui_music", activeSettingsButton == SettingsButton.MUSIC_VOLUME, compact, paint, dp, fitText, drawAudioIconAsset)
        drawSliderRow(canvas, settingsSfxButton, settingsSfxSlider, t("SOUND EFFECTS"), settingsSfxVolume, 0xFF45F2FF.toInt(), "ui_sound", activeSettingsButton == SettingsButton.SFX_VOLUME, compact, paint, dp, fitText, drawAudioIconAsset)
        drawToggleRow(canvas, settingsHapticToggle, t("HAPTIC FEEDBACK"), t("Vibration on actions"), settingsHapticEnabled, 0xFF45F2FF.toInt(), activeSettingsButton == SettingsButton.HAPTIC, 0, compact, paint, dp, fitText)

        drawSectionLabel(canvas, t("GAMEPLAY").uppercase(), left, settingsShakeToggle.top - 12f * dp, compact, 0xFF45F2FF.toInt(), dp)
        drawCardFrame(canvas, left, right, settingsShakeToggle.top - 7f * dp, settingsPerformanceToggle.bottom + 7f * dp, paint, dp)
        drawToggleRow(canvas, settingsShakeToggle, t("SCREEN SHAKE"), t("Shake the screen on impact"), settingsScreenShake, 0xFF45F2FF.toInt(), activeSettingsButton == SettingsButton.SCREEN_SHAKE, 1, compact, paint, dp, fitText)
        drawToggleRow(canvas, settingsPerformanceToggle, t("PERFORMANCE MODE"), t("Reduce effects for smoother gameplay"), settingsPerformanceMode, 0xFF45F2FF.toInt(), activeSettingsButton == SettingsButton.PERFORMANCE, 2, compact, paint, dp, fitText)

        drawSectionLabel(canvas, t("LANGUAGE").uppercase(), left, settingsLanguageButton.top - 12f * dp, compact, 0xFF45F2FF.toInt(), dp)
        drawNavRow(canvas, settingsLanguageButton, t("LANGUAGE"), selectedLanguageLabel, 0xFF45F2FF.toInt(), 0, true, compact, paint, dp, fitText)

        drawSectionLabel(canvas, t("ACCOUNT & CLOUD").uppercase(), left, settingsAccountButton.top - 12f * dp, compact, 0xFF45F2FF.toInt(), dp)
        drawNavRow(canvas, settingsAccountButton, t("ACCOUNT"), accountStatusLabel, 0xFF45F2FF.toInt(), 1, true, compact, paint, dp, fitText)

        drawSectionLabel(canvas, t("INFO & LEGAL").uppercase(), left, settingsPrivacyButton.top - 12f * dp, compact, 0xFF45F2FF.toInt(), dp)
        drawCardFrame(canvas, left, right, settingsPrivacyButton.top, settingsAboutButton.bottom, paint, dp)
        drawNavRow(canvas, settingsPrivacyButton, t("PRIVACY POLICY"), "", 0xFF45F2FF.toInt(), 2, false, compact, paint, dp, fitText)
        drawNavRow(canvas, settingsTermsButton, t("TERMS OF SERVICE"), "", 0xFF45F2FF.toInt(), 3, false, compact, paint, dp, fitText)
        drawNavRow(canvas, settingsDataDeletionButton, t("DATA DELETION"), t("Erase all local app data"), 0xFFFFCF4A.toInt(), 5, false, compact, paint, dp, fitText)
        drawNavRow(canvas, settingsAboutButton, t("ABOUT MOONSOL STUDIOS"), "Kavvoro v$versionName", 0xFF45F2FF.toInt(), 4, false, compact, paint, dp, fitText)

        drawResetRow(canvas, settingsResetButton, compact, paint, dp, fitText, t)
        drawBackHomeButton(canvas, settingsBackButton, activeSettingsButton == SettingsButton.BACK, t("BACK TO HOME").uppercase(), compact, paint, dp)
        canvas.restore()

        if (settingsResetConfirm) {
            drawResetDialog(canvas, viewWidth, viewHeight, pageContentLeft, pageContentRight - pageContentLeft, safeCenterX, settingsResetCancelButton, settingsResetConfirmButton, paint, dp, t)
        }
    }

}
