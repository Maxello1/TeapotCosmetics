package net.sidetopia.cosmetics.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.sidetopia.cosmetics.TeapotCosmetics;
import net.sidetopia.cosmetics.jeweling.JewelingConfig;

public final class KnowledgeBoundIntegration {

    private KnowledgeBoundIntegration() {
    }

    public static KnowledgeBoundBridge create() {
        boolean present = FabricLoader.getInstance().isModLoaded("knowledgebound");
        if (!present || !JewelingConfig.get().useKnowledgeBoundProgression) {
            return new NoKnowledgeBoundBridge(present);
        }

        try {
            return new KnowledgeBoundCompat();
        } catch (ReflectiveOperationException | LinkageError exception) {
            TeapotCosmetics.LOGGER.error(
                    "KnowledgeBound is installed but its Jeweller API is unavailable; Jeweling progression is disabled",
                    exception
            );
            return new UnavailableKnowledgeBoundBridge();
        }
    }

    private static final class UnavailableKnowledgeBoundBridge implements KnowledgeBoundBridge {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public JewellerPermission check(
                net.minecraft.server.network.ServerPlayerEntity player,
                int requiredTier,
                int currentSockets
        ) {
            return new JewellerPermission(
                    false,
                    FailureReason.SYSTEM_DISABLED,
                    0,
                    requiredTier,
                    currentSockets,
                    0
            );
        }

        @Override
        public void grantSuccessXp(net.minecraft.server.network.ServerPlayerEntity player) {
        }
    }
}
