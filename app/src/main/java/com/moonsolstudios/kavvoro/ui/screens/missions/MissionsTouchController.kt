package com.moonsolstudios.kavvoro.ui.screens.missions

import android.view.MotionEvent
import com.moonsolstudios.kavvoro.model.DailyMissionId
import com.moonsolstudios.kavvoro.model.DailyMissionProgress

sealed interface MissionsTouchAction {
    data object Back : MissionsTouchAction
    data class Claim(val missionId: DailyMissionId) : MissionsTouchAction
}

class MissionsTouchController(
    val layoutCalculator: MissionsLayoutCalculator = MissionsLayoutCalculator()
) {
    private var activeTarget = Target.NONE
    private var activeMissionIndex = -1
    val activeClaimIndex: Int get() = activeMissionIndex

    fun reset() {
        activeTarget = Target.NONE
        activeMissionIndex = -1
    }

    fun handleTouch(event: MotionEvent, missions: List<DailyMissionProgress>): MissionsTouchAction? {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeTarget = Target.NONE
                activeMissionIndex = -1
                if (layoutCalculator.backButtonRect.contains(event.x, event.y)) {
                    activeTarget = Target.BACK
                } else {
                    val index = missions.indices.firstOrNull { index ->
                        missions[index].canClaim && layoutCalculator.claimButtonRects[index].contains(event.x, event.y)
                    }
                    if (index != null) {
                        activeTarget = Target.CLAIM
                        activeMissionIndex = index
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val releasedTarget = activeTarget
                val releasedIndex = activeMissionIndex
                activeTarget = Target.NONE
                activeMissionIndex = -1
                return when {
                    releasedTarget == Target.BACK && layoutCalculator.backButtonRect.contains(event.x, event.y) ->
                        MissionsTouchAction.Back
                    releasedTarget == Target.CLAIM && missions.getOrNull(releasedIndex)?.canClaim == true &&
                        layoutCalculator.claimButtonRects.getOrNull(releasedIndex)?.contains(event.x, event.y) == true ->
                        MissionsTouchAction.Claim(missions[releasedIndex].id)
                    else -> null
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                activeTarget = Target.NONE
                activeMissionIndex = -1
            }
        }
        return null
    }

    private enum class Target { NONE, BACK, CLAIM }
}
