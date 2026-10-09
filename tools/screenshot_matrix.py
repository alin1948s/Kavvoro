"""Canonical QA device profiles and screenshot directory manifest."""

from __future__ import annotations

from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
SCREENSHOT_ROOT = PROJECT_ROOT / "screenshots"
PAGE_CAPTURE_ROOT = SCREENSHOT_ROOT / "by-page"
SCREENSIZE_CAPTURE_ROOT = SCREENSHOT_ROOT / "by-screensize"


def target_filename(kind: str, width: int, height: int, density_dpi: int) -> str:
    return f"{kind}-{width}x{height}-{density_dpi}dpi.png"


PORTRAIT_TARGETS = tuple(
    (target_filename(kind, width, height, density), width, height, density)
    for kind, width, height, density in (
        ("phone", 720, 1280, 320),       # 360 x 640 dp, compact
        ("phone", 360, 800, 160),        # narrow/tall
        ("phone", 1080, 2400, 420),      # canonical reference, ~411 x 914 dp
        ("phone", 480, 854, 160),        # wider/shorter
        ("tablet", 600, 1024, 160),      # compact tablet
        ("tablet", 1600, 2560, 320),     # 800 x 1280 dp
        ("tablet", 1536, 2048, 240),     # ~1024 x 1365 dp
    )
)

LANDSCAPE_TARGET = (
    target_filename("tablet-landscape", 1920, 1200, 240),
    1920,
    1200,
    240,
)

PROFILE_MATRIX = (*PORTRAIT_TARGETS, LANDSCAPE_TARGET)
PROFILE_NAMES = frozenset(target[0] for target in PROFILE_MATRIX)

PORTRAIT_NAMES = frozenset(target[0] for target in PORTRAIT_TARGETS)
LANDSCAPE_NAME = LANDSCAPE_TARGET[0]
MISSION_RIFT_NAMES = frozenset(f"rift-challenges-{name}" for name in PROFILE_NAMES)

SCREENSHOT_GROUPS = {
    "launch": PROFILE_NAMES,
    "age-check": PROFILE_NAMES,
    "home": PROFILE_NAMES,
    "play-mode": PROFILE_NAMES,
    "language": PROFILE_NAMES,
    "settings": PROFILE_NAMES,
    "settings/audio": PROFILE_NAMES,
    "settings/gameplay": PROFILE_NAMES,
    "settings/about": PROFILE_NAMES,
    "settings-dialog": PROFILE_NAMES,
    "missions": PROFILE_NAMES | MISSION_RIFT_NAMES,
    "collection": PROFILE_NAMES,
    "leaderboards": PROFILE_NAMES,
    "daily-rift-bonus": PROFILE_NAMES,
    "gameplay": PROFILE_NAMES,
    "outcome": PROFILE_NAMES,
}
