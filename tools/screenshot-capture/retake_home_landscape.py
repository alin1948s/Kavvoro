"""Capture the native tablet landscape Home profile."""

from __future__ import annotations

import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

from capture_support import ADB, PACKAGE, PROJECT_ROOT, restore_emulator_display
from retake_home_11 import capture_png, ensure_privacy_profile, verify_home_screen


WIDTH = 1920
HEIGHT = 1200
DENSITY = 240
OUTPUT = PROJECT_ROOT / "screenshots" / "home" / "tablet-landscape-1920x1200-240dpi.png"


def run_adb(*args: str, timeout: float = 30.0) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )


def current_image_size(png_bytes: bytes) -> tuple[int, int]:
    import io

    from PIL import Image

    with Image.open(io.BytesIO(png_bytes)) as image:
        return image.size


def wait_for_landscape_viewport(timeout: float = 20.0) -> None:
    deadline = time.monotonic() + timeout
    last_seen: object = None
    while time.monotonic() < deadline:
        try:
            last_seen = current_image_size(capture_png())
            if last_seen == (WIDTH, HEIGHT):
                return
        except Exception as error:
            last_seen = f"error: {error}"
        time.sleep(0.3)
    raise RuntimeError(f"Landscape viewport did not settle at {WIDTH}x{HEIGHT}; last_seen={last_seen}")


def dismiss_fullscreen_prompt_if_shown() -> bool:
    """Accept Android's one-time immersive-mode dialog without blind taps."""
    try:
        run_adb("shell", "uiautomator", "dump", "/sdcard/kavvoro_window.xml", timeout=15.0)
        xml_bytes = run_adb("shell", "cat", "/sdcard/kavvoro_window.xml", timeout=8.0).stdout
        root = ET.fromstring(xml_bytes.decode("utf-8", "replace"))
        prompt_visible = any(
            "viewing full screen" in node.attrib.get("text", "").lower()
            for node in root.iter()
        )
        if not prompt_visible:
            return False

        for node in root.iter():
            label = (node.attrib.get("text") or node.attrib.get("content-desc") or "").strip().lower()
            bounds = node.attrib.get("bounds", "")
            if label == "got it" and bounds:
                coordinates = [int(part) for part in bounds.replace("][", ",").strip("[]").split(",")]
                if len(coordinates) == 4:
                    x = (coordinates[0] + coordinates[2]) // 2
                    y = (coordinates[1] + coordinates[3]) // 2
                    run_adb("shell", "input", "tap", str(x), str(y))
                    time.sleep(1.0)
                    return True
        raise RuntimeError("Full-screen prompt is visible but its Got it button bounds were not found")
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
        return False


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    run_adb("shell", "settings", "put", "global", "stay_on_while_plugged_in", "3")
    run_adb("shell", "input", "keyevent", "224")
    run_adb("shell", "wm", "dismiss-keyguard")
    run_adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    run_adb("shell", "settings", "put", "system", "user_rotation", "1")
    run_adb("shell", "wm", "size", f"{WIDTH}x{HEIGHT}")
    run_adb("shell", "wm", "density", str(DENSITY))

    try:
        wait_for_landscape_viewport()
        ensure_privacy_profile()
        run_adb("shell", "am", "force-stop", PACKAGE)
        run_adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
        time.sleep(3.0)
        dismiss_fullscreen_prompt_if_shown()

        deadline = time.monotonic() + 15.0
        last_reason = "timeout"
        while time.monotonic() < deadline:
            png_bytes = capture_png()
            if current_image_size(png_bytes) != (WIDTH, HEIGHT):
                last_reason = f"unexpected_size {current_image_size(png_bytes)}"
                time.sleep(0.3)
                continue
            valid, last_reason = verify_home_screen(png_bytes, WIDTH, HEIGHT)
            if valid:
                OUTPUT.write_bytes(png_bytes)
                print(f"PASS {OUTPUT.name} ({WIDTH}x{HEIGHT}@{DENSITY}dpi), {len(png_bytes)} bytes")
                return
            time.sleep(0.3)
        raise RuntimeError(f"Could not capture a valid landscape Home screen: {last_reason}")
    finally:
        restore_emulator_display(run_adb)


if __name__ == "__main__":
    main()
