package com.moonsolstudios.kavvoro.engine

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max

class PhysicsImpactAudioTest {
    @Test
    fun platformContactExposesImpactStrengthForDynamicBounceAudio() {
        val level = LevelSpec(
            index = 20,
            seed = 77L,
            title = "Impact Audio Test",
            stageHeight = 18f,
            start = Point2(5f, 2f),
            goal = Point2(9f, 16f),
            goalRadius = 0.5f,
            inkLimit = 6f,
            drawSeconds = 3f,
            gravity = 9.8f,
            blocks = listOf(Block(Point2(5f, 4.3f), 5f, 0.25f, 0f, 0xFFFFFFFF.toInt())),
            hazards = emptyList(),
            pulseZones = emptyList(),
            accent = 0xFFFFFFFF.toInt()
        )
        val engine = PhysicsEngine().apply { reset(level) }
        var strongestImpact = 0f

        repeat(180) { frameIndex ->
            val frame = engine.step(1f / 120f, frameIndex / 120f)
            strongestImpact = max(strongestImpact, frame.impactStrength)
        }

        assertTrue("Expected an audible platform impact, got $strongestImpact", strongestImpact >= 0.7f)
    }
}
