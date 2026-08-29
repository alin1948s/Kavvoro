from pathlib import Path
from psd_tools import PSDImage
from PIL import Image

psd_path = r"C:\Users\Alin\Downloads\Kavvoro_Choose_Language_Layered.psd"
out_dir = Path(r"c:\Users\Alin\Desktop\MoonsolStudios\Kavvoro\tools\screenshot-capture\extracted_psd")
out_dir.mkdir(parents=True, exist_ok=True)

psd = PSDImage.open(psd_path)

for layer in psd:
    img = layer.composite()
    if img:
        # get bounding box of non-zero alpha
        bbox = img.getbbox()
        filename = f"{layer.name}.png"
        img.save(out_dir / filename)
        if bbox:
            cropped = img.crop(bbox)
            cropped.save(out_dir / f"{layer.name}_cropped.png")
            print(f"Layer '{layer.name}': full={img.size}, bbox={bbox}, cropped={cropped.size}")
        else:
            print(f"Layer '{layer.name}': empty or full={img.size}")
