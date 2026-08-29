from PIL import Image, ImageDraw
import numpy as np

# Load layer images
cestina = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\12_Cestina.png")
svenska = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\13_Svenska.png")
suomi = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\14_Suomi.png")
trad_zh = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\19_Chinese_Traditional.png")

# Crop badges (78x88)
badge_cs = cestina.crop((441, 866, 519, 954))
badge_sv = svenska.crop((53, 1006, 131, 1094))
badge_fi = suomi.crop((441, 1006, 519, 1094))
badge_zh_tw = trad_zh.crop((53, 1411, 131, 1499))

target_dir = r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi"
badge_cs.save(f"{target_dir}\\flag_badge_cs.png")
badge_sv.save(f"{target_dir}\\flag_badge_sv.png")
badge_fi.save(f"{target_dir}\\flag_badge_fi.png")
badge_zh_tw.save(f"{target_dir}\\flag_badge_zh_tw.png")

print("Saved CS, SV, FI, ZH_TW badges from PSD!")

# Now generate Thailand badge:
# Flag of Thailand: 5 horizontal stripes: Red (1/6), White (1/6), Blue (2/6), White (1/6), Red (1/6)
template_metal = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\badge_metal_frame.png")
th_flag = Image.new("RGBA", (78, 88), (0, 0, 0, 0))
draw = ImageDraw.Draw(th_flag)

# Inner flag box is roughly (9, 13, 69, 75)
draw.rectangle([0, 0, 78, 88], fill=(165, 25, 49, 255)) # Red top/bottom
# White stripe
draw.rectangle([0, 15, 78, 73], fill=(244, 245, 248, 255))
# Blue stripe
draw.rectangle([0, 29, 78, 59], fill=(45, 42, 74, 255))
# Bottom White
draw.rectangle([0, 59, 78, 73], fill=(244, 245, 248, 255))
# Bottom Red
draw.rectangle([0, 73, 78, 88], fill=(165, 25, 49, 255))

# Chamfer mask for inner badge
mask = Image.new("L", (78, 88), 0)
mask_draw = ImageDraw.Draw(mask)
mask_polygon = [
    (18, 12), (60, 12),
    (68, 20), (68, 68),
    (60, 76), (18, 76),
    (10, 68), (10, 20)
]
mask_draw.polygon(mask_polygon, fill=255)

composite_th = Image.new("RGBA", (78, 88), (0, 0, 0, 0))
composite_th.paste(th_flag, (0, 0), mask=mask)
composite_th.alpha_composite(template_metal)

composite_th.save(f"{target_dir}\\flag_badge_th.png")
print("Saved TH flag badge!")
