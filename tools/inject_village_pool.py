"""Add the pet stall to a vanilla village `houses` template pool and write the override.

NeoForge 21.1 has no `StructureModifier` API and Forge 1.20.1 has no equivalent for
jigsaw pools either, so the only way to put a modded house element into a vanilla
village is to ship a same-path copy of
`data/minecraft/worldgen/template_pool/village/<biome>/houses.json` inside the mod
jar: a mod's resources are a higher-priority pack layer than vanilla, so our copy
replaces the vanilla file wholesale.

That copy has to be rebuilt from the target MC version's own data, which is what this
script does - it reads the pool straight out of the vanilla client/server jar, inserts
one element, and writes the result into the mod's resources.  Re-run it (and diff the
output) whenever the project moves to a new MC version.

Injected element:

    {"weight": 2,
     "element": {"element_type": "minecraft:legacy_single_pool_element",
                 "location": "<template>",
                 "processors": {"processors": []},
                 "projection": "rigid"}}

`processors: []` rather than `minecraft:mossify_10_percent`: the stall is a maintained
shop, not a weathered ruin.  Weight 2 matches the vanilla houses so the stall is as
likely as any other house; the trailing `empty_pool_element` keeps its own weight and
stays last.

Usage:
    python tools/inject_village_pool.py \
        --jar ~/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar \
        --resources src/main/resources
"""

import argparse
import json
import os
import sys
import zipfile

POOL_PATH = "data/minecraft/worldgen/template_pool/village/{biome}/houses.json"

# biome -> template name, relative to the mod's structure directory.
DEFAULT_BIOMES = {
    "plains": "pet_stall",
    "savanna": "pet_stall_savanna",
    "desert": "pet_stall_desert",
    "taiga": "pet_stall_spruce",
    "snowy": "pet_stall_snowy",
}


def element(template, weight):
    return {
        "element": {
            "element_type": "minecraft:legacy_single_pool_element",
            "location": "animania:" + template,
            "processors": {"processors": []},
            "projection": "rigid",
        },
        "weight": weight,
    }


def inject(pool, template, weight):
    new = element(template, weight)
    for existing in pool["elements"]:
        loc = existing.get("element", {}).get("location")
        if loc == new["element"]["location"]:
            return pool, False  # already patched, keep the file idempotent
    elements = list(pool["elements"])
    tail = elements[-1] if elements else None
    if tail and tail.get("element", {}).get("element_type", "").endswith("empty_pool_element"):
        elements.insert(len(elements) - 1, new)
    else:
        elements.append(new)
    pool = dict(pool)
    pool["elements"] = elements
    return pool, True


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--jar", required=True, help="vanilla client or server jar of the target MC version")
    parser.add_argument("--resources", required=True, help="the mod's src/main/resources directory")
    parser.add_argument("--weight", type=int, default=2)
    parser.add_argument("--dry-run", action="store_true", help="report what would change without writing")
    args = parser.parse_args()

    changed = 0
    with zipfile.ZipFile(args.jar) as jar:
        for biome, template in DEFAULT_BIOMES.items():
            entry = POOL_PATH.format(biome=biome)
            try:
                raw = jar.read(entry)
            except KeyError:
                print(f"MISSING in jar: {entry}", file=sys.stderr)
                sys.exit(1)
            pool = json.loads(raw.decode("utf-8"))
            pool, did_change = inject(pool, template, args.weight)
            target = os.path.join(args.resources, entry)
            verb = "patched" if did_change else "unchanged"
            if did_change:
                changed += 1
                if not args.dry_run:
                    os.makedirs(os.path.dirname(target), exist_ok=True)
                    with open(target, "w", encoding="utf-8", newline="\n") as fh:
                        json.dump(pool, fh, indent=2)
                        fh.write("\n")
            print(f"{biome:8s} {verb:9s} {template:20s} {len(pool['elements'])} elements -> {target}")

    print(f"\n{changed} pool(s) {'would be ' if args.dry_run else ''}written")


if __name__ == "__main__":
    main()
