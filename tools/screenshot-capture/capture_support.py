"""Shared, machine-independent configuration for screenshot capture tools."""

from __future__ import annotations

import os
import re
import subprocess
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parents[1]
PACKAGE = "com.moonsolstudios.kavvoro"
APK = PROJECT_ROOT / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
PRIVACY_XML = SCRIPT_DIR / "fixtures" / "privacy_profile.xml"

# (filename, width_px, height_px, density_dpi)
# Logical phones keep 160dpi so 360px == 360dp. High-res phones/tablets use a
# realistic dpi so 1080x2400 is ~411dp (not 1080dp) and 1600x2560 is 800dp
# (Pixel Tablet / emulator xhdpi), not a fake 1600dp desktop canvas.
def target_filename(kind: str, width: int, height: int, density_dpi: int) -> str:
    return f"{kind}-{width}x{height}-{density_dpi}dpi.png"


TARGETS = tuple(
    (target_filename(kind, width, height, density), width, height, density)
    for kind, width, height, density in (
        ("phone", 360, 800, 160),
        ("phone", 412, 915, 160),
        ("phone", 480, 854, 160),
        ("phone", 720, 1280, 320),
        ("phone", 1080, 2400, 420),
        ("tablet", 600, 1024, 160),
        ("tablet", 800, 1280, 160),
        ("tablet", 1024, 1366, 160),
        ("tablet", 1200, 1920, 240),
        ("tablet", 1536, 2048, 240),
        ("tablet", 1600, 2560, 320),
    )
)


def target_dp(width_px: int, density_dpi: int) -> float:
    return width_px * 160.0 / float(density_dpi)


def _display_rotation() -> int | None:
    """Return the current display rotation 0..3, or None if unknown."""
    try:
        dump = subprocess.check_output(
            [ADB, "shell", "dumpsys", "window", "displays"],
            timeout=12,
        ).decode("utf-8", "replace")
        match = re.search(r"mCurrentRotation=ROTATION_(\d+)", dump)
        if match:
            degrees = int(match.group(1))
            return {0: 0, 90: 1, 180: 2, 270: 3}.get(degrees)
    except Exception:
        pass
    try:
        out = subprocess.check_output(
            [ADB, "shell", "wm", "user-rotation"],
            timeout=8,
        ).decode("utf-8", "replace")
        match = re.search(r"(\d+)", out)
        if match:
            return int(match.group(1)) % 4
    except Exception:
        pass
    return None


def restore_emulator_display(run) -> None:
    """Undo capture overrides so a tablet can auto-rotate on all 4 sides."""
    run("shell", "wm", "size", "reset")
    run("shell", "wm", "density", "reset")
    try:
        # Pixel Tablet defaults to ignoreOrientationRequest=true, which
        # letterboxes portrait as a landscape thumbnail and rejects 270°.
        run("shell", "cmd", "window", "set-ignore-orientation-request", "false")
    except Exception:
        pass
    try:
        # Match Pixel Tablet natural landscape so auto-rotate does not snap
        # back to a portrait thumbnail inside the landscape emulator window.
        subprocess.run(
            [ADB, "emu", "sensor", "set", "acceleration", "0:9.77631:0.811393"],
            check=False,
            timeout=5,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
    except Exception:
        pass
    run("shell", "settings", "put", "system", "accelerometer_rotation", "1")
    try:
        run("shell", "wm", "user-rotation", "free", "0")
    except Exception:
        run("shell", "settings", "put", "system", "user_rotation", "0")
    for _ in range(4):
        rotation = _display_rotation()
        if rotation in (0, None):
            break
        try:
            subprocess.run(
                [ADB, "emu", "rotate"],
                check=False,
                timeout=5,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
            )
        except Exception:
            break
        try:
            subprocess.run(
                [ADB, "emu", "sensor", "set", "acceleration", "0:9.77631:0.811393"],
                check=False,
                timeout=5,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
            )
            run("shell", "wm", "user-rotation", "free", "0")
        except Exception:
            pass
    run("shell", "am", "force-stop", PACKAGE)
    run("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")


def _adb_executable() -> Path:
    configured_sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if configured_sdk:
        sdk_root = Path(configured_sdk)
    else:
        local_app_data = os.environ.get("LOCALAPPDATA")
        sdk_root = Path(local_app_data) / "Android" / "Sdk" if local_app_data else Path("Android/Sdk")
    executable = "adb.exe" if os.name == "nt" else "adb"
    return sdk_root / "platform-tools" / executable


ADB = str(_adb_executable())
