package com.moonsolstudios.kavvoro.ui.screens.home

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import com.moonsolstudios.kavvoro.i18n.HomeCopy
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n

/**
 * TalkBack virtual accessibility node provider for Kavvoro's SurfaceView/Canvas Home menu.
 *
 * Exposes the PLAY NOW button, Settings gear, the lower navigation cards (Skins, Missions,
 * Leaderboard), and the 3 header stat chips (Streak, Level, hype).
 *
 * Two rules keep this honest:
 *  - nodes are published only while the Home menu surface is the one on screen
 *    ([isHomeSurfaceVisible]). Every other surface is laid out by a different calculator, so
 *    publishing Home nodes there announced controls with stale bounds and no nodes at all for
 *    the controls actually drawn. An uninstrumented surface now publishes nothing instead.
 *  - bounds come from the calculator's touch rects, which are the visual rects grown to the
 *    platform minimum target size.
 */
class HomeAccessibilityTouchHelper(
    private val host: View,
    private val calculator: HomeLayoutCalculator,
    private val isHomeSurfaceVisible: () -> Boolean,
    private val getStreak: () -> Int,
    private val getLevel: () -> Int,
    private val getHypeText: () -> String,
    private val isDailyCheckReady: () -> Boolean,
    private val onPlayClicked: () -> Unit,
    private val onSettingsClicked: () -> Unit,
    private val onSkinsClicked: () -> Unit,
    private val onMissionsClicked: () -> Unit,
    private val onHypeClicked: () -> Unit,
    private val onLeaderboardClicked: () -> Unit
) : ExploreByTouchHelper(host) {

    companion object {
        const val ID_SETTINGS = 1
        const val ID_PLAY_CTA = 2
        const val ID_SKINS_CARD = 3
        const val ID_MISSIONS_CARD = 4
        const val ID_LEADERBOARD_CARD = 5
        const val ID_STREAK_CHIP = 6
        const val ID_LEVEL_CHIP = 7
        const val ID_HYPE_CHIP = 8

        /** Focus and explore-by-touch order: header, primary action, then the navigation deck. */
        private val NODE_IDS = intArrayOf(
            ID_SETTINGS,
            ID_STREAK_CHIP,
            ID_LEVEL_CHIP,
            ID_HYPE_CHIP,
            ID_PLAY_CTA,
            ID_SKINS_CARD,
            ID_MISSIONS_CARD,
            ID_LEADERBOARD_CARD
        )
    }

    private val tempRect = Rect()

    private fun touchRectFor(virtualViewId: Int): LayoutRect? = when (virtualViewId) {
        ID_SETTINGS -> calculator.settingsTouchRect
        ID_STREAK_CHIP -> calculator.streakChipTouchRect
        ID_LEVEL_CHIP -> calculator.levelChipTouchRect
        ID_HYPE_CHIP -> calculator.hypeChipTouchRect
        ID_PLAY_CTA -> calculator.playCtaRect
        ID_SKINS_CARD -> calculator.skinsCardRect
        ID_MISSIONS_CARD -> calculator.missionsCardRect
        ID_LEADERBOARD_CARD -> calculator.leaderboardCardRect
        else -> null
    }

    override fun getVirtualViewAt(x: Float, y: Float): Int {
        if (!isHomeSurfaceVisible()) return HOST_ID
        return NODE_IDS.firstOrNull { touchRectFor(it)?.contains(x, y) == true } ?: HOST_ID
    }

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        if (!isHomeSurfaceVisible()) return
        for (id in NODE_IDS) {
            val rect = touchRectFor(id)
            if (rect != null && !rect.isEmpty()) {
                virtualViewIds.add(id)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
        val context = host.context
        when (virtualViewId) {
            ID_SETTINGS -> {
                node.contentDescription = KavvoroI18n.t(context, "SETTINGS")
                node.className = Button::class.java.name
                node.isClickable = true
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
            ID_PLAY_CTA -> {
                val title = HomeCopy.ctaPlay(context)
                val subtitle = if (calculator.showPlaySubtitle) ", ${HomeCopy.ctaSubtitle(context)}" else ""
                node.contentDescription = "$title$subtitle"
                node.className = Button::class.java.name
                node.isClickable = true
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
            ID_SKINS_CARD -> {
                val title = HomeCopy.skinsTitle(context)
                val sub = if (calculator.showCardSubtitles) ", ${HomeCopy.skinsSubtitle(context)}" else ""
                node.contentDescription = "$title$sub"
                node.className = Button::class.java.name
                node.isClickable = true
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
            ID_MISSIONS_CARD -> {
                val title = HomeCopy.missionsTitle(context)
                val sub = if (calculator.showCardSubtitles) ", ${HomeCopy.missionsSubtitle(context)}" else ""
                node.contentDescription = "$title$sub"
                node.className = Button::class.java.name
                node.isClickable = true
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
            ID_LEADERBOARD_CARD -> {
                val title = HomeCopy.leaderboardTitle(context)
                val sub = if (calculator.showCardSubtitles) ", ${HomeCopy.leaderboardSubtitle(context)}" else ""
                node.contentDescription = "$title$sub"
                node.className = Button::class.java.name
                node.isClickable = true
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
            ID_STREAK_CHIP -> {
                node.contentDescription = "${HomeCopy.streak(context)}: ${getStreak()}"
                node.className = TextView::class.java.name
                node.isClickable = false
            }
            ID_LEVEL_CHIP -> {
                node.contentDescription = "${HomeCopy.level(context)}: ${getLevel()}"
                node.className = TextView::class.java.name
                node.isClickable = false
            }
            ID_HYPE_CHIP -> {
                val bonusState = if (isDailyCheckReady()) "READY" else "CLAIMED"
                node.contentDescription = "${HomeCopy.hype(context)}: ${getHypeText()}, ${KavvoroI18n.t(context, "DAILY RIFT BONUS")}: ${KavvoroI18n.t(context, bonusState)}"
                node.className = Button::class.java.name
                node.isClickable = true
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
            else -> {
                node.contentDescription = ""
                node.className = TextView::class.java.name
                node.isClickable = false
            }
        }
        touchRectFor(virtualViewId).toRect(tempRect)
        node.setBoundsInParent(tempRect)
    }

    override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
        if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
            when (virtualViewId) {
                ID_SETTINGS -> {
                    onSettingsClicked()
                    return true
                }
                ID_PLAY_CTA -> {
                    onPlayClicked()
                    return true
                }
                ID_SKINS_CARD -> {
                    onSkinsClicked()
                    return true
                }
                ID_MISSIONS_CARD -> {
                    onMissionsClicked()
                    return true
                }
                ID_LEADERBOARD_CARD -> {
                    onLeaderboardClicked()
                    return true
                }
                ID_HYPE_CHIP -> {
                    onHypeClicked()
                    return true
                }
            }
        }
        return false
    }

    /**
     * A missing or empty rect would otherwise describe a zero-sized node; fall back to a 1x1
     * bounds so the provider never reports degenerate geometry.
     */
    private fun LayoutRect?.toRect(target: Rect) {
        if (this == null || isEmpty()) {
            target.set(0, 0, 1, 1)
        } else {
            target.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
    }
}
