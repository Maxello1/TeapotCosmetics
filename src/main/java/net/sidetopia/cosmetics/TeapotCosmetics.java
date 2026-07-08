package net.sidetopia.cosmetics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sidetopia.cosmetics.gui.GemTableGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeapotCosmetics implements ModInitializer {

    public static final String MOD_ID = "teapot_cosmetics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[TeapotCosmetics] Initializing...");

        // Tell Polymer to include our mod's custom assets in its auto-generated resource pack!
        eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.addModAssets(MOD_ID);

        // Open Gem Table GUI when player right-clicks the gem_table block
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            var blockState = world.getBlockState(hitResult.getBlockPos());
            var blockId = Registries.BLOCK.getId(blockState.getBlock());

            // Check if the block is our gem_table (Filament registers it under teapot_cosmetics namespace)
            if (blockId.equals(Identifier.of(MOD_ID, "gem_table"))) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    new GemTableGui(serverPlayer).open();
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }
}
