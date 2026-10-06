package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import org.junit.Test

/**
 * Safety verification tests for Home screen procedural icons in [HomeMenuRenderer]:
 * Ensures vector icon paths execute safely without throwing, handle dirty paint
 * configurations, and withstand degenerate bounds (zero/negative sizes).
 */
class HomeMenuRendererSafetyTest {

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
    fun testHomeMenuGearIconExecutesSafelyWithDirtyPaintAndDegenerateSizes() {
        val canvas = Canvas()
        val paint = createDirtyPaint()
        val dp = 2.0f

        HomeMenuRenderer.drawGearIcon(canvas, 100f, 100f, 20f * dp, 0xFF00E5FF.toInt(), paint, dp)

        val degenerateSizes = listOf(0f, -10f, 0.001f, 1f)
        for (sz in degenerateSizes) {
            HomeMenuRenderer.drawGearIcon(canvas, 50f, 50f, sz, 0xFFFFFFFF.toInt(), paint, dp)
        }
    }
}
