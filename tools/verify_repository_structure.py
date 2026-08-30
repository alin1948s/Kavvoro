#!/usr/bin/env python3
"""Fail when repository layout drifts from the documented conventions."""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
ALLOWED_TRACKED_ROOT_FILES = {
    ".firebaserc",
    ".gitattributes",
    ".gitignore",
    "README.md",
    "build.gradle.kts",
    "firebase.json",
    "gradle.properties",
    "gradlew",
    "gradlew.bat",
    "settings.gradle.kts",
}
MACHINE_PATH = re.compile(r"[A-Za-z]:\\Users\\", re.IGNORECASE)
TEXT_SUFFIXES = {".kt", ".kts", ".md", ".py", ".ps1", ".xml", ".json"}


def tracked_files() -> list[str]:
    result = subprocess.run(
        ["git", "ls-files"],
        cwd=PROJECT_ROOT,
        check=True,
        capture_output=True,
        text=True,
    )
    return [line.strip().replace("\\", "/") for line in result.stdout.splitlines() if line.strip()]


def verify_kotlin_packages(errors: list[str]) -> int:
    checked = 0
    for source_root in (PROJECT_ROOT / "app/src/main/java", PROJECT_ROOT / "app/src/test/java"):
        for path in source_root.rglob("*.kt"):
            checked += 1
            first_line = path.read_text(encoding="utf-8").splitlines()[0]
            match = re.fullmatch(r"package\s+([A-Za-z0-9_.]+)", first_line)
            if not match:
                errors.append(f"missing or malformed package declaration: {path.relative_to(PROJECT_ROOT)}")
                continue
            expected_directory = Path(*match.group(1).split("."))
            if path.parent.relative_to(source_root) != expected_directory:
                errors.append(
                    f"package/path mismatch: {path.relative_to(PROJECT_ROOT)} declares {match.group(1)}"
                )
    return checked


def verify_tracked_layout(tracked: list[str], errors: list[str]) -> None:
    unexpected_root = sorted(
        path for path in tracked if "/" not in path and path not in ALLOWED_TRACKED_ROOT_FILES
    )
    errors.extend(f"unexpected tracked root file: {path}" for path in unexpected_root)

    handoff_files = sorted(path for path in tracked if path.startswith("handoff/"))
    if handoff_files != ["handoff/README.md"]:
        errors.append(f"handoff must contain only its README in Git: {handoff_files}")

    for relative in tracked:
        path = PROJECT_ROOT / relative
        if relative.startswith("docs/archive/") or path.suffix.lower() not in TEXT_SUFFIXES:
            continue
        try:
            content = path.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        if MACHINE_PATH.search(content):
            errors.append(f"machine-specific absolute path: {relative}")


def main() -> int:
    errors: list[str] = []
    tracked = tracked_files()
    kotlin_count = verify_kotlin_packages(errors)
    verify_tracked_layout(tracked, errors)

    if errors:
        print("Repository structure validation failed:")
        for error in errors:
            print(f"- {error}")
        return 1

    print(f"Repository structure OK: {len(tracked)} tracked files, {kotlin_count} Kotlin files checked.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
