"""Paint ferret variants on separate, unscaled UV islands.

The original ferret mesh and its animation bones remain intact.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json
import copy
import shutil

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/animania'
original = json.loads((A/'legacy_models/extra/client/model/rodents/modelferret.json').read_text())
source = Image.open(A/'textures/entity/rodents/ferret_grey.png').convert('RGBA')
masks = {s: Image.open(A/f'textures/entity/rodents/ferret_blink_{s}.png').convert('RGBA') for s in ('left','right')}

def rgb(c): return tuple(bytes.fromhex(c))

def faces(n):
    u,v=n['u'],n['v']
    w,h,d=map(int,n['boxes'][0]['size'])
    yield u+d,v,w,d,'top'
    yield u+d+w,v,w,d,'bottom'
    yield u,v+d,d,h,'right'
    yield u+d,v+d,w,h,'front'
    yield u+d+w,v+d,d,h,'left'
    yield u+2*d+w,v+d,w,h,'back'

def coat(breed,n,face,x,y,w,h):
    name=n['name'];p=(x+.5)/w;q=(y+.5)/h
    c='b37749' if breed=='cinnamon' else '6b523b'
    pale='eee3c9' if breed=='cinnamon' else 'd7c8a8'
    if name.startswith(('Body','Neck')):
        if face=='bottom' or (face in ('left','right','front','back') and q>.65):c=pale
        elif face=='top' and .28<p<.72:c='a0683e' if breed=='cinnamon' else '57402e'
    if name.startswith('Paw'):
        c=pale if breed=='cinnamon' else '3d3028'
    if name.startswith('Ear'):
        c=pale if face=='top' else 'b69277'
    if name=='Head':
        if face=='bottom' or face=='front' or (face in ('left','right') and q>.7):c=pale
        if breed=='sable' and face in ('left','right') and .25<q<.7:c='3b2d24'
        if face=='front' and .3<p<.7 and .3<q<.7:c='46332d'
    if name=='Tail':
        distal = q>.68 if face in ('top','bottom') else p>.68 if face in ('left','right') else face=='back'
        if distal:c='302923' if breed=='cinnamon' else '332820'
    return rgb(c)

outputs=[]
for breed in ('cinnamon','sable'):
    data=copy.deepcopy(original);data['textureWidth']=64;data['textureHeight']=64
    out=Image.new('RGBA',(64,64));blink={s:Image.new('RGBA',(64,64)) for s in masks}
    shelf_x=shelf_y=row_h=0
    for n,old in zip(data['nodes'],original['nodes']):
        w,h,d=map(int,n['boxes'][0]['size']);bw=2*(w+d);bh=d+h
        if shelf_x+bw>64:shelf_y+=row_h;shelf_x=row_h=0
        n['u'],n['v']=shelf_x,shelf_y;shelf_x+=bw;row_h=max(row_h,bh)
        assert shelf_y+bh<=64
        for (u,v,fw,fh,face),(ou,ov,_,_,_) in zip(faces(n),faces(old)):
            for y in range(fh):
                for x in range(fw):
                    c=coat(breed,n,face,x,y,fw,fh)
                    shade=5 if face=='top' else -7 if face=='bottom' else -4 if y==fh-1 else 0
                    c=tuple(max(0,min(255,a+shade)) for a in c)
                    if n['name']=='Head':
                        for side,mask in masks.items():
                            m=mask.getpixel((ou+x,ov+y))
                            if m[3]:
                                blink[side].putpixel((u+x,v+y),m)
                                eye=source.getpixel((ou+x,ov+y))
                                if max(eye[:3])<90 or min(eye[:3])>180:c=eye[:3]
                    out.putpixel((u+x,v+y),(*c,255))
    dest=A/f'legacy_models/rodents/ferret_{breed}.json';dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps(data,indent=2)+'\n');outputs.append(dest)
    dest=A/f'textures/entity/rodents/ferret_{breed}.png';out.save(dest);outputs.append(dest)
    for side,mask in blink.items():
        dest=A/f'textures/entity/rodents/ferret_{breed}_blink_{side}.png';mask.save(dest);outputs.append(dest)
for dest in outputs:
    target=ROOT/'.worktrees/forge-1.20.1'/dest.relative_to(ROOT)
    target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(dest,target)
preview=Image.new('RGB',(768,410),(58,63,59));draw=ImageDraw.Draw(preview)
for i,breed in enumerate(('cinnamon','sable')):
    im=Image.open(A/f'textures/entity/rodents/ferret_{breed}.png').resize((384,384),Image.Resampling.NEAREST)
    preview.paste(im,(384*i,24),im);draw.text((384*i+5,5),breed,fill='white')
(ROOT/'build').mkdir(exist_ok=True);preview.save(ROOT/'build/ferret-coats-preview.png')
print('Wrote two ferret models, coats and matching blink masks to both versions.')
