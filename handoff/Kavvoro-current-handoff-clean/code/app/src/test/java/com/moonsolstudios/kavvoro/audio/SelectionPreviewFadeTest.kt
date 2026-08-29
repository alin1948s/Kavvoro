package com.moonsolstudios.kavvoro.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionPreviewFadeTest {
    @Test
    fun `selection preview fade eases the old voice to silence`() {
        val steps = SelectionPreviewFade.steps(0.86f)

        assertEquals(7, steps.size)
        assertEquals(0L, steps.first().elapsedMs)
        assertEquals(0.86f, steps.first().volume, 0.0001f)
        assertEquals(180L, steps.last().elapsedMs)
        assertEquals(0f, steps.last().volume, 0.0001f)
        assertTrue(steps.drop(1).zipWithNext().all { (left, right) -> left.volume >= right.volume })
        assertTrue(steps[1].volume > 0.65f)
        assertTrue(steps[5].volume > 0f)
    }
}
