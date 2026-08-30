package com.moonsolstudios.kavvoro.ui

import android.os.Build
import android.view.HapticFeedbackConstants

object HapticFeedbackCompat {
    val confirm: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.KEYBOARD_TAP
        }

    val reject: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
}
