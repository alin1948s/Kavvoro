# Kavvoro Release Polish Roadmap Implementation Plan

> Historical implementation plan. Retained for decision history; current code,
> release checklists, and the repository README are authoritative.

**Goal:** Complete the four agreed polish stages in sequence: observability, audio polish, store-package verification, and minimal leaderboard integrity hardening.

**Architecture:** Each stage remains isolated and produces a separately verifiable result. Firebase observability uses one app-owned telemetry wrapper; audio polish stays inside `KavvoroSoundEngine`; store readiness is validated from the existing `store-assets/final` package; leaderboard hardening adds client-side sanity checks without introducing a backend yet.

**Tech Stack:** Kotlin, Android API 36, Firebase Analytics/Crashlytics, `MediaPlayer`, `SoundPool`, Google Play Games Services, Gradle, Python/Pillow/ffprobe for asset validation.

**Spec:** User-approved requirements captured in the current task conversation; no separate product spec is required for this staged roadmap.

## Global Constraints

- Keep `compileSdk = 36`, `targetSdk = 36`, and `minSdk = 24`.
- Preserve the existing Brainball artwork, localized voice assets, selection fade, and README history.
- Do not implement challenge import/replay, release-upload work, or Billing in these stages.
- Keep the existing separate Music and SFX mute toggles; do not add volume sliders unless a later review shows they are needed.
- Complete and verify one stage before starting the next; do not commit automatically.

---

### Task 1: Tester QA — localization, training overlay, and gameplay HUD

**Evidence covered:**
- Tester 1, Galaxy S20, Android 13, `pl-PL`: mixed English/Polish screens, Polish strings truncated with ellipses, and inconsistent decimal separators.
- Tester 2: training card controls could feel like gameplay input, with no obvious dismiss-only path before the ball was launched. **Fixed:** the card now has a `GOT IT` dismiss-only action, and a separate later playfield gesture is required to start Rift control.
- Attached tester screenshot: the `RIFT ENERGY` label/bar sits too close to the `CLASSIC - L03 BRAKE & COAST` level title panel.

**Current status:** Task 1 is complete in the debug APK. Tester 2 is fixed and verified; Tester 1's Polish menu/game/result fallback strings, locale-aware decimal formatting, and HUD spacing were fixed and rechecked on the emulator.

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroNumberFormat.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18n.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/TutorialCopy.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialInputGate.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayout.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroNumberFormatTest.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialInputGateTest.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayoutTest.kt`

**Interfaces:**
- `KavvoroNumberFormat.seconds(value: Float, locale: Locale): String` is the single formatter used by both live HUD and result cards.
- `TutorialInputGate` consumes every touch that begins inside the training card; card actions dismiss the card without starting Rift control, and only a later deliberate playfield tap starts the run.
- `TutorialCardLayout` exposes safe vertical bounds for the training card and gameplay HUD so the energy band and level title cannot overlap or sit below the minimum visual gap.

**Acceptance criteria:**
- On `pl-PL`, Age Check, menu, tutorial, HUD, and result screens do not mix English fallback strings into otherwise Polish copy for the visible tester-reported keys.
- High-priority Polish labels do not render as ellipses in the menu, selected Brainball card, daily reward, level-start card, or result card; long copy wraps or uses an intentional short translation.
- The same locale-aware decimal output is used in the live HUD and result card, for example `8,9s` on Polish and `8.9s` on English.
- The training card has an obvious `GOT IT` dismiss-only action, and tapping the card or its action cannot also call `startRiftControl`.
- The energy bar and `CLASSIC - L03 BRAKE & COAST` panel have at least `dp(12f)` of clear vertical separation on the tester phone layout and remain separated on larger layouts.

- [x] **Step 1: Write failing tests for Polish formatting, training input, and HUD bounds**

  Add assertions for Polish decimal commas, the tester-reported visible translation keys, dismiss-only training actions, no same-pointer gameplay start, and non-overlapping energy/title bounds.

- [x] **Step 2: Run the focused tests and confirm the expected failures**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*KavvoroNumberFormatTest' --tests '*KavvoroI18nTest' --tests '*TutorialInputGateTest' --tests '*TutorialCardLayoutTest'
  ```

- [x] **Step 3: Implement locale completeness and the shared number formatter**

  Complete the Polish visible-copy rows first, add the formatter, and replace direct `"%.1f".format(...)` calls in the HUD/result path with `KavvoroNumberFormat.seconds(...)`.

- [x] **Step 4: Fix training input semantics and add an explicit dismiss-only path**

  Completed: card touches remain consumed, `GOT IT` closes without starting play, the gesture resets after dismissal, and a new playfield gesture is required before `startRiftControl` can run.

- [x] **Step 5: Fix localized text layout and HUD spacing**

  Use measured wrapping or intentional short localized labels for the affected cards, then reserve a separate HUD band for `RIFT ENERGY` and the level title instead of relying on English-sized text fitting.

- [x] **Step 6: Run all unit tests and perform the two tester regressions**

  Completed: full unit tests and debug build pass; emulator regression covers the Polish menu, Classic gameplay HUD, locale-aware `11,5s` live time, `2,3s` result time, localized interrupted-run card, and the separated energy/title band. The earlier `GOT IT` tester flow remains verified.

---

### Task 2: Firebase observability and crash reporting

**Current status:** Integrated and verified in the debug APK. Firebase Console now reports the package as installed and shows the debug-only Crashlytics verification issue; production tuning/Remote Config remains a later milestone.

**Files:**
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/telemetry/KavvoroTelemetry.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/telemetry/KavvoroTelemetryTest.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/MainActivity.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`

**Interfaces:**
- `KavvoroTelemetry.logEvent(name: Event, params: Map<String, String>)` accepts only the allowlisted event names and non-PII parameters.
- `KavvoroTelemetry.recordNonFatal(error: Throwable, context: String)` forwards non-fatal failures to Crashlytics.
- Events are `app_open`, `age_gate_completed`, `tutorial_level_completed`, `run_started`, `run_finished`, `run_failed`, `brainball_selected`, `brainball_unlocked`, `replay_shared`, and `purchase_restore_tapped`.

- [x] **Step 1: Write the failing telemetry contract test**

  Assert that the event allowlist contains the ten agreed event names, rejects unknown names, and never accepts date of birth, email, account ID, or raw age as a parameter.

- [x] **Step 2: Run the focused test and confirm the expected failure**

  Run:

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*KavvoroTelemetryTest'
  ```

  Expected result: failure because the telemetry contract and wrapper do not yet exist.

- [x] **Step 3: Add the Crashlytics plugin, dependency, and telemetry wrapper**

  Use the Firebase BoM already present, add `com.google.firebase.crashlytics` to the root and app plugins, add `firebase-crashlytics` beside Analytics, and route all calls through `KavvoroTelemetry`.

- [x] **Step 4: Instrument only the agreed lifecycle and gameplay events**

  Use stable enum names and bounded values such as mode, level, language, result, and Brainball slug; never send entered birth date or free-form user text.

- [x] **Step 5: Run the focused test, all unit tests, and a debug build**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*KavvoroTelemetryTest'
  .\gradlew.bat testDebugUnitTest assembleDebug
  ```

  Completed: tests pass, APK builds, Firebase initializes the package, and Crashlytics Console shows the forced debug issue `Kavvoro debug Crashlytics verification` for version `0.1.0-debug`. No release build is configured to trigger this path.

---

### Task 3: Audio polish without new user-facing controls

**Current status:** Implemented in the debug APK. Music track changes now use a bounded two-player crossfade, while Music and SFX mute paths remain independent; focused/full unit tests and the debug build pass. Final speaker/headphone listening remains a release QA checklist item.

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/audio/MusicTransition.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/audio/KavvoroSoundEngine.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/audio/MusicTransitionTest.kt`
- Modify: `docs/release-polish-checklist.md`

**Interfaces:**
- `MusicTransition.steps(startVolume: Float, endVolume: Float, durationMs: Long)` returns deterministic volume steps for a short crossfade.
- The sound engine keeps separate Music/SFX mute behavior and uses a bounded crossfade when switching Menu, Tutorial, Classic, or Chaos tracks.

- [x] **Step 1: Write the failing crossfade policy test**

  Assert monotonic fade-out/fade-in volumes, a finite transition duration, and no volume outside `0f..1f`.

- [x] **Step 2: Run the focused test and confirm the expected failure**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*MusicTransitionTest'
  ```

- [x] **Step 3: Implement two-player MediaPlayer crossfade behavior**

  Keep the current track alive while the next track ramps up, ramp the old track down over the policy duration, release the old player at zero, and cancel pending callbacks on pause, mute, release, and repeated track changes.

- [x] **Step 4: Verify the existing mute toggles remain authoritative**

  The Music mute/pause paths cancel pending crossfade callbacks, restore the active player volume, and pause/release both players; the existing SFX mute path still stops selection previews immediately.

- [x] **Step 5: Run unit tests, build, and update the audio checklist**

  Focused and full unit tests pass, the debug APK builds, and the checklist now includes phone speaker/headphones, rapid Classic/Chaos switching, app pause/resume, and selection voice balance.

---

### Task 4: Store-package verification and documentation

**Current status:** Store package validated. The seven phone screenshots, core graphics, videos, and thumbnails pass the read-only validator; dedicated 7-inch (1200×1920) and 10-inch (1600×2560) tablet sets were captured and visually reviewed from the current APK. On 2026-08-22, all seven 7-inch and all seven 10-inch screenshots were uploaded to the default Google Play listing and saved as a draft. The review screen reports the icon, feature graphic, phone/tablet screenshots, videos, and Play Games on PC package without validation errors. Chromebook and Android XR assets remain intentionally empty and optional. A release APK also builds successfully; perform the final signed-release visual recheck if release-only assets change before publishing the draft.

**Files:**
- Create: `tools/verify_store_assets.py`
- Test: `tools/test_store_assets.py`
- Create: `store-assets/final/tablet-7/*.png` after the large-screen UI pass
- Create: `store-assets/final/tablet-10/*.png` after the large-screen UI pass
- Delete: `store-assets/final/tablet-7/NOT_READY.md` and `store-assets/final/tablet-10/NOT_READY.md` after truthful tablet captures exist
- Modify: `store-assets/final/UPLOAD_GUIDE.md` only if the verification finds a mismatch
- Modify: `README.md` only to record the verified phone-only store status

**Interfaces:**
- `verify_store_assets.py` validates the existing final package without generating or editing artwork.
- The validator checks the 512×512 icon, 1024×500 feature graphic, seven 1242×2208 phone screenshots, both trailer files, the 1280×720 thumbnail, and seven 1200×1920 / seven 1600×2560 tablet screenshots.

- [x] **Step 1: Write the failing asset-inventory test**

  Assert that every required path exists, each raster asset has the documented dimensions, and the phone screenshot set contains exactly seven numbered files.

- [x] **Step 2: Run the focused test and confirm the expected failure**

  ```powershell
  python -m unittest tools.test_store_assets
  ```

- [x] **Step 3: Implement the read-only validator**

  Use Pillow for raster dimensions and `ffprobe` for video existence/duration; fail with the exact missing path or mismatched dimension.

- [x] **Step 4: Run the large-screen review, capture tablet assets, and reconcile documentation**

  Large-screen review passed on dedicated 1200×1920 and 1600×2560 portrait emulator viewports. The seven-image sets were captured in English, reviewed visually, and are dedicated tablet renders rather than upscaled phone screenshots.

- [x] **Step 5: Run the validator again and record the result in README**

  Validator result: seven phone screenshots, core media, seven 7-inch screenshots, and seven 10-inch screenshots pass. README and the upload guide record the tablet package. Both tablet sets are now present in the Google Play default-listing draft at 7/8 assets each; publishing/review remains a separate release action.

---

### Task 5: Minimal leaderboard integrity hardening

**Current status:** Implemented and verified. Leaderboard submits now pass a conservative local guard for completed positive progress only; full server-side anti-cheat remains intentionally deferred.

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/ui/LeaderboardScoreGuard.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/ui/LeaderboardScoreGuardTest.kt`
- Modify: `docs/play-console-compliance.md`

**Interfaces:**
- `LeaderboardScoreGuard.isSubmitAllowed(board: LeaderboardBoard, score: Int, completedRun: Boolean, currentProgress: Int): Boolean` rejects negative scores, incomplete runs, and values above the locally completed progress.
- `syncLeaderboards()` submits only scores accepted by the guard; local records remain available when Play Games is not configured.

- [x] **Step 1: Write failing guard tests**

  Cover negative scores, incomplete runs, values above current progress, and valid Classic/Chaos level/streak submissions.

- [x] **Step 2: Run the focused test and confirm the expected failure**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*LeaderboardScoreGuardTest'
  ```

- [x] **Step 3: Implement the pure guard and connect it to leaderboard sync**

  Keep the guard conservative: it prevents accidental or obviously impossible submissions but does not claim to be server-side anti-cheat.

- [x] **Step 4: Document the Play Console action**

  Record that new Play Games leaderboards must have tamper protection enabled and that full Play Integrity/backend validation is deferred until leaderboards become materially competitive.

- [x] **Step 5: Run all unit tests and assemble the APK**

  ```powershell
  .\gradlew.bat testDebugUnitTest assembleDebug
  ```

  Stage exit condition met: invalid local submissions are blocked, valid progress remains eligible, and the client-side limitation is documented honestly.

---

## Execution order

1. Tester QA: localization, training overlay, HUD spacing, and number formatting.
2. Firebase observability and Crashlytics.
3. Audio crossfade and mix verification.
4. Store asset validator and phone-listing documentation.
5. Minimal leaderboard score guard.

After each task, pause for review and only continue when its verification commands pass.
