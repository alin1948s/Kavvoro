package com.moonsolstudios.kavvoro.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysicsPowerTest {
    @Test
    fun prismShieldBlocksFirstHazardCollision() {
        val engine = PhysicsEngine()
        engine.reset(levelWithHazard(Point2(5f, 5.12f), 0.34f), BallPower.PRISM_SHIELD)

        val frame = engine.step(1f / 60f, 0.02f)

        assertTrue(frame.outcome == PhysicsOutcome.RUNNING)
        assertTrue(frame.powerTriggered)
    }

    @Test
    fun voidPhaseUsesSmallerHazardHitbox() {
        val level = levelWithHazard(Point2(5.45f, 5f), 0.2f)
        val regular = PhysicsEngine().apply { reset(level, BallPower.NONE) }
        val phased = PhysicsEngine().apply { reset(level, BallPower.VOID_PHASE) }

        val regularFrame = regular.step(1f / 120f, 0.01f)
        val phasedFrame = phased.step(1f / 120f, 0.01f)

        assertTrue(regularFrame.outcome == PhysicsOutcome.LOST)
        assertTrue(phasedFrame.outcome == PhysicsOutcome.RUNNING)
        assertFalse(phasedFrame.powerTriggered)
    }

    @Test
    fun minorPhaseIsWeakerThanPremiumPhase() {
        val level = levelWithHazard(Point2(5.35f, 5f), 0.2f)
        val minor = PhysicsEngine().apply { reset(level, BallPower.MINOR_PHASE) }
        val premium = PhysicsEngine().apply { reset(level, BallPower.VOID_PHASE) }

        val minorFrame = minor.step(1f / 120f, 0.01f)
        val premiumFrame = premium.step(1f / 120f, 0.01f)

        assertTrue(minorFrame.outcome == PhysicsOutcome.LOST)
        assertTrue(premiumFrame.outcome == PhysicsOutcome.RUNNING)
    }

    private fun levelWithHazard(center: Point2, radius: Float): LevelSpec {
        return LevelSpec(
            index = 50,
            seed = 1L,
            title = "Power Test",
            stageHeight = 18f,
            start = Point2(5f, 5f),
            goal = Point2(9f, 16f),
            goalRadius = 0.5f,
            inkLimit = 6f,
            drawSeconds = 3f,
            gravity = 0f,
            blocks = emptyList(),
            hazards = listOf(Hazard(center, radius)),
            pulseZones = emptyList(),
            accent = 0xFFFFFFFF.toInt()
        )
    }
}
