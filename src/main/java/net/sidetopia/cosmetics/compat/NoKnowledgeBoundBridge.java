package net.sidetopia.cosmetics.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.sidetopia.cosmetics.jeweling.JewelingConfig;

public final class NoKnowledgeBoundBridge implements KnowledgeBoundBridge {
    private final boolean knowledgeBoundPresent;

    public NoKnowledgeBoundBridge(boolean knowledgeBoundPresent) {
        this.knowledgeBoundPresent = knowledgeBoundPresent;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public JewellerPermission check(
            ServerPlayerEntity player,
            int requiredTier,
            int currentSockets
    ) {
        JewelingConfig config = JewelingConfig.get();
        int sockets = Math.max(0, currentSockets);
        int maximum = Math.max(0, config.fallbackMaxSockets);
        if (config.requireKnowledgeBound && !knowledgeBoundPresent) {
            return new JewellerPermission(
                    false,
                    FailureReason.KNOWLEDGEBOUND_REQUIRED,
                    0,
                    requiredTier,
                    sockets,
                    maximum
            );
        }
        if (sockets >= maximum) {
            return new JewellerPermission(
                    false,
                    FailureReason.SOCKET_LIMIT_REACHED,
                    0,
                    requiredTier,
                    sockets,
                    maximum
            );
        }
        return new JewellerPermission(
                true,
                FailureReason.ALLOWED,
                0,
                requiredTier,
                sockets,
                maximum
        );
    }

    @Override
    public void grantSuccessXp(ServerPlayerEntity player) {
    }
}
