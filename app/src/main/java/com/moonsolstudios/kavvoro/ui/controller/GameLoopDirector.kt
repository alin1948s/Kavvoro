package com.moonsolstudios.kavvoro.ui.controller

import android.graphics.Canvas
import android.os.Build
import android.view.SurfaceHolder

/**
 * Surface lifecycle and frame render coordinator for hardware/software canvas locking.
 */
object GameLoopDirector {

    fun lockRenderCanvas(holder: SurfaceHolder): Canvas? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                holder.lockHardwareCanvas()
            } catch (_: IllegalArgumentException) {
                holder.lockCanvas()
            } catch (_: IllegalStateException) {
                holder.lockCanvas()
            } catch (_: UnsupportedOperationException) {
                holder.lockCanvas()
            }
        } else {
            holder.lockCanvas()
        }
    }
}
