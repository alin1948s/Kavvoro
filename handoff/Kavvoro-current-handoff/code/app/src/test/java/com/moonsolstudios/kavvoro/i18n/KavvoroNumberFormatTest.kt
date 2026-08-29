package com.moonsolstudios.kavvoro.i18n

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class KavvoroNumberFormatTest {
    @Test
    fun secondsUseTheActiveLocaleDecimalSeparator() {
        assertEquals("8,9s", KavvoroNumberFormat.seconds(8.94f, Locale.forLanguageTag("pl-PL")))
        assertEquals("8.9s", KavvoroNumberFormat.seconds(8.94f, Locale.ENGLISH))
    }

    @Test
    fun arabicUsesItsDecimalSymbolsAndHindiKeepsLocaleAwareIndicFormatting() {
        assertEquals("٨٫٩s", KavvoroNumberFormat.seconds(8.94f, Locale.forLanguageTag("ar")))
        assertEquals("8.9s", KavvoroNumberFormat.seconds(8.94f, Locale.forLanguageTag("hi-IN")))
    }
}
