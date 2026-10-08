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
        val speedBonus = ((9f - seconds).coerceAtLeast(0f) * 72f).roundToInt()
        val inkBonus = ((1f - (inkUsed / inkLimit.coerceAtLeast(0.001f)).coerceIn(0f, 1f)) * 520f).roundToInt()
        val classicReward = 120L + rankBonus + speedBonus + inkBonus +
            streak.coerceAtLeast(0).toLong() * 80L + maxChain.coerceAtLeast(0).toLong() * 120L
        return applyModeMultiplier(classicReward, gameMode)
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
        val safeTimeLimit = timeLimitSeconds.takeIf { it.isFinite() }?.coerceAtLeast(1f) ?: 1f
        val safeElapsed = simElapsed.takeIf { it.isFinite() } ?: 0f
        val levelBonus = (levelIndex.coerceAtLeast(0).toLong() * 10L).coerceAtMost(520L)
        val paceRatio = ((safeTimeLimit - safeElapsed).coerceAtLeast(0f) / safeTimeLimit)
            .coerceIn(0f, 1f)
        val paceBonus = (paceRatio * 360f).roundToInt()
        val energyBonus = (riftEnergy.coerceIn(0f, 1f) * 420f).roundToInt()
        val chainBonus = (maxChain.coerceAtLeast(0).toLong() * 120L + chainCount.coerceAtLeast(0).toLong() * 34L)
            .coerceAtMost(920L)
        val classicEstimate = 340L + levelBonus + paceBonus + energyBonus +
            streak.coerceAtLeast(0).toLong() * 80L + chainBonus
        return applyModeMultiplier(classicEstimate, gameMode)
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
        val rankBonus = if (rank == "S") 220L else if (rank == "A") 140L else 80L
        val energyBonus = ((1f - riftEnergy.coerceIn(0f, 1f)) * 420f).roundToInt()
        val chainBonus = (maxChain.coerceAtLeast(0).toLong() * 65L).coerceAtMost(520L)
        val classicReward = 450L + rankBonus + energyBonus + chainBonus
        return applyModeMultiplier(classicReward, gameMode)
    }

    fun calculateStreakMilestoneBonus(streak: Int, gameMode: GameMode = GameMode.CLASSIC): Int {
        if (streak <= 0 || streak % 5 != 0) return 0
        val classicReward = 250L + (streak.toLong() * 18L).coerceAtMost(900L)
        return applyModeMultiplier(classicReward, gameMode)
    }

    private fun applyModeMultiplier(classicReward: Long, gameMode: GameMode): Int {
        val multiplier = if (gameMode == GameMode.CHAOS) 2L else 1L
        return (classicReward.coerceAtLeast(0L) * multiplier)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }
}
