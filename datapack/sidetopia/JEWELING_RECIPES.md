# Sidetopia custom-enchantment datapack

This directory is a deployable copy of the supplied Minecraft 1.21.1 datapack. Install the directory (or a zip of its contents) in the world's `datapacks` directory alongside Teapot Cosmetics. The Jeweling mappings stay in this pack because they depend on the dynamic enchantments defined here.

Jeweling recipes are loaded from the exact path `data/sidetopia/jeweling_recipes/*.json`. Jewel-producing loot tables now emit registered `teapot_cosmetics:*` items directly; the obsolete custom-data lapis smithing prototype is not included.

Important: this conversion preserves the supplied pack's existing non-jewel loot balance. Its vanilla ore overrides remove standard Silk Touch/Fortune behavior, the deepslate-lapis override drops cobbled deepslate instead of normal lapis, and the base fishing selection excludes vanilla treasure. Confirm those rules are intentional before production deployment; they were not redesigned as part of the jewel-ID migration.

## Verified mapping table

All mapped enchantments below exist under `data/sidetopia/enchantment/`, every requested level is within the enchantment's `max_level`, and every Jeweller tier is in the supported 0–3 range. Tier 0 means the recipe adds no minimum-tier requirement; KnowledgeBound's socket limit and Teapot's compatibility checks still apply.

| Jewel item | Recipe ID | Enchantment | Level | Jeweller tier | Allowed item selector | Duplicate policy |
|---|---|---|---:|---:|---|---|
| `teapot_cosmetics:amber` | `sidetopia:amber` | `sidetopia:amber_collector` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:aquamarine` | `sidetopia:aquamarine` | `sidetopia:aquamarine_disable` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:celestite` | `sidetopia:celestite` | `sidetopia:celestite_clarity` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:cinnabar` | `sidetopia:cinnabar` | `sidetopia:cinnabar_explosive` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:citrine` | `sidetopia:citrine` | `sidetopia:citrine_performance` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:flawless_amethyst` | `sidetopia:flawless_amethyst` | `sidetopia:amethyst_power` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:flawless_diamond` | `sidetopia:flawless_diamond` | `sidetopia:diamond_guard` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:flawless_lapis` | `sidetopia:flawless_lapis` | `sidetopia:lapis_prospector` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:flawless_quartz` | `sidetopia:flawless_quartz` | `sidetopia:quartz_edge` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:garnet` | `sidetopia:garnet` | `sidetopia:garnet_vitality` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:jade` | `sidetopia:jade` | `sidetopia:jade_poison` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:onyx` | `sidetopia:onyx` | `sidetopia:onyx_durability` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:opal` | `sidetopia:opal_chestplate` | `sidetopia:opal_guardian_chestplate` | 1 | 0 | `#minecraft:chest_armor` | `reject` |
| `teapot_cosmetics:opal` | `sidetopia:opal_shield` | `sidetopia:opal_guardian_shield` | 1 | 0 | `minecraft:shield` | `reject` |
| `teapot_cosmetics:opal` | `sidetopia:opal_sword` | `sidetopia:opal_guardian_sword` | 1 | 0 | `#minecraft:swords` | `reject` |
| `teapot_cosmetics:padparadscha` | `sidetopia:padparadscha` | `sidetopia:padparadscha_sentinel` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:pearl` | `sidetopia:pearl` | `sidetopia:pearl_bait` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:peridot` | `sidetopia:peridot` | `sidetopia:peridot_spikes` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:rose_quartz` | `sidetopia:rose_quartz` | `sidetopia:rose_quartz_edge` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:ruby` | `sidetopia:ruby` | `sidetopia:ruby_flame` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:sapphire` | `sidetopia:sapphire` | `sidetopia:sapphire_penetration` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:serpentine` | `sidetopia:serpentine` | `sidetopia:serpentine_silence` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:topaz` | `sidetopia:topaz_boots` | `sidetopia:topaz_adrenaline_boots` | 1 | 0 | `#minecraft:enchantable/foot_armor` | `reject` |
| `teapot_cosmetics:topaz` | `sidetopia:topaz_chestplate` | `sidetopia:topaz_adrenaline_chestplate` | 1 | 0 | `#minecraft:enchantable/chest_armor` | `reject` |
| `teapot_cosmetics:topaz` | `sidetopia:topaz_helmet` | `sidetopia:topaz_adrenaline_helmet` | 1 | 0 | `#minecraft:enchantable/head_armor` | `reject` |
| `teapot_cosmetics:topaz` | `sidetopia:topaz_leggings` | `sidetopia:topaz_adrenaline_leggings` | 1 | 0 | `#minecraft:enchantable/leg_armor` | `reject` |
| `teapot_cosmetics:tourmaline` | `sidetopia:tourmaline` | `sidetopia:tourmaline_stun` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:turquoise` | `sidetopia:turquoise` | `sidetopia:turquoise_control` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:witherite` | `sidetopia:witherite` | `sidetopia:witherite_intimidation` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |
| `teapot_cosmetics:zircon` | `sidetopia:zircon` | `sidetopia:zircon_immunity` | 1 | 0 | `#teapot_cosmetics:jewelable` | `reject` |

There are 30 recipes for 25 mapped jewel items. Opal uses distinct chestplate, shield, and sword recipes; Topaz uses distinct helmet, chestplate, leggings, and boots recipes. Those selectors match the corresponding enchantment `supported_items` values and do not overlap.

## Intentionally unmapped jewels

The current datapack has no enchantment definition for:

- `teapot_cosmetics:alexandrite`
- `teapot_cosmetics:beryl`
- `teapot_cosmetics:flawless_emerald`

They remain valid entries in `#teapot_cosmetics:jewels` and may be acquired as real items, but no unrelated effect is invented for them. The table reports that they have no configured effect.

## Verification

After editing recipes, enchantments, tags, or loot tables, run `/reload`, then use `/teapot jeweling recipes`. A healthy load reports 30 recipes.

The only remaining `minecraft:lapis_lazuli` loot entry is the normal vanilla lapis drop in `data/minecraft/loot_table/blocks/lapis_ore.json`. Other `custom_model_data` entries belong to unrelated custom equipment, rods, tea, and food—not jewels.
