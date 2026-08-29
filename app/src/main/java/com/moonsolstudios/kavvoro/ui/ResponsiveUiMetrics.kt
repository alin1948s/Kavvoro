package com.moonsolstudios.kavvoro.ui

import android.util.DisplayMetrics
import kotlin.math.min

/**
 * One responsive scale for all non-gameplay UI.
 *
 * The game is authored around a 390 x 844 dp portrait canvas.  The scale is
 * constrained by both axes so a very tall phone does not create oversized
 * controls while a short phone still keeps the same visual proportions.
 */
data class ResponsiveUiMetrics(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val scaledDensity: Float,
    val widthDp: Float,
    val heightDp: Float,
    val scale: Float,
    val safeLeftPx: Int = 0,
    val safeTopPx: Int = 0,
    val safeRightPx: Int = 0,
    val safeBottomPx: Int = 0
) {
    val contentWidthPx: Int
        get() = (widthPx - safeLeftPx - safeRightPx).coerceAtLeast(1)

    val contentHeightPx: Int
        get() = (heightPx - safeTopPx - safeBottomPx).coerceAtLeast(1)

    val compact: Boolean
        get() = heightDp < 700f

    val veryCompact: Boolean
        get() = heightDp < 625f

    fun px(dp: Float): Float = dp * density * scale

    fun textSp(sp: Float): Float = sp * scale

    companion object {
        const val REFERENCE_WIDTH_DP = 390f
        const val REFERENCE_HEIGHT_DP = 844f
        const val MIN_SCALE = 0.76f
        const val MAX_SCALE = 1.45f

        fun from(
            widthPx: Int,
            heightPx: Int,
            displayMetrics: DisplayMetrics,
            safeLeftPx: Int = 0,
            safeTopPx: Int = 0,
            safeRightPx: Int = 0,
            safeBottomPx: Int = 0
        ): ResponsiveUiMetrics {
            val density = displayMetrics.density.coerceAtLeast(0.1f)
            val scaledDensity = displayMetrics.scaledDensity.coerceAtLeast(0.1f)
            val usableWidthPx = (widthPx - safeLeftPx - safeRightPx).coerceAtLeast(1)
            val usableHeightPx = (heightPx - safeTopPx - safeBottomPx).coerceAtLeast(1)
            val widthDp = usableWidthPx / density
            val heightDp = usableHeightPx / density
            val scale = min(
                widthDp / REFERENCE_WIDTH_DP,
                heightDp / REFERENCE_HEIGHT_DP
            ).coerceIn(MIN_SCALE, MAX_SCALE)
            return ResponsiveUiMetrics(
                widthPx = widthPx.coerceAtLeast(1),
                heightPx = heightPx.coerceAtLeast(1),
                density = density,
                scaledDensity = scaledDensity,
                widthDp = widthDp,
                heightDp = heightDp,
                scale = scale,
                safeLeftPx = safeLeftPx,
                safeTopPx = safeTopPx,
                safeRightPx = safeRightPx,
                safeBottomPx = safeBottomPx
            )
        }

        fun scaleFor(
            widthPx: Int,
            heightPx: Int,
            displayMetrics: DisplayMetrics
        ): Float = from(widthPx, heightPx, displayMetrics).scale
    }
}
