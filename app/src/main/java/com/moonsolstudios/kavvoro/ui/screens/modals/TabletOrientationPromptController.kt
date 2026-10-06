package com.moonsolstudios.kavvoro.ui.screens.modals

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.RectF
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.MotionEvent
import android.view.WindowManager

class TabletOrientationPromptController {
    var isEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                isModalVisible = false
                isDismissed = false
                isTabletLandscape = false
                clearRect(dismissButtonRect)
                clearRect(persistentBadgeRect)
                clearRect(modalCardRect)
            }
        }

    var isModalVisible: Boolean = false
        private set

    var isDismissed: Boolean = false
        private set

    var isTabletLandscape: Boolean = false
        private set

    var pulseTimer: Float = 0f
        private set

    val dismissButtonRect = RectF()
    val persistentBadgeRect = RectF()
    val modalCardRect = RectF()

    fun update(dt: Float, context: Context, viewWidth: Int, viewHeight: Int) {
        if (!isEnabled) {
            if (isModalVisible || isDismissed || isTabletLandscape) {
                isModalVisible = false
                isDismissed = false
                isTabletLandscape = false
                clearRect(dismissButtonRect)
                clearRect(persistentBadgeRect)
                clearRect(modalCardRect)
            }
            return
        }
        pulseTimer += dt
        val activeTabletLandscape = checkTabletLandscape(context, viewWidth, viewHeight)
        if (activeTabletLandscape != isTabletLandscape) {
            isTabletLandscape = activeTabletLandscape
            if (!isTabletLandscape) {
                // Device rotated to portrait: reset everything and hide all overlays
                isModalVisible = false
                isDismissed = false
                clearRect(dismissButtonRect)
                clearRect(persistentBadgeRect)
                clearRect(modalCardRect)
            } else {
                // Device returned to landscape tablet (thumbnail mode):
                // Always reset dismissal and re-arm modal so it reappears
                isDismissed = false
                isModalVisible = true
            }
        } else if (isTabletLandscape && !isDismissed && !isModalVisible) {
            isModalVisible = true
        }
    }

    fun onResume() {
        if (!isEnabled) return
        if (isTabletLandscape) {
            isDismissed = false
            isModalVisible = true
        }
    }

    fun handleTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || !isTabletLandscape) return false

        val action = event.actionMasked
        val x = event.x
        val y = event.y

        if (isModalVisible) {
            if (action == MotionEvent.ACTION_UP) {
                if (dismissButtonRect.contains(x, y)) {
                    isDismissed = true
                    isModalVisible = false
                    return true
                } else if (!modalCardRect.isEmpty && !modalCardRect.contains(x, y)) {
                    isDismissed = true
                    isModalVisible = false
                    return true
                }
            }
            // Block touches behind the modal while it is active
            return true
        }

        if (isDismissed) {
            // Check if user tapped persistent reminder badge to re-open the modal
            if (action == MotionEvent.ACTION_UP && persistentBadgeRect.contains(x, y)) {
                isModalVisible = true
                isDismissed = false
                return true
            }
        }

        return false
    }

    fun forceShowModal() {
        if (!isEnabled) return
        if (isTabletLandscape) {
            isModalVisible = true
            isDismissed = false
        }
    }

    fun onOrientationStateChanged(isLandscapeTablet: Boolean) {
        if (!isEnabled) return
        if (isLandscapeTablet != isTabletLandscape) {
            isTabletLandscape = isLandscapeTablet
            if (!isTabletLandscape) {
                isModalVisible = false
                isDismissed = false
                clearRect(dismissButtonRect)
                clearRect(persistentBadgeRect)
                clearRect(modalCardRect)
            } else {
                isDismissed = false
                isModalVisible = true
            }
        }
    }

    private fun clearRect(r: RectF) {
        r.left = 0f
        r.top = 0f
        r.right = 0f
        r.bottom = 0f
    }

    companion object {
        fun checkTabletLandscape(context: Context, viewWidth: Int, viewHeight: Int): Boolean {
            val appConfig = context.resources.configuration
            val sysConfig = Resources.getSystem().configuration
            val metrics = context.resources.displayMetrics

            // 1. Official Android tablet qualification: smallestScreenWidthDp >= 600 or min dimension dp >= 600
            val minDimDp = (minOf(metrics.widthPixels, metrics.heightPixels) / metrics.density).toInt()
            val sw = maxOf(appConfig.smallestScreenWidthDp, sysConfig.smallestScreenWidthDp, minDimDp)
            val isTablet = sw >= 600
            if (!isTablet) return false

            // 2. Physical Display Orientation Detection
            var physicalWidth = 0
            var physicalHeight = 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val wm = context.getSystemService(WindowManager::class.java)
                wm?.maximumWindowMetrics?.bounds?.let { bounds ->
                    physicalWidth = bounds.width()
                    physicalHeight = bounds.height()
                }
            }

            if (physicalWidth == 0 || physicalHeight == 0) {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try { context.display } catch (_: Throwable) { null }
                } else {
                    null
                } ?: run {
                    val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                    dm?.getDisplay(Display.DEFAULT_DISPLAY)
                }

                if (display != null) {
                    val realMetrics = DisplayMetrics()
                    @Suppress("DEPRECATION")
                    display.getRealMetrics(realMetrics)
                    physicalWidth = realMetrics.widthPixels
                    physicalHeight = realMetrics.heightPixels
                }
            }

            // Definite hardware display dimensions check
            if (physicalWidth > 0 && physicalHeight > 0) {
                // If height > width, device is strictly held in portrait -> not thumbnail mode!
                if (physicalHeight > physicalWidth) {
                    return false
                }
                // If width > height, device is held in landscape -> thumbnail mode!
                if (physicalWidth > physicalHeight) {
                    return true
                }
            }

            // 3. System-level global configuration orientation
            if (sysConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                return false
            }
            if (sysConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return true
            }

            // 4. App configuration fallback
            if (appConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                return false
            }
            if (appConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return true
            }

            // 5. View aspect ratio fallback
            if (viewWidth > 0 && viewHeight > 0) {
                return viewWidth > viewHeight
            }

            return false
        }
    }
}
