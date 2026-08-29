package com.moonsolstudios.kavvoro.engine

import kotlin.math.max

class ReplayRecorder {
    private val frames = ArrayDeque<PhysicsFrame>()
    private val maxFrames = 720

    fun clear() {
        frames.clear()
    }

    fun add(frame: PhysicsFrame) {
        frames.addLast(frame)
        while (frames.size > maxFrames) {
            frames.removeFirst()
        }
    }

    fun snapshot(): List<PhysicsFrame> = frames.toList()

    fun buildScore(level: LevelSpec, inkUsed: Float, seconds: Float): RunScore {
        val inkRatio = if (level.inkLimit <= 0f) 1f else inkUsed / level.inkLimit
        val rank = when {
            seconds <= 4.2f && inkRatio <= 0.55f -> "S"
            seconds <= 5.5f && inkRatio <= 0.72f -> "A"
            seconds <= 7.0f && inkRatio <= 0.9f -> "B"
            else -> "C"
        }
        return RunScore(
            level = level.index,
            inkUsed = max(0f, inkUsed),
            seconds = max(0f, seconds),
            rank = rank
        )
    }
}
