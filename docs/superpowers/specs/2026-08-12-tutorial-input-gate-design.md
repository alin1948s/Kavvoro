# Tutorial Input Gate Design

## Problem

The training card shown while a level is in `GameState.READY` looks interactive,
but it is only painted onto `ChaosGameView`. The `TAP`, `WALL`, and `EXIT` chips
are labels, not buttons, and `onTouchEvent` does not know that the card exists.
Consequently, any touch outside the HUD buttons calls `startRiftControl`, which
both starts the simulation and applies Rift force. A swipe that Android later
cancels can therefore launch the ball before the player has dismissed or read
the lesson.

This is the direct cause of the external review report. Difficulty balancing is
outside this change; the goal here is to eliminate accidental attempts and make
the training interaction unambiguous.

## Approved User Experience

- Every training level from L1 through L10 presents its lesson before the first
  attempt in each game mode.
- Any later level that presents the same full tutorial card (for example a Rift
  Module lesson) follows the same input-safety rule.
- The card replaces the button-like `TAP`, `WALL`, and `EXIT` chips with one
  large, localized `START LEVEL` action.
- While the card is visible, gameplay touches and gestures are consumed. HUD
  controls such as Home, sound, music, and restart remain real controls and do
  not launch the ball.
- Pressing `START LEVEL` only dismisses the card. It never starts the simulation
  or applies Rift force. Starting the run requires a separate subsequent touch.
- A press must begin and end inside `START LEVEL` without becoming a drag.
  `ACTION_CANCEL`, swipes, and releases outside the button do nothing beyond
  consuming that gesture.
- The acknowledgement is stored per game mode and level. Retrying the same
  level does not show the card again, and learning a Classic lesson does not
  suppress the corresponding Chaos lesson.
- Starting a mode again from L1 does not erase lessons already acknowledged.
  Clearing app data or reinstalling naturally resets them.

## Architecture

### Pure input state machine

Add a small Android-independent tutorial gate state machine. It receives a
normalized pointer action (`DOWN`, `MOVE`, `UP`, or `CANCEL`) plus whether the
pointer is inside the action button. It returns whether the event was consumed
and whether the card was dismissed.

This component owns only gesture semantics. It has no `View`, `Canvas`,
`MotionEvent`, or `SharedPreferences` dependency, which allows the exact bug to
be covered by local unit tests.

### `ChaosGameView` integration

`ChaosGameView` determines card visibility from these facts:

1. the game is on the gameplay screen;
2. the round is `READY`;
3. the level has tutorial copy;
4. the mode-and-level acknowledgement key is absent.

Touch routing preserves existing HUD button handling first. If no HUD control
owns the touch and the tutorial card is visible, the normalized event goes to
the tutorial gate and is consumed. Only a completed `START LEVEL` press records
the acknowledgement and hides the card. The same event returns immediately;
it cannot fall through to `startRiftControl`.

Round reset reconstructs visibility from the stored acknowledgement. A retry
therefore stays unobstructed after the player has explicitly started that
lesson once.

### Rendering and persistence

The card remains in its existing bottom-safe area and keeps its lesson text,
icon, reward line, and opaque surface. Its footer becomes one visually primary
button whose hit rectangle is calculated by the same layout function used for
drawing. The decorative chips are removed so no non-interactive element looks
pressable.

Acknowledgements use `SharedPreferences` keys containing both mode and level.
Missing keys mean unacknowledged, so existing installations require no data
migration and will see each corrected lesson once.

`START LEVEL` is added to the localization catalog rather than hard-coded in
English.

## Failure and Lifecycle Behavior

- A system-edge swipe followed by `ACTION_CANCEL` leaves the card open and the
  ball stationary.
- A drag that begins on the action but leaves it does not dismiss the card.
- Leaving to the menu before acknowledgement stores nothing.
- Surface recreation and app restart recompute visibility from persistence.
- Acknowledgement is written before the card disappears, preventing it from
  reopening if the process is interrupted immediately afterward.

## Verification

### Local tests

The tutorial gate tests must prove:

1. a tap or swipe outside the action is consumed without dismissal;
2. `ACTION_CANCEL` never dismisses;
3. down-inside/up-inside dismisses exactly once;
4. down-inside/move-outside/up-inside does not dismiss;
5. an acknowledged mode-and-level pair is hidden while another mode remains
   eligible;
6. L1-L10 and later full tutorial cards share the safe input rule.

### Android verification

On an emulator or device, continue Classic at a training level and verify:

- the ball remains at its start position while tapping lesson text, the old chip
  area, and the playfield;
- swiping for Android system navigation does not start the run;
- `START LEVEL` closes the card without moving the ball;
- the next deliberate gameplay touch starts the run;
- retry does not reopen the acknowledged lesson;
- the corresponding Chaos lesson is still shown;
- compact portrait layouts keep all copy and the action inside the card.

Run all JVM tests and build the debug APK after device verification.
