"""Capture the Missions page at the canonical 1080x2400 phone profile."""

from __future__ import annotations

import io
import re
import subprocess
import time

import numpy as np
from PIL import Image

from capture_support import ADB, PACKAGE, PRIVACY_XML, PROJECT_ROOT, restore_emulator_display


WIDTH = 1080
HEIGHT = 2400
DENSITY = 420
NAME = f"phone-{WIDTH}x{HEIGHT}-{DENSITY}dpi.png"
OUTPUT = PROJECT_ROOT / "screenshots" / "missions" / NAME


def run_adb(*args: str, timeout: float = 30.0) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )


def capture_png() -> bytes:
    return subprocess.check_output([ADB, "exec-out", "screencap", "-p"], timeout=30.0)


def missions_activity_is_foreground() -> bool:
    output = run_adb("shell", "dumpsys", "window", timeout=12.0).stdout.decode("utf-8", "replace")
    return re.search(rf"mCurrentFocus=.*{re.escape(PACKAGE)}/{re.escape(PACKAGE)}\.MainActivity", output) is not None


def ensure_privacy_profile() -> None:
    run_adb("push", str(PRIVACY_XML), "/data/local/tmp/privacy_profile.xml")
    run_adb("shell", "run-as", PACKAGE, "mkdir", "-p", "shared_prefs")
    run_adb(
        "shell",
        "run-as",
        PACKAGE,
        "cp",
        "/data/local/tmp/privacy_profile.xml",
        "shared_prefs/privacy_profile.xml",
    )


def portrait_png(png_bytes: bytes) -> bytes:
    with Image.open(io.BytesIO(png_bytes)) as image:
        if image.size[0] > image.size[1]:
            image = image.rotate(270, expand=True)
        output = io.BytesIO()
        image.save(output, format="PNG")
        return output.getvalue()


def verify_missions_screen(png_bytes: bytes) -> tuple[bool, str]:
    try:
        image = Image.open(io.BytesIO(png_bytes)).convert("RGB")
    except Exception as error:
        return False, f"corrupt_image ({error})"

    if image.size != (WIDTH, HEIGHT):
        return False, f"bad_size {image.size} != ({WIDTH}, {HEIGHT})"

    pixels = np.asarray(image)
    mean_rgb = float(np.mean(pixels))
    if mean_rgb < 10.0:
        return False, f"black_or_splash (mean={mean_rgb:.1f})"

    non_background = (
        np.abs(pixels[:, :, 0].astype(np.int16) - 5)
        + np.abs(pixels[:, :, 1].astype(np.int16) - 7)
        + np.abs(pixels[:, :, 2].astype(np.int16) - 13)
    ) > 24
    middle = non_background[int(HEIGHT * 0.18) : int(HEIGHT * 0.87), :]
    if float(np.mean(middle)) < 0.035:
        return False, f"mission_cards_not_visible (content={np.mean(middle) * 100:.2f}%)"

    cyan = (pixels[:, :, 1] > 120) & (pixels[:, :, 2] > 130) & (pixels[:, :, 0] < 130)
    gold = (pixels[:, :, 0] > 150) & (pixels[:, :, 1] > 105) & (pixels[:, :, 2] < 155)
    accents = int(np.count_nonzero(cyan | gold))
    if accents < 900:
        return False, f"missing_mission_accents ({accents} pixels)"

    return True, f"verified (mean={mean_rgb:.1f}, content={np.mean(middle) * 100:.2f}%, accents={accents})"


def capture_missions() -> bytes:
    last_reason = "timeout"
    for attempt in range(1, 4):
        ensure_privacy_profile()
        run_adb("shell", "am", "force-stop", PACKAGE)
        run_adb(
            "shell",
            "am",
            "start",
            "-n",
            f"{PACKAGE}/.MainActivity",
            "--es",
            "screen",
            "missions",
        )

        started_at = time.monotonic()
        deadline = started_at + 14.0
        while time.monotonic() < deadline:
            if time.monotonic() - started_at < 2.5:
                time.sleep(0.15)
                continue
            try:
                if not missions_activity_is_foreground():
                    last_reason = "missions_activity_not_foreground"
                    time.sleep(0.35)
                    continue
                raw_png = capture_png()
                oriented_png = portrait_png(raw_png)
                valid, reason = verify_missions_screen(oriented_png)
                if valid:
                    return oriented_png
                last_reason = reason
            except Exception as error:
                last_reason = f"capture_error ({error})"
            time.sleep(0.35)

        print(f"[Attempt {attempt}/3 failed: {last_reason}. Retrying...]", flush=True)
        time.sleep(1.0)

    raise RuntimeError(f"Failed to capture Missions after 3 attempts: {last_reason}")


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    run_adb("shell", "settings", "put", "global", "stay_on_while_plugged_in", "3")
    run_adb("shell", "input", "keyevent", "224")
    run_adb("shell", "wm", "dismiss-keyguard")
    run_adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    run_adb("shell", "settings", "put", "system", "user_rotation", "0")
    try:
        run_adb("shell", "wm", "density", str(DENSITY))
        run_adb("shell", "wm", "size", f"{WIDTH}x{HEIGHT}")
        time.sleep(1.0)
        png_bytes = capture_missions()
        OUTPUT.write_bytes(png_bytes)
        print(f"[PASS] {NAME} ({WIDTH}x{HEIGHT} @{DENSITY}dpi, {len(png_bytes)} bytes)")
    finally:
        restore_emulator_display(lambda *args: run_adb(*args))
        print("Emulator display restored.", flush=True)


if __name__ == "__main__":
    main()
