package com.moonsolstudios.kavvoro.privacy

import android.content.Context
import androidx.core.content.edit

enum class AgeGroup {
    CHILD,
    TEEN,
    ADULT
}

/** Persists only the resolved age category, never a birth date. */
object AgeProfileStore {
    private const val PREFS_NAME = "privacy_profile"
    private const val AGE_GROUP_KEY = "age_group"

    fun read(context: Context): AgeGroup? {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(AGE_GROUP_KEY, null)
            ?: return null
        return AgeGroup.entries.firstOrNull { it.name == saved }
    }

    fun save(context: Context, ageGroup: AgeGroup) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(AGE_GROUP_KEY, ageGroup.name) }
    }
}
