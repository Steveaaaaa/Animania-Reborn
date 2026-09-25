"""Adapt existing pixel art for legacy fox coats and three additional dairy breeds."""
from pathlib import Path
from PIL import Image
import shutil
ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/animania'
outputs=[]
def save(im, rel):
    p=A/rel; p.parent.mkdir(parents=True,exist_ok=True); im.save(p); outputs.append(p)
fox=Image.open(A/'textures/entity/dogs/fox.png').convert('RGBA')
mask=Image.open(A/'textures/entity/dogs/blink_fox.png').convert('RGBA')
for coat in ('red','silver','cross','snow'):
    im=fox.copy()
    for y in range(im.height):
        for x in range(im.width):
            r,g,b,a=fox.getpixel((x,y))
            if a and r>g*1.18 and r>b*1.25:
                if coat=='silver':
                    v=int(32+(r+g+b)/3*.5); c=(v,v+2,v+5)
                elif coat=='snow':
                    v=min(249,int(196+(r+g+b)/3*.3)); c=(v-3,v-1,v)
                elif coat=='cross':
                    c=(int(r*.8),int(g*.78),int(b*.75))
                else:c=(r,g,b)
                im.putpixel((x,y),(*c,a))
    if coat == 'cross':
        import json
        model=json.loads((A/'legacy_models/catsdogs/client/models/dogs/modelfox.json').read_text())
        def fur(px,py,coverage):
            r,g,b,a=fox.getpixel((px,py))
            if not a or not (r>g*1.18 and r>b*1.25):return
            tone=0 if r<100 else 1 if r<155 else 2
            warm=((111,58,35),(153,83,43),(187,111,61))[tone]
            dark=((49,39,35),(65,48,39),(83,61,45))[tone]
            # Three fur tones, with one intermediate band at the marking edge.
            weight=1 if coverage>=.8 else .5 if coverage>0 else 0
            im.putpixel((px,py),(*(round(w*(1-weight)+d*weight) for w,d in zip(warm,dark)),a))
        for node in model['nodes']:
            name=node['name'];u,v=node['u'],node['v']
            w,h,d=map(int,node['boxes'][0]['size'])
            if name in ('body','lower_body','neck1'):
                # Broad dorsal fur: hand-shaped edges vary with body depth.
                edges={'body':[1,1,0,0,0,1,1,2,2],
                       'lower_body':[2,1,1,1,1,0,0,0,1,1,1,1],
                       'neck1':[1,1,1,0,0,0,1,1,1]}[name]
                for z in range(d):
                    edge=edges[z]
                    for x in range(w):
                        margin=min(x,w-1-x)
                        strength=1 if margin>edge else .5 if margin==edge else 0
                        fur(u+d+x,v+z,strength)
                for z in range(d):
                    for y in range(h):
                        # Shoulder fur extends onto the flanks and tapers down;
                        # orange remains dominant on the lower sides.
                        boundary=([1,2,4,5,4,3,1,0,0][z] if name=='body'
                                  else (1 if name=='neck1' else 0))
                        strength=1 if y<boundary-1 else .5 if y==boundary-1 else 0
                        fur(u+(d-1-z),v+d+y,strength)
                        fur(u+d+w+z,v+d+y,strength)
            elif name in ('head_front','upper_jaw_detail','leg_l2','leg_r2',
                          'back_leg_l2','back_leg_r2','tail2'):
                # Dark muzzle and lower limbs; retain pale pixels and the tail tip.
                for y in range(h):
                    for x in range(2*(w+d)):
                        fur(u+x,v+d+y,.5 if name=='tail2' else 1)
    save(im,f'textures/entity/modern/legacy_fox_{coat}.png')
    closed=im.copy()
    for y in range(mask.height):
        for x in range(mask.width):
            if mask.getpixel((x,y))[3]:
                # Eyelids use neighbouring coat pixels, not the original eye colour.
                c=im.getpixel((x,min(y+2,im.height-1)))
                if c[3]==0:c=({'red':(173,93,60),'silver':(85,87,90),'cross':(120,65,40),'snow':(222,225,228)}[coat]+(255,))
                closed.putpixel((x,y),c)
    save(closed,f'textures/entity/modern/legacy_fox_{coat}_sleep.png')
colors={'pinto':((224,201,123),(139,92,54)), 'norwegian_red':((238,184,89),(171,76,45)), 'cream':((246,233,175),(211,178,106))}
for breed,(flesh,rind) in colors.items():
    def cheese(src):
        im=Image.open(A/src).convert('RGBA');assert im.size==(16,16)
        for y in range(16):
            for x in range(16):
                r,g,b,a=im.getpixel((x,y))
                if not a:continue
                v=(r*.3+g*.59+b*.11)/255
                base=rind if v<.47 else flesh
                scale=.65+.35*v
                im.putpixel((x,y),(*(int(c*scale) for c in base),a))
        return im
    for item in ('wheel','wedge'):
        im=cheese(f'textures/item/jersey_cheese_{item}.png')
        save(im,f'textures/item/{breed}_cheese_{item}.png')
    for face in ('top','side','inner','bottom'):
        im=cheese(f'textures/block/holstein_cheese_{face}.png')
        # Sparse holes and rind marks differ without changing the pixel grid.
        points={'pinto':[(4,5),(11,9),(7,12)],'norwegian_red':[(3,4),(10,6),(6,10),(12,12)],'cream':[(5,6),(10,11)]}[breed]
        if face in ('top','inner'):
            for x,y in points:im.putpixel((x,y),(*rind,255))
        save(im,f'textures/block/{breed}_cheese_{face}.png')
    im=Image.open(A/'textures/item/bucket_milk_jersey.png').convert('RGBA');assert im.size==(16,16)
    for y in (10,11,12):
        for x in (6,7,8,9):
            if im.getpixel((x,y))[3]:im.putpixel((x,y),(*rind,255))
    im.putpixel((7,11),(*flesh,255));im.putpixel((8,11),(*flesh,255))
    save(im,f'textures/item/bucket_milk_{breed}.png')
    for flow in ('still','flow'):
        source=A/f'textures/fluid/milk_jersey_{flow}.png';im=Image.open(source).convert('RGBA')
        tint={'pinto':(255,253,244),'norwegian_red':(255,251,237),'cream':(255,248,223)}[breed]
        for y in range(im.height):
            for x in range(im.width):
                r,g,b,a=im.getpixel((x,y));im.putpixel((x,y),(r*tint[0]//255,g*tint[1]//255,b*tint[2]//255,a))
        rel=f'textures/fluid/milk_{breed}_{flow}.png';save(im,rel)
        meta=Path(str(source)+'.mcmeta');target=Path(str(A/rel)+'.mcmeta');shutil.copyfile(meta,target);outputs.append(target)
forge=ROOT/'.worktrees/forge-1.20.1/src/main/resources/assets/animania'
if forge.is_dir():
    for p in outputs:
        target=forge/p.relative_to(A);target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,target)
print(f'Wrote {len(outputs)} fox and dairy assets.')
