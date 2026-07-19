package net.sidetopia.cosmetics.jeweling;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JewelingConfigTest {
    @Test
    void normalizesInvalidAndLegacyValuesWithoutFabricLoader() {
        JewelingConfig config = new JewelingConfig();
        config.gemTableBlockId = "Bad Identifier";
        config.jewelTag = null;
        config.jewelableTag = "teapot_cosmetics:equipment";
        config.successSound = "also invalid";
        config.fallbackMaxSockets = -10;
        config.successSoundVolume = Float.NaN;
        config.successSoundPitch = 0.0F;
        config.messages = null;

        config.normalize();

        assertEquals(Identifier.of("teapot_cosmetics", "gem_table"), config.gemTableBlockId());
        assertEquals(Identifier.of("teapot_cosmetics", "jewels"), config.jewelTagId());
        assertEquals(Identifier.of("teapot_cosmetics", "equipment"), config.jewelableTagId());
        assertEquals(Identifier.of("minecraft", "block.enchantment_table.use"), config.successSoundId());
        assertEquals(0, config.fallbackMaxSockets);
        assertEquals(1.0F, config.successSoundVolume);
        assertEquals(1.0F, config.successSoundPitch);
        assertNotNull(config.messages);
    }

    @Test
    void clampsExcessiveConfigurationValues() {
        JewelingConfig config = new JewelingConfig();
        config.fallbackMaxSockets = 999;
        config.successSoundVolume = 99.0F;
        config.successSoundPitch = 99.0F;

        config.normalize();

        assertEquals(64, config.fallbackMaxSockets);
        assertEquals(4.0F, config.successSoundVolume);
        assertEquals(2.0F, config.successSoundPitch);
    }
}
