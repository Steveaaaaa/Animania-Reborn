$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$assets = Join-Path $root 'src/main/resources/assets/animania'
$data = Join-Path $root 'src/main/resources/data/animania'

function Write-JsonFile([string]$Path, $Value) {
    $dir = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $json = $Value | ConvertTo-Json -Depth 20
    [IO.File]::WriteAllText($Path, $json + [Environment]::NewLine, [Text.UTF8Encoding]::new($false))
}

function Item-Model([string]$Name, [string]$Texture = '') {
    if (-not $Texture) { $Texture = $Name }
    Write-JsonFile (Join-Path $assets "models/item/$Name.json") ([ordered]@{
        parent = 'minecraft:item/generated'; textures = [ordered]@{ layer0 = "animania:item/$Texture" }
    })
}

function Block-Cube([string]$Name, [string]$Texture = '') {
    if (-not $Texture) { $Texture = $Name }
    Write-JsonFile (Join-Path $assets "models/block/$Name.json") ([ordered]@{
        parent = 'minecraft:block/cube_all'; textures = [ordered]@{ all = "animania:block/$Texture" }
    })
    Write-JsonFile (Join-Path $assets "blockstates/$Name.json") ([ordered]@{
        variants = [ordered]@{ '' = [ordered]@{ model = "animania:block/$Name" } }
    })
}

function Shapeless([string]$Name, [array]$Ingredients, [string]$Result, [int]$Count = 1) {
    $resultObject = [ordered]@{ id = $Result }
    if ($Count -ne 1) { $resultObject.count = $Count }
    Write-JsonFile (Join-Path $data "recipe/$Name.json") ([ordered]@{
        type = 'minecraft:crafting_shapeless'; category = 'misc'; ingredients = $Ingredients; result = $resultObject
    })
}

function Smelting([string]$Name, [string]$Input, [string]$Result, [double]$Experience = 0.35) {
    Write-JsonFile (Join-Path $data "recipe/$Name.json") ([ordered]@{
        type = 'minecraft:smelting'; category = 'food'; cookingtime = 200; experience = $Experience
        ingredient = [ordered]@{ item = $Input }; result = [ordered]@{ id = $Result }
    })
}

$generatedItems = @(
    'animania_manual','salt','salt_lick','raw_prime_bacon','cooked_prime_bacon','raw_prime_chicken',
    'cooked_prime_chicken','raw_chevon','cooked_chevon','plain_omelette','cheese_omelette',
    'bacon_omelette','truffle_omelette','super_omelette','truffle_soup','chocolate_truffle',
    'milk_bottle','honey_bottle','carving_knife','riding_crop','entity_egg_random',
    'entity_egg_chicken_random','entity_egg_cow_random','entity_egg_goat_random','entity_egg_pig_random',
    'entity_egg_sheep_random','entity_egg_rabbit_random','entity_egg_peacock_random'
)
foreach ($name in $generatedItems) { Item-Model $name }
Item-Model 'bucket_slop'

foreach ($name in @('bucket_honey')) {
    Write-JsonFile (Join-Path $assets "models/item/$name.json") ([ordered]@{
        parent = 'neoforge:item/bucket'; loader = 'neoforge:fluid_container'; fluid = 'animania:animania_honey'
    })
}

Block-Cube 'block_invisiblock' 'invisiblock'
Write-JsonFile (Join-Path $assets 'models/item/block_straw.json') ([ordered]@{
    parent = 'minecraft:item/generated'; textures = [ordered]@{ layer0 = 'animania:block/straw' }
})
Write-JsonFile (Join-Path $assets 'models/item/salt_lick.json') ([ordered]@{ parent = 'animania:block/salt_lick' })
Write-JsonFile (Join-Path $assets 'models/block/salt_lick.json') ([ordered]@{
    parent = 'minecraft:block/cube_all'; textures = [ordered]@{ all = 'animania:block/salt' }
})
$wearVariants = [ordered]@{}
0..7 | ForEach-Object { $wearVariants["wear=$_"] = [ordered]@{ model = 'animania:block/salt_lick' } }
Write-JsonFile (Join-Path $assets 'blockstates/salt_lick.json') ([ordered]@{ variants = $wearVariants })

$wools = @('dorset_brown','friesian_black','friesian_brown','jacob','merino_brown','merino_white','suffolk_brown')
foreach ($wool in $wools) {
    $id = "wool_$wool"
    Block-Cube $id $id
    Write-JsonFile (Join-Path $assets "models/item/$id.json") ([ordered]@{ parent = "animania:block/$id" })
}

foreach ($fluid in @('milk_holstein','milk_friesian','milk_jersey','milk_goat','milk_sheep','animania_honey','slop')) {
    Write-JsonFile (Join-Path $assets "blockstates/$fluid.json") ([ordered]@{
        variants = [ordered]@{ '' = [ordered]@{ model = "animania:block/$fluid" } }
    })
    $particle = if ($fluid -like 'milk_*') { 'minecraft:block/water_still' } else { "animania:fluid/${fluid}_still" }
    Write-JsonFile (Join-Path $assets "models/block/$fluid.json") ([ordered]@{ textures = [ordered]@{ particle = $particle } })
}

$item = { param($id) [ordered]@{ item = $id } }
$tag = { param($id) [ordered]@{ tag = $id } }
Shapeless 'block_straw' @((&$item 'minecraft:wheat')) 'animania:block_straw'
Shapeless 'animania_manual' @((&$item 'minecraft:book'), (&$tag 'c:seeds'), (&$item 'animania:block_straw')) 'animania:animania_manual'
Shapeless 'salt_lick' @((1..8 | ForEach-Object { &$item 'animania:salt' }) + @((&$item 'minecraft:water_bucket'))) 'animania:salt_lick'

Write-JsonFile (Join-Path $data 'recipe/carving_knife.json') ([ordered]@{
    type='minecraft:crafting_shaped'; category='equipment'; pattern=@(' II',' SI','S  ')
    key=[ordered]@{ I=(& $tag 'c:ingots/iron'); S=(& $tag 'c:rods/wooden') }
    result=[ordered]@{ id='animania:carving_knife' }
})
Write-JsonFile (Join-Path $data 'recipe/riding_crop.json') ([ordered]@{
    type='minecraft:crafting_shaped'; category='equipment'; pattern=@('  L',' S ','L  ')
    key=[ordered]@{ L=(& $item 'minecraft:leather'); S=(& $tag 'c:rods/wooden') }
    result=[ordered]@{ id='animania:riding_crop' }
})
Write-JsonFile (Join-Path $data 'recipe/bee_hive.json') ([ordered]@{
    type='minecraft:crafting_shaped'; category='misc'; pattern=@('PPP','HSH','PPP')
    key=[ordered]@{ P=(& $tag 'minecraft:planks'); H=(& $item 'minecraft:honeycomb'); S=(& $item 'minecraft:slime_ball') }
    result=[ordered]@{ id='animania:bee_hive' }
})

Shapeless 'plain_omelette' @((&$tag 'c:eggs'), (&$tag 'c:eggs')) 'animania:plain_omelette'
Shapeless 'cheese_omelette' @((&$item 'animania:plain_omelette'), (&$tag 'animania:cheese_wedges')) 'animania:cheese_omelette'
Shapeless 'bacon_omelette' @((&$item 'animania:plain_omelette'), (&$item 'animania:cooked_prime_bacon')) 'animania:bacon_omelette'
Shapeless 'truffle_omelette' @((&$item 'animania:plain_omelette'), (&$item 'animania:truffle')) 'animania:truffle_omelette'
Shapeless 'super_omelette' @((&$item 'animania:plain_omelette'), (&$item 'animania:cooked_prime_bacon'), (&$item 'animania:truffle'), (&$tag 'animania:cheese_wedges')) 'animania:super_omelette'
Shapeless 'truffle_soup' @((&$item 'animania:truffle'), (&$item 'animania:truffle'), (&$item 'minecraft:bowl')) 'animania:truffle_soup'
Shapeless 'chocolate_truffle' @((&$item 'animania:truffle'), (&$item 'minecraft:cocoa_beans'), (&$item 'minecraft:sugar')) 'animania:chocolate_truffle'

Smelting 'cooked_prime_bacon' 'animania:raw_prime_bacon' 'animania:cooked_prime_bacon'
Smelting 'cooked_prime_chicken' 'animania:raw_prime_chicken' 'animania:cooked_prime_chicken'
Smelting 'cooked_chevon' 'animania:raw_chevon' 'animania:cooked_chevon'
Shapeless 'beef_cutting' @((&$item 'animania:carving_knife'), (&$item 'animania:raw_prime_beef')) 'animania:raw_prime_steak' 4
Shapeless 'pork_cutting' @((&$item 'animania:carving_knife'), (&$item 'animania:raw_prime_pork')) 'animania:raw_prime_bacon' 4
Shapeless 'straw_cutting' @((&$item 'animania:carving_knife'), (&$item 'minecraft:wheat')) 'animania:block_straw'

foreach ($milk in @('holstein','friesian','jersey','goat','sheep')) {
    Shapeless "milk_bottle_$milk" @((&$item "animania:${milk}_bucket_milk"), (&$item 'minecraft:glass_bottle'), (&$item 'minecraft:glass_bottle'), (&$item 'minecraft:glass_bottle'), (&$item 'minecraft:glass_bottle')) 'animania:milk_bottle' 4
    Shapeless "slop_$milk" @((&$item 'minecraft:carrot'), (&$item 'minecraft:potato'), (&$item "animania:${milk}_bucket_milk")) 'animania:bucket_slop'
    Shapeless "cheese_cutting_$milk" @((&$item 'animania:carving_knife'), (&$item "animania:${milk}_cheese_wheel")) "animania:${milk}_cheese_wedge" 4
}
Shapeless 'milk_bottle_vanilla' @((&$item 'minecraft:milk_bucket'), (&$item 'minecraft:glass_bottle'), (&$item 'minecraft:glass_bottle'), (&$item 'minecraft:glass_bottle'), (&$item 'minecraft:glass_bottle')) 'animania:milk_bottle' 4
Shapeless 'slop_vanilla' @((&$item 'minecraft:carrot'), (&$item 'minecraft:potato'), (&$item 'minecraft:milk_bucket')) 'animania:bucket_slop'
Shapeless 'honey_bottle' @((&$item 'animania:bucket_honey'), (&$item 'minecraft:glass_bottle')) 'animania:honey_bottle'

$woolToVanilla = [ordered]@{
    dorset_brown='minecraft:brown_wool'; friesian_black='minecraft:black_wool'; friesian_brown='minecraft:brown_wool'
    jacob='minecraft:white_wool'; merino_brown='minecraft:brown_wool'; merino_white='minecraft:white_wool'; suffolk_brown='minecraft:brown_wool'
}
foreach ($entry in $woolToVanilla.GetEnumerator()) {
    Shapeless "wool_$($entry.Key)_to_vanilla" @((&$item "animania:wool_$($entry.Key)")) $entry.Value
}

Write-JsonFile (Join-Path $data 'tags/item/cheese_wedges.json') ([ordered]@{
    replace=$false; values=@('animania:holstein_cheese_wedge','animania:friesian_cheese_wedge',
        'animania:jersey_cheese_wedge','animania:goat_cheese_wedge','animania:sheep_cheese_wedge')
})

function Simple-Loot([string]$Name, [string]$ItemName) {
    Write-JsonFile (Join-Path $data "loot_table/blocks/$Name.json") ([ordered]@{
        type='minecraft:block'; pools=@([ordered]@{ rolls=1; entries=@([ordered]@{ type='minecraft:item'; name=$ItemName }) })
        random_sequence="animania:blocks/$Name"
    })
}
Simple-Loot 'block_straw' 'animania:block_straw'
Simple-Loot 'block_hive' 'animania:bee_hive'
Simple-Loot 'block_wild_hive' 'animania:wild_hive'
foreach ($wool in $wools) { Simple-Loot "wool_$wool" "animania:wool_$wool" }
Write-JsonFile (Join-Path $data 'loot_table/blocks/salt_lick.json') ([ordered]@{ type='minecraft:block'; pools=@() })

function Entity-Loot([string]$Name, [string]$Meat) {
    Write-JsonFile (Join-Path $data "loot_table/entities/$Name.json") ([ordered]@{
        type='minecraft:entity'; pools=@(
            [ordered]@{ rolls=1; entries=@([ordered]@{ type='minecraft:item'; name='minecraft:feather'; functions=@([ordered]@{ function='minecraft:set_count'; count=[ordered]@{ min=0; max=1 } },[ordered]@{ function='minecraft:enchanted_count_increase'; enchantment='minecraft:looting'; count=[ordered]@{ min=0; max=1 } }) }) },
            [ordered]@{ rolls=1; entries=@([ordered]@{ type='minecraft:item'; name=$Meat; functions=@([ordered]@{ function='minecraft:set_count'; count=1 },[ordered]@{ function='minecraft:furnace_smelt'; conditions=@([ordered]@{ condition='minecraft:entity_properties'; entity='this'; predicate=[ordered]@{ flags=[ordered]@{ is_on_fire=$true } } }) }) }) }
        ); random_sequence="animania:entities/$Name"
    })
}
Entity-Loot 'chicken_prime' 'animania:raw_prime_chicken'
Entity-Loot 'chicken_regular' 'minecraft:chicken'
Write-JsonFile (Join-Path $data 'loot_table/entities/goat_regular.json') ([ordered]@{
    type='minecraft:entity'; pools=@([ordered]@{ rolls=1; entries=@([ordered]@{ type='minecraft:item'; name='animania:raw_chevon'; functions=@([ordered]@{ function='minecraft:set_count'; count=[ordered]@{ min=1; max=2 } },[ordered]@{ function='minecraft:furnace_smelt'; conditions=@([ordered]@{ condition='minecraft:entity_properties'; entity='this'; predicate=[ordered]@{ flags=[ordered]@{ is_on_fire=$true } } }) }) }) }); random_sequence='animania:entities/goat_regular'
})

$enPath = Join-Path $assets 'lang/en_us.json'
$zhPath = Join-Path $assets 'lang/zh_cn.json'
$en = Get-Content -Raw $enPath | ConvertFrom-Json -AsHashtable
$zh = Get-Content -Raw $zhPath | ConvertFrom-Json -AsHashtable
$names = [ordered]@{
    'block.animania.block_straw'='Straw'; 'block.animania.block_seeds'='Spilled Seeds'; 'block.animania.salt_lick'='Salt Lick'
    'item.animania.animania_manual'='Animania Manual'; 'item.animania.salt'='Salt'; 'item.animania.bucket_slop'='Slop Bucket'
    'item.animania.bucket_honey'='Honey Bucket'; 'item.animania.honey_bottle'='Honey Bottle'; 'item.animania.milk_bottle'='Milk Bottle'
    'item.animania.carving_knife'='Carving Knife'; 'item.animania.riding_crop'='Riding Crop'
    'item.animania.raw_prime_bacon'='Raw Prime Bacon'; 'item.animania.cooked_prime_bacon'='Cooked Prime Bacon'
    'item.animania.raw_prime_chicken'='Raw Prime Chicken'; 'item.animania.cooked_prime_chicken'='Cooked Prime Chicken'
    'item.animania.raw_chevon'='Raw Chevon'; 'item.animania.cooked_chevon'='Cooked Chevon'
    'item.animania.plain_omelette'='Plain Omelette'; 'item.animania.cheese_omelette'='Cheese Omelette'
    'item.animania.bacon_omelette'='Bacon Omelette'; 'item.animania.truffle_omelette'='Truffle Omelette'
    'item.animania.super_omelette'='Ultimate Omelette'; 'item.animania.truffle_soup'='Truffle Soup'
    'item.animania.chocolate_truffle'='Chocolate Truffle'; 'message.animania.salt_lick_uses'='Salt lick: %s uses remaining'
    'message.animania.hive_status'='Honey: %1$s/%2$s mB; next production in %3$s ticks'
    'manual.animania.page.welcome'='Welcome to Animania. This guide summarizes the care, breeding, products and utilities restored in the NeoForge port.'
    'manual.animania.page.needs'='Animal care\n\nAnimals need food and water. Fill troughs or pet bowls and inspect an animal with an empty hand.'
    'manual.animania.page.farm'='Farm animals\n\nBreed compatible adult males and females. Well-cared-for animals reproduce and provide their breed-specific products.'
    'manual.animania.page.dairy'='Dairy\n\nMilk cows, goats and sheep with a bucket. Put a milk bucket into a cheese mold and wait for the wheel to mature.'
    'manual.animania.page.hives'='Hives\n\nHives produce up to 5000 mB of honey. Use a bottle or empty fluid container to extract it. Wild hives sting nearby players.'
    'manual.animania.page.extra'='Extra animals\n\nRabbits, peafowl, amphibians, hamsters, hedgehogs and ferrets retain their species products and utilities.'
    'manual.animania.page.pets'='Cats & Dogs\n\nTame pets, feed them from bowls, command them to sit and use beds, towers, houses, pillows and litter boxes.'
    'manual.animania.page.vehicles'='Vehicles\n\nPull vehicles by hand, hitch an eligible ridden or leashed animal, or shift-use to ride/open storage. Carts accept a chest; pigs pull carts and cattle pull tillers.'
}
$zhNames = [ordered]@{
    'block.animania.block_straw'='稻草'; 'block.animania.block_seeds'='散落的种子'; 'block.animania.salt_lick'='盐砖'
    'item.animania.animania_manual'='动物谷手册'; 'item.animania.salt'='盐'; 'item.animania.bucket_slop'='泔水桶'
    'item.animania.bucket_honey'='蜂蜜桶'; 'item.animania.honey_bottle'='蜂蜜瓶'; 'item.animania.milk_bottle'='牛奶瓶'
    'item.animania.carving_knife'='雕肉刀'; 'item.animania.riding_crop'='马鞭'
    'item.animania.raw_prime_bacon'='生精品培根'; 'item.animania.cooked_prime_bacon'='熟精品培根'
    'item.animania.raw_prime_chicken'='生精品鸡肉'; 'item.animania.cooked_prime_chicken'='熟精品鸡肉'
    'item.animania.raw_chevon'='生山羊肉'; 'item.animania.cooked_chevon'='熟山羊肉'
    'item.animania.plain_omelette'='原味煎蛋卷'; 'item.animania.cheese_omelette'='奶酪煎蛋卷'
    'item.animania.bacon_omelette'='培根煎蛋卷'; 'item.animania.truffle_omelette'='松露煎蛋卷'
    'item.animania.super_omelette'='终极煎蛋卷'; 'item.animania.truffle_soup'='松露汤'
    'item.animania.chocolate_truffle'='巧克力松露'; 'message.animania.salt_lick_uses'='盐砖剩余 %s 次'
    'message.animania.hive_status'='蜂蜜：%1$s/%2$s mB；距离下次产蜜还有 %3$s tick'
    'manual.animania.page.welcome'='欢迎来到动物谷。本手册概述 NeoForge 移植版中的照料、繁殖、产物和工具。'
    'manual.animania.page.needs'='动物照料\n\n动物需要食物和水。填充食槽或宠物碗，空手与动物互动可检查状态。'
    'manual.animania.page.farm'='农场动物\n\n让兼容品种的成年雌雄动物繁殖。照料良好的动物会繁殖并提供品种产物。'
    'manual.animania.page.dairy'='乳制品\n\n用桶给牛、山羊和绵羊挤奶。将奶桶放入奶酪模具并等待奶酪成熟。'
    'manual.animania.page.hives'='蜂巢\n\n蜂巢最多储存 5000 mB 蜂蜜。用瓶子或空流体容器提取。野生蜂巢会蜇伤附近玩家。'
    'manual.animania.page.extra'='额外动物\n\n兔子、孔雀、两栖类、仓鼠、刺猬和雪貂保留各自的产物与工具。'
    'manual.animania.page.pets'='猫与狗\n\n驯服宠物、使用宠物碗喂食、命令坐下，并使用床、猫爬架、狗屋、软垫和猫砂盆。'
    'manual.animania.page.vehicles'='载具\n\n可以空手拉车，或连接正在骑乘/附近拴绳的合适牲畜；潜行使用可乘坐或打开储物。货车可加装箱子，猪可拉货车，牛可拉耕作机。'
    'block.animania.block_invisiblock'='隐形方块'; 'block.animania.animania_honey'='蜂蜜'; 'block.animania.slop'='泔水'
    'block.animania.milk_holstein'='荷斯坦牛奶'; 'block.animania.milk_friesian'='弗里斯兰牛奶'
    'block.animania.milk_jersey'='娟姗牛奶'; 'block.animania.milk_goat'='山羊奶'; 'block.animania.milk_sheep'='绵羊奶'
}
$names['block.animania.block_invisiblock']='Invisible Block'
$names['block.animania.animania_honey']='Honey'
$names['block.animania.slop']='Slop'
$names['block.animania.milk_holstein']='Holstein Milk'
$names['block.animania.milk_friesian']='Friesian Milk'
$names['block.animania.milk_jersey']='Jersey Milk'
$names['block.animania.milk_goat']='Goat Milk'
$names['block.animania.milk_sheep']='Sheep Milk'
foreach ($entry in $names.GetEnumerator()) { $en[$entry.Key] = $entry.Value }
foreach ($entry in $zhNames.GetEnumerator()) { $zh[$entry.Key] = $entry.Value }
foreach ($wool in $wools) {
    $pretty = (Get-Culture).TextInfo.ToTitleCase($wool.Replace('_',' '))
    $en["block.animania.wool_$wool"] = "$pretty Wool"
    $woolZh = [ordered]@{
        dorset_brown='多赛特棕色羊毛'; friesian_black='弗里斯兰黑色羊毛'; friesian_brown='弗里斯兰棕色羊毛'
        jacob='雅各羊毛'; merino_brown='美利奴棕色羊毛'; merino_white='美利奴白色羊毛'; suffolk_brown='萨福克棕色羊毛'
    }
    $zh["block.animania.wool_$wool"] = $woolZh[$wool]
}
$randomEggs = [ordered]@{ random='Random Animal'; chicken_random='Random Chicken'; cow_random='Random Cow'; goat_random='Random Goat'; pig_random='Random Pig'; sheep_random='Random Sheep'; rabbit_random='Random Rabbit'; peacock_random='Random Peafowl' }
$randomEggsZh = [ordered]@{ random='随机动物刷怪蛋'; chicken_random='随机鸡刷怪蛋'; cow_random='随机牛刷怪蛋'; goat_random='随机山羊刷怪蛋'; pig_random='随机猪刷怪蛋'; sheep_random='随机绵羊刷怪蛋'; rabbit_random='随机兔子刷怪蛋'; peacock_random='随机孔雀刷怪蛋' }
foreach ($entry in $randomEggs.GetEnumerator()) {
    $en["item.animania.entity_egg_$($entry.Key)"] = "$($entry.Value) Spawn Egg"
    $zh["item.animania.entity_egg_$($entry.Key)"] = $randomEggsZh[$entry.Key]
}

# Every concrete spawn egg uses the translated entity name. This catches the
# large farm-animal set that was previously present as items but unnamed.
foreach ($entityKey in @($en.Keys | Where-Object { $_ -like 'entity.animania.*' })) {
    $entityId = $entityKey.Substring('entity.animania.'.Length)
    $eggModel = Join-Path $assets "models/item/entity_egg_$entityId.json"
    $eggKey = "item.animania.entity_egg_$entityId"
    if ((Test-Path $eggModel) -and -not $en.ContainsKey($eggKey)) {
        $en[$eggKey] = "$($en[$entityKey]) Spawn Egg"
        $zh[$eggKey] = "$($zh[$entityKey])刷怪蛋"
    }
}
Write-JsonFile $enPath $en
Write-JsonFile $zhPath $zh

Write-Output "Generated completion resources: $($generatedItems.Count) item models, $($wools.Count) wool variants, recipes and loot tables."
