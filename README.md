# Animania Reborn · 动物谷重生

Animania Reborn brings [Animania](https://github.com/capnkirok/animaniamod) to
Minecraft 1.20.1 on Forge. It combines Base, Farm, Cats & Dogs, and Extra in
one mod, using the original 1.12 source, models, textures and sounds.

## Installation

- Minecraft 1.20.1
- Forge 47.1.0 or newer for Minecraft 1.20.1
- Java 17

Place `animania-forge-1.20.1-0.1.2.jar` in your instance's `mods` folder and remove older
Animania Reborn jars before starting the game. Restart the game after updating.
Jade 11.13.1+, JEI 15.20.0.106+ and EMI 1.1.22+ are optional. Use their
Forge 1.20.1 builds.

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

Version 0.1.2 is still being checked in game. A successful build does not establish
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

These are new port recipes, available with Farmer's Delight installed. They add
no status effects and leave the original foods unchanged.
