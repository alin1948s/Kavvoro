package com.moonsolstudios.kavvoro.ui.home

import com.moonsolstudios.kavvoro.model.Brainball
import com.moonsolstudios.kavvoro.model.LayoutMode
import com.moonsolstudios.kavvoro.repository.BrainballRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class HomeResponsiveLayoutTest {

    private data class ResolutionTestSpec(
        val name: String,
        val width: Float,
        val height: Float,
        val density: Float,
        val expectedMode: LayoutMode
    )

    private val testMatrix = listOf(
        ResolutionTestSpec("360x800 (Compact Phone)", 360f, 800f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("412x915 (Compact Phone)", 412f, 915f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("480x854 (Compact Phone)", 480f, 854f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("600x1024 (Medium)", 600f, 1024f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("720x1280 (Medium)", 720f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("800x1280 (Medium)", 800f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("1024x1366 (Tablet)", 1024f, 1366f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1080x2400 (High DPI Phone)", 1080f, 2400f, 2.75f, LayoutMode.COMPACT),
        ResolutionTestSpec("1200x1920 (Tablet)", 1200f, 1920f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1536x2048 (Tablet)", 1536f, 2048f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1600x2560 (Tablet)", 1600f, 2560f, 1f, LayoutMode.TABLET)
    )

    @Test
    fun testAllElevenResolutionsInMatrix() {
        val calculator = HomeLayoutCalculator()
        val brainball = BrainballRepository.getById("kavvoro")

        for (spec in testMatrix) {
            calculator.calculate(
                width = spec.width,
                height = spec.height,
                displayDensity = spec.density,
                brainball = brainball
            )

            // 1. Verify LayoutMode
            assertEquals(
                "Resolution ${spec.name} layoutMode mismatch",
                spec.expectedMode,
                calculator.layoutMode
            )

            // 2. Verify contentRect is valid and centered
            val contentRect = calculator.contentRect
            assertTrue("contentRect width must be positive on ${spec.name}", contentRect.width() > 0f)
            assertTrue("contentRect width must not exceed screen width on ${spec.name}", contentRect.width() <= spec.width)
            val expectedLeft = (spec.width - contentRect.width()) / 2f
            assertTrue(
                "contentRect left must be centered on ${spec.name}",
                abs(contentRect.left - expectedLeft) < 0.1f
            )

            // 3. Verify Header bounds
            val brandRect = calculator.brandRect
            assertTrue("brandRect width must be positive on ${spec.name}", brandRect.width() > 0f)
            assertTrue("brandRect height must be positive on ${spec.name}", brandRect.height() > 0f)
            assertTrue("Settings button must be within content bounds on ${spec.name}", calculator.settingsButtonRect.right <= contentRect.right + 0.1f)
            assertTrue("Sound button must be within content bounds on ${spec.name}", calculator.soundButtonRect.right <= contentRect.right + 0.1f)

            // 4. Verify Stats Cards
            for (i in 0 until 4) {
                val card = calculator.statCardRects[i]
                assertTrue("Stat card $i must have positive width on ${spec.name}", card.width() > 0f)
                assertTrue("Stat card $i must be within contentRect on ${spec.name}", card.left >= contentRect.left - 0.1f && card.right <= contentRect.right + 0.1f)
            }

            // 5. Verify Hero, Portal, Platform, and Character bounds
            val portalRect = calculator.portalRect
            val platformRect = calculator.platformRect
            val charRect = calculator.characterRect

            assertTrue("Portal width must be positive on ${spec.name}", portalRect.width() > 0f)
            assertTrue("Portal must be horizontally centered on ${spec.name}", abs(portalRect.centerX() - contentRect.centerX()) < 0.1f)
            assertTrue("Platform width must be positive on ${spec.name}", platformRect.width() > 0f)
            assertTrue("Platform must be horizontally centered on ${spec.name}", abs(platformRect.centerX() - contentRect.centerX()) < 0.1f)
            assertTrue("Character must have positive dimensions on ${spec.name}", charRect.width() > 0f && charRect.height() > 0f)

            // 6. Verify Play CTA
            val playRect = calculator.playCtaRect
            assertTrue("Play CTA must be positive on ${spec.name}", playRect.width() > 0f && playRect.height() > 0f)
            assertTrue("Play CTA must fit within contentRect on ${spec.name}", playRect.left >= contentRect.left - 0.1f && playRect.right <= contentRect.right + 0.1f)

            // 7. Verify Lower Navigation Cards
            val leaderboards = calculator.leaderboardsCardRect
            val vault = calculator.vaultCardRect
            val collection = calculator.collectionCardRect

            assertTrue("Leaderboards card must be positive on ${spec.name}", leaderboards.width() > 0f)
            assertTrue("Vault card must be positive on ${spec.name}", vault.width() > 0f)
            assertTrue("Collection card must be positive on ${spec.name}", collection.width() > 0f)

            if (spec.expectedMode == LayoutMode.TABLET) {
                // In tablet mode, Vault and Collection are side by side
                assertTrue("Vault and Collection must be side-by-side on tablet ${spec.name}", vault.right < collection.left)
                assertEquals("Vault and Collection should have same top on tablet ${spec.name}", vault.top, collection.top, 0.1f)
                assertEquals("Leaderboards should be full width on tablet ${spec.name}", contentRect.width(), leaderboards.width(), 0.1f)
            } else {
                // In phone mode, cards are stacked vertically
                assertTrue("Leaderboards must be above Vault on ${spec.name}", leaderboards.bottom <= vault.top + 0.1f)
                assertTrue("Vault must be above Collection on ${spec.name}", vault.bottom <= collection.top + 0.1f)
            }
        }
    }

    @Test
    fun testBrainballRepositoryRetrievalAndDefaults() {
        val kavvoro = BrainballRepository.getById("kavvoro")
        assertNotNull(kavvoro)
        assertEquals("Kavvoro", kavvoro.name)

        val nodlo = BrainballRepository.getById("nodlo")
        assertNotNull(nodlo)
        assertEquals("Kavvoro", nodlo.name)

        val prismKing = BrainballRepository.getById("prism_king")
        assertNotNull(prismKing)
        assertEquals("Prism King", prismKing.name)

        val fallback = BrainballRepository.getById("non_existent_id")
        assertNotNull(fallback)
        assertEquals("kavvoro", fallback.id)
    }

    @Test
    fun testDynamicBrainballOffsetsAndScaling() {
        val calculator = HomeLayoutCalculator()
        val customBrainball = Brainball(
            id = "custom_test",
            name = "Custom Test",
            drawableRes = 0,
            homeScale = 1.2f,
            homeOffsetX = 0.05f,
            homeOffsetY = -0.02f
        )

        calculator.calculate(
            width = 412f,
            height = 915f,
            displayDensity = 1f,
            brainball = customBrainball
        )

        val portal = calculator.portalRect
        val charRect = calculator.characterRect

        val expectedSize = portal.width() * 0.54f * 1.2f
        assertEquals("Character width should scale by homeScale", expectedSize, charRect.width(), 0.1f)

        assertEquals("Character centerX should match portal centerX", portal.centerX(), charRect.centerX(), 0.1f)
        assertEquals("Character centerY should match portal centerY", portal.centerY(), charRect.centerY(), 0.1f)
    }
}
