"""Repair the long-nosed sheep muzzle and reuse the original ram horn geometry/UVs."""
from pathlib import Path
import copy
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/animania'
p = A / 'legacy_models/sheep/long_nosed.json'
data = json.loads(p.read_text(encoding='utf-8'))
ram = json.loads((A / 'legacy_models/farm/client/model/sheep/modelmerinoram.json').read_text(encoding='utf-8'))
names = {n['name'] for n in data['nodes']}
for name, parent, uv, width, height in [
    ('MuzzleBridge', 'UpperJaw', (32, 39), 4, 3),
    ('LowerMuzzleBridge', 'LowerJaw', (12, 8), 3, 1)
]:
    if name not in names:
        data['nodes'].append(dict(name=name, parent=parent, u=uv[0], v=uv[1], mirror=False,
            pivot=[0, 0, 0], offset=[0, 0, 0], rotation=[0, 0, 0],
            boxes=[dict(**{'from':[-width/2, 0, -0.5]}, size=[width, height, 2], deformation=0)]))
for node in ram['nodes']:
    if 'Horn' not in node['name'] or node['name'] in names:
        continue
    horn = copy.deepcopy(node)
    horn['u'], horn['v'] = 108, 118
    data['nodes'].append(horn)
p.write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
# Copy existing horn pixels without resampling into an unused atlas region.
source = Image.open(A / 'textures/entity/sheep/sheep_merino_white_ram.png').convert('RGBA')
patch = source.crop((80, 15, 98, 19))
for suffix in ('', '_sheared'):
    path = A / ('textures/entity/sheep/sheep_long_nosed'+suffix+'.png')
    image = Image.open(path).convert('RGBA')
    image.paste(patch, (108, 118))
    image.save(path)
