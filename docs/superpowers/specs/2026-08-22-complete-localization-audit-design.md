# Complete Localization Audit Design

## Objective

Make every selectable Kavvoro language complete and verifiable across the
rendered APK UI. A non-English locale must not silently fall back to English for
ordinary interface copy. Brand and gameplay terms may remain untranslated only
when explicitly allowlisted.

## Supported languages

The audit covers all 19 selectable languages:

- English (`en`)
- Romanian (`ro`)
- Spanish (`es`)
- French (`fr`)
- German (`de`)
- Italian (`it`)
- Portuguese (`pt`)
- Dutch (`nl`)
- Polish (`pl`)
- Turkish (`tr`)
- Russian (`ru`)
- Ukrainian (`uk`)
- Arabic (`ar`)
- Hindi (`hi`)
- Indonesian (`id`)
- Vietnamese (`vi`)
- Japanese (`ja`)
- Korean (`ko`)
- Simplified Chinese (`zh`)

`SYSTEM` remains a locale-selection mode and is not a translation target.

## Scope

The strict catalog covers every string rendered through `KavvoroI18n`, the
tutorial catalog, generated level titles, gameplay status labels, curse and
power ribbons, Age Check, menu, Collection, leaderboard, purchase, result, and
privacy flows.

The audit does not translate proper names, legal third-party product names, or
the explicitly allowlisted terms `Kavvoro`, `Brainball`, `Rift`, `HYPE`,
`Classic`, `Chaos`, `Google Play`, `Firebase`, and `AdMob`.

Store-listing localization is outside this APK-localization change. Existing
localized Brainball voice assets are inventoried but are not regenerated unless
the inventory test finds a missing supported-language file.

## Architecture

`KavvoroI18n` remains the public lookup surface. The current permissive `row`
builder is replaced by a strict catalog assembled from locale-specific maps.
Each selectable language receives an explicit value for every rendered key.
Missing values fail tests and use English only as a defensive runtime fallback
for corrupt or unknown keys, never as accepted catalog data.

The locale maps are split into one Kotlin file per selectable language so each
translation can be reviewed and changed independently without editing one
multi-thousand-line argument list:

`EnTranslations.kt`, `RoTranslations.kt`, `EsTranslations.kt`,
`FrTranslations.kt`, `DeTranslations.kt`, `ItTranslations.kt`,
`PtTranslations.kt`, `NlTranslations.kt`, `PlTranslations.kt`,
`TrTranslations.kt`, `RuTranslations.kt`, `UkTranslations.kt`,
`ArTranslations.kt`, `HiTranslations.kt`, `IdTranslations.kt`,
`ViTranslations.kt`, `JaTranslations.kt`, `KoTranslations.kt`, and
`ZhTranslations.kt`.

`SYSTEM` has no translation file. `LocalizationCatalog` is the only
assembler: it consumes the 19 maps, validates exact key coverage and
placeholder parity, and exposes immutable per-language snapshots. No grouped
translation files are part of this architecture.

Every locale map consumes the same immutable key inventory. Catalog assembly
rejects missing keys, extra keys, blank values, invalid format placeholders,
and accidental English equality outside the allowlist.

## Translation rules

- Preserve `%s`, `%d`, and other format placeholders exactly by type and count.
- Preserve intentional line breaks and punctuation semantics.
- Translate the surrounding sentence naturally; do not perform word-for-word
  transliteration of English UI grammar.
- Use short labels where controls have constrained width.
- Keep the approved brand-term allowlist unchanged.
- Arabic copy uses natural Arabic sentence order and is rendered with RTL-aware
  alignment; embedded Latin brand terms remain isolated and readable.
- Japanese, Korean, and Chinese copy avoids unnecessary spaces and uses native
  punctuation where practical.
- Polish, German, Russian, Ukrainian, Hindi, and Vietnamese receive intentional
  short variants for narrow menu and result-card labels.

## Automated verification

Tests must prove:

1. The rendered-key inventory exactly matches the strict catalog inventory.
2. Every selectable language has a nonblank explicit translation for every key.
3. No non-English translation equals the English source unless the key or value
   is explicitly allowlisted as a brand/product term.
4. Placeholder signatures match English exactly.
5. Tutorial selectors, generated titles, curse ribbons, and power ribbons only
   emit keys in the strict inventory.
6. Decimal formatting uses the active locale for representative dot, comma,
   Arabic, and Indic locales.
7. The language picker contains exactly the 19 audited languages plus `SYSTEM`.
8. Every supported Brainball voice directory contains the required roster files.

The new completeness tests are written first and must fail against the current
permissive catalog before production translations are changed.

## Visual verification

For each of the 19 languages, capture and inspect these representative states:

- Age Check;
- main menu;
- Classic tutorial card and HUD;
- Collection/selected Brainball card;
- interrupted or completed result card.

The audit checks clipping, ellipses, overlap, placeholder leakage, unsupported
glyphs, decimal separators, and mixed English copy. Arabic additionally checks
RTL alignment and mixed-direction brand terms. Screenshots are QA artifacts and
are not Play Store assets.

When a translation cannot fit safely, prefer a shorter natural translation or
measured wrapping. Font-size reductions are the last resort and must not make
one locale visibly inferior.

## Failure handling

Unknown runtime keys continue to return their source text to avoid crashes, but
tests fail if any rendered path relies on that behavior. A missing locale entry
is a build/test failure. Format-placeholder mismatches are rejected before the
string can reach `String.format`.

## Documentation and completion criteria

README is the source of truth and records the audit as in progress until all
checks pass. The release roadmap and polish checklist receive the 19-language
matrix and the final verification evidence.

The work is complete only when:

- strict completeness tests pass for all 19 languages;
- focused and full unit tests pass;
- debug and release builds succeed;
- every representative locale screenshot has been reviewed;
- no unapproved English fallback or visible layout defect remains;
- README no longer labels the audit as in progress.
