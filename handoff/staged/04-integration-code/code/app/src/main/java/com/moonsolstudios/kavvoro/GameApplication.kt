package com.moonsolstudios.kavvoro

import android.app.Application
import com.google.android.gms.games.PlayGamesSdk
import com.moonsolstudios.kavvoro.playgames.PlayGamesConfig

class GameApplication : Application() {
    private var playGamesInitialized = false

    @Synchronized
    fun initializePlayGames() {
        if (!playGamesInitialized && PlayGamesConfig.isConfigured(this)) {
            PlayGamesSdk.initialize(this)
            playGamesInitialized = true
        }
    }
}
