package net.sidetopia.cosmetics.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GemTableGui extends SimpleGui {

    public GemTableGui(ServerPlayerEntity player) {
        // 6 rows chest GUI - can be replaced with a custom layout later
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.literal("Gem Table"));
    }
}
