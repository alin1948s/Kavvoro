"""Shared, machine-independent configuration for screenshot capture tools."""

from __future__ import annotations

import os
import re
import subprocess
import sys
import tempfile
import time
from pathlib import Path
from xml.etree import ElementTree


SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from tools.screenshot_matrix import (
    LANDSCAPE_TARGET,
    PAGE_CAPTURE_ROOT,
    PORTRAIT_TARGETS,
    PROFILE_MATRIX,
    target_filename,
)


PACKAGE = "com.moonsolstudios.kavvoro"
APK = PROJECT_ROOT / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
PRIVACY_XML = SCRIPT_DIR / "fixtures" / "privacy_profile.xml"
HOME_ACCESSIBILITY_ORDER = ("SETTINGS", "HYPE", "PLAY NOW", "Skins", "Missions", "Leaderboard")


def run_adb(*args: str, timeout: float = 30.0) -> subprocess.CompletedProcess[bytes]:
    """Run ADB while honoring ANDROID_SERIAL when several emulators are open."""
    return subprocess.run(
        [ADB, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )


def accessibility_bounds(label: str, timeout: float = 12.0) -> tuple[int, int, int, int] | None:
    """Find a visible Android accessibility node by text/content description."""
    remote_path = "/sdcard/kavvoro_capture_hierarchy.xml"
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            run_adb("shell", "uiautomator", "dump", remote_path, timeout=18.0)
            hierarchy = run_adb("shell", "cat", remote_path, timeout=8.0).stdout
            root = ElementTree.fromstring(hierarchy)
            for node in root.iter("node"):
                text = " ".join(
                    (node.attrib.get(key, "") for key in ("text", "content-desc", "resource-id"))
                )
                if label.casefold() not in text.casefold():
                    continue
                match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.attrib.get("bounds", ""))
                if match:
                    return tuple(int(value) for value in match.groups())
        except (OSError, subprocess.SubprocessError, ElementTree.ParseError):
            pass
        finally:
            try:
                run_adb("shell", "rm", "-f", remote_path, timeout=5.0)
            except subprocess.SubprocessError:
                pass
        time.sleep(0.35)
    return None


def accessibility_control_map(
    labels: tuple[str, ...], timeout: float = 12.0
) -> dict[str, tuple[int, int, int, int]]:
    """Resolve several visible controls from one UIAutomator hierarchy dump."""
    remote_path = "/sdcard/kavvoro_capture_hierarchy.xml"
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            run_adb("shell", "uiautomator", "dump", remote_path, timeout=18.0)
            hierarchy = run_adb("shell", "cat", remote_path, timeout=8.0).stdout
            root = ElementTree.fromstring(hierarchy)
            result: dict[str, tuple[int, int, int, int]] = {}
            for node in root.iter("node"):
                text = " ".join(
                    (node.attrib.get(key, "") for key in ("text", "content-desc", "resource-id"))
                )
                match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.attrib.get("bounds", ""))
                if not match:
                    continue
                bounds = tuple(int(value) for value in match.groups())
                for label in labels:
                    if label not in result and label.casefold() in text.casefold():
                        result[label] = bounds
            if len(result) == len(labels):
                return result
            if set(labels) == set(HOME_ACCESSIBILITY_ORDER):
                # Home's virtual controls have a stable declaration order.
                # Their labels are localized, so use button order as a fallback
                # when the connected emulator is not running in English.
                buttons = [
                    tuple(int(value) for value in re.fullmatch(
                        r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                        node.attrib.get("bounds", ""),
                    ).groups())
                    for node in root.iter("node")
                    if node.attrib.get("class") == "android.widget.Button"
                    and node.attrib.get("clickable") == "true"
                    and re.fullmatch(
                        r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                        node.attrib.get("bounds", ""),
                    )
                ]
                if len(buttons) >= len(HOME_ACCESSIBILITY_ORDER):
                    ordered = dict(zip(HOME_ACCESSIBILITY_ORDER, buttons))
                    return {label: ordered[label] for label in labels}
        except (OSError, subprocess.SubprocessError, ElementTree.ParseError):
            pass
        finally:
            try:
                run_adb("shell", "rm", "-f", remote_path, timeout=5.0)
            except subprocess.SubprocessError:
                pass
        time.sleep(0.3)
    missing = [label for label in labels if label not in locals().get("result", {})]
    raise RuntimeError(f"Accessibility controls not found in one hierarchy: {', '.join(missing)}")


def tap_accessibility(label: str, timeout: float = 12.0) -> tuple[int, int]:
    bounds = wait_for_accessibility(label, timeout=timeout)
    left, top, right, bottom = bounds
    point = ((left + right) // 2, (top + bottom) // 2)
    run_adb("shell", "input", "tap", str(point[0]), str(point[1]))
    return point


def wait_for_accessibility(label: str, timeout: float = 12.0) -> tuple[int, int, int, int]:
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        bounds = accessibility_bounds(label, timeout=min(1.5, max(0.2, deadline - time.monotonic())))
        if bounds is not None:
            return bounds
    raise RuntimeError(f"Accessibility control not found: {label}")


def tap_fraction(width: int, height: int, x: float, y: float) -> None:
    """Tap a responsive screen position expressed as a viewport fraction."""
    run_adb("shell", "input", "tap", str(round(width * x)), str(round(height * y)))


def snapshot_shared_preferences() -> dict[str, bytes]:
    """Keep app progress/settings intact when a visual capture enters gameplay."""
    run_adb("shell", "am", "force-stop", PACKAGE)
    listing = run_adb("shell", "run-as", PACKAGE, "ls", "shared_prefs", timeout=12.0).stdout.decode(
        "utf-8", "replace"
    )
    snapshot: dict[str, bytes] = {}
    for name in listing.splitlines():
        name = name.strip()
        if not re.fullmatch(r"[A-Za-z0-9_.-]+\.xml", name):
            continue
        snapshot[name] = run_adb(
            "exec-out", "run-as", PACKAGE, "cat", f"shared_prefs/{name}", timeout=12.0
        ).stdout
    return snapshot


def restore_shared_preferences(snapshot: dict[str, bytes]) -> None:
    """Restore the exact app preference files after visual QA navigation."""
    run_adb("shell", "am", "force-stop", PACKAGE)
    listing = run_adb("shell", "run-as", PACKAGE, "ls", "shared_prefs", timeout=12.0).stdout.decode(
        "utf-8", "replace"
    )
    current = {
        name.strip()
        for name in listing.splitlines()
        if re.fullmatch(r"[A-Za-z0-9_.-]+\.xml", name.strip())
    }
    for name in sorted(current - snapshot.keys()):
        run_adb("shell", "run-as", PACKAGE, "rm", "-f", f"shared_prefs/{name}")

    with tempfile.TemporaryDirectory(prefix="kavvoro-prefs-") as temporary_directory:
        for index, (name, data) in enumerate(sorted(snapshot.items())):
            local_path = Path(temporary_directory) / name
            remote_path = f"/data/local/tmp/kavvoro-prefs-{index}-{name}"
            local_path.write_bytes(data)
            run_adb("push", str(local_path), remote_path, timeout=20.0)
            run_adb("shell", "run-as", PACKAGE, "cp", remote_path, f"shared_prefs/{name}")
            run_adb("shell", "rm", "-f", remote_path)


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
