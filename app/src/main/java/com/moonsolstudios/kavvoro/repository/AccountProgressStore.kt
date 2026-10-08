package com.moonsolstudios.kavvoro.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.model.GameMode
import java.security.MessageDigest

/**
 * Selects the local progress slot for the active Play Games player.
 *
 * Progress is intentionally stored locally per player ID. This keeps a signed-out
 * session usable while preventing one Google account from inheriting another
 * account's progress when the device changes account.
 */
class AccountProgressStore(context: Context) {
    private val appContext = context.applicationContext
    private val metadata = appContext.getSharedPreferences(METADATA_PREFS_NAME, Context.MODE_PRIVATE)
    private val legacyPrefs = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

    private var activeProfileId: String = GUEST_PROFILE_ID
    private var activePrefs: SharedPreferences = profilePreferences(GUEST_PROFILE_ID)

    init {
        migrateLegacyPreferences()
    }

    fun activePreferences(): SharedPreferences = activePrefs

    /**
     * Activates a player slot. Local/guest progress is never replaced by an empty
     * or older account slot. The first authenticated player inherits the current
     * guest save; a later sign-in only switches away when that slot already has
     * equal or richer gameplay progress. There is no Play Games cloud snapshot.
     */
    fun onSignedIn(playerId: String?): SharedPreferences {
        val normalized = playerId?.trim()?.takeIf { it.isNotEmpty() } ?: return activePrefs
        val target = profilePreferences(normalized)
        val uninitialized = !metadata.getBoolean(initializedKey(normalized), false)
        val firstAccount = metadata.getString(FIRST_ACCOUNT_ID_KEY, null) == null
        if (uninitialized && firstAccount) {
            copyPreferences(activePrefs, target)
            metadata.edit { putString(FIRST_ACCOUNT_ID_KEY, normalized) }
        } else if (shouldPreserveLocalProgress(activePrefs.all, target.all)) {
            copyPreferences(activePrefs, target)
        }
        if (uninitialized) {
            metadata.edit { putBoolean(initializedKey(normalized), true) }
        }
        return switchTo(normalized)
    }

    /**
     * Keeps the current local progress available while signed out. If the user
     * signs back in later, the account slot is restored instead of being merged
     * with another account's slot.
     */
    fun onSignedOut(): SharedPreferences {
        if (activeProfileId != GUEST_PROFILE_ID) {
            copyPreferences(activePrefs, profilePreferences(GUEST_PROFILE_ID))
        }
        return switchTo(GUEST_PROFILE_ID)
    }

    private fun switchTo(profileId: String): SharedPreferences {
        if (profileId == activeProfileId) return activePrefs
        val nextPrefs = profilePreferences(profileId)
        copySettings(activePrefs, nextPrefs)
        activeProfileId = profileId
        activePrefs = nextPrefs
        return activePrefs
    }

    private fun migrateLegacyPreferences() {
        if (metadata.getBoolean(LEGACY_MIGRATED_KEY, false)) return
        val guestPrefs = profilePreferences(GUEST_PROFILE_ID)
        if (guestPrefs.all.isEmpty() && legacyPrefs.all.isNotEmpty()) {
            copyPreferences(legacyPrefs, guestPrefs)
        }
        metadata.edit { putBoolean(LEGACY_MIGRATED_KEY, true) }
    }

    private fun profilePreferences(profileId: String): SharedPreferences =
        appContext.getSharedPreferences(profilePreferencesName(profileId), Context.MODE_PRIVATE)

    private fun copyPreferences(from: SharedPreferences, to: SharedPreferences) {
        to.edit {
            clear()
            from.all.forEach { (key, value) -> putValue(key, value) }
        }
    }

    private fun copySettings(from: SharedPreferences, to: SharedPreferences) {
        to.edit {
            SETTINGS_KEYS.forEach { key ->
                if (from.contains(key)) {
                    putValue(key, from.all[key])
                }
            }
        }
    }

    private fun SharedPreferences.Editor.putValue(key: String, value: Any?) {
        when (value) {
            null -> remove(key)
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value.filterIsInstance<String>().toSet())
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
        }
    }

    companion object {
        const val GUEST_PROFILE_ID = "guest"
        private const val LEGACY_PREFS_NAME = "kavvoro_progress"
        private const val PROFILE_PREFS_PREFIX = "kavvoro_progress_profile_"
        private const val METADATA_PREFS_NAME = "kavvoro_account_profiles"
        private const val LEGACY_MIGRATED_KEY = "legacy_progress_migrated"
        private const val FIRST_ACCOUNT_ID_KEY = "first_account_id"

        private val SETTINGS_KEYS = setOf(
            GameProgressRepository.SFX_MUTED_KEY,
            GameProgressRepository.MUSIC_MUTED_KEY,
            GameProgressRepository.SETTINGS_MASTER_VOLUME_KEY,
            GameProgressRepository.SETTINGS_MUSIC_VOLUME_KEY,
            GameProgressRepository.SETTINGS_SFX_VOLUME_KEY,
            GameProgressRepository.SETTINGS_HAPTIC_KEY,
            GameProgressRepository.SETTINGS_SCREEN_SHAKE_KEY,
            GameProgressRepository.SETTINGS_PERFORMANCE_KEY
        )

        internal fun gameplayScore(values: Map<String, *>): Int {
            fun intVal(key: String): Int = (values[key] as? Int) ?: 0
            val classic = maxOf(intVal(GameProgressRepository.progressKey(GameMode.CLASSIC)), intVal("unlocked_level")).coerceAtLeast(0).toLong()
            val chaos = intVal(GameProgressRepository.progressKey(GameMode.CHAOS)).coerceAtLeast(0).toLong()
            val highest = intVal("highest_level_classic").coerceAtLeast(0).toLong() +
                intVal("highest_level_chaos").coerceAtLeast(0).toLong()
            val streaks = intVal("streak_classic").coerceAtLeast(0).toLong() +
                intVal("streak_chaos").coerceAtLeast(0).toLong() +
                intVal(GameProgressRepository.BEST_STREAK_KEY).coerceAtLeast(0).toLong() +
                intVal("clear_streak").coerceAtLeast(0).toLong()
            val skins = values.keys.count { key ->
                key.startsWith("skin_unlocked_") || key.startsWith("skin_purchased_")
            }
            val experience = intVal(GameProgressRepository.PROFILE_XP_KEY).coerceAtLeast(0).toLong()
            return (classic + chaos + highest + streaks + skins * 10L + experience / 100L)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
        }

        internal fun shouldPreserveLocalProgress(
            local: Map<String, *>,
            account: Map<String, *>
        ): Boolean = gameplayScore(local) > gameplayScore(account)

        fun profilePreferencesName(profileId: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(profileId.toByteArray(Charsets.UTF_8))
            val encoded = digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
            return PROFILE_PREFS_PREFIX + encoded
        }

        private fun initializedKey(profileId: String): String = "initialized_${profilePreferencesName(profileId)}"
    }
}
