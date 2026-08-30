package com.moonsolstudios.kavvoro.ui.tutorial

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun lessonTextShrinksUniformlyToFitTheLongestLocalizedLine() {
        assertEquals(
            8.36f,
            TutorialCardLayout.fittedTextSize(
                startSize = 11f,
                minSize = 7.2f,
                maxWidth = 400f,
                maxMeasuredWidth = 500f
            ),
            0.001f
        )
        assertEquals(
            7.2f,
            TutorialCardLayout.fittedTextSize(
                startSize = 11f,
                minSize = 7.2f,
                maxWidth = 400f,
                maxMeasuredWidth = 900f
            ),
            0.001f
        )
    }

    @Test
    fun hudTitleKeepsBreathingRoomBelowRiftEnergy() {
        val bounds = TutorialCardLayout.hudVerticalBounds(
            energyTop = 62f,
            energyHeight = 13f,
            titleHeight = 22f,
            titleBottomInset = 7f,
            hudBottom = 104f,
            minimumGap = 12f
        )

        assertEquals(75f, bounds.energyBottom, 0.001f)
        assertEquals(87f, bounds.titleTop, 0.001f)
        assertEquals(12f, bounds.gap, 0.001f)
        assertTrue(bounds.titleBottom > 104f)
    }

    @Test
    fun hudBottomIncludesEnergyTitleGapAndBottomInset() {
        assertEquals(
            116f,
            TutorialCardLayout.minimumHudBottom(
                energyTop = 62f,
                energyHeight = 13f,
                titleHeight = 22f,
                titleBottomInset = 7f,
                minimumGap = 12f
            ),
            0.001f
        )
    }

    @Test
    fun arabicCardContentUsesAWiderSafeInsetWithoutChangingHudSpacing() {
        val bounds = TutorialCardLayout.localeSafeHorizontalBounds(
            cardLeft = 40f,
            cardRight = 620f,
            padding = 14f,
            language = com.moonsolstudios.kavvoro.i18n.KavvoroLanguage.AR
        )

        assertEquals(58f, bounds.left, 0.001f)
        assertEquals(602f, bounds.right, 0.001f)
        assertEquals(75f, TutorialCardLayout.hudVerticalBounds(62f, 13f, 22f, 7f, 104f, 12f).energyBottom, 0.001f)
    }
}
