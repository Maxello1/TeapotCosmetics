package net.sidetopia.cosmetics.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.sidetopia.cosmetics.jeweling.JewelingConfig;
import net.sidetopia.cosmetics.jeweling.JewelingEvaluation;
import net.sidetopia.cosmetics.jeweling.JewelingMetadata;
import net.sidetopia.cosmetics.jeweling.JewelingRecipe;
import net.sidetopia.cosmetics.jeweling.JewelingTags;
import net.sidetopia.cosmetics.jeweling.JewelingTableManager;

import java.util.List;

public final class JewelingCommands {

    private JewelingCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("teapot")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("jeweling")
                                .then(CommandManager.literal("reload")
                                        .executes(context -> reload(context.getSource())))
                                .then(CommandManager.literal("recipes")
                                        .executes(context -> recipes(context.getSource())))
                                .then(CommandManager.literal("inspect")
                                        .executes(context -> inspect(context.getSource()))
                                        .then(CommandManager.literal("jewel")
                                                .executes(context -> inspectJewel(context.getSource()))))
                                .then(CommandManager.literal("testmode")
                                        .executes(context -> testModeStatus(context.getSource()))
                                        .then(CommandManager.literal("on")
                                                .executes(context -> setTestMode(context.getSource(), true)))
                                        .then(CommandManager.literal("off")
                                                .executes(context -> setTestMode(context.getSource(), false)))))));
    }

    private static int reload(ServerCommandSource source) {
        JewelingTableManager.reloadConfiguration();
        source.sendFeedback(
                () -> Text.literal(
                        "Teapot Jeweling configuration reloaded. Use /reload for datapack enchantments and recipes."
                ),
                false
        );
        return 1;
    }

    private static int recipes(ServerCommandSource source) {
        List<JewelingRecipe> recipes = JewelingTableManager.recipes().all();
        Registry<Enchantment> enchantments = source.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT);
        source.sendFeedback(
                () -> Text.literal("Loaded Jeweling recipes: " + recipes.size()),
                false
        );
        if (recipes.isEmpty()) {
            source.sendFeedback(
                    () -> Text.literal(
                            "No recipes were found under data/<namespace>/jeweling_recipes/*.json."
                    ),
                    false
            );
        }
        for (JewelingRecipe recipe : recipes) {
            boolean jewelPresent = Registries.ITEM.containsId(recipe.jewel());
            boolean enchantmentPresent = enchantments.containsId(recipe.enchantment());
            source.sendFeedback(
                    () -> Text.literal(
                            recipe.id()
                                    + " | jewel=" + recipe.jewel()
                                    + " (" + (jewelPresent ? "resolved" : "missing") + ")"
                                    + " | enchantment=" + recipe.enchantment()
                                    + " (" + (enchantmentPresent ? "resolved" : "missing") + ")"
                                    + " | level=" + recipe.level()
                                    + " | tier=" + recipe.requiredJewellerTier()
                                    + " | items=" + recipe.allowedItems()
                                    + " | duplicate=" + recipe.duplicatePolicy().name().toLowerCase()
                    ),
                    false
            );
        }
        long missingJewels = recipes.stream()
                .filter(recipe -> !Registries.ITEM.containsId(recipe.jewel()))
                .count();
        long missingEnchantments = recipes.stream()
                .filter(recipe -> !enchantments.containsId(recipe.enchantment()))
                .count();
        source.sendFeedback(
                () -> Text.literal(
                        "Recipes: " + recipes.size()
                                + " | Missing jewels: " + missingJewels
                                + " | Missing enchantments: " + missingEnchantments
                ),
                false
        );
        return 1;
    }

    private static int inspect(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ItemStack jewel = player.getOffHandStack();
        ItemStack equipment = player.getMainHandStack();
        List<JewelingRecipe> candidates = JewelingTableManager.recipes().find(jewel);
        List<JewelingRecipe> selectorMatches = candidates.stream()
                .filter(recipe -> recipe.matches(equipment))
                .toList();
        JewelingRecipe selected = selectorMatches.size() == 1
                ? selectorMatches.getFirst()
                : null;

        Registry<Enchantment> enchantments = player.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> enchantment = selected == null
                ? null
                : enchantments.getEntry(selected.enchantment()).orElse(null);
        boolean supported = enchantment != null
                && enchantment.value().isSupportedItem(equipment);
        int existingLevel = enchantment == null
                ? 0
                : EnchantmentHelper.getLevel(enchantment, equipment);
        boolean duplicatePasses = selected != null && (
                existingLevel == 0
                        || (selected.duplicatePolicy()
                        == JewelingRecipe.DuplicatePolicy.REPLACE_IF_HIGHER
                        && selected.level() > existingLevel)
        );
        boolean conflicts = enchantment != null && EnchantmentHelper
                .getEnchantments(equipment)
                .getEnchantments()
                .stream()
                .anyMatch(existing -> !existing.matches(enchantment)
                        && !Enchantment.canBeCombined(existing, enchantment));

        JewelingEvaluation evaluation = JewelingTableManager.service().evaluate(
                player,
                jewel,
                equipment
        );
        int liveSockets = JewelingMetadata.getSocketCount(
                equipment,
                player.getRegistryManager()
        );

        source.sendFeedback(
                () -> Text.literal(
                        "Jewel=" + Registries.ITEM.getId(jewel.getItem())
                                + " | Equipment=" + Registries.ITEM.getId(equipment.getItem())
                ),
                false
        );
        source.sendFeedback(
                () -> Text.literal(
                        "Jewel recipes=" + recipeIds(candidates)
                                + " | Selector matches=" + recipeIds(selectorMatches)
                                + " | Selected=" + (selected == null ? "none" : selected.id())
                ),
                false
        );
        source.sendFeedback(
                () -> Text.literal(
                        "Selector=" + (selected == null ? "n/a" : "pass")
                                + " | Supported item=" + supported
                                + " | Existing level=" + existingLevel
                                + " | Duplicate check=" + duplicatePasses
                                + " | Conflict=" + conflicts
                ),
                false
        );
        source.sendFeedback(
                () -> Text.literal(
                        "Status=" + evaluation.status()
                                + " | Enchantment=" + evaluation.enchantment()
                                .map(RegistryEntry::getIdAsString)
                                .orElse(enchantment == null ? "none" : enchantment.getIdAsString())
                                + " | Tier=" + evaluation.playerTier()
                                + "/" + evaluation.requiredTier()
                                + " | Sockets=" + liveSockets
                                + "/" + evaluation.maximumSockets()
                                + " | Message=" + evaluation.failureMessage().getString()
                ),
                false
        );
        return evaluation.ready() ? 1 : 0;
    }

    private static int inspectJewel(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ItemStack inspected = player.getMainHandStack();
        if (inspected.isEmpty()) {
            inspected = player.getOffHandStack();
        }
        ItemStack jewel = inspected;
        List<JewelingRecipe> matches = JewelingTableManager.recipes().find(jewel);
        source.sendFeedback(
                () -> Text.literal(
                        "Registry ID=" + Registries.ITEM.getId(jewel.getItem())
                                + " | Jewel tag="
                                + (!jewel.isEmpty() && jewel.isIn(JewelingTags.jewels()))
                                + " | Matching recipes=" + matches.size()
                                + " | Recipe IDs=" + recipeIds(matches)
                ),
                false
        );
        return 1;
    }

    private static int testModeStatus(ServerCommandSource source) {
        boolean enabled = JewelingConfig.get().bypassKnowledgeBoundForOperators;
        source.sendFeedback(
                () -> Text.literal(
                        "Jeweling operator test mode is "
                                + (enabled ? "enabled" : "disabled")
                                + "."
                ),
                false
        );
        return 1;
    }

    private static int setTestMode(ServerCommandSource source, boolean enabled) {
        JewelingConfig.get().bypassKnowledgeBoundForOperators = enabled;
        source.sendFeedback(
                () -> Text.literal(
                        "Jeweling operator test mode "
                                + (enabled ? "enabled" : "disabled")
                                + ". Operators "
                                + (enabled ? "may now" : "no longer")
                                + " bypass Jeweller tier and socket limits."
                ),
                true
        );
        return 1;
    }

    private static String recipeIds(List<JewelingRecipe> recipes) {
        return recipes.isEmpty()
                ? "none"
                : recipes.stream().map(recipe -> recipe.id().toString()).toList().toString();
    }
}
