$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$resources = Join-Path $root 'src/main/resources'
$assets = Join-Path $resources 'assets/animania'
$errors = [Collections.Generic.List[string]]::new()

function Require([bool]$Condition, [string]$Message) {
    if (-not $Condition) { $errors.Add($Message) }
}

# Parse every shipped JSON with the same strict JSON parser used by modern .NET.
Get-ChildItem $resources -Recurse -Filter '*.json' | ForEach-Object {
    try {
        $document = [Text.Json.JsonDocument]::Parse([IO.File]::ReadAllText($_.FullName))
        $document.Dispose()
    } catch {
        $errors.Add("Invalid JSON: $($_.FullName): $($_.Exception.Message)")
    }
}

$legacyModels = @(Get-ChildItem (Join-Path $assets 'legacy_models') -Recurse -Filter '*.json')
$legacyPoses = @(Get-ChildItem (Join-Path $assets 'legacy_animation_poses') -Recurse -Filter '*.json')
$craftModels = @(Get-ChildItem (Join-Path $assets 'craftstudio_models') -Recurse -Filter '*.json')
$craftAnimations = @(Get-ChildItem (Join-Path $assets 'craftstudio_animations') -Recurse -Filter '*.json')
$sounds = @(Get-ChildItem (Join-Path $assets 'sounds') -Recurse -Filter '*.ogg')
$eggModels = @(Get-ChildItem (Join-Path $assets 'models/item') -Filter 'entity_egg_*.json')
Require ($legacyModels.Count -eq 99) "Expected 99 mechanically converted ModelRenderer resources; found $($legacyModels.Count)."
Require ($legacyPoses.Count -eq 82) "Expected 82 extracted sleeping/sitting pose resources; found $($legacyPoses.Count)."
Require ($craftModels.Count -eq 18) "Expected all 18 CraftStudio models; found $($craftModels.Count)."
Require ($craftAnimations.Count -eq 8) "Expected all 8 CraftStudio animations; found $($craftAnimations.Count)."
Require ($sounds.Count -eq 151) "Expected all 151 legacy sounds; found $($sounds.Count)."
Require ($eggModels.Count -eq 231) "Expected all 231 spawn-egg item models; found $($eggModels.Count)."

$en = Get-Content (Join-Path $assets 'lang/en_us.json') -Raw | ConvertFrom-Json -AsHashtable
$zh = Get-Content (Join-Path $assets 'lang/zh_cn.json') -Raw | ConvertFrom-Json -AsHashtable
foreach ($key in $en.Keys) { Require ($zh.ContainsKey($key)) "Missing zh_cn translation: $key" }
foreach ($key in $zh.Keys) { Require ($en.ContainsKey($key)) "Missing en_us translation: $key" }
Get-ChildItem (Join-Path $assets 'models/item') -Filter '*.json' | ForEach-Object {
    $id = $_.BaseName
    Require ($en.ContainsKey("item.animania.$id") -or $en.ContainsKey("block.animania.$id")) `
        "Missing item/block translation for item model: $id"
}

$configSource = Get-Content (Join-Path $root 'src/main/java/com/animania/common/config/LegacyConfig.java') -Raw
$javaSource = (Get-ChildItem (Join-Path $root 'src/main/java') -Recurse -Filter '*.java' |
    ForEach-Object { [IO.File]::ReadAllText($_.FullName) }) -join "`n"
$fields = [regex]::Matches($configSource,
    '(?m)^\s*public static final ModConfigSpec\.[A-Za-z]+Value\s+([A-Z][A-Z0-9_]+);') |
    ForEach-Object { $_.Groups[1].Value }
foreach ($field in $fields) {
    $references = ([regex]::Matches($javaSource, "\b$field\b")).Count
    Require ($references -ge 3) "Legacy config field has no runtime consumer: $field"
}

$spawnData = Get-ChildItem (Join-Path $resources 'data/animania/neoforge/biome_modifier') -Filter 'add_*.json'
foreach ($file in $spawnData) {
    if ($file.Name -eq 'add_wild_hives.json') { continue }
    $raw = [IO.File]::ReadAllText($file.FullName)
    Require ($raw.Contains('"type": "animania:legacy_add_spawns"')) `
        "Spawn modifier bypasses legacy config codec: $($file.Name)"
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output "Port verification passed: $($legacyModels.Count) legacy models, $($legacyPoses.Count) poses, $($craftModels.Count) CraftStudio models, $($craftAnimations.Count) animations, $($eggModels.Count) egg models, $($sounds.Count) sounds, and $($fields.Count) scalar legacy config fields checked."
