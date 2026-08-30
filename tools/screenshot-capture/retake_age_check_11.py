from io import BytesIO
import subprocess
import time

from PIL import Image

from capture_support import ADB, APK, PACKAGE, PROJECT_ROOT, TARGETS

OUTPUT = PROJECT_ROOT / "screenshots" / "age-check"


def run(*args: str, timeout: float = 45.0) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def capture_png() -> bytes:
    return subprocess.check_output(
        [ADB, "exec-out", "screencap", "-p"], timeout=60.0
    )


def image_size(png: bytes) -> tuple[int, int]:
    with Image.open(BytesIO(png)) as image:
        return image.size


def wait_for_viewport(width: int, height: int) -> None:
    deadline = time.monotonic() + 15.0
    while time.monotonic() < deadline:
        if image_size(capture_png()) in ((width, height), (height, width)):
            return
        time.sleep(0.5)
    raise RuntimeError(f"Viewport did not settle at {width}x{height}")


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
        return 0.55 <= balance <= 1.90


def portrait_png(png: bytes, width: int, height: int) -> bytes:
    with Image.open(BytesIO(png)) as image:
        if image.size == (height, width):
            image = image.rotate(90, expand=True)
        output = BytesIO()
        image.save(output, format="PNG")
        return output.getvalue()


def wait_for_age_check(width: int, height: int) -> bytes:
    deadline = time.monotonic() + 20.0
    while time.monotonic() < deadline:
        png = capture_png()
        # The visible emulator can report the previous resumed task for a
        # short period after pm clear/am start even though the new frame is
        # already on screen. The Age Check colour signature is deliberately
        # specific enough to be the authoritative visual readiness signal.
        if is_age_check(png):
            return portrait_png(png, width, height)
        time.sleep(0.5)
    raise RuntimeError("Age Check was not visible before the screenshot timeout")


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    run("install", "-r", str(APK), timeout=90.0)
    try:
        # Keep the emulator's normal 320dpi profile while changing the viewport.
        # The app uses real dp/sp, so a 680dp tablet column is rendered at the same
        # physical scale as the live tablet. Forcing 160dpi here would make a
        # correct 680dp max-width look artificially tiny in 1600px captures.
        run("shell", "wm", "density", "320")
        run("shell", "settings", "put", "system", "accelerometer_rotation", "0")
        run("shell", "settings", "put", "system", "user_rotation", "1")
        for name, width, height in TARGETS:
            print(f"Capturing {name}", flush=True)
            # wm size is expressed in the emulator's natural orientation. The
            # display is locked portrait for this app, so swap the dimensions to
            # make the captured screencap itself width x height.
            run("shell", "wm", "size", f"{height}x{width}")
            wait_for_viewport(width, height)
            run("shell", "am", "force-stop", PACKAGE)
            run("shell", "pm", "clear", PACKAGE)
            run("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
            (OUTPUT / name).write_bytes(wait_for_age_check(width, height))
    finally:
        run("shell", "wm", "size", "reset")
        run("shell", "wm", "density", "reset")
        run("shell", "am", "force-stop", PACKAGE)
        run("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")


if __name__ == "__main__":
    main()
