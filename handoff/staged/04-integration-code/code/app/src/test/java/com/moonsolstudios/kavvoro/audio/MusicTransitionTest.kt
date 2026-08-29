package com.moonsolstudios.kavvoro.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicTransitionTest {
    @Test
    fun `crossfade steps are deterministic bounded and monotonic`() {
        val first = MusicTransition.steps(0f, 1f, 180L)
        val second = MusicTransition.steps(0f, 1f, 180L)

        assertEquals(first, second)
        assertEquals(0f, first.first(), 0.0001f)
        assertEquals(1f, first.last(), 0.0001f)
        assertTrue(first.all { it in 0f..1f })
        assertTrue(first.zipWithNext().all { (left, right) -> left <= right })
        assertTrue(first.size <= 9)
    }

    @Test
    fun `crossfade supports fade out and zero duration endpoints`() {
        val fadeOut = MusicTransition.steps(1f, 0f, 180L)
        val instant = MusicTransition.steps(0.3f, 0.8f, 0L)

        assertEquals(1f, fadeOut.first(), 0.0001f)
        assertEquals(0f, fadeOut.last(), 0.0001f)
        assertTrue(fadeOut.zipWithNext().all { (left, right) -> left >= right })
        assertEquals(listOf(0.3f, 0.8f), instant)
    }
}
