import fs from 'node:fs';
import path from 'node:path';

const workspace = process.cwd();
const sourceRoot = path.join(workspace, '.upstream-animania', 'src', 'main', 'java', 'com', 'animania', 'addons');
const outputRoot = path.join(workspace, 'src', 'main', 'resources', 'assets', 'animania', 'legacy_models');

function number(expression, variables = {}) {
  let clean = expression.trim();
  for (const [name, value] of Object.entries(variables)) {
    clean = clean.replace(new RegExp(`\\b${name}\\b`, 'g'), `(${value})`);
  }
  clean = clean.replace(/\(\s*(?:float|double|int)\s*\)/g, '');
  clean = clean.replace(/(?:java\.lang\.)?Math\.PI/g, `(${Math.PI})`);
  clean = clean.replace(/([0-9.])[fFdD](?![a-zA-Z0-9_])/g, '$1');
  // One upstream chick pivot contains `-073803F`; its adjacent comment and
  // absolute pivot establish that the exported decimal point was lost.
  clean = clean.replace(/^(-?)0(\d{4,})$/, '$10.$2');
  if (!/^[0-9eE+\-*/().\s]+$/.test(clean)) throw new Error(`unsupported numeric expression: ${expression}`);
  return Function(`"use strict"; return (${clean});`)();
}

function methodBody(source, signature) {
  const start = source.indexOf(signature);
  if (start < 0) return '';
  const open = source.indexOf('{', start);
  let depth = 0;
  for (let i = open; i < source.length; i++) {
    if (source[i] === '{') depth++;
    else if (source[i] === '}' && --depth === 0) return source.slice(open + 1, i);
  }
  return '';
}

function convert(file) {
  const raw = fs.readFileSync(file, 'utf8');
  const source = raw.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
  const firstOverride = source.indexOf('@Override');
  const geometry = firstOverride < 0 ? source : source.slice(0, firstOverride);
  const variables = {};
  for (const match of source.matchAll(/(?:float|double|int)\s+(\w+)\s*=\s*([^;]+);/g)) {
    try { variables[match[1]] = number(match[2], variables); } catch { /* runtime-only value */ }
  }
  const nodes = new Map();
  const constructor = /(?:this\.)?(\w+)\s*=\s*new\s+ModelRenderer(?:Animania|Colored)?\s*\(\s*this\s*,\s*([^,]+),\s*([^\)]+)\s*\)\s*;/g;
  for (const match of geometry.matchAll(constructor)) {
    nodes.set(match[1], {
      name: match[1], u: number(match[2], variables), v: number(match[3], variables), mirror: false,
      pivot: [0, 0, 0], offset: [0, 0, 0], rotation: [0, 0, 0], boxes: [], parent: null
    });
  }
  if (!nodes.size) return null;

  const call = (method, action) => {
    const re = new RegExp(`(?:this\\.)?(\\w+)\\.${method}\\s*\\(([^;]*)\\)\\s*;`, 'g');
    for (const match of geometry.matchAll(re)) {
      const node = nodes.get(match[1]);
      if (node) action(node, match[2].split(',').map(value => number(value, variables)));
    }
  };
  call('addBox', (node, args) => node.boxes.push({
    from: args.slice(0, 3), size: args.slice(3, 6), deformation: args[6] ?? 0
  }));
  call('setRotationPoint', (node, args) => node.pivot = args.slice(0, 3));
  call('setOffset', (node, args) => node.offset = args.slice(0, 3));
  for (const match of geometry.matchAll(/(?:this\.)?(\w+)\.mirror\s*=\s*(true|false)\s*;/g)) {
    const node = nodes.get(match[1]);
    if (node) node.mirror = match[2] === 'true';
  }

  // Expand fixed ModelRenderer arrays (the five original hamster cheek stages).
  for (const array of geometry.matchAll(/(?:this\.)?(\w+)\s*=\s*new\s+ModelRenderer\s*\[\s*(\d+)\s*\]\s*;([\s\S]*?)(?=(?:this\.)?\w+\s*=\s*new\s+ModelRenderer\s*\[|(?:this\.)?\w+\s*=\s*new\s+ModelRenderer\s*\(|$)/g)) {
    const [body, name, lengthText] = [array[3], array[1], array[2]];
    const constructorMatch = body.match(/new\s+ModelRenderer\s*\(\s*this\s*,\s*([^,]+),\s*([^\)]+)\)/);
    const addBoxMatch = body.match(/\.addBox\s*\(([^;]+)\)\s*;/);
    const pivotMatch = body.match(/\.setRotationPoint\s*\(([^;]+)\)\s*;/);
    const indexMatch = body.match(/for\s*\(\s*int\s+(\w+)\s*=/);
    if (!constructorMatch || !addBoxMatch || !pivotMatch || !indexMatch) continue;
    for (let index = 0; index < Number(lengthText); index++) {
      const locals = {...variables, [indexMatch[1]]: index};
      const values = addBoxMatch[1].split(',').map(value => number(value, locals));
      const pivot = pivotMatch[1].split(',').map(value => number(value, locals));
      const nodeName = `${name}${index}`;
      nodes.set(nodeName, {
        name: nodeName,
        u: number(constructorMatch[1], locals), v: number(constructorMatch[2], locals), mirror: false,
        pivot: pivot.slice(0, 3), offset: [0, 0, 0], rotation: [0, 0, 0],
        boxes: [{from: values.slice(0, 3), size: values.slice(3, 6), deformation: values[6] ?? 0}],
        parent: null
      });
    }
  }
  let edges = 0;
  for (const match of geometry.matchAll(/(?:this\.)?(\w+)\.addChild\s*\(\s*(?:this\.)?(\w+)\s*\)\s*;/g)) {
    const parent = nodes.get(match[1]);
    const child = nodes.get(match[2]);
    if (parent && child) { child.parent = parent.name; edges++; }
  }

  // Generated Tabula models use this helper for their immutable bind pose.
  for (const match of geometry.matchAll(/setRotateAngle\s*\(\s*(?:this\.)?(\w+)\s*,\s*([^,]+),\s*([^,]+),\s*([^\)]+)\)/g)) {
    const node = nodes.get(match[1]);
    if (node) node.rotation = [number(match[2], variables), number(match[3], variables), number(match[4], variables)];
  }
  // Animania's custom models put the bind pose in setupAngles().
  const setup = methodBody(source, 'void setupAngles()');
  for (const axis of ['X', 'Y', 'Z']) {
    const re = new RegExp(`(?:this\\.)?(\\w+)\\.rotateAngle${axis}\\s*=\\s*([^;]+);`, 'g');
    for (const match of setup.matchAll(re)) {
      const node = nodes.get(match[1]);
      if (node) node.rotation['XYZ'.indexOf(axis)] = number(match[2], variables);
    }
  }

  // Most farm models establish their bind pose every frame instead of in the
  // constructor. Preserve constant assignments from those methods. Models with
  // setupAngles() already have an authoritative constructor bind pose, so their
  // behavioral/sitting alternatives must not overwrite it here.
  let staticRotations = 0;
  if (!setup) {
    for (const axis of ['X', 'Y', 'Z']) {
      const re = new RegExp(`(?:this\\.)?(\\w+)\\.rotateAngle${axis}\\s*=\\s*([^;]+);`, 'g');
      for (const match of source.matchAll(re)) {
        const node = nodes.get(match[1]);
        if (!node) continue;
        try {
          node.rotation['XYZ'.indexOf(axis)] = number(match[2], variables);
          staticRotations++;
        } catch { /* depends on live entity/animation state */ }
      }
    }
  }

  let textureWidth = 64, textureHeight = 32;
  const textureCall = source.match(/\.setTextureSize\s*\(\s*([^,]+),\s*([^\)]+)\)/);
  const textureFields = source.match(/textureWidth\s*=\s*([^;]+);[\s\S]*?textureHeight\s*=\s*([^;]+);/);
  if (textureCall) [textureWidth, textureHeight] = [number(textureCall[1], variables), number(textureCall[2], variables)];
  else if (textureFields) [textureWidth, textureHeight] = [number(textureFields[1], variables), number(textureFields[2], variables)];

  const renderBody = methodBody(source, 'void render(');
  const rendered = new Set();
  for (const match of renderBody.matchAll(/(?:this\.)?(\w+)\.(?:render|renderWithRotation)\s*\(([^)]*)\)/g)) {
    if (!nodes.has(match[1])) continue;
    rendered.add(match[1]);
    const expression = match[2].trim().replace(/\bscale(?:Factor)?\b/g, '1');
    try { nodes.get(match[1]).renderScale = number(expression, variables); }
    catch { nodes.get(match[1]).renderScale = 1; }
  }
  const parentless = [...nodes.values()].filter(node => node.parent === null);
  const roots = parentless
    .filter(node => rendered.size === 0 || rendered.has(node.name))
    .map(node => node.name);
  if (roots.length === 0) roots.push(...parentless.map(node => node.name));
  const relative = path.relative(sourceRoot, file).replaceAll('\\', '/').replace(/\.java$/, '').toLowerCase();
  const data = {
    source: relative, textureWidth, textureHeight, roots,
    nodes: [...nodes.values()],
    audit: {
      nodeCount: nodes.size,
      boxCount: [...nodes.values()].reduce((n, node) => n + node.boxes.length, 0),
      edgeCount: edges,
      parentlessCount: parentless.length,
      renderedRootCount: roots.length,
      staticRotations
    }
  };
  const target = path.join(outputRoot, `${relative}.json`);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  if (process.argv.includes('--add-missing-nodes') && fs.existsSync(target)) {
    const existing = JSON.parse(fs.readFileSync(target, 'utf8'));
    const names = new Set(existing.nodes.map(node => node.name));
    existing.nodes.push(...data.nodes.filter(node => !names.has(node.name)));
    existing.roots.push(...data.roots.filter(name => !names.has(name)));
    existing.audit = {...existing.audit, nodeCount: existing.nodes.length,
      boxCount: existing.nodes.reduce((sum, node) => sum + node.boxes.length, 0),
      edgeCount: existing.nodes.filter(node => node.parent !== null).length,
      parentlessCount: existing.nodes.filter(node => node.parent === null).length,
      renderedRootCount: existing.roots.length};
    Object.assign(data, existing);
  }
  fs.writeFileSync(target, JSON.stringify(data, null, 2) + '\n');
  return data;
}

const files = [];
function walk(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) walk(full);
    else if (/^Model.*\.java$/.test(entry.name) && entry.name !== 'ModelRendererBall.java') files.push(full);
  }
}
walk(sourceRoot);
let converted = 0, nodes = 0, boxes = 0, edges = 0;
for (const file of files) {
  if (process.argv.includes('--sheep-only') && !file.includes(`${path.sep}sheep${path.sep}`)) continue;
  const data = convert(file);
  if (!data) continue;
  converted++;
  nodes += data.audit.nodeCount;
  boxes += data.audit.boxCount;
  edges += data.audit.edgeCount;
}
console.log(JSON.stringify({ files: files.length, converted, nodes, boxes, edges, outputRoot }));
