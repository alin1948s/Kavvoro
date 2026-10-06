package com.moonsolstudios.kavvoro.ui.screens.modals

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabletOrientationPromptControllerTest {

    @Test
    fun initialStateIsHiddenAndUnacknowledged() {
        val controller = TabletOrientationPromptController()
        assertFalse(controller.isTabletLandscape)
        assertFalse(controller.isModalVisible)
        assertFalse(controller.isDismissed)
    }

    @Test
    fun forceShowModalOpensModalWhenInTabletLandscape() {
        val controller = TabletOrientationPromptController()
        controller.onOrientationStateChanged(true)

        controller.forceShowModal()
        assertTrue(controller.isModalVisible)
        assertFalse(controller.isDismissed)
    }

    @Test
    fun forceShowModalIgnoredWhenNotInTabletLandscape() {
        val controller = TabletOrientationPromptController()
        controller.forceShowModal()
        assertFalse(controller.isModalVisible)
    }

    @Test
    fun enteringTabletLandscapeShowsModalAndArmsPrompt() {
        val controller = TabletOrientationPromptController()
        controller.onOrientationStateChanged(true)

        assertTrue(controller.isTabletLandscape)
        assertTrue(controller.isModalVisible)
        assertFalse(controller.isDismissed)
    }

    @Test
    fun rotatingToPortraitHidesPromptAndResetsDismissedState() {
        val controller = TabletOrientationPromptController()
        controller.onOrientationStateChanged(true)
        assertTrue(controller.isModalVisible)

        // Rotate to portrait
        controller.onOrientationStateChanged(false)
        assertFalse(controller.isTabletLandscape)
        assertFalse(controller.isModalVisible)
        assertFalse(controller.isDismissed)
        assertTrue(controller.modalCardRect.left == 0f && controller.modalCardRect.right == 0f)
        assertTrue(controller.dismissButtonRect.left == 0f && controller.dismissButtonRect.right == 0f)
        assertTrue(controller.persistentBadgeRect.left == 0f && controller.persistentBadgeRect.right == 0f)
    }

    @Test
    fun rotatingBackToLandscapeReArmsAndShowsModalAgain() {
        val controller = TabletOrientationPromptController()
        // 1. Initial landscape: modal shows
        controller.onOrientationStateChanged(true)
        assertTrue(controller.isModalVisible)
        assertFalse(controller.isDismissed)

        // 2. User simulates dismissing modal
        controller.dismissButtonRect.left = 100f
        controller.dismissButtonRect.top = 100f
        controller.dismissButtonRect.right = 200f
        controller.dismissButtonRect.bottom = 150f
        val dismissField = TabletOrientationPromptController::class.java.getDeclaredField("isDismissed")
        dismissField.isAccessible = true
        dismissField.set(controller, true)
        val visibleField = TabletOrientationPromptController::class.java.getDeclaredField("isModalVisible")
        visibleField.isAccessible = true
        visibleField.set(controller, false)

        assertFalse(controller.isModalVisible)
        assertTrue(controller.isDismissed)

        // 3. User rotates tablet to portrait (tall screen)
        controller.onOrientationStateChanged(false)
        assertFalse(controller.isTabletLandscape)
        assertFalse(controller.isModalVisible)
        assertFalse(controller.isDismissed)

        // 4. User rotates tablet back to landscape (thumbnail mode)
        // MUST REAPPEAR!
        controller.onOrientationStateChanged(true)
        assertTrue("Modal must reappear when returning to thumbnail mode", controller.isModalVisible)
        assertFalse("isDismissed must be false when returning to thumbnail mode", controller.isDismissed)
    }

    @Test
    fun onResumeInLandscapeGuaranteesModalVisibility() {
        val controller = TabletOrientationPromptController()
        controller.onOrientationStateChanged(true)

        // Dismiss it
        val dismissField = TabletOrientationPromptController::class.java.getDeclaredField("isDismissed")
        dismissField.isAccessible = true
        dismissField.set(controller, true)
        val visibleField = TabletOrientationPromptController::class.java.getDeclaredField("isModalVisible")
        visibleField.isAccessible = true
        visibleField.set(controller, false)

        // Resume should re-arm
        controller.onResume()
        assertTrue(controller.isModalVisible)
        assertFalse(controller.isDismissed)
    }

    @Test
    fun translationsProvideNonEmptyStringsForAllLanguages() {
        com.moonsolstudios.kavvoro.i18n.KavvoroLanguage.entries.forEach { lang ->
            assertTrue(TabletPromptTranslations.getTitle(lang).isNotEmpty())
            assertTrue(TabletPromptTranslations.getDescription(lang).isNotEmpty())
            assertTrue(TabletPromptTranslations.getDismissButton(lang).isNotEmpty())
            assertTrue(TabletPromptTranslations.getBadgeText(lang).isNotEmpty())
            assertTrue(TabletPromptTranslations.getEyebrow(lang).isNotEmpty())
        }
    }
}
