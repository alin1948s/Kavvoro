package com.moonsolstudios.kavvoro.ui.controller

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.FileProvider
import com.moonsolstudios.kavvoro.engine.CurseSpec
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.PhysicsOutcome
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.i18n.KavvoroNumberFormat
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.share.ReplaySharePayload
import com.moonsolstudios.kavvoro.share.ReplayVideoExporter
import com.moonsolstudios.kavvoro.telemetry.KavvoroTelemetry
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2

data class ShareRequest(
    val payload: ReplaySharePayload,
    val text: String
)

object ShareController {

    fun simplifyLine(points: List<Point2>): List<Point2> {
        if (points.size <= 2) return points.toList()
        val simplified = mutableListOf(points.first())
        var last = points.first()
        for (i in 1 until points.lastIndex) {
            val p = points[i]
            val next = points[i + 1]
            val angleA = atan2(p.y - last.y, p.x - last.x)
            val angleB = atan2(next.y - p.y, next.x - p.x)
            val angleDelta = abs(angleA - angleB)
            if (last.distanceTo(p) > 0.18f || angleDelta > 0.18f) {
                simplified += p
                last = p
            }
        }
        simplified += points.last()
        return simplified
    }

    fun shareVideo(context: Context, file: File, body: String, t: (String) -> String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_TITLE, t("Kavvoro 9:16 replay"))
            putExtra(Intent.EXTRA_SUBJECT, t("Beat my Kavvoro rift"))
            clipData = ClipData.newUri(context.contentResolver, t("Kavvoro replay"), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, t("Share Kavvoro short")))
    }

    fun shareText(context: Context, body: String, t: (String) -> String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_TITLE, t("Kavvoro challenge"))
            putExtra(Intent.EXTRA_SUBJECT, t("Beat my Kavvoro rift"))
        }
        context.startActivity(Intent.createChooser(intent, t("Share Kavvoro short")))
    }

    fun nextShareRewardText(
        ballSkins: List<BallSkin>,
        isSkinUnlocked: (BallSkin) -> Boolean,
        totalShares: Int,
        t: (String) -> String
    ): String? {
        val skin = ballSkins
            .filter { it.unlock.type == UnlockType.SHARE_COUNT && !isSkinUnlocked(it) }
            .minByOrNull { it.unlock.value }
            ?: return null
        val remaining = (skin.unlock.value - totalShares).coerceAtLeast(0)
        return "${t("SHARE").uppercase()} $totalShares / ${skin.unlock.value}  /  ${skin.name} ${t("IN").uppercase()} $remaining"
    }

    data class ShareRewardResult(
        val newSkin: BallSkin?,
        val rewardMessage: String
    )

    fun recordShareReward(
        prefs: SharedPreferences,
        shareCountKey: String,
        unlockedSkinIds: Set<String>,
        ballSkins: List<BallSkin>,
        getUnlockedSkinIdsAfter: () -> Set<String>,
        nextShareRewardText: (Int) -> String?,
        rewardLine: (BallSkin?) -> String
    ): ShareRewardResult {
        val before = unlockedSkinIds
        val used = prefs.getInt(shareCountKey, 0)
        val total = used + 1
        prefs.edit().putInt(shareCountKey, total).apply()
        val unlocked = getUnlockedSkinIdsAfter()
        val newSkin = ballSkins.firstOrNull { it.id in (unlocked - before) }
        val msg = if (newSkin != null) {
            rewardLine(newSkin)
        } else {
            nextShareRewardText(total) ?: rewardLine(null)
        }
        return ShareRewardResult(newSkin, msg)
    }

    fun levelArchetype(
        spec: LevelSpec,
        gameMode: GameMode
    ): com.moonsolstudios.kavvoro.model.LevelArchetype {
        val movingHazards = spec.hazards.count { it.isMoving }
        val hasCurse = { type: CurseType -> spec.curses.any { it.type == type } }
        return when {
            spec.portals.isNotEmpty() -> com.moonsolstudios.kavvoro.model.LevelArchetype("PORTAL SLING", "Teleport timing and launch control", 0xFF45F2FF.toInt(), "portal_goal")
            hasCurse(CurseType.RIFT_WIND) -> com.moonsolstudios.kavvoro.model.LevelArchetype("WIND TUNNEL", "Short bursts beat the gust", 0xFF8AA6FF.toInt(), "boost_recharge")
            hasCurse(CurseType.OVERHEAT) || hasCurse(CurseType.RIFT_DRAIN) -> com.moonsolstudios.kavvoro.model.LevelArchetype("ENERGY TAX", "Spend Rift in tiny snaps", 0xFFFF5757.toInt(), "danger_beacon")
            hasCurse(CurseType.FOCUS_FIELD) || hasCurse(CurseType.POWER_HOLD) -> com.moonsolstudios.kavvoro.model.LevelArchetype("CONTROL LAB", "Tap timing changes the pull", 0xFFFFCF4A.toInt(), "boost_plasma")
            spec.pulseZones.size >= 2 || hasCurse(CurseType.PULSE_STORM) -> com.moonsolstudios.kavvoro.model.LevelArchetype("PULSE MAZE", "Fields bend speed and direction", 0xFFC15CFF.toInt(), "boost_pulse")
            movingHazards >= 3 -> com.moonsolstudios.kavvoro.model.LevelArchetype("MOVING DANGER", "Read the lanes before committing", 0xFFFF4D8D.toInt(), "hazard_glitch")
            spec.blocks.size >= 5 -> com.moonsolstudios.kavvoro.model.LevelArchetype("GATE STACK", "Bounce angles matter", 0xFF1DE8C8.toInt(), "platform_classic")
            gameMode == GameMode.CHAOS -> com.moonsolstudios.kavvoro.model.LevelArchetype("CHAOS TOUCH", "Fast reactions, no sleepy holds", 0xFFFF4D8D.toInt(), "boost_chain")
            else -> com.moonsolstudios.kavvoro.model.LevelArchetype("RIFT PATH", "Clean control and smooth release", 0xFF1DE8C8.toInt(), "boost_rift_pull")
        }
    }

    fun curseStackLabel(
        curses: List<CurseSpec>,
        t: (String) -> String
    ): String {
        if (curses.isEmpty()) return t("NO CURSE").uppercase()
        return curses.joinToString(" + ") { t(it.name.uppercase()).uppercase() }
    }

    fun challengeCode(seed: Long, levelIndex: Int, lastHypeScore: Int): String {
        val hype = lastHypeScore.toLong().coerceAtLeast(17L)
        val mixed = seed xor (levelIndex.toLong() * 0x9E3779B9L) xor (hype * 131L)
        val raw = (mixed ushr 1).toString(36).uppercase()
        return if (raw.length >= 6) raw.takeLast(6) else raw.padStart(6, 'X')
    }

    fun createShareRequest(
        score: RunScore?,
        skin: BallSkin,
        level: LevelSpec,
        gameMode: GameMode,
        lastHypeScore: Int,
        maxChain: Int,
        streak: Int,
        playerLine: List<Point2>,
        replayFrames: List<PhysicsFrame>,
        state: GameState,
        ball: Point2,
        pulseIntensity: Float,
        brainballArtResourceId: Int,
        gameplayBallScale: Float,
        lastRiftBreak: Boolean,
        lastRiftBreakReason: String,
        simElapsed: Float,
        locale: Locale,
        t: (String) -> String
    ): ShareRequest {
        val chCode = challengeCode(level.seed, level.index, lastHypeScore)
        val cLabel = curseStackLabel(level.curses, t)
        val arch = levelArchetype(level, gameMode)
        val body = if (score != null) {
            t("Can you beat my Kavvoro rift?").replace("%mode", gameMode.menuTitle(t)).replace("%level", "L${score.level}")
                .replace("%ball", skin.name)
                .replace("%rank", score.rank)
                .replace("%hype", lastHypeScore.toString())
                .replace("%chain", maxChain.toString())
                .replace("%streak", streak.toString())
                .replace("%code", chCode)
        } else {
            t("Trying Brainrot Chaos: Kavvoro")
                .replace("%mode", gameMode.menuTitle(t))
                .replace("%level", "L${level.index}")
                .replace("%ball", skin.name)
                .replace("%code", chCode)
        }
        val resultLabel = score?.let {
            "${t("RANK").uppercase()} ${it.rank}  ${KavvoroNumberFormat.seconds(it.seconds, locale)}  ${t("HYPE").uppercase()} $lastHypeScore"
        } ?: t("CRASH REPLAY").uppercase()
        val frames = replayFrames.ifEmpty {
            listOf(PhysicsFrame(ball, 0f, pulseIntensity, if (state == GameState.WON) PhysicsOutcome.WON else PhysicsOutcome.LOST))
        }
        return ShareRequest(
            payload = ReplaySharePayload(
                level = level,
                line = simplifyLine(playerLine),
                replayFrames = frames,
                modeLabel = gameMode.label,
                hypeScore = lastHypeScore,
                streak = streak,
                challengeCode = chCode,
                curseLabel = cLabel,
                resultLabel = resultLabel,
                ballName = skin.name,
                ballPrimary = skin.primary,
                ballSecondary = skin.secondary,
                lineColor = skin.lineColor,
                ballArtResource = brainballArtResourceId,
                ballVisualScale = gameplayBallScale,
                riftBreak = lastRiftBreak,
                riftBreakLabel = lastRiftBreakReason,
                archetypeLabel = t(arch.label).uppercase(),
                archetypeDetail = t(arch.detail),
                runSeconds = score?.seconds ?: simElapsed
            ),
            text = body
        )
    }

    fun exportAndShare(
        context: Context,
        request: ShareRequest,
        onSuccess: (File, String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread(
            {
                val exporter = ReplayVideoExporter(context.applicationContext)
                try {
                    val video = exporter.export(request.payload)
                    onSuccess(video, request.text)
                } catch (error: Throwable) {
                    Log.e("KavvoroReplay", "Video export failed; falling back to text share", error)
                    KavvoroTelemetry.recordNonFatal(error, "replay_export")
                    onError(request.text)
                }
            },
            "kavvoro-replay-export"
        ).start()
    }
}
