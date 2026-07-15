#!/usr/bin/env python3
"""Generate fixed Brainball selection voice assets for Kavvoro.

This is a production-time helper. The Android app only ships the generated
audio files from res/raw and does not use runtime text-to-speech.
"""

from __future__ import annotations

import asyncio
import shutil
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path

import edge_tts


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

THIN_VOICE_INDICES = {11, 13, 14}
DEEP_VOICE_INDICES = {8}


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


def process_voice(mp3_path: Path, ogg_path: Path, index: int) -> None:
    premium = index < 4 or index >= 40
    hi_pitch = 1.075 + (0.035 if premium else 0.0)
    lo_pitch = 0.91 if premium else 0.94
    if index in THIN_VOICE_INDICES:
        hi_pitch = 1.18 if index in (11, 13) else 1.12
        lo_pitch = 1.04 if index in (11, 13) else 0.99
    elif index in DEEP_VOICE_INDICES:
        hi_pitch = 1.015
        lo_pitch = 0.82
    tremolo_rate = 18 + index % 9
    tremolo_depth = 0.10 if premium else 0.055
    echo_delay = 48 + (index % 5) * 8
    sparkle_a = 620 + (index * 97) % 720
    sparkle_b = int(sparkle_a * (1.48 if premium else 1.32))
    filter_complex = (
        "[0:a]"
        "silenceremove=start_periods=1:start_duration=0.04:start_threshold=-50dB,"
        "areverse,"
        "silenceremove=start_periods=1:start_duration=0.06:start_threshold=-50dB,"
        "areverse,"
        "asetpts=PTS-STARTPTS,"
        "highpass=f=130,lowpass=f=9000,"
        "acompressor=threshold=-21dB:ratio=3.2:attack=4:release=72,"
        f"tremolo=f={tremolo_rate}:d={tremolo_depth},"
        "asplit=3[v0][v1][v2];"
        "[v0]volume=1.03[dry];"
        f"[v1]rubberband=pitch={hi_pitch:.3f},adelay=15|15,volume={0.28 if premium else 0.20}[hi];"
        f"[v2]rubberband=pitch={lo_pitch:.3f},adelay=24|24,volume={0.24 if premium else 0.15}[lo];"
        "[1:a]volume=0.060,atrim=0:1.45,afade=t=in:st=0:d=0.02,afade=t=out:st=0.70:d=0.45[spark1];"
        "[2:a]volume=0.040,atrim=0:1.45,afade=t=in:st=0:d=0.02,afade=t=out:st=0.52:d=0.50[spark2];"
        "[dry][hi][lo][spark1][spark2]"
        "amix=inputs=5:duration=longest:normalize=0,"
        "aphaser=in_gain=0.45:out_gain=0.85:delay=2:decay=0.22:speed=0.42:type=t,"
        f"aecho=0.20:0.13:{echo_delay}:0.075,"
        "crystalizer=i=1.7:c=1,"
        "alimiter=limit=0.92,"
        "loudnorm=I=-14:TP=-1.0:LRA=6,"
        "areverse,"
        "silenceremove=start_periods=1:start_duration=0.05:start_threshold=-58dB,"
        "areverse,"
        "apad=pad_dur=0.12"
    )
    subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-hide_banner",
            "-loglevel",
            "error",
            "-i",
            str(mp3_path),
            "-f",
            "lavfi",
            "-i",
            f"sine=frequency={sparkle_a}:duration=1.9",
            "-f",
            "lavfi",
            "-i",
            f"sine=frequency={sparkle_b}:duration=1.9",
            "-filter_complex",
            filter_complex,
            "-ac",
            "1",
            "-ar",
            "44100",
            "-c:a",
            "libvorbis",
            "-q:a",
            "4",
            str(ogg_path),
        ],
        check=True,
    )


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
