# Cattle and sheep horn audit

The table describes the line represented in the mod, not every genotype found in a real breed. Dehorning, polled inheritance and random horn frequencies are not introduced. Existing fictional breeds keep their established source-inspired appearance. Calves and lambs do not render mature horns; existing adult conversion remains unchanged.

## Cattle

| In-game breed | Adult bulls and cows | Basis / decision |
| --- | --- | --- |
| Angus | Polled | Naturally polled breed; retain hornless models. |
| Cookie | Polled | Represents Belted Galloway; retain hornless models. |
| Friesian | Horned | Retain the represented horned dairy line; polled genetics also exist. |
| Holstein | Horned | Same distinction: horned line, not a claim that all Holsteins have horns. |
| Hereford | Horned | Represents traditional horned Hereford, not Polled Hereford. |
| Jersey | Horned | Keep the horned line; natural polling also exists. |
| Simmental | Horned | Keep the horned line; both horned and polled lines exist. |
| Norwegian Red | Horned | Keep the horned line. Geno explicitly describes both horned and polled animals. |
| Longhorn | Horned | Retain long horns in both sexes. |
| Highland | Horned | Both sexes horned; existing sex-specific longhorn-derived models remain stylized. |
| Fighting | Horned | Custom breed inspired by the historical Animania appearance, not real horned Angus. |
| Umbra | Horned | Earth-inspired Highland derivative; preserve established design. |
| Wooly | Horned | Earth-inspired Highland derivative; preserve established design. |
| Warm | Horned | Minecraft-inspired Longhorn derivative; preserve established design. |
| Pinto | Horned | Earth-inspired coat, not an independently established real breed standard. |
| Albino | Horned | Coat variant; albinism alone does not define horn genotype. Preserve design. |
| Cream | Horned | Earth-inspired coat; preserve design. |
| Mooshroom | Horned | Fictional Minecraft animal; preserve design. |

## Sheep

| In-game breed | Ram | Ewe | Decision |
| --- | --- | --- | --- |
| Dorper | Polled | Polled | Retain represented polled animals. Dorper standards can allow horn bases/small horns in rams; do not invent a frequency. |
| Dorset | Horned | Horned, smaller | Consistently represent Dorset Horn. Add missing ewe horns; Poll Dorset is a distinct alternative line. |
| Friesian | Polled | Polled | Correct legacy model horns; East Friesian milk sheep are polled in both sexes. |
| Jacob | Horned | Horned | Retain original multi-horn geometry. Real Jacobs have variable horn counts, not a new random system here. |
| Merino | Horned | No developed horns | Retain traditional horned-ram line. Ewes may have knobs; Poll Merino also exists. |
| Suffolk | Polled | Polled | Suppress legacy horn geometry for both sexes. |
| Long Nosed | Horned | Horned, smaller | Valais Blacknose reference; retain existing restored horns. |
| Flecked | Polled | Polled | Fictional Earth-inspired coat; preserve source-inspired appearance. |
| Fuzzy | Polled | Polled | Fictional Earth-inspired coat; preserve source-inspired appearance. |
| Inky | Polled | Polled | Fictional Earth-inspired coat; preserve source-inspired appearance. |
| Patched | Polled | Polled | Fictional Earth-inspired coat; preserve source-inspired appearance. |
| Rocky | Polled | Polled | Fictional Earth-inspired coat; preserve source-inspired appearance. |

## Implementation

`CowBreed.hasAdultHorns` and `SheepBreed.hasAdultHorns` explicitly cover every current breed. `LivestockHornAppearance` applies visibility after legacy animation and resets it for each animal, including shared models and sheared animals. Original hidden parts are retained so generated animation references remain valid. Dorset ewe horn segments reuse the Dorset ram's geometry and UV pixels, scaled together around the skull attachment to keep segment joins intact. Original body shape, coat and animation hierarchy remain intact.

Both loaders use the same policy and assets. Visual inspection in game is still required; compilation does not establish that every horn pose aligns perfectly.

## References

- Angus breed association: https://angus.fi/en/the-breed/
- Belted Galloway Society selection standard: https://www.beltie.org/guidelines-for-selecting.php
- Holstein Association polled resources: https://www.holsteinusa.com/genetic_evaluations/polled_resource.html
- Peer-reviewed cattle polled genetics, including Jersey/Friesian lines: https://pmc.ncbi.nlm.nih.gov/articles/PMC3966897/
- Oklahoma State University, Polled Hereford: https://breeds.okstate.edu/cattle/polled-hereford-cattle
- American Simmental Association: https://simmental.org/site/index.php/component/content/article/animal-breeding-basics?Itemid=101&catid=153
- Geno, Norwegian Red genetics FAQ: https://www.norwegianred.com/about-geno-sa/frequently-asked-questions/
- World Association for Buiatrics, Texas Longhorn breed sheet: https://buiatrics.com/wp-content/uploads/2020/12/Breeds_TexasLonghorn.pdf
- Highland Cattle Society: https://www.highlandcattlesociety.com/the-highland-cattle-breed
- BC Purebred Sheep Breeders, East Friesian: https://bcsheep.com/eastfriesian.htm
- United Suffolk Sheep Association standard: https://suffolks.org/pdf/2022/Suffolk_Breed_Standards-updated.pdf
- Oklahoma State University, Dorset: https://breeds.okstate.edu/sheep/dorset-sheep
- Jacob Sheep Society standard: https://jacobsheepsociety.co.uk/breed-standard/
- Australian Stud Merino definition: https://merinos.com.au/wp-content/uploads/Australian-Stud-Merino-Definition-v15092020.pdf
- Dorper association standard: https://www.dorper-hairsheep.com/dorper/rassenstandart/
- New Zealand Valais Blacknose standard: https://nzsheep.co.nz/site/wp-content/uploads/2022/02/VALAIS-BLACKNOSE-BREED-STANDARD.pdf
