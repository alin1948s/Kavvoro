"""Shared, machine-independent configuration for screenshot capture tools."""

from __future__ import annotations

import os
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parents[1]
PACKAGE = "com.moonsolstudios.kavvoro"
APK = PROJECT_ROOT / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
PRIVACY_XML = SCRIPT_DIR / "fixtures" / "privacy_profile.xml"

TARGETS = (
    ("phone-360x800.png", 360, 800),
    ("phone-412x915.png", 412, 915),
    ("phone-480x854.png", 480, 854),
    ("phone-720x1280.png", 720, 1280),
    ("phone-1080x2400.png", 1080, 2400),
    ("tablet-600x1024.png", 600, 1024),
    ("tablet-800x1280.png", 800, 1280),
    ("tablet-1024x1366.png", 1024, 1366),
    ("tablet-1200x1920.png", 1200, 1920),
    ("tablet-1536x2048.png", 1536, 2048),
    ("tablet-1600x2560.png", 1600, 2560),
)


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
