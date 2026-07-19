package net.sidetopia.cosmetics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.sidetopia.cosmetics.jeweling.JewelingConfig;
import net.sidetopia.cosmetics.jeweling.JewelingTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeapotCosmetics implements ModInitializer {

    public static final String MOD_ID = "teapot_cosmetics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[TeapotCosmetics] Initializing...");

        eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils.addModAssets(MOD_ID);
        JewelingTableManager.initialize();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            var blockState = world.getBlockState(hitResult.getBlockPos());
            var blockId = Registries.BLOCK.getId(blockState.getBlock());

            if (blockId.equals(JewelingConfig.get().gemTableBlockId())
                    && player instanceof ServerPlayerEntity serverPlayer
                    && world instanceof ServerWorld serverWorld) {
                JewelingTableManager.open(
                        serverPlayer,
                        serverWorld,
                        hitResult.getBlockPos()
                );
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }
}
