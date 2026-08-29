package com.moonsolstudios.kavvoro.ui.controller

import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.MenuState

object MenuNavigationController {

    data class HomeModeSelectionResult(
        val selectedMenuMode: GameMode,
        val menuState: MenuState,
        val changed: Boolean,
        val accentColor: Int,
        val ballVelXDelta: Float,
        val ballVelYDelta: Float
    )

    fun selectHomeMode(
        currentMode: GameMode,
        targetMode: GameMode,
        modeProgress: (GameMode) -> Int
    ): HomeModeSelectionResult {
        val changed = currentMode != targetMode
        val newMenuState = if (currentMode == targetMode && modeProgress(targetMode) > 1) {
            MenuState.MODE_ACTION
        } else {
            MenuState.MODES
        }
        val accent = if (targetMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
        val velXDelta = if (changed) {
            if (targetMode == GameMode.CHAOS) -420f else 420f
        } else 0f
        val velYDelta = if (changed) {
            if (targetMode == GameMode.CHAOS) 160f else -120f
        } else 0f

        return HomeModeSelectionResult(
            selectedMenuMode = targetMode,
            menuState = newMenuState,
            changed = changed,
            accentColor = accent,
            ballVelXDelta = velXDelta,
            ballVelYDelta = velYDelta
        )
    }

    data class ModeSelectionResult(
        val selectedMenuMode: GameMode,
        val menuState: MenuState,
        val shouldStartDirectly: Boolean
    )

    fun selectMode(
        targetMode: GameMode,
        modeProgress: (GameMode) -> Int
    ): ModeSelectionResult {
        return if (modeProgress(targetMode) <= 1) {
            ModeSelectionResult(
                selectedMenuMode = targetMode,
                menuState = MenuState.MODES,
                shouldStartDirectly = true
            )
        } else {
            ModeSelectionResult(
                selectedMenuMode = targetMode,
                menuState = MenuState.MODE_ACTION,
                shouldStartDirectly = false
            )
        }
    }

    data class BackNavigationResult(
        val consumed: Boolean,
        val newScreen: com.moonsolstudios.kavvoro.model.Screen?,
        val newMenuState: MenuState?,
        val transitionAccent: Int?
    )

    fun handleBack(
        screen: com.moonsolstudios.kavvoro.model.Screen,
        menuState: MenuState,
        languageReturnScreen: com.moonsolstudios.kavvoro.model.Screen,
        onExitToMenu: () -> Unit
    ): BackNavigationResult = when (screen) {
        com.moonsolstudios.kavvoro.model.Screen.GAME -> {
            onExitToMenu()
            BackNavigationResult(true, null, null, null)
        }
        com.moonsolstudios.kavvoro.model.Screen.COLLECTION,
        com.moonsolstudios.kavvoro.model.Screen.LEADERBOARDS,
        com.moonsolstudios.kavvoro.model.Screen.SETTINGS ->
            BackNavigationResult(true, com.moonsolstudios.kavvoro.model.Screen.MENU, MenuState.MODES, 0xFF8AA6FF.toInt())
        com.moonsolstudios.kavvoro.model.Screen.LANGUAGE ->
            BackNavigationResult(true, languageReturnScreen, null, 0xFF45F2FF.toInt())
        com.moonsolstudios.kavvoro.model.Screen.MENU -> {
            if (menuState == MenuState.MODE_ACTION) {
                BackNavigationResult(true, null, MenuState.MODES, 0xFF8AA6FF.toInt())
            } else {
                BackNavigationResult(false, null, null, null)
            }
        }
        com.moonsolstudios.kavvoro.model.Screen.AD -> BackNavigationResult(false, null, null, null)
    }
}
