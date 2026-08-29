package com.moonsolstudios.kavvoro.ui.controller

import android.content.SharedPreferences
import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.audio.KavvoroSoundEngine
import com.moonsolstudios.kavvoro.audio.SoundEvent
import com.moonsolstudios.kavvoro.ui.TutorialGateOutcome
import com.moonsolstudios.kavvoro.ui.TutorialGestureSlop
import com.moonsolstudios.kavvoro.ui.TutorialInputGate
import com.moonsolstudios.kavvoro.ui.TutorialPointerAction
import com.moonsolstudios.kavvoro.ui.TutorialTouchTarget

object TutorialTouchController {

    fun tutorialTouchTarget(
        x: Float,
        y: Float,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF
    ): TutorialTouchTarget = when {
        tutorialCardBounds.isEmpty -> TutorialTouchTarget.CARD
        tutorialStartButton.contains(x, y) -> TutorialTouchTarget.ACTION_BUTTON
        tutorialCardBounds.contains(x, y) -> TutorialTouchTarget.CARD
        else -> TutorialTouchTarget.PLAYFIELD
    }

    fun dismissTutorialCard(
        prefs: SharedPreferences,
        acknowledgementKey: String,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF,
        onDismissed: () -> Unit,
        performHaptic: (Int) -> Unit,
        playSound: (SoundEvent) -> Unit
    ): Boolean {
        prefs.edit().putBoolean(acknowledgementKey, true).apply()
        tutorialCardBounds.setEmpty()
        tutorialStartButton.setEmpty()
        onDismissed()
        performHaptic(HapticFeedbackConstants.CONFIRM)
        playSound(SoundEvent.UI_TAP)
        return true
    }

    data class TutorialTouchState(
        var downX: Float = 0f,
        var downY: Float = 0f,
        var movedBeyondSlop: Boolean = false
    )

    fun handleTouch(
        event: MotionEvent,
        tutorialCardVisible: Boolean,
        state: TutorialTouchState,
        touchSlop: Float,
        inputGate: TutorialInputGate,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF,
        dismissTutorialCard: () -> Boolean,
        startRiftControl: (Float, Float) -> Unit
    ): Boolean {
        if (!tutorialCardVisible) return false
        val action = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                state.downX = event.x
                state.downY = event.y
                state.movedBeyondSlop = false
                TutorialPointerAction.DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                if (TutorialGestureSlop.exceeded(state.downX, state.downY, event.x, event.y, touchSlop)) {
                    state.movedBeyondSlop = true
                }
                TutorialPointerAction.MOVE
            }
            MotionEvent.ACTION_UP -> {
                if (TutorialGestureSlop.exceeded(state.downX, state.downY, event.x, event.y, touchSlop)) {
                    state.movedBeyondSlop = true
                }
                TutorialPointerAction.UP
            }
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_UP -> TutorialPointerAction.MULTI_TOUCH
            MotionEvent.ACTION_CANCEL -> TutorialPointerAction.CANCEL
            else -> return true
        }
        val result = inputGate.onPointer(
            action = action,
            target = tutorialTouchTarget(event.x, event.y, tutorialCardBounds, tutorialStartButton),
            movedBeyondTapSlop = state.movedBeyondSlop
        )

        when (result.outcome) {
            TutorialGateOutcome.NONE -> Unit
            TutorialGateOutcome.DISMISS_ONLY -> dismissTutorialCard()
            TutorialGateOutcome.DISMISS_AND_PLAY -> {
                if (dismissTutorialCard()) startRiftControl(event.x, event.y)
            }
        }

        if (action == TutorialPointerAction.UP ||
            action == TutorialPointerAction.CANCEL ||
            action == TutorialPointerAction.MULTI_TOUCH
        ) {
            inputGate.reset()
            state.downX = 0f
            state.downY = 0f
            state.movedBeyondSlop = false
        }
        return result.consumed
    }
}
