package com.moonsolstudios.kavvoro.ui.screens.collection

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionViewportLayoutTest {
    @Test
    fun viewportStartsAfterSortButtonWhenAvailable() {
        assertEquals(126f, CollectionLayoutCalculator.viewportTop(120f, 80f, 1f), 0.001f)
    }

    @Test
    fun viewportUsesHeroStageOrDefaultWhenSortButtonIsMissing() {
        assertEquals(148f, CollectionLayoutCalculator.viewportTop(null, 80f, 1f), 0.001f)
        assertEquals(284f, CollectionLayoutCalculator.viewportTop(null, null, 1f), 0.001f)
    }
}
