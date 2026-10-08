package com.moonsolstudios.kavvoro.ui.screens.collection

import android.graphics.RectF
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.CollectionSort
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockRule
import com.moonsolstudios.kavvoro.model.UnlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionSortingAndFilterTest {

    private fun testSkin(
        id: String,
        name: String,
        power: BallPower = BallPower.NONE,
        unlockType: UnlockType = UnlockType.HYPE_COST,
        value: Int = 1000,
        style: SkinStyle = SkinStyle.CLASSIC
    ) = BallSkin(
        id = id,
        name = name,
        subtitle = "Test Subtitle",
        primary = 0xFFFFFFFF.toInt(),
        secondary = 0xFF000000.toInt(),
        lineColor = 0xFF1DE8C8.toInt(),
        style = style,
        unlock = UnlockRule(unlockType, value, "label"),
        power = power
    )

    private val sampleSkins = listOf(
        testSkin("classic", "Alpha", unlockType = UnlockType.DEFAULT),
        testSkin("powered", "Beta", power = BallPower.PRISM_SHIELD, unlockType = UnlockType.HYPE_COST, value = 5000),
        testSkin("premium", "Gamma", unlockType = UnlockType.PREMIUM, value = 99),
        testSkin("cosmetic", "Delta", style = SkinStyle.CROWN, unlockType = UnlockType.HYPE_COST, value = 15000)
    )

    @Test
    fun sortCycleTransitionsThroughAllOptions() {
        var current = CollectionSort.AURA_DESC
        current = current.next()
        assertEquals(CollectionSort.RARITY, current)
        current = current.next()
        assertEquals(CollectionSort.POWER_FIRST, current)
        current = current.next()
        assertEquals(CollectionSort.NAME_ASC, current)
        current = current.next()
        assertEquals(CollectionSort.OWNED_FIRST, current)
        current = current.next()
        assertEquals(CollectionSort.AURA_DESC, current)
    }

    @Test
    fun filterMatchesOwnedCorrectly() {
        val unlockedIds = setOf("classic", "powered")
        val isUnlocked: (BallSkin) -> Boolean = { it.id in unlockedIds }

        assertTrue(CollectionTouchController.filterMatches(CollectionFilter.OWNED, sampleSkins[0], isUnlocked))
        assertTrue(CollectionTouchController.filterMatches(CollectionFilter.OWNED, sampleSkins[1], isUnlocked))
        assertFalse(CollectionTouchController.filterMatches(CollectionFilter.OWNED, sampleSkins[2], isUnlocked))
        assertFalse(CollectionTouchController.filterMatches(CollectionFilter.OWNED, sampleSkins[3], isUnlocked))
    }

    @Test
    fun filterMatchesSuperpowerCorrectly() {
        val isUnlocked: (BallSkin) -> Boolean = { true }
        assertFalse(CollectionTouchController.filterMatches(CollectionFilter.SUPERPOWER, sampleSkins[0], isUnlocked))
        assertTrue(CollectionTouchController.filterMatches(CollectionFilter.SUPERPOWER, sampleSkins[1], isUnlocked))
        assertFalse(CollectionTouchController.filterMatches(CollectionFilter.SUPERPOWER, sampleSkins[2], isUnlocked))
        assertFalse(CollectionTouchController.filterMatches(CollectionFilter.SUPERPOWER, sampleSkins[3], isUnlocked))
    }

    @Test
    fun sortedFilteredIndexesOrdersAlphabetically() {
        val indexes = CollectionTouchController.sortedFilteredIndexes(
            ballSkins = sampleSkins,
            filter = CollectionFilter.ALL,
            sort = CollectionSort.NAME_ASC,
            isSkinUnlocked = { true }
        )
        val names = indexes.map { sampleSkins[it].name }
        assertEquals(listOf("Alpha", "Beta", "Delta", "Gamma"), names)
    }

    @Test
    fun sortedFilteredIndexesOrdersOwnedFirst() {
        val unlockedIds = setOf("cosmetic", "powered")
        val indexes = CollectionTouchController.sortedFilteredIndexes(
            ballSkins = sampleSkins,
            filter = CollectionFilter.ALL,
            sort = CollectionSort.OWNED_FIRST,
            isSkinUnlocked = { it.id in unlockedIds }
        )
        val firstTwoIds = indexes.take(2).map { sampleSkins[it].id }.toSet()
        assertEquals(setOf("cosmetic", "powered"), firstTwoIds)
    }

    @Test
    fun layoutCollectionComputesVerticalPortraitPodsAndSortButton() {
        val backButton = RectF()
        val restoreButton = RectF()
        val filterRects = List(CollectionFilter.entries.size) { RectF() }
        val itemRects = mutableListOf<RectF>()

        val dp = 2.0f
        val contentLeft = 20f * dp
        val contentRight = 380f * dp

        val (scroll, maxScroll) = CollectionTouchController.layoutCollection(
            contentLeft = contentLeft,
            contentRight = contentRight,
            safeTop22 = 22f * dp,
            safeTop68 = 68f * dp,
            safeTop88 = 88f * dp,
            safeTop192 = 192f * dp,
            viewportTop = 270f * dp,
            viewportBottom = 700f * dp,
            scroll = 0f,
            ballSkins = sampleSkins,
            filter = CollectionFilter.ALL,
            sort = CollectionSort.AURA_DESC,
            isSkinUnlocked = { true },
            dp = dp,
            backButton = backButton,
            restoreButton = restoreButton,
            filterRects = filterRects,
            itemRects = itemRects
        )

        assertEquals("Back button should use the shared leading-edge position", contentLeft, backButton.left, 0.01f)
        assertEquals(contentLeft + 42f * dp, backButton.right, 0.01f)
        assertEquals("Restore button should preserve its trailing inset", contentRight - 50f * dp, restoreButton.right, 0.01f)
        assertTrue("Header controls should not overlap", backButton.right < restoreButton.left)
        assertFalse("sortButtonRect should not be empty", CollectionTouchController.sortButtonRect.isEmpty)

        val firstPod = itemRects[0]
        val podW = firstPod.right - firstPod.left
        val podH = firstPod.bottom - firstPod.top
        assertTrue("first pod width should be positive", podW > 0f)
        assertTrue("pod height ($podH) should exceed width ($podW) for vertical holo-pods", podH > podW)
    }
}
