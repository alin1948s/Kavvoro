package com.moonsolstudios.kavvoro.model

enum class MissionCategory {
    DAILY,
    RIFT_CHALLENGES
}

enum class MissionId(
    val category: MissionCategory,
    val titleKey: String,
    val target: Int,
    val rewardCoins: Int
) {
    DAILY_CLEAR_LEVELS(MissionCategory.DAILY, "CLEAR 5 LEVELS", target = 5, rewardCoins = 500),
    DAILY_A_RANKS(MissionCategory.DAILY, "SCORE A RANK 3 TIMES", target = 3, rewardCoins = 750),
    DAILY_RIFT_BREAKS(MissionCategory.DAILY, "TRIGGER 2 RIFT BREAKS", target = 2, rewardCoins = 900),
    RIFT_CLASSIC_LEVELS(MissionCategory.RIFT_CHALLENGES, "CLEAR 25 CLASSIC LEVELS", target = 25, rewardCoins = 2_500),
    RIFT_CHAOS_LEVELS(MissionCategory.RIFT_CHALLENGES, "WIN 15 CHAOS LEVELS", target = 15, rewardCoins = 3_000),
    RIFT_BREAKS(MissionCategory.RIFT_CHALLENGES, "TRIGGER 20 RIFT BREAKS", target = 20, rewardCoins = 4_000),
    RIFT_CHAIN_COMBOS(MissionCategory.RIFT_CHALLENGES, "BUILD A 4X COMBO 10 TIMES", target = 10, rewardCoins = 2_500);

    companion object {
        fun inCategory(category: MissionCategory): List<MissionId> = entries.filter { it.category == category }
    }
}

data class MissionProgress(
    val id: MissionId,
    val progress: Int,
    val claimed: Boolean
) {
    val target: Int get() = id.target
    val rewardCoins: Int get() = id.rewardCoins
    val isComplete: Boolean get() = progress >= target
    val canClaim: Boolean get() = isComplete && !claimed
}

data class MissionRoundResult(
    val won: Boolean,
    val gameMode: GameMode,
    val rank: String? = null,
    val riftBreak: Boolean = false,
    val maxChain: Int = 0
)

object MissionProgressLogic {
    fun recordRound(
        missions: List<MissionProgress>,
        result: MissionRoundResult
    ): List<MissionProgress> = missions.map { mission ->
        val increment = when (mission.id) {
            MissionId.DAILY_CLEAR_LEVELS -> result.won
            MissionId.DAILY_A_RANKS -> result.won && (result.rank == "A" || result.rank == "S")
            MissionId.DAILY_RIFT_BREAKS, MissionId.RIFT_BREAKS -> result.won && result.riftBreak
            MissionId.RIFT_CLASSIC_LEVELS -> result.won && result.gameMode == GameMode.CLASSIC
            MissionId.RIFT_CHAOS_LEVELS -> result.won && result.gameMode == GameMode.CHAOS
            MissionId.RIFT_CHAIN_COMBOS -> result.maxChain >= 4
        }
        val progress = mission.progress.toLong() + if (increment) 1L else 0L
        mission.copy(progress = progress.coerceIn(0L, mission.target.toLong()).toInt())
    }

    fun newlyCompleted(
        previous: List<MissionProgress>,
        updated: List<MissionProgress>
    ): List<MissionProgress> = updated.filter { mission ->
        mission.isComplete && previous.none { old -> old.id == mission.id && old.isComplete }
    }
}
