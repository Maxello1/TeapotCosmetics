package net.sidetopia.cosmetics.jeweling;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.sidetopia.cosmetics.TeapotCosmetics;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JewelingRecipeReloadListener
        implements SimpleSynchronousResourceReloadListener {
    private static final String DIRECTORY = "jeweling_recipes";
    private final JewelingRecipeRegistry registry;

    public JewelingRecipeReloadListener(JewelingRecipeRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(TeapotCosmetics.MOD_ID, "jeweling_recipes");
    }

    @Override
    public void reload(ResourceManager manager) {
        if (JewelingConfig.get().closeOpenTablesOnReload) {
            JewelingTableManager.closeAllForReload();
        }

        Map<Identifier, List<Resource>> resources = manager.findAllResources(
                DIRECTORY,
                id -> id.getPath().endsWith(".json")
        );
        applyReload(loadAllResources(resources, JewelingRecipe::decode));
    }

    void applyReload(LoadResult result) {
        result.issues().forEach(JewelingRecipeReloadListener::logIssue);

        try {
            registry.replace(result.recipes());
        } catch (IllegalArgumentException exception) {
            registry.replace(List.of());
            TeapotCosmetics.LOGGER.error(
                    "Could not install the current Jeweling recipes; the registry was cleared: {}",
                    errorMessage(exception),
                    exception
            );
            logEmptyWarning(
                    result.recipes().size() + result.issues().size()
            );
            return;
        }

        if (registry.isEmpty()) {
            logEmptyWarning(result.issues().size());
        } else if (result.issues().isEmpty()) {
            TeapotCosmetics.LOGGER.info(
                    "Loaded {} Jeweling recipes; 0 invalid recipes were skipped.",
                    registry.size()
            );
        } else {
            TeapotCosmetics.LOGGER.warn(
                    "Loaded {} Jeweling recipes; {} invalid recipes were skipped.",
                    registry.size(),
                    result.issues().size()
            );
        }
    }

    static LoadResult loadAllResources(
            Map<Identifier, List<Resource>> resources,
            RecipeDecoder decoder
    ) {
        List<JewelingRecipe> loaded = new ArrayList<>();
        List<ReloadIssue> issues = new ArrayList<>();
        Map<Identifier, LoadedRecipe> firstByRecipeId = new HashMap<>();
        Set<Identifier> duplicateIds = new HashSet<>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> entry.getValue().forEach(resource -> loadOne(
                        entry.getKey(),
                        resource,
                        decoder,
                        loaded,
                        issues,
                        firstByRecipeId,
                        duplicateIds
                )));

        return new LoadResult(List.copyOf(loaded), List.copyOf(issues));
    }

    static LoadResult loadResources(
            Map<Identifier, Resource> resources,
            RecipeDecoder decoder
    ) {
        List<JewelingRecipe> loaded = new ArrayList<>();
        List<ReloadIssue> issues = new ArrayList<>();
        Map<Identifier, LoadedRecipe> firstByRecipeId = new HashMap<>();
        Set<Identifier> duplicateIds = new HashSet<>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> loadOne(
                        entry.getKey(),
                        entry.getValue(),
                        decoder,
                        loaded,
                        issues,
                        firstByRecipeId,
                        duplicateIds
                ));

        return new LoadResult(List.copyOf(loaded), List.copyOf(issues));
    }

    private static void loadOne(
            Identifier resourceId,
            Resource resource,
            RecipeDecoder decoder,
            List<JewelingRecipe> loaded,
            List<ReloadIssue> issues,
            Map<Identifier, LoadedRecipe> firstByRecipeId,
            Set<Identifier> duplicateIds
    ) {
        String packId = resource.getPackId();
        try (Reader reader = resource.getReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JewelingRecipe recipe = decoder.decode(toRecipeId(resourceId), json);
            LoadedRecipe current = new LoadedRecipe(recipe, resourceId, packId);
            LoadedRecipe first = firstByRecipeId.putIfAbsent(recipe.id(), current);

            if (first == null) {
                loaded.add(recipe);
                return;
            }

            if (duplicateIds.add(recipe.id())) {
                loaded.remove(first.recipe());
                issues.add(duplicateIssue(first, current));
            }
            issues.add(duplicateIssue(current, first));
        } catch (Exception exception) {
            issues.add(new ReloadIssue(
                    resourceId,
                    packId,
                    errorMessage(exception),
                    exception
            ));
        }
    }

    private static ReloadIssue duplicateIssue(
            LoadedRecipe source,
            LoadedRecipe conflicting
    ) {
        return new ReloadIssue(
                source.resourceId(),
                source.packId(),
                "duplicate recipe id '"
                        + source.recipe().id()
                        + "'; also provided by resource "
                        + conflicting.resourceId()
                        + " from pack "
                        + conflicting.packId(),
                null
        );
    }

    private static void logIssue(ReloadIssue issue) {
        if (issue.cause() == null) {
            TeapotCosmetics.LOGGER.error(
                    "Invalid Jeweling recipe: {} from pack {}: {}",
                    issue.resourceId(),
                    issue.packId(),
                    issue.message()
            );
            return;
        }
        TeapotCosmetics.LOGGER.error(
                "Invalid Jeweling recipe: {} from pack {}: {}",
                issue.resourceId(),
                issue.packId(),
                issue.message(),
                issue.cause()
        );
    }

    private static void logEmptyWarning(int invalidCount) {
        TeapotCosmetics.LOGGER.warn(
                "[TeapotCosmetics] WARNING: Loaded 0 Jeweling recipes.\n"
                        + "{} invalid recipes were skipped.\n"
                        + "The Gem Table can accept inputs, but cannot produce outputs.\n"
                        + "Expected resources under data/<namespace>/jeweling_recipes/*.json",
                invalidCount
        );
    }

    private static String errorMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getName()
                : message;
    }

    private static Identifier toRecipeId(Identifier resourceId) {
        String path = resourceId.getPath();
        String prefix = DIRECTORY + "/";
        String relative = path.substring(prefix.length(), path.length() - ".json".length());
        return Identifier.of(resourceId.getNamespace(), relative);
    }

    @FunctionalInterface
    interface RecipeDecoder {
        JewelingRecipe decode(Identifier id, JsonObject json);
    }

    record LoadResult(List<JewelingRecipe> recipes, List<ReloadIssue> issues) { }

    record ReloadIssue(
            Identifier resourceId,
            String packId,
            String message,
            Exception cause
    ) { }

    private record LoadedRecipe(
            JewelingRecipe recipe,
            Identifier resourceId,
            String packId
    ) { }
}
