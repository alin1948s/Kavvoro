package com.moonsolstudios.kavvoro.model

import kotlin.math.max

/** Monotonic per-mode progress derived from a successfully cleared level. */
data class LevelProgression(
    val currentLevel: Int,
    val highestLevel: Int,
    val bestStreak: Int
)

/** Pure level advancement rules so persistence and UI cannot disagree about level semantics. */
object LevelProgressionLogic {
    fun recordWin(
        currentLevel: Int,
        highestLevel: Int,
        completedLevel: Int,
        currentStreak: Int,
        previousBestStreak: Int
    ): LevelProgression {
        val nextLevel = if (completedLevel > 0) nextLevel(completedLevel) else 1
        val safeCurrent = currentLevel.coerceAtLeast(1)
        val safeHighest = highestLevel.coerceAtLeast(1)
        val safeStreak = currentStreak.coerceAtLeast(0)
        val nextCurrent = max(safeCurrent, nextLevel)
        val nextHighest = max(safeHighest, nextCurrent)
        return LevelProgression(
            currentLevel = nextCurrent,
            highestLevel = nextHighest,
            bestStreak = max(previousBestStreak.coerceAtLeast(0), safeStreak)
        )
    }

    /** Saturates at the representable limit instead of overflowing into a negative level. */
    fun nextLevel(level: Int): Int =
        (level.coerceAtLeast(1).toLong() + 1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}
