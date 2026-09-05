# Animania Reborn

An unofficial NeoForge 1.21.1 port of
[Animania](https://github.com/capnkirok/animaniamod), combining Base, Farm,
Cats & Dogs, and Extra content in one mod. The implementation derives gameplay,
models, textures, animations and sounds from the original 1.12 branch.

Port source: https://github.com/Steveaaaaa/Animania-Reborn

## Current milestone

- NeoForge 1.21.1 / Java 21 project skeleton
- stable `animania` namespace and modular registration layout
- persistent hunger and thirst data attachments for vanilla animals
- an oak trough that can hold four portions of feed or water
- all 15 legacy Farm chicken entity IDs (five breeds × chick/hen/rooster)
- sex-aware breeding, chick growth, egg laying and rooster dawn behavior
- the legacy `block_nest` and `brown_egg`, including three-egg nest storage and hatching
- natural hen family spawning, spawn eggs, loot, breed textures and client renderers
- all 24 legacy cattle and 21 legacy goat entity IDs with their original breed textures
- delayed pregnancy with parent-breed inheritance, calf/kid growth and family spawning
- post-birth milking gated by hunger/thirst, plus Angora shearing and wool regrowth
- real Holstein, Friesian, Jersey and goat milk fluids with placeable buckets
- a persistent cheese mold that ages milk for 24,000 ticks into edible cheese wheels
- all four breed-specific cheese wheels and wedges, using the legacy registry paths and textures
- all 18 legacy pig entity IDs (six breeds × piglet/sow/hog), with genetics, lifecycle and prime pork
- legacy `block_mud`, persistent washable mud coats, mud-seeking AI and leashed forest truffle rooting
- all 18 legacy sheep entity IDs, breed/color genetics, pregnancy, shearing/regrowth and prime mutton
- sheep milk fluid, legacy `sheep_bucket_milk`, sheep cheese wheel/wedge and mold integration
- daylight ram rivalry with charge, impact damage, knockback and recovery cooldown
- all three draft-horse entity IDs, six coat variants, pregnancy, foal growth, taming and riding
- raw/cooked horse meat, adult horse loot and natural draft-horse family spawning
- legacy `cart`, `wagon` and `tiller` entities plus their `item_*` placement items and wheel recipe
- original cart/wagon/tiller storage capacities (36/54/9 slots), optional cart chest installation,
  mounted/leashed horse hitching, cart pig hitching and tiller cow hitching
- three-block-wide tilling with automatic wheat, beetroot, carrot and potato planting from tiller storage
- Extra's `frog`, `dartfrog` and `toad`, with all six legacy skin textures and swamp spawning
- dart-frog contact poison and renewable poisoned-arrow harvesting, plus frog-leg drops and cooking
- Extra hamsters, normal/albino hedgehogs and grey/white ferrets with all legacy textures
- food taming, owner follow/sit/carry interactions, hamster color persistence and ferret silverfish hunting
- all 24 Extra rabbit IDs (eight breeds × kit/doe/buck), pregnancy, litter birth, growth and genetics
- seven lop-rabbit coats, the `Killer` naming behavior, breed habitats and prime rabbit meat
- all 21 Extra peafowl IDs, breed-aware nesting/hatching, chick growth and the original
  independent male/female/young natural spawn entries
- seven feather colors, male display fans, feather shedding, regular/prime peacock meat and luck effects
- legacy hamster food plus clear and all 16 dyed hamster balls with synced translucent entity rendering
- a persistent hamster wheel that accepts carried tame hamsters, consumes food and exports 20 FE/tick
- wheel energy/food persistence, six-sided NeoForge energy capability, comparator output and safe hamster ejection
- all 24 Cats & Dogs cat IDs (eight breeds × kitten/queen/tom) with legacy textures
- fish taming, owner commands/defense, untamed hunting, pregnancy, litters, growth and breed inheritance
- natural ocelot-cat families in jungle habitats plus complete cat spawn eggs and localization
- all 45 Cats & Dogs canine IDs (15 breeds × puppy/female/male) and all 30 legacy coat textures
- beef taming, owner defense, untamed hunting, pregnancy, litters, growth and coat/breed inheritance
- wild fox and wolf families, Razz/Gloria name skins, and German Shepherd livestock herding
- the legacy pet bowl with three food/water portions, automatic pet feeding, persistence and comparator output
- both cat beds, cat tower, dog house, dog pillow and litter box with recipes, facing and legacy collision sizes
- nighttime bed-seeking behavior for tamed cats and dogs
- random cat and dog spawn eggs that preserve the legacy all-breed behavior
- a pet merchant villager profession using the pet bowl workstation and the legacy breed/price trade table
- all 216 public options from the 1.12 Base, Farm, Extra, and Cats & Dogs configurations,
  split into matching NeoForge server TOMLs; legacy food lists, biome categories, spawn caps,
  vanilla-animal replacement, husbandry timers, hive/wheel rates, salt, wagon and care rules are wired to the port
- optional Jade tooltips for animal needs, feed containers, cheese maturation and hamster-wheel status
- matching JEI and EMI cheese-aging displays for all five milk types, with the cheese mold as catalyst
- all 151 legacy OGG assets, exposed through 48 lowercase 1.21-compatible sound events
- species-, sex- and age-aware ambient/hurt/death sounds, plus hamster eating and vehicle hitch sounds
- mechanically converted chick, hen and rooster models retaining the original cubes, UVs, pivots and hierarchy
- mechanically converted cow, bull and calf models with the original breed-specific variants
- restored breed/sex cattle scaling and the adult mooshroom mushroom render layer
- mechanically converted kid, doe and buck models for every goat breed, including the original Angora geometry
- restored breed horn/ear profiles, beard and udder details, legacy scaling and client-synced Angora shearing
- exact static conversion of all 95 upstream Java ModelRenderer assets, including every animal variant
- exact conversion of all 18 upstream CraftStudio models and all eight keyframed animations
- original animated cart/chest-cart, wagon, tiller, hamster wheel, hamster and bee-hive rendering
- original CraftStudio rendering for all six pet props and original Java pet-bowl geometry
- per-breed cat/dog sitting poses mechanically restored from 171 original bone transforms
- 60 mechanically restored farm/pet sleeping poses, plus original renderer sleeping and nesting offsets
- synchronized trough-feeding head animations and restored fainting-goat collision/fall behavior
- all 52 original blink masks applied to their corresponding animal families
- exact per-root model render scales, including the peacock fan and a rebuilt 16-piece hamster ball shell
- complete item-model coverage for all 221 animal spawn eggs and both random pet eggs
- food/water fill rendering for the persistent pet bowl
- English and Simplified Chinese localization

The implementation was rebuilt as vertical slices. Shared husbandry
state is reused by every custom animal, while species registries retain the
original 1.12 paths wherever their meaning is unchanged.

## Build

Install a 64-bit Java 21 JDK, then run:

```powershell
.\gradlew.bat clean build -x test --no-daemon
```

The output jar is written to `build/libs/`.

When Gradle is run from the restricted Windows sandbox used for this port, keep
the JDK Unix-domain socket path short to avoid `Unable to establish loopback
connection`:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\jtmp'
.\gradlew.bat clean build -x test --no-daemon
```

This process-local option does not require changing the system `TEMP`/`TMP`.
Create `C:\jtmp` first if it does not exist on your machine.

The checked-in `libs/` EMI dependency is compile-only; do not restore the
unreliable SleepingTown Maven endpoint. See `libs/README.md` for its origin.
The mod requires NeoForge 21.1.249 or newer for Minecraft 1.21.1. Jade, JEI
and EMI are optional integrations, not required to play.

## Source asset conversion

Generated game resources are checked in, so a normal build does not need an
upstream checkout. To rerun the Node.js conversion tools, clone the original
source into `.upstream-animania` and use the reference commit:

```powershell
git clone --branch 1.12 https://github.com/capnkirok/animaniamod.git .upstream-animania
git -C .upstream-animania checkout 32ae2b4c56cb84284e865dae0d3b78770992ba1d
node tools/convert-legacy-models.mjs
node tools/convert-legacy-animation-poses.mjs
node tools/convert-craftstudio-models.mjs
```

For static resource validation, use PowerShell 7:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\tools\verify-port.ps1
```

## Roadmap

See [PORTING.md](PORTING.md). Cats & Dogs implementation is complete, including
the pet merchant and random cat/dog eggs. M7's configuration and optional
Jade, JEI and EMI integrations are implemented. M8's source asset conversion is
complete; real-client visual QA and release balancing remain.
Migration from legacy worlds is intentionally outside the scope of this port.
The implementation and static verification pass are complete; final in-game visual
and interaction QA is intentionally left to a real NeoForge client. Run
`tools/verify-port.ps1` to repeat the asset, localization, JSON, spawn-data and
configuration-consumer audit before packaging.

## License and provenance

This project is licensed under LGPL-3.0-only, matching the upstream repository.
Upstream code and assets remain copyright their respective contributors.
Original metadata credits RazzleberryFox, Purplicious_Cow, cy4n, Tschipp,
ZeAmateis, Timmypote, Raptorfarian, vroulas and TheJurassicAlien; the upstream
repository is hosted by capnkirok. This port is not endorsed by the original
team. See `NOTICE`, `LICENSE` (LGPLv3) and `COPYING` (GPLv3).
