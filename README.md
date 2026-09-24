# Animania Reborn · 动物谷重生

Animania Reborn brings [Animania](https://github.com/capnkirok/animaniamod) to
Minecraft 1.20.1 on Forge. It combines Base, Farm, Cats & Dogs, and Extra in
one mod, using the original 1.12 source, models, textures and sounds.

## Installation

- Minecraft 1.20.1
- Forge 47.1.0 or newer for Minecraft 1.20.1
- Java 17

Place `animania-forge-1.20.1-1.0.0.jar` in your instance's `mods` folder and remove older
Animania Reborn jars before starting the game. Restart the game after updating.
Jade 11.13.1+, JEI 15.20.0.106+ and EMI 1.1.22+ are optional. Use their
Forge 1.20.1 builds.

See [Husbandry mood](HUSBANDRY.md) for species needs, care bonuses and configuration.

## Features

- Farm animals with breeds, separate male and female roles, offspring, feeding,
  watering, breeding and milk production.
- Cats, dogs, rabbits, peafowl, rodents and amphibians, with their own habitats
  and behaviours.
- Troughs, nests, pet furniture, cheese making, beehives and hamster wheels.
- Carts, wagons and tillers pulled by animals.
- Animal care information through Jade and cheese recipes through JEI or EMI.
- English and Simplified Chinese translations.

The port follows the original gameplay where possible. Differences include
sleeping animals keeping their resting direction, more clearance between the
wagon pole and its horse, and peacocks closing their tail fans when asleep.
See [PORTING.md](PORTING.md) for implementation notes and
[SPECIAL_AI_PORT.md](SPECIAL_AI_PORT.md) for AI and animation references.

Version 1.0.0 is still being checked in game. A successful build does not establish
compatibility with every shader pack or mod combination. When reporting a bug,
include the mod versions, steps to reproduce it, and the crash report or relevant
part of `latest.log`. Importing worlds from the original 1.12 mod is not supported.

## Building

With a Java 17 JDK installed, run:

```powershell
.\gradlew.bat build -x test --no-daemon
```

The jar is written to `build/libs/`. Compilation targets Forge 47.1.0 and Java 17.
The loader range is a compatibility declaration; later Forge builds still need
in-game checking.

This branch, `forge-1.20.1`, maintains the Forge port. The `main` branch
continues to maintain NeoForge 1.21.1. Both descend from the 0.1.1 implementation;
fixes should be reviewed and applied to each branch as needed. Worlds cannot be
downgraded from Minecraft 1.21.1 to 1.20.1.

If Windows reports `Unable to establish loopback connection`, create `C:\jtmp`
and set a short socket path for the current shell:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\jtmp'
.\gradlew.bat build -x test --no-daemon
```

The local EMI jar is a compile-only dependency and is not included in the mod.
Its source and license are listed in [libs/README.md](libs/README.md).

## Credits and license

Original Animania contributors: RazzleberryFox, Purplicious_Cow, cy4n, Tschipp,
ZeAmateis, Timmypote, Raptorfarian, vroulas and TheJurassicAlien. The original
repository is hosted by capnkirok. Port maintained by
[Steveaaaaa](https://github.com/Steveaaaaa/Animania-Reborn).

This is an unofficial port and is not endorsed by the original team. Code and
assets retain their original credits. The project is distributed under
LGPL-3.0-only; see [NOTICE](NOTICE), [LICENSE](LICENSE) and [COPYING](COPYING).

## Farmer's Delight

Optional compatibility includes meat, milk and egg ingredient tags. Prime beef,
pork, bacon, chicken and mutton work in recipes accepting their corresponding
food tags; other prime meats also count as meat in generic recipes. Prime rabbit
has a separate cooking-pot rabbit stew recipe. Recipes that explicitly require
Farmer's Delight minced beef, patties or other processed cuts retain those inputs.

- Cut any of the five cheese wheels on a cutting board with a knife to get four wedges.
- Cook two truffles in a cooking pot for 200 ticks, then serve with a bowl.
- Cook brown eggs into Farmer's Delight fried eggs, including on its stove.
- Craft one Farmer's Delight straw into one Animania straw bedding. Existing nest
  recipes can use that bedding. There is no reverse conversion.
- Feed Farmer's Delight dog food to dogs by hand or place it in a pet bowl.
  Hand-feeding returns the empty bowl; pet bowls drop it when a dog eats the food.
  Removing uneaten food returns the original serving instead.

Milk bottles return their glass bottles when used as crafting ingredients.
These additions do not change hunger timers, breeding requirements or milk yields.
Farmer's Delight is not required to load Animania Reborn.

### More cooking and animal feeds

Cheese Sandwich uses bread, any cheese wedge and a cabbage leaf. It restores
8 hunger points. Truffle Risotto uses rice, a truffle, any cheese wedge and milk
in a cooking pot (200 ticks), served with a bowl; it restores 12 hunger points.
Both are new dishes added by this port and require Farmer's Delight to craft.

Vegetable slop uses two servings of carrots, potatoes, beetroot, cabbage, cabbage
leaves or tomatoes, plus rice and a water bucket. Cook for 200 ticks and serve
with an empty bucket. The ingredient water bucket leaves its empty bucket in the
pot's remainder output. The resulting slop uses the existing pig-feeding rules.

| Animals | Additional Farmer's Delight feeds |
| --- | --- |
| Cows, sheep, goats, horses | Rice |
| Pigs | Rice, cabbage, cabbage leaves, tomatoes |
| Rabbits | Cabbage, cabbage leaves |
| Chickens, peafowl, hamsters | Rice, cabbage seeds, tomato seeds |

These foods work for hand-feeding and attraction. Troughs accept all listed feeds,
but animals only eat food appropriate to their species; hamster feeds also work
in pet bowls. Existing configuration files do not need to be deleted. Additional
feeds can be changed through the `animania:compat/farmersdelight/feed/<animal>`
item tags. Onions are not included.

### Cutting meat and cooking existing dishes

A cutting board can turn one raw prime beef into four raw prime steaks, or one
raw prime pork into four raw prime bacon. These are the original carving yields;
there is no reverse recipe and the cuts retain their prime quality.

The cooking pot can prepare plain, cheese, bacon, truffle and super omelettes,
as well as chocolate truffles. Each recipe takes 200 ticks and produces one item
with its existing nutrition and effects. Omelettes use two eggs plus the fillings
from the original crafting recipe; bacon recipes accept cooked prime bacon or
Farmer's Delight cooked bacon. These dishes do not require a serving container.

Farmer's Delight cooked bacon also works in separate crafting recipes for bacon
and super omelettes. Two Farmer's Delight fried eggs can be crafted into one
plain omelette. The original recipes remain available without Farmer's Delight.

### Specialty meals

These three cooking-pot recipes each make one serving in 300 ticks (15 seconds).
Each requires a bowl, stacks to 16 and returns the bowl after eating.

| Meal | Ingredients | Hunger restored |
| --- | --- | --- |
| Tomato Chevon Stew | Raw chevon (regular or prime), tomato, carrot, potato, onion | 12 |
| Truffle Peacock Rice | Raw prime peacock, truffle, rice, onion, carrot | 14 |
| Three Cheese Pasta | Raw pasta, one cow cheese wedge (Holstein, Friesian or Jersey), goat cheese wedge, sheep cheese wedge, milk | 12 |

These are new port recipes, available with Farmer's Delight installed. The original foods remain unchanged.

### Meal effects

| Meal | Effects |
| --- | --- |
| Cheese Sandwich | Nourishment for 1 minute; Regeneration I for 10 seconds |
| Truffle Risotto | Nourishment for 3 minutes; Resistance I for 90 seconds |
| Three Cheese Pasta | Strength II, Haste II and Resistance I for 30 seconds |
| Tomato Chevon Stew | Steady Steps for 3 minutes |
| Truffle Peacock Rice | Composure for 4 minutes |

Steady Steps earns one charge per block of cumulative uphill walking, up to
three. Both movement samples must be grounded, so jumping, ladders, swimming,
flying, sprinting and riding do not earn charges. The next damaging fall consumes
all charges: each removes up to 2 points of remaining health damage, capped at
50%. Charges are temporary and reset on eating another serving or reconnecting.

Composure becomes ready after 10 seconds without taking health damage or
attacking. A hit from another living entity consumes readiness, reduces remaining
health damage by 40% (up to 6 points), and grants Speed II for 5 seconds. Any
health damage or attack restarts preparation; environmental damage cannot trigger
the defense. Arrows and thrown projectiles also interrupt preparation when fired.

The custom effects appear in the status-effect display; action-bar messages
announce charges, readiness and activation. Milk removes the effects. Repeated
servings do not stack effect levels or add durations together. All meal bonuses
respect the existing food bonus effects configuration. Nourishment is resolved
from Farmer's Delight when present, without adding a required dependency.

## Simmental cattle

Simmental is a new dual-purpose breed using the original Jersey bull, cow and
calf textures from Animania 1.4.4 (upstream commit `48129f3d`). The Hereford adult
models and standard calf model match that release's Jersey renderers. It retains
Jersey animation branches, including grazing and sleeping, without replacing Jersey.

Simmental cows spawn in plains and hills, configurable through `cowSimmental`
in the farm biome settings. They follow the existing care, breeding and growth
rules, provide ordinary milk under the existing lactation conditions, and use the
prime beef loot table as adults. All three roles have separate spawn eggs.

## Fighting cattle

Fighting cattle are a separate beef breed using the horned Angus appearance from
Animania 0.9.8 BETA. Bull, cow and calf textures come unchanged from CurseForge
file 2383989. That release uses ModelBull, ModelCow and ModelCalf, with adult
scales of 1.4 for bulls and 1.34 for cows. This port uses those model families
with the existing 1.12 animation and animal-care systems.

They spawn in savannas by default (`cowFighting` in the farm biome settings),
have separate bull, cow and calf spawn eggs, and drop prime beef as adults.
Breeding, growth and bull behaviour follow the existing cattle rules. The breed
name does not enable additional fighting AI. Existing Angus cattle are unchanged.

## Modern wildlife

Newly generated wild foxes and mountain goats can now use Animania care,
sexes, pregnancy, and care-dependent growth. Cubs and kids follow their recorded
mother. Feed and water them by hand or through troughs and pet bowls; Jade shows
care and breeding information. Sneak-right-click with an empty hand also shows
sex and pregnancy without Jade. Every breed has its own registered entity type
and fixed-breed spawn egg in the creative tab.

Foxes retain the vanilla red/snow variants, pouncing, item carrying and daytime
sleep. Cubs inherit their parents' variant and can trust the players who fed
their parents. Foxes cannot breed with dogs or wolves, including legacy pet foxes.
Mountain goats retain vanilla jumping, ramming, screaming variants and horn drops.
They graze and rest at night; a cared-for adult female can provide one bucket of
milk after giving birth, before her kid matures. Mountain goats and domestic
Animania goats remain separate species.

`replaceModernFoxes` and `replaceMountainGoats` in `animania-modern-server.toml`
control replacement of new natural/structure spawns. Saved animals, named pets,
vanilla spawn eggs and commands are left intact. Legacy fox spawn entries are
suppressed while modern fox replacement is enabled to avoid duplicate populations.
The new wildlife uses separate Animania models: foxes have a chest ruff, cheek
fur and a fuller, segmented tail; mountain goats have a shaggy shoulder outline
and narrow, swept-back horns. Female mountain goats have shorter horns.

Red foxes can have red, silver or cross coats; snow foxes retain their white coat.
Mountain goats can have white, cream or slate coats. Breeds have separate entity IDs: `red_fox`, `silver_fox`, `cross_fox`, `snow_fox`,
`white_mountain_goat`, `cream_mountain_goat` and `slate_mountain_goat`. Offspring
inherit one parent's breed and spawn as that entity type. Jade and
sneak-right-click show the breed name. Textures are in `assets/animania/textures/entity/modern`.
All supported vanilla-animal natural replacement switches default to enabled,
including horses. Existing saved animals and explicit configuration choices are
preserved.

The older `modern_fox` and `mountain_goat` entity IDs remain loadable for existing
saves. Their generic spawn eggs are retained for saved inventories but no longer
appear in the creative tab. New natural spawns and births use the breed-specific
entity IDs. A breed-specific fox egg keeps its breed in every biome.

### 蜜蜂与蜂箱

新增琥珀、深色、浅色三种蜜蜂，分别注册实体和刷怪蛋，使用独立模型与毛色贴图。保留原版采花、授粉、繁殖、愤怒及蜇刺行为，兼容原版蜂巢。默认替换新自然生成的蜜蜂和新生成区块中蜂巢的初始住户，不转换旧区块中的蜜蜂。

动物谷蜂箱和野生蜂箱可容纳三只新蜜蜂。蜜蜂携蜜、夜间或下雨时会寻找附近蜂箱，入口需留空；无花蜜停留至少 600 tick，携蜜至少 2400 tick，白天且无雨时离开。每次完成携蜜返回增加一个配置产蜜周期的蜂蜜量，原有自动产蜜规则保留。蜂箱保存住户、品种和停留时间，拆除或附近起火时释放蜜蜂。Jade 和蜂箱右键提示显示住户数量。

### Optional animal needs and vanilla spawning

Edit `config/animania-modern-server.toml` inside the instance or server folder, then restart the game or server. Settings apply across all worlds. To retain previous settings, copy the Animania TOML files from the chosen world’s `serverconfig` folder into `config` while the game is closed, backing up any existing files first. These options also apply to existing Animania animals. They do not add needs to vanilla or other mods' animals.

```toml
[husbandry]
hungerBlacklist = ["cats", "dogs", "animania:*_draft"]
thirstBlacklist = ["cats", "dogs", "animania:*_draft"]

[spawning]
replaceVanillaAnimals = false
```

Merge these entries into the existing sections rather than duplicating the section headers. Each blacklist accepts exact entity IDs, `*` wildcards, or the groups `cats` (including ocelots), `dogs`, `wolves`, `foxes`, `cows`, `pigs`, `sheep`, `goats`, `horses`, `chickens`, `peafowl`, `rabbits`, `hamsters`, `hedgehogs`, and `ferrets`. Groups include all breeds, sexes and ages. An empty list preserves normal care; `["*"]` disables the corresponding need for all supported animals. Feeding, taming, breeding, sleep and species-specific behavior remain available. Blacklisted needs stop counting down and no longer trigger food/water searches. This reduces that work, but is not a general AI or performance switch.

`replaceVanillaAnimals = false` keeps vanilla animals alongside independently enabled Animania spawns. It does not add natural spawns for breeds that only appear through replacement (such as modern mountain goats and bees), nor restore animals already replaced. Keep the master switch true to choose replacements individually. `replaceVanillaCats` now controls village cats independently of `replaceVanillaOcelots` in the legacy cats-and-dogs config. Existing per-species replacement switches remain available. No existing saved animals are converted by these options.

### Axolotls

Leucistic, wild, golden, cyan and blue axolotls have separate entity IDs, spawn eggs and buckets. Their models use solid limbs, a lower jaw and a thicker tail base, while retaining the native swimming, crawling, gill and play-dead animations. Entity textures keep a 64x64 UV grid; bucket icons are 16x16.

New natural axolotls are replaced by the four common breeds when `replaceAxolotls` and the master replacement switch are enabled. Existing animals and vanilla bucket releases are preserved. Offspring inherit either parent's breed, with the native 1/1200 chance of a blue offspring. A dedicated breeding goal allows the separate breed IDs to mate without replacing the native hunting and play-dead brain outside courtship. Reproduction keeps the vanilla food-triggered cycle, without mammalian pregnancy or milk.

Feed tropical fish buckets as in vanilla; successful underwater kills also satisfy hunger. Water keeps thirst satisfied. Young animals use the shared care-dependent growth settings. `axolotls` is accepted by both need blacklists. Buckets preserve age, health, hunting cooldown, care timers and growth progress, and work with dispensers. Native drying-out damage remains separate from the thirst blacklist. This is an extension for modern Minecraft, not a restored 1.12 animal.

### Additional chicken breeds

Amber, Bronzed, Gold Crested, Midnight, Cold and Warm chickens have separate
rooster, hen and chick entities and spawn eggs. Existing breeds and saved animals
are unchanged. Coats use the original Animania UV grids, with a blue-gray feather crest
for adult Cold chickens. The original animation families remain
responsible for walking, nesting, sleeping, flapping and crowing.

Amber, Bronzed and Warm hens lay brown eggs; Cold hens lay blue eggs; Gold Crested
and Midnight hens lay ordinary eggs. Bronzed adults use prime chicken drops.
These husbandry roles are Reborn additions inspired by Earth and modern vanilla
variants, rather than a claim that all those mechanics existed in Earth.

Blue eggs appear in nests, can be collected by hand or hopper, and hatch through
the existing nest rules. They join the egg ingredient tags and Farmer's Delight
fried-egg cooking recipes. Animals whose configured food lists accept brown eggs
also accept blue eggs. With egg throwing enabled, thrown blue eggs can hatch Cold
chicks; dispensers use the same projectile. Outside-nest laying follows each
breed's egg color when chickensDropEggs is enabled.

The default chicken spawn weights still total 45, shared between all eleven
breeds. Each new breed has its own farm biome setting: chickenAmber,
chickenBronzed, chickenGoldCrested, chickenMidnight, chickenCold and chickenWarm.
The existing chicken spawn switch, probability, family size and population cap
still apply. No existing saved chickens are converted.

### More pig and rabbit breeds

Mottled, Piebald and Pink Footed pigs join eight new rabbit breeds: Desert,
Black and White, Salt and Pepper, Vested, Bold Striped, Freckled, Harelequin
and Muddy Foot. Each has separate male, female and young spawn eggs, using
Animania's existing care and breeding systems. Their coats adapt Earth, Dungeons
and vanilla references to Animania models. Existing breeds remain unchanged.

### More cattle breeds

Eight additional cattle breeds include Umbra, Wooly, Warm, Pinto, Albino,
Norwegian Red, Cream and Cookie. Each has male, female and calf spawn eggs.
Umbra and Wooly adults can be sheared, with visible coat removal and regrowth.
New breeds retain Animania's feeding, pregnancy and milk-production rules.

### More sheep breeds

Flecked, Fuzzy, Inky, Long Nosed, Patched and Rocky sheep each have ram, ewe and
lamb spawn eggs. Fuzzy sheep yield 4–6 white wool per shearing. Flecked, Inky,
Long Nosed and Rocky sheep produce patterned brown, inky, tan and rocky wool;
these blocks can also craft matching patterned beds. Long Nosed sheep
drop prime mutton. Existing sheep breeds and saved animals remain unchanged.


### Ferret coats and small-animal breeding

Cinnamon and Sable ferrets have separate entities and spawn eggs, with coats
inspired by Minecraft Dungeons. They retain Animania's ferret feeding, taming,
carrying and following behavior.

Hamsters, hedgehogs and ferrets now have persistent sexes and can breed with a
compatible opposite-sex animal. The existing feeding, gestation, litter-size,
sterilization and breeding-limit settings apply. Offspring inherit one parent's
coat, follow their recorded mother and grow with care. Jade shows sex and
pregnancy. Existing entity IDs, pet owners and saved coat colors are preserved.

### Patterned wool beds

Flecked, Inky, Tan and Rocky wool each craft a matching bed using three wool
blocks above three planks. The quilt retains the wool pattern. These beds use
vanilla sleeping, spawn-point, occupancy and dimension rules and can be claimed
by villagers. Wool-to-vanilla-wool recipes remain available for plain beds.

### Additional domestic cats

Black, Tuxedo, Red Tabby, British Shorthair, Calico, Persian, White and Jellie
cats have separate male, female and kitten spawn eggs. Their coats adapt vanilla
Minecraft colors to Animania models. Existing Ragdoll, Siamese and brown Tabby
cats remain unchanged. The new cats are available from the pet merchant and
random cat eggs, and can appear through the existing village-cat replacement
option. They use the normal cat care, taming and breeding systems.

### Additional wolf coats

Ashen, Black, Chestnut, Rusty, Spotted, Striped, Snowy and Woods wolves have
separate male, female and young spawn eggs. Their coats follow Java 1.21.1's
wolf variants while retaining Animania's wolf model and behavior. Existing
wolves, including their eight random coats, remain available and saved animals
are not converted.

New wolves use the corresponding vanilla habitats: snowy taiga, old-growth
pine taiga, old-growth spruce taiga, jungles, savannas, badlands, groves and
forests respectively. In these habitats they take the existing wolf spawn
weight rather than adding another wolf entry on top. The old wolf still spawns
in its other habitats. Existing wildlife, replacement and family settings apply.

### Pet sounds and wolf coat refinements

Cats, ocelots, dogs, wolves and legacy foxes now have ambient, hurt and death
sounds. Awake cats can hiss at a target or purr while sitting, tamed and fed;
dogs and wolves growl at targets and whine when tamed and hungry or badly hurt.
Sleeping pets remain quiet during normal ambient and footstep playback.
English and Chinese subtitles distinguish the animals and their sound events.
Ferrets and hedgehogs reuse their existing hurt recordings for death, and
amphibians use vanilla frog hurt/death sounds.

Audio uses Minecraft events, original Animania recordings and the CC0
external recordings credited in AUDIO_CREDITS.md.
Individual Animania sound-event IDs let resource packs replace each animal's
sounds independently. New wolf coats retain their native pixel grid, with
stepped fur borders, less geometric spots and shading from the original wolf
texture. Existing wolf coats and cat appearances are unchanged.

### External animal recordings

Twelve edited CC0 clips now provide four dog barks, a dog growl and whine,
two cat meows, two cat purrs and two wild-wolf howls. All are mono 44.1 kHz
Ogg Vorbis for positional audio. AUDIO_CREDITS.md lists authors, sources and
processing; the same credits are included in each JAR's META-INF directory.

Adult wolves can occasionally howl while awake, on land, idle, healthy and
fed, with no active attack target. Howls have a one-to-two-minute minimum
cooldown after playback is selected, a one-in-eight selection chance, and
prevent another ambient call for twelve seconds. Sleeping wolves and pups
do not start howls. Existing barks, growls and injury sounds remain separate.
Cats and dogs use the new recordings for the matching ambient events; their
existing injury sounds remain. Ferret and hedgehog recordings are unchanged.

The preparation script and source manifest are tools/prepare-external-audio.py
and tools/external-audio.json. Source files are downloaded separately; only
the processed game clips are bundled. No paid sound-library files are used.
