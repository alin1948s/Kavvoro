# Screenshot capture tools

`tools/screenshot_matrix.py` is the single source of truth for the approved QA
matrix: seven portrait profiles and one native tablet landscape profile. All
device screenshot generators use only those profiles; Play Store asset size
checks remain separate because they validate a different deliverable.

| Profile | Resolution | Density | Use |
| --- | ---: | ---: | --- |
| `phone-720x1280-320dpi.png` | 720×1280 | 320 dpi | Compact phone, 360×640 dp |
| `phone-360x800-160dpi.png` | 360×800 | 160 dpi | Narrow/tall phone |
| `phone-1080x2400-420dpi.png` | 1080×2400 | 420 dpi | Reference phone, ~411×914 dp |
| `phone-480x854-160dpi.png` | 480×854 | 160 dpi | Wide/short phone |
| `tablet-600x1024-160dpi.png` | 600×1024 | 160 dpi | Compact tablet |
| `tablet-1600x2560-320dpi.png` | 1600×2560 | 320 dpi | 800×1280 dp tablet |
| `tablet-1536x2048-240dpi.png` | 1536×2048 | 240 dpi | ~1024×1365 dp tablet |
| `tablet-landscape-1920x1200-240dpi.png` | 1920×1200 | 240 dpi | Native landscape tablet |

## Full capture

From PowerShell at the repository root:

```powershell
.\tools\capture_recommended_screenshots.ps1
```

The script uses the connected phone emulator (default `emulator-5554`), starts
`Pixel_Tablet` on `emulator-5556` if needed, installs the existing debug APK,
captures Age Check and every navigable app page, then validates filenames and
image dimensions. Override serials or the tablet AVD with parameters when needed:

```powershell
.\tools\capture_recommended_screenshots.ps1 -PhoneSerial emulator-5554 -TabletSerial emulator-5556 -TabletAvd Pixel_Tablet
```

If a run stops on a portrait profile, continue from that profile without
recapturing earlier ones:

```powershell
.\tools\capture_recommended_screenshots.ps1 -FromPortraitProfile phone-480x854-160dpi.png
```

The page captures launch the game once per profile, record the startup sequence,
and use `ffmpeg` to select the frame where the MoonSol lines meet. This handles
the variable delay before Android hands off its system splash to the app. The
runner then reads Home accessibility positions in one hierarchy pass and
navigates between pages in the same app session. Screen-entry waits are kept
just above the app's 0.34-second transition animation; the 6-second gameplay
wait is needed to show the outcome state. `ffmpeg` must be available on `PATH`
for Launch captures.

The gameplay/outcome pass backs up and restores the app's `shared_prefs`, so
screenshot work does not change progress. Age Check backs up and restores only
the age profile. The full PowerShell runner carries each configured display
profile from Age Check through the app-page captures, avoiding repeated display
resets, APK installs, and emulator reboots. It resets both emulator displays
after the run. Run an individual capture script without `--keep-display` to
restore the emulator immediately.

## Run a capture group manually

```powershell
$env:ANDROID_SERIAL = "emulator-5554"
python .\tools\screenshot-capture\retake_age_check_matrix.py
python .\tools\screenshot-capture\retake_ui_pages_matrix.py --orientation portrait

$env:ANDROID_SERIAL = "emulator-5556"
python .\tools\screenshot-capture\retake_age_check_landscape.py
python .\tools\screenshot-capture\retake_home_landscape.py
python .\tools\screenshot-capture\retake_ui_pages_matrix.py --orientation landscape

python .\tools\sync_screenshot_views.py
python .\tools\verify_screenshot_matrix.py
```

`retake_home_matrix.py` remains available for a dedicated, verified Home-only
capture across the same seven portrait profiles. The `retake_settings_matrix.py`,
`retake_language_matrix.py`, and `retake_missions_matrix.py` wrappers call the
shared page navigator and use the same canonical profiles.

`localization_visual_matrix.ps1` is a 24-language content pass at the reference
profile (1080×2400@420 dpi), not an additional device-size matrix. The
`verify_store_assets.py` checks Play Store marketing dimensions and are likewise
independent of Android device screenshots.

## Capture policy

- Keep canonical app UI captures in descriptive page subdirectories under
  `screenshots/by-page/`. The generated `screenshots/by-screensize/` folders
  contain byte-identical copies grouped by resolution and density.
- The full capture runner refreshes the by-screen-size copies. After a manual
  page capture, run `python .\tools\sync_screenshot_views.py` before verifying.
- The screenshot matrix validator requires exactly the eight approved profiles
  in each app-page group and checks the PNG dimensions. The full capture script
  removes stale profile PNGs from managed groups before verification.
- The app's branded `launch` page is app-page evidence; do not include OS launcher
  imagery, black transition frames, logs, or temporary files.
- Keep reusable scripts and fixtures here; do not leave one-off crop scripts,
  previews, or generated intermediates in the repository.
