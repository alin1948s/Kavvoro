package com.moonsolstudios.kavvoro.ui

enum class TutorialPointerAction {
    DOWN,
    MOVE,
    UP,
    CANCEL
}

data class TutorialGateResult(
    val consumed: Boolean,
    val dismissed: Boolean
)

class TutorialInputGate {
    private var beganInsideAction = false
    private var leftAction = false

    val actionPressed: Boolean
        get() = beganInsideAction && !leftAction

    fun onPointer(
        action: TutorialPointerAction,
        insideAction: Boolean
    ): TutorialGateResult = when (action) {
        TutorialPointerAction.DOWN -> {
            beganInsideAction = insideAction
            leftAction = false
            TutorialGateResult(consumed = true, dismissed = false)
        }

        TutorialPointerAction.MOVE -> {
            if (beganInsideAction && !insideAction) leftAction = true
            TutorialGateResult(consumed = true, dismissed = false)
        }

        TutorialPointerAction.UP -> {
            val dismissed = beganInsideAction && insideAction && !leftAction
            reset()
            TutorialGateResult(consumed = true, dismissed = dismissed)
        }

        TutorialPointerAction.CANCEL -> {
            reset()
            TutorialGateResult(consumed = true, dismissed = false)
        }
    }

    fun reset() {
        beganInsideAction = false
        leftAction = false
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
