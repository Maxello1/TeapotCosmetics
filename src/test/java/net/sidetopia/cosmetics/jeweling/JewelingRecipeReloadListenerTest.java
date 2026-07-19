package net.sidetopia.cosmetics.jeweling;

import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JewelingRecipeReloadListenerTest {
    private static final Identifier VALID_RESOURCE = Identifier.of(
            "test",
            "jeweling_recipes/valid.json"
    );
    private static final Identifier BROKEN_RESOURCE = Identifier.of(
            "test",
            "jeweling_recipes/broken.json"
    );
    private static final JewelingRecipeReloadListener.RecipeDecoder DECODER =
            (id, json) -> JewelingRecipe.decode(id, json, ignored -> true);

    @Test
    void zeroResourcesReplaceStaleRecipesWithAnEmptySnapshot() {
        JewelingRecipeRegistry registry = registryWithStaleRecipe();
        JewelingRecipeReloadListener listener = new JewelingRecipeReloadListener(registry);

        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadResources(Map.of(), DECODER);
        listener.applyReload(result);

        assertEquals(0, result.recipes().size());
        assertEquals(0, result.issues().size());
        assertTrue(registry.isEmpty());
    }

    @Test
    void malformedResourcePreservesItsIdPackAndExactError() {
        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadResources(
                        Map.of(BROKEN_RESOURCE, resource("broken-pack", "{")),
                        DECODER
                );

        assertEquals(0, result.recipes().size());
        assertEquals(1, result.issues().size());
        JewelingRecipeReloadListener.ReloadIssue issue = result.issues().getFirst();
        assertEquals(BROKEN_RESOURCE, issue.resourceId());
        assertEquals("broken-pack", issue.packId());
        assertFalse(issue.message().isBlank());
        assertEquals(issue.cause().getMessage(), issue.message());
    }

    @Test
    void validRecipeRemainsActiveWhenAnotherResourceIsMalformed() {
        JewelingRecipeRegistry registry = registryWithStaleRecipe();
        JewelingRecipeReloadListener listener = new JewelingRecipeReloadListener(registry);
        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadResources(
                        Map.of(
                                VALID_RESOURCE, resource("valid-pack", validJson()),
                                BROKEN_RESOURCE, resource("broken-pack", "{")
                        ),
                        DECODER
                );

        listener.applyReload(result);

        assertEquals(1, result.recipes().size());
        assertEquals(1, result.issues().size());
        assertEquals(1, registry.size());
        assertEquals(Identifier.of("test", "valid"), registry.all().getFirst().id());
    }

    @Test
    void invalidEnchantmentIdentifierIsSkipped() {
        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadResources(
                        Map.of(
                                BROKEN_RESOURCE,
                                resource(
                                        "identifier-pack",
                                        validJson().replace(
                                                "sidetopia:ruby_flame",
                                                "Bad Identifier"
                                        )
                                )
                        ),
                        DECODER
                );

        assertEquals(0, result.recipes().size());
        assertEquals(1, result.issues().size());
        assertEquals(
                "invalid enchantment identifier 'Bad Identifier'",
                result.issues().getFirst().message()
        );
    }

    @Test
    void unknownJewelIsSkipped() {
        JewelingRecipeReloadListener.RecipeDecoder knownItems =
                (id, json) -> JewelingRecipe.decode(
                        id,
                        json,
                        item -> item.equals(Identifier.of("minecraft", "diamond_sword"))
                );
        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadResources(
                        Map.of(BROKEN_RESOURCE, resource("unknown-pack", validJson())),
                        knownItems
                );

        assertEquals(0, result.recipes().size());
        assertEquals(1, result.issues().size());
        assertEquals(
                "unknown jewel item 'teapot_cosmetics:ruby'",
                result.issues().getFirst().message()
        );
    }

    @Test
    void sameResourceFromTwoPacksReportsBothOrigins() {
        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadAllResources(
                        Map.of(
                                VALID_RESOURCE,
                                List.of(
                                        resource("base-pack", validJson()),
                                        resource("override-pack", validJson())
                                )
                        ),
                        DECODER
                );

        assertEquals(0, result.recipes().size());
        assertEquals(2, result.issues().size());
        assertTrue(result.issues().stream().allMatch(
                issue -> issue.resourceId().equals(VALID_RESOURCE)
        ));
        assertEquals(
                Set.of("base-pack", "override-pack"),
                result.issues().stream()
                        .map(JewelingRecipeReloadListener.ReloadIssue::packId)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void duplicateIdsReportBothOriginsAndClearStaleRegistry() {
        Identifier firstResource = Identifier.of(
                "test",
                "jeweling_recipes/first.json"
        );
        Identifier secondResource = Identifier.of(
                "test",
                "jeweling_recipes/second.json"
        );
        Identifier duplicateId = Identifier.of("test", "duplicate");
        JewelingRecipeReloadListener.RecipeDecoder duplicateDecoder =
                (ignored, json) -> JewelingRecipe.decode(
                        duplicateId,
                        json,
                        item -> true
                );
        JewelingRecipeReloadListener.LoadResult result =
                JewelingRecipeReloadListener.loadResources(
                        Map.of(
                                firstResource, resource("first-pack", validJson()),
                                secondResource, resource("second-pack", validJson())
                        ),
                        duplicateDecoder
                );

        JewelingRecipeRegistry registry = registryWithStaleRecipe();
        new JewelingRecipeReloadListener(registry).applyReload(result);

        assertEquals(0, result.recipes().size());
        assertEquals(2, result.issues().size());
        assertEquals(
                Set.of(firstResource, secondResource),
                result.issues().stream()
                        .map(JewelingRecipeReloadListener.ReloadIssue::resourceId)
                        .collect(java.util.stream.Collectors.toSet())
        );
        assertTrue(result.issues().stream().allMatch(
                issue -> issue.message().contains("duplicate recipe id 'test:duplicate'")
        ));
        assertTrue(result.issues().stream().anyMatch(
                issue -> issue.message().contains("from pack first-pack")
        ));
        assertTrue(result.issues().stream().anyMatch(
                issue -> issue.message().contains("from pack second-pack")
        ));
        assertTrue(registry.isEmpty());
    }

    @Test
    void unexpectedRegistryDuplicateClearsStaleSnapshotWithoutThrowing() {
        JewelingRecipe duplicate = staleRecipe();
        JewelingRecipeReloadListener.LoadResult invalidResult =
                new JewelingRecipeReloadListener.LoadResult(
                        List.of(duplicate, duplicate),
                        List.of()
                );
        JewelingRecipeRegistry registry = registryWithStaleRecipe();
        JewelingRecipeReloadListener listener = new JewelingRecipeReloadListener(registry);

        assertDoesNotThrow(() -> listener.applyReload(invalidResult));
        assertTrue(registry.isEmpty());
    }

    private static JewelingRecipeRegistry registryWithStaleRecipe() {
        JewelingRecipeRegistry registry = new JewelingRecipeRegistry();
        registry.replace(List.of(staleRecipe()));
        return registry;
    }

    private static JewelingRecipe staleRecipe() {
        return JewelingRecipe.decode(
                Identifier.of("test", "stale"),
                com.google.gson.JsonParser.parseString(validJson()).getAsJsonObject(),
                ignored -> true
        );
    }

    private static String validJson() {
        return """
                {
                  "jewel": "teapot_cosmetics:ruby",
                  "enchantment": "sidetopia:ruby_flame",
                  "level": 1,
                  "required_jeweller_tier": 0
                }
                """;
    }

    private static Resource resource(String packId, String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return new Resource(
                new TestResourcePack(packId),
                () -> new ByteArrayInputStream(bytes)
        );
    }

    private record TestResourcePack(String id) implements ResourcePack {
        @Override
        public String getId() {
            return id;
        }

        @Override
        public InputSupplier<InputStream> openRoot(String... segments) {
            return null;
        }

        @Override
        public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
            return null;
        }

        @Override
        public void findResources(
                ResourceType type,
                String namespace,
                String prefix,
                ResultConsumer consumer
        ) { }

        @Override
        public Set<String> getNamespaces(ResourceType type) {
            return Set.of();
        }

        @Override
        public <T> T parseMetadata(ResourceMetadataReader<T> metaReader)
                throws IOException {
            return null;
        }

        @Override
        public ResourcePackInfo getInfo() {
            return null;
        }

        @Override
        public void close() { }
    }
}
