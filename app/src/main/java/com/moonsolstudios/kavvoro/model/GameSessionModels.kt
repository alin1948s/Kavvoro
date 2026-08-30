package com.moonsolstudios.kavvoro.model

enum class GameMode(val label: String) {
    CLASSIC("CLASSIC"),
    CHAOS("CHAOS");

    fun menuTitle(t: (String) -> String = { it }): String = when (this) {
        CLASSIC -> t("CLASSIC").uppercase()
        CHAOS -> t("CHAOS").uppercase()
    }
}

enum class GameState {
    READY,
    SIMULATING,
    WON,
    LOST
}
