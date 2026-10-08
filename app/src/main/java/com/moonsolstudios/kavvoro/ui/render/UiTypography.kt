package com.moonsolstudios.kavvoro.ui.render

/**
 * Shared type scale for screen headings. Canvas renderers apply their normal
 * width fitting after selecting a semantic role from this scale.
 */
object UiTypography {
    const val SCREEN_TITLE_DP = 32f
    const val COMPACT_SCREEN_TITLE_DP = 26f
    const val PANEL_TITLE_DP = 18f
    const val SECTION_TITLE_DP = 16f
    const val COMPACT_SECTION_TITLE_DP = 15f
    const val SCREEN_SUBTITLE_DP = 14f
    const val COMPACT_SCREEN_SUBTITLE_DP = 12f
    const val EYEBROW_DP = 10f
    const val COMPACT_EYEBROW_DP = 9f
    const val LANGUAGE_OPTION_DP = 16f

    fun screenTitleDp(compact: Boolean): Float =
        if (compact) COMPACT_SCREEN_TITLE_DP else SCREEN_TITLE_DP

    fun sectionTitleDp(compact: Boolean): Float =
        if (compact) COMPACT_SECTION_TITLE_DP else SECTION_TITLE_DP

    fun screenSubtitleDp(compact: Boolean): Float =
        if (compact) COMPACT_SCREEN_SUBTITLE_DP else SCREEN_SUBTITLE_DP

    fun eyebrowDp(compact: Boolean): Float =
        if (compact) COMPACT_EYEBROW_DP else EYEBROW_DP
}
