# Kavvoro UI/Design Audit — 2026-09-23

Scope: the shipped Kotlin + Canvas UI (`ChaosGameView` and the per-screen renderers
under `app/src/main/java/com/moonsolstudios/kavvoro/ui/`). Every finding below is
backed by a file reference or a reproducible command; no code was changed by this
audit.

## Summary

| ID | Finding | Priority | User impact |
| --- | --- | --- | --- |
| UI-1 | Home screen copy bypasses the 24-language localization catalog | P0 | 17 of 24 selectable languages render Home in English |
| UI-2 | TalkBack virtual nodes go stale on every screen except Home | P0 | Screen reader users cannot operate Settings/Language/gameplay/results |
| UI-3 | Home header touch targets fall below 48dp on phones | P0 | Missed taps on the gear and stat chips |
| UI-4 | No reduced-motion support | P1 | Shake/glow/parallax cannot be disabled for vestibular sensitivity |
| UI-5 | No shared palette; 1,606 hardcoded ARGB literals across 26 files | P1 | Visual language drifts between screens; contrast fixes do not propagate |
| UI-6 | Hazard/reward semantics ride on hue alone | P2 | Colorblind players can misread hazard state |
| UI-7 | Header chip and language footer composition | P2 | Dead space in chips; weak selected state in the language footer |

## Progress

| ID | State | Notes |
| --- | --- | --- |
| UI-1 | **Done** | 12 Home keys added to the strict catalog for all 24 languages; `HomeCopy` now delegates to `KavvoroI18n.t`; guarded by `HomeCopyCatalogCoverageTest`. |
| UI-2 | **Partially done** | Stale-node behaviour removed (nodes publish only on the Home surface), gear/chip/banner bounds now use the touch rects, `SETTINGS` label localized. Per-screen node sets for Settings/Language/gameplay/results are still outstanding. |
| UI-3 | **Done** | Fixed with separate touch rects; visuals unchanged. Guarded by `HomeTouchTargetTest`. |
| UI-4 to UI-7 | Not started | |

Implementation notes for what shipped are at the end of this document.

## UI-1 — Home screen copy bypasses the localization catalog (P0)

`i18n/HomeCopy.kt` holds hand-written `when (language)` tables that enumerate only
`RO, ES, FR, DE, IT, PT, RU`; every other language hits `else -> "<English>"`.
It does not read `LocalizationCatalog` at all.

The Home keys are absent from the catalog, so this is not a wiring bug — the
inventory itself is incomplete. Verified against the English and Japanese sources:

```powershell
Select-String -Path 'app\src\main\java\com\moonsolstudios\kavvoro\i18n\catalog\EnTranslations.kt','app\src\main\java\com\moonsolstudios\kavvoro\i18n\catalog\JaTranslations.kt' -SimpleMatch '"PLAY NOW"'
```

| Key needed by `HomeCopy` | present in EN | present in JA |
| --- | --- | --- |
| `PLAY NOW`, `JUMP INTO A BRIGHTER UNIVERSE` | no | no |
| `SKINS`, `CUSTOMIZE YOUR STYLE` | no | no |
| `MISSIONS`, `COMPLETE TASKS EARN REWARDS` | no | no |
| `LEADERBOARD` | no | no |
| `MISSIONS & REWARDS`, `COMPLETE DAILY MISSIONS & WIN` | no | no |
| `COINS`, `SMALL MINDS BIG WORLDS` | no | no |
| `PLAY`, `STREAK`, `LEVEL` | yes | yes |

Consequence: the Home screen — the first and most-photographed surface — is English
for `NL, PL, CS, SV, FI, TR, UK, AR, HI, TH, ID, VI, JA, KO, ZH, ZH_TW`, even though
the root `README.md` states the strict catalogs are complete for all 24 selectable
languages. The language selector correctly reports `CURRENT: ENGLISH` while offering
`Indonesia` / `Tiếng Việt` rows, which makes the mismatch visible to the player.

### Fix shape

1. Add the ~14 Home keys to `LocalizationCatalog.requiredKeys` and populate them in
   all 24 files under `i18n/catalog/`. `noUnapprovedEnglishFallbackRemainsInTheStrictCatalog`
   requires real translations, so any legitimately identical string (brand terms such
   as `SKINS`) must go through `LocalizationCatalog.allowlistedEnglishValues`.
2. Reduce `HomeCopy` to thin delegates over `KavvoroI18n.t(language, KEY)` so the
   Home screen can never drift from the catalog again. Keep the `fun x(context: Context)`
   overloads — `HomeAccessibilityTouchHelper` depends on them.
3. Update `HomeResponsiveLayoutTest.testPlayLocalizationContractCopies` if the
   delegate changes the returned values for the languages it asserts.

### Existing gates that cover this work

- `LocalizationCatalogTest.everySelectableLocaleHasExactInventoryCoverageAndNonBlankValues`
- `LocalizationCatalogTest.frozenInventoryMatchesTheCurrentSourceBackedInventory`
- `LocalizationCatalogTest.everyLocalePreservesTheEnglishPlaceholderSignature`
- `LocalizationCatalogTest.proceduralUiVocabularyIsExplicitlyLocalizedForEveryLanguage`

## UI-2 — TalkBack virtual nodes go stale on every screen except Home (P0)

Kavvoro renders every screen from one `SurfaceView` (`ui/ChaosGameView.kt`), so screen
readers depend entirely on the virtual-node provider. There is exactly one such
provider — `ui/accessibility/HomeAccessibilityTouchHelper.kt` — and it is registered
unconditionally in `init`:

```kotlin
// ui/ChaosGameView.kt:489
androidx.core.view.ViewCompat.setAccessibilityDelegate(this, homeAccessibilityHelper)
```

`getVisibleVirtualViews()` always publishes the nine Home nodes
(`ID_SETTINGS … ID_BANNER_CARD`), and `getVirtualViewAt()` only hit-tests
`HomeLayoutCalculator` rects. On `SETTINGS`, `LANGUAGE`, `GAME`, Collection,
Leaderboards, and the result/out-of-run states, TalkBack therefore:

- announces Home's `PLAY NOW` / Skins / Missions / Leaderboard / chip nodes with
  **stale bounds** that do not match what is drawn, and
- exposes **no nodes at all** for the controls actually on screen (settings tabs,
  volume sliders, toggles, language rows, back buttons, result buttons, ad/continue CTA).

Reproduce: enable TalkBack, open Home, then open Settings and swipe through the items —
the announced list is still the Home list.

Secondary localization leak in the same file:

```kotlin
// ui/accessibility/HomeAccessibilityTouchHelper.kt:80
node.contentDescription = "Settings"
```

Every other node routes through `HomeCopy`; this one is hardcoded English.

### Fix shape

1. Make the delegate screen-aware: bind the active `Screen` plus that screen's
   layout calculator, and derive `getVisibleVirtualViews()` / `getVirtualViewAt()` /
   `boundsInParent` from it. `SettingsLayoutCalculator`, `LanguageSelectorLayout`,
   `HomeLayoutCalculator`, and `ModePickerLayoutCalculator` already publish the rects
   to reuse — no new geometry is required.
2. Either register the delegate only for `Screen.MENU`/mode picker, or publish an
   empty node list for screens that are not yet instrumented so stale Home nodes are
   never announced. An empty list is strictly better than wrong nodes.
3. Route the `Settings` label through the catalog (`"SETTINGS"` already exists in
   `UiTranslations.requiredKeys`).
4. Follow the existing node conventions in the file: `className` of `Button` for
   clickable rows, `TextView` for read-only values, `addAction(ACTION_CLICK)` only
   when `onPerformActionForVirtualView` handles it.

### Suggested coverage

Add a unit test that builds each layout calculator at phone and tablet sizes and
asserts the published node set matches the screen (mirroring the existing
`HomeUiRendererSafetyTest` / `SettingsResponsiveLayoutTest` style). No emulator
required.

## UI-3 — Home header touch targets fall below 48dp on phones (P0)

`ui/screens/home/HomeLayoutCalculator.kt` picks `LayoutMode.COMPACT` for
`widthDp <= 480f` (line 150) — every phone — and then sizes the gear and stat chips
from a scaled 38dp:

```kotlin
val scaleFactor = (heightDp / 800f).coerceIn(0.72f, 1.25f)                       // :162
val actionButtonSize = dp(if (layoutMode == LayoutMode.TABLET) 44f else 38f) * scaleFactor // :166
...
val chipHeight = actionButtonSize                                                 // :178
```

| Device height | `scaleFactor` | Gear + chip height |
| --- | --- | --- |
| 800dp | 1.00 | 38dp |
| 640dp | 0.80 | 30.4dp |
| 560dp | 0.72 (floor) | 27.4dp |

All three are below the 48dp minimum touch target, and the same rects supply the
`Streak` / `Level` / `Coins` chips that the accessibility helper publishes as
`boundsInParent`, so the shortfall affects both touch and TalkBack explore-by-touch.

The MEDIUM path in the same file already does this correctly:

```kotlin
val actionButtonSize = dp(48f * scaleFactor).coerceIn(dp(48f), dp(54f))          // :412
```

### Fix shape

Apply the same floor to COMPACT (`dp(48f)` floor, upper bound from the existing
`scaleFactor`), and re-check the two-row fallback for widths under 340dp
(`:224-238`), where the chips move to their own row and currently inherit the same
undersized `chipHeight`. Keep the visual icon at its current size and grow only the
hit rect if a 48dp chip disturbs the header composition; `HomeLayoutCalculator`
already separates visual and interactive rects elsewhere (e.g. `soundButtonRect.set(settingsButtonRect)`).

## UI-4 — No reduced-motion support (P1)

```powershell
Select-String -Path 'app\src\main\java\com\moonsolstudios\kavvoro\**\*.kt' -Pattern 'ValueAnimator|ObjectAnimator|Interpolator|Choreographer|reduceMotion|animationScale|ANIMATOR_DURATION'
# no matches
```

All motion in Kavvoro is frame-counter driven inside the Canvas loop, and nothing
reads Android's global animation scale (`Settings.Global.ANIMATOR_DURATION_SCALE`)
or offers an in-app preference. Players who disable animations system-wide, and
players sensitive to shake and flashing, still get the full effect set.

Settings already establishes the pattern to follow: `SCREEN SHAKE` and
`PERFORMANCE MODE` are boolean rows backed by
`GameProgressRepository.SETTINGS_SCREEN_SHAKE_KEY` /
`SETTINGS_PERFORMANCE_KEY`, with localized labels in `UiTranslations.requiredKeys`.

### Fix shape

1. Add a `REDUCE MOTION` toggle to the GAMEPLAY settings tab alongside `SCREEN SHAKE`.
2. Seed it from the system animation scale on first run, and let the explicit user
   choice win afterwards (persist with the existing settings keys).
3. Gate the non-essential motion: screen shake, glow pulses, portal parallax, and any
   continuous background drift. Keep gameplay-critical motion (ball, tether, hazard
   timing) untouched so the game stays readable and fair.
4. Add the new label to `LocalizationCatalog.requiredKeys` and all 24 catalogs so
   `proceduralUiVocabularyIsExplicitlyLocalizedForEveryLanguage` keeps passing.

## UI-5 — No shared palette; 1,606 hardcoded ARGB literals across 26 files (P1)

```powershell
$m = Get-ChildItem -Recurse -Path 'app\src\main\java' -Filter '*.kt' |
     Select-String -Pattern '0x[0-9A-Fa-f]{8}' -AllMatches
"matches=$((($m | ForEach-Object { $_.Matches.Count }) | Measure-Object -Sum).Sum)"
"files=$(($m | Group-Object Path).Count)"
# matches=1606  files=26
```

Worst offenders are all UI renderers:

| File | Literal lines |
| --- | --- |
| `ui/screens/collection/CollectionUiRenderer.kt` | 128 |
| `ui/screens/settings/SettingsUiRenderer.kt` | 114 |
| `ui/screens/home/ModePickerRenderer.kt` | 111 |
| `ui/ChaosGameView.kt` | 78 |
| `ui/screens/home/HomeUiRenderer.kt` | 75 |
| `ui/screens/settings/SettingsIconRenderer.kt` | 73 |
| `ui/screens/gameplay/GameplayHudRenderer.kt` | 70 |
| `ui/screens/modals/DailyRiftRewardRenderer.kt` | 66 |
| `ui/screens/gameplay/GameplayArenaRenderer.kt` | 62 |
| `ui/screens/leaderboards/LeaderboardUiRenderer.kt` | 61 |
| `ui/screens/home/HomeMenuRenderer.kt` | 55 |
| `ui/screens/language/LanguageSelectorRenderer.kt` | 52 |

There is no token source in the app. `KavvoroDesignTokens.kt` and `UI_TOKENS.json`
exist only under `implementation_pack/KAVVORO_Final_Home_Implementation_Pack/` — a
Compose reference pack that was never wired into the shipped Kotlin/Canvas renderers.
The only shared color helper today is `ui/render/RenderColor.kt` (`withAlpha`).

The practical cost is drift: the cyan `0xFF45F2FF`, magenta `0xFFFF4D8D`, amber
`0xFFFFCF4A`, and violet `0xFFC15CFF` family plus the white/near-white inks are
re-typed per renderer with slightly different alphas, so a contrast fix tuned on Home
does not reach Settings, Collection, or Results.

### Fix shape

1. Add a single semantic token object — `ui/render/KavvoroPalette.kt` — as the Kotlin
   source of truth (`surfaceBase`, `surfaceElevated`, `strokeCyan`, `strokeMagenta`,
   `accentAmber`, `textPrimary`, `textMuted`, `overlayScrim`, …), keeping the existing
   `withAlpha` helper for per-draw alpha.
2. Reconcile token values against `implementation_pack/.../UI_TOKENS.json` so the
   Compose pack and the shipped renderers describe the same system, then record the
   agreed values in this document.
3. Migrate one renderer per change, starting with the four highest-count UI files, and
   verify visually with the existing screenshot workflow. Keep `BallSkinCatalog.kt`
   (50), `LevelDirector.kt` (47), and `AdvancedLevelDirector.kt` (19) literals as
   content data, but have them reference shared tone tokens where the value is a
   shared universe color rather than per-skin art direction.

## UI-6 — Hazard and reward semantics ride on hue alone (P2)

`engine/AdvancedLevelDirector.kt:453-458` assigns each `CurseType` an independent
literal (`Wind Guard` `0xFF8AA6FF`, `Rift Drain` `0xFF64E572`, `Heavy Core`
`0xFFFF8C42`, `Moon Glide` `0xFF45F2FF`, `Focus Field` `0xFFFFCF4A`, `Power Hold`
`0xFFFF4D8D`), and the HUD/arena renderers define their own danger and reward colors
separately. Nothing guarantees that "this can hurt you" is one color everywhere, and
colorblind players cannot distinguish hazard from reward by hue.

### Fix shape

Define one semantic triple in the palette (danger / reward / neutral-telegraph) and
pair it with redundancy that the design language already has — the readable text label
(`HOLD BLOCKS GUSTS`, `SHORTER HOLDS`, …) plus a distinct outline or glyph weight.
Verify against deuteranopia, protanopia, and tritanopia simulations on the Home chips,
the in-run HUD, and the curse badge.

## UI-7 — Header chip and language footer composition (P2)

From current 412x915 captures of Home and the language selector:

- **Header chips**: the `SERIE` / `NIVEL` / `MONEDE` label is right-aligned above its
  value while the icon is pinned far left, leaving a wide empty band; the value digits
  are visually much heavier than the label. The chip outline is cyan on the outer edge
  and magenta inside, so one 38dp-tall component carries two competing accents.
- **Language footer**: `CURRENT: ENGLISH` uses a small cyan dot and much lighter type
  than the 55dp rows above it, so the footer reads as a different component family. The
  selected row's radio is an unfilled ring with almost the same weight as unselected
  rows, so selection state depends on border glow alone. `RADIO_INNER_DOT_DP` is
  already defined in `LanguageSelectorMetrics` but the inner dot is not used to carry
  the selected state.

### Fix shape

- Group icon, label, and value as one centered stack (or move the icon adjacent to the
  label) instead of two opposing alignments; pick a single accent for the chip border
  and reserve the second accent for the selected or active state.
- Render the selected radio as a filled `RADIO_INNER_DOT_DP` dot inside the ring so
  selection is carried by shape and fill, not only by glow.
- Reuse the card chamfer and inner-glow recipe for the footer so it reads as part of
  the same family.

## Suggested implementation sequence

1. ~~**UI-1** — catalog parity plus `HomeCopy` delegation.~~ **Shipped.**
2. ~~**UI-3** — COMPACT touch-target floor.~~ **Shipped as derived touch rects** (see the
   correction in the implementation record).
3. **UI-2** — screen-aware accessibility nodes. The stale-node half shipped; finish the
   per-screen node sets for Settings, Language, gameplay, Collection, Leaderboards, and the
   result screens.
4. **UI-4** — `REDUCE MOTION` setting and motion gating.
5. **UI-5** then **UI-6** — palette tokens and contrast semantics, then the visual
   polish in **UI-7** on top of the stabilized tokens.

## Verification

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Item-specific gates:

- UI-1: `LocalizationCatalogTest` (9 tests), `KavvoroI18nTest`, `HomeResponsiveLayoutTest`,
  and the new `HomeCopyCatalogCoverageTest` (4 tests).
- UI-2: `HomeAccessibilityTouchHelper` uses the touch rects and publishes only on the Home
  surface; the remaining per-screen node sets still need a node-set test plus a manual TalkBack
  pass on Home → Settings → Language → gameplay → result.
- UI-3: the new `HomeTouchTargetTest` (8 tests) asserts the 48dp minimum on eight device
  profiles, that touch rects cover their visual rects and never overlap each other, and that the
  visual gear keeps its designed size.
- UI-4: a settings-persistence test matching the existing `AccountProgressStoreTest`
  style.
- UI-5 and UI-6: visual diff against the reference captures in `screenshots/` and the
  colorblind simulations noted above.

## Notes

- Only Home has an accessibility provider today; this audit deliberately does not
  propose a Compose migration. The renderer-per-screen structure already isolates the
  work, so each item above is a contained change.
- Source-of-truth reminder: this document records findings and a plan. It must not
  override the root `README.md` or the current code.

## Implementation record (2026-09-23)

### UI-1 — shipped

- Twelve new source keys were added to `LocalizationCatalog.renderedSourceKeys` and to all
  24 files under `i18n/catalog/`, each inserted at its ASCII-sorted position:
  `COINS`, `COMPLETE DAILY MISSIONS & WIN`, `COMPLETE TASKS EARN REWARDS`,
  `CUSTOMIZE YOUR STYLE`, `JUMP INTO A BRIGHTER UNIVERSE`, `LEADERBOARD`, `MISSIONS`,
  `MISSIONS & REWARDS`, `PLAY NOW`, `SEE TOP PLAYERS AROUND THE WORLD`, `SKINS`,
  `SMALL MINDS BIG WORLDS`. `PLAY`, `STREAK`, and `LEVEL` already existed and are reused.
- The seven languages (plus English) that `HomeCopy` already carried keep their existing
  wording verbatim, so those installs see no change. The other 17 now render real
  translations instead of English.
- `allowlistedEnglishValues` gained two entries, `Skins` and `Missions`, for the languages
  where the gameplay loanword is the standard term (French/German/Portuguese/Dutch
  "Skins", French "Missions").
- Two existing drifts were resolved in favour of the shared catalog, which is what the
  results/HUD screens already render: German `LEVEL` chip is now `LEVEL` (was `STUFE`) and
  Portuguese `STREAK` chip is now `SÉRIE` (was `SEQUÊNCIA`).
- `HomeCopy` is now 15 one-line delegations plus the `Context` overloads, replacing ~150
  lines of hand-written per-language tables. The public API is unchanged, so
  `HomeMenuRenderer`, `SciFiCtaButtonRenderer`, and `HomeAccessibilityTouchHelper` were not
  touched.
- New guard: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/HomeCopyCatalogCoverageTest.kt`
  (all 24 languages non-blank, Home keys present in the inventory, no non-English language
  falling back to English copy, and spot-checked localized values).
- Drive-by cleanup: `LocalizationCatalog.locale()` fell back to
  `KavvoroI18n.t(resolvedLanguage, key)`, which re-enters `locale()` for the same language
  while its cache entry is being computed. That is unreachable today only because every
  overlay is complete; any future gap would have been a stack overflow instead of an
  English fallback. It now falls back to the source key directly.
- The one-shot migration script is kept at `scratch/apply_home_catalog_keys.py` for
  traceability; re-running it is a no-op.

### UI-3 — shipped, with a correction to the original proposal

The first attempt applied the audit's literal suggestion (floor the visual gear and chips at
48dp). The new `HomeTouchTargetTest` failed immediately and exposed why that is wrong: the
compact header band grew by up to ~20dp, and the layout's one-row/two-row decision for the
stat chips sits right at the threshold, so the composition shifts.

What shipped instead keeps every visual rect exactly as designed and adds derived touch rects:

- `HomeLayoutCalculator.MIN_TOUCH_TARGET_DP` (48dp) is now the single source for the minimum,
  reused by the already-compliant MEDIUM path.
- `settingsTouchRect`, `streakChipTouchRect`, `levelChipTouchRect`, `coinsChipTouchRect`, and
  `bannerTouchRect` are recomputed at the end of every `calculate()` from the visual rects.
  An unset (empty) visual rect yields an empty touch rect, so a layout that does not use a
  surface can never invent a target near the origin.
- The gear recovers any width lost to the screen-edge clamp on its inward side; it is
  hit-tested before the read-only stat chips, so reaching over them is safe.
- Chip touch rects grow vertically in full and horizontally only 2dp per side, which is inside
  the 4.32dp minimum gap, so they never overlap each other.
- Consumers wired to the touch rects: `HomeMenuTouchController.hitTestMenuButton` (gear) and
  `HomeAccessibilityTouchHelper` bounds (gear, chips, banner).

Measured effect on the compact phones in the test matrix: the gear and chips go from 27.4-38dp
up to a full 48dp touch height, and the banner from ~42dp to 48dp, with no visual change.

### UI-2 — what remains

`HomeAccessibilityTouchHelper` now takes `isHomeSurfaceVisible`, which `ChaosGameView` wires to
`screen == Screen.MENU && menuState == MenuState.MODES`. Off that surface the provider
publishes no nodes and never claims a point, which removes the stale-bounds bug. The remaining
work is real node sets for Settings, Language, gameplay, Collection, Leaderboards, and the
result screens, using the rects those layout calculators already produce.