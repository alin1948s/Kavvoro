package com.moonsolstudios.kavvoro.ui.controller

import android.content.Context
import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.audio.KavvoroSoundEngine
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.ui.HapticFeedbackCompat
import kotlin.math.abs

object LanguageTouchController {

    const val LANGUAGE_BACK_INDEX = -2

    fun itemAt(
        x: Float,
        y: Float,
        viewportTop: Float,
        viewportBottom: Float,
        itemRects: List<RectF>
    ): Int {
        if (y < viewportTop || y > viewportBottom) return -1
        val displayCount = KavvoroLanguage.entries.size
        return (0 until displayCount).firstOrNull { itemRects.getOrNull(it)?.contains(x, y) == true } ?: -1
    }

    fun handleTouch(
        event: MotionEvent,
        layoutSelector: () -> Unit,
        activeLanguageIndex: Int,
        setActiveIndex: (Int) -> Unit,
        languageDragging: Boolean,
        setDragging: (Boolean) -> Unit,
        languageTouchY: Float,
        setTouchY: (Float) -> Unit,
        languageLastY: Float,
        setLastY: (Float) -> Unit,
        languageScroll: Float,
        setScroll: (Float) -> Unit,
        languageMaxScroll: Float,
        languageBackButton: RectF,
        languageItemRects: List<RectF>,
        displayLanguages: List<KavvoroLanguage>,
        viewportTop: Float,
        viewportBottom: Float,
        context: Context,
        audio: KavvoroSoundEngine,
        onBack: () -> Unit,
        performHaptic: (Int) -> Unit,
        dp: Float
    ) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutSelector()
                setTouchY(event.y)
                setLastY(event.y)
                setDragging(false)
                val index = when {
                    languageBackButton.contains(event.x, event.y) -> LANGUAGE_BACK_INDEX
                    else -> itemAt(event.x, event.y, viewportTop, viewportBottom, languageItemRects)
                }
                setActiveIndex(index)
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - languageLastY
                if (abs(event.y - languageTouchY) > 5f * dp) {
                    setDragging(true)
                    setActiveIndex(-1)
                }
                setScroll((languageScroll - dy).coerceIn(0f, languageMaxScroll))
                setLastY(event.y)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val released = activeLanguageIndex
                setActiveIndex(-1)
                if (!languageDragging && released == LANGUAGE_BACK_INDEX && languageBackButton.contains(event.x, event.y)) {
                    onBack()
                    performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                val language = displayLanguages.getOrNull(released) ?: return
                val rect = languageItemRects.getOrNull(released) ?: return
                if (languageDragging || !rect.contains(event.x, event.y)) return
                KavvoroI18n.setSelected(context, language)
                audio.setLanguageCode(KavvoroI18n.audioLanguageCode(context))
                performHaptic(HapticFeedbackCompat.confirm)
            }
        }
    }
}
