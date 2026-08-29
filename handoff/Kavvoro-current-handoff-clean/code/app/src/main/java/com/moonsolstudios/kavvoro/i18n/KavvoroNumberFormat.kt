package com.moonsolstudios.kavvoro.i18n

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object KavvoroNumberFormat {
    /** Uses the active locale's digits and decimal symbol for both HUD and results. */
    fun seconds(value: Float, locale: Locale): String {
        val format = DecimalFormat("0.0", DecimalFormatSymbols.getInstance(locale)).apply {
            isGroupingUsed = false
        }
        return "${format.format(value.toDouble())}s"
    }
}
