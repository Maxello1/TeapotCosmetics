package net.sidetopia.cosmetics.jeweling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Predicate;

public final class JewelingRecipe {
    private static final String DEFAULT_ALLOWED_ITEMS = "#teapot_cosmetics:jewelable";
    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "jewel",
            "enchantment",
            "level",
            "required_jeweller_tier",
            "allowed_items",
            "duplicate_policy"
    );

    private final Identifier id;
    private final Identifier jewel;
    private final Identifier enchantment;
    private final int level;
    private final int requiredJewellerTier;
    private final ItemSelector allowedItems;
    private final DuplicatePolicy duplicatePolicy;

    private JewelingRecipe(
            Identifier id,
            Identifier jewel,
            Identifier enchantment,
            int level,
            int requiredJewellerTier,
            ItemSelector allowedItems,
            DuplicatePolicy duplicatePolicy
    ) {
        this.id = id;
        this.jewel = jewel;
        this.enchantment = enchantment;
        this.level = level;
        this.requiredJewellerTier = requiredJewellerTier;
        this.allowedItems = allowedItems;
        this.duplicatePolicy = duplicatePolicy;
    }

    public static JewelingRecipe decode(Identifier id, JsonObject json) {
        return decode(id, json, Registries.ITEM::containsId);
    }

    static JewelingRecipe decode(
            Identifier id,
            JsonObject json,
            Predicate<Identifier> knownItem
    ) {
        if (id == null) throw new IllegalArgumentException("recipe id is required");
        if (json == null) throw new IllegalArgumentException("recipe body is required");

        for (String field : json.keySet()) {
            if (!ALLOWED_FIELDS.contains(field)) {
                throw new IllegalArgumentException("unknown field '" + field + "'");
            }
        }

        Identifier jewel = identifier(requiredString(json, "jewel"), "jewel");
        if (!knownItem.test(jewel)) {
            throw new IllegalArgumentException("unknown jewel item '" + jewel + "'");
        }

        Identifier enchantment = identifier(
                requiredString(json, "enchantment"),
                "enchantment"
        );
        int level = requiredInt(json, "level");
        if (level <= 0) {
            throw new IllegalArgumentException("level must be positive");
        }

        int requiredTier = requiredInt(json, "required_jeweller_tier");
        if (requiredTier < 0 || requiredTier > 3) {
            throw new IllegalArgumentException("required_jeweller_tier must be between 0 and 3");
        }

        String selectorValue = optionalString(
                json,
                "allowed_items",
                DEFAULT_ALLOWED_ITEMS
        );
        ItemSelector selector = ItemSelector.decode(selectorValue, knownItem);
        DuplicatePolicy duplicatePolicy = DuplicatePolicy.decode(
                optionalString(json, "duplicate_policy", "reject")
        );

        return new JewelingRecipe(
                id,
                jewel,
                enchantment,
                level,
                requiredTier,
                selector,
                duplicatePolicy
        );
    }

    public Identifier id() { return id; }

    public Identifier jewel() { return jewel; }

    public Identifier enchantment() { return enchantment; }

    public int level() { return level; }

    public int requiredJewellerTier() { return requiredJewellerTier; }

    public String allowedItems() { return allowedItems.serialized(); }

    public DuplicatePolicy duplicatePolicy() { return duplicatePolicy; }

    public boolean matches(ItemStack stack) { return allowedItems.matches(stack); }

    private static String requiredString(JsonObject json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("missing field '" + field + "'");
        JsonElement value = json.get(field);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("field '" + field + "' must be a string");
        }
        String result = value.getAsString();
        if (result.isBlank()) throw new IllegalArgumentException("field '" + field + "' is blank");
        return result;
    }

    private static String optionalString(JsonObject json, String field, String fallback) {
        return json.has(field) ? requiredString(json, field) : fallback;
    }

    private static int requiredInt(JsonObject json, String field) {
        if (!json.has(field)) throw new IllegalArgumentException("missing field '" + field + "'");
        JsonElement value = json.get(field);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("field '" + field + "' must be an integer");
        }
        try {
            int result = value.getAsInt();
            if (value.getAsDouble() != result) {
                throw new IllegalArgumentException("field '" + field + "' must be an integer");
            }
            return result;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("field '" + field + "' must be an integer", exception);
        }
    }

    private static Identifier identifier(String value, String field) {
        Identifier id = Identifier.tryParse(value);
        if (id == null) throw new IllegalArgumentException("invalid " + field + " identifier '" + value + "'");
        return id;
    }

    public enum DuplicatePolicy {
        REJECT,
        REPLACE_IF_HIGHER;

        private static DuplicatePolicy decode(String value) {
            return switch (value) {
                case "reject" -> REJECT;
                case "replace_if_higher" -> REPLACE_IF_HIGHER;
                default -> throw new IllegalArgumentException("unknown duplicate_policy '" + value + "'");
            };
        }
    }

    private record ItemSelector(Identifier id, boolean tag) {
        private static ItemSelector decode(String value, Predicate<Identifier> knownItem) {
            boolean tag = value.startsWith("#");
            String identifierText = tag ? value.substring(1) : value;
            Identifier id = identifier(identifierText, "allowed_items");
            if (!tag && !knownItem.test(id)) {
                throw new IllegalArgumentException("unknown allowed_items item '" + id + "'");
            }
            return new ItemSelector(id, tag);
        }

        private boolean matches(ItemStack stack) {
            if (stack.isEmpty()) return false;
            return tag
                    ? stack.isIn(TagKey.of(RegistryKeys.ITEM, id))
                    : stack.isOf(Registries.ITEM.get(id));
        }

        private String serialized() {
            return (tag ? "#" : "") + id;
        }
    }
}
