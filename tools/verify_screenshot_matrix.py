"""Validate the approved eight-profile QA screenshot matrix and its PNG dimensions."""

from __future__ import annotations

import filecmp
import sys
from pathlib import Path

from PIL import Image

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from tools.screenshot_matrix import (
    PAGE_CAPTURE_ROOT,
    PROFILE_MATRIX,
    SCREENSHOT_GROUPS,
    SCREENSIZE_CAPTURE_ROOT,
)
from tools.sync_screenshot_views import expected_screensize_files


SCREENSHOT_ROOT = PAGE_CAPTURE_ROOT


def verify_matrix() -> list[str]:
    errors: list[str] = []
    checked = 0
    mirrors_checked = 0
    expected_profiles = {name: (width, height) for name, width, height, _density in PROFILE_MATRIX}

    for relative_directory, expected_names in SCREENSHOT_GROUPS.items():
        directory = SCREENSHOT_ROOT / relative_directory
        actual_names = {path.name for path in directory.glob("*.png")} if directory.is_dir() else set()
        missing = sorted(expected_names - actual_names)
        unexpected = sorted(actual_names - expected_names)
        errors.extend(f"{relative_directory}: missing screenshot {name}" for name in missing)
        errors.extend(f"{relative_directory}: unsupported or unexpected screenshot {name}" for name in unexpected)
        leftovers = sorted(path.name for path in directory.glob("*.tmp")) if directory.is_dir() else []
        errors.extend(f"{relative_directory}: temporary capture remains {name}" for name in leftovers)

        for name in sorted(expected_names & actual_names):
            path = directory / name
            profile_name = name.removeprefix("rift-challenges-")
            expected_size = expected_profiles.get(profile_name)
            if expected_size is None:
                errors.append(f"{relative_directory}/{name}: filename is not an approved profile")
                continue
            try:
                with Image.open(path) as image:
                    if image.format != "PNG":
                        errors.append(f"{relative_directory}/{name}: expected PNG, got {image.format}")
                    if image.size != expected_size:
                        errors.append(
                            f"{relative_directory}/{name}: expected {expected_size}, got {image.size}"
                        )
                    if image.width * image.height == 0:
                        errors.append(f"{relative_directory}/{name}: empty image")
                    checked += 1
            except OSError as error:
                errors.append(f"{relative_directory}/{name}: unreadable PNG ({error})")

    expected_mirrors = expected_screensize_files()
    actual_mirrors = set(SCREENSIZE_CAPTURE_ROOT.rglob("*.png")) if SCREENSIZE_CAPTURE_ROOT.is_dir() else set()
    missing_mirrors = sorted(set(expected_mirrors) - actual_mirrors)
    unexpected_mirrors = sorted(actual_mirrors - set(expected_mirrors))
    errors.extend(
        f"by-screensize: missing mirror {path.relative_to(SCREENSHOT_ROOT.parent)}"
        for path in missing_mirrors
    )
    errors.extend(
        f"by-screensize: unexpected screenshot {path.relative_to(SCREENSHOT_ROOT.parent)}"
        for path in unexpected_mirrors
    )
    for mirror, source in sorted(expected_mirrors.items()):
        if mirror not in actual_mirrors or not source.is_file():
            continue
        if not filecmp.cmp(source, mirror, shallow=False):
            errors.append(
                f"by-screensize/{mirror.relative_to(SCREENSIZE_CAPTURE_ROOT)}: differs from "
                f"by-page/{source.relative_to(SCREENSHOT_ROOT)}"
            )
        else:
            mirrors_checked += 1

    print(f"Checked {checked} screenshots across {len(SCREENSHOT_GROUPS)} UI capture groups.")
    print(f"Checked {mirrors_checked} by-screensize copies against their by-page originals.")
    return errors


def main() -> int:
    errors = verify_matrix()
    if errors:
        print("Screenshot matrix validation failed:")
        for error in errors:
            print(f"- {error}")
        return 1
    print("Screenshot matrix OK: every page has the eight approved profiles and matching screen-size copies.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
