package com.moonsolstudios.kavvoro.ui

import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocaleLayoutPolicyTest {
    @Test
    fun arabicUsesRtlAndKeepsMoreHorizontalBreathingRoom() {
        assertTrue(LocaleLayoutPolicy.isRtl(KavvoroLanguage.AR))
        assertFalse(LocaleLayoutPolicy.isRtl(KavvoroLanguage.EN))
        assertTrue(
            LocaleLayoutPolicy.safeHorizontalPadding(14f, KavvoroLanguage.AR) >
                LocaleLayoutPolicy.safeHorizontalPadding(14f, KavvoroLanguage.EN)
        )
    }

    @Test
    fun narrowLabelsNeverDropBelowReadableLocaleMinimums() {
        assertTrue(
            LocaleLayoutPolicy.minimumTextSizeDp(KavvoroLanguage.AR, LocaleTextRole.LABEL) >= 8f
        )
        assertTrue(
            LocaleLayoutPolicy.minimumTextSizeDp(KavvoroLanguage.HI, LocaleTextRole.BODY) >= 7.5f
        )
        assertTrue(
            LocaleLayoutPolicy.minimumTextSizeDp(KavvoroLanguage.ZH, LocaleTextRole.TITLE) >= 9f
        )
    }

    @Test
    fun longLabelsWrapWithoutEllipsisOrDroppedCharacters() {
        val text = "NEXT CLEAR RESET 3H 30M"
        val lines = LocaleLayoutPolicy.wrapText(
            text = text,
            maxWidth = 11f,
            measureText = { it.length.toFloat() }
        )

        assertEquals(listOf("NEXT CLEAR", "RESET 3H", "30M"), lines)
        assertEquals(text, lines.joinToString(" "))
        assertTrue(lines.none { it.endsWith("...") })
    }

    @Test
    fun scriptsWithoutSpacesBreakAtReadableCharacterBoundaries() {
        val text = "مرحبا بالعالم"
        val lines = LocaleLayoutPolicy.wrapText(
            text = text,
            maxWidth = 8f,
            measureText = { it.length.toFloat() }
        )

        assertEquals(listOf("مرحبا", "بالعالم"), lines)
    }
}
