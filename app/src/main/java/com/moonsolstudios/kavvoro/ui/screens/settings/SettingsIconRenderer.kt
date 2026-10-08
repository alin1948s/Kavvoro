package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import kotlin.math.max
import kotlin.math.min

/**
 * Settings icon family in the glossy, high-contrast visual language of Kavvoro Home.
 * The symbols are drawn in a 48-unit view box so they stay crisp at every screen size.
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
        floatArrayOf(-13f, -3f, -7f, 10f),
        floatArrayOf(-6f, -10f, 0f, 10f),
        floatArrayOf(1f, -7f, 7f, 10f),
        floatArrayOf(8f, -1f, 14f, 10f)
    )
    private val equalizerColors = intArrayOf(
        KavvoroPalette.cyan,
        KavvoroPalette.blue,
        KavvoroPalette.pink,
        KavvoroPalette.gold
    )
    private val chipTerminals = arrayOf(
        floatArrayOf(-15f, -9f, -12f, -5f),
        floatArrayOf(-15f, -2f, -12f, 2f),
        floatArrayOf(-15f, 5f, -12f, 9f),
        floatArrayOf(12f, -9f, 15f, -5f),
        floatArrayOf(12f, -2f, 15f, 2f),
        floatArrayOf(12f, 5f, 15f, 9f)
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

            when (id) {
                SettingsIconId.MASTER_VOLUME -> drawSpeaker(canvas, 0f, 0f, paint)
                SettingsIconId.MUSIC_VOLUME -> drawMusicNotes(canvas, 0f, 0f, paint)
                SettingsIconId.SFX_VOLUME -> drawEqualizer(canvas, 0f, 0f, paint)
                SettingsIconId.HAPTIC -> drawHaptics(canvas, 0f, 0f, paint)
                SettingsIconId.SCREEN_SHAKE -> drawScreenShake(canvas, 0f, 0f, paint)
                SettingsIconId.PERFORMANCE -> drawPerformance(canvas, 0f, 0f, paint)
                SettingsIconId.LANGUAGE -> drawLanguage(canvas, 0f, 0f, paint)
                SettingsIconId.ACCOUNT -> drawAccount(canvas, 0f, 0f, paint)
                SettingsIconId.PRIVACY -> drawPrivacy(canvas, 0f, 0f, paint)
                SettingsIconId.TERMS -> drawTerms(canvas, 0f, 0f, paint)
                SettingsIconId.DATA_DELETION -> drawDataDeletion(canvas, 0f, 0f, paint)
                SettingsIconId.ABOUT -> drawAbout(canvas, 0f, 0f, paint)
                SettingsIconId.RESET -> drawReset(canvas, 0f, 0f, paint)
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
        paint.color = withAlpha(tint, if (active) 27 else 15)
        canvas.drawCircle(cx, cy, size * 0.73f, paint)
        paint.color = withAlpha(tint, if (active) 16 else 9)
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
        paint.color = withAlpha(tint, if (active) 38 else 22)
        canvas.drawRoundRect(tileRect, radius, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.8f else 1.25f
        paint.color = withAlpha(KavvoroPalette.cyan, if (active) 245 else 170)
        canvas.drawRoundRect(tileRect, radius, radius, paint)
        paint.strokeWidth = if (active) 1.35f else 0.9f
        paint.color = withAlpha(KavvoroPalette.pink, if (active) 235 else 150)
        canvas.drawLine(tileRect.centerX(), tileRect.bottom - 1f, tileRect.right - radius * 0.58f, tileRect.bottom - 1f, paint)

        paint.strokeWidth = 0.9f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (active) 125 else 72)
        val highlightInset = size * 0.22f
        canvas.drawLine(
            tileRect.left + highlightInset,
            tileRect.top + 1.1f,
            tileRect.right - highlightInset,
            tileRect.top + 1.1f,
            paint
        )
    }

    private fun drawSpeaker(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx - 15f, cy - 5f)
        glyphPath.lineTo(cx - 9f, cy - 5f)
        glyphPath.lineTo(cx - 1f, cy - 11f)
        glyphPath.lineTo(cx - 1f, cy + 11f)
        glyphPath.lineTo(cx - 9f, cy + 5f)
        glyphPath.lineTo(cx - 15f, cy + 5f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.blue, KavvoroPalette.cyan, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx + 3f, cy - 6f)
        glyphPath.cubicTo(cx + 9f, cy - 3f, cx + 9f, cy + 3f, cx + 3f, cy + 6f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.cyan, 2.7f, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx + 7f, cy - 10f)
        glyphPath.cubicTo(cx + 16f, cy - 5f, cx + 16f, cy + 5f, cx + 7f, cy + 10f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.pink, 2.2f, paint, 0xFFFFD8F5.toInt())

        paint.style = Paint.Style.FILL
        paint.color = 0xFFE8FDFF.toInt()
        canvas.drawCircle(cx - 6f, cy - 2f, 1.35f, paint)
    }

    private fun drawMusicNotes(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx - 5.5f, cy + 4f)
        glyphPath.lineTo(cx - 5.5f, cy - 10f)
        glyphPath.lineTo(cx + 8f, cy - 13f)
        glyphPath.lineTo(cx + 8f, cy + 1f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.cyan, 2.8f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 5.5f, cy - 10f)
        glyphPath.lineTo(cx + 8f, cy - 13f)
        glyphPath.lineTo(cx + 12f, cy - 9f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.pink, 2.4f, paint, 0xFFFFE5F8.toInt())

        drawRaisedOval(canvas, cx - 13f, cy + 1f, cx - 3f, cy + 8f, KavvoroPalette.pink, paint)
        drawRaisedOval(canvas, cx + 1f, cy - 2f, cx + 11f, cy + 5f, KavvoroPalette.cyan, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f
        paint.color = 0xFFFFF4FC.toInt()
        canvas.drawLine(cx - 4.7f, cy - 8f, cx + 4.7f, cy - 10f, paint)
    }

    private fun drawEqualizer(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        equalizerBars.forEachIndexed { index, bar ->
            glyphRect.set(cx + bar[0], cy + bar[1], cx + bar[2], cy + bar[3])
            drawRaisedRoundRect(canvas, glyphRect, 2f, equalizerColors[index], paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.95f
            paint.color = withAlpha(0xFFFFFFFF.toInt(), 185)
            canvas.drawLine(glyphRect.left + 1.3f, glyphRect.top + 2f, glyphRect.right - 1.3f, glyphRect.top + 2f, paint)
        }
        glyphRect.set(cx - 15f, cy + 10f, cx + 15f, cy + 12f)
        drawRaisedRoundRect(canvas, glyphRect, 1f, 0xFF87F6FF.toInt(), paint)
    }

    private fun drawHaptics(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphRect.set(cx - 7f, cy - 13f, cx + 7f, cy + 13f)
        drawRaisedRoundRect(canvas, glyphRect, 3.5f, KavvoroPalette.cyan, paint)
        glyphRect.set(cx - 4.8f, cy - 9.5f, cx + 4.8f, cy + 8f)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF101D3C.toInt()
        canvas.drawRoundRect(glyphRect, 2f, 2f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 185)
        canvas.drawLine(cx - 3f, cy - 7f, cx + 3f, cy - 7f, paint)
        paint.style = Paint.Style.FILL
        paint.color = KavvoroPalette.gold
        canvas.drawCircle(cx, cy + 10f, 1.2f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 10f, cy - 6f)
        glyphPath.cubicTo(cx - 15f, cy - 3f, cx - 15f, cy + 3f, cx - 10f, cy + 6f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.pink, 2.1f, paint, 0xFFFFDDF7.toInt())
        glyphPath.rewind()
        glyphPath.moveTo(cx + 10f, cy - 6f)
        glyphPath.cubicTo(cx + 15f, cy - 3f, cx + 15f, cy + 3f, cx + 10f, cy + 6f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.pink, 2.1f, paint, 0xFFFFDDF7.toInt())
    }

    private fun drawScreenShake(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx - 12f, cy - 8f)
        glyphPath.lineTo(cx + 12f, cy - 8f)
        glyphPath.lineTo(cx + 12f, cy + 7f)
        glyphPath.lineTo(cx - 12f, cy + 7f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.blue, KavvoroPalette.cyan, paint)

        glyphRect.set(cx - 9f, cy - 5f, cx + 9f, cy + 4f)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF09152F.toInt()
        canvas.drawRoundRect(glyphRect, 1.3f, 1.3f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 7f, cy + 1f)
        glyphPath.lineTo(cx - 3f, cy - 2f)
        glyphPath.lineTo(cx, cy + 2f)
        glyphPath.lineTo(cx + 5f, cy - 3f)
        glyphPath.lineTo(cx + 7f, cy - 1f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.gold, 2.1f, paint, 0xFFFFF1B8.toInt())

        glyphPath.rewind()
        glyphPath.moveTo(cx - 16f, cy - 3f)
        glyphPath.lineTo(cx - 13f, cy - 1f)
        glyphPath.lineTo(cx - 16f, cy + 2f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.pink, 1.8f, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx + 16f, cy - 3f)
        glyphPath.lineTo(cx + 13f, cy - 1f)
        glyphPath.lineTo(cx + 16f, cy + 2f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.pink, 1.8f, paint)
    }

    private fun drawPerformance(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        chipTerminals.forEach { t ->
            glyphRect.set(cx + t[0], cy + t[1], cx + t[2], cy + t[3])
            drawRaisedRoundRect(canvas, glyphRect, 1.4f, KavvoroPalette.cyan, paint)
        }

        glyphRect.set(cx - 12f, cy - 12f, cx + 12f, cy + 12f)
        drawRaisedRoundRect(canvas, glyphRect, 4f, KavvoroPalette.blue, paint)
        glyphRect.set(cx - 8.5f, cy - 8.5f, cx + 8.5f, cy + 8.5f)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF101D3E.toInt()
        canvas.drawRoundRect(glyphRect, 2.8f, 2.8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f
        paint.color = withAlpha(KavvoroPalette.cyan, 190)
        canvas.drawRoundRect(glyphRect, 2.8f, 2.8f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx + 1f, cy - 8f)
        glyphPath.lineTo(cx - 5f, cy + 1f)
        glyphPath.lineTo(cx - 1f, cy + 1f)
        glyphPath.lineTo(cx - 3f, cy + 8f)
        glyphPath.lineTo(cx + 5f, cy - 2f)
        glyphPath.lineTo(cx + 1f, cy - 2f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.gold, 0xFFFFF4BD.toInt(), paint, strokeWidth = 0.8f)
        glyphPath.rewind()
        glyphPath.moveTo(cx - 6f, cy - 9f)
        glyphPath.lineTo(cx - 2f, cy - 9f)
        drawRaisedStroke(canvas, glyphPath, 0xFFFFF5D0.toInt(), 0.9f, paint)
    }

    private fun drawLanguage(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = 0xBD061326.toInt()
        canvas.drawCircle(cx, cy + 1.2f, 12.2f, paint)
        paint.color = KavvoroPalette.blue
        canvas.drawCircle(cx, cy, 11.5f, paint)
        paint.color = withAlpha(KavvoroPalette.cyan, 105)
        glyphRect.set(cx - 9.8f, cy - 9f, cx + 9.8f, cy + 9f)
        canvas.drawOval(glyphRect, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.45f
        paint.color = 0xFFE9FDFF.toInt()
        canvas.drawCircle(cx, cy, 11.2f, paint)
        glyphRect.set(cx - 5.2f, cy - 11f, cx + 5.2f, cy + 11f)
        canvas.drawOval(glyphRect, paint)
        glyphRect.set(cx - 10f, cy - 5.2f, cx + 10f, cy + 5.2f)
        paint.strokeWidth = 1.15f
        paint.color = KavvoroPalette.pink
        canvas.drawOval(glyphRect, paint)
        paint.strokeWidth = 1.05f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 210)
        canvas.drawLine(cx - 8f, cy, cx + 8f, cy, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx - 4f, cy - 6f, 1.2f, paint)
    }

    private fun drawAccount(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        drawRaisedCircle(canvas, cx, cy - 6.2f, 5.1f, KavvoroPalette.purple, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFE9FF.toInt()
        canvas.drawCircle(cx - 1.7f, cy - 8.2f, 1.3f, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 12f, cy + 10f)
        glyphPath.cubicTo(cx - 11f, cy + 3f, cx - 6f, cy + 1f, cx, cy + 1f)
        glyphPath.cubicTo(cx + 6f, cy + 1f, cx + 11f, cy + 3f, cx + 12f, cy + 10f)
        glyphPath.quadTo(cx + 12f, cy + 13f, cx + 8.5f, cy + 13f)
        glyphPath.lineTo(cx - 8.5f, cy + 13f)
        glyphPath.quadTo(cx - 12f, cy + 13f, cx - 12f, cy + 10f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.purple, KavvoroPalette.pink, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx - 7f, cy + 5f)
        glyphPath.cubicTo(cx - 4f, cy + 2.5f, cx + 4f, cy + 2.5f, cx + 7f, cy + 5f)
        drawRaisedStroke(canvas, glyphPath, 0xFFE9C9FF.toInt(), 1.15f, paint)
    }

    private fun drawPrivacy(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx, cy - 14f)
        glyphPath.lineTo(cx + 12f, cy - 9.5f)
        glyphPath.lineTo(cx + 10.5f, cy + 1f)
        glyphPath.cubicTo(cx + 9f, cy + 7f, cx + 4f, cy + 12f, cx, cy + 14f)
        glyphPath.cubicTo(cx - 4f, cy + 12f, cx - 9f, cy + 7f, cx - 10.5f, cy + 1f)
        glyphPath.lineTo(cx - 12f, cy - 9.5f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.blue, KavvoroPalette.cyan, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 4f, cy - 1f)
        glyphPath.lineTo(cx - 4f, cy - 4f)
        glyphPath.cubicTo(cx - 4f, cy - 10f, cx + 4f, cy - 10f, cx + 4f, cy - 4f)
        glyphPath.lineTo(cx + 4f, cy - 1f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.gold, 2.4f, paint, 0xFFFFF4C8.toInt())
        glyphRect.set(cx - 6f, cy - 2f, cx + 6f, cy + 7f)
        drawRaisedRoundRect(canvas, glyphRect, 2f, KavvoroPalette.cyan, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF08203B.toInt()
        canvas.drawCircle(cx, cy + 1f, 1.2f, paint)
        glyphRect.set(cx - 0.8f, cy + 1f, cx + 0.8f, cy + 4.2f)
        canvas.drawRoundRect(glyphRect, 0.7f, 0.7f, paint)
    }

    private fun drawTerms(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx - 9f, cy - 13f)
        glyphPath.lineTo(cx + 3f, cy - 13f)
        glyphPath.lineTo(cx + 10f, cy - 6f)
        glyphPath.lineTo(cx + 10f, cy + 12f)
        glyphPath.quadTo(cx + 10f, cy + 14f, cx + 8f, cy + 14f)
        glyphPath.lineTo(cx - 8f, cy + 14f)
        glyphPath.quadTo(cx - 10f, cy + 14f, cx - 10f, cy + 12f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.blue, 0xFFE2FBFF.toInt(), paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx + 3f, cy - 13f)
        glyphPath.lineTo(cx + 3f, cy - 6f)
        glyphPath.lineTo(cx + 10f, cy - 6f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.pink, 0xFFFFE6F8.toInt(), paint, strokeWidth = 0.7f)

        glyphPath.rewind()
        glyphPath.moveTo(cx - 5f, cy - 2f)
        glyphPath.lineTo(cx + 4f, cy - 2f)
        glyphPath.moveTo(cx - 5f, cy + 2f)
        glyphPath.lineTo(cx + 2f, cy + 2f)
        drawRaisedStroke(canvas, glyphPath, 0xFFE9FAFF.toInt(), 1.4f, paint)
        glyphPath.rewind()
        glyphPath.moveTo(cx - 4f, cy + 7f)
        glyphPath.lineTo(cx - 1f, cy + 9.5f)
        glyphPath.lineTo(cx + 5f, cy + 4f)
        drawRaisedStroke(canvas, glyphPath, KavvoroPalette.gold, 2.1f, paint, 0xFFFFF5C7.toInt())
    }

    private fun drawDataDeletion(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphRect.set(cx - 9f, cy - 8f, cx + 9f, cy + 12f)
        drawRaisedRoundRect(canvas, glyphRect, 3f, KavvoroPalette.pink, paint)
        glyphRect.set(cx - 12f, cy - 12f, cx + 12f, cy - 8f)
        drawRaisedRoundRect(canvas, glyphRect, 2f, KavvoroPalette.gold, paint)
        glyphRect.set(cx - 5f, cy - 15f, cx + 5f, cy - 12f)
        drawRaisedRoundRect(canvas, glyphRect, 1.4f, 0xFFFFE6A1.toInt(), paint)

        paint.style = Paint.Style.FILL
        paint.color = 0xFF632A72.toInt()
        glyphRect.set(cx - 5.3f, cy - 3.5f, cx - 3.1f, cy + 7f)
        canvas.drawRoundRect(glyphRect, 1f, 1f, paint)
        glyphRect.set(cx - 1.1f, cy - 3.5f, cx + 1.1f, cy + 7f)
        canvas.drawRoundRect(glyphRect, 1f, 1f, paint)
        glyphRect.set(cx + 3.1f, cy - 3.5f, cx + 5.3f, cy + 7f)
        canvas.drawRoundRect(glyphRect, 1f, 1f, paint)
        paint.color = 0xFFFFE8FC.toInt()
        canvas.drawCircle(cx - 4.7f, cy - 5.8f, 0.7f, paint)
    }

    private fun drawAbout(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphRect.set(cx - 17f, cy - 10f, cx + 17f, cy + 10f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = withAlpha(KavvoroPalette.blue, 220)
        canvas.save()
        canvas.rotate(-28f, cx, cy)
        canvas.drawArc(glyphRect, 195f, 150f, false, paint)
        paint.strokeWidth = 1.3f
        paint.color = KavvoroPalette.pink
        canvas.drawArc(glyphRect, 15f, 125f, false, paint)
        canvas.restore()

        drawRaisedCircle(canvas, cx, cy, 10.7f, KavvoroPalette.purple, paint)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(KavvoroPalette.cyan, 105)
        canvas.drawCircle(cx - 2f, cy - 3f, 6f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy - 4.4f, 1.55f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.8f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xFFF5FDFF.toInt()
        canvas.drawLine(cx, cy, cx, cy + 6f, paint)
        paint.style = Paint.Style.FILL
        paint.color = KavvoroPalette.gold
        canvas.drawCircle(cx + 14f, cy - 10f, 2f, paint)
        drawSpark(canvas, cx - 14f, cy + 9f, 2.8f, KavvoroPalette.cyan, paint)
        paint.color = 0xFFFFF7D1.toInt()
        canvas.drawCircle(cx - 2.5f, cy - 7f, 0.75f, paint)
    }

    private fun drawReset(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        glyphRect.set(cx - 12.5f, cy - 12.5f, cx + 12.5f, cy + 12.5f)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 5f
        paint.color = 0xC7050D22.toInt()
        canvas.save()
        canvas.translate(0f, 1.3f)
        canvas.drawArc(glyphRect, 43f, 282f, false, paint)
        canvas.restore()
        paint.strokeWidth = 4f
        paint.color = KavvoroPalette.cyan
        canvas.drawArc(glyphRect, 43f, 282f, false, paint)
        paint.strokeWidth = 1.2f
        paint.color = 0xFFE7FDFF.toInt()
        canvas.drawArc(glyphRect, 43f, 115f, false, paint)
        paint.strokeWidth = 1.55f
        paint.color = KavvoroPalette.pink
        canvas.drawArc(glyphRect, 185f, 75f, false, paint)

        glyphPath.rewind()
        glyphPath.moveTo(cx + 13f, cy - 7f)
        glyphPath.lineTo(cx + 6f, cy - 10f)
        glyphPath.lineTo(cx + 8f, cy - 3f)
        glyphPath.close()
        drawRaisedPath(canvas, glyphPath, KavvoroPalette.gold, 0xFFFFF7CE.toInt(), paint, strokeWidth = 0.8f)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx - 5.5f, cy + 6.5f, 1.1f, paint)
    }

    private fun drawRaisedPath(
        canvas: Canvas,
        path: Path,
        color: Int,
        highlight: Int,
        paint: Paint,
        strokeWidth: Float = 1.15f
    ) {
        canvas.save()
        canvas.translate(0f, 1.45f)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = 0xC8061028.toInt()
        canvas.drawPath(path, paint)
        canvas.restore()

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = highlight
        canvas.drawPath(path, paint)
    }

    private fun drawRaisedStroke(
        canvas: Canvas,
        path: Path,
        color: Int,
        width: Float,
        paint: Paint,
        highlight: Int = 0xFFE9FCFF.toInt()
    ) {
        canvas.save()
        canvas.translate(0f, 1.35f)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width + 1.3f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = 0xC8061028.toInt()
        canvas.drawPath(path, paint)
        canvas.restore()

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = color
        canvas.drawPath(path, paint)
        paint.strokeWidth = max(0.7f, width * 0.32f)
        paint.color = highlight
        canvas.drawPath(path, paint)
    }

    private fun drawRaisedRoundRect(canvas: Canvas, rect: RectF, radius: Float, color: Int, paint: Paint) {
        canvas.save()
        canvas.translate(0f, 1.35f)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = 0xC8061028.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        canvas.restore()

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.95f
        paint.color = withAlpha(0xFFEFFFFF.toInt(), 205)
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    private fun drawRaisedOval(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, color: Int, paint: Paint) {
        glyphRect.set(left, top, right, bottom)
        canvas.save()
        canvas.translate(0f, 1.2f)
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = 0xC8061028.toInt()
        canvas.drawOval(glyphRect, paint)
        canvas.restore()

        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawOval(glyphRect, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f
        paint.color = 0xFFFFF7FF.toInt()
        canvas.drawOval(glyphRect, paint)
    }

    private fun drawRaisedCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, paint: Paint) {
        paint.reset()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = 0xC8061028.toInt()
        canvas.drawCircle(cx, cy + 1.4f, radius, paint)
        paint.color = color
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = 0xFFFFF3FF.toInt()
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawSpark(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, paint: Paint) {
        glyphPath.rewind()
        glyphPath.moveTo(cx, cy - radius)
        glyphPath.lineTo(cx + radius * 0.32f, cy - radius * 0.32f)
        glyphPath.lineTo(cx + radius, cy)
        glyphPath.lineTo(cx + radius * 0.32f, cy + radius * 0.32f)
        glyphPath.lineTo(cx, cy + radius)
        glyphPath.lineTo(cx - radius * 0.32f, cy + radius * 0.32f)
        glyphPath.lineTo(cx - radius, cy)
        glyphPath.lineTo(cx - radius * 0.32f, cy - radius * 0.32f)
        glyphPath.close()
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(glyphPath, paint)
    }

    private fun iconAccent(id: SettingsIconId, fallback: Int): Int = when (id) {
        SettingsIconId.MUSIC_VOLUME,
        SettingsIconId.ABOUT,
        SettingsIconId.RESET -> KavvoroPalette.pink
        SettingsIconId.SFX_VOLUME,
        SettingsIconId.SCREEN_SHAKE,
        SettingsIconId.TERMS -> KavvoroPalette.blue
        SettingsIconId.HAPTIC,
        SettingsIconId.ACCOUNT -> KavvoroPalette.purple
        SettingsIconId.DATA_DELETION,
        SettingsIconId.PERFORMANCE -> KavvoroPalette.gold
        else -> fallback
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private const val ICON_UNITS = 48f
}
