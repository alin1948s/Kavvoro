from psd_tools import PSDImage
import json

psd = PSDImage.open(r"C:\Users\Alin\Downloads\Kavvoro_Choose_Language_Layered.psd")
print("PSD Canvas Size:", psd.size)
print("PSD Color Mode:", psd.color_mode)
print("PSD Depth:", psd.depth)

def inspect_layer(layer, depth=0):
    indent = "  " * depth
    info = {
        "name": layer.name,
        "kind": layer.kind,
        "visible": layer.visible,
        "opacity": layer.opacity,
        "blend_mode": str(layer.blend_mode),
        "bbox": layer.bbox,
        "size": layer.size
    }
    print(f"{indent}- [{layer.kind}] '{layer.name}' bbox={layer.bbox} visible={layer.visible} blend={layer.blend_mode} opacity={layer.opacity}")
    if layer.is_group():
        for child in layer:
            inspect_layer(child, depth + 1)

for layer in psd:
    inspect_layer(layer)
