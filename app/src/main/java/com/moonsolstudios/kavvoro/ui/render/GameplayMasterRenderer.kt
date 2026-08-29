package com.moonsolstudios.kavvoro.ui.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.NextReward
import com.moonsolstudios.kavvoro.model.Screen

object GameplayMasterRenderer {

    data class GameplayRenderState(
        val screen: Screen,
        val state: GameState,
        val gameMode: GameMode,
        val level: LevelSpec,
        val ball: Point2,
        val riftEnergy: Float,
        val riftActive: Boolean,
        val riftAnchor: Point2?,
        val riftHoldSeconds: Float,
        val stateElapsed: Float,
        val simElapsed: Float,
        val chainCount: Int,
        val maxChain: Int,
        val pulseIntensity: Float,
        val finishPulse: Float,
        val riftBreakTimer: Float,
        val lastRiftBreak: Boolean,
        val lastRiftBreakBonus: Int,
        val lastRiftBreakReason: String,
        val lastScore: RunScore?,
        val lastHypeScore: Int,
        val streak: Int,
        val powerMessage: String,
        val powerMessageTimer: Float,
        val rewardMessage: String,
        val flash: Float,
        val exportingShare: Boolean,
        val screenTransitionTimer: Float,
        val screenTransitionAccent: Int,
        val selectedSkin: BallSkin,
        val totalSkins: Int,
        val playerLine: List<Point2>,
        val liveBallTrail: List<Point2>,
        val replayFrames: List<PhysicsFrame>,
        val nextRewardText: String?,
        val nextRewardInfo: NextReward?,
        val continueRequiresAd: Boolean,
        val viewWidth: Float,
        val viewHeight: Float,
        val safeInsetLeft: Float,
        val safeInsetRight: Float,
        val safeInsetTop: Float,
        val safeInsetBottom: Float,
        val safeContentWidth: Float,
        val safeCenterX: Float,
        val stageLeft: Float,
        val scale: Float,
        val menuPulse: Float,
        val adaptiveQuality: Float,
        val performanceLite: Boolean,
        val richEffects: Boolean,
        val fullEffects: Boolean,
        val isRtl: Boolean,
        val activeButton: ButtonId,
        val homeButton: RectF,
        val restartButton: RectF,
        val sfxButton: RectF,
        val musicButton: RectF,
        val shareButton: RectF,
        val nextButton: RectF,
        val resultNextButton: RectF,
        val resultRetryButton: RectF,
        val resultShareButton: RectF,
        val tutorialCardVisible: Boolean = false,
        val lessonLines: List<String> = emptyList(),
        val obstacleLine: String = "",
        val tutorialCardBounds: RectF,
        val tutorialStartButton: RectF
    )

    fun drawGameplay(
        canvas: Canvas,
        state: GameplayRenderState,
        context: Context,
        paint: Paint,
        textPaint: Paint,
        dp: Float,
        sx: (Float) -> Float,
        sy: (Float) -> Float,
        worldToScreen: (Float) -> Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawFittedText: (Canvas, String, Float, Float, Float, Float, Float) -> Unit,
        levelHasCurse: (CurseType) -> Boolean,
        localizedLevelTitle: (String) -> String,
        formatScoreTime: (Float) -> String,
        formatHypeAmount: (Int) -> String,
        formatTimeRemaining: (Float) -> String,
        ballPowerName: (BallPower) -> String,
        ballPowerDescription: (BallPower) -> String,
        powerIconKey: (BallPower) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawIconButton: (Canvas, RectF, ButtonId) -> Unit,
        drawUiButtonFrame: (Canvas, RectF, Boolean, Int, Float) -> Unit,
        drawUiIconAsset: (Canvas, String, RectF, Float, Int) -> Unit,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit
    ) {
        val level = state.level
        val skin = state.selectedSkin
        val compactHud = state.viewWidth < 520f * dp
        val hasRibbon = skin.power != BallPower.NONE || level.curses.isNotEmpty()
        val hudTop = state.safeInsetTop + (if (compactHud) 8f else 12f) * dp
        val hudLeft = state.safeInsetLeft + (if (compactHud) 12f else 16f) * dp
        val energyTop = hudTop + (if (compactHud && hasRibbon) 78f else if (compactHud) 54f else 114f) * dp

        val baseBottom = state.safeInsetTop + (if (compactHud) {
            if (hasRibbon) 132f else 104f
        } else {
            156f
        }) * dp
        val titleHeight = (if (compactHud) 22f else 28f) * dp
        val titleBottomInset = (if (compactHud) 7f else 9f) * dp
        val requiredBottom = com.moonsolstudios.kavvoro.ui.TutorialCardLayout.minimumHudBottom(
            energyTop = energyTop,
            energyHeight = 13f * dp,
            titleHeight = titleHeight,
            titleBottomInset = titleBottomInset,
            minimumGap = 12f * dp
        )
        val gameplayHudBottom = maxOf(baseBottom, requiredBottom)
        val gameplayOverlayTop = gameplayHudBottom + 8f * dp
        val dockButtons = listOf(state.musicButton, state.sfxButton, state.restartButton, state.homeButton)
        val controlsLeft = dockButtons.filter { !it.isEmpty }.minOfOrNull { it.left } ?: state.viewWidth
        val timeRemaining = (level.timeLimitSeconds - state.simElapsed).coerceAtLeast(0f)
        val energyWidth = kotlin.math.min(state.viewWidth - (if (compactHud) 88f else 42f) * dp, (if (compactHud) 230f else 320f) * dp)

        // 1. Curse Atmosphere
        AtmosphereRenderer.drawCurseAtmosphere(
            canvas = canvas,
            spec = level,
            richEffects = state.richEffects,
            stateElapsed = state.stateElapsed,
            viewWidth = state.viewWidth,
            viewHeight = state.viewHeight,
            state = state.state,
            riftEnergy = state.riftEnergy,
            paint = paint,
            dp = dp
        )

        // 2. Pulse Zones
        PulseZoneRenderer.drawPulseZones(
            canvas = canvas,
            pulseZones = level.pulseZones,
            accent = level.accent,
            stateElapsed = state.stateElapsed,
            isSimulating = state.state == GameState.SIMULATING,
            ballDistanceTo = { center -> state.ball.distanceTo(center) },
            sx = sx,
            sy = sy,
            worldToScreen = worldToScreen,
            richEffects = state.richEffects,
            performanceLite = state.performanceLite,
            fullEffects = state.fullEffects,
            levelIndex = level.index,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            drawWorldAsset = drawWorldAsset
        )

        // 3. World Entities (Hazards, Portals, Blocks, Reactor)
        GameplayWorldRenderer.drawWorldEntities(
            canvas = canvas,
            level = level,
            isChaos = state.gameMode == GameMode.CHAOS,
            isReady = state.state == GameState.READY,
            stateElapsed = state.stateElapsed,
            simElapsed = state.simElapsed,
            richEffects = state.richEffects,
            performanceLite = state.performanceLite,
            sx = sx,
            sy = sy,
            worldToScreen = worldToScreen,
            paint = paint,
            dp = dp,
            drawWorldAsset = drawWorldAsset
        )

        // 4. Route Coach
        TutorialOverlayRenderer.drawRouteCoach(
            canvas = canvas,
            state = state.state,
            level = level,
            stateElapsed = state.stateElapsed,
            menuPulse = state.menuPulse,
            accent = skin.lineColor,
            isCompactHud = compactHud,
            viewWidth = state.viewWidth,
            viewHeight = state.viewHeight,
            gameplayOverlayTop = gameplayOverlayTop,
            paint = paint,
            dp = dp,
            sx = sx,
            sy = sy,
            worldToScreen = worldToScreen,
            t = t,
            levelHasCurse = levelHasCurse
        )

        // 5. Replay Tail
        BallSkinRenderer.drawReplayTail(
            canvas = canvas,
            replayFrames = state.replayFrames,
            isSimulating = state.state == GameState.SIMULATING,
            performanceLite = state.performanceLite,
            skinLineColor = skin.lineColor,
            sx = sx,
            sy = sy,
            paint = paint,
            dp = dp
        )

        // 6. Rift Trail
        BallSkinRenderer.drawRiftTrail(
            canvas = canvas,
            playerLine = state.playerLine,
            performanceLite = state.performanceLite,
            skinLineColor = skin.lineColor,
            sx = sx,
            sy = sy,
            paint = paint,
            dp = dp
        )

        // 7. Drawing Assist
        RiftAssistRenderer.drawDrawingAssist(
            canvas = canvas,
            isSimulating = state.state == GameState.SIMULATING,
            riftActive = state.riftActive,
            anchorScreenX = state.riftAnchor?.let { sx(it.x) },
            anchorScreenY = state.riftAnchor?.let { sy(it.y) },
            ballScreenX = sx(state.ball.x),
            ballScreenY = sy(state.ball.y),
            skinLineColor = skin.lineColor,
            riftEnergy = state.riftEnergy,
            stateElapsed = state.stateElapsed,
            riftHoldSeconds = state.riftHoldSeconds,
            hasFocusField = levelHasCurse(CurseType.FOCUS_FIELD),
            hasPowerHold = levelHasCurse(CurseType.POWER_HOLD),
            hasOverheat = levelHasCurse(CurseType.OVERHEAT),
            hasRiftWind = levelHasCurse(CurseType.RIFT_WIND),
            hasPulseStorm = levelHasCurse(CurseType.PULSE_STORM),
            levelIndex = level.index,
            simElapsed = state.simElapsed,
            viewWidth = state.viewWidth,
            viewHeight = state.viewHeight,
            performanceLite = state.performanceLite,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )

        // 8. Ball
        BallSkinRenderer.drawGameplayBall(
            canvas = canvas,
            ballX = state.ball.x,
            ballY = state.ball.y,
            ballRadiusWorld = com.moonsolstudios.kavvoro.engine.PhysicsEngine.BALL_RADIUS,
            skin = skin,
            skinIndex = 0,
            totalSkins = state.totalSkins,
            liveBallTrail = state.liveBallTrail,
            pulseIntensity = state.pulseIntensity,
            isSimulating = state.state == GameState.SIMULATING,
            hasPulseStorm = levelHasCurse(CurseType.PULSE_STORM),
            levelAccent = level.accent,
            chainCount = state.chainCount,
            pulse = state.menuPulse,
            richEffects = state.richEffects,
            performanceLite = state.performanceLite,
            adaptiveQuality = state.adaptiveQuality,
            viewWidth = state.viewWidth,
            viewHeight = state.viewHeight,
            artBitmap = AssetResourceManager.brainballBitmap(skin, context.resources),
            paint = paint,
            dp = dp,
            sx = sx,
            sy = sy,
            worldToScreen = worldToScreen,
            t = t,
            fitText = fitText,
            powerIconKey = powerIconKey,
            drawWorldAsset = drawWorldAsset
        )

        // 9. HUD
        GameplayOverlayRenderer.drawHud(
            canvas = canvas,
            compactHud = compactHud,
            isRtl = state.isRtl,
            top = hudTop,
            left = hudLeft,
            controlsLeft = controlsLeft,
            hasRibbon = hasRibbon,
            toolbarBottom = gameplayHudBottom,
            viewWidth = state.viewWidth,
            levelAccent = level.accent,
            levelDifficultyRating = level.difficultyRating,
            levelIndex = level.index,
            gameModeTitle = state.gameMode.menuTitle(),
            riftEnergy = state.riftEnergy,
            timeRemaining = timeRemaining,
            hudHype = state.lastHypeScore,
            chainCount = state.chainCount,
            energyTop = energyTop,
            energyWidth = energyWidth,
            power = skin.power,
            skinLineColor = skin.lineColor,
            curses = level.curses,
            dockButtons = dockButtons,
            musicButton = state.musicButton,
            sfxButton = state.sfxButton,
            homeButton = state.homeButton,
            restartButton = state.restartButton,
            shareButton = state.shareButton,
            nextButton = state.nextButton,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            formatHypeAmount = formatHypeAmount,
            formatTimeRemaining = formatTimeRemaining,
            drawIconButton = { c, rect, idOrdinal ->
                val id = when (idOrdinal) {
                    1 -> ButtonId.MUSIC
                    2 -> ButtonId.SFX
                    3 -> ButtonId.RESTART
                    4 -> ButtonId.HOME
                    5 -> ButtonId.SHARE
                    6 -> ButtonId.NEXT
                    else -> ButtonId.HOME
                }
                drawIconButton(c, rect, id)
            },
            drawWorldAsset = drawWorldAsset,
            ballPowerName = ballPowerName,
            powerIconKey = powerIconKey
        )

        // 10. Level Name Glass
        val stageRight = state.stageLeft + com.moonsolstudios.kavvoro.engine.STAGE_WIDTH * state.scale
        val title = "${state.gameMode.menuTitle()} - L${level.index.toString().padStart(2, '0')} ${localizedLevelTitle(level.title)}"
        GameplayOverlayRenderer.drawLevelNameGlass(
            canvas = canvas,
            stageLeft = state.stageLeft,
            stageRight = stageRight,
            viewWidth = state.viewWidth,
            safeTop48 = state.safeInsetTop + 48f * dp,
            compactHud = compactHud,
            energyTop = energyTop,
            gameplayHudBottom = gameplayHudBottom,
            accent = level.accent,
            titleText = title,
            paint = paint,
            dp = dp,
            drawFittedText = drawFittedText
        )

        // 11. Mission Brief
        val warning = MissionBriefRenderer.currentModeWarning(level) { c -> levelHasCurse(c) }
        MissionBriefRenderer.drawMissionBrief(
            canvas = canvas,
            state = state.state,
            stateElapsed = state.stateElapsed,
            level = level,
            gameModeTitle = state.gameMode.menuTitle(),
            warning = warning,
            selectedSkin = skin,
            archetypeLabel = "",
            archetypeDetail = "",
            safeContentWidth = state.safeContentWidth,
            safeCenterX = state.safeCenterX,
            overlayTop = gameplayOverlayTop,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            localizedLevelTitle = localizedLevelTitle,
            ballPowerName = ballPowerName,
            ballPowerDescription = ballPowerDescription,
            drawWorldAsset = drawWorldAsset
        )

        // 12. Power Toast
        if (state.powerMessageTimer > 0f && state.powerMessage.isNotBlank()) {
            val toastWidth = kotlin.math.min(state.safeContentWidth - 32f * dp, 360f * dp)
            val toastHeight = 48f * dp
            val toastLeft = state.safeCenterX - toastWidth * 0.5f
            val toastTop = if (state.state == GameState.READY && state.stateElapsed <= 3.6f) {
                gameplayOverlayTop + (if (level.tutorialHint.isNotBlank()) 126f else 96f) * dp
            } else {
                gameplayOverlayTop
            }
            GameplayOverlayRenderer.drawPowerToast(
                canvas = canvas,
                powerMessage = state.powerMessage,
                skin = skin,
                left = toastLeft,
                top = toastTop,
                width = toastWidth,
                height = toastHeight,
                paint = paint,
                dp = dp,
                t = t,
                fitText = fitText,
                drawWorldAsset = drawWorldAsset,
                powerIconKey = powerIconKey,
                ballPowerName = ballPowerName
            )
        }

        // 13. Tutorial Hint
        TutorialOverlayRenderer.drawTutorialHint(
            canvas = canvas,
            tutorialCardVisible = state.tutorialCardVisible,
            tutorialCardBounds = state.tutorialCardBounds,
            tutorialStartButton = state.tutorialStartButton,
            safeContentWidth = state.safeContentWidth,
            safeInsetLeft = state.safeInsetLeft,
            bottom34 = state.viewHeight - state.safeInsetBottom - 34f * dp,
            accent = warning?.accent ?: level.accent,
            level = level,
            lessonLines = state.lessonLines,
            obstacleLine = state.obstacleLine,
            actionPressed = false,
            context = context,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText,
            drawFittedText = drawFittedText,
            drawWorldAsset = drawWorldAsset,
            tutorialIconKey = { "tutorial_start" },
            levelHasCurse = levelHasCurse
        )

        // 14. Finish Burst
        if (state.finishPulse > 0f) {
            val won = state.state == GameState.WON
            val burstProgress = 1f - state.finishPulse
            val cx = sx(state.ball.x)
            val cy = sy(state.ball.y)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = (2.2f + (1f - burstProgress) * 4.2f) * dp
            paint.color = ((if (won) level.accent else 0xFFFF4D8D.toInt()) and 0x00FFFFFF) or
                (((180f * state.finishPulse).toInt().coerceIn(0, 255)) shl 24)
            canvas.drawCircle(cx, cy, (22f + burstProgress * 68f) * dp, paint)
        }

        // 15. Rift Break Moment
        MissionBriefRenderer.drawRiftBreakMoment(
            canvas = canvas,
            state = state.state,
            lastRiftBreak = state.lastRiftBreak,
            riftBreakTimer = state.riftBreakTimer,
            ballScreenX = sx(state.ball.x),
            ballScreenY = sy(state.ball.y),
            accent = skin.lineColor,
            lastRiftBreakReason = state.lastRiftBreakReason,
            lastRiftBreakBonus = state.lastRiftBreakBonus,
            safeInsetLeft = state.safeInsetLeft,
            safeInsetRight = state.safeInsetRight,
            safeContentWidth = state.safeContentWidth,
            safeCenterX = state.safeCenterX,
            viewWidth = state.viewWidth,
            viewHeight = state.viewHeight,
            safeTop138 = state.safeInsetTop + 138f * dp,
            safeBottom260 = state.viewHeight - state.safeInsetBottom - 260f * dp,
            safeTop168 = state.safeInsetTop + 168f * dp,
            safeBottom160 = state.viewHeight - state.safeInsetBottom - 160f * dp,
            performanceLite = state.performanceLite,
            paint = paint,
            dp = dp,
            t = t,
            fitText = fitText
        )

        // 16. Outcome Screen
        GameplayOutcomeRenderer.drawOutcome(
            canvas = canvas,
            state = state.state,
            level = level,
            gameModeTitle = state.gameMode.menuTitle(t),
            gameplayHudBottom = gameplayHudBottom,
            safeContentWidth = state.safeContentWidth,
            safeInsetLeft = state.safeInsetLeft,
            safeBottom16 = state.safeInsetBottom + 16f * dp,
            viewWidth = state.viewWidth,
            viewHeight = state.viewHeight,
            selectedSkin = skin,
            archetypeLabel = "",
            streak = state.streak,
            lastScore = state.lastScore,
            lastHypeScore = state.lastHypeScore,
            maxChain = state.maxChain,
            lastRiftBreak = state.lastRiftBreak,
            lastRiftBreakBonus = state.lastRiftBreakBonus,
            lastRiftBreakReason = state.lastRiftBreakReason,
            rewardMessage = state.rewardMessage,
            nextRewardText = state.nextRewardText,
            nextRewardInfo = state.nextRewardInfo,
            continueRequiresAd = state.continueRequiresAd,
            resultShareButton = state.resultShareButton,
            resultNextButton = state.resultNextButton,
            resultRetryButton = state.resultRetryButton,
            activeButton = state.activeButton,
            isRtl = state.isRtl,
            paint = paint,
            dp = dp,
            formatScoreTime = formatScoreTime,
            t = t,
            fitText = fitText,
            localizedLevelTitle = localizedLevelTitle,
            drawBallSkin = drawBallSkin,
            drawWorldAsset = drawWorldAsset
        )
    }
}
