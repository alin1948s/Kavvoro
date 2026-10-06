package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import org.junit.Test

/**
 * Safety verification tests for shared UI render primitives ([UiWidgetRenderer], [CyberShapeRenderer]):
 * Ensures vector icon paths and procedural shapes execute safely without throwing,
 * handle dirty paint configurations, and withstand degenerate bounds (zero/negative sizes).
 */
class UiWidgetRendererSafetyTest {

    private fun createDirtyPaint(): Paint {
        return Paint().apply {
            shader = LinearGradient(0f, 0f, 100f, 100f, 0xFF000000.toInt(), 0xFFFFFFFF.toInt(), Shader.TileMode.CLAMP)
            pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    }

    @Test
    fun testUiWidgetRendererIconsExecuteSafely() {
        val canvas = Canvas()
        val paint = createDirtyPaint()
        val dp = 2.0f

        UiWidgetRenderer.drawHomeVectorIcon(canvas, 50f, 50f, 24f * dp, 0xFFFFFFFF.toInt(), paint, dp)
        UiWidgetRenderer.drawRestartVectorIcon(canvas, 50f, 50f, 24f * dp, 0xFFFFFFFF.toInt(), paint, dp)
        UiWidgetRenderer.drawSpeakerVectorIcon(canvas, 50f, 50f, 24f * dp, muted = false, color = 0xFFFFFFFF.toInt(), paint = paint, dp = dp)
        UiWidgetRenderer.drawSpeakerVectorIcon(canvas, 50f, 50f, 24f * dp, muted = true, color = 0xFFFFFFFF.toInt(), paint = paint, dp = dp)
        UiWidgetRenderer.drawMusicVectorIcon(canvas, 50f, 50f, 24f * dp, muted = false, color = 0xFFFFFFFF.toInt(), paint = paint, dp = dp)
        UiWidgetRenderer.drawMusicVectorIcon(canvas, 50f, 50f, 24f * dp, muted = true, color = 0xFFFFFFFF.toInt(), paint = paint, dp = dp)
        UiWidgetRenderer.drawShareVectorIcon(canvas, 50f, 50f, 24f * dp, 0xFFFFFFFF.toInt(), paint, dp)
        UiWidgetRenderer.drawNextVectorIcon(canvas, 50f, 50f, 24f * dp, 0xFFFFFFFF.toInt(), paint, dp)

        val degenerateSizes = listOf(0f, -10f, 0.5f)
        for (sz in degenerateSizes) {
            UiWidgetRenderer.drawHomeVectorIcon(canvas, 50f, 50f, sz, 0xFFFFFFFF.toInt(), paint, dp)
            UiWidgetRenderer.drawRestartVectorIcon(canvas, 50f, 50f, sz, 0xFFFFFFFF.toInt(), paint, dp)
            UiWidgetRenderer.drawSpeakerVectorIcon(canvas, 50f, 50f, sz, muted = false, color = 0xFFFFFFFF.toInt(), paint = paint, dp = dp)
            UiWidgetRenderer.drawMusicVectorIcon(canvas, 50f, 50f, sz, muted = false, color = 0xFFFFFFFF.toInt(), paint = paint, dp = dp)
            UiWidgetRenderer.drawShareVectorIcon(canvas, 50f, 50f, sz, 0xFFFFFFFF.toInt(), paint, dp)
            UiWidgetRenderer.drawNextVectorIcon(canvas, 50f, 50f, sz, 0xFFFFFFFF.toInt(), paint, dp)
        }
    }

    @Test
    fun testCyberShapeRendererExecutesSafely() {
        val canvas = Canvas()
        val paint = createDirtyPaint()
        val rect = RectF(10f, 10f, 100f, 80f)

        CyberShapeRenderer.drawCyberChamferRect(canvas, rect, 10f, 6f, paint)
        CyberShapeRenderer.drawDualRailCyberBorder(canvas, rect, 10f, 6f, 0xFF00E5FF.toInt(), 0xFFFF2E93.toInt(), 2.0f, paint)
    }
}
