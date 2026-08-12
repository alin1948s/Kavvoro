package com.moonsolstudios.kavvoro.ui

data class TutorialHorizontalBounds(
    val left: Float,
    val right: Float
) {
    val center: Float
        get() = (left + right) * 0.5f
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

    private const val TEXT_FIT_SAFETY = 0.95f
}
