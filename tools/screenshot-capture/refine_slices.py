from pathlib import Path
from PIL import Image

psd_crop_dir = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd")
res_drawable = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi")

header = Image.open(psd_crop_dir / "02_Header_Title_Back_cropped.png")
# Header full size: 810x240
# Let's crop just the back button octagon (around x=30..130, y=30..130 in original PSD coords)
# In header_full (offset bbox was (15, 20, 825, 260)), so x_in_header = x_psd - 15, y_in_header = y_psd - 20
# In PSD, back button is at roughly x=40..130, y=40..130
# Let's inspect slice x=15..125, y=15..125
btn_crop = header.crop((18, 18, 112, 115))
# Let's get bbox
btn_crop.save(res_drawable / "lang_back_button.png")
print("New Back Button size:", btn_crop.size)

# Diamond in header_full:
# Diamond is around x=380..425, y=170..215
diamond_crop = header.crop((385, 175, 425, 215))
diamond_bbox = diamond_crop.getbbox()
if diamond_bbox:
    d_clean = diamond_crop.crop(diamond_bbox)
    d_clean.save(res_drawable / "lang_diamond.png")
    print("New Diamond size:", d_clean.size)

# Let's crop the Card Backgrounds (Selected & Unselected)
# Selected card: 03_English_cropped.png (370x120)
card_sel = Image.open(psd_crop_dir / "03_English_cropped.png")
# Unselected card: 04_Romana_cropped.png (370x120)
card_unsel = Image.open(psd_crop_dir / "04_Romana_cropped.png")

# To get clean card frame without flag and text, we can use card_unsel, 
# or we can render the card dynamically or as 9-patch/drawable!
card_sel.save(res_drawable / "lang_card_selected_sample.png")
card_unsel.save(res_drawable / "lang_card_unselected_sample.png")
