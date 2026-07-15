import math
import os
import subprocess
import wave
from pathlib import Path

import numpy as np


ROOT = Path(__file__).resolve().parents[1]
RAW_DIR = ROOT / "app" / "src" / "main" / "res" / "raw"
TMP_DIR = ROOT / "build" / "generated" / "kavvoro_music"
SR = 44_100


def midi_to_hz(midi: float) -> float:
    return 440.0 * (2.0 ** ((midi - 69.0) / 12.0))


def envelope(length: int, attack: float, decay: float, sustain: float, release: float) -> np.ndarray:
    env = np.ones(length, dtype=np.float32) * sustain
    a = min(length, int(attack * SR))
    d = min(max(length - a, 0), int(decay * SR))
    r = min(length, int(release * SR))
    if a > 0:
        env[:a] = np.linspace(0.0, 1.0, a, endpoint=False, dtype=np.float32)
    if d > 0:
        env[a:a + d] = np.linspace(1.0, sustain, d, endpoint=False, dtype=np.float32)
    if r > 0:
        env[-r:] *= np.linspace(1.0, 0.0, r, endpoint=True, dtype=np.float32)
    return env


def stereo_pan(signal: np.ndarray, pan: float) -> np.ndarray:
    pan = float(np.clip(pan, -1.0, 1.0))
    left = math.cos((pan + 1.0) * math.pi / 4.0)
    right = math.sin((pan + 1.0) * math.pi / 4.0)
    return np.column_stack((signal * left, signal * right)).astype(np.float32)


def add(buf: np.ndarray, start: float, stereo: np.ndarray) -> None:
    begin = int(start * SR)
    if begin >= len(buf):
        return
    end = min(len(buf), begin + len(stereo))
    if end <= begin:
        return
    buf[begin:end] += stereo[: end - begin]


def tone(
    duration: float,
    midi: float,
    *,
    wave_name: str,
    volume: float,
    attack: float = 0.01,
    decay: float = 0.08,
    sustain: float = 0.65,
    release: float = 0.08,
    detune: float = 0.0,
    phase: float = 0.0,
) -> np.ndarray:
    n = max(1, int(duration * SR))
    t = np.arange(n, dtype=np.float32) / SR
    freq = midi_to_hz(midi + detune)
    ph = 2.0 * np.pi * freq * t + phase
    if wave_name == "bass":
        sig = np.sin(ph) * 0.82 + np.sin(ph * 0.5) * 0.38 + np.sin(ph * 2.0) * 0.08
        sig = np.tanh(sig * 1.35)
    elif wave_name == "pad":
        sig = (
            np.sin(ph) * 0.62
            + np.sin(ph * 2.0 + 0.2) * 0.2
            + np.sin(ph * 3.0 + 0.5) * 0.08
            + np.sin(ph * 0.5 + 1.0) * 0.14
        )
    elif wave_name == "soft_pluck":
        sig = np.sin(ph) * 0.78 + np.sin(ph * 2.0) * 0.18 + np.sin(ph * 4.0) * 0.05
    elif wave_name == "lead":
        sig = np.sin(ph) * 0.55 + np.sin(ph * 2.0) * 0.25 + np.sin(ph * 3.0) * 0.14
        sig = np.tanh(sig * 1.18)
    elif wave_name == "chaos_bass":
        sig = np.sin(ph) * 0.62 + np.sin(ph * 0.5) * 0.34 + np.sin(ph * 1.01 + 0.8) * 0.34
        sig = np.tanh(sig * 2.25)
    else:
        sig = np.sin(ph)
    sig *= envelope(n, attack, decay, sustain, release) * volume
    return sig.astype(np.float32)


def chord(buf: np.ndarray, start: float, duration: float, notes: list[int], *, volume: float, pan: float = 0.0) -> None:
    mix = np.zeros(int(duration * SR), dtype=np.float32)
    for i, note in enumerate(notes):
        mix += tone(
            duration,
            note,
            wave_name="pad",
            volume=volume / max(1, len(notes)),
            attack=0.22,
            decay=0.5,
            sustain=0.78,
            release=0.75,
            detune=(-0.035 if i % 2 == 0 else 0.035),
            phase=i * 0.31,
        )
    add(buf, start, stereo_pan(mix, pan))


def add_note(buf: np.ndarray, start: float, duration: float, midi: float, wave_name: str, volume: float, pan: float) -> None:
    add(buf, start, stereo_pan(tone(duration, midi, wave_name=wave_name, volume=volume), pan))


def kick(duration: float = 0.36, volume: float = 0.8) -> np.ndarray:
    n = int(duration * SR)
    t = np.arange(n, dtype=np.float32) / SR
    freq = 42.0 + 78.0 * np.exp(-t * 19.0)
    phase = np.cumsum(freq) / SR * 2.0 * np.pi
    sig = np.sin(phase) * np.exp(-t * 8.4)
    click = np.exp(-t * 92.0) * np.sin(2.0 * np.pi * 1450.0 * t) * 0.12
    return np.tanh((sig + click) * 1.45).astype(np.float32) * volume


def snare(duration: float = 0.28, volume: float = 0.42, seed: int = 1) -> np.ndarray:
    rng = np.random.default_rng(seed)
    n = int(duration * SR)
    t = np.arange(n, dtype=np.float32) / SR
    noise = rng.normal(0.0, 1.0, n).astype(np.float32)
    noise = noise - np.convolve(noise, np.ones(80, dtype=np.float32) / 80.0, mode="same")
    body = np.sin(2.0 * np.pi * 185.0 * t) * 0.22
    env = np.exp(-t * 12.0)
    return np.tanh((noise * 0.62 + body) * env * 1.3).astype(np.float32) * volume


def hat(duration: float = 0.08, volume: float = 0.22, seed: int = 2) -> np.ndarray:
    rng = np.random.default_rng(seed)
    n = int(duration * SR)
    t = np.arange(n, dtype=np.float32) / SR
    noise = rng.normal(0.0, 1.0, n).astype(np.float32)
    noise = noise - np.convolve(noise, np.ones(28, dtype=np.float32) / 28.0, mode="same")
    return (noise * np.exp(-t * 46.0) * volume).astype(np.float32)


def add_drums(buf: np.ndarray, tempo: float, bars: int, *, density: str, seed: int) -> None:
    beat = 60.0 / tempo
    for bar in range(bars):
        base = bar * 4 * beat
        add(buf, base, stereo_pan(kick(volume=0.62 if density == "soft" else 0.82), 0.0))
        if density != "soft":
            add(buf, base + 2 * beat, stereo_pan(kick(volume=0.45), 0.0))
        add(buf, base + (2 if density != "soft" else 3) * beat, stereo_pan(snare(seed=seed + bar), 0.03))
        steps = 8 if density == "soft" else 16
        for i in range(steps):
            if density == "soft" and i % 2 == 1:
                continue
            if density == "chaos" and i % 5 == 0:
                add(buf, base + i * 4 * beat / steps, stereo_pan(hat(0.055, 0.2, seed + bar * 17 + i), -0.34 + (i % 3) * 0.24))
            else:
                add(buf, base + i * 4 * beat / steps, stereo_pan(hat(0.05, 0.11 if density == "soft" else 0.16, seed + bar * 17 + i), -0.24 + (i % 2) * 0.48))


def bassline(buf: np.ndarray, tempo: float, pattern: list[int], root_shift: int, *, wave_name: str, volume: float) -> None:
    beat = 60.0 / tempo
    for step, note in enumerate(pattern):
        start = step * beat * 0.5
        midi = note + root_shift
        add_note(buf, start, beat * 0.46, midi, wave_name, volume, -0.04)


def melody(buf: np.ndarray, tempo: float, notes: list[int], *, wave_name: str, volume: float, pan: float) -> None:
    beat = 60.0 / tempo
    for i, note in enumerate(notes):
        if note < 0:
            continue
        add_note(buf, i * beat * 0.5, beat * 0.34, note, wave_name, volume * (0.75 + (i % 3) * 0.1), pan if i % 2 == 0 else -pan)


def finish(buf: np.ndarray) -> np.ndarray:
    buf = np.tanh(buf * 0.88)
    fade = int(0.035 * SR)
    curve = np.linspace(0.0, 1.0, fade, dtype=np.float32)
    buf[:fade] *= curve[:, None]
    buf[-fade:] *= curve[::-1, None]
    peak = float(np.max(np.abs(buf)))
    if peak > 0:
        buf *= min(0.96 / peak, 1.0)
    return buf


def write_wav(path: Path, buf: np.ndarray) -> None:
    pcm = np.clip(buf, -1.0, 1.0)
    pcm16 = (pcm * 32767.0).astype("<i2")
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(2)
        wav.setsampwidth(2)
        wav.setframerate(SR)
        wav.writeframes(pcm16.tobytes())


def make_track(name: str, tempo: float, bars: int, mood: str) -> None:
    duration = bars * 4 * 60.0 / tempo
    buf = np.zeros((int(duration * SR), 2), dtype=np.float32)
    beat = 60.0 / tempo

    if mood == "menu":
        chords = [[50, 57, 62, 65], [46, 53, 58, 62], [53, 57, 60, 65], [48, 55, 60, 64]]
        for bar in range(bars):
            chord(buf, bar * 4 * beat, 4 * beat, chords[bar % 4], volume=0.32, pan=0.05)
        bassline(buf, tempo, ([38, 38, 45, 38, 46, 46, 41, 45] * (bars * 2))[: bars * 8], 0, wave_name="bass", volume=0.28)
        melody(buf, tempo, ([62, -1, 65, 64, 60, -1, 57, -1, 60, 62, -1, 64, 65, -1, 67, -1] * bars)[: bars * 8], wave_name="soft_pluck", volume=0.12, pan=0.36)
        add_drums(buf, tempo, bars, density="soft", seed=100)
    elif mood == "tutorial":
        chords = [[48, 55, 60, 64], [45, 52, 57, 60], [50, 57, 62, 65], [43, 50, 55, 59]]
        for bar in range(bars):
            chord(buf, bar * 4 * beat, 4 * beat, chords[bar % 4], volume=0.28, pan=-0.04)
        bassline(buf, tempo, ([36, 43, 48, 43, 41, 48, 45, 48] * (bars * 2))[: bars * 8], 0, wave_name="bass", volume=0.22)
        melody(buf, tempo, ([60, 62, 64, -1, 67, 64, 62, -1, 59, 60, 62, -1, 64, 62, 60, -1] * bars)[: bars * 8], wave_name="soft_pluck", volume=0.11, pan=0.42)
        add_drums(buf, tempo, bars, density="soft", seed=200)
    elif mood == "classic":
        chords = [[50, 57, 62, 65], [48, 55, 60, 64], [46, 53, 58, 62], [45, 52, 57, 60]]
        for bar in range(bars):
            chord(buf, bar * 4 * beat, 4 * beat, chords[bar % 4], volume=0.25, pan=0.0)
        bassline(buf, tempo, ([38, 38, 50, 45, 48, 48, 55, 50, 46, 46, 53, 50, 45, 45, 52, 48] * bars)[: bars * 8], 0, wave_name="bass", volume=0.34)
        melody(buf, tempo, ([74, 72, 69, 72, 77, -1, 74, 72, 69, 67, 69, 72, 74, -1, 72, 69] * bars)[: bars * 8], wave_name="lead", volume=0.105, pan=0.5)
        add_drums(buf, tempo, bars, density="normal", seed=300)
    else:
        chords = [[41, 48, 53, 56], [44, 51, 56, 60], [46, 53, 58, 61], [39, 46, 51, 55]]
        for bar in range(bars):
            chord(buf, bar * 4 * beat, 4 * beat, chords[bar % 4], volume=0.22, pan=0.02)
        bassline(buf, tempo, ([29, 41, 29, 44, 29, 46, 41, 39, 32, 44, 32, 46, 32, 48, 44, 41] * bars)[: bars * 8], 0, wave_name="chaos_bass", volume=0.38)
        melody(buf, tempo, ([65, 68, -1, 72, 70, -1, 68, 65, 77, -1, 75, 72, 70, 68, -1, 65] * bars)[: bars * 8], wave_name="lead", volume=0.1, pan=0.58)
        add_drums(buf, tempo, bars, density="chaos", seed=400)

    wav_path = TMP_DIR / f"{name}.wav"
    ogg_path = RAW_DIR / f"{name}.ogg"
    write_wav(wav_path, finish(buf))
    subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-i",
            str(wav_path),
            "-c:a",
            "libvorbis",
            "-q:a",
            "4",
            str(ogg_path),
        ],
        check=True,
    )


def main() -> None:
    os.makedirs(TMP_DIR, exist_ok=True)
    os.makedirs(RAW_DIR, exist_ok=True)
    make_track("music_menu", 96.0, 12, "menu")
    make_track("music_tutorial", 90.0, 12, "tutorial")
    make_track("music_classic", 124.0, 16, "classic")
    make_track("music_chaos", 142.0, 16, "chaos")


if __name__ == "__main__":
    main()
