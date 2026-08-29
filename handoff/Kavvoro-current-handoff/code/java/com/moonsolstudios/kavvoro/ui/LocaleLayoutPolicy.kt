package com.moonsolstudios.kavvoro.ui

import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage

enum class LocaleTextRole {
    LABEL,
    BODY,
    TITLE
}

/**
 * Small, Android-free layout policy shared by menu, HUD, tutorial and result
 * text. The values are deliberately conservative: readable text wins over a
 * one-line fit, and wrapping never removes characters or adds ellipses.
 */
object LocaleLayoutPolicy {
    private const val RTL_EXTRA_PADDING = 4f

    private val rtlLanguages = setOf(KavvoroLanguage.AR)

    fun isRtl(language: KavvoroLanguage): Boolean = language in rtlLanguages

    fun safeHorizontalPadding(basePadding: Float, language: KavvoroLanguage): Float =
        basePadding + if (isRtl(language)) RTL_EXTRA_PADDING else 0f

    fun safeContentWidth(
        containerWidth: Float,
        reservedWidth: Float,
        language: KavvoroLanguage
    ): Float = (containerWidth - reservedWidth - if (isRtl(language)) RTL_EXTRA_PADDING * 2f else 0f)
        .coerceAtLeast(0f)

    fun minimumTextSizeDp(language: KavvoroLanguage, role: LocaleTextRole): Float {
        return when (language) {
            KavvoroLanguage.AR -> when (role) {
                LocaleTextRole.LABEL -> 8f
                LocaleTextRole.BODY -> 8f
                LocaleTextRole.TITLE -> 9f
            }
            KavvoroLanguage.HI -> when (role) {
                LocaleTextRole.LABEL -> 7.5f
                LocaleTextRole.BODY -> 7.5f
                LocaleTextRole.TITLE -> 9f
            }
            KavvoroLanguage.JA,
            KavvoroLanguage.KO,
            KavvoroLanguage.ZH -> when (role) {
                LocaleTextRole.LABEL -> 8f
                LocaleTextRole.BODY -> 8f
                LocaleTextRole.TITLE -> 9f
            }
            else -> when (role) {
                LocaleTextRole.LABEL -> 7f
                LocaleTextRole.BODY -> 7.2f
                LocaleTextRole.TITLE -> 8.5f
            }
        }
    }

    /**
     * Wraps text at measured-width boundaries without truncation. Words are
     * preferred for natural-language locales; scripts without spaces fall back
     * to character boundaries. Explicit line breaks are always preserved.
     */
    fun wrapText(
        text: String,
        maxWidth: Float,
        measureText: (String) -> Float
    ): List<String> {
        if (text.isEmpty()) return listOf("")
        if (maxWidth <= 0f) return listOf(text)

        return text.replace("\r\n", "\n")
            .split('\n')
            .flatMap { line -> wrapLine(line, maxWidth, measureText) }
    }

    private fun wrapLine(
        line: String,
        maxWidth: Float,
        measureText: (String) -> Float
    ): List<String> {
        if (line.isEmpty()) return listOf("")
        val tokens = if (line.any(::isNoSpaceScript)) {
            line.map { it.toString() }
        } else {
            line.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
        }
        val lines = mutableListOf<String>()
        var current = ""
        tokens.forEach { token ->
            val candidate = if (current.isEmpty()) token else "$current $token"
            if (measureText(candidate) <= maxWidth) {
                current = candidate
            } else if (current.isNotEmpty()) {
                lines += current
                val parts = splitToken(token, maxWidth, measureText)
                lines += parts.dropLast(1)
                current = parts.lastOrNull().orEmpty()
            } else {
                val parts = splitToken(token, maxWidth, measureText)
                lines += parts.dropLast(1)
                current = parts.lastOrNull().orEmpty()
            }
        }
        if (current.isNotEmpty()) lines += current
        return lines.ifEmpty { listOf("") }
    }

    private fun splitToken(
        token: String,
        maxWidth: Float,
        measureText: (String) -> Float
    ): List<String> {
        val parts = mutableListOf<String>()
        var current = ""
        token.forEach { character ->
            val candidate = current + character
            if (current.isNotEmpty() && measureText(candidate) > maxWidth) {
                parts += current
                current = character.toString()
            } else {
                current = candidate
            }
        }
        if (current.isNotEmpty()) parts += current
        return parts.ifEmpty { listOf(token) }
    }

    private fun isNoSpaceScript(character: Char): Boolean {
        return character in '\u3040'..'\u30ff' ||
            character in '\u3400'..'\u4dbf' ||
            character in '\u4e00'..'\u9fff' ||
            character in '\uac00'..'\ud7af'
    }
}
