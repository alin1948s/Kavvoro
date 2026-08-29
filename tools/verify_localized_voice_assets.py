#!/usr/bin/env python3
"""Validate the shipped Brainball selection voice roster."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path


SUPPORTED_LOCALES = (
    "en", "ro", "es", "fr", "de", "it", "pt", "nl", "pl", "tr",
    "ru", "uk", "ar", "hi", "id", "vi", "ja", "ko", "zh",
)
EXPECTED_BRAINBALLS = 50
LOCALE_FILE = re.compile(r"^brainball_select_(?P<locale>[a-z]{2})_(?P<index>\d{2})\.ogg$")
ENGLISH_FILE = re.compile(r"^brainball_select_(?P<index>\d{2})\.ogg$")


def collect_inventory(raw_dir: Path) -> dict[str, list[Path]]:
    inventory = {locale: [] for locale in SUPPORTED_LOCALES}
    for path in raw_dir.glob("brainball_select*.ogg"):
        match = LOCALE_FILE.match(path.name)
        if match:
            locale = match.group("locale")
            if locale in inventory:
                inventory[locale].append(path)
            continue
        if ENGLISH_FILE.match(path.name):
            inventory["en"].append(path)
    for paths in inventory.values():
        paths.sort()
    return inventory


def validate(raw_dir: Path) -> list[str]:
    errors: list[str] = []
    inventory = collect_inventory(raw_dir)
    for locale in SUPPORTED_LOCALES:
        paths = inventory[locale]
        if len(paths) != EXPECTED_BRAINBALLS:
            errors.append(f"{locale}: expected {EXPECTED_BRAINBALLS} files, found {len(paths)}")
        for path in paths:
            data = path.read_bytes()
            if not data:
                errors.append(f"{locale}: empty file {path.name}")
            elif not data.startswith(b"OggS"):
                errors.append(f"{locale}: invalid OGG container {path.name}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--raw-dir", type=Path, default=Path("app/src/main/res/raw"))
    args = parser.parse_args()
    inventory = collect_inventory(args.raw_dir)
    errors = validate(args.raw_dir)
    summary = {locale: len(paths) for locale, paths in inventory.items()}
    print(json.dumps({"total": sum(summary.values()), "by_locale": summary, "errors": errors}, ensure_ascii=False, indent=2))
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
