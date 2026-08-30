package com.moonsolstudios.kavvoro.ui.tutorial

enum class TutorialPointerAction {
    DOWN,
    MOVE,
    UP,
    MULTI_TOUCH,
    CANCEL
}

enum class TutorialTouchTarget {
    ACTION_BUTTON,
    CARD,
    PLAYFIELD
}

enum class TutorialGateOutcome {
    NONE,
    DISMISS_ONLY,
    DISMISS_AND_PLAY
}

data class TutorialGateResult(
    val consumed: Boolean = true,
    val outcome: TutorialGateOutcome = TutorialGateOutcome.NONE
) {
    val dismissed: Boolean
        get() = outcome != TutorialGateOutcome.NONE
}

object TutorialGestureSlop {
    fun exceeded(
        downX: Float,
        downY: Float,
        currentX: Float,
        currentY: Float,
        touchSlop: Float
    ): Boolean {
        val dx = currentX - downX
        val dy = currentY - downY
        return dx * dx + dy * dy > touchSlop * touchSlop
    }
}

class TutorialInputGate {
    private var initialTarget: TutorialTouchTarget? = null
    private var invalidated = false

    val actionPressed: Boolean
        get() = initialTarget == TutorialTouchTarget.ACTION_BUTTON && !invalidated

    fun onPointer(
        action: TutorialPointerAction,
        target: TutorialTouchTarget,
        movedBeyondTapSlop: Boolean
    ): TutorialGateResult = when (action) {
        TutorialPointerAction.DOWN -> {
            initialTarget = target
            invalidated = movedBeyondTapSlop
            TutorialGateResult()
        }

        TutorialPointerAction.MOVE -> {
            if (movedBeyondTapSlop || target != initialTarget) invalidated = true
            TutorialGateResult()
        }

        TutorialPointerAction.UP -> {
            val start = initialTarget
            val clean = start != null &&
                !invalidated &&
                !movedBeyondTapSlop &&
                target == start
            val outcome = when {
                clean && start == TutorialTouchTarget.ACTION_BUTTON ->
                    TutorialGateOutcome.DISMISS_ONLY

                clean && start == TutorialTouchTarget.PLAYFIELD ->
                    TutorialGateOutcome.DISMISS_AND_PLAY

                else -> TutorialGateOutcome.NONE
            }
            reset()
            TutorialGateResult(outcome = outcome)
        }

        TutorialPointerAction.MULTI_TOUCH,
        TutorialPointerAction.CANCEL -> {
            reset()
            TutorialGateResult()
        }
    }

    fun reset() {
        initialTarget = null
        invalidated = false
    }

    companion object {
        fun acknowledgementKey(modeName: String, level: Int): String =
            "tutorial_ack_${modeName.lowercase()}_$level"

        fun shouldShow(
            gameScreen: Boolean,
            ready: Boolean,
            hasTutorialHint: Boolean,
            acknowledged: Boolean
        ): Boolean = gameScreen && ready && hasTutorialHint && !acknowledged
    }
}
