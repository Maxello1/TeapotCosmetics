# Jeweling Table

Teapot Cosmetics owns the Gem Table, jewel items, recipe loading, GUI transaction, enchantment application, and socket metadata. KnowledgeBound is an optional progression provider; it never owns jewel recipes or item data.

## Server setup

Install Teapot Cosmetics and its declared dependencies. Put the enchantment datapack in the world datapacks directory. KnowledgeBound may be installed to enforce Jeweller tiers and tier-based socket limits.

Configuration is generated at `config/teapot_jeweling.json`. Important defaults are:

- Jeweling enabled.
- KnowledgeBound optional, with progression used when present.
- Three fallback sockets when KnowledgeBound is absent.
- Vanilla enchantment compatibility checks enabled.
- Open tables close when data or configuration is reloaded.

Use `/reload` after changing enchantments, item tags, or Jeweling recipes. Use `/teapot jeweling reload` after changing only `teapot_jeweling.json`; this also closes open tables so no stale preview can be taken.

## Recipe format

Recipes are server data resources under `data/<namespace>/jeweling_recipes/*.json`. The resource path is the stable recipe ID.

```json
{
  "jewel": "teapot_cosmetics:ruby",
  "enchantment": "sidetopia:ruby_flame",
  "level": 1,
  "required_jeweller_tier": 0,
  "allowed_items": "#teapot_cosmetics:jewelable",
  "duplicate_policy": "reject"
}
```

Fields:

- `jewel`: required registered item ID.
- `enchantment`: required dynamic enchantment ID. It is resolved from the current server registry whenever the table is evaluated.
- `level`: required positive integer. Evaluation also rejects levels above the enchantment's current maximum.
- `required_jeweller_tier`: required integer from 0 through 3. It is ignored when KnowledgeBound progression is not active.
- `allowed_items`: optional item ID or item-tag selector. It defaults to `#teapot_cosmetics:jewelable`.
- `duplicate_policy`: optional `reject` or `replace_if_higher`; the default is `reject`.

Unknown fields, invalid IDs, unknown item IDs, invalid numbers, and unknown policies reject the complete recipe reload. The previous immutable registry remains active. Multiple recipes may share a jewel only when exactly one matches a given equipment item; ambiguous matches are rejected at evaluation and logged with their recipe IDs.

## Adding a jewel mapping

1. Register the jewel item in Teapot and add it to `#teapot_cosmetics:jewels`.
2. Ensure the intended equipment is included in `#teapot_cosmetics:jewelable`.
3. Define the data-driven enchantment, including its own `supported_items` rules.
4. Add one or more Jeweling recipe files to the enchantment datapack.
5. Use precise, non-overlapping selectors if one jewel selects different enchantments by equipment category.
6. Run `/reload`, then inspect the mapping with `/teapot jeweling recipes` and `/teapot jeweling inspect` (offhand jewel, main-hand equipment).

A target must pass the broad Teapot tag, the recipe selector, the enchantment's supported-item rules, duplicate handling, compatibility checks, progression checks, and socket limit. The selector never overrides the enchantment definition.

The supplied Sidetopia datapack contains 30 recipes. Alexandrite, Beryl, and Flawless Emerald intentionally have no recipe because no matching enchantment definition exists; the implementation does not invent effects for them.

## Socket metadata

Successful output stores structured versioned metadata in `minecraft:custom_data`:

```text
teapot_jeweling: {
  version: 1,
  sockets: [{
    recipe: "sidetopia:ruby",
    jewel: "teapot_cosmetics:ruby",
    enchantment: "sidetopia:ruby_flame",
    level: 1
  }]
}
```

Unrelated custom data is preserved. Before counting sockets, records whose enchantments are no longer present on the item are ignored. Cleanup is written only to the generated output, never to the preview input. A successful structured write removes the obsolete top-level `knowledgebound_gem_count` number as a one-way migration; it does not infer jewel identities from that old count.

## KnowledgeBound integration

When KnowledgeBound is installed and progression integration is enabled, Teapot calls its public Jeweller API for the current tier, maximum sockets, structured permission result, and post-success XP. XP is requested only after the equipment and one jewel have been consumed and the output has been delivered.

Without KnowledgeBound, Jeweling uses `fallbackMaxSockets` and ignores recipe tiers. Setting `requireKnowledgeBound` to true disables the operation cleanly when KnowledgeBound is absent. If an installed KnowledgeBound version lacks the required API, progression fails closed instead of bypassing restrictions.

KnowledgeBound's smithing behavior applies only to genuine armor-trim changes. Armor trims do not consume jewel sockets or write Teapot metadata, and netherite upgrades do not grant Jeweller XP.

## Transaction safety

The result slot is a generated preview, not stored input. Every result click re-evaluates current data and compares the fresh output with the visible preview before consuming anything. One successful take consumes exactly one jewel and the single equipment item, then produces exactly one result. Closing, disconnecting, dying, moving away, breaking the table, disabling the feature, or reloading returns only real inputs; the preview is never returned.
