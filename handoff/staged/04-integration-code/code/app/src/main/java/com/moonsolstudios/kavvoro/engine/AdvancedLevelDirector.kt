package com.moonsolstudios.kavvoro.engine

import kotlin.math.PI
import kotlin.math.max
import kotlin.random.Random

/** Post-tutorial generator built around readable route families instead of random clutter. */
object AdvancedLevelDirector {
    private const val TONE_STEEL = 0xFF293040.toInt()
    private const val TONE_VIOLET = 0xFF3B3046.toInt()
    private const val TONE_MOSS = 0xFF243B3A.toInt()
    private const val TONE_RIFT = 0xFF431934.toInt()

    fun createClassic(levelIndex: Int, stageHeight: Float, seed: Long): LevelSpec {
        return create(levelIndex, stageHeight, seed, chaos = false)
    }

    fun createChaos(levelIndex: Int, stageHeight: Float, seed: Long): LevelSpec {
        return create(levelIndex, stageHeight, seed, chaos = true)
    }

    private fun create(levelIndex: Int, stageHeight: Float, seed: Long, chaos: Boolean): LevelSpec {
        val depth = (levelIndex - 15).coerceAtLeast(0)
        val cycle = depth / PATTERN_COUNT
        val pattern = (depth + cycle * 5).mod(PATTERN_COUNT)
        val random = Random((seed xor if (chaos) CHAOS_SALT else CLASSIC_SALT).toInt())
        val builder = LevelBuilder(max(stageHeight, 15.5f), random, cycle, chaos)

        if (chaos) buildChaosPattern(pattern, builder) else buildClassicPattern(pattern, builder)
        addMasteryLayer(builder, cycle, pattern)

        val curseCount = if (chaos) {
            when {
                depth >= 32 -> 3
                depth >= 10 -> 2
                else -> 1
            }
        } else {
            when {
                depth >= 38 -> 3
                depth >= 16 -> 2
                depth >= 4 -> 1
                else -> 0
            }
        }
        val curses = selectCurses(seed, pattern, curseCount, chaos)
        if (curses.any { it.type == CurseType.PULSE_STORM }) {
            builder.pulse(5f, 0.56f, 1.35f, -3.1f, if (chaos) 3.4f else 2.5f, 0.4f)
        }
        if (depth >= 8 && pattern % 5 == 2) {
            builder.portal(
                entryX = if (chaos) 7.35f else 2.65f,
                entryY = if (chaos) 0.58f else 0.54f,
                exitX = if (chaos) 2.25f else 7.35f,
                exitY = if (chaos) 0.38f else 0.42f,
                radius = if (chaos) 0.5f else 0.46f
            )
        }

        if (random.nextBoolean()) builder.mirror()

        var gravity = builder.gravity
        if (curses.any { it.type == CurseType.HEAVY_CORE }) gravity *= 1.3f
        if (curses.any { it.type == CurseType.MOON_GLIDE }) gravity *= 0.64f
        gravity = gravity.coerceIn(4.6f, 15.2f)

        var goalRadius = builder.goalRadius
        if (curses.any { it.type == CurseType.TINY_GATE }) goalRadius *= 0.8f
        val windEase = if (curses.any { it.type == CurseType.RIFT_WIND }) {
            when {
                depth < 15 -> 1f
                depth < 30 -> 0.62f
                depth < 50 -> 0.32f
                else -> 0f
            }
        } else {
            0f
        }
        if (windEase > 0f && curses.none { it.type == CurseType.TINY_GATE }) {
            goalRadius += 0.06f * windEase
        }

        val timeLimitBase = when {
            depth < 5 -> if (chaos) 10.4f else 10.8f
            depth < 15 -> if (chaos) 9.7f else 10.2f
            depth < 25 -> if (chaos) 9.15f else 9.65f
            depth < 40 -> if (chaos) 8.7f else 9.2f
            else -> (if (chaos) 8.35f else 8.8f) - ((depth - 40) / 24) * 0.15f
        }
        val timeLimit = (timeLimitBase + windEase * if (chaos) 0.55f else 0.65f)
            .coerceAtLeast(if (chaos) 7.8f else 8.15f)

        val drainMultiplier = (
            1f + depth.coerceAtMost(55) * if (chaos) 0.0062f else 0.0052f
            ).coerceAtMost(if (chaos) 1.36f else 1.3f)

        return LevelSpec(
            index = levelIndex,
            seed = seed,
            title = if (chaos) CHAOS_TITLES[pattern] else CLASSIC_TITLES[pattern],
            stageHeight = builder.height,
            start = builder.start,
            goal = builder.goal,
            goalRadius = goalRadius.coerceAtLeast(0.38f),
            inkLimit = 6.4f,
            drawSeconds = 3.1f,
            gravity = gravity,
            blocks = builder.blocks,
            hazards = builder.hazards,
            pulseZones = builder.pulses,
            portals = builder.portals,
            accent = if (chaos) CHAOS_ACCENTS[pattern.mod(CHAOS_ACCENTS.size)] else CLASSIC_ACCENTS[pattern.mod(CLASSIC_ACCENTS.size)],
            timeLimitSeconds = timeLimit,
            riftDrainMultiplier = drainMultiplier,
            difficultyRating = (3 + depth / 6 + if (chaos) 1 else 0).coerceIn(3, 10),
            curses = curses,
            mascotName = mascotFor(seed, levelIndex)
        ).withPlayableLaneBounds()
    }

    private fun buildClassicPattern(pattern: Int, b: LevelBuilder) {
        when (pattern) {
            0 -> { // Three deliberate direction changes.
                b.gate(0.4f, 7.2f, 2.45f)
                b.gate(0.58f, 2.75f, 2.25f, TONE_MOSS)
                b.gate(0.77f, 6.8f, 2.05f, TONE_VIOLET)
                b.hazard(8.15f, 0.5f, 0.34f)
                b.hazard(1.85f, 0.68f, 0.34f)
            }

            1 -> {
                b.gate(0.45f, 3f, 2.35f)
                b.gate(0.73f, 7f, 2.1f, TONE_MOSS)
                b.hazard(5f, 0.58f, 0.36f, HazardMotion.ORBIT, 1.45f, b.motionSpeed(1.05f), 0f)
                b.hazard(5f, 0.58f, 0.31f, HazardMotion.ORBIT, 1.45f, b.motionSpeed(1.05f), PI.toFloat())
            }

            2 -> {
                b.shoulders(0.43f, 2.25f)
                b.shoulders(0.61f, 2.05f, TONE_VIOLET)
                b.shoulders(0.79f, 1.9f, TONE_MOSS)
                b.hazard(5f, 0.43f, 0.32f, HazardMotion.HORIZONTAL, 2.35f, b.motionSpeed(1.1f), 0f)
                b.hazard(5f, 0.61f, 0.32f, HazardMotion.HORIZONTAL, 2.35f, b.motionSpeed(1.18f), PI.toFloat())
                b.hazard(5f, 0.79f, 0.3f, HazardMotion.HORIZONTAL, 2.15f, b.motionSpeed(1.25f), 1.4f)
            }

            3 -> {
                b.gate(0.39f, 7.25f, 2.1f)
                b.gate(0.54f, 4.8f, 1.9f, TONE_MOSS)
                b.gate(0.69f, 2.55f, 1.8f, TONE_VIOLET)
                b.gate(0.83f, 6.7f, 1.72f)
                b.hazard(5.1f, 0.61f, 0.34f)
            }

            4 -> {
                b.ramp(2.7f, 0.43f, 2.1f, 0.48f, TONE_MOSS)
                b.ramp(7.2f, 0.58f, 2.1f, -0.48f, TONE_VIOLET)
                b.ramp(3.2f, 0.76f, 2.2f, 0.4f)
                b.pulse(3.25f, 0.49f, 1.35f, -2.3f, 1.7f)
                b.pulse(6.65f, 0.67f, 1.35f, 2.1f, -1.8f, 1.8f)
                b.hazard(5.05f, 0.72f, 0.38f)
            }

            5 -> {
                b.segment(0.42f, 3.1f, 6.9f, TONE_STEEL)
                b.segment(0.62f, 0f, 2.6f, TONE_MOSS)
                b.segment(0.62f, 4.1f, 7.1f, TONE_MOSS)
                b.segment(0.62f, 8.6f, 10f, TONE_MOSS)
                b.segment(0.79f, 2.7f, 7.3f, TONE_VIOLET)
                b.hazard(2.75f, 0.51f, 0.36f)
                b.hazard(7.25f, 0.7f, 0.36f)
                b.goal = Point2(2.05f, b.height - 2.25f)
            }

            6 -> {
                b.gate(0.48f, 5f, 2.3f, TONE_MOSS)
                b.gate(0.76f, 5f, 2.05f, TONE_VIOLET)
                b.pulse(5f, 0.61f, 1.75f, -2.8f, 2.6f)
                repeat(3) { i ->
                    b.hazard(5f, 0.61f, 0.29f, HazardMotion.ORBIT, 1.65f, b.motionSpeed(0.92f), i * PI.toFloat() * 2f / 3f)
                }
            }

            7 -> {
                b.gate(0.44f, 7.1f, 2.25f)
                b.gate(0.69f, 2.9f, 2.05f, TONE_MOSS)
                b.pulse(3f, 0.54f, 1.4f, -2.8f, -2.1f)
                b.pulse(7f, 0.58f, 1.4f, 2.8f, 2.1f, PI.toFloat())
                b.hazard(5f, 0.59f, 0.32f, HazardMotion.VERTICAL, b.height * 0.11f, b.motionSpeed(1.0f), 0.4f)
            }

            8 -> {
                repeat(5) { i ->
                    val x = if (i % 2 == 0) 3.05f else 6.95f
                    b.ramp(x, 0.38f + i * 0.105f, 2.15f, if (i % 2 == 0) 0.62f else -0.62f, if (i % 3 == 0) TONE_MOSS else TONE_STEEL)
                }
                b.hazard(5f, 0.57f, 0.38f)
                b.hazard(5f, 0.79f, 0.34f)
            }

            9 -> {
                b.shoulders(0.46f, 1.9f)
                b.shoulders(0.64f, 1.8f, TONE_MOSS)
                b.shoulders(0.81f, 1.7f, TONE_VIOLET)
                repeat(3) { i ->
                    val y = 0.46f + i * 0.175f
                    b.hazard(3.2f, y, 0.31f, HazardMotion.HORIZONTAL, 1.45f, b.motionSpeed(1.08f + i * 0.08f), 0f)
                    b.hazard(6.8f, y, 0.31f, HazardMotion.HORIZONTAL, 1.45f, b.motionSpeed(1.08f + i * 0.08f), PI.toFloat())
                }
            }

            10 -> {
                b.goal = Point2(2.05f, b.height - 2.4f)
                b.gate(0.4f, 2.7f, 1.9f)
                b.gate(0.55f, 7.1f, 1.82f, TONE_MOSS)
                b.gate(0.7f, 3.2f, 1.75f, TONE_VIOLET)
                b.gate(0.84f, 6.6f, 1.68f)
                b.hazard(5f, 0.48f, 0.37f)
                b.hazard(5.1f, 0.77f, 0.37f)
            }

            11 -> {
                repeat(4) { i ->
                    val y = 0.4f + i * 0.14f
                    b.ramp(if (i % 2 == 0) 3.15f else 6.85f, y, 2.15f, if (i % 2 == 0) 0.52f else -0.52f, if (i % 2 == 0) TONE_STEEL else TONE_MOSS)
                }
                b.hazard(2.4f, 0.77f, 0.32f, HazardMotion.VERTICAL, b.height * 0.1f, b.motionSpeed(1.2f), 0f)
                b.hazard(7.6f, 0.77f, 0.32f, HazardMotion.VERTICAL, b.height * 0.1f, b.motionSpeed(1.2f), PI.toFloat())
            }

            12 -> {
                b.gate(0.42f, 7f, 2.2f)
                b.gate(0.76f, 3f, 1.9f, TONE_VIOLET)
                b.hazard(5f, 0.59f, 0.34f, HazardMotion.FIGURE_EIGHT, 2.2f, b.motionSpeed(0.95f), 0f)
                b.hazard(5f, 0.59f, 0.29f, HazardMotion.FIGURE_EIGHT, 2.2f, b.motionSpeed(0.95f), PI.toFloat())
                b.pulse(5f, 0.59f, 1.25f, -1.8f, 2.2f)
            }

            13 -> {
                repeat(4) { i ->
                    val gap = if (i % 2 == 0) 3f else 7f
                    b.gate(0.38f + i * 0.15f, gap, 1.85f - i * 0.04f, if (i % 2 == 0) TONE_MOSS else TONE_VIOLET)
                    b.hazard(gap, 0.445f + i * 0.15f, 0.29f, HazardMotion.HORIZONTAL, 0.8f, b.motionSpeed(1.22f), i * 0.9f)
                }
            }

            14 -> {
                b.gate(0.41f, 2.8f, 2f)
                b.gate(0.58f, 7.2f, 1.9f, TONE_MOSS)
                b.gate(0.76f, 3.15f, 1.8f, TONE_VIOLET)
                b.pulse(2.8f, 0.5f, 1.2f, 2.8f, -2.3f)
                b.pulse(7.2f, 0.67f, 1.2f, -2.8f, 2.3f, PI.toFloat())
                b.hazard(5f, 0.67f, 0.31f, HazardMotion.HORIZONTAL, 1.55f, b.motionSpeed(1.18f), 0.6f)
            }

            15 -> {
                b.gate(0.38f, 7.2f, 1.95f)
                b.gate(0.53f, 3f, 1.82f, TONE_MOSS)
                b.gate(0.69f, 6.8f, 1.72f, TONE_VIOLET)
                b.gate(0.84f, 2.8f, 1.65f)
                b.hazard(5f, 0.61f, 0.32f, HazardMotion.FIGURE_EIGHT, 1.9f, b.motionSpeed(1.04f), 0f)
                b.pulse(5f, 0.72f, 1.2f, -2.5f, 2.7f)
            }
        }
    }

    private fun buildChaosPattern(pattern: Int, b: LevelBuilder) {
        when (pattern) {
            0 -> {
                b.start = Point2(5f, 3.35f)
                b.goal = Point2(5f, b.height - 2.2f)
                b.pulse(5f, 0.58f, 2.05f, -3.8f, 4.5f)
                repeat(4) { i ->
                    b.hazard(5f, 0.58f, 0.3f, HazardMotion.ORBIT, 1.45f + (i % 2) * 0.75f, b.motionSpeed(0.95f + (i % 2) * 0.22f), i * PI.toFloat() * 0.5f)
                }
                b.gate(0.82f, 5f, 1.8f, TONE_RIFT)
            }

            1 -> {
                repeat(5) { i ->
                    val y = 0.36f + i * 0.115f
                    b.hazard(5f, y, 0.3f, HazardMotion.HORIZONTAL, 3.25f, b.motionSpeed(1.0f + i * 0.11f), i * 1.25f)
                }
                b.gate(0.79f, 7.4f, 1.8f, TONE_RIFT)
                b.goal = Point2(8.2f, b.height - 2.5f)
            }

            2 -> {
                b.goal = Point2(1.8f, b.height - 2.4f)
                b.hazard(4.5f, 0.48f, 0.34f, HazardMotion.FIGURE_EIGHT, 2.7f, b.motionSpeed(1.12f), 0f)
                b.hazard(5.5f, 0.69f, 0.34f, HazardMotion.FIGURE_EIGHT, 2.7f, b.motionSpeed(-1.03f), PI.toFloat())
                b.gate(0.57f, 2.5f, 1.85f, TONE_RIFT)
                b.gate(0.82f, 7.3f, 1.7f, TONE_VIOLET)
            }

            3 -> {
                repeat(4) { i ->
                    val y = 0.39f + i * 0.145f
                    val left = i % 2 == 0
                    b.ramp(if (left) 2.7f else 7.3f, y, 3.1f, if (left) 0.18f else -0.18f, TONE_RIFT)
                    b.hazard(if (left) 6.5f else 3.5f, y + 0.04f, 0.33f, HazardMotion.HORIZONTAL, 1.45f, b.motionSpeed(1.25f), i * 0.8f)
                }
            }

            4 -> {
                repeat(4) { i ->
                    val x = if (i % 2 == 0) 3f else 7f
                    b.pulse(x, 0.4f + i * 0.13f, 1.42f, if (i % 2 == 0) -4.1f else 4.1f, if (i % 2 == 0) 3.7f else -3.7f, i.toFloat())
                }
                b.hazard(5f, 0.58f, 0.42f)
                b.hazard(5f, 0.78f, 0.32f, HazardMotion.HORIZONTAL, 2.35f, b.motionSpeed(1.28f), 0f)
            }

            5 -> {
                b.start = Point2(8.65f, 3.35f)
                b.goal = Point2(1.35f, b.height - 2.25f)
                repeat(5) { i ->
                    val x = 2.05f + i * 1.48f
                    b.ramp(x, 0.42f + (i % 2) * 0.16f, 1.25f, if (i % 2 == 0) 0.82f else -0.82f, TONE_RIFT)
                    b.hazard(x, 0.72f - (i % 2) * 0.12f, 0.3f, HazardMotion.VERTICAL, b.height * 0.075f, b.motionSpeed(1.2f), i * 0.7f)
                }
            }

            6 -> {
                b.start = Point2(5f, 3.35f)
                b.goal = Point2(5f, b.height - 2.25f)
                b.gate(0.39f, 5f, 2.2f, TONE_RIFT)
                b.gate(0.8f, 5f, 1.75f, TONE_RIFT)
                repeat(4) { i ->
                    b.hazard(5f, 0.59f, 0.31f, HazardMotion.ORBIT, if (i < 2) 1.35f else 2.25f, b.motionSpeed(if (i < 2) 1.25f else -0.82f), i * PI.toFloat() * 0.5f)
                }
            }

            7 -> {
                repeat(6) { i ->
                    b.ramp(if (i % 2 == 0) 2.7f else 7.3f, 0.35f + i * 0.095f, 1.85f, if (i % 2 == 0) 0.72f else -0.72f, if (i % 3 == 0) TONE_RIFT else TONE_VIOLET)
                }
                b.pulse(5f, 0.61f, 1.7f, 3.8f, 4.2f)
                b.hazard(5f, 0.74f, 0.4f)
            }

            8 -> {
                b.gate(0.39f, 2.5f, 1.85f, TONE_RIFT)
                b.gate(0.58f, 7.5f, 1.75f, TONE_VIOLET)
                b.gate(0.78f, 2.5f, 1.65f, TONE_RIFT)
                repeat(3) { i ->
                    b.hazard(if (i % 2 == 0) 6.5f else 3.5f, 0.43f + i * 0.19f, 0.34f, HazardMotion.ORBIT, 1.05f, b.motionSpeed(1.28f + i * 0.08f), i.toFloat())
                }
            }

            9 -> {
                repeat(4) { i ->
                    val gap = if (i % 2 == 0) 7.25f else 2.75f
                    b.gate(0.38f + i * 0.15f, gap, 1.72f, TONE_RIFT)
                    b.pulse(gap, 0.45f + i * 0.15f, 1.1f, if (i % 2 == 0) -3.5f else 3.5f, if (i % 2 == 0) 3.2f else -3.2f, i.toFloat())
                }
            }

            10 -> {
                repeat(6) { i ->
                    val angle = i * PI.toFloat() * 2f / 6f
                    b.hazard(5f, 0.59f, 0.28f, HazardMotion.ORBIT, if (i % 2 == 0) 1.45f else 2.45f, b.motionSpeed(if (i % 2 == 0) 1.35f else -0.78f), angle)
                }
                b.pulse(5f, 0.59f, 1.25f, -4.4f, 0.8f)
                b.goal = Point2(2f, b.height - 2.2f)
            }

            11 -> {
                b.segment(0.44f, 2f, 8f, TONE_RIFT)
                b.segment(0.64f, 0f, 3.5f, TONE_VIOLET)
                b.segment(0.64f, 6.5f, 10f, TONE_VIOLET)
                b.segment(0.82f, 2f, 8f, TONE_RIFT)
                b.hazard(2.35f, 0.54f, 0.36f, HazardMotion.VERTICAL, b.height * 0.12f, b.motionSpeed(1.15f), 0f)
                b.hazard(7.65f, 0.72f, 0.36f, HazardMotion.VERTICAL, b.height * 0.12f, b.motionSpeed(1.15f), PI.toFloat())
            }

            12 -> {
                repeat(4) { i ->
                    val y = 0.4f + i * 0.145f
                    b.shoulders(y, 1.72f, TONE_RIFT)
                    b.hazard(5f, y, 0.31f, HazardMotion.HORIZONTAL, 2.55f, b.motionSpeed(1.35f + i * 0.08f), i * 1.4f)
                }
                b.goalRadius = 0.48f
            }

            13 -> {
                b.hazard(4.4f, 0.48f, 0.32f, HazardMotion.FIGURE_EIGHT, 2.4f, b.motionSpeed(1.2f), 0f)
                b.hazard(5.6f, 0.71f, 0.32f, HazardMotion.FIGURE_EIGHT, 2.4f, b.motionSpeed(-1.2f), 0f)
                b.pulse(3f, 0.61f, 1.35f, -4f, 3.5f)
                b.pulse(7f, 0.61f, 1.35f, 4f, -3.5f, PI.toFloat())
                b.gate(0.84f, 5f, 1.65f, TONE_RIFT)
            }

            14 -> {
                b.gravity = 11.5f
                b.goal = Point2(1.7f, b.height - 2.3f)
                b.gate(0.37f, 7.4f, 1.7f, TONE_RIFT)
                b.gate(0.5f, 2.6f, 1.62f, TONE_VIOLET)
                b.gate(0.63f, 7.25f, 1.58f, TONE_RIFT)
                b.gate(0.76f, 2.75f, 1.54f, TONE_VIOLET)
                b.gate(0.87f, 6.9f, 1.5f, TONE_RIFT)
            }

            15 -> {
                b.gate(0.38f, 7.25f, 1.75f, TONE_RIFT)
                b.gate(0.55f, 2.75f, 1.68f, TONE_VIOLET)
                b.gate(0.74f, 7.1f, 1.58f, TONE_RIFT)
                b.hazard(5f, 0.62f, 0.33f, HazardMotion.FIGURE_EIGHT, 2.6f, b.motionSpeed(1.16f), 0f)
                b.hazard(5f, 0.62f, 0.29f, HazardMotion.FIGURE_EIGHT, 2.6f, b.motionSpeed(1.16f), PI.toFloat())
                b.pulse(5f, 0.62f, 1.5f, -4.2f, 4.5f)
            }
        }
    }

    private fun addMasteryLayer(b: LevelBuilder, cycle: Int, pattern: Int) {
        repeat(cycle.coerceAtMost(3)) { i ->
            val y = 0.34f + ((pattern * 3 + i * 5).mod(11)) * 0.045f
            if ((pattern + i) % 2 == 0) {
                b.hazard(
                    if (i % 2 == 0) 2.2f else 7.8f,
                    y,
                    0.26f + i * 0.015f,
                    HazardMotion.HORIZONTAL,
                    0.65f + i * 0.12f,
                    b.motionSpeed(1.08f + i * 0.08f),
                    i * 1.2f
                )
            } else {
                b.pulse(if (i % 2 == 0) 2.4f else 7.6f, y, 0.95f, -2.4f + i, 2.1f - i * 0.5f, i.toFloat())
            }
        }
    }

    private fun selectCurses(seed: Long, pattern: Int, count: Int, chaos: Boolean): List<CurseSpec> {
        if (count <= 0) return emptyList()
        val order = if (chaos) CHAOS_CURSE_ORDER else CLASSIC_CURSE_ORDER
        val start = (((seed ushr 3) + pattern * 5L) % order.size).toInt()
        val chosen = mutableListOf<CurseType>()
        var cursor = 0
        while (chosen.size < count && cursor < order.size * 2) {
            val candidate = order[(start + cursor * 3) % order.size]
            val conflicts = (candidate == CurseType.FOCUS_FIELD && CurseType.POWER_HOLD in chosen) ||
                (candidate == CurseType.POWER_HOLD && CurseType.FOCUS_FIELD in chosen) ||
                (candidate == CurseType.HEAVY_CORE && CurseType.MOON_GLIDE in chosen) ||
                (candidate == CurseType.MOON_GLIDE && CurseType.HEAVY_CORE in chosen)
            if (!conflicts && candidate !in chosen) chosen += candidate
            cursor += 1
        }
        return chosen.map(::curse)
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
        val index = (((seed xor (levelIndex.toLong() * 0x45D9F3BL)) ushr 1) % MASCOTS.size).toInt()
        return MASCOTS[index]
    }

    private class LevelBuilder(
        val height: Float,
        private val random: Random,
        private val cycle: Int,
        private val chaos: Boolean
    ) {
        var start = Point2(1.35f, 3.35f)
        var goal = Point2(8.45f, height - 2.3f)
        var goalRadius = if (chaos) 0.52f else 0.57f
        var gravity = if (chaos) 9.5f else 9.15f
        val blocks = mutableListOf<Block>()
        val hazards = mutableListOf<Hazard>()
        val pulses = mutableListOf<PulseZone>()
        val portals = mutableListOf<PortalPair>()

        fun motionSpeed(base: Float): Float {
            return base * (1f + cycle.coerceAtMost(4) * 0.08f) * (0.94f + random.nextFloat() * 0.12f)
        }

        fun gate(y: Float, gapX: Float, gapWidth: Float, tone: Int = TONE_STEEL) {
            val halfGap = gapWidth * 0.5f
            segment(y, 0f, (gapX - halfGap).coerceAtLeast(0f), tone)
            segment(y, (gapX + halfGap).coerceAtMost(STAGE_WIDTH), STAGE_WIDTH, tone)
        }

        fun shoulders(y: Float, openingWidth: Float, tone: Int = TONE_STEEL) {
            gate(y, 5f, openingWidth, tone)
        }

        fun segment(y: Float, fromX: Float, toX: Float, tone: Int) {
            val width = toX - fromX
            if (width <= 0.08f) return
            blocks += Block(Point2((fromX + toX) * 0.5f, height * y), width, 0.22f, 0f, tone)
        }

        fun ramp(x: Float, y: Float, width: Float, angle: Float, tone: Int = TONE_STEEL) {
            blocks += Block(Point2(x, height * y), width, 0.23f, angle, tone)
        }

        fun hazard(
            x: Float,
            y: Float,
            radius: Float,
            motion: HazardMotion = HazardMotion.STATIC,
            travel: Float = 0f,
            speed: Float = 0f,
            phase: Float = 0f
        ) {
            hazards += Hazard(Point2(x, height * y), radius, motion, travel, speed, phase)
        }

        fun pulse(x: Float, y: Float, radius: Float, radial: Float, swirl: Float, phase: Float = 0f) {
            pulses += PulseZone(Point2(x, height * y), radius, radial, swirl, phase)
        }

        fun portal(entryX: Float, entryY: Float, exitX: Float, exitY: Float, radius: Float = 0.48f) {
            portals += PortalPair(Point2(entryX, height * entryY), Point2(exitX, height * exitY), radius, random.nextFloat() * PI.toFloat())
        }

        fun mirror() {
            start = Point2(STAGE_WIDTH - start.x, start.y)
            goal = Point2(STAGE_WIDTH - goal.x, goal.y)
            blocks.replaceAll { block ->
                block.copy(center = Point2(STAGE_WIDTH - block.center.x, block.center.y), angleRadians = -block.angleRadians)
            }
            hazards.replaceAll { hazard ->
                hazard.copy(center = Point2(STAGE_WIDTH - hazard.center.x, hazard.center.y), phase = hazard.phase + PI.toFloat())
            }
            pulses.replaceAll { pulse ->
                pulse.copy(center = Point2(STAGE_WIDTH - pulse.center.x, pulse.center.y), swirlForce = -pulse.swirlForce)
            }
            portals.replaceAll { portal ->
                portal.copy(
                    entry = Point2(STAGE_WIDTH - portal.entry.x, portal.entry.y),
                    exit = Point2(STAGE_WIDTH - portal.exit.x, portal.exit.y)
                )
            }
        }
    }

    private const val PATTERN_COUNT = 16
    private const val CLASSIC_SALT = 0x3451A11CL
    private const val CHAOS_SALT = 0x7A0C51E5L

    private val CLASSIC_TITLES = arrayOf(
        "Switchback Protocol", "Pendulum Run", "Crossing Signal", "Needle Thread",
        "Pulse Relay", "Split Decision", "Orbit Vault", "Cross Current",
        "Pinball Ladder", "Twin Crushers", "Quiet Knife", "Moving Stair",
        "Infinity Cut", "Pressure Locks", "Pulse Switchback", "Master Circuit"
    )

    private val CHAOS_TITLES = arrayOf(
        "Voro Orbit Riot", "Kav Crossfire", "Infinity Slop", "Crusher Collapse",
        "Gravity Roulette", "Mirror Blades", "Moving Cage", "Pinball Fever",
        "Triple Pendulum", "Voro Stormpath", "Void Clock", "Split Panic",
        "Rush Lanes", "Vortex Braid", "Dead Calm", "Kav Overload"
    )

    private val CLASSIC_ACCENTS = intArrayOf(
        0xFF1DE8C8.toInt(), 0xFFFFCF4A.toInt(), 0xFF64E572.toInt(),
        0xFF8AA6FF.toInt(), 0xFFFF8C42.toInt(), 0xFFF7F4FF.toInt()
    )

    private val CHAOS_ACCENTS = intArrayOf(
        0xFFFF4D8D.toInt(), 0xFFFF8C42.toInt(), 0xFFC15CFF.toInt(),
        0xFFFFCF4A.toInt(), 0xFF45F2FF.toInt(), 0xFFFF5757.toInt()
    )

    private val CLASSIC_CURSE_ORDER = arrayOf(
        CurseType.POWER_HOLD, CurseType.RIFT_DRAIN, CurseType.RIFT_WIND,
        CurseType.FOCUS_FIELD, CurseType.TINY_GATE, CurseType.PULSE_STORM,
        CurseType.HEAVY_CORE, CurseType.MOON_GLIDE, CurseType.OVERHEAT
    )

    private val CHAOS_CURSE_ORDER = arrayOf(
        CurseType.OVERHEAT, CurseType.PULSE_STORM, CurseType.RIFT_WIND,
        CurseType.RIFT_DRAIN, CurseType.POWER_HOLD, CurseType.TINY_GATE,
        CurseType.HEAVY_CORE, CurseType.MOON_GLIDE, CurseType.FOCUS_FIELD
    )

    private val MASCOTS = arrayOf(
        "KAVVI", "BLOP VORO", "MIMI VORO", "ZAZA KAV", "GLOBO KAV",
        "TIKKAV RIFT", "LALA VORO", "BYTE VORO", "FIZZ KAV", "WOMP KAV"
    )
}
