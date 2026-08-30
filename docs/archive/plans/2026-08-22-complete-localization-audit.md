# Complete Localization Audit Implementation Plan

> Historical implementation plan. Counts and paths may be stale; current code,
> `docs/localization-qa-matrix.md`, and the repository README are authoritative.

**Goal:** Complete and verify the rendered APK localization catalog for all 19 selectable Kavvoro languages, removing unapproved English fallback and preventing layout/placeholder regressions.

**Architecture:** Keep `KavvoroI18n` as the public lookup surface, but replace its permissive row defaults with a strict immutable key inventory and locale-specific maps. Use one Kotlin translation file per selectable language (`EnTranslations.kt` through `ZhTranslations.kt`, `SYSTEM` excluded); `LocalizationCatalog` is the single strict assembler and validator. Add a deterministic visual-QA matrix for representative screens and document evidence in README/checklists.

**Tech Stack:** Kotlin, JUnit, Android Canvas/layout helpers, Gradle, existing `KavvoroI18n`/`TutorialCopy` catalogs, emulator screenshots, PowerShell/ADB for locale capture.

**Spec:** `docs/superpowers/specs/2026-08-22-complete-localization-audit-design.md`

## Global Constraints

- Cover exactly the 19 selectable languages already defined by `KavvoroLanguage`; `SYSTEM` is not a translation target.
- Preserve the public `KavvoroI18n.t(language, key)` API and all existing key names.
- Preserve `%s`, `%d`, line breaks, and the allowlisted terms `Kavvoro`, `Brainball`, `Rift`, `HYPE`, `Classic`, `Chaos`, `Google Play`, `Firebase`, and `AdMob`.
- Do not change gameplay behavior, store listing text, voice generation, or audio assets in this plan.
- Do not accept English fallback as a valid translation for a non-English locale except for an explicit allowlisted brand/product value.
- Use `apply_patch` for source/docs edits and preserve unrelated dirty-worktree changes.
- Do not commit automatically.

---

### Task 1: Freeze the key inventory and create the failing completeness tests

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalog.kt`
- Create: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalogTest.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18nTest.kt`

**Interfaces:**
- `LocalizationCatalog.requiredKeys: Set<String>` is the immutable inventory of every rendered UI key.
- `LocalizationCatalog.allowlistedEnglishValues: Set<String>` contains only approved brand/product values.
- `LocalizationCatalog.locale(language: KavvoroLanguage): Map<String, String>` returns a complete locale map or fails during test assembly.
- `LocalizationCatalog.placeholderSignature(value: String): List<String>` returns ordered format placeholders for parity checks.

- [x] **Step 1: Inventory every current rendered source key**

  Extract the existing `KavvoroI18n` row keys, `TutorialCopy.requiredKeys`, generated title keys, status labels, curse/power ribbons, and dynamic purchase/privacy keys into a deterministic list. Preserve source spellings exactly so existing callers do not change.

- [x] **Step 2: Write the failing strict catalog tests**

  Add tests that iterate `KavvoroLanguage.entries.filterNot { it == SYSTEM }` and assert exact key-set equality, nonblank values, placeholder parity, no unapproved English equality, and exactly 19 selectable languages. Add a test that fails against the current default-argument catalog because at least one extended locale currently equals English.

- [x] **Step 3: Run the focused tests and verify the expected red state**

  ```powershell
  $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
  .\gradlew.bat testDebugUnitTest --tests '*LocalizationCatalogTest' --tests '*KavvoroI18nTest'
  ```

  Expected result: failure from missing strict maps or unapproved English fallback, not a compilation/setup error.

---

### Task 2: Migrate the lookup layer to strict locale maps

**Files:**
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroI18n.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/TutorialCopy.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalog.kt`

**Interfaces:**
- `KavvoroI18n.t(language, key)` delegates to `LocalizationCatalog.locale(language)[key]` after normalizing legacy/tutorial overrides.
- Unknown keys retain a defensive source-text return at runtime, while catalog tests fail if a rendered path depends on it.
- `TutorialCopy` continues to expose its current selector methods and reads the same language maps.

  - [x] **Step 1: Add a strict source-key adapter without changing callers**

  Build the catalog from the current source-key inventory and route existing lookup calls through it. Keep the old public methods and migrate one per-language map at a time.

  - [x] **Step 2: Remove permissive `= en` defaults from accepted locale data**

  Make every locale map explicit. If a key is intentionally brand-only, use the allowlist; otherwise require a translated value. Keep runtime fallback only after catalog lookup misses due to an unknown key.

  - [x] **Step 3: Run the focused catalog tests**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*LocalizationCatalogTest' --tests '*KavvoroI18nTest'
  ```

  Expected result: all strict catalog and existing tutorial tests pass before visual QA begins.

  **Task 2 checkpoint:** compilation, placeholder parity, inventory checks, and
  existing tutorial tests pass. The unapproved-English fallback assertion is
  intentionally still red with 3,530 gaps until Tasks 3–4 supply the explicit
  translation overlays; see `.superpowers/sdd/complete-localization-audit/task-2-report.md`.

---

### Task 3: Fill the per-language Latin locale files

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/EnTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/RoTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/EsTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/FrTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/DeTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/ItTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/PtTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/NlTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/PlTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/TrTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/IdTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/ViTranslations.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalog.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalogTest.kt`

**Scope:** English baseline, Romanian, Spanish, French, German, Italian, Portuguese, Dutch, Polish, Turkish, Indonesian, and Vietnamese.

- [x] **Step 1: Add explicit translations for every key in each Latin-language file**
- [x] **Step 2: Keep each language map isolated; do not create grouped translation files**
- [x] **Step 3: Preserve short variants for menu/result labels and exact placeholders**
- [x] **Step 4: Run catalog tests and record zero missing/fallback keys**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*LocalizationCatalogTest'
  ```

---

### Task 4: Fill the remaining per-language locale files

**Files:**
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/RuTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/UkTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/ArTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/HiTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/JaTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/KoTranslations.kt`
- Create: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/ZhTranslations.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalog.kt`
- Test: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/LocalizationCatalogTest.kt`

**Scope:** Russian, Ukrainian, Arabic, Hindi, Japanese, Korean, and Simplified Chinese.

- [x] **Step 1: Add explicit Russian/Ukrainian translations and preserve punctuation/placeholders**
- [x] **Step 2: Add natural Arabic/Hindi translations and mark RTL-sensitive labels**
- [x] **Step 3: Add native-punctuation Japanese/Korean/Chinese translations**
- [x] **Step 4: Run catalog tests and inspect representative strings for script correctness**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*LocalizationCatalogTest'
  ```

---

### Task 5: Add locale-safe formatting and layout regression tests

**Files:**
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/i18n/KavvoroNumberFormat.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayout.kt`
- Modify: `app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt`
- Create: `app/src/test/java/com/moonsolstudios/kavvoro/i18n/LocaleLayoutPolicyTest.kt`
- Modify: `app/src/test/java/com/moonsolstudios/kavvoro/ui/TutorialCardLayoutTest.kt`

- [x] **Step 1: Write failing tests for comma/dot/Arabic/Indic number formatting and long-label policy**
- [x] **Step 2: Add explicit format policies for Arabic and Indic locales**
- [x] **Step 3: Add width-safe short-label/wrapping rules for menu, HUD, and result cards**
- [x] **Step 4: Run the focused formatting/layout tests**

  ```powershell
  .\gradlew.bat testDebugUnitTest --tests '*LocaleLayoutPolicyTest' --tests '*KavvoroNumberFormatTest' --tests '*TutorialCardLayoutTest'
  ```

---

### Task 6: Run the 19-language visual QA matrix

**Files:**
- Create: `tools/localization_visual_matrix.ps1`
- Create: `docs/localization-qa-matrix.md`
- Modify: `docs/release-polish-checklist.md`
- Modify: `README.md`

- [x] **Step 1: Write the deterministic emulator capture script**

  The script selects each locale, resets app state, captures Age Check, menu, Classic tutorial/HUD, Collection, and result screens, and writes files under a temporary QA directory. It must restore the emulator locale and window settings after capture.

- [x] **Step 2: Capture all 19 locales**

  Use the existing debug APK and the same representative states for every locale. Capture Arabic with RTL enabled and record any unsupported glyph or alignment issue.

  Accepted evidence: `build/localization-qa-isolated/` contains 19 matrix rows and 38 valid PNG captures after resetting the app before each locale; Arabic/Japanese/Korean selection drift was manually corrected.

- [ ] **Step 3: Review screenshots and fix every clipping/overflow/mixed-language issue**

  Add a row per locale and screen to `docs/localization-qa-matrix.md`; no locale is marked complete until all five states pass.

- [x] **Step 4: Run full tests and both builds**

  ```powershell
  .\gradlew.bat testDebugUnitTest assembleDebug
  .\gradlew.bat assembleRelease
  ```

- [x] **Step 5: Update README and checklist with current evidence and pending visual gate**

  Record the 19/19 automated capture and keep the manual/extended-state gate visible until those screenshots are reviewed.

---

### Task 7: Audit localized Brainball voice assets and finish documentation

**Files:**
- Create: `tools/verify_localized_voice_assets.py`
- Create: `tools/test_localized_voice_assets.py`
- Modify: `README.md`
- Modify: `docs/superpowers/plans/2026-08-22-release-polish-roadmap.md`
- Modify: `docs/release-polish-checklist.md`

- [x] **Step 1: Write the failing voice-inventory test**

  Assert that every supported language has the expected Brainball voice roster and that each file is non-empty and decodable.

- [x] **Step 2: Run the test to verify missing/invalid assets are reported**
- [x] **Step 3: Generate or repair only the missing locale assets**
- [x] **Step 4: Run the validator and document the exact inventory**
- [ ] **Step 5: Mark the 19-language audit complete only when text, layout, and voice checks pass**

---

## Verification gate

The stage is complete only when the strict catalog tests, layout tests, voice
inventory tests, full unit tests, debug build, release build, and the 19-row
visual QA matrix all pass. A language with unreviewed screenshots remains
`pending`, even if its unit tests pass.
