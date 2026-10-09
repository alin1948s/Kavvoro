# Screenshot evidence

Canonical app UI screenshots are organized by page in `by-page/`. Identical
copies are organized by resolution and density in `by-screensize/`; the full
capture runner keeps both views in sync. The canonical eight-profile matrix is
defined in `tools/screenshot_matrix.py`:

- Portrait: 720×1280@320 dpi, 360×800@160 dpi, 1080×2400@420 dpi,
  480×854@160 dpi, 600×1024@160 dpi, 1600×2560@320 dpi, and
  1536×2048@240 dpi.
- Native tablet landscape: 1920×1200@240 dpi.

The matrix covers Launch, Age Check, Home, mode selection, language, all four Settings
tabs, the reset confirmation dialog, Daily Missions, Rift Challenges, Collection,
Leaderboards, the Daily Rift Bonus popup, Gameplay, and Outcome. Each image uses
the corresponding profile and dimensions in `by-page/`; the matching files in
`by-screensize/` use a zero-padded page number and name within a
resolution-and-density folder (for example, `00-launch.png`, `01-age-check.png`, `02-home.png`,
`03-settings.png`). Run
`tools/capture_recommended_screenshots.ps1` to regenerate all captures and
validate the matrix.

The capture scripts navigate the app's visible UI. Gameplay and outcome
captures preserve and restore the device's app preferences. Age Check captures
restore the original age profile. Emulator resolution, density, and orientation
are reset at the end.

Only app UI evidence belongs in these groups. The branded app Launch intro is
included; Android OS launcher captures, transition frames, logs, and temporary
images are not page screenshots. Run
`python tools/sync_screenshot_views.py` after manual captures to refresh the
screen-size view; `tools/verify_screenshot_matrix.py` checks that every mirror
is byte-identical to its page capture.
