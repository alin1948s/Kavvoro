package com.moonsolstudios.kavvoro.model

enum class DailyMissionId(
    val titleKey: String,
    val target: Int,
    val rewardCoins: Int
) {
    PLAY_ROUNDS("PLAY 3 ROUNDS", target = 3, rewardCoins = 200),
    WIN_ROUND("WIN 1 ROUND", target = 1, rewardCoins = 300),
    EARN_COINS("EARN 500 COINS", target = 500, rewardCoins = 500)
}

data class DailyMissionProgress(
    val id: DailyMissionId,
    val progress: Int,
    val claimed: Boolean
) {
    val target: Int get() = id.target
    val rewardCoins: Int get() = id.rewardCoins
    val isComplete: Boolean get() = progress >= target
    val canClaim: Boolean get() = isComplete && !claimed
}

object DailyMissionProgressLogic {
    fun recordRound(
        missions: List<DailyMissionProgress>,
        won: Boolean,
        coinsEarned: Int
    ): List<DailyMissionProgress> = missions.map { mission ->
        val delta = when (mission.id) {
            DailyMissionId.PLAY_ROUNDS -> 1
            DailyMissionId.WIN_ROUND -> if (won) 1 else 0
            DailyMissionId.EARN_COINS -> if (won) coinsEarned.coerceAtLeast(0) else 0
        }
        mission.copy(progress = (mission.progress.toLong() + delta).coerceAtMost(mission.target.toLong()).toInt())
    }

    fun newlyCompleted(
        previous: List<DailyMissionProgress>,
        updated: List<DailyMissionProgress>
    ): List<DailyMissionProgress> = updated.filter { mission ->
        mission.isComplete && previous.none { old -> old.id == mission.id && old.isComplete }
    }
}
