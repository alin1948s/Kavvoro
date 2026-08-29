# Brainrot Chaos: Kavvoro Release Polish Checklist

Use this before every Play Console upload.

## Build Sanity

- Release build has `FORCE_UNLOCK_ALL_BRAINBALLS=false`.
- Debug build uses Google test ads only.
- Release build uses the live AdMob app id and ad unit ids.
- `google-services.json` package name matches `com.moonsolstudios.kavvoro`.
- Play Games ids in `res/values/strings.xml` are real before public launch.
- Billing products are active in Play Console before enabling paid Brainballs publicly.

## First Session

- First 10 levels have no ads.
- Level 1 teaches hold/pull/release.
- Level 2 explains pulse/boost fields.
- Level 3 explains energy and HYPE.
- Levels 4-5 explain hazards and chain.
- Levels 6-10 explain special modifiers.
- Level 10 clearly communicates the Voro Grad unlock.

## Monetization

- Interstitial pacing starts only after training.
- Level interstitial cadence is every 6 eligible levels.
- Rewarded continue is shown after the configured fail threshold.
- Rewarded continue never grants progress unless the reward callback fires.
- Restore purchases works from Collection.
- Paid Brainballs show local Play Billing prices, not hardcoded currency.

## Store Assets

- App icon is readable at launcher size.
- Feature graphic uses the Kavvoro character and the game title.
- Screenshots show Home, gameplay, Collection, superpowers, and result/share.
- Trailer is vertical 9:16 and starts with gameplay in the first 2 seconds.
- Store short description says what the player actually does.

## Low-End Device Pass

- Test on a 720p/low GPU device profile.
- Confirm no frame pacing spikes during Chaos, result burst, and Collection scroll.
- Confirm Lite rendering still keeps the ball, portals, hazards, and goal readable.
- Music switches between Menu, Tutorial, Classic, and Chaos without a hard cut.
- Music mute and SFX mute remain independent in every screen and during a track switch.
- Listen on the phone speaker and headphones at normal volume; music must not clip or mask selection voices.
- Run `tools/verify_localized_voice_assets.py`; every supported locale must have all 50 Brainball voices.
- Rapidly switch Classic/Chaos, pause/resume the app, and confirm there is no stale track or audio burst.
- Audio should not clip or drown out UI feedback.

## Localization QA

- Run the strict 19-language catalog tests before every release candidate.
- Run `tools/localization_visual_matrix.ps1` on a stable emulator or device.
- Check Home, Classic HUD/tutorial, Collection, and result states in each locale.
- Check Arabic RTL alignment and Polish/long-string wrapping explicitly.
- Confirm localized decimal separators remain consistent between HUD and results.
- Record evidence in `docs/localization-qa-matrix.md`; pending rows block release.
