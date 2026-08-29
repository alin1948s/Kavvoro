package com.moonsolstudios.kavvoro.model

import com.moonsolstudios.kavvoro.engine.BallPower

enum class SkinStyle {
    CLASSIC,
    CROWN,
    BLOP,
    ZAP,
    GLITCH,
    LOOP,
    STATIC,
    RIFT,
    BYTE,
    WOBBLE,
    PRISM,
    VOID,
    CHROME,
    PLASMA
}

enum class UnlockType {
    DEFAULT,
    TUTORIAL_CLEAR,
    CLASSIC_LEVEL,
    CHAOS_LEVEL,
    BEST_STREAK,
    SHARE_COUNT,
    HYPE_COST,
    PREMIUM
}

data class UnlockRule(
    val type: UnlockType,
    val value: Int,
    val label: String
)

data class BallSkin(
    val id: String,
    val name: String,
    val subtitle: String,
    val primary: Int,
    val secondary: Int,
    val lineColor: Int,
    val style: SkinStyle,
    val unlock: UnlockRule,
    val power: BallPower = BallPower.NONE
)

data class NextReward(
    val name: String,
    val label: String,
    val target: Int,
    val distance: Int,
    val progress: Float,
    val accent: Int
)
