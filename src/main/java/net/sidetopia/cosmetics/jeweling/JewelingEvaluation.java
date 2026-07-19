package net.sidetopia.cosmetics.jeweling;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.Optional;

public record JewelingEvaluation(
        JewelingStatus status,
        Optional<JewelingRecipe> recipe,
        Optional<RegistryEntry<Enchantment>> enchantment,
        ItemStack output,
        int requiredTier,
        int playerTier,
        int currentSockets,
        int maximumSockets,
        Text failureMessage
) {
    public static JewelingEvaluation failure(JewelingStatus status, Text message) {
        return new JewelingEvaluation(
                status,
                Optional.empty(),
                Optional.empty(),
                ItemStack.EMPTY,
                0,
                0,
                0,
                0,
                message
        );
    }

    public boolean ready() {
        return status == JewelingStatus.READY && !output.isEmpty();
    }
}
