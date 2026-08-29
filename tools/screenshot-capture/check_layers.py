from psd_tools import PSDImage
from PIL import Image
import numpy as np

psd = PSDImage.open(r"C:\Users\Alin\Downloads\Kavvoro_Choose_Language_Layered.psd")

for i, layer in enumerate(psd):
    img = layer.composite()
    if img is not None:
        bbox = img.getbbox()
        arr = np.array(img)
        alpha = arr[:, :, 3] if arr.shape[2] == 4 else None
        non_zero = np.count_nonzero(alpha) if alpha is not None else arr.size
        print(f"Layer {i:02d}: '{layer.name}' non-empty bbox={bbox} non_zero_alpha={non_zero}")
