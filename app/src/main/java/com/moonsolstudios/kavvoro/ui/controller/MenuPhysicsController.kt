package com.moonsolstudios.kavvoro.ui.controller

import android.view.MotionEvent
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.model.GameMode
import kotlin.math.abs

data class MenuBallPhysicsState(
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var touchDx: Float = 0f,
    var touchDy: Float = 0f,
    var lastDragTime: Long = 0L,
    var isDragging: Boolean = false
)

object MenuPhysicsController {

    fun hitTestPreviewBall(
        x: Float,
        y: Float,
        centerX: Float,
        centerY: Float,
        offsetX: Float,
        offsetY: Float,
        radius: Float
    ): Boolean {
        val ballX = centerX + offsetX
        val ballY = centerY + offsetY
        val dx = x - ballX
        val dy = y - ballY
        return dx * dx + dy * dy <= radius * radius
    }

    fun clampOffset(
        offsetX: Float,
        offsetY: Float,
        centerX: Float,
        centerY: Float,
        safeRadius: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
    ): Point2 {
        val targetX = centerX + offsetX
        val targetY = centerY + offsetY
        return Point2(
            if (minX <= maxX) targetX.coerceIn(minX, maxX) - centerX else 0f,
            if (minY <= maxY) targetY.coerceIn(minY, maxY) - centerY else 0f
        )
    }

    fun drag(
        event: MotionEvent,
        state: MenuBallPhysicsState,
        centerX: Float,
        centerY: Float,
        safeRadius: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
    ) {
        val previousX = state.offsetX
        val previousY = state.offsetY
        val targetOffsetX = event.x - state.touchDx - centerX
        val targetOffsetY = event.y - state.touchDy - centerY
        val clamped = clampOffset(targetOffsetX, targetOffsetY, centerX, centerY, safeRadius, minX, maxX, minY, maxY)
        state.offsetX = clamped.x
        state.offsetY = clamped.y

        val elapsedMs = (event.eventTime - state.lastDragTime).coerceAtLeast(1L)
        val dt = (elapsedMs / 1000f).coerceIn(0.008f, 0.08f)
        state.velocityX = ((state.offsetX - previousX) / dt).coerceIn(-2400f, 2400f)
        state.velocityY = ((state.offsetY - previousY) / dt).coerceIn(-2400f, 2400f)
        state.lastDragTime = event.eventTime
    }

    fun update(
        dt: Float,
        state: MenuBallPhysicsState,
        selectedMode: GameMode,
        centerX: Float,
        centerY: Float,
        safeRadius: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
    ) {
        if (state.isDragging) return
        val spring = if (selectedMode == GameMode.CHAOS) 8.8f else 6.8f
        val damping = if (selectedMode == GameMode.CHAOS) 0.8f else 0.84f
        state.velocityX += -state.offsetX * spring * dt
        state.velocityY += -state.offsetY * spring * dt
        state.velocityX *= (1f - dt * (1f - damping) * 18f).coerceIn(0.42f, 1f)
        state.velocityY *= (1f - dt * (1f - damping) * 18f).coerceIn(0.42f, 1f)
        val next = clampOffset(
            state.offsetX + state.velocityX * dt,
            state.offsetY + state.velocityY * dt,
            centerX,
            centerY,
            safeRadius,
            minX,
            maxX,
            minY,
            maxY
        )
        if (next.x != state.offsetX + state.velocityX * dt) state.velocityX *= -0.35f
        if (next.y != state.offsetY + state.velocityY * dt) state.velocityY *= -0.35f
        state.offsetX = next.x
        state.offsetY = next.y
        if (abs(state.offsetX) < 0.4f && abs(state.velocityX) < 4f) {
            state.offsetX = 0f
            state.velocityX = 0f
        }
        if (abs(state.offsetY) < 0.4f && abs(state.velocityY) < 4f) {
            state.offsetY = 0f
            state.velocityY = 0f
        }
    }

    fun handleTouch(
        event: MotionEvent,
        layoutMenuButtons: () -> Unit,
        updateMenuPreviewGeometry: () -> Unit,
        menuButtonAt: (Float, Float) -> com.moonsolstudios.kavvoro.model.MenuButton,
        getActiveMenuButton: () -> com.moonsolstudios.kavvoro.model.MenuButton,
        setActiveMenuButton: (com.moonsolstudios.kavvoro.model.MenuButton) -> Unit,
        handleMenuButton: (com.moonsolstudios.kavvoro.model.MenuButton) -> Unit,
        state: MenuBallPhysicsState,
        centerX: Float,
        centerY: Float,
        safeRadius: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        performHapticFeedback: (Int) -> Unit
    ) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                layoutMenuButtons()
                updateMenuPreviewGeometry()
                val pressedButton = menuButtonAt(event.x, event.y)
                if (pressedButton != com.moonsolstudios.kavvoro.model.MenuButton.NONE) {
                    setActiveMenuButton(pressedButton)
                } else {
                    setActiveMenuButton(com.moonsolstudios.kavvoro.model.MenuButton.NONE)
                    state.velocityX = 0f
                    state.velocityY = 0f
                    val ballX = centerX + state.offsetX
                    val ballY = centerY + state.offsetY
                    if (hitTestPreviewBall(event.x, event.y, centerX, centerY, state.offsetX, state.offsetY, safeRadius * (60f / 42f))) {
                        state.isDragging = true
                        state.touchDx = event.x - ballX
                        state.touchDy = event.y - ballY
                        state.lastDragTime = event.eventTime
                        performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    } else {
                        state.isDragging = false
                        state.touchDx = 0f
                        state.touchDy = 0f
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (state.isDragging) {
                    drag(event, state, centerX, centerY, safeRadius, minX, maxX, minY, maxY)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (state.isDragging) {
                    drag(event, state, centerX, centerY, safeRadius, minX, maxX, minY, maxY)
                    state.isDragging = false
                    performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                    return
                }
                val releasedButton = getActiveMenuButton()
                setActiveMenuButton(com.moonsolstudios.kavvoro.model.MenuButton.NONE)
                if (releasedButton != com.moonsolstudios.kavvoro.model.MenuButton.NONE && menuButtonAt(event.x, event.y) == releasedButton) {
                    handleMenuButton(releasedButton)
                }
            }
        }
    }
}
