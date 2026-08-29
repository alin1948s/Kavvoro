from pathlib import Path
from PIL import Image
import numpy as np

extracted_dir = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd")
slices_dir = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\slices")
slices_dir.mkdir(parents=True, exist_ok=True)

# 1. Inspect 03_English_cropped.png (selected card)
eng = Image.open(extracted_dir / "03_English_cropped.png")
# Card size is 370x120
print("English card size:", eng.size)

# Flag in English card is on the left
# Let's inspect non-transparent regions in left, middle, right of card
flag_crop = eng.crop((12, 16, 12 + 86, 16 + 88))
flag_crop.save(slices_dir / "flag_en_sample.png")

# Radio button in English card is on the right
radio_selected_crop = eng.crop((370 - 70, 25, 370 - 10, 95))
radio_selected_crop.save(slices_dir / "radio_selected_sample.png")

# Unselected card (04_Romana)
ro = Image.open(extracted_dir / "04_Romana_cropped.png")
flag_ro_crop = ro.crop((12, 16, 12 + 86, 16 + 88))
flag_ro_crop.save(slices_dir / "flag_ro_sample.png")
radio_unselected_crop = ro.crop((370 - 70, 25, 370 - 10, 95))
radio_unselected_crop.save(slices_dir / "radio_unselected_sample.png")

# Let's crop all flags from all cards!
cards = [
    ("en", "03_English_cropped.png"),
    ("ro", "04_Romana_cropped.png"),
    ("es", "05_Espanol_cropped.png"),
    ("fr", "06_Francais_cropped.png"),
    ("de", "07_Deutsch_cropped.png"),
    ("it", "08_Italiano_cropped.png"),
    ("pt", "09_Portugues_cropped.png"),
    ("nl", "10_Nederlands_cropped.png"),
    ("pl", "11_Polski_cropped.png"),
    ("cs", "12_Cestina_cropped.png"),
    ("sv", "13_Svenska_cropped.png"),
    ("fi", "14_Suomi_cropped.png"),
    ("tr", "15_Turkce_cropped.png"),
    ("ja", "16_Japanese_cropped.png"),
    ("ko", "17_Korean_cropped.png"),
    ("zh_cn", "18_Chinese_Simplified_cropped.png"),
    ("zh_tw", "19_Chinese_Traditional_cropped.png"),
    ("pt_br", "20_Portugues_BR_cropped.png"),
]

for code, fname in cards:
    card_img = Image.open(extracted_dir / fname)
    # Flag bounding box within card (approx x: 18..98, y: 16..104)
    # Let's find exact flag bbox inside the left 110 pixels
    left_part = card_img.crop((0, 0, 115, card_img.height))
    # Find bounding box of badge in left_part
    # Save the flag badge slice
    f_crop = card_img.crop((18, 16, 98, 104))
    f_crop.save(slices_dir / f"flag_badge_{code}.png")
    print(f"Flag {code} saved")

# Inspect Header
header = Image.open(extracted_dir / "02_Header_Title_Back_cropped.png")
print("Header size:", header.size)
# Back button is on the top-left of header
# Save header components
header.save(slices_dir / "header_full.png")

# Inspect Footer
footer = Image.open(extracted_dir / "21_Current_Language_Footer_cropped.png")
print("Footer size:", footer.size)
footer.save(slices_dir / "footer_full.png")

print("ALL SLICES PROCESSED!")
