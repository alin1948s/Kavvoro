from PIL import Image
import numpy as np

ref = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\01_Reference_Flattened.png")
arr_ref = np.array(ref)

# Let's inspect 02_Header_Title_Back
header = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\02_Header_Title_Back.png")
arr_head = np.array(header)

# Let's check 21_Current_Language_Footer
footer = Image.open(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd\21_Current_Language_Footer.png")
arr_foot = np.array(footer)

print("Ref Top 100px mean RGB:", np.mean(arr_ref[:100, :, :3], axis=(0,1)))
print("Ref Max RGB:", np.max(arr_ref[:, :, :3]))

# In the PSD, the top glowing grid and brackets are in 02_Header_Title_Back!
# And the bottom glowing grid and brackets are in 21_Current_Language_Footer!
print("Header non-zero alpha in full 841x1870:", np.count_nonzero(arr_head[:, :, 3]))
print("Footer non-zero alpha in full 841x1870:", np.count_nonzero(arr_foot[:, :, 3]))
