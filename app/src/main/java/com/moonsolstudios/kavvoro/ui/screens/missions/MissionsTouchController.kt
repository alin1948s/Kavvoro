package com.moonsolstudios.kavvoro.ui.screens.missions

import android.view.MotionEvent
import com.moonsolstudios.kavvoro.model.MissionCategory
import com.moonsolstudios.kavvoro.model.MissionId
import com.moonsolstudios.kavvoro.model.MissionProgress

sealed interface MissionsTouchAction {
    data object Back : MissionsTouchAction
    data class SelectCategory(val category: MissionCategory) : MissionsTouchAction
    data class Claim(val missionId: MissionId) : MissionsTouchAction
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

    fun handleTouch(event: MotionEvent, missions: List<MissionProgress>): MissionsTouchAction? {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeTarget = Target.NONE
                activeMissionIndex = -1
                if (layoutCalculator.backButtonRect.contains(event.x, event.y)) {
                    activeTarget = Target.BACK
                } else if (layoutCalculator.dailyTabRect.contains(event.x, event.y)) {
                    activeTarget = Target.DAILY_TAB
                } else if (layoutCalculator.riftChallengesTabRect.contains(event.x, event.y)) {
                    activeTarget = Target.RIFT_CHALLENGES_TAB
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
                    releasedTarget == Target.DAILY_TAB && layoutCalculator.dailyTabRect.contains(event.x, event.y) ->
                        MissionsTouchAction.SelectCategory(MissionCategory.DAILY)
                    releasedTarget == Target.RIFT_CHALLENGES_TAB && layoutCalculator.riftChallengesTabRect.contains(event.x, event.y) ->
                        MissionsTouchAction.SelectCategory(MissionCategory.RIFT_CHALLENGES)
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

    private enum class Target { NONE, BACK, DAILY_TAB, RIFT_CHALLENGES_TAB, CLAIM }
}
