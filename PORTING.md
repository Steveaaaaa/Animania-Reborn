# Porting notes

## Reference source

The port uses Animania's `1.12` branch at commit
`32ae2b4c56cb84284e865dae0d3b78770992ba1d` from
https://github.com/capnkirok/animaniamod.

Base, Farm, Extra and Cats & Dogs share the `animania` namespace. Existing
registry names are retained where their meaning is unchanged. Common entity and
block code is kept separate from client rendering. Original 1.12 worlds cannot
be loaded directly into this port.

## Resources and animation

Converted resources are committed to the repository; a normal build does not
require the upstream checkout. The conversion tools read `.upstream-animania`.

```powershell
git clone --branch 1.12 https://github.com/capnkirok/animaniamod.git .upstream-animania
git -C .upstream-animania checkout 32ae2b4c56cb84284e865dae0d3b78770992ba1d
```

The main conversion tools are:

| Tool | Output |
| --- | --- |
| `tools/convert-legacy-models.mjs` | Java model geometry and hierarchy |
| `tools/convert-legacy-animation-poses.mjs` | Sitting poses and sleeping pose data |
| `tools/convert-legacy-motion.py` | Per-model animation methods |
| `tools/convert-legacy-sleep.py` | Timer-dependent sleeping branches |
| `tools/convert-craftstudio-models.mjs` | CraftStudio models and keyframes |

These tools overwrite their outputs. Review resource changes after regeneration;
converted assets include fixes made during game testing. The Java model converter
also accepts `--sheep-only --add-missing-nodes` to add missing sheep parts without
replacing existing geometry.

Model parts are instantiated even when the original renderer does not draw them.
For example, the peacock animation writes to the detached `FeatherD1` part.
Rendering and animation therefore use separate root inventories.

## Gameplay and configuration

Animal care, breeding and growth use persistent entity state. Values needed for
rendering are synchronized to clients. Species-specific AI references are listed
in [SPECIAL_AI_PORT.md](SPECIAL_AI_PORT.md).

The original settings are grouped into `animania-server.toml`,
`animania_farm-server.toml`, `animania_extra-server.toml` and
`animania_cats_dogs-server.toml`. Additional port settings are in
`animania-modern-server.toml`.

## Differences from 1.12

- Young mammals follow their recorded mother and require a matching breed.
- Sleeping animals keep their world-facing direction. Look, movement and target
  goals cannot override the resting pose. Sleep pose transitions still run.
- Rendering no longer advances sleep or mud-bath timers on every draw call.
  Sleep visuals advance once per client tick; mud state advances on the server.
  This avoids frame-rate-dependent timing and extra updates during Iris shadow
  passes. Timing can differ from the original at a given frame rate.
- Peacocks close their tail fans during sleep and reopen them on waking. The
  original only tipped the expanded fan backwards.
- Wagon hitch distance includes the pole's reach, animal clearance and a
  0.35-block gap: roughly 5.83 blocks for a draft horse. Cart and tiller spacing
  remains 2.5 blocks. The traces follow both the vehicle pitch and horse body.
- CraftStudio faces with reversed winding are submitted in the corrected order,
  with UV associations preserved. This addresses dark wagon canopy panels under
  shaders. Thin slabs replace zero-thickness feather planes to reduce depth flicker.

## Forge 1.20.1 adaptation

The Forge branch starts from NeoForge 0.1.1 at commit `68484dc`. It retains the
animal, AI, model and animation implementations while adapting platform APIs:

- Forge registry objects, lifecycle events and configuration specs.
- Persistent entity NBT and a tracking channel in `ModAttachments`, replacing
  NeoForge attachments. Temporary fighting state remains unsaved.
- `AnimalTickBridge` preserves the update order before and after entity AI,
  including resting orientation and one animation update per client tick.
- Forge block-entity capabilities preserve inventory, fluid and energy access.
- Item NBT replaces data components; recipes, loot tables, tags and model loaders
  use the 1.20.1 formats.
- The vehicle uses Forge's spawn packet and the 1.20.1 passenger-position callback.
- Rendering uses the 1.20.1 vertex and model interfaces with the existing geometry,
  UV coordinates, normals and animation formulas.

`main` remains the NeoForge 1.21.1 branch; `forge-1.20.1` is maintained
separately. Review gameplay fixes for both branches. Shared conversion tools still
read the same original 1.12 checkout.

## Building

Use Java 17 and the Gradle wrapper. Both compilation and `META-INF/mods.toml`
target Forge 47.1.0 as the minimum version for Minecraft 1.20.1.

```powershell
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=C:\jtmp'
.\gradlew.bat build -x test --no-daemon
```

The jar task is followed by ForgeGradle's `reobfJar`, producing the distributable
`animania-forge-1.20.1-0.1.2.jar`. EMI uses the local Maven directory in `libs/` and
is not bundled. Do not substitute the NeoForge EMI jar.

No GameTests or unit tests are used. Game behaviour, shader support and other mod
combinations are checked manually. The optional resource inventory script is not
part of the regular build.
