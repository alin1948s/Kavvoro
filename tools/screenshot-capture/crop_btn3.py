from PIL import Image

header = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\02_Header_Title_Back_cropped.png")
# Let's crop (20, 20, 136, 136)
oct_crop = header.crop((20, 20, 136, 136))
oct_crop.save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\lang_back_button.png")
print("Perfect Back Button size:", oct_crop.size)
