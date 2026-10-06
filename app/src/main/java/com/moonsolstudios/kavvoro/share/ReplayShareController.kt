package com.moonsolstudios.kavvoro.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.PhysicsOutcome
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.engine.RunScore
import com.moonsolstudios.kavvoro.i18n.TutorialCopy
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.repository.BallSkinCatalog
import com.moonsolstudios.kavvoro.ui.screens.gameplay.LevelArchetype
import java.io.File
import kotlin.math.abs
import kotlin.math.atan2

data class ShareRequest(
    val payload: ReplaySharePayload,
    val text: String
)

/**
 * Dedicated controller for replay payload assembly, line simplification, challenge code generation,
 * share reward progress text, and Android share intent launching.
 */
object ReplayShareController {

    fun buildShareRequest(
        score: RunScore?,
        skin: BallSkin,
        gameMode: GameMode,
        modeMenuTitle: String,
        level: LevelSpec,
        playerLine: List<Point2>,
        replayFrames: List<PhysicsFrame>,
        ball: Point2,
        pulseIntensity: Float,
        state: GameState,
        lastHypeScore: Int,
        maxChain: Int,
        streak: Int,
        lastRiftBreak: Boolean,
        lastRiftBreakReason: String,
        archetype: LevelArchetype,
        simElapsed: Float,
        gameplayBallScale: Float,
        t: (String) -> String
    ): ShareRequest {
        val code = challengeCode(level.seed, level.index, lastHypeScore)
        val body = if (score != null) {
            t("Can you beat my Kavvoro rift?")
                .replace("%mode", modeMenuTitle)
                .replace("%level", "L${score.level}")
                .replace("%ball", skin.name)
                .replace("%rank", score.rank)
                .replace("%hype", lastHypeScore.toString())
                .replace("%chain", maxChain.toString())
                .replace("%streak", streak.toString())
                .replace("%code", code)
        } else {
            t("Trying Brainrot Chaos: Kavvoro")
                .replace("%mode", modeMenuTitle)
                .replace("%level", "L${level.index}")
                .replace("%ball", skin.name)
                .replace("%code", code)
        }
        val resultLabel = score?.let {
            "${t("RANK").uppercase()} ${it.rank}  ${"%.1f".format(it.seconds)}s  ${t("HYPE").uppercase()} $lastHypeScore"
        } ?: t("CRASH REPLAY").uppercase()
        val frames = replayFrames.ifEmpty {
            listOf(
                PhysicsFrame(
                    ball,
                    0f,
                    pulseIntensity,
                    if (state == GameState.WON) PhysicsOutcome.WON else PhysicsOutcome.LOST
                )
            )
        }
        return ShareRequest(
            payload = ReplaySharePayload(
                level = level,
                line = simplifyLine(playerLine),
                replayFrames = frames,
                modeLabel = gameMode.label,
                hypeScore = lastHypeScore,
                streak = streak,
                challengeCode = code,
                curseLabel = curseStackLabel(level, t),
                resultLabel = resultLabel,
                ballName = skin.name,
                ballPrimary = skin.primary,
                ballSecondary = skin.secondary,
                lineColor = skin.lineColor,
                ballArtResource = BallSkinCatalog.ART_RESOURCES[skin.id] ?: R.drawable.brainball_nodlo,
                ballVisualScale = gameplayBallScale,
                riftBreak = lastRiftBreak,
                riftBreakLabel = lastRiftBreakReason,
                archetypeLabel = t(archetype.label).uppercase(),
                archetypeDetail = t(archetype.detail),
                runSeconds = score?.seconds ?: simElapsed
            ),
            text = body
        )
    }

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

    fun challengeCode(levelSeed: Long, levelIndex: Int, lastHypeScore: Int): String {
        val hype = lastHypeScore.toLong().coerceAtLeast(17L)
        val mixed = levelSeed xor (levelIndex.toLong() * 0x9E3779B9L) xor (hype * 131L)
        val raw = (mixed ushr 1).toString(36).uppercase()
        return "KAV-" + raw.takeLast(6).padStart(6, '0')
    }

    fun curseStackLabel(level: LevelSpec, t: (String) -> String): String {
        if (level.curses.isEmpty()) return t("NO CURSE").uppercase()
        return level.curses.joinToString(" + ") { t(TutorialCopy.curseRibbonKey(it.type)).uppercase() }
    }

    fun nextShareRewardText(
        totalShares: Int,
        ballSkins: List<BallSkin>,
        isSkinUnlocked: (BallSkin) -> Boolean,
        t: (String) -> String
    ): String? {
        val skin = ballSkins
            .filter { it.unlock.type == UnlockType.SHARE_COUNT && !isSkinUnlocked(it) }
            .minByOrNull { it.unlock.value }
            ?: return null
        val remaining = (skin.unlock.value - totalShares).coerceAtLeast(0)
        return "${t("SHARE").uppercase()} $totalShares / ${skin.unlock.value}  /  ${skin.name} ${t("IN").uppercase()} $remaining"
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
}
