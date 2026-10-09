"""Refresh byte-identical screenshot copies grouped by screen size and density."""

from __future__ import annotations

import filecmp
import shutil
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from tools.screenshot_matrix import PAGE_CAPTURE_ROOT, SCREENSHOT_GROUPS, SCREENSIZE_CAPTURE_ROOT


PAGE_NAMES = {
    "launch": "00-launch.png",
    "age-check": "01-age-check.png",
    "home": "02-home.png",
    "settings": "03-settings.png",
    "settings/audio": "04-settings-audio.png",
    "settings/gameplay": "05-settings-gameplay.png",
    "settings/about": "06-settings-about.png",
    "language": "07-language.png",
    "settings-dialog": "08-settings-reset-confirmation.png",
    "play-mode": "09-mode-selection.png",
    "collection": "12-collection.png",
    "leaderboards": "13-leaderboards.png",
    "daily-rift-bonus": "14-daily-rift-bonus.png",
    "gameplay": "15-gameplay.png",
    "outcome": "16-outcome.png",
}


def screen_size_folder(profile_filename: str) -> str:
    profile = profile_filename.removeprefix("rift-challenges-").removesuffix(".png")
    for device in ("tablet-landscape", "tablet", "phone"):
        prefix = f"{device}-"
        if profile.startswith(prefix):
            return profile.removeprefix(prefix)
    raise ValueError(f"Unrecognized screenshot profile: {profile_filename}")


def page_filename(group: str, profile_filename: str) -> str:
    if group == "missions":
        if profile_filename.startswith("rift-challenges-"):
            return "11-rift-challenges.png"
        return "10-daily-missions.png"
    try:
        return PAGE_NAMES[group]
    except KeyError as error:
        raise ValueError(f"No ordered by-screensize name is defined for page: {group}") from error


def expected_screensize_files() -> dict[Path, Path]:
    """Map each screen-size mirror path to its canonical by-page source."""
    expected: dict[Path, Path] = {}
    for group, filenames in SCREENSHOT_GROUPS.items():
        for filename in filenames:
            source = PAGE_CAPTURE_ROOT / group / filename
            destination = (
                SCREENSIZE_CAPTURE_ROOT
                / screen_size_folder(filename)
                / page_filename(group, filename)
            )
            if destination in expected:
                raise ValueError(f"Screenshot mirror filename collision: {destination}")
            expected[destination] = source
    return expected


def sync_screenshots() -> tuple[int, int]:
    expected = expected_screensize_files()
    missing_sources = sorted(source for source in expected.values() if not source.is_file())
    if missing_sources:
        paths = ", ".join(str(path.relative_to(PROJECT_ROOT)) for path in missing_sources)
        raise FileNotFoundError(f"Cannot create screen-size mirrors; page captures are missing: {paths}")

    existing_pngs = set(SCREENSIZE_CAPTURE_ROOT.rglob("*.png")) if SCREENSIZE_CAPTURE_ROOT.exists() else set()
    stale_pngs = sorted(existing_pngs - set(expected))
    for path in stale_pngs:
        path.unlink()

    copied = 0
    for destination, source in sorted(expected.items()):
        destination.parent.mkdir(parents=True, exist_ok=True)
        if not destination.is_file() or not filecmp.cmp(source, destination, shallow=False):
            shutil.copy2(source, destination)
            copied += 1

    if SCREENSIZE_CAPTURE_ROOT.exists():
        directories = sorted(
            (path for path in SCREENSIZE_CAPTURE_ROOT.rglob("*") if path.is_dir()),
            key=lambda path: len(path.parts),
            reverse=True,
        )
        for directory in directories:
            try:
                directory.rmdir()
            except OSError:
                pass

    return len(expected), copied


def main() -> int:
    try:
        total, copied = sync_screenshots()
    except (FileNotFoundError, ValueError) as error:
        print(error, file=sys.stderr)
        return 1
    print(f"Synchronized {total} by-screensize images; copied or refreshed {copied}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
