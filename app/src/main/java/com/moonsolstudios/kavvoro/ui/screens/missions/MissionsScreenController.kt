package com.moonsolstudios.kavvoro.ui.screens.missions

import android.graphics.Canvas
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.repository.DailyMissionsRepository

/** Owns the Missions screen layout, rendering, input, and daily mission state. */
class MissionsScreenController(
    private var repository: DailyMissionsRepository,
    private val onTouch: () -> Unit,
    private val onBack: () -> Unit,
    private val onReward: (Int) -> Unit,
    private val onRejectedClaim: () -> Unit
) {
    private val touchController = MissionsTouchController()

    fun replaceRepository(repository: DailyMissionsRepository) {
        this.repository = repository
        touchController.reset()
    }

    fun reset() = touchController.reset()

    fun recordRound(won: Boolean, coinsEarned: Int) = repository.recordRound(won, coinsEarned)

    fun draw(canvas: Canvas, width: Int, height: Int, dp: Float, t: (String) -> String) {
        val layout = touchController.layoutCalculator
        layout.calculate(width.toFloat(), height.toFloat(), dp)
        MissionsUiRenderer.drawScreen(
            canvas = canvas,
            layout = layout,
            missions = repository.missions(),
            activeClaimIndex = touchController.activeClaimIndex,
            dp = dp,
            t = t
        )
    }

    fun handleTouch(event: MotionEvent, width: Int, height: Int, dp: Float): (() -> Unit)? {
        val layout = touchController.layoutCalculator
        layout.calculate(width = width.toFloat(), height = height.toFloat(), density = dp)
        val action = touchController.handleTouch(event, repository.missions()) ?: return null
        onTouch()
        return when (action) {
            MissionsTouchAction.Back -> {
                { onBack() }
            }
            is MissionsTouchAction.Claim -> {
                {
                    val reward = repository.claim(action.missionId)
                    if (reward > 0) onReward(reward) else onRejectedClaim()
                }
            }
        }
    }
}
