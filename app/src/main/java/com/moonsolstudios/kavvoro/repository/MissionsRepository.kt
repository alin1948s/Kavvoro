package com.moonsolstudios.kavvoro.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.model.MissionCategory
import com.moonsolstudios.kavvoro.model.MissionId
import com.moonsolstudios.kavvoro.model.MissionProgress
import com.moonsolstudios.kavvoro.model.MissionProgressLogic
import com.moonsolstudios.kavvoro.model.MissionRoundResult

/** Persists daily missions and profile-wide Rift Challenges in the active profile. */
class MissionsRepository(
    private val prefs: SharedPreferences,
    private val dayProvider: () -> Long = { LevelDirector.dailySeed() }
) {
    fun missions(category: MissionCategory): List<MissionProgress> = synchronized(prefs) {
        ensureCurrentDay()
        readMissions(category)
    }

    fun recordRound(result: MissionRoundResult): List<MissionProgress> = synchronized(prefs) {
        ensureCurrentDay()
        val previous = MissionId.entries.map(::readMission)
        val updated = MissionProgressLogic.recordRound(previous, result)
        prefs.edit {
            updated.forEach { mission -> putInt(progressKey(mission.id), mission.progress) }
        }
        MissionProgressLogic.newlyCompleted(previous, updated)
    }

    /** Returns zero when the mission is incomplete or was already claimed. */
    fun claim(missionId: MissionId): Int = synchronized(prefs) {
        ensureCurrentDay()
        val mission = readMission(missionId)
        if (!mission.canClaim) return@synchronized 0
        prefs.edit { putBoolean(claimedKey(missionId), true) }
        mission.rewardCoins
    }

    private fun ensureCurrentDay() {
        val day = dayProvider()
        if (prefs.getLong(DAILY_DAY_KEY, Long.MIN_VALUE) == day) return
        prefs.edit {
            putLong(DAILY_DAY_KEY, day)
            MissionId.inCategory(MissionCategory.DAILY).forEach { mission ->
                putInt(progressKey(mission), 0)
                putBoolean(claimedKey(mission), false)
            }
        }
    }

    private fun readMissions(category: MissionCategory): List<MissionProgress> =
        MissionId.inCategory(category).map(::readMission)

    private fun readMission(missionId: MissionId): MissionProgress = MissionProgress(
        id = missionId,
        progress = prefs.getInt(progressKey(missionId), 0).coerceIn(0, missionId.target),
        claimed = prefs.getBoolean(claimedKey(missionId), false)
    )

    private fun progressKey(id: MissionId): String =
        if (id.category == MissionCategory.DAILY) "daily_mission_${id.name.lowercase()}_progress"
        else "rift_challenge_${id.name.lowercase()}_progress"

    private fun claimedKey(id: MissionId): String =
        if (id.category == MissionCategory.DAILY) "daily_mission_${id.name.lowercase()}_claimed"
        else "rift_challenge_${id.name.lowercase()}_claimed"

    private companion object {
        const val DAILY_DAY_KEY = "daily_missions_day"
    }
}
