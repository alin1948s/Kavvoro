from psd_tools import PSDImage

psd = PSDImage.open(r"C:\Users\Alin\Downloads\Kavvoro_Choose_Language_Layered.psd")

for layer in psd:
    if layer.name in ["03_English", "04_Romana", "05_Espanol"]:
        print(f"Layer '{layer.name}': has_effects={layer.has_effects()}, kind={layer.kind}")
        if layer.is_group():
            for sub in layer:
                print(f"   Sub: '{sub.name}' kind={sub.kind} bbox={sub.bbox}")
