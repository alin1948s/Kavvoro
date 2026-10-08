"""Capture the native tablet landscape Age Check layout."""

from __future__ import annotations

import subprocess
import time

from capture_support import ADB, APK, PACKAGE, PROJECT_ROOT, restore_emulator_display
from retake_age_check_11 import (
    backup_age_profile,
    capture_png,
    has_age_picker_accessibility_node,
    image_size,
    is_age_check,
    remove_age_profile,
    restore_age_profile,
    run,
)
from retake_home_landscape import dismiss_fullscreen_prompt_if_shown


WIDTH = 1920
HEIGHT = 1200
DENSITY = 240
OUTPUT = PROJECT_ROOT / "screenshots" / "age-check" / "tablet-landscape-1920x1200-240dpi.png"


def wait_for_landscape_viewport(timeout: float = 20.0) -> None:
    deadline = time.monotonic() + timeout
    last_seen: object = None
    stable_frames = 0
    while time.monotonic() < deadline:
        try:
            last_seen = image_size(capture_png())
            if last_seen == (WIDTH, HEIGHT):
                stable_frames += 1
                if stable_frames >= 3:
                    return
            else:
                stable_frames = 0
        except Exception as error:
            last_seen = f"error: {error}"
            stable_frames = 0
        time.sleep(0.3)
    raise RuntimeError(f"Landscape viewport did not settle at {WIDTH}x{HEIGHT}; last_seen={last_seen}")


def wait_for_landscape_age_check(timeout: float = 25.0) -> bytes:
    deadline = time.monotonic() + timeout
    next_accessibility_check = 0.0
    accessibility_node_found = False
    while time.monotonic() < deadline:
        png = capture_png()
        if image_size(png) != (WIDTH, HEIGHT):
            time.sleep(0.3)
            continue
        now = time.monotonic()
        if is_age_check(png) and now >= next_accessibility_check:
            accessibility_node_found = has_age_picker_accessibility_node()
            next_accessibility_check = time.monotonic() + 2.0
        if is_age_check(png) and accessibility_node_found:
            return png
        time.sleep(0.3)
    raise RuntimeError("Tablet landscape Age Check was not visible before the screenshot timeout")


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    run("install", "-r", str(APK), timeout=90.0)
    original_age_profile = backup_age_profile()
    remove_age_profile()
    try:
        run("shell", "settings", "put", "system", "accelerometer_rotation", "0")
        # Pixel Tablet's natural display orientation is landscape; keep the
        # natural rotation so the full-sensor activity receives a landscape config.
        run("shell", "settings", "put", "system", "user_rotation", "0")
        run("shell", "input", "keyevent", "224")
        run("shell", "wm", "dismiss-keyguard")
        run("shell", "wm", "size", f"{WIDTH}x{HEIGHT}")
        run("shell", "wm", "density", str(DENSITY))
        wait_for_landscape_viewport()
        run("shell", "am", "force-stop", PACKAGE)
        run("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
        time.sleep(3.0)
        dismiss_fullscreen_prompt_if_shown()
        screenshot = wait_for_landscape_age_check()
        OUTPUT.write_bytes(screenshot)
        print(f"PASS {OUTPUT.name} ({WIDTH}x{HEIGHT}@{DENSITY}dpi), {len(screenshot)} bytes")
    finally:
        try:
            restore_emulator_display(lambda *args: run(*args))
        finally:
            try:
                restore_age_profile(original_age_profile)
            finally:
                run("shell", "rm", "-f", "/sdcard/kavvoro_age_check.xml", "/sdcard/kavvoro_window.xml")


if __name__ == "__main__":
    main()
