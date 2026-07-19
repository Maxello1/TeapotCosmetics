package net.sidetopia.cosmetics.jeweling;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.sidetopia.cosmetics.commands.JewelingCommands;
import net.sidetopia.cosmetics.compat.KnowledgeBoundBridge;
import net.sidetopia.cosmetics.compat.KnowledgeBoundIntegration;

import java.util.HashSet;
import java.util.Set;

public final class JewelingTableManager {
    private static final JewelingRecipeRegistry RECIPES = new JewelingRecipeRegistry();
    private static final Set<JewelingTableGui> OPEN_TABLES = new HashSet<>();
    private static volatile KnowledgeBoundBridge knowledgeBound;
    private static volatile JewelingService service;

    private JewelingTableManager() {
    }

    public static void initialize() {
        JewelingConfig.load();
        rebuildIntegration();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new JewelingRecipeReloadListener(RECIPES));
        JewelingCommands.register();
    }

    public static void open(
            ServerPlayerEntity player,
            ServerWorld world,
            BlockPos tablePos
    ) {
        new JewelingTableGui(player, world, tablePos).open();
    }

    public static JewelingRecipeRegistry recipes() {
        return RECIPES;
    }

    public static JewelingService service() {
        return service;
    }

    public static synchronized void reloadConfiguration() {
        closeAllForReload();
        JewelingConfig.load();
        rebuildIntegration();
    }

    static synchronized void register(JewelingTableGui gui) {
        OPEN_TABLES.add(gui);
    }

    static synchronized void unregister(JewelingTableGui gui) {
        OPEN_TABLES.remove(gui);
    }

    public static void closeAllForReload() {
        JewelingTableGui[] sessions;
        synchronized (JewelingTableManager.class) {
            sessions = OPEN_TABLES.toArray(JewelingTableGui[]::new);
        }
        for (JewelingTableGui session : sessions) {
            session.closeForReload();
        }
    }

    private static void rebuildIntegration() {
        knowledgeBound = KnowledgeBoundIntegration.create();
        service = new JewelingService(RECIPES, knowledgeBound);
    }
}
