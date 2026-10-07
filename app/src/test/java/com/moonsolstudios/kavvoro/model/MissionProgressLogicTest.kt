package com.moonsolstudios.kavvoro.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionProgressLogicTest {
    @Test
    fun dailyMissionsRewardWinsRanksAndRiftBreaksInsteadOfAnyRound() {
        val daily = MissionId.inCategory(MissionCategory.DAILY).map { MissionProgress(it, 0, claimed = false) }
        val loss = MissionRoundResult(
            won = false,
            gameMode = GameMode.CLASSIC,
            rank = "S",
            riftBreak = true,
            maxChain = 4
        )

        val afterLoss = MissionProgressLogic.recordRound(daily, loss)
        assertEquals(0, afterLoss.single { it.id == MissionId.DAILY_CLEAR_LEVELS }.progress)
        assertEquals(0, afterLoss.single { it.id == MissionId.DAILY_A_RANKS }.progress)
        assertEquals(0, afterLoss.single { it.id == MissionId.DAILY_RIFT_BREAKS }.progress)

        val afterSkilledWin = MissionProgressLogic.recordRound(
            afterLoss,
            MissionRoundResult(won = true, gameMode = GameMode.CHAOS, rank = "A", riftBreak = true, maxChain = 3)
        )
        assertEquals(1, afterSkilledWin.single { it.id == MissionId.DAILY_CLEAR_LEVELS }.progress)
        assertEquals(1, afterSkilledWin.single { it.id == MissionId.DAILY_A_RANKS }.progress)
        assertEquals(1, afterSkilledWin.single { it.id == MissionId.DAILY_RIFT_BREAKS }.progress)
    }

    @Test
    fun permanentChallengesTrackModeSpecificWinsRiftBreaksAndCombos() {
        val challenges = MissionId.inCategory(MissionCategory.RIFT_CHALLENGES)
            .map { MissionProgress(it, 0, claimed = false) }
        val afterClassic = MissionProgressLogic.recordRound(
            challenges,
            MissionRoundResult(won = true, gameMode = GameMode.CLASSIC, rank = "B", maxChain = 2)
        )
        assertEquals(1, afterClassic.single { it.id == MissionId.RIFT_CLASSIC_LEVELS }.progress)
        assertEquals(0, afterClassic.single { it.id == MissionId.RIFT_CHAOS_LEVELS }.progress)

        val afterChaosCombo = MissionProgressLogic.recordRound(
            afterClassic,
            MissionRoundResult(won = false, gameMode = GameMode.CHAOS, maxChain = 4)
        )
        assertEquals(1, afterChaosCombo.single { it.id == MissionId.RIFT_CHAIN_COMBOS }.progress)
        assertEquals(0, afterChaosCombo.single { it.id == MissionId.RIFT_CHAOS_LEVELS }.progress)
    }

    @Test
    fun progressClampsAndCompletionIsReportedOnlyOnce() {
        val mission = MissionProgress(MissionId.DAILY_CLEAR_LEVELS, 4, claimed = false)
        val previous = listOf(mission)
        val updated = MissionProgressLogic.recordRound(
            previous,
            MissionRoundResult(won = true, gameMode = GameMode.CLASSIC)
        )

        assertEquals(5, updated.single().progress)
        assertTrue(updated.single().canClaim)
        assertEquals(updated, MissionProgressLogic.newlyCompleted(previous, updated))
        assertFalse(updated.single().claimed)
        assertTrue(MissionProgressLogic.newlyCompleted(updated, updated).isEmpty())
    }
}
