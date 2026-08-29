package com.moonsolstudios.kavvoro.ui

import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage

data class TutorialHorizontalBounds(
    val left: Float,
    val right: Float
) {
    val center: Float
        get() = (left + right) * 0.5f
}

data class HudVerticalBounds(
    val energyTop: Float,
    val energyBottom: Float,
    val titleTop: Float,
    val titleBottom: Float
) {
    val gap: Float
        get() = titleTop - energyBottom
}

object TutorialCardLayout {
    fun centeredHorizontalBounds(
        cardLeft: Float,
        cardRight: Float,
        padding: Float
    ): TutorialHorizontalBounds = TutorialHorizontalBounds(
        left = cardLeft + padding,
        right = cardRight - padding
    )

    fun localeSafeHorizontalBounds(
        cardLeft: Float,
        cardRight: Float,
        padding: Float,
        language: KavvoroLanguage
    ): TutorialHorizontalBounds = centeredHorizontalBounds(
        cardLeft = cardLeft,
        cardRight = cardRight,
        padding = LocaleLayoutPolicy.safeHorizontalPadding(padding, language)
    )

    fun fittedTextSize(
        startSize: Float,
        minSize: Float,
        maxWidth: Float,
        maxMeasuredWidth: Float
    ): Float {
        if (maxMeasuredWidth <= maxWidth || maxMeasuredWidth <= 0f) return startSize
        return (startSize * maxWidth * TEXT_FIT_SAFETY / maxMeasuredWidth)
            .coerceIn(minSize, startSize)
    }

    fun hudVerticalBounds(
        energyTop: Float,
        energyHeight: Float,
        titleHeight: Float,
        titleBottomInset: Float,
        hudBottom: Float,
        minimumGap: Float
    ): HudVerticalBounds {
        val energyBottom = energyTop + energyHeight
        val titleTop = maxOf(
            hudBottom - titleHeight - titleBottomInset,
            energyBottom + minimumGap
        )
        return HudVerticalBounds(
            energyTop = energyTop,
            energyBottom = energyBottom,
            titleTop = titleTop,
            titleBottom = titleTop + titleHeight
        )
    }

    fun minimumHudBottom(
        energyTop: Float,
        energyHeight: Float,
        titleHeight: Float,
        titleBottomInset: Float,
        minimumGap: Float
    ): Float = energyTop + energyHeight + minimumGap + titleHeight + titleBottomInset

    private const val TEXT_FIT_SAFETY = 0.95f
}
