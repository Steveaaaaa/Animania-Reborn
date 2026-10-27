# Husbandry mood

Mood applies to animals under player care. Hand feeding, food or water from a trough or pet bowl, taming, leashing, a bucket-released axolotl, and use of an Animania nest or hive establish care. Small enclosed pens can also establish care automatically. Merely inspecting an animal does not.

Untamed animals lose this status after 48,000 loaded ticks without care or a detected enclosure. Tamed animals stay under care. A new animal starts at 50 mood and gets 72,000 world calendar ticks (three Minecraft days) to settle in. Offspring inherit care status, and saved mood survives adulthood conversion and axolotl buckets.

## What to provide

Food and water matter wherever those needs already exist. Every species also needs room and freedom from recent injury, fire or fighting. Breeds of the same species use the same requirements.

| Species | Additional needs |
| --- | --- |
| Cattle and sheep | Same-species company, grass or hay, a sheltered resting place |
| Pigs | Company, soil/sand/hay for rooting, mud or recently completed play, shelter |
| Domestic goats | Company, leaves for browsing, climbing platforms or logs, shelter |
| Mountain goats | Company, climbing opportunities, shelter |
| Draft horses | Company, grass or hay, room to move, shelter |
| Chickens and peafowl | Company, dry soil or sand for dust bathing, wooden perches, shelter |
| Rabbits | Company, diggable substrate, a hiding place |
| Cats | A cat tower or logs for scratching, climbing opportunities, a hiding/resting place |
| Ocelots | Climbing opportunities and cover |
| Dogs | Another dog or their nearby owner, shelter |
| Wolves | Other wolves and shelter |
| Foxes | Diggable substrate and cover |
| Hamsters | Diggable bedding, a hide, a wheel, personal space away from other adults |
| Hedgehogs | Diggable substrate, cover, personal space away from other adults |
| Ferrets | Company, substrate to explore, a hide |
| Bees | A home hive, nearby flowers and water |
| Axolotls | Water with room to swim and cover |
| Frogs | Water and cover, with access to land |
| Toads | Diggable ground and cover |
| Dart frogs | Water, vegetation and cover |

Young animals are exempt from the solitary-adult requirement. A mother is not counted as an unwanted neighbour of her own young. Different wolf breeds count as wolves; dogs do not fulfil the wolf-company requirement.

## Effects

Mood is assessed every ten seconds and changes gradually: at most +3 or -2 per assessment. All requirements met gives a target of 100; one missing requirement gives 65; two gives 40; three or more gives 10. Missing food, water, space, safety or required aquatic habitat limits the target to 30.

- **75–100, content:** applicable growth, egg/feather production, wool regrowth and breeding-recovery clocks run 25% faster. Fully met needs also restore one health point roughly once per minute. Content cows, domestic goats and sheep have a 25% chance not to lose their watered state after milking. Nectar delivered by a content bee produces 25% more honey, rounded down to whole millibuckets.
- **35–74, settled:** normal rates.
- **0–34, unhappy:** applicable clocks run at half speed and received healing is halved. Milking cows, domestic goats and sheep adds a 60-second rest before the next milking. Nectar deliveries produce half the normal honey, with a minimum of one millibucket.
- **Below 20:** starting a new breeding attempt is blocked. Existing pregnancies continue normally.

These are husbandry effects shown in Jade, not potion effects that can be removed with milk. Effects apply only to systems a species actually has: frogs and toads do not gain a new breeding lifecycle, and bees stored inside a hive do not continuously run the outdoor mood assessment. Passive legacy hive production is unchanged.

## Reading the enclosure

The game samples nearby connected walkable cells, up to 192 cells within eight horizontal blocks and three blocks of height difference. Water cells are used for axolotls. Resources must border this sampled area; a remote block elsewhere on the farm does not count. The sample is a game approximation, not a certified welfare or enclosure-size assessment.

Pens must be closed within the sample and include recognisable built barriers such as fences, walls, planks, glass or brickwork. Very large paddocks may not be detected as pens; feeding or taming still establishes care there. Unmanaged animals are sampled only once per minute. No chunks are force-loaded, and incomplete samples do not change mood.

Bees use the area around their loaded home hive to check flowers and water, so flying away to forage does not itself make them unhappy. The entrance must be clear of collision blocks and fluid; existing bee navigation still controls the full flight path and entry.

Jade shows the score, active effects, settling-in time and unmet requirements. Hunger/thirst exclusions remain respected. Ambient mode disables mood effects.

## Configuration

In `config/animania-modern-server.toml`, under `[husbandry]`:

```toml
animalMood = true
moodPenalties = true
moodGraceTicks = 72000
```

Set `animalMood` to false to disable the system, or `moodPenalties` to false to retain rewards without penalties. Neutral mood retains the pre-existing production rules. Existing configurations keep their saved value; set `moodGraceTicks` to `72000` to use the new default. This setting applies when an animal enters care, without resetting an existing settling-in countdown. Sleeping through the night counts toward settling in, as does time passing while the animal is unloaded. Pausing the daylight cycle pauses this countdown; moving the calendar backward does not add remaining time. Existing animals keep their remaining countdown when first loaded after this update.

## References and game adaptation

The needs draw on [RSPCA pig welfare](https://www.rspca.org.uk/adviceandwelfare/farm/pigs), [hen welfare](https://www.rspca.org.uk/adviceandwelfare/farm/layinghens), [rabbit housing](https://www.rspca.org.uk/adviceandwelfare/pets/rabbits/indoors), [hamster behaviour](https://www.rspca.org.uk/adviceandwelfare/pets/rodents/hamsters/behaviour), and [cat environments](https://science.rspca.org.uk/en/web/rspca/adviceandwelfare/pets/cats/environment/indoors); the Merck Veterinary Manual's guidance on [goat behaviour](https://www.merckvetmanual.com/behavior/behavior-of-production-animals/behavior-of-goats), [horse behaviour](https://www.merckvetmanual.com/horse-owners/behavior-of-horses/behavior-problems-in-horses), and [ferret housing](https://www.merckvetmanual.com/all-other-pets/ferrets/providing-a-home-for-a-ferret); and [Oregon State Extension's explanation of bees' water use](https://ask.extension.org/kb/faq.php?id=465969).

The numeric thresholds, block substitutions, mood scale and bonuses are gameplay choices. The system does not simulate temperature, ventilation, water chemistry, disease, seasonal breeding or every individual's social compatibility.

## Shared configuration

All five Animania TOML files now load from the instance or dedicated server `config` folder and apply across its worlds. Filenames keep their `-server.toml` suffix for continuity. World `serverconfig` files are no longer read. To keep previous settings, close the game/server and copy the five Animania TOML files from your chosen world into `config`, backing up any existing files first. No world is selected automatically, since worlds may have conflicting settings. Restart after editing. In multiplayer, husbandry simulation uses the server configuration; COMMON configs are not automatically synchronized to clients.


## Facility use and familiar companions

`facilityInteractions` and `animalSocialBehavior` in `animania-modern-server.toml` independently enable these additions. They do not change mating bonds, hunger or production rates.

| Animals | Facilities and social behavior |
| --- | --- |
| Cattle | Rub against a two-block log post; approach familiar cattle for social grooming. Herd regrouping prefers familiar animals. |
| Horses | Keep existing grazing, bedding and standing rest; approach familiar horses for grooming and prefer them when regrouping. No interaction while ridden or pulling. |
| Sheep and domestic goats | Keep their grazing/browsing, bedding and climbing routines; rest near familiar animals and prefer them when regrouping. |
| Mountain goats | Female groups gain familiar-neighbor resting and regrouping. Adult males are not assigned female-herd friendships. |
| Pigs | Root in dirt, coarse dirt or rooted dirt, with snout movement and soil particles; investigate familiar pigs. Existing mud bathing remains separate. |
| Chickens and peafowl | Keep dust bathing, preening and night roosting. Familiar adult females can rest together; existing male rivalry is not replaced. |
| Rabbits | Seek reachable dirt/sand digging patches; approach familiar rabbits for grooming. Existing hiding and parent-following remain. |
| Domestic cats | Scratch logs/cat towers using shoulder and elbow joints, visit reachable elevated log/tower tops, and rest on cat beds. Social grooming requires longer familiarity and is optional, not a need for a cat group. |
| Domestic dogs | Rest on dog pillows and greet/sniff familiar dogs. Owner commands and following take priority. |
| Ferrets | Visit dry spaces below wooden or hay cover, and investigate familiar ferrets. Existing short activity periods and shelter seeking remain. |
| Wolves and foxes | Keep their recorded-family behavior. They do not acquire unrelated dog/herd friendships. |
| Ocelots, hamsters and hedgehogs | Keep their species-specific exploration, cover and existing wheel interaction; no companion-grooming or herd goal is added. |
| Bees, axolotls and amphibians | Keep hive/flower, underwater-shelter and shore/cover routines respectively; no mammal friendship or grooming system is applied. |

Familiarity grows through calm, visible proximity, with at most four saved contacts. Shared breeds can become familiar within a species; pets with different owners cannot. Contact requires reciprocal familiarity: roughly four minutes nearby, or eight for cats, in loaded game time. These are gameplay thresholds, not biological bonding times. Contacts expire after seven game days without encounters; newcomers do not immediately displace recently seen companions. The recipient remains free to leave, and social contact ends if it moves away. Familiarity never claims to guarantee compatibility or remove existing aggression.

Facility searches stay within five blocks horizontally and try at most four paths per attempt. Furniture must be reachable and unoccupied; tower tops need a climbable route such as nearby steps. There is no teleportation into furniture, block destruction, digging of new tunnels, or generation of food. Injury, combat, hunger, thirst, sleep, nursing, leads and player commands interrupt these optional activities. Jade shows each active behavior.

This update adds procedure-driven poses to existing models. Both loader builds compile; pathfinding, furniture alignment and social contact still need in-game observation.

Behavior references:
- [Cattle social licking](https://www.merckvetmanual.com/behavior/behavior-of-production-animals/behavior-of-cattle)
- [Horse grooming and grazing partners](https://www.merckvetmanual.com/horse-owners/behavior-of-horses/behavior-problems-in-horses)
- [Cat scratching and enrichment](https://www.rspca.org.uk/adviceandwelfare/pets/cats/behaviour)
- [Rabbit companionship](https://www.rspca.org.uk/adviceandwelfare/pets/rabbits/company)
- [Pig rooting and social behavior](https://www.merckvetmanual.com/behavior/behavior-of-production-animals/behavior-of-swine)
- [Ferret cover and tunnels](https://www.rspca.org.uk/adviceandwelfare/pets/ferrets/environment)


## Beekeeping and defensive swarms

Occupied Animania hives and wild hives can release a defensive swarm. Harvesting honey by hand gives a warning (three seconds by default); retreating more than six blocks during that warning ends it. Breaking the hive skips the warning. A brief emergence precedes pursuit. The swarm follows the responsible player for at most 20 seconds and within 16 blocks of its home. It does not select nearby bystanders or livestock. Animal retaliation is optional and only applies to an animal that directly attacks the swarm.

The swarm temporarily holds the hive's saved adult residents. Juveniles and bees that have already stung do not join. There is only one swarm entity per colony; small bees in its renderer are a visual representation of colony strength, not independently spawned mobs. The hive reserves space for returning residents. A destroyed or replaced hive causes the residents to disperse as ordinary bees. Smoke, retreat, submersion or expiry ends pursuit. Saved swarm and colony records retain ownership across normal unloads; an expired reservation can free capacity without copying the bees.

Craft a bee smoker from iron, charcoal and leather. Right-click a hive or aim smoke at a swarm. The smoker starts with eight puffs and consumes one charcoal from your inventory when refilled. Smoke lasts 30 seconds by default and does not pass through walls. Campfire smoke also suppresses hive defense. Automation has no responsible player and does not provoke a swarm. Fire and non-player destruction evacuate residents without selecting an arbitrary player to attack.

A beekeeper veil reduces swarm damage by 40%; the jacket, trousers and boots each reduce it by 20%. A full intact set blocks swarm stings, but each attempt wears the clothing. Leather repairs it. This is swarm-specific protection, not immunity to all bee mobs or other attacks. The item sprites are 16×16; worn clothing uses the standard 64×32 armor UV layout.

Jade displays smoke and residents away on defense. The in-game manual includes instructions and refreshes older copies when opened. Settings are in the common configuration's `beekeeping` section: defense, warning/pursuit/smoke duration, pursuit radius, damage, clothing protection, animal retaliation and per-breed defensiveness. Amber, dark and pale are coat-based game breeds, so all use neutral default temperament; color alone is not treated as evidence of a real subspecies' aggression. The former random proximity damage from wild hives has been removed.

Smoke and protective equipment are inspired by ordinary beekeeping practice; durations, damage and clothing percentages are gameplay values. References: [University of Minnesota Bee Lab](https://beelab.umn.edu/videos), [NC State: honey bee stocks](https://content.ces.ncsu.edu/the-different-types-of-honey-bees). The swarm feature was suggested by players familiar with [Angry Bees](https://www.curseforge.com/minecraft/mc-mods/angry-bees); no code or assets from that mod are included.

## Pet merchants and village stalls

Villages in plains, savanna, desert, taiga and snowy biomes can contain a pet stall: an open-front counter with a wool awning, a pet bowl behind it and a pet merchant at the counter. The stall occupies a village house slot, so not every village has one.

The merchant sells Animania kittens and puppies. Its stock is drawn the first time you open the trade screen and then follows ordinary villager rules: a fresh merchant shows two offers, and trading with it, or letting it work at its own bowl, raises its level and unlocks the rest. Vanilla villagers keep their profession when their workstation is removed, so breaking the pet bowl costs the merchant its job site but not its trades.

Handing a tamed pet to another player is not supported yet. A pet records the owner it was tamed by, and that record is what makes it refuse food and breeding interaction from everyone else, so feeding a cat to a friend's account would leave it tame but unusable for them. A real handover needs an explicit confirmation step and an ownership rewrite, and is planned for a later update rather than this one.
## Pet contracts

A pet contract lets you give a tamed pet to somebody else. Craft one from two paper, a leather and an ink sac.

- Right-click a tamed pet you own to record it. The pet disappears into the contract and the item shows its name.
- Right-click a block (or use the item) to release it. The pet reappears at that spot and belongs to whoever
  released it, so handing the filled contract to another player transfers the animal.
- Only the owner can record a pet, a contract holds one pet at a time, and the contract is used up on release.
- Health, hunger, gender and pedigree travel with the pet; it arrives sitting-down-free and is set to stay put
  until it is told otherwise. Breeding and production state are kept, so a recorded cow is still the same cow.

Pets from the original mod could only ever belong to the player who tamed them; there was no leash, crate or
hand-over interaction, so this is an addition rather than a restoration.
