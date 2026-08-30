package com.moonsolstudios.kavvoro.startup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstFrameStartupGateTest {
    @Test
    fun `runs optional startup only once`() {
        val gate = FirstFrameStartupGate()
        var starts = 0

        assertTrue(gate.runOnce { starts += 1 })
        assertFalse(gate.runOnce { starts += 1 })

        assertEquals(1, starts)
    }

    @Test
    fun `does not run startup after activity destruction`() {
        val gate = FirstFrameStartupGate()
        var starts = 0

        gate.cancel()

        assertFalse(gate.runOnce { starts += 1 })
        assertEquals(0, starts)
    }
}
