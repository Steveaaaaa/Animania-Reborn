"""Paint bee palettes on the native UV grid; pass a Minecraft client jar as argv[1]."""
import io
import sys
import zipfile
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
out = root / "src/main/resources/assets/animania/textures/entity/modern"
out.mkdir(parents=True, exist_ok=True)
palettes = {"amber": (224, 158, 48), "dark": (128, 99, 59), "pale": (227, 211, 158)}
with zipfile.ZipFile(sys.argv[1]) as jar:
    for breed, color in palettes.items():
        for state in ["", "_nectar", "_angry", "_angry_nectar"]:
            image = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/entity/bee/bee" + state + ".png"))).convert("RGBA")
            for y in range(18):
                for x in range(64):
                    r, g, b, a = image.getpixel((x, y))
                    # Preserve eyes, wings, dark stripes and nectar highlights.
                    if a and r > g and g > b * 1.35 and r > 110 and g > 65:
                        shade = max(0.45, min(1.13, (r + g) / 390))
                        image.putpixel((x, y), (*[min(255, round(v * shade)) for v in color], a))
            # Separate UV islands for the furred thorax and tapered abdomen cap.
            for y in range(32, 44):
                for x in range(40):
                    shade = 0.72 if (x + y * 3) % 11 == 0 else 0.9 if y % 4 == 0 else 1.0
                    base = color if x < 24 else (63, 47, 32)
                    image.putpixel((x, y), (*[round(v * shade) for v in base], 255))
            image.save(out / ("bee_" + breed + state + ".png"))
