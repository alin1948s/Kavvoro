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
}
