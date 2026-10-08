package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import kotlin.math.min

/**
 * A compact icon family tuned to the neon-cosmic language of the Home screen.
 * Each symbol shares the same glass tile, luminous cyan-to-magenta rim, and
 * restrained glow so the settings stay readable instead of looking like HUD clutter.
 */
object SettingsIconRenderer {

    enum class SettingsIconId {
        MASTER_VOLUME,
        MUSIC_VOLUME,
        SFX_VOLUME,
        HAPTIC,
        SCREEN_SHAKE,
        PERFORMANCE,
        LANGUAGE,
        ACCOUNT,
        PRIVACY,
        TERMS,
        DATA_DELETION,
        ABOUT,
        RESET
    }

    private val tileRect = RectF()
    private val glyphRect = RectF()
    private val glyphPath = Path()
    private val equalizerBars = arrayOf(
        floatArrayOf(-11f, -4f, -7f, 7f),
        floatArrayOf(-5f, -10f, -1f, 10f),
        floatArrayOf(1f, -7f, 5f, 7f),
        floatArrayOf(7f, -2f, 11f, 9f)
    )

    fun drawSettingsIcon(
        canvas: Canvas,
        rect: RectF,
        id: SettingsIconId,
        accent: Int,
        active: Boolean,
        paint: Paint,
        dp: Float
    ) {
        if (rect.isEmpty || dp <= 0f) return

        val size = min(rect.width(), rect.height())
        if (size <= 0f) return

        val cx = rect.centerX()
        val cy = rect.centerY()
        val tint = iconAccent(id, accent)

        drawGlow(canvas, cx, cy, size, tint, active, paint)
        drawTile(canvas, rect, size, tint, active, paint)

        val saveCount = canvas.save()
        try {
            val scale = size / ICON_UNITS
            canvas.translate(cx, cy)
            canvas.scale(scale, scale)
            paint.reset()
            paint.isAntiAlias = true
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND

            // Glyphs use a centered 48-unit view box. Keeping the drawings inside
            // that box gives every symbol the same optical margin on every device.
            when (id) {
                SettingsIconId.MASTER_VOLUME -> drawSpeaker(canvas, 0f, 0f, tint, paint)
                SettingsIconId.MUSIC_VOLUME -> drawMusicNotes(canvas, 0f, 0f, tint, paint)
                SettingsIconId.SFX_VOLUME -> drawEqualizer(canvas, 0f, 0f, tint, paint)
                SettingsIconId.HAPTIC -> drawHaptics(canvas, 0f, 0f, tint, paint)
                SettingsIconId.SCREEN_SHAKE -> drawScreenShake(canvas, 0f, 0f, tint, paint)
                SettingsIconId.PERFORMANCE -> drawPerformance(canvas, 0f, 0f, tint, paint)
                SettingsIconId.LANGUAGE -> drawLanguage(canvas, 0f, 0f, tint, paint)
                SettingsIconId.ACCOUNT -> drawAccount(canvas, 0f, 0f, tint, paint)
                SettingsIconId.PRIVACY -> drawPrivacy(canvas, 0f, 0f, tint, paint)
                SettingsIconId.TERMS -> drawTerms(canvas, 0f, 0f, tint, paint)
                SettingsIconId.DATA_DELETION -> drawDataDeletion(canvas, 0f, 0f, tint, paint)
                SettingsIconId.ABOUT -> drawAbout(canvas, 0f, 0f, tint, paint)
                SettingsIconId.RESET -> drawReset(canvas, 0f, 0f, tint, paint)
            }
        } finally {
            canvas.restoreToCount(saveCount)
            paint.reset()
            paint.isAntiAlias = true
        }
    }

    private fun drawGlow(canvas: Canvas, cx: Float, cy: Float, size: Float, tint: Int, active: Boolean, paint: Paint) {
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(tint, if (active) 23 else 12)
        canvas.drawCircle(cx, cy, size * 0.72f, paint)
        paint.color = withAlpha(tint, if (active) 15 else 7)
        canvas.drawCircle(cx, cy, size * 0.56f, paint)
    }

    private fun drawTile(canvas: Canvas, rect: RectF, size: Float, tint: Int, active: Boolean, paint: Paint) {
        val inset = size * 0.035f
        tileRect.set(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        val radius = size * 0.24f

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = 0xF20D1832.toInt()
        canvas.drawRoundRect(tileRect, radius, radius, paint)
        paint.color = withAlpha(tint, if (active) 31 else 17)
        canvas.drawRoundRect(tileRect, radius, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.7f else 1.15f
        paint.color = withAlpha(KavvoroPalette.cyan, if (active) 235 else 150)
        canvas.drawRoundRect(tileRect, radius, radius, paint)
        paint.strokeWidth = if (active) 1.2f else 0.75f
        paint.color = withAlpha(KavvoroPalette.pink, if (active) 225 else 130)
        canvas.drawLine(tileRect.centerX(), tileRect.bottom - 1f, tileRect.right - radius * 0.65f, tileRect.bottom - 1f, paint)

        paint.strokeWidth = 0.8f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (active) 105 else 55)
        val highlightInset = size * 0.22f
        canvas.drawLine(
            tileRect.left + highlightInset,
            tileRect.top + 1.1f,
            tileRect.right - highlightInset,
            tileRect.top + 1.1f,
            paint
        )
    }

    private fun drawSpeaker(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx - 14f, cy - 5f)
        glyphPath.lineTo(cx - 9f, cy - 5f)
        glyphPath.lineTo(cx - 1f, cy - 11f)
        glyphPath.lineTo(cx - 1f, cy + 11f)
        glyphPath.lineTo(cx - 9f, cy + 5f)
        glyphPath.lineTo(cx - 14f, cy + 5f)
        glyphPath.close()

        paint.style = Paint.Style.FILL
        paint.color = tint
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.25f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 205)
        canvas.drawPath(glyphPath, paint)
        canvas.drawLine(cx - 8f, cy - 4.8f, cx - 1f, cy - 10f, paint)

        paint.strokeWidth = 2f
        paint.color = tint
        glyphPath.rewind()
        glyphPath.moveTo(cx + 3f, cy - 6f)
        glyphPath.cubicTo(cx + 9f, cy - 3f, cx + 9f, cy + 3f, cx + 3f, cy + 6f)
        canvas.drawPath(glyphPath, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx + 6f, cy - 10f)
        glyphPath.cubicTo(cx + 15f, cy - 5f, cx + 15f, cy + 5f, cx + 6f, cy + 10f)
        paint.color = withAlpha(tint, 190)
        canvas.drawPath(glyphPath, paint)
    }

    private fun drawMusicNotes(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = tint
        glyphRect.set(cx - 13f, cy + 2f, cx - 5f, cy + 8f)
        canvas.drawOval(glyphRect, paint)
        glyphRect.set(cx + 2f, cy, cx + 10f, cy + 6f)
        canvas.drawOval(glyphRect, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.3f
        paint.color = tint
        canvas.drawLine(cx - 6f, cy + 5f, cx - 6f, cy - 10f, paint)
        canvas.drawLine(cx + 7f, cy + 3f, cx + 7f, cy - 12f, paint)
        canvas.drawLine(cx - 6f, cy - 10f, cx + 7f, cy - 13f, paint)
        canvas.drawLine(cx + 7f, cy - 12f, cx + 12f, cy - 7f, paint)

        paint.strokeWidth = 1.35f
        paint.color = 0xFFF5FCFF.toInt()
        canvas.drawLine(cx - 5f, cy - 9f, cx + 6f, cy - 12f, paint)
    }

    private fun drawEqualizer(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        equalizerBars.forEachIndexed { index, bar ->
            glyphRect.set(cx + bar[0] - 1f, cy + bar[1], cx + bar[2] - 1f, cy + bar[3])
            paint.color = when (index) {
                1 -> 0xFFF6FDFF.toInt()
                2 -> withAlpha(KavvoroPalette.pink, 230)
                else -> withAlpha(tint, 235)
            }
            canvas.drawRoundRect(glyphRect, 1.7f, 1.7f, paint)
        }
    }

    private fun drawHaptics(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFFEFFBFF.toInt()
        glyphRect.set(cx - 6f, cy - 12f, cx + 6f, cy + 12f)
        canvas.drawRoundRect(glyphRect, 2.7f, 2.7f, paint)

        paint.style = Paint.Style.FILL
        paint.color = tint
        glyphRect.set(cx - 1.3f, cy + 7f, cx + 1.3f, cy + 9.6f)
        canvas.drawRoundRect(glyphRect, 1.2f, 1.2f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        glyphPath.rewind()
        glyphPath.moveTo(cx - 9f, cy - 6f)
        glyphPath.cubicTo(cx - 13f, cy - 3f, cx - 13f, cy + 3f, cx - 9f, cy + 6f)
        canvas.drawPath(glyphPath, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx + 9f, cy - 6f)
        glyphPath.cubicTo(cx + 13f, cy - 3f, cx + 13f, cy + 3f, cx + 9f, cy + 6f)
        canvas.drawPath(glyphPath, paint)
        paint.color = withAlpha(tint, 170)
        glyphPath.rewind()
        glyphPath.moveTo(cx - 13f, cy - 10f)
        glyphPath.cubicTo(cx - 18f, cy - 6f, cx - 18f, cy + 6f, cx - 13f, cy + 10f)
        canvas.drawPath(glyphPath, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx + 13f, cy - 10f)
        glyphPath.cubicTo(cx + 18f, cy - 6f, cx + 18f, cy + 6f, cx + 13f, cy + 10f)
        canvas.drawPath(glyphPath, paint)
    }

    private fun drawScreenShake(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFFF4FCFF.toInt()
        glyphRect.set(cx - 13f, cy - 9f, cx + 13f, cy + 9f)
        canvas.drawRoundRect(glyphRect, 2.5f, 2.5f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 8f, cy + 1f)
        glyphPath.lineTo(cx - 3f, cy - 3f)
        glyphPath.lineTo(cx + 1f, cy + 3f)
        glyphPath.lineTo(cx + 8f, cy - 4f)
        paint.strokeWidth = 2.4f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = tint
        canvas.drawPath(glyphPath, paint)

        paint.strokeWidth = 1.5f
        paint.color = withAlpha(KavvoroPalette.pink, 220)
        canvas.drawLine(cx - 17f, cy, cx - 15f, cy, paint)
        canvas.drawLine(cx + 15f, cy, cx + 17f, cy, paint)
    }

    private fun drawPerformance(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        glyphPath.rewind()
        glyphPath.moveTo(cx, cy - 15f)
        glyphPath.cubicTo(cx + 7f, cy - 9f, cx + 9f, cy - 1f, cx + 6f, cy + 7f)
        glyphPath.lineTo(cx + 3f, cy + 11f)
        glyphPath.lineTo(cx - 3f, cy + 11f)
        glyphPath.lineTo(cx - 6f, cy + 7f)
        glyphPath.cubicTo(cx - 9f, cy - 1f, cx - 7f, cy - 9f, cx, cy - 15f)
        glyphPath.close()
        paint.style = Paint.Style.FILL
        paint.color = tint
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f
        paint.color = 0xFFF2FBFF.toInt()
        canvas.drawPath(glyphPath, paint)

        paint.style = Paint.Style.FILL
        paint.color = 0xFF11172C.toInt()
        canvas.drawCircle(cx, cy - 3f, 3f, paint)
        paint.color = 0xFFF5FCFF.toInt()
        canvas.drawCircle(cx, cy - 3f, 1.2f, paint)
        paint.color = KavvoroPalette.pink
        glyphPath.rewind()
        glyphPath.moveTo(cx - 2f, cy + 11f)
        glyphPath.lineTo(cx, cy + 16f)
        glyphPath.lineTo(cx + 2f, cy + 11f)
        glyphPath.close()
        canvas.drawPath(glyphPath, paint)
    }

    private fun drawLanguage(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.9f
        paint.color = 0xFFF4FCFF.toInt()
        canvas.drawCircle(cx, cy, 11f, paint)

        glyphRect.set(cx - 5f, cy - 11f, cx + 5f, cy + 11f)
        paint.color = tint
        canvas.drawOval(glyphRect, paint)
        canvas.drawLine(cx - 10f, cy, cx + 10f, cy, paint)
        glyphRect.set(cx - 10f, cy - 5f, cx + 10f, cy + 5f)
        paint.strokeWidth = 1.25f
        paint.color = withAlpha(tint, 190)
        canvas.drawOval(glyphRect, paint)
    }

    private fun drawAccount(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = tint
        canvas.drawCircle(cx, cy - 6f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = 0xFFF6FBFF.toInt()
        canvas.drawCircle(cx, cy - 6f, 4f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 11f, cy + 11f)
        glyphPath.cubicTo(cx - 10f, cy + 5f, cx - 5f, cy + 2f, cx, cy + 2f)
        glyphPath.cubicTo(cx + 5f, cy + 2f, cx + 10f, cy + 5f, cx + 11f, cy + 11f)
        glyphPath.quadTo(cx + 11f, cy + 14f, cx + 8f, cy + 14f)
        glyphPath.lineTo(cx - 8f, cy + 14f)
        glyphPath.quadTo(cx - 11f, cy + 14f, cx - 11f, cy + 11f)
        glyphPath.close()
        paint.style = Paint.Style.FILL
        paint.color = tint
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = 0xFFF6FBFF.toInt()
        canvas.drawPath(glyphPath, paint)
    }

    private fun drawPrivacy(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx, cy - 14f)
        glyphPath.lineTo(cx + 11f, cy - 10f)
        glyphPath.lineTo(cx + 10f, cy + 1f)
        glyphPath.cubicTo(cx + 9f, cy + 7f, cx + 4f, cy + 12f, cx, cy + 14f)
        glyphPath.cubicTo(cx - 4f, cy + 12f, cx - 9f, cy + 7f, cx - 10f, cy + 1f)
        glyphPath.lineTo(cx - 11f, cy - 10f)
        glyphPath.close()

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(tint, 48)
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f
        paint.color = 0xFFF4FCFF.toInt()
        canvas.drawPath(glyphPath, paint)

        paint.strokeWidth = 1.7f
        paint.color = KavvoroPalette.cyan
        glyphRect.set(cx - 4f, cy - 2f, cx + 4f, cy + 6f)
        canvas.drawRoundRect(glyphRect, 1.3f, 1.3f, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx - 2.5f, cy - 2f)
        glyphPath.lineTo(cx - 2.5f, cy - 5f)
        glyphPath.cubicTo(cx - 2.5f, cy - 9f, cx + 2.5f, cy - 9f, cx + 2.5f, cy - 5f)
        glyphPath.lineTo(cx + 2.5f, cy - 2f)
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy + 1f, 1.1f, paint)
    }

    private fun drawTerms(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx - 8f, cy - 12f)
        glyphPath.lineTo(cx + 3f, cy - 12f)
        glyphPath.lineTo(cx + 9f, cy - 6f)
        glyphPath.lineTo(cx + 9f, cy + 12f)
        glyphPath.lineTo(cx - 9f, cy + 12f)
        glyphPath.close()

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(tint, 45)
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.8f
        paint.color = 0xFFF5FCFF.toInt()
        canvas.drawPath(glyphPath, paint)
        canvas.drawLine(cx + 3f, cy - 12f, cx + 3f, cy - 6f, paint)
        canvas.drawLine(cx + 3f, cy - 6f, cx + 9f, cy - 6f, paint)

        paint.strokeWidth = 1.8f
        paint.color = tint
        canvas.drawLine(cx - 4f, cy - 3f, cx + 2f, cy - 3f, paint)
        canvas.drawLine(cx - 4f, cy + 1f, cx + 1f, cy + 1f, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx - 3f, cy + 6f)
        glyphPath.lineTo(cx - 0.5f, cy + 8f)
        glyphPath.lineTo(cx + 4f, cy + 3f)
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(glyphPath, paint)
    }

    private fun drawDataDeletion(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.9f
        paint.color = 0xFFFFF8E8.toInt()
        canvas.drawLine(cx - 10f, cy - 9f, cx + 10f, cy - 9f, paint)
        canvas.drawLine(cx - 4f, cy - 13f, cx + 4f, cy - 13f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 8f, cy - 6f)
        glyphPath.lineTo(cx - 6f, cy + 11f)
        glyphPath.lineTo(cx + 6f, cy + 11f)
        glyphPath.lineTo(cx + 8f, cy - 6f)
        glyphPath.close()
        paint.color = tint
        paint.style = Paint.Style.FILL
        canvas.drawPath(glyphPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.color = 0xFFFFF8E8.toInt()
        glyphPath.rewind()
        glyphPath.moveTo(cx - 8f, cy - 6f)
        glyphPath.lineTo(cx + 8f, cy - 6f)
        glyphPath.moveTo(cx - 2.5f, cy - 2f)
        glyphPath.lineTo(cx - 2f, cy + 6f)
        glyphPath.moveTo(cx + 2.5f, cy - 2f)
        glyphPath.lineTo(cx + 2f, cy + 6f)
        canvas.drawPath(glyphPath, paint)

    }

    private fun drawAbout(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.1f
        paint.color = tint
        canvas.drawCircle(cx, cy, 12f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.1f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xFFF7FDFF.toInt()
        canvas.drawCircle(cx, cy - 4f, 1.35f, paint)
        canvas.drawLine(cx, cy, cx, cy + 7f, paint)
    }

    private fun drawReset(canvas: Canvas, cx: Float, cy: Float, tint: Int, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f
        paint.color = 0xFFF7FDFF.toInt()
        glyphRect.set(cx - 12f, cy - 12f, cx + 12f, cy + 12f)
        canvas.drawArc(glyphRect, 42f, 285f, false, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx + 14f, cy - 4f)
        glyphPath.lineTo(cx + 7f, cy - 10f)
        glyphPath.lineTo(cx + 12f, cy - 12f)
        glyphPath.close()
        paint.style = Paint.Style.FILL
        paint.color = tint
        canvas.drawPath(glyphPath, paint)
    }

    private fun iconAccent(id: SettingsIconId, fallback: Int): Int = when (id) {
        SettingsIconId.MUSIC_VOLUME,
        SettingsIconId.ABOUT,
        SettingsIconId.RESET -> KavvoroPalette.pink
        SettingsIconId.SFX_VOLUME,
        SettingsIconId.SCREEN_SHAKE,
        SettingsIconId.TERMS -> KavvoroPalette.blue
        SettingsIconId.ACCOUNT -> KavvoroPalette.purple
        SettingsIconId.DATA_DELETION -> KavvoroPalette.gold
        else -> fallback
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private const val ICON_UNITS = 48f
}
