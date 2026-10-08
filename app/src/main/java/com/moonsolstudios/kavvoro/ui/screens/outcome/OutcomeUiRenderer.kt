package com.moonsolstudios.kavvoro.ui.screens.outcome

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.model.NextReward
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.UiWidgetRenderer
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.min

/**
 * Dedicated renderer for the Game Outcome / Victory & Game Over screen.
 */
object OutcomeUiRenderer {

    private val scratch = RectF()

    fun drawOutcome(
        canvas: Canvas,
        won: Boolean,
        levelAccent: Int,
        levelTitle: String,
        gameMode: GameMode,
        gameplayHudBottom: Float,
        viewWidth: Float,
        viewHeight: Float,
        dp: Float,
        lastScore: RunScore?,
        lastHypeScore: Int,
        experienceReward: Int,
        lastRiftBreak: Boolean,
        lastRiftBreakBonus: Int,
        lastRiftBreakReason: String,
        rewardMessage: String,
        nextRewardText: String?,
        nextRewardInfo: NextReward?,
        resultShareButton: RectF,
        resultNextButton: RectF,
        resultRetryButton: RectF,
        continueRequiresAd: Boolean,
        streak: Int,
        activeButton: ButtonId?,
        selectedBallSkin: BallSkin,
        archetypeLabel: String,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        localizedLevelTitle: (String) -> String,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val accent = if (won) levelAccent else 0xFFFF4D8D.toInt()
        val panelHeight = dp * if (won) 378f else 238f
        val panelWidth = min(viewWidth - dp * 32f, dp * 540f)
        val left = (viewWidth - panelWidth) * 0.5f
        val right = left + panelWidth
        val bottom = viewHeight - dp * 16f
        val top = bottom - panelHeight

        paint.style = Paint.Style.FILL
        paint.color = 0x52000000
        canvas.drawRect(0f, gameplayHudBottom, viewWidth, viewHeight, paint)
        scratch.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.color = 0xF2070B12.toInt()
        canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.9f
        paint.color = withAlpha(accent, 150)
        canvas.drawRoundRect(scratch, dp * 8f, dp * 8f, paint)
        paint.style = Paint.Style.FILL
        paint.color = accent
        canvas.drawRoundRect(left, top, right, top + dp * 4f, dp * 3f, dp * 3f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + dp * 14f, top + dp * 1f, right - dp * 14f, top + dp * 1f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 9f
        textPaint.letterSpacing = 0.08f
        textPaint.color = withAlpha(accent, 235)
        val outcomeSubtitle = "✦ RIFT // ${t(if (won) "RUN COMPLETE" else "RUN INTERRUPTED").uppercase()} ✦  /  ${gameMode.menuTitle(t)}"
        canvas.drawText(fitText(outcomeSubtitle, right - left - dp * 36f), left + dp * 18f, top + dp * 25f, textPaint)
        textPaint.letterSpacing = 0f
        textPaint.textSize = dp * 23f
        textPaint.color = 0xFFF7F4FF.toInt()
        val title = if (won) localizedLevelTitle(levelTitle) else t("RIFT COLLAPSED").uppercase()
        canvas.drawText(fitText(title, right - left - dp * 36f), left + dp * 18f, top + dp * 51f, textPaint)
        drawBrainballResultLine(
            canvas = canvas,
            left = left + dp * 18f,
            top = top + dp * 66f,
            right = right - dp * 18f,
            accent = accent,
            won = won,
            skin = selectedBallSkin,
            archetypeLabel = archetypeLabel,
            lastRiftBreak = lastRiftBreak,
            dp = dp,
            textPaint = textPaint,
            t = t,
            fitText = fitText,
            drawBallSkin = drawBallSkin
        )

        if (won) {
            val score = lastScore
            drawResultSummaryRow(
                canvas = canvas,
                left = left + dp * 18f,
                top = top + dp * 102f,
                right = right - dp * 18f,
                score = score,
                accent = accent,
                lastHypeScore = lastHypeScore,
                experienceReward = experienceReward,
                dp = dp,
                paint = paint,
                textPaint = textPaint,
                t = t,
                fitText = fitText
            )

            if (lastRiftBreak) {
                drawRiftBreakResultBadge(
                    canvas = canvas,
                    left = left + dp * 18f,
                    top = top + dp * 184f,
                    right = right - dp * 18f,
                    accent = accent,
                    lastRiftBreakBonus = lastRiftBreakBonus,
                    lastRiftBreakReason = lastRiftBreakReason,
                    dp = dp,
                    paint = paint,
                    textPaint = textPaint,
                    t = t,
                    fitText = fitText
                )
            }
            val rewardTop = top + if (lastRiftBreak) dp * 222f else dp * 196f
            val reward = rewardMessage.ifBlank { nextRewardText ?: t("ALL FREE REWARDS UNLOCKED").uppercase() }.replace(" | ", "  /  ")
            drawRewardSignalCard(
                canvas = canvas,
                left = left + dp * 18f,
                top = rewardTop,
                right = right - dp * 18f,
                reward = reward,
                accent = accent,
                info = nextRewardInfo,
                dp = dp,
                paint = paint,
                textPaint = textPaint,
                t = t,
                fitText = fitText,
                drawWorldAsset = drawWorldAsset
            )
            drawResultActionButton(
                canvas = canvas,
                rect = resultShareButton,
                label = t("SHARE SHORT").uppercase(),
                accent = 0xFFFFCF4A.toInt(),
                button = ButtonId.SHARE,
                activeButton = activeButton,
                dp = dp,
                paint = paint,
                textPaint = textPaint,
                fitText = fitText
            )
            drawResultActionButton(
                canvas = canvas,
                rect = resultNextButton,
                label = t("NEXT LEVEL").uppercase(),
                accent = levelAccent,
                button = ButtonId.NEXT,
                activeButton = activeButton,
                dp = dp,
                paint = paint,
                textPaint = textPaint,
                fitText = fitText
            )
        } else {
            val needsAd = continueRequiresAd
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = dp * 12f
            textPaint.color = 0xBFFFFFFF.toInt()
            canvas.drawText(
                fitText(
                    if (needsAd) t("Keep streak %s with one ad.").replace("%s", "x$streak")
                    else t("Free recovery available. Streak %s stays active.").replace("%s", "x$streak"),
                    right - left - dp * 36f
                ),
                left + dp * 18f,
                top + dp * 106f,
                textPaint
            )
            paint.style = Paint.Style.FILL
            paint.color = 0x22FFFFFF
            canvas.drawRect(left + dp * 18f, top + dp * 126f, right - dp * 18f, top + dp * 127f, paint)
            textPaint.textSize = dp * 9f
            textPaint.color = 0x77FFFFFF
            canvas.drawText(t("RIFT ENERGY RESETS / LEVEL RESTARTS").uppercase(), left + dp * 18f, top + dp * 148f, textPaint)
            val label = if (continueRequiresAd) t("WATCH AD").uppercase() else t("CONTINUE FREE").uppercase()
            drawResultActionButton(
                canvas = canvas,
                rect = resultRetryButton,
                label = label,
                accent = 0xFFFF4D8D.toInt(),
                button = ButtonId.CONTINUE,
                activeButton = activeButton,
                dp = dp,
                paint = paint,
                textPaint = textPaint,
                fitText = fitText
            )
        }
    }

    fun drawResultSummaryRow(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        score: RunScore?,
        accent: Int,
        lastHypeScore: Int,
        experienceReward: Int,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        val height = dp * 68f
        scratch.set(left, top, right, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left, top, right, top + height,
            intArrayOf(withAlpha(accent, 28), 0xB50B1019.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.8f
        paint.color = withAlpha(accent, 95)
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + dp * 8f, top + dp * 1f, right - dp * 8f, top + dp * 1f, paint)

        val rankWidth = dp * 72f
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 32)
        canvas.drawRoundRect(left, top, left + rankWidth, top + height, dp * 7f, dp * 7f, paint)
        paint.color = withAlpha(accent, 190)
        canvas.drawRect(left + rankWidth - dp * 2f, top + dp * 10f, left + rankWidth, top + height - dp * 10f, paint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 8.5f
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("RANK").uppercase(), left + rankWidth * 0.5f, top + dp * 16f, textPaint)
        textPaint.textSize = dp * 48f
        textPaint.color = accent
        canvas.drawText(score?.rank ?: "-", left + rankWidth * 0.5f, top + dp * 58f, textPaint)

        val metricLeft = left + rankWidth + dp * 10f
        val metricWidth = (right - metricLeft) / 3f
        drawResultMetricCell(canvas, metricLeft, top, metricWidth, t("TIME").uppercase(), score?.let { "${"%.1f".format(it.seconds)}s" } ?: "-", true, dp, paint, textPaint, fitText)
        drawResultMetricCell(canvas, metricLeft + metricWidth, top, metricWidth, t("HYPE").uppercase(), lastHypeScore.toString(), true, dp, paint, textPaint, fitText)
        drawResultMetricCell(canvas, metricLeft + metricWidth * 2f, top, metricWidth, t("XP").uppercase(), "+$experienceReward", false, dp, paint, textPaint, fitText)
    }

    fun drawResultMetricCell(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        label: String,
        value: String,
        divider: Boolean,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        fitText: (String, Float) -> String
    ) {
        if (divider) {
            paint.style = Paint.Style.FILL
            paint.color = 0x20FFFFFF
            canvas.drawRect(left + width - dp * 1f, top + dp * 12f, left + width, top + dp * 56f, paint)
        }
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 8f
        textPaint.color = 0x78FFFFFF
        canvas.drawText(label, left + dp * 9f, top + dp * 22f, textPaint)
        textPaint.textSize = dp * 15f
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(fitText(value, width - dp * 18f), left + dp * 9f, top + dp * 46f, textPaint)
    }

    fun drawBrainballResultLine(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        accent: Int,
        won: Boolean,
        skin: BallSkin,
        archetypeLabel: String,
        lastRiftBreak: Boolean,
        dp: Float,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit
    ) {
        val iconRadius = dp * 12f
        drawBallSkin(canvas, left + iconRadius, top + dp * 11f, iconRadius, skin, true, false)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 8.6f
        textPaint.color = withAlpha(accent, 230)
        val header = "${skin.name}  /  ${t(archetypeLabel).uppercase()}"
        canvas.drawText(fitText(header, right - left - dp * 34f), left + dp * 32f, top + dp * 8f, textPaint)

        textPaint.textSize = dp * 9f
        textPaint.color = 0xAFFFFFFF.toInt()
        val reaction = brainballReactionText(skin.style, won, lastRiftBreak)
        canvas.drawText(fitText(t(reaction), right - left - dp * 34f), left + dp * 32f, top + dp * 23f, textPaint)
    }

    fun drawRiftBreakResultBadge(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        accent: Int,
        lastRiftBreakBonus: Int,
        lastRiftBreakReason: String,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String
    ) {
        scratch.set(left, top, right, top + dp * 28f)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left, top, right, top,
            intArrayOf(withAlpha(0xFFFFCF4A.toInt(), 92), withAlpha(accent, 56), 0x00070B12),
            floatArrayOf(0f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = withAlpha(0xFFFFCF4A.toInt(), 205)
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = 0x55FFFFFF
        canvas.drawLine(left + dp * 8f, top + dp * 1f, right - dp * 8f, top + dp * 1f, paint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 10f
        textPaint.color = 0xFFF7F4FF.toInt()
        val label = "${t("RIFT BREAK").uppercase()}  +$lastRiftBreakBonus ${t("HYPE").uppercase()}"
        canvas.drawText(fitText(label, (right - left) * 0.58f), left + dp * 12f, top + dp * 18f, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = dp * 8.8f
        textPaint.color = withAlpha(0xFFFFCF4A.toInt(), 240)
        canvas.drawText(fitText(lastRiftBreakReason, (right - left) * 0.38f), right - dp * 10f, top + dp * 18f, textPaint)
    }

    fun drawRewardSignalCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        reward: String,
        accent: Int,
        info: NextReward?,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit
    ) {
        val height = dp * 66f
        scratch.set(left, top, right, top + height)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            left, top, right, top + height,
            intArrayOf(withAlpha(accent, 44), 0xCC0B1019.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 0.9f
        paint.color = withAlpha(accent, 135)
        canvas.drawRoundRect(scratch, dp * 7f, dp * 7f, paint)

        // Specular top highlight line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp * 1f
        paint.color = 0x44FFFFFF
        canvas.drawLine(left + dp * 8f, top + dp * 1f, right - dp * 8f, top + dp * 1f, paint)

        val iconSize = dp * 34f
        scratch.set(left + dp * 10f, top + dp * 14f, left + dp * 10f + iconSize, top + dp * 14f + iconSize)
        drawWorldAsset(canvas, "boost_chain", scratch, 220)

        val textLeft = left + dp * 54f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = AssetResourceManager.oxaniumTypeface ?: Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = dp * 8.8f
        textPaint.color = 0x88FFFFFF.toInt()
        canvas.drawText(t("REWARD SIGNAL").uppercase(), textLeft, top + dp * 18f, textPaint)
        textPaint.textSize = dp * 11f
        textPaint.color = 0xFFFFCF4A.toInt()
        canvas.drawText(fitText(reward, right - textLeft - dp * 14f), textLeft, top + dp * 37f, textPaint)

        val barLeft = textLeft
        val barRight = right - dp * 14f
        val barTop = top + dp * 49f
        val progress = info?.progress ?: 1f
        paint.style = Paint.Style.FILL
        paint.color = 0x24FFFFFF
        canvas.drawRoundRect(barLeft, barTop, barRight, barTop + dp * 5f, dp * 3f, dp * 3f, paint)
        paint.color = info?.accent ?: accent
        canvas.drawRoundRect(barLeft, barTop, barLeft + (barRight - barLeft) * progress.coerceIn(0f, 1f), barTop + dp * 5f, dp * 3f, dp * 3f, paint)

        if (info != null) {
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.textSize = dp * 8.2f
            textPaint.color = 0xAAFFFFFF.toInt()
            canvas.drawText("${(progress * 100f).toInt()}%", barRight, top + dp * 18f, textPaint)
        }
    }

    fun drawResultActionButton(
        canvas: Canvas,
        rect: RectF,
        label: String,
        accent: Int,
        button: ButtonId,
        activeButton: ButtonId?,
        dp: Float,
        paint: Paint,
        textPaint: Paint,
        fitText: (String, Float) -> String
    ) = UiWidgetRenderer.drawResultActionButton(
        canvas = canvas,
        rect = rect,
        label = label,
        accent = accent,
        button = button,
        activeButton = activeButton,
        dp = dp,
        paint = paint,
        textPaint = textPaint,
        fitText = fitText
    )

    private fun brainballReactionText(style: SkinStyle, won: Boolean, lastRiftBreak: Boolean): String {
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

    fun layoutOutcomeButtons(
        viewWidth: Float,
        viewHeight: Float,
        won: Boolean,
        dp: (Float) -> Float,
        resultShareButton: RectF,
        resultNextButton: RectF,
        resultRetryButton: RectF
    ) {
        val panelWidth = min(viewWidth - dp(68f), dp(500f))
        val panelLeft = (viewWidth - panelWidth) * 0.5f
        val panelRight = panelLeft + panelWidth
        val resultHeight = dp(48f)
        val resultBottom = viewHeight - dp(34f)
        if (won) {
            val gapWidth = dp(10f)
            val half = (panelRight - panelLeft - gapWidth) * 0.5f
            resultShareButton.apply {
                left = panelLeft
                top = resultBottom - resultHeight
                right = panelLeft + half
                bottom = resultBottom
            }
            resultNextButton.apply {
                left = panelLeft + half + gapWidth
                top = resultBottom - resultHeight
                right = panelRight
                bottom = resultBottom
            }
        } else {
            resultShareButton.apply { left = 0f; top = 0f; right = 0f; bottom = 0f }
            resultNextButton.apply {
                left = panelLeft
                top = resultBottom - resultHeight
                right = panelRight
                bottom = resultBottom
            }
        }
        resultRetryButton.apply {
            left = resultNextButton.left
            top = resultNextButton.top
            right = resultNextButton.right
            bottom = resultNextButton.bottom
        }
    }
}
