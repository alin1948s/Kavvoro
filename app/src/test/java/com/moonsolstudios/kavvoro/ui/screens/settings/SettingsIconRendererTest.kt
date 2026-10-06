package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsIconRendererTest {

    @Test
    fun testAllThirteenIconIdsExist() {
        val values = SettingsIconRenderer.SettingsIconId.values()
        assertEquals(13, values.size)
        val expected = setOf(
            "MASTER_VOLUME",
            "MUSIC_VOLUME",
            "SFX_VOLUME",
            "HAPTIC",
            "SCREEN_SHAKE",
            "PERFORMANCE",
            "LANGUAGE",
            "ACCOUNT",
            "PRIVACY",
            "TERMS",
            "DATA_DELETION",
            "ABOUT",
            "RESET"
        )
        val actual = values.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun testDrawSettingsIconDoesNotThrowForAnyIcon() {
        val canvas = Canvas()
        val paint = Paint()
        val rect = RectF(10f, 10f, 40f, 40f)

        for (id in SettingsIconRenderer.SettingsIconId.values()) {
            SettingsIconRenderer.drawSettingsIcon(
                canvas = canvas,
                rect = rect,
                id = id,
                accent = 0xFF45F2FF.toInt(),
                active = false,
                paint = paint,
                dp = 2.0f
            )
            SettingsIconRenderer.drawSettingsIcon(
                canvas = canvas,
                rect = rect,
                id = id,
                accent = 0xFFFF4D8D.toInt(),
                active = true,
                paint = paint,
                dp = 2.0f
            )
        }
    }
}
