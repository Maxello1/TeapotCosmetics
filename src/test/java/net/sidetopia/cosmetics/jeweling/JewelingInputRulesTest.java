package net.sidetopia.cosmetics.jeweling;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JewelingInputRulesTest {
    @Test
    void namedRulesDependOnlyOnTheSuppliedTagMembership() {
        assertTrue(JewelingInputRules.isJewelInput(false, () -> true));
        assertFalse(JewelingInputRules.isJewelInput(false, () -> false));
        assertTrue(JewelingInputRules.isEquipmentInput(false, () -> true));
        assertFalse(JewelingInputRules.isEquipmentInput(false, () -> false));
    }

    @Test
    void emptyStacksAreRejectedBeforeTagMembershipIsConsulted() {
        AtomicInteger tagChecks = new AtomicInteger();
        assertFalse(JewelingInputRules.isJewelInput(
                true,
                () -> {
                    tagChecks.incrementAndGet();
                    return true;
                }
        ));
        assertFalse(JewelingInputRules.isEquipmentInput(
                true,
                () -> {
                    tagChecks.incrementAndGet();
                    return true;
                }
        ));

        assertEquals(0, tagChecks.get());
    }
}
