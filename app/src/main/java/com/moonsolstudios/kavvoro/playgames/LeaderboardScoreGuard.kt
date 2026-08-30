package com.moonsolstudios.kavvoro.playgames

internal object LeaderboardScoreGuard {
    @Suppress("UNUSED_PARAMETER")
    fun isSubmitAllowed(
        score: Int,
        completedRun: Boolean,
        currentProgress: Int,
    ): Boolean {
        if (!completedRun || score <= 0 || currentProgress <= 0) return false
        return score <= currentProgress
    }
}
