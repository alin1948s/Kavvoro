package com.moonsolstudios.kavvoro.engine

import com.moonsolstudios.kavvoro.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayScoreCalculatorTest {

    @Test
    fun chaosAwardsExactlyDoubleTheClassicHypeForEquivalentPlay() {
        val classic = GameplayScoreCalculator.calculateHypeScore(
            rank = "S",
            gameMode = GameMode.CLASSIC,
            seconds = 4.5f,
            inkUsed = 50f,
            inkLimit = 100f,
            streak = 3,
            maxChain = 2
        )
        val chaos = GameplayScoreCalculator.calculateHypeScore(
            rank = "S",
            gameMode = GameMode.CHAOS,
            seconds = 4.5f,
            inkUsed = 50f,
            inkLimit = 100f,
            streak = 3,
            maxChain = 2
        )
        assertEquals(2_084, classic)
        assertEquals(classic * 2, chaos)
    }

    @Test
    fun calculateStreakMilestoneAwardsBonusEveryFiveStreaks() {
        assertEquals(0, GameplayScoreCalculator.calculateStreakMilestoneBonus(0))
        assertEquals(0, GameplayScoreCalculator.calculateStreakMilestoneBonus(4))
        assertEquals(340, GameplayScoreCalculator.calculateStreakMilestoneBonus(5)) // 250 + 5 * 18 = 340
        assertEquals(430, GameplayScoreCalculator.calculateStreakMilestoneBonus(10)) // 250 + 10 * 18 = 430
        assertEquals(680, GameplayScoreCalculator.calculateStreakMilestoneBonus(5, GameMode.CHAOS))
    }

    @Test
    fun chaosRiftBreakRewardIsExactlyDoubleTheClassicReward() {
        val classic = GameplayScoreCalculator.calculateRiftBreakBonus(
            rank = "A",
            riftEnergy = 0.35f,
            maxChain = 3,
            gameMode = GameMode.CLASSIC
        )
        val chaos = GameplayScoreCalculator.calculateRiftBreakBonus(
            rank = "A",
            riftEnergy = 0.35f,
            maxChain = 3,
            gameMode = GameMode.CHAOS
        )
        assertEquals(classic * 2, chaos)
    }

    @Test
    fun riftBreakTriggersOnLowEnergy() {
        val triggers = GameplayScoreCalculator.shouldTriggerRiftBreak(
            riftEnergy = 0.15f,
            maxChain = 1,
            gameMode = GameMode.CLASSIC,
            seconds = 5f,
            timeLimitSeconds = 15f,
            rank = "B"
        )
        assertTrue(triggers)
    }

    @Test
    fun riftBreakDoesNotTriggerOnHighEnergyLowChain() {
        val triggers = GameplayScoreCalculator.shouldTriggerRiftBreak(
            riftEnergy = 0.85f,
            maxChain = 1,
            gameMode = GameMode.CLASSIC,
            seconds = 5f,
            timeLimitSeconds = 15f,
            rank = "B"
        )
        assertFalse(triggers)
    }
}
