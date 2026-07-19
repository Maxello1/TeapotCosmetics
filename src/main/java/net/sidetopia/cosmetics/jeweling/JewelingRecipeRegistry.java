package net.sidetopia.cosmetics.jeweling;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JewelingRecipeRegistry {
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    public List<JewelingRecipe> find(ItemStack jewel) {
        if (jewel.isEmpty()) return List.of();
        return snapshot.byJewel().getOrDefault(Registries.ITEM.getId(jewel.getItem()), List.of());
    }

    public List<JewelingRecipe> all() {
        return snapshot.all();
    }

    public boolean isEmpty() {
        return snapshot.all().isEmpty();
    }

    public int size() {
        return snapshot.all().size();
    }

    public boolean hasRecipeFor(ItemStack jewel) {
        return !find(jewel).isEmpty();
    }

    public boolean matchesAnyEquipment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return snapshot.all().stream().anyMatch(recipe -> recipe.matches(stack));
    }

    public synchronized void replace(Collection<JewelingRecipe> recipes) {
        Set<Identifier> ids = new HashSet<>();
        Map<Identifier, List<JewelingRecipe>> mutableIndex = new HashMap<>();
        List<JewelingRecipe> sorted = new ArrayList<>(recipes);
        sorted.sort(Comparator.comparing(recipe -> recipe.id().toString()));

        for (JewelingRecipe recipe : sorted) {
            if (!ids.add(recipe.id())) {
                throw new IllegalArgumentException("duplicate recipe resource '" + recipe.id() + "'");
            }
            mutableIndex.computeIfAbsent(recipe.jewel(), ignored -> new ArrayList<>()).add(recipe);
        }

        Map<Identifier, List<JewelingRecipe>> immutableIndex = new HashMap<>();
        mutableIndex.forEach((jewel, values) -> immutableIndex.put(jewel, List.copyOf(values)));
        snapshot = new Snapshot(Map.copyOf(immutableIndex), List.copyOf(sorted));
    }

    private record Snapshot(
            Map<Identifier, List<JewelingRecipe>> byJewel,
            List<JewelingRecipe> all
    ) {
        private static final Snapshot EMPTY = new Snapshot(Map.of(), List.of());
    }
}
