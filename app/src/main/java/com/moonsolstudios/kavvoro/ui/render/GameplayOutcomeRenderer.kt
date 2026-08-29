package com.moonsolstudios.kavvoro.ui.render

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.NextReward
import com.moonsolstudios.kavvoro.model.SkinStyle
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Procedural outcome / game over / victory overlay renderer.
 */
object GameplayOutcomeRenderer {

    private val scratchRect = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawOutcomePanel(
        canvas: Canvas,
        won: Boolean,
        accent: Int,
        panelRect: RectF,
        hudBottom: Float,
        viewWidth: Float,
        viewHeight: Float,
        paint: Paint,
        dp: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.color = 0x52000000
        canvas.drawRect(0f, hudBottom, viewWidth, viewHeight, paint)
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(panelRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 150)
        canvas.drawRoundRect(panelRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(panelRect.left, panelRect.top, panelRect.right, panelRect.top + 4f * dp, 3f * dp, 3f * dp, paint)
    }

    fun drawOutcomeHeader(
        canvas: Canvas,
        won: Boolean,
        title: String,
        menuTitle: String,
        levelIndex: Int,
        accent: Int,
        panelWidth: Float,
        panelLeft: Float,
        panelTop: Float,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val meta = "$menuTitle  /  ${t("LEVEL").uppercase()} ${levelIndex.toString().padStart(2, '0')}"
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, 220)
        val textX = if (isRtl) panelLeft + panelWidth - 20f * dp else panelLeft + 20f * dp
        canvas.drawText(fitText(meta, panelWidth - 40f * dp), textX, panelTop + 24f * dp, textPaint)

        textPaint.textSize = 28f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(title, textX, panelTop + 54f * dp, textPaint)
    }

    fun drawResultSummaryRow(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        score: RunScore?,
        lastHypeScore: Int,
        maxChain: Int,
        timeFormatted: String,
        accent: Int,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String
    ) {
        val width = right - left
        val height = 72f * dp
        scratchRect.set(left, top, right, top + height)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 34)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 140)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        val colWidth = width / 3f
        val metrics = listOf(
            Triple(t("TIME").uppercase(), timeFormatted, accent),
            Triple(t("CHAIN").uppercase(), if (maxChain > 0) "x$maxChain" else "-", 0xFFFFCF4A.toInt()),
            Triple(t("HYPE").uppercase(), "+$lastHypeScore", 0xFFFFCF4A.toInt())
        )
        val ordered = if (isRtl) metrics.reversed() else metrics
        ordered.forEachIndexed { i, (label, value, color) ->
            val cx = left + colWidth * i + colWidth * 0.5f
            textPaint.reset()
            textPaint.isAntiAlias = true
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
            textPaint.textSize = 8.5f * dp
            textPaint.color = 0x88FFFFFF.toInt()
            canvas.drawText(label, cx, top + 22f * dp, textPaint)
            textPaint.textSize = 15f * dp
            textPaint.color = color
            canvas.drawText(value, cx, top + 47f * dp, textPaint)
        }
    }

    fun drawRankBadge(
        canvas: Canvas,
        rank: String,
        rankReason: String,
        accent: Int,
        panelWidth: Float,
        left: Float,
        top: Float,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        val width = panelWidth - 40f * dp
        val height = 48f * dp
        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0x2218202C
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = 0x36FFFFFF
        canvas.drawRoundRect(scratchRect, 6f * dp, 6f * dp, paint)

        val badgeX = if (isRtl) left + width - 26f * dp else left + 26f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 22f * dp
        textPaint.color = accent
        canvas.drawText(rank, badgeX, top + 33f * dp, textPaint)

        val textX = if (isRtl) left + width - 56f * dp else left + 56f * dp
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.textSize = 11f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(rankReason, width - 68f * dp), textX, top + 29f * dp, textPaint)
    }

    fun drawRewardSignalCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        reward: String,
        progress: Float,
        accent: Int,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val height = 66f * dp
        scratchRect.set(left, top, right, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            top + height,
            intArrayOf(withAlpha(accent, 44), 0xCC0B1019.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 135)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        val iconSize = 34f * dp
        val iconLeft = if (isRtl) right - 10f * dp - iconSize else left + 10f * dp
        scratchRect.set(iconLeft, top + 14f * dp, iconLeft + iconSize, top + 14f * dp + iconSize)
        drawWorldAsset(canvas, "boost_chain", scratchRect, 220)

        val textLeft = left + 54f * dp
        val textRight = right - 54f * dp
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 8.8f * dp
        textPaint.color = 0x88FFFFFF.toInt()
        val textX = if (isRtl) textRight else textLeft
        canvas.drawText(t("REWARD SIGNAL").uppercase(), textX, top + 18f * dp, textPaint)
        textPaint.textSize = 11f * dp
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(fitText(reward, right - left - 68f * dp), textX, top + 37f * dp, textPaint)

        val barLeft = if (isRtl) left + 14f * dp else textLeft
        val barRight = if (isRtl) textRight else right - 14f * dp
        val barTop = top + 49f * dp
        paint.style = Paint.Style.FILL
        paint.color = 0x24FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + 5f * dp, 3f * dp, 3f * dp, paint)
        paint.color = accent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress.coerceIn(0f, 1f), barTop + 5f * dp, 3f * dp, 3f * dp, paint)

        textPaint.textAlign = if (isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
        textPaint.textSize = 8.2f * dp
        textPaint.color = 0xAAFFFFFF.toInt()
        canvas.drawText(
            "${(progress * 100f).roundToInt()}%",
            if (isRtl) barLeft else barRight,
            top + 18f * dp,
            textPaint
        )
    }

    fun drawRiftBreakResultBadge(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        accent: Int,
        bonus: Int,
        reason: String,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        scratchRect.set(left, top, right, top + 28f * dp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left,
            top,
            right,
            top,
            intArrayOf(withAlpha(0xFFFFCF4A.toInt(), 92), withAlpha(accent, 56), 0x00070B12),
            floatArrayOf(0f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(0xFFFFCF4A.toInt(), 205)
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 10f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        val label = "${t("RIFT BREAK").uppercase()}  +$bonus ${t("HYPE").uppercase()}"
        val labelX = if (isRtl) right - 12f * dp else left + 12f * dp
        canvas.drawText(fitText(label, (right - left) * 0.58f), labelX, top + 18f * dp, textPaint)
        textPaint.textAlign = if (isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
        textPaint.textSize = 8.8f * dp
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), 240)
        canvas.drawText(
            fitText(reason, (right - left) * 0.38f),
            if (isRtl) left + 10f * dp else right - 10f * dp,
            top + 18f * dp,
            textPaint
        )
    }

    fun drawAdPlaceholder(
        canvas: Canvas,
        accent: Int,
        panelWidth: Float,
        panelHeight: Float,
        left: Float,
        top: Float,
        adReason: String,
        isContinueAfterFail: Boolean,
        adLoading: Boolean,
        stateElapsed: Float,
        menuTitle: String,
        adButton: RectF,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        scratchRect.set(left, top, left + panelWidth, top + panelHeight)
        paint.style = Paint.Style.FILL
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 170)
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(left, top, left + panelWidth, top + 4f * dp, 3f * dp, 3f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, 235)
        canvas.drawText("${t("STREAK PROTECTION").uppercase()}  /  $menuTitle", left + 20f * dp, top + 28f * dp, textPaint)
        textPaint.textSize = 27f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("AD CONTINUE").uppercase(), left + 20f * dp, top + 59f * dp, textPaint)
        textPaint.textSize = 12f * dp
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText(fitText(adReason, panelWidth - 40f * dp), left + 20f * dp, top + 84f * dp, textPaint)
        textPaint.textSize = 9f * dp
        textPaint.color = 0x99FFFFFF.toInt()
        val adLine = if (isContinueAfterFail) {
            t("WATCH TO KEEP THE RUN ALIVE").uppercase()
        } else {
            t("THE RUN RESUMES AFTER THE INTERSTITIAL").uppercase()
        }
        canvas.drawText(adLine, left + 20f * dp, top + 105f * dp, textPaint)

        val cx = left + panelWidth - 38f * dp
        val cy = top + 43f * dp
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * dp
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = withAlpha(accent, 210)
        scratchRect.set(cx - 13f * dp, cy - 13f * dp, cx + 13f * dp, cy + 13f * dp)
        canvas.drawArc(scratchRect, -80f, 290f + sin(stateElapsed * 4f) * 30f, false, paint)
        paint.strokeCap = Paint.Cap.BUTT

        adButton.set(left + 20f * dp, top + panelHeight - 68f * dp, left + panelWidth - 20f * dp, top + panelHeight - 20f * dp)
        drawResultActionButton(
            canvas = canvas,
            rect = adButton,
            label = t(if (adLoading) "LOADING" else "CONTINUE WITH AD").uppercase(),
            accent = accent,
            isSecondary = false,
            isActive = false,
            isContinue = true,
            paint = paint,
            dp = dp,
            fitText = fitText
        )
    }

    fun drawExportingOverlay(
        canvas: Canvas,
        viewWidth: Float,
        viewHeight: Float,
        width: Float,
        height: Float,
        left: Float,
        top: Float,
        accent: Int,
        menuPulse: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xA407090F.toInt()
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, paint)

        scratchRect.set(left, top, left + width, top + height)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF07090F.toInt()
        canvas.drawRoundRect(scratchRect, 9f * dp, 9f * dp, paint)
        paint.shader = LinearGradient(
            left,
            top,
            left + width,
            top + height,
            intArrayOf(withAlpha(accent, 92), 0xFF07090F.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)
        paint.shader = null
        paint.color = accent
        canvas.drawRect(left, top, left + 4f * dp, top + height, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = accent
        canvas.drawRoundRect(scratchRect, 7f * dp, 7f * dp, paint)

        val iconSize = 42f * dp
        scratchRect.set(left + 16f * dp, top + 16f * dp, left + 16f * dp + iconSize, top + 16f * dp + iconSize)
        drawWorldAsset(canvas, "ui_share", scratchRect, 245)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 18f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("BUILDING SHORT").uppercase(), left + 70f * dp, top + 31f * dp, textPaint)
        textPaint.textSize = 11f * dp
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText("9:16 MP4  /  TIKTOK  /  REELS  /  SHORTS", left + 70f * dp, top + 52f * dp, textPaint)

        val barLeft = left + 70f * dp
        val barRight = left + width - 18f * dp
        val barTop = top + 74f * dp
        val pulse = 0.38f + 0.62f * ((sin(menuPulse * 5.4f) + 1f) * 0.5f)
        paint.style = Paint.Style.FILL
        paint.color = 0x36FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + 6f * dp, 4f * dp, 4f * dp, paint)
        paint.color = accent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * pulse, barTop + 6f * dp, 4f * dp, 4f * dp, paint)

        textPaint.textSize = 8.6f * dp
        textPaint.color = 0xB8FFFFFF.toInt()
        canvas.drawText(t("SHARE COUNTS UNLOCK BYTE / KABOOM / 404").uppercase(), barLeft, top + 99f * dp, textPaint)
    }

    fun drawResultActionButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        accent: Int,
        isSecondary: Boolean,
        isActive: Boolean,
        isContinue: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String
    ) {
        val corner = 8f * dp
        paint.style = Paint.Style.FILL
        if (isSecondary) {
            paint.color = if (isActive) 0x6618202C else 0x3D18202C
            canvas.drawRoundRect(rect, corner, corner, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.9f * dp
            paint.color = if (isActive) withAlpha(accent, 220) else 0x42FFFFFF
            canvas.drawRoundRect(rect, corner, corner, paint)
        } else {
            paint.shader = LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                intArrayOf(
                    withAlpha(accent, if (isActive) 255 else 235),
                    withAlpha(accent, if (isActive) 180 else 140)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rect, corner, corner, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = if (isActive) 1.6f * dp else 1f * dp
            paint.color = withAlpha(0xFFFFFFFF.toInt(), if (isActive) 235 else 165)
            canvas.drawRoundRect(rect, corner, corner, paint)
        }

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = if (isContinue) 13.5f * dp else 12f * dp
        textPaint.color = if (isSecondary) 0xFFDCE6F5.toInt() else 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(label, rect.width() - 16f * dp), rect.centerX(), rect.centerY() + 4f * dp, textPaint)
    }

    fun drawBrainballResultLine(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        accent: Int,
        won: Boolean,
        lastRiftBreak: Boolean,
        skin: BallSkin,
        archetypeLabel: String,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit
    ) {
        val iconRadius = 12f * dp
        val iconX = if (isRtl) right - iconRadius else left + iconRadius
        drawBallSkin(canvas, iconX, top + 11f * dp, iconRadius, skin, true, false)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 8.6f * dp
        textPaint.color = withAlpha(accent, 230)
        val header = "${skin.name}  /  ${t(archetypeLabel).uppercase()}"
        val textX = if (isRtl) right - 32f * dp else left + 32f * dp
        canvas.drawText(fitText(header, right - left - 34f * dp), textX, top + 8f * dp, textPaint)

        textPaint.textSize = 9f * dp
        textPaint.color = 0xAFFFFFFF.toInt()
        val reaction = brainballReactionText(skin.style, won, lastRiftBreak)
        canvas.drawText(fitText(t(reaction), right - left - 34f * dp), textX, top + 23f * dp, textPaint)
    }

    fun brainballReactionText(style: SkinStyle, won: Boolean, lastRiftBreak: Boolean): String {
        if (!won) return "Brainball rebooting. Try cleaner taps."
        if (lastRiftBreak) return "Rift snapped. Braincell promoted."
        return when (style) {
            SkinStyle.PRISM -> "Prism brain approved this nonsense."
            SkinStyle.VOID -> "Void walked through the bad idea."
            SkinStyle.CHROME -> "Chrome bounce paid rent today."
            SkinStyle.PLASMA -> "Plasma cooked the route."
            SkinStyle.BLOP -> "Blop survived on pure vibes."
            SkinStyle.GLITCH -> "Glitch found the illegal angle."
            SkinStyle.ZAP -> "Zap arrived before the plan."
            SkinStyle.LOOP -> "Loop did it twice for no reason."
            SkinStyle.STATIC -> "Static stared the level down."
            SkinStyle.RIFT -> "Rift brain knew the shortcut."
            SkinStyle.BYTE -> "Byte uploaded the win."
            SkinStyle.WOBBLE -> "Wobble made physics look confused."
            SkinStyle.CROWN -> "Crown behavior, no debate."
            SkinStyle.CLASSIC -> "Original brainball still has aura."
        }
    }

    fun drawOutcome(
        canvas: Canvas,
        state: GameState,
        level: LevelSpec,
        gameModeTitle: String,
        gameplayHudBottom: Float,
        safeContentWidth: Float,
        safeInsetLeft: Float,
        safeBottom16: Float,
        viewWidth: Float,
        viewHeight: Float,
        selectedSkin: BallSkin,
        archetypeLabel: String,
        streak: Int,
        lastScore: RunScore?,
        lastHypeScore: Int,
        maxChain: Int,
        lastRiftBreak: Boolean,
        lastRiftBreakBonus: Int,
        lastRiftBreakReason: String,
        rewardMessage: String,
        nextRewardText: String?,
        nextRewardInfo: NextReward?,
        continueRequiresAd: Boolean,
        resultShareButton: RectF,
        resultNextButton: RectF,
        resultRetryButton: RectF,
        activeButton: ButtonId,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        formatScoreTime: (Float) -> String,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        localizedLevelTitle: (String) -> String,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        if (state != GameState.WON && state != GameState.LOST) return
        val won = state == GameState.WON
        val accent = if (won) level.accent else 0xFFFF4D8D.toInt()
        val panelHeight = (if (won) 378f else 238f) * dp
        val panelWidth = min(safeContentWidth - 32f * dp, 540f * dp)
        val left = safeInsetLeft + (safeContentWidth - panelWidth) * 0.5f
        val right = left + panelWidth
        val bottom = safeBottom16
        val top = bottom - panelHeight

        paint.style = Paint.Style.FILL
        paint.color = 0x52000000
        canvas.drawRect(0f, gameplayHudBottom, viewWidth, viewHeight, paint)
        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(accent, 150)
        canvas.drawRoundRect(scratchRect, 8f * dp, 8f * dp, paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(left, top, right, top + 4f * dp, 3f * dp, 3f * dp, paint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        textPaint.color = withAlpha(accent, 235)
        val titleX = if (isRtl) right - 18f * dp else left + 18f * dp
        canvas.drawText("${t(if (won) "RUN COMPLETE" else "RUN INTERRUPTED").uppercase()}  /  $gameModeTitle", titleX, top + 25f * dp, textPaint)
        textPaint.textSize = 23f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        val title = if (won) localizedLevelTitle(level.title) else t("RIFT COLLAPSED").uppercase()
        canvas.drawText(fitText(title, right - left - 36f * dp), titleX, top + 51f * dp, textPaint)
        drawBrainballResultLine(canvas, left + 18f * dp, top + 66f * dp, right - 18f * dp, accent, won, lastRiftBreak, selectedSkin, archetypeLabel, isRtl, paint, dp, t, fitText, drawBallSkin)

        if (won) {
            val score = lastScore
            val timeFormatted = score?.let { formatScoreTime(it.seconds) } ?: "-"
            drawResultSummaryRow(canvas, left + 18f * dp, top + 102f * dp, right - 18f * dp, score, lastHypeScore, maxChain, timeFormatted, accent, isRtl, paint, dp, t)

            if (lastRiftBreak) {
                drawRiftBreakResultBadge(canvas, left + 18f * dp, top + 184f * dp, right - 18f * dp, accent, lastRiftBreakBonus, lastRiftBreakReason, isRtl, paint, dp, t, fitText)
            }
            val rewardTop = top + if (lastRiftBreak) 222f * dp else 196f * dp
            val reward = rewardMessage.ifBlank { nextRewardText ?: t("ALL FREE REWARDS UNLOCKED").uppercase() }.replace(" | ", "  /  ")
            val rewardProgress = nextRewardInfo?.progress ?: 1f
            val rewardAccent = nextRewardInfo?.accent ?: accent
            drawRewardSignalCard(canvas, left + 18f * dp, rewardTop, right - 18f * dp, reward, rewardProgress, rewardAccent, isRtl, paint, dp, t, fitText, drawWorldAsset)
            drawResultActionButton(canvas, resultShareButton, t("SHARE SHORT").uppercase(), 0xFFFFCF4A.toInt(), isSecondary = true, isActive = activeButton == ButtonId.SHARE, isContinue = false, paint = paint, dp = dp, fitText = fitText)
            drawResultActionButton(canvas, resultNextButton, t("NEXT LEVEL").uppercase(), level.accent, isSecondary = false, isActive = activeButton == ButtonId.NEXT, isContinue = false, paint = paint, dp = dp, fitText = fitText)
        } else {
            val needsAd = continueRequiresAd
            textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
            textPaint.textSize = 12f * dp
            textPaint.color = 0xBFFFFFFF.toInt()
            val bodyX = if (isRtl) right - 18f * dp else left + 18f * dp
            canvas.drawText(
                fitText(
                    if (needsAd) t("Keep streak %s with one ad.").replace("%s", "x$streak")
                    else t("Free recovery available. Streak %s stays active.").replace("%s", "x$streak"),
                    right - left - 36f * dp
                ),
                bodyX,
                top + 106f * dp,
                textPaint
            )
            paint.style = Paint.Style.FILL
            paint.color = 0x22FFFFFF
            canvas.drawRect(left + 18f * dp, top + 126f * dp, right - 18f * dp, top + 127f * dp, paint)
            textPaint.textSize = 9f * dp
            textPaint.color = 0x77FFFFFF
            canvas.drawText(t("RIFT ENERGY RESETS / LEVEL RESTARTS").uppercase(), bodyX, top + 148f * dp, textPaint)
            val label = if (continueRequiresAd) t("WATCH AD").uppercase() else t("CONTINUE FREE").uppercase()
            drawResultActionButton(canvas, resultRetryButton, label, 0xFFFF4D8D.toInt(), isSecondary = false, isActive = activeButton == ButtonId.CONTINUE, isContinue = true, paint = paint, dp = dp, fitText = fitText)
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0, 255)) shl 24)
}

