package com.moonsolstudios.kavvoro.audio

data class SelectionPreviewFadeStep(
    val elapsedMs: Long,
    val volume: Float,
)

internal object SelectionPreviewFade {
    const val DURATION_MS = 180L
    private const val STEP_COUNT = 6

    fun steps(startVolume: Float): List<SelectionPreviewFadeStep> {
        val safeVolume = startVolume.coerceIn(0f, 1f)
        return (0..STEP_COUNT).map { index ->
            val progress = index.toFloat() / STEP_COUNT
            val easedProgress = progress * progress * (3f - 2f * progress)
            SelectionPreviewFadeStep(
                elapsedMs = DURATION_MS * index / STEP_COUNT,
                volume = safeVolume * (1f - easedProgress),
            )
        }
    }
}
