package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.model.DailyRiftButton
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import kotlin.math.abs
import kotlin.math.max

interface HomeMenuActionListener {
    fun onMenuButtonSelected(button: MenuButton)
    fun onDailyRiftClaim()
    fun onDailyRiftOpenCollection()
    fun onDailyRiftDismissed()
    fun performHaptic(feedbackConstant: Int)
}

/**
 * Controller managing user touch input, mascot ball dragging physics,
 * button hit-testing, and Daily Rift modal interactions on the Home Menu.
 */
class HomeMenuTouchController(
    private val listener: HomeMenuActionListener
) {
    var activeMenuButton: MenuButton = MenuButton.NONE
    var activeDailyRiftButton: DailyRiftButton = DailyRiftButton.NONE

    // Mascot Ball Drag & Spring Physics
    var menuBallOffsetX: Float = 0f
    var menuBallOffsetY: Float = 0f
    var menuBallVelocityX: Float = 0f
    var menuBallVelocityY: Float = 0f
    var menuBallDragging: Boolean = false
    private var menuBallTouchDx: Float = 0f
    private var menuBallTouchDy: Float = 0f
    private var menuBallLastDragTime: Long = 0L

    var menuPreviewCenterX: Float = 0f
    var menuPreviewCenterY: Float = 0f
    var menuPreviewRadius: Float = 0f
    val menuPreviewBounds = RectF()

    val dailyRiftCardBounds = RectF()
    val dailyRiftClaimButtonRect = RectF()
    val dailyRiftActionButtonRect = RectF()
    val dailyRiftCloseButtonRect = RectF()

    val menuStartButton = RectF()
    val menuContinueButton = RectF()
    val menuBackButton = RectF()
    val menuCollectionButton = RectF()
    val menuLeaderboardButton = RectF()
    val menuVaultButton = RectF()
    val menuBannerButton = RectF()
    val menuPrivacyButton = RectF()
    val menuSfxButton = RectF()
    val characterRect = RectF()
    val menuClassicCard = RectF()
    val menuChaosCard = RectF()
    val menuClassicContinueButton = RectF()
    val menuClassicNewButton = RectF()
    val menuChaosContinueButton = RectF()
    val menuChaosNewButton = RectF()
    val menuChaosStartButton = RectF()

    fun syncHomeLayoutRects(homeLayoutCalculator: HomeLayoutCalculator) {
        homeLayoutCalculator.settingsButtonRect.toRectF(menuPrivacyButton)
        homeLayoutCalculator.soundButtonRect.toRectF(menuSfxButton)
        homeLayoutCalculator.playCtaRect.toRectF(menuStartButton)
        homeLayoutCalculator.leaderboardsCardRect.toRectF(menuLeaderboardButton)
        homeLayoutCalculator.vaultCardRect.toRectF(menuVaultButton)
        homeLayoutCalculator.collectionCardRect.toRectF(menuCollectionButton)
        homeLayoutCalculator.bannerCardRect.toRectF(menuBannerButton)
        homeLayoutCalculator.characterRect.toRectF(characterRect)
    }

    fun updatePhysics(dt: Float, selectedMode: GameMode, viewWidth: Int, viewHeight: Int, dp: Float) {
        if (menuBallDragging) return
        val spring = if (selectedMode == GameMode.CHAOS) 8.8f else 6.8f
        val damping = if (selectedMode == GameMode.CHAOS) 0.8f else 0.84f
        menuBallVelocityX += -menuBallOffsetX * spring * dt
        menuBallVelocityY += -menuBallOffsetY * spring * dt
        menuBallVelocityX *= (1f - dt * (1f - damping) * 18f).coerceIn(0.42f, 1f)
        menuBallVelocityY *= (1f - dt * (1f - damping) * 18f).coerceIn(0.42f, 1f)
        val next = clampMenuBallOffset(
            menuBallOffsetX + menuBallVelocityX * dt,
            menuBallOffsetY + menuBallVelocityY * dt,
            viewWidth,
            viewHeight,
            dp
        )
        if (next.x != menuBallOffsetX + menuBallVelocityX * dt) menuBallVelocityX *= -0.35f
        if (next.y != menuBallOffsetY + menuBallVelocityY * dt) menuBallVelocityY *= -0.35f
        menuBallOffsetX = next.x
        menuBallOffsetY = next.y
        if (abs(menuBallOffsetX) < 0.4f && abs(menuBallVelocityX) < 4f) {
            menuBallOffsetX = 0f
            menuBallVelocityX = 0f
        }
        if (abs(menuBallOffsetY) < 0.4f && abs(menuBallVelocityY) < 4f) {
            menuBallOffsetY = 0f
            menuBallVelocityY = 0f
        }
    }

    private fun clampMenuBallOffset(
        offsetX: Float,
        offsetY: Float,
        viewWidth: Int,
        viewHeight: Int,
        dp: Float
    ): Point2 {
        if (viewWidth <= 0 || viewHeight <= 0) return Point2(offsetX, offsetY)
        val safeRadius = 42f * dp
        val minX = safeRadius
        val maxX = max(minX, viewWidth - safeRadius)
        val minY = 58f * dp
        val maxY = max(minY, viewHeight - 58f * dp)
        val targetX = menuPreviewCenterX + offsetX
        val targetY = menuPreviewCenterY + offsetY
        return Point2(
            targetX.coerceIn(minX, maxX) - menuPreviewCenterX,
            targetY.coerceIn(minY, maxY) - menuPreviewCenterY
        )
    }

    private fun menuPreviewBallHit(x: Float, y: Float, dp: Float): Boolean {
        val ballX = menuPreviewCenterX + menuBallOffsetX
        val ballY = menuPreviewCenterY + menuBallOffsetY
        val radius = 60f * dp
        val dx = x - ballX
        val dy = y - ballY
        return dx * dx + dy * dy <= radius * radius
    }

    fun handleTouchEvent(
        event: MotionEvent,
        isDailyRiftModalVisible: Boolean,
        dailyClaimed: Boolean,
        viewWidth: Int,
        viewHeight: Int,
        dp: Float,
        buttonAt: (Float, Float) -> MenuButton
    ): Boolean {
        if (isDailyRiftModalVisible) {
            return handleDailyRiftTouch(event, dailyClaimed)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pressedButton = buttonAt(event.x, event.y)
                if (pressedButton != MenuButton.NONE) {
                    activeMenuButton = pressedButton
                } else {
                    activeMenuButton = MenuButton.NONE
                    menuBallDragging = true
                    menuBallVelocityX = 0f
                    menuBallVelocityY = 0f
                    val ballX = menuPreviewCenterX + menuBallOffsetX
                    val ballY = menuPreviewCenterY + menuBallOffsetY
                    if (menuPreviewBallHit(event.x, event.y, dp)) {
                        menuBallTouchDx = event.x - ballX
                        menuBallTouchDy = event.y - ballY
                    } else {
                        menuBallTouchDx = 0f
                        menuBallTouchDy = 0f
                    }
                    menuBallLastDragTime = event.eventTime
                    listener.performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (menuBallDragging) {
                    dragMenuBall(event, viewWidth, viewHeight, dp)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (menuBallDragging) {
                    dragMenuBall(event, viewWidth, viewHeight, dp)
                    menuBallDragging = false
                    listener.performHaptic(HapticFeedbackConstants.CLOCK_TICK)
                    return true
                }
                val releasedButton = activeMenuButton
                activeMenuButton = MenuButton.NONE
                if (releasedButton != MenuButton.NONE && buttonAt(event.x, event.y) == releasedButton) {
                    listener.onMenuButtonSelected(releasedButton)
                }
                return true
            }
        }
        return false
    }

    fun handleTouchEvent(
        event: MotionEvent,
        isDailyRiftModalVisible: Boolean,
        dailyClaimed: Boolean,
        menuState: MenuState,
        calculator: HomeLayoutCalculator,
        modePickerButtons: Map<MenuButton, RectF>,
        viewWidth: Int,
        viewHeight: Int,
        dp: Float
    ): Boolean {
        return handleTouchEvent(
            event = event,
            isDailyRiftModalVisible = isDailyRiftModalVisible,
            dailyClaimed = dailyClaimed,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            dp = dp,
            buttonAt = { x, y -> hitTestMenuButton(x, y, menuState, calculator, modePickerButtons) }
        )
    }

    private fun dragMenuBall(event: MotionEvent, viewWidth: Int, viewHeight: Int, dp: Float) {
        val previousX = menuBallOffsetX
        val previousY = menuBallOffsetY
        val targetOffsetX = event.x - menuBallTouchDx - menuPreviewCenterX
        val targetOffsetY = event.y - menuBallTouchDy - menuPreviewCenterY
        val clamped = clampMenuBallOffset(targetOffsetX, targetOffsetY, viewWidth, viewHeight, dp)
        menuBallOffsetX = clamped.x
        menuBallOffsetY = clamped.y

        val elapsedMs = (event.eventTime - menuBallLastDragTime).coerceAtLeast(1L)
        val dt = (elapsedMs / 1000f).coerceIn(0.008f, 0.08f)
        menuBallVelocityX = ((menuBallOffsetX - previousX) / dt).coerceIn(-2400f, 2400f)
        menuBallVelocityY = ((menuBallOffsetY - previousY) / dt).coerceIn(-2400f, 2400f)
        menuBallLastDragTime = event.eventTime
    }

    private fun handleDailyRiftTouch(event: MotionEvent, dailyClaimed: Boolean): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeDailyRiftButton = when {
                    dailyRiftClaimButtonRect.contains(event.x, event.y) -> DailyRiftButton.CLAIM
                    dailyRiftActionButtonRect.contains(event.x, event.y) -> DailyRiftButton.ACTION
                    dailyRiftCloseButtonRect.contains(event.x, event.y) -> DailyRiftButton.CLOSE
                    else -> DailyRiftButton.NONE
                }
                if (activeDailyRiftButton != DailyRiftButton.NONE) {
                    listener.performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            }
            MotionEvent.ACTION_UP -> {
                val clicked = activeDailyRiftButton
                activeDailyRiftButton = DailyRiftButton.NONE
                when {
                    clicked == DailyRiftButton.CLAIM && dailyRiftClaimButtonRect.contains(event.x, event.y) -> {
                        if (!dailyClaimed) {
                            listener.onDailyRiftClaim()
                        } else {
                            listener.onDailyRiftDismissed()
                            listener.performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    }
                    clicked == DailyRiftButton.ACTION && dailyRiftActionButtonRect.contains(event.x, event.y) -> {
                        listener.onDailyRiftOpenCollection()
                    }
                    clicked == DailyRiftButton.CLOSE && dailyRiftCloseButtonRect.contains(event.x, event.y) -> {
                        listener.onDailyRiftDismissed()
                        listener.performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    clicked == DailyRiftButton.NONE && !dailyRiftCardBounds.contains(event.x, event.y) -> {
                        listener.onDailyRiftDismissed()
                        listener.performHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                activeDailyRiftButton = DailyRiftButton.NONE
            }
        }
        return true
    }

    fun hitTestMenuButton(
        x: Float,
        y: Float,
        menuState: MenuState,
        calculator: HomeLayoutCalculator,
        modePickerButtons: Map<MenuButton, RectF>
    ): MenuButton {
        if (calculator.settingsTouchRect.contains(x, y)) return MenuButton.SETTINGS
        if (calculator.soundButtonRect.contains(x, y)) return MenuButton.SFX

        if (menuState == MenuState.MODES) {
            if (calculator.playCtaRect.contains(x, y)) return MenuButton.PLAY
            if (calculator.leaderboardsCardRect.contains(x, y)) return MenuButton.LEADERBOARDS
            if (calculator.vaultCardRect.contains(x, y)) return MenuButton.DAILY_RIFT
            if (calculator.collectionCardRect.contains(x, y)) return MenuButton.COLLECTION
            if (calculator.bannerCardRect.contains(x, y)) return MenuButton.DAILY_RIFT
        } else {
            for ((button, rect) in modePickerButtons) {
                if (rect.contains(x, y)) return button
            }
        }
        return MenuButton.NONE
    }

    fun updatePreviewGeometry(
        menuState: MenuState,
        characterRect: RectF,
        menuActionStartButtonTop: Float,
        viewWidth: Int,
        viewHeight: Int,
        dp: Float,
        homeBgWidth: Float = 1600f,
        homeBgHeight: Float = 2560f
    ) {
        menuPreviewBounds.set(10f * dp, 76f * dp, viewWidth - 10f * dp, viewHeight - 78f * dp)
        if (menuState == MenuState.MODES) {
            if (characterRect.width() > 0f) {
                menuPreviewCenterX = characterRect.centerX()
                menuPreviewCenterY = characterRect.centerY()
                menuPreviewRadius = characterRect.width() * 0.5f
                return
            }
            val bgW = if (homeBgWidth > 0f) homeBgWidth else 1600f
            val bgH = if (homeBgHeight > 0f) homeBgHeight else 2560f
            val artAspect = bgW / bgH
            val viewportAspect = viewWidth.toFloat() / viewHeight.coerceAtLeast(1).toFloat()
            val (artWidth, artHeight) = if (viewportAspect > artAspect) {
                Pair(viewWidth.toFloat(), viewWidth.toFloat() / artAspect)
            } else {
                Pair(viewHeight.toFloat() * artAspect, viewHeight.toFloat())
            }
            val artLeft = (viewWidth - artWidth) * 0.5f
            val artTop = (viewHeight - artHeight) * 0.5f
            val coreArtWidth = artHeight * (1080f / 2400f)
            val charSize = coreArtWidth * 0.37f
            menuPreviewCenterX = artLeft + artWidth * 0.5f
            menuPreviewCenterY = artTop + artHeight * 0.362f
            menuPreviewRadius = charSize * 0.5f
            return
        }
        val previewTop = 188f * dp
        val previewBottom = menuActionStartButtonTop - 22f * dp
        val radius = kotlin.math.min(viewWidth * 0.2f, (previewBottom - previewTop) * 0.36f)
            .coerceIn(52f * dp, 78f * dp)
        menuPreviewCenterX = viewWidth * 0.5f
        menuPreviewCenterY = (previewTop + previewBottom) * 0.5f
        menuPreviewRadius = radius
    }

    private fun RectF.hits(x: Float, y: Float): Boolean =
        contains(x, y) || (right > left && bottom > top && x >= left && x < right && y >= top && y < bottom)

    fun menuButtonAt(
        x: Float,
        y: Float,
        menuState: MenuState,
        menuPrivacyButton: RectF = this.menuPrivacyButton,
        menuSfxButton: RectF = this.menuSfxButton,
        menuStartButton: RectF = this.menuStartButton,
        menuLeaderboardButton: RectF = this.menuLeaderboardButton,
        menuVaultButton: RectF = this.menuVaultButton,
        menuCollectionButton: RectF = this.menuCollectionButton,
        menuBannerButton: RectF = this.menuBannerButton,
        menuClassicContinueButton: RectF = this.menuClassicContinueButton,
        menuClassicNewButton: RectF = this.menuClassicNewButton,
        menuChaosContinueButton: RectF = this.menuChaosContinueButton,
        menuChaosNewButton: RectF = this.menuChaosNewButton,
        menuChaosStartButton: RectF = this.menuChaosStartButton,
        menuContinueButton: RectF = this.menuContinueButton,
        menuBackButton: RectF = this.menuBackButton,
        menuClassicCard: RectF = this.menuClassicCard,
        menuChaosCard: RectF = this.menuChaosCard
    ): MenuButton {
        if (menuPrivacyButton.hits(x, y)) return MenuButton.SETTINGS
        if (menuSfxButton.hits(x, y)) return MenuButton.SFX
        if (menuState == MenuState.MODES) {
            if (menuStartButton.hits(x, y)) return MenuButton.PLAY
            if (menuLeaderboardButton.hits(x, y)) return MenuButton.LEADERBOARDS
            if (menuVaultButton.hits(x, y)) return MenuButton.DAILY_RIFT
            if (menuCollectionButton.hits(x, y)) return MenuButton.COLLECTION
            if (menuBannerButton.hits(x, y)) return MenuButton.DAILY_RIFT
            if (menuClassicContinueButton.hits(x, y)) return MenuButton.CLASSIC_CONTINUE
            if (menuClassicNewButton.hits(x, y)) return MenuButton.CLASSIC_START
            if (menuChaosContinueButton.hits(x, y)) return MenuButton.CHAOS_CONTINUE
            if (menuChaosNewButton.hits(x, y)) return MenuButton.CHAOS_NEW
            if (menuChaosStartButton.hits(x, y)) return MenuButton.CHAOS_START
        } else {
            if (menuContinueButton.hits(x, y)) return MenuButton.BACK
            if (menuBackButton.hits(x, y)) return MenuButton.BACK
            if (menuClassicContinueButton.hits(x, y)) return MenuButton.CLASSIC_CONTINUE
            if (menuClassicNewButton.hits(x, y)) return MenuButton.CLASSIC_START
            if (menuChaosContinueButton.hits(x, y)) return MenuButton.CHAOS_CONTINUE
            if (menuChaosNewButton.hits(x, y)) return MenuButton.CHAOS_NEW
            if (menuChaosStartButton.hits(x, y)) return MenuButton.CHAOS_START
            if (menuClassicCard.hits(x, y)) return MenuButton.CLASSIC
            if (menuChaosCard.hits(x, y)) return MenuButton.CHAOS
        }
        return MenuButton.NONE
    }
}
