package com.moonsolstudios.kavvoro.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountProgressStoreTest {
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
