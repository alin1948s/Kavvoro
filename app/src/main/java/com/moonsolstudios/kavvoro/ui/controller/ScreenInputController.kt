package com.moonsolstudios.kavvoro.ui.controller

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.model.Screen

object ScreenInputController {

    fun buttonAt(
        x: Float,
        y: Float,
        screen: Screen,
        state: GameState,
        adButton: RectF,
        resultNextButton: RectF,
        resultShareButton: RectF,
        resultRetryButton: RectF,
        homeButton: RectF,
        restartButton: RectF,
        sfxButton: RectF,
        musicButton: RectF,
        shareButton: RectF,
        nextButton: RectF
    ): ButtonId {
        if (screen == Screen.AD && adButton.contains(x, y)) return ButtonId.AD_CONTINUE
        if (state == GameState.WON && resultNextButton.contains(x, y)) return ButtonId.NEXT
        if (state == GameState.WON && resultShareButton.contains(x, y)) return ButtonId.SHARE
        if (state == GameState.LOST && resultRetryButton.contains(x, y)) return ButtonId.CONTINUE
        if (homeButton.contains(x, y)) return ButtonId.HOME
        if (restartButton.contains(x, y)) return ButtonId.RESTART
        if (sfxButton.contains(x, y)) return ButtonId.SFX
        if (musicButton.contains(x, y)) return ButtonId.MUSIC
        if ((state == GameState.WON || state == GameState.LOST) && shareButton.contains(x, y)) return ButtonId.SHARE
        if (state == GameState.WON && nextButton.contains(x, y)) return ButtonId.NEXT
        return ButtonId.NONE
    }

    fun menuButtonAt(
        x: Float,
        y: Float,
        menuState: MenuState,
        menuPrivacyButton: RectF,
        menuLanguageButton: RectF,
        menuSfxButton: RectF,
        menuMusicButton: RectF,
        menuStartButton: RectF,
        menuContinueButton: RectF,
        menuChaosButton: RectF,
        menuLeaderboardButton: RectF,
        menuVaultButton: RectF,
        menuCollectionButton: RectF,
        menuClassicContinueButton: RectF,
        menuClassicNewButton: RectF,
        menuChaosStartButton: RectF,
        menuClassicCard: RectF,
        menuChaosCard: RectF
    ): MenuButton {
        if (menuPrivacyButton.contains(x, y)) return MenuButton.SETTINGS
        if (menuLanguageButton.contains(x, y)) return MenuButton.LANGUAGE
        if (menuSfxButton.contains(x, y)) return MenuButton.SFX
        if (menuMusicButton.contains(x, y)) return MenuButton.MUSIC
        if (menuState == MenuState.MODES) {
            if (menuStartButton.contains(x, y)) return MenuButton.PLAY
            if (menuContinueButton.contains(x, y)) return MenuButton.CLASSIC
            if (menuChaosButton.contains(x, y)) return MenuButton.CHAOS
            if (menuLeaderboardButton.contains(x, y)) return MenuButton.LEADERBOARDS
            if (menuVaultButton.contains(x, y)) return MenuButton.VAULT
            if (menuCollectionButton.contains(x, y)) return MenuButton.COLLECTION
        } else {
            if (menuClassicContinueButton.contains(x, y)) return MenuButton.CLASSIC_CONTINUE
            if (menuClassicNewButton.contains(x, y)) return MenuButton.CLASSIC_START
            if (menuChaosStartButton.contains(x, y)) return MenuButton.CHAOS_START
            if (menuClassicCard.contains(x, y)) return MenuButton.CLASSIC
            if (menuChaosCard.contains(x, y)) return MenuButton.CHAOS
            if (menuContinueButton.contains(x, y)) return MenuButton.BACK
        }
        return MenuButton.NONE
    }

    fun handleButton(
        button: ButtonId,
        performHaptic: (Int) -> Unit,
        toggleSfx: () -> Unit,
        toggleMusic: () -> Unit,
        playSound: (com.moonsolstudios.kavvoro.audio.SoundEvent) -> Unit,
        exitToMenu: () -> Unit,
        onRestart: () -> Unit,
        shareRun: () -> Unit,
        onNext: () -> Unit,
        continueAfterFail: () -> Unit,
        requestAdThenContinue: () -> Unit
    ): (() -> Unit)? {
        performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        if (button == ButtonId.SFX) {
            toggleSfx()
            return null
        }
        if (button == ButtonId.MUSIC) {
            toggleMusic()
            return null
        }
        if (button != ButtonId.NONE) playSound(com.moonsolstudios.kavvoro.audio.SoundEvent.UI_TAP)
        return when (button) {
            ButtonId.HOME -> {
                exitToMenu()
                null
            }
            ButtonId.RESTART -> {
                onRestart()
                null
            }
            ButtonId.SHARE -> {
                { shareRun() }
            }
            ButtonId.NEXT -> {
                onNext()
                null
            }
            ButtonId.CONTINUE -> {
                continueAfterFail()
                null
            }
            ButtonId.AD_CONTINUE -> {
                requestAdThenContinue()
                null
            }
            ButtonId.SFX,
            ButtonId.MUSIC,
            ButtonId.NONE -> null
        }
    }

    fun handleMenuButton(
        button: MenuButton,
        performHaptic: (Int) -> Unit,
        toggleSfx: () -> Unit,
        toggleMusic: () -> Unit,
        playSound: (com.moonsolstudios.kavvoro.audio.SoundEvent) -> Unit,
        openModePicker: () -> Unit,
        onSelectClassic: () -> Unit,
        onSelectChaos: () -> Unit,
        continueRunFromMenu: (GameMode) -> Unit,
        startRun: (GameMode, Boolean) -> Unit,
        selectedMenuMode: GameMode,
        openCollection: () -> Unit,
        openLeaderboards: () -> Unit,
        openSettings: () -> Unit,
        openLanguage: () -> Unit,
        showPrivacy: () -> Unit,
        onBackToModes: () -> Unit
    ) {
        performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        if (button == MenuButton.SFX) {
            toggleSfx()
            return
        }
        if (button == MenuButton.MUSIC) {
            toggleMusic()
            return
        }
        if (button != MenuButton.NONE) playSound(com.moonsolstudios.kavvoro.audio.SoundEvent.UI_TAP)
        when (button) {
            MenuButton.PLAY -> openModePicker()
            MenuButton.CLASSIC -> onSelectClassic()
            MenuButton.CLASSIC_CONTINUE -> continueRunFromMenu(GameMode.CLASSIC)
            MenuButton.CLASSIC_START -> startRun(GameMode.CLASSIC, false)
            MenuButton.CHAOS_START -> startRun(GameMode.CHAOS, false)
            MenuButton.CHAOS -> onSelectChaos()
            MenuButton.COLLECTION,
            MenuButton.VAULT -> openCollection()
            MenuButton.LEADERBOARDS -> openLeaderboards()
            MenuButton.SETTINGS -> openSettings()
            MenuButton.PRIVACY -> showPrivacy()
            MenuButton.LANGUAGE -> openLanguage()
            MenuButton.START -> startRun(selectedMenuMode, false)
            MenuButton.CONTINUE -> continueRunFromMenu(selectedMenuMode)
            MenuButton.BACK -> onBackToModes()
            MenuButton.SFX,
            MenuButton.MUSIC,
            MenuButton.NONE -> Unit
        }
    }

    fun handleAdTouch(
        event: android.view.MotionEvent,
        buttonAt: (Float, Float) -> ButtonId,
        handleButton: (ButtonId) -> (() -> Unit)?,
        getActiveButton: () -> ButtonId,
        setActiveButton: (ButtonId) -> Unit
    ): (() -> Unit)? {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                setActiveButton(buttonAt(event.x, event.y))
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                val releasedButton = getActiveButton()
                setActiveButton(ButtonId.NONE)
                if (releasedButton != ButtonId.NONE && buttonAt(event.x, event.y) == releasedButton) {
                    return handleButton(releasedButton)
                }
            }
        }
        return null
    }

    fun handleGameplayTouch(
        event: android.view.MotionEvent,
        buttonAt: (Float, Float) -> ButtonId,
        handleButton: (ButtonId) -> (() -> Unit)?,
        getActiveButton: () -> ButtonId,
        setActiveButton: (ButtonId) -> Unit,
        handleTutorialTouch: (android.view.MotionEvent) -> Boolean,
        riftTapReleaseTimer: Float,
        startRiftControl: (Float, Float) -> Unit,
        moveRiftControl: (Float, Float) -> Unit,
        releaseRiftControl: () -> Unit
    ): (() -> Unit)? {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                val btn = buttonAt(event.x, event.y)
                setActiveButton(btn)
                if (btn == ButtonId.NONE && !handleTutorialTouch(event)) {
                    startRiftControl(event.x, event.y)
                }
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                if (getActiveButton() == ButtonId.NONE &&
                    !handleTutorialTouch(event) &&
                    riftTapReleaseTimer <= 0f
                ) {
                    moveRiftControl(event.x, event.y)
                }
            }
            android.view.MotionEvent.ACTION_POINTER_DOWN,
            android.view.MotionEvent.ACTION_POINTER_UP -> {
                handleTutorialTouch(event)
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                val releasedButton = getActiveButton()
                setActiveButton(ButtonId.NONE)
                if (releasedButton != ButtonId.NONE && buttonAt(event.x, event.y) == releasedButton) {
                    return handleButton(releasedButton)
                } else if (!handleTutorialTouch(event) &&
                    (event.actionMasked == android.view.MotionEvent.ACTION_CANCEL || riftTapReleaseTimer <= 0f)
                ) {
                    releaseRiftControl()
                }
            }
        }
        return null
    }
}
