package com.moonsolstudios.kavvoro.ui.screens.language

import android.graphics.RectF
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage

/**
 * Dedicated responsive layout calculator for the Language Selector screen.
 */
object LanguageSelectorLayoutCalculator {

    fun layoutLanguageSelector(
        side: Float,
        contentWidth: Float,
        compact: Boolean,
        headerY: Float,
        viewportTop: Float,
        viewportBottom: Float,
        languageScroll: Float,
        dp: Float,
        languageBackButton: RectF,
        languageItemRects: MutableList<RectF>,
        languageDeckRect: RectF = RectF(),
        languageFooterRect: RectF = RectF(),
        viewportWidth: Float = contentWidth
    ): LanguageSelectorLayout {
        val wDp = viewportWidth / dp
        val totalViewportHeight = viewportBottom - viewportTop
        val columns = if (wDp < 520f) 1 else 2
        val isLandscape = viewportWidth > totalViewportHeight

        val selectableLanguages = KavvoroLanguage.selectableLanguages
        val displayCount = selectableLanguages.size
        val rows = ((displayCount + columns - 1) / columns).coerceAtLeast(1)

        val panelLeft: Float
        val panelRight: Float
        val panelTop: Float
        val panelBottom: Float
        val gridLeft: Float
        val gridRight: Float
        val gridWidth: Float
        val colGap: Float
        val cardWidth: Float
        val cardHeight: Float
        val rowGap: Float
        val gridTopBase: Float
        val footerLeft: Float
        val footerRight: Float
        val footerTop: Float
        val footerBottom: Float

        if (columns == 2 && !isLandscape) {
            // V4.2 Pixel-Matched Reference Layout (1024x1536 design target)
            panelLeft = viewportWidth * LanguageReferenceCanvas.FRAME_LEFT
            panelRight = viewportWidth * LanguageReferenceCanvas.FRAME_RIGHT
            panelTop = viewportTop + totalViewportHeight * LanguageReferenceCanvas.FRAME_TOP
            panelBottom = viewportTop + totalViewportHeight * LanguageReferenceCanvas.FRAME_BOTTOM

            val leftColLeft = viewportWidth * LanguageReferenceCanvas.LEFT_CARD_LEFT
            val leftColRight = viewportWidth * LanguageReferenceCanvas.LEFT_CARD_RIGHT
            val rightColLeft = viewportWidth * LanguageReferenceCanvas.RIGHT_CARD_LEFT
            val rightColRight = viewportWidth * LanguageReferenceCanvas.RIGHT_CARD_RIGHT

            gridLeft = leftColLeft
            gridRight = rightColRight
            gridWidth = gridRight - gridLeft
            cardWidth = leftColRight - leftColLeft
            colGap = rightColLeft - leftColRight

            cardHeight = totalViewportHeight * LanguageReferenceCanvas.CARD_HEIGHT
            rowGap = cardHeight * LanguageReferenceCanvas.ROW_GAP_RATIO
            gridTopBase = viewportTop + totalViewportHeight * LanguageReferenceCanvas.GRID_TOP

            footerLeft = viewportWidth * LanguageReferenceCanvas.FOOTER_LEFT
            footerRight = viewportWidth * LanguageReferenceCanvas.FOOTER_RIGHT
            footerTop = viewportTop + totalViewportHeight * LanguageReferenceCanvas.FOOTER_TOP
            footerBottom = viewportTop + totalViewportHeight * LanguageReferenceCanvas.FOOTER_BOTTOM
        } else {
            // Adaptive / fallback for phone (1 col) or landscape
            val panelWidth = when {
                wDp < 520f -> (viewportWidth * 0.92f).coerceAtMost(viewportWidth - 16f * dp)
                isLandscape -> (viewportWidth * 0.78f).coerceAtMost(860f * dp)
                else -> viewportWidth * LanguageSelectorMetrics.PANEL_WIDTH_RATIO
            }
            val panelHeight = when {
                wDp < 520f -> totalViewportHeight * 0.95f
                isLandscape -> totalViewportHeight * 0.90f
                else -> totalViewportHeight * LanguageSelectorMetrics.PANEL_HEIGHT_RATIO
            }
            panelLeft = (viewportWidth - panelWidth) * 0.5f
            panelRight = panelLeft + panelWidth
            panelTop = viewportTop + (totalViewportHeight - panelHeight) * 0.5f
            panelBottom = panelTop + panelHeight

            val padX = LanguageSelectorMetrics.PANEL_HORIZONTAL_PADDING_DP * dp
            gridLeft = panelLeft + padX
            gridRight = panelRight - padX
            gridWidth = gridRight - gridLeft

            colGap = (if (columns == 1) 0f else LanguageSelectorMetrics.COLUMN_GAP_DP) * dp
            cardWidth = (gridWidth - colGap * (columns - 1)) / columns

            cardHeight = LanguageSelectorMetrics.CARD_HEIGHT_DP * dp
            rowGap = LanguageSelectorMetrics.ROW_GAP_DP * dp

            val padTop = LanguageSelectorMetrics.PANEL_VERTICAL_PADDING_DP * dp
            val headerHeight = LanguageSelectorMetrics.HEADER_HEIGHT_DP * dp
            gridTopBase = panelTop + padTop + headerHeight

            val footerHeight = LanguageSelectorMetrics.FOOTER_HEIGHT_DP * dp
            footerBottom = panelBottom - LanguageSelectorMetrics.FOOTER_BOTTOM_MARGIN_DP * dp
            footerTop = footerBottom - footerHeight
            footerLeft = gridLeft
            footerRight = gridRight
        }

        val totalGridHeight = rows * cardHeight + (rows - 1) * rowGap
        val availableGridHeight = footerTop - gridTopBase
        val fitsCompletely = totalGridHeight <= availableGridHeight

        val gridTop: Float
        val scroll: Float
        val maxScroll: Float

        if (fitsCompletely) {
            gridTop = gridTopBase
            scroll = 0f
            maxScroll = 0f
        } else {
            maxScroll = (
                totalGridHeight - availableGridHeight + LanguageSelectorMetrics.SCROLL_END_CLEARANCE_DP * dp
            ).coerceAtLeast(0f)
            scroll = languageScroll.coerceIn(0f, maxScroll)
            gridTop = gridTopBase - scroll
        }

        languageFooterRect.apply {
            left = footerLeft
            top = footerTop
            right = footerRight
            bottom = footerBottom
        }

        languageDeckRect.apply {
            left = panelLeft
            top = panelTop
            right = panelRight
            bottom = panelBottom
        }

        val backSize = LanguageSelectorMetrics.BACK_VISUAL_SIZE_DP * dp
        val backTouchSize = LanguageSelectorMetrics.BACK_TOUCH_SIZE_DP * dp
        val backLeft = panelLeft + LanguageSelectorMetrics.BACK_MARGIN_START_DP * dp
        val titleCenterY = viewportTop + totalViewportHeight * LanguageReferenceCanvas.TITLE_CENTER_Y
        val backTop = titleCenterY - backSize * 0.5f
        languageBackButton.apply {
            left = backLeft
            top = backTop
            right = backLeft + backSize
            bottom = backTop + backSize
        }

        val cx = backLeft + backSize * 0.5f
        val cy = backTop + backSize * 0.5f
        val backHitbox = RectF().apply {
            left = cx - backTouchSize * 0.5f
            top = cy - backTouchSize * 0.5f
            right = cx + backTouchSize * 0.5f
            bottom = cy + backTouchSize * 0.5f
        }

        while (languageItemRects.size < displayCount) languageItemRects.add(RectF())
        while (languageItemRects.size > displayCount) languageItemRects.removeAt(languageItemRects.lastIndex)

        val isPhone = columns == 1
        val flagW = if (isPhone) {
            LanguageSelectorMetrics.FLAG_PHONE_WIDTH_DP * dp
        } else {
            (cardHeight * 0.60f * 1.48f).coerceAtLeast(LanguageSelectorMetrics.FLAG_WIDTH_DP * dp)
        }
        val flagH = flagW / 1.48f
        val radioSize = if (isPhone) {
            LanguageSelectorMetrics.RADIO_SIZE_DP * dp
        } else {
            (cardHeight * 0.38f).coerceIn(18f * dp, 24f * dp)
        }
        val flagPadLeft = if (isPhone) 10f * dp else (cardWidth * 0.045f)
        val radioPadRight = if (isPhone) LanguageSelectorMetrics.RADIO_MARGIN_END_DP * dp else (cardWidth * 0.045f)
        val flagTextGap = if (isPhone) 10f * dp else (cardWidth * LanguageReferenceCanvas.FLAG_TEXT_GAP_RATIO)

        val cardsList = ArrayList<LanguageCardLayout>(displayCount)
        for (index in 0 until displayCount) {
            val row = index / columns
            val column = index % columns
            val itemLeft = gridLeft + column * (cardWidth + colGap)
            val itemTop = gridTop + row * (cardHeight + rowGap)
            val itemRight = itemLeft + cardWidth
            val itemBottom = itemTop + cardHeight

            val bounds = languageItemRects[index].apply {
                this.left = itemLeft
                this.top = itemTop
                this.right = itemRight
                this.bottom = itemBottom
            }

            val flagBounds = RectF().apply {
                this.left = itemLeft + flagPadLeft
                this.top = itemTop + (cardHeight - flagH) * 0.5f
                this.right = this.left + flagW
                this.bottom = this.top + flagH
            }

            val textX = flagBounds.right + flagTextGap
            val textCenterY = itemTop + cardHeight * 0.5f

            val radioBounds = RectF().apply {
                this.right = itemRight - radioPadRight
                this.left = this.right - radioSize
                this.top = itemTop + (cardHeight - radioSize) * 0.5f
                this.bottom = this.top + radioSize
            }

            cardsList.add(
                LanguageCardLayout(
                    language = selectableLanguages[index],
                    bounds = bounds,
                    flagBounds = flagBounds,
                    textX = textX,
                    textCenterY = textCenterY,
                    radioBounds = radioBounds
                )
            )
        }

        val headerRect = RectF().apply {
            left = panelLeft
            top = panelTop
            right = panelRight
            bottom = gridTopBase
        }

        val gridViewportRect = RectF().apply {
            left = gridLeft
            top = gridTopBase
            right = gridRight
            bottom = footerTop
        }

        return LanguageSelectorLayout(
            scroll = scroll,
            maxScroll = maxScroll,
            panel = languageDeckRect,
            header = headerRect,
            backButton = languageBackButton,
            backButtonHitbox = backHitbox,
            gridViewport = gridViewportRect,
            languageCards = cardsList,
            footer = languageFooterRect,
            columns = columns
        )
    }
}
