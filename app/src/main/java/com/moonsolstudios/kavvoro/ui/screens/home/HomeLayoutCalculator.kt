package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.LayoutMode
import kotlin.math.max
import kotlin.math.min

typealias LayoutRect = com.moonsolstudios.kavvoro.ui.render.LayoutRect

enum class LandscapeClass {
    COMPACT,
    STANDARD,
    WIDE
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
    var landscapeClass: LandscapeClass? = null
        private set

    val contentRect = LayoutRect()
    val bodyRect = LayoutRect()
    val heroStageRect = LayoutRect()
    val navigationDeckRect = LayoutRect()
    val headerRect = LayoutRect()
    val brandRect = LayoutRect()
    val brandMottoRect = LayoutRect()
    val settingsButtonRect = LayoutRect()
    val soundButtonRect = LayoutRect()

    val statsRect = LayoutRect()
    val statCardRects = Array(4) { LayoutRect() }
    val streakChipRect get() = statCardRects[0]
    val levelChipRect get() = statCardRects[1]
    val coinsChipRect get() = statCardRects[2]

    // Touch/hit rects: the visual rects above grown to the platform minimum size. Rendering
    // keeps using the visual rects so the reference composition is untouched, while touch
    // hit-testing and the TalkBack bounds use these.
    val settingsTouchRect = LayoutRect()
    val streakChipTouchRect = LayoutRect()
    val levelChipTouchRect = LayoutRect()
    val coinsChipTouchRect = LayoutRect()
    val bannerTouchRect = LayoutRect()

    val heroRect = LayoutRect()
    val portalRect = LayoutRect()
    val portalBeamRect = LayoutRect()
    val platformRect = LayoutRect()
    val characterRect = LayoutRect()
    val heroMascotRect get() = characterRect
    val portalRingRect get() = portalRect
    val portalFrontRect = LayoutRect()

    val planetBlueRect = LayoutRect()
    val planetPinkRect = LayoutRect()
    val asteroidLeftRect = LayoutRect()
    val asteroidRightRect = LayoutRect()

    val riftStatusRect = LayoutRect()
    val playCtaRect = LayoutRect()

    val leaderboardsCardRect = LayoutRect()
    val vaultCardRect = LayoutRect()
    val collectionCardRect = LayoutRect()
    val skinsCardRect get() = collectionCardRect
    val missionsCardRect get() = vaultCardRect
    val leaderboardCardRect get() = leaderboardsCardRect

    val bannerCardRect = LayoutRect()
    val footerRect = LayoutRect()

    var showPlaySubtitle: Boolean = true
        private set
    var showCardSubtitles: Boolean = true
        private set
    var reduceDecor: Boolean = false
        private set

    fun pxToDp(px: Float): Float = px / density.coerceAtLeast(0.1f)

    fun dp(value: Float): Float = value * density

    /**
     * Grows [visual] to at least [MIN_TOUCH_TARGET_DP] on both axes, keeping its centre.
     *
     * [maxHorizontalGrowth] stops neighbouring targets from overlapping: the stat chips sit a
     * few dp apart, so they grow into that gap rather than into each other.
     */
    private fun growToTouchTarget(
        target: LayoutRect,
        visual: LayoutRect,
        maxHorizontalGrowth: Float = Float.MAX_VALUE
    ) {
        val minSize = dp(MIN_TOUCH_TARGET_DP)
        if (visual.isEmpty()) {
            // Never invent a target near the origin for a surface this layout does not use.
            target.setEmpty()
            return
        }
        val horizontalGrowth = ((minSize - visual.width()) * 0.5f).coerceIn(0f, maxHorizontalGrowth)
        val verticalGrowth = ((minSize - visual.height()) * 0.5f).coerceAtLeast(0f)
        target.set(
            visual.left - horizontalGrowth,
            visual.top - verticalGrowth,
            visual.right + horizontalGrowth,
            visual.bottom + verticalGrowth
        )
    }

    /** Recomputes every touch rect from the visual rects after a layout pass. */
    private fun syncTouchTargets() {
        val minSize = dp(MIN_TOUCH_TARGET_DP)
        // The chips are always at least 6dp * 0.72 = 4.32dp apart, so 2dp per side is safe.
        val chipGrowthLimit = dp(2f)

        growToTouchTarget(settingsTouchRect, settingsButtonRect)
        // The gear sits against the content edge: keep the whole target on screen, then recover
        // any width lost to that clamp on the inward side. The gear is hit-tested before the stat
        // chips, and the chips are read-only, so growing over them is safe.
        settingsTouchRect.right = settingsTouchRect.right.coerceAtMost(screenWidth)
        settingsTouchRect.left = settingsTouchRect.left.coerceAtLeast(0f)
        if (settingsTouchRect.width() < minSize) {
            settingsTouchRect.left = (settingsTouchRect.right - minSize).coerceAtLeast(0f)
        }

        growToTouchTarget(streakChipTouchRect, statCardRects[0], chipGrowthLimit)
        growToTouchTarget(levelChipTouchRect, statCardRects[1], chipGrowthLimit)
        growToTouchTarget(coinsChipTouchRect, statCardRects[2], chipGrowthLimit)
        growToTouchTarget(bannerTouchRect, bannerCardRect)
    }

    fun calculate(
        width: Float,
        height: Float,
        displayDensity: Float,
        brandAspect: Float = 1422f / 675f,
        portalAspect: Float = 1254f / 1225f,
        platformAspect: Float = 2075f / 524f
    ) {
        screenWidth = width.coerceAtLeast(1f)
        screenHeight = height.coerceAtLeast(1f)
        density = displayDensity.coerceAtLeast(0.1f)

        if (screenWidth > screenHeight) {
            calculateLandscape(brandAspect, portalAspect, platformAspect)
        } else {
            calculatePortrait(brandAspect, portalAspect, platformAspect)
        }

        syncTouchTargets()
    }

    private fun calculatePortrait(
        brandAspect: Float,
        portalAspect: Float,
        platformAspect: Float
    ) {
        val widthDp = pxToDp(screenWidth)
        val heightDp = pxToDp(screenHeight)

        landscapeClass = null
        layoutMode = when {
            widthDp <= 480f -> LayoutMode.COMPACT
            widthDp > 840f || (widthDp >= 750f && heightDp >= 1200f && density > 1.2f) -> LayoutMode.TABLET
            else -> LayoutMode.MEDIUM
        }

        // Responsive degradation flags
        showPlaySubtitle = heightDp >= 700f
        showCardSubtitles = heightDp >= 780f
        reduceDecor = heightDp < 650f

        calculatePortraitContentRect(widthDp)

        val scaleFactor = (heightDp / 800f).coerceIn(0.72f, 1.25f)

        // 1. Header calculation
        val headerTop = brandTop(scaleFactor)
        val actionButtonSize = dp(if (layoutMode == LayoutMode.TABLET) 44f else 38f) * scaleFactor
        val actionGap = dp(8f * scaleFactor)

        val maxLogoWidth = when (layoutMode) {
            LayoutMode.COMPACT -> contentRect.width() * 0.44f
            LayoutMode.MEDIUM -> contentRect.width() * 0.40f
            LayoutMode.TABLET -> min(dp(260f), contentRect.width() * 0.35f)
        }
        val safeLogoWidth = min(maxLogoWidth, (contentRect.width() * 0.50f - actionGap * 2f).coerceAtLeast(dp(60f)))
        val logoHeight = safeLogoWidth / brandAspect.coerceAtLeast(0.1f)
        val headerCenterY = headerTop + logoHeight * 0.5f

        val chipHeight = actionButtonSize
        val chipTop = (headerCenterY - chipHeight * 0.5f).coerceAtLeast(headerTop)

        settingsButtonRect.set(
            contentRect.right - actionButtonSize,
            chipTop,
            contentRect.right,
            chipTop + actionButtonSize
        )
        soundButtonRect.set(settingsButtonRect)

        brandRect.set(
            contentRect.left,
            headerTop,
            contentRect.left + safeLogoWidth,
            headerTop + logoHeight
        )

        brandMottoRect.set(
            contentRect.left,
            brandRect.bottom + dp(1f),
            brandRect.right,
            brandRect.bottom + dp(13f * scaleFactor)
        )

        // 3 chips: Streak, Level, Coins
        val availableChipsWidth = (settingsButtonRect.left - actionGap) - (brandRect.right + actionGap)
        val chipsFitInRow = availableChipsWidth >= dp(140f) * scaleFactor

        if (chipsFitInRow) {
            val chipGap = dp(6f * scaleFactor)
            val chipWidth = (availableChipsWidth - chipGap * 2f) / 3f

            statCardRects[0].set(brandRect.right + actionGap, chipTop, brandRect.right + actionGap + chipWidth, chipTop + chipHeight)
            statCardRects[1].set(statCardRects[0].right + chipGap, chipTop, statCardRects[0].right + chipGap + chipWidth, chipTop + chipHeight)
            statCardRects[2].set(statCardRects[1].right + chipGap, chipTop, statCardRects[1].right + chipGap + chipWidth, chipTop + chipHeight)
            statCardRects[3].set(settingsButtonRect)

            statsRect.set(statCardRects[0].left, chipTop, statCardRects[2].right, chipTop + chipHeight)
            headerRect.set(
                contentRect.left,
                headerTop,
                contentRect.right,
                max(brandMottoRect.bottom, chipTop + chipHeight)
            )
        } else {
            // Narrow screen (<340dp width) - row 1 is logo + settings, row 2 is 3 chips
            headerRect.set(
                contentRect.left,
                headerTop,
                contentRect.right,
                max(brandMottoRect.bottom, settingsButtonRect.bottom)
            )
            val row2Top = headerRect.bottom + dp(6f * scaleFactor)
            val row2Height = dp(34f * scaleFactor)
            val chipGap = dp(6f * scaleFactor)
            val chipWidth = (contentRect.width() - chipGap * 2f) / 3f

            statCardRects[0].set(contentRect.left, row2Top, contentRect.left + chipWidth, row2Top + row2Height)
            statCardRects[1].set(statCardRects[0].right + chipGap, row2Top, statCardRects[0].right + chipGap + chipWidth, row2Top + row2Height)
            statCardRects[2].set(statCardRects[1].right + chipGap, row2Top, statCardRects[1].right + chipGap + chipWidth, row2Top + row2Height)
            statCardRects[3].set(settingsButtonRect)

            statsRect.set(contentRect.left, row2Top, contentRect.right, row2Top + row2Height)
        }

        // 2. Lower Navigation & Play CTA (Bottom-Up layout with Banner)
        val footerHeight = dp(14f * scaleFactor)
        val footerBottom = screenHeight - dp(10f * scaleFactor)
        footerRect.set(contentRect.left, footerBottom - footerHeight, contentRect.right, footerBottom)

        // 3 Cards Row & 4th Banner Card: Expanded and prominent matching reference
        val maxNavWidth = if (layoutMode == LayoutMode.TABLET) {
            min(contentRect.width() * 0.94f, dp(640f))
        } else {
            contentRect.width() * 0.94f
        }
        val navRowLeft = contentRect.centerX() - maxNavWidth * 0.5f
        val navRowRight = navRowLeft + maxNavWidth
        val navGap = dp(10f * scaleFactor)
        val singleCardWidth = (maxNavWidth - navGap * 2f) / 3f

        // Cards intrinsic ratio from reference: 285w x 305h => 1.07f height multiplier
        val minCardH = dp(60f * scaleFactor)
        val maxCardH = maxOf(screenHeight * 0.24f, minCardH)
        val navCardHeight = (singleCardWidth * 1.07f).coerceIn(minCardH, maxCardH)

        // Banner card: intrinsic ratio 870w x 100h => ~8.7:1
        val minBannerH = dp(32f * scaleFactor)
        val maxBannerH = maxOf(dp(68f * scaleFactor), minBannerH)
        val bannerHeight = (maxNavWidth / 8.7f).coerceIn(minBannerH, maxBannerH)
        val bannerBottom = footerRect.top - dp(8f * scaleFactor)
        bannerCardRect.set(navRowLeft, bannerBottom - bannerHeight, navRowRight, bannerBottom)

        val navRowBottom = bannerCardRect.top - dp(10f * scaleFactor)
        val navRowTop = navRowBottom - navCardHeight

        skinsCardRect.set(navRowLeft, navRowTop, navRowLeft + singleCardWidth, navRowBottom)
        missionsCardRect.set(skinsCardRect.right + navGap, navRowTop, skinsCardRect.right + navGap + singleCardWidth, navRowBottom)
        leaderboardCardRect.set(missionsCardRect.right + navGap, navRowTop, navRowRight, navRowBottom)

        vaultCardRect.set(missionsCardRect)
        collectionCardRect.set(skinsCardRect)
        leaderboardsCardRect.set(leaderboardCardRect)

        // 3. Play CTA Button (Above 3 cards)
        val playGap = dp(if (heightDp < 700f) 8f else 12f) * scaleFactor
        val playBottom = navRowTop - playGap
        val playWidth = contentRect.width() * 0.90f
        // Exact play chassis aspect ratio from design: 3.25f
        val playHeight = playWidth / 3.25f
        val playLeft = contentRect.centerX() - playWidth * 0.5f
        playCtaRect.set(playLeft, playBottom - playHeight, playLeft + playWidth, playBottom)

        // 4. Hero Stage (3D Smooth Pedestal Platform & Brainball Mascot)
        val heroTop = max(headerRect.bottom, statsRect.bottom) + dp(6f * scaleFactor)
        val heroBottom = playCtaRect.top + playHeight * 0.20f
        val availableHeroH = (heroBottom - heroTop).coerceAtLeast(dp(120f))
        heroRect.set(contentRect.left, heroTop, contentRect.right, heroBottom)

        val heroCx = contentRect.centerX()
        val heroCy = heroRect.centerY()

        val maxMascotW = min(
            contentRect.width() * 0.78f,
            dp(580f)
        )
        // 4a. Smooth 3D Cyber Pedestal Platform (~5.1:1 aspect ratio)
        val platformWidth = playWidth * 0.92f
        val platformHeight = platformWidth / 5.1f
        val portalPlayGap = dp(10f)
        val platformBottom = playCtaRect.top - portalPlayGap
        val platformTop = platformBottom - platformHeight
        platformRect.set(
            heroCx - platformWidth * 0.5f,
            platformTop,
            heroCx + platformWidth * 0.5f,
            platformBottom
        )
        portalRect.set(platformRect)
        portalFrontRect.set(platformRect)

        // 4b. Hero Mascot (brainball_main - enters ~10-15% into platform glow)
        val mascotBottom = platformTop + platformHeight * 0.13f
        val heroSpan = (mascotBottom - heroTop).coerceAtLeast(dp(100f))
        val maxMascotHFromSpan = heroSpan * 0.96f
        val mascotHeight = min(maxMascotW / 1.161f, maxMascotHFromSpan)
        val mascotWidth = mascotHeight * 1.161f
        val mascotTop = mascotBottom - mascotHeight

        characterRect.set(
            heroCx - mascotWidth * 0.5f,
            mascotTop,
            heroCx + mascotWidth * 0.5f,
            mascotBottom
        )
        heroMascotRect.set(characterRect)

        // Portal Beam (radiating vertically behind mascot)
        val beamW = platformWidth * 0.78f
        val beamH = beamW * 1.15f
        portalBeamRect.set(
            heroCx - beamW * 0.5f,
            platformTop - beamH * 0.50f,
            heroCx + beamW * 0.5f,
            platformTop + beamH * 0.50f
        )

        // Celestial Decor
        val minDim = min(screenWidth, screenHeight)
        val planetSize = minDim * (if (layoutMode == LayoutMode.TABLET) 0.28f else 0.38f)
        planetBlueRect.set(
            -planetSize * 0.35f,
            heroCy - planetSize * 0.5f,
            planetSize * 0.65f,
            heroCy + planetSize * 0.5f
        )
        planetPinkRect.set(
            screenWidth - planetSize * 0.68f,
            headerTop + dp(6f),
            screenWidth + planetSize * 0.32f,
            headerTop + dp(6f) + planetSize
        )

        val asteroidW = planetSize * 0.55f
        asteroidLeftRect.set(
            contentRect.left - asteroidW * 0.25f,
            navRowTop - asteroidW * 0.6f,
            contentRect.left + asteroidW * 0.75f,
            navRowTop + asteroidW * 0.4f
        )
        asteroidRightRect.set(
            contentRect.right - asteroidW * 0.75f,
            navRowTop - asteroidW * 0.5f,
            contentRect.right + asteroidW * 0.25f,
            navRowTop + asteroidW * 0.5f
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

        bodyRect.set(contentRect.left, headerRect.bottom, contentRect.right, footerRect.top)
        heroStageRect.set(heroRect)
        navigationDeckRect.set(contentRect.left, skinsCardRect.top, contentRect.right, bannerCardRect.bottom)
    }

    private fun calculateLandscape(
        brandAspect: Float,
        portalAspect: Float,
        platformAspect: Float
    ) {
        val widthDp = pxToDp(screenWidth)
        val heightDp = pxToDp(screenHeight)
        val scaleFactor = (heightDp / 800f).coerceIn(0.75f, 1.25f)
        layoutMode = LayoutMode.MEDIUM

        // Safe insets & margins: full safe width with 24dp internal padding
        val safeLeft = dp(24f * scaleFactor)
        val safeRight = screenWidth - dp(24f * scaleFactor)
        val safeTop = dp(16f * scaleFactor)
        val safeBottom = screenHeight - dp(16f * scaleFactor)

        contentRect.set(safeLeft, 0f, safeRight, screenHeight)

        // 1. Full-width Header
        val headerTop = safeTop
        val actionButtonSize = dp(MIN_TOUCH_TARGET_DP * scaleFactor)
            .coerceIn(dp(MIN_TOUCH_TARGET_DP), dp(MIN_TOUCH_TARGET_DP + 6f))
        val actionGap = dp(10f * scaleFactor)

        // Left Logo
        var logoWidth = dp(210f * scaleFactor).coerceIn(dp(170f), dp(240f))
        val logoHeight = logoWidth / brandAspect.coerceAtLeast(0.1f)

        // Right Controls: Settings button
        settingsButtonRect.set(
            safeRight - actionButtonSize,
            headerTop,
            safeRight,
            headerTop + actionButtonSize
        )
        soundButtonRect.set(settingsButtonRect)

        // Stat Chips
        val targetCoinsW = dp(100f * scaleFactor)
        val targetLevelW = dp(84f * scaleFactor)
        val targetStreakW = dp(92f * scaleFactor)
        var chipGap = dp(8f * scaleFactor)

        // Compact Header Degradation
        val availableHeaderSpace = (settingsButtonRect.left - actionGap) - (safeLeft + logoWidth + actionGap)
        val requiredChipsSpace = targetCoinsW + targetLevelW + targetStreakW + chipGap * 2f

        val (actualStreakW, actualLevelW, actualCoinsW) = if (availableHeaderSpace < requiredChipsSpace) {
            logoWidth = dp(175f * scaleFactor).coerceIn(dp(160f), dp(190f))
            chipGap = dp(6f * scaleFactor)
            val compactChipW = ((availableHeaderSpace - chipGap * 2f) / 3f).coerceIn(dp(54f), dp(80f))
            Triple(compactChipW, compactChipW, compactChipW)
        } else {
            Triple(targetStreakW, targetLevelW, targetCoinsW)
        }

        val coinsRight = settingsButtonRect.left - actionGap
        statCardRects[2].set(coinsRight - actualCoinsW, headerTop, coinsRight, headerTop + actionButtonSize)

        val levelRight = statCardRects[2].left - chipGap
        statCardRects[1].set(levelRight - actualLevelW, headerTop, levelRight, headerTop + actionButtonSize)

        val streakRight = statCardRects[1].left - chipGap
        statCardRects[0].set(streakRight - actualStreakW, headerTop, streakRight, headerTop + actionButtonSize)

        statsRect.set(statCardRects[0].left, headerTop, statCardRects[2].right, headerTop + actionButtonSize)

        brandRect.set(safeLeft, headerTop, safeLeft + logoWidth, headerTop + logoHeight)

        // 2. Body Viewport
        val headerBottom = max(brandRect.bottom, settingsButtonRect.bottom) + dp(12f * scaleFactor)
        headerRect.set(safeLeft, headerTop, safeRight, max(brandRect.bottom, settingsButtonRect.bottom))

        bodyRect.set(safeLeft, headerBottom, safeRight, safeBottom)
        val bodyWidth = bodyRect.width().coerceAtLeast(1f)
        val bodyHeight = bodyRect.height().coerceAtLeast(1f)

        // 3. Stage Gutter & 62/38 Split
        val stageGap = dp(24f * scaleFactor)
        val heroWidth = (bodyWidth - stageGap) * 0.62f
        val deckShiftX = screenWidth * 0.025f
        val deckLeft = bodyRect.left + heroWidth + stageGap - deckShiftX
        val deckWidth = bodyRect.right - deckLeft
        val deckWidthDp = pxToDp(deckWidth)
        val availableWidthDp = pxToDp(safeRight - safeLeft)
        val availableHeightDp = pxToDp(safeBottom - safeTop)

        landscapeClass = when {
            availableHeightDp < 500f || availableWidthDp < 840f -> LandscapeClass.COMPACT
            deckWidthDp >= 600f -> LandscapeClass.WIDE
            else -> LandscapeClass.STANDARD
        }

        // Show motto only on Wide
        if (landscapeClass == LandscapeClass.WIDE && heightDp >= 650f) {
            brandMottoRect.set(safeLeft, brandRect.bottom + dp(1f), safeLeft + logoWidth, brandRect.bottom + dp(13f * scaleFactor))
        } else {
            brandMottoRect.set(safeLeft, brandRect.bottom, safeLeft + logoWidth, brandRect.bottom)
        }

        showPlaySubtitle = landscapeClass != LandscapeClass.COMPACT
        showCardSubtitles = landscapeClass != LandscapeClass.COMPACT
        reduceDecor = landscapeClass == LandscapeClass.COMPACT

        heroStageRect.set(bodyRect.left, bodyRect.top, bodyRect.left + heroWidth, bodyRect.bottom)
        navigationDeckRect.set(deckLeft, bodyRect.top, bodyRect.right, bodyRect.bottom)
        heroRect.set(heroStageRect)

        // 4. Hero Stage (Left ~62%)
        val heroCx = heroStageRect.centerX()

        // Brainball Mascot: occupies 44-58% of body height
        val mascotHeight = min(
            bodyHeight * 0.58f,
            heroWidth * 0.60f / 1.161f
        ).coerceAtLeast(bodyHeight * 0.44f)
        val mascotWidth = mascotHeight * 1.161f

        // Platform / Portal disc: aspect ~5.1:1
        val platformWidth = heroWidth * 0.72f
        val platformHeight = platformWidth / 5.1f
        val discBelowMascot = platformHeight * 0.87f

        // PLAY NOW CTA: ~88% of hero width, chunky ~3.25:1
        val playWidth = heroWidth * 0.88f
        val playHeight = playWidth / 3.25f

        // Separation: 10dp (Hero Assembly)
        val portalPlayGap = dp(10f)

        // Hero Stack Vertical Layout
        val heroStackHeight = mascotHeight + discBelowMascot + portalPlayGap + playHeight
        val availableHeroH = heroStageRect.height()
        val freeHeroSpace = (availableHeroH - heroStackHeight).coerceAtLeast(0f)

        // Brainball ~3-5% higher in Landscape
        var mascotTop = heroStageRect.top + min(freeHeroSpace * 0.35f, availableHeroH * 0.08f).coerceAtLeast(dp(4f)) - availableHeroH * 0.04f
        mascotTop = mascotTop.coerceAtLeast(heroStageRect.top + dp(2f))
        if (mascotTop + heroStackHeight > heroStageRect.bottom) {
            mascotTop = (heroStageRect.bottom - heroStackHeight).coerceAtLeast(heroStageRect.top)
        }
        val mascotBottom = mascotTop + mascotHeight
        characterRect.set(heroCx - mascotWidth * 0.5f, mascotTop, heroCx + mascotWidth * 0.5f, mascotBottom)
        heroMascotRect.set(characterRect)

        // 3D Pedestal Disc Platform beneath mascot (enters ~10-15% into platform glow)
        val platformTop = mascotBottom - platformHeight * 0.13f
        val platformBottom = platformTop + platformHeight
        platformRect.set(heroCx - platformWidth * 0.5f, platformTop, heroCx + platformWidth * 0.5f, platformBottom)
        portalRect.set(platformRect)
        portalFrontRect.set(platformRect)

        // PLAY CTA directly below platform with 10dp separation, shifted ~2% right
        val playShiftX = screenWidth * 0.02f
        val playTop = platformBottom + portalPlayGap
        val playBottom = min(playTop + playHeight, heroStageRect.bottom)
        playCtaRect.set(heroCx - playWidth * 0.5f + playShiftX, playTop, heroCx + playWidth * 0.5f + playShiftX, playBottom)

        // Portal Beam radiating behind mascot
        val beamW = platformWidth * 0.78f
        val beamH = beamW * 1.15f
        portalBeamRect.set(
            heroCx - beamW * 0.5f,
            platformTop - beamH * 0.45f,
            heroCx + beamW * 0.5f,
            platformTop + beamH * 0.55f
        )

        // 5. Navigation Deck (Right ~38%)
        val navGap = dp(16f * scaleFactor)
        val minThreeUpCardWidth = dp(180f)
        val canUseThreeUp = deckWidth >= minThreeUpCardWidth * 3f + navGap * 2f

        if (landscapeClass == LandscapeClass.WIDE && canUseThreeUp) {
            // Row 1: 3 cards side-by-side
            val cardWidth = (deckWidth - navGap * 2f) / 3f
            val cardHeight = (cardWidth * 1.07f).coerceAtMost(bodyHeight * 0.65f)
            val rowTop = navigationDeckRect.top + (navigationDeckRect.height() - cardHeight) * 0.40f
            skinsCardRect.set(deckLeft, rowTop, deckLeft + cardWidth, rowTop + cardHeight)
            missionsCardRect.set(skinsCardRect.right + navGap, rowTop, skinsCardRect.right + navGap + cardWidth, rowTop + cardHeight)
            leaderboardCardRect.set(missionsCardRect.right + navGap, rowTop, deckLeft + deckWidth, rowTop + cardHeight)
        } else {
            // 2 + 1 Cards (Default for Standard & Compact)
            val cardWidth = (deckWidth - navGap) / 2f
            val row2Width = deckWidth
            val row2Height = (deckWidth / 3.15f).coerceIn(dp(96f), dp(150f))
            val availableDeckH = (navigationDeckRect.height() - navGap).coerceAtLeast(dp(150f))
            val cardHeight = (cardWidth * 0.98f).coerceAtMost(availableDeckH - row2Height - dp(4f))

            val totalDeckH = cardHeight + navGap + row2Height
            // Align Row 1 with upper-mid body of Brainball
            val idealDeckTop = mascotTop + mascotHeight * 0.12f
            val safeMaxDeckTop = max(navigationDeckRect.top, navigationDeckRect.bottom - totalDeckH)
            val deckStartTop = idealDeckTop.coerceIn(navigationDeckRect.top, safeMaxDeckTop)

            // Row 1: Skins + Missions
            skinsCardRect.set(deckLeft, deckStartTop, deckLeft + cardWidth, deckStartTop + cardHeight)
            missionsCardRect.set(skinsCardRect.right + navGap, deckStartTop, deckLeft + deckWidth, deckStartTop + cardHeight)

            // Row 2: Leaderboard (full width)
            val row2Top = skinsCardRect.bottom + navGap
            leaderboardCardRect.set(deckLeft, row2Top, deckLeft + deckWidth, row2Top + row2Height)
        }

        // Aliases
        vaultCardRect.set(missionsCardRect)
        collectionCardRect.set(skinsCardRect)
        leaderboardsCardRect.set(leaderboardCardRect)

        // Omitted elements in landscape
        bannerCardRect.setEmpty()
        footerRect.setEmpty()
        riftStatusRect.setEmpty()

        // Celestial Decor
        val blueSize = screenWidth * 0.17f
        planetBlueRect.set(
            -blueSize * 0.38f,
            -blueSize * 0.08f,
            blueSize * 0.62f,
            blueSize * 0.92f
        )

        val pinkSize = screenWidth * 0.135f
        planetPinkRect.set(
            screenWidth - pinkSize * 0.72f,
            headerTop + pinkSize * 0.08f,
            screenWidth + pinkSize * 0.28f,
            headerTop + pinkSize * 1.08f
        )
    }

    fun brandTop(scaleFactor: Float = (pxToDp(screenHeight) / 800f).coerceIn(0.72f, 1.25f)): Float =
        dp(16f * scaleFactor)

    companion object {
        const val BRAND_ASPECT = 440f / 110f

        /**
         * Platform minimum interactive size. Short phones used to shrink the header gear and
         * the stat chips to ~27dp because the header scaled with `heightDp / 800f`.
         */
        const val MIN_TOUCH_TARGET_DP = 48f
    }

    private fun calculatePortraitContentRect(widthDp: Float) {
        val fraction = when {
            widthDp <= 480f -> 0.94f
            widthDp <= 840f -> 0.90f
            else -> 0.82f
        }

        val maxWidthPx = dp(
            when {
                widthDp <= 480f -> 460f
                widthDp <= 840f -> 720f
                else -> 680f // Tablet portrait pillarbox constraint: max 680dp
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
