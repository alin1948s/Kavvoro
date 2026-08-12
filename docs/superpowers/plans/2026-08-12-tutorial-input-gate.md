# Tutorial Input Gate Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent every visible training lesson from launching the ball, replace misleading chips with one explicit `START LEVEL` action, and remember acknowledgement per mode and level.

**Architecture:** Put pointer semantics and visibility/key rules in an Android-independent `TutorialInputGate`, covered by JVM tests. `ChaosGameView` adapts `MotionEvent`, preserves HUD precedence, renders the action, persists acknowledgement, and prevents the dismissal event from reaching Rift control.

**Tech Stack:** Kotlin/JVM 17, Android `SurfaceView`/`Canvas`, `SharedPreferences`, JUnit 4, Gradle Android plugin.

## Global Constraints

- Work directly on `main` in `C:\Users\Alin\Desktop\MoonsolStudios\Kavvoro`; do not create a branch, worktree, or pull request.
- Preserve HUD controls while the lesson is visible.
- `START LEVEL` dismisses only; it never calls `beginLiveRun`, `startRiftControl`, or `physics.setRiftControl`.
- Consume `DOWN`, `MOVE`, `UP`, and `CANCEL` for non-HUD lesson gestures.
- Persist acknowledgement as mode plus level; restarting progress does not clear it.
- Apply the safe gate to every full tutorial card, including later Rift Module lessons.
- Localize `START LEVEL` in all 19 supported non-system languages plus English.
- Never commit `app/google-services.json`, signing files, APKs, or AABs.

---

### Task 1: Pure tutorial input policy

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialInputGate.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt`

**Interfaces:**
- Consumes: normalized pointer actions and primitive visibility facts.
- Produces: `TutorialPointerAction`, `TutorialGateResult`, and `TutorialInputGate.onPointer`, `reset`, `actionPressed`, `acknowledgementKey`, and `shouldShow`.

- [x] **Step 1: Write the failing tests**

Create `TutorialInputGateTest.kt`:

```kotlin
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
        assertNotEquals(classic3, TutorialInputGate.acknowledgementKey("CHAOS", 3))
        assertNotEquals(classic3, TutorialInputGate.acknowledgementKey("CLASSIC", 4))
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
```

- [x] **Step 2: Run the focused test and verify RED**

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest --tests '*TutorialInputGateTest'
```

Expected: compilation fails because the three tutorial input types do not exist.

- [x] **Step 3: Implement the minimal state machine**

Create `TutorialInputGate.kt`:

```kotlin
package com.moonsolstudios.kavvoro.ui

enum class TutorialPointerAction { DOWN, MOVE, UP, CANCEL }

data class TutorialGateResult(val consumed: Boolean, val dismissed: Boolean)

class TutorialInputGate {
    private var beganInsideAction = false
    private var leftAction = false

    val actionPressed: Boolean
        get() = beganInsideAction && !leftAction

    fun onPointer(action: TutorialPointerAction, insideAction: Boolean): TutorialGateResult =
        when (action) {
            TutorialPointerAction.DOWN -> {
                beganInsideAction = insideAction
                leftAction = false
                TutorialGateResult(true, false)
            }
            TutorialPointerAction.MOVE -> {
                if (beganInsideAction && !insideAction) leftAction = true
                TutorialGateResult(true, false)
            }
            TutorialPointerAction.UP -> {
                val dismissed = beganInsideAction && insideAction && !leftAction
                reset()
                TutorialGateResult(true, dismissed)
            }
            TutorialPointerAction.CANCEL -> {
                reset()
                TutorialGateResult(true, false)
            }
        }

    fun reset() {
        beganInsideAction = false
        leftAction = false
    }

    companion object {
        fun acknowledgementKey(modeName: String, level: Int): String =
            "tutorial_ack_${modeName.lowercase()}_$level"

        fun shouldShow(
            gameScreen: Boolean,
            ready: Boolean,
            hasTutorialHint: Boolean,
            acknowledged: Boolean
        ): Boolean = gameScreen && ready && hasTutorialHint && !acknowledged
    }
}
```

- [x] **Step 4: Verify GREEN and the full JVM suite**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*TutorialInputGateTest'
.\gradlew.bat testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` twice with zero failed tests.

- [x] **Step 5: Commit**

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialInputGate.kt app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt
git commit -m "Add deterministic tutorial input gate"
```

---

### Task 2: Localized explicit action

**Files:**
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18n.kt`
- Create: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt`

**Interfaces:**
- Consumes: `KavvoroI18n.t(KavvoroLanguage, String)`.
- Produces: a translated `START LEVEL` value for every supported language.

- [x] **Step 1: Write the failing coverage test**

```kotlin
package com.moonsolstudios.kavvoro.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class KavvoroI18nTest {
    @Test
    fun startLevelIsTranslatedForEverySupportedLanguage() {
        assertEquals("START LEVEL", KavvoroI18n.t(KavvoroLanguage.EN, "START LEVEL"))
        KavvoroLanguage.entries
            .filterNot { it == KavvoroLanguage.SYSTEM || it == KavvoroLanguage.EN }
            .forEach { language ->
                assertNotEquals(language.code, "START LEVEL", KavvoroI18n.t(language, "START LEVEL"))
            }
    }
}
```

- [x] **Step 2: Run and verify RED**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*KavvoroI18nTest'
```

Expected: failure for Romanian because the value is still `START LEVEL`.

- [x] **Step 3: Add all translations beside the existing `START` row**

```kotlin
row(
    "START LEVEL", "Începe nivelul", "Iniciar nivel", "Commencer le niveau",
    "Level starten", "Avvia livello", "Iniciar nível", "Level starten",
    "Rozpocznij poziom", "Seviyeyi başlat", "Начать уровень",
    "Почати рівень", "ابدأ المستوى", "लेवल शुरू करें", "Mulai level",
    "Bắt đầu cấp độ", "レベル開始", "레벨 시작", "开始关卡"
),
```

- [x] **Step 4: Verify GREEN and all JVM tests**

```powershell
.\gradlew.bat testDebugUnitTest --tests '*KavvoroI18nTest'
.\gradlew.bat testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` twice with zero failed tests.

- [x] **Step 5: Commit**

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18n.kt app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt
git commit -m "Localize tutorial start action"
```

---

### Task 3: Wire the gate into `ChaosGameView`

**Files:**
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt`

**Interfaces:**
- Consumes: all `TutorialInputGate` interfaces and localized `START LEVEL`.
- Produces: safe touch routing, persistent dismissal, a single real action, and no decorative chips.

- [x] **Step 1: Add and run a policy regression for later lessons**

Add to `TutorialInputGateTest.kt`:

```kotlin
@Test
fun laterFullLessonUsesTheSameVisibilityRule() {
    assertTrue(TutorialInputGate.shouldShow(
        gameScreen = true,
        ready = true,
        hasTutorialHint = true,
        acknowledged = false
    ))
}
```

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests '*TutorialInputGateTest'
```

Expected: PASS, proving integration must depend on `hasTutorialHint`, not `level <= 10`.

- [x] **Step 2: Add view state and visibility helpers**

Add fields near the existing input state and rectangles:

```kotlin
private val tutorialInputGate = TutorialInputGate()
private var tutorialCardVisible = false
private val tutorialStartButton = RectF()
```

Call `refreshTutorialCardVisibility()` at the end of `resetRound`, and add:

```kotlin
private fun tutorialAcknowledgementKey(): String =
    TutorialInputGate.acknowledgementKey(gameMode.name, level.index)

private fun refreshTutorialCardVisibility() {
    tutorialInputGate.reset()
    tutorialStartButton.setEmpty()
    tutorialCardVisible = TutorialInputGate.shouldShow(
        screen == Screen.GAME,
        state == GameState.READY,
        level.tutorialHint.isNotBlank(),
        prefs.getBoolean(tutorialAcknowledgementKey(), false)
    )
}
```

- [x] **Step 3: Consume tutorial pointer sequences before Rift control**

Add:

```kotlin
private fun handleTutorialTouch(event: MotionEvent): Boolean {
    if (!tutorialCardVisible) return false
    val action = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> TutorialPointerAction.DOWN
        MotionEvent.ACTION_MOVE -> TutorialPointerAction.MOVE
        MotionEvent.ACTION_UP -> TutorialPointerAction.UP
        MotionEvent.ACTION_CANCEL -> TutorialPointerAction.CANCEL
        else -> return true
    }
    val result = tutorialInputGate.onPointer(
        action,
        tutorialStartButton.contains(event.x, event.y)
    )
    if (result.dismissed) dismissTutorialCard()
    return result.consumed
}

private fun dismissTutorialCard() {
    if (!prefs.edit().putBoolean(tutorialAcknowledgementKey(), true).commit()) return
    tutorialCardVisible = false
    tutorialStartButton.setEmpty()
    tutorialInputGate.reset()
    stateElapsed = 0f
    performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    audio.playEvent(SoundEvent.UI_TAP, selectedBallIndex())
}
```

Replace the gameplay `when (event.actionMasked)` body with this routing. The
HUD remains first, while every unowned lesson gesture is consumed before Rift
control:

```kotlin
when (event.actionMasked) {
    MotionEvent.ACTION_DOWN -> {
        activeButton = buttonAt(event.x, event.y)
        if (activeButton == ButtonId.NONE && !handleTutorialTouch(event)) {
            startRiftControl(event.x, event.y)
        }
    }

    MotionEvent.ACTION_MOVE -> {
        if (activeButton == ButtonId.NONE &&
            !handleTutorialTouch(event) &&
            riftTapReleaseTimer <= 0f
        ) {
            moveRiftControl(event.x, event.y)
        }
    }

    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        val releasedButton = activeButton
        activeButton = ButtonId.NONE
        if (releasedButton != ButtonId.NONE &&
            buttonAt(event.x, event.y) == releasedButton
        ) {
            pendingAction = handleButton(releasedButton)
        } else if (!handleTutorialTouch(event) &&
            (event.actionMasked == MotionEvent.ACTION_CANCEL ||
                riftTapReleaseTimer <= 0f)
        ) {
            releaseRiftControl()
        }
    }
}
```

- [x] **Step 4: Replace chips with the real action**

At the start of `drawTutorialHint`:

```kotlin
if (!tutorialCardVisible) {
    tutorialStartButton.setEmpty()
    return
}
```

Increase the card height from `154dp` to `184dp`. Keep the icon, lesson, footer, and divider. Remove the `tutorialChipLabels` loop and method. Define and draw the shared action geometry:

```kotlin
tutorialStartButton.set(
    left + dp(62f), top + dp(126f),
    left + width - dp(14f), top + dp(170f)
)
drawTutorialStartButton(canvas, accent)
```

Add the focused renderer below `drawTutorialHint`:

```kotlin
private fun drawTutorialStartButton(canvas: Canvas, accent: Int) {
    val active = tutorialInputGate.actionPressed
    paint.style = Paint.Style.FILL
    paint.shader = LinearGradient(
        tutorialStartButton.left,
        tutorialStartButton.top,
        tutorialStartButton.right,
        tutorialStartButton.bottom,
        intArrayOf(
            withAlpha(accent, if (active) 255 else 232),
            withAlpha(accent, if (active) 180 else 132)
        ),
        null,
        Shader.TileMode.CLAMP
    )
    canvas.drawRoundRect(tutorialStartButton, dp(7f), dp(7f), paint)
    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = dp(if (active) 1.8f else 1.1f)
    paint.color = withAlpha(0xFFFFFFFF.toInt(), if (active) 235 else 175)
    canvas.drawRoundRect(tutorialStartButton, dp(7f), dp(7f), paint)

    textPaint.textAlign = Paint.Align.CENTER
    textPaint.typeface = android.graphics.Typeface.create(
        "sans",
        android.graphics.Typeface.BOLD
    )
    textPaint.textSize = dp(12f)
    textPaint.color = 0xFFF7F4FF.toInt()
    canvas.drawText(
        t("START LEVEL").uppercase(),
        tutorialStartButton.centerX(),
        tutorialStartButton.centerY() + dp(4f),
        textPaint
    )
}
```

- [x] **Step 5: Compile, test, inspect, and commit**

```powershell
.\gradlew.bat testDebugUnitTest compileDebugKotlin
git diff --check
git diff -- app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt
```

Expected: `BUILD SUCCESSFUL`, no whitespace errors, and the tutorial branch visibly precedes all Rift control calls.

```powershell
git add -- app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt
git commit -m "Block gameplay behind training lessons"
```

---

### Task 4: Android verification and publication

**Files:**
- Local-only input: `C:\Users\Alin\Downloads\google-services.json`
- Build output: `app/build/outputs/apk/debug/app-debug.apk`
- Modify: `docs/superpowers/plans/2026-08-12-tutorial-input-gate.md` (checkbox tracking only)

**Interfaces:**
- Consumes: the complete feature.
- Produces: device evidence, verified APK, clean `main`, and synchronized `origin/main`.

- [ ] **Step 1: Install the ignored Firebase configuration**

```powershell
$firebase = Get-Content -Raw 'C:\Users\Alin\Downloads\google-services.json' | ConvertFrom-Json
$packages = $firebase.client.client_info.android_client_info.package_name
if ('com.moonsolstudios.kavvoro' -notin $packages) {
    throw 'Downloaded google-services.json does not contain Kavvoro.'
}
Copy-Item -LiteralPath 'C:\Users\Alin\Downloads\google-services.json' -Destination 'app\google-services.json'
git check-ignore 'app/google-services.json'
```

Expected: `app/google-services.json` is ignored and absent from `git status`.

- [ ] **Step 2: Run a clean build**

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

Expected: `BUILD SUCCESSFUL`; APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 3: Install and verify the reported flow**

```powershell
$adb='C:\Users\Alin\AppData\Local\Android\Sdk\platform-tools\adb.exe'
& $adb devices
& $adb install -r 'app\build\outputs\apk\debug\app-debug.apk'
& $adb shell am force-stop com.moonsolstudios.kavvoro
& $adb shell monkey -p com.moonsolstudios.kavvoro 1
```

At an unacknowledged training level, verify and capture screenshots for:

1. taps on lesson text, old chip area, and playfield leave the ball stationary;
2. a system-edge swipe followed by cancellation leaves the card and ball unchanged;
3. dragging out of `START LEVEL` does not dismiss;
4. tapping `START LEVEL` closes only the card;
5. a separate later playfield touch starts the run;
6. retry does not reopen the acknowledged lesson;
7. the corresponding lesson in the other mode still appears;
8. Romanian and Arabic labels fit and remain centered.

- [ ] **Step 4: Run final verification**

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
git diff --check
git status --short --branch
```

Expected: tests/build pass; diff check is empty; only plan checkbox tracking may remain.

- [ ] **Step 5: Complete tracking, commit, and push `main`**

Mark completed checkboxes in this file, then run:

```powershell
git add -- 'docs/superpowers/plans/2026-08-12-tutorial-input-gate.md'
git commit -m "Complete tutorial input gate rollout"
git push origin main
git fetch origin main
git rev-list --left-right --count origin/main...main
git status --short --branch
```

Expected divergence: `0  0`; status: `main...origin/main` with no changed files.
