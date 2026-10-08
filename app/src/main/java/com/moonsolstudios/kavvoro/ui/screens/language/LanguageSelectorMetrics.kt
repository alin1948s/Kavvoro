package com.moonsolstudios.kavvoro.ui.screens.language

import com.moonsolstudios.kavvoro.ui.render.UiTypography

/**
 * Design canvas reference system and metrics for Language Selector V4 — Pixel-Matched Reference.
 * Target reference mockup: 1024 x 1536 (futuristic_romanian_language_selection_ui.png).
 */
object LanguageReferenceCanvas {
    const val WIDTH = 1024f
    const val HEIGHT = 1536f

    // 1. Single Main Cyber Frame (Normalized 0..1)
    const val FRAME_LEFT = 0.042f        // 43 / 1024
    const val FRAME_RIGHT = 0.958f       // 981 / 1024
    const val FRAME_TOP = 0.040f         // 61 / 1536
    const val FRAME_BOTTOM = 0.929f      // 1427 / 1536
    const val FRAME_CHAMFER_PX = 20f     // 18-22px in 1024x1536 space

    // 2. Header
    const val TITLE_CENTER_Y = 0.076f    // 117 / 1536
    const val TITLE_SIZE_PX = 33f        // ~30sp on tablet
    const val SEPARATOR_Y = 0.103f       // 158 / 1536
    const val DIAMOND_SIZE_PX = 13f
    const val SUBTITLE_CENTER_Y = 0.123f // 189 / 1536
    const val SUBTITLE_SIZE_PX = 15.5f   // ~13sp on tablet

    // 3. Language Cards Grid (V4.2 Strict Geometry)
    const val GRID_TOP = 0.150f          // 230 / 1536
    const val LEFT_CARD_LEFT = 0.071f    // 50px / 703px (~7.11% of viewport width)
    const val LEFT_CARD_RIGHT = 0.489f   // 344px / 703px (~48.93% of viewport width)
    const val RIGHT_CARD_LEFT = 0.513f   // 361px / 703px (~51.35% of viewport width)
    const val RIGHT_CARD_RIGHT = 0.931f  // 655px / 703px (~93.17% of viewport width)
    const val COLUMN_GAP = 0.024f        // gap = 0.513f - 0.489f = 0.024f (~17px / 703px)
    const val CONTENT_HORIZ_PAD = 0.030f // ~3% of frame width
    const val CARD_HEIGHT = 0.0495f      // 76 / 1536 (~5% of viewport height)
    const val ROW_GAP_RATIO = 0.145f     // gap = cardHeight * 0.145f (~11px)
    const val CARD_CHAMFER_PX = 11f      // 10-12px in 1024x1536 space

    // 4. Card Content
    const val FLAG_WIDTH_PX = 60f        // hero vector flag box (~60x44 design px)
    const val FLAG_HEIGHT_PX = 44f
    const val FLAG_TEXT_GAP_RATIO = 0.055f // gap between flag and text = cardWidth * 0.055f
    const val TEXT_SIZE_PX = 23.5f       // ~20-25% larger language text
    const val RADIO_DIAMETER_PX = 23.5f  // ~20-22dp on tablet
    const val RADIO_MARGIN_END_PX = 18f

    // 5. Enclosed Cyber Footer Bar (V4.2 Wide Bounds, V4.3 +10% height)
    const val FOOTER_LEFT = 0.066f       // 46 / 703
    const val FOOTER_RIGHT = 0.934f      // 657 / 703
    const val FOOTER_TOP = 0.878f        // 1348 / 1536 (+10% height centered)
    const val FOOTER_BOTTOM = 0.921f     // 1415 / 1536
    const val FOOTER_CHAMFER_PX = 13f

    // 6. Visual Calibration & Correction V4.4
    const val TITLE_TARGET_WIDTH_RATIO = 0.38f
    const val BODY_SCALE_FROM_CURRENT = 1.22f // +20-25% larger text
    const val FOOTER_TEXT_SCALE_FROM_CURRENT = 1.25f
    const val SELECTED_BORDER_PX = 2.0f
    const val SELECTED_GLOW_ALPHA = 0.20f
    const val SELECTED_GLOW_RADIUS = 8f
    const val RADIO_GLOW_ALPHA = 0.20f
    const val RADIO_GLOW_RADIUS = 5f
    const val INACTIVE_CHAMFER_SCALE = 1.10f
    const val SELECTED_CHAMFER_SCALE = 1.25f
    const val FRAME_GLOW_ALPHA = 0.07f
    const val FRAME_GLOW_RADIUS = 18f
}

object LanguageSelectorMetrics {
    // Legacy DP fallbacks for compact phone views
    const val PANEL_WIDTH_RATIO = 0.916f
    const val PANEL_HEIGHT_RATIO = 0.889f

    const val PANEL_HORIZONTAL_PADDING_DP = 16f
    const val PANEL_VERTICAL_PADDING_DP = 14f

    const val HEADER_HEIGHT_DP = 88f

    const val CARD_HEIGHT_DP = 55f
    const val ROW_GAP_DP = 8f
    const val COLUMN_GAP_DP = 16f

    const val FLAG_WIDTH_DP = 48f
    const val FLAG_HEIGHT_DP = 32f
    const val FLAG_PHONE_WIDTH_DP = 36f
    const val FLAG_PHONE_HEIGHT_DP = 24f

    const val RADIO_SIZE_DP = 20f
    const val RADIO_INNER_DOT_DP = 7f
    const val RADIO_MARGIN_END_DP = 14f
    const val LANGUAGE_NAME_MIN_SIZE_DP = 14f
    const val LANGUAGE_NAME_MAX_SIZE_DP = UiTypography.LANGUAGE_OPTION_DP
    const val LANGUAGE_KICKER_TEXT_SIZE_DP = 12f
    const val CURRENT_STATUS_TEXT_SIZE_DP = 13f
    const val SCROLL_END_CLEARANCE_DP = 8f

    const val FOOTER_HEIGHT_DP = 48f
    const val FOOTER_BOTTOM_MARGIN_DP = 16f

    const val BACK_VISUAL_SIZE_DP = 36f
    const val BACK_TOUCH_SIZE_DP = 48f
    const val BACK_MARGIN_START_DP = 17f
    const val BACK_MARGIN_TOP_DP = 14f

    // V4.1 Visual Calibration Constants
    const val TITLE_TARGET_WIDTH_RATIO = LanguageReferenceCanvas.TITLE_TARGET_WIDTH_RATIO
    const val BODY_SCALE_FROM_CURRENT = LanguageReferenceCanvas.BODY_SCALE_FROM_CURRENT
    const val FOOTER_TEXT_SCALE_FROM_CURRENT = LanguageReferenceCanvas.FOOTER_TEXT_SCALE_FROM_CURRENT
    const val SELECTED_BORDER_PX = LanguageReferenceCanvas.SELECTED_BORDER_PX
    const val SELECTED_GLOW_ALPHA = LanguageReferenceCanvas.SELECTED_GLOW_ALPHA
    const val SELECTED_GLOW_RADIUS = LanguageReferenceCanvas.SELECTED_GLOW_RADIUS
    const val RADIO_GLOW_ALPHA = LanguageReferenceCanvas.RADIO_GLOW_ALPHA
    const val RADIO_GLOW_RADIUS = LanguageReferenceCanvas.RADIO_GLOW_RADIUS
    const val INACTIVE_CHAMFER_SCALE = LanguageReferenceCanvas.INACTIVE_CHAMFER_SCALE
    const val SELECTED_CHAMFER_SCALE = LanguageReferenceCanvas.SELECTED_CHAMFER_SCALE
    const val FRAME_GLOW_ALPHA = LanguageReferenceCanvas.FRAME_GLOW_ALPHA
    const val FRAME_GLOW_RADIUS = LanguageReferenceCanvas.FRAME_GLOW_RADIUS

    fun languageNameTextSize(cardHeight: Float, dp: Float): Float =
        (cardHeight * 0.29f).coerceIn(LANGUAGE_NAME_MIN_SIZE_DP * dp, LANGUAGE_NAME_MAX_SIZE_DP * dp)
}
