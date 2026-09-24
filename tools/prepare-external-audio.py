"""Prepare credited CC0 recordings as mono positional Minecraft sounds.

Usage: python tools/prepare-external-audio.py --ffmpeg PATH --source-dir DIR
Source filenames and download URLs are listed in external-audio.json.
"""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--ffmpeg', default='ffmpeg')
parser.add_argument('--source-dir', type=Path, default=ROOT / '.tmp/external-audio')
args = parser.parse_args()
manifest = json.loads((ROOT / 'tools/external-audio.json').read_text(encoding='utf-8'))
for clip in manifest['clips']:
    source = manifest['sources'][clip['source']]
    input_file = args.source_dir / source['file']
    if hashlib.sha256(input_file.read_bytes()).hexdigest() != source['sha256']:
        raise ValueError('Source checksum mismatch: ' + str(input_file))
    output = ROOT / 'src/main/resources/assets/animania/sounds/external' / (clip['name'] + '.ogg')
    output.parent.mkdir(parents=True, exist_ok=True)
    duration = clip['end'] - clip['start']
    filters = [f'atrim=start={clip["start"]}:end={clip["end"]}', 'asetpts=PTS-STARTPTS',
               f'highpass=f={clip["highpass"]}', 'lowpass=f=10000',
               'afftdn=nf=-40:tn=1:nr=6',
               f'loudnorm=I={clip["lufs"]}:TP=-3:LRA=7',
               'afade=t=in:st=0:d=0.008',
               'areverse', 'afade=t=in:st=0:d=0.06', 'areverse']
    subprocess.run([args.ffmpeg, '-hide_banner', '-loglevel', 'error', '-y',
                    '-i', str(input_file),
                    '-ac', '1', '-af', ','.join(filters), '-ar', '44100',
                    '-c:a', 'libvorbis', '-q:a', '4', '-map_metadata', '-1', str(output)], check=True)
    forge = ROOT / '.worktrees/forge-1.20.1'
    if forge.exists():
        target = forge / output.relative_to(ROOT)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(output, target)
    print(clip['name'], f'{duration:.2f}s', output.stat().st_size, 'bytes')
