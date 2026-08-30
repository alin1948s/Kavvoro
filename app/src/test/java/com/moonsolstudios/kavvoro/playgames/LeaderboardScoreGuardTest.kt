package com.moonsolstudios.kavvoro.playgames

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardScoreGuardTest {
    @Test
    fun `rejects negative zero incomplete and impossible scores`() {
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(-1, true, 4))
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(0, true, 4))
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(4, false, 4))
        assertFalse(LeaderboardScoreGuard.isSubmitAllowed(5, true, 4))
    }

    @Test
    fun `accepts positive completed classic and chaos progress`() {
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(4, true, 4))
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(12, true, 18))
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(7, true, 7))
        assertTrue(LeaderboardScoreGuard.isSubmitAllowed(9, true, 11))
    }
}
