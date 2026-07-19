package net.sidetopia.cosmetics.jeweling;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JewelingRecipeTest {
    private static final Identifier RECIPE_ID = Identifier.of("test", "ruby");

    @Test
    void decodesValidRecipeAndDefaults() {
        JewelingRecipe recipe = decode("""
                {
                  "jewel": "teapot_cosmetics:ruby",
                  "enchantment": "sidetopia:ruby_flame",
                  "level": 1,
                  "required_jeweller_tier": 2
                }
                """);

        assertEquals(RECIPE_ID, recipe.id());
        assertEquals(Identifier.of("teapot_cosmetics", "ruby"), recipe.jewel());
        assertEquals(Identifier.of("sidetopia", "ruby_flame"), recipe.enchantment());
        assertEquals(1, recipe.level());
        assertEquals(2, recipe.requiredJewellerTier());
        assertEquals("#teapot_cosmetics:jewelable", recipe.allowedItems());
        assertEquals(JewelingRecipe.DuplicatePolicy.REJECT, recipe.duplicatePolicy());
    }

    @Test
    void decodesItemAndTagSelectors() {
        JewelingRecipe item = decode(baseJson()
                .replace("}", ", \"allowed_items\": \"minecraft:diamond_sword\"}"));
        JewelingRecipe tag = decode(baseJson()
                .replace("}", ", \"allowed_items\": \"#minecraft:enchantable/sword\", "
                        + "\"duplicate_policy\": \"replace_if_higher\"}"));

        assertEquals("minecraft:diamond_sword", item.allowedItems());
        assertEquals("#minecraft:enchantable/sword", tag.allowedItems());
        assertEquals(JewelingRecipe.DuplicatePolicy.REPLACE_IF_HIGHER, tag.duplicatePolicy());
    }

    @Test
    void rejectsMissingFieldsAndInvalidIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> decode("""
                {"enchantment":"sidetopia:ruby_flame","level":1,"required_jeweller_tier":0}
                """));
        assertThrows(IllegalArgumentException.class, () -> decode("""
                {"jewel":"teapot_cosmetics:ruby","level":1,"required_jeweller_tier":0}
                """));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson()
                .replace("teapot_cosmetics:ruby", "Bad Identifier")));
    }

    @Test
    void rejectsInvalidNumbersAndPolicy() {
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson().replace("\"level\":1", "\"level\":0")));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson().replace("\"level\":1", "\"level\":-1")));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson().replace("\"level\":1", "\"level\":1.5")));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson().replace("\"required_jeweller_tier\":0", "\"required_jeweller_tier\":-1")));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson().replace("\"required_jeweller_tier\":0", "\"required_jeweller_tier\":4")));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson()
                .replace("}", ", \"duplicate_policy\": \"stack\"}")));
    }

    @Test
    void rejectsUnknownItemsAndFields() {
        Predicate<Identifier> knownItems = id -> !id.getNamespace().equals("missing");
        assertThrows(IllegalArgumentException.class, () -> decode(
                baseJson().replace("teapot_cosmetics:ruby", "missing:gem"),
                knownItems
        ));
        assertThrows(IllegalArgumentException.class, () -> decode(
                baseJson().replace("}", ", \"allowed_items\": \"missing:sword\"}"),
                knownItems
        ));
        assertThrows(IllegalArgumentException.class, () -> decode(baseJson()
                .replace("}", ", \"surprise\": true}")));
    }

    @Test
    void registryRejectsDuplicateResourceIds() {
        JewelingRecipe recipe = decode(baseJson());
        JewelingRecipeRegistry registry = new JewelingRecipeRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.replace(List.of(recipe, recipe)));
    }

    private static JewelingRecipe decode(String json) {
        return decode(json, ignored -> true);
    }

    private static JewelingRecipe decode(String json, Predicate<Identifier> knownItem) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return JewelingRecipe.decode(RECIPE_ID, object, knownItem);
    }

    private static String baseJson() {
        return """
                {"jewel":"teapot_cosmetics:ruby","enchantment":"sidetopia:ruby_flame","level":1,"required_jeweller_tier":0}
                """;
    }
}
