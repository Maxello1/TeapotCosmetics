package net.sidetopia.cosmetics.jeweling;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JewelingMetadataTest {
    @Test
    void readsNoMetadata() {
        assertTrue(JewelingMetadata.read(new NbtCompound()).isEmpty());
    }

    @Test
    void writesSocketsPreservesCustomDataAndRemovesLegacyCount() {
        NbtCompound original = new NbtCompound();
        original.putString("unrelated", "preserved");
        original.putInt("knowledgebound_gem_count", 7);
        JewelingMetadata.SocketRecord socket = new JewelingMetadata.SocketRecord(
                Identifier.of("sidetopia", "ruby"),
                Identifier.of("teapot_cosmetics", "ruby"),
                Identifier.of("sidetopia", "ruby_flame"),
                1
        );

        NbtCompound written = JewelingMetadata.writeRoot(original, List.of(socket));

        assertEquals("preserved", written.getString("unrelated"));
        assertFalse(written.contains("knowledgebound_gem_count"));
        assertEquals(List.of(socket), JewelingMetadata.read(written));
        assertTrue(original.contains("knowledgebound_gem_count"));
        assertFalse(original.contains("teapot_jeweling"));
    }

    @Test
    void roundTripsMultipleSockets() {
        JewelingMetadata.SocketRecord first = record("ruby", "ruby_flame");
        JewelingMetadata.SocketRecord second = record("sapphire", "sapphire_penetration");
        NbtCompound written = JewelingMetadata.writeRoot(
                new NbtCompound(),
                List.of(first, second)
        );
        assertEquals(List.of(first, second), JewelingMetadata.read(written));
    }

    @Test
    void ignoresMalformedEntriesAndUnsupportedVersions() {
        NbtCompound malformedRoot = new NbtCompound();
        NbtCompound malformedJeweling = new NbtCompound();
        malformedJeweling.putInt("version", JewelingMetadata.VERSION);
        NbtList malformedSockets = new NbtList();
        NbtCompound malformedSocket = new NbtCompound();
        malformedSocket.putString("recipe", "not an id");
        malformedSocket.putString("jewel", "teapot_cosmetics:ruby");
        malformedSocket.putString("enchantment", "sidetopia:ruby_flame");
        malformedSocket.putInt("level", 1);
        malformedSockets.add(malformedSocket);
        malformedJeweling.put("sockets", malformedSockets);
        malformedRoot.put("teapot_jeweling", malformedJeweling);
        assertTrue(JewelingMetadata.read(malformedRoot).isEmpty());

        malformedJeweling.putInt("version", 99);
        assertTrue(JewelingMetadata.read(malformedRoot).isEmpty());
    }

    @Test
    void emptyWriteRemovesOnlyJewelingAndLegacyKeys() {
        NbtCompound original = JewelingMetadata.writeRoot(
                new NbtCompound(),
                List.of(record("ruby", "ruby_flame"))
        );
        original.putString("unrelated", "preserved");
        original.putInt("knowledgebound_gem_count", 1);

        NbtCompound cleaned = JewelingMetadata.writeRoot(original, List.of());

        assertFalse(cleaned.contains("teapot_jeweling"));
        assertFalse(cleaned.contains("knowledgebound_gem_count"));
        assertEquals("preserved", cleaned.getString("unrelated"));
    }

    private static JewelingMetadata.SocketRecord record(String jewel, String enchantment) {
        return new JewelingMetadata.SocketRecord(
                Identifier.of("sidetopia", jewel),
                Identifier.of("teapot_cosmetics", jewel),
                Identifier.of("sidetopia", enchantment),
                1
        );
    }
}
