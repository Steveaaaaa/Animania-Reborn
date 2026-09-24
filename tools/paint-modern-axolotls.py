"""Maintain native 64x64 UVs and 16x16 bucket icons; argv[1] is a Minecraft client jar."""
from pathlib import Path
from PIL import Image
import io
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
textures = root / "src/main/resources/assets/animania/textures"
colors = {"lucy": (228, 166, 180), "wild": (122, 101, 67), "gold": (233, 186, 69),
          "cyan": (173, 211, 206), "blue": (110, 114, 173)}
with zipfile.ZipFile(sys.argv[1]) as jar:
    for breed, color in colors.items():
        image = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/entity/axolotl/axolotl_" + breed + ".png"))).convert("RGBA")
        # Preserve the face and gill islands. Sparse flank markings stay on the body side UVs.
        for x, y in [(1,22),(4,24),(7,23),(20,22),(23,24),(26,23)]:
            if image.getpixel((x,y))[3]:
                image.putpixel((x,y), (*[round(v * .82) for v in color],255))
        for left, top, width, height in [(32,32,8,6),(40,40,20,5),(32,50,20,10)]:
            for y in range(top, top+height):
                for x in range(left,left+width):
                    assert image.getpixel((x,y))[3] == 0, (breed,x,y)
                    shade = .86 if y == top+height-1 else 1.05 if y == top else 1
                    image.putpixel((x,y), (*[min(255,round(v*shade)) for v in color],255))
        (textures / "entity/modern").mkdir(parents=True, exist_ok=True)
        image.save(textures / "entity/modern" / ("axolotl_" + breed + ".png"))
        icon = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/item/axolotl_bucket.png"))).convert("RGBA")
        for y in range(16):
            for x in range(16):
                r,g,b,a = icon.getpixel((x,y))
                if a and r > g*1.15 and b > g*1.05:
                    shade = max(.45,min(1.15,r/228))
                    icon.putpixel((x,y),(*[min(255,round(v*shade)) for v in color],a))
        icon.save(textures / "item" / (breed + "_axolotl_bucket.png"))
