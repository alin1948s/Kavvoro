from PIL import Image, ImageDraw

# Load layer images from extracted PSD
cestina = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\12_Cestina_cropped.png")
svenska = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\13_Svenska_cropped.png")
suomi = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\14_Suomi_cropped.png")
trad_zh = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\19_Chinese_Traditional_cropped.png")

# In cropped 370x120 card, flag badge is at (18, 16, 18+78, 16+88) = (18, 16, 96, 104)
badge_cs = cestina.crop((18, 16, 96, 104))
badge_sv = svenska.crop((18, 16, 96, 104))
badge_fi = suomi.crop((18, 16, 96, 104))
badge_zh_tw = trad_zh.crop((18, 16, 96, 104))

target_dir = r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi"
badge_cs.save(f"{target_dir}\\flag_badge_cs.png")
badge_sv.save(f"{target_dir}\\flag_badge_sv.png")
badge_fi.save(f"{target_dir}\\flag_badge_fi.png")
badge_zh_tw.save(f"{target_dir}\\flag_badge_zh_tw.png")

# Now generate Thailand badge using fix_flags technique:
ro = Image.open(f"{target_dir}\\flag_badge_ro.png")
bw, bh = ro.size
mask = Image.new("L", (bw, bh), 0)
d = ImageDraw.Draw(mask)
c = 18
poly = [(c, 8), (bw - c, 8), (bw - 8, c), (bw - 8, bh - c), 
        (bw - c, bh - 8), (c, bh - 8), (8, bh - c), (8, c)]
d.polygon(poly, fill=255)

frame = ro.copy()
f_draw = ImageDraw.Draw(frame)
d_poly = [(c+4, 12), (bw - c - 4, 12), (bw - 12, c + 4), (bw - 12, bh - c - 4), 
          (bw - c - 4, bh - 12), (c + 4, bh - 12), (12, bh - c - 4), (12, c + 4)]
f_draw.polygon(d_poly, fill=(0, 0, 0, 0))

def draw_th(im):
    td = ImageDraw.Draw(im)
    # 5 stripes: Red (1/6), White (1/6), Blue (2/6), White (1/6), Red (1/6)
    h6 = bh / 6.0
    td.rectangle([0, 0, bw, h6], fill="#A51931")
    td.rectangle([0, h6, bw, h6*2], fill="#F4F5F8")
    td.rectangle([0, h6*2, bw, h6*4], fill="#2D2A4A")
    td.rectangle([0, h6*4, bw, h6*5], fill="#F4F5F8")
    td.rectangle([0, h6*5, bw, bh], fill="#A51931")

bg_th = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
flag_content = Image.new("RGBA", (bw, bh), (0, 0, 0, 255))
draw_th(flag_content)
bg_th.paste(flag_content, (0, 0), mask)
bg_th.paste(frame, (0, 0), frame)
bg_th.save(f"{target_dir}\\flag_badge_th.png")

print("All 5 flag badges created successfully!")
