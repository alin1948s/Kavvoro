# Android startup and Home ANR investigation — 2026-08-30

## Confirmed root cause

The captured failures are foreground input-dispatch ANRs. Android waited about
five seconds for `MainActivity` to process focus or touch input while its main
thread was busy initializing Google Mobile Ads and the WebView runtime.

Direct evidence from the captured device traces:

- `MobileAds.initialize` → `PrivacyAdsController.initializeAdsIfAllowed` →
  `MainActivity.startGame` was on the `main` thread when the focus event timed
  out.
- A second trace placed the main thread inside Chromium WebView and
  `SharedPreferences` setup during a motion-event timeout.
- The failing emulator was under 98% total CPU load; the app used 86% CPU,
  mostly in kernel work, while WebView/media/SoundPool workers were active.

Raw `adb`/ANR dumps are intentionally not versioned because they are transient,
device-specific diagnostics. This document preserves the relevant stacks,
measurements, interpretation, and validated fix.

The earlier eager SoundPool preload and large Home bitmap decode increased CPU
and memory pressure, but neither explains the captured main-thread stack by
itself. Audio is now loaded on demand and Home rendering remains on the game
thread.

## Production fix

- `ChaosGameView` reports its first successfully posted Surface frame.
- UMP/Ads and Play Billing are started only after that frame, outside the launch
  critical path.
- Mobile Ads initialization uses the application context on a dedicated
  background-priority executor.
- Late consent/Ads callbacks are lifecycle guarded after Activity destruction.
- Billing cannot reconnect from `onResume` before its deferred start.
- The redundant manual Play Games initialization was removed. The dependency's
  merged-manifest `PlayGamesInitProvider` already initializes the SDK.
- `Activity.reportFullyDrawn()` now marks the real first game frame for Android
  vitals.

## Equivalent Pixel Tablet emulator validation

Debug APK, Android 15 `sdk_gtablet_x86_64`, three force-stop cold starts:

| Metric | Checkpoint baseline | Fixed build |
| --- | ---: | ---: |
| Displayed, run 1 | 3370 ms | 1462 ms |
| Displayed, run 2 | 2355 ms | 1474 ms |
| Displayed, run 3 | 1779 ms | 1553 ms |
| Median displayed | 2355 ms | 1474 ms |
| Fully drawn | Not reported | 1816–1951 ms |
| New ANR / fatal exception | 0 | 0 |

The fixed median is about 38% lower. Post-settle memory was approximately
190 MB PSS / 401 MB RSS. Ads still creates four WebViews, so memory should be
checked on a low-RAM physical device before release.

## Home log classification

The remaining warnings/errors reproduced after `Fully drawn` are platform or
SDK diagnostics rather than Home renderer failures:

- `adservices ... Service is not available`: Android 15 emulator does not
  expose the measurement service.
- Chromium variation seed/signature and codec warnings: emulator WebView/media
  environment.
- EGL/HWUI configuration warnings: emulator graphics stack fallback.
- Billing API/reconnection warnings: Play Store emulator has no signed-in
  account.

No `AndroidRuntime` fatal exception, `ChaosGameView` game-loop exception, bitmap
decode failure, or new ANR was observed. Do not add unrelated permissions or
suppress these SDK logs; validate Billing/Ads once more on a Play-enabled,
signed-in physical device.

## Authoritative guidance

- Android ANR diagnosis: https://developer.android.com/topic/performance/anrs/diagnose-and-fix-anrs
- Google Mobile Ads startup optimization: https://developers.google.com/admob/android/optimize-initialization
- Google Mobile Ads quick start: https://developers.google.com/admob/android/quick-start
- Play Games SDK initialization provider: https://developers.google.com/android/reference/com/google/android/gms/games/PlayGamesSdk
