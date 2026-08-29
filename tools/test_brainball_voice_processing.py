import math
import struct
import subprocess
import tempfile
import unittest
import wave
from pathlib import Path

from brainball_voice_processing import process_voice


class BrainballVoiceProcessingTest(unittest.TestCase):
    def test_short_voice_keeps_a_soft_tail(self):
        with tempfile.TemporaryDirectory(prefix="kavvoro_voice_test_") as temp_dir:
            root = Path(temp_dir)
            source = root / "source.wav"
            output = root / "output.ogg"

            with wave.open(str(source), "wb") as audio:
                audio.setnchannels(1)
                audio.setsampwidth(2)
                audio.setframerate(44100)
                for index in range(int(0.75 * 44100)):
                    sample = int(12000 * math.sin(2 * math.pi * 440 * index / 44100))
                    audio.writeframes(struct.pack("<h", sample))

            process_voice(source, output, 0)
            duration = float(
                subprocess.check_output(
                    [
                        "ffprobe",
                        "-v",
                        "error",
                        "-show_entries",
                        "format=duration",
                        "-of",
                        "default=noprint_wrappers=1:nokey=1",
                        str(output),
                    ],
                    text=True,
                ).strip()
            )

            self.assertGreaterEqual(duration, 1.20)


if __name__ == "__main__":
    unittest.main()
