package com.moonsolstudios.kavvoro.ui.screens.ad

import android.graphics.RectF
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.model.ButtonId
import kotlin.math.min

/**
 * Dedicated layout and touch controller for the Ad Checkpoint / Rewarded Continue screen (Screen.AD).
 */
object AdScreenTouchController {

    val adButtonRect = RectF()

    fun layoutAdButton(
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        adButton: RectF
    ) {
        val panelWidth = min(viewWidth - 32f * dp, 390f * dp)
        val panelHeight = 206f * dp
        val left = viewWidth * 0.5f - panelWidth * 0.5f
        val top = viewHeight * 0.5f - panelHeight * 0.5f
        val btnLeft = left + 20f * dp
        val btnTop = top + panelHeight - 68f * dp
        val btnRight = left + panelWidth - 20f * dp
        val btnBottom = top + panelHeight - 20f * dp
        adButton.left = btnLeft
        adButton.top = btnTop
        adButton.right = btnRight
        adButton.bottom = btnBottom
        adButton.set(btnLeft, btnTop, btnRight, btnBottom)
    }

    fun buttonAt(x: Float, y: Float, adButton: RectF = adButtonRect): ButtonId {
        val hit = adButton.contains(x, y) ||
            (adButton.right > adButton.left && adButton.bottom > adButton.top &&
                x >= adButton.left && x < adButton.right && y >= adButton.top && y < adButton.bottom)
        return if (hit) ButtonId.AD_CONTINUE else ButtonId.NONE
    }

    fun handleTouch(
        event: MotionEvent,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        adButton: RectF,
        activeButton: ButtonId,
        setActiveButton: (ButtonId) -> Unit,
        onAction: (ButtonId) -> (() -> Unit)?
    ): (() -> Unit)? {
        if (adButton.isEmpty) {
            layoutAdButton(viewWidth, viewHeight, dp, adButton)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                setActiveButton(buttonAt(event.x, event.y, adButton))
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val releasedButton = activeButton
                setActiveButton(ButtonId.NONE)
                if (releasedButton != ButtonId.NONE && buttonAt(event.x, event.y, adButton) == releasedButton) {
                    return onAction(releasedButton)
                }
            }
        }
        return null
    }
}
