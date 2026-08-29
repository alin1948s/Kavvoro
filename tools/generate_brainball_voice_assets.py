#!/usr/bin/env python3
"""Generate fixed Brainball selection voice assets for Kavvoro.

This is a production-time helper. The Android app only ships the generated
audio files from res/raw and does not use runtime text-to-speech.
"""

from __future__ import annotations

import asyncio
import shutil
import tempfile
from dataclasses import dataclass
from pathlib import Path

import edge_tts

from brainball_voice_processing import process_voice


RAW_DIR = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"


@dataclass(frozen=True)
class VoiceLine:
    name: str
    line: str
    voice: str
    rate: str
    pitch: str


VOICE_ROTATION = [
    ("en-US-RogerNeural", "+30%", "+10Hz"),
    ("en-US-AvaMultilingualNeural", "+32%", "+16Hz"),
    ("en-US-AndrewMultilingualNeural", "+28%", "-4Hz"),
    ("en-US-EmmaMultilingualNeural", "+34%", "+18Hz"),
    ("en-US-GuyNeural", "+26%", "-8Hz"),
]

VOICE_OVERRIDES = {
    6: ("en-US-AvaMultilingualNeural", "+32%", "+12Hz"),      # BLOP VORO - female
    8: ("en-US-AriaNeural", "+24%", "-28Hz"),                 # LALA VORO - deep female
    11: ("en-US-RogerNeural", "+42%", "+55Hz"),               # ZAZA KAV - very thin male
    13: ("en-US-EmmaMultilingualNeural", "+44%", "+60Hz"),    # BYTE VORO - very thin female
    14: ("en-US-AndrewMultilingualNeural", "+34%", "+38Hz"),  # GLOBO KAV - thin male
    16: ("en-US-GuyNeural", "+24%", "-6Hz"),                  # KING KAV - male
    21: ("en-US-AndrewMultilingualNeural", "+26%", "-10Hz"),  # SLOPPI VORO - male
    37: ("en-US-JennyNeural", "+20%", "-4Hz"),                # SLEEPY VORO - female
}

RAW_LINES = [
    ("VORO PRIME", "Voro Prime! Aura boss, no cap!"),
    ("KAV ZERO", "Kav Zero! Zero brain cells, full send!"),
    ("CHROME VORO", "Chrome Voro! Sigma mirror, giga bonk!"),
    ("NOVA KAV", "Nova Kav! Galaxy rizz, physics cooked!"),
    ("KAVVORO", "Kavvoro! Original brainrot, certified!"),
    ("VORO GRAD", "Voro Grad! Diploma in yapping!"),
    ("BLOP VORO", "Blop Voro! Emotionally aerodynamic!"),
    ("FIZZ KAV", "Fizz Kav! Charger eaten, still zooming!"),
    ("LALA VORO", "Lala Voro! Wrong password, right aura!"),
    ("WOMP KAV", "Womp Kav! Looping thoughts, loud bounce!"),
    ("MIMI VORO", "Mimi Voro! Staring in four K!"),
    ("ZAZA KAV", "Zaza Kav! Homework deleted, zoom mode!"),
    ("TIKKAV RIFT", "Tick Kav Rift! Wind tax collected!"),
    ("BYTE VORO", "Byte Voro! Share button gobbled!"),
    ("GLOBO KAV", "Globo Kav! Gravity got unemployed!"),
    ("ELDER VORO", "Elder Voro! Ancient yap unlocked!"),
    ("KING KAV", "King Kav! Crowned by pure aura!"),
    ("NIBBI KAV", "Nibbi Kav! Forgot the plan, still him!"),
    ("VORO RIZZ", "Voro Rizz! Aura dealer on duty!"),
    ("BONGO KAV", "Bongo Kav! Brain says bonk bonk!"),
    ("GLITCH NONA", "Glitch Nona! Buffering in real life!"),
    ("SLOPPI VORO", "Sloppi Voro! Microwave water legend!"),
    ("KAV KABOOM", "Kav Kaboom! Volume illegal!"),
    ("DRIPPI MIM", "Drippi Mim! Invisible sneakers, loud drip!"),
    ("NAPPA VORO", "Nappa Voro! Sleeping at maximum speed!"),
    ("YAPPA KAV", "Yappa Kav! Podcast with no mic!"),
    ("TURBO BLOB", "Turbo Blob! No brakes, many opinions!"),
    ("WIFI VORO", "Wi Fi Voro! Connected, no internet!"),
    ("CRINGE KAV", "Cringe Kav! Weaponized awkward!"),
    ("KAV 404", "Kav four oh four! Brain not found!"),
    ("PASTA VORO", "Pasta Voro! Sauce department online!"),
    ("LAGGI KAV", "Laggi Kav! Three frames late!"),
    ("MOGGO VORO", "Moggo Voro! Jawline rendered separate!"),
    ("BRAIN BEAN", "Brain Bean! One bean, two thoughts!"),
    ("AURA THIEF", "Aura Thief! Your vibe is mine!"),
    ("GIGI GLITCH", "Gigi Glitch! Reality update failed!"),
    ("NOODLE KAV", "Noodle Kav! Built different, barely!"),
    ("SLEEPY VORO", "Sleepy Voro! Dreaming leaderboard!"),
    ("PANIC BEAN", "Panic Bean! Calm mode unavailable!"),
    ("BOSSY BLOP", "Bossy Blop! Self promoted again!"),
    ("QUANTUM KAV", "Quantum Kav! Winning and crashing!"),
    ("WOBBLE CEO", "Wobble C E O! Fake company acquired!"),
    ("ERROR VORO", "Error Voro! Too cursed to compile!"),
    ("GOLDEN YAP", "Golden Yap! Legendary mouth DLC!"),
    ("VOID JUNIOR", "Void Junior! Tiny void, huge ego!"),
    ("KAV MAXX", "Kav Max! Illegal sliders activated!"),
    ("RIFT RIZZLER", "Rift Rizzler! Gravity subscribed!"),
    ("ULTRA NONA", "Ultra Nona! Yapping battery full!"),
    ("AURA TITAN", "Aura Titan! Awkwardness deleted!"),
    ("FINAL VORO", "Final Voro! Lore ended, bounce didn't!"),
]


def voice_lines() -> list[VoiceLine]:
    lines: list[VoiceLine] = []
    for index, (name, line) in enumerate(RAW_LINES):
        voice, rate, pitch = VOICE_ROTATION[index % len(VOICE_ROTATION)]
        if index < 4 or index >= 40:
            rate = "+38%"
            pitch = "+20Hz" if index % 2 else "-10Hz"
        if index in VOICE_OVERRIDES:
            voice, rate, pitch = VOICE_OVERRIDES[index]
        lines.append(VoiceLine(name, line, voice, rate, pitch))
    return lines


async def synthesize_to_mp3(item: VoiceLine, mp3_path: Path) -> None:
    communicate = edge_tts.Communicate(
        item.line,
        item.voice,
        rate=item.rate,
        pitch=item.pitch,
        volume="+0%",
    )
    await communicate.save(str(mp3_path))


async def generate() -> None:
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    lines = voice_lines()
    with tempfile.TemporaryDirectory(prefix="kavvoro_voice_assets_") as temp_root:
        temp_dir = Path(temp_root)
        staged_ogg_paths: list[Path] = []
        for index, item in enumerate(lines):
            basename = f"brainball_select_{index:02d}"
            mp3_path = temp_dir / f"{basename}.mp3"
            ogg_path = temp_dir / f"{basename}.ogg"
            print(f"{index:02d} {item.name}: {item.voice} {item.rate} {item.pitch}")
            await synthesize_to_mp3(item, mp3_path)
            process_voice(mp3_path, ogg_path, index)
            staged_ogg_paths.append(ogg_path)

        if len(staged_ogg_paths) != 50 or any(not path.exists() for path in staged_ogg_paths):
            raise RuntimeError("Expected 50 generated voice assets before replacing raw files.")

        for index, staged_path in enumerate(staged_ogg_paths):
            basename = f"brainball_select_{index:02d}"
            target = RAW_DIR / f"{basename}.ogg"
            for suffix in (".wav", ".mp3", ".m4a"):
                old_path = RAW_DIR / f"{basename}{suffix}"
                if old_path.exists():
                    old_path.unlink()
            shutil.copy2(staged_path, target)


if __name__ == "__main__":
    asyncio.run(generate())
