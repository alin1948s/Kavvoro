from pathlib import Path
from PIL import Image, ImageDraw, ImageOps
import numpy as np

psd_crop_dir = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd")
res_drawable = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi")
res_drawable.mkdir(parents=True, exist_ok=True)

# 1. Export Background
bg = Image.open(psd_crop_dir / "00_Background.png")
bg.save(res_drawable / "bg_language.png")
print("Saved bg_language.png:", bg.size)

# 2. Extract Card Base Template (Unselected & Selected)
# Let's inspect 03_English_cropped (selected card) and 04_Romana_cropped (unselected card)
card_sel = Image.open(psd_crop_dir / "03_English_cropped.png")
card_unsel = Image.open(psd_crop_dir / "04_Romana_cropped.png")

# Let's extract Radio Selected & Radio Unselected
# Radio selected is at roughly x=305..360, y=35..90
radio_sel = card_sel.crop((300, 25, 355, 95))
radio_sel_bbox = radio_sel.getbbox()
if radio_sel_bbox:
    radio_sel_cropped = radio_sel.crop(radio_sel_bbox)
    # Add 2px padding
    padded_sel = Image.new("RGBA", (radio_sel_cropped.width + 4, radio_sel_cropped.height + 4), (0,0,0,0))
    padded_sel.paste(radio_sel_cropped, (2, 2))
    padded_sel.save(res_drawable / "lang_radio_selected.png")
    print("Saved lang_radio_selected.png:", padded_sel.size)

radio_unsel = card_unsel.crop((300, 25, 355, 95))
radio_unsel_bbox = radio_unsel.getbbox()
if radio_unsel_bbox:
    radio_unsel_cropped = radio_unsel.crop(radio_unsel_bbox)
    padded_unsel = Image.new("RGBA", (radio_unsel_cropped.width + 4, radio_unsel_cropped.height + 4), (0,0,0,0))
    padded_unsel.paste(radio_unsel_cropped, (2, 2))
    padded_unsel.save(res_drawable / "lang_radio_unselected.png")
    print("Saved lang_radio_unselected.png:", padded_unsel.size)

# 3. Extract Back Button from Header
header = Image.open(psd_crop_dir / "02_Header_Title_Back_cropped.png")
# Back button is around x=0..120, y=0..120
back_btn = header.crop((10, 10, 115, 115))
back_bbox = back_btn.getbbox()
if back_bbox:
    back_cropped = back_btn.crop(back_bbox)
    back_cropped.save(res_drawable / "lang_back_button.png")
    print("Saved lang_back_button.png:", back_cropped.size)

# 4. Extract Diamond Jewel from Header
# Diamond is at bottom center of header (around x=380..430, y=190..240)
diamond = header.crop((380, 180, 430, 240))
diamond_bbox = diamond.getbbox()
if diamond_bbox:
    diamond_cropped = diamond.crop(diamond_bbox)
    diamond_cropped.save(res_drawable / "lang_diamond.png")
    print("Saved lang_diamond.png:", diamond_cropped.size)

# 5. Extract Footer Panel
footer = Image.open(psd_crop_dir / "21_Current_Language_Footer_cropped.png")
# Let's save footer as asset
footer.save(res_drawable / "lang_footer_panel.png")
print("Saved lang_footer_panel.png:", footer.size)

# 6. Extract Flag Badge Template & All 19 Flags
# Flag position in card_unsel:
# Flag badge bbox is roughly x=20..100, y=20..100
flag_sample = card_unsel.crop((15, 15, 105, 105))
flag_bbox = flag_sample.getbbox()
print("Flag sample bbox:", flag_bbox)

# Let's extract all available flag badges from PSD layers:
flag_mapping = {
    "en": "03_English_cropped.png",
    "ro": "04_Romana_cropped.png",
    "es": "05_Espanol_cropped.png",
    "fr": "06_Francais_cropped.png",
    "de": "07_Deutsch_cropped.png",
    "it": "08_Italiano_cropped.png",
    "pt": "09_Portugues_cropped.png",
    "nl": "10_Nederlands_cropped.png",
    "pl": "11_Polski_cropped.png",
    "cs": "12_Cestina_cropped.png",
    "sv": "13_Svenska_cropped.png",
    "fi": "14_Suomi_cropped.png",
    "tr": "15_Turkce_cropped.png",
    "ja": "16_Japanese_cropped.png",
    "ko": "17_Korean_cropped.png",
    "zh": "18_Chinese_Simplified_cropped.png",
    "zh_tw": "19_Chinese_Traditional_cropped.png",
    "pt_br": "20_Portugues_BR_cropped.png",
}

extracted_flags = {}
for lang, fname in flag_mapping.items():
    c_img = Image.open(psd_crop_dir / fname)
    # flag badge is located at x=20..95, y=20..100
    sub = c_img.crop((18, 16, 96, 104))
    fb = sub.getbbox()
    if fb:
        badge = sub.crop(fb)
        extracted_flags[lang] = badge
        badge.save(res_drawable / f"flag_badge_{lang}.png")
        print(f"Saved flag_badge_{lang}.png: {badge.size}")

# Now let's extract the metallic frame badge mask from 'ro' to generate RU, UK, AR, HI, ID, VI
ro_badge = extracted_flags["ro"]
bw, bh = ro_badge.size
print("Standard Flag Badge Size:", bw, bh)

# Create remaining APK flags (ru, uk, ar, hi, id, vi) with identical metallic badge geometry & bevel:
def create_badge(flag_content_drawer):
    # Base badge image with transparent bg
    img = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    # Draw flag in center (clipped to inner octagon)
    flag_inner = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    flag_content_drawer(flag_inner)
    
    # Create octagonal mask for inner flag
    mask = Image.new("L", (bw, bh), 0)
    m_draw = ImageDraw.Draw(mask)
    # Chamfered octagon coords
    c = 10
    poly = [(c, 4), (bw - c, 4), (bw - 4, c), (bw - 4, bh - c), 
            (bw - c, bh - 4), (c, bh - 4), (4, bh - c), (4, c)]
    m_draw.polygon(poly, fill=255)
    
    img.paste(flag_inner, (0, 0), mask)
    
    # Composite metallic frame border over flag from ro_badge border
    # Extract ro_badge border: pixels near edges
    ro_arr = np.array(ro_badge)
    img_arr = np.array(img)
    # Border mask: outside of inner flag
    border_mask = np.array(mask) < 250
    img_arr[border_mask] = ro_arr[border_mask]
    
    final_badge = Image.fromarray(img_arr)
    return final_badge

# RU (White, Blue, Red)
def draw_ru(im):
    d = ImageDraw.Draw(im)
    h3 = bh / 3
    d.rectangle([0, 0, bw, h3], fill="#FFFFFF")
    d.rectangle([0, h3, bw, h3*2], fill="#0039A6")
    d.rectangle([0, h3*2, bw, bh], fill="#D52B1E")

create_badge(draw_ru).save(res_drawable / "flag_badge_ru.png")
print("Generated flag_badge_ru.png")

# UK (Blue, Yellow)
def draw_uk(im):
    d = ImageDraw.Draw(im)
    h2 = bh / 2
    d.rectangle([0, 0, bw, h2], fill="#0057B7")
    d.rectangle([0, h2, bw, bh], fill="#FFD700")

create_badge(draw_uk).save(res_drawable / "flag_badge_uk.png")
print("Generated flag_badge_uk.png")

# AR (Saudi Green with emblem)
def draw_ar(im):
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, bw, bh], fill="#006C35")
    d.line([bw*0.2, bh*0.65, bw*0.8, bh*0.65], fill="#FFFFFF", width=3)
    d.ellipse([bw*0.35, bh*0.3, bw*0.65, bh*0.55], fill="#FFFFFF")

create_badge(draw_ar).save(res_drawable / "flag_badge_ar.png")
print("Generated flag_badge_ar.png")

# HI (India: Saffron, White, Green + Navy Chakra)
def draw_hi(im):
    d = ImageDraw.Draw(im)
    h3 = bh / 3
    d.rectangle([0, 0, bw, h3], fill="#FF9933")
    d.rectangle([0, h3, bw, h3*2], fill="#FFFFFF")
    d.rectangle([0, h3*2, bw, bh], fill="#128807")
    cx, cy = bw/2, bh/2
    d.ellipse([cx-8, cy-8, cx+8, cy+8], outline="#000088", width=2)

create_badge(draw_hi).save(res_drawable / "flag_badge_hi.png")
print("Generated flag_badge_hi.png")

# ID (Indonesia: Red, White)
def draw_id(im):
    d = ImageDraw.Draw(im)
    h2 = bh / 2
    d.rectangle([0, 0, bw, h2], fill="#FF0000")
    d.rectangle([0, h2, bw, bh], fill="#FFFFFF")

create_badge(draw_id).save(res_drawable / "flag_badge_id.png")
print("Generated flag_badge_id.png")

# VI (Vietnam: Red + Yellow Star)
def draw_vi(im):
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, bw, bh], fill="#DA251D")
    cx, cy, r = bw/2, bh/2, 16
    points = []
    for i in range(10):
        radius = r if i % 2 == 0 else r * 0.4
        ang = i * np.pi / 5 - np.pi / 2
        points.append((cx + np.cos(ang) * radius, cy + np.sin(ang) * radius))
    d.polygon(points, fill="#FFFF00")

create_badge(draw_vi).save(res_drawable / "flag_badge_vi.png")
print("Generated flag_badge_vi.png")

print("ALL ASSETS EXTRACTED AND GENERATED TO drawable-nodpi!")
