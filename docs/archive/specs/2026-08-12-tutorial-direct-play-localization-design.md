# Tutorial Direct Play and Full Localization Design

> Historical design record. Current code and the repository README are
> authoritative.

**Date:** 2026-08-12  
**Status:** Approved design  
**Scope:** Kavvoro Android tutorial presentation and input behavior

## Goal

Make the training card fast to leave without restoring the accidental-launch bug:

- a deliberate tap on the playfield outside the card closes the card and starts play with that same tap;
- a swipe, drag, system-edge gesture, cancel, or multi-touch sequence never closes the card and never launches the ball;
- `START LEVEL` remains the safe dismiss-only action;
- the action button is visually centered inside the card;
- every visible tutorial string is explicitly translated in every supported language.

## Confirmed Interaction Model

The tutorial recognizes three touch targets:

1. `ACTION_BUTTON` — the centered `START LEVEL` button;
2. `CARD` — any other point inside the tutorial card;
3. `PLAYFIELD` — any non-HUD point outside the tutorial card.

HUD controls keep their existing precedence and behavior.

### Clean tap rules

- `DOWN` and `UP` inside `ACTION_BUTTON`, without exceeding Android touch slop: persist acknowledgement and dismiss the card only. The ball remains stationary.
- `DOWN` and `UP` on `PLAYFIELD`, without exceeding touch slop: persist acknowledgement, dismiss the card, and immediately apply that tap to Rift control at the release coordinate. This is the first gameplay action.
- A tap inside `CARD` but outside the action button is consumed and leaves the card open.
- A pointer that crosses between targets, exceeds touch slop, is cancelled, or becomes multi-touch is consumed and cannot dismiss or start play.
- System-edge navigation gestures therefore remain harmless even if Android sends part of the sequence to the view.

The acknowledgement write remains synchronous. If persistence fails, the card stays visible and gameplay does not start.

## Input Architecture

`TutorialInputGate` remains Android-independent. Its input expands from a Boolean button hit to:

- normalized pointer action;
- normalized target (`ACTION_BUTTON`, `CARD`, `PLAYFIELD`);
- whether movement has exceeded the tap threshold.

Its result distinguishes:

- consumed with no action;
- dismiss only;
- dismiss and start play.

`ChaosGameView` owns Android details:

- captures the initial coordinates;
- obtains the platform touch-slop value;
- classifies points using a shared card rectangle and the button rectangle;
- forwards all pointer events to the gate before Rift control;
- performs dismissal and, only for a clean playfield tap, calls the existing Rift start path with the release coordinates.

No tutorial pointer event may fall through implicitly to gameplay. Starting from the outside tap is an explicit result from the gate.

## Popup Layout

The card remains bottom-anchored. The icon and text retain their current content column.

The action button no longer begins at the text-column inset. Its rectangle uses equal left and right card padding, so its geometric center equals the card center. The divider above it uses the same centered horizontal bounds. Button text remains center-aligned and is fitted when a translation is unusually long.

The full card rectangle is stored during layout/drawing and reused for touch classification, avoiding visual and hit-area drift.

For Arabic, tutorial header, body, obstacle line, and footer use right alignment within the text column. The action label stays geometrically centered. Mixed product tokens such as `Rift`, `HYPE`, `BOOST`, `IN`, and `OUT` may remain unchanged inside otherwise translated sentences.

## Localization Scope

The supported languages are English plus Romanian, Spanish, French, German, Italian, Portuguese, Dutch, Polish, Turkish, Russian, Ukrainian, Arabic, Hindi, Indonesian, Vietnamese, Japanese, Korean, and Simplified Chinese.

Every key rendered on a tutorial gameplay screen must have an explicit value for all 20 languages:

- card headers and footer messages;
- `START LEVEL`;
- all Classic and Chaos tutorial level titles;
- every level-specific instructional sentence;
- portal-module instructional sentences;
- every obstacle sentence;
- tutorial action labels such as tap variants;
- field labels such as `RIFT ENERGY`, `BOOST`, `AVOID`, `BOUNCE WALL`, portal and exit labels;
- the stable HUD labels visible during training, except intentionally universal product tokens such as `HYPE`.

A required-row helper without default language arguments will be used for tutorial copy. This makes missing translations a compile-time error when a row is edited. A centralized tutorial-key inventory will also support an automated coverage test and prevent silent English fallback.

Translation does not rename the Kavvoro, Rift, HYPE, Voro Grad, Classic, or Chaos brands unless the existing language convention already localizes the surrounding mode label.

## Testing

### Pure input tests

Cover:

- clean action-button tap returns dismiss-only;
- clean outside-card playfield tap returns dismiss-and-play;
- card-body tap is consumed with no action;
- movement beyond touch slop cancels both actions;
- crossing between card/button/playfield cancels;
- `CANCEL` and multi-touch cancel;
- an action is emitted at most once.

### Localization tests

- every tutorial key has an explicit nonblank value for every supported language;
- the complete inventory includes all strings returned by tutorial lesson, obstacle, header, footer, title, and field-label selectors;
- intentionally shared product tokens are allowlisted rather than mistaken for missing translations.

### Android verification

On the emulator:

1. tap card body — nothing starts and the card remains;
2. swipe in the playfield and perform a system-edge swipe — nothing starts and the card remains;
3. tap outside the card — card closes and that tap starts the ball;
4. tap `START LEVEL` on a fresh lesson — card closes but ball stays still;
5. retry — acknowledged lesson remains closed;
6. Classic and Chaos acknowledgements remain independent;
7. inspect English, Romanian, German, Arabic, Hindi, Japanese, and Chinese as representative width and script cases;
8. confirm all languages pass automated explicit-translation coverage.

## Error Handling and Compatibility

- Existing acknowledgement keys remain unchanged, so current users do not see dismissed lessons again.
- Progress reset does not clear acknowledgements.
- A failed preference commit leaves the card and input lock intact.
- Existing HUD, ad, result, menu, and non-tutorial gameplay routing remain outside this change.

## Acceptance Criteria

- One clean playfield tap outside the popup closes it and starts play.
- No swipe, drag, cancel, system gesture, or multi-touch sequence starts play while the popup is visible.
- `START LEVEL` closes only the popup.
- The button and divider are visually centered with equal card margins.
- No tutorial-screen key silently falls back to English in any supported non-English language.
- JVM tests, Android compilation, debug APK build, and targeted emulator verification pass.
