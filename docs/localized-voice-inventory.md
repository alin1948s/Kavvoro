# Localized Brainball voice inventory

The release APK ships 50 Brainball selection voices for each of the 19
selectable languages. The files live in `app/src/main/res/raw/` and are
validated by `tools/verify_localized_voice_assets.py`.

| Locale | Files |
|---|---:|
| en | 50 |
| ro | 50 |
| es | 50 |
| fr | 50 |
| de | 50 |
| it | 50 |
| pt | 50 |
| nl | 50 |
| pl | 50 |
| tr | 50 |
| ru | 50 |
| uk | 50 |
| ar | 50 |
| hi | 50 |
| id | 50 |
| vi | 50 |
| ja | 50 |
| ko | 50 |
| zh | 50 |

The validator checks the exact roster, non-empty files, and the OGG container
signature. Full audio decoding/playback remains part of the device audio pass.
