from PIL import Image, ImageDraw

header = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\02_Header_Title_Back_cropped.png")
# Header is 810x240
# Let's erase back button (x=10..130, y=10..130) and title (x=140..700, y=40..170) and bottom line (y=180..240)
# So only the top framing brackets and subtle decals remain!
h_frame = header.copy()
d = ImageDraw.Draw(h_frame)
# Erase back button area
d.rectangle([10, 10, 135, 135], fill=(0,0,0,0))
# Erase title area
d.rectangle([140, 40, 700, 170], fill=(0,0,0,0))
# Erase bottom divider line
d.rectangle([0, 175, 810, 240], fill=(0,0,0,0))

h_frame.save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\lang_header_frame.png")
print("Saved lang_header_frame.png:", h_frame.size)
