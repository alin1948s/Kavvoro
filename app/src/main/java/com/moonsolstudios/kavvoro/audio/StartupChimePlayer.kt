package com.moonsolstudios.kavvoro.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.moonsolstudios.kavvoro.R

/** Plays the short brand chime when the MoonSol launch lines meet. */
class StartupChimePlayer(context: Context) : AutoCloseable {
    private val lock = Any()
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var sampleId = 0
    private var loadedSampleId = 0
    private var pendingVolume = 0f
    private var released = false

    init {
        soundPool.setOnLoadCompleteListener { pool, loadedId, status ->
            val queuedVolume = synchronized(lock) {
                if (released || status != 0) {
                    pendingVolume = 0f
                    0f
                } else {
                    loadedSampleId = loadedId
                    pendingVolume.also { pendingVolume = 0f }
                }
            }
            if (queuedVolume > 0f) {
                pool.play(loadedId, queuedVolume, queuedVolume, 2, 0, 1f)
            }
        }
        sampleId = soundPool.load(context.applicationContext, R.raw.moonsol_merge_chime, 1)
    }

    fun play(volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        val readySample = synchronized(lock) {
            if (released || safeVolume <= 0f) return
            if (loadedSampleId == sampleId && sampleId != 0) {
                sampleId
            } else {
                pendingVolume = maxOf(pendingVolume, safeVolume)
                0
            }
        }
        if (readySample != 0) {
            soundPool.play(readySample, safeVolume, safeVolume, 2, 0, 1f)
        }
    }

    override fun close() {
        synchronized(lock) {
            if (released) return
            released = true
            pendingVolume = 0f
        }
        soundPool.release()
    }
}
