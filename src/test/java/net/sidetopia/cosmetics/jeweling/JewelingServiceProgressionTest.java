package net.sidetopia.cosmetics.jeweling;

import net.sidetopia.cosmetics.compat.KnowledgeBoundBridge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JewelingServiceProgressionTest {
    @Test
    void operatorsBypassOnlyTierAndSocketFailuresWhenEnabled() {
        for (KnowledgeBoundBridge.FailureReason reason
                : KnowledgeBoundBridge.FailureReason.values()) {
            boolean expected = reason == KnowledgeBoundBridge.FailureReason.TIER_TOO_LOW
                    || reason == KnowledgeBoundBridge.FailureReason.SOCKET_LIMIT_REACHED;

            assertEquals(
                    expected,
                    JewelingService.canOperatorBypassProgression(true, true, reason),
                    reason.name()
            );
        }
    }

    @Test
    void disabledModeAndNonOperatorsNeverBypassProgression() {
        for (KnowledgeBoundBridge.FailureReason reason
                : KnowledgeBoundBridge.FailureReason.values()) {
            assertFalse(
                    JewelingService.canOperatorBypassProgression(false, true, reason)
            );
            assertFalse(
                    JewelingService.canOperatorBypassProgression(true, false, reason)
            );
        }
    }
}
