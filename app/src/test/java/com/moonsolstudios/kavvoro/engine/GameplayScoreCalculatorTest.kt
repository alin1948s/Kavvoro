package com.moonsolstudios.kavvoro.engine

import com.moonsolstudios.kavvoro.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayScoreCalculatorTest {

    @Test
    fun calculateHypeScoreAwardsCorrectBonusForSRankChaos() {
        val score = GameplayScoreCalculator.calculateHypeScore(
            rank = "S",
            gameMode = GameMode.CHAOS,
            seconds = 4.5f,
            inkUsed = 50f,
            inkLimit = 100f,
            streak = 3,
            maxChain = 2
        )
        // 900 (S rank) + 360 (Chaos) + (4.5 * 72 = 324) + (0.5 * 520 = 260) + 3 * 80 (240) + 2 * 120 (240)
        // Total = 900 + 360 + 324 + 260 + 240 + 240 = 2324
        assertEquals(2324, score)
    }

    @Test
    fun calculateStreakMilestoneAwardsBonusEveryFiveStreaks() {
        assertEquals(0, GameplayScoreCalculator.calculateStreakMilestoneBonus(0))
        assertEquals(0, GameplayScoreCalculator.calculateStreakMilestoneBonus(4))
        assertEquals(340, GameplayScoreCalculator.calculateStreakMilestoneBonus(5)) // 250 + 5 * 18 = 340
        assertEquals(430, GameplayScoreCalculator.calculateStreakMilestoneBonus(10)) // 250 + 10 * 18 = 430
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
