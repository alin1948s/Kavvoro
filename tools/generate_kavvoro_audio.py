#!/usr/bin/env python3
"""Generate Kavvoro's deterministic, license-free game audio palette."""

from __future__ import annotations

import math
import random
import struct
import wave
from pathlib import Path


SAMPLE_RATE = 22_050
RAW_DIR = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"


def oscillator(kind: int, phase: float) -> float:
    cycle = (phase / (2.0 * math.pi)) % 1.0
    if kind == 0:
        return math.sin(phase)
    if kind == 1:
        return 1.0 if cycle < 0.5 else -1.0
    if kind == 2:
        return 2.0 * cycle - 1.0
    if kind == 3:
        return 1.0 - 4.0 * abs(cycle - 0.5)
    return math.sin(phase) * (0.72 + 0.28 * math.sin(phase * 0.5))


def envelope(t: float, duration: float, attack: float = 0.008, release: float = 0.13) -> float:
    rise = min(1.0, t / max(attack, 0.001))
    fall = min(1.0, (duration - t) / max(release, 0.001))
    return max(0.0, rise * fall) ** 1.35


def write_wav(name: str, samples: list[float]) -> None:
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    peak = max(0.001, max(abs(value) for value in samples))
    gain = 0.9 / peak
    pcm = bytearray()
    for value in samples:
        clipped = max(-1.0, min(1.0, value * gain))
        pcm.extend(struct.pack("<h", int(clipped * 32_767)))
    with wave.open(str(RAW_DIR / f"{name}.wav"), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(SAMPLE_RATE)
        output.writeframes(pcm)


def brainball_voice(index: int) -> list[float]:
    rng = random.Random(0x4B4156 + index * 7919)
    family = index % 10
    duration = 0.31 + (index % 4) * 0.035
    base = 155.0 + (index * 47 % 390)
    notes = 2 + index % 3
    ratios = [1.0, 1.2, 1.5, 1.75, 2.0]
    kind = family % 5
    samples: list[float] = []

    for sample_index in range(int(duration * SAMPLE_RATE)):
        t = sample_index / SAMPLE_RATE
        position = min(notes - 1, int(t / duration * notes))
        local = (t - position * duration / notes) / (duration / notes)
        ratio = ratios[(index + position * 2) % len(ratios)]
        direction = -1.0 if family in (1, 6, 8) else 1.0
        sweep = 1.0 + direction * (local - 0.5) * (0.08 + family * 0.012)
        vibrato = 1.0 + math.sin(t * (18.0 + family * 1.7)) * (0.006 + (index % 5) * 0.002)
        frequency = base * ratio * sweep * vibrato
        phase = 2.0 * math.pi * frequency * t
        body = oscillator(kind, phase)
        sub = math.sin(phase * 0.5 + family * 0.23) * 0.28
        sparkle = math.sin(phase * (2.0 + (index % 3) * 0.5)) * 0.16
        noise = (rng.random() * 2.0 - 1.0) * (0.045 + (0.04 if family in (4, 8) else 0.0))
        gate = 1.0
        if family in (4, 8):
            gate = 0.34 if int(t * (42 + index % 9)) % 5 == 0 else 1.0
        if family == 2:
            body *= math.exp(-local * 3.2)
            sub += math.sin(phase * 0.25) * 0.35
        if family == 6:
            body = math.sin(phase + math.sin(phase * 0.21) * 4.2)
        accent = math.exp(-((local - 0.08) / 0.07) ** 2) * math.sin(phase * 3.0) * 0.22
        samples.append((body * 0.56 + sub + sparkle + noise + accent) * gate * envelope(t, duration))
    return samples


def bounce_sound(tier: int) -> list[float]:
    rng = random.Random(9000 + tier)
    duration = 0.075 + tier * 0.012
    samples: list[float] = []
    for sample_index in range(int(duration * SAMPLE_RATE)):
        t = sample_index / SAMPLE_RATE
        progress = t / duration
        frequency = 240.0 + tier * 58.0
        phase = 2.0 * math.pi * frequency * t * (1.0 - progress * 0.32)
        click = (rng.random() * 2.0 - 1.0) * math.exp(-progress * 24.0) * 0.65
        rubber = math.sin(phase + math.sin(phase * 0.27) * 1.5) * math.exp(-progress * (7.0 - tier * 0.25))
        samples.append(rubber * 0.72 + click)
    return samples


def tonal_sweep(start: float, end: float, duration: float, pulse: float = 0.0) -> list[float]:
    rng = random.Random(int(start * 17 + end * 31))
    samples: list[float] = []
    phase = 0.0
    for sample_index in range(int(duration * SAMPLE_RATE)):
        t = sample_index / SAMPLE_RATE
        progress = t / duration
        frequency = start * ((end / start) ** progress)
        phase += 2.0 * math.pi * frequency / SAMPLE_RATE
        gate = 1.0 if pulse <= 0.0 or int(t * pulse) % 2 == 0 else 0.36
        value = math.sin(phase) * 0.62 + math.sin(phase * 1.51) * 0.22
        value += (rng.random() * 2.0 - 1.0) * 0.035
        samples.append(value * gate * envelope(t, duration, 0.006, duration * 0.42))
    return samples


def generate() -> None:
    for index in range(50):
        write_wav(f"brainball_select_{index:02d}", brainball_voice(index))
    for tier in range(1, 9):
        write_wav(f"sfx_bounce_{tier}", bounce_sound(tier))

    effects = {
        "sfx_ui_tap": tonal_sweep(620.0, 760.0, 0.075),
        "sfx_locked": tonal_sweep(190.0, 105.0, 0.22, 18.0),
        "sfx_rift_on": tonal_sweep(175.0, 880.0, 0.24, 22.0),
        "sfx_rift_off": tonal_sweep(720.0, 150.0, 0.16),
        "sfx_goal": tonal_sweep(290.0, 1_450.0, 0.52, 13.0),
        "sfx_fail": tonal_sweep(330.0, 72.0, 0.43, 17.0),
        "sfx_power": tonal_sweep(240.0, 1_180.0, 0.36, 27.0),
        "sfx_unlock": tonal_sweep(410.0, 1_620.0, 0.58, 15.0),
        "sfx_chain": tonal_sweep(760.0, 1_040.0, 0.095),
    }
    for name, samples in effects.items():
        write_wav(name, samples)


if __name__ == "__main__":
    generate()
