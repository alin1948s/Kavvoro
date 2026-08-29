import unittest
from pathlib import Path

from verify_localized_voice_assets import (
    EXPECTED_BRAINBALLS,
    SUPPORTED_LOCALES,
    collect_inventory,
    validate,
)


class LocalizedVoiceAssetsTest(unittest.TestCase):
    def test_every_supported_locale_has_the_complete_roster(self) -> None:
        raw_dir = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"
        inventory = collect_inventory(raw_dir)
        self.assertEqual(set(inventory), set(SUPPORTED_LOCALES))
        self.assertTrue(all(len(paths) == EXPECTED_BRAINBALLS for paths in inventory.values()))

    def test_every_voice_file_is_non_empty_ogg(self) -> None:
        raw_dir = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"
        self.assertEqual(validate(raw_dir), [])


if __name__ == "__main__":
    unittest.main()
