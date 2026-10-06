package com.moonsolstudios.kavvoro.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountProgressStoreTest {
    @Test
    fun emptyAccountSlotDoesNotBeatGuestProgress() {
        val guest = mapOf("classic_level" to 7, "chaos_level" to 3)
        val emptyAccount = emptyMap<String, Any>()
        assertTrue(AccountProgressStore.shouldPreserveLocalProgress(guest, emptyAccount))
        assertEquals(10, AccountProgressStore.gameplayScore(guest))
        assertEquals(0, AccountProgressStore.gameplayScore(emptyAccount))
    }

    @Test
    fun richerAccountSlotIsKept() {
        val guest = mapOf("classic_level" to 4)
        val account = mapOf("classic_level" to 12, "skin_unlocked_neon" to true)
        assertTrue(AccountProgressStore.gameplayScore(account) > AccountProgressStore.gameplayScore(guest))
        assertTrue(!AccountProgressStore.shouldPreserveLocalProgress(guest, account))
    }

    @Test
    fun settingsOnlyLocalSaveIsNotTreatedAsRicherProgress() {
        val guestSettings = mapOf("settings_master_volume" to 80, "sfx_muted" to true)
        val emptyAccount = emptyMap<String, Any>()
        assertEquals(0, AccountProgressStore.gameplayScore(guestSettings))
        assertTrue(!AccountProgressStore.shouldPreserveLocalProgress(guestSettings, emptyAccount))
    }

    @Test
    fun profileStorageName_isStableAndOpaque() {
        val first = AccountProgressStore.profilePreferencesName("player-a")
        val second = AccountProgressStore.profilePreferencesName("player-a")

        assertEquals(first, second)
        assertTrue(first.startsWith("kavvoro_progress_profile_"))
        assertTrue(first.removePrefix("kavvoro_progress_profile_").matches(Regex("[0-9a-f]{64}")))
        assertTrue("player-a" !in first)
    }

    @Test
    fun profileStorageName_separatesPlayers() {
        assertNotEquals(
            AccountProgressStore.profilePreferencesName("player-a"),
            AccountProgressStore.profilePreferencesName("player-b")
        )
    }
}
