package com.moonsolstudios.kavvoro.ui.screens.missions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.MissionCategory
import com.moonsolstudios.kavvoro.model.MissionId
import com.moonsolstudios.kavvoro.model.MissionProgress
import com.moonsolstudios.kavvoro.ui.render.AssetResourceManager
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import java.util.Locale
import kotlin.math.min

object MissionsUiRenderer {
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arrowPath = Path()
    private val checkPath = Path()
    private val glyphPath = Path()
    private val scratchBackRect = RectF()
    private val scratchTextRect = RectF()
    private val scratchCardRect = RectF()
    private val scratchChipRect = RectF()
    private val scratchButtonRect = RectF()
    private val scratchTrackRect = RectF()
    private val scratchFillRect = RectF()
    private val scratchShadowRect = RectF()
    private val scratchFaceRect = RectF()
    private val scratchIconRect = RectF()

    fun drawScreen(
        canvas: Canvas,
        layout: MissionsLayoutCalculator,
        missions: List<MissionProgress>,
        activeClaimIndex: Int,
        dp: Float,
        missionArt: Bitmap?,
        coinArt: Bitmap?,
        category: MissionCategory,
        t: (String) -> String
    ) {
        drawHeader(canvas, layout, dp, category, t)
        drawCategoryTabs(canvas, layout, category, dp, t)
        if (layout.showSummary) drawSummary(canvas, layout, missions, missionArt, coinArt, category, dp, t)
        missions.forEachIndexed { index, mission ->
            if (layout.cardRects.getOrNull(index)?.isEmpty() != false) return@forEachIndexed
            drawMissionCard(canvas, layout, mission, index, activeClaimIndex == index, coinArt, dp, t)
        }
    }

    private fun drawHeader(canvas: Canvas, layout: MissionsLayoutCalculator, dp: Float, category: MissionCategory, t: (String) -> String) {
        val back = layout.backButtonRect.toRectF(scratchBackRect)
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(back.left, back.top, back.right, back.bottom,
            0xE9152947.toInt(), 0xE9071127.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(back, 17f * dp, 17f * dp, shapePaint)
        shapePaint.shader = LinearGradient(back.left, back.top, back.right, back.bottom,
            KavvoroPalette.cyan, KavvoroPalette.blue, Shader.TileMode.CLAMP)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1.6f * dp
        canvas.drawRoundRect(back, 17f * dp, 17f * dp, shapePaint)

        arrowPath.reset()
        arrowPath.moveTo(back.centerX() + 6f * dp, back.centerY() - 9f * dp)
        arrowPath.lineTo(back.centerX() - 4f * dp, back.centerY())
        arrowPath.lineTo(back.centerX() + 6f * dp, back.centerY() + 9f * dp)
        shapePaint.shader = null
        shapePaint.strokeWidth = 3.1f * dp
        shapePaint.strokeCap = Paint.Cap.ROUND
        shapePaint.strokeJoin = Paint.Join.ROUND
        shapePaint.color = KavvoroPalette.cyan
        canvas.drawPath(arrowPath, shapePaint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.textSize = 18f * dp
        drawFitted(canvas, t("MISSIONS & REWARDS").uppercase(), layout.titleRect.toRectF(scratchTextRect), textPaint, 11f * dp)

        textPaint.typeface = AssetResourceManager.oxaniumNormal()
        textPaint.color = 0xFFD1D9F2.toInt()
        textPaint.textSize = 10f * dp
        textPaint.letterSpacing = 0.035f
        val subtitleKey = if (category == MissionCategory.DAILY) "COMPLETE DAILY MISSIONS & WIN" else "MASTER THE RIFT, EARN REWARDS"
        drawFitted(canvas, t(subtitleKey).uppercase(), layout.subtitleRect.toRectF(scratchTextRect), textPaint, 7f * dp)
        textPaint.letterSpacing = 0f
    }

    private fun drawCategoryTabs(canvas: Canvas, layout: MissionsLayoutCalculator, selected: MissionCategory, dp: Float, t: (String) -> String) {
        val tabs = layout.tabsRect.toRectF(scratchCardRect)
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = 0xB908142B.toInt()
        canvas.drawRoundRect(tabs, 17f * dp, 17f * dp, shapePaint)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1f * dp
        shapePaint.color = 0x775DA1CC
        canvas.drawRoundRect(tabs, 17f * dp, 17f * dp, shapePaint)

        drawCategoryTab(canvas, layout.dailyTabRect.toRectF(scratchTextRect), MissionCategory.DAILY, selected, dp, t)
        drawCategoryTab(canvas, layout.riftChallengesTabRect.toRectF(scratchIconRect), MissionCategory.RIFT_CHALLENGES, selected, dp, t)
    }

    private fun drawCategoryTab(
        canvas: Canvas,
        rect: RectF,
        category: MissionCategory,
        selected: MissionCategory,
        dp: Float,
        t: (String) -> String
    ) {
        val active = category == selected
        val accent = if (category == MissionCategory.DAILY) KavvoroPalette.cyan else KavvoroPalette.pink
        if (active) {
            shapePaint.style = Paint.Style.FILL
            shapePaint.shader = LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                if (category == MissionCategory.DAILY) 0xFF28D7EE.toInt() else 0xFFE844D8.toInt(),
                if (category == MissionCategory.DAILY) 0xFF3D70F8.toInt() else 0xFF8D48F4.toInt(),
                Shader.TileMode.CLAMP)
            canvas.drawRoundRect(rect, 13f * dp, 13f * dp, shapePaint)
            shapePaint.shader = null
            shapePaint.style = Paint.Style.STROKE
            shapePaint.strokeWidth = 0.8f * dp
            shapePaint.color = withAlpha(0xFFFFFFFF.toInt(), 125)
            canvas.drawRoundRect(rect, 13f * dp, 13f * dp, shapePaint)
        }
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 9.5f * dp
        textPaint.letterSpacing = 0.01f
        textPaint.color = if (active) 0xFFFFFFFF.toInt() else withAlpha(accent, 205)
        val key = if (category == MissionCategory.DAILY) "DAILY MISSIONS" else "RIFT CHALLENGES"
        drawFitted(canvas, t(key).uppercase(), rect, textPaint, 6.5f * dp)
    }

    private fun drawSummary(
        canvas: Canvas,
        layout: MissionsLayoutCalculator,
        missions: List<MissionProgress>,
        missionArt: Bitmap?,
        coinArt: Bitmap?,
        category: MissionCategory,
        dp: Float,
        t: (String) -> String
    ) {
        val summary = layout.summaryRect.toRectF(scratchCardRect)
        val radius = 22f * dp
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(summary.left, summary.top, summary.right, summary.bottom,
            0xF0203D70.toInt(), 0xF00B142F.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(summary, radius, radius, shapePaint)
        shapePaint.shader = null

        scratchShadowRect.set(summary.left, summary.top, summary.right, summary.top + summary.height() * 0.54f)
        shapePaint.shader = LinearGradient(scratchShadowRect.left, scratchShadowRect.top,
            scratchShadowRect.right, scratchShadowRect.bottom,
            0x222CB7F3, 0x000D1834, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchShadowRect, radius, radius, shapePaint)
        shapePaint.shader = LinearGradient(summary.left, summary.top, summary.right, summary.bottom,
            KavvoroPalette.cyan, KavvoroPalette.blue, Shader.TileMode.CLAMP)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1.25f * dp
        canvas.drawRoundRect(summary, radius, radius, shapePaint)
        shapePaint.shader = null

        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(summary.left, summary.top, summary.left + 4f * dp, summary.bottom,
            KavvoroPalette.cyan, KavvoroPalette.pink, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(summary.left + 1f * dp, summary.top + 23f * dp,
            summary.left + 4f * dp, summary.bottom - 23f * dp, 2f * dp, 2f * dp, shapePaint)
        shapePaint.shader = null

        val art = layout.summaryArtRect.toRectF(scratchIconRect)
        if (!art.isEmpty) {
            shapePaint.color = withAlpha(KavvoroPalette.cyan, 24)
            canvas.drawCircle(art.centerX(), art.centerY(), art.width() * 0.49f, shapePaint)
            shapePaint.style = Paint.Style.STROKE
            shapePaint.strokeWidth = 1.2f * dp
            shapePaint.color = withAlpha(KavvoroPalette.cyan, 115)
            canvas.drawCircle(art.centerX(), art.centerY(), art.width() * 0.42f, shapePaint)
            shapePaint.color = withAlpha(KavvoroPalette.pink, 150)
            canvas.drawArc(art.left + 7f * dp, art.top + 7f * dp, art.right - 7f * dp, art.bottom - 7f * dp,
                -42f, 93f, false, shapePaint)
            shapePaint.style = Paint.Style.FILL
            missionArt?.let {
                shapePaint.isFilterBitmap = true
                canvas.drawBitmap(it, null, art, shapePaint)
            }
        }

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.oxaniumMedium()
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 9f * dp
        textPaint.letterSpacing = 0.08f
        textPaint.color = 0xFFD6E3FF.toInt()
        val progressRect = layout.summaryProgressTextRect.toRectF(scratchTextRect)
        val progressLabelKey = if (category == MissionCategory.DAILY) "DAILY PROGRESS" else "MISSIONS COMPLETE"
        canvas.drawText(t(progressLabelKey).uppercase(), progressRect.left, summary.top + 31f * dp, textPaint)
        textPaint.letterSpacing = 0f
        textPaint.typeface = AssetResourceManager.spaceGroteskExtraBold()
        textPaint.textSize = 30f * dp
        textPaint.color = 0xFFFFFFFF.toInt()
        val completeCount = missions.count { it.isComplete }
        drawFitted(canvas, "$completeCount/${missions.size}", progressRect, textPaint, 20f * dp)

        val rewardLabel = layout.summaryRewardLabelRect.toRectF(scratchTextRect)
        textPaint.typeface = AssetResourceManager.oxaniumMedium()
        textPaint.textSize = 8f * dp
        textPaint.letterSpacing = 0.07f
        textPaint.color = 0xFFBFC9E5.toInt()
        canvas.drawText(t("TOTAL REWARDS").uppercase(), rewardLabel.left, baselineFor(rewardLabel, textPaint), textPaint)
        textPaint.letterSpacing = 0f

        val rewardRect = layout.summaryRewardTextRect.toRectF(scratchTextRect)
        coinArt?.let { bitmap ->
            scratchChipRect.set(rewardRect.left, rewardRect.centerY() - 11f * dp,
                rewardRect.left + 22f * dp, rewardRect.centerY() + 11f * dp)
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.isFilterBitmap = true
            canvas.drawBitmap(bitmap, null, scratchChipRect, shapePaint)
        }
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.textSize = 12f * dp
        textPaint.color = KavvoroPalette.gold
        scratchFaceRect.set(rewardRect.left + 25f * dp, rewardRect.top,
            rewardRect.right, rewardRect.bottom)
        drawFitted(canvas, "+${missions.sumOf { it.rewardCoins }.formatGrouped()} ${t("COINS").uppercase()}",
            scratchFaceRect, textPaint, 7f * dp)

        val track = layout.summaryTrackRect.toRectF(scratchTrackRect)
        shapePaint.color = 0x99030A1C.toInt()
        canvas.drawRoundRect(track, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
        val fraction = (completeCount.toFloat() / missions.size.coerceAtLeast(1)).coerceIn(0f, 1f)
        if (fraction > 0f) {
            scratchFillRect.set(track.left, track.top, track.left + track.width() * fraction, track.bottom)
            shapePaint.shader = LinearGradient(scratchFillRect.left, scratchFillRect.top,
                scratchFillRect.right.coerceAtLeast(scratchFillRect.left + 1f), scratchFillRect.bottom,
                KavvoroPalette.cyan, KavvoroPalette.pink, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(scratchFillRect, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
            shapePaint.shader = null
        }
    }

    private fun drawMissionCard(
        canvas: Canvas,
        layout: MissionsLayoutCalculator,
        mission: MissionProgress,
        index: Int,
        active: Boolean,
        coinArt: Bitmap?,
        dp: Float,
        t: (String) -> String
    ) {
        val card = layout.cardRects[index].toRectF(scratchCardRect)
        val accent = when (mission.id) {
            MissionId.DAILY_CLEAR_LEVELS, MissionId.RIFT_CLASSIC_LEVELS -> KavvoroPalette.cyan
            MissionId.DAILY_A_RANKS, MissionId.RIFT_CHAOS_LEVELS -> KavvoroPalette.pink
            MissionId.DAILY_RIFT_BREAKS, MissionId.RIFT_BREAKS -> KavvoroPalette.gold
            MissionId.RIFT_CHAIN_COMBOS -> KavvoroPalette.blue
        }
        val edge = if (mission.isComplete) KavvoroPalette.gold else accent
        val radius = 20f * dp
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        scratchShadowRect.set(card.left, card.top + 4f * dp, card.right, card.bottom + 4f * dp)
        shapePaint.color = 0xA8020715.toInt()
        canvas.drawRoundRect(scratchShadowRect, radius, radius, shapePaint)
        shapePaint.shader = LinearGradient(card.left, card.top, card.right, card.bottom,
            0xF4162543.toInt(), 0xF509132C.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(card, radius, radius, shapePaint)
        shapePaint.shader = null
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = if (mission.canClaim) 1.7f * dp else 1f * dp
        shapePaint.color = withAlpha(edge, if (mission.canClaim) 225 else 145)
        canvas.drawRoundRect(card, radius, radius, shapePaint)

        shapePaint.style = Paint.Style.FILL
        shapePaint.shader = LinearGradient(card.left, card.top, card.left, card.top + 50f * dp,
            withAlpha(edge, 195), withAlpha(edge, 0), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(card.left + 1f * dp, card.top + 17f * dp,
            card.left + 4f * dp, card.top + 66f * dp, 2f * dp, 2f * dp, shapePaint)
        shapePaint.shader = null
        shapePaint.color = withAlpha(0xFFFFFFFF.toInt(), 15)
        canvas.drawRoundRect(card.left + 17f * dp, card.top + 1.5f * dp,
            card.right - 17f * dp, card.top + 2.5f * dp, 1f * dp, 1f * dp, shapePaint)

        val icon = layout.missionIconRects[index].toRectF(scratchIconRect)
        val iconColor = if (mission.isComplete) KavvoroPalette.gold else accent
        shapePaint.color = withAlpha(iconColor, 34)
        canvas.drawCircle(icon.centerX(), icon.centerY(), icon.width() * 0.52f, shapePaint)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1.25f * dp
        shapePaint.color = withAlpha(iconColor, 190)
        canvas.drawCircle(icon.centerX(), icon.centerY(), icon.width() * 0.43f, shapePaint)
        drawMissionGlyph(canvas, mission.id, icon.centerX(), icon.centerY(), icon.width() * 0.22f, iconColor, dp)
        if (mission.isComplete) {
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = KavvoroPalette.gold
            canvas.drawCircle(icon.right - 2f * dp, icon.top + 2f * dp, 7f * dp, shapePaint)
            checkPath.reset()
            checkPath.moveTo(icon.right - 5f * dp, icon.top + 2f * dp)
            checkPath.lineTo(icon.right - 2.5f * dp, icon.top + 4.5f * dp)
            checkPath.lineTo(icon.right + 1.5f * dp, icon.top - 0.5f * dp)
            shapePaint.style = Paint.Style.STROKE
            shapePaint.strokeWidth = 1.4f * dp
            shapePaint.strokeCap = Paint.Cap.ROUND
            shapePaint.strokeJoin = Paint.Join.ROUND
            shapePaint.color = 0xFF182039.toInt()
            canvas.drawPath(checkPath, shapePaint)
        }

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.textSize = if (layout.gridLayout) 14f * dp else 14f * dp
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.LEFT
        drawFitted(canvas, t(mission.id.titleKey).uppercase(), layout.titleTextRects[index].toRectF(scratchTextRect), textPaint, 8f * dp)

        drawRewardChip(canvas, layout.rewardTextRects[index].toRectF(scratchChipRect), mission.rewardCoins, coinArt, dp, t)

        val progressRect = layout.progressTextRects[index].toRectF(scratchTextRect)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.textSize = 11f * dp
        textPaint.textAlign = if (layout.gridLayout) Paint.Align.CENTER else Paint.Align.LEFT
        textPaint.color = if (mission.isComplete) KavvoroPalette.gold else 0xFFE2E9FA.toInt()
        if (!progressRect.isEmpty) {
            drawFitted(canvas, "${mission.progress} / ${mission.target}", progressRect, textPaint, 8f * dp)
        }

        val status = when {
            mission.claimed -> t("CLAIMED")
            mission.canClaim -> t("READY")
            else -> t("IN PROGRESS")
        }
        val statusRect = layout.statusTextRects[index].toRectF(scratchTextRect)
        if (!statusRect.isEmpty) {
            val statusAccent = when {
                mission.claimed -> KavvoroPalette.mutedText
                mission.canClaim -> KavvoroPalette.cyan
                else -> 0xFF9CA9C7.toInt()
            }
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = withAlpha(statusAccent, 21)
            canvas.drawRoundRect(statusRect, statusRect.height() * 0.5f, statusRect.height() * 0.5f, shapePaint)
            shapePaint.style = Paint.Style.STROKE
            shapePaint.strokeWidth = 0.8f * dp
            shapePaint.color = withAlpha(statusAccent, 110)
            canvas.drawRoundRect(statusRect, statusRect.height() * 0.5f, statusRect.height() * 0.5f, shapePaint)
            textPaint.typeface = AssetResourceManager.oxaniumMedium()
            textPaint.textSize = 8f * dp
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = statusAccent
            drawFitted(canvas, status.uppercase(), statusRect, textPaint, 6f * dp)
        }

        val track = layout.progressTrackRects[index].toRectF(scratchTrackRect)
        if (!track.isEmpty) {
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.style = Paint.Style.FILL
            shapePaint.color = 0xBB020918.toInt()
            canvas.drawRoundRect(track, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
            val fraction = (mission.progress.toFloat() / mission.target.coerceAtLeast(1)).coerceIn(0f, 1f)
            if (fraction > 0f) {
                scratchFillRect.set(track.left, track.top, track.left + track.width() * fraction, track.bottom)
                shapePaint.shader = LinearGradient(scratchFillRect.left, scratchFillRect.top,
                    scratchFillRect.right.coerceAtLeast(scratchFillRect.left + 1f), scratchFillRect.bottom,
                    accent, if (mission.isComplete) KavvoroPalette.gold else KavvoroPalette.blue, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(scratchFillRect, track.height() * 0.5f, track.height() * 0.5f, shapePaint)
                shapePaint.shader = null
            }
        }

        drawClaimButton(canvas, layout.claimButtonRects[index].toRectF(scratchButtonRect), mission, active, dp, t)
    }

    private fun drawRewardChip(canvas: Canvas, rect: RectF, reward: Int, coinArt: Bitmap?, dp: Float, t: (String) -> String) {
        if (rect.isEmpty) return
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = withAlpha(KavvoroPalette.gold, 25)
        canvas.drawRoundRect(rect, rect.height() * 0.5f, rect.height() * 0.5f, shapePaint)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 0.9f * dp
        shapePaint.color = withAlpha(KavvoroPalette.gold, 150)
        canvas.drawRoundRect(rect, rect.height() * 0.5f, rect.height() * 0.5f, shapePaint)
        coinArt?.let { bitmap ->
            scratchFaceRect.set(rect.left + 4f * dp, rect.centerY() - 10f * dp,
                rect.left + 22f * dp, rect.centerY() + 8f * dp)
            shapePaint.reset()
            shapePaint.isAntiAlias = true
            shapePaint.isFilterBitmap = true
            canvas.drawBitmap(bitmap, null, scratchFaceRect, shapePaint)
        } ?: drawCoinGlyph(canvas, rect.left + 13f * dp, rect.centerY(), 5.7f * dp, KavvoroPalette.gold, dp)
        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 8.7f * dp
        textPaint.color = KavvoroPalette.gold
        scratchFaceRect.set(rect.left + 24f * dp, rect.top, rect.right - 5f * dp, rect.bottom)
        drawFitted(canvas, "+$reward ${t("COINS").uppercase()}", scratchFaceRect, textPaint, 6f * dp)
    }

    private fun drawMissionGlyph(canvas: Canvas, mission: MissionId, x: Float, y: Float, size: Float, color: Int, dp: Float) {
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = color
        when (mission) {
            MissionId.DAILY_CLEAR_LEVELS, MissionId.RIFT_CLASSIC_LEVELS -> {
                glyphPath.reset()
                glyphPath.moveTo(x - size * 0.55f, y - size * 0.78f)
                glyphPath.lineTo(x + size * 0.8f, y)
                glyphPath.lineTo(x - size * 0.55f, y + size * 0.78f)
                glyphPath.close()
                canvas.drawPath(glyphPath, shapePaint)
            }
            MissionId.DAILY_A_RANKS, MissionId.RIFT_CHAOS_LEVELS -> {
                glyphPath.reset()
                for (point in 0 until 10) {
                    val angle = Math.PI * point / 5.0 - Math.PI / 2.0
                    val radius = if (point % 2 == 0) size * 0.84f else size * 0.38f
                    val px = x + kotlin.math.cos(angle).toFloat() * radius
                    val py = y + kotlin.math.sin(angle).toFloat() * radius
                    if (point == 0) glyphPath.moveTo(px, py) else glyphPath.lineTo(px, py)
                }
                glyphPath.close()
                canvas.drawPath(glyphPath, shapePaint)
            }
            MissionId.DAILY_RIFT_BREAKS, MissionId.RIFT_BREAKS -> {
                glyphPath.reset()
                glyphPath.moveTo(x, y - size)
                glyphPath.lineTo(x + size * 0.72f, y)
                glyphPath.lineTo(x, y + size)
                glyphPath.lineTo(x - size * 0.72f, y)
                glyphPath.close()
                canvas.drawPath(glyphPath, shapePaint)
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = maxOf(1.2f * dp, size * 0.16f)
                canvas.drawLine(x - size * 1.12f, y, x + size * 1.12f, y, shapePaint)
                canvas.drawLine(x, y - size * 1.12f, x, y + size * 1.12f, shapePaint)
            }
            MissionId.RIFT_CHAIN_COMBOS -> {
                shapePaint.style = Paint.Style.STROKE
                shapePaint.strokeWidth = maxOf(1.5f * dp, size * 0.19f)
                canvas.drawCircle(x - size * 0.38f, y, size * 0.5f, shapePaint)
                canvas.drawCircle(x + size * 0.38f, y, size * 0.5f, shapePaint)
            }
        }
    }

    private fun drawCoinGlyph(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int, dp: Float) {
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = withAlpha(color, 58)
        canvas.drawCircle(x, y, radius, shapePaint)
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = maxOf(1.5f * dp, radius * 0.2f)
        shapePaint.color = color
        canvas.drawCircle(x, y, radius, shapePaint)
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = color
        canvas.drawCircle(x, y, radius * 0.18f, shapePaint)
    }

    private fun drawClaimButton(canvas: Canvas, button: RectF, mission: MissionProgress, active: Boolean, dp: Float, t: (String) -> String) {
        if (button.isEmpty) return
        val radius = button.height() * 0.46f
        val pressedOffset = if (active && mission.canClaim) 2f * dp else 0f
        shapePaint.reset()
        shapePaint.isAntiAlias = true
        shapePaint.style = Paint.Style.FILL
        if (mission.canClaim) {
            scratchShadowRect.set(button.left, button.top + 3f * dp, button.right, button.bottom + 3f * dp)
            shapePaint.color = 0xB506102A.toInt()
            canvas.drawRoundRect(scratchShadowRect, radius, radius, shapePaint)
        }
        if (mission.canClaim) {
            scratchFaceRect.set(button.left, button.top + pressedOffset, button.right, button.bottom - 2f * dp + pressedOffset)
        } else {
            val statusWidth = min(button.width() * 0.76f, 126f * dp)
            val statusHeight = min(button.height() * 0.72f, 32f * dp)
            scratchFaceRect.set(button.centerX() - statusWidth * 0.5f, button.centerY() - statusHeight * 0.5f,
                button.centerX() + statusWidth * 0.5f, button.centerY() + statusHeight * 0.5f)
        }
        if (mission.canClaim) {
            shapePaint.shader = LinearGradient(scratchFaceRect.left, scratchFaceRect.top,
                scratchFaceRect.right, scratchFaceRect.bottom,
                0xFF25D9F4.toInt(), 0xFF825BFF.toInt(), Shader.TileMode.CLAMP)
        } else {
            shapePaint.color = if (mission.claimed) 0x334CCFC1 else 0x33425476
        }
        canvas.drawRoundRect(scratchFaceRect, radius, radius, shapePaint)
        shapePaint.shader = null
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = if (mission.canClaim) 1f * dp else 0.75f * dp
        shapePaint.color = withAlpha(if (mission.canClaim) 0xFFE9FCFF.toInt() else KavvoroPalette.mutedText,
            if (mission.canClaim) 190 else 74)
        canvas.drawRoundRect(scratchFaceRect, radius, radius, shapePaint)

        textPaint.reset()
        textPaint.isAntiAlias = true
        textPaint.typeface = AssetResourceManager.spaceGroteskBold()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = min(9f * dp, button.height() * 0.25f)
        textPaint.color = if (mission.canClaim) 0xFFFFFFFF.toInt() else 0xFFB3BED9.toInt()
        val label = when {
            mission.claimed -> t("CLAIMED")
            mission.canClaim -> t("CLAIM REWARD")
            else -> t("IN PROGRESS")
        }
        drawFitted(canvas, label.uppercase(), scratchFaceRect, textPaint, 6f * dp)
    }

    private fun baselineFor(rect: RectF, paint: Paint): Float =
        rect.centerY() - (paint.ascent() + paint.descent()) * 0.5f

    private fun drawFitted(canvas: Canvas, value: String, rect: RectF, paint: Paint, minimumTextSize: Float) {
        if (rect.isEmpty) return
        val originalSize = paint.textSize
        val maxWidth = rect.width().coerceAtLeast(1f)
        while (paint.textSize > minimumTextSize && paint.measureText(value) > maxWidth) {
            paint.textSize -= 0.5f
        }
        val x = when (paint.textAlign) {
            Paint.Align.CENTER -> rect.centerX()
            Paint.Align.RIGHT -> rect.right
            else -> rect.left
        }
        canvas.drawText(value, x, baselineFor(rect, paint), paint)
        paint.textSize = originalSize
    }

    private fun Int.formatGrouped(): String = String.format(Locale.getDefault(), "%,d", this)
}
