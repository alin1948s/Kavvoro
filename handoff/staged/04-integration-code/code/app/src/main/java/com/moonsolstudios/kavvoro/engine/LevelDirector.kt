package com.moonsolstudios.kavvoro.engine

import kotlin.math.PI
import kotlin.math.max
import kotlin.random.Random

object LevelDirector {
    private const val DAY_MILLIS = 86_400_000L
    private const val TONE_STEEL = 0xFF293040.toInt()
    private const val TONE_VIOLET = 0xFF3B3046.toInt()
    private const val TONE_MOSS = 0xFF243B3A.toInt()
    private const val TONE_RIFT = 0xFF431934.toInt()

    fun dailySeed(): Long = System.currentTimeMillis() / DAY_MILLIS

    fun create(levelIndex: Int, stageHeight: Float, daySeed: Long = dailySeed()): LevelSpec {
        return createClassic(levelIndex, stageHeight, daySeed)
    }

    fun createClassic(levelIndex: Int, stageHeight: Float, daySeed: Long = dailySeed()): LevelSpec {
        val h = max(stageHeight, 15.5f)
        val seed = mix(daySeed, levelIndex.toLong())
        if (levelIndex <= 10) {
            return createClassicTutorial(levelIndex, h, seed)
        }
        if (levelIndex >= 15) {
            return AdvancedLevelDirector.createClassic(levelIndex, h, seed)
        }
        val random = Random(seed.toInt())
        val postTutorial = levelIndex - 11
        val step = postTutorial.mod(classicTitles.size)
        val cycle = postTutorial / classicTitles.size
        val blocks = mutableListOf<Block>()
        val hazards = mutableListOf<Hazard>()
        val pulses = mutableListOf<PulseZone>()
        val portals = mutableListOf<PortalPair>()
        val start = Point2(1.15f + random.nextFloat() * 0.35f, 2.0f + random.nextFloat() * 0.5f)
        var goal = Point2(8.35f, h - 2.35f)
        var gravity = 8.9f + cycle * 0.18f
        var inkLimit = 6.65f + cycle * 0.16f
        var drawSeconds = 3.35f
        var goalRadius = 0.58f
        var hint = ""

        when (step) {
            0 -> {
                goal = Point2(8.15f, h - 2.25f)
                blocks += block(5.7f, h * 0.72f, 2.9f, 0.26f, -0.12f)
                pulses += pulse(3.15f, h * 0.47f, 1.25f, -1.4f, 0.8f)
                inkLimit = 6.6f
            }

            1 -> {
                portals += PortalPair(Point2(3.05f, h * 0.54f), Point2(6.95f, h * 0.42f), 0.54f, 0.2f)
                goal = Point2(8.4f, h - 2.75f)
                blocks += block(4.9f, h * 0.57f, 2.2f, 0.24f, 0.16f, TONE_VIOLET)
                blocks += block(7.05f, h * 0.74f, 1.6f, 0.25f, -0.18f)
                hazards += Hazard(Point2(5.45f, h * 0.69f), 0.38f)
                pulses += pulse(3.1f, h * 0.43f, 1.4f, -2.0f, 1.0f)
                hint = "PORTAL: enter IN, exit OUT with speed toward the goal."
            }

            2 -> {
                goal = Point2(8.0f, h - 3.2f)
                blocks += block(3.55f, h * 0.63f, 1.45f, 0.24f, -0.5f, TONE_MOSS)
                blocks += block(6.5f, h * 0.54f, 1.9f, 0.24f, 0.44f, TONE_VIOLET)
                hazards += Hazard(Point2(4.75f, h * 0.73f), 0.42f)
                pulses += pulse(7.35f, h * 0.43f, 1.3f, 1.8f, -1.6f, PI.toFloat() * 0.5f)
                inkLimit = 6.45f
            }

            3 -> {
                goal = Point2(8.55f, h - 2.4f)
                blocks += block(5.1f, h * 0.42f, 4.1f, 0.22f, 0.02f)
                blocks += block(5.7f, h * 0.68f, 2.6f, 0.25f, -0.18f, TONE_MOSS)
                hazards += Hazard(Point2(3.1f, h * 0.58f), 0.34f)
                hazards += Hazard(Point2(7.05f, h * 0.59f), 0.34f)
                pulses += pulse(4.95f, h * 0.54f, 1.15f, -2.2f, 1.8f)
            }

            4 -> {
                goal = Point2(8.35f, h - 2.9f)
                blocks += block(4.0f, h * 0.74f, 1.6f, 0.24f, -0.38f)
                blocks += block(6.4f, h * 0.62f, 1.7f, 0.24f, 0.38f, TONE_VIOLET)
                hazards += Hazard(Point2(5.2f, h * 0.7f), 0.5f)
                pulses += pulse(2.75f, h * 0.48f, 1.35f, -2.7f, 1.5f)
                pulses += pulse(7.2f, h * 0.55f, 1.35f, 2.1f, -1.7f, PI.toFloat())
                inkLimit = 6.85f
            }

            5 -> {
                goal = Point2(7.9f, h - 3.8f)
                blocks += block(3.0f, h * 0.55f, 1.1f, 0.25f, 0.7f, TONE_MOSS)
                blocks += block(5.25f, h * 0.66f, 1.8f, 0.24f, -0.15f)
                blocks += block(7.6f, h * 0.49f, 1.2f, 0.24f, -0.65f, TONE_VIOLET)
                hazards += Hazard(Point2(6.1f, h * 0.78f), 0.42f)
                pulses += pulse(5f, h * 0.47f, 1.65f, -1.0f, 2.4f)
                gravity = 9.0f
            }

            6 -> {
                goal = Point2(8.55f, h - 2.2f)
                blocks += block(2.55f, h * 0.68f, 1.2f, 0.24f, 0.34f)
                blocks += block(5.05f, h * 0.56f, 1.2f, 0.24f, -0.34f, TONE_MOSS)
                blocks += block(7.55f, h * 0.7f, 1.2f, 0.24f, 0.34f, TONE_VIOLET)
                hazards += Hazard(Point2(3.9f, h * 0.73f), 0.36f)
                hazards += Hazard(Point2(6.45f, h * 0.62f), 0.36f)
                pulses += pulse(4.85f, h * 0.42f, 1.25f, -2.5f, -1.4f)
                inkLimit = 7.0f
            }

            7 -> {
                goal = Point2(8.1f, h - 4.0f)
                blocks += block(3.75f, h * 0.76f, 2.0f, 0.24f, -0.58f)
                blocks += block(7.0f, h * 0.37f, 2.2f, 0.24f, 0.38f, TONE_MOSS)
                hazards += Hazard(Point2(5.35f, h * 0.57f), 0.48f)
                hazards += Hazard(Point2(7.55f, h * 0.72f), 0.36f)
                pulses += pulse(3.0f, h * 0.45f, 1.2f, 1.5f, 2.2f)
                pulses += pulse(6.6f, h * 0.61f, 1.35f, -2.8f, -2.0f)
                drawSeconds = 3.05f
            }

            8 -> {
                goal = Point2(8.35f, h - 2.55f)
                blocks += block(3.1f, h * 0.48f, 2.15f, 0.23f, 0.3f, TONE_MOSS)
                blocks += block(6.0f, h * 0.58f, 1.35f, 0.23f, -0.62f, TONE_VIOLET)
                blocks += block(8.05f, h * 0.76f, 1.6f, 0.23f, 0.18f)
                pulses += pulse(4.55f, h * 0.72f, 1.25f, 1.9f, -1.1f)
                drawSeconds = 3.45f
                inkLimit = 6.75f
            }

            9 -> {
                goal = Point2(7.65f, h - 4.55f)
                blocks += block(2.95f, h * 0.73f, 1.8f, 0.23f, -0.42f)
                blocks += block(5.15f, h * 0.47f, 1.65f, 0.23f, 0.5f, TONE_MOSS)
                blocks += block(7.45f, h * 0.62f, 1.85f, 0.23f, -0.38f, TONE_VIOLET)
                hazards += Hazard(Point2(4.2f, h * 0.67f), 0.34f)
                pulses += pulse(6.8f, h * 0.38f, 1.25f, -1.6f, 2.0f)
                inkLimit = 7.05f
            }

            10 -> {
                goal = Point2(8.55f, h - 2.65f)
                blocks += block(3.0f, h * 0.6f, 1.2f, 0.23f, 0.68f, TONE_MOSS)
                blocks += block(4.75f, h * 0.68f, 1.2f, 0.23f, -0.68f, TONE_VIOLET)
                blocks += block(6.5f, h * 0.6f, 1.2f, 0.23f, 0.68f, TONE_MOSS)
                blocks += block(8.1f, h * 0.72f, 1.1f, 0.23f, -0.35f)
                hazards += Hazard(Point2(5.6f, h * 0.82f), 0.36f)
                pulses += pulse(4.9f, h * 0.45f, 1.4f, -2.3f, 1.4f)
                drawSeconds = 3.25f
            }

            11 -> {
                goal = Point2(8.05f, h - 3.05f)
                blocks += block(3.25f, h * 0.78f, 2.4f, 0.24f, -0.28f)
                blocks += block(5.85f, h * 0.53f, 2.2f, 0.24f, 0.22f, TONE_MOSS)
                blocks += block(7.85f, h * 0.42f, 1.4f, 0.24f, -0.45f, TONE_VIOLET)
                hazards += Hazard(Point2(6.8f, h * 0.7f), 0.33f)
                pulses += pulse(2.8f, h * 0.5f, 1.15f, 1.8f, 1.2f)
                pulses += pulse(7.4f, h * 0.59f, 1.05f, -2.2f, -1.4f)
                inkLimit = 6.95f
            }
        }

        addSeedVariation(postTutorial, h, random, blocks, hazards, pulses)
        addClassicEscalation(cycle, h, random, blocks, hazards, pulses)
        val curses = classicCurses(levelIndex, step, cycle)
        if (hasCurse(curses, CurseType.PULSE_STORM)) {
            pulses += pulse(5f, h * 0.52f, 1.45f, -2.7f, 2.8f, PI.toFloat() * 0.35f)
        }
        gravity = tunedGravity(gravity, curses)
        inkLimit = tunedInkLimit(inkLimit, curses)
        drawSeconds = tunedDrawSeconds(drawSeconds, curses)
        goalRadius = tunedGoalRadius(goalRadius, curses)

        return LevelSpec(
            index = levelIndex,
            seed = seed,
            title = if (portals.isNotEmpty()) "Portal Sling" else classicTitles[step],
            stageHeight = h,
            start = Point2(start.x, max(start.y, 3.35f)),
            goal = goal,
            goalRadius = goalRadius,
            inkLimit = inkLimit,
            drawSeconds = drawSeconds,
            gravity = gravity,
            blocks = blocks,
            hazards = hazards,
            pulseZones = pulses,
            portals = portals,
            accent = classicAccents[step.mod(classicAccents.size)],
            curses = curses,
            mascotName = mascotFor(seed, levelIndex),
            tutorialHint = hint
        ).withPlayableLaneBounds()
    }

    fun createChaos(levelIndex: Int, stageHeight: Float, daySeed: Long = dailySeed()): LevelSpec {
        val h = max(stageHeight, 15.5f)
        val seed = mix(daySeed xor 0x51A0C0A5L, levelIndex.toLong() * 7L)
        if (levelIndex <= 10) {
            return createChaosTutorial(levelIndex, h, seed)
        }
        if (levelIndex >= 15) {
            return AdvancedLevelDirector.createChaos(levelIndex, h, seed)
        }
        val random = Random(seed.toInt())
        val postTutorial = levelIndex - 11
        val step = postTutorial.mod(chaosTitles.size)
        val cycle = postTutorial / chaosTitles.size
        val blocks = mutableListOf<Block>()
        val hazards = mutableListOf<Hazard>()
        val pulses = mutableListOf<PulseZone>()
        val portals = mutableListOf<PortalPair>()
        var start = Point2(1.1f + random.nextFloat() * 0.45f, 2.0f + random.nextFloat() * 0.8f)
        var goal = Point2(8.45f, h - 2.4f)
        var gravity = 8.1f + random.nextFloat() * 2.1f
        var inkLimit = 6.65f + cycle * 0.14f
        var drawSeconds = 3.05f
        var goalRadius = 0.54f
        var hint = ""

        when (step) {
            0 -> {
                goal = Point2(8.5f, h - 4.1f)
                blocks += block(2.8f, h * 0.48f, 1.6f, 0.24f, 0.82f, TONE_RIFT)
                blocks += block(5.2f, h * 0.63f, 1.7f, 0.24f, -0.72f, TONE_VIOLET)
                blocks += block(7.4f, h * 0.48f, 1.6f, 0.24f, 0.82f, TONE_RIFT)
                hazards += Hazard(Point2(4.15f, h * 0.72f), 0.48f)
                hazards += Hazard(Point2(6.2f, h * 0.37f), 0.42f)
                pulses += pulse(4.8f, h * 0.46f, 1.9f, -3.2f, 3.0f)
                pulses += pulse(7.5f, h * 0.66f, 1.4f, 2.9f, -2.6f, PI.toFloat())
            }

            1 -> {
                start = Point2(5.0f, 2.0f)
                goal = Point2(5.0f, h - 2.3f)
                portals += PortalPair(Point2(2.65f, h * 0.5f), Point2(7.25f, h * 0.43f), 0.52f, 0.6f)
                blocks += block(2.2f, h * 0.44f, 1.7f, 0.24f, 0.42f, TONE_RIFT)
                blocks += block(7.8f, h * 0.44f, 1.7f, 0.24f, -0.42f, TONE_RIFT)
                blocks += block(3.2f, h * 0.73f, 1.5f, 0.24f, -0.56f, TONE_MOSS)
                blocks += block(6.8f, h * 0.73f, 1.5f, 0.24f, 0.56f, TONE_MOSS)
                hazards += Hazard(Point2(5f, h * 0.56f), 0.58f)
                pulses += pulse(5f, h * 0.54f, 2.25f, -3.8f, 4.2f)
                pulses += pulse(5f, h * 0.78f, 1.55f, 3.0f, -3.2f, PI.toFloat() * 0.75f)
                gravity = 7.6f
                inkLimit = 7.25f
                hint = "PORTAL: IN teleports to OUT. Aim for the exit after launch."
            }

            2 -> {
                goal = Point2(8.25f, h - 2.15f)
                repeat(5) { i ->
                    val x = 2.0f + i * 1.55f
                    blocks += block(x, h * (0.42f + (i % 2) * 0.16f), 1.0f, 0.22f, if (i % 2 == 0) 0.72f else -0.72f, TONE_RIFT)
                    hazards += Hazard(Point2(x + 0.6f, h * (0.68f - (i % 2) * 0.1f)), 0.32f)
                }
                pulses += pulse(3.1f, h * 0.78f, 1.25f, 2.4f, 2.6f)
                pulses += pulse(6.9f, h * 0.35f, 1.5f, -3.4f, -2.4f)
                gravity = 11.2f
            }

            3 -> {
                start = Point2(8.8f, 2.4f)
                goal = Point2(1.25f, h - 2.7f)
                blocks += block(6.9f, h * 0.42f, 2.0f, 0.24f, -0.4f, TONE_RIFT)
                blocks += block(3.4f, h * 0.55f, 2.3f, 0.24f, 0.35f, TONE_VIOLET)
                blocks += block(6.2f, h * 0.76f, 2.0f, 0.24f, -0.25f, TONE_MOSS)
                hazards += Hazard(Point2(5.1f, h * 0.62f), 0.52f)
                hazards += Hazard(Point2(2.65f, h * 0.78f), 0.38f)
                pulses += pulse(7.7f, h * 0.55f, 1.55f, -3.0f, -3.4f)
                pulses += pulse(2.8f, h * 0.44f, 1.25f, 2.4f, 2.8f)
            }

            4 -> {
                goal = Point2(8.65f, h - 3.2f)
                blocks += block(4.7f, h * 0.44f, 4.9f, 0.22f, 0.0f, TONE_RIFT)
                blocks += block(5.3f, h * 0.78f, 4.6f, 0.22f, 0.0f, TONE_MOSS)
                hazards += Hazard(Point2(2.55f, h * 0.62f), 0.42f)
                hazards += Hazard(Point2(5.0f, h * 0.61f), 0.42f)
                hazards += Hazard(Point2(7.45f, h * 0.62f), 0.42f)
                pulses += pulse(5f, h * 0.61f, 2.25f, -4.2f, 0.4f)
                inkLimit = 5.75f
            }

            5 -> {
                goal = Point2(8.15f, h - 2.2f)
                repeat(4) { i ->
                    val x = 2.4f + random.nextFloat() * 5.2f
                    val y = h * (0.38f + random.nextFloat() * 0.42f)
                    blocks += block(x, y, 1.1f + random.nextFloat() * 1.2f, 0.22f, -0.95f + random.nextFloat() * 1.9f, if (i % 2 == 0) TONE_RIFT else TONE_VIOLET)
                }
                repeat(4) {
                    hazards += Hazard(Point2(2.0f + random.nextFloat() * 6.2f, h * (0.38f + random.nextFloat() * 0.47f)), 0.3f + random.nextFloat() * 0.2f)
                }
                repeat(3) {
                    pulses += pulse(2.2f + random.nextFloat() * 6.1f, h * (0.36f + random.nextFloat() * 0.45f), 1.15f + random.nextFloat() * 0.7f, -4f + random.nextFloat() * 8f, -4f + random.nextFloat() * 8f)
                }
                gravity = 8.2f + random.nextFloat() * 4.4f
                inkLimit = 6.9f
            }

            6 -> {
                goal = Point2(8.45f, h - 4.25f)
                blocks += block(2.5f, h * 0.74f, 1.8f, 0.22f, -0.7f, TONE_RIFT)
                blocks += block(4.8f, h * 0.52f, 1.25f, 0.22f, 0.8f, TONE_MOSS)
                blocks += block(7.35f, h * 0.7f, 2.0f, 0.22f, -0.28f, TONE_VIOLET)
                hazards += Hazard(Point2(5.85f, h * 0.77f), 0.36f)
                pulses += pulse(3.35f, h * 0.45f, 1.2f, 2.2f, 2.8f)
                pulses += pulse(7.3f, h * 0.52f, 1.1f, -3.1f, -1.2f)
                inkLimit = 7.1f
            }

            7 -> {
                start = Point2(8.65f, 2.1f)
                goal = Point2(1.35f, h - 2.45f)
                blocks += block(7.35f, h * 0.56f, 1.55f, 0.22f, -0.62f, TONE_RIFT)
                blocks += block(5.15f, h * 0.72f, 1.3f, 0.22f, 0.72f, TONE_MOSS)
                blocks += block(2.95f, h * 0.58f, 1.7f, 0.22f, -0.34f, TONE_VIOLET)
                hazards += Hazard(Point2(4.35f, h * 0.46f), 0.4f)
                pulses += pulse(6.6f, h * 0.44f, 1.45f, -3.0f, 2.0f)
                drawSeconds = 3.2f
            }

            8 -> {
                goal = Point2(8.35f, h - 2.75f)
                repeat(6) { i ->
                    val x = 1.9f + i * 1.35f
                    val y = h * (0.46f + if (i % 2 == 0) 0.2f else -0.02f)
                    blocks += block(x, y, 0.9f, 0.21f, if (i % 2 == 0) -0.82f else 0.82f, if (i % 3 == 0) TONE_RIFT else TONE_VIOLET)
                }
                hazards += Hazard(Point2(3.45f, h * 0.8f), 0.3f)
                hazards += Hazard(Point2(6.35f, h * 0.37f), 0.3f)
                pulses += pulse(5.15f, h * 0.62f, 1.6f, -3.5f, 0.9f)
                inkLimit = 7.25f
            }

            9 -> {
                start = Point2(5.0f, 2.05f)
                goal = Point2(8.25f, h - 3.25f)
                blocks += block(2.25f, h * 0.62f, 1.5f, 0.22f, -0.3f, TONE_MOSS)
                blocks += block(5.0f, h * 0.78f, 2.6f, 0.22f, 0.0f, TONE_RIFT)
                blocks += block(7.75f, h * 0.54f, 1.6f, 0.22f, 0.42f, TONE_VIOLET)
                hazards += Hazard(Point2(5f, h * 0.61f), 0.42f)
                pulses += pulse(3.0f, h * 0.44f, 1.25f, 2.8f, -2.0f)
                pulses += pulse(7.1f, h * 0.74f, 1.35f, -2.6f, 2.6f)
                gravity = 9.1f
                inkLimit = 7.0f
            }
        }

        addSeedVariation(postTutorial + 17, h, random, blocks, hazards, pulses)
        if (cycle > 0) {
            repeat(cycle.coerceAtMost(4)) {
                hazards += Hazard(Point2(1.8f + random.nextFloat() * 6.8f, h * (0.36f + random.nextFloat() * 0.5f)), 0.28f + random.nextFloat() * 0.16f)
                pulses += pulse(2.2f + random.nextFloat() * 5.9f, h * (0.34f + random.nextFloat() * 0.48f), 0.9f + random.nextFloat() * 0.55f, -3.5f + random.nextFloat() * 7f, -3.5f + random.nextFloat() * 7f)
            }
        }
        val stackSize = (1 + cycle + if (step >= 4) 1 else 0).coerceIn(1, 3)
        val curses = chainCurses(seed, levelIndex, stackSize)
        if (hasCurse(curses, CurseType.PULSE_STORM)) {
            pulses += pulse(2.0f + random.nextFloat() * 6.2f, h * (0.35f + random.nextFloat() * 0.42f), 1.25f + random.nextFloat() * 0.45f, -5.2f + random.nextFloat() * 10.4f, -5.2f + random.nextFloat() * 10.4f)
        }
        gravity = tunedGravity(gravity, curses)
        inkLimit = tunedInkLimit(inkLimit, curses)
        drawSeconds = tunedDrawSeconds(drawSeconds, curses)
        goalRadius = tunedGoalRadius(goalRadius, curses)

        return LevelSpec(
            index = levelIndex,
            seed = seed,
            title = if (portals.isNotEmpty()) "Portal Brainrot" else chaosTitles[step],
            stageHeight = h,
            start = Point2(start.x, max(start.y, 3.35f)),
            goal = goal,
            goalRadius = goalRadius,
            inkLimit = inkLimit,
            drawSeconds = drawSeconds,
            gravity = gravity,
            blocks = blocks,
            hazards = hazards,
            pulseZones = pulses,
            portals = portals,
            accent = chaosAccents[step.mod(chaosAccents.size)],
            curses = curses,
            mascotName = mascotFor(seed, levelIndex),
            tutorialHint = hint
        ).withPlayableLaneBounds()
    }

    private fun classicCurses(levelIndex: Int, step: Int, cycle: Int): List<CurseSpec> {
        val curses = mutableListOf<CurseSpec>()
        when (step) {
            2 -> curses += curse(CurseType.POWER_HOLD)
            3 -> curses += curse(CurseType.OVERHEAT)
            4 -> curses += curse(CurseType.PULSE_STORM)
            5 -> curses += curse(CurseType.MOON_GLIDE)
            6 -> curses += curse(CurseType.RIFT_DRAIN)
            7 -> curses += curse(CurseType.RIFT_WIND)
            8 -> curses += curse(CurseType.FOCUS_FIELD)
            9 -> curses += curse(CurseType.TINY_GATE)
            10 -> curses += curse(CurseType.POWER_HOLD)
            11 -> curses += curse(CurseType.HEAVY_CORE)
        }
        if (cycle > 0 && step % 4 == 0) curses += curse(CurseType.TINY_GATE)
        return curses.distinctBy { it.type }
    }

    private fun createClassicTutorial(levelIndex: Int, h: Float, seed: Long): LevelSpec {
        val blocks = mutableListOf<Block>()
        val hazards = mutableListOf<Hazard>()
        val pulses = mutableListOf<PulseZone>()
        var start = Point2(1.25f, 2.15f)
        var goal = Point2(8.35f, h - 2.15f)
        var gravity = 7.45f
        var inkLimit = 9.35f
        var drawSeconds = 6.15f
        var goalRadius = 0.95f
        val curses = mutableListOf<CurseSpec>()
        var title = "Rift Touch"
        var hint = "Hold anywhere to pull the ball toward your finger."
        var accent = 0xFF1DE8C8.toInt()

        when (levelIndex) {
            1 -> {
                blocks += block(5.15f, h * 0.69f, 3.4f, 0.24f, -0.05f)
                hint = "HOLD to create a gravity tether. Drag it toward the goal."
            }

            2 -> {
                title = "Orbit Curve"
                pulses += pulse(4.65f, h * 0.54f, 2.0f, -1.7f, 1.25f)
                blocks += block(6.25f, h * 0.73f, 2.55f, 0.24f, -0.16f, TONE_MOSS)
                hint = "DRAG around the ball to curve its flight. Pulse zones add force."
                inkLimit = 9.6f
                accent = 0xFFFFCF4A.toInt()
            }

            3 -> {
                title = "Brake & Coast"
                blocks += block(3.9f, h * 0.64f, 1.8f, 0.24f, -0.18f)
                blocks += block(6.35f, h * 0.69f, 2.0f, 0.24f, 0.12f, TONE_MOSS)
                hint = "Hold behind the ball to brake and turn. Release to let it coast."
                inkLimit = 9.75f
                accent = 0xFF64E572.toInt()
            }

            4 -> {
                title = "Hazard Dodge"
                blocks += block(4.55f, h * 0.62f, 2.3f, 0.24f, -0.14f, TONE_VIOLET)
                blocks += block(7.15f, h * 0.75f, 1.55f, 0.24f, -0.18f)
                hazards += Hazard(Point2(5.85f, h * 0.82f), 0.28f)
                hint = "Use short pulls to dodge the pink crash node."
                goalRadius = 1.0f
                accent = 0xFFFF8C42.toInt()
            }

            5 -> {
                title = "Pulse Chain"
                pulses += pulse(4.8f, h * 0.52f, 1.7f, -1.8f, 1.4f)
                blocks += block(3.9f, h * 0.61f, 1.6f, 0.24f, -0.2f, TONE_MOSS)
                blocks += block(6.7f, h * 0.72f, 2.1f, 0.24f, -0.13f, TONE_VIOLET)
                hazards += Hazard(Point2(7.55f, h * 0.84f), 0.3f)
                hint = "Hold through the pulse, then coast into the goal to build a chain."
                inkLimit = 9.9f
                goalRadius = 0.96f
                accent = 0xFF8AA6FF.toInt()
            }

            6 -> {
                title = "Rift Drain"
                curses += curse(CurseType.RIFT_DRAIN)
                blocks += block(5.1f, h * 0.68f, 3.0f, 0.24f, -0.07f, TONE_MOSS)
                hint = "SPECIAL: energy drains faster while the tether is active."
                inkLimit = 9.55f
                accent = 0xFF64E572.toInt()
            }

            7 -> {
                title = "Pulse Guard"
                curses += curse(CurseType.PULSE_STORM)
                pulses += pulse(4.9f, h * 0.53f, 2.0f, -2.0f, 2.1f)
                blocks += block(6.35f, h * 0.72f, 2.2f, 0.24f, -0.17f, TONE_VIOLET)
                hint = "SPECIAL: pulses are stronger, but HOLD weakens their force."
                inkLimit = 9.35f
                accent = 0xFFC15CFF.toInt()
            }

            8 -> {
                title = "Focus Heavy"
                curses += curse(CurseType.FOCUS_FIELD)
                curses += curse(CurseType.HEAVY_CORE)
                blocks += block(4.6f, h * 0.59f, 1.7f, 0.24f, -0.16f, TONE_MOSS)
                blocks += block(6.75f, h * 0.72f, 1.8f, 0.24f, -0.18f)
                hint = "SPECIAL: HOLD slows the ball while heavy gravity pulls down."
                gravity = 7.65f
                inkLimit = 9.4f
                accent = 0xFFFFCF4A.toInt()
            }

            9 -> {
                title = "Power Moon"
                curses += curse(CurseType.POWER_HOLD)
                curses += curse(CurseType.MOON_GLIDE)
                pulses += pulse(5.8f, h * 0.55f, 1.35f, -1.3f, 1.4f)
                blocks += block(4.0f, h * 0.65f, 1.5f, 0.24f, -0.25f, TONE_RIFT)
                hint = "SPECIAL: HOLD builds pull power while Moon Glide keeps momentum."
                gravity = 6.95f
                goalRadius = 0.9f
                accent = 0xFF45F2FF.toInt()
            }

            10 -> {
                title = "Wind Control"
                start = Point2(1.55f, 2.2f)
                goal = Point2(8.15f, h - 2.18f)
                curses += curse(CurseType.RIFT_WIND)
                curses += curse(CurseType.OVERHEAT)
                curses += curse(CurseType.TINY_GATE)
                blocks += block(5.65f, h * 0.68f, 2.6f, 0.24f, -0.12f, TONE_VIOLET)
                hint = "SPECIAL: HOLD blocks wind, but Overheat forces short bursts."
                inkLimit = 9.2f
                drawSeconds = 6.3f
                goalRadius = 1.0f
                accent = 0xFF8AA6FF.toInt()
            }
        }

        return tutorialSpec(levelIndex, seed, title, h, start, goal, goalRadius, inkLimit, drawSeconds, gravity, blocks, hazards, pulses, accent, curses, hint)
    }

    private fun createChaosTutorial(levelIndex: Int, h: Float, seed: Long): LevelSpec {
        val blocks = mutableListOf<Block>()
        val hazards = mutableListOf<Hazard>()
        val pulses = mutableListOf<PulseZone>()
        var start = Point2(1.35f, 2.2f)
        var goal = Point2(8.25f, h - 2.35f)
        var gravity = 7.8f
        var inkLimit = 9.1f
        var drawSeconds = 5.9f
        var goalRadius = 0.9f
        val curses = mutableListOf<CurseSpec>()
        var title = "Chaos Touch"
        var hint = "Hold anywhere to pull the ball. Chaos starts forgiving."
        var accent = 0xFFFF4D8D.toInt()

        when (levelIndex) {
            1 -> {
                title = "Chaos Touch"
                blocks += block(5.1f, h * 0.68f, 3.1f, 0.24f, -0.08f, TONE_RIFT)
                hint = "HOLD and drag the rift toward the goal. This one is very easy."
            }

            2 -> {
                title = "Chaos Orbit"
                pulses += pulse(4.75f, h * 0.53f, 1.95f, -1.9f, 1.8f)
                blocks += block(6.4f, h * 0.72f, 2.25f, 0.24f, -0.18f, TONE_VIOLET)
                hint = "Curve around the bright force zone and let it boost the ball."
                inkLimit = 9.35f
                accent = 0xFFC15CFF.toInt()
            }

            3 -> {
                title = "Chaos Coast"
                blocks += block(3.65f, h * 0.63f, 1.55f, 0.24f, -0.22f, TONE_RIFT)
                blocks += block(6.25f, h * 0.68f, 1.85f, 0.24f, 0.14f, TONE_MOSS)
                hint = "HOLD to steer, then release early and coast through the gap."
                inkLimit = 9.45f
                accent = 0xFFFFCF4A.toInt()
            }

            4 -> {
                title = "Crash Dodge"
                blocks += block(4.35f, h * 0.61f, 2.1f, 0.24f, -0.15f, TONE_VIOLET)
                hazards += Hazard(Point2(6.15f, h * 0.82f), 0.28f)
                hint = "Short tether bursts dodge hazards better than holding forever."
                accent = 0xFF45F2FF.toInt()
            }

            5 -> {
                title = "Rift Combo"
                pulses += pulse(4.7f, h * 0.52f, 1.65f, -1.7f, 1.7f)
                blocks += block(3.8f, h * 0.61f, 1.55f, 0.24f, -0.24f, TONE_RIFT)
                blocks += block(6.7f, h * 0.72f, 2.1f, 0.24f, -0.14f, TONE_MOSS)
                hint = "Combine hold steering, pulse guard and a clean coast."
                inkLimit = 9.55f
                accent = 0xFFFF8C42.toInt()
            }

            6 -> {
                title = "Rift Drain"
                curses += curse(CurseType.RIFT_DRAIN)
                blocks += block(5.15f, h * 0.68f, 3.0f, 0.24f, -0.08f, TONE_RIFT)
                hint = "SPECIAL: tether energy drains faster. Release to recharge."
                inkLimit = 9.2f
                accent = 0xFF64E572.toInt()
            }

            7 -> {
                title = "Pulse Guard"
                curses += curse(CurseType.PULSE_STORM)
                pulses += pulse(4.8f, h * 0.52f, 2.05f, -2.1f, 2.5f)
                blocks += block(6.35f, h * 0.72f, 2.1f, 0.24f, -0.2f, TONE_VIOLET)
                hint = "SPECIAL: pulse storms weaken while HOLD is active."
                accent = 0xFFC15CFF.toInt()
            }

            8 -> {
                title = "Focus Heavy"
                curses += curse(CurseType.FOCUS_FIELD)
                curses += curse(CurseType.HEAVY_CORE)
                blocks += block(4.35f, h * 0.59f, 1.7f, 0.24f, -0.2f, TONE_MOSS)
                blocks += block(6.55f, h * 0.72f, 1.9f, 0.24f, -0.18f, TONE_RIFT)
                hint = "SPECIAL: HOLD slows the ball so you can fight heavy gravity."
                gravity = 7.65f
                inkLimit = 9.25f
                accent = 0xFFFFCF4A.toInt()
            }

            9 -> {
                title = "Power Moon"
                curses += curse(CurseType.POWER_HOLD)
                curses += curse(CurseType.MOON_GLIDE)
                pulses += pulse(5.75f, h * 0.55f, 1.4f, -1.4f, 1.5f)
                blocks += block(4.15f, h * 0.66f, 1.45f, 0.24f, -0.26f, TONE_RIFT)
                hint = "SPECIAL: hold longer for more pull while the ball glides."
                gravity = 6.9f
                accent = 0xFF45F2FF.toInt()
            }

            10 -> {
                title = "Wind Overheat"
                start = Point2(1.55f, 2.3f)
                goal = Point2(8.15f, h - 2.12f)
                curses += curse(CurseType.RIFT_WIND)
                curses += curse(CurseType.OVERHEAT)
                curses += curse(CurseType.TINY_GATE)
                blocks += block(5.65f, h * 0.67f, 2.5f, 0.24f, -0.12f, TONE_RIFT)
                hint = "SPECIAL: HOLD guards wind but overheats. Use short bursts."
                drawSeconds = 6.15f
                inkLimit = 9.0f
                goalRadius = 1.0f
                accent = 0xFF8AA6FF.toInt()
            }
        }

        return tutorialSpec(levelIndex, seed, title, h, start, goal, goalRadius, inkLimit, drawSeconds, gravity, blocks, hazards, pulses, accent, curses, hint)
    }

    private fun tutorialSpec(
        levelIndex: Int,
        seed: Long,
        title: String,
        h: Float,
        start: Point2,
        goal: Point2,
        goalRadius: Float,
        inkLimit: Float,
        drawSeconds: Float,
        gravity: Float,
        blocks: List<Block>,
        hazards: List<Hazard>,
        pulses: List<PulseZone>,
        accent: Int,
        curses: List<CurseSpec>,
        hint: String
    ): LevelSpec {
        val tunedGravity = tunedGravity(gravity, curses)
        val tunedInk = tunedInkLimit(inkLimit, curses)
        val tunedSeconds = tunedDrawSeconds(drawSeconds, curses)
        val tunedGoalRadius = tunedGoalRadius(goalRadius, curses)
        return LevelSpec(
            index = levelIndex,
            seed = seed,
            title = title,
            stageHeight = h,
            start = Point2(start.x, max(start.y, 3.35f)),
            goal = goal,
            goalRadius = tunedGoalRadius,
            inkLimit = tunedInk,
            drawSeconds = tunedSeconds,
            gravity = tunedGravity,
            blocks = blocks,
            hazards = hazards,
            pulseZones = pulses,
            accent = accent,
            curses = curses.distinctBy { it.type },
            mascotName = mascotFor(seed, levelIndex),
            tutorialHint = hint
        ).withPlayableLaneBounds()
    }

    private fun chainCurses(seed: Long, levelIndex: Int, stackSize: Int): List<CurseSpec> {
        val order = arrayOf(
            CurseType.RIFT_WIND,
            CurseType.RIFT_DRAIN,
            CurseType.PULSE_STORM,
            CurseType.FOCUS_FIELD,
            CurseType.POWER_HOLD,
            CurseType.HEAVY_CORE,
            CurseType.MOON_GLIDE,
            CurseType.TINY_GATE,
            CurseType.OVERHEAT
        )
        val offset = (((seed xor (levelIndex.toLong() * 31L)) ushr 1) % order.size).toInt()
        val selected = mutableListOf<CurseType>()
        var cursor = 0
        while (selected.size < stackSize && cursor < order.size * 2) {
            val type = order[(offset + cursor * 3) % order.size]
            val conflicts = (type == CurseType.FOCUS_FIELD && CurseType.POWER_HOLD in selected) ||
                (type == CurseType.POWER_HOLD && CurseType.FOCUS_FIELD in selected) ||
                (type == CurseType.HEAVY_CORE && CurseType.MOON_GLIDE in selected) ||
                (type == CurseType.MOON_GLIDE && CurseType.HEAVY_CORE in selected)
            if (!conflicts && type !in selected) selected += type
            cursor += 1
        }
        if (selected.isEmpty()) selected += CurseType.PULSE_STORM
        return selected.map(::curse)
    }

    private fun tunedGravity(base: Float, curses: List<CurseSpec>): Float {
        var gravity = base
        if (hasCurse(curses, CurseType.HEAVY_CORE)) gravity *= 1.38f
        if (hasCurse(curses, CurseType.MOON_GLIDE)) gravity *= 0.58f
        return gravity.coerceIn(4.2f, 15.6f)
    }

    private fun tunedInkLimit(base: Float, curses: List<CurseSpec>): Float {
        var ink = base
        if (hasCurse(curses, CurseType.RIFT_DRAIN)) ink *= 0.88f
        if (hasCurse(curses, CurseType.RIFT_WIND)) ink += 0.28f
        return ink.coerceAtLeast(4.35f)
    }

    private fun tunedDrawSeconds(base: Float, curses: List<CurseSpec>): Float {
        var seconds = base
        if (hasCurse(curses, CurseType.OVERHEAT)) seconds -= 0.55f
        if (hasCurse(curses, CurseType.RIFT_WIND)) seconds += 0.25f
        return seconds.coerceAtLeast(2.1f)
    }

    private fun tunedGoalRadius(base: Float, curses: List<CurseSpec>): Float {
        return if (hasCurse(curses, CurseType.TINY_GATE)) base * 0.78f else base
    }

    private fun hasCurse(curses: List<CurseSpec>, type: CurseType): Boolean {
        return curses.any { it.type == type }
    }

    private fun curse(type: CurseType): CurseSpec = when (type) {
        CurseType.RIFT_WIND -> CurseSpec(type, "Wind Guard", "HOLD BLOCKS GUSTS", 0xFF8AA6FF.toInt())
        CurseType.RIFT_DRAIN -> CurseSpec(type, "Rift Drain", "SHORTER HOLDS", 0xFF64E572.toInt())
        CurseType.HEAVY_CORE -> CurseSpec(type, "Heavy Core", "GRAVITY RAMPS", 0xFFFF8C42.toInt())
        CurseType.MOON_GLIDE -> CurseSpec(type, "Moon Glide", "LONG COAST", 0xFF45F2FF.toInt())
        CurseType.FOCUS_FIELD -> CurseSpec(type, "Focus Field", "HOLD SLOWS", 0xFFFFCF4A.toInt())
        CurseType.POWER_HOLD -> CurseSpec(type, "Power Hold", "HOLD BUILDS FORCE", 0xFFFF4D8D.toInt())
        CurseType.PULSE_STORM -> CurseSpec(type, "Pulse Guard", "HOLD BLOCKS PULSE", 0xFFC15CFF.toInt())
        CurseType.TINY_GATE -> CurseSpec(type, "Tiny Gate", "SMALL TARGET", 0xFFF7F4FF.toInt())
        CurseType.OVERHEAT -> CurseSpec(type, "Overheat", "POWER VS HEAT", 0xFFFF5757.toInt())
    }

    private fun mascotFor(seed: Long, levelIndex: Int): String {
        val index = (((seed xor (levelIndex.toLong() * 0x45D9F3BL)) ushr 1) % mascots.size).toInt()
        return mascots[index]
    }

    private fun addClassicEscalation(
        cycle: Int,
        h: Float,
        random: Random,
        blocks: MutableList<Block>,
        hazards: MutableList<Hazard>,
        pulses: MutableList<PulseZone>
    ) {
        repeat(cycle.coerceAtMost(4)) { i ->
            blocks += block(
                x = 2.4f + random.nextFloat() * 5.4f,
                y = h * (0.36f + random.nextFloat() * 0.46f),
                width = 0.9f + random.nextFloat() * 1.0f,
                height = 0.21f,
                angle = -0.55f + random.nextFloat() * 1.1f,
                tone = if (i % 2 == 0) TONE_STEEL else TONE_MOSS
            )
            if (i % 2 == 0) {
                hazards += Hazard(Point2(2.5f + random.nextFloat() * 5.5f, h * (0.45f + random.nextFloat() * 0.35f)), 0.28f + random.nextFloat() * 0.12f)
            } else {
                pulses += pulse(2.5f + random.nextFloat() * 5.5f, h * (0.38f + random.nextFloat() * 0.4f), 0.95f + random.nextFloat() * 0.35f, -2.4f + random.nextFloat() * 4.8f, -2f + random.nextFloat() * 4f)
            }
        }
    }

    private fun addSeedVariation(
        index: Int,
        h: Float,
        random: Random,
        blocks: MutableList<Block>,
        hazards: MutableList<Hazard>,
        pulses: MutableList<PulseZone>
    ) {
        when (index.mod(5)) {
            0 -> pulses += pulse(
                x = 2.4f + random.nextFloat() * 5.8f,
                y = h * (0.38f + random.nextFloat() * 0.38f),
                radius = 0.85f + random.nextFloat() * 0.38f,
                radial = -1.8f + random.nextFloat() * 3.6f,
                swirl = -2.2f + random.nextFloat() * 4.4f
            )

            1 -> blocks += block(
                x = 2.2f + random.nextFloat() * 5.9f,
                y = h * (0.42f + random.nextFloat() * 0.34f),
                width = 0.85f + random.nextFloat() * 0.75f,
                height = 0.2f,
                angle = -0.72f + random.nextFloat() * 1.44f,
                tone = if (random.nextBoolean()) TONE_MOSS else TONE_VIOLET
            )

            2 -> hazards += Hazard(
                center = Point2(2.7f + random.nextFloat() * 4.9f, h * (0.47f + random.nextFloat() * 0.3f)),
                radius = 0.24f + random.nextFloat() * 0.1f
            )

            3 -> {
                blocks += block(2.1f + random.nextFloat() * 2.1f, h * (0.69f + random.nextFloat() * 0.12f), 1.2f, 0.2f, -0.48f + random.nextFloat() * 0.35f, TONE_STEEL)
                blocks += block(6.2f + random.nextFloat() * 1.6f, h * (0.43f + random.nextFloat() * 0.16f), 1.2f, 0.2f, 0.28f + random.nextFloat() * 0.42f, TONE_RIFT)
            }

            4 -> pulses += pulse(
                x = 5.0f + random.nextFloat() * 2.5f,
                y = h * (0.52f + random.nextFloat() * 0.22f),
                radius = 1.0f + random.nextFloat() * 0.3f,
                radial = 1.2f + random.nextFloat() * 2.4f,
                swirl = -1.8f + random.nextFloat() * 3.6f,
                phase = PI.toFloat() * random.nextFloat()
            )
        }
    }

    private fun block(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        angle: Float,
        tone: Int = TONE_STEEL
    ): Block = Block(Point2(x, y), width, height, angle, tone)

    private fun pulse(
        x: Float,
        y: Float,
        radius: Float,
        radial: Float,
        swirl: Float,
        phase: Float = 0f
    ): PulseZone = PulseZone(Point2(x, y), radius, radial, swirl, phase)

    private fun mix(daySeed: Long, level: Long): Long {
        var x = daySeed xor (level * 0x9E3779B97F4A7C15uL.toLong())
        x = (x xor (x ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
        x = (x xor (x ushr 27)) * 0x94D049BB133111EBuL.toLong()
        return x xor (x ushr 31)
    }

    private val classicAccents = intArrayOf(
        0xFF1DE8C8.toInt(),
        0xFFFFCF4A.toInt(),
        0xFF64E572.toInt(),
        0xFF8AA6FF.toInt(),
        0xFFFF8C42.toInt()
    )

    private val chaosAccents = intArrayOf(
        0xFFFF4D8D.toInt(),
        0xFFFF8C42.toInt(),
        0xFFC15CFF.toInt(),
        0xFFFFCF4A.toInt(),
        0xFF45F2FF.toInt()
    )

    private val classicTitles = arrayOf(
        "First Noodle",
        "Drop Script",
        "Bounce Signal",
        "Low Ceiling",
        "Twin Pulse",
        "Orbit Lesson",
        "Step Maze",
        "Crosswind",
        "Lift Fork",
        "Drop Catch",
        "Saw Bridge",
        "Funnel Skip"
    )

    private val chaosTitles = arrayOf(
        "Voro Slop",
        "Orbit Rot",
        "Kav Crusher",
        "Reverse Drip",
        "Panic Voro",
        "Glitch Soup",
        "Fan Belt",
        "Backbite",
        "Saw Soup",
        "Orbit Trap"
    )

    private val mascots = arrayOf(
        "KAVVI",
        "BLOP VORO",
        "MIMI VORO",
        "ZAZA KAV",
        "GLOBO KAV",
        "TIKKAV RIFT",
        "LALA VORO",
        "BYTE VORO",
        "FIZZ KAV",
        "WOMP KAV"
    )
}
