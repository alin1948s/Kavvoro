package com.moonsolstudios.kavvoro.model

/** Persistent account XP is separate from the per-mode level currently being played. */
data class ProfileExperience(
    val totalXp: Int,
    val level: Int,
    val xpIntoLevel: Int
)

object ProfileExperienceLogic {
    const val XP_PER_PROFILE_LEVEL = 1_000
    const val CLASSIC_WIN_XP = 100
    const val CHAOS_XP_MULTIPLIER = 2

    fun rewardForWin(mode: GameMode): Int =
        CLASSIC_WIN_XP * if (mode == GameMode.CHAOS) CHAOS_XP_MULTIPLIER else 1

    fun addXp(totalXp: Int, reward: Int): Int =
        (totalXp.coerceAtLeast(0).toLong() + reward.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

    fun fromTotalXp(totalXp: Int): ProfileExperience {
        val safeTotal = totalXp.coerceAtLeast(0)
        return ProfileExperience(
            totalXp = safeTotal,
            level = safeTotal / XP_PER_PROFILE_LEVEL + 1,
            xpIntoLevel = safeTotal % XP_PER_PROFILE_LEVEL
        )
    }
}
