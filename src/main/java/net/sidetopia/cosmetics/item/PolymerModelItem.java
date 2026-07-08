package net.sidetopia.cosmetics.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PolymerModelItem extends Item implements PolymerItem {
    private final Item polymerItem;
    private final int customModelData;

    public PolymerModelItem(Settings settings, Item polymerItem, int customModelData) {
        super(settings);
        this.polymerItem = polymerItem;
        this.customModelData = customModelData;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerItem;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.customModelData;
    }
}
