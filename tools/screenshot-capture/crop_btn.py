from PIL import Image

im = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\lang_back_button.png")
# Let's find octagon boundaries:
# In im (size 94x97), let's crop (17, 18, 94, 95)
oct_crop = im.crop((18, 19, 93, 94))
oct_crop.save(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\app\src\main\res\drawable-nodpi\lang_back_button.png")
print("Cleaned back button size:", oct_crop.size)
