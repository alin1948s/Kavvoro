package com.moonsolstudios.kavvoro.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardScoreGuardTest {
    @Test
    fun `rejects negative zero incomplete and impossible scores`() {
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CLASSIC_LEVEL, -1, true, 4))
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CLASSIC_LEVEL, 0, true, 4))
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CLASSIC_LEVEL, 4, false, 4))
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CLASSIC_LEVEL, 5, true, 4))
    }

    @Test
    fun `accepts positive completed classic and chaos progress`() {
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CLASSIC_LEVEL, 4, true, 4))
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CHAOS_LEVEL, 12, true, 18))
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CLASSIC_STREAK, 7, true, 7))
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(LeaderboardBoard.CHAOS_STREAK, 9, true, 11))
    }
}
