# Animania Reborn · 动物谷重生

Animania Reborn brings [Animania](https://github.com/capnkirok/animaniamod) to
Minecraft 1.21.1 on NeoForge. It combines Base, Farm, Cats & Dogs, and Extra in
one mod, using the original 1.12 source, models, textures and sounds.

## Installation

- Minecraft 1.21.1
- NeoForge 21.1.200 or newer
- Java 21

Place `animania-neoforge-1.21.1-0.1.2.jar` in your instance's `mods` folder and remove older
Animania Reborn jars before starting the game. Restart the game after updating.
Jade, JEI and EMI are optional.

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

With a Java 21 JDK installed, run:

```powershell
.\gradlew.bat build -x test --no-daemon
```

The jar is written to `build/libs/`. The build uses NeoForge 21.1.249; the mod's
metadata permits 21.1.200 and later. This lower bound is a compatibility
declaration, not a record of testing every NeoForge release.

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
