package com.moonsolstudios.kavvoro.playgames

enum class LeaderboardBoard {
    CLASSIC_LEVEL,
    CHAOS_LEVEL,
    CLASSIC_STREAK,
    CHAOS_STREAK
}

interface LeaderboardBridge {
    val configured: Boolean

    fun open(board: LeaderboardBoard, onFailure: () -> Unit)

    fun submitScore(board: LeaderboardBoard, score: Long)

    companion object {
        val NONE = object : LeaderboardBridge {
            override val configured = false

            override fun open(board: LeaderboardBoard, onFailure: () -> Unit) {
                onFailure()
            }

            override fun submitScore(board: LeaderboardBoard, score: Long) = Unit
        }
    }
}
