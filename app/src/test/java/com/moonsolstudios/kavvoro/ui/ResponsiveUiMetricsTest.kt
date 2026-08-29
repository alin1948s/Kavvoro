package com.moonsolstudios.kavvoro.ui

import android.util.DisplayMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponsiveUiMetricsTest {
    private fun metrics(density: Float = 1f): DisplayMetrics = DisplayMetrics().apply {
        this.density = density
        scaledDensity = density
    }

    @Test
    fun standardPortraitUsesReferenceScale() {
        val result = ResponsiveUiMetrics.from(390, 844, metrics())
        assertEquals(1f, result.scale, 0.001f)
    }

    @Test
    fun shortPhoneIsConstrainedByHeight() {
        val result = ResponsiveUiMetrics.from(360, 640, metrics())
        assertEquals(ResponsiveUiMetrics.MIN_SCALE, result.scale, 0.001f)
    }

    @Test
    fun safeInsetsReduceAvailableScaleAndExposeContentBounds() {
        val result = ResponsiveUiMetrics.from(1080, 2400, metrics(2.75f), safeTopPx = 90, safeBottomPx = 120)
        assertTrue(result.contentHeightPx < 2400)
        assertTrue(result.scale <= ResponsiveUiMetrics.MAX_SCALE)
    }
}
