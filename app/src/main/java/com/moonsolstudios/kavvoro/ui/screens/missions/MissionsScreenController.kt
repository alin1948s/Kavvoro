package com.moonsolstudios.kavvoro.ui.screens.missions

import android.graphics.Canvas
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.repository.DailyMissionsRepository
import com.moonsolstudios.kavvoro.model.DailyMissionProgress

/** Owns the Missions screen layout, rendering, input, and daily mission state. */
class MissionsScreenController(
    private var repository: DailyMissionsRepository,
    private val onTouch: () -> Unit,
    private val onBack: () -> Unit,
    private val onReward: (Int) -> Unit,
    private val onRejectedClaim: () -> Unit,
    private val worldBitmap: (String) -> Bitmap? = { null }
) {
    private val touchController = MissionsTouchController()
    private var completionPopup: CompletionPopup? = null

    fun replaceRepository(repository: DailyMissionsRepository) {
        this.repository = repository
        touchController.reset()
        dismissGamePopup()
    }

    fun reset() {
        touchController.reset()
        dismissGamePopup()
    }

    fun recordRound(won: Boolean, coinsEarned: Int) {
        val completed = repository.recordRound(won, coinsEarned)
        if (completed.isNotEmpty()) {
            completionPopup = CompletionPopup(completed, SystemClock.uptimeMillis())
        }
    }

    fun dismissGamePopup() {
        completionPopup = null
    }

    fun handleGamePopupTouch(event: MotionEvent): Boolean {
        val popup = completionPopup ?: return false
        if (SystemClock.uptimeMillis() - popup.createdAtMs >= COMPLETION_POPUP_DURATION_MS) {
            dismissGamePopup()
            return false
        }
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            dismissGamePopup()
            onTouch()
        }
        return true
    }

    fun drawGamePopup(canvas: Canvas, width: Int, height: Int, dp: Float, t: (String) -> String) {
        val popup = completionPopup ?: return
        val elapsed = SystemClock.uptimeMillis() - popup.createdAtMs
        if (elapsed >= COMPLETION_POPUP_DURATION_MS) {
            dismissGamePopup()
            return
        }
        val fadeIn = (elapsed / POPUP_FADE_IN_MS.toFloat()).coerceIn(0f, 1f)
        val fadeOut = ((COMPLETION_POPUP_DURATION_MS - elapsed) / POPUP_FADE_OUT_MS.toFloat()).coerceIn(0f, 1f)
        MissionsCompletionPopupRenderer.draw(
            canvas = canvas,
            viewWidth = width.toFloat(),
            viewHeight = height.toFloat(),
            missions = popup.missions,
            dp = dp,
            alpha = minOf(fadeIn, fadeOut),
            t = t
        )
    }

    fun draw(canvas: Canvas, width: Int, height: Int, dp: Float, t: (String) -> String) {
        val layout = touchController.layoutCalculator
        layout.calculate(width.toFloat(), height.toFloat(), dp)
        MissionsUiRenderer.drawScreen(
            canvas = canvas,
            layout = layout,
            missions = repository.missions(),
            activeClaimIndex = touchController.activeClaimIndex,
            dp = dp,
            missionArt = worldBitmap("brainball_main"),
            coinArt = worldBitmap("ic_stat_coin_3d"),
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

    private data class CompletionPopup(val missions: List<DailyMissionProgress>, val createdAtMs: Long)

    private companion object {
        const val COMPLETION_POPUP_DURATION_MS = 7_000L
        const val POPUP_FADE_IN_MS = 220L
        const val POPUP_FADE_OUT_MS = 650L
    }
}
