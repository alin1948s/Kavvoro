package com.moonsolstudios.kavvoro.ui.screens.language

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
    val backButtonRect = RectF()
    val footerRect = RectF()
    val deckRect = RectF()
    val itemRects = MutableList(KavvoroLanguage.selectableLanguages.size) { RectF() }
    var latestLayout: LanguageSelectorLayout? = null

    fun itemAt(
        x: Float,
        y: Float,
        viewportTop: Float,
        viewportBottom: Float,
        itemRects: List<RectF>,
        layout: LanguageSelectorLayout? = null,
        dp: Float = 1f
    ): Int {
        val clipTop = layout?.gridViewport?.top ?: viewportTop
        val clipBottom = layout?.gridViewport?.bottom ?: viewportBottom
        if (y < clipTop || y > clipBottom) return -1

        if (layout != null) {
            val pad = 3f * dp
            return layout.languageCards.indexOfFirst { card ->
                x >= card.bounds.left - pad && x <= card.bounds.right + pad &&
                y >= card.bounds.top - pad && y <= card.bounds.bottom + pad
            }
        }
        val displayCount = itemRects.size
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
        dp: Float,
        layout: LanguageSelectorLayout? = null
    ) {
        val backHit = layout?.backButtonHitbox ?: languageBackButton
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutSelector()
                setTouchY(event.y)
                setLastY(event.y)
                setDragging(false)
                val index = when {
                    backHit.contains(event.x, event.y) -> LANGUAGE_BACK_INDEX
                    else -> itemAt(event.x, event.y, viewportTop, viewportBottom, languageItemRects, layout, dp)
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
                if (!languageDragging && released == LANGUAGE_BACK_INDEX && backHit.contains(event.x, event.y)) {
                    onBack()
                    performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                val language = displayLanguages.getOrNull(released) ?: return
                val rect = layout?.languageCards?.getOrNull(released)?.bounds ?: languageItemRects.getOrNull(released) ?: return
                val pad = 3f * dp
                val containsTouch = event.x >= rect.left - pad && event.x <= rect.right + pad &&
                                    event.y >= rect.top - pad && event.y <= rect.bottom + pad
                if (languageDragging || !containsTouch) return
                KavvoroI18n.setSelected(context, language)
                audio.setLanguageCode(KavvoroI18n.audioLanguageCode(context))
                performHaptic(HapticFeedbackCompat.confirm)
            }
        }
    }
}
