from PIL import Image, ImageDraw
import numpy as np

ro = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_ro.png")
bw, bh = ro.size
print("ro size:", bw, bh)

# Let's find the inner octagon by looking at flag_badge_ro
# In flag_badge_ro (78x88), the flag bounds are:
# x: 8 to 70, y: 8 to 80
# The 4 chamfer corners cut roughly 16 pixels
mask = Image.new("L", (bw, bh), 0)
d = ImageDraw.Draw(mask)
c = 18
poly = [(c, 8), (bw - c, 8), (bw - 8, c), (bw - 8, bh - c), 
        (bw - c, bh - 8), (c, bh - 8), (8, bh - c), (8, c)]
d.polygon(poly, fill=255)

# Extract frame from ro:
# The frame is where mask == 0 plus a 4px inner border bevel
# Let's copy ro and erase the inside of polygon
frame = ro.copy()
f_draw = ImageDraw.Draw(frame)
# erase inner flag area with transparency
d_poly = [(c+4, 12), (bw - c - 4, 12), (bw - 12, c + 4), (bw - 12, bh - c - 4), 
          (bw - c - 4, bh - 12), (c + 4, bh - 12), (12, bh - c - 4), (12, c + 4)]
f_draw.polygon(d_poly, fill=(0, 0, 0, 0))

def create_flag_badge(draw_func):
    bg = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    flag_content = Image.new("RGBA", (bw, bh), (0, 0, 0, 255))
    draw_func(flag_content)
    # Clip flag_content to the inner octagon
    bg.paste(flag_content, (0, 0), mask)
    # Composite metallic frame on top
    bg.paste(frame, (0, 0), frame)
    return bg

# RU
def draw_ru(im):
    d = ImageDraw.Draw(im)
    h3 = bh / 3
    d.rectangle([0, 0, bw, h3], fill="#FFFFFF")
    d.rectangle([0, h3, bw, h3*2], fill="#0039A6")
    d.rectangle([0, h3*2, bw, bh], fill="#D52B1E")

create_flag_badge(draw_ru).save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_ru.png")

# UK
def draw_uk(im):
    d = ImageDraw.Draw(im)
    h2 = bh / 2
    d.rectangle([0, 0, bw, h2], fill="#0057B7")
    d.rectangle([0, h2, bw, bh], fill="#FFD700")

create_flag_badge(draw_uk).save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_uk.png")

# AR
def draw_ar(im):
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, bw, bh], fill="#006C35")
    d.line([bw*0.2, bh*0.62, bw*0.8, bh*0.62], fill="#FFFFFF", width=3)
    d.ellipse([bw*0.35, bh*0.32, bw*0.65, bh*0.52], fill="#FFFFFF")

create_flag_badge(draw_ar).save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_ar.png")

# HI
def draw_hi(im):
    d = ImageDraw.Draw(im)
    h3 = bh / 3
    d.rectangle([0, 0, bw, h3], fill="#FF9933")
    d.rectangle([0, h3, bw, h3*2], fill="#FFFFFF")
    d.rectangle([0, h3*2, bw, bh], fill="#128807")
    cx, cy = bw/2, bh/2
    d.ellipse([cx-8, cy-8, cx+8, cy+8], outline="#000088", width=2)

create_flag_badge(draw_hi).save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_hi.png")

# ID
def draw_id(im):
    d = ImageDraw.Draw(im)
    h2 = bh / 2
    d.rectangle([0, 0, bw, h2], fill="#FF0000")
    d.rectangle([0, h2, bw, bh], fill="#FFFFFF")

create_flag_badge(draw_id).save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_id.png")

# VI
def draw_vi(im):
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, bw, bh], fill="#DA251D")
    cx, cy, r = bw/2, bh/2, 14
    points = []
    for i in range(10):
        radius = r if i % 2 == 0 else r * 0.4
        ang = i * np.pi / 5 - np.pi / 2
        points.append((cx + np.cos(ang) * radius, cy + np.sin(ang) * radius))
    d.polygon(points, fill="#FFFF00")

create_flag_badge(draw_vi).save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\flag_badge_vi.png")

print("Regenerated 6 flags with perfect inner clipping & metallic frame!")
