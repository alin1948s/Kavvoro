"""Remove stale device-profile and temporary PNGs from managed screenshot groups."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from tools.screenshot_matrix import PAGE_CAPTURE_ROOT, SCREENSHOT_GROUPS


SCREENSHOT_ROOT = PAGE_CAPTURE_ROOT


def stale_paths() -> list[Path]:
    stale: list[Path] = []
    for group, expected in SCREENSHOT_GROUPS.items():
        directory = SCREENSHOT_ROOT / group
        if not directory.is_dir():
            continue
        stale.extend(path for path in directory.glob("*.png") if path.name not in expected)
        stale.extend(directory.glob("*.tmp"))
    return sorted(stale)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--apply",
        action="store_true",
        help="delete unsupported screenshot PNGs and interrupted temporary captures",
    )
    args = parser.parse_args()
    paths = stale_paths()
    if not paths:
        print("Screenshot groups contain no stale profiles or temporary captures.")
        return 0
    for path in paths:
        print(f"{'Removing' if args.apply else 'Would remove'} {path.relative_to(PROJECT_ROOT)}")
        if args.apply:
            path.unlink()
    if not args.apply:
        print("Preview only. Pass --apply to remove these generated screenshot files.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
