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
