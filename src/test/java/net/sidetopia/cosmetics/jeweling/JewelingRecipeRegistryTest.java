package net.sidetopia.cosmetics.jeweling;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JewelingRecipeRegistryTest {
    @Test
    void startsEmptyAndExposesSize() {
        JewelingRecipeRegistry registry = new JewelingRecipeRegistry();

        assertTrue(registry.isEmpty());
        assertEquals(0, registry.size());
        assertEquals(List.of(), registry.all());
    }

    @Test
    void replacesWithOneAndMultipleRecipes() {
        JewelingRecipeRegistry registry = new JewelingRecipeRegistry();
        JewelingRecipe sapphire = recipe("sapphire", "sapphire");
        JewelingRecipe ruby = recipe("ruby", "ruby");

        registry.replace(List.of(ruby));

        assertFalse(registry.isEmpty());
        assertEquals(1, registry.size());
        assertEquals(List.of(ruby), registry.all());

        registry.replace(List.of(sapphire, ruby));

        assertEquals(2, registry.size());
        assertEquals(List.of(ruby, sapphire), registry.all());
    }

    @Test
    void emptyReplacementClearsThePreviousSnapshot() {
        JewelingRecipeRegistry registry = new JewelingRecipeRegistry();
        registry.replace(List.of(recipe("ruby", "ruby")));

        registry.replace(List.of());

        assertTrue(registry.isEmpty());
        assertEquals(0, registry.size());
    }

    @Test
    void duplicateReplacementIsRejectedWithoutPartialMutation() {
        JewelingRecipeRegistry registry = new JewelingRecipeRegistry();
        JewelingRecipe original = recipe("original", "ruby");
        JewelingRecipe duplicate = recipe("duplicate", "sapphire");
        registry.replace(List.of(original));

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.replace(List.of(duplicate, duplicate))
        );

        assertEquals(List.of(original), registry.all());
    }

    private static JewelingRecipe recipe(String recipePath, String jewelPath) {
        JsonObject json = JsonParser.parseString("""
                {
                  "jewel": "teapot_cosmetics:%s",
                  "enchantment": "sidetopia:%s_effect",
                  "level": 1,
                  "required_jeweller_tier": 0
                }
                """.formatted(jewelPath, jewelPath)).getAsJsonObject();
        return JewelingRecipe.decode(
                Identifier.of("test", recipePath),
                json,
                ignored -> true
        );
    }
}
