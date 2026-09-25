"""Paint Earth-inspired sheep on the original 128-pixel UV sheet.

No source-game textures are imported. Reference and balancing notes: PORTING.md.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import copy
import json
import shutil

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/animania'
BREEDS = ('flecked', 'fuzzy', 'inky', 'long_nosed', 'patched', 'rocky')
BASE = json.loads((A / 'legacy_models/farm/client/model/sheep/modelmerinoewe.json').read_text())

def rgb(c):
    return tuple(bytes.fromhex(c))

def faces(n):
    u, v = n['u'], n['v']
    for box in n['boxes']:
        w, h, d = map(int, box['size'])
        yield u+d, v, w, d, 'top'
        yield u+d+w, v, w, d, 'bottom'
        yield u, v+d, d, h, 'left'
        yield u+d, v+d, w, h, 'front'
        yield u+d+w, v+d, d, h, 'right'
        yield u+2*d+w, v+d, w, h, 'back'

def color(breed, name, face, x, y, w, h, sheared):
    p, q = (x+.5)/max(w, 1), (y+.5)/max(h, 1)
    wool = 'Wool' in name
    leg = 'Leg' in name
    head = name in ('Head', 'Neck', 'UpperJaw', 'LowerJaw', 'UpperJawDetail1', 'UpperJawDetail2')
    c = 'ded9cb'
    if breed == 'flecked':
        c = 'e4ded0' if head or 'Ear' in name else '78573e'
        if wool and q > (.62 if (x//3)%3 else .45): c = 'd7cdb8'
        if wool and 1 < x%9 < 5 and 1 < y%8 < 4: c = 'ab8760'
    elif breed == 'fuzzy':
        c = 'e4ded0' if wool else '45362c' if head or 'Ear' in name else '332e2a'
        if leg and not wool and (q > .87 or face == 'bottom'): c = 'd6d0bc'
    elif breed == 'inky':
        c = 'a9967a' if head or 'Ear' in name else 'ded5bd'
        if leg or name in ('Hips', 'WoolHips', 'Tail'): c = '302c29'
        if name in ('HeadWool', 'NeckWool', 'NeckWool2', 'WoolBody1', 'WoolBody2'):
            if face in ('left', 'right') or (face in ('top', 'front', 'back') and (p < .28 or p > .72)):
                c = '34302c'
    elif breed == 'long_nosed':
        c = '3c302a' if head or 'Ear' in name else 'c9b78e'
        if name in ('HeadWool', 'NeckWool2') or (leg and q > .60): c = '392e29'
        if leg and not wool and q > .88: c = '272522'
    elif breed == 'patched':
        c = '343331' if head or 'Ear' in name else 'e1ddcf'
        if leg and (.40 < q < .60 or (not wool and (q > .87 or face == 'bottom'))): c = '33312e'
    elif breed == 'rocky':
        c = '686862' if head or 'Ear' in name else 'c5c4b8'
        if wool:
            patch = ((x//3 + (y//3)*2)%7 in (0, 1))
            if patch: c = '62635e'
            elif (x//4+y//2)%5 == 0: c = '93968c'
        if name == 'Nose': c = 'dc9690'
    if name == 'Nose' and breed != 'rocky': c = '272522'
    if leg and not wool and breed in ('flecked', 'inky', 'rocky') and (q > .88 or face == 'bottom'): c = '413b32'
    if name == 'Tongue': c = 'cb8c82'
    if sheared and name in ('Body', 'Hips'): c = 'c4b49e'
    return c

outputs = []
source = Image.open(A / 'textures/entity/sheep/sheep_merino_white_ewe.png').convert('RGBA')
eyes = Image.new('RGBA', source.size)
for side in ('left', 'right'):
    eyes.alpha_composite(Image.open(A / f'textures/entity/sheep/sheep_blink_{side}.png').convert('RGBA'))

for breed in BREEDS:
    data = copy.deepcopy(BASE)
    for n in data['nodes']:
        # The legacy head contains an identical duplicate cube.
        n['boxes'] = list({json.dumps(b, sort_keys=True): b for b in n['boxes']}.values())
        if breed == 'fuzzy' and 'Wool' in n['name']:
            for b in n['boxes']: b['deformation'] = b.get('deformation', 0) + .6
        if breed == 'long_nosed' and n['name'] in ('UpperJaw', 'LowerJaw', 'UpperJawDetail1', 'UpperJawDetail2', 'Nose'):
            n['pivot'][2] -= 1.5
    if breed == 'fuzzy':
        data['nodes'].append({'name':'Tongue', 'parent':'HeadNode', 'u':112, 'v':50,
                             'mirror':False, 'pivot':[-1, .6, -17.4], 'offset':[0,0,0],
                             'rotation':[0,0,0], 'boxes':[{'from':[0,0,0], 'size':[2,1,1], 'deformation':0}]})
    dest = A / f'legacy_models/sheep/{breed}.json'
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(data, indent=2)+'\n'); outputs.append(dest)
    for sheared in (False, True):
        out = Image.new('RGBA', source.size)
        for n in data['nodes']:
            for u, v, w, h, face in faces(n):
                for y in range(h):
                    for x in range(w):
                        px, py = u+x, v+y
                        if not (0 <= px < out.width and 0 <= py < out.height): continue
                        c = rgb(color(breed, n['name'], face, x, y, w, h, sheared))
                        delta = 5 if face == 'top' else -6 if face == 'bottom' else 0
                        if 'Wool' in n['name'] and y%5 == 4 and x%4 in (1,2): delta -= 7
                        c = tuple(max(0, min(255, a+delta)) for a in c)
                        if n['name'] == 'Head' and eyes.getpixel((px,py))[3]:
                            old = source.getpixel((px,py))
                            if max(old[:3]) < 90 or min(old[:3]) > 180: c = old[:3]
                        out.putpixel((px,py), (*c, 255))
        dest = A / f'textures/entity/sheep/sheep_{breed}{"_sheared" if sheared else ""}.png'
        out.save(dest); outputs.append(dest)

for wool, palette in {'flecked':('78573e','ac8b64','d7cdb8'),
                      'inky':('34302c','696157','ded5bd'),
                      'tan':('c9b78e','b3a17c','dbcbab'),
                      'rocky':('96988e','62635e','c5c4b8')}.items():
    out = Image.new('RGB', (16,16))
    for y in range(16):
        for x in range(16):
            index = 1 if (x//3 + 2*(y//3))%7 in (0,1) else 0
            if (x//2+y//3)%6 == 3: index = 2
            c = rgb(palette[index]); delta = -8 if y%4 == 3 and x%4 in (1,2) else 0
            out.putpixel((x,y), tuple(max(0,a+delta) for a in c))
    dest = A / f'textures/block/wool_{wool}.png'; out.save(dest); outputs.append(dest)

# Keep muzzle connections and original horn UVs when regenerating the coats.
import runpy
runpy.run_path(str(ROOT / "tools/repair-sheep-details.py"))

for dest in outputs:
    target = ROOT / '.worktrees/forge-1.20.1' / dest.relative_to(ROOT)
    target.parent.mkdir(parents=True, exist_ok=True); shutil.copy2(dest, target)
preview = Image.new('RGB', (768,560), (57,63,58)); draw = ImageDraw.Draw(preview)
for i, breed in enumerate(BREEDS):
    im = Image.open(A / f'textures/entity/sheep/sheep_{breed}.png')
    im = im.resize((256,256), Image.Resampling.NEAREST)
    x,y = (i%3)*256,(i//3)*280
    draw.text((x+4,y+4), breed, fill='white'); preview.paste(im,(x,y+20),im)
(ROOT/'build').mkdir(exist_ok=True)
preview.save(ROOT/'build/sheep-coats-preview.png')
print(f'Wrote {len(outputs)} sheep assets to both versions.')
