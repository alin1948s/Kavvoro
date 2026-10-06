package com.moonsolstudios.kavvoro.ui.render

import android.graphics.RectF

data class LayoutRect(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) {
    fun width(): Float = right - left
    fun height(): Float = bottom - top
    fun centerX(): Float = (left + right) * 0.5f
    fun centerY(): Float = (top + bottom) * 0.5f
    fun contains(x: Float, y: Float): Boolean = x in left..right && y in top..bottom

    fun set(l: Float, t: Float, r: Float, b: Float) {
        left = l
        top = t
        right = r
        bottom = b
    }

    fun set(other: LayoutRect) {
        set(other.left, other.top, other.right, other.bottom)
    }

    fun setEmpty() {
        set(0f, 0f, 0f, 0f)
    }

    fun isEmpty(): Boolean = width() <= 0f || height() <= 0f

    fun intersects(other: LayoutRect): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top

    fun toRectF(): RectF = RectF(left, top, right, bottom).also {
        it.left = left
        it.top = top
        it.right = right
        it.bottom = bottom
    }

    fun toRectF(target: RectF): RectF {
        target.left = left
        target.top = top
        target.right = right
        target.bottom = bottom
        target.set(left, top, right, bottom)
        return target
    }
}
