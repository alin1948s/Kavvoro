from psd_tools import PSDImage
from PIL import Image

psd = PSDImage.open(r"C:\Users\Alin\Downloads\Kavvoro_Choose_Language_Layered.psd")

for layer in psd:
    if any(k in layer.name.lower() for k in ["cestina", "svenska", "suomi", "traditional", "chinese"]):
        print(f"Layer: '{layer.name}' bbox={layer.bbox}")
