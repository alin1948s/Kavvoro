package com.moonsolstudios.kavvoro.i18n

import android.content.Context

object HomeCopy {
    val requiredKeys: Set<String> = setOf(
        "PLAY NOW",
        "PLAY",
        "JUMP INTO A BRIGHTER UNIVERSE",
        "SKINS",
        "CUSTOMIZE YOUR STYLE",
        "MISSIONS",
        "COMPLETE TASKS EARN REWARDS",
        "LEADERBOARD",
        "SEE TOP PLAYERS AROUND THE WORLD",
        "STREAK",
        "LEVEL",
        "SMALL MINDS BIG WORLDS",
        "DIFFERENT WORLDS. SAME CHAOS.",
        "LEVELS",
        "MULTIVERSE PORTAL",
        "DIVISION 1",
        "OPEN",
        "TOP",
        "TOP PLAYERS"
    )

    fun ctaPlay(context: Context): String = KavvoroI18n.t(context, "PLAY NOW")
    fun ctaPlay(language: KavvoroLanguage): String = KavvoroI18n.t(language, "PLAY NOW")

    fun ctaPlayShort(context: Context): String = KavvoroI18n.t(context, "PLAY")
    fun ctaPlayShort(language: KavvoroLanguage): String = KavvoroI18n.t(language, "PLAY")

    fun ctaSubtitle(context: Context): String = KavvoroI18n.t(context, "JUMP INTO A BRIGHTER UNIVERSE")
    fun ctaSubtitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "JUMP INTO A BRIGHTER UNIVERSE")

    fun skinsTitle(context: Context): String = KavvoroI18n.t(context, "SKINS")
    fun skinsTitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "SKINS")

    fun skinsSubtitle(context: Context): String = KavvoroI18n.t(context, "CUSTOMIZE YOUR STYLE")
    fun skinsSubtitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "CUSTOMIZE YOUR STYLE")

    fun missionsTitle(context: Context): String = KavvoroI18n.t(context, "MISSIONS")
    fun missionsTitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "MISSIONS")

    fun missionsSubtitle(context: Context): String = KavvoroI18n.t(context, "COMPLETE TASKS EARN REWARDS")
    fun missionsSubtitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "COMPLETE TASKS EARN REWARDS")

    fun leaderboardTitle(context: Context): String = KavvoroI18n.t(context, "LEADERBOARD")
    fun leaderboardTitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "LEADERBOARD")

    fun leaderboardSubtitle(context: Context): String = KavvoroI18n.t(context, "SEE TOP PLAYERS AROUND THE WORLD")
    fun leaderboardSubtitle(language: KavvoroLanguage): String = KavvoroI18n.t(language, "SEE TOP PLAYERS AROUND THE WORLD")

    fun streak(context: Context): String = KavvoroI18n.t(context, "STREAK")
    fun streak(language: KavvoroLanguage): String = KavvoroI18n.t(language, "STREAK")

    fun level(context: Context): String = KavvoroI18n.t(context, "LEVEL")
    fun level(language: KavvoroLanguage): String = KavvoroI18n.t(language, "LEVEL")

    fun hype(context: Context): String = KavvoroI18n.t(context, "HYPE")
    fun hype(language: KavvoroLanguage): String = KavvoroI18n.t(language, "HYPE")

    fun brandMotto(context: Context): String = KavvoroI18n.t(context, "SMALL MINDS BIG WORLDS")
    fun brandMotto(language: KavvoroLanguage): String = KavvoroI18n.t(language, "SMALL MINDS BIG WORLDS")

    fun landscapeWatermark(context: Context): String =
        KavvoroI18n.t(context, "DIFFERENT WORLDS. SAME CHAOS.")

    fun landscapeWatermark(language: KavvoroLanguage): String =
        KavvoroI18n.t(language, "DIFFERENT WORLDS. SAME CHAOS.")
}
