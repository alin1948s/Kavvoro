package com.moonsolstudios.kavvoro.ui.screens.gameplay

import android.graphics.RectF
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.PortalPair
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockRule
import com.moonsolstudios.kavvoro.model.UnlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayModularRendererTest {

    @Test
    fun testGameplayBallScaleRules() {
        val standardSkin = BallSkin(
            id = "test_std",
            name = "Test Std",
            subtitle = "Subtitle",
            primary = 0xFFFFFFFF.toInt(),
            secondary = 0xFF000000.toInt(),
            lineColor = 0xFF222222.toInt(),
            style = SkinStyle.CLASSIC,
            unlock = UnlockRule(UnlockType.DEFAULT, 0, "FREE"),
            power = BallPower.NONE
        )

        val powerSkin = standardSkin.copy(
            id = "test_pow",
            power = BallPower.PRISM_SHIELD
        )

        val premiumSkin = standardSkin.copy(
            id = "test_prem",
            unlock = UnlockRule(UnlockType.PREMIUM, 0, "STORE")
        )

        assertEquals(1.34f, GameplayArenaRenderer.gameplayBallScale(premiumSkin, 0), 0.001f)
        assertEquals(1.29f, GameplayArenaRenderer.gameplayBallScale(powerSkin, 0), 0.001f)
        assertEquals(1.25f, GameplayArenaRenderer.gameplayBallScale(standardSkin, 40), 0.001f)
        assertEquals(1.2f, GameplayArenaRenderer.gameplayBallScale(standardSkin, 5), 0.001f)
    }

    @Test
    fun testTutorialAnchorPointClampedToStageBounds() {
        val anchor = GameplayArenaRenderer.tutorialAnchorPoint(
            start = Point2(2f, 3f),
            goal = Point2(8f, 15f),
            pulseTarget = null,
            portal = null,
            orbit = 0f,
            hasFocusField = false,
            hasRiftWind = false,
            stageHeight = 20f
        )

        assertTrue("Anchor X must be within stage bounds", anchor.x in 0.8f..9.2f)
        assertTrue("Anchor Y must be within stage bounds", anchor.y in 1.2f..18.8f)
    }

    @Test
    fun testTutorialAnchorPointPrioritizesPortal() {
        val portal = PortalPair(
            entry = Point2(4f, 5f),
            exit = Point2(7f, 12f)
        )
        val withPortal = GameplayArenaRenderer.tutorialAnchorPoint(
            start = Point2(1f, 1f),
            goal = Point2(9f, 18f),
            pulseTarget = Point2(5f, 5f),
            portal = portal,
            orbit = 0f,
            hasFocusField = true,
            hasRiftWind = true,
            stageHeight = 20f
        )

        // Portal entry is weighted 0.65f vs start 0.35f
        val expectedBaseX = 1f * 0.35f + 4f * 0.65f
        val expectedBaseY = 1f * 0.35f + 5f * 0.65f
        assertEquals(expectedBaseX + 0.32f, withPortal.x, 0.05f)
        assertEquals(expectedBaseY, withPortal.y, 0.05f)
    }

    @Test
    fun testHudMetricsAndDimensions() {
        val dp = 2.5f

        // Compact HUD threshold is 520dp
        assertTrue(GameplayHudRenderer.isCompactHud(500f * dp, dp))
        assertFalse(GameplayHudRenderer.isCompactHud(530f * dp, dp))

        // Ribbon presence depends on power or curses
        assertFalse(GameplayHudRenderer.hudHasRibbon(BallPower.NONE, false))
        assertTrue(GameplayHudRenderer.hudHasRibbon(BallPower.PRISM_SHIELD, false))
        assertTrue(GameplayHudRenderer.hudHasRibbon(BallPower.NONE, true))

        // Bottom heights
        val compactNoRibbon = GameplayHudRenderer.gameplayHudBottom(isCompact = true, hasRibbon = false, dp = dp)
        val compactWithRibbon = GameplayHudRenderer.gameplayHudBottom(isCompact = true, hasRibbon = true, dp = dp)
        val expandedNoRibbon = GameplayHudRenderer.gameplayHudBottom(isCompact = false, hasRibbon = false, dp = dp)

        assertEquals(dp * 104f, compactNoRibbon, 0.001f)
        assertEquals(dp * 132f, compactWithRibbon, 0.001f)
        assertEquals(dp * 156f, expandedNoRibbon, 0.001f)
    }

    @Test
    fun testHudControlsLeftDetection() {
        val btn1 = com.moonsolstudios.kavvoro.ui.screens.home.LayoutRect(700f, 10f, 750f, 60f)
        val btn2 = com.moonsolstudios.kavvoro.ui.screens.home.LayoutRect(620f, 10f, 670f, 60f)
        val emptyBtn = com.moonsolstudios.kavvoro.ui.screens.home.LayoutRect()

        val left = GameplayHudRenderer.hudControlsLeftBounds(listOf(btn1, btn2, emptyBtn), 800f)
        assertEquals(620f, left, 0.001f)

        val allEmpty = GameplayHudRenderer.hudControlsLeftBounds(listOf(emptyBtn), 800f)
        assertEquals(800f, allEmpty, 0.001f)
    }

    @Test
    fun testModeWarnings() {
        val dummyLevel = LevelDirector.createClassic(1, 20f)

        // Portal warning takes precedence when portals exist
        val portalLevel = dummyLevel.copy(
            portals = listOf(PortalPair(Point2(2f, 2f), Point2(4f, 4f)))
        )
        val portalWarning = GameplayHudRenderer.currentModeWarning(portalLevel) { false }
        assertNotNull(portalWarning)
        assertEquals("PORTAL RIFT!", portalWarning?.title)

        // Curse combination warnings
        val windAndOverheat = GameplayHudRenderer.currentModeWarning(dummyLevel) {
            it == CurseType.RIFT_WIND || it == CurseType.OVERHEAT
        }
        assertNotNull(windAndOverheat)
        assertEquals("WIND + OVERHEAT!", windAndOverheat?.title)

        // Single curse warning
        val singleDrain = GameplayHudRenderer.currentModeWarning(dummyLevel) {
            it == CurseType.RIFT_DRAIN
        }
        assertNotNull(singleDrain)
        assertEquals("RIFT DRAIN!", singleDrain?.title)

        // No curses, no portals -> null
        val none = GameplayHudRenderer.currentModeWarning(dummyLevel) { false }
        assertNull(none)
    }
}
