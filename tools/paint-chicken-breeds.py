"""Adapt documented Earth/vanilla chicken color layouts to Animania UVs.

See PORTING.md for appearance references. Shipped textures are never resampled.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json
import shutil

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/animania'
COATS = {
    'amber': ('b67d21','d2962c','e6b844','f2cf6b'),
    'bronzed': ('244c59','326575','47858a','7992a7'),
    'gold_crested': ('a4a6a2','c2c4be','deded4','eeeadd'),
    'midnight': ('151923','202638','2a3249','38435a'),
    'cold': ('444c64','59647c','707c95','8c96ac'),
    'warm': ('af702e','c59043','dfb55c','edcb7c'),
}

def rgb(s): return tuple(bytes.fromhex(s))

def faces(node):
    u,v=node['u'],node['v']
    for box in node['boxes']:
        w,h,d=map(int,box['size'])
        # ModelPart box UV layout; zero-width feather planes still have side UVs.
        yield (u+d,v,w,d,'top')
        yield (u+d+w,v,w,d,'bottom')
        yield (u,v+d,d,h,'side')
        yield (u+d,v+d,w,h,'front')
        yield (u+d+w,v+d,d,h,'side')
        yield (u+2*d+w,v+d,w,h,'back')

outputs=[]
for breed,colors in COATS.items():
    for role in ('hen','rooster','chick'):
        src=Image.open(ASSETS/f'textures/entity/chickens/{role}_white.png').convert('RGBA')
        out=src.copy()
        # Fill UV margins too: fractional feather edges may sample adjacent texels.
        for y in range(src.height):
            for x in range(src.width):
                r,g,b,a=src.getpixel((x,y))
                out.putpixel((x,y), (r,g,b,a) if max(r,g,b)<45 else (*rgb(colors[2]),a))
        data=json.loads((ASSETS/f'legacy_models/farm/client/model/chicken/model{role}.json').read_text())
        # Paint feather surfaces first, then exposed skin and face markings.
        def priority(n):
            name=n['name'].lower()
            return 3 if name=='head' else 4 if name.startswith(('beak','crest','foot','leg')) else 1
        for node in sorted(data['nodes'],key=priority):
            name=node['name'].lower()
            for x0,y0,w,h,face in faces(node):
                for dy in range(h):
                    for dx in range(w):
                        x,y=x0+dx,y0+dy
                        if not (0<=x<src.width and 0<=y<src.height): continue
                        original=src.getpixel((x,y));alpha=original[3]
                        if not alpha: continue
                        # Keep the original eye locations, not unrelated black feather pixels.
                        if name=='head' and max(original[:3])<45: continue
                        color=colors[3] if face=='top' else colors[1] if face=='bottom' else colors[2]
                        if h>=3 and dy==h-1: color=colors[1]
                        if name.startswith('wing') and dy%3==2: color=colors[1]
                        if breed=='bronzed':
                            if name=='head': color='b47743' if face!='bottom' else '935b35'
                            elif name.startswith(('tail','feather','wing')): color='7b88a6' if face!='bottom' else '546783'
                            elif name.startswith('body'): color='315c69' if face=='bottom' else '617e99' if face=='back' else '47858a'
                        elif breed=='gold_crested':
                            if name=='head' or name.startswith(('wing','neck')):
                                color='e0b44b' if dy<h-1 else 'c29439'
                        elif breed=='warm' and name.startswith('wing'):
                            color='b88038' if dy%3==2 else 'e3b65b'
                        elif breed=='amber' and name.startswith('wing'):
                            color='cf942a' if dy%3==2 else 'e6b844'
                        if name.startswith(('foot','leg')):
                            color={'amber':'dba66a','bronzed':'b9c5ca','gold_crested':'cda05a',
                                   'midnight':'343a4b','cold':'919698','warm':'d2a25a'}[breed]
                        if name.startswith('beak'):
                            color={'amber':'dfab6e','bronzed':'c6a369','gold_crested':'d9ae65',
                                   'midnight':'858a93' if dy==0 else '333948','cold':'b6ac89','warm':'d5a15e'}[breed]
                        if name.startswith('crest'):
                            color='30334c' if breed=='midnight' else 'd4a23b' if breed=='gold_crested' else 'a74739'
                        out.putpixel((x,y),(*rgb(color),alpha))
        if breed=='cold' and role!='chick':
            draw=ImageDraw.Draw(out)
            draw.rectangle((54,20,63,23),fill=(*rgb('707c95'),255))
            draw.line((54,23,63,23),fill=(*rgb('59647c'),255))
        dest=ASSETS/f'textures/entity/chickens/{role}_{breed}.png'
        out.save(dest);outputs.append(dest)
        if breed in ('cold','gold_crested'):
            if role!='chick' and breed=='cold':
                # A three-lobed blue-gray crest follows the head through every animation.
                for i,(x,y) in enumerate(((-1.5,-3.5),(-0.5,-4),(0.5,-3.5))):
                    data['nodes'].append({'name':f'BreedCrown{i}','u':54,'v':20,'mirror':False,
                        'pivot':[0,0,0],'offset':[0,0,0],'rotation':[0,0,0],
                        'boxes':[{'from':[x,y,-1],'size':[1,2,2],'deformation':0}],
                        'parent':'Head'})
            dest=ASSETS/f'legacy_models/chicken/{breed}_{role}.json'
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_text(json.dumps(data,indent=2)+'\n');outputs.append(dest)

# An opaque, five-color 16x16 egg with a stepped outline and an upper-left highlight.
egg = Image.new('RGBA',(16,16))
draw = ImageDraw.Draw(egg)
rows = {2:(7,8),3:(6,9),4:(5,10),5:(5,10),6:(4,11),7:(4,11),8:(3,12),9:(3,12),10:(3,12),11:(4,11),12:(5,10),13:(6,9)}
for y,(left,right) in rows.items():
    for x in range(left,right+1):
        edge = x in (left,right) or y in (2,13)
        color = '547d87' if edge else '8fbbc4' if x>8 or y>10 else 'b6d9dc'
        draw.point((x,y),fill=(*rgb(color),255))
draw.line((6,5,6,8),fill=(*rgb('e1f1e9'),255))
draw.point((7,4),fill=(*rgb('e1f1e9'),255))
dest=ASSETS/'textures/item/blue_egg.png';egg.save(dest);outputs.append(dest)

# Preserve egg geometry but give chicken blue eggs their own pale-blue shell texture.
base=Image.open(ASSETS/'textures/entity/tileentities/block_nest_white.png').convert('RGBA')
for y in range(base.height):
    for x in range(base.width):
        r,g,b,a=base.getpixel((x,y))
        value=(r+g+b)//3
        color=rgb('d8ece6' if value>225 else 'b6d9dc' if value>180 else '8fbbc4' if value>115 else '547d87')
        base.putpixel((x,y),(*color,a))
dest=ASSETS/'textures/entity/tileentities/block_nest_chicken_blue.png';base.save(dest);outputs.append(dest)
for dest in outputs:
    target=ROOT/'.worktrees/forge-1.20.1'/dest.relative_to(ROOT)
    if (ROOT/'.worktrees/forge-1.20.1/src/main/resources').is_dir():
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(dest,target)
preview=Image.new('RGB',(6*256,3*150),(55,61,59))
draw=ImageDraw.Draw(preview)
for column,breed in enumerate(COATS):
    for row,role in enumerate(('hen','rooster','chick')):
        im=Image.open(ASSETS/f'textures/entity/chickens/{role}_{breed}.png')
        im=im.resize((im.width*4,im.height*4),Image.Resampling.NEAREST)
        preview.paste(im,(column*256,row*150+20),im)
        draw.text((column*256+3,row*150+3),breed+' '+role,fill='white')
preview.save(ROOT/'build/chicken-coats-preview.png')
print(f'Wrote {len(outputs)} breed assets to both versions')
