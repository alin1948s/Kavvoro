package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.moonsolstudios.kavvoro.ui.render.CyberShapeRenderer
import com.moonsolstudios.kavvoro.ui.render.KavvoroPalette
import com.moonsolstudios.kavvoro.ui.render.withAlpha
import kotlin.math.min

/**
 * Dedicated AAA-grade procedural renderer for all Settings screen icons.
 * Replaces flat stick/wireframe glyphs with rich, volumetric, multi-layered sci-fi emblems:
 * - 3D-like chamfered titanium armor housing with top specular highlights.
 * - Deep recessed reactor well with radial cavity lighting.
 * - Micro-laser reticle rings and telemetry node ticks.
 * - Volumetric, gradient-shaded artworks with glowing fusion/plasma cores and specular bevels.
 */
object SettingsIconRenderer {

    enum class SettingsIconId {
        MASTER_VOLUME,
        MUSIC_VOLUME,
        SFX_VOLUME,
        HAPTIC,
        SCREEN_SHAKE,
        PERFORMANCE,
        LANGUAGE,
        ACCOUNT,
        PRIVACY,
        TERMS,
        DATA_DELETION,
        ABOUT,
        RESET
    }

    private val tempPath = Path()
    private val badgePath = Path()
    private val scratchRect = RectF()
    private val scratchRect2 = RectF()
    private val badgeRect = RectF()

    /**
     * Draws a complete AAA cyber-command badge icon within [rect].
     */
    fun drawSettingsIcon(
        canvas: Canvas,
        rect: RectF,
        id: SettingsIconId,
        accent: Int,
        active: Boolean,
        paint: Paint,
        dp: Float
    ) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val available = min(rect.width(), rect.height()).takeIf { it > 0f } ?: (44f * dp)
        val badgeSize = available.coerceIn(38f * dp, 56f * dp)
        val corner = 5f * dp
        val notch = 4f * dp

        badgeRect.set(
            cx - badgeSize * 0.5f,
            cy - badgeSize * 0.5f,
            cx + badgeSize * 0.5f,
            cy + badgeSize * 0.5f
        )

        val badgeAccent = when (id) {
            SettingsIconId.MUSIC_VOLUME, SettingsIconId.RESET -> KavvoroPalette.pink
            SettingsIconId.DATA_DELETION -> KavvoroPalette.gold
            else -> accent
        }

        // 1. Ambient Radial Color Bloom behind the badge
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy, badgeSize * 0.85f,
            intArrayOf(withAlpha(badgeAccent, if (active) 130 else 75), 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, badgeSize * 0.85f, paint)

        // 2. Chamfered Titanium Armor Chassis
        CyberShapeRenderer.createChamferPath(badgePath, badgeRect, corner, notch)

        // 2a. Polycarbonate Obsidian Fill
        paint.shader = LinearGradient(
            badgeRect.left, badgeRect.top, badgeRect.left, badgeRect.bottom,
            intArrayOf(
                withAlpha(badgeAccent, if (active) 140 else 90),
                0xF40A1828.toInt(),
                0xF802060C.toInt()
            ),
            floatArrayOf(0f, 0.40f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(badgePath, paint)
        paint.shader = null

        // 2b. Recessed Inner Reactor Well
        val wellRadius = badgeSize * 0.38f
        paint.shader = RadialGradient(
            cx, cy, wellRadius,
            intArrayOf(0x35000000, withAlpha(badgeAccent, 45), withAlpha(KavvoroPalette.background, 85)),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, wellRadius, paint)
        paint.shader = null

        // 2c. Micro-Laser Reticle Ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f * dp
        paint.color = withAlpha(badgeAccent, if (active) 150 else 90)
        canvas.drawCircle(cx, cy, wellRadius, paint)

        // 2d. Precision Laser Outer Rim
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = if (active) 1.6f * dp else 1.2f * dp
        paint.shader = LinearGradient(
            badgeRect.left, badgeRect.top, badgeRect.right, badgeRect.bottom,
            intArrayOf(
                withAlpha(badgeAccent, if (active) 255 else 210),
                withAlpha(badgeAccent, if (active) 160 else 110)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(badgePath, paint)
        paint.shader = null

        // 2e. Top Specular Bevel Highlight
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(0xFFFFFFFF.toInt(), if (active) 120 else 60)
        canvas.drawLine(
            badgeRect.left + corner + notch, badgeRect.top + 0.8f * dp,
            badgeRect.right - corner - notch, badgeRect.top + 0.8f * dp,
            paint
        )

        // 2f. Four Tactical Corner Rivets
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(badgeAccent, if (active) 255 else 180)
        val tickOffset = badgeSize * 0.44f
        val tickSize = 1.1f * dp
        canvas.drawCircle(cx - tickOffset, cy - tickOffset, tickSize, paint)
        canvas.drawCircle(cx + tickOffset, cy - tickOffset, tickSize, paint)
        canvas.drawCircle(cx - tickOffset, cy + tickOffset, tickSize, paint)
        canvas.drawCircle(cx + tickOffset, cy + tickOffset, tickSize, paint)

        // 3. Volumetric Artwork
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        val artScale = (badgeSize / (42f * dp)).coerceIn(0.9f, 1.35f)
        canvas.save()
        canvas.scale(artScale, artScale, cx, cy)

        when (id) {
            SettingsIconId.MASTER_VOLUME -> drawMasterVolume(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.MUSIC_VOLUME -> drawMusicVolume(canvas, cx, cy, KavvoroPalette.pink, paint, dp)
            SettingsIconId.SFX_VOLUME -> drawSfxVolume(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.HAPTIC -> drawHaptic(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.SCREEN_SHAKE -> drawScreenShake(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.PERFORMANCE -> drawPerformance(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.LANGUAGE -> drawLanguage(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.ACCOUNT -> drawAccount(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.PRIVACY -> drawPrivacy(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.TERMS -> drawTerms(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.DATA_DELETION -> drawDataDeletion(canvas, cx, cy, KavvoroPalette.gold, paint, dp)
            SettingsIconId.ABOUT -> drawAbout(canvas, cx, cy, badgeAccent, paint, dp)
            SettingsIconId.RESET -> drawReset(canvas, cx, cy, KavvoroPalette.pink, paint, dp)
        }
        canvas.restore()

        // 4. Restore clean paint invariants
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeMiter = 4f
        paint.pathEffect = null
        paint.shader = null
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. MASTER VOLUME: Volumetric Acoustic Cannon & Segmented Shockwaves
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawMasterVolume(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        // Driver Cylinder Body (Left block with heat-sink ribs)
        scratchRect.set(cx - 10f * dp, cy - 4.5f * dp, cx - 4.5f * dp, cy + 4.5f * dp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
            0xFFFFFFFF.toInt(), accent, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 1.5f * dp, 1.5f * dp, paint)
        paint.shader = null

        // Heat-sink vertical rib lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f * dp
        paint.color = withAlpha(KavvoroPalette.background, 170).toInt()
        canvas.drawLine(cx - 8f * dp, cy - 3.8f * dp, cx - 8f * dp, cy + 3.8f * dp, paint)
        canvas.drawLine(cx - 6.2f * dp, cy - 3.8f * dp, cx - 6.2f * dp, cy + 3.8f * dp, paint)

        // Flared Conical Horn Flare
        tempPath.reset()
        tempPath.moveTo(cx - 4.5f * dp, cy - 4.5f * dp)
        tempPath.lineTo(cx + 1.2f * dp, cy - 8.5f * dp)
        tempPath.lineTo(cx + 1.2f * dp, cy + 8.5f * dp)
        tempPath.lineTo(cx - 4.5f * dp, cy + 4.5f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx - 4.5f * dp, cy, cx + 1.2f * dp, cy,
            accent, 0xFFFFFFFF.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Horn Bevel Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx + 1.2f * dp, cy - 8.5f * dp, cx + 1.2f * dp, cy + 8.5f * dp, paint)

        // Neodymium Central Driver Dome (White fusion core)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx - 1.2f * dp, cy, 1.8f * dp, paint)

        // 3 Progressive Concentric Laser Shockwaves
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        // Wave 1 (Inner intense pulse)
        paint.strokeWidth = 2.2f * dp
        paint.color = 0xFFFFFFFF.toInt()
        scratchRect.set(cx - 2.5f * dp, cy - 5f * dp, cx + 5.5f * dp, cy + 5f * dp)
        canvas.drawArc(scratchRect, -45f, 90f, false, paint)

        // Wave 2 (Middle resonance arc)
        paint.strokeWidth = 2.4f * dp
        paint.color = accent
        scratchRect.set(cx - 2.5f * dp, cy - 8.8f * dp, cx + 9.5f * dp, cy + 8.8f * dp)
        canvas.drawArc(scratchRect, -42f, 84f, false, paint)

        // Wave 3 (Outer burst arc with terminal diamond nodes)
        paint.strokeWidth = 1.6f * dp
        paint.color = withAlpha(accent, 170)
        scratchRect.set(cx - 2.5f * dp, cy - 12.2f * dp, cx + 13.5f * dp, cy + 12.2f * dp)
        canvas.drawArc(scratchRect, -38f, 76f, false, paint)

        // Terminal diamond nodes on outer wave
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        drawDiamondNode(canvas, cx + 10.2f * dp, cy - 7.5f * dp, 1.4f * dp, paint)
        drawDiamondNode(canvas, cx + 10.2f * dp, cy + 7.5f * dp, 1.4f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. MUSIC VOLUME: Cyber Synthwave Double Note & Equalizer Matrix
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawMusicVolume(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        // Dual Holographic Synthwave Notes in Hot Pink & Neon White
        val pink = accent
        val cyan = KavvoroPalette.cyan

        // Note Head 1 (Left)
        paint.style = Paint.Style.FILL
        paint.color = pink
        canvas.drawCircle(cx - 6f * dp, cy + 5.5f * dp, 3.8f * dp, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx - 6f * dp, cy + 5.5f * dp, 1.5f * dp, paint)

        // Note Head 2 (Center)
        paint.color = pink
        canvas.drawCircle(cx + 0.5f * dp, cy + 3.5f * dp, 3.5f * dp, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx + 0.5f * dp, cy + 3.5f * dp, 1.4f * dp, paint)

        // Dual Vertical Stems
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * dp
        paint.color = pink
        canvas.drawLine(cx - 3f * dp, cy + 5.5f * dp, cx - 3f * dp, cy - 7.5f * dp, paint)
        canvas.drawLine(cx + 3.3f * dp, cy + 3.5f * dp, cx + 3.3f * dp, cy - 9.5f * dp, paint)

        // Angular Cyber Crossbeam (Double Bar)
        paint.strokeWidth = 2.6f * dp
        paint.color = pink
        canvas.drawLine(cx - 3.2f * dp, cy - 6.5f * dp, cx + 3.5f * dp, cy - 8.5f * dp, paint)
        paint.strokeWidth = 1.2f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx - 3.2f * dp, cy - 6.5f * dp, cx + 3.5f * dp, cy - 8.5f * dp, paint)

        // 3-Band Graphic Equalizer Visualizer on Right Flank
        val eqX1 = cx + 6.8f * dp
        val eqX2 = cx + 9.5f * dp
        val eqX3 = cx + 12.2f * dp
        val eqBottom = cy + 7.5f * dp
        val barW = 1.8f * dp

        drawEqualizerPillar(canvas, eqX1, eqBottom - 7f * dp, eqX1 + barW, eqBottom, cyan, pink, paint, dp)
        drawEqualizerPillar(canvas, eqX2, eqBottom - 13f * dp, eqX2 + barW, eqBottom, cyan, pink, paint, dp)
        drawEqualizerPillar(canvas, eqX3, eqBottom - 9f * dp, eqX3 + barW, eqBottom, cyan, pink, paint, dp)

        // Peak hold pips
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRect(eqX1, eqBottom - 8.5f * dp, eqX1 + barW, eqBottom - 7.5f * dp, paint)
        canvas.drawRect(eqX2, eqBottom - 14.5f * dp, eqX2 + barW, eqBottom - 13.5f * dp, paint)
        canvas.drawRect(eqX3, eqBottom - 10.5f * dp, eqX3 + barW, eqBottom - 9.5f * dp, paint)
    }

    private fun drawEqualizerPillar(
        canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float,
        cyan: Int, pink: Int, paint: Paint, dp: Float
    ) {
        scratchRect.set(left, top, right, bottom)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(left, bottom, left, top, cyan, pink, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(scratchRect, 0.8f * dp, 0.8f * dp, paint)
        paint.shader = null
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. SFX VOLUME: Quantum Sonic Resonance Cannon & Particle Spark
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawSfxVolume(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Acoustic Emitter Horn
        tempPath.reset()
        tempPath.moveTo(cx - 10f * dp, cy - 3.8f * dp)
        tempPath.lineTo(cx - 5f * dp, cy - 3.8f * dp)
        tempPath.lineTo(cx, cy - 8.2f * dp)
        tempPath.lineTo(cx, cy + 8.2f * dp)
        tempPath.lineTo(cx - 5f * dp, cy + 3.8f * dp)
        tempPath.lineTo(cx - 10f * dp, cy + 3.8f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx - 10f * dp, cy, cx, cy,
            accent, 0xFFFFFFFF.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Muzzle rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx, cy - 8.2f * dp, cx, cy + 8.2f * dp, paint)

        // Central Sonic Spark
        paint.style = Paint.Style.FILL
        paint.color = pink
        canvas.drawCircle(cx + 2.5f * dp, cy, 2.2f * dp, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx + 2.5f * dp, cy, 1f * dp, paint)

        // Alternating Supersonic Shockwave Arcs
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        // Inner Arc (Pink)
        paint.strokeWidth = 2.4f * dp
        paint.color = pink
        scratchRect.set(cx - 3.5f * dp, cy - 5.5f * dp, cx + 6.5f * dp, cy + 5.5f * dp)
        canvas.drawArc(scratchRect, -45f, 90f, false, paint)

        // Outer Arc (Cyan)
        paint.strokeWidth = 2.2f * dp
        paint.color = accent
        scratchRect.set(cx - 3.5f * dp, cy - 9.5f * dp, cx + 11.5f * dp, cy + 9.5f * dp)
        canvas.drawArc(scratchRect, -42f, 84f, false, paint)

        // Radiating Diamond Particles ◆
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        drawDiamondNode(canvas, cx + 10.5f * dp, cy - 5.5f * dp, 1.6f * dp, paint)
        drawDiamondNode(canvas, cx + 12f * dp, cy + 0.5f * dp, 1.8f * dp, paint)
        drawDiamondNode(canvas, cx + 10.5f * dp, cy + 6.5f * dp, 1.6f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. HAPTIC: Industrial Linear Actuator & Electromagnetic Coils
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawHaptic(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Actuator Heavy Chassis
        scratchRect.set(cx - 5.5f * dp, cy - 9.5f * dp, cx + 5.5f * dp, cy + 9.5f * dp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
            0xF6122438.toInt(), 0xF8030812.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 2.5f * dp, 2.5f * dp, paint)
        paint.shader = null

        // Chassis Laser Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = accent
        canvas.drawRoundRect(scratchRect, 2.5f * dp, 2.5f * dp, paint)

        // Internal Copper/Neon Solenoid Coils
        paint.style = Paint.Style.FILL
        paint.color = KavvoroPalette.gold
        scratchRect2.set(cx - 3.8f * dp, cy - 7f * dp, cx + 3.8f * dp, cy - 4.5f * dp)
        canvas.drawRoundRect(scratchRect2, 1f * dp, 1f * dp, paint)
        scratchRect2.set(cx - 3.8f * dp, cy + 4.5f * dp, cx + 3.8f * dp, cy + 7f * dp)
        canvas.drawRoundRect(scratchRect2, 1f * dp, 1f * dp, paint)

        // Center Tungsten Inertial Piston
        scratchRect2.set(cx - 4.2f * dp, cy - 2.8f * dp, cx + 4.2f * dp, cy + 2.8f * dp)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(scratchRect2, 1.2f * dp, 1.2f * dp, paint)

        // Status Core Diode
        paint.color = pink
        canvas.drawCircle(cx, cy, 1.4f * dp, paint)

        // Bilateral Supersonic Vibration Waves (( ))
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2.4f * dp
        paint.color = pink

        // Left waves
        scratchRect.set(cx - 11.5f * dp, cy - 6f * dp, cx - 1.5f * dp, cy + 6f * dp)
        canvas.drawArc(scratchRect, 130f, 100f, false, paint)

        // Right waves
        scratchRect.set(cx + 1.5f * dp, cy - 6f * dp, cx + 11.5f * dp, cy + 6f * dp)
        canvas.drawArc(scratchRect, -50f, 100f, false, paint)

        // Outer fainter ripples
        paint.strokeWidth = 1.4f * dp
        paint.color = withAlpha(accent, 180)
        scratchRect.set(cx - 14.5f * dp, cy - 8.5f * dp, cx - 1.5f * dp, cy + 8.5f * dp)
        canvas.drawArc(scratchRect, 135f, 90f, false, paint)
        scratchRect.set(cx + 1.5f * dp, cy - 8.5f * dp, cx + 14.5f * dp, cy + 8.5f * dp)
        canvas.drawArc(scratchRect, -45f, 90f, false, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 5. SCREEN SHAKE: Tactical Impact HUD Seismograph
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawScreenShake(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Tactical Display HUD Monitor Frame
        scratchRect.set(cx - 10f * dp, cy - 7f * dp, cx + 10f * dp, cy + 7f * dp)
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            scratchRect.left, scratchRect.top, scratchRect.left, scratchRect.bottom,
            0xF60F2032.toInt(), 0xF803070E.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(scratchRect, 2.5f * dp, 2.5f * dp, paint)
        paint.shader = null

        // Laser Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * dp
        paint.color = withAlpha(accent, 150)
        canvas.drawRoundRect(scratchRect, 2.5f * dp, 2.5f * dp, paint)

        // 4 Glowing Corner Crosshairs [+]
        paint.strokeWidth = 1.8f * dp
        paint.color = accent
        val bracketLen = 3f * dp
        // Top-Left
        canvas.drawLine(scratchRect.left + 1f * dp, scratchRect.top + bracketLen, scratchRect.left + 1f * dp, scratchRect.top + 1f * dp, paint)
        canvas.drawLine(scratchRect.left + 1f * dp, scratchRect.top + 1f * dp, scratchRect.left + bracketLen, scratchRect.top + 1f * dp, paint)
        // Top-Right
        canvas.drawLine(scratchRect.right - 1f * dp, scratchRect.top + bracketLen, scratchRect.right - 1f * dp, scratchRect.top + 1f * dp, paint)
        canvas.drawLine(scratchRect.right - bracketLen, scratchRect.top + 1f * dp, scratchRect.right - 1f * dp, scratchRect.top + 1f * dp, paint)
        // Bottom-Left
        canvas.drawLine(scratchRect.left + 1f * dp, scratchRect.bottom - bracketLen, scratchRect.left + 1f * dp, scratchRect.bottom - 1f * dp, paint)
        canvas.drawLine(scratchRect.left + 1f * dp, scratchRect.bottom - 1f * dp, scratchRect.left + bracketLen, scratchRect.bottom - 1f * dp, paint)
        // Bottom-Right
        canvas.drawLine(scratchRect.right - 1f * dp, scratchRect.bottom - bracketLen, scratchRect.right - 1f * dp, scratchRect.bottom - 1f * dp, paint)
        canvas.drawLine(scratchRect.right - bracketLen, scratchRect.bottom - 1f * dp, scratchRect.right - 1f * dp, scratchRect.bottom - 1f * dp, paint)

        // Dynamic Seismic Pulse Waveform (Pure white core + glowing pink halo)
        tempPath.reset()
        tempPath.moveTo(cx - 8f * dp, cy)
        tempPath.lineTo(cx - 4.5f * dp, cy)
        tempPath.lineTo(cx - 2.5f * dp, cy - 4.5f * dp)
        tempPath.lineTo(cx, cy + 4.8f * dp)
        tempPath.lineTo(cx + 2.5f * dp, cy - 3.5f * dp)
        tempPath.lineTo(cx + 4.5f * dp, cy)
        tempPath.lineTo(cx + 8f * dp, cy)

        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND

        // Outer glow stroke
        paint.strokeWidth = 3.2f * dp
        paint.color = withAlpha(pink, 160)
        canvas.drawPath(tempPath, paint)

        // Inner laser core
        paint.strokeWidth = 1.6f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, paint)

        // Epicenter Diamond Node at highest peak
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        drawDiamondNode(canvas, cx - 2.5f * dp, cy - 4.5f * dp, 1.6f * dp, paint)

        // Lateral kinetic chevrons ◀ ▶
        paint.color = pink
        drawChevronLeft(canvas, cx - 12f * dp, cy, 2.5f * dp, paint)
        drawChevronRight(canvas, cx + 12f * dp, cy, 2.5f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 6. PERFORMANCE: Quantum Overclock Hyperdrive Core
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawPerformance(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        // Armored Octagonal Magnetic Confinement Chamber
        val octRadius = 9f * dp
        tempPath.reset()
        val cos45 = 0.7071f * octRadius
        tempPath.moveTo(cx - cos45, cy - octRadius)
        tempPath.lineTo(cx + cos45, cy - octRadius)
        tempPath.lineTo(cx + octRadius, cy - cos45)
        tempPath.lineTo(cx + octRadius, cy + cos45)
        tempPath.lineTo(cx + cos45, cy + octRadius)
        tempPath.lineTo(cx - cos45, cy + octRadius)
        tempPath.lineTo(cx - octRadius, cy + cos45)
        tempPath.lineTo(cx - octRadius, cy - cos45)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy, octRadius,
            intArrayOf(0xF8040C16.toInt(), withAlpha(accent, 85), 0xF602070E.toInt()),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Octagonal Laser Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = accent
        canvas.drawPath(tempPath, paint)

        // Segmented Magnetic Confinement Ring
        paint.strokeWidth = 1.8f * dp
        paint.color = 0xFFFFFFFF.toInt()
        scratchRect.set(cx - 6f * dp, cy - 6f * dp, cx + 6f * dp, cy + 6f * dp)
        canvas.drawArc(scratchRect, 15f, 60f, false, paint)
        canvas.drawArc(scratchRect, 105f, 60f, false, paint)
        canvas.drawArc(scratchRect, 195f, 60f, false, paint)
        canvas.drawArc(scratchRect, 285f, 60f, false, paint)

        // Supercharged Quantum Star ✦ at Center
        paint.style = Paint.Style.FILL
        tempPath.reset()
        val spikeLong = 7.5f * dp
        val spikeShort = 2f * dp
        tempPath.moveTo(cx, cy - spikeLong)
        tempPath.lineTo(cx + spikeShort, cy - spikeShort)
        tempPath.lineTo(cx + spikeLong, cy)
        tempPath.lineTo(cx + spikeShort, cy + spikeShort)
        tempPath.lineTo(cx, cy + spikeLong)
        tempPath.lineTo(cx - spikeShort, cy + spikeShort)
        tempPath.lineTo(cx - spikeLong, cy)
        tempPath.lineTo(cx - spikeShort, cy - spikeShort)
        tempPath.close()

        // Star energy halo
        paint.color = accent
        canvas.drawPath(tempPath, paint)

        // Pure white fusion plasma core
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy, 2.2f * dp, paint)

        // 4 diagonal micro-diamonds
        val dDist = 4.2f * dp
        drawDiamondNode(canvas, cx - dDist, cy - dDist, 1.1f * dp, paint)
        drawDiamondNode(canvas, cx + dDist, cy - dDist, 1.1f * dp, paint)
        drawDiamondNode(canvas, cx - dDist, cy + dDist, 1.1f * dp, paint)
        drawDiamondNode(canvas, cx + dDist, cy + dDist, 1.1f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7. LANGUAGE: Holographic Celestial Astrolabe & Orbital Geosphere
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawLanguage(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val radius = 9f * dp
        val pink = KavvoroPalette.pink

        // 3D Shaded Celestial Holosphere
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx - 2.5f * dp, cy - 2.5f * dp, radius * 1.3f,
            intArrayOf(0xFFFFFFFF.toInt(), accent, 0xF602070E.toInt()),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        // Celestial Sphere Laser Outer Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy, radius, paint)

        // Equator & Latitude Data Lines
        paint.strokeWidth = 1f * dp
        paint.color = 0xCC050E18.toInt()
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paint)
        scratchRect.set(cx - radius * 0.88f, cy - 4.5f * dp, cx + radius * 0.88f, cy + 4.5f * dp)
        canvas.drawOval(scratchRect, paint)

        // Longitude Ellipse
        scratchRect.set(cx - 4.5f * dp, cy - radius, cx + 4.5f * dp, cy + radius)
        canvas.drawOval(scratchRect, paint)

        // Meridian Illuminated Node Dots
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx - 4.5f * dp, cy, 1.2f * dp, paint)
        canvas.drawCircle(cx, cy, 1.4f * dp, paint)
        canvas.drawCircle(cx + 4.5f * dp, cy, 1.2f * dp, paint)

        // Tilted Precession Orbital Ring (-25°) in Hot Pink
        scratchRect.set(cx - 12.5f * dp, cy - 4.2f * dp, cx + 12.5f * dp, cy + 4.2f * dp)
        canvas.save()
        canvas.rotate(-25f, cx, cy)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f * dp
        paint.color = pink
        canvas.drawOval(scratchRect, paint)

        // Orbiting satellite diamond ◆ on ring
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        drawDiamondNode(canvas, cx + 10f * dp, cy, 1.6f * dp, paint)
        canvas.restore()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 8. ACCOUNT: Cyber-Commander Helm & Biometric Crest
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawAccount(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Angular Commander Helmet Crown
        tempPath.reset()
        tempPath.moveTo(cx, cy - 9.5f * dp)
        tempPath.lineTo(cx + 5f * dp, cy - 6f * dp)
        tempPath.lineTo(cx + 4.5f * dp, cy - 1.5f * dp)
        tempPath.lineTo(cx - 4.5f * dp, cy - 1.5f * dp)
        tempPath.lineTo(cx - 5f * dp, cy - 6f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx, cy - 9.5f * dp, cx, cy - 1.5f * dp,
            0xFFFFFFFF.toInt(), accent, Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Visor Blade: Radiant Cyan Beam with Pure White Reflection
        scratchRect.set(cx - 5.5f * dp, cy - 1.8f * dp, cx + 5.5f * dp, cy + 0.5f * dp)
        paint.color = accent
        canvas.drawRoundRect(scratchRect, 0.8f * dp, 0.8f * dp, paint)
        // Inner white laser core
        scratchRect2.set(cx - 3.8f * dp, cy - 1.2f * dp, cx + 3.8f * dp, cy - 0.2f * dp)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(scratchRect2, 0.5f * dp, 0.5f * dp, paint)

        // Armored Gorget / Shoulder Plate
        tempPath.reset()
        tempPath.moveTo(cx - 9.5f * dp, cy + 8.5f * dp)
        tempPath.lineTo(cx - 6f * dp, cy + 2f * dp)
        tempPath.lineTo(cx + 6f * dp, cy + 2f * dp)
        tempPath.lineTo(cx + 9.5f * dp, cy + 8.5f * dp)
        tempPath.close()

        paint.shader = LinearGradient(
            cx, cy + 2f * dp, cx, cy + 8.5f * dp,
            accent, 0xF8051220.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Specular armor rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, paint)

        // Commander Rank Chevrons ▼▼ on chest
        paint.style = Paint.Style.FILL
        paint.color = pink
        drawChevronDown(canvas, cx, cy + 4f * dp, 2f * dp, paint)
        drawChevronDown(canvas, cx, cy + 6.2f * dp, 2f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 9. PRIVACY: Cryptographic Quantum Aegis Shield
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawPrivacy(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Heavy Faceted Armor Shield
        tempPath.reset()
        tempPath.moveTo(cx, cy - 9.5f * dp)
        tempPath.lineTo(cx + 8.5f * dp, cy - 6f * dp)
        tempPath.lineTo(cx + 8.5f * dp, cy + 1.5f * dp)
        tempPath.quadTo(cx + 6.5f * dp, cy + 7.5f * dp, cx, cy + 10f * dp)
        tempPath.quadTo(cx - 6.5f * dp, cy + 7.5f * dp, cx - 8.5f * dp, cy + 1.5f * dp)
        tempPath.lineTo(cx - 8.5f * dp, cy - 6f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx, cy - 9.5f * dp, cx, cy + 10f * dp,
            0xFFFFFFFF.toInt(), accent, Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Laser Perimeter
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, paint)

        // Inner Dark Cavity Plate
        tempPath.reset()
        tempPath.moveTo(cx, cy - 7f * dp)
        tempPath.lineTo(cx + 6.5f * dp, cy - 4.2f * dp)
        tempPath.lineTo(cx + 6.5f * dp, cy + 1.2f * dp)
        tempPath.quadTo(cx + 4.8f * dp, cy + 5.8f * dp, cx, cy + 7.8f * dp)
        tempPath.quadTo(cx - 4.8f * dp, cy + 5.8f * dp, cx - 6.5f * dp, cy + 1.2f * dp)
        tempPath.lineTo(cx - 6.5f * dp, cy - 4.2f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.color = 0xF6040A14.toInt()
        canvas.drawPath(tempPath, paint)

        // Laser-Cut Keyhole Reactor Core (Pure white glow)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy - 0.5f * dp, 2.2f * dp, paint)

        tempPath.reset()
        tempPath.moveTo(cx - 1.2f * dp, cy)
        tempPath.lineTo(cx + 1.2f * dp, cy)
        tempPath.lineTo(cx + 1.6f * dp, cy + 4f * dp)
        tempPath.lineTo(cx - 1.6f * dp, cy + 4f * dp)
        tempPath.close()
        canvas.drawPath(tempPath, paint)

        // Dual Security Traces in Hot Pink
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * dp
        paint.color = pink
        canvas.drawLine(cx - 4.5f * dp, cy - 2f * dp, cx - 3f * dp, cy - 2f * dp, paint)
        canvas.drawLine(cx + 3f * dp, cy - 2f * dp, cx + 4.5f * dp, cy - 2f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 10. TERMS: Encrypted Military Smart-Contract Datapad
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawTerms(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Datapad Slab with Folded Upper Right Corner
        tempPath.reset()
        tempPath.moveTo(cx - 7.5f * dp, cy - 9.5f * dp)
        tempPath.lineTo(cx + 3.2f * dp, cy - 9.5f * dp)
        tempPath.lineTo(cx + 8f * dp, cy - 4.8f * dp)
        tempPath.lineTo(cx + 8f * dp, cy + 9.5f * dp)
        tempPath.lineTo(cx - 7.5f * dp, cy + 9.5f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx - 7.5f * dp, cy - 9.5f * dp, cx + 8f * dp, cy + 9.5f * dp,
            0xF614263A.toInt(), 0xF8030812.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Laser Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = accent
        canvas.drawPath(tempPath, paint)

        // Corner Fold Triangle (Pure white / cyan highlight)
        tempPath.reset()
        tempPath.moveTo(cx + 3.2f * dp, cy - 9.5f * dp)
        tempPath.lineTo(cx + 3.2f * dp, cy - 4.8f * dp)
        tempPath.lineTo(cx + 8f * dp, cy - 4.8f * dp)
        tempPath.close()
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, paint)

        // 4 Encrypted Data Stream Lines
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 1.8f * dp

        // Line 1 (White)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx - 4.5f * dp, cy - 1.5f * dp, cx + 4.5f * dp, cy - 1.5f * dp, paint)
        // Line 2 (Cyan)
        paint.color = accent
        canvas.drawLine(cx - 4.5f * dp, cy + 1.8f * dp, cx + 4.5f * dp, cy + 1.8f * dp, paint)
        // Line 3 (Pink)
        paint.color = pink
        canvas.drawLine(cx - 4.5f * dp, cy + 5f * dp, cx + 1.5f * dp, cy + 5f * dp, paint)

        // Verification Seal / Checkmark in lower right
        paint.style = Paint.Style.FILL
        paint.color = KavvoroPalette.cyan
        canvas.drawCircle(cx + 4.8f * dp, cy + 5.2f * dp, 1.8f * dp, paint)
        paint.color = KavvoroPalette.background
        canvas.drawCircle(cx + 4.8f * dp, cy + 5.2f * dp, 0.7f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11. DATA DELETION: Hazardous Antimatter Containment Vault
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawDataDeletion(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val amber = accent
        val pink = KavvoroPalette.pink

        // Reinforced Canister Drum
        tempPath.reset()
        tempPath.moveTo(cx - 6.5f * dp, cy - 4.5f * dp)
        tempPath.lineTo(cx - 5.2f * dp, cy + 8.8f * dp)
        tempPath.lineTo(cx + 5.2f * dp, cy + 8.8f * dp)
        tempPath.lineTo(cx + 6.5f * dp, cy - 4.5f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx, cy - 4.5f * dp, cx, cy + 8.8f * dp,
            amber, 0xF80B0703.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Laser Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.3f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, paint)

        // Pressure Valve Cap
        scratchRect.set(cx - 8.5f * dp, cy - 8.5f * dp, cx + 8.5f * dp, cy - 4.5f * dp)
        paint.style = Paint.Style.FILL
        paint.color = amber
        canvas.drawRoundRect(scratchRect, 1.5f * dp, 1.5f * dp, paint)
        paint.style = Paint.Style.STROKE
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(scratchRect, 1.5f * dp, 1.5f * dp, paint)

        // Diagonal Hazard Stripes in Deep Carbon
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * dp
        paint.color = KavvoroPalette.background
        canvas.drawLine(cx - 4.2f * dp, cy - 2f * dp, cx - 1.2f * dp, cy + 2f * dp, paint)
        canvas.drawLine(cx + 0.2f * dp, cy - 2f * dp, cx + 3.2f * dp, cy + 2f * dp, paint)

        // Warning Hazard Chamber (Pink Core)
        paint.style = Paint.Style.FILL
        paint.color = pink
        canvas.drawCircle(cx, cy + 4.5f * dp, 2f * dp, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy + 4.5f * dp, 0.9f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 12. ABOUT: Interstellar Flagship & Lunar Orbit Emblem
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawAbout(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val pink = KavvoroPalette.pink

        // Background Lunar Orbital Crescent
        tempPath.reset()
        scratchRect.set(cx - 9.5f * dp, cy - 9.5f * dp, cx + 9.5f * dp, cy + 9.5f * dp)
        tempPath.arcTo(scratchRect, -120f, 240f, true)
        scratchRect.set(cx - 5.5f * dp, cy - 7f * dp, cx + 8.5f * dp, cy + 7f * dp)
        tempPath.arcTo(scratchRect, 120f, -240f, false)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx - 9.5f * dp, cy, cx + 9.5f * dp, cy,
            pink, withAlpha(accent, 120), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Supersonic Delta-Wing Starship (Ascending forward)
        tempPath.reset()
        tempPath.moveTo(cx + 1f * dp, cy - 9.5f * dp)  // Nose
        tempPath.lineTo(cx + 7.5f * dp, cy + 3.5f * dp) // Right wingtip
        tempPath.lineTo(cx + 3.5f * dp, cy + 2f * dp)   // Right inner fuselage
        tempPath.lineTo(cx + 2.5f * dp, cy + 6.5f * dp)  // Right engine
        tempPath.lineTo(cx - 0.5f * dp, cy + 6.5f * dp)  // Left engine
        tempPath.lineTo(cx - 1.5f * dp, cy + 2f * dp)   // Left inner fuselage
        tempPath.lineTo(cx - 5.5f * dp, cy + 3.5f * dp) // Left wingtip
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx, cy - 9.5f * dp, cx, cy + 6.5f * dp,
            0xFFFFFFFF.toInt(), accent, Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Razor-Sharp Leading Edge Highlights
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx + 1f * dp, cy - 9.5f * dp, cx + 7.5f * dp, cy + 3.5f * dp, paint)
        canvas.drawLine(cx + 1f * dp, cy - 9.5f * dp, cx - 5.5f * dp, cy + 3.5f * dp, paint)

        // Ion Thruster Plasma Plumes in Pure White/Cyan
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx + 1f * dp, cy + 7.8f * dp, 1.2f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 13. RESET: Critical Reactor Meltdown Warning Core
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawReset(canvas: Canvas, cx: Float, cy: Float, accent: Int, paint: Paint, dp: Float) {
        val crimson = accent

        // Heavy Chamfered Hazard Housing (Emergency Triangle / Shield)
        tempPath.reset()
        tempPath.moveTo(cx, cy - 9.5f * dp)
        tempPath.lineTo(cx + 9.5f * dp, cy + 7f * dp)
        tempPath.lineTo(cx - 9.5f * dp, cy + 7f * dp)
        tempPath.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx, cy - 9.5f * dp, cx, cy + 7f * dp,
            0xF628040C.toInt(), 0xF80B0205.toInt(), Shader.TileMode.CLAMP
        )
        canvas.drawPath(tempPath, paint)
        paint.shader = null

        // Hazard Dual Laser Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * dp
        paint.color = crimson
        canvas.drawPath(tempPath, paint)

        // Inner Warning Core
        paint.strokeWidth = 0.8f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(tempPath, paint)

        // Emergency Exclamation Alert Core
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2.4f * dp
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(cx, cy - 4f * dp, cx, cy + 1.2f * dp, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy + 4.2f * dp, 1.3f * dp, paint)

        // 3 Radiating Alert Pulses
        paint.color = crimson
        drawDiamondNode(canvas, cx - 6.5f * dp, cy + 2f * dp, 1.2f * dp, paint)
        drawDiamondNode(canvas, cx + 6.5f * dp, cy + 2f * dp, 1.2f * dp, paint)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper Geometric Primitives
    // ─────────────────────────────────────────────────────────────────────────────
    private fun drawDiamondNode(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        tempPath.reset()
        tempPath.moveTo(x, y - radius)
        tempPath.lineTo(x + radius, y)
        tempPath.lineTo(x, y + radius)
        tempPath.lineTo(x - radius, y)
        tempPath.close()
        canvas.drawPath(tempPath, paint)
    }

    private fun drawChevronLeft(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(x + size, y - size, x, y, paint)
        canvas.drawLine(x, y, x + size, y + size, paint)
    }

    private fun drawChevronRight(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(x - size, y - size, x, y, paint)
        canvas.drawLine(x, y, x - size, y + size, paint)
    }

    private fun drawChevronDown(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(x - size, y - size * 0.5f, x, y + size * 0.5f, paint)
        canvas.drawLine(x, y + size * 0.5f, x + size, y - size * 0.5f, paint)
    }
}
