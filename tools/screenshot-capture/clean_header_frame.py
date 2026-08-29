from PIL import Image, ImageDraw

header = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\02_Header_Title_Back_cropped.png")
# Header is 810x240
# Clean up back button area completely (x=0..150, y=0..170)
# Clean up title (x=140..700, y=0..170)
# Clean up bottom line & diamond (y=160..240)
# Leaving only the top and side cyber framing brackets!
h_frame = header.copy()
d = ImageDraw.Draw(h_frame)
d.rectangle([0, 0, 150, 175], fill=(0,0,0,0))
d.rectangle([140, 0, 720, 175], fill=(0,0,0,0))
d.rectangle([0, 160, 810, 240], fill=(0,0,0,0))

h_frame.save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\lang_header_frame.png")
print("Perfect cleaned header frame saved!")
