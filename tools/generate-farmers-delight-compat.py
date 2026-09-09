"""Generate optional Farmer's Delight recipes and shared ingredient tags."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
neo = 'neo_version=' in (root / 'gradle.properties').read_text(encoding='utf-8')
data = root / 'src/main/resources/data'
recipe_dir = 'recipe' if neo else 'recipes'
tag_dir = 'item' if neo else 'items'
namespace = 'c' if neo else 'forge'


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')


def tag(name, values):
    path = data / namespace / 'tags' / tag_dir / (name + '.json')
    value = json.loads(path.read_text(encoding='utf-8')) if path.exists() else {'replace': False, 'values': []}
    for item in values:
        if item not in value['values']:
            value['values'].append(item)
    write(path, value)


def stack(item, count=1):
    return {'id' if neo else 'item': item, 'count': count}


def recipe(name, value):
    value['neoforge:conditions' if neo else 'conditions'] = [
        {'type': 'neoforge:mod_loaded' if neo else 'forge:mod_loaded', 'modid': 'farmersdelight'}]
    write(data / 'animania' / recipe_dir / 'compat/farmersdelight' / (name + '.json'), value)


milk_types = ['holstein', 'friesian', 'jersey', 'goat', 'sheep']
meats = {
    'beef': ['beef', 'steak'], 'pork': ['pork', 'bacon'],
    'chicken': ['chicken'], 'mutton': ['mutton'], 'bacon': ['bacon']
}
for state in ['raw', 'cooked']:
    for kind, items in meats.items():
        tag(('foods/' if neo else '') + state + '_' + kind,
            ['animania:' + state + '_prime_' + item for item in items])
    tag(('foods/' if neo else '') + state + '_meat', [
        'animania:' + state + '_prime_' + item
        for item in ['beef', 'steak', 'pork', 'bacon', 'chicken', 'mutton', 'chevon', 'rabbit', 'peacock']])
tag('eggs', ['animania:brown_egg'])
tag('drinks/milk' if neo else 'milk',
    ['animania:' + milk + '_bucket_milk' for milk in milk_types] + ['animania:milk_bottle'])

knife_tag = 'c:tools/knife' if neo else 'forge:tools/knives'
tag('tools/knife' if neo else 'tools/knives', ['animania:carving_knife'])
for milk in milk_types:
    result = stack('animania:' + milk + '_cheese_wedge', 4)
    recipe('cutting/' + milk + '_cheese', {
        'type': 'farmersdelight:cutting',
        'ingredients': [{'item': 'animania:' + milk + '_cheese_wheel'}],
        'tool': {'tag': knife_tag},
        'result': [{'item': result}] if neo else [result]
    })

recipe('straw_bedding', {
    'type': 'minecraft:crafting_shapeless',
    'ingredients': [{'item': 'farmersdelight:straw'}],
    'result': stack('animania:block_straw')
})
recipe('cooking/truffle_soup', {
    'type': 'farmersdelight:cooking',
    'ingredients': [{'item': 'animania:truffle'}, {'item': 'animania:truffle'}],
    'result': stack('animania:truffle_soup'), 'container': stack('minecraft:bowl'),
    'cookingtime': 200, 'experience': 1.0, 'recipe_book_tab': 'meals'
})
# Farmer's Delight's rabbit stew uses a literal vanilla rabbit ingredient.
recipe('cooking/prime_rabbit_stew', {
    'type': 'farmersdelight:cooking',
    'ingredients': [{'item': 'animania:raw_prime_rabbit'}, {'item': 'minecraft:potato'},
                    {'item': 'minecraft:carrot'},
                    [{'item': 'minecraft:brown_mushroom'}, {'item': 'minecraft:red_mushroom'}]],
    'result': stack('minecraft:rabbit_stew'), 'container': stack('minecraft:bowl'),
    'cookingtime': 200, 'experience': 1.0, 'recipe_book_tab': 'meals'
})
for method, time in [('smelting', 200), ('smoking', 100), ('campfire_cooking', 600)]:
    recipe('brown_egg_' + method, {
        'type': 'minecraft:' + method, 'category': 'food',
        'ingredient': {'item': 'animania:brown_egg'},
        'result': stack('farmersdelight:fried_egg') if neo else 'farmersdelight:fried_egg',
        'experience': 0.35, 'cookingtime': time
    })
recipe('cheese_sandwich', {
    'type': 'minecraft:crafting_shapeless',
    'ingredients': [{'item': 'minecraft:bread'}, {'tag': 'animania:cheese_wedges'},
                    {'item': 'farmersdelight:cabbage_leaf'}],
    'result': stack('animania:cheese_sandwich')
})
recipe('cooking/truffle_risotto', {
    'type': 'farmersdelight:cooking',
    'ingredients': [{'item': 'farmersdelight:rice'}, {'item': 'animania:truffle'},
                    {'tag': 'animania:cheese_wedges'},
                    {'tag': 'c:drinks/milk' if neo else 'forge:milk'}],
    'result': stack('animania:truffle_risotto'), 'container': stack('minecraft:bowl'),
    'cookingtime': 200, 'experience': 1.0, 'recipe_book_tab': 'meals'
})
recipe('cooking/vegetable_slop', {
    'type': 'farmersdelight:cooking',
    'ingredients': [{'tag': 'animania:compat/farmersdelight/slop_vegetables'},
                    {'tag': 'animania:compat/farmersdelight/slop_vegetables'},
                    {'item': 'farmersdelight:rice'}, {'item': 'minecraft:water_bucket'}],
    'result': stack('animania:bucket_slop'), 'container': stack('minecraft:bucket'),
    'cookingtime': 200, 'experience': 0.35, 'recipe_book_tab': 'misc'
})

feeds = {
    'cow': ['rice'], 'sheep': ['rice'], 'goat': ['rice'], 'horse': ['rice'],
    'pig': ['cabbage', 'cabbage_leaf', 'tomato', 'rice'],
    'rabbit': ['cabbage', 'cabbage_leaf'],
    'chicken': ['rice', 'cabbage_seeds', 'tomato_seeds'],
    'peacock': ['rice', 'cabbage_seeds', 'tomato_seeds'],
    'hamster': ['rice', 'cabbage_seeds', 'tomato_seeds'],
}
feeds['trough'] = sorted(set(item for items in feeds.values() for item in items))
feeds['petbowl'] = feeds['hamster']
for animal, items in feeds.items():
    write(data / 'animania/tags' / tag_dir / 'compat/farmersdelight/feed' / (animal + '.json'), {
        'replace': False,
        'values': [{'id': 'farmersdelight:' + item, 'required': False} for item in items]
    })
write(data / 'animania/tags' / tag_dir / 'compat/farmersdelight/slop_vegetables.json', {
    'replace': False,
    'values': ['minecraft:carrot', 'minecraft:potato', 'minecraft:beetroot'] + [
        {'id': 'farmersdelight:' + item, 'required': False}
        for item in ['cabbage', 'cabbage_leaf', 'tomato']]
})
print('Generated Farmer\'s Delight compatibility for', 'NeoForge' if neo else 'Forge')
