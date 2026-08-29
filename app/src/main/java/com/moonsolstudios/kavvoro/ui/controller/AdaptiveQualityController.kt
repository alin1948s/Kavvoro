package com.moonsolstudios.kavvoro.ui.controller

import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.RenderProfile
import com.moonsolstudios.kavvoro.model.Screen

object AdaptiveQualityController {

    fun initialAdaptiveQuality(profile: RenderProfile): Float = when (profile) {
        RenderProfile.LOW -> 0.38f
        RenderProfile.BALANCED -> 0.72f
        RenderProfile.HIGH -> 0.88f
    }

    fun qualityFloor(profile: RenderProfile): Float = when (profile) {
        RenderProfile.LOW -> 0.3f
        RenderProfile.BALANCED -> 0.5f
        RenderProfile.HIGH -> 0.56f
    }

    fun qualityCeiling(profile: RenderProfile): Float = when (profile) {
        RenderProfile.LOW -> 0.5f
        RenderProfile.BALANCED -> 0.84f
        RenderProfile.HIGH -> 1f
    }

    fun targetFrameMillis(
        profile: RenderProfile,
        screen: Screen,
        state: GameState,
        screenTransitionTimer: Float,
        exportingShare: Boolean
    ): Long = when {
        exportingShare -> 33L
        screenTransitionTimer > 0f -> if (profile == RenderProfile.LOW) 42L else 22L
        screen == Screen.GAME && state == GameState.SIMULATING -> when (profile) {
            RenderProfile.LOW -> 33L
            RenderProfile.BALANCED -> 18L
            RenderProfile.HIGH -> 16L
        }
        screen == Screen.GAME -> if (profile == RenderProfile.LOW) 42L else 22L
        screen == Screen.MENU -> if (profile == RenderProfile.LOW) 50L else 33L
        else -> if (profile == RenderProfile.LOW) 50L else 33L
    }

    data class AdaptiveQualityResult(
        val adaptiveQuality: Float,
        val consecutiveSlowFrames: Int
    )

    fun adjustAdaptiveQuality(
        frameTimeMs: Float,
        profile: RenderProfile,
        currentAdaptiveQuality: Float,
        currentSlowFrames: Int,
        targetMillis: Long
    ): AdaptiveQualityResult {
        val target = targetMillis.toFloat()
        val slowFrames = if (frameTimeMs > target * 1.18f) currentSlowFrames + 1 else 0
        val floor = qualityFloor(profile)
        val ceiling = qualityCeiling(profile)
        val quality = when {
            frameTimeMs > target * 1.8f -> currentAdaptiveQuality - 0.16f
            frameTimeMs > target * 1.32f -> currentAdaptiveQuality - 0.085f
            slowFrames >= 3 -> currentAdaptiveQuality - 0.05f
            frameTimeMs < target * 0.58f -> currentAdaptiveQuality + if (profile == RenderProfile.LOW) 0.004f else 0.012f
            frameTimeMs < target * 0.78f -> currentAdaptiveQuality + if (profile == RenderProfile.HIGH) 0.006f else 0.002f
            else -> currentAdaptiveQuality
        }.coerceIn(floor, ceiling)
        return AdaptiveQualityResult(quality, slowFrames)
    }

    fun isPerformanceLite(
        settingsPerformanceMode: Boolean,
        profile: RenderProfile,
        adaptiveQuality: Float
    ): Boolean = settingsPerformanceMode || profile == RenderProfile.LOW || adaptiveQuality < 0.64f

    fun isRichEffects(profile: RenderProfile, adaptiveQuality: Float): Boolean =
        profile != RenderProfile.LOW && adaptiveQuality >= 0.8f

    fun isFullEffects(profile: RenderProfile, adaptiveQuality: Float): Boolean =
        profile == RenderProfile.HIGH && adaptiveQuality >= 0.94f

    fun detectRenderProfile(): RenderProfile {
        val deviceText = listOf(
            android.os.Build.MANUFACTURER,
            android.os.Build.MODEL,
            android.os.Build.DEVICE,
            android.os.Build.PRODUCT,
            android.os.Build.HARDWARE,
            android.os.Build.BOARD
        ).joinToString(" ").lowercase()
        val cores = Runtime.getRuntime().availableProcessors()
        val lowEndSignature = listOf(
            "moto g06",
            "xt2535",
            "mt6768",
            "mt6769",
            "mt6765",
            "helio g80",
            "helio g81",
            "helio g85",
            "mali-g52"
        ).any { it in deviceText }
        return when {
            lowEndSignature || cores <= 4 -> RenderProfile.LOW
            cores <= 6 -> RenderProfile.BALANCED
            else -> RenderProfile.HIGH
        }
    }
}
