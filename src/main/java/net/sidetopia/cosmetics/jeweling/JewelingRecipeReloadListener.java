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
import java.util.List;
import java.util.Map;

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

        Map<Identifier, Resource> resources = manager.findResources(
                DIRECTORY,
                id -> id.getPath().endsWith(".json")
        );
        List<JewelingRecipe> loaded = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> loadOne(entry.getKey(), entry.getValue(), loaded, errors));

        if (!errors.isEmpty()) {
            errors.forEach(error -> TeapotCosmetics.LOGGER.error("Invalid Jeweling recipe: {}", error));
            throw new IllegalStateException(
                    "Refusing to replace Jeweling recipes because "
                            + errors.size()
                            + " resource(s) are invalid"
            );
        }

        registry.replace(loaded);
        TeapotCosmetics.LOGGER.info("Loaded {} Jeweling recipes", loaded.size());
    }

    private static void loadOne(
            Identifier resourceId,
            Resource resource,
            List<JewelingRecipe> loaded,
            List<String> errors
    ) {
        try (Reader reader = resource.getReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            loaded.add(JewelingRecipe.decode(toRecipeId(resourceId), json));
        } catch (Exception exception) {
            errors.add(
                    resourceId
                            + " from pack "
                            + resource.getPackId()
                            + ": "
                            + exception.getMessage()
            );
        }
    }

    private static Identifier toRecipeId(Identifier resourceId) {
        String path = resourceId.getPath();
        String prefix = DIRECTORY + "/";
        String relative = path.substring(prefix.length(), path.length() - ".json".length());
        return Identifier.of(resourceId.getNamespace(), relative);
    }
}
