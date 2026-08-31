package com.moonsolstudios.kavvoro.playgames

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.moonsolstudios.kavvoro.R

class PlayGamesLeaderboardController(
    private val activity: Activity,
    private val accountController: PlayGamesAccountController = PlayGamesAccountController(activity)
) : LeaderboardBridge {
    override val configured: Boolean = PlayGamesConfig.isConfigured(activity)

    override fun open(board: LeaderboardBoard, onFailure: () -> Unit) {
        if (!configured) {
            onFailure()
            return
        }
        accountController.withAuthenticated(
            onAuthenticated = { openAuthenticated(board, onFailure) },
            onFailure = onFailure
        )
    }

    override fun submitScore(board: LeaderboardBoard, score: Long) {
        if (!configured || score <= 0L) return
        PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener(activity) { task ->
            if (task.isSuccessful && task.result.isAuthenticated) {
                // The v2 SDK exposes submitScore as a fire-and-forget call. Keep the
                // submission centralized here so every caller uses the same auth gate.
                PlayGames.getLeaderboardsClient(activity).submitScore(boardId(board), score)
            } else if (!task.isSuccessful) {
                Log.w(TAG, "Unable to verify Play Games authentication before score submission", task.exception)
            }
        }
    }

    private fun openAuthenticated(board: LeaderboardBoard, onFailure: () -> Unit) {
        PlayGames.getLeaderboardsClient(activity)
            .getLeaderboardIntent(boardId(board))
            .addOnSuccessListener(activity) { intent ->
                @Suppress("DEPRECATION")
                activity.startActivityForResult(intent, LEADERBOARD_REQUEST_CODE)
            }
            .addOnFailureListener(activity) { error ->
                Log.w(TAG, "Failed to open leaderboard $board", error)
                onFailure()
            }
    }

    private fun boardId(board: LeaderboardBoard): String {
        val resource = when (board) {
            LeaderboardBoard.CLASSIC_LEVEL -> R.string.leaderboard_classic_level_id
            LeaderboardBoard.CHAOS_LEVEL -> R.string.leaderboard_chaos_level_id
            LeaderboardBoard.CLASSIC_STREAK -> R.string.leaderboard_classic_streak_id
            LeaderboardBoard.CHAOS_STREAK -> R.string.leaderboard_chaos_streak_id
        }
        return activity.getString(resource)
    }

    companion object {
        private const val TAG = "KavvoroLeaderboard"
        private const val LEADERBOARD_REQUEST_CODE = 9004
    }
}

object PlayGamesConfig {
    fun isConfigured(context: Context): Boolean {
        val projectId = context.getString(R.string.game_services_project_id)
        val boardIds = listOf(
            R.string.leaderboard_classic_level_id,
            R.string.leaderboard_chaos_level_id,
            R.string.leaderboard_classic_streak_id,
            R.string.leaderboard_chaos_streak_id
        ).map(context::getString)
        return projectId != "0000000000" && boardIds.none { it.startsWith("REPLACE_") }
    }
}
