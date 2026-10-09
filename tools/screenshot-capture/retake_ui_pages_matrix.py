"""Capture every navigable app page for one orientation group of the 8-profile QA matrix."""

from __future__ import annotations

import argparse
import io
import os
import shutil
import subprocess
import tempfile
import time
from pathlib import Path

import numpy as np
from PIL import Image, ImageChops

from capture_support import (
    ADB,
    LANDSCAPE_TARGET,
    PAGE_CAPTURE_ROOT,
    PACKAGE,
    PORTRAIT_TARGETS,
    PRIVACY_XML,
    accessibility_control_map,
    restore_emulator_display,
    restore_shared_preferences,
    run_adb,
    snapshot_shared_preferences,
    tap_accessibility,
    tap_fraction,
)


SCREENSHOTS = PAGE_CAPTURE_ROOT
LAUNCH_RECORDING_SECONDS = 8
LAUNCH_SAMPLE_FPS = 5


def capture_png() -> bytes:
    return subprocess.check_output([ADB, "exec-out", "screencap", "-p"], timeout=60.0)


def image_size(png_bytes: bytes) -> tuple[int, int]:
    with Image.open(io.BytesIO(png_bytes)) as image:
        return image.size


def image_mean(png_bytes: bytes) -> float:
    with Image.open(io.BytesIO(png_bytes)).convert("RGB") as image:
        return float(np.asarray(image).mean())


def wait_for_viewport(width: int, height: int, timeout: float = 30.0) -> None:
    deadline = time.monotonic() + timeout
    last_seen: tuple[int, int] | None = None
    while time.monotonic() < deadline:
        try:
            last_seen = image_size(capture_png())
            if last_seen == (width, height):
                return
        except Exception:
            pass
        time.sleep(0.4)
    raise RuntimeError(f"Viewport did not settle at {width}x{height}; last image was {last_seen}")


def seed_privacy_profile() -> None:
    if not PRIVACY_XML.is_file():
        raise FileNotFoundError(f"Screenshot fixture not found: {PRIVACY_XML}")
    run_adb("push", str(PRIVACY_XML), "/data/local/tmp/kavvoro_capture_privacy.xml")
    run_adb("shell", "run-as", PACKAGE, "mkdir", "-p", "shared_prefs")
    run_adb(
        "shell",
        "run-as",
        PACKAGE,
        "cp",
        "/data/local/tmp/kavvoro_capture_privacy.xml",
        "shared_prefs/privacy_profile.xml",
    )
    run_adb("shell", "rm", "-f", "/data/local/tmp/kavvoro_capture_privacy.xml")


HOME_CONTROLS = ("PLAY NOW", "SETTINGS", "Skins", "Leaderboard", "Missions", "HYPE")


class HomeNavigator:
    """Use one launch and one accessibility lookup for all Home destinations."""

    def __init__(self, controls: dict[str, tuple[int, int, int, int]]) -> None:
        self.controls = controls

    @classmethod
    def launch(cls, launch_capture: tuple[str, int, int] | None = None) -> "HomeNavigator":
        started = time.monotonic()
        seed_privacy_profile()
        run_adb("shell", "am", "force-stop", PACKAGE)
        if launch_capture is not None:
            filename, width, height = launch_capture
            capture_launch_page(filename, width, height)
        else:
            run_adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")

        # Dismiss Android's one-time immersive prompt only when it is actually shown.
        try:
            tap_accessibility("Got it", timeout=1.5)
            time.sleep(0.5)
        except RuntimeError:
            pass

        controls = accessibility_control_map(HOME_CONTROLS, timeout=35.0)
        print(f"Home ready; controls resolved in {time.monotonic() - started:.1f}s", flush=True)
        return cls(controls)

    def tap(self, label: str, settle: float = 1.25) -> None:
        try:
            left, top, right, bottom = self.controls[label]
        except KeyError as error:
            raise RuntimeError(f"Home control was not cached: {label}") from error
        x, y = (left + right) // 2, (top + bottom) // 2
        run_adb("shell", "input", "tap", str(x), str(y))
        time.sleep(settle)

    @staticmethod
    def back(steps: int = 1, settle: float = 0.45) -> None:
        for _ in range(steps):
            run_adb("shell", "input", "keyevent", "4")
            time.sleep(settle)


def start_home(launch_capture: tuple[str, int, int] | None = None) -> HomeNavigator:
    return HomeNavigator.launch(launch_capture)


def tap_settings_tab(width: int, height: int, density: int, tab: str) -> None:
    """Select a Settings tab in either the 2x2 mobile or 4-column layout."""
    width_dp = width * 160.0 / density
    two_by_two = width_dp <= 600.0 and height > width
    # The 480x854 profile places its second row lower than the other 2x2
    # profiles. Avoid its top border while keeping the 600x1024 row centered.
    row_two_y = 0.40 if two_by_two and 460.0 <= width_dp <= 500.0 and height < 900 else 0.35
    row_one_y = 0.31 if width > height else 0.25
    positions = {
        "gameplay": (0.66, 0.28) if two_by_two else (0.375, row_one_y),
        "system": (0.22, row_two_y) if two_by_two else (0.625, row_one_y),
        "about": (0.66, row_two_y) if two_by_two else (0.875, row_one_y),
    }
    try:
        x, y = positions[tab]
    except KeyError as error:
        raise ValueError(f"Unknown Settings tab: {tab}") from error
    tap_fraction(width, height, x, y)
    time.sleep(0.55)


def tap_reset_card(width: int, height: int, filename: str) -> None:
    """Find the Reset Progress label on the rendered canvas, then tap it."""
    png = capture_png()
    if image_size(png) != (width, height):
        raise RuntimeError(f"{filename}: cannot locate Reset card in unexpected viewport")
    with Image.open(io.BytesIO(png)).convert("RGB") as image:
        pixels = np.asarray(image)
    red, green, blue = pixels[:, :, 0], pixels[:, :, 1], pixels[:, :, 2]
    pink = (
        (red > 150)
        & (green < 130)
        & (blue > 120)
        & (red > green * 1.25)
        & (blue > green * 1.2)
    )
    # The System tab's Reset Progress title is the only saturated pink text
    # below the tabs. Limiting the scan excludes the selected-tab gradient.
    top, bottom = int(height * 0.44), int(height * 0.90)
    left, right = int(width * 0.20), int(width * 0.80)
    rows = pink[top:bottom, left:right].sum(axis=1)
    window = max(3, int(height * 0.012))
    smoothed = np.convolve(rows, np.ones(window, dtype=np.int32), mode="same")
    local_y = int(smoothed.argmax())
    if int(smoothed[local_y]) < 20:
        raise RuntimeError(f"{filename}: could not find the pink Reset Progress label")
    y = top + local_y
    tap_fraction(width, height, 0.50, y / height)


def tap_language_card(width: int, height: int, density: int, filename: str) -> None:
    """Locate the first cyan-accented System row instead of assuming a fixed Y."""
    png = capture_png()
    if image_size(png) != (width, height):
        raise RuntimeError(f"{filename}: cannot locate Language row in unexpected viewport")
    with Image.open(io.BytesIO(png)).convert("RGB") as image:
        pixels = np.asarray(image)
    red, green, blue = pixels[:, :, 0], pixels[:, :, 1], pixels[:, :, 2]
    cyan = (red < 130) & (green > 110) & (blue > 140) & (blue > red * 1.3)
    width_dp = width * 160.0 / density
    two_by_two = width_dp <= 600.0 and height > width
    if width > height:
        top_fraction, bottom_fraction = 0.43, 0.54
    elif two_by_two:
        top_fraction, bottom_fraction = 0.49, 0.58
    else:
        top_fraction, bottom_fraction = 0.34, 0.43
    top = int(height * top_fraction)
    bottom = min(int(height * bottom_fraction), top + int(height * 0.12))
    left, right = int(width * 0.03), int(width * 0.12)
    rows = cyan[top:bottom, left:right].sum(axis=1)
    window = max(3, int(height * 0.008))
    smoothed = np.convolve(rows, np.ones(window, dtype=np.int32), mode="same")
    local_y = int(smoothed.argmax())
    if int(smoothed[local_y]) < 20:
        raise RuntimeError(f"{filename}: could not find the cyan Language card marker")
    tap_fraction(width, height, 0.50, (top + local_y) / height)
    time.sleep(1.25)


def tap_rift_challenges_tab(width: int, height: int, density: int) -> None:
    """Use the same responsive tab-center formula as MissionsLayoutCalculator."""
    dp = density / 160.0
    top = max(22.0 * dp, height * 0.035)
    short_compact = height / dp < 430.0
    tab_top = top + (51.0 if short_compact else 62.0) * dp
    tab_height = (34.0 if short_compact else 42.0) * dp
    # Tap below the visual center: on tablet portrait the rendered tab row sits
    # slightly lower than the controller's inset-based origin, so a center tap
    # can land just above its hit rect after wm-size overrides.
    tab_center_y = tab_top + tab_height * 0.80
    tap_fraction(width, height, 0.75, tab_center_y / height)
    time.sleep(1.0)


def capture_page(
    folder: str,
    filename: str,
    width: int,
    height: int,
    png_bytes: bytes | None = None,
) -> bytes:
    started = time.monotonic()
    png = png_bytes if png_bytes is not None else capture_png()
    actual_size = image_size(png)
    if actual_size != (width, height):
        raise RuntimeError(f"{folder}/{filename}: expected {width}x{height}, got {actual_size}")
    mean = image_mean(png)
    if mean < 4.0:
        raise RuntimeError(f"{folder}/{filename}: black/empty capture (mean RGB={mean:.1f})")
    target = SCREENSHOTS / folder / filename
    target.parent.mkdir(parents=True, exist_ok=True)
    temporary = target.with_name(target.name + ".tmp")
    temporary.write_bytes(png)
    os.replace(temporary, target)
    print(
        f"[PASS] {folder}/{filename} ({width}x{height}, mean={mean:.1f}, "
        f"capture={time.monotonic() - started:.2f}s)",
        flush=True,
    )
    return png


def launch_frame_score(png_bytes: bytes, width: int, height: int) -> tuple[int, int, int, int] | None:
    """Recognize the app intro after its convergence flash, excluding OS splash frames."""
    with Image.open(io.BytesIO(png_bytes)).convert("RGB") as image:
        if image.size != (width, height):
            return None
        pixels = np.asarray(image)

    if float(pixels.mean()) >= 40.0:
        return None

    height_px, width_px, _ = pixels.shape
    title_region = pixels[
        int(height_px * 0.48):int(height_px * 0.76),
        int(width_px * 0.25):int(width_px * 0.75),
    ].astype(np.int16)
    red, green, blue = (title_region[:, :, channel] for channel in range(3))
    white_pixels = int(((red > 175) & (green > 175) & (blue > 175)).sum())
    gold_pixels = int(((red > 155) & (green > 90) & (blue < 135)).sum())

    line_region = pixels[
        int(height_px * 0.55):int(height_px * 0.79),
        int(width_px * 0.20):int(width_px * 0.80),
    ].astype(np.int16)
    red, green, blue = (line_region[:, :, channel] for channel in range(3))
    cyan = (green > red * 1.30) & (blue > red * 1.20) & (green > 85)
    magenta = (red > green * 1.30) & (blue > green * 1.15) & (red > 85)
    line_rows = (cyan | magenta).sum(axis=1)
    line_index = int(line_rows.argmax())
    line_pixels = int(line_rows[line_index])
    line_y = int(height_px * 0.55) + line_index

    center_x = width_px // 2
    horizontal_radius = max(4, round(width_px * 0.018))
    vertical_radius = max(4, round(height_px * 0.008))
    center_patch = pixels[
        max(0, line_y - vertical_radius):min(height_px, line_y + vertical_radius + 1),
        max(0, center_x - horizontal_radius):min(width_px, center_x + horizontal_radius + 1),
    ].astype(np.int16)
    red, green, blue = (center_patch[:, :, channel] for channel in range(3))
    flash_pixels = int(((red > 175) & (green > 175) & (blue > 175)).sum())
    center_cyan = int(((green > red * 1.30) & (blue > red * 1.20) & (green > 85)).sum())
    center_magenta = int(((red > green * 1.30) & (blue > green * 1.15) & (red > 85)).sum())

    has_wordmark = white_pixels > max(160, round(width_px * 0.20))
    has_subtitle = gold_pixels > max(70, round(width_px * 0.07))
    has_centered_lines = line_pixels > max(14, round(width_px * 0.07))
    lines_have_met = flash_pixels > 2 or (center_cyan > 2 and center_magenta > 2)
    if not (has_wordmark and has_subtitle and has_centered_lines and lines_have_met):
        return None
    return flash_pixels, center_cyan + center_magenta, line_pixels, white_pixels


def capture_launch_page(filename: str, width: int, height: int) -> bytes:
    ffmpeg = shutil.which("ffmpeg")
    if ffmpeg is None:
        raise RuntimeError("ffmpeg is required to isolate the brief MoonSol launch animation from Android's system splash")

    remote_video = "/sdcard/kavvoro_launch_capture.mp4"
    run_adb("shell", "rm", "-f", remote_video)
    recorder = subprocess.Popen(
        [
            ADB,
            "shell",
            "screenrecord",
            "--time-limit",
            str(LAUNCH_RECORDING_SECONDS),
            "--bit-rate",
            "12M",
            remote_video,
        ],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    try:
        time.sleep(0.25)
        run_adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
        try:
            recorder.wait(timeout=LAUNCH_RECORDING_SECONDS + 8)
        except subprocess.TimeoutExpired as error:
            recorder.kill()
            raise RuntimeError("Android launch screen recording did not finish") from error
        if recorder.returncode != 0:
            raise RuntimeError(f"Android launch screen recording failed with exit code {recorder.returncode}")

        with tempfile.TemporaryDirectory(prefix="kavvoro-launch-") as temporary:
            temporary_dir = Path(temporary)
            video_path = temporary_dir / "launch.mp4"
            frame_dir = temporary_dir / "frames"
            frame_dir.mkdir()
            run_adb("pull", remote_video, str(video_path), timeout=45.0)
            subprocess.run(
                [
                    ffmpeg,
                    "-hide_banner",
                    "-loglevel",
                    "error",
                    "-i",
                    str(video_path),
                    "-vf",
                    f"fps={LAUNCH_SAMPLE_FPS}",
                    str(frame_dir / "frame-%03d.png"),
                ],
                check=True,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.PIPE,
                timeout=45.0,
            )
            candidates: list[tuple[tuple[int, int, int, int], Path]] = []
            for frame_path in sorted(frame_dir.glob("frame-*.png")):
                frame = frame_path.read_bytes()
                score = launch_frame_score(frame, width, height)
                if score is not None:
                    candidates.append((score, frame_path))
            if not candidates:
                raise RuntimeError(
                    f"{filename}: no converged MoonSol intro frame found in the {LAUNCH_RECORDING_SECONDS}s launch recording"
                )
            _, selected_frame = max(candidates, key=lambda candidate: candidate[0])
            return capture_page("launch", filename, width, height, selected_frame.read_bytes())
    finally:
        if recorder.poll() is None:
            recorder.kill()
            recorder.wait(timeout=3)
        try:
            run_adb("shell", "rm", "-f", remote_video, timeout=5.0)
        except subprocess.SubprocessError:
            pass


def require_visual_change(reference: bytes, candidate: bytes, label: str, threshold: float = 0.75) -> None:
    difference = ImageChops.difference(
        Image.open(io.BytesIO(reference)).convert("RGB"),
        Image.open(io.BytesIO(candidate)).convert("RGB"),
    )
    mean_difference = float(np.asarray(difference).mean())
    if mean_difference < threshold:
        raise RuntimeError(f"{label}: expected a screen transition, visual difference={mean_difference:.2f}")


def capture_settings(navigator: HomeNavigator, width: int, height: int, density: int, filename: str) -> None:
    navigator.tap("SETTINGS")
    capture_page("settings/audio", filename, width, height)

    tap_settings_tab(width, height, density, "gameplay")
    capture_page("settings/gameplay", filename, width, height)

    tap_settings_tab(width, height, density, "system")
    capture_page("settings", filename, width, height)

    tap_settings_tab(width, height, density, "about")
    capture_page("settings/about", filename, width, height)


def capture_language(navigator: HomeNavigator, width: int, height: int, density: int, filename: str) -> None:
    tap_language_card(width, height, density, filename)
    language_png = capture_page("language", filename, width, height)
    system_png = (SCREENSHOTS / "settings" / filename).read_bytes()
    difference = ImageChops.difference(
        Image.open(io.BytesIO(system_png)).convert("RGB"),
        Image.open(io.BytesIO(language_png)).convert("RGB"),
    )
    if float(np.asarray(difference).mean()) < 0.75:
        raise RuntimeError(f"{filename}: Settings did not open the language selector")
    navigator.back()


def capture_settings_dialog(navigator: HomeNavigator, width: int, height: int, density: int, filename: str) -> None:
    # The reset card sits below account/language on the System tab. Give the
    # custom SurfaceView time to redraw its confirmation overlay before capture.
    tap_reset_card(width, height, filename)
    time.sleep(1.4)
    dialog_png = capture_page("settings-dialog", filename, width, height)
    system_png = (SCREENSHOTS / "settings" / filename).read_bytes()
    difference = ImageChops.difference(
        Image.open(io.BytesIO(system_png)).convert("RGB"),
        Image.open(io.BytesIO(dialog_png)).convert("RGB"),
    )
    if float(np.asarray(difference).mean()) < 0.75:
        raise RuntimeError(f"{filename}: Reset confirmation dialog did not open")
    navigator.back(steps=2)


def capture_profile(
    target: tuple[str, int, int, int],
    landscape: bool,
    pages: set[str],
    reuse_display: bool = False,
) -> None:
    profile_started = time.monotonic()
    filename, width, height, density = target
    run_adb("shell", "am", "force-stop", PACKAGE)
    run_adb("shell", "input", "keyevent", "224")
    run_adb("shell", "wm", "dismiss-keyguard")
    run_adb("shell", "settings", "put", "global", "stay_on_while_plugged_in", "3")

    if not reuse_display:
        run_adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
        if landscape:
            # The designated tablet profile uses its native landscape orientation.
            run_adb("shell", "cmd", "window", "set-ignore-orientation-request", "false")
        run_adb("shell", "settings", "put", "system", "user_rotation", "0")
        run_adb("shell", "wm", "density", str(density))
        run_adb("shell", "wm", "size", f"{width}x{height}")
    wait_for_viewport(width, height)
    time.sleep(1.0)

    print(f"\n=== {filename} ===", flush=True)

    navigator = start_home((filename, width, height))
    home_reference = capture_png()
    if image_size(home_reference) != (width, height):
        raise RuntimeError(f"{filename}: Home reference has unexpected size {image_size(home_reference)}")

    if "home" in pages or "all" in pages:
        capture_page("home", filename, width, height)

    if "play-mode" in pages or "all" in pages:
        navigator.tap("PLAY NOW")
        require_visual_change(home_reference, capture_page("play-mode", filename, width, height), f"{filename} play-mode")
        navigator.back()

    if "collection" in pages or "all" in pages:
        navigator.tap("Skins")
        require_visual_change(home_reference, capture_page("collection", filename, width, height), f"{filename} collection")
        navigator.back()

    if "leaderboards" in pages or "all" in pages:
        navigator.tap("Leaderboard")
        require_visual_change(home_reference, capture_page("leaderboards", filename, width, height), f"{filename} leaderboards")
        navigator.back()

    if "missions" in pages or "all" in pages:
        navigator.tap("Missions")
        daily_missions_png = capture_page("missions", filename, width, height)
        require_visual_change(home_reference, daily_missions_png, f"{filename} missions")
        tap_rift_challenges_tab(width, height, density)
        rift_png = capture_page("missions", f"rift-challenges-{filename}", width, height)
        require_visual_change(daily_missions_png, rift_png, f"{filename} rift challenges")
        navigator.back()

    if "daily-rift-bonus" in pages or "all" in pages:
        navigator.tap("HYPE")
        require_visual_change(home_reference, capture_page("daily-rift-bonus", filename, width, height), f"{filename} daily bonus")
        navigator.back()

    capture_settings_group = "settings" in pages or "all" in pages
    capture_language_group = "language" in pages or "all" in pages
    capture_dialog_group = "settings-dialog" in pages or "all" in pages
    if capture_settings_group:
        capture_settings(navigator, width, height, density, filename)

    if capture_language_group or capture_dialog_group:
        if not capture_settings_group:
            navigator.tap("SETTINGS")
        tap_settings_tab(width, height, density, "system")

        if capture_language_group:
            capture_language(navigator, width, height, density, filename)

        if capture_dialog_group:
            capture_settings_dialog(navigator, width, height, density, filename)
        else:
            navigator.back()
    elif capture_settings_group:
        navigator.back()

    if "gameplay" in pages or "all" in pages:
        # Preferences are snapshotted once for the whole run and restored afterward.
        navigator.tap("PLAY NOW")
        mode_reference = capture_png()
        tap_fraction(width, height, 0.35, 0.273)
        time.sleep(1.2)
        gameplay_png = capture_page("gameplay", filename, width, height)
        require_visual_change(mode_reference, gameplay_png, f"{filename} gameplay")
        time.sleep(6.0)
        outcome_png = capture_page("outcome", filename, width, height)
        require_visual_change(gameplay_png, outcome_png, f"{filename} outcome")
        navigator.back()
    print(f"Profile {filename} completed in {time.monotonic() - profile_started:.1f}s", flush=True)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--orientation", choices=("portrait", "landscape"), default="portrait")
    parser.add_argument("--serial", help="ADB serial; otherwise honor ANDROID_SERIAL")
    parser.add_argument("--profile", help="capture one approved profile instead of the whole orientation group")
    parser.add_argument("--display-ready", action="store_true", help="reuse display geometry configured by a parent capture run")
    parser.add_argument("--keep-display", action="store_true", help="leave display settings in place for the next capture group")
    parser.add_argument(
        "--only",
        choices=("all", "home", "play-mode", "collection", "leaderboards", "missions", "daily-rift-bonus", "settings", "language", "settings-dialog", "gameplay"),
        default="all",
        help="capture one page group; all captures the complete navigation and gameplay set",
    )
    args = parser.parse_args()
    if args.serial:
        os.environ["ANDROID_SERIAL"] = args.serial

    targets = PORTRAIT_TARGETS if args.orientation == "portrait" else (LANDSCAPE_TARGET,)
    if args.profile:
        matching = [target for target in (*PORTRAIT_TARGETS, LANDSCAPE_TARGET) if target[0] == args.profile]
        if not matching:
            allowed = ", ".join(target[0] for target in (*PORTRAIT_TARGETS, LANDSCAPE_TARGET))
            parser.error(f"unknown profile {args.profile!r}; choose one of: {allowed}")
        if args.orientation == "portrait" and matching[0] == LANDSCAPE_TARGET:
            parser.error("the tablet landscape profile requires --orientation landscape")
        if args.orientation == "landscape" and matching[0] != LANDSCAPE_TARGET:
            parser.error("portrait profiles require --orientation portrait")
        targets = tuple(matching)
    original_preferences = snapshot_shared_preferences()
    try:
        for target in targets:
            capture_profile(
                target,
                landscape=args.orientation == "landscape",
                pages={args.only},
                reuse_display=args.display_ready,
            )
    finally:
        try:
            restore_shared_preferences(original_preferences)
        finally:
            if not args.keep_display:
                restore_emulator_display(lambda *arguments: run_adb(*arguments))
    print(f"Completed {len(targets)} {args.orientation} profile(s).")


if __name__ == "__main__":
    main()
