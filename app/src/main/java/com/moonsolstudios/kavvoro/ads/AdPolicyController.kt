package com.moonsolstudios.kavvoro.ads

import android.content.SharedPreferences
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.repository.GameProgressRepository

object AdPolicyController {

    const val FAILS_BEFORE_CONTINUE_AD = 4

    fun failContinueCount(prefs: SharedPreferences, mode: GameMode, levelIndex: Int): Int =
        prefs.getInt(GameProgressRepository.failContinueCountKey(mode, levelIndex), 0)

    fun recordFreeContinue(prefs: SharedPreferences, mode: GameMode, levelIndex: Int) {
        val used = failContinueCount(prefs, mode, levelIndex)
        prefs.edit { putInt(GameProgressRepository.failContinueCountKey(mode, levelIndex), used + 1) }
    }

    fun clearFailContinueCount(prefs: SharedPreferences, mode: GameMode, levelIndex: Int) {
        prefs.edit { putInt(GameProgressRepository.failContinueCountKey(mode, levelIndex), 0) }
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
        prefs.edit { putInt(levelAdKey(mode), levelNumber) }
    }
}
