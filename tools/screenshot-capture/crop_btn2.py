from PIL import Image

header = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\02_Header_Title_Back_cropped.png")
print("Header full size:", header.size)

# The back button is around x=30..150, y=30..150
# Let's save a wide crop to see the exact bounds
wide_crop = header.crop((0, 0, 160, 160))
wide_crop.save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\slices\btn_wide.png")

# Let's find non-zero alpha in wide_crop, but excluding top-left corner bracket
# Let's inspect pixels:
# The octagon has a pink/cyan border.
# Let's crop x=35, y=35, x=135, y=135
oct_crop = header.crop((35, 35, 135, 135))
oct_crop.save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\lang_back_button.png")
print("New back button crop size:", oct_crop.size)
