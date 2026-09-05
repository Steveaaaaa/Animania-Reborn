# NeoForge 1.21.1 port plan

## Constraints discovered upstream

The 1.12 branch contains 729 Java files, 562 JSON files, 526 PNG textures and
151 OGG sounds. It is split into Base, Farm, Extra, and Cats & Dogs. Several
models use the obsolete CraftStudio API and must be exported or rebuilt as
modern baked/LayerDefinition models. The old Forge registries, capabilities,
network packets, metadata, mappings and entity AI APIs all require replacement.

## Compatibility rules

1. Keep the `animania` namespace and reuse old registry paths when semantics are
   unchanged.
2. Never copy 1.12 Java into production without rewriting it against Mojmap and
   NeoForge APIs.
3. Persist per-animal needs with NeoForge data attachments. Use synced entity
   data only for values that affect rendering.
4. Prefer tags and data packs over hard-coded item lists.
5. Keep common code dedicated-server safe; renderers and model layers stay in
   client-only packages.
6. Do not rename released registry names. Migration from legacy worlds is outside this port's scope.

## Milestones

- [x] M0: reproducible Java 21 / NeoForge 1.21.1 build and metadata
- [x] M1: shared husbandry core (hunger, thirst, trough)
- [x] M2: Farm chickens, nests, eggs, sex and growth lifecycle
- [x] M3: cattle and goats, milk, breeds and breeding genetics
- [x] M4: pigs, sheep, horses and farm vehicles
- [x] M5: Extra animals, habitats and hamster utilities
- [x] M6: Cats & Dogs
- [x] M7: config and JEI/EMI/Jade integration
- [x] M8 implementation: exact assets/models, animation conversion, legacy balancing and release build
- [ ] External QA: real-client visual and interaction verification

Each animal milestone should ship only after it has server smoke tests, spawn
rules, attributes, loot tables, breeding/growth tests, translations and client
renderer verification.

## M8 correction checkpoint

- All 151 upstream sound files are present behind 48 modern sound events.
- All 95 ordinary upstream Java `ModelRenderer` assets are now deterministically converted;
  their cube UVs, dimensions, deformation, pivots, offsets, rotations and hierarchy
  are retained. Animal renderers select the original breed/role-specific models.
- All 18 upstream CraftStudio models and all 8 `.csjsmodelanim` files are now
  deterministically converted. The renderer preserves hierarchy, custom vertices,
  per-face UVs and CraftStudio quaternion/keyframe interpolation. The cart, wagon,
  tiller, hamster wheel/hamster and both hives use those converted animations in game.
- The six Cats & Dogs props and pet bowl now use their original render geometry and
  byte-identical textures instead of placeholder block models.
- All 22 cat/dog sitting poses and 60 source sleeping poses (82 pose files, 991
  transforms) are mechanically extracted and applied per breed. Every converted
  animal model responds to locomotion, feeding and sleeping state.
- The custom 16-piece hamster-ball shell has been rebuilt from `ModelRendererBall`.
  Per-root render scales are preserved, including the peacock fan's original 1/3
  scale and the independently scaled piglet/Angora parts.
- All 52 legacy blink masks are present and used by the corresponding animal
  families. Fainting goats restore their sprint-collision timer and fall pose.
- The authoritative upstream inputs are 104 Java ModelRenderer model classes,
  18 CraftStudio `.csjsmodel` files and 8 `.csjsmodelanim` animation files.
- Exact conversion must preserve every UV offset, cube, pivot, local offset,
  rotation, parent-child relationship and animation keyframe.
- All 221 registered animal entity eggs, two random pet eggs and eight category
  random eggs now have item model resources (231/231).
- Static verification currently passes: Java 21 compile of all non-optional
  integration sources plus `tools/verify-port.ps1`, which checks JSON parsing,
  model/pose/animation/sound/egg inventories, localization parity, legacy
  configuration consumers and configured spawn modifiers.
- Legacy husbandry AI no longer falls back to vanilla lifecycle shortcuts:
  children advance through the original 85 care-gated growth steps, mammals use
  200-tick courtship with persistent pairing/fertility/dry periods, and pregnancy
  keeps the original random duration, loss, litter and pre-birth wake rules.
- Sleeping, salt-lick use and egg laying now require successful pathfinding to
  the configured bed or target block. Hamsters/hedgehogs retain daytime sleep,
  rabbits retain their two sleep windows, and hens/peahens only insert an egg
  after reaching a compatible non-occupied nest.
- Species AI restored in this pass includes puppy/kitten chase play, German
  shepherd herding, buck and ram rivalry, paired-stallion following, configurable
  rooster fights, pig mud/snuffling, rodent grazing and nest raids, legacy
  predator/avoidance relationships, the Pepe frog behavior, and the configurable
  tame-animal teleport gate.
- The complete 216-field public configuration surface from the four 1.12 config
  classes is represented by `animania-server.toml`, `animania_farm-server.toml`,
  `animania_extra-server.toml`, and `animania_cats_dogs-server.toml`. The port-only
  tuning controls remain isolated in `animania-modern-server.toml`.

The implementation portion and release build are complete. Real NeoForge client
visual/interaction QA remains a separate external checkbox. Per request, no
GameTests or unit-test suite are part of this checkpoint. Legacy-world migration
is explicitly outside scope.

## Client QA corrections: 2026-09-06

- Wagon pole clearance: composing the unchanged source `Tow` hierarchy places
  the farthest `Tow7` vertex 4.329062 blocks ahead of the vehicle centre, beyond
  the old 3.2-block puller centre. At the user's request, single-horse wagon
  spacing is now pole reach + animal clearance + a 0.35-block gap (about 5.83
  blocks for a draft horse). This intentionally differs from the 1.12 spacing;
  the original model is unchanged. Both hitch placement and continuous following
  use this distance. Horse vehicle detection also covers the increased distance.
- Wagon traces now start at the original `Tow6` crossbar endpoints, using the
  same interpolated yaw and slope pitch as the model, and end at a breast strap
  following the horse's body orientation. The original wagon animation does not
  animate the Tow hierarchy. Cart and tiller spacing is unchanged.
- Resting animals no longer watch passing players, turn their bodies or apply
  awake head/idle animation over their sleeping pose. The 1.12 reference is
  `GenericAIWatchClosest`, `GenericAILookIdle` and the sleeping guards in the
  other generic AI goals. A priority sleep hold now excludes active movement,
  look and target goals, while both logical sides retain the resting orientation
  through body/head interpolation. Existing wake conditions and species sleep
  schedules remain in use.
- Validation: `tools/verify-port.ps1` and Java compilation/full
  `clean build -x test --no-daemon` passed. Real-client confirmation remains pending.
- Shader canopy correction: the source `Web7` is a zero-height cloth panel.
  Its converted thin geometry has reversed winding, as do `FrontWeb` and
  `BackWeb`. The renderer previously negated only the lighting normal, leaving
  the submitted face order inconsistent for shader front-face and tangent
  calculations. It now reverses vertex traversal as well, preserving every
  vertex's UV association, and accounts for reflected pose transforms. No model
  coordinates, textures or animation assets were changed. A static geometry
  audit covered 3,858 nondegenerate faces across 18 models (312 affected faces),
  including reflected transforms. Port verification and the full Java build
  passed; confirmation with the user's shader pack remains pending.
