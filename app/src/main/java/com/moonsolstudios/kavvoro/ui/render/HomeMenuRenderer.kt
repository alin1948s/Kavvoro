package com.moonsolstudios.kavvoro.ui.render

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.LayoutMode
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import kotlin.math.min
import kotlin.math.sin

/**
 * Procedural renderer for brand header, play mode selector, and menu preview scene.
 */
object HomeMenuRenderer {

    private val scratch = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawBrandTitle(
        canvas: Canvas,
        menuState: MenuState,
        left: Float,
        top: Float,
        menuContentRight: Float,
        menuPrivacyButtonLeft: Float,
        selectedSkinLineColor: Int,
        homeLayoutScale: Float,
        homeLayoutMode: LayoutMode,
        homeContentWidth: Float,
        isRtl: Boolean,
        paint: Paint,
        dp: Float,
        fitText: (String, Float) -> String,
        worldBitmap: (String) -> Bitmap?
    ) {
        if (menuState == MenuState.MODES || menuState == MenuState.MODE_ACTION) {
            val right = menuContentRight
            val titleX = if (isRtl) right else left
            val brand = worldBitmap("brand_kavvoro")
            if (brand != null) {
                val logoWidth = when (homeLayoutMode) {
                    LayoutMode.COMPACT -> homeContentWidth * 0.47f
                    LayoutMode.MEDIUM -> homeContentWidth * 0.43f
                    LayoutMode.TABLET -> homeContentWidth * 0.36f
                }
                val logoHeight = logoWidth * brand.height / brand.width.toFloat()
                val brandLeft = if (isRtl) right - logoWidth else titleX
                scratch.set(brandLeft, top, brandLeft + logoWidth, top + logoHeight)
                paint.alpha = 255
                paint.isFilterBitmap = true
                canvas.drawBitmap(brand, null, scratch, paint)
                return
            }
            textPaint.shader = null
            textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
            textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
            val scale = homeLayoutScale
            val maxBrandW = menuPrivacyButtonLeft - titleX - 10f * dp
            val brandTop = top + 4f * scale * dp

            // Glowing cyan dot
            val dotR = 2.6f * scale * dp
            val dotX = titleX + dotR
            val dotY = brandTop - 3.8f * scale * dp
            paint.style = Paint.Style.FILL
            paint.color = 0xFF00E5FF.toInt()
            canvas.drawCircle(dotX, dotY, dotR, paint)

            val textStartX = dotX + dotR + 5f * scale * dp
            textPaint.textSize = (10.5f * scale).coerceIn(8.5f, 14f) * dp
            val first = "BRAINROT"
            val second = "CHAOS"
            textPaint.color = 0xFF00E5FF.toInt()
            canvas.drawText(first, textStartX, brandTop, textPaint)
            val firstWidth = textPaint.measureText(first)
            val secondX = if (isRtl) textStartX - firstWidth - 8f * scale * dp else textStartX + firstWidth + 8f * scale * dp
            textPaint.color = 0xFFFF2E93.toInt()
            canvas.drawText(second, secondX, brandTop, textPaint)

            textPaint.textSize = (31f * scale).coerceIn(24f, 46f) * dp
            textPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawText(fitText("KAVVORO", maxBrandW), titleX, top + 38f * scale * dp, textPaint)
            return
        }
        val right = menuContentRight
        val titleX = if (isRtl) right else left
        textPaint.shader = null
        textPaint.textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 11f * dp
        textPaint.color = 0xFFFF4D8D.toInt()
        canvas.drawText("BRAINROT CHAOS", titleX, top - 25f * dp, textPaint)

        textPaint.textSize = 39f * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText("KAVVORO", titleX, top + 9f * dp, textPaint)

        paint.style = Paint.Style.FILL
        paint.color = selectedSkinLineColor
        val accentLeft = if (isRtl) right - 78f * dp else left
        canvas.drawRoundRect(accentLeft, top + 18f * dp, accentLeft + 78f * dp, top + 22f * dp, 2f * dp, 2f * dp, paint)
        paint.color = 0xFFFFCF4A.toInt()
        val secondaryLeft = if (isRtl) right - 122f * dp else left + 84f * dp
        canvas.drawRoundRect(secondaryLeft, top + 18f * dp, secondaryLeft + 38f * dp, top + 22f * dp, 2f * dp, 2f * dp, paint)
    }

    fun drawHomeFooterNote(
        canvas: Canvas,
        scale: Float,
        centerX: Float,
        viewHeight: Float,
        safeInsetBottom: Float,
        dp: Float,
        t: (String) -> String
    ) {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.NORMAL)
        textPaint.textSize = ((8f * scale).coerceIn(7f, 11f)) * dp
        textPaint.color = 0x889AA8BA.toInt()
        canvas.drawText(t("Collection, language and privacy are available from Settings."), centerX, viewHeight - safeInsetBottom - 6f * scale * dp, textPaint)
    }

    fun drawPlayModeScreen(
        canvas: Canvas,
        menuClassicCard: RectF,
        menuChaosCard: RectF,
        menuClassicContinueButton: RectF,
        menuClassicNewButton: RectF,
        menuChaosContinueButton: RectF,
        menuChaosNewButton: RectF,
        menuChaosStartButton: RectF,
        menuContinueButton: RectF,
        activeMenuButton: MenuButton,
        classicProgress: Int,
        chaosProgress: Int,
        classicStreak: Int,
        chaosStreak: Int,
        compact: Boolean,
        short: Boolean,
        safeCenterX: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        menuButtonAt: (Float, Float) -> MenuButton
    ) {
        val headingBottom = menuClassicCard.top - (if (compact) 12f else 18f) * dp
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = (if (compact) 25f else 34f) * dp
        textPaint.color = 0xFFF7F4FF.toInt()
        canvas.drawText(t("PLAY").uppercase(), safeCenterX, headingBottom - (if (compact) 18f else 26f) * dp, textPaint)
        textPaint.textSize = (if (compact) 7.5f else 10f) * dp
        textPaint.color = 0xB8D2DCE8.toInt()
        canvas.drawText(t("CHOOSE MODE").uppercase(), safeCenterX, headingBottom, textPaint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF1DE8C8.toInt()
        canvas.drawCircle(safeCenterX - 70f * dp, headingBottom - 3f * dp, 2f * dp, paint)
        paint.color = 0xFFFF4D8D.toInt()
        canvas.drawCircle(safeCenterX + 70f * dp, headingBottom - 3f * dp, 2f * dp, paint)

        val classicDetail = "${t("STANDARD RULES").uppercase()}  •  ${t("STEADY PROGRESSION").lowercase()}"
        ModePickerRenderer.drawModeCard(
            canvas = canvas,
            rect = menuClassicCard,
            title = t("CLASSIC").uppercase(),
            description = classicDetail,
            accent = 0xFF1DE8C8.toInt(),
            active = activeMenuButton == MenuButton.CLASSIC,
            activeRun = classicProgress > 1,
            levelText = "${t("LEVEL").uppercase()} ${classicProgress.toString().padStart(2, '0')}",
            streakText = classicStreak.toString(),
            activeRunLabel = t("ACTIVE RUN").uppercase(),
            noActiveRunLabel = t("NO ACTIVE RUN").uppercase(),
            bestStreakLabel = t("BEST STREAK").uppercase(),
            startFreshLabel = t("START FRESH WHEN READY").uppercase(),
            continueLabel = t("CONTINUE").uppercase(),
            newGameLabel = t("NEW GAME").uppercase(),
            startLabel = t("START NEW GAME").uppercase(),
            compact = compact,
            short = short,
            continueButton = menuClassicContinueButton,
            newGameButton = menuClassicNewButton,
            startButton = menuClassicNewButton,
            activeButtonId = activeMenuButton.ordinal,
            continueButtonId = if (!menuClassicContinueButton.isEmpty) menuButtonAt(menuClassicContinueButton.centerX(), menuClassicContinueButton.centerY()).ordinal else -1,
            newGameButtonId = if (!menuClassicNewButton.isEmpty) menuButtonAt(menuClassicNewButton.centerX(), menuClassicNewButton.centerY()).ordinal else -1,
            startButtonId = if (!menuClassicNewButton.isEmpty) menuButtonAt(menuClassicNewButton.centerX(), menuClassicNewButton.centerY()).ordinal else -1,
            paint = paint,
            dp = dp,
            fitText = fitText,
            drawIcon = { c, r -> drawWorldAsset(c, "portal_goal", r, 235) }
        )

        val chaosDetail = "${t("WILD MODIFIERS").uppercase()}  •  ${t("HIGHER INTENSITY").lowercase()}"
        ModePickerRenderer.drawModeCard(
            canvas = canvas,
            rect = menuChaosCard,
            title = t("CHAOS").uppercase(),
            description = chaosDetail,
            accent = 0xFFFF4D8D.toInt(),
            active = activeMenuButton == MenuButton.CHAOS,
            activeRun = chaosProgress > 1,
            levelText = "${t("LEVEL").uppercase()} ${chaosProgress.toString().padStart(2, '0')}",
            streakText = chaosStreak.toString(),
            activeRunLabel = t("ACTIVE RUN").uppercase(),
            noActiveRunLabel = t("NO ACTIVE RUN").uppercase(),
            bestStreakLabel = t("BEST STREAK").uppercase(),
            startFreshLabel = t("START FRESH WHEN READY").uppercase(),
            continueLabel = t("CONTINUE").uppercase(),
            newGameLabel = t("NEW GAME").uppercase(),
            startLabel = t("START NEW GAME").uppercase(),
            compact = compact,
            short = short,
            continueButton = menuChaosContinueButton,
            newGameButton = menuChaosNewButton,
            startButton = menuChaosStartButton,
            activeButtonId = activeMenuButton.ordinal,
            continueButtonId = if (!menuChaosContinueButton.isEmpty) menuButtonAt(menuChaosContinueButton.centerX(), menuChaosContinueButton.centerY()).ordinal else -1,
            newGameButtonId = if (!menuChaosNewButton.isEmpty) menuButtonAt(menuChaosNewButton.centerX(), menuChaosNewButton.centerY()).ordinal else -1,
            startButtonId = if (!menuChaosStartButton.isEmpty) menuButtonAt(menuChaosStartButton.centerX(), menuChaosStartButton.centerY()).ordinal else -1,
            paint = paint,
            dp = dp,
            fitText = fitText,
            drawIcon = { c, r -> drawWorldAsset(c, "hazard_glitch", r, 235) }
        )

        ModePickerRenderer.drawBackButton(
            canvas = canvas,
            rect = menuContinueButton,
            label = t("BACK TO HOME").uppercase(),
            short = short,
            paint = paint,
            dp = dp
        )
    }

    fun drawMenuPreview(
        canvas: Canvas,
        homePreview: Boolean,
        menuPulse: Float,
        selectedMenuMode: GameMode,
        skin: BallSkin,
        scale: Float,
        compactMenuViewport: Boolean,
        menuBallDragging: Boolean,
        menuBallOffsetX: Float,
        menuBallOffsetY: Float,
        menuPreviewCenterX: Float,
        menuPreviewCenterY: Float,
        menuPreviewRadius: Float,
        menuPreviewBounds: RectF,
        portalBackRect: RectF,
        platformRect: RectF,
        characterRect: RectF,
        portalFrontRect: RectF,
        heroRect: RectF,
        menuStartButtonTop: Float,
        menuActionStartButtonTop: Float,
        safeContentWidth: Float,
        safeInsetLeft: Float,
        safeInsetRight: Float,
        viewWidth: Float,
        richEffects: Boolean,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        worldBitmap: (String) -> Bitmap?,
        brainballBitmap: (BallSkin) -> Bitmap?,
        drawBallSkin: (Canvas, Float, Float, Float, BallSkin, Boolean, Boolean) -> Unit,
        drawWorldAsset: (Canvas, String, RectF, Int) -> Unit,
        drawRiftOnlineBadge: (Canvas, Float, Float, Float, String) -> Unit
    ) {
        if (homePreview) {
            // 1. BACK PORTAL
            val portalBackBitmap = worldBitmap("home_portal_back")
            if (portalBackBitmap != null) {
                paint.alpha = 255
                paint.isFilterBitmap = true
                canvas.drawBitmap(portalBackBitmap, null, portalBackRect, paint)
            }

            // 2. PLATFORM
            val platformBitmap = worldBitmap("home_portal_platform")
            if (platformBitmap != null) {
                paint.alpha = 255
                paint.isFilterBitmap = true
                canvas.drawBitmap(platformBitmap, null, platformRect, paint)
            }

            // 3. SELECTED BRAINBALL
            val characterBitmap = brainballBitmap(skin)
            if (characterBitmap != null) {
                paint.alpha = 255
                paint.isFilterBitmap = true
                canvas.drawBitmap(characterBitmap, null, characterRect, paint)
            } else {
                drawBallSkin(
                    canvas,
                    characterRect.centerX(),
                    characterRect.centerY(),
                    characterRect.width() * 0.5f,
                    skin,
                    true,
                    false
                )
            }

            // 4. FRONT PORTAL EFFECTS
            val portalFrontBitmap = worldBitmap("home_portal_front")
            if (portalFrontBitmap != null) {
                paint.alpha = 255
                paint.isFilterBitmap = true
                canvas.drawBitmap(portalFrontBitmap, null, portalFrontRect, paint)
            }

            // 5. RIFT ONLINE BADGE
            val badgeY = (platformRect.bottom + 14f * scale * dp).coerceAtMost(menuStartButtonTop - 10f * scale * dp)
            drawRiftOnlineBadge(canvas, heroRect.centerX(), badgeY, scale, skin.name)
            return
        }

        val cx = menuPreviewCenterX
        val cy = menuPreviewCenterY
        val radius = menuPreviewRadius
        val floatX = sin(menuPulse * 1.3f) * 6f * dp
        val floatY = sin(menuPulse * 1.8f) * 5f * dp
        val ballX = cx + floatX + menuBallOffsetX
        val ballY = cy + floatY + menuBallOffsetY
        val modeAccent = if (selectedMenuMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else 0xFF1DE8C8.toInt()
        val portalRadius = radius * (0.74f + sin(menuPulse * 2.2f) * 0.025f)
        scratch.set(cx - portalRadius, cy - portalRadius, cx + portalRadius, cy + portalRadius)
        drawWorldAsset(canvas, "portal_goal", scratch, 235)

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2.2f * dp
        paint.color = withAlpha(modeAccent, if (menuBallDragging) 210 else 120)
        canvas.drawLine(cx, cy, ballX, ballY, paint)
        paint.strokeWidth = 10f * dp
        paint.color = withAlpha(modeAccent, if (menuBallDragging) 34 else 18)
        canvas.drawLine(cx, cy, ballX, ballY, paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (selectedMenuMode == GameMode.CHAOS) 0xFFFF4D8D.toInt() else skin.lineColor, 125)
        if (richEffects) {
            paint.maskFilter = BlurMaskFilter(24f * dp, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(
            ballX,
            ballY,
            when {
                menuBallDragging && compactMenuViewport -> 39f
                menuBallDragging -> 47f
                compactMenuViewport -> 32f
                else -> 42f
            } * dp,
            paint
        )
        paint.maskFilter = null
        drawBallSkin(
            canvas,
            ballX,
            ballY,
            when {
                menuBallDragging && compactMenuViewport -> 34f
                menuBallDragging -> 37f
                compactMenuViewport -> 26f
                else -> 34f
            } * dp,
            skin,
            true,
            false
        )

        val caption = "${t("RIFT ONLINE").uppercase()}  /  ${skin.name}"
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create("sans", Typeface.BOLD)
        textPaint.textSize = 9f * dp
        val maxCaptionWidth = safeContentWidth - 48f * dp
        val captionWidth = min(textPaint.measureText(caption) + 24f * dp, maxCaptionWidth)
        val captionX = ballX.coerceIn(
            safeInsetLeft + 24f * dp + captionWidth * 0.5f,
            viewWidth - safeInsetRight - 24f * dp - captionWidth * 0.5f
        )
        val captionSafeBottom = menuActionStartButtonTop - 8f * dp
        val captionOffset = 58f * dp
        val preferredCaptionY = if (ballY + captionOffset <= captionSafeBottom) {
            ballY + captionOffset
        } else {
            ballY - 56f * dp
        }
        val captionY = preferredCaptionY.coerceIn(menuPreviewBounds.top + 20f * dp, captionSafeBottom)
        scratch.set(
            captionX - captionWidth * 0.5f,
            captionY - 16f * dp,
            captionX + captionWidth * 0.5f,
            captionY + 8f * dp
        )
        paint.style = Paint.Style.FILL
        paint.color = 0xAA050911.toInt()
        canvas.drawRoundRect(scratch, 10f * dp, 10f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = withAlpha(modeAccent, 120)
        canvas.drawRoundRect(scratch, 10f * dp, 10f * dp, paint)
        textPaint.color = 0xDEFFFFFF.toInt()
        canvas.drawText(fitText(caption, captionWidth - 18f * dp), captionX, captionY, textPaint)
    }

    fun drawMenuScreen(
        canvas: Canvas,
        menuState: MenuState,
        homeScale: Float,
        homeLayoutMode: LayoutMode,
        homeContentLeft: Float,
        homeContentWidth: Float,
        menuContentRight: Float,
        safeTopMenu: Float,
        selectedSkin: BallSkin,
        isRtl: Boolean,
        menuPrivacyButton: RectF,
        menuSfxButton: RectF,
        menuStartButton: RectF,
        menuLeaderboardButton: RectF,
        menuVaultButton: RectF,
        menuCollectionButton: RectF,
        menuStatsRects: List<RectF>,
        menuStatsTop: Float,
        menuStatsHeight: Float,
        activeMenuButton: MenuButton,
        sfxMuted: Boolean,
        bestStreak: Int,
        hypeBalance: Int,
        currentLevel: Int,
        dailyReady: Boolean,
        unlockedSkinCount: Int,
        totalSkinsCount: Int,
        nextRewardText: String?,
        centerX: Float,
        viewHeight: Float,
        safeInsetBottom: Float,
        paint: Paint,
        dp: Float,
        t: (String) -> String,
        fitText: (String, Float) -> String,
        formatHypeAmount: (Int) -> String,
        worldBitmap: (String) -> Bitmap?,
        drawPrimaryPlayButton: (Canvas, RectF) -> Unit,
        drawMenuPreview: (Canvas) -> Unit,
        drawPlayModeScreen: (Canvas, Float, Float, Float) -> Unit
    ) {
        if (menuState == MenuState.MODES) {
            drawMenuPreview(canvas)
        }

        drawBrandTitle(
            canvas = canvas,
            menuState = menuState,
            left = homeContentLeft,
            top = safeTopMenu,
            menuContentRight = menuContentRight,
            menuPrivacyButtonLeft = if (menuSfxButton.isEmpty) menuPrivacyButton.left else minOf(menuPrivacyButton.left, menuSfxButton.left),
            selectedSkinLineColor = selectedSkin.lineColor,
            homeLayoutScale = homeScale,
            homeLayoutMode = homeLayoutMode,
            homeContentWidth = homeContentWidth,
            isRtl = isRtl,
            paint = paint,
            dp = dp,
            fitText = fitText,
            worldBitmap = worldBitmap
        )

        if (menuState == MenuState.MODES) {
            HomeUiRenderer.drawHeaderActions(
                canvas = canvas,
                settingsRect = menuPrivacyButton,
                activeSettings = activeMenuButton == MenuButton.SETTINGS,
                sfxRect = menuSfxButton,
                activeSound = activeMenuButton == MenuButton.SFX,
                sfxMuted = sfxMuted,
                scale = homeScale,
                paint = paint,
                dp = dp
            )

            val gap = 7f * homeScale * dp
            val cardWidth = (homeContentWidth - gap * 3f) / 4f
            val cards = listOf(
                HomeStatSpec(t("BEST STREAK").uppercase(), "x$bestStreak", 0xFF00E5FF.toInt(), "stat_streak"),
                HomeStatSpec(t("HYPE").uppercase(), formatHypeAmount(hypeBalance), 0xFFFF2E93.toInt(), "stat_hype"),
                HomeStatSpec(t("LEVEL").uppercase(), currentLevel.toString(), 0xFF00E5FF.toInt(), "stat_level"),
                HomeStatSpec(t("DAILY RIFT").uppercase(), if (dailyReady) t("READY").uppercase() else t("CLAIMED").uppercase(), 0xFF00E5FF.toInt(), "stat_daily")
            )
            cards.forEachIndexed { index, spec ->
                val rect = menuStatsRects[index]
                rect.set(homeContentLeft + index * (cardWidth + gap), menuStatsTop, homeContentLeft + index * (cardWidth + gap) + cardWidth, menuStatsTop + menuStatsHeight)
                HomeUiRenderer.drawStatCard(
                    canvas = canvas,
                    rect = rect,
                    spec = spec,
                    scale = homeScale,
                    showLevelProgress = index == 2,
                    level = currentLevel,
                    paint = paint,
                    dp = dp,
                    fitText = fitText
                )
            }

            drawPrimaryPlayButton(canvas, menuStartButton)
            HomeUiRenderer.drawInfoRow(
                canvas = canvas,
                rect = menuLeaderboardButton,
                active = activeMenuButton == MenuButton.LEADERBOARDS,
                title = t("LEADERBOARDS").uppercase(),
                meta = "",
                iconType = "leaderboards",
                accent = 0xFF00E5FF.toInt(),
                scale = homeScale,
                paint = paint,
                dp = dp,
                fitText = fitText
            )
            HomeUiRenderer.drawInfoRow(
                canvas = canvas,
                rect = menuVaultButton,
                active = activeMenuButton == MenuButton.VAULT,
                title = t("VAULT MAXED").uppercase(),
                meta = t("ALL FREE REWARDS UNLOCKED").uppercase(),
                iconType = "vault",
                accent = 0xFF00E5FF.toInt(),
                scale = homeScale,
                paint = paint,
                dp = dp,
                fitText = fitText
            )

            val next = nextRewardText?.uppercase() ?: t("MAXED").uppercase()
            HomeUiRenderer.drawCollectionRow(
                canvas = canvas,
                rect = menuCollectionButton,
                active = activeMenuButton == MenuButton.COLLECTION,
                unlockedCount = unlockedSkinCount,
                totalSkins = totalSkinsCount,
                nextRewardText = next,
                scale = homeScale,
                paint = paint,
                dp = dp,
                t = t,
                fitText = fitText
            )

            drawHomeFooterNote(
                canvas = canvas,
                scale = homeScale,
                centerX = centerX,
                viewHeight = viewHeight,
                safeInsetBottom = safeInsetBottom,
                dp = dp,
                t = t
            )
        } else {
            drawPlayModeScreen(canvas, homeContentLeft, homeContentWidth, safeTopMenu)
        }
    }
}
