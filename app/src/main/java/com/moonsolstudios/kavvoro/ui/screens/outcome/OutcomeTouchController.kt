package com.moonsolstudios.kavvoro.ui.screens.outcome

import android.graphics.RectF
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState

/**
 * Dedicated touch hit-testing and outcome summary/reward line builder for the Outcome screen/overlay.
 */
object OutcomeTouchController {

    val resultNextButtonRect = RectF()
    val resultRetryButtonRect = RectF()
    val resultShareButtonRect = RectF()

    private fun RectF.hits(x: Float, y: Float): Boolean =
        contains(x, y) || (right > left && bottom > top && x >= left && x < right && y >= top && y < bottom)

    fun buttonAt(
        x: Float,
        y: Float,
        state: GameState,
        resultNextButton: RectF = resultNextButtonRect,
        resultShareButton: RectF = resultShareButtonRect,
        resultRetryButton: RectF = resultRetryButtonRect
    ): ButtonId {
        if (state == GameState.WON && resultNextButton.hits(x, y)) return ButtonId.NEXT
        if (state == GameState.WON && resultShareButton.hits(x, y)) return ButtonId.SHARE
        if (state == GameState.LOST && resultRetryButton.hits(x, y)) return ButtonId.CONTINUE
        return ButtonId.NONE
    }

    fun riftBreakReason(
        score: RunScore,
        riftEnergy: Float,
        maxChain: Int,
        timeLimitSeconds: Float,
        gameMode: GameMode,
        t: (String) -> String
    ): String {
        return when {
            riftEnergy <= 0.18f -> t("LOW ENERGY FINISH").uppercase()
            maxChain >= 5 -> t("CHAIN SPIKE").uppercase()
            score.seconds >= timeLimitSeconds * 0.8f -> t("LAST SECOND CLUTCH").uppercase()
            gameMode == GameMode.CHAOS -> t("CHAOS CONTROL").uppercase()
            else -> t("CLEAN RIFT SNAP").uppercase()
        }
    }

    fun rewardLine(
        newSkin: BallSkin?,
        nextRewardText: (String?) -> String?,
        t: (String) -> String
    ): String {
        val next = nextRewardText(newSkin?.id)
        return if (newSkin != null) {
            "${t("UNLOCKED").uppercase()} ${newSkin.name} | ${next ?: t("ALL FREE REWARDS UNLOCKED").uppercase()}"
        } else {
            next ?: t("ALL FREE REWARDS UNLOCKED").uppercase()
        }
    }

    fun finishRewardLine(
        newSkin: BallSkin?,
        lastDailyBonus: Int,
        lastRiftBreak: Boolean,
        lastRiftBreakBonus: Int,
        lastStreakMilestoneBonus: Int,
        streak: Int,
        nextRewardText: (String?) -> String?,
        t: (String) -> String
    ): String {
        val signals = mutableListOf<String>()
        if (lastDailyBonus > 0) {
            signals += "${t("DAILY RIFT BONUS").uppercase()} +$lastDailyBonus"
        }
        if (lastRiftBreak && lastRiftBreakBonus > 0) {
            signals += "${t("RIFT BREAK").uppercase()} +$lastRiftBreakBonus"
        }
        if (lastStreakMilestoneBonus > 0) {
            signals += "${t("STREAK SURGE").uppercase()} x$streak +$lastStreakMilestoneBonus"
        }
        signals += rewardLine(newSkin, nextRewardText, t)
        return signals.joinToString(" | ")
    }
}
