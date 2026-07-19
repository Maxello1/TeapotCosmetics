package net.sidetopia.cosmetics.jeweling;

import net.minecraft.item.ItemStack;

import java.util.function.BooleanSupplier;

public final class JewelingInputRules {
    private JewelingInputRules() {
    }

    public static boolean isJewelInput(ItemStack stack) {
        return isJewelInput(
                stack.isEmpty(),
                () -> stack.isIn(JewelingTags.jewels())
        );
    }

    public static boolean isEquipmentInput(ItemStack stack) {
        return isEquipmentInput(
                stack.isEmpty(),
                () -> stack.isIn(JewelingTags.jewelable())
        );
    }

    static boolean isJewelInput(
            boolean empty,
            BooleanSupplier jewelTagMembership
    ) {
        return isTaggedInput(empty, jewelTagMembership);
    }

    static boolean isEquipmentInput(
            boolean empty,
            BooleanSupplier equipmentTagMembership
    ) {
        return isTaggedInput(empty, equipmentTagMembership);
    }

    private static boolean isTaggedInput(
            boolean empty,
            BooleanSupplier tagMembership
    ) {
        return !empty && tagMembership.getAsBoolean();
    }
}
