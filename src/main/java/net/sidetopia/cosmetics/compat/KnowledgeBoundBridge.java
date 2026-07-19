package net.sidetopia.cosmetics.compat;

import net.minecraft.server.network.ServerPlayerEntity;

public interface KnowledgeBoundBridge {

    boolean isAvailable();

    JewellerPermission check(
            ServerPlayerEntity player,
            int requiredTier,
            int currentSockets
    );

    void grantSuccessXp(ServerPlayerEntity player);

    enum FailureReason {
        ALLOWED,
        KNOWLEDGEBOUND_REQUIRED,
        SYSTEM_DISABLED,
        TIER_TOO_LOW,
        SOCKET_LIMIT_REACHED
    }

    record JewellerPermission(
            boolean allowed,
            FailureReason reason,
            int playerTier,
            int requiredTier,
            int currentSockets,
            int maximumSockets
    ) {
    }
}
