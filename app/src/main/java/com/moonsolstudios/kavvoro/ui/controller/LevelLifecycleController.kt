package com.moonsolstudios.kavvoro.ui.controller

import android.content.SharedPreferences
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.ui.LeaderboardBoard
import kotlin.math.max

object LevelLifecycleController {

    const val TUTORIAL_LAST_LEVEL = 10

    fun isTutorialLevel(levelNumber: Int): Boolean = levelNumber <= TUTORIAL_LAST_LEVEL

    fun createLevel(
        gameMode: GameMode,
        levelIndex: Int,
        stageHeight: Float,
        viewWidth: Int,
        viewHeight: Int,
        scale: Float,
        hasHeaderRibbon: Boolean,
        dp: Float
    ): LevelSpec {
        val spec = when (gameMode) {
            GameMode.CLASSIC -> LevelDirector.createClassic(levelIndex, stageHeight)
            GameMode.CHAOS -> LevelDirector.createChaos(levelIndex, stageHeight, LevelDirector.dailySeed() * 31L + 777L)
        }
        return withHudSafeStart(spec, viewWidth, viewHeight, scale, hasHeaderRibbon, stageHeight, dp)
    }

    fun withHudSafeStart(
        spec: LevelSpec,
        viewWidth: Int,
        viewHeight: Int,
        scale: Float,
        hasHeaderRibbon: Boolean,
        stageHeight: Float,
        dp: Float
    ): LevelSpec {
        if (viewWidth <= 1 || viewHeight <= 1 || scale <= 1.1f) return spec
        val compactHud = viewWidth < 520f * dp
        val reservedHudBottom = (if (compactHud) {
            if (hasHeaderRibbon) 132f else 104f
        } else {
            156f
        }) * dp
        val safeMargin = (if (compactHud) 56f else 64f) * dp
        val safeStartY = ((reservedHudBottom + safeMargin) / scale)
            .coerceIn(3.35f, stageHeight - 3.0f)
        if (spec.start.y >= safeStartY) return spec
        return spec.copy(start = spec.start.copy(y = safeStartY))
    }

    fun saveBest(
        score: RunScore,
        gameMode: GameMode,
        streak: Int,
        lastHypeScore: Int,
        isSkinPowerNone: Boolean,
        prefs: SharedPreferences,
        bestStreak: Int,
        modeProgress: Int,
        modeBestStreak: Int,
        modeHighestLevel: Int,
        hypeBalance: Int,
        fairHighestLevelKey: String,
        fairBestStreakKey: String,
        progressKey: String,
        streakKey: String,
        highestLevelKey: String,
        bestModeStreakKey: String,
        bestStreakKey: String,
        hypeBankKey: String,
        bestKey: String,
        rankValue: (String) -> Int = ::rankValue,
        submitLeaderboardScore: (LeaderboardBoard, Int) -> Unit
    ) {
        val current = prefs.getString(bestKey, null)
        if (current == null || rankValue(score.rank) < rankValue(current)) {
            prefs.edit().putString(bestKey, score.rank).apply()
        }
        val nextLevel = max(modeProgress, score.level + 1)
        val nextBestStreak = max(modeBestStreak, streak)
        val newHypeBalance = (hypeBalance.toLong() + lastHypeScore.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        prefs.edit()
            .putInt(progressKey, nextLevel)
            .putInt(streakKey, streak)
            .putInt(highestLevelKey, max(modeHighestLevel, nextLevel))
            .putInt(bestModeStreakKey, nextBestStreak)
            .putInt(bestStreakKey, max(bestStreak, streak))
            .putInt("clear_streak", streak)
            .putInt("last_hype", lastHypeScore)
            .putInt(hypeBankKey, newHypeBalance)
            .apply()

        if (isSkinPowerNone) {
            val levelBoard = if (gameMode == GameMode.CLASSIC) LeaderboardBoard.CLASSIC_LEVEL else LeaderboardBoard.CHAOS_LEVEL
            val streakBoard = if (gameMode == GameMode.CLASSIC) LeaderboardBoard.CLASSIC_STREAK else LeaderboardBoard.CHAOS_STREAK
            val fairLevel = max(prefs.getInt(fairHighestLevelKey, modeHighestLevel), nextLevel)
            val fairStreak = max(prefs.getInt(fairBestStreakKey, modeBestStreak), nextBestStreak)
            prefs.edit()
                .putInt(fairHighestLevelKey, fairLevel)
                .putInt(fairBestStreakKey, fairStreak)
                .apply()
            submitLeaderboardScore(levelBoard, fairLevel)
            submitLeaderboardScore(streakBoard, fairStreak)
        }
    }

    fun riftBreakReason(
        riftEnergy: Float,
        maxChain: Int,
        runSeconds: Float,
        timeLimitSeconds: Float,
        gameMode: GameMode,
        t: (String) -> String
    ): String = when {
        riftEnergy <= 0.18f -> t("LOW ENERGY FINISH").uppercase()
        maxChain >= 5 -> t("CHAIN SPIKE").uppercase()
        runSeconds >= timeLimitSeconds * 0.8f -> t("LAST SECOND CLUTCH").uppercase()
        gameMode == GameMode.CHAOS -> t("CHAOS CONTROL").uppercase()
        else -> t("CLEAN RIFT SNAP").uppercase()
    }

    fun rankValue(rank: String): Int = when (rank) {
        "S+" -> 0
        "S" -> 0
        "A" -> 1
        "B" -> 2
        "C" -> 3
        else -> 9
    }
}
