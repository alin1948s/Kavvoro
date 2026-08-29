package com.moonsolstudios.kavvoro.audio

import kotlin.math.ceil

internal object MusicTransition {
    const val DURATION_MS = 180L
    private const val STEP_MS = 30L
    private const val MAX_DURATION_MS = 240L

    fun steps(startVolume: Float, endVolume: Float, durationMs: Long): List<Float> {
        val start = startVolume.coerceIn(0f, 1f)
        val end = endVolume.coerceIn(0f, 1f)
        val duration = durationMs.coerceIn(0L, MAX_DURATION_MS)
        val stepCount = maxOf(1, ceil(duration / STEP_MS.toDouble()).toInt())

        return (0..stepCount).map { index ->
            val progress = index.toFloat() / stepCount
            val easedProgress = progress * progress * (3f - 2f * progress)
            start + (end - start) * easedProgress
        }
    }
}
