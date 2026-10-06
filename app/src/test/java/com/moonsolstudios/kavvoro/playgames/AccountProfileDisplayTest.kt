package com.moonsolstudios.kavvoro.playgames

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountProfileDisplayTest {

    @Test
    fun unsignedStatesShowGuestUserNotGooglePlayStatus() {
        val guest = "User"
        assertEquals(guest, AccountProfileDisplay.headerName(AccountState.SIGNED_OUT, null, guest))
        assertEquals(guest, AccountProfileDisplay.headerName(AccountState.UNAVAILABLE, "Ignored", guest))
        assertEquals(guest, AccountProfileDisplay.headerName(AccountState.CONNECTING, "Ignored", guest))
        assertEquals(guest, AccountProfileDisplay.headerName(AccountState.SIGNED_OUT, "GOOGLE PLAY NECONECTAT", guest))
    }

    @Test
    fun signedInUsesPlayGamesDisplayName() {
        assertEquals(
            "PlayerAlex",
            AccountProfileDisplay.headerName(AccountState.SIGNED_IN, "PlayerAlex", "User")
        )
        assertEquals(
            "Long Google Play Name",
            AccountProfileDisplay.headerName(AccountState.SIGNED_IN, "  Long Google Play Name  ", "User")
        )
    }

    @Test
    fun signedInWithoutNameFallsBackToUser() {
        assertEquals("User", AccountProfileDisplay.headerName(AccountState.SIGNED_IN, null, "User"))
        assertEquals("User", AccountProfileDisplay.headerName(AccountState.SIGNED_IN, "   ", "User"))
    }
}
