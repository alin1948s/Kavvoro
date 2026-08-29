package com.moonsolstudios.kavvoro.engine

import kotlin.math.cos
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

const val STAGE_WIDTH = 10f
const val STAGE_WALL_INSET = 1.52f
const val STAGE_SAFE_CENTER_X = 1.9f
private const val MIN_MECHANICAL_SIDE_CORRIDOR = 0.84f

data class Point2(
    val x: Float,
    val y: Float
) {
    fun distanceTo(other: Point2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }
}

data class Block(
    val center: Point2,
    val width: Float,
    val height: Float,
    val angleRadians: Float,
    val tone: Int
)

enum class HazardMotion {
    STATIC,
    HORIZONTAL,
    VERTICAL,
    ORBIT,
    FIGURE_EIGHT
}

data class Hazard(
    val center: Point2,
    val radius: Float,
    val motion: HazardMotion = HazardMotion.STATIC,
    val travel: Float = 0f,
    val speed: Float = 0f,
    val phase: Float = 0f
) {
    fun positionAt(elapsedSeconds: Float): Point2 {
        if (motion == HazardMotion.STATIC || travel <= 0f || speed == 0f) return center
        val time = elapsedSeconds * speed + phase
        return when (motion) {
            HazardMotion.STATIC -> center
            HazardMotion.HORIZONTAL -> Point2(center.x + sin(time) * travel, center.y)
            HazardMotion.VERTICAL -> Point2(center.x, center.y + sin(time) * travel)
            HazardMotion.ORBIT -> Point2(
                center.x + cos(time) * travel,
                center.y + sin(time) * travel
            )
            HazardMotion.FIGURE_EIGHT -> Point2(
                center.x + sin(time) * travel,
                center.y + sin(time * 2f) * travel * 0.5f
            )
        }
    }

    val isMoving: Boolean
        get() = motion != HazardMotion.STATIC && travel > 0f && speed != 0f
}

data class PulseZone(
    val center: Point2,
    val radius: Float,
    val radialForce: Float,
    val swirlForce: Float,
    val phase: Float
)

data class PortalPair(
    val entry: Point2,
    val exit: Point2,
    val radius: Float = 0.48f,
    val phase: Float = 0f
)

enum class CurseType {
    RIFT_WIND,
    RIFT_DRAIN,
    HEAVY_CORE,
    MOON_GLIDE,
    FOCUS_FIELD,
    POWER_HOLD,
    PULSE_STORM,
    TINY_GATE,
    OVERHEAT
}

enum class BallPower {
    NONE,
    PRISM_SHIELD,
    VOID_PHASE,
    CHROME_RICOCHET,
    PLASMA_SURGE,
    MINOR_PHASE,
    MINOR_RICOCHET,
    MINOR_SURGE
}

data class CurseSpec(
    val type: CurseType,
    val name: String,
    val callout: String,
    val accent: Int
)

data class LevelSpec(
    val index: Int,
    val seed: Long,
    val title: String,
    val stageHeight: Float,
    val start: Point2,
    val goal: Point2,
    val goalRadius: Float,
    val inkLimit: Float,
    val drawSeconds: Float,
    val gravity: Float,
    val blocks: List<Block>,
    val hazards: List<Hazard>,
    val pulseZones: List<PulseZone>,
    val portals: List<PortalPair> = emptyList(),
    val accent: Int,
    val timeLimitSeconds: Float = 11.5f,
    val riftDrainMultiplier: Float = 1f,
    val difficultyRating: Int = 1,
    val curses: List<CurseSpec> = emptyList(),
    val mascotName: String = "KAVVI",
    val tutorialHint: String = ""
)

fun Point2.coerceToPlayableLane(extraMargin: Float = 0f): Point2 {
    val minX = (STAGE_SAFE_CENTER_X + extraMargin).coerceAtMost(STAGE_WIDTH * 0.5f)
    val maxX = (STAGE_WIDTH - STAGE_SAFE_CENTER_X - extraMargin).coerceAtLeast(STAGE_WIDTH * 0.5f)
    return copy(x = x.coerceIn(minX, maxX))
}

fun LevelSpec.withPlayableLaneBounds(): LevelSpec = copy(
    start = start.coerceToPlayableLane(),
    goal = goal.coerceToPlayableLane(),
    blocks = blocks.map(Block::withMechanicalSideCorridor),
    hazards = hazards.map { hazard ->
        hazard.copy(center = hazard.center.coerceToPlayableLane(hazard.radius * 0.35f))
    },
    pulseZones = pulseZones.map { pulse ->
        pulse.copy(center = pulse.center.coerceToPlayableLane(0.05f))
    },
    portals = portals.map { portal ->
        portal.copy(
            entry = portal.entry.coerceToPlayableLane(0.08f),
            exit = portal.exit.coerceToPlayableLane(0.08f)
        )
    }
)

private fun Block.withMechanicalSideCorridor(): Block {
    if (abs(angleRadians) > 0.08f) return this
    val leftWall = STAGE_WALL_INSET
    val rightWall = STAGE_WIDTH - STAGE_WALL_INSET
    val leftEdge = center.x - width * 0.5f
    val rightEdge = center.x + width * 0.5f
    if (leftEdge <= leftWall || rightEdge >= rightWall) return this

    val leftCorridor = leftEdge - leftWall
    val rightCorridor = rightWall - rightEdge
    if (leftCorridor >= MIN_MECHANICAL_SIDE_CORRIDOR || rightCorridor >= MIN_MECHANICAL_SIDE_CORRIDOR) return this

    val repairedLeft = leftWall + MIN_MECHANICAL_SIDE_CORRIDOR
    val repairedRight = rightWall - MIN_MECHANICAL_SIDE_CORRIDOR
    if (repairedRight <= repairedLeft) return this
    return copy(
        center = Point2((repairedLeft + repairedRight) * 0.5f, center.y),
        width = repairedRight - repairedLeft
    )
}

data class RunScore(
    val level: Int,
    val inkUsed: Float,
    val seconds: Float,
    val rank: String
)

enum class PhysicsOutcome {
    RUNNING,
    WON,
    LOST
}

data class PhysicsFrame(
    val ball: Point2,
    val speed: Float,
    val pulseIntensity: Float,
    val outcome: PhysicsOutcome,
    val riftAnchor: Point2? = null,
    val riftStrength: Float = 0f,
    val elapsedSeconds: Float = 0f,
    val powerTriggered: Boolean = false,
    val impactStrength: Float = 0f,
    val portalTriggered: Boolean = false
)
