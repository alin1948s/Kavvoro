# Brainrot Chaos: Kavvoro

Android physics game for phones and tablets, built around short one-thumb runs,
original brainrot characters, challengeable gameplay moments, and shareable
vertical replays.

> This README is the project source of truth. Update **Current Status** and
> **Next Milestones** whenever a meaningful feature, release, or verification
> step changes.

## Current Status

- Package: `com.moonsolstudios.kavvoro`
- Target platform: Android phones and tablets, portrait-only.
- Modes: Classic Mode and Chaos Mode, each with its own progression, streak,
  tutorial sequence, and infinite level generation.
- Tutorial: the first five levels of each mode introduce the core controls,
  boosts, and curse mechanics progressively.
- Collection: 50 Brainballs total — 38 shared universe characters/assets and
  12 Kavvoro-exclusive extras — with unlock rules, superpowers, and
  premium-content hooks.
- UI: responsive phone/tablet layouts, localized interface and tutorial copy,
  animated menu, collection, leaderboards, and release-oriented privacy flow.
- Sharing: vertical 9:16 MP4 replay export through the Android share sheet,
  including the ball, control trail, tether, score, and challenge code.
- Monetization: AdMob interstitial/rewarded-ad gates with test IDs in debug
  builds, age-gated consent handling, and Google Play Billing integration hooks.
- Platform services: Google Play Games Services v2 hooks for authentication,
  leaderboards, and score submission.
- Observability: Firebase Analytics event schema and Crashlytics are integrated;
  the debug build has been verified to report a test crash for this package.
- Audio inventory: 50 Brainball selection OGGs are present and container-
  validated for each of the 19 selectable languages; see
  `docs/localized-voice-inventory.md`.
- Localization audit: strict rendered catalogs are complete for all 19
  selectable languages, with one source-controlled Kotlin file per language
  under `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/`. The
  isolated Home/Classic HUD capture is now accepted for 19/19 locales (38
  valid PNGs); the run resets the app before each language and was manually
  corrected for Arabic/Japanese/Korean selection drift. The remaining release
  gate is full review of tutorial, Collection, and result states. Arabic RTL
  and Polish long-string smoke checks are already covered.
- Store package: the default Google Play listing has its icon, feature graphic,
  videos, seven phone screenshots, seven dedicated 7-inch screenshots, seven
  dedicated 10-inch screenshots, and the complete Play Games on PC image set.
  The tablet additions were saved as a Play Console draft on 2026-08-22; the
  same source assets remain validator-checked locally.
- Leaderboards: local sanity guard blocks incomplete, non-positive, or
  impossible-progress submissions; server-side anti-cheat is still deferred.
- Release state: version `0.1.0` / version code `1` has been prepared as a
  signed AAB. Google Play has granted production-release access, but the
  release is inactive and the latest store-listing changes remain a draft.
- Active roadmap: [`docs/superpowers/plans/2026-08-22-release-polish-roadmap.md`](docs/superpowers/plans/2026-08-22-release-polish-roadmap.md)
- Repository safety: signing material and `app/google-services.json` are local
  only and must never be committed.

## Brainball Roster Contract

The shared Brainball artwork is synchronized from the canonical Rift Lab roster
used by the other two games:

- 30 core Brainballs;
- 6 legendary Brainballs;
- 2 additional cross-game/flight characters;
- 38 shared slugs and PNG assets in total.

Kavvoro also keeps 12 additional Brainballs that are exclusive to this first
game. They are intentionally retained so existing collection progress and
unlock progression are not removed. Shared asset slugs must remain stable across
all three games; gameplay-specific unlock rules and short taglines may differ by
game.

The local baseline is verified with:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Core Loop

The ball launches when the player first holds the stage. Holding creates a movable gravity tether, dragging steers the flight, and releasing lets the ball coast while Rift energy recharges. A full Rift lasts roughly 2.5 seconds, so each run requires short, deliberate control bursts.

The game combines:

- a responsive animated menu with Classic Mode and Chaos Mode;
- portrait-only phone and tablet presentation;
- mode-specific streaks, continue flow, ad placeholders, and progress;
- localized UI and interactive tutorial overlays;
- deterministic daily seeded levels;
- JBox2D physics for stable collision behavior;
- continuous one-thumb Rift Control instead of a draw-and-wait loop;
- live tether energy, capped ball speed, hold chains, and HYPE scoring;
- pulse fields that push and swirl the character;
- original Kavvoro mascots and curse modifiers;
- hold-first run modifiers such as Wind Guard, Rift Drain, Pulse Guard, Focus Field, Power Hold, Moon Glide, Tiny Gate, and Overheat;
- short runs built for replay/share moments;
- 9:16 MP4 replay export through Android share sheet for TikTok, Instagram, and other apps;
- replay videos that render the ball, control trail, and active gravity tether;
- a dedicated Leaderboards screen with separate all-time Classic and Chaos level/streak records;
- Google Play Games Services v2 hooks for authentication, score submission, and native global rankings;
- rank scoring by time, energy economy, and chain execution;
- hype score, streak metadata, and short challenge codes for shareable post-run cards;
- AdMob interstitial ad gates for streak and continue moments.

## Ads

Debug builds use Google's interstitial test unit. Release builds use:

- App ID: `ca-app-pub-5095011886038660~7427660921`
- Interstitial unit: `ca-app-pub-5095011886038660/8267923702`
- Rewarded continue unit: `ca-app-pub-5095011886038660/1976689979`

Use test ads while developing to avoid invalid traffic on the AdMob account.

No consent request, ad initialization, or ad request starts until the first-launch
neutral age screen is completed. The app stores only `CHILD`, `TEEN`, or `ADULT`,
never the entered date:

- child profiles receive Google's child age treatment and G-rated ads;
- teen profiles receive Google's teen age treatment and T-rated ads;
- adult profiles use the UMP consent flow when required by region;
- ads remain disabled until UMP reports that ads may be requested.

Before publishing, create and publish the required message in AdMob under
`Privacy & messaging`, host a public privacy policy, add its URL to Play Console
and the AdMob app settings, and complete Play Console's Target audience, Ads,
Data safety, and Families declarations consistently with the implementation.

Release prep checklists:

- `docs/release-polish-checklist.md`
- `docs/play-console-compliance.md`

## Google Play Games

Create four highest-score leaderboards in Play Console, then replace the placeholders in `app/src/main/res/values/strings.xml`:

- `game_services_project_id`
- `leaderboard_classic_level_id`
- `leaderboard_chaos_level_id`
- `leaderboard_classic_streak_id`
- `leaderboard_chaos_streak_id`

Until all five values are configured, the Leaderboards screen remains in local-record mode and the Play Games SDK is not initialized.

## Build

The project requires Android Studio's bundled JDK 17+ and an Android SDK with
API 36 installed. The app compiles against and targets API 36, while keeping
API 24 as the minimum supported version. Set paths for the current machine before building; adjust
them if Android Studio is installed elsewhere.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### Firebase configuration

`app/google-services.json` is intentionally not committed. Download the Android
configuration for `com.moonsolstudios.kavvoro` from the Firebase console and
place it at `app/google-services.json` before building. Never commit that file
or any signing material.

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat bundleRelease
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release App Bundle:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Google Play Readiness

Before production release:

- configure the five Play Games IDs in `app/src/main/res/values/strings.xml`;
- configure and publish the AdMob privacy message and public privacy policy;
- complete Play Console target-audience, ads, Data Safety, and Families
  declarations consistently with the implementation;
- complete final physical-device QA, publish the saved store-listing changes,
  and create the intended Google Play release;
- create and activate premium Brainball products before enabling paid content;
- rotate/restrict Firebase and other API keys if they have ever been exposed;
- keep signing backups secure and separate from the repository.

See also:

- `docs/release-polish-checklist.md`
- `docs/play-console-compliance.md`

## Next Milestones

Priority order:

1. **P0 — Complete localization audit:** finish extended-state evidence and
   full manual review; strict catalogs, trusted 19-row Home/Classic capture,
   placeholder checks, locale number formatting, and voice inventory are
   complete. Track evidence in
   `docs/localization-qa-matrix.md`.
2. **P0 — Challenge loop:** add challenge deep-link import/export and ghost
   replay so one player can send a run that another player can attempt.
3. **P1 — Closed-test validation:** test the complete onboarding, tutorial,
   Classic/Chaos progression, tablet layout, sharing, ads, and billing flows on
   physical devices and record every regression in this README or the release
   checklist.
4. **P1 — Measurement tuning:** configure Remote Config for safe tuning of
   level difficulty, streak gates, and monetization; the Analytics event schema
   and Crashlytics baseline are now in place.
5. **P2 — Social growth:** improve challenge links, replay cards, and the
   share-to-short-video flow around the challenge/ghost loop.
6. **P2 — Final polish:** finish beat-reactive haptics, final audio listening
   checks, performance checks, and signed-release store review before production.
