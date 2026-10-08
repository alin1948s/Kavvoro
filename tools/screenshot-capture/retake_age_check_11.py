from io import BytesIO
import subprocess
import time

from PIL import Image

from capture_support import ADB, APK, PACKAGE, PROJECT_ROOT, TARGETS, restore_emulator_display
from retake_home_landscape import dismiss_fullscreen_prompt_if_shown

OUTPUT = PROJECT_ROOT / "screenshots" / "age-check"


def run(*args: str, timeout: float = 45.0) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def backup_age_profile() -> bytes | None:
    try:
        return run("exec-out", "run-as", PACKAGE, "cat", "shared_prefs/privacy_profile.xml").stdout
    except subprocess.CalledProcessError as error:
        if b"No such file" in error.stdout:
            return None
        raise


def remove_age_profile() -> None:
    run("shell", "am", "force-stop", PACKAGE)
    run("shell", "run-as", PACKAGE, "rm", "-f", "shared_prefs/privacy_profile.xml")


def restore_age_profile(profile: bytes | None) -> None:
    # Stop MainActivity before touching its SharedPreferences file. Otherwise
    # its still-open editor can recreate the file after this raw restoration.
    run("shell", "am", "force-stop", PACKAGE)
    if profile is None:
        run("shell", "run-as", PACKAGE, "rm", "-f", "shared_prefs/privacy_profile.xml")
        return
    command = (
        f"run-as {PACKAGE} sh -c "
        f"'cat > /data/user/0/{PACKAGE}/shared_prefs/privacy_profile.xml'"
    )
    subprocess.run(
        [ADB, "shell", command],
        check=True,
        input=profile,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=20.0,
    )


def capture_png() -> bytes:
    return subprocess.check_output(
        [ADB, "exec-out", "screencap", "-p"], timeout=60.0
    )


def image_size(png: bytes) -> tuple[int, int]:
    with Image.open(BytesIO(png)) as image:
        return image.size


def wait_for_viewport(width: int, height: int) -> None:
    # Large tablet frames can take several seconds each to capture, so allow
    # enough time to observe multiple stable frames at the highest resolution.
    deadline = time.monotonic() + 45.0
    last_size = None
    stable_frames = 0
    while time.monotonic() < deadline:
        size = image_size(capture_png())
        if size not in ((width, height), (height, width)):
            stable_frames = 0
            last_size = size
        elif size == last_size:
            stable_frames += 1
        else:
            last_size = size
            stable_frames = 1
        # wm size can briefly expose natural landscape dimensions while the
        # phone's portrait request is taking effect. Wait for a stable viewport.
        if stable_frames >= 3:
            return
        time.sleep(0.5)
    raise RuntimeError(
        f"Viewport did not settle at {width}x{height}; last size was {last_size}"
    )


def is_main_activity_foreground() -> bool:
    state = subprocess.check_output(
        [ADB, "shell", "dumpsys", "activity", "activities"], timeout=15.0
    ).decode("utf-8", errors="ignore")
    return any(
        ("mResumedActivity" in line or "topResumedActivity" in line or "ResumedActivity:" in line)
        and PACKAGE in line
        for line in state.splitlines()
    )


def is_age_check(png: bytes) -> bool:
    with Image.open(BytesIO(png)).convert("RGB") as image:
        top = image.crop(
            (
                int(image.width * 0.04),
                0,
                int(image.width * 0.96),
                int(image.height * 0.55),
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
        return 0.35 <= balance <= 1.90


def has_age_picker_accessibility_node() -> bool:
    try:
        run("shell", "uiautomator", "dump", "/sdcard/kavvoro_age_check.xml", timeout=20.0)
        hierarchy = run("shell", "cat", "/sdcard/kavvoro_age_check.xml").stdout.decode(
            "utf-8", errors="ignore"
        )
        return 'class="android.widget.NumberPicker"' in hierarchy
    except (OSError, subprocess.SubprocessError):
        return False


def orient_png(png: bytes, width: int, height: int) -> bytes:
    with Image.open(BytesIO(png)) as image:
        if image.size == (height, width):
            image = image.rotate(90, expand=True)
        output = BytesIO()
        image.save(output, format="PNG")
        return output.getvalue()


def wait_for_age_check(width: int, height: int) -> bytes:
    deadline = time.monotonic() + 20.0
    next_accessibility_check = 0.0
    accessibility_node_found = False
    while time.monotonic() < deadline:
        png = capture_png()
        size = image_size(png)
        if size not in ((width, height), (height, width)):
            time.sleep(0.5)
            continue
        normalized = orient_png(png, width, height)
        now = time.monotonic()
        if is_age_check(normalized) and now >= next_accessibility_check:
            accessibility_node_found = has_age_picker_accessibility_node()
            next_accessibility_check = time.monotonic() + 2.0
        # Home also has cyan and magenta accents, so require the native age
        # selector's accessibility node before accepting the frame.
        if is_age_check(normalized) and accessibility_node_found:
            return normalized
        time.sleep(0.5)
    raise RuntimeError("Age Check was not visible before the screenshot timeout")


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    run("install", "-r", str(APK), timeout=90.0)
    original_age_profile = backup_age_profile()
    remove_age_profile()
    try:
        run("shell", "settings", "put", "system", "accelerometer_rotation", "0")
        run("shell", "settings", "put", "system", "user_rotation", "1")
        for name, width, height, density in TARGETS:
            output_width, output_height = width, height
            output_name = name
            run("shell", "wm", "density", str(density))
            print(f"Capturing {output_name}", flush=True)
            # These age-check profiles are portrait. Lock the display before
            # changing wm size so full-sensor tablets do not rotate mid-capture.
            run("shell", "settings", "put", "system", "user_rotation", "0")
            if width == 1080 and height == 2400:
                # This matches the emulator's native phone profile. Resizing it
                # can stall screencap on AVDs.
                run("shell", "wm", "size", "reset")
            else:
                run("shell", "wm", "size", f"{width}x{height}")
            wait_for_viewport(output_width, output_height)
            run("shell", "am", "force-stop", PACKAGE)
            run("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
            time.sleep(2.0)
            dismiss_fullscreen_prompt_if_shown()
            (OUTPUT / output_name).write_bytes(wait_for_age_check(output_width, output_height))
    finally:
        try:
            restore_emulator_display(lambda *args: run(*args))
        finally:
            try:
                restore_age_profile(original_age_profile)
            finally:
                try:
                    run("shell", "rm", "-f", "/sdcard/kavvoro_age_check.xml", "/sdcard/kavvoro_window.xml")
                finally:
                    run("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")


if __name__ == "__main__":
    main()
