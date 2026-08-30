package com.moonsolstudios.kavvoro.ui.controller

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.SettingsButton
import kotlin.math.roundToInt

object SettingsTouchController {

    fun buttonAt(
        x: Float,
        y: Float,
        resetConfirmButton: RectF,
        backButton: RectF,
        headerGearButton: RectF,
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
        resetButton: RectF
    ): SettingsButton = when {
        resetConfirmButton.contains(x, y) -> SettingsButton.NONE
        backButton.contains(x, y) -> SettingsButton.BACK
        headerGearButton.contains(x, y) -> SettingsButton.HEADER_GEAR
        masterSlider.contains(x, y) || masterButton.contains(x, y) -> SettingsButton.MASTER_VOLUME
        musicSlider.contains(x, y) || musicButton.contains(x, y) -> SettingsButton.MUSIC_VOLUME
        sfxSlider.contains(x, y) || sfxButton.contains(x, y) -> SettingsButton.SFX_VOLUME
        hapticToggle.contains(x, y) -> SettingsButton.HAPTIC
        shakeToggle.contains(x, y) -> SettingsButton.SCREEN_SHAKE
        performanceToggle.contains(x, y) -> SettingsButton.PERFORMANCE
        languageButton.contains(x, y) -> SettingsButton.LANGUAGE
        accountButton.contains(x, y) -> SettingsButton.ACCOUNT
        privacyButton.contains(x, y) -> SettingsButton.PRIVACY
        termsButton.contains(x, y) -> SettingsButton.TERMS
        dataDeletionButton.contains(x, y) -> SettingsButton.DATA_DELETION
        aboutButton.contains(x, y) -> SettingsButton.ABOUT
        resetButton.contains(x, y) -> SettingsButton.RESET
        else -> SettingsButton.NONE
    }

    fun calculateSliderValue(rect: RectF, x: Float): Int {
        if (rect.width() <= 0f) return 0
        return (((x - rect.left) / rect.width()) * 100f).roundToInt().coerceIn(0, 100)
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
        requestResetConfirm: () -> Unit
    ) {
        performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        when (button) {
            SettingsButton.BACK -> handleSystemBack()
            SettingsButton.HEADER_GEAR -> Unit
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
