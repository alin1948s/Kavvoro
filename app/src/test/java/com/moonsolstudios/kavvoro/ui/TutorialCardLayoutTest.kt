package com.moonsolstudios.kavvoro.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TutorialCardLayoutTest {
    @Test
    fun actionUsesEqualCardMarginsAndSharesTheCardCenter() {
        val bounds = TutorialCardLayout.centeredHorizontalBounds(
            cardLeft = 40f,
            cardRight = 620f,
            padding = 14f
        )

        assertEquals(54f, bounds.left, 0.001f)
        assertEquals(606f, bounds.right, 0.001f)
        assertEquals(330f, bounds.center, 0.001f)
    }
}
