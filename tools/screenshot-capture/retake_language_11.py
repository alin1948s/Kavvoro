import io
import subprocess
import time

import numpy as np
from PIL import Image

from capture_support import ADB, PACKAGE, PRIVACY_XML, PROJECT_ROOT, TARGETS

OUTPUT = PROJECT_ROOT / "screenshots" / "language"


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


def image_size(png_bytes: bytes) -> tuple[int, int]:
    with Image.open(io.BytesIO(png_bytes)) as img:
        return img.size


def wait_for_viewport(width: int, height: int, timeout: float = 12.0) -> None:
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            size = image_size(capture_png())
            if size in ((width, height), (height, width)):
                return
        except Exception:
            pass
        time.sleep(0.3)
    raise RuntimeError(f"Viewport did not settle at {width}x{height}")


def portrait_png(png_bytes: bytes, width: int, height: int) -> bytes:
    with Image.open(io.BytesIO(png_bytes)) as img:
        if img.size[0] > img.size[1]:
            img = img.rotate(270, expand=True)
        out = io.BytesIO()
        img.save(out, format="PNG")
        return out.getvalue()


def verify_language_screen(png_bytes: bytes, width: int, height: int) -> tuple[bool, str]:
    try:
        img = Image.open(io.BytesIO(png_bytes)).convert("RGB")
    except Exception as e:
        return False, f"corrupt_image ({e})"

    if img.size not in ((width, height), (height, width)):
        return False, f"bad_size {img.size} != ({width}, {height})"

    arr = np.array(img)
    w, h = img.size
    total_pixels = w * h

    mean_rgb = float(np.mean(arr))
    if mean_rgb < 6.0:
        return False, f"black_or_dim (mean={mean_rgb:.1f})"

    # UI neon accents (cyan and magenta elements)
    cyan = (arr[:, :, 2] > 140) & (arr[:, :, 1] > 140) & (arr[:, :, 0] < 140)
    magenta = (arr[:, :, 0] > 140) & (arr[:, :, 2] > 80) & (arr[:, :, 1] < 140)
    accent_ratio = (np.sum(cyan) + np.sum(magenta)) / float(total_pixels)
    if accent_ratio < 0.00005:
        return False, f"low_accents (accent={accent_ratio * 100:.3f}%)"

    # Bottom 20% must contain active UI elements (bottom current language bar)
    bot_pixels = arr[int(h * 0.80) :, :, :]
    bot_mean = float(np.mean(bot_pixels))
    if bot_mean < 4.0:
        return False, f"empty_footer (bot={bot_mean:.1f})"

    return True, f"verified (mean={mean_rgb:.1f}, bot={bot_mean:.1f}, accents={accent_ratio * 100:.2f}%)"


def ensure_privacy_profile() -> None:
    run_adb("push", str(PRIVACY_XML), "/data/local/tmp/privacy_profile.xml")
    run_adb("shell", "run-as", PACKAGE, "mkdir", "-p", "shared_prefs")
    run_adb("shell", "run-as", PACKAGE, "cp", "/data/local/tmp/privacy_profile.xml", "shared_prefs/privacy_profile.xml")


def capture_language_target(name: str, width: int, height: int, max_attempts: int = 3) -> bytes:
    for attempt in range(1, max_attempts + 1):
        ensure_privacy_profile()
        run_adb("shell", "am", "force-stop", PACKAGE)
        run_adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity", "--es", "screen", "language")

        time.sleep(2.2)
        deadline = time.monotonic() + 10.0
        last_reason = "timeout"

        while time.monotonic() < deadline:
            try:
                raw_png = capture_png()
                valid, reason = verify_language_screen(raw_png, width, height)
                if valid:
                    final_png = portrait_png(raw_png, width, height)
                    extra_valid, extra_reason = verify_language_screen(final_png, width, height)
                    if extra_valid:
                        return final_png
                    last_reason = f"post_check_failed ({extra_reason})"
                else:
                    last_reason = reason
            except Exception as ex:
                last_reason = f"capture_exception ({ex})"
            time.sleep(0.35)

        print(f"   [Attempt {attempt}/{max_attempts} failed: {last_reason}. Retrying...]", flush=True)

    raise RuntimeError(f"Failed to capture {name} after {max_attempts} attempts: {last_reason}")


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    run_adb("shell", "settings", "put", "global", "stay_on_while_plugged_in", "3")
    run_adb("shell", "input", "keyevent", "224")
    run_adb("shell", "wm", "dismiss-keyguard")
    run_adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    run_adb("shell", "settings", "put", "system", "user_rotation", "1")
    run_adb("shell", "wm", "density", "320")

    print(f"Capturing {len(TARGETS)} Language Screen resolutions to {OUTPUT}...", flush=True)
    try:
        for name, width, height in TARGETS:
            t0 = time.monotonic()
            run_adb("shell", "wm", "size", f"{height}x{width}")
            wait_for_viewport(width, height)
            time.sleep(0.3)
            png_bytes = capture_language_target(name, width, height)
            target_path = OUTPUT / name
            target_path.write_bytes(png_bytes)
            elapsed = time.monotonic() - t0
            print(f"-> [PASS] {name:20s} ({width}x{height}) {len(png_bytes):7d} bytes in {elapsed:4.1f}s", flush=True)
    finally:
        run_adb("shell", "wm", "size", "reset")
        run_adb("shell", "wm", "density", "reset")
        run_adb("shell", "am", "force-stop", PACKAGE)
        run_adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
        print("ALL DONE! Emulator restored.", flush=True)


if __name__ == "__main__":
    main()
