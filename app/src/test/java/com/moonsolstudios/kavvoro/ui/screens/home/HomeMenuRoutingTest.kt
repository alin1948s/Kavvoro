package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeMenuRoutingTest {
    @Test
    fun missionsOpensItsOwnPageAndhypeOpensDailyCheck() {
        val listener = object : HomeMenuActionListener {
            override fun onMenuButtonSelected(button: MenuButton) = Unit
            override fun onDailyRiftClaim() = Unit
            override fun onDailyRiftOpenCollection() = Unit
            override fun onDailyRiftDismissed() = Unit
            override fun performHaptic(feedbackConstant: Int) = Unit
        }
        val calculator = HomeLayoutCalculator().apply {
            calculate(width = 1080f, height = 2400f, displayDensity = 2.625f)
        }
        val controller = HomeMenuTouchController(listener)

        assertEquals(
            MenuButton.MISSIONS,
            controller.hitTestMenuButton(
                calculator.missionsCardRect.centerX(),
                calculator.missionsCardRect.centerY(),
                MenuState.MODES,
                calculator,
                emptyMap()
            )
        )
        assertEquals(
            MenuButton.DAILY_RIFT,
            controller.hitTestMenuButton(
                calculator.hypeChipTouchRect.centerX(),
                calculator.hypeChipTouchRect.centerY(),
                MenuState.MODES,
                calculator,
                emptyMap()
            )
        )

        val synced = HomeMenuTouchController(listener).apply { syncHomeLayoutRects(calculator) }
        assertEquals(
            MenuButton.DAILY_RIFT,
            synced.menuButtonAt(
                calculator.hypeChipTouchRect.centerX(),
                calculator.hypeChipTouchRect.centerY(),
                MenuState.MODES,
                menuPrivacyButton = RectF(),
                menuSfxButton = RectF()
            )
        )
    }
}
