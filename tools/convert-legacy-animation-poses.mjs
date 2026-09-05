import fs from 'node:fs';
import path from 'node:path';

const workspace = process.cwd();
const sourceRoot = path.join(workspace, '.upstream-animania', 'src', 'main', 'java', 'com', 'animania', 'addons');
const outputRoot = path.join(workspace, 'src', 'main', 'resources', 'assets', 'animania', 'legacy_animation_poses');

function numeric(expression) {
  const clean = expression.trim()
    .replace(/\(\s*(?:float|double|int)\s*\)/g, '')
    .replace(/(?:java\.lang\.)?Math\.PI/g, `(${Math.PI})`)
    .replace(/([0-9.])[fFdD](?![a-zA-Z0-9_])/g, '$1');
  if (!/^[0-9eE+\-*/().\s]+$/.test(clean)) throw new Error(`non-constant pose value: ${expression}`);
  return Function(`"use strict"; return (${clean});`)();
}

function bracedBody(source, from) {
  const open = source.indexOf('{', from);
  if (open < 0) return '';
  let depth = 0;
  for (let index = open; index < source.length; index++) {
    if (source[index] === '{') depth++;
    else if (source[index] === '}' && --depth === 0) return source.slice(open + 1, index);
  }
  return '';
}

function walk(directory, output = []) {
  for (const entry of fs.readdirSync(directory, {withFileTypes: true})) {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) walk(full, output);
    else if (/^Model.*\.java$/.test(entry.name)) output.push(full);
  }
  return output;
}

fs.rmSync(outputRoot, {recursive: true, force: true});
let modelCount = 0;
let partCount = 0;
for (const file of walk(sourceRoot)) {
  const raw = fs.readFileSync(file, 'utf8').replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
  const condition = /if\s*\(\s*sitting\b[^)]*\)/g;
  let match;
  let body = '';
  while ((match = condition.exec(raw))) {
    const candidate = bracedBody(raw, match.index);
    if (/rotateAngle[XYZ]|setRotationPoint/.test(candidate)) body += '\n' + candidate;
  }
  if (!body) continue;

  const parts = {};
  const part = name => parts[name] ??= {};
  for (const assignment of body.matchAll(/(?:this\.)?(\w+)\.rotateAngle([XYZ])\s*=\s*([^;]+);/g)) {
    const pose = part(assignment[1]);
    pose.rotation ??= [null, null, null];
    pose.rotation['XYZ'.indexOf(assignment[2])] = numeric(assignment[3]);
  }
  for (const call of body.matchAll(/(?:this\.)?(\w+)\.setRotationPoint\s*\(([^;]+)\)\s*;/g)) {
    part(call[1]).pivot = call[2].split(',').map(numeric);
  }
  if (!Object.keys(parts).length) continue;

  const relative = path.relative(sourceRoot, file).replaceAll('\\', '/').replace(/\.java$/, '').toLowerCase();
  const target = path.join(outputRoot, relative, 'sitting.json');
  fs.mkdirSync(path.dirname(target), {recursive: true});
  fs.writeFileSync(target, JSON.stringify({source: relative, parts}, null, 2) + '\n');
  modelCount++;
  partCount += Object.keys(parts).length;
}

// Farm cattle, goats, sheep and draft horses expressed their sleeping pose
// inline as a function of a renderer-maintained timer. Extract the settled
// endpoint (-0.55) so the modern synced sleeping state retains the original
// per-model leg, head and body arrangement.
for (const file of walk(sourceRoot)) {
  const raw = fs.readFileSync(file, 'utf8').replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
  if (!raw.includes('sleepTimer')) continue;
  const condition = /if\s*\(\s*isSleeping\s*\)/g;
  const match = condition.exec(raw);
  if (!match) continue;
  const body = bracedBody(raw, match.index);
  const parts = {};
  const part = name => parts[name] ??= {};
  for (const assignment of body.matchAll(/(?:this\.)?(\w+)\.rotateAngle([XYZ])\s*=\s*([^;]+);/g)) {
    if (!assignment[3].includes('sleepTimer')) continue;
    const expression = assignment[3].replaceAll('sleepTimer', '(-0.55)');
    const pose = part(assignment[1]);
    pose.rotation ??= [null, null, null];
    pose.rotation['XYZ'.indexOf(assignment[2])] = numeric(expression);
  }
  if (!Object.keys(parts).length) continue;
  const relative = path.relative(sourceRoot, file).replaceAll('\\', '/').replace(/\.java$/, '').toLowerCase();
  const target = path.join(outputRoot, relative, 'sleeping.json');
  fs.mkdirSync(path.dirname(target), {recursive: true});
  fs.writeFileSync(target, JSON.stringify({source: relative, timerEndpoint: -0.55, parts}, null, 2) + '\n');
  modelCount++;
  partCount += Object.keys(parts).length;
}

// Cats and dogs used separate full-model pose classes for sleeping. ModelPose
// copied the transforms from those instances at runtime; extract the same final
// transforms and associate them with every model that imported the pose.
const petModelRoot = path.join(sourceRoot, 'catsdogs', 'client', 'models');
for (const file of walk(petModelRoot)) {
  const modelRaw = fs.readFileSync(file, 'utf8');
  const poseName = modelRaw.match(/Pose([A-Za-z0-9_]*Sleeping)\.INSTANCE/)?.[1];
  if (!poseName) continue;
  const species = file.includes(`${path.sep}cats${path.sep}`) ? 'cats' : 'dogs';
  const poseFile = path.join(petModelRoot, species, 'poses', `Pose${poseName}.java`);
  if (!fs.existsSync(poseFile)) continue;
  const raw = fs.readFileSync(poseFile, 'utf8').replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
  const parts = {};
  const offsets = {};
  const parents = {};
  const part = name => parts[name] ??= {};
  for (const assignment of raw.matchAll(/(?:this\.)?(\w+)\.rotateAngle([XYZ])\s*=\s*([^;]+);/g)) {
    const pose = part(assignment[1]);
    pose.rotation ??= [null, null, null];
    pose.rotation['XYZ'.indexOf(assignment[2])] = numeric(assignment[3]);
  }
  for (const call of raw.matchAll(/(?:this\.)?(\w+)\.setRotationPoint\s*\(([^;]+)\)\s*;/g)) {
    part(call[1]).pivot = call[2].split(',').map(numeric);
  }
  for (const call of raw.matchAll(/(?:this\.)?(\w+)\.setOffset\s*\(([^;]+)\)\s*;/g)) {
    offsets[call[1]] = call[2].split(',').map(numeric);
  }
  for (const call of raw.matchAll(/(?:this\.)?(\w+)\.addChild\s*\(\s*(?:this\.)?(\w+)\s*\)/g)) {
    parents[call[2]] = call[1];
  }
  for (const [name, pose] of Object.entries(parts)) {
    const parentOffset = offsets[parents[name]];
    if (pose.pivot && parentOffset) pose.pivot = pose.pivot.map((value, index) => value + parentOffset[index]);
  }
  const relative = path.relative(sourceRoot, file).replaceAll('\\', '/').replace(/\.java$/, '').toLowerCase();
  const target = path.join(outputRoot, relative, 'sleeping.json');
  fs.mkdirSync(path.dirname(target), {recursive: true});
  fs.writeFileSync(target, JSON.stringify({source: path.basename(poseFile, '.java'), parts}, null, 2) + '\n');
  modelCount++;
  partCount += Object.keys(parts).length;
}

console.log(JSON.stringify({modelCount, partCount, outputRoot}));
