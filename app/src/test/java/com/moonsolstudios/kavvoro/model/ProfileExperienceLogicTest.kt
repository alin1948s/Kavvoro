package com.moonsolstudios.kavvoro.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileExperienceLogicTest {
    @Test
    fun chaosAwardsExactlyTwiceTheClassicProfileXp() {
        val classic = ProfileExperienceLogic.rewardForWin(GameMode.CLASSIC)
        val chaos = ProfileExperienceLogic.rewardForWin(GameMode.CHAOS)

        assertEquals(100, classic)
        assertEquals(classic * 2, chaos)
    }

    @Test
    fun profileLevelAndRemainderAdvanceAtStableThresholds() {
        assertEquals(ProfileExperience(0, 1, 0), ProfileExperienceLogic.fromTotalXp(0))
        assertEquals(ProfileExperience(999, 1, 999), ProfileExperienceLogic.fromTotalXp(999))
        assertEquals(ProfileExperience(1_000, 2, 0), ProfileExperienceLogic.fromTotalXp(1_000))
    }

    @Test
    fun xpInputIsClampedAndAccumulationSaturatesInsteadOfOverflowing() {
        assertEquals(0, ProfileExperienceLogic.addXp(-50, -5))
        assertEquals(Int.MAX_VALUE, ProfileExperienceLogic.addXp(Int.MAX_VALUE - 10, 50))
        assertEquals(ProfileExperience(0, 1, 0), ProfileExperienceLogic.fromTotalXp(-1))
    }
}
