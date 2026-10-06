package com.moonsolstudios.kavvoro.ui.screens.gameplay

import android.graphics.RectF
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameState

/**
 * Dedicated touch controller and HUD button hit-tester for the Gameplay screen (Screen.GAME).
 */
object GameplayTouchController {

    val homeButtonRect = RectF()
    val restartButtonRect = RectF()
    val sfxButtonRect = RectF()
    val musicButtonRect = RectF()
    val shareButtonRect = RectF()
    val nextButtonRect = RectF()

    private fun RectF.hits(x: Float, y: Float): Boolean =
        contains(x, y) || (right > left && bottom > top && x >= left && x < right && y >= top && y < bottom)

    fun toolbarButtonAt(
        x: Float,
        y: Float,
        state: GameState,
        homeButton: RectF,
        restartButton: RectF,
        sfxButton: RectF,
        musicButton: RectF,
        shareButton: RectF,
        nextButton: RectF
    ): ButtonId {
        if (homeButton.hits(x, y)) return ButtonId.HOME
        if (restartButton.hits(x, y)) return ButtonId.RESTART
        if (sfxButton.hits(x, y)) return ButtonId.SFX
        if (musicButton.hits(x, y)) return ButtonId.MUSIC
        if ((state == GameState.WON || state == GameState.LOST) && shareButton.hits(x, y)) return ButtonId.SHARE
        if (state == GameState.WON && nextButton.hits(x, y)) return ButtonId.NEXT
        return ButtonId.NONE
    }

    fun buttonAt(
        x: Float,
        y: Float,
        state: GameState,
        homeButton: RectF = homeButtonRect,
        restartButton: RectF = restartButtonRect,
        sfxButton: RectF = sfxButtonRect,
        musicButton: RectF = musicButtonRect,
        shareButton: RectF = shareButtonRect,
        nextButton: RectF = nextButtonRect,
        resultNextButton: RectF = RectF(),
        resultShareButton: RectF = RectF(),
        resultRetryButton: RectF = RectF()
    ): ButtonId {
        if (state == GameState.WON) {
            if (resultNextButton.hits(x, y)) return ButtonId.NEXT
            if (resultShareButton.hits(x, y)) return ButtonId.SHARE
        } else if (state == GameState.LOST) {
            if (resultRetryButton.hits(x, y)) return ButtonId.CONTINUE
        }

        return toolbarButtonAt(
            x = x,
            y = y,
            state = state,
            homeButton = homeButton,
            restartButton = restartButton,
            sfxButton = sfxButton,
            musicButton = musicButton,
            shareButton = shareButton,
            nextButton = nextButton
        )
    }

    fun handleTouch(
        event: MotionEvent,
        activeButton: ButtonId,
        setActiveButton: (ButtonId) -> Unit,
        buttonAt: (Float, Float) -> ButtonId,
        handleTutorialTouch: (MotionEvent) -> Boolean,
        riftTapReleaseTimer: Float,
        startRiftControl: (Float, Float) -> Unit,
        moveRiftControl: (Float, Float) -> Unit,
        releaseRiftControl: () -> Unit,
        onButtonAction: (ButtonId) -> (() -> Unit)?
    ): (() -> Unit)? {
        var pendingAction: (() -> Unit)? = null
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val hit = buttonAt(event.x, event.y)
                setActiveButton(hit)
                if (hit == ButtonId.NONE && !handleTutorialTouch(event)) {
                    startRiftControl(event.x, event.y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (activeButton == ButtonId.NONE &&
                    !handleTutorialTouch(event) &&
                    riftTapReleaseTimer <= 0f
                ) {
                    moveRiftControl(event.x, event.y)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_UP -> {
                handleTutorialTouch(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val releasedButton = activeButton
                setActiveButton(ButtonId.NONE)
                if (releasedButton != ButtonId.NONE && buttonAt(event.x, event.y) == releasedButton) {
                    pendingAction = onButtonAction(releasedButton)
                } else if (!handleTutorialTouch(event) &&
                    (event.actionMasked == MotionEvent.ACTION_CANCEL || riftTapReleaseTimer <= 0f)
                ) {
                    releaseRiftControl()
                }
            }
        }
        return pendingAction
    }
}
