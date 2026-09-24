"""Paint Java 1.21.1-inspired wolf coats on Animania's native UV pixel grid.

Reference: bundled wolf_ashen/black/chestnut/rusty/spotted/striped/snowy/woods
textures. Meshes and animation bones retain the existing Animania wolf design.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import copy
import json
import shutil

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/animania'
# Base fur, dark markings, light underside and nose.
PALETTES = {
    'ashen': ('92949f', '646673', 'dce4e2', '383c45'),
    'black': ('31323b', '22232b', '41424b', '171821'),
    'chestnut': ('c7a796', '816c60', 'eddbca', '453631'),
    'rusty': ('ad683e', '72452f', 'f0dfbc', '372925'),
    'spotted': ('303139', '171820', 'ebd5ac', '171820'),
    'striped': ('c4b07a', '5b5140', 'e8d9a6', '302e29'),
    'snowy': ('e5efef', 'bdcbd0', 'f4f8ef', '4b575e'),
    'woods': ('927346', '594b35', 'e1d0a5', '342c24'),
}

def rgb(value):
    return tuple(bytes.fromhex(value))

def faces(n):
    u, v = n['u'], n['v']
    w, h, d = map(int, n['boxes'][0]['size'])
    return [(u+d,v,w,d,'top'), (u+d+w,v,w,d,'bottom'),
            (u,v+d,d,h,'right'), (u+d,v+d,w,h,'front'),
            (u+d+w,v+d,d,h,'left'), (u+2*d+w,v+d,w,h,'back')]

def coat(breed, name, face, x, y, w, h):
    base, dark, light, nose = PALETTES[breed]
    p, q = (x+.5)/w, (y+.5)/h
    body = name in ('body', 'lower_body')
    head = name == 'head_base'
    legs = 'leg' in name
    muzzle = name in ('head_front', 'jaw', 'upper_jaw_detail') or name.startswith('chops')
    tail = name.startswith('tail')
    ear = name.startswith('ear')
    c = base
    # Stepped tufts break up the saddle and belly without adding sub-pixels.
    fringe = (0, 1, 1, 0, -1, 0, 1)[x % 7] / max(h, 1)
    if body:
        if face == 'bottom' or face in ('left','right') and q > .72 + fringe: c = light
        elif face == 'top' or face in ('left','right') and q < .25 + fringe: c = dark
    if name == 'neck1' and face in ('front','bottom'): c = light
    if muzzle: c = light if face != 'top' else base
    if legs and q > .68: c = light
    if tail and face == 'bottom': c = light
    if name == 'tail4' and breed not in ('snowy','ashen','spotted'): c = dark
    if head:
        if face == 'front' and q > .50 + fringe: c = light
        if face == 'top' or face in ('left','right') and q < .35: c = dark
    if breed == 'snowy':
        c = light if muzzle or face == 'bottom' else base
        if body and face in ('left','right') and q < .2 or head and face=='top': c = dark
    elif breed == 'black':
        c = dark if face == 'bottom' else base
        if muzzle or name == 'neck1' and face == 'front': c = light
    elif breed == 'spotted':
        # Dark saddle, golden legs/cheeks, irregular cream patches and tail tip.
        if legs or muzzle or ear: c = 'c18b4f'
        if body and face in ('left','right','top'):
            if abs(p-.24)*1.2+abs(q-.25)<.22 or abs(p-.73)+abs(q-.7)*.8<.21: c=light
            elif abs(p-.42)+abs(q-.88)<.26 or abs(p-.87)+abs(q-.18)<.25: c='b67a3d'
        if head and face in ('left','right','front') and q > .48: c='c18b4f'
        if name=='tail4' or legs and q>.9: c=light
    elif breed == 'striped':
        if body and face in ('left','right','top'):
            stripe = (x + y//4)%6
            if stripe == 1 or stripe == 2 and q < .65: c=dark
        if tail and y%4 < 2: c=dark
        if head and face in ('left','right') and x%4==1: c=dark
        if legs and q>.77: c=dark
    elif breed == 'rusty':
        if head and face=='front' and .35<p<.65: c=light
        if legs and q>.7: c=base
    elif breed == 'chestnut':
        if ear or head and face=='top': c=dark
        if legs and q>.6 or tail: c=base
    if ear:
        if face=='bottom': c='82736d' if breed not in ('black','spotted') else '534d50'
        elif breed not in ('snowy','spotted'): c=dark
    if 'toe' in name: c=dark if breed in ('black','striped') else light
    if name=='nose': return rgb(nose)
    # Native-sized clusters follow the fur, with quieter highlights on dark coats.
    delta = -5 if face=='bottom' else 3 if face=='top' else 0
    if (x//2 + y//3*3)%7==0: delta-=6
    return tuple(max(0,min(255,v+delta)) for v in rgb(c))

original=json.loads((A/'legacy_models/catsdogs/client/models/dogs/modelwolf.json').read_text())
old_mask=Image.open(A/'textures/entity/dogs/blink_collie.png').convert('RGBA')
old_fur=Image.open(A/'textures/entity/dogs/wolf0.png').convert('RGBA')
outputs=[]
for breed in PALETTES:
    data=copy.deepcopy(original)
    data['textureWidth']=data['textureHeight']=128
    out=Image.new('RGBA',(128,128));mask=Image.new('RGBA',out.size)
    sx=sy=row=0
    for n,old in zip(data['nodes'],original['nodes']):
        if not n['boxes']: continue
        w,h,d=map(int,n['boxes'][0]['size']);bw=2*(w+d);bh=h+d
        if sx+bw>128:sy+=row;sx=row=0
        n['u'],n['v']=sx,sy;sx+=bw;row=max(row,bh)
        assert sy+bh<=128
        for (u,v,fw,fh,face),(ou,ov,_,_,_) in zip(faces(n),faces(old)):
            samples=[old_fur.getpixel((ou+x,ov+y)) for y in range(fh) for x in range(fw)]
            values=[sum(p[:3])/3 for p in samples if p[3]]
            mean=sum(values)/max(1,len(values))
            for y in range(fh):
                for x in range(fw):
                    c=coat(breed,n['name'],face,x,y,fw,fh)
                    # Retain small-scale shading from the original wolf's fur at
                    # exactly the same UV pixel; ignore strong old coat boundaries.
                    source=old_fur.getpixel((ou+x,ov+y))
                    if source[3] and n['name']!='nose':
                        residual=max(-12,min(12,(sum(source[:3])/3-mean)*.22))
                        strength=.45 if breed=='black' else .65 if breed=='snowy' else 1
                        c=tuple(max(0,min(255,round(v+residual*strength))) for v in c)
                    out.putpixel((u+x,v+y),(*c,255))
                    if n['name']=='head_base' and old_mask.getpixel((ou+x,ov+y))[3]:
                        mask.putpixel((u+x,v+y),(*c,255))
                        out.putpixel((u+x,v+y),(21,23,26,255))
    dest=A/f'legacy_models/dogs/wolf_{breed}.json';dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps(data,indent=2)+'\n');outputs.append(dest)
    for suffix,im in (('',out),('_blink',mask)):
        dest=A/f'textures/entity/dogs/wolf_{breed}{suffix}.png';im.save(dest);outputs.append(dest)
for dest in outputs:
    target=ROOT/'.worktrees/forge-1.20.1'/dest.relative_to(ROOT)
    target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(dest,target)
preview=Image.new('RGB',(1024,560),(73,79,70));draw=ImageDraw.Draw(preview)
for i,breed in enumerate(PALETTES):
    im=Image.open(A/f'textures/entity/dogs/wolf_{breed}.png').resize((256,256),Image.Resampling.NEAREST)
    x,y=i%4*256,i//4*280;draw.text((x+4,y+4),breed,fill='white');preview.paste(im,(x,y+20),im)
(ROOT/'build').mkdir(exist_ok=True);preview.save(ROOT/'build/wolf-coats-preview.png')
print('Wrote eight wolf models, coats and eyelid masks to both versions.')
