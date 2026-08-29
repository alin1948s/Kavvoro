package com.moonsolstudios.kavvoro.model

import androidx.annotation.DrawableRes

enum class LayoutMode {
    COMPACT,
    MEDIUM,
    TABLET
}

data class HomeStats(
    val bestStreak: Int = 0,
    val hype: Int = 0,
    val level: Int = 1,
    val levelProgress: Float = 0f,
    val dailyRiftReady: Boolean = true
)

data class Brainball(
    val id: String,
    val name: String,
    @param:DrawableRes val drawableRes: Int,
    val homeScale: Float = 1f,
    val homeOffsetX: Float = 0f,
    val homeOffsetY: Float = 0f
)
