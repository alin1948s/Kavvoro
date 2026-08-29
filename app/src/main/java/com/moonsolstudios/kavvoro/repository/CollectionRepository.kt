package com.moonsolstudios.kavvoro.repository

import android.content.Context
import android.content.SharedPreferences

class CollectionRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedBrainballId(): String {
        return prefs.getString(KEY_SELECTED_BRAINBALL_ID, null)
            ?: prefs.getString(KEY_SELECTED_BALL_SKIN, DEFAULT_ID)
            ?: DEFAULT_ID
    }

    fun setSelectedBrainballId(id: String) {
        prefs.edit()
            .putString(KEY_SELECTED_BRAINBALL_ID, id)
            .putString(KEY_SELECTED_BALL_SKIN, id)
            .apply()
    }

    companion object {
        const val PREFS_NAME = "kavvoro_progress"
        const val KEY_SELECTED_BRAINBALL_ID = "selected_brainball_id"
        const val KEY_SELECTED_BALL_SKIN = "selected_ball_skin"
        const val DEFAULT_ID = "kavvoro"
    }
}
