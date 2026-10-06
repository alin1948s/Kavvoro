package com.moonsolstudios.kavvoro.ui.tutorial

import android.graphics.RectF
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.i18n.TutorialCopy

/**
 * Dedicated touch controller and copy helper for the interactive Tutorial card overlay.
 */
class TutorialTouchController(
    val inputGate: TutorialInputGate = TutorialInputGate()
) {
    val cardBounds = RectF()
    val startButtonRect = RectF()

    var downX: Float = 0f
        private set
    var downY: Float = 0f
        private set
    var movedBeyondSlop: Boolean = false
        private set

    fun reset() {
        inputGate.reset()
        downX = 0f
        downY = 0f
        movedBeyondSlop = false
        startButtonRect.setEmpty()
    }

    private fun RectF.hits(x: Float, y: Float): Boolean =
        contains(x, y) || (right > left && bottom > top && x >= left && x < right && y >= top && y < bottom)

    private fun RectF.hasBounds(): Boolean =
        !isEmpty || (right > left && bottom > top)

    fun touchTarget(
        x: Float,
        y: Float,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF
    ): TutorialTouchTarget = when {
        !tutorialCardBounds.hasBounds() -> TutorialTouchTarget.CARD
        tutorialStartButton.hits(x, y) -> TutorialTouchTarget.ACTION_BUTTON
        tutorialCardBounds.hits(x, y) -> TutorialTouchTarget.CARD
        else -> TutorialTouchTarget.PLAYFIELD
    }

    fun handleTouch(
        event: MotionEvent,
        tutorialCardVisible: Boolean,
        tutorialCardBounds: RectF,
        tutorialStartButton: RectF,
        touchSlop: Float,
        onDismissOnly: () -> Unit,
        onDismissAndPlay: (Float, Float) -> Unit
    ): Boolean {
        if (!tutorialCardVisible) return false
        val action = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                movedBeyondSlop = false
                TutorialPointerAction.DOWN
            }

            MotionEvent.ACTION_MOVE -> {
                if (TutorialGestureSlop.exceeded(downX, downY, event.x, event.y, touchSlop)) {
                    movedBeyondSlop = true
                }
                TutorialPointerAction.MOVE
            }

            MotionEvent.ACTION_UP -> {
                if (TutorialGestureSlop.exceeded(downX, downY, event.x, event.y, touchSlop)) {
                    movedBeyondSlop = true
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
            target = touchTarget(event.x, event.y, tutorialCardBounds, tutorialStartButton),
            movedBeyondTapSlop = movedBeyondSlop
        )

        when (result.outcome) {
            TutorialGateOutcome.NONE -> Unit
            TutorialGateOutcome.DISMISS_ONLY -> onDismissOnly()
            TutorialGateOutcome.DISMISS_AND_PLAY -> onDismissAndPlay(event.x, event.y)
        }

        if (action == TutorialPointerAction.UP ||
            action == TutorialPointerAction.CANCEL ||
            action == TutorialPointerAction.MULTI_TOUCH
        ) {
            reset()
        }
        return result.consumed
    }

    companion object {
        fun actionLabel(
            hasCurse: (CurseType) -> Boolean,
            t: (String) -> String
        ): String {
            return t(
                TutorialCopy.actionLabelKey(
                    hasOverheat = hasCurse(CurseType.OVERHEAT),
                    hasPowerTap = hasCurse(CurseType.POWER_HOLD),
                    hasFocusField = hasCurse(CurseType.FOCUS_FIELD),
                    hasRiftDrain = hasCurse(CurseType.RIFT_DRAIN)
                )
            ).uppercase()
        }

        fun lessonLines(
            level: LevelSpec,
            t: (String) -> String
        ): List<String> {
            return TutorialCopy.lessonKeys(
                levelIndex = level.index,
                hasPortals = level.portals.isNotEmpty()
            ).map(t)
        }

        fun obstacleLine(
            level: LevelSpec,
            hasCurse: (CurseType) -> Boolean,
            t: (String) -> String
        ): String {
            return t(
                TutorialCopy.obstacleKey(
                    hasPortals = level.portals.isNotEmpty(),
                    hasHazards = level.hazards.isNotEmpty(),
                    hasTinyGate = hasCurse(CurseType.TINY_GATE),
                    hasPulseZones = level.pulseZones.isNotEmpty(),
                    hasBlocks = level.blocks.isNotEmpty()
                )
            )
        }
    }
}
