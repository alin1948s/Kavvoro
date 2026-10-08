import io
import os
import subprocess
import time

import numpy as np
from PIL import Image

from capture_support import ADB, PACKAGE, PRIVACY_XML, PROJECT_ROOT, TARGETS, restore_emulator_display, target_dp

OUTPUT = PROJECT_ROOT / "screenshots" / "settings"


def run_adb(*args: str, timeout: float = 30.0) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )


def write_png(path, data: bytes) -> None:
    tmp = path.with_name(path.name + ".tmp")
    tmp.write_bytes(data)
    last_error = None
    for attempt in range(8):
        try:
            os.replace(tmp, path)
            return
        except OSError as exc:
            last_error = exc
            time.sleep(0.4 * (attempt + 1))
    raise OSError(f"Unable to replace {path}: {last_error}") from last_error


def capture_png() -> bytes:
    return subprocess.check_output([ADB, "exec-out", "screencap", "-p"], timeout=60.0)


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


def verify_settings_screen(png_bytes: bytes, width: int, height: int) -> tuple[bool, str]:
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
    if mean_rgb < 7.0:
        return False, f"black_or_dim (mean={mean_rgb:.1f})"

    cyan = (arr[:, :, 2] > 140) & (arr[:, :, 1] > 140) & (arr[:, :, 0] < 140)
    magenta = (arr[:, :, 0] > 140) & (arr[:, :, 2] > 80) & (arr[:, :, 1] < 140)
    accent_ratio = (np.sum(cyan) + np.sum(magenta)) / float(total_pixels)
    if accent_ratio < 0.00004:
        return False, f"low_accents (accent={accent_ratio * 100:.3f}%)"

    top = arr[: int(h * 0.28), :, :]
    mid = arr[int(h * 0.28) : int(h * 0.78), :, :]
    bot = arr[int(h * 0.78) :, :, :]
    top_mean = float(np.mean(top))
    mid_mean = float(np.mean(mid))
    bot_mean = float(np.mean(bot))
    if top_mean < 6.0:
        return False, f"missing_header (top={top_mean:.1f})"
    if mid_mean < 6.0:
        return False, f"empty_card (mid={mid_mean:.1f})"
    if bot_mean < 4.0:
        return False, f"empty_footer (bot={bot_mean:.1f})"

    col_means = arr.mean(axis=(0, 2))
    active_cols = float(np.sum(col_means > 8.0))
    if active_cols / float(w) < 0.55:
        return False, f"letterboxed_or_rotated (active_cols={active_cols / w * 100:.1f}%)"

    return True, (
        f"verified (mean={mean_rgb:.1f}, top={top_mean:.1f}, "
        f"mid={mid_mean:.1f}, bot={bot_mean:.1f}, accents={accent_ratio * 100:.2f}%)"
    )


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


def dismiss_blocking_dialogs(width: int, height: int) -> None:
    # Top-left tap clears orientation/age chrome without hitting Settings rows.
    run_adb("shell", "input", "tap", "40", "40")
    # Touching an immersive screen can reveal Android's transient system bars.
    # Let them auto-hide before accepting a screenshot as a clean app capture.
    time.sleep(3.5)


def capture_settings_target(name: str, width: int, height: int, max_attempts: int = 6) -> bytes:
    for attempt in range(1, max_attempts + 1):
        run_adb("shell", "input", "keyevent", "224")
        ensure_privacy_profile()
        run_adb("shell", "am", "force-stop", PACKAGE)
        time.sleep(0.8)
        run_adb(
            "shell",
            "am",
            "start",
            "-n",
            f"{PACKAGE}/.MainActivity",
            "--es",
            "screen",
            "settings",
            "--es",
            "tab",
            "system",
        )
        time.sleep(5.0)
        dismiss_blocking_dialogs(width, height)
        try:
            wait_for_viewport(width, height, timeout=8.0)
        except RuntimeError:
            pass
        deadline = time.monotonic() + 18.0
        last_reason = "timeout"
        dismissed_again = False

        while time.monotonic() < deadline:
            try:
                raw_png = capture_png()
                valid, reason = verify_settings_screen(raw_png, width, height)
                if valid:
                    final_png = portrait_png(raw_png, width, height)
                    extra_valid, extra_reason = verify_settings_screen(final_png, width, height)
                    if extra_valid:
                        return final_png
                    last_reason = f"post_check_failed ({extra_reason})"
                else:
                    last_reason = reason
                    if not dismissed_again and reason.startswith(("low_accents", "black_or_dim")):
                        dismiss_blocking_dialogs(width, height)
                        dismissed_again = True
            except Exception as ex:
                last_reason = f"capture_exception ({ex})"
            time.sleep(0.4)

        print(f"   [Attempt {attempt}/{max_attempts} failed: {last_reason}. Retrying...]", flush=True)
        time.sleep(2.0)

    raise RuntimeError(f"Failed to capture {name} after {max_attempts} attempts: {last_reason}")


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    run_adb("shell", "settings", "put", "global", "stay_on_while_plugged_in", "3")
    run_adb("shell", "input", "keyevent", "224")
    run_adb("shell", "wm", "dismiss-keyguard")
    run_adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    run_adb("shell", "settings", "put", "system", "user_rotation", "0")

    start_after = os.environ.get("START_AFTER", "")
    skipping = bool(start_after)
    print(f"Capturing {len(TARGETS)} Settings Screen resolutions to {OUTPUT}...", flush=True)
    try:
        for name, width, height, density in TARGETS:
            if skipping:
                if name == start_after:
                    skipping = False
                continue
            t0 = time.monotonic()
            run_adb("shell", "wm", "density", str(density))
            run_adb("shell", "wm", "size", f"{width}x{height}")
            run_adb("shell", "input", "keyevent", "224")
            run_adb("shell", "wm", "dismiss-keyguard")
            time.sleep(2.5)
            try:
                wait_for_viewport(width, height, timeout=12.0)
            except RuntimeError:
                print(f"   [warn] viewport did not settle at {width}x{height}; capturing anyway", flush=True)
            time.sleep(1.0)
            png_bytes = capture_settings_target(name, width, height)
            target_path = OUTPUT / name
            write_png(target_path, png_bytes)
            elapsed = time.monotonic() - t0
            dp_w = target_dp(width, density)
            print(
                f"-> [PASS] {name:20s} ({width}x{height} @{density}dpi ~{dp_w:.0f}dp) "
                f"{len(png_bytes):7d} bytes in {elapsed:4.1f}s",
                flush=True,
            )
            time.sleep(2.0)
    finally:
        restore_emulator_display(lambda *args: run_adb(*args))
        print("ALL DONE! Emulator restored.", flush=True)


if __name__ == "__main__":
    main()
