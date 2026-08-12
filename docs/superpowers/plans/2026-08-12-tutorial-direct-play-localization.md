# Tutorial Direct Play and Full Localization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let one deliberate playfield tap close the tutorial and start play, center the safe action inside the card, and explicitly translate the complete tutorial screen in every supported language.

**Architecture:** Extend the Android-independent `TutorialInputGate` to classify clean gestures by target and return an explicit outcome instead of relying on touch fallthrough. Keep Android coordinates, touch slop, persistence, and Rift startup in `ChaosGameView`; move required tutorial copy into a strict catalog that cannot default missing languages to English.

**Tech Stack:** Kotlin/JVM 17, Android `SurfaceView`/`Canvas`, `MotionEvent`, `ViewConfiguration`, `SharedPreferences`, JUnit 4, Gradle Android plugin.

## Global Constraints

- Work directly on `main` in `C:\Users\Alin\Desktop\MoonsolStudios\Kavvoro`; do not create a branch, worktree, or pull request.
- A clean tap outside the tutorial card dismisses it and explicitly starts Rift control with the release coordinate.
- `START LEVEL` dismisses only; it never starts Rift control.
- Card-body taps, movement past Android touch slop, target crossing, `CANCEL`, and multi-touch never dismiss or start play.
- HUD controls retain precedence.
- Persist acknowledgement synchronously before either dismissal outcome; persistence failure keeps the card and input lock active.
- Keep existing `tutorial_ack_<mode>_<level>` keys and mode separation.
- Center the button and divider using equal card-relative margins.
- Provide explicit tutorial-screen translations for all 19 supported languages (English plus 18 others).
- Keep Kavvoro, Rift, HYPE, Voro Grad, Classic, Chaos, BOOST, IN, and OUT as product/game tokens where natural.
- Never commit `app/google-services.json`, signing files, APKs, AABs, emulator data, or screenshots.

---

### Task 1: Clean gesture outcomes in the pure input gate

**Files:**
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialInputGate.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt`

**Interfaces:**
- Consumes: normalized pointer actions, normalized touch targets, and a Boolean touch-slop signal.
- Produces: `TutorialTouchTarget`, `TutorialGateOutcome`, and `TutorialGateResult.outcome`; `TutorialInputGate.onPointer(action, target, movedBeyondTapSlop)`.

- [x] **Step 1: Replace the button-only tests with failing target/outcome tests**

Add these public types to the test contract:

```kotlin
enum class TutorialTouchTarget { ACTION_BUTTON, CARD, PLAYFIELD }
enum class TutorialGateOutcome { NONE, DISMISS_ONLY, DISMISS_AND_PLAY }
```

Add tests for the exact behavior:

```kotlin
@Test
fun cleanActionTapDismissesWithoutStartingPlay() {
    val gate = TutorialInputGate()
    gate.onPointer(DOWN, ACTION_BUTTON, false)
    assertEquals(
        DISMISS_ONLY,
        gate.onPointer(UP, ACTION_BUTTON, false).outcome
    )
}

@Test
fun cleanPlayfieldTapDismissesAndStartsPlay() {
    val gate = TutorialInputGate()
    gate.onPointer(DOWN, PLAYFIELD, false)
    assertEquals(
        DISMISS_AND_PLAY,
        gate.onPointer(UP, PLAYFIELD, false).outcome
    )
}

@Test
fun cardBodyTapDoesNothing() {
    val gate = TutorialInputGate()
    gate.onPointer(DOWN, CARD, false)
    assertEquals(NONE, gate.onPointer(UP, CARD, false).outcome)
}

@Test
fun movementAndTargetCrossingCancelTheGesture() {
    TutorialInputGate().run {
        onPointer(DOWN, PLAYFIELD, false)
        assertEquals(NONE, onPointer(MOVE, PLAYFIELD, true).outcome)
        assertEquals(NONE, onPointer(UP, PLAYFIELD, true).outcome)
    }
    TutorialInputGate().run {
        onPointer(DOWN, PLAYFIELD, false)
        onPointer(MOVE, CARD, false)
        assertEquals(NONE, onPointer(UP, PLAYFIELD, false).outcome)
    }
}

@Test
fun cancelAndMultiTouchNeverEmitAnOutcome() {
    listOf(CANCEL, MULTI_TOUCH).forEach { interrupt ->
        val gate = TutorialInputGate()
        gate.onPointer(DOWN, PLAYFIELD, false)
        assertEquals(NONE, gate.onPointer(interrupt, PLAYFIELD, false).outcome)
        assertEquals(NONE, gate.onPointer(UP, PLAYFIELD, false).outcome)
    }
}
```

Retain the acknowledgement and visibility tests.

- [x] **Step 2: Run the focused test and verify RED**

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\Alin\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest --tests '*TutorialInputGateTest'
```

Expected: compilation fails because `TutorialTouchTarget`, `TutorialGateOutcome`, and the new `onPointer` signature do not exist.

- [x] **Step 3: Implement the minimal target-aware state machine**

Use this shape in `TutorialInputGate.kt`:

```kotlin
enum class TutorialTouchTarget { ACTION_BUTTON, CARD, PLAYFIELD }
enum class TutorialGateOutcome { NONE, DISMISS_ONLY, DISMISS_AND_PLAY }

data class TutorialGateResult(
    val consumed: Boolean = true,
    val outcome: TutorialGateOutcome = TutorialGateOutcome.NONE
)

class TutorialInputGate {
    private var initialTarget: TutorialTouchTarget? = null
    private var invalidated = false

    val actionPressed: Boolean
        get() = initialTarget == TutorialTouchTarget.ACTION_BUTTON && !invalidated

    fun onPointer(
        action: TutorialPointerAction,
        target: TutorialTouchTarget,
        movedBeyondTapSlop: Boolean
    ): TutorialGateResult = when (action) {
        TutorialPointerAction.DOWN -> {
            initialTarget = target
            invalidated = movedBeyondTapSlop
            TutorialGateResult()
        }
        TutorialPointerAction.MOVE -> {
            if (movedBeyondTapSlop || target != initialTarget) invalidated = true
            TutorialGateResult()
        }
        TutorialPointerAction.UP -> {
            val start = initialTarget
            val clean = !invalidated && !movedBeyondTapSlop && target == start
            val outcome = when {
                clean && start == TutorialTouchTarget.ACTION_BUTTON ->
                    TutorialGateOutcome.DISMISS_ONLY
                clean && start == TutorialTouchTarget.PLAYFIELD ->
                    TutorialGateOutcome.DISMISS_AND_PLAY
                else -> TutorialGateOutcome.NONE
            }
            reset()
            TutorialGateResult(outcome = outcome)
        }
        TutorialPointerAction.MULTI_TOUCH,
        TutorialPointerAction.CANCEL -> {
            reset()
            TutorialGateResult()
        }
    }

    fun reset() {
        initialTarget = null
        invalidated = false
    }
}
```

- [x] **Step 4: Verify the focused and full JVM suites**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*TutorialInputGateTest'
.\gradlew.bat testDebugUnitTest
```

Expected: both commands report `BUILD SUCCESSFUL` with zero failures.

- [x] **Step 5: Commit the pure input contract**

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialInputGate.kt app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt
git commit -m "Model direct-play tutorial taps"
```

---

### Task 2: Android routing and centered popup geometry

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayout.kt`
- Create: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayoutTest.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`

**Interfaces:**
- Consumes: Task 1 `TutorialInputGate.onPointer` and `TutorialGateOutcome`.
- Produces: `TutorialCardLayout.centeredHorizontalBounds(cardLeft, cardRight, padding)`; safe explicit outside-tap startup; shared `tutorialCardBounds` and centered button geometry.

- [x] **Step 1: Write a failing geometry regression**

Create `TutorialCardLayoutTest.kt`:

```kotlin
class TutorialCardLayoutTest {
    @Test
    fun actionUsesEqualCardMarginsAndSharesTheCardCenter() {
        val bounds = TutorialCardLayout.centeredHorizontalBounds(40f, 620f, 14f)
        assertEquals(54f, bounds.left, 0.001f)
        assertEquals(606f, bounds.right, 0.001f)
        assertEquals(330f, bounds.center, 0.001f)
    }
}
```

- [x] **Step 2: Run and verify RED**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*TutorialCardLayoutTest'
```

Expected: compilation fails because `TutorialCardLayout` does not exist.

- [x] **Step 3: Implement the pure horizontal layout helper**

Create `TutorialCardLayout.kt`:

```kotlin
data class TutorialHorizontalBounds(
    val left: Float,
    val right: Float
) {
    val center: Float get() = (left + right) * 0.5f
}

object TutorialCardLayout {
    fun centeredHorizontalBounds(
        cardLeft: Float,
        cardRight: Float,
        padding: Float
    ): TutorialHorizontalBounds = TutorialHorizontalBounds(
        left = cardLeft + padding,
        right = cardRight - padding
    )
}
```

- [x] **Step 4: Add Android gesture state and shared hit rectangles**

In `ChaosGameView`:

```kotlin
private val tutorialCardBounds = RectF()
private val tutorialTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
private var tutorialDownX = 0f
private var tutorialDownY = 0f
private var tutorialMovedBeyondSlop = false
```

On `ACTION_DOWN`, store coordinates and reset the slop flag. On `ACTION_MOVE`, set the flag when squared distance from the original down point exceeds `tutorialTouchSlop * tutorialTouchSlop`. Reset all gesture state with the gate.

Classify targets with this precedence:

```kotlin
private fun tutorialTouchTarget(x: Float, y: Float): TutorialTouchTarget = when {
    tutorialStartButton.contains(x, y) -> TutorialTouchTarget.ACTION_BUTTON
    tutorialCardBounds.contains(x, y) -> TutorialTouchTarget.CARD
    else -> TutorialTouchTarget.PLAYFIELD
}
```

If the card bounds have not been laid out yet, consume the event as `CARD` rather than allowing startup.

- [x] **Step 5: Make outcomes explicit and persistence authoritative**

Change dismissal to return success:

```kotlin
private fun dismissTutorialCard(): Boolean {
    if (!prefs.edit()
            .putBoolean(tutorialAcknowledgementKey(), true)
            .commit()
    ) return false
    tutorialCardVisible = false
    tutorialCardBounds.setEmpty()
    tutorialStartButton.setEmpty()
    tutorialInputGate.reset()
    stateElapsed = 0f
    performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
    return true
}
```

In `handleTutorialTouch`, map the gate result explicitly:

```kotlin
when (result.outcome) {
    TutorialGateOutcome.NONE -> Unit
    TutorialGateOutcome.DISMISS_ONLY -> dismissTutorialCard()
    TutorialGateOutcome.DISMISS_AND_PLAY -> {
        if (dismissTutorialCard()) startRiftControl(event.x, event.y)
    }
}
```

All tutorial events return consumed. No tutorial outcome falls through to the surrounding `startRiftControl`, `moveRiftControl`, or `releaseRiftControl` branches.

- [x] **Step 6: Center the divider and action using shared card geometry**

In `drawTutorialHint`, store the card rectangle in `tutorialCardBounds`. Use `TutorialCardLayout.centeredHorizontalBounds(left, left + width, dp(14f))` for both divider and action:

```kotlin
val actionBounds = TutorialCardLayout.centeredHorizontalBounds(
    cardLeft = tutorialCardBounds.left,
    cardRight = tutorialCardBounds.right,
    padding = dp(14f)
)
canvas.drawRoundRect(
    actionBounds.left, top + dp(116f),
    actionBounds.right, top + dp(117.5f),
    dp(1f), dp(1f), paint
)
tutorialStartButton.set(
    actionBounds.left, top + dp(126f),
    actionBounds.right, top + dp(170f)
)
```

Fit the translated action label with `drawFittedText` using the button center and `tutorialStartButton.width() - dp(20f)`.

- [x] **Step 7: Add Arabic alignment inside the card**

Use `KavvoroI18n.active(context) == KavvoroLanguage.AR` to choose `Paint.Align.RIGHT`. Keep the same maximum width and draw header/body/footer at `tutorialCardBounds.right - dp(14f)` in Arabic, and at `left + dp(62f)` in other languages. The centered action label remains unchanged.

- [x] **Step 8: Compile, run tests, inspect the diff, and commit**

```powershell
.\gradlew.bat testDebugUnitTest compileDebugKotlin
git diff --check
git diff -- app/src/main/java/com/moonsolstudios/kavvoro/ui app/src/test/java/com/moonsolstudios/kavvoro/ui
```

Expected: build succeeds; the button center equals the card center; only an explicit `DISMISS_AND_PLAY` branch invokes startup while the card is visible.

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayout.kt app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayoutTest.kt app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt
git commit -m "Start play from clean tutorial taps"
```

---

### Task 3: Strict all-language tutorial catalog

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/TutorialCopy.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18n.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt`

**Interfaces:**
- Consumes: `KavvoroLanguage`.
- Produces: `TutorialCopy.translation(language, key)`, `TutorialCopy.hasExplicitTranslation(language, key)`, and `TutorialCopy.requiredKeys`.

- [x] **Step 1: Add failing strict-coverage tests**

Replace the single `START LEVEL` test with:

```kotlin
@Test
fun everyTutorialKeyHasAnExplicitTranslationInEveryLanguage() {
    KavvoroLanguage.entries
        .filterNot { it == KavvoroLanguage.SYSTEM }
        .forEach { language ->
            TutorialCopy.requiredKeys.forEach { key ->
                assertTrue(
                    "${language.code}: $key",
                    TutorialCopy.hasExplicitTranslation(language, key)
                )
                assertTrue(
                    "${language.code}: $key is blank",
                    TutorialCopy.translation(language, key).orEmpty().isNotBlank()
                )
            }
        }
}

@Test
fun tutorialLookupUsesTheStrictCatalogBeforeLegacyOverrides() {
    assertEquals(
        "ابدأ المستوى",
        KavvoroI18n.t(KavvoroLanguage.AR, "START LEVEL")
    )
    assertNotEquals(
        "Pink crash nodes end the run.",
        KavvoroI18n.t(KavvoroLanguage.JA, "Pink crash nodes end the run.")
    )
}
```

- [x] **Step 2: Run and verify RED**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*KavvoroI18nTest'
```

Expected: compilation fails because `TutorialCopy` does not exist.

- [x] **Step 3: Create a required-row API with no default languages**

In `TutorialCopy.kt`, define a helper whose 19 values are mandatory:

```kotlin
private fun requiredRow(
    en: String,
    ro: String,
    es: String,
    fr: String,
    de: String,
    it: String,
    pt: String,
    nl: String,
    pl: String,
    tr: String,
    ru: String,
    uk: String,
    ar: String,
    hi: String,
    id: String,
    vi: String,
    ja: String,
    ko: String,
    zh: String
): Pair<String, Map<KavvoroLanguage, String>>
```

Store English explicitly in the returned map. Resolve `SYSTEM` to English for direct tests, but exclude `SYSTEM` from explicit-coverage requirements.

- [x] **Step 4: Populate the exact tutorial key inventory**

Create explicit required rows for these groups; no row may use English defaults for another language:

```text
Chrome and HUD:
TIME; CHAIN; RIFT ENERGY; TRAINING; RIFT MODULE;
NO ADS IN TRAINING; L10 UNLOCKS VORO GRAD; TRAINING REWARD READY;
START LEVEL

Field labels:
TAP; SHORT TAP; SLOW TAP; TAP BURST; POWER TAP; BOOST; CRASH;
AVOID; WALL; BOUNCE WALL; EXIT; TINY EXIT; GLIDE; PORTAL;
PORTAL IN; PORTAL OUT

Tutorial level titles:
RIFT TOUCH; ORBIT CURVE; BRAKE & COAST; HAZARD DODGE;
PULSE CHAIN; RIFT DRAIN; PULSE GUARD; FOCUS HEAVY;
POWER MOON; WIND CONTROL; CHAOS TOUCH; CHAOS ORBIT;
CHAOS COAST; CRASH DODGE; RIFT COMBO; WIND OVERHEAT

Portal lesson:
Portal IN teleports the ball to OUT.
The exit launches with extra speed toward goal.
Aim before entering; it has a short cooldown.

Levels 1-10 lesson lines:
Tap to fire a short Rift tether.
The ball accelerates toward the tap point.
Chain clean taps to steer without wasting energy.
Pulse zones are not decoration.
They push and swirl the ball inside the circle.
BOOST means the field is affecting you.
Tap behind the ball to brake.
Wait between taps to coast and save rift energy.
Less rift used gives more HYPE.
Pink crash nodes end the run.
Short tap bursts dodge better than panic spam.
Clean dodges keep your streak alive.
CHAIN is your live combo.
It grows during fast rift control or boost fields.
Max chain adds big HYPE at finish.
Rift energy is limited.
Rift Drain spends energy faster during tap bursts.
Pause between taps to recharge.
Pulse Storm makes fields stronger.
Tap through the pulse when it gets wild.
Use the storm for speed, not panic.
Focus Field slows the ball during tap bursts.
Heavy Core pulls down harder.
Use precision taps to fight gravity.
Power Tap charges a stronger pull.
Moon Glide keeps momentum after release.
Tap, glide, then coast into the exit.
Wind pushes the ball sideways.
Overheat punishes tap spam.
Use short bursts for the tiny gate.

Obstacle lines:
Obstacle: portals change position and speed instantly.
Obstacle: pink crash nodes instantly fail the run.
Obstacle: tiny gate makes the exit much smaller.
Obstacle: platforms bounce you; pulse fields bend speed.
Obstacle: platforms bounce and redirect the ball.
Obstacle: screen edges and timer can still end the run.
```

Translations must be natural, concise mobile UI copy in every supported language. Preserve only the approved game tokens from Global Constraints; translate surrounding grammar and every instructional verb.

- [x] **Step 5: Route tutorial lookup before legacy fallback**

At the start of `KavvoroI18n.t(language, english)`:

```kotlin
TutorialCopy.translation(language, english)?.let { return it }
```

Then retain existing `copyOverrides` and `phrases` behavior for non-tutorial strings.

- [x] **Step 6: Verify focused and full coverage, then commit**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*KavvoroI18nTest'
.\gradlew.bat testDebugUnitTest
git diff --check
```

Expected: every required key has 19 explicit nonblank values and both commands succeed.

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/i18n/TutorialCopy.kt app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18n.kt app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt
git commit -m "Translate the complete tutorial screen"
```

---

### Task 4: Tutorial selector inventory and RTL rendering verification

**Files:**
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/TutorialCopy.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt`

**Interfaces:**
- Consumes: `TutorialCopy.requiredKeys` and all string selectors used by `ChaosGameView`.
- Produces: `TutorialCopy.renderedKeyInventory` and an automated equality test against `requiredKeys`.

- [x] **Step 1: Add a failing inventory equality test**

```kotlin
@Test
fun strictCatalogExactlyCoversTheRenderedTutorialInventory() {
    assertEquals(
        TutorialCopy.renderedKeyInventory,
        TutorialCopy.requiredKeys
    )
}
```

Define `renderedKeyInventory` as the union of named public sets: `chromeKeys`, `fieldLabelKeys`, `levelTitleKeys`, `lessonKeys`, and `obstacleKeys`. The map rows must derive `requiredKeys` from their keys. This catches a selector key added without a row and a stale row no longer used by the tutorial.

- [x] **Step 2: Run and verify RED**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*KavvoroI18nTest.strictCatalogExactlyCoversTheRenderedTutorialInventory'
```

Expected: compilation fails until the public inventory sets exist.

- [x] **Step 3: Centralize selector keys without changing lesson selection**

Expose the five immutable key sets from `TutorialCopy`, use their exact strings in the existing `tutorialLessonLines`, `tutorialObstacleLine`, title renderer, action label, footer, HUD, and coach-label code, and keep all calls passing through `t(key)`.

Do not change level mechanics, level-to-lesson mapping, obstacle priority, or the selected icon.

- [x] **Step 4: Run full automated verification and commit**

```powershell
.\gradlew.bat testDebugUnitTest compileDebugKotlin
git diff --check
rg -n '"(Tap|Pulse|Pink|Obstacle:|Wind|Overheat|START LEVEL|TRAINING|RIFT ENERGY|BOUNCE WALL|PORTAL IN|PORTAL OUT)' app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt
```

Expected: tests/build succeed; every visible tutorial literal found by the audit is passed through `t(...)` and belongs to the strict inventory.

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt app/src/main/java/com/moonsolstudios/kavvoro/i18n/TutorialCopy.kt app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt
git commit -m "Enforce tutorial translation coverage"
```

---

### Task 5: Android QA, review, and direct publication

**Files:**
- Local-only input: `C:\Users\Alin\Downloads\google-services.json`
- Build output: `app/build/outputs/apk/debug/app-debug.apk`
- Modify: `docs/superpowers/plans/2026-08-12-tutorial-direct-play-localization.md` (checkbox tracking only)

**Interfaces:**
- Consumes: the complete feature.
- Produces: verified APK, clean `main`, independent review, and synchronized `origin/main`.

- [x] **Step 1: Verify Firebase config remains valid and ignored**

```powershell
$firebase = Get-Content -Raw 'C:\Users\Alin\Downloads\google-services.json' | ConvertFrom-Json
$packages = $firebase.client.client_info.android_client_info.package_name
if ('com.moonsolstudios.kavvoro' -notin $packages) {
    throw 'Downloaded google-services.json does not contain Kavvoro.'
}
Copy-Item -LiteralPath 'C:\Users\Alin\Downloads\google-services.json' -Destination 'app\google-services.json'
git check-ignore -v app/google-services.json
```

Expected: `.gitignore` owns `app/google-services.json`, and it is absent from `git status`.

- [x] **Step 2: Run a clean build**

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\Alin\AppData\Local\Android\Sdk'
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

Expected: `BUILD SUCCESSFUL`; APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

- [x] **Step 3: Install and verify the touch matrix on the emulator**

```powershell
$adb='C:\Users\Alin\AppData\Local\Android\Sdk\platform-tools\adb.exe'
& $adb devices
& $adb install -r 'app\build\outputs\apk\debug\app-debug.apk'
& $adb shell am force-stop com.moonsolstudios.kavvoro
& $adb shell am start -n com.moonsolstudios.kavvoro/.MainActivity
```

At fresh unacknowledged lessons, verify with screenshots and timer/ball position:

1. card-body tap leaves card open and timer unchanged;
2. playfield swipe and bottom-edge system swipe leave card open and timer unchanged;
3. drag from playfield into card leaves card open;
4. multi-touch leaves card open;
5. clean playfield tap closes card and starts the ball from that tap;
6. `START LEVEL` on another fresh lesson closes only the card, with timer and ball unchanged;
7. retry does not reopen an acknowledged card;
8. Classic acknowledgement does not suppress the same Chaos lesson.

- [x] **Step 4: Verify representative scripts and centered layout**

Inspect English, Romanian, German, Arabic, Hindi, Japanese, and Chinese. For each:

- no English instructional sentence remains;
- header, four body lines, footer, and action fit inside the card;
- the action and divider have equal left/right margins;
- the action label center matches the card center;
- Arabic body copy is right-aligned and the action stays centered;
- coach labels and tutorial titles use the selected language.

- [x] **Step 5: Request independent code review and resolve findings**

Review the complete range from the pre-feature base through `HEAD` against `docs/superpowers/specs/2026-08-12-tutorial-direct-play-localization-design.md`. Fix every Critical or Important issue, rerun focused tests for any fix, and request a concise re-review before publication.

- [x] **Step 6: Run fresh final verification**

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
git diff --check
git status --short --branch
```

Expected: build succeeds, zero failed tests, no whitespace errors, and only this plan's checkbox tracking may remain.

- [x] **Step 7: Complete tracking, commit, and push direct to `main`**

Mark all completed checkboxes, then:

```powershell
git add -- docs/superpowers/plans/2026-08-12-tutorial-direct-play-localization.md
git commit -m "Complete localized tutorial interaction rollout"
git fetch origin main
git rev-list --left-right --count origin/main...main
git push origin main
git fetch origin main
git rev-list --left-right --count origin/main...main
git status --short --branch
```

Before push, expected divergence is `0 <positive>`. After push, expected divergence is `0 0` and status is `main...origin/main` with no changed files.
