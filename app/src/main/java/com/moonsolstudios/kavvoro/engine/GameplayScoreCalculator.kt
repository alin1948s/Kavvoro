package com.moonsolstudios.kavvoro.engine

import com.moonsolstudios.kavvoro.model.GameMode
import kotlin.math.roundToInt

/**
 * Pure domain logic for calculating run scores, hype bonuses, rift break triggers, and streak milestones.
 */
object GameplayScoreCalculator {

    fun calculateHypeScore(
        rank: String,
        gameMode: GameMode,
        seconds: Float,
        inkUsed: Float,
        inkLimit: Float,
        streak: Int,
        maxChain: Int
    ): Int {
        val rankBonus = when (rank) {
            "S" -> 900
            "A" -> 650
            "B" -> 420
            else -> 250
        }
        val modeBonus = when (gameMode) {
            GameMode.CHAOS -> 360
            GameMode.CLASSIC -> 120
        }
        val speedBonus = ((9f - seconds).coerceAtLeast(0f) * 72f).roundToInt()
        val inkBonus = ((1f - (inkUsed / inkLimit.coerceAtLeast(0.001f)).coerceIn(0f, 1f)) * 520f).roundToInt()
        return rankBonus + modeBonus + speedBonus + inkBonus + streak * 80 + maxChain * 120
    }

    fun currentHudHypeScore(
        won: Boolean,
        lost: Boolean,
        lastHypeScore: Int,
        gameMode: GameMode,
        levelIndex: Int,
        timeLimitSeconds: Float,
        simElapsed: Float,
        riftEnergy: Float,
        streak: Int,
        maxChain: Int,
        chainCount: Int
    ): Int {
        if (won && lastHypeScore > 0) return lastHypeScore
        if (lost) return 0
        val modeBonus = if (gameMode == GameMode.CHAOS) 360 else 120
        val levelBonus = (levelIndex * if (gameMode == GameMode.CHAOS) 16 else 10).coerceAtMost(520)
        val paceRatio = ((timeLimitSeconds - simElapsed).coerceAtLeast(0f) / timeLimitSeconds.coerceAtLeast(1f))
            .coerceIn(0f, 1f)
        val paceBonus = (paceRatio * 360f).roundToInt()
        val energyBonus = (riftEnergy.coerceIn(0f, 1f) * 420f).roundToInt()
        val chainBonus = (maxChain * 120 + chainCount * 34).coerceAtMost(920)
        return 220 + modeBonus + levelBonus + paceBonus + energyBonus + streak * 80 + chainBonus
    }

    fun shouldTriggerRiftBreak(
        riftEnergy: Float,
        maxChain: Int,
        gameMode: GameMode,
        seconds: Float,
        timeLimitSeconds: Float,
        rank: String
    ): Boolean {
        val lowEnergyFinish = riftEnergy <= 0.24f
        val comboSpike = maxChain >= if (gameMode == GameMode.CHAOS) 3 else 4
        val clutchTimer = seconds >= timeLimitSeconds * 0.72f && riftEnergy <= 0.36f
        val cleanHighRank = (rank == "S" || rank == "A") && maxChain >= 2 && riftEnergy <= 0.42f
        return lowEnergyFinish || comboSpike || clutchTimer || cleanHighRank
    }

    fun calculateRiftBreakBonus(
        rank: String,
        riftEnergy: Float,
        maxChain: Int,
        gameMode: GameMode
    ): Int {
        val rankBonus = if (rank == "S") 220 else if (rank == "A") 140 else 80
        val energyBonus = ((1f - riftEnergy.coerceIn(0f, 1f)) * 420f).roundToInt()
        val chainBonus = (maxChain * 65).coerceAtMost(520)
        val modeBonus = if (gameMode == GameMode.CHAOS) 180 else 90
        return 360 + rankBonus + energyBonus + chainBonus + modeBonus
    }

    fun calculateStreakMilestoneBonus(streak: Int): Int {
        if (streak <= 0 || streak % 5 != 0) return 0
        return 250 + (streak * 18).coerceAtMost(900)
    }
}
