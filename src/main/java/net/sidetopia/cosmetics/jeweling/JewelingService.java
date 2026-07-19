package net.sidetopia.cosmetics.jeweling;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.sidetopia.cosmetics.TeapotCosmetics;
import net.sidetopia.cosmetics.compat.KnowledgeBoundBridge;

import java.util.List;
import java.util.Optional;

public final class JewelingService {
    private final JewelingRecipeRegistry recipes;
    private final KnowledgeBoundBridge knowledgeBound;

    public JewelingService(
            JewelingRecipeRegistry recipes,
            KnowledgeBoundBridge knowledgeBound
    ) {
        this.recipes = recipes;
        this.knowledgeBound = knowledgeBound;
    }

    public JewelingEvaluation evaluate(
            ServerPlayerEntity player,
            ItemStack jewel,
            ItemStack equipment
    ) {
        JewelingConfig config = JewelingConfig.get();
        if (!config.jewelingEnabled) {
            return simpleFailure(JewelingStatus.FEATURE_DISABLED, config.messages.featureDisabled);
        }
        if (jewel.isEmpty() || equipment.isEmpty()) {
            return simpleFailure(JewelingStatus.EMPTY, "");
        }
        if (!jewel.isIn(JewelingTags.jewels())) {
            return simpleFailure(JewelingStatus.INVALID_JEWEL, config.messages.invalidJewel);
        }

        List<JewelingRecipe> jewelRecipes = recipes.find(jewel);
        if (jewelRecipes.isEmpty()) {
            return simpleFailure(JewelingStatus.NO_RECIPE, config.messages.noRecipe);
        }

        List<JewelingRecipe> matching = jewelRecipes.stream()
                .filter(recipe -> recipe.matches(equipment))
                .toList();
        if (matching.isEmpty()) {
            return simpleFailure(JewelingStatus.INVALID_EQUIPMENT, config.messages.unsupportedEquipment);
        }
        if (matching.size() > 1) {
            TeapotCosmetics.LOGGER.error(
                    "Ambiguous Jeweling recipes for jewel {} and equipment {}: {}",
                    jewel.getName().getString(),
                    equipment.getName().getString(),
                    matching.stream().map(recipe -> recipe.id().toString()).toList()
            );
            return simpleFailure(JewelingStatus.AMBIGUOUS_RECIPE, config.messages.ambiguousRecipe);
        }

        JewelingRecipe recipe = matching.getFirst();
        Registry<Enchantment> enchantments = player.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> enchantment = enchantments
                .getEntry(recipe.enchantment())
                .orElse(null);
        if (enchantment == null) {
            return failure(
                    JewelingStatus.MISSING_ENCHANTMENT,
                    config.messages.missingEnchantment,
                    recipe,
                    null,
                    0,
                    0,
                    0
            );
        }
        if (recipe.level() <= 0 || recipe.level() > enchantment.value().getMaxLevel()) {
            return failure(
                    JewelingStatus.INVALID_LEVEL,
                    config.messages.invalidLevel,
                    recipe,
                    enchantment,
                    0,
                    0,
                    0
            );
        }

        int currentSockets = JewelingMetadata.getSocketCount(
                equipment,
                player.getRegistryManager()
        );
        KnowledgeBoundBridge.JewellerPermission permission = knowledgeBound.check(
                player,
                recipe.requiredJewellerTier(),
                currentSockets
        );
        boolean operatorProgressionBypass = canOperatorBypassProgression(
                config.bypassKnowledgeBoundForOperators,
                player.hasPermissionLevel(2),
                permission.reason()
        );
        if (!permission.allowed() && !operatorProgressionBypass) {
            JewelingStatus status = switch (permission.reason()) {
                case KNOWLEDGEBOUND_REQUIRED -> JewelingStatus.KNOWLEDGEBOUND_REQUIRED;
                case SYSTEM_DISABLED -> JewelingStatus.SYSTEM_DISABLED;
                case TIER_TOO_LOW -> JewelingStatus.TIER_TOO_LOW;
                case SOCKET_LIMIT_REACHED -> JewelingStatus.SOCKET_LIMIT_REACHED;
                case ALLOWED -> JewelingStatus.SYSTEM_DISABLED;
            };
            String message = switch (status) {
                case KNOWLEDGEBOUND_REQUIRED -> config.messages.knowledgeBoundRequired;
                case TIER_TOO_LOW -> config.messages.tierTooLow;
                case SOCKET_LIMIT_REACHED -> config.messages.socketLimit;
                default -> config.messages.systemDisabled;
            };
            return failure(
                    status,
                    format(message, jewel, enchantment, permission),
                    recipe,
                    enchantment,
                    permission.playerTier(),
                    currentSockets,
                    permission.maximumSockets()
            );
        }

        if (!equipment.isIn(JewelingTags.jewelable()) || !recipe.matches(equipment)) {
            return failure(
                    JewelingStatus.INVALID_EQUIPMENT,
                    config.messages.invalidEquipment,
                    recipe,
                    enchantment,
                    permission.playerTier(),
                    currentSockets,
                    permission.maximumSockets()
            );
        }
        if (!enchantment.value().isSupportedItem(equipment)) {
            return failure(
                    JewelingStatus.INVALID_EQUIPMENT,
                    config.messages.unsupportedEquipment,
                    recipe,
                    enchantment,
                    permission.playerTier(),
                    currentSockets,
                    permission.maximumSockets()
            );
        }

        int existingLevel = EnchantmentHelper.getLevel(enchantment, equipment);
        if (existingLevel > 0) {
            boolean canReplace = recipe.duplicatePolicy()
                    == JewelingRecipe.DuplicatePolicy.REPLACE_IF_HIGHER
                    && recipe.level() > existingLevel;
            if (!canReplace) {
                return failure(
                        JewelingStatus.DUPLICATE_ENCHANTMENT,
                        config.messages.duplicateEnchantment,
                        recipe,
                        enchantment,
                        permission.playerTier(),
                        currentSockets,
                        permission.maximumSockets()
                );
            }
        }

        if (config.checkEnchantmentCompatibility) {
            for (RegistryEntry<Enchantment> existing
                    : EnchantmentHelper.getEnchantments(equipment).getEnchantments()) {
                if (!existing.matches(enchantment)
                        && !Enchantment.canBeCombined(existing, enchantment)) {
                    return failure(
                            JewelingStatus.INCOMPATIBLE_ENCHANTMENT,
                            config.messages.incompatibleEnchantment,
                            recipe,
                            enchantment,
                            permission.playerTier(),
                            currentSockets,
                            permission.maximumSockets()
                    );
                }
            }
        }

        ItemStack output = equipment.copy();
        output.setCount(1);
        JewelingMetadata.reconcile(output, player.getRegistryManager());
        EnchantmentHelper.apply(output, builder -> builder.set(enchantment, recipe.level()));
        JewelingMetadata.appendSocket(output, recipe);

        return new JewelingEvaluation(
                JewelingStatus.READY,
                Optional.of(recipe),
                Optional.of(enchantment),
                output,
                recipe.requiredJewellerTier(),
                permission.playerTier(),
                currentSockets,
                permission.maximumSockets(),
                Text.empty()
        );
    }

    public KnowledgeBoundBridge knowledgeBound() {
        return knowledgeBound;
    }

    private static JewelingEvaluation simpleFailure(JewelingStatus status, String message) {
        return new JewelingEvaluation(
                status,
                Optional.empty(),
                Optional.empty(),
                ItemStack.EMPTY,
                0,
                0,
                0,
                0,
                message.isEmpty() ? Text.empty() : Text.literal(message)
        );
    }

    private static JewelingEvaluation failure(
            JewelingStatus status,
            String message,
            JewelingRecipe recipe,
            RegistryEntry<Enchantment> enchantment,
            int playerTier,
            int currentSockets,
            int maximumSockets
    ) {
        return new JewelingEvaluation(
                status,
                Optional.ofNullable(recipe),
                Optional.ofNullable(enchantment),
                ItemStack.EMPTY,
                recipe == null ? 0 : recipe.requiredJewellerTier(),
                playerTier,
                currentSockets,
                maximumSockets,
                Text.literal(message)
        );
    }

    static boolean canOperatorBypassProgression(
            boolean enabled,
            boolean operator,
            KnowledgeBoundBridge.FailureReason reason
    ) {
        return enabled
                && operator
                && (reason == KnowledgeBoundBridge.FailureReason.TIER_TOO_LOW
                || reason == KnowledgeBoundBridge.FailureReason.SOCKET_LIMIT_REACHED);
    }

    private static String format(
            String template,
            ItemStack jewel,
            RegistryEntry<Enchantment> enchantment,
            KnowledgeBoundBridge.JewellerPermission permission
    ) {
        return template
                .replace("{jewel}", jewel.getName().getString())
                .replace("{enchantment}", enchantment.value().description().getString())
                .replace("{minTier}", String.valueOf(permission.requiredTier()))
                .replace("{playerTier}", String.valueOf(permission.playerTier()))
                .replace("{currentSockets}", String.valueOf(permission.currentSockets()))
                .replace("{maxSockets}", String.valueOf(permission.maximumSockets()));
    }
}
