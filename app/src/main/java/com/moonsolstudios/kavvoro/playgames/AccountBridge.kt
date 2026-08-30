package com.moonsolstudios.kavvoro.playgames

/**
 * Platform account state exposed to the game UI without coupling it to Google APIs.
 * Play Games authenticates silently at launch; [retry] is only a user-initiated fallback.
 */
enum class AccountState {
    CONNECTING,
    SIGNED_IN,
    SIGNED_OUT,
    UNAVAILABLE
}

interface AccountBridge {
    val configured: Boolean
    val state: AccountState
    /** Stable Play Games player ID used to select the local progress slot. */
    val profileId: String?

    fun start(onStateChanged: (AccountState) -> Unit)

    fun refresh()

    /** Explicit user action used after silent authentication is unavailable. */
    fun retry()

    companion object {
        val NONE = object : AccountBridge {
            override val configured: Boolean = false
            override val state: AccountState = AccountState.UNAVAILABLE
            override val profileId: String? = null
            override fun start(onStateChanged: (AccountState) -> Unit) = onStateChanged(state)
            override fun refresh() = Unit
            override fun retry() = Unit
        }
    }
}
