package com.moonsolstudios.kavvoro.ui.screens.missions

import android.graphics.Canvas
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.MissionCategory
import com.moonsolstudios.kavvoro.model.MissionClaimReward
import com.moonsolstudios.kavvoro.model.MissionProgress
import com.moonsolstudios.kavvoro.model.MissionRoundResult
import com.moonsolstudios.kavvoro.repository.MissionsRepository

/** Owns Missions categories, progress, layout, rendering, and claim input. */
class MissionsScreenController(
    private var repository: MissionsRepository,
    private val onTouch: () -> Unit,
    private val onBack: () -> Unit,
    private val onReward: (MissionClaimReward) -> Unit,
    private val onRejectedClaim: () -> Unit,
    private val worldBitmap: (String) -> Bitmap? = { null },
    private val skinName: (String) -> String = { it }
) {
    private val touchController = MissionsTouchController()
    private var selectedCategory = MissionCategory.DAILY
    private var completionPopup: CompletionPopup? = null

    fun replaceRepository(repository: MissionsRepository) {
        this.repository = repository
        selectedCategory = MissionCategory.DAILY
        touchController.reset()
        dismissGamePopup()
    }

    fun reset() {
        selectedCategory = MissionCategory.DAILY
        touchController.reset()
        dismissGamePopup()
    }

    fun recordRound(
        won: Boolean,
        gameMode: GameMode,
        rank: String?,
        riftBreak: Boolean,
        maxChain: Int,
        completedLevel: Int = 0
    ) {
        val completed = repository.recordRound(
            MissionRoundResult(
                won = won,
                gameMode = gameMode,
                rank = rank,
                riftBreak = riftBreak,
                maxChain = maxChain,
                completedLevel = completedLevel
            )
        )
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
            t = t,
            skinName = skinName
        )
    }

    fun draw(canvas: Canvas, width: Int, height: Int, dp: Float, t: (String) -> String) {
        val missions = repository.missions(selectedCategory)
        val layout = touchController.layoutCalculator
        layout.calculate(width.toFloat(), height.toFloat(), dp, missions.size)
        MissionsUiRenderer.drawScreen(
            canvas = canvas,
            layout = layout,
            missions = missions,
            activeClaimIndex = touchController.activeClaimIndex,
            dp = dp,
            missionArt = worldBitmap("brainball_main"),
            hypeArt = worldBitmap("ic_stat_hype_3d"),
            category = selectedCategory,
            t = t,
            skinName = skinName
        )
    }

    fun handleTouch(event: MotionEvent, width: Int, height: Int, dp: Float): (() -> Unit)? {
        val missions = repository.missions(selectedCategory)
        val layout = touchController.layoutCalculator
        layout.calculate(width = width.toFloat(), height = height.toFloat(), density = dp, missionCount = missions.size)
        val action = touchController.handleTouch(event, missions) ?: return null
        return when (action) {
            MissionsTouchAction.Back -> {
                onTouch()
                onBack
            }
            is MissionsTouchAction.SelectCategory -> {
                selectedCategory = action.category
                onTouch()
                null
            }
            is MissionsTouchAction.Claim -> {
                onTouch()
                val missionId = action.missionId
                {
                    val reward = repository.claim(missionId)
                    if (!reward.isEmpty) onReward(reward) else onRejectedClaim()
                }
            }
        }
    }

    private data class CompletionPopup(val missions: List<MissionProgress>, val createdAtMs: Long)

    private companion object {
        const val COMPLETION_POPUP_DURATION_MS = 7_000L
        const val POPUP_FADE_IN_MS = 220L
        const val POPUP_FADE_OUT_MS = 650L
    }
}
