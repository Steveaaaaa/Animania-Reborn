"""Paint coat variants on the native UV grid without resampling its features.

Usage: python tools/paint-modern-wildlife.py <vanilla-client.jar>
"""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "src/main/resources/assets/animania/textures/entity/modern"
OUTPUT.mkdir(parents=True, exist_ok=True)

FOX_FUR = [(142, 63, 36), (176, 81, 34), (204, 105, 32), (226, 124, 33), (231, 143, 65)]
SILVER_FUR = [(35, 36, 43), (48, 49, 57), (64, 66, 76), (88, 91, 102), (123, 126, 136)]
CROSS_FUR = [(108, 56, 33), (147, 76, 39), (177, 97, 45), (203, 121, 57), (223, 146, 77)]
GOAT_FUR = [(133, 114, 97), (156, 136, 118), (192, 172, 144), (219, 213, 194),
            (234, 231, 222), (240, 239, 236), (250, 250, 250)]
CREAM_FUR = [(118, 92, 66), (145, 115, 80), (183, 150, 107), (211, 184, 141),
             (229, 210, 172), (241, 227, 194), (251, 242, 216)]
SLATE_FUR = [(38, 39, 43), (47, 49, 55), (58, 61, 68), (73, 77, 85),
             (89, 94, 102), (108, 112, 119), (129, 132, 137)]


def native(archive, name):
    return Image.open(BytesIO(archive.read("assets/minecraft/textures/entity/" + name))).convert("RGBA")


def paint(source, palette):
    result = source.copy()
    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = source.getpixel((x, y))
            if a:
                result.putpixel((x, y), (*palette.get((r, g, b), (r, g, b)), 255))
    return result


def fill_cube_uv(image, u, v, width, height, depth, color):
    # These six rectangles are ModelPart.Cube's UV islands. New fur and horn
    # details can use previously empty texels, but never sample outside the atlas.
    faces = [(u + depth, v, width, depth), (u + depth + width, v, width, depth),
             (u, v + depth, depth, height), (u + depth, v + depth, width, height),
             (u + depth + width, v + depth, depth, height),
             (u + 2 * depth + width, v + depth, width, height)]
    for x, y, w, h in faces:
        assert 0 <= x <= x + w <= image.width and 0 <= y <= y + h <= image.height
        for py in range(y, y + h):
            for px in range(x, x + w):
                if image.getpixel((px, py))[3] == 0:
                    image.putpixel((px, py), (*color, 255))


with ZipFile(sys.argv[1]) as archive:
    for coat in ["red", "silver", "cross", "snow"]:
        for asleep in [False, True]:
            suffix = "_sleep" if asleep else ""
            original = native(archive, "fox/" + ("snow_fox" if coat == "snow" else "fox") + suffix + ".png")
            colors = SILVER_FUR if coat == "silver" else CROSS_FUR if coat == "cross" else FOX_FUR
            image = paint(original, dict(zip(FOX_FUR, colors)) if coat != "snow" else {})
            if coat == "silver":
                image = paint(image, {(213, 182, 159): (198, 196, 190), (180, 143, 131): (143, 144, 149),
                                      (249, 244, 244): (238, 237, 231), (231, 217, 211): (215, 214, 207)})
            if coat == "cross":
                # A shoulder band and a lengthwise stripe on the back. Pale
                # muzzle, belly and tail-tip pixels keep their original colors.
                for y in range(21, 32):
                    for x in range(24, 48):
                        if original.getpixel((x, y))[:3] not in FOX_FUR:
                            continue
                        if y in (23, 24, 25) or 43 <= x <= 45:
                            index = FOX_FUR.index(original.getpixel((x, y))[:3])
                            image.putpixel((x, y), (*[(49, 37, 32), (62, 44, 35), (78, 53, 38),
                                                      (93, 63, 43), (110, 76, 52)][index], 255))
            fur = (229, 230, 234) if coat == "snow" else colors[2]
            for u, v, w, h, d in [(8, 1, 2, 2, 1), (15, 1, 2, 2, 1), (9, 1, 1, 1, 1),
                                  (24, 18, 2, 3, 3), (24, 15, 6, 4, 6), (35, 9, 3, 2, 3)]:
                fill_cube_uv(image, u, v, w, h, d, fur)
            image.save(OUTPUT / ("fox_" + coat + suffix + ".png"))
    original = native(archive, "goat/goat.png")
    for coat, colors in [("white", GOAT_FUR), ("cream", CREAM_FUR), ("slate", SLATE_FUR)]:
        image = paint(original, dict(zip(GOAT_FUR, colors)))
        for u, v, w, h, d in [(17, 17, 1, 4, 3), (17, 17, 3, 3, 2), (12, 55, 1, 5, 1), (12, 55, 1, 3, 1)]:
            fill_cube_uv(image, u, v, w, h, d, colors[3] if u == 17 else (64, 62, 62))
        image.save(OUTPUT / ("goat_" + coat + ".png"))
