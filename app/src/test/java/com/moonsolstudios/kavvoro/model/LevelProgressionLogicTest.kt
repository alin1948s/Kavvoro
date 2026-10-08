package com.moonsolstudios.kavvoro.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LevelProgressionLogicTest {
    @Test
    fun clearsAdvanceFromLevelOneAndPreserveModeHighWaterMark() {
        assertEquals(
            LevelProgression(currentLevel = 2, highestLevel = 2, bestStreak = 1),
            LevelProgressionLogic.recordWin(
                currentLevel = 1,
                highestLevel = 1,
                completedLevel = 1,
                currentStreak = 1,
                previousBestStreak = 0
            )
        )
        assertEquals(
            LevelProgression(currentLevel = 30, highestLevel = 31, bestStreak = 8),
            LevelProgressionLogic.recordWin(
                currentLevel = 30,
                highestLevel = 31,
                completedLevel = 7,
                currentStreak = 6,
                previousBestStreak = 8
            )
        )
    }

    @Test
    fun invalidProgressIsClampedAndAdvancementSaturatesInsteadOfOverflowing() {
        val progression = LevelProgressionLogic.recordWin(
            currentLevel = -8,
            highestLevel = -4,
            completedLevel = Int.MAX_VALUE,
            currentStreak = -1,
            previousBestStreak = -2
        )

        assertEquals(Int.MAX_VALUE, progression.currentLevel)
        assertEquals(Int.MAX_VALUE, progression.highestLevel)
        assertEquals(0, progression.bestStreak)
        assertEquals(Int.MAX_VALUE, LevelProgressionLogic.nextLevel(Int.MAX_VALUE))

        val invalidCompletion = LevelProgressionLogic.recordWin(
            currentLevel = 1,
            highestLevel = 1,
            completedLevel = 0,
            currentStreak = 0,
            previousBestStreak = 0
        )
        assertEquals(1, invalidCompletion.currentLevel)
        assertEquals(1, invalidCompletion.highestLevel)
    }
}
