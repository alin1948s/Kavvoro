package com.moonsolstudios.kavvoro.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialInputGateTest {
    @Test
    fun outsideTapAndSwipeAreConsumedWithoutDismissal() {
        val gate = TutorialInputGate()

        assertTrue(gate.onPointer(TutorialPointerAction.DOWN, false).consumed)
        assertFalse(gate.onPointer(TutorialPointerAction.MOVE, false).dismissed)
        assertFalse(gate.onPointer(TutorialPointerAction.UP, false).dismissed)
    }

    @Test
    fun cancelNeverDismisses() {
        val gate = TutorialInputGate()

        gate.onPointer(TutorialPointerAction.DOWN, true)
        val result = gate.onPointer(TutorialPointerAction.CANCEL, true)

        assertTrue(result.consumed)
        assertFalse(result.dismissed)
        assertFalse(gate.actionPressed)
    }

    @Test
    fun cleanActionTapDismissesExactlyOnce() {
        val gate = TutorialInputGate()

        gate.onPointer(TutorialPointerAction.DOWN, true)
        assertTrue(gate.actionPressed)
        assertTrue(gate.onPointer(TutorialPointerAction.UP, true).dismissed)
        assertFalse(gate.onPointer(TutorialPointerAction.UP, true).dismissed)
    }

    @Test
    fun leavingActionCancelsPressEvenAfterReturning() {
        val gate = TutorialInputGate()

        gate.onPointer(TutorialPointerAction.DOWN, true)
        gate.onPointer(TutorialPointerAction.MOVE, false)
        gate.onPointer(TutorialPointerAction.MOVE, true)

        assertFalse(gate.onPointer(TutorialPointerAction.UP, true).dismissed)
    }

    @Test
    fun acknowledgementSeparatesModeAndLevel() {
        val classic3 = TutorialInputGate.acknowledgementKey("CLASSIC", 3)

        assertNotEquals(
            classic3,
            TutorialInputGate.acknowledgementKey("CHAOS", 3)
        )
        assertNotEquals(
            classic3,
            TutorialInputGate.acknowledgementKey("CLASSIC", 4)
        )
    }

    @Test
    fun lessonIsVisibleOnlyWhileReadyAndUnacknowledged() {
        assertTrue(TutorialInputGate.shouldShow(true, true, true, false))
        assertFalse(TutorialInputGate.shouldShow(false, true, true, false))
        assertFalse(TutorialInputGate.shouldShow(true, false, true, false))
        assertFalse(TutorialInputGate.shouldShow(true, true, false, false))
        assertFalse(TutorialInputGate.shouldShow(true, true, true, true))
    }
}
