package com.moonsolstudios.kavvoro.repository

import com.moonsolstudios.kavvoro.model.MissionId
import com.moonsolstudios.kavvoro.model.UnlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionSkinRewardCatalogTest {
    @Test
    fun missionSkinRewardsResolveToMatchingMissionGatedSkins() {
        val rewardedMissions = MissionId.entries.filter { it.rewardSkinId != null }
        val skinsById = BallSkinCatalog.ALL_SKINS.associateBy { it.id }

        assertEquals(rewardedMissions.size, rewardedMissions.map { it.rewardSkinId }.toSet().size)
        rewardedMissions.forEach { mission ->
            val skin = requireNotNull(skinsById[mission.rewardSkinId]) {
                "${mission.name} points to a missing skin"
            }
            assertEquals(UnlockType.MISSION_REWARD, skin.unlock.type)
            assertEquals(mission, skin.unlock.missionId)
            assertEquals(mission.target, skin.unlock.value)
        }
        assertTrue(rewardedMissions.any { it == MissionId.RIFT_CLASSIC_LEVELS })
        assertTrue(rewardedMissions.any { it == MissionId.RIFT_CHAOS_LEVELS })
    }
}
