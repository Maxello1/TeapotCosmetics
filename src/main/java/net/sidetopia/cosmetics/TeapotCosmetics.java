package net.sidetopia.cosmetics;

import net.fabricmc.api.ModInitializer;
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

        net.sidetopia.cosmetics.item.ModItems.registerModItems();
    }
}
