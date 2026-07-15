package com.moonsolstudios.kavvoro.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.moonsolstudios.kavvoro.R
import java.util.concurrent.ConcurrentHashMap
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
    private val selectionSounds: IntArray
    private val localizedSelectionSounds = ConcurrentHashMap<String, IntArray>()
    private val bounceSounds: IntArray
    private val eventSounds: Map<SoundEvent, Int>
    @Volatile
    private var released = false
    @Volatile
    private var sfxMuted = false
    @Volatile
    private var musicMuted = false
    @Volatile
    private var paused = false
    private var languageCode = "en"
    private var activeSelectionStream = 0
    private var currentMusic: MusicTrack? = null
    private var musicPlayer: MediaPlayer? = null

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSounds += sampleId
        }
        selectionSounds = selectionResources.map { soundPool.load(context, it, 1) }.toIntArray()
        bounceSounds = bounceResources.map { soundPool.load(context, it, 1) }.toIntArray()
        eventSounds = eventResources.mapValues { (_, resource) -> soundPool.load(context, resource, 1) }
    }

    fun playSelection(index: Int) {
        if (sfxMuted) return
        val sound = selectionSoundsForLanguage().getOrNull(index) ?: return
        if (activeSelectionStream != 0) {
            soundPool.stop(activeSelectionStream)
            activeSelectionStream = 0
        }
        activeSelectionStream = play(sound, volume = 0.86f, rate = 1f, priority = 5)
    }

    @Synchronized
    fun setLanguageCode(code: String) {
        if (languageCode == code) return
        languageCode = code
        ensureSelectionPackLoaded(code)
        if (activeSelectionStream != 0) {
            soundPool.stop(activeSelectionStream)
            activeSelectionStream = 0
        }
    }

    fun playBounce(brainballIndex: Int, impactStrength: Float) {
        if (sfxMuted) return
        if (impactStrength < 0.7f) return
        val normalized = ((impactStrength - 0.7f) / 6.2f).coerceIn(0f, 1f)
        val tier = (normalized * (bounceSounds.size - 1)).roundToInt()
        val sound = bounceSounds[tier]
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
        val sound = eventSounds[event] ?: return
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
        rebuildMusicPlayer()
    }

    @Synchronized
    fun setSfxMuted(muted: Boolean) {
        sfxMuted = muted
        if (muted && activeSelectionStream != 0) {
            soundPool.stop(activeSelectionStream)
            activeSelectionStream = 0
        }
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
        if (activeSelectionStream != 0) {
            soundPool.stop(activeSelectionStream)
            activeSelectionStream = 0
        }
        releaseMusicPlayer()
        soundPool.release()
        loadedSounds.clear()
    }

    private fun play(sound: Int, volume: Float, rate: Float, priority: Int): Int {
        if (released || sfxMuted || sound !in loadedSounds) return 0
        return soundPool.play(sound, volume, volume, priority, 0, rate.coerceIn(0.5f, 2f))
    }

    private fun selectionSoundsForLanguage(): IntArray {
        return localizedSelectionSounds[languageCode] ?: ensureSelectionPackLoaded(languageCode) ?: selectionSounds
    }

    private fun ensureSelectionPackLoaded(languageCode: String): IntArray? {
        if (languageCode == "en" || languageCode !in supportedAudioLanguageCodes) return null
        localizedSelectionSounds[languageCode]?.let { return it }
        val loaded = loadSelectionPack(languageCode) ?: return null
        localizedSelectionSounds[languageCode] = loaded
        return loaded
    }

    private fun loadSelectionPack(languageCode: String): IntArray? {
        val resources = (0 until SELECTION_SOUND_COUNT).map { index ->
            appContext.resources.getIdentifier(
                "brainball_select_${languageCode}_${index.toString().padStart(2, '0')}",
                "raw",
                appContext.packageName
            )
        }
        if (resources.any { it == 0 }) return null
        return resources.map { soundPool.load(appContext, it, 1) }.toIntArray()
    }

    private fun startMusicIfAllowed() {
        if (released || paused || musicMuted) return
        val track = currentMusic ?: return
        val player = musicPlayer
        if (player == null) {
            rebuildMusicPlayer()
            return
        }
        try {
            if (!player.isPlaying) player.start()
        } catch (_: IllegalStateException) {
            rebuildMusicPlayer(track)
        }
    }

    private fun pauseMusic() {
        val player = musicPlayer ?: return
        try {
            if (player.isPlaying) player.pause()
        } catch (_: IllegalStateException) {
            releaseMusicPlayer()
        }
    }

    private fun rebuildMusicPlayer(track: MusicTrack? = currentMusic) {
        releaseMusicPlayer()
        if (released || paused || musicMuted || track == null) return
        val resource = musicResources[track] ?: return
        val player = MediaPlayer.create(appContext, resource) ?: return
        try {
            player.isLooping = true
            player.setVolume(MUSIC_VOLUME, MUSIC_VOLUME)
            player.start()
            musicPlayer = player
        } catch (_: IllegalStateException) {
            player.release()
        }
    }

    private fun releaseMusicPlayer() {
        val player = musicPlayer ?: return
        musicPlayer = null
        try {
            player.stop()
        } catch (_: IllegalStateException) {
            // Already stopped or never started.
        }
        player.release()
    }

    companion object {
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
