package com.moonsolstudios.kavvoro.ui.controller

import android.content.SharedPreferences
import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.ui.LeaderboardBoard
import com.moonsolstudios.kavvoro.ui.LeaderboardBridge
import com.moonsolstudios.kavvoro.ui.LeaderboardScoreGuard

object LeaderboardTouchController {

    const val LEADERBOARD_BACK_INDEX = -2

    fun leaderboardScore(
        board: LeaderboardBoard,
        prefs: SharedPreferences,
        fairHighestLevelKey: (GameMode) -> String,
        fairBestStreakKey: (GameMode) -> String,
        modeHighestLevel: (GameMode) -> Int,
        modeBestStreak: (GameMode) -> Int
    ): Int = when (board) {
        LeaderboardBoard.CLASSIC_LEVEL -> prefs.getInt(fairHighestLevelKey(GameMode.CLASSIC), modeHighestLevel(GameMode.CLASSIC))
        LeaderboardBoard.CHAOS_LEVEL -> prefs.getInt(fairHighestLevelKey(GameMode.CHAOS), modeHighestLevel(GameMode.CHAOS))
        LeaderboardBoard.CLASSIC_STREAK -> prefs.getInt(fairBestStreakKey(GameMode.CLASSIC), modeBestStreak(GameMode.CLASSIC))
        LeaderboardBoard.CHAOS_STREAK -> prefs.getInt(fairBestStreakKey(GameMode.CHAOS), modeBestStreak(GameMode.CHAOS))
    }

    fun ensureFairLeaderboardSnapshot(
        prefs: SharedPreferences,
        fairHighestLevelKey: (GameMode) -> String,
        fairBestStreakKey: (GameMode) -> String,
        modeHighestLevel: (GameMode) -> Int,
        modeBestStreak: (GameMode) -> Int
    ) {
        val editor = prefs.edit()
        GameMode.entries.forEach { mode ->
            if (!prefs.contains(fairHighestLevelKey(mode))) editor.putInt(fairHighestLevelKey(mode), modeHighestLevel(mode))
            if (!prefs.contains(fairBestStreakKey(mode))) editor.putInt(fairBestStreakKey(mode), modeBestStreak(mode))
        }
        editor.apply()
    }

    fun submitScore(
        board: LeaderboardBoard,
        score: Int,
        leaderboardBridge: LeaderboardBridge,
        modeHighestLevel: (GameMode) -> Int,
        modeBestStreak: (GameMode) -> Int
    ) {
        val currentProgress = when (board) {
            LeaderboardBoard.CLASSIC_LEVEL -> modeHighestLevel(GameMode.CLASSIC)
            LeaderboardBoard.CHAOS_LEVEL -> modeHighestLevel(GameMode.CHAOS)
            LeaderboardBoard.CLASSIC_STREAK -> modeBestStreak(GameMode.CLASSIC)
            LeaderboardBoard.CHAOS_STREAK -> modeBestStreak(GameMode.CHAOS)
        }
        if (LeaderboardScoreGuard.isSubmitAllowed(board, score, completedRun = true, currentProgress = currentProgress)) {
            leaderboardBridge.submitScore(board, score.toLong())
        }
    }

    fun syncLeaderboards(
        prefs: SharedPreferences,
        leaderboardBridge: LeaderboardBridge,
        fairHighestLevelKey: (GameMode) -> String,
        fairBestStreakKey: (GameMode) -> String,
        modeHighestLevel: (GameMode) -> Int,
        modeBestStreak: (GameMode) -> Int
    ) {
        if (!leaderboardBridge.configured) return
        LeaderboardBoard.entries.forEach { board ->
            val score = leaderboardScore(board, prefs, fairHighestLevelKey, fairBestStreakKey, modeHighestLevel, modeBestStreak)
            submitScore(board, score, leaderboardBridge, modeHighestLevel, modeBestStreak)
        }
    }

    fun handleTouch(
        event: MotionEvent,
        layoutLeaderboards: () -> Unit,
        activeLeaderboardIndex: Int,
        setActiveIndex: (Int) -> Unit,
        leaderboardBackButton: RectF,
        leaderboardItemRects: List<RectF>,
        leaderboardBridge: LeaderboardBridge,
        performHaptic: (Int) -> Unit,
        t: (String) -> String,
        onBack: () -> Unit,
        setMessage: (String, Float) -> Unit,
        postAction: ((() -> Unit) -> Unit)
    ) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutLeaderboards()
                val index = when {
                    leaderboardBackButton.contains(event.x, event.y) -> LEADERBOARD_BACK_INDEX
                    else -> leaderboardItemRects.indexOfFirst { it.contains(event.x, event.y) }
                }
                setActiveIndex(index)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val released = activeLeaderboardIndex
                setActiveIndex(-1)
                if (released == LEADERBOARD_BACK_INDEX && leaderboardBackButton.contains(event.x, event.y)) {
                    onBack()
                    performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                val board = LeaderboardBoard.entries.getOrNull(released) ?: return
                if (leaderboardItemRects.getOrNull(released)?.contains(event.x, event.y) != true) return
                performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                if (!leaderboardBridge.configured) {
                    setMessage(t("GLOBAL SYNC OFFLINE"), 2.8f)
                    return
                }
                setMessage(t("OPENING GOOGLE PLAY"), 3f)
                leaderboardBridge.open(board) {
                    postAction {
                        setMessage(t("PLAY GAMES UNAVAILABLE"), 3.2f)
                    }
                }
            }
        }
    }
}
