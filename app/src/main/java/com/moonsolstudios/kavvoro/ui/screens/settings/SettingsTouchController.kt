package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.SettingsButton
import kotlin.math.roundToInt

object SettingsTouchController {

    val layoutCalculator = SettingsLayoutCalculator()
    val tabAudio = RectF()
    val tabGameplay = RectF()
    val tabSystem = RectF()
    val tabInfo = RectF()
    val contentPanel = RectF()
    val masterButton = RectF()
    val masterSlider = RectF()
    val musicButton = RectF()
    val musicSlider = RectF()
    val sfxButton = RectF()
    val sfxSlider = RectF()
    val hapticToggle = RectF()
    val shakeToggle = RectF()
    val performanceToggle = RectF()
    val languageButton = RectF()
    val accountButton = RectF()
    val privacyButton = RectF()
    val termsButton = RectF()
    val dataDeletionButton = RectF()
    val aboutButton = RectF()
    val resetButton = RectF()
    val backButton = RectF()
    val resetCancelButton = RectF()
    val resetConfirmButton = RectF()

    fun syncLayoutRects() {
        layoutCalculator.copyTo(
            tabAudio = tabAudio,
            tabGameplay = tabGameplay,
            tabSystem = tabSystem,
            tabInfo = tabInfo,
            contentPanel = contentPanel,
            masterButton = masterButton,
            musicButton = musicButton,
            sfxButton = sfxButton,
            hapticToggle = hapticToggle,
            shakeToggle = shakeToggle,
            performanceToggle = performanceToggle,
            languageButton = languageButton,
            accountButton = accountButton,
            privacyButton = privacyButton,
            termsButton = termsButton,
            dataDeletionButton = dataDeletionButton,
            aboutButton = aboutButton,
            resetButton = resetButton,
            backButton = backButton,
            masterSlider = masterSlider,
            musicSlider = musicSlider,
            sfxSlider = sfxSlider
        )
    }

    fun buttonAt(x: Float, y: Float, viewportTop: Float, viewportBottom: Float): SettingsButton {
        if (y < viewportTop || y > viewportBottom) return SettingsButton.NONE
        return buttonAt(
            x = x,
            y = y,
            resetConfirmButton = resetConfirmButton,
            backButton = backButton,
            masterSlider = masterSlider,
            masterButton = masterButton,
            musicSlider = musicSlider,
            musicButton = musicButton,
            sfxSlider = sfxSlider,
            sfxButton = sfxButton,
            hapticToggle = hapticToggle,
            shakeToggle = shakeToggle,
            performanceToggle = performanceToggle,
            languageButton = languageButton,
            accountButton = accountButton,
            privacyButton = privacyButton,
            termsButton = termsButton,
            dataDeletionButton = dataDeletionButton,
            aboutButton = aboutButton,
            resetButton = resetButton,
            tabAudio = tabAudio,
            tabGameplay = tabGameplay,
            tabSystem = tabSystem,
            tabInfo = tabInfo
        )
    }

    private fun RectF.containsPoint(x: Float, y: Float): Boolean =
        left < right && top < bottom && x >= left && x <= right && y >= top && y <= bottom

    fun buttonAt(
        x: Float,
        y: Float,
        resetConfirmButton: RectF,
        backButton: RectF,
        masterSlider: RectF,
        masterButton: RectF,
        musicSlider: RectF,
        musicButton: RectF,
        sfxSlider: RectF,
        sfxButton: RectF,
        hapticToggle: RectF,
        shakeToggle: RectF,
        performanceToggle: RectF,
        languageButton: RectF,
        accountButton: RectF,
        privacyButton: RectF,
        termsButton: RectF,
        dataDeletionButton: RectF,
        aboutButton: RectF,
        resetButton: RectF,
        tabAudio: RectF? = null,
        tabGameplay: RectF? = null,
        tabSystem: RectF? = null,
        tabInfo: RectF? = null
    ): SettingsButton = when {
        resetConfirmButton.containsPoint(x, y) -> SettingsButton.NONE
        backButton.containsPoint(x, y) -> SettingsButton.BACK
        tabAudio?.containsPoint(x, y) == true -> SettingsButton.TAB_AUDIO
        tabGameplay?.containsPoint(x, y) == true -> SettingsButton.TAB_GAMEPLAY
        tabSystem?.containsPoint(x, y) == true -> SettingsButton.TAB_SYSTEM
        tabInfo?.containsPoint(x, y) == true -> SettingsButton.TAB_INFO
        masterSlider.containsPoint(x, y) || masterButton.containsPoint(x, y) -> SettingsButton.MASTER_VOLUME
        musicSlider.containsPoint(x, y) || musicButton.containsPoint(x, y) -> SettingsButton.MUSIC_VOLUME
        sfxSlider.containsPoint(x, y) || sfxButton.containsPoint(x, y) -> SettingsButton.SFX_VOLUME
        hapticToggle.containsPoint(x, y) -> SettingsButton.HAPTIC
        shakeToggle.containsPoint(x, y) -> SettingsButton.SCREEN_SHAKE
        performanceToggle.containsPoint(x, y) -> SettingsButton.PERFORMANCE
        languageButton.containsPoint(x, y) -> SettingsButton.LANGUAGE
        accountButton.containsPoint(x, y) -> SettingsButton.ACCOUNT
        privacyButton.containsPoint(x, y) -> SettingsButton.PRIVACY
        termsButton.containsPoint(x, y) -> SettingsButton.TERMS
        dataDeletionButton.containsPoint(x, y) -> SettingsButton.DATA_DELETION
        aboutButton.containsPoint(x, y) -> SettingsButton.ABOUT
        resetButton.containsPoint(x, y) -> SettingsButton.RESET
        else -> SettingsButton.NONE
    }

    fun calculateSliderValue(rect: RectF, x: Float): Int {
        val w = rect.right - rect.left
        if (w <= 0f) return 0
        return (((x - rect.left) / w) * 100f).roundToInt().coerceIn(0, 100)
    }

    fun handleAction(
        button: SettingsButton,
        performHaptic: (Int) -> Unit,
        handleSystemBack: () -> Unit,
        toggleHaptic: () -> Unit,
        toggleScreenShake: () -> Unit,
        togglePerformance: () -> Unit,
        openLanguage: () -> Unit,
        showAccountMessage: () -> Unit,
        showPrivacy: () -> Unit,
        requestResetConfirm: () -> Unit,
        switchTab: (com.moonsolstudios.kavvoro.model.SettingsTab) -> Unit = {}
    ) {
        performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        when (button) {
            SettingsButton.BACK -> handleSystemBack()
            SettingsButton.TAB_AUDIO -> switchTab(com.moonsolstudios.kavvoro.model.SettingsTab.AUDIO)
            SettingsButton.TAB_GAMEPLAY -> switchTab(com.moonsolstudios.kavvoro.model.SettingsTab.GAMEPLAY)
            SettingsButton.TAB_SYSTEM -> switchTab(com.moonsolstudios.kavvoro.model.SettingsTab.SYSTEM)
            SettingsButton.TAB_INFO -> switchTab(com.moonsolstudios.kavvoro.model.SettingsTab.INFO)
            SettingsButton.HAPTIC -> toggleHaptic()
            SettingsButton.SCREEN_SHAKE -> toggleScreenShake()
            SettingsButton.PERFORMANCE -> togglePerformance()
            SettingsButton.LANGUAGE -> openLanguage()
            SettingsButton.ACCOUNT -> showAccountMessage()
            SettingsButton.PRIVACY -> showPrivacy()
            SettingsButton.TERMS,
            SettingsButton.DATA_DELETION,
            SettingsButton.ABOUT -> Unit
            SettingsButton.RESET -> requestResetConfirm()
            SettingsButton.MASTER_VOLUME,
            SettingsButton.MUSIC_VOLUME,
            SettingsButton.SFX_VOLUME,
            SettingsButton.NONE -> Unit
        }
    }

    fun updateSlider(
        button: SettingsButton,
        x: Float,
        masterSlider: RectF,
        musicSlider: RectF,
        sfxSlider: RectF,
        onMasterChanged: (Int) -> Unit,
        onMusicChanged: (Int) -> Unit,
        onSfxChanged: (Int) -> Unit
    ) {
        val rect = when (button) {
            SettingsButton.MASTER_VOLUME -> masterSlider
            SettingsButton.MUSIC_VOLUME -> musicSlider
            SettingsButton.SFX_VOLUME -> sfxSlider
            else -> return
        }
        val value = calculateSliderValue(rect, x)
        when (button) {
            SettingsButton.MASTER_VOLUME -> onMasterChanged(value)
            SettingsButton.MUSIC_VOLUME -> onMusicChanged(value)
            SettingsButton.SFX_VOLUME -> onSfxChanged(value)
            else -> Unit
        }
    }

    fun handleTouch(
        event: android.view.MotionEvent,
        settingsResetConfirm: Boolean,
        settingsResetCancelButton: RectF,
        settingsResetConfirmButton: RectF,
        onResetCancelled: () -> Unit,
        onResetConfirmed: () -> Unit,
        layoutSettings: () -> Unit,
        settingsTouchY: Float,
        setTouchY: (Float) -> Unit,
        settingsLastY: Float,
        setLastY: (Float) -> Unit,
        settingsDragging: Boolean,
        setDragging: (Boolean) -> Unit,
        activeSettingsButton: SettingsButton,
        setActiveButton: (SettingsButton) -> Unit,
        settingsScroll: Float,
        setScroll: (Float) -> Unit,
        settingsMaxScroll: Float,
        tutorialTouchSlop: Float,
        buttonAt: (Float, Float) -> SettingsButton,
        updateSlider: (SettingsButton, Float) -> Unit,
        handleAction: (SettingsButton) -> Unit,
        requestPostInvalidate: () -> Unit
    ) {
        if (settingsResetConfirm) {
            if (event.actionMasked == android.view.MotionEvent.ACTION_UP) {
                when {
                    settingsResetCancelButton.contains(event.x, event.y) -> onResetCancelled()
                    settingsResetConfirmButton.contains(event.x, event.y) -> onResetConfirmed()
                }
                requestPostInvalidate()
            }
            return
        }
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                layoutSettings()
                setTouchY(event.y)
                setLastY(event.y)
                setDragging(false)
                val target = buttonAt(event.x, event.y)
                setActiveButton(target)
                if (target == SettingsButton.MASTER_VOLUME || target == SettingsButton.MUSIC_VOLUME || target == SettingsButton.SFX_VOLUME) {
                    updateSlider(target, event.x)
                }
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                val dy = event.y - settingsLastY
                if (activeSettingsButton == SettingsButton.MASTER_VOLUME || activeSettingsButton == SettingsButton.MUSIC_VOLUME || activeSettingsButton == SettingsButton.SFX_VOLUME) {
                    updateSlider(activeSettingsButton, event.x)
                } else if (settingsDragging || kotlin.math.abs(event.y - settingsTouchY) > tutorialTouchSlop) {
                    setDragging(true)
                    setScroll((settingsScroll - dy).coerceIn(0f, settingsMaxScroll))
                    setActiveButton(SettingsButton.NONE)
                    layoutSettings()
                }
                setLastY(event.y)
            }
            android.view.MotionEvent.ACTION_UP -> {
                val released = activeSettingsButton
                val sameTarget = !settingsDragging && buttonAt(event.x, event.y) == released
                setActiveButton(SettingsButton.NONE)
                if (sameTarget) handleAction(released)
                setDragging(false)
            }
            android.view.MotionEvent.ACTION_CANCEL -> {
                setActiveButton(SettingsButton.NONE)
                setDragging(false)
            }
        }
        requestPostInvalidate()
    }
}
