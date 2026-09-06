"""Preserve the original timer-dependent sleeping branches, including their conditions."""
from pathlib import Path
import re

root = Path('.upstream-animania/src/main/java/com/animania/addons')
out = ['package com.animania.client;', 'import java.util.Map;',
       'import net.minecraft.client.model.geom.ModelPart;',
       '/** Generated from the LGPL-3.0 1.12 model sleeping branches. */',
       'final class GeneratedLegacySleep {',
       'static boolean apply(String key, Map<String, ModelPart> parts, float sleepTimer) {',
       'switch (key) {']
for p in sorted(root.rglob('Model*.java')):
    s = re.sub(r'/\*[\s\S]*?\*/|//[^\n]*', '', p.read_text(encoding='utf8'))
    m = re.search(r'if\s*\(\s*isSleeping\s*\)', s)
    if not m or 'sleepTimer' not in s:
        continue
    a = s.index('{', m.end()); b = a + 1; depth = 1
    while depth:
        depth += (s[b] == '{') - (s[b] == '}'); b += 1
    body = s[a+1:b-1]
    body = re.sub(r'(?:this\.)?\w+\.render\([^;]*;', '', body)
    body = re.sub(r'float sleepTimer\s*=[^;]*;', '', body)
    body = re.sub(r'(?:this\.)?(\w+)\.rotateAngle([XYZ])',
                  lambda m: 'parts.get("' + m[1] + '").' + m[2].lower() + 'Rot', body)
    key = p.relative_to(root).with_suffix('').as_posix().lower()
    out.append('case "' + key + '" -> {')
    out.extend(line for line in body.splitlines() if line.strip())
    out.append('return true; }')
out += ['default -> { return false; }', '}', '}', '}']
Path('src/main/java/com/animania/client/GeneratedLegacySleep.java').write_text('\n'.join(out)+'\n', encoding='utf8')
