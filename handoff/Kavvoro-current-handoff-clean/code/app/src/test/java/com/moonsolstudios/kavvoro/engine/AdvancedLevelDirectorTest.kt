package com.moonsolstudios.kavvoro.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs

class AdvancedLevelDirectorTest {
    private val height = 18f
    private val seed = 20_260_630L

    @Test
    fun advancedFamiliesDoNotRepeatDuringFirstRotation() {
        val classic = (15..30).map { LevelDirector.createClassic(it, height, seed).title }
        val chaos = (15..30).map { LevelDirector.createChaos(it, height, seed).title }

        assertEquals(16, classic.toSet().size)
        assertEquals(16, chaos.toSet().size)
        assertTrue(classic.toSet().intersect(chaos.toSet()).isEmpty())
    }

    @Test
    fun pressureIncreasesBeyondLevelFifteen() {
        val classicEarly = (15..22).map { LevelDirector.createClassic(it, height, seed) }
        val classicLate = (45..52).map { LevelDirector.createClassic(it, height, seed) }
        val chaosEarly = (15..22).map { LevelDirector.createChaos(it, height, seed) }
        val chaosLate = (45..52).map { LevelDirector.createChaos(it, height, seed) }

        assertTrue(classicLate.map(::complexity).average() > classicEarly.map(::complexity).average())
        assertTrue(chaosLate.map(::complexity).average() > chaosEarly.map(::complexity).average())
        assertTrue(classicLate.first().timeLimitSeconds < classicEarly.first().timeLimitSeconds)
        assertTrue(chaosLate.first().riftDrainMultiplier > chaosEarly.first().riftDrainMultiplier)
        assertTrue(classicLate.any { level -> level.hazards.any(Hazard::isMoving) })
        assertTrue(chaosLate.all { level -> level.hazards.any(Hazard::isMoving) || level.pulseZones.size >= 3 })
    }

    @Test
    fun advancedGenerationIsDeterministic() {
        for (level in listOf(15, 31, 50, 86, 130)) {
            assertEquals(
                LevelDirector.createClassic(level, height, seed),
                LevelDirector.createClassic(level, height, seed)
            )
            assertEquals(
                LevelDirector.createChaos(level, height, seed),
                LevelDirector.createChaos(level, height, seed)
            )
        }
    }

    @Test
    fun generatedGeometryStaysInsidePlayableStage() {
        for (levelIndex in 15..130) {
            validate(LevelDirector.createClassic(levelIndex, height, seed))
            validate(LevelDirector.createChaos(levelIndex, height, seed))
        }
    }

    @Test
    fun chaosLevelTwentySixKeepsAPhysicalRouteAroundItsBarriers() {
        validateMechanicalCorridors(LevelDirector.createChaos(26, height, seed))
    }

    @Test
    fun generatedFlatBarriersKeepPhysicalSidePassages() {
        val seeds = listOf(seed, seed + 1L, 20_260_704L, 867_5309L)
        for (candidateSeed in seeds) {
            for (levelIndex in 15..180) {
                validateMechanicalCorridors(LevelDirector.createClassic(levelIndex, height, candidateSeed))
                validateMechanicalCorridors(LevelDirector.createChaos(levelIndex, height, candidateSeed))
            }
        }
    }

    @Test
    fun movingHazardFollowsDeclaredTrack() {
        val hazard = Hazard(
            center = Point2(5f, 8f),
            radius = 0.3f,
            motion = HazardMotion.HORIZONTAL,
            travel = 2f,
            speed = 2f
        )

        assertEquals(Point2(5f, 8f), hazard.positionAt(0f))
        val peak = hazard.positionAt(PI.toFloat() / 4f)
        assertEquals(7f, peak.x, 0.0001f)
        assertEquals(8f, peak.y, 0.0001f)
    }

    private fun validate(level: LevelSpec) {
        assertTrue(level.start.x in 0.7f..9.3f)
        assertTrue(level.start.y in 3.2f..5f)
        assertTrue(level.goal.x in 0.7f..9.3f)
        assertTrue(level.goal.y in (level.stageHeight * 0.75f)..(level.stageHeight - 1f))
        assertTrue(level.goalRadius >= 0.38f)
        assertTrue(level.timeLimitSeconds >= 7.8f)
        assertTrue(level.riftDrainMultiplier in 1f..1.36f)

        level.blocks.forEach { block ->
            assertTrue(block.center.x in 0f..STAGE_WIDTH)
            assertTrue(block.center.y in 4.5f..(level.stageHeight - 1.2f))
            assertTrue(block.width > 0f && block.width <= STAGE_WIDTH)
        }
        level.hazards.forEach { hazard ->
            val xTravel = when (hazard.motion) {
                HazardMotion.HORIZONTAL, HazardMotion.ORBIT, HazardMotion.FIGURE_EIGHT -> hazard.travel
                else -> 0f
            }
            val yTravel = when (hazard.motion) {
                HazardMotion.VERTICAL, HazardMotion.ORBIT -> hazard.travel
                HazardMotion.FIGURE_EIGHT -> hazard.travel * 0.5f
                else -> 0f
            }
            assertTrue(hazard.center.x - xTravel - hazard.radius >= 0.15f)
            assertTrue(hazard.center.x + xTravel + hazard.radius <= STAGE_WIDTH - 0.15f)
            assertTrue(hazard.center.y - yTravel - hazard.radius >= 4.2f)
            assertTrue(hazard.center.y + yTravel + hazard.radius <= level.stageHeight - 0.65f)
        }
    }

    private fun validateMechanicalCorridors(level: LevelSpec) {
        val leftWall = STAGE_WALL_INSET
        val rightWall = STAGE_WIDTH - STAGE_WALL_INSET
        level.blocks.forEach { block ->
            if (abs(block.angleRadians) > 0.08f) return@forEach
            val leftEdge = block.center.x - block.width * 0.5f
            val rightEdge = block.center.x + block.width * 0.5f
            if (leftEdge <= leftWall || rightEdge >= rightWall) return@forEach
            val leftCorridor = leftEdge - leftWall
            val rightCorridor = rightWall - rightEdge
            assertTrue(
                "Level ${level.index} '${level.title}' has an unpassable flat barrier: $block, left=$leftCorridor, right=$rightCorridor",
                leftCorridor >= MIN_TEST_SIDE_CORRIDOR || rightCorridor >= MIN_TEST_SIDE_CORRIDOR
            )
        }
    }

    private fun complexity(level: LevelSpec): Int {
        return level.blocks.size +
            level.hazards.size * 2 +
            level.hazards.count(Hazard::isMoving) * 3 +
            level.pulseZones.size * 2 +
            level.curses.size * 3 +
            level.difficultyRating
    }

    companion object {
        private const val MIN_TEST_SIDE_CORRIDOR = 0.84f
    }
}
