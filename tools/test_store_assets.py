import unittest
from pathlib import Path

from tools.verify_store_assets import validate_store_assets


ROOT = Path(__file__).resolve().parents[1]


class StoreAssetValidatorTest(unittest.TestCase):
    def test_current_listing_package_has_required_phone_and_video_assets(self):
        report = validate_store_assets(ROOT / "store-assets" / "final")

        self.assertEqual(report.errors, [])
        self.assertEqual(report.phone_screenshot_count, 7)
        self.assertEqual(report.ready_tablet_sets, ["tablet-7", "tablet-10"])

    def test_tablet_sets_have_complete_dedicated_capture_sets(self):
        report = validate_store_assets(ROOT / "store-assets" / "final")

        self.assertEqual(report.tablet_screenshot_counts, {"tablet-7": 7, "tablet-10": 7})
        self.assertEqual(report.withheld_tablet_sets, [])


if __name__ == "__main__":
    unittest.main()
