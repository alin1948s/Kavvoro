package com.moonsolstudios.kavvoro.ui.layout

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.LayoutMode
import kotlin.math.max
import kotlin.math.min

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

    fun toRectF(): RectF = RectF(left, top, right, bottom)

    fun toRectF(target: RectF): RectF {
        target.set(left, top, right, bottom)
        return target
    }
}

class HomeLayoutCalculator {
    var screenWidth: Float = 0f
        private set
    var screenHeight: Float = 0f
        private set
    var density: Float = 1f
        private set

    var layoutMode: LayoutMode = LayoutMode.COMPACT
        private set

    val contentRect = LayoutRect()
    val headerRect = LayoutRect()
    val brandRect = LayoutRect()
    val settingsButtonRect = LayoutRect()
    val soundButtonRect = LayoutRect()

    val statsRect = LayoutRect()
    val statCardRects = Array(4) { LayoutRect() }

    val heroRect = LayoutRect()
    val portalRect = LayoutRect()
    val platformRect = LayoutRect()
    val characterRect = LayoutRect()
    val portalFrontRect = LayoutRect()

    val riftStatusRect = LayoutRect()
    val playCtaRect = LayoutRect()

    val leaderboardsCardRect = LayoutRect()
    val vaultCardRect = LayoutRect()
    val collectionCardRect = LayoutRect()

    val footerRect = LayoutRect()

    fun pxToDp(px: Float): Float = px / density.coerceAtLeast(0.1f)

    fun dp(value: Float): Float = value * density

    fun calculate(
        width: Float,
        height: Float,
        displayDensity: Float,
        brandAspect: Float = 2095f / 499f,
        portalAspect: Float = 1254f / 1225f,
        platformAspect: Float = 2075f / 524f
    ) {
        screenWidth = width.coerceAtLeast(1f)
        screenHeight = height.coerceAtLeast(1f)
        density = displayDensity.coerceAtLeast(0.1f)

        val widthDp = pxToDp(screenWidth)
        val heightDp = pxToDp(screenHeight)

        layoutMode = when {
            widthDp <= 480f -> LayoutMode.COMPACT
            widthDp <= 840f -> LayoutMode.MEDIUM
            else -> LayoutMode.TABLET
        }

        calculateContentRect(widthDp)

        val scaleFactor = (heightDp / 800f).coerceIn(0.72f, 1.25f)

        // 1. Header calculation
        val headerTop = dp(16f * scaleFactor)
        val logoWidth = when (layoutMode) {
            LayoutMode.COMPACT -> contentRect.width() * 0.47f
            LayoutMode.MEDIUM -> contentRect.width() * 0.43f
            LayoutMode.TABLET -> contentRect.width() * 0.36f
        }
        val logoHeight = logoWidth / brandAspect.coerceAtLeast(0.1f)
        brandRect.set(
            contentRect.left,
            headerTop,
            contentRect.left + logoWidth,
            headerTop + logoHeight
        )

        val actionButtonSize = dp(if (layoutMode == LayoutMode.TABLET) 44f else 38f) * scaleFactor
        val actionGap = dp(8f * scaleFactor)
        soundButtonRect.set(
            contentRect.right - actionButtonSize,
            headerTop + (logoHeight - actionButtonSize) * 0.5f,
            contentRect.right,
            headerTop + (logoHeight - actionButtonSize) * 0.5f + actionButtonSize
        )
        settingsButtonRect.set(
            soundButtonRect.left - actionGap - actionButtonSize,
            soundButtonRect.top,
            soundButtonRect.left - actionGap,
            soundButtonRect.bottom
        )
        headerRect.set(
            contentRect.left,
            headerTop,
            contentRect.right,
            max(brandRect.bottom, soundButtonRect.bottom)
        )

        // 2. Stats row calculation
        val statsTop = headerRect.bottom + dp(10f * scaleFactor)
        val statsHeight = dp(if (layoutMode == LayoutMode.TABLET) 50f else 44f) * scaleFactor
        statsRect.set(contentRect.left, statsTop, contentRect.right, statsTop + statsHeight)

        val cardGap = dp(8f * scaleFactor)
        val cardWidth = (contentRect.width() - cardGap * 3f) / 4f
        for (i in 0 until 4) {
            val left = contentRect.left + i * (cardWidth + cardGap)
            statCardRects[i].set(left, statsTop, left + cardWidth, statsTop + statsHeight)
        }

        // 3. Lower navigation & Play CTA bottom-up layout
        val footerHeight = dp(18f * scaleFactor)
        val footerBottom = screenHeight - dp(10f * scaleFactor)
        footerRect.set(contentRect.left, footerBottom - footerHeight, contentRect.right, footerBottom)

        val rowGap = dp(if (layoutMode == LayoutMode.TABLET) 10f else 7f) * scaleFactor
        val cardHeight = dp(if (layoutMode == LayoutMode.TABLET) 56f else 48f) * scaleFactor
        val playHeight = dp(if (layoutMode == LayoutMode.TABLET) 76f else 66f) * scaleFactor

        val playAspect = 2048f / 559f

        if (layoutMode == LayoutMode.TABLET) {
            // Tablet: Leaderboards full-width, Vault + Collection 2-column
            val colGap = dp(16f * scaleFactor)
            val halfColWidth = (contentRect.width() - colGap) / 2f
            val lowerRowBottom = footerRect.top - dp(8f * scaleFactor)
            val lowerRowTop = lowerRowBottom - cardHeight

            vaultCardRect.set(contentRect.left, lowerRowTop, contentRect.left + halfColWidth, lowerRowBottom)
            collectionCardRect.set(vaultCardRect.right + colGap, lowerRowTop, contentRect.right, lowerRowBottom)

            val leaderboardsBottom = vaultCardRect.top - rowGap
            leaderboardsCardRect.set(contentRect.left, leaderboardsBottom - cardHeight, contentRect.right, leaderboardsBottom)

            val playBottom = leaderboardsCardRect.top - rowGap
            val maxPlayW = min(contentRect.width(), dp(820f))
            val targetPlayH = dp(78f * scaleFactor)
            val playW = min(maxPlayW, targetPlayH * playAspect)
            val playH = playW / playAspect
            val playCx = contentRect.centerX()
            playCtaRect.set(playCx - playW * 0.5f, playBottom - playH, playCx + playW * 0.5f, playBottom)
        } else {
            // Phone: 3 Stacked rows
            val collectionBottom = footerRect.top - dp(8f * scaleFactor)
            collectionCardRect.set(contentRect.left, collectionBottom - cardHeight, contentRect.right, collectionBottom)

            val vaultBottom = collectionCardRect.top - rowGap
            vaultCardRect.set(contentRect.left, vaultBottom - cardHeight, contentRect.right, vaultBottom)

            val leaderboardsBottom = vaultCardRect.top - rowGap
            leaderboardsCardRect.set(contentRect.left, leaderboardsBottom - cardHeight, contentRect.right, leaderboardsBottom)

            val playBottom = leaderboardsCardRect.top - rowGap
            val maxPlayW = contentRect.width()
            val targetPlayH = dp(72f * scaleFactor)
            val playW = min(maxPlayW, targetPlayH * playAspect)
            val playH = playW / playAspect
            val playCx = contentRect.centerX()
            playCtaRect.set(playCx - playW * 0.5f, playBottom - playH, playCx + playW * 0.5f, playBottom)
        }

        // 4. Hero stage calculation (fitted between stats and play CTA)
        val heroTop = statsRect.bottom + dp(6f * scaleFactor)
        val heroBottom = playCtaRect.top - dp(8f * scaleFactor)
        val heroHeight = (heroBottom - heroTop).coerceAtLeast(dp(100f))
        heroRect.set(contentRect.left, heroTop, contentRect.right, heroTop + heroHeight)

        val portalWidth = when (layoutMode) {
            LayoutMode.COMPACT -> min(contentRect.width() * 0.76f, heroRect.height() * 0.78f)
            LayoutMode.MEDIUM -> min(contentRect.width() * 0.70f, heroRect.height() * 0.76f)
            LayoutMode.TABLET -> min(contentRect.width() * 0.52f, dp(480f))
        }
        val portalHeight = portalWidth / portalAspect.coerceAtLeast(0.1f)
        val portalCx = contentRect.centerX()
        val portalCy = heroRect.centerY() - dp(10f * scaleFactor)

        portalRect.set(
            portalCx - portalWidth / 2f,
            portalCy - portalHeight / 2f,
            portalCx + portalWidth / 2f,
            portalCy + portalHeight / 2f
        )
        portalFrontRect.set(portalRect)

        val platformWidth = portalRect.width() * 0.88f
        val platformHeight = platformWidth / platformAspect.coerceAtLeast(0.1f)
        val platformCy = portalRect.centerY() + portalRect.height() * 0.38f

        platformRect.set(
            portalRect.centerX() - platformWidth / 2f,
            platformCy - platformHeight / 2f,
            portalRect.centerX() + platformWidth / 2f,
            platformCy + platformHeight / 2f
        )

        // Brainball sizing and placement - centered inside portal
        val characterSize = portalRect.width() * 0.54f
        val characterCx = portalRect.centerX()
        val characterCy = portalRect.centerY()

        characterRect.set(
            characterCx - characterSize / 2f,
            characterCy - characterSize / 2f,
            characterCx + characterSize / 2f,
            characterCy + characterSize / 2f
        )

        // Rift status capsule
        val riftCapsuleH = dp(24f * scaleFactor)
        val riftCapsuleW = min(contentRect.width() * 0.62f, dp(240f * scaleFactor))
        val riftCy = min(platformRect.bottom + dp(12f * scaleFactor), heroRect.bottom - riftCapsuleH * 0.5f)
        riftStatusRect.set(
            contentRect.centerX() - riftCapsuleW / 2f,
            riftCy - riftCapsuleH / 2f,
            contentRect.centerX() + riftCapsuleW / 2f,
            riftCy + riftCapsuleH / 2f
        )
    }

    private fun calculateContentRect(widthDp: Float) {
        val fraction = when {
            widthDp <= 480f -> 0.94f
            widthDp <= 840f -> 0.90f
            else -> 0.82f
        }

        val maxWidthPx = dp(
            when {
                widthDp <= 480f -> 460f
                widthDp <= 840f -> 720f
                else -> 1100f
            }
        )

        val contentWidth = min(
            screenWidth * fraction,
            maxWidthPx
        )

        val left = (screenWidth - contentWidth) / 2f

        contentRect.set(
            left,
            0f,
            left + contentWidth,
            screenHeight
        )
    }
}
