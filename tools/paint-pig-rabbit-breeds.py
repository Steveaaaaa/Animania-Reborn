"""Paint breed markings on the original Animania UV grids without resampling.

Appearance references are listed in PORTING.md. Shared leg UVs retain the old
animation/mud-overlay layout. The new breeds do not replace existing textures.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json
import shutil

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/animania'

def rgb(value): return tuple(bytes.fromhex(value))

def faces(node):
    u,v=node['u'],node['v']
    for box in node['boxes']:
        w,h,d=map(int,box['size'])
        yield u+d,v,w,d,'top'
        yield u+d+w,v,w,d,'bottom'
        yield u,v+d,d,h,'left'
        yield u+d,v+d,w,h,'front'
        yield u+d+w,v+d,d,h,'right'
        yield u+2*d+w,v+d,w,h,'back'

def shade(color,face,dx,dy,w,h):
    # Small hand-placed edge steps, not random noise or filtered gradients.
    delta=5 if face=='top' else -8 if face=='bottom' else 0
    if dy==h-1 and h>3: delta-=5
    if dx==0 and w>3: delta-=3
    return tuple(max(0,min(255,c+delta)) for c in rgb(color))

PIGS={'mottled':'59402f','piebald':'d7cdb0','pink_footed':'353130'}
RABBITS={'desert':'d5c28b','black_and_white':'dedcd1','salt_and_pepper':'8d8271',
         'vested':'dedcd1','bold_striped':'39312d','freckled':'dedcd1',
         'harelequin':'39312d','muddy_foot':'dedcd1'}

def pig_color(breed,name,face,x,y,w,h):
    p,q=(x+.5)/max(1,w),(y+.5)/max(1,h)
    c=PIGS[breed]
    leg=name.startswith('leg'); ear=name.startswith('ear'); tail=name.startswith('tail')
    if breed=='mottled':
        # Broken, lengthwise brown bands, with pale hooves.
        if (x//2+y//4)%4==0: c='765539'
        elif (x+y//3)%7==0: c='432e24'
        if leg and (q>.67 or face=='bottom'): c='a49c90'
        if name=='head' and q<.3 and face not in ('top','bottom'): c='765539'
        if name=='snout': c='493125'
    elif breed=='piebald':
        if name=='body' and ((q>.60 and face not in ('top','bottom')) or face=='bottom'):
            c='796047' if (x+y)%7 else '68513e'
        if name=='head' and face=='top' and .2<p<.7 and .3<q<.8: c='796047'
        if tail: c='584330'
        if leg:
            if .25<q<.60 and p<.55: c='796047'
            if q>.80 or face=='bottom': c='a18a6b'
        if name=='snout': c='b9a184'
    else:
        if name=='body' and face=='left' and .15<p<.55 and q>.58: c='aba49e'
        if leg:
            c='d3b1ae' if q>.58 or face=='bottom' else 'a59d98' if q>.38 else c
        if ear: c='282525'
        if name=='snout': c='c8aaa7' if face!='bottom' else '66888a'
    if name.startswith(('nipple','block')): c='a78a80' if breed=='piebald' else '765657'
    return c

def rabbit_color(breed,name,face,x,y,w,h):
    p,q=(x+.5)/max(1,w),(y+.5)/max(1,h)
    ear=name.startswith('ear'); head=name.startswith('head')
    leg='leg' in name; foot=name.endswith('2') or name.startswith('leg')
    body=name=='lowerbody'; muzzle=name=='headfront'
    c=RABBITS[breed]
    # Body runs along Z: side UV width follows the animal's front-to-rear axis.
    rear=p>.50 if face in ('left','right') else face=='back'
    if breed=='desert':
        if face=='bottom' or muzzle: c='e6d7a9'
        if ear and q<.75: c='bca477'
        if body and face=='top' and (x+2*y)%9<2: c='c4b17e'
    elif breed=='black_and_white':
        if body and ((rear and q<.65) or (not rear and q>.55 and face=='left')): c='343332'
        if head and ((face=='right' and p>.4) or (face=='top' and p<.4)): c='343332'
        if ear and face!='bottom': c='343332' if x!=1 else '74706b'
        if leg and not foot: c='343332'
    elif breed=='salt_and_pepper':
        if body or leg:
            c=('dedbd0','a7a08f','6c675e','c2bcae')[(x//2+3*(y//2))%4]
        elif head and (x+y*2)%7<2: c='b9ad95'
        if muzzle: c='c9c1ad'
        if ear: c='95846b'
    elif breed=='vested':
        if head and not muzzle: c='81858a'
        if ear: c='81858a' if q>.65 else 'dddcd5'
        if body and q>.45 and face!='top': c='b0b2b0'
        if leg: c='b0b2b0'
        if foot and (q>.7 or face=='bottom'): c='5b5f64'
    elif breed=='bold_striped':
        if body and (x//2+y//3)%4==0: c='97704a'
        if head and not muzzle and (x//2+y//2)%3==0: c='97704a'
        if name.startswith('leg') and q<.5: c='97704a'
        if muzzle or (foot and (q>.80 or face=='bottom')): c='d8d1bc'
        if ear and q<.45: c='6a4a34'
    elif breed=='freckled':
        if (body and rear) or name.startswith('backleg') or (head and face=='back'):
            spot=(x*3+y*5)%17
            if spot in (0,1): c='78624e'
            elif spot==8: c='8b8b83'
            elif spot==12: c='383734'
        if name.startswith('leg') and (q>.75 or face=='bottom'): c='898a82'
    elif breed=='harelequin':
        if body:
            c='ded9c9' if q<.38 or face=='top' else 'a17348' if q<.57 else '39312d'
        if name=='neck1': c='ded9c9'
        if muzzle or (foot and (q>.80 or face=='bottom')): c='ded9c9'
        if ear and q<.5: c='865e3e'
    elif breed=='muddy_foot':
        if muzzle or name=='tail': c='664936'
        if ear and q<.65: c='664936'
        if foot and (q>.65 or face=='bottom'): c='664936'
    if name=='nose': c='43322b' if breed=='muddy_foot' else 'b8948b'
    if name.startswith('whisker'): c='a29c8e'
    return c

outputs=[]
for family,breeds in [('pigs',PIGS),('rabbits',RABBITS)]:
    for breed,base in breeds.items():
        for role in (('hog','sow','piglet') if family=='pigs' else ('rabbit',)):
            pig=family=='pigs'
            source=A/f'textures/entity/{family}/{role}_{"large_white" if pig else "cottontail"}.png'
            src=Image.open(source).convert('RGBA')
            out=Image.new('RGBA',src.size)
            for y in range(src.height):
                for x in range(src.width): out.putpixel((x,y),(*rgb(base),src.getpixel((x,y))[3]))
            key=f'farm/client/model/pig/model{role}' if pig else 'extra/client/model/rabbits/modelcottontail'
            data=json.loads((A/f'legacy_models/{key}.json').read_text())
            for node in data['nodes']:
                name=node['name'].lower()
                for u,v,w,h,face in faces(node):
                    for y in range(h):
                        for x in range(w):
                            px,py=u+x,v+y
                            if not (0<=px<src.width and 0<=py<src.height): continue
                            r,g,b,a=src.getpixel((px,py))
                            if not a: continue
                            color=pig_color(breed,name,face,x,y,w,h) if pig else rabbit_color(breed,name,face,x,y,w,h)
                            # Retain the established eye locations and nostril pixels.
                            eye=name in ('head','headbase') and max(r,g,b)<65
                            nostril=pig and name=='snout' and max(r,g,b)<115
                            painted=(r,g,b) if eye or nostril else shade(color,face,x,y,w,h)
                            out.putpixel((px,py),(*painted,a))
            dest=A/f'textures/entity/{family}/{role}_{breed}.png'
            out.save(dest);outputs.append(dest)

for dest in outputs:
    target=ROOT/'.worktrees/forge-1.20.1'/dest.relative_to(ROOT)
    target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(dest,target)

# Review sheet only; shipped resources retain their original pixel dimensions.
preview=Image.new('RGB',(1024,780),(60,64,59));draw=ImageDraw.Draw(preview)
for i,dest in enumerate([p for p in outputs if p.stem.startswith(('sow_','rabbit_'))]):
    im=Image.open(dest)
    used=im.getbbox();im=im.crop((0,0,max(64,used[2]),max(32,used[3])))
    im=im.resize((im.width*3,im.height*3),Image.Resampling.NEAREST)
    x,y=(i%4)*256,(i//4)*260
    draw.text((x+4,y+4),dest.stem,fill='white');preview.paste(im,(x,y+24),im)
preview.save(ROOT/'build/pig-rabbit-coats-preview.png')
print(f'Painted {len(outputs)} textures for both versions.')
