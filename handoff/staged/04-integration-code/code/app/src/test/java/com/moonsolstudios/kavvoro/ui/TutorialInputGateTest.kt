package com.moonsolstudios.kavvoro.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialInputGateTest {
    @Test
    fun cleanActionTapDismissesWithoutStartingPlay() {
        val gate = TutorialInputGate()

        gate.onPointer(
            TutorialPointerAction.DOWN,
            TutorialTouchTarget.ACTION_BUTTON,
            movedBeyondTapSlop = false
        )

        assertEquals(
            TutorialGateOutcome.DISMISS_ONLY,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.ACTION_BUTTON,
                movedBeyondTapSlop = false
            ).outcome
        )
    }

    @Test
    fun cleanPlayfieldTapDismissesAndStartsPlay() {
        val gate = TutorialInputGate()

        gate.onPointer(
            TutorialPointerAction.DOWN,
            TutorialTouchTarget.PLAYFIELD,
            movedBeyondTapSlop = false
        )

        assertEquals(
            TutorialGateOutcome.DISMISS_AND_PLAY,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.PLAYFIELD,
                movedBeyondTapSlop = false
            ).outcome
        )
    }

    @Test
    fun cardBodyTapIsConsumedWithoutDismissal() {
        val gate = TutorialInputGate()

        assertTrue(
            gate.onPointer(
                TutorialPointerAction.DOWN,
                TutorialTouchTarget.CARD,
                movedBeyondTapSlop = false
            ).consumed
        )
        assertEquals(
            TutorialGateOutcome.NONE,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.CARD,
                movedBeyondTapSlop = false
            ).outcome
        )
    }

    @Test
    fun movementBeyondTouchSlopCancelsPlayfieldTap() {
        val gate = TutorialInputGate()

        gate.onPointer(
            TutorialPointerAction.DOWN,
            TutorialTouchTarget.PLAYFIELD,
            movedBeyondTapSlop = false
        )
        gate.onPointer(
            TutorialPointerAction.MOVE,
            TutorialTouchTarget.PLAYFIELD,
            movedBeyondTapSlop = true
        )

        assertEquals(
            TutorialGateOutcome.NONE,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.PLAYFIELD,
                movedBeyondTapSlop = true
            ).outcome
        )
    }

    @Test
    fun distantUpWithoutMoveIsNotAPlayfieldTap() {
        val gate = TutorialInputGate()
        gate.onPointer(
            TutorialPointerAction.DOWN,
            TutorialTouchTarget.PLAYFIELD,
            movedBeyondTapSlop = false
        )

        val movedBeyondSlop = TutorialGestureSlop.exceeded(
            downX = 10f,
            downY = 20f,
            currentX = 110f,
            currentY = 20f,
            touchSlop = 8f
        )

        assertTrue(movedBeyondSlop)
        assertEquals(
            TutorialGateOutcome.NONE,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.PLAYFIELD,
                movedBeyondTapSlop = movedBeyondSlop
            ).outcome
        )
    }

    @Test
    fun crossingTouchTargetsCancelsTheGesture() {
        val gate = TutorialInputGate()

        gate.onPointer(
            TutorialPointerAction.DOWN,
            TutorialTouchTarget.PLAYFIELD,
            movedBeyondTapSlop = false
        )
        gate.onPointer(
            TutorialPointerAction.MOVE,
            TutorialTouchTarget.CARD,
            movedBeyondTapSlop = false
        )

        assertEquals(
            TutorialGateOutcome.NONE,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.PLAYFIELD,
                movedBeyondTapSlop = false
            ).outcome
        )
    }

    @Test
    fun cancelAndMultiTouchNeverEmitAnOutcome() {
        listOf(
            TutorialPointerAction.CANCEL,
            TutorialPointerAction.MULTI_TOUCH
        ).forEach { interrupt ->
            val gate = TutorialInputGate()
            gate.onPointer(
                TutorialPointerAction.DOWN,
                TutorialTouchTarget.PLAYFIELD,
                movedBeyondTapSlop = false
            )

            val interrupted = gate.onPointer(
                interrupt,
                TutorialTouchTarget.PLAYFIELD,
                movedBeyondTapSlop = false
            )

            assertTrue(interrupted.consumed)
            assertEquals(TutorialGateOutcome.NONE, interrupted.outcome)
            assertEquals(
                TutorialGateOutcome.NONE,
                gate.onPointer(
                    TutorialPointerAction.UP,
                    TutorialTouchTarget.PLAYFIELD,
                    movedBeyondTapSlop = false
                ).outcome
            )
        }
    }

    @Test
    fun actionOutcomeIsEmittedAtMostOnce() {
        val gate = TutorialInputGate()
        gate.onPointer(
            TutorialPointerAction.DOWN,
            TutorialTouchTarget.ACTION_BUTTON,
            movedBeyondTapSlop = false
        )

        assertEquals(
            TutorialGateOutcome.DISMISS_ONLY,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.ACTION_BUTTON,
                movedBeyondTapSlop = false
            ).outcome
        )
        assertEquals(
            TutorialGateOutcome.NONE,
            gate.onPointer(
                TutorialPointerAction.UP,
                TutorialTouchTarget.ACTION_BUTTON,
                movedBeyondTapSlop = false
            ).outcome
        )
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
