package com.moonsolstudios.kavvoro.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.moonsolstudios.kavvoro.R
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.roundToInt

enum class SoundEvent {
    UI_TAP,
    LOCKED,
    RIFT_ON,
    RIFT_OFF,
    GOAL,
    FAIL,
    POWER,
    UNLOCK,
    CHAIN
}

enum class MusicTrack {
    MENU,
    TUTORIAL,
    CLASSIC,
    CHAOS
}

class KavvoroSoundEngine(context: Context) {
    private val appContext = context.applicationContext
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val loadedSounds = ConcurrentHashMap.newKeySet<Int>()
    private val rawResourceToSampleId = ConcurrentHashMap<Int, Int>()
    @Volatile
    private var released = false
    @Volatile
    private var sfxMuted = false
    @Volatile
    private var musicMuted = false
    @Volatile
    private var paused = false
    @Volatile
    private var masterVolume = 1f
    @Volatile
    private var musicVolume = 1f
    @Volatile
    private var sfxVolume = 1f
    private var languageCode = "en"
    private var activeSelectionStream = 0
    private var pendingSelectionSampleId = 0
    private val selectionFadeHandler = Handler(Looper.getMainLooper())
    private val selectionFadeRunnables = mutableMapOf<Int, Runnable>()
    private val fadingSelectionStreams = mutableSetOf<Int>()
    private val musicTransitionHandler = Handler(Looper.getMainLooper())
    private val musicExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "kavvoro-music-worker")
    }
    private var currentMusic: MusicTrack? = null
    private var musicPlayer: MediaPlayer? = null
    private var fadingMusicPlayer: MediaPlayer? = null
    private var musicTransitionRunnable: Runnable? = null

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds += sampleId
                synchronized(this@KavvoroSoundEngine) {
                    if (!released && !sfxMuted && pendingSelectionSampleId == sampleId) {
                        pendingSelectionSampleId = 0
                        fadeOutSelectionStream(activeSelectionStream)
                        activeSelectionStream = play(sampleId, volume = SELECTION_PREVIEW_VOLUME, rate = 1f, priority = 5)
                    }
                }
            }
        }
    }

    @Synchronized
    fun playSelection(index: Int) {
        if (released || sfxMuted) return
        val resId = getSelectionResourceId(languageCode, index)
        if (resId == 0) return
        val sampleId = getOrLoadSample(resId)
        fadeOutSelectionStream(activeSelectionStream)
        if (sampleId in loadedSounds) {
            pendingSelectionSampleId = 0
            activeSelectionStream = play(sampleId, volume = SELECTION_PREVIEW_VOLUME, rate = 1f, priority = 5)
        } else {
            pendingSelectionSampleId = sampleId
        }
    }

    @Synchronized
    fun setLanguageCode(code: String) {
        if (languageCode == code) return
        languageCode = code
        fadeOutSelectionStream(activeSelectionStream)
    }

    fun playBounce(brainballIndex: Int, impactStrength: Float) {
        if (sfxMuted) return
        if (impactStrength < 0.7f) return
        val normalized = ((impactStrength - 0.7f) / 6.2f).coerceIn(0f, 1f)
        val tier = (normalized * (bounceResources.size - 1)).roundToInt()
        val resId = bounceResources.getOrNull(tier) ?: return
        val sound = getOrLoadSample(resId)
        val characterPitch = 0.86f + ((brainballIndex * 37) % 29) / 100f
        val impactPitch = 0.92f + normalized * 0.16f
        play(
            sound,
            volume = 0.14f + normalized * 0.48f,
            rate = (characterPitch * impactPitch).coerceIn(0.72f, 1.34f),
            priority = 1
        )
    }

    fun playEvent(event: SoundEvent, brainballIndex: Int = 0, intensity: Float = 1f) {
        if (sfxMuted) return
        val resId = eventResources[event] ?: return
        val sound = getOrLoadSample(resId)
        val characterPitch = 0.94f + ((brainballIndex * 19) % 15) / 100f
        val rate = when (event) {
            SoundEvent.RIFT_ON, SoundEvent.RIFT_OFF, SoundEvent.POWER -> characterPitch
            SoundEvent.CHAIN -> (0.92f + intensity.coerceIn(0f, 1f) * 0.24f)
            else -> 1f
        }
        val volume = when (event) {
            SoundEvent.UI_TAP -> 0.3f
            SoundEvent.CHAIN -> 0.24f + intensity.coerceIn(0f, 1f) * 0.2f
            SoundEvent.RIFT_OFF -> 0.32f
            SoundEvent.GOAL, SoundEvent.UNLOCK -> 0.68f
            SoundEvent.FAIL -> 0.62f
            else -> 0.64f
        }
        play(sound, volume, rate, if (event == SoundEvent.GOAL || event == SoundEvent.UNLOCK) 4 else 2)
    }

    @Synchronized
    fun playMusic(track: MusicTrack) {
        if (released) return
        if (currentMusic == track) {
            if (musicPlayer == null && !paused && !musicMuted) {
                rebuildMusicPlayer(track)
            }
            return
        }
        currentMusic = track
        if (paused || musicMuted) {
            releaseMusicPlayer()
        } else {
            rebuildMusicPlayer()
        }
    }

    @Synchronized
    fun setSfxMuted(muted: Boolean) {
        sfxMuted = muted
        if (muted) stopSelectionPreviewsImmediately()
    }

    @Synchronized
    fun setMusicMuted(muted: Boolean) {
        if (musicMuted == muted) return
        musicMuted = muted
        if (muted) {
            pauseMusic()
        } else {
            startMusicIfAllowed()
        }
    }

    @Synchronized
    fun setVolumes(masterPercent: Int, musicPercent: Int, sfxPercent: Int) {
        masterVolume = masterPercent.coerceIn(0, 100) / 100f
        musicVolume = musicPercent.coerceIn(0, 100) / 100f
        sfxVolume = sfxPercent.coerceIn(0, 100) / 100f
        val output = musicOutputVolume()
        try {
            musicPlayer?.setVolume(output, output)
            fadingMusicPlayer?.setVolume(output, output)
        } catch (_: IllegalStateException) {
            // The platform released a player while the setting was changing.
        }
    }

    @Synchronized
    fun setPaused(isPaused: Boolean) {
        if (paused == isPaused) return
        paused = isPaused
        if (paused) {
            pauseMusic()
        } else {
            startMusicIfAllowed()
        }
    }

    @Synchronized
    fun release() {
        if (released) return
        released = true
        pendingSelectionSampleId = 0
        stopSelectionPreviewsImmediately()
        releaseMusicPlayer()
        soundPool.release()
        loadedSounds.clear()
        rawResourceToSampleId.clear()
        musicExecutor.shutdown()
    }

    private fun play(sound: Int, volume: Float, rate: Float, priority: Int): Int {
        if (released || sfxMuted || sound !in loadedSounds) return 0
        val output = volume * masterVolume * sfxVolume
        return soundPool.play(sound, output, output, priority, 0, rate.coerceIn(0.5f, 2f))
    }

    private fun fadeOutSelectionStream(streamId: Int) {
        if (streamId == 0) return
        selectionFadeRunnables[streamId]?.let(selectionFadeHandler::removeCallbacks)
        val steps = SelectionPreviewFade.steps(SELECTION_PREVIEW_VOLUME * masterVolume * sfxVolume)
        var stepIndex = 0
        fadingSelectionStreams += streamId
        val fade = object : Runnable {
            override fun run() {
                if (released) {
                    selectionFadeRunnables.remove(streamId)
                    fadingSelectionStreams.remove(streamId)
                    return
                }
                val step = steps[stepIndex]
                soundPool.setVolume(streamId, step.volume, step.volume)
                if (stepIndex == steps.lastIndex) {
                    soundPool.stop(streamId)
                    selectionFadeRunnables.remove(streamId)
                    fadingSelectionStreams.remove(streamId)
                    return
                }
                stepIndex += 1
                selectionFadeHandler.postDelayed(this, steps[stepIndex].elapsedMs - step.elapsedMs)
            }
        }
        selectionFadeRunnables[streamId] = fade
        selectionFadeHandler.post(fade)
    }

    private fun stopSelectionPreviewsImmediately() {
        selectionFadeHandler.removeCallbacksAndMessages(null)
        if (activeSelectionStream != 0) soundPool.stop(activeSelectionStream)
        fadingSelectionStreams.forEach(soundPool::stop)
        activeSelectionStream = 0
        selectionFadeRunnables.clear()
        fadingSelectionStreams.clear()
    }

    // Localized clips follow a stable generated naming scheme; keep.xml preserves them for shrinking.
    @SuppressLint("DiscouragedApi")
    private fun getSelectionResourceId(lang: String, index: Int): Int {
        if (lang != "en" && lang in supportedAudioLanguageCodes) {
            val resId = appContext.resources.getIdentifier(
                "brainball_select_${lang}_${index.toString().padStart(2, '0')}",
                "raw",
                appContext.packageName
            )
            if (resId != 0) return resId
        }
        return selectionResources.getOrNull(index) ?: 0
    }

    private fun getOrLoadSample(resId: Int): Int {
        if (resId == 0) return 0
        rawResourceToSampleId[resId]?.let { return it }
        val sampleId = soundPool.load(appContext, resId, 1)
        rawResourceToSampleId[resId] = sampleId
        return sampleId
    }

    private fun startMusicIfAllowed() {
        if (released || paused || musicMuted) return
        val track = currentMusic ?: return
        val player = musicPlayer
        if (player == null) {
            rebuildMusicPlayer(track, crossfade = false)
            return
        }
        try {
            if (!player.isPlaying) player.start()
        } catch (_: IllegalStateException) {
            rebuildMusicPlayer(track, crossfade = false)
        }
    }

    private fun pauseMusic() {
        cancelMusicTransition(restoreActiveVolume = true)
        val player = musicPlayer ?: return
        try {
            if (player.isPlaying) player.pause()
        } catch (_: IllegalStateException) {
            releaseMusicPlayer()
        }
    }

    private fun rebuildMusicPlayer(
        track: MusicTrack? = currentMusic,
        crossfade: Boolean = true,
    ) {
        if (released || paused || musicMuted || track == null) return
        val resource = musicResources[track] ?: return
        cancelMusicTransition(restoreActiveVolume = true)
        val oldPlayer = musicPlayer
        musicPlayer = null
        musicExecutor.execute {
            if (released || paused || musicMuted || currentMusic != track) {
                releaseMediaPlayer(oldPlayer)
                return@execute
            }
            val player = MediaPlayer.create(appContext, resource) ?: run {
                releaseMediaPlayer(oldPlayer)
                return@execute
            }
            synchronized(this@KavvoroSoundEngine) {
                if (released || paused || musicMuted || currentMusic != track) {
                    releaseMediaPlayer(oldPlayer)
                    releaseMediaPlayer(player)
                    return@synchronized
                }
                try {
                    player.isLooping = true
                    val shouldCrossfade = crossfade && oldPlayer != null
                    val initialVolume = if (shouldCrossfade) 0f else musicOutputVolume()
                    player.setVolume(initialVolume, initialVolume)
                    player.start()
                    musicPlayer = player
                    if (shouldCrossfade) {
                        beginMusicCrossfade(oldPlayer, player)
                    } else {
                        releaseMediaPlayer(oldPlayer)
                    }
                } catch (_: IllegalStateException) {
                    releaseMediaPlayer(player)
                    releaseMediaPlayer(oldPlayer)
                }
            }
        }
    }

    private fun releaseMusicPlayer() {
        cancelMusicTransition(restoreActiveVolume = false)
        val player = musicPlayer
        musicPlayer = null
        releaseMediaPlayer(player)
    }

    private fun beginMusicCrossfade(oldPlayer: MediaPlayer?, newPlayer: MediaPlayer) {
        if (oldPlayer == null) return
        val fadeInSteps = MusicTransition.steps(0f, 1f, MusicTransition.DURATION_MS)
        fadingMusicPlayer = oldPlayer
        val stepDelayMs = (MusicTransition.DURATION_MS / fadeInSteps.lastIndex).coerceAtLeast(1L)
        val fade = object : Runnable {
            var stepIndex = 0

            override fun run() {
                if (
                    released ||
                    musicPlayer !== newPlayer ||
                    fadingMusicPlayer !== oldPlayer ||
                    paused ||
                    musicMuted
                ) {
                    return
                }

                val fadeIn = fadeInSteps[stepIndex]
                val fadeOut = 1f - fadeIn
                try {
                    val output = musicOutputVolume()
                    oldPlayer.setVolume(output * fadeOut, output * fadeOut)
                    newPlayer.setVolume(output * fadeIn, output * fadeIn)
                } catch (_: IllegalStateException) {
                    cancelMusicTransition(restoreActiveVolume = true)
                    return
                }

                if (stepIndex == fadeInSteps.lastIndex) {
                    fadingMusicPlayer = null
                    musicTransitionRunnable = null
                    releaseMediaPlayer(oldPlayer)
                    try {
                        val output = musicOutputVolume()
                        newPlayer.setVolume(output, output)
                    } catch (_: IllegalStateException) {
                        // The player was released by the platform while transitioning.
                    }
                    return
                }

                stepIndex += 1
                musicTransitionHandler.postDelayed(this, stepDelayMs)
            }
        }
        musicTransitionRunnable = fade
        musicTransitionHandler.post(fade)
    }

    private fun cancelMusicTransition(restoreActiveVolume: Boolean) {
        musicTransitionRunnable?.let(musicTransitionHandler::removeCallbacks)
        musicTransitionRunnable = null
        val fadingPlayer = fadingMusicPlayer
        fadingMusicPlayer = null
        releaseMediaPlayer(fadingPlayer)
        if (restoreActiveVolume) {
            try {
                val output = musicOutputVolume()
                musicPlayer?.setVolume(output, output)
            } catch (_: IllegalStateException) {
                // The player was released by the platform while transitioning.
            }
        }
    }

    private fun releaseMediaPlayer(player: MediaPlayer?) {
        if (player == null) return
        musicExecutor.execute {
            try {
                player.stop()
            } catch (_: IllegalStateException) {
                // Already stopped or never started.
            }
            player.release()
        }
    }

    private fun musicOutputVolume(): Float = MUSIC_VOLUME * masterVolume * musicVolume

    companion object {
        private const val SELECTION_PREVIEW_VOLUME = 0.86f
        private const val MUSIC_VOLUME = 0.3f
        private const val SELECTION_SOUND_COUNT = 50
        private val supportedAudioLanguageCodes = listOf(
            "ro",
            "es",
            "fr",
            "de",
            "it",
            "pt",
            "nl",
            "pl",
            "tr",
            "ru",
            "uk",
            "ar",
            "hi",
            "id",
            "vi",
            "ja",
            "ko",
            "zh"
        )

        private val selectionResources = listOf(
            R.raw.brainball_select_00, R.raw.brainball_select_01, R.raw.brainball_select_02,
            R.raw.brainball_select_03, R.raw.brainball_select_04, R.raw.brainball_select_05,
            R.raw.brainball_select_06, R.raw.brainball_select_07, R.raw.brainball_select_08,
            R.raw.brainball_select_09, R.raw.brainball_select_10, R.raw.brainball_select_11,
            R.raw.brainball_select_12, R.raw.brainball_select_13, R.raw.brainball_select_14,
            R.raw.brainball_select_15, R.raw.brainball_select_16, R.raw.brainball_select_17,
            R.raw.brainball_select_18, R.raw.brainball_select_19, R.raw.brainball_select_20,
            R.raw.brainball_select_21, R.raw.brainball_select_22, R.raw.brainball_select_23,
            R.raw.brainball_select_24, R.raw.brainball_select_25, R.raw.brainball_select_26,
            R.raw.brainball_select_27, R.raw.brainball_select_28, R.raw.brainball_select_29,
            R.raw.brainball_select_30, R.raw.brainball_select_31, R.raw.brainball_select_32,
            R.raw.brainball_select_33, R.raw.brainball_select_34, R.raw.brainball_select_35,
            R.raw.brainball_select_36, R.raw.brainball_select_37, R.raw.brainball_select_38,
            R.raw.brainball_select_39, R.raw.brainball_select_40, R.raw.brainball_select_41,
            R.raw.brainball_select_42, R.raw.brainball_select_43, R.raw.brainball_select_44,
            R.raw.brainball_select_45, R.raw.brainball_select_46, R.raw.brainball_select_47,
            R.raw.brainball_select_48, R.raw.brainball_select_49
        )

        private val bounceResources = listOf(
            R.raw.sfx_bounce_1,
            R.raw.sfx_bounce_2,
            R.raw.sfx_bounce_3,
            R.raw.sfx_bounce_4,
            R.raw.sfx_bounce_5,
            R.raw.sfx_bounce_6,
            R.raw.sfx_bounce_7,
            R.raw.sfx_bounce_8
        )

        private val eventResources = mapOf(
            SoundEvent.UI_TAP to R.raw.sfx_ui_tap,
            SoundEvent.LOCKED to R.raw.sfx_locked,
            SoundEvent.RIFT_ON to R.raw.sfx_rift_on,
            SoundEvent.RIFT_OFF to R.raw.sfx_rift_off,
            SoundEvent.GOAL to R.raw.sfx_goal,
            SoundEvent.FAIL to R.raw.sfx_fail,
            SoundEvent.POWER to R.raw.sfx_power,
            SoundEvent.UNLOCK to R.raw.sfx_unlock,
            SoundEvent.CHAIN to R.raw.sfx_chain
        )

        private val musicResources = mapOf(
            MusicTrack.MENU to R.raw.music_menu,
            MusicTrack.TUTORIAL to R.raw.music_tutorial,
            MusicTrack.CLASSIC to R.raw.music_classic,
            MusicTrack.CHAOS to R.raw.music_chaos
        )
    }
}
