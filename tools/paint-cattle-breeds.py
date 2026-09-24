"""Paint cattle coats and shaggy meshes on Animania's original pixel grid.

The extra sheet space holds coat islands; existing UVs are not scaled. Reference
notes and intentional adaptations are recorded in PORTING.md.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json
import copy
import shutil

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/animania'
COLORS={'umbra':'403744','wooly':'966038','warm':'985739','pinto':'d8cba7',
        'albino':'eae5db','norwegian_red':'6b5142','cream':'d9b56d','cookie':'56616c'}

def rgb(value):return tuple(bytes.fromhex(value))

def faces(n):
    u,v=n['u'],n['v']
    for box in n['boxes']:
        w,h,d=map(int,box['size'])
        for x,y,fw,fh,face in [(u+d,v,w,d,'top'),(u+d+w,v,w,d,'bottom'),
                              (u,v+d,d,h,'left'),(u+d,v+d,w,h,'front'),
                              (u+d+w,v+d,d,h,'right'),(u+2*d+w,v+d,w,h,'back')]:
            yield x,y,fw,fh,face,box

def coat_node(name,parent,u,v,origin,size,deform=0):
    return {'name':name,'parent':parent,'u':u,'v':v,'mirror':False,
            'pivot':[0,0,0],'offset':[0,0,0],'rotation':[0,0,0],
            'boxes':[{'from':origin,'size':size,'deformation':deform}]}

def add_coat(data,breed,role):
    young=role=='calf'
    data['textureHeight']=128
    for node in list(data['nodes']):
        if node['name'].lower() in ('body','bodyhump','bodyhump2'):
            box=node['boxes'][0]
            # Attach to the original animated bone, preserving sleep and eating poses.
            data['nodes'].append(coat_node('Coat'+node['name'],node['name'],0,64,
                                box['from'],box['size'],.65 if not young else .4))
    head=next(n for n in data['nodes'] if n['name'].lower()=='head')
    x,y,z=head['boxes'][0]['from'];w,h,d=head['boxes'][0]['size']
    # Umbra's fringe covers both eyes; Wooly's longer side covers only one.
    split=3 if young else 5
    left_h=h if breed=='umbra' else h-1
    right_h=h if breed=='umbra' else 2
    data['nodes'].append(coat_node('CoatFringeLeft',head['name'],0,104,
                        [x-.3,y-.4,z-.4],[split,left_h,d+1],.05))
    data['nodes'].append(coat_node('CoatFringeRight',head['name'],30,104,
                        [x+split-.3,y-.4,z-.4],[w-split+1,right_h,d+1],.05))
    return data

def color_at(breed,n,face,x,y,w,h,box,sheared):
    name=n['name'].lower();p=(x+.5)/max(w,1);q=(y+.5)/max(h,1)
    color=COLORS[breed];body='body' in name;leg=name.startswith('leg')
    coat=name.startswith('coat');head=name=='head'
    if breed in ('umbra','wooly'):
        if sheared:color='716268' if breed=='umbra' else 'b28761'
        elif coat:
            # Three bounded tones form vertical locks along the fringe and coat sides.
            tones=('403744','50404e','665049') if breed=='umbra' else ('966038','aa7240','b78148')
            color=tones[1 if x in (1,4,8,11,15,19,24,29,34,39,44,49) else 0]
            if y>=h-2 and (x//3)%2==0:color=tones[2]
        if name.startswith('horn'):color='8e7f96' if breed=='umbra' else 'ddd6bb'
        if name=='snout':color='917e90' if breed=='umbra' else 'c29281'
        if leg and (q>.83 or face=='bottom'):color='3c343d' if breed=='umbra' else '624634'
    elif breed=='warm':
        if body and face=='front':color='a96a44'
        if head and face=='top':color='a46945'
        if name.startswith('horn'):color='dfcbb0'
        if name=='snout':color='ac896d'
        if leg and (q>.83 or face=='bottom'):color='604633'
    elif breed=='pinto':
        if body:
            patch=((.07<p<.40 and .12<q<.55) or (.53<p<.96 and .42<q<.91))
            if patch:color='846a50'
        if head and face in ('left','top') and p<.7:color='846a50'
        if leg and .18<q<.50:color='846a50'
        if name=='snout':color='b3977c'
        if name.startswith('horn'):color='c7bfa8'
    elif breed=='albino':
        if name=='snout':color='d8b3ac'
        if name.startswith('horn'):color='d4c9ab'
        if leg and (q>.83 or face=='bottom'):color='b7a28b'
    elif breed=='norwegian_red':
        if body and ((.1<p<.42 and .25<q<.75) or (p>.63 and q<.45) or (p>.78 and q>.70)):color='ddd9cd'
        if head and face in ('right','top') and p>.5:color='ddd9cd'
        if leg and .52<q<.83:color='ddd9cd'
        if name=='snout':color='a79792'
        if name.startswith('horn'):color='c4bca8'
    elif breed=='cream':
        if body or leg:
            # Small scalloped cream patches, preserving a honey-colored ground.
            pattern=('..##....','...##...','.....##.','......##','##......','.##.....')
            if pattern[y%6][x%8]=='#':color='eee4c6'
        if name=='snout':color='a8c2c8'
        if name.startswith('horn'):color='7d9da9'
        if leg and (q>.83 or face=='bottom'):color='9a754a'
    elif breed=='cookie':
        if body:
            if face in ('top','bottom'):longitudinal=0 if face=='top' else 1
            else:longitudinal=q
            world_z=n['pivot'][2]+box['from'][1]+box['size'][1]*longitudinal
            if -2<=world_z<=3:color='e5ddc7'
        if head and q>.65 and face=='front':color='e5ddc7'
        if name=='snout':color='a4a9aa'
        if leg and (q>.83 or face=='bottom'):color='3d4044'
    if name.startswith(('udder','sac','penis')):color='c3a29a' if breed!='umbra' else '80707e'
    return color

outputs=[]
for breed,base in COLORS.items():
    wool=breed in ('umbra','wooly')
    family='longhorn' if wool or breed=='warm' else 'angus' if breed=='cookie' else ''
    for role in ('cow','bull','calf'):
        key=f'farm/client/model/cow/model{role}{family}'
        data=json.loads((A/f'legacy_models/{key}.json').read_text())
        source_breed='longhorn' if family=='longhorn' else 'angus' if family=='angus' else 'holstein'
        src=Image.open(A/f'textures/entity/cows/{role}_{source_breed}.png').convert('RGBA')
        # Only the actual blink-mask footprint can retain dark eye pixels.
        eye=Image.new('RGBA',src.size)
        for side in ('left','right'):
            mask=Image.open(A/f'textures/entity/cows/{role}_blink_{side}.png').convert('RGBA')
            eye.alpha_composite(mask)
        if wool:
            data=add_coat(copy.deepcopy(data),breed,role)
            dest=A/f'legacy_models/cow/{breed}_{role}.json';dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_text(json.dumps(data,indent=2)+'\n');outputs.append(dest)
            for side in ('left','right'):
                mask=Image.open(A/f'textures/entity/cows/{role}_blink_{side}.png').convert('RGBA')
                extended=Image.new('RGBA',(data['textureWidth'],128));extended.paste(mask,(0,0))
                dest=A/f'textures/entity/cows/{role}_wool_blink_{side}.png';extended.save(dest)
                if dest not in outputs:outputs.append(dest)
        for sheared in ((False,True) if wool else (False,)):
            out=Image.new('RGBA',(data['textureWidth'],data['textureHeight']))
            for y in range(src.height):
                for x in range(src.width):out.putpixel((x,y),(*rgb(base),src.getpixel((x,y))[3]))
            for n in sorted(data['nodes'],key=lambda n: 2 if n['name'].lower()=='head' else 3 if n['name'].lower().startswith(('horn','snout')) else 1):
                for u,v,w,h,face,box in faces(n):
                    for y in range(h):
                        for x in range(w):
                            px,py=u+x,v+y
                            if not (0<=px<out.width and 0<=py<out.height):continue
                            coat=n['name'].startswith('Coat')
                            a=255 if coat else src.getpixel((px,py))[3]
                            if not a:continue
                            color=color_at(breed,n,face,x,y,w,h,box,sheared)
                            delta=5 if face=='top' else -7 if face=='bottom' else -4 if y==h-1 and h>3 else 0
                            painted=tuple(max(0,min(255,c+delta)) for c in rgb(color))
                            if n['name'].lower()=='head' and eye.getpixel((px,py))[3]:
                                r,g,b,_=src.getpixel((px,py))
                                if max(r,g,b)<90:painted=rgb('ba8887') if breed=='albino' else (r,g,b)
                                elif min(r,g,b)>180:painted=(r,g,b)
                            if n['name'].lower()=='snout':
                                r,g,b,_=src.getpixel((px,py))
                                if max(r,g,b)<70:painted=tuple(max(0,c-30) for c in painted)
                            out.putpixel((px,py),(*painted,a))
            dest=A/f'textures/entity/cows/{role}_{breed}{"_sheared" if sheared else ""}.png'
            out.save(dest);outputs.append(dest)

for dest in outputs:
    target=ROOT/'.worktrees/forge-1.20.1'/dest.relative_to(ROOT)
    target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(dest,target)
preview=Image.new('RGB',(1024,800),(57,63,58));draw=ImageDraw.Draw(preview)
for i,breed in enumerate(COLORS):
    im=Image.open(A/f'textures/entity/cows/cow_{breed}.png')
    im=im.resize((im.width*3,im.height*3),Image.Resampling.NEAREST)
    x,y=i%4*256,i//4*400
    draw.text((x+4,y+4),breed,fill='white');preview.paste(im,(x,y+20),im)
preview.save(ROOT/'build/cattle-coats-preview.png')
print(f'Wrote {len(outputs)} cattle assets to both versions.')
