"""Generate the pet-stall village structures as structure-template .nbt files.

The pet seller never appears in generated villages because the port dropped every
village structure integration: `ModVillagers` registers the profession and the
`animania:pet_bowl` point of interest, `PetMerchantTrades` registers the offers,
but no village ever places the bowl, so no villager can ever take the job.  This
script emits the missing piece: a small open-front stall that the vanilla village
`houses` template pools are patched to spawn (see tools/inject_village_pool.py).

Layout is a 7 x 5 x 6 footprint:

    y=0          floor, whole footprint
    y=1..3       stripped-log posts on the four corners
    y=1..3       plank back wall (z=0)
    y=1          plank side rails (x=0, x=6, z=1..4)
    y=1          slab counter at z=3 with a gate at x=3 so the seller can reach
                 its point of interest without jumping the counter
    y=3          coloured-wool awning over the open front (z=5)
    y=4          top-slab roof over the whole footprint
    y=1          animania:pet_bowl on the floor at (3,1,4)

A single `minecraft:jigsaw` block sits at the middle of the open front edge, (3,0,5),
mirroring the entrance jigsaw of every vanilla village house: `name` and `target` are
`minecraft:building_entrance`, `joint` is `aligned`, the pool is that biome's `streets`
pool, and `final_state` restores the floor block.  This is mandatory, not decoration:
`JigsawBlock.canAttach` requires the parent jigsaw's `target` to equal the child
jigsaw's `name` and the two fronts to oppose, so a pool entry whose template has no
`building_entrance` jigsaw is silently never placed - the village simply grows around
the missing house slot.  The jigsaw is also what lets the stall pull one street piece
to its own front afterwards, so the door is not left buried.

The seller villager is emitted as a structure entity marker at (3,1,2), facing the
counter (+Z).  Its NBT carries the profession but deliberately no `Offers` list:
`AbstractVillager#getOffers()` fills an empty offer list by calling
`updateTrades()`, which reads `VillagerTrades.TRADES` - the map NeoForge fills from
`PetMerchantTrades`' `VillagerTradesEvent` handler.  Serialising offers here would
bake stale, level-1-only prices into the template and skip that event entirely.

Usage:
    python tools/gen_pet_stall.py --out src/main/resources/data/animania/structure \
        --data-version 3955
    python tools/gen_pet_stall.py --out ../forge-1.20.1/src/main/resources/data/animania/structures \
        --data-version 3465
"""

import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import nbt as N  # noqa: E402

SIZE = (7, 5, 6)
PET_BOWL = "animania:pet_bowl"

# Entrance jigsaw: centre of the open front edge, ground level, facing out (+Z).
JIGSAW_POS = (3, 0, SIZE[2] - 1)

# One entry per village biome; snowy villages reuse the spruce timber in white.
STYLES = {
    "pet_stall": dict(
        floor="minecraft:oak_planks",
        plank="minecraft:oak_planks",
        post="minecraft:stripped_oak_log",
        slab="minecraft:oak_slab",
        awning="minecraft:red_wool",
        villager_type="minecraft:plains",
        streets_pool="minecraft:village/plains/streets",
    ),
    "pet_stall_savanna": dict(
        floor="minecraft:acacia_planks",
        plank="minecraft:acacia_planks",
        post="minecraft:stripped_acacia_log",
        slab="minecraft:acacia_slab",
        awning="minecraft:orange_wool",
        villager_type="minecraft:savanna",
        streets_pool="minecraft:village/savanna/streets",
    ),
    "pet_stall_desert": dict(
        floor="minecraft:smooth_sandstone",
        plank="minecraft:smooth_sandstone",
        post="minecraft:chiseled_sandstone",
        slab="minecraft:smooth_sandstone_slab",
        awning="minecraft:yellow_wool",
        villager_type="minecraft:desert",
        streets_pool="minecraft:village/desert/streets",
    ),
    "pet_stall_spruce": dict(
        floor="minecraft:spruce_planks",
        plank="minecraft:spruce_planks",
        post="minecraft:stripped_spruce_log",
        slab="minecraft:spruce_slab",
        awning="minecraft:brown_wool",
        villager_type="minecraft:taiga",
        streets_pool="minecraft:village/taiga/streets",
    ),
    "pet_stall_snowy": dict(
        floor="minecraft:spruce_planks",
        plank="minecraft:spruce_planks",
        post="minecraft:stripped_spruce_log",
        slab="minecraft:spruce_slab",
        awning="minecraft:white_wool",
        villager_type="minecraft:snowy",
        streets_pool="minecraft:village/snowy/streets",
    ),
}


def layout(style):
    """Return {(x, y, z): (block name, properties)} for one stall."""
    cells = {}

    def put(x, y, z, name, **props):
        cells[(x, y, z)] = (name, props)

    # Floor.
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            put(x, 0, z, style["floor"])

    # Corner posts.
    for x in (0, SIZE[0] - 1):
        for z in (0, SIZE[2] - 1):
            for y in (1, 2, 3):
                put(x, y, z, style["post"], axis="y")

    # Back wall.
    for x in range(1, SIZE[0] - 1):
        for y in (1, 2, 3):
            put(x, y, 0, style["plank"])

    # Side rails along the open sides.
    for z in range(1, 5):
        put(0, 1, z, style["plank"])
        put(SIZE[0] - 1, 1, z, style["plank"])

    # Counter with a centre gate so the seller can walk to its point of interest.
    for x in (1, 2, 4, 5):
        put(x, 1, 3, style["slab"], type="bottom")

    # Awning over the open front.
    for x in range(1, SIZE[0] - 1):
        put(x, 3, SIZE[2] - 1, style["awning"])

    # Roof.
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            put(x, 4, z, style["slab"], type="top")

    # The job site itself.
    put(3, 1, 4, PET_BOWL)

    # Entrance jigsaw, standing in for the floor block the village connector grabs.
    put(JIGSAW_POS[0], JIGSAW_POS[1], JIGSAW_POS[2], "minecraft:jigsaw", orientation="south_up")
    return cells


def jigsaw_nbt(style):
    """Block-entity payload that lets a street piece attach to the stall."""
    return N.Compound({
        "id": N.String("minecraft:jigsaw"),
        "name": N.String("minecraft:building_entrance"),
        "target": N.String("minecraft:building_entrance"),
        "pool": N.String(style["streets_pool"]),
        "joint": N.String("aligned"),
        "final_state": N.String(style["floor"]),
    })


def seller_nbt(villager_type):
    return N.Compound({
        "id": N.String("minecraft:villager"),
        "VillagerData": N.Compound({
            "level": N.Int(1),
            "profession": N.String("animania:pet_seller"),
            "type": N.String(villager_type),
        }),
        "PersistenceRequired": N.Byte(1),
        "Health": N.Float(20.0),
        "Rotation": N.List([N.Float(0.0), N.Float(0.0)]),
    })


def template(style_name, style, data_version):
    cells = layout(style)

    # Deterministic palette: one entry per distinct (name, properties) pair.
    palette = []
    palette_index = {}
    for name, props in sorted(cells.values(), key=lambda np: (np[0], sorted(np[1].items()))):
        key = (name, tuple(sorted(props.items())))
        if key not in palette_index:
            palette_index[key] = len(palette)
            entry = {"Name": N.String(name)}
            if props:
                entry["Properties"] = N.Compound({k: N.String(v) for k, v in props.items()})
            palette.append(N.Compound(entry))

    blocks = []
    for (x, y, z), (name, props) in sorted(cells.items()):
        index = palette_index[(name, tuple(sorted(props.items())))]
        entry = {
            "pos": N.List([N.Int(x), N.Int(y), N.Int(z)]),
            "state": N.Int(index),
        }
        if name == "minecraft:jigsaw":
            entry["nbt"] = jigsaw_nbt(style)
        blocks.append(N.Compound(entry))

    entity = N.Compound({
        "pos": N.List([N.Double(3.5), N.Double(1.0), N.Double(2.5)]),
        "blockPos": N.List([N.Int(3), N.Int(1), N.Int(2)]),
        "nbt": seller_nbt(style["villager_type"]),
    })

    return N.Compound({
        "DataVersion": N.Int(data_version),
        "size": N.List([N.Int(SIZE[0]), N.Int(SIZE[1]), N.Int(SIZE[2])]),
        "palette": N.List(palette),
        "blocks": N.List(blocks),
        "entities": N.List([entity]),
    }), len(palette), len(blocks)


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", required=True, help="target directory for the .nbt files")
    parser.add_argument("--data-version", type=int, required=True,
                        help="DataVersion of the target MC version (1.21.1=3955, 1.20.1=3465)")
    args = parser.parse_args()

    os.makedirs(args.out, exist_ok=True)
    for name, style in STYLES.items():
        root, palette_size, block_count = template(name, style, args.data_version)
        path = os.path.join(args.out, name + ".nbt")
        N.dump(path, root)
        print(f"{name}: {block_count} blocks, {palette_size} palette entries, "
              f"seller type {style['villager_type']} -> {path}")


if __name__ == "__main__":
    main()
