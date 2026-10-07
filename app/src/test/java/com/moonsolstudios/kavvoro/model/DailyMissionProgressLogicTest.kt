package com.moonsolstudios.kavvoro.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyMissionProgressLogicTest {
    @Test
    fun completedRoundsAdvancePlayAndWinMissionsButClampAtTheirTargets() {
        val initial = DailyMissionId.entries.map { DailyMissionProgress(it, 0, claimed = false) }

        val afterLoss = DailyMissionProgressLogic.recordRound(initial, won = false, coinsEarned = 900)
        assertEquals(1, afterLoss.single { it.id == DailyMissionId.PLAY_ROUNDS }.progress)
        assertEquals(0, afterLoss.single { it.id == DailyMissionId.WIN_ROUND }.progress)
        assertEquals(0, afterLoss.single { it.id == DailyMissionId.EARN_COINS }.progress)

        val afterWin = DailyMissionProgressLogic.recordRound(afterLoss, won = true, coinsEarned = 700)
        assertEquals(2, afterWin.single { it.id == DailyMissionId.PLAY_ROUNDS }.progress)
        assertEquals(1, afterWin.single { it.id == DailyMissionId.WIN_ROUND }.progress)
        assertEquals(500, afterWin.single { it.id == DailyMissionId.EARN_COINS }.progress)
        assertTrue(afterWin.single { it.id == DailyMissionId.WIN_ROUND }.canClaim)
        assertFalse(afterWin.single { it.id == DailyMissionId.PLAY_ROUNDS }.isComplete)
    }

    @Test
    fun earnedCoinsIgnoreNegativeValuesAndClaimStateIsPreserved() {
        val earnedCoins = DailyMissionProgress(DailyMissionId.EARN_COINS, 120, claimed = false)
        val afterRound = DailyMissionProgressLogic.recordRound(listOf(earnedCoins), won = true, coinsEarned = -50).single()
        assertEquals(120, afterRound.progress)
        assertFalse(afterRound.canClaim)
    }

    @Test
    fun newlyCompletedReportsOnlyTheMissionsThatCrossedTheirTargets() {
        val previous = DailyMissionId.entries.map { id ->
            DailyMissionProgress(id, if (id == DailyMissionId.PLAY_ROUNDS) 2 else 0, claimed = false)
        }
        val updated = DailyMissionProgressLogic.recordRound(previous, won = true, coinsEarned = 500)

        assertEquals(
            setOf(DailyMissionId.PLAY_ROUNDS, DailyMissionId.WIN_ROUND, DailyMissionId.EARN_COINS),
            DailyMissionProgressLogic.newlyCompleted(previous, updated).map { it.id }.toSet()
        )
        assertTrue(DailyMissionProgressLogic.newlyCompleted(updated, updated).isEmpty())
    }
}
