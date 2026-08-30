package com.moonsolstudios.kavvoro.ui.render

/** Returns [color] with a clamped 8-bit alpha channel. */
internal fun withAlpha(color: Int, alpha: Int): Int =
    (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
