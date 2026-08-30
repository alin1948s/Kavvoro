# Localization QA matrix

Scope: the 24 selectable `KavvoroLanguage` values. `SYSTEM` is intentionally
excluded because it resolves to the device locale.

## Automated evidence

- Strict catalog inventory: 24/24 languages, 404 rendered keys per locale.
- Placeholder parity and fallback checks: passing in `LocalizationCatalogTest`.
- Layout and number-format tests: passing in `LocaleLayoutPolicyTest`.
- The repository validation commands are documented in the root README and are
  rerun before structural changes are merged.

## Visual evidence

The repeatable capture tool is [localization_visual_matrix.ps1](../tools/localization_visual_matrix.ps1).
It deterministically selects each of the 24 canonical languages, captures the
Home menu and Classic entry/HUD, and writes PNG/CSV/Markdown artifacts under
`build/localization-qa/`. The first exploratory run in
`build/localization-qa-final/` was rejected because some iterations drifted into
the wrong state and showed an Android "isn't responding" dialog. The accepted
isolated run is in `build/localization-qa-isolated/`: 19 CSV rows and 38 valid
PNG captures. That baseline predates Czech, Swedish, Finnish, Thai, and
Traditional Chinese support. The tool no longer taps position-dependent rows;
it injects the debug locale preference and restarts the app for every capture.

Manual visual checks completed on `Medium_Phone`:

- English: Home menu and Classic HUD.
- Polish: language selector, Home menu, and Classic HUD.
- Arabic: language selector, RTL Home menu, and RTL Classic HUD.

The Home-menu review below covers all 19 historical locale rows. Representative
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
| Czech | cs | PASS | Pending | Pending | Pending |
| Swedish | sv | PASS | Pending | Pending | Pending |
| Finnish | fi | PASS | Pending | Pending | Pending |
| Turkish | tr | PASS | PASS | PASS | Pending |
| Russian | ru | PASS | PASS | PASS | Pending |
| Ukrainian | uk | PASS | PASS | PASS | Pending |
| Arabic | ar | PASS | PASS | PASS | Pending |
| Hindi | hi | PASS | PASS | PASS | Pending |
| Thai | th | PASS | Pending | Pending | Pending |
| Indonesian | id | PASS | PASS | PASS | Pending |
| Vietnamese | vi | PASS | PASS | PASS | Pending |
| Japanese | ja | PASS | PASS | PASS | Pending |
| Korean | ko | PASS | PASS | PASS | Pending |
| Simplified Chinese | zh | PASS | PASS | PASS | Pending |
| Traditional Chinese | zh_tw | PASS | Pending | Pending | Pending |

The audit is not marked complete until the extended states and full manual
review have evidence on a stable emulator or physical device.
