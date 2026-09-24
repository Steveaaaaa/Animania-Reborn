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
replacing existing geometry. `--nest-only` exports the original nest egg models.

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
  shaders. Feather planes use one uncullable face with the original UV coordinates;
  adding thickness shifted UVs into adjacent pixels and caused pale outlines.
- Birds drink from troughs, bowls and held water containers without reducing their water level. The 1.12
  code consumed 50 mB per drink; natural water blocks remain intact in both versions.
- Angora goats support wool dyeing. The old goat interaction accepted dye clicks
  but did not apply a color. Sheep retain separate natural coat and wool dye colors.
- Hungry pigs can finish snuffling by eating grass when they have not found food.
  The original snuffling goal only fed them from truffles.
- Distant pets can teleport even when navigation returns an incomplete path.
  Wandering no longer interrupts feeding or active food following.
- Village structure animals use the configured biome breeds and replacement
  switches. Named animals and player-created vanilla animals are not converted.

For a version-range-only metadata edit, no Java changes or verification are
required. Normal development builds skip tests. Game behaviour and shader
compatibility are checked manually. The optional `tools/verify-port.ps1` script
checks resource and registration inventories; it is not a substitute for playing
the game and is not run after every change.

## Pixel art

New pixel-art assets use a 16 x 16 canvas by default. Each pixel occupies one
cell on a consistent grid. Use a compact palette, hard edges and deliberate
clusters; avoid mixed pixel sizes, anti-aliasing, smooth gradients and dithering.
Keep silhouettes readable at native size. Establish the shape and value contrast
first, optionally in grayscale, then add color and sparse texture. Inspect the
native-size result as well as a nearest-neighbor enlargement before shipping.

## Build delivery

After each build, copy the release JAR to the existing Minecraft-version folder
under `Desktop/新建文件夹`: Forge builds go in `1.20.1`, and NeoForge builds go in
`1.21.1`. These folders are named for Minecraft versions, not mod release numbers.
Update the matching JAR filename while keeping older releases and unrelated files.

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

## Modern wildlife extension

The modern fox and mountain-goat breeds extend the vanilla Fox and Goat classes.
Their pregnancy, care and growth use the shared Animania settings. This is new
content, not a reproduction of a 1.12 feature. Existing fox registry IDs remain
available for saved pets and spawn eggs.

Mountain-goat husbandry goals wait for native ram/jump activities to finish.
While a care goal owns movement, the goat brain is suspended; its normal
activities resume afterward. Foxes retain their native sleep schedule and pose.
Natural replacement is limited to natural, chunk-generation and structure spawn
reasons; it does not convert animals as saved chunks load.

Modern wildlife models define their own mesh while retaining the native bone
names for sleeping, pouncing, sitting, ramming and horn visibility. Coat textures
retain the native 48x32 fox and 64x64 goat UV grids; additional model details use
explicitly filled UV islands. `tools/paint-modern-wildlife.py` rebuilds these
palettes from a vanilla client JAR. Each fox coat includes a separate closed-eye
texture. Breed-specific entity types fix their appearance. `AnimaniaCoat` remains saved
and synced for the older generic entities; it does not override a fixed breed.
Pregnancy snapshots store the sire's entity ID so offspring can inherit his breed
after a reload, even if the sire is no longer loaded.

### 蜜蜂与蜂箱

新增琥珀、深色、浅色三种蜜蜂，分别注册实体和刷怪蛋，使用独立模型与毛色贴图。保留原版采花、授粉、繁殖、愤怒及蜇刺行为，兼容原版蜂巢。默认替换新自然生成的蜜蜂和新生成区块中蜂巢的初始住户，不转换旧区块中的蜜蜂。

动物谷蜂箱和野生蜂箱可容纳三只新蜜蜂。蜜蜂携蜜、夜间或下雨时会寻找附近蜂箱，入口需留空；无花蜜停留至少 600 tick，携蜜至少 2400 tick，白天且无雨时离开。每次完成携蜜返回增加一个配置产蜜周期的蜂蜜量，原有自动产蜜规则保留。蜂箱保存住户、品种和停留时间，拆除或附近起火时释放蜜蜂。Jade 和蜂箱右键提示显示住户数量。

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

### Appearance references for new breeds

For a breed originating in another Minecraft game, preserve its recognizable
color layout, markings and silhouette before adapting it to Animania's mesh and
animation system. Do not infer its appearance from its name or substitute the
appearance of a presumed real-world breed. Existing Animania breeds remain intact.
Adult references guide the new male/female versions; juvenile adaptations should
retain the breed's recognizable colors without being presented as official designs.

The additional chicken coats were revised against these references:

- [Amber](https://minecraft.fandom.com/wiki/Minecraft_Earth:Amber_Chicken): golden plumage with apricot-colored beak and legs.
- [Bronzed](https://minecraft.fandom.com/wiki/Minecraft_Earth:Bronzed_Chicken): copper head, teal front, darker underside, indigo back and pale legs.
- [Gold Crested](https://minecraft.fandom.com/wiki/Minecraft_Earth:Gold_Crested_Chicken): golden upper plumage and wings with a pale lower body. The earlier invented tall crown was removed.
- [Midnight](https://minecraft.fandom.com/wiki/Minecraft_Earth:Midnight_Chicken): blue-black plumage, dark legs and a gray stripe on the beak.
- [Cold and Warm](https://www.minecraft.net/en-us/article/new-features-hatching): blue-gray Cold plumage and crest; sandy golden-brown Warm plumage with darker wing bands.

These are newly painted adaptations on Animania UVs, not imported official game
textures. The coat generator records the palettes and part-specific UV painting.

### Additional pig and rabbit breeds

Mottled, Piebald and Pink Footed pigs have separate hog, sow and piglet entities.
Desert, Black and White, Salt and Pepper, Vested, Bold Striped, Freckled,
Harelequin and Muddy Foot rabbits have separate buck, doe and kit entities.
All 33 entities have their own spawn eggs and English/Chinese names.

These breeds use the existing pig and rabbit animation families, feeding,
needs, sleep, pregnancy, offspring inheritance and growth rules. They use
ordinary meat drops. Existing breeds, saved entities and entity IDs are unchanged.
Spawn weights are shared with the existing breeds, retaining each biome
modifier's previous total. Each new breed has a configurable habitat entry.
Desert rabbits inhabit sandy biomes; Black and White and Vested rabbits plains;
Salt and Pepper rabbits mountains/hills; the four Earth rabbits forests.

The coats are newly painted adaptations on the original Animania meshes. Pig
sheets remain 64x32 (32x32 for piglets), and rabbit sheets remain 128x128, without
resampling. Shared leg UVs and the original mud overlay remain in use, so the
Piebald leg markings are symmetrical rather than reproducing Earth's asymmetric
leg spots. Male, female and juvenile presentations use Animania's original body
shapes, rather than importing the other games' models.

Appearance references:

- [Mottled Pig](https://minecraft.fandom.com/wiki/Minecraft_Earth:Mottled_Pig): dark brown broken bands, pale feet and a brown muzzle.
- [Piebald Pig](https://minecraft.fandom.com/wiki/Minecraft_Earth:Piebald_Pig): cream coat, brown rear patch and crown spot, dark tail and light brown hooves.
- [Pink Footed Pig](https://minecraft.fandom.com/wiki/Minecraft_Earth:Pink_Footed_Pig): dark body, pale knees, pink feet and muzzle, and a small pale belly patch.
- Desert, Black and White and Salt and Pepper rabbits: the vanilla Java 1.20.1 `gold`, `white_splotched` and `salt` coat references. Black and White uses irregular patches, distinct from the existing Dutch breed.
- [Vested Rabbit](https://minecraft.wiki/w/Dungeons:Vested_Rabbit): the Dungeons white coat with a gray face, pale gray lower body and dark gray feet.
- [Bold Striped Rabbit](https://minecraft.fandom.com/wiki/Minecraft_Earth:Bold_Striped_Rabbit): dark coat with brown markings, pale muzzle and foot tips.
- [Freckled Rabbit](https://minecraft.fandom.com/wiki/Minecraft_Earth:Freckled_Rabbit): a white coat with small brown, gray and dark flecks concentrated toward the rear.
- [Harelequin Rabbit](https://minecraft.fandom.com/wiki/Minecraft_Earth:Harelequin_Rabbit): dark head, pale muzzle and upper body, with a brown transition to the darker lower coat. The entity ID uses Earth's spelling, `harelequin`.
- [Muddy Foot Rabbit](https://minecraft.fandom.com/wiki/Minecraft_Earth:Muddy_Foot_Rabbit): a white coat with brown ear tips, feet, tail and muzzle. These markings are permanent fur coloration, not a mud state.

### Additional cattle breeds

Umbra, Wooly, Warm, Pinto, Albino, Norwegian Red, Cream and Cookie cattle have
separate cow, bull and calf entities and spawn eggs. Existing cattle IDs and
saved animals are unchanged. New calves inherit one parent's breed through the
existing pregnancy rules, and use the normal care-dependent growth system.

Umbra and Wooly use additional coat meshes attached to the original animated
head and body bones. Umbra's fringe covers both eyes; Wooly's longer fringe
covers one. Adults can be sheared for 1–2 black or brown wool respectively,
including through vanilla shearing dispensers. Shearing removes the outer mesh
and switches to a shorter-coat texture. Regrowth uses `woolRegrowthTimer` while
the animal is fed and watered. The timer is saved with the animal and the coat
state is synchronized to clients. This husbandry cycle is a Reborn adaptation
of Earth's shearable cattle; the existing Highland breed is unchanged.

Warm, Umbra, Wooly and Cookie cattle use prime beef drops. Pinto, Albino,
Norwegian Red and Cream use ordinary beef. New mothers produce ordinary milk
under the existing pregnancy and care rules; the original breed-specific milks
and cheeses remain unchanged. These production choices are Reborn balancing,
not properties claimed for the source games or real breeds.

Natural spawn weights still total 81 in the existing cattle modifier. Albino
has weight 1, other new breeds weight 5; every new breed has its own habitat
configuration. Existing population caps, spawn switches and replacement rules
still apply. No new breed converts animals already present in a saved world.

Reference notes:

- [Umbra](https://minecraft.fandom.com/wiki/Minecraft_Earth:Umbra_Cow): purple-tinted dark hair, brown hair tips, a covered face and violet-gray muzzle/horns.
- [Wooly](https://minecraft.fandom.com/wiki/Minecraft_Earth:Wooly_Cow): thick brown hair, pale horns, pink muzzle and an uneven fringe.
- [Warm](https://www.minecraft.net/en-us/article/more-minecraft-game-drop-features): auburn coat, adapted to Animania's existing longhorn silhouette.
- [Pinto](https://minecraft.wiki/w/Earth:Pinto_Cow): cream ground with large brown patches.
- [Albino](https://minecraft.fandom.com/wiki/Minecraft_Earth:Albino_Cow): pale coat, pink muzzle and eyes, beige horns and light brown hooves.
- Norwegian Red: a vanilla-inspired brown-and-white coat. The breed name does not imply that every real Norwegian Red has Minecraft's markings.
- [Cream](https://minecraft.fandom.com/wiki/Minecraft_Earth:Cream_Cow): honey-colored coat with pale wavering spots, blue-gray horns and muzzle, caramel hooves.
- [Cookie](https://minecraft.fandom.com/wiki/Minecraft_Earth:Cookie_Cow): blue-gray coat and a pale belt. The approved hornless beef-breed design uses Animania's Angus mesh; Earth's Cookie Cow itself had horns.

Textures retain the original pixel density. Wool-coated models extend their
UV sheets vertically for separate coat islands, with matching padded blink
masks. The new cattle and the previously added pigs/chickens are mapped to their
original model's breed-specific animation branches, including feeding poses.

### Additional sheep breeds

Six Earth-inspired breeds have separate ram, ewe and lamb entities and spawn
eggs. Their hornless models retain the Merino ewe's animated bones, with larger
male and smaller lamb scales. Fuzzy sheep have a thicker outer fleece and a
small tongue; Long Nosed sheep have an extended muzzle. Native 128x128 UV sheets
retain the original pixel density. New wool block textures are 16x16.

Appearance references:

- [Flecked Sheep](https://minecraft.fandom.com/wiki/Minecraft_Earth:Flecked_Sheep): brown fleece, pale lower patches and a white head.
- [Fuzzy Sheep](https://minecraft.fandom.com/wiki/Minecraft_Earth:Fuzzy_Sheep): thick white fleece, dark brown face, black legs, pale hooves and a small tongue.
- [Inky Sheep](https://minecraft.fandom.com/wiki/Minecraft_Earth:Inky_Sheep): cream fleece, dark side stripes and hindquarters, with a light brown face.
- [Long Nose Sheep](https://minecraft.fandom.com/wiki/Minecraft_Earth:Long_Nose_Sheep): beige fleece, dark face and leg markings. Reborn uses an elongated Animania muzzle rather than importing the Earth model.
- [Patched Sheep](https://minecraft.fandom.com/wiki/Minecraft_Earth:Patched_Sheep): white fleece and legs with a black face, knee patches and hooves.
- [Rocky Sheep](https://minecraft.fandom.com/wiki/Minecraft_Earth:Rocky_Sheep): gray-and-pale mottling, dark facial patches and a pink nose. Animania's shared leg UVs make the leg markings symmetrical.

Fuzzy sheep yield 4–6 white wool; the other new breeds yield 1–3 wool. Flecked,
Inky, Long Nosed and Rocky have their own undyeable wool patterns; Fuzzy and
Patched retain white-wool dyeing. Patterned wool is included in wool tags and
has recipes for matching vanilla wool and patterned beds. Long Nosed uses prime mutton; other new breeds use ordinary
mutton. These yields, wool blocks and meat choices are Reborn adaptations.

Shearing hides the outer fleece and exposes a shorter coat. Regrowth, needs,
pregnancy and milk production follow the existing sheep rules. New breeds use
their own configurable habitats; the default spawn modifier retains total
weight 48. Existing entities are neither renamed nor converted.

### Ferret variants and small-animal reproduction

`ferret_cinnamon` and `ferret_sable` are additional ferret types with independent
spawn eggs. Existing gray and white ferret IDs remain unchanged. Small animals
retain their entity type while maturing; sex and age are saved on the animal,
rather than replacing it with another entity. A breed's spawn egg gives a
random-sex adult. Old animals without sex data receive a stable assignment from
their UUID, which is then saved explicitly.

Appearance references:

- [Cinnamon Ferret](https://minecraft.wiki/w/Dungeons:Cinnamon_Ferret): cinnamon coat, pale underside and paws, small ears and a black tail tip.
- [Sable Ferret](https://minecraft.wiki/w/Dungeons:Sable_Ferret): brown-and-tan coat, elongated body, small ears and a long tail. The pale muzzle and darker extremities are adapted to Animania's ferret mesh.

The new sheets are native 64x64. Original mesh dimensions and animation bones
remain intact; UV islands are separated without scaling their pixels. Each
coat has matching remapped blink masks. These are newly painted adaptations,
not imported Dungeons textures or models. Dungeons ferrets are cosmetic pets;
Reborn uses Animania's existing interactive ferret behavior.

Hamsters, both hedgehog coats and all four ferret coats share the new pregnancy
implementation. Mating requires opposite sexes within the same species, adult
age, appropriate care and the existing breeding settings. Sitting animals,
passengers and hamsters in balls cannot mate. Different ferret or hedgehog coats
can produce either parent's coat; hamsters inherit either parent's color.
Pregnancy saves the father's coat and uses the configured gestation and litter
rules. Offspring inherit the mother's owner when she is tame, record her UUID
and use care-dependent growth. Mother following accepts different coats within
the same species, but still requires that exact recorded mother.

Both mating and parent-follow goals are installed once for these animals and
check the current sex/age when running, so a male born in the world can breed
after maturing without a reload. Jade reports sex, fertility and pregnancy.
This reproductive system is a requested Reborn extension, not a claim that
Animania 1.12 or Minecraft Dungeons had these small-animal breeding mechanics.

The existing ferret spawn budget of 16 is shared across four coats. Cinnamon
uses savanna habitats and Sable forest habitats, configurable independently.
No existing animal is converted into either new coat.

### Patterned beds

Four beds (`bed_flecked`, `bed_inky`, `bed_tan`, `bed_rocky`) extend vanilla
BedBlock, preserving placement, paired halves, sleep, respawn, occupancy,
bouncing and invalid-dimension explosions. BedItem supplies the vanilla placement
flags. Head-only loot follows the vanilla bed table, avoiding duplicate drops.
The existing wool-to-bed recipes now produce the matching patterned beds;
wool conversion recipes still provide an indirect route to plain vanilla beds.

Ordinary block and item models use the existing 16x16 wool textures, oak frames
and white pillows. Their geometry matches the vanilla nine-pixel bed height.
The full two-block item model has explicit UVs bounded to each texture sprite;
it does not rely on UVs derived from translated geometry. No image resampling
or client-only block entity is required. All facings and both halves have models.

Both block and item bed tags include the additions. At server startup, after
registry snapshots are restored and before chunks load, the loader's state-to-POI
map associates only bed-head states with the existing HOME type. This preserves
vanilla villager home lookups without replacing that registry entry. The HOME
record's original matchingStates set is not mutated; integrations enumerating
that set directly will not see these beds, whereas normal POI state lookups do.

### Vanilla-inspired domestic cat coats

Eight additional CatBreed entries use separate tom, queen and kitten entity
IDs: `all_black`, `tuxedo`, `red_tabby`, `british_shorthair`, `calico`, `persian`,
`white` and `jellie`. These are domestic-cat breeds/coats, not new species.
Existing cats and saved IDs remain intact. Ragdoll, Siamese and brown Tabby
already have Animania counterparts and are not duplicated.

Appearance references are Java 1.20.1's bundled
`assets/minecraft/textures/entity/cat/{all_black,black,red,british_shorthair,
calico,persian,white,jellie}.png`. Vanilla `black` is the tuxedo coat; `all_black`
is the solid dark coat. The palettes preserve black cats' amber eyes, tuxedo
and red cats' green eyes, British Shorthair's amber eyes, Persian's blue eyes,
White and Calico's differently colored eyes, and Jellie's gray-and-white patches.
Coats are newly painted adaptations rather than copies of the vanilla UV sheet.

New models retain existing Animania meshes and animation bones: Tabby for six
coats, American Shorthair for British Shorthair and Exotic for Persian. The
Persian therefore uses Animania's rounded, short-faced silhouette rather than
claiming to reproduce vanilla's exact geometry. Original UV islands are moved
apart on native 128x128 sheets without resampling, allowing asymmetric calico
markings. Each coat has separately remapped, coat-colored eyelid masks so closed
eyes do not expose colored iris pixels.

The pet merchant offers the new cats at levels 2–3. Random cat eggs and the
existing configurable village-cat replacement pool include them automatically;
no additional wilderness spawns or conversion of saved cats are introduced.
Feeding, pregnancy, kitten growth, following and sitting retain the existing
cat implementation. Breed lookup selects the longest matching suffix so
`red_tabby` does not resolve to the older `tabby` entry.

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

References are the bundled Java 1.21.1 `wolf_variant` definitions and
`textures/entity/wolf/wolf_*.png` assets. New 128x128 coats and colored eyelid
masks use remapped native UV islands without scaling pixels. Model hierarchy,
pivots and animation keys remain those of `modelwolf`; sitting, sleeping,
walking and head movement therefore retain the original implementation.
`tools/paint-wolf-breeds.py` reproduces these assets.

New wolf placement uses vanilla `wolves_spawnable_on` and the usual daylight
check, allowing snow, podzol and coarse dirt as well as grass. Habitat tags are
shared between both versions, including the backported 1.20.1 breeds. Per-breed
biome-category filters are available as `wolfAshen`, `wolfBlack`, etc. Tags set
the available spawn biomes; these category options further restrict them.
The generic dog spawn probability and family limit still apply. Random dog
eggs include the additions. Breeding and offspring inheritance use the existing
dog/wolf system; foxes remain excluded from that breeding group.

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

The original EntityAnimaniaCat and EntityAnimaniaDog inherited EntityOcelot
and EntityWolf audio hooks. The port's TamableAnimal base supplied no equivalent
ambient/hurt/death sounds. Explicit overrides restore this missing audio path.
Contextual purring and fox-specific calls are Reborn additions. Death aliases
for ferrets and hedgehogs deliberately reuse their existing hurt clip pools;
they are not presented as newly recorded death sounds.

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

### Egg tooltip and repeated milking corrections

Jade now sends a separate EggTimerActive flag for adult hens. The vanilla
EggLayTime value is only shown when loose egg drops are enabled and the hen
can run that timer; the disabled-mode 999-tick placeholder is not a countdown.
Nest-only hens show "Nest laying: preparing", then "Looking for a nest" when
ready. Peahens retain their real LaidTimer countdown. No laying timers or nest
behavior are changed by this display correction.

Cow, goat and sheep milking no longer clears HasKids/milkable. The 1.12 cow,
doe and ewe implementations consume the watered flag when milked, retaining
the maternal state. Repeated milking during that state is available after care
requirements are met again. Birth, natural families, spawn-milking configuration
and child-maturity handling remain as before; this is not a new simulation of
real-world lactation length. Previously saved false HasKids values cannot be
reliably distinguished from animals that never entered lactation and are not
automatically changed.

### Species behavior: first batch

Livestock, domestic cats/dogs, rabbits and small mammals no longer keep an
exclusive mate. Wolves and foxes retain social pair bonds. The legacy
malesMateMultipleFemales setting is retained for config-file compatibility but
no longer determines these rules. Existing non-pairing mate references are
ignored immediately and cleared during normal updates. Pregnancy inheritance,
maternal UUIDs, breed compatibility, feeding requirements and population limits
remain in place. Horse mate-following no longer runs without a pair bond.
A social pair here remains a gameplay approximation, not a claim of lifelong
or genetically exclusive mating in every wild canid population.

Chickens scratch and peck on grass/dirt and take dust baths on exposed dirt or
sand in dry weather. Completing a foraging bout supplies food. At night they
prefer reachable raised log/plank roosts, falling back to configured bedding.
The perch search does not teleport birds or give them new flight abilities.

Fed adult cattle and sheep can settle with open eyes and chew cud, then rise.
Adults farther than eight blocks from their nearest visible herd neighbour
can rejoin it, stopping about four blocks away. Young animals keep their
existing maternal-following behavior. Grazing animation now lasts for the
whole eating action.

Pig snuffling keeps its existing food/truffle rules and animates throughout.
Mud bathing is a timed activity with approach, lowering, body/leg movement
and rising, rather than an automatic pose whenever standing on mud. Completing
a bath refreshes play needs; residual mud no longer refreshes those needs
indefinitely. Rain/water still removes the mud coating.

The new daytime activities yield to injury, threats, food lures and sleep;
searches are staggered, travel attempts expire, and finished/interrupted
activities have a cooldown. Animation state is transient and synchronized.
Jade names the current activity. Durations are game pacing choices: five
seconds for foraging, eight for dust bathing, twenty for resting/rumination,
and eleven for a mud bath, with one-to-two-minute cooldowns.

Sources:
- https://poultry.extension.org/articles/poultry-behavior/normal-behaviors-of-chickens-in-small-and-backyard-poultry-flocks/
- https://www.merckvetmanual.com/behavior/behavior-of-production-animals/behavior-of-cattle
- https://www.merckvetmanual.com/behavior/behavior-of-production-animals/behavior-of-swine
- https://animaldiversity.org/accounts/Bos_taurus/
- https://animaldiversity.org/accounts/Ovis_aries/
- https://animaldiversity.org/accounts/Capra_hircus/
- https://animaldiversity.org/accounts/Equus_caballus/
- https://animaldiversity.org/accounts/Felis_catus/

This is a Reborn behavior revision, not an unchanged 1.12 port. Seasonal
breeding, temperature physiology and the remaining species' daily routines
are not included in this batch. In-game pathfinding and animation still need
player testing on both loaders.

### Species behavior: domestic goats and draft horses

Domestic goats can browse accessible low oak, birch and acacia leaves when
hungry. The search checks head clearance, height and a reachable adjacent
standing position; feeding occurs after a five-second browsing action. Leaf
removal respects plantsRemovedAfterEating and mobGriefing. Other foliage,
including cherry leaves, is not automatically classified as edible.

Goats also explore raised, navigable platforms and terrain one to three blocks
above them. Searches are staggered and bounded; routes with successive height
changes exceeding one block are rejected. Normal pathfinding remains responsible
for traversing the route. This is not a climbing-wall ability or a new jump.
Adult goats can regroup and rest while chewing cud. Young animals keep maternal
following priority. MountainGoat's separate native brain is unchanged.

Adult males can engage in short reciprocal sparring bouts, with restrained
head movement and a one-to-two-minute cooldown. Each participant owns its own
navigation instead of one animal continually commanding both. Bouts do not
assign combat targets or deal damage, and yield to injury, sleep, hunger, thirst,
food lures and handling. The animalsCanAttackOthers switch still controls them.

Draft horses can regroup with other adults and rest standing during the day.
A relaxed head and slightly flexed hind leg distinguish this from the existing
nighttime lying sleep. These are short game-paced rests, not a simulation of
sleep stages or deprivation.

An unfamiliar survival-mode player sprinting toward a horse within eight blocks
can trigger a brief alert. The horse first watches; continued close approach
within four blocks can cause a short evasive movement. Its owner, creative and
spectator players do not trigger this response. Riding, leashing and vehicle
pulling suppress the new horse activities. No persistent familiarity score or
herd-wide fear propagation is introduced.

Jade names the five new activity states. Both loaders use the same gameplay
rules and animation equations. In-game pathfinding and model appearance require
player testing; compilation alone does not establish those visual outcomes.

Sources:
- https://www.merckvetmanual.com/behavior/behavior-of-production-animals/behavior-of-goats
- https://www.merckvetmanual.com/management-and-nutrition/nutrition-goats/herbage-and-browse-usage-in-goats
- https://www.merckvetmanual.com/behavior/behavior-of-horses/behavior-problems-of-horses

### Species behavior: domestic cats and dogs

Domestic cats can groom, explore nearby reachable ground and pause to observe,
or settle in a spot no brighter than their starting position. Grooming blends
the original sitting pose with a raised foreleg and restrained head movement.
Quiet rest blends the original lying pose without setting the sleeping flag.
The models retain their existing UVs, geometry and ear shapes.

Domestic dogs can approach a nearby, non-sprinting owner, pause beside them
with gentle tail/head movement, or investigate a nearby location by sniffing.
Sniffing is an exploratory action; no scent-map simulation, tracking reward,
food generation or new recording is claimed. Owners retain sit/follow control.
The new domestic routines do not apply to wolves, foxes or ocelots.

The routines are short and have individual one-to-two-minute cooldowns.
Paths are checked before use; travel expires after eight seconds. Hunger,
thirst, injury, combat, rain, sleep, leashes, carrying, sit commands, food lures
and an owner moving more than five blocks away stop them. Normal following and
existing teleport configuration take precedence.

Existing kitten/puppy chase play now has a maximum ten-second bout and a
thirty-to-sixty-second cooldown instead of a ten-percent stop roll every tick.
Both partners must remain able to play; domestic dogs, wolves and foxes are
separate play groups, as are domestic cats and ocelots. The existing play-goal
lookup now uses weak values to avoid retaining unloaded animal entities.

Jade identifies the five new routines. Both loader builds share the same rules.
This batch does not add toys, fetch, scratching furniture, persistent scent
trails or a new cat sleep schedule. Actual animations and pathfinding require
in-game observation.

Sources:
- https://www.merckvetmanual.com/behavior/behavior-of-cats/social-behavior-of-cats
- https://www.merckvetmanual.com/cat-owners/behavior-of-cats/behavior-problems-in-cats
- https://www.rspca.org.uk/whatwedo/latest/essays/followingdogs

### Species behavior: rabbits and small mammals

Rabbits, hamsters, hedgehogs and ferrets can investigate nearby reachable
positions and seek sheltered space during rest periods, rain, or a nearby
unfamiliar sprinting player's disturbance. A shelter needs collision-free room,
solid footing and cover within three blocks overhead. Searches and path attempts
are bounded, and travel expires. Sheltered animals can enter normal sleep when
it is their rest period and sleep is enabled. If no shelter is reachable, no
burrow is created and the animal is not teleported.

Rabbit rest windows now avoid dawn and dusk (rest at world times 2000-10000
and 17000-20000). Hamsters and hedgehogs retain their daytime-rest direction.
Ferrets have staggered 6000-tick cycles with 4500 ticks designated for rest;
the phase derives from their UUID and is stable across reloads. These windows
are game approximations, not biological clocks or guaranteed sleep durations.
They do not bypass the global sleep switch for starting shelter sleep.

Rabbits and hamsters can briefly scratch soft ground with their forelegs and
small block particles. This is enrichment, with no terrain destruction or item
production. Food requirements, grazing, hamster cheek pouches, wheels, balls,
carrying and maternal following remain separate. A hamster does not randomly
stand up in the middle of a new activity. The new goals yield to handling,
injury, water, food lures, feeding and owner/maternal following as appropriate.

Jade identifies investigating, taking shelter and digging. No new defensive
ball model, burrow block, scent system or recording is included. Animations and
paths still require in-game observation on both loaders.

Sources:
- https://www.rspca.org.uk/adviceandwelfare/pets/rabbits
- https://www.rspca.org.uk/adviceandwelfare/pets/rabbits/behaviour/enrichment
- https://www.rspca.org.uk/adviceandwelfare/pets/rodents/hamsters/behaviour
- https://www.rspca.org.uk/adviceandwelfare/pets/rodents/hamsters/environment
- https://www.rspca.org.uk/adviceandwelfare/pets/ferrets/environment

### Species behavior: bird feather care and roosting

Adult chickens and peafowl can pause to preen during the day. The seven-second
animation turns the neck toward one wing, with small repeated head/wing movement;
male peafowl fold their train during preening and dust bathing. Peafowl now use
the existing eight-second dust-bath activity on dry dirt or sand. These activities
use existing models and have individual one-to-two-minute cooldowns. They require
food and water and yield to nest seeking, food lures, injury, threats and sleep.
There is no parasite simulation or new feather reward.

Peafowl join chickens in preferring an unoccupied raised log/plank perch at night.
Normal navigation must get them there; no flight or teleport was added. Their
configured bedding remains the fallback. Bird sleep destinations require solid
support and collision-free space for the full body. Birds must reach the perch
before sleeping, instead of using the generic search goal's wider arrival radius;
a travel attempt expires after 240 ticks. Idle wandering and watching no longer
outrank peafowl sleep or feather care. Jade identifies preening.

These are Reborn behavior additions rather than an unchanged 1.12 port. Timings
are game pacing choices. Both loaders require in-game animation/path observation.

Sources:
- https://animaldiversity.org/accounts/Pavo_cristatus/
- https://poultry.caes.uga.edu/content/dam/caes-subsite/poultry/documents/archived-poultry-tips/behavior-of-backyard-flock-part2-mar-08.pdf

### Species behavior: wolves, legacy foxes and ocelots

Untamed adult wolves can rejoin known adult family members within sixteen blocks
when separated by more than eight blocks, stopping within four. Relationships use
existing reciprocal mate IDs, recorded mothers or a shared recorded mother. This
does not assign unrelated wolves to a pack or reconstruct missing parent records.
A stable UUID ordering avoids paired adults and siblings following each other in
circles; adult offspring may follow their recorded mother. Searches are staggered,
try at most four paths and expire after ten seconds. Hunger, thirst, threats,
handling, sleep and food lures take priority. Tamed wolves retain owner following
instead of joining these family movements. Jade names the regrouping activity.

Legacy foxes and ocelots now have a daytime rest window at world times 2000-10000,
leaving dawn, dusk and night available for activity. They can seek reachable covered
space using the shelter routine; global sleep settings still govern shelter sleep.
Their young can follow their mother during the revised active hours. Fed animals
pause proactive target acquisition during the rest window; hungry animals can
still seek prey. This does not replace owner-defense or retaliation goals.

Legacy foxes and wolves can pause to sniff nearby reachable ground. Ocelots can
groom and explore independently using the existing feline animations, without
adding a grouping goal. All retain their species, breeds, textures and models.
ModernFox keeps its native fox goals and is not given a second sleep schedule.
The shelter routine now checks the animal's actual standing position before
starting, not only the nearby destination, to avoid sleeping just outside cover.

These are game-paced behavior revisions, not a complete simulation of territories,
dispersal, hunting cooperation or scent tracking. The existing wolf sleep schedule
is unchanged. Both loaders still need in-game observation.

Sources:
- https://animaldiversity.org/accounts/Canis_lupus/
- https://animaldiversity.org/accounts/Vulpes_vulpes/
- https://animaldiversity.org/accounts/Leopardus_pardalis/

### Species behavior: bee colony foraging and return routes

Modern bees retain vanilla pollination, crop fertilization, flight and anger.
Returning to an Animania hive now checks capacity, loaded chunks, entrance fluid,
full-body collision clearance and nearby fire before choosing a route. Candidate
hives are distance-sorted with at most four new route attempts per search. A failed
route or interrupted blocked entrance is avoided for thirty seconds; travel still
expires after thirty seconds. Searches are staggered. Night or rain can bypass
the normal twenty-second post-exit delay, while anger, mating and handling prevent
routine return. A burning hive is not selected again during an emergency exit.

A colony remembers up to four flower positions supplied by successful nectar-bearing
returnees, within thirty-two blocks of the hive. On ordinary release, a bee whose
own remembered flower is no longer valid may receive a valid colony flower. Native
pollination still finds and collects the nectar; no nectar, crop growth or honey
is awarded just for sharing a location. Records persist with the hive, expire after
24000 world ticks and are removed when a loaded flower disappears. Unloaded flower
chunks are not force-loaded or offered to departing bees. Existing valid personal
flower memories are kept. This abstracts forage recruitment, without a waggle-dance
animation or a simulation of flower quality and nectar depletion.

Jade shows returning bees and the hive's currently loaded valid flower-site count.
Existing legacy passive honey production, colony size, breeding, models, sound and
emergency release rules are retained. No new hunger system is added to bees.
Both loader builds require in-game route and hive observation.

Sources:
- https://content.ces.ncsu.edu/honey-bee-dance-language
- https://extension.umaine.edu/ipm/background-honeybee-flight-activity-index/

### Species behavior: mountain goats

Mountain goats now have daytime rumination, raised-ground exploration and female
regrouping, registered through the existing native-brain/care-goal bridge. Care
goals wait for native long jumps and rams to finish before starting. Native jump,
ram, horn and breeding mechanics remain in place.

Only adult females use the new regrouping routine, approaching another visible
adult female when separated by more than eight blocks and stopping within four.
Adult males are not pulled into this routine; young animals retain recorded-mother
following. Mountain-goat regrouping requires food and water, rejects routes with
successive height differences greater than one block and ends after ten seconds.
This is a local grouping rule, not a simulation of seasonal herds or migration.

Raised-ground exploration uses the existing bounded goat search: candidates one
to three blocks higher, at most four paths per search, ordinary walkable routes
and a nine-second travel limit. It does not add climbing walls, teleportation or
new jumps. Babies do not run this exploration routine.

Fed and watered adults can rest and chew cud on dry solid ground, including rock.
The twenty-second action blends into the existing tucked-leg resting pose while
keeping the animal awake, with small head and muzzle movement. It yields to food
lures, injury, threats and night sleep, and has a one-to-two-minute cooldown. Jade
uses the existing rumination and exploration labels. No geometry or texture was
replaced. Both loader builds still need in-game animation and terrain observation.

Sources:
- https://www.adfg.alaska.gov/index.cfm?adfg=goatidentification.behaviorsex
- https://animaldiversity.org/accounts/Oreamnos_americanus/

### Species behavior: aquatic axolotl shelter

Fed, watered axolotls can take a short daytime rest on a nearby submerged bottom
with low light or overhead cover. Candidate space must fit the whole body in water
and be collision-free, with solid footing below. Up to 48 nearby candidates and
four paths are considered per staggered search; accepted path nodes must remain
submerged. No chunks are force-loaded, no water is created and no animal is
teleported. Travel expires after eight seconds; resting lasts ten seconds, with
one-to-two-minute cooldowns.

The routine yields to injury, recent attacks, playing dead, breeding, food lures,
handling, hunger, thirst, loss of water or loss of shelter. It does not set the
land-animal sleeping flag or close the eyes. Native underwater idle animation is
retained. Jade identifies underwater rest. Native movement behaviors are stopped
before installing the shelter path and resume after the goal ends, using the
existing care/native-brain bridge. Vanilla hunting, playing dead, bucket handling,
water seeking and breeding remain in place outside the short care activity.

This is a game-paced shelter preference, not a forced all-day sleep schedule or a
water-temperature simulation. Frogs, toads and dart frogs are not assigned this
aquatic routine. Both loader builds still need in-game path and behavior observation.

Source:
- https://animaldiversity.org/accounts/Ambystoma_mexicanum/

### Species behavior: frog, toad and dart-frog habitat visits

Ordinary frogs can make short daytime visits to solid bank positions immediately
beside water. Toads seek overhead cover during dry daytime periods; dart frogs seek
cover at night. These are local habitat preferences, not a complete sleep or
hydration simulation. The generic frog/toad registrations do not identify exact
biological species, so the category-level choices are intentionally limited.

Destinations require ground support, room for the body and empty fluid at the feet.
Lava/magma and campfire resting sites are rejected. Searches sample forty positions
and try at most four paths; routes crossing water or successive height differences
over one block are rejected. Travel lasts at most nine seconds, visits ten seconds,
with thirty-to-sixty-second cooldowns. No blocks, burrows or water are created.

Panic and avoidance take priority over the new visits; idle wandering and looking
are below them. Taking cover remains interruptible and never sets the mammalian
sleeping flag. At rest the original grounded model pose is retained and pending
legacy hops are cancelled, rather than layering on a new sleeping model. Jade
identifies bank visits or shelter. Poison-arrow interaction, skins, lack of a
breeding lifecycle and the Pepe goals are retained; Pepe clears the transient
habitat state when it replaces ordinary goals.

Both loader builds still require in-game navigation and interruption observation.

Sources:
- https://nationalzoo.si.edu/animals/poison-frogs
- https://animaldiversity.org/accounts/Anaxyrus_americanus/


## 繁殖与育幼（保留现有出生方式）

- 牛、猪、羊、山羊、挽马、雪羊：接近后持续求偶；孕后恢复期间不能再次受孕。母兽会主动靠近缺食或缺水的未断奶幼崽，接触一段时间后完成哺乳。
- 猫、豹猫、狗、狼、狐狸、兔、仓鼠、刺猬、雪貂：接入相同的亲子身份和护理流程。早期幼崽减少自行游荡，母亲主动回访；兔子的护理间隔比其他哺乳动物长。狼和狐狸的父亲可回访、与幼崽团聚，但不能哺乳。
- 鸡、孔雀：求偶不直接生成幼鸟，仍通过鸟巢孵化。每枚蛋保存母鸟和已知父鸟信息，孵出的幼鸟跟随记录中的母亲；母鸟会回访自己的巢。已记录受精信息的蛋不再要求雄鸟一直待在巢边。旧蛋和玩家放入的蛋保留原来的附近雄鸟孵化规则。
- 蜜蜂：保留原版繁殖方式。幼蜂继承已知的动物谷蜂箱位置，也可寻找有空位的蜂箱；入巢后正常成长至成年再出巢。火灾、破坏蜂箱等紧急释放仍有效。
- 美西螈：保留已有求偶、繁殖冷却和独立幼体行为，不添加哺乳或跟随母亲。
- 青蛙、蟾蜍、箭毒蛙：保留当前出生方式。本次不新增此前不存在的产卵、蝌蚪、背负幼体流程，也不套用哺乳动物育幼 AI。

断奶按现有成长进度计算：挽马为第 65 阶段，牛、羊、山羊和雪羊为第 55 阶段，其余哺乳动物为第 40 阶段；成年仍为第 85 阶段。这些是游戏时间尺度，不代表现实中的天数。孕后恢复为牛、挽马 6000 tick，猪及巢居幼体哺乳动物 4000 tick，其他哺乳动物 3000 tick，原有繁殖限制仍叠加生效。

母兽必须吃饱、喝足且能够接近幼崽才能护理；每次护理需要持续接触 60 tick。寻亲最多搜索附近 16 格，移动最多持续 240 tick，受伤、战斗、睡眠、拴绳和玩家坐下命令可中断。不会强制加载亲属所在区块，也不会因玩家经过就发动攻击。

父母身份、恢复时间和未断奶幼崽名单保存到存档。断奶或死亡造成的家庭关系变化通过存档消息保留，相关亲属重新加载后再处理。多胎母兽不会因第一只幼崽断奶就立即停止泌乳。Jade 显示孕后恢复剩余秒数及幼崽尚未断奶状态。

这些改动扩展了原版育幼行为，不声称复刻真实动物的完整生命周期。护理动作见下方的繁殖与育幼动画说明。


## 繁殖与育幼动画

求偶进入近距离阶段后，雄性抬头并缓慢点头；鸟类小幅展翅，哺乳动物摆尾。走向配偶的途中保持正常步行动画。

母兽接触幼崽时低头轻触，巢居幼体的母兽和母猪增加轻微屈腿、降低重心的姿势；牛、羊、山羊、雪羊和马保持站立护理。幼崽在吸吮阶段抬头并做小幅口鼻动作，短暂停止游荡。父兽团聚和鸟类带雏使用轻触动作，不播放哺乳动作。

母鸟到达自己的巢后降低身体、屈腿并收翅；孔雀同时收拢尾羽。离开巢位后恢复步行姿态。蜜蜂在外界沿用原有飞行和入巢表现，存入蜂箱期间不渲染实体。

动作以服务器行为阶段为准，使用不写入存档的同步状态；开始和结束各用约 8 tick 过渡。护理或求偶停止后清除动作，状态另有短超时，防止亲属卸载后姿势残留。睡眠、受伤和着火优先于这些动作。新狐狸和雪羊也接入相同的阶段动画，仓鼠独立的鼻子、耳朵随头部一起变换。

这是在现有模型骨骼上新增的程序动画，不是对原 1.12 动画的逐帧复制。未更换模型和贴图；版本维持 0.1.5。


## 饲养心情

新增基于物种的饲养心情，细则见 HUSBANDRY.md。状态保存到存档，未确认被饲养的野生动物不受影响。手喂、食槽/食盆、驯服、拴绳、人工巢/蜂箱等可建立饲养状态；小型封闭围栏使用有界连通区域判断。安置期默认 72000 个世界日历 tick（3 个游戏天，睡觉跳过的夜晚计入），心情按 200 tick 分散评估，不强制加载区块。

满足状态提高适用的成长、产蛋、羽毛、长毛、繁殖恢复效率；不满降低效率和接受治疗量，低于 20 暂停新的受孕。没有增加伤害或流产判定。牛、家养山羊、羊的挤奶和蜜蜂交蜜也有对应效果。中性状态不改变原生产节奏，原蜂箱被动产蜜不受个体心情影响。蛙类保留原来的无繁殖流程，只参与适用的健康/毒液恢复效果。

Jade 显示心情、效果和具体缺失条件。新增 animalMood、moodPenalties、moodGraceTicks 配置。饥饿/口渴黑名单继续生效，同时修正了扩展狼品种未被 wolves 分组匹配的问题。Forge 的新狐狸、雪羊、美西螈和蜜蜂补接统一 tick 入口，确保心情及现有护理计时实际执行。

安置期改为保存世界日历采样时间，区块卸载期间的日历进度也计入；旧存档沿用剩余时间，时间倒退不增加倒计时。五份配置改为 COMMON，在实例 config 下跨存档共用，保留原文件名。旧存档配置不自动合并，以免不同世界的设置互相覆盖；迁移说明见 HUSBANDRY.md。

猫梳毛改为使用原模型的上臂/前臂父子关节，先坐下，再屈肘抬爪、低头舔爪，最后落爪起身。单独适配豹猫 leg_l21 的前臂节点，避免误转对侧肩部。沿用原模型几何和 UV，普通与新增毛色品种共用动作；运行时效果待游戏内确认。
