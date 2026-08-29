from pathlib import Path
import subprocess


THIN_VOICE_INDICES = {11, 13, 14}
DEEP_VOICE_INDICES = {8}
TAIL_FADE_SECONDS = 0.34
TAIL_PADDING_SECONDS = 0.18


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
        "areverse,"
        f"afade=t=in:st=0:d={TAIL_FADE_SECONDS:.2f},"
        "areverse,"
        f"apad=pad_dur={TAIL_PADDING_SECONDS:.2f}"
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
