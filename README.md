# Animania Reborn · 动物谷重生

Animania Reborn brings [Animania](https://github.com/capnkirok/animaniamod) to
Minecraft 1.21.1 on NeoForge. It combines Base, Farm, Cats & Dogs, and Extra in
one mod, using the original 1.12 source, models, textures and sounds.

## Installation

- Minecraft 1.21.1
- NeoForge 21.1.200 or newer
- Java 21

Place `animania-0.1.1.jar` in your instance's `mods` folder and remove older
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

Version 0.1.1 is still being checked in game. A successful build does not establish
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
