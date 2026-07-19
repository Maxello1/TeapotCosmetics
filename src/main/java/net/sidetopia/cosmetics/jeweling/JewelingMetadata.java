package net.sidetopia.cosmetics.jeweling;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class JewelingMetadata {
    public static final int VERSION = 1;
    private static final String ROOT_KEY = "teapot_jeweling";
    private static final String LEGACY_KNOWLEDGEBOUND_COUNT = "knowledgebound_gem_count";

    private JewelingMetadata() {
    }

    public static List<SocketRecord> read(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        return data == null ? List.of() : read(data.copyNbt());
    }

    static List<SocketRecord> read(NbtCompound root) {
        if (root == null || !root.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) return List.of();
        NbtCompound jeweling = root.getCompound(ROOT_KEY);
        if (jeweling.getInt("version") != VERSION
                || !jeweling.contains("sockets", NbtElement.LIST_TYPE)) {
            return List.of();
        }

        NbtList sockets = jeweling.getList("sockets", NbtElement.COMPOUND_TYPE);
        List<SocketRecord> records = new ArrayList<>();
        for (int index = 0; index < sockets.size(); index++) {
            NbtCompound socket = sockets.getCompound(index);
            Identifier recipe = Identifier.tryParse(socket.getString("recipe"));
            Identifier jewel = Identifier.tryParse(socket.getString("jewel"));
            Identifier enchantment = Identifier.tryParse(socket.getString("enchantment"));
            int level = socket.getInt("level");
            if (recipe != null && jewel != null && enchantment != null && level > 0) {
                records.add(new SocketRecord(recipe, jewel, enchantment, level));
            }
        }
        return List.copyOf(records);
    }

    public static int getSocketCount(ItemStack stack, DynamicRegistryManager registryManager) {
        return liveRecords(stack, registryManager).size();
    }

    public static void reconcile(ItemStack stack, DynamicRegistryManager registryManager) {
        write(stack, liveRecords(stack, registryManager));
    }

    public static void appendSocket(ItemStack stack, JewelingRecipe recipe) {
        List<SocketRecord> records = new ArrayList<>(read(stack));
        records.add(new SocketRecord(
                recipe.id(),
                recipe.jewel(),
                recipe.enchantment(),
                recipe.level()
        ));
        write(stack, records);
    }

    private static List<SocketRecord> liveRecords(
            ItemStack stack,
            DynamicRegistryManager registryManager
    ) {
        Registry<Enchantment> enchantments = registryManager.get(RegistryKeys.ENCHANTMENT);
        List<SocketRecord> live = new ArrayList<>();
        for (SocketRecord record : read(stack)) {
            RegistryEntry<Enchantment> enchantment = enchantments
                    .getEntry(record.enchantment())
                    .orElse(null);
            if (enchantment != null && EnchantmentHelper.getLevel(enchantment, stack) > 0) {
                live.add(record);
            }
        }
        return List.copyOf(live);
    }

    private static void write(ItemStack stack, List<SocketRecord> sockets) {
        NbtComponent existing = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound root = writeRoot(
                existing == null ? new NbtCompound() : existing.copyNbt(),
                sockets
        );
        if (root.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
        }
    }

    static NbtCompound writeRoot(NbtCompound original, List<SocketRecord> sockets) {
        NbtCompound root = original == null ? new NbtCompound() : original.copy();
        root.remove(LEGACY_KNOWLEDGEBOUND_COUNT);
        if (sockets == null || sockets.isEmpty()) {
            root.remove(ROOT_KEY);
            return root;
        }

        NbtCompound jeweling = new NbtCompound();
        jeweling.putInt("version", VERSION);
        NbtList list = new NbtList();
        for (SocketRecord record : sockets) {
            NbtCompound socket = new NbtCompound();
            socket.putString("recipe", record.recipe().toString());
            socket.putString("jewel", record.jewel().toString());
            socket.putString("enchantment", record.enchantment().toString());
            socket.putInt("level", record.level());
            list.add(socket);
        }
        jeweling.put("sockets", list);
        root.put(ROOT_KEY, jeweling);
        return root;
    }

    public record SocketRecord(
            Identifier recipe,
            Identifier jewel,
            Identifier enchantment,
            int level
    ) {
    }
}
