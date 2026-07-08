package net.sidetopia.cosmetics.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.sidetopia.cosmetics.TeapotCosmetics;

public class ModItems {

    // Single Items
    public static final Item COOKED_EGG = registerItem("cooked_egg", new PolymerModelItem(new Item.Settings(), Items.COOKED_COD, 1));
    public static final Item LANCE = registerItem("lance", new PolymerModelSwordItem(ToolMaterials.IRON, new Item.Settings(), Items.IRON_AXE, 1));
    public static final Item ROYAL_JELLY = registerItem("royal_jelly", new PolymerModelItem(new Item.Settings(), Items.HONEY_BOTTLE, 1));
    public static final Item HEAVY_CROSSBOW = registerItem("heavy_crossbow", new PolymerModelCrossbowItem(new Item.Settings().maxDamage(326), Items.CROSSBOW, 1));
    public static final Item STRONG_BOW = registerItem("strong_bow", new PolymerModelBowItem(new Item.Settings().maxDamage(384), Items.BOW, 1));
    public static final Item GOOD_ROD = registerItem("good_rod", new PolymerModelFishingRodItem(new Item.Settings().maxDamage(64), Items.FISHING_ROD, 1));
    public static final Item SUPER_ROD = registerItem("super_rod", new PolymerModelFishingRodItem(new Item.Settings().maxDamage(64), Items.FISHING_ROD, 2));

    // Gems (Lapis Lazuli based)
    public static final Item FLAWLESS_AMETHYST = registerGem("flawless_amethyst", 1);
    public static final Item FLAWLESS_EMERALD = registerGem("flawless_emerald", 2);
    public static final Item FLAWLESS_DIAMOND = registerGem("flawless_diamond", 3);
    public static final Item FLAWLESS_LAPIS = registerGem("flawless_lapis", 4);
    public static final Item FLAWLESS_QUARTZ = registerGem("flawless_quartz", 5);
    public static final Item AQUAMARINE = registerGem("aquamarine", 6);
    public static final Item SAPPHIRE = registerGem("sapphire", 7);
    public static final Item RUBY = registerGem("ruby", 8);
    public static final Item TOURMALINE = registerGem("tourmaline", 9);
    public static final Item TURQUOISE = registerGem("turquoise", 10);
    public static final Item BERYL = registerGem("beryl", 11);
    public static final Item PERIDOT = registerGem("peridot", 12);
    public static final Item CITRINE = registerGem("citrine", 13);
    public static final Item ONYX = registerGem("onyx", 14);
    public static final Item ROSE_QUARTZ = registerGem("rose_quartz", 15);
    public static final Item ZIRCON = registerGem("zircon", 16);
    public static final Item WITHERITE = registerGem("witherite", 17);
    public static final Item TOPAZ = registerGem("topaz", 18);
    public static final Item SERPENTINE = registerGem("serpentine", 19);
    public static final Item PEARL = registerGem("pearl", 20);
    public static final Item JADE = registerGem("jade", 21);
    public static final Item GARNET = registerGem("garnet", 22);
    public static final Item CINNABAR = registerGem("cinnabar", 23);
    public static final Item CELESTITE = registerGem("celestite", 24);
    public static final Item AMBER = registerGem("amber", 25);
    public static final Item ALEXANDRITE = registerGem("alexandrite", 26);
    public static final Item OPAL = registerGem("opal", 27);
    public static final Item PADPARADSCHA = registerGem("padparadscha", 28);

    private static Item registerGem(String name, int customModelData) {
        return registerItem(name, new PolymerModelItem(new Item.Settings(), Items.LAPIS_LAZULI, customModelData));
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(TeapotCosmetics.MOD_ID, name), item);
    }

    public static void registerModItems() {
        TeapotCosmetics.LOGGER.info("Registering Mod Items for " + TeapotCosmetics.MOD_ID);

        // Add items to creative tabs
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(FLAWLESS_AMETHYST);
            entries.add(FLAWLESS_EMERALD);
            entries.add(FLAWLESS_DIAMOND);
            entries.add(FLAWLESS_LAPIS);
            entries.add(FLAWLESS_QUARTZ);
            entries.add(AQUAMARINE);
            entries.add(SAPPHIRE);
            entries.add(RUBY);
            entries.add(TOURMALINE);
            entries.add(TURQUOISE);
            entries.add(BERYL);
            entries.add(PERIDOT);
            entries.add(CITRINE);
            entries.add(ONYX);
            entries.add(ROSE_QUARTZ);
            entries.add(ZIRCON);
            entries.add(WITHERITE);
            entries.add(TOPAZ);
            entries.add(SERPENTINE);
            entries.add(PEARL);
            entries.add(JADE);
            entries.add(GARNET);
            entries.add(CINNABAR);
            entries.add(CELESTITE);
            entries.add(AMBER);
            entries.add(ALEXANDRITE);
            entries.add(OPAL);
            entries.add(PADPARADSCHA);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(COOKED_EGG);
            entries.add(ROYAL_JELLY);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(LANCE);
            entries.add(STRONG_BOW);
            entries.add(HEAVY_CROSSBOW);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(GOOD_ROD);
            entries.add(SUPER_ROD);
        });
    }
}
