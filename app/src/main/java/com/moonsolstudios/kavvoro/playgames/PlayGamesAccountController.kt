package com.moonsolstudios.kavvoro.playgames

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.games.PlayGames
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n

/**
 * Owns the Play Games Services v2 authentication lifecycle.
 *
 * The Play Games v2 init provider performs silent platform authentication during process start.
 * We only observe that result after the first frame; an interactive sign-in is reserved for an
 * explicit retry from Account or Leaderboards so the launch path never waits on Google Play.
 */
class PlayGamesAccountController(
    private val activity: Activity
) : AccountBridge {
    override val configured: Boolean = PlayGamesConfig.isConfigured(activity)

    @Volatile
    private var currentState: AccountState = if (configured) {
        AccountState.CONNECTING
    } else {
        AccountState.UNAVAILABLE
    }

    private var stateListener: ((AccountState) -> Unit)? = null

    override val state: AccountState
        get() = currentState

    override fun start(onStateChanged: (AccountState) -> Unit) {
        stateListener = onStateChanged
        refresh()
    }

    override fun refresh() {
        if (!configured) {
            publish(AccountState.UNAVAILABLE)
            return
        }
        publish(AccountState.CONNECTING)
        PlayGames.getGamesSignInClient(activity).isAuthenticated
            .addOnCompleteListener(activity) { task ->
                publish(
                    if (task.isSuccessful && task.result.isAuthenticated) {
                        AccountState.SIGNED_IN
                    } else {
                        AccountState.SIGNED_OUT
                    }
                )
            }
    }

    override fun retry() {
        if (!configured) {
            publish(AccountState.UNAVAILABLE)
            return
        }
        publish(AccountState.CONNECTING)
        PlayGames.getGamesSignInClient(activity).signIn()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result.isAuthenticated) {
                    publish(AccountState.SIGNED_IN)
                } else {
                    publish(AccountState.SIGNED_OUT)
                    Toast.makeText(
                        activity,
                        KavvoroI18n.t(activity, "PLAY GAMES UNAVAILABLE"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Opens a leaderboard after checking the current platform session. If silent auth is no
     * longer valid, the call uses the same explicit sign-in fallback as Account.
     */
    fun withAuthenticated(onAuthenticated: () -> Unit, onFailure: () -> Unit) {
        if (!configured) {
            publish(AccountState.UNAVAILABLE)
            onFailure()
            return
        }
        PlayGames.getGamesSignInClient(activity).isAuthenticated
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful && task.result.isAuthenticated) {
                    publish(AccountState.SIGNED_IN)
                    onAuthenticated()
                } else {
                    publish(AccountState.CONNECTING)
                    PlayGames.getGamesSignInClient(activity).signIn()
                        .addOnCompleteListener(activity) { signInTask ->
                            if (signInTask.isSuccessful && signInTask.result.isAuthenticated) {
                                publish(AccountState.SIGNED_IN)
                                onAuthenticated()
                            } else {
                                publish(AccountState.SIGNED_OUT)
                                onFailure()
                            }
                        }
                }
            }
    }

    private fun publish(next: AccountState) {
        currentState = next
        stateListener?.invoke(next)
    }
}
