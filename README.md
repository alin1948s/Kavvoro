# Brainrot Chaos: Kavvoro

Phone-first Android physics game prototype.

## Core Loop

The ball launches when the player first holds the stage. Holding creates a movable gravity tether, dragging steers the flight, and releasing lets the ball coast while Rift energy recharges. A full Rift lasts roughly 2.5 seconds, so each run requires short, deliberate control bursts.

The prototype combines:

- a phone-first animated menu with Classic Mode and Chaos Mode;
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
- hype score, streak metadata, and short challenge codes for shareable post-run cards.
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

### Firebase configuration

`app/google-services.json` is intentionally not committed. Download the Android
configuration for `com.moonsolstudios.kavvoro` from the Firebase console and
place it at `app/google-services.json` before building. Never commit that file
or any signing material.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:ANDROID_HOME='C:\Users\creta\AppData\Local\Android\Sdk'
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

## Next Milestones

- Add deep-link challenge import and ghost replay.
- Integrate Firebase Analytics and Remote Config.
- Add sound design and beat-reactive haptics.
- Add Play Billing for premium balls.
- Add tablet layout after phone retention and readability are validated.
