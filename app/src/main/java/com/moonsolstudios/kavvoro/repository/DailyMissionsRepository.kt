package com.moonsolstudios.kavvoro.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.model.DailyMissionId
import com.moonsolstudios.kavvoro.model.DailyMissionProgress
import com.moonsolstudios.kavvoro.model.DailyMissionProgressLogic

/** Persists the current day's missions alongside the active local or signed-in profile. */
class DailyMissionsRepository(
    private val prefs: SharedPreferences,
    private val dayProvider: () -> Long = { LevelDirector.dailySeed() }
) {
    fun missions(): List<DailyMissionProgress> = synchronized(prefs) {
        ensureCurrentDay()
        readMissions()
    }

    fun recordRound(won: Boolean, coinsEarned: Int): List<DailyMissionProgress> = synchronized(prefs) {
        ensureCurrentDay()
        val previous = readMissions()
        val updated = DailyMissionProgressLogic.recordRound(previous, won, coinsEarned)
        prefs.edit {
            updated.forEach { mission -> putInt(progressKey(mission.id), mission.progress) }
        }
        DailyMissionProgressLogic.newlyCompleted(previous, updated)
    }

    /** Returns zero when the mission is incomplete or was already claimed. */
    fun claim(missionId: DailyMissionId): Int = synchronized(prefs) {
        ensureCurrentDay()
        val mission = readMissions().firstOrNull { it.id == missionId } ?: return@synchronized 0
        if (!mission.canClaim) return@synchronized 0
        prefs.edit { putBoolean(claimedKey(missionId), true) }
        mission.rewardCoins
    }

    private fun ensureCurrentDay() {
        val day = dayProvider()
        if (prefs.getLong(DAY_KEY, Long.MIN_VALUE) == day) return
        prefs.edit {
            putLong(DAY_KEY, day)
            DailyMissionId.entries.forEach { mission ->
                putInt(progressKey(mission), 0)
                putBoolean(claimedKey(mission), false)
            }
        }
    }

    private fun readMissions(): List<DailyMissionProgress> = DailyMissionId.entries.map { mission ->
        DailyMissionProgress(
            id = mission,
            progress = prefs.getInt(progressKey(mission), 0).coerceIn(0, mission.target),
            claimed = prefs.getBoolean(claimedKey(mission), false)
        )
    }

    private fun progressKey(id: DailyMissionId): String = "daily_mission_${id.name.lowercase()}_progress"
    private fun claimedKey(id: DailyMissionId): String = "daily_mission_${id.name.lowercase()}_claimed"

    private companion object {
        const val DAY_KEY = "daily_missions_day"
    }
}
