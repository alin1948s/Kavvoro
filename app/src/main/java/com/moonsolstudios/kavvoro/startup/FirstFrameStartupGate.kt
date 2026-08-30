package com.moonsolstudios.kavvoro.startup

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Keeps optional SDK work off the launch critical path and guarantees that it
 * starts at most once after the game has presented its first frame.
 */
class FirstFrameStartupGate {
    private val started = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    fun runOnce(action: () -> Unit): Boolean {
        if (cancelled.get() || !started.compareAndSet(false, true)) return false
        if (cancelled.get()) return false
        action()
        return true
    }

    fun cancel() {
        cancelled.set(true)
    }
}
