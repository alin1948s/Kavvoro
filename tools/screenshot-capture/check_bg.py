from PIL import Image
import numpy as np

bg = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\00_Background.png")
print("BG size:", bg.size, "mode:", bg.mode)

# Let's inspect different vertical sections:
arr = np.array(bg)
print("Top 100px mean RGB:", np.mean(arr[:100, :, :3], axis=(0,1)))
print("Middle 100px mean RGB:", np.mean(arr[900:1000, :, :3], axis=(0,1)))
print("Bottom 100px mean RGB:", np.mean(arr[1770:, :, :3], axis=(0,1)))

# Let's check max/min values:
print("Min RGB:", np.min(arr[:, :, :3]), "Max RGB:", np.max(arr[:, :, :3]))
