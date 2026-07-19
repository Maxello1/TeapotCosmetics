package net.sidetopia.cosmetics.jeweling;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class JewelingTags {

    private JewelingTags() {
    }

    public static TagKey<Item> jewels() {
        return TagKey.of(RegistryKeys.ITEM, JewelingConfig.get().jewelTagId());
    }

    public static TagKey<Item> jewelable() {
        return TagKey.of(RegistryKeys.ITEM, JewelingConfig.get().jewelableTagId());
    }
}
