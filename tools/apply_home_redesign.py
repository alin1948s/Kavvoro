import re
from pathlib import Path

target = Path('app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt')
code = target.read_text(encoding='utf-8')
print('Read file, size:', len(code))
