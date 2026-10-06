package com.moonsolstudios.kavvoro.ui.screens.home

import com.moonsolstudios.kavvoro.model.LayoutMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards audit item UI-3.
 *
 * The portrait header scaled its gear and stat chips with `heightDp / 800f`, so a 38dp design
 * size became ~27dp on short phones, and the banner card is only ~42dp tall by design. Those
 * rects were also published as the TalkBack bounds, so the shortfall hit touch and
 * explore-by-touch alike.
 *
 * The fix keeps the visual geometry (growing it flipped the reference 411dp phone from one chip
 * row to two) and grows separate touch rects instead. These tests pin both halves.
 */
class HomeTouchTargetTest {

    private data class Device(
        val name: String,
        val width: Float,
        val height: Float,
        val density: Float
    )

    private val devices = listOf(
        Device("1080x2400 @2.625 (compact phone)", 1080f, 2400f, 2.625f),
        Device("1080x2340 @2.75 (tall compact phone)", 1080f, 2340f, 2.75f),
        Device("720x1280 @2.0 (small phone)", 720f, 1280f, 2f),
        Device("720x1080 @2.0 (short phone)", 720f, 1080f, 2f),
        Device("600x1024 @2.0 (narrow phone)", 600f, 1024f, 2f),
        Device("1080x1920 @1.5 (medium phone)", 1080f, 1920f, 1.5f),
        Device("1600x2560 @2.0 (tablet)", 1600f, 2560f, 2f),
        Device("800x1280 @1.0 (density 1 tablet)", 800f, 1280f, 1f)
    )

    private val minimumDp = HomeLayoutCalculator.MIN_TOUCH_TARGET_DP

    private fun dp(value: Float, device: Device): Float = value / device.density

    /** [minWidth]/[minHeight] default to the platform minimum; pass 0f to skip an axis. */
    private fun assertSizeAtLeast(
        label: String,
        device: Device,
        width: Float,
        height: Float,
        minWidth: Float = minimumDp,
        minHeight: Float = minimumDp
    ) {
        assertTrue(
            "$label is ${dp(width, device)}x${dp(height, device)}dp on ${device.name}, " +
                "below the ${minWidth}x${minHeight}dp minimum",
            dp(width, device) >= minWidth - 0.01f && dp(height, device) >= minHeight - 0.01f
        )
    }

    @Test
    fun gearButtonMeetsTheMinimumTouchTargetOnEveryDevice() {
        val calculator = HomeLayoutCalculator()
        for (device in devices) {
            calculator.calculate(device.width, device.height, device.density)
            assertSizeAtLeast(
                "settingsTouchRect",
                device,
                calculator.settingsTouchRect.width(),
                calculator.settingsTouchRect.height()
            )
        }
    }

    @Test
    fun bannerCardMeetsTheMinimumTouchTargetOnEveryDevice() {
        val calculator = HomeLayoutCalculator()
        for (device in devices) {
            calculator.calculate(device.width, device.height, device.density)
            assertSizeAtLeast(
                "bannerTouchRect",
                device,
                calculator.bannerTouchRect.width(),
                calculator.bannerTouchRect.height()
            )
        }
    }

    @Test
    fun statChipsReachTheMinimumTouchHeight() {
        val calculator = HomeLayoutCalculator()
        for (device in devices) {
            calculator.calculate(device.width, device.height, device.density)

            val chips = listOf(
                "streakChipTouchRect" to calculator.streakChipTouchRect,
                "levelChipTouchRect" to calculator.levelChipTouchRect,
                "coinsChipTouchRect" to calculator.coinsChipTouchRect
            )
            for ((label, rect) in chips) {
                // Height is fully corrected; width can only grow into the few dp between chips.
                assertSizeAtLeast(label, device, rect.width(), rect.height(), minWidth = 0f)
            }
        }
    }

    @Test
    fun touchTargetsNeverOverlapEachOther() {
        val calculator = HomeLayoutCalculator()
        for (device in devices) {
            calculator.calculate(device.width, device.height, device.density)

            val streak = calculator.streakChipTouchRect
            val level = calculator.levelChipTouchRect
            val coins = calculator.coinsChipTouchRect

            assertFalse(
                "streak and level touch targets overlap on ${device.name}",
                streak.intersects(level)
            )
            assertFalse(
                "level and coins touch targets overlap on ${device.name}",
                level.intersects(coins)
            )
            assertFalse(
                "streak and coins touch targets overlap on ${device.name}",
                streak.intersects(coins)
            )
            // The gear is deliberately allowed to reach over the chips: it is hit-tested first and
            // the chips are read-only, and without that reach it cannot reach 48dp on narrow
            // phones where the chips come close to the content edge.
        }
    }

    @Test
    fun touchTargetsCoverTheirVisualRects() {
        val calculator = HomeLayoutCalculator()
        for (device in devices) {
            calculator.calculate(device.width, device.height, device.density)

            val pairs = listOf(
                "settings" to (calculator.settingsButtonRect to calculator.settingsTouchRect),
                "streak chip" to (calculator.streakChipRect to calculator.streakChipTouchRect),
                "level chip" to (calculator.levelChipRect to calculator.levelChipTouchRect),
                "coins chip" to (calculator.coinsChipRect to calculator.coinsChipTouchRect),
                "banner" to (calculator.bannerCardRect to calculator.bannerTouchRect)
            )
            for ((label, rects) in pairs) {
                val (visual, touch) = rects
                assertTrue(
                    "$label touch target must cover its visual rect on ${device.name}",
                    touch.left <= visual.left + 0.1f &&
                        touch.top <= visual.top + 0.1f &&
                        touch.right >= visual.right - 0.1f &&
                        touch.bottom >= visual.bottom - 0.1f
                )
            }
        }
    }

    @Test
    fun headerCompositionKeepsDesignedSizesWhileTouchTargetsReachTheMinimum() {
        val calculator = HomeLayoutCalculator()
        // Regression guard: growing the visual gear instead of its touch rect ate ~20dp of hero
        // space on compact phones, and the visual 38dp gear is what the reference design shows.
        val compactPhones = devices.filter { device ->
            device.width / device.density <= 480f
        }
        for (device in compactPhones) {
            calculator.calculate(device.width, device.height, device.density)

            val visualHeightDp = dp(calculator.settingsButtonRect.height(), device)
            val touchHeightDp = dp(calculator.settingsTouchRect.height(), device)
            assertTrue(
                "the visual gear must keep its designed size on ${device.name} (was ${visualHeightDp}dp)",
                visualHeightDp < minimumDp - 0.01f
            )
            assertTrue(
                "the gear touch target must still reach ${minimumDp}dp on ${device.name} " +
                    "(was ${touchHeightDp}dp)",
                touchHeightDp >= minimumDp - 0.01f
            )
        }
    }

    @Test
    fun headerControlsStayInsideTheirBounds() {
        val calculator = HomeLayoutCalculator()
        for (device in devices) {
            calculator.calculate(device.width, device.height, device.density)

            val content = calculator.contentRect
            assertTrue(
                "settings visual rect must stay inside content width on ${device.name}",
                calculator.settingsButtonRect.right <= content.right + 0.1f &&
                    calculator.settingsButtonRect.left >= content.left - 0.1f
            )
            assertTrue(
                "the gear touch target must stay on screen on ${device.name}",
                calculator.settingsTouchRect.left >= -0.1f &&
                    calculator.settingsTouchRect.right <= device.width + 0.1f
            )
            assertTrue(
                "brandRect must not collide with settings on ${device.name}",
                calculator.brandRect.right <= calculator.settingsButtonRect.left + 0.1f
            )
            assertTrue(
                "the header must stay above the hero stage on ${device.name}",
                calculator.headerRect.bottom <= calculator.heroRect.top + 0.1f
            )
        }
    }

    @Test
    fun compactPhonesAndTabletsKeepTheirLayoutMode() {
        val calculator = HomeLayoutCalculator()

        calculator.calculate(1080f, 2400f, 2.625f)
        assertTrue(
            "a 411dp-wide phone must stay COMPACT",
            calculator.layoutMode == LayoutMode.COMPACT
        )

        calculator.calculate(1600f, 2560f, 2f)
        assertTrue(
            "an 800dp-wide density-2 tablet must stay TABLET",
            calculator.layoutMode == LayoutMode.TABLET
        )
    }
}