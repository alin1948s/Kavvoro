package com.moonsolstudios.kavvoro.ui.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.ui.layout.LocaleLayoutPolicy
import com.moonsolstudios.kavvoro.ui.layout.LocaleTextRole
import kotlin.math.max

object UiWidgetRenderer {

    private val scratch = RectF()

    fun fitText(
        context: Context,
        text: String,
        maxWidth: Float,
        textPaint: Paint,
        dp: Float
    ): String {
        textPaint.textScaleX = 1f
        if (textPaint.measureText(text) <= maxWidth) return text
        val minimumSize = LocaleLayoutPolicy.minimumTextSizeDp(
            KavvoroI18n.active(context),
            LocaleTextRole.LABEL
        ) * dp
        while (textPaint.textSize > minimumSize && textPaint.measureText(text) > maxWidth) {
            textPaint.textSize -= 0.35f * dp
        }
        val measuredWidth = textPaint.measureText(text)
        if (measuredWidth > maxWidth && maxWidth > 0f) {
            textPaint.textScaleX = (maxWidth / measuredWidth).coerceAtMost(1f)
        }
        return text
    }

    fun drawFittedText(
        canvas: Canvas,
        context: Context,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        startSizeDp: Float,
        minSizeDp: Float,
        textPaint: Paint,
        dp: Float
    ) {
        textPaint.textSize = startSizeDp * dp
        val minSize = maxOf(
            minSizeDp,
            LocaleLayoutPolicy.minimumTextSizeDp(
                KavvoroI18n.active(context),
                LocaleTextRole.TITLE
            )
        ) * dp
        while (textPaint.textSize > minSize && textPaint.measureText(text) > maxWidth) {
            textPaint.textSize -= 0.35f * dp
        }
        canvas.drawText(fitText(context, text, maxWidth, textPaint, dp), x, y, textPaint)
    }

    fun drawUiButtonFrame(
        canvas: Canvas,
        rect: RectF,
        active: Boolean,
        accent: Int,
        cornerDp: Float,
        paint: Paint,
        dp: Float
    ) {
        val corner = cornerDp * dp
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            intArrayOf(
                if (active) withAlpha(accent, 92) else 0xA00B101C.toInt(),
                0x7A141B27,
                if (active) withAlpha(accent, 42) else 0x52101822
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (active) 1.35f * dp else 0.85f * dp
        paint.color = if (active) withAlpha(accent, 220) else 0x52FFFFFF
        canvas.drawRoundRect(rect, corner, corner, paint)

        paint.style = Paint.Style.FILL
        paint.color = if (active) withAlpha(0xFFF7F4FF.toInt(), 34) else 0x18FFFFFF
        canvas.drawRoundRect(
            rect.left + 3f * dp,
            rect.top + 3f * dp,
            rect.right - 3f * dp,
            rect.top + rect.height() * 0.42f,
            max(2f * dp, corner - 3f * dp),
            max(2f * dp, corner - 3f * dp),
            paint
        )
    }

    fun drawUiIconAsset(
        canvas: Canvas,
        key: String,
        rect: RectF,
        padDp: Float,
        alpha: Int,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val pad = padDp * dp
        scratch.set(rect.left + pad, rect.top + pad, rect.right - pad, rect.bottom - pad)
        drawWorldAsset(canvas, key, scratch, alpha)
    }

    fun drawAudioIconAsset(
        canvas: Canvas,
        rect: RectF,
        key: String,
        muted: Boolean,
        active: Boolean,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        drawUiIconAsset(canvas, key, rect, padDp = -1f, alpha = if (muted) 155 else if (active) 255 else 232, dp = dp, drawWorldAsset = drawWorldAsset)
        if (muted) {
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 2.4f * dp
            paint.color = 0xFFFF4D8D.toInt()
            canvas.drawLine(
                rect.left + rect.width() * 0.26f,
                rect.bottom - rect.height() * 0.25f,
                rect.right - rect.width() * 0.24f,
                rect.top + rect.height() * 0.24f,
                paint
            )
            paint.strokeCap = Paint.Cap.BUTT
        }
    }

    fun drawIconButton(
        canvas: Canvas,
        rect: RectF,
        id: ButtonId,
        active: Boolean,
        sfxMuted: Boolean,
        musicMuted: Boolean,
        levelAccent: Int,
        paint: Paint,
        dp: Float,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (rect.isEmpty) return
        val accent = when (id) {
            ButtonId.HOME -> 0xFF45F2FF.toInt()
            ButtonId.RESTART -> 0xFFFF4D8D.toInt()
            ButtonId.SHARE -> 0xFFC15CFF.toInt()
            ButtonId.NEXT -> 0xFFFFCF4A.toInt()
            ButtonId.SFX -> 0xFF45F2FF.toInt()
            ButtonId.MUSIC -> 0xFFFFCF4A.toInt()
            else -> levelAccent
        }
        drawUiButtonFrame(canvas, rect, active, accent, cornerDp = 7f, paint = paint, dp = dp)
        val iconKey = when (id) {
            ButtonId.HOME -> "ui_home"
            ButtonId.RESTART -> "ui_retry"
            ButtonId.SHARE -> "ui_share"
            ButtonId.NEXT -> "ui_next"
            ButtonId.SFX -> "ui_sound"
            ButtonId.MUSIC -> "ui_music"
            ButtonId.CONTINUE,
            ButtonId.AD_CONTINUE,
            ButtonId.NONE -> null
        }
        when (id) {
            ButtonId.SFX -> drawAudioIconAsset(canvas, rect, "ui_sound", muted = sfxMuted, active = active, paint = paint, dp = dp, drawWorldAsset = drawWorldAsset)
            ButtonId.MUSIC -> drawAudioIconAsset(canvas, rect, "ui_music", muted = musicMuted, active = active, paint = paint, dp = dp, drawWorldAsset = drawWorldAsset)
            else -> iconKey?.let {
                drawUiIconAsset(canvas, it, rect, padDp = -1f, alpha = if (active) 255 else 232, dp = dp, drawWorldAsset = drawWorldAsset)
            }
        }
    }
}
