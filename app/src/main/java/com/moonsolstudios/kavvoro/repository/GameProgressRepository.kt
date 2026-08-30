package com.moonsolstudios.kavvoro.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.BuildConfig
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.NextReward
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
        const val PREMIUM_PRICE_KEY = "premium_price_label"
        const val MINUTE_MILLIS = 60_000L
        const val HOUR_MILLIS = 60L * MINUTE_MILLIS
        const val DAY_MILLIS = 24L * HOUR_MILLIS

        fun earnedSkinKey(id: String): String = "skin_unlocked_$id"
        fun premiumPriceKey(id: String): String = "premium_price_$id"
        fun purchasedSkinKey(id: String): String = "skin_purchased_$id"
        fun failContinueCountKey(mode: GameMode, levelNumber: Int): String =
            "fail_continue_${mode.name.lowercase()}_$levelNumber"
        fun bestKey(mode: GameMode, levelNumber: Int): String =
            "best_rank_${mode.name.lowercase()}_$levelNumber"
        fun progressKey(mode: GameMode): String = when (mode) {
            GameMode.CLASSIC -> "classic_level"
            GameMode.CHAOS -> "chaos_level"
        }
        fun streakKey(mode: GameMode): String = "streak_${mode.name.lowercase()}"
    }

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
        return ballSkins.firstOrNull { it.id == selectedSkinId && isSkinUnlocked(it) }
            ?: ballSkins.first { it.id == DEFAULT_SKIN_ID }
    }

    fun bestStreak(): Int = prefs.getInt(BEST_STREAK_KEY, prefs.getInt("clear_streak", 0)).coerceAtLeast(0)

    fun hypeBalance(): Int = prefs.getInt(HYPE_BANK_KEY, prefs.getInt("last_hype", 0)).coerceAtLeast(0)

    fun spendHype(amount: Int) {
        val next = (hypeBalance() - amount.coerceAtLeast(0)).coerceAtLeast(0)
        prefs.edit { putInt(HYPE_BANK_KEY, next) }
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

    fun claimDailyRiftBonus(gameMode: GameMode): Int {
        val key = dailyRiftBonusKey()
        if (prefs.getBoolean(key, false)) return 0
        val bonus = dailyRiftBonusForMode(gameMode)
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

    fun dailyRiftBonusForMode(mode: GameMode): Int = if (mode == GameMode.CHAOS) 420 else 320

    fun dailyRiftClaimedAmount(): Int = prefs.getInt(dailyRiftBonusAmountKey(), 0)

    fun dailyRiftClaimedMode(): GameMode? {
        return prefs.getString(dailyRiftBonusModeKey(), null)?.let { saved ->
            runCatching { GameMode.valueOf(saved) }.getOrNull()
        }
    }

    fun dailyRiftResetText(): String {
        val remaining = dailyRiftRemainingMillis()
        val hours = remaining / HOUR_MILLIS
        val minutes = ((remaining % HOUR_MILLIS) / MINUTE_MILLIS).coerceAtLeast(1L)
        return "${t("RESET").uppercase()} ${hours}H ${minutes}M"
    }

    fun dailyRiftDayProgress(): Float {
        val remaining = dailyRiftRemainingMillis().coerceIn(0L, DAY_MILLIS)
        return (1f - remaining.toFloat() / DAY_MILLIS.toFloat()).coerceIn(0.05f, 0.95f)
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

    fun nextStreakRewardInfo(): NextReward? {
        return ballSkins
            .filter { it.unlock.type == UnlockType.BEST_STREAK && !isSkinUnlocked(it) }
            .minByOrNull { it.unlock.value }
            ?.let { skin ->
                val target = skin.unlock.value.coerceAtLeast(1)
                NextReward(
                    name = skin.name,
                    label = unlockShortLabel(skin),
                    target = target,
                    distance = (target - bestStreak()).coerceAtLeast(0),
                    progress = (bestStreak().toFloat() / target.toFloat()).coerceIn(0f, 1f),
                    accent = skin.lineColor
                )
            }
    }

    fun rewardDistance(rule: UnlockRule): Int? {
        return when (rule.type) {
            UnlockType.CLASSIC_LEVEL -> (rule.value - clearedLevel(GameMode.CLASSIC)).coerceAtLeast(0)
            UnlockType.CHAOS_LEVEL -> (rule.value - clearedLevel(GameMode.CHAOS)).coerceAtLeast(0)
            UnlockType.TUTORIAL_CLEAR -> (rule.value - max(clearedLevel(GameMode.CLASSIC), clearedLevel(GameMode.CHAOS))).coerceAtLeast(0)
            UnlockType.BEST_STREAK -> (rule.value - bestStreak()).coerceAtLeast(0)
            UnlockType.SHARE_COUNT -> (rule.value - prefs.getInt(SHARE_COUNT_KEY, 0)).coerceAtLeast(0)
            UnlockType.HYPE_COST -> (rule.value - hypeBalance()).coerceAtLeast(0)
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
            UnlockType.BEST_STREAK -> "${t("STREAK")} ${skin.unlock.value}"
            UnlockType.SHARE_COUNT -> "${t("SHARE")} ${skin.unlock.value}"
            UnlockType.HYPE_COST -> "${formatHypeAmount(skin.unlock.value)} ${t("HYPE").uppercase()}"
        }
    }

    fun unlockLongLabel(skin: BallSkin): String {
        return when (skin.unlock.type) {
            UnlockType.PREMIUM -> "${premiumPriceLabel(skin)} - ${t("local price from Play Billing")}"
            UnlockType.HYPE_COST -> "${t("UNLOCK WITH").uppercase()} ${formatHypeAmount(skin.unlock.value)} ${t("HYPE").uppercase()} / ${t("HYPE BANK").uppercase()} ${formatHypeAmount(hypeBalance())}"
            else -> t(skin.unlock.label)
        }
    }

    fun premiumPriceLabel(skin: BallSkin): String {
        return premiumPricesBySkin[skin.id]
            ?: prefs.getString(premiumPriceKey(skin.id), null)
            ?: prefs.getString(PREMIUM_PRICE_KEY, "0.99 LOCAL")
            ?: "0.99 LOCAL"
    }

    fun modeMeta(mode: GameMode, streak: Int): String {
        val progress = modeProgress(mode)
        val modeStreak = modeStreak(mode, streak)
        return if (progress <= 1) {
            t("START LEVEL 01").uppercase()
        } else {
            "${t("LEVEL").uppercase()} ${progress.toString().padStart(2, '0')}   ${t("STREAK").uppercase()} $modeStreak"
        }
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

    fun highestLevelKey(mode: GameMode): String = "highest_level_${mode.name.lowercase()}"

    fun fairHighestLevelKey(mode: GameMode): String = "fair_highest_level_${mode.name.lowercase()}"

    fun bestModeStreakKey(mode: GameMode): String = "best_streak_${mode.name.lowercase()}"

    fun fairBestStreakKey(mode: GameMode): String = "fair_best_streak_${mode.name.lowercase()}"

    fun freeFailContinueKey(mode: GameMode): String = "free_fail_continue_${mode.name.lowercase()}"

    fun continueAdStreakKey(mode: GameMode): String = "continue_ad_streak_${mode.name.lowercase()}"

    fun levelAdKey(mode: GameMode): String = "level_ad_checkpoint_${mode.name.lowercase()}"

    fun breakStreak(mode: GameMode) {
        prefs.edit {
            putInt(streakKey(mode), 0)
            putInt("clear_streak", 0)
        }
    }
}
