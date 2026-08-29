from pathlib import Path
from PIL import Image, ImageDraw
import numpy as np

psd_crop_dir = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd")
res_drawable = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi")

# 1. Clean Card Frames (370x120)
# We can extract selected card from 03_English_cropped.png:
# The flag is at x=18..98, y=16..104
# The radio is at x=300..360, y=25..95
# The text is in the center
# We can get the clean card background from the empty edges and fill the center with the clean dark glass gradient!
sel_img = Image.open(psd_crop_dir / "03_English_cropped.png")
unsel_left = Image.open(psd_crop_dir / "05_Espanol_cropped.png")
unsel_right = Image.open(psd_crop_dir / "04_Romana_cropped.png")

# Let's inspect the frame structure of unsel_left (370x120)
print("unsel_left size:", unsel_left.size)

# Let's create clean card frames:
# To make a clean card frame, keep the border pixels (outer 15px on all 4 sides) and fill inner with the dark glass color #080D17
def make_clean_card(src_img, inner_fill=(8, 13, 23, 245)):
    clean = src_img.copy()
    # Erase inner area where flag, text, radio are: x=16..354, y=14..106
    d = ImageDraw.Draw(clean)
    # Mask of inner region
    # Fill inner with uniform dark glass fill
    d.rectangle([16, 14, 354, 106], fill=inner_fill)
    return clean

card_sel_clean = make_clean_card(sel_img, (13, 28, 42, 245))
card_sel_clean.save(res_drawable / "lang_card_selected.png")

card_left_clean = make_clean_card(unsel_left, (8, 13, 23, 245))
card_left_clean.save(res_drawable / "lang_card_left.png")

card_right_clean = make_clean_card(unsel_right, (8, 13, 23, 245))
card_right_clean.save(res_drawable / "lang_card_right.png")

# 2. Perfect Flag Badge Template:
# From ro_badge (78x88), let's create a transparent cutout of the center flag area
ro_badge = Image.open(res_drawable / "flag_badge_ro.png")
bw, bh = ro_badge.size
ro_arr = np.array(ro_badge)

# The metallic border is on outer pixels.
# Let's create an exact empty frame where the inner flag area is transparent!
# Inner flag area in 78x88:
# x: 10..68, y: 10..78
# Let's make an overlay frame image:
overlay_frame = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
o_arr = np.array(overlay_frame)

# Copy the metallic border from ro_badge:
# Let's find which pixels in ro_badge are the metallic frame vs the flag
# The frame has golden/metallic colors (#C29B38, #8C6D23, #4A3C1A) or gradient
# Outer 8 pixels around the octagon are 100% frame
for y in range(bh):
    for x in range(bw):
        # distance from center
        dx = abs(x - bw/2) / (bw/2)
        dy = abs(y - bh/2) / (bh/2)
        # Corner chamfer check
        if (x < 10 and y < 10) or (x > bw-10 and y < 10) or (x < 10 and y > bh-10) or (x > bw-10 and y > bh-10):
            o_arr[y, x] = ro_arr[y, x]
        elif x <= 9 or x >= bw-10 or y <= 9 or y >= bh-10:
            o_arr[y, x] = ro_arr[y, x]

frame_img = Image.fromarray(o_arr)
frame_img.save(res_drawable / "flag_badge_frame_template.png")

# Now re-generate the 6 remaining flags with the exact metallic frame overlaid:
def make_flag_with_frame(draw_func):
    base = Image.new("RGBA", (bw, bh), (0, 0, 0, 255))
    draw_func(base)
    # Mask to octagon
    mask = Image.new("L", (bw, bh), 0)
    m_draw = ImageDraw.Draw(mask)
    c = 10
    poly = [(c, 0), (bw - c, 0), (bw, c), (bw, bh - c), 
            (bw - c, bh), (c, bh), (0, bh - c), (0, c)]
    m_draw.polygon(poly, fill=255)
    
    out = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    out.paste(base, (0, 0), mask)
    out.paste(ro_badge, (0, 0), frame_img)
    return out

# RU
def draw_ru(im):
    d = ImageDraw.Draw(im)
    h3 = bh / 3
    d.rectangle([0, 0, bw, h3], fill="#FFFFFF")
    d.rectangle([0, h3, bw, h3*2], fill="#0039A6")
    d.rectangle([0, h3*2, bw, bh], fill="#D52B1E")

make_flag_with_frame(draw_ru).save(res_drawable / "flag_badge_ru.png")

# UK
def draw_uk(im):
    d = ImageDraw.Draw(im)
    h2 = bh / 2
    d.rectangle([0, 0, bw, h2], fill="#0057B7")
    d.rectangle([0, h2, bw, bh], fill="#FFD700")

make_flag_with_frame(draw_uk).save(res_drawable / "flag_badge_uk.png")

# AR
def draw_ar(im):
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, bw, bh], fill="#006C35")
    d.line([bw*0.2, bh*0.62, bw*0.8, bh*0.62], fill="#FFFFFF", width=3)
    d.ellipse([bw*0.35, bh*0.32, bw*0.65, bh*0.52], fill="#FFFFFF")

make_flag_with_frame(draw_ar).save(res_drawable / "flag_badge_ar.png")

# HI
def draw_hi(im):
    d = ImageDraw.Draw(im)
    h3 = bh / 3
    d.rectangle([0, 0, bw, h3], fill="#FF9933")
    d.rectangle([0, h3, bw, h3*2], fill="#FFFFFF")
    d.rectangle([0, h3*2, bw, bh], fill="#128807")
    cx, cy = bw/2, bh/2
    d.ellipse([cx-8, cy-8, cx+8, cy+8], outline="#000088", width=2)

make_flag_with_frame(draw_hi).save(res_drawable / "flag_badge_hi.png")

# ID
def draw_id(im):
    d = ImageDraw.Draw(im)
    h2 = bh / 2
    d.rectangle([0, 0, bw, h2], fill="#FF0000")
    d.rectangle([0, h2, bw, bh], fill="#FFFFFF")

make_flag_with_frame(draw_id).save(res_drawable / "flag_badge_id.png")

# VI
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

make_flag_with_frame(draw_vi).save(res_drawable / "flag_badge_vi.png")

# 3. Clean Footer Panel:
# In footer (810x275), erase the hardcoded text "CURRENT: ENGLISH" and dot
# The text is around x=60..380, y=70..170
footer = Image.open(psd_crop_dir / "21_Current_Language_Footer_cropped.png")
f_clean = footer.copy()
f_draw = ImageDraw.Draw(f_clean)
# Fill inner content area with dark #040810
f_draw.rectangle([50, 70, 760, 190], fill=(4, 8, 16, 240))
f_clean.save(res_drawable / "lang_footer_panel.png")

print("ALL ASSETS CLEANED AND SAVED!")
