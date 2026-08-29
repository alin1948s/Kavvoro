from pathlib import Path
from psd_tools import PSDImage

psd_path = r"C:\Users\Alin\Downloads\Kavvoro_Choose_Language_Layered.psd"
psd = PSDImage.open(psd_path)
print(f"PSD Size: {psd.width}x{psd.height}")

def print_layers(layer, depth=0):
    indent = "  " * depth
    bbox = f"({layer.left}, {layer.top}, {layer.right}, {layer.bottom}) {layer.width}x{layer.height}" if hasattr(layer, "left") else ""
    print(f"{indent}- [{layer.kind}] \"{layer.name}\" visible={layer.visible} {bbox}")
    if layer.is_group():
        for child in layer:
            print_layers(child, depth + 1)

for layer in psd:
    print_layers(layer)
