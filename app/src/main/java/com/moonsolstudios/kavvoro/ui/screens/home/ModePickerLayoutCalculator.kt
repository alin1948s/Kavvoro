package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.RectF

/**
 * Bounds computed by [ModePickerLayoutCalculator].
 */
data class ModePickerBounds(
    val classicCard: LayoutRect = LayoutRect(),
    val chaosCard: LayoutRect = LayoutRect(),
    val classicContinueButton: LayoutRect = LayoutRect(),
    val classicNewButton: LayoutRect = LayoutRect(),
    val chaosContinueButton: LayoutRect = LayoutRect(),
    val chaosNewButton: LayoutRect = LayoutRect(),
    val chaosStartButton: LayoutRect = LayoutRect(),
    val continueButton: LayoutRect = LayoutRect(),
    val backButton: LayoutRect = LayoutRect()
)

/**
 * Calculates responsive layout geometry for the Mode Selection screen.
 * Positions Classic and Quantum Chaos cards, action buttons, and back button.
 */
object ModePickerLayoutCalculator {

    fun calculate(
        viewWidth: Float,
        viewHeight: Float,
        uiDensity: Float,
        left: Float,
        contentWidth: Float,
        top: Float,
        classicProgress: Int,
        chaosProgress: Int
    ): ModePickerBounds {
        val bounds = ModePickerBounds()
        val dp = { v: Float -> v * uiDensity }
        val widthDp = viewWidth / uiDensity
        val heightDp = viewHeight / uiDensity
        val compact = widthDp <= 480f
        val short = heightDp < 620f
        val isTablet = widthDp >= 600f

        val cardW = minOf(contentWidth, dp(580f))
        val side = left + (contentWidth - cardW) * 0.5f
        val right = side + cardW
        val gap = (if (short) 10f else if (compact) 14f else if (isTablet) 20f else 18f) * dp(1f)
        val cardHeight = (if (short) 146f else if (compact) 185f else if (isTablet) 235f else 210f) * dp(1f)
        val startY = if (isTablet) (viewHeight * 0.155f) else top + (if (short) 68f else if (compact) 85f else 100f) * dp(1f)

        bounds.classicCard.set(side, startY, right, startY + cardHeight)
        bounds.chaosCard.set(side, bounds.classicCard.bottom + gap, right, bounds.classicCard.bottom + gap + cardHeight)

        val mascotAreaW = minOf(cardW * 0.36f, cardHeight * 0.88f)
        val bayLeft = bounds.classicCard.left + (if (short) 18f else if (compact) 24f else 30f) * dp(1f)
        val bayRight = bounds.classicCard.right - mascotAreaW - (if (short) 8f else 14f) * dp(1f)
        val bayW = maxOf(dp(120f), bayRight - bayLeft)
        val btnH = (if (short) 30f else if (compact) 36f else 42f) * dp(1f)
        val btnY = bounds.classicCard.bottom - btnH - (if (short) 12f else 18f) * dp(1f)
        val btnGap = dp(10f)

        if (classicProgress > 1) {
            val continueW = (bayW - btnGap) * 0.58f
            val newW = (bayW - btnGap) * 0.42f
            bounds.classicContinueButton.set(bayLeft, btnY, bayLeft + continueW, btnY + btnH)
            bounds.classicNewButton.set(bounds.classicContinueButton.right + btnGap, btnY, bounds.classicContinueButton.right + btnGap + newW, btnY + btnH)
        } else {
            bounds.classicContinueButton.setEmpty()
            bounds.classicNewButton.set(bayLeft, btnY, bayLeft + bayW, btnY + btnH)
        }

        val chaosBtnY = bounds.chaosCard.bottom - btnH - (if (short) 12f else 18f) * dp(1f)
        val chaosBayLeft = bounds.chaosCard.left + (if (short) 18f else if (compact) 24f else 30f) * dp(1f)
        val chaosBayRight = bounds.chaosCard.right - mascotAreaW - (if (short) 8f else 14f) * dp(1f)
        val chaosBayW = maxOf(dp(120f), chaosBayRight - chaosBayLeft)

        if (chaosProgress > 1) {
            val chaosContinueW = (chaosBayW - btnGap) * 0.58f
            val chaosNewW = (chaosBayW - btnGap) * 0.42f
            bounds.chaosContinueButton.set(chaosBayLeft, chaosBtnY, chaosBayLeft + chaosContinueW, chaosBtnY + btnH)
            bounds.chaosNewButton.set(bounds.chaosContinueButton.right + btnGap, chaosBtnY, bounds.chaosContinueButton.right + btnGap + chaosNewW, chaosBtnY + btnH)
            bounds.chaosStartButton.setEmpty()
        } else {
            bounds.chaosContinueButton.setEmpty()
            bounds.chaosNewButton.set(chaosBayLeft, chaosBtnY, chaosBayLeft + chaosBayW, chaosBtnY + btnH)
            bounds.chaosStartButton.set(bounds.chaosNewButton)
        }

        val backBtnW = minOf(cardW, dp(360f))
        val backBtnH = (if (short) 38f else 48f) * dp(1f)
        val backBtnX = left + (contentWidth - backBtnW) * 0.5f
        val backBtnY = bounds.chaosCard.bottom + (if (short) 12f else 18f) * dp(1f)
        bounds.continueButton.set(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH)
        bounds.backButton.set(bounds.continueButton)
        return bounds
    }

    fun layoutPlayModeScreen(
        viewWidth: Float,
        viewHeight: Float,
        uiDensity: Float,
        left: Float,
        contentWidth: Float,
        top: Float,
        classicProgress: Int,
        chaosProgress: Int,
        menuClassicCard: RectF,
        menuChaosCard: RectF,
        menuClassicContinueButton: RectF,
        menuClassicNewButton: RectF,
        menuChaosContinueButton: RectF,
        menuChaosNewButton: RectF,
        menuChaosStartButton: RectF,
        menuContinueButton: RectF,
        menuBackButton: RectF
    ) {
        val bounds = calculate(
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            uiDensity = uiDensity,
            left = left,
            contentWidth = contentWidth,
            top = top,
            classicProgress = classicProgress,
            chaosProgress = chaosProgress
        )

        bounds.classicCard.toRectF(menuClassicCard)
        bounds.chaosCard.toRectF(menuChaosCard)
        bounds.classicContinueButton.toRectF(menuClassicContinueButton)
        bounds.classicNewButton.toRectF(menuClassicNewButton)
        bounds.chaosContinueButton.toRectF(menuChaosContinueButton)
        bounds.chaosNewButton.toRectF(menuChaosNewButton)
        bounds.chaosStartButton.toRectF(menuChaosStartButton)
        bounds.continueButton.toRectF(menuContinueButton)
        bounds.backButton.toRectF(menuBackButton)
    }
}
