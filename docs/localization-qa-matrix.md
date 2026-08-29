# Localization QA matrix

Scope: the 19 selectable `KavvoroLanguage` values. `SYSTEM` is intentionally
excluded because it resolves to the device locale.

## Automated evidence

- Strict catalog inventory: 19/19 languages, 404 rendered keys per locale.
- Placeholder parity and fallback checks: passing in `LocalizationCatalogTest`.
- Layout and number-format tests: passing in `LocaleLayoutPolicyTest`.
- Full unit suite at the latest completed run: 57 tests, 0 failures.
- `assembleDebug`: successful.
- `assembleRelease`: successful.

## Visual evidence

The repeatable capture tool is [localization_visual_matrix.ps1](../tools/localization_visual_matrix.ps1).
It captures the Home menu and Classic entry/HUD and writes PNG/CSV/Markdown
artifacts under `build/localization-qa/`. The first exploratory run in
`build/localization-qa-final/` was rejected because some iterations drifted into
the wrong state and showed an Android "isn't responding" dialog. The accepted
isolated run is in `build/localization-qa-isolated/`: 19 CSV rows and 38 valid
PNG captures, with an app restart before each locale and manual correction of
the Arabic/Japanese/Korean selection rows.

Manual visual checks completed on `Medium_Phone`:

- English: Home menu and Classic HUD.
- Polish: language selector, Home menu, and Classic HUD.
- Arabic: language selector, RTL Home menu, and RTL Classic HUD.

The Home-menu review below covers all 19 captured locale rows. Representative
Classic HUD review covers English, Polish, Arabic, Japanese, Korean, and
Chinese; Tutorial, Collection, and result states remain manual checkpoints
because their state transitions are gameplay-dependent.

| Language | Code | Catalog | Automated Home/HUD capture | Home menu review | Extended states |
|---|---:|---|---|---|---|
| English | en | PASS | PASS | PASS | Pending |
| Romanian | ro | PASS | PASS | PASS | Pending |
| Spanish | es | PASS | PASS | PASS | Pending |
| French | fr | PASS | PASS | PASS | Pending |
| German | de | PASS | PASS | PASS | Pending |
| Italian | it | PASS | PASS | PASS | Pending |
| Portuguese | pt | PASS | PASS | PASS | Pending |
| Dutch | nl | PASS | PASS | PASS | Pending |
| Polish | pl | PASS | PASS | PASS | Pending |
| Turkish | tr | PASS | PASS | PASS | Pending |
| Russian | ru | PASS | PASS | PASS | Pending |
| Ukrainian | uk | PASS | PASS | PASS | Pending |
| Arabic | ar | PASS | PASS | PASS | Pending |
| Hindi | hi | PASS | PASS | PASS | Pending |
| Indonesian | id | PASS | PASS | PASS | Pending |
| Vietnamese | vi | PASS | PASS | PASS | Pending |
| Japanese | ja | PASS | PASS | PASS | Pending |
| Korean | ko | PASS | PASS | PASS | Pending |
| Chinese | zh | PASS | PASS | PASS | Pending |

The audit is not marked complete until the extended states and full manual
review have evidence on a stable emulator or physical device.
