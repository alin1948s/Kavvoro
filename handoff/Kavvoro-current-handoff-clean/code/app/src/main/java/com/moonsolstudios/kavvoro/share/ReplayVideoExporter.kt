package com.moonsolstudios.kavvoro.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.engine.Block
import com.moonsolstudios.kavvoro.engine.Hazard
import com.moonsolstudios.kavvoro.engine.HazardMotion
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsEngine
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.PortalPair
import com.moonsolstudios.kavvoro.engine.PulseZone
import com.moonsolstudios.kavvoro.engine.STAGE_WIDTH
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class ReplaySharePayload(
    val level: LevelSpec,
    val line: List<Point2>,
    val replayFrames: List<PhysicsFrame>,
    val modeLabel: String,
    val hypeScore: Int,
    val streak: Int,
    val challengeCode: String,
    val curseLabel: String,
    val resultLabel: String,
    val ballName: String = "KAVVI",
    val ballPrimary: Int = 0xFFF7F4FF.toInt(),
    val ballSecondary: Int = 0xFF1DE8C8.toInt(),
    val lineColor: Int = 0xE8F7F4FF.toInt(),
    val ballArtResource: Int = R.drawable.brainball_nodlo,
    val ballVisualScale: Float = 1.2f,
    val riftBreak: Boolean = false,
    val riftBreakLabel: String = "",
    val archetypeLabel: String = "",
    val archetypeDetail: String = "",
    val runSeconds: Float = 0f
)

class ReplayVideoExporter(private val context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val scratch = RectF()
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    fun export(payload: ReplaySharePayload): File {
        val dir = File(context.cacheDir, "shared_replays").apply {
            if (!exists()) mkdirs()
        }
        dir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".mp4")) file.delete()
        }

        val outputFile = File(dir, "kavvoro-${System.currentTimeMillis()}.mp4")
        encode(outputFile, payload)
        return outputFile
    }

    private fun encode(outputFile: File, payload: ReplaySharePayload) {
        val codec = MediaCodec.createEncoderByType(MIME_TYPE)
        val format = MediaFormat.createVideoFormat(MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = codec.createInputSurface()
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val bufferInfo = MediaCodec.BufferInfo()
        val muxerState = MuxerState()

        try {
            val frameCount = (FRAME_RATE * videoSeconds(payload)).roundToInt().coerceAtLeast(FRAME_RATE * 4)
            repeat(frameCount) { frameIndex ->
                drawVideoFrame(inputSurface, payload, frameIndex, frameCount)
                drainEncoder(codec, muxer, bufferInfo, muxerState, signalEnd = false)
            }
            drainEncoder(codec, muxer, bufferInfo, muxerState, signalEnd = true)
        } finally {
            inputSurface.release()
            codec.stop()
            codec.release()
            if (muxerState.started) {
                muxer.stop()
            }
            muxer.release()
        }
    }

    private fun drawVideoFrame(surface: Surface, payload: ReplaySharePayload, frameIndex: Int, frameCount: Int) {
        val canvas = surface.lockCanvas(null)
        try {
            drawPayload(canvas, payload, frameIndex, frameCount)
        } finally {
            surface.unlockCanvasAndPost(canvas)
        }
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        muxerState: MuxerState,
        signalEnd: Boolean
    ) {
        if (signalEnd) {
            codec.signalEndOfInputStream()
        }

        while (true) {
            val encoderStatus = codec.dequeueOutputBuffer(bufferInfo, if (signalEnd) 10_000L else 0L)
            when {
                encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!signalEnd) return
                }

                encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    check(!muxerState.started) { "Encoder format changed twice." }
                    muxerState.trackIndex = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerState.started = true
                }

                encoderStatus >= 0 -> {
                    val encodedData = codec.getOutputBuffer(encoderStatus)
                    if (encodedData != null && bufferInfo.size > 0 && muxerState.started) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        bufferInfo.presentationTimeUs = muxerState.sampleIndex * 1_000_000L / FRAME_RATE
                        muxerState.sampleIndex += 1
                        muxer.writeSampleData(muxerState.trackIndex, encodedData, bufferInfo)
                    }
                    val endOfStream = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    codec.releaseOutputBuffer(encoderStatus, false)
                    if (endOfStream) return
                }
            }
        }
    }

    private fun drawPayload(canvas: Canvas, payload: ReplaySharePayload, frameIndex: Int, frameCount: Int) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val videoProgress = frameIndex / max(1f, (frameCount - 1).toFloat())
        val replayStart = payload.replayFrames.firstOrNull()?.elapsedSeconds ?: 0f
        val replayEnd = payload.replayFrames.lastOrNull()?.elapsedSeconds ?: replayStart
        val replayDuration = (replayEnd - replayStart).coerceAtLeast(0.01f)
        val videoSeconds = frameCount / FRAME_RATE.toFloat()
        val playbackSeconds = (frameIndex / FRAME_RATE.toFloat()).coerceAtMost(replayDuration)
        val replayTime = replayStart + playbackSeconds
        val timedFrame = frameAtTime(payload.replayFrames, replayTime)
        val replayIndex = timedFrame?.index ?: 0
        val currentFrame = timedFrame?.frame
        val currentBall = currentFrame?.ball ?: payload.level.start
        val chaosMode = payload.modeLabel.contains("CHAOS", ignoreCase = true)
        val gameplayProgress = (playbackSeconds / replayDuration).coerceIn(0f, 1f)
        val effectProgress = gameplayProgress
        val outroProgress = ((frameIndex / FRAME_RATE.toFloat() - replayDuration) / (videoSeconds - replayDuration).coerceAtLeast(0.01f)).coerceIn(0f, 1f)

        val background = when {
            payload.level.index <= 10 && chaosMode -> R.drawable.world_bg_tutorial_chaos
            payload.level.index <= 10 -> R.drawable.world_bg_tutorial_classic
            payload.level.index >= 150 -> R.drawable.world_bg_endgame
            chaosMode -> R.drawable.world_bg_chaos
            else -> R.drawable.world_bg_classic
        }
        drawAsset(canvas, background, RectF(0f, 0f, w, h))
        drawGameplayScrim(canvas, payload, chaosMode)

        val scale = min(w / STAGE_WIDTH, h / payload.level.stageHeight)
        val left = (w - STAGE_WIDTH * scale) * 0.5f
        val top = (h - payload.level.stageHeight * scale) * 0.5f
        val tx: (Float) -> Float = { left + it * scale }
        val ty: (Float) -> Float = { top + it * scale }

        drawStageSideMask(canvas, left, top, scale, payload.level.stageHeight, payload.level.accent)
        drawGrid(canvas, left, top, scale, payload.level.stageHeight)
        payload.level.pulseZones.forEach { drawPulse(canvas, it, tx, ty, scale, effectProgress, payload.level.accent) }
        payload.level.portals.forEach { drawPortal(canvas, it, tx, ty, scale, effectProgress) }
        payload.level.blocks.forEach { drawBlock(canvas, it, tx, ty, scale, chaosMode) }
        val hazardTime = currentFrame?.elapsedSeconds ?: replayTime
        payload.level.hazards.forEach { drawHazard(canvas, it, tx, ty, scale, hazardTime) }
        drawGoal(canvas, payload.level, tx, ty, scale, effectProgress)
        drawPolyline(canvas, payload.line, tx, ty, scale * 0.065f, withAlpha(payload.lineColor, 92))
        drawReplayTrail(canvas, payload.replayFrames.take(replayIndex) + listOfNotNull(currentFrame), tx, ty)
        currentFrame?.riftAnchor?.let { anchor ->
            drawRiftTether(canvas, currentBall, anchor, currentFrame.riftStrength, tx, ty, payload.lineColor)
        }
        drawBall(canvas, currentBall, tx, ty, scale, payload)

        drawTopOverlay(canvas, payload)
        drawBottomOverlay(canvas, payload, gameplayProgress)
        if (outroProgress > 0f) {
            drawEndCard(canvas, payload, outroProgress, chaosMode)
        }
    }

    private fun frameAtTime(frames: List<PhysicsFrame>, seconds: Float): TimedReplayFrame? {
        if (frames.isEmpty()) return null
        if (seconds <= frames.first().elapsedSeconds) return TimedReplayFrame(frames.first(), 0)
        val lastIndex = frames.lastIndex
        if (seconds >= frames[lastIndex].elapsedSeconds) return TimedReplayFrame(frames[lastIndex], lastIndex)

        var low = 0
        var high = lastIndex
        while (low < high) {
            val mid = (low + high) / 2
            if (frames[mid].elapsedSeconds < seconds) {
                low = mid + 1
            } else {
                high = mid
            }
        }

        val nextIndex = low
        val previousIndex = (nextIndex - 1).coerceAtLeast(0)
        val previous = frames[previousIndex]
        val next = frames[nextIndex]
        val span = (next.elapsedSeconds - previous.elapsedSeconds).coerceAtLeast(0.0001f)
        val blend = ((seconds - previous.elapsedSeconds) / span).coerceIn(0f, 1f)
        val frame = previous.copy(
            ball = lerp(previous.ball, next.ball, blend),
            speed = previous.speed + (next.speed - previous.speed) * blend,
            pulseIntensity = previous.pulseIntensity + (next.pulseIntensity - previous.pulseIntensity) * blend,
            outcome = next.outcome,
            riftAnchor = lerpNullable(previous.riftAnchor, next.riftAnchor, blend),
            riftStrength = previous.riftStrength + (next.riftStrength - previous.riftStrength) * blend,
            elapsedSeconds = seconds,
            powerTriggered = next.powerTriggered,
            impactStrength = next.impactStrength,
            portalTriggered = next.portalTriggered
        )
        return TimedReplayFrame(frame, nextIndex)
    }

    private fun lerp(a: Point2, b: Point2, blend: Float): Point2 {
        return Point2(a.x + (b.x - a.x) * blend, a.y + (b.y - a.y) * blend)
    }

    private fun lerpNullable(a: Point2?, b: Point2?, blend: Float): Point2? {
        if (a == null || b == null) return if (blend < 0.5f) a else b
        return lerp(a, b, blend)
    }

    private fun drawGameplayScrim(canvas: Canvas, payload: ReplaySharePayload, chaosMode: Boolean) {
        paint.style = Paint.Style.FILL
        paint.color = if (chaosMode) 0x5A07020A else 0x5202070D
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        paint.shader = android.graphics.LinearGradient(
            0f,
            0f,
            0f,
            canvas.height.toFloat(),
            intArrayOf(0xB8070A10.toInt(), 0x26070A10, 0x9A070A10.toInt()),
            floatArrayOf(0f, 0.44f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(payload.level.accent, 72)
        canvas.drawRect(0f, 0f, canvas.width * 0.008f, canvas.height.toFloat(), paint)
        canvas.drawRect(canvas.width * 0.992f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
    }

    private fun drawStageSideMask(
        canvas: Canvas,
        left: Float,
        top: Float,
        scale: Float,
        stageHeight: Float,
        accent: Int
    ) {
        val stageRight = left + STAGE_WIDTH * scale
        val stageBottom = top + stageHeight * scale
        paint.style = Paint.Style.FILL
        if (left > 1f) {
            paint.color = 0x9A07090F.toInt()
            canvas.drawRect(0f, 0f, left, canvas.height.toFloat(), paint)
            canvas.drawRect(stageRight, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = withAlpha(accent, 118)
            canvas.drawRect(left - 3f, 0f, left, canvas.height.toFloat(), paint)
            canvas.drawRect(stageRight, 0f, stageRight + 3f, canvas.height.toFloat(), paint)
        }
        if (top > 1f) {
            paint.color = 0x8A07090F.toInt()
            canvas.drawRect(left, 0f, stageRight, top, paint)
            canvas.drawRect(left, stageBottom, stageRight, canvas.height.toFloat(), paint)
        }
    }

    private fun drawGrid(canvas: Canvas, left: Float, top: Float, scale: Float, stageHeight: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = 0x18FFFFFF
        var x = 0
        while (x <= STAGE_WIDTH.toInt()) {
            val sx = left + x * scale
            canvas.drawLine(sx, top, sx, top + stageHeight * scale, paint)
            x += 1
        }
        var y = 0
        while (y <= stageHeight.toInt()) {
            val sy = top + y * scale
            canvas.drawLine(left, sy, left + STAGE_WIDTH * scale, sy, paint)
            y += 1
        }
    }

    private fun drawPulse(
        canvas: Canvas,
        zone: PulseZone,
        tx: (Float) -> Float,
        ty: (Float) -> Float,
        scale: Float,
        progress: Float,
        accent: Int
    ) {
        val cx = tx(zone.center.x)
        val cy = ty(zone.center.y)
        val r = zone.radius * scale
        val wave = 0.84f + 0.12f * sin(progress * PI.toFloat() * 8f + zone.phase)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 34)
        canvas.drawCircle(cx, cy, r * wave, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = withAlpha(accent, 170)
        canvas.drawCircle(cx, cy, r * wave, paint)

        paint.strokeWidth = 4f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xDDF7F4FF.toInt()
        repeat(3) { i ->
            val angle = progress * PI.toFloat() * 2f + i * PI.toFloat() * 2f / 3f
            val sign = if (zone.radialForce >= 0f) 1f else -1f
            val inner = r * if (sign > 0f) 0.24f else 0.42f
            val outer = r * if (sign > 0f) 0.42f else 0.24f
            canvas.drawLine(cx + cos(angle) * inner, cy + sin(angle) * inner, cx + cos(angle) * outer, cy + sin(angle) * outer, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        val coreSize = r * 0.9f
        val coreArt = if (zone.radialForce >= 0f) R.drawable.world_reactor_out else R.drawable.world_reactor_in
        drawAsset(canvas, coreArt, RectF(cx - coreSize, cy - coreSize, cx + coreSize, cy + coreSize))
    }

    private fun drawPortal(
        canvas: Canvas,
        portal: PortalPair,
        tx: (Float) -> Float,
        ty: (Float) -> Float,
        scale: Float,
        progress: Float
    ) {
        val entryX = tx(portal.entry.x)
        val entryY = ty(portal.entry.y)
        val exitX = tx(portal.exit.x)
        val exitY = ty(portal.exit.y)
        val r = portal.radius * scale

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 15f
        paint.color = 0x3145F2FF
        canvas.drawLine(entryX, entryY, exitX, exitY, paint)
        paint.strokeWidth = 4f
        paint.color = 0xB8FFCF4A.toInt()
        canvas.drawLine(entryX, entryY, exitX, exitY, paint)
        paint.strokeCap = Paint.Cap.BUTT

        drawPortalNode(canvas, entryX, entryY, r, progress, 0xFF45F2FF.toInt(), "IN", 1f + portal.phase)
        drawPortalNode(canvas, exitX, exitY, r, progress, 0xFFFFCF4A.toInt(), "OUT", -1f - portal.phase)
    }

    private fun drawPortalNode(canvas: Canvas, cx: Float, cy: Float, radius: Float, progress: Float, accent: Int, label: String, spin: Float) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(accent, 66)
        canvas.drawCircle(cx, cy, radius * 1.48f, paint)
        paint.style = Paint.Style.STROKE
        repeat(3) { ring ->
            val wave = ((progress * (2.2f + ring * 0.24f) + ring * 0.27f) % 1f + 1f) % 1f
            paint.strokeWidth = 3.2f - ring * 0.4f
            paint.color = withAlpha(accent, ((1f - wave) * 210f).roundToInt())
            canvas.drawCircle(cx, cy, radius * (0.64f + wave * 0.92f), paint)
        }
        val artSize = radius * 1.05f
        canvas.save()
        canvas.rotate(progress * 360f * spin, cx, cy)
        drawAsset(canvas, R.drawable.world_portal_goal, RectF(cx - artSize, cy - artSize, cx + artSize, cy + artSize))
        canvas.restore()

        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 18f
        textPaint.color = withAlpha(accent, 235)
        canvas.drawText(label, cx, cy + radius + 29f, textPaint)
    }

    private fun drawBlock(canvas: Canvas, block: Block, tx: (Float) -> Float, ty: (Float) -> Float, scale: Float, chaosMode: Boolean) {
        val cx = tx(block.center.x)
        val cy = ty(block.center.y)
        val hw = block.width * scale * 0.5f
        val hh = block.height * scale * 0.5f
        canvas.save()
        canvas.rotate(block.angleRadians * 180f / PI.toFloat(), cx, cy)
        val visualHalfHeight = max(hh, 6f)
        scratch.set(cx - hw, cy - visualHalfHeight, cx + hw, cy + visualHalfHeight)
        paint.style = Paint.Style.FILL
        drawAsset(
            canvas,
            if (chaosMode) R.drawable.world_platform_chaos else R.drawable.world_platform_classic,
            scratch
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0x44FFFFFF
        canvas.drawRoundRect(scratch, 7f, 7f, paint)
        canvas.restore()
    }

    private fun drawHazard(canvas: Canvas, hazard: Hazard, tx: (Float) -> Float, ty: (Float) -> Float, scale: Float, elapsed: Float) {
        val position = hazard.positionAt(elapsed)
        val cx = tx(position.x)
        val cy = ty(position.y)
        val r = hazard.radius * scale
        paint.style = Paint.Style.FILL
        paint.color = 0x33FF4D8D
        canvas.drawCircle(cx, cy, r * 1.55f, paint)
        val art = when (hazard.motion) {
            HazardMotion.STATIC -> R.drawable.world_hazard_static
            HazardMotion.HORIZONTAL, HazardMotion.VERTICAL -> R.drawable.world_hazard_glitch
            HazardMotion.ORBIT, HazardMotion.FIGURE_EIGHT -> R.drawable.world_hazard_void
        }
        canvas.save()
        canvas.rotate(elapsed * 95f + hazard.phase * 30f, cx, cy)
        drawAsset(canvas, art, RectF(cx - r * 1.22f, cy - r * 1.22f, cx + r * 1.22f, cy + r * 1.22f))
        canvas.restore()
    }

    private fun drawGoal(canvas: Canvas, level: LevelSpec, tx: (Float) -> Float, ty: (Float) -> Float, scale: Float, progress: Float) {
        val cx = tx(level.goal.x)
        val cy = ty(level.goal.y)
        val r = level.goalRadius * scale
        paint.style = Paint.Style.FILL
        paint.color = 0x4464E572
        canvas.drawCircle(cx, cy, r * (1.32f + 0.08f * sin(progress * PI.toFloat() * 6f)), paint)
        val size = r * 1.32f
        canvas.save()
        canvas.rotate(progress * 28f, cx, cy)
        drawAsset(canvas, R.drawable.world_portal_goal, RectF(cx - size, cy - size, cx + size, cy + size))
        canvas.restore()
    }

    private fun drawPolyline(
        canvas: Canvas,
        points: List<Point2>,
        tx: (Float) -> Float,
        ty: (Float) -> Float,
        stroke: Float,
        color: Int
    ) {
        if (points.isEmpty()) return
        paint.style = Paint.Style.FILL
        val visible = points.takeLast(120)
        visible.forEachIndexed { index, point ->
            if (index % 2 != 0) return@forEachIndexed
            val progress = index / visible.size.toFloat()
            paint.color = withAlpha(color, (24f + progress * 100f).roundToInt())
            canvas.drawCircle(tx(point.x), ty(point.y), (stroke * (0.28f + progress * 0.22f)).coerceAtLeast(2.5f), paint)
        }
    }

    private fun drawReplayTrail(canvas: Canvas, frames: List<PhysicsFrame>, tx: (Float) -> Float, ty: (Float) -> Float) {
        if (frames.size < 2) return
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 5.6f
        path.reset()
        frames.takeLast(36).forEachIndexed { index, frame ->
            if (index == 0) path.moveTo(tx(frame.ball.x), ty(frame.ball.y)) else path.lineTo(tx(frame.ball.x), ty(frame.ball.y))
        }
        paint.color = 0x72FFFFFF
        canvas.drawPath(path, paint)
        paint.strokeWidth = 2.6f
        paint.color = 0xAA45F2FF.toInt()
        canvas.drawPath(path, paint)
        paint.strokeCap = Paint.Cap.BUTT
    }

    private fun drawRiftTether(
        canvas: Canvas,
        ball: Point2,
        anchor: Point2,
        strength: Float,
        tx: (Float) -> Float,
        ty: (Float) -> Float,
        color: Int
    ) {
        val ballX = tx(ball.x)
        val ballY = ty(ball.y)
        val anchorX = tx(anchor.x)
        val anchorY = ty(anchor.y)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 18f
        paint.color = withAlpha(color, (36 + strength * 42f).roundToInt())
        canvas.drawLine(ballX, ballY, anchorX, anchorY, paint)
        paint.strokeWidth = 7f
        paint.color = withAlpha(color, (160 + strength * 80f).roundToInt())
        canvas.drawLine(ballX, ballY, anchorX, anchorY, paint)
        paint.strokeWidth = 2.5f
        paint.color = 0xE8FFFFFF.toInt()
        canvas.drawLine(ballX, ballY, anchorX, anchorY, paint)
        paint.strokeCap = Paint.Cap.BUTT

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(color, 76)
        canvas.drawCircle(anchorX, anchorY, 28f + strength * 9f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = withAlpha(color, 225)
        canvas.drawCircle(anchorX, anchorY, 16f + strength * 5f, paint)
    }

    private fun drawBall(canvas: Canvas, ball: Point2, tx: (Float) -> Float, ty: (Float) -> Float, scale: Float, payload: ReplaySharePayload) {
        val cx = tx(ball.x)
        val cy = ty(ball.y)
        val r = PhysicsEngine.BALL_RADIUS * scale
        val visualRadius = r * payload.ballVisualScale
        val wave = 0.18f * sin(cy * 0.015f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(payload.lineColor, 105)
        canvas.drawCircle(cx, cy, visualRadius * (1.55f + wave * 0.06f), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2.6f, visualRadius * 0.045f)
        paint.color = withAlpha(payload.ballSecondary, 150)
        scratch.set(cx - visualRadius * 1.14f, cy - visualRadius * 0.72f, cx + visualRadius * 1.14f, cy + visualRadius * 0.72f)
        canvas.save()
        canvas.rotate((cy * 0.26f) % 360f, cx, cy)
        canvas.drawOval(scratch, paint)
        canvas.restore()

        val artRadius = visualRadius * 1.1f
        val saveCount = canvas.save()
        path.reset()
        path.addCircle(cx, cy, artRadius * 0.985f, Path.Direction.CW)
        canvas.clipPath(path)
        drawAsset(canvas, payload.ballArtResource, RectF(cx - artRadius, cy - artRadius, cx + artRadius, cy + artRadius))
        canvas.restoreToCount(saveCount)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = visualRadius * 0.075f
        paint.color = withAlpha(payload.lineColor, 240)
        canvas.drawCircle(cx, cy, visualRadius * 1.05f, paint)
    }

    private fun drawTopOverlay(canvas: Canvas, payload: ReplaySharePayload) {
        paint.style = Paint.Style.FILL
        paint.color = 0xE807090F.toInt()
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), 132f, paint)

        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.textSize = 34f
        canvas.drawText("BRAINROT CHAOS: KAVVORO", 34f, 52f, textPaint)
        textPaint.textSize = 24f
        textPaint.color = payload.lineColor
        val archetype = payload.archetypeLabel.ifBlank { payload.level.title.uppercase() }
        canvas.drawText("${payload.modeLabel} L${payload.level.index.toString().padStart(2, '0')}  ${payload.ballName}", 34f, 90f, textPaint)
        textPaint.textSize = 19f
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText(archetype.take(34), 34f, 118f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = 0xFFFFCF4A.toInt()
        textPaint.textSize = 26f
        canvas.drawText("HYPE ${payload.hypeScore}", canvas.width - 34f, 72f, textPaint)
        if (payload.riftBreak) {
            textPaint.textSize = 20f
            textPaint.color = 0xFFF7F4FF.toInt()
            canvas.drawText(payload.riftBreakLabel.ifBlank { "RIFT BREAK" }.take(24), canvas.width - 34f, 104f, textPaint)
        }
    }

    private fun drawBottomOverlay(canvas: Canvas, payload: ReplaySharePayload, progress: Float) {
        val h = canvas.height.toFloat()
        paint.style = Paint.Style.FILL
        paint.color = 0xEA07090F.toInt()
        canvas.drawRect(0f, h - 174f, canvas.width.toFloat(), h, paint)

        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.textSize = 32f
        canvas.drawText(payload.resultLabel, canvas.width * 0.5f, h - 120f, textPaint)
        textPaint.textSize = 23f
        textPaint.color = 0xCCFFFFFF.toInt()
        val detail = payload.archetypeDetail.ifBlank { payload.curseLabel }
        canvas.drawText(detail.take(42), canvas.width * 0.5f, h - 82f, textPaint)
        textPaint.textSize = 24f
        textPaint.color = payload.level.accent
        canvas.drawText("BEAT THIS RUN  ${payload.challengeCode}", canvas.width * 0.5f, h - 40f, textPaint)

        paint.style = Paint.Style.FILL
        paint.color = payload.level.accent
        canvas.drawRect(0f, h - 6f, canvas.width * progress, h, paint)
    }

    private fun drawEndCard(canvas: Canvas, payload: ReplaySharePayload, progress: Float, chaosMode: Boolean) {
        val eased = (progress * progress * (3f - 2f * progress)).coerceIn(0f, 1f)
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(if (chaosMode) 0xFF07020A.toInt() else 0xFF07090F.toInt(), (218f * eased).roundToInt())
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

        val cx = canvas.width * 0.5f
        val cy = canvas.height * 0.43f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        repeat(5) { ring ->
            paint.strokeWidth = 5f - ring * 0.4f
            paint.color = withAlpha(if (ring % 2 == 0) payload.lineColor else 0xFFFFCF4A.toInt(), (190f * eased * (1f - ring * 0.12f)).roundToInt())
            canvas.drawCircle(cx, cy, 116f + ring * 26f + sin(progress * PI.toFloat() * 2f + ring) * 8f, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT

        drawBall(canvas, payload.level.goal, { cx }, { cy }, 112f / PhysicsEngine.BALL_RADIUS, payload)

        textPaint.typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = 0xFFF7F4FF.toInt()
        textPaint.textSize = 42f
        canvas.drawText(if (payload.riftBreak) "RIFT BREAK" else "BEAT THIS RIFT", cx, cy + 184f, textPaint)
        textPaint.textSize = 28f
        textPaint.color = payload.level.accent
        val result = if (payload.riftBreak && payload.riftBreakLabel.isNotBlank()) payload.riftBreakLabel else payload.resultLabel
        canvas.drawText(result.take(38), cx, cy + 226f, textPaint)
        textPaint.textSize = 25f
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText("${payload.ballName}  /  ${payload.challengeCode}", cx, cy + 266f, textPaint)
    }

    private fun videoSeconds(payload: ReplaySharePayload): Float {
        val first = payload.replayFrames.firstOrNull()?.elapsedSeconds ?: 0f
        val last = payload.replayFrames.lastOrNull()?.elapsedSeconds ?: first
        val replaySeconds = (last - first).coerceAtLeast(payload.runSeconds.takeIf { it > 0f } ?: 0.01f)
        return (replaySeconds + 1.2f).coerceIn(4.2f, 10.8f)
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
    }

    private fun drawAsset(canvas: Canvas, resource: Int, bounds: RectF, alpha: Int = 255) {
        val bitmap = bitmapCache[resource]
            ?: BitmapFactory.decodeResource(context.resources, resource)?.also { bitmapCache[resource] = it }
            ?: return
        paint.alpha = alpha.coerceIn(0, 255)
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, null, bounds, paint)
        paint.alpha = 255
    }

    private data class MuxerState(
        var trackIndex: Int = -1,
        var started: Boolean = false,
        var sampleIndex: Long = 0L
    )

    private data class TimedReplayFrame(
        val frame: PhysicsFrame,
        val index: Int
    )

    companion object {
        private const val MIME_TYPE = "video/avc"
        private const val VIDEO_WIDTH = 720
        private const val VIDEO_HEIGHT = 1280
        private const val FRAME_RATE = 30
        private const val VIDEO_SECONDS = 5
        private const val BIT_RATE = 3_600_000
    }
}
