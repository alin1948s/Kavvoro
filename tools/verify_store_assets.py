"""Validate the final Google Play asset package without modifying it."""

from __future__ import annotations

import argparse
import json
import shutil
import subprocess
from dataclasses import dataclass, field
from pathlib import Path

from PIL import Image


PHONE_SCREENSHOT_NAMES = [
    "01-meet-kavvoro.png",
    "02-tap-the-rift.png",
    "03-choose-your-chaos.png",
    "04-collect-50.png",
    "05-advanced-chaos.png",
    "06-trigger-powers.png",
    "07-bend-space.png",
]

TABLET_SCREENSHOT_NAMES = PHONE_SCREENSHOT_NAMES
TABLET_SPECS = {
    "tablet-7": (1200, 1920),
    "tablet-10": (1600, 2560),
}


@dataclass
class StoreAssetReport:
    errors: list[str] = field(default_factory=list)
    phone_screenshot_count: int = 0
    tablet_screenshot_counts: dict[str, int] = field(default_factory=dict)
    ready_tablet_sets: list[str] = field(default_factory=list)
    withheld_tablet_sets: list[str] = field(default_factory=list)


def validate_store_assets(store_root: Path) -> StoreAssetReport:
    report = StoreAssetReport()
    store_root = Path(store_root)

    raster_specs = {
        "graphics/kavvoro-play-icon-512.png": (512, 512),
        "graphics/kavvoro-feature-1024x500.jpg": (1024, 500),
        "video/kavvoro-youtube-thumbnail-1280x720.jpg": (1280, 720),
    }
    for relative_path, expected_size in raster_specs.items():
        _check_raster(store_root / relative_path, expected_size, report)

    phone_dir = store_root / "phone"
    phone_pngs = sorted(path.name for path in phone_dir.glob("*.png"))
    report.phone_screenshot_count = len(phone_pngs)
    _check_exact_names(phone_pngs, PHONE_SCREENSHOT_NAMES, "phone screenshots", report)
    for name in PHONE_SCREENSHOT_NAMES:
        _check_raster(phone_dir / name, (1242, 2208), report, max_bytes=8 * 1024 * 1024)

    video_specs = {
        "video/kavvoro-play-trailer-vertical-1080x1920.mp4": (1080, 1920),
        "video/kavvoro-play-trailer-youtube-1920x1080.mp4": (1920, 1080),
    }
    for relative_path, expected_size in video_specs.items():
        _check_video(store_root / relative_path, expected_size, report)

    for tablet_name, expected_size in TABLET_SPECS.items():
        _check_tablet_set(store_root / tablet_name, tablet_name, expected_size, report)

    return report


def _check_exact_names(
    actual: list[str], expected: list[str], label: str, report: StoreAssetReport
) -> None:
    actual_set = set(actual)
    expected_set = set(expected)
    for name in sorted(expected_set - actual_set):
        report.errors.append(f"Missing {label} file: {name}")
    for name in sorted(actual_set - expected_set):
        report.errors.append(f"Unexpected {label} file: {name}")


def _check_raster(
    path: Path,
    expected_size: tuple[int, int],
    report: StoreAssetReport,
    max_bytes: int | None = None,
) -> None:
    if not path.is_file():
        report.errors.append(f"Missing raster asset: {path}")
        return
    if max_bytes is not None and path.stat().st_size >= max_bytes:
        report.errors.append(f"Raster asset exceeds {max_bytes} bytes: {path}")
    try:
        with Image.open(path) as image:
            if image.size != expected_size:
                report.errors.append(
                    f"Wrong dimensions for {path}: expected {expected_size}, got {image.size}"
                )
    except OSError as error:
        report.errors.append(f"Unreadable raster asset {path}: {error}")


def _check_video(path: Path, expected_size: tuple[int, int], report: StoreAssetReport) -> None:
    if not path.is_file():
        report.errors.append(f"Missing video asset: {path}")
        return
    ffprobe = shutil.which("ffprobe")
    if ffprobe is None:
        report.errors.append("ffprobe is required to validate video assets")
        return
    command = [
        ffprobe,
        "-v",
        "error",
        "-select_streams",
        "v:0",
        "-show_entries",
        "stream=width,height:format=duration",
        "-of",
        "json",
        str(path),
    ]
    try:
        result = subprocess.run(command, check=True, capture_output=True, text=True)
        data = json.loads(result.stdout)
        stream = data.get("streams", [{}])[0]
        actual_size = (stream.get("width"), stream.get("height"))
        if actual_size != expected_size:
            report.errors.append(
                f"Wrong dimensions for {path}: expected {expected_size}, got {actual_size}"
            )
        duration = float(data.get("format", {}).get("duration", 0.0))
        if duration <= 0.0:
            report.errors.append(f"Video has no positive duration: {path}")
    except (OSError, subprocess.CalledProcessError, ValueError, KeyError, IndexError) as error:
        report.errors.append(f"Unreadable video asset {path}: {error}")


def _check_tablet_set(
    directory: Path,
    tablet_name: str,
    expected_size: tuple[int, int],
    report: StoreAssetReport,
) -> None:
    pngs = sorted(path.name for path in directory.glob("*.png"))
    report.tablet_screenshot_counts[tablet_name] = len(pngs)
    if not pngs:
        if (directory / "NOT_READY.md").is_file():
            report.withheld_tablet_sets.append(tablet_name)
        else:
            report.errors.append(
                f"{tablet_name} has no screenshots and no NOT_READY.md status file"
            )
        return

    if (directory / "NOT_READY.md").is_file():
        report.errors.append(f"{tablet_name} contains screenshots but is still marked NOT_READY")
    _check_exact_names(pngs, TABLET_SCREENSHOT_NAMES, f"{tablet_name} screenshots", report)
    for name in TABLET_SCREENSHOT_NAMES:
        _check_raster(directory / name, expected_size, report, max_bytes=8 * 1024 * 1024)
    if not report.errors:
        report.ready_tablet_sets.append(tablet_name)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "store_root",
        nargs="?",
        type=Path,
        default=Path(__file__).resolve().parents[1] / "store-assets" / "final",
    )
    args = parser.parse_args()
    report = validate_store_assets(args.store_root)
    if report.errors:
        for error in report.errors:
            print(f"ERROR: {error}")
        return 1
    print(f"Validated {report.phone_screenshot_count} phone screenshots and the core media package.")
    if report.ready_tablet_sets:
        print(f"Ready tablet sets: {', '.join(report.ready_tablet_sets)}")
    if report.withheld_tablet_sets:
        print(f"Withheld tablet sets: {', '.join(report.withheld_tablet_sets)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
