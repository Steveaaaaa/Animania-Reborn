"""Adapt vanilla cat coats to Animania meshes on an unscaled pixel grid.

Colors and markings were checked against Java 1.20.1's bundled cat textures.
Shared legacy UV islands are separated to allow asymmetric coat markings.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json
import copy
import shutil

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/animania'
BASES={'all_black':'tabby','tuxedo':'tabby','red_tabby':'tabby',
       'british_shorthair':'americanshorthair','calico':'tabby','persian':'exotic',
       'white':'tabby','jellie':'tabby'}
COATS={'all_black':'161524','tuxedo':'1c1827','red_tabby':'eaa939',
       'british_shorthair':'a6a7a6','calico':'e7e9ea','persian':'ffe0b2',
       'white':'fdf9fb','jellie':'f0f0f0'}
EYES={'all_black':('e1b945','e1b945'),'tuxedo':('5a9d12','5a9d12'),
      'red_tabby':('4d890d','4d890d'),'british_shorthair':('efd496','efd496'),
      'calico':('efd496','71e1f8'),'persian':('60b3f8','60b3f8'),
      'white':('e8e8a6','a6e5e8'),'jellie':('aec099','aec099')}

def rgb(c):return tuple(bytes.fromhex(c))

def faces(n):
    u,v=n['u'],n['v'];w,h,d=map(int,n['boxes'][0]['size'])
    yield u+d,v,w,d,'top'
    yield u+d+w,v,w,d,'bottom'
    yield u,v+d,d,h,'right'
    yield u+d,v+d,w,h,'front'
    yield u+d+w,v+d,d,h,'left'
    yield u+2*d+w,v+d,w,h,'back'

def color(b,n,face,x,y,w,h):
    name=n['name'];p=(x+.5)/w;q=(y+.5)/h;c=COATS[b]
    body=name in ('body','lower_body');leg='leg' in name;tail=name.startswith('tail')
    head=name.startswith(('head','cheek'));ear=name.startswith('ear')
    if b=='tuxedo':
        if name=='jaw' or name=='neck1' and face in ('front','bottom'):c='eaeaea'
        if body and (face=='bottom' or face in ('front','back') and q>.60):c='eaeaea'
        if leg and q>.72:c='eaeaea'
        if name=='head_front' and q>.5:c='eaeaea'
    elif b=='red_tabby':
        if body or tail or leg:
            if y%5 in (1,2) and face!='bottom':c='db7920'
        if name in ('head_front','jaw') or name=='neck1' and face=='front':c='ffe7bd'
        if body and face=='bottom':c='ffe7bd'
        if head and face=='top' and x%3==0:c='db7920'
        if leg and q>.85:c='ffe7bd'
    elif b=='british_shorthair':
        if face=='bottom' or leg and q>.72:c='bcc1bb'
        if head and face=='front':c='bcbcbc'
        if body and face in ('left','right') and y%5==0:c='999d9b'
    elif b=='calico':
        if body:
            patch=(.08<p<.47 and .12<q<.62) or (.56<p<.95 and q>.40)
            if patch:c='db9c3e' if face in ('left','front','top') else '4f4940'
        if name=='head_base':
            if face=='right' or face=='front' and p<.35:c='423b32'
            elif face=='left' or face=='front' and p>.62:c='db9c3e'
        if name.startswith('cheek_r') or name.startswith('ear_r'):c='423b32'
        if name.startswith('cheek_l') or name.startswith('ear_l'):c='db9c3e'
        if tail:c='db9c3e' if name=='tail' else '4f4940'
        if leg and '_r' in name and q<.38:c='db9c3e'
    elif b=='persian':
        if face=='top' or head:c='ffeacb'
        if body or leg or tail:
            if y%5 in (1,2) and x%4<3:c='f3ca8d'
    elif b=='white':
        if face=='bottom' or leg and q>.8:c='e9ebec'
    elif b=='jellie':
        if body and (face=='top' or face in ('left','right') and q<.63):c='777570' if name=='body' else '525251'
        if name=='head_base' and (face=='top' or face=='front' and (p<.32 or p>.68) and q<.60):c='4b4a4a'
        if ear:c='4b4a4a'
        if tail:c='777570' if name!='tail3' else '4b4a4a'
        if leg and q<.3 and '_r' in name:c='96948e'
    if ear and face=='bottom':c='b39b9e' if b not in ('all_black','tuxedo') else '665465'
    if name=='nose':c='bf7aa5' if b in ('white','jellie') else '92969a' if b=='british_shorthair' else 'ec8b7a' if b=='persian' else '926983' if b=='all_black' else '302932' if b=='tuxedo' else 'd8968d'
    return rgb(c)

masks={s:Image.open(A/f'textures/entity/cats/blink_1_{s}.png').convert('RGBA') for s in ('left','right')}
outputs=[]
for breed,base in BASES.items():
    original=json.loads((A/f'legacy_models/catsdogs/client/models/cats/modelcat{base}.json').read_text())
    data=copy.deepcopy(original);data['textureWidth']=128;data['textureHeight']=128
    out=Image.new('RGBA',(128,128));blink={s:Image.new('RGBA',out.size) for s in masks}
    sx=sy=row=0
    for n,old in zip(data['nodes'],original['nodes']):
        if not n['boxes']:continue
        w,h,d=map(int,n['boxes'][0]['size']);bw=2*(w+d);bh=h+d
        if sx+bw>128:sy+=row;sx=row=0
        n['u'],n['v']=sx,sy;sx+=bw;row=max(row,bh)
        assert sy+bh<=128
        for (u,v,fw,fh,face),(ou,ov,_,_,_) in zip(faces(n),faces(old)):
            for y in range(fh):
                for x in range(fw):
                    c=color(breed,n,face,x,y,fw,fh)
                    delta=3 if face=='top' else -6 if face=='bottom' else -3 if y==fh-1 else 0
                    c=tuple(max(0,min(255,a+delta)) for a in c)
                    out.putpixel((u+x,v+y),(*c,255))
                    if n['name']=='head_base':
                        for side,mask in masks.items():
                            if mask.getpixel((ou+x,ov+y))[3]:
                                blink[side].putpixel((u+x,v+y),(*c,255))
                                eye=rgb(EYES[breed][0 if side=='left' else 1]) if ov+y==20 else (18,25,40)
                                out.putpixel((u+x,v+y),(*eye,255))
    dest=A/f'legacy_models/cats/{breed}.json';dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps(data,indent=2)+'\n');outputs.append(dest)
    dest=A/f'textures/entity/cats/{breed}.png';out.save(dest);outputs.append(dest)
    for side,mask in blink.items():
        dest=A/f'textures/entity/cats/{breed}_blink_{side}.png';mask.save(dest);outputs.append(dest)
for dest in outputs:
    target=ROOT/'.worktrees/forge-1.20.1'/dest.relative_to(ROOT)
    target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(dest,target)
preview=Image.new('RGB',(1024,560),(61,65,60));draw=ImageDraw.Draw(preview)
for i,breed in enumerate(BASES):
    im=Image.open(A/f'textures/entity/cats/{breed}.png').resize((256,256),Image.Resampling.NEAREST)
    x,y=i%4*256,i//4*280;draw.text((x+4,y+4),breed,fill='white');preview.paste(im,(x,y+20),im)
(ROOT/'build').mkdir(exist_ok=True);preview.save(ROOT/'build/cat-coats-preview.png')
print('Wrote eight cat models, coats and matching eyelid masks to both versions.')
