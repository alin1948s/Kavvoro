"""Retake the Language page across the seven approved portrait profiles."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path


SCRIPT = Path(__file__).with_name("retake_ui_pages_matrix.py")


if __name__ == "__main__":
    raise SystemExit(
        subprocess.run(
            [sys.executable, str(SCRIPT), "--orientation", "portrait", "--only", "language"],
            check=False,
        ).returncode
    )
