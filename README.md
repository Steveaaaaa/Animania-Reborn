# Animania Reborn · 动物谷重生

A continuation of [Animania](https://github.com/capnkirok/animaniamod), bringing Base, Farm, Cats & Dogs and Extra to modern Minecraft.

Since 1.0.0, Reborn has expanded beyond a direct port, combining the original mod's ideas with new breeds, animal behaviors and husbandry systems. Many features are configurable. Players who prefer the earlier port's gameplay can stay on 0.1.4.

## Installation

Current release: **1.0.1**. This branch targets **Minecraft 1.20.1**, **Forge 47.1.0+** and **Java 17**.

Put the matching JAR in your instance's `mods` folder and remove the previous Animania Reborn JAR.

- [NeoForge 1.21.1 source](https://github.com/Steveaaaaa/Animania-Reborn/tree/main)
- [Forge 1.20.1 source](https://github.com/Steveaaaaa/Animania-Reborn/tree/forge-1.20.1)

## Features

- Farm animals and pets with distinct breeds, feeding, breeding and family care.
- Species-specific activities, animations and husbandry mood.
- Animania-style foxes, mountain goats, bees and axolotls.
- Troughs, nests, beehives, cheese making, pet furniture and animal-drawn vehicles.
- Additional coats, patterned wool and beds, and breed-specific dairy products.
- English and Simplified Chinese translations.

## Compatibility

All integrations are optional; use versions matching your Minecraft version and loader.

- **Jade:** animal needs, sex, breeding, activities and mood information.
- **JEI / EMI:** cheese-aging recipes.
- **Farmer's Delight:** compatible ingredients and animal feeds, cutting-board and cooking-pot recipes, and specialty meals with food effects.

## Configuration

Settings are shared across worlds through the five Animania TOML files in `config`. You can control vanilla-animal replacement, exclude animals from hunger or thirst, and disable husbandry mood or its penalties. See [Husbandry](HUSBANDRY.md) for details.

Upgrading from world-specific configs? Close the game or server, back up existing files, then copy the Animania TOML files from your chosen world's `serverconfig` folder into `config`. Restart after editing.

## Development and feedback

Build with Java 17: `./gradlew build -x test --no-daemon` (`gradlew.bat` on Windows). Output goes to `build/libs`.

Report bugs through [GitHub Issues](https://github.com/Steveaaaaa/Animania-Reborn/issues), including mod versions, reproduction steps and the relevant log. Technical notes are in [PORTING.md](PORTING.md). Original 1.12 worlds cannot be imported directly.

## Credits and license

Original Animania contributors: RazzleberryFox, Purplicious_Cow, cy4n, Tschipp, ZeAmateis, Timmypote, Raptorfarian, vroulas and TheJurassicAlien. The original repository is hosted by capnkirok. Reborn is maintained by [Steveaaaaa](https://github.com/Steveaaaaa).

This is an unofficial continuation, not endorsed by the original team. Distributed under **LGPL-3.0-only**; see [LICENSE](LICENSE), [COPYING](COPYING) and [NOTICE](NOTICE). External recordings and their separate licenses are listed in [AUDIO_CREDITS.md](AUDIO_CREDITS.md).
