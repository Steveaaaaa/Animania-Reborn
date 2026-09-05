import fs from 'node:fs';
import path from 'node:path';

const workspace = process.cwd();
const sourceRoot = path.join(workspace, '.upstream-animania', 'src', 'main', 'resources');
const outputRoot = path.join(workspace, 'src', 'main', 'resources', 'assets', 'animania', 'craftstudio_models');
const animationOutputRoot = path.join(workspace, 'src', 'main', 'resources', 'assets', 'animania', 'craftstudio_animations');
const planeHalfThickness = 0.01;
const coplanarLayerStep = 0.01;
const models = [
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_cat_bed_1.csjsmodel', 'catsdogs/blocks/model_cat_bed_1', 64, 32],
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_cat_bed_2.csjsmodel', 'catsdogs/blocks/model_cat_bed_2', 64, 32],
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_cat_tower.csjsmodel', 'catsdogs/blocks/model_cat_tower', 128, 128],
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_dog_house.csjsmodel', 'catsdogs/blocks/model_dog_house', 64, 64],
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_dog_pillow.csjsmodel', 'catsdogs/blocks/model_dog_pillow', 128, 128],
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_litter_box.csjsmodel', 'catsdogs/blocks/model_litter_box', 64, 64],
  ['assets/catsdogs/animania/craftstudio/models/blocks/model_pet_bowl.csjsmodel', 'catsdogs/blocks/model_pet_bowl', 64, 32],
  ['assets/catsdogs/animania/craftstudio/models/entity/model_ragdoll.csjsmodel', 'catsdogs/entity/model_ragdoll', 128, 64],
  ['assets/extra/animania/craftstudio/models/blocks/model_hamster_wheel.csjsmodel', 'extra/blocks/model_hamster_wheel', 64, 32],
  ['assets/extra/animania/craftstudio/models/entity/hamster.csjsmodel', 'extra/entity/hamster', 64, 32],
  ['assets/farm/animania/craftstudio/models/blocks/model_bee_hive.csjsmodel', 'farm/blocks/model_bee_hive', 128, 64],
  ['assets/farm/animania/craftstudio/models/blocks/model_wild_hive.csjsmodel', 'farm/blocks/model_wild_hive', 128, 64],
  ['assets/farm/animania/craftstudio/models/entity/model_cart.csjsmodel', 'farm/entity/model_cart', 128, 128],
  ['assets/farm/animania/craftstudio/models/entity/model_cart_chest.csjsmodel', 'farm/entity/model_cart_chest', 128, 128],
  ['assets/farm/animania/craftstudio/models/entity/model_tiller.csjsmodel', 'farm/entity/model_tiller', 128, 64],
  ['assets/farm/animania/craftstudio/models/entity/model_wagon.csjsmodel', 'farm/entity/model_wagon', 256, 128],
  ['assets/player_anim/craftstudio/models/entity/player.csjsmodel', 'player/entity/player', 64, 64],
  ['assets/player_anim/craftstudio/models/entity/player_sit.csjsmodel', 'player/entity/player_sit', 64, 64]
];

const animations = [
  ['assets/extra/animania/craftstudio/animations/blocks/anim_hamster_wheel.csjsmodelanim', 'extra/blocks/anim_hamster_wheel'],
  ['assets/extra/animania/craftstudio/animations/entity/hamster_run.csjsmodelanim', 'extra/entity/hamster_run'],
  ['assets/farm/animania/craftstudio/animations/blocks/anim_bees.csjsmodelanim', 'farm/blocks/anim_bees'],
  ['assets/farm/animania/craftstudio/animations/blocks/anim_bees_wild.csjsmodelanim', 'farm/blocks/anim_bees_wild'],
  ['assets/farm/animania/craftstudio/animations/entity/anim_cart.csjsmodelanim', 'farm/entity/anim_cart'],
  ['assets/farm/animania/craftstudio/animations/entity/anim_cart_chest.csjsmodelanim', 'farm/entity/anim_cart_chest'],
  ['assets/farm/animania/craftstudio/animations/entity/anim_tiller.csjsmodelanim', 'farm/entity/anim_tiller'],
  ['assets/farm/animania/craftstudio/animations/entity/anim_wagon.csjsmodelanim', 'farm/entity/anim_wagon']
];

function convert(sourceName, outputName, textureWidth, textureHeight) {
  const source = JSON.parse(fs.readFileSync(path.join(sourceRoot, sourceName), 'utf8'));
  const nodes = [];
  const roots = [];

  function textureUvs(u, v, width, height, depth) {
    const h = -height;
    const d = -depth;
    return [
      [u + d + width + d, v + d + h, u + d + width, v + d],
      [u + d, v + d + h, u, v + d],
      [u + d + width, v, u + d + width + width, v + d],
      [u + d, v, u + d + width, v + d],
      [u + d + width + d + width, v + d + h, u + d + width + d, v + d],
      [u + d + width, v + d + h, u + d, v + d]
    ].map(rect => rect.map(Math.trunc));
  }

  function generatedVertices(size) {
    const [width, height, depth] = size;
    // CraftStudio intentionally uses zero-sized cuboids for cloth, webbing and
    // flat decorations. Rendering both faces at the exact same depth causes
    // severe Z-fighting in the modern batched renderer, most visibly on the
    // wagon canopy. Give only the geometry a 0.02-pixel thickness; retain the
    // original zero-sized UV layout below so its texture remains unchanged.
    const x0 = width === 0 ? -planeHalfThickness : -width / 2;
    const y0 = height === 0 ? -planeHalfThickness : -height / 2;
    const z0 = depth === 0 ? -planeHalfThickness : -depth / 2;
    const x1 = width === 0 ? planeHalfThickness : x0 + width;
    const y1 = height === 0 ? planeHalfThickness : y0 + height;
    const z1 = depth === 0 ? planeHalfThickness : z0 + depth;
    return [
      [x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0],
      [x0, y0, z1], [x1, y0, z1], [x1, y1, z1], [x0, y1, z1]
    ];
  }

  function customVertices(block, rawSize) {
    if (!block.vertexCoords) return {vertices: null, stretch: [1, 1, 1]};
    const order = [3, 2, 1, 0, 6, 7, 4, 5];
    const vertices = order.map(index => {
      const vertex = block.vertexCoords[index];
      return [vertex[0], -vertex[1], -vertex[2]];
    });
    const stretch = [
      rawSize[0] === 0 ? 1 : Math.abs(vertices[1][0] - vertices[0][0]) / rawSize[0],
      rawSize[1] === 0 ? 1 : Math.abs(vertices[3][1] - vertices[0][1]) / rawSize[1],
      rawSize[2] === 0 ? 1 : Math.abs(vertices[4][2] - vertices[0][2]) / rawSize[2]
    ];
    for (const vertex of vertices) {
      vertex[0] /= stretch[0];
      vertex[1] /= stretch[1];
      vertex[2] /= stretch[2];
    }
    return {vertices, stretch};
  }

  function visit(block, parentId) {
    // CraftStudio node names are display/animation labels, not identifiers.
    // Several furniture models legitimately contain dozens of nodes named
    // "Block1".  Preserve those names, but use a traversal-stable unique ID
    // for hierarchy links so duplicate labels cannot collapse into cycles.
    const id = String(nodes.length);
    const name = block.name;
    const rawSize = block.size ?? [0, 0, 0];
    const size = [rawSize[0], -rawSize[1], -rawSize[2]];
    const rawPosition = block.position ?? [0, 0, 0];
    const rawOffset = block.offsetFromPivot ?? [0, 0, 0];
    const rawRotation = block.rotation ?? [0, 0, 0];
    const texOffset = block.texOffset ?? [0, 0];
    const custom = customVertices(block, rawSize);
    nodes.push({
      id,
      name,
      pivot: parentId === null
        ? [rawPosition[0], 24 - rawPosition[1], -rawPosition[2]]
        : [rawPosition[0], -rawPosition[1], -rawPosition[2]],
      offset: [rawOffset[0], -rawOffset[1], -rawOffset[2]],
      rotation: [rawRotation[0], -rawRotation[1], -rawRotation[2]],
      stretch: custom.stretch,
      vertices: custom.vertices ?? generatedVertices(size),
      uv: textureUvs(texOffset[0], texOffset[1], ...size),
      parentId
    });
    if (parentId === null) roots.push(id);
    for (const child of block.children ?? []) visit(child, id);
  }

  for (const root of source.tree) visit(root, null);

  // The covered cat bed builds its octagonal cushion from four rotated,
  // overlapping cuboids whose top faces occupy precisely the same plane.
  // That worked with CraftStudio's immediate renderer, but a modern buffered
  // renderer alternates between the surfaces (Z-fighting). Separate only the
  // overlapping faces by hundredths of a model pixel: far below a visible
  // shape change, but comfortably above the depth-buffer ambiguity.
  let coplanarLayerOffsets = 0;
  if (outputName === 'catsdogs/blocks/model_cat_bed_2') {
    const sides = nodes.filter(node => node.name === 'Side');
    for (let index = 1; index < sides.length; index++) {
      // Side pieces are siblings, so each needs an absolute micro-offset.
      sides[index].offset[1] += index * coplanarLayerStep;
      coplanarLayerOffsets++;
    }
    const bottoms = nodes.filter(node => node.name === 'Bottom');
    for (let index = 1; index < bottoms.length; index++) {
      // Bottom pieces form a parent/child chain; equal local offsets accumulate.
      bottoms[index].offset[1] += coplanarLayerStep;
      coplanarLayerOffsets++;
    }
  }
  const data = {
    source: sourceName.replaceAll('\\', '/'),
    textureWidth,
    textureHeight,
    roots,
    nodes,
    audit: {
      nodeCount: nodes.length,
      boxCount: nodes.length,
      edgeCount: nodes.length - roots.length,
      parentlessCount: roots.length,
      renderedRootCount: roots.length,
      staticRotations: nodes.filter(node => node.rotation.some(value => value !== 0)).length,
      customVertexBlocks: nodes.filter(node => node.stretch.some(value => value !== 1)).length
    }
  };
  if (coplanarLayerOffsets > 0) data.audit.coplanarLayerOffsets = coplanarLayerOffsets;
  const target = path.join(outputRoot, outputName + '.json');
  fs.mkdirSync(path.dirname(target), {recursive: true});
  fs.writeFileSync(target, JSON.stringify(data, null, 2) + '\n');
  return data;
}

let nodes = 0;
for (const model of models) nodes += convert(...model).nodes.length;
let animationNodes = 0;
for (const [sourceName, outputName] of animations) {
  const source = JSON.parse(fs.readFileSync(path.join(sourceRoot, sourceName), 'utf8'));
  animationNodes += Object.keys(source.nodeAnimations ?? {}).length;
  const target = path.join(animationOutputRoot, outputName + '.json');
  fs.mkdirSync(path.dirname(target), {recursive: true});
  fs.writeFileSync(target, JSON.stringify(source, null, 2) + '\n');
}
console.log(JSON.stringify({models: models.length, nodes, animations: animations.length,
  animationNodes, outputRoot, animationOutputRoot}));
