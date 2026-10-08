package com.moonsolstudios.kavvoro.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.BuildConfig
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.LevelProgression
import com.moonsolstudios.kavvoro.model.LevelProgressionLogic
import com.moonsolstudios.kavvoro.model.MissionId
import com.moonsolstudios.kavvoro.model.NextReward
import com.moonsolstudios.kavvoro.model.ProfileExperienceLogic
import com.moonsolstudios.kavvoro.model.UnlockRule
import com.moonsolstudios.kavvoro.model.UnlockType
import kotlin.math.max

/**
 * Manages game progress, SharedPreferences persistence, unlock checks, streaks, and hype bank.
 */
class GameProgressRepository(
    private val prefs: SharedPreferences,
    private val ballSkins: List<BallSkin>,
    private val premiumPricesBySkin: Map<String, String> = emptyMap(),
    private val t: (String) -> String = { it }
) {

    /** Clears gameplay/progression data without changing the user's preferences. */
    fun resetAllProgressPreservingSettings() {
        val settingsKeys = setOf(
            SFX_MUTED_KEY,
            MUSIC_MUTED_KEY,
            SETTINGS_MASTER_VOLUME_KEY,
            SETTINGS_MUSIC_VOLUME_KEY,
            SETTINGS_SFX_VOLUME_KEY,
            SETTINGS_HAPTIC_KEY,
            SETTINGS_SCREEN_SHAKE_KEY,
            SETTINGS_PERFORMANCE_KEY
        )
        prefs.edit {
            prefs.all.keys.filterNot { it in settingsKeys }.forEach { remove(it) }
        }
    }

    companion object {
        const val DEFAULT_SKIN_ID = "nodlo"
        const val SELECTED_SKIN_KEY = "selected_ball_skin"
        const val SFX_MUTED_KEY = "sfx_muted"
        const val MUSIC_MUTED_KEY = "music_muted"
        const val SETTINGS_MASTER_VOLUME_KEY = "settings_master_volume"
        const val SETTINGS_MUSIC_VOLUME_KEY = "settings_music_volume"
        const val SETTINGS_SFX_VOLUME_KEY = "settings_sfx_volume"
        const val SETTINGS_HAPTIC_KEY = "settings_haptic_enabled"
        const val SETTINGS_SCREEN_SHAKE_KEY = "settings_screen_shake"
        const val SETTINGS_PERFORMANCE_KEY = "settings_performance_mode"
        const val BEST_STREAK_KEY = "best_streak"
        const val SHARE_COUNT_KEY = "share_count"
        const val HYPE_BANK_KEY = "hype_bank"
        const val PROFILE_XP_KEY = "profile_xp"
        const val PREMIUM_PRICE_KEY = "premium_price_label"
        const val DAILY_STREAK_KEY = "daily_rift_login_streak"
        const val LAST_CLAIMED_SEED_KEY = "daily_rift_last_claimed_seed"
        const val MINUTE_MILLIS = 60_000L
        const val HOUR_MILLIS = 60L * MINUTE_MILLIS
        const val DAY_MILLIS = 24L * HOUR_MILLIS

        private val EARNED_KEYS = BallSkinCatalog.ALL_SKINS.associate { it.id to "skin_unlocked_${it.id}" }
        private val PREMIUM_PRICE_KEYS = BallSkinCatalog.ALL_SKINS.associate { it.id to "premium_price_${it.id}" }
        private val PURCHASED_KEYS = BallSkinCatalog.ALL_SKINS.associate { it.id to "skin_purchased_${it.id}" }

        fun earnedSkinKey(id: String): String = EARNED_KEYS[id] ?: "skin_unlocked_$id"
        fun premiumPriceKey(id: String): String = PREMIUM_PRICE_KEYS[id] ?: "premium_price_$id"
        fun purchasedSkinKey(id: String): String = PURCHASED_KEYS[id] ?: "skin_purchased_$id"
        fun failContinueCountKey(mode: GameMode, levelNumber: Int): String =
            "fail_continue_${mode.name.lowercase()}_$levelNumber"
        fun bestKey(mode: GameMode, levelNumber: Int): String =
            "best_rank_${mode.name.lowercase()}_$levelNumber"
        fun progressKey(mode: GameMode): String = when (mode) {
            GameMode.CLASSIC -> "classic_level"
            GameMode.CHAOS -> "chaos_level"
        }
        fun streakKey(mode: GameMode): String = when (mode) {
            GameMode.CLASSIC -> "streak_classic"
            GameMode.CHAOS -> "streak_chaos"
        }
    }

    private val skinById: Map<String, BallSkin> = ballSkins.associateBy { it.id }
    private val defaultSkin: BallSkin = skinById[DEFAULT_SKIN_ID] ?: ballSkins.first()

    fun isSkinUnlocked(skin: BallSkin): Boolean {
        if (BuildConfig.FORCE_UNLOCK_ALL_BRAINBALLS) return true
        if (skin.unlock.type == UnlockType.DEFAULT) return true
        if (skin.unlock.type == UnlockType.PREMIUM) return prefs.getBoolean(purchasedSkinKey(skin.id), false)
        if (skin.unlock.type == UnlockType.HYPE_COST) return prefs.getBoolean(earnedSkinKey(skin.id), false)
        if (prefs.getBoolean(earnedSkinKey(skin.id), false)) return true
        if (!unlockConditionMet(skin.unlock)) return false
        prefs.edit { putBoolean(earnedSkinKey(skin.id), true) }
        return true
    }

    fun unlockConditionMet(rule: UnlockRule): Boolean {
        return when (rule.type) {
            UnlockType.DEFAULT -> true
            UnlockType.PREMIUM -> false
            UnlockType.CLASSIC_LEVEL -> clearedLevel(GameMode.CLASSIC) >= rule.value
            UnlockType.CHAOS_LEVEL -> clearedLevel(GameMode.CHAOS) >= rule.value
            UnlockType.TUTORIAL_CLEAR -> max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS)) >= rule.value
            UnlockType.MISSION_REWARD -> false
            UnlockType.BEST_STREAK -> bestStreak() >= rule.value
            UnlockType.SHARE_COUNT -> prefs.getInt(SHARE_COUNT_KEY, 0) >= rule.value
            UnlockType.HYPE_COST -> false
        }
    }

    fun unlockedSkinIds(): Set<String> {
        return ballSkins.filter(::isSkinUnlocked).map { it.id }.toSet()
    }

    fun unlockedSkinCount(): Int = ballSkins.count(::isSkinUnlocked)

    fun selectedBallSkin(selectedSkinId: String): BallSkin {
        val candidate = skinById[selectedSkinId]
        return if (candidate != null && isSkinUnlocked(candidate)) {
            candidate
        } else {
            defaultSkin
        }
    }

    fun bestStreak(): Int = prefs.getInt(BEST_STREAK_KEY, prefs.getInt("clear_streak", 0)).coerceAtLeast(0)

    fun hypeBalance(): Int = prefs.getInt(HYPE_BANK_KEY, prefs.getInt("last_hype", 0)).coerceAtLeast(0)

    fun profileExperiencePoints(): Int = prefs.getInt(PROFILE_XP_KEY, 0).coerceAtLeast(0)

    fun profileLevel(): Int = ProfileExperienceLogic.fromTotalXp(profileExperiencePoints()).level

    fun addHype(amount: Int) = synchronized(prefs) {
        val next = (hypeBalance().toLong() + amount.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        prefs.edit { putInt(HYPE_BANK_KEY, next) }
    }

    fun spendHype(amount: Int) = synchronized(prefs) {
        val next = (hypeBalance().toLong() - amount.coerceAtLeast(0).toLong())
            .coerceAtLeast(0L)
            .toInt()
        prefs.edit { putInt(HYPE_BANK_KEY, next) }
    }

    /** Persists one successful level clear as a single, monotonic progression update. */
    fun recordLevelWin(
        mode: GameMode,
        completedLevel: Int,
        currentStreak: Int,
        hypeReward: Int
    ): LevelProgression = synchronized(prefs) {
        val progression = LevelProgressionLogic.recordWin(
            currentLevel = modeProgress(mode),
            highestLevel = modeHighestLevel(mode),
            completedLevel = completedLevel,
            currentStreak = currentStreak,
            previousBestStreak = modeBestStreak(mode)
        )
        val safeStreak = currentStreak.coerceAtLeast(0)
        val safeHypeReward = hypeReward.coerceAtLeast(0)
        val newProfileXp = ProfileExperienceLogic.addXp(
            profileExperiencePoints(),
            ProfileExperienceLogic.rewardForWin(mode)
        )
        val newHypeBalance = (hypeBalance().toLong() + safeHypeReward.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        prefs.edit {
            putInt(progressKey(mode), progression.currentLevel)
            putInt(streakKey(mode), safeStreak)
            putInt(highestLevelKey(mode), progression.highestLevel)
            putInt(bestModeStreakKey(mode), progression.bestStreak)
            putInt(BEST_STREAK_KEY, max(bestStreak(), safeStreak))
            putInt("clear_streak", safeStreak)
            putInt("last_hype", safeHypeReward)
            putInt(HYPE_BANK_KEY, newHypeBalance)
            putInt(PROFILE_XP_KEY, newProfileXp)
        }
        progression
    }

    fun skinHypePrice(skin: BallSkin): Int? {
        if (skin.unlock.type == UnlockType.DEFAULT) return null
        if (skin.unlock.type == UnlockType.PREMIUM) return null
        if (skin.unlock.type == UnlockType.HYPE_COST) return skin.unlock.value

        return when (skin.unlock.type) {
            UnlockType.TUTORIAL_CLEAR -> 5_000
            UnlockType.CLASSIC_LEVEL,
            UnlockType.CHAOS_LEVEL -> {
                val lvl = skin.unlock.value
                when {
                    lvl <= 25 -> 6_000
                    lvl <= 50 -> 12_000
                    lvl <= 75 -> 20_000
                    lvl <= 100 -> 32_000
                    lvl <= 150 -> 48_000
                    lvl <= 200 -> 68_000
                    lvl <= 250 -> 92_000
                    lvl <= 300 -> 120_000
                    else -> 160_000
                }
            }
            UnlockType.BEST_STREAK -> {
                val strk = skin.unlock.value
                when {
                    strk <= 15 -> 6_000
                    strk <= 25 -> 12_000
                    strk <= 35 -> 20_000
                    strk <= 50 -> 35_000
                    strk <= 150 -> 75_000
                    else -> 120_000
                }
            }
            UnlockType.SHARE_COUNT -> {
                val shares = skin.unlock.value
                when {
                    shares <= 5 -> 5_000
                    shares <= 10 -> 10_000
                    shares <= 20 -> 22_000
                    shares <= 30 -> 38_000
                    else -> 60_000
                }
            }
            else -> null
        }
    }

    fun formatHypeAmount(value: Int): String {
        val safe = value.coerceAtLeast(0)
        return when {
            safe >= 1_000_000 -> "${safe / 100_000 / 10f}M"
            safe >= 10_000 -> "${safe / 1_000}K"
            safe >= 1_000 -> {
                val tenths = safe / 100
                "${tenths / 10}.${tenths % 10}K"
            }
            else -> safe.toString()
        }
    }

    fun clearedLevel(mode: GameMode): Int = (modeHighestLevel(mode) - 1).coerceAtLeast(0)

    data class DailyClaimResult(
        val amount: Int,
        val streakDay: Int,
        val isJackpot: Boolean,
        val alreadyClaimed: Boolean
    )

    fun dailyRiftStreak(): Int = prefs.getInt(DAILY_STREAK_KEY, 1).coerceIn(1, 7)

    fun dailyRiftRewardForDay(day: Int): Int {
        return when (day.coerceIn(1, 7)) {
            1 -> 1_000
            2 -> 1_250
            3 -> 1_500
            4 -> 1_750
            5 -> 2_000
            6 -> 2_500
            7 -> 5_000
            else -> 1_000
        }
    }

    fun claimDailyRiftHome(): DailyClaimResult {
        val currentSeed = LevelDirector.dailySeed()
        val key = dailyRiftBonusKey()
        if (prefs.getBoolean(key, false)) {
            return DailyClaimResult(0, dailyRiftStreak(), isJackpot = false, alreadyClaimed = true)
        }
        val lastSeed = prefs.getLong(LAST_CLAIMED_SEED_KEY, 0L)
        val currentStreak = if (lastSeed == currentSeed - 1L) {
            (prefs.getInt(DAILY_STREAK_KEY, 0) % 7) + 1
        } else if (lastSeed == currentSeed) {
            prefs.getInt(DAILY_STREAK_KEY, 1)
        } else {
            1
        }

        val reward = dailyRiftRewardForDay(currentStreak)
        addHype(reward)
        prefs.edit {
            putBoolean(key, true)
            putInt(dailyRiftBonusAmountKey(), reward)
            putString(dailyRiftBonusModeKey(), "HOME")
            putLong(dailyRiftBonusClaimedAtKey(), System.currentTimeMillis())
            putLong(LAST_CLAIMED_SEED_KEY, currentSeed)
            putInt(DAILY_STREAK_KEY, currentStreak)
        }
        return DailyClaimResult(reward, currentStreak, isJackpot = currentStreak == 7, alreadyClaimed = false)
    }

    fun claimDailyRiftBonus(gameMode: GameMode): Int {
        val key = dailyRiftBonusKey()
        if (prefs.getBoolean(key, false)) return 0
        val bonus = dailyRiftBonusForMode(gameMode)
        addHype(bonus)
        prefs.edit {
            putBoolean(key, true)
            putInt(dailyRiftBonusAmountKey(), bonus)
            putString(dailyRiftBonusModeKey(), gameMode.name)
            putLong(dailyRiftBonusClaimedAtKey(), System.currentTimeMillis())
        }
        return bonus
    }

    fun dailyRiftBonusClaimed(): Boolean = prefs.getBoolean(dailyRiftBonusKey(), false)

    fun dailyRiftBonusKey(): String = "daily_rift_bonus_${LevelDirector.dailySeed()}"

    fun dailyRiftBonusAmountKey(): String = "${dailyRiftBonusKey()}_amount"

    fun dailyRiftBonusModeKey(): String = "${dailyRiftBonusKey()}_mode"

    fun dailyRiftBonusClaimedAtKey(): String = "${dailyRiftBonusKey()}_claimed_at"

    fun dailyRiftBonusForMode(mode: GameMode): Int = if (mode == GameMode.CHAOS) 640 else 320

    fun dailyRiftResetText(): String {
        val remaining = dailyRiftRemainingMillis()
        val hours = remaining / HOUR_MILLIS
        val minutes = ((remaining % HOUR_MILLIS) / MINUTE_MILLIS).coerceAtLeast(1L)
        return "${t("RESET").uppercase()} ${hours}H ${minutes}M"
    }

    fun dailyRiftRemainingMillis(): Long {
        val now = System.currentTimeMillis()
        val nextReset = (LevelDirector.dailySeed() + 1L) * DAY_MILLIS
        return (nextReset - now).coerceIn(0L, DAY_MILLIS)
    }

    fun nextRewardText(excludeId: String? = null): String? {
        val next = nextRewardInfo(excludeId)
        return next?.let { "${it.name} ${t("AT").uppercase()} ${it.label.uppercase()}" }
    }

    fun nextRewardInfo(excludeId: String? = null): NextReward? {
        return ballSkins
            .filter { it.id != excludeId && it.unlock.type != UnlockType.PREMIUM && !isSkinUnlocked(it) }
            .mapNotNull { skin ->
                rewardDistance(skin.unlock)?.let { distance ->
                    val current = rewardProgressValue(skin.unlock)
                    val target = skin.unlock.value.coerceAtLeast(1)
                    NextReward(
                        name = skin.name,
                        label = unlockShortLabel(skin),
                        target = target,
                        distance = distance,
                        progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f),
                        accent = skin.lineColor
                    )
                }
            }
            .minWithOrNull(compareBy<NextReward> { it.distance }.thenBy { it.label })
    }

    fun rewardDistance(rule: UnlockRule): Int? {
        return when (rule.type) {
            UnlockType.CLASSIC_LEVEL -> (rule.value - clearedLevel(GameMode.CLASSIC)).coerceAtLeast(0)
            UnlockType.CHAOS_LEVEL -> (rule.value - clearedLevel(GameMode.CHAOS)).coerceAtLeast(0)
            UnlockType.TUTORIAL_CLEAR -> (rule.value - max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS))).coerceAtLeast(0)
            UnlockType.BEST_STREAK -> (rule.value - bestStreak()).coerceAtLeast(0)
            UnlockType.SHARE_COUNT -> (rule.value - prefs.getInt(SHARE_COUNT_KEY, 0)).coerceAtLeast(0)
            UnlockType.HYPE_COST -> (rule.value - hypeBalance()).coerceAtLeast(0)
            UnlockType.MISSION_REWARD -> rule.missionId?.let { mission ->
                (mission.target - missionProgress(mission)).coerceAtLeast(0)
            }
            UnlockType.DEFAULT,
            UnlockType.PREMIUM -> null
        }
    }

    fun rewardProgressValue(rule: UnlockRule): Int {
        return when (rule.type) {
            UnlockType.CLASSIC_LEVEL -> clearedLevel(GameMode.CLASSIC)
            UnlockType.CHAOS_LEVEL -> clearedLevel(GameMode.CHAOS)
            UnlockType.TUTORIAL_CLEAR -> max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS))
            UnlockType.BEST_STREAK -> bestStreak()
            UnlockType.SHARE_COUNT -> prefs.getInt(SHARE_COUNT_KEY, 0)
            UnlockType.HYPE_COST -> hypeBalance()
            UnlockType.MISSION_REWARD -> rule.missionId?.let(::missionProgress) ?: 0
            UnlockType.DEFAULT,
            UnlockType.PREMIUM -> 0
        }
    }

    fun unlockShortLabel(skin: BallSkin): String {
        return when (skin.unlock.type) {
            UnlockType.DEFAULT -> t("UNLOCKED").uppercase()
            UnlockType.PREMIUM -> premiumPriceLabel(skin)
            UnlockType.CLASSIC_LEVEL -> "${t("CLASSIC")} L${skin.unlock.value.toString().padStart(2, '0')}"
            UnlockType.CHAOS_LEVEL -> "${t("CHAOS")} L${skin.unlock.value.toString().padStart(2, '0')}"
            UnlockType.TUTORIAL_CLEAR -> "${t("TUTORIAL")} L${skin.unlock.value.toString().padStart(2, '0')}"
            UnlockType.MISSION_REWARD -> {
                val ready = skin.unlock.missionId?.let { missionProgress(it) >= it.target } == true
                if (ready) "${t("MISSIONS").uppercase()} ${t("READY").uppercase()}"
                else "${t("MISSIONS").uppercase()} L${skin.unlock.value.toString().padStart(2, '0')}"
            }
            UnlockType.BEST_STREAK -> "${t("STREAK")} ${skin.unlock.value}"
            UnlockType.SHARE_COUNT -> "${t("SHARE")} ${skin.unlock.value}"
            UnlockType.HYPE_COST -> "${formatHypeAmount(skin.unlock.value)} ${t("HYPE").uppercase()}"
        }
    }

    fun unlockLongLabel(skin: BallSkin): String {
        return when (skin.unlock.type) {
            UnlockType.PREMIUM -> "${premiumPriceLabel(skin)} - ${t("local price from Play Billing")}"
            UnlockType.HYPE_COST -> "${t("UNLOCK WITH").uppercase()} ${formatHypeAmount(skin.unlock.value)} ${t("HYPE").uppercase()} / ${t("HYPE BANK").uppercase()} ${formatHypeAmount(hypeBalance())}"
            UnlockType.MISSION_REWARD -> {
                val mission = skin.unlock.missionId
                if (mission == null) unlockShortLabel(skin)
                else "${t("RIFT CHALLENGES").uppercase()} / ${t(mission.titleKey).uppercase()}"
            }
            // UnlockRule.label is retained as an English data/debug description,
            // but must never be rendered directly. Build the visible label from
            // the structured rule so every locale uses the same vocabulary.
            else -> unlockShortLabel(skin)
        }
    }

    fun premiumPriceLabel(skin: BallSkin): String {
        return premiumPricesBySkin[skin.id]
            ?: prefs.getString(premiumPriceKey(skin.id), null)
            ?: prefs.getString(PREMIUM_PRICE_KEY, "0.99 LOCAL")
            ?: "0.99 LOCAL"
    }

    fun modeProgress(mode: GameMode): Int {
        return when (mode) {
            GameMode.CLASSIC -> prefs.getInt(progressKey(mode), prefs.getInt("unlocked_level", 1)).coerceAtLeast(1)
            GameMode.CHAOS -> prefs.getInt(progressKey(mode), prefs.getInt("chaos_level", 1)).coerceAtLeast(1)
        }
    }

    fun modeStreak(mode: GameMode, currentStreak: Int = 0): Int {
        return prefs.getInt(streakKey(mode), currentStreak).coerceAtLeast(0)
    }

    fun modeHighestLevel(mode: GameMode): Int {
        return prefs.getInt(highestLevelKey(mode), modeProgress(mode)).coerceAtLeast(1)
    }

    fun modeBestStreak(mode: GameMode, currentStreak: Int = 0): Int {
        return prefs.getInt(bestModeStreakKey(mode), modeStreak(mode, currentStreak)).coerceAtLeast(0)
    }

    private fun missionProgress(mission: MissionId): Int {
        val savedProgress = prefs.getInt("rift_challenge_${mission.name.lowercase()}_progress", 0)
        val levelProgress = mission.levelMilestoneMode?.let(::clearedLevel) ?: 0
        return max(savedProgress, levelProgress).coerceIn(0, mission.target)
    }

    fun resetModeProgress(mode: GameMode) {
        val existingHighestLevel = modeHighestLevel(mode)
        val existingBestStreak = modeBestStreak(mode)
        prefs.edit {
            putInt(highestLevelKey(mode), existingHighestLevel)
            putInt(bestModeStreakKey(mode), existingBestStreak)
            putInt(progressKey(mode), 1)
            putInt(streakKey(mode), 0)
            putInt(freeFailContinueKey(mode), 0)
            putInt(continueAdStreakKey(mode), 0)
            putInt(levelAdKey(mode), 0)
        }
    }

    fun highestLevelKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "highest_level_classic"
        GameMode.CHAOS -> "highest_level_chaos"
    }

    fun fairHighestLevelKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "fair_highest_level_classic"
        GameMode.CHAOS -> "fair_highest_level_chaos"
    }

    fun bestModeStreakKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "best_streak_classic"
        GameMode.CHAOS -> "best_streak_chaos"
    }

    fun fairBestStreakKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "fair_best_streak_classic"
        GameMode.CHAOS -> "fair_best_streak_chaos"
    }

    fun freeFailContinueKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "free_fail_continue_classic"
        GameMode.CHAOS -> "free_fail_continue_chaos"
    }

    fun continueAdStreakKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "continue_ad_streak_classic"
        GameMode.CHAOS -> "continue_ad_streak_chaos"
    }

    fun levelAdKey(mode: GameMode): String = when (mode) {
        GameMode.CLASSIC -> "level_ad_checkpoint_classic"
        GameMode.CHAOS -> "level_ad_checkpoint_chaos"
    }

    fun breakStreak(mode: GameMode) {
        prefs.edit {
            putInt(streakKey(mode), 0)
            putInt("clear_streak", 0)
        }
    }
}
