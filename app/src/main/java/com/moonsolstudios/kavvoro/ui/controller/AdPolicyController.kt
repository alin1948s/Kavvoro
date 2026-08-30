package com.moonsolstudios.kavvoro.ui.controller

import android.content.SharedPreferences
import com.moonsolstudios.kavvoro.model.GameMode

object AdPolicyController {

    const val FAILS_BEFORE_CONTINUE_AD = 4

    fun failContinueCountKey(mode: GameMode, levelIndex: Int): String =
        "fail_continue_${mode.name.lowercase()}_$levelIndex"

    fun failContinueCount(prefs: SharedPreferences, mode: GameMode, levelIndex: Int): Int =
        prefs.getInt(failContinueCountKey(mode, levelIndex), 0)

    fun recordFreeContinue(prefs: SharedPreferences, mode: GameMode, levelIndex: Int) {
        val used = failContinueCount(prefs, mode, levelIndex)
        prefs.edit().putInt(failContinueCountKey(mode, levelIndex), used + 1).apply()
    }

    fun clearFailContinueCount(prefs: SharedPreferences, mode: GameMode, levelIndex: Int) {
        prefs.edit().putInt(failContinueCountKey(mode, levelIndex), 0).apply()
    }

    fun shouldShowFailContinueAd(prefs: SharedPreferences, mode: GameMode, levelIndex: Int): Boolean =
        failContinueCount(prefs, mode, levelIndex) + 1 >= FAILS_BEFORE_CONTINUE_AD

    fun continueRequiresAd(
        prefs: SharedPreferences,
        mode: GameMode,
        levelIndex: Int,
        shouldShowLevelAd: (GameMode, Int) -> Boolean
    ): Boolean =
        shouldShowLevelAd(mode, levelIndex) || shouldShowFailContinueAd(prefs, mode, levelIndex)

    fun continueAdReason(
        prefs: SharedPreferences,
        mode: GameMode,
        levelIndex: Int,
        shouldShowLevelAd: (GameMode, Int) -> Boolean,
        t: (String) -> String
    ): String = if (shouldShowLevelAd(mode, levelIndex)) {
        "${t("LEVEL").uppercase()} $levelIndex ${t("CHECKPOINT").uppercase()}"
    } else {
        "${t("FAILED").uppercase()} ${FAILS_BEFORE_CONTINUE_AD}X"
    }

    fun shouldShowLevelAd(
        prefs: SharedPreferences,
        mode: GameMode,
        levelNumber: Int,
        isTutorialLevel: (Int) -> Boolean,
        tutorialLastLevel: Int,
        adLevelInterval: Int,
        levelAdKey: (GameMode) -> String
    ): Boolean {
        if (isTutorialLevel(levelNumber)) return false
        val firstAdLevel = tutorialLastLevel + adLevelInterval + 1
        if (levelNumber < firstAdLevel) return false
        if ((levelNumber - firstAdLevel) % adLevelInterval != 0) return false
        return prefs.getInt(levelAdKey(mode), 0) != levelNumber
    }

    fun markLevelAdShown(
        prefs: SharedPreferences,
        mode: GameMode,
        levelNumber: Int,
        levelAdKey: (GameMode) -> String
    ) {
        prefs.edit().putInt(levelAdKey(mode), levelNumber).apply()
    }
}
