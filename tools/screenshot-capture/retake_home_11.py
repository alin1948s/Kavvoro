import io
import subprocess
import time

import numpy as np
from PIL import Image

from capture_support import ADB, PACKAGE, PRIVACY_XML, PROJECT_ROOT, TARGETS

OUTPUT = PROJECT_ROOT / "screenshots" / "home"


def run_adb(*args: str, timeout: float = 30.0) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )


def capture_png() -> bytes:
    return subprocess.check_output([ADB, "exec-out", "screencap", "-p"], timeout=20.0)


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
        if img.size == (height, width):
            img = img.rotate(90, expand=True)
        out = io.BytesIO()
        img.save(out, format="PNG")
        return out.getvalue()


def is_age_check(img: Image.Image) -> bool:
    top = img.crop(
        (
            int(img.width * 0.04),
            0,
            int(img.width * 0.96),
            int(img.height * 0.55),
        )
    ).resize((160, 96))
    pixels = top.load()
    cyan = 0
    magenta = 0
    for y in range(top.height):
        for x in range(top.width):
            red, green, blue = pixels[x, y]
            if green > 105 and blue > 115 and red < 135:
                cyan += 1
            if red > 125 and blue > 90 and green < 135:
                magenta += 1
    if cyan < 20 or magenta < 20:
        return False
    balance = magenta / float(cyan)
    return 0.55 <= balance <= 1.90


def verify_home_screen(png_bytes: bytes, width: int, height: int) -> tuple[bool, str]:
    try:
        img = Image.open(io.BytesIO(png_bytes)).convert("RGB")
    except Exception as e:
        return False, f"corrupt_image ({e})"

    if img.size not in ((width, height), (height, width)):
        return False, f"bad_size {img.size} != ({width}, {height})"

    arr = np.array(img)
    w, h = img.size

    mean_rgb = float(np.mean(arr))
    # Splash screen (~11.5) and black transitions (< 8.5) have low brightness.
    # Home screen with full neon glow, buttons, text and aura has mean > 22.0.
    if mean_rgb < 20.0:
        return False, f"mean_too_low ({mean_rgb:.1f} < 20.0)"

    if is_age_check(img):
        return False, "age_check_active"

    # Background color distance
    diff1 = np.abs(arr[:, :, 0] - 5) + np.abs(arr[:, :, 1] - 7) + np.abs(arr[:, :, 2] - 13)
    diff2 = np.abs(arr[:, :, 0] - 4) + np.abs(arr[:, :, 1] - 8) + np.abs(arr[:, :, 2] - 17)
    is_non_bg = (diff1 > 15) & (diff2 > 15)

    top_region = is_non_bg[: int(h * 0.20), :]
    bot_region = is_non_bg[int(h * 0.75) :, :]

    top_pct = float(np.sum(top_region)) / (w * int(h * 0.20)) * 100
    bot_pct = float(np.sum(bot_region)) / (w * (h - int(h * 0.75))) * 100
    bot_mean = float(np.mean(arr[int(h * 0.75) :, :, :]))

    # The MoonSol Splash screen has 0% content in the top 20% and bottom 25%.
    # The Home Screen has brand title & stats in top 20% and navigation in bottom 25%.
    if bot_pct < 5.0 or bot_mean < 14.0:
        return False, f"splash_or_empty_footer (bot_pct={bot_pct:.1f}%, bot_mean={bot_mean:.1f})"

    if top_pct < 5.0:
        return False, f"missing_header (top_pct={top_pct:.1f}%)"

    return True, f"verified (mean={mean_rgb:.1f}, top_pct={top_pct:.1f}%, bot_pct={bot_pct:.1f}%, bytes={len(png_bytes)})"


def ensure_privacy_profile() -> None:
    run_adb("push", str(PRIVACY_XML), "/data/local/tmp/privacy_profile.xml")
    run_adb("shell", "run-as", PACKAGE, "mkdir", "-p", "shared_prefs")
    run_adb("shell", "run-as", PACKAGE, "cp", "/data/local/tmp/privacy_profile.xml", "shared_prefs/privacy_profile.xml")


def capture_home_target(name: str, width: int, height: int, max_attempts: int = 3) -> bytes:
    for attempt in range(1, max_attempts + 1):
        ensure_privacy_profile()
        run_adb("shell", "am", "force-stop", PACKAGE)
        run_adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")

        # Polling deadline: give emulator enough time for 1.45s splash + surface init + home render
        start_time = time.monotonic()
        deadline = start_time + 10.0
        last_reason = "timeout"

        while time.monotonic() < deadline:
            try:
                raw_png = capture_png()
                valid, reason = verify_home_screen(raw_png, width, height)
                if valid:
                    final_png = portrait_png(raw_png, width, height)
                    # Extra check step on the final rotated portrait image
                    extra_valid, extra_reason = verify_home_screen(final_png, width, height)
                    if extra_valid:
                        return final_png
                    last_reason = f"post_check_failed ({extra_reason})"
                else:
                    last_reason = reason
            except Exception as e:
                last_reason = f"screencap_error ({e})"
            time.sleep(0.3)

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

    print(f"Capturing {len(TARGETS)} Home Screen resolutions to {OUTPUT}...", flush=True)
    try:
        for name, width, height in TARGETS:
            t0 = time.monotonic()
            run_adb("shell", "wm", "size", f"{height}x{width}")
            wait_for_viewport(width, height)
            png_bytes = capture_home_target(name, width, height)
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
