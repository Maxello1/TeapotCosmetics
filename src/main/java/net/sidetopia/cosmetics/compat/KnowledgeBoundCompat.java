package net.sidetopia.cosmetics.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.sidetopia.cosmetics.TeapotCosmetics;

import java.lang.reflect.Method;

/**
 * Reflection keeps KnowledgeBound entirely optional at both compile time and class load time.
 */
public final class KnowledgeBoundCompat implements KnowledgeBoundBridge {
    private final Method canApply;
    private final Method grantXp;
    private final Method allowed;
    private final Method reason;
    private final Method playerTier;
    private final Method requiredTier;
    private final Method currentSockets;
    private final Method maximumSockets;

    public KnowledgeBoundCompat() throws ReflectiveOperationException {
        Class<?> api = Class.forName("net.maxello.knowledgebound.api.JewellerApi");
        canApply = api.getMethod(
                "canApply",
                ServerPlayerEntity.class,
                int.class,
                int.class
        );
        grantXp = api.getMethod("grantSuccessfulJewelingXp", ServerPlayerEntity.class);

        Class<?> result = Class.forName(
                "net.maxello.knowledgebound.api.JewellerApi$JewellerCheckResult"
        );
        allowed = result.getMethod("allowed");
        reason = result.getMethod("reason");
        playerTier = result.getMethod("playerTier");
        requiredTier = result.getMethod("requiredTier");
        currentSockets = result.getMethod("currentSockets");
        maximumSockets = result.getMethod("maximumSockets");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public JewellerPermission check(
            ServerPlayerEntity player,
            int minimumTier,
            int socketCount
    ) {
        try {
            Object result = canApply.invoke(null, player, minimumTier, socketCount);
            return new JewellerPermission(
                    (boolean) allowed.invoke(result),
                    mapReason(reason.invoke(result).toString()),
                    (int) playerTier.invoke(result),
                    (int) requiredTier.invoke(result),
                    (int) currentSockets.invoke(result),
                    (int) maximumSockets.invoke(result)
            );
        } catch (ReflectiveOperationException exception) {
            TeapotCosmetics.LOGGER.error("KnowledgeBound Jeweller check failed", exception);
            return new JewellerPermission(
                    false,
                    FailureReason.SYSTEM_DISABLED,
                    0,
                    minimumTier,
                    socketCount,
                    0
            );
        }
    }

    @Override
    public void grantSuccessXp(ServerPlayerEntity player) {
        try {
            grantXp.invoke(null, player);
        } catch (ReflectiveOperationException exception) {
            TeapotCosmetics.LOGGER.error("KnowledgeBound Jeweller XP grant failed", exception);
        }
    }

    private static FailureReason mapReason(String name) {
        try {
            return FailureReason.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return FailureReason.SYSTEM_DISABLED;
        }
    }
}
