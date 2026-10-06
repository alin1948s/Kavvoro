package com.moonsolstudios.kavvoro.ui.layout

import kotlin.math.min

/** Pure helpers for viewport geometry shared by independent screens. */
object ViewportLayoutCalculator {
    fun centeredContentWidth(
        viewWidth: Float,
        density: Float,
        horizontalInsetDp: Float,
        maxWidthDp: Float
    ): Float = min(viewWidth - density * horizontalInsetDp, density * maxWidthDp).coerceAtLeast(0f)

    fun centeredLeft(viewWidth: Float, contentWidth: Float): Float =
        (viewWidth - contentWidth) * 0.5f

    fun contentRight(contentLeft: Float, contentWidth: Float): Float =
        contentLeft + contentWidth

    fun bottomInset(viewHeight: Float, density: Float, insetDp: Float): Float =
        viewHeight - density * insetDp
}
